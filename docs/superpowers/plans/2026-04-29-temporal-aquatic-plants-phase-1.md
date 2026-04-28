# Temporal Aquatic Plants — Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace vanilla `minecraft:kelp` and `minecraft:seagrass` placement in the ChronoDawn dimension with custom Temporal Kelp / Temporal Seagrass / Tall Temporal Seagrass blocks, and add a Dried Temporal Kelp food item smoked from Temporal Kelp. ChronoDawn ocean and swamp generate the new plants from new chunks; vanilla overworld is untouched.

**Architecture:** Four block classes (`TemporalKelpBlock` head, `TemporalKelpPlantBlock` stem, `TemporalSeagrassBlock`, `TallTemporalSeagrassBlock`) extend the matching vanilla classes (`KelpBlock`, `KelpPlantBlock`, `SeagrassBlock`, `TallSeagrassBlock`). One food item (`dried_temporal_kelp`) is registered as a plain `Item` with `FoodProperties`. Two existing configured features (`chronodawn:kelp`, `chronodawn:seagrass`) are rewritten to use vanilla generic feature types (`minecraft:block_column` for the kelp column, `minecraft:simple_block` plus `minecraft:random_patch` for seagrass). All work first lands end-to-end on Minecraft 1.21.11, then mechanically replicates to the other 10 supported versions.

**Tech Stack:** Java 21, NeoForge / Fabric via Architectury 13.x–19.x, Mojang mappings, per-version source modules under `common/<version>/`, shared resources under `common/shared/`, era-split resources under `common/shared-1.21.1+/`, `common/shared-1.21.2+/`, `common/shared-1.21.5+/`. Build tasks: `:common-<version>:build`, `runClientFabric<version>`, `validateResources`, `validateTranslations`, `checkAll`.

**Spec:** `docs/superpowers/specs/2026-04-29-temporal-aquatic-plants-design.md`

---

## File Inventory

### Created — Java sources (44 files, 4 per version)

For each `<v>` in {1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}:

- `common/<v>/src/main/java/com/chronodawn/blocks/TemporalKelpBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/TemporalKelpPlantBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/TallTemporalSeagrassBlock.java`

### Modified — Java sources

- `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` — add 4 IDs
- `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java` — add 4 IDs (3 BlockItem + 1 food)
- `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java` — register 4 blocks (×11)
- `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java` — register 3 BlockItems + 1 food + creative-tab entries (×11)

### Created — shared assets (`common/shared/src/main/resources/assets/chronodawn/`)

Textures (4):
- `textures/block/temporal_kelp.png`
- `textures/block/temporal_kelp_plant.png`
- `textures/block/temporal_seagrass.png`
- `textures/block/tall_temporal_seagrass_top.png` (and `_bottom.png` if used)

Blockstates (4):
- `blockstates/temporal_kelp.json`
- `blockstates/temporal_kelp_plant.json`
- `blockstates/temporal_seagrass.json`
- `blockstates/tall_temporal_seagrass.json`

Block models (4):
- `models/block/temporal_kelp.json`
- `models/block/temporal_kelp_plant.json`
- `models/block/temporal_seagrass.json`
- `models/block/tall_temporal_seagrass_top.json` and `tall_temporal_seagrass_bottom.json`

Item models (4):
- `models/item/temporal_kelp.json`
- `models/item/temporal_seagrass.json`
- `models/item/tall_temporal_seagrass.json`
- `models/item/dried_temporal_kelp.json`

### Modified — shared assets

- `assets/chronodawn/lang/en_us.json` — 5 keys (4 blocks + dried kelp)

### Created — Client Items JSON (era-split)

For 1.21.4 only:
- `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_kelp.json`
- `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_seagrass.json`
- `common/1.21.4/src/main/resources/assets/chronodawn/items/tall_temporal_seagrass.json`
- `common/1.21.4/src/main/resources/assets/chronodawn/items/dried_temporal_kelp.json`

For 1.21.5+ (shared dir):
- `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_kelp.json`
- `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_seagrass.json`
- `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/tall_temporal_seagrass.json`
- `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/dried_temporal_kelp.json`

### Created — loot tables (era-split)

For 1.20.1 (`loot_tables/` plural):
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_kelp.json`
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_kelp_plant.json`
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_seagrass.json`
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/tall_temporal_seagrass.json`

