# Chronite Ore and the Overworld Path to Ancient Ruins Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an Overworld ore, Chronite, whose shard crafts a Time Compass that points at Ancient Ruins, so a player can always reach the mod's entry structure without changing its worldgen density.

**Architecture:** Chronite Ore generates in vanilla Overworld stone and deepslate via per-loader biome injection. Its shard is a low-tier reagent with no gear recipes, consumed permanently through an alternate Time Arrow recipe. `TimeCompassItem` gains a fallback that targets Ancient Ruins when an untargeted compass is used in the Overworld, which keeps the compass recipe free of NBT and component era splits.

**Tech Stack:** Java 21 / Java 25, Architectury, Fabric API, NeoForge, Gradle (Groovy DSL), Mojang mappings (unobfuscated names on 26.x).

**Spec:** `docs/superpowers/specs/2026-09-13-chronite-ore-design.md`

## Global Constraints

- **Worktree:** all work happens in `.worktrees/feature-chronite-ore` on branch `feature/chronite-ore`. Never edit the main checkout. Confirm with `pwd` before every command.
- **Thirteen version modules**, every one of them, every time a per-version file is touched: `1.20.1`, `1.21.1`, `1.21.2`, `1.21.4`, `1.21.5`, `1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11`, `26.1.2`, `26.2`. There is no `1.21.3` module; it reuses `1.21.2`.
- **Verify repeated identical edits by hash, not by reading.** After editing N copies of the same file, run `md5 common/*/src/main/java/.../File.java` and confirm the expected grouping. Reading thirteen files linearly is how copies get missed.
- **Never state a module count from memory.** Re-derive it with `ls -d common/*/` when it matters.
- **Item and Block `createProperties()` splits into three eras.** No `.setId(...)` on `1.20.1` and `1.21.1`. `.setId(...)` with `ResourceLocation.fromNamespaceAndPath` on `1.21.2` through `1.21.11`. `.setId(...)` with `Identifier.fromNamespaceAndPath` on `26.1.2` and `26.2`.
- **Resource paths split by era.** Recipes: `common/1.20.1/.../data/chronodawn/recipes/`, `common/1.21.1/.../data/chronodawn/recipe/`, `common/shared-1.21.2+/.../data/chronodawn/recipe/`. Recipe advancements: `common/1.20.1/.../data/chronodawn/advancements/recipes/`, `common/shared-1.21.1+/.../data/chronodawn/advancement/recipes/`. Loot: `common/1.20.1/.../data/chronodawn/loot_tables/blocks/`, `common/shared-1.21.1+/.../data/chronodawn/loot_table/blocks/`. Block tags: `common/1.20.1/.../data/minecraft/tags/blocks/`, `common/shared-1.21.1+/.../data/minecraft/tags/block/`. Lang: `common/1.20.1/`, `common/1.21.1/`, `common/shared-1.21.2+/`.
- **Client item models are mandatory on 1.21.4 and later.** Every item needs `assets/chronodawn/items/<id>.json` in both `common/1.21.4/` and `common/shared-1.21.5+/`. A missing one renders as a purple and black square rather than failing the build.
- **Edit lang JSON line by line.** Do not round-trip through `json.dumps`; it strips the blank-line grouping the files rely on.
- **All documentation and code comments are English.** Conversation with the user stays Japanese.
- **No em dashes and no semicolons in prose**, in any language. Split the sentence or use a comma.
- **Do not run `gameTestAll` from this worktree.** It leaks into the main checkout. Run it from the main checkout after the branch merges, or run per-version GameTest tasks here.
- **One `./gradlew` invocation per Bash call**, with one `-Ptarget_mc_version` at a time. Chained or looped invocations produce spurious failures.
- **Commit at the end of every task.** The user has asked to review before pushing, so never push.

---

### Task 1: Chronite Shard item

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`
- Create: `common/<each of 13>/src/main/java/com/chronodawn/items/base/ChroniteShardItem.java`
- Modify: `common/<each of 13>/src/main/java/com/chronodawn/registry/ModItems.java`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/chronite_shard.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/chronite_shard.png`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/chronite_shard.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/chronite_shard.json`
- Modify: `common/1.20.1/src/main/resources/assets/chronodawn/lang/{en_us,ja_jp}.json`
- Modify: `common/1.21.1/src/main/resources/assets/chronodawn/lang/{en_us,ja_jp}.json`
- Modify: `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/{en_us,ja_jp}.json`

**Interfaces:**
- Consumes: nothing.
- Produces: `ModItemId.CHRONITE_SHARD` with id string `"chronite_shard"`; `ModItems.CHRONITE_SHARD` of type `RegistrySupplier<Item>`; `ChroniteShardItem.createProperties()` returning `Item.Properties`. Later tasks reference the item as `chronodawn:chronite_shard`.

- [ ] **Step 1: Add the ID constant**

In `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`, next to `RAW_TEMPORAL_AMBER` and `TEMPORAL_AMBER_DUST` (around line 229), add:

```java
    CHRONITE_SHARD("chronite_shard"),
```

- [ ] **Step 2: Create the item class for the no-setId era (1.20.1, 1.21.1)**

Create `common/1.20.1/src/main/java/com/chronodawn/items/base/ChroniteShardItem.java` and the identical file under `common/1.21.1/`:

```java
package com.chronodawn.items.base;

import net.minecraft.world.item.Item;

/**
 * Chronite Shard - low-tier temporal reagent mined in the Overworld.
 *
 * The Chrono Dawn's temporal field bleeds into Overworld bedrock and
 * crystallises as Chronite. A Time Compass built from the shards senses
 * the place where the two worlds touched, the Ancient Ruins.
 *
 * Deliberately has no armour, tool, or weapon recipes. The shard form
 * signals a reagent rather than a gear material, so its abundance cannot
 * lower the difficulty of the game.
 */
