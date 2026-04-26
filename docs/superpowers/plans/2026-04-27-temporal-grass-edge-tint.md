# Temporal Grass Block Edge Tint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a 2-block-wide gradient ring around Temporal Sand / Temporal Gravel circles by shifting the Temporal Grass Block tint toward a paler "edge" color when the block is within 2 blocks (Chebyshev, same Y) of any sand/gravel block.

**Architecture:** A new pure-Java helper `TemporalGrassEdgeTint` in `common/shared/` owns the blend math (per-channel linear interpolation between the biome grass color and a fixed `EDGE_TINT`) and the edge-trigger detection. The existing per-loader, per-version block-color lambdas (11 Fabric + 9 NeoForge places) are rewritten to delegate to this helper via a method reference. No worldgen, data pack, or block-class changes — purely client-side.

**Tech Stack:** Java 21, Minecraft 1.20.1 / 1.21.1–1.21.11, Architectury (Fabric + NeoForge multi-loader), JUnit 5 (`org.junit.jupiter.api`), Gradle (Groovy DSL), Mojang mappings.

**Spec:** `docs/superpowers/specs/2026-04-27-temporal-grass-edge-tint-design.md`

**Files this plan touches:**

- New: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`
- New: `common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java`
- Modify (Fabric, 11 files): `fabric/{1.20.1,1.21.1,1.21.2,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11}/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify (NeoForge, 9 files): `neoforge/{base,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11}/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java` (note: 1.21.1 and 1.21.2 use `neoforge/base/` directly)

---

### Task 1: Add helper class with TDD on `lerpRgb`

**Files:**
- Create: `common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java`
- Create: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`

The blend math (per-channel linear interpolation) is the only piece that can be unit-tested without a Minecraft runtime. The existing test suite under `common/shared/src/test/java/com/chronodawn/unit/` is JSON-validation-style, no Mockito; we use the same JUnit 5 setup. Mocking `BlockAndTintGetter` for `blend()` would require infrastructure that does not exist in this project — those code paths are covered by the visual smoke test in Task 3.

- [ ] **Step 1: Write the failing test**

Create `common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java`:

```java
package com.chronodawn.unit;

import com.chronodawn.client.TemporalGrassEdgeTint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link TemporalGrassEdgeTint}.
 * Only the pure math (lerpRgb) is testable without a Minecraft runtime.
 * Neighborhood scan and provider entry are validated by visual smoke test.
 */
class TemporalGrassEdgeTintTest {

    @Test
    void lerpRgb_t0_returnsA() {
        assertEquals(0x123456, TemporalGrassEdgeTint.lerpRgb(0x123456, 0xABCDEF, 0.0f));
    }

    @Test
    void lerpRgb_t1_returnsB() {
        assertEquals(0xABCDEF, TemporalGrassEdgeTint.lerpRgb(0x123456, 0xABCDEF, 1.0f));
    }

    @Test
    void lerpRgb_tHalf_returnsRoundedMidpoint() {
        // (0x10 + 0x20) / 2 = 0x18 per channel
        assertEquals(0x181818, TemporalGrassEdgeTint.lerpRgb(0x101010, 0x202020, 0.5f));
    }

