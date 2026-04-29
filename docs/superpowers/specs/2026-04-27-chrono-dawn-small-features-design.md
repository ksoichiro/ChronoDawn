# Chrono Dawn Small Features — Design

Date: 2026-04-27
Status: Draft (pending user review)

## Goal

Add 7 small, naturally-scattered placed features to the Chrono Dawn dimension
to fill the empty space between existing dungeon-scale structures. The features
are predominantly decorative — only one of the seven contains a loot chest —
and reinforce the time theme through "frozen / petrified / abandoned moment"
motifs. They cover all five POI patterns (mini-structure, lore ruin, traveler
camp, themed landmark, natural distortion) catalogued in
`docs/design/world_variety_patterns.md` Axis 1.

## Non-Goals

- New blocks or items (all 7 reuse existing ChronoDawn / vanilla blocks).
- Boss encounters or combat objectives.
- `/locate` discoverability (these are ambient features, not destinations).
- Dungeon-scale loot rewards. The single loot chest is intentionally weaker
  than existing dungeon chests so it does not devalue boss rewards.
- Ocean placement. Surface ocean variants (sunken clockface, beached boat) are
  deferred — they need water-aware placement that is out of scope here.
- Behavioral/animated mechanics (e.g., spinning sundial gnomons, sand falling
  through hourglass). All visuals are achieved through static block layout.

## Feature Set

Seven features grouped by pattern category. Each is implemented as a single
NBT template loaded via a shared `NbtTemplateFeature` (see Architecture).

| # | ID | Footprint | Tier | Biomes |
|---|----|-----------|------|--------|
| 1 | `time_well` | 3×3×4 | T3 (rare landmark) | plains, forest, prairies |
| 2 | `petrified_adventurer` | 1×1×2 + Item Frame | T2 (standard ambient) | all land biomes (9) |
| 3 | `time_cairn` | 1×1×3 | T1 (filler) | all land biomes (9) |
| 4 | `watchmaker_camp` | 5×2×5 | T4 (rare loot) | plains, forest |
| 5 | `old_sundial` | 3×3×3 | T2 (standard ambient) | plains, prairies |
| 6 | `hourglass_monolith` | 3×3×5 | T2 (standard ambient) | desert |
| 7 | `upside_down_tree` | 4×4×6 | T2 (standard ambient) | ancient_forest |

"All land biomes" = plains, forest, prairies, ancient_forest, dark_forest,
mountain, desert, snowy, swamp. Ocean is excluded.

### 1. Time-Stopped Well (`time_well`)

A round stone well frame with the water surface frozen in place — the bucket
hangs in mid-air on its rope, captured at the moment time stopped.

- Frame: Cobblestone Wall on the four corners, Mossy Cobblestone on the four
  side walls, Cobblestone on the top rim. Hollow 1×1 center shaft.
- Roof posts: Time Wood Log on two opposite corners (vertical), Time Wood Slab
  spanning between them as the small roof.
- "Frozen water surface": one Blue Ice block at the rim level instead of a
  water source. The visual reads as ice-crystallized water, reinforcing the
  time-stopped theme.
- Bucket: Item Frame attached to the roof post displaying a Bucket item.
- Embedded reward: one Temporal Amber Ore (`chronodawn:temporal_amber_ore`)
  placed at the bottom of the shaft, beneath the Blue Ice. The player must
  mine through the ice to retrieve it (the ore drops `raw_temporal_amber`).
  No loot table — the reward is a single placed block, deterministic per
  instance. (Note: there is no `temporal_amber_block` in the registry; the
  ore is the canonical "buried amber" form.)
- Surroundings: a 1-block-wide ring of tall grass / temporal_root patches
  (decoration optional, NBT-embedded).

### 2. Petrified Adventurer (`petrified_adventurer`)

A traveler caught mid-stride and turned to stone, with a discarded tool nearby.

- Body: 2 blocks stacked vertically — the upper block is plain Temporal
  Stone (the "torso/head"); the lower block is Temporal Stone Bricks (the
  "worn legs", reads more weathered). Both are full blocks so the figure
  sits flush with the ground. (There is no `polished_temporal_stone`
  block family — only the deepslate variant has polished forms — so
  `temporal_stone_bricks` carries the "old/carved" look.)
- Pose hint: one adjacent Temporal Stone Stair to imply a forward-leaning
  body, oriented per random rotation.
