# Runtime Performance Re-Audit (2026-11)

**Date**: 2026-11-28
**Base commit**: `a1b21fea4` (main)
**Procedure**: `.claude/skills/audit-performance-thread-safety/SKILL.md`
**Previous audit**: commit `081b8fd19` (2026-01-02, thread safety and dimension filters only)
**Scope**: Static code reading only. Nothing was profiled and no code was changed.
All numbers below are estimates from loop bounds, not measurements.

## Method

The audited sources are `common/26.3` and `common/shared`. The 1.21.1 module registers
the same tick handlers (`EntityEventHandler`, `BlockEventHandler` and the four spawning
classes, checked with `grep`), so findings 1 to 6 apply there too. Line numbers refer to
`common/26.3`.

Covered:

- Every `TickEvent.SERVER_POST`, `SERVER_LEVEL_POST` and `PLAYER_POST` registration
- The classes under `worldgen/spawning/` that are driven by those ticks
- The mixins that run on worldgen or every tick: `FreezeTopLayerMixin` (26.3 compiles its own
  copy under `common/26.3`), and `LevelGetTimeMixin` and `ServerLevelSetTimeMixin` (compiled for
  1.21.x only; `common/26.3/build.gradle` excludes both because 26.x keeps the time in the
  clock manager)
- Cheap handlers also seen: `TemporalPhantomSpawner` (SERVER_POST, 600-tick check) and
  `BlockEventHandler.processRestorationTimers` (SERVER_LEVEL_POST, empty-map early return)
- Static non-concurrent collections

Not covered in depth (see "Not reviewed"):

- Per-entity `aiStep`/`tick` code of mobs and bosses
- `PhantomCatacombsBossRoomPlacer.evaluateAllPlacements` and the later placement phases
- Client-side tick handlers

## Summary

| # | Item | File | Severity | Cost driver |
|---|------|------|----------|-------------|
| 1 | Unstable-portal sweep | `events/BlockEventHandler.java:105` | HIGH | 9,261 `getBlockState` per portal per tick |
| 2 | Boss room placer marker scans | `worldgen/spawning/MasterClockBossRoomPlacer.java:1654`, `PhantomCatacombsBossRoomPlacer.java:1070` | MEDIUM | ~98k `getBlockState` per tick per active structure |
| 3 | Structure detection around each player | `MasterClockBossRoomPlacer.java:1587`, `PhantomCatacombsBossRoomPlacer.java:1707`, `TimeGuardianSpawner.java:99` | MEDIUM | 289 chunk lookups per player per check |
| 4 | Time distortion entity sweep | `events/EntityEventHandler.java:126` | MEDIUM | every living entity in the dimension every 5 ticks |
| 5 | Engine room marker scan | `worldgen/spawning/ClockworkColossusSpawner.java:203` | LOW-MEDIUM | `getBlockEntity` over a whole bounding box every 200 ticks |
| 6 | Snow transition mixin | `common/26.3/.../mixin/FreezeTopLayerMixin.java` | LOW-MEDIUM | 2,304 climate samples per chunk (worldgen) |
| 7 | Time Tyrant armor echo scan | `common/shared/.../items/artifacts/TimeTyrantArmorAbilityHandler.java:163` | LOW | entity query every 5 ticks while sprinting |
| 8 | Per-tick saved data write (1.21.x only) | `ServerLevelSetTimeMixin` and `TimeDistortionEventHandler` | LOW | `setDirty()` every tick |
| 9 | Player dimension tracker | `common/shared/.../events/PlayerEventHandler.java:74` | LOW | map write per player per tick, never removed |
| 10 | Chrono Aegis clarity | `events/EntityEventHandler.java:263` | LOW | holder wrap per player per tick |

Threading: all tick-driven shared state uses `ConcurrentHashMap` or atomics (fixed in
`081b8fd19`). The remaining plain `HashMap` statics (`ChronoDawnPortalBlock`,
`PortalTeleportHandler.LAST_ARRIVAL_DIMENSION`, `BossRoomProtectionProcessor.WARNED_PLACEMENT_IDS`)
are only touched from the server thread, which `BossRoomProtectionProcessor` documents.
No new thread-safety defect was found.

Dimension isolation: every spawning handler and `TimeDistortionEventHandler` filters on
`CHRONO_DAWN_DIMENSION` before doing work. `ChronoShieldTickHandler` and
`PlayerEventHandler` are intentionally cross-dimension and are cheap.

## Findings

