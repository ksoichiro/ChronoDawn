# Completed Tasks: Chronosphere Mod

**Feature Branch**: `001-chronosphere-mod`

このファイルには完了したタスクが記録されています。

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Architecturyマルチローダープロジェクトの初期化と基本構造の構築

- [X] T001 Create Architectury multi-module project structure per plan.md
- [X] T002 Initialize Gradle configuration in build.gradle with Architectury dependencies
- [X] T003 [P] Configure gradle.properties with Minecraft 1.21.1, NeoForge 21.1.74, Fabric API 0.116.6, Architectury API 13.0.8
- [X] T004 [P] Configure settings.gradle for multi-module project (common, fabric, neoforge)
- [X] T005 Create common module build configuration in common/build.gradle
- [X] T006 [P] Create fabric module build configuration in fabric/build.gradle
- [X] T007 [P] Create neoforge module build configuration in neoforge/build.gradle (with loom.platform=neoforge)
- [X] T008 Create common mod entry point in common/src/main/java/com/chronosphere/Chronosphere.java
- [X] T009 [P] Create Fabric mod entry point in fabric/src/main/java/com/chronosphere/fabric/ChronosphereFabric.java
- [X] T010 [P] Create NeoForge mod entry point in neoforge/src/main/java/com/chronosphere/neoforge/ChronosphereNeoForge.java
- [X] T011 [P] Create Fabric mod metadata in fabric/src/main/resources/fabric.mod.json
- [X] T012 [P] Create NeoForge mod metadata in neoforge/src/main/resources/META-INF/neoforge.mods.toml
- [X] T012a [P] Add Fabric API and Architectury dependencies to fabric.mod.json for automatic installation in launchers like Prism Launcher
- [X] T012b [P] Add Architectury dependency to neoforge.mods.toml for automatic installation in launchers
- [X] T013 Configure logging with log4j2 in common/src/main/resources/log4j2.xml
- [X] T014 Verify build succeeds for both loaders using ./gradlew build

---

### Registry Infrastructure

- [X] T015 Implement Architectury Registry wrapper in common/src/main/java/com/chronosphere/registry/ModBlocks.java
- [X] T016 [P] Implement Architectury Registry wrapper in common/src/main/java/com/chronosphere/registry/ModItems.java
- [X] T017 [P] Implement Architectury Registry wrapper in common/src/main/java/com/chronosphere/registry/ModEntities.java
- [X] T018 [P] Implement Architectury Registry wrapper in common/src/main/java/com/chronosphere/registry/ModDimensions.java

### Platform Abstraction Layer (@ExpectPlatform)

- [X] T019 Create @ExpectPlatform interface in common/src/main/java/com/chronosphere/platform/ChronospherePlatform.java
- [X] T020 [P] Implement ChronospherePlatform for Fabric in fabric/src/main/java/com/chronosphere/fabric/platform/ChronospherePlatformImpl.java
- [X] T021 [P] Implement ChronospherePlatform for NeoForge in neoforge/src/main/java/com/chronosphere/neoforge/platform/ChronospherePlatformImpl.java

### Event System (Architectury Events)

- [X] T022 Create base event handler structure in common/src/main/java/com/chronosphere/events/ChronosphereEvents.java
- [X] T023 [P] Implement entity event handler in common/src/main/java/com/chronosphere/events/EntityEventHandler.java
- [X] T024 [P] Implement block event handler in common/src/main/java/com/chronosphere/events/BlockEventHandler.java
- [X] T025 [P] Implement player event handler in common/src/main/java/com/chronosphere/events/PlayerEventHandler.java

### Data Persistence Framework

- [X] T026 Implement world saved data base class in common/src/main/java/com/chronosphere/data/ChronosphereWorldData.java
- [X] T027 Implement portal registry data handler in common/src/main/java/com/chronosphere/data/PortalRegistryData.java
- [X] T028 Implement player progress data handler in common/src/main/java/com/chronosphere/data/PlayerProgressData.java
- [X] T029 Implement dimension state data handler in common/src/main/java/com/chronosphere/data/DimensionStateData.java

### Testing Infrastructure (JUnit + GameTest)

- [X] T030 Setup mcjunitlib integration in common/build.gradle
- [X] T031 Create base test class in common/src/test/java/com/chronosphere/ChronosphereTestBase.java
- [X] T032 [P] Create unit test structure in common/src/test/java/com/chronosphere/unit/
- [X] T033 [P] Create integration test structure in common/src/test/java/com/chronosphere/integration/
- [X] T034 Configure GameTest framework for both loaders

### Creative Tab Infrastructure

- [X] T034a Create ModCreativeTabs registry in common/src/main/java/com/chronosphere/registry/ModCreativeTabs.java
- [X] T034b Register Chronosphere creative tab with icon
- [X] T034c Implement item group population for both loaders

### Portal Color Customization (Phase 2.5)

