# Design: Time Distortion Rebalance

**Created**: 2026-10-06
**Status**: Draft

---

## Why

Time Distortion gives every hostile mob in the Chrono Dawn dimension
Slowness IV (60% slower), and Slowness V after the Eye of Chronos enhancement.
The effect is constant, applies to all hostile mobs equally, and sits near the
top of the Slowness scale. In play, most mobs end up trivial. Clockwork
Sentinel (immune) and Paradox Crawler (leap attack) are the only mobs that
still feel threatening.

The goal is to keep the dimension's identity, mobs slowed by distorted time,
while making encounters vary over time and between mob types. The change must
not alter existing worlds without the user opting in.

Out of scope, deferred:

- Mob-specific counter abilities against slowness, such as short time-skip
  dashes.
- Reversing the direction of the Eye of Chronos enhancement.

## Summary

1. Lower the default normal Slowness level from IV to III, and the enhanced
   level from V to IV, for new installs only.
2. Link the effect strength to the dimension's existing variable time-flow
   speed. Slow time makes mobs slower. Very fast time gives mobs Speed I
   instead, which creates rare dangerous periods.
3. Give a diegetic cue when the dangerous period starts: a distant bell sound,
   visible Speed particles on affected mobs, and a Chronicle explanation.
4. Add per-mob resistance tiers through entity type tags: immune, resistant,
   and normal.
5. Fix 1.21.5+ ignoring the `time_distortion_immune` tag.

## 1. Bands

`TimeDistortionEventHandler` already picks a random day/night speed multiplier
in `[min_speed, max_speed]` (defaults 0.67 to 5.0, uniform) and holds it for
1200 to 6000 ticks. The effect level is chosen from the band the current
multiplier falls in.

| Band | Multiplier (default bounds) | Share of time (defaults) | Effect |
|---|---|---|---|
| Slow | `< slow_below` (1.0) | 8% | Slowness, base + 1 |
| Normal | `slow_below` to `fast_above` (1.0 to 3.0) | 46% | Slowness, base |
| Fast | `fast_above` to `very_fast_above` (3.0 to 4.5) | 35% | Slowness, base - 1 |
| Very fast | `>= very_fast_above` (4.5) | 12% | Speed, `very_fast_speed_level` |

Boundary rule: a value equal to a bound belongs to the higher band.

"Base" is `normal_slowness_level` before the Eye of Chronos enhancement and
`enhanced_slowness_level` after it. With the new defaults (3 and 4):

| Band | Before enhancement | After enhancement |
|---|---|---|
| Slow | Slowness IV | Slowness V |
| Normal | Slowness III | Slowness IV |
| Fast | Slowness II | Slowness III |
| Very fast | Speed I | Speed I |

The Very fast band keeps Speed I after the enhancement, so a dangerous period
still exists after the main progression is finished.

Slowness levels are clamped to at most 5. A computed level below 1 means no
Slowness.

The Normal band is used whenever the multiplier is not meaningful:

- `link_to_time_flow = false`
- `[gameplay.time_flow] enabled = false` (time always runs at 1.0x)
- before the first speed is chosen after a server start (the handler starts at
  1.0x)

## 2. Resistance tiers

| Tier | Tag | Slowness | Speed |
|---|---|---|---|
| Immune | `chronodawn:time_distortion_immune` (existing) | none | none |
| Resistant | `chronodawn:time_distortion_resistant` (new) | level - 2 | unchanged |
| Normal | neither | as computed | as computed |

If an entity is in both tags, Immune wins.

Default tag contents:

- Immune: the six bosses (unchanged) plus `chronodawn:clockwork_sentinel`.
- Resistant: `chronodawn:temporal_wraith`, `chronodawn:timeline_strider`,
  `chronodawn:forgotten_minute`.
- Normal: Epoch Husk, Moment Creeper, Floq, Secondhand Archer, Chronal Leech,
  Hourglass Golem, Paradox Crawler.

With the defaults in the Normal band (Slowness III), seven mob types are
clearly slow, three are slightly slow (Slowness I), and Clockwork Sentinel
moves at full speed.

### Clockwork Sentinel override

