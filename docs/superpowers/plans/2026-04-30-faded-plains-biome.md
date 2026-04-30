# Faded Plains Biome Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `chronodawn:chronodawn_faded_plains` biome — a time-worn, yellow-grass wasteland — to the Chrono Dawn dimension, along with three new fixed-color blocks (`FADED_TEMPORAL_GRASS`, `PARCHED_TEMPORAL_DIRT`, `TEMPORAL_DEAD_BUSH`) and four new worldgen features.

**Architecture:** Multi-version Architectury mod (Fabric + NeoForge × 11 Minecraft versions). New blocks register through per-version `ModBlocks.java` / `ModItems.java`, share a single set of assets/textures under `common/shared/`, and split loot tables and tags between `1.20.1` (plural directory names) and `shared-1.21.1+` (singular). The new biome is wired into the dimension via the existing multi_noise generator. Surface uses the default rule (no `noise_settings` edits needed) — yellowness comes from biome `grass_color`/`foliage_color` tinting `TEMPORAL_GRASS_BLOCK`.

**Tech Stack:** Java 21, Architectury Loom, Mojang mappings, Groovy DSL Gradle, Shadow plugin. Memory references throughout document version pitfalls (e.g. `feedback_loot_table_directory`, `feedback_no_collision_spelling_split`, `feedback_resourcelocation_identifier_rename`).

**Spec:** `docs/superpowers/specs/2026-04-30-faded-plains-biome-design.md`

---

## File Structure

### Created files (counts in parentheses)

- `common/shared/src/main/resources/assets/chronodawn/textures/block/` — 3 PNGs (`faded_temporal_grass.png`, `parched_temporal_dirt.png`, `temporal_dead_bush.png`)
- `common/shared/src/main/resources/assets/chronodawn/blockstates/` — 3 JSONs
- `common/shared/src/main/resources/assets/chronodawn/models/block/` — 3 JSONs
- `common/shared/src/main/resources/assets/chronodawn/models/item/` — 3 JSONs
- `common/1.21.4/src/main/resources/assets/chronodawn/items/` — 3 JSONs (1.21.4 only)
- `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/` — 3 JSONs (1.21.5+)
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/` — 3 JSONs
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/` — 3 JSONs
- `common/<each-of-11>/src/main/java/com/chronodawn/blocks/FadedTemporalGrassBlock.java` — 11 files
- `common/<each-of-11>/src/main/java/com/chronodawn/blocks/TemporalDeadBushBlock.java` — 11 files
- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/` — 5 new JSONs (`faded_grass_block`, `temporal_dead_bush_block`, `dead_snag`, `patch_faded_grass`, `patch_temporal_dead_bush`)
- `common/1.20.1/src/main/resources/data/chronodawn/worldgen/configured_feature/parched_temporal_dirt_disk.json` — 1 JSON (1.20.1 nested format)
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/parched_temporal_dirt_disk.json` — 1 JSON (1.21.1+ flat format)
- `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/` — 4 placed feature JSONs
- `common/{1.20.1,1.21.1,1.21.2,1.21.11}/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_faded_plains.json` — 4 per-version biome JSONs
- `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_faded_plains.json` — 1 shared biome JSON
- `gametest/src/main/java/com/chronodawn/gametest/biome/FadedPlainsTest.java` — 1 file
- `gametest/src/main/resources/data/chronodawn/gametest/structure/faded_plains_*.snbt` — up to 3 SNBT structures (one per gametest)

### Modified files

- `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`
- `common/<each-of-11>/src/main/java/com/chronodawn/registry/ModBlocks.java` — 11 files
- `common/<each-of-11>/src/main/java/com/chronodawn/registry/ModItems.java` — 11 files
- `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/dirt.json`
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/dirt.json`
- `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/shovel.json`
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/shovel.json`
- `common/shared/src/main/resources/data/chronodawn/dimension/chronodawn.json`
- `common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_master_clock.json`
- `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`
- `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- `docs/player_guide.md`
- `docs/curseforge_description.md`
- `docs/modrinth_description.md`

### Reference files (read-only context)

- `common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_fern.json` — blockstate template
- `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_fern.json` — block model template (note: temporal_fern uses `tinted_cross`; faded_grass uses plain `block/cross`)
- `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_fern.json` — item model template
- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/patch_grass.json` — random_patch template
- `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/grass_block.json` — simple_block template
- `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/patch_grass.json` — placed feature template
- `common/{1.20.1,1.21.1,1.21.2,1.21.11}/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_prairies.json` — biome template for each per-version directory
- `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_prairies.json` — shared biome template
- `common/<version>/src/main/java/com/chronodawn/blocks/TemporalFernBlock.java` — Java class template per version (mirror for new bush blocks)

---

## Phase 0: ID Enums (Foundation)

### Task 0.1: Add three new entries to `ModBlockId`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`

- [ ] **Step 1: Add the new IDs after `TEMPORAL_FERN`**

Locate the section starting at line 195:

```java
    TEMPORAL_TALL_GRASS(def("temporal_tall_grass").cutout()),
    TEMPORAL_FERN(def("temporal_fern").cutout()),
```

Add three new entries immediately after `TEMPORAL_FERN`:

```java
    TEMPORAL_TALL_GRASS(def("temporal_tall_grass").cutout()),
    TEMPORAL_FERN(def("temporal_fern").cutout()),
    FADED_TEMPORAL_GRASS(def("faded_temporal_grass").cutout()),
    PARCHED_TEMPORAL_DIRT(def("parched_temporal_dirt")),
    TEMPORAL_DEAD_BUSH(def("temporal_dead_bush").cutout()),
```

`PARCHED_TEMPORAL_DIRT` does not get `.cutout()` because it's a fully opaque block. The cross plants do.

- [ ] **Step 2: Verify the file still compiles by inspecting it**

Read the modified file and confirm the three new lines parse as valid Java enum entries (commas, parentheses balanced). No commit yet — pair with Task 0.2.

### Task 0.2: Add three new entries to `ModItemId`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Locate the corresponding plant section in `ModItemId.java`**

Use `git grep -n 'TEMPORAL_FERN' common/shared/src/main/java/com/chronodawn/registry/ModItemId.java` to find the line. The structure mirrors `ModBlockId`.

