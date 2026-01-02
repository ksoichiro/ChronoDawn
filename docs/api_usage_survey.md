# Chrono Dawn - API Usage Survey

**Date**: 2026-01-02
**Author**: Claude
**Target Versions**: Minecraft 1.20.1 + 1.21.1
**Phase**: Phase 2 (T407 - Survey existing code API usage patterns)

---

## Overview

This document lists all files that use version-specific APIs and require migration to the compatibility layer.

**Total Java Files**: 256

## 1. ItemStack Data API Usage

### Files Using DataComponents.CUSTOM_DATA (1.21.1 API)

Total: **1 file**

| File | API Used | Migration Required |
|------|----------|-------------------|
| `common/src/main/java/com/chronodawn/items/TimeCompassItem.java` | `DataComponents.CUSTOM_DATA`, `CustomData.update()`, `CustomData.copyTag()` | ✅ Yes |

**API Pattern**:
```java
// Write
CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
    tag.putString(NBT_TARGET_STRUCTURE, structureType);
});

// Read
CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
if (customData != null) {
    return customData.copyTag().getString(NBT_TARGET_STRUCTURE);
}
```

**1.20.1 Equivalent**:
```java
// Write
stack.getOrCreateTag().putString(NBT_TARGET_STRUCTURE, structureType);

// Read
CompoundTag tag = stack.getTag();
return tag != null ? tag.getString(NBT_TARGET_STRUCTURE) : "";
```

**Migration Path**: Use `CompatHandlers.ITEM_DATA` interface

---

## 2. SavedData API Usage

### Files Extending SavedData (1.21.1 API)

Total: **4 files**

| File | Class | save() Signature | Migration Required |
|------|-------|------------------|-------------------|
| `common/src/main/java/com/chronodawn/data/ChronoDawnGlobalState.java` | `ChronoDawnGlobalState extends SavedData` | `save(CompoundTag, HolderLookup.Provider)` | ✅ Yes |
| `common/src/main/java/com/chronodawn/data/ChronoDawnTimeData.java` | `ChronoDawnTimeData extends SavedData` | `save(CompoundTag, HolderLookup.Provider)` | ✅ Yes |
| `common/src/main/java/com/chronodawn/data/ChronoDawnWorldData.java` | `ChronoDawnWorldData extends SavedData` (abstract base) | `save(CompoundTag, HolderLookup.Provider)` | ✅ Yes |
| `common/src/main/java/com/chronodawn/data/TimeKeeperVillageData.java` | `TimeKeeperVillageData extends SavedData` | `save(CompoundTag, HolderLookup.Provider)` | ✅ Yes |

**Additional SavedData Files** (found via HolderLookup.Provider search):
| File | Migration Required |
|------|-------------------|
| `common/src/main/java/com/chronodawn/data/DimensionStateData.java` | ✅ Yes |
| `common/src/main/java/com/chronodawn/data/PlayerProgressData.java` | ✅ Yes |
| `common/src/main/java/com/chronodawn/data/PortalRegistryData.java` | ✅ Yes |

**Total SavedData files**: **7 files**

**API Pattern**:
```java
// 1.21.1
@Override
public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
    tag.putBoolean("HasEnteredChronoDawn", hasEnteredChronoDawn);
    return tag;
}

public static ChronoDawnGlobalState load(CompoundTag tag, HolderLookup.Provider provider) {
    ChronoDawnGlobalState state = new ChronoDawnGlobalState();
    state.hasEnteredChronoDawn = tag.getBoolean("HasEnteredChronoDawn");
    return state;
}
```

**1.20.1 Equivalent**:
```java
// 1.20.1
@Override
public CompoundTag save(CompoundTag tag) {
    tag.putBoolean("HasEnteredChronoDawn", hasEnteredChronoDawn);
    return tag;
}

public static ChronoDawnGlobalState load(CompoundTag tag) {
    ChronoDawnGlobalState state = new ChronoDawnGlobalState();
    state.hasEnteredChronoDawn = tag.getBoolean("HasEnteredChronoDawn");
    return state;
}
```

**Migration Path**: Extend `CompatSavedData` and implement `saveData(CompoundTag)` + `loadData(CompoundTag)`

---

## 3. BlockEntity API Usage

### Files Extending BlockEntity (1.21.1 API)

Total: **3 files**