public class ChroniteShardItem extends Item {
    public ChroniteShardItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64);
    }
}
```

- [ ] **Step 3: Create the item class for the ResourceLocation setId era**

Create the same file under `common/1.21.2/`, `common/1.21.4/`, `common/1.21.5/`, `common/1.21.6/`, `common/1.21.7/`, `common/1.21.8/`, `common/1.21.9/`, `common/1.21.10/`, `common/1.21.11/`, with this body and the same Javadoc as Step 2:

```java
package com.chronodawn.items.base;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ChroniteShardItem extends Item {
    public ChroniteShardItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronite_shard")));
    }
}
```

- [ ] **Step 4: Create the item class for the Identifier setId era (26.1.2, 26.2)**

Same as Step 3 but with `Identifier` in place of `ResourceLocation`, matching `common/26.2/src/main/java/com/chronodawn/items/base/ClockstoneItem.java`:

```java
import net.minecraft.resources.Identifier;
...
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronite_shard")));
```

Copy the exact import line for `Identifier` from `common/26.2/src/main/java/com/chronodawn/items/base/ClockstoneItem.java` rather than guessing the package.

- [ ] **Step 5: Verify the three era groups by hash**

Run:

```bash
md5 common/*/src/main/java/com/chronodawn/items/base/ChroniteShardItem.java
```

Expected: exactly three distinct hashes. Two files share the first (1.20.1, 1.21.1), nine share the second, two share the third. If you see more than three, a copy diverged.

- [ ] **Step 6: Register the item in all thirteen ModItems**

In each `common/<version>/src/main/java/com/chronodawn/registry/ModItems.java`, immediately after the `CLOCKSTONE` registration block, add:

```java
    /**
     * Chronite Shard - Overworld reagent used to craft the Time Compass,
     * an alternate Time Arrow recipe, and Chronite Blocks.
     */
    public static final RegistrySupplier<Item> CHRONITE_SHARD = ITEMS.register(
        ModItemId.CHRONITE_SHARD.id(),
        () -> new ChroniteShardItem(ChroniteShardItem.createProperties())
    );
```

Add the import `com.chronodawn.items.base.ChroniteShardItem` alongside the existing `ClockstoneItem` import in the same file.

- [ ] **Step 7: Add the item to the creative tab in all thirteen ModItems**

In the same files, find the line `output.accept(CLOCKSTONE.get());` and add directly after it:

```java
        output.accept(CHRONITE_SHARD.get());
```

`CreativeTabCompletenessTest` fails if an item is neither in the tab nor on its exclusion list, so this step is what keeps that test green.

- [ ] **Step 8: Create the item model**

`common/shared/src/main/resources/assets/chronodawn/models/item/chronite_shard.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:item/chronite_shard"
  }
}
```

`item/generated` is required for a flat icon. A custom geometry parent renders dark and flat.

- [ ] **Step 9: Create the client item definitions for 1.21.4 and later**

Both `common/1.21.4/src/main/resources/assets/chronodawn/items/chronite_shard.json` and `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/chronite_shard.json`:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "chronodawn:item/chronite_shard"
  }
}
```

Match the exact shape of `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/clockstone_ore.json`; if it differs from the above, that file wins.

- [ ] **Step 10: Create the item texture**

Produce a 16x16 `common/shared/src/main/resources/assets/chronodawn/textures/item/chronite_shard.png`. Start from the existing `textures/item/time_crystal.png` silhouette so the art reads as part of the set, and shift the hue to a distinct pale amber so it is not confused with Clockstone's light blue or Time Crystal's colour. Confirm the file is exactly 16x16:

```bash
python3 -c "from PIL import Image; im=Image.open('common/shared/src/main/resources/assets/chronodawn/textures/item/chronite_shard.png'); print(im.size, im.mode)"
```

Expected: `(16, 16) RGBA`

- [ ] **Step 11: Add translations**

Add to all six lang files listed under **Files**, editing line by line and keeping the existing blank-line grouping:

`en_us.json`: `"item.chronodawn.chronite_shard": "Chronite Shard",`
`ja_jp.json`: `"item.chronodawn.chronite_shard": "クロナイトの欠片",`

- [ ] **Step 12: Verify**

```bash
./gradlew validateResources
```

Expected: BUILD SUCCESSFUL, with no unresolved model or texture reference for `chronite_shard`.

```bash
./gradlew validateTranslations
```

Expected: BUILD SUCCESSFUL.

```bash
./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1
```

Expected: BUILD SUCCESSFUL. `CreativeTabCompletenessTest` covers the new item.

```bash
./gradlew build1_20_1
```

Expected: BUILD SUCCESSFUL. This is the era without `.setId`, so it catches a wrongly copied item class.

```bash
./gradlew build26_2
```

Expected: BUILD SUCCESSFUL. This is the `Identifier` era.

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "feat(items): add Chronite Shard"
```

---

### Task 2: Chronite Ore blocks

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`
- Create: `common/<each of 13>/src/main/java/com/chronodawn/blocks/{ChroniteOre,DeepslateChroniteOre}.java`
- Modify: `common/<each of 13>/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/<each of 13>/src/main/java/com/chronodawn/registry/ModItems.java`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/{chronite_ore,deepslate_chronite_ore}.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/{chronite_ore,deepslate_chronite_ore}.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/{chronite_ore,deepslate_chronite_ore}.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/{chronite_ore,deepslate_chronite_ore}.png`
- Create: `common/1.21.4/` and `common/shared-1.21.5+/` `assets/chronodawn/items/{chronite_ore,deepslate_chronite_ore}.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/{chronite_ore,deepslate_chronite_ore}.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/{chronite_ore,deepslate_chronite_ore}.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/{mineable/pickaxe,needs_stone_tool}.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/{mineable/pickaxe,needs_stone_tool}.json`
- Modify: the six lang files

**Interfaces:**
- Consumes: `chronodawn:chronite_shard` from Task 1.
- Produces: blocks `chronodawn:chronite_ore` and `chronodawn:deepslate_chronite_ore`; `ModBlocks.CHRONITE_ORE` and `ModBlocks.DEEPSLATE_CHRONITE_ORE` as `RegistrySupplier<Block>`. Task 4 places these block states from its configured feature.

- [ ] **Step 1: Add the ID constants**

In `ModBlockId.java`, beside `CLOCKSTONE_ORE` (line 41) and `DEEPSLATE_CLOCKSTONE_ORE` (line 58):

```java
    CHRONITE_ORE(def("chronite_ore")),
    DEEPSLATE_CHRONITE_ORE(def("deepslate_chronite_ore")),
```

In `ModItemId.java`, beside the matching ore item entries:

```java
    CHRONITE_ORE("chronite_ore"),
    DEEPSLATE_CHRONITE_ORE("deepslate_chronite_ore"),
```

- [ ] **Step 2: Create the block classes for 1.20.1 and 1.21.1**

`common/1.20.1/src/main/java/com/chronodawn/blocks/ChroniteOre.java`, duplicated to `common/1.21.1/`:

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Chronite Ore - Overworld ore that drops Chronite Shards.
 *
 * Softer than Clockstone Ore and mineable with a stone pickaxe, because an
 * early player must be able to reach the Time Compass reliably.
 */
public class ChroniteOre extends Block {
    public ChroniteOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f) // hardness, blast resistance
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
```

