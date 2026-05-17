# Portal Destination Water/Lava Avoidance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop the ChronoDawn destination portal from materialising inside water/lava and always give the player a Clockstone footing to step out onto.

**Architecture:** Modify `PortalTeleportHandler.java` in all 11 version-specific common modules. Add fluid-aware position predicates, replace the downward-through-fluid search in `findGroundLevel()` with an upward air-search, and unconditionally write a 4-block Clockstone footing under the frame's bottom row in `generatePortalStructure()`. A predicate-level unit test lives only in `common/1.21.11` (reference version).

**Tech Stack:** Java 21, Architectury, Mojang mappings, JUnit 5, Mockito (already on classpath in 1.21.11).

**Spec:** [docs/superpowers/specs/2026-05-17-portal-destination-water-lava-avoidance-design.md](../specs/2026-05-17-portal-destination-water-lava-avoidance-design.md)

---

## File map

Each version directory contains its own `PortalTeleportHandler.java`. Same logical change everywhere; only the `Level` min-Y API name differs across the version split:

| Version | Min-Y API           | Path                                                                                          |
|---------|---------------------|-----------------------------------------------------------------------------------------------|
| 1.20.1  | `getMinBuildHeight` | `common/1.20.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.1  | `getMinBuildHeight` | `common/1.21.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.2  | `getMinBuildHeight` | `common/1.21.2/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.4  | `getMinY`           | `common/1.21.4/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.5  | `getMinY`           | `common/1.21.5/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.6  | `getMinY`           | `common/1.21.6/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.7  | `getMinY`           | `common/1.21.7/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.8  | `getMinY`           | `common/1.21.8/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.9  | `getMinY`           | `common/1.21.9/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`           |
| 1.21.10 | `getMinY`           | `common/1.21.10/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`          |
| 1.21.11 | `getMinY`           | `common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`          |

Test file (added only in 1.21.11, reference version): `common/1.21.11/src/test/java/com/chronodawn/core/portal/PortalGroundPredicatesTest.java`

---

## Shared edit recipe

These three changes apply identically to every version's `PortalTeleportHandler.java`. Tasks below reference this recipe and only call out per-version deltas.

### Recipe step A: add `isClearForPortalSpace` static helper

Locate the existing `isSuitablePortalGround` method (currently a single line). Replace it with the two helpers below, in the same position:

```java
private static boolean isClearForPortalSpace(BlockState state) {
    // Air or replaceable (e.g., grass/snow), but NOT a fluid.
    // Water/lava are intentionally excluded so the upward search never
    // treats a water column as usable air space.
    return (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty();
}

private static boolean isSuitablePortalGround(BlockState state) {
    return state.isSolid()
        && !state.isAir()
        && !state.canBeReplaced()
        && state.getFluidState().isEmpty()
        && !state.is(BlockTags.LEAVES);
}
```

### Recipe step B: replace `findGroundLevel` body

Replace the entire method body with the upward-search implementation below:

```java
private static BlockPos findGroundLevel(ServerLevel level, BlockPos start) {
    // Server-time heightmap (not the *_WG worldgen variant).
    int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, start.getX(), start.getZ());

    // Cap upward search at a fixed safe ceiling instead of probing
    // version-specific Level max-Y APIs. Worlds always go higher than 250.
    final int upwardCeiling = 250;
    final int requiredAir = 6;            // 1 stand-on + 5 portal height

    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(start.getX(), surfaceY, start.getZ());

    for (int y = surfaceY; y <= upwardCeiling; y++) {
        cursor.setY(y);
        BlockState below = level.getBlockState(cursor.below());
        boolean groundIsSolid = isSuitablePortalGround(below);

        // Check 6-block clear column at y..y+5.
        boolean columnClear = true;
        for (int dy = 0; dy < requiredAir; dy++) {
            cursor.setY(y + dy);
            if (!isClearForPortalSpace(level.getBlockState(cursor))) {
                columnClear = false;
                break;
            }
        }

        if (columnClear && groundIsSolid) {
            // Natural land with clear space above.
            return new BlockPos(start.getX(), y, start.getZ());
        }
        if (columnClear) {
            // Clear column but no solid floor — the platform forced by
            // generatePortalStructure() will provide footing.
            return new BlockPos(start.getX(), y, start.getZ());
        }
    }

    // Last-resort fallback: float at Y=120. The forced Clockstone footing
    // in generatePortalStructure() still guarantees a step-out surface.
    return new BlockPos(start.getX(), 120, start.getZ());
}
```

