# Temporal Aquatic Plants — Phase 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the "Light and color" tier of ChronoDawn aquatic decorations: 1 Lumen Polyp light source + 16 Chrono Coral blocks (4 colors × 4 forms), plus the worldgen wiring that makes them generate in `chronodawn:chronodawn_ocean`. Vanilla overworld is untouched.

**Architecture:** `LumenPolypBlock extends SeaPickleBlock` (vanilla light/bone-meal/waterlog parity). Four shared Chrono Coral form classes (`ChronoCoralBlock`, `ChronoCoralPlantBlock`, `ChronoCoralFanBlock`, `ChronoCoralWallFanBlock`) extending the matching vanilla classes; each form's `randomTick` is overridden to no-op so the coral never dies out of water (Frozen Time fiction). The 16 Chrono Coral blocks share these 4 form classes — color is a per-registration string id passed into `createProperties(String id)` (Phase 1 pattern), not a per-color subclass. All work first lands end-to-end on Minecraft 1.21.11; 1.21.10 → 1.20.1 follow with mechanical replication paying attention to era boundaries (`Identifier`↔`ResourceLocation`, `noCollision`↔`noCollission`, `MapCodec` 1.21+ only, `setId` 1.21.2+ only, `loot_table/`↔`loot_tables/` 1.21.1 boundary, Client Items JSON 1.21.4+).

**Tech Stack:** Java 21, NeoForge / Fabric via Architectury 13.x–19.x, Mojang mappings, per-version source modules under `common/<version>/`, shared resources under `common/shared/`, era-split resources under `common/shared-1.21.1+/`, `common/shared-1.21.2+/`, `common/shared-1.21.5+/`. Build tasks: `:common-<version>:build`, `runClientFabric<version>`, `validateResources`, `validateTranslations`, `checkAll`.

**Spec:** `docs/superpowers/specs/2026-04-29-temporal-aquatic-plants-design.md`

**Phase 1 reference plan:** `docs/superpowers/plans/2026-04-29-temporal-aquatic-plants-phase-1.md`

---

## Sub-phase Overview

The Phase 2 work is broken into 11 sub-phases, ordered by dependency. **Sub-phase A is the recommended Phase-2 entry point** — it ships Lumen Polyp end-to-end on 1.21.11 as the smallest demonstration of the SeaPickleBlock-based pattern, and is independently mergeable.

| Sub-phase | Scope | Versions touched | Tasks |
|---|---|---|---|
| A | Lumen Polyp foundation on 1.21.11 | 1.21.11 only | 1–11 |
| B | Lumen Polyp worldgen on 1.21.11 | 1.21.11 only | 12–14 |
| C | Chrono Coral form classes + 1 color (Dawn) on 1.21.11 | 1.21.11 only | 15–25 |
| D | Chrono Coral 3 remaining colors (Dusk, Twilight, Eternal) on 1.21.11 | 1.21.11 only | 26–32 |
| E | Chrono Coral worldgen on 1.21.11 | 1.21.11 only | 33–34 |
| F | Replicate Java to 1.21.10 → 1.21.5 (`Identifier`→`ResourceLocation`; `noCollision` boundary at 1.21.9) | 1.21.10–1.21.5 | 35 |
| G | Replicate Java to 1.21.4 (incl. Client Items JSON era boundary) | 1.21.4 | 36 |
| H | Replicate Java + biome refs to 1.21.2 | 1.21.2 | 37 |
| I | Replicate Java to 1.21.1 (drop `setId`, drop `Identifier` import); biome refs | 1.21.1 | 38 |
| J | Replicate to 1.20.1 (drop `MapCodec`/`codec()`; recipe schema; loot dir plural) — large divergence | 1.20.1 | 39 |
| K | Tests + cross-version verification + docs + version bump | all | 40–44 |

**Cross-version replication strategy:** Sub-phases A–E land all 17 blocks end-to-end on 1.21.11 (the default build target). F–J then mechanically copy Java sources per era. The shared resources (textures, blockstates, models, lang, loot tables in `shared-1.21.1+/`, recipe in `shared-1.21.2+/`) are authored once during A–E and need no per-version duplication.

---

## File Inventory

### Created — Java sources

For **each** `<v>` in {1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}:

- `common/<v>/src/main/java/com/chronodawn/blocks/LumenPolypBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralPlantBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralFanBlock.java`
- `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralWallFanBlock.java`

Total: 5 classes × 11 versions = 55 Java files.

### Modified — Java sources

- `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` — add 17 IDs
- `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java` — add 13 IDs (Lumen Polyp + 12 BlockItems for Chrono Coral block / plant / fan; wall_fan reuses fan item, vanilla pattern)
- `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java` — register 17 blocks (×11)
- `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java` — register 13 BlockItems + creative-tab entries (×11)

### Created — shared assets (`common/shared/src/main/resources/assets/chronodawn/`)

Block textures (17):
- `textures/block/lumen_polyp.png`
- `textures/block/{dawn,dusk,twilight,eternal}_chrono_coral_block.png` (4)
- `textures/block/{dawn,dusk,twilight,eternal}_chrono_coral.png` (4 — plant texture, also used for fan/wall_fan)

Item textures (13):
- `textures/item/lumen_polyp.png`
- `textures/item/{dawn,dusk,twilight,eternal}_chrono_coral_block.png` (4)
- `textures/item/{dawn,dusk,twilight,eternal}_chrono_coral.png` (4 — plant)
- `textures/item/{dawn,dusk,twilight,eternal}_chrono_coral_fan.png` (4 — fan)

Blockstates (17):
- `blockstates/lumen_polyp.json`
- `blockstates/{COLOR}_chrono_coral_block.json` (4)
- `blockstates/{COLOR}_chrono_coral.json` (4)
- `blockstates/{COLOR}_chrono_coral_fan.json` (4)
- `blockstates/{COLOR}_chrono_coral_wall_fan.json` (4)

Block models — 17 base + 4 lumen polyp pickle counts = 21:
- `models/block/lumen_polyp_1.json`, `lumen_polyp_2.json`, `lumen_polyp_3.json`, `lumen_polyp_4.json`
- `models/block/{COLOR}_chrono_coral_block.json` (4)
- `models/block/{COLOR}_chrono_coral.json` (4)
- `models/block/{COLOR}_chrono_coral_fan.json` (4)
- `models/block/{COLOR}_chrono_coral_wall_fan.json` (4)

Item models (13):
- `models/item/lumen_polyp.json`
- `models/item/{COLOR}_chrono_coral_block.json` (4)
- `models/item/{COLOR}_chrono_coral.json` (4)
- `models/item/{COLOR}_chrono_coral_fan.json` (4)

### Modified — shared assets

- `assets/chronodawn/lang/en_us.json` — 17 block keys (lumen_polyp + 16 corals incl. wall_fan keys)

### Created — Client Items JSON (era-split, 13 entries each)

For 1.21.4 only:
- `common/1.21.4/src/main/resources/assets/chronodawn/items/lumen_polyp.json`
- `common/1.21.4/src/main/resources/assets/chronodawn/items/{COLOR}_chrono_coral_block.json` (4)
- `common/1.21.4/src/main/resources/assets/chronodawn/items/{COLOR}_chrono_coral.json` (4)
- `common/1.21.4/src/main/resources/assets/chronodawn/items/{COLOR}_chrono_coral_fan.json` (4)

For 1.21.5+ (shared dir): same 13 paths under `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/`.

### Created — loot tables

