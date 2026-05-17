# Ore Clockstone Config Tuning Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose `chronodawn:ore_clockstone` generation parameters (`enabled` / `count` / `y_min` / `y_max`) via `config/chronodawn.toml`, following the established four-field `[world.ores.*]` pattern, and unify the bundled placed_feature `count` at `8` across every supported Minecraft version (1.20.1 + 1.21.1–1.21.11).

**Architecture:** Append `clockstone` as the fourth `OreSettings` field on the existing `OresConfig` record. `ConfigDefaults` gains a single `CLOCKSTONE_DEFAULTS` constant (`enabled=true, count=8, yMin=-16, yMax=80`); the constant works for every version once the 1.21.1+ bundled JSON is corrected from `count=16` to `count=8` to match the 2026-01 design intent (`90413a2e`). `ConfigLoader.parseOres()` gains a fourth `parseOre` call and `RuntimePlacedFeatureOverlay.generate()` gains a fourth entry. No new files; no compat class.

**Tech Stack:** Java 21, JUnit 5, night-config (TOML parser), Gson (test-only JSON tree comparison). Existing Architectury multi-loader / multi-version Gradle layout.

**Spec reference:** `docs/superpowers/specs/2026-05-17-ore-clockstone-config-tuning-design.md`

---

## Task 1: Restore intended count=8 on the 1.21.1+ bundled placed_feature

Rationale: this is the prerequisite for a single shared `ConfigDefaults.CLOCKSTONE_DEFAULTS`. Doing it first means every later task (records, defaults, tests) can use one constant value on every version. The 1.20.1 bundled JSON is already at `count=8` and stays untouched.

**Files:**
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_clockstone.json`

- [ ] **Step 1: Apply the one-line count correction**

Change `"count": 16` → `"count": 8` in the placement step. The full corrected file should read:

```json
{
  "feature": "chronodawn:ore_clockstone",
  "placement": [
    {
      "type": "minecraft:count",
      "count": 8
    },
    {
      "type": "minecraft:in_square"
    },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:trapezoid",
        "min_inclusive": {
          "absolute": -16
        },
        "max_inclusive": {
          "absolute": 80
        }
      }
    },
    {
      "type": "minecraft:biome"
    }
  ]
}
```

- [ ] **Step 2: Confirm the 1.20.1 file is already correct**

Run: `grep '"count"' common/1.20.1/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_clockstone.json`
Expected: a single line showing `"count": 8`.

If the 1.20.1 file is anything other than `8`, STOP and surface to the user — the spec assumes 1.20.1 was already at `8` from commit `90413a2e`.

- [ ] **Step 3: Validate resources**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL. This catches malformed JSON; it does not detect a semantic regression on its own, which is why Task 3 adds the round-trip test below.

- [ ] **Step 4: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_clockstone.json
git commit -m "fix(worldgen): correct ore_clockstone count to 8 on 1.21.1+

The 2026-01 frequency reduction (90413a2e: \"reduce Clockstone ore
frequency to align closer to vanilla iron ore levels\") was applied
only to the 1.20.1 placed_feature; the 1.21.1+ file silently retained
the pre-fix count=16. Aligns 1.21.1+ with the documented design intent
and enables a single shared CLOCKSTONE_DEFAULTS constant in the
follow-up config slice."
```

---

## Task 2: Add the clockstone field to OresConfig, ConfigDefaults, ConfigLoader, and RuntimePlacedFeatureOverlay

This is a single mechanical change that touches five files and adds one new test-helper signature. The build and all existing tests must stay green after this task (no clockstone-specific assertions exist yet; those land in Task 3 and Task 4).

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/OresConfig.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java`
- Modify: `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java` (helper signature only)

- [ ] **Step 1: Extend `OresConfig` record with `clockstone`**

Replace the body of `common/shared/src/main/java/com/chronodawn/config/OresConfig.java` (record declaration and Javadoc) with:

```java
/**
 * Container for the ChronoDawn-specific ores currently exposed via
 * {@code config/chronodawn.toml}: Time Crystal, Entropy Crystal,
 * Temporal Amber, and Clockstone. The vanilla-overlay ores
 * (iron/gold/coal/redstone) are deliberately not exposed here — see
 * the design spec for the rationale.
 */
