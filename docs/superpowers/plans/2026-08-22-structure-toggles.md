# Per-Structure Generation Configuration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let modpack authors disable or retune all eight Chrono Dawn structures through `chronodawn.toml`, not just Ancient Ruins.

**Architecture:** Generalise the existing `ChronoDawnConfig.AncientRuins` record into a shared `StructureSettings`, and introduce a `ManagedStructure` enum that is the single source of truth for the set of eight structures (TOML key, structure ID, pack path, accessor, defaults, progression note). `ConfigLoader`, `RuntimeStructureOverlay` and the guard tests all iterate that enum, so a structure cannot be wired into one and forgotten in another. Generation reuses the existing runtime data-pack overlay; nothing new is needed on the loader side.

**Tech Stack:** Java 21, night-config (TOML), Gson (tests only), JUnit 5. All production code lives in `common/shared` (version-agnostic, no Minecraft classes in the config package).

**Spec:** `docs/superpowers/specs/2026-08-22-structure-toggles-design.md`

## Global Constraints

- Production code goes in `common/shared/src/main/java/com/chronodawn/config/` and `.../worldgen/runtime/`. Do not touch per-version `common/<mcversion>/` modules — this feature has no version-specific surface.
- Every new file starts with the LGPL-3.0 header block copied verbatim from `common/shared/src/main/java/com/chronodawn/config/PortalSettings.java`.
- Comments and documentation in English.
- Defaults must be a no-op: an untouched config must reproduce today's generation exactly.
- Per-key fallback semantics: an invalid value reverts that key alone, logs at `ERROR`, and never discards a sibling key, another structure, or the whole section.
- `spacing` valid range `1..=4096`; `separation` valid range `0..spacing` (exclusive upper bound). Constants `MIN_SPACING`, `MAX_SPACING`, `MIN_SEPARATION` already exist in `ConfigLoader`.
- Do not bump `schema_version`; earlier tunables shipped without a bump.
- The eight structures and their shipped defaults (from `common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/*.json`):

  | TOML key | structure ID | spacing | separation | salt | progression note |
  | --- | --- | --- | --- | --- | --- |
  | `ancient_ruins` | `chronodawn:ancient_ruins` | 56 | 20 | 20005897 | *(none)* |
  | `forgotten_library` | `chronodawn:forgotten_library` | 30 | 15 | 8735421890 | the Portal Stabilizer recipe |
  | `desert_clock_tower` | `chronodawn:desert_clock_tower` | 30 | 10 | 1663542342 | Time Guardian, the Master Clock Key and Enhanced Clockstone |
  | `guardian_vault` | `chronodawn:guardian_vault` | 48 | 24 | 928374651 | Chronos Warden and the Guardian Stone |
  | `clockwork_depths` | `chronodawn:clockwork_depths` | 56 | 28 | 837465129 | Clockwork Colossus and the Colossus Gear |
  | `phantom_catacombs` | `chronodawn:phantom_catacombs` | 20 | 8 | 745182936 | Temporal Phantom and the Phantom Essence |
  | `entropy_crypt` | `chronodawn:entropy_crypt` | 50 | 25 | 738291456 | Entropy Keeper and the Entropy Core |
  | `master_clock` | `chronodawn:master_clock` | 60 | 20 | 1234567890 | Time Tyrant, the final boss |

  **Note on `forgotten_library`'s salt:** `8735421890` exceeds `Integer.MAX_VALUE`. The existing loader reads salt via `Number::intValue`, which silently truncates. Task 2 preserves the shipped JSON value as a `long` field in the enum defaults so the guard test in Task 3 can compare against the bundled JSON without modifying resources. See Task 2 Step 1 for the exact handling.

- Verification command for a single version (fast loop):
  `export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"; ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '<pattern>'`
- Final verification before the last commit: `./gradlew checkAll` with the same `JAVA_HOME` exports, run with `dangerouslyDisableSandbox: true`.

---

### Task 1: Introduce `StructureSettings` and reshape `Structures`