For 1.21.1+ (`loot_table/` singular):
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/lumen_polyp.json`
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/{COLOR}_chrono_coral_block.json` (4)
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/{COLOR}_chrono_coral.json` (4 — silk-touch only, fan-fall pattern)
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/{COLOR}_chrono_coral_fan.json` (4 — silk-touch only)
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/{COLOR}_chrono_coral_wall_fan.json` (4 — silk-touch only, drops fan)

For 1.20.1 (`loot_tables/` plural): same 17 paths under `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/`.

### Created — worldgen (era-split per Phase 1 precedent)

For 1.21.1+ (`shared-1.21.1+`):
- `data/chronodawn/worldgen/configured_feature/lumen_polyp.json`
- `data/chronodawn/worldgen/configured_feature/chrono_coral_patch.json`
- `data/chronodawn/worldgen/placed_feature/lumen_polyp.json`
- `data/chronodawn/worldgen/placed_feature/chrono_coral_patch.json`

For 1.20.1 (`common/1.20.1/`): same 4 files under `data/chronodawn/worldgen/{configured,placed}_feature/`. The 1.20.1 schema may differ for the IntProvider/state provider keys (per `feedback_intprovider_uniform_flat_keys.md` and `feedback_matching_fluids_1_20_5_only.md`).

### Modified — biome JSONs (5 locations per Phase 1 precedent)

Each of the following has its `features` array (typically index 4 / `vegetal_decoration`) extended with two new placed-feature references (`chronodawn:lumen_polyp`, `chronodawn:chrono_coral_patch`):

- `common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`
- `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`
- `common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`
- `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`
- `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`

### Tests (created)

- `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsPhase2RegistrationTest.java` — registry presence check for 17 IDs
- `common/shared/src/test/java/com/chronodawn/unit/ChronoCoralLootTableTest.java` — silk-touch loot table presence per era

GameTest:
- `gametest/src/main/java/com/chronodawn/gametest/aquatic/LumenPolypGameTest.java` — bone-meal grows count from 1 → 2

### Documentation (modified)

- `CHANGELOG.md` — Phase 2 entry under Unreleased
- `docs/player_guide.md` — extend Aquatic Plants section
- `docs/curseforge_description.md`, `docs/modrinth_description.md` — feature list update
- `gradle.properties` — `mod_version` MINOR bump

---

## Cross-Task References (read once before starting any task)

### Pattern references already in the repo

| Want to know how | Read |
|---|---|
| Block class extending vanilla SeagrassBlock with `setId(String id)` factory | `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java` |
| Same class without `setId` (1.21.1 era) | `common/1.21.1/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java` |
| Same class without `MapCodec` (1.20.1 era) | `common/1.20.1/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java` |
| 1.21.11 `BlockBehaviour.Properties` chain with `noCollision()` (single-s) | `TemporalSeagrassBlock.java` (1.21.11) |
| 1.21.5 era — same chain but `noCollission()` (double-s) | `common/1.21.5/src/main/java/com/chronodawn/blocks/TemporalSeagrassBlock.java` |
| `ModBlockId` plant-section additions | `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` lines 197–200 |
| `ModBlocks.register(...)` for plant blocks | `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java` lines 946–965 |
| `ModItems.register(...)` for `BlockItem` | `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java` (search `TEMPORAL_SEAGRASS = ITEMS.register`) |
| Existing `chronodawn:kelp` worldgen JSON | `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/kelp.json` |
| Existing `chronodawn:seagrass` worldgen JSON | `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/seagrass.json` |
| Biome features-array example | `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json` |
| Color-variant block (2 colors) | `common/1.21.11/src/main/java/com/chronodawn/blocks/DawnBellBlock.java` and `DuskBellBlock.java` |

### Memory-recorded pitfalls relevant to Phase 2

Confirm before each related task:

- **`feedback_setid_shared_factory_pitfall.md`** — never share one `createProperties()` factory across the 4 Dawn/Dusk/Twilight/Eternal coral siblings. Use `createProperties(String id)` taking the per-color id (Phase 1 `TemporalSeagrassBlock` pattern), or one factory call per color.
- **`feedback_bare_item_requires_setid.md`** — every new `Item` from 1.21.2+ must chain `.setId(...)`. Skipping it compiles but NPEs on first interaction.
- **`feedback_spawn_egg_client_items.md`** — every new BlockItem from 1.21.4+ needs `assets/chronodawn/items/<id>.json` in both 1.21.4 and shared-1.21.5+. With 13 BlockItems, this is the most likely place for omissions.
- **`feedback_loot_table_directory.md`** — 1.20.1 uses `loot_tables/` (plural), 1.21.1+ uses `loot_table/` (singular). Wrong dir name silently disables drops.
- **`feedback_loot_table_silk_touch_format.md`** — 1.20.1 silk-touch uses direct `enchantments`; 1.21.1+ uses nested `predicates.minecraft:enchantments`. All 16 Chrono Coral loot tables hit this split.
- **`feedback_lang_json_line_based_edit.md`** — never overwrite `en_us.json` via `json.dumps`; use `Edit` with anchors.
- **`feedback_check_all_before_phase_completion.md`** — run `./gradlew checkAll` before declaring Phase 2 complete.
- **`feedback_resourcelocation_identifier_rename.md`** — 1.21.11 only uses `Identifier`; 1.21.10 and below use `ResourceLocation`. Bulk-replicating Java sources without flipping the import is a silent build break for 1.21.2–1.21.10.
- **`feedback_no_collision_spelling_split.md`** — 1.21.9+ uses `noCollision` (single-s); 1.21.8 and below use `noCollission` (double-s, vanilla typo). Both must be honored when copying classes.
- **`feedback_mapcodec_1_21_only.md`** — 1.20.1 must NOT declare `CODEC` + `codec()` override.
- **`feedback_1_21_11_biome_overrides.md`** — biome edits to `chronodawn_ocean.json` must be applied to all 5 locations: `1.20.1/`, `1.21.1/`, `1.21.2/`, `shared-1.21.2+/`, `1.21.11/`.
- **`feedback_simple_random_selector_placed_feature.md`** — `simple_random_selector` features array entries must be placed_feature ID strings, OR objects of shape `{"feature": <ConfiguredFeature>, "placement": []}`. Bare `{"feature": <CF>}` crashes world creation.
- **`feedback_intprovider_uniform_flat_keys.md`** — `minecraft:uniform` IntProvider takes `min_inclusive`/`max_inclusive` flat next to `type`, NOT nested under `value` (1.21+).
- **`feedback_matching_fluids_1_20_5_only.md`** — 1.20.1 must NOT use `matching_fluids` BlockPredicate; substitute `matching_blocks` ["minecraft:water"].
- **`feedback_seagrass_block_water_source.md`** — applies if any of these blocks ever extend SeagrassBlock and end up placed on land. Lumen Polyp uses SeaPickleBlock (no fluid quirk) and Chrono Coral lives on solid floor (BaseCoralPlantTypeBlock places fine), so this should not bite — but if a Chrono Coral plant block silently becomes a water source on land in playtest, recheck.
- **`feedback_food_properties_saturation_rename.md`** — not relevant (no food items in Phase 2), but note for the dispatch table when adapting to 1.20.1.

---

## Open Items / Design Clarifications

- **Lumen Polyp light spec ambiguity.** Spec table says "light level 6/12/15 underwater, 9 max above water" but the same row ends with "fully vanilla-equivalent". Vanilla SeaPickleBlock emits `3 + 3 * pickles` underwater (so 6/9/12/15) and 0 above water. **Plan default: vanilla parity (no override of `getLightEmission`).** If implementer prefers spec-literal numbers, override `getLightEmission` in `LumenPolypBlock` and document the deviation in the Player Guide; otherwise leave parity.
- **Chrono Coral wall_fan en_us key.** Vanilla 1.21+ has separate keys for `block.minecraft.tube_coral_wall_fan` etc. Plan adds 4 wall_fan keys for parity.
- **Texture authoring for 16 corals.** The spec proposes a hue-shift script run against vanilla coral textures. The plan includes a single Task (24) that authors **placeholder solid-color PNGs** so the build proceeds; replacing them with hue-shifted vanilla-derivatives is a follow-up cosmetic commit and not a blocker.
- **`chrono_coral_patch` worldgen distribution.** Plan uses `simple_random_selector` over the 16 placed-feature children with equal weight; tuning weights per color is left to the implementer's playtest judgement.
- **Smelting the corals into white form.** Vanilla allows smelting coral plants into `dead_*`. ChronoDawn corals do not die, so no smelting recipe is added. Confirm this is acceptable to spec author at PR review time.

---

## Sub-phase A: Lumen Polyp foundation on 1.21.11

The minimum independently-shippable Phase-2 unit. Implements `chronodawn:lumen_polyp` end-to-end on 1.21.11. Releasable on its own as a `0.X.0` bump if needed.

---

### Task 1: Add Lumen Polyp ID to `ModBlockId` and `ModItemId`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Append after `TALL_TEMPORAL_SEAGRASS` in `ModBlockId.java`**

`old_string`:

```
    TALL_TEMPORAL_SEAGRASS(def("tall_temporal_seagrass").cutout()),
```

`new_string`:

```
    TALL_TEMPORAL_SEAGRASS(def("tall_temporal_seagrass").cutout()),
    LUMEN_POLYP(def("lumen_polyp").cutout()),
```

- [ ] **Step 2: Append after `TALL_TEMPORAL_SEAGRASS` in `ModItemId.java`**

`old_string`:

```
    TALL_TEMPORAL_SEAGRASS("tall_temporal_seagrass"),
```

`new_string`:

```
    TALL_TEMPORAL_SEAGRASS("tall_temporal_seagrass"),
    LUMEN_POLYP("lumen_polyp"),
```

(Note: `DRIED_TEMPORAL_KELP` stays where it is — Phase 1 left it adjacent to other foods.)

- [ ] **Step 3: Compile shared and 1.21.11**

Run: `./gradlew :common:compileJava :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat(plants): add LUMEN_POLYP id to ModBlockId/ModItemId"
```

---

### Task 2: Implement `LumenPolypBlock` for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/LumenPolypBlock.java`

**Reference:** `TemporalSeagrassBlock.java` in the same dir for the `setId` pattern. Vanilla `SeaPickleBlock` is the parent; its constructor takes only `BlockBehaviour.Properties`. The `.lightLevel(...)` is set on Properties (vanilla approach), not via override.

- [ ] **Step 1: Write the class**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LumenPolypBlock extends SeaPickleBlock {
    public static final MapCodec<LumenPolypBlock> CODEC = simpleCodec(LumenPolypBlock::new);

    public LumenPolypBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<SeaPickleBlock> codec() {
        return (MapCodec<SeaPickleBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .lightLevel(state -> {
                int pickles = state.getValue(SeaPickleBlock.PICKLES);
                return state.getValue(SeaPickleBlock.WATERLOGGED) ? 3 + 3 * pickles : 0;
            })
            .sound(SoundType.SLIME_BLOCK)
            .noOcclusion()
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

The `lightLevel` lambda matches vanilla SeaPickleBlock parity (6/9/12/15 underwater, 0 above). See "Open Items" above for the spec's slightly different numbers — adjust here only if the implementer chooses to deviate.

- [ ] **Step 2: Compile**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/LumenPolypBlock.java
git commit -m "feat(plants): add LumenPolypBlock for 1.21.11"
```

---

### Task 3: Register `LumenPolyp` in `ModBlocks` and `ModItems` for 1.21.11

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Add `import com.chronodawn.blocks.LumenPolypBlock;` to `ModBlocks.java`**

- [ ] **Step 2: Append after `TALL_TEMPORAL_SEAGRASS` registration (around line 961)**

`old_string`:

```java
    public static final RegistrySupplier<Block> TALL_TEMPORAL_SEAGRASS = BLOCKS.register(
        ModBlockId.TALL_TEMPORAL_SEAGRASS.id(),
        () -> new TallTemporalSeagrassBlock(TallTemporalSeagrassBlock.createProperties(ModBlockId.TALL_TEMPORAL_SEAGRASS.id()))
    );
```

`new_string`:

```java
    public static final RegistrySupplier<Block> TALL_TEMPORAL_SEAGRASS = BLOCKS.register(
        ModBlockId.TALL_TEMPORAL_SEAGRASS.id(),
        () -> new TallTemporalSeagrassBlock(TallTemporalSeagrassBlock.createProperties(ModBlockId.TALL_TEMPORAL_SEAGRASS.id()))
    );

    public static final RegistrySupplier<Block> LUMEN_POLYP = BLOCKS.register(
        ModBlockId.LUMEN_POLYP.id(),
        () -> new LumenPolypBlock(LumenPolypBlock.createProperties(ModBlockId.LUMEN_POLYP.id()))
    );
```

- [ ] **Step 3: Append a `BlockItem` registration in `ModItems.java`** after the TALL_TEMPORAL_SEAGRASS BlockItem

`new_string` (insert after the corresponding TALL_TEMPORAL_SEAGRASS item registration):

```java
    public static final RegistrySupplier<Item> LUMEN_POLYP = ITEMS.register(
        ModItemId.LUMEN_POLYP.id(),
        () -> new BlockItem(ModBlocks.LUMEN_POLYP.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.LUMEN_POLYP.id()))))
    );