`DeepslateChroniteOre.java` in the same two modules is identical except for the class name, `MapColor.DEEPSLATE`, `.strength(4.5f, 3.0f)`, and `.sound(SoundType.DEEPSLATE)`, matching vanilla's deepslate ore values.

- [ ] **Step 3: Create the block classes for 1.21.2 through 1.21.11**

Same Javadoc, with the `ofFullCopy` and `setId` form used by `common/1.21.2/src/main/java/com/chronodawn/blocks/ClockstoneOre.java`:

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ChroniteOre extends Block {
    public ChroniteOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .strength(3.0f, 3.0f) // hardness, blast resistance
                .requiresCorrectToolForDrops()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronite_ore")));
    }
}
```

`ofFullCopy` carries the copied block's strength, so the explicit `.strength(...)` after it is required, not decorative. Dropping it silently gives the block vanilla stone's hardness.

The deepslate variant uses `Blocks.DEEPSLATE`, `.strength(4.5f, 3.0f)`, and the id `"deepslate_chronite_ore"`.

- [ ] **Step 4: Create the block classes for 26.1.2 and 26.2**

Same as Step 3 with `Identifier` in place of `ResourceLocation`. Copy the import line from `common/26.2/src/main/java/com/chronodawn/blocks/ClockstoneOre.java`.

- [ ] **Step 5: Verify the era groups by hash**

```bash
md5 common/*/src/main/java/com/chronodawn/blocks/ChroniteOre.java
md5 common/*/src/main/java/com/chronodawn/blocks/DeepslateChroniteOre.java
```

Expected: three distinct hashes each, grouped 2 / 9 / 2.

- [ ] **Step 6: Register the blocks and their block items**

In each `ModBlocks.java`, after the `DEEPSLATE_CLOCKSTONE_ORE` block (around line 233):

```java
    /**
     * Chronite Ore - Overworld ore that drops Chronite Shards.
     */
    public static final RegistrySupplier<Block> CHRONITE_ORE = BLOCKS.register(
        ModBlockId.CHRONITE_ORE.id(),
        () -> new ChroniteOre(ChroniteOre.createProperties())
    );

    /**
     * Deepslate Chronite Ore - deepslate variant of Chronite Ore.
     */
    public static final RegistrySupplier<Block> DEEPSLATE_CHRONITE_ORE = BLOCKS.register(
        ModBlockId.DEEPSLATE_CHRONITE_ORE.id(),
        () -> new DeepslateChroniteOre(DeepslateChroniteOre.createProperties())
    );
```

Register the block items in `ModItems.java` by copying the exact form used for `CLOCKSTONE_ORE` and `DEEPSLATE_CLOCKSTONE_ORE` in that same file. Do not invent a different registration helper; the form differs between eras and the existing lines are the source of truth.

- [ ] **Step 7: Add both block items to the creative tab**

In each `ModItems.java`, next to the existing `output.accept(CLOCKSTONE_ORE.get());`:

```java
        output.accept(CHRONITE_ORE.get());
        output.accept(DEEPSLATE_CHRONITE_ORE.get());
```

- [ ] **Step 8: Create blockstates and models**

`blockstates/chronite_ore.json`:

```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/chronite_ore"
    }
  }
}
```

`models/block/chronite_ore.json`:

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/chronite_ore"
  }
}
```

`models/item/chronite_ore.json`:

```json
{
  "parent": "chronodawn:block/chronite_ore"
}
```

Repeat all three for `deepslate_chronite_ore`.

- [ ] **Step 9: Create the client item definitions for 1.21.4 and later**

Four files, two ids across `common/1.21.4/` and `common/shared-1.21.5+/`, each mirroring the shape of the existing `clockstone_ore.json` in the same directory.

- [ ] **Step 10: Create the block textures**

Extract vanilla stone and deepslate once:

```bash
unzip -o -j ~/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar \
  assets/minecraft/textures/block/stone.png \
  assets/minecraft/textures/block/deepslate.png -d "$TMPDIR"
```

Then re-base the existing Clockstone ore overlay off `temporal_stone` and onto each vanilla background using the repo's script, which applies `result = clamp(base + (ore - original_base))` and so preserves the anti-aliased overlay edges:

```bash
python3 scripts/composite_ore_on_custom_stone.py \
  --vanilla-stone common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_stone.png \
  --base "$TMPDIR/stone.png" \
  --ore common/shared/src/main/resources/assets/chronodawn/textures/block/clockstone_ore.png
```

Read `scripts/composite_ore_on_custom_stone.py` for its exact flag semantics and output path before running it; the argument names above come from its docstring, and the script writes in place, so work on copies. Recolour the resulting overlay to Chronite's pale amber so it is visually distinct from Clockstone, then save as `chronite_ore.png` and, repeating with `deepslate.png` as the base, `deepslate_chronite_ore.png`.

Verify both are 16x16 RGBA with the same Pillow one-liner from Task 1 Step 10.

- [ ] **Step 11: Create the loot tables for 1.21.1 and later**

`common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/chronite_ore.json`:

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
                          "levels": {
                            "min": 1
                          }
                        }
                      ]
                    }
                  }
                }
              ],
              "name": "chronodawn:chronite_ore"
            },
            {
              "type": "minecraft:item",
              "functions": [
                {
                  "function": "minecraft:set_count",
                  "count": {
                    "type": "minecraft:uniform",
                    "min": 1,
                    "max": 2
                  }
                },
                {
                  "function": "minecraft:apply_bonus",
                  "enchantment": "minecraft:fortune",
                  "formula": "minecraft:ore_drops"
                },
                {
                  "function": "minecraft:explosion_decay"
                }
              ],
              "name": "chronodawn:chronite_shard"
            }
          ]
        }
      ]
    }
  ]
}
```

The deepslate variant is identical except that the silk touch entry names `chronodawn:deepslate_chronite_ore`.

- [ ] **Step 12: Create the loot tables for 1.20.1**

Same two files under `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/`, but the silk touch condition uses the pre-1.21 shape, matching `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/clockstone_ore.json`:

```json
                  "predicate": {
                    "enchantments": [
                      {
                        "enchantment": "minecraft:silk_touch",
                        "levels": {
                          "min": 1
                        }
                      }
                    ]
                  }
```

The `predicates` wrapper and the plural `enchantments` key are 1.21.1-and-later only. Getting this wrong fails silently at runtime rather than at build time.

- [ ] **Step 13: Add the mining tags**

Add `"chronodawn:chronite_ore"` and `"chronodawn:deepslate_chronite_ore"` to the `values` array of all four files:

- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/needs_stone_tool.json`
- `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/needs_stone_tool.json`

