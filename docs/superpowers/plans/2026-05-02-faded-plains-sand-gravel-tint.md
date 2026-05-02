# Faded Plains Sand/Gravel Tint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply a Faded Plains–localized multiplicative tint to Temporal Sand and Temporal Gravel, and re-tune the existing grass↔sand edge blend for the new biome, without affecting other biomes.

**Architecture:** Modify a single shared-module class (`TemporalGrassEdgeTint.java`). Add Faded Plains–specific constants paralleling the existing prairies-tuned constants. Branch the two color provider entry points on a biome check that uses string comparison to dodge the 1.21.11 `ResourceLocation` → `Identifier` rename.

**Tech Stack:** Java 21, Minecraft Mojang mappings, Architectury (color providers register from per-loader client init files; the math lives in `common/shared/`).

**Spec:** `docs/superpowers/specs/2026-05-02-faded-plains-sand-gravel-tint-design.md`

---

## File Structure

**Modified files:**
- `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java` — add 4 constants, 3 helpers, branch `provide` and `provideForSandGravel` on biome.
- `common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java` — add tests for the new `multiplyRgb` helper.

**No new files.** **No per-version Java spread.** Color provider registration sites in `fabric/<ver>/.../ChronoDawnClientFabric.java` and `neoforge/<ver>/.../ChronoDawnClientNeoForge.java` use method references (`TemporalGrassEdgeTint::provide`, `::provideForSandGravel`) and need no changes — the function signatures stay the same.

---

## Background: Current Code Shape

The class currently has:
- `provide(BlockState, BlockAndTintGetter, BlockPos, int) -> int` — grass-side color provider.
- `provideForSandGravel(BlockState, BlockAndTintGetter, BlockPos, int) -> int` — sand-side color provider.
- `blend(BlockGetter, BlockPos, int) -> int` — package-private; called from `provide`.
- `isEdgeTrigger(BlockState) -> boolean`, `isWater(BlockState) -> boolean`, `lerpRgb(int, int, float) -> int`.
- Constants: `DEFAULT_FALLBACK = 0x5B8AC4`, `EDGE_TINT = 0x90BBE7`, `SAND_NEIGHBOR_TINT = 0xE8EFF5`, `WET_TINT = 0x3A597F`, `RADIUS = 3`, `WATER_RADIUS = 2`.