```

> **Pattern note:** every aquatic-plant BlockItem (TEMPORAL_KELP, TEMPORAL_SEAGRASS, TALL_TEMPORAL_SEAGRASS, LUMEN_POLYP, all Chrono Coral block / plant / fan) **must** chain `.useBlockDescriptionPrefix()` on the `Item.Properties()`. This makes `BlockItem.getDescriptionId()` resolve to `block.chronodawn.<id>` (matching the `block.chronodawn.<id>` lang key) instead of `item.chronodawn.<id>`. Omission silently breaks the held-item tooltip — visible only after lang keys are wired up. Apply the same pattern in Tasks 19, 27.\* and the per-version replication tasks 35–39.

- [ ] **Step 4: Add creative-tab entry** — locate `output.accept(TALL_TEMPORAL_SEAGRASS.get());` and append `output.accept(LUMEN_POLYP.get());` on the next line.

- [ ] **Step 5: Build**

Run: `./gradlew :common-1.21.11:build -Ptarget_mc_version=1.21.11 -x test`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(plants): register LumenPolyp in ModBlocks/ModItems 1.21.11"
```

---

### Task 4: Add Lumen Polyp textures

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/lumen_polyp.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/lumen_polyp.png`

- [ ] **Step 1: Author 16×16 placeholder PNGs**

Bioluminescent purple/blue cold-light palette per spec. Use the texture-creation skill (`texture-creation`) for guidance; for first-pass implementation a hue-shift of vanilla `block/sea_pickle.png` is acceptable.

- [ ] **Step 2: Verify dimensions**

Run: `python3 -c "from PIL import Image; print(Image.open('common/shared/src/main/resources/assets/chronodawn/textures/block/lumen_polyp.png').size); print(Image.open('common/shared/src/main/resources/assets/chronodawn/textures/item/lumen_polyp.png').size)"`
Expected: both print `(16, 16)`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/lumen_polyp.png common/shared/src/main/resources/assets/chronodawn/textures/item/lumen_polyp.png
git commit -m "feat(plants): add Lumen Polyp textures"
```

---

### Task 5: Add Lumen Polyp blockstate JSON

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/lumen_polyp.json`

Vanilla `sea_pickle` blockstate has variants over `pickles=1..4` and `waterlogged=true|false`. Each pickle count has its own model.

- [ ] **Step 1: Write the file**

```json
{
  "variants": {
    "pickles=1,waterlogged=false": { "model": "chronodawn:block/lumen_polyp_1" },
    "pickles=1,waterlogged=true":  { "model": "chronodawn:block/lumen_polyp_1" },
    "pickles=2,waterlogged=false": { "model": "chronodawn:block/lumen_polyp_2" },
    "pickles=2,waterlogged=true":  { "model": "chronodawn:block/lumen_polyp_2" },
    "pickles=3,waterlogged=false": { "model": "chronodawn:block/lumen_polyp_3" },
    "pickles=3,waterlogged=true":  { "model": "chronodawn:block/lumen_polyp_3" },
    "pickles=4,waterlogged=false": { "model": "chronodawn:block/lumen_polyp_4" },
    "pickles=4,waterlogged=true":  { "model": "chronodawn:block/lumen_polyp_4" }
  }
}
```

- [ ] **Step 2: Commit (validation deferred to after model creation)**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/lumen_polyp.json
git commit -m "feat(plants): add Lumen Polyp blockstate"
```

---

### Task 6: Add Lumen Polyp block models (4 pickle counts) and item model

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/lumen_polyp_1.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/lumen_polyp_2.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/lumen_polyp_3.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/lumen_polyp_4.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/lumen_polyp.json`

Vanilla parents `minecraft:block/sea_pickle_1..4` are reusable since the texture-mapped slot is named `texture`.

- [ ] **Step 1: Write `lumen_polyp_1.json`**

```json
{
  "parent": "minecraft:block/sea_pickle_1",
  "render_type": "minecraft:cutout",
  "textures": {
    "texture": "chronodawn:block/lumen_polyp"
  }
}
```

> **Verify** parent existence by inspecting vanilla `sea_pickle_1.json` via `./gradlew :common-1.21.11:genSources -Ptarget_mc_version=1.21.11`. If the texture-mapped slot is named differently in the vanilla parent, adjust the key and update the other three model files accordingly.

- [ ] **Step 2: Write `lumen_polyp_2.json`**, `lumen_polyp_3.json`, `lumen_polyp_4.json` — identical structure, only `parent` changes (`sea_pickle_2`, `_3`, `_4`).

- [ ] **Step 3: Write the item model `models/item/lumen_polyp.json`** — per `feedback_item_model_flat_icon_pattern.md`, use `item/generated`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "chronodawn:item/lumen_polyp" }
}
```

- [ ] **Step 4: Validate**

Run: `./gradlew validateResources`
Expected: PASS — blockstate→model and model→texture cross-references resolve.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/
git commit -m "feat(plants): add Lumen Polyp block + item models"
```

---

### Task 7: Add Lumen Polyp Client Items JSON for 1.21.5+

**Files:**
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/lumen_polyp.json`

- [ ] **Step 1: Write the file**

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/lumen_polyp"}}
```

- [ ] **Step 2: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/lumen_polyp.json
git commit -m "feat(plants): add Lumen Polyp Client Items JSON (1.21.5+)"
```

---

### Task 8: Add Lumen Polyp loot table for 1.21.1+

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/lumen_polyp.json`

Drop policy (vanilla SeaPickleBlock parity): drops `lumen_polyp` items equal to the `pickles` count, regardless of tool.

- [ ] **Step 1: Write the file**

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
          "name": "chronodawn:lumen_polyp",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "type": "minecraft:score",
                "target": "this",
                "score": "pickles"
              }
            }
          ]
        }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/lumen_polyp"
}
```

> **Verify** the `set_count` form used by vanilla `sea_pickle.json` (1.21.x); the literal vanilla form uses a `block_state_property` count provider rather than the `score` provider above. If `validateResources` fails, fall back to copying vanilla `data/minecraft/loot_tables/blocks/sea_pickle.json` from genSources output and substituting the item id.

- [ ] **Step 2: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/lumen_polyp.json
git commit -m "feat(plants): add Lumen Polyp loot table (1.21.1+)"
```

---

### Task 9: Add Lumen Polyp en_us translation

**Files:**
- Modify: `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json`

> **Plan-typo correction:** earlier drafts referenced `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`, but the lang files are era-split — the only existing locations are `common/shared-1.21.2+/`, `common/1.21.1/`, and `common/1.20.1/`. Use the `shared-1.21.2+` file for 1.21.11 (it covers 1.21.2+); Sub-phases I and J will add the same key to the older lang files.

Per `feedback_lang_json_line_based_edit.md`, use `Edit` with anchors.

- [ ] **Step 1: Insert after the seagrass-family entries**

The file pairs `block.chronodawn.<id>` immediately with `item.chronodawn.<id>` for every double-registered block (`temporal_seagrass`, `tall_temporal_seagrass`, etc.). Use a 2-line anchor covering both lines of `tall_temporal_seagrass` so the new Lumen Polyp entry slots in after the seagrass family without splitting a paired block/item lang group.

`old_string`:

```
  "block.chronodawn.tall_temporal_seagrass": "Tall Temporal Seagrass",
  "item.chronodawn.tall_temporal_seagrass": "Tall Temporal Seagrass",
```

`new_string`:

```
  "block.chronodawn.tall_temporal_seagrass": "Tall Temporal Seagrass",
  "item.chronodawn.tall_temporal_seagrass": "Tall Temporal Seagrass",
  "block.chronodawn.lumen_polyp": "Lumen Polyp",
```

- [ ] **Step 2: Validate**

Run: `./gradlew validateTranslations`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat(plants): add Lumen Polyp en_us translation"
```

---

### Task 10: Verify Lumen Polyp end-to-end on 1.21.11 (no worldgen yet)

- [ ] **Step 1: Build**

Run: `./gradlew clean1_21_11 build1_21_11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run client**

Run: `./gradlew runClientFabric1_21_11`
In-game checks (in creative):
1. Place a Lumen Polyp underwater — verify it emits light (~6, 9, 12, 15 per pickle count).
2. Bone-meal a Lumen Polyp underwater — count grows up to 4.
3. Place out of water — emits 0 light, count stays 1.
4. Break with bare hand — drops 1–4 Lumen Polyp items matching pickle count.

- [ ] **Step 3: No commit needed** (no file changes).

---

### Task 11: Foundation checkpoint commit

If all in-world checks pass, Sub-phase A is complete. Tag the state with a marker commit (optional):

```bash
git commit --allow-empty -m "checkpoint: Lumen Polyp foundation verified on 1.21.11"
```

---

## Sub-phase B: Lumen Polyp worldgen on 1.21.11

Adds the configured + placed feature pair so Lumen Polyp generates in `chronodawn:chronodawn_ocean`.

---

### Task 12: Add `chronodawn:lumen_polyp` configured + placed feature for 1.21.1+

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/lumen_polyp.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/lumen_polyp.json`

The feature places a `lumen_polyp` block with random pickle count on the ocean floor, low frequency.

- [ ] **Step 1: Write configured feature**

```json
{
  "type": "minecraft:simple_block",
  "config": {
    "to_place": {
      "type": "minecraft:weighted_state_provider",
      "entries": [
        { "weight": 4, "data": { "Name": "chronodawn:lumen_polyp", "Properties": { "pickles": "1", "waterlogged": "true" } } },
        { "weight": 2, "data": { "Name": "chronodawn:lumen_polyp", "Properties": { "pickles": "2", "waterlogged": "true" } } },
        { "weight": 1, "data": { "Name": "chronodawn:lumen_polyp", "Properties": { "pickles": "3", "waterlogged": "true" } } },
        { "weight": 1, "data": { "Name": "chronodawn:lumen_polyp", "Properties": { "pickles": "4", "waterlogged": "true" } } }
      ]
    }
  }
}
```

- [ ] **Step 2: Write placed feature**

