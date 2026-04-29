# Chrono Dawn Small Features Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 7 small placed features (Time-Stopped Well, Petrified Adventurer, Time Cairn, Watchmaker's Camp, Old Sundial, Hourglass Monolith, Upside-Down Tree) to the Chrono Dawn dimension to fill space between dungeon-scale structures.

**Architecture:** A single shared `NbtTemplateFeature` Java class loads NBT templates and places them via `StructureTemplate#placeInWorld`. Each feature is a `configured_feature` JSON pointing this class at a different `.nbt` file plus a `placed_feature` JSON for density. Biome JSONs in all 5 biome-data directories are updated to reference the new placed features.

**Tech Stack:** Java 21 / Architectury Loom, Mojang mappings, `DeferredRegister<Feature<?>>`, `StructureTemplate`, vanilla `placed_feature` modifiers, NBT format.

**Spec:** [`docs/superpowers/specs/2026-04-27-chrono-dawn-small-features-design.md`](../specs/2026-04-27-chrono-dawn-small-features-design.md)

**Project commit policy:** This project's CLAUDE.md requires explicit user approval before each `git commit`. The "Commit" step inside each task is the trigger to ASK the user before running `git add` / `git commit`. Do not auto-commit.

---

## File Structure

### New files (Java)

```
common/shared/src/main/java/com/chronodawn/worldgen/features/
  NbtTemplateConfiguration.java     # FeatureConfiguration: ResourceLocation + rotation flag + Y offset
  NbtTemplateFeature.java           # Feature<NbtTemplateConfiguration>: loads NBT, places it

common/<each-version>/src/main/java/com/chronodawn/registry/
  ModFeatures.java                  # DeferredRegister<Feature<?>> wrapper, registers NbtTemplateFeature
                                    # Versions: 1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6,
                                    #          1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11
```

### New tests

```
common/shared/src/test/java/com/chronodawn/worldgen/features/
  NbtTemplateConfigurationCodecTest.java   # Codec round-trip
```

### New NBT files (×7) — placed in BOTH locations

```
common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/
common/1.20.1/src/main/resources/data/chronodawn/structure/
  time_well.nbt
  petrified_adventurer.nbt
  petrified_adventurer_snowy.nbt
  time_cairn.nbt
  watchmaker_camp.nbt
  old_sundial.nbt
  hourglass_monolith.nbt
  upside_down_tree.nbt
```

### New configured_feature / placed_feature JSONs (×8 each)

```
common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/
common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/
  time_well.json / time_well_placed.json
  petrified_adventurer.json / petrified_adventurer_placed.json
  petrified_adventurer_snowy.json / petrified_adventurer_snowy_placed.json
  time_cairn.json / time_cairn_placed.json
  watchmaker_camp.json / watchmaker_camp_placed.json
  old_sundial.json / old_sundial_placed.json
  hourglass_monolith.json / hourglass_monolith_placed.json
  upside_down_tree.json / upside_down_tree_placed.json
```

### New loot table

```
common/shared/src/main/resources/data/chronodawn/loot_table/chests/
  watchmaker_camp.json     # 1.21.1+ uses singular `loot_table/`

common/1.20.1/src/main/resources/data/chronodawn/loot_tables/chests/
  watchmaker_camp.json     # 1.20.1 uses plural `loot_tables/` (memory: feedback_loot_table_directory)
```

### Modified biome JSONs

```
common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/
common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/
common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/
common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/
common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/
  chronodawn_plains.json         (gets: time_well, petrified_adventurer, time_cairn, watchmaker_camp, old_sundial)
  chronodawn_forest.json         (gets: time_well, petrified_adventurer, time_cairn, watchmaker_camp)
  chronodawn_prairies.json       (gets: time_well, petrified_adventurer, time_cairn, old_sundial)
  chronodawn_ancient_forest.json (gets: petrified_adventurer, time_cairn, upside_down_tree)
  chronodawn_dark_forest.json    (gets: petrified_adventurer, time_cairn)
  chronodawn_mountain.json       (gets: petrified_adventurer, time_cairn)
  chronodawn_desert.json         (gets: petrified_adventurer, time_cairn, hourglass_monolith)
  chronodawn_snowy.json          (gets: petrified_adventurer_snowy, time_cairn)
  chronodawn_swamp.json          (gets: petrified_adventurer, time_cairn)
  # chronodawn_ocean.json — NO changes (out of scope, see spec)
```

### Modified Java init

```
common/shared/src/main/java/com/chronodawn/ChronoDawn.java   # add ModFeatures.register() call
```

---

## Phase 0: Foundation — Java Feature Class and Registration

### Task 0.1: Create NbtTemplateConfiguration class

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/worldgen/features/NbtTemplateConfiguration.java`

- [ ] **Step 1: Create the configuration class**

Path: `common/shared/src/main/java/com/chronodawn/worldgen/features/NbtTemplateConfiguration.java`

```java
package com.chronodawn.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration for {@link NbtTemplateFeature}.
 *
 * @param template     The structure template to load (e.g. "chronodawn:time_cairn").
 * @param randomRotate If true, the template is placed with a random 90-degree rotation.
 * @param yOffset      Vertical offset applied after heightmap snapping. Negative values
 *                     bury the structure into terrain (useful for "settled" footprints).
 */
