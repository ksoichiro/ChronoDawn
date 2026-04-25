# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

#### New Blocks
- **Time Torches** (Orange / Pink / Purple) — colored decorative torch variants with floor and wall placement. Crafted from the matching Time Blossom + Stick (4 torches per recipe)
- **Temporal Lantern**, **Dawn Lantern**, and **Dusk Lantern** — pickaxe-mined light sources using the vanilla lantern shape (3D rendering, animated flame, standing and hanging placement). Dawn/Dusk Lantern crafted from the matching Bell flower (+ iron nuggets + torch); Temporal Lantern crafted from any Time Blossom (+ iron nuggets + torch)
- Recipe advancements unlock the Time Torch / Lantern recipes when the player picks up the relevant Time Blossom or Bell
- **Temporal Stalactites** and **Temporal Stalagmites** — decorative pointed cave blocks themed on Temporal Stone, generating across all ChronoDawn dimension biomes. Two sizes per direction (`tip` and `frustum`) form length-1 and length-2 formations on cave ceilings (stalactite) and floors (stalagmite). Pickaxe-mined at stone tier; `tip` blocks self-drop while `frustum` drops the matching `tip` so worldgen-only formations still produce a usable item when broken. Vanilla `pointed_dripstone` and Dripstone Caves remain untouched

## [0.7.0] - 2026-04-22

### Added

#### New Blocks
- **Temporal Stone** and **Temporal Cobblestone** block sets with stairs, slabs, and walls
- **Temporal Dirt** and **Temporal Grass Block** as custom terrain in ChronoDawn dimension
- **Temporal ore blocks** (coal, gold, iron, redstone) replacing vanilla ores in ChronoDawn dimension
- **Entropy Crystal Ore** — new Tier 2 ore found at Y=40-100 in ChronoDawn dimension
- **Temporal Amber Ore** — new Tier 2 ore found at Y=-30 to +20 in ChronoDawn dimension
- **Temporal Sand**, **Temporal Gravel**, and **Temporal Sandstone** (plus sandstone stairs/slabs/walls) — pale-blue terrain blocks that replace vanilla `sand`/`red_sand`/`gravel`/`sandstone`/`red_sandstone` wherever they generate in the Chrono Dawn dimension
- Registered under `#minecraft:sand`, `#minecraft:gravel`, and `#minecraft:sandstone_blocks` so tag-based vanilla recipes accept the Temporal variants
- Crafting recipes: 4 Temporal Sand → Temporal Sandstone; Temporal Sandstone → stairs/slabs/walls (crafting + stonecutting); Temporal Sand → Glass (furnace smelting); Temporal Sand + Gunpowder → TNT; Temporal Sand + Temporal Gravel + Dye → Concrete Powder (all 16 colors)
- Recipe advancements unlock the above recipes in the recipe book when the player picks up the relevant ingredient (Temporal Sand / Temporal Gravel / Temporal Sandstone)

#### New Items & Equipment
- **Entropy Crystal** — material dropped from Entropy Crystal Ore
- **Entropy Crystal Sword** — applies Entropy Effect (damage over time) on hit
- **Raw Temporal Amber** — material dropped from Temporal Amber Ore
- **Temporal Amber Dust** — crafting material refined from Raw Temporal Amber
- **Temporal Amber Armor** (full set) — with auto-repair feature that consumes Temporal Amber Dust when out of combat
- ChronoDawn Custom Shields: three tiered shields (Clockstone, Enhanced Clockstone, Entropy Crystal) with cumulative time-manipulation effects — passive time-debuff shortening, faster raise on 1.21.5+, Speed on successful block (T2+), and Time Echo auto-block (T3). Crafted via a tier upgrade chain with Time Wood plank variants and dimension materials. Independent from the existing Chrono Aegis artifact.

#### Gameplay Features
- **Entropy Effect** — new mob effect dealing damage over time, applied by Entropy Crystal Sword
- **Temporal Amber Armor auto-repair** — 5 durability per piece every 3 seconds while out of combat, consuming 1 Temporal Amber Dust per repair tick
- 4 new advancements: Eroder of Time, Sealed in Time, Fractured Time, Preserved in Amber
- Recipe unlock for Entropy Crystal Sword on Entropy Crystal pickup
- Emissive render layers for Timeline Strider, Paradox Crawler, and Pulse Hog mobs
- Loot table for Clockwork Depths archive vault chests
- Experience drops from Temporal Coal Ore
- TimeKeeper village block replacement pipeline using NBT templates

#### Development & Build
- Shared Gradle scripts via minecraft-mod-gradle-scripts submodule

### Changed
- Rebased Time Crystal and Clockstone ore textures onto Temporal Stone base
- Chrono Dawn dimension surface rules now produce Temporal sand/gravel/sandstone; the `sand_disk` / `gravel_disk` configured features place Temporal variants instead of the vanilla blocks

