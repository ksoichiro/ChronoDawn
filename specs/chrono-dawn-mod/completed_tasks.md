# Completed Tasks: Chrono Dawn Mod

**Branch**: `main`

This file contains all completed task sections that have been moved from tasks.md for better organization.

**Last Updated**: 2026-01-09

---

## License Compliance & Guidebook Migration (✅ REVERTED TO PATCHOULI)

**Purpose**: Attempted migration from Patchouli (CC-BY-NC-SA 3.0) to Lavender (MIT) for licensing freedom.

**Background**:
- Patchouli is licensed under CC-BY-NC-SA 3.0 (NonCommercial, ShareAlike)
- Initial concern about commercial use restrictions
- Lavender appeared as MIT-licensed alternative

**Critical Discovery (2025-12-23)**:
- **Lavender is Fabric-only** - No NeoForge support as of v0.1.15+1.21
- NeoForge users would have no guidebook (unacceptable experience gap)
- Multi-loader support is core project requirement

**Final Decision**:
- **Reverted to Patchouli 1.21.1-92** (supports both Fabric and NeoForge)
- **License Clarification**: Patchouli API dependency usage (not Jar-in-Jar) is acceptable per official docs
- **Future Option**: Custom UI implementation documented in `custom-guidebook-ui-plan.md` for post-1.0
- Project now uses **LGPL-3.0 License** (after Patchouli removal)

**Tasks**:

- [x] T500 [P] Research Lavender implementation and architecture
- [x] T501 [P] Remove Patchouli dependencies
- [x] T502 [P] Add Lavender dependencies (Fabric only - NeoForge not supported)
- [x] T503 [P] Create Lavender guidebook structure
- [x] T504 [P] Implement basic guidebook content (English)
- [x] T505 [P] Implement Japanese guidebook content
- [x] T506 [P] Create guidebook item and recipe
- [x] T507 [P] Test guidebook functionality
- [x] T508 [P] Update project documentation
- [x] T509 [P] Update LICENSE and licensing documentation
- [x] T510 [P] Final verification and cleanup (Lavender migration)

**Reversion Tasks** (2025-12-23):
- [x] T511 [P] Revert to Patchouli dependencies
- [x] T512 [P] Update documentation for Patchouli reversion

**Final Status (2025-12-23)**:
- ✅ Patchouli 1.21.1-92 restored for both Fabric and NeoForge
- ✅ All Lavender code and references removed
- ✅ Bilingual guidebook (English/Japanese) functional
- ✅ Multi-loader support maintained (Fabric and NeoForge)
- ✅ No build errors on either loader
- ✅ Documentation fully updated
- 📋 Future option: Custom UI implementation plan documented

---

## Custom Guidebook UI Implementation (✅ COMPLETED 2025-12-26)

**Purpose**: Implement custom, dependency-free guidebook UI system to remove Patchouli dependency and achieve complete licensing independence.

**Background**:
- Removed Patchouli 1.21.1-92 (CC-BY-NC-SA 3.0) dependency
- Custom UI provides: complete control, zero dependencies, full license ownership
- Multi-loader compatible via Architectury

**Implementation**:
- Custom "Chronicle" guidebook system with category navigation
- Entry display with text rendering and multi-page support
- Recipe integration using vanilla UI components
- Bilingual support (English + Japanese)
- Multi-loader via Architectury MenuRegistry

**Tasks**:

- [x] T700 [P] Phase 1: Implement basic structure (2025-12-26)
- [x] T701 [P] Phase 2: Implement category navigation (2025-12-26)
- [x] T702 [P] Phase 3: Implement entry display (2025-12-26)
- [x] T703 [P] Phase 4: Add bilingual support (2025-12-26)
- [x] T704 [P] Phase 5: Migrate content from Patchouli (2025-12-26)
- [x] T705 [P] Phase 6: Polish and multi-loader testing (2025-12-26)
- [x] T706 [P] Remove Patchouli dependency (2025-12-26)

**Final Status**:
- ✅ Chronicle guidebook fully functional
- ✅ All Patchouli content migrated
- ✅ Bilingual support working (English/Japanese)
- ✅ Zero external dependencies
- ✅ Patchouli completely removed
- ✅ Enabled LGPL-3.0 license migration

---

## License Migration to LGPL-3.0 (✅ COMPLETED 2025-12-27)

**Purpose**: Migrated project license from "All Rights Reserved" to LGPL-3.0 to enable open-source distribution.

**Background**:
- Previous license: All Rights Reserved (proprietary)
- Patchouli CC-BY-NC-SA 3.0 dependency blocked commercial use
- Custom Chronicle UI removed Patchouli dependency
- Enabled LGPL-3.0 migration

**Key Decision**: LGPL-3.0 for Minecraft mod compatibility
- Standard in Minecraft modding community (Forge, Applied Energistics 2)
- Derivative works must remain LGPL-3.0 and publish source
- Commercial use allowed but requires source disclosure

**Tasks**:

- [x] T600 [Documentation] Remove Patchouli references from documentation (2025-12-27)
- [x] T601 [License] Replace LICENSE file with LGPL-3.0 text (2025-12-27)
- [x] T602 [Documentation] Update README.md license section (2025-12-27)
- [x] T603 [Metadata] Update fabric.mod.json license field (2025-12-27)
- [x] T604 [Metadata] Update neoforge.mods.toml license field (2025-12-27)
- [x] T605 [Platform] Update docs/curseforge_description.md license section (2025-12-27)
- [x] T606 [Platform] Update docs/modrinth_description.md license section (2025-12-27)
- [x] T607 [Optional] Add LGPL-3.0 headers to main source files (2025-12-27)
  - Added to ChronoDawn.java, ChronoDawnFabric.java, ChronoDawnNeoForge.java
  - Header: Copyright (C) 2025 ksoichiro + LGPL-3.0 version 3 only boilerplate
- [x] T608 [Guidelines] Update CLAUDE.md project guidelines (2025-12-27)

**Final Status**:
- ✅ LICENSE file replaced with LGPL-3.0
- ✅ All metadata files updated
- ✅ Documentation updated across all platforms
- ✅ License headers added to main entry points
- ✅ CLAUDE.md updated with compliance guidelines
- ✅ Project is now fully open source under LGPL-3.0

---

## Screenshots & Release Preparation (✅ COMPLETED 2025-12-27)