- Discarded tool: Item Frame on a small flat stone tile (1 Stone Bricks block
  on ground) beside the body. Frame holds a randomly weighted item: Iron
  Pickaxe, Iron Sword, or Iron Shovel (selection lives in the NBT data via
  multiple variant NBTs, see "Variants" below).

Variants (separate NBT files keyed off biome / random):

- `petrified_adventurer.nbt` — default (any non-snowy land biome).
- `petrified_adventurer_snowy.nbt` — snowy biome variant. Adds a Snow layer
  partially covering the figure to read as frostbitten.

### 3. Time Cairn (`time_cairn`)

A small mossy stone stack with a flat clockwork dial on top — left as a
waymarker by long-vanished travelers.

- Bottom: Mossy Cobblestone Slab (top half) or Mossy Cobblestone block.
- Middle: Cobblestone block.
- Top: Iron Trapdoor placed horizontally and closed, oriented per random
  rotation so it reads as a clock face.

This is the densest filler. It is intentionally minimal so high-density
placement does not feel cluttered.

### 4. Watchmaker's Camp (`watchmaker_camp`)

An abandoned campsite belonging to a wandering watchmaker. The only feature
in this set with a loot chest.

- Centerpiece: Campfire with `lit=false`.
- Workbench cluster (2-block-wide arc around the campfire):
  - Crafting Table
  - Lectern
  - Chest (loot table: `chronodawn:chests/watchmaker_camp`)
  - Anvil (slightly damaged variant — `chipped_anvil` block)
- Tent frame (covers a 3×2 portion of the 5×5):
  - 4 Time Wood Fences as poles at the corners
  - White Wool blocks forming a flat roof. (Vanilla has no
    `white_wool_stairs`; the original "sloped roof" idea is dropped —
    flat ceiling reads fine for an abandoned camp.)
- Decoration: 1 Item Frame mounted on the side of the Crafting Table
  holding a Clock; 1 Cobblestone block as a stool. The natural ground
  (grass / temporal_grass) is left as-is — replacing it with Coarse Dirt
  at template Y=0 would float per the half-block pitfall (Appendix A.1).
  If a "tent floor" is desired in a follow-up, switch the configured
  feature to `y_offset=-1` and place Coarse Dirt at template Y=0 to
  replace the grass instead of stacking on top of it.

Loot table — `chronodawn:chests/watchmaker_camp`:

```
pool { rolls: uniform 2..4 }
  Copper Ingot    1..3   weight 4
  Time Wood Log   1..3   weight 3
  Bread           1..2   weight 3
  Iron Ingot      1      weight 2
  Time Crystal    1..2   weight 2
  Clock           1      weight 1
  Compass         1      weight 1
  Raw Temporal Amber  1..2  weight 1
  Fruit of Time   1..2   weight 1
total weight: 18
```

