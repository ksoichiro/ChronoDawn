# Tasks: Chronosphere Mod - 時間操作をテーマにしたMinecraft Mod

**Feature Branch**: `001-chronosphere-mod`
**Input**: Design documents from `/specs/001-chronosphere-mod/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Architecture**: Architectury Multi-Loader Framework (common / fabric / neoforge)
**Tech Stack**: Java 21, Minecraft 1.21.1, NeoForge 21.1.x + Fabric

**Tests**: テストタスクは明示的に要求されている (quickstart.md参照)。JUnit 5 + GameTest Frameworkを使用。

**Organization**: タスクはUser Storyごとにグループ化され、各ストーリーを独立して実装・テスト可能にする。

## Format: `[ID] [P?] [Story] Description`
- **[P]**: 並列実行可能 (異なるファイル、依存関係なし)
- **[Story]**: このタスクが属するUser Story (US1, US2, US3)
- 説明には正確なファイルパスを含む

## Path Conventions
- **common module**: `common/src/main/java/com/chronosphere/`
- **fabric module**: `fabric/src/main/java/com/chronosphere/fabric/`
- **neoforge module**: `neoforge/src/main/java/com/chronosphere/neoforge/`
- **resources**: `common/src/main/resources/`, `fabric/src/main/resources/`, `neoforge/src/main/resources/`
- **tests**: `common/src/test/java/com/chronosphere/`

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

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 全User Storyが依存するコアインフラストラクチャ (この完了前にUser Story作業は開始不可)

**⚠️ CRITICAL**: このフェーズ完了前はUser Story実装を開始できない

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

## Phase 3: User Story 1 - ディメンションへの初回突入と帰還路の確保 (Priority: P1) 🎯 MVP

**Goal**: プレイヤーが古代遺跡でクロックストーンを発見し、ポータルを作成してクロノスフィアに突入。ディメンション内で忘れられた図書館を発見し、ポータル安定化装置を作成してオーバーワールドとクロノスフィアを自由に往来できるようにする。

**Independent Test**: オーバーワールドで古代遺跡を発見→ポータル作成→クロノスフィア突入→ポータル機能停止→忘れられた図書館で設計図発見→ポータル安定化装置作成→ポータル修復→自由往来可能

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

### Additional Tree Variants (US1 Enhancement - Optional, Low Priority)

**Purpose**: 木のバリエーションを増やして視覚的多様性を向上

**Note**: これは優先度が低く、US1の MVP には必須ではない。他の機能が完成後に実装を検討

- [ ] T088t [P] [US1] (Optional) Design Time Wood color variants (e.g., Dark Time Wood, Ancient Time Wood)
- [ ] T088u [P] [US1] (Optional) Create variant textures and block definitions
- [ ] T088v [P] [US1] (Optional) Create variant tree features and configure placement in different biomes

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
- [ ] T208 [P] [US1] (Optional, Low Priority) Create spawn eggs for custom mobs in ModItems for creative mode and debugging (temporal_wraith_spawn_egg, clockwork_sentinel_spawn_egg, time_keeper_spawn_egg)

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

## Phase 4: User Story 2 - 時間操作アイテムの獲得とボス戦への準備 (Priority: P2)

**Goal**: プレイヤーが砂漠の時計塔を探索して強化クロックストーンを入手し、時間操作アイテム (タイムクロック、空間連結ツルハシ) を作成。時の番人 (中ボス) を撃破してマスタークロックへの鍵を入手。

**Independent Test**: 砂漠の時計塔を探索→強化クロックストーン入手→時間操作アイテム作成→効果確認 (Mob攻撃キャンセル、ドロップ増加)→時の番人撃破→鍵入手

### Tests for User Story 2

- [X] T089 [P] [US2] Write GameTest for Desert Clock Tower generation in common/src/test/java/com/chronosphere/integration/DesertClockTowerTest.java
- [X] T090 [P] [US2] Write unit test for Time Clock cooldown logic in common/src/test/java/com/chronosphere/unit/TimeClockTest.java
- [X] T091 [P] [US2] Write unit test for Spatially Linked Pickaxe drop multiplier in common/src/test/java/com/chronosphere/unit/PickaxeDropTest.java
- [ ] T092 [P] [US2] Write GameTest for Time Guardian boss fight in common/src/test/java/com/chronosphere/integration/TimeGuardianFightTest.java

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

### Player Guidance & Discovery System (US2 Enhancement)

**Purpose**: プレイヤーが構造物やアイテムを発見し、ゲームを進行できるようガイダンスを提供

**Current Issue**: Ancient Ruins、Desert Clock Tower、Master Clock Towerなどの構造物の場所や、アイテムの入手方法について説明がなく、プレイヤーが作者の想定通りに進行するのは困難

**Guidance Methods**: 書物アイテム、村人との取引（地図）、進捗ヒント、構造物の出現頻度調整、ロケーターアイテムなど

- [ ] T115f [US2] Research appropriate player guidance methods (book items, advancement hints, villager trades, structure frequency)
- [ ] T115g [P] [US2] Create Chronicle of Chronosphere book item (guide book explaining dimension mechanics, structures, and progression)
- [ ] T115h [P] [US2] Add villager trades for structure maps (Ancient Ruins Map, Desert Clock Tower Map, Master Clock Tower Map)
- [ ] T115i [US2] Add advancement system with descriptive hints for key progression milestones (first portal, Ancient Ruins discovery, Time Guardian defeat)
- [ ] T115j [US2] Adjust structure spawn rates to make discovery easier (increase frequency or reduce spacing)
- [ ] T115k [P] [US2] Create Time Compass item (points to nearest key structure, similar to lodestone compass)
- [ ] T115l [US2] Add initial guidance on first dimension entry (chat message, advancement, or book given to player)

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

- [ ] T250 [P] [US2] Create Enhanced Clockstone Sword item in common/src/main/java/com/chronosphere/items/equipment/EnhancedClockstoneSwordItem.java (Tier 2 weapon, better than Tier 1, chance to freeze enemy on hit for 2s)
- [ ] T251 [P] [US2] Create Enhanced Clockstone Axe/Shovel/Hoe items in common/src/main/java/com/chronosphere/items/equipment/ (Tier 2 tools, faster mining speed)
- [ ] T252 [P] [US2] Create Enhanced Clockstone Armor Set (Helmet, Chestplate, Leggings, Boots) in common/src/main/java/com/chronosphere/items/equipment/ (Tier 2 armor, higher protection + set bonus: immunity to time distortion effects)
- [ ] T253 [P] [US2] Create crafting recipes for Tier 2 equipment in common/src/main/resources/data/chronosphere/recipes/ (uses Enhanced Clockstone + Time Crystal)
- [ ] T254 [P] [US2] Implement time-manipulation effects for Tier 2 equipment (freeze on hit for sword, set bonus for armor)
- [ ] T255 [P] [US2] Create textures for Tier 2 equipment in common/src/main/resources/assets/chronosphere/textures/item/
- [ ] T256 [US2] Add Tier 2 equipment to creative tab and localization files

**Checkpoint**: User Story 1とUser Story 2が両方とも独立して動作すること

---

## Phase 5: User Story 3 - ラスボス撃破と最終報酬の獲得 (Priority: P3)

**Goal**: プレイヤーがマスタークロックへの鍵を使用して最深部に到達し、時間の暴君 (ラスボス) を撃破。静止のコアが破壊され、クロノスの瞳と静止のコアの破片を獲得。究極のアーティファクトを作成可能にする。

**Independent Test**: マスタークロック到達→鍵使用→最深部侵入→時間の暴君撃破→クロノスの瞳と破片獲得→究極アイテム作成→効果確認 (攻撃AIスキップ、ロールバック、デコイ召喚、速度効果入れ替え)

### Tests for User Story 3

- [ ] T123 [P] [US3] Write GameTest for Master Clock structure generation in common/src/test/java/com/chronosphere/integration/MasterClockTest.java
- [ ] T124 [P] [US3] Write GameTest for Time Tyrant boss fight in common/src/test/java/com/chronosphere/integration/TimeTyrantFightTest.java
- [ ] T125 [P] [US3] Write unit test for Chronoblade AI skip probability in common/src/test/java/com/chronosphere/unit/ChronobladeTest.java
- [ ] T126 [P] [US3] Write unit test for Time Guardian Mail rollback logic in common/src/test/java/com/chronosphere/unit/TimeGuardianMailTest.java
- [ ] T127 [P] [US3] Write GameTest for Echoing Time Boots decoy in common/src/test/java/com/chronosphere/integration/DecoyTest.java

### World Generation - Master Clock (US3)

**Design**: Underground palace structure with Jigsaw random room generation (see master-clock-design.md)
**Location**: Near world spawn using concentric_rings placement (80-100 chunk radius, 1 per dimension)
**Size**: Entrance 15x10x15, Dungeon 41x50x41, Boss Room 35x20x35

#### NBT Structures - Entrance & Boss Room

- [ ] T128 [P] [US3] Create Master Clock entrance NBT (15x10x15) in common/src/main/resources/data/chronosphere/structure/master_clock_entrance.nbt (small surface temple, key-locked door, stairs to underground)
- [ ] T129 [P] [US3] Create Master Clock boss room NBT (35x20x35) in common/src/main/resources/data/chronosphere/structure/master_clock_boss_room.nbt (large hall, Time Tyrant spawn point, pillars, reward chest)

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

- [ ] T131a [P] [US3] Create Ancient Gear item in common/src/main/java/com/chronosphere/items/quest/AncientGearItem.java
- [ ] T131b [P] [US3] Register Ancient Gear in ModItems registry
- [ ] T131c [P] [US3] Create Ancient Gear texture in common/src/main/resources/assets/chronosphere/textures/item/ancient_gear.png (clockwork gear theme)
- [ ] T131d [US3] Implement Ancient Gears detection logic in BlockEventHandler.java (check 3 gears in inventory, open boss room door)

#### Jigsaw Template Pools

- [ ] T132a [P] [US3] Create entrance pool JSON in common/src/main/resources/data/chronosphere/worldgen/template_pool/master_clock/entrance_pool.json (single entrance NBT)
- [ ] T132b [P] [US3] Create room pool JSON in common/src/main/resources/data/chronosphere/worldgen/template_pool/master_clock/room_pool.json (8 room variants, equal weights)
- [ ] T132c [P] [US3] Create boss room pool JSON in common/src/main/resources/data/chronosphere/worldgen/template_pool/master_clock/boss_room_pool.json (single boss room NBT)

#### Structure Configuration

- [ ] T132d [P] [US3] Create loot processor JSON in common/src/main/resources/data/chronosphere/worldgen/processor_list/master_clock_loot.json (chest loot with Ancient Gears)
- [ ] T132e [P] [US3] Create structure JSON in common/src/main/resources/data/chronosphere/worldgen/structure/master_clock.json (references entrance pool, Jigsaw configuration)
- [ ] T132f [P] [US3] Create structure set JSON in common/src/main/resources/data/chronosphere/worldgen/structure_set/master_clock.json (concentric_rings placement, distance: 80, spread: 20, count: 1)
- [ ] T132g [P] [US3] Create biome tag has_master_clock in common/src/main/resources/data/chronosphere/tags/worldgen/biome/has_master_clock.json (all Chronosphere biomes)

#### Door Opening Logic

- [ ] T133 [US3] Implement Key to Master Clock door opening logic in BlockEventHandler.java (entrance door unlock when key used)

### Entities - Time Tyrant (Boss) (US3)

- [ ] T134 [US3] Create Time Tyrant entity in common/src/main/java/com/chronosphere/entities/bosses/TimeTyrantEntity.java
- [ ] T135 [US3] Register Time Tyrant in ModEntities registry
- [ ] T136 [US3] Implement Time Tyrant AI (Phase 1: time stop, Phase 2: teleport + speed, Phase 3: AoE + HP recovery) in common/src/main/java/com/chronosphere/entities/bosses/TimeTyrantAI.java
- [ ] T137 [US3] Create Time Tyrant loot table in common/src/main/resources/data/chronosphere/loot_tables/entities/time_tyrant.json
- [ ] T138 [US3] Implement Stasis Core destruction on defeat in EntityEventHandler.java
- [ ] T139 [US3] Implement reversed resonance trigger (60 seconds) on defeat in EntityEventHandler.java
- [ ] T140 [US3] Implement dimension stabilization on defeat in common/src/main/java/com/chronosphere/core/dimension/DimensionStabilizer.java

### Items - Boss Materials (US3)

- [ ] T141 [P] [US3] Create Fragment of Stasis Core item in common/src/main/java/com/chronosphere/items/base/FragmentOfStasisCoreItem.java
- [ ] T142 [P] [US3] Register Fragment of Stasis Core in ModItems registry
- [ ] T143 [P] [US3] Create Fragment of Stasis Core texture in common/src/main/resources/assets/chronosphere/textures/item/fragment_of_stasis_core.png
- [ ] T144 [P] [US3] Create Eye of Chronos item in common/src/main/java/com/chronosphere/items/artifacts/EyeOfChronosItem.java
- [ ] T145 [P] [US3] Register Eye of Chronos in ModItems registry
- [ ] T146 [P] [US3] Create Eye of Chronos texture in common/src/main/resources/assets/chronosphere/textures/item/eye_of_chronos.png
- [ ] T147 [US3] Implement enhanced time distortion effect (Slowness V) when Eye of Chronos is in inventory in EntityEventHandler.java

### Weapons - Chronoblade (US3)

- [ ] T148 [P] [US3] Create Chronoblade item in common/src/main/java/com/chronosphere/items/artifacts/ChronobladeItem.java
- [ ] T149 [P] [US3] Register Chronoblade in ModItems registry
- [ ] T150 [P] [US3] Create Chronoblade texture in common/src/main/resources/assets/chronosphere/textures/item/chronoblade.png
- [ ] T151 [P] [US3] Create Chronoblade recipe in common/src/main/resources/data/chronosphere/recipes/chronoblade.json
- [ ] T152 [US3] Implement AI skip on hit (25% chance) in common/src/main/java/com/chronosphere/items/artifacts/ChronobladeAISkipHandler.java

### Armor - Time Guardian's Mail (US3)

- [ ] T153 [P] [US3] Create Time Guardian's Mail item in common/src/main/java/com/chronosphere/items/artifacts/TimeGuardianMailItem.java
- [ ] T154 [P] [US3] Register Time Guardian's Mail in ModItems registry
- [ ] T155 [P] [US3] Create Time Guardian's Mail texture in common/src/main/resources/assets/chronosphere/textures/item/time_guardian_mail.png
- [ ] T156 [P] [US3] Create Time Guardian's Mail recipe in common/src/main/resources/data/chronosphere/recipes/time_guardian_mail.json
- [ ] T157 [US3] Implement rollback on lethal damage (20% chance, 60s cooldown) in common/src/main/java/com/chronosphere/items/artifacts/TimeGuardianMailRollbackHandler.java

### Armor - Echoing Time Boots (US3)

- [ ] T158 [P] [US3] Create Echoing Time Boots item in common/src/main/java/com/chronosphere/items/artifacts/EchoingTimeBootsItem.java
- [ ] T159 [P] [US3] Register Echoing Time Boots in ModItems registry
- [ ] T160 [P] [US3] Create Echoing Time Boots texture in common/src/main/resources/assets/chronosphere/textures/item/echoing_time_boots.png
- [ ] T161 [P] [US3] Create Echoing Time Boots recipe in common/src/main/resources/data/chronosphere/recipes/echoing_time_boots.json
- [ ] T162 [US3] Create Decoy Entity in common/src/main/java/com/chronosphere/entities/DecoyEntity.java
- [ ] T163 [US3] Register Decoy Entity in ModEntities registry
- [ ] T164 [US3] Implement decoy summoning on sprint (15s cooldown) in common/src/main/java/com/chronosphere/items/artifacts/EchoingTimeBootsDecoyHandler.java

### Tools - Ultimate Spatially Linked Pickaxe (US3)

- [ ] T165 [P] [US3] Create ultimate version recipe in common/src/main/resources/data/chronosphere/recipes/spatially_linked_pickaxe_ultimate.json
- [ ] T166 [US3] Update Spatially Linked Pickaxe with enhanced drop multiplier for ultimate version

### Utility - Unstable Pocket Watch (US3)

- [ ] T167 [P] [US3] Create Unstable Pocket Watch item in common/src/main/java/com/chronosphere/items/artifacts/UnstablePocketWatchItem.java
- [ ] T168 [P] [US3] Register Unstable Pocket Watch in ModItems registry
- [ ] T169 [P] [US3] Create Unstable Pocket Watch texture in common/src/main/resources/assets/chronosphere/textures/item/unstable_pocket_watch.png
- [ ] T170 [P] [US3] Create Unstable Pocket Watch recipe in common/src/main/resources/data/chronosphere/recipes/unstable_pocket_watch.json
- [ ] T171 [US3] Implement speed effect swapping logic (30s cooldown) in common/src/main/java/com/chronosphere/items/artifacts/UnstablePocketWatchSwapHandler.java

### Localization & Creative Tab (US3)

- [ ] T171a [US3] Update English localization file with all US3 items/entities in en_us.json
- [ ] T171b [US3] Update Japanese localization file with all US3 items/entities in ja_jp.json
- [ ] T171c [US3] Add all US3 items to creative tab in ModCreativeTabs

### Recipe Material Review (US3)

**Purpose**: Ensure all US3 recipes use only Chronosphere-obtainable materials (no Overworld-exclusive items like diamonds, ender pearls, nether stars)

- [ ] T171d [P] [US3] Review all US3 recipes (Chronoblade, Time Guardian's Mail, Echoing Time Boots, Ultimate Spatially Linked Pickaxe, Unstable Pocket Watch) for Overworld-exclusive materials
- [ ] T171e [P] [US3] Update recipes if needed to use Chronosphere materials (Fragment of Stasis Core, Enhanced Clockstone, Clockstone Block, etc.)

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

## Phase 6: Cross-Loader Compatibility & Polish

**Purpose**: 両ローダー間の互換性確保、パフォーマンス最適化、ドキュメント整備

### Cross-Loader Testing

- [ ] T172 [P] Run all GameTests on Fabric loader using ./gradlew :fabric:runGameTest
- [ ] T173 [P] Run all GameTests on NeoForge loader using ./gradlew :neoforge:runGameTest
- [ ] T174 Verify entity renderer registration for Fabric (standard API) in fabric/src/main/java/com/chronosphere/fabric/client/ChronosphereClientFabric.java
- [ ] T175 Verify entity renderer registration for NeoForge (manual event registration) in neoforge/src/main/java/com/chronosphere/neoforge/event/EntityRendererHandler.java
- [ ] T176 Test portal mechanics on both loaders for consistency
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

### Advancements (Achievements)

**Purpose**: クロノスフィアの進捗システムを定義し、プレイヤーの達成度を追跡

- [ ] T184a [P] Design advancement tree structure and milestone achievements (e.g., "Enter Chronosphere", "Defeat Time Guardian", "Stabilize Portal", "Defeat Time Tyrant")
- [ ] T184b [P] Create root advancement for Chronosphere in common/src/main/resources/data/chronosphere/advancements/root.json
- [ ] T184c [P] Create US1 advancements (portal creation, dimension entry, portal stabilization) in common/src/main/resources/data/chronosphere/advancements/
- [ ] T184d [P] Create US2 advancements (Desert Clock Tower discovery, time manipulation tools, Time Guardian defeat) in common/src/main/resources/data/chronosphere/advancements/
- [ ] T184e [P] Create US3 advancements (Master Clock access, Time Tyrant defeat, ultimate artifacts) in common/src/main/resources/data/chronosphere/advancements/
- [ ] T184f [P] Add advancement localization to en_us.json and ja_jp.json
- [ ] T184g Test advancement triggers in-game and verify progression flow

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

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 依存関係なし - 即座に開始可能
- **Foundational (Phase 2)**: Setup完了に依存 - 全User Storyをブロック
- **User Stories (Phase 3-5)**: Foundationalフェーズ完了に依存
  - User Storyは並列実行可能 (リソースがあれば)
  - または優先順位順に逐次実行 (P1 → P2 → P3)
- **Polish (Phase 6)**: 全User Story完了に依存

### User Story Dependencies

- **User Story 1 (P1)**: Foundational完了後に開始可能 - 他ストーリーへの依存なし
- **User Story 2 (P2)**: Foundational完了後に開始可能 - US1と独立してテスト可能
- **User Story 3 (P3)**: Foundational完了後に開始可能 - US1/US2と独立してテスト可能

### Within Each User Story

- テスト (含まれる場合) は実装前に記述し、FAILすることを確認
- モデル→サービス→エンドポイントの順
- コア実装→統合の順
- ストーリー完了後に次の優先度へ進む

### Parallel Opportunities

- Phase 1の[P]タスクは並列実行可能
- Phase 2の[P]タスクは並列実行可能
- Foundationalフェーズ完了後、全User Storyを並列開始可能 (チーム容量次第)
- 各User Story内の[P]タスクは並列実行可能
- 各User Story内のテスト[P]タスクは並列実行可能
- 異なるUser Storyは異なるチームメンバーが並列作業可能

---

## Parallel Example: User Story 1

```bash
# User Story 1の全テストを同時起動 (TDD approach):
Task: "Write unit test for dimension registration"
Task: "Write unit test for portal state transitions"
Task: "Write GameTest for portal activation"
Task: "Write GameTest for dimension travel"
Task: "Write GameTest for portal stabilization"

