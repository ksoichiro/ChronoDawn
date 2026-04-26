# Temporal Plant Biome Tint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tint Temporal Tall Grass and Temporal Fern blocks with a 50/50 blend of their baked-in teal baseline and the per-position biome grass color, so they pick up local biome character (e.g. dark_forest) while preserving their Chrono Dawn appearance in plains.

**Architecture:** A new shared, loader-agnostic utility class `TemporalPlantColorProvider` in `common/shared/` exposes a pure-math `blockTint(world, pos, tintIndex)` and a constant `itemTint(tintIndex)`. Each per-version Fabric and NeoForge client init file calls into this utility from existing `registerBlockColors` / `onRegisterBlockColors` and (where applicable) item color registration sites. A single JUnit test in `common/1.21.2` exercises the formula.

**Tech Stack:** Java 21, Architectury (multi-loader), Fabric `ColorProviderRegistry`, NeoForge `RegisterColorHandlersEvent`, JUnit 5 (`junit-jupiter-api:5.10.0`), Mojang mappings, `BiomeColors.getAverageGrassColor`.

**Spec:** `docs/superpowers/specs/2026-04-26-temporal-plant-biome-tint-design.md`

---

## File Structure

**New files:**
- `common/shared/src/main/java/com/chronodawn/client/TemporalPlantColorProvider.java` — pure utility, block + item tint helpers, package-private `blendChannel` for testing
- `common/1.21.2/src/test/java/com/chronodawn/client/TemporalPlantColorProviderTest.java` — JUnit 5 unit test for the formula

**Modified files (block color registration):**

Fabric (11 files), inside existing `registerBlockColors()`:
- `fabric/1.20.1/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.1/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.2/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.4/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.5/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.6/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.7/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.8/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.9/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.10/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- `fabric/1.21.11/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`

NeoForge (9 files), inside existing `onRegisterBlockColors`:
- `neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java` (also has item color event, see Task 4)
- `neoforge/1.21.4/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- `neoforge/1.21.5/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- `neoforge/1.21.6/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- `neoforge/1.21.7/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- `neoforge/1.21.8/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- `neoforge/1.21.9/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- `neoforge/1.21.10/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- `neoforge/1.21.11/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`

Untouched: textures, blockstates, models, datapacks, ModBlocks, ModItems.

---

## Task 1: Build `TemporalPlantColorProvider` (TDD)

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/client/TemporalPlantColorProvider.java`
- Test: `common/1.21.2/src/test/java/com/chronodawn/client/TemporalPlantColorProviderTest.java`

The shared utility lives in `common/shared/` (the version-agnostic source root), so the same code is used for every Minecraft version. The test runs only against `common/1.21.2`'s test classpath; that suffices because the source is identical.

- [ ] **Step 1: Write the failing test**

Create `common/1.21.2/src/test/java/com/chronodawn/client/TemporalPlantColorProviderTest.java`:

```java
package com.chronodawn.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TemporalPlantColorProvider}.
 *
 * The class is loader-agnostic and lives under {@code common/shared/}, so testing
 * it from a single per-version test module covers all 11 Minecraft versions.
 */
class TemporalPlantColorProviderTest {

    @Test
    void itemTint_returnsNoTintSentinel_forAnyTintIndex() {
        assertEquals(-1, TemporalPlantColorProvider.itemTint(0));
        assertEquals(-1, TemporalPlantColorProvider.itemTint(1));
        assertEquals(-1, TemporalPlantColorProvider.itemTint(2));
    }

    @Test
    void blockTint_nonZeroTintIndex_returnsMinusOne() {
        assertEquals(-1, TemporalPlantColorProvider.blockTint(null, null, 1));
    }

    @Test
    void blockTint_nullWorld_returnsWhite() {
        assertEquals(0xFFFFFF, TemporalPlantColorProvider.blockTint(null, null, 0));
    }