public record OresConfig(
    OreSettings timeCrystal,
    OreSettings entropyCrystal,
    OreSettings temporalAmber,
    OreSettings clockstone
) {}
```

(Keep the existing license header at the top unchanged.)

- [ ] **Step 2: Add CLOCKSTONE_DEFAULTS and wire it into `ConfigDefaults.defaults()`**

In `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`, immediately below the existing `TEMPORAL_AMBER_DEFAULTS` line (after line 39), add:

```java
    public static final OreSettings CLOCKSTONE_DEFAULTS = new OreSettings(true, 8, -16, 80);
```

Then update the `new OresConfig(...)` call inside `defaults()` to pass the new constant as the fourth argument:

```java
                new OresConfig(
                    TIME_CRYSTAL_DEFAULTS,
                    ENTROPY_CRYSTAL_DEFAULTS,
                    TEMPORAL_AMBER_DEFAULTS,
                    CLOCKSTONE_DEFAULTS
                )
```

- [ ] **Step 3: Add the clockstone key constant and fourth `parseOre` call in `ConfigLoader`**

In `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`:

Add a new key constant alongside the existing ore-key constants (immediately after `K_TEMPORAL_AMBER` near line 64):

```java
    private static final String K_CLOCKSTONE = "clockstone";
```

Update `parseOres()` to return the four-field record:

```java
    private static com.chronodawn.config.OresConfig parseOres(CommentedConfig parsed) {
        return new com.chronodawn.config.OresConfig(
            parseOre(parsed, K_TIME_CRYSTAL, ConfigDefaults.TIME_CRYSTAL_DEFAULTS),
            parseOre(parsed, K_ENTROPY_CRYSTAL, ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS),
            parseOre(parsed, K_TEMPORAL_AMBER, ConfigDefaults.TEMPORAL_AMBER_DEFAULTS),
            parseOre(parsed, K_CLOCKSTONE, ConfigDefaults.CLOCKSTONE_DEFAULTS)
        );
    }
```

No other changes to `ConfigLoader` are needed — `parseOre()` already takes an `OreSettings defaults` argument and is reused verbatim.

- [ ] **Step 4: Add CLOCKSTONE_PATH and the fourth `generate()` entry in the overlay emitter**

In `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java`:

Add a fourth path constant immediately below the existing `TEMPORAL_AMBER_PATH` (around line 50):

```java
    public static final String CLOCKSTONE_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_clockstone.json";
```

Extend `generate()` to emit clockstone. The final `generate()` should look like:

```java
    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        out.put(TIME_CRYSTAL_PATH, generateOre(
            "ore_time_crystal", "minecraft:trapezoid", config.world().ores().timeCrystal()));
        out.put(ENTROPY_CRYSTAL_PATH, generateOre(
            "ore_entropy_crystal", "minecraft:trapezoid", config.world().ores().entropyCrystal()));
        out.put(TEMPORAL_AMBER_PATH, generateOre(
            "ore_temporal_amber", "minecraft:uniform", config.world().ores().temporalAmber()));
        out.put(CLOCKSTONE_PATH, generateOre(
            "ore_clockstone", "minecraft:trapezoid", config.world().ores().clockstone()));
        return out;
    }
```

No change to the existing `generateOre(String, String, OreSettings)` helper.

- [ ] **Step 5: Update the `withOres` helper in `RuntimePlacedFeatureOverlayTest` to take four arguments**

In `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`, replace the existing private helper near the bottom with:

```java
    private static ChronoDawnConfig withOres(OreSettings tc, OreSettings ec, OreSettings ta, OreSettings clk) {
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        return new ChronoDawnConfig(
            defaults.schemaVersion(),
            new ChronoDawnConfig.World(
                defaults.world().structures(),
                new com.chronodawn.config.OresConfig(tc, ec, ta, clk)
            )
        );
    }
