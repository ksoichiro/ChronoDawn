# Per-Biome Generation Toggles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let modpack authors disable any of the nine non-core Chrono Dawn biomes from `chronodawn.toml`, with the disabled biome's region of the multi-noise parameter space taken over by a predictable fallback biome.

**Architecture:** A `ManagedBiome` enum is the single source of truth for the configurable biome set — the config parser, the runtime overlay and the guard tests all iterate it. `RuntimeBiomeOverlay` reads the bundled `dimension/chronodawn.json` from the classpath, rewrites only the `biome` field of entries whose biome is disabled, and emits the result into the existing runtime overlay datapack. Multi-noise parameters are never copied into Java.

**Tech Stack:** Java 21, Gson (shipped by Minecraft, already used in `ChronicleData`; a `testImplementation` dependency in each version module), NightConfig `CommentedConfig` (existing config stack), JUnit 5.

**Spec:** [`docs/superpowers/specs/2026-08-23-biome-toggles-design.md`](../specs/2026-08-23-biome-toggles-design.md)

## Global Constraints

- **Worktree:** all work happens in `.claude/worktrees/biome-toggles` on branch `biome-toggles`. Run `pwd` before the first command of every task and confirm it ends in `/.claude/worktrees/biome-toggles`.
- **JAVA_HOME is unset in agent shells and there is no system JDK.** Prefix every Gradle command with:
  `export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH";`
- **Never run two Gradle builds against this worktree at once.** A concurrent build produces bogus failures.
- **Never run a foreground Gradle build while a background one is active in the same repo.**
- All source files are shared across every supported Minecraft version. `common/shared/` is **not** a Gradle subproject — its sources compile into each `common-<version>` module. Unit tests run per version module.
- Unit test command used throughout: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '<pattern>'`
- Comments and documentation in **English**. Conventional Commits, English.
- Every new file ends with a newline.
- LGPL-3.0 header on every new `.java` file — copy verbatim from an existing sibling (e.g. `common/shared/src/main/java/com/chronodawn/config/ManagedStructure.java` lines 1-17).
- `schema_version` stays at `1`. Do not bump `ChronoDawnConfig.CURRENT_SCHEMA_VERSION`.
- Do not modify anything under `data/chronodawn/worldgen/biome/` — the spec's existing-chunk safety property depends on biome definitions staying registered.
- Do not push. Do not touch the 14 unpushed commits on `main`.

## File Structure

**Created:**
- `common/shared/src/main/java/com/chronodawn/config/BiomeSettings.java` — one-field settings record.
- `common/shared/src/main/java/com/chronodawn/config/ManagedBiome.java` — the enum; identity, fallback chain, content note.
- `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeBiomeOverlay.java` — bundled-JSON transform.
- `common/shared/src/test/java/com/chronodawn/unit/ManagedBiomeTest.java` — enum consistency + the 512-combination structural property.
- `common/shared/src/test/java/com/chronodawn/unit/RuntimeBiomeOverlayTest.java` — overlay behaviour.

**Modified:**
- `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java` — `World` gains a `Biomes` component.
- `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java` — `BIOME_DEFAULTS` + wire into `defaults()`.
- `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java` — `parseBiomes` / `parseBiome`, plus the biome warning loop in `load`.
- `common/shared/src/main/java/com/chronodawn/config/ProgressionWarnings.java` — `forDisabledBiomes`.
- `common/shared/src/main/java/com/chronodawn/worldgen/runtime/OverlayPackBootstrap.java` — one `putAll` line.
- `common/shared/src/main/resources/chronodawn-default-config.toml` — nine `[world.biomes.*]` tables.
- `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java` — parser tests + TOML drift guard.
- `common/shared/src/test/java/com/chronodawn/unit/ProgressionWarningsTest.java` — biome warning tests.
- `docs/configuration.md` — `[world.biomes.*]` section.
- `CHANGELOG.md`, `README.md` — user-facing entries.

**Task order rationale:** Task 1 (the content audit) produces data that Task 2 (the enum) hard-codes, so it must come first. Tasks 2→3→4 build the enum, then the config plumbing that reads it, then the overlay that consumes it. Task 5 is the structural property test, which needs only the enum but is more meaningful once the whole thing works. Task 6 is documentation.

---

### Task 1: Audit biome-exclusive content

The spec deliberately left the `contentNote` values undetermined: a feature being exclusive to one biome does not mean the *blocks* it places are. This task produces the data Task 2 hard-codes. Its deliverable is a written findings section, not code.

**Files:**
- Create: `docs/superpowers/specs/2026-08-23-biome-toggles-design.md` — append a `## Audit results` section (the file exists; append to it).

**Interfaces:**
- Consumes: nothing.
- Produces: for each of the nine non-core biomes, a `contentNote` string or the explicit finding that it has none. Task 2 copies these verbatim into `ManagedBiome`.

- [ ] **Step 1: Confirm the worktree**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles && pwd
```

Expected: path ends with `/.claude/worktrees/biome-toggles`.

- [ ] **Step 2: Collect each biome's feature and mob set**

The nine non-core biomes to audit are: `desert`, `prairies`, `forest`, `dark_forest`, `ancient_forest`, `snowy`, `mountain`, `swamp`, `faded_plains`. (`plains` and `ocean` are core — they cannot be disabled, so they need no audit.)

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
python3 - <<'EOF'
import json, glob, collections, os
os.chdir('common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome')
mobs = collections.defaultdict(set)
feats = collections.defaultdict(set)
for f in sorted(glob.glob('*.json')):
    b = f[:-5]
    d = json.load(open(f))
    for cat, lst in d.get('spawners', {}).items():
        for e in (lst if isinstance(lst, list) else []):
            mobs[e['type']].add(b)
    for layer in d.get('features', []):
        for x in layer:
            feats[x].add(b)
print('=== mobs exclusive to one biome ===')
for t, bs in sorted(mobs.items()):
    if len(bs) == 1:
        print(' ', t, '->', list(bs)[0])
print('=== chronodawn features exclusive to one biome ===')
for t, bs in sorted(feats.items()):
    if len(bs) == 1 and t.startswith('chronodawn'):
        print(' ', t, '->', list(bs)[0])
EOF
```

Expected: two mobs (`chronodawn:chrono_ursid` → `chronodawn_snowy`, `chronodawn:temporal_caprid` → `chronodawn_mountain`) plus vanilla mobs, and 25 `chronodawn:`-namespaced exclusive features.

- [ ] **Step 3: Expand exclusive features to the blocks they place**

For each exclusive feature belonging to a non-core biome, resolve `placed_feature` → `configured_feature` → block IDs, then determine whether each block is reachable from any other biome.

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
python3 - <<'EOF'
import json, glob, collections, os, re
root = 'common/1.21.11/src/main/resources/data/chronodawn/worldgen'
CORE = {'chronodawn_plains', 'chronodawn_ocean'}

def load(kind, rid):
    """Load a placed_feature / configured_feature JSON by registry ID, or None."""
    ns, _, path = rid.partition(':')
    if ns != 'chronodawn':
        return None
    p = os.path.join(root, kind, path + '.json')
    return json.load(open(p)) if os.path.exists(p) else None

