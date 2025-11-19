# Work Notes - Crop System Implementation (T211-T215)

## 2025-11-17: T211-T215 Implementation

### Task Overview

**Purpose**: ディメンション内での食料調達手段を追加し、長期滞在やサバイバルプレイを可能にする

**Current State**:
- Time Wheat (crop) - ✅ Implemented
- Time Bread (food) - ✅ Implemented
- Fruit of Time (food) - ✅ Implemented (grows on Time Wood trees)
- Time Fruit Pie (food) - ✅ Implemented
- Time Jam (food) - ✅ Implemented

**Gap Analysis**:
現在の食料システムは実装済みだが、タスクT211-T215では追加の作物システムを求めている。

### T211: Design Time-Themed Crop Concepts

#### Design Decision: Multiple Diverse Crops

**Rationale**:
1. **Existing Coverage**:
   - Wheat-type crop: ✅ Time Wheat (planted on farmland)
   - Tree fruit: ✅ Fruit of Time (grows on trees, berry-like)
   - Root vegetable: ❌ Not yet implemented
   - Melon-type: ❌ Not yet implemented
   - Mushroom: ✅ Unstable Fungus (not edible) - need edible variant

2. **Design Philosophy**:
   - **Avoid Duplication**: Fruit of Time already fills the "berry" niche
   - **Maximize Variety**: Different crop types = different gameplay mechanics
   - **Time Theme**: Each crop should have temporal/chronological connection

3. **Crop Type Distribution**:
   - **Farmland crops**: Time Wheat (existing), Temporal Root (new)
   - **Stem crops**: Chrono Melon (new)
   - **Dark-growing**: Timeless Mushroom (new, distinct from Unstable Fungus)
   - **Tree crops**: Fruit of Time (existing)

---

## Crop 1: Temporal Root (時の根菜) - Root Vegetable

**Vanilla Reference**: Carrot / Potato
**Time Theme**: "Roots of time" - grows underground, symbolizes ancient/deep time

**Block Properties**:
- **Name**: Temporal Root Crop (時の根菜)
- **Type**: Farmland crop (like carrots/potatoes)
- **Growth Stages**: 8 stages (0-7), same as carrots
- **Placement**: Must be planted on farmland
- **Light Requirement**: Light level 9+ to grow
- **Growth Speed**: Normal random tick growth

**Harvest Behavior**:
- **Stage 0-6**: Drops only 1 Temporal Root Seeds
- **Stage 7 (mature)**: Drops 2-4 Temporal Roots + 0-2 Temporal Root Seeds
- **Fortune Effect**: Yes (increases drop count)

**Item: Temporal Root (時の根菜)**:
- **Type**: Food item (raw vegetable)
- **Nutrition**: 3 hunger points (1.5 drumsticks)
- **Saturation**: 0.6 (total 1.8)
- **Effect**: None (raw state)
- **Usage**: Can be eaten raw or cooked

**Item: Baked Temporal Root (焼いた時の根菜)**:
- **Type**: Food item (cooked)
- **Nutrition**: 6 hunger points (3 drumsticks)
- **Saturation**: 0.6 (total 3.6)
- **Effect**: Regeneration I for 5 seconds (100% chance)
- **Crafting**: Smelt Temporal Root in furnace/smoker
- **Usage**: Better than raw, provides healing

**Worldgen Placement**:
- **Biomes**: chronosphere_plains (rare patches)
- **Frequency**: Rare (like wild carrots/potatoes in villages)
- **Placement Rule**: Small patches (2-4 crops) on farmland near water

---

## Crop 2: Chrono Melon (時のメロン) - Stem Crop

**Vanilla Reference**: Melon / Pumpkin
**Time Theme**: "Crystallized time" - melon contains time essence in solid form

**Block Properties**:
- **Name**: Chrono Melon (時のメロン)
- **Type**: Stem crop (like melon/pumpkin)
- **Growth Stages**:
  - Stem: 8 stages (0-7)
  - Melon Block: Full block with time-themed texture
- **Placement**: Stem on farmland, melon grows on adjacent dirt/grass
- **Light Requirement**: Light level 9+ for stem growth
- **Growth Speed**: Normal random tick growth