### Fixed
- TimeBlastRenderer crash on 1.21.9+ due to missing setLight in submit() API
- All armors invisible when worn on 1.21.4+ (missing equipment asset path)
- Boss entities could despawn unexpectedly (added persistence overrides)
- Underground dungeon overlap (added exclusion zones between structures)
- Frozen time progression in ChronoDawn dimension
- Missing Ticking Sheep registrations in NeoForge 1.21.4 and other version modules
- Missing Client Items JSON for Ticking Sheep spawn egg
- Removed non-functional Spatially Linked Pickaxe ultimate recipe

## [0.6.0] - 2026-03-14

### Added

#### New Mobs
- **Ticking Sheep**: Custom sheep mob ported to all supported versions

#### Minecraft Version Support
- Minecraft 1.21.7 support
- Minecraft 1.21.8 support
- Minecraft 1.21.9 support
- Minecraft 1.21.10 support
- Minecraft 1.21.11 support

#### Gameplay Features
- **Chrono Dawn Prairies biome**: New biome added and ported to all supported versions
- Light emission (level 9) added to Clockstone Block across all versions
- Animated textures for Clockstone, teleporter, ice, and crystal blocks
- Dye crafting recipes for custom flowers
- Loot tables for hostile mobs

#### Development & Build
- CurseForge and Modrinth release automation scripts
- Shared test infrastructure for cross-version validation
- Unit tests for MinecraftVersion, ModIdEnum, PortalState, and TestUtils

### Changed
- Moved tyrant defeat advancement grant from tick polling to player join event
- Shared identical resource files across version ranges via shared directories (shared, shared-1.21.1+, shared-1.21.2+, shared-1.21.5+)
- Restructured version-specific directories to hierarchical layout
- Generated 1.20.1 NBT structures into build directory instead of src
- Limited gameTestAll to 2 parallel threads via fixed thread pool

### Fixed
- Removed chrono_turtle from mountain and snowy biomes
- Synced mob spawner entries across all versions
- Added 1.20.1-compatible disk feature format for coarse dirt
- Rewrote 1.20.1 Clockwork Colossus spawner to use direct engine room check
- Replaced minecraft:creeper with chronodawn:moment_creeper in biome spawns for 1.20.1 and 1.21.1
- Triggered arm swing animation on boss room door interaction (1.21.2-1.21.10)
- Updated preliminary_surface_level to vanilla find_top_surface density function (1.21.9, 1.21.10)
- Added 0xFF alpha prefix to drawString color values (1.21.6-1.21.8)
- Used JSON objects for text components in loot table blueprints (1.21.5-1.21.8)
- Restored axe stripping arm swing animation for Time Wood logs (1.21.2-1.21.8)
- Fixed reverse sort in JAR listing for release scripts

## [0.5.0] - 2026-02-06

### Added

#### New Mobs
- **Timebound Rabbit**: New mob in Chrono Dawn dimension
- **Pulse Hog**: New mob in Chrono Dawn dimension
- **Secondwing Fowl**: Chicken-based mob with vanilla chicken parameters
- **Hourglass Golem**: Iron Golem-based mob

#### Minecraft Version Support
- Minecraft 1.21.3 support (hotfix release sharing 1.21.2 modules)
- Minecraft 1.21.4 support with API migration (EntityRenderState, spawn egg changes)
- Minecraft 1.21.5 support with API migration (SavedData Codec, weapon/tool APIs, MobEffects, NBT methods)
- Minecraft 1.21.6 support with API migration (advancement background resource path, mixin config updates)

#### Gameplay Features
- Land ecosystem added to ocean biome for above-water terrain generation

#### Development & Build
- `collectJars` and `release` Gradle tasks for streamlined release pipeline
- Cross-version translation key validation
- GameTest support for Minecraft 1.21.5 (Fabric and NeoForge)

### Fixed
- Entropy Crypt trapdoor can now be opened after boss spawn

### Performance
- Parallelized `buildAll` task with isolated project cache for faster builds

## [0.4.0] - 2026-01-25

## [0.4.0-beta] - 2026-01-25

### Added

#### Minecraft 1.21.2 Support
- Added Minecraft 1.21.2 support alongside existing 1.20.1 and 1.21.1
- Migrated to 1.21.2 APIs: EntityRenderState, ArmorMaterial, ToolMaterial, Consumable, data-driven equipment
- Version-specific Gradle tasks: `clean1_21_2`, `build1_21_2`, `runClientFabric1_21_2`, `runClientNeoForge1_21_2`

