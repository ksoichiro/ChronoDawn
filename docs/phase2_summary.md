# Phase 2: Abstraction Layer Design - Summary

**Date**: 2026-01-02
**Author**: Claude
**Tasks**: T406-T408

---

## Completed Tasks

### T406: Design compat package structure ✅

**Document**: `docs/compat_layer_design.md`

**Key Decisions**:
1. **Package Structure**: `compat/` with version-specific subdirectories (`v1_20_1/`, `v1_21_1/`)
2. **Three Main Interfaces**:
   - `ItemDataHandler`: Abstract ItemStack custom data (NBT vs DataComponents)
   - `CompatSavedData`: Base class for SavedData with version-independent methods
   - `CompatBlockEntity`: Base class for BlockEntity with version-independent methods
3. **Factory Pattern**: `CompatHandlers` singleton for version detection
4. **Gradle Integration**: Version-specific directories added via `sourceSets`

### T407: Survey existing code API usage patterns ✅

**Document**: `docs/api_usage_survey.md`

**Key Findings**:
1. **ItemStack Data**: Only 1 file uses DataComponents (`TimeCompassItem.java`)
   - Original estimate: 47 files
   - Actual: 1 file (other items don't store custom data)
2. **SavedData**: 7 files require migration
3. **BlockEntity**: 3 files require migration
4. **Entity**: 9+ files, but **NO migration required** (signature unchanged)
5. **Total files requiring migration**: **11 files** (not 47)

**Impact**: Migration scope is much smaller than originally estimated, reducing Phase 4 effort significantly.

### T408: Finalize abstraction layer specifications ✅

**Specifications Finalized**:

#### 1. ItemDataHandler Interface

**Methods**:
```java
void setString(ItemStack stack, String key, String value);
String getString(ItemStack stack, String key);
void setInt(ItemStack stack, String key, int value);
int getInt(ItemStack stack, String key);
boolean contains(ItemStack stack, String key);
CompoundTag getCustomData(ItemStack stack);
void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater);
```

**Error Handling Policy**:
- Return default values (empty string, 0, false) instead of throwing exceptions
- Never return null from interface methods
- Rationale: Matches Minecraft's lenient NBT API, prevents crashes from missing data

**Null Safety Guarantee**:
- All methods handle null ItemStack gracefully (return defaults)
- All methods handle missing data gracefully (return defaults)
- CompoundTag methods return empty tag instead of null

#### 2. CompatSavedData Base Class

**Methods**:
```java
abstract CompoundTag saveData(CompoundTag tag);  // Version-independent save
abstract void loadData(CompoundTag tag);         // Version-independent load
CompoundTag save(CompoundTag, HolderLookup.Provider); // 1.21.1 implementation (calls saveData)
```

**Error Handling Policy**:
- Subclasses implement only `saveData()` and `loadData()`
- Base class handles version-specific `save()` signature
- No exceptions thrown - missing data uses default values

**Null Safety Guarantee**:
- `saveData()` always receives non-null CompoundTag
- `loadData()` always receives non-null CompoundTag
- Subclasses don't need null checks for tags

#### 3. CompatBlockEntity Base Class

**Methods**:
```java
abstract void saveData(CompoundTag tag);  // Version-independent save
abstract void loadData(CompoundTag tag);  // Version-independent load
void saveAdditional(CompoundTag, HolderLookup.Provider); // 1.21.1 implementation (calls saveData)
void loadAdditional(CompoundTag, HolderLookup.Provider); // 1.21.1 implementation (calls loadData)
```

**Error Handling Policy**:
- Same as CompatSavedData
- Subclasses implement only version-independent methods

**Null Safety Guarantee**:
- Same as CompatSavedData

#### 4. Version Detection Strategy

**Gradle Property**:
```groovy
tasks.withType(JavaCompile) {
    systemProperty 'chronodawn.minecraft.version', project.ext.minecraft_version
}
```

**Factory Logic**:
```java
public class CompatHandlers {
    public static final ItemDataHandler ITEM_DATA = createItemDataHandler();

    private static ItemDataHandler createItemDataHandler() {
        String mcVersion = System.getProperty("chronodawn.minecraft.version", "1.21.1");
        if (mcVersion.startsWith("1.20.1")) {
            return new ItemDataHandler120();
        } else {
            return new ItemDataHandler121();
        }
    }
}
```

**Benefits**:
- Compile-time version detection (no runtime overhead)
- Single source of truth (Gradle property)
- Easy to extend for future versions

---

## Design Validation

### Coverage Analysis

| API Category | Files Affected | Coverage Strategy |
|-------------|---------------|------------------|
| ItemStack Data | 1 file | `ItemDataHandler` interface |
| SavedData | 7 files | `CompatSavedData` base class |
| BlockEntity | 3 files | `CompatBlockEntity` base class |
| Entity | 0 files | No compat layer needed |

**Total**: 11 files covered by 3 compat abstractions ✅

### Migration Path Validation

**Example 1: TimeCompassItem (ItemStack)**
```java
// Before (1.21.1 only)
CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
    tag.putString(NBT_TARGET_STRUCTURE, structureType);
});

// After (1.20.1 + 1.21.1)
CompatHandlers.ITEM_DATA.setString(stack, NBT_TARGET_STRUCTURE, structureType);
```

**Example 2: ChronoDawnGlobalState (SavedData)**
```java
// Before (1.21.1 only)
public class ChronoDawnGlobalState extends SavedData {
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("HasEnteredChronoDawn", hasEnteredChronoDawn);
        return tag;
    }
}

// After (1.20.1 + 1.21.1)
public class ChronoDawnGlobalState extends CompatSavedData {
    @Override
    public CompoundTag saveData(CompoundTag tag) {
        tag.putBoolean("HasEnteredChronoDawn", hasEnteredChronoDawn);
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        hasEnteredChronoDawn = tag.getBoolean("HasEnteredChronoDawn");
    }
}
```

**Example 3: BossRoomDoorBlockEntity (BlockEntity)**
```java
// Before (1.21.1 only)
public class BossRoomDoorBlockEntity extends BlockEntity {
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("DoorType", doorType);
    }
}

// After (1.20.1 + 1.21.1)
public class BossRoomDoorBlockEntity extends CompatBlockEntity {
    @Override
    public void saveData(CompoundTag tag) {
        tag.putString("DoorType", doorType);
    }

    @Override
    public void loadData(CompoundTag tag) {
        if (tag.contains("DoorType")) {
            doorType = tag.getString("DoorType");
        }
    }
}
```

All migration paths validated ✅

---

## Risks and Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| **Version detection fails** | Low | High | Fallback to default version (1.21.1), add compile-time validation |
| **Performance overhead** | Low | Low | JVM inlining for simple wrapper methods |
| **Interface design flaw** | Medium | High | **Phase 3 prototype testing before full migration** |
| **1.20.1 API incompatibility** | Medium | High | Test in 1.20.1 early in Phase 3 |

**Critical Mitigation**: Implement and test compat layer in Phase 3 **before** migrating all files in Phase 4.

---

## Updated Phase 4 Estimate

**Original Estimate**: 10-14 days (47 files)
**Revised Estimate**: **3-5 days** (11 files)

**Breakdown**:
- ItemStack migration: 1 file × 0.5 days = 0.5 days
- SavedData migration: 7 files × 0.3 days = 2.1 days
- BlockEntity migration: 3 files × 0.3 days = 0.9 days
- Testing and fixes: 1 day

**Total**: ~4.5 days (rounded to 3-5 days)

---

## Next Steps

### Phase 3: Java Code Abstraction Implementation (T3-1 to T3-4)

**Priority**: High
**Estimated Duration**: 5-7 days (unchanged)

#### Tasks

1. **T3-1**: Implement `ItemDataHandler` interface
   - `ItemDataHandler.java` (interface)
   - `v1_20_1/ItemDataHandler120.java` (NBT-based)
   - `v1_21_1/ItemDataHandler121.java` (DataComponents-based)
   - `CompatHandlers.java` (factory)

2. **T3-2**: Implement `CompatSavedData` base class
   - `CompatSavedData.java`
   - Version-specific implementations (handled by base class)

3. **T3-3**: Implement `CompatBlockEntity` base class
   - `CompatBlockEntity.java`
   - Version-specific implementations (handled by base class)

4. **T3-4**: Extend Gradle build scripts
   - Add `compat_package` property to common/build.gradle
   - Add version-specific directory to `sourceSets.main.java`
   - Set system property at compile time

5. **T3-5**: Verification (NEW - added based on survey results)
   - Test build for 1.20.1 (verify `ItemDataHandler120` compiled)
   - Test build for 1.21.1 (verify `ItemDataHandler121` compiled)
   - Create unit tests for compat handlers (if possible)

**Critical Success Factor**: Verify compat layer works in **both versions** before proceeding to Phase 4.

---

## Specification Status

✅ **Phase 2 Complete**

All specifications finalized:
- [x] T406: Package structure designed
- [x] T407: Existing code surveyed (11 files identified)
- [x] T408: Interface specifications finalized

**Ready for Phase 3**: Implementation of compat package

---

**Document Status**: Phase 2 Complete
**Next Update**: After Phase 3 completion (implementation review)