**Harvest Behavior**:
- **Stem**: Does not drop anything when broken
- **Melon Block**: Drops 3-7 Chrono Melon Slices when broken
- **Silk Touch**: Drops the full melon block
- **Fortune Effect**: Yes (increases slice drops)

**Item: Chrono Melon Seeds (時のメロンの種)**:
- **Type**: Seeds (plantable item)
- **Acquisition**: Crafting (1 Chrono Melon Slice → 1 Seeds)

**Item: Chrono Melon Slice (時のメロンの切れ端)**:
- **Type**: Food item
- **Nutrition**: 2 hunger points (1 drumstick)
- **Saturation**: 0.3 (total 0.6)
- **Effect**: None
- **Usage**: Quick food, or crafted into better items

**Item: Chrono Melon Block (時のメロン・ブロック)**:
- **Type**: Block item (decorative + craftable)
- **Usage**: Decoration, or craft into 9 slices

**Worldgen Placement**:
- **Biomes**: chronosphere_plains, chronosphere_forest
- **Frequency**: Uncommon patches
- **Placement Rule**: Small melon patches (like pumpkin patches)

---

## Crop 3: Timeless Mushroom (時知らずのキノコ) - Fungus

**Vanilla Reference**: Brown/Red Mushroom
**Time Theme**: "Timeless" - grows in darkness, exists outside normal time flow
**Visual Distinction**: Different from Unstable Fungus (purple/blue vs brown/red)

**Block Properties**:
- **Name**: Timeless Mushroom (時知らずのキノコ)
- **Type**: Mushroom block
- **Growth Stages**: No stages (instant placement)
- **Placement**: Dirt, grass, mycelium, podzol (like vanilla mushrooms)
- **Light Requirement**: **Light level 12 or less** (grows in darkness/dim light)
- **Spread**: Can spread to nearby blocks in darkness (like vanilla mushrooms)
- **Growth Speed**: Slow natural spread

**Visual Design**:
- **Color**: Silver/white with faint glow (distinct from purple Unstable Fungus)
- **Size**: Small mushroom (same as vanilla)
- **Theme**: "Timeless" = pale, ethereal, ghost-like

**Harvest Behavior**:
- **Break**: Drops 1 Timeless Mushroom item
- **Bonemeal**: Cannot grow into huge mushroom (keeps it distinct from vanilla)

**Item: Timeless Mushroom (時知らずのキノコ)**:
- **Type**: Food item (plantable)
- **Nutrition**: 2 hunger points (1 drumstick)
- **Saturation**: 0.3 (total 0.6)
- **Effect**: None (raw state)
- **Usage**: Eaten raw, used in stew, or planted

**Worldgen Placement**:
- **Biomes**: chronosphere_forest (dark areas), chronosphere_cave areas
- **Frequency**: Uncommon in dark spots
- **Placement Rule**: Small clusters in low-light areas (similar to vanilla mushrooms)

---

## Additional Food Items (T215)

### Crafted Foods from New Crops

**1. Temporal Root Stew (時の根菜シチュー)**:
- **Recipe**: 1x Baked Temporal Root + 1x Timeless Mushroom + 1x Bowl → 1x Temporal Root Stew
- **Nutrition**: 8 hunger points (4 drumsticks)
- **Saturation**: 0.6 (total 4.8)
- **Effect**: Regeneration II for 10 seconds (100% chance)
- **Usage**: High-value healing food combining multiple crops

**2. Glistening Chrono Melon (きらめく時のメロン)**:
- **Recipe**: 1x Chrono Melon Slice + 8x Gold Nuggets → 1x Glistening Chrono Melon
- **Nutrition**: 2 hunger points (1 drumstick)
- **Saturation**: 1.2 (total 2.4) - very high saturation
- **Effect**: Absorption I for 30 seconds (100% chance)
- **Usage**: Premium ingredient for brewing or high-saturation snack

**3. Chrono Melon Juice (時のメロンジュース)**:
- **Recipe**: 4x Chrono Melon Slices + 1x Glass Bottle → 1x Chrono Melon Juice
- **Nutrition**: 4 hunger points (2 drumsticks)
- **Saturation**: 0.4 (total 1.6)
- **Effect**: Speed I for 60 seconds (100% chance)
- **Usage**: Drinkable food (like honey bottle) with movement buff

