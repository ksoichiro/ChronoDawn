# Custom Dirt Block Set Design

## Overview

Replace vanilla dirt/grass/coarse_dirt in the ChronoDawn dimension with custom blocks that have unique textures while supporting biome-based color tinting.

## Blocks

### Temporal Dirt (`temporal_dirt`)

- Equivalent to vanilla `dirt`
- Hoe converts to `minecraft:farmland`
- Full-color texture (not tinted)

### Temporal Grass Block (`temporal_grass_block`)

- Equivalent to vanilla `grass_block`
- Grass spreads to adjacent `temporal_dirt`
- Reverts to `temporal_dirt` when light is blocked
- Drops `temporal_dirt` without Silk Touch
- Bone meal spawns grass/flowers
- Top and side overlay textures are **grayscale**, tinted by biome `grass_color`
- `BlockColors` client registration returns biome grass color (same tint index as vanilla)

### Coarse Temporal Dirt (`coarse_temporal_dirt`)

- Equivalent to vanilla `coarse_dirt`
- Hoe converts to `temporal_dirt`
- Grass does not spread onto this block
- Full-color texture (not tinted)

## Textures Required

All 16x16 PNG files, placed in `assets/chronodawn/textures/block/`:

| File | Description | Color Mode |
|---|---|---|
| `temporal_dirt.png` | Dirt block, all faces | Full color |
| `temporal_grass_block_top.png` | Grass block top face | Grayscale (biome tinted) |
| `temporal_grass_block_side_overlay.png` | Grass overlay on side faces | Grayscale (biome tinted) |
| `coarse_temporal_dirt.png` | Coarse dirt, all faces | Full color |
| `temporal_grass_block_side.png` | Grass block side (dirt + grass combined) | Full color |

Note: Vanilla grass blocks use both `grass_block_side` (full color) and `grass_block_side_overlay` (grayscale). Following the same pattern requires 5 textures. If overlay-only approach is used, `temporal_grass_block_side.png` can be omitted (4 textures).

## Block Models

- `temporal_dirt` — `minecraft:block/cube_all` parent, texture `temporal_dirt`
- `temporal_grass_block` — Custom model following vanilla `grass_block` pattern (tinted top + side overlay, untinted bottom using `temporal_dirt`)
- `coarse_temporal_dirt` — `minecraft:block/cube_all` parent, texture `coarse_temporal_dirt`

## Block Registration

- Add entries to `ModBlockId` enum (`temporal_dirt`, `temporal_grass_block`, `coarse_temporal_dirt`)
- Create block classes extending appropriate vanilla base classes
- Register in `ModBlocks` for each supported version
- Register `BlockColors` on client side for `temporal_grass_block`

## Surface Rule Updates

Update `overworld.json` noise settings in each version directory:

- Surface block: `minecraft:grass_block` -> `chronodawn:temporal_grass_block` (all biomes except desert)
- Subsurface block: `minecraft:dirt` -> `chronodawn:temporal_dirt` (all biomes except desert)
- `patch_coarse_dirt` feature: `minecraft:coarse_dirt` -> `chronodawn:coarse_temporal_dirt`

Desert biome continues to use `chronodawn:reversing_time_sandstone`.

## Compatibility

- Farmland conversion uses vanilla `minecraft:farmland` (no custom farmland)
- Existing biome `grass_color` / `foliage_color` values are reused as-is
