# Temporal blocks vanilla recipe parity

**Status**: All stages complete.
**Start date**: 2026-04-15

## Goal

Temporal Stone / Temporal Cobblestone / Deepslate Temporal Stone / Cobbled Deepslate Temporal Stone をバニラの石・丸石・深層岩・深層岩丸石と**同等のクラフト素材**として使えるようにする。

## Scope decisions (user-confirmed)

1. **Cobbled deepslate 変種を追加**する（Stage 1 で完了）。
2. **石の見た目が残るクラフト成果物**（石ボタン、石の感圧板、石レンガ、レバー、磨かれた深層岩など）は **Temporal 系の新規アイテム**として追加する。原型と異なる成果物（ストーンカッター、リピーター、コンパレーター）は**バニラと同じアイテム**を成果物にする。
3. **石レンガ以降の派生**（mossy/cracked/chiseled stone_bricks, deepslate_tiles, deepslate_bricks, chiseled_deepslate の Temporal 版）は**今回スコープ外**。

## Stages

### Stage 1: Deepslate restructure ✅ COMPLETE (commit 00062acc)

- 既存 `deepslate_temporal_stone_{stairs,slab,wall}` を `cobbled_deepslate_temporal_stone_{stairs,slab,wall}` にリネーム。
- 新規 `cobbled_deepslate_temporal_stone` ベースブロックを追加（uniform cube_all）。
- 既存 `deepslate_temporal_stone` を raw deepslate 相当に再構築（`cube_column` モデル、シルク以外で cobbled をドロップ、新テクスチャは vanilla deepslate からの色変換）。
- 11 バージョン対応。

### Stage 2: polished_deepslate_temporal_stone + stairs/slab/wall ✅ COMPLETE

- `polished_deepslate_temporal_stone` ベースブロックを新規追加。
- stairs/slab/wall 変種も追加。
- 11 バージョン × block class + blockstate + model + texture + loot + tag + lang + items JSON。
- テクスチャは vanilla `polished_deepslate.png` に ChronoDawn 色変換（R×0.95, G×1.08, B×1.32）を適用して生成。

### Stage 3: temporal_stone_bricks + stairs/slab/wall ✅ COMPLETE

- `temporal_stone_bricks` ベースブロック + stairs/slab/wall 変種。
- 11 バージョン対応（Stage 2 と同じファイル種別）。
- テクスチャは vanilla `stone_bricks.png` に ChronoDawn 色変換（既存 temporal_stone で使用した transform を再適用）を適用して生成。

### Stage 4: temporal_stone_button + temporal_stone_pressure_plate ✅ COMPLETE

- ボタンブロック・感圧板ブロックを新規追加。
- 11 バージョン対応。`ButtonBlock` / `PressurePlateBlock` 系は vanilla API の差異に注意（1.21.2 以降で BlockSetType 指定が変わった）。
- テクスチャは `temporal_stone.png` を流用可能（parent モデルを使うだけなので不要な可能性大）。

### Stage 5: Tags for vanilla recipe hooks ✅ COMPLETE

タグに追加することで以下が自動的にクラフト可能になる:
- 石ツール5種（pickaxe/axe/sword/shovel/hoe）
- かまど

追加対象タグ（block + item 両方、1.20.1 の `tags/blocks/` と shared-1.21.1+ の `tags/block/` 両ディレクトリ形式）:
- `minecraft:stone_tool_materials` に `chronodawn:temporal_cobblestone`, `chronodawn:cobbled_deepslate_temporal_stone`
- `minecraft:stone_crafting_materials` に同上

### Stage 6: Recipes (direct + smelting) ✅ COMPLETE

3 つのレシピ JSON 形式世代（1.20.1 / 1.21.1 / 1.21.2+）別に作成する。

**Temporal 系成果物**（石の見た目が残るもの — 新規アイテムへ）:
- `temporal_stone_button` ← `temporal_stone`
- `temporal_stone_pressure_plate` ← 2× `temporal_stone`
- `temporal_stone_bricks` ×4 ← 4× `temporal_stone`
- `minecraft:lever` ← `temporal_cobblestone` + stick（＊成果物はバニラでよいので例外）
- `polished_deepslate_temporal_stone` ×4 ← 4× `cobbled_deepslate_temporal_stone`

**バニラ成果物**（原型と異なるもの — バニラのアイテムを出す）:
- `minecraft:stonecutter` ← `temporal_stone` + iron ingot
- `minecraft:repeater` ← `temporal_stone` + redstone + 3× stick
- `minecraft:comparator` ← `temporal_stone` + redstone_torch + quartz

**精錬レシピ**:
- `temporal_cobblestone` → `temporal_stone`（1 XP, 200 tick）
- `cobbled_deepslate_temporal_stone` → `deepslate_temporal_stone`（0.1 XP, 200 tick、vanilla cobbled_deepslate 精錬に合わせる）

## Implementation reference

- 手順の詳細は `.claude/skills/add-terrain-block` スキルを参照。
- Stage 1 の変更パターン（参考）: commit `00062acc`
- 3 つのレシピ JSON 形式世代のサンプル: `common/1.20.1/.../recipes/mossy_temporal_cobblestone.json`, `common/1.21.1/.../recipe/mossy_temporal_cobblestone.json`, `common/shared-1.21.2+/.../recipe/mossy_temporal_cobblestone.json`

## Verification per Stage

各 Stage 完了時に以下を実行してからコミット:
- `./gradlew validateResources`
- `./gradlew validateTranslations`
- `./gradlew build1_21_11 -x test` （Group C: Identifier パターン検証）
- `./gradlew build1_20_1 -x test` （Group A: Properties.of パターン検証）
- `./gradlew :common-1.21.11:test`（CreativeTabCompletenessTest 含む）

可能なら最後に `./gradlew checkAll` で全バージョン確認（多版面での silent break 防止）。

## Known separate follow-up (Stage 外)

**1.20.1 の silk-touch 述語フォーマット不整合**: `common/1.20.1/.../loot_tables/blocks/` 配下の複数ファイルが新形式（`predicates.minecraft:enchantments`）で書かれており、1.20.1 ではサイレントに動作しない可能性。

候補ファイル:
- `temporal_stone.json`
- `deepslate_temporal_stone.json`（Stage 1 で追加）
- `deepslate_temporal_redstone_ore.json`
- `frozen_time_ice.json`
- `time_crystal_ore.json`
- `clockstone_ore.json`（要確認）

詳細はメモリ `feedback_loot_table_silk_touch_format.md` 参照。
