---
name: add-terrain-block
description: Use when adding terrain blocks (stone, dirt, grass, cobblestone, ore) or their stairs/slab/wall variants to ChronoDawn. Covers block classes, registration, resources, loot tables, tool tier tags, worldgen integration, and version-specific pitfalls.
---

# Add Terrain Block to ChronoDawn

**Purpose**: Complete guide for adding terrain/ore blocks with full multi-version support. Covers plain blocks, stairs/slab/wall variants, and ore blocks. Includes non-obvious pitfalls discovered during temporal_stone and temporal ore implementations.

---

## Quick Decision: What type of block?

| Block Type | Base Class | Example |
|---|---|---|
| Plain terrain | `Block` | temporal_stone, temporal_cobblestone, temporal_dirt |
| Grass-like (spreading) | `SpreadingSnowyDirtBlock` | temporal_grass_block |
| Stairs | `StairBlock` | temporal_stone_stairs |
| Slab | `SlabBlock` | temporal_stone_slab |
| Wall | `WallBlock` | temporal_stone_wall |
| Plain ore | `Block` | temporal_coal_ore, temporal_iron_ore, temporal_gold_ore |
| Redstone ore (glowing) | `RedStoneOreBlock` | temporal_redstone_ore |

---

## Version Groups (CRITICAL - block class patterns differ)

Block classes use **3 different patterns** based on Minecraft API differences:

| Group | Versions | Pattern |
|---|---|---|
| **A** | 1.20.1, 1.21.1 | `Properties.of()` with explicit props, no `setId()` |
| **B** | 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10 | `Properties.ofFullCopy(Blocks.X)` + `.setId(ResourceKey...)` with `ResourceLocation` |
| **C** | 1.21.11 | Same as B but `Identifier` instead of `ResourceLocation` |

**Every block class must be created in ALL 11 version directories** (`common/<ver>/src/main/java/com/chronodawn/blocks/`).

---

## Checklist (MUST complete ALL applicable items)

### 1. ID Registration (common/shared/)
- [ ] Add to `ModBlockId.java` (enum entry, default SOLID render layer)
- [ ] Add to `ModItemId.java` (matching item ID)

### 2. Block Classes (per version × 11 versions)
- [ ] Create block class in `common/<ver>/src/main/java/com/chronodawn/blocks/`
- [ ] Use correct Group A/B/C pattern per version
- [ ] For stairs: constructor calls `super(ModBlocks.BASE.get().defaultBlockState(), properties)`
- [ ] For variants: delegate `createProperties()` to base block + add `.setId(...)` in Group B/C

### 3. Block Registration (per version × 11 versions)
- [ ] `ModBlocks.java` — add `BLOCKS.register(...)` entries + imports
- [ ] `ModItems.java` — add `ITEMS.register(...)` BlockItem entries (3 patterns A/B/C)
- [ ] `ModItems.java` — add to `populateCreativeTab()` for creative menu visibility

### 4. Resources (common/shared/)
- [ ] `textures/block/<name>.png` (16x16, follow temporal theme)
- [ ] `blockstates/<name>.json` (simple or multi-variant for stairs/slab/wall)
- [ ] `models/block/<name>.json` (cube_all, stairs, slab, wall templates)
- [ ] `models/item/<name>.json` (parent block model; walls use `minecraft:block/wall_inventory`)

### 5. Client Items JSON (1.21.4+) — REQUIRED, often missed
- [ ] `common/1.21.4/src/main/resources/assets/chronodawn/items/<name>.json`
- [ ] `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/<name>.json`
- Format: `{"model":{"type":"minecraft:model","model":"chronodawn:item/<name>"}}`

### 6. Loot Tables (both directories, CORRECT silk touch format)
- [ ] `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/<name>.json` (plural `loot_tables`)
- [ ] `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/<name>.json` (singular `loot_table`)
- [ ] **Use new nested predicate format** (see Critical Gotchas)

### 7. Tags
- [ ] `minecraft:mineable/pickaxe` (both 1.20.1 `tags/blocks/` and shared-1.21.1+ `tags/block/`)
- [ ] **Tool tier tag** (ores only — see Critical Gotchas)

### 8. Translations
- [ ] `common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json` + `ja_jp.json`
- [ ] `common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json` + `ja_jp.json`
- [ ] `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json` + `ja_jp.json`