Use `needs_stone_tool`, not `needs_iron_tool`. Clockstone Ore uses the iron tag; Chronite deliberately does not, because the whole point is early reachability.

- [ ] **Step 14: Add translations**

Six lang files, two keys each:

`en_us.json`:
```
  "block.chronodawn.chronite_ore": "Chronite Ore",
  "block.chronodawn.deepslate_chronite_ore": "Deepslate Chronite Ore",
```

`ja_jp.json`:
```
  "block.chronodawn.chronite_ore": "クロナイト鉱石",
  "block.chronodawn.deepslate_chronite_ore": "深層クロナイト鉱石",
```

- [ ] **Step 15: Verify**

```bash
./gradlew validateResources
```
Expected: BUILD SUCCESSFUL, blockstate to model to texture all resolve for both ores.

```bash
./gradlew validateData
```
Expected: BUILD SUCCESSFUL, tag entries resolve to registered IDs and 1.21.4+ client item coverage is complete.

```bash
./gradlew validateTranslations
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew build1_20_1
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew build26_2
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 16: Commit**

```bash
git add -A
git commit -m "feat(blocks): add Chronite Ore and its deepslate variant"
```

---

### Task 3: Chronite Block and its recipe pair

**Files:**
- Modify: `ModBlockId.java`, `ModItemId.java`
- Create: `common/<each of 13>/src/main/java/com/chronodawn/blocks/ChroniteBlock.java`
- Modify: `common/<each of 13>/src/main/java/com/chronodawn/registry/{ModBlocks,ModItems}.java`
- Create: blockstate, block model, item model, texture under `common/shared/.../assets/chronodawn/`
- Create: client item JSON in `common/1.21.4/` and `common/shared-1.21.5+/`
- Create: loot table in both loot directories
- Modify: pickaxe and `needs_stone_tool` tags in both tag directories
- Create: `chronite_block.json` and `chronite_from_block.json` recipes in all three recipe directories
- Create: matching advancements in both advancement directories
- Modify: the six lang files

**Interfaces:**
- Consumes: `chronodawn:chronite_shard` from Task 1.
- Produces: `chronodawn:chronite_block`. Nothing later depends on it.

- [ ] **Step 1: Mirror the Clockstone Block implementation**

`ClockstoneBlock` is registered at `common/<version>/src/main/java/com/chronodawn/registry/ModBlocks.java:242`. Create `ChroniteBlock` following exactly the same three-era `createProperties()` split established in Task 2 Steps 2 through 4, using `Blocks.STONE` as the `ofFullCopy` source and `"chronite_block"` as the id. Register it, register its block item, and add it to the creative tab beside `CLOCKSTONE_BLOCK`.

- [ ] **Step 2: Verify the era groups by hash**

```bash
md5 common/*/src/main/java/com/chronodawn/blocks/ChroniteBlock.java
```
Expected: three distinct hashes, grouped 2 / 9 / 2.

- [ ] **Step 3: Create the assets**

Blockstate, `cube_all` block model, item model that parents the block model, and a 16x16 texture, following the exact file shapes given in Task 2 Step 8. Plus the two client item JSONs for 1.21.4 and later.

- [ ] **Step 4: Create the loot table**

A plain self-drop, in both loot directories. Copy the shape of `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/clockstone_block.json` and its 1.20.1 counterpart rather than writing it from scratch.

- [ ] **Step 5: Add the mining tags**

`"chronodawn:chronite_block"` into all four pickaxe and `needs_stone_tool` files.

- [ ] **Step 6: Create the packing recipe**

`chronite_block.json` in all three recipe directories. For `common/shared-1.21.2+/` and `common/1.21.1/`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "XXX",
    "XXX",
    "XXX"
  ],
  "key": {
    "X": "chronodawn:chronite_shard"
  },
  "result": {
    "id": "chronodawn:chronite_block",
    "count": 1
  }
}
```

For `common/1.20.1/src/main/resources/data/chronodawn/recipes/chronite_block.json`, use the 1.20.1 result and key shape taken from `common/1.20.1/src/main/resources/data/chronodawn/recipes/clockstone_block.json`. The `result` object and the `key` entry format both differ in that era; copy the neighbouring file rather than adapting the JSON above by hand.

- [ ] **Step 7: Create the unpacking recipe**

`chronite_from_block.json`, shapeless, one `chronodawn:chronite_block` to nine `chronodawn:chronite_shard`, again with the 1.20.1 variant copied from `clockstone_from_block.json`.

- [ ] **Step 8: Create the recipe unlock advancements**

Two files in `common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes/misc/` and two in `common/1.20.1/src/main/resources/data/chronodawn/advancements/recipes/misc/`, following `clockstone_block.json` exactly, with `chronodawn:chronite_shard` as the `has_item` trigger and the matching recipe id in `has_the_recipe` and `rewards.recipes`.

Without these files the recipes never appear in the recipe book.

- [ ] **Step 9: Add translations**

`"block.chronodawn.chronite_block"`: `"Block of Chronite"` / `"クロナイトブロック"`, in all six lang files.

- [ ] **Step 10: Verify**

```bash
./gradlew validateResources
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew validateData
```
Expected: BUILD SUCCESSFUL, with the recipe references resolving.

```bash
./gradlew validateTranslations
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew build1_20_1
```
Expected: BUILD SUCCESSFUL. This is the era whose recipe JSON shape differs, so it is the one that catches a bad copy.

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "feat(blocks): add Chronite Block with packing recipes"
```

---

### Task 4: Worldgen and Overworld biome injection

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_chronite.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_chronite.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_chronite.json`
- Create: `common/shared/src/main/resources/data/neoforge/biome_modifier/add_chronite_ore.json`
- Create: `fabric/base/src/main/java/com/chronodawn/fabric/worldgen/ChroniteBiomeModifications.java`
- Modify: `fabric/<each of 13>/src/main/java/com/chronodawn/fabric/ChronoDawnFabric.java`

**Interfaces:**
- Consumes: `chronodawn:chronite_ore` and `chronodawn:deepslate_chronite_ore` from Task 2.
- Produces: placed feature `chronodawn:ore_chronite`, which Task 5's runtime overlay rewrites.

- [ ] **Step 1: Create the configured feature**

`common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_chronite.json`:

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 8,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables" },
        "state": { "Name": "chronodawn:chronite_ore" }
      },
      {
        "target": { "predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables" },
        "state": { "Name": "chronodawn:deepslate_chronite_ore" }
      }
    ]
  }
}
```

This uses `tag_match` against the vanilla replaceables tags rather than the `block_match` form `ore_clockstone.json` uses, because Chronite generates in vanilla terrain where the stone type varies, not in the dimension's single `temporal_stone`.

- [ ] **Step 2: Create the placed feature for 1.21.1 and later**

`common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_chronite.json`:

```json
{
  "feature": "chronodawn:ore_chronite",
  "placement": [
    {
      "type": "minecraft:count",
      "count": 6
    },
    {
      "type": "minecraft:in_square"
    },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:trapezoid",
        "min_inclusive": {
          "absolute": -48
        },
        "max_inclusive": {
          "absolute": 112
        }
      }
    },
    {
      "type": "minecraft:biome"
    }
  ]
}
```

- [ ] **Step 3: Create the placed feature for 1.20.1**

Identical content at `common/1.20.1/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_chronite.json`. That module keeps its own copy of every placed feature; check the existing `ore_clockstone.json` there for any field the 1.21 version does not have and match it.

- [ ] **Step 4: Create the NeoForge biome modifier**

`common/shared/src/main/resources/data/neoforge/biome_modifier/add_chronite_ore.json`:

```json
{
  "type": "neoforge:add_features",
  "biomes": "#minecraft:is_overworld",
  "features": "chronodawn:ore_chronite",
  "step": "underground_ores"
}
```

This is the whole NeoForge side. No Java.

- [ ] **Step 5: Create the Fabric biome modification**

`fabric/base/src/main/java/com/chronodawn/fabric/worldgen/ChroniteBiomeModifications.java`. `fabric/base/` is shared by all thirteen Fabric subprojects and compiles as one unit, so this is one file rather than thirteen:

```java
package com.chronodawn.fabric.worldgen;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Injects Chronite Ore into every Overworld biome.
 *
 * NeoForge does this from data via data/neoforge/biome_modifier/add_chronite_ore.json.
 * Fabric has no data-driven equivalent, so the same injection is expressed here.
 * Keep the two in sync: same placed feature, same generation step, same biome set.
 */