- [ ] **Step 2: Add the three new entries after the matching `TEMPORAL_FERN`**

```java
    TEMPORAL_TALL_GRASS(def("temporal_tall_grass")),
    TEMPORAL_FERN(def("temporal_fern")),
    FADED_TEMPORAL_GRASS(def("faded_temporal_grass")),
    PARCHED_TEMPORAL_DIRT(def("parched_temporal_dirt")),
    TEMPORAL_DEAD_BUSH(def("temporal_dead_bush")),
```

(`ModItemId.def()` typically does not chain `.cutout()` — confirm by inspecting nearby entries.)

- [ ] **Step 3: Commit Task 0.1 and 0.2 together**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java \
        common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat(blocks): register IDs for faded_temporal_grass, parched_temporal_dirt, temporal_dead_bush"
```

---

## Phase 1: PARCHED_TEMPORAL_DIRT (Simplest Block)

This block has no behavioral override — registered as a plain `Block`. Only resources, registration entries, tags, lang, and loot are needed.

### Task 1.1: Create the texture

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/parched_temporal_dirt.png`

- [ ] **Step 1: Produce the 16×16 texture**

Base: copy `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_dirt.png`. Apply cracks in `#8B6F35` overlay (darker brown, 1–2 pixel-wide irregular lines) covering ~20% of the surface. Save as `parched_temporal_dirt.png`. No `.mcmeta` needed.

The skill agent should generate or hand-paint this asset; if generation isn't possible in this environment, leave a placeholder PNG (single solid `#8B6F35` 16×16) and note this as TODO in the commit message — replace before merging.

- [ ] **Step 2: Verify the file is a valid PNG**

```bash
file common/shared/src/main/resources/assets/chronodawn/textures/block/parched_temporal_dirt.png
```

Expected: `PNG image data, 16 x 16, ...`

### Task 1.2: Create the blockstate JSON

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/parched_temporal_dirt.json`

- [ ] **Step 1: Write the blockstate**

```json
{
  "variants": {
    "": { "model": "chronodawn:block/parched_temporal_dirt" }
  }
}
```

### Task 1.3: Create the block model JSON

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/parched_temporal_dirt.json`

- [ ] **Step 1: Write the model**

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/parched_temporal_dirt"
  }
}
```

### Task 1.4: Create the item model JSON

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/parched_temporal_dirt.json`

- [ ] **Step 1: Write the item model (parents the block model)**

```json
{
  "parent": "chronodawn:block/parched_temporal_dirt"
}
```

### Task 1.5: Create the 1.21.4 / 1.21.5+ items JSON

**Files:**
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/parched_temporal_dirt.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/parched_temporal_dirt.json`

- [ ] **Step 1: Write both files with identical content**

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/parched_temporal_dirt"}}
```

(Memory: `feedback_spawn_egg_client_items` — required for 1.21.4+ to render the item icon.)

### Task 1.6: Create the loot tables

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/parched_temporal_dirt.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/parched_temporal_dirt.json`

- [ ] **Step 1: Write the 1.20.1 (plural directory) loot table**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:parched_temporal_dirt"
        }
      ],
      "conditions": [
        { "condition": "minecraft:survives_explosion" }
      ]
    }
  ]
}
```

- [ ] **Step 2: Write the 1.21.1+ (singular directory) loot table**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "rolls": 1.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:parched_temporal_dirt"
        }
      ],
      "conditions": [
        { "condition": "minecraft:survives_explosion" }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/parched_temporal_dirt"
}
```

(Memory: `feedback_loot_table_directory` — directory rename plural→singular at 1.21.1.)

### Task 1.7: Add tag entries

**Files:**
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/dirt.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/dirt.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/shovel.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/shovel.json`

- [ ] **Step 1: 1.21.1+ `dirt.json` — append `chronodawn:parched_temporal_dirt`**

The file currently looks like:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_dirt",
    "chronodawn:temporal_grass_block",
    "chronodawn:coarse_temporal_dirt"
  ]
}
```

After the edit:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_dirt",
    "chronodawn:temporal_grass_block",
    "chronodawn:coarse_temporal_dirt",
    "chronodawn:parched_temporal_dirt"
  ]
}
```

- [ ] **Step 2: 1.20.1 `dirt.json` — same edit, plural directory path**

`common/1.20.1/src/main/resources/data/minecraft/tags/blocks/dirt.json` has the same content shape. Apply the same append.

- [ ] **Step 3: 1.21.1+ `mineable/shovel.json` — append `chronodawn:parched_temporal_dirt`**

Locate the `values` array (it lists existing shovel-mineable Chrono Dawn blocks like `temporal_dirt`, etc.) and append the new entry preserving JSON formatting.

- [ ] **Step 4: 1.20.1 `mineable/shovel.json` — same edit, plural directory path**

(Memory: `feedback_dirt_tag_for_custom_terrain` — custom terrain blocks must be tagged.)

### Task 1.8: Add lang entries

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json`

- [ ] **Step 1: Add the three keys to `en_us.json`**

Find the existing `block.chronodawn.temporal_fern` line and add the three new keys right after it. Use `Edit` with anchored old_string to preserve blank-line section grouping (memory `feedback_lang_json_line_based_edit`):

old_string anchor:
```
  "block.chronodawn.temporal_fern": "Temporal Fern",
```

new_string:
```
  "block.chronodawn.temporal_fern": "Temporal Fern",
  "block.chronodawn.faded_temporal_grass": "Faded Temporal Grass",
  "block.chronodawn.parched_temporal_dirt": "Parched Temporal Dirt",
  "block.chronodawn.temporal_dead_bush": "Temporal Dead Bush",
```

- [ ] **Step 2: Add the three keys to `ja_jp.json` similarly**

Translation:
- `Faded Temporal Grass` → `色褪せた時の草`
- `Parched Temporal Dirt` → `干からびた時の土`
- `Temporal Dead Bush` → `時の枯れ木`

Apply the same anchored Edit pattern as Step 1.

- [ ] **Step 3: Add a biome name entry to both lang files**

old_string anchor (en_us.json), find the existing biome lang line e.g. `"biome.chronodawn.chronodawn_prairies"`:

```
  "biome.chronodawn.chronodawn_prairies": "Chrono Dawn Prairies",
