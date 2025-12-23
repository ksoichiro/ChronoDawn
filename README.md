# Chrono Dawn

**Time-manipulation themed dimension mod for Minecraft**

A multi-loader Minecraft mod that adds a mysterious time-themed dimension called "Chrono Dawn" with custom portals, unique biomes, powerful bosses, and time-manipulating artifacts.

## Features

### Core Mechanics
- **Custom Dimension**: Explore Chrono Dawn, a dimension frozen in perpetual twilight
- **Portal System**: One-way portal travel using Time Hourglass, upgradeable to bidirectional with Portal Stabilizer
- **Time Distortion**: Hostile mobs move slower in Chrono Dawn dimension
- **8 Unique Biomes**: Forest, Desert, Mountain, Ocean, Snowy, Swamp, Dark Forest, and Ancient Forest

### Content
- **Custom Wood Type**: Time Wood trees with full block set (logs, planks, stairs, slabs, doors, etc.)
- **Major Structures**:
  - Ancient Ruins (Overworld spawn)
  - Forgotten Library (Chrono Dawn)
  - Guardian Vault (Chronos Warden boss)
  - Clockwork Depths (Clockwork Colossus boss)
  - Phantom Catacombs (Temporal Phantom boss)
  - Entropy Crypt (Entropy Keeper boss)
  - Master Clock (final dungeon with Time Tyrant boss)
- **Boss Enemies**:
  - Time Guardian (mini-boss, drops Master Clock Key)
  - Chronos Warden (mid-boss, drops Guardian Stone)
  - Clockwork Colossus (mid-boss, drops Colossus Gear)
  - Temporal Phantom (mid-boss, drops Phantom Essence)
  - Entropy Keeper (mid-boss, drops Entropy Core)
  - Time Tyrant (final boss)
- **Ultimate Artifacts**: Chronoblade (sword), Time Guardian's Mail (armor), Chrono Aegis (shield)

### Technical Features
- **Multi-Loader Architecture**: Supports both Fabric and NeoForge loaders
- **Architectury Framework**: Shared common code (80%) with loader-specific implementations (20%)
- **Custom Portal API**: Advanced portal mechanics with Custom Portal API
- **Performance Optimized**: Efficient entity tick handling and caching systems

## Requirements

### For Players
- **Minecraft**: Java Edition 1.21.1
- **Mod Loader**:
  - Fabric Loader 0.17.3+ with Fabric API 0.116.7+
  - OR NeoForge 21.1.209+
- **Dependencies**:
  - Architectury API 13.0.8+
  - Custom Portal API 0.0.1-beta66-1.21+
  - Patchouli 1.21.1-92+ (for in-game guide book)

### For Developers
- **Java Development Kit (JDK)**: 21 or higher
- **IDE**: IntelliJ IDEA (recommended) or Eclipse
- **Git**: For version control

## Building from Source

### Clone Repository

```bash
git clone https://github.com/ksoichiro/ChronoDawn.git
cd ChronoDawn
```

### Build All Loaders

```bash
# Windows
gradlew.bat build

# macOS/Linux
./gradlew build
```

**Output Files**:
- `fabric/build/libs/chronodawn-0.1.0+1.21.1-fabric.jar` - Fabric loader JAR
- `neoforge/build/libs/chronodawn-0.1.0+1.21.1-neoforge.jar` - NeoForge loader JAR
- `common/build/libs/common-0.1.0.jar` - Common module (not usable standalone)

### Build Specific Loader

#### Fabric Only
```bash
# Windows
gradlew.bat :fabric:build

# macOS/Linux
./gradlew :fabric:build
```

#### NeoForge Only
```bash
# Windows
gradlew.bat :neoforge:build

# macOS/Linux
./gradlew :neoforge:build
```

## Development Setup

### Step 1: Import Project to IDE

#### IntelliJ IDEA (Recommended)
1. Open IntelliJ IDEA
2. File → Open → Select `build.gradle` in project root
3. Choose "Open as Project"
4. Wait for Gradle sync to complete

#### Eclipse
1. Open Eclipse
2. File → Import → Existing Gradle Project
3. Select project root directory
4. Click Finish

### Step 2: Run in Development Environment

#### Fabric Development Client
```bash
# Windows
gradlew.bat :fabric:runClient

# macOS/Linux
./gradlew :fabric:runClient
```

#### NeoForge Development Client
```bash
# Windows
gradlew.bat :neoforge:runClient

# macOS/Linux
./gradlew :neoforge:runClient
```

#### Development Server
```bash
# Fabric
./gradlew :fabric:runServer

# NeoForge
./gradlew :neoforge:runServer
```

### Step 3: Verify Setup

Launch the development client and verify:
- [ ] Minecraft starts successfully
- [ ] "Chrono Dawn" appears in mod list
- [ ] Creative inventory includes Chrono Dawn items
- [ ] World generation includes Ancient Ruins structures

## Testing

### Run All Tests
```bash
# Windows
gradlew.bat test

# macOS/Linux
./gradlew test
```

### Run Loader-Specific Tests
```bash
# Fabric tests
./gradlew :fabric:test

# NeoForge tests
./gradlew :neoforge:test
```

### Manual Testing Checklist

1. **Portal System**:
   - [ ] Find Ancient Ruins in Overworld
   - [ ] Craft Time Hourglass
   - [ ] Activate portal and travel to Chrono Dawn
   - [ ] Verify portal is one-way (stops working after entry)

