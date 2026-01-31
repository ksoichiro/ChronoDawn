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

Chrono Dawn uses the **Architectury** framework to support both Fabric and NeoForge loaders with a shared codebase.

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

- **Java 21**: Target language version
- **Minecraft**: Java Edition 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4 / 1.21.5
- **Fabric Loader**: 0.17.3+
- **NeoForge**: 21.1.209+ (for 1.21.1) / 21.2.0-beta+ (for 1.21.2) / 21.3.0-beta+ (for 1.21.3) / 21.4.0-beta+ (for 1.21.4) / 21.5.96+ (for 1.21.5)
- **Architectury API**: 13.0.8+ (for 1.21.1) / 14.0.4+ (for 1.21.2/1.21.3) / 15.0.1+ (for 1.21.4) / 16.1.4+ (for 1.21.5)
- **Gradle**: Build automation (Groovy DSL)
- **Mojang Mappings**: Official Minecraft class names

*Note: 1.21.3 is a hotfix release that shares code modules with 1.21.2.*

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
├── common-shared/                          # Shared version-agnostic sources (included via srcDir)
├── common-gametest/                        # Shared gametest sources (included via srcDir)
├── common-1.21.2/                          # Common module for MC 1.21.2
├── common-1.21.5/                          # Common module for MC 1.21.5 (~80% of code)
│   ├── src/main/java/com/chronodawn/
│   │   ├── ChronoDawn.java                 # Common entry point
│   │   ├── blocks/                         # Custom blocks
│   │   │   ├── ModBlocks.java              # Block registration
│   │   │   ├── ReversingTimeSandstone.java
│   │   │   └── UnstableFungus.java
│   │   ├── items/                          # Custom items
│   │   │   ├── ModItems.java               # Item registration
│   │   │   ├── TimeHourglassItem.java
│   │   │   └── artifacts/                  # Ultimate artifacts
│   │   ├── entities/                       # Custom entities
│   │   │   ├── ModEntities.java            # Entity registration
│   │   │   └── bosses/                     # Boss entities
│   │   │       ├── TimeGuardianEntity.java
│   │   │       ├── TemporalPhantomEntity.java
│   │   │       └── TimeTyrantEntity.java
│   │   ├── worldgen/                       # World generation
│   │   │   ├── biomes/                     # Custom biomes
│   │   │   ├── features/                   # Worldgen features
│   │   │   └── structures/                 # Structure generation
│   │   ├── events/                         # Event handlers
│   │   │   ├── EntityEventHandler.java     # Time Distortion effect
│   │   │   └── PlayerEventHandler.java
│   │   ├── registry/                       # Architectury registries
│   │   │   └── ModRegistries.java
│   │   ├── platform/                       # @ExpectPlatform methods
│   │   │   └── PlatformHelper.java
│   │   ├── mixin/                          # Mixin classes
│   │   │   └── StructureStartMixin.java    # Waterlogging prevention
│   │   └── util/                           # Utility classes
│   ├── src/main/resources/
│   │   ├── data/chronodawn/
│   │   │   ├── dimension/                  # Dimension JSON
│   │   │   ├── worldgen/                   # Biomes, features, structures
│   │   │   ├── recipes/                    # Crafting recipes
│   │   │   ├── loot_tables/                # Loot tables
│   │   │   └── structures/                 # NBT structure files
│   │   └── assets/chronodawn/
│   │       ├── textures/                   # Textures (blocks, items, entities)
│   │       ├── models/                     # Block/item models
│   │       ├── blockstates/                # Block state definitions
│   │       ├── lang/                       # Translations
│   │       └── sounds/                     # Sound files
│   └── src/test/java/com/chronodawn/     # JUnit tests
├── common-1.21.1/                          # Common module for MC 1.21.1
├── common-1.20.1/                          # Common module for MC 1.20.1
├── fabric-base/                            # Shared Fabric sources (NOT a Gradle subproject)
│   └── src/main/java/com/chronodawn/fabric/
│       ├── event/                          # Event handlers
│       └── platform/                       # Platform implementations
├── fabric-1.20.1/                          # Fabric 1.20.1 subproject
├── fabric-1.21.1/                          # Fabric 1.21.1 subproject
├── fabric-1.21.2/                          # Fabric 1.21.2 subproject
│   ├── src/main/java/com/chronodawn/fabric/
│   │   ├── ChronoDawnFabric.java           # Fabric entry point (version-specific)
│   │   └── client/                         # Client-side initialization
│   └── src/main/resources/
│       ├── fabric.mod.json                 # Fabric mod metadata
│       └── chronodawn-fabric.mixins.json   # Fabric Mixin config (with refMap)
├── neoforge-base/                          # Shared NeoForge sources (NOT a Gradle subproject)
│   └── src/main/java/com/chronodawn/neoforge/
│       ├── ChronoDawnNeoForge.java         # NeoForge entry point
│       ├── client/                         # Client-side initialization
│       └── platform/                       # Platform implementations
├── neoforge-1.21.1/                        # NeoForge 1.21.1 subproject
├── neoforge-1.21.2/                        # NeoForge 1.21.2 subproject
│   ├── src/main/java/com/chronodawn/neoforge/
│   │   └── mixin/                          # Version-specific mixins
│   └── src/main/resources/
│       └── META-INF/neoforge.mods.toml     # NeoForge mod metadata
├── specs/chrono-dawn-mod/                  # Design documents
│   ├── spec.md                             # Feature specification
│   ├── plan.md                             # Implementation plan
│   ├── tasks.md                            # Task tracking
│   ├── data-model.md                       # Data model
│   └── research.md                         # Research notes
├── docs/                                   # Documentation
│   ├── player_guide.md                     # Player guide
│   └── developer_guide.md                  # This file
├── build.gradle                            # Root build configuration
├── settings.gradle                         # Multi-module settings
├── gradle.properties                       # Version configuration
└── CLAUDE.md                               # Development guidelines
```

---

## Build System

### Gradle Configuration (Groovy DSL)

**Important**: This project uses **Groovy DSL**, not Kotlin DSL, for Architectury Loom 1.11-SNAPSHOT compatibility.

#### Root `build.gradle`

```groovy
plugins {
    id 'dev.architectury.loom' version '1.11-SNAPSHOT' apply false
    id 'architectury-plugin' version '3.4-SNAPSHOT'
    id 'com.gradleup.shadow' version '8.3.6' apply false
}