Replaces the single-purpose `ChronoDawnConfig.AncientRuins` record with a reusable one and widens `Structures` to eight fields. Behavior is unchanged: this task is a pure rename/widen that keeps Ancient Ruins working and gives the other seven structures their shipped defaults.

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/config/StructureSettings.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java` (the `Structures` and `AncientRuins` records, around lines 55-59)
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java` (the `ANCIENT_RUINS_*` constants around lines 29-33, and the `defaults()` factory)
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java` (`parseAncientRuins` return type and the `Structures` construction, around lines 159-221)
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeStructureOverlay.java` (`generateAncientRuins` parameter type)
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java` (existing Ancient Ruins tests must keep passing)

**Interfaces:**
- Consumes: nothing from earlier tasks.
- Produces:
  - `com.chronodawn.config.StructureSettings` — `record StructureSettings(boolean enabled, int spacing, int separation, long salt)` with accessors `enabled()`, `spacing()`, `separation()`, `salt()`.
  - `ChronoDawnConfig.Structures` — canonical constructor `Structures(StructureSettings ancientRuins, StructureSettings forgottenLibrary, StructureSettings desertClockTower, StructureSettings guardianVault, StructureSettings clockworkDepths, StructureSettings phantomCatacombs, StructureSettings entropyCrypt, StructureSettings masterClock)`, plus a one-argument convenience constructor `Structures(StructureSettings ancientRuins)` that fills the remaining seven from `ConfigDefaults`.
  - `ConfigDefaults.ANCIENT_RUINS_DEFAULTS`, `FORGOTTEN_LIBRARY_DEFAULTS`, `DESERT_CLOCK_TOWER_DEFAULTS`, `GUARDIAN_VAULT_DEFAULTS`, `CLOCKWORK_DEPTHS_DEFAULTS`, `PHANTOM_CATACOMBS_DEFAULTS`, `ENTROPY_CRYPT_DEFAULTS`, `MASTER_CLOCK_DEFAULTS` — all `public static final StructureSettings`.

- [ ] **Step 1: Write the failing test**

Add to `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java` (append inside the class, before the closing brace):

```java
    @Test
    void structures_defaults_carryEveryShippedStructure() {
        ChronoDawnConfig.Structures structures = ConfigDefaults.defaults().world().structures();

        assertEquals(new StructureSettings(true, 56, 20, 20005897L), structures.ancientRuins());
        assertEquals(new StructureSettings(true, 30, 15, 8735421890L), structures.forgottenLibrary());
        assertEquals(new StructureSettings(true, 30, 10, 1663542342L), structures.desertClockTower());
        assertEquals(new StructureSettings(true, 48, 24, 928374651L), structures.guardianVault());
        assertEquals(new StructureSettings(true, 56, 28, 837465129L), structures.clockworkDepths());
        assertEquals(new StructureSettings(true, 20, 8, 745182936L), structures.phantomCatacombs());
        assertEquals(new StructureSettings(true, 50, 25, 738291456L), structures.entropyCrypt());
        assertEquals(new StructureSettings(true, 60, 20, 1234567890L), structures.masterClock());
    }
```

Add the import `import com.chronodawn.config.StructureSettings;` alongside the existing config imports at the top of the file.

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*'
```
Expected: FAIL — compilation error, `StructureSettings` does not exist.

- [ ] **Step 3: Create `StructureSettings`**

Create `common/shared/src/main/java/com/chronodawn/config/StructureSettings.java` with the LGPL header block copied from `PortalSettings.java`, then:

```java
package com.chronodawn.config;

/**
 * Placement settings for one structure set.
 *
 * <p>{@code salt} is a {@code long} because one shipped structure set
 * ({@code forgotten_library}) uses a value above {@link Integer#MAX_VALUE}.
 * Vanilla reads salt as an int and truncates; keeping the full value here lets
 * the runtime overlay reproduce the bundled JSON byte-for-byte.
 */
public record StructureSettings(boolean enabled, int spacing, int separation, long salt) {}
```

- [ ] **Step 4: Reshape `ChronoDawnConfig.Structures`**

In `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java`, delete the `AncientRuins` record and replace the `Structures` record with:

```java
    public record Structures(
        StructureSettings ancientRuins,
        StructureSettings forgottenLibrary,
        StructureSettings desertClockTower,
        StructureSettings guardianVault,
        StructureSettings clockworkDepths,
        StructureSettings phantomCatacombs,
        StructureSettings entropyCrypt,
        StructureSettings masterClock
    ) {
        /** Preserve the existing construction pattern for callers that only configure Ancient Ruins. */
        public Structures(StructureSettings ancientRuins) {
            this(
                ancientRuins,
                ConfigDefaults.FORGOTTEN_LIBRARY_DEFAULTS,
                ConfigDefaults.DESERT_CLOCK_TOWER_DEFAULTS,
                ConfigDefaults.GUARDIAN_VAULT_DEFAULTS,
                ConfigDefaults.CLOCKWORK_DEPTHS_DEFAULTS,
                ConfigDefaults.PHANTOM_CATACOMBS_DEFAULTS,
                ConfigDefaults.ENTROPY_CRYPT_DEFAULTS,
                ConfigDefaults.MASTER_CLOCK_DEFAULTS
            );
        }
    }
```

- [ ] **Step 5: Replace the Ancient Ruins constants in `ConfigDefaults`**

In `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`, replace the four `ANCIENT_RUINS_*` constants with:

```java
    // Defaults mirror common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/*.json
    // Asserted by RuntimeStructureOverlayTest (tree equality against the bundled JSONs).
    public static final StructureSettings ANCIENT_RUINS_DEFAULTS = new StructureSettings(true, 56, 20, 20005897L);
    public static final StructureSettings FORGOTTEN_LIBRARY_DEFAULTS = new StructureSettings(true, 30, 15, 8735421890L);
    public static final StructureSettings DESERT_CLOCK_TOWER_DEFAULTS = new StructureSettings(true, 30, 10, 1663542342L);
    public static final StructureSettings GUARDIAN_VAULT_DEFAULTS = new StructureSettings(true, 48, 24, 928374651L);
    public static final StructureSettings CLOCKWORK_DEPTHS_DEFAULTS = new StructureSettings(true, 56, 28, 837465129L);
    public static final StructureSettings PHANTOM_CATACOMBS_DEFAULTS = new StructureSettings(true, 20, 8, 745182936L);
    public static final StructureSettings ENTROPY_CRYPT_DEFAULTS = new StructureSettings(true, 50, 25, 738291456L);
    public static final StructureSettings MASTER_CLOCK_DEFAULTS = new StructureSettings(true, 60, 20, 1234567890L);
```