```

Update every existing call to `withOres(...)` in this file to pass `ConfigDefaults.CLOCKSTONE_DEFAULTS` as the fourth argument so behavior stays identical. Concretely:

- `timeCrystalCountOverride_changesCountStepOnly`: `withOres(new OreSettings(true, 10, 0, 48), ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, ConfigDefaults.TEMPORAL_AMBER_DEFAULTS, ConfigDefaults.CLOCKSTONE_DEFAULTS)`
- `entropyCrystalDisabled_emitsCountZeroAndPreservesOtherValues`: `withOres(ConfigDefaults.TIME_CRYSTAL_DEFAULTS, disabled, ConfigDefaults.TEMPORAL_AMBER_DEFAULTS, ConfigDefaults.CLOCKSTONE_DEFAULTS)`
- `temporalAmberCustomYRange_changesHeightBoundsAndKeepsUniform`: `withOres(ConfigDefaults.TIME_CRYSTAL_DEFAULTS, ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, amber, ConfigDefaults.CLOCKSTONE_DEFAULTS)`

- [ ] **Step 6: Compile and run the shared-module unit tests on one version**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL. All existing `RuntimePlacedFeatureOverlayTest` and `ConfigLoaderTest` tests must still pass. The new clockstone code path is exercised only indirectly here (existing tests don't assert clockstone behavior), so a green run proves the structural change does not regress the three existing ores.

If a test fails, do NOT proceed — fix the wiring first.

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/OresConfig.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java \
        common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java \
        common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java
git commit -m "feat(config): wire ore_clockstone into OresConfig and runtime overlay

Adds a fourth OreSettings field (clockstone) to OresConfig with
CLOCKSTONE_DEFAULTS (true, 8, -16, 80) matching the now-uniform
bundled placed_feature JSON. ConfigLoader.parseOres and
RuntimePlacedFeatureOverlay.generate each gain one entry; the
test-helper withOres signature grows a fourth argument.

No new tests yet — existing assertions still pass, proving the
three previously shipped ores are not regressed."
```

---

## Task 3: Add clockstone-specific tests to `RuntimePlacedFeatureOverlayTest`

These tests assert the new code path's behavior. Round-trip equality vs the bundled JSON only works after Task 1 corrected 1.21.1+ to `count=8`.

**Files:**
- Modify: `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`

- [ ] **Step 1: Add the round-trip default-equivalence test**

Append the following test method directly below `defaultConfig_reproducesBundledTemporalAmberJson`:

```java
    @Test
    void defaultConfig_reproducesBundledClockstoneJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH);
        assertNotNull(bytes, "Overlay must contain ore_clockstone.json under expected path");
        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_clockstone.json");
        assertEquals(bundled, generated);
    }
```

- [ ] **Step 2: Add count-override, disabled, and y-range tests**

Append directly after the test added in Step 1:

```java
    @Test
    void clockstoneCountOverride_changesCountStepOnly() {
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS,
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            new OreSettings(true, 20, -16, 80)
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH));
        assertEquals(20, countStep(json).get("count").getAsInt());
    }

    @Test
    void clockstoneDisabled_emitsCountZeroAndPreservesOtherValues() {
        OreSettings disabled = new OreSettings(false, 12, -32, 60);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS,
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            disabled
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH));
        assertEquals(0, countStep(json).get("count").getAsInt(),
            "enabled=false must force the count step to 0");
        JsonObject heightRange = json.getAsJsonArray("placement").get(2).getAsJsonObject();
        assertEquals(-32,
            heightRange.getAsJsonObject("height").getAsJsonObject("min_inclusive").get("absolute").getAsInt(),
            "yMin must be preserved verbatim when disabled");
        assertEquals(60,
            heightRange.getAsJsonObject("height").getAsJsonObject("max_inclusive").get("absolute").getAsInt(),
            "yMax must be preserved verbatim when disabled");
    }

    @Test
    void clockstoneCustomYRange_changesHeightBoundsAndKeepsTrapezoid() {
        OreSettings clk = new OreSettings(true, 8, -50, 30);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS,
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            clk
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH));
        JsonObject height = json.getAsJsonArray("placement").get(2).getAsJsonObject().getAsJsonObject("height");
        assertEquals(-50, height.getAsJsonObject("min_inclusive").get("absolute").getAsInt());
        assertEquals(30, height.getAsJsonObject("max_inclusive").get("absolute").getAsInt());
        assertEquals("minecraft:trapezoid", height.get("type").getAsString());
    }
```