Existing tests cover only `lerpRgb`. Biome-conditional logic and neighbor scan are validated by visual smoke test (per the file's class-doc note).

---

## Task 1: Add `multiplyRgb` helper (TDD)

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`
- Modify: `common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java`

`multiplyRgb` will be used in Task 6 to layer the per-block biome tint with the d=1 grass-neighbor tint inside Faded Plains. Add it now with a unit test so the math is verified before any caller exists.

- [ ] **Step 1: Write failing tests for `multiplyRgb`**

Add to `TemporalGrassEdgeTintTest.java` (after the existing `lerpRgb_grassToEdgeSample` test):

```java
@Test
void multiplyRgb_byWhite_isIdentity() {
    assertEquals(0x123456, TemporalGrassEdgeTint.multiplyRgb(0x123456, 0xFFFFFF));
}

@Test
void multiplyRgb_byBlack_isZero() {
    assertEquals(0x000000, TemporalGrassEdgeTint.multiplyRgb(0x123456, 0x000000));
}

@Test
void multiplyRgb_byHalf_halvesEachChannel() {
    // 0x80 / 0xFF ~= 0.502; per-channel: round(0x40 * 128 / 255) = 0x20, etc.
    int actual = TemporalGrassEdgeTint.multiplyRgb(0x408060, 0x808080);
    int r = (0x40 * 0x80) / 0xFF; // 32
    int g = (0x80 * 0x80) / 0xFF; // 64
    int b = (0x60 * 0x80) / 0xFF; // 48
    int expected = (r << 16) | (g << 8) | b;
    assertEquals(expected, actual);
}
```

- [ ] **Step 2: Run tests to verify they fail**

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests com.chronodawn.unit.TemporalGrassEdgeTintTest
```

Expected: compilation failure ("symbol not found: multiplyRgb").

- [ ] **Step 3: Add `multiplyRgb` to `TemporalGrassEdgeTint.java`**

Insert just below `lerpRgb` (around line 207, before the closing brace):

```java
/** Per-channel multiplicative blend: out = a * b / 255 per channel. */
public static int multiplyRgb(int a, int b) {
    int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
    int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
    int r = (ar * br) / 0xFF;
    int g = (ag * bg) / 0xFF;
    int bl = (ab * bb) / 0xFF;
    return (r << 16) | (g << 8) | bl;
}
```

- [ ] **Step 4: Run tests to verify they pass**

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests com.chronodawn.unit.TemporalGrassEdgeTintTest
```

Expected: PASS for all 7 tests (4 existing `lerpRgb` + 3 new `multiplyRgb`).

- [ ] **Step 5: Stage but do not commit yet** (commit at end of Task 2 after pure refactor lands together — keeps single-responsibility commits coherent)

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git add common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java
git status
```

Expected: 2 files staged.

---

## Task 2: Extract `scanForGrassNeighbor` from `provideForSandGravel` (pure refactor)

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`

Pure refactor — no behavior change. The existing 3×3 same-Y loop in `provideForSandGravel` (current lines 126–137) becomes a private helper so Task 6 can reuse it. The existing tests still pass (no test calls `scanForGrassNeighbor` directly).

- [ ] **Step 1: Add the new helper above `provideForSandGravel`**

Insert immediately before the existing `public static int provideForSandGravel(...)` declaration:

```java
/**
 * Returns true if any block at Chebyshev distance 1 (same Y) from {@code pos}
 * is a Temporal Grass Block.
 */
static boolean scanForGrassNeighbor(BlockAndTintGetter world, BlockPos pos) {
    BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
    for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) continue;
            cur.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
            if (world.getBlockState(cur).getBlock() instanceof TemporalGrassBlock) {
                return true;
            }
        }
    }
    return false;
}
```

- [ ] **Step 2: Replace the inline loop in `provideForSandGravel` with a call**

Replace the entire body of `provideForSandGravel` (currently lines 122–137) with:

```java
public static int provideForSandGravel(BlockState state, @Nullable BlockAndTintGetter world,
                                       @Nullable BlockPos pos, int tintIndex) {
    if (tintIndex != 0) return -1;
    if (world == null || pos == null) return 0xFFFFFF;
    return scanForGrassNeighbor(world, pos) ? SAND_NEIGHBOR_TINT : 0xFFFFFF;
}
```

- [ ] **Step 3: Run tests to confirm no regression**

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests com.chronodawn.unit.TemporalGrassEdgeTintTest
```

Expected: PASS for all 7 tests.

- [ ] **Step 4: Per-version compile check (oldest version — largest API delta)**

```
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit (Task 1 + Task 2 together — TDD helper + pure refactor)**

Pause and ask the user "1. Commit / 2. Continue without commit". Per CLAUDE.md: "Require user instruction before: ... git commit ...". On approval, run:

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git add common/shared/src/test/java/com/chronodawn/unit/TemporalGrassEdgeTintTest.java
git commit -m "refactor(client): add multiplyRgb and extract scanForGrassNeighbor

Prepares TemporalGrassEdgeTint for Faded Plains biome-aware tinting.
No behavior change yet."
```

---

## Task 3: Add `isInFadedPlains` helper

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`

No callers yet. The helper exists only so Task 5 and Task 6 can use it. The test pattern stays "validated by visual smoke test" — biome detection cannot be unit-tested without a Minecraft runtime.

- [ ] **Step 1: Add the constant and helper near the top of the class**

Insert immediately after `private TemporalGrassEdgeTint() {}` (around line 91):

```java
/** Resource location string of the Faded Plains biome. Compared via toString()
 *  to avoid naming the class (renamed ResourceLocation → Identifier in 1.21.11). */
private static final String FADED_PLAINS_ID = "chronodawn:chronodawn_faded_plains";

/**
 * Returns true if the biome at {@code pos} is {@code chronodawn:chronodawn_faded_plains}.
 *
 * <p>Implementation note: never names the {@code ResourceLocation} / {@code Identifier}
 * class (renamed in 1.21.11). Compares the location string directly.
 */