**4. Timeless Mushroom Soup (時知らずのキノコスープ)**:
- **Recipe**: 2x Timeless Mushrooms + 1x Bowl → 1x Timeless Mushroom Soup
- **Nutrition**: 6 hunger points (3 drumsticks)
- **Saturation**: 0.6 (total 3.6)
- **Effect**: Night Vision for 60 seconds (100% chance)
- **Usage**: Exploration food with vision enhancement

### Time Wheat Enhancements (Candidate 4)

**5. Enhanced Time Bread (強化された時のパン)** - T216 Extension:
- **Recipe**: 3x Time Wheat + 1x Temporal Root → 1x Enhanced Time Bread
- **Nutrition**: 7 hunger points (3.5 drumsticks)
- **Saturation**: 0.8 (total 5.6)
- **Effect**: Regeneration I for 10 seconds (100% chance)
- **Usage**: Improved bread combining wheat and root crops

**6. Time Wheat Cookie (時の小麦クッキー)**:
- **Recipe**: 2x Time Wheat + 1x Chrono Melon Slice → 8x Time Wheat Cookies
- **Nutrition**: 2 hunger points (1 drumstick) each
- **Saturation**: 0.4 (total 0.8)
- **Effect**: None
- **Usage**: Efficient snack food (high yield from recipe)

**7. Golden Time Wheat (黄金の時の小麦)**:
- **Recipe**: 1x Time Wheat + 8x Gold Nuggets → 1x Golden Time Wheat
- **Nutrition**: 3 hunger points (1.5 drumsticks)
- **Saturation**: 2.4 (total 7.2) - extremely high saturation
- **Effect**: Regeneration II for 5 seconds + Absorption I for 30 seconds (100% chance)
- **Usage**: Ultimate premium ingredient (can substitute for Golden Carrot in brewing)

---

## Food System Summary

### Complete Food & Crop List (After T211-T215)

#### Existing Foods (Already Implemented)
| Food Item | Nutrition | Saturation | Effect | Acquisition |
|-----------|-----------|------------|--------|-------------|
| Time Wheat | 1 | 0.6 | None | Farming (crop) |
| Time Bread | 5 | 0.6 | None | Crafting (3x Time Wheat) |
| Fruit of Time | 4 | 0.6 | Haste I (30s) | Tree harvest |
| Time Fruit Pie | 8 | 0.3 | Haste II (30s) | Crafting |
| Time Jam | 4 | 0.5 | Speed I (60s) | Crafting |

#### New Crops & Raw Foods ⭐
| Food Item | Nutrition | Saturation | Effect | Acquisition |
|-----------|-----------|------------|--------|-------------|
| **Temporal Root** | 3 | 0.6 | None | Farming (root crop) |
| **Baked Temporal Root** | 6 | 0.6 | Regen I (5s) | Smelting |
| **Chrono Melon Slice** | 2 | 0.3 | None | Melon harvest |
| **Timeless Mushroom** | 2 | 0.3 | None | Foraging (dark areas) |

#### New Crafted Foods ⭐
| Food Item | Nutrition | Saturation | Effect | Acquisition |
|-----------|-----------|------------|--------|-------------|
| **Temporal Root Stew** | 8 | 0.6 | Regen II (10s) | Crafting |
| **Glistening Chrono Melon** | 2 | 1.2 | Absorption I (30s) | Crafting + Gold |
| **Chrono Melon Juice** | 4 | 0.4 | Speed I (60s) | Crafting (bottle) |
| **Timeless Mushroom Soup** | 6 | 0.6 | Night Vision (60s) | Crafting |
| **Enhanced Time Bread** | 7 | 0.8 | Regen I (10s) | Crafting |
| **Time Wheat Cookie** | 2 | 0.4 | None | Crafting (8 pieces) |
| **Golden Time Wheat** | 3 | 2.4 | Regen II (5s) + Absorption I (30s) | Crafting + Gold |

⭐ = New items from T211-T215