public record NbtTemplateConfiguration(
        ResourceLocation template,
        boolean randomRotate,
        int yOffset
) implements FeatureConfiguration {

    public static final Codec<NbtTemplateConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("template").forGetter(NbtTemplateConfiguration::template),
                    Codec.BOOL.optionalFieldOf("random_rotate", Boolean.TRUE).forGetter(NbtTemplateConfiguration::randomRotate),
                    Codec.INT.optionalFieldOf("y_offset", 0).forGetter(NbtTemplateConfiguration::yOffset)
            ).apply(instance, NbtTemplateConfiguration::new)
    );
}
```

- [ ] **Step 2: Build-test compilation on a representative version**

Run from worktree root: `./gradlew :common-1.21.6:compileJava -Ptarget_mc_version=1.21.6`
Expected: BUILD SUCCESSFUL.

If it fails because `common-1.21.6` is not a project, use the equivalent module name from `settings.gradle` — discover with `./gradlew projects` once.

### Task 0.2: Write codec round-trip test

**Files:**
- Create: `common/shared/src/test/java/com/chronodawn/worldgen/features/NbtTemplateConfigurationCodecTest.java`

- [ ] **Step 1: Write the failing test**

Path: `common/shared/src/test/java/com/chronodawn/worldgen/features/NbtTemplateConfigurationCodecTest.java`

```java
package com.chronodawn.worldgen.features;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NbtTemplateConfigurationCodecTest {

    @Test
    void roundTripsAllFields() {
        NbtTemplateConfiguration original = new NbtTemplateConfiguration(
                ResourceLocation.parse("chronodawn:time_cairn"),
                false,
                -1
        );

        JsonElement encoded = NbtTemplateConfiguration.CODEC.encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        NbtTemplateConfiguration decoded = NbtTemplateConfiguration.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original.template(), decoded.template());
        assertEquals(original.randomRotate(), decoded.randomRotate());
        assertEquals(original.yOffset(), decoded.yOffset());
    }

    @Test
    void defaultsRandomRotateAndYOffsetWhenAbsent() {
        String json = "{\"template\":\"chronodawn:time_well\"}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        NbtTemplateConfiguration decoded = NbtTemplateConfiguration.CODEC.parse(JsonOps.INSTANCE, obj)
                .getOrThrow();

        assertEquals(ResourceLocation.parse("chronodawn:time_well"), decoded.template());
        assertTrue(decoded.randomRotate());
        assertEquals(0, decoded.yOffset());
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew :common-1.21.6:test --tests "com.chronodawn.worldgen.features.NbtTemplateConfigurationCodecTest" -Ptarget_mc_version=1.21.6`
Expected: BUILD SUCCESSFUL, 2 tests passed.

If `:common-1.21.6:test` is not the right module name, find it via `./gradlew projects | grep common`.

- [ ] **Step 3: Commit (ask user first per project policy)**

```bash
git add common/shared/src/main/java/com/chronodawn/worldgen/features/NbtTemplateConfiguration.java
git add common/shared/src/test/java/com/chronodawn/worldgen/features/NbtTemplateConfigurationCodecTest.java
git commit -m "feat(worldgen): add NbtTemplateConfiguration for small features"
```

### Task 0.3: Create NbtTemplateFeature class

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/worldgen/features/NbtTemplateFeature.java`

- [ ] **Step 1: Write the feature class**

Path: `common/shared/src/main/java/com/chronodawn/worldgen/features/NbtTemplateFeature.java`

```java
package com.chronodawn.worldgen.features;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Generic feature that loads an NBT structure template and places it at the
 * configured location. Used by Chrono Dawn's small ambient features (wells,
 * cairns, sundials, etc.).
 *
 * <p>Placement uses the MOTION_BLOCKING_NO_LEAVES heightmap so the template
 * sits on solid terrain rather than on top of leaves or grass. A configurable
 * Y offset lets templates "settle" partially into the ground.</p>
 *
 * <p>This feature does not run any structure processors. Block-state replacement
 * (e.g. mossy variants, biome-aware swaps) should be authored directly into the
 * NBT.</p>
 */
public class NbtTemplateFeature extends Feature<NbtTemplateConfiguration> {

    public NbtTemplateFeature() {
        super(NbtTemplateConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NbtTemplateConfiguration> context) {
        NbtTemplateConfiguration config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager manager = serverLevel.getStructureManager();
        StructureTemplate template = manager.get(config.template()).orElse(null);
        if (template == null) {
            ChronoDawn.LOGGER.warn("NbtTemplateFeature: template not found: {}", config.template());
            return false;
        }

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        BlockPos placePos = new BlockPos(origin.getX(), surfaceY + config.yOffset(), origin.getZ());

        Rotation rotation = config.randomRotate() ? Rotation.getRandom(random) : Rotation.NONE;

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false)
                .setRandom(random);

        BlockPos size = template.getSize();
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        settings.setRotationPivot(pivot);

        return template.placeInWorld(level, placePos, placePos, settings, random, 2);
    }
}
```

- [ ] **Step 2: Verify compilation on 1.21.6**

Run: `./gradlew :common-1.21.6:compileJava -Ptarget_mc_version=1.21.6`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Verify compilation on the API-corner versions**

Run sequentially (so failures are easier to attribute):
```
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1
./gradlew :common-1.21.2:compileJava -Ptarget_mc_version=1.21.2
./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11
```
Expected: all BUILD SUCCESSFUL.

If any version fails because of API drift (e.g. `Heightmap.Types` import path moved, `RandomSource` shape changed), copy `NbtTemplateFeature.java` into the failing version's `common/<version>/src/main/java/com/chronodawn/worldgen/features/` directory and adjust imports/calls there. Existing pattern: see `FruitOfTimeTreeFeature.java` overrides under `common/1.21.11/...`.

- [ ] **Step 4: Commit (ask user first)**

```bash
git add common/shared/src/main/java/com/chronodawn/worldgen/features/NbtTemplateFeature.java
# Also add any version-specific overrides if Step 3 produced them.
git commit -m "feat(worldgen): add NbtTemplateFeature for small NBT-backed features"
```

### Task 0.4: Create ModFeatures registry (per-version)

**Files:**
- Create (×11, one per version): `common/<version>/src/main/java/com/chronodawn/registry/ModFeatures.java`

The 11 versions: `1.20.1`, `1.21.1`, `1.21.2`, `1.21.4`, `1.21.5`, `1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11`.

This mirrors `ModStructureProcessorTypes` which lives in each `common/<version>/.../registry/` directory.

- [ ] **Step 1: Create the 1.21.6 reference implementation**

Path: `common/1.21.6/src/main/java/com/chronodawn/registry/ModFeatures.java`

```java
package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.features.NbtTemplateConfiguration;
import com.chronodawn.worldgen.features.NbtTemplateFeature;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Architectury Registry wrapper for Chrono Dawn custom feature types.
 *
 * <p>Currently registers a single generic {@link NbtTemplateFeature} which is
 * reused for all small-decoration features (wells, cairns, sundials, etc.).
 * Per-feature configuration lives in the corresponding configured_feature JSON.</p>
 */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.FEATURE);

    public static final RegistrySupplier<NbtTemplateFeature> NBT_TEMPLATE =
        FEATURES.register("nbt_template", NbtTemplateFeature::new);

    public static void register() {
        FEATURES.register();
        ChronoDawn.LOGGER.debug("Registered ModFeatures");
    }

    private ModFeatures() {
        throw new UnsupportedOperationException("Utility class");
    }
}
```

- [ ] **Step 2: Mirror the file to all 10 other version directories**

For each of `1.20.1`, `1.21.1`, `1.21.2`, `1.21.4`, `1.21.5`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11`, copy the file to `common/<version>/src/main/java/com/chronodawn/registry/ModFeatures.java`.

Use the Bash tool:
```
for v in 1.20.1 1.21.1 1.21.2 1.21.4 1.21.5 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11; do
  cp common/1.21.6/src/main/java/com/chronodawn/registry/ModFeatures.java \
     common/$v/src/main/java/com/chronodawn/registry/ModFeatures.java
done
```

- [ ] **Step 3: Verify compilation on each version**

```
for v in 1.20.1 1.21.1 1.21.2 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11; do
  ./gradlew :common-$v:compileJava -Ptarget_mc_version=$v || echo "FAIL: $v"
done
```
Expected: all BUILD SUCCESSFUL.

If a version fails on `Registries.FEATURE` import (1.20.1 may use a different path), adjust that version's file only. Reference: existing `ModStructureProcessorTypes.java` in the same version directory.

- [ ] **Step 4: Commit (ask user first)**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModFeatures.java
git commit -m "feat(worldgen): register ModFeatures with NbtTemplateFeature in all versions"
```

### Task 0.5: Wire ModFeatures.register() into ChronoDawn.init()

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/ChronoDawn.java`

- [ ] **Step 1: Add the import and call**

Edit `common/shared/src/main/java/com/chronodawn/ChronoDawn.java`. Add to the imports block (alphabetical with the other `com.chronodawn.registry.*`):

```java
import com.chronodawn.registry.ModFeatures;
```

Inside `public static void init()`, in the "Initialize registries (Phase 2 - Foundational)" group, just after `ModStructureProcessorTypes.register();`, add:

```java
        ModFeatures.register();
```

Verification — the resulting block should look like:

```java
        ModTreeDecoratorTypes.register();
        ModStructureProcessorTypes.register();
        ModFeatures.register();
```

- [ ] **Step 2: Build the entire 1.21.6 module to verify init still works**

Run: `./gradlew :common-1.21.6:build -Ptarget_mc_version=1.21.6 -x test`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit (ask user first)**

```bash
git add common/shared/src/main/java/com/chronodawn/ChronoDawn.java
git commit -m "feat(worldgen): wire ModFeatures into ChronoDawn init"
```

---

## Phase 1: Time Cairn — End-to-End PoC (smallest feature)

### Task 1.1: Build the time_cairn NBT in-game

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_cairn.nbt`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/structure/time_cairn.nbt`

The Time Cairn is a 1×1×3 (or 1×2×3 with foundation) static placement.

- [ ] **Step 1: Launch a creative test world**

Run: `./gradlew runClientFabric1_21_6` (any version works; we'll save the NBT once and copy to both target dirs).

In-game:
1. Create a flat creative world.
2. Place a Mossy Cobblestone Slab (top half) on the ground.
3. Place a Cobblestone block on top of the slab.
4. Place an Iron Trapdoor on top of the Cobblestone, oriented HORIZONTAL + CLOSED so the flat face reads upward like a small clock dial. (Right-click placement automatically picks the right axis when you hit the top face.)

The footprint should be exactly 1×1 with 3 blocks vertically (slab counts as half but still 1 block height).

- [ ] **Step 2: Capture with structure block**

1. Place a Structure Block in SAVE mode at the cairn base.
2. Set Structure Name to `chronodawn:time_cairn`.
3. Set the bounding box to start `0 1 0` from the structure block, size `1 3 1` (X=1, Y=3, Z=1).
4. Set "Include entities" = false.
5. Click SAVE.

The exported NBT lives at `<world-dir>/generated/chronodawn/structure/time_cairn.nbt`.

- [ ] **Step 3: Copy the NBT into the repo (both target dirs)**

```bash
cp <world-dir>/generated/chronodawn/structure/time_cairn.nbt \
   common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_cairn.nbt

cp <world-dir>/generated/chronodawn/structure/time_cairn.nbt \
   common/1.20.1/src/main/resources/data/chronodawn/structure/time_cairn.nbt
```

(Replace `<world-dir>` with the actual save directory path printed by the structure block, typically under `fabric/1.21.6/run/saves/<world-name>/`.)

- [ ] **Step 4: Verify file presence and rough size**

```bash
ls -la common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_cairn.nbt
ls -la common/1.20.1/src/main/resources/data/chronodawn/structure/time_cairn.nbt
```

Expected: both files exist, between ~100 bytes and ~2 KB. If a file is suspiciously empty (<50 bytes), the structure block save likely captured air; redo Step 2 with the bounding box anchored correctly.

### Task 1.2: Create configured_feature and placed_feature JSONs for time_cairn

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/time_cairn.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/time_cairn_placed.json`

- [ ] **Step 1: Write the configured_feature JSON**

Path: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/time_cairn.json`

```json
{
  "type": "chronodawn:nbt_template",
  "config": {
    "template": "chronodawn:time_cairn",
    "random_rotate": true,
    "y_offset": 0
  }
}
```

- [ ] **Step 2: Write the placed_feature JSON (T1 density, all land biomes)**

Path: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/time_cairn_placed.json`

```json
{
  "feature": "chronodawn:time_cairn",
  "placement": [
    { "type": "minecraft:rarity_filter", "chance": 2 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "offset": [0, -1, 0],
        "blocks": [
          "chronodawn:temporal_grass_block",
          "chronodawn:temporal_dirt",
          "chronodawn:temporal_sand",
          "chronodawn:temporal_coarse_dirt",
          "chronodawn:temporal_podzol"
        ]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

Note: the exact terrain block IDs (e.g. `chronodawn:temporal_grass_block`) must match the project's actual block IDs. Cross-check by grepping existing placed_feature files:
```
grep -rh "matching_blocks" common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ | head -20
```
If the IDs in this snippet differ, copy them from an existing placed_feature (e.g. `patch_grass.json`) instead.

- [ ] **Step 3: Validate JSON**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL with no warnings about `time_cairn.json` or `time_cairn_placed.json`.

### Task 1.3: Wire time_cairn into chronodawn_plains biome (PoC scope: one biome only)

**Files:**
- Modify: `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_plains.json` (around line 188; vegetal_decoration array)

- [ ] **Step 1: Add the placed feature to the vegetal_decoration list**

Edit the file. Find the array starting around line 177 (the one containing `"chronodawn:patch_grass"`, `"chronodawn:fruit_of_time_tree"`, etc.). Add `"chronodawn:time_cairn"` as the last entry of that array.

Before:
```json
      "chronodawn:gravel_disk_placed",
      "chronodawn:sand_disk_placed"
    ],