Delete the existing `isSuitablePortalGround` line that lived as a separate method declaration (it is now defined inside the helper block from step A).

### Recipe step C: drop the downward-adjustment loop in `generatePortalStructure`, write the footing only in `generatePortal`

Two edits, in two different methods:

**C.1 — In `generatePortalStructure(ServerLevel level, BlockPos pos, Direction.Axis axis)`:**

Delete the entire `needsAdjustment` / `while (adjustedPos.getY() > level.getMinBuildHeight())` (or `getMinY()`) block that searches downward for solid ground. `findGroundLevel` is now authoritative. Do **not** add a footing loop here — this method is also called from `findReusablePortalFrame` to re-light an existing returning frame, and we must not overwrite player-placed blocks under a reused frame.

**C.2 — In `generatePortal(ServerLevel level, BlockPos coords, Direction.Axis axis)`:**

After the existing `generatePortalStructure(level, groundPos, axis);` call and before `return groundPos;`, add the forced footing:

```java
// Write the 4-block Clockstone footing under the frame's bottom row, but only
// on the new-portal path (this method). The reused-frame path
// (findReusablePortalFrame -> generatePortalStructure) must not touch what the
// player has placed below an existing returning frame.
BlockState footingState = ModBlocks.CLOCKSTONE_BLOCK.get().defaultBlockState();
Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
for (int x = 0; x < 4; x++) {
    BlockPos footing = groundPos.relative(horizontal, x).below();
    level.setBlock(footing, footingState, 3);
}
```

The literal `4` matches the existing `width = 4` in `generatePortalStructure`; the literal `3` matches `Block.UPDATE_ALL` used elsewhere in the same file.

---

## Tasks

### Task 1: Apply the change to 1.21.11 (reference version) and add the predicate unit test

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Create: `common/1.21.11/src/test/java/com/chronodawn/core/portal/PortalGroundPredicatesTest.java`

- [ ] **Step 1: Read the current 1.21.11 PortalTeleportHandler**

Run: `wc -l common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
Expected: ~819 lines.

Skim it so you know exactly where `findGroundLevel`, `isSuitablePortalGround`, and `generatePortalStructure` are located.

- [ ] **Step 2: Apply Recipe step A** — replace the `isSuitablePortalGround` method with the two-helper block from the shared recipe above.

- [ ] **Step 3: Apply Recipe step B** — replace the entire body of `findGroundLevel`.

- [ ] **Step 4: Apply Recipe step C** — see the two-part instruction in the shared recipe above. C.1 removes the downward-adjustment block from `generatePortalStructure` (uses `level.getMinY()` on this version). C.2 inserts the forced-footing loop at the end of `generatePortal`, **not** inside `generatePortalStructure`, so the reused-frame relight path does not overwrite player-placed blocks.

- [ ] **Step 5: Make the two new helpers package-private for testing**

Change the access modifier on both `isClearForPortalSpace` and `isSuitablePortalGround` from `private static` to `static` (package-private). No other visibility changes.

- [ ] **Step 6: Write the failing predicate test**

Create `common/1.21.11/src/test/java/com/chronodawn/core/portal/PortalGroundPredicatesTest.java`:

```java
package com.chronodawn.core.portal;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the static position predicates in {@link PortalTeleportHandler}.
 * Verifies that fluids are rejected by both "clear space" and "ground" checks,
 * preventing the destination portal from being placed underwater or in lava.
 */
class PortalGroundPredicatesTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void clearSpace_air_isClear() {
        BlockState air = Blocks.AIR.defaultBlockState();
        assertTrue(PortalTeleportHandler.isClearForPortalSpace(air));
    }

    @Test
    void clearSpace_water_isNotClear() {
        BlockState water = Blocks.WATER.defaultBlockState();
        assertFalse(PortalTeleportHandler.isClearForPortalSpace(water),
            "Water must not count as clear space; otherwise portals generate underwater.");
    }

    @Test
    void clearSpace_lava_isNotClear() {
        BlockState lava = Blocks.LAVA.defaultBlockState();
        assertFalse(PortalTeleportHandler.isClearForPortalSpace(lava),
            "Lava must not count as clear space.");
    }

    @Test
    void clearSpace_snowLayer_isClear() {
        BlockState snow = Blocks.SNOW.defaultBlockState();
        assertTrue(PortalTeleportHandler.isClearForPortalSpace(snow),
            "Replaceable non-fluid blocks (snow layer) should still count as clear (vanilla parity).");
    }

    @Test
    void ground_stone_isSuitable() {
        BlockState stone = Blocks.STONE.defaultBlockState();
        assertTrue(PortalTeleportHandler.isSuitablePortalGround(stone));
    }

    @Test
    void ground_water_isNotSuitable() {
        BlockState water = Blocks.WATER.defaultBlockState();
        assertFalse(PortalTeleportHandler.isSuitablePortalGround(water),
            "Water must not count as solid ground; otherwise portals settle on the seabed.");
    }

    @Test
    void ground_lava_isNotSuitable() {
        BlockState lava = Blocks.LAVA.defaultBlockState();
        assertFalse(PortalTeleportHandler.isSuitablePortalGround(lava));
    }
}
```

Note: `Blocks.SNOW` is the snow-layer block (not snow block); it is `canBeReplaced=true` and is confirmed present in `common/1.21.11/.../TemporalGrassBlock.java:75`.

Note: a leaves-rejection test was intentionally omitted. `BlockTags.LEAVES` is populated by data-pack tag bindings that `Bootstrap.bootStrap()` alone does not load, so `state.is(BlockTags.LEAVES)` returns `false` in the unit-test environment. The leaves exclusion in `isSuitablePortalGround` is pre-existing code — this PR does not change it — so dropping the test case does not reduce coverage of the change being introduced.

- [ ] **Step 7: Run the new test and verify it passes**

Run: `./gradlew :common-1.21.11:test --tests com.chronodawn.core.portal.PortalGroundPredicatesTest -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL, 7 tests passed.

- [ ] **Step 8: Run the full 1.21.11 unit test suite as a regression check**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL, all existing tests still pass.

- [ ] **Step 9: Build 1.21.11 to confirm the production code still compiles**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 10: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.11/src/test/java/com/chronodawn/core/portal/PortalGroundPredicatesTest.java
git commit -m "$(cat <<'EOF'
fix(portal): avoid water/lava on destination portal generation (1.21.11)

Replace the downward-through-fluid ground search with an upward
air-search, exclude fluids from clear-space and ground predicates, and
always lay a 4-block Clockstone footing under the frame so the player
arrives on solid ground. Adds PortalGroundPredicatesTest covering the
new fluid-aware predicates.
EOF
)"
```

---

### Task 2: Replicate the fix across 1.21.4 — 1.21.10 (identical Min-Y API)

These 7 versions share the same `getMinY()` API and the same source structure as 1.21.11. The edit is mechanically identical to Task 1 steps 2–4. No new test files.

**Files (modify each):**
- `common/1.21.4/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.5/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.6/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.7/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.8/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.9/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.10/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`

- [ ] **Step 1: For each of the 7 files above**
  - Apply Recipe step A (helper replacement)
  - Apply Recipe step B (`findGroundLevel` body replacement)
  - Apply Recipe step C (drop downward adjustment + add forced footing)
  - Helpers stay `private static` (no test file in these versions, so no visibility relaxation needed)

- [ ] **Step 2: Build each touched version individually**

Run, in sequence:
```
./gradlew build1_21_4
./gradlew build1_21_5
./gradlew build1_21_6
./gradlew build1_21_7
./gradlew build1_21_8
./gradlew build1_21_9
./gradlew build1_21_10
```
Expected: BUILD SUCCESSFUL for each. If any fails, the most likely cause is a stray `getMinY()` reference that was not removed when the downward loop was deleted; re-open Recipe step C and confirm.

- [ ] **Step 3: Run unit tests for each touched version**

Run, in sequence:
```
./gradlew :common-1.21.4:test -Ptarget_mc_version=1.21.4
./gradlew :common-1.21.5:test -Ptarget_mc_version=1.21.5
./gradlew :common-1.21.6:test -Ptarget_mc_version=1.21.6
./gradlew :common-1.21.7:test -Ptarget_mc_version=1.21.7
./gradlew :common-1.21.8:test -Ptarget_mc_version=1.21.8
./gradlew :common-1.21.9:test -Ptarget_mc_version=1.21.9
./gradlew :common-1.21.10:test -Ptarget_mc_version=1.21.10
```
Expected: BUILD SUCCESSFUL for each.

- [ ] **Step 4: Commit**

```bash
git add common/1.21.4/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.5/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.6/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.7/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.8/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.9/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.10/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java
git commit -m "$(cat <<'EOF'
fix(portal): avoid water/lava on destination portal generation (1.21.4-1.21.10)