### Crop Type Distribution
- **Farmland Crops**: Time Wheat (existing), Temporal Root (new)
- **Stem Crops**: Chrono Melon (new)
- **Mushrooms**: Timeless Mushroom (new, edible - distinct from Unstable Fungus)
- **Tree Crops**: Fruit of Time (existing)

### Effect Distribution
- **Haste** (mining): Fruit of Time → Time Fruit Pie
- **Speed** (movement): Time Jam, Chrono Melon Juice
- **Regeneration** (healing): Baked Temporal Root, Temporal Root Stew, Enhanced Time Bread, Golden Time Wheat
- **Night Vision** (exploration): Timeless Mushroom Soup
- **Absorption** (extra health): Glistening Chrono Melon, Golden Time Wheat

### Progression Balance
- **Early Game**: Raw crops (Temporal Root, Chrono Melon Slice, Timeless Mushroom)
- **Mid Game**: Cooked/simple crafts (Baked Root, Mushroom Soup, Melon Juice)
- **Advanced**: Complex crafts (Temporal Root Stew, Enhanced Time Bread)
- **Late Game**: Gold-enhanced foods (Glistening Melon, Golden Wheat)

---

## Implementation Plan

### T211: ✅ Design Completed

All crop and food designs documented in this file.

### T212: Implement Crop Blocks and Base Items

**Blocks**:
1. `TemporalRootBlock.java` - Carrot-like crop block (8 stages)
2. `ChronoMelonStemBlock.java` - Melon stem block (8 stages)
3. `ChronoMelonBlock.java` - Full melon block
4. `TimelessMushroomBlock.java` - Mushroom block (spreadable)

**Base Food Items**:
1. `TemporalRootItem.java` - Raw root vegetable (food)
2. `BakedTemporalRootItem.java` - Cooked root with Regen effect
3. `ChronoMelonSliceItem.java` - Melon slice (food)
4. `TimelessMushroomItem.java` - Mushroom (food + plantable)

**Seeds**:
1. `TemporalRootItem.java` - Plantable item (same as food item, like carrot)
2. `ChronoMelonSeedsItem.java` - Seeds (crafted from slices)

**Block Items**:
1. `ChronoMelonBlock` item - Decorative/craftable melon block

**Registration**:
- Register all blocks in `ModBlocks`
- Register all items in `ModItems`
- Add loot tables for all crop blocks

### T213: Create Crop Textures

**Block Textures** (in `assets/chronosphere/textures/block/`):
- `temporal_root_stage_0.png` through `temporal_root_stage_7.png` (8 stages)
- `chrono_melon_stem_stage_0.png` through `chrono_melon_stem_stage_7.png` (8 stages)
- `chrono_melon_side.png`, `chrono_melon_top.png`, `chrono_melon_bottom.png`
- `timeless_mushroom.png` (single mushroom)

**Item Textures** (in `assets/chronosphere/textures/item/`):
- `temporal_root.png` (raw root)
- `baked_temporal_root.png` (cooked root)
- `chrono_melon_slice.png` (melon slice)
- `chrono_melon_seeds.png` (seeds)
- `timeless_mushroom.png` (mushroom item)

**Models & Blockstates**:
- Create blockstate JSONs for all crop blocks
- Create block models for all growth stages
- Create item models for all items

### T214: Add Worldgen Placement

**Configured Features**:
- `temporal_root_patch.json` - Small farmland patches with roots
- `chrono_melon_patch.json` - Melon patches (stem + melons)
- `timeless_mushroom_patch.json` - Dark-area mushroom clusters

**Placed Features**:
- `temporal_root_placed.json` - Rare plains placement
- `chrono_melon_placed.json` - Uncommon plains/forest placement
- `timeless_mushroom_placed.json` - Dark forest/cave placement

**Biome Integration**:
- Add features to `chronosphere_plains.json`
- Add features to `chronosphere_forest.json`
- Add mushrooms to dark/cave areas

### T215: Implement Crafted Food Items