(Item IDs reflect actual ChronoDawn registry: `chronodawn:time_crystal` is the
ore-drop item — there is no separate "shard" form — and the unprocessed amber
drop is `chronodawn:raw_temporal_amber`. Both forms read naturally as a
watchmaker's stockpile of raw materials.)

The expected per-chest yield (3 rolls × 18 weight) keeps Time Crystal and
Raw Temporal Amber visible but not abundant — a player exploring 5–10 camps
gets a useful but non-game-breaking trickle of progression materials.

### 5. Old Sundial (`old_sundial`)

A weathered stone pedestal with a vertical metal gnomon. Static (does not tell
real time).

- Base: 3×3 platform of Smooth Stone (`minecraft:smooth_stone`, full block).
  Vanilla has no `polished_stone_slab` block, and a top-half slab placed at
  template Y=0 would float — see Appendix A.1's "half-block float" pitfall.
- Pedestal: 1 Stone Bricks block at the center on top of the base.
- Gnomon: 1 Iron Bars block placed vertically on top of the pedestal.
- "Cracked" detail: 1–2 Cobblestone Stairs around the base, oriented to read
  as broken edges.

### 6. Hourglass Monolith (`hourglass_monolith`)

A tall sandstone monolith carved in the shape of an hourglass — a desert-only
landmark visible from a distance.

- Lower bulb (layers 1–2): 3×3 ring of Temporal Sandstone Wall
  (`chronodawn:temporal_sandstone_wall` — vanilla has no
  `smooth_sandstone_wall`) on layer 1, 3×3 solid Temporal Sandstone
  (`chronodawn:temporal_sandstone`) on layer 2. Using the ChronoDawn
  variant matches the desert biome's `temporal_sand` ground.
- Pinch (layer 3): 1×1 Chiseled Sandstone (vanilla
  `minecraft:chiseled_sandstone`) at the center, surrounded by air.
- Upper bulb (layers 4–5): mirror of the lower bulb (3×3 solid Temporal
  Sandstone, then 3×3 Temporal Sandstone Wall ring on the top).
- "Sand has run through" detail: 2–3 Sand blocks settled at the bottom of the
  lower bulb. No falling Sand above the pinch — gravity-affected blocks above
  air would fall on chunk load and disrupt the visual. The empty pinch reads
  as "all the sand has already run out", which fits the time theme.

### 7. Upside-Down Tree (`upside_down_tree`)

A Time Wood tree caught in a localized time inversion — its roots reach up
into the air and its canopy is buried at ground level.

- Trunk: 3 Time Wood Log blocks vertical, axis=y.
- Inverted roots (top): 5–7 Time Wood Log blocks branching horizontally from
  the trunk top, axis=x or axis=z, reading as gnarled roots in the air.
- Inverted canopy (base): one ground-level layer of Time Wood Leaves
  surrounding the trunk in a 3×3 ring, with 1–2 Fruit of Time blocks
  optionally embedded in the leaves.
- The trunk's bottom block sits on terrain; no underground excavation. The
  "buried canopy" effect is achieved by having leaves at ground level,
  partially covered by 1-block-tall grass / dirt patches placed by the NBT.

## Architecture

### Java — shared `NbtTemplateFeature`

A single generic feature class lives in `common/shared/` alongside existing
tree features (`com.chronodawn.worldgen.features.*`):

```
package com.chronodawn.worldgen.features;

public class NbtTemplateFeature extends Feature<NbtTemplateConfiguration> {
    // Reads the configured ResourceLocation -> StructureTemplate
    // Picks a random rotation if the config allows it
    // Adjusts placement to ground level (heightmap MOTION_BLOCKING_NO_LEAVES)
    // Calls template.placeInWorld(...) with NO_CALLBACK + StructurePlaceSettings
}
```

The existing pattern (e.g. `FruitOfTimeTreeFeature.java`,
`AncientTimeWoodTreeFeature.java`) keeps the canonical implementation in
`common/shared/` and overrides only in `common/1.21.11/` when API forced a
divergence. We follow the same convention for `NbtTemplateFeature`.

The configuration carries the template `ResourceLocation`, an optional list of
biome tag predicates (or surface block predicates), a "random rotation"
toggle, and a "ground offset" Y delta. Each of the seven features is just a
`configured_feature` JSON pointing this single class at a different template.

API differences across versions are limited to:

- `RandomSource` location (`net.minecraft.util.RandomSource` from 1.18+ — same
  in all supported versions, no split needed).
- `BlockPos.MutableBlockPos` and `Heightmap` paths — same across versions.
- `StructureTemplate#placeInWorld` signature — stable across 1.20.1 / 1.21.x.
- `Util` package moved in 1.21.11 (memory: `feedback_util_package_1_21_11`)
  — only relevant if we import `Util` directly. The Feature implementation is
  expected to avoid it.

The class is therefore expected to live entirely in `common/shared/` with no
per-version overrides. If a version-specific subtle difference appears, only
that version's `common/<version>/` module gets a partial override using the
existing `compat/` abstraction pattern.

### Data files

```
common/
  shared/src/main/java/.../worldgen/feature/
    NbtTemplateFeature.java
    NbtTemplateConfiguration.java
    ModFeatures.java                    (registration)
  shared/src/main/resources/data/chronodawn/worldgen/configured_feature/
    time_well.json
    petrified_adventurer.json
    petrified_adventurer_snowy.json
    time_cairn.json
    watchmaker_camp.json
    old_sundial.json
    hourglass_monolith.json
    upside_down_tree.json
  shared/src/main/resources/data/chronodawn/worldgen/placed_feature/
    time_well_placed.json
    petrified_adventurer_placed.json
    petrified_adventurer_snowy_placed.json
    time_cairn_placed.json
    watchmaker_camp_placed.json
    old_sundial_placed.json
    hourglass_monolith_placed.json
    upside_down_tree_placed.json
  shared/src/main/resources/data/chronodawn/loot_table/chests/
    watchmaker_camp.json                (1.21.1+ singular)
  1.20.1/src/main/resources/data/chronodawn/loot_tables/chests/
    watchmaker_camp.json                (1.20.1 plural — see Risks)
  shared-1.21.1+/src/main/resources/data/chronodawn/structure/
    time_well.nbt
    petrified_adventurer.nbt
    petrified_adventurer_snowy.nbt
    time_cairn.nbt
    watchmaker_camp.nbt
    old_sundial.nbt
    hourglass_monolith.nbt
    upside_down_tree.nbt
```

NBT files live in **only one place** — `common/shared-1.21.1+/`. The build
pipeline propagates them to other versions:

- `gradle/nbt-block-replacement.gradle` (`replaceNbtBlocks` task) reads from
  `shared-1.21.1+/structure/`, applies vanilla→ChronoDawn block ID
  rewrites per `scripts/nbt_block_mappings.json` (currently
  `minecraft:dirt`/`grass_block`/`coarse_dirt` → ChronoDawn temporal
  variants), and writes the result to `${buildDir}/generated/nbt-block-replaced/`.
- 1.21.x version modules add that build directory to their resources via
  `srcDir` in `sourceSets`.
- 1.20.1 module additionally runs `convertNbtStructures`, which calls
  `gradle/shared/convert_nbt_1_21_to_1_20.py` to translate the 1.21 NBT
  format to 1.20.1 and rename the directory from `structure/` (singular)
  to `structures/` (plural — 1.20.1 data-pack convention).

NBTs therefore must NOT be hand-mirrored to `common/1.20.1/.../structure/`.
There is also no need to manually translate vanilla `dirt`/`grass_block`/
`coarse_dirt` to their ChronoDawn variants — the auto-replacement step
handles that.

### Biome integration

Each `placed_feature` is added to the relevant biomes' `features` array under
the `vegetal_decoration` step (or `top_layer_modification` for surface props).

Biome JSONs to update (per the established multi-version pattern):

- `common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/*.json`
- `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/*.json`
- `common/1.21.2/src/main/resources/data/chronodawn/worldgen/biome/*.json`
- `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/*.json`
- `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/*.json`

The 1.21.11 directory is independent (memory:
`feedback_1_21_11_biome_overrides`) and must be updated even though many other
versions inherit from `shared-1.21.2+`.

### Density tuning

Densities are realised in each `placed_feature` via placement modifiers:

| Tier | Modifier chain (starting point) | Target travel-distance |
|------|--------------------------------|------------------------|
| T1 | `count 1` + `rarity_filter chance=2` (~ once every 2 chunks) | ~100 blocks |
| T2 | `count 1` + `rarity_filter chance=16` (~ once every 16 chunks) | ~250 blocks |
| T3 | `count 1` + `rarity_filter chance=32` (~ once every 32 chunks) | ~450 blocks |
| T4 | `count 1` + `rarity_filter chance=48` (~ once every 48 chunks) | ~700 blocks |

Plus the standard `in_square` + `heightmap WORLD_SURFACE_WG` + `biome` steps.
Numbers are starting points; tuning happens after first playtest.

## Multi-Version Implementation Strategy

1. NBT templates live in `common/shared-1.21.1+/.../structure/` only.
   `gradle/nbt-block-replacement.gradle` rewrites a small set of vanilla
   block IDs to ChronoDawn variants (`scripts/nbt_block_mappings.json`),
   and the 1.20.1 module's `convertNbtStructures` task translates the
   1.21 NBT format to 1.20.1 (singular `structure/` → plural `structures/`).
   The author should choose blocks present in 1.20.1 (Cobblestone, Mossy
   Cobblestone, Time Wood, Temporal Stone variants, Iron Trapdoor, Iron
   Bars, vanilla Sandstone family, Blue Ice, Wool, Campfire, Crafting
   Table, Lectern, Chest, Anvil, Item Frame, Bucket, Clock, Sand, Coarse
   Dirt) so the converter does not have to drop unknown states.
2. The Java `NbtTemplateFeature` registration goes through the existing
   ChronoDawn `ModFeatures` (or equivalent) Architectury-compatible registry.
3. Feature registration must happen in **all** version-specific common
   modules' entry points (memory: `feedback_entity_attributes_all_versions`
   warns about partial registration; same pattern applies to features).
