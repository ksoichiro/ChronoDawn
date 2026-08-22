# Design: Portal Behavior Configuration

**Created**: 2026-08-04
**Status**: Implemented
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A

## Goal

Let modpack authors alter Chrono Dawn's portal progression without changing the
default one-way entry and Portal Stabilizer milestone.

## Configuration

```toml
[gameplay.portals]
one_way_until_stabilized = true
allow_reignition_before_stabilization = false
```

- `one_way_until_stabilized` preserves the initial one-way entry when `true`:
  entry deactivates the departure portal and destroys the arrival portal until
  the dimension is stabilized. When `false`, neither action happens, so the
  initial portal pair supports immediate return travel.
- `allow_reignition_before_stabilization` permits the Time Hourglass to ignite
  a Chrono Dawn portal while the dimension is still unstable when the one-way
  setting remains enabled. This lets packs retain the initial gate while
  providing a deliberately crafted return portal. Disabling the one-way
  setting also permits re-ignition, so it fully removes the progression gate.
  Enabling re-ignition also suspends the unstable-portal block sweep in the
  Chrono Dawn dimension; otherwise the sweep would remove the crafted portal
  on the tick after it is lit, making the setting a no-op.

## Compatibility

Settings are loaded once at common initialization, follow the existing
per-key fallback behavior, and have no client synchronization requirement.
They are read by every supported version's server-side portal implementation.

Changing either setting affects future portal use after a server restart; it
does not restore portal blocks destroyed by an earlier one-way arrival.

Suspending the sweep is coarse: it is disabled for the whole session rather
than only for player-lit portals, because no per-portal "deliberately lit"
flag exists. The remaining position-local cleanup paths (arrival
extinguishing and unstable-arrival destruction) stay active, and the only
producers of portal blocks are still gated, so an unreaped portal is at worst
a leftover the player built on purpose.
