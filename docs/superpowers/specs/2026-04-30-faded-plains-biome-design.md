# Faded Plains Biome — Design

Date: 2026-04-30
Status: Approved (pending implementation plan)
Theme: Time-worn wasteland — once-flourishing land withered by the stopping of time

---

## 1. Overview and Scope

Add a new biome to the Chrono Dawn dimension representing a desolate, withered landscape: yellowish-brown ground, sparse dead vegetation, dry cracked dirt patches, and bare snags from long-dead trees. The biome must feel "barren but not desert" — grass remains, but it is brittle and faded.

### Additions

- **Biome** `chronodawn:chronodawn_faded_plains`
  Climate niche: `temperature [0.5, 1.0]` × `humidity [0.0, 0.5]` (currently vacant; sits adjacent to `chronodawn_desert`).
- **Blocks** (3 new, all with fixed-color textures — no biome tint):
  - `FADED_TEMPORAL_GRASS` — short brittle grass cross plant.
  - `PARCHED_TEMPORAL_DIRT` — cracked dry dirt block.
  - `TEMPORAL_DEAD_BUSH` — dead bush analog.
- **Worldgen features** (4 new placed/configured features):
  - `patch_faded_grass`
  - `patch_temporal_dead_bush`
  - `disk_parched_temporal_dirt`
  - `dead_snag_placed` (1–3 vertical `STRIPPED_TIME_WOOD_LOG` columns, no foliage)

### Modifications

- `common/shared/.../dimension/chronodawn.json` — add the new biome to the multi_noise list.
- `common/shared/.../tags/worldgen/biome/has_master_clock.json` — include the new biome (consistency with all biomes).
- Per-version biome JSON in `common/1.20.1/`, `common/1.21.1/`, `common/1.21.2/`, `common/1.21.11/`, plus `common/shared-1.21.2+/`.
- Per-version `ModBlocks.java` (11 versions).
- Lang files (`en_us.json`, `ja_jp.json`).

### Out of scope

- New mobs (e.g., `withered_husk`).
- New custom structures (`withered_outpost` etc.).
- Advancements / chronicle entries dedicated to the biome (may be added later).
- Changes to existing biome climate parameters.

---

## 2. New Block Specifications

All three blocks use **fixed-color textures with no biome tint**. Rationale:

- They represent a "withered" state that should remain visually consistent regardless of which biome they are placed in (e.g., if the player carries them to a green biome).
- Avoids the cost of registering color providers in 11 × 2 (Fabric/NeoForge) client init files.
- Sidesteps the `tinted_cross` requirement (`feedback_tinted_cross_required`), which only matters for tintindex-bearing models.

### 2.1 `FADED_TEMPORAL_GRASS`

| Field | Value |
|---|---|
| Java class | New `FadedTemporalGrassBlock` (per version), extends `BushBlock`. Reference: `TemporalFernBlock` per-version layout (codec / setId / noCollision spelling differ per Minecraft version). |
| Survival surface | `TEMPORAL_DIRT`, `TEMPORAL_GRASS_BLOCK`, `COARSE_TEMPORAL_DIRT`, `PARCHED_TEMPORAL_DIRT` |
| Block model | `block/cross` (no tintindex) |
| Item model | `item/generated` parent (per `feedback_item_model_flat_icon_pattern`) |
| Texture | 16×16, baked yellow-brown (~`#B89C4A`), 3–4 short blades |
| Loot | Shears: drops self. Otherwise: nothing. |
| Tags | `minecraft:replaceable`, `minecraft:sword_efficient` |
| Sound | `SoundType.GRASS` |

### 2.2 `PARCHED_TEMPORAL_DIRT`

| Field | Value |
|---|---|
| Java class | None — register directly as `Block` (no behavioral override needed). |
| Properties base | `BlockBehaviour.Properties.ofFullCopy(ModBlocks.COARSE_TEMPORAL_DIRT.get())` then chain `.setId(...)` per version (avoid `feedback_setid_shared_factory_pitfall`). Strength explicit `.strength(0.5f)` (per `feedback_ofFullCopy_hardness_pitfall`). |
| Sound | `SoundType.GRAVEL` (or `ROOTED_DIRT` if available in 1.20.1) |
| Block model | `block/cube_all` |
| Texture | 16×16, baked-in cracked dry dirt (~`#8B6F35` cracks over the temporal_dirt base) |
| Loot | 1× self (silk touch not required for dirt). 1.20.1 vs 1.21.1+ predicate format split per `feedback_loot_table_silk_touch_format` does not apply (no silk branch). |
| Tags | `minecraft:mineable/shovel`, `minecraft:dirt` (per `feedback_dirt_tag_for_custom_terrain`) |
| Hoe interaction | None — does not till to `TEMPORAL_FARMLAND`. |