| File | Class | saveAdditional() Signature | Migration Required |
|------|-------|---------------------------|-------------------|
| `common/src/main/java/com/chronodawn/blocks/BossRoomBoundaryMarkerBlockEntity.java` | `BossRoomBoundaryMarkerBlockEntity extends BlockEntity` | `saveAdditional(CompoundTag, HolderLookup.Provider)` | ✅ Yes |
| `common/src/main/java/com/chronodawn/blocks/BossRoomDoorBlockEntity.java` | `BossRoomDoorBlockEntity extends BlockEntity` | `saveAdditional(CompoundTag, HolderLookup.Provider)` | ✅ Yes |
| `common/src/main/java/com/chronodawn/blocks/ClockTowerTeleporterBlockEntity.java` | `ClockTowerTeleporterBlockEntity extends BlockEntity` | `saveAdditional(CompoundTag, HolderLookup.Provider)` | ✅ Yes |

**API Pattern**:
```java
// 1.21.1
@Override
protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putString("DoorType", doorType);
}

@Override
protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    if (tag.contains("DoorType")) {
        doorType = tag.getString("DoorType");
    }
}
```

**1.20.1 Equivalent**:
```java
// 1.20.1
@Override
protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putString("DoorType", doorType);
}

@Override
public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("DoorType")) {
        doorType = tag.getString("DoorType");
    }
}
```

**Migration Path**: Extend `CompatBlockEntity` and implement `saveData(CompoundTag)` + `loadData(CompoundTag)`

---

## 4. Entity API Usage (NO MIGRATION REQUIRED)

### Files Extending Entity (No Version Difference)

Total: **9 files** (no migration required)

Entity classes use `addAdditionalSaveData(CompoundTag)` and `readAdditionalSaveData(CompoundTag)` in **both 1.20.1 and 1.21.1** - no HolderLookup.Provider parameter.

| File | Class | Migration Required |
|------|-------|-------------------|
| `common/src/main/java/com/chronodawn/entities/boats/ChronoDawnBoat.java` | `ChronoDawnBoat extends Boat` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/boats/ChronoDawnChestBoat.java` | `ChronoDawnChestBoat extends ChestBoat` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/bosses/ChronosWardenEntity.java` | `ChronosWardenEntity extends Monster` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/bosses/ClockworkColossusEntity.java` | `ClockworkColossusEntity extends Monster` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/bosses/EntropyKeeperEntity.java` | `EntropyKeeperEntity extends Monster` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/bosses/TemporalPhantomEntity.java` | `TemporalPhantomEntity extends Monster` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java` | `TimeGuardianEntity extends Monster` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/bosses/TimeTyrantEntity.java` | `TimeTyrantEntity extends Monster` | ❌ No |
| `common/src/main/java/com/chronodawn/entities/mobs/TimeKeeperEntity.java` | `TimeKeeperEntity extends AbstractVillager` | ❌ No |

**API Pattern** (unchanged between versions):
```java
@Override
public void addAdditionalSaveData(CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putInt("Phase", this.entityData.get(PHASE));
}

@Override
public void readAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    this.entityData.set(PHASE, tag.getInt("Phase"));
}
```

---

## Summary

| Category | Files Requiring Migration | Total Files |
|----------|---------------------------|-------------|
| **ItemStack (DataComponents)** | 1 | 39 items (only 1 uses data components) |
| **SavedData** | 7 | 7 |
| **BlockEntity** | 3 | 3 |
| **Entity** | 0 | 9+ (no migration required) |
| **Total** | **11 files** | **256 total Java files** |

## Migration Priority

### High Priority (Core Systems)
1. **SavedData classes** (7 files) - Affects world persistence
   - ChronoDawnGlobalState.java
   - ChronoDawnTimeData.java
   - ChronoDawnWorldData.java (base class)
   - TimeKeeperVillageData.java
   - DimensionStateData.java
   - PlayerProgressData.java
   - PortalRegistryData.java

### Medium Priority (Gameplay Features)
2. **BlockEntity classes** (3 files) - Affects boss rooms and teleportation
   - BossRoomBoundaryMarkerBlockEntity.java
   - BossRoomDoorBlockEntity.java
   - ClockTowerTeleporterBlockEntity.java

3. **ItemStack data** (1 file) - Affects Time Compass
   - TimeCompassItem.java

## Next Steps (Phase 3)

1. Create `CompatSavedData` base class
2. Create `CompatBlockEntity` base class
3. Implement `ItemDataHandler` interface + version-specific implementations
4. Migrate all 11 files to use compat layer
5. Test in both 1.20.1 and 1.21.1

---

**Document Status**: Phase 2 Complete (T407)
**Next Phase**: Phase 3 (T3-1 to T3-4) - Implement compat package
