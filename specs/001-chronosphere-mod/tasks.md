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

### Additional Tree Variants (US1 Enhancement - Optional, Low Priority)

**Purpose**: 木のバリエーションを増やして視覚的多様性を向上

**Note**: これは優先度が低く、US1の MVP には必須ではない。他の機能が完成後に実装を検討

- [ ] T088t [P] [US1] (Optional) Design Time Wood color variants (e.g., Dark Time Wood, Ancient Time Wood)
- [ ] T088u [P] [US1] (Optional) Create variant textures and block definitions
- [ ] T088v [P] [US1] (Optional) Create variant tree features and configure placement in different biomes

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
- [ ] T209 [US1] Investigate and fix mob spawn rate issues (hostile mobs too few, friendly/neutral animals too few compared to vanilla Overworld)
- [ ] T210 [P] [US2] Add ranged attack capability to Time Guardian (design projectile, implement attack pattern, configure AI goals)
  - **Note**: Time Guardian (時の番人) is the mini-boss, not Time Keeper (時間の管理者/trading mob)

### Food & Crops (US1 Enhancement - Medium Priority)

**Purpose**: ディメンション内での食料調達手段を追加し、長期滞在やサバイバルプレイを可能にする

**Note**: 現在は時のパン(Time Bread)のみで食料が不足。作物や食料アイテムを追加して多様性を向上

- [ ] T211 [US1] Design time-themed crop concepts (plant types, growth stages, harvest items, growth conditions)
- [ ] T212 [P] [US1] Implement time-themed crop block and item (Temporal Wheat, Time Berry Bush, or similar)
- [ ] T213 [P] [US1] Create crop textures for all growth stages in common/src/main/resources/assets/chronosphere/textures/block/
- [ ] T214 [P] [US1] Add crop worldgen placement (natural generation in specific biomes)
- [ ] T215 [P] [US1] Implement additional food items (cooked variants, crafted recipes, nutritional values)
- [ ] T216 [P] [US1] Add eating effect to Time Bread (e.g., Speed I for 30 seconds, or Regeneration I for 10 seconds)
- [ ] T217 [US1] Test crop growth mechanics and food item effects in-game

### Forgotten Library Enhancements (US2 Enhancement - Medium Priority)

**Purpose**: 図書館構造物の魅力を向上し、探索報酬を追加

- [ ] T218 [P] [US2] Add hidden chest under carpet blocks in Forgotten Library structure NBT (1-2 chests with valuable loot tables)
- [ ] T219 [US2] Test hidden chest placement and ensure carpet blocks can be broken to reveal chests

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

- [ ] T115f [US2] Research appropriate player guidance methods (book items, advancement hints, villager trades, structure frequency)
- [ ] T115g [P] [US2] Create Chronicle of Chronosphere book item (guide book explaining dimension mechanics, structures, and progression)
- [ ] T115h [P] [US2] Add villager trades for structure maps (Ancient Ruins Map, Desert Clock Tower Map, Master Clock Tower Map)
- [ ] T115i [US2] Add advancement system with descriptive hints for key progression milestones (first portal, Ancient Ruins discovery, Time Guardian defeat)
- [ ] T115j [US2] Adjust structure spawn rates to make discovery easier (increase frequency or reduce spacing)
- [ ] T115k [P] [US2] Create Time Compass item (points to nearest key structure, similar to lodestone compass)
- [ ] T115l [US2] Add initial guidance on first dimension entry (chat message, advancement, or book given to player)

### Master Clock Tower & Boss Battle Improvements (US3 Bug Fixes & Enhancements - High Priority)

**Purpose**: Master Clock Towerとボス戦の問題修正と体験向上

- [ ] T223 [P] [US3] Rename "boss_room_door" to time-themed name in localization files (e.g., "Time Tyrant's Chamber Door", "Temporal Sanctum Door")
- [ ] T224 [P] [US3] Fix Clock Tower teleporter block durability issue (make unbreakable or add protection mechanism to prevent breaking)
- [ ] T225 [P] [US3] Fix Clock Tower teleporter destination corruption when re-placed (validate teleport coordinates on placement, warn player if invalid)
- [ ] T226 [P] [US3] Add boss room access control - require Ancient Gears before allowing entry (check inventory on button press, display message if missing)
- [ ] T227 [P] [US3] Fix water disappearing from Master Clock structure NBT (investigate waterlogging state preservation, ensure water blocks save correctly)
- [ ] T228 [P] [US3] Add glowing effect to Time Tyrant entity for visibility (apply Glowing status effect or custom shader/outline rendering)
- [ ] T229 [P] [US3] Fix Time Tyrant teleport suffocation bug (validate teleport destination has 2+ air blocks above, revert position if invalid)
- [ ] T229a [US3] Investigate Time Tyrant buff/debuff behavior (verify if Slowness is incorrectly applied instead of intended buff, check status effect logic)
- [ ] T230 [US3] Test all Master Clock Tower and boss battle fixes in-game

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

**Purpose**: ボスの数を増やし、戦闘の多様性を向上

**Note**: 現在Time GuardianとTime Tyrantのみ。隠しボスを含めたバリエーションを追加

- [ ] T234 [US3] Design additional boss concepts (hidden boss mechanics, lore, unique abilities, drops, spawn conditions)
- [ ] T235 [P] [US3] Implement hidden boss entity (e.g., Temporal Devourer, Master of Lost Time)
- [ ] T236 [P] [US3] Create boss textures and models in common/src/main/resources/assets/chronosphere/textures/entity/
- [ ] T237 [P] [US3] Design hidden boss arena structure or spawn conditions (rare structure, summoning item, secret room)
- [ ] T238 [US3] Test additional boss spawning, mechanics, and loot drops in-game

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

