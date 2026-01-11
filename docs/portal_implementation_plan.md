# ChronoDawn Portal - Independent Implementation Plan

## Overview

This document describes the plan to replace Custom Portal API dependency with an independent portal implementation.

**Date**: 2026-01-10
**Status**: Phase 1 - In Progress
**Related Tasks**: Multiversion Support (T401-T427)

---

## Motivation

**Problem**: Custom Portal API has limited version availability
- 1.20.1: No compatible version available
- 1.21.1: Available but adds unnecessary dependency
- Future versions: Uncertain availability

**Solution**: Implement independent portal system
- Full version control (1.20.1 + 1.21.1 + future)
- Minimal dependencies
- Custom behavior tailored to ChronoDawn

---

## Existing Implementation (Already Complete)

### ✅ Portal State Management
- **Class**: `PortalStateMachine`
- **States**: INACTIVE → ACTIVATED → DEACTIVATED → STABILIZED
- **Location**: `common/src/main/java/com/chronodawn/core/portal/`

### ✅ Portal Frame Validation
- **Class**: `PortalFrameValidator`
- **Features**:
  - Frame size: 4x5 to 23x23 (width x height)
  - Frame block: Clockstone Block
  - Interior validation: Air or portal blocks
- **Location**: `common/src/main/java/com/chronodawn/core/portal/`

### ✅ Portal Registry
- **Class**: `PortalRegistry`
- **Features**:
  - Portal registration and lookup
  - Persistent storage via `PortalRegistryData`
- **Location**: `common/src/main/java/com/chronodawn/core/portal/`

### ✅ Portal Stabilizer Item
- **Class**: `PortalStabilizerItem`
- **Features**: Nearly complete, uses `PortalRegistry` and `PortalStateMachine`
- **Location**: `common/src/main/java/com/chronodawn/items/`

### ✅ Time Hourglass Item (Shell)
- **Class**: `TimeHourglassItem`
- **Status**: Skeleton implementation, needs portal ignition logic
- **Location**: `common/src/main/java/com/chronodawn/items/`

---

## Components to Implement

### ❌ Portal Block
**Status**: Not implemented
**Dependencies**: Custom Portal API (to be removed)

### ❌ Portal Ignition (Time Hourglass)
**Status**: Shell only
**Dependencies**: Custom Portal API (to be removed)

### ❌ Teleportation Logic
**Status**: Not implemented
**Dependencies**: Custom Portal API (to be removed)

### ❌ Destination Portal Auto-Generation
**Status**: Not implemented
**Dependencies**: Custom Portal API (to be removed)

---

## Implementation Phases

### Phase 1: Portal Block Creation ⏳ (Current)

**Goal**: Create custom portal block to replace Custom Portal API's portal blocks

