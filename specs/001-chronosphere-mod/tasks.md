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

- [ ] T045 [US1] Create portal frame validation logic in common/src/main/java/com/chronosphere/core/portal/PortalFrameValidator.java
- [ ] T046 [US1] Implement portal state machine in common/src/main/java/com/chronosphere/core/portal/PortalStateMachine.java
- [ ] T047 [US1] Implement portal registry in common/src/main/java/com/chronosphere/core/portal/PortalRegistry.java
- [ ] T048 [US1] Integrate Custom Portal API (Fabric) in fabric/src/main/java/com/chronosphere/fabric/compat/CustomPortalFabric.java
- [ ] T049 [US1] Integrate Custom Portal API (NeoForge) in neoforge/src/main/java/com/chronosphere/neoforge/compat/CustomPortalNeoForge.java

### Blocks - Base Materials (US1)

- [ ] T050 [P] [US1] Create Clockstone Ore block in common/src/main/java/com/chronosphere/blocks/ClockstoneOre.java
- [ ] T051 [P] [US1] Register Clockstone Ore in ModBlocks registry
- [ ] T052 [P] [US1] Create Clockstone Ore texture in common/src/main/resources/assets/chronosphere/textures/block/clockstone_ore.png
- [ ] T053 [P] [US1] Create Clockstone Ore block model in common/src/main/resources/assets/chronosphere/models/block/clockstone_ore.json

### Items - Base Materials (US1)

- [ ] T054 [P] [US1] Create Clockstone item in common/src/main/java/com/chronosphere/items/base/ClockstoneItem.java
- [ ] T055 [P] [US1] Register Clockstone item in ModItems registry
- [ ] T056 [P] [US1] Create Clockstone texture in common/src/main/resources/assets/chronosphere/textures/item/clockstone.png
- [ ] T057 [P] [US1] Create Clockstone item model in common/src/main/resources/assets/chronosphere/models/item/clockstone.json
- [ ] T058 [P] [US1] Create Clockstone Ore loot table in common/src/main/resources/data/chronosphere/loot_tables/blocks/clockstone_ore.json

### Items - Portal Items (US1)

- [ ] T059 [P] [US1] Create Time Hourglass item in common/src/main/java/com/chronosphere/items/TimeHourglassItem.java
- [ ] T060 [P] [US1] Register Time Hourglass in ModItems registry
- [ ] T061 [P] [US1] Create Time Hourglass texture in common/src/main/resources/assets/chronosphere/textures/item/time_hourglass.png
- [ ] T062 [P] [US1] Create Time Hourglass recipe in common/src/main/resources/data/chronosphere/recipes/time_hourglass.json
- [ ] T063 [P] [US1] Create Portal Stabilizer item in common/src/main/java/com/chronosphere/items/PortalStabilizerItem.java
- [ ] T064 [P] [US1] Register Portal Stabilizer in ModItems registry
- [ ] T065 [P] [US1] Create Portal Stabilizer texture in common/src/main/resources/assets/chronosphere/textures/item/portal_stabilizer.png
- [ ] T066 [P] [US1] Create Portal Stabilizer recipe in common/src/main/resources/data/chronosphere/recipes/portal_stabilizer.json

### World Generation - Structures (US1)

- [ ] T067 [P] [US1] Create Ancient Ruins structure NBT in common/src/main/resources/data/chronosphere/structures/ancient_ruins.nbt
- [ ] T068 [P] [US1] Implement Ancient Ruins structure feature in common/src/main/java/com/chronosphere/worldgen/structures/AncientRuinsStructure.java
- [ ] T069 [P] [US1] Create Ancient Ruins structure set in common/src/main/resources/data/chronosphere/worldgen/structure_set/ancient_ruins.json
- [ ] T070 [P] [US1] Create Forgotten Library structure NBT in common/src/main/resources/data/chronosphere/structures/forgotten_library.nbt
- [ ] T071 [P] [US1] Implement Forgotten Library structure feature in common/src/main/java/com/chronosphere/worldgen/structures/ForgottenLibraryStructure.java
- [ ] T072 [P] [US1] Create Forgotten Library structure set in common/src/main/resources/data/chronosphere/worldgen/structure_set/forgotten_library.json

### Time Distortion Effect (US1)

- [ ] T073 [US1] Implement time distortion effect logic in common/src/main/java/com/chronosphere/core/time/TimeDistortionEffect.java
- [ ] T074 [US1] Add entity tick event handler for Slowness IV application in EntityEventHandler.java
- [ ] T075 [US1] Write unit test for time distortion effect in common/src/test/java/com/chronosphere/unit/TimeDistortionTest.java

### Consumables - Fruit of Time (US1)

