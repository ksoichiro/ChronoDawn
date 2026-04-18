# Temporal Sand & Gravel Block Set Design

## Overview

Chrono Dawn ディメンションで生成される砂・砂利・砂岩系を、青白テーマの Temporal 系カスタムブロックに置き換える。バニラ `red_sand` / `red_sandstone` は Temporal 版に統合し、青系統で見た目が被らない構成にする。

## Blocks (合計 6)

| Block | ID | Base Class | Replaces |
|---|---|---|---|
| Temporal Sand | `temporal_sand` | `FallingBlock` | `minecraft:sand`, `minecraft:red_sand` |
| Temporal Gravel | `temporal_gravel` | `GravelBlock` (1.21.1+) / `FallingBlock` (1.20.1) | `minecraft:gravel` |
| Temporal Sandstone | `temporal_sandstone` | `Block` | `minecraft:sandstone`, `minecraft:red_sandstone` |
| Temporal Sandstone Stairs | `temporal_sandstone_stairs` | `StairBlock` | — |
| Temporal Sandstone Slab | `temporal_sandstone_slab` | `SlabBlock` | — |
| Temporal Sandstone Wall | `temporal_sandstone_wall` | `WallBlock` | — |

派生は階段/ハーフ/壁のみ（chiseled / cut / smooth はスコープ外）。

## Block Properties

### Temporal Sand
- `mapColor(MapColor.SAND)`
- `strength(0.5f)`
- `sound(SoundType.SAND)`
- バニラ `sand` と同じ落下挙動・shovel 採掘

### Temporal Gravel
- `mapColor(MapColor.STONE)`
- `strength(0.6f)`
- `sound(SoundType.GRAVEL)`
- 1.21.1+ は `GravelBlock` 経由で flint ドロップ仕様も継承
- 1.20.1 は `FallingBlock` を直接拡張し、loot table 側で flint ドロップを定義

### Temporal Sandstone
- `mapColor(MapColor.SAND)`
- `strength(0.8f, 0.8f)`
- `requiresCorrectToolForDrops()`
- `sound(SoundType.STONE)`

### Variants (Stairs / Slab / Wall)
- 1.21.2+ は `Properties.ofFullCopy(Blocks.SANDSTONE_*)` + 各派生用 `setId(...)`
- 1.20.1 / 1.21.1 は `Properties.of()` + 明示プロパティ

## Textures

バニラの `sand.png`, `gravel.png`, `sandstone_*.png` をベースに：

1. グレースケール化（`L = 0.299R + 0.587G + 0.114B`）
2. ターゲット色 `#DCE6F0`（pale ice blue）で乗算（明部 weight 0.85、暗部 0.7 で締まりを残す）
3. `sand` と `red_sand` は同じ手順で同一出力 → `temporal_sand.png` 1ファイルに統合
4. `sandstone` は top / bottom / side の 3 テクスチャを同一手順で処理

`scripts/generate_temporal_sand_textures.py` として保存し再現可能にする（`assets/temporal_dirt/` 同様の運用）。

## Worldgen Integration

### (a) noise_settings/chronodawn.json の surface_rule 編集

11 バージョン × 各 24 occurrences（uniform）の置換：

| 旧 | 新 |
|---|---|
| `"Name": "minecraft:sand"` | `"Name": "chronodawn:temporal_sand"` |
| `"Name": "minecraft:red_sand"` | `"Name": "chronodawn:temporal_sand"` |
| `"Name": "minecraft:gravel"` | `"Name": "chronodawn:temporal_gravel"` |
| `"Name": "minecraft:sandstone"` | `"Name": "chronodawn:temporal_sandstone"` |
| `"Name": "minecraft:red_sandstone"` | `"Name": "chronodawn:temporal_sandstone"` |

**注意**: `"noise": "minecraft:gravel"`（noise プロバイダ名）は置換しない。Edit 時は `"Name": "minecraft:..."` のみ厳密マッチさせる。

