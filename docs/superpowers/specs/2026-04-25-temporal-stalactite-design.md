# Temporal Stalactite / Stalagmite — Design

Date: 2026-04-25
Status: Draft (pending user review)

## Goal

Add Temporal Stone-themed pointed blocks (stalactite / stalagmite) that
generate naturally in the caves of the ChronoDawn dimension. The blocks are
purely decorative — no falling, no growth, no impalement damage, no liquid
collection — and serve to add visual variety to the dimension's underground
spaces.

## Non-Goals

- Falling / impale damage when broken from above
- Growth from water drips, cauldron filling
- `thickness` BlockState that auto-updates from neighbors
- Generation in the vanilla overworld (Dripstone Caves stays untouched)
- Adding stalactite/stalagmite variants for slabs / stairs / walls / bricks etc.
- Waterlogging support

## Block Set

Four new blocks under namespace `chronodawn`:

| ID | Direction | Size | Description |
|---|---|---|---|
| `temporal_stalactite_tip` | ceiling-down | small | Narrow tip hanging from the ceiling |
| `temporal_stalactite_frustum` | ceiling-down | larger | Thicker root attached to the ceiling |
| `temporal_stalagmite_tip` | floor-up | small | Narrow tip rising from the floor |
| `temporal_stalagmite_frustum` | floor-up | larger | Thicker root attached to the floor |

### Naming Rationale

- `stalactite` / `stalagmite` make the orientation obvious to players,
  including those unfamiliar with vanilla `pointed_dripstone` thickness terms.
- `tip` and `frustum` reuse vanilla `pointed_dripstone` thickness vocabulary
  so the visual mapping (tip = small end, frustum = wider segment) is clear in
  English locale and unambiguous in code.

### Block Properties

- Material: stone (`SoundType.POINTED_DRIPSTONE` for placement / break sound to
  echo the visual cue; revisit if the audio clashes with Temporal Stone tone)
- Hardness / resistance: `1.5f / 6.0f` (matches `TemporalStoneBlock`)
- Tool: pickaxe required (`mineable/pickaxe`)
- Tier: stone or higher (`needs_stone_tool`)
- Collision: solid bounding box smaller than a full cube (see VoxelShape below)
- Light emission: none
- No waterlogging
- No falling-block behavior
- No `BlockEntity`

### VoxelShape

Each block uses a single static `getShape()` returning a tapered AABB that
visually matches the block's role:

- `temporal_stalactite_tip`: roughly `Block.box(5, 0, 5, 11, 11, 11)`
  (narrow, hangs in upper part of the cell)
- `temporal_stalactite_frustum`: roughly `Block.box(3, 0, 3, 13, 16, 13)`
  (full height, wider; attaches to ceiling above)
- `temporal_stalagmite_tip`: mirror of stalactite_tip along Y
- `temporal_stalagmite_frustum`: mirror of stalactite_frustum along Y

Exact pixel coordinates will be tuned during implementation against vanilla
`pointed_dripstone` for visual parity.

## Drop Behavior

| Block | Drops |
|---|---|
| `temporal_stalactite_tip` | itself |
| `temporal_stalactite_frustum` | `temporal_stalactite_tip` |
| `temporal_stalagmite_tip` | itself |
| `temporal_stalagmite_frustum` | `temporal_stalagmite_tip` |

Rationale: a `frustum` block placed standalone would look awkward (no tapered
end), so it is treated as worldgen-only. Players collecting cave decoration
get the visually self-contained `tip` block. This preserves the
"decorative only / minimum implementation" scope from Q1 of brainstorming
(no `thickness` BlockState, no neighbor-aware update logic) while avoiding
the ugly standalone-frustum case.

Trade-off accepted: players cannot rebuild a length-2 stalactite as it
appears in worldgen. They can still chain `tip` blocks side-by-side, matching
how vanilla `pointed_dripstone` decoration is typically used.

Silk Touch is **not** required for self-drop on tips, matching `TemporalStoneBlock`.

## Class Layout

Pattern: one Java class per block, mirroring the existing
`common/<version>/.../blocks/TemporalStone*.java` family.

```
common/<version>/src/main/java/com/chronodawn/blocks/
  TemporalStalactiteTipBlock.java
  TemporalStalactiteFrustumBlock.java
  TemporalStalagmiteTipBlock.java
  TemporalStalagmiteFrustumBlock.java
```

Each is a thin `Block` subclass that overrides `getShape()` (and
`getCollisionShape()` to match) with a hardcoded VoxelShape constant.