```

After:
```json
      "chronodawn:gravel_disk_placed",
      "chronodawn:sand_disk_placed",
      "chronodawn:time_cairn"
    ],
```

- [ ] **Step 2: Validate JSON**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL.

### Task 1.4: In-game verification of Time Cairn generation

- [ ] **Step 1: Launch a fresh world**

Run: `./gradlew runClientFabric1_21_6`

In-game:
1. Create a NEW Creative world (do not reuse an existing one — feature placement is fixed at chunk-generation time).
2. Use `/execute in chronodawn:chrono_dawn run tp @s 0 100 0` to enter the Chrono Dawn dimension. (Or use the in-mod portal if a quicker path is documented in `docs/player_guide.md`.)
3. Find a `chronodawn_plains` biome via `/locate biome chronodawn:chronodawn_plains`.
4. Walk ~200 blocks across the plains.

- [ ] **Step 2: Confirm visual presence**

Expected: at least 2–4 Time Cairns visible across that walk, each oriented at a different rotation. They should sit on the ground (no floating, no sinking through terrain).

If NONE appear:
- Re-check `placement` array in `time_cairn_placed.json` (is `chance` value reasonable? Does the `matching_blocks` predicate match the actual ground block in the biome?)
- Check world load logs for `"NbtTemplateFeature: template not found"` — if present, the NBT is in the wrong directory or the resource path is wrong.

If they appear floating in air or buried:
- Adjust `y_offset` in `time_cairn.json` (negative to bury, positive to raise).
- Reload world (or use `/reload` for data-pack changes; for placed-feature changes a new world is required).

- [ ] **Step 3: Commit Phase 1 (ask user first)**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_cairn.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/time_cairn.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/time_cairn.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/time_cairn_placed.json
git add common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_plains.json
git commit -m "feat(worldgen): add Time Cairn small feature (PoC)"
```

---

## Phase 2: Remaining 6 NBTs (one task per feature)

For each of the next six features, follow the same pattern as Phase 1 (build NBT in-game → copy to two directories → write configured_feature + placed_feature JSON → validate). The biome integration step is deferred to Phase 4 (single bulk pass).

The pattern for every feature task:

1. Build the NBT in-game using a structure block (size noted per feature).
2. Save with name `chronodawn:<feature_id>`.
3. Copy the NBT to BOTH `common/shared-1.21.1+/.../structure/` and `common/1.20.1/.../structure/`.
4. Write `configured_feature/<feature_id>.json` with `"type": "chronodawn:nbt_template"`.
5. Write `placed_feature/<feature_id>_placed.json` with the tier-appropriate `rarity_filter`.
6. Run `./gradlew validateResources`.
7. Commit (ask user first).

Tier → rarity_filter chance values (see spec):