public final class ChroniteBiomeModifications {
    private ChroniteBiomeModifications() {
    }

    public static void register() {
        ResourceKey<PlacedFeature> oreChronite = ResourceKey.create(
            Registries.PLACED_FEATURE,
            CompatResourceLocation.create(ChronoDawn.MOD_ID, "ore_chronite")
        );

        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            oreChronite
        );
    }
}
```

Use `CompatResourceLocation.create` rather than a direct constructor. The class name is `ResourceLocation` before 26.x and `Identifier` from 26.1.2, and `fabric/base/` compiles against all of them, so a direct reference breaks the build on one era or the other.

- [ ] **Step 6: Call it from the Fabric entrypoint**

In each `fabric/<version>/src/main/java/com/chronodawn/fabric/ChronoDawnFabric.java`, inside the same initialiser that already calls `ChronoDawnFuelRegistry.register()`, add:

```java
        ChroniteBiomeModifications.register();
```

with the matching import. Verify with:

```bash
grep -c "ChroniteBiomeModifications.register()" fabric/*/src/main/java/com/chronodawn/fabric/ChronoDawnFabric.java
```

Expected: `1` for every one of the thirteen files, none reporting `0`.

- [ ] **Step 7: Verify the data resolves**

```bash
./gradlew validateData
```
Expected: BUILD SUCCESSFUL, with `chronodawn:ore_chronite` resolving from the placed feature to the configured feature to the two registered block IDs.

- [ ] **Step 8: Verify both loaders build**

```bash
./gradlew build1_21_1
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew build26_2
```
Expected: BUILD SUCCESSFUL. If `BiomeModifications` or `BiomeSelectors` moved on 26.x, the failure surfaces here, and the fix belongs in the single `fabric/base/` file.

- [ ] **Step 9: Confirm generation in a real world, both loaders**

```bash
./gradlew runClientFabric1_21_1
```

In game, create a new creative world, then run `/fill ~ ~ ~ ~16 ~ ~16 air` style exploration or simply dig down and check. The reliable check is:

```
/execute in minecraft:overworld run locate biome minecraft:plains
```
then fly to bedrock level and confirm Chronite Ore appears in stone above Y 0 and Deepslate Chronite Ore below. Break one with a stone pickaxe and confirm it drops 1 to 2 Chronite Shards, and with a Silk Touch pickaxe and confirm it drops the ore block.

Repeat with `./gradlew runClientNeoForge1_21_1`. The two loaders use completely different injection mechanisms, so a passing Fabric run says nothing about NeoForge.

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat(worldgen): generate Chronite Ore across Overworld biomes"
```

---

### Task 5: Config exposure for Chronite generation

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/OresConfig.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlay.java`
- Modify: `common/shared/src/test/java/com/chronodawn/worldgen/runtime/RuntimePlacedFeatureOverlayTest.java`

**Interfaces:**
- Consumes: the placed feature `ore_chronite` from Task 4.
- Produces: `OresConfig.chronite()` returning `OreSettings`; `ConfigDefaults.CHRONITE_DEFAULTS`; the config key `world.ores.chronite`.

- [ ] **Step 1: Add the default**

In `ConfigDefaults.java`, beside `CLOCKSTONE_DEFAULTS` (line 45):

```java
    public static final OreSettings CHRONITE_DEFAULTS = new OreSettings(true, 6, -48, 112);
```

These four values must match `placed_feature/ore_chronite.json` exactly. `RuntimePlacedFeatureOverlayTest` asserts round-trip equality between the defaults and the bundled JSON, so a mismatch fails the test rather than shipping.

- [ ] **Step 2: Extend the config record**

In `OresConfig.java`, add `OreSettings chronite` as the fifth component, and update the class Javadoc, which currently names only four ores.

- [ ] **Step 3: Parse the new key**

In `ConfigLoader.java`, beside `K_CLOCKSTONE` (line 68):

```java
    private static final String K_CHRONITE = "chronite";
```

and in `parseOres` (line 271), add as the fifth argument:

```java
            parseOre(parsed, K_CHRONITE, ConfigDefaults.CHRONITE_DEFAULTS)
```

- [ ] **Step 4: Wire the runtime overlay**

In `RuntimePlacedFeatureOverlay.java`, add the path constant beside the one at line 52:

```java
    private static final String ORE_CHRONITE_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_chronite.json";
```

and the generation call beside line 65:

```java
        entries.put(ORE_CHRONITE_PATH, generateOre(
            "ore_chronite", "minecraft:trapezoid", config.world().ores().chronite()));
```

Match the surrounding code's exact `entries.put` form; the snippet above assumes it, and the file is the authority.

`"minecraft:trapezoid"` must match the `height` distribution type written in `placed_feature/ore_chronite.json` in Task 4 Step 2. `ore_temporal_amber` uses `uniform`, so this is a real per-ore choice, not boilerplate.

- [ ] **Step 5: Extend the overlay test**

In `RuntimePlacedFeatureOverlayTest.java`, add Chronite to whatever list or parameter source drives the existing four ores. Read the file first; the assertion style dictates the shape of the addition.

- [ ] **Step 6: Run the test and watch it pass**

```bash
./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*RuntimePlacedFeatureOverlayTest*'
```
Expected: PASS, including the new Chronite case.

To confirm the test actually guards the defaults rather than passing vacuously, temporarily change `CHRONITE_DEFAULTS` count from `6` to `7`, re-run, confirm it FAILS, then revert to `6` and confirm it PASSES again. A green test that would stay green under a wrong value is not a guard.

- [ ] **Step 7: Verify the generated overlay in game**

```bash
./gradlew runClientFabric1_21_1
```

Quit to desktop once, edit `fabric/1.21.1/run/config/chronodawn.toml` to set `[world.ores.chronite] count = 0`, relaunch, create a fresh world, and confirm no Chronite generates. Restore the file afterwards.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat(config): expose Chronite generation via world.ores.chronite"
```

---

### Task 6: Time Compass Overworld fallback and recipe

**Files:**
- Modify: `common/<each of 13>/src/main/java/com/chronodawn/items/TimeCompassItem.java`
- Create: `time_compass.json` recipe in all three recipe directories
- Create: `time_compass.json` advancement in both advancement directories

**Interfaces:**
- Consumes: `chronodawn:chronite_shard` from Task 1.
- Produces: a craftable, self-targeting `chronodawn:time_compass`. Nothing later depends on it.

- [ ] **Step 1: Read the current target resolution**

Open `common/1.21.1/src/main/java/com/chronodawn/items/TimeCompassItem.java`. `STRUCTURE_ANCIENT_RUINS` is declared at line 73. The structure search runs around line 340 to 385 and already branches on `ManagedStructure.Dimension.OVERWORLD`. Find the method that reads the stored structure type from the stack and identify where it returns empty or null for an untargeted compass. That call site is where the fallback goes.

- [ ] **Step 2: Add the fallback**

At that call site, before the untargeted early return, insert logic equivalent to:

```java
        // An untargeted compass used in the Overworld resolves to Ancient Ruins.
        // This is the only Overworld-obtainable Time Compass target, and it is what
        // lets the crafting recipe stay plain: baking a preset target into a recipe
        // result would mean NBT on 1.20.1 and components from 1.21.2 onward.
        // Dimension structures stay trade-only, so Time Keeper trades are unaffected.
        if (structureType == null && player.level().dimension().equals(Level.OVERWORLD)) {
            structureType = STRUCTURE_ANCIENT_RUINS;
            setTargetStructure(stack, STRUCTURE_ANCIENT_RUINS);
        }
```

Adapt the variable and setter names to whatever the file actually uses. Do not introduce a new setter if one already exists.

- [ ] **Step 3: Apply to all thirteen modules and verify by hash**

```bash
md5 common/*/src/main/java/com/chronodawn/items/TimeCompassItem.java
```

Record the hash grouping *before* editing, then re-run after. The number of distinct groups must not increase. If it does, one module's edit diverged from its era-mates.

- [ ] **Step 4: Create the recipe**

`common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/time_compass.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " S ",
    "SCS",
    " K "
  ],
  "key": {
    "S": "chronodawn:chronite_shard",
    "C": "minecraft:compass",
    "K": "minecraft:clock"
  },
  "result": {
    "id": "chronodawn:time_compass",
    "count": 1
  }
}
```

The asymmetric pattern is deliberate. A recipe lives at a `chronodawn:` ID so it can never overwrite another mod's file, but two shaped recipes with identical patterns resolve ambiguously, and this shape is unlikely to collide.

Create the `common/1.21.1/` copy with the same content, and the `common/1.20.1/` copy with that era's `result` and `key` shape copied from a neighbouring 1.20.1 recipe.

- [ ] **Step 5: Create the recipe unlock advancement**

In both advancement directories, following `clockstone_block.json`, with `chronodawn:chronite_shard` as the `has_item` trigger and `chronodawn:time_compass` throughout.

- [ ] **Step 6: Verify**

```bash
./gradlew validateData
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew build1_20_1
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew build26_2
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Confirm the behaviour in game**