`ClockworkSentinelEntity.canBeAffected` rejects every Slowness effect, from
any source. If it stayed as is, removing Sentinel from the immune tag through a
datapack would have no effect.

The override changes to reject Slowness only while the entity's type is in
`time_distortion_immune`. With the default tag, behavior is unchanged. Removing
Sentinel from the tag makes it receive both ambient and other Slowness.

This means the tag has a wider meaning for Sentinel than for bosses. Bosses
in the tag skip only the ambient effect, and still receive Slowness from other
sources. Document this on the tag constant and on the override.

## 3. Applying and replacing the effect

The ambient effect is reapplied every tick with a 100-tick duration. Two
vanilla behaviors get in the way of band changes:

- `addEffect` with a lower amplifier does not replace a stronger active
  instance. A drop from Slowness IV to III would lag by up to 5 seconds.
- Slowness and Speed are separate effects, so switching bands would leave the
  old one active for up to 5 seconds.

Mark ambient instances with `ambient = true`. Vanilla applies ambient effects
to mobs from almost nothing else. On each application:

1. Remove an active Slowness or Speed instance only if it is ambient, its
   remaining duration is at most the ambient duration (100 ticks), and it
   differs from the desired effect in type or amplifier.
2. Add the desired instance, if any.

Non-ambient effects stay untouched. Examples are Resonance Reversal's Speed II
on nearby mobs, Temporal Wraith hits, and potions.

Particles: Slowness stays invisible (`visible = false`), as today. Speed is
visible (`visible = true`). Ambient particles render translucent, which suits
a subtle cue.

## 4. Dangerous-period cue

- When the time-flow handler picks a new multiplier and the band changes from
  any band to Very fast, play `minecraft:block.bell.resonate` once to every
  player in the Chrono Dawn dimension. Play it at the player's position, in the
  ambient sound category, at low volume and slightly lowered pitch. No sound
  plays when leaving the band.
- No sound plays when `link_to_time_flow`, `[gameplay.time_flow] enabled`, or
  the Very fast band (`very_fast_speed_level = 0`) is disabled.
- Update the Chronicle entry
  `assets/chronodawn/chronicle/entries/progression/time_distortion.json`
  (en_us and ja_jp). Describe that slowness strength follows the speed of time,
  and that a distant bell, a racing sky, and swirling mobs signal that enemies
  have become fast. The current text says "Slowness IV" and "Passive animals
  are not affected", so rewrite it so that it does not depend on specific
  levels or scope.

No new sound assets and no new language keys are needed. The Chronicle file
holds both languages and is shared by all versions.

## 5. Configuration

`[gameplay.time_distortion]` keeps its existing keys and adds new ones.

| Key | Type | New default | Range | Notes |
|---|---|---|---|---|
| `enabled` | bool | `true` | | unchanged |
| `normal_slowness_level` | int | `3` (was `4`) | `1..=5` | now the Normal band level before enhancement |
| `enhanced_slowness_level` | int | `4` (was `5`) | `1..=5` | now the Normal band level after enhancement |
| `scope` | string | `"hostile_mobs"` | | unchanged. `all_mobs` also gives passive mobs Speed in the Very fast band |
| `link_to_time_flow` | bool | `true` | | new |
| `slow_below` | float | `1.0` | `0.05..=20.0` | new |
| `fast_above` | float | `3.0` | `0.05..=20.0` | new |
| `very_fast_above` | float | `4.5` | `0.05..=20.0` | new |
| `very_fast_speed_level` | int | `1` | `0..=2` | new, `0` disables the Very fast band and its cue |

Validation follows the existing ConfigLoader pattern: an out-of-range value
logs a warning and falls back to its default. If the three bounds are not
strictly increasing (`slow_below < fast_above < very_fast_above`), log one
warning and use the default bounds for all three. When `very_fast_speed_level`
is `0`, the Very fast band is treated as the Fast band.

### Existing installs

ConfigLoader writes the bundled default file on first launch and never
rewrites it. Existing installs therefore keep `normal_slowness_level = 4` and
`enhanced_slowness_level = 5`. Under the new rules, they get:

| Band | Before enhancement | After enhancement |
|---|---|---|
| Slow | Slowness V | Slowness V |
| Normal | Slowness IV | Slowness V |
| Fast | Slowness III | Slowness IV |
| Very fast | Speed I | Speed I |

This is intentional. Updating the mod must not silently change an existing
world's balance. There is no migration. CHANGELOG and `docs/configuration.md`
tell users to set the two levels to 3 and 4 to adopt the new balance.

## 6. Code structure

- **New, `common/shared`: `TimeDistortionPolicy`** (pure Java, no Minecraft
  types). Input: time multiplier, enhanced flag, tier (`IMMUNE`, `RESISTANT`,
  `NORMAL`), and `TimeDistortionSettings`. Output: a value representing none,
  Slowness at level n, or Speed at level n. Also exposes the band for a
  multiplier, which the cue uses.
- **`TimeDistortionSettings`**: add the new fields, keep `slownessAmplifier`
  only if callers still need it.
- **`ConfigDefaults`, `ConfigLoader`, `chronodawn-default-config.toml`**: new
  keys, defaults, and validation.
- **`TimeDistortionEffect`** (shared copy and the eleven 1.21.5+ copies): get
  the multiplier from `TimeDistortionEventHandler.getCurrentSpeed`, the tier
  from the tags, ask the policy, then apply as in section 3. The 1.21.5+ copies
  replace their `instanceof` boss chain with the tag check. The copies keep
  differing only in version-specific API names (`SLOWNESS` / `SPEED`,
  `identifier()`).
- **`TimeDistortionEventHandler`** (shared copy and the 26.1.2, 26.2, and 26.3
  copies, which differ for the world clock API): on speed change, compare old
  and new bands through the policy and play the cue on entering Very fast.
- **`ModEntityTypeTags`**: add `TIME_DISTORTION_RESISTANT`.
- **Tag files**: `time_distortion_resistant.json` and the Sentinel entry, in
  `common/1.20.1/.../tags/entity_types/` and
  `common/shared/.../tags/entity_type/`.
- **`ClockworkSentinelEntity`** (all 14 version modules, 1.20.1 through 26.3):
  tag-aware override from section 2.

## 7. Testing

- Unit tests for `TimeDistortionPolicy`:
  - every band, before and after enhancement, with the defaults
  - each band boundary value
  - clamping at V and dropping to none below I
  - each resistance tier, including Resistant in the Very fast band
  - the Normal band when linking is disabled
  - `very_fast_speed_level = 0`
- ConfigLoader unit tests: new keys, range fallback, and non-increasing bounds
  fallback.
- `ConfigLoaderTest.timeDistortion_missingSection_fallsBackToDefaults`
  currently asserts amplifiers 3 and 4 (levels IV and V). It must change to 2
  and 3, because the defaults change intentionally in this spec. This is a
  specification change, not a test adjusted to pass.
- Build all versions. Run `testAll`.
- Runtime check on 1.21.1 (NeoForge and Fabric, per Support Priority) and the
  default target 26.3:
  - With a fresh config, mobs in the Normal band have Slowness III, and the
    resistant and immune mobs behave per their tier.
  - Force a speed change to observe band switching without the 5-second lag,
    the bell on entering Very fast, and visible Speed particles. This needs a
    debug or command path to set the speed, or temporarily narrowed
    `min_speed` and `max_speed` in the config.
  - An existing config with 4 and 5 keeps the stronger table in section 5.
  - Removing `clockwork_sentinel` from the immune tag through a datapack makes
    it slowed, on a 1.21.5+ version.

## 8. Documentation

- `docs/configuration.md`: new keys, band table, existing-install note.
- `docs/player_guide.md`: replace the fixed "Slowness IV" descriptions, around
  lines 39, 194, 1089, and 1225.
- `docs/curseforge_description.md`, `docs/modrinth_description.md`, `README.md`:
  check for fixed-level wording.
- `CHANGELOG.md` (Unreleased): the rebalance, the new keys, how to adopt the
  new defaults on an existing install, and the 1.21.5+ immune tag fix.
- Chronicle entry, per section 4.
