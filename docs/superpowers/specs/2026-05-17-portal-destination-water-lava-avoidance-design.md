# Design: ChronoDawn Portal Destination — Water/Lava Avoidance & Step-out Footing

**Date**: 2026-05-17
**Status**: Approved scope, pending implementation
**Branch (planned)**: `portal-destination-water-lava-avoidance`

---

## Problem

When a player enters a ChronoDawn portal, `PortalTeleportHandler.generatePortal()`
creates the destination portal at the destination X/Z coordinates. The current
implementation in `findGroundLevel()` decides where to place the portal by
checking that the candidate position has 6 blocks of "air or replaceable"
above. **Water blocks return `canBeReplaced() = true`**, so the check happily
passes through water columns. Combined with a downward search for solid ground
that bottoms out at the seabed, the result is that the destination portal
frequently materialises **submerged on the ocean / lake floor**, with the
portal interior filled with water. The same logic would also place portals
inside lava pools (lava is also a non-air `canBeReplaced` fluid in some
configurations and is non-solid in all of them).

Additionally, the generated structure is only the 4x5 Clockstone frame and
the 2x3 portal blocks. There is no guarantee that the bottom row of the
frame rests on a walkable block — if the chosen position has water or lava
directly under the frame, the player drops/drowns the moment they step out.
Vanilla Nether portals avoid this by always laying an obsidian platform under
the frame and never placing in lava.

User-reported symptom (2026-05-17): "Sometimes the portal is generated in
water. Vanilla Nether portals never appear in lava and always come with a
block to step out onto."

## Goals

1. Never place the destination portal so that its **interior** (the 2x3 portal
   blocks at frame rows 1–3) is inside a fluid (water, lava, or any block
   whose `BlockState.getFluidState()` is non-empty). The bottom-frame row
   itself is allowed to occupy a fluid position — it is replaced by Clockstone
   when written, and players don't stand inside the frame ring anyway.
2. Match a player's typical hand-built water portal: when the natural surface
   at the destination X/Z is water (or lava), the destination frame's bottom
   row should sit AT the fluid surface (replacing the top fluid block), not
   one block above it. The interior 3-block column stays above the fluid
   surface, so the player teleports onto solid frame Clockstone with no
   submersion or drowning.
3. Do NOT generate any extra Clockstone footing beneath the frame. The 4x5
   frame on its own is the canonical ignitable shape; an extra row of
   Clockstone below the bottom-frame row creates a non-vanilla shape that
   the frame validator can reject (observed: re-ignition fails on such
   shapes). Sidestepping the validator quirk by not creating the shape is
   simpler than relaxing the validator.
4. Apply the fix to all 11 supported Minecraft versions (1.20.1, 1.21.1,
   1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10,
   1.21.11). 1.21.3 reuses 1.21.2 modules and inherits the fix automatically.

## Non-goals

- Step-out platform extending in front of the portal (the answered scope
  is "frame-bottom 4 blocks only"; bidirectional / multi-block aprons are
  out).
- Changing the destination X/Z mapping (still 1:1 with source frame
  bottom-left).
- Changing the heightmap type used for the initial surface probe
  (`WORLD_SURFACE` is kept; the fix is applied after the probe).
- Existing portals already placed underwater in saved worlds — this design
  only affects newly generated destinations. Re-generation of bad existing
  portals is out of scope.
- Reused / reignited frames (`findReusablePortalFrame`). Those are by
  definition already-validated player-built frames; we do not move them.
- Any change to ChronoDawn-side ground rules (the bug is symmetric, but the
  primary report is for the Overworld-side return target where lakes / rivers
  are common; ChronoDawn currently has no fluid surfaces by design).

## Approach

### 1. Fluid-aware "clear space" predicate

`PortalTeleportHandler` keeps a single small helper:

```java
private static boolean isClearForPortalSpace(BlockState state) {
    // Air or a replaceable block (e.g., snow layer, tall grass)
    // BUT NOT a fluid — water and lava are excluded.
    return (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty();
}
```

`getFluidState().isEmpty()` covers vanilla water/lava and any modded fluid
that follows the standard waterlogging contract. The earlier draft of this
design also introduced `isSuitablePortalGround` to gate the frame on solid
ground; that gating is now removed (see section 2), so the second predicate
is dropped.

### 2. `findGroundLevel()` redesign

Replace the current "find surface → search down for solid ground → check air
above" flow with a minimal placement rule:

1. Read `surfaceY = level.getHeight(WORLD_SURFACE, x, z)` (server-time
   heightmap; not the `_WG` worldgen variant).
2. Probe the topmost natural block at `surfaceY - 1`. If it is a fluid
   (`getFluidState().isEmpty() == false`) — i.e., the column ends in water
   or lava — start the candidate `frameY` at `surfaceY - 1`. Otherwise start
   `frameY` at `surfaceY` (above the natural surface, as a normal land-built
   portal would).
3. From the candidate `frameY`, walk upward one block at a time until the
   portal **interior** at rows `frameY+1 … frameY+3` (the three 2x3 portal
   blocks vertically) is `isClearForPortalSpace` everywhere. The bottom-frame
   row at `frameY` itself is NOT checked — it is overwritten by Clockstone
   on placement and is allowed to coincide with the top fluid block.