```bash
./gradlew runClientFabric1_21_1
```

In a fresh survival or creative world in the Overworld, craft a Time Compass from a vanilla compass, a clock, and three Chronite Shards. Use it. Expect the chat feedback naming Ancient Ruins with coordinates, a direction, and a distance, and expect the tooltip to then show Ancient Ruins as the target. Cross-check the reported coordinates against:

```
/locate structure chronodawn:ancient_ruins
```

The two must agree. Then repeat in the Chrono Dawn dimension with a fresh untargeted compass and confirm the fallback does *not* fire there, leaving the compass untargeted.

Repeat the Overworld half on `./gradlew runClientNeoForge1_21_1`.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat(items): let an untargeted Time Compass find Ancient Ruins in the Overworld"
```

---

### Task 7: Chronite consumption recipes

**Files:**
- Create: `time_arrow_from_chronite.json` in all three recipe directories
- Create: `clock_from_chronite.json` in all three recipe directories
- Create: both advancements in both advancement directories

**Interfaces:**
- Consumes: `chronodawn:chronite_shard` from Task 1.
- Produces: nothing later tasks depend on.

- [ ] **Step 1: Create the Time Arrow recipe**

`common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/time_arrow_from_chronite.json`:

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    "chronodawn:chronite_shard",
    "minecraft:arrow"
  ],
  "result": {
    "id": "chronodawn:time_arrow",
    "count": 1
  }
}
```

Do not touch the existing `time_arrow.json`. Both recipes coexist. The Clockstone route stays four times more efficient per arrow in special material, so it keeps its place; this route is the one that never runs out.

- [ ] **Step 2: Create the clock recipe**

`clock_from_chronite.json`, producing `minecraft:clock`, using the vanilla clock pattern with `chronodawn:chronite_shard` in place of `minecraft:gold_ingot` and keeping `minecraft:redstone` in the centre:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " S ",
    "SRS",
    " S "
  ],
  "key": {
    "S": "chronodawn:chronite_shard",
    "R": "minecraft:redstone"
  },
  "result": {
    "id": "minecraft:clock",
    "count": 1
  }
}
```

Producing a vanilla item from a `chronodawn:`-namespaced recipe adds a route without displacing the vanilla one.

- [ ] **Step 3: Create both 1.21.1 and 1.20.1 copies**

Same content for `common/1.21.1/`. For `common/1.20.1/`, convert the `result` and `ingredients` shapes to that era's format by copying from an existing 1.20.1 shapeless recipe such as `clockstone_from_block.json`.

- [ ] **Step 4: Create the four advancements**

Two recipes across two advancement directories, all four triggered by `chronodawn:chronite_shard`.

- [ ] **Step 5: Verify**

```bash
./gradlew validateData
```
Expected: BUILD SUCCESSFUL, with `minecraft:clock` and `chronodawn:time_arrow` both resolving as recipe results.

```bash
./gradlew build1_20_1
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Confirm in game**

