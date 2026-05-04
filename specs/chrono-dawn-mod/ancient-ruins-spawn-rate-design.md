# Ancient Ruins Spawn Rate Tuning — Design

**Date**: 2026-05-04
**Status**: Approved (pending implementation plan)

## Problem

User feedback reports that Ancient Ruins generate too frequently. The current
configuration (`common/shared/.../structure_set/ancient_ruins.json`):

```json
{
  "placement": {
    "type": "minecraft:random_spread",
    "salt": 20005897,
    "spacing": 24,
    "separation": 8
  }
}
```

with biome tag `has_ancient_ruins` containing `#minecraft:is_forest` and
`#minecraft:is_taiga` produces a structure denser than vanilla villages
(spacing 32) and equal to Ancient Cities (spacing 24).

Two consequences:

1. **Gameplay**: ruins are visually intrusive and lose the "lost civilization"
   feel; players rarely have to explore to find one.
2. **Documentation drift**: the in-game Chronicle entry already describes ruins
   as *"relatively rare"* and claims they spawn in *"plains, forests, deserts,
   and more"*. Both statements contradict the implementation.

## Goals

- Reduce Ancient Ruins density to fit the existing Chronicle copy and the
  intended discovery loop ("explore deliberately, but always find one").
- Target time-to-first-ruin: roughly **600–1000 blocks** of exploration from a
  random spawn (not the current 100–300).
- Avoid worst-case seeds where the player has to travel 5000+ blocks (the
  failure mode of vanilla woodland mansions).

## Non-Goals

- No new items, recipes, advancements, locator maps, or villager trades.
- No changes to the Ancient Ruins structure NBT, loot table, or processor list.
- No changes to other ChronoDawn structures (Forgotten Library, Master Clock,
  etc.).

## Approach

Two coordinated changes to the existing JSON, plus consequential documentation
fixes. No code changes.

### 1. Loosen `random_spread` placement

`common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json`:

| Field | Before | After |
| --- | --- | --- |
| `spacing` | 24 | **56** |
| `separation` | 8 | **20** |
| `salt` | 20005897 | unchanged |

Grid cell size grows from ~384 blocks to ~896 blocks; minimum gap between any
two ruins grows from ~128 to ~320 blocks.

### 2. Narrow the biome tag (with a common-biome safety net)

`common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_ancient_ruins.json`:

Before:
```json
{
  "replace": false,
  "values": [
    "#minecraft:is_forest",
    "#minecraft:is_taiga"
  ]
}
```

After:
```json
{
  "replace": false,
  "values": [
    "#minecraft:is_taiga",
    "minecraft:dark_forest"
  ]
}
```

`#minecraft:is_taiga` resolves to `taiga`, `snowy_taiga`,
`old_growth_pine_taiga`, `old_growth_spruce_taiga`. Of these, **regular
`taiga` is one of the most globally distributed biomes**, which is the
critical safety net: it guarantees that no seed will leave the player without
a valid spawn region within a few hundred blocks of cluster boundaries.

`dark_forest` adds the "old, shadowy woods" flavour without becoming the sole
target (which would replicate the woodland-mansion problem).

Biomes intentionally **dropped** from the tag:

- `forest`, `birch_forest`, `flower_forest`, `old_growth_birch_forest` —
  the temperate/light forest variants of `#minecraft:is_forest`. They don't
  fit the "lost civilization" framing and were the main source of cluttered
  spawning.

### Expected distance and density

Rough order-of-magnitude estimates (vanilla seeds, average case):

| Quantity | Before | After |
| --- | --- | --- |
| Grid cell size | ~384 b | ~896 b |
| Biome eligibility (rough share of land) | ~30% | ~15% |
| Effective avg distance between ruins | ~700 b | ~2300 b |
| Expected distance to first ruin from spawn | ~200–400 b | ~1000–2000 b |
| Worst-case distance (unlucky seeds) | ~800 b | ~2500 b |

The "worst case" stays well under the 5000-block fail threshold because at
least one common biome (`taiga`) is in the tag.

## Consequential changes

### Chronicle entry

`common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json`:

- Page 4 currently says "ruins can spawn in plains, forests, deserts, and
  more" — this is incorrect today and incorrect after the change. Rewrite
  to describe taiga + dark forest as the target biomes, framed as a hint:
  "Look in old taigas and shadowy dark forests — the kind of woods where
  time itself seems to slow."
- Page 2's "ruins are relatively rare, so exploration is key" can stay; the
  implementation now matches it.

### Spec acceptance criterion

`specs/chrono-dawn-mod/spec.md` SC-001 currently states:

> Players can discover Ancient Ruins in the Overworld and obtain materials
> and blueprints necessary for entering Chrono Dawn within **10 minutes**.

Relax to **30 minutes** to match the new discovery loop. This is a copy
change, no test impact.

### Changelog

Add an `Unreleased` entry summarising the change for players.

## Files touched

1. `common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json`
2. `common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_ancient_ruins.json`
3. `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json`
4. `specs/chrono-dawn-mod/spec.md`
5. `CHANGELOG.md`

All under `common/shared/`, so the change applies to all 11 supported
Minecraft versions (1.20.1 through 1.21.11) automatically. No
version-specific overrides expected.

## Verification

1. `./gradlew validateResources` — confirms JSON syntax stays valid and the
   biome tag references resolve.
2. Manual smoke test on at least one Fabric and one NeoForge version
   (`./gradlew runClientFabric1_21_11`, `./gradlew runClientNeoForge1_21_11`):
   create a fresh world, fly outward, confirm ruins appear in taiga or
   dark forest (not in plain forests) and at noticeably wider intervals.
3. Re-read the in-world Chronicle entry to confirm the updated copy matches
   what the player will actually experience.

## Risks

- **Existing worlds**: chunks already generated retain their existing ruins;
  only newly generated chunks reflect the change. This is the standard
  Minecraft worldgen behaviour and is acceptable.
- **Mod compatibility**: other mods that injected into `is_forest`-tagged
  biomes will no longer cause Ancient Ruins to spawn there. This is
  intentional.
- **Tighter biome list could still feel rare to some players**: if post-launch
  feedback says ruins are now too hard to find, the cheapest follow-up is to
  add `forest` (the common temperate forest) back into the tag while keeping
  the looser spacing.

## Out of scope (potential follow-ups)

- Locator items / explorer maps for Ancient Ruins (case "C" from the design
  discussion). Revisit only if the simpler tuning is not enough.
- Tuning Phantom Catacombs (currently spacing 20/sep 8, denser than this
  proposal). Out of scope for this change.