### 2.3 `TEMPORAL_DEAD_BUSH`

| Field | Value |
|---|---|
| Java class | New `TemporalDeadBushBlock` (per version), extends `BushBlock`. Reference: vanilla `DeadBushBlock` and per-version `TemporalFernBlock` for the API form. |
| Survival surface | `TEMPORAL_SAND`, `TEMPORAL_DIRT`, `COARSE_TEMPORAL_DIRT`, `PARCHED_TEMPORAL_DIRT`, `TEMPORAL_GRAVEL`, plus vanilla `Blocks.SAND` and `Blocks.RED_SAND` (so players can place the bush on vanilla sand outside the Chrono Dawn dimension). **Excludes** `TEMPORAL_GRASS_BLOCK` and vanilla grass (a dead bush on living grass is incongruent). Worldgen placement is constrained separately via the placed feature's `block_predicate_filter` to keep natural generation Chrono Dawn-only. |
| Block model | `block/cross` |
| Item model | `item/generated` |
| Texture | 16×16, baked-in `#6E5A35`-ish weathered branches |
| Loot | Shears: drops self. Otherwise: `minecraft:stick` × uniform[0,2] (vanilla parity). |
| Tags | `minecraft:replaceable`, `minecraft:sword_efficient` |
| Sound | `SoundType.GRASS` (vanilla parity) |

### 2.4 Cross-version API guards (applies to all three blocks)

- `noCollission` (1.20.1–1.21.8) vs `noCollision` (1.21.9+) — per `feedback_no_collision_spelling_split`.
- `Identifier` import (1.21.11) vs `ResourceLocation` (others) — per `feedback_resourcelocation_identifier_rename`.
- `MapCodec` / `codec()` override — only 1.21+; 1.20.1 must omit (per `feedback_mapcodec_1_21_only`).
- `BlockItem` `.setId(...)` chain — required from 1.21.2+ (per `feedback_bare_item_requires_setid`).
- Do not share a `BlockBehaviour.Properties` factory across blocks once `.setId(...)` is involved — `setId` also wires drops/descriptionId, so a shared factory aliases loot tables across variants (per `feedback_setid_shared_factory_pitfall`). Each version's `ModBlocks` should chain `.setId(...)` per registration.

---

## 3. Worldgen JSON

### 3.1 Biome JSON: `chronodawn_faded_plains.json`

Placement: `common/1.20.1/`, `common/1.21.1/`, `common/1.21.2/`, `common/1.21.11/`, plus `common/shared-1.21.2+/` — same fan-out as existing biomes (`feedback_1_21_11_biome_overrides`).

Key fields:

```jsonc
{
  "has_precipitation": false,
  "temperature": 1.0,
  "temperature_modifier": "none",
  "downfall": 0.1,
  "effects": {
    "sky_color": 9474192,        // dimension-shared
    "fog_color": 12632256,       // dimension-shared
    "water_color": 6983845,      // dimension-shared
    "water_fog_color": 3482025,  // dimension-shared
    "grass_color": 12953179,     // 0xC4A85B — warm yellow-brown
    "foliage_color": 12096325,   // 0xB89545 — slightly more amber
    "mood_sound": { /* same as prairies */ },
    "music": [ /* chronodawn:music.chronodawn_dimension, same as prairies */ ],
    "music_volume": 1.0
  }
}
```

`spawners`:

- `monster`: same set as `chronodawn_prairies`, with `epoch_husk` and `temporal_wraith` weights raised to `110` each (others unchanged) for a slightly heavier "wandering dead" feel.
- `creature`: only `chronodawn:timebound_rabbit` (weight 4, count 2–3) and `chronodawn:time_keeper` (weight 10, count 1–2). Cow / ticking_sheep / pulse_hog / secondwing_fowl are removed.
- `ambient`: `minecraft:bat` (parity with existing biomes).
- `water_creature` / `water_ambient` / `misc`: empty.

`features` (10-element array, same shape as other biomes):

- Layer 6 (underground ores): identical to other biomes — `ore_gold`, `ore_redstone`, `ore_time_crystal`, `ore_entropy_crystal`, `ore_temporal_amber`, `ore_clockstone`, `ore_coal`, `ore_iron`, `clockwork_block_cluster`. Required by `feedback_biome_ore_features_checklist` to avoid silent ore generation gaps.
- Layer 7 (underground decoration): `temporal_stalactite_cluster`, `temporal_stalagmite_cluster`, `temporal_pointed_single`.
- Layer 9 (surface decoration):
  ```
  chronodawn:patch_faded_grass
  chronodawn:patch_temporal_dead_bush
  chronodawn:patch_coarse_dirt
  chronodawn:disk_parched_temporal_dirt
  chronodawn:gravel_disk_placed
  chronodawn:boulder_placed
  chronodawn:fallen_log_placed
  chronodawn:dead_snag_placed
  chronodawn:lost_adventurer_memorial_placed
  chronodawn:time_cairn_placed
  chronodawn:old_sundial_placed
  ```
