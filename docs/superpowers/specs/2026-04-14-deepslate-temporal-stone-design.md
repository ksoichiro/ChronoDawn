# Deepslate Temporal Stone & Deepslate Ores

## Summary

Add Deepslate Temporal Stone as the deep-layer base block for the ChronoDawn dimension (replacing vanilla `minecraft:deepslate` at Y≤0), along with deepslate variants of ores that generate in that zone.

## Problem

- ChronoDawn dimension uses vanilla `minecraft:deepslate` at Y≤0, which looks out of place
- Ores generating below Y=0 (gold, redstone, clockstone, temporal amber) either fail to generate in the deepslate zone or use the wrong texture

## New Blocks (8 total)

### Deepslate Temporal Stone + Derivatives (4 blocks)

| Block ID | Type | Properties |
|---|---|---|
| `deepslate_temporal_stone` | Basic block | hardness 3.0, resistance 6.0, pickaxe, drops self |
| `deepslate_temporal_stone_stairs` | Stairs | Same properties |
| `deepslate_temporal_stone_slab` | Slab | Same properties |
| `deepslate_temporal_stone_wall` | Wall | Same properties |

### Deepslate Ores (4 blocks)

| Block ID | Drops | Tool Tier | XP |
|---|---|---|---|
| `deepslate_temporal_amber_ore` | Same as temporal_amber_ore | Iron | 2-5 |
| `deepslate_temporal_gold_ore` | Same as temporal_gold_ore | Iron | — |
| `deepslate_temporal_redstone_ore` | Same as temporal_redstone_ore | Iron | 1-5 |
| `deepslate_clockstone_ore` | Same as clockstone_ore | Iron | — |

All deepslate ores have hardness 4.5 / resistance 3.0 (1.5x harder than normal variants, following vanilla deepslate ore convention).

## Textures

6 placeholder PNG files (to be replaced by user):
- `deepslate_temporal_stone.png`
- `deepslate_temporal_amber_ore.png`
- `deepslate_temporal_gold_ore.png`
- `deepslate_temporal_redstone_ore.png`
- `deepslate_clockstone_ore.png`

Stairs/slab/wall reuse `deepslate_temporal_stone.png`.

## Worldgen Changes

### Surface Rules (noise_settings/chronodawn.json)

Replace `minecraft:deepslate` with `chronodawn:deepslate_temporal_stone` at Y≤0 in all version-specific noise settings files.

### Ore Feature Targets

Add deepslate target entries to configured features:

| Configured Feature | New Target | New State |
|---|---|---|
| `ore_temporal_amber` | `chronodawn:deepslate_temporal_stone` | `chronodawn:deepslate_temporal_amber_ore` |
| `ore_gold` | `chronodawn:deepslate_temporal_stone` | `chronodawn:deepslate_temporal_gold_ore` |
| `ore_redstone` | `chronodawn:deepslate_temporal_stone` | `chronodawn:deepslate_temporal_redstone_ore` |
| `ore_clockstone` | `chronodawn:deepslate_temporal_stone` | `chronodawn:deepslate_clockstone_ore` |

For `ore_temporal_amber`: also remove the existing `minecraft:deepslate` target (no longer present in the dimension).

## Resources per Block

- Blockstate JSON (`common/shared/`)
- Block model JSON (`common/shared/`)
- Item model JSON (`common/shared/`)
- Items JSON (`common/1.21.4/` and `common/shared-1.21.5+/`)
- Loot table (`common/shared-1.21.1+/` and `common/1.20.1/`)
- Tags: `mineable/pickaxe` (all), `needs_iron_tool` (all 4 ores)
- Translation keys: `en_us.json`, `ja_jp.json` (`common/shared-1.21.2+/` and `common/1.20.1/`)

## Registration

- Add IDs to `ModBlockId.java` (`common/shared/`)
- Add block classes per version (`common/<version>/`)
- Register blocks + items in `ModBlocks.java` per version

## Out of Scope

- Deepslate Entropy Crystal Ore (generates Y=40-100, above deepslate zone)
- Deepslate Coal/Iron/Time Crystal Ore (generates Y≥0)
- Deepslate Temporal Cobblestone derivatives
