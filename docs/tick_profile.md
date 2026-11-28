# Server Tick Profile

How to measure what Chrono Dawn costs on the server thread, and the result of the first
measurement (2026-11-28, Minecraft 1.21.1, NeoForge and Fabric, commit `a1b21fea4`).
The static audit that chose the suspects is `performance-audit-2026-11.md` (on the
`daring-tiger` branch until it is merged).

## Method

There is no code in the mod for this. `scripts/tick_profile/TickProfileAgent.java` is a Java agent that
samples the stack of the thread named `Server thread` every few milliseconds and counts the samples
that have a `com.chronodawn.` frame. A server that keeps 20 TPS spends 50 ms of wall time per tick,
so `share of samples x 50 ms` is the mod's cost in ms per tick.

I compared this with spark. spark needs a mod or a Java agent too, but it is a separate download
and its output is a web viewer. The agent here is 150 lines, gives one text file, loads with the
scenario check unchanged, and groups by this mod's methods directly, so I used it. spark remains the
better tool for a modpack with other mods, since it also shows their share.

What the agent reports (`tick-profile-<pid>.txt`, rewritten every 30 s and at exit):

- `ENTRY`: the outermost mod frame of each sample, which is the handler or entity the server called.
- `INCLUSIVE`: every mod method on the stack.
- `LEAF`: the innermost mod frame with its line. **Vanilla code that the mod line calls is counted
  here.** A `super.tick()` or `getBlockState` line shows the cost of vanilla, not of the mod.
- `idle` / `busy`: samples inside `MinecraftServer.waitUntilNextTick`. It works on NeoForge only.
  Fabric 1.21.1 has obfuscated names, so `idle` stays 0 there and only the mod share is valid.

Limits: sampling sees only safepoints, and a single real client is the only player. Several players
were not measured. The per-player loops are O(players) over small bounds, so they are read from the
code, not measured.

## Procedure

Run it outside the sandbox with the repository on a volume that has at least 20 GB free
(`scenarioCheck` refuses to start otherwise).

```bash
W=$PWD
mkdir -p build/tick-profile/cls build/tick-profile/run
javac -d build/tick-profile/cls scripts/tick_profile/TickProfileAgent.java
printf 'Premain-Class: TickProfileAgent\n' > build/tick-profile/MANIFEST.MF
jar --create --file build/tick-profile/tick-profile-agent.jar \
    --manifest build/tick-profile/MANIFEST.MF -C build/tick-profile/cls .
SCENARIO_CHECK_SCENARIOS_DIR=$W/scripts/tick_profile/scenarios \
JAVA_TOOL_OPTIONS="-javaagent:$W/build/tick-profile/tick-profile-agent.jar=$W/build/tick-profile/run" \
./gradlew :neoforge:scenarioCheck -Ptarget_mc_version=1.21.1 \
    -Pscenario_check_scenarios=tick-profile-chronodawn
```

- `SCENARIO_CHECK_SCENARIOS_DIR` points the driver at `scripts/tick_profile/scenarios/`, so these
  120 second scenarios do not run in every default `scenarioCheck`.
- `JAVA_TOOL_OPTIONS` reaches the server JVM. The Gradle and client JVMs load the agent too, but they have no
  `Server thread` and write no report. Keep any `JAVA_TOOL_OPTIONS` you already use in the value.
- Use a different output directory for each run, since the files are named by PID.
- In a zsh script, write `:${1}:scenarioCheck`, not `:$1:scenarioCheck` (`:s` is a modifier).
- Scenarios: `tick-profile-overworld` (player in the Overworld, nobody in Chrono Dawn),
  `tick-profile-chronodawn` (one player in Chrono Dawn), `tick-profile-crowded` (the same with 100
  persistent NoAI cows around the player).

## Result (1.21.1, one player)

| Scenario | NeoForge mod ms/tick | Fabric mod ms/tick |
|----------|---------------------:|-------------------:|
| Overworld only (0 players in Chrono Dawn) | 0.08 | 0.05 |
| Chrono Dawn | 1.76 | 2.01 |
| Chrono Dawn + 100 entities | 1.75 | 2.05 |

NeoForge busy time was 5.7 to 6.7 ms of the 50 ms tick, so the server had plenty of room. About
1 ms of the Chrono Dawn figure is `super.tick()` and `super.aiStep()` of vanilla mob logic that the
mod's mob classes call (Floq, Ticking Sheep, Moment Creeper, Secondhand Archer, Paradox Crawler). It is
the cost of having mobs, not code that can be thinned. The rest, by handler (NeoForge / Fabric, ms/tick):

