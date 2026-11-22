# Plains and Mountain Surface Variety

Status: implemented (2026-11-22). Textures are placeholders.

## Intent

Walking through Chrono Dawn Plains (`chronodawn:chronodawn_plains`) and
Chrono Dawn Mountain (`chronodawn:chronodawn_mountain`) should look different
from the other Chrono Dawn biomes. Today both use the default surface
(`temporal_grass_block` over `temporal_dirt`), and their decorations
(`boulder_placed`, `fallen_log_placed`, flowers) are shared with most other
biomes. The only biome-specific surface rule besides the desert is the steep
mountain rock face added in [PR #54](https://github.com/ksoichiro/ChronoDawn/pull/54), which exposes plain `temporal_stone` on
every steep mountain slope above water.

The dimension also lacks natural stone variety. Vanilla has granite,
andesite and diorite; Chrono Dawn only has `temporal_stone` (plus deepslate
and cobblestone forms). Two new natural stones fill that gap and give the
plains and mountain material of their own.

## Goals

- Mountain: the steep rock face is a mottled mix instead of a single block.
- Two new natural stones with stairs, slab and wall, usable as building
  material and visible in the world.
- Plains: three plains-only surface features (rock outcrops, ruined wall
  remnants, worn dirt paths) that the neighbouring Prairies and Faded Plains
  do not get.
- Same behaviour on every supported version and loader.

## Non-goals

- Polished variants of the new stones (follow-up task, since added: Polished
  Dawnstone and Polished Duskstone with stairs and slabs, no walls, like
  vanilla polished granite and andesite).
- Biome behaviour gimmicks from `docs/design/world_variety_patterns.md`
  (low-gravity summit, synchronized blooms).
- Changes to Prairies, Faded Plains or other biomes beyond what the new
  stones' underground blobs need.
- Retrofitting existing chunks. Surface rules and features apply to newly
  generated chunks only.

## Anti-goals

- The mountain must not turn into a grey mass. Rock belongs on steep faces.
  Gentle slopes and plateaus keep grass and snow.
- Plains decorations must not crowd out the existing wayside features (Time
  Well, Old Sundial, Watchmaker's Camp) or read as man-made villages.
- No textures recoloured from vanilla PNGs.

## Decisions made by the owner

1. Add two new stones, each with stairs, slab and wall (8 blocks total).
   Polished variants are a separate task.
2. Plains gets three features: low outcrops of the new stone, ruined wall
   remnants, and worn dirt paths. Flower meadows were not chosen.
3. Mountain steep faces mix the three existing stones with surface noise,
   roughly 60% Temporal Stone, 25% Temporal Cobblestone and 15% Mossy Temporal
   Cobblestone. Part of the new stone also shows on the cliffs.
4. Textures are original placeholders drawn by a script (no vanilla pixels).
   The owner hand-edits them later. The PR says so.

## Approaches considered

For the mountain mix:

- **Surface-noise mottling (chosen).** `noise_threshold` conditions on
  `minecraft:surface` inside the existing steep branch. JSON only, already
  used by the badlands-style rules in the same file.
- Altitude bands (mossy low, cobble mid, stone high). Rejected by the owner:
  bands look artificial on the dimension's moderate mountain heights.
- Equal mix. Rejected: cobblestone dominating reads as rubble, not cliffs.

For plains features:

- **Surface rule for paths, small NBT templates for outcrops and walls
  (chosen).** No new Java code. Outcrops and walls reuse the existing
  `chronodawn:nbt_template` feature that wayside features already use, which
  has one schema per era (flat in 26.3) and no per-era block state format.
- Vanilla `disk` plus `random_patch` for outcrops. Rejected during
  implementation: `random_patch` and state provider schemas change in 26.x
  and 26.3, the disk and its stones would be placed independently, and a
  template gives a hand-shaped look.
- A new Java feature for walls. Rejected: 14 version copies for something a
  template does.

## Design

### New blocks (builder's call on names, see "Calls made")

| ID | English | Japanese | Look |
|---|---|---|---|
| `dawnstone` | Dawnstone | ドーンストーン | warm rose-amber, granite-like speckle |
| `dawnstone_stairs` / `_slab` / `_wall` | Dawnstone Stairs / Slab / Wall | ドーンストーンの階段 / ハーフブロック / 塀 | |
| `duskstone` | Duskstone | ダスクストーン | cool blue-grey, andesite-like fine grain |
| `duskstone_stairs` / `_slab` / `_wall` | Duskstone Stairs / Slab / Wall | ダスクストーンの階段 / ハーフブロック / 塀 | |

Properties follow vanilla granite/andesite: stone-like strength, requires a
pickaxe (`minecraft:mineable/pickaxe`), drops itself (no cobble form), no
tool tier gate. Follow `.claude/skills/add-terrain-block/SKILL.md` for every
version: block classes where the version needs them, registration, items,
creative tab, blockstates, models, client items (1.21.4+), loot tables
(slab double drop), tags (`mineable/pickaxe`, `minecraft:stairs`,
`minecraft:slabs`, `minecraft:walls`, and `blocks_motion_no_leaves` on 26.3
per the heightmap memo), translations (en_us and ja_jp in all lang copies).

Recipes (each with a recipe-unlock advancement in every era, including 26.3's
schema):

- stairs (6 from 6), slab (6 from 3), wall (6 from 6) by crafting
- stonecutter: stone → stairs, slab ×2, wall
- No recipe creates the base stone (like granite we do not add a crafting
  recipe from other stones in this task).

Conventional tags: add the base stones to `c:stones` (block and item) next
to `temporal_stone` in `common/shared-1.21.1+/.../data/c/tags/`. 1.20.1 has
no such entry for `temporal_stone`, so it gets none. Do not add them to
`minecraft:stone_crafting_materials` or `minecraft:stone_tool_materials`,
because `temporal_stone` is not in those tags either (only the cobblestone
forms are).

### Underground blobs

Ore-style blobs like vanilla `ore_granite_upper`/`ore_granite_lower`:
`minecraft:ore`, size 64, target `chronodawn:temporal_stone`. Placement:
count 2, `in_square`, height range from Y 0 to Y 128, `biome`. The
dimension spans Y -64 to 319. Starting at Y 0 avoids the deepslate layer,
where a `temporal_stone` target would silently match nothing. Add them
to Plains and Mountain only (both blobs to both biomes), in the
`underground_ores` step after the existing ores, in the same relative order
in both biomes so the global feature order stays consistent. Cliff faces cut
through these blobs and show patches of the new stones naturally.

### Surface rule copies

Every surface rule copy gets the same change: the `noise_settings/chronodawn.json`
in each of `common/1.20.1`, `1.21.1`, `1.21.2`, `1.21.4`, `1.21.5` to
`1.21.11`, `26.1.2`, `26.2`, and `common/26.3/.../material_rule/chronodawn.json`
(26.3 uses the flat `result_state` string form). Missing a copy fails
silently, so the plan should check the count (14 files) with grep.

### Mountain steep face mix

In the mountain steep branch (inside `stone_depth` floor, offset 0, above
water), replace the single `temporal_stone` block with a `sequence` of
`noise_threshold` conditions on `minecraft:surface`, falling back to
`temporal_stone`. Target shares on steep columns: Temporal Stone about 55%,
Temporal Cobblestone about 22%, Mossy Temporal Cobblestone about 13%,
Duskstone about 10%. The builder tunes thresholds to these shares. Duskstone
is the mountain's new stone, Dawnstone is the plains' (outcrops); both appear
underground in both biomes.


### Plains features

All three are added to `chronodawn_plains` only, in every biome JSON copy
(`common/1.20.1`, `common/1.21.1`, `common/1.21.2`, `common/shared-1.21.2+`,
`common/1.21.11`, `common/26.1.2`, `common/26.2`, `common/26.3`). The template
features go at the start of `vegetal_decoration`, before the trees, so they
never land on top of a trunk.

1. **Worn dirt paths (surface rule).** There is no plains branch yet. Add a
   new `biome_is: [chronodawn:chronodawn_plains]` condition right after the
   mountain branch, inside the same `stone_depth` floor offset 0 sequence, so
   Prairies and Faded Plains keep the default. Within it, a narrow band of a 2D noise replaces
   the top grass with `coarse_temporal_dirt`, and a narrower core of the band
   with `temporal_gravel` here and there. Narrow bands of smooth noise trace
   winding lines, which read as animal trails. Only above water and on
   non-steep ground. Pick a noise that is not `minecraft:surface` so paths do
   not line up with other surface patterns. A vanilla noise
   (`minecraft:surface_secondary`, `minecraft:patch` or
   `minecraft:gravel`) is fine. These exist in every supported version's
   vanilla noise registry. Verify the chosen key exists in 1.20.1 and 26.3
   before tuning. Target:
   a path crosses most plains areas a few chunks wide, covering roughly 3 to
   6% of plains surface columns.
2. **Low outcrops (NBT templates).** Three templates
   `dawnstone_outcrop_{a,b,c}` (4×4 to 6×5, 2 to 3 high) of Dawnstone with
   some Mossy Temporal Cobblestone and a Dawnstone Slab. The bottom layer
   replaces the ground surface (`y_offset` -1), the upper layers stick out one
   or two blocks. `min_ground_contact_ratio` 0.8. Rarity 18 each, about 1 in
   6 chunks combined.
3. **Ruined wall remnants (NBT templates).** Three templates
   `ruined_wall_{a,b,c}` (a straight 8-long run, an L corner, a 10-long run
   with a gap and a fallen block), 1 to 3 high, of Mossy Temporal
   Cobblestone, Temporal Cobblestone, Mossy Temporal Cobblestone Slab on the
   broken tops and a Dawnstone block. Wall blocks are avoided because the
   template is placed without neighbour updates, so their connection states
   would have to be authored by hand. `y_offset` 0,
   `min_ground_contact_ratio` 0.7. Rarity 72 each, about 1 in 24 chunks
   combined.

Both kinds use `chronodawn:nbt_template` with random rotation and a
`block_predicate_filter` for `temporal_grass_block` below, the same way
`time_cairn_placed.json` does. Templates live in
`common/shared-1.21.1+/.../structure/` (DataVersion 3955), and 1.20.1
converts them through the existing `nbtStructures` task. No air entries, no
loot, no entities. `StructureSpecs` guards their size and main blocks. The
generator script is not kept. Edit the `.nbt` files directly when changing
them.

### Textures

Original 16×16 placeholders generated by a script with seeded noise: a base
colour, speckles and mineral flecks, no vanilla source pixels. Stairs, slab
and wall reuse the base texture. Mark the textures as placeholders in the PR
body. Delete the drawing script after generating (owner preference from
earlier texture work).

Revised later: the first placeholders showed hard block borders when tiled,
looked flat, and Dawnstone read as brown like vanilla granite. They are now
drawn by `scripts/generate_dawn_dusk_stone_textures.py`, which the owner
chose to keep in the repository. Its noise fields and specks wrap around the
16×16 tile so neighbouring blocks continue each other, a height field shaded
from the upper left adds relief, and Dawnstone uses a rose-pink palette.
Duskstone keeps its cool blue-grey.

The same script draws `polished_dawnstone.png` and `polished_duskstone.png`
from the base palettes with less grain and fewer specks, plus a one-pixel
bevel. The bevel shows each block's border on purpose, as vanilla polished
stones do.

### Docs and changelog

- `CHANGELOG.md` Unreleased: new stones, mountain cliff mix, plains features.
- `docs/player_guide.md`: mention the new stones and where they appear if
  the guide lists terrain blocks.

## Verification

- `./gradlew validateResources validateData validateTranslations` (and
  `validateAll`).
- `./gradlew buildAll -Pbuildall_run_tests=true` and `gameTestAll` (the
  creative-tab completeness test and structure tests cover the new
  blocks/templates).
- `prodSmokeTest` for 1.21.1 and 26.3 on Fabric and NeoForge.
- New-world check on all versions where the implementation differs, at
  minimum 1.21.1 (Fabric and NeoForge) and 26.3, plus 1.20.1 Forge since its
  surface rule copy and NBT conversion differ.
- Rock-face evaluation over at least three seeds on 1.21.1 Fabric/NeoForge
  and 26.3: share of rock blocks on mountain surface columns, split into
  steep and non-steep, plus screenshots of grass/snow transitions. The owner
  has no exposure target yet, so the builder reports the numbers and
  screenshots and proposes a range. Starting point: rock on roughly 15 to 35%
  of mountain surface columns, almost none on flat summits.

## Results (2026-11-22)

Final thresholds, identical in all 14 surface rule copies:

- Mountain steep branch, `minecraft:surface`: Duskstone `|n| < 0.035`,
  Mossy Temporal Cobblestone `n < -0.32`, Temporal Cobblestone `n > 0.22`,
  otherwise Temporal Stone.
- Plains paths, `minecraft:surface_secondary`: Temporal Gravel
  `|n| < 0.007`, Coarse Temporal Dirt `|n| < 0.022`.

New-world scans on production servers (seeds 1111, 2222, 3333 on 1.21.1
Fabric, seed 1111 on 1.21.1 NeoForge and 26.3 Fabric). Each run
force-generated about 1,100 to 1,600 full chunks around two plains and two
mountain areas, then the region files were scanned for the top block below
`MOTION_BLOCKING_NO_LEAVES`. "Steep" here means a neighbour column differs by
3 or more blocks, which is close to but not the same as the surface rule's
`steep` condition.

| Measure | Seed 1111 | Seed 2222 | Seed 3333 |
|---|---|---|---|
| Plains: Coarse Temporal Dirt / Temporal Gravel | 3.5% / 2.8% | 3.3% / 2.0% | 3.1% / 2.4% |
| Mountain steep columns: Temporal Stone / Cobblestone / Mossy / Duskstone | 28.3 / 7.1 / 6.0 / 5.1% | 20.3 / 9.9 / 7.5 / 5.3% | 35.5 / 2.2 / 6.6 / 2.9% (695 columns only) |
| Mountain, all surface columns that are rock | about 4% | about 5% | about 2% |

- Paths: Temporal Gravel also comes from the existing gravel disks (about
  1.3% in the mountain, which has no paths), so the paths themselves cover
  about 5% of plains columns, inside the 3 to 6% target.
- Mountain mix: across seeds 1111 and 2222 the rock on steep columns splits
  about 55% Temporal Stone, 19% Temporal Cobblestone, 15% Mossy Temporal
  Cobblestone and 11% Duskstone, close to the 55/22/13/10 target.
- Rock exposure: about half of the steep columns show rock, and grass covers
  the rest. Flat mountain ground stays about 80% grass and 1% rock. Rock
  covers only 2 to 5% of all mountain surface columns because the Chrono
  Dawn mountain is mostly gentle terrain. That is well below the 15 to 35%
  starting point above. Raising it means changing which slopes count as
  steep, which belongs to the rock-face rule rather than the mix, so it is
  left for the owner's in-game judgement.
- Plains decorations: Dawnstone is the top block in about 0.4 to 0.5% of
  plains columns, which works out to roughly one outcrop per 15 plains
  chunks, fewer than the 1 in 6 aimed for. The grass filter and the 0.8
  ground-contact ratio reject many attempts. Lower the rarity values if they
  look too sparse in play.
- 1.21.1 NeoForge and 26.3 Fabric produced the same shares as 1.21.1 Fabric
  for the same seed. 1.20.1 Forge was not scanned, because the production
  server tooling does not support Forge before 50. Its surface rule copy is
  checked to be identical by the parity script, and the GameTests pass.

## Calls made by the builder (owner can override)

- Names Dawnstone / Duskstone (ドーンストーン / ダスクストーン), following the
  Clockstone transliteration convention.
- Underground blobs only in Plains and Mountain, not in all biomes.
- Duskstone on mountain cliffs, Dawnstone in plains outcrops.
- Shares and rarities above are starting values to tune in play.