**Purpose**: Created high-quality screenshots for CurseForge and Modrinth galleries to ensure successful initial release approval.

**Background**:
- Both platforms require screenshots for mod listings
- Quality screenshots critical for first impressions
- Screenshots showcase: structures, bosses, dimension, items
- Resolution: 1920x1080 (16:9 aspect ratio)
- CurseForge icon requirement: minimum 400x400px

**Tasks**:

- [x] T513 [P] Set up screenshot infrastructure
- [x] T514 [P] Create project icon (512x512px)
- [x] T515 [P] Capture essential screenshots (Priority 1)
  - 8 essential screenshots + 1 featured image (9 total)
  - All 1920x1080 PNG format, F1 to hide HUD
  - Used Complementary Shaders Unbound for enhanced visuals
- [x] T516 [P] Capture supplementary screenshots (Priority 2) - PARTIAL
- [x] T517 [P] Optimize and organize screenshots
- [x] T518 [P] Upload to CurseForge gallery (2025-12-27)
- [x] T519 [P] Upload to Modrinth gallery (2025-12-27)
- [x] T520 [P] Update documentation with screenshots

**Final Status**:
- ✅ 9 high-quality screenshots captured
- ✅ Project icon created (512x512px)
- ✅ All screenshots uploaded to CurseForge
- ✅ All screenshots uploaded to Modrinth
- ✅ Documentation updated
- ✅ Ready for initial release approval

---

### Basic Resources (US1 Enhancement - High Priority)

**Purpose**: Chrono Dawn内でサバイバルプレイに必要な基本リソースを入手可能にする

**Note**: 石炭は松明や燃料として必須。長期滞在を可能にするため優先度は高い

- [x] T265 [P] [US1] Add coal ore generation in Chrono Dawn (frequency, Y-level distribution, biome placement)
  - **Completed**: Added coal ore to all 9 Chrono Dawn biomes
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

- [x] T268 [P] [US1] Create Time Wood Boat and Time Wood Chest Boat items and entities
  - **Completed**: Created unified ChronoDawnBoat and ChronoDawnChestBoat entities with ChronoDawnBoatType enum
  - ChronoDawnBoatItem handles all boat types with type parameter
  - Registered entities in ModEntities, items in ModItems
- [x] T269 [P] [US1] Create Dark Time Wood Boat and Dark Time Wood Chest Boat items and entities
  - **Completed**: Uses same ChronoDawnBoat/ChronoDawnChestBoat with DARK_TIME_WOOD type
  - Items registered with ChronoDawnBoatType.DARK_TIME_WOOD
- [x] T270 [P] [US1] Create Ancient Time Wood Boat and Ancient Time Wood Chest Boat items and entities
  - **Completed**: Uses same ChronoDawnBoat/ChronoDawnChestBoat with ANCIENT_TIME_WOOD type
  - Items registered with ChronoDawnBoatType.ANCIENT_TIME_WOOD
- [x] T271 [P] [US1] Add boat crafting recipes (planks → boat, boat + chest → chest boat)
  - **Completed**: Created 6 shaped recipes (3 boat variants: 5 planks in boat shape)
  - Created 6 shapeless recipes (3 chest boat variants: boat + chest)
  - Note: Recipe unlock advancements deferred (not blocking functionality)
- [x] T272 [P] [US1] Create boat textures and models for all 3 variants
  - **Completed**: Extracted vanilla oak boat textures and applied color transformations
  - Time Wood: R×0.95, G×1.17, B×0.85 (yellowish-olive)
  - Dark Time Wood: R×0.70, G×0.70, B×0.75 (darker tone)
  - Ancient Time Wood: R×0.80, G×0.75, B×0.70 (aged/grayish)
  - Created item textures, entity textures (boat/ and chest_boat/), and item models
  - Created custom renderers (ChronoDawnBoatRenderer, ChronoDawnChestBoatRenderer)
  - Registered renderers in both Fabric and NeoForge client
- [x] T273 [US1] Test boat functionality (movement, durability, chest storage) in Chrono Dawn ocean biome
  - Test boat placement and riding ✓ (Fabric)
  - Test chest boat storage ✓ (Fabric)
  - Test crafting recipes ✓ (Fabric)
  - Test boat breaking and item drops ✓ (Fabric)
  - Verify entity rendering on Fabric ✓
  - Fixed chest boat water rendering (waterPatch)
  - Added recipe advancement files (unlock on water entry / boat possession)
- [x] T273a [US1] Verify boat functionality on NeoForge
  - **Completed**: Verified boat placement, riding, chest storage
  - **Completed**: Verified entity rendering
  - **Result**: All boat functionality works correctly on NeoForge after build issues were resolved

### Biome Enhancements (US1 Enhancement - Medium Priority)

**Purpose**: バイオームの見た目と大きさを調整し、Chrono Dawnの独自性を向上

- [x] T298 [P] [US1] Add distinctive features to Snowy biome (ice structures, frozen time effects, unique blocks) to differentiate from Overworld
  - **Completed**: Added ice pillars with varied heights (4-11 blocks) using packed_ice, blue_ice, and frozen_time_ice
  - Random thick 3x3 clusters (25% chance), reduced density for natural look at biome boundaries
  - Files: ice_pillar.json, ice_pillar_cluster.json, ice_pillar_random.json, ice_spike_placed.json
- [x] T299 [P] [US1] Adjust biome size/scale in dimension_type/chronodawn.json or noise settings to reduce biome area
  - **Completed**: Implemented BiomeScalingMixin with 2.5x coordinate scaling (biomes ~40% smaller)
  - Adjusted continentalness parameters to align with vanilla terrain generation
  - File: BiomeScalingMixin.java, dimension/chronodawn.json
  - **Custom Noise Settings Experiment** (attempted but not used):
    - Created simple custom noise_settings and density_functions
    - Result: Terrain generation failed (Y=-40 to 320 steep mountains, lava seas, flat summits)
    - Issue: Parameter tuning extremely difficult, requires deep understanding of density functions
    - Estimated effort for proper implementation: 10-15 hours
    - Conclusion: Using vanilla `minecraft:overworld` settings is more practical
    - Reference: If future terrain customization is needed, start with vanilla density functions as base