```

new_string:

```
  "biome.chronodawn.chronodawn_prairies": "Chrono Dawn Prairies",
  "biome.chronodawn.chronodawn_faded_plains": "Chrono Dawn Faded Plains",
```

For `ja_jp.json`: `クロノドーンの色褪せた草原`.

### Task 1.9: Register the block in `ModBlocks.java` (all 11 versions)

**Files:**
- Modify: `common/1.20.1/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.2/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.4/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.5/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.6/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.7/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.8/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.9/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java`

For each file, the block uses `ofFullCopy(COARSE_TEMPORAL_DIRT)` then explicitly `.strength(0.5f)` (memory `feedback_ofFullCopy_hardness_pitfall`) and a per-version `setId` chain.

- [ ] **Step 1: Locate the `COARSE_TEMPORAL_DIRT` registration in `1.21.11/.../ModBlocks.java` (around line 568)**

Add after it:

```java
    public static final RegistrySupplier<Block> PARCHED_TEMPORAL_DIRT = BLOCKS.register(
        ModBlockId.PARCHED_TEMPORAL_DIRT.id(),
        () -> new Block(BlockBehaviour.Properties.ofFullCopy(ModBlocks.COARSE_TEMPORAL_DIRT.get())
            .strength(0.5f)
            .sound(SoundType.GRAVEL)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModBlockId.PARCHED_TEMPORAL_DIRT.id()))))
    );
```

For 1.21.5–1.21.10, replace `Identifier.fromNamespaceAndPath(...)` with `ResourceLocation.fromNamespaceAndPath(...)` (memory `feedback_resourcelocation_identifier_rename`).

For 1.21.2–1.21.4, also use `ResourceLocation.fromNamespaceAndPath(...)`.

For 1.21.1, drop the `.setId(...)` chain — just:

```java
    public static final RegistrySupplier<Block> PARCHED_TEMPORAL_DIRT = BLOCKS.register(
        ModBlockId.PARCHED_TEMPORAL_DIRT.id(),
        () -> new Block(BlockBehaviour.Properties.ofFullCopy(ModBlocks.COARSE_TEMPORAL_DIRT.get())
            .strength(0.5f)
            .sound(SoundType.GRAVEL))
    );
```

For 1.20.1, the `ofFullCopy` API may not exist — use `.of()` chain mirroring the existing `COARSE_TEMPORAL_DIRT` registration in `1.20.1/.../ModBlocks.java` exactly. Open that file's `COARSE_TEMPORAL_DIRT` and clone its property chain, swapping the id to `parched_temporal_dirt`.

- [ ] **Step 2: Verify imports for each version**

If a version lacks `import net.minecraft.core.registries.Registries;` or `import net.minecraft.resources.ResourceKey;`, add them. Look at existing per-version registrations to confirm.

- [ ] **Step 3: Run per-version build for the most divergent versions**

```bash
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1
./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5
./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11
```

Expected: BUILD SUCCESSFUL for each.

If any fail, read the diagnostic and inspect the existing `COARSE_TEMPORAL_DIRT` registration in that version to mirror the exact API style.

### Task 1.10: Register the block-item in `ModItems.java` (all 11 versions)

**Files:** all 11 `common/<version>/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Add a `BlockItem` registration mirroring `TEMPORAL_FERN`**

For 1.21.11 (around line 1612):

```java
    public static final RegistrySupplier<Item> PARCHED_TEMPORAL_DIRT = ITEMS.register(
        ModItemId.PARCHED_TEMPORAL_DIRT.id(),
        () -> new BlockItem(ModBlocks.PARCHED_TEMPORAL_DIRT.get(), new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.PARCHED_TEMPORAL_DIRT.id())))));
```

(Memory: `feedback_bare_item_requires_setid` — `.setId(...)` is required from 1.21.2+ to avoid runtime NPE.)

For 1.21.2–1.21.10, swap `Identifier` → `ResourceLocation`.

For 1.21.1 and 1.20.1, drop the `.setId(...)` chain (just `new Item.Properties()`).

- [ ] **Step 2: Add to creative tab `output.accept(...)`**

In each file, find the existing `output.accept(TEMPORAL_FERN.get());` line and add:

```java
        output.accept(PARCHED_TEMPORAL_DIRT.get());
```

immediately after it (or in a similar terrain-block group — inspect each file for the convention).

- [ ] **Step 3: Per-version build verification**

```bash
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1
./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5
./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11
```

Expected: BUILD SUCCESSFUL.

### Task 1.11: Full per-version build for parched_temporal_dirt

- [ ] **Step 1: Run per-version build for all 11 versions**

```bash
./gradlew build1_20_1 \
       && ./gradlew build1_21_1 \
       && ./gradlew build1_21_2 \
       && ./gradlew build1_21_4 \
       && ./gradlew build1_21_5 \
       && ./gradlew build1_21_6 \
       && ./gradlew build1_21_7 \
       && ./gradlew build1_21_8 \
       && ./gradlew build1_21_9 \
       && ./gradlew build1_21_10 \
       && ./gradlew build1_21_11
```

Expected: each command exits 0. If any fails, fix the underlying issue before continuing.

(Memory: `feedback_buildall_gametestall_wrapper_unreliable` — do not rely on the `buildAll` aggregate command; per-version is authoritative.)

### Task 1.12: Commit Phase 1

- [ ] **Step 1: Commit**

```bash
git add common/ docs/
git commit -m "$(cat <<'EOF'
feat(blocks): add parched_temporal_dirt for the upcoming faded plains biome

Cracked dry dirt variant used as a surface and disk-feature material in the
new chronodawn_faded_plains biome. Tagged minecraft:dirt + mineable/shovel.
EOF
)"
```

---

## Phase 2: FADED_TEMPORAL_GRASS (Cross-Plant with Java Class)

### Task 2.1: Texture

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/faded_temporal_grass.png`

- [ ] **Step 1: Produce the 16×16 cross plant texture**

3–4 blade strands, baked color `#B89C4A` ± natural variation. The texture must already look yellow-brown — no biome tint will be applied at runtime.