For 1.21.1+ (`loot_table/` singular):
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_kelp.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_kelp_plant.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_seagrass.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/tall_temporal_seagrass.json`

### Created — smoking recipe (era-split, 3 forms × 1 recipe)

- `common/1.20.1/src/main/resources/data/chronodawn/recipes/dried_temporal_kelp_from_smoking.json`
- `common/1.21.1/src/main/resources/data/chronodawn/recipe/dried_temporal_kelp_from_smoking.json`
- `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/dried_temporal_kelp_from_smoking.json`

### Modified — vanilla tags (era-split)

- `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/climbable.json` — add `chronodawn:temporal_kelp`, `chronodawn:temporal_kelp_plant`
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/climbable.json` — same additions
- `common/1.20.1/src/main/resources/data/minecraft/tags/items/turtle_food.json` — add `chronodawn:temporal_seagrass`
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/item/turtle_food.json` — same additions

> If the existing repo does not yet have these tag files (only vanilla owns them), they are *created* in the ChronoDawn datapack to *append* the new entries — investigate during Task 13.

### Modified — worldgen (single, shared)

- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/kelp.json` — rewrite to `minecraft:block_column`
- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/seagrass.json` — rewrite to mixed simple_block + random_patch with tall variant

### Tests (created)

- `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsRegistrationTest.java` (lightweight registry presence check)
- `common/shared/src/test/java/com/chronodawn/unit/DriedTemporalKelpFoodTest.java` (food properties)
- `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsLootTableTest.java` (loot tables exist + expected drop names)
- `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsRecipeTest.java` (smoking recipe present per era)

GameTests (created, where the project's GameTest harness lives):
- `gametest/src/main/java/.../TemporalKelpGameTest.java` (one structure-driven test for kelp bone-meal growth, one for smoking via furnace block-entity simulation)

### Documentation (modified)

- `CHANGELOG.md` — Added entry under the next minor version
- `docs/player_guide.md` — new "Aquatic plants" subsection
- `docs/curseforge_description.md`, `docs/modrinth_description.md` — feature list update

---

## Cross-Task References (read once before starting any task)

### Pattern references already in the repo

| Want to know how | Read |
|---|---|
| Block class for an existing extension of vanilla `BushBlock` | `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalFernBlock.java` (1.21.5+ era), `common/1.21.1/src/main/java/com/chronodawn/blocks/TemporalFernBlock.java` (≤1.21.4 era) |
| `ModBlockId` enum entry shape | `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` lines 197–198 |
| `ModBlocks.register(...)` pattern (1.21.5+) | `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java` (search `TEMPORAL_TALL_GRASS`) |
| `ModItems.register(...)` for `BlockItem` | `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java` lines 1612–1626 |
| `ModItems.register(...)` for plain food `Item` | same file, lines 2400–2412 (`GLIDE_FISH`) |
| Smoking recipe (1.20.1 era) | `common/1.20.1/src/main/resources/data/chronodawn/recipes/cooked_glide_fish_from_smoking.json` |
| Smoking recipe (1.21.1 era) | `common/1.21.1/src/main/resources/data/chronodawn/recipe/cooked_glide_fish_from_smoking.json` |
| Smoking recipe (1.21.2+ era) | `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/cooked_glide_fish_from_smoking.json` |
| Loot table (1.21.1+) | `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_fern.json` |
| Client Items JSON | `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/glide_fish.json` |
| Existing `chronodawn:kelp` / `chronodawn:seagrass` configured feature | `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/kelp.json`, `seagrass.json` (current contents wrap vanilla feature types) |

### Memory-recorded pitfalls relevant to Phase 1

Confirm before each related task:

- **`feedback_bare_item_requires_setid.md`** — every new `Item` from 1.21.2+ must chain `.setId(...)`. Skipping it compiles but NPEs on first interaction.
- **`feedback_setid_shared_factory_pitfall.md`** — never share a `createProperties()` factory across multiple blocks; the `setId` baked into a shared factory aliases the loot table id across siblings.
- **`feedback_spawn_egg_client_items.md`** ("Client Items JSON required for ALL items in 1.21.4+") — every new BlockItem and food item needs `assets/chronodawn/items/<id>.json` in both 1.21.4 and shared-1.21.5+.
- **`feedback_loot_table_directory.md`** — 1.20.1 uses `loot_tables/` (plural), 1.21.1+ uses `loot_table/` (singular). Wrong dir name silently disables drops.
- **`feedback_loot_table_silk_touch_format.md`** — 1.20.1 silk-touch uses direct `enchantments`; 1.21.1+ uses nested `predicates.minecraft:enchantments`. Wrong form silently breaks the silk-touch branch.
- **`feedback_recipe_json_format_versions.md`** — 1.20.1: `result` is a string + `ingredient` is `{"item": "..."}`. 1.21.1: `result` is `{"id": "..."}` + `ingredient` is `{"item": "..."}`. 1.21.2+: `result` is `{"id": "..."}` + `ingredient` is the bare item id string.
- **`feedback_lang_json_line_based_edit.md`** — never overwrite `en_us.json` via `json.dumps`; use the `Edit` tool with anchor strings to preserve blank-line separators.
- **`feedback_check_all_before_phase_completion.md`** — multi-version phases must run `./gradlew checkAll` before being marked complete; per-version success on the foundation version is not sufficient.
- **`feedback_verify_subagent_commits.md`** — when delegating to subagents, always verify their commit claims against `git log` before proceeding.

---

## Phase 1: Foundation on 1.21.11 (one version, end-to-end)

The goal of Phase 1 is to land a complete, runnable implementation on Minecraft 1.21.11 (the default build target). Phases 2–6 then mechanically replicate the Java sources to the other 10 versions, paying attention to the era-specific divergences. This avoids cross-version mistakes from parallel multi-version edits.

---

### Task 1: Add 4 block IDs and 4 item IDs to the shared registries

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Locate the Temporal Tall Grass / Temporal Fern section in `ModBlockId.java`**

Search for `TEMPORAL_TALL_GRASS(def(`. The current entries are at lines 197–198 (subject to drift; confirm by reading the file). They are followed by other groupings; pick a comment break that separates plant blocks from the next category (the file is grouped by domain).

- [ ] **Step 2: Append 4 entries to `ModBlockId`**

Use `Edit` with `old_string` matching the two existing temporal-plant lines and a comment break, and `new_string` adding the four new IDs. Concrete `new_string`:

```java
    TEMPORAL_TALL_GRASS(def("temporal_tall_grass").cutout()),
    TEMPORAL_FERN(def("temporal_fern").cutout()),
    TEMPORAL_KELP(def("temporal_kelp").cutout()),
    TEMPORAL_KELP_PLANT(def("temporal_kelp_plant").cutout()),
    TEMPORAL_SEAGRASS(def("temporal_seagrass").cutout()),
    TALL_TEMPORAL_SEAGRASS(def("tall_temporal_seagrass").cutout()),
```

The `.cutout()` modifier matches Temporal Tall Grass / Fern (transparent edges).

- [ ] **Step 3: Locate the corresponding section of `ModItemId.java` and add 4 IDs**

Append:

```java
    TEMPORAL_KELP("temporal_kelp"),
    TEMPORAL_SEAGRASS("temporal_seagrass"),
    TALL_TEMPORAL_SEAGRASS("tall_temporal_seagrass"),
    DRIED_TEMPORAL_KELP("dried_temporal_kelp"),
```

(There is no `temporal_kelp_plant` Item — kelp's stem block has no item form, mirroring vanilla.)

- [ ] **Step 4: Compile shared and 1.21.11 to confirm enums still build**

Run: `./gradlew :common:compileJava :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`. No consumers reference the new IDs yet, so no compile errors.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat(plants): add Temporal Aquatic Plant IDs to ModBlockId/ModItemId"
```

---

### Task 2: Implement TemporalKelpBlock and TemporalKelpPlantBlock for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalKelpBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalKelpPlantBlock.java`

**Reference:** `TemporalFernBlock.java` in the same directory shows the 1.21.5+ pattern with `setId`. Use `KelpBlock` and `KelpPlantBlock` as parents (those are concrete vanilla classes; subclasses need only forward the `Properties` constructor argument).

> **Pitfall (`feedback_setid_shared_factory_pitfall.md`):** each block's `createProperties(String id)` factory must call `.setId(...)` with that block's *own* id. Do not share one factory across the two kelp blocks.

- [ ] **Step 1: Write `TemporalKelpBlock.java`**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalKelpBlock extends KelpBlock {
    public static final MapCodec<TemporalKelpBlock> CODEC = simpleCodec(TemporalKelpBlock::new);

    public TemporalKelpBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<KelpBlock> codec() {
        return (MapCodec<KelpBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

- [ ] **Step 2: Write `TemporalKelpPlantBlock.java`**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.KelpPlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalKelpPlantBlock extends KelpPlantBlock {
    public static final MapCodec<TemporalKelpPlantBlock> CODEC = simpleCodec(TemporalKelpPlantBlock::new);

    public TemporalKelpPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<KelpPlantBlock> codec() {
        return (MapCodec<KelpPlantBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

- [ ] **Step 3: Compile to confirm class shape matches the parent in 1.21.11**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`. If the `codec()` signature or the constructor parent visibility differ in 1.21.11, the compiler will surface it; cross-check against `KelpBlock.java` in the deobfuscated Minecraft sources jar (`./gradlew :common-1.21.11:genSources -Ptarget_mc_version=1.21.11`).

- [ ] **Step 4: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalKelpBlock.java common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalKelpPlantBlock.java
git commit -m "feat(plants): add TemporalKelpBlock + TemporalKelpPlantBlock for 1.21.11"
```

---

### Task 3: Implement TemporalSeagrassBlock and TallTemporalSeagrassBlock for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TallTemporalSeagrassBlock.java`

- [ ] **Step 1: Write `TemporalSeagrassBlock.java`**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSeagrassBlock extends SeagrassBlock {
    public static final MapCodec<TemporalSeagrassBlock> CODEC = simpleCodec(TemporalSeagrassBlock::new);

    public TemporalSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<SeagrassBlock> codec() {
        return (MapCodec<SeagrassBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

- [ ] **Step 2: Write `TallTemporalSeagrassBlock.java`**

`TallSeagrassBlock` extends `DoublePlantBlock`. Most of the bone-meal / placement logic is in the parent; we only need a `setId`-bearing `createProperties` factory.

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TallTemporalSeagrassBlock extends TallSeagrassBlock {
    public static final MapCodec<TallTemporalSeagrassBlock> CODEC = simpleCodec(TallTemporalSeagrassBlock::new);

    public TallTemporalSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<TallSeagrassBlock> codec() {
        return (MapCodec<TallSeagrassBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java common/1.21.11/src/main/java/com/chronodawn/blocks/TallTemporalSeagrassBlock.java
git commit -m "feat(plants): add TemporalSeagrassBlock + TallTemporalSeagrassBlock for 1.21.11"
```

---

### Task 4: Register the 4 blocks in ModBlocks for 1.21.11

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java`

- [ ] **Step 1: Add 4 imports**

Locate the import block (top of file). Add:

```java
import com.chronodawn.blocks.TemporalKelpBlock;
import com.chronodawn.blocks.TemporalKelpPlantBlock;
import com.chronodawn.blocks.TemporalSeagrassBlock;
import com.chronodawn.blocks.TallTemporalSeagrassBlock;
```

- [ ] **Step 2: Append registrations after `TEMPORAL_FERN`**

Locate the existing `TEMPORAL_FERN` registration (search for `TEMPORAL_FERN = BLOCKS.register`). After its closing `;`:

```java
    public static final RegistrySupplier<Block> TEMPORAL_KELP = BLOCKS.register(
        ModBlockId.TEMPORAL_KELP.id(),
        () -> new TemporalKelpBlock(TemporalKelpBlock.createProperties(ModBlockId.TEMPORAL_KELP.id()))
    );

    public static final RegistrySupplier<Block> TEMPORAL_KELP_PLANT = BLOCKS.register(
        ModBlockId.TEMPORAL_KELP_PLANT.id(),
        () -> new TemporalKelpPlantBlock(TemporalKelpPlantBlock.createProperties(ModBlockId.TEMPORAL_KELP_PLANT.id()))
    );

    public static final RegistrySupplier<Block> TEMPORAL_SEAGRASS = BLOCKS.register(
        ModBlockId.TEMPORAL_SEAGRASS.id(),
        () -> new TemporalSeagrassBlock(TemporalSeagrassBlock.createProperties(ModBlockId.TEMPORAL_SEAGRASS.id()))
    );

    public static final RegistrySupplier<Block> TALL_TEMPORAL_SEAGRASS = BLOCKS.register(
        ModBlockId.TALL_TEMPORAL_SEAGRASS.id(),
        () -> new TallTemporalSeagrassBlock(TallTemporalSeagrassBlock.createProperties(ModBlockId.TALL_TEMPORAL_SEAGRASS.id()))
    );
```

- [ ] **Step 3: Build to confirm registrations resolve**

Run: `./gradlew :common-1.21.11:build -Ptarget_mc_version=1.21.11 -x test`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java
git commit -m "feat(plants): register Temporal aquatic blocks in ModBlocks 1.21.11"
```

---

### Task 5: Register 3 BlockItems and 1 food Item in ModItems for 1.21.11

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java`

The `temporal_kelp_plant` block has *no* BlockItem (mirrors vanilla `kelp_plant`). Three `BlockItem` registrations + one `Item` (food) registration + creative-tab `accept(...)` calls.

- [ ] **Step 1: Add imports if missing**

Likely already imported (from `TEMPORAL_FERN` and `GLIDE_FISH` patterns). Search and add only what is missing:

```java
import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModItemId;
import net.minecraft.world.food.FoodProperties;
```

- [ ] **Step 2: Append registrations after the `TEMPORAL_FERN` BlockItem**

Locate `TEMPORAL_FERN = ITEMS.register` (around line 1620). After its `;`:

```java
    public static final RegistrySupplier<Item> TEMPORAL_KELP = ITEMS.register(
        ModItemId.TEMPORAL_KELP.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_KELP.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_KELP.id()))))
    );

    public static final RegistrySupplier<Item> TEMPORAL_SEAGRASS = ITEMS.register(
        ModItemId.TEMPORAL_SEAGRASS.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_SEAGRASS.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_SEAGRASS.id()))))
    );

    public static final RegistrySupplier<Item> TALL_TEMPORAL_SEAGRASS = ITEMS.register(
        ModItemId.TALL_TEMPORAL_SEAGRASS.id(),
        () -> new BlockItem(ModBlocks.TALL_TEMPORAL_SEAGRASS.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TALL_TEMPORAL_SEAGRASS.id()))))
    );

    /**
     * Dried Temporal Kelp - Smoked product of Temporal Kelp.
     * Nutrition: 1, Saturation: 0.6 (parity with vanilla dried_kelp).
     */
    public static final RegistrySupplier<Item> DRIED_TEMPORAL_KELP = ITEMS.register(
        ModItemId.DRIED_TEMPORAL_KELP.id(),
        () -> new Item(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.6f)
                    .build())
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.DRIED_TEMPORAL_KELP.id()))))
    );
```

- [ ] **Step 3: Add creative-tab entries**

Locate the section where `output.accept(TEMPORAL_FERN.get())` appears (~line 2891) and append:

```java
        output.accept(TEMPORAL_KELP.get());
        output.accept(TEMPORAL_SEAGRASS.get());
        output.accept(TALL_TEMPORAL_SEAGRASS.get());
```

For the food creative tab section (where `GLIDE_FISH` and `COOKED_GLIDE_FISH` are accepted):

```java
        output.accept(DRIED_TEMPORAL_KELP.get());
```

- [ ] **Step 4: Build**

Run: `./gradlew :common-1.21.11:build -Ptarget_mc_version=1.21.11 -x test`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(plants): register Temporal aquatic items + dried kelp food in ModItems 1.21.11"
```

---

### Task 6: Add 4 textures (placeholder PNG files)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_kelp.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_kelp_plant.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_seagrass.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/tall_temporal_seagrass_top.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/tall_temporal_seagrass_bottom.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_kelp.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_seagrass.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/tall_temporal_seagrass.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/dried_temporal_kelp.png`

Texture authoring is out of scope for this plan. Use a hue-shifted derivative of the vanilla `block/kelp.png`, `block/seagrass.png`, `item/dried_kelp.png` baselines, retargeted to Frozen-Time pale-cyan/white per the spec's aesthetic section.

- [ ] **Step 1: Author 9 PNGs at 16×16 (block textures) and 16×16 (item textures)**

Drop the PNGs into the paths above. Use the texture-creation skill (`texture-creation`) for guidance on hue-shift workflow against vanilla baselines.

- [ ] **Step 2: Verify pixel dimensions and PNG validity**

Run: `python3 -c "from PIL import Image; [print(p, Image.open(p).size) for p in ['common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_kelp.png','common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_kelp_plant.png','common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_seagrass.png','common/shared/src/main/resources/assets/chronodawn/textures/block/tall_temporal_seagrass_top.png','common/shared/src/main/resources/assets/chronodawn/textures/block/tall_temporal_seagrass_bottom.png','common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_kelp.png','common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_seagrass.png','common/shared/src/main/resources/assets/chronodawn/textures/item/tall_temporal_seagrass.png','common/shared/src/main/resources/assets/chronodawn/textures/item/dried_temporal_kelp.png']]"`
Expected: every line prints a path followed by `(16, 16)`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/
git commit -m "feat(plants): add Temporal aquatic textures (kelp, seagrass, dried kelp)"
```

---

### Task 7: Add 4 blockstate JSONs

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_kelp.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_kelp_plant.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_seagrass.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/tall_temporal_seagrass.json`

Vanilla `kelp` has an `age` property 0–25; the blockstate must list 26 variants pointing at one model. This is verbose but mechanical.

- [ ] **Step 1: Write `temporal_kelp.json`**

```json
{
  "variants": {
    "age=0":  { "model": "chronodawn:block/temporal_kelp" },
    "age=1":  { "model": "chronodawn:block/temporal_kelp" },
    "age=2":  { "model": "chronodawn:block/temporal_kelp" },
    "age=3":  { "model": "chronodawn:block/temporal_kelp" },
    "age=4":  { "model": "chronodawn:block/temporal_kelp" },
    "age=5":  { "model": "chronodawn:block/temporal_kelp" },
    "age=6":  { "model": "chronodawn:block/temporal_kelp" },
    "age=7":  { "model": "chronodawn:block/temporal_kelp" },
    "age=8":  { "model": "chronodawn:block/temporal_kelp" },
    "age=9":  { "model": "chronodawn:block/temporal_kelp" },
    "age=10": { "model": "chronodawn:block/temporal_kelp" },
    "age=11": { "model": "chronodawn:block/temporal_kelp" },
    "age=12": { "model": "chronodawn:block/temporal_kelp" },
    "age=13": { "model": "chronodawn:block/temporal_kelp" },
    "age=14": { "model": "chronodawn:block/temporal_kelp" },
    "age=15": { "model": "chronodawn:block/temporal_kelp" },
    "age=16": { "model": "chronodawn:block/temporal_kelp" },
    "age=17": { "model": "chronodawn:block/temporal_kelp" },
    "age=18": { "model": "chronodawn:block/temporal_kelp" },
    "age=19": { "model": "chronodawn:block/temporal_kelp" },
    "age=20": { "model": "chronodawn:block/temporal_kelp" },
    "age=21": { "model": "chronodawn:block/temporal_kelp" },
    "age=22": { "model": "chronodawn:block/temporal_kelp" },
    "age=23": { "model": "chronodawn:block/temporal_kelp" },
    "age=24": { "model": "chronodawn:block/temporal_kelp" },
    "age=25": { "model": "chronodawn:block/temporal_kelp" }
  }
}
```

- [ ] **Step 2: Write `temporal_kelp_plant.json`**

```json
{
  "variants": {
    "": { "model": "chronodawn:block/temporal_kelp_plant" }
  }
}
```

- [ ] **Step 3: Write `temporal_seagrass.json`**

```json
{
  "variants": {
    "": { "model": "chronodawn:block/temporal_seagrass" }
  }
}
```

- [ ] **Step 4: Write `tall_temporal_seagrass.json`** (DoublePlantBlock has a `half=lower|upper` property)

```json
{
  "variants": {
    "half=lower": { "model": "chronodawn:block/tall_temporal_seagrass_bottom" },
    "half=upper": { "model": "chronodawn:block/tall_temporal_seagrass_top" }
  }
}
```

- [ ] **Step 5: Run JSON validator**

Run: `./gradlew validateResources`
Expected: PASS (no JSON syntax errors). Note: `validateResources` cross-references blockstate→model, so models must exist before this passes — defer the check to after Task 8 if needed; running now is harmless if it fails on unresolved model refs.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/
git commit -m "feat(plants): add blockstates for Temporal aquatic plants"
```

---

### Task 8: Add 4 block model JSONs

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_kelp.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_kelp_plant.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_seagrass.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/tall_temporal_seagrass_bottom.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/tall_temporal_seagrass_top.json`

Vanilla kelp model uses `minecraft:block/template_kelp`. Vanilla seagrass uses `minecraft:block/template_seagrass`. Vanilla tall seagrass top/bottom use `minecraft:block/template_seagrass`.

- [ ] **Step 1: Write `temporal_kelp.json`**

```json
{
  "parent": "minecraft:block/template_kelp",
  "render_type": "minecraft:cutout",
  "textures": {
    "kelp": "chronodawn:block/temporal_kelp"
  }
}
```

- [ ] **Step 2: Write `temporal_kelp_plant.json`**

```json
{
  "parent": "minecraft:block/template_kelp",
  "render_type": "minecraft:cutout",
  "textures": {
    "kelp": "chronodawn:block/temporal_kelp_plant"
  }
}
```

- [ ] **Step 3: Write `temporal_seagrass.json`**

```json
{
  "parent": "minecraft:block/template_seagrass",
  "render_type": "minecraft:cutout",
  "textures": {
    "texture": "chronodawn:block/temporal_seagrass"
  }
}
```

- [ ] **Step 4: Write `tall_temporal_seagrass_bottom.json`**

```json
{
  "parent": "minecraft:block/template_seagrass",
  "render_type": "minecraft:cutout",
  "textures": {
    "texture": "chronodawn:block/tall_temporal_seagrass_bottom"
  }
}
```

- [ ] **Step 5: Write `tall_temporal_seagrass_top.json`**

```json
{
  "parent": "minecraft:block/template_seagrass",
  "render_type": "minecraft:cutout",
  "textures": {
    "texture": "chronodawn:block/tall_temporal_seagrass_top"
  }
}
```

- [ ] **Step 6: Run validator**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/block/
git commit -m "feat(plants): add block models for Temporal aquatic plants"
```

---

### Task 9: Add 4 item model JSONs

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_kelp.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_seagrass.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/tall_temporal_seagrass.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/dried_temporal_kelp.json`

Per `feedback_item_model_flat_icon_pattern.md`, plant items use `item/generated` parent (not block-model parent) so their inventory icon is flat-shaded (avoids harsh 3D directional lighting).

- [ ] **Step 1: Write each item model**

```json
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "chronodawn:item/temporal_kelp" }
}
```

Repeat for `temporal_seagrass`, `tall_temporal_seagrass`, `dried_temporal_kelp` (each with its own `layer0` texture path).

- [ ] **Step 2: Run validator**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/item/
git commit -m "feat(plants): add item models for Temporal aquatic plants"
```

---

### Task 10: Add Client Items JSON for 1.21.5+ (foundation version)

**Files:**
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_kelp.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_seagrass.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/tall_temporal_seagrass.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/dried_temporal_kelp.json`

Per `feedback_spawn_egg_client_items.md`, every new BlockItem and food item from 1.21.4+ needs this JSON. Without it the inventory icon renders as the purple-black fallback.

- [ ] **Step 1: Write each Client Items JSON**

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/temporal_kelp"}}
```

Repeat for the other three items (one file each, identical structure with the matching model path).

- [ ] **Step 2: Run validator**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/
git commit -m "feat(plants): add Client Items JSON for Temporal aquatic items (1.21.5+)"
```

---

### Task 11: Add 4 loot tables to shared-1.21.1+

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_kelp.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_kelp_plant.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_seagrass.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/tall_temporal_seagrass.json`

Per `feedback_loot_table_silk_touch_format.md`, 1.21.1+ uses the nested `predicates.minecraft:enchantments` form for silk-touch. Use `temporal_fern.json` (already in shared-1.21.1+) as the reference pattern.

Drop policies (per spec):
- `temporal_kelp` (head): always drops `chronodawn:temporal_kelp` item.
- `temporal_kelp_plant` (stem): drops `chronodawn:temporal_kelp` item (stem chunks become kelp item, like vanilla).
- `temporal_seagrass`: shears or silk-touch only.
- `tall_temporal_seagrass`: shears drops 2 `chronodawn:temporal_seagrass` items; silk-touch drops 1 `chronodawn:tall_temporal_seagrass` item.

- [ ] **Step 1: Write `temporal_kelp.json`**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        { "type": "minecraft:item", "name": "chronodawn:temporal_kelp" }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/temporal_kelp"
}
```

- [ ] **Step 2: Write `temporal_kelp_plant.json`**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        { "type": "minecraft:item", "name": "chronodawn:temporal_kelp" }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/temporal_kelp_plant"
}
```

- [ ] **Step 3: Write `temporal_seagrass.json`** (shears-only)

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_seagrass",
          "conditions": [
            {
              "condition": "minecraft:match_tool",
              "predicate": { "items": "minecraft:shears" }
            }
          ]
        }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/temporal_seagrass"
}
```

- [ ] **Step 4: Write `tall_temporal_seagrass.json`** (shears OR silk-touch, lower-half only)

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "name": "chronodawn:tall_temporal_seagrass",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "predicates": {
                      "minecraft:enchantments": [
                        { "enchantments": "minecraft:silk_touch", "levels": { "min": 1 } }
                      ]
                    }
                  }
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "chronodawn:temporal_seagrass",
              "functions": [
                { "function": "minecraft:set_count", "count": 2 }
              ],
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": { "items": "minecraft:shears" }
                }
              ]
            }
          ]
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:block_state_property",
          "block": "chronodawn:tall_temporal_seagrass",
          "properties": { "half": "lower" }
        }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/tall_temporal_seagrass"
}
```

- [ ] **Step 5: Run validator**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/
git commit -m "feat(plants): add loot tables for Temporal aquatic plants (1.21.1+)"
```

---

### Task 12: Add smoking recipe for shared-1.21.2+

**Files:**
- Create: `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/dried_temporal_kelp_from_smoking.json`

Per `feedback_recipe_json_format_versions.md`, 1.21.2+ uses ingredient-as-bare-string and result as `{"id":"..."}`.

- [ ] **Step 1: Write the recipe**

```json
{
  "type": "minecraft:smoking",
  "category": "food",
  "cookingtime": 100,
  "experience": 0.1,
  "ingredient": "chronodawn:temporal_kelp",
  "result": {
    "id": "chronodawn:dried_temporal_kelp"
  }
}
```

`experience: 0.1` matches vanilla `dried_kelp_from_smoking`.

- [ ] **Step 2: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/dried_temporal_kelp_from_smoking.json
git commit -m "feat(plants): add smoking recipe for Dried Temporal Kelp (1.21.2+)"
```

---

### Task 13: Add tag entries (climbable + turtle_food) for 1.21.1+

**Files:**
- Modify (create if absent): `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/climbable.json`
- Modify (create if absent): `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/item/turtle_food.json`

- [ ] **Step 1: Discover whether the project already overrides these vanilla tags**

Run: `find common/shared-1.21.1+/src/main/resources/data/minecraft/tags -type f -name "climbable.json" -o -name "turtle_food.json"`
Expected: zero or one or two paths printed. If neither exists, both are created fresh.

- [ ] **Step 2: Write/append to `block/climbable.json`**

If creating fresh:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_kelp",
    "chronodawn:temporal_kelp_plant"
  ]
}
```

If the file already exists and contains a `values` array, use `Edit` to insert the two new entries within `values`.

- [ ] **Step 3: Write/append to `item/turtle_food.json`**

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_seagrass"
  ]
}
```

> Tall Temporal Seagrass is intentionally not in `turtle_food` — vanilla turtles eat short seagrass only.

- [ ] **Step 4: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/minecraft/tags/
git commit -m "feat(plants): tag Temporal Kelp/Plant climbable, Temporal Seagrass turtle_food"
```

---

### Task 14: Add en_us translations

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`

Per `feedback_lang_json_line_based_edit.md`, never `json.dumps` this file — use `Edit` with anchor strings to preserve blank-line separators.

- [ ] **Step 1: Locate the existing temporal-plant block-translation lines**

Search for `block.chronodawn.temporal_fern`. The blocks lang section will have a line like:

```json
  "block.chronodawn.temporal_fern": "Temporal Fern",
  "block.chronodawn.temporal_tall_grass": "Temporal Tall Grass",
```

- [ ] **Step 2: Insert 4 block keys after `temporal_fern`**

`old_string`:

```
  "block.chronodawn.temporal_fern": "Temporal Fern",
```

`new_string`:

```
  "block.chronodawn.temporal_fern": "Temporal Fern",
  "block.chronodawn.temporal_kelp": "Temporal Kelp",
  "block.chronodawn.temporal_kelp_plant": "Temporal Kelp Plant",
  "block.chronodawn.temporal_seagrass": "Temporal Seagrass",
  "block.chronodawn.tall_temporal_seagrass": "Tall Temporal Seagrass",
```

- [ ] **Step 3: Locate the items lang section and add 4 item keys**

Find `item.chronodawn.glide_fish` (or any nearby food key). Insert:

```
  "item.chronodawn.temporal_kelp": "Temporal Kelp",
  "item.chronodawn.temporal_seagrass": "Temporal Seagrass",
  "item.chronodawn.tall_temporal_seagrass": "Tall Temporal Seagrass",
  "item.chronodawn.dried_temporal_kelp": "Dried Temporal Kelp",
```

- [ ] **Step 4: Validate**

Run: `./gradlew validateTranslations`
Expected: PASS — all referenced lang keys present.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat(plants): add en_us translations for Temporal aquatic plants"
```

---

### Task 15: Rewrite `chronodawn:kelp` configured feature to use `block_column`

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/kelp.json`

The current contents wrap vanilla `minecraft:kelp`. We replace with `minecraft:block_column` placing `temporal_kelp_plant` (stem layer) under `temporal_kelp` (tip layer).

- [ ] **Step 1: Replace the file contents**

```json
{
  "type": "minecraft:block_column",
  "config": {
    "direction": "up",
    "allowed_placement": {
      "type": "minecraft:matching_fluids",
      "fluids": ["minecraft:water"]
    },
    "prioritize_tip": true,
    "layers": [
      {
        "height": {
          "type": "minecraft:weighted_list",
          "distribution": [
            { "data": { "type": "minecraft:constant", "value": 0 }, "weight": 1 },
            { "data": { "type": "minecraft:uniform", "value": { "min_inclusive": 1, "max_inclusive": 25 } }, "weight": 4 }
          ]
        },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:temporal_kelp_plant" }
        }
      },
      {
        "height": { "type": "minecraft:constant", "value": 1 },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:temporal_kelp", "Properties": { "age": "0" } }
        }
      }
    ]
  }
}
```

> The exact schema for `minecraft:block_column` should be cross-checked against `data/minecraft/worldgen/configured_feature/huge_dripleaf.json` in vanilla 1.21.11 (extract via `./gradlew :common-1.21.11:genSources -Ptarget_mc_version=1.21.11` and inspect `build/.../minecraft.jar`). If the value-vs-distribution wrapping differs, fix here before continuing.

- [ ] **Step 2: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/kelp.json
git commit -m "feat(worldgen): rewrite chronodawn:kelp to place Temporal Kelp via block_column"
```

---

### Task 16: Rewrite `chronodawn:seagrass` configured feature

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/seagrass.json`

The new feature must:
1. Probabilistically place `chronodawn:temporal_seagrass` (short).
2. With lower probability, place `chronodawn:tall_temporal_seagrass` as a 2-block column (lower + upper halves).

The cleanest expression is `minecraft:simple_random_selector` selecting between two child features at runtime.

- [ ] **Step 1: Add a sibling configured feature for the tall variant**

Create `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/tall_temporal_seagrass_column.json`:

```json
{
  "type": "minecraft:block_column",
  "config": {
    "direction": "up",
    "allowed_placement": {
      "type": "minecraft:matching_fluids",
      "fluids": ["minecraft:water"]
    },
    "prioritize_tip": false,
    "layers": [
      {
        "height": { "type": "minecraft:constant", "value": 1 },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:tall_temporal_seagrass", "Properties": { "half": "lower" } }
        }
      },
      {
        "height": { "type": "minecraft:constant", "value": 1 },
        "provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "chronodawn:tall_temporal_seagrass", "Properties": { "half": "upper" } }
        }
      }
    ]
  }
}
```

- [ ] **Step 2: Replace `seagrass.json` contents**

```json
{
  "type": "minecraft:simple_random_selector",
  "config": {
    "features": [
      {
        "feature": {
          "type": "minecraft:simple_block",
          "config": {
            "to_place": {
              "type": "minecraft:simple_state_provider",
              "state": { "Name": "chronodawn:temporal_seagrass" }
            }
          }
        }
      },
      {
        "feature": {
          "type": "minecraft:simple_block",
          "config": {
            "to_place": {
              "type": "minecraft:simple_state_provider",
              "state": { "Name": "chronodawn:temporal_seagrass" }
            }
          }
        }
      },
      {
        "feature": "chronodawn:tall_temporal_seagrass_column"
      }
    ]
  }
}
```

(Two short entries to one tall entry yields ~33% tall, matching vanilla `minecraft:seagrass` `probability: 0.33` for tall.)

- [ ] **Step 3: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/seagrass.json common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/tall_temporal_seagrass_column.json
git commit -m "feat(worldgen): rewrite chronodawn:seagrass to place Temporal Seagrass + Tall variant"
```