**Food Items** (7 new items):
1. `TemporalRootStewItem.java` - Stew with Regen II
2. `GlisteningChronoMelonItem.java` - Gold-enhanced melon
3. `ChronoMelonJuiceItem.java` - Drinkable juice (bottle)
4. `TimelessMushroomSoupItem.java` - Soup with Night Vision
5. `EnhancedTimeBreadItem.java` - Improved bread with Regen
6. `TimeWheatCookieItem.java` - Efficient snack (8 per craft)
7. `GoldenTimeWheatItem.java` - Premium gold-enhanced wheat

**Recipes** (in `data/chronosphere/recipes/`):
- Crafting recipes for all 7 items
- Smelting recipe for `baked_temporal_root.json`
- Seeds crafting: `chrono_melon_seeds.json` (1 slice → 1 seeds)
- Melon crafting: `chrono_melon_block.json` (9 slices ↔ 1 block)

**Textures** (in `assets/chronosphere/textures/item/`):
- `temporal_root_stew.png`
- `glistening_chrono_melon.png`
- `chrono_melon_juice.png`
- `timeless_mushroom_soup.png`
- `enhanced_time_bread.png`
- `time_wheat_cookie.png`
- `golden_time_wheat.png`

**Localization**:
- Add all item names to `en_us.json` and `ja_jp.json`
- Add creative tab entries

### T216: Add Eating Effect to Time Bread (Optional Enhancement)

**Option A**: Keep Time Bread simple (no effect) - distinguishes it from Enhanced Time Bread
**Option B**: Add basic effect (Speed I for 30s or Regen I for 10s)

Decision: Recommend **Option A** to maintain clear progression:
- Time Bread = Basic, no effect
- Enhanced Time Bread = Advanced, with Regen effect

### T217: Test Crop Growth and Food Effects

**Testing Checklist**:
- [ ] Temporal Root growth (8 stages, fortune effect)
- [ ] Chrono Melon stem growth and melon placement
- [ ] Timeless Mushroom spreading in darkness
- [ ] All crop worldgen placement (frequency, biome distribution)
- [ ] All food effects apply correctly
- [ ] Smelting recipe for Baked Temporal Root
- [ ] All crafting recipes work
- [ ] Loot tables drop correct items
- [ ] Creative tab contains all items

### Files to Create

```
common/src/main/java/com/chronosphere/
├── blocks/
│   ├── TemporalRootBlock.java            [T212]
│   ├── ChronoMelonStemBlock.java         [T212]
│   ├── ChronoMelonBlock.java             [T212]
│   └── TimelessMushroomBlock.java        [T212]
└── items/consumables/
    ├── TemporalRootItem.java             [T212] (raw + plantable)
    ├── BakedTemporalRootItem.java        [T212]
    ├── ChronoMelonSliceItem.java         [T212]
    ├── ChronoMelonSeedsItem.java         [T212]
    ├── TimelessMushroomItem.java         [T212]
    ├── TemporalRootStewItem.java         [T215]
    ├── GlisteningChronoMelonItem.java    [T215]
    ├── ChronoMelonJuiceItem.java         [T215]
    ├── TimelessMushroomSoupItem.java     [T215]
    ├── EnhancedTimeBreadItem.java        [T215]
    ├── TimeWheatCookieItem.java          [T215]
    └── GoldenTimeWheatItem.java          [T215]

common/src/main/resources/
├── assets/chronosphere/
│   ├── textures/
│   │   ├── block/                        [T213]
│   │   │   ├── temporal_root_stage_0.png → temporal_root_stage_7.png (8 files)
│   │   │   ├── chrono_melon_stem_stage_0.png → chrono_melon_stem_stage_7.png (8 files)
│   │   │   ├── chrono_melon_side.png
│   │   │   ├── chrono_melon_top.png
│   │   │   ├── chrono_melon_bottom.png
│   │   │   └── timeless_mushroom.png
│   │   └── item/                         [T213 + T215]
│   │       ├── temporal_root.png
│   │       ├── baked_temporal_root.png
│   │       ├── chrono_melon_slice.png
│   │       ├── chrono_melon_seeds.png
│   │       ├── timeless_mushroom.png
│   │       ├── temporal_root_stew.png
│   │       ├── glistening_chrono_melon.png
│   │       ├── chrono_melon_juice.png
│   │       ├── timeless_mushroom_soup.png
│   │       ├── enhanced_time_bread.png
│   │       ├── time_wheat_cookie.png
│   │       └── golden_time_wheat.png
│   ├── blockstates/                      [T213]
│   │   ├── temporal_root.json
│   │   ├── chrono_melon_stem.json
│   │   ├── chrono_melon.json
│   │   └── timeless_mushroom.json
│   └── models/                           [T213]
│       ├── block/ (growth stage models)
│       └── item/ (item models)
└── data/chronosphere/
    ├── worldgen/                         [T214]
    │   ├── configured_feature/
    │   │   ├── temporal_root_patch.json
    │   │   ├── chrono_melon_patch.json
    │   │   └── timeless_mushroom_patch.json
    │   └── placed_feature/
    │       ├── temporal_root_placed.json
    │       ├── chrono_melon_placed.json
    │       └── timeless_mushroom_placed.json
    ├── recipes/                          [T215]
    │   ├── baked_temporal_root.json (smelting)
    │   ├── chrono_melon_seeds.json
    │   ├── chrono_melon_block.json
    │   ├── chrono_melon_from_block.json
    │   ├── temporal_root_stew.json
    │   ├── glistening_chrono_melon.json
    │   ├── chrono_melon_juice.json
    │   ├── timeless_mushroom_soup.json
    │   ├── enhanced_time_bread.json
    │   ├── time_wheat_cookie.json
    │   └── golden_time_wheat.json
    └── loot_table/blocks/               [T212]
        ├── temporal_root.json
        ├── chrono_melon.json
        ├── chrono_melon_stem.json (empty)
        └── timeless_mushroom.json
```

