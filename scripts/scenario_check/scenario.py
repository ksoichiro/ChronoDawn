"""Scenario files and the step interpreter.

A scenario is a JSON file in scripts/scenario_check/scenarios/:

    {
      "description": "What the scenario checks",
      "steps": [ ... ]
    }

Each step is one of:

- "command"                      Run an RCON command. A reply that looks like an
                                 error stops the scenario (SETUP FAILED).
- {"command": "...", "match": "regex", "check": "id", "await": true,
   "timeout": 60, "max_seconds": 10}
                                 Run a command and search its reply for match.
                                 Named groups ((?P<x>...)) become variables.
                                 With "await", re-run every half second until
                                 it matches or timeout passes. With "check", the
                                 outcome is reported as PASS or FAIL. Without
                                 it, a mismatch is a SETUP FAILED. max_seconds
                                 fails a check whose command took longer.
- {"settle": 60}                 Wait until the client holds the chunks around
                                 the player and has built them: nothing is
                                 missing within 3 chunks, no section builds are
                                 queued, and the chunk and visible-section counts
                                 have not changed for 2 seconds. The value is the
                                 timeout in seconds. A timeout is a FAIL, and the
                                 scenario goes on so the shots still show what
                                 the client had.
- {"sleep": 2, "reason": "why no condition can replace it"}
                                 Wait a fixed time. Prefer an await, settle or
                                 screen step. The reason is required.
- {"shot": "name", "note": "what to look at", "settle": 30, "wait": 1,
   "reason": "..."}
                                 Settle (timeout "settle", default 30; false to
                                 skip), then take a screenshot in the client. The
                                 note tells the reviewer what the image must
                                 show. "wait" adds a fixed wait after settling
                                 and needs a reason.
                                 Every shot is also checked automatically: not a
                                 single color, no missing-texture magenta, and a
                                 mean brightness of at least 15 (0-255). Any
                                 miss is a FAIL. "expect" adds rules for one
                                 shot: {"min_brightness": 5} overrides the
                                 brightness floor (0 turns it off, for a dark
                                 cave), and {"colors": [{"rgb": "#5a8c3c",
                                 "tolerance": 40, "min_ratio": 0.02}]} needs
                                 that color (within tolerance on each channel,
                                 default 30) to cover at least min_ratio and/or
                                 at most max_ratio of the frame, as a stand-in
                                 for "the grass, the sky or the block is there".
- {"input": "use"}               Act as the player in the client: "use" clicks
                                 the right mouse button once (open a trader,
                                 a container, place a block), "attack" the left
                                 one (hit a mob, break a block in creative).
                                 It acts on whatever the crosshair points at, so
                                 tp the player facing the target first, in a
                                 game mode that allows it (not spectator). It
                                 fails while a screen is open. "close" closes
                                 the open screen and is fine with none.
- {"input": "use", "until": "command", "match": "regex", "repeat": 30}
                                 Click, then run the command, and click again
                                 until its reply matches, at most repeat times.
                                 Running out of clicks is a SETUP FAILED. Use it
                                 instead of a fixed number of clicks with sleeps,
                                 for example to bone-meal a sapling until it
                                 grows.
- {"screen": "regex", "check": "id", "timeout": 10}
                                 Wait until the title of the screen open in the
                                 client matches (an empty title means no
                                 screen). check and the outcome work as for a
                                 command with match and await.

- {"click": [dx, dy], "button": 0}
                                 Click inside the open screen at GUI-scaled
                                 coordinates relative to the screen center (x to
                                 the right, y down), for buttons and lists. The
                                 step fails if nothing took the click.
- {"slot": 2, "button": 0, "action": "pickup"}
                                 Click a slot of the open container as the game
                                 does. "quick_move" is a shift-click. Slot
                                 indexes are the menu's, for example 2 is the
                                 result of a trader or a crafting table.
- {"hover": [dx, dy]} or {"hover_slot": 5}
                                 Park the mouse over a point or a slot so the next
                                 shot shows its tooltip.
- {"scroll": [dx, dy], "amount": -5}
                                 Scroll the wheel there (positive scrolls up).

Commands and notes may use ${name} for captured variables and ${player} for
the test player's name. Every reply is written to the transcript.
"""

import json
import re
import time
from dataclasses import dataclass, field
from pathlib import Path

from scripts.scenario_check import images
from scripts.upgrade_test import compare

