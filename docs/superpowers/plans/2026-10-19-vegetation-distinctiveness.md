# Vegetation Distinctiveness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Chrono Bloom (a flower that opens and closes with the time of day, plus a potted variant), a Chrono Bloom meadow in prairies, and falling, rising, and night-petal leaf particles for Chrono Dawn's trees.

**Architecture:** One pure function, `ChronoBloomStage.stageFor(long)`, maps time of day to a 0-3 stage and is shared by the flower, the pot, and the night-petal check. A per-version `CompatLevelTime.dayTime(Level)` hides the 26.x clock API. Leaf particles are client-only: a shared `LeafParticleEffects` helper spawns them, and each leaf class calls it from the hook its version offers. Every feature is built and verified on 1.21.1 first, then ported to the other 13 version modules.

**Tech Stack:** Java 17/21/25, Architectury, Fabric, NeoForge, Forge 1.20.1, JUnit 5, Minecraft data packs.

**Spec:** `docs/superpowers/specs/2026-10-12-vegetation-distinctiveness-design.md`

## Global Constraints

- Versions: 1.20.1 (Fabric, Forge), 1.21.1 to 1.21.11 (Fabric, NeoForge), 26.1.2, 26.2, 26.3 (Fabric, NeoForge). 1.21.3 shares 1.21.2 modules.
- Version modules: `common/{1.20.1,1.21.1,1.21.2,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11,26.1.2,26.2,26.3}` (14).
- Stage values: 0 closed, 1 opening, 2 fully open, 3 closing. Ranges on `dayTime mod 24000`: 23000-999 → 1, 1000-10999 → 2, 11000-12999 → 3, 13000-22999 → 0.
- Neither Chrono Bloom block emits light. Stage updates use `Block.UPDATE_CLIENTS` (flag 2).
- Leaf particle chance: 0.01 per animate tick, matching `FALLING_LEAVES_PARTICLE_CHANCE`.
- Composting chance for Chrono Bloom: 0.65 (`MEDIUM`).
- Dye: Chrono Bloom → 1 yellow dye.
- New textures are original pixel art. Never recolor a vanilla texture. Mark them as drafts for the user to review.
- Docs and code comments in English. Mojang names. Groovy build files.
- Commits are GPG-signed. If `git commit` fails with a GPG sandbox error, rerun it with the sandbox disabled. Never disable signing.

## Review Focus

1. **Time values outside 0-23999** (negative after `/time set` arithmetic, or totals far above one day): `stageFor` must still return the stage for `dayTime mod 24000`. Pinned in Task 1 tests.
2. **Potting a Chrono Bloom at noon**: the pot must show stage 2 right away, not stage 0 until a random tick. `FlowerPotBlock` places `potted.defaultBlockState()`, so the pot fixes itself in `onPlace`. Pinned by a GameTest in Task 2.
3. **Breaking a potted Chrono Bloom**: drops the flower and a flower pot. The existing potted Time Blossoms have no loot table, so do not copy that gap. Pinned by a GameTest in Task 2.
4. **Bonemeal on Temporal Grass in prairies**: must grow Chrono Bloom, which requires `patch_chrono_bloom` to use the `minecraft:flower` feature type. Pinned by the feature JSON check in Task 4.
5. **Leaves under a solid block**: falling particles must not spawn into a solid block below, rising ones not into a solid block above. Checked in the Task 5 manual run.

---

### Task 1: Stage function and per-version time access

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/blocks/ChronoBloomStage.java`
- Create: `common/shared/src/test/java/com/chronodawn/unit/ChronoBloomStageTest.java`
- Create: `common/<v>/src/main/java/com/chronodawn/compat/CompatLevelTime.java` for all 14 version modules

**Interfaces:**
- Produces: `ChronoBloomStage.stageFor(long dayTime) -> int` (0-3); `ChronoBloomStage.isNight(long dayTime) -> boolean` (true when stage is 0); constants `CLOSED = 0`, `OPENING = 1`, `OPEN = 2`, `CLOSING = 3`.
- Produces: `com.chronodawn.compat.CompatLevelTime.dayTime(Level level) -> long` and `CompatLevelTime.setDayTime(ServerLevel level, long time)` (used by GameTests).

- [ ] **Step 1: Write the failing test** `ChronoBloomStageTest` with a parameterized case per boundary:

```java
@ParameterizedTest
@CsvSource({
    "0,1", "999,1", "1000,2", "10999,2", "11000,3", "12999,3",
    "13000,0", "22999,0", "23000,1", "23999,1",
    "24000,1", "25000,2", "-1,1", "-11001,0", "240013000,0"
})
void stageForMapsTimeOfDay(long dayTime, int expected) {
    assertEquals(expected, ChronoBloomStage.stageFor(dayTime));
}

