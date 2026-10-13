# Design: Wayside Features, Part 1 (Cooldown Foundation and Old Sundial)

**Created**: 2026-10-12
**Status**: Draft

---

## Why

Major Chrono Dawn structures sit far apart. Their `random_spread` spacing is
48 to 60 chunks for Master Clock, Ancient Ruins, Clockwork Depths, Entropy
Crypt and Guardian Vault, and biome restrictions skip some candidate cells.
A player walking toward the next structure often covers several hundred to
over a thousand blocks. A compass makes the direction obvious, so the walk
itself has nothing to discover.

Six small surface features already appear along the way (`old_sundial`,
`hourglass_monolith`, `time_well`, `time_cairn`,
`lost_adventurer_memorial`, `watchmaker_camp`). Only `watchmaker_camp` has
anything to interact with. The other five are decoration, so stopping at one
gives the player nothing.

The goal is to give each of these features a reason to stop, so that a long
walk is punctuated every few minutes by something worth a short detour.

## Overall plan

Each feature gets its own role. They are delivered as separate sub-projects,
each with its own spec, plan and implementation.

| Order | Feature | Role |
|---|---|---|
| 1 | Shared foundation + `old_sundial` | Per-player cooldown activator, travel buff |
| 2 | `lost_adventurer_memorial` | Lore fragment and supplies, with a light-beam effect |
| 3 | `time_well` | Wishing well that exchanges a thrown item for another |
| 4 | `watchmaker_camp` | Per-player chest contents that refill over time (Lootr-like) |
| 5 | `time_cairn` | Collect time-of-day "cairn stones", with a rare midnight echo |
| 6 | `hourglass_monolith` | Small wave fight with a reward |

Decisions that apply to all six:

- **Reusable, never one-shot.** Every feature can be used again after a
  cooldown. No feature becomes permanently spent.
- **Cooldown is per location and per player.** Players on a shared server
  never compete for the same feature.
- **State lives in a dedicated activator block.** The interactive part of
  each feature is a new block whose block entity stores the cooldowns. A
  block placed by a player that looks the same does nothing.
- **Already-generated features are not converted.** Only features generated
  in new chunks after the update are interactive.
- **Major structure placement is unchanged.**

This document covers order 1 only. The decisions for all six parts, the
status of each, and lessons from part 1 are kept in the
[roadmap](./2026-10-14-wayside-features-roadmap.md).

## Scope of part 1

In scope:

- A reusable per-player cooldown tracker and an abstract block entity that
  stores it
- The `sundial_gnomon` block and its block entity
- The Old Sundial buff behavior
- Config entries for the sundial
- Replacing the gnomon in `old_sundial.nbt`

Out of scope: the other five features, conversion of existing worlds, any
change to structure spacing.

## Player-facing behavior

### Appearance

The center post of the sundial (currently `minecraft:iron_bars`) becomes
`chronodawn:sundial_gnomon`. It looks like the current post.

### Use

Right-clicking the gnomon with any item or an empty hand activates it. The
server decides the outcome. The client holds no state.

### Effect by time of day

The effect depends on the dimension's time of day (`dayTime % 24000`).

| Phase | Range of `dayTime % 24000` | Effect | Presentation |
|---|---|---|---|
| Noon | 5000 to 6999 | Speed II for 3 minutes | Dense sunlight particles along the shadow direction, chime sound |
| Day | 0 to 4999 and 7000 to 12999 | Speed I for 3 minutes | Sunlight particles along the shadow direction, chime sound |
| Night | 13000 to 23999 | Night Vision for 3 minutes | Faint moonlight particles, softer chime sound |

Night still gives something, so a player who arrives at night does not walk
away empty-handed. All effects are vanilla effects. No new effect is
registered.

Chrono Dawn's day length varies (time flow speed changes), so the same
sundial can give different effects depending on when the player arrives.
This is intended.

### Cooldown

- Per player, per sundial: 24000 ticks (about 20 real minutes) by default.
- Measured in game time (`Level#getGameTime`), not time of day. Sleeping or
  time-flow speed changes do not shorten it.
- Using the gnomon during cooldown shows an action-bar message (for example
  "The shadow has not moved yet") with the approximate remaining time, and
  applies nothing.

### Farming prevention

- Breaking the gnomon drops `minecraft:iron_bars`.
- `sundial_gnomon` has no item. It cannot be obtained in survival.
- A sundial built by a player from iron bars does nothing.

## Internal structure

The layout follows existing project patterns. Blocks are per version.
Block entities live in `common/shared` for 1.20.1 to 1.21.4 and per version
for 1.21.5 and later, the same split as `ClockTowerTeleporterBlockEntity`.