    @Test
    void lerpRgb_grassToEdgeSample() {
        // Sanity check with the actual default constants.
        // base = 0x5B8AC4, edge = 0x9CB6CC, t = 1/3 (distance 2)
        // r: 0x5B + (0x9C - 0x5B)/3 = 0x5B + 0x14 = 0x6F  (91 + 65/3 ≈ 91 + 22 = 113 = 0x71; rounding)
        int actual = TemporalGrassEdgeTint.lerpRgb(0x5B8AC4, 0x9CB6CC, 1.0f / 3.0f);
        // Per-channel: 91→114 (R), 138→152 (G), 196→206 (B)  with float math + round
        // Compute exactly here so the test is precise: roundUp behaviour with Math.round
        int r = Math.round(0x5B + (0x9C - 0x5B) * (1.0f / 3.0f));
        int g = Math.round(0x8A + (0xB6 - 0x8A) * (1.0f / 3.0f));
        int b = Math.round(0xC4 + (0xCC - 0xC4) * (1.0f / 3.0f));
        int expected = (r << 16) | (g << 8) | b;
        assertEquals(expected, actual);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.TemporalGrassEdgeTintTest"
```

Expected: FAIL with compilation error `class TemporalGrassEdgeTint not found` (or similar).

- [ ] **Step 3: Create the helper class**

Create `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`:

```java
package com.chronodawn.client;

import com.chronodawn.blocks.TemporalGrassBlock;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Computes the per-block tint for {@link TemporalGrassBlock}, blending the
 * biome grass color toward a pale "edge" color when the block is near a
 * Temporal Sand / Temporal Gravel disk (or vanilla sand/gravel placed by a
 * player).
 *
 * Within 2 blocks (Chebyshev, same Y) of an edge trigger, the grass tint
 * lerps toward {@link #EDGE_TINT} with weight {@code (3 - d) / 3}, so the
 * gradient is strongest at distance 1 and fades out by distance 3.
 *
 * Pure function — no caching, recomputed on every chunk mesh bake. Cost is
 * comparable to vanilla {@link BiomeColors#getAverageGrassColor}.
 */
public final class TemporalGrassEdgeTint {

    /** Returned for inventory / fallback contexts where world+pos cannot be sampled. */
    public static final int DEFAULT_FALLBACK = 0x5B8AC4;

    /**
     * Color the tint shifts toward at the edge of a sand/gravel disk.
     * Initial value picked as a midpoint between {@link #DEFAULT_FALLBACK} and
     * the average pixel color of {@code temporal_sand.png}; refined in Task 6
     * after sampling the texture exactly.
     */
    public static final int EDGE_TINT = 0x9CB6CC;

    /** Chebyshev radius scanned for edge triggers. */
    private static final int RADIUS = 2;

    private TemporalGrassEdgeTint() {}

    /**
     * Block-color provider entry point. Hand this as a method reference to
     * {@code ColorProviderRegistry.BLOCK.register} (Fabric) or
     * {@code RegisterColorHandlersEvent.Block#register} (NeoForge).
     */
    public static int provide(BlockState state, @Nullable BlockAndTintGetter world,
                              @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        if (world == null || pos == null
                || !(world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock)) {
            return DEFAULT_FALLBACK;
        }
        int base = BiomeColors.getAverageGrassColor(world, pos);
        return blend(world, pos, base);
    }

    /**
     * Pure blend: scan the {@code RADIUS}-block Chebyshev neighborhood at the
     * same Y as {@code pos} and lerp {@code baseTint} toward {@link #EDGE_TINT}
     * by the closest edge-trigger distance.
     */
    static int blend(BlockGetter world, BlockPos pos, int baseTint) {
        int minDist = RADIUS + 1;
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        outer:
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                int d = Math.max(Math.abs(dx), Math.abs(dz));
                if (d >= minDist) continue;
                cur.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                if (isEdgeTrigger(world.getBlockState(cur))) {
                    minDist = d;
                    if (d == 1) break outer; // cannot improve
                }
            }
        }
        if (minDist > RADIUS) return baseTint;
        float t = (RADIUS + 1 - minDist) / (float) (RADIUS + 1);
        return lerpRgb(baseTint, EDGE_TINT, t);
    }

    static boolean isEdgeTrigger(BlockState state) {
        return state.is(ModBlocks.TEMPORAL_SAND.get())
            || state.is(ModBlocks.TEMPORAL_GRAVEL.get())
            || state.is(Blocks.SAND)
            || state.is(Blocks.GRAVEL);
    }

    /** Per-channel linear interpolation. {@code t} is clamped by callers. */
    static int lerpRgb(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run:

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.TemporalGrassEdgeTintTest"
```

Expected: 4 tests pass.

- [ ] **Step 5: Commit**

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java \
        common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java
git commit -m "feat(client): add TemporalGrassEdgeTint helper for sand/gravel edge blending"
```

---

### Task 2: Wire helper into Fabric 1.21.11 and visually verify

**Files:**
- Modify: `fabric/1.21.11/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`

This is the canary version — once it looks right, Task 3 fans out the same change to the other 10 Fabric files mechanically.

- [ ] **Step 1: Replace the existing Temporal Grass Block lambda body**

Open `fabric/1.21.11/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java` and find the block (search for `ModBlocks.TEMPORAL_GRASS_BLOCK.get()` near the bottom of `registerBlockColors()`):

Replace this block:

```java
        // Register Temporal Grass Block color (biome-dependent grass tint)
        // Only tint faces with tintIndex 0 (top and side overlay in grass_block model).
        // Check that the block at pos is actually TemporalGrassBlock to avoid
        // item rendering context (which may pass player's world/pos, returning wrong biome color).
        ColorProviderRegistry.BLOCK.register(
            (state, world, pos, tintIndex) -> {
                if (tintIndex != 0) return -1;
                if (world != null && pos != null
                        && world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock) {
                    return BiomeColors.getAverageGrassColor(world, pos);
                }
                return 0x5B8AC4; // Chrono Dawn plains grass_color
            },
            ModBlocks.TEMPORAL_GRASS_BLOCK.get()
        );
```

With:

```java
        // Register Temporal Grass Block color: biome grass tint, blended toward a pale
        // edge color near Temporal Sand / Gravel disks. Logic lives in the shared helper.
        ColorProviderRegistry.BLOCK.register(
            TemporalGrassEdgeTint::provide,
            ModBlocks.TEMPORAL_GRASS_BLOCK.get()
        );
```

Add the import (sort alphabetically with existing `com.chronodawn.client.*` imports):

```java
import com.chronodawn.client.TemporalGrassEdgeTint;
```

The existing imports `com.chronodawn.blocks.TemporalGrassBlock`, `net.minecraft.client.renderer.BiomeColors`, and any others used only by the removed lambda body can be removed if no other code uses them. Check by searching the file for each import; remove only those with no remaining usages.

- [ ] **Step 2: Build the Fabric module for 1.21.11**

```
./gradlew :fabric:build -Ptarget_mc_version=1.21.11
```

Expected: `BUILD SUCCESSFUL`. JAR appears at `fabric/1.21.11/build/libs/`.

- [ ] **Step 3: Launch the client and visually verify**

```
./gradlew runClientFabric1_21_11
```

In the client:

1. Create a new world (Creative). The Chrono Dawn dimension portal recipe is documented in `docs/player_guide.md`; alternatively, use `/execute in chronodawn:chronodawn run tp @s 0 80 0` to teleport into the dimension once it has been generated.
2. Fly until you find a Sand or Gravel circle in `chronodawn:chronodawn_plains`. Or use `/locate biome chronodawn:chronodawn_plains` first.
3. Confirm:
   - Temporal Grass Blocks immediately adjacent to a circle look noticeably paler (closer to the sand color) than grass blocks farther away.
   - The transition spans roughly two block rows: row 1 (touching circle) is most shifted, row 2 is mildly shifted, row 3+ is unchanged.
   - Place a sand block on grass somewhere away from any circle. The surrounding grass should retint after the chunk re-bakes.

If the EDGE_TINT looks wrong (too saturated, too pale, wrong hue), do not adjust here — the precise tuning happens in Task 6 after texture sampling. As long as a visible gradient exists, this step passes.

- [ ] **Step 4: Commit**

```
git add fabric/1.21.11/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java
git commit -m "feat(client): use TemporalGrassEdgeTint helper on Fabric 1.21.11"
```

---

### Task 3: Wire helper into remaining Fabric versions

**Files (10):**
- Modify: `fabric/1.20.1/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.1/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.2/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.4/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.5/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.6/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.7/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.8/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.9/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `fabric/1.21.10/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`

The change is identical to Task 2 step 1 in each file. Apply mechanically.

- [ ] **Step 1: Apply the same edit in every file above**

For each file, locate the `ModBlocks.TEMPORAL_GRASS_BLOCK.get()` block-color registration and replace its body with the same one used in Task 2:

```java
        ColorProviderRegistry.BLOCK.register(
            TemporalGrassEdgeTint::provide,
            ModBlocks.TEMPORAL_GRASS_BLOCK.get()
        );
```

Add the import `import com.chronodawn.client.TemporalGrassEdgeTint;`. Remove now-unused imports (`com.chronodawn.blocks.TemporalGrassBlock`, `net.minecraft.client.renderer.BiomeColors`) only if nothing else in the file references them.

- [ ] **Step 2: Build each version sequentially to confirm compilation**

```
./gradlew clean1_20_1 build1_20_1
./gradlew clean1_21_1 build1_21_1
./gradlew clean1_21_2 build1_21_2
./gradlew clean1_21_4 build1_21_4
./gradlew clean1_21_5 build1_21_5
./gradlew clean1_21_6 build1_21_6
./gradlew clean1_21_7 build1_21_7
./gradlew clean1_21_8 build1_21_8
./gradlew clean1_21_9 build1_21_9
./gradlew clean1_21_10 build1_21_10
```

Expected: each command ends with `BUILD SUCCESSFUL`. If a build fails on a specific version, the API drift is almost certainly in `BiomeColors.getAverageGrassColor` or `BlockAndTintGetter` — both have been stable since 1.20.1, so a failure most likely means an import was removed that another piece of the file still uses; restore it.

- [ ] **Step 3: Commit**

```
git add fabric/1.20.1/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.1/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.2/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.4/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.5/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.6/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.7/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.8/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.9/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java \
        fabric/1.21.10/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java
git commit -m "feat(client): use TemporalGrassEdgeTint helper on remaining Fabric versions"
```

---

### Task 4: Wire helper into NeoForge (base + version-specific)

**Files (9):**
- Modify: `neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java` (used by 1.21.1 and 1.21.2)
- Modify: `neoforge/1.21.4/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: `neoforge/1.21.5/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: `neoforge/1.21.6/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: `neoforge/1.21.7/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: `neoforge/1.21.8/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: `neoforge/1.21.9/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: `neoforge/1.21.10/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: `neoforge/1.21.11/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`

NeoForge calls the registration through its event API (`event.register(...)`) rather than `ColorProviderRegistry`, but the lambda body is the same code as the Fabric one. Per the project memory `feedback_entity_attributes_all_versions`, every NeoForge module gets its own copy and all must be updated.

- [ ] **Step 1: Replace the lambda body in each file**

In each NeoForge file above, find the registration inside `onRegisterBlockColors(RegisterColorHandlersEvent.Block event)`:

```java
        event.register(
            (state, world, pos, tintIndex) -> {
                if (tintIndex != 0) return -1;
                if (world != null && pos != null
                        && world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock) {
                    return BiomeColors.getAverageGrassColor(world, pos);
                }
                return 0x5B8AC4; // Chrono Dawn plains grass_color
            },
            ModBlocks.TEMPORAL_GRASS_BLOCK.get()
        );
```

Replace with:

```java
        event.register(
            TemporalGrassEdgeTint::provide,
            ModBlocks.TEMPORAL_GRASS_BLOCK.get()
        );
```

Add the import `import com.chronodawn.client.TemporalGrassEdgeTint;`. Remove now-unused imports (`com.chronodawn.blocks.TemporalGrassBlock`, `net.minecraft.client.renderer.BiomeColors`) only if nothing else in the file references them.

- [ ] **Step 2: Build NeoForge for one version to confirm the API matches**

```
./gradlew clean1_21_11
./gradlew :neoforge:build -Ptarget_mc_version=1.21.11
```

Expected: `BUILD SUCCESSFUL`. If the method reference fails to type-check against `event.register`, the most likely cause is a NeoForge `BlockColor` interface variant — fall back to a 1-line lambda:

```java
        event.register(
            (state, world, pos, tintIndex) -> TemporalGrassEdgeTint.provide(state, world, pos, tintIndex),
            ModBlocks.TEMPORAL_GRASS_BLOCK.get()
        );
```

- [ ] **Step 3: Build the remaining NeoForge versions**

```
./gradlew clean1_21_1 build1_21_1
./gradlew clean1_21_2 build1_21_2
./gradlew clean1_21_4 build1_21_4
./gradlew clean1_21_5 build1_21_5
./gradlew clean1_21_6 build1_21_6
./gradlew clean1_21_7 build1_21_7
./gradlew clean1_21_8 build1_21_8
./gradlew clean1_21_9 build1_21_9
./gradlew clean1_21_10 build1_21_10
```

Expected: each ends with `BUILD SUCCESSFUL`.

- [ ] **Step 4: Spot-check NeoForge runtime**

```
./gradlew runClientNeoForge1_21_11
```

Same visual checklist as Task 2 step 3. The blend should look identical across loaders.

- [ ] **Step 5: Commit**

```
git add neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.4/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.5/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.6/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.7/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.8/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.9/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.10/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java \
        neoforge/1.21.11/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java
git commit -m "feat(client): use TemporalGrassEdgeTint helper on NeoForge (all versions)"
```

---

### Task 5: Sample Temporal Sand texture and refine `EDGE_TINT`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`

The `EDGE_TINT = 0x9CB6CC` placeholder needs to be replaced with a value derived from the Temporal Sand texture so the gradient lands cleanly on the actual baked color rather than a guessed midpoint.

- [ ] **Step 1: Compute the average pixel color of `temporal_sand.png`**

Run from the worktree root:

```
python3 - <<'PY'
from PIL import Image
img = Image.open("common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sand.png").convert("RGBA")
pixels = [(r,g,b) for r,g,b,a in img.getdata() if a > 0]
n = len(pixels)
ar = sum(p[0] for p in pixels) // n
ag = sum(p[1] for p in pixels) // n
ab = sum(p[2] for p in pixels) // n
print(f"avg = 0x{ar:02X}{ag:02X}{ab:02X}  ({n} opaque pixels)")
PY
```

Record the output. If `PIL` is not installed: `pip3 install --user pillow` first.

- [ ] **Step 2: Pick the new `EDGE_TINT`**

Apply two adjustments to the raw average:

1. The grass tint should not jump fully to the sand color; bias 30% toward the grass tint to keep some "grass-ness" at the closest edge:
   `EDGE_TINT = lerpRgb(sand_avg, 0x5B8AC4, 0.30)`
2. Round each channel to the nearest 4 (`& 0xFC`) to avoid drawing the eye to a precise digit.

Example: if sand_avg comes out as `0xB8C8D0`, then biased = `0x9CB6CC` (close to current placeholder); rounded = `0x9CB4CC`. Compute the actual value from your sampled average.

Update the constant in `TemporalGrassEdgeTint.java`:

```java
    /**
     * Color the tint shifts toward at the edge of a sand/gravel disk.
     * Derived from the average pixel color of {@code temporal_sand.png}
     * (= 0x__SAMPLED__) biased 30% toward the grass tint to retain a hint of
     * green at the boundary. Rounded to multiples of 4 per channel.
     */
    public static final int EDGE_TINT = 0x________; // computed in Task 5
```

The comment must record both the sampled average and the bias factor — future maintainers should not need to re-derive it.

- [ ] **Step 3: Update the unit test that pins EDGE_TINT**

Inside `TemporalGrassEdgeTintTest.lerpRgb_grassToEdgeSample`, the inputs reference the constant indirectly. The test still passes because it computes the expected value from `lerpRgb` itself — re-run to confirm:

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.TemporalGrassEdgeTintTest"
```

Expected: 4 tests pass.

- [ ] **Step 4: Re-run the client and visually verify**

```
./gradlew runClientFabric1_21_11
```

Confirm the gradient now lands cleanly on the sand color at the edge. If it still looks off, iterate: adjust the bias factor (Step 2) and rebuild — do not just guess hex values.

- [ ] **Step 5: Commit**

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git commit -m "feat(client): tune TemporalGrassEdgeTint EDGE_TINT to sand-sampled average"
```

---

### Task 6: Run full verification and finalize

**Files:** none

Per the project memory `feedback_check_all_before_phase_completion`, single-version builds are not sufficient for a multi-version change. `checkAll` must pass before this work is merged.

- [ ] **Step 1: Run the full verification pipeline**

```
./gradlew checkAll
```

This runs, in order: cleanAll → validateResources → validateTranslations → buildAll → testAll → gameTestAll. Expected: `BUILD SUCCESSFUL` end-to-end.

- [ ] **Step 2: Triage any failures**

- A `testAll` failure in `TemporalGrassEdgeTintTest`: investigate the math — most likely a rounding edge case in `lerpRgb`.
- A `validateResources` / `validateTranslations` failure: not caused by this plan (no JSON or lang changes); investigate the unrelated regression.
- A `buildAll` failure on a specific version: missing import or stale cache. Re-run `./gradlew clean<version> build<version>` for that version and inspect the compiler error directly.
- A `gameTestAll` failure: this plan does not add gametests; check whether an unrelated test broke and fix or revert as appropriate.

Do not mark this task complete until `checkAll` passes cleanly.

- [ ] **Step 3: Final visual confirmation across loaders**

Run each of:

```
./gradlew runClientFabric1_21_11
./gradlew runClientNeoForge1_21_11
```

Both clients should render the gradient identically. If they differ, the most likely cause is one loader's lambda still pointing at the old code path — search for `BiomeColors.getAverageGrassColor` across the worktree to find any remaining call site:

```
grep -rln "BiomeColors.getAverageGrassColor" fabric neoforge
```

Expected output: no matches (the only remaining caller is `TemporalGrassEdgeTint.provide`).

- [ ] **Step 4: No additional commit if Step 1–3 pass**

If `checkAll` was already green and no further code changes were required, no commit. If a fix was needed, commit it with a descriptive message at the time it was made (do not bundle multiple fixes into one final commit).

---

## Out of Scope / Future Work

These items are deferred per the spec and should not be done in this plan unless visual testing reveals they are required:

- ±1 Y-axis scan for terrain steps.
- Tag-based edge trigger list.
- Distance-weighted blend (Approach C from brainstorming).
