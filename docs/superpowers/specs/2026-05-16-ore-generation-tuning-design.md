# Design: Ore Generation Tuning

**Date**: 2026-05-16
**Status**: Approved scope, pending implementation
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A, second PR
**Predecessor PR**: [Config Foundation + Ancient Ruins Tuning](./2026-05-09-config-foundation-design.md)
**Branch (planned)**: `ore-generation-tuning`

---

## Problem

Chrono Dawn's three ChronoDawn-specific ores in the Chrono dimension —
Time Crystal, Entropy Crystal, and Temporal Amber — have their generation
parameters (count per chunk, Y range) baked into bundled placed_feature
JSONs. Modpack creators who want to make a specific ore rarer for
progression pacing, or push it to a different Y band, must either patch
the mod or ship a full datapack override. "Make Entropy Crystal rarer"
and "lower the Temporal Amber Y range" are the kind of small tuning
requests we expect to see most often; exposing these as TOML knobs is a
natural second slice for the config infrastructure shipped in the
previous PR.

## Goals

1. Expose `enabled`, `count`, `y_min`, `y_max` for three ChronoDawn ores
   (`time_crystal`, `entropy_crystal`, `temporal_amber`) via
   `config/chronodawn.toml`.
2. Reuse the existing overlay-pack infrastructure
   (`OverlayPackBootstrap` / `OverlayPackPlatform`) by adding a
   `RuntimePlacedFeatureOverlay` next to `RuntimeStructureOverlay`.
3. Guarantee that the default config produces placed_feature JSON
   semantically equivalent to the bundled files (compared as parsed
   JSON trees) via a round-trip unit test, so any future drift between
   hardcoded defaults and bundled JSON is caught immediately.

## Non-goals

- `chronodawn:ore_clockstone` tuning — its bundled `count` differs
  between 1.20.1 (`8`) and 1.21.1+ (`16`). Exposing it would require
  version-aware defaults, which is its own design decision. Deferred to
  a follow-up PR that also resolves the bundled-value discrepancy.
- `chronodawn:ore_iron / gold / coal / redstone` (vanilla-block overlay
  ores in the Chrono dimension) — present as balance scaffolding rather
  than pack-author-facing knobs; will be reconsidered if a request
  surfaces.
- Per-biome ore adjustments — biome JSONs attach placed_features, so
  per-biome tuning would require expanding the overlay to biome
  resources. This conflicts with the planned dimension-level toggle slice
  and is deferred.
- User-controlled height distribution type
  (`minecraft:trapezoid` vs `minecraft:uniform`) — each ore keeps its
  bundled distribution. Authors who need a different shape can ship a
  full datapack override.
- `configured_feature` `size` (blocks per cluster) — same rationale.
- Hot reload of worldgen values; retroactive changes to existing
  chunks — vanilla constraints, identical to the Ancient Ruins slice.

## TOML schema (additions)

Appended to `chronodawn-default-config.toml` immediately after the
existing `[world.structures.ancient_ruins]` section. Default values
match the current bundled placed_feature JSONs exactly.

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

## Architecture

### Configuration types

```java
public record OreSettings(boolean enabled, int count, int yMin, int yMax) {}

public record OresConfig(
    OreSettings timeCrystal,
    OreSettings entropyCrystal,
    OreSettings temporalAmber
) {}
```

`WorldConfig` (already present from the config foundation PR) gets an
`ores()` accessor returning `OresConfig`, so call sites read
`config.world().ores().timeCrystal().count()`.

`ConfigDefaults` adds three constants that mirror the bundled JSON byte
for byte (verified by `RuntimePlacedFeatureOverlayTest`):

```java
public static final OreSettings TIME_CRYSTAL_DEFAULTS = new OreSettings(true, 3, 0, 48);
public static final OreSettings ENTROPY_CRYSTAL_DEFAULTS = new OreSettings(true, 4, 40, 100);
public static final OreSettings TEMPORAL_AMBER_DEFAULTS = new OreSettings(true, 4, -30, 20);
```

### Overlay generation flow

`RuntimePlacedFeatureOverlay` is added next to `RuntimeStructureOverlay`
in `common/shared/src/main/java/com/chronodawn/worldgen/runtime/` and
follows the same conventions (`String`-literal JSON construction; no
JSON library dependency).

For each of the three ores:

- The emitted placed_feature JSON has the four placement modifiers used
  by the bundled files: `count`, `in_square`, `height_range`, `biome`.
- The `height_range.height.type` is hardcoded per ore in the emitter
  (`trapezoid` for `time_crystal` and `entropy_crystal`, `uniform` for
  `temporal_amber`) and is **not** exposed via TOML.