```json
{
  "feature": "chronodawn:lumen_polyp",
  "placement": [
    { "type": "minecraft:count", "count": 4 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR_WG" },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "blocks": [
          "minecraft:sand",
          "minecraft:gravel",
          "chronodawn:temporal_sand"
        ]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

The `block_predicate_filter` constrains placement to ocean-floor sand/gravel (vanilla coral patches use the same approach). `chronodawn:temporal_sand` is included since the dimension floor is mostly Temporal Sand.

- [ ] **Step 3: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/
git commit -m "feat(worldgen): add chronodawn:lumen_polyp configured + placed feature (1.21.1+)"
```

---

### Task 13: Reference `chronodawn:lumen_polyp` from `chronodawn_ocean.json` biomes (4 of 5 locations)

**Files:**
- Modify: `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`
- Modify: `common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`
- Modify: `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`
- Modify: `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json`

(The 1.20.1 biome JSON is touched in Sub-phase J alongside its loader-specific worldgen.)

- [ ] **Step 1: Read each file's `features` array (typically `vegetal_decoration` step, index 4)** to confirm the line that already contains `chronodawn:seagrass` or `chronodawn:kelp`. The new entry inserts on a new line right after that.

- [ ] **Step 2: Insert `"chronodawn:lumen_polyp",` after the existing `"chronodawn:seagrass",`** in each of the 4 files. Use `Edit` with the exact prior line as anchor.

`old_string` (per file, may vary slightly — read to confirm):

```
        "chronodawn:seagrass"
```

`new_string`:

```
        "chronodawn:seagrass",
        "chronodawn:lumen_polyp"
```

If the line already ends with a comma (entries before the last in the array), keep the existing comma and add `"chronodawn:lumen_polyp",` after.

- [ ] **Step 3: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_ocean.json
git commit -m "feat(worldgen): reference chronodawn:lumen_polyp from chronodawn_ocean biome (1.21.1-1.21.11)"
```

---

### Task 14: Verify Lumen Polyp worldgen on 1.21.11

- [ ] **Step 1: Build**

Run: `./gradlew clean1_21_11 build1_21_11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run client**

Run: `./gradlew runClientFabric1_21_11`
In-game checks:
1. Create a new world with seed `0` (or any reproducible seed).
2. Find `chronodawn:chronodawn_ocean` and inspect the floor.
3. Confirm Lumen Polyp clusters appear sporadically on the floor, with mixed pickle counts.
4. Confirm they emit light underwater.

- [ ] **Step 3: No commit needed.**

---

## Sub-phase C: Chrono Coral form classes + Dawn color on 1.21.11

Implements the 4 form base classes (no death) + the first color (Dawn) end-to-end.

---

### Task 15: Implement `ChronoCoralBlock` for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralBlock.java`

Vanilla `CoralBlock` (1.21.x) has a `randomTick` that converts to its constructor-supplied `deadBlock`. Our subclass overrides `randomTick` to no-op. Constructor takes only `BlockBehaviour.Properties` (single-arg, since we never use the dead-block path) — this means we cannot extend `CoralBlock` directly because its sole public constructor is `(Block deadBlock, BlockBehaviour.Properties)`. **Workaround:** pass `Blocks.AIR` as the `deadBlock` and override `randomTick`/`tick` so the never-used dead conversion is unreachable.