### 1. Unstable-portal sweep scans a 21x21x21 cube every tick (HIGH)

**File**: `events/BlockEventHandler.java` lines 105-146

**Pattern**: Repeated full-area scan on `SERVER_POST`, with no interval.

While `enforcesInstabilityGate(arePortalsUnstable())` is true, every tick walks every
registered portal whose source dimension is Chrono Dawn and calls
`chronodawnLevel.getBlockState` for 21 x 21 x 21 = 9,261 positions.

- The gate is on by default: `ConfigDefaults.PORTAL_DEFAULTS = new PortalSettings(true, false)`.
  It stays on from the first entry into Chrono Dawn until a Portal Stabilizer is used
  (`ChronoDawnGlobalState.arePortalsUnstable()`), which can be most of a playthrough.
- `PlayerEventHandler` (line 203) and `PortalTeleportHandler` (line 744) register a new
  portal state for each arrival or ignition, and `PortalRegistry` never prunes them, so the
  cost grows with the number of portals.
- The scan does not check whether the chunk is loaded. `getBlockState` on an unloaded
  chunk can force a load, so portals far from every player may keep chunks alive.
  This needs confirmation in game or with a profiler.

**Estimate**: about 9k lookups per tick per portal. With 5 portals that is about 46k per
tick, in the same range as other known tick hogs. It runs on the main thread with no
player nearby.

**Fix direction**: Run the sweep on an interval (for example every 20 ticks), skip portals
whose chunk is not loaded (`level.hasChunkAt`), or remove the blocks when the portal is
ignited instead of polling.

### 2. Boss room placers scan whole chunk columns on the main thread (MEDIUM)

**Files**:
- `MasterClockBossRoomPlacer.progressSearchSurfaceMarker` (lines 1654-1690)
- `PhantomCatacombsBossRoomPlacer.getStructureBoundingBox` fallback (lines 1070-1100)

**Pattern**: Large block scan, one chunk per tick (the T428 state machine).

- Master Clock: for each active structure, one tick scans 16 x 16 x (maxY - minY) blocks.
  In Chrono Dawn that is about 16 x 16 x 384 = 98k `getBlockState` calls, repeated for up to
  49 chunks and up to 4 passes (`searchRetryCount > 3`) when no Dropper marker exists.
  The earlier audit's chunk-per-tick spreading keeps single spikes small, but each tick is
  still a large fixed cost, and several structures near players run in parallel because
  `progressAllProcessing` advances all active states every tick.
- The scan uses `level.getBlockState(pos)` with no loaded check, so it can force a chunk
  load or generation for the outer chunks of the 7 x 7 area.
- Phantom Catacombs: the chunk scan fallback runs only when `StructureStart` has no bounding
  box. It does check `level.hasChunk`, so it does not force loads. It is rare, but the same
  98k loop runs per chunk in one call, for every chunk of the structure.

**Fix direction**: Read the marker position from the structure's pieces or from
`StructureStart` instead of scanning blocks. If scanning stays, limit Y to the structure's
bounding box, and check `hasChunk` first.

### 3. Structure detection repeats 289 lookups per player (MEDIUM)

**Files**: `MasterClockBossRoomPlacer.checkAndPlaceRooms`, `PhantomCatacombsBossRoomPlacer.checkAndPlaceRooms`,
`TimeGuardianSpawner.checkAndSpawnGuardians`

**Pattern**: Batch operation on a single tick, multiplied by player count.

Each runs on an interval (600, 600 and 100 ticks) and, for every player, loops 17 x 17 = 289
chunks:

- The two placers call `processStructure`, which calls `getAllStructuresAt` and a registry
  `getKey` per structure, up to three times (`hasX`, `getStructureOrigin`, bounding box).
- `TimeGuardianSpawner` calls `startsForStructure` with a lambda that does a registry lookup
  per structure, and runs every 100 ticks, so it is the most frequent.
- Nothing dedupes chunks between players. Two players standing together double the work, and
  the loop does not stop when the structure is already processed or spawned.

**Estimate**: 289 x players lookups per check. Small per call, but all three run on the same
tick for some intervals (600 is a multiple of 100).

**Fix direction**: Build a set of chunks around all players once, drop chunks already
recorded as processed, and cache the structure registry key outside the loop. For
`TimeGuardianSpawner`, the structure position could be recorded once at generation and
looked up from `BossSpawnData` instead of polling.

### 4. Time distortion sweeps every living entity (MEDIUM)

