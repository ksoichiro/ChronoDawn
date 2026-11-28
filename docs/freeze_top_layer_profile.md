# FreezeTopLayerMixin chunk generation cost (2026-11-28)

How much `FreezeTopLayerMixin` adds to new-chunk generation time in Chrono Dawn, measured on
Minecraft 26.3 with NeoForge. The mixin injects into `SnowAndFreezeFeature.place` twice:

- `HEAD` (`chronodawn$skipUnneededFreezeTopLayer`): 6 x 6 coarse biome samples. With no snowy biome
  nearby it cancels `place`, which also skips the vanilla freeze work.
- `RETURN` (`chronodawn$extendSnowTransition`): a coarse boundary check (`EARLY` when there is no
  snowy/warm boundary), then 48 x 48 = 2,304 biome samples (`FULL`, or `NOBOTH` when the full
  sample finds only one kind), then the snow add, snow remove and surface blend passes.

## Method

Nothing is added to the mod. `scripts/freeze_profile/freeze-profile.patch` is a temporary patch:

- `FreezeProfile` (a new helper) reads the system property `chronodawn.freezeProfile`:
  `on` (default) records one CSV line per handler call, `off` disables both handlers (vanilla
  behavior), `headonly` runs only the `HEAD` skip.
- The mixin gets `System.nanoTime()` stamps around the boundary check, the 48 x 48 sampling and the
  passes. Only the handler bodies were timed, not the overhead of Mixin's own callback dispatch.

`scripts/freeze_profile/scenarios/freeze-profile-far.json` runs on a new world (scenario seed
`20261023`). It force-loads, one after another, and waits until the corners and the center of each
square are loaded:

1. a warm-up square at chunks 562..576 x 0..14 (not counted),
2. a square of up to 16 x 16 chunks around the nearest `chronodawn_snowy` biome (`locate` found
   `-352 1616`),
3. four 15 x 15 chunk squares far from the origin (`+-375..389`, `+-390..-376` in chunks).

Steps:

```bash
git apply scripts/freeze_profile/freeze-profile.patch
W=$PWD; mkdir -p build/freeze-profile
SCENARIO_CHECK_SCENARIOS_DIR=$W/scripts/freeze_profile/scenarios \
JAVA_TOOL_OPTIONS="-Dchronodawn.freezeProfile=on -Dchronodawn.freezeProfile.out=$W/build/freeze-profile/on-1.csv" \
./gradlew :neoforge:scenarioCheck -Ptarget_mc_version=26.3 -Pscenario_check_scenarios=freeze-profile-far
# repeat with =off and =headonly (and a new csv name), copy neoforge/26.3/build/scenario-check/freeze-profile-far/transcript.txt
python3 scripts/freeze_profile/analyze.py   # run in the directory that holds the csv and transcript files
git apply -R scripts/freeze_profile/freeze-profile.patch
```

Order: `on`, `off`, `headonly`, then the same three again (6 runs, each on a fresh world). The
scenario runs outside the sandbox. The wall time of a region is the sum of the step times from
`forceload add` to the last `if loaded` wait. The mod JAR was built from `main` at
`02c5c22a2` plus the patch. The server had 9 `Worker-Main` threads.

## Results

### How often the full sampling runs (identical in every run, same seed)

| Region | `place` calls | Skipped at `HEAD` (no snow nearby) | `EARLY` | `FULL` |
| --- | ---: | ---: | ---: | ---: |
| warm-up (no snow) | 441 | 441 (100%) | 0 | 0 |
| around the snowy biome | 441 | 244 (55%) | 103 (23%) | 94 (21%) |
| four far squares | 1,764 | 1,683 (95%) | 24 (1%) | 57 (3%) |
| all | 2,646 | 2,368 (89%) | 127 (5%) | 151 (5.7%) |

`NOBOTH` never happened. The coarse check and the full sampling agreed every time. The first `on`
run saw 2,928 calls (282 more in the warm-up area, not explained), later runs 2,646. The table
uses `on-2`.

### Cost per call (run `on-2`, elapsed time on one worker thread)