In the `defaults()` factory, replace the `new ChronoDawnConfig.Structures(new ChronoDawnConfig.AncientRuins(...))` expression with:

```java
                new ChronoDawnConfig.Structures(
                    ANCIENT_RUINS_DEFAULTS,
                    FORGOTTEN_LIBRARY_DEFAULTS,
                    DESERT_CLOCK_TOWER_DEFAULTS,
                    GUARDIAN_VAULT_DEFAULTS,
                    CLOCKWORK_DEPTHS_DEFAULTS,
                    PHANTOM_CATACOMBS_DEFAULTS,
                    ENTROPY_CRYPT_DEFAULTS,
                    MASTER_CLOCK_DEFAULTS
                ),
```

- [ ] **Step 6: Retarget `ConfigLoader` and `RuntimeStructureOverlay` to the new type**

In `ConfigLoader.java`:
- change `parseAncientRuins`'s return type from `ChronoDawnConfig.AncientRuins` to `StructureSettings`, and its final statement to `return new StructureSettings(enabled, spacing, separation, salt);`
- change the local variable at the call site (around line 159) from `ChronoDawnConfig.AncientRuins ancientRuins` to `StructureSettings ancientRuins`
- change `int salt = parsed.<Number>getOptional(...).map(Number::intValue).orElse(ConfigDefaults.ANCIENT_RUINS_SALT);` to `long salt = parsed.<Number>getOptional(path + "." + K_AR_SALT).map(Number::longValue).orElse(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.salt());`
- replace the remaining `ConfigDefaults.ANCIENT_RUINS_ENABLED` / `_SPACING` / `_SEPARATION` references with `ConfigDefaults.ANCIENT_RUINS_DEFAULTS.enabled()` / `.spacing()` / `.separation()`

In `RuntimeStructureOverlay.java`, change `generateAncientRuins(ChronoDawnConfig.AncientRuins ar)` to `generateAncientRuins(StructureSettings ar)` and add `import com.chronodawn.config.StructureSettings;`. The method body is unchanged.

- [ ] **Step 7: Run the tests to verify they pass**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*' --tests '*RuntimeStructureOverlayTest*'
```
Expected: PASS, including the pre-existing Ancient Ruins tests — this task must not change behavior.

- [ ] **Step 8: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeStructureOverlay.java common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java
git commit -m "refactor(config): generalize Ancient Ruins settings to StructureSettings"
```

---

### Task 2: Add the `ManagedStructure` enum

The single source of truth for the set of eight. No consumer changes yet — this task only introduces the enum and its coverage test, so a reviewer can gate the table's correctness on its own.

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/config/ManagedStructure.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ManagedStructureTest.java`

**Interfaces:**
- Consumes: `StructureSettings` and the eight `ConfigDefaults.*_DEFAULTS` constants from Task 1.
- Produces: `com.chronodawn.config.ManagedStructure`, an enum with eight constants and these instance methods:
  - `String configKey()` — the TOML key, e.g. `"master_clock"`
  - `String structureId()` — e.g. `"chronodawn:master_clock"`
  - `String packPath()` — e.g. `"data/chronodawn/worldgen/structure_set/master_clock.json"`
  - `StructureSettings defaults()`
  - `String progressionNote()` — empty string for `ANCIENT_RUINS`
  - `StructureSettings settingsOf(ChronoDawnConfig.Structures structures)`

- [ ] **Step 1: Write the failing test**

Create `common/shared/src/test/java/com/chronodawn/unit/ManagedStructureTest.java` with the LGPL header, then:

```java
package com.chronodawn.unit;

import com.chronodawn.ChronoDawn;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ManagedStructure;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards {@link ManagedStructure} as the single source of truth for configurable structures.
 *
 * <p>A structure that ships a structure_set but is missing from the enum would be
 * silently unconfigurable, and an enum constant with no structure_set would emit an
 * overlay file that overrides nothing. Both directions are checked here.
 */
class ManagedStructureTest {

    private static final Path STRUCTURE_SET_DIR = Paths.get(
        TestUtils.getProjectRoot(),
        "common", "shared", "src", "main", "resources",
        "data", "chronodawn", "worldgen", "structure_set"
    );

    @Test
    void enumCoversEveryShippedStructureSet() {
        File[] files = STRUCTURE_SET_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertTrue(files != null && files.length > 0,
            "No structure_set JSON found under " + STRUCTURE_SET_DIR);

        Set<String> shipped = Arrays.stream(files)
            .map(f -> f.getName().replace(".json", ""))
            .collect(Collectors.toCollection(TreeSet::new));
        Set<String> registered = Arrays.stream(ManagedStructure.values())
            .map(ManagedStructure::configKey)
            .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(shipped, registered,
            "Every structure_set must be registered in ManagedStructure and vice versa");
    }