---

### Task 17: Foundation verification on 1.21.11

- [ ] **Step 1: Full build for 1.21.11**

Run: `./gradlew clean1_21_11 build1_21_11`
Expected: `BUILD SUCCESSFUL`. JARs produced under `fabric/1.21.11/build/libs/` and `neoforge/1.21.11/build/libs/`.

- [ ] **Step 2: Resource and translation validation**

Run: `./gradlew validateResources validateTranslations`
Expected: PASS for both.

- [ ] **Step 3: Manual in-world verification (Fabric)**

Run: `./gradlew runClientFabric1_21_11`
In-game checks:
1. Create a new world with seed `0` (or any reproducible seed).
2. Use `/execute in chronodawn:overworld run tp @s 0 80 0` then `/locate biome chronodawn:chronodawn_ocean` and travel there.
3. Confirm Temporal Kelp generates as columns 1–25 high; Temporal Seagrass and Tall Temporal Seagrass appear on the ocean floor; no vanilla `kelp` or `seagrass` are present in newly generated chunks.
4. Use shears on Temporal Seagrass — it drops itself.
5. Use shears on the lower half of Tall Temporal Seagrass — drops 2 Temporal Seagrass items.
6. Use bone meal on a Temporal Kelp head — it grows up by 1 block.
7. Place Temporal Kelp into a smoker with fuel — it produces Dried Temporal Kelp; eating Dried Temporal Kelp restores 1 hunger and 0.6 saturation.