### Summary Statistics

**New Crops**: 3 types (Temporal Root, Chrono Melon, Timeless Mushroom)
**New Food Items**: 11 items total
  - 4 base foods (raw/cooked crops)
  - 7 crafted foods (stews, juices, enhanced items)
**Code Files**: 16 Java files (4 blocks + 12 items)
**Textures**: ~30 texture files
**Data Files**: ~20 JSON files (worldgen, recipes, loot tables, models)

### Next Steps

1. ✅ T211: Design completed (comprehensive multi-crop system)
2. ⏳ T212: Implement 4 crop blocks + 5 base items
3. ⏳ T213: Create ~30 textures + models/blockstates
4. ⏳ T214: Add worldgen for 3 crop types
5. ⏳ T215: Implement 7 crafted food items + recipes

---

## Previous Work Notes

# Work Notes - Desert Clock Tower Implementation

## 2025-11-02: T093-T095, T099 Implementation

### Completed Tasks

✅ **T093**: Desert Clock Tower structure NBT and JSON configuration
- Created template_pool JSON: `/common/src/main/resources/data/chronosphere/worldgen/template_pool/desert_clock_tower/start_pool.json`
- Created processor_list JSON: `/common/src/main/resources/data/chronosphere/worldgen/processor_list/desert_clock_tower_loot.json`
- Created placeholder NBT: `/common/src/main/resources/data/chronosphere/structure/desert_clock_tower.nbt`
  - ⚠️ **IMPORTANT**: Currently using ancient_ruins.nbt as placeholder
  - **TODO**: Create actual tower structure using structure blocks in-game

✅ **T094**: Desert Clock Tower structure feature
- Created structure JSON: `/common/src/main/resources/data/chronosphere/worldgen/structure/desert_clock_tower.json`
- Configuration:
  - Type: `minecraft:jigsaw`
  - Biome: `chronosphere:chronosphere_plains`
  - Terrain adaptation: `beard_thin`
  - Start height: `absolute: 0`

✅ **T095**: Desert Clock Tower structure set
- Created structure_set JSON: `/common/src/main/resources/data/chronosphere/worldgen/structure_set/desert_clock_tower.json`
- Placement configuration:
  - Type: `minecraft:random_spread`
  - Salt: `1663542342`
  - Spacing: `20` (more rare than ancient_ruins which uses 16)
  - Separation: `8`

