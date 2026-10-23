"""Preservation comparison: what existed before the upgrade must still be there, unchanged."""

import re
from dataclasses import dataclass

# RCON replies that mean the setup did not produce what the check reads.
_SETUP_FAILURE_MARKERS = ("No entity was found", "is not a container", "Unknown", "Incorrect argument",
                          "Test failed", "That position is not loaded", "Found no elements",
                          "is not a block entity")
# Replies to setup commands (setblock, item replace, give, summon, ...) that mean the command failed.
_SETUP_COMMAND_ERRORS = ("Unknown", "Incorrect argument", "<--[HERE]", "Could not", "No entity was found",
                         "No player was found", "not loaded", "Found no elements", "is not a block entity",
                         "is not a container", "Invalid", "Expected")
_EMPTY_DATA = re.compile(r"has the following (block|entity) data: (\[\]|\{\})$")


class SetupFailed(Exception):
    def __init__(self, check_id, value):
        super().__init__(f"{check_id}: {value!r}")
        self.check_id = check_id
        self.value = value


@dataclass
class Difference:
    path: str
    before: object
    after: object
    reason: str


def looks_unready(value) -> bool:
    """A check reply that reads nothing: missing target, unloaded chunk, or an empty container."""
    return isinstance(value, str) and (any(marker in value for marker in _SETUP_FAILURE_MARKERS)
                                       or bool(_EMPTY_DATA.search(value.strip())))


def setup_reply_failed(reply: str) -> bool:
    return any(marker in reply for marker in _SETUP_COMMAND_ERRORS)


def check_before_present(check_id: str, value, command_reply: bool = True) -> None:
    if value is None or value == "" or value == {} or value == []:
        raise SetupFailed(check_id, value)
    if command_reply and looks_unready(value):
        raise SetupFailed(check_id, value)


def _first_int(value):
    match = re.search(r"-?\d+", str(value))
    return int(match.group()) if match else None


def compare_values(before, after, mode: str = "equal", monotonic=()) -> list[Difference]:
    """monotonic: dotted paths of counters that keep running (game clocks) and may only grow."""
    if mode == "int_gte":
        b, a = _first_int(before), _first_int(after)
        if b is None or a is None or a < b:
            return [Difference("", before, after, "not >= before")]
        return []
    diffs = []
    _compare(before, after, "", diffs, set(monotonic))
    return diffs


def _join(path, key):
    return f"{path}.{key}" if path else str(key)


def _compare(before, after, path, diffs, monotonic):
    if path in monotonic and isinstance(before, (int, float)) and isinstance(after, (int, float)):
        if after < before:
            diffs.append(Difference(path, before, after, "decreased"))
        return
    if isinstance(before, dict):
        if not isinstance(after, dict):
            diffs.append(Difference(path, before, after, "type changed"))
            return
        for key, value in before.items():
            if key not in after:
                diffs.append(Difference(_join(path, key), value, None, "missing"))
            else:
                _compare(value, after[key], _join(path, key), diffs, monotonic)
    elif isinstance(before, list):
        if not isinstance(after, list):
            diffs.append(Difference(path, before, after, "type changed"))
            return
        for i, value in enumerate(before):
            item_path = f"{path}[{i}]"
            if i >= len(after):
                diffs.append(Difference(item_path, value, None, "missing"))
            else:
                _compare(value, after[i], item_path, diffs, monotonic)
    elif before != after:
        diffs.append(Difference(path, before, after, "changed"))


def format_report(results) -> str:
    """results: iterable of (status, check_id, detail) with status PASS, FAIL, SETUP FAILED or NOT COVERED."""
    lines = []
    for status, check_id, detail in results:
        lines.append(f"{status} {check_id}" + (f": {detail}" if detail else ""))
    return "\n".join(lines) + "\n"


def describe(diffs: list[Difference]) -> str:
    return "; ".join(f"{d.path or '<value>'} before={d.before!r} after={d.after!r} ({d.reason})" for d in diffs)