architectury {
    minecraft = project.minecraft_version
}
```

#### `gradle.properties`

```properties
mod_version=0.5.0-beta
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
# Build for default version (1.21.5)
./gradlew build

# Build for a specific Minecraft version
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

# Build specific module
./gradlew :fabric:build -Ptarget_mc_version=1.21.5
./gradlew :neoforge:build -Ptarget_mc_version=1.21.5

# Run development client (version-specific)
./gradlew :fabric:runClient -Ptarget_mc_version=1.21.5
./gradlew :fabric:runClient -Ptarget_mc_version=1.21.2
./gradlew :fabric:runClient -Ptarget_mc_version=1.21.1
./gradlew :fabric:runClient -Ptarget_mc_version=1.20.1
./gradlew :neoforge:runClient -Ptarget_mc_version=1.21.5
./gradlew :neoforge:runClient -Ptarget_mc_version=1.21.1

# Run development server
./gradlew :fabric:runServer -Ptarget_mc_version=1.21.5
./gradlew :neoforge:runServer -Ptarget_mc_version=1.21.5

# Run unit tests
./gradlew test
./gradlew :common-1.21.5:test -Ptarget_mc_version=1.21.5

# Run GameTests (in-game integration tests)
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.5
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.2
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1
./gradlew gameTestAll   # All versions and loaders (fully parallel)

# Validate resource files (JSON syntax + cross-references)
./gradlew validateResources

# Collect release JARs into build/release/
./gradlew collectJars

# Full release pipeline (cleanAll → buildAll → collectJars)
./gradlew release

# Run ALL verification tasks (recommended before commits/PRs)
./gradlew checkAll
# Runs in sequence: cleanAll → validateResources → buildAll → testAll → gameTestAll

