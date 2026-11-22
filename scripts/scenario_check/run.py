"""Scenario check driver: boot a production server and client, run scenarios over RCON, capture the client window.

Invoked by the scenarioCheck Gradle task with the env.json that
prepareUpgradeTest writes. See docs/scenario_check.md.
"""

import argparse
import json
import re
import shutil
import subprocess
import sys
import tempfile
import time
from pathlib import Path

from scripts.scenario_check import capture, images
from scripts.scenario_check.scenario import Runner, Scenario, ScenarioError, load_scenarios
from scripts.upgrade_test import compare, logcheck, processes
from scripts.upgrade_test.rcon import Rcon
from scripts.upgrade_test.run import PLAYER, Env, _end_session, _start_session

HERE = Path(__file__).resolve().parent
WINDOW_SIZE = (1280, 720)
# Keeps the RCON socket open through slow commands such as /locate.
RCON_TIMEOUT = 600
WINDOW_TIMEOUT = 120
# The join message and the first terrain need a moment before any shot.
SETTLE_SECONDS = 12

# Chat is hidden so command feedback and join messages stay out of the shots.
_CLIENT_OPTIONS = """onboardAccessibility:false
pauseOnLostFocus:false
skipMultiplayerWarning:true
joinedFirstServer:true
tutorialStep:none
renderDistance:8
simulationDistance:5
guiScale:2
chatVisibility:2
narrator:0
soundCategory_master:0.0
"""

# Run before every scenario so each one starts from the same conditions.
PREAMBLE = [
    "gamerule sendCommandFeedback false",
    "gamerule doDaylightCycle false",
    "gamerule doWeatherCycle false",
    "gamerule announceAdvancements false",
    "weather clear",
    "time set noon",
    f"gamemode spectator {PLAYER}",
    f"execute in minecraft:overworld run tp {PLAYER} 0 120 0 0 30",
]


# The server rejects the client's position update that races a scenario's
# teleport (most often across dimensions). The harness causes it, not the mod.
HARNESS_LOG_NOISE = re.compile(r"moved too quickly!")


class PermissionMissing(Exception):
    pass


def screen_locked() -> bool:
    """macOS keeps the lock state in the IORegistry root, readable without any permission."""
    result = subprocess.run(["ioreg", "-n", "Root", "-d1", "-a"], capture_output=True, text=True)
    return bool(re.search(r"<key>CGSSessionScreenIsLocked</key>\s*<true/>", result.stdout))


def preflight_capture():
    """Fail before booting anything when macOS will not let this process capture the screen."""
    if sys.platform != "darwin":
        raise PermissionMissing("scenarioCheck captures the client window with macOS screencapture")
    with tempfile.TemporaryDirectory() as tmp:
        probe = Path(tmp) / "probe.png"
        result = subprocess.run(["screencapture", "-x", "-R0,0,8,8", str(probe)], capture_output=True, text=True)
        if result.returncode != 0 or not probe.exists():
            if screen_locked():
                raise PermissionMissing(
                    "The screen is locked, and macOS refuses every capture while it is. Unlock the Mac "
                    "and run again.")
            raise PermissionMissing(
                "screencapture cannot capture the screen. Give the app that runs Gradle (Terminal, iTerm, "
                "the IDE, or the agent host) the Screen Recording permission in System Settings > Privacy & "
                f"Security, then restart it. screencapture said: {result.stderr.strip()!r}")


def wait_for_window(client, timeout=WINDOW_TIMEOUT):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        window = capture.find_window(client.pid)
        if window is not None:
            return window
        if not client.alive():
            raise processes.ProcessExited(client.exit_code, client.log_file)
        time.sleep(1)
    raise processes.ReadyTimeout(f"No client window for pid {client.pid} after {timeout}s")


def _shot_results(shots, scenario_name):
    results = []
    for shot in shots:
        problems = images.inspect(shot.file)
        if problems:
            results.append(("FAIL", f"{scenario_name}/shot:{shot.name}", "; ".join(problems)))
    return results


def write_summary(out_dir: Path, report: str, shots_by_scenario: dict, sheet: Path | None):
    lines = ["# Scenario check", "", "```", report.rstrip(), "```", ""]
    if sheet:
        lines += [f"Contact sheet: `{sheet}`", ""]
    for name, shots in shots_by_scenario.items():
        if not shots:
            continue
        lines += [f"## {name}", ""]
        for shot in shots:
            lines += [f"- `{shot.file}`", f"  - Look for: {shot.note or '(no note)'}"]
        lines.append("")
    (out_dir / "summary.md").write_text("\n".join(lines))