- [x] T300 [P] [US1] Change grass drop from vanilla seeds to Temporal Wheat Seeds (modify grass block loot table)
  - **Completed**: Override vanilla short_grass loot table to drop time_wheat_seeds (12.5% chance) in Chrono Dawn only
  - File: minecraft/loot_table/blocks/short_grass.json

### Custom Mobs (US1 Enhancement - High Priority)

**Purpose**: 時間をテーマにした独自モブを追加し、ディメンションの独自性とゲームプレイの多様性を向上

**Note**: ボス以外の通常モブがないという問題を解決。敵対・中立・友好モブを追加して探索体験を豊かにする

- [X] T200 [US1] Design custom mob concepts (hostile, neutral, friendly with time theme, behavior patterns, drops, spawn conditions)
- [X] T201 [P] [US1] Create Temporal Wraith entity in common/src/main/java/com/chronodawn/entities/mobs/TemporalWraithEntity.java (hostile, phases through blocks when hit, inflicts Slowness II on attack)
- [X] T202 [P] [US1] Create Clockwork Sentinel entity in common/src/main/java/com/chronodawn/entities/mobs/ClockworkSentinelEntity.java (hostile, immune to time distortion effects, drops Ancient Gears)
- [X] T203 [P] [US1] Create Time Keeper entity in common/src/main/java/com/chronodawn/entities/mobs/TimeKeeperEntity.java (neutral, villager-like trading for time-related items)
- [X] T204 [P] [US1] Register custom mobs in ModEntities registry and configure spawning in biomes (Temporal Wraith in forest/plains, Clockwork Sentinel in desert/structures, Time Keeper in libraries)
- [X] T205 [P] [US1] Create custom mob textures and models in common/src/main/resources/assets/chronodawn/textures/entity/
- [X] T206 [P] [US1] Create custom mob loot tables in common/src/main/resources/data/chronodawn/loot_tables/entities/
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
  - **Files**: All 7 biome JSON files (chronodawn_plains.json, chronodawn_forest.json, chronodawn_desert.json, chronodawn_mountain.json, chronodawn_ocean.json, chronodawn_snowy.json, chronodawn_swamp.json)
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
- [X] T213 [P] [US1] Create crop textures for all growth stages in common/src/main/resources/assets/chronodawn/textures/block/
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
  - Added to chronodawn_plains and chronodawn_forest biomes
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
  - **File**: common/src/main/resources/data/chronodawn/structure/forgotten_library.nbt
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

- [X] T089 [P] [US2] Write GameTest for Desert Clock Tower generation in common/src/test/java/com/chronodawn/integration/DesertClockTowerTest.java
- [X] T090 [P] [US2] Write unit test for Time Clock cooldown logic in common/src/test/java/com/chronodawn/unit/TimeClockTest.java
- [X] T091 [P] [US2] Write unit test for Spatially Linked Pickaxe drop multiplier in common/src/test/java/com/chronodawn/unit/PickaxeDropTest.java
- [X] T092 [P] [US2] Write GameTest for Time Guardian boss fight in common/src/test/java/com/chronodawn/integration/TimeGuardianFightTest.java

### Player Guidance & Discovery System (US2 Enhancement)

**Purpose**: プレイヤーが構造物やアイテムを発見し、ゲームを進行できるようガイダンスを提供

**Current Issue**: Ancient Ruins、Desert Clock Tower、Master Clock Towerなどの構造物の場所や、アイテムの入手方法について説明がなく、プレイヤーが作者の想定通りに進行するのは困難

**Guidance Methods**: 書物アイテム、村人との取引（地図）、進捗ヒント、構造物の出現頻度調整、ロケーターアイテムなど

- [X] T115f [US2] Research appropriate player guidance methods (book items, advancement hints, villager trades, structure frequency)
- [X] T115g [P] [US2] Create Chronicle of Chrono Dawn book item (guide book explaining dimension mechanics, structures, and progression)
  - Created ChronicleOfChronoDawnItem.java with English version only
  - Book auto-given on first Chrono Dawn entry
  - Added to creative tab as readable vanilla written_book
  - Documented complete worldbuilding in specs/chrono-dawn-mod/lore.md
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
  - Chronicle of Chrono Dawn auto-given to player on first Chrono Dawn entry
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

### Tests for User Story 3

- [X] T123 [P] [US3] Write GameTest for Master Clock structure generation in common/src/test/java/com/chronodawn/integration/MasterClockTest.java
- [X] T124 [P] [US3] Write GameTest for Time Tyrant boss fight in common/src/test/java/com/chronodawn/integration/TimeTyrantFightTest.java
- [X] T125 [P] [US3] Write unit test for Chronoblade AI skip probability in common/src/test/java/com/chronodawn/unit/ChronobladeTest.java
- [X] T126 [P] [US3] Write unit test for Time Guardian Mail rollback logic in common/src/test/java/com/chronodawn/unit/TimeGuardianMailTest.java
- [X] T127 [P] [US3] Write GameTest for Echoing Time Boots decoy in common/src/test/java/com/chronodawn/integration/DecoyTest.java

### Master Clock Structure Improvements (US3 Enhancement - High Priority)

**Purpose**: Master Clock構造物の安全性とゲームプレイバランスの向上

- [x] T301 [P] [US3] Increase Master Clock boss room depth to prevent surface exposure (adjust Y-level in structure NBT or placement config)
  - **Completed**: Implemented Jigsaw stairs extension system
  - Structure size increased from 3 to 10 to allow proper stair extension
  - Surface remains at Y=0 with `project_start_to_heightmap: WORLD_SURFACE_WG`
  - Created master_clock_stairs_bottom.nbt for stair termination
  - Updated stairs_pool.json with both stairs (weight 6) and stairs_bottom (weight 4)
  - Stairs now naturally extend downward (~6-8 segments) from surface to boss room
- [x] T302 [P] [US3] Make all Master Clock walls indestructible (not just boss room) to prevent bypassing Ancient Gears requirement (apply boss_room_protected tag to all structure blocks)
  - **Completed**: Created MasterClockProtectionProcessor for entire structure protection
  - Detects Chiseled Quartz Block and Polished Andesite as structure markers
  - Registers ±50 block XZ protection area (Y=-70 to Y=10) covering entire Master Clock
  - Protection lifts when Time Tyrant is defeated (uses same system as boss room protection)
  - Registered in ModStructureProcessorTypes and integrated into server tick events (Fabric/NeoForge)

