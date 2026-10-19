# Vegetation Distinctiveness Design

Date: 2026-10-12

## Goal

Chrono Dawn's trees and flowers differ from vanilla mostly by color. Tree
shapes use vanilla trunk/foliage placers configured in JSON, and Time Blossom
has no behavior of its own. This design adds behavior and ambient effects that
make the vegetation feel tied to the dimension's time theme, without adding
new wood sets or new materials.

Success criteria:

- A player can see at a glance that Chrono Dawn leaves and flowers behave
  differently from vanilla.
- No new server-side load beyond what vanilla crops and grass already cause.
- Existing worlds and player builds keep their current appearance. Nothing
  already placed changes its look.

## Scope

In scope:

1. Falling leaf particles for every Chrono Dawn leaf type.
2. Rising leaf particles for Ancient Time Wood.
3. Night-only glowing petal particles for Dark Time Wood.
4. Chrono Bloom, a new flower that opens and closes with the time of day, plus
   a potted variant that does the same.
5. A Chrono Bloom meadow in `chronodawn_prairies`.

Out of scope:

- New tree species or wood sets.
- Drops or materials from the night petals. Revisit after the visual effect
  ships.
- Changes to Time Blossom, Dawn Bell, or Dusk Bell.
- Wind or swaying vegetation. Vanilla has no wind system.

## Decisions and Rationale

| Decision | Rationale |
|---|---|
| Night effect on Dark Time Wood is particles only, not a block-state change | Toggling leaf states would cause chunk re-meshes on every leaf, and light updates if the leaves emitted light. Random ticks also only run inside simulation distance, so distant forests would show stale states. Client-side particles have no server cost and never go stale. Vanilla's Firefly Bush uses the same approach. |
| Night effect goes on an existing tree, not a new species | A new species needs a full wood set across 14 versions. Dark Time Wood currently differs from other trees only by height, so it benefits most. |
| Base leaf particles fall downward; only Ancient Time Wood rises | If every tree had rising leaves, rising would become the dimension's normal and lose its effect. Falling leaves also fix a regression in 1.21.5+, where vanilla trees drop leaves and Chrono Dawn trees drop nothing. |
| Chrono Bloom is a new flower, not a reworked Time Blossom | Reworking Time Blossom would change the look of flowers players already placed. 1.21.1 modpack users need safe upgrades. |
| Chrono Bloom gets its own meadow in prairies | A single flower opening is easy to miss. A field of flowers opening and closing together turns the change into scenery. Prairies currently has no flower feature, so Chrono Bloom becomes its first flower feature and grass bonemeal grows it without displacing Time Blossom. |

## Chrono Bloom

### Blocks

- `chronodawn:chrono_bloom`: a one-block flower.
- `chronodawn:potted_chrono_bloom`: a dedicated flower pot block. Vanilla
  `FlowerPotBlock` has no block-state properties for its contents, so the
  potted variant needs its own class to carry the stage.

Both blocks have an integer property `stage` with values 0 to 3.

| Stage | Meaning |
|---|---|
| 0 | Closed (bud) |
| 1 | Opening |
| 2 | Fully open |
| 3 | Closing |

Neither block emits light.

### Time-to-Stage Mapping

The day is 24000 ticks. The stage depends on `dayTime % 24000`.

| Time of day (ticks) | Stage |
|---|---|
| 23000 to 999 (dawn, wraps around midnight) | 1 Opening |
| 1000 to 10999 (day) | 2 Fully open |
| 11000 to 12999 (dusk) | 3 Closing |
| 13000 to 22999 (night) | 0 Closed |

The mapping lives in one pure function, `ChronoBloomStage.stageFor(long
dayTime)`, in `common/shared`. The flower block, the potted block, and the
Dark Time Wood night check all call it. The function takes a raw `long` and
handles negative values and values above 24000.

### Reading the Time

- 1.20.1 to 1.21.11: `level.getDayTime()`. `LevelGetTimeMixin` already
  returns Chrono Dawn's independent time inside the dimension, and vanilla
  time elsewhere.
- 26.1.2, 26.2, 26.3: `Level.getDayTime()` no longer exists. Read the time
  from the level's clock manager, as `TimeDistortionEventHandler` does on the
  server (`level.clockManager().getInstance(clockHolder).totalTicks()`).
  The particle code runs on the client. Whether `ClientLevel` exposes the same
  clock data must be confirmed during implementation. If it does not, use the
  client-side value that drives the sky brightness.

The time lookup goes behind a small per-version helper so the shared block
code does not branch on version.

### Updating

- The block ticks randomly. On each random tick it computes the stage for the
  current time and, if it differs, sets the new state with update flag 2
  (notify clients only, no neighbor updates).
- `getStateForPlacement` returns the correct stage, so a newly placed flower
  never shows a stale stage.
- Worldgen places the flowers with stage 0. The first random tick corrects
  them. Within simulation distance this happens within a minute or two.
- The shortest stage lasts 2000 ticks (100 seconds). Random ticks average
  about 68 seconds per block at the default `randomTickSpeed`, so the lag is
  at most about one stage.
- In other dimensions the flower follows that dimension's time.

### Bonemeal

- Bonemeal on a Chrono Bloom drops one `chrono_bloom` item, like vanilla tall
  flowers. It does not grow a new block.
- Bonemeal on Temporal Grass in prairies can grow Chrono Bloom through the
  existing `TemporalGrassBlock.performBonemeal` path, which uses the biome's
  first flower feature. No change to that code is needed.

