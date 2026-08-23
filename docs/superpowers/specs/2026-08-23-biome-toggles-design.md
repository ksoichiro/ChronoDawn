# Design: Per-Biome Generation Toggles

**Created**: 2026-08-23
**Status**: Design approved — implementation pending
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A
**Companion**: [Per-Structure Generation Toggles](./2026-08-22-structure-toggles-design.md)

---

## Why

The roadmap's "dimension-level toggles" tunable was deliberately split in two. The
per-structure half shipped; this spec covers the per-biome half, which was held
back because it needs decisions the structure work did not.

Pack authors disable biomes to narrow a dimension's visual identity, to remove a
biome that clashes with another mod's content, or to cut the mob and decoration
set a biome brings with it. The structure toggles gave them one half of that
control; without biome toggles the Chrono dimension's landscape is fixed.

## What a "disabled" biome means

The Chrono dimension's biome distribution is a `minecraft:multi_noise` biome
source: a list of entries, each pairing a biome ID with a box in the
temperature / humidity / continentalness / erosion / depth / weirdness parameter
space. Disabling a biome could mean dropping its entries, but that leaves a hole
in the parameter space that vanilla fills by nearest-match — so the pack author
cannot predict which neighbour absorbs the region.

**A disabled biome is therefore remapped, not removed.** Its entries keep their
`parameters` block untouched; only the `biome` field is rewritten to a fallback
biome. The region keeps its exact shape and position; a different biome occupies
it. The result is deterministic and stated up front in the config comment.

### Core biomes

`chronodawn_plains` and `chronodawn_ocean` cannot be disabled. They are the
terminals of the fallback chains, so they must always be available.

`plains` is the generic land fallback. `ocean` is separate because it alone owns
the `continentalness [-1.0, -0.11]` band; filling that band with a land biome
would put grassland underwater across every ocean in the dimension. A pack author
who wants no oceans is better served by editing the noise settings than by a
toggle that produces a visibly broken result.

Neither biome gets a config table at all. Their absence from the config file *is*
the statement that they are not disableable — there is no `enabled = true` that a
pack author can flip and have silently ignored.

### Fallback chains

| Biome | Fallback |
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
| `plains` | — (core) |
| `ocean` | — (core) |

Chains prefer visual continuity over the shortest path: disabling
`ancient_forest` yields dark forest rather than open plains, and disabling
`desert` yields the faded plains rather than green grassland. Resolution follows
the chain until it reaches an enabled biome, so disabling `forest` and
`dark_forest` together resolves `ancient_forest` all the way to `plains`.

## Effect on existing worlds

The overlay applies to existing worlds, not only to newly created ones.

This was verified against Minecraft's own code rather than assumed.
`WorldDimensions.bake(Registry<LevelStem>)` resolves each dimension key as:

```java
registry.getOptional(key).or(() -> this.dimensions.get(key))
```

The datapack-loaded `LEVEL_STEM` registry wins; the `WorldGenSettings.dimensions`
map persisted in `level.dat` is only a fallback for keys the datapack does not
provide. Because `chronodawn:chronodawn` is always provided by the mod's
datapack — bundled or overlaid — the overlay version is what the game uses, even
for a world created long before.

(Existing `level.dat` files in this repo's dev saves do carry stale biome lists
from whenever the world was created. That copy is retained by `PrimaryLevelData`
and re-serialized on save, but it is not what the generator reads.)

The behaviour therefore splits into three layers:

- **Already-generated chunks**: unchanged. Since 1.18, biomes are written into
  the chunk's palette at generation time; the biome source is consulted only when
  generating a chunk, never when loading one. Terrain, decoration and structures
  already placed stay exactly as they are.
- **Not-yet-generated chunks**: generated with the new distribution. A pack author
  who changes this mid-world gets a seam at the boundary of the explored area —
  the same behaviour as changing structure spacing, and documented as such.
- **The biome registry**: unchanged. Disabled biomes stay registered.

That last point is a safety property of remapping rather than deleting. The
design never touches `data/chronodawn/worldgen/biome/*.json`. If it deleted the
biome definition of a disabled biome, every existing chunk referencing that biome
would fail to load. Remapping the dimension entries cannot cause that.

