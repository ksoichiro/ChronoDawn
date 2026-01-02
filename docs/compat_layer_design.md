# Chrono Dawn - Compatibility Layer Design

**Date**: 2026-01-02
**Author**: Claude
**Target Versions**: Minecraft 1.20.1 + 1.21.1

---

## Overview

This document describes the design of the compatibility layer (`compat/` package) that abstracts API differences between Minecraft 1.20.1 and 1.21.1.

## API Differences Summary

| Category | 1.20.1 (NBT-based) | 1.21.1 (Component-based) |
|----------|-------------------|-------------------------|
| **ItemStack Data** | `stack.getOrCreateTag()` | `stack.get(DataComponents.CUSTOM_DATA)` |
| **SavedData** | `save(CompoundTag)` | `save(CompoundTag, HolderLookup.Provider)` |
| **BlockEntity** | `saveAdditional(CompoundTag)` | `saveAdditional(CompoundTag, HolderLookup.Provider)` |

## Package Structure

```
common/src/main/java/com/chronodawn/compat/
├── ItemDataHandler.java              # Interface for ItemStack data operations
├── SavedDataHandler.java             # Interface for SavedData serialization
├── BlockEntityDataHandler.java       # Interface for BlockEntity serialization
├── CompatHandlers.java                # Factory for version-specific handlers
├── v1_20_1/                           # 1.20.1 implementations
│   ├── ItemDataHandler120.java
│   ├── SavedDataHandler120.java
│   └── BlockEntityDataHandler120.java
└── v1_21_1/                           # 1.21.1 implementations
    ├── ItemDataHandler121.java
    ├── SavedDataHandler121.java
    └── BlockEntityDataHandler121.java
```

## 1. ItemDataHandler Interface

### Purpose
Abstract ItemStack custom data operations (NBT tags in 1.20.1, Data Components in 1.21.1).

### Interface Definition

```java
package com.chronodawn.compat;

import net.minecraft.world.item.ItemStack;

/**
 * Abstracts ItemStack custom data operations across Minecraft versions.
 *
 * Version Differences:
 * - 1.20.1: Uses NBT tags (stack.getOrCreateTag())
 * - 1.21.1: Uses Data Components (DataComponents.CUSTOM_DATA)
 */
public interface ItemDataHandler {
    /**
     * Set a string value in the ItemStack's custom data.
     *
     * @param stack ItemStack to modify
     * @param key Data key
     * @param value String value
     */
    void setString(ItemStack stack, String key, String value);

    /**
     * Get a string value from the ItemStack's custom data.
     *
     * @param stack ItemStack to read from
     * @param key Data key
     * @return String value, or empty string if not found
     */
    String getString(ItemStack stack, String key);

    /**
     * Set an integer value in the ItemStack's custom data.
     *
     * @param stack ItemStack to modify
     * @param key Data key
     * @param value Integer value
     */
    void setInt(ItemStack stack, String key, int value);

    /**
     * Get an integer value from the ItemStack's custom data.
     *
     * @param stack ItemStack to read from
     * @param key Data key
     * @return Integer value, or 0 if not found
     */
    int getInt(ItemStack stack, String key);

    /**
     * Check if the ItemStack has a specific key in its custom data.
     *
     * @param stack ItemStack to check
     * @param key Data key
     * @return true if key exists
     */
    boolean contains(ItemStack stack, String key);

    /**
     * Get the raw NBT tag from the ItemStack's custom data.
     * This is provided for complex operations that need direct NBT access.
     *
     * @param stack ItemStack to read from
     * @return CompoundTag, or empty tag if no data
     */
    net.minecraft.nbt.CompoundTag getCustomData(ItemStack stack);

    /**
     * Update the ItemStack's custom data using a Consumer.
     * This allows batch updates without multiple copies.
     *
     * @param stack ItemStack to modify
     * @param updater Consumer that modifies the CompoundTag
     */
    void updateCustomData(ItemStack stack, java.util.function.Consumer<net.minecraft.nbt.CompoundTag> updater);
}
```

### 1.20.1 Implementation

```java
package com.chronodawn.compat.v1_20_1;

import com.chronodawn.compat.ItemDataHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemDataHandler120 implements ItemDataHandler {
    @Override
    public void setString(ItemStack stack, String key, String value) {
        stack.getOrCreateTag().putString(key, value);
    }

    @Override
    public String getString(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getString(key) : "";
    }

    @Override
    public void setInt(ItemStack stack, String key, int value) {
        stack.getOrCreateTag().putInt(key, value);
    }

    @Override
    public int getInt(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(key) : 0;
    }

    @Override
    public boolean contains(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key);
    }

    @Override
    public CompoundTag getCustomData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag : new CompoundTag();
    }

    @Override
    public void updateCustomData(ItemStack stack, java.util.function.Consumer<CompoundTag> updater) {
        CompoundTag tag = stack.getOrCreateTag();
        updater.accept(tag);
    }
}
```