- [ ] T076 [P] [US1] Create Fruit of Time item in common/src/main/java/com/chronosphere/items/consumables/FruitOfTimeItem.java
- [ ] T077 [P] [US1] Register Fruit of Time in ModItems registry
- [ ] T078 [P] [US1] Create Fruit of Time texture in common/src/main/resources/assets/chronosphere/textures/item/fruit_of_time.png
- [ ] T079 [P] [US1] Create Fruit of Time block feature in common/src/main/java/com/chronosphere/worldgen/features/FruitOfTimeTreeFeature.java
- [ ] T080 [P] [US1] Configure Fruit of Time tree placement in common/src/main/resources/data/chronosphere/worldgen/placed_feature/fruit_of_time_tree.json

### Special Blocks (US1)

- [ ] T081 [P] [US1] Create Reversing Time Sandstone block in common/src/main/java/com/chronosphere/blocks/ReversingTimeSandstone.java
- [ ] T082 [P] [US1] Register Reversing Time Sandstone in ModBlocks registry
- [ ] T083 [P] [US1] Implement block restoration logic in BlockEventHandler.java
- [ ] T084 [P] [US1] Create Unstable Fungus block in common/src/main/java/com/chronosphere/blocks/UnstableFungus.java
- [ ] T085 [P] [US1] Register Unstable Fungus in ModBlocks registry
- [ ] T086 [P] [US1] Implement collision event handler in EntityEventHandler.java for random speed effects

### Respawn Logic (US1)

- [ ] T087 [US1] Implement respawn handler in PlayerEventHandler.java
- [ ] T088 [US1] Write GameTest for respawn behavior in common/src/test/java/com/chronosphere/integration/RespawnTest.java

**Checkpoint**: User Story 1が完全に機能し、独立してテスト可能であること

---

## Phase 4: User Story 2 - 時間操作アイテムの獲得とボス戦への準備 (Priority: P2)

**Goal**: プレイヤーが砂漠の時計塔を探索して強化クロックストーンを入手し、時間操作アイテム (タイムクロック、空間連結ツルハシ) を作成。時の番人 (中ボス) を撃破してマスタークロックへの鍵を入手。

**Independent Test**: 砂漠の時計塔を探索→強化クロックストーン入手→時間操作アイテム作成→効果確認 (Mob攻撃キャンセル、ドロップ増加)→時の番人撃破→鍵入手

### Tests for User Story 2

- [ ] T089 [P] [US2] Write GameTest for Desert Clock Tower generation in common/src/test/java/com/chronosphere/integration/DesertClockTowerTest.java
- [ ] T090 [P] [US2] Write unit test for Time Clock cooldown logic in common/src/test/java/com/chronosphere/unit/TimeClockTest.java
- [ ] T091 [P] [US2] Write unit test for Spatially Linked Pickaxe drop multiplier in common/src/test/java/com/chronosphere/unit/PickaxeDropTest.java
- [ ] T092 [P] [US2] Write GameTest for Time Guardian boss fight in common/src/test/java/com/chronosphere/integration/TimeGuardianFightTest.java

### World Generation - Desert Clock Tower (US2)

- [ ] T093 [P] [US2] Create Desert Clock Tower structure NBT in common/src/main/resources/data/chronosphere/structures/desert_clock_tower.nbt
- [ ] T094 [P] [US2] Implement Desert Clock Tower structure feature in common/src/main/java/com/chronosphere/worldgen/structures/DesertClockTowerStructure.java
- [ ] T095 [P] [US2] Create Desert Clock Tower structure set in common/src/main/resources/data/chronosphere/worldgen/structure_set/desert_clock_tower.json

### Items - Enhanced Materials (US2)

- [ ] T096 [P] [US2] Create Enhanced Clockstone item in common/src/main/java/com/chronosphere/items/base/EnhancedClockstoneItem.java
- [ ] T097 [P] [US2] Register Enhanced Clockstone in ModItems registry
- [ ] T098 [P] [US2] Create Enhanced Clockstone texture in common/src/main/resources/assets/chronosphere/textures/item/enhanced_clockstone.png
- [ ] T099 [P] [US2] Configure Enhanced Clockstone loot in Desert Clock Tower chest

### Items - Time Manipulation Tools (US2)

- [ ] T100 [P] [US2] Create Time Clock item in common/src/main/java/com/chronosphere/items/tools/TimeClockItem.java
- [ ] T101 [P] [US2] Register Time Clock in ModItems registry
- [ ] T102 [P] [US2] Create Time Clock texture in common/src/main/resources/assets/chronosphere/textures/item/time_clock.png
- [ ] T103 [P] [US2] Create Time Clock recipe in common/src/main/resources/data/chronosphere/recipes/time_clock.json
- [ ] T104 [P] [US2] Implement Time Clock AI cancellation logic in common/src/main/java/com/chronosphere/core/time/MobAICanceller.java
- [ ] T105 [P] [US2] Create Spatially Linked Pickaxe in common/src/main/java/com/chronosphere/items/tools/SpatiallyLinkedPickaxeItem.java
- [ ] T106 [P] [US2] Register Spatially Linked Pickaxe in ModItems registry
- [ ] T107 [P] [US2] Create Spatially Linked Pickaxe texture in common/src/main/resources/assets/chronosphere/textures/item/spatially_linked_pickaxe.png
- [ ] T108 [P] [US2] Create Spatially Linked Pickaxe recipe in common/src/main/resources/data/chronosphere/recipes/spatially_linked_pickaxe.json
- [ ] T109 [P] [US2] Implement drop doubling logic in BlockEventHandler.java

