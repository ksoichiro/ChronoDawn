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
other. The driver reserves a free server/RCON port pair at run time and
prints it (`Using ports: ...`), so checkouts can run the same loader at once
without `-P`. Pass `-Pscenario_check_ports=<server>,<rcon>` to pin the ports.
At most `CHRONODAWN_MAX_PARALLEL_VERIFY` runs (default 2) go at once on a
machine. Further runs wait for a free slot.

### Screenshots and player input

The client takes the screenshots itself, the way F2 does. The driver compiles
`scripts/scenario_check/agent/ScenarioShotAgent.java` with the client's JDK
and loads it with `-javaagent`. The agent saves the game's framebuffer when
the driver asks for a shot, so the capture does not go through macOS screen
recording. It needs no Screen Recording permission, and it keeps working while
the screen is locked or other windows cover the game window. Both loaders
were checked on a locked Mac.

The same agent also acts as the player. It clicks the game's own use and
attack key mappings, reads the title of the open screen, and clicks, scrolls
and hovers inside it, so player input needs no Accessibility permission
either and works on a locked Mac. Screen input is supported on 1.21.1 only:
26.x changed the click methods.

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
| `summary.md` | The report, the time each scenario took, then every screenshot with what to look for |
| `sheet.png` | All screenshots with their notes on one image, for attaching to a PR or task |
| `<scenario>/shots/*.png` | Full-size screenshots (the framebuffer of the 1280x720 window, 2560x1440 on a Retina display) |
| `<scenario>/transcript.txt` | Every RCON command and reply, and the time each step took |
| `scenario-server.log`, `scenario-client.log` | Server and client logs |

