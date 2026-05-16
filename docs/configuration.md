# Configuration

Chrono Dawn reads its runtime configuration from a single TOML file at
`config/chronodawn.toml` in your Minecraft instance (or server) directory.

The file is created automatically with documented defaults the first time the
mod loads. To change a value, stop the game (or server), edit the file, and
start it again.

> [!IMPORTANT]
> **Changes require a server (or single-player world) restart to take effect.**
> Running `/reload` does **not** re-read this file — vanilla limitations.

> [!IMPORTANT]
> **Existing chunks are not retroactively updated.** Worldgen-related options
> (e.g. structure spacing) only affect chunks generated *after* the change.

---

## File location

| Environment | Path |
| --- | --- |
| Single-player (Fabric / NeoForge) | `<minecraft instance>/config/chronodawn.toml` |
| Dedicated server | `<server root>/config/chronodawn.toml` |
| Modpack distribution | Bundle the file under `config/` in the pack's `overrides/` |

The mod also writes `<configDir>/chronodawn-runtime-overlay/`, an internal
data pack regenerated on every startup from the values in the TOML. It is
safe to delete; it will be recreated on next launch. Do not edit it by hand —
your edits will be overwritten.

---

## Behavior on invalid input

| Situation | What happens |
| --- | --- |
| File missing | The bundled commented default file is written to disk. |
| Field missing | That field uses its default; the rest of the config still loads. |
| Field has the wrong type or out-of-range value | That field reverts to its default and an `ERROR` is logged. The rest of the config still loads. |
| Unknown key | Logged at `WARN` and ignored. |
| Malformed TOML | Logged at `ERROR`. The config loader falls back to all defaults. |
| `schema_version` newer than this build understands | Logged at `WARN`. Recognized fields still load. |

---

## Schema reference

### `schema_version`

```toml
schema_version = 1
```

Versioning marker for the file format. Currently `1`. Reserved for future
field renames or removals; you should not need to change it manually.

---

### `[world.structures.ancient_ruins]`

Generation parameters for the Overworld **Ancient Ruins** structure.

```toml
[world.structures.ancient_ruins]
# Whether Ancient Ruins generate at all in the Overworld.
enabled = true

# Average distance (in chunks) between Ancient Ruins placement attempts.
# Lower = denser, higher = rarer. Must be > separation. Vanilla limit: 4096.
spacing = 56

# Minimum guaranteed distance (in chunks) between two Ancient Ruins.
# Must be < spacing.
separation = 20

# Random seed offset for placement; usually no reason to change this.
salt = 20005897
```

| Field | Type | Default | Range | Notes |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | `true` | — | When `false`, the structure set is registered but contains no variants, so nothing generates. |
| `spacing` | integer (chunks) | `56` | `1..=4096` | Average distance between placements. |
| `separation` | integer (chunks) | `20` | `0..spacing` | Minimum distance between any two placements. |
| `salt` | integer | `20005897` | any 32-bit int | Random seed offset. Different salts produce different placement patterns even with the same world seed. |

#### Example: denser ruins

```toml
[world.structures.ancient_ruins]
spacing = 24
separation = 8
```

Roughly the early-development density. New worlds only.

#### Example: disable Ancient Ruins entirely

```toml
[world.structures.ancient_ruins]
enabled = false
```

The placement is still registered (so other systems referencing the ID still
find it) but no structure variants are emitted, so nothing generates. New
worlds only.

---

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

---

## Adding more configuration

This file is the canonical reference. Future Chrono Dawn versions will add
sections for boss tuning, ore generation, and more — every new tunable will
appear in this document with its default and validation rules.

For modpack creators who want to bundle a `chronodawn.toml` with their pack,
see [`docs/modpack-integration.md`](modpack-integration.md).