### Pure logic (`common/shared`, no Minecraft dependency)

- **`PlayerCooldownTracker`**: map from player UUID to last-use game time.
  Operations: remaining ticks for a player, record use, and prune entries
  whose cooldown has expired. Every use prunes first, so the stored data
  holds at most the players still on cooldown. It serializes to a single
  string, which reads and writes the same way on every supported version.
  Parts 2 to 6 reuse this class.
- **`SundialBlessing`**: maps `dayTime` to a phase (`DAY`, `NOON`, `NIGHT`)
  and returns the effect, amplifier and duration for that phase. Duration
  comes from config.

### Block entities

- **`CooldownActivatorBlockEntity`** (abstract): owns a
  `PlayerCooldownTracker` and persists it. 1.20.1 to 1.21.4 extend
  `CompatBlockEntity` in `common/shared`. 1.21.5 and later use per-version
  copies that follow the existing value I/O API split. Parts 2 to 6 extend
  this class.
- **`SundialGnomonBlockEntity`**: extends `CooldownActivatorBlockEntity`
  with no extra state.

### Block (all 14 version modules)

- **`SundialGnomonBlock`**: handles right-click on the server side. It reads
  the cooldown, applies the blessing, spawns particles, plays the sound and
  sends the action-bar message. It uses the same interaction method
  signature as the existing `ClockTowerTeleporterBlock` in each version.
- Time of day is read the same way `TimeDistortionEventHandler` reads it in
  each version (`Level#getDayTime` before 26.x, the world clock in 26.x).

### Registration

- Add `SUNDIAL_GNOMON` to `ModBlockId` and `ModBlockEntityId`.
- Register the block and block entity in every version's `ModBlocks` and
  `ModBlockEntities`.
- No `BlockItem`.
- Cutout render layer on Fabric, Forge and NeoForge, matching iron bars.

### Resources

- Blockstate and model. The model uses vanilla `block/iron_bars_post` as its
  parent so the post looks unchanged.
- Loot table dropping `minecraft:iron_bars` (`loot_tables` directory on
  1.20.1, `loot_table` on 1.21.1 and later).
- `minecraft:mineable/pickaxe` tag entry.
- Translation keys for the block name and the action-bar messages in
  `en_us` and `ja_jp` for every lang file set, including the standalone
  1.20.1 and 1.21.1 files.
- `common/shared-1.21.1+/.../structure/old_sundial.nbt`: replace the
  `minecraft:iron_bars` palette entry with `chronodawn:sundial_gnomon`,
  edited directly with nbtlib. The 1.20.1 copy is generated by the existing
  `convertNbt` task.

### Config

A new `[gameplay.wayside]` section in the Chrono Dawn config:

| Key | Default | Range | Meaning |
|---|---|---|---|
| `sundial_cooldown_ticks` | 24000 | 20 to 1728000 | Per-player cooldown for one sundial |
| `sundial_effect_duration_ticks` | 3600 | 20 to 72000 | Duration of the granted effect |

It follows the existing record-based settings pattern (`TimeFlowSettings`
and others) and `ConfigDefaults` / `ConfigLoader`.

## Error handling

- A block entity that fails to load its cooldown data starts with an empty
  tracker. The worst case is that a player can use the sundial early once.
- Unknown or malformed UUID entries are skipped on load.
- Config values outside their range fall back to the defaults, each key
  independently.
- Client-side clicks return success without doing anything, so the hand
  swings without a duplicated server action.

## Testing

- **Unit tests** (`common/shared/src/test`):
  - `PlayerCooldownTracker`: ready on first use, blocked within cooldown,
    ready again after cooldown, independence between players, pruning of
    expired entries only.
  - `SundialBlessing`: phase boundaries at 0, 4999, 5000, 6999, 7000,
    12999, 13000, 23999, and wrap-around for `dayTime` above 24000.
- **GameTest**:
  - Place `sundial_gnomon` and assert its block entity is a
    `SundialGnomonBlockEntity`. Activate it for one player UUID, assert a
    second activation is refused, and assert a different UUID is accepted.
    Effect application needs a real player and is covered by the manual
    check.
  - Update `StructureTests` so `old_sundial` expects one `sundial_gnomon`
    instead of one `iron_bars`.
- **Validation**: `validateResources`, `validateData`,
  `validateTranslations`.
- **Manual check**: on 1.21.1 NeoForge and Fabric, find a newly generated
  sundial, use it in day and at night, and confirm the cooldown message.
  1.21.1 is the modpack regression target.

## Open questions for later parts

- Whether cooldown durations for parts 2 to 6 share one config section or
  each feature gets its own keys. Part 1 uses `wayside` so later parts can
  add keys there.
