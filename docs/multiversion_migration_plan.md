# Chrono Dawn - Multi-Version Migration Plan

**Date**: 2026-01-01
**Author**: Claude + User
**Target Versions**: Minecraft 1.20.6 + 1.21.1 (Single Codebase)
**Strategy**: Custom Gradle Scripts + Abstraction Layer Approach

---

## Table of Contents

1. [Overview](#1-overview)
2. [Current State Analysis](#2-current-state-analysis)
3. [Target Architecture](#3-target-architecture)
4. [Phased Implementation Plan](#4-phased-implementation-plan)
5. [Verification Methods](#5-verification-methods)
6. [Risks and Mitigation](#6-risks-and-mitigation)
7. [References](#7-references)

---

## 1. Overview

### 1.1 Purpose

- Support both Minecraft 1.20.6 and 1.21.1 from a **single codebase**
- Prioritize **AI development efficiency** by consolidating all code in one location (avoid Git branch separation)
- Ensure maintainability with **no external library dependencies** (no preprocessors like Stonecutter/Manifold)

### 1.2 Basic Policy

1. **Data Pack**: Use version-specific directories (`resources-1.20.6/`, `resources-1.21.1/`) with Gradle switching
2. **Java Code**: Abstract version differences through a compatibility layer, with version-specific implementations in `compat/` package
3. **Gradle Scripts**: Custom `sourceSets` configuration for build-time version selection

### 1.3 Scope

- **Target Versions**: Minecraft 1.20.6, 1.21.1
- **Target Loaders**: Fabric, NeoForge (maintain existing Architectury structure)
- **Out of Scope**: 1.20.1-1.20.4 (Architectury Loom 1.11 only supports 1.20.4+)

---

## 2. Current State Analysis

### 2.1 Version-Dependent Areas

#### A. Data Pack (Folder Naming: Singular/Plural)

| Category | 1.20.6 (Plural) | 1.21.1 (Singular) | Impact |
|----------|----------------|-------------------|--------|
| **Advancements** | `advancement`**s** | `advancement` | ~100 files |
| **Loot Tables** | `loot_table`**s** | `loot_table` | ~50 files |
| **Recipes** | `recipe`**s** | `recipe` | ~150 files |
| **Functions** | `function`**s** | `function` | ~0 files (unused) |
| **Tags** | `tags/item`**s** | `tags/item` | ~20 files |

**Pack Format**:
- 1.20.6: `pack_format: 41`
- 1.21.1: `pack_format: 48`

**Current State**: Only 1.21.1 format (singular) exists

#### B. Java API Breaking Changes

| Category | 1.20.6 (NBT-based) | 1.21.1 (Component-based) | Affected Files |
|----------|--------------------|-----------------------|--------------|
| **ItemStack Data** | `stack.getOrCreateTag()` | `stack.set(DataComponents.CUSTOM_DATA, ...)` | 47 files |
| **SavedData** | `CompoundTag` only | `CompoundTag` + `HolderLookup.Provider` | 7 files |
| **Registry Access** | Direct `Registry.XXXX` | Via `RegistryAccess` | Unknown |
| **BlockEntity** | `saveAdditional(tag)` | `saveAdditional(tag, registries)` | Unknown |

**Main Affected Areas**:
```
common/src/main/java/com/chronodawn/
├── items/               # ItemStack operations (PortalStabilizerItem, TimeClockItem, etc.)
├── data/                # SavedData (ChronoDawnWorldData, PortalRegistryData, etc.)
├── entities/bosses/     # Entity NBT (TimeTyrantEntity, ClockworkColossusEntity, etc.)
├── blocks/              # BlockEntity NBT (BossRoomDoorBlockEntity, etc.)
└── core/                # Portal registry, teleport handling
```

#### C. Dependency Library Versions

| Library | 1.20.6 | 1.21.1 (Current) | Compatibility |
|---------|--------|------------------|---------------|
| Architectury API | `12.1.4` | `13.0.8` | **Incompatible** |
| Fabric API | `0.100.4+1.20.6` | `0.116.7+1.21.1` | **Incompatible** |
| NeoForge | `20.6.145` | `21.1.209` | **Incompatible** |
| Custom Portal API (Fabric) | `0.0.1-beta66-1.20` | `0.0.1-beta66-1.21` | **Incompatible** |

---

## 3. Target Architecture

### 3.1 Directory Structure

```
ChronoDawn/
├── gradle.properties                # Default version settings
├── props/                           # Version-specific configurations (NEW)
│   ├── 1.20.6.properties
│   └── 1.21.1.properties
├── common/
│   ├── build.gradle                 # Multi-version Gradle configuration
│   └── src/main/
│       ├── java/com/chronodawn/
│       │   ├── compat/              # Version-specific implementations (NEW)
│       │   │   ├── ItemDataHandler.java         # Interface
│       │   │   ├── v1_20_6/                     # 1.20.6 implementation
│       │   │   │   └── ItemDataHandler120.java
│       │   │   └── v1_21_1/                     # 1.21.1 implementation
│       │   │       └── ItemDataHandler121.java
│       │   ├── items/               # Common code (version-independent)
│       │   ├── blocks/
│       │   └── ...
│       ├── resources/               # Common resources (assets, structure NBTs, etc.)
│       ├── resources-1.20.6/        # 1.20.6-specific (NEW)
│       │   ├── pack.mcmeta          # pack_format: 41
│       │   └── data/chronodawn/
│       │       ├── advancements/    # Plural
│       │       ├── loot_tables/     # Plural
│       │       └── recipes/         # Plural
│       └── resources-1.21.1/        # 1.21.1-specific (NEW)
│           ├── pack.mcmeta          # pack_format: 48
│           └── data/chronodawn/
│               ├── advancement/     # Singular
│               ├── loot_table/      # Singular
│               └── recipe/          # Singular
├── fabric/build.gradle              # Fabric loader-specific (version variable support)
└── neoforge/build.gradle            # NeoForge loader-specific (version variable support)
```

### 3.2 Abstraction Layer Design

#### A. ItemStack Data Abstraction

```java
// common/src/main/java/com/chronodawn/compat/ItemDataHandler.java
public interface ItemDataHandler {
    void setString(ItemStack stack, String key, String value);
    String getString(ItemStack stack, String key);
    void setInt(ItemStack stack, String key, int value);
    int getInt(ItemStack stack, String key);
    // ... other data types
}

// 1.20.6 implementation
public class ItemDataHandler120 implements ItemDataHandler {
    @Override
    public void setString(ItemStack stack, String key, String value) {
        stack.getOrCreateTag().putString(key, value);
    }

    @Override
    public String getString(ItemStack stack, String key) {
        return stack.getOrCreateTag().getString(key);
    }
}

// 1.21.1 implementation
public class ItemDataHandler121 implements ItemDataHandler {
    @Override
    public void setString(ItemStack stack, String key, String value) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(tag -> tag.putString(key, value));
        stack.set(DataComponents.CUSTOM_DATA, customData);
    }

    @Override
    public String getString(ItemStack stack, String key) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getString(key);
    }
}

// Factory (compile-time version detection)
public class CompatHandlers {
    public static final ItemDataHandler ITEM_DATA = createItemDataHandler();

    private static ItemDataHandler createItemDataHandler() {
        // Version detection via Gradle-defined property
        String mcVersion = System.getProperty("chronodawn.minecraft.version", "1.21.1");
        if (mcVersion.startsWith("1.20")) {
            return new ItemDataHandler120();
        } else {
            return new ItemDataHandler121();
        }
    }
}
```

#### B. SavedData Abstraction

```java
// common/src/main/java/com/chronodawn/compat/SavedDataHandler.java
public interface SavedDataHandler {
    CompoundTag save(CompoundTag tag, Object registries);
    // registries is ignored in 1.20.6, used as HolderLookup.Provider in 1.21.1
}
```

### 3.3 Gradle Configuration

#### A. Version Property Files

**props/1.20.6.properties**:
```properties
minecraft_version=1.20.6
pack_format=41
architectury_api_version=12.1.4
fabric_api_version=0.100.4+1.20.6
neoforge_version=20.6.145
custom_portal_api_fabric_version=0.0.1-beta66-1.20
compat_package=v1_20_6
```

**props/1.21.1.properties**:
```properties
minecraft_version=1.21.1
pack_format=48
architectury_api_version=13.0.8
fabric_api_version=0.116.7+1.21.1
neoforge_version=21.1.209
custom_portal_api_fabric_version=0.0.1-beta66-1.21
compat_package=v1_21_1
```

#### B. build.gradle Extensions

**build.gradle (root)**:
```groovy
// Load version properties
ext.loadVersionProperties = { String version ->
    def propsFile = file("props/${version}.properties")
    if (!propsFile.exists()) {
        throw new GradleException("Version properties not found: ${propsFile}")
    }
    def props = new Properties()
    propsFile.withInputStream { props.load(it) }

    // Override gradle.properties
    props.each { key, value ->
        project.ext.set(key, value)
    }
}

// Command line or default version
def targetVersion = project.findProperty('target_mc_version') ?: '1.21.1'
loadVersionProperties(targetVersion)

// Propagate to subprojects
subprojects {
    ext.minecraft_version = rootProject.ext.minecraft_version
    ext.pack_format = rootProject.ext.pack_format
    ext.compat_package = rootProject.ext.compat_package
}
```

**common/build.gradle**:
```groovy
sourceSets {
    main {
        java {
            srcDir 'src/main/java'
            // Add version-specific implementation
            srcDir "src/main/java/${project.compat_package}"
        }

        resources {
            srcDir 'src/main/resources'  // Common resources

            // Prioritize version-specific resources
            def versionResources = file("src/main/resources-${project.minecraft_version}")
            if (versionResources.exists()) {
                srcDir versionResources
            }
        }
    }
}

// Set version info as Java system property at compile time
tasks.withType(JavaCompile) {
    options.compilerArgs += [
        "-Achronodawn.minecraft.version=${project.minecraft_version}"
    ]
    systemProperty 'chronodawn.minecraft.version', project.minecraft_version
}

processResources {
    inputs.property 'minecraft_version', project.minecraft_version
    inputs.property 'pack_format', project.pack_format

    filesMatching('pack.mcmeta') {
        expand 'pack_format': project.pack_format
    }
}
```

---

## 4. Phased Implementation Plan

### Phase 1: Data Pack Integration (Priority: High)

**Goal**: Separate version-specific Data Packs and switch at build time with Gradle

#### Task List

- [ ] **T1-1**: Create `props/` directory and version property files
- [ ] **T1-2**: Add version switching logic to root `build.gradle`
- [ ] **T1-3**: Create `common/src/main/resources-1.21.1/` directory (copy current data)
- [ ] **T1-4**: Create `common/src/main/resources-1.20.6/` directory
  - [ ] T1-4a: Rename `advancement/` → `advancements/` (copy all files)
  - [ ] T1-4b: Rename `loot_table/` → `loot_tables/`
  - [ ] T1-4c: Rename `recipe/` → `recipes/`
  - [ ] T1-4d: Rename `tags/item/` → `tags/items/`
  - [ ] T1-4e: Change `pack_format` to 41 in `pack.mcmeta`
- [ ] **T1-5**: Add `sourceSets.main.resources` configuration to `common/build.gradle`
- [ ] **T1-6**: Verification
  - [ ] T1-6a: Verify singular folders used with `./gradlew :common:processResources -Ptarget_mc_version=1.21.1`
  - [ ] T1-6b: Verify plural folders used with `./gradlew :common:processResources -Ptarget_mc_version=1.20.6`

**Duration**: 2-3 days
**Risk**: Low (standard Gradle features only)

---

### Phase 2: Abstraction Layer Design (Priority: High)

**Goal**: Design abstraction interfaces to absorb API differences between versions

#### Task List

- [ ] **T2-1**: Design `compat/` package structure
  - [ ] T2-1a: Design `ItemDataHandler` interface
  - [ ] T2-1b: Design `SavedDataHandler` interface
  - [ ] T2-1c: Design `RegistryHandler` interface (if needed)
- [ ] **T2-2**: Survey existing code API usage patterns
  - [ ] T2-2a: Create list of `getOrCreateTag()` usage locations (47 files)
  - [ ] T2-2b: Survey `SavedData.save()` signature (7 files)
  - [ ] T2-2c: Survey BlockEntity `saveAdditional()`
- [ ] **T2-3**: Finalize interface specifications
  - [ ] T2-3a: Create ItemDataHandler method list (setString, getInt, etc.)
  - [ ] T2-3b: Decide error handling policy
  - [ ] T2-3c: Decide null safety guarantee approach

**Duration**: 3-4 days
**Risk**: Medium (design mistakes will impact subsequent phases)

---

### Phase 3: Java Code Abstraction Implementation (Priority: Medium)

**Goal**: Implement abstraction layer and separate version-specific code

#### Task List

- [ ] **T3-1**: Implement `compat/` package
  - [ ] T3-1a: Implement `ItemDataHandler` interface
  - [ ] T3-1b: Implement `v1_20_6/ItemDataHandler120.java` (NBT version)
  - [ ] T3-1c: Implement `v1_21_1/ItemDataHandler121.java` (Component version)
  - [ ] T3-1d: Implement `CompatHandlers` factory class
- [ ] **T3-2**: Implement `SavedDataHandler`
  - [ ] T3-2a: Implement interface
  - [ ] T3-2b: Implement 1.20.6 version (no `HolderLookup.Provider`)
  - [ ] T3-2c: Implement 1.21.1 version (with `HolderLookup.Provider`)
- [ ] **T3-3**: Extend Gradle build scripts
  - [ ] T3-3a: Add `compat_package` property
  - [ ] T3-3b: Add version-specific directory to `sourceSets.main.java`
  - [ ] T3-3c: Set system property at compile time
- [ ] **T3-4**: Verification
  - [ ] T3-4a: Verify `ItemDataHandler120` used in 1.20.6 build
  - [ ] T3-4b: Verify `ItemDataHandler121` used in 1.21.1 build
  - [ ] T3-4c: Create unit tests (if possible)

**Duration**: 5-7 days
**Risk**: Medium (potential compile errors)

---

### Phase 4: Migrate Existing Code (Priority: Medium)

**Goal**: Refactor existing 47 files to use abstraction layer

#### Task List

- [ ] **T4-1**: Migrate `items/` package
  - [ ] T4-1a: Refactor `PortalStabilizerItem.java`
  - [ ] T4-1b: Refactor `TimeClockItem.java`
  - [ ] T4-1c: Other items (~10 files)
- [ ] **T4-2**: Migrate `data/` package
  - [ ] T4-2a: Modify `ChronoDawnWorldData.java` `save()` method
  - [ ] T4-2b: Modify `PortalRegistryData.java`
  - [ ] T4-2c: Other SavedData classes (~5 files)
- [ ] **T4-3**: Migrate `entities/bosses/` package
  - [ ] T4-3a: Modify `TimeTyrantEntity.java` NBT handling
  - [ ] T4-3b: Other boss entities (~5 files)
- [ ] **T4-4**: Migrate `blocks/` package
  - [ ] T4-4a: Modify `BossRoomDoorBlockEntity.java`
  - [ ] T4-4b: Other BlockEntities (~3 files)
- [ ] **T4-5**: Verification
  - [ ] T4-5a: Verify no direct `getOrCreateTag()` calls in all files
  - [ ] T4-5b: Verify successful build (both versions)

**Duration**: 10-14 days
**Risk**: High (massive file changes, potential bugs)

#### Progress Report (2026-01-02)

**Status**: **In Progress** - Extended migration approach (T401-T427)

**Note**: Target version changed from 1.20.6 to **1.20.1** during implementation to maximize version coverage.

**Completed Tasks**:

- ✅ **T4-1: ItemStack DataComponents Migration (1 file)**
  - `TimeCompassItem.java`: Migrated to `CompatHandlers.ITEM_DATA`
  - Pattern: `DataComponents.CUSTOM_DATA` → `CompatHandlers.ITEM_DATA.setString()`

- ✅ **T4-2: SavedData Migration (7 files)**
  - `ChronoDawnGlobalState.java`, `ChronoDawnWorldData.java`, `ChronoDawnTimeData.java`, `TimeKeeperVillageData.java`
  - `DimensionStateData.java`, `PlayerProgressData.java`, `PortalRegistryData.java`
  - Pattern: Extend `CompatSavedData`, implement `saveData(CompoundTag)` / `loadData(CompoundTag)`
  - Result: Version-independent save/load logic

- ✅ **T4-3: BlockEntity Migration (3 files)**
  - `BossRoomDoorBlockEntity.java`, `ClockTowerTeleporterBlockEntity.java`, `BossRoomBoundaryMarkerBlockEntity.java`
  - Pattern: Extend `CompatBlockEntity`, implement `saveData(CompoundTag)` / `loadData(CompoundTag)`

- ✅ **T4-4: Extended API Survey**
  - Identified additional version-specific APIs beyond initial 47 files
  - Categories: TreeGrower, BootstrapContext, Entity APIs, Tooltip APIs, ArmorMaterial, GUI APIs

- ✅ **T4-5: TreeGrower Migration (3 saplings × 2 versions = 6 files)**
  - Version-specific implementations in `compat/v1_20_1/blocks/` and `compat/v1_21_1/blocks/`
  - **1.20.1**: `AbstractTreeGrower` inheritance (custom inner class)
  - **1.21.1**: `TreeGrower` instance usage
  - Files: `TimeWoodSapling.java`, `AncientTimeWoodSapling.java`, `DarkTimeWoodSapling.java`
  - Original files in `blocks/` removed

- ✅ **T4-6: BootstrapContext Removal (3 files)**
  - `FruitOfTimeTreeFeature.java`, `AncientTimeWoodTreeFeature.java`, `DarkTimeWoodTreeFeature.java`
  - **Discovery**: `bootstrap()` methods were unused - tree configs are JSON-defined
  - Solution: Removed `bootstrap()` and `createTreeConfiguration()` methods
  - Result: Simplified to ResourceKey constants only (version-independent)

- ✅ **T4-7: Entity APIs (SynchedEntityData.Builder) - 11 files**
  - Initial 5 files: TimeKeeperEntity, GearProjectileEntity, ClockworkColossusEntity, ChronosWardenEntity, TimeGuardianEntity
  - Additional 6 files discovered: TemporalPhantomEntity, EntropyKeeperEntity, TimeTyrantEntity, TimeBlastEntity, ChronoDawnChestBoat, ChronoDawnBoat
  - **Solution**: Moved all 11 Entity files to version-specific directories (v1_20_1/entities/, v1_21_1/entities/)
  - **1.20.1 API**: `protected void defineSynchedData()` (no parameter, use `this.entityData.define()`)
  - **1.21.1 API**: `protected void defineSynchedData(SynchedEntityData.Builder builder)` (builder parameter)
  - **T4-7.1**: Fixed TimeKeeperEntity `ItemCost` issue
    - `ItemCost` class is new in 1.21.1, doesn't exist in 1.20.1
    - Replaced `new ItemCost(item, count)` with `new ItemStack(item, count)` for 1.20.1
    - Changed `Optional.of(new ItemCost(...))` to direct `new ItemStack(...)` in MerchantOffer constructor

**Remaining Tasks**:

- 📋 **T4-8: Tooltip APIs (TooltipContext) - 2 files**
  - ChronoAegisItem, TimeCompassItem (appendHoverText method)

- 📋 **T4-9: ArmorMaterial.Layer - 2 files**
  - ClockstoneArmorMaterial, EnhancedClockstoneArmorMaterial

- 📋 **T4-10: Remaining APIs - 3 files**
  - ClientAdvancementsAccessor (AdvancementHolder)
  - AnvilMenuMixin (DataComponents)
  - EntryPageWidget (GUI APIs)

- 📋 **T4-11: Final Verification**
  - Build verification for both 1.20.1 and 1.21.1

**Implementation Patterns Discovered**:

1. **CompatSavedData/CompatBlockEntity Package Structure**:
   ```
   Location: compat/v1_20_1/CompatSavedData.java, compat/v1_21_1/CompatSavedData.java
   Package: package com.chronodawn.compat;  // Version-neutral
   Import:  import com.chronodawn.compat.CompatSavedData;  // No version in path
   ```
   Gradle exclude rules select correct implementation at build time.

2. **Version-Specific File Separation**:
   - Required when API structure fundamentally differs (inheritance → instantiation)
   - Both versions use same package declaration for seamless integration
   - Example: TreeGrower (AbstractTreeGrower vs TreeGrower class)

3. **Code Simplification Opportunities**:
   - Data generation code (bootstrap methods) may be unused
   - Modern Minecraft uses JSON-based feature definitions
   - Verify actual usage before implementing compatibility layer

**Build Status** (Updated: 2026-01-09):
- **1.21.1**: ✅ BUILD SUCCESSFUL (91/91 game tests passed)
- **1.20.1**: ⚠️ BUILD FAILED (84 errors remaining, mostly Model files)

**Phase 4 Extended Progress (2026-01-09)**:

**Additional Fixes Completed (T4-13)**:
- ✅ BlockSetType constructor: Added canOpenByWindCharge, canButtonBeActivatedByArrows, pressurePlateSensitivity parameters
- ✅ TrapDoorBlock: Changed to (BlockSetType, Properties) constructor, use() → useWithoutItem()
- ✅ ArmorMaterial: Migrated interface → record (TimeTyrant, Clockstone, EnhancedClockstone)
- ✅ Tier: Removed getLevel() method (deprecated in 1.21.1)
- ✅ Item.appendHoverText(): TooltipContext parameter
- ✅ Mob.finalizeSpawn(): Reduced from 5 to 4 parameters (1.21.1)

**1.20.1 Compatibility Fixes (T4-14, ongoing)**:
- ✅ PressurePlateBlock: Added Sensitivity parameter (3 files)
- ✅ MapCodec/simpleCodec: Removed 1.21-only methods (2 files)
- ✅ FoodProperties.alwaysEdible(): Removed (1.21+ only)
- ✅ ResourceLocation.parse(): Changed to constructor
- ✅ GameTestHelper.makeMockPlayer(): Removed GameType parameter (6 locations)
- ✅ Mob.finalizeSpawn(): Added SpawnGroupData/CompoundTag parameters (1.20.1 needs 5 params)
- ✅ ItemStack.hurtAndBreak(): Consumer-based API (2 locations)

**Remaining Issues (84 errors)**:
- ❌ EntityModel.renderToBuffer() signature change (9 model files × ~6-12 errors each)
  - 1.20.1: `render(PoseStack, VertexConsumer, int, int, float, float, float, float)` (RGBA floats)
  - 1.21.1: `render(PoseStack, VertexConsumer, int, int, int)` (packed color int)
  - Affected: FloqModel, TimeTyrantModel, TimeKeeperModel, TimeGuardianModel, etc.
- ❌ ChronoAegisEffect.applyEffectTick() override signature
- ❌ Minor issues in TimeArrowItem, SkyColorMixin, DeferredSpawnEggItem, TimeBlastRenderer

**Commits**:
- `67f40cd`: Phase 4 complete (1.21.1 API compatibility)
- `2a45ae4`: Phase 5 complete (Build tasks, JAR naming, documentation)
- `a286a04`: 1.20.1 fixes part 1 (PressurePlate, MapCodec, alwaysEdible, ResourceLocation)
- `d320402`: 1.20.1 fixes part 2 (makeMockPlayer, finalizeSpawn, hurtAndBreak)

**Next Steps**:
1. Fix EntityModel render method signatures (9 files, version-specific implementation likely needed)
2. Fix remaining 4 files (ChronoAegisEffect, TimeArrowItem, SkyColorMixin, etc.)
3. Verify 1.20.1 build success
4. Run Phase 6 integration tests

**Files Migrated**: 50+ files (including Phase 4 and Phase 5 changes)
**Estimated Time to Complete 1.20.1 Build**: 2-3 hours (Model layer refactoring)

---

### Phase 5: Build Script Enhancement (Priority: Low)

**Goal**: CI/CD support and improved developer experience

#### Task List

- [ ] **T5-1**: Add Gradle tasks
  - [ ] T5-1a: Create `build1206` task (1.20.6 build shortcut)
  - [ ] T5-1b: Create `build1211` task (1.21.1 build shortcut)
  - [ ] T5-1c: Create `buildAll` task (sequential build for all versions)
- [ ] **T5-2**: Rename output JARs
  - [ ] T5-2a: Change to `chronodawn-0.1.0+1.20.6-fabric.jar` format
  - [ ] T5-2b: Change to `chronodawn-0.1.0+1.21.1-neoforge.jar` format
- [ ] **T5-3**: Update GitHub Actions workflow (if applicable)
  - [ ] T5-3a: Configure matrix build (1.20.6 / 1.21.1)
  - [ ] T5-3b: Configure artifact upload
- [ ] **T5-4**: Update documentation
  - [ ] T5-4a: Update build instructions in `README.md`
  - [ ] T5-4b: Add Gradle multi-version setup to `CLAUDE.md`

**Duration**: 3-4 days
**Risk**: Low

---

### Phase 6: Integration Testing and Verification (Priority: High)

**Goal**: Verify operation in both versions

#### Task List

- [ ] **T6-1**: Verify 1.20.6 build
  - [ ] T6-1a: Verify successful build
  - [ ] T6-1b: Verify startup in Minecraft 1.20.6
  - [ ] T6-1c: Verify Data Pack loaded correctly (plural folders)
  - [ ] T6-1d: Verify Portal Stabilizer operation (ItemStack NBT)
  - [ ] T6-1e: Verify SavedData save/load
- [ ] **T6-2**: Verify 1.21.1 build
  - [ ] T6-2a: Verify successful build
  - [ ] T6-2b: Verify startup in Minecraft 1.21.1
  - [ ] T6-2c: Verify Data Pack loaded correctly (singular folders)
  - [ ] T6-2d: Verify Portal Stabilizer operation (ItemStack Components)
  - [ ] T6-2e: Verify SavedData save/load
- [ ] **T6-3**: Bug fixes
  - [ ] T6-3a: Fix discovered bugs
  - [ ] T6-3b: Re-test
- [ ] **T6-4**: Performance verification
  - [ ] T6-4a: Compare startup time (between versions)
  - [ ] T6-4b: Check memory usage

**Duration**: 5-7 days
**Risk**: High (potential unexpected bugs)

---

## 5. Verification Methods

### 5.1 Build Verification

#### Command Examples

```bash
# 1.20.6 build
./gradlew clean build -Ptarget_mc_version=1.20.6

# 1.21.1 build
./gradlew clean build -Ptarget_mc_version=1.21.1

# All versions build (after Phase 5 implementation)
./gradlew buildAll
```

#### Success Criteria

- ✅ No compile errors
- ✅ JAR files generated successfully (`fabric/build/libs/`, `neoforge/build/libs/`)
- ✅ JAR file names correct (including version number)

### 5.2 Runtime Verification

#### Checklist

| Category | Verification Item | 1.20.6 | 1.21.1 |
|---------|-------------------|--------|--------|
| **Startup** | Minecraft starts | ☐ | ☐ |
| **Logs** | No error logs | ☐ | ☐ |
| **Data Pack** | pack.mcmeta loaded | ☐ | ☐ |
| **Data Pack** | Advancements work | ☐ | ☐ |
| **Data Pack** | Recipes work | ☐ | ☐ |
| **ItemStack** | Portal Stabilizer usable | ☐ | ☐ |
| **ItemStack** | Time Clock displays time correctly | ☐ | ☐ |
| **SavedData** | Portal info saved | ☐ | ☐ |
| **SavedData** | Data persists after world reload | ☐ | ☐ |
| **Entity** | Bosses spawn correctly | ☐ | ☐ |
| **Entity** | Boss drops correct | ☐ | ☐ |

### 5.3 Code Quality Verification

#### Static Analysis

```bash
# Detect locations calling NBT/Component API directly without abstraction layer
grep -r "getOrCreateTag()\|getOrCreateTagElement\|set(DataComponents" common/src/main/java/com/chronodawn/ \
  | grep -v "compat/" \
  | grep -v "// OK:"

# Expected: 0 results (all go through compat/)
```

#### Testing

```bash
# Unit tests implemented in Phase 3+
./gradlew test -Ptarget_mc_version=1.20.6
./gradlew test -Ptarget_mc_version=1.21.1
```

---

## 6. Risks and Mitigation

### 6.1 Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Abstraction layer design flaw** | High | Medium | Thorough investigation in Phase 2, validate with small prototype |
| **Gradle build script complexity** | Medium | Low | Maintain simple design, add comprehensive comments |
| **Version-specific bug oversight** | High | High | Thorough integration testing in Phase 6, real testing in both versions |
| **Dependency library incompatibility** | Medium | Low | Verify Architectury API version differences, create wrappers if needed |
| **Performance degradation** | Low | Low | Inline abstraction layer, minimize overhead |

### 6.2 Project Management Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Work estimation error** | Medium | Medium | Assess implementation difficulty in Phase 1-2, adjust plan if needed |
| **Breaking existing features** | High | Medium | Test progressively in each Phase, maintain strict Git version control |
| **Reduced AI development efficiency** | Medium | Low | Design clear abstraction layer interfaces, document thoroughly |

### 6.3 Rollback Plan

If implementation becomes difficult in Phase 3-4:

1. **Plan B**: Multi-version Data Pack only, Git branch separation for Java code
2. **Plan C**: Revert to 1.21.1 only, postpone 1.20.6 support

---

## 7. References

### 7.1 Reference Links

- [Minecraft 1.20.4 → 1.20.5 Migration Primer](https://gist.github.com/ChampionAsh5357/53b04132e292aa12638d339abfabf955)
- [Minecraft 1.20.5/6 → 1.21 Migration Primer](https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f)
- [Fabric Custom Data Components Guide](https://docs.fabricmc.net/develop/items/custom-data-components)
- [Minecraft Wiki - Data Component Format](https://minecraft.wiki/w/Data_component_format)
- [Minecraft Wiki - Pack Format](https://minecraft.wiki/w/Pack_format)
- [Fabric for Minecraft 1.20.5 & 1.20.6](https://fabricmc.net/2024/04/19/1205.html)

### 7.2 Glossary

| Term | Description |
|------|-------------|
| **Data Pack** | Minecraft's data-driven content (advancements, loot_tables, recipes, etc.) |
| **pack_format** | Data Pack version number (1.20.6=41, 1.21.1=48) |
| **NBT** | Named Binary Tag, Minecraft's data storage format (used for ItemStack until 1.20.6) |
| **DataComponents** | New ItemStack data storage system introduced in 1.20.5+ |
| **SavedData** | World data persistence API (saved to `data/<mod_id>_<name>.dat`) |
| **HolderLookup.Provider** | Registry access interface required in 1.21.1+ |
| **Architectury** | Toolchain supporting multi-loader (Fabric/NeoForge) development |
| **compat package** | Abstraction layer that absorbs API differences between versions |

### 7.3 File List (Phase 4 Targets)

**ItemStack operation related (47 files)**:
```
common/src/main/java/com/chronodawn/
├── items/
│   ├── PortalStabilizerItem.java
│   ├── TimeClockItem.java
│   ├── TimeCompassItem.java
│   ├── ChronoAegisItem.java
│   ├── DecorativeWaterBucketItem.java
│   └── tools/TimeClockItem.java
├── entities/bosses/
│   ├── TimeTyrantEntity.java
│   ├── TimeGuardianEntity.java
│   ├── TemporalPhantomEntity.java
│   ├── EntropyKeeperEntity.java
│   └── ClockworkColossusEntity.java
├── data/
│   ├── ChronoDawnWorldData.java
│   ├── PortalRegistryData.java
│   ├── PlayerProgressData.java
│   ├── DimensionStateData.java
│   ├── ChronoDawnGlobalState.java
│   ├── ChronoDawnTimeData.java
│   └── TimeKeeperVillageData.java
└── blocks/
    ├── BossRoomDoorBlockEntity.java
    ├── BossRoomDoorBlock.java
    └── EntropyCryptTrapdoorBlock.java
... (other 20+ files)
```

---

## Next Steps

1. **Review this document**: Confirm plan details with user
2. **Start Phase 1**: Begin with Data Pack integration
3. **Regular progress checks**: Verify at end of each Phase

---

**History**:
- 2026-01-01: Initial version created (Phase 1-6 planning)
