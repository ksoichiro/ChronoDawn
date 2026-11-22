# Scenario Check

`scenarioCheck` turns an in-game check into a command. It boots the freshly
built JAR on a production server and client for Minecraft 1.21.1 or 26.3, runs
scenario files over RCON, judges the replies, and takes in-game screenshots
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
./gradlew :neoforge:scenarioCheck -Ptarget_mc_version=26.3
./gradlew :fabric:scenarioCheck -Ptarget_mc_version=26.3
```

The versions it runs on are `upgrade_test_mc_versions` in `gradle.properties`.
Scenarios are written once in 1.21.1 command syntax. On 26.3 the driver
renames the game rules that Minecraft 1.21.11 renamed (`doDaylightCycle` to
`advance_time` and so on, see `scripts/upgrade_test/commands.py`) before it
sends a command. Add a rule there when a scenario uses one that is not
listed yet.

- `scenario_check_scenarios`: comma-separated scenario names (file names
  without `.json`). Default: every file in `scripts/scenario_check/scenarios/`.
- `upgrade_test_candidate_jar`: test another mod JAR instead of the fresh build.

Each run starts from a new world (seed `20261023`), so runs do not affect each
other. Ports: Fabric 25631/25632, NeoForge 25641/25642. Pass
`-Pscenario_check_ports=<server>,<rcon>` to use others, for example when
another checkout is running a scenario check on the same loader.

### Screenshots and player input

The client takes the screenshots itself, the way F2 does. The driver compiles
`scripts/scenario_check/agent/ScenarioShotAgent.java` with the client's JDK
and loads it with `-javaagent`. The agent saves the game's framebuffer when
the driver asks for a shot, so the capture does not go through macOS screen
recording. It needs no Screen Recording permission, and it keeps working while
the screen is locked or other windows cover the game window. Both loaders
were checked on a locked Mac.

The same agent also acts as the player. It clicks the game's own use and
attack key mappings and reads the title of the open screen, so player input
needs no Accessibility permission either and works on a locked Mac.

The agent calls Minecraft by name: Mojang names on NeoForge and intermediary
names on Fabric 1.21.1. Minecraft 26.x is unobfuscated, so both loaders use
Mojang names there. 26.x moved the main render target to `GameRenderer` and
the open screen to `Gui`, and `Screenshot.grab` takes a downscale factor. The
agent tries the 1.21.1 shape first and falls back to the 26.x one.

## Output

Everything is under `<loader>/<version>/build/scenario-check/`, for example
`neoforge/26.3/build/scenario-check/`:

| File | Contents |
|------|----------|
| `report.txt` | One line per result: `PASS`, `FAIL`, `SETUP FAILED`, `NOT COVERED`, or `SHOT` |
| `summary.md` | The report, then every screenshot with what to look for |
| `sheet.png` | All screenshots with their notes on one image, for attaching to a PR or task |
| `<scenario>/shots/*.png` | Full-size screenshots (the framebuffer of the 1280x720 window, 2560x1440 on a Retina display) |
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
3. The agent attaches `sheet.png` to the PR or the task and keeps working
   through the PR. It does not wait for the owner.
4. The owner answers OK or NG for each shot when they review the PR. Checks
   a scenario cannot cover (typing or clicking inside a GUI screen, held or
   repeated input such as mining in survival, other Minecraft versions) are
   listed in the PR as manual checks.

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

### Player input and screens

Two step types act in the client instead of over RCON:

- `{"input": "use"}` clicks the right mouse button once, `{"input": "attack"}`
  the left one. The game acts on whatever the crosshair points at in its next
  tick: it opens a trader or a container, places the held block, hits a mob,
  or breaks a block in creative. `{"input": "close"}` closes the open screen
  and tells the server, so a trader or a chest is released. It is fine to send
  it with no screen open.
- `{"screen": "regex", "check": "id", "timeout": 10}` waits until the title
  of the open screen matches. A screen with an empty title, or no screen,
  matches `^$`. Without `check` a mismatch is `SETUP FAILED`, as for a
  command. The loading screen shown after a teleport into another dimension
  also has an empty title, so `^$` does not wait for it. Add a `sleep` of a
  few seconds after such a teleport before an `input` step.

Spectators cannot use or attack, so switch the player first with
`"gamemode creative ${player}"` (or `survival`). Aim by teleporting the
player to a spot within reach (about 3 blocks for entities in survival) with
an explicit rotation, for example `tp ${player} 24.5 201 8.5 0 10` for a
target 2 blocks south. `tp ... facing entity` left the crosshair above the
target in testing. A shot just before the input shows where the crosshair
was if the input does not land. Clear the inventory with `clear ${player}`
unless the held item matters: the Chronicle given on join opens its own
screen on a right-click. `use` and `attack` fail while a screen is open, because the game
would only act on the click after the screen closes. In creative or survival
the hotbar and hearts show in the shots.

`time-keeper-trade.json` is the worked example: it summons a Time Keeper
with no AI, right-clicks it, checks that a screen titled Time Keeper opened,
takes a shot of the trading screen, and closes it.

Before every scenario the driver turns off command feedback, the daylight and
weather cycles, and advancement announcements, turns mob spawning on, sets
clear weather at noon, closes any screen a previous scenario left open,
switches the test player (`Dev`) to spectator, and moves them to the
overworld at `0 120 0`. The client hides chat. Spectator mode keeps the
hotbar and hearts out of the shots, and the player does not fall or suffocate.

The Chrono Dawn dimension keeps its own time, and it keeps flowing (sometimes
fast, under time distortion) whatever `doDaylightCycle` says. A shot there
can come out at night unless `time set noon` runs just before it, so the
Chrono Dawn scenarios wait for chunks with a `sleep`, set noon, and then take
the shot with a short `wait`.

Coordinates near `0 200 0` in the overworld are above the terrain, so blocks
placed there have a plain sky behind them. Use `forceload` and an `await` on
`execute if loaded` before placing blocks in chunks nobody has loaded yet.

### Existing scenarios

Start from the closest existing scenario before writing a new one. Pass the
names you need with `-Pscenario_check_scenarios` to check one change quickly.

| Scenario | Checks |
|----------|--------|
| `polished-stones` | Dawnstone, Duskstone, and their polished blocks, stairs, slabs, and walls |
| `sapling-root-dirt` | Bone meal grows the three saplings; the block under the trunk in Chrono Dawn and in the Overworld |
| `wolf-spawn-forest`, `wolf-spawn-dark-forest` | Wolves generate in the biome. The forest one also checks that Temporal Grass is in `minecraft:wolves_spawnable_on` |
| `time-keeper-trade` | Right-clicking a Time Keeper opens its trading screen |
| `time-keeper-village` | The village placed near the first entry into Chrono Dawn |
| `plains-surface`, `mountain-surface` | The surface of the Plains and Mountain biomes |
| `forgotten-library` | `/locate` speed for the Forgotten Library, and a shot of it from above |
| `desert-clock-tower`, `master-clock`, `phantom-catacombs`, `entropy-crypt`, `sentinel-outpost` | `/locate` speed for the structure, and shots of it from above and from the side |

The Time Keeper Village is placed near wherever a player first enters Chrono
Dawn in a world, so every scenario that goes there enters at `0 0` first.
Otherwise the scenario that runs first decides where the village goes, and it
can land on the structure another scenario looks at. Scenarios that place
blocks use separate areas: overworld x 0-15 (`polished-stones`), 16-31
(`time-keeper-trade`), 48-63 (`wolf-spawn-forest`), and x 196-247 in both dimensions
(`sapling-root-dirt`).

## Limits

- Minecraft 1.21.1 and 26.3 only, like the upgrade test.
- Tested on macOS only.
- In spectator mode the crosshair shows only when it points at an entity or
  a container, so it is usually not in the shots.
- `wait` is a fixed time. Far or slow chunks may need a longer wait.
- Toasts (advancements, recipes) can appear in the top-right corner.
- Input is one click at a time. Holding a button (mining in survival,
  drawing a bow), moving, typing, and clicking slots inside a screen are not
  supported.
