# Temporal Ore Blocks + Dimension Terrain Update Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 4 temporal ore blocks (coal, gold, iron, redstone), replace dimension terrain with temporal_stone, and update all ore worldgen to target temporal_stone.

**Architecture:** Follow existing ClockstoneOre pattern for simple ore blocks. Temporal Redstone Ore extends vanilla `RedStoneOreBlock` for lit behavior. Worldgen configured_features updated to target `chronodawn:temporal_stone`. Noise settings updated per-version. Biome JSONs updated for iron ore restoration in 1.21.1+.

**Tech Stack:** Java 21, Architectury API, Minecraft modding

---

## Version Groups

| Group | Versions | Key Differences |
|---|---|---|
| A | 1.20.1, 1.21.1 | `Properties.of()` with explicit props, no `setId()` |
| B | 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10 | `Properties.ofFullCopy()` + `.setId(ResourceKey...)` with `ResourceLocation` |
| C | 1.21.11 | Same as B but `Identifier` instead of `ResourceLocation` |

---

### Task 1: Register Block and Item IDs

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Add block IDs to ModBlockId.java**

Add near existing ore entries (after `TIME_CRYSTAL_ORE`):

```java
// Temporal Ore variants
TEMPORAL_COAL_ORE(def("temporal_coal_ore")),
TEMPORAL_GOLD_ORE(def("temporal_gold_ore")),
TEMPORAL_IRON_ORE(def("temporal_iron_ore")),
TEMPORAL_REDSTONE_ORE(def("temporal_redstone_ore")),
```

- [ ] **Step 2: Add item IDs to ModItemId.java**

Add matching entries:

```java
// Temporal Ore variants
TEMPORAL_COAL_ORE("temporal_coal_ore"),
TEMPORAL_GOLD_ORE("temporal_gold_ore"),
TEMPORAL_IRON_ORE("temporal_iron_ore"),
TEMPORAL_REDSTONE_ORE("temporal_redstone_ore"),
```

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat: add temporal ore block and item IDs"
```

---

### Task 2: Create Ore Block Classes (Group A: 1.20.1, 1.21.1)

**Files (per version, 4 files each):**
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCoalOre.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalGoldOre.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalIronOre.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalRedstoneOre.java`

- [ ] **Step 1: Create TemporalCoalOre.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TemporalCoalOre extends Block {
    public TemporalCoalOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
```

- [ ] **Step 2: Create TemporalGoldOre.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TemporalGoldOre extends Block {
    public TemporalGoldOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
```

- [ ] **Step 3: Create TemporalIronOre.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TemporalIronOre extends Block {
    public TemporalIronOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
```

- [ ] **Step 4: Create TemporalRedstoneOre.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

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

- [ ] **Step 5: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/blocks/Temporal*Ore.java
git add common/1.21.1/src/main/java/com/chronodawn/blocks/Temporal*Ore.java
git commit -m "feat: add temporal ore block classes for 1.20.1 and 1.21.1"
```

---

### Task 3: Create Ore Block Classes (Group B: 1.21.2–1.21.10)

**Files:** 4 block classes per version for 8 versions.

- [ ] **Step 1: Create TemporalCoalOre.java for Group B**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCoalOre extends Block {
    public TemporalCoalOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_coal_ore")));
    }
}
```

- [ ] **Step 2: Create TemporalGoldOre.java for Group B**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalGoldOre extends Block {
    public TemporalGoldOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_gold_ore")));
    }
}
```

- [ ] **Step 3: Create TemporalIronOre.java for Group B**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalIronOre extends Block {
    public TemporalIronOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_iron_ore")));
    }
}
```

- [ ] **Step 4: Create TemporalRedstoneOre.java for Group B**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalRedstoneOre extends RedStoneOreBlock {
    public TemporalRedstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_redstone_ore")));
    }
}
```

- [ ] **Step 5: Copy to all Group B versions (1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10)**

- [ ] **Step 6: Commit**

```bash
git add common/1.21.{2,4,5,6,7,8,9,10}/src/main/java/com/chronodawn/blocks/Temporal*Ore.java
git commit -m "feat: add temporal ore block classes for 1.21.2-1.21.10"
```

---

### Task 4: Create Ore Block Classes (Group C: 1.21.11)

**Files:** 4 block classes in `common/1.21.11/`