| Tier | chance value |
|------|--------------|
| T1 (cairn — already done) | 2 |
| T2 (standard) | 16 |
| T3 (well) | 32 |
| T4 (loot camp) | 48 |

### Task 2.1: Petrified Adventurer (T2, 1×1×2 + Item Frame)

**Block plan:**
- 1× Polished Temporal Stone or Temporal Stone Slab (top half) at base — "worn legs".
- 1× Temporal Stone block above — "torso/head".
- 1× Temporal Stone Stair adjacent on the same level as the bust, oriented forward — "leaning posture".
- 1× Stone Bricks block on the ground beside the figure — flat tile for tools.
- 1× Item Frame on the Stone Bricks tile, holding 1× Iron Pickaxe (use Item Frame items inventory before saving).

**Files:**
- Create NBT: `common/shared-1.21.1+/.../structure/petrified_adventurer.nbt` and `common/1.20.1/.../structure/petrified_adventurer.nbt`
- Create: `configured_feature/petrified_adventurer.json` (template: `chronodawn:petrified_adventurer`, random_rotate: true, y_offset: 0)
- Create: `placed_feature/petrified_adventurer_placed.json` (T2: rarity_filter chance 16, biome filter, in_square, heightmap WORLD_SURFACE_WG, matching_blocks ground predicate same as time_cairn)

- [ ] **Step 1:** Build NBT in-game (size `3 3 3` to capture base + bust + adjacent stair + tile).
- [ ] **Step 2:** Copy NBT to both target dirs.
- [ ] **Step 3:** Write configured_feature JSON.
- [ ] **Step 4:** Write placed_feature JSON.