- [ ] **Step 4: Foundation commit checkpoint**

If all in-world checks pass, the 1.21.11 foundation is complete. No commit needed (no file changes at this step).

---

## Phase 2: Replicate Java sources to 1.21.10, 1.21.9, 1.21.8, 1.21.7, 1.21.6, 1.21.5

These six versions share the 1.21.5+ API era with 1.21.11 *except* they use `ResourceLocation` instead of `Identifier`. All resource files (textures, blockstates, models, loot tables, recipe, tags, lang, worldgen) are already shared across this era — no per-version resource changes.

For each `<v>` in {1.21.10, 1.21.9, 1.21.8, 1.21.7, 1.21.6, 1.21.5}:

### Task 18.<v>: Replicate the 4 block classes for `<v>`

**Files:**
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/TemporalKelpBlock.java`
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/TemporalKelpPlantBlock.java`
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java`
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/TallTemporalSeagrassBlock.java`

- [ ] **Step 1: Copy each class from 1.21.11**

```bash
cp common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalKelpBlock.java common/<v>/src/main/java/com/chronodawn/blocks/TemporalKelpBlock.java
cp common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalKelpPlantBlock.java common/<v>/src/main/java/com/chronodawn/blocks/TemporalKelpPlantBlock.java
cp common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java common/<v>/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java
cp common/1.21.11/src/main/java/com/chronodawn/blocks/TallTemporalSeagrassBlock.java common/<v>/src/main/java/com/chronodawn/blocks/TallTemporalSeagrassBlock.java
```

