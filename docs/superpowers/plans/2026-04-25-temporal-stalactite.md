# Temporal Stalactite / Stalagmite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Temporal Stone-themed stalactite / stalagmite blocks that
generate decoratively in the caves of every ChronoDawn dimension biome,
across all 11 supported Minecraft versions.

**Architecture:** Four static decorative blocks (`temporal_stalactite_tip`,
`temporal_stalactite_frustum`, `temporal_stalagmite_tip`,
`temporal_stalagmite_frustum`). Each is a thin `Block` subclass with a
hardcoded VoxelShape and the same hardness/tool requirements as
`TemporalStoneBlock`. `frustum` blocks drop the matching `tip` so worldgen
can place length-2 formations without producing awkward standalone-frustum
inventory items. Worldgen uses three configured + three placed features
added to the `UNDERGROUND_DECORATION` step of all 10 ChronoDawn biomes in
each supported version.

**Tech Stack:** Java 21, NeoForge / Fabric via Architectury 13.x–19.x,
Mojang mappings, Mixin (not used here), per-version source modules under
`common/<version>/`, shared resources under `common/shared/` and
`common/shared-1.21.1+/`.

**Spec:** `docs/superpowers/specs/2026-04-25-temporal-stalactite-design.md`

---

## File Inventory

### Created — Java sources (44 files, 4 per version)

For each `<v>` in {1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7,
1.21.8, 1.21.9, 1.21.10, 1.21.11}:

- `common/<v>/src/main/java/com/chronodawn/blocks/TemporalStalactiteTipBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/TemporalStalactiteFrustumBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/TemporalStalagmiteTipBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/TemporalStalagmiteFrustumBlock.java`

### Modified — Java sources

- `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` (add 4 IDs)
- `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java` (×11, register 4 blocks)
- `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java` (×11, add 4 BlockItems + creative tab entries)

### Created — shared resources (under `common/shared/...`)

Models (8 — 4 block + 4 item):
- `assets/chronodawn/models/block/temporal_stalactite_tip.json`
- `assets/chronodawn/models/block/temporal_stalactite_frustum.json`
- `assets/chronodawn/models/block/temporal_stalagmite_tip.json`
- `assets/chronodawn/models/block/temporal_stalagmite_frustum.json`
- `assets/chronodawn/models/item/temporal_stalactite_tip.json`
- `assets/chronodawn/models/item/temporal_stalactite_frustum.json`
- `assets/chronodawn/models/item/temporal_stalagmite_tip.json`
- `assets/chronodawn/models/item/temporal_stalagmite_frustum.json`

Blockstates (4):
- `assets/chronodawn/blockstates/temporal_stalactite_tip.json`
- `assets/chronodawn/blockstates/temporal_stalactite_frustum.json`
- `assets/chronodawn/blockstates/temporal_stalagmite_tip.json`
- `assets/chronodawn/blockstates/temporal_stalagmite_frustum.json`

Textures (4):
- `assets/chronodawn/textures/block/temporal_stalactite_tip.png`
- `assets/chronodawn/textures/block/temporal_stalactite_frustum.png`
- `assets/chronodawn/textures/block/temporal_stalagmite_tip.png`
- `assets/chronodawn/textures/block/temporal_stalagmite_frustum.png`

Worldgen (6):
- `data/chronodawn/worldgen/configured_feature/temporal_stalactite_cluster.json`
- `data/chronodawn/worldgen/configured_feature/temporal_stalagmite_cluster.json`
- `data/chronodawn/worldgen/configured_feature/temporal_pointed_simple.json`
- `data/chronodawn/worldgen/placed_feature/temporal_stalactite_cluster.json`
- `data/chronodawn/worldgen/placed_feature/temporal_stalagmite_cluster.json`
- `data/chronodawn/worldgen/placed_feature/temporal_pointed_single.json`

### Modified — shared resources

- `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json` (4 keys)
- `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json` (4 keys)

### Created — version-split loot tables (8 files)

- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_stalactite_tip.json`
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_stalactite_frustum.json`
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_stalagmite_tip.json`
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_stalagmite_frustum.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_stalactite_tip.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_stalactite_frustum.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_stalagmite_tip.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_stalagmite_frustum.json`

### Modified — tags (4 entries each in 4 files)

- `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/needs_stone_tool.json`
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/needs_stone_tool.json`

### Modified — biome JSON files (50 total)

- 10 biomes × `common/1.20.1/.../worldgen/biome/`
- 10 biomes × `common/1.21.1/.../worldgen/biome/`
- 10 biomes × `common/1.21.2/.../worldgen/biome/` (per-version, music format pre-1.21.4)
- 10 biomes × `common/shared-1.21.2+/.../worldgen/biome/` (covers 1.21.4–1.21.10)
- 10 biomes × `common/1.21.11/.../worldgen/biome/` (now includes `chronodawn_prairies` after d873fd4e)

---

## Phase 1: Foundation on 1.21.11 (one version, end-to-end)

The goal of Phase 1 is to land a complete, runnable implementation on 1.21.11
(the default build target). Phase 2 then mechanically replicates the Java
sources to the other 10 versions. This avoids cross-version mistakes from
parallel multi-version edits.

---

### Task 1: Add 4 block IDs to ModBlockId

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`

- [ ] **Step 1: Read existing Temporal Stone section**

Read lines 83–101 of `ModBlockId.java`. Confirm current end-of-section is
`TEMPORAL_STONE_PRESSURE_PLATE(def("temporal_stone_pressure_plate")),` at
line 101.

- [ ] **Step 2: Append 4 entries to the Temporal Stone section**

Use Edit tool with `old_string`:

```java
    TEMPORAL_STONE_BUTTON(def("temporal_stone_button")),
    TEMPORAL_STONE_PRESSURE_PLATE(def("temporal_stone_pressure_plate")),

    // Temporal Sand / Gravel / Sandstone variants
```

and `new_string`:

```java
    TEMPORAL_STONE_BUTTON(def("temporal_stone_button")),
    TEMPORAL_STONE_PRESSURE_PLATE(def("temporal_stone_pressure_plate")),
    TEMPORAL_STALACTITE_TIP(def("temporal_stalactite_tip")),
    TEMPORAL_STALACTITE_FRUSTUM(def("temporal_stalactite_frustum")),
    TEMPORAL_STALAGMITE_TIP(def("temporal_stalagmite_tip")),
    TEMPORAL_STALAGMITE_FRUSTUM(def("temporal_stalagmite_frustum")),

    // Temporal Sand / Gravel / Sandstone variants
```

- [ ] **Step 3: Verify enum compiles for 1.21.11**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL (only enum changed; no consumers yet so no
references).

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
git commit -m "feat(blocks): add Temporal Stalactite/Stalagmite block IDs"
```

---

### Task 2: Implement 4 block classes for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalactiteTipBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalactiteFrustumBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalagmiteTipBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalagmiteFrustumBlock.java`

Reference: existing `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStoneBlock.java`
uses `Identifier` (renamed from `ResourceLocation` in 1.21.11 only — see
`feedback_util_package_1_21_11.md`-adjacent rename) and `setId(...)`.

Per `feedback_setid_shared_factory_pitfall.md`: each block MUST have its own
`createProperties()` that calls `setId(...)` with the block's own id. Do not
share one factory across the four classes.

Per `feedback_ofFullCopy_hardness_pitfall.md`: chain `.strength(1.5f, 6.0f)`
explicitly even when copying from `Blocks.STONE`.

- [ ] **Step 1: Write `TemporalStalactiteTipBlock.java`**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;

public class TemporalStalactiteTipBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 16.0D, 11.0D);

    public TemporalStalactiteTipBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .strength(1.5f, 6.0f)
                .sound(SoundType.POINTED_DRIPSTONE)
                .noOcclusion()
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stalactite_tip")));
    }
}
```

- [ ] **Step 2: Write `TemporalStalactiteFrustumBlock.java`**

Same template as Step 1 with these substitutions:

- Class name: `TemporalStalactiteFrustumBlock`
- `SHAPE`: `Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D)`
- `setId` path: `"temporal_stalactite_frustum"`

- [ ] **Step 3: Write `TemporalStalagmiteTipBlock.java`**

Same template as Step 1 with:

- Class name: `TemporalStalagmiteTipBlock`
- `SHAPE`: `Block.box(5.0D, 0.0D, 5.0D, 11.0D, 11.0D, 11.0D)` (mirror — narrow rises from floor)
- `setId` path: `"temporal_stalagmite_tip"`

- [ ] **Step 4: Write `TemporalStalagmiteFrustumBlock.java`**

Same template as Step 1 with:

- Class name: `TemporalStalagmiteFrustumBlock`
- `SHAPE`: `Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D)` (same as ceiling-frustum; full height block)
- `setId` path: `"temporal_stalagmite_frustum"`

- [ ] **Step 5: Verify compile for 1.21.11**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalactiteTipBlock.java \
        common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalactiteFrustumBlock.java \
        common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalagmiteTipBlock.java \
        common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalStalagmiteFrustumBlock.java
git commit -m "feat(blocks): add Temporal Stalactite/Stalagmite block classes (1.21.11)"
```

---

### Task 3: Register 4 blocks in 1.21.11 ModBlocks

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java`

- [ ] **Step 1: Add imports**

Use Edit tool. Find:

```java
import com.chronodawn.blocks.TemporalStoneButton;
import com.chronodawn.blocks.TemporalStonePressurePlate;
```

Replace with:

```java
import com.chronodawn.blocks.TemporalStoneButton;
import com.chronodawn.blocks.TemporalStonePressurePlate;
import com.chronodawn.blocks.TemporalStalactiteTipBlock;
import com.chronodawn.blocks.TemporalStalactiteFrustumBlock;
import com.chronodawn.blocks.TemporalStalagmiteTipBlock;
import com.chronodawn.blocks.TemporalStalagmiteFrustumBlock;
```

- [ ] **Step 2: Append registrations after `POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL`**

Find the block of code ending at line 654:

```java
    public static final RegistrySupplier<Block> POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL = BLOCKS.register(
        ModBlockId.POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL.id(),
        () -> new PolishedDeepslateTemporalStoneWall(PolishedDeepslateTemporalStoneWall.createProperties())
    );
```

Append immediately after (before the next blank line + `TEMPORAL_COBBLESTONE_STAIRS`):

```java

    // Temporal Stalactite / Stalagmite (cave decoration)
    public static final RegistrySupplier<Block> TEMPORAL_STALACTITE_TIP = BLOCKS.register(
        ModBlockId.TEMPORAL_STALACTITE_TIP.id(),
        () -> new TemporalStalactiteTipBlock(TemporalStalactiteTipBlock.createProperties())
    );
    public static final RegistrySupplier<Block> TEMPORAL_STALACTITE_FRUSTUM = BLOCKS.register(
        ModBlockId.TEMPORAL_STALACTITE_FRUSTUM.id(),
        () -> new TemporalStalactiteFrustumBlock(TemporalStalactiteFrustumBlock.createProperties())
    );
    public static final RegistrySupplier<Block> TEMPORAL_STALAGMITE_TIP = BLOCKS.register(
        ModBlockId.TEMPORAL_STALAGMITE_TIP.id(),
        () -> new TemporalStalagmiteTipBlock(TemporalStalagmiteTipBlock.createProperties())
    );
    public static final RegistrySupplier<Block> TEMPORAL_STALAGMITE_FRUSTUM = BLOCKS.register(
        ModBlockId.TEMPORAL_STALAGMITE_FRUSTUM.id(),
        () -> new TemporalStalagmiteFrustumBlock(TemporalStalagmiteFrustumBlock.createProperties())
    );
```

- [ ] **Step 3: Verify compile**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java
git commit -m "feat(blocks): register Temporal Stalactite/Stalagmite (1.21.11)"
```

---

### Task 4: Add BlockItems and creative tab entries in 1.21.11 ModItems

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Locate end of Temporal Stone item registrations**

The last polished-deepslate-wall block item is around line 1100; locate it
with `grep -n POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java`.

- [ ] **Step 2: Append four BlockItem registrations after `POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL`**

Mirror the pattern of `TEMPORAL_STONE_BUTTON` (lines 1046–1051). Inserted
block:

```java
    public static final RegistrySupplier<Item> TEMPORAL_STALACTITE_TIP = ITEMS.register(
        ModItemId.TEMPORAL_STALACTITE_TIP.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_STALACTITE_TIP.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_STALACTITE_TIP.id())))));

    public static final RegistrySupplier<Item> TEMPORAL_STALACTITE_FRUSTUM = ITEMS.register(
        ModItemId.TEMPORAL_STALACTITE_FRUSTUM.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_STALACTITE_FRUSTUM.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_STALACTITE_FRUSTUM.id())))));

    public static final RegistrySupplier<Item> TEMPORAL_STALAGMITE_TIP = ITEMS.register(
        ModItemId.TEMPORAL_STALAGMITE_TIP.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_STALAGMITE_TIP.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_STALAGMITE_TIP.id())))));

    public static final RegistrySupplier<Item> TEMPORAL_STALAGMITE_FRUSTUM = ITEMS.register(
        ModItemId.TEMPORAL_STALAGMITE_FRUSTUM.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_STALAGMITE_FRUSTUM.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_STALAGMITE_FRUSTUM.id())))));
```

- [ ] **Step 3: Add ModItemId entries**

Open `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`
and find the Temporal Stone item ID block (mirrors ModBlockId structure).
Append the same 4 IDs in the same position. Use the same pattern as the
existing `TEMPORAL_STONE_BUTTON` / `TEMPORAL_STONE_PRESSURE_PLATE`
neighborhood.

- [ ] **Step 4: Add to creative tab `populateCreativeTab` (line ~2797)**

Find the line:

```java
        output.accept(POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL.get());
```

Append immediately after:

```java
        output.accept(TEMPORAL_STALACTITE_TIP.get());
        output.accept(TEMPORAL_STALACTITE_FRUSTUM.get());
        output.accept(TEMPORAL_STALAGMITE_TIP.get());
        output.accept(TEMPORAL_STALAGMITE_FRUSTUM.get());
```

- [ ] **Step 5: Verify compile**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Run unit tests for 1.21.11 (must include CreativeTabCompletenessTest)**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11`
Expected: PASS. The completeness test should now require shared resources
(blockstates, models, lang) to exist — if it fails on missing resources, that
is expected and confirms the test catches missing wiring; proceed to Task 5
to add resources, then re-run.

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java \
        common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(items): register Temporal Stalactite/Stalagmite items (1.21.11)"
```

---

### Task 5: Create blockstate, model, and texture resources (shared)

**Files:**
- Create: 4 blockstate JSONs, 4 block model JSONs, 4 item model JSONs, 4 textures (under `common/shared/src/main/resources/assets/chronodawn/`)

- [ ] **Step 1: Create blockstate JSONs (single-variant)**

For each block, write `assets/chronodawn/blockstates/<id>.json`:

```json
{
  "variants": {
    "": { "model": "chronodawn:block/<id>" }
  }
}
```

Where `<id>` is one of `temporal_stalactite_tip`,
`temporal_stalactite_frustum`, `temporal_stalagmite_tip`,
`temporal_stalagmite_frustum`.

- [ ] **Step 2: Create block model JSONs**

For each block, write `assets/chronodawn/models/block/<id>.json`. Use a
custom model that matches the VoxelShape silhouette. Reference vanilla
`pointed_dripstone_up_tip` as the structural template.

For `temporal_stalactite_tip` (narrow tip hanging from ceiling):

```json
{
  "parent": "minecraft:block/block",
  "render_type": "minecraft:cutout",
  "textures": {
    "particle": "chronodawn:block/temporal_stalactite_tip",
    "side": "chronodawn:block/temporal_stalactite_tip"
  },
  "elements": [
    {
      "from": [5, 5, 5],
      "to": [11, 16, 11],
      "faces": {
        "down":  { "uv": [5, 5, 11, 11], "texture": "#side" },
        "up":    { "uv": [5, 5, 11, 11], "texture": "#side" },
        "north": { "uv": [5, 0, 11, 11], "texture": "#side" },
        "south": { "uv": [5, 0, 11, 11], "texture": "#side" },
        "west":  { "uv": [5, 0, 11, 11], "texture": "#side" },
        "east":  { "uv": [5, 0, 11, 11], "texture": "#side" }
      }
    }
  ]
}
```

For `temporal_stalactite_frustum` (wider full-height block, ceiling side):

```json
{
  "parent": "minecraft:block/block",
  "render_type": "minecraft:cutout",
  "textures": {
    "particle": "chronodawn:block/temporal_stalactite_frustum",
    "side": "chronodawn:block/temporal_stalactite_frustum"
  },
  "elements": [
    {
      "from": [3, 0, 3],
      "to": [13, 16, 13],
      "faces": {
        "down":  { "uv": [3, 3, 13, 13], "texture": "#side" },
        "up":    { "uv": [3, 3, 13, 13], "texture": "#side" },
        "north": { "uv": [3, 0, 13, 16], "texture": "#side" },
        "south": { "uv": [3, 0, 13, 16], "texture": "#side" },
        "west":  { "uv": [3, 0, 13, 16], "texture": "#side" },
        "east":  { "uv": [3, 0, 13, 16], "texture": "#side" }
      }
    }
  ]
}
```

For `temporal_stalagmite_tip` (narrow tip rising from floor):

```json
{
  "parent": "minecraft:block/block",
  "render_type": "minecraft:cutout",
  "textures": {
    "particle": "chronodawn:block/temporal_stalagmite_tip",
    "side": "chronodawn:block/temporal_stalagmite_tip"
  },
  "elements": [
    {
      "from": [5, 0, 5],
      "to": [11, 11, 11],
      "faces": {
        "down":  { "uv": [5, 5, 11, 11], "texture": "#side" },
        "up":    { "uv": [5, 5, 11, 11], "texture": "#side" },
        "north": { "uv": [5, 5, 11, 16], "texture": "#side" },
        "south": { "uv": [5, 5, 11, 16], "texture": "#side" },
        "west":  { "uv": [5, 5, 11, 16], "texture": "#side" },
        "east":  { "uv": [5, 5, 11, 16], "texture": "#side" }
      }
    }
  ]
}
```

For `temporal_stalagmite_frustum`: same as `temporal_stalactite_frustum`
but with texture refs swapped to `temporal_stalagmite_frustum`.