Propagate the upward air-search and forced Clockstone footing introduced
for 1.21.11 to all seven intermediate versions sharing the getMinY() API.
EOF
)"
```

---

### Task 3: Replicate the fix across 1.20.1, 1.21.1, 1.21.2 (older `getMinBuildHeight` API)

Same logical change as Task 2, but the downward-adjustment loop being removed uses `level.getMinBuildHeight()` instead of `level.getMinY()`. Since Recipe step C deletes the loop entirely, the API difference does not appear in the resulting source. No special handling needed.

**Files:**
- `common/1.20.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- `common/1.21.2/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`

- [ ] **Step 1: For each of the 3 files above**
  - Apply Recipe step A
  - Apply Recipe step B
  - Apply Recipe step C — confirm the deleted block contained `getMinBuildHeight`, not `getMinY`. After deletion the API name is gone.
  - Helpers stay `private static`

- [ ] **Step 2: Build each touched version**

Run, in sequence:
```
./gradlew build1_20_1
./gradlew build1_21_1
./gradlew build1_21_2
```
Expected: BUILD SUCCESSFUL for each.

- [ ] **Step 3: Run unit tests for each touched version**

Run, in sequence:
```
./gradlew :common-1.20.1:test -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1
./gradlew :common-1.21.2:test -Ptarget_mc_version=1.21.2
```
Expected: BUILD SUCCESSFUL for each.

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.2/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java
git commit -m "$(cat <<'EOF'
fix(portal): avoid water/lava on destination portal generation (1.20.1/1.21.1/1.21.2)

