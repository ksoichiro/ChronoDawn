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
| Unstable-portal sweep (`BlockEventHandler`, `SERVER_POST`) | 0.42 to 0.49 / 0.67 | Fix. It is the top item. The sweep is already throttled on the branch of a separate task, so this change leaves it |
| Player dimension tracker (`PlayerEventHandler`) | about 0.3 | Keep. It is a one-time burst on arrival (`TimeKeeperVillagePlacer.searchInRange` and its chunk loads), not a per-tick cost |
| Time distortion entity sweep (`EntityEventHandler`) | 0.04 (100 entities) | Keep. The audit ranked it MEDIUM from loop bounds, but it is a tenth of the portal sweep even with 100 entities |
| `TimeDistortionEventHandler` | 0.01 | Keep |
| `TimeGuardianSpawner` structure check | 0.01 (NeoForge), 0.11 to 0.13 (Fabric) | Keep. It runs every 100 ticks, so the Fabric number is a 11 to 13 ms spike once per 5 s. The two loaders disagree, so it looks like cold structure caches after arrival and not a steady cost. Re-measure after a longer run before changing it |
| Boss room placers, `ClockworkColossusSpawner`, `TemporalPhantomSpawner`, `ChronoShieldTickHandler`, Time Tyrant armor | each under 0.02 | Keep |

With no player in Chrono Dawn (first row) the whole mod costs 0.05 to 0.08 ms/tick, which is
the cost every modpack pays whether or not anyone visits the dimension.

## Target

The first target is a proposal from these numbers, to be revised with the owner:

- **Steady cost with one player in Chrono Dawn: at most 0.5 ms/tick of mod code**, excluding the
  vanilla mob logic that the mod's mobs call. That is 1% of the 50 ms budget.
- **No periodic handler spike above 10 ms.**
- **0 players in Chrono Dawn: at most 0.1 ms/tick.** Met now.

Only the portal sweep misses the steady target today. The Fabric `TimeGuardianSpawner` spike is the only
candidate for the spike target, and it is not confirmed.

## Decision

No gameplay code changes in this task. The measurement shows one real cost, which a separate
task already throttles. The other audit items are below the noise of the measurement, and thinning
them would add a risk of delayed reactions (time distortion, spawning) for no measurable gain.
Run the procedure again after the portal sweep change to check that the steady cost falls under
0.5 ms/tick, and when a new per-tick handler is added.