### 9. Worldgen Integration (optional, see Worldgen section)
- [ ] Noise settings (if replacing dimension base block)
- [ ] Configured feature (ores)
- [ ] Placed feature (ores)
- [ ] Biome feature lists (1.21.1 + shared-1.21.2+ + **1.21.11 overrides**)

---

## Critical Gotchas (READ BEFORE IMPLEMENTING)

### 🔴 Loot Table Silk Touch Predicate Format

**This caused temporal_stone to never drop temporal_cobblestone.** The old simple format parses as valid JSON but does NOT work at runtime — the condition misbehaves silently.

**WRONG (old format):**
```json
"predicate": {
  "enchantments": [
    { "enchantment": "minecraft:silk_touch", "levels": { "min": 1 } }
  ]
}
```

**CORRECT (new nested format, use for ALL versions):**
```json
"predicate": {
  "predicates": {
    "minecraft:enchantments": [
      { "enchantments": "minecraft:silk_touch", "levels": { "min": 1 } }
    ]
  }
}
```

Note: `enchantments` (plural) inside the array element, not `enchantment`. Reference: `clockstone_ore.json`, `frozen_time_ice.json`.

### 🔴 Tool Tier Tags (ores)

`requiresCorrectToolForDrops()` does NOT set the tier. Tier is data-driven via tags. Without tier tags, **a wooden pickaxe can mine and drop iron/gold/redstone ores**, breaking vanilla parity.

| Ore | Required tag |
|---|---|
| `temporal_coal_ore` | (none — wood pickaxe OK) |
| `temporal_iron_ore` | `minecraft:needs_stone_tool` |
| `temporal_gold_ore` | `minecraft:needs_iron_tool` |
| `temporal_redstone_ore` | `minecraft:needs_iron_tool` |

Files to update/create:
- `common/1.20.1/.../minecraft/tags/blocks/needs_{stone,iron}_tool.json` (plural `blocks/`)
- `common/shared-1.21.1+/.../minecraft/tags/block/needs_{stone,iron}_tool.json` (singular `block/`)

Create `needs_stone_tool.json` if it doesn't exist.

### 🔴 1.21.11 Biome Overrides Are Independent

`common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/` contains **version-specific biome JSON overrides** (9 biomes) that must be updated **independently** from `common/shared-1.21.2+/.../biome/`. Changes only made in shared-1.21.2+ will silently skip 1.21.11.

When adding/removing biome features, always update BOTH:
- `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/` (10 files)
- `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/` (10 files)
- `common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/` (9 files)

### 🔴 Subagent-Driven Development: Watch for Unintended Edits

When dispatching subagents to create block classes across 11 versions in parallel, they may accidentally modify **existing** files (e.g., ClockstoneBlock.java, UnstableFungus.java, TemporalParticleEmitterBlock.java typos) while searching for "similar patterns". Always run:

```bash
git diff --name-only -- '*.java' | grep -v 'New\|ModBlocks\|ModItems\|ModBlockId\|ModItemId'
```

and restore unintended changes with `git checkout --` before building.

### 🔴 Creative Tab Completeness Test

The project has `CreativeTabCompletenessTest` that fails if any registered block/item is not in `populateCreativeTab()`. Always add new blocks to the creative tab immediately when registering them, or tests fail.

---

## Block Class Templates

