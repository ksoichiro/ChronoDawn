# Future Features - Chrono Dawn Mod

このドキュメントは、計画されているが未実装の機能を記録します。

---

## Crop System Implementation (T211-T215)

**作成日**: 2025-11-17
**ステータス**: 計画段階 - 未実装
**目的**: ディメンション内での食料調達手段を追加し、長期滞在やサバイバルプレイを可能にする

### 現在の食料システム

**既に実装済み:**
- Time Wheat (作物) - ✅ 実装済み
- Time Bread (食料) - ✅ 実装済み
- Fruit of Time (食料) - ✅ 実装済み（Time Woodの木に生える）
- Time Fruit Pie (食料) - ✅ 実装済み
- Time Jam (食料) - ✅ 実装済み

### ギャップ分析

現在の食料システムは実装済みだが、より多様な作物システムを追加することで、以下のメリットがある:
1. 食料の多様性向上
2. 異なるゲームプレイメカニクス（農地作物、茎作物、キノコ）
3. 長期滞在のサステナビリティ向上

---

## 設計: Multiple Diverse Crops

### デザイン哲学

1. **既存カバレッジ:**
   - 小麦タイプ作物: ✅ Time Wheat（農地に植える）
   - 木の実: ✅ Fruit of Time（木に生える、ベリー風）
   - 根菜: ❌ 未実装
   - メロンタイプ: ❌ 未実装
   - キノコ: ✅ Unstable Fungus（食用不可） - 食用バリアント必要

2. **デザイン原則:**
   - **重複回避**: Fruit of Timeが既に「ベリー」ニッチを埋めている
   - **多様性の最大化**: 異なる作物タイプ = 異なるゲームプレイメカニクス
   - **時間テーマ**: 各作物が時間的/年代的な繋がりを持つ

3. **作物タイプ分布:**
   - **農地作物**: Time Wheat（既存）、Temporal Root（新規）
   - **茎作物**: Chrono Melon（新規）
   - **暗所育成**: Timeless Mushroom（新規、Unstable Fungusとは別）
   - **木の作物**: Fruit of Time（既存）

---

## Crop 1: Temporal Root（時の根菜）- Root Vegetable

**バニラリファレンス**: ニンジン / ジャガイモ
**時間テーマ**: "時の根" - 地下で育つ、古代/深い時間の象徴

### ブロックプロパティ
- **名前**: Temporal Root Crop（時の根菜）
- **タイプ**: 農地作物（ニンジン/ジャガイモと同様）
- **成長段階**: 8段階（0-7）、ニンジンと同じ
- **配置**: 農地に植える必要あり
- **光要件**: 成長には光レベル9以上
- **成長速度**: 通常のランダムティック成長

### 収穫動作
- **段階0-6**: Temporal Root Seeds 1個のみドロップ
- **段階7（成熟）**: Temporal Roots 2-4個 + Temporal Root Seeds 0-2個
- **幸運効果**: あり（ドロップ数増加）

### アイテム

**Temporal Root（時の根菜）:**
- **タイプ**: 食料アイテム（生野菜）
- **栄養**: 3満腹度（1.5ドラムスティック）
- **満腹度回復**: 0.6（合計1.8）
- **効果**: なし（生状態）
- **用途**: 生で食べるか調理可能

**Baked Temporal Root（焼いた時の根菜）:**
- **タイプ**: 食料アイテム（調理済み）
- **栄養**: 6満腹度（3ドラムスティック）
- **満腹度回復**: 0.6（合計3.6）
- **効果**: Regeneration I for 5秒（100%確率）
- **クラフト**: かまど/燻製器でTemporal Rootを精錬
- **用途**: 生より良い、回復効果付き

### ワールドジェン配置
- **バイオーム**: chronodawn_plains（レアパッチ）
- **頻度**: レア（村の野生ニンジン/ジャガイモのように）
- **配置ルール**: 水の近くの農地に小さなパッチ（2-4作物）

---

## Crop 2: Chrono Melon（時のメロン）- Stem Crop

**バニラリファレンス**: スイカ / カボチャ
**時間テーマ**: "結晶化した時間" - メロンが固体形態の時間の本質を含む