- [ ] **Step 1: Write the class**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChronoCoralBlock extends CoralBlock {
    public static final MapCodec<ChronoCoralBlock> CODEC = simpleCodec(ChronoCoralBlock::new);

    public ChronoCoralBlock(BlockBehaviour.Properties properties) {
        super(Blocks.AIR, properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<CoralBlock> codec() {
        return (MapCodec<CoralBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // ChronoDawn coral is frozen in time — never dies out of water.
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // ChronoDawn coral is frozen in time — never dies out of water.
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .strength(1.5F, 6.0F)
            .sound(SoundType.CORAL_BLOCK)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

> **Verify** the `CoralBlock` constructor signature is `(Block, BlockBehaviour.Properties)` in 1.21.11 by genSources inspection. If it instead takes the dead-block as a `Supplier<Block>` (older pattern), adjust the `super(...)` call.

- [ ] **Step 2: Compile**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralBlock.java
git commit -m "feat(plants): add ChronoCoralBlock (no-death override) for 1.21.11"
```

---

### Task 16: Implement `ChronoCoralPlantBlock` for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralPlantBlock.java`

Vanilla `CoralPlantBlock` extends `BaseCoralPlantTypeBlock`, which itself overrides `randomTick` to convert to a dead variant. We override the `randomTick` here.

- [ ] **Step 1: Write the class**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CoralPlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChronoCoralPlantBlock extends CoralPlantBlock {
    public static final MapCodec<ChronoCoralPlantBlock> CODEC = simpleCodec(ChronoCoralPlantBlock::new);

    public ChronoCoralPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<CoralPlantBlock> codec() {
        return (MapCodec<CoralPlantBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Frozen in time — never dies out of water.
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollision()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralPlantBlock.java
git commit -m "feat(plants): add ChronoCoralPlantBlock (no-death override) for 1.21.11"
```

---

### Task 17: Implement `ChronoCoralFanBlock` and `ChronoCoralWallFanBlock` for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralFanBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralWallFanBlock.java`

- [ ] **Step 1: Write `ChronoCoralFanBlock.java`**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChronoCoralFanBlock extends CoralFanBlock {
    public static final MapCodec<ChronoCoralFanBlock> CODEC = simpleCodec(ChronoCoralFanBlock::new);

    public ChronoCoralFanBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<CoralFanBlock> codec() {
        return (MapCodec<CoralFanBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Frozen in time — never dies out of water.
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollision()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
```

- [ ] **Step 2: Write `ChronoCoralWallFanBlock.java`** (same pattern, different parent; vanilla wall_fan loot is authored as a separate file that drops the fan item — see Task 24 step 4)

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CoralWallFanBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChronoCoralWallFanBlock extends CoralWallFanBlock {
    public static final MapCodec<ChronoCoralWallFanBlock> CODEC = simpleCodec(ChronoCoralWallFanBlock::new);

    public ChronoCoralWallFanBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<CoralWallFanBlock> codec() {
        return (MapCodec<CoralWallFanBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Frozen in time — never dies out of water.
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollision()
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
git add common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralFanBlock.java common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralWallFanBlock.java
git commit -m "feat(plants): add ChronoCoralFanBlock + WallFanBlock (no-death override) for 1.21.11"
```

---

### Task 18: Add 4 Dawn Chrono Coral IDs

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Append after `LUMEN_POLYP` in `ModBlockId.java`**

`old_string`:

```
    LUMEN_POLYP(def("lumen_polyp").cutout()),
```

`new_string`:

```
    LUMEN_POLYP(def("lumen_polyp").cutout()),
    DAWN_CHRONO_CORAL_BLOCK(def("dawn_chrono_coral_block")),
    DAWN_CHRONO_CORAL(def("dawn_chrono_coral").cutout()),
    DAWN_CHRONO_CORAL_FAN(def("dawn_chrono_coral_fan").cutout()),
    DAWN_CHRONO_CORAL_WALL_FAN(def("dawn_chrono_coral_wall_fan").cutout()),
```

(The full block has no cutout; the plant/fan/wall_fan all do.)

- [ ] **Step 2: Append in `ModItemId.java`** — note: only 3 items per color (block, plant, fan); wall_fan reuses fan item per vanilla pattern.

`old_string`:

```
    LUMEN_POLYP("lumen_polyp"),
```

`new_string`:

```
    LUMEN_POLYP("lumen_polyp"),
    DAWN_CHRONO_CORAL_BLOCK("dawn_chrono_coral_block"),
    DAWN_CHRONO_CORAL("dawn_chrono_coral"),
    DAWN_CHRONO_CORAL_FAN("dawn_chrono_coral_fan"),
```

- [ ] **Step 3: Compile**

Run: `./gradlew :common:compileJava :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat(plants): add Dawn Chrono Coral ids (4 blocks, 3 items)"
```

---

### Task 19: Register 4 Dawn blocks + 3 BlockItems for 1.21.11

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Add imports** to `ModBlocks.java`:

```java
import com.chronodawn.blocks.ChronoCoralBlock;
import com.chronodawn.blocks.ChronoCoralPlantBlock;
import com.chronodawn.blocks.ChronoCoralFanBlock;
import com.chronodawn.blocks.ChronoCoralWallFanBlock;
```

- [ ] **Step 2: Append 4 block registrations** after `LUMEN_POLYP`:

```java
    public static final RegistrySupplier<Block> DAWN_CHRONO_CORAL_BLOCK = BLOCKS.register(
        ModBlockId.DAWN_CHRONO_CORAL_BLOCK.id(),
        () -> new ChronoCoralBlock(ChronoCoralBlock.createProperties(ModBlockId.DAWN_CHRONO_CORAL_BLOCK.id()))
    );

    public static final RegistrySupplier<Block> DAWN_CHRONO_CORAL = BLOCKS.register(
        ModBlockId.DAWN_CHRONO_CORAL.id(),
        () -> new ChronoCoralPlantBlock(ChronoCoralPlantBlock.createProperties(ModBlockId.DAWN_CHRONO_CORAL.id()))
    );

    public static final RegistrySupplier<Block> DAWN_CHRONO_CORAL_FAN = BLOCKS.register(
        ModBlockId.DAWN_CHRONO_CORAL_FAN.id(),
        () -> new ChronoCoralFanBlock(ChronoCoralFanBlock.createProperties(ModBlockId.DAWN_CHRONO_CORAL_FAN.id()))
    );

    public static final RegistrySupplier<Block> DAWN_CHRONO_CORAL_WALL_FAN = BLOCKS.register(
        ModBlockId.DAWN_CHRONO_CORAL_WALL_FAN.id(),
        () -> new ChronoCoralWallFanBlock(ChronoCoralWallFanBlock.createProperties(ModBlockId.DAWN_CHRONO_CORAL_WALL_FAN.id()))
    );
```

- [ ] **Step 3: Append 3 BlockItem registrations** in `ModItems.java` (no item for wall_fan — vanilla pattern). Each `Item.Properties()` MUST chain `.useBlockDescriptionPrefix()` per Task 3 step 3's pattern note:

```java
    public static final RegistrySupplier<Item> DAWN_CHRONO_CORAL_BLOCK = ITEMS.register(
        ModItemId.DAWN_CHRONO_CORAL_BLOCK.id(),
        () -> new BlockItem(ModBlocks.DAWN_CHRONO_CORAL_BLOCK.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.DAWN_CHRONO_CORAL_BLOCK.id()))))
    );

    public static final RegistrySupplier<Item> DAWN_CHRONO_CORAL = ITEMS.register(
        ModItemId.DAWN_CHRONO_CORAL.id(),
        () -> new BlockItem(ModBlocks.DAWN_CHRONO_CORAL.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.DAWN_CHRONO_CORAL.id()))))
    );

    public static final RegistrySupplier<Item> DAWN_CHRONO_CORAL_FAN = ITEMS.register(
        ModItemId.DAWN_CHRONO_CORAL_FAN.id(),
        () -> new StandingAndWallBlockItem(ModBlocks.DAWN_CHRONO_CORAL_FAN.get(), ModBlocks.DAWN_CHRONO_CORAL_WALL_FAN.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.DAWN_CHRONO_CORAL_FAN.id()))), Direction.DOWN)
    );
```

> **Note:** vanilla coral fan items use `StandingAndWallBlockItem` so placing on a wall auto-converts to the wall_fan block. Required imports: `net.minecraft.world.item.StandingAndWallBlockItem`, `net.minecraft.core.Direction`.

- [ ] **Step 4: Add creative-tab entries** — append after `output.accept(LUMEN_POLYP.get());`:

```java
        output.accept(DAWN_CHRONO_CORAL_BLOCK.get());
        output.accept(DAWN_CHRONO_CORAL.get());
        output.accept(DAWN_CHRONO_CORAL_FAN.get());
```

- [ ] **Step 5: Build**

Run: `./gradlew :common-1.21.11:build -Ptarget_mc_version=1.21.11 -x test`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(plants): register Dawn Chrono Coral (4 blocks + 3 items) in 1.21.11"
```

---

### Task 20: Add Dawn Chrono Coral textures (8 PNGs)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/dawn_chrono_coral_block.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/dawn_chrono_coral.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral_block.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral_fan.png`

(Block textures only need 2 — the plant texture is also used by the fan and wall_fan models per vanilla precedent. Item textures need 3 — block, plant, fan — wall_fan has no item.)

- [ ] **Step 1: Author 16×16 placeholder PNGs in Dawn (orange/golden) palette**

Use `texture-creation` skill or hue-shift `block/tube_coral_block.png` and `block/tube_coral.png`.

- [ ] **Step 2: Verify dimensions**

Run: `python3 -c "from PIL import Image; [print(p, Image.open(p).size) for p in ['common/shared/src/main/resources/assets/chronodawn/textures/block/dawn_chrono_coral_block.png','common/shared/src/main/resources/assets/chronodawn/textures/block/dawn_chrono_coral.png','common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral_block.png','common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral.png','common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral_fan.png']]"`
Expected: every line prints `(16, 16)`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/dawn_chrono_coral_block.png common/shared/src/main/resources/assets/chronodawn/textures/block/dawn_chrono_coral.png common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral_block.png common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral.png common/shared/src/main/resources/assets/chronodawn/textures/item/dawn_chrono_coral_fan.png
git commit -m "feat(plants): add Dawn Chrono Coral textures"
```

---

### Task 21: Add Dawn Chrono Coral blockstates (4 files)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/dawn_chrono_coral_block.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/dawn_chrono_coral.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/dawn_chrono_coral_fan.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/dawn_chrono_coral_wall_fan.json`

Vanilla `tube_coral_*` blockstates as reference. CoralBlock is a single variant; plant/fan have a `waterlogged` property; wall_fan has `facing` × `waterlogged`.

- [ ] **Step 1: `dawn_chrono_coral_block.json`**

```json
{
  "variants": {
    "": { "model": "chronodawn:block/dawn_chrono_coral_block" }
  }
}
```

- [ ] **Step 2: `dawn_chrono_coral.json`** (the plant — has `waterlogged` property)

```json
{
  "variants": {
    "waterlogged=false": { "model": "chronodawn:block/dawn_chrono_coral" },
    "waterlogged=true":  { "model": "chronodawn:block/dawn_chrono_coral" }
  }
}
```

- [ ] **Step 3: `dawn_chrono_coral_fan.json`** (also has `waterlogged`)

```json
{
  "variants": {
    "waterlogged=false": { "model": "chronodawn:block/dawn_chrono_coral_fan" },
    "waterlogged=true":  { "model": "chronodawn:block/dawn_chrono_coral_fan" }
  }
}
```

- [ ] **Step 4: `dawn_chrono_coral_wall_fan.json`** (has `facing` × `waterlogged`)

```json
{
  "variants": {
    "facing=north,waterlogged=false": { "model": "chronodawn:block/dawn_chrono_coral_wall_fan", "y": 270 },
    "facing=north,waterlogged=true":  { "model": "chronodawn:block/dawn_chrono_coral_wall_fan", "y": 270 },
    "facing=east,waterlogged=false":  { "model": "chronodawn:block/dawn_chrono_coral_wall_fan" },
    "facing=east,waterlogged=true":   { "model": "chronodawn:block/dawn_chrono_coral_wall_fan" },
    "facing=south,waterlogged=false": { "model": "chronodawn:block/dawn_chrono_coral_wall_fan", "y": 90 },
    "facing=south,waterlogged=true":  { "model": "chronodawn:block/dawn_chrono_coral_wall_fan", "y": 90 },
    "facing=west,waterlogged=false":  { "model": "chronodawn:block/dawn_chrono_coral_wall_fan", "y": 180 },
    "facing=west,waterlogged=true":   { "model": "chronodawn:block/dawn_chrono_coral_wall_fan", "y": 180 }
  }
}
```

- [ ] **Step 5: Commit (validation deferred to after model creation)**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/dawn_chrono_coral*
git commit -m "feat(plants): add Dawn Chrono Coral blockstates"
```

---

### Task 22: Add Dawn Chrono Coral block + item models (7 files)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/dawn_chrono_coral_block.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/dawn_chrono_coral.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/dawn_chrono_coral_fan.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/dawn_chrono_coral_wall_fan.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/dawn_chrono_coral_block.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/dawn_chrono_coral.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/dawn_chrono_coral_fan.json`

Vanilla parents:
- `minecraft:block/coral_block` — texture key `all`
- `minecraft:block/coral_plant` — texture key `coral`
- `minecraft:block/coral_fan` — texture key `fan`
- `minecraft:block/coral_wall_fan` — texture key `fan`

- [ ] **Step 1: `models/block/dawn_chrono_coral_block.json`**

```json
{
  "parent": "minecraft:block/coral_block",
  "textures": { "all": "chronodawn:block/dawn_chrono_coral_block" }
}
```

- [ ] **Step 2: `models/block/dawn_chrono_coral.json`**

```json
{
  "parent": "minecraft:block/coral_plant",
  "render_type": "minecraft:cutout",
  "textures": { "coral": "chronodawn:block/dawn_chrono_coral" }
}
```

- [ ] **Step 3: `models/block/dawn_chrono_coral_fan.json`**

```json
{
  "parent": "minecraft:block/coral_fan",
  "render_type": "minecraft:cutout",
  "textures": { "fan": "chronodawn:block/dawn_chrono_coral" }
}
```

(Vanilla reuses the plant texture for the fan — same does ChronoDawn.)

- [ ] **Step 4: `models/block/dawn_chrono_coral_wall_fan.json`**

```json
{
  "parent": "minecraft:block/coral_wall_fan",
  "render_type": "minecraft:cutout",
  "textures": { "fan": "chronodawn:block/dawn_chrono_coral" }
}
```

- [ ] **Step 5: `models/item/dawn_chrono_coral_block.json`**

```json
{
  "parent": "chronodawn:block/dawn_chrono_coral_block"
}
```

(Block model parent is fine for full cubes — vanilla coral_block items do this.)

- [ ] **Step 6: `models/item/dawn_chrono_coral.json`** (per `feedback_item_model_flat_icon_pattern.md`, plant items use `item/generated`)

```json
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "chronodawn:item/dawn_chrono_coral" }
}
```

- [ ] **Step 7: `models/item/dawn_chrono_coral_fan.json`**

```json
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "chronodawn:item/dawn_chrono_coral_fan" }
}
```

- [ ] **Step 8: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/
git commit -m "feat(plants): add Dawn Chrono Coral block + item models"
```

---

### Task 23: Add Dawn Chrono Coral Client Items JSON (3 files for 1.21.5+)

**Files:**
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/dawn_chrono_coral_block.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/dawn_chrono_coral.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/dawn_chrono_coral_fan.json`

(No wall_fan Client Items JSON — wall_fan has no BlockItem.)

- [ ] **Step 1: Each file's contents** (substitute the matching item id):

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/dawn_chrono_coral_block"}}
```

- [ ] **Step 2: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/dawn_chrono_coral*
git commit -m "feat(plants): add Dawn Chrono Coral Client Items JSON (1.21.5+)"
```

---

### Task 24: Add Dawn Chrono Coral loot tables (4 files for 1.21.1+)

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/dawn_chrono_coral_block.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/dawn_chrono_coral.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/dawn_chrono_coral_fan.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/dawn_chrono_coral_wall_fan.json`

Drop policy (vanilla coral parity):
- Coral block: drops itself only with silk-touch (vanilla coral block becomes dead without silk).
  - **Override for ChronoDawn:** since our coral never dies, dropping itself unconditionally is acceptable too. **Plan default: silk-touch only**, matching vanilla feel.
- Coral plant / fan: silk-touch only.
- Coral wall_fan: silk-touch only, drops the fan item (`name: "chronodawn:dawn_chrono_coral_fan"`).

Per `feedback_loot_table_silk_touch_format.md`, 1.21.1+ uses nested `predicates.minecraft:enchantments` form.

- [ ] **Step 1: `dawn_chrono_coral_block.json`** (silk-touch)

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
          "name": "chronodawn:dawn_chrono_coral_block",
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
        }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/dawn_chrono_coral_block"
}
```

- [ ] **Step 2: `dawn_chrono_coral.json`** (plant — silk-touch, drops `dawn_chrono_coral`)

Same shape as Step 1, with `name: "chronodawn:dawn_chrono_coral"` and `random_sequence: "chronodawn:blocks/dawn_chrono_coral"`.

- [ ] **Step 3: `dawn_chrono_coral_fan.json`** — same with `name: "chronodawn:dawn_chrono_coral_fan"`.

- [ ] **Step 4: `dawn_chrono_coral_wall_fan.json`** — same shape, but the dropped item is `chronodawn:dawn_chrono_coral_fan` (vanilla pattern: wall_fan drops fan item).

- [ ] **Step 5: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/dawn_chrono_coral*
git commit -m "feat(plants): add Dawn Chrono Coral loot tables (silk-touch, 1.21.1+)"
```

---

### Task 25: Add Dawn Chrono Coral en_us translations + verify foundation

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`

- [ ] **Step 1: Insert after `block.chronodawn.lumen_polyp`**

`old_string`:

```
  "block.chronodawn.lumen_polyp": "Lumen Polyp",
```

`new_string`:

```
  "block.chronodawn.lumen_polyp": "Lumen Polyp",
  "block.chronodawn.dawn_chrono_coral_block": "Dawn Chrono Coral Block",
  "block.chronodawn.dawn_chrono_coral": "Dawn Chrono Coral",
  "block.chronodawn.dawn_chrono_coral_fan": "Dawn Chrono Coral Fan",
  "block.chronodawn.dawn_chrono_coral_wall_fan": "Dawn Chrono Coral Wall Fan",
```

- [ ] **Step 2: Validate**

Run: `./gradlew validateTranslations`
Expected: PASS.

- [ ] **Step 3: Build + run client**

Run: `./gradlew clean1_21_11 build1_21_11`
Expected: `BUILD SUCCESSFUL`.

Run: `./gradlew runClientFabric1_21_11`
In creative:
1. Place a Dawn Chrono Coral Block. After several minutes out of water, confirm it does NOT convert (stays Dawn Chrono Coral Block).
2. Place a Dawn Chrono Coral plant on a sand block underwater. Wait — does NOT die.
3. Place a Dawn Chrono Coral Fan on a sand block underwater. Stays.
4. Place a Dawn Chrono Coral Fan on a wall — it auto-converts to wall_fan. Wait — does NOT die.
5. Break each with silk-touch shears: drops itself.
6. Break each without silk-touch: no drop.
7. Break wall_fan: drops a fan item.

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat(plants): add Dawn Chrono Coral en_us translations"
```

---

## Sub-phase D: Chrono Coral 3 remaining colors (Dusk, Twilight, Eternal)

Mechanical replication of Dawn pattern × 3. 12 blocks, 9 BlockItems, 6 textures, 12 blockstates, 21 models, 9 Client Items JSONs, 12 loot tables, 12 lang keys.

For each `<COLOR>` in {dusk, twilight, eternal}:

---

### Task 26.<COLOR>: Add 4 IDs

Apply Task 18 with `DAWN` → `<COLOR_UPPER>` and `dawn_` → `<COLOR>_`.

Concrete for `dusk`:

```
    DUSK_CHRONO_CORAL_BLOCK(def("dusk_chrono_coral_block")),
    DUSK_CHRONO_CORAL(def("dusk_chrono_coral").cutout()),
    DUSK_CHRONO_CORAL_FAN(def("dusk_chrono_coral_fan").cutout()),
    DUSK_CHRONO_CORAL_WALL_FAN(def("dusk_chrono_coral_wall_fan").cutout()),
```

(Item ids analogous; only block/plant/fan, no wall_fan item.)

- [ ] **Step 1: Edit `ModBlockId.java` + `ModItemId.java`**
- [ ] **Step 2: Compile**
- [ ] **Step 3: Commit** with message `feat(plants): add <Color> Chrono Coral ids`

---

### Task 27.<COLOR>: Register 4 blocks + 3 BlockItems for 1.21.11

Apply Task 19 mechanically.

- [ ] **Step 1: Edit `ModBlocks.java` + `ModItems.java`** for 1.21.11
- [ ] **Step 2: Build**
- [ ] **Step 3: Commit** with message `feat(plants): register <Color> Chrono Coral in 1.21.11`

---

### Task 28.<COLOR>: Add 5 textures

- 2 block textures: `<color>_chrono_coral_block.png`, `<color>_chrono_coral.png`
- 3 item textures: `<color>_chrono_coral_block.png`, `<color>_chrono_coral.png`, `<color>_chrono_coral_fan.png`

Palette guide:
- **Dusk**: violet/lavender (vanilla `tube_coral` hue-shifted to magenta).
- **Twilight**: deep blue/navy (vanilla `bubble_coral` hue-shifted darker).
- **Eternal**: pale white/silver (vanilla `tube_coral` desaturated).

- [ ] **Step 1: Author 5 PNGs at 16×16**
- [ ] **Step 2: Verify dimensions**
- [ ] **Step 3: Commit** with message `feat(plants): add <Color> Chrono Coral textures`

---

### Task 29.<COLOR>: Add 4 blockstates

Apply Task 21 mechanically.

- [ ] **Step 1: Author 4 blockstate JSONs**
- [ ] **Step 2: Commit** with message `feat(plants): add <Color> Chrono Coral blockstates`

---

### Task 30.<COLOR>: Add 4 block models + 3 item models

Apply Task 22 mechanically.

- [ ] **Step 1: Author 7 model JSONs**
- [ ] **Step 2: Validate**: `./gradlew validateResources`
- [ ] **Step 3: Commit** with message `feat(plants): add <Color> Chrono Coral models`

---

### Task 31.<COLOR>: Add 3 Client Items JSON

Apply Task 23 mechanically.

- [ ] **Step 1: Author 3 JSONs in `shared-1.21.5+`**
- [ ] **Step 2: Validate**
- [ ] **Step 3: Commit** with message `feat(plants): add <Color> Chrono Coral Client Items JSON`

---

### Task 32.<COLOR>: Add 4 loot tables + en_us translations

Apply Tasks 24 + 25 mechanically.

- [ ] **Step 1: Author 4 loot tables in `shared-1.21.1+`**
- [ ] **Step 2: Append 4 lang keys**
- [ ] **Step 3: Validate**: `./gradlew validateResources validateTranslations`
- [ ] **Step 4: Build + commit** with message `feat(plants): add <Color> Chrono Coral loot tables + lang`

---

## Sub-phase E: Chrono Coral worldgen on 1.21.11

---

### Task 33: Add `chronodawn:chrono_coral_patch` configured + placed feature

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/chrono_coral_patch.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/chrono_coral_patch.json`
- Create: 16 `data/chronodawn/worldgen/placed_feature/<color>_chrono_coral_<form>.json` files (one per coral block placement, used as inner placed_feature children — needed because `simple_random_selector` requires placed_feature references per `feedback_simple_random_selector_placed_feature.md`)

Per `feedback_simple_random_selector_placed_feature.md`, the `simple_random_selector` features array entries must be placed_feature ID strings. So we create 16 small inner placed_feature wrappers, each placing one coral block, and the outer `chrono_coral_patch` uses `simple_random_selector` to pick across them.

> **Simpler alternative:** wrap each coral as a `simple_block` configured-feature inline within `simple_random_selector` using the `{"feature": <CF>, "placement": []}` object form (also valid per the same memory entry). This avoids the 16 inner placed_feature files. Plan recommends this simpler form.

- [ ] **Step 1: Write `configured_feature/chrono_coral_patch.json`** using inline configured-features:

```json
{
  "type": "minecraft:simple_random_selector",
  "config": {
    "features": [
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:dawn_chrono_coral_block" } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:dusk_chrono_coral_block" } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:twilight_chrono_coral_block" } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:eternal_chrono_coral_block" } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:dawn_chrono_coral", "Properties": { "waterlogged": "true" } } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:dusk_chrono_coral", "Properties": { "waterlogged": "true" } } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:twilight_chrono_coral", "Properties": { "waterlogged": "true" } } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:eternal_chrono_coral", "Properties": { "waterlogged": "true" } } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:dawn_chrono_coral_fan", "Properties": { "waterlogged": "true" } } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:dusk_chrono_coral_fan", "Properties": { "waterlogged": "true" } } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:twilight_chrono_coral_fan", "Properties": { "waterlogged": "true" } } } } }, "placement": [] },
      { "feature": { "type": "minecraft:simple_block", "config": { "to_place": { "type": "minecraft:simple_state_provider", "state": { "Name": "chronodawn:eternal_chrono_coral_fan", "Properties": { "waterlogged": "true" } } } } }, "placement": [] }
    ]
  }
}
```

12 entries: 4 blocks + 4 plants + 4 fans. Wall_fan is omitted from the random selector — wall placement happens organically when a player places a fan item against a wall; worldgen places the floor variant only. This matches vanilla `coral_claw` and `coral_tree` feature behavior.

- [ ] **Step 2: Write `placed_feature/chrono_coral_patch.json`**

```json
{
  "feature": "chronodawn:chrono_coral_patch",
  "placement": [
    { "type": "minecraft:count", "count": 6 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR_WG" },
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:matching_blocks",
        "blocks": [
          "minecraft:sand",
          "minecraft:gravel",
          "chronodawn:temporal_sand"
        ]
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 3: Validate**

Run: `./gradlew validateResources`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/
git commit -m "feat(worldgen): add chronodawn:chrono_coral_patch configured + placed feature (1.21.1+)"
```

---

### Task 34: Reference `chronodawn:chrono_coral_patch` from biome JSONs (4 of 5 locations)

Apply Task 13 mechanically with `chronodawn:chrono_coral_patch`. Insert after the `chronodawn:lumen_polyp` line added in Task 13.

- [ ] **Step 1: Edit each of the 4 biome JSONs**
- [ ] **Step 2: Validate**: `./gradlew validateResources`
- [ ] **Step 3: Verify on 1.21.11**: `./gradlew clean1_21_11 build1_21_11 runClientFabric1_21_11`
  - Travel to `chronodawn:chronodawn_ocean` and confirm 4-color coral patches appear on the floor with mixed forms.
- [ ] **Step 4: Commit** with message `feat(worldgen): reference chrono_coral_patch from chronodawn_ocean biome (1.21.1-1.21.11)`

---

## Sub-phase F: Replicate Java to 1.21.10 → 1.21.5

For each `<v>` in {1.21.10, 1.21.9, 1.21.8, 1.21.7, 1.21.6, 1.21.5}:

API era boundaries to honor:
- **1.21.10**: same as 1.21.11 except `Identifier` → `ResourceLocation`. `noCollision()` (single-s) on 1.21.10 — wait, the boundary is at 1.21.9.
- **1.21.9**: `noCollision()` (single-s, same as 1.21.10/11), `ResourceLocation`.
- **1.21.8 down to 1.21.5**: `noCollission()` (double-s), `ResourceLocation`.

### Task 35: Replicate to each `<v>`

**Files (per `<v>`):**
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/LumenPolypBlock.java`
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralBlock.java`
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralPlantBlock.java`
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralFanBlock.java`
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralWallFanBlock.java`
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Copy 5 block classes from 1.21.11**

```bash
cp common/1.21.11/src/main/java/com/chronodawn/blocks/LumenPolypBlock.java common/<v>/src/main/java/com/chronodawn/blocks/LumenPolypBlock.java
cp common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralBlock.java common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralBlock.java
cp common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralPlantBlock.java common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralPlantBlock.java
cp common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralFanBlock.java common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralFanBlock.java
cp common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoCoralWallFanBlock.java common/<v>/src/main/java/com/chronodawn/blocks/ChronoCoralWallFanBlock.java
```

- [ ] **Step 2: In each of the 5 files, replace `Identifier` with `ResourceLocation`**

Use `Edit` with `replace_all`:
- `old_string: "import net.minecraft.resources.Identifier;"` → `new_string: "import net.minecraft.resources.ResourceLocation;"`
- `old_string: "Identifier.fromNamespaceAndPath"` → `new_string: "ResourceLocation.fromNamespaceAndPath"`

- [ ] **Step 3: For `<v>` ≤ 1.21.8, replace `noCollision()` with `noCollission()`** in `ChronoCoralPlantBlock.java`, `ChronoCoralFanBlock.java`, `ChronoCoralWallFanBlock.java` (LumenPolypBlock and ChronoCoralBlock don't use `noCollision`).

Use `Edit` with `replace_all`:
- `old_string: ".noCollision()"` → `new_string: ".noCollission()"`

- [ ] **Step 4: Replicate `ModBlocks.java` and `ModItems.java` edits from Tasks 3, 19, 27.dusk, 27.twilight, 27.eternal**

Mechanical: same `import` additions, same registration lines, with `Identifier` → `ResourceLocation` swap.

- [ ] **Step 5: Build for `<v>`**

Run: `./gradlew clean<v_underscored> build<v_underscored> -Ptarget_mc_version=<v>`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add common/<v>/src/main/java/com/chronodawn/blocks/ common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java common/<v>/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(plants): replicate Phase 2 aquatic plants to <v>"
```

---

## Sub-phase G: Replicate to 1.21.4

1.21.4 differs in:
1. `noCollission()` (double-s) — already covered by Sub-phase F's `<v> ≤ 1.21.8` rule, applies here too.
2. **Client Items JSON dir is per-version** (`common/1.21.4/`), not shared.

### Task 36: Replicate Java + Client Items JSON to 1.21.4

- [ ] **Step 1: Copy 5 block classes from 1.21.5**

(Use 1.21.5 as the source since it already has `ResourceLocation` + `noCollission()`.)

- [ ] **Step 2: Replicate `ModBlocks.java` and `ModItems.java`** from 1.21.5.

- [ ] **Step 3: Copy 13 Client Items JSON from `shared-1.21.5+` to `1.21.4`**

```bash
for f in lumen_polyp \
         dawn_chrono_coral_block dawn_chrono_coral dawn_chrono_coral_fan \
         dusk_chrono_coral_block dusk_chrono_coral dusk_chrono_coral_fan \
         twilight_chrono_coral_block twilight_chrono_coral twilight_chrono_coral_fan \
         eternal_chrono_coral_block eternal_chrono_coral eternal_chrono_coral_fan; do
  cp common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/$f.json common/1.21.4/src/main/resources/assets/chronodawn/items/$f.json
done
```

- [ ] **Step 4: Build for 1.21.4**

Run: `./gradlew clean1_21_4 build1_21_4 -Ptarget_mc_version=1.21.4`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add common/1.21.4/
git commit -m "feat(plants): replicate Phase 2 aquatic plants to 1.21.4"
```

---

## Sub-phase H: Replicate to 1.21.2

1.21.2 differs only by inheritance from `shared-1.21.2+/` for Client Items and biome JSON. Java mirrors 1.21.4.

### Task 37: Replicate Java to 1.21.2

- [ ] **Step 1: Copy 5 block classes from 1.21.4 (`ResourceLocation` + `noCollission()`)**
- [ ] **Step 2: Replicate `ModBlocks.java` + `ModItems.java`**
- [ ] **Step 3: Build for 1.21.2**: `./gradlew clean1_21_2 build1_21_2 -Ptarget_mc_version=1.21.2`
- [ ] **Step 4: Commit**: `git commit -m "feat(plants): replicate Phase 2 aquatic plants to 1.21.2"`

---

## Sub-phase I: Replicate to 1.21.1

1.21.1 differs in:
1. **No `setId`** on `Item.Properties` or `BlockBehaviour.Properties`.
2. `MapCodec`/`codec()` override is required (1.21+ rule), but signature differs slightly: covariance in `MapCodec<? extends ParentBlock>` form (per `TemporalSeagrassBlock.java` 1.21.1 reference).

### Task 38: Replicate Java to 1.21.1

- [ ] **Step 1: Author 5 classes for 1.21.1 by adapting the 1.21.2 ones**

Each class: drop the `setId(...)` chain in `createProperties`; drop the `Identifier` import; the `MapCodec`/`codec()` block stays.

Concrete `LumenPolypBlock.java` for 1.21.1:

```java
package com.chronodawn.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LumenPolypBlock extends SeaPickleBlock {
    public static final MapCodec<LumenPolypBlock> CODEC = simpleCodec(LumenPolypBlock::new);

    public LumenPolypBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<SeaPickleBlock> codec() {
        return (MapCodec<SeaPickleBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .lightLevel(state -> {
                int pickles = state.getValue(SeaPickleBlock.PICKLES);
                return state.getValue(SeaPickleBlock.WATERLOGGED) ? 3 + 3 * pickles : 0;
            })
            .sound(SoundType.SLIME_BLOCK)
            .noOcclusion()
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
```

(`createProperties()` no longer takes `id`. The 4 ChronoCoral classes follow the same pattern.)

- [ ] **Step 2: Replicate `ModBlocks.java` + `ModItems.java`** for 1.21.1, dropping the `setId(...)` chain on `Item.Properties` and the `id` arg on each `createProperties()` call.

- [ ] **Step 3: Modify `chronodawn_ocean.json` for 1.21.1** (insert `chronodawn:lumen_polyp` and `chronodawn:chrono_coral_patch` references)

- [ ] **Step 4: Build for 1.21.1**: `./gradlew clean1_21_1 build1_21_1 -Ptarget_mc_version=1.21.1`

- [ ] **Step 5: Commit**: `git commit -m "feat(plants): replicate Phase 2 aquatic plants to 1.21.1"`

---

## Sub-phase J: Replicate to 1.20.1 (largest divergence)

1.20.1 differs in:
1. `MapCodec`/`codec()` override is **forbidden** (per `feedback_mapcodec_1_21_only.md`).
2. No `setId` (same as 1.21.1).
3. Loot table dir is `loot_tables/` (plural).
4. `feedback_matching_fluids_1_20_5_only.md` — placed feature `block_predicate_filter` predicates differ.

### Task 39: Replicate Java + worldgen + loot tables + biome to 1.20.1

- [ ] **Step 1: Author 5 classes for 1.20.1**

Each class: take the 1.21.1 form and **delete** the `MapCodec` and `codec()` declarations entirely. Keep the `randomTick`/`tick` overrides.

Concrete `LumenPolypBlock.java` for 1.20.1:

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

// MapCodec/codec() is 1.21+ only — not used in 1.20.1
public class LumenPolypBlock extends SeaPickleBlock {
    public LumenPolypBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .lightLevel(state -> {
                int pickles = state.getValue(SeaPickleBlock.PICKLES);
                return state.getValue(SeaPickleBlock.WATERLOGGED) ? 3 + 3 * pickles : 0;
            })
            .sound(SoundType.SLIME_BLOCK)
            .noOcclusion()
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
```

The 4 ChronoCoral classes drop the `MapCodec`/`codec()` similarly.

- [ ] **Step 2: Replicate `ModBlocks.java` + `ModItems.java`** for 1.20.1.

- [ ] **Step 3: Author 17 loot tables in `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/`**

Same shape as 1.21.1+ but using the 1.20.1 silk-touch form: direct `enchantments` (not nested `predicates.minecraft:enchantments`), and items array `[ "minecraft:shears" ]` (not bare string). Reference: `common/1.20.1/.../loot_tables/blocks/temporal_seagrass.json`.

- [ ] **Step 4: Author 4 worldgen JSONs in `common/1.20.1/src/main/resources/data/chronodawn/worldgen/`**

Configured + placed feature for `lumen_polyp` and `chrono_coral_patch`. Same content as 1.21.1+ except:
- Per `feedback_matching_fluids_1_20_5_only.md`, placed feature `block_predicate_filter` must use `matching_blocks` ["minecraft:water"] instead of `matching_fluids` (if applicable — check; the Phase 1 placed features did substitute).
- Per `feedback_intprovider_uniform_flat_keys.md` — not relevant here since we use weighted state providers, not IntProviders.

- [ ] **Step 5: Modify `common/1.20.1/.../biome/chronodawn_ocean.json`** to reference both new placed features.

- [ ] **Step 6: Build for 1.20.1**: `./gradlew clean1_20_1 build1_20_1 -Ptarget_mc_version=1.20.1`

- [ ] **Step 7: Commit**: `git commit -m "feat(plants): replicate Phase 2 aquatic plants to 1.20.1"`

---

## Sub-phase K: Tests, verification, docs, release

---

### Task 40: Add unit tests

**Files:**
- Create: `common/shared/src/test/java/com/chronodawn/unit/AquaticPlantsPhase2RegistrationTest.java`
- Create: `common/shared/src/test/java/com/chronodawn/unit/ChronoCoralLootTableTest.java`

- [ ] **Step 1: Write `AquaticPlantsPhase2RegistrationTest.java`** asserting all 17 ids exist (`LUMEN_POLYP`, `<COLOR>_CHRONO_CORAL_BLOCK/PLANT/FAN/WALL_FAN` × 4)

```java
package com.chronodawn.unit;

import com.chronodawn.registry.ModBlockId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AquaticPlantsPhase2RegistrationTest {
    @Test
    void lumenPolypIdExists() {
        assertNotNull(ModBlockId.valueOf("LUMEN_POLYP"));
    }

    @Test
    void dawnChronoCoralBlockIdExists() {
        assertNotNull(ModBlockId.valueOf("DAWN_CHRONO_CORAL_BLOCK"));
    }

    @Test
    void dawnChronoCoralIdExists() {
        assertNotNull(ModBlockId.valueOf("DAWN_CHRONO_CORAL"));
    }

    @Test
    void dawnChronoCoralFanIdExists() {
        assertNotNull(ModBlockId.valueOf("DAWN_CHRONO_CORAL_FAN"));
    }

    @Test
    void dawnChronoCoralWallFanIdExists() {
        assertNotNull(ModBlockId.valueOf("DAWN_CHRONO_CORAL_WALL_FAN"));
    }

    @Test
    void duskChronoCoralBlockIdExists() {
        assertNotNull(ModBlockId.valueOf("DUSK_CHRONO_CORAL_BLOCK"));
    }

    @Test
    void duskChronoCoralIdExists() {
        assertNotNull(ModBlockId.valueOf("DUSK_CHRONO_CORAL"));
    }

    @Test
    void duskChronoCoralFanIdExists() {
        assertNotNull(ModBlockId.valueOf("DUSK_CHRONO_CORAL_FAN"));
    }

    @Test
    void duskChronoCoralWallFanIdExists() {
        assertNotNull(ModBlockId.valueOf("DUSK_CHRONO_CORAL_WALL_FAN"));
    }

    @Test
    void twilightChronoCoralBlockIdExists() {
        assertNotNull(ModBlockId.valueOf("TWILIGHT_CHRONO_CORAL_BLOCK"));
    }

    @Test
    void twilightChronoCoralIdExists() {
        assertNotNull(ModBlockId.valueOf("TWILIGHT_CHRONO_CORAL"));
    }

    @Test
    void twilightChronoCoralFanIdExists() {
        assertNotNull(ModBlockId.valueOf("TWILIGHT_CHRONO_CORAL_FAN"));
    }

    @Test
    void twilightChronoCoralWallFanIdExists() {
        assertNotNull(ModBlockId.valueOf("TWILIGHT_CHRONO_CORAL_WALL_FAN"));
    }

    @Test
    void eternalChronoCoralBlockIdExists() {
        assertNotNull(ModBlockId.valueOf("ETERNAL_CHRONO_CORAL_BLOCK"));
    }

    @Test
    void eternalChronoCoralIdExists() {
        assertNotNull(ModBlockId.valueOf("ETERNAL_CHRONO_CORAL"));
    }

    @Test
    void eternalChronoCoralFanIdExists() {
        assertNotNull(ModBlockId.valueOf("ETERNAL_CHRONO_CORAL_FAN"));
    }

    @Test
    void eternalChronoCoralWallFanIdExists() {
        assertNotNull(ModBlockId.valueOf("ETERNAL_CHRONO_CORAL_WALL_FAN"));
    }
}
```

- [ ] **Step 2: Write `ChronoCoralLootTableTest.java`**

Resource-driven test that opens each of the 16 coral loot table JSONs from the 1.21.1+ classpath and asserts: file exists, has at least one pool, the pool's first entry is gated by silk-touch.

Pattern: copy the `LootTableValidationTest.java` setup; iterate over a fixed list of 16 coral block ids.

- [ ] **Step 3: Run unit tests on 1.21.11**: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11`

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/test/java/com/chronodawn/unit/
git commit -m "test(plants): add Phase 2 registration + loot table tests"
```

---

### Task 41: Add GameTest for Lumen Polyp bone-meal growth

**Files:**
- Create: `gametest/src/main/java/com/chronodawn/gametest/aquatic/LumenPolypGameTest.java`

Reference: any existing GameTest under `gametest/src/main/java/com/chronodawn/gametest/`.

- [ ] **Step 1: Author the GameTest** — place a Lumen Polyp at fixed pos, call `BoneMealItem.applyBonemeal` (or equivalent), assert pickle count incremented.

- [ ] **Step 2: Author structure NBT** at `gametest/src/main/resources/data/chronodawn/structures/aquatic/lumen_polyp_grow.nbt` — single sand block underwater.

- [ ] **Step 3: Run gametest for 1.21.11**: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11`

Expected: PASS.

- [ ] **Step 4: Commit**: `git commit -m "test(plants): add GameTest for Lumen Polyp bone-meal growth"`

---

### Task 42: Run full `checkAll`

> Per `feedback_check_all_before_phase_completion.md`, this MUST be run before declaring Phase 2 complete.

- [ ] **Step 1: Run umbrella verification**

Run: `./gradlew checkAll`
Expected: PASS — runs cleanAll, validateResources, validateTranslations, buildAll (11 versions), testAll, gameTestAll.

Per memory `feedback_buildall_gametestall_wrapper_unreliable.md`, if `buildAll` or `gameTestAll` reports FAILED but per-version standalone runs pass, the wrapper is the issue, not the code. Verify standalone where ambiguous:

```bash
./gradlew clean1_21_11 build1_21_11 -Ptarget_mc_version=1.21.11
./gradlew clean1_21_1 build1_21_1 -Ptarget_mc_version=1.21.1
./gradlew clean1_20_1 build1_20_1 -Ptarget_mc_version=1.20.1
```

- [ ] **Step 2: Manual in-world verification on 3 representative versions**

```bash
./gradlew runClientFabric1_20_1     # oldest era
./gradlew runClientNeoForge1_21_1   # NeoForge era
./gradlew runClientFabric1_21_4     # Client Items JSON era boundary
```

Repeat the in-world checks from Tasks 14, 25, 34.

---

### Task 43: Update CHANGELOG, player guide, store descriptions

**Files:**
- Modify: `CHANGELOG.md`
- Modify: `docs/player_guide.md`
- Modify: `docs/curseforge_description.md`
- Modify: `docs/modrinth_description.md`

- [ ] **Step 1: `CHANGELOG.md` Unreleased > Added**

```markdown
- **Temporal Aquatic Plants (Phase 2 — Light and Color)**: ChronoDawn ocean now spawns Lumen Polyp (bioluminescent underwater light source, 1–4 cluster, bone-meal grows, vanilla SeaPickleBlock parity) and Chrono Coral in 4 colors (Dawn, Dusk, Twilight, Eternal) × 4 forms (block, plant, fan, wall fan). Chrono Coral does not die out of water — frozen-time fiction. Pre-existing chunks retain prior content; only newly generated chunks see the new blocks.
```

- [ ] **Step 2: `docs/player_guide.md`** — extend the "Aquatic Plants" subsection with Phase 2 content (covering Lumen Polyp light levels, Chrono Coral immortality out of water, drop rules: silk-touch only).

- [ ] **Step 3: CurseForge / Modrinth feature lists** — add bullet `Bioluminescent Lumen Polyp + 16 Chrono Coral variants` to each.

- [ ] **Step 4: Commit**

```bash
git add CHANGELOG.md docs/player_guide.md docs/curseforge_description.md docs/modrinth_description.md
git commit -m "docs(plants): document Temporal Aquatic Plants Phase 2"
```

---

### Task 44: Bump version + final `checkAll`

**Files:**
- Modify: `gradle.properties`

Phase 2 is a MINOR bump (additive feature) per `versioning` skill: e.g. `0.8.X` → `0.9.0`.

- [ ] **Step 1: Bump `mod_version` in `gradle.properties`**

- [ ] **Step 2: Final `checkAll`**

Run: `./gradlew checkAll`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add gradle.properties
git commit -m "chore(plants): bump version for Temporal Aquatic Plants Phase 2"
```

---

## Self-Review Notes

Spec section to task mapping:

| Spec section | Implementing task(s) |
|---|---|
| Plant Inventory > Phase 2 > Lumen Polyp | Tasks 1–11, 12–14 (worldgen) |
| Plant Inventory > Phase 2 > Chrono Coral × 4 colors × 4 forms | Tasks 15–25 (Dawn), 26–32 (× 3 colors), 33–34 (worldgen) |
| Worldgen Replacement Strategy > new placed features | Tasks 12, 33 |
| Worldgen Replacement Strategy > biome JSON updates | Tasks 13, 34 (1.21.1–1.21.11), 39 step 5 (1.20.1) |
| Multi-Version Considerations > setId requirement | Tasks 2, 15–17 (1.21.11), 35–36 (1.21.4–1.21.10), 37 (1.21.2), 38 (1.21.1 drops it), 39 (1.20.1 drops it) |
| Multi-Version Considerations > Client Items JSON | Tasks 7, 23, 31 (1.21.5+ shared); Task 36 (1.21.4 per-version) |
| Multi-Version Considerations > loot table dir name | Tasks 8, 24, 32 (1.21.1+ singular); Task 39 step 3 (1.20.1 plural) |
| Multi-Version Considerations > 1.21.11 biome overrides | Tasks 13, 34 (touch 1.21.11/biome dir alongside shared-1.21.2+) |
| Testing Plan | Tasks 40 (unit), 41 (GameTest), 42 (checkAll + manual) |
| Documentation Touchpoints per Phase | Task 43 |
| Versioning | Task 44 |

**Open items (still deferred to implementation time):**

- Vanilla `CoralBlock` constructor signature exact arity in 1.21.11 — cross-check at Task 15. Plan assumes `(Block, BlockBehaviour.Properties)`; if `(BlockBehaviour.Properties)` only, drop the `Blocks.AIR` arg.
- Vanilla `block/coral_*` model parents and texture keys — cross-check at Task 22.
- `set_count` provider for Lumen Polyp loot — cross-check at Task 8.
- Lumen Polyp light spec — see Open Items above.
- Texture authoring beyond placeholders — out of plan scope, follow-up cosmetic commit.

**Implementation note for the engineer:** Sub-phases C / D require **17 unique block ids** registered correctly with their own `setId`. The single most likely place for bugs is mis-typing one of the 16 coral block ids in `ModBlocks.register(...)`; if you see one block render with a wrong texture or share a loot table with another, confirm `createProperties(ModBlockId.<X>.id())` wasn't accidentally pasted with the wrong color prefix.