    @Test
    void blendChannel_baselineMatch_returns255() {
        // When biome channel == baseline channel, ratio = 1.0, scaled = 255,
        // tint = 255 + 0.5 * (255 - 255) = 255.
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0x5B, 0x5B));
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0xC4, 0xC4));
    }

    @Test
    void blendChannel_biomeBelowBaseline_dampensTowardBiome() {
        // dark_forest R = 0x44 over plains R = 0x5B:
        // ratio = 0x44 / 0x5B = 0.7472...
        // scaled = 190.5
        // tint = 255 + 0.5 * (190.5 - 255) = 222.75 -> 223 (rounded)
        int tint = TemporalPlantColorProvider.blendChannel(0x44, 0x5B);
        assertTrue(tint >= 222 && tint <= 224,
            "Expected tint near 223 for biome=0x44 base=0x5B, got " + tint);
    }

    @Test
    void blendChannel_biomeAboveBaseline_clampsAt255() {
        // ratio > 1 means scaled is clamped to 255 -> tint = 255.
        // (Highlights stay neutral; we don't brighten the texture.)
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0xFF, 0x80));
    }

    @Test
    void blendChannel_zeroBaseline_fallsBackToOne() {
        // Defensive guard: baseC == 0 makes ratio default to 1f, so tint = 255.
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0x44, 0));
    }

    @Test
    void blendChannel_zeroBiome_neverNegative() {
        // ratio = 0, scaled = 0, tint = 255 + 0.5 * (-255) = 127.5 -> 128.
        int tint = TemporalPlantColorProvider.blendChannel(0, 0xFF);
        assertTrue(tint >= 127 && tint <= 128,
            "Expected tint near 128 for biome=0 base=0xFF, got " + tint);
    }
}
```

- [ ] **Step 2: Run the test and verify it fails**

```
./gradlew :common-1.21.2:test --tests com.chronodawn.client.TemporalPlantColorProviderTest -Ptarget_mc_version=1.21.2
```

Expected: compilation failure — `TemporalPlantColorProvider` does not exist.

- [ ] **Step 3: Write the minimal implementation**

Create `common/shared/src/main/java/com/chronodawn/client/TemporalPlantColorProvider.java`:

```java
package com.chronodawn.client;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Block / item tint provider for Temporal Tall Grass and Temporal Fern.
 *
 * <p>The plant textures are baked with a deep teal that matches Chrono Dawn's
 * plains {@code grass_color} ({@value #BASELINE}). Without a tint provider, the
 * plants stay teal in every biome, which clashes with biomes whose grass color
 * differs (e.g. {@code chronodawn_dark_forest} = {@code 0x44520C}).
 *
 * <p>This provider returns a tint that, when multiplied with the texel teal,
 * lands the rendered colour halfway between the baseline and the biome's
 * grass color. The math collapses to {@code 0xFFFFFF} (no tint) when the
 * biome's grass color equals the baseline, so plains appearance is preserved
 * exactly.
 *
 * <p>Inventory and first-person held-item rendering pass {@code world == null},
 * which falls through to {@code 0xFFFFFF} so the icon shows the raw texture.
 */
public final class TemporalPlantColorProvider {

    /** Chrono Dawn plains {@code grass_color} — the texture's intended baseline tint. */
    public static final int BASELINE = 0x5B8AC4;

    /** Blend factor between baseline and biome color. {@code 0.5} = even split. */
    private static final float BLEND = 0.5f;

    private TemporalPlantColorProvider() {}

    /** Block-color callback. Returns {@code -1} for {@code tintIndex != 0}. */
    public static int blockTint(BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        if (world == null || pos == null) return 0xFFFFFF;

        int biome = BiomeColors.getAverageGrassColor(world, pos);
        int rTint = blendChannel((biome >> 16) & 0xFF, (BASELINE >> 16) & 0xFF);
        int gTint = blendChannel((biome >>  8) & 0xFF, (BASELINE >>  8) & 0xFF);
        int bTint = blendChannel( biome        & 0xFF,  BASELINE        & 0xFF);
        return (rTint << 16) | (gTint << 8) | bTint;
    }

    /**
     * Item-color callback. Always returns {@code -1} so the inventory icon
     * renders the texture's raw teal — no in-world biome blending is applied
     * to held items. (The ItemColor contract uses {@code -1} as the
     * "no tint" sentinel; {@code tintIndex} is unused because Temporal plants
     * declare only {@code tintindex: 0}.)
     */
    public static int itemTint(int tintIndex) {
        return -1;
    }

    /**
     * Per-channel blend used by {@link #blockTint}. Package-private to allow
     * direct unit testing without reflection.
     *
     * <p>Computes {@code lerp(255, min(biomeC * 255 / baseC, 255), BLEND)} and
     * clamps to {@code [0, 255]}. When {@code baseC == 0} the ratio defaults to
     * {@code 1f} (defensive — the live baseline {@code 0x5B8AC4} has no zero
     * channel, but the constant could change).
     */
    static int blendChannel(int biomeC, int baseC) {
        float ratio = baseC == 0 ? 1f : (float) biomeC / (float) baseC;
        float scaled = Math.min(ratio * 255f, 255f);
        float tint = 255f + BLEND * (scaled - 255f);
        return Math.max(0, Math.min(255, Math.round(tint)));
    }
}
```

- [ ] **Step 4: Run the test and verify it passes**

```
./gradlew :common-1.21.2:test --tests com.chronodawn.client.TemporalPlantColorProviderTest -Ptarget_mc_version=1.21.2
```

Expected: 8 tests pass (one of them asserts the no-tint sentinel for three tintIndex values).

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/client/TemporalPlantColorProvider.java \
        common/1.21.2/src/test/java/com/chronodawn/client/TemporalPlantColorProviderTest.java
git commit -m "feat(plants): add TemporalPlantColorProvider for biome-blended tint"
```

---

## Task 2: Wire Fabric block + item color registration (all 11 versions)

**Files:** all 11 `fabric/<ver>/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java` listed above.

Each file already has a `registerBlockColors()` method that registers `ModBlocks.TEMPORAL_GRASS_BLOCK`. Add the new registrations immediately after the existing `TEMPORAL_GRASS_BLOCK` block — keeping related plant tinting together makes future review easier.

- [ ] **Step 1: Add import in every Fabric client file**

For each of the 11 files, add this import alongside the existing `LeafColorProvider` import:

```java
import com.chronodawn.client.TemporalPlantColorProvider;
```

- [ ] **Step 2: Add block color registration in every Fabric client file**

For each of the 11 files, locate the existing `// Register Temporal Grass Block color` block in `registerBlockColors()` and append this snippet after it (before the method's closing brace):

```java
        // Register Temporal Tall Grass + Temporal Fern colors.
        // Plant textures are baked teal (Chrono Dawn plains grass_color);
        // tint is biased toward the local biome's grass color via 50/50 blend.
        ColorProviderRegistry.BLOCK.register(
            (state, world, pos, tintIndex) ->
                TemporalPlantColorProvider.blockTint(world, pos, tintIndex),
            ModBlocks.TEMPORAL_TALL_GRASS.get(),
            ModBlocks.TEMPORAL_FERN.get()
        );

        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> TemporalPlantColorProvider.itemTint(tintIndex),
            ModItems.TEMPORAL_TALL_GRASS.get(),
            ModItems.TEMPORAL_FERN.get()
        );
```

The signature `ColorProviderRegistry.BLOCK.register(provider, Block...)` is stable across all 11 Fabric versions — the existing `TEMPORAL_GRASS_BLOCK` registration uses the same form in every file.

The `ITEM.register` call is added only to `fabric/1.20.1`, `fabric/1.21.1`, and `fabric/1.21.2`. Fabric 1.21.4+ uses Client Items JSON for item tints (the existing `assets/chronodawn/items/temporal_tall_grass.json` / `temporal_fern.json` files already render the raw texture), so `ITEM.register` would be dead code in those versions. This mirrors the NeoForge 1.21.4+ convention (spec §"Per-loader registration").

- [ ] **Step 3: Build for one version to confirm compile**

```
./gradlew build1_21_2
```

Expected: BUILD SUCCESSFUL. If a single Fabric version fails to compile, fix and re-run before continuing.

- [ ] **Step 4: Build all release versions**

```
./gradlew buildAll
```

Expected: BUILD SUCCESSFUL across 1.20.1, 1.21.1, 1.21.2, 1.21.4–1.21.11.

- [ ] **Step 5: Commit**

```bash
git add fabric
git commit -m "feat(plants): register Fabric biome tint for Temporal Tall Grass/Fern"
```

---

## Task 3: Wire NeoForge block color registration (`neoforge/base` + 1.21.4–1.21.11)

**Files:** all 9 `neoforge/<ver>/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java` listed above.

Each file has an `onRegisterBlockColors(RegisterColorHandlersEvent.Block event)` method. Add the new registration after the existing `TEMPORAL_GRASS_BLOCK` block, before the trailing `LOGGER.debug` call.

- [ ] **Step 1: Add import in every NeoForge client file**

For each of the 9 files, add this import alongside the existing `LeafColorProvider` import:

```java
import com.chronodawn.client.TemporalPlantColorProvider;
```

- [ ] **Step 2: Add block color registration in every NeoForge client file**

For each of the 9 files, locate the `// Register Temporal Grass Block color` block inside `onRegisterBlockColors`, then append:

```java
        // Register Temporal Tall Grass + Temporal Fern colors (50/50 baseline-vs-biome blend).
        event.register(
            (state, world, pos, tintIndex) ->
                TemporalPlantColorProvider.blockTint(world, pos, tintIndex),
            ModBlocks.TEMPORAL_TALL_GRASS.get(),
            ModBlocks.TEMPORAL_FERN.get()
        );
```

- [ ] **Step 3: Build for 1.21.2 (NeoForge `base` covers 1.21.1–1.21.3) and 1.21.4 + 1.21.11**

```
./gradlew build1_21_2
./gradlew build1_21_4
./gradlew build1_21_11
```

Expected: each BUILD SUCCESSFUL. These three exercise the `base/` shared NeoForge source, the earliest NeoForge per-version source (1.21.4), and the latest (1.21.11) — covering every code-path variant.

- [ ] **Step 4: Build all release versions**

```
./gradlew buildAll
```

Expected: BUILD SUCCESSFUL across 1.20.1, 1.21.1, 1.21.2, 1.21.4–1.21.11.

- [ ] **Step 5: Commit**

```bash
git add neoforge
git commit -m "feat(plants): register NeoForge biome tint for Temporal Tall Grass/Fern"
```

---

## Task 4: Wire NeoForge `base` item color registration (1.21.1–1.21.3 only)

**Files:** `neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`

NeoForge `base/` covers 1.21.1–1.21.3 and still uses `RegisterColorHandlersEvent.Item`. Per-version NeoForge files for 1.21.4+ removed item color registration in favour of Client Items JSON; since our item tint returns `-1` (the "no tint" sentinel, texture unchanged), no Client Items JSON edits are required for those versions.

- [ ] **Step 1: Add item color registration in `neoforge/base`**

Locate `onRegisterItemColors(RegisterColorHandlersEvent.Item event)` and append (before the trailing `LOGGER.debug`):

```java
        // Item icon for Temporal Tall Grass + Temporal Fern: keep raw texture (no tint).
        event.register(
            (stack, tintIndex) -> TemporalPlantColorProvider.itemTint(tintIndex),
            ModItems.TEMPORAL_TALL_GRASS.get(),
            ModItems.TEMPORAL_FERN.get()
        );
```

- [ ] **Step 2: Build NeoForge for 1.21.2 (uses `base/`)**

```
./gradlew build1_21_2
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java
git commit -m "feat(plants): register NeoForge item color for Temporal plants on 1.21.1-1.21.3"
```

---

## Task 5: Multi-version verification + manual smoke test

**Files:** none modified — verification only.

- [ ] **Step 1: Run `checkAll`**

```
./gradlew checkAll
```

Expected: all phases pass — `cleanAll`, `validateResources`, `validateTranslations`, `buildAll`, `testAll` (8 unit tests for `TemporalPlantColorProvider`), `gameTestAll`.

If `testAll` reports `TemporalPlantColorProviderTest` failures, debug locally with `./gradlew :common-1.21.2:test -Ptarget_mc_version=1.21.2 --tests com.chronodawn.client.TemporalPlantColorProviderTest`.

- [ ] **Step 2: Smoke test in Fabric 1.21.2**

```
./gradlew runClientFabric1_21_2
```

In-game:

1. `/give @s chronodawn:temporal_tall_grass 64`
2. `/give @s chronodawn:temporal_fern 64`
3. Confirm inventory icons match the existing teal appearance (no visible change).
4. `/locate biome chronodawn:chronodawn_plains` → teleport → place both plants. Visual: identical to current build (baseline tint = WHITE).
5. `/locate biome chronodawn:chronodawn_dark_forest` → teleport → place both plants. Visual: muted teal-green, biome influence visible but blue still present.
6. `/locate biome chronodawn:chronodawn_snowy` → place. Visual: still mostly teal, slightly cooler.
7. `/locate biome chronodawn:chronodawn_swamp` → place. Visual: greener tint (swamp modifier applied).
8. Find naturally-generated tall grass / fern in dark_forest or swamp. Confirm they match user-placed plants in the same biome.

- [ ] **Step 3: Smoke test in NeoForge 1.21.2 (covers `base/` path)**

```
./gradlew runClientNeoForge1_21_2
```

Repeat steps 1–8 above. Confirm parity with Fabric.

- [ ] **Step 4: Spot-check NeoForge 1.21.11 (latest per-version path)**

```
./gradlew runClientNeoForge1_21_11
```

Place plants in dark_forest and plains. Confirm tint behaviour matches 1.21.2.

- [ ] **Step 5: Commit (if any docs updates needed during verification)**

If verification surfaces no issues, no commit is needed for this task. If you tweak the `BLEND` constant or fix any per-version discrepancy, commit those changes:

```bash
git add <changed files>
git commit -m "fix(plants): <describe the tweak>"
```

---

## Self-Review

**Spec coverage:**
- Architecture / shared utility → Task 1
- Tint formula (incl. clamp, zero-channel guard) → Task 1 Step 3 + Task 1 Step 1 tests
- Fabric registration (11 files) → Task 2
- NeoForge block registration (9 files) → Task 3
- NeoForge `base/` item registration → Task 4
- Edge cases (`tintIndex != 0`, `world == null`, `baseC == 0`, `BLEND` tunable) → Task 1 covers via tests + comments
- Cross-version stability note → covered by Task 2 / Task 3 build steps + Task 5 smoke checks
- Unit test plan (5 cases from spec, expanded to 8 named methods / 8 invocations after merging duplicate-value `itemTint` cases into one method) → Task 1 Step 1
- Manual in-game checks → Task 5
- `checkAll` requirement → Task 5 Step 1

**Placeholder scan:** No `TBD`, `TODO`, or "implement later" left in steps. All code blocks contain complete, copy-pasteable code.

**Type consistency:**
- `TemporalPlantColorProvider.blockTint(BlockAndTintGetter, BlockPos, int)` — same signature in impl, test, and all registration sites.
- `TemporalPlantColorProvider.itemTint(int)` — same signature everywhere.
- `blendChannel(int, int)` — package-private in source; called directly in test (no reflection).
- `BASELINE = 0x5B8AC4` named constant; `BLEND = 0.5f` named constant.
- `ModBlocks.TEMPORAL_TALL_GRASS` / `TEMPORAL_FERN` and `ModItems.TEMPORAL_TALL_GRASS` / `TEMPORAL_FERN` confirmed to exist in `common/<ver>/.../registry/`.
