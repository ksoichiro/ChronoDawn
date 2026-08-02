# Design: Time Distortion Configuration

**Created**: 2026-08-02
**Status**: Implemented
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A

## Goal

Let modpack authors tune or disable the Chrono Dawn dimension's ambient Time
Distortion without changing the default gameplay.

## Configuration

```toml
[gameplay.time_distortion]
enabled = true
normal_slowness_level = 4
enhanced_slowness_level = 5
scope = "hostile_mobs"
```

Potion levels are player-visible values in the inclusive range `1..=5`.
`hostile_mobs` preserves the shipped behavior. `all_mobs` also
affects passive and friendly mobs, but players and Chrono Dawn bosses remain
excluded so the player experience and boss abilities retain their established
boundaries.

## Compatibility

The setting is read once during common initialization, as with the existing
configuration. It applies to the shared implementation used through 1.21.4
and the version-specific implementations for 1.21.5 through 1.21.11. The
1.20.1 to 1.21.4 effect uses the older `MOVEMENT_SLOWDOWN` constant; later
versions use `SLOWNESS`, but both consume the same zero-based amplifier.

No runtime overlay datapack is required because Time Distortion is applied by
server-side entity logic. Existing effects expire naturally; after restart,
eligible mobs receive the configured effect on their next tick.