**Purpose**: ネザーポータルと差別化するため、ポータルの色をオレンジ (#db8813) に設定

- [X] T034d [P] Research and decide final portal tint color (non-purple theme)
- [X] T034e [P] Update PORTAL_COLOR_* constants in CustomPortalFabric.java to RGB(219, 136, 19)
- [ ] T034f [P] Update PORTAL_COLOR_* constants in CustomPortalNeoForge.java to RGB(219, 136, 19) (depends on T049)
- [X] T034g [P] Add color constant documentation explaining theme choice (#db8813 - orange/gold time theme)
- [X] T034h [P] Test portal visual appearance in-game (portal block, overlay, particles)
- [X] T034i [P] Update spec.md with finalized portal color documentation

### Tests for User Story 1 (TDD approach)

**NOTE: これらのテストを最初に記述し、実装前にFAILすることを確認すること**

- [X] T035 [P] [US1] Write unit test for dimension registration in common/src/test/java/com/chronosphere/unit/DimensionTest.java
- [X] T036 [P] [US1] Write unit test for portal state transitions in common/src/test/java/com/chronosphere/unit/PortalStateTest.java
- [X] T037 [P] [US1] Write GameTest for portal activation in common/src/test/java/com/chronosphere/integration/PortalActivationTest.java
- [X] T038 [P] [US1] Write GameTest for dimension travel in common/src/test/java/com/chronosphere/integration/DimensionTravelTest.java
- [X] T039 [P] [US1] Write GameTest for portal stabilization in common/src/test/java/com/chronosphere/integration/PortalStabilizationTest.java

### Core Dimension System

- [X] T040 [US1] Create custom dimension type definition in common/src/main/resources/data/chronosphere/dimension_type/chronosphere.json
- [X] T041 [US1] Create dimension JSON in common/src/main/resources/data/chronosphere/dimension/chronosphere_dimension.json
- [X] T042 [US1] Implement dimension registry logic in common/src/main/java/com/chronosphere/core/dimension/ChronosphereDimension.java
- [X] T043 [US1] Implement custom biome provider in common/src/main/java/com/chronosphere/core/dimension/ChronosphereBiomeProvider.java
- [X] T044 [US1] Create custom biome definition in common/src/main/resources/data/chronosphere/worldgen/biome/chronosphere_plains.json

### Portal System (Custom Portal API Integration)

- [X] T045 [US1] Create portal frame validation logic in common/src/main/java/com/chronosphere/core/portal/PortalFrameValidator.java
- [X] T046 [US1] Implement portal state machine in common/src/main/java/com/chronosphere/core/portal/PortalStateMachine.java
- [X] T047 [US1] Implement portal registry in common/src/main/java/com/chronosphere/core/portal/PortalRegistry.java
- [X] T048 [US1] Integrate Custom Portal API (Fabric) in fabric/src/main/java/com/chronosphere/fabric/compat/CustomPortalFabric.java
- [ ] T049 [US1] Integrate Custom Portal API (NeoForge) in neoforge/src/main/java/com/chronosphere/neoforge/compat/CustomPortalNeoForge.java
- [X] T049a [US1] Fix Chronosphere-side portal placement to avoid spawning on top of trees (modify portal placement logic to find suitable ground level)

### Blocks - Base Materials (US1)

- [X] T050 [P] [US1] Create Clockstone Ore block in common/src/main/java/com/chronosphere/blocks/ClockstoneOre.java
- [X] T051 [P] [US1] Register Clockstone Ore in ModBlocks registry
- [X] T052 [P] [US1] Create Clockstone Ore texture in common/src/main/resources/assets/chronosphere/textures/block/clockstone_ore.png (from diamond_ore.png)
- [X] T053 [P] [US1] Create Clockstone Ore block model in common/src/main/resources/assets/chronosphere/models/block/clockstone_ore.json

### Items - Base Materials (US1)

- [X] T054 [P] [US1] Create Clockstone item in common/src/main/java/com/chronosphere/items/base/ClockstoneItem.java
- [X] T055 [P] [US1] Register Clockstone item in ModItems registry
- [X] T056 [P] [US1] Create Clockstone texture in common/src/main/resources/assets/chronosphere/textures/item/clockstone.png (from amethyst_shard.png)
- [X] T057 [P] [US1] Create Clockstone item model in common/src/main/resources/assets/chronosphere/models/item/clockstone.json
- [X] T058 [P] [US1] Create Clockstone Ore loot table in common/src/main/resources/data/chronosphere/loot_tables/blocks/clockstone_ore.json

### Items - Portal Items (US1)

- [X] T059 [P] [US1] Create Time Hourglass item in common/src/main/java/com/chronosphere/items/TimeHourglassItem.java
- [X] T060 [P] [US1] Register Time Hourglass in ModItems registry
- [X] T061 [P] [US1] Create Time Hourglass texture in common/src/main/resources/assets/chronosphere/textures/item/time_hourglass.png (from glowstone_dust.png)
- [X] T062 [P] [US1] Create Time Hourglass recipe in common/src/main/resources/data/chronosphere/recipes/time_hourglass.json
- [X] T062a [US1] Update Time Hourglass to be consumable (shrink stack on portal ignition in BlockEventHandler.java)
- [X] T063 [P] [US1] Create Portal Stabilizer item in common/src/main/java/com/chronosphere/items/PortalStabilizerItem.java (dimension stabilization only, no portal ignition; includes particle+sound effects and server-wide message broadcast)
- [X] T064 [P] [US1] Register Portal Stabilizer in ModItems registry
- [X] T065 [P] [US1] Create Portal Stabilizer texture in common/src/main/resources/assets/chronosphere/textures/item/portal_stabilizer.png (from nether_star.png)
- [X] T066 [P] [US1] Create Portal Stabilizer recipe in common/src/main/resources/data/chronosphere/recipes/portal_stabilizer.json

### World Generation - Structures (US1)

- [X] T067 [P] [US1] Create Ancient Ruins structure NBT in common/src/main/resources/data/chronosphere/structures/ancient_ruins.nbt (structure created, loot chest blueprint pending)
- [X] T068 [P] [US1] Implement Ancient Ruins structure framework (template_pool, structure, structure_set JSONs created)
- [X] T069 [P] [US1] Create Ancient Ruins structure set in common/src/main/resources/data/chronosphere/worldgen/structure_set/ancient_ruins.json
- [X] T070 [P] [US1] Create Forgotten Library structure NBT in common/src/main/resources/data/chronosphere/structure/forgotten_library.nbt (35x10x35 library building with interior completed)
- [X] T071 [P] [US1] Implement Forgotten Library structure framework (template_pool, structure, structure_set JSONs created)
- [X] T072 [P] [US1] Create Forgotten Library structure set in common/src/main/resources/data/chronosphere/worldgen/structure_set/forgotten_library.json

### Time Distortion Effect (US1)

- [X] T073 [US1] Implement time distortion effect logic in common/src/main/java/com/chronosphere/core/time/TimeDistortionEffect.java
- [X] T074 [US1] Add entity tick event handler for Slowness IV application in EntityEventHandler.java
- [X] T075 [US1] Write unit test for time distortion effect in common/src/test/java/com/chronosphere/unit/TimeDistortionTest.java

### Consumables - Fruit of Time (US1)

- [X] T076 [P] [US1] Create Fruit of Time item in common/src/main/java/com/chronosphere/items/consumables/FruitOfTimeItem.java
- [X] T077 [P] [US1] Register Fruit of Time in ModItems registry
- [X] T078 [P] [US1] Create Fruit of Time texture and model in common/src/main/resources/assets/chronosphere/ (from glow_berries.png)
- [X] T079 [P] [US1] Create Fruit of Time block feature in common/src/main/java/com/chronosphere/worldgen/features/FruitOfTimeTreeFeature.java
- [X] T080 [P] [US1] Configure Fruit of Time tree placement in common/src/main/resources/data/chronosphere/worldgen/placed_feature/fruit_of_time_tree.json

### Enhanced Tree Blocks & Fruit System (US1 - Fruit of Time Enhancement)

**Purpose**: Replace temporary Oak blocks with custom Time Wood blocks and implement fruit-bearing system

**Note**: T079-T080 implemented basic tree generation with Oak blocks. This section enhances it with custom blocks and fruit mechanics.

#### Custom Wood Blocks

- [X] T080a [P] [US1] Create Time Wood Log block in common/src/main/java/com/chronosphere/blocks/TimeWoodLog.java
- [X] T080b [P] [US1] Register Time Wood Log in ModBlocks registry
- [X] T080c [P] [US1] Create Time Wood Log textures in common/src/main/resources/assets/chronosphere/textures/block/ (time_wood_log.png, time_wood_log_top.png)
- [X] T080d [P] [US1] Create Time Wood Log blockstate and models (block, item) in common/src/main/resources/assets/chronosphere/
- [X] T080e [P] [US1] Create Time Wood Log loot table in common/src/main/resources/data/chronosphere/loot_table/blocks/time_wood_log.json
- [X] T080f [P] [US1] Create Time Wood Leaves block in common/src/main/java/com/chronosphere/blocks/TimeWoodLeaves.java
- [X] T080g [P] [US1] Register Time Wood Leaves in ModBlocks registry
- [X] T080h [P] [US1] Create Time Wood Leaves texture in common/src/main/resources/assets/chronosphere/textures/block/time_wood_leaves.png
- [X] T080i [P] [US1] Create Time Wood Leaves blockstate and models (block, item)
- [X] T080j [P] [US1] Create Time Wood Leaves loot table in common/src/main/resources/data/chronosphere/loot_table/blocks/time_wood_leaves.json

#### Additional Wood Components (Implemented)

- [X] Time Wood Planks block (common/src/main/java/com/chronosphere/blocks/TimeWoodPlanks.java)
- [X] Time Wood Sapling block (common/src/main/java/com/chronosphere/blocks/TimeWoodSapling.java)
- [X] Crafting recipe for planks (1 log → 4 planks)
- [X] Sapling drop from leaves (5% base chance, Fortune affected)
- [X] Block tags (logs, leaves, mineable/axe for log/planks, mineable/hoe for leaves)
- [X] Render layers for transparency (cutout for sapling, cutoutMipped for leaves)
- [X] Leaves decay logic (distance tracking, persistent state)

#### Fruit Block System

- [X] T080k [P] [US1] Create Fruit of Time block in common/src/main/java/com/chronosphere/blocks/FruitOfTimeBlock.java (with growth stages 0-2, similar to Cocoa)
- [X] T080l [P] [US1] Register Fruit of Time block in ModBlocks registry
- [X] T080m [P] [US1] Create Fruit of Time block textures for each growth stage in common/src/main/resources/assets/chronosphere/textures/block/ (fruit_of_time_stage_0.png, stage_1.png, stage_2.png)
- [X] T080n [P] [US1] Create Fruit of Time block blockstate in common/src/main/resources/assets/chronosphere/blockstates/fruit_of_time.json
- [X] T080o [P] [US1] Create Fruit of Time block loot table in common/src/main/resources/data/chronosphere/loot_tables/blocks/fruit_of_time.json (drops 1-3 items when mature)
- [X] T080p [US1] Implement fruit growth logic with random tick in FruitOfTimeBlock.java

#### Tree Decorator & Generation Update

- [X] T080q [US1] Implement FruitDecorator class in common/src/main/java/com/chronosphere/worldgen/decorators/FruitDecorator.java
- [X] T080r [US1] Update FruitOfTimeTreeFeature.java to use Time Wood Log/Leaves instead of Oak blocks
- [X] T080s [US1] Update configured_feature/fruit_of_time_tree.json with custom blocks and fruit decorator in decorators array
- [X] T080t [P] [US1] Update localization files (en_us.json, ja_jp.json) with Time Wood blocks; add leaves to mineable/hoe tag (not axe)
- [X] T080u [US1] Test custom tree generation and fruit growth in-game (verify custom textures, fruit placement, growth, harvesting) (tree generation and leaves decay tested)

### Time Wood Functional Blocks (US1 Enhancement)

**Purpose**: Time Wood木材の基礎的な機能ブロックを追加し、バニラ木材と同等の使い勝手を提供

**Note**: Door, Trapdoor, Fence Gate, Button, Pressure Plate実装済み。Sign/Boatは複雑なため後回し。

**Implementation Note**: 作業台はTime Wood専用ブロックを作らず、Time Wood Planksを`#minecraft:planks`タグに追加することでバニラ作業台をクラフト可能にした。

- [X] T080v [P] [US1] ~~Create Time Wood Crafting Table block~~ → CHANGED: Add Time Wood Planks to #minecraft:planks tag (data/minecraft/tags/item/planks.json, data/minecraft/tags/block/planks.json) to allow crafting vanilla crafting table
- [X] T080w [P] [US1] Create Time Wood Door block in common/src/main/java/com/chronosphere/blocks/TimeWoodDoor.java
- [X] T080x [P] [US1] Create Time Wood Trapdoor block in common/src/main/java/com/chronosphere/blocks/TimeWoodTrapdoor.java
- [X] T080y [P] [US1] Create Time Wood Fence Gate block in common/src/main/java/com/chronosphere/blocks/TimeWoodFenceGate.java
- [X] T080z [P] [US1] Create Time Wood Button block in common/src/main/java/com/chronosphere/blocks/TimeWoodButton.java
- [X] T080aa [P] [US1] Create Time Wood Pressure Plate block in common/src/main/java/com/chronosphere/blocks/TimeWoodPressurePlate.java
- [ ] T080ab [P] [US1] Create Time Wood Sign blocks in common/src/main/java/com/chronosphere/blocks/ (TimeWoodStandingSign.java, TimeWoodWallSign.java, TimeWoodHangingSign.java, TimeWoodWallHangingSign.java) - DEFERRED: Complex implementation requiring BlockEntity and custom renderer
- [ ] T080ac [P] [US1] Create Time Wood Boat entities in common/src/main/java/com/chronosphere/entities/vehicle/ (TimeWoodBoat.java, TimeWoodChestBoat.java) - DEFERRED: Complex implementation requiring custom entity and physics
- [X] T080ad [P] [US1] Register all Time Wood functional blocks in ModBlocks registry (Door, Trapdoor, Fence Gate, Button, Pressure Plate)
- [ ] T080ae [P] [US1] Register Time Wood Boat entities in ModEntities registry - DEFERRED: Depends on T080ac
- [X] T080af [P] [US1] Create crafting recipes for all Time Wood functional blocks in common/src/main/resources/data/chronosphere/recipe/ (door, trapdoor, fence_gate, button, pressure_plate)
- [X] T080ag [P] [US1] Create blockstates/models for all Time Wood functional blocks in common/src/main/resources/assets/chronosphere/blockstates/ and models/ (Door, Trapdoor, Fence Gate, Button, Pressure Plate)
- [X] T080ah [P] [US1] Create textures for all Time Wood functional blocks in common/src/main/resources/assets/chronosphere/textures/block/ and item/ (converted from Jungle textures using RGB channel adjustments: R*0.95, G*1.17, B*0.85)
- [ ] T080ai [P] [US1] Create Sign item in common/src/main/java/com/chronosphere/items/TimeWoodSignItem.java and HangingSignItem for inventory representation - DEFERRED: Depends on T080ab
- [ ] T080aj [P] [US1] Create Boat items in common/src/main/java/com/chronosphere/items/TimeWoodBoatItem.java and TimeWoodChestBoatItem.java - DEFERRED: Depends on T080ac
- [X] T080ak [P] [US1] Update localization files (en_us.json, ja_jp.json) with all Time Wood functional blocks (Door, Trapdoor, Fence Gate, Button, Pressure Plate)
- [X] T080al [US1] Add all Time Wood functional blocks to creative tab in ModCreativeTabs (Door, Trapdoor, Fence Gate, Button, Pressure Plate)
- [X] T080am [US1] Test all Time Wood functional blocks in-game (verified: crafting vanilla crafting table with Time Wood Planks, Door/Trapdoor transparency, all blocks placement and interaction) - PARTIAL: Sign/Boat not tested (not implemented)

### Special Blocks (US1)

- [X] T081 [P] [US1] Create Reversing Time Sandstone block in common/src/main/java/com/chronosphere/blocks/ReversingTimeSandstone.java
- [X] T082 [P] [US1] Register Reversing Time Sandstone in ModBlocks registry
- [X] T083 [P] [US1] Implement block restoration logic in BlockEventHandler.java
- [X] T084 [P] [US1] Create Unstable Fungus block in common/src/main/java/com/chronosphere/blocks/UnstableFungus.java
- [X] T085 [P] [US1] Register Unstable Fungus in ModBlocks registry
- [X] T086 [P] [US1] Implement collision event handler in EntityEventHandler.java for random speed effects

### Respawn Logic (US1)

- [X] T087 [US1] ~~Implement respawn handler in PlayerEventHandler.java~~ - REVERTED: Uses Minecraft standard behavior (like End dimension)
- [X] T088 [US1] ~~Write GameTest for respawn behavior in common/src/test/java/com/chronosphere/integration/RespawnTest.java~~ - REMOVED: No custom respawn logic needed

**Design Decision**: Chronosphere respawn follows Minecraft's standard behavior (similar to End dimension):
- Players respawn at their set bed/respawn anchor, or world spawn if none set
- Portal Stabilizer does NOT affect respawn location (only stabilizes dimension)
- Players can escape Chronosphere by breaking bed and dying (same as End)
- This maintains tension (one-way portal initially) without excessive difficulty

**Portal Deactivation Logic** (PlayerEventHandler.java):
- When player enters Chronosphere, check global state (ChronosphereGlobalState.arePortalsUnstable())
- If portals are unstable (hasEnteredChronosphere && !isPortalStabilized), deactivate portal
- If portals are stable (isPortalStabilized), skip deactivation (portal remains active)
- This allows free bidirectional travel after Portal Stabilizer is used once

### Localization & Creative Tab (US1)

- [X] T088a [US1] Create English localization file with all US1 items/blocks in common/src/main/resources/assets/chronosphere/lang/en_us.json (includes portal_stabilizer.success_reignite_required message)
- [X] T088b [US1] Create Japanese localization file with all US1 items/blocks in common/src/main/resources/assets/chronosphere/lang/ja_jp.json (includes portal_stabilizer.success_reignite_required message)
- [X] T088c [US1] Add all US1 items/blocks to creative tab in ModCreativeTabs

### Multiple Biomes (US1 Enhancement)

**Purpose**: クロノスフィアに複数のバイオームを追加し、探索の多様性と視認性を向上

**Current Issue**: dimension/chronosphere.json が `biome_source: { type: "minecraft:fixed" }` を使用しているため、ディメンション全体が単一バイオーム（plains）として生成され、海もplainsバイオームになっている

- [X] T088d [US1] Research multi_noise biome generation parameters (temperature, humidity, continentalness, erosion, depth, weirdness)
- [X] T088e [P] [US1] Create chronosphere_ocean biome JSON with appropriate water_color for visibility and empty tree features
- [X] T088f [P] [US1] Create chronosphere_forest biome JSON with increased Time Wood tree density
- [X] T088g [US1] Create multi_noise biome source configuration in common/src/main/resources/data/chronosphere/worldgen/multi_noise_biome_source_parameter_list/chronosphere.json
- [X] T088h [US1] Update dimension/chronosphere.json to use multi_noise biome source instead of fixed
- [X] T088i [US1] Test biome generation in-game and verify ocean/plains/forest distribution
- [X] T088iu [P] [US1] Create chronosphere_desert biome JSON with appropriate temperature/downfall and sand terrain (for Desert Clock Tower structure in US2)
- [X] T088iv [US1] Update multi_noise biome source configuration to include desert biome with appropriate noise parameters (high temperature, low humidity)

### Basic Mob Spawning (US1 Enhancement)

**Purpose**: クロノスフィアに基本的な敵対モブ・友好モブを配置してディメンションを活性化

**Note**: カスタムモブは後のフェーズで実装。まずはバニラモブで世界に生命を与える

- [X] T088j [US1] Research appropriate vanilla mob selection and spawn rates for time-distorted dimension
- [X] T088k [P] [US1] Add monster spawners to chronosphere_plains.json (Zombie, Skeleton with low spawn rates)
- [X] T088l [P] [US1] Add creature/ambient spawners to chronosphere_plains.json (passive mobs for ambiance)
- [X] T088m [P] [US1] Add monster spawners to chronosphere_forest.json (forest-appropriate hostile mobs)
- [X] T088n [P] [US1] Add water_creature/water_ambient spawners to chronosphere_ocean.json (fish, squid, etc.)
- [X] T088o [US1] Test mob spawning in-game and verify Time Distortion Effect (Slowness IV) applies correctly to hostile mobs

### Biome Color Adjustment (US1 Enhancement)

**Purpose**: 海と陸地の視認性を向上させるため色設定を調整

**Current Issue**: water_color (4159204 = 0x3F76C4 blue) と foliage_color (7909594 = 0x78A85A green) が近い色調で海の視認性が低い

- [X] T088p [US1] Research optimal color values for ocean visibility (water vs foliage/grass contrast)
- [X] T088q [P] [US1] Adjust water_color in chronosphere_ocean.json for better contrast and visibility
- [X] T088r [P] [US1] Consider adjusting foliage_color/grass_color in plains/forest for visual distinction
- [X] T088s [US1] Test color adjustments in-game and iterate until visibility is acceptable

### Vegetation System (US1 Enhancement)

**Purpose**: 植生を追加して探索に生命感と視覚的多様性を提供

**Note**: 基本的な生命感を与えるため、MVP完了前の実装を推奨。モブスポーン（T088j-o）と並行実装可能

- [X] T088w [US1] Research vanilla vegetation generation patterns (tall grass, flowers, ferns, seagrass, kelp)
- [X] T088x [P] [US1] Create tall grass configured_feature for chronosphere_plains.json (random patches)
- [X] T088y [P] [US1] Create flower configured_feature for chronosphere_plains.json (custom color theme: orange/gold/grey)
- [X] T088z [P] [US1] Create fern/mushroom configured_feature for chronosphere_forest.json (forest undergrowth)
- [X] T088aa [P] [US1] Create seagrass configured_feature for chronosphere_ocean.json (ocean floor vegetation)
- [X] T088ab [P] [US1] Create kelp configured_feature for chronosphere_ocean.json (vertical underwater vegetation)
- [X] T088ac [US1] Test vegetation generation in-game and adjust density/distribution

### Basic Equipment Set (US1 Enhancement - High Priority)

**Purpose**: 基本的な装備セットを追加し、収集とクラフトの楽しみを提供

**Note**: 現在はClockstone Oreしかなく、装備も限定的。基本装備セット（武器・防具・ツール）を追加してゲームプレイの深みを増す

- [X] T210 [P] [US1] Create Time Crystal Ore block in common/src/main/java/com/chronosphere/blocks/TimeCrystalOre.java (new ore type, rarer than Clockstone)
- [X] T211 [P] [US1] Create Time Crystal item in common/src/main/java/com/chronosphere/items/base/TimeCrystalItem.java
- [X] T212 [P] [US1] Configure Time Crystal Ore worldgen in common/src/main/resources/data/chronosphere/worldgen/placed_feature/time_crystal_ore.json (Y: 0-48, vein size 3-5)
- [X] T213 [P] [US1] Create Clockstone Sword item in common/src/main/java/com/chronosphere/items/equipment/ClockstoneSwordItem.java (basic tier weapon, slightly better than iron)
- [X] T214 [P] [US1] Create Clockstone Axe/Shovel/Hoe items in common/src/main/java/com/chronosphere/items/equipment/ (basic tier tools)
- [X] T215 [P] [US1] Create Clockstone Armor Set (Helmet, Chestplate, Leggings, Boots) in common/src/main/java/com/chronosphere/items/equipment/
- [X] T216 [P] [US1] Create crafting recipes for Clockstone equipment in common/src/main/resources/data/chronosphere/recipes/ (uses Clockstone + Time Crystal for enhanced durability)
- [X] T217 [P] [US1] Create textures for Clockstone equipment in common/src/main/resources/assets/chronosphere/textures/item/
- [X] T218 [US1] Add equipment to creative tab and localization files (en_us.json, ja_jp.json)

### Food System Expansion (US1 Enhancement - High Priority)

**Purpose**: 食料システムを拡張し、クラフトと収集の楽しみを追加

**Note**: 現在はFruit of Timeのみで、クラフトできない。加工食料と独自作物を追加して食料システムに深みを持たせる

- [X] T220 [P] [US1] Create Time Fruit Pie item in common/src/main/java/com/chronosphere/items/consumables/TimeFruitPieItem.java (crafted from 3x Fruit of Time + wheat, restores 8 hunger + 30s Haste II)
- [X] T221 [P] [US1] Create Time Jam item in common/src/main/java/com/chronosphere/items/consumables/TimeJamItem.java (crafted from 4x Fruit of Time + sugar, restores 4 hunger + 60s Speed I)
- [X] T222 [P] [US1] Create Time Wheat crop block in common/src/main/java/com/chronosphere/blocks/TimeWheatBlock.java (grows in Chronosphere, 8 growth stages like vanilla wheat)
- [X] T223 [P] [US1] Create Time Wheat Seeds and Time Wheat items in common/src/main/java/com/chronosphere/items/consumables/
- [X] T224 [P] [US1] Create Time Bread item in common/src/main/java/com/chronosphere/items/consumables/TimeBreadItem.java (crafted from 3x Time Wheat, restores 5 hunger)
- [X] T225 [P] [US1] Configure Time Wheat worldgen in plains/forest biomes (random patches like vanilla wheat in villages)
- [X] T226 [P] [US1] Create crafting recipes for food items in common/src/main/resources/data/chronosphere/recipes/
- [X] T227 [P] [US1] Create textures for food items and crop stages in common/src/main/resources/assets/chronosphere/textures/
- [X] T228 [US1] Add food items to creative tab and localization files

### Additional Biomes (US1 Enhancement - Medium Priority)

**Purpose**: バイオームの種類を増やし、探索の多様性と飽きにくさを向上

**Note**: 現在は4種類（plains, ocean, forest, desert）のみ。山岳・湿地・雪原・洞窟を追加してバリエーションを増やす

- [X] T230 [P] [US1] Create chronosphere_mountain biome JSON in common/src/main/resources/data/chronosphere/worldgen/biome/chronosphere_mountain.json (stone terrain, high elevation, sparse vegetation)
- [X] T231 [P] [US1] Create chronosphere_swamp biome JSON in common/src/main/resources/data/chronosphere/worldgen/biome/chronosphere_swamp.json (water, clay, Temporal Moss, unique vegetation)
- [X] T232 [P] [US1] Create chronosphere_snowy biome JSON in common/src/main/resources/data/chronosphere/worldgen/biome/chronosphere_snowy.json (snow, ice, Frozen Time Ice, frozen time theme)
- [X] T233 [P] [US1] ~~Create chronosphere_cave biome JSON~~ - SKIPPED: Vanilla cave generation is sufficient; underground decoration can be added via existing biomes' `underground_decoration` feature lists. US3's Time Crystal Caverns (T263) will provide special cave features.
- [X] T234 [US1] Update multi_noise parameters in multi_noise_biome_source_parameter_list/chronosphere.json to include new biomes with distinct noise values (mountain: high erosion, swamp: low continentalness, snowy: low temperature, cave: depth offset)
- [X] T235 [US1] Test new biomes in-game and verify distribution balance (ensure each biome is discoverable without excessive travel)

### Block Variety Expansion (US1 Enhancement - Medium Priority)

**Purpose**: ブロックの種類を増やし、建築と装飾の選択肢を拡充

**Note**: 現在はカスタムブロックが少なく、バイオームの見た目が単調。装飾ブロック・建築用バリエーション・バイオーム固有ブロックを追加

- [x] T240 [P] [US1] Create Clockwork Block in common/src/main/java/com/chronosphere/blocks/ClockworkBlock.java (decorative block, animated texture with rotating gears)
- [x] T241 [P] [US1] Create Time Crystal Block in common/src/main/java/com/chronosphere/blocks/TimeCrystalBlock.java (decorative, emits light level 10, crafted from 9 Time Crystals)
- [x] T242 [P] [US1] Create Temporal Bricks block in common/src/main/java/com/chronosphere/blocks/TemporalBricksBlock.java (building block, crafted from 4 Clockstone)
- [x] T243 [P] [US1] Create stairs/slabs/walls/fences variants for Time Wood Planks and Temporal Bricks in common/src/main/java/com/chronosphere/blocks/
- [x] T244 [P] [US1] Create Temporal Moss block in common/src/main/java/com/chronosphere/blocks/TemporalMossBlock.java (decorative, swamp biome exclusive, spreads like vanilla moss)
- [x] T245 [P] [US1] Create Frozen Time Ice block in common/src/main/java/com/chronosphere/blocks/FrozenTimeIceBlock.java (snowy biome exclusive, doesn't melt, slippery like ice)
- [x] T246 [P] [US1] Register new blocks in ModBlocks and create blockstates/models/textures in common/src/main/resources/
- [x] T247 [P] [US1] Create crafting recipes for decorative blocks (Clockwork Block, Time Crystal Block, Temporal Bricks, stairs/slabs/walls/fences)
- [ ] T248 [P] [US1] Configure Temporal Moss and Frozen Time Ice worldgen in respective biomes (Deferred: requires T230-235 swamp/snowy biomes)
- [x] T249 [US1] Add new blocks to creative tab and localization files

**Checkpoint**: User Story 1が完全に機能し、独立してテスト可能であること

---

### World Generation - Desert Clock Tower (US2)

- [X] T093 [P] [US2] Create Desert Clock Tower structure NBT in common/src/main/resources/data/chronosphere/structures/desert_clock_tower.nbt (JSON complete, NBT placeholder - needs proper tower structure in-game)
- [X] T094 [P] [US2] Implement Desert Clock Tower structure feature in common/src/main/java/com/chronosphere/worldgen/structures/DesertClockTowerStructure.java (JSON-based implementation complete)
- [X] T095 [P] [US2] Create Desert Clock Tower structure set in common/src/main/resources/data/chronosphere/worldgen/structure_set/desert_clock_tower.json
- [X] T095a [US2] Create actual Desert Clock Tower NBT structure using structure blocks in-game (21x50x21 sandstone tower with chest and Time Guardian spawn point on top floor)
- [X] T095b [US2] Update Desert Clock Tower structure to spawn only in chronosphere:chronosphere_desert biome (modify structure JSON biomes field)
- [X] T095c [US2] Fix monster spawner in Desert Clock Tower - ensure spawners are configured correctly to spawn monsters (RESOLVED: Spawners removed due to light level constraints and design trade-offs; natural mob spawning used instead)

### Items - Enhanced Materials (US2)

- [X] T096 [P] [US2] Create Enhanced Clockstone item in common/src/main/java/com/chronosphere/items/base/EnhancedClockstoneItem.java
- [X] T097 [P] [US2] Register Enhanced Clockstone in ModItems registry
- [X] T098 [P] [US2] Create Enhanced Clockstone texture in common/src/main/resources/assets/chronosphere/textures/item/enhanced_clockstone.png
- [X] T099 [P] [US2] Configure Enhanced Clockstone loot in Desert Clock Tower chest

### Items - Time Manipulation Tools (US2)

- [X] T100 [P] [US2] Create Time Clock item in common/src/main/java/com/chronosphere/items/tools/TimeClockItem.java
- [X] T101 [P] [US2] Register Time Clock in ModItems registry
- [X] T102 [P] [US2] Create Time Clock texture in common/src/main/resources/assets/chronosphere/textures/item/time_clock.png
- [X] T103 [P] [US2] Create Time Clock recipe in common/src/main/resources/data/chronosphere/recipes/time_clock.json
- [X] T104 [P] [US2] Implement Time Clock AI cancellation logic in common/src/main/java/com/chronosphere/core/time/MobAICanceller.java
- [X] T105 [P] [US2] Create Spatially Linked Pickaxe in common/src/main/java/com/chronosphere/items/tools/SpatiallyLinkedPickaxeItem.java
- [X] T106 [P] [US2] Register Spatially Linked Pickaxe in ModItems registry
- [X] T107 [P] [US2] Create Spatially Linked Pickaxe texture in common/src/main/resources/assets/chronosphere/textures/item/spatially_linked_pickaxe.png
- [X] T108 [P] [US2] Create Spatially Linked Pickaxe recipe in common/src/main/resources/data/chronosphere/recipes/spatially_linked_pickaxe.json
- [X] T109 [P] [US2] Implement drop doubling logic in BlockEventHandler.java

### Entities - Time Guardian (Mini-Boss) (US2)

- [X] T110 [US2] Create Time Guardian entity in common/src/main/java/com/chronosphere/entities/bosses/TimeGuardianEntity.java
- [X] T111 [US2] Register Time Guardian in ModEntities registry
- [X] T112 [US2] Implement Time Guardian AI (Phase 1: melee, Phase 2: teleport + AoE) in common/src/main/java/com/chronosphere/entities/bosses/TimeGuardianAI.java
- [X] T113 [US2] Create Time Guardian loot table in common/src/main/resources/data/chronosphere/loot_tables/entities/time_guardian.json
- [X] T114 [US2] Create Time Guardian spawn logic in common/src/main/java/com/chronosphere/worldgen/spawning/TimeGuardianSpawner.java (spawns on Desert Clock Tower top floor)
- [X] T115 [US2] Implement reversed resonance trigger on defeat in EntityEventHandler.java (Note: 一度実装したが、ゲームバランスの観点から廃止。reversed resonance機能自体は他のトリガー（Unstable Hourglassクラフト、Time Tyrant撃破）で使用予定のため保持)

#### Time Guardian Enhancements (Post-MVP)

**Purpose**: Time Guardianの見た目・バランス・ゲームプレイ体験を改善

**Note**: 基本機能は実装済み。以下はポリッシュと体験向上のための追加タスク

- [X] T115a [US2] Create custom model and texture for Time Guardian (currently using zombie placeholder)
  - [X] T115a-1 [US2] Improve Time Guardian texture (currently using placeholder image with minimal modifications)
  - [X] T115a-2 [US2] Improve Time Guardian model geometry (more imposing design, arm shape that conveys extended reach)
- [X] T115b [US2] Adjust Time Guardian parameters (HP, attack damage, armor) based on playtesting feedback
- [X] T115c [US2] Implement boss floor access mechanism (stairs, elevator, or teleporter to top floor)
- [X] T115d [US2] Add boss arena entrance lockdown (prevent escape/re-entry during combat)
- [ ] T115e [US2] Add reversed resonance explanation (book item, advancement description, or in-game message) (Note: Time Guardian撃破トリガーは廃止。Unstable Hourglassクラフト、Time Tyrant撃破用の説明が必要)
- [X] T115m [US2] Fix boss bar display timing (should appear when player reaches top floor, not before)

### Items - Key Items (US2)

- [X] T116 [P] [US2] Create Key to Master Clock item in common/src/main/java/com/chronosphere/items/KeyToMasterClockItem.java
- [X] T117 [P] [US2] Register Key to Master Clock in ModItems registry
- [X] T118 [P] [US2] Create Key to Master Clock texture in common/src/main/resources/assets/chronosphere/textures/item/key_to_master_clock.png

### Items - Unstable Hourglass (US2)

- [X] T119 [P] [US2] Create Unstable Hourglass item in common/src/main/java/com/chronosphere/items/UnstableHourglassItem.java
- [X] T120 [P] [US2] Register Unstable Hourglass in ModItems registry
- [X] T121 [P] [US2] Create Unstable Hourglass recipe in common/src/main/resources/data/chronosphere/recipes/unstable_hourglass.json
- [X] T122 [P] [US2] Implement reversed resonance trigger on crafting in common/src/main/java/com/chronosphere/events/CraftEventHandler.java

### Localization & Creative Tab (US2)

- [X] T122a [US2] Update English localization file with all US2 items/entities in en_us.json
- [X] T122b [US2] Update Japanese localization file with all US2 items/entities in ja_jp.json
- [X] T122c [US2] Add all US2 items to creative tab in ModCreativeTabs

### Recipe Material Review (US2)

**Purpose**: Ensure all US2 recipes use only Chronosphere-obtainable materials (no Overworld-exclusive items like diamonds, ender pearls, nether stars)

- [X] T122d [P] [US2] Review all US2 recipes (Time Clock, Spatially Linked Pickaxe, Unstable Hourglass) for Overworld-exclusive materials
- [X] T122e [P] [US2] Update recipes if needed to use Chronosphere materials - Added gold ore, redstone ore, and sand generation to Chronosphere biomes instead

### Equipment Tier System (US2 Enhancement - High Priority)

**Purpose**: 装備にティア制を導入し、プログレッション感と戦略性を向上

**Note**: 現在は基本装備（Tier 1）と究極装備（Tier 3）のみ。Tier 2（Enhanced Clockstone装備）を追加して段階的な成長を実現

- [x] T250 [P] [US2] Create Enhanced Clockstone Sword item in common/src/main/java/com/chronosphere/items/equipment/EnhancedClockstoneSwordItem.java (Tier 2 weapon, better than Tier 1, chance to freeze enemy on hit for 2s)
- [x] T251 [P] [US2] Create Enhanced Clockstone Axe/Shovel/Hoe items in common/src/main/java/com/chronosphere/items/equipment/ (Tier 2 tools, faster mining speed)
- [x] T252 [P] [US2] Create Enhanced Clockstone Armor Set (Helmet, Chestplate, Leggings, Boots) in common/src/main/java/com/chronosphere/items/equipment/ (Tier 2 armor, higher protection + set bonus: immunity to time distortion effects)
- [x] T253 [P] [US2] Create crafting recipes for Tier 2 equipment in common/src/main/resources/data/chronosphere/recipes/ (uses Enhanced Clockstone + Time Crystal)
- [x] T254 [P] [US2] Implement time-manipulation effects for Tier 2 equipment (freeze on hit for sword, set bonus for armor)
- [x] T255 [P] [US2] Create textures for Tier 2 equipment in common/src/main/resources/assets/chronosphere/textures/item/
- [x] T256 [US2] Add Tier 2 equipment to creative tab and localization files

**Checkpoint**: User Story 1とUser Story 2が両方とも独立して動作すること

---

### World Generation - Master Clock (US3)

**Design**: Underground palace structure with Jigsaw random room generation (see master-clock-design.md)
**Location**: Near world spawn using concentric_rings placement (80-100 chunk radius, 1 per dimension)
**Size**: Entrance 15x10x15, Dungeon 41x50x41, Boss Room 35x20x35

#### NBT Structures - Entrance & Boss Room

- [x] T128 [P] [US3] Create Master Clock entrance NBT (15x10x15) in common/src/main/resources/data/chronosphere/structure/master_clock_entrance.nbt (small surface temple, key-locked door, stairs to underground)
- [x] T129 [P] [US3] Create Master Clock boss room NBT (35x20x35) in common/src/main/resources/data/chronosphere/structure/master_clock_boss_room.nbt (large hall, Time Tyrant spawn point, pillars, reward chest)

#### NBT Structures - Jigsaw Rooms (8 variants)

- [ ] T130a [P] [US3] Create room_trap_arrows NBT (15x8x15) in common/src/main/resources/data/chronosphere/structure/master_clock_room_trap_arrows.nbt (pressure plates, dispensers, Reversing Time Sandstone floor)
- [ ] T130b [P] [US3] Create room_spawner NBT (17x8x17) in common/src/main/resources/data/chronosphere/structure/master_clock_room_spawner.nbt (mob spawners for Skeleton/Zombie, multiple exits)
- [ ] T130c [P] [US3] Create room_maze NBT (21x8x21) in common/src/main/resources/data/chronosphere/structure/master_clock_room_maze.nbt (maze layout, Reversing Time Sandstone walls, chest at dead-ends)
- [ ] T130d [P] [US3] Create room_puzzle_redstone NBT (15x10x15) in common/src/main/resources/data/chronosphere/structure/master_clock_room_puzzle_redstone.nbt (redstone puzzle, lever sequence, door unlock)
- [ ] T130e [P] [US3] Create room_lava NBT (17x12x17) in common/src/main/resources/data/chronosphere/structure/master_clock_room_lava.nbt (lava trap, parkour, Reversing Time Sandstone platforms)
- [ ] T130f [P] [US3] Create room_time_puzzle NBT (15x8x15) in common/src/main/resources/data/chronosphere/structure/master_clock_room_time_puzzle.nbt (lever→door opens→auto-closes, timing challenge)
- [ ] T130g [P] [US3] Create room_guardian_arena NBT (19x10x19) in common/src/main/resources/data/chronosphere/structure/master_clock_room_guardian_arena.nbt (elite enemies, combat arena, reward chest)
- [ ] T130h [P] [US3] Create room_rest NBT (13x8x13) in common/src/main/resources/data/chronosphere/structure/master_clock_room_rest.nbt (safe room, bed placement area, food/potion chest)

#### Ancient Gears Item (Progressive Unlock)

- [x] T131a [P] [US3] Create Ancient Gear item in common/src/main/java/com/chronosphere/items/quest/AncientGearItem.java
- [x] T131b [P] [US3] Register Ancient Gear in ModItems registry
- [x] T131c [P] [US3] Create Ancient Gear texture in common/src/main/resources/assets/chronosphere/textures/item/ancient_gear.png (clockwork gear theme)
- [x] T131d [US3] Implement Ancient Gears detection logic in BlockEventHandler.java (check 3 gears in inventory, open boss room door)

#### Jigsaw Template Pools

- [x] T132a [P] [US3] Create entrance pool JSON in common/src/main/resources/data/chronosphere/worldgen/template_pool/master_clock/entrance_pool.json (single entrance NBT)
- [ ] T132b [P] [US3] Create room pool JSON in common/src/main/resources/data/chronosphere/worldgen/template_pool/master_clock/room_pool.json (8 room variants, equal weights)
- [x] T132c [P] [US3] Create boss room pool JSON in common/src/main/resources/data/chronosphere/worldgen/template_pool/master_clock/boss_room_pool.json (single boss room NBT)

#### Structure Configuration

- [ ] T132d [P] [US3] Create loot processor JSON in common/src/main/resources/data/chronosphere/worldgen/processor_list/master_clock_loot.json (chest loot with Ancient Gears)
- [x] T132e [P] [US3] Create structure JSON in common/src/main/resources/data/chronosphere/worldgen/structure/master_clock.json (references entrance pool, Jigsaw configuration)
- [x] T132f [P] [US3] Create structure set JSON in common/src/main/resources/data/chronosphere/worldgen/structure_set/master_clock.json (concentric_rings placement, distance: 80, spread: 20, count: 1)
- [x] T132g [P] [US3] Create biome tag has_master_clock in common/src/main/resources/data/chronosphere/tags/worldgen/biome/has_master_clock.json (all Chronosphere biomes)

#### Door Opening Logic

- [x] T133 [US3] Implement Key to Master Clock door opening logic in BlockEventHandler.java (entrance door unlock when key used)

### Entities - Time Tyrant (Boss) (US3)

- [x] T134 [US3] Create Time Tyrant entity in common/src/main/java/com/chronosphere/entities/bosses/TimeTyrantEntity.java
- [x] T135 [US3] Register Time Tyrant in ModEntities registry
- [x] T136 [US3] Implement Time Tyrant AI (Phase 1: time stop, Phase 2: teleport + speed, Phase 3: AoE + HP recovery) in common/src/main/java/com/chronosphere/entities/bosses/TimeTyrantAI.java
- [x] T137 [US3] Create Time Tyrant loot table in common/src/main/resources/data/chronosphere/loot_tables/entities/time_tyrant.json
- [x] T138 [US3] Implement Stasis Core destruction on defeat in EntityEventHandler.java
- [x] T139 [US3] Implement reversed resonance trigger (60 seconds) on defeat in EntityEventHandler.java
- [x] T140 [US3] Implement dimension stabilization on defeat in common/src/main/java/com/chronosphere/core/dimension/DimensionStabilizer.java

### Items - Boss Materials (US3)

- [x] T141 [P] [US3] Create Fragment of Stasis Core item in common/src/main/java/com/chronosphere/items/base/FragmentOfStasisCoreItem.java
- [x] T142 [P] [US3] Register Fragment of Stasis Core in ModItems registry
- [x] T143 [P] [US3] Create Fragment of Stasis Core texture in common/src/main/resources/assets/chronosphere/textures/item/fragment_of_stasis_core.png
- [x] T144 [P] [US3] Create Eye of Chronos item in common/src/main/java/com/chronosphere/items/artifacts/EyeOfChronosItem.java
- [x] T145 [P] [US3] Register Eye of Chronos in ModItems registry
- [x] T146 [P] [US3] Create Eye of Chronos texture in common/src/main/resources/assets/chronosphere/textures/item/eye_of_chronos.png
- [x] T147 [US3] Implement enhanced time distortion effect (Slowness V) when Eye of Chronos is in inventory in EntityEventHandler.java

### Weapons - Chronoblade (US3)

- [x] T148 [P] [US3] Create Chronoblade item in common/src/main/java/com/chronosphere/items/artifacts/ChronobladeItem.java
- [x] T149 [P] [US3] Register Chronoblade in ModItems registry
- [x] T150 [P] [US3] Create Chronoblade texture in common/src/main/resources/assets/chronosphere/textures/item/chronoblade.png
- [x] T151 [P] [US3] Create Chronoblade recipe in common/src/main/resources/data/chronosphere/recipes/chronoblade.json
- [x] T152 [US3] Implement AI skip on hit (25% chance) in common/src/main/java/com/chronosphere/items/artifacts/ChronobladeAISkipHandler.java

### Armor - Time Guardian's Mail (US3)

- [x] T153 [P] [US3] Create Time Guardian's Mail item in common/src/main/java/com/chronosphere/items/artifacts/TimeGuardianMailItem.java
- [x] T154 [P] [US3] Register Time Guardian's Mail in ModItems registry
- [x] T155 [P] [US3] Create Time Guardian's Mail texture in common/src/main/resources/assets/chronosphere/textures/item/time_guardian_mail.png
- [x] T156 [P] [US3] Create Time Guardian's Mail recipe in common/src/main/resources/data/chronosphere/recipes/time_guardian_mail.json
- [x] T157 [US3] Implement rollback on lethal damage (20% chance, 60s cooldown) in common/src/main/java/com/chronosphere/items/artifacts/TimeGuardianMailRollbackHandler.java

### Armor - Echoing Time Boots (US3)

- [x] T158 [P] [US3] Create Echoing Time Boots item in common/src/main/java/com/chronosphere/items/artifacts/EchoingTimeBootsItem.java
- [x] T159 [P] [US3] Register Echoing Time Boots in ModItems registry
- [x] T160 [P] [US3] Create Echoing Time Boots texture in common/src/main/resources/assets/chronosphere/textures/item/echoing_time_boots.png
- [x] T161 [P] [US3] Create Echoing Time Boots recipe in common/src/main/resources/data/chronosphere/recipes/echoing_time_boots.json
- [x] T162 [US3] Create Decoy Entity in common/src/main/java/com/chronosphere/entities/DecoyEntity.java
- [x] T163 [US3] Register Decoy Entity in ModEntities registry
- [x] T164 [US3] Implement decoy summoning on sprint (15s cooldown) in common/src/main/java/com/chronosphere/items/artifacts/EchoingTimeBootsDecoyHandler.java

### Tools - Ultimate Spatially Linked Pickaxe (US3)

- [x] T165 [P] [US3] Create ultimate version recipe in common/src/main/resources/data/chronosphere/recipes/spatially_linked_pickaxe_ultimate.json
- [x] T166 [US3] Update Spatially Linked Pickaxe with enhanced drop multiplier for ultimate version

### Utility - Unstable Pocket Watch (US3)

- [x] T167 [P] [US3] Create Unstable Pocket Watch item in common/src/main/java/com/chronosphere/items/artifacts/UnstablePocketWatchItem.java
- [x] T168 [P] [US3] Register Unstable Pocket Watch in ModItems registry
- [x] T169 [P] [US3] Create Unstable Pocket Watch texture in common/src/main/resources/assets/chronosphere/textures/item/unstable_pocket_watch.png
- [x] T170 [P] [US3] Create Unstable Pocket Watch recipe in common/src/main/resources/data/chronosphere/recipes/unstable_pocket_watch.json
- [x] T171 [US3] Implement speed effect swapping logic (30s cooldown) in common/src/main/java/com/chronosphere/items/artifacts/UnstablePocketWatchSwapHandler.java

### Localization & Creative Tab (US3)

- [x] T171a [US3] Update English localization file with all US3 items/entities in en_us.json
- [x] T171b [US3] Update Japanese localization file with all US3 items/entities in ja_jp.json
- [x] T171c [US3] Add all US3 items to creative tab in ModCreativeTabs

### Recipe Material Review (US3)

**Purpose**: Ensure all US3 recipes use only Chronosphere-obtainable materials (no Overworld-exclusive items like diamonds, ender pearls, nether stars)

- [x] T171d [P] [US3] Review all US3 recipes (Chronoblade, Time Tyrant's Mail, Echoing Time Boots, Ultimate Spatially Linked Pickaxe, Unstable Pocket Watch) for Overworld-exclusive materials
- [x] T171e [P] [US3] Update recipes if needed to use Chronosphere materials (Fragment of Stasis Core, Enhanced Clockstone, Clockstone Block, etc.) - No changes needed, all recipes use appropriate Chronosphere materials

### Boss Battle Balance & Strategy (US3 Enhancement - High Priority)

**Purpose**: Time Tyrant戦に戦略的なギミックを追加し、難易度バランスを改善

**Context**: テストプレイの結果、Time Tyrantが強すぎてネザライトフル装備でも倒せない。単純な数値調整ではなく、戦略的なアイテムやギミックを追加して攻略の幅を広げる。

- [x] T171f [US3] Implement Time Clock weakening mechanic in common/src/main/java/com/chronosphere/items/tools/TimeClockItem.java (right-click on Time Tyrant: 10s defense 15→5, speed 50% reduction, 1x per phase, 30s cooldown)
- [x] T171g [US3] Create Time Arrow item in common/src/main/java/com/chronosphere/items/combat/TimeArrowItem.java (extends ArrowItem, applies Slowness III 10s, Weakness II 10s, Glowing 15s to Time Tyrant)
- [x] T171h [P] [US3] Add Time Arrow crafting recipe in common/src/main/resources/data/chronosphere/recipe/time_arrow.json (Fruit of Time center + Arrow bottom + Clockstone top → 4x Time Arrow)
- [x] T171i [P] [US3] Add Time Arrow localization in en_us.json and ja_jp.json

### Advancements (Achievements)

**Purpose**: クロノスフィアの進捗システムを定義し、プレイヤーの達成度を追跡

- [x] T184a [P] Design advancement tree structure and milestone achievements (e.g., "Enter Chronosphere", "Defeat Time Guardian", "Stabilize Portal", "Defeat Time Tyrant")
- [x] T184b [P] Create root advancement for Chronosphere in common/src/main/resources/data/chronosphere/advancement/root.json
- [x] T184c [P] Create US1 advancements (portal creation, dimension entry, portal stabilization) in common/src/main/resources/data/chronosphere/advancement/
- [x] T184d [P] Create US2 advancements (Desert Clock Tower discovery, time manipulation tools, Time Guardian defeat) in common/src/main/resources/data/chronosphere/advancement/
- [x] T184e [P] Create US3 advancements (Master Clock access, Time Tyrant defeat, ultimate artifacts) in common/src/main/resources/data/chronosphere/advancement/
- [x] T184f [P] Create food-related advancements (first Time Fruit consumption, Time Fruit Pie, Time Jam, Time Bread, all custom foods consumed) in common/src/main/resources/data/chronosphere/advancement/
- [x] T184g [P] Create equipment-related advancements (Tier 1 full set, Tier 2 full set) in common/src/main/resources/data/chronosphere/advancement/ (Note: time_distortion_immunity removed as duplicate)
- [x] T184h [P] Create exploration-related advancements (first Time Crystal Ore mined) in common/src/main/resources/data/chronosphere/advancement/ (Note: Forgotten Library and all_biomes_explored removed due to minecraft:location trigger issues)
- [x] T184i [P] Create mob-related advancements (first Temporal Wraith defeated, first Clockwork Sentinel defeated, first trade with Time Keeper) in common/src/main/resources/data/chronosphere/advancement/
- [x] T184j [P] Add advancement localization to en_us.json and ja_jp.json
- [x] T184k Test advancement triggers in-game and verify progression flow