- [ ] **Step 3: Create item model JSONs**

For each block, write `assets/chronodawn/models/item/<id>.json` referencing
the block model:

```json
{
  "parent": "chronodawn:block/<id>"
}
```

- [ ] **Step 4: Create placeholder textures**

For each of the 4 block textures, generate a 16×16 PNG in the Temporal Stone
palette. Implementation approach:

1. Open vanilla `pointed_dripstone_up_tip.png`,
   `pointed_dripstone_up_frustum.png`,
   `pointed_dripstone_down_tip.png`,
   `pointed_dripstone_down_frustum.png` from the unpacked vanilla assets.
2. Apply a hue/saturation shift toward the Temporal Stone palette
   (sample existing `temporal_stone.png` for the target hue —
   slate-blue with subtle time-crystal glow).
3. Save as the 4 target paths.

The placeholder is acceptable for first commit; texture polish is a deferred
question from the spec. If the implementer cannot run an image editor, mark
this step blocked and stop.

- [ ] **Step 5: Run validateResources**

Run: `./gradlew validateResources`
Expected: PASS — no JSON syntax errors, all texture refs resolved.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_stal*.json \
        common/shared/src/main/resources/assets/chronodawn/models/block/temporal_stal*.json \
        common/shared/src/main/resources/assets/chronodawn/models/item/temporal_stal*.json \
        common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_stal*.png
git commit -m "feat(assets): add Temporal Stalactite/Stalagmite models, blockstates, textures"
```

---

### Task 6: Add lang entries (en_us + ja_jp)

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json`

Per `feedback_lang_json_line_based_edit.md`: use the Edit tool with anchors
to preserve existing blank-line separators between sections. Do NOT round-trip
through `json.dumps` / `json.load` — that strips formatting.

- [ ] **Step 1: Locate the Temporal Stone section in `en_us.json`**

Run: `grep -n "temporal_stone_pressure_plate\|temporal_sand\b" common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`

The 4 new entries belong in the same section as
`block.chronodawn.temporal_stone_*`, after `temporal_stone_pressure_plate`
and before the next block group.

- [ ] **Step 2: Edit `en_us.json` to append four keys**

Use Edit tool. Find:

```json
  "block.chronodawn.temporal_stone_pressure_plate": "Temporal Stone Pressure Plate",
```

(The exact surrounding line — capture the next existing line as anchor too
to make the edit unique.)

Replace with:

```json
  "block.chronodawn.temporal_stone_pressure_plate": "Temporal Stone Pressure Plate",
  "block.chronodawn.temporal_stalactite_tip": "Temporal Stalactite Tip",
  "block.chronodawn.temporal_stalactite_frustum": "Temporal Stalactite Frustum",
  "block.chronodawn.temporal_stalagmite_tip": "Temporal Stalagmite Tip",
  "block.chronodawn.temporal_stalagmite_frustum": "Temporal Stalagmite Frustum",
```

- [ ] **Step 3: Edit `ja_jp.json` to append four keys**

Same approach. Edit anchor:

```json
  "block.chronodawn.temporal_stone_pressure_plate": "時の石の感圧板",
```

(Verify exact translation; use existing string.) Replace with the original
line plus:

```json
  "block.chronodawn.temporal_stalactite_tip": "時の鍾乳石（先端）",
  "block.chronodawn.temporal_stalactite_frustum": "時の鍾乳石（根元）",
  "block.chronodawn.temporal_stalagmite_tip": "時の石筍（先端）",
  "block.chronodawn.temporal_stalagmite_frustum": "時の石筍（根元）",
```

- [ ] **Step 4: Run validateTranslations**

Run: `./gradlew validateTranslations`
Expected: PASS — all 4 keys present in both lang files.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json \
        common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json
git commit -m "feat(lang): add Temporal Stalactite/Stalagmite translations (en, ja)"
```

---

### Task 7: Add tag entries (mineable/pickaxe + needs_stone_tool)

**Files:**
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/needs_stone_tool.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/needs_stone_tool.json`

Note path split per `feedback_loot_table_directory.md` family: 1.20.1 uses
`tags/blocks/` (plural) while 1.21.1+ uses `tags/block/` (singular).

- [ ] **Step 1: Add to `common/shared-1.21.1+/.../pickaxe.json`**

Use Edit tool. Find:

```json
    "chronodawn:dusk_lantern"
  ]
}
```

Replace with:

```json
    "chronodawn:dusk_lantern",
    "chronodawn:temporal_stalactite_tip",
    "chronodawn:temporal_stalactite_frustum",
    "chronodawn:temporal_stalagmite_tip",
    "chronodawn:temporal_stalagmite_frustum"
  ]
}
```

- [ ] **Step 2: Add to `common/shared-1.21.1+/.../needs_stone_tool.json`**

Same approach as Step 1; same 4 entries appended at end.

- [ ] **Step 3: Add to `common/1.20.1/.../pickaxe.json`**

Same approach. Same 4 entries.

- [ ] **Step 4: Add to `common/1.20.1/.../needs_stone_tool.json`**

Same approach. Same 4 entries.

- [ ] **Step 5: Run validateResources**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add common/1.20.1/src/main/resources/data/minecraft/tags/ \
        common/shared-1.21.1+/src/main/resources/data/minecraft/tags/
git commit -m "feat(tags): tag Temporal Stalactite/Stalagmite as pickaxe + stone-tier"
```

---

### Task 8: Add loot tables (1.20.1 plural + 1.21.1+ singular)

**Files:**
- Create: 4 in `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/`
- Create: 4 in `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/`

The 1.20.1 vs 1.21.1+ split is mandatory per
`feedback_loot_table_directory.md` (silent failure otherwise). No silk-touch
branch is needed (per spec), so the loot tables are simpler than
`temporal_stone.json`.

- [ ] **Step 1: Create `common/shared-1.21.1+/.../loot_table/blocks/temporal_stalactite_tip.json`**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_stalactite_tip"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
```

- [ ] **Step 2: Create `common/shared-1.21.1+/.../loot_table/blocks/temporal_stalagmite_tip.json`**

Same as Step 1 with `temporal_stalactite_tip` → `temporal_stalagmite_tip`.

