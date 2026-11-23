# Chrono Dawn - Developer Guide

**Contributing to Chrono Dawn**

This guide provides technical documentation for developers who want to contribute to or extend Chrono Dawn.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Development Environment Setup](#development-environment-setup)
3. [Project Structure](#project-structure)
4. [Build System](#build-system)
5. [Core Systems](#core-systems)
6. [Adding New Features](#adding-new-features)
7. [Testing](#testing)
8. [Debugging](#debugging)
9. [Contributing Guidelines](#contributing-guidelines)
10. [API Reference](#api-reference)

---

## Architecture Overview

### Multi-Loader Architecture (Architectury)

Chrono Dawn uses the **Architectury** framework to support Fabric and NeoForge loaders with a shared codebase, plus a Forge loader for legacy Minecraft 1.20.1 (NeoForge only supports Minecraft 1.20.5+).

**Design Principle**: **80% Common, 20% Loader-Specific**

```
┌─────────────────────────────────────┐
│         Common Module (~80%)        │
│  - Core game logic                  │
│  - Blocks, Items, Entities          │
│  - World generation                 │
│  - Event handlers (Architectury)    │
└─────────────────────────────────────┘
           ↓              ↓
    ┌──────────┐   ┌──────────┐
    │  Fabric  │   │ NeoForge │
    │  (~10%)  │   │  (~10%)  │
    └──────────┘   └──────────┘
```

### Key Technologies

- **Java 21**: Target language version (Java 17 for 1.20.1, Java 25 for 26.1.2/26.2/26.3)
- **Minecraft**: Java Edition 1.20.1 (legacy) / 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4 / 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 / 1.21.9 / 1.21.10 / 1.21.11 / 26.1.2 / 26.2 / 26.3
- **Fabric Loader**: 0.15.11+ (for 1.20.1) / 0.17.3+ (for 1.21.1+) / 0.19.5+ (for 26.1.2/26.2/26.3)
- **Forge**: 47.3.5+ (for 1.20.1 only)
- **NeoForge**: 21.1.209+ (for 1.21.1) / 21.2.0-beta+ (for 1.21.2) / 21.3.0-beta+ (for 1.21.3) / 21.4.4-beta+ (for 1.21.4) / 21.5.96+ (for 1.21.5) / 21.6.20-beta+ (for 1.21.6) / 21.7.25-beta+ (for 1.21.7) / 21.8.0-beta+ (for 1.21.8) / 21.9.16-beta+ (for 1.21.9) / 21.10.64+ (for 1.21.10) / 21.11.38-beta+ (for 1.21.11) / 26.1.2.103+ (for 26.1.2) / 26.2.0.82+ (for 26.2) / 26.3.0.89+ (for 26.3)
- **Architectury API**: 9.2.14+ (for 1.20.1) / 13.0.8+ (for 1.21.1) / 14.0.4+ (for 1.21.2/1.21.3) / 15.0.1+ (for 1.21.4) / 16.1.4+ (for 1.21.5) / 17.0.6+ (for 1.21.6) / 17.0.8+ (for 1.21.7/1.21.8) / 18.0.3+ (for 1.21.9) / 18.0.8+ (for 1.21.10) / 19.0.1+ (for 1.21.11) / 20.0.12+ (for 26.1.2) / 21.1.9+ (for 26.2) / 22.0.4+ (for 26.3)
- **Gradle**: Build automation (Groovy DSL)
- **Mojang Mappings**: Official Minecraft class names

*Note: 1.21.3 is a hotfix release that shares code modules with 1.21.2. Minecraft 1.20.1 is a legacy target that predates NeoForge (which requires 1.20.5+), so it uses Forge instead; it has no GameTest coverage since Forge has no standard GameTest integration in this project.*

---

## Development Environment Setup

### Prerequisites

1. **Java Development Kit (JDK) 21**
   - Download: [Microsoft OpenJDK 21](https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21)
   - Verify: `java -version` should show 21.x.x

2. **IntelliJ IDEA** (recommended) or Eclipse
   - [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/)
   - Install Minecraft Development plugin (optional but helpful)

3. **Git**
   - [Download Git](https://git-scm.com/downloads)

### Clone and Setup

```bash
# Clone repository
git clone https://github.com/ksoichiro/ChronoDawn.git
cd ChronoDawn

# Build project (downloads dependencies)
./gradlew build

# Import to IntelliJ IDEA
# File → Open → Select build.gradle → Open as Project
```

### IDE Configuration

#### IntelliJ IDEA

1. **Import Project**:
   - File → Open → Select `build.gradle`
   - Choose "Open as Project"
   - Wait for Gradle sync

2. **Set Project SDK**:
   - File → Project Structure → Project
   - SDK: Java 21
   - Language Level: 21

3. **Enable Annotation Processing**:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

4. **Run Configurations**:
   - Gradle tasks are auto-detected
   - Use `:fabric:runClient -Ptarget_mc_version=1.21.2` or `:neoforge:runClient -Ptarget_mc_version=1.21.2` to launch development game

#### Eclipse

1. **Import Gradle Project**:
   - File → Import → Existing Gradle Project
   - Select project root directory

2. **Configure Java Compiler**:
   - Project Properties → Java Compiler
   - Compiler compliance level: 21

---

## Project Structure

```
ChronoDawn/
├── common/
│   ├── shared/                             # Shared version-agnostic sources (included via srcDir)
│   ├── shared-1.21.1+/                    # Shared resources for 1.21.1~26.3
│   ├── shared-1.21.2+/                    # Shared resources for 1.21.2~26.3
│   ├── shared-1.21.5+/                    # Shared resources for 1.21.5~26.3
│   ├── gametest/                           # Shared gametest sources (included via srcDir)
│   ├── 1.21.2/                             # Common module for MC 1.21.2
│   ├── 1.21.11/                            # Common module for MC 1.21.11
│   ├── 26.1.2/                             # Common module for MC 26.1.2
│   ├── 26.3/                               # Common module for MC 26.3
│   ├── 26.2/                               # Common module for MC 26.2 (~80% of code)
│   │   ├── src/main/java/com/chronodawn/
│   │   │   ├── ChronoDawn.java             # Common entry point
│   │   │   ├── blocks/                     # Custom blocks
│   │   │   │   ├── ModBlocks.java          # Block registration
│   │   │   │   ├── ReversingTimeSandstone.java
│   │   │   │   └── UnstableFungus.java
│   │   │   ├── items/                      # Custom items
│   │   │   │   ├── ModItems.java           # Item registration
│   │   │   │   ├── TimeHourglassItem.java
│   │   │   │   └── artifacts/              # Ultimate artifacts
│   │   │   ├── entities/                   # Custom entities
│   │   │   │   ├── ModEntities.java        # Entity registration
│   │   │   │   └── bosses/                 # Boss entities
│   │   │   │       ├── TimeGuardianEntity.java
│   │   │   │       ├── TemporalPhantomEntity.java
│   │   │   │       └── TimeTyrantEntity.java
│   │   │   ├── worldgen/                   # World generation
│   │   │   │   ├── biomes/                 # Custom biomes
│   │   │   │   ├── features/               # Worldgen features
│   │   │   │   └── structures/             # Structure generation
│   │   │   ├── events/                     # Event handlers
│   │   │   │   ├── EntityEventHandler.java # Time Distortion effect
│   │   │   │   └── PlayerEventHandler.java
│   │   │   ├── registry/                   # Architectury registries
│   │   │   │   └── ModRegistries.java
│   │   │   ├── platform/                   # @ExpectPlatform methods
│   │   │   │   └── PlatformHelper.java
│   │   │   ├── mixin/                      # Mixin classes
│   │   │   │   └── StructureStartMixin.java # Waterlogging prevention
│   │   │   └── util/                       # Utility classes
│   │   ├── src/main/resources/
│   │   │   ├── data/chronodawn/
│   │   │   │   ├── dimension/              # Dimension JSON
│   │   │   │   ├── worldgen/               # Biomes, features, structures
│   │   │   │   ├── recipes/                # Crafting recipes
│   │   │   │   ├── loot_tables/            # Loot tables
│   │   │   │   └── structures/             # NBT structure files
│   │   │   └── assets/chronodawn/
│   │   │       ├── textures/               # Textures (blocks, items, entities)
│   │   │       ├── models/                 # Block/item models
│   │   │       ├── blockstates/            # Block state definitions
│   │   │       ├── lang/                   # Translations
│   │   │       └── sounds/                 # Sound files
│   │   └── src/test/java/com/chronodawn/  # JUnit tests
│   ├── 1.21.1/                             # Common module for MC 1.21.1
│   └── 1.20.1/                             # Common module for MC 1.20.1
├── fabric/
│   ├── base/                               # Shared Fabric sources (NOT a Gradle subproject)
│   │   └── src/main/java/com/chronodawn/fabric/
│   │       ├── event/                      # Event handlers
│   │       └── platform/                   # Platform implementations
│   ├── 1.20.1/                             # Fabric 1.20.1 subproject
│   ├── 1.21.1/                             # Fabric 1.21.1 subproject
│   └── 1.21.2/                             # Fabric 1.21.2 subproject
│       ├── src/main/java/com/chronodawn/fabric/
│       │   ├── ChronoDawnFabric.java       # Fabric entry point (version-specific)
│       │   └── client/                     # Client-side initialization
│       └── src/main/resources/
│           ├── fabric.mod.json             # Fabric mod metadata
│           └── chronodawn-fabric.mixins.json # Fabric Mixin config
├── neoforge/
│   ├── base/                               # Shared NeoForge sources (NOT a Gradle subproject)
│   │   └── src/main/java/com/chronodawn/neoforge/
│   │       ├── ChronoDawnNeoForge.java     # NeoForge entry point
│   │       ├── client/                     # Client-side initialization
│   │       └── platform/                   # Platform implementations
│   ├── 1.21.1/                             # NeoForge 1.21.1 subproject
│   └── 1.21.2/                             # NeoForge 1.21.2 subproject
│       ├── src/main/java/com/chronodawn/neoforge/
│       │   └── mixin/                      # Version-specific mixins
│       └── src/main/resources/
│           └── META-INF/neoforge.mods.toml # NeoForge mod metadata
├── forge/
│   ├── base/                               # Shared Forge sources (NOT a Gradle subproject)
│   │   └── src/main/java/com/chronodawn/forge/
│   │       ├── ChronoDawnForge.java        # Forge entry point
│   │       ├── client/                     # Client-side initialization
│   │       └── platform/                   # Platform implementations
│   └── 1.20.1/                             # Forge 1.20.1 subproject (legacy loader, no NeoForge < 1.20.5)
│       ├── src/main/java/com/chronodawn/forge/
│       │   └── mixin/                      # Version-specific mixins
│       └── src/main/resources/
│           └── META-INF/mods.toml          # Forge mod metadata
├── docs/                                   # Documentation
│   ├── player_guide.md                     # Player guide
│   ├── developer_guide.md                  # This file
│   └── archive/                            # Historical designs and research
├── build.gradle                            # Root build configuration
├── settings.gradle                         # Multi-module settings
├── gradle.properties                       # Version configuration
└── CLAUDE.md                               # Development guidelines
```

---

## Build System

### Gradle Configuration (Groovy DSL)

**Important**: This project uses **Groovy DSL**, not Kotlin DSL, for Architectury Loom 1.17.491 compatibility.

#### Root `build.gradle`

```groovy
plugins {
    id 'dev.architectury.loom' version '1.17.491' apply false
    id 'architectury-plugin' version '3.5-SNAPSHOT'
    id 'com.gradleup.shadow' version '8.3.6' apply false
}

architectury {
    minecraft = project.minecraft_version
}
```

#### `gradle.properties`

```properties
mod_version=0.9.0
maven_group=com.chronodawn
archives_name=chronodawn
minecraft_version=1.21.2
architectury_api_version=14.0.4
fabric_loader_version=0.17.3
fabric_api_version=0.116.7+1.21.2
neoforge_version=21.2.0-beta
```

### Build Commands

```bash
# Build for default version (26.3)
./gradlew build

# Build for a specific Minecraft version
./gradlew build -Ptarget_mc_version=26.2
./gradlew build -Ptarget_mc_version=26.1.2
./gradlew build -Ptarget_mc_version=1.21.11
./gradlew build -Ptarget_mc_version=1.21.5
./gradlew build -Ptarget_mc_version=1.21.2
./gradlew build -Ptarget_mc_version=1.21.1
./gradlew build -Ptarget_mc_version=1.20.1

# Build all supported versions at once
./gradlew buildAll

# Shorthand for specific versions
./gradlew build1_20_1
./gradlew build1_21_1
./gradlew build1_21_5
./gradlew build1_21_11
./gradlew build26_1_2
./gradlew build26_2

# Build specific module
./gradlew :fabric:build -Ptarget_mc_version=26.2
./gradlew :neoforge:build -Ptarget_mc_version=26.2
./gradlew :fabric:build -Ptarget_mc_version=1.21.11
./gradlew :neoforge:build -Ptarget_mc_version=1.21.11
./gradlew :forge:build -Ptarget_mc_version=1.20.1

# Run development client (version-specific)
./gradlew :fabric:runClient -Ptarget_mc_version=26.2
./gradlew :fabric:runClient -Ptarget_mc_version=1.21.5
./gradlew :fabric:runClient -Ptarget_mc_version=1.21.2
./gradlew :fabric:runClient -Ptarget_mc_version=1.21.1
./gradlew :fabric:runClient -Ptarget_mc_version=1.20.1
./gradlew :neoforge:runClient -Ptarget_mc_version=26.2
./gradlew :neoforge:runClient -Ptarget_mc_version=1.21.5
./gradlew :neoforge:runClient -Ptarget_mc_version=1.21.1
./gradlew :forge:runClient -Ptarget_mc_version=1.20.1
# Or shortcut command:
./gradlew runClientForge1_20_1

# Run development server
./gradlew :fabric:runServer -Ptarget_mc_version=26.2
./gradlew :neoforge:runServer -Ptarget_mc_version=26.2

# Run unit tests
./gradlew test
./gradlew :common-26.2:test -Ptarget_mc_version=26.2

# Run GameTests (in-game integration tests)
./gradlew :fabric:runGameTest -Ptarget_mc_version=26.2
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.5
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.2
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1
./gradlew gameTestAll   # All versions and loaders (fully parallel)

# Validate asset files (JSON syntax + blockstate/model/texture cross-references)
./gradlew validateResources

# Validate data-pack cross-references (tag entries → registered IDs,
# recipe references, 1.21.4+ client item coverage)
./gradlew validateData

# Validate translation keys across versions (entities, spawn eggs)
./gradlew validateTranslations

# Project-specific validations (lang parity, block tag membership,
# recipe-unlock advancements, per-era JSON format, minecraft: tag parity
# between 1.20.1 and 1.21.1+, display names for every block and item ID,
# an icon texture for every mob effect)
# Item coverage checks the registered item or block description prefix for each version.
./gradlew validateLangParity validateBlockTagMembership validateRecipeUnlockAdvancements validateEraJsonFormat validateEraTagParity validateRegistryLangCoverage validateMobEffectIcons

# Verify conventional (c:) tags: coverage of every material the rules match,
# tag-name validity per era, cross-root consistency, and that every tag path
# this mod coined has a tag.item.* display name in en_us and ja_jp
./gradlew validateConventionalTags

# Run every validation above, plus validateSoundSubtitles, in one invocation.
# This is the first CI job (.github/workflows/ci.yml)
./gradlew validateAll

# CI passes this flag so that only en_us is required. Missing keys in other
# locales (ja_jp included) become warnings. Without it, ja_jp is required too,
# which is what checkAll and releases use (lang_required_locales in gradle.properties)
./gradlew validateAll -Plang_required_locales=en_us

# Skip the version subprojects (no Loom or Minecraft setup). Only the
# root-project validation tasks work in this mode
./gradlew validateAll -Pvalidation_only=true

# Collect release JARs into build/release/
./gradlew collectJars

# Full release pipeline (cleanAll → buildAll → collectJars)
./gradlew release

# Run ALL verification tasks (recommended before commits/PRs)
./gradlew checkAll
# Runs in sequence: cleanAll → validateResources → validateTranslations
#   → project-specific validations + validateData + validateConventionalTags
#     (checkall_extra_tasks)
#   → buildAll (with unit tests) → gameTestAll

# Clean build artifacts
./gradlew clean
```

### Output Files

After building:
- **Fabric JAR**: `fabric/26.2/build/libs/chronodawn-0.9.0+26.2-fabric.jar`
- **NeoForge JAR**: `neoforge/26.2/build/libs/chronodawn-0.9.0+26.2-neoforge.jar`
- **Common JAR**: `common/26.2/build/libs/common-26.2-0.9.0.jar` (bundled into loader JARs)
- **Forge JAR** (1.20.1 only): `forge/1.20.1/build/libs/chronodawn-0.9.0+1.20.1-forge.jar`

---

## Core Systems

### 1. Dimension System

**Location**: `common/1.21.11/src/main/java/com/chronodawn/core/dimension/`

**Key Classes**:
- `ChronoDawnDimension.java`: Dimension registration and key definitions
- Custom biomes in `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/`

**Dimension JSON**: `common/1.21.11/src/main/resources/data/chronodawn/dimension/chronodawn_dimension.json`

```json
{
  "type": "chronodawn:chronodawn_dimension_type",
  "generator": {
    "type": "minecraft:noise",
    "biome_source": {
      "type": "minecraft:multi_noise",
      "biomes": [...]
    }
  }
}
```

### 2. Portal System

**Location**:
- Common: `common/1.21.11/src/main/java/com/chronodawn/core/portal/`
- Blocks: `common/1.21.11/src/main/java/com/chronodawn/blocks/ChronoDawnPortalBlock.java`

**Key Components**:
- **PortalRegistry**: Tracks portal states (INACTIVE, ACTIVE, STABILIZED)
- **ChronoDawnPortalBlock**: Custom portal block implementation
- **PortalTeleportHandler**: Handles teleportation logic between dimensions
- **TimeHourglassItem**: Portal ignition logic
- **PortalStabilizerItem**: Dimension stabilization logic

**Portal Color**: Orange (#db8813 - RGB: 219, 136, 19)

### 3. Time Distortion Effect

**Location**: `common/1.21.11/src/main/java/com/chronodawn/events/EntityEventHandler.java`

**Implementation**:
```java
public class EntityEventHandler {
    public static void onEntityTick(ServerLevel level, Entity entity) {
        if (level.dimension().equals(ChronoDawnDimension.DIMENSION_KEY)) {
            if (entity instanceof LivingEntity living && isHostileMob(living)) {
                // Apply Slowness IV to hostile mobs
                living.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, false
                ));
            }
        }
    }
}
```

**Optimization**: Check every 5 ticks instead of every tick (T178)

### 4. Worldgen System

Biome placement is shared across versions in
`common/shared/src/main/resources/data/chronodawn/dimension/chronodawn.json`.
Snowy regions use temperature ranges `[-1.0, -0.55]` for dry climates and
`[-1.0, -0.4]` for wet climates. Plains, prairies, and forests extend down to
`-0.4` to reduce the area occupied by snow. These climate ranges are not area
percentages or biome size limits. Check several seeds in newly generated terrain
when evaluating their visual balance.

**Biomes**: `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/`
- 11 custom biomes with unique features

**Structures**: `common/1.21.11/src/main/java/com/chronodawn/worldgen/structures/`
- Ancient Ruins (Overworld)
- Forgotten Library (Chrono Dawn)
- Master Clock (final dungeon)
- Phantom Catacombs (maze with boss)

**Structure NBT Files**: `common/1.21.11/src/main/resources/data/chronodawn/structure/`

### 5. Boss System

**Entities**: `common/1.21.11/src/main/java/com/chronodawn/entities/bosses/`

**Boss AI Components**:
- **TimeGuardianAI**: AI state machine for Time Guardian
- **TemporalPhantomAI**: Phantom boss AI with phase transitions
- **TimeTyrantAI**: Final boss AI with multiple phases

**Boss Spawning**: See `PhantomCatacombsBossRoomPlacer.java` for programmatic boss room placement

### 6. Chronicle Guidebook UI

**Location**: `common/1.21.11/src/main/java/com/chronodawn/client/gui/screens/chronicle/`

**Description**: Custom in-game guidebook system that replaced Patchouli dependency. Chronicle provides players with information about biomes, structures, bosses, items, and progression.

**Key Components**:
- **ChronicleScreen.java**: Main screen with category sidebar and entry display
- **ChronicleData.java**: JSON data loader for guidebook content
- **ChronicleBookItem.java**: Item that opens the Chronicle GUI

**Data Files**: `common/1.21.11/src/main/resources/assets/chronodawn/chronicle/`
- **categories.json**: Category definitions
- **entries.json**: Entry data with localized text

**Features**:
- Bilingual support (English/Japanese)
- Category-based navigation
- Scrollable entry list and content area
- Automatic distribution when entering Chrono Dawn dimension

**Adding New Entries**:

1. Add entry data to `entries.json`:
```json
{
  "id": "new_entry",
  "category": "progression",
  "icon": "chronodawn:item/example_item",
  "title": {
    "en_us": "Entry Title",
    "ja_jp": "エントリータイトル"
  },
  "content": {
    "en_us": "Entry content...",
    "ja_jp": "エントリー内容..."
  }
}
```

2. Reference from code:
- Chronicle data is loaded automatically when screen opens
- No code changes needed for new entries (data-driven)

### 7. Custom Shields

Three tiered ShieldItem subclasses exist per MC version in `common/<ver>/src/main/java/com/chronodawn/items/` (ClockstoneShieldItem, EnhancedClockstoneShieldItem, EntropyCrystalShieldItem).

Shared tier data and effect helpers live in `common/shared/src/main/java/com/chronodawn/items/shield/`:
- `ChronoShieldTier` — enum with durability, axe-disable window, block-delay seconds, and per-tier effect flags
- `ChronoShieldMarker` — marker interface so Mixins can `instanceof`-check our shields regardless of subclass
- `ChronoShieldEffectHandler` — static helpers: `maybeShortenDebuff`, `onBlockSuccess`, `tryGenerateEcho`, `tryConsumeEcho`

The handler has version-specific copies for APIs that diverged:
- `common/1.21.11/` — uses `Identifier` and new `MobEffects` names (`SLOWNESS`)
- `common/1.21.{1,2,4}/` — uses `ResourceLocation` and old `MobEffects` names (`MOVEMENT_SLOWDOWN`)
- `common/1.20.1/` — adds raw `MobEffect` (not `Holder<MobEffect>`) support

Three Mixins per version implement the effects:
- `ChronoShieldEffectInterceptorMixin` — halves time-themed debuff durations via `LivingEntity.addEffect` `@ModifyVariable` HEAD
- `ChronoShieldBlockingMixin` — detects block success. 1.21.5+ targets `LivingEntity.applyItemBlocking`; 1.21.4 and earlier target `LivingEntity.hurtCurrentlyUsedShield`
- `ChronoShieldDamageMixin` — consumes Time Echo on incoming damage. 1.21.2+ targets `Player.hurtServer`; 1.21.1 and 1.20.1 target `Player.hurt`

Player-bound cooldown state lives in `PlayerProgressData` rather than ItemStack NBT to prevent hot-swap exploits and avoid cross-version NBT↔DataComponent divergence:
- `shieldSpeedCooldownEndTick` — #7 Speed-on-block CD
- `shieldEchoActiveUntilTick` — #12 Time Echo active window
- `shieldEchoCooldownEndTick` — #12 Time Echo CD

Effect #1 (3-tick raise) is implemented via `BLOCKS_ATTACKS.block_delay_seconds = 0.15F` on 1.21.5+. Pre-1.21.5 versions fall back to vanilla's 5-tick raise (no data-component equivalent).

Time Echo visuals (particle + sound, 3 stages) use a custom `ChronoShieldEchoParticle`. The particle class has per-era variants: `SingleQuadParticle`-based for 1.21.9+ and `TextureSheetParticle`-based for 1.21.5 - 1.21.8 and 1.20.1.

Historical design:
- Design: `docs/archive/superpowers/2026-04-19-chronodawn-custom-shields-design.md`

---

## Adding New Features

### Adding a New Block

1. **Create Block Class** (`common/1.21.11/src/main/java/com/chronodawn/blocks/MyCustomBlock.java`):
```java
public class MyCustomBlock extends Block {
    public MyCustomBlock(Properties properties) {
        super(properties);
    }

    // Override methods as needed
}
```

2. **Register Block** (`ModBlocks.java`):
```java
public static final RegistrySupplier<Block> MY_CUSTOM_BLOCK = BLOCKS.register(
    "my_custom_block",
    () -> new MyCustomBlock(BlockBehaviour.Properties.of()
        .strength(3.0f)
        .sound(SoundType.STONE))
);
```

3. **Add Blockstate JSON** (`assets/chronodawn/blockstates/my_custom_block.json`)

4. **Add Model JSON** (`assets/chronodawn/models/block/my_custom_block.json`)

5. **Add Texture** (`assets/chronodawn/textures/block/my_custom_block.png`)

6. **Add Translation** (`assets/chronodawn/lang/en_us.json`):
```json
{
  "block.chronodawn.my_custom_block": "My Custom Block"
}
```

### Adding a New Item

1. **Create Item Class** (if custom behavior needed)
2. **Register Item** (`ModItems.java`)
3. **Add Model JSON** (`assets/chronodawn/models/item/my_item.json`)
4. **Add Texture** (`assets/chronodawn/textures/item/my_item.png`)
5. **Add Translation**

### Adding a New Entity

1. **Create Entity Class** (`common/1.21.11/src/main/java/com/chronodawn/entities/MyEntity.java`):
```java
public class MyEntity extends Mob {
    public MyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    // Implement AI, attributes, etc.
}
```

2. **Register Entity** (`ModEntities.java`):
```java
public static final RegistrySupplier<EntityType<MyEntity>> MY_ENTITY = ENTITIES.register(
    "my_entity",
    () -> EntityType.Builder.of(MyEntity::new, MobCategory.CREATURE)
        .sized(0.6f, 1.8f)
        .build("my_entity")
);
```

3. **Add Renderer** (client-side, loader-specific)
4. **Add Model and Texture**
5. **Add Spawn Rules** (if applicable)

### Adding a Structure

1. **Create NBT Structure** using Minecraft structure blocks
2. **Save to**: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/`
3. **Define Structure JSON**: `data/chronodawn/worldgen/structure/my_structure.json`
4. **Add to Structure Set**: `data/chronodawn/worldgen/structure_set/my_structure_set.json`
5. **Implement Structure Processor** (if custom logic needed)
6. **Keep the terrain around surface structures**: a structure block saves the
   air around the building, and a rigid template with terrain adaptation places
   that air over the terrain. Run `python3 scripts/strip_exterior_air.py <file>.nbt`
   to remove the air outside the building, above its roof and under high
   overhangs (see the script's docstring for the rule), and add the
   structure to `TERRAIN_PRESERVING` in `StructureTemplateTest`. Run it again
   every time the template is saved from a structure block. For the Sentinel
   Outpost, run `python3 scripts/finish_sentinel_outpost.py` instead. It also
   writes the spawner settings, empties the chests, and checks that every
   spawner is enclosed by Reversing Time Sandstone and that no black concrete
   is left
7. **Weather the template**: `python3 scripts/weather_structures.py` swaps a
   seeded share of the exposed Temporal Stone Bricks for cracked and mossy
   variants. Add the template and its rates to `RATES` in the script. The
   result depends only on the template name and block position, so running it
   again changes nothing. Run it after `strip_exterior_air.py`

---

## Testing

### Unit Testing

**Framework**: JUnit 5

**Location**: `common/1.21.11/src/test/java/com/chronodawn/unit/`

**Test Types**:

- **ResourceValidationTest** — Uses `@TestFactory` to generate dynamic tests that verify the following without launching a Minecraft server:
  - Blockstate JSON existence for all blocks
  - Item model JSON existence for all items
  - Translation key completeness (`en_us.json`)

  Parses source files to extract `RegistrySupplier` field names and validates resource existence via `ClassLoader.getResource()` (~0.1s for 402 tests).

- **StructureTemplateTest** — Checks structure NBT dimensions, required block counts, camp chest headroom,
  disallowed placeholders, air above the roofs of surface structures and specification coverage.
  Minecraft 1.20.1 uses its converted resources.
  GameTest separately verifies that the server loads each template.

**Run Tests**:
```bash
./gradlew :common-1.21.11:test
```

### Integration Testing (GameTest)

**Framework**: Minecraft GameTest Framework with registry-driven test generation

**Location**:
- `common/1.21.11/src/main/java/com/chronodawn/gametest/` (1.21.11)
- `common/1.21.5/src/main/java/com/chronodawn/gametest/` (1.21.5)
- `common/1.21.2/src/main/java/com/chronodawn/gametest/` (1.21.2)
- `common/1.21.1/src/main/java/com/chronodawn/compat/v1_21_1/gametest/` (1.21.1)
- `common/1.20.1/src/main/java/com/chronodawn/compat/v1_20_1/gametest/` (1.20.1)

**Test Categories** (auto-generated from registries):
- **Block placement** — Verifies all blocks can be placed
- **Entity spawning** — Verifies all entities can be spawned
- **Tool durability / Armor defense** — Validates equipment properties
- **Registry ID consistency** — Field names match registry IDs
- **Food properties / Equipment stack size** — Property validation
- **Boss fight tests** — Boss encounter mechanics
- **Conventional (`c:`) tag membership** — Verifies the published `c:` tags
  resolve at runtime and carry their members, which the build-time
  `validateConventionalTags` cannot know. Vanilla-material ores are asserted
  against their own subtag (`c:ores/coal`) rather than the `c:ores` umbrella:
  the loaders do not reference those subtags from the umbrella on 1.21.2
  through 1.21.5. See `common/gametest/.../ConventionalTagTests.java`

> **Note**: Blockstate/model/translation existence tests have been migrated to JUnit (see Unit Testing above).

**Run GameTests**:
```bash
# Run for a specific version
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.5
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.2
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.11
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.5
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.2
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1

# Run for all versions and loaders (fully parallel)
./gradlew gameTestAll
```

### gameTestAll Architecture

`gameTestAll` runs 5 configurations (fabric×3 versions + neoforge×2 versions) grouped by version.

**Parallelization strategy**:

Configurations are grouped by Minecraft version. Each version group runs as a single Gradle process (with `--parallel`), and different versions run in parallel:
```
Thread 1: :fabric:runGameTest (1.20.1)                          ║
Thread 2: :fabric:runGameTest + :neoforge:runGameTestServer (1.21.1)  ║  3 versions in parallel
Thread 3: :fabric:runGameTest + :neoforge:runGameTestServer (1.21.2)  ║
```

Within each version group, fabric and neoforge share the same common module build, so they run in a single Gradle invocation to avoid concurrent build output conflicts.

**How it works**:

Each version uses a separate directory (e.g., `fabric/1.21.2/`) via `project(':fabric').projectDir = file("fabric/1.21.2")` in settings.gradle. This gives each version its own `.gradle/architectury/` directory and build output.

Each subprocess uses `--project-cache-dir .gradle/build-<version>` to avoid lock conflicts with the outer Gradle process and other subprocesses. `buildAll` uses the same per-version directory, and the two phases run one after the other, so `gameTestAll` reuses the task history and Loom cache that `buildAll` left behind. `org.gradle.caching=true` (set in `gradle.properties`) still lets a phase restore `compileJava` output from the Gradle build cache (`FROM-CACHE`) when the task history does not match.

**Disk usage of `.gradle/`**: Because the common modules declare an access widener, Loom treats the remapped Minecraft jars as project-specific. It stores them under `<project-cache-dir>/loom-cache/minecraftMaven` instead of the shared `~/.gradle/caches/fabric-loom/minecraftMaven`. This takes about 100 to 230 MB per Minecraft version, or about 2.9 GB per checkout after `checkAll`. Before both phases shared `.gradle/build-<version>`, `gameTestAll` kept a second copy in `.gradle/gametest-<version>`, so a checkout reached about 5.7 GB. Delete leftover `.gradle/gametest-*` directories from older checkouts by hand. Single-version commands without `--project-cache-dir` use `.gradle/loom-cache`, which keeps jars for every version and access widener revision that ran in that checkout. Delete it when it grows large. The next build recreates the jars, which takes a few extra minutes.

These caches are not shared across worktrees. Pointing several checkouts at one `--project-cache-dir` would also share Gradle's file hashes, task history, and stale output records, which Gradle expects to belong to a single build.

### Running verification in parallel

Several worktrees (for example one per agent) can verify at the same time.

**Ports**: `scenarioCheck` and `upgradeTest` reserve a free server/RCON pair in 25700-25999 when they start. Each reserved port has a lock file under `~/.cache/chronodawn/verify-locks` (override the directory with `CHRONODAWN_VERIFY_LOCK_DIR`). The lock goes away with the process, so a crashed run leaves nothing behind. `-Pupgrade_test_ports` and `-Pscenario_check_ports` still pin the ports and skip the automatic choice. `prodSmokeTest` and the GameTests keep their own ports.

**Limit**: run at most 2 verifications at once on a machine (`checkAll`, `scenarioCheck`, `upgradeTest`). The `scenarioCheck` and `upgradeTest` drivers hold one of N slot locks for the whole run and wait when none is free. Set `CHRONODAWN_MAX_PARALLEL_VERIFY` (for example in `~/.zshrc`) to change N on a machine with more or less memory and disk. `checkAll` does not take a slot, so start it only when fewer than N verifications are running.

**Scope before a PR**: run `checkAll` for all versions. Limit it to the changed versions (`build<version>` and `:fabric:runGameTest` / `:neoforge:runGameTestServer` with `-Ptarget_mc_version=<v>`) only when the impact is clearly confined to them, and state that in the PR body.

**Disk per worktree** (measured on 2026-11-23 in a checkout after `checkAll`):

| Item | Size | Shared between worktrees |
|---|---|---|
| `.gradle/` (Loom caches and task history, about 100 to 230 MB per version) | about 2.9 GB | No (see above) |
| `build/` and per-module `build/` directories | about 0.4 GB and up | No |
| `run-prod/` (`cache/` holds Minecraft assets, libraries and installers, `instances/` holds `runProd` worlds) | about 3.1 GB | Opt in with `prod_base_dir` |
| `run-prod/cache/upgrade-test/` (old mod JARs, Python venv) | about 0.1 GB | Opt in with `prod_base_dir` |

Plan for roughly 6.5 GB or more per worktree that has run `checkAll` and a production run. To share the production runtime cache, set `prod_base_dir=<absolute path>` (for example `/Users/<you>/.cache/chronodawn/run-prod`) in `~/.gradle/gradle.properties`. This moves `cache/` and `instances/`, so `runProd` worlds become shared too. Run one production task in a single checkout first to fill the cache before starting parallel runs, because concurrent first-time downloads into the same directory are untested. Loom and `.gradle/` stay per checkout.

Shared sources are included via `srcDir` references to `fabric/base/` and `neoforge/base/` directories (which are source directories, not Gradle subprojects).

**Log files**: Each version group's output is saved to `build/gametest-<version>.log`.

### Resource Validation

**Tool**: Gradle `validateResources` task

**Location**: `gradle/resource-validation.gradle`

Validates data/asset file integrity at build time without launching the game:

- **JSON syntax check** — Parses all `.json` files under `common/1.21.11/src/main/resources/`
- **Blockstate → model reference check** — Verifies `"model": "chronodawn:block/<name>"` targets exist
- **Model → texture reference check** — Verifies `"textures"` entries with `chronodawn:` prefix have matching `.png` files

```bash
./gradlew validateResources
```

`./gradlew validateAll -Pvalidation_only=true` runs every file-only validation that CI and `checkAll` run, in a few seconds. [Bug Prevention Checks](bug_prevention.md) lists which past bug classes each validation covers, how to update `gradle/version-sync.lock` when per-version copies are meant to differ, and how to install the pre-commit hook (`scripts/hooks/install.sh`, Git 2.54+).

### Manual Testing

1. **Launch Development Client**:
   ```bash
   ./gradlew :fabric:runClient -Ptarget_mc_version=1.21.11
   ```

2. **Create Test World**: Creative mode recommended

3. **Test the changed behavior** in the test world and check the client log.

### Production Smoke Test (headless)

Recommended after Mixin config changes or when adding a new Minecraft
version. Boots a real, installer-provisioned dedicated server (not Loom's
dev/mapped environment) with the built mod and fails if it doesn't create a
world within 5 minutes. No display required.

```bash
./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=1.21.11
./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=1.21.11
```

The Fabric and NeoForge smoke tests use ports 25601 and 25602 respectively.
Run them as separate Gradle invocations when a built NeoForge JAR matters:
in one invocation both loaders transform the shared common module at the same
time, and the NeoForge JAR once resolved `@ExpectPlatform` calls to the Fabric
implementation.

CI runs this for Fabric and NeoForge on 1.21.1 and 26.3 in the `prod-smoke`
job of every pull request. When it fails, the job's last step prints the tail
of each server log. The job downloads Minecraft and the loader installers at
run time and writes `eula=true`, so the person who runs it accepts the
Minecraft EULA. Do not upload the server JAR, mappings or world data as
artifacts.

CI caches the Gradle home with `setup-gradle`, saved only by runs on `main`.
On 2026-11-23 the repository held 12.2 GB of Actions cache (limit 10 GB, 73
entries). Each push to `main` saved about 400 MB per job and Minecraft
version, six entries in all. Only the `build` job saves now, and `prod-smoke`
and `gametest` read it (`cache-read-only`). Check the usage with
`gh api repos/ksoichiro/ChronoDawn/actions/cache/usage` and revisit the cache
settings when it passes 8 GB.

Not supported for Forge (1.20.1) — see "Mixin Configuration" below. Uses
`build/prod-smoke/<loader>-<version>/`, separate from the manually-verified
`run-prod/` used by `runProd`.

The test also fails when the server log has a WARN, ERROR, or FATAL line
that no pattern in `prod-smoke-log-ignore.txt` matches
(`prod_smoke_log_check=true` in `gradle.properties`). This catches problems
that only show up as log warnings, such as NeoForge's biome mob category
mismatch. Fix the cause where you can. Add a commented regex to the ignore
file only for known issues, and remove it together with the fix. Pass
`-Pprod_smoke_log_check=false` to skip the check for a single run.

### Disk space with parallel work

Plan for 2 concurrent checkouts (main plus one worktree, or two worktrees).
Use 3 only when at least 25 GB is free. Each checkout costs 3 to 5 GB after
heavy tests, on top of about 76 GB of shared Gradle caches.
[Disk Usage with Parallel Work](disk_usage.md) has the measurements and the
reasons.

- When a task ends, remove its worktree with `git worktree remove <path>` and
  make sure the directory is gone. Delete empty directories that
  `git worktree list` does not show.
- Run only one heavy task at a time across all checkouts (`checkAll`,
  `gameTestAll`, `upgradeTest`, `scenarioCheck`, multi-version
  `prodSmokeTest`).
- Clean up when free space drops below 20 GB. `checkAll`, `gameTestAll`,
  `upgradeTest`, `scenarioCheck` and `prodSmokeTest` check this themselves
  (see the next section). With 3 concurrent checkouts, keep at least 25 GB
  free. The check does not know how many checkouts run, so watch
  `df -h /` yourself in that case.
- After a heavy task, run `./gradlew clean<version>` or `cleanAll` and delete
  `build/upgrade-test` and `build/scenario-check` server copies once the
  report is saved.
- Shorten the Gradle build cache retention to release about 10 GB. Put this
  in `~/.gradle/init.d/build-cache-retention.gradle`:

  ```groovy
  beforeSettings { settings ->
      settings.caches.buildCache.setRemoveUnusedEntriesAfterDays(2)
  }
  ```

### Free disk space check

`checkAll`, `gameTestAll`, `upgradeTest`, `scenarioCheck` and `prodSmokeTest`
fail before any task runs when the volume of the checkout has less free space
than `min_free_disk_gb` (20 GB by default, set in `gradle.properties`). The
check only asks the file system for the free space, so it adds no noticeable time.
It runs after Gradle configures the projects, because it needs the task graph
to know whether a heavy task was requested.

```
Not enough free disk space to start :fabric:prodSmokeTest.
  Free on the volume of /path/to/checkout: 13.3 GB
  Required (min_free_disk_gb): 20 GB
```

When it fails, free space with the steps the message lists:
`./gradlew clean<version>` or `cleanAll`, `git worktree remove <path>` for
finished worktrees, and the other steps under "Operating rules" in
[Disk Usage with Parallel Work](disk_usage.md). Then run the task again.

- `-Pmin_free_disk_gb=<GB>` changes the threshold for one run. Put it in
  `~/.gradle/gradle.properties` to change it on one machine.
- `-Pmin_free_disk_gb=0` turns the check off. CI sets
  `ORG_GRADLE_PROJECT_min_free_disk_gb=0` because a hosted runner can have
  less than 20 GB free.
- `checkAll` checks once when it starts. The `gameTestAll` that it runs in a
  child Gradle process skips the check, so a `-P` value given to `checkAll`
  covers the whole run. A marker file in `.gradle/disk-space-check/` tells the
  child that `checkAll` is running and is removed when `checkAll` ends.
- Only the volume of the checkout is checked. When `prod_base_dir` points to
  another volume, check that volume yourself.
- `validateAll` and the other file-only validations never run the check.

---

## Debugging

### Enable Debug Logging

**Add to** `common/1.21.11/src/main/resources/log4j2.xml`:
```xml
<Configuration status="warn">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="[%d{HH:mm:ss}] [%t/%level] [%logger]: %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Logger level="debug" name="com.chronodawn"/>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

### IntelliJ IDEA Debugging

1. **Set Breakpoints**: Click left margin in code editor
2. **Launch Debug Mode**: Run → Debug 'Fabric Client'
3. **Step Through Code**: F8 (step over), F7 (step into)

### Common Debug Scenarios

**Portal Not Activating**:
- Check `PortalRegistry.getPortalState(pos)`
- Verify portal frame validation logic
- Log portal activation events

**Boss Not Spawning**:
- Verify boss room detection logic
- Check boss spawn conditions in `BossSpawner`
- Enable debug logging for structure placement

**Waterlogging Issues**:
- Check `StructureStartMixin` execution
- Verify `CopyFluidLevelProcessor` logic
- Inspect `INTENTIONAL_WATERLOGGING` set contents

---

## Contributing Guidelines

### Code Style

1. **Follow Java Conventions**:
   - Class names: PascalCase
   - Method names: camelCase
   - Constants: UPPER_SNAKE_CASE

2. **Use Mojang Mappings**:
   - `net.minecraft.world.level.Level` (not Yarn's `class_XXXX`)
   - `BlockState`, `ItemStack`, etc. (official names)

3. **Comment Complex Logic**:
   - Explain non-intuitive implementations
   - Document tricky bug fixes
   - Avoid comments for deleted code (use Git history)

### Commit Guidelines

**Follow Conventional Commits**:
```
feat: add Time Wood door and trapdoor blocks
fix: prevent waterlogging in Phantom Catacombs
chore: update dependencies to latest versions
docs: improve player guide boss strategies
```

**Commit Message Format**:
```
<type>: <short description>

<detailed description if needed>
```

### Pull Request Process

1. **Fork Repository**
2. **Create Feature Branch**: `git checkout -b feature/my-feature`
3. **Implement Changes**: Follow code style and testing guidelines
4. **Run Tests**: `./gradlew test`
5. **Build All Loaders**: `./gradlew build`
6. **Commit Changes**: Use conventional commits
7. **Push to Fork**: `git push origin feature/my-feature`
8. **Create Pull Request**: Target `main` branch

### Testing Requirements

Before submitting PR, run full verification:
```bash
./gradlew checkAll
```

This single command runs all automated checks in sequence:
- [ ] Clean all build outputs (cleanAll)
- [ ] Resource validation passes (validateResources)
- [ ] All versions build successfully and all unit tests pass (buildAll with -Pbuildall_run_tests=true)
- [ ] All GameTests pass (gameTestAll)

Additional manual checks:
- [ ] Manual testing completed (launch game, test feature)
- [ ] No new warnings or errors in logs
- [ ] Documentation updated (if applicable)
- [ ] CI is green: `validate`, `build`, `prod-smoke` and `gametest` for 1.21.1 and 26.3. They cover `prodSmokeTest` and `runGameTest`/`runGameTestServer` for those two versions
- [ ] If this PR changes Mixin config or adds a new Minecraft version: `prodSmokeTest` also passes for the other versions you touch (see "Production Smoke Test" above)
- [ ] GameTest servers may omit custom dimensions, so a dimension-dependent test can skip in CI. Check cross-dimension behavior in a normal client or server

---

## API Reference

### Platform Abstraction (@ExpectPlatform)

Use `@ExpectPlatform` for loader-specific implementations:

**Common** (`PlatformHelper.java`):
```java
public class PlatformHelper {
    @ExpectPlatform
    public static String getModVersion() {
        throw new AssertionError();
    }
}
```

**Fabric** (`PlatformHelperFabric.java`):
```java
public class PlatformHelperImpl {
    public static String getModVersion() {
        return FabricLoader.getInstance()
            .getModContainer("chronodawn")
            .get().getMetadata().getVersion().getFriendlyString();
    }
}
```

**NeoForge** (`PlatformHelperNeoForge.java`):
```java
public class PlatformHelperImpl {
    public static String getModVersion() {
        return ModList.get()
            .getModContainerById("chronodawn")
            .get().getModInfo().getVersion().toString();
    }
}
```

### Registry Helpers

**Architectury Registry Wrappers**:
```java
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> MY_BLOCK = BLOCKS.register(
        "my_block",
        () -> new Block(BlockBehaviour.Properties.of())
    );

    public static void register() {
        BLOCKS.register();
    }
}
```

### Event Handling

**Architectury Events**:
```java
public class ChronoDawnEvents {
    public static void register() {
        EntityEvent.LIVING_TICK.register((entity) -> {
            if (entity.level() instanceof ServerLevel serverLevel) {
                EntityEventHandler.onEntityTick(serverLevel, entity);
            }
        });
    }
}
```

### Public addon events

Public integration events live under `com.chronodawn.api.event` and must keep
their documented semantic-versioning contract. Addons register boss defeat
listeners through `BossDefeatedEvents`; the event is dispatched server-side
after the boss's built-in defeat consequences. See the
[modpack integration guide](modpack-integration.md#boss-defeated-event-api) for
the public accessors, stable IDs and registration example. The
`kubejs.classfilter.txt` shipped in `common/shared/src/main/resources`
allows KubeJS scripts to reach these classes directly with `Java.loadClass`;
see the
[KubeJS scripting bridge](modpack-integration.md#kubejs-scripting-bridge)
section for the script-side pattern, and
[FTB Quests integration](modpack-integration.md#ftb-quests-integration) for
how to combine it with FTB Quests' own KubeJS support (via FTB XMod
Compat) to drive quest progress from these events without any
ChronoDawn-specific Java addon.

---

## Advanced Topics

### Mixin Configuration

Fabric, NeoForge, and Forge use separate loader-specific Mixin configs.

**Fabric**: `chronodawn-fabric.mixins.json`
```json
{
  "required": true,
  "package": "com.chronodawn.mixin",
  "mixins": [
    "StructureStartMixin"
  ]
}
```

**NeoForge**: `chronodawn-neoforge.mixins.json`
```json
{
  "required": true,
  "package": "com.chronodawn.mixin",
  "mixins": [
    "StructureStartMixin"
  ]
}
```

**Forge** (1.20.1 only): `chronodawn-forge.mixins.json`

Do not add a `refmap` property or configure `loom.mixin.defaultRefmapName`.
Architectury Loom remaps Mixin targets directly in production JARs. Its legacy
Mixin annotation processor is disabled, so a configured refmap name would point
to a file that is not generated.

See `CLAUDE.md` → "Mixin Configuration" for full details. After changing Mixin
config, run the headless `prodSmokeTest` (Fabric/NeoForge, see "Production
Smoke Test" above). `runGameTest`/`runGameTestServer` run in Loom's
dev/mapped environment and can mask production remapping or injection failures.

### Structure Waterlogging Prevention

**Problem**: Underground structures waterlog due to Aquifer system.

**Solution**: Three-component system:
1. **Decorative Water Fluid**: Custom fluid for intended water features
2. **Structure Processor**: Records intentional waterlogging, removes all waterlogging during placement
3. **Mixin**: Replaces Aquifer water with a placeholder before placement, then puts the water back where the template left the placeholder (structure_void) and restores intentional waterlogging

**Reference**: `CLAUDE.md` → "Structure Worldgen Guidelines" → "Advanced Solution"

### Performance Optimization

**Tick Rate Optimization** (T178):
- Check Time Distortion effect every 5 ticks instead of every tick
- Reduces server load by 80%

**Caching** (T179):
- Cache portal registry lookups
- Implement lazy initialization for heavy objects

**AI Optimization** (T180):
- Use efficient state machines for boss AI
- Minimize pathfinding calculations

---

## Resources

### Documentation

- **Project Docs**: `docs/`
- **Design Guidelines**: `CLAUDE.md`
- **Historical Designs**: `docs/archive/README.md`

### External Resources

- [Architectury Docs](https://docs.architectury.dev/)
- [Fabric Docs](https://docs.fabricmc.net/)
- [NeoForge Docs](https://docs.neoforged.net/)
- [Minecraft Wiki](https://minecraft.wiki)
- [Mojang Mappings](https://maven.fabricmc.net/docs/yarn-javadoc/)

### Community

- [Architectury Discord](https://discord.gg/architectury)
- [Fabric Discord](https://discord.gg/v6v4pMv)
- [NeoForge Discord](https://discord.neoforged.net/)

---

## License

**Chrono Dawn** is licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**.

### What This Means

- ✅ **Free to Use**: You can use this mod in any way, including commercially
- ✅ **Free to Modify**: You can modify the source code and create derivative works
- ✅ **Free to Distribute**: You can redistribute the mod and modifications
- ⚠️ **Copyleft**: Modified versions must also be licensed under LGPL-3.0
- ✅ **Dynamic Linking**: Mods that use Chrono Dawn as a dependency (normal Minecraft mod usage) can use any license

### For Contributors

When contributing code to Chrono Dawn:

1. **All contributions** are licensed under LGPL-3.0
2. **Ensure compatibility**: Only use dependencies with LGPL-3.0-compatible licenses (MIT, Apache 2.0, BSD)
3. **Avoid incompatible licenses**: Do not add dependencies with GPL-incompatible licenses (CC-BY-NC, proprietary)
4. **Document dependencies**: Add third-party licenses to `THIRD_PARTY_LICENSES.md`

### License Header (Optional)

While not required, you may add LGPL-3.0 headers to new source files:

```java
/*
 * Chrono Dawn - A Time-Manipulation Dimension Mod for Minecraft
 * Copyright (C) 2025 Soichiro Kashima
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
```

### Full License Text

See the [LICENSE](../LICENSE) file for the complete LGPL-3.0 license text.

---

**For questions or support, open an issue on [GitHub](https://github.com/ksoichiro/ChronoDawn/issues).**