| Handler | Cost | Decision |
|---------|-----:|----------|
| Unstable-portal sweep (`BlockEventHandler`, `SERVER_POST`) | 0.42 to 0.49 / 0.67 | Fix. It is the top item. A separate change throttles it (see "After the portal sweep throttle"), so this change leaves it |
| Player dimension tracker (`PlayerEventHandler`) | about 0.3 | Keep. It is a one-time burst on arrival (`TimeKeeperVillagePlacer.searchInRange` and its chunk loads), not a per-tick cost |
| Time distortion entity sweep (`EntityEventHandler`) | 0.04 (100 entities) | Keep. The audit ranked it MEDIUM from loop bounds, but it is a tenth of the portal sweep even with 100 entities |
| `TimeDistortionEventHandler` | 0.01 | Keep |
| `TimeGuardianSpawner` structure check | 0.01 (NeoForge), 0.11 to 0.13 (Fabric) | Keep. It runs every 100 ticks, so the Fabric number is a 11 to 13 ms spike once per 5 s. The two loaders disagree, so it looks like cold structure caches after arrival and not a steady cost. Re-measure after a longer run before changing it |
| Boss room placers, `ClockworkColossusSpawner`, `TemporalPhantomSpawner`, `ChronoShieldTickHandler`, Time Tyrant armor | each under 0.02 | Keep |

With no player in Chrono Dawn (first row) the whole mod costs 0.05 to 0.08 ms/tick, which is
the cost every modpack pays whether or not anyone visits the dimension.

## After the portal sweep throttle

The throttle and unloaded-chunk skip for the unstable-portal cleanup was merged to `main` as a separate
change. The procedure above was run again on `main` with it (commit `830a89a23` plus this change),
one player in Chrono Dawn, 1.21.1:

| Scenario | Loader | Mod ms/tick before | Mod ms/tick after | Portal sweep before | Portal sweep after |
|----------|--------|-------------------:|------------------:|--------------------:|-------------------:|
| Chrono Dawn | NeoForge | 1.76 | 0.79 | 0.42 | 0.008 |
| Chrono Dawn | Fabric | 2.01 | 0.66 | 0.67 | 0.013 |
| Chrono Dawn + 100 entities | NeoForge | 1.75 | 0.63 | 0.49 | 0.012 |

The sweep fell to about 2% of its old cost, and it was the whole difference. What is left is the
one-time arrival burst (`PlayerEventHandler` through `onPlayerChangeDimension`, 0.22 to 0.29 ms/tick
averaged over the 120 s window, about 0.6 s of main-thread time once when the player first enters the
dimension) and vanilla mob logic (about 1 ms before, less here because the window is longer). With
both removed the steady mod cost is well under the 0.5 ms/tick target. The arrival burst is a hitch
rather than a steady cost, and is measured and fixed in "Arrival burst" below.

## Target

The first target is a proposal from these numbers, to be revised with the owner:

- **Steady cost with one player in Chrono Dawn: at most 0.5 ms/tick of mod code**, excluding the
  vanilla mob logic that the mod's mobs call. That is 1% of the 50 ms budget.
- **No periodic handler spike above 10 ms.**
- **0 players in Chrono Dawn: at most 0.1 ms/tick.** Met now.

Only the portal sweep missed the steady target in the first measurement, and it meets it after the throttle. The Fabric `TimeGuardianSpawner` spike is the only
candidate for the spike target, and it is not confirmed.

## Decision

No gameplay code changes in this task. The measurement shows one real cost, which a separate
task already throttles. The other audit items are below the noise of the measurement, and thinning
them would add a risk of delayed reactions (time distortion, spawning) for no measurable gain.
The re-measurement above confirms the steady cost is under 0.5 ms/tick. Run the procedure again when a
new per-tick handler is added.

## Arrival burst (Time Keeper Village placement)

The stack sampling above could not give the length of the burst, so the time was logged directly
(1.21.1 NeoForge, `time-keeper-village` scenario on a new world, one player entering Chrono Dawn
for the first time, temporary `System.nanoTime` logs that are not in the code):

| Step | Before | After |
|------|-------:|------:|
| `findSuitablePosition` (chunk loading and generation for the candidates) | 1963 ms | spread over 3 ticks, under 2 ms each |
| `placeVillage` | 16 ms | 16 ms, in the last tick (41.6 ms including the search) |
| Longest single tick of the whole village step | about 1983 ms | 41.6 ms |
| `extinguishPortal` (only while portals are unstable) | 76 ms | unchanged |

So the stall was one tick of about two seconds, 40 ticks long, and the cause was
`level.getChunk` generating terrain for the candidates on the server thread. The village search
now runs on the following ticks (`TimeKeeperVillagePlacer.tickPendingPlacement`). It only takes
candidates whose chunks are all loaded (`getChunkNow`), so nothing is generated on the main thread,
and it falls back to the old synchronous search only after every distance range has had 60 ticks
without a loaded candidate. The village appears about 4 ticks after arrival instead of in the
arrival tick.

`extinguishPortal` took 76 ms because the scenario teleports with `spreadplayers`, so no portal block
exists nearby and the 101x101x101 fallback scan runs. A real portal arrival finds the block in the
31x31x31 scan, so it was left alone. Measure it with a real portal arrival before changing it.

Only 1.21.1 NeoForge was measured. The change is the same text in every version, and the
scenario check passes there. The Fabric `TimeGuardianSpawner` spike stays open and belongs to the
follow-up that dedupes its chunk scan.