- [ ] **Step 3: Create `common/shared-1.21.1+/.../loot_table/blocks/temporal_stalactite_frustum.json`**

Drops `temporal_stalactite_tip`:

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_stalactite_tip"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
```

- [ ] **Step 4: Create `common/shared-1.21.1+/.../loot_table/blocks/temporal_stalagmite_frustum.json`**

Same as Step 3 with the dropped item swapped to `temporal_stalagmite_tip`.

- [ ] **Step 5: Create the 4 corresponding files in `common/1.20.1/.../loot_tables/blocks/`**

Identical content to Steps 1–4. Path uses `loot_tables/` (plural).

- [ ] **Step 6: Run validateResources**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_stal*.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_stal*.json
git commit -m "feat(loot): add Temporal Stalactite/Stalagmite loot tables"
```

---

### Task 9: Build 1.21.11 and visually smoke-test in client

**Files:**
- None (verification only)

- [ ] **Step 1: Full 1.21.11 build**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL with JARs in `fabric/1.21.11/build/libs/` and
`neoforge/1.21.11/build/libs/`.

- [ ] **Step 2: Run Fabric client and visually inspect**

Run: `./gradlew runClientFabric1_21_11` (foreground; let user run if
implementer cannot launch GUI).

In the client:
1. Open creative inventory → Building Blocks tab → confirm 4 new entries
   appear at end of Temporal Stone family.
2. Place each of the 4 blocks. Verify the silhouettes look as expected
   (tips narrow, frustums wider; correct vertical orientation).
3. Break each tip → confirm self-drop.
4. Break each frustum → confirm matching tip drops.

- [ ] **Step 3: Stop the client**

- [ ] **Step 4: Commit (no file changes; this task is verification)**

No commit needed. Phase 1 ends here. Confirm with user before Phase 2.

---

## Phase 2: Replicate Java sources to all other versions

Each version-replication task is mechanical: copy the four block classes
from 1.21.11, adapt for the version's API era, and replicate the ModBlocks /
ModItems edits.

**API era reference:**

- **Era A (1.20.1, 1.21.1):** `BlockBehaviour.Properties.of()` chain.
  No `setId(...)`. Constructor uses `MapColor`, `.strength(...)`,
  `.requiresCorrectToolForDrops()`, `.sound(SoundType.X)`.
- **Era B (1.21.2, 1.21.4–1.21.10):** `BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)`
  + `setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(...)))`.
  Uses `ResourceLocation`.
- **Era C (1.21.11):** identical to Era B but `ResourceLocation` is renamed to
  `Identifier`. Already implemented in Phase 1.

For Era A blocks, the `createProperties()` body is:

```java
public static BlockBehaviour.Properties createProperties() {
    return BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(1.5f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.POINTED_DRIPSTONE);
}
```

(no `setId`). Imports also differ: no `ChronoDawn`, no `Registries`, no
`ResourceKey`, no `Identifier`/`ResourceLocation`.

For Era B blocks, the `createProperties()` body matches Era C but with
`ResourceLocation.fromNamespaceAndPath(...)` instead of
`Identifier.fromNamespaceAndPath(...)`.

**ModItems / ModBlocks** edits: pattern identical to Phase 1, but the
neighborhood of existing Temporal Stone registrations may differ slightly
between versions. Always copy the structure of the existing
`TEMPORAL_STONE_PRESSURE_PLATE` (or `POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL`)
neighborhood in that version's file.

---

### Task 10: Replicate to 1.21.10 (Era B)

**Files:**
- Create: 4 block classes in `common/1.21.10/src/main/java/com/chronodawn/blocks/`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Read 1.21.10 sibling reference**

Read `common/1.21.10/src/main/java/com/chronodawn/blocks/TemporalStoneBlock.java`
to confirm Era B import set (`ResourceLocation`, not `Identifier`).

- [ ] **Step 2: Copy and adapt the 4 block classes from 1.21.11**

For each of the 4 stalactite/stalagmite class files:
1. Copy the file from `common/1.21.11/.../blocks/` to `common/1.21.10/.../blocks/`.
2. Replace `import net.minecraft.resources.Identifier;` with
   `import net.minecraft.resources.ResourceLocation;`.
3. Replace `Identifier.fromNamespaceAndPath(` with
   `ResourceLocation.fromNamespaceAndPath(`.

- [ ] **Step 3: Replicate ModBlocks edits**

Apply the same imports and registration entries as Phase 1 Task 3, but in
`common/1.21.10/src/main/java/com/chronodawn/registry/ModBlocks.java`.
Find the local `POLISHED_DEEPSLATE_TEMPORAL_STONE_WALL` registration as
the anchor.

- [ ] **Step 4: Replicate ModItems edits**

Apply the same imports, item registrations, and creative tab additions as
Phase 1 Task 4, but in
`common/1.21.10/src/main/java/com/chronodawn/registry/ModItems.java`.

- [ ] **Step 5: Build and test 1.21.10**

Run: `./gradlew :common-1.21.10:test -Ptarget_mc_version=1.21.10`
Expected: PASS.

Run: `./gradlew build1_21_10`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add common/1.21.10/src/main/java/com/chronodawn/
git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.10"
```

---

### Task 11: Replicate to 1.21.9 (Era B)

Same procedure as Task 10. Substitute `1.21.10` → `1.21.9` throughout.

- [ ] **Step 1: Copy + adapt 4 block classes** (Identifier→ResourceLocation if needed; check sibling).
- [ ] **Step 2: Replicate ModBlocks edits.**
- [ ] **Step 3: Replicate ModItems edits.**
- [ ] **Step 4: `./gradlew :common-1.21.9:test -Ptarget_mc_version=1.21.9` and `build1_21_9`.**
- [ ] **Step 5: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.9"`.

---

### Task 12: Replicate to 1.21.8 (Era B)

Same procedure as Task 10, version `1.21.8`.

- [ ] **Step 1: Copy + adapt 4 block classes.**
- [ ] **Step 2: Replicate ModBlocks edits.**
- [ ] **Step 3: Replicate ModItems edits.**
- [ ] **Step 4: `./gradlew :common-1.21.8:test -Ptarget_mc_version=1.21.8` and `build1_21_8`.**
- [ ] **Step 5: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.8"`.