_VAR = re.compile(r"\$\{(\w+)\}")
DEFAULT_AWAIT_TIMEOUT = 120
DEFAULT_SHOT_SETTLE = 30
DEFAULT_SCREEN_TIMEOUT = 10
DEFAULT_INPUT_REPEAT = 30
INPUTS = ("use", "attack", "close")
AWAIT_POLL_SECONDS = 0.5
SCREEN_POLL_SECONDS = 0.25
SETTLE_POLL_SECONDS = 0.25
# Long enough that a pause between two chunk packets does not look settled.
SETTLE_STABLE_SECONDS = 2
# A click reaches the server one client tick and one server tick later, and
# an RCON command runs between server ticks, so give it a few ticks.
INPUT_EFFECT_SECONDS = 0.25
NOT_IN_WORLD = "no player in a world"
_RENDER = re.compile(r"chunks=(\d+) missing=(\d+) sections=(\d+) queue=(\w+)")


class ScenarioError(Exception):
    """The scenario file itself is invalid."""


class StepFailed(Exception):
    """A step failed and the rest of the scenario cannot run."""

    def __init__(self, status, message):
        super().__init__(message)
        self.status = status


@dataclass
class Scenario:
    name: str
    description: str
    steps: list
    path: Path | None = None


@dataclass
class Shot:
    name: str
    file: Path
    note: str
    expect: dict = field(default_factory=dict)
    checks: list = field(default_factory=list)  # (name, passed, detail), filled by the driver


@dataclass
class Outcome:
    results: list = field(default_factory=list)  # (status, id, detail)
    shots: list = field(default_factory=list)
    transcript: list = field(default_factory=list)
    timings: list = field(default_factory=list)  # (step number, kind, seconds)

    @property
    def seconds(self):
        return sum(t[2] for t in self.timings)


def step_kind(step):
    if isinstance(step, str):
        return "command"
    return next((k for k in _STEP_KEYS if k in step), "command")


_STEP_KEYS = {
    "command": {"command", "match", "check", "await", "timeout", "max_seconds"},
    # "shot" comes before "settle" because a shot step may carry a settle option.
    "shot": {"shot", "wait", "note", "settle", "reason", "expect"},
    "settle": {"settle"},
    "sleep": {"sleep", "reason"},
    "input": {"input", "until", "match", "repeat"},
    "screen": {"screen", "check", "timeout"},
    "click": {"click", "button"},
    "slot": {"slot", "button", "action"},
    "hover": {"hover"},
    "hover_slot": {"hover_slot"},
    "scroll": {"scroll", "amount"},
}
SLOT_ACTIONS = ("pickup", "quick_move")
SCREEN_OPERATIONS = ("click", "slot", "hover", "hover_slot", "scroll")


def _is_int(value):
    return isinstance(value, int) and not isinstance(value, bool)


def _is_number(value):
    return isinstance(value, (int, float)) and not isinstance(value, bool)


def _screen_operation(step):
    """The line the agent reads for a click, slot, hover or scroll step."""
    if "click" in step:
        dx, dy = step["click"]
        return f"click {dx} {dy} {step.get('button', 0)}"
    if "slot" in step:
        return f"slot {step['slot']} {step.get('button', 0)} {step.get('action', 'pickup')}"
    if "hover" in step:
        dx, dy = step["hover"]
        return f"hover {dx} {dy}"
    if "hover_slot" in step:
        return f"hover_slot {step['hover_slot']}"
    dx, dy = step["scroll"]
    return f"scroll {dx} {dy} {step['amount']}"


def _kinds(step):
    kinds = [k for k in _STEP_KEYS if k in step]
    return [k for k in kinds if k != "settle"] if "shot" in kinds else kinds


