# Ore Generation Tuning Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose `enabled`, `count`, `y_min`, `y_max` for three ChronoDawn-specific ores (Time Crystal, Entropy Crystal, Temporal Amber) via `config/chronodawn.toml`, reusing the runtime overlay-pack infrastructure shipped in the Ancient Ruins config PR.

**Architecture:** Add a second overlay generator `RuntimePlacedFeatureOverlay` alongside the existing `RuntimeStructureOverlay`. It emits three placed-feature JSON files from `OresConfig` values; `OverlayPackBootstrap` merges its output into the same virtual datapack already wired through `OverlayPackPlatform`. New nested config records (`OresConfig`, `OreSettings`) are added to `ChronoDawnConfig.World`; defaults exactly mirror the bundled placed-feature JSONs, enforced by a round-trip parsed-JSON-tree equality test that runs on all 11 supported MC versions.

**Tech Stack:** Java 21, Architectury common module, night-config (TOML), JUnit 5, Gson (test-only JSON parsing). No new dependencies.

**Spec:** [`docs/superpowers/specs/2026-05-16-ore-generation-tuning-design.md`](../specs/2026-05-16-ore-generation-tuning-design.md)

**Branch:** `ore-generation-tuning` (worktree already present at `.worktrees/ore-generation-tuning`)

---

## File Structure

### New files (created by this plan)

| Path | Responsibility |
| --- | --- |
| `common/shared/src/main/java/com/chronodawn/config/OreSettings.java` | Immutable record `(boolean enabled, int count, int yMin, int yMax)` for one ore's tunables. |
| `common/shared/src/main/java/com/chronodawn/config/OresConfig.java` | Aggregates the three `OreSettings` instances. |
| `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java` | Builds placed-feature JSON bytes for each ore from the active config. String-literal JSON, no library dep. |
| `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java` | Round-trip tree equality against bundled JSON + custom-value + disabled-state + distribution-type assertions. |

### Modified files

| Path | Change |
| --- | --- |
| `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java` | Extend `World` record with `OresConfig ores`; add accessor. |
| `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java` | Add three `OreSettings` defaults; wire into `defaults()` builder. |
| `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java` | Parse `[world.ores.<id>]` per ore; validate `enabled` / `count` / `y_min` / `y_max`; field-level revert on invalid. |
| `common/shared/src/main/java/com/chronodawn/worldgen/runtime/OverlayPackBootstrap.java` | Merge `RuntimePlacedFeatureOverlay.generate(config)` into the on-disk overlay datapack. |
| `common/shared/src/main/resources/chronodawn-default-config.toml` | Append three `[world.ores.*]` sections with comments. |
| `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java` | Add tests for the new sections and their validation rules. |
| `docs/configuration.md` | New "Ore generation" section listing the three ores, keys, ranges, restart/existing-chunk notes. |
| `docs/modpack-integration.md` | One short snippet rebalancing a single ore. |
| `CHANGELOG.md` | Single line under `[Unreleased] ### Added`. |
| `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md` | Update sub-project A status; record `ore_clockstone` as deferred follow-up (depends on version-aware defaults). |

### Untouched (deliberate)

- `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_{time_crystal,entropy_crystal,temporal_amber}.json` — bundled JSONs remain the source of truth for shape. The round-trip test asserts the runtime overlay reproduces them byte-for-tree at default settings.
- `data/chronodawn/worldgen/configured_feature/ore_*.json` — cluster size / target predicates untouched.
- `ore_clockstone`, `ore_iron`, `ore_gold`, `ore_coal`, `ore_redstone` placed features — out of scope.
- `OverlayPackPlatform` and its Fabric/NeoForge `@ExpectPlatform` implementations — overlay entries get merged inside the existing virtual pack; the loader interface does not change.

---

## Task 1: Add `OreSettings` and `OresConfig` records and wire them into `ChronoDawnConfig` + `ConfigDefaults`

These three files are tightly coupled (a record, its aggregator, and the defaults that build the aggregator). Implementing them in one task keeps the codebase compilable; downstream tasks add behavior + tests.

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/config/OreSettings.java`
- Create: `common/shared/src/main/java/com/chronodawn/config/OresConfig.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`

- [ ] **Step 1: Create `OreSettings.java`**

```java
/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.config;

/**
 * Tunable settings for a single ore's placed-feature generation.
 *
 * <p>{@code count} is the number of placement attempts per chunk (the
 * leading {@code minecraft:count} step). {@code yMin} and {@code yMax} are
 * absolute Y bounds for the {@code minecraft:height_range} modifier. When
 * {@code enabled} is {@code false}, the runtime overlay emits the same
 * shape with {@code count = 0} so the resource stays registered but
 * generates nothing.
 */
public record OreSettings(boolean enabled, int count, int yMin, int yMax) {}
```

- [ ] **Step 2: Create `OresConfig.java`**

```java
/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.config;