If asset generation isn't available, place a placeholder solid `#B89C4A` 16×16 and note as TODO.

### Task 2.2: Blockstate

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/faded_temporal_grass.json`

- [ ] **Step 1: Write**

```json
{
  "variants": {
    "": { "model": "chronodawn:block/faded_temporal_grass" }
  }
}
```

### Task 2.3: Block model (plain `block/cross`, no tint)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/faded_temporal_grass.json`

- [ ] **Step 1: Write**

```json
{
  "parent": "minecraft:block/cross",
  "render_type": "minecraft:cutout",
  "textures": {
    "cross": "chronodawn:block/faded_temporal_grass"
  }
}
```

(Note: `minecraft:block/cross`, NOT `block/tinted_cross`. Memory: `feedback_tinted_cross_required` does NOT apply — we want a fixed-color, untinted appearance.)

### Task 2.4: Item model

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/faded_temporal_grass.json`

- [ ] **Step 1: Write**

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:block/faded_temporal_grass"
  }
}
```

(Memory: `feedback_item_model_flat_icon_pattern` — cross plants need `item/generated` parent for inventory icon.)

### Task 2.5: Items JSON for 1.21.4 and 1.21.5+

**Files:**
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/faded_temporal_grass.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/faded_temporal_grass.json`

- [ ] **Step 1: Write both with identical content**

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/faded_temporal_grass"}}
```

### Task 2.6: Loot table (shears-only drop)

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/faded_temporal_grass.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/faded_temporal_grass.json`

- [ ] **Step 1: 1.20.1 form (predicate `enchantments` direct, items array)**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "items": [
                      "minecraft:shears"
                    ]
                  }
                }
              ],
              "name": "chronodawn:faded_temporal_grass"
            }
          ]
        }
      ]
    }
  ]
}
```

- [ ] **Step 2: 1.21.1+ form (nested predicates, items as string)**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "rolls": 1.0,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "items": "minecraft:shears"
                  }
                }
              ],
              "name": "chronodawn:faded_temporal_grass"
            }
          ]
        }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/faded_temporal_grass"
}
```

(Memory: `feedback_loot_table_silk_touch_format` — directly-keyed vs nested predicates split is the same shape that affects shears, only minus the silk_touch branch.)

### Task 2.7: Java block class — write 11 per-version files

**Files:** `common/<each-of-11>/src/main/java/com/chronodawn/blocks/FadedTemporalGrassBlock.java`

This is a thin wrapper around `BushBlock` (or whatever the per-version base for cross plants is — `TemporalFernBlock` is the canonical reference).

- [ ] **Step 1: For each version, open `TemporalFernBlock.java` in that version**

Read `common/<version>/src/main/java/com/chronodawn/blocks/TemporalFernBlock.java`. The new class is essentially identical except:
- Class name `FadedTemporalGrassBlock`
- `mayPlaceOn` allowed surfaces: `BlockTags.DIRT` plus an explicit check for `chronodawn:temporal_grass_block` (which is in the `dirt` tag already, so `BlockTags.DIRT` covers it). Drop the `Blocks.FARMLAND` check — faded grass on farmland is incongruent.
- VoxelShape: shorter, since this is "short brittle grass". Use `Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0)` (8 instead of 13 for height).

- [ ] **Step 2: Write the file for 1.20.1**

```java
package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FadedTemporalGrassBlock extends BushBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

    public FadedTemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }
}
```

- [ ] **Step 3: Write the file for 1.21.1**

Add MapCodec and `protected MapCodec<? extends BushBlock> codec()`:

```java
package com.chronodawn.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FadedTemporalGrassBlock extends BushBlock {
    public static final MapCodec<FadedTemporalGrassBlock> CODEC = simpleCodec(FadedTemporalGrassBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

    public FadedTemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }
}
```

- [ ] **Step 4: Write the file for 1.21.2 and 1.21.4 (createProperties takes id String, .setId chain, ResourceLocation)**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FadedTemporalGrassBlock extends BushBlock {
    public static final MapCodec<FadedTemporalGrassBlock> CODEC = simpleCodec(FadedTemporalGrassBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

    public FadedTemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .setId(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }
}
```

- [ ] **Step 5: Write the file for 1.21.5–1.21.8 (cast codec signature)**

Same as Step 4 except replace the `codec()` method with:

```java
    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<BushBlock> codec() {
        return (MapCodec<BushBlock>) (MapCodec<?>) CODEC;
    }
```

(Mirrors the per-version `TemporalFernBlock.codec()` style for these versions.)

- [ ] **Step 6: Write the file for 1.21.9 and 1.21.10 (`noCollision` single-s)**

Same as Step 5 except `.noCollission()` becomes `.noCollision()` (memory `feedback_no_collision_spelling_split`).

- [ ] **Step 7: Write the file for 1.21.11 (`Identifier` import)**

Same as Step 6 except replace:
- `import net.minecraft.resources.ResourceLocation;` → `import net.minecraft.resources.Identifier;`
- `ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`

(Memory: `feedback_resourcelocation_identifier_rename`.)

- [ ] **Step 8: Compile each version individually**

```bash
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1
./gradlew :common-1.21.2:compileJava -Ptarget_mc_version=1.21.2
./gradlew :common-1.21.4:compileJava -Ptarget_mc_version=1.21.4
./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5
./gradlew :common-1.21.6:compileJava -Ptarget_mc_version=1.21.6
./gradlew :common-1.21.7:compileJava -Ptarget_mc_version=1.21.7
./gradlew :common-1.21.8:compileJava -Ptarget_mc_version=1.21.8
./gradlew :common-1.21.9:compileJava -Ptarget_mc_version=1.21.9
./gradlew :common-1.21.10:compileJava -Ptarget_mc_version=1.21.10
./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11
```

Each must exit 0. (No new test files yet — registration not done.)

### Task 2.8: Register in `ModBlocks.java` (all 11 versions)

- [ ] **Step 1: For each ModBlocks.java, add after the `TEMPORAL_FERN` block**

For 1.21.5+ (where `createProperties` takes id):

