# Design: Per-Structure Generation Configuration

**Created**: 2026-08-22
**Status**: Approved — ready for implementation planning
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A

## Goal

Let modpack authors disable or retune every Chrono Dawn structure, not just
Ancient Ruins. This is the "dimension-level toggles" follow-up tunable in
sub-project A, narrowed to structures; per-biome toggles are a separate slice.

## Scope

The eight structure sets the mod ships:

| Structure | Dimension | Progression role |
| --- | --- | --- |
| `ancient_ruins` | Overworld | none — flavour only |
| `forgotten_library` | Chrono Dawn | Portal Stabilizer recipe |
| `desert_clock_tower` | Chrono Dawn | Time Guardian → Master Clock Key, Enhanced Clockstone |
| `guardian_vault` | Chrono Dawn | Chronos Warden → Guardian Stone |
| `clockwork_depths` | Chrono Dawn | Clockwork Colossus → Colossus Gear |
| `phantom_catacombs` | Chrono Dawn | Temporal Phantom → Phantom Essence |
| `entropy_crypt` | Chrono Dawn | Entropy Keeper → Entropy Core |
| `master_clock` | Chrono Dawn | Time Tyrant, the final boss |

`ancient_ruins` is already configurable; it is folded into the new shared shape
rather than left as a special case.

Out of scope: per-biome enable flags, and the `structure/*.json` definitions
(room contents, jigsaw pools). Only `structure_set` placement is configurable.

## Configuration

Each structure gets the four fields Ancient Ruins already exposes, under a flat
key matching its name:

```toml
[world.structures.master_clock]
enabled = true
spacing = 60
separation = 20
salt = 1234567890
```

Defaults equal the values in the shipped `structure_set` JSON, so an untouched
config reproduces today's generation exactly.

## Data model

`ChronoDawnConfig.AncientRuins` generalises into a shared record, mirroring how
`BossesConfig` holds six `BossSettings`:

```java
public record StructureSettings(boolean enabled, int spacing, int separation, int salt) {}

public record Structures(
    StructureSettings ancientRuins,
    StructureSettings forgottenLibrary,
    StructureSettings desertClockTower,
    StructureSettings guardianVault,
    StructureSettings clockworkDepths,
    StructureSettings phantomCatacombs,
    StructureSettings entropyCrypt,
    StructureSettings masterClock
) {}
```

A `ManagedStructure` enum is the single source of truth for the set of eight.
Each constant carries:

- the TOML key (`master_clock`)
- the structure ID (`chronodawn:master_clock`)
- the pack-relative `structure_set` path
- an accessor from `Structures` to that constant's `StructureSettings`
- its default `StructureSettings`
- a progression note, or empty for `ancient_ruins`

`ConfigLoader`, `RuntimeStructureOverlay` and the guard tests all iterate this
enum. A structure that is not registered there appears in none of them, and one
that is registered appears in all three — so the "added a structure, forgot to
wire it somewhere" failure mode cannot happen silently.

## Generation

`RuntimeStructureOverlay.generate()` iterates `ManagedStructure` and emits one
`structure_set` JSON per constant, in today's Ancient Ruins format. A disabled
structure keeps its `placement` block but emits an empty `structures` array, so
systems referencing the ID still resolve it while nothing generates.

All eight `structure_set` JSON files live in `common/shared`, so no per-version
branching is needed. (The `structure/*.json` definitions are version-split, but
this slice does not touch them.)

## Validation

Reuses the existing Ancient Ruins rules and log format, per key:

- `spacing` must be in `1..=4096`
- `separation` must be in `0..spacing`
- out-of-range values revert that key alone to its default and log at `ERROR`

Per-key fallback semantics are unchanged: one bad value never discards a whole
section, nor another structure's settings.

## Progression warnings

Disabling any structure except Ancient Ruins breaks the main progression chain.
Rather than refuse the setting — pack authors routinely substitute their own
sources — the mod states the consequence. After the config is read,
`ConfigLoader` logs one `WARN` per disabled structure that carries a
progression note:

```
world.structures.master_clock is disabled: Time Tyrant (final boss) and its
drops become unobtainable unless your pack provides another source.
```

The message list is produced by a pure function from `Structures`, so it is unit
testable without capturing log output.

## Existing worlds

Placement changes do not apply to already-generated chunks, matching every
other worldgen tunable. Documented with the same "new worlds only" note.

## Testing

1. **Defaults match the shipped JSON.** Iterate `ManagedStructure` and assert
   each constant's default `spacing` / `separation` / `salt` equals the value in
   its static `structure_set` JSON. Guards the duplication between Java defaults
   and resources — 24 values that would otherwise drift unnoticed.
2. **Coverage.** The set of files in `structure_set/` and the set of
   `ManagedStructure` constants must be equal, in both directions.
3. **`ConfigLoader`.** Missing section yields defaults for all eight; custom
   values are returned verbatim; an out-of-range `spacing` or a `separation`
   at or above `spacing` reverts only that key, leaving sibling keys and other
   structures untouched.
4. **`RuntimeStructureOverlay`.** `generate()` emits all eight paths; a disabled
   structure yields an empty `structures` array; an enabled one contains its
   structure ID. Existing Ancient Ruins assertions stay.
5. **Progression warnings.** The pure message-building function returns one
   entry per disabled progression-critical structure, and none for
   `ancient_ruins`.

No GameTest: the config is read at startup and GameTests run with defaults, so
a runtime test would assert nothing the unit tests do not already cover.
Verification is the usual `checkAll`.

## Documentation

`docs/configuration.md` gains a `[world.structures.*]` section replacing the
Ancient-Ruins-only one, with a table of the eight structures, their defaults,
and their progression roles. `docs/modpack-integration.md` gains an example of
disabling a dungeon and the caveat about progression.