# User Story 1の基本アイテムを同時作成:
Task: "Create Clockstone item in common/src/main/java/com/chronosphere/items/base/ClockstoneItem.java"
Task: "Create Clockstone texture in common/src/main/resources/assets/chronosphere/textures/item/clockstone.png"
Task: "Create Clockstone item model in common/src/main/resources/assets/chronosphere/models/item/clockstone.json"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1完了: Setup
2. Phase 2完了: Foundational (CRITICAL - 全ストーリーをブロック)
3. Phase 3完了: User Story 1
4. **STOP and VALIDATE**: User Story 1を独立してテスト
5. デプロイ/デモ準備完了

### Incremental Delivery

1. Setup + Foundational完了 → 基盤準備完了
2. User Story 1追加 → 独立テスト → デプロイ/デモ (MVP!)
3. User Story 2追加 → 独立テスト → デプロイ/デモ
4. User Story 3追加 → 独立テスト → デプロイ/デモ
5. 各ストーリーが前のストーリーを壊さずに価値を追加

### Parallel Team Strategy

複数開発者がいる場合:

1. チーム全体でSetup + Foundational完了
2. Foundational完了後:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. ストーリーが独立して完了・統合

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

## Total Task Count

**Total Tasks**: 363 (updated with gameplay enhancement tasks: custom mobs, equipment tiers, food system, biomes, blocks, terrain features, spawn eggs, Time Wood functional blocks)