- [ ] **Step 3: Extend the existing `perOreDistributionType_isFixed` test**

Locate the existing test (around line 124). Add one more assertion at the end of the method:

```java
        assertEquals("minecraft:trapezoid",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH)));
```

- [ ] **Step 4: Run the overlay tests**

Run: `./gradlew :common-1.21.11:test --tests com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL. The four new methods plus the extended `perOreDistributionType_isFixed` all pass; the seven previously existing methods still pass.

Also spot-check 1.20.1 to confirm cross-version round-trip:

Run: `./gradlew :common-1.20.1:test --tests com.chronodawn.worldgen.runtime.RuntimePlacedFeatureOverlayTest -Ptarget_mc_version=1.20.1`
Expected: BUILD SUCCESSFUL.

If `defaultConfig_reproducesBundledClockstoneJson` fails on either version, the most likely cause is a typo in Task 1's JSON edit; re-verify the bundled file has `"count": 8`.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java
git commit -m "test(config): assert clockstone overlay emission against bundled JSON

Round-trip equality, count override, disabled state, custom y range,
and per-ore distribution type for ore_clockstone. Mirrors the three
ore tests shipped in the previous slice."
```

---

## Task 4: Add clockstone-specific tests to `ConfigLoaderTest`

**Files:**
- Modify: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java`

- [ ] **Step 1: Extend `oresDefault_allFieldsMatchDefaults` to assert clockstone**

Locate the existing method (around line 195). Append one assertion after the temporal_amber one:

```java
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS, ores.clockstone());
```

- [ ] **Step 2: Add `validClockstoneCustom_isReturnedVerbatim`**

Append after the existing `validOreCustom_isReturnedVerbatim` test:

```java
    @Test
    void validClockstoneCustom_isReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.clockstone]\n" +
            "enabled = false\n" +
            "count = 12\n" +
            "y_min = -8\n" +
            "y_max = 64\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings clk = config.world().ores().clockstone();
        assertEquals(false, clk.enabled());
        assertEquals(12, clk.count());
        assertEquals(-8, clk.yMin());
        assertEquals(64, clk.yMax());
        // Other ores untouched
        assertEquals(ConfigDefaults.TIME_CRYSTAL_DEFAULTS, config.world().ores().timeCrystal());
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, config.world().ores().entropyCrystal());
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS, config.world().ores().temporalAmber());
    }
```

- [ ] **Step 3: Add `invalidClockstoneCount_overMax_revertsCount`**

Append directly after the test from Step 2:

```java
    @Test
    void invalidClockstoneCount_overMax_revertsCount(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.clockstone]\n" +
            "count = 999\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS.count(),
            config.world().ores().clockstone().count());
    }
```

- [ ] **Step 4: Add `clockstoneInvertedYRange_revertsBothYFieldsOnly`**

Append directly after the test from Step 3:

```java
    @Test
    void clockstoneInvertedYRange_revertsBothYFieldsOnly(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.clockstone]\n" +
            "count = 11\n" +
            "y_min = 100\n" +
            "y_max = 50\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings clk = config.world().ores().clockstone();
        assertEquals(11, clk.count(), "Valid count survives an inverted-Y revert");
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS.yMin(), clk.yMin());
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS.yMax(), clk.yMax());
    }
```

- [ ] **Step 5: Run the loader tests**

Run: `./gradlew :common-1.21.11:test --tests com.chronodawn.unit.ConfigLoaderTest -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL. The four new clockstone assertions pass; all previously existing tests still pass.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java
git commit -m "test(config): cover clockstone TOML parsing and validation

Default round-trip plus three per-field revert cases (count over max,
inverted y range, verbatim custom values). Mirrors the existing
time_crystal/entropy_crystal/temporal_amber coverage."
```

---

## Task 5: Append `[world.ores.clockstone]` to the bundled default TOML

**Files:**
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml`

- [ ] **Step 1: Append the new section**

Append the following block at the end of the file (after the existing `[world.ores.temporal_amber]` block, line 58 is currently the last `y_max = 20`):

