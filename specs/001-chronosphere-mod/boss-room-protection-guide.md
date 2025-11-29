# Boss Room Protection System Guide

**Last Updated**: 2025-11-30
**Implementation**: T224 - Boss Room Boundary Marker Block

## Overview

This guide explains how to protect boss rooms from block breaking using the Boss Room Boundary Marker system. This system allows you to define protected areas in structure NBT files without hardcoding coordinates.

## System Architecture

### Components

1. **BossRoomBoundaryMarkerBlock** - Visible marker block (orange/purple checkered pattern)
2. **BossRoomBoundaryMarkerBlockEntity** - Stores marker type and replacement block in NBT
3. **BossRoomProtectionProcessor** - Detects markers during structure generation, calculates BoundingBox, registers protection
4. **BlockProtectionHandler** - Central handler for protection management and boss defeat detection
5. **Event Handlers** (Fabric/NeoForge) - Prevent block breaking in protected areas

### Processing Flow

```
1. Structure Editing (Structure Block)
   ├─ Place boss_room_min marker at SW floor corner
   └─ Place boss_room_max marker at NE ceiling corner

2. World Generation
   ├─ Structure generation starts
   ├─ BossRoomProtectionProcessor detects markers
   ├─ Calculate BoundingBox from min/max positions
   ├─ Register protection with BlockProtectionHandler
   └─ Replace markers with specified blocks

3. Gameplay
   ├─ Player tries to break block in protected area
   ├─ Event handler checks BlockProtectionHandler.isProtected()
   ├─ Display warning message if protected
   └─ Cancel break event

4. Boss Defeat
   ├─ Boss entity dies (e.g., ChronosWardenEntity.die())
   ├─ Call BlockProtectionHandler.onBossDefeatedAt(level, bossPos)
   ├─ Find protected area containing boss position
   └─ Remove protection
```

## Usage in Structure Block Editor

### 1. Obtain Marker Blocks

```
/give @s chronosphere:boss_room_boundary_marker
```

### 2. Place Markers

Place **two** marker blocks to define the boss room bounding box:

#### Minimum Marker (boss_room_min)
- **Position**: Southwest floor corner
  - Southwest = smallest X, smallest Z
  - Floor = smallest Y
- **Example**: If boss room is (10, 60, 20) to (30, 70, 40) → place at (10, 60, 20)

#### Maximum Marker (boss_room_max)
- **Position**: Northeast ceiling corner
  - Northeast = largest X, largest Z
  - Ceiling = largest Y
- **Example**: If boss room is (10, 60, 20) to (30, 70, 40) → place at (30, 70, 40)

### 3. Configure Marker NBT

After placing markers, edit their BlockEntity NBT:

#### boss_room_min marker NBT:
```json
{
  "MarkerType": "boss_room_min",
  "ReplaceWith": "minecraft:stone_bricks"
}
```

#### boss_room_max marker NBT:
```json
{
  "MarkerType": "boss_room_max",
  "ReplaceWith": "minecraft:air"
}
```

**ReplaceWith Block Selection**:
- Floor marker → Floor material (e.g., `minecraft:stone_bricks`, `minecraft:deepslate_tiles`)
- Ceiling marker → Air (`minecraft:air`) or ceiling material
- The marker block will be replaced with this block during world generation

### 4. Save Structure

Save the structure NBT file with the markers included. During world generation, markers will automatically be processed and replaced.

## Coordinate System

### Understanding Min/Max Positions

```
Y-axis (vertical)
↑
│         MAX (30, 70, 40)
│           ┌─────────────┐  ← Ceiling (Y=70)
│           │             │
│           │  Boss Room  │
│           │             │
│           └─────────────┘  ← Floor (Y=60)
│       MIN (10, 60, 20)
│
└────────────────────────────→ X/Z-axis (horizontal)
      SW                 NE
```

- **MIN marker**: Southwest + Floor (smallest X, Y, Z)
- **MAX marker**: Northeast + Ceiling (largest X, Y, Z)

## Integration with Structure Processor

Add the boss room protection processor to your structure's processor list:

**File**: `data/chronosphere/worldgen/processor_list/your_structure_combined.json`

```json
{
  "processors": [
    {
      "processor_type": "minecraft:rule",
      "rules": [
        // ... other rules ...
      ]
    },
    {
      "processor_type": "chronosphere:boss_room_protection"
    }
  ]
}
```

## Boss Defeat Detection

To unprotect the boss room when the boss is defeated, call `BlockProtectionHandler.onBossDefeatedAt()` in the boss entity's death method:

**Example** (ChronosWardenEntity.java):

```java
@Override
public void die(DamageSource source) {
    super.die(source);
    if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
        BlockProtectionHandler.onBossDefeatedAt(serverLevel, this.blockPosition());
    }
}
```

This method:
1. Finds the protected area containing the boss position
2. Marks the area as defeated
3. Allows block breaking in that area

## Player Messages

### Protection Warning
When a player tries to break a block in a protected area:
- **English**: "This area is protected until you defeat the boss"
- **Japanese**: "このエリアはボスを倒すまで保護されています"

### Configuration

Messages are defined in language files:

**File**: `assets/chronosphere/lang/en_us.json`
```json
{
  "message.chronosphere.boss_room_protected": "This area is protected until you defeat the boss"
}
```

**File**: `assets/chronosphere/lang/ja_jp.json`
```json
{
  "message.chronosphere.boss_room_protected": "このエリアはボスを倒すまで保護されています"
}
```

## Technical Details

### BoundingBox Calculation

