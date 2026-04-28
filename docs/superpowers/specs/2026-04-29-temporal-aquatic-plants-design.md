# Temporal Aquatic Plants — Design

Date: 2026-04-29
Scope: ChronoDawn dimension underwater vegetation overhaul (`chronodawn_ocean`, `chronodawn_swamp`)

## Background

ChronoDawn currently customizes most of its dimension's surface vegetation (Time Wood saplings, Temporal Tall Grass, Temporal Fern, Time Blossom, Dawn/Dusk Bell). However, the underwater portion of `chronodawn_ocean` (and the water cells in `chronodawn_swamp`) still spawns vanilla `minecraft:kelp`, `minecraft:seagrass`, and `minecraft:tall_seagrass` because their referencing configured features in ChronoDawn's worldgen JSON simply forward to the vanilla `minecraft:kelp` and `minecraft:seagrass` feature types, which hardcode block placement.

This breaks the worldview: a player descending into ChronoDawn's ocean encounters bright green vanilla seaweed alongside Temporal sand, Temporal stone variants, and the dimension's purple/teal palette. Vanilla `sea_pickle` is not currently spawned in ChronoDawn (no placed feature reference exists in the dimension's biome JSON), but the underwater scene is bare of intentional light sources or accent decoration as a result.

## Goal

Replace ChronoDawn's underwater vegetation with a cohesive set of custom aquatic plants that fit the "frozen time" worldview, while introducing new categories (light source, multi-color coral, decorative drift / hourglass / frozen bubble forms) that go beyond a 1:1 vanilla mirror. The replacement happens only inside the ChronoDawn dimension; vanilla overworld oceans are not affected.

The aesthetic is intentionally mixed across three sub-themes:

- **Frozen Time** (Temporal Kelp, Temporal Seagrass, Frozen Bubble Coral): pale cyan/white, crystalline.
- **Bioluminescent** (Lumen Polyp): purple/blue cold-light, the dedicated underwater light source role.
- **Temporal Drift** (Drift Frond, Hourglass Bulb, Chrono Coral): translucent magenta or Time Wood-themed (Dawn/Dusk/Twilight/Eternal) palette echoing the existing land plants.

## Non-Goals

- Affecting vanilla overworld oceans (biome modifier on `minecraft:ocean*` family is out of scope).
- Adding new aquatic mobs, structures, or dungeons (only blocks and a single derived food item).
- Gameplay effects on Hourglass Bulb and Frozen Bubble Coral — both are decorative-only this round; gameplay extensions are explicitly future work.
- Compressed-block forms and fuel mechanics for Dried Temporal Kelp (Phase-future, additive).
- Removing or rewriting vanilla aquatic plants already present in player worlds (only newly generated chunks switch to the new plants; existing chunks keep what they have).
- Custom particle registration for Hourglass Bulb (vanilla `falling_dust` is reused).
- New `MobEffect` for any plant.
- Tall Drift Frond variant (single-block-tall only).

## Approach

Selected approach: **JSON-only configured-feature substitution mirroring the existing Temporal Tall Grass / Temporal Fern replacement pattern (commit `04c432da`).**

The existing `chronodawn:kelp` and `chronodawn:seagrass` configured features that today wrap the hardcoded vanilla `minecraft:kelp` / `minecraft:seagrass` feature types are rewritten to use vanilla generic feature types (`minecraft:block_column`, `minecraft:simple_block`, `minecraft:random_patch`) configured to place ChronoDawn blocks. The `chronodawn:kelp` / `chronodawn:seagrass` placed-feature wrappers and biome references are unchanged in placement logic — only the configured feature payload changes.

Three approaches were considered:

1. **JSON-only with vanilla generic feature types** *(selected)* — minimal code, parallels grass replacement, fits the project's JSON-first worldgen convention. Tradeoff: kelp's per-tick growth quirks reproduce only as far as `minecraft:block_column` allows; for worldgen-time placement this is sufficient.
2. **Custom `Feature<T>` Java class** — perfect kelp-fidelity at the cost of a new class plus per-version registration and signature drift risk. Rejected: the only kelp behavior the player perceives at worldgen time is "a column of variable height," which `block_column` covers.
3. **Mixin / tag-based block substitution at placement time** — rejected: invasive, dual-loader implementations required, debugging unfriendly, contradicts the "JSON-first" pattern.

## Plant Inventory

Seven blocks plus one consumed-by-eating item, organized by Phase.

### Phase 1 — Vanilla replacement, minimum healthy state

| Block / Item | Replaces | Aesthetic | Mechanics |
|---|---|---|---|
| `temporal_kelp` (head) + `temporal_kelp_plant` (stem) | `minecraft:kelp` | Frozen Time | KelpBlock-like, growable with bone meal, harvestable to drop the kelp item; sheared drops the block |
| `temporal_seagrass` | `minecraft:seagrass` | Frozen Time | SeagrassBlock-like; sheared drops |
| `tall_temporal_seagrass` (lower + upper) | `minecraft:tall_seagrass` | Frozen Time | DoublePlantBlock-like; sheared drops 2; only lower half is the "active" entry |
| `dried_temporal_kelp` (item only) | — | Frozen Time | Smoking output of `temporal_kelp`. Food: nutrition 1, saturation 0.6 (parity with vanilla dried kelp) |

Cow / turtle vanilla food tag handling: the new `temporal_seagrass` is added to the turtle food tag (`minecraft:turtle_food` or per-version equivalent). Cow handling is not addressed because cows do not spawn in ChronoDawn.

### Phase 2 — Light and color

| Block | Aesthetic | Mechanics |
|---|---|---|
| `lumen_polyp` (count 1–4) | Bioluminescent | SeaPickleBlock-like — light level 6/12/15 underwater, 9 max above water, bone-meal grows count; fully vanilla-equivalent |
| `dawn_chrono_coral_block` / `dusk_*` / `twilight_*` / `eternal_*` (4 colors) | Mixed (Time Wood palette) | Vanilla coral block parity — but **no dying behavior out of water** (frozen-time fiction) |
| `dawn_chrono_coral` / `dusk_*` / `twilight_*` / `eternal_*` (plant form, 4 colors) | Same | Vanilla coral plant parity except no death |
| `dawn_chrono_coral_fan` / etc. (4 colors) | Same | Vanilla coral fan parity except no death |
| `dawn_chrono_coral_wall_fan` / etc. (4 colors) | Same | Vanilla wall coral fan parity except no death |

That is 1 + 16 = 17 new Phase 2 blocks. Chrono Coral does not emit light (Lumen Polyp owns that role).

### Phase 3 — Decorative finishing

| Block | Aesthetic | Mechanics |
|---|---|---|
| `drift_frond` | Temporal Drift | Single-tall, SeagrassBlock-like behavior. Sheared-only drop |
| `hourglass_bulb` | Temporal Drift | Decorative single block. Periodic `falling_dust` particles via `Block#animateTick` (random 20–40 tick interval). No effect on entities |
| `frozen_bubble_coral` | Frozen Time | Decorative single block resembling a frozen bubble sculpture. **No bubble column**. Silk-touch-only drop |

### Naming summary

- Vanilla-replacement plants use the `temporal_` prefix (`temporal_kelp`, `temporal_seagrass`, etc.) consistent with Temporal Tall Grass and Temporal Fern.
- Net-new categories use thematic names (`lumen_polyp`, `chrono_coral`, `drift_frond`, `hourglass_bulb`, `frozen_bubble_coral`) consistent with prior precedent (Time Blossom, Dawn Bell, Dusk Bell are similarly themed rather than `temporal_*`).
- Chrono Coral colors echo Time Wood (`dawn_`, `dusk_`, `twilight_`, `eternal_`); the fifth Time Wood prefix `ancient_` is intentionally reserved (so coral reads as a "subset family" rather than a full mirror).

## Worldgen Replacement Strategy

### Existing files to overwrite

- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/kelp.json`
  - From: `{"type": "minecraft:kelp", "config": {}}`
  - To: `{"type": "minecraft:block_column", ...}` configuration that places `chronodawn:temporal_kelp_plant` for the variable-height stem layer and `chronodawn:temporal_kelp` for the single-block tip layer, matching vanilla kelp's height distribution as closely as `block_column` allows.
- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/seagrass.json`
  - From: `{"type": "minecraft:seagrass", "config": {"probability": 0.4}}`
  - To: a `minecraft:simple_block` or `minecraft:random_patch` configuration that places `chronodawn:temporal_seagrass`. Tall variant placement is covered separately (see below).

### New configured / placed features (additive)

Phase 1:

- A configured + placed feature pair to spawn `tall_temporal_seagrass` as a 2-block column, integrated either by extending the existing `chronodawn:seagrass` configured feature with a probabilistic tall layer or by adding a sibling feature referenced from `chronodawn_ocean.json`. Decision deferred to plan time after inspecting how vanilla `seagrass_simple` separates its tall variant.

Phase 2:

- `chronodawn:lumen_polyp` configured + placed feature (low count, ocean floor only). New entry in `chronodawn_ocean.json` features array.
- `chronodawn:chrono_coral_patch` configured + placed feature using `minecraft:simple_random_selector` to pick across the 16 Chrono Coral block forms. New entry in `chronodawn_ocean.json`.

Phase 3:

- `chronodawn:drift_frond`, `chronodawn:hourglass_bulb`, `chronodawn:frozen_bubble_coral` configured + placed features. New entries in `chronodawn_ocean.json`.

### Biome JSON updates

- `chronodawn_ocean.json` already references `chronodawn:kelp` and `chronodawn:seagrass` placed features (line 196–197 in the 1.21.2+ shared variant); no change needed for Phase 1.
- `chronodawn_swamp.json` already references `chronodawn:seagrass` (line 150); no change for Phase 1.
- Each Phase 2 / 3 added feature requires inserting one line into `chronodawn_ocean.json`'s features array.
- 1.21.11 maintains its own biome JSON directory (per the project's MEMORY entry on `1.21.11 biome overrides`); the same edits must be applied there in parallel.

### Effect on existing player worlds

Configured features run at chunk generation only. Already-generated chunks keep whatever vanilla aquatic blocks are already placed; only newly generated chunks see the new plants. This is intentional — equivalent to how the Temporal Grass replacement landed.

The CHANGELOG and player guide for each Phase note this explicitly to manage expectations.

## File Layout (Phase 1 illustrative)

| Type | Path | Versioning |
|---|---|---|
| Block / Item Java | `common/shared/src/main/java/com/github/ksoichiro/chronodawn/block/Temporal*.java` | Shared (era-checked at compile time per existing pattern) |
| Registration entries | `common/shared/.../core/ModBlocks.java`, `ModItems.java` | Shared |
| Blockstate / model JSON | `common/shared/src/main/resources/assets/chronodawn/{blockstates,models}/` | Shared |
| Texture PNG | `common/shared/src/main/resources/assets/chronodawn/textures/{block,item}/` | Shared |
| Loot table (1.21.1+) | `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/` | Singular `loot_table/` |
| Loot table (1.20.1) | `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/` | Plural `loot_tables/` |
| Recipe (1.20.1) | `common/1.20.1/.../recipes/` | Plural, 1.20.1 schema |
| Recipe (1.21.1) | `common/1.21.1/.../recipe/` | Singular, 1.21.1 schema |
| Recipe (1.21.2+) | `common/shared-1.21.2+/.../recipe/` | Singular, 1.21.2+ schema |
| Configured feature | `common/shared/.../worldgen/configured_feature/` | Shared overwrite |
| Client items JSON (1.21.4) | `common/1.21.4/src/main/resources/assets/chronodawn/items/` | Per-era |
| Client items JSON (1.21.5+) | `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/` | Per-era |
| en_us | `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json` | Shared |

## Multi-Version Considerations

The project supports 1.20.1 / 1.21.1 / 1.21.2 / 1.21.4 / 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 / 1.21.9 / 1.21.10 / 1.21.11. API era boundaries that affect this work:

- **`Item.Properties().setId()` is required from 1.21.2+** — every new BlockItem and the Dried Temporal Kelp item must chain `.setId(...)`. Failing to do so compiles but NPEs at runtime.
- **Client Items JSON is required from 1.21.4+** — every new item (block items, the dried kelp item, coral fan items, etc.) needs a JSON in `assets/chronodawn/items/<id>.json`, both in `common/1.21.4/` and `common/shared-1.21.5+/`. Missing entries silently render as the purple-black fallback.
- **Loot table directory is `loot_table/` (singular) from 1.21.1+** — 1.20.1 uses `loot_tables/` (plural). Both must be authored separately.
- **Recipe JSON format splits across three eras** (1.20.1 / 1.21.1 / 1.21.2+): `result.item` vs `result.id`, ingredient object vs string. The smoking recipe for Dried Temporal Kelp is authored in three forms.
- **Per-version biome JSON in 1.21.11** — every Phase that edits `chronodawn_ocean.json` must apply the same edit to the 1.21.11-specific biome directory.
- **`KelpBlock`, `SeagrassBlock`, `DoublePlantBlock`, `SeaPickleBlock`, `CoralBlock`, `CoralPlantBlock`, `CoralFanBlock`, `CoralWallFanBlock` constructor signatures** — assumed stable across the 1.20.1 → 1.21.11 range, but each must be sanity-checked at Phase implementation time. Reference per-era usage of similar blocks already in the project (no exact equivalents exist for sea pickle / coral, so vanilla 1.20.1 / 1.21.X source must be inspected).

The project's `MEMORY.md` records additional pitfalls referenced during plan and implementation phases (`Bare new Item requires setId`, `Client Items JSON required for ALL items in 1.21.4+`, `Recipe JSON format splits by version`, `1.21.11 biome overrides`, `Lang JSON line-based edit`, etc.).

## Phase Plan

Phases are independent and may be released as separate minor versions. Each Phase ends with `./gradlew checkAll` passing across all 11 supported versions. Each Phase ships its own commit/PR/release.

### Phase 1 — Replacement of vanilla aquatic plants

- Adds 4 blocks (kelp head + kelp plant + seagrass + tall seagrass), 1 dedicated item (dried temporal kelp), 1 smoking recipe.
- Rewrites 2 existing configured features.
- Approximate file count: 30–45.
- Player-visible result: ChronoDawn ocean and swamp generate Temporal Kelp / Temporal Seagrass / Tall Temporal Seagrass instead of vanilla; Temporal Kelp can be smoked into a food.

### Phase 2 — Light and color

- Adds 17 blocks (1 Lumen Polyp + 16 Chrono Coral).
- Adds 2 configured + placed feature pairs and 2 biome feature references.
- Approximate file count: 80–100. Texture creation can leverage hue-shift scripting on the 16 vanilla coral textures.
- Player-visible result: ChronoDawn ocean has subtle light sources and color accents.

### Phase 3 — Decorative finishing

- Adds 3 blocks (Drift Frond, Hourglass Bulb, Frozen Bubble Coral).
- Adds 3 configured + placed feature pairs and 3 biome feature references.
- Approximate file count: 20–30.
- Player-visible result: rare decorative variety.

### Versioning

Per ChronoDawn's `versioning` skill, each Phase is a MINOR version bump (additive features). Patch versions are reserved for bug fixes within a Phase.

## Testing Plan

Each Phase passes the full check suite before merge:

| Test | Coverage |
|---|---|
| `./gradlew validateResources` | JSON syntax + cross-references (blockstate→model, model→texture) |
| `./gradlew validateTranslations` | en_us key parity across versions |
| `./gradlew buildAll` | Build all 11 versions for release |
| `./gradlew testAll` | Unit tests across 11 versions |
| `./gradlew gameTestAll` | GameTests including 1.21.3 runtime verification |
| Manual `runClient` per loader/version | Spot-check ChronoDawn ocean generation, particle behavior, light source, smoking, shears |

GameTest scope per Phase:

- **Phase 1**: Temporal Kelp bone-meal growth, Dried Temporal Kelp smoking, Tall Temporal Seagrass shears drops.
- **Phase 2**: Lumen Polyp count-dependent light level, bone-meal count increase, Chrono Coral shears drop, Chrono Coral persistence out of water.
- **Phase 3**: Basic placement smoke tests; particle behavior for Hourglass Bulb is verified manually (no GameTest harness for client-side particles).

## Documentation Touchpoints per Phase

- `CHANGELOG.md` — Added section listing the new blocks / items.
- `docs/player_guide.md` — new Aquatic Plants subsection (or extension thereof).
- `docs/curseforge_description.md` and `docs/modrinth_description.md` — feature list update.
- `docs/developer_guide.md` — only if a Phase adds a reusable pattern (e.g. Phase 2's hue-shift coral scripting is worth documenting if pursued).

## Open Items Deferred to Plan Phase

These do not block design approval but must be resolved when writing the implementation plan:

- Exact `minecraft:block_column` schema for the rewritten kelp configured feature (validate per MC version; vanilla `huge_dripleaf` is a reference).
- Whether tall seagrass spawns via an extension to the existing `chronodawn:seagrass` configured feature or via a new sibling feature referenced from `chronodawn_ocean.json`.
- Whether the 1.21.11 dimension type changes (per MEMORY entry on `Dimension type JSON change in 1.21.11`) interact with biome features (currently believed to be independent — verify).
- Final per-version Mixin requirements (none anticipated; vanilla extension blocks are sufficient).
- Whether texture hue-shift for the 16 Chrono Coral textures is automated via a one-shot script (committed alongside generated PNGs) or hand-painted variant by variant.