```java
    public static final RegistrySupplier<Block> FADED_TEMPORAL_GRASS = BLOCKS.register(
        ModBlockId.FADED_TEMPORAL_GRASS.id(),
        () -> new FadedTemporalGrassBlock(FadedTemporalGrassBlock.createProperties("faded_temporal_grass"))
    );
```

For 1.21.1 / 1.20.1 (where `createProperties()` takes no args):

```java
    public static final RegistrySupplier<Block> FADED_TEMPORAL_GRASS = BLOCKS.register(
        ModBlockId.FADED_TEMPORAL_GRASS.id(),
        () -> new FadedTemporalGrassBlock(FadedTemporalGrassBlock.createProperties())
    );
```

- [ ] **Step 2: Per-version compileJava**

Same command set as Task 2.7 Step 8.

### Task 2.9: Register block-item in `ModItems.java` (all 11 versions)

- [ ] **Step 1: After the `TEMPORAL_FERN` BlockItem, add**

For 1.21.11:

```java
    public static final RegistrySupplier<Item> FADED_TEMPORAL_GRASS = ITEMS.register(
        ModItemId.FADED_TEMPORAL_GRASS.id(),
        () -> new BlockItem(ModBlocks.FADED_TEMPORAL_GRASS.get(), new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.FADED_TEMPORAL_GRASS.id())))));
```

For 1.21.2–1.21.10: swap `Identifier` → `ResourceLocation`. For 1.21.1 / 1.20.1: drop `.setId(...)`.

- [ ] **Step 2: Add to creative tab**

```java
        output.accept(FADED_TEMPORAL_GRASS.get());
```

after the existing `TEMPORAL_FERN` line.

- [ ] **Step 3: Compile each version**

Same command set as Task 2.7 Step 8.

### Task 2.10: Per-version build verification for Phase 2

- [ ] **Step 1: Run `build1_X_Y` for all 11 versions, sequentially, halting on the first failure**

If a build fails, the most likely cause is a missing import or per-version API mismatch in the new Block class. Read the diagnostic and inspect the same version's `TemporalFernBlock.java` for the exact API style.

### Task 2.11: Commit Phase 2

- [ ] **Step 1: Commit**

```bash
git add common/
git commit -m "feat(blocks): add faded_temporal_grass cross plant"
```

---

## Phase 3: TEMPORAL_DEAD_BUSH

This block follows the same structure as Phase 2 (`FADED_TEMPORAL_GRASS`), with three differences:
- Drops sticks (uniform 0–2) when broken without shears
- `mayPlaceOn` excludes `chronodawn:temporal_grass_block` and `minecraft:grass_block`
- Texture art differs (dead-bush silhouette, `#6E5A35`)

### Task 3.1–3.5: Resources (analogous to Tasks 2.1–2.5)

- [ ] **Step 1: Texture** — `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_dead_bush.png` (dead-bush silhouette, `#6E5A35`)
- [ ] **Step 2: Blockstate** — same shape as Task 2.2 with `temporal_dead_bush` substituted
- [ ] **Step 3: Block model** — same as Task 2.3
- [ ] **Step 4: Item model** — same as Task 2.4
- [ ] **Step 5: Items JSON for 1.21.4 + shared-1.21.5+** — same as Task 2.5

### Task 3.6: Loot table (shears OR sticks)

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_dead_bush.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_dead_bush.json`

- [ ] **Step 1: 1.20.1 form**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "items": [
                      "minecraft:shears"
                    ]
                  }
                }
              ],
              "name": "chronodawn:temporal_dead_bush"
            },
            {
              "type": "minecraft:item",
              "name": "minecraft:stick",
              "functions": [
                {
                  "function": "minecraft:set_count",
                  "count": {
                    "type": "minecraft:uniform",
                    "min": 0.0,
                    "max": 2.0
                  }
                },
                {
                  "function": "minecraft:explosion_decay"
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

- [ ] **Step 2: 1.21.1+ form**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "rolls": 1.0,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "items": "minecraft:shears"
                  }
                }
              ],
              "name": "chronodawn:temporal_dead_bush"
            },
            {
              "type": "minecraft:item",
              "name": "minecraft:stick",
              "functions": [
                {
                  "function": "minecraft:set_count",
                  "count": {
                    "type": "minecraft:uniform",
                    "min": 0.0,
                    "max": 2.0
                  }
                },
                {
                  "function": "minecraft:explosion_decay"
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  "random_sequence": "chronodawn:blocks/temporal_dead_bush"
}
```

### Task 3.7: Java block class (all 11 versions)

Same structure as Task 2.7 with these substitutions:
- Class name: `TemporalDeadBushBlock`
- `mayPlaceOn` returns:

```java
return state.is(net.minecraft.world.level.block.Blocks.SAND)
    || state.is(net.minecraft.world.level.block.Blocks.RED_SAND)
    || state.is(com.chronodawn.registry.ModBlocks.TEMPORAL_SAND.get())
    || state.is(com.chronodawn.registry.ModBlocks.TEMPORAL_DIRT.get())
    || state.is(com.chronodawn.registry.ModBlocks.COARSE_TEMPORAL_DIRT.get())
    || state.is(com.chronodawn.registry.ModBlocks.PARCHED_TEMPORAL_DIRT.get())
    || state.is(com.chronodawn.registry.ModBlocks.TEMPORAL_GRAVEL.get());
```

- VoxelShape: `Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)` (taller, similar to fern)

- [ ] **Step 1: Write each per-version file mirroring Tasks 2.7 Steps 2–7**

- [ ] **Step 2: Per-version `compileJava`**

### Task 3.8: Register in ModBlocks.java + ModItems.java

Same pattern as Tasks 2.8 and 2.9, with `TEMPORAL_DEAD_BUSH` substituted.

- [ ] **Step 1: ModBlocks.java additions for all 11 versions**
- [ ] **Step 2: ModItems.java additions for all 11 versions**
- [ ] **Step 3: Per-version `compileJava` × 11**

### Task 3.9: Per-version full build for Phase 3

- [ ] **Step 1: Run all 11 `build1_X_Y` commands**

### Task 3.10: Commit Phase 3

```bash
git add common/
git commit -m "feat(blocks): add temporal_dead_bush with shears + stick drops"
```

---

## Phase 4: Worldgen Configured & Placed Features