/**
 * Container for the three ChronoDawn-specific ores currently exposed via
 * {@code config/chronodawn.toml}: Time Crystal, Entropy Crystal, and
 * Temporal Amber. {@code ore_clockstone} and the vanilla-overlay ores
 * (iron/gold/coal/redstone) are deliberately not exposed here — see the
 * design spec for the rationale.
 */
public record OresConfig(
    OreSettings timeCrystal,
    OreSettings entropyCrystal,
    OreSettings temporalAmber
) {}
```

- [ ] **Step 3: Extend `ChronoDawnConfig.World` with `OresConfig ores`**

Locate the nested record near the bottom of `ChronoDawnConfig.java`:

```java
    public record World(Structures structures) {}
```

Replace with:

```java
    public record World(Structures structures, OresConfig ores) {}
```

(Other nested records `Structures` and `AncientRuins` stay as-is. `OresConfig` and `OreSettings` already live in the same `com.chronodawn.config` package so no imports needed.)

- [ ] **Step 4: Extend `ConfigDefaults` with three ore defaults and wire them**

In `ConfigDefaults.java`, add the three constants just below the existing `ANCIENT_RUINS_*` block:

```java
    // Defaults mirror common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_*.json
    // Asserted by RuntimePlacedFeatureOverlayTest (round-trip parsed-tree equality against the bundled JSONs).
    public static final OreSettings TIME_CRYSTAL_DEFAULTS = new OreSettings(true, 3, 0, 48);
    public static final OreSettings ENTROPY_CRYSTAL_DEFAULTS = new OreSettings(true, 4, 40, 100);
    public static final OreSettings TEMPORAL_AMBER_DEFAULTS = new OreSettings(true, 4, -30, 20);
```

Update `defaults()` to construct the `World` with the new `OresConfig`:

```java
    public static ChronoDawnConfig defaults() {
        return new ChronoDawnConfig(
            ChronoDawnConfig.CURRENT_SCHEMA_VERSION,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    new ChronoDawnConfig.AncientRuins(
                        ANCIENT_RUINS_ENABLED,
                        ANCIENT_RUINS_SPACING,
                        ANCIENT_RUINS_SEPARATION,
                        ANCIENT_RUINS_SALT
                    )
                ),
                new OresConfig(
                    TIME_CRYSTAL_DEFAULTS,
                    ENTROPY_CRYSTAL_DEFAULTS,
                    TEMPORAL_AMBER_DEFAULTS
                )
            )
        );
    }
```

- [ ] **Step 5: Build to confirm compile + downstream callers still match**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`. If a caller of `new ChronoDawnConfig.World(structures)` exists elsewhere, the compiler points it out — every such site must pass the second `OresConfig` argument (use `ConfigDefaults.defaults().world().ores()` as the safe value if the caller has no opinion). Inside production code only `ConfigDefaults.defaults()` and `ConfigLoader.parseOrDefaults` should be constructing `World`; tests (`RuntimeStructureOverlayTest`) construct it directly and must be patched to pass `OresConfig` too — fix any such test sites at this point so the build stays green.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/OreSettings.java \
        common/shared/src/main/java/com/chronodawn/config/OresConfig.java \
        common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java \
        common/shared/src/test/java/com/chronodawn/unit/RuntimeStructureOverlayTest.java
git commit -m "feat(config): add OreSettings / OresConfig records and defaults"
```

---

## Task 2: Append three `[world.ores.*]` sections to the bundled default TOML

The default file is what users see on first launch; the values here must agree with `ConfigDefaults` exactly (round-trip test in Task 7 also asserts equivalence against the bundled JSONs).

**Files:**
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml`

- [ ] **Step 1: Append the three sections after the existing `[world.structures.ancient_ruins]` block**

Open the file and add (preserving the trailing newline):

```toml

[world.ores.time_crystal]
# Whether Time Crystal ore generates in the Chrono dimension.
enabled = true

# Number of placement attempts per chunk. Lower = rarer, higher = denser.
# Each attempt generates one cluster (cluster size is fixed at the data-pack level).
count = 3

# Minimum and maximum Y level (absolute) where Time Crystal ore can spawn.
# The distribution between min and max is biased toward the middle (trapezoid).
y_min = 0
y_max = 48

[world.ores.entropy_crystal]
# Whether Entropy Crystal ore generates.
enabled = true

count = 4
y_min = 40
y_max = 100

[world.ores.temporal_amber]
# Whether Temporal Amber ore generates.
enabled = true

# Temporal Amber uses a uniform distribution (flat probability across the Y range)
# rather than trapezoid; tune y_min/y_max accordingly.
count = 4
y_min = -30
y_max = 20
```

- [ ] **Step 2: Sanity-check the file parses**

Run: `python3 -c "import tomllib, sys; tomllib.load(open('common/shared/src/main/resources/chronodawn-default-config.toml', 'rb'))"`
Expected: no output (success).

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/chronodawn-default-config.toml
git commit -m "feat(config): bundle default ore tuning sections in chronodawn-default-config.toml"
```

---

## Task 3: TDD — `RuntimePlacedFeatureOverlay` produces the Time Crystal placed-feature JSON

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java`
- Create: `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`