Total: 4 classes × 11 versions = 44 source files. The implementation is
near-identical across versions; only `Properties.setId(...)` (1.21.2+) and
constructor signature differences from
`feedback_drop_experience_block_ctor_order.md` and
`feedback_setid_shared_factory_pitfall.md` need per-version attention.

Block instances are added to `ModBlocks` (per version) at the end of the
Temporal Stone section, and to the Building Blocks creative tab at the end of
the Temporal Stone family.

## Generation Strategy

### Target Biomes

ChronoDawn dimension only — all biomes. Underground cave layers are shared
across the dimension regardless of surface biome, so visual variety should
not depend on which biome is overhead.

The dimension defines 10 biomes (1.20.1 through 1.21.10):

- `chronodawn:chronodawn_ancient_forest`
- `chronodawn:chronodawn_dark_forest`
- `chronodawn:chronodawn_desert`
- `chronodawn:chronodawn_forest`
- `chronodawn:chronodawn_mountain`
- `chronodawn:chronodawn_ocean`
- `chronodawn:chronodawn_plains`
- `chronodawn:chronodawn_prairies`
- `chronodawn:chronodawn_snowy`
- `chronodawn:chronodawn_swamp`

1.21.11 drops `chronodawn:chronodawn_prairies`, so its biome list contains 9
entries. All other biomes carry over.

Per `feedback_biome_ore_features_checklist.md`, missing a biome silently
breaks generation in chunks under that biome with no log output, so the
implementation plan must explicitly enumerate the full list per version
rather than relying on glob-style edits.

### Configured Features (in `common/shared/...`)

1. `temporal_stalactite_cluster.json` — places a `frustum` on the ceiling
   and a `tip` directly below it (length 2 from above).
2. `temporal_stalagmite_cluster.json` — symmetrical: `frustum` on the floor
   plus a `tip` above it (length 2 from below).
3. `temporal_pointed_simple.json` — random single tip on whichever surface
   is appropriate (length 1, ceiling or floor selected by environment scan).

### Placed Feature

A single `temporal_pointed.json` placed feature wraps the three configured
features via `random_select` with weights ~ length-2 : length-1 = 1 : 2.
Placement modifiers:

- `count` ≈ 8–12 attempts per chunk (final value tuned empirically)
- `in_square` for x/z jitter
- `height_range` covering the dimension's cave Y range (target Y=-32 to Y=80;
  exact range confirmed against ChronoDawn `noise_settings` during
  implementation)
- `environment_scan` to find a solid ceiling (for stalactite features) or
  floor (for stalagmite features)
- `block_predicate_filter` accepting only `chronodawn:temporal_stone` and
  `chronodawn:deepslate_temporal_stone` as the attachment surface, so the
  feature never spawns on alien blocks if biomes are reused elsewhere

Generation step: `UNDERGROUND_DECORATION` (matching vanilla
`pointed_dripstone`).

Density target: noticeably less dense than vanilla Dripstone Caves —
roughly 1–3 cluster instances per chunk on average. Concrete weights are an
implementation detail; this spec only fixes the order of magnitude so the
biome stays explorable rather than visually crowded.

## Resources (in `common/shared/...`)

### Models

- `assets/chronodawn/models/block/temporal_{stalactite,stalagmite}_{tip,frustum}.json`
  Use a `cross`-style parent (mirroring `pointed_dripstone`'s vanilla model
  family) with the appropriate texture reference.
- `assets/chronodawn/models/item/...` — references the block model.

### Blockstates

- One single-variant blockstate JSON per block, pointing to its model.
  No state machine; each direction/size combination is its own block.

### Textures

Four 16×16 PNGs derived from vanilla `pointed_dripstone_{up,down}_{tip,frustum}`
by recoloring to the Temporal Stone palette (slate-blue with subtle
time-crystal hue). Vanilla shape silhouette is preserved so players
immediately recognize the role.

### Translations

`assets/chronodawn/lang/en_us.json`:

- `block.chronodawn.temporal_stalactite_tip` = "Temporal Stalactite Tip"
- `block.chronodawn.temporal_stalactite_frustum` = "Temporal Stalactite Frustum"
- `block.chronodawn.temporal_stalagmite_tip` = "Temporal Stalagmite Tip"
- `block.chronodawn.temporal_stalagmite_frustum` = "Temporal Stalagmite Frustum"

`assets/chronodawn/lang/ja_jp.json`:

- `temporal_stalactite_tip` = "時の鍾乳石（先端）"
- `temporal_stalactite_frustum` = "時の鍾乳石（根元）"
- `temporal_stalagmite_tip` = "時の石筍（先端）"
- `temporal_stalagmite_frustum` = "時の石筍（根元）"