- The output path is
  `data/chronodawn/worldgen/placed_feature/ore_<id>.json` for each ore.

`OverlayPackBootstrap` calls both
`RuntimeStructureOverlay.generate(config)` and
`RuntimePlacedFeatureOverlay.generate(config)` and merges the resulting
maps into a single set of overlay entries that the loader-specific
provider registers as a virtual pack.

### Disabled state representation

`enabled = false` results in the same placed_feature JSON shape, but
with `count = 0`. The remaining `count` and Y values from the user's
TOML are preserved verbatim in the emitted JSON; only the leading
`minecraft:count` step's `count` field is forced to `0`. Rationale:

- Mirrors the Ancient Ruins disabled-state design: keep the resource
  registered so biome references resolve, evaluate to zero placements at
  runtime.
- Round-tripping the user's chosen `count` / `y_min` / `y_max` to the
  JSON (rather than rewriting them to defaults) means re-enabling the
  ore restores the user's last numbers, not the mod defaults.

Effect on chunk generation: `count(0)` yields zero starting positions
for the placement chain, so the downstream `in_square` / `height_range`
/ `biome` steps run zero times and no ore is placed.

### Validation rules

Applied per ore inside `ConfigLoader`:

- `enabled` must be a boolean. Non-boolean → revert that field to
  default, log at `ERROR`.
- `count` must satisfy `0 <= count <= 64`. Out of range → revert.
- `y_min` and `y_max` must satisfy
  `-64 <= y_min <= y_max <= 320`. Out of range or inverted → revert
  both to defaults for that ore.
- Field-level revert: invalid `count` leaves `enabled` / `y_min` /
  `y_max` alone (and vice versa). Other ores are unaffected.

Unknown keys (`[world.ores.nonexistent]`) are warned and ignored, same
as the existing config loader behavior.

### Cross-version coverage

- All three target ores' bundled JSONs are identical across the 11
  supported MC versions. `ConfigDefaults` therefore stays version-free.
- `RuntimePlacedFeatureOverlay` lives in `common/shared/` and is shared
  by every version's common module.
- No changes to `OverlayPackPlatform` (the loader interface) or its
  Fabric / NeoForge implementations. The new overlay entries are added
  inside the existing virtual pack.

### Schema versioning

`schema_version = 1` is preserved. The new `[world.ores.*]` sections
are additive; old configs missing them load with all-default values,
matching the bundled JSON exactly.

## Files

### New

- `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java`
- `common/shared/src/main/java/com/chronodawn/config/OreSettings.java`
- `common/shared/src/main/java/com/chronodawn/config/OresConfig.java`
- `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`
  (mirror of `RuntimeStructureOverlayTest`)
- Test cases added to `ConfigLoaderTest` for the new sections.

### Modified

- `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java`
  — `WorldConfig` gains `ores()` accessor.
- `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`
  — three new defaults + builder wiring.
- `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
  — read / validate / default-revert for `[world.ores.<id>]`.
- `common/shared/src/main/resources/chronodawn-default-config.toml`
  — three new sections (content above).
- `common/shared/src/main/java/com/chronodawn/worldgen/runtime/OverlayPackBootstrap.java`
  — call the new overlay generator and merge results.
- `docs/configuration.md` — add an "Ore generation" section listing
  the three ores, each key, validation ranges, and the standard
  reminders that changes require a restart and do not retro-fit
  existing chunks.
- `docs/modpack-integration.md` — optional one-snippet example
  showing how to rebalance a single ore (kept short; this doc grows
  per-slice).
- `CHANGELOG.md` — single line under `[Unreleased]` `### Added`:
  `Ore generation tuning for Time Crystal, Entropy Crystal, and Temporal Amber via config`.
- `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`
  — update the sub-project A status note (second slice shipped) and
  record `ore_clockstone` as a deferred item in the Planned follow-up
  tunables list (note that it depends on version-aware defaults).

### Untouched (deliberate)

- `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_time_crystal.json`
- `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_entropy_crystal.json`
- `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_temporal_amber.json`
  — bundled JSONs stay as the source of truth for shape; overlay
  emits identical content when all values are at defaults.
- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_*.json`
  — cluster `size` and target predicates remain untouched.
- `ore_clockstone`, `ore_iron`, `ore_gold`, `ore_coal`, `ore_redstone`
  placed_features — out of scope.

## Verification plan

### Unit tests

`RuntimePlacedFeatureOverlayTest`:

- Default config → for each of the three ores, generated JSON parses to
  the same tree as the bundled file (compared as parsed JSON, not raw
  bytes; formatting differences are acceptable).
- `time_crystal.count = 10` → emitted JSON's `count` step is `10`.
- `entropy_crystal.enabled = false` → emitted JSON's `count` step is
  `0`; other placement steps and `height_range` values are unchanged.
- `temporal_amber.y_min = -20, y_max = 30` → emitted `height_range.min_inclusive.absolute = -20`
  and `max_inclusive.absolute = 30`; `height.type` is still
  `minecraft:uniform`.
- Per-ore distribution type is asserted directly
  (`time_crystal` / `entropy_crystal` → `trapezoid`,
  `temporal_amber` → `uniform`).

`ConfigLoaderTest` additions:

- `[world.ores.time_crystal] count = -5` → reverts to default `3`,
  ERROR logged.
- `[world.ores.entropy_crystal] y_min = 50, y_max = 40` → reverts to
  defaults `(40, 100)` for that ore, ERROR logged.
- Unknown key `[world.ores.unknown]` → WARN logged, ignored, other
  ores unaffected.

### Manual integration

1. Build for **1.21.11** Fabric. Run client. Generate a new world,
   visit the Chrono dimension. Confirm visually that all three ores
   generate at default density (regression check).
2. Stop. Edit `time_crystal.count = 20`. Restart. Generate a new
   Chrono-dim chunk and confirm visibly higher Time Crystal density.
3. Stop. Edit `temporal_amber.enabled = false`. Restart. Generate a
   new chunk and confirm no Temporal Amber ore appears.
4. Repeat steps 1–3 on **1.21.1** Fabric. (1.21.1 + 1.21.11 are the
   primary manual-verification pair established by the Ancient Ruins
   PR; `checkAll` covers the rest.)
5. `./gradlew checkAll` to validate all 11 versions × 2 loaders
   (1.20.1 Fabric-only). `gameTestAll` is skipped — this PR does not
   touch GameTest sources, matching the precedent set by the Ancient
   Ruins PR.

## Risks

| Risk | Mitigation |
| --- | --- |
| `ConfigDefaults` drifts from bundled JSON | Round-trip unit test asserts `generate(defaults)` parses to the same tree as bundled JSON; broken on first run. |
| User sets `y_min > y_max` | `ConfigLoader` validation reverts to defaults for that ore, logs `ERROR`. |
| `enabled = false` representation breaks on a future MC version | `count(0)` is a stable placement modifier across the supported 1.20.1–1.21.11 range; `checkAll` plus the unit test will flag a future regression. |
| New MC version added later requires a different ore JSON shape | Same maintenance category as the existing structure-set overlay; tracked in the roadmap document. |
| Existing chunks do not reflect changes | Documented in `docs/configuration.md` "Ore generation" section. |

## Open questions

All resolved at design time:

- ✅ Tuning surface: absolute `count` + `y_min` + `y_max` + `enabled`
  (no multipliers, no `size`).
- ✅ Distribution type: frozen to bundled per ore, not user-tunable.
- ✅ Scope: three ores, not four; `ore_clockstone` deferred for
  version-aware defaults.
- ✅ Per-biome control: deferred (out of scope).
- ✅ Schema version: stays at `1` (additive change).
- ✅ Disabled representation: `count = 0` in emitted JSON; preserve
  user's `count` / `y_min` / `y_max` for round-tripping.

## Extension pattern (recap)

This PR exercises the extension recipe documented in the config
foundation spec ("Extension pattern (for future tunables)"). New
nested record → defaults → bundled TOML comment → overlay generator →
docs → tests. No infrastructure changes were needed, which is the
point of the previous PR's scope.

## Out of scope (explicit)

- `ore_clockstone` tuning (needs version-aware defaults)
- Vanilla-overlay ores in the Chrono dimension (`ore_iron`, `gold`,
  `coal`, `redstone`)
- Per-biome ore rates
- Distribution type customization
- `configured_feature` `size`
- Hot reload / retroactive chunk updates

## Done criteria

- All files in the "Files / New + Modified" section exist or are
  updated.
- Unit tests pass for all eligible versions (`./gradlew testAll`).
- Manual integration verification (steps 1–3) passes on both 1.21.1
  and 1.21.11.
- `./gradlew checkAll` succeeds.
- `docs/configuration.md` "Ore generation" section reviewed for
  clarity.
- CHANGELOG entry written.
- Roadmap document's status tracker updated to reflect that the second
  slice of sub-project A has shipped, and `ore_clockstone` is recorded
  as a deferred follow-up tied to version-aware defaults.
