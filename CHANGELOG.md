# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.4.0-beta] - 2026-01-17

### Added

### Changed

### Fixed

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

[Unreleased]: https://github.com/ksoichiro/ChronoDawn/compare/v0.4.0-beta+1.21.1...HEAD
[0.4.0-beta]: https://github.com/ksoichiro/ChronoDawn/compare/v0.3.0+1.21.1...v0.4.0-beta+1.21.1
[0.3.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.3.0-beta+1.21.1...v0.3.0+1.21.1
[0.3.0-beta]: https://github.com/ksoichiro/ChronoDawn/compare/v0.2.0+1.21.1...v0.3.0-beta+1.21.1
[0.2.0]: https://github.com/ksoichiro/ChronoDawn/compare/v0.1.0+1.21.1...v0.2.0+1.21.1
[0.1.0]: https://github.com/ksoichiro/ChronoDawn/releases/tag/v0.1.0+1.21.1