---

### Task 13: Replicate to 1.21.7 (Era B)

Same procedure as Task 10, version `1.21.7`.

- [ ] **Step 1: Copy + adapt 4 block classes.**
- [ ] **Step 2: Replicate ModBlocks edits.**
- [ ] **Step 3: Replicate ModItems edits.**
- [ ] **Step 4: `./gradlew :common-1.21.7:test -Ptarget_mc_version=1.21.7` and `build1_21_7`.**
- [ ] **Step 5: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.7"`.

---

### Task 14: Replicate to 1.21.6 (Era B)

Same procedure as Task 10, version `1.21.6`.

- [ ] **Step 1: Copy + adapt 4 block classes.**
- [ ] **Step 2: Replicate ModBlocks edits.**
- [ ] **Step 3: Replicate ModItems edits.**
- [ ] **Step 4: `./gradlew :common-1.21.6:test -Ptarget_mc_version=1.21.6` and `build1_21_6`.**
- [ ] **Step 5: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.6"`.

---

### Task 15: Replicate to 1.21.5 (Era B)

Same procedure as Task 10, version `1.21.5`.

- [ ] **Step 1: Copy + adapt 4 block classes.**
- [ ] **Step 2: Replicate ModBlocks edits.**
- [ ] **Step 3: Replicate ModItems edits.**
- [ ] **Step 4: `./gradlew :common-1.21.5:test -Ptarget_mc_version=1.21.5` and `build1_21_5`.**
- [ ] **Step 5: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.5"`.

---

### Task 16: Replicate to 1.21.4 (Era B)

Same procedure as Task 10, version `1.21.4`.

- [ ] **Step 1: Copy + adapt 4 block classes.**
- [ ] **Step 2: Replicate ModBlocks edits.**
- [ ] **Step 3: Replicate ModItems edits.**
- [ ] **Step 4: `./gradlew :common-1.21.4:test -Ptarget_mc_version=1.21.4` and `build1_21_4`.**
- [ ] **Step 5: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.4"`.

---

### Task 17: Replicate to 1.21.2 (Era B)

Same procedure as Task 10, version `1.21.2`. Note: 1.21.3 reuses 1.21.2
modules, so no separate replication for 1.21.3.

- [ ] **Step 1: Copy + adapt 4 block classes.**
- [ ] **Step 2: Replicate ModBlocks edits.**
- [ ] **Step 3: Replicate ModItems edits.**
- [ ] **Step 4: `./gradlew :common-1.21.2:test -Ptarget_mc_version=1.21.2` and `build1_21_2`.**
- [ ] **Step 5: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.2"`.

---

### Task 18: Replicate to 1.21.1 (Era A)

**Files:**
- Create: 4 block classes (Era A — different `createProperties()` body)
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Read 1.21.1 sibling reference**

Read `common/1.21.1/src/main/java/com/chronodawn/blocks/TemporalStoneBlock.java`
to confirm Era A pattern.

- [ ] **Step 2: Write 4 block classes (Era A)**

Same VoxelShape constants as Phase 1, but use the Era A `createProperties()`
body shown at the top of Phase 2:

```java
public static BlockBehaviour.Properties createProperties() {
    return BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(1.5f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.POINTED_DRIPSTONE);
}
```

Imports list trims to the minimum (no `ChronoDawn`, `Registries`,
`ResourceKey`, `Identifier`/`ResourceLocation`):

```java
package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
```

The class body (constructor, `getShape`, `getCollisionShape`, `SHAPE`) is
identical to the Era B/C version. Only `createProperties()` differs.

- [ ] **Step 3: Replicate ModBlocks edits**

In Era A, BlockItems are typically registered in ModItems with
`Item.Properties()` rather than `Properties().setId(...)`. Check the existing
1.21.1 `ModItems.java` for the `TEMPORAL_STONE_BUTTON` pattern and copy
verbatim.

- [ ] **Step 4: Replicate ModItems edits**

Mirror the Era A pattern from existing 1.21.1 `TEMPORAL_STONE_PRESSURE_PLATE`
neighborhood. Add the 4 BlockItems and 4 `output.accept(...)` lines in the
creative tab.

- [ ] **Step 5: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1` and `build1_21_1`.**

- [ ] **Step 6: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.21.1"`.

---

### Task 19: Replicate to 1.20.1 (Era A)

Same procedure as Task 18 with version `1.20.1`. The 1.20.1 codebase uses
Yarn-era class quirks rare in this mod's blocks/, but cross-check sibling
`TemporalStoneBlock.java` for any package/import differences.

- [ ] **Step 1: Read 1.20.1 sibling reference.**
- [ ] **Step 2: Copy 1.21.1 block classes; verify identical compile.**
- [ ] **Step 3: Replicate ModBlocks edits.**
- [ ] **Step 4: Replicate ModItems edits.**
- [ ] **Step 5: `./gradlew :common-1.20.1:test -Ptarget_mc_version=1.20.1` and `build1_20_1`.**
- [ ] **Step 6: Commit:** `git commit -m "feat(blocks): port Temporal Stalactite/Stalagmite to 1.20.1"`.

---

## Phase 3: Worldgen

The worldgen JSON itself is shared, but the biome JSON additions are
per-version because the surrounding music/audio formats differ across eras
(see `feedback_biome_music_format_versions.md`). Only the
`features` array is touched in each biome JSON.

---

