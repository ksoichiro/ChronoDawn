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
- [x] T236s: Create custom texture for Temporal Phantom (COMPLETED)
  - **Completed**: Created custom model and texture with spectral/ghostly appearance
  - **Model**: TemporalPhantomModel.java (converted from Blockbench export)
  - **Texture**: temporal_phantom.png (blue/purple spectral theme)
  - **Renderer**: Updated to use TemporalPhantomModel with custom layer location
  - **AI improvements**: Safe teleport validation, ranged attack implementation, increased movement speed (0.25 → 0.3)
  - **Commits**: fa1b684, fd001bb, 197cfd5

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
  - **Skipped**: Requires multiplayer setup
- [x] T238p: Balance testing: Time Tyrant fight with vs without Chrono Aegis
  - **Completed**: Tested in survival mode, Chrono Aegis makes Time Tyrant fight significantly easier
- [x] T238q: Full playthrough test
  - **Completed**: Tested as part of T241
- [ ] T238u-x: Documentation updates

#### Remaining High-Priority Tasks
- [x] T239: Guardian Vault structure generation (COMPLETED)
  - Jigsaw structure with entrance (surface) + main hall (underground)
  - StructureStartMixin for waterlogging prevention (removes Aquifer water before placement)
  - ChronosWardenSpawner for boss spawning (Boss Room Door with DoorType: "guardian_vault")
  - Loot table for treasure chests
  - Spawns in chronosphere_plains and chronosphere_forest biomes
- [x] T240: Fix Clarity auto-cleanse feature (implemented using event system in EntityEventHandler.handleChronoAegisClarity)
- [x] T241: Comprehensive testing (boss spawning, Chrono Aegis crafting, Time Tyrant fight)
  - **Completed**: Verified Chrono Aegis makes Time Tyrant fight easier, balanced for player advantage

#### Future Improvements (Low Priority)
- [ ] T242: Improve boss visual diversity (custom models instead of reusing TimeGuardianModel)
- [ ] T243: Add boss-specific sound effects
- [ ] T244: Create advancement system for defeating all 4 bosses

#### Boss Balance Adjustments (US3 - High Priority)

**Purpose**: プレイヤーフィードバックに基づくボス戦闘のバランス調整

- [ ] T245: Clockwork Colossus - Shield Durability Balance
  - **Issue**: 盾の耐久度消費が激しい（フィードバック）
  - **Analysis**: Phase 2のStrength I効果により近接攻撃が約18ダメージ（盾消費19）+ Gear Shot 5秒ごと（盾消費9）で、336耐久度の盾が約20-30回の防御で消耗
  - **Primary Solution**: 盾の耐久度消費軽減システムを実装
    - ClockworkColossusEntityからの攻撃時、盾の耐久度ダメージを30-50%軽減
    - LivingEntity.hurtCurrentlyUsedShield() または ShieldBlockEvent をフックして実装
    - 実装難易度が高い場合は以下のFallback Solutionを採用
  - **Fallback Solution**: Phase 2のバフとGear Shotクールダウンを調整
    - Phase 2のStrength I効果を削除（Speed IIのみ残す、またはノックバック増加など別の効果に変更）
    - Gear Shotクールダウンを5秒→8-10秒に延長
  - **Files**:
    - Primary: common/src/main/java/com/chronosphere/entities/bosses/ClockworkColossusEntity.java
    - Primary (Optional): common/src/main/java/com/chronosphere/events/ShieldDurabilityHandler.java (new)
    - Fallback: common/src/main/java/com/chronosphere/entities/bosses/ClockworkColossusEntity.java (lines 224-228, 90)
  - **Testing**: 盾を持って実際にボス戦を行い、耐久度消費が適切か検証

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

- [ ] T289 [US3] Design Temporal Rift Canyon structure concept (distorted terrain, floating blocks, time crystal veins exposed in walls, visual effects)
- [ ] T290 [P] [US3] Create Temporal Rift Canyon structure NBT in common/src/main/resources/data/chronosphere/structures/temporal_rift_canyon.nbt (canyon with irregular terrain, time crystal ores)
- [ ] T291 [P] [US3] Create Floating Clockwork Ruins structure NBT in common/src/main/resources/data/chronosphere/structures/floating_clockwork_ruins.nbt (floating islands with broken clockwork mechanisms, loot chests)
- [ ] T292 [P] [US3] Create Time Crystal Caverns feature in common/src/main/java/com/chronosphere/worldgen/features/TimeCrystalCavernsFeature.java (underground crystal formations, glowing effects)
- [ ] T293 [P] [US3] Configure custom terrain feature placement in common/src/main/resources/data/chronosphere/worldgen/structure_set/ and placed_feature/ (rare placement, biome-specific)
- [ ] T294 [US3] Test custom terrain features in-game and verify they generate correctly without breaking existing structures

**Note**: Task IDs renumbered from T260-T265 to T289-T294 to avoid conflict with T265 (coal ore) in Basic Resources section

**Checkpoint**: 全User Storyが独立して機能すること

---

### Performance Optimization