### ブロックプロパティ
- **名前**: Chrono Melon（時のメロン）
- **タイプ**: 茎作物（スイカ/カボチャと同様）
- **成長段階**:
  - 茎: 8段階（0-7）
  - メロンブロック: 時間テーマのテクスチャを持つフルブロック
- **配置**: 茎は農地、メロンは隣接する土/草に生える
- **光要件**: 茎の成長には光レベル9以上
- **成長速度**: 通常のランダムティック成長

### 収穫動作
- **茎**: 破壊しても何もドロップしない
- **メロンブロック**: 破壊時にChrono Melon Slices 3-7個ドロップ
- **シルクタッチ**: フルメロンブロックをドロップ
- **幸運効果**: あり（スライスドロップ増加）

### アイテム

**Chrono Melon Seeds（時のメロンの種）:**
- **タイプ**: 種（植えられるアイテム）
- **入手**: クラフト（1 Chrono Melon Slice → 1 Seeds）

**Chrono Melon Slice（時のメロンの切れ端）:**
- **タイプ**: 食料アイテム
- **栄養**: 2満腹度（1ドラムスティック）
- **満腹度回復**: 0.3（合計0.6）
- **効果**: なし
- **用途**: 素早い食料、または他のアイテムにクラフト

**Chrono Melon Block（時のメロン・ブロック）:**
- **タイプ**: ブロックアイテム（装飾 + クラフト可能）
- **用途**: 装飾、または9スライスにクラフト

### ワールドジェン配置
- **バイオーム**: chronodawn_plains、chronodawn_forest
- **頻度**: やや珍しいパッチ
- **配置ルール**: 小さなメロンパッチ（カボチャパッチのように）

---

## Crop 3: Timeless Mushroom（時知らずのキノコ）- Fungus

**バニラリファレンス**: 茶/赤キノコ
**時間テーマ**: "時知らず" - 暗闇で育つ、通常の時間の流れの外に存在
**視覚的差別化**: Unstable Fungus（紫/青 vs 茶/赤）とは異なる

### ブロックプロパティ
- **名前**: Timeless Mushroom（時知らずのキノコ）
- **タイプ**: キノコブロック
- **成長段階**: 段階なし（即座に配置）
- **配置**: 土、草、菌糸、ポドゾル（バニラキノコと同様）
- **光要件**: **光レベル12以下**（暗闇/薄暗い光で育つ）
- **拡散**: 暗闇で近くのブロックに拡散可能（バニラキノコと同様）
- **成長速度**: 遅い自然拡散

### ビジュアルデザイン
- **色**: 銀/白で微かな光（紫のUnstable Fungusとは別）
- **サイズ**: 小さなキノコ（バニラと同じ）
- **テーマ**: "時知らず" = 淡い、幽玄、幽霊のような

### 収穫動作
- **破壊**: Timeless Mushroom アイテム1個ドロップ
- **骨粉**: 巨大キノコに成長不可（バニラとの差別化）

### アイテム

**Timeless Mushroom（時知らずのキノコ）:**
- **タイプ**: 食料アイテム（植えられる）
- **栄養**: 2満腹度（1ドラムスティック）
- **満腹度回復**: 0.3（合計0.6）
- **効果**: なし（生状態）
- **用途**: 生で食べる、シチューに使用、または植える

### ワールドジェン配置
- **バイオーム**: chronodawn_forest（暗い場所）、chronodawn_cave エリア
- **頻度**: 暗所でやや珍しい
- **配置ルール**: 低光量エリアの小さなクラスター（バニラキノコと同様）

---

## Additional Food Items

### 新作物からのクラフト食料

**1. Temporal Root Stew（時の根菜シチュー）:**
- **レシピ**: 1x Baked Temporal Root + 1x Timeless Mushroom + 1x Bowl → 1x Temporal Root Stew
- **栄養**: 8満腹度（4ドラムスティック）
- **満腹度回復**: 0.6（合計4.8）
- **効果**: Regeneration II for 10秒（100%確率）
- **用途**: 複数の作物を組み合わせた高価値回復食料