def blocks_of(obj):
    """Every Block ID mentioned anywhere in a configured_feature tree."""
    out = set()
    def walk(o):
        if isinstance(o, dict):
            for k, v in o.items():
                if k in ('Name', 'block', 'to_place') and isinstance(v, str):
                    out.add(v)
                walk(v)
        elif isinstance(o, list):
            for v in o:
                walk(v)
        elif isinstance(o, str) and re.fullmatch(r'[a-z0-9_]+:[a-z0-9_/]+', o):
            out.add(o)
    walk(obj)
    return {b for b in out if b.startswith('chronodawn:') or b.startswith('minecraft:')}

def feature_blocks(placed_id, seen=None):
    seen = seen or set()
    if placed_id in seen:
        return set()
    seen.add(placed_id)
    pf = load('placed_feature', placed_id)
    if pf is None:
        return set()
    cf_ref = pf.get('feature')
    if isinstance(cf_ref, str):
        cf = load('configured_feature', cf_ref)
        return blocks_of(cf) if cf else set()
    return blocks_of(cf_ref)

# biome -> set(blocks reachable from that biome)
biome_blocks = {}
for f in sorted(glob.glob(os.path.join(root, '..', 'worldgen', 'biome', '*.json'))):
    b = os.path.basename(f)[:-5]
    d = json.load(open(f))
    acc = set()
    for layer in d.get('features', []):
        for pid in layer:
            acc |= feature_blocks(pid)
    biome_blocks[b] = acc

for b in sorted(biome_blocks):
    if b in CORE:
        continue
    others = set()
    for ob, bl in biome_blocks.items():
        if ob != b:
            others |= bl
    exclusive = sorted(biome_blocks[b] - others)
    print(f'{b}: {len(exclusive)} exclusive block(s)')
    for x in exclusive:
        print('   ', x)
EOF
```

Read the output carefully. A block listed here has **no other biome** placing it. Cross-check any surprising result by grepping for the block in recipes and loot tables — a block obtainable by crafting is not lost when its biome is disabled:

```bash
grep -rl "<block-id-without-namespace>" common/shared/src/main/resources/data/chronodawn/recipe common/shared/src/main/resources/data/chronodawn/loot_table 2>/dev/null | head
```

- [ ] **Step 4: Write the findings into the spec**

Append to `docs/superpowers/specs/2026-08-23-biome-toggles-design.md`:

```markdown
## Audit results

Produced by the method in "Implementation notes", run against the 1.21.11
biome definitions on 2026-08-23.

| Biome | `contentNote` | Basis |
| --- | --- | --- |
| `snowy` | `the Chrono Ursid` | only biome the mob spawns in |
| `mountain` | `the Temporal Caprid` | only biome the mob spawns in |
| ... | ... | ... |

Biomes with an empty `contentNote` place nothing that is unobtainable
elsewhere; disabling them costs decoration and scenery only.
```

Fill every one of the nine rows. A biome with nothing exclusive gets an empty note and an explicit "nothing exclusive" basis — that is a finding, not a gap. Where a block is exclusive to a biome's worldgen but craftable, record the note as empty and give the recipe as the basis.

- [ ] **Step 5: Commit**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
git add docs/superpowers/specs/2026-08-23-biome-toggles-design.md
git commit -m "docs: audit biome-exclusive content for toggle warnings"
```

---

### Task 2: `ManagedBiome` and `BiomeSettings`

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/config/BiomeSettings.java`
- Create: `common/shared/src/main/java/com/chronodawn/config/ManagedBiome.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ManagedBiomeTest.java`

**Interfaces:**
- Consumes: the `contentNote` values from Task 1.
- Produces:
  - `record BiomeSettings(boolean enabled)`
  - `ManagedBiome.values()`, `.configKey() -> String`, `.biomeId() -> String`, `.contentNote() -> String`, `.isCore() -> boolean`, `.fallback() -> Optional<ManagedBiome>`, `.resolveFallback(Predicate<ManagedBiome> enabled) -> ManagedBiome`, `static Optional<ManagedBiome> byBiomeId(String)`, `static Optional<ManagedBiome> byConfigKey(String)`, `static List<ManagedBiome> configurable()`
  - `ManagedBiome.DIMENSION_PACK_PATH` = `"data/chronodawn/dimension/chronodawn.json"`

- [ ] **Step 1: Write the failing test**

Create `common/shared/src/test/java/com/chronodawn/unit/ManagedBiomeTest.java` (LGPL header first, copied from `ManagedStructureTest.java`):

```java
package com.chronodawn.unit;

import com.chronodawn.config.ManagedBiome;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagedBiomeTest {

    @Test
    void exactlyTwoCoreBiomes_plainsAndOcean() {
        List<String> core = java.util.Arrays.stream(ManagedBiome.values())
            .filter(ManagedBiome::isCore)
            .map(ManagedBiome::configKey)
            .sorted()
            .toList();
        assertEquals(List.of("ocean", "plains"), core, "plains and ocean are the only core biomes");
    }

    @Test
    void coreBiomesHaveNoFallback_nonCoreBiomesDo() {
        for (ManagedBiome biome : ManagedBiome.values()) {
            if (biome.isCore()) {
                assertTrue(biome.fallback().isEmpty(), biome + " is core so it must have no fallback");
            } else {
                assertTrue(biome.fallback().isPresent(), biome + " is not core so it must have a fallback");
            }
        }
    }

    @Test
    void everyFallbackChainTerminatesAtACoreBiome() {
        for (ManagedBiome start : ManagedBiome.values()) {
            Set<ManagedBiome> visited = new HashSet<>();
            ManagedBiome current = start;
            while (!current.isCore()) {
                assertTrue(visited.add(current),
                    start + ": fallback chain loops at " + current);
                Optional<ManagedBiome> next = current.fallback();
                assertTrue(next.isPresent(), current + " is not core so it must have a fallback");
                current = next.get();
            }
        }
    }

    @Test
    void resolveFallback_skipsDisabledBiomesUntilAnEnabledOne() {
        ManagedBiome ancientForest = ManagedBiome.ANCIENT_FOREST;

        // Nothing disabled: a biome resolves to itself.
        assertEquals(ancientForest, ancientForest.resolveFallback(b -> true));

        // ancient_forest disabled only: falls back one step to dark_forest.
        assertEquals(
            ManagedBiome.DARK_FOREST,
            ancientForest.resolveFallback(b -> b != ManagedBiome.ANCIENT_FOREST)
        );

        // ancient_forest, dark_forest and forest all disabled: resolves to plains.
        Set<ManagedBiome> off = Set.of(
            ManagedBiome.ANCIENT_FOREST, ManagedBiome.DARK_FOREST, ManagedBiome.FOREST);
        assertEquals(ManagedBiome.PLAINS, ancientForest.resolveFallback(b -> !off.contains(b)));
    }

    @Test
    void configKeysAndBiomeIdsAreUnique() {
        Set<String> keys = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (ManagedBiome biome : ManagedBiome.values()) {
            assertTrue(keys.add(biome.configKey()), "Duplicate configKey: " + biome.configKey());
            assertTrue(ids.add(biome.biomeId()), "Duplicate biomeId: " + biome.biomeId());
        }
    }

    @Test
    void configurable_excludesCoreBiomes() {
        List<ManagedBiome> configurable = ManagedBiome.configurable();
        assertEquals(9, configurable.size(), "Nine of the eleven biomes are configurable");
        for (ManagedBiome biome : configurable) {
            assertFalse(biome.isCore(), biome + " is core and must not be configurable");
        }
    }

    @Test
    void byBiomeId_findsEveryBiome_andRejectsUnknown() {
        for (ManagedBiome biome : ManagedBiome.values()) {
            assertEquals(Optional.of(biome), ManagedBiome.byBiomeId(biome.biomeId()));
        }
        assertTrue(ManagedBiome.byBiomeId("minecraft:plains").isEmpty());
    }

    /**
     * Guards against the enum drifting from the bundled dimension JSON. Adding a biome to
     * the dimension without registering it here would leave it silently untoggleable;
     * removing one from the dimension while it stays here would produce a config table
     * that does nothing.
     *
     * <p>Compared as sets, not lists: chronodawn_snowy occupies two entries.
     */
    @Test
    void managedBiomes_matchTheBundledDimensionJson() {
        Set<String> declared = java.util.Arrays.stream(ManagedBiome.values())
            .map(ManagedBiome::biomeId)
            .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));

        Set<String> inJson = new java.util.TreeSet<>();
        try (java.io.InputStream in = ManagedBiomeTest.class.getClassLoader()
            .getResourceAsStream(ManagedBiome.DIMENSION_PACK_PATH)) {
            assertNotNull(in, "Bundled dimension JSON not found on the test classpath");
            com.google.gson.JsonObject dimension = com.google.gson.JsonParser
                .parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
            for (com.google.gson.JsonElement entry : dimension.getAsJsonObject("generator")
                .getAsJsonObject("biome_source").getAsJsonArray("biomes")) {
                inJson.add(entry.getAsJsonObject().get("biome").getAsString());
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(inJson, declared,
            "ManagedBiome must declare exactly the biomes the bundled dimension JSON references");
    }
}
```

Add `import static org.junit.jupiter.api.Assertions.assertNotNull;` to the file.

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ManagedBiomeTest*'
```

