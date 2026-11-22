---
name: scenario-check
description: Use when adding or editing a scenarioCheck scenario (scripts/scenario_check/scenarios/*.json) or its driver, or when a scenario check run is slow. Rules for waiting on conditions instead of fixed sleeps, and for checking how long a scenario takes.
---

# Scenario check: keep scenarios fast

`docs/scenario_check.md` is the reference for running scenarios and for every
step type. This skill has the rules that keep a scenario from sitting idle.
A whole run boots a real server and client, so every fixed second in a
scenario costs that second on every loader and version it runs on.

## Rules

1. Wait for a condition, not for a time. Pick the step that observes what
   the next step needs:

   | The next step needs | Use |
   |---------------------|-----|
   | Chunks around the player loaded and drawn in the client (a shot, an `input` that aims at a block) | `{"settle": 120}` after the teleport. A shot settles by itself too |
   | A server chunk loaded before `setblock` or `fill` | `forceload add` and an `await` on `execute if loaded` |
   | A block or entity the server places later (a structure placer, a spawn) | `await` on `execute if block` or `execute if entity` at a known offset |
   | A screen opened or closed | `screen` |
   | Repeated clicks until something changes (bone meal) | `input` with `until`, `match`, and `repeat` |

2. A fixed wait (`sleep`, or `wait` on a shot) needs a `reason` that says why
   no condition above can replace it. The loader rejects one without it, and
   `testScenarioCheckScripts` fails when a bundled scenario has more than
   10 seconds of them in total.
3. Do not add a wait "to be safe" after a command whose effect is
   synchronous. A teleport, `setblock`, `fill`, `summon`, `gamerule`, and the
   Chrono Dawn entry event (which places the Time Keeper Village) are done
   when the RCON reply comes back. The client is not: it applies a
   teleport a moment later, so an `input` right after a `tp` can click from
   the old spot. Put a `settle` between them. In `sapling-root-dirt` such a
   click bone-mealed the grass next to a sapling, and the plants that grew
   there took every later click.
4. In the Chrono Dawn dimension run `settle`, then `time set noon`, then the
   shot. Time keeps flowing there, so the long wait has to come before
   `time set noon`. The shot settles again by itself, but that takes about
   2 seconds once the explicit settle is done. Write it as
   `execute in chronodawn:chronodawn run time set noon`: on 26.3 each
   dimension has its own clock, and a bare `time set noon` from RCON only
   moves the overworld one.
5. Sprint ticks only for server work that counts ticks and that no command
   can observe. The Phantom Catacombs boss room placer checks every 600
   ticks, runs entirely on the server thread, and puts the boss room at a
   random spot, so `phantom-catacombs` runs `tick sprint 1500` (the 75 s it
   used to sleep) and then awaits `tick query` matching
   `running normally|can't keep up`. Prefer an `await` on a placed block when
   its position is fixed (the Master Clock boss room door). Do not sprint to
   wait for chunk generation or client rendering, which do not run on ticks,
   and do not sprint between `time set noon` and a shot.
6. Check how long the scenario takes. After a run, read the `Time` table in
   `<loader>/<version>/build/scenario-check/summary.md` and the
   `# step N (kind): Xs` lines in `<scenario>/transcript.txt`. A step that
   takes much longer than what it waits for points at a missing condition.
   Name the slow steps and their reason in the PR when they remain.

## What settle checks

The client agent answers `render` with
`chunks=<n> missing=<n> sections=<n> queue=<empty|busy>`. The driver treats
the world as settled when `missing` (chunks the client lacks within 3 chunks
of the player) is 0, `queue` is `empty`, and `chunks` and `sections` stay the
same for 2 seconds. It does not know what the server still has to generate
outside that radius, or what a placer adds later, so those need an `await`.

The test server runs with `view-distance=4`. A shot that needs terrain more
than about 4 chunks away does not get it, whatever the wait.