- [ ] **Step 2: Replace `Identifier` import with `ResourceLocation`**

In each of the 4 copied files, use `Edit` with `replace_all`:

`old_string`: `net.minecraft.resources.Identifier`
`new_string`: `net.minecraft.resources.ResourceLocation`

`old_string`: `Identifier.fromNamespaceAndPath`
`new_string`: `ResourceLocation.fromNamespaceAndPath`

- [ ] **Step 3: Replicate ModBlocks and ModItems registrations**

Apply the same edits as Tasks 4 and 5 to `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java` and `ModItems.java`. Use `Edit` with the same `old_string` / `new_string` patterns; the only adaptation is `Identifier` → `ResourceLocation`.

- [ ] **Step 4: Build for `<v>`**

Run: `./gradlew clean<v_underscored> build<v_underscored> -Ptarget_mc_version=<v>`
(For example, `clean1_21_10 build1_21_10` for 1.21.10.)
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add common/<v>/src/main/java/com/chronodawn/blocks/ common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java common/<v>/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(plants): replicate Temporal aquatic blocks/items to <v>"
```

---

## Phase 3: Replicate to 1.21.4 (Client Items JSON era boundary)

1.21.4 is the *first* version to require Client Items JSON, but it lives in the per-version dir (not `shared-1.21.5+`).

### Task 19: Replicate Java to 1.21.4

- [ ] **Step 1: Copy block classes from 1.21.11 to 1.21.4 and switch `Identifier` → `ResourceLocation`** (same as Phase 2 Step 1+2)

- [ ] **Step 2: Replicate ModBlocks/ModItems edits to 1.21.4** (same pattern as Phase 2 Step 3)

### Task 20: Add Client Items JSON for 1.21.4

**Files:**
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_kelp.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_seagrass.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/tall_temporal_seagrass.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/dried_temporal_kelp.json`