Path: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/petrified_adventurer.json`
```json
{
  "type": "chronodawn:nbt_template",
  "config": {
    "template": "chronodawn:petrified_adventurer",
    "random_rotate": true,
    "y_offset": 0
  }
}
```

Path: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/petrified_adventurer_placed.json`
```json
{
  "feature": "chronodawn:petrified_adventurer",
  "placement": [
    { "type": "minecraft:rarity_filter", "chance": 16 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "offset": [0, -1, 0],
        "blocks": [
          "chronodawn:temporal_grass_block",
          "chronodawn:temporal_dirt",
          "chronodawn:temporal_sand",
          "chronodawn:temporal_coarse_dirt",
          "chronodawn:temporal_podzol"
        ]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 5:** Run `./gradlew validateResources`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 6:** Commit (ask user first):
```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/petrified_adventurer.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/petrified_adventurer.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/petrified_adventurer.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/petrified_adventurer_placed.json
git commit -m "feat(worldgen): add Petrified Adventurer small feature"
```

### Task 2.2: Petrified Adventurer (snowy variant)

**Block plan:** Same as Task 2.1, with one Snow layer (`minecraft:snow` snow_height ≥ 4) in front of and around the bust to read as frostbitten.

**Files:**
- Create NBT: `petrified_adventurer_snowy.nbt` (both target dirs)
- Create: `configured_feature/petrified_adventurer_snowy.json`
- Create: `placed_feature/petrified_adventurer_snowy_placed.json`

- [ ] **Step 1:** Build NBT in-game (in a snowy biome with snow layer present).
- [ ] **Step 2:** Copy NBT to both target dirs.
- [ ] **Step 3:** Write configured_feature JSON (template: `chronodawn:petrified_adventurer_snowy`, otherwise identical to Task 2.1's JSON).
- [ ] **Step 4:** Write placed_feature JSON. Same as Task 2.1's placed_feature except:
  - `feature` is `chronodawn:petrified_adventurer_snowy`.
  - `matching_blocks` predicate adds `"minecraft:snow_block"` and `"minecraft:powder_snow"` to the allowed ground blocks.
  - rarity_filter `chance: 16` (T2).
- [ ] **Step 5:** Run `./gradlew validateResources`.
- [ ] **Step 6:** Commit (ask user first):
```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/petrified_adventurer_snowy.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/petrified_adventurer_snowy.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/petrified_adventurer_snowy.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/petrified_adventurer_snowy_placed.json
git commit -m "feat(worldgen): add Petrified Adventurer snowy variant"
```

### Task 2.3: Old Sundial (T2, 3×3×3)

**Block plan:**
- 3×3 base of Polished Stone Slabs (top half) on the ground.
- 1× Stone Bricks block at the center on top of the slab base.
- 1× Iron Bars block on top of the Stone Bricks (the gnomon).
- 1× Cobblestone Stairs at one corner of the base, oriented to read as a broken edge.

**Files:**
- Create NBT: `old_sundial.nbt` (both target dirs)
- Create: `configured_feature/old_sundial.json` (random_rotate: true, y_offset: 0)
- Create: `placed_feature/old_sundial_placed.json` (T2: chance 16)

- [ ] **Step 1:** Build NBT (size `3 3 3`).
- [ ] **Step 2:** Copy NBT to both target dirs.
- [ ] **Step 3:** Write configured_feature JSON.
- [ ] **Step 4:** Write placed_feature JSON (template same shape as Task 2.1, replace IDs).
- [ ] **Step 5:** Run `./gradlew validateResources`.
- [ ] **Step 6:** Commit (ask user first):
```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/old_sundial.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/old_sundial.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/old_sundial.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/old_sundial_placed.json
git commit -m "feat(worldgen): add Old Sundial small feature"
```

### Task 2.4: Hourglass Monolith (T2, 3×3×5, desert-only)

**Block plan:**
- Layer 1 (ground): 3×3 ring of Smooth Sandstone Wall. Hollow center.
- Layer 2: 3×3 solid Smooth Sandstone.
- Layer 3 (pinch): 1× Chiseled Sandstone at center; 8× air around it.
- Layer 4: 3×3 solid Smooth Sandstone (mirror of layer 2).
- Layer 5 (top): 3×3 ring of Smooth Sandstone Wall (mirror of layer 1).
- Floor of layer 1 (inside the ring): 2–3× Sand blocks ("settled sand that's already fallen").

**Files:**
- Create NBT: `hourglass_monolith.nbt` (both target dirs)
- Create: `configured_feature/hourglass_monolith.json` (random_rotate: true, y_offset: 0)
- Create: `placed_feature/hourglass_monolith_placed.json` (T2: chance 16, ground predicate restricted to `chronodawn:temporal_sand`)

- [ ] **Step 1:** Build NBT (size `3 5 3`).
- [ ] **Step 2:** Copy NBT to both target dirs.
- [ ] **Step 3:** Write configured_feature JSON.
- [ ] **Step 4:** Write placed_feature JSON. Use:
```json
{
  "feature": "chronodawn:hourglass_monolith",
  "placement": [
    { "type": "minecraft:rarity_filter", "chance": 16 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "offset": [0, -1, 0],
        "blocks": ["chronodawn:temporal_sand"]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```
- [ ] **Step 5:** Run `./gradlew validateResources`.
- [ ] **Step 6:** Commit (ask user first):
```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/hourglass_monolith.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/hourglass_monolith.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/hourglass_monolith.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/hourglass_monolith_placed.json
git commit -m "feat(worldgen): add Hourglass Monolith desert landmark"
```

### Task 2.5: Upside-Down Tree (T2, 4×4×6, ancient_forest only)

**Block plan:**
- Trunk (Time Wood Log axis=y): 3 blocks vertical at center.
- Inverted roots: 5–7 Time Wood Log blocks branching horizontally from the top of the trunk in a "splay" pattern (axis=x for some, axis=z for others).
- Inverted canopy at ground: a 3×3 ring of Time Wood Leaves (`persistent=true`) around the trunk base, with 1–2 Fruit of Time blocks substituted into random leaf positions.

**Files:**
- Create NBT: `upside_down_tree.nbt` (both target dirs)
- Create: `configured_feature/upside_down_tree.json` (random_rotate: true, y_offset: 0)
- Create: `placed_feature/upside_down_tree_placed.json` (T2: chance 16, ground predicate same as time_cairn)

- [ ] **Step 1:** Build NBT (size `4 6 4`).
- [ ] **Step 2:** Copy NBT to both target dirs.
- [ ] **Step 3:** Write configured_feature JSON.
- [ ] **Step 4:** Write placed_feature JSON.
- [ ] **Step 5:** Run `./gradlew validateResources`.
- [ ] **Step 6:** Commit (ask user first):
```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/upside_down_tree.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/upside_down_tree.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/upside_down_tree.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/upside_down_tree_placed.json
git commit -m "feat(worldgen): add Upside-Down Tree natural anomaly"
```

### Task 2.6: Time-Stopped Well (T3, 3×3×4)

**Block plan:**
- Frame layer 1: 3×3 ring of blocks. Corners = Cobblestone Wall, sides = Mossy Cobblestone, center = air.
- Frame layer 2: 3×3 solid ring. Corners = Cobblestone, sides = Mossy Cobblestone, center = Blue Ice (the "frozen water" effect).
- Frame layer 3: above two opposite corners, place a Time Wood Log (axis=y) on each — the well posts.
- Frame layer 4: bridge the two posts with a Time Wood Slab (top half) — the small roof.
- Item Frame: place on one of the posts at layer 2 height, holding a Bucket.
- Embedded reward: 1× Temporal Amber Block placed at layer 0 (one block below the Blue Ice). Capture this in the structure block bounding box.

NBT bounding box: start `0 0 0` from a structure block 1 block below ground level so the Temporal Amber Block at the bottom is included. Size `3 5 3`.

**Files:**
- Create NBT: `time_well.nbt` (both target dirs)
- Create: `configured_feature/time_well.json` (random_rotate: true, y_offset: -1 — sinks the well's bottom 1 block into ground)
- Create: `placed_feature/time_well_placed.json` (T3: chance 32, ground predicate same as time_cairn)

- [ ] **Step 1:** Build NBT.
- [ ] **Step 2:** Copy NBT to both target dirs.
- [ ] **Step 3:** Write configured_feature JSON. Note `y_offset: -1`.
- [ ] **Step 4:** Write placed_feature JSON.
- [ ] **Step 5:** Run `./gradlew validateResources`.
- [ ] **Step 6:** Commit (ask user first):
```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_well.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/time_well.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/time_well.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/time_well_placed.json
git commit -m "feat(worldgen): add Time-Stopped Well rare landmark"
```

### Task 2.7: Watchmaker's Camp (T4, 5×5×3)

**Block plan:**
- Center: Campfire (`lit=false`) on Coarse Dirt.
- Around the campfire (3×3 inside the 5×5 footprint, leaving a 1-block outer ring of grass terrain):
  - Crafting Table (north of campfire)
  - Lectern (east)
  - Chest (south, facing the campfire) — loot table set in NBT to `chronodawn:chests/watchmaker_camp`
  - Anvil with `chipped_anvil` block-state variant (west)
- Tent frame (on top of and around the chest area):
  - 4× Time Wood Fence as corner posts at the 5×5 corners, 1 block tall.
  - Roof: 3× White Wool spanning between posts at height 2.
  - 1× White Wool Stairs along one side as a sloped roof edge.
- Decoration:
  - 1× Item Frame on the Crafting Table side, holding 1× Clock.
  - 1× Cobblestone block adjacent to the campfire (a "stool").
- Ground inside the 3×3 tent area: Coarse Dirt (replace whatever terrain block is there).

NBT bounding box: size `5 3 5`.

**Loot in chest:** the chest's NBT must reference the loot table by string. When saving via the structure block, manually set the chest's `LootTable` NBT tag to `chronodawn:chests/watchmaker_camp` BEFORE saving (use `/data merge block <x> <y> <z> {LootTable:"chronodawn:chests/watchmaker_camp"}`). The loot table itself is created in Phase 3.

**Files:**
- Create NBT: `watchmaker_camp.nbt` (both target dirs)
- Create: `configured_feature/watchmaker_camp.json` (random_rotate: true, y_offset: 0)
- Create: `placed_feature/watchmaker_camp_placed.json` (T4: chance 48, ground predicate same as time_cairn)

- [ ] **Step 1:** Build NBT.
- [ ] **Step 2:** Set chest LootTable NBT tag via `/data merge block` BEFORE structure-block save.
- [ ] **Step 3:** Save structure with structure block, copy NBT to both target dirs.
- [ ] **Step 4:** Write configured_feature JSON.
- [ ] **Step 5:** Write placed_feature JSON.
- [ ] **Step 6:** Run `./gradlew validateResources`.
- [ ] **Step 7:** Commit (ask user first):
```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/watchmaker_camp.nbt
git add common/1.20.1/src/main/resources/data/chronodawn/structure/watchmaker_camp.nbt
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/watchmaker_camp.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/watchmaker_camp_placed.json
git commit -m "feat(worldgen): add Watchmaker's Camp rare loot feature"
```

---

## Phase 3: Loot Table for Watchmaker's Camp

The chest in `watchmaker_camp.nbt` references `chronodawn:chests/watchmaker_camp`. This phase creates the actual loot table at both directory paths (1.21.1+ singular, 1.20.1 plural — memory: feedback_loot_table_directory).

### Task 3.1: Write the 1.21.1+ loot table (singular dir)

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/loot_table/chests/watchmaker_camp.json`

- [ ] **Step 1: Verify item IDs against the existing item registry**

The loot table references the following items. Verify each exists by grepping the registry:
```
grep -E "(time_crystal_shard|temporal_amber|fruit_of_time|time_wood_log)" common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
```
If any name differs (e.g. `time_wood_log` is actually registered as `time_log`), adjust the loot table accordingly.

- [ ] **Step 2: Write the loot table**

Path: `common/shared/src/main/resources/data/chronodawn/loot_table/chests/watchmaker_camp.json`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": { "type": "minecraft:uniform", "min": 2, "max": 4 },
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:copper_ingot",
          "weight": 4,
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 3 } }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "chronodawn:time_wood_log",
          "weight": 3,
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 3 } }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "minecraft:bread",
          "weight": 3,
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 2 } }
          ]
        },
        { "type": "minecraft:item", "name": "minecraft:iron_ingot", "weight": 2 },
        {
          "type": "minecraft:item",
          "name": "chronodawn:time_crystal_shard",
          "weight": 2,
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 2 } }
          ]
        },
        { "type": "minecraft:item", "name": "minecraft:clock", "weight": 1 },
        { "type": "minecraft:item", "name": "minecraft:compass", "weight": 1 },
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_amber",
          "weight": 1,
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 2 } }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "chronodawn:fruit_of_time",
          "weight": 1,
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 2 } }
          ]
        }
      ]
    }
  ]
}
```

- [ ] **Step 3: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL.

### Task 3.2: Mirror the loot table to the 1.20.1 plural directory

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/chests/watchmaker_camp.json`

Note the directory name: `loot_tables` (plural) for 1.20.1 vs `loot_table` (singular) for 1.21.1+.