### Group A: 1.20.1 / 1.21.1 — Plain block (stone-like)

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TemporalStoneBlock extends Block {
    public TemporalStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
```

### Group B: 1.21.2–1.21.10 — Plain block

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneBlock extends Block {
    public TemporalStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stone")));
    }
}
```

### Group C: 1.21.11 — Same as B but `Identifier`

Replace `ResourceLocation.fromNamespaceAndPath(...)` with `Identifier.fromNamespaceAndPath(...)` and change import.

### Stairs/Slab/Wall Variant (all groups)

```java
// Stairs — Group A
public class TemporalStoneStairs extends StairBlock {
    public TemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_STONE.get().defaultBlockState(), properties);
    }
    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBlock.createProperties();
    }
}

// Stairs — Group B (adds .setId for the variant's own ID)
public static BlockBehaviour.Properties createProperties() {
    return TemporalStoneBlock.createProperties()
            .setId(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stone_stairs")));
}
```

Slab/Wall are identical patterns (extend `SlabBlock` / `WallBlock`, no base state argument).

### Redstone Ore (lit behavior)

```java
// Group A
public class TemporalRedstoneOre extends RedStoneOreBlock {
    public TemporalRedstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .randomTicks()
                .lightLevel(state -> state.getValue(RedStoneOreBlock.LIT) ? 9 : 0)
                .isRedstoneConductor((state, getter, pos) -> false);
    }
}
```

For Group B/C, use `Properties.ofFullCopy(Blocks.REDSTONE_ORE)` which inherits all the lit behavior automatically.

---

## Registration Patterns

### ModBlocks.java (identical across all 11 versions)

```java
public static final RegistrySupplier<Block> TEMPORAL_STONE = BLOCKS.register(
    ModBlockId.TEMPORAL_STONE.id(),
    () -> new TemporalStoneBlock(TemporalStoneBlock.createProperties())
);
```

### ModItems.java Pattern A (1.20.1, 1.21.1)

```java
public static final RegistrySupplier<Item> TEMPORAL_STONE = ITEMS.register(
    ModItemId.TEMPORAL_STONE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_STONE.get(), new Item.Properties())
);
```

### ModItems.java Pattern B (1.21.2–1.21.10)

```java
public static final RegistrySupplier<Item> TEMPORAL_STONE = ITEMS.register(
    ModItemId.TEMPORAL_STONE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_STONE.get(), new Item.Properties()
            .useBlockDescriptionPrefix()
            .setId(ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_STONE.id()))))
);
```

### ModItems.java Pattern C (1.21.11)

Same as B but `Identifier.fromNamespaceAndPath(...)`.

### Creative tab

In `populateCreativeTab()` method:
```java
output.accept(TEMPORAL_STONE.get());
```

---

## Resource Templates

### Blockstate: Simple block

```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/temporal_stone"
    }
  }
}
```

### Blockstate: Slab

```json
{
  "variants": {
    "type=bottom": { "model": "chronodawn:block/temporal_stone_slab" },
    "type=double": { "model": "chronodawn:block/temporal_stone" },
    "type=top": { "model": "chronodawn:block/temporal_stone_slab_top" }
  }
}
```

### Blockstate: Redstone ore (lit state)

```json
{
  "variants": {
    "lit=false": { "model": "chronodawn:block/temporal_redstone_ore" },
    "lit=true": { "model": "chronodawn:block/temporal_redstone_ore" }
  }
}
```

### Blockstate: Stairs / Wall

Copy exact structure from `clockstone_stairs.json` / `clockstone_wall.json` and rename model references. Stairs has ~209 lines of facing/half/shape combinations, wall has multipart structure.

### Block model: Simple cube

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "chronodawn:block/temporal_stone" }
}
```

### Block model: Stairs (3 files: stairs, stairs_inner, stairs_outer)

```json
{
  "parent": "minecraft:block/stairs",
  "textures": {
    "bottom": "chronodawn:block/temporal_stone",
    "top": "chronodawn:block/temporal_stone",
    "side": "chronodawn:block/temporal_stone"
  }
}
```

Same pattern with `inner_stairs` / `outer_stairs` parent.

### Block model: Slab (2 files: slab, slab_top)

```json
{
  "parent": "minecraft:block/slab",
  "textures": {
    "bottom": "chronodawn:block/temporal_stone",
    "top": "chronodawn:block/temporal_stone",
    "side": "chronodawn:block/temporal_stone"
  }
}
```

### Block model: Wall (3 files: post, side, side_tall)

```json
{
  "parent": "minecraft:block/template_wall_post",
  "textures": { "wall": "chronodawn:block/temporal_stone" }
}
```

### Item model: Regular block

```json
{ "parent": "chronodawn:block/temporal_stone" }
```

### Item model: Wall (special - uses wall_inventory)

```json
{
  "parent": "minecraft:block/wall_inventory",
  "textures": { "wall": "chronodawn:block/temporal_stone" }
}
```

### Client Items JSON (1.21.4+)

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/temporal_stone"}}
```

---

## Loot Table Templates

### Simple drop-self

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        { "type": "minecraft:item", "name": "chronodawn:temporal_stone" }
      ],
      "conditions": [{ "condition": "minecraft:survives_explosion" }]
    }
  ]
}
```

### Stone → Cobblestone (silk touch drops self)

Use the **new nested predicate format** (see Critical Gotchas):

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
              "name": "chronodawn:temporal_stone",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "predicates": {
                      "minecraft:enchantments": [
                        {
                          "enchantments": "minecraft:silk_touch",
                          "levels": { "min": 1 }
                        }
                      ]
                    }
                  }
                }
              ]
            },
            { "type": "minecraft:item", "name": "chronodawn:temporal_cobblestone" }
          ]
        }
      ],
      "conditions": [{ "condition": "minecraft:survives_explosion" }]
    }
  ]
}
```