### Guaranteed Structure Placement System (US2/US3 Enhancement - Medium Priority)

**Purpose**: 構造物が一定距離内に必ず生成されることを保証し、プレイヤーの探索体験を向上

**Background**:
- 現在の`random_spread`配置は最小距離を保証するが、最大距離は保証しない
- プレイヤーが構造物を見つけられずゲームが退屈になる懸念
- Time Keeperとの取引にはTime Compassが必要だが、肝心のTime Keeperに会えなければ意味がない

**Reference**: See research.md "Guaranteed Structure Placement Research (2025-12-01)"

#### Phase 1: Time Keeper Village (プログラム的配置) - COMPLETED
- [X] T274 [P] [US2] Design Time Keeper Village structure concept (small settlement with 1-2 Time Keepers, basic shelter, trading post)
  - **Completed**: Simple village structure with shelter and trading area
- [X] T275 [P] [US2] Create Time Keeper Village NBT structure file (time_keeper_village.nbt)
  - **File**: common/src/main/resources/data/chronodawn/structure/time_keeper_village.nbt
- [X] T276 [P] [US2] Implement TimeKeeperVillagePlacer.java for programmatic placement near spawn (64 blocks)
  - **Implemented**: TimeKeeperVillagePlacer with TimeKeeperVillageData (SavedData)
  - Placement range: 32-256 blocks from player entry point
  - Foundation filling and progressive search range expansion
  - Spawns 2 Time Keepers programmatically after placement
- [X] T277 [US2] Test Time Keeper Village generation and Time Keeper spawning
  - **Verified**: Village generates near spawn, Time Keepers spawn correctly

#### Phase 2: Custom StructurePlacement Type (汎用システム) - COMPLETED BUT DEPRECATED
- [X] T278 [P] [US3] Create GuaranteedRadiusStructurePlacement.java extending StructurePlacement
  - **Implemented**: GuaranteedRadiusStructurePlacement with radius_chunks constraint
  - **Status**: Deprecated and removed (commit d2c8f0a)
  - **Reason**: Caused /locate command to hang indefinitely
- [X] T279 [P] [US3] Create ModStructurePlacementTypes.java registry class
  - **Implemented**: ModStructurePlacementTypes with GUARANTEED_RADIUS placement type
  - **Status**: Deprecated and removed (commit d2c8f0a)
- [X] T280 [P] [US3] Create Architectury platform-specific registration (Fabric/NeoForge)
  - **Implemented**: Platform-specific registration in ChronoDawn.java
  - **Status**: Deprecated and removed (commit d2c8f0a)
- [X] T281 [US3] Test custom placement type with test structure
  - **Completed**: Testing performed successfully
- [X] T282 [P] [US3] Apply guaranteed_radius placement to existing structures (Ancient Ruins, Desert Clock Tower, Master Clock)
  - **Completed**: Applied to all 3 structures
  - **Status**: Migrated to minecraft:random_spread (commit d2c8f0a)
- [X] T283 [US3] Comprehensive testing across multiple seeds and locations
  - **Completed**: Testing performed, issues identified (/locate hang)
  - **Result**: Migrated to random_spread approach with expanded biome tags

#### Phase 3: Structure Ocean Variants (新規タスク) - COMPLETED
- [X] T287 [P] [US3] Create Desert Clock Tower ocean variant
  - **Implemented**: Jigsaw structure with ocean platform + reused tower
  - **Files**: desert_clock_tower_ocean_platform.nbt, structure_set/desert_clock_tower_ocean.json
  - **Status**: Later removed during random_spread migration (commit d2c8f0a)
  - **Reason**: No longer needed after expanding biome tags to include ocean
- [X] T288 [P] [US3] Create Master Clock ocean variant
  - **Implemented**: Added ocean biome to has_master_clock.json tag
  - **Result**: Existing 7x7 entrance works naturally in ocean biome
  - **Files**: tags/worldgen/biome/has_master_clock.json

**Implementation Notes**:
- Phase 1 (Time Keeper Village) remains active - currently used for guaranteed Time Keeper access
- Phase 2 (GuaranteedRadiusStructurePlacement) deprecated due to /locate compatibility issues
- Phase 3 (Ocean variants) partially removed - Master Clock ocean support retained via biome tags
- Current approach: minecraft:random_spread with expanded biome tags for all structures
- Ocean biome significantly reduced (continentalness: -0.3 → -0.85) to increase land generation

### Cross-Loader Testing

- [X] T172 [P] Run all GameTests on Fabric loader using ./gradlew :fabric:runGameTest
  - Implemented GameTest Framework for Fabric using Architectury Loom's gametest source set
  - Created ChronoDawnGameTestsFabric.java with shared test logic in ChronoDawnGameTestLogic.java
  - Uses @GameTest annotation with FabricGameTest.EMPTY_STRUCTURE template
  - **Current Status**: 92 tests pass (commits: 9cca9db initial, 51934c9 player input tests)
  - **Test Categories**:
    - Entity spawning (12 tests)
    - Block placement (30 tests)
    - Entity attributes (20 tests)
    - Item attributes (24 tests)
    - Player input simulation (6 tests)
- [X] T173 [P] Run all GameTests on NeoForge loader using ./gradlew :neoforge:runGameTestServer
  - Implemented GameTest Framework for NeoForge using @GameTestGenerator pattern
  - Created ChronoDawnGameTestsNeoForge.java with shared test logic in ChronoDawnGameTestLogic.java
  - Uses RegisterGameTestsEvent for test registration and existing structure template
  - **Current Status**: 91 tests pass (commits: 6790b80 initial, 51934c9 player input tests)
  - **Test Categories**: Same as Fabric (player input simulation uses `helper.makeMockPlayer(GameType)` API)
- [X] T174 Verify entity renderer registration for Fabric (standard API) in fabric/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java
  - **Verified**: 15 entities and 7 model layers registered using standard Fabric API
  - Uses `EntityRendererRegistry.register()` and `EntityModelLayerRegistry.registerModelLayer()`
  - All custom renderers properly implemented in common/client package
- [X] T175 Verify entity renderer registration for NeoForge (manual event registration) in neoforge/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java
  - **Verified**: 15 entities and 7 model layers registered using event-based system
  - Uses `@SubscribeEvent` with `EntityRenderersEvent.RegisterRenderers` and `EntityRenderersEvent.RegisterLayerDefinitions`
  - All entity types and model layers match Fabric implementation
  - Proper event bus registration with `@EventBusSubscriber`