Expected: compilation failure — `package com.chronodawn.config.ManagedBiome does not exist`.

- [ ] **Step 3: Write `BiomeSettings`**

Create `common/shared/src/main/java/com/chronodawn/config/BiomeSettings.java` (LGPL header, then):

```java
package com.chronodawn.config;

/**
 * Generation settings for one biome.
 *
 * <p>A biome has no placement axes of its own — its position in the dimension's
 * multi-noise parameter space is fixed by the bundled dimension JSON — so unlike
 * {@link StructureSettings} this carries a single flag.
 */
public record BiomeSettings(boolean enabled) {}
```

- [ ] **Step 4: Write `ManagedBiome`**

Create `common/shared/src/main/java/com/chronodawn/config/ManagedBiome.java` (LGPL header, then):

```java
package com.chronodawn.config;

import com.chronodawn.ChronoDawn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Every biome of the Chrono dimension whose generation Chrono Dawn exposes to
 * configuration.
 *
 * <p>This enum is the single source of truth for that set: the config parser, the
 * runtime overlay and the guard tests all iterate it. A biome that is not
 * registered here appears in none of them, and one that is registered appears in
 * all of them — so a biome cannot be half-wired.
 *
 * <p>Deliberately absent: the multi-noise parameters. Those stay in the bundled
 * dimension JSON, which {@code RuntimeBiomeOverlay} transforms rather than
 * regenerates, so they are never duplicated into Java.
 *
 * <p>Disabling a biome remaps its region of the parameter space to
 * {@link #fallback()} rather than leaving a hole, so the replacement is
 * predictable. {@link #PLAINS} and {@link #OCEAN} are core: they terminate every
 * fallback chain, so they cannot be disabled and get no config table at all.
 */
public enum ManagedBiome {
    // Fallbacks are declared by name, not by constant reference: the JLS forbids an
    // enum constant's initializer from referencing a later-declared sibling, even
    // through a lambda, and these chains point forward. A String literal is not a
    // reference to the constant, so valueOf() in fallback() sidesteps the restriction.
    PLAINS("plains", "chronodawn_plains", null, ""),
    OCEAN("ocean", "chronodawn_ocean", null, ""),
    FOREST("forest", "chronodawn_forest", "PLAINS", ""),
    DARK_FOREST("dark_forest", "chronodawn_dark_forest", "FOREST", ""),
    ANCIENT_FOREST("ancient_forest", "chronodawn_ancient_forest", "DARK_FOREST", ""),
    SWAMP("swamp", "chronodawn_swamp", "FOREST", ""),
    FADED_PLAINS("faded_plains", "chronodawn_faded_plains", "PLAINS", ""),
    DESERT("desert", "chronodawn_desert", "FADED_PLAINS", ""),
    PRAIRIES("prairies", "chronodawn_prairies", "PLAINS", ""),
    SNOWY("snowy", "chronodawn_snowy", "PLAINS", "the Chrono Ursid"),
    MOUNTAIN("mountain", "chronodawn_mountain", "PLAINS", "the Temporal Caprid");

    /** The data-pack-relative path of the dimension JSON the overlay replaces. */
    public static final String DIMENSION_PACK_PATH =
        "data/" + ChronoDawn.MOD_ID + "/dimension/" + ChronoDawn.MOD_ID + ".json";

    private final String configKey;
    private final String biomePath;
    private final String fallbackName;
    private final String contentNote;

    ManagedBiome(String configKey, String biomePath, String fallbackName, String contentNote) {
        this.configKey = configKey;
        this.biomePath = biomePath;
        this.fallbackName = fallbackName;
        this.contentNote = contentNote;
    }

    /**
     * The TOML table name under {@code [world.biomes]}.
     *
     * <p>Core biomes have one too — it names them in logs and guard tests — but no
     * table is written for them, which is how the config states that they cannot be
     * disabled.
     */
    public String configKey() {
        return configKey;
    }

    /** The registry ID of this biome, as it appears in the dimension JSON. */
    public String biomeId() {
        return ChronoDawn.MOD_ID + ":" + biomePath;
    }

    /** What a pack loses by disabling this biome; empty when nothing becomes unobtainable. */
    public String contentNote() {
        return contentNote;
    }

    /** Whether this biome terminates fallback chains and therefore cannot be disabled. */
    public boolean isCore() {
        return fallbackName == null;
    }

    /** The biome this one's parameter-space region is remapped to when disabled. */
    public Optional<ManagedBiome> fallback() {
        return fallbackName == null ? Optional.empty() : Optional.of(ManagedBiome.valueOf(fallbackName));
    }

    /**
     * Walks the fallback chain until it reaches a biome the given predicate accepts.
     *
     * <p>Terminates because every chain ends at a core biome and core biomes are
     * always enabled — a property {@code ManagedBiomeTest} asserts directly. The
     * visited set is a backstop against a malformed chain hanging the server.
     *
     * @param enabled tells whether a biome is enabled in the active config
     * @return this biome if it is enabled, otherwise the first enabled biome down the chain
     */
    public ManagedBiome resolveFallback(Predicate<ManagedBiome> enabled) {
        Set<ManagedBiome> visited = new HashSet<>();
        ManagedBiome current = this;
        while (!enabled.test(current)) {
            if (!visited.add(current)) {
                throw new IllegalStateException("Fallback chain loops at " + current);
            }
            ManagedBiome disabled = current;
            current = current.fallback()
                .orElseThrow(() -> new IllegalStateException(
                    "Core biome reported as disabled: " + disabled));
        }
        return current;
    }

    /** The biomes a pack can toggle — every biome except the core ones, in enum order. */
    public static List<ManagedBiome> configurable() {
        return Arrays.stream(values()).filter(b -> !b.isCore()).toList();
    }

    /** Looks up a biome by its {@link #biomeId()}. */
    public static Optional<ManagedBiome> byBiomeId(String biomeId) {
        return Arrays.stream(values()).filter(b -> b.biomeId().equals(biomeId)).findFirst();
    }

    /** Looks up a biome by its {@link #configKey()}. */
    public static Optional<ManagedBiome> byConfigKey(String configKey) {
        return Arrays.stream(values()).filter(b -> b.configKey.equals(configKey)).findFirst();
    }
}
```