    @Test
    void packPathAndStructureIdFollowTheConfigKey() {
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertEquals(ChronoDawn.MOD_ID + ":" + structure.configKey(), structure.structureId(),
                structure.name());
            assertEquals(
                "data/" + ChronoDawn.MOD_ID + "/worldgen/structure_set/" + structure.configKey() + ".json",
                structure.packPath(),
                structure.name());
        }
    }

    @Test
    void settingsOfReturnsTheMatchingField() {
        ChronoDawnConfig.Structures defaults = ConfigDefaults.defaults().world().structures();
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertEquals(structure.defaults(), structure.settingsOf(defaults),
                structure.name() + ": settingsOf must read the field its defaults describe");
        }
    }

    @Test
    void onlyAncientRuinsHasNoProgressionNote() {
        for (ManagedStructure structure : ManagedStructure.values()) {
            if (structure == ManagedStructure.ANCIENT_RUINS) {
                assertTrue(structure.progressionNote().isEmpty(),
                    "Ancient Ruins is Overworld flavour and must carry no progression note");
            } else {
                assertFalse(structure.progressionNote().isEmpty(),
                    structure.name() + " gates progression and must explain what disabling it costs");
            }
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ManagedStructureTest*'
```
Expected: FAIL — compilation error, `ManagedStructure` does not exist.

- [ ] **Step 3: Create the enum**

Create `common/shared/src/main/java/com/chronodawn/config/ManagedStructure.java` with the LGPL header, then:

```java
package com.chronodawn.config;

import com.chronodawn.ChronoDawn;

import java.util.function.Function;

/**
 * Every structure whose placement Chrono Dawn exposes to configuration.
 *
 * <p>This enum is the single source of truth for that set: the config parser, the
 * runtime overlay and the guard tests all iterate it. A structure that is not
 * registered here appears in none of them, and one that is registered appears in
 * all of them — so a structure cannot be half-wired.
 */
public enum ManagedStructure {
    ANCIENT_RUINS("ancient_ruins", ConfigDefaults.ANCIENT_RUINS_DEFAULTS,
        ChronoDawnConfig.Structures::ancientRuins,
        ""),
    FORGOTTEN_LIBRARY("forgotten_library", ConfigDefaults.FORGOTTEN_LIBRARY_DEFAULTS,
        ChronoDawnConfig.Structures::forgottenLibrary,
        "the Portal Stabilizer recipe"),
    DESERT_CLOCK_TOWER("desert_clock_tower", ConfigDefaults.DESERT_CLOCK_TOWER_DEFAULTS,
        ChronoDawnConfig.Structures::desertClockTower,
        "Time Guardian, the Master Clock Key and Enhanced Clockstone"),
    GUARDIAN_VAULT("guardian_vault", ConfigDefaults.GUARDIAN_VAULT_DEFAULTS,
        ChronoDawnConfig.Structures::guardianVault,
        "Chronos Warden and the Guardian Stone"),
    CLOCKWORK_DEPTHS("clockwork_depths", ConfigDefaults.CLOCKWORK_DEPTHS_DEFAULTS,
        ChronoDawnConfig.Structures::clockworkDepths,
        "Clockwork Colossus and the Colossus Gear"),
    PHANTOM_CATACOMBS("phantom_catacombs", ConfigDefaults.PHANTOM_CATACOMBS_DEFAULTS,
        ChronoDawnConfig.Structures::phantomCatacombs,
        "Temporal Phantom and the Phantom Essence"),
    ENTROPY_CRYPT("entropy_crypt", ConfigDefaults.ENTROPY_CRYPT_DEFAULTS,
        ChronoDawnConfig.Structures::entropyCrypt,
        "Entropy Keeper and the Entropy Core"),
    MASTER_CLOCK("master_clock", ConfigDefaults.MASTER_CLOCK_DEFAULTS,
        ChronoDawnConfig.Structures::masterClock,
        "Time Tyrant, the final boss");

    private final String configKey;
    private final StructureSettings defaults;
    private final Function<ChronoDawnConfig.Structures, StructureSettings> accessor;
    private final String progressionNote;

    ManagedStructure(
        String configKey,
        StructureSettings defaults,
        Function<ChronoDawnConfig.Structures, StructureSettings> accessor,
        String progressionNote
    ) {
        this.configKey = configKey;
        this.defaults = defaults;
        this.accessor = accessor;
        this.progressionNote = progressionNote;
    }

    /** The TOML table name under {@code [world.structures]}. */
    public String configKey() {
        return configKey;
    }

    /** The registry ID of the structure this set places. */
    public String structureId() {
        return ChronoDawn.MOD_ID + ":" + configKey;
    }

    /** The data-pack-relative path of the structure_set JSON the overlay replaces. */
    public String packPath() {
        return "data/" + ChronoDawn.MOD_ID + "/worldgen/structure_set/" + configKey + ".json";
    }

    /** The shipped placement values, matching the bundled JSON. */
    public StructureSettings defaults() {
        return defaults;
    }

    /** What becomes unobtainable if this structure is disabled; empty when nothing does. */
    public String progressionNote() {
        return progressionNote;
    }

    /** Reads this structure's settings out of a parsed config. */
    public StructureSettings settingsOf(ChronoDawnConfig.Structures structures) {
        return accessor.apply(structures);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ManagedStructureTest*'
```
Expected: PASS, all four tests.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ManagedStructure.java common/shared/src/test/java/com/chronodawn/unit/ManagedStructureTest.java
git commit -m "feat(config): add ManagedStructure as the structure registry"
```

---

### Task 3: Generate every structure_set from the overlay

Widen `RuntimeStructureOverlay` from one hardcoded file to all eight, driven by the enum. The guard here is tree equality against the bundled JSONs, matching how `RuntimePlacedFeatureOverlayTest` guards the ore overlay.

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeStructureOverlay.java`
- Test: `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimeStructureOverlayTest.java` (note: this file lives under `worldgen/runtime`, not `unit` — check both directories before editing)

**Interfaces:**
- Consumes: `ManagedStructure` (Task 2), `StructureSettings` (Task 1).
- Produces: `RuntimeStructureOverlay.generate(ChronoDawnConfig)` returning a map with one entry per `ManagedStructure`, keyed by `packPath()`. `ANCIENT_RUINS_PATH` stays as a public constant for existing callers and tests. New package-private helper `static byte[] generateStructureSet(ManagedStructure structure, StructureSettings settings)`.

- [ ] **Step 1: Write the failing test**

First locate the existing test file:

```bash
find common -name "RuntimeStructureOverlayTest.java" -not -path "*/build/*"
```

Append these tests inside that class, and add `import com.chronodawn.config.ManagedStructure;`:

```java
    @Test
    void defaultConfig_reproducesEveryBundledStructureSet() {
        Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(ConfigDefaults.defaults());

        for (ManagedStructure structure : ManagedStructure.values()) {
            byte[] bytes = overlay.get(structure.packPath());
            assertNotNull(bytes, "Overlay must contain " + structure.packPath());

            JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
            JsonElement bundled = loadBundled(structure.packPath());
            assertEquals(bundled, generated,
                structure.name() + ": default overlay output must be tree-equal to the bundled JSON");
        }
    }

    @Test
    void overlay_containsExactlyTheManagedStructures() {
        Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(ConfigDefaults.defaults());

        Set<String> expected = Arrays.stream(ManagedStructure.values())
            .map(ManagedStructure::packPath)
            .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(expected, new TreeSet<>(overlay.keySet()));
    }

    @Test
    void disablingMasterClock_emptiesOnlyItsStructuresArray() {
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        ChronoDawnConfig.Structures s = defaults.world().structures();
        ChronoDawnConfig config = new ChronoDawnConfig(
            defaults.schemaVersion(),
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    s.ancientRuins(), s.forgottenLibrary(), s.desertClockTower(), s.guardianVault(),
                    s.clockworkDepths(), s.phantomCatacombs(), s.entropyCrypt(),
                    new StructureSettings(false, 60, 20, 1234567890L)
                ),
                defaults.world().ores()
            ),
            defaults.gameplay()
        );

        Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(config);

        JsonObject masterClock = parseObject(overlay.get(ManagedStructure.MASTER_CLOCK.packPath()));
        assertEquals(0, masterClock.getAsJsonArray("structures").size(),
            "Disabled structure must emit an empty structures array");
        assertEquals(60, masterClock.getAsJsonObject("placement").get("spacing").getAsInt(),
            "Disabled structure keeps its placement block");

        JsonObject entropyCrypt = parseObject(overlay.get(ManagedStructure.ENTROPY_CRYPT.packPath()));
        assertEquals(1, entropyCrypt.getAsJsonArray("structures").size(),
            "Disabling one structure must not affect its siblings");
    }
```

Add whatever of these imports the file does not already have: `com.google.gson.JsonElement`, `com.google.gson.JsonObject`, `com.google.gson.JsonParser`, `com.chronodawn.config.ChronoDawnConfig`, `com.chronodawn.config.ConfigDefaults`, `com.chronodawn.config.StructureSettings`, `java.nio.charset.StandardCharsets`, `java.util.Arrays`, `java.util.Map`, `java.util.Set`, `java.util.TreeSet`, `java.util.stream.Collectors`, and the assertions `assertEquals` / `assertNotNull`.

If the file has no `loadBundled` or `parseObject` helper, copy both verbatim from `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`.

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*RuntimeStructureOverlayTest*'
```
Expected: FAIL — the overlay contains only `ancient_ruins.json`, so the map lookups for the other seven return null.

- [ ] **Step 3: Drive generation from the enum**

In `RuntimeStructureOverlay.java`, replace `generate` and `generateAncientRuins` with:

```java
    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        for (ManagedStructure structure : ManagedStructure.values()) {
            out.put(
                structure.packPath(),
                generateStructureSet(structure, structure.settingsOf(config.world().structures()))
            );
        }
        return out;
    }

    static byte[] generateStructureSet(ManagedStructure structure, StructureSettings settings) {
        // Disabled state: keep placement registered (so other systems referencing
        // the ID still find it) but emit no structure variants, so nothing generates.
        String structuresArray = settings.enabled()
            ? "{\n      \"structure\": \"" + structure.structureId() + "\",\n      \"weight\": 1\n    }"
            : "";
        String json =
            "{\n" +
            "  \"structures\": [" + (structuresArray.isEmpty() ? "" : "\n    " + structuresArray + "\n  ") + "],\n" +
            "  \"placement\": {\n" +
            "    \"type\": \"minecraft:random_spread\",\n" +
            "    \"salt\": " + settings.salt() + ",\n" +
            "    \"spacing\": " + settings.spacing() + ",\n" +
            "    \"separation\": " + settings.separation() + "\n" +
            "  }\n" +
            "}\n";
        return json.getBytes(StandardCharsets.UTF_8);
    }
```

Keep `ANCIENT_RUINS_PATH` and redefine it as `ManagedStructure.ANCIENT_RUINS.packPath()`. Add the imports `com.chronodawn.config.ManagedStructure` and `com.chronodawn.config.StructureSettings`.

- [ ] **Step 4: Run test to verify it passes**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*RuntimeStructureOverlayTest*'
```
Expected: PASS. If `defaultConfig_reproducesEveryBundledStructureSet` fails on key order or whitespace, the assertion compares parsed trees, so a failure means a real value mismatch — fix the defaults table, not the formatting.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeStructureOverlay.java common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimeStructureOverlayTest.java
git commit -m "feat(worldgen): generate every structure_set from the config overlay"
```

---

### Task 4: Parse all eight structures from TOML

Replace `parseAncientRuins` with an enum-driven parser so every structure reads its own section with the same validation and per-key fallback.

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java`

**Interfaces:**
- Consumes: `ManagedStructure` (Task 2), `StructureSettings` (Task 1).
- Produces: no new public API. `ConfigLoader.load(Path)` now populates all eight fields of `ChronoDawnConfig.Structures`.

- [ ] **Step 1: Write the failing test**

Append to `ConfigLoaderTest.java`:

```java
    @Test
    void structures_missingSections_fallBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");

        ChronoDawnConfig.Structures structures = ConfigLoader.load(tmp).world().structures();
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertEquals(structure.defaults(), structure.settingsOf(structures), structure.name());
        }
    }

    @Test
    void structures_customValues_areReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.master_clock]\n" +
            "enabled = false\n" +
            "spacing = 120\n" +
            "separation = 40\n" +
            "salt = 42\n");

        ChronoDawnConfig.Structures structures = ConfigLoader.load(tmp).world().structures();

        assertEquals(new StructureSettings(false, 120, 40, 42L), structures.masterClock());
        assertEquals(ConfigDefaults.ENTROPY_CRYPT_DEFAULTS, structures.entropyCrypt(),
            "One structure's section must not disturb another's");
    }

    @Test
    void structures_invalidSpacing_revertsOnlyThatKey(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.guardian_vault]\n" +
            "spacing = 0\n" +
            "separation = 5\n");

        StructureSettings settings = ConfigLoader.load(tmp).world().structures().guardianVault();

        assertEquals(ConfigDefaults.GUARDIAN_VAULT_DEFAULTS.spacing(), settings.spacing());
        assertEquals(5, settings.separation(), "A valid separation must survive an invalid spacing");
    }

    @Test
    void structures_separationAtOrAboveSpacing_revertsSeparation(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.phantom_catacombs]\n" +
            "spacing = 10\n" +
            "separation = 10\n");

        StructureSettings settings = ConfigLoader.load(tmp).world().structures().phantomCatacombs();

        assertEquals(10, settings.spacing());
        assertEquals(ConfigDefaults.PHANTOM_CATACOMBS_DEFAULTS.separation(), settings.separation());
    }
