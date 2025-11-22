# Texture Design Guide - Chronosphere Mod

**Created**: 2025-11-02
**Purpose**: アイテムテクスチャのコンセプトとデザイン指針

---

## Color Theme

**Chronosphere Modのテーマカラー**:
- **メインカラー**: `#db8813` (RGB: 219, 136, 19) - オレンジ/ゴールド
- ポータルの色と統一し、時計や時間のテーマに合致

**配色統一の設計方針** (2025-11-16 決定):
- **素材と装備の配色統一**: 装備は素材の色を反映する
  - Fragment of Stasis Core（紫系）→ Time Tyrant's Mail、Echoing Time Boots（紫系）
  - Chronoblade も Fragment から作成されるため紫系に統一予定
  - **理由**: 素材から装備を鍛造する際、素材の力（色）が装備に宿るという一貫した世界観
  - **参考色範囲**: `#2C1F1F` (濃い紫黒) → `#6B52BF` (明るい紫)
  - **RGB変換パラメータ**: R×0.42, G×0.32, B×0.75 (Clockstone ベースから紫系への変換)

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

## 6. Ancient Gear（古代の歯車）

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

---

## Block Textures (T240-249)

### Overview

T240-249で追加された装飾・建築用ブロックのテクスチャデザインガイド。
すべてのブロックはChronosphere次元のテーマカラー（紫/青）を基調とする。

**Recommended Texture Size**: 16x16 pixels (Minecraft standard)

**Color Palette for Chronosphere**:
- **Primary**: Purple (`#9B59B6`), Blue (`#3498DB`)
- **Secondary**: Light Blue (`#5DADE2`), Teal
- **Metallic**: Copper, Bronze, Antique Gold
- **Ice**: Ice blue with purple tint
- **Organic**: Green with purple/blue highlights

---

## 7. Clockwork Block（歯車ブロック）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/block/clockwork_block.png`

**Concept**: Steampunk/Mechanical clock themed decorative block

**Design**:
- **Theme**: Multiple gears visible on metal surface
- **Color**: Metallic gray, bronze, copper tones (MapColor.METAL)
- **Texture**: Hard, metallic surface with visible mechanical components
- **Animation**: Rotating gears (can be implemented with .mcmeta file)
- **Details**:
  - 3-4 gears of different sizes
  - Clock hands or mechanism parts visible
  - Rivets or bolts on the edges

**Sound**: METAL (metallic sound)

**Reference**:
- Vanilla `observer.png` (mechanical feel)
- Vanilla `lodestone_top.png` (decorative metal)

**Properties**:
- Hardness: 3.5
- Requires: Pickaxe
- Light: None

**Current Status**: ⚠️ Placeholder (copy of clockstone_block.png)

---

## 8. Time Crystal Block（時間水晶ブロック）

**ファイル（2層構造）**:
- `common/src/main/resources/assets/chronosphere/textures/block/time_crystal_block_outer.png` - 外側のガラス層
- `common/src/main/resources/assets/chronosphere/textures/block/time_crystal_block_core.png` - 内側の発光コア

**Concept**: Multi-layer glowing crystal block - beacon-like appearance with inner core

**Design Architecture**:
- **2-Layer Structure**:
  - **Outer Layer**: Transparent glass shell (16x16x16 full cube)
  - **Inner Layer**: Glowing core (10x10x10 cube centered inside)
- **Color**: Light blue (MapColor.COLOR_LIGHT_BLUE)
- **Glow**: Emits light level 10 (bright ambient light)

**Layer 1: Outer Glass Shell** (`time_crystal_block_outer.png`):
- **Purpose**: Transparent protective layer like beacon glass
- **Texture**: Glass-like with subtle crystalline patterns
- **Transparency**: High alpha (very transparent, ~70-80% transparent)
- **Color**: Light blue tint with slight opacity
- **Details**:
  - Thin frame or edges visible
  - Crystal facets on surface
  - Subtle sparkles or reflections
  - Similar to stained glass but more refined

**Layer 2: Inner Core** (`time_crystal_block_core.png`):
- **Purpose**: Bright glowing center (light source)
- **Texture**: Solid, bright, energy-like
- **Transparency**: None (fully opaque)
- **Color**: Bright cyan/light blue, very luminous
- **Details**:
  - Concentrated energy or crystal in center
  - Radial glow pattern from center
  - Can have geometric patterns (hexagons, circles)
  - Similar to Sea Lantern core but more crystalline

**Sound**: GLASS (glass/crystal sound)

**Reference**:
- Vanilla `beacon.png` (beacon glass structure)
- Vanilla `sea_lantern.png` (glowing effect)
- Vanilla `light_blue_stained_glass.png` (transparent glass)
- Vanilla `amethyst_block.png` (crystalline texture)

**Properties**:
- Hardness: 3.0
- Requires: Pickaxe
- Light: Level 10
- **Transparency**: Translucent rendering (like stained glass)

**Rendering**:
- Uses `RenderType.translucent()` for glass-like transparency
- **Multi-layer JSON model**: Inner core (6x6x6) + Outer shell (16x16x16)
- Outer texture must have alpha channel for transparency
- Inner texture is opaque and bright

**Crafting**: 9x Time Crystal → 1x Time Crystal Block

**Texture Creation Tips**:

**For Outer Layer** (`time_crystal_block_outer.png`):
1. Create 16x16 RGBA image
2. Base: Light blue color (#5DADE2)
3. Alpha: 70-80% transparent overall
4. Edges: Slightly more opaque (frame effect)
5. Add subtle crystalline facets or geometric patterns
6. Keep it minimal - this is the glass shell

**For Inner Core** (`time_crystal_block_core.png`):
1. Create 16x16 RGB or RGBA image (no transparency needed)
2. Base: Very bright cyan/light blue (#00FFFF or #5DADE2 at max brightness)
3. Center: Brightest point
4. Edges: Gradually darker (radial gradient)
5. Add geometric patterns (optional):
   - Hexagonal crystal structure
   - Concentric circles
   - Energy burst pattern
6. Should look like compressed glowing energy

**Visual Result**:
When both layers combine, it should look like a glass cube with a glowing crystal/energy core floating in the center - similar to a beacon or containment field.

**Current Status**: ⚠️ Placeholder (both layers use copy of clockstone_block.png)

---

## 9. Temporal Bricks（時のレンガ）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/block/temporal_bricks.png`

**Concept**: Time-themed brick building block

**Design**:
- **Theme**: Brick pattern with clock/time decorations
- **Color**: Purple/Blue base (MapColor.COLOR_PURPLE) - Chronosphere theme color
- **Texture**: Stone brick pattern with time motifs
- **Details**:
  - Standard brick layout (similar to Stone Bricks)
  - Each brick has subtle clock face or hands decoration
  - Purple-blue gradient
  - Slightly weathered/ancient appearance

**Sound**: STONE (stone sound)

**Reference**:
- Vanilla `stone_bricks.png` (brick pattern) + purple/blue color scheme

**Properties**:
- Hardness: 2.5
- Requires: Pickaxe
- Light: None

**Crafting**: 4x Clockstone → 1x Temporal Bricks (2x2)

**Variants**:
- Temporal Bricks Stairs
- Temporal Bricks Slab
- Temporal Bricks Wall

**Current Status**: ⚠️ Placeholder (copy of clockstone_block.png)

---

## 10. Temporal Moss（時の苔）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/block/temporal_moss.png`

**Concept**: Time-affected moss block exclusive to swamp biome

**Design**:
- **Theme**: Organic moss with temporal influence
- **Color**: Green base (MapColor.COLOR_GREEN) with purple/blue highlights
- **Texture**: Moss-like organic texture with mysterious feel
- **Details**:
  - Similar to vanilla moss but with magical twist
  - Purple/blue tinted areas
  - Slightly glowing spots (optional)
  - More mystical than vanilla moss

**Sound**: MOSS_CARPET (moss sound, same as vanilla moss)

**Reference**:
- Vanilla `moss_block.png` as base + time theme color overlay

**Properties**:
- Hardness: 0.1 (very soft)
- Tool: Any tool or hand (hoe is fastest)
- Light: None
- Special: Spreads to adjacent blocks, bonemeal-able

**Biome**: chronosphere_swamp exclusive

**Current Status**: ⚠️ Placeholder (copy of unstable_fungus.png)

---

## 11. Frozen Time Ice（凍結時間の氷）

**ファイル（2層構造）**:
- `common/src/main/resources/assets/chronosphere/textures/block/frozen_time_ice_outer.png` - 外側の透明な氷層
- `common/src/main/resources/assets/chronosphere/textures/block/frozen_time_ice_core.png` - 内側の凍結結晶コア

**Concept**: Multi-layer frozen ice block - transparent ice with frozen crystal inside

**Design Architecture**:
- **2-Layer Structure**:
  - **Outer Layer**: Transparent ice shell (16x16x16 full cube)
  - **Inner Layer**: Frozen crystal core (cross model - X-shaped intersecting planes)
- **Core Shape**: Cross pattern (like flowers/mushrooms) - two intersecting vertical planes
- **Color**: Ice blue (MapColor.ICE) with purple/blue tint
- **Special**: Never melts, slippery surface

**Layer 1: Outer Ice Shell** (`frozen_time_ice_outer.png`):
- **Purpose**: Transparent ice layer like vanilla ice
- **Texture**: Ice-like with high transparency
- **Transparency**: High alpha (very transparent, ~70-80% transparent)
- **Color**: Ice blue with purple/blue tint (#9B59B6 or #3498DB)
- **Details**:
  - Similar to vanilla ice texture
  - Purple/blue color filter applied
  - Slight frost patterns on surface
  - More mystical than vanilla ice

**Layer 2: Inner Crystal Core** (`frozen_time_ice_core.png`):
- **Purpose**: Frozen-in-time crystal suspended in ice - displayed as cross pattern
- **Texture**: Vertical plane texture (will be shown on X-shaped intersecting planes)
- **Model**: Cross pattern (like mushrooms/flowers) - two perpendicular vertical planes
- **Transparency**: Semi-transparent (40-50% transparent)
- **Color**: Deeper purple/blue, crystalline
- **Details**:
  - Design a vertical frozen crystal/ice formation pattern
  - Fractal ice formations or crystalline structures
  - Can include clock motifs or time symbols frozen inside
  - Snowflake-like patterns, hexagonal crystals
  - Top-to-bottom ice crystal formation
  - The texture will be visible from all 4 directions due to cross pattern

**Sound**: GLASS (glass/ice sound)

**Reference**:
- Vanilla `ice.png` (ice transparency)
- Vanilla `packed_ice.png` (ice structure)
- Vanilla `amethyst_block.png` (crystalline patterns)

**Properties**:
- Hardness: 0.5
- Tool: Pickaxe (silk touch to get block)
- Light: None
- Special: Slippery (friction 0.98), never melts, semi-transparent
- **Transparency**: Translucent rendering (like ice)

**Rendering**:
- Uses `RenderType.translucent()` for ice-like transparency
- **Multi-layer JSON model**: Inner crystal (10x10x10) + Outer ice (16x16x16)
- Both textures should have alpha channel for transparency
- Outer layer more transparent than inner layer

**Biome**: chronosphere_snowy exclusive

**Texture Creation Tips**:

**For Outer Layer** (`frozen_time_ice_outer.png`):
1. Create 16x16 RGBA image
2. Base on vanilla ice.png structure
3. Color: Ice blue with purple tint (#9B59B6 or #3498DB)
4. Alpha: 70-80% transparent (very transparent ice)
5. Add subtle frost patterns
6. Keep it light and airy - this is the ice shell

**For Inner Core** (`frozen_time_ice_core.png`):
1. Create 16x16 RGBA image
2. Color: Deeper purple/blue (#9B59B6 or #5D6D9E)
3. Alpha: 40-50% transparent (less transparent than outer)
4. Add geometric crystal patterns:
   - Snowflake structure
   - Hexagonal crystals
   - Fractal ice formations
   - Optional: Small clock hands or time symbols frozen inside
5. Should look like frozen-in-time crystal structure

**Visual Result**:
When both layers combine, it should look like a transparent ice block with frozen crystals or time fragments suspended inside - like something frozen in time within ice.

**Current Status**: ⚠️ Placeholder (both layers use copy of time_wood_leaves.png)

---

## 12. Time Wood Fence（時の木フェンス）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/block/time_wood_fence_***.png`

**Concept**: Fence variant using Time Wood Planks texture

**Design**:
- **Theme**: Uses existing Time Wood Planks texture
- **Structure**: Standard fence shape (post + side bars)
- **Color**: Same as Time Wood Planks

**Reference**:
- Vanilla `oak_fence.png` structure + Time Wood Planks texture

**Models Required**:
- `time_wood_fence_post.json`
- `time_wood_fence_side.json`
- `time_wood_fence_inventory.json`

**Current Status**: ✅ Models created, uses Time Wood Planks texture

---

## Design Principles - Blocks

1. **Thematic Consistency**: All blocks use purple/blue as Chronosphere dimension theme
2. **Vanilla Compatibility**: Follow Minecraft's 16x16 pixel art style
3. **Functional Clarity**: Visual design indicates block function (light, decoration, building)
4. **Biome Exclusivity**: Moss (swamp) and Ice (snowy) have appropriate natural appearance
5. **Animation Support**: Clockwork Block can use .mcmeta for gear animation

---

## Implementation Checklist - Blocks

- ⚠️ Clockwork Block - TODO: Create mechanical/gear texture
- ⚠️ Time Crystal Block - TODO: Create glowing crystal texture
- ⚠️ Temporal Bricks - TODO: Create purple brick pattern with clock motifs
- ⚠️ Temporal Moss - TODO: Create green moss with purple highlights
- ⚠️ Frozen Time Ice - TODO: Create purple-tinted ice texture
- ✅ Time Wood Fence - Uses existing Time Wood Planks texture
- ✅ Temporal Bricks Variants - Will reuse Temporal Bricks texture

---

## Optional: Animation File for Clockwork Block

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/block/clockwork_block.png.mcmeta`

```json
{
  "animation": {
    "frametime": 4,
    "interpolate": true
  }
}
```

This creates smooth rotating gear animation. The PNG should contain multiple frames vertically stacked.
## 9. Fragment of Stasis Core（静止のコアの破片）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/fragment_of_stasis_core.png`

**コンセプト**:
- Time Tyrant（ラスボス）を倒して入手するボス素材
- 3-5個ドロップ（Looting効果で増加）
- 究極のアーティファクト（Chronoblade, Time Tyrant's Mail等）のクラフト素材
- Time Tyrantのコア（Stasis Core）が破壊された際の破片
- 時間を静止させる強大な力の残滓を秘めている

**デザイン方向性**:
- **ベース**: 結晶の破片形状（Amethyst Shardよりシャープで鋭い）
- **カラー**:
  - 青白い/シアン色（時間の静止を表現）
  - または深い紫/インディゴ（Time Tyrantのテーマカラー）
  - わずかにオレンジ/ゴールド（#db8813）のアクセント（Chronosphere modのテーマカラーを残す）
  - 凍結したような透明感
- **特徴**:
  - 不規則な破片形状（粉々に砕けたコアの一部）
  - 内部に光のライン（エネルギーの流れ）
  - 鋭利なエッジ（危険で強力な力の象徴）
  - 冷たい光の反射（ガラスや氷のような質感）
  - わずかなグロー効果（静止のエネルギー）
- **雰囲気**:
  - 破壊されたボスの名残
  - 強大な力の断片
  - レア度：Rare（黄色テキスト）にふさわしい存在感
  - 冷徹で無機質な力

**参考イメージ**:
- 青白い水晶の破片
- 凍結した時間のイメージ
- 時計の文字盤が止まった瞬間のような静寂

**差別化ポイント**:
- Clockstone: 淡いオレンジ結晶（基本素材）
- Enhanced Clockstone: 濃いオレンジ結晶（強化素材）
- **Fragment of Stasis Core**: 青白い/紫の結晶破片（ボス素材、時間静止のテーマ）

**Status**: ⚠️ TODO - 現在はenhanced_clockstone.pngのコピー、要編集

**優先度**: 高（T141-T147, US3ボス素材）

---

## 10. Eye of Chronos（クロノスの瞳）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/eye_of_chronos.png`

**コンセプト**:
- Time Tyrant（ラスボス）を倒して確定ドロップする究極のアーティファクト
- 時の神クロノスの瞳
- インベントリに所持しているだけで全敵対モブにSlowness Vを付与（通常はSlowness IV）
- 全てを見通し、時間を極限まで遅らせる神の力
- スタック数1、Epic rarity（薄紫テキスト）の最高級アイテム

**デザイン方向性**:
- **ベース**: 瞳（目）のモチーフ
- **カラー**:
  - **中心（瞳孔）**: 深い紫/インディゴまたは金色
  - **虹彩**: グラデーション（紫→金→オレンジ）
  - **周囲**: Epic rarityを表現する光のオーラ
  - オレンジ/ゴールド（#db8813）を含めてmodのテーマカラーを保つ
  - 神秘的で荘厳な配色
- **特徴**:
  - 明確な瞳のシルエット（縦長の瞳孔、または円形）
  - 虹彩に時計の文字盤模様（XII, III, VI, IX等）
  - 瞳孔の中に歯車や時計の針のシルエット
  - 強い発光/グロー効果（神の力を表現）
  - 周囲にオーラやエネルギーの波紋
  - 幾何学的な装飾（古代の魔法陣的な模様）
- **雰囲気**:
  - 神聖で畏怖を感じさせる
  - 全知全能の存在
  - 時間を支配する究極の力
  - Epic rarityにふさわしい圧倒的な存在感
  - 見る者を睨みつけるような威圧感

**参考イメージ**:
- ドラゴンの瞳や爬虫類の瞳（神秘的）
- エンダーマンの瞳（Minecraftのモチーフ）
- 時計の文字盤と瞳が融合したデザイン
- エジプト神話の「ホルスの目」やギリシャ神話の神々の瞳

**差別化ポイント**:
- Fragment of Stasis Core: 青白い結晶破片（素材）
- **Eye of Chronos**: 紫/金の瞳（究極アーティファクト、神の力）

**視覚的ヒエラルキー**:
- 最高級の希少性と力を持つアイテムとして、他の全アイテムを凌駕するデザイン
- Epicテキストカラー（薄紫）と調和する配色
- 一目で「特別なアイテム」と分かる圧倒的な視覚効果

**Status**: ⚠️ TODO - 現在はclockstone.pngのコピー、要編集

**優先度**: 高（T144-T147, US3究極アーティファクト）

---

## Updated Visual Hierarchy（更新版視覚的ヒエラルキー）

アイテムの重要度・希少性を色の濃さと効果で表現:

### Tier 1: Base Materials（基本素材）
1. **Clockstone**: 淡いオレンジ/ゴールド
2. **Enhanced Clockstone**: 濃いオレンジ/ゴールド + 発光

### Tier 2: Tools & Quest Items（ツール・クエストアイテム）
3. **Time Clock**: ゴールド + メカニカル + わずかな発光
4. **Spatially Linked Pickaxe**: シアン/ライトブルー + オレンジアクセント
5. **Ancient Gear**: ブロンズ + 金属質感
6. **Key to Master Clock**: 重厚なゴールド/ブロンズ

### Tier 3: High-Risk Materials（高リスク素材）
7. **Unstable Hourglass**: オレンジ/ゴールド + 赤/紫の不安定な色

### Tier 4: Boss Materials（ボス素材） - NEW
8. **Fragment of Stasis Core**: 青白い/紫の結晶破片 + グロー（Rare rarity）

### Tier 5: Ultimate Artifact（究極アーティファクト） - NEW
9. **Eye of Chronos**: 紫/金の瞳 + 強烈な発光/オーラ（Epic rarity）

---

---

## 11. Chronoblade（クロノブレード）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/chronoblade.png`

**コンセプト**:
- Time Tyrant撃破後に作成できる究極の武器
- Fragment of Stasis Core、Eye of Chronos、Enhanced Clockstone、Unstable Hourglassから鍛造
- 25%の確率で敵の次の攻撃AIを完全にスキップする時間操作能力
- 攻撃力8.0、耐久2000（ネザライト以上の性能）
- Epic rarity（薄紫テキスト）

**デザイン方向性**:
- **ベース**: 剣の形状（ダイヤモンド剣やネザライト剣より華麗）
- **カラー**:
  - **刀身**: シアン/ライトブルー（時間操作のエネルギー）+ 紫/インディゴのアクセント
  - **柄**: オレンジ/ゴールド（#db8813、modのテーマカラー）
  - **装飾**: 青白い発光エフェクト
- **特徴**:
  - 刀身に時計の針のようなデザイン
  - 刀身中央にエネルギーラインやコアの輝き
  - 柄に歯車や時計の文字盤モチーフ
  - 刀身から時間のオーラが放射（発光エフェクト）
  - 鋭利でエレガントな形状
  - ネザライト剣より洗練された高級感
- **雰囲気**:
  - 究極の時間操作武器
  - 時間を切り裂く神々しい力
  - Epic rarityにふさわしい圧倒的な存在感
  - 敵の時間軸そのものを断ち切る能力を視覚化

**参考イメージ**:
- ネザライト剣の形状にエネルギー武器の要素を追加
- 光る刀身（エンチャント時のグローをベースに）
- 時計の針を剣にしたようなデザイン

**Status**: ⚠️ Placeholder - enhanced_clockstone_sword.pngのコピー、要編集

**優先度**: 中（US3究極武器）

---

## 12. Time Tyrant's Mail（時間の暴君のメイル）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/time_tyrant_mail.png`

**コンセプト**:
- Time Tyrant撃破後に作成できる究極のチェストプレート
- Fragment of Stasis Core、Enhanced Clockstone、Unstable Hourglassから鍛造
- 20%の確率で致命的ダメージを受ける前の状態にロールバック（60秒クールダウン）
- Defense 8（ネザライトチェストプレート同等）、耐久600
- Epic rarity（薄紫テキスト）

**デザイン方向性**:
- **ベース**: チェストプレートの形状
- **カラー**: Fragment of Stasis Core の紫系配色に基づくカラーパレット
  - **ベースカラー**: #2C1F1F (濃い紫黒) - プレート本体の暗部
  - **セカンダリ**: #48353B (ミディアム紫グレー) - シャドウと深み
  - **アクセント1**: #563F50 (暗い紫) - 装飾とエネルギーライン
  - **アクセント2**: #664C6B (ミディアム紫) - 時間操作の痕跡
  - **ハイライト**: #6B52BF (明るい紫) - 発光部分と時間のエネルギー
- **特徴**:
  - 胸部中央に時計の文字盤や砂時計のエンブレム（Time Tyrant のシンボル）
  - 肩部分に暴君の支配を象徴する威圧的な装飾
  - プレート表面に時間が歪む紫の静止エネルギーパターン
  - ロールバック能力を表現する逆回転矢印や時計回りマーク
  - Fragment of Stasis Core の紫の輝きを纏う重厚な鎧
- **雰囲気**:
  - Fragment of Stasis Core から鍛え上げた静止の力を宿す鎧
  - 時間を巻き戻して死を回避する究極の防御
  - 破壊された Time Tyrant のコア破片の紫の光を放つ

**参考イメージ**:
- ネザライトチェストプレートに Fragment of Stasis Core の紫の輝きを融合
- 重厚な板金鎧に静止のエネルギーを纏わせたデザイン

**Status**: ⚠️ Placeholder - enhanced_clockstone_chestplate.pngのコピー、要編集

**優先度**: 中（US3究極防具）

---

## 13. Echoing Time Boots（時間の残響ブーツ）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/echoing_time_boots.png`

**コンセプト**:
- Time Tyrant撃破後に作成できる究極のブーツ
- Fragment of Stasis CoreとEnhanced Clockstoneから鍛造
- スプリント時に残像（デコイエンティティ）を召喚し敵の注意を引く（15秒クールダウン）
- Defense 3（ネザライトブーツ同等）、耐久500
- Epic rarity（薄紫テキスト）

**デザイン方向性**:
- **ベース**: ブーツの形状
- **カラー**: Fragment of Stasis Core の紫系配色に基づく（Time Tyrant's Mail と統一）
  - **ベースカラー**: #2C1F1F (濃い紫黒) - ブーツ本体の暗部
  - **セカンダリ**: #48353B (ミディアム紫グレー) - シャドウと深み
  - **アクセント1**: #563F50 (暗い紫) - 装飾とエネルギーライン
  - **アクセント2**: #664C6B (ミディアム紫) - 時間操作の痕跡
  - **ハイライト**: #6B52BF (明るい紫) - 残像エフェクトと時間のエネルギー
- **特徴**:
  - 足首部分に小さな時計や歯車の装飾
  - かかと部分に時間の流れを表現する波紋模様
  - Fragment of Stasis Core の紫の輝きが残像として表現される
  - スピード感と機動性を感じさせるデザイン
  - 軽快さと動的な印象
- **雰囲気**:
  - 時間の残響（紫の残像）を残して駆け抜ける
  - 高速移動と欺瞞戦術の象徴
  - Fragment of Stasis Core の力で過去の残像を召喚する

**参考イメージ**:
- ネザライトブーツに Fragment of Stasis Core の紫の輝きを融合
- 静止のエネルギーが残像として視覚化されるデザイン

**Status**: ⚠️ Placeholder - enhanced_clockstone_boots.pngのコピー、要編集

**優先度**: 中（US3究極防具）

---

## 14. Unstable Pocket Watch（不安定な懐中時計）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/item/unstable_pocket_watch.png`

**コンセプト**:
- Time Tyrant撃破後に作成できる究極のユーティリティアイテム
- Fragment of Stasis Core、Unstable Hourglass、Enhanced Clockstoneから鍛造
- 使用時に周囲のMobとプレイヤーの速度効果を入れ替える（30秒クールダウン）
- スタック数1、Epic rarity（薄紫テキスト）
- 極めて強力だが制御が難しい不安定な時間操作デバイス

**デザイン方向性**:
- **ベース**: 懐中時計の形状（Time Clockより大型で装飾的）
- **カラー**:
  - **本体**: オレンジ/ゴールド（#db8813）をベースに
  - **不安定さの表現**: 紫/赤のエネルギーが漏れ出している
  - **文字盤**: 時計の針が逆回転または不規則に動いているイメージ
- **特徴**:
  - Time Clockより大きく豪華な懐中時計
  - 文字盤の針が複数方向を指している（混沌）
  - ケース部分に亀裂や歪み（不安定さ）
  - エネルギーが制御不能に漏れ出しているエフェクト
  - 紫/赤の不吉な光のオーラ
  - 歯車が一部露出し不規則に回転
  - Unstable Hourglassとの視覚的な関連性
- **雰囲気**:
  - 強力だが危険な時間操作デバイス
  - 制御が難しく予測不能
  - 使用者と敵の立場を瞬時に入れ替える混沌の力
  - Epic rarityにふさわしい禁忌の道具

**参考イメージ**:
- バニラの時計（clock.png）を豪華にしたデザイン
- 壊れかけの古時計、狂った時計
- Unstable Hourglassと同系統の「不安定」な視覚表現

**Status**: ⚠️ Placeholder - time_clock.pngのコピー、要編集

**優先度**: 中（US3究極ユーティリティ）

---

---

## Crop Textures (T211-T215 Implementation)

### Temporal Root (時の根菜)

**Block Textures**: `temporal_root_stage_0.png` ~ `temporal_root_stage_7.png` (8 stages)
**Item Textures**: `temporal_root.png`, `baked_temporal_root.png`

**コンセプト**:
- 根菜類の作物（ニンジン/ポテトのような）
- 時間をテーマにした地下作物
- 生で食べるか焼いて食べる

**デザイン方向性**:
- **ベース**: バニラのニンジン（carrots）を参考
- **カラー**: 淡いオレンジ/ゴールド系（時間のテーマカラー #db8813）
- **成長段階**:
  - Stage 0-3: 小さな葉のみ（地上部分）
  - Stage 4-6: 葉が成長し、根の一部が見える
  - Stage 7: 完全に成熟、根が大きく見える
- **アイテム**:
  - `temporal_root.png`: ニンジンのような形状、オレンジ/ゴールド色
  - `baked_temporal_root.png`: 焼き色がついた根菜、より濃いオレンジ色

**プレースホルダー**: バニラのcarrots_stage_*.png と carrot.png を使用

---

### Chrono Melon (時のメロン)

**Block Textures**:
- `chrono_melon_stem_stage_0.png` ~ `chrono_melon_stem_stage_7.png` (8 stages)
- `chrono_melon_side.png`, `chrono_melon_top.png`, `chrono_melon_bottom.png`

**Item Textures**: `chrono_melon_slice.png`, `chrono_melon_seeds.png`

**コンセプト**:
- メロン/カボチャのような茎作物
- 時間の結晶化したエッセンスを含むメロン
- スライスして食べる

**デザイン方向性**:
- **ベース**: バニラのメロン（melon）を参考
- **カラー**: ゴールデン/アンバー色（時間のテーマカラー）
- **茎の成長段階**:
  - Stage 0-3: 小さく細い茎
  - Stage 4-6: 茎が太く成長
  - Stage 7: 完全に成熟、隣にメロンを生成可能
- **メロンブロック**:
  - Side: ゴールデンイエローの皮、縦縞模様
  - Top/Bottom: 茎の跡、放射状の模様
- **アイテム**:
  - `chrono_melon_slice.png`: 黄金色のメロンスライス、時計の針のような模様
  - `chrono_melon_seeds.png`: 小さな金色の種

**プレースホルダー**: バニラのmelon_stem_*.png, melon_*.png, melon_slice.png, melon_seeds.png を使用

---

### Timeless Mushroom (時知らずのキノコ)

**Block/Item Texture**: `timeless_mushroom.png`

**コンセプト**:
- 暗所で育つ食用キノコ
- 時間の流れから外れた存在
- 銀/白色で、Unstable Fungus（紫色）と区別

**デザイン方向性**:
- **ベース**: バニラのキノコ（brown_mushroom または red_mushroom）を参考
- **カラー**: 銀/白色、淡く光る（#C0C0C0 ~ #E8E8E8）
- **特徴**:
  - 傘は丸く、淡い銀白色
  - 柄は白っぽく、半透明感
  - 微かに光る（時間の外にある存在感）
  - Unstable Fungusとは明確に異なる色（紫 vs 銀白）
- **雰囲気**:
  - 幽霊のような、時間を超越した存在
  - 「Timeless（時知らず）」= 時間の流れに影響されない

**プレースホルダー**: バニラのbrown_mushroom.png を使用（色は後で調整）

---

### Enhanced Time Bread (強化された時のパン)

**Item Texture**: `enhanced_time_bread.png`

**コンセプト**:
- Time Breadの強化版、Temporal Rootを加えて栄養価を高めた
- レシピ: 3 Time Wheat + 1 Temporal Root
- 効果: Saturation I (5秒)

**デザイン方向性**:
- **ベース**: バニラのbread.pngを参考、Time Breadの上位版
- **カラー**:
  - メインカラー: やや濃いめの茶色/ゴールド（パンの焼き色）
  - アクセントカラー: オレンジ色の筋や斑点（Temporal Rootの混入）
- **特徴**:
  - 通常のパンより大きめ、または2つ重ねた印象
  - Temporal Rootの色（オレンジ #db8813）が生地に混ざっている感じ
  - 表面に金色の光沢、わずかに発光感
  - 切り口や断面にオレンジ色の野菜片が見える
- **雰囲気**:
  - 栄養豊富な高級パン
  - 通常のTime Breadより豪華で健康的
  - 「強化」されていることが一目で分かる

**プレースホルダー**: バニラのbread.png を使用

---

### Time Wheat Cookie (時の小麦クッキー)

**Item Texture**: `time_wheat_cookie.png`

**コンセプト**:
- Time WheatとChrono Melon Sliceで作る軽食
- レシピ: 2 Time Wheat + 1 Chrono Melon Slice → 8個
- 効果: なし（高速食べ可能）

**デザイン方向性**:
- **ベース**: バニラのcookie.pngを参考
- **カラー**:
  - メインカラー: 黄金色の焼き色（時間のテーマカラー #db8813）
  - アクセントカラー: 明るいゴールド（Chrono Melonの影響）
- **特徴**:
  - 円形または四角形のクッキー
  - 表面に金色の粒や結晶（Chrono Melonの果肉片）
  - わずかにキラキラした質感
  - 焼き目がしっかりついている
  - バニラのクッキーより明るく、黄金色が強い
- **雰囲気**:
  - 手軽なおやつ、エネルギー補給
  - 「時間のエッセンス」が詰まったクッキー
  - 通常のクッキーより高級感

**プレースホルダー**: バニラのcookie.png を使用

---

### Golden Time Wheat (黄金の時の小麦)

**Item Texture**: `golden_time_wheat.png`

**コンセプト**:
- 金インゴットで囲んで作る最上級の時間素材
- レシピ: 1 Time Wheat + 8 Gold Ingots
- 効果: Regeneration II (10秒) + Absorption II (2分)、常に食べられる

**デザイン方向性**:
- **ベース**: バニラのwheat.pngとgolden_apple.pngを融合
- **カラー**:
  - メインカラー: 明るい黄金色、メタリックゴールド
  - ハイライト: 純金のような輝き
- **特徴**:
  - Time Wheatの形状を保ちつつ、完全に黄金化
  - Golden Appleのような豪華な光沢、グロー効果
  - 麦穂全体が金色に輝いている
  - エンチャントグリント風の効果（オプション）
  - 魔法的なオーラや輝きを放っている
- **雰囲気**:
  - 最上級の時間アイテム
  - Golden Carrot、Golden Appleに匹敵する貴重さ
  - 「伝説の食材」感
  - 時間と富の融合

**プレースホルダー**: バニラのwheat.png を使用

---

## Implementation Status（更新版）

- ✅ Clockstone - 完成済み
- ⚠️ Enhanced Clockstone - コピーのまま、要編集
- ⚠️ Time Clock - コピーのまま、要編集
- ⚠️ Spatially Linked Pickaxe - コピーのまま、要編集
- ⚠️ Ancient Gear - コピーのまま、要編集
- ⚠️ Key to Master Clock - コピーのまま、要編集
- ⚠️ Unstable Hourglass - コピーのまま、要編集
- ⚠️ **Fragment of Stasis Core** - enhanced_clockstone.pngのコピー、要編集（優先度：高）
- ⚠️ **Eye of Chronos** - clockstone.pngのコピー、要編集（優先度：高）
- ⚠️ **Chronoblade** - enhanced_clockstone_sword.pngのコピー、要編集（優先度：中）
- ⚠️ **Time Tyrant's Mail** - enhanced_clockstone_chestplate.pngのコピー、要編集（優先度：中）
- ⚠️ **Echoing Time Boots** - enhanced_clockstone_boots.pngのコピー、要編集（優先度：中）
- ⚠️ **Unstable Pocket Watch** - time_clock.pngのコピー、要編集（優先度：中）
- ⚠️ **Temporal Root** (8 stages + 2 items) - プレースホルダー: carrots/carrot（T213）
- ⚠️ **Chrono Melon** (8 stem stages + 3 block faces + 2 items) - プレースホルダー: melon系（T213）
- ⚠️ **Timeless Mushroom** (1 texture) - プレースホルダー: brown_mushroom（T213）
- ⚠️ **Enhanced Time Bread** - プレースホルダー: bread.png、デザイン定義済み（T215）
- ⚠️ **Time Wheat Cookie** - プレースホルダー: cookie.png、デザイン定義済み（T215）
- ⚠️ **Golden Time Wheat** - プレースホルダー: wheat.png、デザイン定義済み（T215）

---

# Entity Textures（エンティティテクスチャ）

## E1. Chronos Warden（クロノスの監視者）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/entity/chronos_warden.png`

**コンセプト**:
- Phase 1の追加ボス（4体のうちの1体目）
- Guardian Vaultを守護する古代の石像ゴーレム
- 侵入者が近づくと目覚めて攻撃する番人
- Stone Stance（石化して80%ダメージ軽減）とGround Slam（地面を叩き範囲攻撃）が特徴
- Time Guardian相当の難易度（HP 180、防御12、攻撃力9）

**デザイン方向性**:
- **ベース**: Time Guardian（`time_guardian.png`）をベースとした人型モデル
- **カラー**:
  - **主色**: 石の灰色～茶色（Stone Brick、Polished Granite系の色）
  - **基調色**: #7A7A7A（ライトグレー）～ #8B6F47（ブラウングレー）
  - **苔・経年劣化**: 緑系の斑点（#3D6B3D）を部分的に追加
  - **時間のアクセント**: わずかにオレンジ/ゴールド（#db8813）の痕跡（時計の装飾部分）
- **特徴**:
  - **質感**: 粗い石の質感、ひび割れ、摩耗
  - **装飾**:
    - 胸部や肩に時計の歯車や文字盤の浮き彫り（古代遺跡風）
    - 関節部分に金属の装飾（動く石像のメカニズム）
  - **経年劣化**:
    - 苔の付着（肩、腕、脚部に緑の斑点）
    - 表面のひび割れ（戦いの痕跡）
    - 風化した古代の碑文や模様
  - **目**:
    - Time Guardianのような発光する目
    - 色は青ではなくオレンジ/ゴールド（#db8813）で統一
    - 目覚めると輝く
  - **弱点の表現（背中）**:
    - 背中部分に露出したコアや結晶（オレンジ色の輝き）
    - Stone Stance時に狙うべき弱点として視覚的に分かりやすく
- **雰囲気**:
  - 重厚で動きが鈍い石のゴーレム
  - 古代遺跡の番人としての威厳
  - 長い年月を経た遺物の雰囲気
  - Time Guardianの青い魔法的な雰囲気とは対照的な、物理的・機械的な印象

**参考イメージ**:
- バニラの Iron Golem（石版）
- Time Guardian の体型とポーズ
- 石像や古代遺跡のゴーレム
- スチームパンク的な石造りのオートマトン

**カラーパレット**:
```
主色:
- Base Stone: #7A7A7A (Light Gray)
- Dark Stone: #5A5A5A (Dark Gray)
- Warm Stone: #8B6F47 (Brown Gray - Polished Granite tone)

装飾・アクセント:
- Gear/Clock Gold: #db8813 (Orange/Gold - mod theme)
- Moss Green: #3D6B3D (Aged moss)
- Core Glow: #db8813 (Orange glow for eyes and weak spot)

質感:
- Cracks: #3A3A3A (Dark gray for cracks)
- Highlights: #A0A0A0 (Light gray for stone highlights)
```

**変換方法（Time GuardianからChronos Wardenへ）**:
1. **色相変更**: 青系 → グレー/茶色系
   - Time Guardianの青い部分 → 石の灰色/茶色
   - 発光部分の青 → オレンジ/ゴールド
2. **質感追加**:
   - ノイズテクスチャで石の粗さを追加
   - ひび割れのラインを描き込み
   - 緑の苔の斑点をランダムに配置
3. **装飾の調整**:
   - 時計・歯車のモチーフは維持（色をゴールドに）
   - より古代遺跡風のディテールに変更

**モデル**:
- Time Guardianと同じHumanoidModel（2.5ブロック高）
- 同じアニメーションを使用（歩行、攻撃）
- Ground Slam時は腕を振り下ろすアニメーション

**Status**: ⚠️ Placeholder - time_guardian.pngのコピー、要編集

**優先度**: 中（T234実装）

**実装タイミング**: Phase 1（Chronos Warden + Clockwork Colossus）

---

## Entity Texture Design Principles

1. **Base Consistency**: Time Guardianをベースとして、各ボスに独自の個性を付与
2. **Color Coding**: 各ボスを色で区別（Chronos Warden = 石の灰色/茶色、他のボスは別の色）
3. **Thematic Unity**: すべてのボスが時計・時間のモチーフを持つ
4. **Visual Clarity**: プレイヤーが一目でどのボスか判別できるデザイン
5. **Rarity Expression**: ボスの難易度や重要度を視覚的に表現

---

---

## E2. Clockwork Colossus（機械仕掛けの巨人）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/entity/clockwork_colossus.png`

**コンセプト**:
- Phase 1の追加ボス（4体のうちの2体目）
- Clockwork Depthsを守護する巨大な機械ゴーレム
- 古代の時計職人が作り上げた機械の守護者
- Overcharge（自己強化）とRepair Protocol（一度だけ回復）が特徴
- HP 200、攻撃力12、高い攻撃性能

**デザイン方向性**:
- **ベース**: Time Guardian（`time_guardian.png`）をベースとした人型モデル
- **カラー**:
  - **主色**: 鉄・銅の金属色（Iron Block、Copper Block系）
  - **基調色**: #7F7F7F（鉄グレー）、#B87333（銅ブラウン）
  - **歯車・メカ**: 暗い金属色（#4A4A4A）と明るい金属（#C0C0C0）のコントラスト
  - **エネルギー**: オレンジ/ゴールド（#db8813）の発光（稼働中のエネルギー）
  - **Overcharge時**: 赤みがかった光（#FF4500）が追加
- **特徴**:
  - **質感**: 金属の硬質な質感、リベット、ボルト
  - **装飾**:
    - 胸部に大きな歯車が露出（回転しているイメージ）
    - 肩・腕・脚に複数の小さな歯車
    - 関節部分に機械的なジョイント
    - 配管やケーブルが体表を走る
  - **機械的ディテール**:
    - 鉄と銅の異素材の組み合わせ（パネル状）
    - 歯車が複数箇所で露出
    - リベット・ボルトで固定された外装
    - 内部からオレンジ色の光が漏れる（エネルギーコア）
  - **目**:
    - Redstone Lampのような赤い発光（#FF3333）
    - Overcharge時はより強く輝く
  - **サイズ感**:
    - Time Guardianより大型に見えるディテール
    - 重厚な装甲プレート
- **雰囲気**:
  - 重厚な金属の塊
  - スチームパンク的な機械美
  - Iron Golemのような無機質さ + 古代の高度技術
  - 動くたびにギア音が聞こえそうな機械感

**参考イメージ**:
- バニラの Iron Golem（機械版）
- 時計の内部機構（大量の歯車）
- スチームパンクのロボット
- 銅と鉄を組み合わせた機械装甲

**カラーパレット**:
```
主色:
- Iron Gray: #7F7F7F (鉄の本体)
- Copper Brown: #B87333 (銅のパネル)
- Dark Metal: #4A4A4A (暗い金属パーツ)
- Light Metal: #C0C0C0 (明るい金属・ハイライト)

装飾・エネルギー:
- Gear Gold: #db8813 (歯車・エネルギーコア)
- Overcharge Red: #FF4500 (Phase 2の赤い発光)
- Eye Red: #FF3333 (Redstone Lamp風の目)

質感:
- Rivet Dark: #3A3A3A (リベット・ボルト)
- Panel Edge: #A0A0A0 (パネルの縁)
```

**変換方法（Time GuardianからClockwork Colossusへ）**:
1. **色相変更**: 青系 → 金属グレー/銅色
   - Time Guardianの青い鎧部分 → 鉄グレー
   - 装飾部分 → 銅ブラウン
   - 発光部分 → オレンジ/赤
2. **質感追加**:
   - 金属のパネル分割（鉄と銅）
   - リベット・ボルトをパネル接続部に配置
   - 歯車のディテールを胸部・関節に追加
3. **装飾の調整**:
   - より機械的なデザイン
   - 歯車を強調
   - 配管・ケーブルを追加

**モデル**:
- Time Guardianと同じHumanoidModel（2.5ブロック高）
- より大型に見える視覚効果

**Status**: ⏳ 未実装

**優先度**: 中（T235実装）

**実装タイミング**: Phase 1（Chronos Warden + Clockwork Colossus）

---

## E3. Temporal Phantom（時の亡霊）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/entity/temporal_phantom.png`

**コンセプト**:
- Phase 2の追加ボス（4体のうちの3体目）
- Phantom Catacombsに現れる幽霊のような存在
- Phase Shift（物理攻撃無効化）とPhantom Clone（分身召喚）が特徴
- HP 150、高速移動（0.25）、魔法的な攻撃

**デザイン方向性**:
- **ベース**: Time Guardian（`time_guardian.png`）をベースとした人型モデル
- **カラー**:
  - **主色**: 紫・青の半透明な霊体（幽霊的）
  - **基調色**: #9B59B6（紫）、#5DADE2（ライトブルー）
  - **半透明**: 全体的に半透明（Alpha 70-80%）
  - **発光**: 紫の強い発光エフェクト（#C471ED）
  - **Phase Shift時**: さらに透明化（Alpha 40-50%）
- **特徴**:
  - **質感**: 幽霊的な半透明、エーテル的な存在
  - **装飾**:
    - 体全体が霧や煙のように揺らめくイメージ
    - 時計の針や文字盤がぼんやりと浮かび上がる
    - エンドポータルのようなパーティクル模様
  - **霊体ディテール**:
    - 輪郭が少しぼやけている
    - 内部から紫の光が透けて見える
    - 下半身が霧状に薄れていく（幽霊的表現）
    - Phantomモブのような浮遊感
  - **目**:
    - 紫色に強く発光（#C471ED）
    - 虚ろで不気味な印象
  - **魔法エフェクト**:
    - 体の周囲に魔法陣やルーン文字が浮遊
    - 時間の歪みを表現する紫の波紋
- **雰囲気**:
  - 実体のない幽霊
  - 魔法的で神秘的
  - Enderman + Phantomを融合したような不気味さ
  - 時間に囚われた亡霊の悲哀

**参考イメージ**:
- バニラの Phantom（幽霊的浮遊感）
- バニラの Enderman（紫のパーティクル）
- 幽霊や霊体の半透明表現
- エンドポータルの渦巻き模様

**カラーパレット**:
```
主色:
- Phantom Purple: #9B59B6 (霊体の基本色)
- Light Blue: #5DADE2 (半透明部分の青)
- Dark Purple: #6A4C93 (濃い紫の影)

発光:
- Glow Purple: #C471ED (強い紫の発光)
- Magic Purple: #9D84B7 (魔法エフェクト)
- Eye Purple: #DA70D6 (目の発光)

半透明:
- Overall Alpha: 70-80% (通常時)
- Phase Shift Alpha: 40-50% (物理攻撃無効時)
```

**変換方法（Time GuardianからTemporal Phantomへ）**:
1. **色相変更**: 青系 → 紫系（より神秘的に）
   - Time Guardianの青 → 紫/ライトブルー
   - 発光部分 → 強い紫
2. **質感変更**:
   - 固体 → 半透明の霊体
   - Alpha値を下げる（透明化）
   - エッジをぼかす（幽霊感）
3. **エフェクト追加**:
   - 半透明レイヤー
   - 内部発光
   - 魔法陣・ルーンの浮遊

**モデル**:
- Time Guardianと同じHumanoidModel（2.5ブロック高）
- レンダリング時に半透明処理を適用

**Status**: ⏳ 未実装

**優先度**: 低（T236実装）

**実装タイミング**: Phase 2（Temporal Phantom + Entropy Keeper）

---

## E4. Entropy Keeper（エントロピーの管理者）

**ファイル**: `common/src/main/resources/assets/chronosphere/textures/entity/entropy_keeper.png`

**コンセプト**:
- Phase 2の追加ボス（4体のうちの4体目）
- Entropy Cryptを支配する腐敗と崩壊の化身
- Decay Aura（Wither効果）とEntropy Burst（爆発攻撃）が特徴
- HP 160、時間経過で強化されるDegradation能力

**デザイン方向性**:
- **ベース**: Time Guardian（`time_guardian.png`）をベースとした人型モデル
- **カラー**:
  - **主色**: 緑黒・枯れた茶色（腐敗・崩壊のテーマ）
  - **基調色**: #2F4F2F（ダークグリーン）、#3A2A1A（枯れた茶色）
  - **腐敗の緑**: #7CFC00（Slime緑）、#228B22（深緑）
  - **Wither効果**: 黒（#1A1A1A）と緑（#39FF14）のコントラスト
  - **Degradation発光**: 時間経過で緑の光が強くなる（#00FF00）
- **特徴**:
  - **質感**: 枯れた、腐食した、崩壊しつつある存在
  - **装飾**:
    - 苔やスカルク（Sculk）のような腐敗が体を覆う
    - 時計の歯車が錆び、ひび割れている
    - 体の一部が崩壊・欠損しているように見える
  - **腐敗ディテール**:
    - Soul SandやMoss Blockのテクスチャ要素
    - 緑色の粘液が滴るイメージ
    - ひび割れから緑の光が漏れる（エントロピーのエネルギー）
    - 骨のような露出した内部構造
  - **目**:
    - Witherのような空洞化した黒い目
    - 周囲に緑の発光（#39FF14）
  - **時間経過の変化**:
    - Degradationが進むにつれて緑の発光が強くなる
    - 腐敗が進行しているように見える
- **雰囲気**:
  - 不気味で不吉
  - Witherのような死と崩壊のテーマ
  - Soul Sand Valley（ソウルサンドの谷）の雰囲気
  - 時間の腐敗と劣化の具現化

**参考イメージ**:
- バニラの Wither（死と崩壊）
- Soul Sand, Moss Block（腐敗素材）
- Sculk（不気味な有機的テクスチャ）
- Slime（緑の粘液）

**カラーパレット**:
```
主色:
- Dark Green: #2F4F2F (腐敗の暗緑)
- Decay Brown: #3A2A1A (枯れた茶色)
- Black: #1A1A1A (Wither的な黒)
- Moss Green: #228B22 (苔の深緑)

腐敗エフェクト:
- Slime Green: #7CFC00 (粘液の緑)
- Glow Green: #39FF14 (腐敗の発光)
- Degradation Glow: #00FF00 (時間経過で強化)

質感:
- Rust: #8B4513 (錆びた金属)
- Decay: #556B2F (腐敗部分)
- Crack: #2A2A2A (ひび割れ)
```

**変換方法（Time GuardianからEntropy Keeperへ）**:
1. **色相変更**: 青系 → 緑黒系
   - Time Guardianの青い部分 → 暗緑/茶色
   - 発光部分 → 緑の発光
2. **質感変更**:
   - 綺麗な装甲 → 腐食・崩壊した外観
   - 苔・スカルクのテクスチャを追加
   - ひび割れ、欠損を強調
3. **エフェクト追加**:
   - 緑の粘液
   - 腐敗の緑発光
   - 骨や内部構造の露出

**モデル**:
- Time Guardianと同じHumanoidModel（2.5ブロック高）
- 一部が欠損しているような視覚効果

**Status**: ⏳ 未実装

**優先度**: 低（T237実装）

**実装タイミング**: Phase 2（Temporal Phantom + Entropy Keeper）

---

## Entity Implementation Checklist

- ⚠️ **Chronos Warden** - time_guardian.pngのコピー、要編集（優先度：中、Phase 1）
- ⏳ **Clockwork Colossus** - 未実装（優先度：中、Phase 1）
- ⏳ **Temporal Phantom** - 未実装（優先度：低、Phase 2）
- ⏳ **Entropy Keeper** - 未実装（優先度：低、Phase 2）