Apply the upward air-search and forced Clockstone footing to the three
older versions that use Level.getMinBuildHeight(). The downward-search
removal eliminates the API-specific call site, so no compat layer is needed.
EOF
)"
```

---

### Task 4: Full multi-version verification

This task does not modify code — it confirms the cumulative effect of Tasks 1–3 across all supported versions.

- [ ] **Step 1: Run the project-wide check pipeline**

Run: `./gradlew checkAll`
Expected: BUILD SUCCESSFUL.

`checkAll` chains cleanAll → validateResources → validateTranslations → buildAll → testAll → gameTestAll. If `gameTestAll` is flaky on your machine, fall back to `./gradlew buildAll && ./gradlew testAll` per `feedback_buildall_gametestall_wrapper_unreliable.md` and confirm each ran to completion individually.

- [ ] **Step 2: Manual smoke test on 1.21.11 (recommended pre-merge step)**

The check pipeline cannot exercise the actual portal-in-water case. Perform this manually on at least one version (1.21.11 chosen as the latest):

1. `./gradlew runClientFabric1_21_11` (or NeoForge equivalent)
2. Locate or generate a small island in an ocean biome surrounded by ≥5-block-deep water. Note the X/Z coordinates.
3. Build a ChronoDawn portal on the island, activate it, travel to ChronoDawn.
4. Return through the portal.
5. Confirm: in the Overworld, the destination portal does **not** appear inside the water column. The frame sits on a visible Clockstone footing, and the player exits onto a walkable block.
6. Repeat over a lava lake (use `/setblock` to seed lava at the target X/Z if natural geography is uncooperative).
7. Repeat on flat grassland. Confirm no user-visible regression (the footing row is buried under the frame's bottom edge and not visible).

Record the verification result in the PR description.

- [ ] **Step 3: No commit unless step 1 surfaced a fix**

If `checkAll` is green, this task closes without a commit.

---

## Out-of-scope reminders (do not implement)

- Step-out platform extending in front of the portal
- Rewriting the destination X/Z mapping
- Regenerating existing underwater portals from saved worlds
- Reused / reignited frames (`findReusablePortalFrame`)
- Lifting helpers into `common/shared/` (per existing pattern, intentionally kept per-version)

---

## Revision 2 (2026-05-17 — post-manual-test amendments)

After the original 4 tasks landed, in-game smoke testing surfaced two issues:

1. **Over-correction on water surface.** Source portal built with frame
   bottom row 1 block submerged and interior just above the water surface
   produced a destination portal placed 1 block HIGHER than the equivalent
   natural position — the destination's frame floated above the water with
   nothing touching the fluid. The user's hand-built convention is the
   reference behaviour; the destination should match.
2. **Re-ignition failure due to forced footing.** With an extra row of
   Clockstone directly beneath the bottom-frame row (the forced footing
   introduced by Recipe step C.2 of the original plan), `PortalFrameValidator`
   rejected the shape during re-ignition. A user trying to relight an
   extinguished generated portal could not do so. The validator quirk is
   technically a separate issue; we sidestep it by not creating the
   non-canonical shape.

The spec at
`docs/superpowers/specs/2026-05-17-portal-destination-water-lava-avoidance-design.md`
has been amended (Goals section and Approach sections 1–4). The recipe and
tasks below replace the corresponding sections of the original plan above.

### Revised recipe

#### R-A — `isClearForPortalSpace` only

Keep `isClearForPortalSpace` exactly as introduced in original Recipe A.
**Delete** the `isSuitablePortalGround` method entirely. It has no remaining
callers after R-B (below).

```java
private static boolean isClearForPortalSpace(BlockState state) {
    // Air or replaceable (e.g., snow layer / grass), but NOT a fluid.
    // Water/lava are intentionally excluded so the upward search never
    // treats a water column as usable air space.
    return (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty();
}
```

#### R-B — `findGroundLevel` simplified (interior-only check, fluid-surface awareness)

Replace the entire `findGroundLevel` body (the one introduced in the
original Recipe B) with:

```java
private static BlockPos findGroundLevel(ServerLevel level, BlockPos start) {
    // Server-time heightmap (not the *_WG worldgen variant).
    int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, start.getX(), start.getZ());

    final int upwardCeiling = 250;
    final int interiorHeight = 3;   // portal blocks at y+1, y+2, y+3

    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(start.getX(), 0, start.getZ());

    // Default: frame's bottom row sits ABOVE the natural surface (normal land
    // placement, identical to a player-built portal).
    int frameY = surfaceY;

    // Exception: if the topmost natural block is a fluid (open water / lava),
    // let the bottom-frame row occupy that fluid block — when the frame is
    // written, the fluid block is overwritten with Clockstone and the
    // interior 3-block column ends up right at the fluid surface, dry.
    cursor.setY(surfaceY - 1);
    if (!level.getBlockState(cursor).getFluidState().isEmpty()) {
        frameY = surfaceY - 1;
    }

    // Push frameY upward only if the portal interior would still be in fluid
    // (or any non-clear block) at the chosen frameY.
    for (int y = frameY; y <= upwardCeiling; y++) {
        boolean interiorClear = true;
        for (int dy = 1; dy <= interiorHeight; dy++) {
            cursor.setY(y + dy);
            if (!isClearForPortalSpace(level.getBlockState(cursor))) {
                interiorClear = false;
                break;
            }
        }
        if (interiorClear) {
            return new BlockPos(start.getX(), y, start.getZ());
        }
    }

    // Last-resort fallback: float at Y=120.
    return new BlockPos(start.getX(), 120, start.getZ());
}
```

Notes:

- The bottom-frame row at `frameY` is intentionally NOT checked. It is
  always overwritten by `generatePortalStructure` and is allowed to coincide
  with a fluid block (intended for the open-water case) or with a solid
  surface block (the player-built convention is to set portals down on solid
  ground, replacing the topsoil; vanilla Nether does the same with its
  obsidian platform).
- `requiredAir = 6` and the dual-branch "natural land vs. forced footing"
  return logic from the original Recipe B are both gone. The new method has
  a single return path.

#### R-C — drop the forced footing entirely

The original Recipe C had two parts. R-C keeps part C.1 (delete the
downward-adjustment block from `generatePortalStructure`) and **reverts**
part C.2 — `generatePortal` reverts to its short pre-footing form:

```java
private static BlockPos generatePortal(ServerLevel level, BlockPos coords, Direction.Axis axis) {
    BlockPos groundPos = findGroundLevel(level, coords);
    generatePortalStructure(level, groundPos, axis);
    return groundPos;
}
```

No `BlockState footingState`, no footing loop, no `Direction horizontal`
local. With R-B placing the frame at a Y where the interior is already
clear (and the bottom-frame row replacing whatever is below), no extra
Clockstone is needed.

#### R-D — predicate test cleanup

In `common/1.21.11/src/test/java/com/chronodawn/core/portal/PortalGroundPredicatesTest.java`,
delete the three `ground_*_*` tests:

- `ground_stone_isSuitable`
- `ground_water_isNotSuitable`
- `ground_lava_isNotSuitable`

These tested `isSuitablePortalGround`, which is now removed. The 4 remaining
`clearSpace_*` tests cover `isClearForPortalSpace` exactly as before.

If `Blocks` is no longer referenced by the surviving imports after the
delete, also remove the unused `import net.minecraft.world.level.block.Blocks;`
line (it should still be needed for the surviving water / lava / snow / air
tests, so likely unchanged).

### Revision-2 task list

#### Task 5: Apply R-A, R-B, R-C, R-D to 1.21.11 (reference)

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.11/src/test/java/com/chronodawn/core/portal/PortalGroundPredicatesTest.java`