- [X] T176 Test portal mechanics on both loaders for consistency
  - Implemented custom portal system Reforged integration for NeoForge (cpapireforged 1.2.2)
  - Created CustomPortalNeoForge.java for portal registration
  - Added Mixin (CustomPortalBlockMixin) to override particle effects with custom orange particles
  - Updated PlayerEventHandler to recognize cpapireforged:custom_portal_block for portal deactivation
  - Verified: portal creation, teleportation, deactivation on entry, stabilization with Portal Stabilizer
- [X] T177 Verify time distortion effect consistency across loaders
  - **Verified on Fabric**: Time Distortion (Slowness IV) correctly applied to hostile mobs
  - **Verified on NeoForge**: Time Distortion effect consistent with Fabric
  - **Verified Exclusions**: Time Keeper, Time Guardian, Time Tyrant, Floq correctly excluded
  - **Note**: Time Keeper's slow movement is due to base movement speed (0.2), not Slowness effect
  - **Eye of Chronos Enhancement**: Slowness V vs IV difference is subtle (15% difference), difficult to perceive visually

### Localization (Final Review)

**NOTE**: Basic localization is completed in each User Story (T088a-c, T122a-c, T171a-c). This phase is for final review and completeness check.

- [X] T183 [P] Review English localization file for completeness and consistency
  - **Completed**: Removed 6 duplicate keys (dark_time_wood and ancient_time_wood blocks)
  - **Completed**: Added 5 missing Patchouli GUI translation keys
  - **Result**: 264 keys, all valid JSON, no duplicates, complete consistency with Japanese version
- [X] T184 [P] Review Japanese localization file for completeness and consistency
  - **Completed**: Removed 6 duplicate keys (dark_time_wood and ancient_time_wood blocks)
  - **Result**: 264 keys, all valid JSON, no duplicates, complete consistency with English version

---

## Documentation (✅ COMPLETED)

**Purpose**: Create comprehensive documentation for players, developers, and mod hosting platforms

- [x] T185 [P] Update README.md with build instructions for both loaders
- [x] T186 [P] Create CurseForge mod page description (docs/curseforge_description.md - includes Chronicle UI, unified with Modrinth)
- [x] T187 [P] Create Modrinth mod page description (docs/modrinth_description.md - includes Chronicle UI, unified with CurseForge)
- [x] T188 [P] Write player guide in docs/player_guide.md
- [x] T189 [P] Write developer guide in docs/developer_guide.md - Updated with Chronicle UI and LGPL-3.0 (2025-12-27)
- [x] T295 [P] Configure mod metadata (license="LGPL-3.0", authors="ksoichiro" in fabric.mod.json and neoforge.mods.toml) - Completed 2025-12-27
- [x] T296 [P] Add mod icon/logo image (512x512 PNG) to resources - Added to Fabric and NeoForge (2025-12-27)
- [x] T297 [P] Add LICENSE file and document dependency licenses (Architectury, custom portal system, Patchouli, etc.)

**Final Status**:
- ✅ All documentation files created and updated
- ✅ Mod metadata configured with LGPL-3.0 license
- ✅ Project icon added to both loaders
- ✅ LICENSE file and dependency documentation complete

---

## OSS Internationalization (English Translation) (✅ COMPLETED 2025-12-27)

**Purpose**: Complete English translation of all documentation for international open-source contributors

**Background**: After LGPL-3.0 license adoption, the project is now fully open source. To maximize international contribution, all internal documentation is now available in English.

**Phase 1: Essential Documentation**:

- [x] T707 [P] Create CONTRIBUTING.md in English - Completed 2025-12-27
  - Wrote contribution guidelines (setup, workflow, code style, PR process)
  - Included link to developer_guide.md for technical details
  - Documented LGPL-3.0 license compliance requirements

- [x] T708 [P] Translate specs/chrono-dawn-mod/spec.md to English - Completed 2025-12-27
  - Translated design specification (156 lines of Japanese content)
  - Essential for new contributors to understand feature design
  - Maintained existing structure and content

- [x] T709 [P] Translate specs/chrono-dawn-mod/tasks.md to English - Completed 2025-12-27
  - Translated task descriptions and notes (49 lines of Japanese content)
  - Important for project progress visibility
  - Kept task IDs and completion status unchanged

**Phase 2: Historical Documentation**:

- [x] T710 [Documentation] Translate docs/implementation_history.md to English (2025-12-27)
  - Translated implementation history (466 lines)
  - Provides context for design decisions (Ancient Ruins placement trials, Additional Bosses, Desert Clock Tower)

- [x] T711 [Documentation] Translate docs/initial_design.md to English (2025-12-27)
  - Translated initial design document (70 lines)
  - Historical value for understanding project evolution (original storyline, dimension mechanics, artifacts concept)

**Final Status**:
- ✅ All essential documentation (spec.md, tasks.md, CONTRIBUTING.md) available in English
- ✅ New international contributors can understand project structure and contribute
- ✅ No Japanese content in critical development files
- ✅ Historical documentation provides context for design decisions

---

## Playtest Improvements - Dimension Mechanics (✅ COMPLETED)

**Purpose**: Improvements to dimension functionality discovered through playtesting

