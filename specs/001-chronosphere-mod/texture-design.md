# Texture Design Guide - Chronosphere Mod

**Created**: 2025-11-02
**Purpose**: アイテムテクスチャのコンセプトとデザイン指針

---

## Color Theme

**Chronosphere Modのテーマカラー**:
- **メインカラー**: `#db8813` (RGB: 219, 136, 19) - オレンジ/ゴールド
- ポータルの色と統一し、時計や時間のテーマに合致

---

## 1. Clockstone（クロックストーン）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/clockstone.png`

**コンセプト**:
- 基本的な時間の力を持つ鉱石
- オーバーワールドの古代遺跡やクロノスフィアで採掘可能
- 全ての時間操作アイテムの基礎素材

**デザイン**:
- 淡いオレンジ/ゴールド
- 粗削りな結晶
- Amethyst Shardをベースにした形状

**Status**: ✅ Already implemented

---

## 2. Enhanced Clockstone（強化クロックストーン）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/enhanced_clockstone.png`

**コンセプト**:
- 砂漠の時計塔で入手できる上位素材
- Time Clock、Spatially Linked Pickaxe、Unstable Hourglassなどのクラフト材料
- 通常のクロックストーンより高度な時間操作の力を秘めている
- 古代の時計塔に保管されていた貴重な素材

**デザイン方向性**（推奨：ハイブリッド型）:
- **ベース**: `clockstone.png`を改変
- **カラー**:
  - 通常版より濃く鮮やかなオレンジ/ゴールド
  - `#db8813`を強調
  - わずかに発光感
- **特徴**:
  - より精錬された外観（結晶が整っている）
  - 内部からの発光効果
  - 表面に時計の歯車や文字盤の模様が浮かび上がる
  - わずかなグロー効果（オプション）
- **雰囲気**:
  - レア度が高い貴重な素材
  - 通常版より明らかに「強化されている」ことが視覚的に分かる
  - 古代文明の高度な技術の産物

**通常版との差別化**:
- Clockstone: 淡いオレンジ、粗削り
- Enhanced: 濃いオレンジ、精錬済み + 発光 + メカニカル模様

**Status**: ⚠️ TODO - 現在はclocksone.pngのコピー、要編集

---

## 3. Time Clock（タイムクロック）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/time_clock.png`

**コンセプト**:
- 周囲のMobの攻撃AIをキャンセルする時間操作デバイス
- 懐中時計や置時計のイメージ
- クールダウン10秒で使用できる実用的なツール

**デザイン方向性**:
- **ベース**: Minecraftの`clock.png`（バニラの時計）
- **カラー**: オレンジ/ゴールド（#db8813）でポータルと統一感
- **特徴**:
  - 時計の文字盤と針が見える
  - メカニカルな印象
  - 少し発光感を持たせて「時間操作」の魔法的な要素を表現
- **雰囲気**: 実用的なツール、精密機械

**Status**: ⚠️ TODO - 現在はenhanced_clockstone.pngのコピー、要編集

---

## 4. Key to Master Clock（マスタークロックへの鍵）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/key_to_master_clock.png`

**コンセプト**:
- 時の番人（中ボス）を倒して入手する重要なキーアイテム
- マスタークロック最深部への扉を開く
- 古代の時計塔にふさわしい荘厳さ

**デザイン方向性**:
- **ベース**: 古典的な鍵の形状（歯車モチーフ）
- **カラー**: ゴールド/ブロンズ（#db8813 + darker tones）
- **特徴**:
  - 鍵の先端に時計の歯車デザイン
  - 持ち手部分に時計の文字盤や針のシルエット
- **雰囲気**:
  - 古代の遺物
  - 重厚感
  - Boss撃破の報酬にふさわしい存在感

**Status**: ⚠️ TODO - 現在はportal_stabilizer.pngのコピー、要編集

---

## 5. Unstable Hourglass（不安定な砂時計）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/unstable_hourglass.png`

**コンセプト**:
- クラフト時に反転共鳴（危険な効果）を発動する
- Time Hourglassの上位版だが「不安定」
- リスクを伴う強力な素材

**デザイン方向性**:
- **ベース**: `time_hourglass.png`を改変
- **カラー**:
  - オレンジ/ゴールド（基本）
  - **不安定さを表現**: 赤みがかった色（#db8813 → darker/redder tones）
  - または紫/ピンクのグラデーションを追加
- **特徴**:
  - ガラス部分にひび割れ模様
  - 内部の砂が渦巻いている/混沌とした感じ
  - わずかに歪んだ輪郭（不安定さの表現）
  - パーティクル的な効果（オプション）
- **雰囲気**:
  - Time Hourglassより危険で不穏
  - パワフルだが制御が難しい

**Status**: ⚠️ TODO - 現在はtime_hourglass.pngのコピー、要編集

---

## Visual Hierarchy（視覚的ヒエラルキー）

アイテムの重要度・希少性を色の濃さと効果で表現:

1. **Clockstone**: 基本素材 - 淡いオレンジ/ゴールド
2. **Enhanced Clockstone**: 強化素材 - 濃いオレンジ/ゴールド + 発光
3. **Time Clock**: ツール - ゴールド + メカニカル + わずかな発光
4. **Key to Master Clock**: ボス報酬 - 重厚なゴールド/ブロンズ
5. **Unstable Hourglass**: 危険な素材 - オレンジ/ゴールド + 赤/紫の不安定な色

このヒエラルキーにより、プレイヤーが一目で価値と用途を判断できるようにする。

---

## Design Principles

1. **Color Consistency**: 全アイテムで`#db8813`（オレンジ/ゴールド）をベースカラーとして使用
2. **Differentiation**: 各アイテムの用途や希少性に応じて、発光、模様、追加色で差別化
3. **Minecraft Style**: バニラMinecraftのアートスタイル（16x16ピクセル、シンプルな形状）を踏襲
4. **Readability**: 小さいアイコンでも認識できる明確なシルエット
5. **Thematic Unity**: 時計/時間/歯車モチーフで統一感を持たせる

---

## Implementation Status

- ✅ Clockstone - 完成済み
- ⚠️ Enhanced Clockstone - コピーのまま、要編集
- ⚠️ Time Clock - コピーのまま、要編集
- ⚠️ Key to Master Clock - コピーのまま、要編集
- ⚠️ Unstable Hourglass - コピーのまま、要編集

---

## Notes

- テクスチャ編集後、`./gradlew build`で確認
- ゲーム内でアイテムを実際に見て、視認性と統一感を確認することを推奨
- 必要に応じて明度・彩度を調整