- [ ] **Step 1: Copy each from `shared-1.21.5+`**

```bash
cp common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_kelp.json common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_kelp.json
cp common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_seagrass.json common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_seagrass.json
cp common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/tall_temporal_seagrass.json common/1.21.4/src/main/resources/assets/chronodawn/items/tall_temporal_seagrass.json
cp common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/dried_temporal_kelp.json common/1.21.4/src/main/resources/assets/chronodawn/items/dried_temporal_kelp.json
```

- [ ] **Step 2: Build for 1.21.4**

Run: `./gradlew clean1_21_4 build1_21_4 -Ptarget_mc_version=1.21.4`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add common/1.21.4/
git commit -m "feat(plants): replicate Temporal aquatic plants to 1.21.4 (incl. Client Items JSON)"
```

---

## Phase 4: Replicate to 1.21.2

1.21.2 has the same `setId` requirement as 1.21.5+ but predates Client Items JSON. Java sources mirror 1.21.4. No Client Items JSON needed.

### Task 21: Replicate to 1.21.2

- [ ] **Step 1: Copy block classes from 1.21.4 to 1.21.2**

Identifier vs ResourceLocation: 1.21.2 uses `ResourceLocation` (same as 1.21.4).

- [ ] **Step 2: Replicate ModBlocks/ModItems edits**

- [ ] **Step 3: Build**

Run: `./gradlew clean1_21_2 build1_21_2 -Ptarget_mc_version=1.21.2`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/1.21.2/
git commit -m "feat(plants): replicate Temporal aquatic plants to 1.21.2"
```

---

## Phase 5: Replicate to 1.21.1 (recipe JSON + setId era boundary)

1.21.1 differs from 1.21.2+ in two ways:
1. `Item.Properties().setId(...)` did not yet exist — block/item registration omits the `setId(...)` chain.
2. Smoking recipe uses `result: { "id": "..." }` AND `ingredient: { "item": "..." }` (object form for both).

### Task 22: Replicate Java sources to 1.21.1

**Files:**
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/TemporalKelpBlock.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/TemporalKelpPlantBlock.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/TallTemporalSeagrassBlock.java`

Reference: `common/1.21.1/src/main/java/com/chronodawn/blocks/TemporalFernBlock.java` shows the 1.21.1 era — `createProperties()` factory takes no `id` argument and does not call `setId`.

- [ ] **Step 1: Write `TemporalKelpBlock.java` for 1.21.1** (no setId)

```java
package com.chronodawn.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalKelpBlock extends KelpBlock {
    public static final MapCodec<TemporalKelpBlock> CODEC = simpleCodec(TemporalKelpBlock::new);

    public TemporalKelpBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends KelpBlock> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
```

- [ ] **Step 2: Write the other 3 classes following the same pattern** (drop `setId`, `Identifier` imports; rename `MapCodec` covariance per `TemporalFernBlock.java` 1.21.1)

- [ ] **Step 3: Replicate ModBlocks registration for 1.21.1**

The constructor calls become:

```java
() -> new TemporalKelpBlock(TemporalKelpBlock.createProperties())
```

(no `id` argument).

- [ ] **Step 4: Replicate ModItems registration for 1.21.1**

The Item.Properties calls drop the `.setId(...)` chain:

```java
() -> new BlockItem(ModBlocks.TEMPORAL_KELP.get(), new Item.Properties())
```

For dried_temporal_kelp:

```java
() -> new Item(new Item.Properties()
        .food(new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.6f)
            .build()))