- [ ] **Step 1: Copy the file to the 1.20.1 plural directory**

```bash
mkdir -p common/1.20.1/src/main/resources/data/chronodawn/loot_tables/chests
cp common/shared/src/main/resources/data/chronodawn/loot_table/chests/watchmaker_camp.json \
   common/1.20.1/src/main/resources/data/chronodawn/loot_tables/chests/watchmaker_camp.json
```

- [ ] **Step 2: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit Phase 3 (ask user first)**

```bash
git add common/shared/src/main/resources/data/chronodawn/loot_table/chests/watchmaker_camp.json
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/chests/watchmaker_camp.json
git commit -m "feat(loot): add Watchmaker's Camp chest loot table"
```

---

## Phase 4: Biome Integration (the long tail)

Phase 1 wired Time Cairn into `chronodawn_plains.json` in `shared-1.21.2+/` only. This phase adds all 8 placed features to all 9 land biomes across all 5 biome directories.

### Task 4.1: Build the biome × feature matrix as a checklist

Use this matrix to track edits. **Each cell is one Edit.** Total ≈ 9 biomes × 5 dirs × ~3 feature-additions on average = ~135 cell-edits, but many cells are identical so the work is mostly mechanical.

**Per-biome feature lists** (placed-feature IDs added to vegetal_decoration array):

| biome | features added |
|-------|----------------|
| chronodawn_plains | time_well, petrified_adventurer, time_cairn (already present in shared-1.21.2+ only — re-check), watchmaker_camp, old_sundial |
| chronodawn_forest | time_well, petrified_adventurer, time_cairn, watchmaker_camp |
| chronodawn_prairies | time_well, petrified_adventurer, time_cairn, old_sundial |
| chronodawn_ancient_forest | petrified_adventurer, time_cairn, upside_down_tree |
| chronodawn_dark_forest | petrified_adventurer, time_cairn |
| chronodawn_mountain | petrified_adventurer, time_cairn |
| chronodawn_desert | petrified_adventurer, time_cairn, hourglass_monolith |
| chronodawn_snowy | petrified_adventurer_snowy, time_cairn |
| chronodawn_swamp | petrified_adventurer, time_cairn |

**Per-version-dir scope:** apply the same matrix to all 5 biome directories:
1. `common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/`
2. `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/`
3. `common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/`
4. `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/`
5. `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/`

(Memory: `feedback_1_21_11_biome_overrides` — 1.21.11 has its own biome override directory that must be updated independently.)

For each biome JSON, the additions go inside the existing `features` array's vegetal_decoration step (the array containing `chronodawn:patch_grass` etc., typically index 9 — same array Phase 1 modified).

- [ ] **Step 1: Sanity-check that all 5 dirs contain all 9 land biome JSONs**

```bash
for dir in \
  common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome \
  common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome \
  common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome \
  common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome \
  common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome ; do
  echo "=== $dir ==="
  ls "$dir"
done
```
Expected: each lists `chronodawn_plains.json`, `chronodawn_forest.json`, `chronodawn_prairies.json`, `chronodawn_ancient_forest.json`, `chronodawn_dark_forest.json`, `chronodawn_mountain.json`, `chronodawn_desert.json`, `chronodawn_snowy.json`, `chronodawn_swamp.json`. (Plus `chronodawn_ocean.json` which we leave untouched.)

If a biome JSON is missing from a directory, that means the version inherits from an earlier-shared directory and does NOT need to be edited there — proceed without it.

### Task 4.2: Update biomes in shared-1.21.2+ (covers 1.21.4–1.21.10)

This is the largest single edit pass. Edit each of the 9 biome JSONs in `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/`, adding the corresponding placed_feature IDs from the matrix to the vegetal_decoration array (the same array updated in Task 1.3).

- [ ] **Step 1: Edit chronodawn_plains.json**

For chronodawn_plains, the array currently has `chronodawn:time_cairn` (from Phase 1). Now ALSO add: `chronodawn:time_well`, `chronodawn:petrified_adventurer`, `chronodawn:watchmaker_camp`, `chronodawn:old_sundial`.

After the edit, the trailing portion of that array should look like:
```json
      "chronodawn:gravel_disk_placed",
      "chronodawn:sand_disk_placed",
      "chronodawn:time_cairn",
      "chronodawn:time_well",
      "chronodawn:petrified_adventurer",
      "chronodawn:watchmaker_camp",
      "chronodawn:old_sundial"
    ],
```

- [ ] **Step 2: Edit chronodawn_forest.json**

Find the vegetal_decoration array (the one containing `chronodawn:patch_*` entries). Add as a contiguous block:
```
"chronodawn:time_well",
"chronodawn:petrified_adventurer",
"chronodawn:time_cairn",
"chronodawn:watchmaker_camp"
```

- [ ] **Step 3: Edit chronodawn_prairies.json**

Add: `time_well`, `petrified_adventurer`, `time_cairn`, `old_sundial`.

- [ ] **Step 4: Edit chronodawn_ancient_forest.json**

Add: `petrified_adventurer`, `time_cairn`, `upside_down_tree`.

- [ ] **Step 5: Edit chronodawn_dark_forest.json**

Add: `petrified_adventurer`, `time_cairn`.

- [ ] **Step 6: Edit chronodawn_mountain.json**

Add: `petrified_adventurer`, `time_cairn`.

- [ ] **Step 7: Edit chronodawn_desert.json**

Add: `petrified_adventurer`, `time_cairn`, `hourglass_monolith`.

- [ ] **Step 8: Edit chronodawn_snowy.json**

Add: `petrified_adventurer_snowy`, `time_cairn`. (Note: snowy variant, NOT the plain `petrified_adventurer`.)

- [ ] **Step 9: Edit chronodawn_swamp.json**

Add: `petrified_adventurer`, `time_cairn`.

- [ ] **Step 10: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL with no warnings about biome JSONs.

### Task 4.3: Update biomes in 1.20.1 directory

Repeat Task 4.2's 9 sub-edits in `common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/`. The 1.20.1 biome JSONs may differ slightly in the surrounding fields (e.g. `effects` block) but the `features` array shape is the same.

- [ ] **Step 1–9: Apply each of the 9 biome edits in 1.20.1**

(Same content as Task 4.2; if exact line numbers differ, find the vegetal_decoration array by searching for `chronodawn:patch_grass`.)

- [ ] **Step 10: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL.

### Task 4.4: Update biomes in 1.21.1 directory

Repeat in `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/`.

- [ ] **Step 1–9: Apply each of the 9 biome edits in 1.21.1**
- [ ] **Step 10: Validate**

### Task 4.5: Update biomes in 1.21.2 directory

Repeat in `common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/`. Some biome files here may be intentional overrides of `shared-1.21.2+/`; check that the `features` arrays diverge before assuming the edits are mirror-identical. If the 1.21.2 version of a biome lacks the `chronodawn:patch_grass` line, it is probably an override that intentionally drops vegetation — do NOT add features to it without checking with the user.

- [ ] **Step 1: Compare 1.21.2 biome JSONs against shared-1.21.2+**

```bash
for biome in chronodawn_plains chronodawn_forest chronodawn_prairies \
             chronodawn_ancient_forest chronodawn_dark_forest chronodawn_mountain \
             chronodawn_desert chronodawn_snowy chronodawn_swamp ; do
  echo "=== $biome ==="
  diff common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/$biome.json \
       common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/$biome.json
done
```
If diff output is empty for a biome, the 1.21.2 file is identical to shared and we just apply the same edit. If diff output exists, inspect manually before editing.

