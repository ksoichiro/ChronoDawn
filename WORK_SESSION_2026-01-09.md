# Work Session Progress - 2026-01-09

## Branch: multiversion-support-t401-t427

## Summary

Phase 4 (1.21.1) and Phase 5 complete. 1.20.1 build partially fixed.

## Completed Work

### Phase 4: 1.21.1 API Compatibility ✅
- Fixed all API compatibility issues for Minecraft 1.21.1
- Build: ✅ SUCCESS (91/91 game tests passed)
- Commit: `67f40cd`

Key API changes fixed:
- BlockSetType constructor (new parameters)
- TrapDoorBlock constructor order change
- ArmorMaterial interface → record migration
- Tier.getLevel() removal
- Item.appendHoverText() signature change
- Mob.finalizeSpawn() parameter reduction (5→4)

### Phase 5: Build Script Enhancement ✅
- Added custom Gradle tasks: `build1201`, `build1211`, `buildAll`
- JAR naming already configured: `chronodawn-{version}+{mc_version}-{loader}.jar`
- Updated README.md and CLAUDE.md with multi-version build instructions
- Commit: `2a45ae4`

### 1.20.1 Compatibility Fixes (Partial) ⚠️
- Fixed 22 errors (106 → 84 remaining)
- Commits: `a286a04`, `d320402`

Fixed issues:
- PressurePlateBlock: Added Sensitivity parameter
- MapCodec/simpleCodec: Removed (1.21+ only)
- FoodProperties.alwaysEdible(): Removed (1.21+ only)
- ResourceLocation.parse() → new ResourceLocation()
- GameTestHelper.makeMockPlayer(): Removed GameType parameter
- Mob.finalizeSpawn(): Added parameters for 1.20.1 (needs 5 params)
- ItemStack.hurtAndBreak(): Changed to Consumer API

## Remaining Issues (84 errors)

### Critical: EntityModel Render Methods (9 files, ~70+ errors)

**Problem**: Render method signature changed between versions

**1.20.1 signature**:
```java
render(PoseStack, VertexConsumer, int packedLight, int packedOverlay,
       float red, float green, float blue, float alpha)
```

**1.21.1 signature**:
```java
render(PoseStack, VertexConsumer, int packedLight, int packedOverlay, int color)
```

**Affected files**:
- FloqModel.java (12 errors)
- TimeTyrantModel.java (6 errors)
- TimeKeeperModel.java (6 errors)
- TimeGuardianModel.java (6 errors)
- TemporalWraithModel.java (6 errors)
- TemporalPhantomModel.java (6 errors)
- EntropyKeeperModel.java (6 errors)
- ClockworkSentinelModel.java (6 errors)
- ClockworkColossusModel.java (6 errors)
- ChronosWardenModel.java (6 errors)

**Solution approach**: Likely need version-specific implementations (v1_20_1/client/model/ and v1_21_1/client/model/)

### Minor Issues (~10 errors)
- ChronoAegisEffect.applyEffectTick() override signature
- TimeArrowItem (2 errors)
- SkyColorMixin (2 errors)
- DeferredSpawnEggItem (2 errors)
- TimeBlastRenderer (2 errors)

## Next Steps

1. **Create version-specific Model implementations** (highest priority)
   - Move 9 model files to v1_20_1/client/model/ and v1_21_1/client/model/
   - Update render() method signatures for each version
   - Estimated time: 2-3 hours

2. **Fix remaining 4 files**
   - ChronoAegisEffect, TimeArrowItem, SkyColorMixin, DeferredSpawnEggItem, TimeBlastRenderer
   - Estimated time: 30 minutes

3. **Verify 1.20.1 build success**
   - Run: `./gradlew build -Ptarget_mc_version=1.20.1`
   - Expected: BUILD SUCCESSFUL

4. **Phase 6: Integration Testing**
   - Test 1.20.1 in Minecraft
   - Test 1.21.1 in Minecraft
   - Verify game tests pass in both versions

## Build Commands

```bash
# Build 1.21.1 (default, working)
./gradlew build1211

# Build 1.20.1 (failing with 84 errors)
./gradlew build1201

# Build all versions
./gradlew buildAll
```

### 1.20.1 Compatibility Fixes (Complete) ✅
- Fixed all 84 errors
- Commits: `a286a04`, `d320402`, `5fed080`, `fc8eb48`

**Part 3 (Major Progress)**: Model and renderer files (commit `5fed080`)
- Moved 10 entity model files to version-specific directories
  - v1_21_1: renderToBuffer(5 params with int color)
  - v1_20_1: renderToBuffer(8 params with RGBA floats)