### (b) disk configured_feature の更新

`common/shared-1.21.1+/.../configured_feature/sand_disk.json` と同 `gravel_disk.json`、加えて 1.20.1 版：

- `sand_disk.json`: `state_provider.fallback.state.Name` → `chronodawn:temporal_sand`、`target.blocks` の `minecraft:gravel` → `chronodawn:temporal_gravel`
- `gravel_disk.json`: `state_provider.fallback.state.Name` → `chronodawn:temporal_gravel`、target はそのまま

### (c) biome features

参照 ID（`chronodawn:sand_disk_placed` / `chronodawn:gravel_disk_placed`）は変更しないため biome JSON は無変更。

### (d) 1.21.11 構造差

memory「Dimension type JSON change in 1.21.11」は `dimension_type/` の話。`noise_settings/` の surface_rule 構造は 11 バージョンで一貫（24 occurrences 一致で確認）。

## Tags

各バージョンのタグディレクトリ（1.20.1 は `tags/blocks/`・`tags/items/`、1.21.1+ は `tags/block/`・`tags/item/`）に追加：

- `minecraft:sand` (block + item) ← `temporal_sand`
- `minecraft:gravel` (block + item) ← `temporal_gravel`
- `minecraft:sandstone_blocks` (block + item) ← `temporal_sandstone`
- `minecraft:walls` (block + item) ← `temporal_sandstone_wall`
- `minecraft:mineable/shovel` ← `temporal_sand`, `temporal_gravel`
- `minecraft:mineable/pickaxe` ← `temporal_sandstone`, `*_stairs`, `*_slab`, `*_wall`

タグ登録によりバニラの TNT・コンクリートパウダー・ガラス精錬・砂岩クラフト等のレシピがそのまま使える。

## Recipes

`common/1.20.1/.../recipes/`（plural） / `common/1.21.1/.../recipe/` / `common/shared-1.21.2+/.../recipe/`：

| Recipe | Type | Result |
|---|---|---|
| `temporal_sandstone.json` | shaped (2x2) | 4 temporal_sand → 1 temporal_sandstone |
| `temporal_sandstone_stairs.json` | shaped | 6 temporal_sandstone → 4 stairs |
| `temporal_sandstone_slab.json` | shaped | 3 temporal_sandstone → 6 slab |
| `temporal_sandstone_wall.json` | shaped | 6 temporal_sandstone → 6 wall |
| `temporal_sandstone_stairs_stonecutting.json` | stonecutting | 1 temporal_sandstone → 1 stairs |
| `temporal_sandstone_slab_stonecutting.json` | stonecutting | 1 temporal_sandstone → 2 slab |
| `temporal_sandstone_wall_stonecutting.json` | stonecutting | 1 temporal_sandstone → 1 wall |

memory「Recipe JSON format splits by version」に従い、1.20.1 / 1.21.1 / 1.21.2+ の 3 形式で正しく書き分ける（`result.item` vs `result.id`、ingredient のオブジェクト vs 文字列）。

## Loot Tables

`common/1.20.1/.../loot_tables/blocks/` と `common/shared-1.21.1+/.../loot_table/blocks/` に：

- `temporal_sand.json`: 単純 drop-self
- `temporal_gravel.json`: vanilla `gravel` 互換 — 通常は drop-self、shovel + chance(10% base, fortune で増加 14/25/100%) で flint をドロップ。シルクタッチ時は drop-self 確定。
- `temporal_sandstone.json`, `*_stairs.json`, `*_wall.json`: drop-self
- `temporal_sandstone_slab.json`: double=2 drops の特殊形（既存 `temporal_stone_slab.json` と同形式）

memory「Loot table silk touch predicate format (version-split)」に従い 1.20.1 と 1.21.1+ で predicate 形式を分ける。

## Code Registration