**Breakdown by Phase**:
- Phase 1 (Setup): 16 tasks (added T012a-b for dependency metadata)
- Phase 2 (Foundational): 20 tasks
- Phase 3 (User Story 1 - P1): 183 tasks (includes fruit enhancement + exploration diversity + gameplay enhancements: T200-249 + Time Wood functional blocks: T080v-T080am)
- Phase 4 (User Story 2 - P2): 43 tasks (added desert biome spawning + equipment tier system: T250-256)
- Phase 5 (User Story 3 - P3): 71 tasks (added Master Clock Jigsaw system + custom terrain features: T260-265)
- Phase 6 (Polish): 30 tasks (added advancement system)

**Breakdown by User Story**:
- User Story 1 (P1): 182 tasks (50% of total) - includes gameplay enhancements + Time Wood functional blocks
- User Story 2 (P2): 43 tasks (12% of total) - includes equipment tier system
- User Story 3 (P3): 71 tasks (20% of total) - includes custom terrain features
- Infrastructure (Setup + Foundational + Polish): 66 tasks (18% of total)

**Phase 3 Enhancement Breakdown**:
- Core US1 features: 75 tasks (T001-T088c)
- Multiple Biomes: 6 tasks (T088d-T088i)
- Basic Mob Spawning: 6 tasks (T088j-T088o)
- Biome Color Adjustment: 4 tasks (T088p-T088s)
- Additional Tree Variants (Optional): 3 tasks (T088t-T088v)
- Vegetation System: 7 tasks (T088w-T088ac)
- Decorative Terrain Features: 6 tasks (T088ad-T088ai)
- Biome-Specific Landmarks (Optional): 5 tasks (T088aj-T088an)
- Ambient Sounds & Particles (Optional): 6 tasks (T088ao-T088at)
- **Time Wood Functional Blocks**: 18 tasks (T080v-T080am, crafting table, door, trapdoor, fence gate, button, pressure plate, sign, hanging sign, boat, chest boat)
- **Custom Mobs (High Priority)**: 9 tasks (T200-T208, includes optional spawn eggs)
- **Basic Equipment Set (High Priority)**: 9 tasks (T210-T218)
- **Food System Expansion (High Priority)**: 9 tasks (T220-T228)
- **Additional Biomes (Medium Priority)**: 6 tasks (T230-T235)
- **Block Variety Expansion (Medium Priority)**: 10 tasks (T240-T249)