- [x] T178 [P] Optimize entity tick rate for time distortion (5-tick interval) in EntityEventHandler.java
  - **Completed**: Changed time distortion processing from every tick to every 5 ticks
  - Added timeDistortionTickCounter to EntityEventHandler
  - Reduces entity processing load by 80% in Chronosphere dimension
  - Slowness effect duration (100 ticks) ensures continuous coverage without gaps
  - Tested in-game on both Fabric and NeoForge loaders
- [x] T179 [P] Implement portal registry caching in PortalRegistry.java
  - **Completed**: Added unmodifiableDimensionPortalCache for dimension portal lookups
  - Caches unmodifiable Set views to avoid defensive copying overhead
  - Cache automatically invalidates on portal register/unregister operations
  - Maintains O(1) lookup performance while reducing allocations
  - Tested in-game on both Fabric and NeoForge loaders
- [ ] T180 [P] Optimize boss AI state machine in TimeGuardianAI.java and TimeTyrantAI.java
- [ ] T181 Profile server performance with Spark profiler
- [ ] T182 Ensure server load increase stays within +10% threshold per success criteria SC-008

### Documentation

- [ ] T185 [P] Update README.md with build instructions for both loaders
- [ ] T186 [P] Create CurseForge mod page description
- [ ] T187 [P] Create Modrinth mod page description
- [ ] T188 [P] Write player guide in docs/player_guide.md
- [ ] T189 [P] Write developer guide in docs/developer_guide.md
- [ ] T295 [P] Configure mod metadata (display name, description, author, icon) in fabric.mod.json and neoforge.mods.toml
- [ ] T296 [P] Add mod icon/logo image (512x512 or 256x256 PNG) to resources
- [x] T297 [P] Add LICENSE file and document dependency licenses (Architectury, Custom Portal API, Patchouli, etc.)

### Playtest Improvements - Dimension Mechanics

**Purpose**: プレイテストで発見されたディメンション機能の改善

- [x] T301 [P] Fix bed sleeping mechanic in Chronosphere (currently sleeping doesn't advance time to morning)
  - **Issue**: Chronosphere has day-night cycle but sleeping in bed doesn't skip to morning
  - **Root Cause**: Custom dimensions don't naturally support time skipping when sleeping (Minecraft limitation)
  - **Solution**: Implemented SleepMixin to manually check if all players in Chronosphere are sleeping and advance time to morning
  - **Implementation Details**:
    - Created SleepMixin.java that hooks into ServerLevel.tick()
    - Checks if ALL players in Chronosphere dimension are sleeping long enough
    - Advances time to morning (1000 ticks) when all players are sleeping
    - Independent from Overworld sleep mechanics (dimension-specific)
    - Respects Chronosphere's variable time cycle system
  - **Files Modified**:
    - common/src/main/java/com/chronosphere/mixin/SleepMixin.java (new)
    - fabric/src/main/resources/chronosphere-fabric.mixins.json
    - neoforge/src/main/resources/chronosphere-neoforge.mixins.json
  - **Tested**: Build successful, all game tests passed (92/92)

### Playtest Improvements - Boss Battle

**Purpose**: プレイテストで発見されたボスバトル関連の改善

- [x] T302 [P] Fix Master Clock boss room door unlock requirement
  - **Issue**: Door opens with only Ancient Clockwork x3, should also require Key to Master Clock
  - **Investigation**: Check BossRoomDoorBlock.java unlock condition logic
  - **Fix**: Update unlock requirement to check for both Ancient Clockwork x3 AND Key to Master Clock
  - **Completed**: Modified BlockEventHandler.java:166 to require both hasRequiredAncientGears() AND hasKeyToMasterClock()
  - **File**: common/src/main/java/com/chronosphere/events/BlockEventHandler.java:166
- [ ] T303 [P] Prevent non-boss mob spawning in Desert Clock Tower boss room
  - **Issue**: Other mobs spawn in Desert Clock Tower boss room during battle
  - **Investigation**: Check structure configuration and spawning rules for desert_clock_tower
  - **Possible solutions**: Add no_mob_spawning flag to boss room area, or implement custom spawning blocker
- [ ] T304 [P] Fix Master Clock boss room ceiling height for Time Tyrant
  - **Issue**: Time Tyrant cannot pass through some low-height areas in Master Clock boss room
  - **Investigation**: Check Master Clock structure NBT and Time Tyrant entity height
  - **Fix**: Increase ceiling height in problematic areas to accommodate Time Tyrant (height = 3.5 blocks)
- [ ] T305 [P] Prevent player camping in Master Clock boss room
  - **Issue**: Players can hide in safe spots and attack Time Tyrant without risk
  - **Investigation**: Identify hiding spots in Master Clock structure
  - **Possible solutions**: Add teleport mechanic to Time Tyrant AI, redesign room layout to remove camping spots, or add area denial attacks

### Playtest Improvements - Exploration

**Purpose**: プレイテストで発見された探索関連の改善

- [ ] T306 [P] Add Ancient Ruins location assistance system
  - **Issue**: Players cannot find Ancient Ruins in some cases
  - **Investigation**: Check Ancient Ruins spawn frequency and distribution
  - **Possible solutions**:
    - Add craftable compass that points to nearest Ancient Ruins
    - Increase structure spawn rate in chronosphere_plains/forest
    - Add visual cues (e.g., beacon beam visible from distance)
    - Add advancement hint system with approximate coordinates

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