# Clean build artifacts
./gradlew clean
```

### Output Files

After building:
- **Fabric JAR**: `fabric-1.21.5/build/libs/chronodawn-0.5.0-beta+1.21.5-fabric.jar`
- **NeoForge JAR**: `neoforge-1.21.5/build/libs/chronodawn-0.5.0-beta+1.21.5-neoforge.jar`
- **Common JAR**: `common-1.21.5/build/libs/common-1.21.5-0.5.0-beta.jar` (bundled into loader JARs)

---

## Core Systems

### 1. Dimension System

**Location**: `common-1.21.5/src/main/java/com/chronodawn/core/dimension/`

**Key Classes**:
- `ChronoDawnDimension.java`: Dimension registration and key definitions
- Custom biomes in `common-1.21.5/src/main/resources/data/chronodawn/worldgen/biome/`

**Dimension JSON**: `common-1.21.5/src/main/resources/data/chronodawn/dimension/chronodawn_dimension.json`

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
- Common: `common-1.21.5/src/main/java/com/chronodawn/core/portal/`
- Blocks: `common-1.21.5/src/main/java/com/chronodawn/blocks/ChronoDawnPortalBlock.java`

**Key Components**:
- **PortalRegistry**: Tracks portal states (INACTIVE, ACTIVE, STABILIZED)
- **ChronoDawnPortalBlock**: Custom portal block implementation
- **PortalTeleportHandler**: Handles teleportation logic between dimensions
- **TimeHourglassItem**: Portal ignition logic
- **PortalStabilizerItem**: Dimension stabilization logic

**Portal Color**: Orange (#db8813 - RGB: 219, 136, 19)

### 3. Time Distortion Effect

**Location**: `common-1.21.5/src/main/java/com/chronodawn/events/EntityEventHandler.java`

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

**Biomes**: `common-1.21.5/src/main/resources/data/chronodawn/worldgen/biome/`
- 8 custom biomes with unique features

**Structures**: `common-1.21.5/src/main/java/com/chronodawn/worldgen/structures/`
- Ancient Ruins (Overworld)
- Forgotten Library (Chrono Dawn)
- Master Clock (final dungeon)
- Phantom Catacombs (maze with boss)

**Structure NBT Files**: `common-1.21.5/src/main/resources/data/chronodawn/structure/`

### 5. Boss System

**Entities**: `common-1.21.5/src/main/java/com/chronodawn/entities/bosses/`

**Boss AI Components**:
- **TimeGuardianAI**: AI state machine for Time Guardian
- **TemporalPhantomAI**: Phantom boss AI with phase transitions
- **TimeTyrantAI**: Final boss AI with multiple phases

**Boss Spawning**: See `PhantomCatacombsBossRoomPlacer.java` for programmatic boss room placement

### 6. Chronicle Guidebook UI

**Location**: `common-1.21.5/src/main/java/com/chronodawn/client/gui/screens/chronicle/`

**Description**: Custom in-game guidebook system that replaced Patchouli dependency. Chronicle provides players with information about biomes, structures, bosses, items, and progression.

**Key Components**:
- **ChronicleScreen.java**: Main screen with category sidebar and entry display
- **ChronicleData.java**: JSON data loader for guidebook content
- **ChronicleBookItem.java**: Item that opens the Chronicle GUI

**Data Files**: `common-1.21.5/src/main/resources/assets/chronodawn/chronicle/`
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

---

## Adding New Features

### Adding a New Block

1. **Create Block Class** (`common-1.21.5/src/main/java/com/chronodawn/blocks/MyCustomBlock.java`):
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

1. **Create Entity Class** (`common-1.21.5/src/main/java/com/chronodawn/entities/MyEntity.java`):
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
2. **Save to**: `common-1.21.5/src/main/resources/data/chronodawn/structure/`
3. **Define Structure JSON**: `data/chronodawn/worldgen/structure/my_structure.json`
4. **Add to Structure Set**: `data/chronodawn/worldgen/structure_set/my_structure_set.json`
5. **Implement Structure Processor** (if custom logic needed)

---

## Testing

### Unit Testing

**Framework**: JUnit 5

**Location**: `common-1.21.5/src/test/java/com/chronodawn/unit/`

**Test Types**:

- **ResourceValidationTest** — Uses `@TestFactory` to generate dynamic tests that verify the following without launching a Minecraft server:
  - Blockstate JSON existence for all blocks
  - Item model JSON existence for all items
  - Translation key completeness (`en_us.json`)

  Parses source files to extract `RegistrySupplier` field names and validates resource existence via `ClassLoader.getResource()` (~0.1s for 402 tests).

**Run Tests**:
```bash
./gradlew :common-1.21.5:test
```

### Integration Testing (GameTest)

**Framework**: Minecraft GameTest Framework with registry-driven test generation

**Location**:
- `common-1.21.5/src/main/java/com/chronodawn/gametest/` (1.21.5)
- `common-1.21.2/src/main/java/com/chronodawn/gametest/` (1.21.2)
- `common-1.21.1/src/main/java/com/chronodawn/compat/v1_21_1/gametest/` (1.21.1)
- `common-1.20.1/src/main/java/com/chronodawn/compat/v1_20_1/gametest/` (1.20.1)

**Test Categories** (auto-generated from registries):
- **Block placement** — Verifies all blocks can be placed
- **Entity spawning** — Verifies all entities can be spawned
- **Tool durability / Armor defense** — Validates equipment properties
- **Registry ID consistency** — Field names match registry IDs
- **Food properties / Equipment stack size** — Property validation
- **Boss fight tests** — Boss encounter mechanics

> **Note**: Blockstate/model/translation existence tests have been migrated to JUnit (see Unit Testing above).

**Run GameTests**:
```bash
# Run for a specific version
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.5
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.2
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1
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