Same as Group B but replace `ResourceLocation` → `Identifier`:
- `import net.minecraft.resources.ResourceLocation;` → `import net.minecraft.resources.Identifier;`
- `ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`

- [ ] **Step 1: Create all 4 files for 1.21.11**

TemporalCoalOre, TemporalGoldOre, TemporalIronOre: same as Group B with `Identifier`.

TemporalRedstoneOre:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalRedstoneOre extends RedStoneOreBlock {
    public TemporalRedstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_redstone_ore")));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/Temporal*Ore.java
git commit -m "feat: add temporal ore block classes for 1.21.11"
```

---

### Task 5: Register Blocks in ModBlocks.java (All Versions)

**Files:** Modify `common/<ver>/src/main/java/com/chronodawn/registry/ModBlocks.java` for all 11 versions.

- [ ] **Step 1: Add registrations and imports to all versions**

Add after existing `TIME_CRYSTAL_ORE` registration:

```java
// Temporal Ore variants
public static final RegistrySupplier<Block> TEMPORAL_COAL_ORE = BLOCKS.register(
    ModBlockId.TEMPORAL_COAL_ORE.id(),
    () -> new TemporalCoalOre(TemporalCoalOre.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_GOLD_ORE = BLOCKS.register(
    ModBlockId.TEMPORAL_GOLD_ORE.id(),
    () -> new TemporalGoldOre(TemporalGoldOre.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_IRON_ORE = BLOCKS.register(
    ModBlockId.TEMPORAL_IRON_ORE.id(),
    () -> new TemporalIronOre(TemporalIronOre.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_REDSTONE_ORE = BLOCKS.register(
    ModBlockId.TEMPORAL_REDSTONE_ORE.id(),
    () -> new TemporalRedstoneOre(TemporalRedstoneOre.createProperties())
);
```

Add imports:
```java
import com.chronodawn.blocks.TemporalCoalOre;
import com.chronodawn.blocks.TemporalGoldOre;
import com.chronodawn.blocks.TemporalIronOre;
import com.chronodawn.blocks.TemporalRedstoneOre;
```

- [ ] **Step 2: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModBlocks.java
git commit -m "feat: register temporal ore blocks in ModBlocks for all versions"
```

---

### Task 6: Register Block Items in ModItems.java (All Versions)

**Files:** Modify `common/<ver>/src/main/java/com/chronodawn/registry/ModItems.java` for all 11 versions.

- [ ] **Step 1: Add item registrations (Pattern A for 1.20.1/1.21.1)**

```java
// Temporal Ore variants
public static final RegistrySupplier<Item> TEMPORAL_COAL_ORE = ITEMS.register(
    ModItemId.TEMPORAL_COAL_ORE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_COAL_ORE.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_GOLD_ORE = ITEMS.register(
    ModItemId.TEMPORAL_GOLD_ORE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_GOLD_ORE.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_IRON_ORE = ITEMS.register(
    ModItemId.TEMPORAL_IRON_ORE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_IRON_ORE.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_REDSTONE_ORE = ITEMS.register(
    ModItemId.TEMPORAL_REDSTONE_ORE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_REDSTONE_ORE.get(), new Item.Properties())
);
```

- [ ] **Step 2: Add item registrations (Pattern B for 1.21.2–1.21.10 with ResourceLocation, Pattern C for 1.21.11 with Identifier)**

Same pattern as temporal stone blocks — `.useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, ...))`.

- [ ] **Step 3: Add creative tab entries in all versions**

In `populateCreativeTab()`, add after existing ore entries:

```java
output.accept(TEMPORAL_COAL_ORE.get());
output.accept(TEMPORAL_GOLD_ORE.get());
output.accept(TEMPORAL_IRON_ORE.get());
output.accept(TEMPORAL_REDSTONE_ORE.get());
```

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register temporal ore block items for all versions"
```

---

### Task 7: Create Textures

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_coal_ore.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_gold_ore.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_iron_ore.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_redstone_ore.png`

- [ ] **Step 1: Create placeholder textures using Python/PIL**

Color-transform vanilla ore textures with temporal purple/blue tint, or create procedural 16x16 ore textures.

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_*_ore.png
git commit -m "feat: add temporal ore textures (placeholder)"
```

---

### Task 8: Create Blockstate and Model JSONs

**Files:** All in `common/shared/src/main/resources/assets/chronodawn/`

- [ ] **Step 1: Create blockstate JSONs (4 files)**

For temporal_coal_ore, temporal_gold_ore, temporal_iron_ore — simple single-variant:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/temporal_coal_ore"
    }
  }
}
```

For temporal_redstone_ore — two variants (lit=true/false):
```json
{
  "variants": {
    "lit=false": {
      "model": "chronodawn:block/temporal_redstone_ore"
    },
    "lit=true": {
      "model": "chronodawn:block/temporal_redstone_ore"
    }
  }
}
```

- [ ] **Step 2: Create block model JSONs (4 files)**

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/temporal_coal_ore"
  }
}
```

Same pattern for all 4 ore block models.

- [ ] **Step 3: Create item model JSONs (4 files in common/shared/models/item/)**

```json
{
  "parent": "chronodawn:block/temporal_coal_ore"
}
```

- [ ] **Step 4: Create Client Items JSONs (4 files each in common/1.21.4/items/ and common/shared-1.21.5+/items/)**

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/temporal_coal_ore"}}
```

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_*_ore.json
git add common/shared/src/main/resources/assets/chronodawn/models/block/temporal_*_ore.json
git add common/shared/src/main/resources/assets/chronodawn/models/item/temporal_*_ore.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_*_ore.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_*_ore.json
git commit -m "feat: add temporal ore blockstate, model, and item JSONs"
```

---

### Task 9: Create Loot Tables

**Files:**
- 1.20.1: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/` — 4 files
- 1.21.1+: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/` — 4 files

- [ ] **Step 1: Create temporal_coal_ore.json (silk touch → self, otherwise → minecraft:coal with Fortune)**

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
                        {
                          "enchantments": "minecraft:silk_touch",
                          "levels": { "min": 1 }
                        }
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

- [ ] **Step 2: Create temporal_gold_ore.json (silk touch → self, otherwise → minecraft:raw_gold with Fortune)**

Same structure, `"name": "chronodawn:temporal_gold_ore"` for silk touch, `"name": "minecraft:raw_gold"` for normal drop.

- [ ] **Step 3: Create temporal_iron_ore.json (silk touch → self, otherwise → minecraft:raw_iron with Fortune)**

Same structure, `"name": "chronodawn:temporal_iron_ore"` for silk touch, `"name": "minecraft:raw_iron"` for normal drop.

- [ ] **Step 4: Create temporal_redstone_ore.json (silk touch → self, otherwise → minecraft:redstone 1-5 with Fortune)**

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
                        {
                          "enchantments": "minecraft:silk_touch",
                          "levels": { "min": 1 }
                        }
                      ]
                    }
                  }
                }
              ],
              "name": "chronodawn:temporal_redstone_ore"
            },
            {
              "type": "minecraft:item",
              "functions": [
                {
                  "function": "minecraft:set_count",
                  "count": { "type": "minecraft:uniform", "min": 4, "max": 5 }
                },
                {
                  "function": "minecraft:apply_bonus",
                  "enchantment": "minecraft:fortune",
                  "formula": "minecraft:uniform_bonus_count",
                  "parameters": { "bonusMultiplier": 1 }
                },
                { "function": "minecraft:explosion_decay" }
              ],
              "name": "minecraft:redstone"
            }
          ]
        }
      ]
    }
  ]
}
```

- [ ] **Step 5: Copy all 4 to both loot table directories (1.20.1 and 1.21.1+)**

- [ ] **Step 6: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_*_ore.json
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_*_ore.json
git commit -m "feat: add temporal ore loot tables"
```

---

### Task 10: Update Mining Tags and Translations

**Files:**
- Tags: pickaxe mineable tags (1.20.1 and 1.21.1+)
- Lang: en_us, ja_jp (1.20.1, 1.21.1, shared-1.21.2+)

- [ ] **Step 1: Add to pickaxe tags**

Add to both tag files:
```json
"chronodawn:temporal_coal_ore",
"chronodawn:temporal_gold_ore",
"chronodawn:temporal_iron_ore",
"chronodawn:temporal_redstone_ore"
```

- [ ] **Step 2: Add English translations (1.20.1, 1.21.1, shared-1.21.2+)**

```json
"block.chronodawn.temporal_coal_ore": "Temporal Coal Ore",
"block.chronodawn.temporal_gold_ore": "Temporal Gold Ore",
"block.chronodawn.temporal_iron_ore": "Temporal Iron Ore",
"block.chronodawn.temporal_redstone_ore": "Temporal Redstone Ore"
```

- [ ] **Step 3: Add Japanese translations (1.20.1, 1.21.1, shared-1.21.2+)**

```json
"block.chronodawn.temporal_coal_ore": "テンポラル石炭鉱石",
"block.chronodawn.temporal_gold_ore": "テンポラル金鉱石",
"block.chronodawn.temporal_iron_ore": "テンポラル鉄鉱石",
"block.chronodawn.temporal_redstone_ore": "テンポラルレッドストーン鉱石"
```

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json
git add common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json
git add common/1.20.1/src/main/resources/assets/chronodawn/lang/*.json
git add common/1.21.1/src/main/resources/assets/chronodawn/lang/*.json
git add common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: add temporal ore mining tags and translations"
```

---

### Task 11: Update Noise Settings (dimension terrain → temporal_stone)

**Files:** Modify `common/<ver>/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json` for ALL 11 versions.

- [ ] **Step 1: Change default_block in all noise settings**

In each file, change:
```json
"default_block": { "Name": "minecraft:stone" }
```
to:
```json
"default_block": { "Name": "chronodawn:temporal_stone" }
```

- [ ] **Step 2: Commit**

```bash
git add common/*/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json
git commit -m "feat: replace dimension default_block with temporal_stone"
```

---

### Task 12: Update Configured Features (ore targets → temporal_stone)

**Files:** Modify 6 files in `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/`

- [ ] **Step 1: Update ore_coal.json**

Change targets to single entry (remove deepslate):
```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 17,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:temporal_stone" },
        "state": { "Name": "chronodawn:temporal_coal_ore" }
      }
    ]
  }
}
```

- [ ] **Step 2: Update ore_gold.json**

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 9,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:temporal_stone" },
        "state": { "Name": "chronodawn:temporal_gold_ore" }
      }
    ]
  }
}
```

- [ ] **Step 3: Update ore_iron.json**

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

- [ ] **Step 4: Update ore_redstone.json**

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 8,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:temporal_stone" },
        "state": { "Name": "chronodawn:temporal_redstone_ore" }
      }
    ]
  }
}
```

- [ ] **Step 5: Update ore_clockstone.json (target only, ore block unchanged)**

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 9,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:temporal_stone" },
        "state": { "Name": "chronodawn:clockstone_ore" }
      }
    ]
  }
}
```

- [ ] **Step 6: Update ore_time_crystal.json (target only, ore block unchanged)**

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 4,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:temporal_stone" },
        "state": { "Name": "chronodawn:time_crystal_ore" }
      }
    ]
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_*.json
git commit -m "feat: update ore configured features to target temporal_stone"
```