```bash
./gradlew runClientFabric1_21_1
```

Craft a Time Arrow from one Chronite Shard and one arrow. Craft a clock from four shards and a redstone. Confirm the original Clockstone Time Arrow recipe still works and still yields four arrows, and that the vanilla gold clock recipe still works.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat(recipes): add Chronite routes for Time Arrows and clocks"
```

---

### Task 8: Chronicle entries

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/items/chronite.json`

**Interfaces:**
- Consumes: everything from Tasks 1 through 7.
- Produces: nothing.

- [ ] **Step 1: Add a Time Compass page to the Ancient Ruins entry**

Append a fifth page to the `pages` array of `basics/ancient_ruins.json`, after the existing "How to Find Ancient Ruins" page:

```json
    {
      "text": {
        "en_us": "The Reliable Route:\n\nMine Chronite Ore. It appears in ordinary Overworld stone and deepslate, in every biome, and a stone pickaxe is enough.\n\nCraft a Time Compass from a compass, a clock, and Chronite Shards. Use it anywhere in the Overworld and it will lock onto the nearest Ancient Ruins, then report the direction and distance whenever you use it again.\n\nYou no longer have to search. You only have to walk.",
        "ja_jp": "確実な方法:\n\nクロナイト鉱石を採掘しましょう。オーバーワールドの普通の石と深層岩に、どのバイオームでも生成され、石のツルハシがあれば採れます。\n\nコンパスと時計とクロナイトの欠片でタイムコンパスを作ります。オーバーワールドのどこで使っても最寄りの古代遺跡を捕捉し、以降は使うたびに方角と距離を知らせます。\n\nもう探す必要はありません。歩くだけです。"
      }
    }
```

- [ ] **Step 2: Create the Chronite entry**

`entries/items/chronite.json`, following the structure of a neighbouring entry such as `entries/items/clockstone.json`:

```json
{
  "category": "items",
  "title": {
    "en_us": "Chronite",
    "ja_jp": "クロナイト"
  },
  "icon": "chronodawn:chronite_shard",
  "sortnum": 2,
  "pages": [
    {
      "text": {
        "en_us": "Chronite is the Chrono Dawn's temporal field bleeding into Overworld bedrock and crystallising there. It is the only trace of the frozen dimension that reaches your world on its own.\n\nBecause it carries that trace, a compass built from Chronite Shards can sense the place where the two worlds once touched.",
        "ja_jp": "クロナイトは、クロノドーンの時間場がオーバーワールドの岩盤に染み出して結晶化したものです。凍結した次元の痕跡のうち、自力でこちらの世界に届く唯一のものです。\n\nその痕跡を帯びているため、クロナイトの欠片で作った羅針盤は、2つの世界がかつて接触した場所を感知できます。"
      }
    },
    {
      "text": {
        "en_us": "Where to Find It:\n\n• Ordinary Overworld stone and deepslate\n• Every biome, no exceptions\n• Y -48 to 112, most common in the middle of that band\n• A stone pickaxe is enough\n\nUses:\n\n• Time Compass\n• Time Arrows, one shard per arrow\n• Clocks, without gold\n• Blocks of Chronite for storage\n\nChronite makes no armour, tools, or weapons. It is a reagent, not a metal.",
        "ja_jp": "見つかる場所:\n\n• オーバーワールドの普通の石と深層岩\n• 例外なくすべてのバイオーム\n• Y -48〜112、帯の中央付近が最も多い\n• 石のツルハシで採掘可能\n\n用途:\n\n• タイムコンパス\n• 時の矢、矢1本につき欠片1個\n• 時計、金を使わずに作成\n• クロナイトブロックで保管\n\nクロナイトからは防具・道具・武器は作れません。金属ではなく試薬です。"
      }
    }
  ]
}
```

Check `sortnum` against the other entries in `entries/items/` and pick a value that places Chronite sensibly rather than colliding.

- [ ] **Step 3: Verify**

```bash
./gradlew validateResources
```
Expected: BUILD SUCCESSFUL, with `chronodawn:chronite_shard` resolving as the entry icon.

- [ ] **Step 4: Confirm in game**

```bash
./gradlew runClientFabric1_21_1
```
Open the Chronicle, confirm both the new Ancient Ruins page and the Chronite entry render, with no missing-translation placeholders.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "docs(chronicle): document Chronite and the Time Compass route"
```

---

### Task 9: Documentation corrections

**Files:**
- Modify: `docs/player_guide.md`
- Modify: `docs/configuration.md`
- Modify: `docs/curseforge_description.md`
- Modify: `docs/modrinth_description.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes: everything above.
- Produces: nothing.

- [ ] **Step 1: Fix the false biome claim in the player guide**

`docs/player_guide.md` lines 115 to 120 currently read:

```
**How to find Ancient Ruins**:
- Explore the Overworld surface in various biomes
- Look for distinctive ancient stone structures above ground
- Use `/locate structure chronodawn:ancient_ruins` (if cheats enabled)

**Tip**: Ancient Ruins are surface structures that spawn in various biomes. Keep exploring the landscape until you find one!
```

Both "various biomes" claims predate the taiga and dark forest restriction and send players to search plains and deserts. Replace with:

```
**How to find Ancient Ruins**:
- The reliable route: mine Chronite Ore (any biome, ordinary stone and deepslate, stone pickaxe), craft a Time Compass from a compass, a clock, and Chronite Shards, and use it in the Overworld. It locks onto the nearest Ancient Ruins and reports direction and distance.
- By exploration: ruins generate only in taiga biomes and dark forests, on the surface. They are rare, and forest canopy hides them, so look from high ground.
- Use `/locate structure chronodawn:ancient_ruins` (if cheats enabled)
- If ruins are still too sparse for your taste, lower `spacing` under `[world.structures.ancient_ruins]` in `config/chronodawn.toml`. See the Configuration Guide.
```

- [ ] **Step 2: Correct the configuration guide's gating claim**