#### New Mobs
- **Timeline Strider**: Enderman replacement in Chrono Dawn dimension
- **Secondhand Archer**: Skeleton replacement in Chrono Dawn dimension
- **Paradox Crawler**: Spider replacement in Chrono Dawn dimension
- **Epoch Husk**: Zombie replacement in Chrono Dawn dimension
- **Forgotten Minute**: Flying hostile mob with unique AI behavior
- **Chronal Leech**: Hostile mob in Chrono Dawn dimension
- **Moment Creeper**: Creeper variant with complete creeper behavior
- **Chrono Turtle**: Friendly water mob with spawn egg
- **Glide Fish**: Water creature mob with food items (raw/cooked) and cooking recipes

#### Gameplay Features
- Master Clock structure generation triggered by entrance door open
- Custom mob spawns added to all Chrono Dawn dimension biomes

#### Development & Testing
- Comprehensive GameTest suite: structure templates, mob behavior, advancements, registry validation, block protection
- `checkAll` task for full verification (clean → validateResources → build → test → gameTest)
- `cleanAll`, `testAll`, `gameTestAll` Gradle tasks
- `runClient` shortcut tasks for each version and loader combination
- Registry-driven GameTest generation replacing hand-written tests
- Resource validation tests (JSON syntax, blockstate→model, model→texture cross-references)
- Multi-language translation validation
- Centralized ID definitions (ModBlockId, ModItemId, ModEntityId, ModBlockEntityId) with GameTest validation
- Colored log output for runClient tasks

### Changed
- Restructured project to multi-version subproject architecture
- Extracted 54 version-agnostic source files into `common-shared/`
- Centralized default `target_mc_version` in `gradle.properties`
- Moved Glide Fish items to Food section in creative tab
- Changed ChronoDawn log output from INFO to DEBUG level
- Reduced excessive DEBUG logging from checkAndPlaceRooms
- Parallelized `gameTestAll` by Minecraft version for faster execution
- Migrated render layer configuration from code to JSON model metadata
- Replaced string literals with enum references (ModBlockId, ModItemId) throughout codebase
- Used custom models for boss_room_door to eliminate deprecated BlockRenderLayerMap API

### Fixed

#### Resource & Recipe Fixes
- Restored BlockRenderLayerMap registration for Fabric (translucent blocks)
- Added missing item tag translations for Fabric tag conventions
- Added missing loot tables for wood doors, trapdoors, fence gates, buttons, pressure plates
- Used tag-based ingredients for planks recipes to allow all log variants
- Added missing wood blocks and chrono_melon to `minecraft:mineable/axe` tag
- Added `useBlockDescriptionPrefix` to BlockItems for proper translation keys

#### Build & Development
- Added Architectury Transformer cache cleanup to cleanAll task
- Added IDE `bin/` cleanup to cleanAll task
- Fixed portal teleports at END_SERVER_TICK to prevent ConcurrentModificationException

### Performance
- Parallelized gameTestAll execution by Minecraft version
- Removed excessive DEBUG logging from structure generation

## [0.3.0] - 2026-01-17

### Changed
- Official stable release of 0.3.0 (no functional changes from 0.3.0-beta)

## [0.3.0-beta] - 2026-01-04

### ⚠️ BREAKING CHANGES

**Warning**: This update is NOT compatible with worlds created in v0.2.0 or earlier.

- **Portal System Rewrite**: Custom Portal API dependency has been removed and replaced with an independent portal implementation. Existing portals will not function correctly.
  - **Action Required**: Break and rebuild portals using Time Hourglass
  - **Technical Details**: Portal placement and teleportation logic has been rewritten using Mixins instead of Custom Portal API
- **Save Data Migration**: Eye of Chronos effect storage format has changed from inventory-based detection to persistent dimension state (SavedData).
  - **Impact**: Boss defeat progress tracked by Eye of Chronos may be reset
  - **Affected Features**: Final boss access requirements, dimension-specific persistent effects

**Recommendation**: Back up your world before updating. If you have in-progress gameplay in v0.2.0 worlds, consider completing your current objectives before updating.

### Fixed
- Resolved NeoForge runClient module resolution error caused by loader-specific Mixins using common package name
- Moved loader-specific PortalPlacerMixin to loader-specific packages (com.chronodawn.fabric.mixin, com.chronodawn.neoforge.mixin)

## [0.2.0] - 2026-01-04

### Added
- Wood and Stripped Wood blocks for all Time Wood variants (Ancient, Dawn, Dusk, Twilight, Eternal)
- Axe stripping functionality for Time Wood logs (right-click with axe to convert Log → Stripped Log)
- Spawn eggs for all boss entities (Time Guardian, Chronos Warden, Clockwork Colossus, Temporal Phantom, Entropy Keeper, Time Tyrant)
- Iron ore generation in Chrono Dawn dimension for sustainable gameplay
- Dawn Bell and Dusk Bell custom flowers in Chrono Dawn dimension
- Time Blossom flowers replacing vanilla flowers in worldgen
- Persistent boss spawn tracking system using SavedData (prevents duplicate boss spawns)