```

Add the import `import com.chronodawn.config.ManagedStructure;`.

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*'
```
Expected: FAIL — `structures_customValues_areReturnedVerbatim` reports the default master clock settings, because only `ancient_ruins` is parsed.

- [ ] **Step 3: Replace the parser**

In `ConfigLoader.java`, delete `parseAncientRuins` and the now-unused `K_ANCIENT_RUINS` constant, and add:

```java
    private static ChronoDawnConfig.Structures parseStructures(CommentedConfig parsed) {
        return new ChronoDawnConfig.Structures(
            parseStructure(parsed, ManagedStructure.ANCIENT_RUINS),
            parseStructure(parsed, ManagedStructure.FORGOTTEN_LIBRARY),
            parseStructure(parsed, ManagedStructure.DESERT_CLOCK_TOWER),
            parseStructure(parsed, ManagedStructure.GUARDIAN_VAULT),
            parseStructure(parsed, ManagedStructure.CLOCKWORK_DEPTHS),
            parseStructure(parsed, ManagedStructure.PHANTOM_CATACOMBS),
            parseStructure(parsed, ManagedStructure.ENTROPY_CRYPT),
            parseStructure(parsed, ManagedStructure.MASTER_CLOCK)
        );
    }

    private static StructureSettings parseStructure(CommentedConfig parsed, ManagedStructure structure) {
        String path = K_WORLD + "." + K_STRUCTURES + "." + structure.configKey();
        StructureSettings defaults = structure.defaults();

        boolean enabled = parsed.<Boolean>getOptional(path + "." + K_AR_ENABLED)
            .orElse(defaults.enabled());

        int spacing = parsed.<Number>getOptional(path + "." + K_AR_SPACING)
            .map(Number::intValue)
            .orElse(defaults.spacing());

        int separation = parsed.<Number>getOptional(path + "." + K_AR_SEPARATION)
            .map(Number::intValue)
            .orElse(defaults.separation());

        long salt = parsed.<Number>getOptional(path + "." + K_AR_SALT)
            .map(Number::longValue)
            .orElse(defaults.salt());

        // Validation: spacing must be in vanilla range, separation must be in [0, spacing).
        // Each field is validated independently so one bad value doesn't reset the others.
        if (spacing < MIN_SPACING || spacing > MAX_SPACING) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_AR_SPACING, spacing, MIN_SPACING, MAX_SPACING, defaults.spacing()
            );
            spacing = defaults.spacing();
        }
        if (separation < MIN_SEPARATION || separation >= spacing) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, spacing={})); using default {}",
                path, K_AR_SEPARATION, separation, MIN_SEPARATION, spacing, defaults.separation()
            );
            separation = defaults.separation();
            // If even the default exceeds the (now-validated) spacing, fall back to the safer half-spacing rule.
            if (separation >= spacing) {
                separation = Math.max(0, spacing - 1);
            }
        }

        return new StructureSettings(enabled, spacing, separation, salt);
    }
```