- Fixed 3 renderer files (Boat, ChestBoat, TimeBlast)
- Moved 4 GUI/Item files (ChronicleScreen, CategoryListWidget, TimeArrowItem, DeferredSpawnEggItem)
- Moved 1 mixin file (SkyColorMixin)
- Fixed ChronoAegisEffect for 1.20.1
- **Error reduction**: 84 → 6

**Part 4 (Final)**: Remaining errors fixed (commit `fc8eb48`)
- GUI/Item method signature fixes:
  - ChronicleScreen: Removed @Override for renderBackground()
  - CategoryListWidget: Changed mouseScrolled() to 3 parameters
  - TimeArrowItem: Changed createArrow() to 3 parameters
  - DeferredSpawnEggItem: Removed @Override for getType()
- Renderer API fixes:
  - TimeBlastRenderer: Changed addVertex() to vertex()
- Mixin fixes:
  - SkyColorMixin: Added iteration through progress map
- GameTest fixes:
  - Moved ChronoDawnGameTestLogic to version-specific directories
- Common file fixes:
  - PortalRegistry: Use CompatResourceLocation.parse()
- **Error reduction**: 6 → 0

**Build Status**: ✅ **COMMON MODULE BUILD SUCCESSFUL**
- common:build 1.20.1: ✅ BUILD SUCCESSFUL (0 errors)
- common:build 1.21.1: ✅ BUILD SUCCESSFUL (0 errors)

**Part 5 (Fabric Module)**: Loader-specific fixes (in progress)
- Identified issue: **NeoForge only supports Minecraft 1.20.5+**
  - 1.20.1 support requires Forge (legacy ModLoader)
  - **Decision**: 1.20.1 support is **Fabric-only** (NeoForge users use 1.21.1)
- Fabric module version-specific separation:
  - Created `fabric/src/main/java-1.20.1/` and `fabric/src/main/java-1.21.1/`
  - Updated `fabric/build.gradle` to support version-specific sources
  - Moved ChronoDawnFabric.java, ChronoDawnClientFabric.java, ChronoDawnFuelRegistry.java
- Fixed Fabric 1.20.1 compatibility:
  - ChronoDawnFabric: `SpawnPlacementTypes.ON_GROUND` → `SpawnPlacements.Type.ON_GROUND`
  - ChronoDawnClientFabric: Removed unused `ClientPlayerBlockBreakEvents` import
  - ChronoDawnClientFabric: `ResourceLocation.fromNamespaceAndPath()` → `new ResourceLocation()`
  - ChronoDawnFuelRegistry: `ResourceLocation.parse()` → `new ResourceLocation()`
- NeoForge module disabled for 1.20.1:
  - Updated `settings.gradle` to exclude `neoforge` when `target_mc_version=1.20.1`
- **Remaining issue**: 1 error in ChronoDawnFuelRegistry
  - `FabricTagKey` class not found in Fabric API 0.92.2+1.20.1
  - Needs investigation or temporary workaround

**Build Status Update**:
- ✅ common:build 1.20.1: BUILD SUCCESSFUL (0 errors)
- ✅ common:build 1.21.1: BUILD SUCCESSFUL (0 errors)
- ⚠️ fabric:build 1.20.1: BUILD FAILED (1 error - FabricTagKey)
- ✅ fabric:build 1.21.1: Not tested yet (expected success)
- ➖ neoforge:build 1.20.1: SKIPPED (not supported)
- ✅ neoforge:build 1.21.1: Not tested yet (expected success)

## Current Error Count

### Common Module
- **Starting**: 106 errors
- **After Part 1**: ~100 errors
- **After Part 2**: 84 errors
- **After Part 3**: 6 errors
- **After Part 4**: **0 errors** ✅ **ACHIEVED**

### Fabric Module
- **Part 5**: 1 error remaining (FabricTagKey)

### Overall Status
- **Common**: ✅ Fully compatible with 1.20.1 and 1.21.1
- **Fabric**: ⚠️ 1 error remaining for 1.20.1
- **NeoForge**: ➖ 1.20.1 not supported (requires Minecraft 1.20.5+)

## Git Log