Fill in the `contentNote` for `FOREST`, `DARK_FOREST`, `ANCIENT_FOREST`, `SWAMP`, `FADED_PLAINS`, `DESERT` and `PRAIRIES` from Task 1's audit table — seven values, each either a note or a deliberate empty string. `SNOWY` and `MOUNTAIN` are already filled in above; confirm the audit agrees before leaving them. Core biomes always have an empty note.

- [ ] **Step 5: Run the test to verify it passes**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ManagedBiomeTest*'
```

Expected: PASS, 8 tests.

- [ ] **Step 6: Verify the loop guard actually fires**

A guard test that has only ever been seen green is not evidence. Temporarily change `PRAIRIES`'s fallback from `"PLAINS"` to `"SNOWY"` and `SNOWY`'s from `"PLAINS"` to `"PRAIRIES"`, re-run the same command, and confirm `everyFallbackChainTerminatesAtACoreBiome` **fails** with "fallback chain loops at". Then revert both edits and re-run to confirm green again.

Do the same for the drift guard: temporarily change `PRAIRIES`'s second constructor argument from `"chronodawn_prairies"` to `"chronodawn_nonexistent"`, confirm `managedBiomes_matchTheBundledDimensionJson` **fails**, then revert and confirm green.

- [ ] **Step 7: Commit**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
git add common/shared/src/main/java/com/chronodawn/config/BiomeSettings.java \
        common/shared/src/main/java/com/chronodawn/config/ManagedBiome.java \
        common/shared/src/test/java/com/chronodawn/unit/ManagedBiomeTest.java
git commit -m "feat(config): add ManagedBiome as the source of truth for biome toggles"
```

---

### Task 3: Config plumbing — record, defaults, parser, TOML, warnings

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ProgressionWarnings.java`
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ProgressionWarningsTest.java`

**Interfaces:**
- Consumes: `ManagedBiome`, `BiomeSettings` from Task 2.
- Produces:
  - `ChronoDawnConfig.Biomes` — a record with nine `BiomeSettings` components named `desert`, `prairies`, `forest`, `darkForest`, `ancientForest`, `snowy`, `mountain`, `swamp`, `fadedPlains`
  - `ChronoDawnConfig.World(Structures, OresConfig, Biomes)` plus the retained `World(Structures, OresConfig)`
  - `ChronoDawnConfig.World.biomes() -> Biomes`
  - `ManagedBiome.settingsOf(ChronoDawnConfig.Biomes) -> BiomeSettings`
  - `ConfigDefaults.BIOME_DEFAULTS -> ChronoDawnConfig.Biomes`
  - `ProgressionWarnings.forDisabledBiomes(ChronoDawnConfig.Biomes) -> List<String>`

- [ ] **Step 1: Write the failing tests**

Append to `ConfigLoaderTest.java`, inside the class (before its closing brace):

```java
    @Test
    void biomes_defaultToEnabledWhenAbsentFromConfig(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");

        ChronoDawnConfig.Biomes biomes = ConfigLoader.load(tmp).world().biomes();

        for (ManagedBiome biome : ManagedBiome.configurable()) {
            assertTrue(biome.settingsOf(biomes).enabled(), biome.name() + " must default to enabled");
        }
    }

    @Test
    void biomes_readExplicitDisable(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), """
            schema_version = 1

            [world.biomes.snowy]
            enabled = false
            """);

        ChronoDawnConfig.Biomes biomes = ConfigLoader.load(tmp).world().biomes();

        assertFalse(ManagedBiome.SNOWY.settingsOf(biomes).enabled(), "snowy was explicitly disabled");
        assertTrue(ManagedBiome.MOUNTAIN.settingsOf(biomes).enabled(), "siblings are unaffected");
    }

    @Test
    void bundledDefaultTemplate_hasATableForEveryConfigurableBiome(@TempDir Path tmp) throws IOException {
        try (InputStream in = ConfigLoader.class.getResourceAsStream("/chronodawn-default-config.toml")) {
            assertTrue(in != null, "Bundled default config resource not found");
            Files.copy(in, tmp.resolve("chronodawn.toml"), StandardCopyOption.REPLACE_EXISTING);
        }
        String toml = Files.readString(tmp.resolve("chronodawn.toml"));

        for (ManagedBiome biome : ManagedBiome.configurable()) {
            assertTrue(toml.contains("[world.biomes." + biome.configKey() + "]"),
                biome.name() + ": bundled chronodawn-default-config.toml must declare its table");
        }
        for (ManagedBiome biome : ManagedBiome.values()) {
            if (!biome.isCore()) continue;
            assertFalse(toml.contains("[world.biomes." + biome.configKey() + "]"),
                biome.name() + " is core and must not appear in the bundled config");
        }

        assertEquals(ConfigDefaults.BIOME_DEFAULTS, ConfigLoader.load(tmp).world().biomes(),
            "bundled template must load to exactly ConfigDefaults.BIOME_DEFAULTS");
    }
```

Add the imports `com.chronodawn.config.ManagedBiome` and `com.chronodawn.config.BiomeSettings` at the top of the file if not already present, and `static org.junit.jupiter.api.Assertions.assertFalse`.

Append to `ProgressionWarningsTest.java`, inside the class:

```java
    @Test
    void disabledBiomeWithContentNote_producesAWarning() {
        ChronoDawnConfig.Biomes biomes = disable(ManagedBiome.SNOWY);

        List<String> warnings = ProgressionWarnings.forDisabledBiomes(biomes);

        assertEquals(1, warnings.size(), "Only snowy was disabled");
        assertTrue(warnings.get(0).contains("world.biomes.snowy"), warnings.get(0));
        assertTrue(warnings.get(0).contains("the Chrono Ursid"), warnings.get(0));
    }

    @Test
    void allBiomesEnabled_producesNoWarnings() {
        assertTrue(ProgressionWarnings.forDisabledBiomes(ConfigDefaults.BIOME_DEFAULTS).isEmpty());
    }

    /** Builds a Biomes record with exactly the given biome disabled. */
    private static ChronoDawnConfig.Biomes disable(ManagedBiome target) {
        java.util.function.Function<ManagedBiome, BiomeSettings> s =
            b -> new BiomeSettings(b != target);
        return new ChronoDawnConfig.Biomes(
            s.apply(ManagedBiome.DESERT),
            s.apply(ManagedBiome.PRAIRIES),
            s.apply(ManagedBiome.FOREST),
            s.apply(ManagedBiome.DARK_FOREST),
            s.apply(ManagedBiome.ANCIENT_FOREST),
            s.apply(ManagedBiome.SNOWY),
            s.apply(ManagedBiome.MOUNTAIN),
            s.apply(ManagedBiome.SWAMP),
            s.apply(ManagedBiome.FADED_PLAINS)
        );
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*' --tests '*ProgressionWarningsTest*'
```