static boolean isInFadedPlains(BlockAndTintGetter world, BlockPos pos) {
    return world.getBiome(pos).unwrapKey()
        .map(key -> key.location().toString().equals(FADED_PLAINS_ID))
        .orElse(false);
}
```

- [ ] **Step 2: Compile check on the renamed-class era (1.21.11)**

```
./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11
```

Expected: BUILD SUCCESSFUL. If compilation fails on the lambda inferring the renamed class, fall back to:

```java
static boolean isInFadedPlains(BlockAndTintGetter world, BlockPos pos) {
    var key = world.getBiome(pos).unwrapKey();
    return key.isPresent() && key.get().location().toString().equals(FADED_PLAINS_ID);
}
```

- [ ] **Step 3: Compile check on the oldest version (1.20.1)**

```
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Stage (commit deferred — combine with Task 4)**

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git status
```

---

## Task 4: Refactor `blend` to accept `edgeTint` parameter

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`

Pure refactor. `provide` will start passing `EDGE_TINT` as the new parameter — same value as before, so no behavior change. Task 5 then changes `provide` to pass either `EDGE_TINT` or the new Faded Plains–specific value.

- [ ] **Step 1: Update `blend` signature and body**

Replace the existing `blend` method (current lines 145–186) with:

```java
/**
 * Pure blend: scan the {@code RADIUS}-block Chebyshev neighborhood and
 * apply up to two tint layers — one for sand/gravel edge proximity
 * (toward {@code edgeTint}) and one for water proximity
 * (toward {@link #WET_TINT}). Both scans share the same loop.
 */
static int blend(BlockGetter world, BlockPos pos, int baseTint, int edgeTint) {
    int minDistEdge = RADIUS + 1;
    int minDistWater = WATER_RADIUS + 1;
    BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
    outer:
    for (int dx = -RADIUS; dx <= RADIUS; dx++) {
        for (int dz = -RADIUS; dz <= RADIUS; dz++) {
            if (dx == 0 && dz == 0) continue;
            int d = Math.max(Math.abs(dx), Math.abs(dz));
            boolean canImproveEdge = d < minDistEdge;
            boolean canImproveWater = d <= WATER_RADIUS && d < minDistWater;
            if (!canImproveEdge && !canImproveWater) continue;
            cur.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
            BlockState neighbor = world.getBlockState(cur);
            if (canImproveEdge && isEdgeTrigger(neighbor)) {
                minDistEdge = d;
            }
            if (canImproveWater) {
                if (isWater(neighbor)) {
                    minDistWater = d;
                } else {
                    cur.setY(pos.getY() - 1);
                    if (isWater(world.getBlockState(cur))) {
                        minDistWater = d;
                    }
                    cur.setY(pos.getY());
                }
            }
            if (minDistEdge == 1 && minDistWater == 1) break outer;
        }
    }
    int result = baseTint;
    if (minDistEdge <= RADIUS) {
        float t = (RADIUS + 1 - minDistEdge) / (float) (RADIUS + 1);
        result = lerpRgb(result, edgeTint, t);
    }
    if (minDistWater <= WATER_RADIUS) {
        float t = (WATER_RADIUS + 1 - minDistWater) / (float) (WATER_RADIUS + 1);
        result = lerpRgb(result, WET_TINT, t);
    }
    return result;
}
```

Two changes only: `int edgeTint` parameter added, `EDGE_TINT` constant in the lerp call replaced with the parameter `edgeTint`.

- [ ] **Step 2: Update `provide` to pass `EDGE_TINT` (no behavior change yet)**

Replace the existing `provide` method (current lines 98–107) with:

```java
public static int provide(BlockState state, @Nullable BlockAndTintGetter world,
                          @Nullable BlockPos pos, int tintIndex) {
    if (tintIndex != 0) return -1;
    if (world == null || pos == null
            || !(world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock)) {
        return DEFAULT_FALLBACK;
    }
    int base = BiomeColors.getAverageGrassColor(world, pos);
    return blend(world, pos, base, EDGE_TINT);
}
```