- [ ] **Step 1: Write the failing test — default config reproduces bundled `ore_time_crystal.json`**

Create the test file with:

```java
/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.worldgen.runtime;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link RuntimePlacedFeatureOverlay}.
 *
 * <p>The default-equivalence assertions compare parsed JSON trees (not raw bytes)
 * so the overlay generator can format whitespace however it likes. If a future
 * change drifts the runtime emitter away from a bundled JSON, these tests fail
 * with a tree diff pointing at the differing key.
 */
class RuntimePlacedFeatureOverlayTest {

    @Test
    void defaultConfig_reproducesBundledTimeCrystalJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.TIME_CRYSTAL_PATH);
        assertNotNull(bytes, "Overlay must contain ore_time_crystal.json under expected path");

        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_time_crystal.json");
        assertEquals(bundled, generated,
            "Runtime overlay output for default config must be tree-equal to the bundled placed_feature JSON");
    }

    private static JsonElement loadBundled(String classpathRelative) {
        try (InputStream in = RuntimePlacedFeatureOverlayTest.class.getClassLoader()
            .getResourceAsStream(classpathRelative)) {
            assertNotNull(in, "Bundled resource not found on test classpath: " + classpathRelative);
            return JsonParser.parseReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: Run the test, expect FAIL (compilation error: `RuntimePlacedFeatureOverlay` does not exist)**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest"`
Expected: compilation failure pointing at the missing class.

- [ ] **Step 3: Implement `RuntimePlacedFeatureOverlay` with only the Time Crystal path**

```java
/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.worldgen.runtime;

import com.chronodawn.ChronoDawn;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.OreSettings;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates ChronoDawn ore placed_feature JSON files at runtime from
 * {@link ChronoDawnConfig}'s {@code OresConfig}.
 *
 * <p>Output paths follow the data-pack convention
 * {@code data/<namespace>/worldgen/placed_feature/<id>.json} so the
 * generated entries shadow the bundled mod resources.
 *
 * <p>{@code enabled = false} emits the same JSON shape with {@code count = 0}.
 * Keeping the resource registered means biome references to the placed_feature
 * still resolve; runtime placement evaluates to zero attempts so nothing
 * actually generates. The user's {@code count}, {@code yMin}, {@code yMax}
 * values are preserved verbatim (only the leading {@code minecraft:count}
 * step's {@code count} is overridden to 0), so re-enabling the ore restores
 * the user's last numbers rather than reverting to mod defaults.
 */
public final class RuntimePlacedFeatureOverlay {
    public static final String TIME_CRYSTAL_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_time_crystal.json";

    private RuntimePlacedFeatureOverlay() {}

    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        out.put(TIME_CRYSTAL_PATH, generateOre(
            "ore_time_crystal", "minecraft:trapezoid", config.world().ores().timeCrystal()));
        return out;
    }

    static byte[] generateOre(String featureId, String heightDistributionType, OreSettings settings) {
        int emittedCount = settings.enabled() ? settings.count() : 0;
        String json =
            "{\n" +
            "  \"feature\": \"" + ChronoDawn.MOD_ID + ":" + featureId + "\",\n" +
            "  \"placement\": [\n" +
            "    {\n" +
            "      \"type\": \"minecraft:count\",\n" +
            "      \"count\": " + emittedCount + "\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"minecraft:in_square\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"minecraft:height_range\",\n" +
            "      \"height\": {\n" +
            "        \"type\": \"" + heightDistributionType + "\",\n" +
            "        \"min_inclusive\": {\n" +
            "          \"absolute\": " + settings.yMin() + "\n" +
            "        },\n" +
            "        \"max_inclusive\": {\n" +
            "          \"absolute\": " + settings.yMax() + "\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"minecraft:biome\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 4: Run the test, expect PASS**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest"`
Expected: `BUILD SUCCESSFUL`, one test passes.

If the tree-equality assertion fails, the diff is printed by Gson — read the differing key and patch either the constants (Task 1) or the emitter string. Do NOT relax the assertion.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java \
        common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java
git commit -m "feat(worldgen): add RuntimePlacedFeatureOverlay for Time Crystal ore"
```

---

## Task 4: Add Entropy Crystal and Temporal Amber default round-trip tests + emitter wiring

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java`
- Modify: `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`

- [ ] **Step 1: Add the two failing tests**

Append to `RuntimePlacedFeatureOverlayTest`:

```java
    @Test
    void defaultConfig_reproducesBundledEntropyCrystalJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.ENTROPY_CRYSTAL_PATH);
        assertNotNull(bytes, "Overlay must contain ore_entropy_crystal.json under expected path");
        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_entropy_crystal.json");
        assertEquals(bundled, generated);
    }

    @Test
    void defaultConfig_reproducesBundledTemporalAmberJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.TEMPORAL_AMBER_PATH);
        assertNotNull(bytes, "Overlay must contain ore_temporal_amber.json under expected path");
        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_temporal_amber.json");
        assertEquals(bundled, generated);
    }
```