Expected: compilation failure — `cannot find symbol: method biomes()`.

- [ ] **Step 3: Add the `Biomes` record and widen `World`**

In `ChronoDawnConfig.java`, replace `public record World(Structures structures, OresConfig ores) {}` with:

```java
    public record World(Structures structures, OresConfig ores, Biomes biomes) {
        /** Preserve the existing construction pattern for callers that don't configure biomes. */
        public World(Structures structures, OresConfig ores) {
            this(structures, ores, ConfigDefaults.BIOME_DEFAULTS);
        }
    }

    public record Biomes(
        BiomeSettings desert,
        BiomeSettings prairies,
        BiomeSettings forest,
        BiomeSettings darkForest,
        BiomeSettings ancientForest,
        BiomeSettings snowy,
        BiomeSettings mountain,
        BiomeSettings swamp,
        BiomeSettings fadedPlains
    ) {}
```

- [ ] **Step 4: Add `settingsOf` to `ManagedBiome`**

Add to `ManagedBiome.java` — an accessor field mirroring `ManagedStructure`'s. Add the import `java.util.function.Function`, add a field and constructor parameter, and this method:

```java
    /** Reads this biome's settings out of a parsed config. Core biomes are always enabled. */
    public BiomeSettings settingsOf(ChronoDawnConfig.Biomes biomes) {
        return accessor == null ? new BiomeSettings(true) : accessor.apply(biomes);
    }
```

Give the two core constants `null` for the new accessor argument and each configurable constant its method reference — e.g. `DESERT("desert", "chronodawn_desert", "FADED_PLAINS", "", ChronoDawnConfig.Biomes::desert)`. Core biomes report enabled unconditionally, which is what makes `resolveFallback` terminate.

- [ ] **Step 5: Add `BIOME_DEFAULTS` and wire it into `defaults()`**

In `ConfigDefaults.java`, add near the other defaults:

```java
    /** Every biome generates by default. */
    public static final ChronoDawnConfig.Biomes BIOME_DEFAULTS = new ChronoDawnConfig.Biomes(
        new BiomeSettings(true), new BiomeSettings(true), new BiomeSettings(true),
        new BiomeSettings(true), new BiomeSettings(true), new BiomeSettings(true),
        new BiomeSettings(true), new BiomeSettings(true), new BiomeSettings(true)
    );
```

In `defaults()`, change the `new ChronoDawnConfig.World(...)` call to pass `BIOME_DEFAULTS` as its third argument, after the `new OresConfig(...)` block.

- [ ] **Step 6: Add the parser**

In `ConfigLoader.java`, add the key constant next to `K_STRUCTURES`:

```java
    private static final String K_BIOMES = "biomes";
    private static final String K_BIOME_ENABLED = "enabled";
```

Add the two parse methods next to `parseStructures`:

```java
    private static ChronoDawnConfig.Biomes parseBiomes(CommentedConfig parsed) {
        return new ChronoDawnConfig.Biomes(
            parseBiome(parsed, ManagedBiome.DESERT),
            parseBiome(parsed, ManagedBiome.PRAIRIES),
            parseBiome(parsed, ManagedBiome.FOREST),
            parseBiome(parsed, ManagedBiome.DARK_FOREST),
            parseBiome(parsed, ManagedBiome.ANCIENT_FOREST),
            parseBiome(parsed, ManagedBiome.SNOWY),
            parseBiome(parsed, ManagedBiome.MOUNTAIN),
            parseBiome(parsed, ManagedBiome.SWAMP),
            parseBiome(parsed, ManagedBiome.FADED_PLAINS)
        );
    }

    // No validation beyond the type: a biome has a single boolean and no interacting
    // fields, so there is nothing to clamp the way parseStructure clamps separation.
    private static BiomeSettings parseBiome(CommentedConfig parsed, ManagedBiome biome) {
        String path = K_WORLD + "." + K_BIOMES + "." + biome.configKey();
        boolean enabled = parsed.<Boolean>getOptional(path + "." + K_BIOME_ENABLED).orElse(true);
        return new BiomeSettings(enabled);
    }
```

In `parseOrDefaults`, pass `parseBiomes(parsed)` as the third argument to the `new ChronoDawnConfig.World(...)` construction alongside the existing `structures` and ores arguments.

In `load(Path)`, add the biome warning loop immediately after the existing structure one:

```java
        for (String warning : ProgressionWarnings.forDisabledBiomes(config.world().biomes())) {
            LOGGER.warn(warning);
        }
```

- [ ] **Step 7: Add `forDisabledBiomes`**

In `ProgressionWarnings.java`, add:

```java
    /**
     * @param biomes the parsed biome settings
     * @return one message per disabled biome that takes exclusive content with it, in enum order
     */
    public static List<String> forDisabledBiomes(ChronoDawnConfig.Biomes biomes) {
        List<String> warnings = new ArrayList<>();
        for (ManagedBiome biome : ManagedBiome.configurable()) {
            if (biome.contentNote().isEmpty()) {
                continue;
            }
            if (biome.settingsOf(biomes).enabled()) {
                continue;
            }
            warnings.add(
                "world.biomes." + biome.configKey() + " is disabled: "
                    + biome.contentNote()
                    + " will no longer generate unless your pack provides another source."
            );
        }
        return warnings;
    }
```

- [ ] **Step 8: Add the nine TOML tables**

Append to `common/shared/src/main/resources/chronodawn-default-config.toml`, after the last `[world.ores.*]` table and before the `[gameplay...]` tables. Write all nine; the fallback named in each comment must match `ManagedBiome`'s chain exactly. Two examples, in the required shape:

```toml
[world.biomes.desert]
# Whether the Chrono Desert biome generates in Chrono Dawn.
# When disabled, its area of the biome distribution is taken over by
# chronodawn:chronodawn_faded_plains instead of being left empty.
# Only affects newly generated chunks; existing terrain is unchanged.
enabled = true

[world.biomes.snowy]
# Whether the Chrono Snowy biome generates in Chrono Dawn.
# When disabled, its area of the biome distribution is taken over by
# chronodawn:chronodawn_plains instead of being left empty.
# Disabling it stops the Chrono Ursid from generating.
# Only affects newly generated chunks; existing terrain is unchanged.
enabled = true
```

Add the "Disabling it stops ... from generating" line only for biomes whose `contentNote` is non-empty, and keep its wording aligned with `forDisabledBiomes`. Note the warning tail is "will no longer generate", which reads correctly for both singular and plural notes. Do **not** add tables for `plains` or `ocean`.