### Slab (double slab → 2 drops)

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "function": "minecraft:set_count",
              "conditions": [{
                "condition": "minecraft:block_state_property",
                "block": "chronodawn:temporal_stone_slab",
                "properties": { "type": "double" }
              }],
              "count": 2,
              "add": false
            },
            { "function": "minecraft:explosion_decay" }
          ],
          "name": "chronodawn:temporal_stone_slab"
        }
      ]
    }
  ]
}
```

### Ore with Fortune (coal example)

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
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
                    "predicates": {
                      "minecraft:enchantments": [
                        { "enchantments": "minecraft:silk_touch", "levels": { "min": 1 } }
                      ]
                    }
                  }
                }
              ],
              "name": "chronodawn:temporal_coal_ore"
            },
            {
              "type": "minecraft:item",
              "functions": [
                {
                  "function": "minecraft:apply_bonus",
                  "enchantment": "minecraft:fortune",
                  "formula": "minecraft:ore_drops"
                },
                { "function": "minecraft:explosion_decay" }
              ],
              "name": "minecraft:coal"
            }
          ]
        }
      ]
    }
  ]
}
```

For redstone_ore, use `minecraft:set_count` with uniform 4-5 and `uniform_bonus_count` Fortune formula (see `temporal_redstone_ore.json`).

---

## Worldgen Integration

### Replace dimension base block

Edit `common/<ver>/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json` in **all 11 versions**:

```json
"default_block": { "Name": "chronodawn:temporal_stone" }
```

### Ore configured feature (target temporal_stone)

Edit `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_<name>.json`:

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 9,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:temporal_stone" },
        "state": { "Name": "chronodawn:temporal_iron_ore" }
      }
    ]
  }
}
```

### Add ore to biomes

Update biome JSON `features` array at index 6 (ore generation step) in **all 3 locations**:
- `common/1.21.1/.../biome/*.json` (10 files)
- `common/shared-1.21.2+/.../biome/*.json` (10 files)
- `common/1.21.11/.../biome/*.json` (9 files — **DO NOT FORGET**)
- `common/1.20.1/.../biome/*.json` (10 files, if supporting 1.20.1)

---

## Verification Steps

```bash
./gradlew validateResources      # JSON cross-references
./gradlew validateTranslations   # translation key parity
./gradlew build1_21_11           # spot build latest version
./gradlew buildAll               # all 11 versions
./gradlew testAll                # including CreativeTabCompletenessTest
```

If `buildAll` fails, check for unintended Java file modifications:
```bash
git diff --name-only -- '*.java'
```

---

## Common Mistakes

| Mistake | Symptom | Fix |
|---|---|---|
| Old silk touch format | Block never drops alternative (always drops self or nothing) | Use nested `predicates.minecraft:enchantments` format |
| Missing tier tag | Wood pickaxe mines iron/gold/redstone ore | Add to `needs_stone_tool` / `needs_iron_tool` |
| Forgot 1.21.11 biomes | Ore doesn't generate in 1.21.11 only | Update `common/1.21.11/.../biome/*.json` |
| Missing Client Items JSON | Item shows as missing model (purple/black) in 1.21.4+ | Add files in `1.21.4/items/` and `shared-1.21.5+/items/` |
| Missing creative tab entry | CreativeTabCompletenessTest fails | Add `output.accept(...)` in `populateCreativeTab()` |
| Subagent modified existing file | `buildAll` fails with unrelated compilation errors | `git checkout --` the unrelated files |
| Wrong ModItems pattern per version | Build fails in 1.21.11 only | Use `Identifier` not `ResourceLocation` for 1.21.11 |
| Wrong `setId` on variant | Registration conflict | Each variant's `createProperties()` must have its OWN `.setId(variant_id)` |

---

## Related Skills

- `multiversion-support` — Multi-version API differences
- `texture-creation` — Vanilla texture reuse patterns
- `noise-settings` — Detailed noise settings customization
- `structure-worldgen` — Worldgen structure integration
- `build-troubleshooting` — Build/test failure diagnosis