### 1.21.1 Implementation

```java
package com.chronodawn.compat.v1_21_1;

import com.chronodawn.compat.ItemDataHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ItemDataHandler121 implements ItemDataHandler {
    @Override
    public void setString(ItemStack stack, String key, String value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(key, value);
        });
    }

    @Override
    public String getString(ItemStack stack, String key) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getString(key);
        }
        return "";
    }

    @Override
    public void setInt(ItemStack stack, String key, int value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(key, value);
        });
    }

    @Override
    public int getInt(ItemStack stack, String key) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getInt(key);
        }
        return 0;
    }

    @Override
    public boolean contains(ItemStack stack, String key) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().contains(key);
        }
        return false;
    }

    @Override
    public CompoundTag getCustomData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag();
        }
        return new CompoundTag();
    }

    @Override
    public void updateCustomData(ItemStack stack, java.util.function.Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }
}
```

## 2. SavedDataHandler Interface

### Purpose
Abstract SavedData load/save signature differences (HolderLookup.Provider parameter in 1.21.1).

### Interface Definition

```java
package com.chronodawn.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Abstracts SavedData serialization across Minecraft versions.
 *
 * Version Differences:
 * - 1.20.1: save(CompoundTag)
 * - 1.21.1: save(CompoundTag, HolderLookup.Provider)
 *
 * Usage:
 * Subclasses should call SavedDataHandler methods instead of overriding save() directly.
 */
public interface SavedDataHandler {
    /**
     * Save data to NBT tag.
     * This method is version-independent.
     *
     * @param tag CompoundTag to write to
     * @return The same CompoundTag (for chaining)
     */
    CompoundTag saveData(CompoundTag tag);

    /**
     * Load data from NBT tag.
     * This method is version-independent.
     *
     * @param tag CompoundTag to read from
     */
    void loadData(CompoundTag tag);
}
```

### Base SavedData Class

```java
package com.chronodawn.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Base class for SavedData that abstracts version differences.
 *
 * Subclasses should implement SavedDataHandler instead of overriding save() directly.
 */
public abstract class CompatSavedData extends SavedData implements SavedDataHandler {
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        // Call version-independent saveData()
        return saveData(tag);
    }

    /**
     * Version-independent save method.
     * Subclasses must implement this.
     */
    @Override
    public abstract CompoundTag saveData(CompoundTag tag);

    /**
     * Version-independent load method.
     * Subclasses must implement this.
     */
    @Override
    public abstract void loadData(CompoundTag tag);
}
```

**Note**: In 1.20.1, SavedData classes will need a different approach since the `save()` signature is incompatible. See "Version-Specific Implementation" section below.

## 3. BlockEntityDataHandler Interface

### Purpose
Abstract BlockEntity saveAdditional/loadAdditional signature differences (HolderLookup.Provider parameter in 1.21.1).

### Interface Definition

```java
package com.chronodawn.compat;

import net.minecraft.nbt.CompoundTag;

/**
 * Abstracts BlockEntity data serialization across Minecraft versions.
 *
 * Version Differences:
 * - 1.20.1: saveAdditional(CompoundTag)
 * - 1.21.1: saveAdditional(CompoundTag, HolderLookup.Provider)
 */
public interface BlockEntityDataHandler {
    /**
     * Save BlockEntity data to NBT tag.
     * This method is version-independent.
     *
     * @param tag CompoundTag to write to
     */
    void saveData(CompoundTag tag);

    /**
     * Load BlockEntity data from NBT tag.
     * This method is version-independent.
     *
     * @param tag CompoundTag to read from
     */
    void loadData(CompoundTag tag);
}
```

### Base BlockEntity Class

```java
package com.chronodawn.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for BlockEntity that abstracts version differences.
 *
 * Subclasses should implement BlockEntityDataHandler instead of overriding saveAdditional() directly.
 */
public abstract class CompatBlockEntity extends BlockEntity implements BlockEntityDataHandler {
    public CompatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Call version-independent saveData()
        saveData(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Call version-independent loadData()
        loadData(tag);
    }

    /**
     * Version-independent save method.
     * Subclasses must implement this.
     */
    @Override
    public abstract void saveData(CompoundTag tag);

    /**
     * Version-independent load method.
     * Subclasses must implement this.
     */
    @Override
    public abstract void loadData(CompoundTag tag);
}
```

## 4. CompatHandlers Factory

### Purpose
Provide singleton access to version-specific handler implementations.

### Implementation