### Entities - Time Guardian (Mini-Boss) (US2)

- [ ] T110 [US2] Create Time Guardian entity in common/src/main/java/com/chronosphere/entities/bosses/TimeGuardianEntity.java
- [ ] T111 [US2] Register Time Guardian in ModEntities registry
- [ ] T112 [US2] Implement Time Guardian AI (Phase 1: melee, Phase 2: teleport + AoE) in common/src/main/java/com/chronosphere/entities/bosses/TimeGuardianAI.java
- [ ] T113 [US2] Create Time Guardian loot table in common/src/main/resources/data/chronosphere/loot_tables/entities/time_guardian.json
- [ ] T114 [US2] Create Time Guardian spawn logic in common/src/main/java/com/chronosphere/worldgen/spawning/TimeGuardianSpawner.java
- [ ] T115 [US2] Implement reversed resonance trigger on defeat in EntityEventHandler.java

### Items - Key Items (US2)

- [ ] T116 [P] [US2] Create Key to Master Clock item in common/src/main/java/com/chronosphere/items/KeyToMasterClockItem.java
- [ ] T117 [P] [US2] Register Key to Master Clock in ModItems registry
- [ ] T118 [P] [US2] Create Key to Master Clock texture in common/src/main/resources/assets/chronosphere/textures/item/key_to_master_clock.png

### Items - Unstable Hourglass (US2)

- [ ] T119 [P] [US2] Create Unstable Hourglass item in common/src/main/java/com/chronosphere/items/UnstableHourglassItem.java
- [ ] T120 [P] [US2] Register Unstable Hourglass in ModItems registry
- [ ] T121 [P] [US2] Create Unstable Hourglass recipe in common/src/main/resources/data/chronosphere/recipes/unstable_hourglass.json
- [ ] T122 [P] [US2] Implement reversed resonance trigger on crafting in common/src/main/java/com/chronosphere/events/CraftEventHandler.java

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

- [ ] T128 [P] [US3] Create Master Clock structure NBT (entrance) in common/src/main/resources/data/chronosphere/structures/master_clock_entrance.nbt
- [ ] T129 [P] [US3] Create Master Clock structure NBT (mid-layer) in common/src/main/resources/data/chronosphere/structures/master_clock_mid.nbt
- [ ] T130 [P] [US3] Create Master Clock structure NBT (boss room) in common/src/main/resources/data/chronosphere/structures/master_clock_boss_room.nbt
- [ ] T131 [P] [US3] Implement Master Clock structure feature in common/src/main/java/com/chronosphere/worldgen/structures/MasterClockStructure.java
- [ ] T132 [P] [US3] Create Master Clock structure set in common/src/main/resources/data/chronosphere/worldgen/structure_set/master_clock.json
- [ ] T133 [US3] Implement key-based door opening logic in BlockEventHandler.java

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

### Localization

- [ ] T183 [P] Create English localization file in common/src/main/resources/assets/chronosphere/lang/en_us.json
- [ ] T184 [P] Create Japanese localization file in common/src/main/resources/assets/chronosphere/lang/ja_jp.json

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

**Total Tasks**: 194

**Breakdown by Phase**:
- Phase 1 (Setup): 14 tasks
- Phase 2 (Foundational): 20 tasks
- Phase 3 (User Story 1 - P1): 54 tasks
- Phase 4 (User Story 2 - P2): 34 tasks
- Phase 5 (User Story 3 - P3): 49 tasks
- Phase 6 (Polish): 23 tasks

**Breakdown by User Story**:
- User Story 1 (P1): 54 tasks (28% of total)
- User Story 2 (P2): 34 tasks (18% of total)
- User Story 3 (P3): 49 tasks (25% of total)
- Infrastructure (Setup + Foundational + Polish): 57 tasks (29% of total)

**Parallel Opportunities**:
- Phase 1: 7 parallel groups
- Phase 2: 8 parallel groups
- Phase 3 (US1): 22 parallel groups
- Phase 4 (US2): 17 parallel groups
- Phase 5 (US3): 24 parallel groups
- Phase 6: 10 parallel groups

**Independent Test Criteria**:
- User Story 1: Portal creation, dimension travel, portal stabilization, free travel between dimensions
- User Story 2: Desert Clock Tower exploration, time manipulation items, Time Guardian defeat, key acquisition
- User Story 3: Master Clock access, Time Tyrant defeat, ultimate artifacts creation, artifact effects validation

**Suggested MVP Scope**: User Story 1 (Phase 1 + Phase 2 + Phase 3) = 88 tasks