**2. Glistening Chrono Melon（きらめく時のメロン）:**
- **レシピ**: 1x Chrono Melon Slice + 8x Gold Nuggets → 1x Glistening Chrono Melon
- **栄養**: 2満腹度（1ドラムスティック）
- **満腹度回復**: 1.2（合計2.4） - 非常に高い満腹度回復
- **効果**: Absorption I for 30秒（100%確率）
- **用途**: 醸造用プレミアム材料または高満腹度スナック

**3. Chrono Melon Juice（時のメロンジュース）:**
- **レシピ**: 4x Chrono Melon Slices + 1x Glass Bottle → 1x Chrono Melon Juice
- **栄養**: 4満腹度（2ドラムスティック）
- **満腹度回復**: 0.4（合計1.6）
- **効果**: Speed I for 60秒（100%確率）
- **用途**: 飲料食料（ハチミツボトルのように）+ 移動バフ

**4. Timeless Mushroom Soup（時知らずのキノコスープ）:**
- **レシピ**: 2x Timeless Mushrooms + 1x Bowl → 1x Timeless Mushroom Soup
- **栄養**: 6満腹度（3ドラムスティック）
- **満腹度回復**: 0.6（合計3.6）
- **効果**: Night Vision for 60秒（100%確率）
- **用途**: 視覚強化付き探索食料

### Time Wheat強化（候補4）

**5. Enhanced Time Bread（強化された時のパン）:**
- **レシピ**: 3x Time Wheat + 1x Temporal Root → 1x Enhanced Time Bread
- **栄養**: 7満腹度（3.5ドラムスティック）
- **満腹度回復**: 0.8（合計5.6）
- **効果**: Regeneration I for 10秒（100%確率）
- **用途**: 小麦と根菜作物を組み合わせた改良パン

**6. Time Wheat Cookie（時の小麦クッキー）:**
- **レシピ**: 2x Time Wheat + 1x Chrono Melon Slice → 8x Time Wheat Cookies
- **栄養**: 2満腹度（1ドラムスティック）各
- **満腹度回復**: 0.4（合計0.8）
- **効果**: なし
- **用途**: 効率的なスナック食料（レシピからの高収量）

**7. Golden Time Wheat（黄金の時の小麦）:**
- **レシピ**: 1x Time Wheat + 8x Gold Nuggets → 1x Golden Time Wheat
- **栄養**: 3満腹度（1.5ドラムスティック）
- **満腹度回復**: 2.4（合計7.2） - 極めて高い満腹度回復
- **効果**: Regeneration II for 5秒 + Absorption I for 30秒（100%確率）
- **用途**: 究極のプレミアム材料（醸造でゴールデンキャロットの代替可能）

---

## Food System Summary

### 完全な食料&作物リスト（T211-T215後）

#### 既存の食料（既に実装済み）
| 食料アイテム | 栄養 | 満腹度回復 | 効果 | 入手方法 |
|-----------|-----------|------------|--------|-------------|
| Time Wheat | 1 | 0.6 | なし | 農業（作物） |
| Time Bread | 5 | 0.6 | なし | クラフト（3x Time Wheat） |
| Fruit of Time | 4 | 0.6 | Haste I (30s) | 木の収穫 |
| Time Fruit Pie | 8 | 0.3 | Haste II (30s) | クラフト |
| Time Jam | 4 | 0.5 | Speed I (60s) | クラフト |

#### 新しい作物&生食料 ⭐
| 食料アイテム | 栄養 | 満腹度回復 | 効果 | 入手方法 |
|-----------|-----------|------------|--------|-------------|
| **Temporal Root** | 3 | 0.6 | なし | 農業（根菜作物） |
| **Baked Temporal Root** | 6 | 0.6 | Regen I (5s) | 精錬 |
| **Chrono Melon Slice** | 2 | 0.3 | なし | メロン収穫 |
| **Timeless Mushroom** | 2 | 0.3 | なし | 採集（暗所） |