---

### Task 13: Add ore_iron to 1.21.1+ Biome JSONs

**Files:**
- Modify: `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/` — all 10 biome files
- Modify: `common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/` — all 10 biome files

- [ ] **Step 1: Add `"chronodawn:ore_iron"` to the ore features list (generation step index 6) in all 20 biome files**

For each biome, find the array at index 6 of the `features` list and add `"chronodawn:ore_iron"` alongside the existing ore entries.

Example for `chronodawn_plains.json`:
```json
[
  "chronodawn:ore_gold",
  "chronodawn:ore_redstone",
  "chronodawn:ore_time_crystal",
  "chronodawn:ore_clockstone",
  "chronodawn:ore_coal",
  "chronodawn:ore_iron",
  "chronodawn:clockwork_block_cluster"
]
```

For snowy/swamp/mountain (which lack coal and time_crystal):
```json
[
  "chronodawn:ore_gold",
  "chronodawn:ore_redstone",
  "chronodawn:ore_clockstone",
  "chronodawn:ore_iron"
]
```

- [ ] **Step 2: Commit**

```bash
git add common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/*.json
git add common/shared-1.21.2+/src/main/resources/data/chronodawn/worldgen/biome/*.json
git commit -m "feat: add ore_iron to all 1.21.1+ biome feature lists"
```

---

### Task 14: Build Verification

- [ ] **Step 1: Validate resources**

```bash
./gradlew validateResources
```

- [ ] **Step 2: Validate translations**

```bash
./gradlew validateTranslations
```

- [ ] **Step 3: Build all versions**

```bash
./gradlew buildAll
```

- [ ] **Step 4: Run all tests**

```bash
./gradlew testAll
```