On success, the disposable `server/`, `client/`, and `agent/` directories are
removed. Reports, transcripts, screenshots, and the server and client logs stay.
On failure, all output stays for diagnosis. Add `-Pkeep_test_output=true` to
keep the disposable directories after success as well.

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
   a scenario cannot cover (typing, key presses, dragging, held or
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
    {"settle": 120},
    {"shot": "front", "note": "What the image must show"}
  ]
}
```

- A plain string is a setup command. A reply that looks like an error stops
  the scenario as `SETUP FAILED`.
- `match` searches the reply. Named groups become `${name}` variables for
  later steps. `check` reports the match as `PASS` or `FAIL`, with the time
  the command took. `max_seconds` fails a slow command. `await: true` re-runs
  the command every half second until it matches or `timeout` (default 120 s)
  passes, for example to wait for `execute if loaded`.
- `{"settle": 120}` waits until the client holds the chunks around the player
  and has built them: no chunk is missing within 3 chunks of the player, no
  section builds are queued, and the chunk and visible-section counts have
  not changed for 2 seconds. The number is the timeout. A timeout is a
  `FAIL`, and the scenario goes on so the shots still show what the client
  had. Use it after a teleport, before `time set noon` and the shot.
- `shot` settles first (timeout `settle`, default 30 s, `false` to skip), then
  captures the window. Write the `note` as the thing a reviewer must see,
  because the reviewer only has the image and the note.
- `{"sleep": 2, "reason": "..."}` waits a fixed time, and so does `wait` on a
  shot. Both need a `reason`, and the unit tests fail when a bundled scenario
  adds up more than 10 seconds of them. Wait for a condition instead (`await`,
  `settle`, `screen`, or `input` with `until`).

The `scenario-check` skill (`.claude/skills/scenario-check/SKILL.md`) has the
rules for keeping a new scenario fast and what each condition is for.

### Player input and screens

These step types act in the client instead of over RCON:

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
  also has an empty title, so `^$` does not wait for it. Add a `settle` step
  after such a teleport before an `input` step.
- `{"input": "use", "until": "command", "match": "regex", "repeat": 30}`
  clicks, runs the command a quarter second later, and clicks again until
  the reply matches, at most `repeat` times. Running out of clicks is
  `SETUP FAILED`. `sapling-root-dirt` uses it to bone-meal each sapling until
  the tree grows, instead of a fixed number of clicks.

- `{"click": [dx, dy], "button": 0}` clicks inside the open screen. The
  coordinates are GUI-scaled and relative to the screen center (x right, y
  down), so they do not depend on the window size. The step fails when
  nothing took the click.
- `{"slot": 2, "button": 0, "action": "pickup"}` clicks a slot of the open
  container the way the game sends it. `"action": "quick_move"` is a
  shift-click. Use the menu's slot indexes: for a crafting table 0 is the
  result, 1-9 the grid, 10-36 the inventory and 37-45 the hotbar (`give` fills
  the hotbar first). For a trader 0-1 are the payment slots and 2 the result.
  Selecting a trade offer moves the matching items into the payment slots.
- `{"hover": [dx, dy]}` and `{"hover_slot": 37}` park the mouse over a point
  or a slot. A `shot` after it shows the tooltip.
- `{"scroll": [dx, dy], "amount": -20}` turns the mouse wheel there
  (positive scrolls up). A trader's offer list scrolls to the bottom with a
  large negative amount.

To click a list, work out the positions from the screen's code and check them
on a shot. `chronicle-wayside-features.json` explains the Chronicle layout in
its description, and a trader's offer buttons are 20 high from y -56 at x -90.
There is no step for a key press, so open a container screen by using a block
or entity, and use a crafting table to show the inventory.

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
`time-keeper-chronite-trade.json` goes on to scroll the offers, select one,
and shift-click the result, then checks the inventory with `clear <player>
<item> 0` (its reply is `Found N matching item(s)` or `No items were found`).

Before every scenario the driver turns off command feedback, the daylight and
weather cycles, and advancement announcements, turns mob spawning on, sets
clear weather at noon, closes any screen a previous scenario left open,
switches the test player (`Dev`) to spectator, and moves them to the
overworld at `0 120 0`. The client hides chat. Spectator mode keeps the
hotbar and hearts out of the shots, and the player does not fall or suffocate.

The Chrono Dawn dimension keeps its own time, and it keeps flowing (sometimes
fast, under time distortion) whatever `doDaylightCycle` says. A shot there
can come out at night unless `time set noon` runs just before it, so the
Chrono Dawn scenarios wait for chunks with `settle`, set noon, and then take
the shot. Run it as `execute in chronodawn:chronodawn run time set noon`. On
26.3 each dimension has its own clock and a bare `time set noon` from RCON
only moves the overworld one. On 1.21.1 the command sets every dimension
either way.

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
| `wolf-spawn-forest` | Wolves generate in the forest (a 240 block square around the biome edge is force-loaded, since a chunk gets a creature pack with only about a 10% chance). Also checks that Temporal Grass is in `minecraft:wolves_spawnable_on`. There is no Dark Forest scenario: its canopy leaves the ground below light level 9, so no wolf spawns there |
| `time-keeper-trade` | Right-clicking a Time Keeper opens its trading screen |
| `time-keeper-chronite-trade` | The Chronite Shard buy-back trades (Emerald, Clockstone) complete in the trading screen |
| `daylight-detector-craft` | The Chronite Daylight Detector recipe crafts at a crafting table |
| `chronicle-wayside-features` | Chronicle category and entry clicks, and the Wayside Features pages with the next page button |
| `cairn-stone-tooltip` | The Cairn Stone tooltip (name and two lore lines) on hover |
| `time-keeper-village` | The village placed near the first entry into Chrono Dawn |
| `plains-surface`, `mountain-surface` | The surface of the Plains and Mountain biomes |
| `forgotten-library` | `/locate` speed for the Forgotten Library, and a shot of it from above |
| `desert-clock-tower`, `master-clock`, `phantom-catacombs`, `entropy-crypt`, `sentinel-outpost` | `/locate` speed for the structure, and shots of it from above and from the side |

The Time Keeper Village is placed near wherever a player first enters Chrono
Dawn in a world, so every scenario that goes there enters at `0 0` first.
Otherwise the scenario that runs first decides where the village goes, and it
can land on the structure another scenario looks at. Scenarios that place
blocks use separate areas: overworld x 0-15 (`polished-stones`), 16-31
(`time-keeper-trade`, `time-keeper-chronite-trade`), 32-47 (`daylight-detector-craft`), 48-63 (`wolf-spawn-forest`), 64-79 (`cairn-stone-tooltip`), and x 196 to 247, z -4 to 47 in both dimensions
(`sapling-root-dirt`).

## Limits

- Minecraft 1.21.1 and 26.3 only, like the upgrade test.
- Tested on macOS only.
- In spectator mode the crosshair shows only when it points at an entity or
  a container, so it is usually not in the shots.
- `settle` sees what the client has loaded and built, not what the server
  has still to generate or place. A structure that a placer adds later (the
  Master Clock boss room) needs an `await` on one of its blocks. The Phantom
  Catacombs boss room lands at a random spot, so `phantom-catacombs` runs
  `tick sprint 1500` for its tick-counting placer and awaits `tick query`
  instead.
- Toasts (advancements, recipes) can appear in the top-right corner.
- Input is one click at a time. Holding a button (mining in survival,
  drawing a bow), moving, typing, key presses and dragging are not supported.
  Clicks, slot clicks, scrolling and hovering inside a screen work on 1.21.1
  only. 26.3 answers `error:` for them until its click methods are mapped.