- [ ] **Step 2: Run, expect FAIL (compile error: `ENTROPY_CRYSTAL_PATH` / `TEMPORAL_AMBER_PATH` undefined)**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest"`
Expected: compilation failure naming the two missing constants.

- [ ] **Step 3: Extend the emitter to register the other two ores**

In `RuntimePlacedFeatureOverlay.java`, add two constants beneath `TIME_CRYSTAL_PATH`:

```java
    public static final String ENTROPY_CRYSTAL_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_entropy_crystal.json";
    public static final String TEMPORAL_AMBER_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_temporal_amber.json";
```

Replace the body of `generate(...)` with:

```java
    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        out.put(TIME_CRYSTAL_PATH, generateOre(
            "ore_time_crystal", "minecraft:trapezoid", config.world().ores().timeCrystal()));
        out.put(ENTROPY_CRYSTAL_PATH, generateOre(
            "ore_entropy_crystal", "minecraft:trapezoid", config.world().ores().entropyCrystal()));
        out.put(TEMPORAL_AMBER_PATH, generateOre(
            "ore_temporal_amber", "minecraft:uniform", config.world().ores().temporalAmber()));
        return out;
    }
```

(Note: Temporal Amber uses `minecraft:uniform`; the other two use `minecraft:trapezoid`. This per-ore mapping is the only ore-shape difference and is intentionally not user-tunable — see spec non-goal "User-controlled height distribution type".)

- [ ] **Step 4: Run, expect all three default round-trip tests PASS**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest"`
Expected: 3 tests, all pass.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java \
        common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java