### Supporting Data

Follow the existing Time Blossom files for each item below:

- Loot tables for both blocks. The potted block drops the flower and the pot.
- Translations in `en_us` and `ja_jp` for every version-specific lang file.
- Client Items JSON for 1.21.4 and later.
- Blockstate JSON with one model per stage, for both blocks.
- Composting chance in `ModComposting`, at 0.65 like Time Blossom.
- Flower tags that Time Blossom belongs to.
- A recipe for yellow dye from Chrono Bloom, plus its recipe-unlock
  advancement, following `purple_dye_from_purple_time_blossom`.
- Creative tab entry next to Time Blossom.

## Worldgen

Add `chronodawn:patch_chrono_bloom` (configured and placed features) to
`chronodawn_prairies` in every version's biome JSON. It uses the
`minecraft:flower` feature type so that `getFlowerFeatures()` picks it up for
bonemeal. Density is a placement-tuning value. Start near vanilla flower
forest density and adjust after visual checks.

Biome feature ordering must stay consistent across biomes. Prairies is the
only biome that gets this feature, so it does not create an ordering cycle.

The feature goes directly into the `chronodawn_prairies` biome JSON. No
loader-level biome modifier (NeoForge, Fabric `BiomeModifications`, or Forge
1.20.1 `forge:add_features`) is involved, because those only apply to
injection into biomes that Chrono Dawn does not own.

`chronodawn_prairies.json` exists in eight places, and each needs the
feature:

- `common/1.20.1`, `common/1.21.1`, `common/1.21.2`
- `common/shared-1.21.2+` (used by 1.21.4 to 1.21.10)
- `common/1.21.11`, `common/26.1.2`, `common/26.2`, `common/26.3`

26.3 stores configured features under `worldgen/feature/` instead of
`worldgen/configured_feature/`. Follow the existing `patch_flowers` files in
each module for the directory and JSON format.

## Leaf Particles

### Particle Types

Four new particle types registered in `ModParticles`, using the same
registration pattern as the Time Torch flame particles.

| Particle | Spawned by | Motion |
|---|---|---|
| `time_wood_leaf` | Time Wood leaves, including Fruit of Time trees, which use Time Wood leaves | Spawns below the leaf block, falls slowly with side-to-side drift, like vanilla falling leaves |
| `ancient_time_wood_leaf` | Ancient Time Wood leaves | Spawns above the leaf block, drifts slowly upward, fades out |
| `dark_time_wood_leaf` | Dark Time Wood leaves, day and night | Same as `time_wood_leaf` |
| `night_blossom_petal` | Dark Time Wood leaves, night only (stage 0) | Spawns below the leaf block, falls more slowly than leaves, rendered at full brightness so it glows in the dark |

### Spawn Conditions

- Spawn chance per animate tick is 0.01, matching vanilla and the existing
  `FALLING_LEAVES_PARTICLE_CHANCE` constant.
- Falling particles spawn only when the block below is not a full solid face.
- Rising particles spawn only when the block above is not a full solid face.
- 1.21.5 and later: implement the existing empty
  `spawnFallingLeavesParticle` override in each leaf class.
- 1.20.1 to 1.21.4: these versions have no such hook. Override
  `animateTick`, call `super`, then apply the same chance and conditions.
- The night petal check runs inside the same client-side tick and uses
  `ChronoBloomStage.stageFor(time) == 0`.

All particle work is client-side rendering. It sends no packets and changes
no block states.

### Textures

- The three leaf particles are derived from each leaf texture's colors,
  following the `texture-creation` skill.
- The night petal is a new pale texture, pale blue or pale violet, chosen to
  contrast with Dark Time Wood's dark foliage.

## Version Coverage

All supported versions: 1.20.1 (Fabric, Forge), 1.21.1 to 1.21.11 (Fabric,
NeoForge), 26.1.2, 26.2, 26.3 (Fabric, NeoForge). 1.21.3 shares 1.21.2
modules.

Known era splits to account for:

- Leaf particle hook: `spawnFallingLeavesParticle` from 1.21.5, `animateTick`
  before that.
- Time reading: `getDayTime()` up to 1.21.11, clock manager in 26.x.
- Block registration factories, `setId`, and `BushBlock` changes follow the
  existing patterns for Time Blossom in each version module.
- Data pack directory names, recipe JSON, and advancement JSON formats follow
  the existing per-era files for Time Blossom.

## Testing

Automated:

- Unit tests for `ChronoBloomStage.stageFor` at the boundaries 0, 999, 1000,
  10999, 11000, 12999, 13000, 22999, 23000, 23999, plus a negative value and a
  value above 24000.
- `validateResources`, `validateData`, `validateTranslations`.
- `buildAll` and `testAll`.
- `prodSmokeTest` is not required, since no Mixin configuration changes.

Manual, by running the client:

- 1.20.1 (Fabric), 1.21.1 (NeoForge and Fabric), 26.3 (Fabric).
- Check each leaf particle direction and frequency.
- Check that night petals appear only at night in the Dark Forest and glow.
- Check Chrono Bloom stage changes in the Chrono Dawn dimension and the
  overworld, both planted and potted. Confirm during implementation whether
  `/time set` changes Chrono Dawn's independent time. If it does not, use the
  mod's own time control or wait for the cycle.
- Check bonemeal on Chrono Bloom and on prairies grass.
- Check that existing Time Blossom placements look unchanged.