```toml

[world.ores.clockstone]
# Whether Clockstone Ore generates in the Chrono dimension.
enabled = true

# Number of placement attempts per chunk. Lower = rarer, higher = denser.
# Each attempt generates one cluster (cluster size is fixed at the data-pack level).
count = 8

# Minimum and maximum Y level (absolute) where Clockstone Ore can spawn.
# Distribution between min and max is biased toward the middle (trapezoid).
y_min = -16
y_max = 80
```

(Note the leading blank line — every existing ore section is separated by one blank line.)

- [ ] **Step 2: Smoke-test that the unit suite still passes**

Run: `./gradlew :common-1.21.11:test --tests com.chronodawn.unit.ConfigLoaderTest -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL. The bundled TOML is not exercised by any existing test (every loader test writes its own TOML to a `@TempDir`), so a malformed bundled file would only surface at runtime on first install. This step is a low-cost regression check; the authoritative validation of the bundled file is the manual integration step in Task 10.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/chronodawn-default-config.toml
git commit -m "feat(config): document [world.ores.clockstone] in default TOML

Adds the bundled commented section so users see the clockstone knobs
on first install. Values match CLOCKSTONE_DEFAULTS."
```

---

## Task 6: Update `docs/configuration.md` to cover clockstone

**Files:**
- Modify: `docs/configuration.md`

- [ ] **Step 1: Update the `[world.ores.*]` section intro**

Replace the existing paragraph (currently at lines 114–117) and the `[!NOTE]` callout (lines 119–122):

Current:
```
Per-ore generation tuning for the three ChronoDawn-specific ores in the
Chrono dimension: **Time Crystal**, **Entropy Crystal**, and **Temporal
Amber**. Each ore has its own table; missing sections fall back to the
defaults listed below.

> [!NOTE]
> `ore_clockstone` and the vanilla-overlay ores (`iron` / `gold` / `coal`
> / `redstone`) are intentionally not exposed yet — see the design spec
> for context.
```

Replace with:
```
Per-ore generation tuning for the four ChronoDawn-specific ores in the
Chrono dimension: **Time Crystal**, **Entropy Crystal**, **Temporal
Amber**, and **Clockstone**. Each ore has its own table; missing
sections fall back to the defaults listed below.

> [!NOTE]
> The vanilla-overlay ores (`iron` / `gold` / `coal` / `redstone`) in the
> Chrono dimension are intentionally not exposed yet — see the design
> spec for context.
```

- [ ] **Step 2: Extend the TOML example block to include clockstone**

In the same section, append the clockstone block to the existing fenced TOML example (currently lines 124–142). The combined block should read:

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

[world.ores.clockstone]
enabled = true
count = 8
y_min = -16
y_max = 80
```

- [ ] **Step 3: Extend the per-field defaults table**

Replace the existing table (lines 144–149) with the four-column version covering clockstone. The full table:

```
| Field | Type | Default (TC / EC / TA / CS) | Range | Notes |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | `true` for all four | — | When `false`, the placed feature stays registered but emits `count = 0` so nothing generates. Your other values are preserved verbatim and restored on re-enable. |
| `count` | integer | `3` / `4` / `4` / `8` | `0..=64` | Number of placement attempts per chunk. Lower = rarer. Cluster size (blocks per attempt) is not exposed here. |
| `y_min` | integer | `0` / `40` / `-30` / `-16` | `-64..=y_max` | Absolute minimum Y for placement. |
| `y_max` | integer | `48` / `100` / `20` / `80` | `y_min..=320` | Absolute maximum Y for placement. |
```

- [ ] **Step 4: Update the distribution-type bullet list**

Replace the existing two bullets (currently lines 154–157):

Current:
```
- Time Crystal and Entropy Crystal use `minecraft:trapezoid` (concentrated
  in the middle of the range).
- Temporal Amber uses `minecraft:uniform` (flat probability across the
  range).
```

Replace with:
```
- Time Crystal, Entropy Crystal, and Clockstone use `minecraft:trapezoid`
  (concentrated in the middle of the range).
- Temporal Amber uses `minecraft:uniform` (flat probability across the
  range).
