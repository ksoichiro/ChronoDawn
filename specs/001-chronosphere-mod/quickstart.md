# Quickstart Guide: Chronosphere Mod Development

**Feature**: Chronosphere Mod - 時間操作をテーマにしたMinecraft Mod
**Target Version**: Minecraft Java Edition 1.21.1
**Architecture**: Architectury Multi-Loader (NeoForge 21.1.x + Fabric)
**Date**: 2025-10-19 (Updated for Architectury)

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 21**
   - Download: [Microsoft OpenJDK 21](https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21) または [Adoptium Temurin 21](https://adoptium.net/)
   - Verify: `java -version` (21.x.x が表示されることを確認)

2. **IntelliJ IDEA** (推奨) または **Eclipse**
   - IntelliJ IDEA Community Edition: https://www.jetbrains.com/idea/download/
   - Eclipse: https://www.eclipse.org/downloads/

3. **Git**
   - Download: https://git-scm.com/downloads

### Optional Tools

- **Visual Studio Code**: テキストファイル編集用
- **Blockbench**: カスタムモデル作成用 (https://www.blockbench.net/)

## Project Setup

### Step 1: Clone Repository

```bash
git clone https://github.com/[YOUR_USERNAME]/Chronosphere.git
cd Chronosphere
git checkout 001-chronosphere-mod
```

### Step 2: Initialize Architectury Multi-Loader Environment

#### Option A: Using Architectury Template Generator (推奨)

1. Visit https://generate.architectury.dev/
2. Configure template:
   - Minecraft Version: 1.21.1
   - Mod Loaders: NeoForge + Fabric
   - Mod ID: chronosphere
   - Package: com.chronosphere
   - Architectury API Version: 13.0.8
3. Download generated template
4. Extract to project root
5. Run initial setup:

```bash
# Windows
gradlew.bat build

# macOS/Linux
./gradlew build
```

#### Option B: Using MultiLoader Template (手動設定)

1. Clone template:
```bash
git clone https://github.com/jaredlll08/MultiLoader-Template
cd MultiLoader-Template
```

2. Configure `gradle.properties`:
```properties
# Minecraft & Loaders
minecraft_version=1.21.1
neoforge_version=21.1.208
fabric_loader_version=0.17.2
fabric_api_version=0.115.6+1.21.1

# Architectury
architectury_version=13.0.8

# Mod Info
mod_id=chronosphere
mod_name=Chronosphere
mod_version=1.0.0
maven_group=com.chronosphere
archives_base_name=chronosphere
```

3. Update `settings.gradle`:
```gradle
pluginManagement {
    repositories {
        maven { url = "https://maven.neoforged.net/releases" }
        maven { url = "https://maven.fabricmc.net/" }
        maven { url = "https://maven.architectury.dev/" }
        gradlePluginPortal()
    }
}

rootProject.name = "chronosphere"

include("common")
include("fabric")
include("neoforge")
```

4. Run setup:
```bash
# Windows
gradlew.bat build

# macOS/Linux
./gradlew build
```

### Step 3: Import Project to IDE

#### IntelliJ IDEA

1. Open IntelliJ IDEA
2. File → Open → プロジェクトルートの `build.gradle` を選択
3. "Open as Project" を選択
4. Gradle sync完了まで待機

#### Eclipse

1. Open Eclipse
2. File → Import → Existing Gradle Project
3. プロジェクトルートを選択
4. Finish

### Step 4: Verify Setup

#### Test Fabric Loader

```bash
# Windows
gradlew.bat :fabric:runClient

# macOS/Linux
./gradlew :fabric:runClient
```

Minecraftが起動し、Modリストに "Chronosphere" が表示されることを確認（Fabricローダー）

#### Test NeoForge Loader

```bash
# Windows
gradlew.bat :neoforge:runClient

# macOS/Linux
./gradlew :neoforge:runClient
```

Minecraftが起動し、Modリストに "Chronosphere" が表示されることを確認（NeoForgeローダー）

#### Build Both Loaders

```bash
# Windows
gradlew.bat build

# macOS/Linux
./gradlew build
```

ビルド成功後、以下のJARファイルが生成されることを確認:
- `fabric/build/libs/chronosphere-fabric-1.0.0.jar`
- `neoforge/build/libs/chronosphere-neoforge-1.0.0.jar`

## Project Structure Overview (Architectury Multi-Loader)

```
Chronosphere/
├── common/                             # ローダー非依存の共通コード
│   ├── src/main/java/com/chronosphere/
│   │   ├── Chronosphere.java           # 共通エントリーポイント
│   │   ├── core/                       # Core systems (dimension, portal, time)
│   │   ├── blocks/                     # Custom blocks
│   │   ├── items/                      # Custom items
│   │   ├── entities/                   # Custom entities
│   │   ├── worldgen/                   # World generation
│   │   ├── events/                     # Event handlers (Architectury Events)
│   │   ├── registry/                   # Architectury Registry wrappers
│   │   ├── platform/                   # @ExpectPlatform abstractions
│   │   └── util/                       # Utility classes
│   ├── src/main/resources/
│   │   ├── data/chronosphere/          # Data packs (recipes, loot tables, worldgen)
│   │   └── assets/chronosphere/        # Assets (textures, models, sounds)
│   └── src/test/java/com/chronosphere/  # 共通テスト
│       ├── integration/                # Integration tests (run per loader)
│       └── unit/                       # Unit tests
├── fabric/                             # Fabric固有実装
│   ├── src/main/java/com/chronosphere/fabric/
│   │   ├── ChronosphereFabric.java     # Fabric entry point
│   │   ├── client/                     # Client initialization
│   │   ├── platform/                   # @ExpectPlatform implementations
│   │   └── compat/                     # Fabric API integrations
│   └── src/main/resources/
│       ├── fabric.mod.json             # Fabric mod metadata
│       └── chronosphere.mixins.json
├── neoforge/                           # NeoForge固有実装
│   ├── src/main/java/com/chronosphere/neoforge/
│   │   ├── ChronosphereNeoForge.java   # NeoForge entry point
│   │   ├── client/                     # Client initialization
│   │   ├── platform/                   # @ExpectPlatform implementations
│   │   ├── event/                      # NeoForge event handlers
│   │   └── compat/                     # NeoForge API integrations
│   └── src/main/resources/
│       ├── META-INF/neoforge.mods.toml # NeoForge mod metadata
│       └── pack.mcmeta
├── specs/001-chronosphere-mod/         # Design documents
│   ├── spec.md                         # Feature specification
│   ├── plan.md                         # Implementation plan
│   ├── research.md                     # Technology research
│   ├── data-model.md                   # Data model
│   ├── contracts/                      # JSON contracts & build configs
│   └── quickstart.md                   # This file
├── build.gradle                        # Root build configuration
├── settings.gradle                     # Multi-module settings
├── gradle.properties                   # Gradle properties
└── architectury.properties             # Architectury configuration
```

## Development Workflow (Architectury Multi-Loader)

### Phase 1: Core Systems (P1 - 最優先)

#### Task 1.1: Setup Mod Entry Points

**Common Entry Point** (`common/src/main/java/com/chronosphere/Chronosphere.java`):

```java
public class Chronosphere {
    public static final String MOD_ID = "chronosphere";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        // Register common components using Architectury API
        LOGGER.info("Chronosphere Mod (common) initialized");

        // Initialize registries
        ModBlocks.register();
        ModItems.register();
        ModEntities.register();

        // Register event handlers
        ChronosphereEvents.register();
    }
}
```

**Fabric Entry Point** (`fabric/src/main/java/com/chronosphere/fabric/ChronosphereFabric.java`):

```java
@Mod(Chronosphere.MOD_ID)
public class ChronosphereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chronosphere.init();
        // Fabric-specific initialization
    }
}
```

**NeoForge Entry Point** (`neoforge/src/main/java/com/chronosphere/neoforge/ChronosphereNeoForge.java`):

```java
@Mod(Chronosphere.MOD_ID)
public class ChronosphereNeoForge {
    public ChronosphereNeoForge() {
        Chronosphere.init();
        // NeoForge-specific initialization
    }
}
```

#### Task 1.2: Create Custom Dimension

**Reference**: `specs/001-chronosphere-mod/data-model.md` (Dimension: Chronosphere)
**Implementation**: `src/main/java/com/chronosphere/core/dimension/`

**Key Files**:
- `ChronosphereDimension.java` - ディメンション登録
- `ChronosphereBiomeProvider.java` - バイオーム生成

**Data Files**:
- `src/main/resources/data/chronosphere/dimension/chronosphere_dimension.json`

#### Task 1.3: Implement Portal System

**Reference**: `specs/001-chronosphere-mod/data-model.md` (Portal System)
**Dependencies**: Custom Portal API Reforged
**Implementation**: `src/main/java/com/chronosphere/core/portal/`

**Key Files**:
- `ChronospherePortal.java` - ポータルロジック
- `PortalRegistry.java` - ポータル管理

#### Task 1.4: Add Time Distortion Effect

**Reference**: `specs/001-chronosphere-mod/data-model.md` (Time Distortion Effects)
**Implementation**: `src/main/java/com/chronosphere/events/EntityEventHandler.java`

```java
@SubscribeEvent
public void onEntityTick(LivingEvent.LivingTickEvent event) {
    if (event.getEntity().level().dimension() == CHRONOSPHERE_DIMENSION) {
        // Apply Slowness IV to custom mobs
        if (isCustomMob(event.getEntity())) {
            event.getEntity().addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, false
            ));
        }
    }
}
```

### Phase 2: Items & Blocks (P1)

#### Task 2.1: Create Base Materials

**Files**:
- `src/main/java/com/chronosphere/items/base/ClockstonItem.java`
- `src/main/java/com/chronosphere/blocks/ClockstoneOre.java`

**Textures**: `src/main/resources/assets/chronosphere/textures/item/clockstone.png`

#### Task 2.2: Create Portal Items

**Files**:
- `src/main/java/com/chronosphere/items/TimeHourglassItem.java`
- `src/main/java/com/chronosphere/items/PortalStabilizerItem.java`

**Recipes**:
- `src/main/resources/data/chronosphere/recipes/time_hourglass.json`
- `src/main/resources/data/chronosphere/recipes/portal_stabilizer.json`

### Phase 3: World Generation (P1)

#### Task 3.1: Generate Structures

**Reference**: `specs/001-chronosphere-mod/data-model.md` (Structures)
**Implementation**: `src/main/java/com/chronosphere/worldgen/structures/`

**Structures**:
- Ancient Ruins (オーバーワールド)
- Forgotten Library (クロノスフィア)

#### Task 3.2: Add Custom Blocks

**Files**:
- `src/main/java/com/chronosphere/blocks/ReversingTimeSandstone.java`
- `src/main/java/com/chronosphere/blocks/UnstableFungus.java`

### Phase 4: Boss Entities (P2-P3)

#### Task 4.1: Implement Time Guardian (中ボス)

**Reference**: `specs/001-chronosphere-mod/data-model.md` (Time Guardian)
**Implementation**: `src/main/java/com/chronosphere/entities/bosses/TimeGuardianEntity.java`

**Loot Table**: `src/main/resources/data/chronosphere/loot_tables/entities/time_guardian.json`

#### Task 4.2: Implement Time Tyrant (ラスボス)

**Reference**: `specs/001-chronosphere-mod/data-model.md` (Time Tyrant)
**Implementation**: `src/main/java/com/chronosphere/entities/bosses/TimeTyrantEntity.java`

**Loot Table**: `src/main/resources/data/chronosphere/loot_tables/entities/time_tyrant.json`

### Phase 5: Ultimate Artifacts (P3)

#### Task 5.1: Implement Chronoblade

**File**: `src/main/java/com/chronosphere/items/artifacts/ChronobladeItem.java`
**Recipe**: `src/main/resources/data/chronosphere/recipes/chronoblade.json`

**Special Effect**: 攻撃命中時、25%の確率でMobの攻撃AIをスキップ

#### Task 5.2: Implement Time Guardian's Mail

**File**: `src/main/java/com/chronosphere/items/artifacts/TimeGuardianMailItem.java`
**Recipe**: `src/main/resources/data/chronosphere/recipes/time_guardian_mail.json`

**Special Effect**: 致命的ダメージ時、20%の確率でロールバック

## Testing

### Unit Testing

**Framework**: JUnit 5 + mcjunitlib
**Location**: `src/test/java/com/chronosphere/unit/`

```bash
# Run unit tests
./gradlew test
```

**Example Test**:

```java
@Test
public void testTimeDistortionEffect() {
    // Test that custom mobs receive Slowness IV in Chronosphere dimension
    // ...
}
```

### Integration Testing

**Framework**: Minecraft GameTest Framework
**Location**: `src/test/java/com/chronosphere/integration/`

```bash
# Run game tests
./gradlew runGameTest
```

**Example Test**:

```java
@GameTest
public static void testPortalTravel(GameTestHelper helper) {
    // Test portal activation and dimension travel
    // ...
}
```

### Manual Testing

1. Launch development client:
   ```bash
   ./gradlew runClient
   ```

2. Create a new world (Creative Mode recommended for testing)

3. Test checklist:
   - [ ] Find Ancient Ruins in Overworld
   - [ ] Craft Time Hourglass
   - [ ] Activate portal and travel to Chronosphere
   - [ ] Verify portal stops working after entry
   - [ ] Find Forgotten Library
   - [ ] Craft Portal Stabilizer
   - [ ] Stabilize portal and return to Overworld
   - [ ] Test round-trip travel

## Debugging

### Enable Debug Logging

**File**: `src/main/resources/log4j2.xml`

```xml
<Logger level="debug" name="com.chronosphere"/>
```

### Common Issues

#### Issue 1: Mod not appearing in Mod List

**Solution**: Check `src/main/resources/META-INF/mods.toml` configuration

#### Issue 2: Dimension not generating

**Solution**: Verify dimension JSON in `data/chronosphere/dimension/`

#### Issue 3: Portal not activating

**Solution**: Check portal frame validation logic in `ChronospherePortal.java`

## Building for Distribution

### Build JAR

```bash
./gradlew build
```

**Output**: `build/libs/chronosphere-1.0.0.jar`

### Test JAR in Production Environment

1. Install Minecraft 1.21.1
2. Install NeoForge 21.1.x
3. Copy JAR to `mods/` folder
4. Launch Minecraft

## Resources

### Documentation

- **Spec**: `specs/001-chronosphere-mod/spec.md`
- **Plan**: `specs/001-chronosphere-mod/plan.md`
- **Research**: `specs/001-chronosphere-mod/research.md`
- **Data Model**: `specs/001-chronosphere-mod/data-model.md`

### External Resources

- **NeoForge Docs**: https://docs.neoforged.net
- **Minecraft Wiki**: https://minecraft.wiki
- **Custom Portal API**: https://moddedmc.wiki/cs/project/cpapireforged
- **mcjunitlib**: https://github.com/alcatrazEscapee/mcjunitlib

### Community

- **NeoForge Discord**: https://discord.neoforged.net/
- **Minecraft Modding Wiki**: https://moddedmc.wiki

## Next Steps

1. **Phase 0 Complete**: 技術選定完了 ✅
2. **Phase 1 Start**: Core Systems実装開始
   - ディメンション登録
   - ポータルシステム
   - 時間の歪み効果

3. **Follow-up Commands**:
   ```bash
   # Generate task list for implementation
   /speckit.tasks

   # Start implementation
   /speckit.implement
   ```

## Support

問題が発生した場合:

1. `specs/001-chronosphere-mod/` 内のドキュメントを確認
2. NeoForge公式ドキュメントを参照
3. プロジェクトのIssueトラッカーで報告