@Test
void isNightOnlyDuringClosedStage() {
    assertTrue(ChronoBloomStage.isNight(13000));
    assertFalse(ChronoBloomStage.isNight(12999));
    assertFalse(ChronoBloomStage.isNight(23000));
}
```

- [ ] **Step 2: Run it and confirm it fails to compile**

Run: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests com.chronodawn.unit.ChronoBloomStageTest`
Expected: FAIL, `ChronoBloomStage` not found.

- [ ] **Step 3: Implement `ChronoBloomStage`.** Use `Math.floorMod(dayTime, 24000L)` so negative values wrap. No Minecraft imports, so the class compiles unchanged in every version.

- [ ] **Step 4: Run the test and confirm it passes** (same command). Expected: PASS.

- [ ] **Step 5: Add `CompatLevelTime` to all 14 modules.** `common/1.20.1` to `common/1.21.11`: `level.getDayTime()` / `level.setDayTime(time)`. `common/26.1.2`, `26.2`, `26.3`: `level.getDefaultClockTime()`, and for the setter `level.clockManager().setTotalTicks(holder, time)` with the holder resolved as in that module's `TimeDistortionEventHandler.getClockHolder`.

- [ ] **Step 6: Compile every module.**

Run: `./gradlew buildAll`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/blocks/ChronoBloomStage.java common/shared/src/test/java/com/chronodawn/unit/ChronoBloomStageTest.java common/*/src/main/java/com/chronodawn/compat/CompatLevelTime.java
git commit -m "feat: add the Chrono Bloom stage function and per-version time access"
```

---

### Task 2: Chrono Bloom and potted Chrono Bloom on 1.21.1

Use `TimeBlossomBlock`, `TimelessMushroomBlock` (bonemeal signatures), and the purple Time Blossom data files in `common/1.21.1`, `common/shared`, and `common/shared-1.21.1+` as the pattern for every file below.

**Files:**
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/ChronoBloomBlock.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/PottedChronoBloomBlock.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` (add `CHRONO_BLOOM` and `POTTED_CHRONO_BLOOM`, both `.cutout()`, next to the Time Blossom entries)
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java` (add `CHRONO_BLOOM`)
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModComposting.java` (Chrono Bloom at 0.65 under `// Flowers`)
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java`, `ModItems.java` (registration, creative tab entry right after `PINK_TIME_BLOSSOM`)
- Modify: `common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestData.java` (add `ModBlocks.CHRONO_BLOOM` to the flowers group)
- Create assets in `common/shared/src/main/resources/assets/chronodawn/`: `blockstates/chrono_bloom.json`, `blockstates/potted_chrono_bloom.json` (one variant per `stage`), `models/block/chrono_bloom_stage{0..3}.json` (parent `minecraft:block/cross`), `models/block/potted_chrono_bloom_stage{0..3}.json` (parent `minecraft:block/flower_pot_cross`), `models/item/chrono_bloom.json` (`item/generated`, stage 2 texture), `textures/block/chrono_bloom_stage{0..3}.png`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/chrono_bloom.json` and `potted_chrono_bloom.json`
- Create: `common/1.21.1/src/main/resources/data/chronodawn/recipe/yellow_dye_from_chrono_bloom.json`
- Modify: `common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json`, `ja_jp.json`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/gametest/` (add a `ChronoBloomTests` class next to the existing flower tests)

**Interfaces:**
- Consumes: `ChronoBloomStage.stageFor`, `CompatLevelTime.dayTime` (Task 1).
- Produces: `ChronoBloomBlock.STAGE` (`IntegerProperty`, 0-3); `ModBlocks.CHRONO_BLOOM`, `ModBlocks.POTTED_CHRONO_BLOOM`, `ModItems.CHRONO_BLOOM`; block IDs `chronodawn:chrono_bloom`, `chronodawn:potted_chrono_bloom`.

- [ ] **Step 1: Write the failing GameTests** in `ChronoBloomTests`:
  - `chronoBloomPlacedAtNoonIsOpen`: set the level time to 6000 with `CompatLevelTime.setDayTime`, place `CHRONO_BLOOM` via its placement state, assert `STAGE == 2`.
  - `randomTickUpdatesStage`: place with `STAGE = 0` at time 6000, call `randomTick`, assert `STAGE == 2`.
  - `pottingAtNoonShowsOpenStage`: place an empty flower pot, use a Chrono Bloom item on it at time 6000, assert the block is `POTTED_CHRONO_BLOOM` with `STAGE == 2`.
  - `pottedBloomDropsFlowerAndPot`: break `POTTED_CHRONO_BLOOM` with a survival player, assert one `CHRONO_BLOOM` and one `FLOWER_POT` item entity.
  - `bonemealDropsOneBloom`: apply bonemeal, assert one `CHRONO_BLOOM` item entity and the block is unchanged.

- [ ] **Step 2: Run the GameTests and confirm they fail**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1`
Expected: FAIL to compile, `ModBlocks.CHRONO_BLOOM` not found.

- [ ] **Step 3: Implement `ChronoBloomBlock extends BushBlock implements BonemealableBlock`.** Declare `STAGE`, register `CLOSED` as the default state, and override `createBlockStateDefinition`, `getStateForPlacement` (stage from `CompatLevelTime.dayTime(context.getLevel())`), `isRandomlyTicking` (true), `randomTick` (set the new stage with flag 2 only when it differs), `isValidBonemealTarget` (true), `isBonemealSuccess` (true), and `performBonemeal` (`popResource` of one `ModItems.CHRONO_BLOOM`). Keep `codec()` as in `TimeBlossomBlock`. `createProperties()` copies Time Blossom's properties with `.randomTicks()` added and no light level.

- [ ] **Step 4: Implement `PottedChronoBloomBlock extends FlowerPotBlock`.** The constructor passes `ModBlocks.CHRONO_BLOOM.get()` as the content. Add `STAGE` to its state definition, set the stage in `onPlace` when it differs from `stageFor(time)` (server side only, flag 2), and update it in `randomTick` as in Step 3. Register it in `ModBlocks` with `CompatBlockProperties.ofFullCopy(Blocks.FLOWER_POT).randomTicks()`.

- [ ] **Step 5: Add the registrations, data, assets, and lang entries listed under Files.** Lang keys: `block.chronodawn.chrono_bloom` and `item.chronodawn.chrono_bloom` = "Chrono Bloom" / "クロノブルーム", `block.chronodawn.potted_chrono_bloom` = "Potted Chrono Bloom" / "クロノブルームの鉢植え". The potted loot table drops `chronodawn:chrono_bloom` and `minecraft:flower_pot`. Draw the four stage textures as original 16×16 pixel art: a closed bud, a half-open flower, a fully open flower with a clock-face center, and a half-closed flower. Report them as drafts.

- [ ] **Step 6: Run the GameTests and confirm they pass** (same command, then `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1`). Expected: all `ChronoBloomTests` PASS, no other test regresses.

- [ ] **Step 7: Run validations.**

Run: `./gradlew validateResources validateData validateTranslations`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add -A common/shared common/shared-1.21.1+ common/1.21.1
git commit -m "feat: add Chrono Bloom, a flower that opens and closes with the time of day"
```

---

### Task 3: Port Chrono Bloom to the other 13 versions

**Files (per module in Global Constraints, except 1.21.1):**
- Create: `blocks/ChronoBloomBlock.java`, `blocks/PottedChronoBloomBlock.java`, `gametest/ChronoBloomTests.java` (if the module has its own gametest sources)
- Modify: `registry/ModBlocks.java`, `registry/ModItems.java`, `gametest/RegistryDrivenTestData.java`
- Create data: 1.20.1 uses `loot_tables/`, `recipes/`, and the 1.20.1 recipe and loot formats; 26.3 has its own `loot_table/`; `common/shared-1.21.2+` holds the recipe for 1.21.2 to 1.21.11; check where 26.1.2 and 26.2 get the purple dye recipe and follow it.
- Create: Client Items JSON `items/chrono_bloom.json` and `items/potted_chrono_bloom.json` in `common/1.21.4` and `common/shared-1.21.5+`
- Modify: lang files in `common/1.20.1`, `common/shared-1.21.2+`, and any other module that has its own `lang/en_us.json`
- Modify: `common/26.3/src/test/java/com/chronodawn/registry/ModItemsFuelAndCompostTest.java` (add `Map.entry("CHRONO_BLOOM", "MEDIUM")`)

Copy each version's `TimeBlossomBlock` and `TimelessMushroomBlock` for the base class, `codec()`, `setId`/`createProperties(String id)`, and bonemeal signatures (1.20.1 has the `boolean isClientSide` parameter; 26.3 has `BonemealSource`).

- [ ] **Step 1: Port the Java and data to all 13 modules.**
- [ ] **Step 2: Build and unit test everything.**

Run: `./gradlew buildAll testAll`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Run validations and GameTests.**

Run: `./gradlew validateResources validateData validateTranslations gameTestAll`
Expected: BUILD SUCCESSFUL, `ChronoBloomTests` pass on every version.

- [ ] **Step 4: Commit**

```bash
git add -A common
git commit -m "feat: add Chrono Bloom to every supported version"
```

---

### Task 4: Chrono Bloom meadow in prairies

Data only, so it is done for all versions at once. It comes after Task 3 because the feature references `chronodawn:chrono_bloom`, which must be registered in every version before this data loads.

**Files:**
- Create next to each existing `patch_flowers.json` / `flower_block.json` (`common/shared`, `common/shared-1.21.1+`, `common/1.20.1`, `common/26.1.2`, `common/26.2`, `common/26.3`; 26.3 uses `worldgen/feature/`): configured `chrono_bloom_block` (simple block, `chronodawn:chrono_bloom` with `stage` 0), placed `chrono_bloom_block` (same predicate filter as `flower_block`), configured `patch_chrono_bloom` (`minecraft:flower`, `tries` 64, `xz_spread` 6, `y_spread` 2), placed `patch_chrono_bloom` (`count` 4, `in_square`, `heightmap WORLD_SURFACE_WG`, `biome`).
- Modify: `chronodawn_prairies.json` in `common/1.20.1`, `common/1.21.1`, `common/1.21.2`, `common/shared-1.21.2+`, `common/1.21.11`, `common/26.1.2`, `common/26.2`, `common/26.3`. Add `chronodawn:patch_chrono_bloom` to the vegetal-decoration step (index 9).

- [ ] **Step 1: Add the feature files and biome entries.** Mirror each module's existing flower files exactly for format.

- [ ] **Step 2: Check that every prairies file references the feature and every feature resolves.**

Run: `git grep -c "chronodawn:patch_chrono_bloom" -- '*chronodawn_prairies.json'` then `./gradlew validateData`
Expected: 8 files with count 1, and BUILD SUCCESSFUL.

- [ ] **Step 3: Run the production smoke test on 1.21.1 and 26.3** (new worldgen data must load on a real server).

Run: `./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=1.21.1` and `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=26.3`
Expected: both pass with no new WARN/ERROR lines.

- [ ] **Step 4: Commit**

```bash
git add -A common
git commit -m "feat: grow a Chrono Bloom meadow in the prairies"
```

---

### Task 5: Leaf particles on 1.21.1

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModParticles.java` (add `TIME_WOOD_LEAF`, `ANCIENT_TIME_WOOD_LEAF`, `DARK_TIME_WOOD_LEAF`, `NIGHT_BLOSSOM_PETAL`)
- Create: `common/shared/src/main/java/com/chronodawn/client/particle/TimeLeafParticle.java`
- Create: `common/shared/src/main/java/com/chronodawn/blocks/LeafParticleEffects.java`
- Create: `common/shared/src/main/resources/assets/chronodawn/particles/{time_wood_leaf,ancient_time_wood_leaf,dark_time_wood_leaf,night_blossom_petal}.json` and `textures/particle/` PNGs (original 8×8 drafts)
- Modify: `common/1.21.1/src/main/java/com/chronodawn/blocks/{TimeWoodLeaves,AncientTimeWoodLeaves,DarkTimeWoodLeaves}.java`
- Modify: `fabric/1.21.1/.../ChronoDawnClientFabric.java` `registerParticles()` and `neoforge/base/.../ChronoDawnClientNeoForge.java` sprite-set registration

**Interfaces:**
- Consumes: `ChronoBloomStage.isNight`, `CompatLevelTime.dayTime` (Task 1).
- Produces: `LeafParticleEffects.Kind { TIME_WOOD, ANCIENT_TIME_WOOD, DARK_TIME_WOOD }`; `LeafParticleEffects.spawn(Kind kind, Level level, BlockPos pos, RandomSource random)` spawns one effect without rolling the chance; `LeafParticleEffects.trySpawn(Kind, Level, BlockPos, RandomSource)` rolls `random.nextFloat() < 0.01f` first, for versions without the vanilla hook. `TimeLeafParticle.Motion { FALLING, RISING, GLOWING_FALLING }`; `TimeLeafParticle.Provider(SpriteSet sprites, Motion motion)`.

- [ ] **Step 1: Register the four particle types** in `ModParticles` with `createSimpleParticleType`.

- [ ] **Step 2: Implement `TimeLeafParticle`** (a `TextureSheetParticle`, modeled on `ChronoShieldEchoParticle` for the 1.21.1 API). `FALLING`: small downward gravity, horizontal sway, lifetime about 100-160 ticks. `RISING`: negative gravity at a similar speed, alpha fades to 0 over the lifetime. `GLOWING_FALLING`: half the falling speed, full brightness from `getLightColor`, translucent render type.

- [ ] **Step 3: Implement `LeafParticleEffects.spawn`.** `TIME_WOOD` and `DARK_TIME_WOOD` spawn their leaf particle just below the block when the block below is not sturdy on its `UP` face. `ANCIENT_TIME_WOOD` spawns just above when the block above is not sturdy on its `DOWN` face. `DARK_TIME_WOOD` also spawns `NIGHT_BLOSSOM_PETAL` below when `ChronoBloomStage.isNight(CompatLevelTime.dayTime(level))`.

- [ ] **Step 4: Hook the three 1.21.1 leaf classes.** Override `animateTick`, call `super`, then `LeafParticleEffects.trySpawn(<kind>, ...)`.

- [ ] **Step 5: Register providers** on Fabric and NeoForge 1.21.1 with `sprites -> new TimeLeafParticle.Provider(sprites, <motion>)`, next to the Time Torch flame registrations.

- [ ] **Step 6: Build and validate.**

Run: `./gradlew build1_21_1 validateResources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Manual check on 1.21.1 NeoForge and Fabric** (`runClientNeoForge1_21_1`, `runClientFabric1_21_1`). Confirm: leaves fall under Time Wood and Dark Time Wood; Ancient Time Wood leaves rise and fade; glowing petals appear under Dark Time Wood only at night; no particles inside a solid block placed under or over leaves. Ask the user to run the client if it cannot be driven headless.

- [ ] **Step 8: Commit**

```bash
git add -A common/shared common/1.21.1 fabric/1.21.1 neoforge/base
git commit -m "feat: add falling, rising, and night petal leaf particles"
```

---

### Task 6: Port leaf particles to the other 13 versions

**Files:**
- Modify: the three leaf classes in every other module. 1.20.1, 1.21.2, 1.21.4, 26.3: override `animateTick` and call `trySpawn` (26.3's `LeavesBlock` has no falling-leaf hook). 1.21.5 to 26.2: replace the empty `spawnFallingLeavesParticle` body with `LeafParticleEffects.spawn(...)`, since vanilla already rolls the chance.
- Create: `client/particle/TimeLeafParticle.java` overrides in the modules that override `ChronoShieldEchoParticle` (1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3), following that file's API changes.
- Modify: `ChronoDawnClientFabric.java` in every `fabric/<v>`, `ChronoDawnClientNeoForge.java` in every `neoforge/<v>` that has its own copy, and `forge/base/.../ChronoDawnClientForge.java`.

- [ ] **Step 1: Port the hooks, particle class overrides, and provider registrations.**
- [ ] **Step 2: Build and test everything.**

Run: `./gradlew buildAll testAll`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Production smoke test on a hook version and on 26.3.**

Run: `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=1.21.5` and `./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=26.3`
Expected: both pass.

- [ ] **Step 4: Commit**

```bash
git add -A common fabric neoforge forge
git commit -m "feat: add leaf particles to every supported version"
```

---

### Task 7: Docs and final verification

**Files:**
- Modify: `CHANGELOG.md` (`[Unreleased]` → `### Added`: Chrono Bloom, prairies meadow, leaf particles)
- Modify: `docs/player_guide.md` (flora section: Chrono Bloom stages, where it grows, bonemeal; leaf effects in one sentence)
- Check: `docs/curseforge_description.md`, `docs/modrinth_description.md` feature lists; update only if they list individual flowers.

- [ ] **Step 1: Update the docs.**
- [ ] **Step 2: Run full verification.**

Run: `./gradlew checkAll`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Manual checks listed in the spec's Testing section** on 1.20.1 Fabric, 1.21.1 NeoForge and Fabric, and 26.3 Fabric. In particular, confirm that Chrono Bloom follows Chrono Dawn's time inside the dimension on the client in 26.3, and that existing Time Blossoms look unchanged. Hand the texture drafts to the user for review.

- [ ] **Step 4: Commit**

```bash
git add CHANGELOG.md docs
git commit -m "docs: describe Chrono Bloom and the new leaf effects"
```