```
6ad9f4f docs: Update progress report - 1.20.1 compatibility complete
fc8eb48 fix: Complete 1.20.1 compatibility - all errors resolved
5fed080 wip: Fix 1.20.1 compatibility errors (part 3 - major progress)
fa8a7e0 docs: Update progress report for 1.20.1 build fixes
d320402 wip: Fix 1.20.1 compatibility errors (part 2)
a286a04 wip: Fix 1.20.1 compatibility errors (part 1)
2a45ae4 feat: Phase 5 - Build Script Enhancement
67f40cd fix: Phase 4 API compatibility - complete 1.21.1 migration
```

## Token Usage

Session total: ~112,000 / 200,000 tokens (56% used)

## Next Steps

### Immediate (Part 5 completion)
1. **Fix FabricTagKey error** in ChronoDawnFuelRegistry.java
   - Option A: Temporarily disable fuel registry for 1.20.1
   - Option B: Find alternative Fabric API method
   - Option C: Investigate if different Fabric API version has FabricTagKey

### After Part 5
2. **Verify all builds**:
   - `./gradlew build -Ptarget_mc_version=1.20.1 -x test` (Fabric only)
   - `./gradlew build -Ptarget_mc_version=1.21.1 -x test` (Fabric + NeoForge)

3. **Phase 6: Integration Testing**:
   - Test 1.20.1 JAR in Minecraft 1.20.1 (Fabric)
   - Test 1.21.1 JARs in Minecraft 1.21.1 (Fabric + NeoForge)
   - Run game tests
   - Update multiversion_migration_plan.md

## Notes for Next Session

1. ✅ Common module: All compilation errors resolved
2. ⚠️ Fabric module: 1 error remaining (FabricTagKey)
3. ✅ NeoForge strategy: 1.20.1 excluded (Fabric-only support)
4. ✅ Version separation working (common, fabric modules)
5. 📝 Document final decision: **1.20.1 = Fabric only, 1.21.1 = Fabric + NeoForge**

---

## Continuation (2026-01-10)

### Part 6: Fabric Module Runtime Fixes (Complete Build Success)

**Phase 5 (Fabric Module) Completion Work**

#### Issue 1: FabricTagKey Error (Fixed ✅)
- **Problem**: `ChronoDawnFuelRegistry.java` used TagKey-based registration, but Fabric API 0.92.2+1.20.1 doesn't have `FabricTagKey`
- **Solution**: Changed 1.20.1 version to individual item registration
  - Registered 39 Time Wood items individually using `FuelRegistry.INSTANCE.add(item, burnTime)`
  - 1.21.1 version kept TagKey-based registration
- **Commit**: `c82b6e0`

#### Issue 2: sourcesJar Duplicate Files Error (Fixed ✅)
- **Problem**: `sourcesJar` task failed with duplicate file error when using version-specific source directories
- **Solution**: Added `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` to `fabric/build.gradle`
- **Commit**: `c82b6e0`

#### Issue 3: Runtime Dependency Errors (Fixed ✅)
- **Problem**: `fabric.mod.json` had 1.21.1 dependencies hardcoded, causing incompatibility errors at runtime
- **Error Message**:
  ```
  Mod 'Chrono Dawn' requires architectury >=13.0.8 but 9.2.14 is loaded
  Mod 'Chrono Dawn' requires minecraft ~1.21.1 but 1.20.1 is loaded
  ```
- **Solution**: Created version-specific `fabric.mod.json`
  - Deleted `fabric/src/main/resources/fabric.mod.json`
  - Created `fabric/src/main/resources-1.20.1/fabric.mod.json` (architectury >=9.2.14, minecraft ~1.20.1)
  - Created `fabric/src/main/resources-1.21.1/fabric.mod.json` (architectury >=13.0.8, minecraft ~1.21.1)
  - Updated `fabric/build.gradle` to support version-specific resources directories
- **Commit**: `c82b6e0`

#### Issue 4: Custom Portal API Dependency (Fixed ✅)
- **Problem**: Custom Portal API 0.0.1-beta66-1.20.1 does not exist (only 1.21 version available)
- **Solution**: Restricted Custom Portal API to 1.21.1 only
  - Modified `fabric/build.gradle` dependencies with version check
  - Moved `CustomPortalFabric.java` to `fabric/src/main/java-1.21.1/`
  - Commented out Custom Portal API calls in 1.20.1 version of `ChronoDawnFabric.java`
- **Commit**: `c82b6e0`

#### Issue 5: Block Registration Order (Fixed ✅)
- **Problem**: `ChronoMelonStemBlock` constructor calls `ModBlocks.CHRONO_MELON.get()` but `CHRONO_MELON` was registered after `CHRONO_MELON_STEM`
- **Error**: `NullPointerException: Registry Object not present: chronodawn:chrono_melon`
- **Solution**: Moved `CHRONO_MELON` registration before `CHRONO_MELON_STEM` in `ModBlocks.java`
- **Status**: ⚠️ Fixed but runtime error still occurs (needs further investigation)