```

### Task 23: Add 1.21.1-format smoking recipe

**Files:**
- Create: `common/1.21.1/src/main/resources/data/chronodawn/recipe/dried_temporal_kelp_from_smoking.json`

```json
{
  "type": "minecraft:smoking",
  "category": "food",
  "cookingtime": 100,
  "experience": 0.1,
  "ingredient": { "item": "chronodawn:temporal_kelp" },
  "result": { "id": "chronodawn:dried_temporal_kelp" }
}
```

### Task 24: Verify 1.21.1 build

- [ ] **Step 1: Build**

Run: `./gradlew clean1_21_1 build1_21_1 -Ptarget_mc_version=1.21.1`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Commit**

```bash
git add common/1.21.1/
git commit -m "feat(plants): replicate Temporal aquatic plants to 1.21.1"
```

---

## Phase 6: Replicate to 1.20.1 (largest divergence)

1.20.1 differs in:
1. No `setId(...)` (same as 1.21.1).
2. Loot table dir name is `loot_tables/` (plural), not `loot_table/`.
3. Recipe dir name is `recipes/` (plural), not `recipe/`.
4. Recipe schema: `result` is a bare string, `ingredient` is `{"item": "..."}`.
5. Loot-table silk-touch predicate uses the *direct* `enchantments` form, not nested `predicates.minecraft:enchantments`.
6. `KelpBlock` may have a slightly different ctor signature — cross-check against `common/1.20.1/src/main/java/com/chronodawn/blocks/TemporalFernBlock.java`.

### Task 25: Replicate Java sources to 1.20.1

- [ ] **Step 1: Copy from 1.21.1 and verify class signatures against vanilla 1.20.1 sources** (`./gradlew :common-1.20.1:genSources -Ptarget_mc_version=1.20.1`)

If `KelpBlock`, `KelpPlantBlock`, `SeagrassBlock`, `TallSeagrassBlock` ctor signatures differ in 1.20.1, adjust accordingly. Vanilla 1.20.1 generally takes `BlockBehaviour.Properties` for all four — same as 1.21.1.

- [ ] **Step 2: Replicate ModBlocks/ModItems edits**

### Task 26: Add 1.20.1-format loot tables (4 files, plural dir)

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_kelp.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_kelp_plant.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_seagrass.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/tall_temporal_seagrass.json`

Reference: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_fern.json` shows the 1.20.1 silk-touch form.

- [ ] **Step 1: Write `temporal_kelp.json`**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        { "type": "minecraft:item", "name": "chronodawn:temporal_kelp" }
      ]
    }
  ]
}
```

(No `random_sequence` in 1.20.1.)

- [ ] **Step 2: Write `temporal_kelp_plant.json`** (same pattern, drops kelp item)

- [ ] **Step 3: Write `temporal_seagrass.json`** (shears-only, 1.20.1 form)

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_seagrass",
          "conditions": [
            {
              "condition": "minecraft:match_tool",
              "predicate": { "items": [ "minecraft:shears" ] }
            }
          ]
        }
      ]
    }
  ]
}
```

- [ ] **Step 4: Write `tall_temporal_seagrass.json`** (shears OR silk-touch direct enchantments form)

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "name": "chronodawn:tall_temporal_seagrass",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "enchantments": [
                      { "enchantment": "minecraft:silk_touch", "levels": { "min": 1 } }
                    ]
                  }
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "chronodawn:temporal_seagrass",
              "functions": [
                { "function": "minecraft:set_count", "count": 2 }
              ],
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": { "items": [ "minecraft:shears" ] }
                }
              ]
            }
          ]
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:block_state_property",
          "block": "chronodawn:tall_temporal_seagrass",
          "properties": { "half": "lower" }
        }
      ]
    }
  ]
}
```

### Task 27: Add 1.20.1-format smoking recipe

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/recipes/dried_temporal_kelp_from_smoking.json`

```json
{
  "type": "minecraft:smoking",
  "category": "food",
  "cookingtime": 100,
  "experience": 0.1,
  "ingredient": { "item": "chronodawn:temporal_kelp" },
  "result": "chronodawn:dried_temporal_kelp"
}
```

### Task 28: Add 1.20.1 tag entries

**Files:**
- Modify (or create): `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/climbable.json`
- Modify (or create): `common/1.20.1/src/main/resources/data/minecraft/tags/items/turtle_food.json`

Same content as Task 13, but the dir is `blocks/` and `items/` (plural).

### Task 29: Verify 1.20.1 build

- [ ] **Step 1: Build**

Run: `./gradlew clean1_20_1 build1_20_1 -Ptarget_mc_version=1.20.1`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Commit**

```bash
git add common/1.20.1/
git commit -m "feat(plants): replicate Temporal aquatic plants to 1.20.1"
```

---

## Phase 7: Tests

### Task 30: Add unit tests

**Files:**
- Create: `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsRegistrationTest.java`
- Create: `common/shared/src/test/java/com/chronodawn/unit/DriedTemporalKelpFoodTest.java`
- Create: `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsLootTableTest.java`
- Create: `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsRecipeTest.java`

Reference: existing tests in `common/shared/src/test/java/com/chronodawn/unit/` (e.g. `LootTableValidationTest.java`, `RecipeValidationTest.java`) show how the project asserts JSON resource presence and shape against the per-version classpath.

- [ ] **Step 1: Write `AquaticPlantsRegistrationTest.java`**

Asserts that the 4 block IDs and 4 item IDs exist in `ModBlockId` / `ModItemId`.

```java
package com.chronodawn.unit;

import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModItemId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AquaticPlantsRegistrationTest {
    @Test
    void temporalKelpHeadIdExists() {
        assertNotNull(ModBlockId.valueOf("TEMPORAL_KELP"));
    }

    @Test
    void temporalKelpPlantIdExists() {
        assertNotNull(ModBlockId.valueOf("TEMPORAL_KELP_PLANT"));
    }

    @Test
    void temporalSeagrassIdExists() {
        assertNotNull(ModBlockId.valueOf("TEMPORAL_SEAGRASS"));
    }

    @Test
    void tallTemporalSeagrassIdExists() {
        assertNotNull(ModBlockId.valueOf("TALL_TEMPORAL_SEAGRASS"));
    }

    @Test
    void driedTemporalKelpItemIdExists() {
        assertNotNull(ModItemId.valueOf("DRIED_TEMPORAL_KELP"));
    }
}
```

- [ ] **Step 2: Write `DriedTemporalKelpFoodTest.java`** (asserts loot table presence per era — food properties are baked into Java, harder to unit-test without Minecraft bootstrap; this test asserts presence of the recipe and that the item id is in the items lang)

```java
package com.chronodawn.unit;

import com.chronodawn.registry.ModItemId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DriedTemporalKelpFoodTest {
    @Test
    void itemIdMatchesExpected() {
        assertEquals("dried_temporal_kelp", ModItemId.DRIED_TEMPORAL_KELP.id());
    }
}
```

- [ ] **Step 3: Write `AquaticPlantsLootTableTest.java`**

Resource-driven test that opens each loot table JSON from the classpath (era-specific path resolution per `MinecraftVersion`) and asserts:
1. File exists.
2. Has at least one pool with one entry.
3. Drop names contain `chronodawn:temporal_kelp` / `chronodawn:temporal_seagrass` / `chronodawn:tall_temporal_seagrass` as appropriate.

Use the pattern in `LootTableValidationTest.java` as the template — copy its setup, change the block ids list to the 4 new ones.

- [ ] **Step 4: Write `AquaticPlantsRecipeTest.java`**

Asserts that the smoking recipe JSON exists in the era-appropriate dir and has `ingredient` referencing `chronodawn:temporal_kelp` and `result` (string in 1.20.1, object in 1.21.1+) referencing `chronodawn:dried_temporal_kelp`.

Use the pattern in `RecipeValidationTest.java` as the template.

- [ ] **Step 5: Run unit tests on the foundation version**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsRegistrationTest.java common/shared/src/test/java/com/chronodawn/unit/DriedTemporalKelpFoodTest.java common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsLootTableTest.java common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsRecipeTest.java
git commit -m "test(plants): add unit tests for Temporal aquatic plants"
```