2. **Dimension Features**:
   - [ ] Explore different biomes (Forest, Desert, Mountain, etc.)
   - [ ] Verify hostile mobs have Slowness IV effect
   - [ ] Find Forgotten Library structure

3. **Portal Stabilizer**:
   - [ ] Craft Portal Stabilizer in Chrono Dawn
   - [ ] Stabilize portal for bidirectional travel
   - [ ] Test round-trip travel Overworld ↔ Chrono Dawn

4. **Boss Battles**:
   - [ ] Defeat Time Guardian mini-boss
   - [ ] Explore Phantom Catacombs and defeat Temporal Phantom
   - [ ] Locate Master Clock structure
   - [ ] Defeat Time Tyrant final boss

## Installing Pre-built JAR

### For Fabric
1. Install Minecraft 1.21.1
2. Install Fabric Loader 0.17.3+
3. Download and install Fabric API 0.116.7+
4. Download and install required dependencies:
   - Architectury API 13.0.8+
   - Custom Portal API (Fabric version)
   - Patchouli 1.21.1-92+
5. Copy `chronodawn-0.1.0+1.21.1-fabric.jar` to `.minecraft/mods/` folder
6. Launch Minecraft with Fabric profile

### For NeoForge
1. Install Minecraft 1.21.1
2. Install NeoForge 21.1.209+
3. Download and install required dependencies:
   - Architectury API 13.0.8+
   - Custom Portal API (NeoForge version)
   - Patchouli 1.21.1-92+
4. Copy `chronodawn-0.1.0+1.21.1-neoforge.jar` to `.minecraft/mods/` folder
5. Launch Minecraft with NeoForge profile

**Note**: Download the correct version for your mod loader (Fabric or NeoForge).

## Project Structure

```
ChronoDawn/
├── common/                          # Loader-independent shared code (~80%)
│   ├── src/main/java/com/chronodawn/
│   │   ├── ChronoDawn.java          # Common entry point
│   │   ├── blocks/                  # Custom blocks
│   │   ├── items/                   # Custom items
│   │   ├── entities/                # Custom entities (bosses, mobs)
│   │   ├── worldgen/                # World generation features
│   │   └── registry/                # Registry wrappers
│   └── src/main/resources/
│       ├── data/chronodawn/         # Data packs (recipes, worldgen, structures)
│       └── assets/chronodawn/       # Assets (textures, models, sounds)
├── fabric/                          # Fabric-specific implementation (~10%)
│   ├── src/main/java/com/chronodawn/fabric/
│   │   └── ChronoDawnFabric.java    # Fabric entry point
│   └── src/main/resources/
│       └── fabric.mod.json          # Fabric mod metadata
├── neoforge/                        # NeoForge-specific implementation (~10%)
│   ├── src/main/java/com/chronodawn/neoforge/
│   │   └── ChronoDawnNeoForge.java  # NeoForge entry point
│   └── src/main/resources/
│       └── META-INF/neoforge.mods.toml # NeoForge mod metadata
├── specs/chrono-dawn-mod/           # Design documents
├── build.gradle                     # Root build configuration (Groovy DSL)
├── settings.gradle                  # Multi-module settings
└── gradle.properties                # Version configuration
```

## Documentation

- **Feature Specification**: `specs/chrono-dawn-mod/spec.md`
- **Implementation Plan**: `specs/chrono-dawn-mod/plan.md`
- **Development Guide**: `specs/chrono-dawn-mod/quickstart.md`
- **Data Model**: `specs/chrono-dawn-mod/data-model.md`
- **Research Notes**: `specs/chrono-dawn-mod/research.md`

## Technical Notes

### Build Configuration
- **Build DSL**: Groovy DSL (for Architectury Loom 1.11-SNAPSHOT compatibility)
- **Mappings**: Mojang mappings (official Minecraft class names like `net.minecraft.core.Registry`)
- **Shadow Plugin**: Bundles common module into loader-specific JARs

### Mixin Configuration
- Fabric and NeoForge use **separate** Mixin configuration files
- **Fabric**: `chronodawn-fabric.mixins.json` (with refMap for Intermediary mappings)
- **NeoForge**: `chronodawn-neoforge.mixins.json` (without refMap for Mojang mappings)

See `CLAUDE.md` → "Mixin Configuration" section for details.

## Resources

### Official Documentation
- [Fabric Documentation](https://docs.fabricmc.net/)
- [NeoForge Documentation](https://docs.neoforged.net/)
- [Architectury Documentation](https://docs.architectury.dev/)
- [Minecraft Wiki](https://minecraft.wiki)

### Community
- [Fabric Discord](https://discord.gg/v6v4pMv)
- [NeoForge Discord](https://discord.neoforged.net/)
- [Architectury Discord](https://discord.gg/architectury)

## License

**All Rights Reserved** - Copyright (c) 2025 Soichiro Kashima

This software is protected by copyright. Use, reproduction, modification, and distribution are prohibited without the express permission of the copyright holder.

**Note**: This mod currently depends on Patchouli (CC-BY-NC-SA 3.0 license), which restricts commercial use.

## Credits

- Built with [Architectury](https://github.com/architectury/architectury-api)
- Portal mechanics powered by [Custom Portal API](https://github.com/kyrptonaught/customportalapi)

## Support

For issues, feature requests, or questions:
- Open an issue on [GitHub Issues](https://github.com/ksoichiro/ChronoDawn/issues)
- Check existing documentation in `specs/chrono-dawn-mod/`

---

**Developed for Minecraft Java Edition 1.21.1**