Each version uses a separate directory (e.g., `fabric-1.21.2/`) via `project(':fabric').projectDir = file("fabric-1.21.2")` in settings.gradle. This gives each version its own `.gradle/architectury/` directory and build output.

Each subprocess uses `--project-cache-dir .gradle/gametest-<version>` to avoid lock conflicts with the outer Gradle process and other subprocesses.

Shared sources are included via `srcDir` references to `fabric-base/` and `neoforge-base/` directories (which are source directories, not Gradle subprojects).

**Log files**: Each version group's output is saved to `build/gametest-<version>.log`.

### Resource Validation

**Tool**: Gradle `validateResources` task

**Location**: `gradle/resource-validation.gradle`

Validates data/asset file integrity at build time without launching the game:

- **JSON syntax check** — Parses all `.json` files under `common-1.21.5/src/main/resources/`
- **Blockstate → model reference check** — Verifies `"model": "chronodawn:block/<name>"` targets exist
- **Model → texture reference check** — Verifies `"textures"` entries with `chronodawn:` prefix have matching `.png` files

```bash
./gradlew validateResources
```

### Manual Testing

1. **Launch Development Client**:
   ```bash
   ./gradlew :fabric:runClient -Ptarget_mc_version=1.21.5
   ```

2. **Create Test World**: Creative mode recommended

3. **Test Checklist**: See `specs/chrono-dawn-mod/quickstart.md`

---

## Debugging

### Enable Debug Logging

**Add to** `common-1.21.5/src/main/resources/log4j2.xml`:
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
- [ ] All versions build successfully (buildAll)
- [ ] All unit tests pass (testAll)
- [ ] All GameTests pass (gameTestAll)

Additional manual checks:
- [ ] Manual testing completed (launch game, test feature)
- [ ] No new warnings or errors in logs
- [ ] Documentation updated (if applicable)

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

---

## Advanced Topics

### Mixin Configuration

**Critical**: Fabric and NeoForge require **separate** Mixin configs due to mapping differences.

**Fabric**: `chronodawn-fabric.mixins.json` (with refMap)
```json
{
  "required": true,
  "package": "com.chronodawn.mixin",
  "refmap": "common-common-refmap.json",
  "mixins": [
    "StructureStartMixin"
  ]
}
```

**NeoForge**: `chronodawn-neoforge.mixins.json` (without refMap)
```json
{
  "required": true,
  "package": "com.chronodawn.mixin",
  "mixins": [
    "StructureStartMixin"
  ]
}
```

See `CLAUDE.md` → "Mixin Configuration" for full details.

### Structure Waterlogging Prevention

**Problem**: Underground structures waterlog due to Aquifer system.

**Solution**: Three-component system:
1. **Decorative Water Fluid**: Custom fluid for intended water features
2. **Structure Processor**: Records intentional waterlogging, removes all waterlogging during placement
3. **Mixin**: Removes Aquifer water before placement, restores intentional waterlogging after

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

- **Project Docs**: `specs/chrono-dawn-mod/`
- **Design Guidelines**: `CLAUDE.md`
- **Quickstart Guide**: `specs/chrono-dawn-mod/quickstart.md`

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
