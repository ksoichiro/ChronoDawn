# Tall Tree Mega-Form Redesign

**Date**: 2026-05-09
**Status**: Approved (pending implementation)

## Problem

Existing `_tall` configured-feature variants for ChronoDawn trees use a 1-block-thick straight (or forking) trunk topped with a small foliage cap. At trunk heights of 16-24 (`dark_time_wood_tree_tall`) the cap-on-toothpick silhouette looks unnatural — the leaf cluster is disproportionately small for the trunk height.

## Goal

Make tall tree variants visually proportional:

1. **Trunk thickness 4 blocks** (2x2) for all `_tall` variants.
2. **Cone-shaped foliage extending down ~60-70 % of trunk height** so leaves cover a height proportional to the trunk, not just a cap on top.

## Scope

In scope:
- `dark_time_wood_tree_tall.json`
- `ancient_time_wood_tree_tall.json`
- `fruit_of_time_tree_tall.json`

Out of scope:
- Default / `_short` / `_medium` / `_wide` variants (already proportional).
- `random` selectors and biome / placed_feature wiring (no ID rename).
- Custom block models, leaf rendering, fruit decorator logic.
- Bumping mega-tree spawn weights — frequency change is an acceptable side-effect of the larger footprint.

## Approach

For each target file, swap to vanilla mega-tree placers:
- `trunk_placer` → `minecraft:mega_jungle_trunk_placer` (2x2 trunk).
- `foliage_placer` → `minecraft:mega_pine_foliage_placer` (cone tapering down from the top by `crown_height`).
- `minimum_size` → `lower_size: 1`, `upper_limit: 2` so the placement check requires 3x3 trunk space and 5x5 foliage space.

ancient `_tall` loses its `forking_trunk_placer` branching — accepted trade-off (case I in brainstorm).

## Per-tree settings

| Tree                     | Trunk placer        | base / a / b | Final height | crown_height (uniform) |
| ------------------------ | ------------------- | ------------ | ------------ | ---------------------- |
| dark_time_wood_tree_tall | mega_jungle         | 16 / 5 / 3   | 16-24 (unchanged) | 12-16              |
| ancient_time_wood_tree_tall | mega_jungle      | 11 / 2 / 2   | 11-15 (was 8-13)  | 7-10               |
| fruit_of_time_tree_tall  | mega_jungle         | 10 / 3 / 1   | 10-14 (was 7-11)  | 6-9                |

`crown_height` uses ChronoDawn's flat IntProvider convention:

```json
"crown_height": {
  "type": "minecraft:uniform",
  "min_inclusive": 12,
  "max_inclusive": 16
}
```

## Common JSON shape

```json
{
  "type": "minecraft:tree",
  "config": {
    "trunk_provider": { "...": "unchanged custom log" },
    "trunk_placer": {
      "type": "minecraft:mega_jungle_trunk_placer",
      "base_height": <per-tree>,
      "height_rand_a": <per-tree>,
      "height_rand_b": <per-tree>
    },
    "foliage_provider": { "...": "unchanged custom leaves" },
    "foliage_placer": {
      "type": "minecraft:mega_pine_foliage_placer",
      "radius": 0,
      "offset": 0,
      "crown_height": {
        "type": "minecraft:uniform",
        "min_inclusive": <per-tree>,
        "max_inclusive": <per-tree>
      }
    },
    "force_dirt": false,
    "ignore_vines": true,
    "minimum_size": {
      "type": "minecraft:two_layers_feature_size",
      "limit": 1,
      "lower_size": 1,
      "upper_limit": 2
    },
    "dirt_provider": { "...": "unchanged temporal_dirt" },
    "decorators": [ /* dark, ancient: []; fruit: [chronodawn:fruit_decorator] */ ]
  }
}
```

## Side effects

1. **Generation density drops** for `_tall` variants because the wider footprint causes more `minimum_size` rejections in tight terrain. Acceptable: tall mega trees are meant to be rarer.
2. **Fruit decorator output increases** with ~3-4× more leaf blocks on `fruit_of_time_tree_tall`. Observe in playtest; tune later if oversaturated.
3. **No biome / placed_feature edits**: same configured_feature IDs, same references.
4. **Multi-version**: `mega_jungle_trunk_placer`, `mega_pine_foliage_placer`, and `two_layers_feature_size` all exist in vanilla MC 1.20.1 → 1.21.11. No version branching.

## Verification

1. `./gradlew validateResources` — JSON syntax + cross-reference checks.
2. Visual smoke test on at least one version (e.g. `./gradlew runClientFabric1_21_11`):
   - Locate a temporal biome with the `_tall` variants.
   - Confirm trunks are 2x2, foliage forms a cone covering the upper ~60-70 % of trunk height.
   - Confirm fruit decorator still places fruit on `fruit_of_time_tree_tall`.
3. (Optional) `./gradlew buildAll` for full multi-version sanity.

## Risks

- **`fruit_decorator` interaction with mega trunks** — decorator runs on the produced tree blocks; mega geometry should be transparent to it, but worth confirming in step 2 above.
- **Stumpy outliers** at the low end of each height range (e.g. ancient at trunk 11 with crown 7-10): height ranges were chosen so even the shortest sample stays clearly tall (≥ 10 trunk + ≥ 6 crown), but if visuals disappoint a follow-up bump of the bases is the lever.