- [ ] **Step 2–10: Apply edits and validate** (per-biome decisions per Step 1).

### Task 4.6: Update biomes in 1.21.11 directory

Repeat in `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/`. Per memory `feedback_1_21_11_biome_overrides` and `feedback_dimension_type_1_21_11`, the 1.21.11 biome format may differ from earlier versions in fields outside `features`. The `features` array shape itself is unchanged.

- [ ] **Step 1–9: Apply each of the 9 biome edits in 1.21.11**
- [ ] **Step 10: Validate**

### Task 4.7: Commit Phase 4

- [ ] **Step 1: Commit (ask user first)**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/
git add common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/
git add common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/
git add common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/
git add common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/
git commit -m "feat(worldgen): wire 7 small features into Chrono Dawn biomes"
```

---

## Phase 5: Verification

### Task 5.1: validateResources

- [ ] **Step 1: Run validation**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL with no warnings about any of the 16+1 new JSONs (8 configured_feature, 8 placed_feature, 1 loot table) or any biome edit.

If a warning surfaces, fix it inline before proceeding.

### Task 5.2: Run unit tests

- [ ] **Step 1: Run codec test on representative versions**

```
./gradlew :common-1.21.6:test --tests "com.chronodawn.worldgen.features.NbtTemplateConfigurationCodecTest" -Ptarget_mc_version=1.21.6
./gradlew :common-1.20.1:test --tests "com.chronodawn.worldgen.features.NbtTemplateConfigurationCodecTest" -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.11:test --tests "com.chronodawn.worldgen.features.NbtTemplateConfigurationCodecTest" -Ptarget_mc_version=1.21.11
```
Expected: all BUILD SUCCESSFUL, 2 tests passed each.

### Task 5.3: Targeted builds (sampling API eras)

- [ ] **Step 1: Build representative versions**

```
./gradlew build1_20_1
./gradlew build1_21_1
./gradlew build1_21_6
./gradlew build1_21_11
```
Expected: all BUILD SUCCESSFUL.

### Task 5.4: checkAll

Per memory `feedback_check_all_before_phase_completion`, the multi-version verification is the gate.

- [ ] **Step 1: Run checkAll**

Run: `./gradlew checkAll`
Expected: BUILD SUCCESSFUL covering cleanAll → validateResources → validateTranslations → buildAll → testAll → gameTestAll.

If any phase fails, stop and fix the underlying issue before continuing. Do NOT skip versions.

### Task 5.5: Manual in-game verification

- [ ] **Step 1: Test on 1.21.6 (most-developed version)**

Run: `./gradlew runClientFabric1_21_6`

In-game checklist:
1. New world, creative mode.
2. Enter chronodawn:chrono_dawn dimension.
3. `/locate biome chronodawn:chronodawn_plains` → walk ~500 blocks. Expect at least one Time-Stopped Well, several Time Cairns, multiple Petrified Adventurers, at least one Watchmaker's Camp, and Old Sundials.
4. Open a Watchmaker's Camp chest. Expect 2–4 stacks from the loot table.
5. `/locate biome chronodawn:chronodawn_desert` → walk to a Hourglass Monolith.
6. `/locate biome chronodawn:chronodawn_ancient_forest` → walk to an Upside-Down Tree.
7. `/locate biome chronodawn:chronodawn_snowy` → confirm Petrified Adventurer Snowy variants.

- [ ] **Step 2: Test on 1.20.1 (legacy era)**

Run: `./gradlew runClientFabric1_20_1`

Repeat the checklist. Particularly verify:
- Watchmaker chest loot is present (proves the plural `loot_tables/` directory split worked).
- All NBTs load (proves the 1.20.1 NBT mirror works).

- [ ] **Step 3: Test on 1.21.11 (latest era)**

Run: `./gradlew runClientFabric1_21_11`

Repeat the checklist. Particularly verify:
- Features generate (proves the 1.21.11 biome override directory was updated correctly).

- [ ] **Step 4: Test NeoForge on 1.21.6**

Run: `./gradlew runClientNeoForge1_21_6`

Spot-check that at least Time Cairns appear (proves loader-agnostic registration works).

### Task 5.6: Final commit-of-record (ask user first)

- [ ] **Step 1: Confirm working tree is clean**

```bash
git status
```
Expected: nothing to commit. (All work was committed phase by phase.)

- [ ] **Step 2: Push and open PR (ask user first)**

This step is held until the user explicitly requests merging. Per the project policy (CLAUDE.md), do not push or create PRs without instruction.

---

## Self-Review Notes

The plan was reviewed against the spec on 2026-04-27. Spec coverage:

| Spec section | Covered by |
|--------------|-----------|
| Goal — 7 features in 5 categories | Phase 1 + Phase 2 (Tasks 1.1–2.7) |
| Architecture — single NbtTemplateFeature class | Phase 0 (Tasks 0.1–0.5) |
| Per-feature block compositions | Phase 1 + Phase 2, "Block plan" sub-sections |
| Watchmaker loot table | Phase 3 (Tasks 3.1–3.2) |
| Density tiers | placed_feature `rarity_filter chance` per task |
| Biome assignments matrix | Phase 4, Task 4.1 matrix |
| Multi-version data files | Phase 4 covers all 5 biome dirs; loot table covers both `loot_table/` and `loot_tables/` |
| Risks — biome update completeness | Phase 4 has explicit per-version-dir tasks |
| Risks — loot table directory split | Task 3.2 mirrors to plural dir |
| Risks — Item Frame rotation | Verified manually in Task 5.5 |
| Acceptance criteria — checkAll passes | Task 5.4 |
| Acceptance criteria — manual world tests | Task 5.5 |

No gaps identified. No placeholder content remains. No TBD/TODO markers in the plan body.

---

## Appendix A: NBT Building Reference

Single source of truth for the 8 small-feature NBT structures (7 features +
1 snowy variant) that need to be authored in-game with structure blocks.
The Phase 1 + Phase 2 task `Block plan:` sub-sections summarise; this
appendix has the full layered detail and is the recommended companion
while building.

### A.1 Common pitfalls

- **Item Frames are entities, not block entities.** Any structure that
  contains an Item Frame must be saved with `Include entities: ON` on the
  structure block. Otherwise the frames disappear from the saved NBT.
- **Chest LootTable must be set BEFORE saving.** Place the chest, then run
  `/data merge block <x> <y> <z> {LootTable:"chronodawn:chests/watchmaker_camp"}`,
  THEN save with the structure block. If you save first the chest will
  generate empty in-world.
- **Sand has gravity.** Any `minecraft:sand` block in an NBT must have a
  solid block directly above it (or be at the very top of its column with
  no air gap below). Sand placed above air will fall on chunk load and
  destroy the visual.
- **Time Wood Leaves should be `persistent=true`.** Otherwise the leaves
  decay when no log is in range. Place them with the leaves item (which
  sets persistent automatically) or override via `/setblock`.
- **Iron Trapdoor orientation.** Right-click the top face of a block to
  place a closed trapdoor with `half=top, open=false`. That gives the
  flat upward-facing "clock dial" pose used by `time_cairn`.
- **Save NBT to `chronodawn:<name>`.** This makes the export land in
  `<world>/generated/chronodawn/structure/<name>.nbt`. Copy that file to
  BOTH `common/shared-1.21.1+/.../structure/` and `common/1.20.1/.../structure/`.
- **Sizes below are `X × Y(height) × Z`** unless otherwise noted.

### A.2 Block plans

#### A.2.1 `time_cairn` — Time Cairn (T1, all land biomes)

Size: **1 × 3 × 1**

```
Y=2 (top)    : Iron Trapdoor (closed, half=top, horizontal — clock-dial pose)
Y=1 (middle) : Cobblestone
Y=0 (base)   : Mossy Cobblestone Slab (top half)
```

Block count: 3. `Include entities: false`. Save name: `chronodawn:time_cairn`.

#### A.2.2 `petrified_adventurer` — Petrified Adventurer (T2, 8 land biomes excl. snowy)

Size: **3 × 3 × 3**

```
Y=2 (top)  : all air
Y=1 (mid)  : center = Temporal Stone (torso/head)
             one block in front of center = Temporal Stone Stair (forward-leaning posture; orientation
             rotates with random_rotate)
