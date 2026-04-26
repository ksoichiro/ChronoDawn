# Temporal Grass Block Edge Tint — Design

Date: 2026-04-27

## Problem

Temporal Sand and Temporal Gravel disks placed by worldgen produce a visually
jarring color seam against the surrounding Temporal Grass Block. The gap is
more pronounced than with vanilla sand/gravel because every block in the
Chrono Dawn dimension shares a cool blue palette: Temporal Grass Block is
biome-tinted at runtime by `grass_color: 0x5B8AC4` (slate blue-green), while
Temporal Sand / Gravel are baked PNGs in a brighter pastel cyan. The two
neighboring blues fight for the same color space and any mismatch in
saturation or value reads as an unnatural step.

The user wants the grass blocks adjacent to a sand/gravel circle to
visually blend toward the circle, not the other way around. The transition
should also kick in for player-placed sand/gravel — not only natural worldgen.

## Goal

When a Temporal Grass Block is within 2 blocks (Chebyshev distance, same Y)
of any sand/gravel block, its grass tint shifts smoothly toward a paler
"edge" color. Distance 1 produces the strongest shift; distance 2 produces a
mild shift; distance ≥ 3 keeps the original biome tint. The result is a
2-block-wide gradient ring around every disk, eliminating the hard color
seam.

Item rendering (inventory icon) is unaffected — keeps the existing
`0x5B8AC4` constant.

## Non-Goals

- Worldgen / data pack changes. The effect is purely client-side.
- Persisted block state (no new `transition` blockstate property).
- A new sibling block. Temporal Grass Block stays as one block class.
- Changing the textures or baked colors of Temporal Sand / Gravel.
- Custom block models or per-face tinting. The single per-block tint that
  vanilla `BlockColors` supplies is sufficient.

## Approach

A new shared helper, `TemporalGrassEdgeTint`, owns the algorithm. Existing
loader-specific block-color lambdas in `ChronoDawnClientFabric.java` (×11
versions) and `ChronoDawnClientNeoForge.java` (×9 places) shrink to a single
call into this helper. All version-agnostic logic lives in
`common/shared/`, so the helper is written once.

### Algorithm

```
colorAt(state, world, pos):
    if world == null || pos == null
       || !(world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock):
        return DEFAULT_FALLBACK         # 0x5B8AC4 (constant, no biome lookup)

    base = BiomeColors.getAverageGrassColor(world, pos)

    minDist = 3                          # sentinel: nothing in radius 2
    for dx in [-2..2]:
        for dz in [-2..2]:
            if dx == 0 && dz == 0: continue
            d = max(|dx|, |dz|)          # Chebyshev (chess king distance)
            if d >= minDist: continue    # already have a closer hit
            neighbor = world.getBlockState(pos.offset(dx, 0, dz))
            if isEdgeTrigger(neighbor):
                minDist = d
                if d == 1: goto done     # cannot improve

    if minDist > 2: return base          # no edge nearby

    t = (3 - minDist) / 3.0              # d=1 → 0.667, d=2 → 0.333
    return lerpRgb(base, EDGE_TINT, t)
```

Per-channel linear interpolation: `out = base * (1 - t) + edge * t`, applied
to R, G, B independently.

### Constants

- **`EDGE_TINT`**: starts at `0x9CB6CC` — the midpoint between the slate
  blue-green grass tint (`0x5B8AC4`) and the lighter pastel of Temporal
  Sand. The exact value is refined during implementation by sampling the
  average pixel color of `temporal_sand.png` and biasing slightly toward
  the grass side. Final hex is committed with a comment recording how it
  was chosen.
- **`DEFAULT_FALLBACK`**: `0x5B8AC4` — same as today; used when the color
  provider is invoked outside a world context (e.g. some inventory render
  paths) or when the block at `pos` is not actually a Temporal Grass Block.

### Edge triggers

A block counts as an "edge" if `state.is(...)` matches any of:

- `chronodawn:temporal_sand`
- `chronodawn:temporal_gravel`
- `minecraft:sand`
- `minecraft:gravel`