(Use the `Edit` tool with anchors per
`feedback_lang_json_line_based_edit.md` — `json.dumps` would strip the
section blank-lines.)

### Tags

- `data/minecraft/tags/blocks/mineable/pickaxe.json`: add the four blocks.
- `data/minecraft/tags/blocks/needs_stone_tool.json`: add the four blocks.

### Loot Tables

Per `feedback_loot_table_directory.md`:

- 1.20.1 path: `data/chronodawn/loot_tables/blocks/temporal_*.json`
- 1.21.1+ path: `data/chronodawn/loot_table/blocks/temporal_*.json`

Tip blocks: simple self-drop entry. Frustum blocks: drop the matching tip.
No silk-touch branch, so `feedback_loot_table_silk_touch_format.md` does
not apply this time.

### Worldgen JSON

Under `common/shared/...`:

- `data/chronodawn/worldgen/configured_feature/temporal_stalactite_cluster.json`
- `data/chronodawn/worldgen/configured_feature/temporal_stalagmite_cluster.json`
- `data/chronodawn/worldgen/configured_feature/temporal_pointed_simple.json`
- `data/chronodawn/worldgen/placed_feature/temporal_pointed.json`

### Biome JSON Updates

Add `chronodawn:temporal_pointed` to the `UNDERGROUND_DECORATION` features
list of every ChronoDawn biome, in every directory the project keeps biome
overrides:

- `common/1.20.1/.../worldgen/biome/chronodawn_*.json` — 10 biomes
- `common/1.21.1/.../worldgen/biome/chronodawn_*.json` — 10 biomes
- `common/1.21.2/.../worldgen/biome/chronodawn_*.json` — 10 biomes
  (per-version override; the shared dir below also exists and may duplicate
  these — implementation plan resolves which file is canonical)
- `common/shared-1.21.2+/.../worldgen/biome/chronodawn_*.json` — 10 biomes
  (covers 1.21.4 through 1.21.10)
- `common/1.21.11/.../worldgen/biome/chronodawn_*.json` — 9 biomes (per
  `feedback_1_21_11_biome_overrides.md` — 1.21.11 has its own biome JSON
  dir; `chronodawn_prairies` is absent in this version)

Total: 10 + 10 + 10 + 10 + 9 = 49 biome JSON files touched.

## Multi-Version Considerations

- 11 supported versions: 1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6,
  1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11. (1.21.3 reuses 1.21.2 modules
  per project convention.)
- Block class differences between versions are limited to
  `Properties.setId(...)` adoption from 1.21.2+ and any constructor-arg
  changes already handled by sibling `TemporalStone*` classes — copy from
  the same-version sibling rather than bulk-copying across versions.
- `feedback_check_all_before_phase_completion.md`: run `./gradlew checkAll`
  before declaring multi-version completion. Per-version smoke tests do
  not catch silent feature-step regressions in biome JSON.

## Known Pitfalls Cross-Checked

- `feedback_drop_experience_block_ctor_order.md` — N/A, blocks have no XP.
- `feedback_ofFullCopy_hardness_pitfall.md` — chain `.strength(1.5f, 6.0f)`
  explicitly even when copying from `TemporalStoneBlock` properties.
- `feedback_setid_shared_factory_pitfall.md` — give each block its own
  `setId(...)` call; do not share a `Properties` factory across the four
  blocks on 1.21.2+.
- `feedback_button_pressure_plate_signature_versions.md` — N/A.
- `feedback_humanoid_armor_1_21_5_api.md` — N/A.
- `feedback_recipe_json_format_versions.md` — no recipes added.
- `feedback_advancement_json_format_versions.md` — no advancements added.

## Verification

Once implemented:

1. `./gradlew validateResources` — JSON syntax / cross-reference.
2. `./gradlew validateTranslations` — both lang files have all four keys.
3. `./gradlew checkAll` — full multi-version verification.
4. Manual: enter ChronoDawn dimension, locate caves in each of the three
   biomes, confirm visual presence of stalactite/stalagmite formations,
   confirm break drops the correct items (tip self-drop, frustum drops tip),
   confirm Vanilla `pointed_dripstone` in vanilla Dripstone Caves is
   unchanged.

## Open Questions Deferred to Implementation

- Final density tuning (`count` value).
- Final Y range for placement (depends on ChronoDawn noise settings).
- Whether `SoundType.POINTED_DRIPSTONE` audio fits Temporal Stone tone, or
  whether `SoundType.STONE` reads better. Decide during in-game test pass.
- Texture palette specifics (which Temporal Stone shade to sample).