**Phase 4 Enhancement Breakdown**:
- Core US2 features: 36 tasks (T089-T122e)
- **Equipment Tier System (High Priority)**: 7 tasks (T250-T256)

**Phase 5 Enhancement Breakdown**:
- Core US3 features: 65 tasks (T123-T171e)
- **Custom Terrain Features (Medium Priority)**: 6 tasks (T260-T265)

**Parallel Opportunities**:
- Phase 1: 7 parallel groups
- Phase 2: 8 parallel groups
- Phase 3 (US1): 55 parallel groups (includes gameplay enhancement tasks)
- Phase 4 (US2): 20 parallel groups (includes equipment tier system)
- Phase 5 (US3): 26 parallel groups (includes custom terrain features)
- Phase 6: 10 parallel groups

**Independent Test Criteria**:
- User Story 1: Portal creation, dimension travel, portal stabilization, free travel between dimensions, custom tree generation with harvestable fruits, multiple biomes (plains/ocean/forest/desert/mountain/swamp/snowy/cave) with vegetation, custom mob spawning with time distortion effects, decorative terrain features, basic equipment set, food system with crafting, Time Wood functional blocks (crafting table, door, trapdoor, fence gate, button, pressure plate, sign, boat)
- User Story 2: Desert Clock Tower exploration, time manipulation items, Time Guardian defeat, key acquisition, Enhanced Clockstone equipment tier
- User Story 3: Master Clock access, Time Tyrant defeat, ultimate artifacts creation, artifact effects validation, custom terrain features

**Suggested MVP Scope**: User Story 1 (Phase 1 + Phase 2 + Phase 3 Core + Multiple Biomes + Basic Mob Spawning + Vegetation System + Custom Mobs + Basic Equipment Set + Food System + Time Wood Functional Blocks) = 221 tasks (recommended for initial release with gameplay enhancements, excludes optional enhancements and custom terrain features)