### Task 20: Create the three configured features

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/temporal_stalactite_cluster.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/temporal_stalagmite_cluster.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/temporal_pointed_simple.json`

- [ ] **Step 1: Write `temporal_stalactite_cluster.json`**

Uses `minecraft:simple_block` placed twice (frustum at top, tip below) via a
small wrapper. The cleanest expression in vanilla worldgen JSON is to use a
custom feature block-pile — but for two stacked decorative blocks, a
`minecraft:block_column` alternative or a pair of `simple_block` placements
inside `minecraft:random_patch` works. Use this:

```json
{
  "type": "minecraft:block_column",
  "config": {
    "direction": "down",
    "allowed_placement": {
      "predicate_type": "minecraft:matching_blocks",
      "blocks": "minecraft:air"
    },
    "prioritize_tip": false,
    "layers": [
      {
        "height": { "type": "minecraft:constant", "value": 1 },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:temporal_stalactite_frustum" }
        }
      },
      {
        "height": { "type": "minecraft:constant", "value": 1 },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:temporal_stalactite_tip" }
        }
      }
    ]
  }
}
```

(Vanilla `minecraft:block_column` is the same feature type used by
`pointed_dripstone`'s cluster generation.)

- [ ] **Step 2: Write `temporal_stalagmite_cluster.json`**

Symmetrical: `direction` is `up`, layers swap so frustum is bottom and tip
is top:

```json
{
  "type": "minecraft:block_column",
  "config": {
    "direction": "up",
    "allowed_placement": {
      "predicate_type": "minecraft:matching_blocks",
      "blocks": "minecraft:air"
    },
    "prioritize_tip": false,
    "layers": [
      {
        "height": { "type": "minecraft:constant", "value": 1 },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:temporal_stalagmite_frustum" }
        }
      },
      {
        "height": { "type": "minecraft:constant", "value": 1 },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:temporal_stalagmite_tip" }
        }
      }
    ]
  }
}
```

- [ ] **Step 3: Write `temporal_pointed_simple.json`**

A single tip placed without orientation (the placed feature wrapping it
will choose ceiling vs floor via separate `environment_scan` modifiers — see
Task 21). For this configured feature, just place a stalactite tip; the
stalagmite single-tip variant is omitted to keep the count down. Use:

```json
{
  "type": "minecraft:simple_block",
  "config": {
    "to_place": {
      "type": "minecraft:simple_state_provider",
      "state": { "Name": "chronodawn:temporal_stalactite_tip" }
    }
  }
}
```

(Single-tip stalagmites are produced by the stalagmite cluster being
truncated when ceiling height is short, so this configured feature only
covers the tip-stalactite case to keep the JSON inventory minimal.)

- [ ] **Step 4: Run validateResources**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/temporal_stal*.json \
        common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/temporal_pointed_simple.json
git commit -m "feat(worldgen): add Temporal Stalactite/Stalagmite configured features"
```

---

### Task 21: Create three placed features (one per configured feature)

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/temporal_stalactite_cluster.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/temporal_stalagmite_cluster.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/temporal_pointed_single.json`

Because `minecraft:random_select` is a configured-feature type rather than a
placed-feature modifier, we cannot wrap all three via random selection at
the placed-feature layer. Instead, create one placed feature per configured
feature and reference all three from each biome's `features[7]` list. The
single-tip placed feature gets a higher count to keep the length-1 : length-2
ratio described in the spec (~ 2 : 1 visually).

- [ ] **Step 1: Create `placed_feature/temporal_stalactite_cluster.json`**

```json
{
  "feature": "chronodawn:temporal_stalactite_cluster",
  "placement": [
    { "type": "minecraft:count", "count": 4 },
    { "type": "minecraft:in_square" },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:uniform",
        "min_inclusive": { "absolute": -32 },
        "max_inclusive": { "absolute": 80 }
      }
    },
    {
      "type": "minecraft:environment_scan",
      "direction_of_search": "up",
      "max_steps": 12,
      "target_condition": {
        "type": "minecraft:solid"
      }
    },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "blocks": ["chronodawn:temporal_stone", "chronodawn:deepslate_temporal_stone"],
        "offset": [0, 1, 0]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 2: Create `placed_feature/temporal_stalagmite_cluster.json`**

Same as Step 1 but `direction_of_search` flipped to `down`, and the
`block_predicate_filter` offset flipped to `[0, -1, 0]`:

```json
{
  "feature": "chronodawn:temporal_stalagmite_cluster",
  "placement": [
    { "type": "minecraft:count", "count": 4 },
    { "type": "minecraft:in_square" },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:uniform",
        "min_inclusive": { "absolute": -32 },
        "max_inclusive": { "absolute": 80 }
      }
    },
    {
      "type": "minecraft:environment_scan",
      "direction_of_search": "down",
      "max_steps": 12,
      "target_condition": {
        "type": "minecraft:solid"
      }
    },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "blocks": ["chronodawn:temporal_stone", "chronodawn:deepslate_temporal_stone"],
        "offset": [0, -1, 0]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 3: Create `placed_feature/temporal_pointed_single.json`**

Single-tip variant; lower density.

```json
{
  "feature": "chronodawn:temporal_pointed_simple",
  "placement": [
    { "type": "minecraft:count", "count": 8 },
    { "type": "minecraft:in_square" },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:uniform",
        "min_inclusive": { "absolute": -32 },
        "max_inclusive": { "absolute": 80 }
      }
    },
    {
      "type": "minecraft:environment_scan",
      "direction_of_search": "up",
      "max_steps": 12,
      "target_condition": {
        "type": "minecraft:solid"
      }
    },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "blocks": ["chronodawn:temporal_stone", "chronodawn:deepslate_temporal_stone"],
        "offset": [0, 1, 0]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 4: Run validateResources**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/temporal_*.json
git commit -m "feat(worldgen): add Temporal Stalactite/Stalagmite placed features"
```

---

### Task 22: Add three placed features to all 1.20.1 ChronoDawn biomes

**Files:**
- Modify (10): `common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_<biome>.json`
  for `<biome>` in {ancient_forest, dark_forest, desert, forest, mountain, ocean, plains, prairies, snowy, swamp}.

- [ ] **Step 1: Inspect one biome to find the `UNDERGROUND_DECORATION` index**

Read `common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_forest.json`.
Locate the `"features"` array; index 7 (zero-based) is `UNDERGROUND_DECORATION`.

- [ ] **Step 2: For each of the 10 biomes, add the three placed features to `features[7]`**

Each biome's `features[7]` array currently contains either `[]` or a list of
existing features. Use the Edit tool to insert these three entries (preserve
existing content):

```
"chronodawn:temporal_stalactite_cluster",
"chronodawn:temporal_stalagmite_cluster",
"chronodawn:temporal_pointed_single"
```

If `features[7]` is `[]`, replace the empty array with:

```json
[
      "chronodawn:temporal_stalactite_cluster",
      "chronodawn:temporal_stalagmite_cluster",
      "chronodawn:temporal_pointed_single"
    ]