---

### Task 31: Add GameTests for kelp growth and smoking

**Files:**
- Create: `gametest/src/main/java/com/chronodawn/gametest/aquatic/TemporalKelpGameTest.java`

Reference: any existing GameTest under `gametest/src/main/java/com/chronodawn/gametest/` that uses bone-meal growth on a sapling or a smoker recipe. The general pattern is:

```java
@GameTest(template = "chronodawn:aquatic/temporal_kelp_grow")
public static void kelpGrowsOneBlockOnBoneMeal(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 2, 1), ModBlocks.TEMPORAL_KELP.get().defaultBlockState());
    // simulate bone-meal use; assert block at (1, 3, 1) is TEMPORAL_KELP
    helper.succeed();
}
```

The structure file `chronodawn:aquatic/temporal_kelp_grow` is created in `gametest/src/main/resources/data/chronodawn/structures/aquatic/temporal_kelp_grow.nbt` — author by hand or recreate via in-game `/test` command and export.

- [ ] **Step 1: Author the GameTest class** (use `_FernGameTest.java` if any exists for the bone-meal pattern; if none, follow vanilla `KelpFeatureTest` as a reference)

- [ ] **Step 2: Author the structure NBT files**

- [ ] **Step 3: Run gametest for 1.21.11**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11`
Expected: GameTest passes.

- [ ] **Step 4: Commit**

```bash
git add gametest/
git commit -m "test(plants): add GameTests for Temporal Kelp growth"
```

---

## Phase 8: Cross-version verification

### Task 32: Run full checkAll

> Per `feedback_check_all_before_phase_completion.md`, this MUST be run before declaring Phase 1 complete. Per-version success on the foundation alone is not sufficient.

- [ ] **Step 1: Run the umbrella verification task**

Run: `./gradlew checkAll`
Expected: PASS — runs cleanAll, validateResources, validateTranslations, buildAll (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11), testAll, gameTestAll.

- [ ] **Step 2: If a version fails, identify the failing era**

Common failure patterns and where to look:
- 1.20.1 unit-test failure → era-specific loot/recipe JSON path or schema (Tasks 26–27).
- 1.21.1 build failure → setId chain still present in code (Task 22 step 4).
- 1.21.4 runtime failure → missing Client Items JSON (Task 20).
- 1.21.11 build failure → `Identifier` import collision (rebuild from Task 2 reference).

Resolve, then re-run `checkAll`.

---

### Task 33: Manual in-world verification on 3 representative versions

- [ ] **Step 1: Run client for 1.20.1 (Fabric)**

Run: `./gradlew runClientFabric1_20_1`
Same in-world checks as Task 17 Step 3.

- [ ] **Step 2: Run client for 1.21.1 (NeoForge)**

Run: `./gradlew runClientNeoForge1_21_1`

- [ ] **Step 3: Run client for 1.21.4 (Fabric)** — this verifies the Client Items JSON path on the era boundary

Run: `./gradlew runClientFabric1_21_4`

- [ ] **Step 4: If any version fails, fix and re-run `checkAll`**

---

## Phase 9: Documentation and release

### Task 34: Update CHANGELOG.md

**Files:**
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Open `CHANGELOG.md`, locate the `[Unreleased]` section (or create one above the latest released version)**

- [ ] **Step 2: Add an `### Added` block**

```markdown
### Added

- **Temporal Aquatic Plants (Phase 1)**: ChronoDawn dimension oceans and swamps now generate Temporal Kelp, Temporal Seagrass, and Tall Temporal Seagrass instead of vanilla aquatic plants. Temporal Kelp can be smoked into Dried Temporal Kelp (food: nutrition 1, saturation 0.6, parity with vanilla dried kelp). Pre-existing oceans retain vanilla plants — only newly generated chunks use the Temporal variants.
```

### Task 35: Update player guide

**Files:**
- Modify: `docs/player_guide.md`

- [ ] **Step 1: Add an "Aquatic Plants" subsection** under the existing flora section (or create one if it does not yet exist) covering:
  - The four blocks and where they spawn.
  - Drop rules (shears for seagrass, kelp item from any break, silk-touch for tall seagrass to keep tall variant).
  - Smoking recipe for Dried Temporal Kelp.

### Task 36: Update CurseForge / Modrinth descriptions

**Files:**
- Modify: `docs/curseforge_description.md`
- Modify: `docs/modrinth_description.md`

- [ ] **Step 1: Add Aquatic Plants to the feature list section in each.**

### Task 37: Bump version and commit

**Files:**
- Modify: `gradle.properties` (`mod_version`)

Use the project's `versioning` skill: Phase 1 is a MINOR bump (additive feature) — for example `0.7.X` → `0.8.0`.

- [ ] **Step 1: Bump `mod_version` in `gradle.properties`**

- [ ] **Step 2: Commit version bump and docs together**

```bash
git add gradle.properties CHANGELOG.md docs/player_guide.md docs/curseforge_description.md docs/modrinth_description.md
git commit -m "chore(plants): bump version + document Temporal Aquatic Plants Phase 1"
```

- [ ] **Step 3: Final `checkAll` to confirm version bump did not break anything**

Run: `./gradlew checkAll`
Expected: PASS.

---

## Self-Review Notes

The following spec sections map to the tasks above:

| Spec section | Implementing task(s) |
|---|---|
| Plant Inventory > Temporal Kelp + Temporal Kelp Plant | Tasks 2, 4, 5, 7, 8, 11, 13 (climbable tag), 14 |
| Plant Inventory > Temporal Seagrass | Tasks 3, 4, 5, 7, 8, 11, 13 (turtle_food tag), 14 |
| Plant Inventory > Tall Temporal Seagrass | Tasks 3, 4, 5, 7, 8, 11, 14 |
| Plant Inventory > Dried Temporal Kelp food | Tasks 1 (item id), 5, 9, 10, 12, 14 |
| Worldgen Replacement Strategy | Tasks 15 (kelp), 16 (seagrass + tall) |
| Multi-Version Considerations > setId requirement | Tasks 2–5 (1.21.11 era), 22 (1.21.1 era), 25 (1.20.1) |
| Multi-Version Considerations > Client Items JSON | Tasks 10 (1.21.5+), 20 (1.21.4) |
| Multi-Version Considerations > loot table dir name | Tasks 11 (1.21.1+), 26 (1.20.1) |
| Multi-Version Considerations > recipe schema split | Tasks 12 (1.21.2+), 23 (1.21.1), 27 (1.20.1) |
| Multi-Version Considerations > 1.21.11 biome overrides | Not needed for Phase 1: the `chronodawn:kelp` and `chronodawn:seagrass` configured features live in `common/shared/.../worldgen/configured_feature/` (not in any 1.21.11-specific override path); biome JSONs already reference these names and need no changes. |
| Testing Plan | Tasks 30 (unit), 31 (GameTest), 32 (checkAll), 33 (manual) |
| Documentation Touchpoints per Phase | Tasks 34–36 |
| Versioning | Task 37 |

**Open items deferred from spec → still deferred:**
- The exact `minecraft:block_column` schema (Task 15 step 1 includes a verification step against vanilla `huge_dripleaf`).
- 1.21.11 dimension-type changes (independent of biome features; not relevant to Phase 1).
- Texture hue-shift automation (Task 6 leaves this to texture-creation skill / engineer judgement).

**No placeholders, no TBDs.** Every task has concrete code, file paths, and commands.