Y=0 (base) : center = Polished Temporal Stone Slab (top half) — the "worn legs"
             one block to the side of center = Stone Bricks (the tool tile)
```

Item Frame: place on the top face of the Stone Bricks tile, holding 1×
Iron Pickaxe. `Include entities: ON`.

Block count: 4 + 1 entity. Save name: `chronodawn:petrified_adventurer`.

#### A.2.3 `petrified_adventurer_snowy` — Snowy variant (T2, snowy biome only)

Same as A.2.2, plus 4–6 `minecraft:snow` blocks (`layers=4`) scattered in
the surrounding 3×3 ring on Y=0 to read as half-buried frostbitten remains.

Size: **3 × 3 × 3**. `Include entities: ON` (Item Frame). Save name:
`chronodawn:petrified_adventurer_snowy`.

#### A.2.4 `time_well` — Time-Stopped Well (T3, plains/forest/prairies)

Size: **3 × 5 × 3**. Configured-feature `y_offset = -1`, so when saving,
anchor the structure block so the saved Y=0 lands one block BELOW the
target ground level (the buried Temporal Amber sits there).

```
Y=4 (roof)   : Time Wood Slab (top half) bridging the two posts (1 block)
Y=3 (posts)  : two opposite corners = Time Wood Log (axis=y); other 7 cells = air
Y=2 (rim)    : 4 corners = Cobblestone Wall; 4 sides = Cobblestone; center = Blue Ice
Y=1 (shaft)  : 4 corners = Cobblestone; 4 sides = Mossy Cobblestone; center = air
Y=0 (bottom) : center = Temporal Amber Block (buried reward); 8 surrounding cells unused/air
```

Item Frame: attach to the side of one Time Wood Log post at Y=3, holding
1× Bucket. `Include entities: ON`.

Save name: `chronodawn:time_well`.

#### A.2.5 `watchmaker_camp` — Watchmaker's Camp (T4, plains/forest)

Size: **5 × 3 × 5**

```
Y=2 (roof tip) : optional 1× White Wool Stairs along one edge for a sloped roof end; otherwise air
Y=1 (tent)     : 3× White Wool spanning between corner fence posts (the tent ceiling)
                 + 1× Item Frame attached to the side of the Crafting Table holding a Clock
Y=0 (camp)     : center = Campfire (lit=false) on Coarse Dirt
                 north = Crafting Table
                 east  = Lectern
                 south = Chest (LootTable set per A.1 — chronodawn:chests/watchmaker_camp)
                 west  = Anvil with the chipped variant block-state
                 4 corners (5×5) = Time Wood Fence (tent posts)
                 1 cell next to the campfire = Cobblestone (a stool)
                 inner 3×3 floor = Coarse Dirt
```

`Include entities: ON` (Item Frame). Set Chest's `LootTable` NBT before
saving via `/data merge block`. Save name: `chronodawn:watchmaker_camp`.

#### A.2.6 `old_sundial` — Old Sundial (T2, plains/prairies)

Size: **3 × 3 × 3**

```
Y=2 (top)  : center = Iron Bars (the gnomon, 1 block tall); other 8 cells = air
Y=1 (mid)  : center = Stone Bricks (pedestal column)
             one corner = Cobblestone Stairs (broken-edge detail; orientation
             rotates with random_rotate); other 7 cells = air
Y=0 (base) : 3×3 of Polished Stone Slab (top half) = the platform
```

Block count: 9 + 1 + 1 + 1 = 12. `Include entities: false`. Save name:
`chronodawn:old_sundial`.

#### A.2.7 `hourglass_monolith` — Hourglass Monolith (T2, desert)

Size: **3 × 5 × 3**

```
Y=4 (top ring)  : 3×3 ring of Smooth Sandstone Wall (8 blocks); center = air
Y=3 (upper bulb): 3×3 solid Smooth Sandstone (9 blocks)
Y=2 (pinch)     : center = Chiseled Sandstone (1 block); 8 surrounding cells = air
Y=1 (lower bulb): 3×3 solid Smooth Sandstone (9 blocks)
Y=0 (base ring) : 3×3 ring of Smooth Sandstone Wall (8 blocks); center = 1× Sand
                  (the "settled" sand that has finished running through)
```

The single Sand block at Y=0 center is safe — the Y=1 Smooth Sandstone
above it acts as a lid, so it does not fall on chunk load.

`Include entities: false`. Save name: `chronodawn:hourglass_monolith`.

#### A.2.8 `upside_down_tree` — Upside-Down Tree (T2, ancient_forest)

Size: **4 × 6 × 4** (centered roughly on a 3,3 origin in the bounding box)

```
Y=5 (root tips)  : 1–2 Time Wood Log blocks placed on the outer ends of Y=4 logs
                   to imply tapering root tips (axis matches the log they extend)
Y=4 (root splay) : 5–7 Time Wood Log blocks branching horizontally from the
                   trunk top — center plus 4 cardinal directions, with axis=x
                   and axis=z mixed so the splay reads gnarled rather than uniform
Y=3 (trunk top)  : center = Time Wood Log (axis=y)
Y=2 (trunk mid)  : center = Time Wood Log (axis=y)
Y=1 (trunk base) : center = Time Wood Log (axis=y)
Y=0 (canopy at ground) :
                   3×3 ring around the trunk base = Time Wood Leaves (persistent=true)
                   1–2 random leaf cells replaced with Fruit of Time block
```

`Include entities: false`. Save name: `chronodawn:upside_down_tree`.

### A.3 Recommended build order

Easiest → hardest, so toolchain quirks (entities, chest NBT, gravity)
are encountered incrementally:

1. `time_cairn` — smallest, validates Iron Trapdoor pose
2. `old_sundial` — medium, no entities, no special blocks
3. `hourglass_monolith` — medium, validates the Sand-with-lid trick
4. `upside_down_tree` — medium, validates `persistent=true` on leaves
5. `petrified_adventurer` — first one with an Item Frame
6. `petrified_adventurer_snowy` — clone of #5 with snow layers
7. `time_well` — buried block + Item Frame on a post
8. `watchmaker_camp` — largest, requires chest LootTable NBT before save

After each NBT is saved into both `shared-1.21.1+/.../structure/` and
`1.20.1/.../structure/`, run `./gradlew validateResources`. The validation
does not check NBT contents directly, so this only catches accidental JSON
breakage from copy/paste mistakes. In-world verification of all eight
features should be done in one pass at the end (Phase 5 Task 5.5).