- [ ] **Step 3: Run tests + per-version compile (regression check)**

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests com.chronodawn.unit.TemporalGrassEdgeTintTest
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
```

Expected: PASS / BUILD SUCCESSFUL. Behavior is byte-identical to before.

- [ ] **Step 4: Commit (Task 3 + Task 4 — biome helper + blend signature change)**

Pause and ask the user "1. Commit / 2. Continue without commit". On approval:

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git commit -m "refactor(client): parameterize blend edge tint and add isInFadedPlains

Threads edgeTint through TemporalGrassEdgeTint.blend so it can be
biome-conditional. isInFadedPlains uses string-comparison biome lookup
to remain compatible with the 1.21.11 ResourceLocation -> Identifier rename.
No behavior change."
```

---

## Task 5: Add Faded Plains constants and wire into `provide` (grass-side)

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`

First behavior change. Inside Faded Plains, the grass color provider lerps toward `EDGE_TINT_FADED` (warm) instead of `EDGE_TINT` (blue) when sand/gravel is nearby.

- [ ] **Step 1: Add the three new tint constants**

Insert immediately after the existing `SAND_NEIGHBOR_TINT` constant (around line 63), before the `RADIUS` constant:

```java
/**
 * Multiplicative tint applied to Temporal Sand / Temporal Gravel anywhere inside
 * Faded Plains (chronodawn:chronodawn_faded_plains). Pulls the blue-leaning
 * texture (~0x90BBE7) toward a warm faded color. Effective rendered color
 * approx 0x90A460 (warm olive).
 *
 * Multiplicative tint cannot raise channel values, so a "true" sandstone
 * yellow is unreachable without a texture rewrite — see design spec §7.
 *
 * Tuning history:
 *   - 0xFFE06A (current): initial estimate, derived to push blue down to ~0x60
 */
public static final int BIOME_TINT_FADED = 0xFFE06A;

/**
 * Color the grass-side blend lerps toward when scanning detects an adjacent
 * sand/gravel block in Faded Plains. Mirrors prairies, where {@link #EDGE_TINT}
 * is the raw sand average — here it is the effective tinted-sand color (the
 * result of applying {@link #BIOME_TINT_FADED} to the sand texture average).
 *
 * Tuning history:
 *   - 0x90A460 (current): initial estimate, matches BIOME_TINT_FADED's effective output
 */
public static final int EDGE_TINT_FADED = 0x90A460;

/**
 * Multiplicative tint applied to Temporal Sand / Temporal Gravel at Chebyshev
 * distance 1 from a Temporal Grass Block, when in Faded Plains. Initial value
 * 0xFFFFFF (no further pull) on the assumption that warm sand and warm grass
 * are already close enough at the boundary. Tune to a small darken
 * (e.g., 0xF8F8F0) only if visual testing reveals a step at d=1.
 *
 * Tuning history:
 *   - 0xFFFFFF (current): initial estimate, no additional darkening
 */
public static final int SAND_NEIGHBOR_TINT_FADED = 0xFFFFFF;
```

- [ ] **Step 2: Update `provide` to choose `edgeTint` based on biome**

Replace the existing `provide` method (the version from Task 4) with:

```java
public static int provide(BlockState state, @Nullable BlockAndTintGetter world,
                          @Nullable BlockPos pos, int tintIndex) {
    if (tintIndex != 0) return -1;
    if (world == null || pos == null
            || !(world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock)) {
        return DEFAULT_FALLBACK;
    }
    int base = BiomeColors.getAverageGrassColor(world, pos);
    int edgeTint = isInFadedPlains(world, pos) ? EDGE_TINT_FADED : EDGE_TINT;
    return blend(world, pos, base, edgeTint);
}
```

- [ ] **Step 3: Run tests + per-version compile**

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests com.chronodawn.unit.TemporalGrassEdgeTintTest
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5
```

Expected: PASS / BUILD SUCCESSFUL all three.

- [ ] **Step 4: Stage (commit deferred — combine with Task 6)**

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git status
```

---

## Task 6: Wire Faded Plains tint into `provideForSandGravel` (sand-side)

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`