- [ ] **Step 9: Run the tests to verify they pass**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*' --tests '*ProgressionWarningsTest*' --tests '*ManagedBiomeTest*'
```

Expected: PASS.

- [ ] **Step 10: Verify the TOML drift guard actually fires**

Temporarily delete the `[world.biomes.prairies]` table from the bundled TOML, re-run the command above, and confirm `bundledDefaultTemplate_hasATableForEveryConfigurableBiome` **fails** naming `PRAIRIES`. Restore the table and re-run to confirm green.

- [ ] **Step 11: Commit**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
git add common/shared/src/main common/shared/src/test
git commit -m "feat(config): add [world.biomes.*] enable toggles"
```

---

### Task 4: `RuntimeBiomeOverlay`

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeBiomeOverlay.java`
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/OverlayPackBootstrap.java:88-90`
- Test: `common/shared/src/test/java/com/chronodawn/unit/RuntimeBiomeOverlayTest.java`

**Interfaces:**
- Consumes: `ManagedBiome`, `BiomeSettings`, `ChronoDawnConfig.Biomes` from Tasks 2-3.
- Produces: `RuntimeBiomeOverlay.generate(ChronoDawnConfig) -> Map<String, byte[]>`, keyed by `ManagedBiome.DIMENSION_PACK_PATH`.

- [ ] **Step 1: Write the failing test**

Create `common/shared/src/test/java/com/chronodawn/unit/RuntimeBiomeOverlayTest.java` (LGPL header, then):

```java
package com.chronodawn.unit;

import com.chronodawn.config.BiomeSettings;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ManagedBiome;
import com.chronodawn.worldgen.runtime.RuntimeBiomeOverlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeBiomeOverlayTest {

    @Test
    void packPath_matchesTheBundledResourceLocation() {
        assertEquals("data/chronodawn/dimension/chronodawn.json", ManagedBiome.DIMENSION_PACK_PATH);
    }

    @Test
    void defaultConfig_reproducesTheBundledDimensionJson() {
        Map<String, byte[]> overlay = RuntimeBiomeOverlay.generate(ConfigDefaults.defaults());

        byte[] bytes = overlay.get(ManagedBiome.DIMENSION_PACK_PATH);
        assertNotNull(bytes, "Overlay must contain the dimension JSON");
        assertEquals(loadBundled(), parse(bytes),
            "With every biome enabled the output must be tree-equal to the bundled JSON");
    }

    @Test
    void disablingOneBiome_rewritesOnlyItsBiomeFieldAndKeepsParameters() {
        JsonArray bundled = biomeEntries(loadBundled().getAsJsonObject());
        JsonArray actual = biomeEntries(parse(
            RuntimeBiomeOverlay.generate(configWith(Set.of(ManagedBiome.DARK_FOREST)))
                .get(ManagedBiome.DIMENSION_PACK_PATH)));

        assertEquals(bundled.size(), actual.size(), "Entry count must not change");
        for (int i = 0; i < bundled.size(); i++) {
            JsonObject before = bundled.get(i).getAsJsonObject();
            JsonObject after = actual.get(i).getAsJsonObject();
            assertEquals(before.get("parameters"), after.get("parameters"),
                "Entry " + i + ": parameters must never be touched");
            String expected = ManagedBiome.DARK_FOREST.biomeId().equals(before.get("biome").getAsString())
                ? ManagedBiome.FOREST.biomeId()
                : before.get("biome").getAsString();
            assertEquals(expected, after.get("biome").getAsString(), "Entry " + i + ": biome field");
        }
    }

    @Test
    void disablingSnowy_rewritesBothOfItsEntries() {
        JsonArray entries = biomeEntries(parse(
            RuntimeBiomeOverlay.generate(configWith(Set.of(ManagedBiome.SNOWY)))
                .get(ManagedBiome.DIMENSION_PACK_PATH)));

        long remaining = count(entries, ManagedBiome.SNOWY.biomeId());
        assertEquals(0, remaining, "chronodawn_snowy occupies two entries; both must be rewritten");
    }

    @Test
    void disablingAChain_resolvesToTheFirstEnabledBiome() {
        JsonArray entries = biomeEntries(parse(
            RuntimeBiomeOverlay.generate(configWith(
                    Set.of(ManagedBiome.ANCIENT_FOREST, ManagedBiome.DARK_FOREST, ManagedBiome.FOREST)))
                .get(ManagedBiome.DIMENSION_PACK_PATH)));

        assertEquals(0, count(entries, ManagedBiome.ANCIENT_FOREST.biomeId()));
        assertEquals(0, count(entries, ManagedBiome.DARK_FOREST.biomeId()));
        assertEquals(0, count(entries, ManagedBiome.FOREST.biomeId()));
        assertTrue(count(entries, ManagedBiome.PLAINS.biomeId()) >= 4,
            "plains absorbs its own entry plus the three disabled ones");
    }

    private static long count(JsonArray entries, String biomeId) {
        long n = 0;
        for (JsonElement e : entries) {
            if (biomeId.equals(e.getAsJsonObject().get("biome").getAsString())) n++;
        }
        return n;
    }

    private static JsonArray biomeEntries(JsonObject dimension) {
        return dimension.getAsJsonObject("generator")
            .getAsJsonObject("biome_source")
            .getAsJsonArray("biomes");
    }

    /** A config with exactly the given biomes disabled. */
    private static ChronoDawnConfig configWith(Set<ManagedBiome> disabled) {
        java.util.function.Function<ManagedBiome, BiomeSettings> s =
            b -> new BiomeSettings(!disabled.contains(b));
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        return new ChronoDawnConfig(
            defaults.schemaVersion(),
            new ChronoDawnConfig.World(
                defaults.world().structures(),
                defaults.world().ores(),
                new ChronoDawnConfig.Biomes(
                    s.apply(ManagedBiome.DESERT),
                    s.apply(ManagedBiome.PRAIRIES),
                    s.apply(ManagedBiome.FOREST),
                    s.apply(ManagedBiome.DARK_FOREST),
                    s.apply(ManagedBiome.ANCIENT_FOREST),
                    s.apply(ManagedBiome.SNOWY),
                    s.apply(ManagedBiome.MOUNTAIN),
                    s.apply(ManagedBiome.SWAMP),
                    s.apply(ManagedBiome.FADED_PLAINS)
                )
            ),
            defaults.gameplay()
        );
    }

    private static JsonElement parse(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
    }

    private static JsonElement loadBundled() {
        try (InputStream in = RuntimeBiomeOverlayTest.class.getClassLoader()
            .getResourceAsStream(ManagedBiome.DIMENSION_PACK_PATH)) {
            assertNotNull(in, "Bundled dimension JSON not found on the test classpath");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*RuntimeBiomeOverlayTest*'
```

Expected: compilation failure — `package com.chronodawn.worldgen.runtime.RuntimeBiomeOverlay does not exist`.

- [ ] **Step 3: Write `RuntimeBiomeOverlay`**

Create `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeBiomeOverlay.java` (LGPL header, then):