### Task 4.1: Inner ConfiguredFeature `faded_grass_block`

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/faded_grass_block.json`

- [ ] **Step 1: Write the simple_block feature**

```json
{
  "type": "minecraft:simple_block",
  "config": {
    "to_place": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:faded_temporal_grass"
      }
    }
  }
}
```

### Task 4.2: Inner ConfiguredFeature `temporal_dead_bush_block`

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/temporal_dead_bush_block.json`

- [ ] **Step 1: Write**

```json
{
  "type": "minecraft:simple_block",
  "config": {
    "to_place": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:temporal_dead_bush"
      }
    }
  }
}
```

### Task 4.3: Disk feature for `parched_temporal_dirt` (per-version split)

The `disk` feature schema differs between 1.20.1 (nested `value`) and 1.21.1+ (flat). See `common/1.20.1/.../gravel_disk.json` vs `common/shared-1.21.1+/.../gravel_disk.json` for reference.

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/worldgen/configured_feature/parched_temporal_dirt_disk.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/parched_temporal_dirt_disk.json`

- [ ] **Step 1: 1.20.1 form (nested `value` IntProvider)**

```json
{
  "type": "minecraft:disk",
  "config": {
    "half_height": 1,
    "radius": {
      "type": "minecraft:uniform",
      "value": {
        "min_inclusive": 2,
        "max_inclusive": 4
      }
    },
    "state_provider": {
      "fallback": {
        "type": "minecraft:simple_state_provider",
        "state": {
          "Name": "chronodawn:parched_temporal_dirt"
        }
      },
      "rules": []
    },
    "target": {
      "type": "minecraft:matching_blocks",
      "blocks": [
        "chronodawn:temporal_grass_block",
        "chronodawn:temporal_dirt",
        "chronodawn:coarse_temporal_dirt"
      ]
    }
  }
}
```

- [ ] **Step 2: 1.21.1+ form (flat IntProvider keys, memory `feedback_intprovider_uniform_flat_keys`)**

```json
{
  "type": "minecraft:disk",
  "config": {
    "half_height": 1,
    "radius": {
      "type": "minecraft:uniform",
      "min_inclusive": 2,
      "max_inclusive": 4
    },
    "state_provider": {
      "fallback": {
        "type": "minecraft:simple_state_provider",
        "state": {
          "Name": "chronodawn:parched_temporal_dirt"
        }
      },
      "rules": []
    },
    "target": {
      "type": "minecraft:matching_blocks",
      "blocks": [
        "chronodawn:temporal_grass_block",
        "chronodawn:temporal_dirt",
        "chronodawn:coarse_temporal_dirt"
      ]
    }
  }
}
```

### Task 4.4: ConfiguredFeature `dead_snag` (vertical bare log)

This is the trickiest feature. We want a 3–5 block vertical column of `STRIPPED_TIME_WOOD_LOG` with no foliage. We will use `minecraft:tree` with `no_op` foliage placer, falling back to `minecraft:simple_block` repeated if `no_op` isn't available in 1.20.1.

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/dead_snag.json`

- [ ] **Step 1: First, attempt the `minecraft:tree` form**

```json
{
  "type": "minecraft:tree",
  "config": {
    "trunk_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:stripped_time_wood_log",
        "Properties": {
          "axis": "y"
        }
      }
    },
    "foliage_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "minecraft:air"
      }
    },
    "trunk_placer": {
      "type": "minecraft:straight_trunk_placer",
      "base_height": 3,
      "height_rand_a": 2,
      "height_rand_b": 0
    },
    "foliage_placer": {
      "type": "minecraft:blob_foliage_placer",
      "radius": 0,
      "offset": 0,
      "height": 0
    },
    "minimum_size": {
      "type": "minecraft:two_layers_feature_size",
      "limit": 1,
      "lower_size": 0,
      "upper_size": 1
    },
    "decorators": []
  }
}
```

(`blob_foliage_placer` with radius 0 effectively places no foliage; combined with `air` foliage_provider this is safe across versions.)

- [ ] **Step 2: Run `validateResources` (both 1.20.1 and 1.21.11) to confirm the JSON parses**

```bash
./gradlew validateResources
```

If 1.20.1 rejects the format, split into per-version files (1.20.1-specific override under `common/1.20.1/.../configured_feature/dead_snag.json`).

### Task 4.5: Outer `random_patch` features

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/patch_faded_grass.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/patch_temporal_dead_bush.json`

- [ ] **Step 1: `patch_faded_grass`**

```json
{
  "type": "minecraft:random_patch",
  "config": {
    "tries": 64,
    "xz_spread": 7,
    "y_spread": 3,
    "feature": "chronodawn:faded_grass_block"
  }
}
```

- [ ] **Step 2: `patch_temporal_dead_bush`**

```json
{
  "type": "minecraft:random_patch",
  "config": {
    "tries": 32,
    "xz_spread": 5,
    "y_spread": 3,
    "feature": "chronodawn:temporal_dead_bush_block"
  }
}
```

### Task 4.6: Placed features

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/patch_faded_grass.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/patch_temporal_dead_bush.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/disk_parched_temporal_dirt.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/dead_snag_placed.json`

- [ ] **Step 1: `patch_faded_grass.json`**