```java
// From two marker positions
BoundingBox box = new BoundingBox(
    Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ),
    Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ)
);
```

### Protection Registration

```java
String dimensionKey = serverLevel.dimension().location().toString();
String areaKey = dimensionKey + ":" + minMarkerPos + "_" + maxMarkerPos;
BlockProtectionHandler.registerProtection(serverLevel, areaKey, box);
```

**Key Format**: `dimension:minPos_maxPos`
- Example: `chronosphere:chronosphere:BlockPos{x=100, y=60, z=200}_BlockPos{x=120, y=70, z=220}`

### Position-Based Boss Defeat Detection

```java
public static boolean onBossDefeatedAt(ServerLevel level, BlockPos bossPos) {
    String dimensionKey = level.dimension().location().toString();

    for (Map.Entry<String, BoundingBox> entry : PROTECTED_AREAS.entrySet()) {
        String key = entry.getKey();
        BoundingBox area = entry.getValue();

        if (key.startsWith(dimensionKey + ":") && area.isInside(bossPos)) {
            DEFEATED_BOSSES.add(key);
            Chronosphere.LOGGER.info("Boss defeated at {}! Unprotected boss room: {}",
                                     bossPos, key);
            return true;
        }
    }
    return false;
}
```

## Example: Guardian Vault Boss Room

### Structure Setup

1. **Guardian Vault NBT file** (`guardian_vault.nbt`)
   - Boss room area: approximately (relative coordinates in structure)
   - Place `boss_room_min` marker at SW floor corner
   - Place `boss_room_max` marker at NE ceiling corner

2. **Marker Configuration**
   ```json
   // boss_room_min
   {
     "MarkerType": "boss_room_min",
     "ReplaceWith": "minecraft:deepslate_tiles"
   }

   // boss_room_max
   {
     "MarkerType": "boss_room_max",
     "ReplaceWith": "minecraft:air"
   }
   ```

3. **Processor List** (`guardian_vault_combined.json`)
   ```json
   {
     "processors": [
       {
         "processor_type": "minecraft:rule",
         "rules": [...]
       },
       {
         "processor_type": "chronosphere:boss_room_protection"
       }
     ]
   }
   ```

4. **Boss Entity** (ChronosWardenEntity.java)
   ```java
   @Override
   public void die(DamageSource source) {
       super.die(source);
       if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
           BlockProtectionHandler.onBossDefeatedAt(serverLevel, this.blockPosition());
       }
   }
   ```

## Applying to Other Boss Rooms

This system can be applied to any boss room structure:

1. **Master Clock Tower** (T218-T223) - Chronos Warden boss
2. **Clockwork Depths** (Future) - Underground mining dungeon
3. **Phantom Catacombs** (Future) - Maze dungeon
4. **Entropy Crypt** (Future) - Final boss area

### Steps for Each Structure

1. Load structure NBT in Structure Block editor
2. Place boss_room_min and boss_room_max markers
3. Configure marker NBT (MarkerType, ReplaceWith)
4. Add boss_room_protection processor to structure's processor list
5. Implement boss defeat detection in boss entity's die() method
6. Save and test

## Advantages Over Hardcoded Coordinates

- ✅ **No code changes needed** when editing structure layout
- ✅ **Visual feedback** during structure editing (visible marker blocks)
- ✅ **Flexible placement** - markers define exact boundaries
- ✅ **Easy to understand** - similar to Jigsaw block workflow
- ✅ **Reusable pattern** - same system for all boss rooms
- ✅ **Automatic cleanup** - markers replaced during generation

## Files Reference

### Core Implementation
- `common/src/main/java/com/chronosphere/blocks/BossRoomBoundaryMarkerBlock.java`
- `common/src/main/java/com/chronosphere/blocks/BossRoomBoundaryMarkerBlockEntity.java`
- `common/src/main/java/com/chronosphere/worldgen/processors/BossRoomProtectionProcessor.java`
- `common/src/main/java/com/chronosphere/worldgen/protection/BlockProtectionHandler.java`

### Platform-Specific
- `fabric/src/main/java/com/chronosphere/fabric/event/BlockProtectionEventHandler.java`
- `neoforge/src/main/java/com/chronosphere/neoforge/event/BlockProtectionEventHandler.java`

### Resources
- `common/src/main/resources/assets/chronosphere/textures/block/boss_room_boundary_marker.png`
- `common/src/main/resources/assets/chronosphere/lang/en_us.json`
- `common/src/main/resources/assets/chronosphere/lang/ja_jp.json`

### Registry
- `common/src/main/java/com/chronosphere/registry/ModBlocks.java`
- `common/src/main/java/com/chronosphere/registry/ModBlockEntities.java`
- `common/src/main/java/com/chronosphere/registry/ModItems.java`
- `common/src/main/java/com/chronosphere/registry/ModStructureProcessorTypes.java`

## Troubleshooting

### Marker block not available via `/give` command
- **Solution**: Ensure BlockItem is registered in ModItems.java
- Check: `ModItems.BOSS_ROOM_BOUNDARY_MARKER` registration

### Protection not registered during world generation
- **Solution**: Verify processor is added to structure's processor list
- Check: `data/chronosphere/worldgen/processor_list/your_structure_combined.json`

### Boss room still protected after boss defeat
- **Solution**: Ensure boss entity calls `BlockProtectionHandler.onBossDefeatedAt()` in `die()` method
- Check: Boss dies within the protected BoundingBox

### Marker blocks hard to place in Structure Block editor
- **Solution**: Marker blocks have full collision detection (added in T224)
- Visual: Orange/purple checkered pattern for easy identification