```java
package com.chronodawn.worldgen.runtime;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ManagedBiome;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Rewrites the Chrono dimension's biome distribution from {@link ChronoDawnConfig}.
 *
 * <p>Unlike the other overlay generators, which build their JSON from nothing, this
 * one <em>transforms</em> the bundled {@code dimension/chronodawn.json}: it replaces
 * the {@code biome} field of every entry whose biome a pack disabled and leaves the
 * {@code parameters} block alone. Keeping the parameters in the resource means the
 * twelve entries are defined in exactly one place and cannot drift.
 *
 * <p>Remapping rather than deleting entries also matters for existing worlds. The
 * biome definitions themselves are never touched, so a disabled biome stays
 * registered and chunks generated before the change still load.
 */
public final class RuntimeBiomeOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeBiomeOverlay.class);
    private static final Gson GSON = new Gson();

    private RuntimeBiomeOverlay() {}

    /**
     * Build the dimension overlay for the given config.
     *
     * @param config the active configuration
     * @return a single-entry map, or an empty map if the bundled resource could not be
     *         read or parsed — in which case the bundled JSON stays in effect
     */
    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        JsonObject dimension;
        try (InputStream in = RuntimeBiomeOverlay.class.getClassLoader()
            .getResourceAsStream(ManagedBiome.DIMENSION_PACK_PATH)) {
            if (in == null) {
                LOGGER.error("Bundled dimension JSON not found at {}; biome toggles will be inactive",
                    ManagedBiome.DIMENSION_PACK_PATH);
                return Map.of();
            }
            dimension = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                .getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Failed to read bundled dimension JSON; biome toggles will be inactive", e);
            return Map.of();
        }

        try {
            remapDisabledBiomes(dimension, config.world().biomes());
        } catch (Exception e) {
            LOGGER.error("Bundled dimension JSON has an unexpected shape; biome toggles will be inactive", e);
            return Map.of();
        }

        Map<String, byte[]> out = new LinkedHashMap<>();
        out.put(ManagedBiome.DIMENSION_PACK_PATH, GSON.toJson(dimension).getBytes(StandardCharsets.UTF_8));
        return out;
    }

    /**
     * Walks the multi-noise entries in place, pointing each disabled biome's entry at
     * the first enabled biome down its fallback chain.
     *
     * <p>Iterating entries rather than biomes is what makes the one biome that owns two
     * entries ({@code chronodawn_snowy}) work without a special case.
     */
    private static void remapDisabledBiomes(JsonObject dimension, ChronoDawnConfig.Biomes biomes) {
        JsonArray entries = dimension.getAsJsonObject("generator")
            .getAsJsonObject("biome_source")
            .getAsJsonArray("biomes");

        for (JsonElement element : entries) {
            JsonObject entry = element.getAsJsonObject();
            Optional<ManagedBiome> managed = ManagedBiome.byBiomeId(entry.get("biome").getAsString());
            if (managed.isEmpty()) {
                continue;
            }
            ManagedBiome resolved = managed.get()
                .resolveFallback(b -> b.settingsOf(biomes).enabled());
            if (resolved != managed.get()) {
                entry.addProperty("biome", resolved.biomeId());
            }
        }
    }
}
```

- [ ] **Step 4: Wire it into the bootstrap**

In `OverlayPackBootstrap.java`, after the existing `overlay.putAll(RuntimePlacedFeatureOverlay.generate(config));` (around line 90), add:

```java
            overlay.putAll(RuntimeBiomeOverlay.generate(config));
```

