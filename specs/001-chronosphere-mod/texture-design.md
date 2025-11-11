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

---

## Detailed Design: Spatially Linked Pickaxe (空間連結ツルハシ)

### Item Concept

**Function**: Magic pickaxe that manipulates spatial connections to duplicate block drops from another dimension
**Performance**: Diamond pickaxe equivalent
**Special Effect**: 33% chance to double drops (spatial duplication)

### Visual Design

#### Basic Shape
```
Pickaxe Shape:
   ◇◇◇  ← Pick head (Made from Enhanced Clockstone)
     |   ← Handle (Wooden stick)
     |
     |
```

#### Color Palette

1. **Main Colors**
   - **Primary**: Cyan/Light Blue (Time manipulation magic energy)
   - **Accent**: Orange/Gold (#db8813 - Matches portal theme)
   - **Handle**: Brown (Wooden stick)

2. **Decorative Elements**
   - **Pick Head**: Enhanced Clockstone crystal material texture
   - **Energy Effect**: Faint glow/aura around the head
   - **Clock Motif**: Small gears or clock hands decoration (optional)
   - **Space Distortion**: Visual effect showing space warping around pick head

### Texture Options

#### Option 1: Diamond Pickaxe Base + Time Effect
```
Based on vanilla diamond_pickaxe.png:
- Pick Head: Cyan/Light Blue (Time magic)
- Handle: Standard brown (Wooden stick)
- Glow Effect: Orange/Gold glow on pick head edges
```

#### Option 2: Enhanced Clockstone Style
```
Emphasizing Enhanced Clockstone material:
- Pick Head: Same crystal texture as Enhanced Clockstone
- Clock hand decorations
- Energy lines running down the handle
```

### Technical Specifications

- **Format**: PNG image data, 16 x 16, 8-bit/color RGBA, non-interlaced
- **Location**: `common/src/main/resources/assets/chronosphere/textures/item/spatially_linked_pickaxe.png`
- **Model**: `common/src/main/resources/assets/chronosphere/models/item/spatially_linked_pickaxe.json`

### Creation Steps

1. **Reference Vanilla Texture**
   - Check Minecraft's assets/minecraft/textures/item/diamond_pickaxe.png

2. **Modify Colors**
   - Diamond blue → Brighter cyan/light blue
   - Add orange/gold accent (#db8813)

3. **Add Decorations (Optional)**
   - Small clock hands
   - Energy glow
   - Emphasize crystal texture

### Current Status

**Placeholder**: Currently using enhanced_clockstone.png as placeholder texture
**TODO**: Create custom texture based on the design above

---

## 8. Ancient Gear（古代の歯車）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/ancient_gear.png`

**コンセプト**:
- Master Clockダンジョンで収集するクエストアイテム
- 3個集めるとボス部屋への扉を開く鍵となる
- 古代の時計仕掛けメカニズムの部品
- Clockwork Sentinel（時計仕掛けの番兵）がドロップ

**デザイン方向性**:
- **ベース**: 歯車（ギア）の形状
- **カラー**: 
  - 金属的なブロンズ/真鍮色
  - オレンジ/ゴールド（#db8813）のアクセント
  - 酸化・経年劣化の表現（緑青、錆び）
- **特徴**:
  - はっきりとした歯車の歯（8-12個程度）
  - 中央に穴または軸受け部分
  - 古代の刻印や時計の文字盤模様
  - 金属の質感（光沢とシャドウ）
  - わずかな破損や摩耗（古代の遺物感）
- **雰囲気**: 
  - 古代文明の精密機械部品
  - 重厚で歴史を感じさせる
  - 機能的だが装飾性もある

**参考イメージ**:
- スチームパンク的な歯車
- 時計の内部メカニズム
- 古代ギリシャ・ローマの青銅製歯車（アンティキティラ島の機械）

**差別化ポイント**:
- Clockstone系: 結晶・鉱石
- Enhanced Clockstone: 精錬された結晶
- **Ancient Gear**: 機械部品・歯車

**Status**: ⚠️ TODO - 現在はclockstone.pngのコピー、要編集

**優先度**: 中（US3実装で必要）