def _validate_step(name, index, step):
    where = f"{name} step {index + 1}"
    if isinstance(step, str):
        return
    if not isinstance(step, dict):
        raise ScenarioError(f"{where}: a step is a string or an object, got {type(step).__name__}")
    kinds = _kinds(step)
    if len(kinds) != 1:
        raise ScenarioError(f"{where}: needs exactly one of {sorted(_STEP_KEYS)}, got {sorted(step)}")
    unknown = set(step) - _STEP_KEYS[kinds[0]]
    if unknown:
        raise ScenarioError(f"{where}: unknown keys {sorted(unknown)}")
    if kinds[0] == "input" and step["input"] not in INPUTS:
        raise ScenarioError(f"{where}: input is one of {list(INPUTS)}, got {step['input']!r}")
    if kinds[0] in ("click", "hover", "scroll"):
        point = step[kinds[0]]
        if not (isinstance(point, list) and len(point) == 2 and all(_is_number(v) for v in point)):
            raise ScenarioError(f"{where}: {kinds[0]} is [dx, dy] relative to the screen center, got {point!r}")
    if kinds[0] == "scroll" and not _is_number(step.get("amount")):
        raise ScenarioError(f"{where}: scroll needs a numeric amount")
    if kinds[0] in ("slot", "hover_slot") and not _is_int(step[kinds[0]]):
        raise ScenarioError(f"{where}: {kinds[0]} is a slot index, got {step[kinds[0]]!r}")
    if "button" in step and not _is_int(step["button"]):
        raise ScenarioError(f"{where}: button is a mouse button number, got {step['button']!r}")
    if kinds[0] == "slot" and step.get("action", "pickup") not in SLOT_ACTIONS:
        raise ScenarioError(f"{where}: action is one of {list(SLOT_ACTIONS)}, got {step['action']!r}")
    if kinds[0] == "input" and ("until" in step) != ("match" in step):
        raise ScenarioError(f"{where}: until and match go together")
    if kinds[0] == "input" and "repeat" in step and "until" not in step:
        raise ScenarioError(f"{where}: repeat needs until and match")
    if kinds[0] == "input" and "repeat" in step and (
            isinstance(step["repeat"], bool) or not isinstance(step["repeat"], int) or step["repeat"] < 1):
        raise ScenarioError(f"{where}: repeat is a positive whole number")
    if kinds[0] == "input" and "match" in step:
        try:
            re.compile(step["match"])
        except re.error as e:
            raise ScenarioError(f"{where}: invalid match regex: {e}") from e
    if kinds[0] in ("settle", "shot") and "settle" in step and not (step["settle"] is False and kinds[0] == "shot") and (
            isinstance(step["settle"], bool) or not isinstance(step["settle"], (int, float)) or step["settle"] <= 0):
        raise ScenarioError(f"{where}: settle is a timeout in seconds" + (" or false" if kinds[0] == "shot" else ""))
    # A fixed wait is the usual source of a slow scenario, so each one has to
    # say why no condition (await, settle, screen) can replace it.
    if (kinds[0] == "sleep" or (kinds[0] == "shot" and step.get("wait"))) and not step.get("reason"):
        raise ScenarioError(f"{where}: a fixed wait needs a reason; prefer an await, settle or screen step")
    if kinds[0] == "screen":
        try:
            re.compile(step["screen"])
        except re.error as e:
            raise ScenarioError(f"{where}: invalid screen regex: {e}") from e
    if kinds[0] == "command":
        if ("await" in step or "max_seconds" in step or "check" in step) and "match" not in step:
            raise ScenarioError(f"{where}: await, check and max_seconds need match")
        if "match" in step:
            try:
                re.compile(step["match"])
            except re.error as e:
                raise ScenarioError(f"{where}: invalid match regex: {e}") from e
    if kinds[0] == "shot" and not re.fullmatch(r"[A-Za-z0-9_.-]+", step["shot"]):
        raise ScenarioError(f"{where}: shot names may use letters, digits, '.', '_' and '-' only")
    if kinds[0] == "shot" and "expect" in step:
        try:
            images.validate_expect(step["expect"])
        except ValueError as e:
            raise ScenarioError(f"{where}: {e}") from e


def load_scenario(path: Path) -> Scenario:
    path = Path(path)
    raw = json.loads(path.read_text())
    name = path.stem
    if not isinstance(raw.get("steps"), list) or not raw["steps"]:
        raise ScenarioError(f"{name}: needs a non-empty steps list")
    for index, step in enumerate(raw["steps"]):
        _validate_step(name, index, step)
    shots = [s["shot"] for s in raw["steps"] if isinstance(s, dict) and "shot" in s]
    if len(shots) != len(set(shots)):
        raise ScenarioError(f"{name}: shot names must be unique")
    return Scenario(name, raw.get("description", ""), raw["steps"], path)


def load_scenarios(directory: Path, names: list[str] | None = None) -> list[Scenario]:
    directory = Path(directory)
    if names:
        missing = [n for n in names if not (directory / f"{n}.json").exists()]
        if missing:
            available = sorted(p.stem for p in directory.glob("*.json"))
            raise ScenarioError(f"unknown scenario(s) {missing}; available: {available}")
        return [load_scenario(directory / f"{n}.json") for n in names]
    return [load_scenario(p) for p in sorted(directory.glob("*.json"))]


