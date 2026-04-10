# Temporal Ore Blocks + Dimension Terrain Update Design

## Overview

Chrono Dawn ディメンション用の4種の鉱石ブロック（coal, gold, iron, redstone）を追加し、ディメンションの基本地形を `minecraft:stone` から `chronodawn:temporal_stone` に置き換え、全鉱石（新規4種 + 既存2種）が temporal_stone に埋め込まれる形で生成されるようにする。

## Part 1: Ore Blocks (4 new blocks)

| Block | ID | Drop | Properties |
|---|---|---|---|
| Temporal Coal Ore | `temporal_coal_ore` | `minecraft:coal` (silk touch: self) | Vanilla coal_ore equivalent |
| Temporal Gold Ore | `temporal_gold_ore` | `minecraft:raw_gold` (silk touch: self) | Vanilla gold_ore equivalent |
| Temporal Iron Ore | `temporal_iron_ore` | `minecraft:raw_iron` (silk touch: self) | Vanilla iron_ore equivalent |
| Temporal Redstone Ore | `temporal_redstone_ore` | `minecraft:redstone` x1-5, Fortune, silk touch: self | Vanilla redstone_ore equivalent + lit behavior |

### Block Properties

- temporal_coal_ore / gold_ore / iron_ore: `strength(3.0f, 3.0f)`, `requiresCorrectToolForDrops()`, `sound(SoundType.STONE)`
- temporal_redstone_ore: Same + `lightLevel(lit ? 9 : 0)`, `randomTicks`, `isRedstoneConductor(false)`. Has `lit` blockstate property.

### Redstone Ore Special Behavior

`temporal_redstone_ore` extends vanilla `RedStoneOreBlock`. It has the `lit` blockstate and glows when stepped on or interacted with, identical to vanilla behavior.

### Textures

Color-transformed vanilla ore textures with temporal purple/blue tint. Placeholder; to be replaced manually later.

## Part 2: Dimension Terrain Replacement

Change Chrono Dawn dimension noise settings `default_block` from `minecraft:stone` to `chronodawn:temporal_stone`.

## Part 3: Worldgen Ore Configuration Updates

### configured_feature updates (6 files)

| Feature | Old Target | New Target | Old Ore Block | New Ore Block |
|---|---|---|---|---|
| `ore_coal` | `minecraft:stone` | `chronodawn:temporal_stone` | `minecraft:coal_ore` | `chronodawn:temporal_coal_ore` |
| `ore_gold` | `minecraft:stone` | `chronodawn:temporal_stone` | `minecraft:gold_ore` | `chronodawn:temporal_gold_ore` |
| `ore_iron` | `minecraft:stone` | `chronodawn:temporal_stone` | `minecraft:iron_ore` | `chronodawn:temporal_iron_ore` |
| `ore_redstone` | `minecraft:stone` | `chronodawn:temporal_stone` | `minecraft:redstone_ore` | `chronodawn:temporal_redstone_ore` |
| `ore_clockstone` | `minecraft:stone` | `chronodawn:temporal_stone` | (unchanged) | (unchanged) |
| `ore_time_crystal` | `minecraft:stone` | `chronodawn:temporal_stone` | (unchanged) | (unchanged) |

All deepslate targets removed (temporal_stone only).

### Biome JSON updates

Add `chronodawn:ore_iron` to all 1.21.1+ biome feature lists (restoring iron ore generation removed since 1.21.1).

## Out of Scope

- Deepslate variants
- Recipes (smelting ore blocks)
- Additional ore types
