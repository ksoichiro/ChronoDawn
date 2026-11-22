# Scenario Check

`scenarioCheck` turns an in-game check into a command. It boots the freshly
built JAR on a production server and client for Minecraft 1.21.1, runs
scenario files over RCON, judges the replies, and captures the client window
at the points each scenario names. The owner can review the result remotely
from the report and one contact sheet image, without starting the game.

It reuses the runtimes, `env.json`, Python venv, and process helpers of the
upgrade test (`gradle/upgrade-test.gradle`, `scripts/upgrade_test/`).

## Running

Run it outside the sandbox (JVM forks, local ports, network for the first
provisioning). A game window opens for the length of the run.

```bash
./gradlew :neoforge:scenarioCheck -Ptarget_mc_version=1.21.1
./gradlew :fabric:scenarioCheck -Ptarget_mc_version=1.21.1 -Pscenario_check_scenarios=polished-stones
```

- `scenario_check_scenarios`: comma-separated scenario names (file names
  without `.json`). Default: every file in `scripts/scenario_check/scenarios/`.
- `upgrade_test_candidate_jar`: test another mod JAR instead of the fresh build.

Each run starts from a new world (seed `20261023`), so runs do not affect each
other. Ports: Fabric 25631/25632, NeoForge 25641/25642.

### Screen Recording permission

The window is captured with `screencapture -l <window id>`, so the app that
runs Gradle needs the Screen Recording permission (System Settings > Privacy &
Security > Screen & System Audio Recording). That is the terminal or IDE, or
for an agent session the process at the top of its process tree (for example
`~/.local/bin/fuda`). macOS applies a new grant only after that app restarts.
The driver probes the permission before booting anything and stops with a
message when it is missing.

The capture works while other windows cover the game window. A minimized
window cannot be captured.

## Output

Everything is under `<loader>/1.21.1/build/scenario-check/`:

| File | Contents |
|------|----------|
| `report.txt` | One line per result: `PASS`, `FAIL`, `SETUP FAILED`, `NOT COVERED`, or `SHOT` |
| `summary.md` | The report, then every screenshot with what to look for |
| `sheet.png` | All screenshots with their notes on one image, for attaching to a PR or task |
| `<scenario>/shots/*.png` | Full-size screenshots (1280x720 window) |
| `<scenario>/transcript.txt` | Every RCON command and reply |
| `scenario-server.log`, `scenario-client.log` | Server and client logs |

The task fails when a check fails, a setup command fails, a server log line
is an unexpected WARN or ERROR, a client log line about this mod is, or a
first-pass image check flags a screenshot.

## Review flow

1. The driver checks every screenshot automatically. A single-color frame (a
   loading screen or a failed capture) or more than 0.5% missing-texture
   magenta fails the run.
2. An agent reads `summary.md` and looks at each screenshot against its note.
   It sends back obvious problems (a missing structure, a wrong block shape,
   broken textures) before asking anyone.
3. The agent attaches `sheet.png` to the PR or the task, and the owner
   answers OK or NG for each shot.

## Writing a scenario

A scenario is a JSON file in `scripts/scenario_check/scenarios/` with a
`description` and a list of `steps`. The full step reference is in the
docstring of `scripts/scenario_check/scenario.py`. In short:

```json
{
  "description": "What this checks",
  "steps": [
    "execute in minecraft:overworld run setblock 0 201 10 chronodawn:polished_dawnstone",
    {"command": "execute in minecraft:overworld if block 0 201 10 chronodawn:polished_dawnstone",
     "match": "Test passed", "check": "placed"},
    {"command": "execute in chronodawn:chronodawn run locate structure chronodawn:forgotten_library",
     "match": "\\[(?P<x>-?\\d+), [^,]+, (?P<z>-?\\d+)\\]", "check": "locate", "max_seconds": 60},
    "execute in chronodawn:chronodawn run spreadplayers ${x} ${z} 0 1 false ${player}",
    {"shot": "front", "wait": 8, "note": "What the image must show"}
  ]
}
```

- A plain string is a setup command. A reply that looks like an error stops
  the scenario as `SETUP FAILED`.
- `match` searches the reply. Named groups become `${name}` variables for
  later steps. `check` reports the match as `PASS` or `FAIL`, with the time
  the command took. `max_seconds` fails a slow command. `await: true` re-runs
  the command once a second until it matches or `timeout` (default 120 s)
  passes, for example to wait for `execute if loaded`.
- `shot` waits `wait` seconds (default 5) for chunks to render, then captures
  the window. Write the `note` as the thing a reviewer must see, because the
  reviewer only has the image and the note.

Before every scenario the driver turns off command feedback, the daylight and
weather cycles, and advancement announcements, sets clear weather at noon,
switches the test player (`Dev`) to spectator, and moves them to the
overworld at `0 120 0`. The client hides chat. Spectator mode keeps the
hotbar and hearts out of the shots, and the player does not fall or suffocate.

Coordinates near `0 200 0` in the overworld are above the terrain, so blocks
placed there have a plain sky behind them. Use `forceload` and an `await` on
`execute if loaded` before placing blocks in chunks nobody has loaded yet.

## Limits

- Minecraft 1.21.1 only, like the upgrade test.
- macOS only (window capture).
- The crosshair stays in the shots. F1 cannot be pressed from RCON.
- `wait` is a fixed time. Far or slow chunks may need a longer wait.
- Toasts (advancements, recipes) can appear in the top-right corner.