Then, in `parseOrDefaults`, replace the `ChronoDawnConfig.AncientRuins ancientRuins = parseAncientRuins(parsed);` line and the `new ChronoDawnConfig.Structures(ancientRuins)` expression with a single `parseStructures(parsed)` call:

```java
        ChronoDawnConfig.Structures structures = parseStructures(parsed);
```
```java
            new ChronoDawnConfig.World(
                structures,
                ores
            ),
```

Rename the four `K_AR_*` constants to `K_STRUCTURE_ENABLED`, `K_STRUCTURE_SPACING`, `K_STRUCTURE_SEPARATION`, `K_STRUCTURE_SALT` and update every reference — they are no longer Ancient-Ruins-specific.

- [ ] **Step 4: Extend the bundled default TOML**

In `common/shared/src/main/resources/chronodawn-default-config.toml`, after the existing `[world.structures.ancient_ruins]` block, add one block per remaining structure, in the order the enum declares them. Use the exact defaults from the Global Constraints table. For example:

```toml
[world.structures.forgotten_library]
# Whether the Forgotten Library generates in Chrono Dawn.
# Disabling it removes the Portal Stabilizer recipe from world generation.
enabled = true

# Average distance (in chunks) between placement attempts.
# Lower = denser, higher = rarer. Must be > separation. Vanilla limit: 4096.
spacing = 30

# Minimum guaranteed distance (in chunks) between two placements.
# Must be < spacing.
separation = 15

# Random seed offset for placement; usually no reason to change this.
salt = 8735421890
```