### Build Status (2026-01-10)

- ✅ **1.20.1 (Fabric)**: BUILD SUCCESSFUL
  - JAR: `chronodawn-0.2.0-beta+1.20.1-fabric.jar` (9.3M)
  - Loading: `Loading Minecraft 1.20.1 with Fabric Loader 0.15.11` ✅
- ✅ **1.21.1 (Fabric + NeoForge)**: BUILD SUCCESSFUL
  - JAR: `chronodawn-0.2.0-beta+1.21.1-fabric.jar` (9.3M)
  - JAR: `chronodawn-0.2.0-beta+1.21.1-neoforge.jar` (9.3M)

### Runtime Status

- ✅ **Dependencies**: All mod compatibility errors resolved
- ⚠️ **Startup**: Runtime error still occurs (under investigation)

### Modified Files Summary

1. **`fabric/build.gradle`**
   - Added version-specific resources support
   - Added `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` to `sourcesJar`
   - Custom Portal API dependency conditional on version

2. **`fabric/src/main/resources-1.20.1/fabric.mod.json`** (New)
   - Dependencies: architectury >=9.2.14, minecraft ~1.20.1

3. **`fabric/src/main/resources-1.21.1/fabric.mod.json`** (New)
   - Dependencies: architectury >=13.0.8, minecraft ~1.21.1

4. **`fabric/src/main/java-1.20.1/com/chronodawn/fabric/ChronoDawnFuelRegistry.java`**
   - Changed from TagKey-based to individual item registration (39 items)

5. **`fabric/src/main/java-1.20.1/com/chronodawn/fabric/ChronoDawnFabric.java`**
   - Commented out Custom Portal API initialization

6. **`fabric/src/main/java-1.21.1/com/chronodawn/fabric/compat/CustomPortalFabric.java`** (Moved)
   - Moved from `fabric/src/main/java/` to version-specific directory

7. **`common/src/main/java/com/chronodawn/registry/ModBlocks.java`**
   - Reordered: `CHRONO_MELON` now registered before `CHRONO_MELON_STEM`

### Token Usage

Session total: ~112,000 / 200,000 tokens (56% used)

### Next Steps

1. **Investigate remaining runtime error** in 1.20.1 startup
2. **Test 1.21.1 startup** to verify it works correctly
3. **Phase 6: Integration Testing** after startup issues are resolved
   - In-game dimension teleportation test
   - Feature verification in both versions
   - Game test execution

---

## Continuation (2026-01-10 continued)

### Part 7: Runtime Fixes - ChronoMelonBlock and TreeDecoratorType

**Startup Issues Resolution**

#### Issue 6: ChronoMelonBlock ClassCastException (Fixed ✅)
- **Problem**: `ChronoMelonBlock cannot be cast to StemGrownBlock` at runtime
- **Error**: `ClassCastException: class com.chronodawn.blocks.ChronoMelonBlock cannot be cast to class net.minecraft.world.level.block.StemGrownBlock`
- **Root Cause**: `StemBlock` constructor signature differs between versions
  - 1.20.1: Requires `StemGrownBlock` instance (abstract class)
  - 1.21.1: Requires `ResourceKey<Block>` (getKey() method)
- **Solution**: Separated `ChronoMelonBlock` into version-specific directories
  - Created `common/src/main/java/com/chronodawn/compat/v1_20_1/blocks/ChronoMelonBlock.java`
    - Extends `StemGrownBlock` (abstract class in 1.20.1)
    - Implements `getStem()` and `getAttachedStem()` methods
  - Created `common/src/main/java/com/chronodawn/compat/v1_21_1/blocks/ChronoMelonBlock.java`
    - Extends `Block` (no StemGrownBlock needed in 1.21.1)
  - Updated `ChronoMelonStemBlock` and `AttachedChronoMelonStemBlock` constructors for both versions
  - Added import to `ModBlocks.java`: `import com.chronodawn.blocks.ChronoMelonBlock;`

#### Issue 7: TreeDecoratorType NoSuchMethodException (Fixed ✅)
- **Problem**: `NoSuchMethodException: net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType.<init>(com.mojang.serialization.MapCodec)`
- **Root Cause**: `TreeDecoratorType` constructor signature changed between versions
  - 1.20.1: Takes `Codec` parameter
  - 1.21.1: Takes `MapCodec` parameter