```

- [ ] **Step 5: Add a clockstone usage example**

Insert this block immediately before the `Example: disable Time Crystal entirely` heading (currently around line 178):

```
#### Example: make Clockstone rare

```toml
[world.ores.clockstone]
count = 2
```

This roughly quarters tier-1 abundance versus the default `count = 8`.
```

- [ ] **Step 6: Commit**

```bash
git add docs/configuration.md
git commit -m "docs(config): cover [world.ores.clockstone] in configuration guide

Updates the intro, TOML example, defaults table, and distribution
list to include the new ore. Adds a 'make Clockstone rare' example
alongside the existing per-ore examples."
```

---

## Task 7: Add a clockstone snippet to `docs/modpack-integration.md`

**Files:**
- Modify: `docs/modpack-integration.md`

- [ ] **Step 1: Extend the "rebalance a single ore" example with a clockstone snippet**

Locate `### Example: rebalance a single ore` (currently line 33). Append a second example immediately below the existing entropy_crystal snippet (after the closing fence at line 41):

```

### Example: lower Clockstone tier-1 abundance

```toml
# Halve Clockstone density. Tier-1 progression slows; pairs well with
# raising the bundled Time Crystal / Entropy Crystal counts elsewhere.
[world.ores.clockstone]
count = 4
```
```

(Then keep the existing paragraph that begins "Bundle this file via `overrides/config/chronodawn.toml`" unchanged.)

- [ ] **Step 2: Commit**

```bash
git add docs/modpack-integration.md
git commit -m "docs(modpack): show clockstone rebalance example

Adds a second worked example alongside the existing entropy_crystal
snippet so pack authors see the new knob in the integration guide."
```

---

## Task 8: Update `CHANGELOG.md`

**Files:**
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Extend the `### Added` block under `[Unreleased]`**

Find the existing Configuration entry under `[Unreleased]` → `### Added` (around line 14):

```
- **Ore generation tuning** for Time Crystal, Entropy Crystal, and Temporal Amber — exposes `enabled`, `count`, `y_min`, `y_max` per ore via `config/chronodawn.toml`. See [docs/configuration.md](docs/configuration.md#worldores).
```

Append a new bullet immediately after it:

```
- **Clockstone ore generation tuning** — exposes `enabled`, `count`, `y_min`, `y_max` for `chronodawn:ore_clockstone` via `config/chronodawn.toml`, completing the per-ore tuning surface for the four ChronoDawn-specific ores. See [docs/configuration.md](docs/configuration.md#worldores).
```

- [ ] **Step 2: Add a `### Fixed` entry under `[Unreleased]`**

Find the `### Fixed` heading under `[Unreleased]` (around line 72). Append this bullet at the end of that section:

```
- **`ore_clockstone` count on 1.21.1+ corrected from 16 to 8** to match the 2026-01 frequency reduction (`90413a2e`) that was originally applied only to the 1.20.1 placed_feature. Brings 1.21.1+ in line with the documented design intent (clockstone density comparable to vanilla iron). Existing worlds keep their already-generated chunks; only newly-generated chunks reflect the change.
```