Repeat for `desert_clock_tower`, `guardian_vault`, `clockwork_depths`, `phantom_catacombs`, `entropy_crypt` and `master_clock`, replacing the first comment line with that structure's progression note from the Global Constraints table.

- [ ] **Step 5: Run tests to verify they pass**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*' --tests '*ManagedStructureTest*' --tests '*RuntimeStructureOverlayTest*'
```
Expected: PASS, including all pre-existing Ancient Ruins tests.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java common/shared/src/main/resources/chronodawn-default-config.toml common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java
git commit -m "feat(config): parse per-structure placement settings"
```

---

### Task 5: Warn about disabled progression structures

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/config/ProgressionWarnings.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java` (emit the warnings after parsing, inside `load`)
- Test: `common/shared/src/test/java/com/chronodawn/unit/ProgressionWarningsTest.java`

**Interfaces:**
- Consumes: `ManagedStructure` (Task 2), `ChronoDawnConfig.Structures` (Task 1).
- Produces: `ProgressionWarnings.forDisabledStructures(ChronoDawnConfig.Structures structures)` returning `List<String>` — one message per disabled structure that carries a progression note, in enum declaration order.

- [ ] **Step 1: Write the failing test**

Create `common/shared/src/test/java/com/chronodawn/unit/ProgressionWarningsTest.java` with the LGPL header, then:

```java
package com.chronodawn.unit;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ProgressionWarnings;
import com.chronodawn.config.StructureSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionWarningsTest {

    private static ChronoDawnConfig.Structures with(boolean ancientRuins, boolean masterClock, boolean entropyCrypt) {
        ChronoDawnConfig.Structures d = ConfigDefaults.defaults().world().structures();
        return new ChronoDawnConfig.Structures(
            disable(d.ancientRuins(), ancientRuins),
            d.forgottenLibrary(),
            d.desertClockTower(),
            d.guardianVault(),
            d.clockworkDepths(),
            d.phantomCatacombs(),
            disable(d.entropyCrypt(), entropyCrypt),
            disable(d.masterClock(), masterClock)
        );
    }

    private static StructureSettings disable(StructureSettings s, boolean enabled) {
        return new StructureSettings(enabled, s.spacing(), s.separation(), s.salt());
    }

    @Test
    void defaults_produceNoWarnings() {
        assertTrue(ProgressionWarnings.forDisabledStructures(
            ConfigDefaults.defaults().world().structures()).isEmpty());
    }

    @Test
    void disabledAncientRuins_producesNoWarning() {
        assertTrue(ProgressionWarnings.forDisabledStructures(with(false, true, true)).stream()
            .noneMatch(m -> m.contains("ancient_ruins")),
            "Ancient Ruins gates nothing, so disabling it must stay silent");
    }

    @Test
    void disabledProgressionStructures_produceOneMessageEach() {
        List<String> warnings = ProgressionWarnings.forDisabledStructures(with(true, false, false));

        assertEquals(2, warnings.size());
        assertTrue(warnings.get(0).contains("world.structures.entropy_crypt"), warnings.get(0));
        assertTrue(warnings.get(0).contains("Entropy Keeper and the Entropy Core"), warnings.get(0));
        assertTrue(warnings.get(1).contains("world.structures.master_clock"), warnings.get(1));
        assertTrue(warnings.get(1).contains("Time Tyrant, the final boss"), warnings.get(1));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ProgressionWarningsTest*'
```
Expected: FAIL — compilation error, `ProgressionWarnings` does not exist.

- [ ] **Step 3: Implement the message builder**

Create `common/shared/src/main/java/com/chronodawn/config/ProgressionWarnings.java` with the LGPL header, then:

```java
package com.chronodawn.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the startup warnings for structures a pack has disabled.
 *
 * <p>Disabling a structure is allowed — pack authors routinely substitute their own
 * sources — so the mod states the consequence rather than refusing the setting.
 * Message building is separated from logging so it can be tested directly.
 */
public final class ProgressionWarnings {
    private ProgressionWarnings() {}

    /**
     * @param structures the parsed structure settings
     * @return one message per disabled structure that gates progression, in enum order
     */
    public static List<String> forDisabledStructures(ChronoDawnConfig.Structures structures) {
        List<String> warnings = new ArrayList<>();
        for (ManagedStructure structure : ManagedStructure.values()) {
            if (structure.progressionNote().isEmpty()) {
                continue;
            }
            if (structure.settingsOf(structures).enabled()) {
                continue;
            }
            warnings.add(
                "world.structures." + structure.configKey() + " is disabled: "
                    + structure.progressionNote()
                    + " becomes unobtainable unless your pack provides another source."
            );
        }
        return warnings;
    }
}
```

- [ ] **Step 4: Emit the warnings from `ConfigLoader`**

In `ConfigLoader.load(Path)`, immediately after the config is parsed and before it is returned, add:

```java
        for (String warning : ProgressionWarnings.forDisabledStructures(config.world().structures())) {
            LOGGER.warn(warning);
        }
```

Adjust the local variable name to whatever `load` already uses for the parsed config. If `load` returns the parse result directly, introduce a local first so the warnings run exactly once per load.

- [ ] **Step 5: Run tests to verify they pass**

Run:
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ProgressionWarningsTest*' --tests '*ConfigLoaderTest*'
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ProgressionWarnings.java common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java common/shared/src/test/java/com/chronodawn/unit/ProgressionWarningsTest.java
git commit -m "feat(config): warn when a disabled structure gates progression"
```

---

### Task 6: Document the new settings and verify everything

**Files:**
- Modify: `docs/configuration.md` (the `### `[world.structures.ancient_ruins]`` section, around lines 61-110)
- Modify: `docs/modpack-integration.md` (the Configuration examples section)
- Modify: `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md` (sub-project A status and the tracker table)

**Interfaces:**
- Consumes: everything from Tasks 1-5. Produces no code.

- [ ] **Step 1: Rewrite the configuration reference section**

In `docs/configuration.md`, replace the `### `[world.structures.ancient_ruins]`` heading and its body with a `### `[world.structures.*]`` section containing:

- One paragraph explaining that all eight structures share the same four fields.
- A table listing every structure with its `enabled` / `spacing` / `separation` / `salt` defaults and its progression role, using the Global Constraints table of this plan as the source of values.
- The field-semantics table (`enabled`, `spacing`, `separation`, `salt`) copied from the current Ancient Ruins section, since the rules are unchanged.
- A prominent note: disabling any structure other than Ancient Ruins breaks the main progression chain, the mod logs a warning at startup naming what becomes unobtainable, and the pack is responsible for providing another source.
- The existing "denser ruins" and "disable Ancient Ruins entirely" examples, plus one new example disabling a dungeon:

```toml
# Remove the Clockwork Depths dungeon. Colossus Gear must come from your pack.
[world.structures.clockwork_depths]
enabled = false
```

- The existing "new worlds only" caveat.

- [ ] **Step 2: Add the modpack-integration example**

In `docs/modpack-integration.md`, after the existing ore examples, add an example that retunes a dungeon's density and one that disables a structure, each two to four lines of TOML with a one-sentence explanation, and a cross-reference to the progression warning.

- [ ] **Step 3: Update the roadmap**

In `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`:
- In the sub-project A "Planned follow-up tunables" list, mark the dimension-level toggles entry as partially shipped: structures done, per-biome flags still open. Link `./2026-08-22-structure-toggles-design.md`.
- Update the sub-project A status line and the tracker table row to say seven PRs shipped and to add the new design link.

- [ ] **Step 4: Run the full verification**

Run (with `dangerouslyDisableSandbox: true`, since GameTest forks a JVM):
```bash
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew checkAll > /private/tmp/claude-501/checkall-structures.log 2>&1; echo "EXIT=$?"; tail -25 /private/tmp/claude-501/checkall-structures.log
```
Expected: `All verification tasks passed!` and exit code 0. This takes roughly 11 minutes. Do not claim success without reading the tail of the log.

- [ ] **Step 5: Commit**

```bash
git add docs/
git commit -m "docs(config): document per-structure generation settings"
```

---

## Notes for the executor

- `RuntimeStructureOverlayTest` lives under `common/shared/src/test/java/com/chronodawn/worldgen/runtime/`, while the config tests live under `.../com/chronodawn/unit/`. Both are on the same test source set; put new tests next to their subject.
- The test source set is shared by every `common/<mcversion>` module, so running one version's `test` task is enough during development. `checkAll` covers the rest.
- If `git commit` fails with a GPG error about `pubring.kbx` permissions, re-run the command with the sandbox disabled.