**File**: `events/EntityEventHandler.java` lines 70-90 and 126-133

Every 5 ticks in Chrono Dawn, `processChronoDawnEntities` iterates `level.getAllEntities()`
and calls `TimeDistortionEffect.applyTimeDistortion` for each `LivingEntity`. Each call reads
`ChronoDawnConfig.get()` twice, checks the dimension and the scope, computes the band, and
calls `getEffect` plus possibly `removeEffect` and `addEffect` for Slowness and Speed
(`TimeDistortionEffect`, ambient duration 100 ticks).

- It runs even when no player is in the dimension.
- It does not restrict to entities near players, so the cost scales with all loaded
  Chrono Dawn entities, including passive animals.

**Fix direction**: Return early when `level.players().isEmpty()`, and skip the call when the
computed outcome equals the current effect (the policy class already has
`isReplaceableAmbient`). A larger change would apply the effect on mob join and band change
instead of polling.

### 5. Engine room marker scan uses `getBlockEntity` over the whole box (LOW-MEDIUM)

**File**: `worldgen/spawning/ClockworkColossusSpawner.java` lines 203-228

When a player is inside an unspawned engine room, every 200 ticks
`findEngineRoomMarkersInBoundingBox` calls `level.getBlockEntity` for every position in the
bounding box and compares sign text. The result is not cached, so a player who stays in the
room repeats it until the boss spawns. `computeIfAbsent` on saved data also runs every
tick before the interval check, which is a cheap lookup but avoidable.

**Fix direction**: Cache the marker list per room, or iterate the chunks' block entity maps.

### 6. Snow transition mixin samples biomes per chunk (LOW-MEDIUM)

**File**: `common/26.3/src/main/java/com/chronodawn/mixin/FreezeTopLayerMixin.java` (1.21.x use the copy in `common/shared`; the constants are identical)

For Chrono Dawn chunks that pass the coarse filter, `chronodawn$extendSnowTransition`
samples a 48 x 48 grid (2,304 climate samples) per feature placement. This is worldgen
cost on worker threads, paid once per new chunk, and it is bounded by the early exit.
Not a tick cost, but it adds to chunk generation time while exploring.

### 7-10. Minor items (LOW)

- **7**: `tryTemporalEcho` runs an entity query (16-block radius) every 5 ticks, only while the
  wearer of the boots is sprinting with the cooldown ready. Bounded, no change needed now.
- **8** (1.21.x only, 26.3 excludes the mixin and uses `clockManager().setTotalTicks`): `ServerLevelSetTimeMixin` writes saved data and calls `setDirty()` on every `setDayTime`,
  which `TimeDistortionEventHandler` calls every tick in Chrono Dawn. The write is a field set
  and a flag, and disk IO happens only on autosave. It could skip `setDirty` when the value
  changes by less than a threshold, but the gain is small.
- **9**: `PlayerEventHandler.playerDimensions` is updated for every player every tick and
  entries are never removed on logout (one small entry per UUID per session).
- **10**: `handleChronoAegisClarity` wraps the effect holder for every player every tick before
  its cheap `hasEffect` check. The wrap can move below the `tickCount % 40` check.

## Not reviewed

- `PhantomCatacombsBossRoomPlacer.evaluateAllPlacements` and `progressMarkerSearch`: they test
  every dead-end marker with several rotations. They are likely the largest single-tick cost
  of the placers, but they run once per structure. They need a profile, not a read.
- Boss and mob entity ticks (for example `EntropyKeeperEntity.applyDecayAura`): they run only
  while the entity exists, and the entity queries seen are bounded by an AABB.
- Time Keeper Village, Chronos Warden and Time Tyrant spawners: they have no tick handler
  (`SERVER_LEVEL_LOAD` only).
- Client-side tick handlers.
- Loader-level ticks (`ChronoDawnNeoForge.onServerTick` runs `processPendingTeleports` every tick
  and `BossRoomProtectionProcessor.registerPendingProtections` every 100 ticks).

## Recommended order

1. Finding 1 (portal sweep): smallest change, biggest recurring cost.
2. Finding 4 (entity sweep): add the player check and the unchanged-effect skip.
3. Finding 3 (structure detection): dedupe across players and cache the key.
4. Finding 2 (marker scans) and finding 5: replace block scans with structure data.
5. Findings 6-10 as opportunistic cleanup.

Measure each fix with the Spark profiler or a similar tick profile on a world with several
portals and structures before and after, because the estimates above are not measured.