4. Loot table directory naming is split (memory:
   `feedback_loot_table_directory`):
   - 1.20.1 → `data/chronodawn/loot_tables/chests/watchmaker_camp.json`
   - 1.21.1+ → `data/chronodawn/loot_table/chests/watchmaker_camp.json`
5. Verification uses `./gradlew checkAll` per the project standard (memory:
   `feedback_check_all_before_phase_completion`). Per-task verification on a
   single Minecraft version is insufficient — biome JSON updates can silently
   miss a version.

## Risks and Known Pitfalls

Drawn from accumulated memory:

- **Biome update completeness** (`feedback_biome_ore_features_checklist`,
  `feedback_1_21_11_biome_overrides`). Missing a biome JSON update in any
  version directory means features silently do not generate in that version.
  Mitigation: maintain a per-feature × per-biome × per-version-dir checklist
  during implementation, then run `validateResources` and a test world load
  on at least 1.20.1, 1.21.1, 1.21.6, 1.21.11.
- **Loot table directory split** (`feedback_loot_table_directory`). Putting
  the watchmaker chest loot table in the wrong directory for a given version
  silently makes the chest empty. Mitigation: write to both locations from
  the start; the `validateResources` task should catch a typo.
- **Loot enchantment predicate format split** (`feedback_loot_table_silk_touch_format`).
  Not directly relevant — our loot table has no silk-touch branch. Confirmed
  N/A.