Second behavior change. Inside Faded Plains, Temporal Sand and Temporal Gravel always get `BIOME_TINT_FADED` applied; if also at d=1 from a Temporal Grass Block, layer `SAND_NEIGHBOR_TINT_FADED` on top via `multiplyRgb`.

- [ ] **Step 1: Update `provideForSandGravel`**

Replace the existing body (the version from Task 2) with:

```java
public static int provideForSandGravel(BlockState state, @Nullable BlockAndTintGetter world,
                                       @Nullable BlockPos pos, int tintIndex) {
    if (tintIndex != 0) return -1;
    if (world == null || pos == null) return 0xFFFFFF;
    boolean nearGrass = scanForGrassNeighbor(world, pos);
    if (isInFadedPlains(world, pos)) {
        int result = BIOME_TINT_FADED;
        if (nearGrass) result = multiplyRgb(result, SAND_NEIGHBOR_TINT_FADED);
        return result;
    }
    return nearGrass ? SAND_NEIGHBOR_TINT : 0xFFFFFF;
}
```

- [ ] **Step 2: Update class-level Javadoc to mention biome-conditional behavior**

Find the class-level Javadoc block (currently lines 14–30, starting with `/**` above `public final class TemporalGrassEdgeTint`). Replace it with:

```java
/**
 * Computes the per-block tint for {@link TemporalGrassBlock}, blending the
 * biome grass color toward a pale "edge" color when the block is near a
 * Temporal Sand / Temporal Gravel disk (or vanilla sand/gravel placed by a
 * player), and toward a dark teal "wet" color when near water.
 *
 * Edge blend: within {@link #RADIUS} blocks (Chebyshev, same Y) of an edge
 * trigger, the grass tint lerps toward {@link #EDGE_TINT} — or
 * {@link #EDGE_TINT_FADED} if the grass block is inside the Faded Plains
 * biome.
 *
 * Water blend: within {@link #WATER_RADIUS} blocks (Chebyshev, same Y and
 * one Y below) of water, the grass tint lerps toward {@link #WET_TINT}.
 *
 * Both effects stack — a block near both sand and water receives both blends.
 *
 * Sand/gravel side: outside Faded Plains, applies a subtle pull
 * ({@link #SAND_NEIGHBOR_TINT}) toward grass at d=1; inside Faded Plains,
 * applies {@link #BIOME_TINT_FADED} unconditionally and layers
 * {@link #SAND_NEIGHBOR_TINT_FADED} at d=1.
 *
 * Pure function — no caching, recomputed on every chunk mesh bake. Cost is
 * comparable to vanilla {@link BiomeColors#getAverageGrassColor}.
 */
```

- [ ] **Step 3: Run tests + per-version compile**

```
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests com.chronodawn.unit.TemporalGrassEdgeTintTest
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5
```

Expected: PASS / BUILD SUCCESSFUL all three.

- [ ] **Step 4: Commit (Task 5 + Task 6 — both biome behaviors land together)**

Pause and ask the user "1. Commit / 2. Continue without commit". On approval:

```
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git commit -m "feat(client): tint Temporal Sand/Gravel inside Faded Plains

Adds Faded Plains-specific tint constants paralleling the existing
prairies-tuned values. Inside chronodawn:chronodawn_faded_plains the
grass-side edge blend pulls toward warm olive, and Temporal Sand/Gravel
is multiplicatively tinted to a faded warm look. Other biomes are
byte-for-byte unchanged."
```

---

## Task 7: Per-version full build verification

**Files:** none modified.

Confirm the change compiles and tests pass cleanly on the three eras with the largest API differences (oldest, mid, latest).

- [ ] **Step 1: 1.21.11 (latest, renamed-class era)**

```
./gradlew build1_21_11
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: 1.21.5 (mid)**

```
./gradlew build1_21_5
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 1.20.1 (oldest, largest API delta)**

```
./gradlew build1_20_1
```

Expected: BUILD SUCCESSFUL.