`docs/configuration.md:70` describes Ancient Ruins as `Overworld flavour; gates nothing.` and line 103 implies disabling it is safe. Both are wrong: `ore_clockstone` is referenced only from `chronodawn_*` biomes, so ruins are the sole Overworld source of Clockstone, and the Time Hourglass blueprint drops only from `chests/ancient_ruins.json`. Change the table note to state that Ancient Ruins gates entry to the dimension, and correct line 103 to say that disabling Ancient Ruins also breaks progression unless the player already has Clockstone and the blueprint.

- [ ] **Step 3: Document the new ore config**

Add `[world.ores.chronite]` to `docs/configuration.md` in the `[world.ores.*]` section starting at line 208, with defaults `enabled = true`, `count = 6`, `y_min = -48`, `y_max = 112`, and update the per-field default column, which currently lists four ores as `TC / EC / TA / CS`.

- [ ] **Step 4: Add spoiler-gated guidance to both store pages**

In `docs/curseforge_description.md` and `docs/modrinth_description.md`, keep the existing one-line Step 1 and add below it:

```markdown
<details>
<summary>Spoiler: how to find Ancient Ruins</summary>

Ancient Ruins generate on the surface of taiga biomes and dark forests only, and they are rare.

The reliable route is Chronite Ore. It generates in ordinary Overworld stone and deepslate in every biome and needs only a stone pickaxe. Craft a Time Compass from a compass, a clock, and Chronite Shards, then use it anywhere in the Overworld. It locks onto the nearest Ancient Ruins and reports direction and distance.

You can also raise the ruins' density with `spacing` under `[world.structures.ancient_ruins]` in `config/chronodawn.toml`.

</details>
```

Then verify that Modrinth renders `<details>`. Open the project's description editor preview, or push a draft description, and confirm the block collapses. If it renders as literal text, replace it on the Modrinth page only with a `### ⚠️ Spoiler: how to find Ancient Ruins` heading preceded by a `---` rule, keeping the same body. Record which form shipped in the commit message.

- [ ] **Step 5: Update the changelog**

Add to the `Unreleased` section of `CHANGELOG.md`, under `Added`, folding the documentation corrections into the feature entry rather than listing them as separate fixes:

```markdown
- Chronite Ore, a common Overworld ore that drops Chronite Shards. Craft a Time Compass from a compass, a clock, and Chronite Shards to locate the nearest Ancient Ruins from anywhere in the Overworld. Chronite also crafts Time Arrows, clocks without gold, and Blocks of Chronite. It makes no armour, tools, or weapons.
```

- [ ] **Step 6: Check for em dashes and semicolons**

```bash
grep -n "—\|――" docs/player_guide.md docs/configuration.md docs/curseforge_description.md docs/modrinth_description.md CHANGELOG.md
```
Expected: no hits in any line you added. Pre-existing hits elsewhere are out of scope.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "docs: correct Ancient Ruins biome and gating claims, document Chronite"
```

---

### Task 10: Full verification

**Files:** none.

**Interfaces:**
- Consumes: all preceding tasks.
- Produces: the evidence that the branch is ready for review.

- [ ] **Step 1: Stop any stale Gradle daemon**

```bash
./gradlew --stop
```

A daemon reused from an earlier JDK produces failures that have nothing to do with this branch. On this repo, 26.1.2 and 26.2 need JDK 25 while the rest need JDK 21.

- [ ] **Step 2: Check for orphaned Gradle processes from other sessions**

```bash
pgrep -fl GradleDaemon
```

If a daemon has been running for many hours it is likely orphaned from another session and will contend for the same build directories. Confirm with the user before killing anything.

- [ ] **Step 3: Run the validators**

```bash
./gradlew validateResources
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew validateData
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew validateTranslations
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Clean and build every version**

```bash
./gradlew cleanAll
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew buildAll
```
Expected: BUILD SUCCESSFUL.

`buildAll` intermittently reports a spurious FAILED from a `RemapSourcesJarTask` race. If it fails, re-read the actual error before believing it, and re-run the single failing version with `./gradlew build<version>` to confirm.

- [ ] **Step 5: Run every unit test**

```bash
./gradlew testAll
```
Expected: BUILD SUCCESSFUL, including `CreativeTabCompletenessTest`, `ResourceValidationTest`, and `RuntimePlacedFeatureOverlayTest`.

- [ ] **Step 6: Run GameTests per version rather than gameTestAll**

`gameTestAll` leaks into the main checkout when run from a worktree, so run the per-version tasks here. At minimum cover one version per API era:

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1
```
Expected: BUILD SUCCESSFUL.

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=26.2
```
Expected: BUILD SUCCESSFUL.

Run each in its own Bash call. Chaining them produces spurious failures. If a NeoForge GameTest server crashes, the JVM does not exit on its own and blocks the next run, so check for and kill the leftover process before continuing.

- [ ] **Step 7: Report honestly**

Write up what passed, what was skipped, and why. If `gameTestAll` was not run in full, say so explicitly and note that it must run from the main checkout before merge. Do not describe the branch as verified on the strength of a single version.

- [ ] **Step 8: Confirm the main checkout is clean**

```bash
git -C ../.. status --short
```
Expected: empty. Work in a worktree occasionally lands in the main checkout by accident, and it is invisible from here unless you look.

- [ ] **Step 9: Hand back to the user**

Do not push and do not merge. Report the verification results and wait.

---

## Self-Review

**Spec coverage.** Every section of the spec maps to a task. Chronite Ore blocks and shard: Tasks 1 and 2. Worldgen and per-loader biome injection: Task 4. Config: Task 5. The four uses, Time Compass, clock, Time Arrow, Chronite Block: Tasks 3, 6, and 7. Time Compass fallback: Task 6. Documentation including the `player_guide.md` and `configuration.md` corrections and the store-page spoilers: Task 9. Chronicle: Task 8. Verification: Task 10.

**Type consistency.** `chronodawn:chronite_shard`, `chronodawn:chronite_ore`, `chronodawn:deepslate_chronite_ore`, `chronodawn:chronite_block`, and `chronodawn:ore_chronite` are used identically wherever they appear. `ConfigDefaults.CHRONITE_DEFAULTS` and `OresConfig.chronite()` match between Task 5 Steps 1, 2, 3, and 4. The `count = 6`, `y_min = -48`, `y_max = 112` triple is the same in Task 4 Step 2, Task 5 Step 1, and Task 9 Step 3.

**Known deferrals, all deliberate and stated in the spec.** Ore cluster `size` and the exact texture palette are tuned during implementation. The Modrinth `<details>` support question is resolved empirically in Task 9 Step 4 with a stated fallback. A Time Keeper trade that buys Chronite is out of scope until the arrow sink proves insufficient.