**Tasks**:
1. Create `ChronoDawnPortalBlock` class
   - Extend `Block` (not `NetherPortalBlock` - too much Nether-specific logic)
   - Reference vanilla `NetherPortalBlock` for structure
   - Orange particles (#db8813 / RGB 219, 136, 19)
   - Collision detection for teleport trigger (Phase 3 implementation)
   - Render as translucent block

2. Register block in `ModBlocks`
   - Block properties: NoCollision, RandomTicks, LightEmission(11)
   - Block state: Axis (X or Z)

3. Create block model and textures (optional - can use procedural rendering)

**Estimated Time**: 1-2 hours

**Deliverables**:
- `common/src/main/java/com/chronodawn/blocks/ChronoDawnPortalBlock.java`
- Updated `ModBlocks.java`

---

### Phase 2: Portal Ignition Logic

**Goal**: Implement Time Hourglass portal ignition

**Tasks**:
1. Implement `TimeHourglassItem.useOn()`
   - Use `PortalFrameValidator` to validate frame
   - Fill interior with `ChronoDawnPortalBlock`
   - Register portal in `PortalRegistry` (state: ACTIVATED)
   - Consume item (except Creative mode)
   - Play ignition sound

2. Handle portal state restrictions
   - Check `ChronoDawnGlobalState.arePortalsUnstable()`
   - Show appropriate error messages

**Estimated Time**: 1-2 hours

**Deliverables**:
- Updated `TimeHourglassItem.java`

---

### Phase 3: Teleportation Logic

**Goal**: Implement player teleportation through portals

**Tasks**:
1. Implement `ChronoDawnPortalBlock.entityInside()`
   - Detect player collision with portal
   - Manage teleport cooldown (prevent instant re-teleport)
   - Call teleportation handler

2. Create `PortalTeleportHandler` class
   - Calculate destination coordinates
   - Search for existing portal at destination
   - Generate portal if none exists (Y=70-100 range)
   - Execute player teleportation
   - Update portal state (ACTIVATED → DEACTIVATED)

3. Handle edge cases
   - Creative mode teleportation
   - Entity teleportation (optional)
   - Failed teleport scenarios

**Estimated Time**: 2-3 hours

**Deliverables**:
- Updated `ChronoDawnPortalBlock.java`
- `common/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`

---

### Phase 4: Multi-Version Support

**Goal**: Ensure compatibility with 1.20.1 and 1.21.1

**Tasks**:
1. Version-specific teleportation API
   - 1.20.1: `changeDimension()` method
   - 1.21.1: `changeDimension()` method (check signature changes)

2. Version-specific particle effects (if needed)

3. Test both versions
   - 1.20.1 Fabric
   - 1.21.1 Fabric
   - 1.21.1 NeoForge

**Estimated Time**: 1 hour

**Deliverables**:
- Version-specific compatibility layer (if needed)

---

## Custom Portal API Removal Plan

**After Phase 1-4 Complete**:

1. Remove Custom Portal API dependency
   - `fabric/build.gradle`: Remove dependency
   - Delete `CustomPortalFabric.java`
   - Remove `ChronoDawnFabric.java` initialization call

2. Remove Custom Portal API Mixin
   - Delete `PortalPlacerMixin.java`
   - Update mixin config files

3. Verify clean build
   - No Custom Portal API references
   - Both versions build successfully

---

## Portal Behavior Specification

### Portal Creation Flow

```
Player builds Clockstone Block frame (4x5 to 23x23)
  ↓
Player right-clicks frame with Time Hourglass
  ↓
PortalFrameValidator validates frame
  ↓ (valid)
Fill interior with ChronoDawnPortalBlock
  ↓
Register portal in PortalRegistry (state: ACTIVATED)
  ↓
Consume Time Hourglass (except Creative)
  ↓
Play ignition sound + particles
```

### Teleportation Flow

```
Player enters portal (collision with ChronoDawnPortalBlock)
  ↓
Check teleport cooldown (prevent instant re-teleport)
  ↓
Check portal state in PortalRegistry
  ↓ (ACTIVATED or STABILIZED)
Calculate destination coordinates
  ↓
Search for existing portal at destination
  ↓ (not found)
Generate new portal at destination (Y=70-100)
  ↓
Teleport player to destination
  ↓
Update portal state:
  - If ACTIVATED → DEACTIVATED (one-way travel complete)
  - If STABILIZED → remains STABILIZED (bidirectional)
```

### Portal Stabilization Flow

```
Player right-clicks deactivated portal frame with Portal Stabilizer
  ↓
Find portal in PortalRegistry
  ↓
Check portal state (must be DEACTIVATED)
  ↓
Update state to STABILIZED
  ↓
Re-ignite portal blocks
  ↓
Consume Portal Stabilizer
  ↓
Portal now allows bidirectional travel
```

---

## Portal Visual Design

### Portal Color
- **Color**: Orange (#db8813 / RGB 219, 136, 19)
- **Theme**: Represents time/temporal energy (clock hands, brass gears)
- **Differentiation**: Distinct from Nether (purple), End (green), Aether (blue)

### Particle Effects
- **Ambient Particles**: Orange sparkles around frame
- **Activation**: Burst of orange particles when ignited
- **Teleportation**: Orange swirl effect during teleport

### Block Properties
- **Collision**: None (players walk through)
- **Light Level**: 11 (similar to Nether Portal)
- **Sound**: Custom portal ambient sound (optional)
- **Render**: Translucent, animated texture

---

## Testing Plan

### Phase 1 Testing
- [ ] Portal block renders correctly
- [ ] Orange color visible
- [ ] No collision (player walks through)
- [ ] Light emission works (level 11)
- [ ] Block state (axis) functions correctly

### Phase 2 Testing
- [ ] Time Hourglass validates frame correctly
- [ ] Portal ignition fills interior with portal blocks
- [ ] Portal registered in PortalRegistry
- [ ] Item consumed (except Creative)
- [ ] Ignition sound plays
- [ ] Unstable portal prevention works

### Phase 3 Testing
- [ ] Player teleports when entering portal
- [ ] Destination portal generated (Y=70-100)
- [ ] Portal state updates correctly
- [ ] Teleport cooldown prevents instant re-teleport
- [ ] One-way travel (ACTIVATED → DEACTIVATED)
- [ ] Bidirectional travel (STABILIZED)

### Phase 4 Testing
- [ ] 1.20.1 Fabric: All features work
- [ ] 1.21.1 Fabric: All features work
- [ ] 1.21.1 NeoForge: All features work

---

## Reference Implementation

**Vanilla Nether Portal** (for reference):
- `net.minecraft.world.level.block.NetherPortalBlock`
- `net.minecraft.world.level.portal.PortalForcer`
- Portal frame: Obsidian (10x23 max)
- Ignition: Flint and Steel
- Destination: Coordinate scaling (1:8 ratio)

**ChronoDawn Portal Differences**:
- Frame: Clockstone Block (23x23 max)
- Ignition: Time Hourglass (consumable)
- Destination: 1:1 coordinate mapping (no scaling)
- States: INACTIVE/ACTIVATED/DEACTIVATED/STABILIZED
- One-way → Bidirectional (via Portal Stabilizer)

---

## Notes

- Custom Portal API removal will reduce JAR size and dependencies
- Independent implementation allows full control over portal behavior
- Existing state machine and validation logic are reusable
- Portal behavior matches ChronoDawn's game design (one-way → bidirectional)

---

## Change Log

- 2026-01-10: Initial plan created
- 2026-01-10: Phase 1 started (ChronoDawnPortalBlock)
