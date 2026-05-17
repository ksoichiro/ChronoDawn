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

1. Never place the destination portal so that any part of its frame interior
   or its bottom-row platform is inside a fluid (water, lava, or any block
   whose `BlockState.getFluidState()` is non-empty).
2. When the natural surface at the destination X/Z is over water or lava,
   pick an air position **higher up** in the same column and place the
   portal there with a forced Clockstone platform underneath, rather than
   diving below the fluid.
3. Guarantee a walkable Clockstone footing directly underneath the frame's
   bottom row (4 blocks) on every generated portal — including those placed
   on natural land, since the existing "search down for solid ground"
   adjustment can still pick a position with weak ground (e.g., leaves
   replaced by `canBeReplaced`).
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

### 1. Fluid-aware position predicate

Introduce a small helper in `PortalTeleportHandler`:

```java
private static boolean isClearForPortalSpace(BlockState state) {
    // Air or a replaceable block (e.g., snow layer, tall grass)
    // BUT NOT a fluid — water and lava are excluded.
    return (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty();
}

private static boolean isSuitablePortalGround(BlockState state) {
    return state.isSolid()
        && !state.isAir()
        && !state.canBeReplaced()
        && state.getFluidState().isEmpty()
        && !state.is(BlockTags.LEAVES);
}
```

`getFluidState().isEmpty()` covers vanilla water/lava and any modded fluid
that follows the standard waterlogging contract.

### 2. `findGroundLevel()` redesign

Replace the current "find surface → search down for solid ground → check air
above" flow with a position search that prefers staying at-or-above the
heightmap surface:

1. Start at `surfaceY = level.getHeight(WORLD_SURFACE, x, z)` (server-time
   heightmap; not the `_WG` worldgen variant).
2. If the block at `surfaceY` is non-fluid solid ground AND the 6 blocks
   above are all `isClearForPortalSpace`, place the portal one block above
   `surfaceY`.
3. Otherwise (water/lava at surface, or insufficient air clearance), move
   the candidate Y **upwards**, one block at a time, until either:
   - A solid non-fluid block is found directly below a 6-block clear column
     (natural land somewhere higher), OR
   - A position is found where the candidate block and the 6 blocks above
     are all clear (`isClearForPortalSpace`) — the portal will float on a
     generated Clockstone platform (step 3 of generation handles the
     platform).
4. Hard cap the upward search at `level.getMaxBuildHeight() - 10` (or the
   version-equivalent ceiling). If still nothing, fall back to Y=120 as a
   last resort and still generate with the forced platform — the player must
   never end up with a half-broken portal.

The **downward** search through fluids is removed entirely. This is the core
behavioural change: we never descend below a water/lava column to find
"ground."

### 3. Forced footing under the frame

In `generatePortalStructure()`, after the frame position is finalised, always
write a Clockstone block at each of the 4 positions directly below the
frame's bottom row, regardless of what is currently there:

```java
for (int x = 0; x < width; x++) {
    BlockPos footing = pos.relative(horizontal, x).below();
    level.setBlock(footing, clockstoneState, Block.UPDATE_ALL);
}
```

This guarantees:

- If the portal is placed on natural land, the footing overwrites the
  topsoil block under the frame (cosmetic only — invisible to the player
  since it is buried under the frame's bottom row).
- If the portal is floating above a previously-fluid column, this row is
  the actual platform that supports both the frame and the player stepping
  out.

Forcing 4 blocks instead of detecting "do we need a platform?" simplifies
the logic considerably and is consistent with the answered scope. The
landscape cost is minimal (4 blocks per generated destination).

### 4. Failure mode

`generatePortal()` continues to return the frame bottom-left position. With
the fallback in step 2, it can no longer return `null` for fluid-related
reasons. The signature stays nullable to preserve existing call-site
handling for genuinely impossible cases (e.g., dimension chunk loading
failure), but the teleport flow is now guaranteed to land the player on
solid Clockstone in air.

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

- A focused unit test for the new `isClearForPortalSpace` /
  `isSuitablePortalGround` predicates, asserting that water and lava
  `BlockState`s return `false` for "clear space" and `false` for "ground".
  These are pure functions of `BlockState` and need no level stub.

**Manual verification** (must pass before merge, on at least one version,
ideally 1.21.11):

1. Build a ChronoDawn portal on a small island in an ocean biome with the
   surrounding water deeper than 5 blocks.
2. Activate and travel to ChronoDawn, then return.
3. Confirm: destination portal in the Overworld does **not** appear inside
   the water column, and the frame sits on visible Clockstone.
4. Repeat in a lava lake (ChronoDawn → Overworld return targeting a known
   lava-pool X/Z).
5. Repeat on flat land — confirm no visible change to the player (the
   buried Clockstone row is under the frame's bottom edge).

## Open risks / follow-ups

- Modded fluids that do not implement the standard waterlogging contract
  may still slip through `getFluidState().isEmpty()`. Acceptable: this is
  consistent with vanilla's own assumption.
- The forced-platform approach means a destination portal can appear
  floating above water with no path back to land. The player can still
  re-enter the portal (it is on Clockstone), so this is a navigability
  inconvenience rather than a soft-lock. Mitigation (e.g., generating a
  small approach bridge) is deferred.