- [x] T301 [P] Fix bed sleeping mechanic in Chrono Dawn (currently sleeping doesn't advance time to morning)
  - **Issue**: Chrono Dawn has day-night cycle but sleeping in bed doesn't skip to morning
  - **Root Cause**: Custom dimensions don't naturally support time skipping when sleeping (Minecraft limitation)
  - **Solution**: Implemented SleepMixin to manually check if all players in Chrono Dawn are sleeping and advance time to morning
  - **Implementation Details**:
    - Created SleepMixin.java that hooks into ServerLevel.tick()
    - Checks if ALL players in Chrono Dawn dimension are sleeping long enough
    - Advances time to morning (1000 ticks) when all players are sleeping
    - Independent from Overworld sleep mechanics (dimension-specific)
    - Respects Chrono Dawn's variable time cycle system
  - **Files Modified**:
    - common/src/main/java/com/chronodawn/mixin/SleepMixin.java (new)
    - fabric/src/main/resources/chronodawn-fabric.mixins.json
    - neoforge/src/main/resources/chronodawn-neoforge.mixins.json
  - **Tested**: Build successful, all game tests passed (92/92)
- [x] T310 [P] Fix portal regeneration in multiplayer when multiple players use same portal
  - **Issue**: When multiple players transition to Chrono Dawn through the same portal, a once-broken portal regenerates and becomes indestructible
  - **Root Cause**: BlockEventHandler only checked Fabric portal block ID (customportalapi:customportalblock), causing NeoForge portal blocks (cpapireforged:custom_portal_block) to persist after deactivation
  - **Result**: On NeoForge, portals regenerated and remained functional after death/re-entry, unlike Fabric where portals were properly removed
  - **Solution**: Added `isCustomPortalBlock()` method to check both loader-specific portal block IDs
    - Fabric: customportalapi:customportalblock
    - NeoForge: cpapireforged:custom_portal_block
  - **Changes**:
    - common/src/main/java/com/chronodawn/events/BlockEventHandler.java
      - Added isCustomPortalBlock() helper method
      - Replaced hardcoded Fabric-only check with loader-agnostic method
  - **Tested**: Multiplayer verified on both Fabric and NeoForge - portal blocks properly removed, no indestructible portals
  - **Commit**: 247749d
- [x] T311 [P] Fix portal generation to spawn on surface instead of underground
  - **Issue**: Portal generated at Y=-48 underground (in a cave), making game progression significantly harder
  - **Investigation**: Check portal placement logic and Y-coordinate calculation
  - **Expected behavior**: Portal should generate on surface (ground level)
  - **Solution implemented**:
    - **Fabric**: Added `setPortalSearchYRange(70, 100)` to restrict portal search range
    - **NeoForge**: custom portal system Reforged v1.2.2 lacks `setPortalSearchYRange` method
    - **Both platforms**: Implemented `PortalPlacerMixin` to modify `topY`/`bottomY` local variables to Y=70-100 range
    - Prevents deep underground spawning (Y=-48) while allowing terrain variation
  - **Test results**:
    - NeoForge (structure present): Y=73 on Forgotten Library roof ✅
    - NeoForge (flat terrain): Ground level ✅
    - Fabric: Surface generation confirmed ✅
  - **Files modified**:
    - `common/src/main/java/com/chronodawn/mixin/PortalPlacerMixin.java` (new)
    - `common/src/main/resources/chronodawn.mixins.json`
    - `fabric/src/main/resources/chronodawn-fabric.mixins.json`
    - `neoforge/src/main/resources/chronodawn-neoforge.mixins.json`
    - `fabric/src/main/java/com/chronodawn/fabric/compat/CustomPortalFabric.java`
    - `neoforge/src/main/java/com/chronodawn/neoforge/compat/CustomPortalNeoForge.java`
  - **Priority**: High (gameplay difficulty issue) - RESOLVED

**Final Status**:
- ✅ Bed sleeping mechanic fixed for Chrono Dawn dimension
- ✅ Portal regeneration bug fixed in multiplayer
- ✅ Portal generation now spawns on surface (Y=70-100)

---

## Playtest Improvements - Boss Battle (✅ COMPLETED)

**Purpose**: Boss battle improvements discovered through playtesting

- [x] T302 [P] Fix Master Clock boss room door unlock requirement
  - **Issue**: Door opens with only Ancient Clockwork x3, should also require Key to Master Clock
  - **Investigation**: Check BossRoomDoorBlock.java unlock condition logic
  - **Fix**: Update unlock requirement to check for both Ancient Clockwork x3 AND Key to Master Clock
  - **Completed**: Modified BlockEventHandler.java:166 to require both hasRequiredAncientGears() AND hasKeyToMasterClock()
  - **File**: common/src/main/java/com/chronodawn/events/BlockEventHandler.java:166
- [x] T303 [P] Prevent non-boss mob spawning in Desert Clock Tower boss room
  - **Issue**: Other mobs spawn in Desert Clock Tower boss room during battle
  - **Solution**: Added light sources to boss room in Desert Clock Tower structure NBT to prevent hostile mob spawning
  - **Implementation**: Modified desert_clock_tower.nbt to include lighting blocks (light level 7+) in boss room
  - **Result**: Hostile mobs no longer spawn in boss room during Time Guardian battle (Minecraft's natural spawning rules prevent spawning at light level 7+)
  - **Files Modified**: common/src/main/resources/data/chronodawn/structure/desert_clock_tower.nbt
  - **Commit**: 304b2ed (fix: add lighting to Desert Clock Tower boss room to prevent non-boss mob spawning)
- [x] T303a [P] Prevent Master Clock structure from being overwritten by other structures
  - **Issue**: Master Clock may be overwritten by other structures (e.g., Ancient Ruins, Desert Clock Tower)
  - **Solution**: Adjusted structure generation step priority
    - Master Clock: `underground_structures` (earlier generation)
    - Ancient Ruins: `surface_structures` (later generation)
    - Desert Clock Tower: `surface_structures` (later generation)
  - **Result**: Master Clock now generates before other structures, preventing overwrites
  - **Files Modified**:
    - common/src/main/resources/data/chronodawn/worldgen/structure_set/master_clock_set.json
    - common/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins_set.json
    - common/src/main/resources/data/chronodawn/worldgen/structure_set/desert_clock_tower_set.json
  - **Commit**: 4bb4268 (fix: prevent Master Clock structure from being overridden)
- [x] T304 [P] Fix Master Clock boss room ceiling height for Time Tyrant
  - **Issue**: Time Tyrant cannot pass through some low-height areas in Master Clock boss room
  - **Solution**: Increased ceiling height in problematic areas to accommodate Time Tyrant (height = 3.5 blocks)
  - **Files Modified**: common/src/main/resources/data/chronodawn/structures/master_clock_boss_room.nbt
  - **Commit**: 035a723 (fix: improve Master Clock boss room layout for better gameplay)
- [x] T305 [P] Prevent player camping in Master Clock boss room
  - **Issue**: Players can hide in safe spots and attack Time Tyrant without risk
  - **Solution**: Removed hiding spots from room layout to ensure boss has proper access to entire arena
  - **Files Modified**: common/src/main/resources/data/chronodawn/structures/master_clock_boss_room.nbt
  - **Commit**: 035a723 (fix: improve Master Clock boss room layout for better gameplay)
- [x] T309 [P] Fix Phantom Catacombs structure search freezing (2026-01-02)
  - **Issue**: When using structure search for Phantom Catacombs, the world freezes, especially in multiplayer where other players get disconnected and boss room placement never completes
  - **Root Cause**: Synchronous marker search scanning up to 34M blocks on main thread
  - **Solution Implemented**: Multi-tick state machine
    - Process 1 chunk per tick to avoid main thread blocking
    - State phases: SEARCHING_MARKERS → EVALUATING_CANDIDATES → PLACING_ROOMS → COMPLETED
    - Thread-safe state management using ConcurrentHashMap
    - Dimension filtering to prevent cross-dimension interference
    - StructurePiece-based chunk collection for accurate coverage
  - **Files Modified**:
    - `common/src/main/java/com/chronodawn/worldgen/spawning/PhantomCatacombsBossRoomPlacer.java`
  - **Testing**: Verified on both Fabric and NeoForge, multiplayer stability confirmed
  - **Commits**:
    - 6e80ceb (feat: implement multi-tick state machine)
    - 1a956b6 (fix: use ConcurrentHashMap for thread safety)
    - d5c81f6 (refactor: reduce excessive logging)
  - **Priority**: Critical (game-breaking bug)
- [x] T714 [P] Exclude all boss entities from Time Distortion Effect (Slowness debuff)
  - **Issue**: Boss mobs (Entropy Keeper, Chronos Warden, Clockwork Colossus, Temporal Phantom) are affected by Time Distortion Effect (Slowness IV/V), making them move too slowly during battles
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Current State**: Only Time Guardian and Time Tyrant are excluded from Time Distortion Effect
  - **Implementation**:
    - Update `TimeDistortionEffect.isHostileMob()` method to exclude all boss entities
    - Add exclusion checks for: ChronosWardenEntity, ClockworkColossusEntity, EntropyKeeperEntity, TemporalPhantomEntity
    - Reference: `common/src/main/java/com/chronodawn/core/time/TimeDistortionEffect.java:111-142` (isHostileMob method)
  - **Files Modified**:
    - `common/src/main/java/com/chronodawn/core/time/TimeDistortionEffect.java`
      - Added imports for all 4 boss entities (ChronosWardenEntity, ClockworkColossusEntity, EntropyKeeperEntity, TemporalPhantomEntity)
      - Updated `isHostileMob()` method to exclude all boss entities
      - Added debug logging for boss exclusion verification
      - Updated class documentation to reflect all boss exclusions
    - `common/src/main/java/com/chronodawn/entities/bosses/ChronosWardenEntity.java`
      - Increased movement speed from 0.15 to 0.20 (standard boss speed, matching Time Guardian and Entropy Keeper)
      - Updated class documentation to reflect new movement speed
  - **Result**: All boss mobs now move at normal speed without Slowness debuff, maintaining proper difficulty balance
  - **Completed**: 2026-01-03
- [x] T715 [P] Add spawn eggs for all boss entities (command-only, not in creative tab)
  - **Requirement**: Boss spawn eggs should be available for testing and debugging via `/give` command, but not displayed in creative inventory tab (following vanilla behavior for Ender Dragon and Wither)
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Implementation**:
    - Create spawn egg items for all boss entities using DeferredSpawnEggItem
    - Add spawn eggs to ModItems.java registry
    - Call initializeSpawnEgg() in registerSpawnEggs() method
    - **Do NOT add to creative tab** (do not call output.accept() in addItemsToCreativeTab())
  - **Boss Entities Added**:
    - Time Guardian (0xFFD700 gold background, 0x4169E1 royal blue spots)
    - Time Tyrant (0x8B0000 dark red background, 0xFFD700 gold spots)
    - Chronos Warden (0x2F4F4F dark slate gray background, 0x00CED1 dark turquoise spots)
    - Clockwork Colossus (0x708090 slate gray background, 0xFF8C00 dark orange spots)
    - Entropy Keeper (0x191970 midnight blue background, 0x9370DB medium purple spots)
    - Temporal Phantom (0x2F2F2F dark gray background, 0xE0E0E0 light gray spots)
  - **Files Modified**:
    - `common/src/main/java/com/chronodawn/registry/ModItems.java`
  - **Expected Behavior**: Boss spawn eggs can be obtained via `/give @s chronodawn:time_guardian_spawn_egg` but do not appear in creative inventory
- [x] T716 [P] Fix duplicate recipe conflict between Enhanced Clockstone Pickaxe and Spatially Linked Pickaxe
  - **Issue**: Enhanced Clockstone Pickaxe and Spatially Linked Pickaxe have identical recipes (Enhanced Clockstone x3 + Stick x2), causing recipe conflict where only one can be crafted
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Solution Implemented**: Changed Spatially Linked Pickaxe recipe to use smithing table upgrade
    - Base: Enhanced Clockstone Pickaxe
    - Template: None (empty)
    - Addition: Time Crystal
    - Result: Spatially Linked Pickaxe
  - **Files Modified**:
    - `common/src/main/resources/data/chronodawn/recipe/spatially_linked_pickaxe.json` (changed from shaped_crafting to smithing_transform)
  - **Expected Behavior**: Both pickaxes can be crafted with distinct recipes, no recipe conflict

**Final Status**:
- ✅ Master Clock boss room door requires both Ancient Clockwork x3 AND Key to Master Clock
- ✅ Desert Clock Tower boss room has lighting to prevent hostile mob spawning
- ✅ Master Clock structure protected from being overwritten
- ✅ Master Clock boss room ceiling height fixed for Time Tyrant
- ✅ Master Clock boss room camping spots removed
- ✅ Phantom Catacombs structure search freezing fixed
- ✅ All boss entities excluded from Time Distortion Effect
- ✅ Boss spawn eggs added for testing (command-only)
- ✅ Recipe conflict between Enhanced Clockstone Pickaxe and Spatially Linked Pickaxe fixed

---

## Playtest Improvements - Guidebook Distribution (✅ COMPLETED)

**Purpose**: Fix guidebook distribution bugs discovered through playtesting

- [x] T712 [P] Fix Chronicle Book not being distributed when entering Chrono Dawn dimension
  - **Issue**: Chronicle Book was not distributed on dimension entry on BOTH Fabric and NeoForge
  - **Root Cause**: giveChronicleBook() call was removed during Patchouli to Chronicle GUI migration (commit 053897a)
  - **Fix**: Restored giveChronicleBook() and hasChronicleBook() methods in PlayerEventHandler.java (common module)
  - **Verified**: Both Fabric and NeoForge now distribute Chronicle Book on dimension entry ✓
  - **Priority**: High (affects player experience on both platforms)

**Final Status**:
- ✅ Chronicle Book now distributed on dimension entry for both Fabric and NeoForge

---

## Final Validation (✅ COMPLETED)

**Purpose**: Comprehensive testing and validation of all mod features before release

- [x] T190 Run full test suite for both loaders using ./gradlew test
- [x] T191 Validate quickstart.md manual testing checklist
- [x] T192 Build final JARs for distribution using ./gradlew build
- [x] T193 Test Fabric JAR in production Minecraft 1.21.1 + Fabric environment
- [x] T194 Test NeoForge JAR in production Minecraft 1.21.1 + NeoForge environment

**Final Status**:
- ✅ All tests passed on both loaders
- ✅ Manual testing checklist validated
- ✅ Final JARs built successfully
- ✅ Production testing completed on Fabric
- ✅ Production testing completed on NeoForge

---

## Mod Rebranding (✅ COMPLETED 2025-12-21)

**Purpose**: Change mod name and dimension name from "Chronosphere" to "Chrono Dawn"

- [x] T308 [Rebranding] Rename mod and dimension from "Chronosphere" to "Chrono Dawn" (2025-12-21)
  - **Completed**:
    - ✅ Mod name: "Chrono Dawn" (fabric.mod.json, neoforge.mods.toml)
    - ✅ Directory: ChronoDawn (renamed from 001-chronosphere-mod)
    - ✅ Mod ID: chronodawn (maintained)
    - ✅ Package name: com.chronodawn (maintained)
    - ✅ Documentation updated (README.md, docs/*, CLAUDE.md)
    - ✅ All resource files and translations updated
  - **Testing**:
    - ✅ Mod loads correctly with new name
    - ✅ Dimension accessible with identifier
    - ✅ Translations verified
  - **Priority**: Medium (branding update)

**Final Status**:
- ✅ Mod successfully rebranded to "Chrono Dawn"
- ✅ All documentation and resource files updated

---

## Performance & Thread Safety Audit (Critical) (✅ COMPLETED 2026-01-02)

**Purpose**: Identify and fix potential performance and thread safety issues across all server-side code

**Background**: Lessons learned from T309 (Phantom Catacombs freezing fix):
1. Main thread blocking causes world freezing and player disconnections in multiplayer
2. Non-thread-safe collections (HashMap, HashSet, ArrayList) cause race conditions in multi-threaded server environment

- [x] T428 [Performance] Audit and fix main thread blocking in structure generation
  - **Issue**: Long-running synchronous operations on main thread cause world freezing
  - **Investigation**:
    - Scanned all structure generation code for large-scale block scanning (e.g., Boss Room Placers)
    - Identified chunk-loading operations that may block main thread
    - Checked for nested loops scanning large areas (>10,000 blocks)
  - **Files checked**:
    - `common/src/main/java/com/chronodawn/worldgen/spawning/PhantomCatacombsBossRoomPlacer.java` ✓ (Fixed in T309)
    - Other Boss Room Placers - No similar issues found
    - Structure generation classes in `common/src/main/java/com/chronodawn/worldgen/structures/` - No issues found
  - **Solution patterns**:
    - Multi-tick state machine (process 1 chunk per tick)
    - Async processing with main thread synchronization
    - Limit search area to reasonable bounds
  - **Result**: No additional main thread blocking issues found
  - **Reference**: T309 implementation (PhantomCatacombsBossRoomPlacer.java multi-tick state machine)

- [x] T429 [Thread Safety] Audit and fix non-thread-safe collection usage
  - **Issue**: HashMap, HashSet, ArrayList are not thread-safe and cause race conditions in multiplayer
  - **Investigation**:
    - Scanned all server-side shared state for non-thread-safe collections
    - Identified collections accessed by multiple dimensions/threads simultaneously
    - Checked static fields and cached data structures
  - **Files audited and fixed**:
    - `common/src/main/java/com/chronodawn/worldgen/spawning/PhantomCatacombsBossRoomPlacer.java` ✓ (Fixed in T309)
    - `common/src/main/java/com/chronodawn/data/ChronoDawnWorldData.java` - Fixed non-thread-safe collections
    - `common/src/main/java/com/chronodawn/data/PortalRegistryData.java` - Fixed non-thread-safe collections
  - **Solution patterns**:
    - Replaced HashMap → ConcurrentHashMap
    - Replaced HashSet → ConcurrentHashMap.newKeySet()
    - Used atomic operations (compute, computeIfAbsent, etc.)
  - **Result**: All server-side shared state now uses thread-safe collections

- [x] T430 [Dimension Isolation] Audit and fix dimension filtering in chunk processing
  - **Issue**: Processing logic may handle entities/structures from all dimensions instead of current dimension only
  - **Investigation**:
    - Scanned all server-level tick handlers that process chunks/structures/entities
    - Identified code that stores state globally without dimension filtering
    - Checked for cross-dimension interference in structure generation and entity processing
  - **Files audited and fixed**:
    - `common/src/main/java/com/chronodawn/worldgen/spawning/PhantomCatacombsBossRoomPlacer.java` ✓ (Fixed in T309)
    - `common/src/main/java/com/chronodawn/worldgen/spawning/TemporalPhantomSpawner.java` - Added dimension filtering
    - `common/src/main/java/com/chronodawn/worldgen/spawning/ClockworkColossusSpawner.java` - Added dimension filtering
  - **Solution patterns**:
    - Stored dimension ID alongside structure/entity state
    - Filtered by `level.dimension().location()` before processing
    - Used dimension-keyed maps where appropriate
  - **Result**: Each dimension now processes only its own structures/entities, no cross-dimension interference

**Final Status**:
- ✅ No main thread blocking issues found beyond T309 fix
- ✅ All server-side shared state uses thread-safe collections
- ✅ Dimension isolation implemented in all tick handlers
- ✅ Multiplayer stability verified on both Fabric and NeoForge
- ✅ No world freezing or race conditions detected

