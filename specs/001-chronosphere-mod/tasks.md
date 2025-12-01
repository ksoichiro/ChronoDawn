# Tasks: Chronosphere Mod - 時間操作をテーマにしたMinecraft Mod

**Feature Branch**: `001-chronosphere-mod`
**Input**: Design documents from `/specs/001-chronosphere-mod/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Architecture**: Architectury Multi-Loader Framework (common / fabric / neoforge)
**Tech Stack**: Java 21, Minecraft 1.21.1, NeoForge 21.1.x + Fabric

**Tests**: テストタスクは明示的に要求されている (quickstart.md参照)。JUnit 5 + GameTest Frameworkを使用。

**Organization**: タスクはUser Storyごとにグループ化され、各ストーリーを独立して実装・テスト可能にする。

### Time Cycle Configuration (Phase 2.5)

**Purpose**: 固定時刻を設定し、他のディメンションmodと同様の独特な雰囲気を作り出す

- [X] T034j [P] Research day-night cycle vs fixed time design (Decision 7)
- [X] T034k [P] Implement fixed time (6000 ticks = noon) and End sky effects in dimension_type/chronosphere.json
- [X] T034l [P] Test dimension visual appearance with fixed time and grey sky in-game
- [X] T034m [P] Evaluate time/sky settings and document adjustments needed (fixed_time: 4000-8000, effects: overworld/end/custom)
- [ ] T034n [P] (Future) Research custom DimensionSpecialEffects for precise sky color control
- [ ] T034o [P] (Future) Design sky color unlock mechanic tied to boss defeat (Option D element)

**Current Settings**: `fixed_time: 6000` (noon), `effects: minecraft:overworld`, biome `sky_color: 9474192` (grey)

**Note**: 固定時刻の値（4000-8000=明るい時間帯）と空の色はプレイテスト後に調整可能

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

### Special Blocks - World Generation & Advanced Features (US1)

**Note**: Current implementation allows special blocks (Reversing Time Sandstone, Unstable Fungus) to be obtained only via creative mode or commands. This section defines future tasks for natural generation and gameplay integration.

**Considerations for Reversing Time Sandstone**:
- **Natural Generation**: Should spawn in appropriate terrain (deserts, beaches, structures) rather than plains, as it's sandstone-based
- **Block Variants**: Consider adding stone-based variants (e.g., Reversing Time Stone) for different biomes
- **Special Tool**: Design a special tool (e.g., "Temporal Pickaxe") that can permanently break and collect these blocks before restoration
- **Integration**: Tie into existing structures (Ancient Ruins, Forgotten Library) or new generation features

- [ ] T086a [US1] Design natural generation strategy for special blocks (biome selection, frequency, placement rules)
- [ ] T086b [US1] Consider and design block variants for different terrain types (sandstone for deserts/beaches, stone for caves/mountains)
- [ ] T086c [US1] Design special tool specification for breaking restoration blocks (crafting recipe, mechanics, additional features)
- [ ] T086d [US1] Implement worldgen features and/or structure integration for special block placement

### Basic Resources (US1 Enhancement - High Priority)

**Purpose**: Chronosphere内でサバイバルプレイに必要な基本リソースを入手可能にする

**Note**: 石炭は松明や燃料として必須。長期滞在を可能にするため優先度は高い

- [x] T265 [P] [US1] Add coal ore generation in Chronosphere (frequency, Y-level distribution, biome placement)
  - **Completed**: Added coal ore to all 9 Chronosphere biomes
  - **Configuration**: 30 attempts/chunk, Y0-256 trapezoid distribution
- [x] T266 [P] [US1] Configure coal ore worldgen feature (vein size, rarity comparable to Overworld)
  - **Completed**: Vein size 17 (matches vanilla), uses vanilla coal_ore and deepslate_coal_ore
  - **Files**: configured_feature/ore_coal.json, placed_feature/ore_coal.json
- [x] T267 [US1] Test coal ore generation and verify mining/smelting works correctly
  - **Completed**: Tested in-game, coal ore generates correctly and mining/fuel mechanics work as expected

### Additional Tree Variants (US1 Enhancement - Optional, Low Priority)

**Purpose**: 木のバリエーションを増やして視覚的多様性を向上

**Note**: これは優先度が低く、US1の MVP には必須ではない。他の機能が完成後に実装を検討

- [X] T088t [P] [US1] (Optional) Design Time Wood color variants (e.g., Dark Time Wood, Ancient Time Wood)
- [X] T088u [P] [US1] (Optional) Create variant textures and block definitions
- [X] T088v [P] [US1] (Optional) Create variant tree features and configure placement in different biomes

### Time Wood Boats & Chest Boats (US1 Enhancement - Medium Priority)

**Purpose**: Time Wood系の木材から舟とチェスト付き舟を作成可能にする

**Note**: オーシャンバイオームでの移動手段として有用。3種類の木材それぞれに対応

**Implementation Approach**: Custom Boat entities (extending vanilla Boat/ChestBoat) for Architectury compatibility. See research.md "Time Wood Boats Implementation Plan" for detailed architecture.

**Estimated Effort**: 5-7 hours total

- [ ] T268 [P] [US1] Create Time Wood Boat and Time Wood Chest Boat items and entities
  - Create TimeWoodBoat.java and TimeWoodChestBoat.java (extend vanilla Boat/ChestBoat)
  - Create TimeWoodBoatItem.java and TimeWoodChestBoatItem.java
  - Register entities in ModEntities, items in ModItems
- [ ] T269 [P] [US1] Create Dark Time Wood Boat and Dark Time Wood Chest Boat items and entities
  - Create DarkTimeWoodBoat.java and DarkTimeWoodChestBoat.java
  - Create corresponding items
  - Register entities and items
- [ ] T270 [P] [US1] Create Ancient Time Wood Boat and Ancient Time Wood Chest Boat items and entities
  - Create AncientTimeWoodBoat.java and AncientTimeWoodChestBoat.java
  - Create corresponding items
  - Register entities and items
- [ ] T271 [P] [US1] Add boat crafting recipes (planks → boat, boat + chest → chest boat)
  - Create 6 shaped recipes (3 boat variants: 5 planks in boat shape)
  - Create 6 shapeless recipes (3 chest boat variants: boat + chest)
  - Add recipe unlock advancements (on obtaining planks)
- [ ] T272 [P] [US1] Create boat textures and models for all 3 variants
  - Extract vanilla oak boat textures as base
  - Apply Time Wood color palettes (use plank textures as color reference)
  - Create 3 boat textures + 3 chest boat textures (6 total)
  - Create item models (can reuse vanilla boat item model structure)
- [ ] T273 [US1] Test boat functionality (movement, durability, chest storage) in Chronosphere ocean biome
  - Test boat placement and riding
  - Test chest boat storage
  - Test crafting recipes
  - Test boat breaking and item drops
  - Verify entity rendering on both Fabric and NeoForge

### Decorative Terrain Features (US1 Enhancement)

**Purpose**: 装飾的な地形要素を追加して探索の視覚的多様性を向上

**Note**: MVP後の改善として実装。植生とモブスポーンの後に実装推奨

- [X] T088ad [US1] Research vanilla decorative features (boulders, fallen logs, disk features for gravel/sand)
- [X] T088ae [P] [US1] Create boulder configured_feature (cobblestone/mossy cobblestone clusters)
- [X] T088af [P] [US1] Create fallen log configured_feature (Time Wood Log horizontal placement)
- [X] T088ag [P] [US1] Create gravel disk configured_feature for varied terrain texture
- [X] T088ah [P] [US1] Add decorative features to biome feature lists (vegetation decoration step)
- [X] T088ai [US1] Test decorative features in-game and adjust frequency/placement

**Current Implementation**: Boulders (mossy cobblestone), fallen logs (random_patch, 2-3 blocks scattered), gravel disks (radius 2-5 blocks)

**Future Improvements**:
- [ ] T088ai-a [US1] (Future) Implement straight fallen logs using NBT structures for more natural appearance (current: random_patch creates scattered logs)

### Biome-Specific Landmarks (US1 Enhancement - Optional)

**Purpose**: バイオームに固有の小規模構造物を追加して識別性を向上

**Note**: 優先度は低い。Phase 6（Polish）段階での実装を検討

- [ ] T088aj [P] [US1] (Optional) Design small rock circle feature for chronosphere_plains (Stonehenge-like miniature)
- [ ] T088ak [P] [US1] (Optional) Design mushroom circle feature for chronosphere_forest (fairy ring theme)
- [ ] T088al [P] [US1] (Optional) Design coral-like time structure for chronosphere_ocean (temporal reef theme)
- [ ] T088am [P] [US1] (Optional) Implement landmark features and configure rare placement
- [ ] T088an [US1] (Optional) Test landmark generation and ensure they don't interfere with main structures

### Ambient Sounds & Particles (US1 Enhancement - Optional)

**Purpose**: 環境音とパーティクルで没入感を向上

**Note**: 優先度は最も低い。Phase 6（Polish）段階での実装を検討

- [ ] T088ao [P] [US1] (Optional) Research custom biome ambient sounds (wind, waves, cave echoes)
- [ ] T088ap [P] [US1] (Optional) Create custom sound files or select vanilla sounds for each biome
- [ ] T088aq [P] [US1] (Optional) Configure ambient sounds in biome JSONs (mood_sound, additions_sound)
- [ ] T088ar [P] [US1] (Optional) Design time distortion particle effects (subtle sparkles, clock hands)
- [ ] T088as [P] [US1] (Optional) Implement particle spawning logic for biomes (client-side)
- [ ] T088at [US1] (Optional) Test ambient sounds and particles in-game for immersion quality

### Custom Mobs (US1 Enhancement - High Priority)

**Purpose**: 時間をテーマにした独自モブを追加し、ディメンションの独自性とゲームプレイの多様性を向上

**Note**: ボス以外の通常モブがないという問題を解決。敵対・中立・友好モブを追加して探索体験を豊かにする

- [X] T200 [US1] Design custom mob concepts (hostile, neutral, friendly with time theme, behavior patterns, drops, spawn conditions)
- [X] T201 [P] [US1] Create Temporal Wraith entity in common/src/main/java/com/chronosphere/entities/mobs/TemporalWraithEntity.java (hostile, phases through blocks when hit, inflicts Slowness II on attack)
- [X] T202 [P] [US1] Create Clockwork Sentinel entity in common/src/main/java/com/chronosphere/entities/mobs/ClockworkSentinelEntity.java (hostile, immune to time distortion effects, drops Ancient Gears)
- [X] T203 [P] [US1] Create Time Keeper entity in common/src/main/java/com/chronosphere/entities/mobs/TimeKeeperEntity.java (neutral, villager-like trading for time-related items)
- [X] T204 [P] [US1] Register custom mobs in ModEntities registry and configure spawning in biomes (Temporal Wraith in forest/plains, Clockwork Sentinel in desert/structures, Time Keeper in libraries)
- [X] T205 [P] [US1] Create custom mob textures and models in common/src/main/resources/assets/chronosphere/textures/entity/
- [X] T206 [P] [US1] Create custom mob loot tables in common/src/main/resources/data/chronosphere/loot_tables/entities/
- [X] T207 [US1] Test custom mob spawning, AI behavior, and loot drops in-game
- [X] T208 [P] [US1] Create spawn eggs for custom mobs in ModItems for creative mode and debugging (temporal_wraith_spawn_egg, clockwork_sentinel_spawn_egg, time_keeper_spawn_egg)
  - **Completed**: Implemented spawn eggs for regular mobs only (Temporal Wraith, Clockwork Sentinel, Time Keeper)
  - **Note**: Boss mobs (Time Guardian, Time Tyrant) intentionally excluded following vanilla conventions
    - Vanilla pattern: Ender Dragon and Wither (bosses with boss bars) have no spawn eggs
    - Warden and Elder Guardian (strong mobs without boss bars) have spawn eggs
    - Time Guardian (mini-boss) and Time Tyrant (final boss) follow boss pattern → no spawn eggs
  - **Implementation**: Created DeferredSpawnEggItem with reflection-based registration for Architectury compatibility
  - **Colors**: Match entity texture schemes (dark purple/cyan, royal blue/gold, dark slate blue/white)
  - **Files**: DeferredSpawnEggItem.java, ModItems.java updates, model JSONs, localizations
- [X] T209 [US1] Investigate and fix mob spawn rate issues (hostile mobs too few, friendly/neutral animals too few compared to vanilla Overworld)
  - **Root Cause**: Monster spawn weights were 1/3 to 1/10 of vanilla Overworld (120-175 vs ~515), creature weights were also low
  - **Solution**: Increased monster weights to match vanilla (~500-600 total per biome), standardized minCount/maxCount to 4/4
  - **Changes**:
    - Added creeper, enderman, witch to most biomes
    - Increased custom mob weights (temporal_wraith, clockwork_sentinel) to 100
    - Increased creature weights and counts in Plains/Forest (cow: 8, sheep: 12, pig: 10, chicken: 10)
    - Added biome-specific variants (husk in Desert, stray in Snowy)
  - **Files**: All 7 biome JSON files (chronosphere_plains.json, chronosphere_forest.json, chronosphere_desert.json, chronosphere_mountain.json, chronosphere_ocean.json, chronosphere_snowy.json, chronosphere_swamp.json)
- [X] T210 [P] [US2] Add ranged attack capability to Time Guardian (design projectile, implement attack pattern, configure AI goals)
  - **Note**: Time Guardian (時の番人) is the mini-boss, not Time Keeper (時間の管理者/trading mob)
  - **Completed**: Implemented Time Blast projectile with custom ranged attack AI
  - **Projectile**: TimeBlastEntity - magical projectile that applies Slowness II + Mining Fatigue I (5 seconds)
  - **AI**: TimeGuardianRangedAttackGoal with cooldown (10s) and distance requirements (7-15 blocks)
  - **Balance**: Attack interval 5s, minimum range 7 blocks prevents spam, melee fallback for close combat
  - **Files**: TimeBlastEntity.java, TimeGuardianRangedAttackGoal.java, TimeBlastRenderer.java, texture, localizations

### Food & Crops (US1 Enhancement - Medium Priority)

**Purpose**: ディメンション内での食料調達手段を追加し、長期滞在やサバイバルプレイを可能にする

**Note**: 現在は時のパン(Time Bread)のみで食料が不足。作物や食料アイテムを追加して多様性を向上

- [X] T211 [US1] Design time-themed crop concepts (plant types, growth stages, harvest items, growth conditions)
- [X] T212 [P] [US1] Implement time-themed crop block and item (Temporal Wheat, Time Berry Bush, or similar)
- [X] T213 [P] [US1] Create crop textures for all growth stages in common/src/main/resources/assets/chronosphere/textures/block/
- [X] T214 [P] [US1] Add crop worldgen placement (natural generation in specific biomes)
- [X] T215 [P] [US1] Implement additional food items (cooked variants, crafted recipes, nutritional values)
- [X] T216 [P] [US1] Add eating effect to Time Bread (e.g., Speed I for 30 seconds, or Regeneration I for 10 seconds)
  - **Implemented**: Regeneration I for 5 seconds
  - **Rationale**: Shorter duration (5s instead of 10s) to prevent overpowered healing considering ease of farming Time Wheat
  - **Theme**: "Time reversal" concept - healing through rewinding damage
- [X] T217 [US1] Test crop growth mechanics and food item effects in-game
  - **Completed**: All crop worldgen, growth mechanics, and food effects tested and verified
  - **Worldgen fixes applied** (2025-11-29):
    - Fixed loot tables: Temporal Root (2-4 drops), Chrono Melon (9 slices)
    - Fixed worldgen placement: All crops use `y_spread: 0` for ground-level placement
    - Added `canSurvive()` checks: Chrono Melon, Temporal Root, Timeless Mushroom
    - Prevented log block replacement: All crops avoid tree roots and trunks
    - Timeless Mushroom: Reduced generation intensity to prevent terrain modification
  - **Final settings**:
    - Temporal Root: rarity 1/8, tries 16, ground placement only
    - Chrono Melon: rarity 1/32, tries 6, `canSurvive()` validation
    - Timeless Mushroom: count 4/chunk, tries 8, `canSurvive()` validation

**Implementation (T211-T215 completed 2025-11-22)**:
- **T211 Design**: 3 crop types designed - Temporal Root (root vegetable), Chrono Melon (stem crop), Timeless Mushroom (fungus)
- **T212 Implementation**:
  - 4 block classes: TemporalRootBlock, ChronoMelonStemBlock, ChronoMelonBlock, TimelessMushroomBlock
  - 12 item classes: 5 base foods + 7 crafted foods
  - All registered in ModBlocks, ModItems, and creative tab
- **T213 Textures**:
  - 31 placeholder textures created (block: 20, item: 11)
  - Texture design specifications documented in texture-design.md
  - Note: Using temporary placeholders with basic modifications, will be replaced with custom designs later
- **T214 Worldgen**:
  - Configured features and placed features for all 3 crops
  - Added to chronosphere_plains and chronosphere_forest biomes
- **T215 Food Items**:
  - 11 recipes created (1 smelting + 10 crafting)
  - All food effects implemented (Regeneration, Speed, Night Vision, Absorption, Saturation)
  - Loot tables for all crop blocks
- **Commits**: 51b954b (implementation), fe6d3bd (texture designs), 670993c (placeholder textures)

### Forgotten Library Enhancements (US2 Enhancement - Medium Priority)

**Purpose**: 図書館構造物の魅力を向上し、探索報酬を追加

- [X] T218 [P] [US2] Add hidden chest under carpet blocks in Forgotten Library structure NBT (1-2 chests with valuable loot tables)
  - **Completed**: Added 4 hidden chests in strategic locations throughout Forgotten Library
  - **Implementation**: Placed chests under carpet blocks using Structure Block in-game editing
  - **File**: common/src/main/resources/data/chronosphere/structure/forgotten_library.nbt
- [X] T219 [US2] Test hidden chest placement and ensure carpet blocks can be broken to reveal chests
  - **Completed**: Verified in-game that carpet blocks can be broken to reveal hidden chests
  - **Note**: 4 chests provide additional exploration rewards and discovery mechanics

### Time Arrow Item Fix (US2 Bug Fix - High Priority)

**Purpose**: 時の矢(Time Arrow)が効果を持たない問題を修正

**Issue**: 射撃しても何も効果が発生しない

- [X] T220 [US2] Investigate Time Arrow implementation and identify missing effect logic
- [X] T221 [P] [US2] Implement Time Arrow hit effect (e.g., inflict Slowness on mobs, or teleport entities back to previous position)
- [X] T222 [US2] Test Time Arrow projectile mechanics and effects in-game

**Root Cause**: Minecraft 1.21でArrowItem.createArrow()のシグネチャが変更され、4つ目のパラメータ(ItemStack weapon)が追加されたが、実装が旧シグネチャのままだったためオーバーライドされず、通常のArrowエンティティが発射されていた。

**Solution**:
- createArrow()メソッドに4つ目のパラメータを追加
- minecraft:arrowsアイテムタグにTime Arrowを追加
- 全てのLivingEntityにSlowness IIエフェクト(3秒)を付与するよう実装
- Time Tyrant専用の強化エフェクト(Slowness III + Weakness II + Glowing)は維持

**Known Limitation**: Time Arrowでは「Take Aim」進捗が達成されない(バニラ進捗は特定の矢タイプのみ認識)。通常の矢で進捗達成可能なため、バニラ進捗オーバーライド(他modとの競合リスク)は行わない。

### Tests for User Story 2

- [X] T089 [P] [US2] Write GameTest for Desert Clock Tower generation in common/src/test/java/com/chronosphere/integration/DesertClockTowerTest.java
- [X] T090 [P] [US2] Write unit test for Time Clock cooldown logic in common/src/test/java/com/chronosphere/unit/TimeClockTest.java
- [X] T091 [P] [US2] Write unit test for Spatially Linked Pickaxe drop multiplier in common/src/test/java/com/chronosphere/unit/PickaxeDropTest.java
- [ ] T092 [P] [US2] Write GameTest for Time Guardian boss fight in common/src/test/java/com/chronosphere/integration/TimeGuardianFightTest.java

### Player Guidance & Discovery System (US2 Enhancement)

**Purpose**: プレイヤーが構造物やアイテムを発見し、ゲームを進行できるようガイダンスを提供

**Current Issue**: Ancient Ruins、Desert Clock Tower、Master Clock Towerなどの構造物の場所や、アイテムの入手方法について説明がなく、プレイヤーが作者の想定通りに進行するのは困難

**Guidance Methods**: 書物アイテム、村人との取引（地図）、進捗ヒント、構造物の出現頻度調整、ロケーターアイテムなど

- [X] T115f [US2] Research appropriate player guidance methods (book items, advancement hints, villager trades, structure frequency)
- [X] T115g [P] [US2] Create Chronicle of Chronosphere book item (guide book explaining dimension mechanics, structures, and progression)
  - Created ChronicleOfChronosphereItem.java with English version only
  - Book auto-given on first Chronosphere entry
  - Added to creative tab as readable vanilla written_book
  - Documented complete worldbuilding in specs/001-chronosphere-mod/lore.md
- [X] T115h [P] [US2] Add Time Keeper trades for Time Compass items (Desert Clock Tower Compass, Master Clock Compass)
  - Trade 1: 16 Clockstone → Time Compass (Desert Clock Tower) - 3 max uses
  - Trade 2: 8 Enhanced Clockstone → Time Compass (Master Clock) - 1 max use
  - Compasses point to structures within 100 chunk radius
  - Right-click compass to locate structure and show distance/direction
  - Displays X/Z coordinates, distance in blocks, and 8-direction heading (N/NE/E/SE/S/SW/W/NW)
- [X] T115i [US2] Add advancement system with descriptive hints for key progression milestones (first portal, Ancient Ruins discovery, Time Guardian defeat)
  - Improved existing advancement descriptions (portal_creation, dimension_entry, portal_stabilization)
  - Added explicit next-step guidance in advancement text
- [X] T115j [US2] Adjust structure spawn rates to make discovery easier (increase frequency or reduce spacing)
  - Ancient Ruins: spacing 16→12, separation 4→3 (25% more frequent)
  - Forgotten Library: spacing 50→30, separation 25→15 (40% more frequent)
  - Desert Clock Tower: spacing 32→20, separation 8→5 (37.5% more frequent)
  - Master Clock Tower: spacing 100→70, separation 50→35 (30% more frequent)
- [X] T115k [P] [US2] Create Time Compass item (points to nearest key structure, similar to lodestone compass)
  - Created TimeCompassItem.java with NBT-based structure targeting
  - Stores target structure type (desert_clock_tower, master_clock) and GlobalPos coordinates in CustomData
  - Client-side item property ("angle") for compass needle animation
  - Right-click to locate structure via findNearestMapStructure API
  - Shows localized messages with coordinates, distance, and 8-direction heading
  - Inherits vanilla compass model/textures for seamless integration
- [X] T115l [US2] Add initial guidance on first dimension entry (chat message, advancement, or book given to player)
  - Chronicle of Chronosphere auto-given to player on first Chronosphere entry
  - Book dropped at player's feet if inventory is full
- [X] T115m [US2] Implement time distortion particle effects for Ancient Ruins (Temporal Seal failure visualization)
  - Created TemporalParticleEmitterBlock (invisible, indestructible, no collision)
  - Uses SOUL_FIRE_FLAME particles (cyan-blue color)
  - Particles float upward (0.15-0.25 speed) with 70% spawn frequency
  - Placed multiple emitters in Ancient Ruins structure NBT
  - Visual indicator of Temporal Seal degradation (referenced in lore.md)
- [X] T115n [P] [US2] Implement stable Ancient Ruins placement with random_spread
  - **Final Implementation**: Changed from concentric_rings to random_spread for distance stability
  - **Placement**: spacing=48 chunks (768 blocks), separation=24 chunks (384 blocks minimum)
  - **Distance**: Minimum 384 blocks, average 500-1000 blocks from spawn
  - **Biomes**: Restricted to forests and taiga only (#minecraft:is_forest, #minecraft:is_taiga)
  - **Terrain Adaptation**: beard_thin for smooth ground integration on slopes
  - **Removed**: TerraBlender dependency, Strange Forest biome (chunk access limitations)
  - **Removed**: StrangeLeavesProcessor, AncientRuinsSignalBlock (experimental code)
  - **Documentation**: IMPLEMENTATION_LOG.md with 6 attempted approaches
  - **Files**: ancient_ruins.json, has_ancient_ruins.json, build.gradle files, removed TerraBlender code
- [X] T115o [P] [US2] Add tall tower to Ancient Ruins structure for long-range visibility
  - **Purpose**: Physical structure (20-30 blocks tall) for reliable long-range discovery
  - **Design**: Stone/brick tower with glowstone/sea lantern at top for nighttime visibility
  - **Implementation**: Edit Ancient Ruins NBT structure file with Structure Block
  - **Expected Range**: Visible from 100-200 blocks away due to height
  - **Result**: Added tall tower to Ancient Ruins structure for improved discoverability from distance

### Master Clock Tower & Boss Battle Improvements (US3 Bug Fixes & Enhancements - High Priority)

**Purpose**: Master Clock Towerとボス戦の問題修正と体験向上

- [X] T223 [P] [US3] Rename "boss_room_door" to time-themed name in localization files (e.g., "Time Tyrant's Chamber Door", "Temporal Sanctum Door")
  - **Decision**: No changes needed - keeping generic "Boss Room Door" name
  - **Rationale**:
    1. "boss_room_door" is used across 5+ boss structures (Master Clock, Guardian Vault, Clockwork Depths, Phantom Catacombs, Entropy Crypt)
    2. Generic name is appropriate for multi-purpose use across all bosses
    3. "boss room" terminology is already consistent project-wide (boss_room_protected, boss_room_locked, BossRoomDoorBlock, etc.)
    4. Large impact scope (NBT structures, Java classes, message keys, translations) with minimal benefit
  - **Current Translation**: EN: "Boss Room Door", JP: "ボス部屋のドア"
- [X] T224 [P] [US3] Fix Clock Tower teleporter block durability issue (make unbreakable or add protection mechanism to prevent breaking)
  - **Completed**: Implemented survival mode protection in ClockTowerTeleporterBlock.java
  - **Implementation**: playerWillDestroy() cancels destruction, getDestroyProgress() returns 0, attack() shows message
  - **Result**: Block is indestructible in survival mode, breakable only in creative mode
- [X] T225 [P] [US3] Fix Clock Tower teleporter destination corruption when re-placed (validate teleport coordinates on placement, warn player if invalid)
  - **Completed**: Issue mitigated by T224 survival mode protection
  - **Analysis**: Teleporter block cannot be broken/re-placed in survival mode
  - **Fallback**: Creative mode uses relative offset (-8 blocks) when targetPos is null
  - **Result**: No gameplay impact in survival; creative mode has acceptable fallback behavior
- [X] T226 [P] [US3] Add boss room access control - require Ancient Gears before allowing entry (check inventory on button press, display message if missing)
  - **Completed**: Implemented in BlockEventHandler.java
  - **Implementation**: hasRequiredAncientGears() checks player inventory for 3+ Ancient Gears
  - **Behavior**: Door opens only if player has required items; displays locked/unlocked message
  - **Files**: BlockEventHandler.java (lines 164-169, 442-457)
- [X] T227 [P] [US3] Fix water disappearing from Master Clock structure NBT (investigate waterlogging state preservation, ensure water blocks save correctly)
  - **Completed**: Applied complete waterlogging prevention system to all Master Clock template pools
  - **Implementation**: All 4 pools (surface, stairs, corridor, boss_room) use `convert_decorative_water` processor
  - **System**: StructureStartMixin removes Aquifer water, CopyFluidLevelProcessor preserves decorative water
  - **Result**: Water features preserved correctly, unwanted waterlogging prevented
- [X] T228 [P] [US3] Add glowing effect to Time Tyrant entity for visibility (apply Glowing status effect or custom shader/outline rendering)
  - **Decision**: No changes needed - glowing effect not necessary
  - **Rationale**: In-game testing shows Time Tyrant actively pursues players and stays visible regardless of dungeon layout
  - **AI Behavior**: Entity's aggressive proximity-seeking AI ensures visibility without additional visual effects
- [X] T229 [P] [US3] Fix Time Tyrant teleport suffocation bug (validate teleport destination has 2+ air blocks above, revert position if invalid)
  - **Completed**: Implemented comprehensive safe teleport validation in TimeTyrantEntity.java
  - **Changes**:
    - `findSafeGroundPosition()`: Scans ±3 blocks vertically to find valid floor with 5-block clearance
    - `isSafeTeleportPosition()`: Validates 5 blocks of vertical clearance (4.0 height + 1.0 margin)
    - `teleportToPosition()`: Validates after teleport, reverts if stuck in blocks
    - `isStuckInBlocks()`: Checks 3x3x5 area for solid blocks
    - `hasWallBetween()`: Prevents teleporting through walls (line-of-sight check)
    - `isAreaSafe()`: Checks 3x3x5 area around position for safe landing
  - **Bounding Box Fix**: Updated entity height from 3.0f to 4.0f in ModEntities.java (includes head/horns)
  - **Door Interaction Fix**: Fixed BlockProtectionEventHandler (Fabric) to only block block placement, not door/button interactions
  - **Result**: Time Tyrant now finds valid ground, cannot teleport through walls, and cannot get stuck in walls/ceilings
- [X] T229a [US3] Investigate Time Tyrant buff/debuff behavior (verify if Slowness is incorrectly applied instead of intended buff, check status effect logic)
  - **Completed**: Identified and fixed issue where Time Tyrant was receiving unintended Slowness IV/V from Time Distortion Effect
  - **Root Cause**: Time Tyrant extends Monster class but was not excluded from TimeDistortionEffect.isHostileMob()
  - **Impact**: Phase 2 Time Acceleration (Speed II buff) was being overridden by Slowness IV/V, making the ability non-functional
  - **Fix**: Added Time Tyrant exclusion to TimeDistortionEffect.java (similar to existing Time Guardian exclusion)
  - **Verification**: Tested in-game (Survival mode) - Time Acceleration now works correctly with visible particle effects and increased speed
  - **Documentation**: Detailed investigation results recorded in research.md (lines 3401-3549)
- [X] T230 [US3] Test all Master Clock Tower and boss battle fixes in-game
  - **Completed**: Tested T224-T229 fixes in-game
  - **Verified**: Clock Tower teleporter protection, boss room access control, Time Tyrant teleport safety, buff/debuff behavior all working correctly

### Ancient Gears Acquisition (US3 Bug Fix - Critical Priority)

**Purpose**: 古代の歯車(Ancient Gears)の入手手段が不明瞭な問題を修正

**Issue**: プレイヤーが入手場所や方法を把握できない

- [X] T231 [US3] Investigate current Ancient Gears acquisition methods (check loot tables, mob drops, crafting recipes, structure loot)
  - **Completed**: Fixed loot table directory structure (loot_tables/ → loot_table/)
  - **Root Cause**: Clockwork Sentinel loot table had iron_ingot placeholder instead of ancient_gear
  - **Commit**: 4c45069
- [X] T232 [P] [US3] Add or improve Ancient Gears acquisition methods (add to Clockwork Sentinel drops, structure loot chests, or crafting recipe)
  - **Completed**: Updated clockwork_sentinel.json with guaranteed ancient_gear drop
  - **Added**: Crafting recipe (Enhanced Clockstone + 4x Time Crystal + 4x Iron Ingot → Ancient Gear)
  - **Commit**: 4c45069
- [X] T233 [US3] Test Ancient Gears acquisition and verify clear acquisition path exists
  - **Completed**: Tested and verified Ancient Gear drops from Clockwork Sentinel
  - **Commit**: 4c45069

### Additional Boss Enemies (US3 Enhancement - Medium Priority)

**Purpose**: 4つの追加ミニボスとChrono Aegisシステムの実装

**Implementation Status**: Boss entities, Chrono Aegis, Guardian Vault, and Clockwork Depths complete. Temporal Phantom and Entropy Keeper structures pending

**Reference**: See research.md "Additional Bosses Implementation Plan (T234-T238)"

#### T234: Chronos Warden (クロノスの監視者) - COMPLETED
- [x] T234a-i: Entity, item, renderer, texture, translations implemented
- [x] T234j-o: Guardian Vault structure generation (COMPLETED - see T239)

#### T235: Clockwork Colossus (機械仕掛けの巨像) - COMPLETED
- [x] T235a-l: Entity, Gear Projectile, item, renderer, translations implemented
- [x] T235m-r: Clockwork Depths structure generation (COMPLETED)
  - Multi-level Jigsaw structure: tower (surface) → gearshaft → engine_room → archive_vault
  - ClockworkColossusSpawner for proximity-based boss spawning (Clockwork Block markers)
  - Template pools: tower_pool, gearshaft_pool, engine_room_pool, archive_vault_pool
  - Spawns in chronosphere_desert and chronosphere_mountain biomes
  - Boss spawns when player approaches within 20 blocks of Clockwork Block markers

#### T236: Temporal Phantom (時間の幻影) - PARTIALLY COMPLETED
- [x] T236a-l: Entity, item, renderer, texture, translations implemented
- [x] T236m-r: Phantom Catacombs structure generation (COMPLETED)
  - Jigsaw-based underground maze structure with entrance, corridors, maze rooms, dead-ends
  - Boss room programmatic placement with collision detection and 2-stage fallback system
  - TemporalPhantomSpawner for proximity-based boss spawning (player enters boss_room)
  - Complete waterlogging prevention system (DecorativeWaterFluid, CopyFluidLevelProcessor, StructureStartMixin)
  - Template pools: entrance_pool, corridor_pool, maze_room_pool, room_7_pool, boss_room_pool, terminator_pool
  - Spawns in chronosphere_forest and chronosphere_swamp biomes
  - Boss spawns when player enters boss_room (21x21x9 area, 1-second interval check)
  - Terrain adaptation: "none" (prevents surface terrain deletion)
- [ ] T236s: Create custom texture for Temporal Phantom (PENDING)
  - Current: Uses time_guardian.png texture (identical MD5 hash)
  - Required: Spectral/ghostly appearance (purple/blue semi-transparent theme)
  - File: common/src/main/resources/assets/chronosphere/textures/entity/temporal_phantom.png

#### T237: Entropy Keeper (エントロピーの管理者) - COMPLETED
- [x] T237a-m: Entity, item, renderer, texture, translations implemented
- [x] T237n-r: Entropy Crypt structure generation (COMPLETED)
  - Jigsaw structure: entrance (surface) → stairs → main (Boss Chamber + Vault)
  - EntropyCryptTrapdoorBlock triggers boss spawn when player attempts to open
  - Custom BlockSetType allows hand interaction with iron sounds
  - ACTIVATED property tracks boss spawn state
  - Template pools: entrance_pool, stairs_pool, main_pool
  - Spawns in chronosphere_swamp and chronosphere_forest biomes
  - Mixin refmap configuration for production JAR compatibility

#### T238: Chrono Aegis System - PARTIALLY COMPLETED
- [x] T238a-f: Chrono Aegis item, effect, recipe, translations implemented
- [x] T238g-j: Time Tyrant integration (Time Stop Resistance, Dimensional Anchor, Temporal Shield, Time Reversal Disruption)
- [x] T238k: Implement safe Clarity effect (implemented using PLAYER_POST event in EntityEventHandler)
- [x] T238l: Multiplayer safeguards implemented
- [x] T238m: Test Chrono Aegis crafting from 4 boss drops
  - **Completed**: Changed recipe to shapeless (1 of each boss item instead of 2)
  - **Completed**: Added recipe unlock advancement (triggers on any boss item obtained)
  - **Result**: Recipe unlocks when player obtains any of the 4 boss items, providing clear guidance
  - **Commit**: fab50ba
- [x] T238n: Test Chrono Aegis effects against Time Tyrant (Time Stop Resistance, Dimensional Anchor, Temporal Shield, Time Reversal Disruption, Clarity)
  - **Completed**: Tested in survival mode against Time Tyrant
  - **Result**: All 5 effects appear to be working correctly (some effects difficult to verify precisely in survival, but behavior is as expected)
  - **Effects verified**: Time Stop Resistance, Dimensional Anchor, Temporal Shield, Time Reversal Disruption, Clarity
- [ ] T238o: Test multiplayer scenario (2+ players with Chrono Aegis, verify debuff flags don't stack)
- [ ] T238p: Balance testing: Time Tyrant fight with vs without Chrono Aegis
- [ ] T238q: Full playthrough test
- [ ] T238u-x: Documentation updates

#### Remaining High-Priority Tasks
- [x] T239: Guardian Vault structure generation (COMPLETED)
  - Jigsaw structure with entrance (surface) + main hall (underground)
  - StructureStartMixin for waterlogging prevention (removes Aquifer water before placement)
  - ChronosWardenSpawner for boss spawning (Boss Room Door with DoorType: "guardian_vault")
  - Loot table for treasure chests
  - Spawns in chronosphere_plains and chronosphere_forest biomes
- [x] T240: Fix Clarity auto-cleanse feature (implemented using event system in EntityEventHandler.handleChronoAegisClarity)
- [ ] T241: Comprehensive testing (boss spawning, Chrono Aegis crafting, Time Tyrant fight)

#### Future Improvements (Low Priority)
- [ ] T242: Improve boss visual diversity (custom models instead of reusing TimeGuardianModel)
- [ ] T243: Add boss-specific sound effects
- [ ] T244: Create advancement system for defeating all 4 bosses

### Tests for User Story 3

- [ ] T123 [P] [US3] Write GameTest for Master Clock structure generation in common/src/test/java/com/chronosphere/integration/MasterClockTest.java
- [ ] T124 [P] [US3] Write GameTest for Time Tyrant boss fight in common/src/test/java/com/chronosphere/integration/TimeTyrantFightTest.java
- [ ] T125 [P] [US3] Write unit test for Chronoblade AI skip probability in common/src/test/java/com/chronosphere/unit/ChronobladeTest.java
- [ ] T126 [P] [US3] Write unit test for Time Guardian Mail rollback logic in common/src/test/java/com/chronosphere/unit/TimeGuardianMailTest.java
- [ ] T127 [P] [US3] Write GameTest for Echoing Time Boots decoy in common/src/test/java/com/chronosphere/integration/DecoyTest.java

### Future Boss Battle Enhancements (US3 - Phase 7+)

**Purpose**: Master Clock大規模化とテストプレイ後のバランス調整

**Dependencies**:
- T171j-k: Master Clockダンジョンを現在の4部屋から大規模化
- T171l-n: テストプレイによるゲームバランス確認後に実装

- [ ] T171j [US3] Design large-scale Master Clock dungeon layout (20+ rooms, multi-floor, complex maze structure, environmental hazards)
- [ ] T171k [US3] Implement large-scale Master Clock dungeon generation system in common/src/main/java/com/chronosphere/worldgen/structures/MasterClockLargeGenerator.java
- [ ] T171l [US3] Implement Temporal Anchor environmental mechanic in common/src/main/java/com/chronosphere/blocks/TemporalAnchorBlock.java (3 anchors in dungeon, destroy to weaken Time Tyrant: teleport frequency, Time Acceleration, AoE range)
- [ ] T171m [US3] Adjust Time Tyrant base stats based on playtesting (HP 500→400, Attack 18→15, Defense 15→12) if needed
- [ ] T171n [US3] Implement Grave Marker death recovery system in common/src/main/java/com/chronosphere/mechanics/GraveMarkerHandler.java (death marker at boss room entrance, safe item recovery from outside)

### Guaranteed Structure Placement System (US2/US3 Enhancement - Medium Priority)

**Purpose**: 構造物が一定距離内に必ず生成されることを保証し、プレイヤーの探索体験を向上

**Background**:
- 現在の`random_spread`配置は最小距離を保証するが、最大距離は保証しない
- プレイヤーが構造物を見つけられずゲームが退屈になる懸念
- Time Keeperとの取引にはTime Compassが必要だが、肝心のTime Keeperに会えなければ意味がない

**Reference**: See research.md "Guaranteed Structure Placement Research (2025-12-01)"

#### Phase 1: Time Keeper Village (プログラム的配置)
- [ ] T274 [P] [US2] Design Time Keeper Village structure concept (small settlement with 1-2 Time Keepers, basic shelter, trading post)
- [ ] T275 [P] [US2] Create Time Keeper Village NBT structure file (time_keeper_village.nbt)
- [ ] T276 [P] [US2] Implement TimeKeeperVillagePlacer.java for programmatic placement near spawn (64 blocks)
  - Trigger on first Chronosphere entry
  - Find suitable position on surface
  - Use saved data to track placement status
- [ ] T277 [US2] Test Time Keeper Village generation and Time Keeper spawning

#### Phase 2: Custom StructurePlacement Type (汎用システム)
- [ ] T278 [P] [US3] Create GuaranteedRadiusStructurePlacement.java extending StructurePlacement
  - `radius_chunks`: 保証半径（チャンク単位）
  - `min_distance`: 最小距離（チャンク単位）
  - Implement isPlacementChunk() with seeded random
- [ ] T279 [P] [US3] Create ModStructurePlacementTypes.java registry class
  - Register GUARANTEED_RADIUS placement type
  - Setup codec for JSON serialization
- [ ] T280 [P] [US3] Create Architectury platform-specific registration (Fabric/NeoForge)
- [ ] T281 [US3] Test custom placement type with test structure
- [ ] T282 [P] [US3] Apply guaranteed_radius placement to existing structures (Ancient Ruins, Desert Clock Tower, Master Clock)
- [ ] T283 [US3] Comprehensive testing across multiple seeds and locations

**Implementation Notes**:
- Phase 1 uses Phantom Catacombs boss room pattern (proven reliable)
- Phase 2 integrates with standard worldgen system (locate command compatible)
- Phase 2 can be applied to other structures after validation

### Custom Terrain Features (US3 Enhancement - Medium Priority)

**Purpose**: 時間をテーマにした独自地形を追加し、地形生成の独自性を向上

**Note**: 現在はオーバーワールドと同じ地形生成アルゴリズムを使用。時間のテーマに沿った特殊地形を追加して探索の面白さを増す

- [ ] T260 [US3] Design Temporal Rift Canyon structure concept (distorted terrain, floating blocks, time crystal veins exposed in walls, visual effects)
- [ ] T261 [P] [US3] Create Temporal Rift Canyon structure NBT in common/src/main/resources/data/chronosphere/structures/temporal_rift_canyon.nbt (canyon with irregular terrain, time crystal ores)
- [ ] T262 [P] [US3] Create Floating Clockwork Ruins structure NBT in common/src/main/resources/data/chronosphere/structures/floating_clockwork_ruins.nbt (floating islands with broken clockwork mechanisms, loot chests)
- [ ] T263 [P] [US3] Create Time Crystal Caverns feature in common/src/main/java/com/chronosphere/worldgen/features/TimeCrystalCavernsFeature.java (underground crystal formations, glowing effects)
- [ ] T264 [P] [US3] Configure custom terrain feature placement in common/src/main/resources/data/chronosphere/worldgen/structure_set/ and placed_feature/ (rare placement, biome-specific)
- [ ] T265 [US3] Test custom terrain features in-game and verify they generate correctly without breaking existing structures

**Checkpoint**: 全User Storyが独立して機能すること

---

### Cross-Loader Testing

- [ ] T172 [P] Run all GameTests on Fabric loader using ./gradlew :fabric:runGameTest
- [ ] T173 [P] Run all GameTests on NeoForge loader using ./gradlew :neoforge:runGameTest
- [ ] T174 Verify entity renderer registration for Fabric (standard API) in fabric/src/main/java/com/chronosphere/fabric/client/ChronosphereClientFabric.java
- [ ] T175 Verify entity renderer registration for NeoForge (manual event registration) in neoforge/src/main/java/com/chronosphere/neoforge/event/EntityRendererHandler.java
- [X] T176 Test portal mechanics on both loaders for consistency
  - Implemented Custom Portal API Reforged integration for NeoForge (cpapireforged 1.2.2)
  - Created CustomPortalNeoForge.java for portal registration
  - Added Mixin (CustomPortalBlockMixin) to override particle effects with custom orange particles
  - Updated PlayerEventHandler to recognize cpapireforged:custom_portal_block for portal deactivation
  - Verified: portal creation, teleportation, deactivation on entry, stabilization with Portal Stabilizer
- [ ] T177 Verify time distortion effect consistency across loaders

### Performance Optimization

- [ ] T178 [P] Optimize entity tick rate for time distortion (5-tick interval) in EntityEventHandler.java
- [ ] T179 [P] Implement portal registry caching in PortalRegistry.java
- [ ] T180 [P] Optimize boss AI state machine in TimeGuardianAI.java and TimeTyrantAI.java
- [ ] T181 Profile server performance with Spark profiler
- [ ] T182 Ensure server load increase stays within +10% threshold per success criteria SC-008

### Localization (Final Review)

**NOTE**: Basic localization is completed in each User Story (T088a-c, T122a-c, T171a-c). This phase is for final review and completeness check.

- [ ] T183 [P] Review English localization file for completeness and consistency
- [ ] T184 [P] Review Japanese localization file for completeness and consistency

### Documentation

- [ ] T185 [P] Update README.md with build instructions for both loaders
- [ ] T186 [P] Create CurseForge mod page description
- [ ] T187 [P] Create Modrinth mod page description
- [ ] T188 [P] Write player guide in docs/player_guide.md
- [ ] T189 [P] Write developer guide in docs/developer_guide.md

### Final Validation

- [ ] T190 Run full test suite for both loaders using ./gradlew test
- [ ] T191 Validate quickstart.md manual testing checklist
- [ ] T192 Build final JARs for distribution using ./gradlew build
- [ ] T193 Test Fabric JAR in production Minecraft 1.21.1 + Fabric environment
- [ ] T194 Test NeoForge JAR in production Minecraft 1.21.1 + NeoForge environment

---

## Notes

- [P]タスク = 異なるファイル、依存関係なし
- [Story]ラベルはタスクを特定のUser Storyにマッピング (トレーサビリティ確保)
- 各User Storyは独立して完了・テスト可能であるべき
- 実装前にテストがFAILすることを確認
- 各タスクまたは論理的なグループ後にコミット
- 任意のCheckpointで停止してストーリーを独立して検証
- 避けるべき: 曖昧なタスク、同一ファイルの競合、独立性を破壊するストーリー間依存
- Architectury特有の注意: commonモジュールで80%のロジックを実装、ローダー固有実装は20%に抑制
- エンティティレンダリング: Fabricは標準API、NeoForgeは手動イベント登録 (Issue #641対応)

---