- [ ] **Step 5: Run the tests to verify they pass**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*RuntimeBiomeOverlayTest*' --tests '*OverlayPackBootstrapTest*'
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
git add common/shared/src/main/java/com/chronodawn/worldgen/runtime common/shared/src/test/java/com/chronodawn/unit/RuntimeBiomeOverlayTest.java
git commit -m "feat(worldgen): remap disabled biomes in the runtime dimension overlay"
```

---

### Task 5: The structural property — biome toggles can never orphan a structure

Every Chrono structure's `has_*` biome tag currently lists `chronodawn_plains`, a core biome, so no combination of toggles can leave a structure with no biome to generate in. This test turns that from an implicit dependency on today's tag contents into a measured guarantee.

**Files:**
- Test: `common/shared/src/test/java/com/chronodawn/unit/ManagedBiomeTest.java` (append)

**Interfaces:**
- Consumes: `ManagedBiome` from Task 2, `ManagedStructure` (existing).
- Produces: nothing consumed by later tasks.

- [ ] **Step 1: Write the failing test**

Append to `ManagedBiomeTest.java`, inside the class:

```java
    /**
     * No combination of biome toggles may leave a Chrono structure with no biome to
     * generate in. This holds today because every {@code has_*} tag lists
     * chronodawn_plains, which is core — but that is a property of the tag files, not
     * something the toggle code enforces, so it is measured rather than assumed.
     *
     * <p>Exhausts all 2^9 combinations of the configurable biomes.
     */
    @Test
    void noCombinationOfBiomeTogglesCanOrphanAStructure() {
        List<ManagedBiome> configurable = ManagedBiome.configurable();
        int combinations = 1 << configurable.size();

        Map<ManagedStructure, Set<String>> tags = new LinkedHashMap<>();
        for (ManagedStructure structure : ManagedStructure.values()) {
            if (structure.dimension() != ManagedStructure.Dimension.CHRONO_DAWN) {
                continue; // Ancient Ruins is placed in Overworld biomes, out of scope here.
            }
            readBiomeTag(structure.configKey()).ifPresent(values -> tags.put(structure, values));
        }
        assertFalse(tags.isEmpty(), "Expected at least one has_* biome tag on the test classpath");

        for (int mask = 0; mask < combinations; mask++) {
            Set<String> enabledIds = new HashSet<>();
            for (ManagedBiome biome : ManagedBiome.values()) {
                if (biome.isCore()) {
                    enabledIds.add(biome.biomeId());
                }
            }
            for (int i = 0; i < configurable.size(); i++) {
                if ((mask & (1 << i)) == 0) {
                    enabledIds.add(configurable.get(i).biomeId());
                }
            }
            for (Map.Entry<ManagedStructure, Set<String>> entry : tags.entrySet()) {
                boolean anyEnabled = entry.getValue().stream().anyMatch(enabledIds::contains);
                assertTrue(anyEnabled,
                    entry.getKey().name() + " has no enabled biome for combination mask " + mask);
            }
        }
    }

    /** Reads the chronodawn: biome IDs out of a has_&lt;key&gt; biome tag, if the tag exists. */
    private static Optional<Set<String>> readBiomeTag(String configKey) {
        String path = "data/chronodawn/tags/worldgen/biome/has_" + configKey + ".json";
        try (java.io.InputStream in = ManagedBiomeTest.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return Optional.empty(); // e.g. forgotten_library, which is placed without a tag.
            }
            com.google.gson.JsonObject tag = com.google.gson.JsonParser
                .parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
            Set<String> values = new HashSet<>();
            for (com.google.gson.JsonElement value : tag.getAsJsonArray("values")) {
                String id = value.getAsString();
                if (id.startsWith("chronodawn:")) {
                    values.add(id);
                }
            }
            return values.isEmpty() ? Optional.empty() : Optional.of(values);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
```

Add the imports `com.chronodawn.config.ManagedStructure`, `java.util.LinkedHashMap`, `java.util.Map`, and `static org.junit.jupiter.api.Assertions.assertFalse` to the file.

- [ ] **Step 2: Run the test to verify it passes**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ManagedBiomeTest*'
```

Expected: PASS. (This test asserts an existing property, so unlike the others it passes on first run — Step 3 is what proves it has teeth.)

- [ ] **Step 3: Verify the guard actually fires**

Temporarily remove `"chronodawn:chronodawn_plains"` from `common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_guardian_vault.json`, re-run the command above, and confirm the test **fails** naming `GUARDIAN_VAULT`. Restore the entry and re-run to confirm green.

- [ ] **Step 4: Commit**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
git add common/shared/src/test/java/com/chronodawn/unit/ManagedBiomeTest.java
git commit -m "test(worldgen): prove no biome toggle combination can orphan a structure"
```

---

### Task 6: Documentation and full verification

**Files:**
- Modify: `docs/configuration.md` — new `### [world.biomes.*]` section after `### [world.structures.*]` (which ends at line 150)
- Modify: `CHANGELOG.md` — `[Unreleased]` → `Added`
- Modify: `README.md` — the configuration feature list

**Interfaces:**
- Consumes: everything above.
- Produces: nothing.

- [ ] **Step 1: Write the `docs/configuration.md` section**

Insert after the `[world.structures.*]` section, matching that section's structure (intro, table, example, notes). The outer fence below is four backticks so the inner TOML block survives; write the inner content into the doc as a normal three-backtick block:

````markdown
### `[world.biomes.*]`

Controls which biomes generate in the Chrono dimension. One table per biome:
`desert`, `prairies`, `forest`, `dark_forest`, `ancient_forest`, `snowy`,
`mountain`, `swamp`, `faded_plains`.

| Key | Type | Default | Meaning |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | Whether the biome generates |

```toml
[world.biomes.dark_forest]
enabled = false
```

**A disabled biome is replaced, not removed.** The dimension places biomes by
sampling a multi-noise parameter space; a disabled biome's region of that space is
handed to a fallback biome rather than left empty, so the result is predictable:

| Biome | Replaced by |
| --- | --- |
| `ancient_forest` | `dark_forest` |
| `dark_forest` | `forest` |
| `forest` | `plains` |
| `swamp` | `forest` |
| `desert` | `faded_plains` |
| `faded_plains` | `plains` |
| `prairies` | `plains` |
| `snowy` | `plains` |
| `mountain` | `plains` |

Fallbacks chain: with both `forest` and `dark_forest` disabled, `ancient_forest`
becomes `plains`.

**`plains` and `ocean` cannot be disabled** and have no table. They terminate the
fallback chains. `ocean` additionally owns the whole low-continentalness band of
the parameter space, so replacing it with a land biome would put grassland under
every ocean in the dimension.

**Effect on an existing world.** Biomes are written into a chunk when it is
generated, so changing these settings mid-world leaves already-explored terrain
exactly as it is and applies only to newly generated chunks. Expect a visible seam
at the edge of the explored area, the same as when changing structure spacing.
Disabled biomes stay registered, so existing chunks containing them keep loading
normally.

**Structures are unaffected.** Every Chrono Dawn structure can generate in
`plains`, so no combination of biome toggles can make a structure ungenerable. Use
[`[world.structures.*]`](#worldstructures) to control structures.
````

- [ ] **Step 2: Add the CHANGELOG entry**

Under `## [Unreleased]` → `### Added` in `CHANGELOG.md`:

```markdown
- Per-biome generation toggles: `[world.biomes.*]` in `chronodawn.toml` disables any of
  the nine non-core Chrono dimension biomes. A disabled biome's region of the biome
  distribution is taken over by a documented fallback biome rather than left empty.
  `plains` and `ocean` are always generated. Existing terrain is unaffected; only newly
  generated chunks change.
```

- [ ] **Step 3: Update the README configuration list**

Find where `README.md` lists what the config file controls (search for `world.structures`) and add a matching bullet for `[world.biomes.*]` in the same style and level of detail as its neighbours.

- [ ] **Step 4: Run the full verification suite**

This is the gate before the branch is considered done. It takes a long time; do not start anything else against this worktree while it runs.

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
mkdir -p build/logs
./gradlew checkAll > build/logs/checkAll.log 2>&1; echo "exit=$?"
```

Expected: `exit=0`. Note the standing caveat that the `buildAll` / `gameTestAll` wrappers can fail spuriously through a `RemapSourcesJarTask` race — if the log shows that specific failure, re-run the affected version standalone (`./gradlew build1_21_11 -Ptarget_mc_version=1.21.11`) and treat the standalone result as authoritative. Any other failure is real.

Do not report success without pasting the tail of the log. A green claim without the command output is not evidence.

- [ ] **Step 5: In-game verification**

Unit tests confirm the JSON this code produces; only a running game confirms Minecraft accepts it. That splits into an automated half and a manual half.

**Automated (already covered by Step 4).** Once `RuntimeBiomeOverlay` is wired into `OverlayPackBootstrap`, *every* server boot writes and loads the overlay's dimension JSON — so the `gameTestAll` stage of `checkAll` exercises "Minecraft accepts our generated dimension JSON" on all eleven versions and both loaders. If the generated JSON were malformed or referenced an unregistered biome, world load would fail and every GameTest would fail with it. No new GameTest class is needed for this; confirm Step 4 was green rather than adding one.

**Manual (the disabled case).** The GameTest harness boots with the default config and has no hook for supplying a modified `chronodawn.toml`, so the disabled-biome path cannot be automated within it. Verify it by hand:

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21; export PATH="$JAVA_HOME/bin:$PATH"
./gradlew runClientFabric1_21_11
```

(Run with the sandbox disabled — the client forks a JVM.)

1. Launch once to materialise `run/config/chronodawn.toml`, then quit.
2. Set `[world.biomes.snowy] enabled = false`.
3. Relaunch and confirm the startup log contains the `world.biomes.snowy is disabled: the Chrono Ursid ...` warning.
4. Create a new world, enter the Chrono dimension, and confirm it loads with no error.
5. `/locate biome chronodawn:chronodawn_snowy` — expect it to fail to find one.
6. `/locate biome chronodawn:chronodawn_plains` — expect success, confirming the dimension is still generating normally.

- [ ] **Step 6: Commit**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
git add docs/configuration.md CHANGELOG.md README.md
git commit -m "docs: document [world.biomes.*] generation toggles"
```

- [ ] **Step 7: Update the roadmap**

In `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`, update the "Dimension-level toggles" bullet under sub-project A and the status-tracker row: the tunable is no longer *partial* — both the per-structure and per-biome halves have shipped. Add a link to `2026-08-23-biome-toggles-design.md`.

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/biome-toggles
git add docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md
git commit -m "docs: mark dimension-level toggles complete in the readiness roadmap"
```

---

## Notes for the executor

- **`common/shared/` is not a Gradle subproject.** Editing files there and running `:common-1.21.11:test` is correct; there is no `:common-shared:test`.
- **Nine configurable biomes, eleven total.** `plains` and `ocean` are in `ManagedBiome` (they are fallback targets) but excluded from `configurable()` and from the TOML.
- **`chronodawn_snowy` owns two multi-noise entries.** Any code that walks biomes rather than entries will silently miss one; that is why the overlay iterates entries.
- **Do not "fix" the bundled dimension JSON's formatting.** Overlay tests compare parsed JSON trees, not bytes, precisely so re-serialization differences are not failures.
- If a guard-verification step (2.6, 3.10, 5.3) does *not* fail when you inject the violation, stop and report it. A guard that cannot fail is worse than no guard.