- [ ] **Step 1:** Apply R-A (delete `isSuitablePortalGround`).
- [ ] **Step 2:** Apply R-B (replace `findGroundLevel` body with the simplified version).
- [ ] **Step 3:** Apply R-C (revert `generatePortal` to the short form, no footing loop).
- [ ] **Step 4:** Apply R-D (delete the three `ground_*` tests in `PortalGroundPredicatesTest`).
- [ ] **Step 5:** Run the predicate-only test:
      `./gradlew :common-1.21.11:test --tests com.chronodawn.core.portal.PortalGroundPredicatesTest -Ptarget_mc_version=1.21.11` — expect 4 tests, BUILD SUCCESSFUL.
- [ ] **Step 6:** Run the full 1.21.11 test suite. BUILD SUCCESSFUL.
- [ ] **Step 7:** Build 1.21.11. BUILD SUCCESSFUL.
- [ ] **Step 8:** Commit:

```bash
git add common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.11/src/test/java/com/chronodawn/core/portal/PortalGroundPredicatesTest.java
git commit -m "$(cat <<'EOF'
fix(portal): drop forced footing, only correct Y when interior in fluid (1.21.11)

Manual smoke test surfaced two issues with the previous fix:
- destination over-corrected by 1 block when source frame had its bottom
  row 1 block submerged with interior just above water;
- the extra Clockstone footing below the bottom-frame row created a
  non-canonical shape that PortalFrameValidator rejects on re-ignition.

Drop isSuitablePortalGround and the forced footing entirely. New
findGroundLevel keeps the frame at surfaceY by default, lowers it to
surfaceY-1 only when the topmost natural block is a fluid (open water /
lava surface), and only pushes upward if the portal interior would still
be in fluid. The bottom-frame row is the player's step-out surface,
identical to vanilla Nether portal placement.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

#### Task 6: Replicate R-A, R-B, R-C to 1.21.4 — 1.21.10

Identical mechanical change to 7 sister versions. No test changes (the test
file only lives in 1.21.11). Helpers stay `private static` in these versions
(as in the original plan).

**Files:** the seven `common/1.21.{4..10}/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`.

- [ ] For each of the 7 files: apply R-A, R-B, R-C.
- [ ] Build & test each: `build1_21_X` + `:common-1.21.X:test -Ptarget_mc_version=1.21.X`.
- [ ] Single commit with all 7 files staged:

```bash
git commit -m "$(cat <<'EOF'
fix(portal): drop forced footing, only correct Y when interior in fluid (1.21.4-1.21.10)

Mechanical propagation of the 1.21.11 revision to seven sister versions
sharing the same Java sources for PortalTeleportHandler.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

#### Task 7: Replicate R-A, R-B, R-C to 1.20.1 / 1.21.1 / 1.21.2

Same change to the three oldest versions. Identical mechanical edit.

**Files:** the three `common/{1.20.1, 1.21.1, 1.21.2}/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`.

- [ ] For each of the 3 files: apply R-A, R-B, R-C.
- [ ] Build & test each.
- [ ] Single commit with all 3 files staged:

```bash
git commit -m "$(cat <<'EOF'
fix(portal): drop forced footing, only correct Y when interior in fluid (1.20.1/1.21.1/1.21.2)

Mechanical propagation of the revision to the three older versions.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

#### Task 8: Hand back to user for manual re-test

The same two scenarios that surfaced the issues, plus a re-ignite check:

1. Build a source portal with the bottom-frame row 1 block submerged in the
   ocean; interior just above water. Activate, travel, return. The
   destination portal should sit at the same relative Y — bottom-frame row
   at the water surface (replacing the top water block); interior dry.
2. Same on flat grass. Destination frame sits AT or ABOVE the surface
   (depends on which is more natural for the spawn point); no extra
   Clockstone below.
3. Re-ignite test: break a portal block of a freshly-generated destination
   portal, then re-light it with a Time Hourglass. Frame must be recognised
   by `PortalFrameValidator` and the portal re-ignite cleanly.

If 1 or 2 still misbehave: re-open R-B logic. If 3 fails despite no extra
Clockstone below the frame, the `PortalFrameValidator` itself has a
pre-existing bug — file a separate issue.