#### 新しいクラフト食料 ⭐
| 食料アイテム | 栄養 | 満腹度回復 | 効果 | 入手方法 |
|-----------|-----------|------------|--------|-------------|
| **Temporal Root Stew** | 8 | 0.6 | Regen II (10s) | クラフト |
| **Glistening Chrono Melon** | 2 | 1.2 | Absorption I (30s) | クラフト + ゴールド |
| **Chrono Melon Juice** | 4 | 0.4 | Speed I (60s) | クラフト（ボトル） |
| **Timeless Mushroom Soup** | 6 | 0.6 | Night Vision (60s) | クラフト |
| **Enhanced Time Bread** | 7 | 0.8 | Regen I (10s) | クラフト |
| **Time Wheat Cookie** | 2 | 0.4 | なし | クラフト（8個） |
| **Golden Time Wheat** | 3 | 2.4 | Regen II (5s) + Absorption I (30s) | クラフト + ゴールド |

⭐ = T211-T215からの新アイテム

---

## Implementation Plan

### T211: ✅ デザイン完了

すべての作物と食料のデザインが文書化されている。

### T212: Crop Blocks and Base Items実装

**ブロック:**
1. `TemporalRootBlock.java` - ニンジン風作物ブロック（8段階）
2. `ChronoMelonStemBlock.java` - メロン茎ブロック（8段階）
3. `ChronoMelonBlock.java` - フルメロンブロック
4. `TimelessMushroomBlock.java` - キノコブロック（拡散可能）

**基本食料アイテム:**
1. `TemporalRootItem.java` - 生根菜（食料）
2. `BakedTemporalRootItem.java` - 調理済み根菜（Regen効果付き）
3. `ChronoMelonSliceItem.java` - メロンスライス（食料）
4. `TimelessMushroomItem.java` - キノコ（食料 + 植えられる）

**種:**
1. `TemporalRootItem.java` - 植えられるアイテム（ニンジンのように食料アイテムと同じ）
2. `ChronoMelonSeedsItem.java` - 種（スライスからクラフト）

**ブロックアイテム:**
1. `ChronoMelonBlock` アイテム - 装飾/クラフト可能なメロンブロック

### T213: Create Crop Textures

**ブロックテクスチャ:**
- `temporal_root_stage_0.png`から`temporal_root_stage_7.png`（8段階）
- `chrono_melon_stem_stage_0.png`から`chrono_melon_stem_stage_7.png`（8段階）
- `chrono_melon_side.png`、`chrono_melon_top.png`、`chrono_melon_bottom.png`
- `timeless_mushroom.png`（単一キノコ）

**アイテムテクスチャ:**
- `temporal_root.png`（生根菜）
- `baked_temporal_root.png`（調理済み根菜）
- `chrono_melon_slice.png`（メロンスライス）
- `chrono_melon_seeds.png`（種）
- `timeless_mushroom.png`（キノコアイテム）

### T214: Add Worldgen Placement

**Configured Features:**
- `temporal_root_patch.json` - 根菜付き農地パッチ
- `chrono_melon_patch.json` - メロンパッチ（茎 + メロン）
- `timeless_mushroom_patch.json` - 暗所キノココラスター

### T215: Implement Crafted Food Items

**食料アイテム（7新アイテム）:**
1. `TemporalRootStewItem.java` - Regen II付きシチュー
2. `GlisteningChronoMelonItem.java` - ゴールド強化メロン
3. `ChronoMelonJuiceItem.java` - 飲料ジュース（ボトル）
4. `TimelessMushroomSoupItem.java` - Night Vision付きスープ
5. `EnhancedTimeBreadItem.java` - Regen付き改良パン
6. `TimeWheatCookieItem.java` - 効率的なスナック（8個/クラフト）
7. `GoldenTimeWheatItem.java` - プレミアムゴールド強化小麦

---

## Summary Statistics

**新作物**: 3タイプ（Temporal Root、Chrono Melon、Timeless Mushroom）
**新食料アイテム**: 合計11アイテム
  - 4基本食料（生/調理済み作物）
  - 7クラフト食料（シチュー、ジュース、強化アイテム）
**コードファイル**: 16 Javaファイル（4ブロック + 12アイテム）
**テクスチャ**: ~30テクスチャファイル
**データファイル**: ~20 JSONファイル（worldgen、レシピ、ルートテーブル、モデル）

---

*最終更新: 2025-12-15*