def substitute(text: str, variables: dict) -> str:
    def replace(match):
        key = match.group(1)
        if key not in variables:
            raise StepFailed("SETUP FAILED", f"undefined variable ${{{key}}} in {text!r}")
        return str(variables[key])
    return _VAR.sub(replace, text)


class Runner:
    """Runs a scenario's steps against an RCON connection.

    capture(name) takes a screenshot and returns its file. client(operation)
    sends an input or screen operation to the client and returns its answer.
    clock and sleep are injectable so the unit tests do not wait.
    """

    def __init__(self, rcon, capture, player, client=None, clock=time.monotonic, sleep=time.sleep):
        self.rcon = rcon
        self.capture = capture
        self.player = player
        self.client = client
        self.clock = clock
        self.sleep = sleep

    def run(self, scenario: Scenario) -> Outcome:
        outcome = Outcome()
        variables = {"player": self.player}
        for index, step in enumerate(scenario.steps):
            started = self.clock()
            try:
                self._step(scenario.name, step, variables, outcome)
            except StepFailed as e:
                outcome.results.append((e.status, f"{scenario.name}/step{index + 1}", str(e)))
                skipped = [s["shot"] for s in scenario.steps[index + 1:] if isinstance(s, dict) and "shot" in s]
                if skipped:
                    outcome.results.append(("NOT COVERED", f"{scenario.name}/shots",
                                            f"skipped after the failure: {', '.join(skipped)}"))
                break
            finally:
                seconds = self.clock() - started
                outcome.timings.append((index + 1, step_kind(step), seconds))
                outcome.transcript.append(f"# step {index + 1} ({step_kind(step)}): {seconds:.1f}s")
        return outcome

    def _command(self, command, outcome, prefix="> "):
        reply = self.rcon.command(command)
        outcome.transcript.append(f"{prefix}{command}\n{reply}")
        return reply

    def _step(self, name, step, variables, outcome):
        if isinstance(step, str):
            command = substitute(step, variables)
            reply = self._command(command, outcome)
            if compare.setup_reply_failed(reply):
                raise StepFailed("SETUP FAILED", f"{command!r} -> {reply!r}")
            return
        if "sleep" in step:
            self.sleep(step["sleep"])
            return
        if "shot" in step:
            settle = step.get("settle", DEFAULT_SHOT_SETTLE)
            if settle is not False:
                self._settle(f"{name}/shot:{step['shot']}:settle", settle, outcome)
            if step.get("wait"):
                self.sleep(step["wait"])
            note = substitute(step.get("note", ""), variables)
            try:
                file = self.capture(step["shot"])
            except Exception as e:  # a failed capture should not hide the other shots
                outcome.results.append(("FAIL", f"{name}/shot:{step['shot']}", f"{type(e).__name__}: {e}"))
                return
            outcome.shots.append(Shot(step["shot"], file, note, step.get("expect", {})))
            outcome.results.append(("SHOT", f"{name}/shot:{step['shot']}", note))
            return
        if "settle" in step:
            self._settle(f"{name}/settle", step["settle"], outcome)
            return
        if "input" in step:
            self._input_step(step, variables, outcome)
            return
        if "screen" in step:
            self._screen_step(name, step, outcome)
            return
        for kind in SCREEN_OPERATIONS:
            if kind in step:
                operation = _screen_operation(step)
                answer = self._client(operation, outcome)
                if answer.startswith("error:"):
                    raise StepFailed("SETUP FAILED", f"{operation!r} -> {answer!r}")
                return
        self._command_step(name, step, variables, outcome)

    def _client(self, operation, outcome):
        if self.client is None:
            raise StepFailed("SETUP FAILED", f"no client to send {operation!r} to")
        try:
            answer = self.client(operation)
        except Exception as e:
            raise StepFailed("SETUP FAILED", f"{operation!r}: {type(e).__name__}: {e}") from e
        outcome.transcript.append(f"> (client) {operation}\n{answer}")
        return answer

    def _input_step(self, step, variables, outcome):
        until = substitute(step["until"], variables) if "until" in step else None
        repeat = step.get("repeat", DEFAULT_INPUT_REPEAT) if until else 1
        for _ in range(repeat):
            answer = self._client(step["input"], outcome)
            if answer.startswith("error:"):
                raise StepFailed("SETUP FAILED", f"input {step['input']!r} -> {answer!r}")
            if until is None:
                return
            self.sleep(INPUT_EFFECT_SECONDS)
            if re.search(step["match"], self._command(until, outcome)):
                return
        raise StepFailed("SETUP FAILED", f"{until!r} did not match /{step['match']}/ after {repeat} {step['input']!r}")

    def _settle(self, label, timeout, outcome):
        """Wait until the client reports the world around the player loaded and built."""
        if self.client is None:
            raise StepFailed("SETUP FAILED", "no client to wait for")
        start = self.clock()
        stable_key, stable_since, answer = None, None, ""
        while True:
            try:
                answer = self.client("render")
            except Exception as e:
                raise StepFailed("SETUP FAILED", f"'render': {type(e).__name__}: {e}") from e
            now = self.clock()
            if answer.startswith("error:") and NOT_IN_WORLD not in answer:
                # Anything but "not in a world yet" will not go away by waiting, so fail fast.
                raise StepFailed("SETUP FAILED", f"'render' -> {answer!r}")
            state = _RENDER.search(answer)
            # During a join or a dimension change the player is briefly not in a world. That is not ready yet.
            if state and state.group(2) == "0" and state.group(4) == "empty":
                key = (state.group(1), state.group(3))
                if key != stable_key:
                    stable_key, stable_since = key, now
                elif now - stable_since >= SETTLE_STABLE_SECONDS:
                    outcome.transcript.append(f"> (client) settle\n{answer} after {now - start:.1f}s")
                    return
            else:
                stable_key = None
            if now - start >= timeout:
                detail = f"the client did not settle within {timeout}s: {answer!r}"
                outcome.transcript.append(f"> (client) settle\n{detail}")
                outcome.results.append(("FAIL", label, detail))
                return
            self.sleep(SETTLE_POLL_SECONDS)

    def _screen_step(self, name, step, outcome):
        pattern = re.compile(step["screen"])
        timeout = step.get("timeout", DEFAULT_SCREEN_TIMEOUT)
        start = self.clock()
        while True:
            title = self._client("screen", outcome)
            if title.startswith("error:"):
                raise StepFailed("SETUP FAILED", f"screen -> {title!r}")
            if pattern.search(title):
                break
            if self.clock() - start >= timeout:
                detail = f"screen title {title!r} did not match /{step['screen']}/ within {timeout}s"
                if step.get("check"):
                    outcome.results.append(("FAIL", f"{name}/{step['check']}", detail))
                    raise StepFailed("NOT COVERED", "later steps depend on the failed check")
                raise StepFailed("SETUP FAILED", detail)
            self.sleep(SCREEN_POLL_SECONDS)
        if step.get("check"):
            outcome.results.append(("PASS", f"{name}/{step['check']}", f"screen: {title!r}"))

    def _command_step(self, name, step, variables, outcome):
        command = substitute(step["command"], variables)
        pattern = re.compile(step["match"]) if "match" in step else None
        timeout = step.get("timeout", DEFAULT_AWAIT_TIMEOUT)
        start = self.clock()
        while True:
            issued = self.clock()
            reply = self._command(command, outcome, "> (await) " if step.get("await") else "> ")
            elapsed = self.clock() - issued
            match = pattern.search(reply) if pattern else None
            if pattern is None or match or not step.get("await") or self.clock() - start >= timeout:
                break
            self.sleep(AWAIT_POLL_SECONDS)
        check = step.get("check")
        if pattern is None:
            if compare.setup_reply_failed(reply):
                raise StepFailed("SETUP FAILED", f"{command!r} -> {reply!r}")
            return
        if not match:
            detail = f"{command!r} did not match /{step['match']}/: {reply[:500]!r}"
            if check:
                outcome.results.append(("FAIL", f"{name}/{check}", detail))
                raise StepFailed("NOT COVERED", "later steps depend on the failed check")
            raise StepFailed("SETUP FAILED", detail)
        variables.update({k: v for k, v in match.groupdict().items() if v is not None})
        if not check:
            return
        limit = step.get("max_seconds")
        timing = f"{elapsed:.1f}s"
        if limit is not None and elapsed > limit:
            outcome.results.append(("FAIL", f"{name}/{check}", f"took {timing}, limit {limit}s: {reply[:300]}"))
            return
        outcome.results.append(("PASS", f"{name}/{check}", f"{timing}: {reply[:300]}"))