## Components

### `ManagedBiome` (new enum, `com.chronodawn.config`)

The single source of truth for the set of biomes whose generation is exposed to
configuration, mirroring `ManagedStructure`. The config parser, the runtime
overlay and the guard tests all iterate it, so a biome registered here appears in
all three and one that is not appears in none — a biome cannot be half-wired.

Unlike `ManagedStructure`, it does **not** hold the multi-noise parameters. Those
stay in the bundled JSON (see "Overlay" below).

| Field | Example | Purpose |
| --- | --- | --- |
| `configKey` | `dark_forest` | TOML table name under `[world.biomes]` |
| `biomeId` | `chronodawn:chronodawn_dark_forest` | matches the `biome` field in the dimension JSON |
| `fallbackName` | `"FOREST"` | the biome to remap to when disabled; `null` marks a core biome |
| `contentNote` | `"the Chrono Ursid"` | what a pack loses by disabling it; empty means no warning |

`fallbackName` is stored as a `String` and resolved lazily through `valueOf()`,
the same workaround `ManagedStructure.exclusionZone()` uses: the JLS forbids an
enum constant's initializer from referencing a later-declared sibling, even
through a lambda, and the chains here point forward.

Settings are a new `BiomeSettings(boolean enabled)` record rather than a reuse of
`StructureSettings`. A biome has no spacing, separation or salt analogue; the
record has exactly one field and no cross-field validation.

### `RuntimeBiomeOverlay` (new, `com.chronodawn.worldgen.runtime`)

Same contract as the two existing overlay generators —
`generate(ChronoDawnConfig) -> Map<String, byte[]>` — so `OverlayPackBootstrap`
gains a single line next to the existing two.

Unlike `RuntimeStructureOverlay` and `RuntimePlacedFeatureOverlay`, which build
their JSON from nothing by string concatenation, this generator **transforms the
bundled JSON**:

1. Read `/data/chronodawn/dimension/chronodawn.json` from the classpath. The
   common module is bundled into both loaders' JARs, so one code path serves both.
2. Parse with Gson (already used in `ChronicleData`; Minecraft ships it).
3. Walk `generator.biome_source.biomes`, looking up each entry's `biome` value
   through `ManagedBiome.byBiomeId()`.
4. When the entry's biome is disabled, replace the `biome` value with the
   resolved end of its fallback chain. Leave `parameters` untouched.
5. Re-serialize and return it under the same pack-relative path.

Transforming rather than regenerating is the point of the design: the twelve
entries and their seven parameters each stay defined in exactly one place. The
alternative — moving the parameters into `ManagedBiome` and guarding the copy
against drift, as `ConfigDefaults` does for the bundled TOML — was considered and
rejected: the duplication is large enough that a guard test only tells you the
two copies disagree, without making them any easier to keep in step.

Iterating over entries rather than biomes also handles `chronodawn_snowy`, the
one biome holding two entries in the parameter space, with no special case.

**Failure handling**: if the resource cannot be read or the JSON does not parse,
log an error and return an empty map. Emitting no dimension file leaves the
bundled JSON in effect, so worldgen falls back to the shipped defaults. This
matches `OverlayPackBootstrap`'s existing stance of disabling the overlay rather
than writing something broken.

**All biomes disabled** is unreachable: the two core biomes have no config table,
so at least two always remain. No special case is needed, and none is written.

### Config surface

Nine tables, one per non-core biome:

```toml
[world.biomes.dark_forest]
# Whether the Dark Forest biome generates in Chrono Dawn.
# When disabled, its area of the biome distribution is taken over by
# chronodawn:chronodawn_forest instead of being left empty.
# Only affects newly generated chunks; existing terrain is unchanged.
enabled = true
```

Naming the fallback in the comment is the one place this differs from the
structure tables. A pack author cannot make an informed decision without knowing
what replaces the biome, and the answer differs per biome.

`ChronoDawnConfig.World` gains a `Biomes biomes` component. The existing
`World(Structures, OresConfig)` constructor is kept and delegates to
`ConfigDefaults.BIOME_DEFAULTS`, following the same backward-compatible pattern as
`Structures(StructureSettings)` and `Gameplay(BossesConfig)`. `schemaVersion`
stays at 1, as it did for the structure toggles.