```java
package com.chronodawn.compat;

/**
 * Factory for version-specific compatibility handlers.
 *
 * This class detects the Minecraft version at compile time and provides
 * the appropriate handler implementations.
 */
public class CompatHandlers {
    public static final ItemDataHandler ITEM_DATA = createItemDataHandler();

    private static ItemDataHandler createItemDataHandler() {
        // Version detection via Gradle-defined system property
        String mcVersion = System.getProperty("chronodawn.minecraft.version", "1.21.1");

        if (mcVersion.startsWith("1.20.1")) {
            return new com.chronodawn.compat.v1_20_1.ItemDataHandler120();
        } else {
            return new com.chronodawn.compat.v1_21_1.ItemDataHandler121();
        }
    }

    // Private constructor - static factory only
    private CompatHandlers() {}
}
```

## Version-Specific Implementation Strategy

### Gradle Configuration

**common/build.gradle**:
```groovy
sourceSets {
    main {
        java {
            srcDir 'src/main/java'
            // Add version-specific implementation directory
            def compatPackage = project.ext.compat_package // "v1_20_1" or "v1_21_1"
            srcDir "src/main/java/com/chronodawn/compat/${compatPackage}"
        }
    }
}

// Set version info as Java system property at compile time
tasks.withType(JavaCompile) {
    systemProperty 'chronodawn.minecraft.version', project.ext.minecraft_version
}
```

### Directory Structure for Version-Specific Code

Only the appropriate version's implementation directory is included in the build:

**When building for 1.20.1**:
```
common/src/main/java/
├── com/chronodawn/compat/
│   ├── ItemDataHandler.java (interface)
│   └── v1_20_1/
│       └── ItemDataHandler120.java (INCLUDED)
```

**When building for 1.21.1**:
```
common/src/main/java/
├── com/chronodawn/compat/
│   ├── ItemDataHandler.java (interface)
│   └── v1_21_1/
│       └── ItemDataHandler121.java (INCLUDED)
```

## Migration Guide

### Example: Migrating TimeCompassItem

**Before (1.21.1 only)**:
```java
public static ItemStack createCompass(String structureType) {
    ItemStack stack = new ItemStack(ModItems.TIME_COMPASS.get());

    // 1.21.1 only
    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
        tag.putString(NBT_TARGET_STRUCTURE, structureType);
    });

    return stack;
}

public static String getTargetStructure(ItemStack stack) {
    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    if (customData != null) {
        return customData.copyTag().getString(NBT_TARGET_STRUCTURE);
    }
    return null;
}
```

**After (1.20.1 + 1.21.1 compatible)**:
```java
public static ItemStack createCompass(String structureType) {
    ItemStack stack = new ItemStack(ModItems.TIME_COMPASS.get());

    // Version-independent
    CompatHandlers.ITEM_DATA.setString(stack, NBT_TARGET_STRUCTURE, structureType);

    return stack;
}

public static String getTargetStructure(ItemStack stack) {
    // Version-independent
    String target = CompatHandlers.ITEM_DATA.getString(stack, NBT_TARGET_STRUCTURE);
    return target.isEmpty() ? null : target;
}
```

### Example: Migrating ChronoDawnGlobalState

**Before (1.21.1 only)**:
```java
public class ChronoDawnGlobalState extends SavedData {
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
}
```

**After (1.20.1 + 1.21.1 compatible)**:
```java
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

    public static ChronoDawnGlobalState load(CompoundTag tag, HolderLookup.Provider provider) {
        ChronoDawnGlobalState state = new ChronoDawnGlobalState();
        state.loadData(tag); // Call version-independent method
        return state;
    }
}
```

### Example: Migrating BossRoomDoorBlockEntity

**Before (1.21.1 only)**:
```java
public class BossRoomDoorBlockEntity extends BlockEntity {
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
}
```

**After (1.20.1 + 1.21.1 compatible)**:
```java
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

## Design Considerations

### 1. Error Handling
- **Policy**: Return default values (empty string, 0, false) instead of throwing exceptions
- **Rationale**: Minecraft's NBT API is lenient, and missing data should not crash the game

### 2. Null Safety
- **Policy**: Never return null from interface methods
- **Rationale**: Simplifies client code and prevents NullPointerExceptions

### 3. Performance
- **Concern**: Multiple layers of abstraction may introduce overhead
- **Mitigation**: Handler methods are simple wrappers with minimal logic, JVM will likely inline them

### 4. Future Versions
- **Extensibility**: If 1.22+ introduces new data APIs, add new `v1_22_x/` package without modifying existing interfaces
- **Maintenance**: Keep interface signatures stable to minimize refactoring

## Next Steps

1. **Phase 3 (T3-1 to T3-4)**: Implement compat package (interfaces + version-specific classes)
2. **Phase 4 (T4-1 to T4-5)**: Migrate all existing code to use compat handlers
3. **Phase 6 (T6-1 to T6-4)**: Integration testing in both versions

---

**Document Status**: Initial Design (Phase 2: T406)
**Next Update**: After Phase 3 implementation (code review + lessons learned)