- **NBT block-id versioning**. Older NBT files saved with a vanilla version
  may carry block state IDs that are renamed/removed in a later version. We
  mitigate by saving the NBTs against 1.21.11 (newest) and verifying they
  load cleanly on 1.20.1 — block IDs used here are stable across the range.
- **`Heightmap.MOTION_BLOCKING` vs surface blocks like Snow** (potential).
  Placing on snowy biomes may put the feature on top of the snow layer
  instead of the ground. The `NbtTemplateFeature` will use
  `MOTION_BLOCKING_NO_LEAVES` and an optional Y offset to compensate; tested
  per feature as part of acceptance.
- **Random rotation breaks Item Frame orientations**. Item Frames carry their
  own facing in NBT. If the template is rotated, the NBT-saved facing must
  rotate with it. `StructureTemplate#placeInWorld` handles this for vanilla
  block entities; verify in a test world that the Bucket / Clock / Iron
  Pickaxe Item Frames render correctly in all 4 rotations.
- **Config feature registry name conflicts**. Eight new IDs under
  `chronodawn:` namespace — check for collisions with existing
  configured/placed features (`temporal_stalactite_cluster`,
  `clockwork_block_cluster`, etc.). The chosen names do not collide as of
  this design.

## Open Questions

- **Should `time_well` ever generate a "functional" variant** (real water
  source instead of Blue Ice) at low probability? Not in scope for v1; defer
  to a follow-up if play-testing shows the always-frozen version feels too
  static.
- **Should `petrified_adventurer` rarely include a partial backpack chest**
  with low-tier loot? Not in scope — keeping pure decoration preserves the
  T2/T4 tier separation. Re-evaluate after first release.
- **Snow biome variant of `time_cairn`**? Probably unnecessary at T1 density
  — players will see them often enough that one biome looking slightly
  different does not justify a second NBT. Skip.

## Acceptance Criteria

1. All 7 features generate visibly in their target biomes on a fresh world
   for at least 1.20.1, 1.21.1, 1.21.6, and 1.21.11 (sampling the API eras).
2. `./gradlew checkAll` passes (build + validateResources +
   validateTranslations + tests + GameTest).
3. Watchmaker camp chest contents match the documented weights — verified by
   opening 5+ camps in a test world or by loot-table unit test if the
   project has one for chests.
4. No new blocks or items are registered.
5. Density tiers feel right in playtest: walking ~100 blocks reveals a Time
   Cairn, ~250 blocks reveals a T2 feature, ~450 blocks reveals a Time Well,
   ~700 blocks reveals a Watchmaker camp.
6. No `validateResources` warnings for the new JSONs / biome edits.

## Future Extensions (Out of Scope)

These were proposed in brainstorming but cut to keep v1 focused. They are
candidates for follow-up specs:

- Reverse-Flow Fountain (A category)
- Cracked Tombstone (B category, alternate to Petrified Adventurer)
- Broken Cart (C category)
- Floating Gear Ring (mountain-specific D)
- Sunken Clockface (ocean D — needs water-aware placement)
- Frozen Watchman (snowy E)
- Time Pool (forest E)
- Clockwork Beached Boat (ocean shore)