✅ **T099**: Enhanced Clockstone loot configuration
- Created loot table: `/common/src/main/resources/data/chronosphere/loot_table/chests/desert_clock_tower.json`
- Loot contents:
  - **Pool 1** (guaranteed): Enhanced Clockstone x4-8
  - **Pool 2** (guaranteed): Clockstone x8-16
  - **Pool 3** (2-4 random items):
    - Iron Ingot x2-6 (weight: 10)
    - Gold Ingot x2-5 (weight: 8)
    - Diamond x1-3 (weight: 5)
    - Fruit of Time x4-8 (weight: 12)
    - Torch x8-16 (weight: 10)

### Build Status

✅ Build successful: `./gradlew :fabric:build` completed without errors

### Next Steps (TODO)

#### High Priority
1. **Create actual NBT structure** (T095a)
   - Use Minecraft structure blocks in-game
   - Recommended specifications:
     - Size: 15x30x15 blocks (tall tower design)
     - Materials: Sandstone-based blocks (smooth sandstone, cut sandstone, chiseled sandstone)
     - Features:
       - Clock tower aesthetic (vertical tower with clock face decoration)
       - Multiple floors/levels
       - Place chest(s) at strategic location(s)
       - Add decorative elements (stairs, slabs, fences for details)
     - Desert theme to match "Desert Clock Tower" name
   - Steps to create:
     1. Launch Minecraft with the mod loaded
     2. Build the tower structure in Creative mode
     3. Use Structure Block (Save mode) to save as `desert_clock_tower`
     4. Copy the generated NBT from `.minecraft/saves/<world>/generated/minecraft/structures/` to `common/src/main/resources/data/chronosphere/structure/desert_clock_tower.nbt`
     5. Test structure generation with `/locate structure chronosphere:desert_clock_tower`

#### Medium Priority
2. **Test structure generation in-game**
   - Verify structure spawns in chronosphere_plains biome
   - Confirm chest loot (Enhanced Clockstone present)
   - Check spacing/frequency (should be rarer than ancient_ruins)

3. **Visual verification**
   - Ensure tower height is appropriate (30 blocks recommended)
   - Verify sandstone materials blend with chronosphere biome
   - Check for any generation issues (floating blocks, missing floor, etc.)

#### Optional Enhancements
4. **Consider adding variants** (future)
   - Multiple tower designs (tall/short, different shapes)
   - Different material variants for biome variety
   - Add structure weight randomization

### Files Created

```
common/src/main/resources/data/chronosphere/
├── structure/
│   └── desert_clock_tower.nbt (placeholder - needs replacement)
├── worldgen/
│   ├── template_pool/desert_clock_tower/
│   │   └── start_pool.json
│   ├── processor_list/
│   │   └── desert_clock_tower_loot.json
│   ├── structure/
│   │   └── desert_clock_tower.json
│   └── structure_set/
│       └── desert_clock_tower.json
└── loot_table/chests/
    └── desert_clock_tower.json
```

### Design Notes

- **Placement strategy**: Rarer than ancient_ruins (spacing 20 vs 16) to make Enhanced Clockstone more valuable
- **Loot balance**: Guaranteed Enhanced Clockstone drop (4-8) provides sufficient material for time manipulation items
- **Biome restriction**: Currently limited to chronosphere_plains - may expand to other chronosphere biomes in future
- **Structure adaptation**: Uses `beard_thin` for natural terrain blending

### Related Tasks

- **Previous**: T096-T098 (Enhanced Clockstone item implementation) ✅ Complete
- **Next**: T100-T104 (Time Clock item implementation)
- **Dependencies**: NBT structure creation (T095a) is independent and can be done in parallel with other US2 tasks

---

## Instructions for Resuming Work

1. If continuing with NBT structure creation:
   - Read the "Create actual NBT structure" section above
   - Launch Minecraft with mod and build the tower
   - Follow the save/copy steps carefully

2. If moving to next tasks:
   - T100-T109: Time manipulation items (Time Clock, Spatially Linked Pickaxe)
   - T110-T115: Time Guardian boss entity
   - Can proceed with these while NBT structure is pending

3. Reference files for structure design:
   - `ancient_ruins.nbt` - simple ruin structure (reference for size/complexity)
   - `forgotten_library.nbt` - 35x10x35 building (reference for larger structures)