def run(env, scenarios, candidate_jar, out_dir, ports):
    if out_dir.exists():
        shutil.rmtree(out_dir)
    out_dir.mkdir(parents=True)
    server_dir = out_dir / "server"
    client_dir = out_dir / "client"
    processes.preflight_ports(list(ports))
    processes.prepare_server_dir(env.server, server_dir, ports)
    client_dir.mkdir(parents=True)
    (client_dir / "options.txt").write_text(_CLIENT_OPTIONS)
    mods = [candidate_jar, *env.prod_mods]
    print(f"[{env.loader}] candidate: {candidate_jar.name}, scenarios: {', '.join(s.name for s in scenarios)}",
          flush=True)

    results = []
    shots_by_scenario = {}
    client_args = ["--width", str(WINDOW_SIZE[0]), "--height", str(WINDOW_SIZE[1])]
    server, client = _start_session(env, server_dir, client_dir, mods, ports, "scenario", out_dir, client_args)
    try:
        window = wait_for_window(client)
        time.sleep(SETTLE_SECONDS)
        with Rcon("127.0.0.1", ports[1], processes.RCON_PASSWORD, timeout=RCON_TIMEOUT) as rcon:
            for scenario in scenarios:
                scenario_dir = out_dir / scenario.name
                shots_dir = scenario_dir / "shots"

                def take(name, shots_dir=shots_dir):
                    if not client.alive():
                        raise processes.ProcessExited(client.exit_code, client.log_file)
                    return capture.capture_window(window, shots_dir / f"{name}.png")

                print(f"[{env.loader}] scenario {scenario.name}", flush=True)
                runner = Runner(rcon, take, PLAYER)
                outcome = runner.run(Scenario(f"{scenario.name}/preamble", "", PREAMBLE))
                if not outcome.results:
                    main_part = runner.run(scenario)
                    outcome.results += main_part.results
                    outcome.shots += main_part.shots
                    outcome.transcript += main_part.transcript
                scenario_dir.mkdir(parents=True, exist_ok=True)
                (scenario_dir / "transcript.txt").write_text("\n".join(outcome.transcript) + "\n")
                results += outcome.results + _shot_results(outcome.shots, scenario.name)
                shots_by_scenario[scenario.name] = outcome.shots
        _end_session(server, client, ports)
    finally:
        client.kill()
        server.kill()

    all_shots = [(f"{name}/{s.name}", s.file, s.note) for name, shots in shots_by_scenario.items() for s in shots]
    sheet = images.contact_sheet(all_shots, out_dir / "sheet.png", f"scenarioCheck {env.loader} 1.21.1, "
                                 f"{candidate_jar.name}") if all_shots else None

    server_problems = [line for line in logcheck.check_server_log(out_dir / "scenario-server.log",
                                                                  env.log_ignore_file)
                       if not HARNESS_LOG_NOISE.search(line)]
    results.append(("FAIL" if server_problems else "PASS", "server-log", "\n  ".join(server_problems[:30])))
    client_problems = logcheck.check_client_log(out_dir / "scenario-client.log", client_dir, env.log_ignore_file)
    results.append(("FAIL" if client_problems else "PASS", "client-log", "\n  ".join(client_problems[:30])))
    return results, shots_by_scenario, sheet


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--env", required=True, type=Path)
    parser.add_argument("--out-dir", required=True, type=Path)
    parser.add_argument("--ports", required=True, help="server port and RCON port, comma-separated")
    parser.add_argument("--scenarios", help="comma-separated scenario names; default: all")
    parser.add_argument("--candidate-jar", type=Path)
    args = parser.parse_args(argv)

    env = Env(json.loads(args.env.read_text()))
    ports = tuple(int(p) for p in args.ports.split(","))
    names = [n.strip() for n in args.scenarios.split(",") if n.strip()] if args.scenarios else None
    try:
        scenarios = load_scenarios(HERE / "scenarios", names)
    except ScenarioError as e:
        print(f"Invalid scenario: {e}", file=sys.stderr)
        return 2
    try:
        preflight_capture()
    except PermissionMissing as e:
        print(str(e), file=sys.stderr)
        return 2
    candidate = args.candidate_jar or env.candidate_jar
    processes.install_signal_handlers()

    out_dir = args.out_dir.resolve()
    shots_by_scenario, sheet = {}, None
    try:
        results, shots_by_scenario, sheet = run(env, scenarios, candidate, out_dir, ports)
    except Exception as e:
        results = [("FAIL", "harness", f"{type(e).__name__}: {e}")]

    report = f"scenarioCheck {env.loader} 1.21.1, candidate {candidate.name}\n" + compare.format_report(results)
    out_dir.mkdir(parents=True, exist_ok=True)
    (out_dir / "report.txt").write_text(report)
    write_summary(out_dir, report, shots_by_scenario, sheet)
    print(report, flush=True)
    failed = [r for r in results if r[0] in ("FAIL", "SETUP FAILED")]
    print(f"{len(failed)} failure(s). Report: {out_dir / 'report.txt'}, summary: {out_dir / 'summary.md'}"
          + (f", contact sheet: {sheet}" if sheet else ""), flush=True)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
