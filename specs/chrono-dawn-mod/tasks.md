# Tasks: Chrono Dawn Mod - Time-Manipulation Themed Minecraft Mod

**Branch**: `main`
**Input**: Design documents from `/specs/chrono-dawn-mod/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Architecture**: Architectury Multi-Loader Framework (common / fabric / neoforge)
**Tech Stack**: Java 21, Minecraft 1.21.1, NeoForge 21.1.x + Fabric

**Tests**: Test tasks are explicitly requested (see quickstart.md). Uses JUnit 5 + GameTest Framework.

**Organization**: Tasks are grouped by User Story, making each story independently implementable and testable.

---

### Time Cycle Configuration (Phase 2.5)

**Purpose**: Set fixed time to create unique atmosphere similar to other dimension mods

- [X] T034j [P] Research day-night cycle vs fixed time design (Decision 7)
- [X] T034k [P] Implement fixed time (6000 ticks = noon) and End sky effects in dimension_type/chronodawn.json
- [X] T034l [P] Test dimension visual appearance with fixed time and grey sky in-game
- [X] T034m [P] Evaluate time/sky settings and document adjustments needed (fixed_time: 4000-8000, effects: overworld/end/custom)
- [X] T034n [P] Research custom DimensionSpecialEffects for precise sky color control (Completed: 2025-12-17, see research.md)
- [X] T034p [P] Implement SkyColorMixin for Time Tyrant defeat sky color change (0x909090 → 0x5588DD) (Completed: 2025-12-21)
- [ ] T034o [P] (Future) Design sky color unlock mechanic tied to boss defeat (Option D element)

**Current Settings**: `fixed_time: 6000` (noon), `effects: minecraft:overworld`, biome `sky_color: 9474192` (grey)

**Note**: Fixed time value (4000-8000 = bright time period) and sky color are adjustable after playtesting

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

#### T034p: Sky Color Change on Time Tyrant Defeat

**Purpose**: Implement dynamic sky color change when Time Tyrant is defeated, making the sky slightly brighter as the first step of sky color progression.

**Implementation Approach**: Client-side Mixin (similar to FogRendererMixin)

**Detailed Steps**:
1. **Create SkyColorMixin class**
   - File: `common/src/main/java/com/chronodawn/mixin/client/SkyColorMixin.java`
   - Target: `net.minecraft.client.renderer.LevelRenderer`
   - Method to hook: `renderSky()` or related sky rendering method
   - Pattern: Similar to FogRendererMixin (biome detection + RenderSystem calls)

2. **Implement boss defeat detection**
   - Check Time Tyrant advancement: `chronodawn:time_tyrant_defeated`
   - Use AdvancementManager on client-side to query completion status
   - Cache result to avoid repeated checks

3. **Apply sky color override**
   - Default color: `0x909090` (144, 144, 144) - current grey
   - After Time Tyrant defeat: `0x5588DD` (85, 136, 221) - bright blue sky
   - Apply color only in Chrono Dawn dimension
   - Direct color replacement (no interpolation needed)

4. **Update Mixin configuration files**
   - Add SkyColorMixin to `chronodawn.mixins.json`
   - Update `chronodawn-fabric.mixins.json` (with refMap)
   - Update `chronodawn-neoforge.mixins.json` (without refMap)

5. **Testing**
   - Test without Time Tyrant defeat (should show grey sky: 0x909090)
   - Test with Time Tyrant defeat (should show bright blue sky: 0x5588DD)
   - Test dimension transition (color should reset when leaving/entering)
   - Test multiplayer sync (all players should see same sky color)

**Technical References**:
- Similar implementation: `FogRendererMixin.java` (lines 28-82)
- Advancement check: Use `ClientAdvancements` or player NBT data
- Research document: See `research.md` section "T034n: DimensionSpecialEffects Research"

**Success Criteria**:
- Sky color remains grey (0x909090) by default
- Sky color becomes bright blue (0x5588DD) after Time Tyrant defeat
- Color change persists across game sessions
- No performance impact on rendering
- Works on both Fabric and NeoForge

**Estimated Effort**: 2-3 hours

**Future Enhancement**: This task is the first step towards full sky color progression (T034o) where defeating all bosses gradually restores the sky to vanilla blue (0x78A5FF).

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

**Purpose**: Add decorative terrain elements to enhance visual diversity of exploration

**Note**: Implement as post-MVP improvement. Recommended to implement after vegetation and mob spawning

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

**Purpose**: Add biome-specific small structures to improve distinctiveness

**Note**: Low priority. Consider implementation in Phase 6 (Polish) stage

- [ ] T088aj [P] [US1] (Optional) Design small rock circle feature for chronodawn_plains (Stonehenge-like miniature)
- [ ] T088ak [P] [US1] (Optional) Design mushroom circle feature for chronodawn_forest (fairy ring theme)
- [ ] T088al [P] [US1] (Optional) Design coral-like time structure for chronodawn_ocean (temporal reef theme)
- [ ] T088am [P] [US1] (Optional) Implement landmark features and configure rare placement
- [ ] T088an [US1] (Optional) Test landmark generation and ensure they don't interfere with main structures

### Ambient Sounds & Particles (US1 Enhancement - Optional)

**Purpose**: Enhance immersion with ambient sounds and particles

**Note**: Lowest priority. Consider implementation in Phase 6 (Polish) stage

- [ ] T088ao [P] [US1] (Optional) Research custom biome ambient sounds (wind, waves, cave echoes)
- [ ] T088ap [P] [US1] (Optional) Create custom sound files or select vanilla sounds for each biome
- [ ] T088aq [P] [US1] (Optional) Configure ambient sounds in biome JSONs (mood_sound, additions_sound)
- [ ] T088ar [P] [US1] (Optional) Design time distortion particle effects (subtle sparkles, clock hands)
- [ ] T088as [P] [US1] (Optional) Implement particle spawning logic for biomes (client-side)
- [ ] T088at [US1] (Optional) Test ambient sounds and particles in-game for immersion quality

### Additional Boss Enemies (US3 Enhancement - Medium Priority)

**Purpose**: Implement 4 additional mid-bosses and Chrono Aegis system

**Implementation Status**: Boss entities, Chrono Aegis, Guardian Vault, and Clockwork Depths complete. Temporal Phantom and Entropy Keeper structures pending

**Reference**: See research.md "Additional Bosses Implementation Plan (T234-T238)"

#### T234: Chronos Warden (Guardian of Chronos) - COMPLETED
- [x] T234a-i: Entity, item, renderer, texture, translations implemented
- [x] T234j-o: Guardian Vault structure generation (COMPLETED - see T239)

#### T235: Clockwork Colossus (Mechanical Giant) - COMPLETED
- [x] T235a-l: Entity, Gear Projectile, item, renderer, translations implemented
- [x] T235m-r: Clockwork Depths structure generation (COMPLETED)
  - Multi-level Jigsaw structure: tower (surface) → gearshaft → engine_room → archive_vault
  - ClockworkColossusSpawner for proximity-based boss spawning (Clockwork Block markers)
  - Template pools: tower_pool, gearshaft_pool, engine_room_pool, archive_vault_pool
  - Spawns in chronodawn_desert and chronodawn_mountain biomes
  - Boss spawns when player approaches within 20 blocks of Clockwork Block markers

#### T236: Temporal Phantom (Phantom of Time) - PARTIALLY COMPLETED
- [x] T236a-l: Entity, item, renderer, texture, translations implemented
- [x] T236m-r: Phantom Catacombs structure generation (COMPLETED)
  - Jigsaw-based underground maze structure with entrance, corridors, maze rooms, dead-ends
  - Boss room programmatic placement with collision detection and 2-stage fallback system
  - TemporalPhantomSpawner for proximity-based boss spawning (player enters boss_room)
  - Complete waterlogging prevention system (DecorativeWaterFluid, CopyFluidLevelProcessor, StructureStartMixin)
  - Template pools: entrance_pool, corridor_pool, maze_room_pool, room_7_pool, boss_room_pool, terminator_pool
  - Spawns in chronodawn_forest and chronodawn_swamp biomes
  - Boss spawns when player enters boss_room (21x21x9 area, 1-second interval check)
  - Terrain adaptation: "none" (prevents surface terrain deletion)
- [x] T236s: Create custom texture for Temporal Phantom (COMPLETED)
  - **Completed**: Created custom model and texture with spectral/ghostly appearance
  - **Model**: TemporalPhantomModel.java (converted from Blockbench export)
  - **Texture**: temporal_phantom.png (blue/purple spectral theme)
  - **Renderer**: Updated to use TemporalPhantomModel with custom layer location
  - **AI improvements**: Safe teleport validation, ranged attack implementation, increased movement speed (0.25 → 0.3)
  - **Commits**: fa1b684, fd001bb, 197cfd5

#### T237: Entropy Keeper (Keeper of Entropy) - COMPLETED
- [x] T237a-m: Entity, item, renderer, texture, translations implemented
- [x] T237n-r: Entropy Crypt structure generation (COMPLETED)
  - Jigsaw structure: entrance (surface) → stairs → main (Boss Chamber + Vault)
  - EntropyCryptTrapdoorBlock triggers boss spawn when player attempts to open
  - Custom BlockSetType allows hand interaction with iron sounds
  - ACTIVATED property tracks boss spawn state
  - Template pools: entrance_pool, stairs_pool, main_pool
  - Spawns in chronodawn_swamp and chronodawn_forest biomes
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
  - Spawns in chronodawn_plains and chronodawn_forest biomes
- [x] T240: Fix Clarity auto-cleanse feature (implemented using event system in EntityEventHandler.handleChronoAegisClarity)
- [x] T241: Comprehensive testing (boss spawning, Chrono Aegis crafting, Time Tyrant fight)
  - **Completed**: Verified Chrono Aegis makes Time Tyrant fight easier, balanced for player advantage

#### Future Improvements (Low Priority)
- [x] T242: Improve boss visual diversity (custom models instead of reusing TimeGuardianModel)
  - **Completed**: All 4 mid-bosses have custom models implemented
  - ChronosWardenModel.java, ClockworkColossusModel.java, TemporalPhantomModel.java, EntropyKeeperModel.java
- [ ] T243: Add boss-specific sound effects
- [x] T244: Create advancement system for defeating all 4 bosses
  - **Completed**: chrono_aegis_obtained.json advancement requires all 4 boss drops
  - Criteria: Guardian Stone + Phantom Essence + Colossus Gear + Entropy Core
  - Frame: Challenge, Rewards: 100 XP

#### Boss Balance Adjustments (US3 - High Priority)

**Purpose**: Adjust boss combat balance based on player feedback

- [x] T245: Clockwork Colossus - Shield Durability Balance
  - **Issue**: Shield durability consumption is excessive (feedback)
  - **Analysis**: Phase 2 Strength I effect causes melee attacks of ~18 damage (shield consumption 19) + Gear Shot every 5 seconds (shield consumption 9), causing 336 durability shield to deplete in ~20-30 blocks
  - **Solution Implemented**: Fallback Solution (Phase 2 buff and Gear Shot adjustment)
    - Removed Phase 2 Strength I effect (changed to Speed II only)
    - Extended Gear Shot cooldown from 5 seconds → 8 seconds
  - **Result**: Shield durability consumption significantly reduced
    - Melee attacks: Base damage 12 only (no Strength I effect)
    - Ranged attacks: Extended to 8 second intervals (60% frequency reduction)
  - **Files Modified**:
    - common/src/main/java/com/chronodawn/entities/bosses/ClockworkColossusEntity.java (lines 52, 55, 90, 213, 218-223)
  - **Testing**: Build successful, all 92 tests passed

### Future Boss Battle Enhancements (US3 - Phase 7+)

**Purpose**: Master Clock expansion and balance adjustments after playtesting

**Dependencies**:
- T171j-k: Expand Master Clock dungeon from current 4 rooms to large-scale
- T171l-n: Implement after game balance confirmation through playtesting

- [ ] T171j [US3] Design large-scale Master Clock dungeon layout (20+ rooms, multi-floor, complex maze structure, environmental hazards)
- [ ] T171k [US3] Implement large-scale Master Clock dungeon generation system in common/src/main/java/com/chronodawn/worldgen/structures/MasterClockLargeGenerator.java
- [ ] T171l [US3] Implement Temporal Anchor environmental mechanic in common/src/main/java/com/chronodawn/blocks/TemporalAnchorBlock.java (3 anchors in dungeon, destroy to weaken Time Tyrant: teleport frequency, Time Acceleration, AoE range)
- [ ] T171m [US3] Adjust Time Tyrant base stats based on playtesting (HP 500→400, Attack 18→15, Defense 15→12) if needed
- [ ] T171n [US3] Implement Grave Marker death recovery system in common/src/main/java/com/chronodawn/mechanics/GraveMarkerHandler.java (death marker at boss room entrance, safe item recovery from outside)

### Custom Terrain Features (US3 Enhancement - Medium Priority)

**Purpose**: Add unique time-themed terrain to enhance terrain generation distinctiveness

**Note**: Currently uses the same terrain generation algorithm as Overworld. Add special terrain along time theme to increase exploration interest

- [ ] T289 [US3] Design Temporal Rift Canyon structure concept (distorted terrain, floating blocks, time crystal veins exposed in walls, visual effects)
- [ ] T290 [P] [US3] Create Temporal Rift Canyon structure NBT in common/src/main/resources/data/chronodawn/structures/temporal_rift_canyon.nbt (canyon with irregular terrain, time crystal ores)
- [ ] T291 [P] [US3] Create Floating Clockwork Ruins structure NBT in common/src/main/resources/data/chronodawn/structures/floating_clockwork_ruins.nbt (floating islands with broken clockwork mechanisms, loot chests)
- [ ] T292 [P] [US3] Create Time Crystal Caverns feature in common/src/main/java/com/chronodawn/worldgen/features/TimeCrystalCavernsFeature.java (underground crystal formations, glowing effects)
- [ ] T293 [P] [US3] Configure custom terrain feature placement in common/src/main/resources/data/chronodawn/worldgen/structure_set/ and placed_feature/ (rare placement, biome-specific)
- [ ] T294 [US3] Test custom terrain features in-game and verify they generate correctly without breaking existing structures

**Note**: Task IDs renumbered from T260-T265 to T289-T294 to avoid conflict with T265 (coal ore) in Basic Resources section

**Checkpoint**: All User Stories function independently

---

### Performance Optimization

- [x] T178 [P] Optimize entity tick rate for time distortion (5-tick interval) in EntityEventHandler.java
  - **Completed**: Changed time distortion processing from every tick to every 5 ticks
  - Added timeDistortionTickCounter to EntityEventHandler
  - Reduces entity processing load by 80% in Chrono Dawn dimension
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

- [x] T185 [P] Update README.md with build instructions for both loaders
- [x] T186 [P] Create CurseForge mod page description (docs/curseforge_description.md - includes Chronicle UI, unified with Modrinth)
- [x] T187 [P] Create Modrinth mod page description (docs/modrinth_description.md - includes Chronicle UI, unified with CurseForge)
- [x] T188 [P] Write player guide in docs/player_guide.md
- [x] T189 [P] Write developer guide in docs/developer_guide.md - Updated with Chronicle UI and LGPL-3.0 (2025-12-27)
- [x] T295 [P] Configure mod metadata (license="LGPL-3.0", authors="ksoichiro" in fabric.mod.json and neoforge.mods.toml) - Completed 2025-12-27
- [x] T296 [P] Add mod icon/logo image (512x512 PNG) to resources - Added to Fabric and NeoForge (2025-12-27)
- [x] T297 [P] Add LICENSE file and document dependency licenses (Architectury, Custom Portal API, Patchouli, etc.)

### OSS Internationalization (English Translation)

**Purpose**: Complete English translation of all documentation for international open-source contributors

**Background**: After LGPL-3.0 license adoption, the project is now fully open source. To maximize international contribution, all internal documentation should be available in English.

**Priority**: High (for OSS adoption)

**Phase 1: Essential Documentation** (Immediate):

- [x] T707 [P] Create CONTRIBUTING.md in English - Completed 2025-12-27
  - Write contribution guidelines (setup, workflow, code style, PR process)
  - Include link to developer_guide.md for technical details
  - Document LGPL-3.0 license compliance requirements
  - Estimated effort: 30-40 minutes
  - Status: Required for OSS launch

- [x] T708 [P] Translate specs/chrono-dawn-mod/spec.md to English - Completed 2025-12-27
  - Translate design specification (156 lines of Japanese content)
  - Essential for new contributors to understand feature design
  - Maintain existing structure and content
  - Estimated effort: 1-1.5 hours
  - Status: High priority for feature understanding

- [x] T709 [P] Translate specs/chrono-dawn-mod/tasks.md to English - Completed 2025-12-27
  - Translate task descriptions and notes (49 lines of Japanese content)
  - Important for project progress visibility
  - Keep task IDs and completion status unchanged
  - Estimated effort: 30 minutes
  - Status: High priority for collaboration

**Phase 2: Historical Documentation** (Lower priority, can be done later):

- [x] T710 [Documentation] Translate docs/implementation_history.md to English (2025-12-27)
  - Translated implementation history (466 lines)
  - Provides context for design decisions (Ancient Ruins placement trials, Additional Bosses, Desert Clock Tower)
  - Estimated effort: 1.5-2 hours
  - Status: Medium priority (historical reference)

- [x] T711 [Documentation] Translate docs/initial_design.md to English (2025-12-27)
  - Translated initial design document (70 lines)
  - Historical value for understanding project evolution (original storyline, dimension mechanics, artifacts concept)
  - Estimated effort: 30 minutes
  - Status: Low priority (historical reference)

**Estimated Total Effort**:
- Phase 1 (Essential): 2-2.5 hours
- Phase 2 (Historical): 2-2.5 hours
- Complete: 4-5 hours

**Success Criteria**:
- All essential documentation (spec.md, tasks.md, CONTRIBUTING.md) available in English
- New international contributors can understand project structure and contribute
- No Japanese content in critical development files

---

### Playtest Improvements - Dimension Mechanics

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
    - **NeoForge**: Custom Portal API Reforged v1.2.2 lacks `setPortalSearchYRange` method
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

### Playtest Improvements - Boss Battle

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
- [ ] T309 [P] Fix Phantom Catacombs structure search freezing
  - **Issue**: When using structure search for Phantom Catacombs, the world freezes, especially in multiplayer where other players get disconnected and boss room placement never completes
  - **Investigation**: Check Phantom Catacombs structure generation logic and boss room placement algorithm
  - **Possible causes**:
    - Infinite loop or deadlock in boss room placement collision detection
    - Excessive chunk loading during structure search
    - Synchronous boss room placement blocking main thread
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

- [ ] T715 [P] Add spawn eggs for all boss entities (command-only, not in creative tab)
  - **Requirement**: Boss spawn eggs should be available for testing and debugging via `/give` command, but not displayed in creative inventory tab (following vanilla behavior for Ender Dragon and Wither)
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Implementation**:
    - Create spawn egg items for all boss entities using DeferredSpawnEggItem
    - Add spawn eggs to ModItems.java registry
    - Call initializeSpawnEgg() in registerSpawnEggs() method
    - **Do NOT add to creative tab** (do not call output.accept() in addItemsToCreativeTab())
  - **Boss Entities to Add**:
    - Time Guardian (TimeGuardianEntity) - Suggest colors: 0xFFD700 (gold) background, 0x4169E1 (royal blue) spots
    - Time Tyrant (TimeTyrantEntity) - Suggest colors: 0x8B0000 (dark red) background, 0xFFD700 (gold) spots
    - Chronos Warden (ChronosWardenEntity) - Suggest colors: 0x2F4F4F (dark slate gray) background, 0x00CED1 (dark turquoise) spots
    - Clockwork Colossus (ClockworkColossusEntity) - Suggest colors: 0x708090 (slate gray) background, 0xFF8C00 (dark orange) spots
    - Entropy Keeper (EntropyKeeperEntity) - Suggest colors: 0x191970 (midnight blue) background, 0x9370DB (medium purple) spots
    - Temporal Phantom (TemporalPhantomEntity) - Suggest colors: 0x2F2F2F (dark gray) background, 0xE0E0E0 (light gray) spots
  - **Reference Implementation**:
    - Existing spawn eggs: `ModItems.java:1270-1308` (TEMPORAL_WRAITH_SPAWN_EGG, etc.)
    - Creative tab registration: `ModItems.java:1581-1588` (DO NOT add boss eggs here)
  - **Files to Modify**:
    - `common/src/main/java/com/chronodawn/registry/ModItems.java`
  - **Expected Behavior**: Boss spawn eggs can be obtained via `/give @s chronodawn:time_guardian_spawn_egg` but do not appear in creative inventory

- [ ] T716 [P] Fix duplicate recipe conflict between Enhanced Clockstone Pickaxe and Spatially Linked Pickaxe
  - **Issue**: Enhanced Clockstone Pickaxe and Spatially Linked Pickaxe have identical recipes (Enhanced Clockstone x3 + Stick x2), causing recipe conflict where only one can be crafted
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Current Recipes**:
    - Enhanced Clockstone Pickaxe: `EEE / _S_ / _S_` (E=Enhanced Clockstone, S=Stick)
    - Spatially Linked Pickaxe: `EEE / _S_ / _S_` (E=Enhanced Clockstone, S=Stick) ← Same pattern!
  - **Design Spec Reference**: `data-model.md:252-266` specifies Spatially Linked Pickaxe recipe should be:
    - Stasis Core Fragment x2 + Enhanced Clockstone x3 + Diamond Pickaxe x1
  - **Problem**: Stasis Core Fragment is not yet implemented
  - **Proposed Solution** (choose one):
    1. **Option A** (Recommended): Change Spatially Linked Pickaxe recipe to use Time Crystal instead of Stasis Core Fragment until it's implemented
       - Pattern: `TCT / EPE / _T_` (T=Time Crystal, C=Clockwork Block or Enhanced Clockstone, E=Enhanced Clockstone, P=Diamond Pickaxe)
    2. **Option B**: Use smithing table upgrade: Enhanced Clockstone Pickaxe + Time Crystal → Spatially Linked Pickaxe
    3. **Option C**: Implement Stasis Core Fragment first, then use the spec-compliant recipe
  - **Files to Modify**:
    - `common/src/main/resources/data/chronodawn/recipe/spatially_linked_pickaxe.json`
    - `common/src/main/resources/data/chronodawn/advancement/recipes/misc/spatially_linked_pickaxe.json` (if recipe structure changes)
  - **Expected Behavior**: Both pickaxes can be crafted with distinct recipes, no recipe conflict

### Playtest Improvements - Exploration

**Purpose**: Exploration improvements discovered through playtesting

- [ ] T306 [P] Add Ancient Ruins location assistance system
  - **Issue**: Players cannot find Ancient Ruins in some cases
  - **Investigation**: Check Ancient Ruins spawn frequency and distribution
  - **Possible solutions**:
    - Add craftable compass that points to nearest Ancient Ruins
    - Increase structure spawn rate in chronodawn_plains/forest
    - Add visual cues (e.g., beacon beam visible from distance)
    - Add advancement hint system with approximate coordinates

- [x] T713 [P] Add iron ore generation to Chrono Dawn dimension
  - **Issue**: Iron ore does not generate in Chrono Dawn dimension, preventing crafting of buckets, shields, and other essential items
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Implementation**:
    - Create `ore_iron.json` configured_feature (similar to Time Crystal ore settings)
    - Create `ore_iron.json` placed_feature with generation frequency similar to Time Crystal (count: 3, height: Y=0-48)
    - Add `chronodawn:ore_iron` to all biome feature lists (step 6: underground_ores)
    - Reference: Time Crystal ore settings at `worldgen/configured_feature/ore_time_crystal.json` and `worldgen/placed_feature/ore_time_crystal.json`
  - **Expected Behavior**: Iron ore generates at similar frequency to Time Crystal ore, allowing players to obtain iron without returning to Overworld

- [ ] T717 [P] Reduce Clockstone ore generation frequency
  - **Issue**: Clockstone ore generates too frequently, making it too abundant and reducing resource gathering challenge
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Current Settings**:
    - Size: 9 (vein size)
    - Count: 16 (spawn attempts per chunk)
    - Height: Y=-16 to 80
  - **Proposed Adjustment**:
    - Reduce count from 16 to 8-10 (about 50% reduction)
    - Keep size: 9 and height range unchanged
    - This aligns Clockstone frequency closer to iron ore in vanilla (which has count ~10-12 per major height band)
  - **Files to Modify**:
    - `common/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_clockstone.json` (change count value)
  - **Expected Behavior**: Clockstone ore becomes moderately rare, maintaining its value as a tier 1 crafting material without being overly abundant

- [ ] T718 [P] Extend Chrono Aegis buff to all nearby players instead of only the user
  - **Issue**: Chrono Aegis buff currently applies only to the player who uses the item, making multiplayer boss fights unbalanced (only one player gets protection)
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Current Implementation**: `ChronoAegisItem.use()` calls `player.addEffect()` only on the user (line 70-77)
  - **Proposed Implementation**:
    - Apply buff to all players within a radius (suggest 32 blocks, similar to beacon range)
    - Use `level.players()` to get all players in dimension, filter by distance from user
    - Apply same 10-minute Chrono Aegis buff to each player in range
    - Display message to each affected player indicating they received the buff
    - Play sound effect at user's location (visible/audible to all nearby players)
  - **Design Considerations**:
    - Item still consumed from user's inventory (single use)
    - All players in range receive full 10-minute buff
    - Range should be generous to cover typical boss arena (32-64 blocks recommended)
    - Consider cross-dimension check: only apply to players in same dimension as user
  - **Files to Modify**:
    - `common/src/main/java/com/chronodawn/items/ChronoAegisItem.java` (modify use() method)
  - **Reference Implementation**:
    - Vanilla beacon: applies effects to all players in range
    - Current code: `ChronoAegisItem.java:52-102` (use() method)
  - **Expected Behavior**: When a player uses Chrono Aegis, all nearby players (within 32 blocks) receive the 10-minute buff, making multiplayer boss fights more balanced

- [ ] T719 [P] Add axe stripping functionality for Time Wood logs (bark removal with right-click)
  - **Issue**: Time Wood logs (Time Wood Log, Dark Time Wood Log, Ancient Time Wood Log) cannot be stripped with axe right-click, unlike vanilla logs
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Current State**: Stripped variants do not exist
  - **Implementation**:
    1. **Create Stripped Log Blocks** (3 variants):
       - Stripped Time Wood Log
       - Stripped Dark Time Wood Log
       - Stripped Ancient Time Wood Log
       - Use `RotatedPillarBlock` class (same as regular logs)
       - Copy block properties from corresponding regular logs
    2. **Create Stripped Wood Blocks** (3 variants, optional but recommended for parity):
       - Stripped Time Wood (all-bark block, if regular Time Wood exists)
       - Stripped Dark Time Wood
       - Stripped Ancient Time Wood
    3. **Register Strippable Mappings**:
       - Use `UseOnContext` event to register axe stripping behavior
       - Map: Time Wood Log → Stripped Time Wood Log
       - Map: Dark Time Wood Log → Stripped Dark Time Wood Log
       - Map: Ancient Time Wood Log → Stripped Ancient Time Wood Log
       - Reference: Vanilla uses `AxeItem.STRIPPABLES` map (access via mixin or event)
    4. **Add Block/Item Registration**:
       - Register stripped blocks in `ModBlocks.java`
       - Register stripped block items in `ModItems.java`
       - Add to creative tab
    5. **Add Loot Tables**:
       - Create loot tables for stripped logs (drop themselves)
       - Path: `data/chronodawn/loot_table/blocks/stripped_time_wood_log.json`, etc.
    6. **Add Textures**:
       - Create stripped log textures (top and side)
       - Follow vanilla stripped log pattern (lighter, no bark texture)
  - **Files to Modify/Create**:
    - `common/src/main/java/com/chronodawn/registry/ModBlocks.java` (add stripped blocks)
    - `common/src/main/java/com/chronodawn/registry/ModItems.java` (add stripped block items)
    - `common/src/main/java/com/chronodawn/events/BlockEventHandler.java` (add stripping logic)
    - `common/src/main/resources/assets/chronodawn/blockstates/stripped_*.json` (3 files)
    - `common/src/main/resources/assets/chronodawn/models/block/stripped_*.json` (3 files)
    - `common/src/main/resources/assets/chronodawn/textures/block/stripped_*_log*.png` (6 textures)
    - `common/src/main/resources/data/chronodawn/loot_table/blocks/stripped_*.json` (3 files)
  - **Reference Implementation**:
    - Vanilla: Oak Log → Stripped Oak Log (right-click with any axe)
    - Architectury example: Use `UseOnContext` event to intercept axe usage
  - **Expected Behavior**: Right-clicking Time Wood logs with any axe converts them to stripped variants, matching vanilla behavior

- [ ] T720 [P] Fix NeoForge block placement item loss in protected boss rooms
  - **Issue**: On NeoForge, when players try to place blocks in protected boss rooms, the block placement is cancelled but the item is consumed from inventory, causing item loss
  - **Feedback Source**: Playtest feedback (2026-01-03)
  - **Root Cause**: NeoForge's `BlockEvent.EntityPlaceEvent` fires AFTER the item is consumed, so cancelling the event doesn't restore the item
  - **Current Implementation**:
    - NeoForge: Uses `BlockEvent.EntityPlaceEvent` which is too late (line 80-119 in `neoforge/event/BlockProtectionEventHandler.java`)
    - Fabric: Uses `UseBlockCallback.EVENT` which fires BEFORE placement, correctly preventing item consumption (line 71-114 in `fabric/event/BlockProtectionEventHandler.java`)
  - **Proposed Solution**:
    - Replace `BlockEvent.EntityPlaceEvent` with `PlayerInteractEvent.RightClickBlock` event in NeoForge implementation
    - Check protection BEFORE block placement occurs
    - Return `InteractionResult.FAIL` to cancel placement without consuming item
    - Pattern similar to Fabric's UseBlockCallback approach
  - **Implementation Steps**:
    1. Remove `@SubscribeEvent onBlockPlace(BlockEvent.EntityPlaceEvent)` method
    2. Add new `@SubscribeEvent onRightClickBlock(PlayerInteractEvent.RightClickBlock)` method
    3. Check if player is holding a BlockItem (similar to Fabric line 80)
    4. Calculate placement position from hit result
    5. Check protection (boss room and permanent)
    6. Return `InteractionResult.FAIL` if protected (cancels action without consuming item)
    7. Return `InteractionResult.PASS` if not protected (allows normal placement)
  - **Files to Modify**:
    - `neoforge/src/main/java/com/chronodawn/neoforge/event/BlockProtectionEventHandler.java`
  - **Reference Implementation**:
    - Fabric implementation (correct): `fabric/event/BlockProtectionEventHandler.java:71-114`
    - NeoForge PlayerInteractEvent.RightClickBlock documentation
  - **Testing**:
    - Verify block placement is prevented in protected areas WITHOUT consuming items
    - Verify warning message still displays
    - Verify creative mode bypass still works
    - Test with various block types (normal blocks, slabs, stairs, etc.)
  - **Expected Behavior**: When players attempt to place blocks in protected boss rooms on NeoForge, the placement is cancelled AND the item remains in inventory (matching Fabric behavior)

### Playtest Improvements - Guidebook Distribution

**Purpose**: Fix guidebook distribution bugs discovered through playtesting

- [x] T712 [P] Fix Chronicle Book not being distributed when entering Chrono Dawn dimension
  - **Issue**: Chronicle Book was not distributed on dimension entry on BOTH Fabric and NeoForge
  - **Root Cause**: giveChronicleBook() call was removed during Patchouli to Chronicle GUI migration (commit 053897a)
  - **Fix**: Restored giveChronicleBook() and hasChronicleBook() methods in PlayerEventHandler.java (common module)
  - **Verified**: Both Fabric and NeoForge now distribute Chronicle Book on dimension entry ✓
  - **Priority**: High (affects player experience on both platforms)

### Final Validation

- [ ] T190 Run full test suite for both loaders using ./gradlew test
- [ ] T191 Validate quickstart.md manual testing checklist
- [ ] T192 Build final JARs for distribution using ./gradlew build
- [ ] T193 Test Fabric JAR in production Minecraft 1.21.1 + Fabric environment
- [ ] T194 Test NeoForge JAR in production Minecraft 1.21.1 + NeoForge environment

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] labels map tasks to specific User Stories (ensuring traceability)
- Each User Story should be independently completable and testable
- Verify tests FAIL before implementation
- Commit after each task or logical group
- Can stop at any Checkpoint to verify story independently
- Avoid: ambiguous tasks, conflicts in same file, inter-story dependencies breaking independence
- Architectury-specific note: Implement 80% of logic in common module, limit loader-specific implementation to 20%
- Entity rendering: Fabric uses standard API, NeoForge uses manual event registration (addresses Issue #641)

---


### Biome System Improvements

**Purpose**: Improve visual quality of biome boundaries (future improvement task)

- [ ] T307 [Research] Investigate snowy biome boundary smoothing solutions
  - **Issue**: Snowy biome boundaries with other biomes (especially dark_forest) are too sharp/linear
  - **Root Cause**: 
    - Biome source temperature ranges meet exactly at -0.2 without overlap
    - `freeze_top_layer` feature depends on biome temperature, causing linear boundaries
  - **Attempted Solutions** (2025-12-19):
    1. Placement modifiers (`in_square`) → Chunk access error
    2. Biome temperature adjustment (0.0 → 0.10) → Limited effect
    3. Offset adjustment (0.0 → 0.3) → Minimal expected effect
  - **Recommended Solutions** (in priority order):
    1. Add transition biomes (Snowy Plains, Snowy Forest, etc.) with intermediate temperatures
    2. Implement custom noise settings for more complex biome distribution
    3. Implement custom snow placement Feature with noise-based logic (last resort)
  - **Reference**: specs/chrono-dawn-mod/research.md → "Snowy Biome Boundary Smoothing (2025-12-19)"
  - **Dependencies**: None (future enhancement)
  - **Priority**: Low (cosmetic improvement)

---

### Mod Rebranding

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

---

### Multi-Version Support (Infrastructure)

**Purpose**: Support Minecraft 1.20.6 and 1.21.1 from a single codebase using custom Gradle scripts and abstraction layer

**Strategy**: Gradle-based multi-version setup with version-specific directories and compatibility layer (no external preprocessors)

**Documentation**: See `docs/multiversion_migration_plan.md` for detailed implementation plan

**Target Versions**: Minecraft 1.20.6 (pack_format: 41, NBT-based APIs) + 1.21.1 (pack_format: 48, Component-based APIs)

**Estimated Duration**: 28-39 days (1-1.5 months) across 6 phases

#### Phase 1: Data Pack Integration (2-3 days)

**Goal**: Separate version-specific Data Packs and switch at build time with Gradle

- [ ] T401 [Infrastructure] Create version property files
  - Create `props/` directory
  - Create `props/1.20.6.properties` with minecraft_version, pack_format, architectury_api_version, etc.
  - Create `props/1.21.1.properties` with corresponding values
  - **Priority**: High
  - **Risk**: Low

- [ ] T402 [Infrastructure] Add version switching logic to root build.gradle
  - Implement `loadVersionProperties()` function
  - Add command-line property `target_mc_version` support
  - Propagate properties to subprojects
  - **Priority**: High
  - **Risk**: Low

- [ ] T403 [Infrastructure] Create version-specific resource directories
  - Create `common/src/main/resources-1.21.1/` (copy current data)
  - Create `common/src/main/resources-1.20.6/` with plural folder names
  - Rename folders: `advancement/` → `advancements/`, `loot_table/` → `loot_tables/`, `recipe/` → `recipes/`, `tags/item/` → `tags/items/`
  - Update `pack_format` to 41 in 1.20.6 pack.mcmeta
  - **Priority**: High
  - **Risk**: Low
  - **Files**: ~320 data pack files

- [ ] T404 [Infrastructure] Configure Gradle sourceSets for resources
  - Add `sourceSets.main.resources` configuration to `common/build.gradle`
  - Implement version-specific resource directory selection
  - Add `processResources` task configuration with pack_format expansion
  - **Priority**: High
  - **Risk**: Low

- [ ] T405 [Infrastructure] Verify Data Pack integration
  - Test: `./gradlew :common:processResources -Ptarget_mc_version=1.21.1` uses singular folders
  - Test: `./gradlew :common:processResources -Ptarget_mc_version=1.20.6` uses plural folders
  - Verify pack.mcmeta pack_format values
  - **Priority**: High
  - **Risk**: Low

#### Phase 2: Abstraction Layer Design (3-4 days)

**Goal**: Design abstraction interfaces to absorb API differences between versions

- [ ] T406 [Infrastructure] Design compat package structure
  - Design `ItemDataHandler` interface (setString, getString, setInt, getInt, etc.)
  - Design `SavedDataHandler` interface (save method with registries parameter)
  - Design `RegistryHandler` interface (if needed)
  - Document interface contracts and null safety guarantees
  - **Priority**: High
  - **Risk**: Medium (design mistakes impact subsequent phases)

- [ ] T407 [Infrastructure] Survey existing code API usage patterns
  - Create list of `getOrCreateTag()` usage locations (47 files identified)
  - Survey `SavedData.save()` signature changes (7 files)
  - Survey BlockEntity `saveAdditional()` signature changes
  - Document all version-specific API calls
  - **Priority**: High
  - **Risk**: Medium

- [ ] T408 [Infrastructure] Finalize abstraction layer specifications
  - Finalize ItemDataHandler method list with all required data types
  - Define error handling policy (exceptions vs null returns)
  - Define version detection mechanism (system properties)
  - Create API documentation for compat package
  - **Priority**: High
  - **Risk**: Medium

#### Phase 3: Abstraction Layer Implementation (5-7 days)

**Goal**: Implement abstraction layer and separate version-specific code

- [ ] T409 [Infrastructure] Implement ItemDataHandler abstraction
  - Create `common/src/main/java/com/chronodawn/compat/ItemDataHandler.java` interface
  - Create `common/src/main/java/com/chronodawn/compat/v1_20_6/ItemDataHandler120.java` (NBT implementation)
  - Create `common/src/main/java/com/chronodawn/compat/v1_21_1/ItemDataHandler121.java` (Component implementation)
  - Create `CompatHandlers` factory class with version detection
  - **Priority**: High
  - **Risk**: Medium

- [ ] T410 [Infrastructure] Implement SavedDataHandler abstraction
  - Create SavedDataHandler interface
  - Implement 1.20.6 version (ignore registries parameter)
  - Implement 1.21.1 version (use HolderLookup.Provider)
  - Add to CompatHandlers factory
  - **Priority**: High
  - **Risk**: Medium

- [ ] T411 [Infrastructure] Configure Gradle for version-specific Java code
  - Add `compat_package` property to version property files
  - Update `sourceSets.main.java` to include version-specific directories
  - Set `chronodawn.minecraft.version` system property at compile time
  - **Priority**: High
  - **Risk**: Medium

- [ ] T412 [Infrastructure] Verify abstraction layer implementation
  - Test: 1.20.6 build uses ItemDataHandler120
  - Test: 1.21.1 build uses ItemDataHandler121
  - Create unit tests for compat handlers (if possible)
  - Verify no compile errors for both versions
  - **Priority**: High
  - **Risk**: Medium

#### Phase 4: Migrate Existing Code (10-14 days)

**Goal**: Refactor existing 47 files to use abstraction layer

- [ ] T413 [Infrastructure] Migrate items package to compat layer
  - Refactor `PortalStabilizerItem.java` to use ItemDataHandler
  - Refactor `TimeClockItem.java` to use ItemDataHandler
  - Refactor `TimeCompassItem.java`, `ChronoAegisItem.java`, `DecorativeWaterBucketItem.java`
  - Refactor other items (~10 files)
  - **Priority**: Medium
  - **Risk**: High (potential bugs)
  - **Files**: ~15 files

- [ ] T414 [Infrastructure] Migrate data package to compat layer
  - Modify `ChronoDawnWorldData.java` save() method to use SavedDataHandler
  - Modify `PortalRegistryData.java` save() method
  - Modify other SavedData classes (~5 files)
  - **Priority**: Medium
  - **Risk**: High
  - **Files**: ~7 files

- [ ] T415 [Infrastructure] Migrate entities/bosses package to compat layer
  - Modify `TimeTyrantEntity.java` NBT handling
  - Modify `TimeGuardianEntity.java`, `TemporalPhantomEntity.java`, `EntropyKeeperEntity.java`, `ClockworkColossusEntity.java`
  - Modify other entity classes
  - **Priority**: Medium
  - **Risk**: High
  - **Files**: ~10 files

- [ ] T416 [Infrastructure] Migrate blocks package to compat layer
  - Modify `BossRoomDoorBlockEntity.java` NBT handling
  - Modify other BlockEntity classes
  - **Priority**: Medium
  - **Risk**: High
  - **Files**: ~5 files

- [ ] T417 [Infrastructure] Migrate remaining files to compat layer
  - Survey and migrate core/, gui/, worldgen/, etc. packages
  - Refactor all remaining direct NBT/Component API calls
  - **Priority**: Medium
  - **Risk**: High
  - **Files**: ~10 files

- [ ] T418 [Infrastructure] Verify code migration completion
  - Run static analysis: `grep -r "getOrCreateTag()" | grep -v "compat/"` (expect: 0 results)
  - Verify successful build for both versions
  - Run test suite for both versions
  - **Priority**: High
  - **Risk**: High

#### Phase 5: Build Script Enhancement (3-4 days)

**Goal**: CI/CD support and improved developer experience

- [ ] T419 [Infrastructure] Add convenient Gradle tasks
  - Create `build1206` task (shortcut for 1.20.6 build)
  - Create `build1211` task (shortcut for 1.21.1 build)
  - Create `buildAll` task (sequential build for all versions)
  - **Priority**: Low
  - **Risk**: Low

- [ ] T420 [Infrastructure] Configure output JAR naming
  - Update Fabric build.gradle: `chronodawn-0.1.0+1.20.6-fabric.jar`
  - Update NeoForge build.gradle: `chronodawn-0.1.0+1.21.1-neoforge.jar`
  - **Priority**: Low
  - **Risk**: Low

- [ ] T421 [Infrastructure] Update GitHub Actions workflow (if applicable)
  - Configure matrix build for both versions
  - Configure artifact upload with version labels
  - **Priority**: Low
  - **Risk**: Low

- [ ] T422 [Infrastructure] Update documentation
  - Update build instructions in README.md
  - Add Gradle multi-version setup notes to CLAUDE.md
  - Update developer_guide.md with version switching commands
  - **Priority**: Low
  - **Risk**: Low

#### Phase 6: Integration Testing and Verification (5-7 days)

**Goal**: Verify operation in both versions with real Minecraft environments

- [ ] T423 [Infrastructure] Verify 1.20.6 build and runtime
  - Build: `./gradlew clean build -Ptarget_mc_version=1.20.6`
  - Test Minecraft 1.20.6 startup
  - Verify Data Pack loaded (plural folders)
  - Test Portal Stabilizer (ItemStack NBT)
  - Test SavedData persistence
  - **Priority**: High
  - **Risk**: High

- [ ] T424 [Infrastructure] Verify 1.21.1 build and runtime
  - Build: `./gradlew clean build -Ptarget_mc_version=1.21.1`
  - Test Minecraft 1.21.1 startup
  - Verify Data Pack loaded (singular folders)
  - Test Portal Stabilizer (ItemStack Components)
  - Test SavedData persistence
  - **Priority**: High
  - **Risk**: High

- [ ] T425 [Infrastructure] Cross-version feature testing
  - Test all bosses spawn correctly in both versions
  - Test all structures generate correctly in both versions
  - Test Chronicle guidebook in both versions
  - Test portal mechanics in both versions
  - Test dimension features in both versions
  - **Priority**: High
  - **Risk**: High

- [ ] T426 [Infrastructure] Performance verification
  - Measure and compare startup time (1.20.6 vs 1.21.1)
  - Measure memory usage in both versions
  - Verify no performance degradation from abstraction layer
  - **Priority**: Medium
  - **Risk**: Medium

- [ ] T427 [Infrastructure] Bug fixes and polish
  - Fix all bugs discovered during testing
  - Re-test after fixes
  - Create regression test suite
  - **Priority**: High
  - **Risk**: High

**Rollback Plan**:
- If Phase 3-4 becomes too difficult: Implement Plan B (Data Pack multi-version only, Java code via Git branches)
- If critical issues found in Phase 6: Implement Plan C (Revert to 1.21.1 only, postpone 1.20.6 support)

**Success Criteria**:
- ✅ Both 1.20.6 and 1.21.1 builds compile without errors
- ✅ Both versions run in production Minecraft environments
- ✅ All features work correctly in both versions
- ✅ No direct NBT/Component API calls outside compat/ package
- ✅ Documentation updated with multi-version build instructions

**Notes**:
- External dependencies: None (no Stonecutter, Manifold, or other preprocessors)
- AI development efficiency: All code in single codebase, visible to AI
- Maintainability: Standard Gradle features only, no complex toolchains

---

### Playtest Improvements - Structure Generation

**Purpose**: Fix structure generation issues discovered through playtesting

- [X] T309 [P] Fix Phantom Catacombs structure search freezing (2026-01-02)
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

---

### Performance & Thread Safety Audit (Critical)

**Purpose**: Identify and fix potential performance and thread safety issues across all server-side code

**Background**: Lessons learned from T309 (Phantom Catacombs freezing fix):
1. Main thread blocking causes world freezing and player disconnections in multiplayer
2. Non-thread-safe collections (HashMap, HashSet, ArrayList) cause race conditions in multi-threaded server environment

**Priority**: Critical (affects all multiplayer gameplay)

- [x] T428 [Performance] Audit and fix main thread blocking in structure generation
  - **Issue**: Long-running synchronous operations on main thread cause world freezing
  - **Investigation**:
    - Scan all structure generation code for large-scale block scanning (e.g., Boss Room Placers)
    - Identify chunk-loading operations that may block main thread
    - Check for nested loops scanning large areas (>10,000 blocks)
  - **Files to check**:
    - `common/src/main/java/com/chronodawn/worldgen/spawning/PhantomCatacombsBossRoomPlacer.java` ✓ (Fixed in T309)
    - Other Boss Room Placers (if any exist)
    - Structure generation classes in `common/src/main/java/com/chronodawn/worldgen/structures/`
  - **Solution patterns**:
    - Multi-tick state machine (process 1 chunk per tick)
    - Async processing with main thread synchronization
    - Limit search area to reasonable bounds
  - **Success criteria**:
    - No single-tick operations scanning >1,000 blocks
    - No world freezing during structure generation in multiplayer
    - Server TPS remains stable during structure placement
  - **Reference**: T309 implementation (PhantomCatacombsBossRoomPlacer.java multi-tick state machine)
  - **Priority**: Critical
  - **Risk**: High

- [ ] T429 [Thread Safety] Audit and fix non-thread-safe collection usage
  - **Issue**: HashMap, HashSet, ArrayList are not thread-safe and cause race conditions in multiplayer
  - **Investigation**:
    - Scan all server-side shared state for non-thread-safe collections
    - Identify collections accessed by multiple dimensions/threads simultaneously
    - Check static fields and cached data structures
  - **Files to check**:
    - `common/src/main/java/com/chronodawn/worldgen/spawning/PhantomCatacombsBossRoomPlacer.java` ✓ (Fixed in T309)
    - Other Boss Spawner classes (`TemporalPhantomSpawner.java`, `ChronosWardenSpawner.java`, etc.)
    - Registry classes (`PortalRegistry.java`, etc.)
    - Event handlers (`EntityEventHandler.java`, `BlockEventHandler.java`, etc.)
  - **Solution patterns**:
    - Replace HashMap → ConcurrentHashMap
    - Replace HashSet → ConcurrentHashMap.newKeySet()
    - Replace ArrayList → CopyOnWriteArrayList (if read-heavy)
    - Use atomic operations (compute, computeIfAbsent, etc.)
  - **Search commands**:
    ```bash
    # Find HashMap usage
    grep -r "new HashMap<" --include="*.java" common/src/main/java/com/chronodawn/
    # Find HashSet usage
    grep -r "new HashSet<" --include="*.java" common/src/main/java/com/chronodawn/
    # Find ArrayList in static fields
    grep -r "static.*new ArrayList<" --include="*.java" common/src/main/java/com/chronodawn/
    ```
  - **Success criteria**:
    - No HashMap/HashSet/ArrayList in server-side shared state
    - All static collections use thread-safe implementations
    - No race conditions in multiplayer testing
  - **Reference**: T309 fix (ConcurrentHashMap usage in PhantomCatacombsBossRoomPlacer.java)
  - **Priority**: Critical
  - **Risk**: High

- [ ] T430 [Dimension Isolation] Audit and fix dimension filtering in chunk processing
  - **Issue**: Processing logic may handle entities/structures from all dimensions instead of current dimension only
  - **Investigation**:
    - Scan all server-level tick handlers that process chunks/structures/entities
    - Identify code that stores state globally without dimension filtering
    - Check for cross-dimension interference in structure generation and entity processing
  - **Files to check**:
    - `common/src/main/java/com/chronodawn/worldgen/spawning/PhantomCatacombsBossRoomPlacer.java` ✓ (Fixed in T309)
    - Other Boss Spawner classes with tick-based processing
    - Entity tick handlers (`EntityEventHandler.java`)
    - Structure generation classes
  - **Solution patterns**:
    - Store dimension ID alongside structure/entity state
    - Filter by `level.dimension().location()` before processing
    - Use dimension-keyed maps: `Map<ResourceLocation, Map<?, ?>>`
  - **Example code pattern**:
    ```java
    // Store dimension ID with state
    private static class ProcessingState {
        ResourceLocation dimensionId;
        // ... other fields
    }

    // Filter by dimension before processing
    public static void progressAllProcessing(ServerLevel level) {
        ResourceLocation currentDimension = level.dimension().location();

        for (Map.Entry<?, ProcessingState> entry : states.entrySet()) {
            ProcessingState state = entry.getValue();
            if (state.dimensionId.equals(currentDimension)) {
                // Process only structures belonging to current dimension
                progressProcessing(level, state);
            }
        }
    }
    ```
  - **Success criteria**:
    - Each dimension processes only its own structures/entities
    - No cross-dimension interference in multiplayer
    - Overworld/Nether/End dimensions don't process Chrono Dawn structures
  - **Reference**: T309 fix (dimension filtering in PhantomCatacombsBossRoomPlacer.java:1277-1292)
  - **Priority**: Critical
  - **Risk**: High

**Estimated Total Effort**: 1.5-2.5 days (T428: 4-6 hours, T429: 4-6 hours, T430: 4-6 hours)

**Testing Requirements**:
- Single-player: No performance degradation
- Multiplayer: No world freezing with 2+ players
- Multiplayer: No race conditions when multiple players trigger same structure/boss spawn

---