4. Cap the upward walk at a fixed Y=250 ceiling and fall back to Y=120 if no
   clear interior is found. (Avoids version-specific `getMaxBuildHeight()` /
   `getMaxY()` API differences.)

The downward-through-fluid search is removed entirely (unchanged from the
previous draft). The new behaviour for the two common cases:

- **Open ocean / lake**: top water block at `surfaceY - 1`. Start at
  `frameY = surfaceY - 1`. Interior at `surfaceY … surfaceY+2` is air. Return
  `frameY`. The frame's bottom row replaces the top water block; interior
  sits exactly at the water surface, dry. Matches what a player typically
  hand-builds when raising a portal from open water.
- **Natural land**: top of grass / dirt at `surfaceY - 1`. Topmost block is
  non-fluid. Start at `frameY = surfaceY`. Interior is 3 air blocks. Return
  `frameY`. The frame sits ABOVE the ground; the topsoil is untouched.

The "1 block higher than necessary" over-correction on water that motivated
this revision is gone: when the surface is fluid, the frame can occupy the
fluid block instead of being pushed above it.

### 3. No forced footing

The earlier draft wrote 4 extra Clockstone blocks below the frame's bottom
row. This is dropped. Two reasons:

- A frame with an extra Clockstone row directly beneath its bottom edge is
  outside the canonical 4x5 shape, and `PortalFrameValidator` rejects it
  during re-ignition checks (observed failure: an extinguished generated
  portal cannot be re-lit by the player because the validator can no longer
  identify the frame).
- With `findGroundLevel` now placing `frameY` so that the interior is always
  clear (either above ground or at the fluid surface), the footing was never
  load-bearing. The bottom-frame row itself is the player's step-out
  surface, identical to vanilla Nether portal behaviour.

`generatePortal` ends with just the `generatePortalStructure` call — no
auxiliary block writes.

### 4. Failure mode

`generatePortal()` continues to return the frame bottom-left position. With
the Y=120 fallback in step 2.4, it can no longer return `null` for
fluid-related reasons. The signature stays nullable to preserve existing
call-site handling for genuinely impossible cases (e.g., dimension chunk
loading failure).

## Files affected

For each of the 11 supported versions:

- `common/<version>/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`

Helper code is local to each copy. We do **not** lift this into
`common/shared/` in this PR — the existing pattern keeps version-specific
ServerLevel / Heightmap API quirks isolated, and consolidation would balloon
the change. (See `feedback_resource_dedup_deferred.md` for the parallel
decision on JSON dedup.)

## Cross-version API risks

- `level.getMaxBuildHeight()` was renamed across the 1.21.x line; verify
  per-version. Where the call differs, copy the equivalent from a sibling
  Java file in the same version directory (per the established cross-version
  porting pattern).
- `BlockState.getFluidState()` exists on all supported versions.
- `BlockTags.LEAVES` exists on all supported versions.

No new abstraction-layer (`compat/`) method is needed.

## Testing

**Unit tests** (`PortalStateTest` lives in `common/shared/src/test/java/...`,
but it does not currently exercise `PortalTeleportHandler` directly — it
covers `PortalState`). Adding a `PortalTeleportHandlerTest` would require a
non-trivial `ServerLevel` stub; given the cost, we instead rely on:

- A focused unit test for the `isClearForPortalSpace` predicate, asserting
  that water and lava `BlockState`s return `false` and that air / snow-layer
  return `true`. The earlier draft also tested `isSuitablePortalGround`;
  that predicate is removed alongside the redesign, and its tests with it.

**Manual verification** (must pass before merge, on at least one version,
ideally 1.21.11):

1. Build a ChronoDawn portal on a small island in an ocean biome with the
   surrounding water deeper than 5 blocks.
2. Activate and travel to ChronoDawn, then return.
3. Confirm: destination portal interior is NOT submerged. The frame's bottom
   row may sit at water level (water lapping at its sides) — that is the
   intended look. The 2x3 interior column is dry.
4. Repeat in a lava lake. Same acceptance: interior dry, bottom-frame row
   can occupy the surface lava block.
5. Repeat on flat land — frame sits ABOVE the natural surface, identical to
   a player-built portal (no terrain overwrite, no extra Clockstone below
   the frame).
6. Re-ignite test: break a portal block of a freshly-generated destination
   portal, then re-light it with a Time Hourglass. Confirm the frame is
   recognised by `PortalFrameValidator` and re-ignites cleanly. (This was
   the symptom that motivated dropping the forced footing.)

## Open risks / follow-ups

- Modded fluids that do not implement the standard waterlogging contract
  may still slip through `getFluidState().isEmpty()`. Acceptable: this is
  consistent with vanilla's own assumption.
- A destination portal placed on the water surface has no visible
  walkway back to dry land. The player can still re-enter the portal (the
  frame's bottom row is solid Clockstone at the water surface), so this is
  a navigability inconvenience rather than a soft-lock. Approach-bridge
  generation is deferred.
- `PortalFrameValidator` rejecting frames with extra Clockstone immediately
  beneath the bottom row is, strictly speaking, a separate validator
  issue. We avoid the symptom by not creating such shapes; the validator
  itself is left as-is for now.