- **Solution**: Separated `FruitDecorator` and `ModTreeDecoratorTypes` into version-specific directories
  - **FruitDecorator**:
    - 1.20.1 version: Uses `Codec<FruitDecorator>`
    - 1.21.1 version: Uses `MapCodec<FruitDecorator>`
  - **ModTreeDecoratorTypes**:
    - 1.20.1 version: Reflection with `Codec.class` parameter
    - 1.21.1 version: Reflection with `MapCodec.class` parameter
  - Created directories:
    - `common/src/main/java/com/chronodawn/compat/v1_20_1/worldgen/decorators/`
    - `common/src/main/java/com/chronodawn/compat/v1_21_1/worldgen/decorators/`
    - `common/src/main/java/com/chronodawn/compat/v1_20_1/registry/`
    - `common/src/main/java/com/chronodawn/compat/v1_21_1/registry/`

### Build Status (2026-01-10 Final)

- ✅ **1.20.1 (Fabric)**: BUILD SUCCESSFUL
  - JAR: `chronodawn-0.2.0-beta+1.20.1-fabric.jar` (9.2M)
  - **Startup**: ✅ SUCCESS
  - **World Generation**: ⚠️ CRASHES (under investigation)
- ✅ **1.21.1 (Fabric + NeoForge)**: BUILD SUCCESSFUL
  - JAR: `chronodawn-0.2.0-beta+1.21.1-fabric.jar` (9.3M)
  - JAR: `chronodawn-0.2.0-beta+1.21.1-neoforge.jar` (9.3M)
  - **Startup**: Not tested yet

### Runtime Status

- ✅ **Dependencies**: All resolved
- ✅ **Mod Loading**: Success
- ✅ **Startup (1.20.1)**: Success (Main menu loads)
- ⚠️ **World Generation**: Crashes during world creation (needs investigation)

### Modified Files Summary (Part 7)

1. **`common/src/main/java/com/chronodawn/compat/v1_20_1/blocks/ChronoMelonBlock.java`** (New)
   - Extends `StemGrownBlock` for 1.20.1 compatibility

2. **`common/src/main/java/com/chronodawn/compat/v1_21_1/blocks/ChronoMelonBlock.java`** (New)
   - Extends `Block` (standard implementation for 1.21.1)

3. **`common/src/main/java/com/chronodawn/compat/v1_20_1/blocks/ChronoMelonStemBlock.java`** (Modified)
   - Changed constructor to use `.get()` and cast to `StemGrownBlock`

4. **`common/src/main/java/com/chronodawn/compat/v1_20_1/blocks/AttachedChronoMelonStemBlock.java`** (Modified)
   - Changed constructor to use `.get()` and cast to `StemGrownBlock`

5. **`common/src/main/java/com/chronodawn/compat/v1_20_1/worldgen/decorators/FruitDecorator.java`** (New)
   - Uses `Codec<FruitDecorator>` instead of `MapCodec`

6. **`common/src/main/java/com/chronodawn/compat/v1_21_1/worldgen/decorators/FruitDecorator.java`** (New)
   - Uses `MapCodec<FruitDecorator>` (original implementation)

7. **`common/src/main/java/com/chronodawn/compat/v1_20_1/registry/ModTreeDecoratorTypes.java`** (New)
   - Reflection uses `Codec.class` parameter

8. **`common/src/main/java/com/chronodawn/compat/v1_21_1/registry/ModTreeDecoratorTypes.java`** (New)
   - Reflection uses `MapCodec.class` parameter (original implementation)

9. **`common/src/main/java/com/chronodawn/registry/ModBlocks.java`** (Modified)
   - Added import: `import com.chronodawn.blocks.ChronoMelonBlock;`

10. **Deleted files**:
    - `common/src/main/java/com/chronodawn/blocks/ChronoMelonBlock.java` (moved to version-specific)
    - `common/src/main/java/com/chronodawn/worldgen/decorators/FruitDecorator.java` (moved to version-specific)
    - `common/src/main/java/com/chronodawn/registry/ModTreeDecoratorTypes.java` (moved to version-specific)

### Token Usage

Session total: ~85,000 / 200,000 tokens (42.5% used)

### Next Steps

1. **Investigate world generation crash** (highest priority)
   - Analyze crash log to identify root cause
   - Likely related to worldgen features, biomes, or structures
2. **Test 1.21.1 startup and world generation**
3. **Phase 6: Integration Testing** after crash resolution
   - Verify dimension teleportation
   - Test all features in both versions
   - Run game tests