`ConfigLoader` gains `parseBiomes` / `parseBiome(parsed, ManagedBiome)`. With a
single boolean and no interacting fields there is no clamping logic, so none of
the `parseStructure` spacing/separation handling carries over.

### Startup warnings

`ProgressionWarnings` gains `forDisabledBiomes()`, in the same voice as
`forDisabledStructures()`: disabling is allowed, so the mod states the
consequence rather than refusing the setting.

Two `contentNote` values are certain, being the only mobs that spawn in exactly
one biome:

- `chronodawn_snowy` — the Chrono Ursid
- `chronodawn_mountain` — the Temporal Caprid

The remaining notes are **not fixed by this design**. Twenty-five
`chronodawn:`-namespaced features are exclusive to a single biome, but exclusivity
of a *feature* does not imply exclusivity of the *blocks* it places — a block may
well have another source in another biome or from another feature. Guessing here
produces warnings that are wrong in both directions: claiming a loss that is not
real, or staying silent about one that is.

The design fixes only the mechanism (a biome with a non-empty `contentNote`
produces a warning). Determining the values is an implementation task with a
stated method: for each biome, expand its `features` array through each
`placed_feature` to its `configured_feature` and collect the block IDs placed;
then a block is exclusive to that biome only if it appears in no other biome's
expansion. See "Implementation notes" below.

## Testing

### `ManagedBiomeTest` — the enum's internal consistency

- Every biome's fallback chain terminates at a core biome within a finite number
  of steps, with a visited-set loop check. A mistyped fallback fails here.
- Exactly two core biomes, `plains` and `ocean`, and `fallbackName == null` holds
  for core biomes and only for them.
- No duplicate `configKey` or `biomeId`.

### Drift guards

- The set of `ManagedBiome.biomeId()` values equals the set of `biome` values in
  the bundled dimension JSON. Adding a biome to the dimension without registering
  it here fails. Comparing sets rather than lists absorbs the duplicated `snowy`
  entry.
- The `[world.biomes.*]` tables in the bundled TOML equal the non-core
  `ManagedBiome` set — the same shape as the `ConfigDefaults` TOML guard added in
  `2620fa85`.

### `RuntimeBiomeOverlayTest`

- With everything enabled, the output is semantically equal to the bundled JSON.
  Compared as parsed `JsonObject`s rather than bytes, since re-serialization may
  legitimately change formatting.
- With one biome disabled, only that entry's `biome` field changes, and its
  `parameters` object is untouched.
- With `snowy` disabled, **both** of its entries are rewritten.
- With `forest` and `dark_forest` both disabled, `ancient_forest`'s entry resolves
  through the chain to `plains`.
- Malformed input returns an empty map and throws nothing.

### The structural property

For all 2⁹ = 512 enabled/disabled combinations of the non-core biomes, every
`has_*` biome tag still contains at least one enabled biome.

Every Chrono structure's biome tag currently includes `chronodawn_plains`, which
is a core biome, so no combination of biome toggles can orphan a structure. That
is worth having as a *measured* guarantee rather than an implicit dependency on
today's tag contents: if `plains` is ever dropped from a `has_*` tag, or the set
of core biomes changes, this test catches it. Exhausting 512 cases is instant.

### In-game verification

Unit tests verify the JSON this code produces; only a running game verifies that
Minecraft accepts it. That splits in two.

**Automatic, once the overlay is wired.** Every server boot writes and loads the
overlay's dimension JSON, so the existing `gameTestAll` stage covers "Minecraft
accepts our generated dimension JSON" across all eleven versions and both loaders
without a new test class: malformed JSON or a reference to an unregistered biome
would fail world load and take every GameTest down with it.

**Manual, for the disabled case.** The GameTest harness boots with the default
config and offers no hook for supplying a modified `chronodawn.toml`, so the
disabled-biome path cannot be automated within it. It is verified by hand: confirm
the startup warning, that the dimension loads, that `/locate biome` fails for the
disabled biome, and that it still succeeds for a core one.