- Other layers: empty arrays.

Excluded from layer 9 (with reason):

- `fruit_of_time_tree` / `ancient_time_wood_tree` / `patch_grass*` / `patch_flowers` / `patch_time_wheat` / `patch_temporal_root` / `patch_chrono_melon` / `patch_tall_grass` / `patch_ferns` — too lush.
- `time_well_placed` — wet/lush feel.
- `freeze_top_layer` — biome has no precipitation, no snow.
- `watchmaker_camp_placed` — implies an active settlement.
- `hourglass_monolith_placed` — desert-iconic.
- `sand_disk_placed` — tips the biome toward desert.

`carvers`: empty.

### 3.2 Configured / Placed Features

Add under `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/` and `.../placed_feature/`:

| Feature ID | Type | Density (approx.) |
|---|---|---|
| `chronodawn:patch_faded_grass` | `minecraft:random_patch` + `simple_block(faded_temporal_grass)`, tries 64, radius 7, `block_predicate_filter` for valid surface blocks | ~10 placements/chunk |
| `chronodawn:patch_temporal_dead_bush` | `minecraft:random_patch` + `simple_block(temporal_dead_bush)`, tries 32, radius 5 | ~2–3 placements/chunk |
| `chronodawn:disk_parched_temporal_dirt` | `minecraft:disk` (radius 2–4), targets `TEMPORAL_DIRT`/`TEMPORAL_GRASS_BLOCK`/`COARSE_TEMPORAL_DIRT` | ~3–5 disks/chunk |
| `chronodawn:dead_snag_placed` | `minecraft:tree` configured feature: trunk = `STRIPPED_TIME_WOOD_LOG` height 3–5, foliage = `minecraft:no_op` if available else single `minecraft:simple_block` loop, no decorators | `count 1` + `chance 5` (≈1 per 5 chunks) |

If `minecraft:no_op` foliage placer is unavailable in 1.20.1, fall back to a manually composed `minecraft:tree` with empty foliage placement (verify during implementation). The `disk` feature schema differs between 1.20.1 and 1.21.1+ — match the existing `gravel_disk_placed` / `sand_disk_placed` per-version variants.

### 3.3 Surface rule

Existing surface rule structure (under `data/chronodawn/worldgen/noise_settings/chronodawn.json` or equivalent) governs how the surface is built per-biome. For `chronodawn_faded_plains`:

- Top: `TEMPORAL_GRASS_BLOCK` (yellow biome tint applied via `grass_color`).
- Subsoil (a few layers down): `TEMPORAL_DIRT`, with ~10% admixture of `COARSE_TEMPORAL_DIRT` and ~10% of `PARCHED_TEMPORAL_DIRT`.

Exact noise/rule encoding to be confirmed during implementation by reading the existing surface rule for `chronodawn_prairies`.

### 3.4 Dimension JSON entry

Append to `common/shared/.../dimension/chronodawn.json` `biome_source.biomes`:

```json
{
  "biome": "chronodawn:chronodawn_faded_plains",
  "parameters": {
    "temperature": [0.5, 1.0],
    "humidity": [0.0, 0.5],
    "continentalness": [0.03, 1.0],
    "erosion": [0.0, 1.0],
    "depth": 0,
    "weirdness": [-1.0, 1.0],
    "offset": 0.0
  }
}
```

### 3.5 Biome tag

Append `chronodawn:chronodawn_faded_plains` to `data/chronodawn/tags/worldgen/biome/has_master_clock.json` (consistency with all other biomes).

---

## 4. Cross-Version Distribution and Risks

### Per-version files

| File set | Versions | Notes |
|---|---|---|
| `ModBlocks.java` (registration of 3 new blocks) | 11 (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11) | Per-version API guards listed in §2.4 |
| `FadedTemporalGrassBlock.java` | 11 | Mirror per-version layout of `TemporalFernBlock` |
| `TemporalDeadBushBlock.java` | 11 | Same |
| Biome JSON | 4 (1.20.1, 1.21.1, 1.21.2, 1.21.11) + `shared-1.21.2+` | Per `feedback_1_21_11_biome_overrides` |
| Loot table JSON | 1.20.1 in `loot_tables/` (plural), 1.21.1+ in `loot_table/` (singular) | Per `feedback_loot_table_directory` |
| Item model JSON | 1.21.4+ requires `items/<id>.json` | Per `feedback_spawn_egg_client_items` |

### Shared resources (single set)