| Stage | Calls | Mean | Range |
| --- | ---: | ---: | --- |
| `HEAD` skip check | 2,646 | 0.11 to 0.33 ms (by region and run) | |
| `RETURN` `EARLY` | 127 | 0.18 ms | |
| `RETURN` `FULL` (48 x 48 sampling + passes) | 151 | 10.8 ms (median; region means 10.8 to 14.0 over both runs) | 10.3 to 51.7 ms |

Inside a `FULL` call the 48 x 48 sampling is 98% of the time (about 4.7 us per
`getNoiseBiome` call). The passes (snow add and remove, surface blend) take about 0.1 ms in total.
Summed over the run, the mixin spent about 2.2 s: 1.7 s in the `FULL` sampling, 0.5 s in
the `HEAD` check (0.10 s, 0.05 s and 0.35 s by region), 0.05 s in `EARLY`.

### Wall time to generate the five counted regions (about 1,125 chunks forced, seconds)

| Run | `on` | `off` (vanilla) | `headonly` |
| --- | ---: | ---: | ---: |
| round 1 (first run on a cold JVM and a cold build) | 17.8 | 14.8 | 11.7 |
| round 2 | 13.2 | 12.0 | 10.6 |

Per region (round 2, warm-up, snowy, ne, nw, se, sw): `on` 2.6 3.6 2.3 2.3 1.9 3.1,
`off` 2.7 3.2 2.1 2.1 1.8 2.8, `headonly` 2.5 2.6 1.7 1.9 1.9 2.5.

Reading: the run to run noise is about 2 s, so only the gaps larger than that are meaningful. The
`on` runs also pay for the recording itself (string building and a CSV flush on the worker
thread), so the `on` gaps are an upper bound.

- `headonly` is the fastest, about 1.4 to 3.1 s faster than `off`. The `HEAD` skip costs 0.1 to
  0.3 ms per call, but it saves the vanilla freeze work in 89% of the chunks.
- `on` is 2.6 s (round 2) to 6.1 s (round 1) slower than `headonly`. This is more than the
  1.7 s of summed worker time that the `RETURN` extension used (spread over 9 threads), so the
  gap is partly noise, the cold first run and the recording overhead. Read it as "visible, but
  at most this large". The snowy region shows it most (3.6 s against 2.6 s), the far squares only a little (0 to 0.6 s).
- Against vanilla (`off`), the whole mixin is +1.2 s (+10%) in round 2 and +3.0 s (+20%) in
  round 1. It is a net cost, smaller than the `RETURN` part alone, since the skip pays
  part of it back.

## Limits

- Measured only on 26.3 NeoForge, one machine, 2 runs per mode. The 1.21.x copy (`common/shared`)
  has the same constants and was not measured (the task says to measure it only when 26.3 shows a
  significant cost, which is the case, so it is a follow-up).
- `if loaded` on the corners and the center is a proxy for "the square is generated". Features of
  the edge chunks may still run after it, so a part of the cost can fall into the next region.
- A player moving through the world forces chunks one ring at a time, not 225 at once. The
  per-call costs above apply to that case too, but the wall-time share does not.
- The vanilla `SnowAndFreezeFeature.place` itself was not timed.

## Decision

There is no threshold in the task, so this is a proposal for the owner:

- Per chunk the extra cost is tiny (0.2 ms) in most chunks and about 11 ms in the 6% of chunks that
  have a snow boundary (21% around the snowy biome). That is worker-thread time, so it does not
  stall the tick, but it is the largest single mixin cost, and in boundary-heavy areas the
  summed worker time of the `FULL` sampling is about 1.7 s per 1,125 chunks (the measured wall
  gap is at most 2.6 to 6.1 s, see above).
- The cost is easy to remove without changing any output. `Biome` lookups go through
  `QuartPos.fromBlock`, so the 48 x 48 block samples hit only 12 x 12 = 144 distinct biome cells
  (each cell is hit 16 times). Sampling once per 4 blocks (and indexing by block through the cell)
  gives the same `snowySamples` and `precipitationSamples` at about 1/16 of the cost (about
  0.7 ms per `FULL` call). Calling `createUncachedResolver` once per `place` instead of twice
  would save a little more.
- Recommendation: fix it (a small change with identical output), then measure the 1.21.x copy
  with the same patch when porting. A follow-up task is filed for that.