- [ ] **Step 3: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs(changelog): record clockstone config tuning and 1.21.1+ count fix"
```

---

## Task 9: Update the Modpack-Author Readiness roadmap

**Files:**
- Modify: `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`

- [ ] **Step 1: Update sub-project A status note**

Replace line 25 (status sentence under "A. Config system"):

Current:
```
**Status**: 🚧 Two PRs shipped: Ancient Ruins ([design](./2026-05-09-config-foundation-design.md), [plan](../plans/2026-05-09-config-foundation.md)) and Ore generation tuning for Time Crystal / Entropy Crystal / Temporal Amber ([design](./2026-05-16-ore-generation-tuning-design.md), [plan](../plans/2026-05-16-ore-generation-tuning.md)). Continuing with follow-up tunables.
```

Replace with:
```
**Status**: 🚧 Three PRs shipped: Ancient Ruins ([design](./2026-05-09-config-foundation-design.md), [plan](../plans/2026-05-09-config-foundation.md)), Ore generation tuning for Time Crystal / Entropy Crystal / Temporal Amber ([design](./2026-05-16-ore-generation-tuning-design.md), [plan](../plans/2026-05-16-ore-generation-tuning.md)), and Clockstone tuning ([design](./2026-05-17-ore-clockstone-config-tuning-design.md), [plan](../plans/2026-05-17-ore-clockstone-config-tuning.md)). Continuing with follow-up tunables.
```

- [ ] **Step 2: Update the ore line under Planned follow-up tunables**

Replace line 32:

Current:
```
- Ore generation rates and Y ranges — *partial: Time Crystal / Entropy Crystal / Temporal Amber shipped. `ore_clockstone` deferred (needs version-aware defaults; bundled count differs across MC versions). Vanilla-overlay ores in the Chrono dimension also deferred.*
```

Replace with:
```
- Ore generation rates and Y ranges — *partial: Time Crystal / Entropy Crystal / Temporal Amber / Clockstone shipped. Vanilla-overlay ores in the Chrono dimension (`iron` / `gold` / `coal` / `redstone`) remain deferred — will be reconsidered if a request surfaces.*
```

- [ ] **Step 3: Update the status tracker table**

Replace line 71:

Current:
```
| A. Config system | 🚧 Two PRs shipped (Ancient Ruins, Ore tuning) | [2026-05-09-config-foundation-design.md](./2026-05-09-config-foundation-design.md) |
```

Replace with:
```
| A. Config system | 🚧 Three PRs shipped (Ancient Ruins, Ore tuning, Clockstone tuning) | [2026-05-09-config-foundation-design.md](./2026-05-09-config-foundation-design.md) |
```

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md
git commit -m "docs(roadmap): record clockstone tuning slice on Config system sub-project"
```

---

## Task 10: Full cross-version verification

**Files:** None.

- [ ] **Step 1: Run the full `checkAll` pipeline**

Run: `./gradlew checkAll`
Expected: BUILD SUCCESSFUL. This is the authoritative verification — it runs `cleanAll` → `validateResources` → `validateTranslations` → `buildAll` → `testAll` → `gameTestAll` across every supported version (1.20.1 + 1.21.1 + 1.21.2 + 1.21.4 + 1.21.5 + 1.21.6 + 1.21.7 + 1.21.8 + 1.21.9 + 1.21.10 + 1.21.11; 1.21.3 reuses 1.21.2 modules).

Watch in particular for failures of:
- `RuntimePlacedFeatureOverlayTest.defaultConfig_reproducesBundledClockstoneJson` on any 1.21.x version → would indicate Task 1's JSON edit was wrong.
- Any `ConfigLoaderTest` failure on any version → would indicate a `parseOres` regression.

If `checkAll` is too slow to iterate on, the minimal safe subset for a clockstone-specific spot-check is:
- `./gradlew :common-1.20.1:test -Ptarget_mc_version=1.20.1` (proves the 1.20.1 round-trip)
- `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11` (proves the 1.21.1+ round-trip via the shared resource)
- `./gradlew testAll` (full unit-test sweep without GameTest)

But the final commit / merge candidate must pass `checkAll`.

- [ ] **Step 2: Manual integration spot-check (optional but recommended)**

On **1.21.11 Fabric**:
1. `./gradlew runClientFabric1_21_11`
2. Create a new world, enter the Chrono dimension. Confirm clockstone now generates at the lower density (~half of the previous build). This is the regression check for the 1.21.1+ JSON fix.
3. Quit client. Edit `config/chronodawn.toml` → `clockstone.count = 20`. Restart. Generate a new chunk. Confirm visibly higher clockstone density.
4. Quit client. Edit `clockstone.enabled = false`. Restart. Confirm no clockstone appears in newly-generated chunks.

Repeat on **1.21.1 Fabric** (`./gradlew runClientFabric1_21_1`) — the cross-version manual-verification pair established by the previous ore PR.

No client run is required on 1.20.1 because that version already had `count=8`; the regression risk is on 1.21.1+ only.

- [ ] **Step 3: Surface results to the user**

Do NOT push or open a PR autonomously. Report `checkAll` status and any manual-test observations, then wait for the user to decide on commit / PR follow-up.
