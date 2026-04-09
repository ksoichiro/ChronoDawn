# Temporal Stone Block Set Design

## Overview

Chrono Dawn ディメンションの地形生成用基本石ブロックセット。バニラの石（stone）/ 丸石（cobblestone）に相当するカスタムブロックとそのバリエーション（階段・ハーフブロック・壁）を追加する。

## Blocks

| Block | ID | Base Class | Drop |
|---|---|---|---|
| Temporal Stone | `temporal_stone` | `Block` | `temporal_cobblestone` (silk touch: self) |
| Temporal Cobblestone | `temporal_cobblestone` | `Block` | self |
| Temporal Stone Stairs | `temporal_stone_stairs` | `StairBlock` | self |
| Temporal Stone Slab | `temporal_stone_slab` | `SlabBlock` | self |
| Temporal Stone Wall | `temporal_stone_wall` | `WallBlock` | self |
| Temporal Cobblestone Stairs | `temporal_cobblestone_stairs` | `StairBlock` | self |
| Temporal Cobblestone Slab | `temporal_cobblestone_slab` | `SlabBlock` | self |
| Temporal Cobblestone Wall | `temporal_cobblestone_wall` | `WallBlock` | self |

## Block Properties

### Temporal Stone
- `mapColor(MapColor.STONE)`
- `strength(1.5f, 6.0f)`
- `requiresCorrectToolForDrops()`
- `sound(SoundType.STONE)`

### Temporal Cobblestone
- `mapColor(MapColor.STONE)`
- `strength(2.0f, 6.0f)`
- `requiresCorrectToolForDrops()`
- `sound(SoundType.STONE)`

### Variants (Stairs / Slab / Wall)
- Inherit properties from their respective base block via `ofFullCopy`

## Textures

- Vanilla `stone.png` / `cobblestone.png` をベースに色変換
- 既存の temporal 系ブロック（temporal_dirt, temporal_grass_block）の色調に合わせた紫〜青系

## Implementation Pattern

既存の `clockstone_block` + `clockstone_stairs/slab/wall` パターンに準拠：

1. **ModBlockId**: 8エントリ追加（render layer は全て SOLID デフォルト）
2. **Block classes**: `TemporalStoneBlock`, `TemporalCobblestoneBlock` を新規作成。階段/ハーフ/壁は既存の汎用クラスパターンを使用
3. **ModBlocks**: 8ブロック登録
4. **ModItems**: 8ブロックアイテム登録
5. **Resources** (common/shared):
   - `blockstates/` - 8 JSON files
   - `models/block/` - block models (階段は inner/outer 含む、壁は post/side/side_tall 含む)
   - `models/item/` - 8 JSON files
   - `textures/block/` - 2 PNG files (temporal_stone, temporal_cobblestone)
6. **Loot tables**: 8 loot table JSON files (temporal_stone はシルクタッチ分岐あり)
7. **Mining tags**: `mineable/pickaxe` タグに追加
8. **Translation keys**: en_US, ja_JP

## Version Support

全バージョン（1.20.1〜1.21.11）に対応。各バージョンの ModBlocks/ModItems にエントリ追加。

## Out of Scope

- ワールド生成への組み込み
- 石レンガ・磨き石などの追加バリエーション
- レシピ（かまど精錬、ストーンカッター等）
- クリエイティブタブへの配置（既存パターンに従う）
