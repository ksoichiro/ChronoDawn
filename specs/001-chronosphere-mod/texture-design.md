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

## 6. Clockwork Block（歯車ブロック）

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

## 7. Time Crystal Block（時間水晶ブロック）

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

## 8. Temporal Bricks（時のレンガ）

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

## 9. Temporal Moss（時の苔）

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

## 10. Frozen Time Ice（凍結時間の氷）

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

## 11. Time Wood Fence（時の木フェンス）

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