```json
{
  "feature": "chronodawn:patch_faded_grass",
  "placement": [
    { "type": "minecraft:count", "count": 10 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 2: `patch_temporal_dead_bush.json`**

```json
{
  "feature": "chronodawn:patch_temporal_dead_bush",
  "placement": [
    { "type": "minecraft:count", "count": 3 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 3: `disk_parched_temporal_dirt.json`**

```json
{
  "feature": "chronodawn:parched_temporal_dirt_disk",
  "placement": [
    { "type": "minecraft:rarity_filter", "chance": 5 },
    { "type": "minecraft:count", "count": 1 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 4: `dead_snag_placed.json`**

```json
{
  "feature": "chronodawn:dead_snag",
  "placement": [
    { "type": "minecraft:rarity_filter", "chance": 5 },
    { "type": "minecraft:count", "count": 1 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
    { "type": "minecraft:biome" }
  ]
}
```

### Task 4.7: Resource validation for new features

- [ ] **Step 1: Run validateResources**

```bash
./gradlew validateResources
```

Expected: BUILD SUCCESSFUL with no JSON syntax or cross-reference errors.

If failures relate to `dead_snag` JSON shape, fall back to a per-version split of that file (write the 1.20.1 form under `common/1.20.1/.../configured_feature/dead_snag.json` mirroring vanilla `huge_brown_mushroom`-style empty-foliage tree).

### Task 4.8: Commit Phase 4

```bash
git add common/
git commit -m "feat(worldgen): add faded plains decorative features (grass patch, dead bush patch, parched dirt disk, dead snag)"
```

---

## Phase 5: Biome Integration

### Task 5.1: Biome JSON for `common/1.20.1/`

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_faded_plains.json`

- [ ] **Step 1: Open the existing `common/1.20.1/.../chronodawn_prairies.json` and copy as a template**

- [ ] **Step 2: Apply these substitutions**

```jsonc
{
  "has_precipitation": false,         // changed from true
  "temperature": 1.0,                 // changed from 0.55
  "temperature_modifier": "none",
  "downfall": 0.1,                    // changed from 0.45
  "effects": {
    "sky_color": 9474192,             // unchanged (dimension shared)
    "fog_color": 12632256,            // unchanged
    "water_color": 6983845,           // unchanged
    "water_fog_color": 3482025,       // unchanged
    "grass_color": 12953179,          // 0xC4A85B — new
    "foliage_color": 12096325,        // 0xB89545 — new
    "mood_sound": { /* same as prairies */ },
    "music": [ /* same as prairies */ ],
    "music_volume": 1.0
  },
  "spawners": {
    /* monster: identical to prairies but with epoch_husk weight 110, temporal_wraith weight 110 */
    /* creature: keep ONLY chronodawn:timebound_rabbit (weight 4, count 2-3) and chronodawn:time_keeper (weight 10, count 1-2) */
    /* ambient: minecraft:bat (weight 10, count 4-6) */
    /* water_creature, water_ambient, misc: empty */
  },
  "spawn_costs": {},
  "features": [
    [], [], [], [], [], [],
    [
      "chronodawn:ore_gold",
      "chronodawn:ore_redstone",
      "chronodawn:ore_time_crystal",
      "chronodawn:ore_entropy_crystal",
      "chronodawn:ore_temporal_amber",
      "chronodawn:ore_clockstone",
      "chronodawn:ore_coal",
      "chronodawn:ore_iron",
      "chronodawn:clockwork_block_cluster"
    ],
    [
      "chronodawn:temporal_stalactite_cluster",
      "chronodawn:temporal_stalagmite_cluster",
      "chronodawn:temporal_pointed_single"
    ],
    [],
    [
      "chronodawn:patch_faded_grass",
      "chronodawn:patch_temporal_dead_bush",
      "chronodawn:patch_coarse_dirt",
      "chronodawn:disk_parched_temporal_dirt",
      "chronodawn:gravel_disk_placed",
      "chronodawn:boulder_placed",
      "chronodawn:fallen_log_placed",
      "chronodawn:dead_snag_placed",
      "chronodawn:lost_adventurer_memorial_placed",
      "chronodawn:time_cairn_placed",
      "chronodawn:old_sundial_placed"
    ],
    []
  ],
  "carvers": []
}
```

- [ ] **Step 3: Verify the file is valid JSON**

```bash
python3 -c "import json; json.load(open('common/1.20.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_faded_plains.json'))"
```

Expected: no output (success).

### Task 5.2: Biome JSON for `common/1.21.1/`

- [ ] **Step 1: Copy the `common/1.21.1/.../chronodawn_prairies.json` as a template**

- [ ] **Step 2: Apply the same field substitutions as Task 5.1**

The 1.21.1 form may differ slightly from 1.20.1 — diff against the 1.20.1 prairies file to confirm structure.

### Task 5.3: Biome JSON for `common/1.21.2/`

Same structure as Task 5.2, copying from `common/1.21.2/.../chronodawn_prairies.json`.

### Task 5.4: Biome JSON for `common/1.21.11/`

Same as 5.2, copying from `common/1.21.11/.../chronodawn_prairies.json` (memory: `feedback_1_21_11_biome_overrides`).

### Task 5.5: Biome JSON for `common/shared-1.21.2+/`

- [ ] **Step 1: Copy `common/shared-1.21.2+/.../chronodawn_prairies.json` and apply substitutions**

This file covers 1.21.4–1.21.10 (versions without their own biome dir).

### Task 5.6: Add new biome to dimension JSON

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/dimension/chronodawn.json`

- [ ] **Step 1: Append a new entry inside `biome_source.biomes` array**

Old anchor (last entry of the existing array, the swamp entry):

```json
        {
          "biome": "chronodawn:chronodawn_swamp",
          "parameters": {
            "temperature": [0.3, 1.0],
            "humidity": [0.5, 1.0],
            "continentalness": [0.03, 0.3],
            "erosion": [0.0, 1.0],
            "depth": 0,
            "weirdness": [-1.0, 1.0],
            "offset": 0.0
          }
        }
      ]
```

New (append the new entry, with closing `]` after it):

```json
        {
          "biome": "chronodawn:chronodawn_swamp",
          "parameters": {
            "temperature": [0.3, 1.0],
            "humidity": [0.5, 1.0],
            "continentalness": [0.03, 0.3],
            "erosion": [0.0, 1.0],
            "depth": 0,
            "weirdness": [-1.0, 1.0],
            "offset": 0.0
          }
        },
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
      ]
```

### Task 5.7: Add new biome to `has_master_clock` tag

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_master_clock.json`

- [ ] **Step 1: Append `"chronodawn:chronodawn_faded_plains"` to the `values` array**

### Task 5.8: Per-version build for biome integration

- [ ] **Step 1: Run all 11 `build1_X_Y` commands sequentially**

If any per-version build fails with an error referencing a missing feature ID, double-check that the placed feature exists in `common/shared/.../placed_feature/` and the configured feature it references resolves.

### Task 5.9: Manual world load smoke test (1.21.11 only)

- [ ] **Step 1: Run client**

```bash
./gradlew runClientFabric1_21_11
```

- [ ] **Step 2: In-game**

1. Open creative world.
2. Use the Chrono Dawn portal to enter the dimension.
3. Run `/locate biome chronodawn:chronodawn_faded_plains` — should report a position.
4. Run `/tp` to that position.
5. Visually verify: yellow-tinted grass, brown dead bushes, cracked parched dirt patches, occasional bare log columns.
6. Open inventory, verify all three new block icons render correctly (not black/purple).

### Task 5.10: Commit Phase 5

```bash
git add common/
git commit -m "feat(worldgen): add chronodawn_faded_plains biome to dimension"
```

---

## Phase 6: GameTests and Documentation

### Task 6.1: GameTest for `temporal_dead_bush` survival on `parched_temporal_dirt`

**Files:**
- Create: `gametest/src/main/java/com/chronodawn/gametest/biome/FadedPlainsTest.java` (also pick existing gametest dir convention if different)
- Create: `gametest/src/main/resources/data/chronodawn/gametest/structure/faded_plains_dead_bush_survival.snbt`

- [ ] **Step 1: Inspect the existing gametest layout**

```bash
git ls-files 'gametest/src/main/java/**/*.java' | head -20
git ls-files 'gametest/src/main/resources/data/chronodawn/gametest/structure/' | head -10
```

Read at least one existing test to mirror its structure (annotations, helper class).

- [ ] **Step 2: Write the test class**

(Concrete code depends on existing helper classes — match their style. The test:)

```java
package com.chronodawn.gametest.biome;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FadedPlainsTest {

    @GameTest(template = "chronodawn:faded_plains_dead_bush_survival")
    public void deadBushSurvivesOnParchedDirt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), ModBlocks.PARCHED_TEMPORAL_DIRT.get().defaultBlockState());
        helper.setBlock(pos, ModBlocks.TEMPORAL_DEAD_BUSH.get().defaultBlockState());
        helper.succeedWhen(() -> helper.assertBlockPresent(ModBlocks.TEMPORAL_DEAD_BUSH.get(), pos));
    }

    @GameTest(template = "chronodawn:faded_plains_dead_bush_survival")
    public void deadBushBreaksOnGrass(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), ModBlocks.TEMPORAL_GRASS_BLOCK.get().defaultBlockState());
        helper.setBlock(pos, ModBlocks.TEMPORAL_DEAD_BUSH.get().defaultBlockState());
        helper.runAfterDelay(5, () -> {
            helper.assertBlockNotPresent(ModBlocks.TEMPORAL_DEAD_BUSH.get(), pos);
            helper.succeed();
        });
    }
}
```

- [ ] **Step 3: Write the SNBT structure (3×3×3 with air)**

Use an existing structure as a template, e.g. `gametest/src/main/resources/data/chronodawn/gametest/structure/empty_3x3x3.snbt` if it exists.

- [ ] **Step 4: Register the test class in the GameTest registrar**

Find the existing test registry (e.g. `gametest/src/main/java/com/chronodawn/gametest/ChronoDawnGameTests.java`) and add `FadedPlainsTest.class` to the list.

- [ ] **Step 5: Run the gametest**

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11
```

Expected: tests `chronodawn:faded_plains_test.dead_bush_survives_on_parched_dirt` and `dead_bush_breaks_on_grass` pass.

### Task 6.2: GameTest for `faded_temporal_grass` shears drop

This test is harder — it requires breaking a block with shears as if a player did. If the existing GameTest framework has a `breakBlock` helper that supports tools, use it; otherwise skip this test in favor of a unit test of the loot table.

- [ ] **Step 1: Search for existing shear-tool tests**

```bash
git grep -l 'shears' gametest/src/main/java/
```

- [ ] **Step 2: If a precedent exists, mirror it; otherwise skip and add a TODO note in the design doc Open Questions section**

### Task 6.3: Update player guide

**Files:**
- Modify: `docs/player_guide.md`

- [ ] **Step 1: Find the biome list section**

```bash
git grep -n '## Biomes\|### Biomes\|Prairies' docs/player_guide.md
```

- [ ] **Step 2: Add a paragraph for Faded Plains**

After the existing prairies entry, add:

```markdown
### Faded Plains

A withered wasteland adjacent to the deserts of Chrono Dawn. The grass remains but has long since lost its color, and the soil shows signs of cracking. Stripped tree skeletons stand silent under a still sun. The land here barely supports life — only timebound rabbits and wandering time keepers — but the same ores can be mined beneath as in any biome.
```

### Task 6.4: Update CurseForge / Modrinth descriptions

**Files:**
- Modify: `docs/curseforge_description.md`
- Modify: `docs/modrinth_description.md`

- [ ] **Step 1: Append a one-line bullet to each**

Locate the biome list. Append:

```
- **Faded Plains** — a withered wasteland with sun-faded grass and bare snags
```

### Task 6.5: Final per-version build verification

- [ ] **Step 1: Run all 11 `build1_X_Y` commands**

- [ ] **Step 2: Run `validateResources` and `validateTranslations`**

```bash
./gradlew validateResources
./gradlew validateTranslations
```

Expected: BUILD SUCCESSFUL for both.

### Task 6.6: Final commit

```bash
git add docs/ gametest/
git commit -m "$(cat <<'EOF'
test(biome): add gametest coverage and player-facing docs for faded plains

GameTests verify TEMPORAL_DEAD_BUSH survives on PARCHED_TEMPORAL_DIRT and
breaks above TEMPORAL_GRASS_BLOCK. Player guide and external descriptions
now mention the new biome.
EOF
)"
```

---

## Verification Summary

Final acceptance:

- [ ] All 11 `build1_X_Y` invocations exit 0.
- [ ] `./gradlew validateResources` exits 0.
- [ ] `./gradlew validateTranslations` exits 0.
- [ ] `FadedPlainsTest` GameTests pass on at least 1.21.11 via `:fabric:runGameTest -Ptarget_mc_version=1.21.11`.
- [ ] Manual smoke test (Task 5.9) confirms biome appears, blocks render, ground is yellow.
- [ ] `docs/player_guide.md`, `docs/curseforge_description.md`, `docs/modrinth_description.md` mention the new biome.

After acceptance, ask the user before pushing or releasing (per CLAUDE.md commit policy).