- Block models, blockstates, textures, item model parents — `common/shared/.../assets/`.
- Lang files — `common/shared/.../assets/chronodawn/lang/{en_us,ja_jp}.json`. Edit with anchored `Edit` calls (per `feedback_lang_json_line_based_edit`).
- Configured/placed features — `common/shared/.../data/chronodawn/worldgen/`.
- Block tags — single-form (`tags/block/...`) for 1.21.1+, plural for 1.20.1.
- Dimension JSON, biome master_clock tag — `common/shared/`.

### Risk register

1. **`disk` JSON schema 1.20.1 vs 1.21.1+** — match existing `gravel_disk_placed` / `sand_disk_placed` per-version variants.
2. **`minecraft:no_op` foliage placer availability** — verify in 1.20.1 during implementation; fall back to manual composition if absent.
3. **`canSurvive` enforcement vs feature placement** — `TEMPORAL_DEAD_BUSH` must not be placed on `TEMPORAL_GRASS_BLOCK`. Use `block_predicate_filter` in placed feature, not just `canSurvive`, to avoid silent placement failures.
4. **Layer 6 (ores) must be populated** — never empty (`feedback_biome_ore_features_checklist`).
5. **`buildAll` / `gameTestAll` wrapper unreliability** — verification uses per-version build (`feedback_buildall_gametestall_wrapper_unreliable`).
6. **Color provider must NOT be registered for the new blocks** — fixed-color textures depend on no tint being applied.
7. **`PARCHED_TEMPORAL_DIRT` should not be tilled** — verify it is not registered in any hoe-tilling map. Since it does not extend `TemporalDirtBlock` (registered as plain `Block`), no override needed.

### Implementation phases

- **Phase A** — register all three blocks in every version-specific module (Java + assets + loot + tags + lang). Verify with per-version build for each of the 11 versions.
- **Phase B** — add shared worldgen content: dimension JSON entry, biome tag entry, biome JSONs (per version), configured/placed feature JSONs. Run `validateResources` and `validateTranslations`.
- **Phase C** — add 3 GameTests (see §5). Run per-version `runGameTest`.

---

## 5. Verification Strategy

### Build

- Per Phase A: `./gradlew build1_20_1` … `build1_21_11` — sequential per-version. Skip `buildAll` (unreliable wrapper).

### Resource validation

- `./gradlew validateResources` — JSON syntax + blockstate/model/texture references.
- `./gradlew validateTranslations` — `block.chronodawn.faded_temporal_grass`, `block.chronodawn.parched_temporal_dirt`, `block.chronodawn.temporal_dead_bush` and `biome.chronodawn.chronodawn_faded_plains` resolve in en_us and ja_jp.

### Unit tests

- Block registration tests for the 3 new blocks (mirror existing test pattern).

### GameTest

Add `gametest/src/main/java/com/chronodawn/gametest/biome/FadedPlainsTest.java`:

1. `dead_bush_canSurvive_onParchedDirt` — place `TEMPORAL_DEAD_BUSH` on `PARCHED_TEMPORAL_DIRT`, tick, assert still present.
2. `dead_bush_breaks_onGrassBlock` — place `TEMPORAL_DEAD_BUSH` on `TEMPORAL_GRASS_BLOCK`, tick, assert removed.
3. `faded_grass_shears_drops_self` — break `FADED_TEMPORAL_GRASS` with shears, assert single self-drop.

### Manual verification

- `./gradlew runClientFabric1_21_11`, `/locate biome chronodawn:chronodawn_faded_plains`, `/tp` and visually confirm yellow ground tint, `PARCHED_TEMPORAL_DIRT` patches, dead bushes, snags.
- Repeat with `runClientFabric1_20_1` (largest API delta).
- Inspect inventory icons for the three new blocks (per `feedback_spawn_egg_client_items`).

### Documentation updates

- `docs/player_guide.md` — short biome description.
- `docs/curseforge_description.md`, `docs/modrinth_description.md` — short biome description.
- `CHANGELOG.md` — appended only after user instruction (per CLAUDE.md commit policy).

### Completion criteria

- Per-version build green for all 11 versions.
- `validateResources` and `validateTranslations` clean.
- 3 new GameTests pass on at least one version (1.21.11) and 1.20.1 manual smoke test passes.
- Player-facing docs updated under `docs/`.

---

## 6. Open Questions Deferred to Implementation

- Exact texture artwork (target colors specified; final pixels finalized at implementation time).
- Whether `minecraft:no_op` foliage placer exists in 1.20.1 (verify when authoring `dead_snag_placed`).
- Exact density tuning for placed features (initial values in §3.2 may need adjustment after manual playtest).
- Whether the surface rule mixes `PARCHED_TEMPORAL_DIRT` directly into the surface rule or relies entirely on `disk_parched_temporal_dirt` for visible cracks (decide by reading the prairies surface rule).