### Changed
- Chrono Aegis shield buff now affects all nearby players (not just wielder)
- Replaced vanilla flowers with Time Blossom in Chrono Dawn dimension worldgen

### Fixed
- Added Stripped Time Wood Logs and Wood blocks to `minecraft:mineable/axe` tag (fixes slow mining speed with axes)
- Prevented item loss when placing blocks in protected areas on NeoForge
- Reduced Clockstone ore generation frequency (was too common)
- Resolved recipe conflict between Enhanced Clockstone Pickaxe and Spatially Linked Pickaxe
- Excluded all bosses from Time Distortion Effect (Slowness IV) and increased Chronos Warden base speed
- Fixed EntropyKeeperSpawner block scanning freeze issue
- Implemented multi-tick state machine for MasterClock boss room placement (prevents server lag)
- Resolved thread-safety issues in boss spawner systems
- Improved BossRoomProtectionProcessor performance with dimension filtering
- Fixed immediate marker pairing for programmatic structure placement

### Performance
- Optimized TimeGuardianSpawner for better performance and thread-safety
- Refactored ClockworkColossusSpawner for improved reliability and thread-safety
- Consolidated boss spawn data into single BossSpawnData class
- Added dimension filtering to BossRoomProtectionProcessor (avoids unnecessary processing)

## [0.1.0] - 2025-12-27

### Added
- **Core Features**:
  - Chrono Dawn dimension with perpetual twilight atmosphere
  - Custom portal system using Time Hourglass (one-way) and Portal Stabilizer (bidirectional)
  - Time Distortion effect (hostile mobs move slower in Chrono Dawn)
  - 8 unique biomes: Forest, Desert, Mountain, Ocean, Snowy, Swamp, Dark Forest, Ancient Forest

- **Content**:
  - Time Wood custom wood type with full block set (logs, planks, stairs, slabs, doors, trapdoors, fences, buttons, pressure plates)
  - 4 Time Wood variants: Ancient, Dawn, Dusk, Twilight, Eternal
  - Custom sapling with unique tree generation

- **Structures**:
  - Ancient Ruins (Overworld spawn point with portal frame)
  - Forgotten Library (Chrono Dawn exploration structure)
  - Guardian Vault (Chronos Warden boss dungeon)
  - Clockwork Depths (Clockwork Colossus boss dungeon)
  - Phantom Catacombs (Temporal Phantom boss dungeon)
  - Entropy Crypt (Entropy Keeper boss dungeon)
  - Master Clock (final dungeon with Time Tyrant boss)

- **Boss Enemies**:
  - Time Guardian (mini-boss, drops Master Clock Key)
  - Chronos Warden (mid-boss, drops Guardian Stone)
  - Clockwork Colossus (mid-boss, drops Colossus Gear)
  - Temporal Phantom (mid-boss, drops Phantom Essence)
  - Entropy Keeper (mid-boss, drops Entropy Core)
  - Time Tyrant (final boss)

- **Ultimate Artifacts**:
  - Chronoblade (powerful sword)
  - Time Guardian's Mail (enhanced armor)
  - Chrono Aegis (protective shield)

- **Resources**:
  - Clockstone ore and refined resources
  - Custom tools and equipment progression
  - Portal activation items (Time Hourglass, Portal Stabilizer)

- **Technical**:
  - Multi-loader architecture (Fabric + NeoForge)
  - Architectury framework with shared common code
  - Custom Portal API integration
  - Performance-optimized entity tick handling
  - Boss room protection system

### Technical Notes
- Built for Minecraft Java Edition 1.21.1
- Fabric Loader 0.17.3+ with Fabric API 0.116.7+
- NeoForge 21.1.209+
- Architectury API 13.0.8+
- Custom Portal API 0.0.1-beta66-1.21 (Fabric bundled, NeoForge requires separate installation)

[Unreleased]: https://github.com/ksoichiro/ChronoDawn/compare/v0.7.0...HEAD
[0.7.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.5.0+1.21.2...v0.6.0+1.21.2
[0.5.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.4.0+1.21.2...v0.5.0+1.21.2
[0.4.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.4.0-beta+1.21.1...v0.4.0+1.21.1
[0.4.0-beta]: https://github.com/ksoichiro/ChronoDawn/compare/v0.3.0+1.21.1...v0.4.0-beta+1.21.1
[0.3.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.3.0-beta+1.21.1...v0.3.0+1.21.1
[0.3.0-beta]: https://github.com/ksoichiro/ChronoDawn/compare/v0.2.0+1.21.1...v0.3.0-beta+1.21.1
[0.2.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.1.0+1.21.1...v0.2.0+1.21.1
[0.1.0]: https://github.com/ksoichiro/ChronoDawn/releases/tag/v0.1.0+1.21.1
