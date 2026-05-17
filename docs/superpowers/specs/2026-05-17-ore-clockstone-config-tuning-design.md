# Design: Ore Clockstone Config Tuning

**Date**: 2026-05-17
**Status**: Approved scope, pending implementation
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A, third PR
**Predecessor PR**: [Ore Generation Tuning (Time Crystal / Entropy Crystal / Temporal Amber)](./2026-05-16-ore-generation-tuning-design.md)
**Branch (planned)**: `ore-clockstone-config-tuning`

---

## Problem

Clockstone Ore (`chronodawn:ore_clockstone`) — the most common tier-1
crafting material in the Chrono dimension — has its generation
parameters baked into bundled placed_feature JSONs. Modpack creators
who want to tune its abundance for progression pacing must patch the
mod or ship a full datapack override. The previous PR exposed
`count` / `y_min` / `y_max` / `enabled` for the three rarer ChronoDawn
ores but deferred `ore_clockstone` because its bundled `count` differed
between MC versions (1.20.1 = `8`, 1.21.1+ = `16`), which it framed as
requiring version-aware defaults.

Investigation of git history (see "Background: bundled count drift"
below) shows the difference is **unintentional drift, not a balance
decision**: a 2026-01 frequency reduction (`90413a2e`: "reduce
Clockstone ore frequency to align closer to vanilla iron ore levels")
was applied only to the 1.20.1 placed_feature after a same-week
multi-version split (`065b3014`). The 1.21.1+ file was missed and
silently retained the pre-fix `count = 16`. Restoring the intended
`count = 8` for 1.21.1+ removes the need for version-aware defaults
entirely, leaving the slice as a straightforward extension of the
previous ore tuning PR.

## Background: bundled count drift

Timeline:

| Commit | Date | Change |
| --- | --- | --- |
| `0e98768b` | initial | Clockstone ore added with `count = 16` ("iron-level abundance"). |
| `065b3014` | 2026-01-02 | Multi-version split copies the resource into both `1.20.1/` and `1.21.1/` directories — both still `count = 16`. |
| `90413a2e` | 2026-01-03 | "Reduce Clockstone ore generation frequency to align closer to vanilla iron ore levels". Diff touches only the 1.20.1 file (`count: 16 → 8`); the 1.21.1 file is silently skipped. |
| `54ddfe96` | 2026-03-01 | The 1.21.1+ file is consolidated into `common/shared-1.21.1+/`, freezing the stale `count = 16` across every later 1.21.x version. |

The 1.21.1+ `count = 16` is the value the 2026-01-03 fix intended to
overwrite. Unifying both bundled JSONs at `count = 8` restores the
documented design intent.

## Goals

1. Expose `enabled`, `count`, `y_min`, `y_max` for `ore_clockstone` via
   `config/chronodawn.toml`, following the exact same TOML / record /
   loader / overlay pattern established by the previous PR.
2. Correct the 1.21.1+ bundled `placed_feature/ore_clockstone.json`
   from `count = 16` to `count = 8` so all eleven supported MC
   versions ship the intended balance and the round-trip overlay test
   stays green from a single shared `ConfigDefaults` constant.
3. Keep the implementation purely additive against the existing
   `OresConfig` record — no new compat class, no version-aware
   defaults, no Gradle token replacement, no new resource paths.

## Non-goals

- `ore_iron` / `gold` / `coal` / `redstone` (vanilla-block overlay ores
  in the Chrono dimension). Same rationale as the previous PR:
  balance scaffolding rather than pack-author-facing knobs; will be
  reconsidered if a request surfaces.
- Per-biome ore adjustments.
- User-controlled height distribution type. Clockstone keeps its
  bundled `minecraft:trapezoid`.
- `configured_feature` `size` (cluster size) — stays at `9`.
- Hot reload / retroactive chunk updates.
- Re-balancing other ores' counts.

## TOML schema (addition)

Appended to `chronodawn-default-config.toml` immediately after the
existing `[world.ores.temporal_amber]` section. Defaults match the
corrected bundled placed_feature JSON exactly.

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

## Architecture

### Configuration types

`OresConfig` (existing record) gains a fourth field:

```java
public record OresConfig(
    OreSettings timeCrystal,
    OreSettings entropyCrystal,
    OreSettings temporalAmber,
    OreSettings clockstone
) {}
```

`ConfigDefaults` adds one constant mirroring the (corrected) bundled
JSON byte for byte:

```java
public static final OreSettings CLOCKSTONE_DEFAULTS = new OreSettings(true, 8, -16, 80);
```

`WorldConfig.ores()` continues to expose `OresConfig`; call sites read
`config.world().ores().clockstone().count()`.

### Overlay generation flow

`RuntimePlacedFeatureOverlay` adds:

- `CLOCKSTONE_PATH = "data/chronodawn/worldgen/placed_feature/ore_clockstone.json"`
- A new entry in `generate()` that emits the standard ore shape with
  `heightDistributionType = "minecraft:trapezoid"`.

No new helpers — the existing `generateOre(featureId, distributionType, settings)`
method already covers the shape (count + in_square + height_range +
biome). Disabled state representation (`count = 0`, other fields
preserved) follows the existing convention.

### Bundled JSON correction

`common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_clockstone.json`
is edited inline: `"count": 16` → `"count": 8`. No other field
changes. The 1.20.1 file (`common/1.20.1/.../ore_clockstone.json`) is
already at `count = 8` and stays untouched.

The single shared `CLOCKSTONE_DEFAULTS` constant now matches every
bundled JSON for every supported version.

### Validation rules

Per ore, applied inside `ConfigLoader` exactly like the existing three
ores:

- `enabled` must be boolean. Otherwise → revert that field to default,
  log at `ERROR`.
- `count` must satisfy `0 <= count <= 64`. Otherwise → revert.
- `y_min` and `y_max` must satisfy `-64 <= y_min <= y_max <= 320`.
  Otherwise → revert both to defaults for clockstone.
- Field-level revert: a bad `count` leaves `enabled` / `y_min` / `y_max`
  alone (and vice versa). Other ores are unaffected.

Unknown keys (e.g. `[world.ores.something_else]`) continue to be
warned and ignored.

### Cross-version coverage

After the bundled JSON correction, the placed_feature is identical
across all eleven supported versions. `CLOCKSTONE_DEFAULTS` therefore
stays version-free. `RuntimePlacedFeatureOverlay` lives in
`common/shared/` and is shared by every version's common module. No
loader-interface changes.

### Schema versioning

`schema_version = 1` is preserved. The new `[world.ores.clockstone]`
section is additive; old configs missing it load with the four-field
default that matches the (corrected) bundled JSON exactly.

## Files

### Modified

- `common/shared/src/main/java/com/chronodawn/config/OresConfig.java`
  — add `clockstone` field.
- `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`
  — add `CLOCKSTONE_DEFAULTS` + wire into `defaults()`.
- `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
  — add `K_CLOCKSTONE` constant and a fourth `parseOre` call inside
  `parseOres()`. Other parser code is unchanged.
- `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java`
  — add `CLOCKSTONE_PATH` and the corresponding `generate()` entry.
- `common/shared/src/main/resources/chronodawn-default-config.toml`
  — append the `[world.ores.clockstone]` section above.
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_clockstone.json`
  — `count: 16 → 8` (one-line correction restoring the
  `90413a2e` design intent across the 1.21.1+ range).
- `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`
  — add clockstone assertions (round-trip against bundled JSON,
  count/y override, disabled, distribution type).
- `common/shared/src/test/java/com/chronodawn/config/ConfigLoaderTest.java`
  — add clockstone validation cases (invalid count revert,
  inverted y range revert, unknown-key tolerance).
- `docs/configuration.md` — extend the "Ore generation" section to
  cover clockstone (one more table row plus the standard reminder
  that changes require a restart and do not retro-fit existing
  chunks).
- `docs/modpack-integration.md` — one short snippet showing
  clockstone rebalance (optional, kept short like the previous PR).
- `CHANGELOG.md` — under `[Unreleased]`:
  - `### Added`: `Ore generation tuning for Clockstone Ore via config`.
  - `### Fixed`: `ore_clockstone bundled count on 1.21.1+ corrected from 16 to 8 to match the 2026-01 frequency reduction (design intent restored across all supported MC versions)`.
- `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`
  — update the sub-project A status note (third slice shipped),
  remove `ore_clockstone` from the deferred list under "Planned
  follow-up tunables", leave the vanilla-overlay ores entry intact.

### Untouched (deliberate)

- `common/1.20.1/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_clockstone.json`
  — already at `count = 8`; matches new shared default.
- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_clockstone.json`
  — cluster `size` and target predicates stay at `9` /
  `temporal_stone` + `deepslate_temporal_stone`.
- `ore_iron`, `ore_gold`, `ore_coal`, `ore_redstone` placed_features
  — out of scope.

### New

**None.** The unification at `count = 8` removes the need for a
per-version compat class.

## Verification plan

### Unit tests

`RuntimePlacedFeatureOverlayTest` additions:

- Default config → emitted clockstone JSON parses to the same tree as
  the bundled JSON (now `count = 8` everywhere). Compared as parsed
  JSON, not raw bytes.
- `clockstone.count = 20` → emitted JSON's `count` step is `20`.
- `clockstone.enabled = false` → emitted JSON's `count` step is `0`;
  other placement steps and `height_range` values reflect the user's
  inputs verbatim.
- `clockstone.y_min = -32, y_max = 60` → emitted
  `height_range.min_inclusive.absolute = -32`,
  `max_inclusive.absolute = 60`; `height.type` is still
  `minecraft:trapezoid`.
- Distribution type for clockstone asserted directly
  (`minecraft:trapezoid`).

`ConfigLoaderTest` additions:

- `[world.ores.clockstone] count = 999` → reverts to default `8`,
  `ERROR` logged.
- `[world.ores.clockstone] y_min = 100, y_max = 50` → reverts to
  defaults `(-16, 80)` for clockstone, `ERROR` logged.
- Existing unknown-key test still passes; an extra assertion confirms
  the clockstone block is parsed independently when other ores are
  malformed.

### Manual integration

1. Build for **1.21.11** Fabric. Run client. Generate a new world,
   visit the Chrono dimension. Confirm visually that clockstone now
   generates at the lower (1.20.1-matching) density. **Note**: this
   is a balance change for 1.21.1+; existing 1.21.x worlds keep their
   already-generated clockstone-rich chunks, only newly-generated
   chunks reflect the new value.
2. Stop. Edit `clockstone.count = 20`. Restart. Generate a new
   Chrono-dim chunk and confirm visibly higher Clockstone density.
3. Stop. Edit `clockstone.enabled = false`. Restart. Generate a new
   chunk and confirm no Clockstone Ore appears.
4. Repeat steps 1–3 on **1.21.1** Fabric. (1.21.1 + 1.21.11 are the
   primary manual-verification pair established by the Ancient Ruins
   and previous ore PRs; `checkAll` covers the rest.)
5. `./gradlew checkAll` to validate all eleven versions × two loaders
   (1.20.1 Fabric-only). `gameTestAll` is skipped — this PR does not
   touch GameTest sources, matching the precedent.

## Risks

| Risk | Mitigation |
| --- | --- |
| 1.21.1+ users perceive "the mod nerfed clockstone" in new chunks | CHANGELOG entry under `### Fixed` makes clear this restores the documented 2026-01 design intent (the 1.21.1+ value was a missed update). Existing chunks are unaffected by vanilla worldgen rules. Modpack authors who prefer the old density can set `clockstone.count = 16` in their bundled config. |
| `ConfigDefaults.CLOCKSTONE_DEFAULTS` drifts from bundled JSON | Round-trip unit test asserts `generate(defaults)` parses to the same tree as bundled JSON; broken on first run. |
| User sets `y_min > y_max` | `ConfigLoader` validation reverts to defaults for clockstone, logs `ERROR`. |
| `enabled = false` representation breaks on a future MC version | `count(0)` is a stable placement modifier across the supported 1.20.1–1.21.11 range; `checkAll` plus unit tests flag a future regression. |
| Existing chunks do not reflect changes | Documented in `docs/configuration.md` "Ore generation" section (carried over from the previous PR). |

## Open questions

All resolved at design time:

- ✅ Tuning surface: absolute `count` + `y_min` + `y_max` + `enabled`
  (no multipliers, no `size`). Mirrors the previous PR exactly.
- ✅ Distribution type: frozen to `minecraft:trapezoid`, not
  user-tunable.
- ✅ Version-aware defaults: **not needed**. The 1.21.1+ bundled JSON
  is corrected to `count = 8` so a single shared default suffices.
- ✅ Scope: only `ore_clockstone`; vanilla-overlay ores remain
  deferred.
- ✅ Schema version: stays at `1` (additive change).
- ✅ Disabled representation: `count = 0` in emitted JSON; preserve
  user's `count` / `y_min` / `y_max` for round-tripping.

## Extension pattern (recap)

Same recipe as the previous PR: new field on `OresConfig`, new
constant in `ConfigDefaults`, new key in `ConfigLoader`, new entry in
`RuntimePlacedFeatureOverlay.generate()`, new TOML section, new
tests, doc updates. The bundled-JSON correction is the only piece
specific to this slice; everything else is a copy-and-rename of the
existing clockstone-shaped scaffolding for the prior three ores.

## Out of scope (explicit)

- Vanilla-overlay ores in the Chrono dimension (`ore_iron`, `gold`,
  `coal`, `redstone`)
- Per-biome ore rates
- Distribution type customization
- `configured_feature` `size`
- Hot reload / retroactive chunk updates
- Other ChronoDawn ores already shipped in the previous PR

## Done criteria

- All files in the "Files / Modified" section exist or are updated.
- Unit tests pass for all eligible versions (`./gradlew testAll`).
- Manual integration verification (steps 1–3) passes on both 1.21.1
  and 1.21.11.
- `./gradlew checkAll` succeeds.
- `docs/configuration.md` "Ore generation" section reads consistently
  with clockstone added.
- CHANGELOG entries (`Added` + `Fixed`) written.
- Roadmap document's status tracker updated to reflect that the third
  slice of sub-project A has shipped, with `ore_clockstone` removed
  from the deferred list.