### Guard verification

Per the project's standing lesson, each guard test above is verified by injecting
a deliberate violation, confirming it turns red, and reverting. A guard that has
only ever been seen green is not evidence that it guards anything.

## Implementation notes

The biome-exclusivity audit that produces the `contentNote` values is its own
task, ahead of the `ManagedBiome` work that consumes its output. Its inputs:

- Biome-exclusive mobs, already determined: Chrono Ursid (`snowy`), Temporal
  Caprid (`mountain`). Vanilla mobs exclusive to `ocean` and `swamp` are not
  relevant — `ocean` is core, and `frog` is a vanilla mob a pack can source
  elsewhere.
- Biome-exclusive features, to be expanded to block IDs and cross-checked as
  described above.

A biome with nothing exclusive gets an empty `contentNote` and produces no
warning; that is a legitimate outcome, not a gap.

## Out of scope

- Exposing multi-noise parameters (temperature / humidity ranges) for tuning.
  Considered and rejected: it demands multi-noise fluency from pack authors and
  makes broken distributions easy to produce.
- A pack-author-specified fallback (`fallback = "chronodawn:..."`). The extra
  config surface would need validation for unknown IDs, cycles and references to
  disabled biomes, for a degree of control no request has asked for.
- Per-biome feature or mob toggles. A finer granularity than this spec, and
  independent of it.

## Audit results

Produced by the method in "Implementation notes", run against the 1.21.11
biome definitions on 2026-08-23. The exclusive-feature list was expanded to
`placed_feature` -> `configured_feature` chains (including `nbt_template`
structures and `random_selector`/`random_patch` wrappers), each candidate
block was checked against every other biome's placed features, and any
survivor was cross-checked against `common/*/recipe/*.json` for a crafting
path that would make it obtainable without the biome.

| Biome | `contentNote` | Basis |
| --- | --- | --- |
| `desert` | `the Hourglass Monolith landmark` | `chronodawn:hourglass_monolith` is placed only via `hourglass_monolith_placed` in `chronodawn_desert`; no other biome references the template, and it has no recipe |
| `prairies` | (empty) | Exclusive blocks are `chronodawn:coarse_temporal_dirt` and `chronodawn:tall_grass_block` — terrain-palette variants with no recipe and no use as a crafting ingredient anywhere; decoration only |
| `forest` | (empty) | Only exclusive feature is `fruit_of_time_tree_dense`; the underlying `fruit_of_time_tree` (log/leaves/fruit) also generates in `mountain`, `prairies`, `swamp`, and the core `plains`/`ocean` biomes, so nothing is actually lost |
| `dark_forest` | `Dark Time Wood trees` | `chronodawn:dark_time_wood_log`/`dark_time_wood_leaves` generate only via the `dark_time_wood_tree_*` variants in `chronodawn_dark_forest`; the log has planks/stripped/charcoal recipes that consume it but none that produce it, so it is not obtainable elsewhere |
| `ancient_forest` | (empty) | Exclusive features (`ancient_time_wood_tree_dense`, `upside_down_tree`) reuse `chronodawn:ancient_time_wood_log`/leaves, which also generate via the core `chronodawn_plains` biome's `ancient_time_wood_tree`; the upside-down variant is a decorative reskin, not a new block |
| `snowy` | `the Chrono Ursid` | only biome the mob spawns in |
| `mountain` | `the Temporal Caprid` | only biome the mob spawns in |
| `swamp` | (empty) | `swamp_mud_disk`/`swamp_pond` place vanilla `minecraft:mud` and `minecraft:water`, both obtainable everywhere |
| `faded_plains` | (empty) | Exclusive blocks are `chronodawn:parched_temporal_dirt`, `chronodawn:faded_grass_block`, `chronodawn:temporal_dead_bush_block` — terrain-palette variants with no recipe and no use as a crafting ingredient anywhere; `ancient_fallen_log`/`dead_snag` reuse the non-exclusive `ancient_time_wood_log`; decoration only |

Biomes with an empty `contentNote` place nothing that is unobtainable
elsewhere; disabling them costs decoration and scenery only.
