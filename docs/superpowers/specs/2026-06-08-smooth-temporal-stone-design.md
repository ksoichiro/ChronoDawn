# Smooth Temporal Stone — Design Spec

**Date**: 2026-06-08
**Status**: Approved

## Goal

Add a "smooth" variant of Temporal Stone, mirroring vanilla Smooth Stone:

- A full block `chronodawn:smooth_temporal_stone`, obtained by **smelting** `chronodawn:temporal_stone` (parallels vanilla `stone` → `smooth_stone`).
- Its slab `chronodawn:smooth_temporal_stone_slab` (craftable + stonecutting), parallels vanilla `smooth_stone_slab`.
- Replace vanilla `minecraft:smooth_stone` with the new block inside the mod's NBT structures.

**Out of scope**: world/natural generation, worldgen integration of any kind.

## Naming

Follows the existing adjective-prefix convention (`mossy_temporal_cobblestone`,
`deepslate_temporal_stone`): `smooth_temporal_stone` / `smooth_temporal_stone_slab`.

## Block properties

Match vanilla Smooth Stone exactly (NOT plain stone):

- hardness **2.0**, resistance 6.0, `MapColor.STONE`, `SoundType.STONE`, `requiresCorrectToolForDrops()`.
- Group A (1.20.1, 1.21.1): `Properties.of()` with `.strength(2.0f, 6.0f)` etc., no `setId()`.
- Group B (1.21.2–1.21.10): `Properties.ofFullCopy(Blocks.SMOOTH_STONE)` + `.setId(ResourceLocation…)`.
- Group C (1.21.11): same as B but `Identifier`.
- Slab delegates `createProperties()` to the block; Group B/C add the slab's own `.setId(...)`.
- Tool tier: no tier tag (vanilla smooth stone mines with any pickaxe). Only `mineable/pickaxe`.

## Files / Tasks

### IDs (single source)
- `common/shared/.../registry/ModBlockId.java`: add `SMOOTH_TEMPORAL_STONE`, `SMOOTH_TEMPORAL_STONE_SLAB`.
- `common/shared/.../registry/ModItemId.java`: matching two entries.

### Block classes (per version × 11)
- `SmoothTemporalStoneBlock.java`, `SmoothTemporalStoneSlab.java` in each `common/<ver>/.../blocks/`, Group A/B/C pattern.

### Registration (per version × 11)
- `ModBlocks.java`: register both blocks.
- `ModItems.java`: register both `BlockItem`s (A/B/C pattern) + add to `populateCreativeTab()`.

### Resources (shared)
- Texture `textures/block/smooth_temporal_stone.png` — auto-generated: extract vanilla `smooth_stone.png`, apply the `stone`→`temporal_stone` per-channel multipliers (derived via histogram comparison), 16×16.
- Blockstates: `smooth_temporal_stone.json`, `smooth_temporal_stone_slab.json` (3 states).
- Block models: `smooth_temporal_stone` (cube_all), `smooth_temporal_stone_slab`, `smooth_temporal_stone_slab_top`.
- Item models: `smooth_temporal_stone`, `smooth_temporal_stone_slab`.
- Client Items JSON (1.21.4 and shared-1.21.5+) for both ids.

### Loot tables
- `1.20.1` (`loot_tables/blocks/`) + `shared-1.21.1+` (`loot_table/blocks/`):
  - block: drop self.
  - slab: double → 2 (standard slab loot).

### Tags
- `minecraft:mineable/pickaxe` for both, in `1.20.1` (`tags/blocks/`) and `shared-1.21.1+` (`tags/block/`).

### Translations (3 eras: 1.20.1, 1.21.1, shared-1.21.2+)
- en_us: "Smooth Temporal Stone" / "Smooth Temporal Stone Slab".
- ja_jp: "なめらかなTemporal Stone" / "なめらかなTemporal Stoneのハーフブロック" (match existing ja slab phrasing).

### Recipes (3 eras: 1.20.1 `recipes/`, 1.21.1 `recipe/`, shared-1.21.2+ `recipe/`)
- `smooth_temporal_stone_from_smelting.json` (or vanilla-style name): smelting `temporal_stone` → `smooth_temporal_stone`, cooktime 200, exp 0.1.
- `smooth_temporal_stone_slab.json`: shaped `BBB` → slab ×6.
- `smooth_temporal_stone_slab_stonecutting.json`: `smooth_temporal_stone` → slab ×2.

### NBT structure replacement
- Add to `scripts/nbt_block_mappings.json`:
  `"minecraft:smooth_stone": "chronodawn:smooth_temporal_stone"`.
- Affects 4 structures: clockwork_depths_archive_vault, clockwork_depths_engine_room,
  clockwork_depths_gearshaft, old_sundial. (Build-time replacement; no binary editing.)

### Docs
- `CHANGELOG.md` entry.
- Player/developer guides: add to block lists only if such lists exist (confirm).

## Verification
- `./gradlew validateResources` + `validateTranslations`.
- `./gradlew build1_21_11` spot build, then `buildAll` + `testAll` (CreativeTabCompletenessTest).
- Confirm no unintended `*.java` modifications via `git diff --name-only`.