```

If `features[7]` already has entries, append the three new IDs after the
last existing entry (mind the trailing comma).

Repeat for all 10 biome files.

- [ ] **Step 3: Run validateResources**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/
git commit -m "feat(worldgen): generate Temporal Stalactites in 1.20.1 ChronoDawn biomes"
```

---

### Task 23: Add to all 1.21.1 ChronoDawn biomes (10 files)

Same procedure as Task 22, in `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/`.

- [ ] **Step 1: Apply to all 10 biome JSONs.**
- [ ] **Step 2: `./gradlew validateResources`.**
- [ ] **Step 3: Commit:** `git commit -m "feat(worldgen): generate Temporal Stalactites in 1.21.1 ChronoDawn biomes"`.

---

### Task 24: Add to all 1.21.2 ChronoDawn biomes (10 files, per-version)

Same procedure as Task 22, in `common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/`.

This is the per-version override that Era B-pre-1.21.4 needs because of the
single-object `music` format. The `features` array shape matches Task 22.

- [ ] **Step 1: Apply to all 10 biome JSONs.**
- [ ] **Step 2: `./gradlew validateResources`.**
- [ ] **Step 3: Commit:** `git commit -m "feat(worldgen): generate Temporal Stalactites in 1.21.2 ChronoDawn biomes"`.

---

### Task 25: Add to all shared-1.21.2+ ChronoDawn biomes (10 files, covers 1.21.4–1.21.10)

Same procedure as Task 22, in `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/`.

- [ ] **Step 1: Apply to all 10 biome JSONs.**
- [ ] **Step 2: `./gradlew validateResources`.**
- [ ] **Step 3: Commit:** `git commit -m "feat(worldgen): generate Temporal Stalactites in shared-1.21.2+ ChronoDawn biomes"`.

---

### Task 26: Add to all 1.21.11 ChronoDawn biomes (10 files)

Same procedure as Task 22, in `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/`.
Includes `chronodawn_prairies` (added back in commit d873fd4e).

- [ ] **Step 1: Apply to all 10 biome JSONs.**
- [ ] **Step 2: `./gradlew validateResources`.**
- [ ] **Step 3: Commit:** `git commit -m "feat(worldgen): generate Temporal Stalactites in 1.21.11 ChronoDawn biomes"`.

---

## Phase 4: Verification

### Task 27: Full multi-version verification

Per `feedback_check_all_before_phase_completion.md`: per-version smoke tests
do NOT catch silent biome JSON regressions. Run `checkAll` before declaring
the feature complete.

- [ ] **Step 1: Run validateResources**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 2: Run validateTranslations**

Run: `./gradlew validateTranslations`
Expected: PASS.

- [ ] **Step 3: Run checkAll**

Run: `./gradlew checkAll`
Expected: PASS — includes cleanAll, validateResources, validateTranslations,
buildAll, testAll, gameTestAll across all 11 versions.

If any failure surfaces:
- Stop. Diagnose. Fix root cause.
- Per CLAUDE.md, NEVER disable failing tests, modify expected values, or
  bypass linters. Ask user if blocked.

---

### Task 28: Manual in-game verification (representative versions)

Pick three versions: 1.20.1 (oldest, Era A), 1.21.4 (mid, Era B), 1.21.11
(newest, Era C). For each:

- [ ] **Step 1: Build and run the Fabric client**

For 1.20.1: `./gradlew runClientFabric1_20_1`
For 1.21.4: `./gradlew runClientFabric1_21_4`
For 1.21.11: `./gradlew runClientFabric1_21_11`

(If the implementer cannot launch a GUI, surface this to the user and
request manual verification.)

- [ ] **Step 2: Enter the ChronoDawn dimension via the existing portal flow**

(Existing dev workflow — refer to `docs/player_guide.md` for portal recipe.)

- [ ] **Step 3: Locate caves under several biomes**

Use creative-mode flight + `/locate biome chronodawn:chronodawn_forest`
followed by `chronodawn_mountain` and `chronodawn_snowy` to sample cave
chunks under at least 3 different biomes.

- [ ] **Step 4: Confirm visual presence of stalactite + stalagmite formations**

In each cave: confirm Temporal Stone-colored pointed blocks attached to the
ceilings (stalactites, length 1 or 2) and floors (stalagmites, length 1 or
2). Density should be noticeable but not crowded — roughly 1–3 instances
per chunk.

- [ ] **Step 5: Confirm break-and-pickup behavior**

Switch to survival mode (`/gamemode survival`). Break each of the 4 block
variants; confirm:

- `temporal_stalactite_tip` → drops `temporal_stalactite_tip`
- `temporal_stalactite_frustum` → drops `temporal_stalactite_tip`
- `temporal_stalagmite_tip` → drops `temporal_stalagmite_tip`
- `temporal_stalagmite_frustum` → drops `temporal_stalagmite_tip`

Confirm pickaxe is required (try with bare hand: no drop expected) and that
wood pickaxe does NOT drop (stone-tier requirement).

- [ ] **Step 6: Confirm vanilla overworld is unchanged**

Use `/locate biome minecraft:dripstone_caves` in the overworld. Verify the
biome still spawns vanilla `pointed_dripstone` formations and that no
ChronoDawn pointed stones leak into vanilla biomes.

- [ ] **Step 7: Stop the client.**

- [ ] **Step 8: No commit (verification only). Report findings to user.**

If any verification step fails, return to the relevant earlier task to fix.
Do not mark the implementation complete.

---

## Out of scope for this plan (deferred / explicit non-goals)

These are noted in the spec and will not be implemented here:

- Pointed-block falling / impale damage (vanilla `pointed_dripstone` behavior)
- Liquid drip growth or cauldron filling
- `thickness` BlockState with neighbor-aware updates
- Generation in vanilla overworld biomes
- Slab / stairs / wall variants of the pointed blocks
- Waterlogging
- Texture polish iterations beyond initial recolor
- Final density tuning beyond the placeholder `count` values (revisit after
  Task 28 in-game observation)
- `SoundType` choice between `POINTED_DRIPSTONE` and `STONE` (defaulted to
  `POINTED_DRIPSTONE`; revisit if it clashes audibly)