Vanilla sand/gravel are included so player-imported sand/gravel from the
Overworld also triggers the blend, matching the user's expectation that
"any time grass touches sand, it should blend." A custom tag is rejected as
premature — four hardcoded checks are simpler and the list is unlikely to
grow.

### Y-axis

Scan only the same Y as `pos`. Sand/Gravel disks place blocks at the
surface where the grass also lives, so cross-Y neighbors are uncommon. If
visual testing later shows seams from Y mismatches, expanding to ±1 Y is a
mechanical change (3× the lookups). Documented as a tunable, not done now.

## File Changes

| File | Change |
| --- | --- |
| `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java` | New (~60 lines): static `colorAt`, constants, helpers |
| `fabric/{1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java` | Replace existing Temporal Grass Block lambda body with one-line call into helper (11 files) |
| `neoforge/{base, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java` | Same replacement (9 files; 1.21.1 / 1.21.2 use `base` directly) |

The lambda after the change reads:

```java
event.register(           // (or ColorProviderRegistry.BLOCK.register on Fabric)
    (state, world, pos, tintIndex) ->
        tintIndex != 0 ? -1 : TemporalGrassEdgeTint.colorAt(state, world, pos),
    ModBlocks.TEMPORAL_GRASS_BLOCK.get()
);
```

The `tintIndex != 0` guard stays in the lambda (per
`feedback_neoforge_tintindex_guard`) so the helper can assume tinted faces
only. Item color registrations are untouched.

## Performance

`BlockColors` runs only during chunk mesh re-baking (off the render
thread), not per-frame. For a chunk containing many Temporal Grass Blocks:

- Worst case: 24 `getBlockState` lookups per grass block (5×5 minus self).
- `getBlockState` on the `BlockAndTintGetter` snapshot is O(1).
- For comparison, vanilla `BiomeColors.getAverageGrassColor` already
  averages over a (radius 3 in older / radius 7 in newer) biome
  neighborhood, so the additional cost sits in the same envelope.

Early-exit: as soon as a distance-1 hit is found, the loop breaks (cannot
improve). Most grass blocks not near a disk pay the full 24 lookups but
nothing else.

No caching is added (per `feedback_mixin_client_cache` — caching client
state can mask updates). The function is pure and re-runs on every mesh
bake.

## Risks and Mitigations

- **Chunk boundaries.** `getBlockState` returns AIR for positions outside
  the loaded mesh snapshot. A grass block on the very edge of one chunk
  will not see a sand block in the adjacent unbaked chunk, producing a
  brief seam during chunk load. This matches how vanilla biome blending
  already behaves and is acceptable.
- **API drift across versions.** `BiomeColors.getAverageGrassColor` and
  `BlockAndTintGetter.getBlockState` are stable from 1.20.1 through
  1.21.11. The lambda parameter types stay loader-defined; the helper
  declares the looser interface (`BlockAndTintGetter`) which both Fabric
  and NeoForge satisfy.
- **EDGE_TINT looks wrong.** If the chosen midpoint reads as muddy or off,
  the constant is one-line tunable. No structural change required.

## Verification

1. Build all target versions: `./gradlew checkAll`
   - cleanAll → validateResources → validateTranslations → buildAll → testAll
   - This is required per `feedback_check_all_before_phase_completion`;
     single-version builds are not sufficient evidence for a multi-version
     change.
2. Visual smoke test on the default version (1.21.11):
   - `./gradlew runClientFabric1_21_11`
   - Generate a Chrono Dawn dimension, fly over `chronodawn:chronodawn_plains`
     until a Sand or Gravel disk is visible.
   - Confirm a 2-block-wide gradient ring around the circle, with no hard
     color seam.
   - Place a sand block on grass manually and confirm the surrounding
     grass tints in real time after the chunk re-bakes.
3. No GameTest. The effect is purely visual.

## Out of Scope (Future Work)

- ±1 Y-axis scan if seams appear at terrain steps.
- Tag-based edge trigger list if more block types ever need to participate.
- Distance-weighted blend (Approach C from brainstorming) if smoother
  intersections at overlapping disks become a concern.