git commit -m "feat(worldgen): emit RuntimePlacedFeatureOverlay for entropy_crystal and temporal_amber"
```

---

## Task 5: Cover custom values, disabled state, and per-ore distribution-type assertions

This covers the rest of the spec's verification list for `RuntimePlacedFeatureOverlayTest`: custom `count`, `enabled = false` with preserved Y / count round-trip, custom `y_min` / `y_max` for Temporal Amber, and explicit distribution-type assertions per ore.

**Files:**
- Modify: `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`

- [ ] **Step 1: Add four tests covering the remaining verification points**

Append:

```java
    @Test
    void timeCrystalCountOverride_changesCountStepOnly() {
        ChronoDawnConfig custom = withOres(
            new OreSettings(true, 10, 0, 48),
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.TIME_CRYSTAL_PATH));
        assertEquals(10, countStep(json).get("count").getAsInt());
    }

    @Test
    void entropyCrystalDisabled_emitsCountZeroAndPreservesOtherValues() {
        // Disabled state: forced count=0 in the emitted JSON, but user's count / yMin / yMax
        // round-trip verbatim so re-enabling restores their last numbers.
        OreSettings disabled = new OreSettings(false, 7, 30, 90);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS, disabled, ConfigDefaults.TEMPORAL_AMBER_DEFAULTS
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.ENTROPY_CRYSTAL_PATH));
        assertEquals(0, countStep(json).get("count").getAsInt(),
            "enabled=false must force the count step to 0");
        JsonObject heightRange = json.getAsJsonArray("placement").get(2).getAsJsonObject();
        assertEquals(30,
            heightRange.getAsJsonObject("height").getAsJsonObject("min_inclusive").get("absolute").getAsInt(),
            "yMin must be preserved verbatim when disabled");
        assertEquals(90,
            heightRange.getAsJsonObject("height").getAsJsonObject("max_inclusive").get("absolute").getAsInt(),
            "yMax must be preserved verbatim when disabled");
    }

    @Test
    void temporalAmberCustomYRange_changesHeightBoundsAndKeepsUniform() {
        OreSettings amber = new OreSettings(true, 4, -20, 30);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS, ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, amber
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.TEMPORAL_AMBER_PATH));
        JsonObject height = json.getAsJsonArray("placement").get(2).getAsJsonObject().getAsJsonObject("height");
        assertEquals(-20, height.getAsJsonObject("min_inclusive").get("absolute").getAsInt());
        assertEquals(30, height.getAsJsonObject("max_inclusive").get("absolute").getAsInt());
        assertEquals("minecraft:uniform", height.get("type").getAsString());
    }

    @Test
    void perOreDistributionType_isFixed() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        assertEquals("minecraft:trapezoid",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.TIME_CRYSTAL_PATH)));
        assertEquals("minecraft:trapezoid",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.ENTROPY_CRYSTAL_PATH)));
        assertEquals("minecraft:uniform",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.TEMPORAL_AMBER_PATH)));
    }

    // --- helpers ---

    private static ChronoDawnConfig withOres(OreSettings tc, OreSettings ec, OreSettings ta) {
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        return new ChronoDawnConfig(
            defaults.schemaVersion(),
            new ChronoDawnConfig.World(
                defaults.world().structures(),
                new com.chronodawn.config.OresConfig(tc, ec, ta)
            )
        );
    }

    private static JsonObject parseObject(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static JsonObject countStep(JsonObject placedFeature) {
        return placedFeature.getAsJsonArray("placement").get(0).getAsJsonObject();
    }

    private static String heightType(byte[] bytes) {
        return parseObject(bytes).getAsJsonArray("placement").get(2).getAsJsonObject()
            .getAsJsonObject("height").get("type").getAsString();
    }
```

Also add the imports at the top of the test file:

```java
import com.chronodawn.config.OreSettings;
```

(`JsonObject` is already imported in the spec test pattern — re-verify the test compiles after pasting.)

- [ ] **Step 2: Run the new tests, expect PASS without further impl changes**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest"`
Expected: 7 tests, all pass.

If `entropyCrystalDisabled_emitsCountZeroAndPreservesOtherValues` fails saying `yMin` was rewritten, the emitter is incorrectly zeroing more than just the count — fix `generateOre` to only override the count when `!enabled`, leaving `yMin` / `yMax` / `count` (in their original positions) untouched.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java
git commit -m "test(worldgen): cover custom values, disabled state, and distribution types for ore overlay"
```

---

## Task 6: Wire `RuntimePlacedFeatureOverlay` into `OverlayPackBootstrap`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/OverlayPackBootstrap.java`

- [ ] **Step 1: Merge both overlay maps**

Locate the loop in `writeOverlay`:

```java
            // Generated content
            Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(config);
            for (Map.Entry<String, byte[]> entry : overlay.entrySet()) {
```

Replace the two lines that build `overlay` with:

```java
            // Generated content — merge every overlay generator into a single map
            Map<String, byte[]> overlay = new java.util.LinkedHashMap<>();
            overlay.putAll(RuntimeStructureOverlay.generate(config));
            overlay.putAll(RuntimePlacedFeatureOverlay.generate(config));
```

Path collisions between generators should never happen (different worldgen subdirectories); if they did, `putAll`'s last-write-wins semantics would mask the bug. Add an `assert` only if a future generator emits overlapping paths.

- [ ] **Step 2: Verify all existing overlay tests still pass + the ore tests pass under bootstrap path**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.RuntimeStructureOverlayTest" --tests "com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest"`
Expected: all pass.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/worldgen/runtime/OverlayPackBootstrap.java
git commit -m "feat(worldgen): merge placed-feature overlay into runtime pack bootstrap"
```

---

## Task 7: Parse `[world.ores.<id>]` sections in `ConfigLoader` with field-level validation

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
- Modify: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java`

- [ ] **Step 1: Add failing tests covering the spec's `ConfigLoaderTest` additions**

Append to `ConfigLoaderTest`:

```java
    @Test
    void oresDefault_allFieldsMatchDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OresConfig ores = config.world().ores();
        assertEquals(ConfigDefaults.TIME_CRYSTAL_DEFAULTS, ores.timeCrystal());
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, ores.entropyCrystal());
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS, ores.temporalAmber());
    }

    @Test
    void validOreCustom_isReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.time_crystal]\n" +
            "enabled = false\n" +
            "count = 7\n" +
            "y_min = -10\n" +
            "y_max = 20\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings tc = config.world().ores().timeCrystal();
        assertEquals(false, tc.enabled());
        assertEquals(7, tc.count());
        assertEquals(-10, tc.yMin());
        assertEquals(20, tc.yMax());
        // Other ores are untouched
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, config.world().ores().entropyCrystal());
    }

    @Test
    void invalidOreCount_negative_revertsCountOnly(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.time_crystal]\n" +
            "count = -5\n" +
            "y_min = 5\n" +
            "y_max = 40\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings tc = config.world().ores().timeCrystal();
        assertEquals(ConfigDefaults.TIME_CRYSTAL_DEFAULTS.count(), tc.count(),
            "Invalid count reverts to default");
        assertEquals(5, tc.yMin(), "Valid yMin survives a sibling field's revert");
        assertEquals(40, tc.yMax(), "Valid yMax survives a sibling field's revert");
    }

    @Test
    void invalidOreCount_overMax_revertsCount(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.entropy_crystal]\n" +
            "count = 100\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS.count(),
            config.world().ores().entropyCrystal().count());
    }

    @Test
    void invertedYRange_revertsBothYFieldsOnly(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.entropy_crystal]\n" +
            "count = 6\n" +
            "y_min = 50\n" +
            "y_max = 40\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings ec = config.world().ores().entropyCrystal();
        assertEquals(6, ec.count(), "Valid count survives an inverted-Y revert");
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS.yMin(), ec.yMin());
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS.yMax(), ec.yMax());
    }

    @Test
    void yOutOfRange_belowMin_revertsBothY(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.temporal_amber]\n" +
            "y_min = -100\n" +
            "y_max = 10\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings ta = config.world().ores().temporalAmber();
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS.yMin(), ta.yMin());
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS.yMax(), ta.yMax());
    }

    @Test
    void unknownOreSection_isIgnored(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.nonexistent]\n" +
            "count = 5\n" +
            "[world.ores.time_crystal]\n" +
            "count = 9\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(9, config.world().ores().timeCrystal().count(),
            "Known ore section still loads despite an unknown sibling");
    }
```

- [ ] **Step 2: Run new tests, expect FAIL (parser doesn't read `world.ores.*` yet → all defaults returned, several assertions fail)**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.ConfigLoaderTest"`
Expected: the seven new tests fail; existing 11 tests pass.

- [ ] **Step 3: Add ore parsing + validation to `ConfigLoader`**

Add key constants near the existing ones:

```java
    private static final String K_ORES = "ores";
    private static final String K_TIME_CRYSTAL = "time_crystal";
    private static final String K_ENTROPY_CRYSTAL = "entropy_crystal";
    private static final String K_TEMPORAL_AMBER = "temporal_amber";
    private static final String K_ORE_ENABLED = "enabled";
    private static final String K_ORE_COUNT = "count";
    private static final String K_ORE_Y_MIN = "y_min";
    private static final String K_ORE_Y_MAX = "y_max";

    private static final int MIN_ORE_COUNT = 0;
    private static final int MAX_ORE_COUNT = 64;
    private static final int MIN_ORE_Y = -64;
    private static final int MAX_ORE_Y = 320;
```

Extend `parseOrDefaults` to build an `OresConfig` and pass it into `World`:

```java
        ChronoDawnConfig.AncientRuins ancientRuins = parseAncientRuins(parsed);
        com.chronodawn.config.OresConfig ores = parseOres(parsed);

        // ... unknown-key warning loop unchanged ...

        return new ChronoDawnConfig(
            schemaVersion,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(ancientRuins),
                ores
            )
        );
```

Add the new parsing methods:

```java
    private static com.chronodawn.config.OresConfig parseOres(CommentedConfig parsed) {
        return new com.chronodawn.config.OresConfig(
            parseOre(parsed, K_TIME_CRYSTAL, ConfigDefaults.TIME_CRYSTAL_DEFAULTS),
            parseOre(parsed, K_ENTROPY_CRYSTAL, ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS),
            parseOre(parsed, K_TEMPORAL_AMBER, ConfigDefaults.TEMPORAL_AMBER_DEFAULTS)
        );
    }

    private static com.chronodawn.config.OreSettings parseOre(
        CommentedConfig parsed, String oreKey, com.chronodawn.config.OreSettings defaults
    ) {
        String path = K_WORLD + "." + K_ORES + "." + oreKey;

        boolean enabled = parsed.<Boolean>getOptional(path + "." + K_ORE_ENABLED)
            .orElse(defaults.enabled());

        int count = parsed.<Number>getOptional(path + "." + K_ORE_COUNT)
            .map(Number::intValue)
            .orElse(defaults.count());

        int yMin = parsed.<Number>getOptional(path + "." + K_ORE_Y_MIN)
            .map(Number::intValue)
            .orElse(defaults.yMin());

        int yMax = parsed.<Number>getOptional(path + "." + K_ORE_Y_MAX)
            .map(Number::intValue)
            .orElse(defaults.yMax());

        // Validation: each field reverts independently so one bad value doesn't reset the others.
        if (count < MIN_ORE_COUNT || count > MAX_ORE_COUNT) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_ORE_COUNT, count, MIN_ORE_COUNT, MAX_ORE_COUNT, defaults.count()
            );
            count = defaults.count();
        }
        if (yMin < MIN_ORE_Y || yMax > MAX_ORE_Y || yMin > yMax) {
            LOGGER.error(
                "Invalid {}.{{y_min,y_max}} = ({}, {}) (must satisfy {} <= y_min <= y_max <= {}); using defaults ({}, {})",
                path, yMin, yMax, MIN_ORE_Y, MAX_ORE_Y, defaults.yMin(), defaults.yMax()
            );
            yMin = defaults.yMin();
            yMax = defaults.yMax();
        }

        return new com.chronodawn.config.OreSettings(enabled, count, yMin, yMax);
    }
```

(The unknown-top-level-key warning loop in `parseOrDefaults` already handles `[world.ores.nonexistent]` — `world` is recognised, and nested unknown keys are deliberately not walked per the existing comment in `ConfigLoader`. The spec accepts WARN-and-ignore behavior here.)

- [ ] **Step 4: Run the loader tests, expect PASS**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.ConfigLoaderTest"`
Expected: all 18 tests pass.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java \
        common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java
git commit -m "feat(config): parse and validate [world.ores.*] sections"
```

---

## Task 8: Document the new tunables in `docs/configuration.md`

**Files:**
- Modify: `docs/configuration.md`

- [ ] **Step 1: Insert an "Ore generation" section after `[world.structures.ancient_ruins]`**

After the closing `---` of the Ancient Ruins section (before "Adding more configuration"), insert:

```markdown
### `[world.ores.*]`

Per-ore generation tuning for the three ChronoDawn-specific ores in the
Chrono dimension: **Time Crystal**, **Entropy Crystal**, and **Temporal
Amber**. Each ore has its own table; missing sections fall back to the
defaults listed below.

> [!NOTE]
> `ore_clockstone` and the vanilla-overlay ores (`iron` / `gold` / `coal`
> / `redstone`) are intentionally not exposed yet — see the design spec
> for context.

```toml
[world.ores.time_crystal]
enabled = true
count = 3
y_min = 0
y_max = 48

[world.ores.entropy_crystal]
enabled = true
count = 4
y_min = 40
y_max = 100

[world.ores.temporal_amber]
enabled = true
count = 4
y_min = -30
y_max = 20
```

| Field | Type | Default (TC / EC / TA) | Range | Notes |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | `true` for all three | — | When `false`, the placed feature stays registered but emits `count = 0` so nothing generates. Your other values are preserved verbatim and restored on re-enable. |
| `count` | integer | `3` / `4` / `4` | `0..=64` | Number of placement attempts per chunk. Lower = rarer. Cluster size (blocks per attempt) is not exposed here. |
| `y_min` | integer | `0` / `40` / `-30` | `-64..=y_max` | Absolute minimum Y for placement. |
| `y_max` | integer | `48` / `100` / `20` | `y_min..=320` | Absolute maximum Y for placement. |

Distribution shape between `y_min` and `y_max` is **fixed per ore** and
not configurable:

- Time Crystal and Entropy Crystal use `minecraft:trapezoid` (concentrated
  in the middle of the range).
- Temporal Amber uses `minecraft:uniform` (flat probability across the
  range).

Authors who need a different distribution can ship a full datapack
override at `data/chronodawn/worldgen/placed_feature/ore_<id>.json`; the
runtime overlay sits below user datapacks, so your file wins.

#### Example: make Entropy Crystal rare

```toml
[world.ores.entropy_crystal]
count = 1
```

#### Example: push Temporal Amber deeper

```toml
[world.ores.temporal_amber]
y_min = -60
y_max = -10
```

#### Example: disable Time Crystal entirely

```toml
[world.ores.time_crystal]
enabled = false
```

The placed feature is still registered (biome references resolve) but
zero attempts are made per chunk. **New chunks only** — existing chunks
keep whatever already generated.
```

(Heading style and table conventions mirror the existing Ancient Ruins
section.)

- [ ] **Step 2: Commit**

```bash
git add docs/configuration.md
git commit -m "docs: document [world.ores.*] tuning in configuration.md"
```

---

## Task 9: Modpack snippet + CHANGELOG + roadmap update

**Files:**
- Modify: `docs/modpack-integration.md`
- Modify: `CHANGELOG.md`
- Modify: `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`

- [ ] **Step 1: Add the ore-rebalance snippet to `docs/modpack-integration.md`**

Inside the "Configuration → Bundling a configuration with your pack" subsection (or directly below the existing example block), append:

```markdown
### Example: rebalance a single ore

```toml
# Make Entropy Crystal one-third as common; deeper-only.
[world.ores.entropy_crystal]
count = 1
y_min = 5
y_max = 50
```

Bundle this file via `overrides/config/chronodawn.toml` and the runtime
overlay applies it on first launch. The other two ores stay at their
defaults. See [`docs/configuration.md`](configuration.md) for the full
schema.
```

Also update the "Restart and existing-world caveats" table — add a row:

```markdown
| `world.ores.*` | yes (server / world reload) | no — only new chunks |
```

- [ ] **Step 2: Add the CHANGELOG entry**

In `CHANGELOG.md`, under the existing `## [Unreleased]` → `### Added` block, append a single line at the end (preserving existing entries):

```markdown
- **Ore generation tuning** for Time Crystal, Entropy Crystal, and Temporal Amber — exposes `enabled`, `count`, `y_min`, `y_max` per ore via `config/chronodawn.toml`. See [docs/configuration.md](docs/configuration.md#worldores).
```

- [ ] **Step 3: Update the roadmap status note**

In `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`:

Replace this block (line ~23-25):

```markdown
### A. Config system

**Status**: 🚧 First PR shipped (Ancient Ruins). See design [`2026-05-09-config-foundation-design.md`](./2026-05-09-config-foundation-design.md) and plan [`../plans/2026-05-09-config-foundation.md`](../plans/2026-05-09-config-foundation.md). Continuing with follow-up tunables.
```

With:

```markdown
### A. Config system

**Status**: 🚧 Two PRs shipped: Ancient Ruins ([design](./2026-05-09-config-foundation-design.md), [plan](../plans/2026-05-09-config-foundation.md)) and Ore generation tuning for Time Crystal / Entropy Crystal / Temporal Amber ([design](./2026-05-16-ore-generation-tuning-design.md), [plan](../plans/2026-05-16-ore-generation-tuning.md)). Continuing with follow-up tunables.
```

Update the bulleted "Planned follow-up tunables" list (line ~29) — replace the existing item `- Ore generation rates and Y ranges` with:

```markdown
- Ore generation rates and Y ranges — *partial: Time Crystal / Entropy Crystal / Temporal Amber shipped. `ore_clockstone` deferred (needs version-aware defaults; bundled count differs across MC versions). Vanilla-overlay ores in the Chrono dimension also deferred.*
```

Also update the status-tracker table row (search for "A. Config system" in the table):

```markdown
| A. Config system | 🚧 Two PRs shipped (Ancient Ruins, Ore tuning) | [2026-05-09-config-foundation-design.md](./2026-05-09-config-foundation-design.md) |
```

- [ ] **Step 4: Commit**

```bash
git add docs/modpack-integration.md CHANGELOG.md docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md
git commit -m "docs: add ore tuning snippet, CHANGELOG entry, roadmap status update"
```

---

## Task 10: Full multi-version verification

The shared common module is already exercised by `:common-1.21.11:test`; the rest of the matrix verifies that the same shared sources build cleanly under every other version's compile classpath. Per the spec, `gameTestAll` is **not** part of this PR (no GameTest sources touched).

- [ ] **Step 1: Run unit tests for the primary verification pair**

Run: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1`
Expected: `BUILD SUCCESSFUL`.

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run the build + test matrix (gameTestAll skipped)**

Per the spec ("`gameTestAll` is skipped — this PR does not touch GameTest sources, matching the precedent set by the Ancient Ruins PR"), run only the build + unit-test layers of the matrix, not the wrapper `checkAll`:

```
./gradlew cleanAll validateResources validateTranslations buildAll testAll
```

Expected: `BUILD SUCCESSFUL`. The 11 versions × 2 loaders compile cleanly and `:common-X:test` is green on every version.

Per existing project memory, the `buildAll` wrapper occasionally reports spurious failures from RemapSourcesJarTask races; if a failure surfaces only in the aggregate wrapper, re-run the affected per-version build directly (e.g. `./gradlew build1_21_4 -Ptarget_mc_version=1.21.4`) to confirm whether it is real before investigating.

> [!NOTE]
> Running `./gradlew checkAll` (which wraps the above plus `gameTestAll`) is acceptable but the `gameTestAll` step may surface unrelated pre-existing failures on this branch (e.g. 1.20.1 mega-tree configured_feature parse errors introduced by earlier commits). Treat any `gameTestAll` failure that does not touch ore generation or config files as out of scope for this PR and file it as separate tech debt.

- [ ] **Step 3: If verification is fully green, no commit needed (verification only).**

---

## Task 11: Manual integration verification on 1.21.1 and 1.21.11 Fabric

This mirrors the manual-verification pair established by the Ancient Ruins
PR. Must be run by the developer, not automated.

- [ ] **Step 1: Default density regression check (1.21.11 Fabric)**

```bash
./gradlew runClientFabric1_21_11
```

In the launched client:
1. Create a new world (any seed).
2. Travel to the Chrono dimension.
3. Spend a few minutes locating each of Time Crystal, Entropy Crystal, and Temporal Amber. They should generate at the same visible density as before this PR.

Expected: all three ores are findable without invasive search; densities feel like the pre-PR baseline.

- [ ] **Step 2: Density boost — `time_crystal.count = 20`**

Stop the client. Edit `run/config/chronodawn.toml` (path printed in logs on first launch):

```toml
[world.ores.time_crystal]
count = 20
```

Restart with `./gradlew runClientFabric1_21_11`. Create a NEW world. Expected: Time Crystal is visibly far denser than in step 1.

- [ ] **Step 3: Disable — `temporal_amber.enabled = false`**

Stop the client. Edit `run/config/chronodawn.toml`:

```toml
[world.ores.temporal_amber]
enabled = false
```

Restart, create a NEW world, hunt for Temporal Amber. Expected: zero Temporal Amber ore generated.

- [ ] **Step 4: Repeat steps 1–3 on 1.21.1 Fabric**

```bash
./gradlew runClientFabric1_21_1
```

Use the same edit sequence. Expected: identical behavior to 1.21.11. If 1.21.1 diverges (e.g. ore generation under a 1.21.1 height-range modifier behaves differently), capture log lines and investigate before merging — the round-trip test should already have caught any JSON-shape divergence.

- [ ] **Step 5: No commit needed (verification only). If everything passes, the feature is done per the spec's "Done criteria"; proceed to the standard PR creation workflow.**

---

## Done criteria

- All files in the spec's "Files / New + Modified" section exist or are updated.
- `./gradlew testAll` passes on every supported version.
- `./gradlew cleanAll validateResources validateTranslations buildAll testAll` succeeds (per the spec; `gameTestAll` is skipped for this PR).
- Manual verification (Task 11 steps 1–4) passes on both 1.21.1 and 1.21.11.
- `docs/configuration.md` "Ore generation" section reviewed for clarity.
- CHANGELOG entry written under `[Unreleased]` `### Added`.
- Roadmap document's status tracker updated; `ore_clockstone` recorded as deferred follow-up tied to version-aware defaults.