If any version fails, do not proceed. Fix the failure (most likely path: the `isInFadedPlains` lambda hits a generics inference issue on the renamed-class era — apply the `var` fallback shown in Task 3 Step 2). Re-run all three before continuing.

- [ ] **Step 4: No commit needed for Task 7** (nothing changed). Proceed to manual visual test.

---

## Task 8: Manual visual smoke test

**Files:** none modified.

This is the verification of record (no automated test can validate rendering). Run on both 1.21.11 (latest, renamed-class) and 1.20.1 (oldest API).

- [ ] **Step 1: Launch 1.21.11 Fabric client**

```
./gradlew runClientFabric1_21_11
```

In game:
- `/gamemode creative`
- Create a new world in the Chrono Dawn dimension (or use `/execute in chronodawn:chronodawn run tp @s 0 200 0`).
- `/locate biome chronodawn:chronodawn_faded_plains`
- `/tp` to the located coordinates.

- [ ] **Step 2: Verify Faded Plains tint behavior**

Visually confirm:
- Temporal Sand and Temporal Gravel surfaces appear warm-olive (not blue).
- The boundary between Temporal Sand/Gravel and Temporal Grass Block transitions smoothly — no sharp color step at d=1.
- Inventory icons of Temporal Sand and Temporal Gravel are unchanged (no biome context → fall back to the standard 0xFFFFFF "no tint" path).

If the d=1 boundary shows a visible step, edit `SAND_NEIGHBOR_TINT_FADED` (e.g., to `0xF8F8F0` for a slight darken) and rebuild — append the rationale to the constant's Tuning history comment.

- [ ] **Step 3: Verify prairies regression**

In the same client session:
- `/locate biome chronodawn:chronodawn_prairies`
- `/tp`

Visually confirm:
- Temporal Sand and Temporal Gravel still appear with the original blue tint (unchanged).
- The grass↔sand edge gradient at sand/gravel disk borders looks identical to before this change.

- [ ] **Step 4: Repeat smoke test on 1.20.1**

```
./gradlew runClientFabric1_20_1
```

Repeat Steps 1–3. The 1.20.1 era has the largest API delta so it catches any subtle render-path differences not visible in 1.21.11.

- [ ] **Step 5: If any tuning was applied during smoke test, commit**

Pause and ask the user "1. Commit tuning / 2. Skip if no changes". On approval:

```
git diff   # confirm only constant values + Tuning history comments changed
git add common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java
git commit -m "tune(client): adjust Faded Plains tint constants from visual review

Updated values + appended rationale to Tuning history comments."
```

If no tuning needed, no commit — proceed.

---

## Task 9: Full pre-merge verification

**Files:** none modified.

- [ ] **Step 1: Run `checkAll`**

```
./gradlew checkAll
```

This runs cleanAll → validateResources → validateTranslations → buildAll → testAll → gameTestAll across all 11 versions. Per `feedback_buildall_gametestall_wrapper_unreliable`, watch for spurious FAILED reports — if any version reports failed, re-run that single version standalone (`./gradlew build1_X_Y`) to confirm.

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: No commit for Task 9** (nothing changed). Verification complete.

---

## Risk Watchlist

- **`isInFadedPlains` lambda type inference**: the lambda `key -> key.location().toString()...` may fail to compile on a specific Minecraft version where Mojang changed the `Holder` / `ResourceKey` generic signature. Mitigation in Task 3 Step 2: switch to `var key = ...; if (key.isPresent()) ...`. If even that fails, move biome detection to per-version code (last resort — would expand scope to 11 modules).
- **Per-version test wrapper unreliability** (`feedback_buildall_gametestall_wrapper_unreliable`): if Task 9 reports a per-version failure, re-run that version's `build1_X_Y` standalone before declaring it broken.
- **No automated regression for biome-conditional logic**: visual smoke test on prairies (Task 8 Step 3) is the only protection against breaking the existing tuned edge gradient. Do not skip it.
- **Multiplicative tint ceiling** (per spec §7): if the warm-olive look is judged "not warm enough" during smoke test, the fix is to lower more channels of `BIOME_TINT_FADED`, not to raise (impossible). True sandstone yellow requires a texture variant — out of scope for this plan.
