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
- [ ] T242: Improve boss visual diversity (custom models instead of reusing TimeGuardianModel)
- [ ] T243: Add boss-specific sound effects
- [ ] T244: Create advancement system for defeating all 4 bosses

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

- [ ] T710 [Documentation] Translate docs/implementation_history.md to English
  - Translate implementation history (227 lines of Japanese)
  - Provides context for design decisions
  - Estimated effort: 1.5-2 hours
  - Status: Medium priority (historical reference)

- [ ] T711 [Documentation] Translate docs/initial_design.md to English
  - Translate initial design document (45 lines of Japanese)
  - Historical value for understanding project evolution
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

### Playtest Improvements - Boss Battle

**Purpose**: Boss battle improvements discovered through playtesting

- [x] T302 [P] Fix Master Clock boss room door unlock requirement
  - **Issue**: Door opens with only Ancient Clockwork x3, should also require Key to Master Clock
  - **Investigation**: Check BossRoomDoorBlock.java unlock condition logic
  - **Fix**: Update unlock requirement to check for both Ancient Clockwork x3 AND Key to Master Clock
  - **Completed**: Modified BlockEventHandler.java:166 to require both hasRequiredAncientGears() AND hasKeyToMasterClock()
  - **File**: common/src/main/java/com/chronodawn/events/BlockEventHandler.java:166
- [ ] T303 [P] Prevent non-boss mob spawning in Desert Clock Tower boss room
  - **Issue**: Other mobs spawn in Desert Clock Tower boss room during battle
  - **Investigation**: Check structure configuration and spawning rules for desert_clock_tower
  - **Possible solutions**: Add no_mob_spawning flag to boss room area, or implement custom spawning blocker
- [ ] T303a [P] Prevent Master Clock structure from being overwritten by other structures
  - **Issue**: Master Clock may be overwritten by other structures (e.g., Ancient Ruins, Desert Clock Tower)
  - **Investigation**: Check structure generation priority and placement rules
  - **Possible solutions**:
    - Adjust structure generation priority (step parameter in structure_set)
    - Add structure spacing/separation rules to prevent overlap
    - Implement custom structure conflict detection system
    - Use higher `step` value (e.g., RAW_GENERATION) to ensure Master Clock generates before other structures
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

- [ ] T308 [Rebranding] Rename mod and dimension from "Chronosphere" to "Chrono Dawn"
  - **Scope**:
    - Mod name change (Chronosphere → Chrono Dawn)
    - Dimension name change (chronodawn → chrono_dawn)
    - Package name maintained (com.chronodawn)
  - **Files to Update**:
    - Documentation files (README.md, docs/*, CLAUDE.md, etc.)
    - Mod metadata files (fabric.mod.json, neoforge.mods.toml)
    - Resource files (dimension configs, lang files, etc.)
    - Build configuration files (gradle.properties, build.gradle)
  - **Testing**:
    - Verify mod loads correctly with new name
    - Verify dimension is accessible with new identifier
    - Verify translations are correct
  - **Priority**: Medium (branding update)