各バージョン共通：
- `common/shared/` の `ModBlockId.java`, `ModItemId.java` に 6 enum entry 追加（render layer SOLID）
- `common/<ver>/` の `ModBlocks.java` × 11 にブロック登録
- `common/<ver>/` の `ModItems.java` × 11 にアイテム登録（version 毎の 3 パターン）
- `ModItems.populateCreativeTab()` に 6 entries 追加（`CreativeTabCompletenessTest` 対策）

memory「humanoidArmor() and equipment-builder API is 1.21.5+ only」と同様、Block class は Group A/B/C で分けて 11 ファイル ずつ作成。

## Resource Layout

`common/shared/src/main/resources/assets/chronodawn/`:
- `textures/block/`: temporal_sand.png, temporal_gravel.png, temporal_sandstone_top.png, temporal_sandstone_bottom.png, temporal_sandstone.png (side)
- `blockstates/`: 6 ファイル
- `models/block/`: 階段 inner/outer、ハーフ top、壁 post/side/side_tall を含めて約 14 ファイル
- `models/item/`: 6 ファイル（壁は `wall_inventory` parent）

1.21.4+ Client Items JSON:
- `common/1.21.4/.../assets/chronodawn/items/`: 6 ファイル
- `common/shared-1.21.5+/.../assets/chronodawn/items/`: 6 ファイル

## Translations

各 lang ロケーション（1.20.1 / 1.21.1 / shared-1.21.2+）の en_us.json / ja_jp.json に 6 エントリ追加：

```
"block.chronodawn.temporal_sand": "Temporal Sand" / "テンポラルサンド"
"block.chronodawn.temporal_gravel": "Temporal Gravel" / "テンポラルグラベル"
"block.chronodawn.temporal_sandstone": "Temporal Sandstone" / "テンポラル砂岩"
"block.chronodawn.temporal_sandstone_stairs" / "..._slab" / "..._wall"
```

memory「Lang JSON line-based edit」に従い Edit ツールでアンカー指定して挿入（json.dumps による全書き換えは避ける）。

## Verification

```
./gradlew validateResources       # JSON 構文 + クロスリファレンス
./gradlew validateTranslations    # 翻訳キー版間整合
./gradlew build1_21_11            # 最新版で先行ビルド
./gradlew buildAll                # 全 11 版
./gradlew testAll                 # 含 CreativeTabCompletenessTest
./gradlew gameTestAll             # ランタイム検証
./gradlew checkAll                # フル検証（コミット前必須）
```

手動確認（`runClientFabric1_21_11`）：
1. クリエイティブタブに 6 ブロックが並ぶ
2. Chrono Dawn ディメンションの新規チャンクで砂・砂利・砂岩が Temporal 系に置換されている
3. Temporal Sand → Temporal Sandstone のクラフト動作
4. バニラ TNT レシピに Temporal Sand が使える（タグ互換確認）
5. Temporal Gravel をシャベルで採掘して flint がドロップする（fortune 補正含む）

## Side Effects & Risks

- **既存ワールドのチャンク**: 既生成チャンクは旧 `minecraft:sand` のまま残る。仕様として受容（新規探索が必要）
- **mob 生成**: vanilla husk 等の `#minecraft:sand` 参照 spawn rule はタグ登録で互換維持
- **red_sand / red_sandstone の喪失**: Chrono Dawn ディメンション内では消えるが、他ディメンションには影響なし
- **Subagent への委譲**: memory「Subagent-Driven Development: Watch for Unintended Edits」「Verify subagent commit claims」に従い、並列実行時は `git diff --name-only -- '*.java'` で意図しない変更を検出し、コミット後は controller 側で `git log` 確認

## Out of Scope

- chiseled / cut / smooth 砂岩バリエーション
- バニラ砂のように雷でガラス化するなどの特殊効果（今回は vanilla parity のみ）
- 既存ワールドのチャンク再生成
- バニラの `red_sand` / `red_sandstone` を Chrono Dawn 以外で置換すること
