# Deepslate Temporal Stone Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Deepslate Temporal Stone as the deep-layer base block for the ChronoDawn dimension, plus deepslate variants of ores that generate below Y=0.

**Architecture:** 8 new blocks (deepslate_temporal_stone + stairs/slab/wall, 4 deepslate ores) registered across 11 Minecraft versions (1.20.1 through 1.21.11). Shared JSON resources in `common/shared/` and `common/shared-1.21.1+/`, version-specific Java classes in `common/<version>/`. Worldgen updated in all 11 noise_settings files and 4 configured features.

**Tech Stack:** Java 21, Architectury API (Fabric + NeoForge), Mojang mappings

**Spec:** `docs/superpowers/specs/2026-04-14-deepslate-temporal-stone-design.md`

---

## Version Era Reference

Block class patterns differ across versions. The plan references these eras:

| Era | Versions | Properties | setId | DropExperienceBlock ctor | ResourceLocation class |
|-----|----------|-----------|-------|--------------------------|----------------------|
| A | 1.20.1 | `Properties.of()` + manual | No | `super(props, xpRange)` | N/A |
| B | 1.21.1 | `Properties.of()` + manual | No | `super(xpRange, props)` | N/A |
| C | 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10 | `ofFullCopy()` | Yes | `super(xpRange, props)` | `ResourceLocation` |
| D | 1.21.11 | `ofFullCopy()` | Yes | `super(xpRange, props)` | `Identifier` |

---

## Task 1: Add Block IDs to ModBlockId.java

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`

- [ ] **Step 1: Add deepslate block IDs**

Add after the existing `TEMPORAL_STONE_WALL` entry block (around the temporal stone variants section):

```java
// Deepslate Temporal Stone variants
DEEPSLATE_TEMPORAL_STONE(def("deepslate_temporal_stone")),
DEEPSLATE_TEMPORAL_STONE_STAIRS(def("deepslate_temporal_stone_stairs")),
DEEPSLATE_TEMPORAL_STONE_SLAB(def("deepslate_temporal_stone_slab")),
DEEPSLATE_TEMPORAL_STONE_WALL(def("deepslate_temporal_stone_wall")),
```

Add after the existing `TEMPORAL_AMBER_ORE` entry (in the ores section):

```java
// Deepslate ores
DEEPSLATE_TEMPORAL_AMBER_ORE(def("deepslate_temporal_amber_ore")),
DEEPSLATE_TEMPORAL_GOLD_ORE(def("deepslate_temporal_gold_ore")),
DEEPSLATE_TEMPORAL_REDSTONE_ORE(def("deepslate_temporal_redstone_ore")),
DEEPSLATE_CLOCKSTONE_ORE(def("deepslate_clockstone_ore")),
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
git commit -m "feat: add deepslate temporal stone and deepslate ore block IDs"
```

---

## Task 2: Create Placeholder Textures

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/deepslate_temporal_stone.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/deepslate_temporal_amber_ore.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/deepslate_temporal_gold_ore.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/deepslate_temporal_redstone_ore.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/deepslate_clockstone_ore.png`

- [ ] **Step 1: Generate placeholder textures**

Create 16x16 magenta/black checkerboard placeholder PNGs for each texture. Use the existing `temporal_stone.png` as a size reference. Generate with ImageMagick or Python PIL:

```bash
cd common/shared/src/main/resources/assets/chronodawn/textures/block/
# Create a 16x16 magenta-black checkerboard placeholder
for name in deepslate_temporal_stone deepslate_temporal_amber_ore deepslate_temporal_gold_ore deepslate_temporal_redstone_ore deepslate_clockstone_ore; do
  convert -size 16x16 pattern:checkerboard -auto-level \
    -fill magenta -opaque white \
    -fill black -opaque black \
    "${name}.png"
done
```

If ImageMagick is not available, copy `temporal_stone.png` as placeholder and rename:

```bash
cd common/shared/src/main/resources/assets/chronodawn/textures/block/
for name in deepslate_temporal_stone deepslate_temporal_amber_ore deepslate_temporal_gold_ore deepslate_temporal_redstone_ore deepslate_clockstone_ore; do
  cp temporal_stone.png "${name}.png"
done
```

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/deepslate_*.png
git commit -m "feat: add placeholder textures for deepslate temporal stone and ores"
```

---

## Task 3: Create Shared JSON Resources (Blockstates, Models, Item Models)

**Files (all in `common/shared/src/main/resources/assets/chronodawn/`):**
- Create: `blockstates/deepslate_temporal_stone.json`
- Create: `blockstates/deepslate_temporal_stone_stairs.json`
- Create: `blockstates/deepslate_temporal_stone_slab.json`
- Create: `blockstates/deepslate_temporal_stone_wall.json`
- Create: `blockstates/deepslate_temporal_amber_ore.json`
- Create: `blockstates/deepslate_temporal_gold_ore.json`
- Create: `blockstates/deepslate_temporal_redstone_ore.json`
- Create: `blockstates/deepslate_clockstone_ore.json`
- Create: `models/block/deepslate_temporal_stone.json`
- Create: `models/block/deepslate_temporal_stone_stairs.json`
- Create: `models/block/deepslate_temporal_stone_stairs_inner.json`
- Create: `models/block/deepslate_temporal_stone_stairs_outer.json`
- Create: `models/block/deepslate_temporal_stone_slab.json`
- Create: `models/block/deepslate_temporal_stone_slab_top.json`
- Create: `models/block/deepslate_temporal_stone_wall_post.json`
- Create: `models/block/deepslate_temporal_stone_wall_side.json`
- Create: `models/block/deepslate_temporal_stone_wall_side_tall.json`
- Create: `models/block/deepslate_temporal_amber_ore.json`
- Create: `models/block/deepslate_temporal_gold_ore.json`
- Create: `models/block/deepslate_temporal_redstone_ore.json`
- Create: `models/block/deepslate_clockstone_ore.json`
- Create: `models/item/deepslate_temporal_stone.json`
- Create: `models/item/deepslate_temporal_stone_stairs.json`
- Create: `models/item/deepslate_temporal_stone_slab.json`
- Create: `models/item/deepslate_temporal_stone_wall.json`
- Create: `models/item/deepslate_temporal_amber_ore.json`
- Create: `models/item/deepslate_temporal_gold_ore.json`
- Create: `models/item/deepslate_temporal_redstone_ore.json`
- Create: `models/item/deepslate_clockstone_ore.json`

- [ ] **Step 1: Create blockstate JSONs**

`blockstates/deepslate_temporal_stone.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/deepslate_temporal_stone"
    }
  }
}
```

`blockstates/deepslate_temporal_stone_stairs.json`: Copy from `blockstates/temporal_stone_stairs.json`, replacing all `chronodawn:block/temporal_stone_stairs` references with `chronodawn:block/deepslate_temporal_stone_stairs` (including `_inner` and `_outer` variants).

`blockstates/deepslate_temporal_stone_slab.json`:
```json
{
  "variants": {
    "type=bottom": { "model": "chronodawn:block/deepslate_temporal_stone_slab" },
    "type=double": { "model": "chronodawn:block/deepslate_temporal_stone" },
    "type=top": { "model": "chronodawn:block/deepslate_temporal_stone_slab_top" }
  }
}
```

`blockstates/deepslate_temporal_stone_wall.json`: Copy from `blockstates/temporal_stone_wall.json`, replacing all `chronodawn:block/temporal_stone_wall` references with `chronodawn:block/deepslate_temporal_stone_wall`.

`blockstates/deepslate_temporal_amber_ore.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/deepslate_temporal_amber_ore"
    }
  }
}
```

`blockstates/deepslate_temporal_gold_ore.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/deepslate_temporal_gold_ore"
    }
  }
}
```

`blockstates/deepslate_temporal_redstone_ore.json` (has `lit` property like temporal_redstone_ore):
```json
{
  "variants": {
    "lit=false": {
      "model": "chronodawn:block/deepslate_temporal_redstone_ore"
    },
    "lit=true": {
      "model": "chronodawn:block/deepslate_temporal_redstone_ore"
    }
  }
}
```

`blockstates/deepslate_clockstone_ore.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/deepslate_clockstone_ore"
    }
  }
}
```

- [ ] **Step 2: Create block model JSONs**

All simple ore/stone models use `cube_all` parent:

`models/block/deepslate_temporal_stone.json`:
```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/deepslate_temporal_stone"
  }
}
```

Repeat this pattern for each deepslate ore model (`deepslate_temporal_amber_ore`, `deepslate_temporal_gold_ore`, `deepslate_temporal_redstone_ore`, `deepslate_clockstone_ore`), each pointing to its own texture.

Stairs models (3 files):

`models/block/deepslate_temporal_stone_stairs.json`:
```json
{
  "parent": "minecraft:block/stairs",
  "textures": {
    "bottom": "chronodawn:block/deepslate_temporal_stone",
    "top": "chronodawn:block/deepslate_temporal_stone",
    "side": "chronodawn:block/deepslate_temporal_stone"
  }
}
```

`models/block/deepslate_temporal_stone_stairs_inner.json`:
```json
{
  "parent": "minecraft:block/inner_stairs",
  "textures": {
    "bottom": "chronodawn:block/deepslate_temporal_stone",
    "top": "chronodawn:block/deepslate_temporal_stone",
    "side": "chronodawn:block/deepslate_temporal_stone"
  }
}
```

`models/block/deepslate_temporal_stone_stairs_outer.json`:
```json
{
  "parent": "minecraft:block/outer_stairs",
  "textures": {
    "bottom": "chronodawn:block/deepslate_temporal_stone",
    "top": "chronodawn:block/deepslate_temporal_stone",
    "side": "chronodawn:block/deepslate_temporal_stone"
  }
}
```

Slab models (2 files):

`models/block/deepslate_temporal_stone_slab.json`:
```json
{
  "parent": "minecraft:block/slab",
  "textures": {
    "bottom": "chronodawn:block/deepslate_temporal_stone",
    "top": "chronodawn:block/deepslate_temporal_stone",
    "side": "chronodawn:block/deepslate_temporal_stone"
  }
}
```

`models/block/deepslate_temporal_stone_slab_top.json`:
```json
{
  "parent": "minecraft:block/slab_top",
  "textures": {
    "bottom": "chronodawn:block/deepslate_temporal_stone",
    "top": "chronodawn:block/deepslate_temporal_stone",
    "side": "chronodawn:block/deepslate_temporal_stone"
  }
}
```

Wall models (3 files):

`models/block/deepslate_temporal_stone_wall_post.json`:
```json
{
  "parent": "minecraft:block/template_wall_post",
  "textures": { "wall": "chronodawn:block/deepslate_temporal_stone" }
}
```

`models/block/deepslate_temporal_stone_wall_side.json`:
```json
{
  "parent": "minecraft:block/template_wall_side",
  "textures": { "wall": "chronodawn:block/deepslate_temporal_stone" }
}
```

`models/block/deepslate_temporal_stone_wall_side_tall.json`:
```json
{
  "parent": "minecraft:block/template_wall_side_tall",
  "textures": { "wall": "chronodawn:block/deepslate_temporal_stone" }
}
```

- [ ] **Step 3: Create item model JSONs**

Simple block items reference their block model:

`models/item/deepslate_temporal_stone.json`:
```json
{
  "parent": "chronodawn:block/deepslate_temporal_stone"
}
```

Same pattern for stairs, slab, and all 4 ores:
- `models/item/deepslate_temporal_stone_stairs.json` → parent `chronodawn:block/deepslate_temporal_stone_stairs`
- `models/item/deepslate_temporal_stone_slab.json` → parent `chronodawn:block/deepslate_temporal_stone_slab`
- `models/item/deepslate_temporal_amber_ore.json` → parent `chronodawn:block/deepslate_temporal_amber_ore`
- `models/item/deepslate_temporal_gold_ore.json` → parent `chronodawn:block/deepslate_temporal_gold_ore`
- `models/item/deepslate_temporal_redstone_ore.json` → parent `chronodawn:block/deepslate_temporal_redstone_ore`
- `models/item/deepslate_clockstone_ore.json` → parent `chronodawn:block/deepslate_clockstone_ore`

Wall item uses special inventory model:

`models/item/deepslate_temporal_stone_wall.json`:
```json
{
  "parent": "minecraft:block/wall_inventory",
  "textures": { "wall": "chronodawn:block/deepslate_temporal_stone" }
}
```

- [ ] **Step 4: Run resource validation**

Run: `./gradlew validateResources`
Expected: No errors for new files (warnings about missing blocks OK until Java classes added)

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/deepslate_*
git add common/shared/src/main/resources/assets/chronodawn/models/block/deepslate_*
git add common/shared/src/main/resources/assets/chronodawn/models/item/deepslate_*
git commit -m "feat: add blockstate, block model, and item model JSONs for deepslate blocks"
```

---

## Task 4: Create Items JSONs for 1.21.4 and 1.21.5+

**Files (8 items × 2 locations = 16 files):**
- Create in `common/1.21.4/src/main/resources/assets/chronodawn/items/`: all 8 deepslate block items
- Create in `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/`: all 8 deepslate block items

- [ ] **Step 1: Create items JSONs**

For each of the 8 blocks, create identical files in both locations. The format is the same for all:

`items/deepslate_temporal_stone.json`:
```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/deepslate_temporal_stone"}}
```

Repeat for all 8 blocks:
- `deepslate_temporal_stone.json`
- `deepslate_temporal_stone_stairs.json`
- `deepslate_temporal_stone_slab.json`
- `deepslate_temporal_stone_wall.json`
- `deepslate_temporal_amber_ore.json`
- `deepslate_temporal_gold_ore.json`
- `deepslate_temporal_redstone_ore.json`
- `deepslate_clockstone_ore.json`

Each file has the same structure: `{"model":{"type":"minecraft:model","model":"chronodawn:item/<block_id>"}}`.

- [ ] **Step 2: Commit**

```bash
git add common/1.21.4/src/main/resources/assets/chronodawn/items/deepslate_*
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/deepslate_*
git commit -m "feat: add items JSONs for deepslate blocks (1.21.4 and 1.21.5+)"
```

---

## Task 5: Create Block Classes — Era A (1.20.1)

**Files (all in `common/1.20.1/src/main/java/com/chronodawn/blocks/`):**
- Create: `DeepslateTemporalStoneBlock.java`
- Create: `DeepslateTemporalStoneStairs.java`
- Create: `DeepslateTemporalStoneSlab.java`
- Create: `DeepslateTemporalStoneWall.java`
- Create: `DeepslateTemporalAmberOre.java`
- Create: `DeepslateTemporalGoldOre.java`
- Create: `DeepslateTemporalRedstoneOre.java`
- Create: `DeepslateClockstoneOre.java`

- [ ] **Step 1: Create DeepslateTemporalStoneBlock.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DeepslateTemporalStoneBlock extends Block {
    public DeepslateTemporalStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
```

- [ ] **Step 2: Create DeepslateTemporalStoneStairs.java**

```java
package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneStairs extends StairBlock {
    public DeepslateTemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.DEEPSLATE_TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties();
    }
}
```

- [ ] **Step 3: Create DeepslateTemporalStoneSlab.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneSlab extends SlabBlock {
    public DeepslateTemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties();
    }
}
```

- [ ] **Step 4: Create DeepslateTemporalStoneWall.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneWall extends WallBlock {
    public DeepslateTemporalStoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties();
    }
}
```

- [ ] **Step 5: Create DeepslateTemporalAmberOre.java**

```java
package com.chronodawn.blocks;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DeepslateTemporalAmberOre extends DropExperienceBlock {
    public DeepslateTemporalAmberOre(BlockBehaviour.Properties properties) {
        super(properties, UniformInt.of(2, 5));
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
```

Note: 1.20.1 DropExperienceBlock ctor is `super(properties, xpRange)`.

- [ ] **Step 6: Create DeepslateTemporalGoldOre.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DeepslateTemporalGoldOre extends Block {
    public DeepslateTemporalGoldOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
```

- [ ] **Step 7: Create DeepslateTemporalRedstoneOre.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DeepslateTemporalRedstoneOre extends RedStoneOreBlock {
    public DeepslateTemporalRedstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE)
                .randomTicks()
                .lightLevel(state -> state.getValue(RedStoneOreBlock.LIT) ? 9 : 0)
                .isRedstoneConductor((state, getter, pos) -> false);
    }
}
```

- [ ] **Step 8: Create DeepslateClockstoneOre.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DeepslateClockstoneOre extends Block {
    public DeepslateClockstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
```

- [ ] **Step 9: Verify compilation**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Expected: BUILD SUCCESSFUL (block classes compile but aren't registered yet — that's OK)

- [ ] **Step 10: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/blocks/Deepslate*.java
git commit -m "feat: add deepslate block classes for 1.20.1 (Era A)"
```

---

## Task 6: Create Block Classes — Era B (1.21.1)

**Files (all in `common/1.21.1/src/main/java/com/chronodawn/blocks/`):**
- Create: same 8 files as Task 5

- [ ] **Step 1: Create all 8 block classes**

Identical to Task 5 (Era A) except for `DeepslateTemporalAmberOre.java` — the DropExperienceBlock constructor order is reversed:

```java
public class DeepslateTemporalAmberOre extends DropExperienceBlock {
    public DeepslateTemporalAmberOre(BlockBehaviour.Properties properties) {
        super(UniformInt.of(2, 5), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
```

Note: 1.21.1 DropExperienceBlock ctor is `super(xpRange, properties)`.

All other 7 block classes are identical to Era A.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/1.21.1/src/main/java/com/chronodawn/blocks/Deepslate*.java
git commit -m "feat: add deepslate block classes for 1.21.1 (Era B)"
```

---

## Task 7: Create Block Classes — Era C (1.21.2, 1.21.4–1.21.10)

**Files:** Same 8 block classes in each of 9 version directories:
`common/1.21.2/`, `common/1.21.4/`, `common/1.21.5/`, `common/1.21.6/`, `common/1.21.7/`, `common/1.21.8/`, `common/1.21.9/`, `common/1.21.10/`

- [ ] **Step 1: Create block classes for 1.21.2 first (template)**

`DeepslateTemporalStoneBlock.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneBlock extends Block {
    public DeepslateTemporalStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_stone")));
    }
}
```

`DeepslateTemporalStoneStairs.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneStairs extends StairBlock {
    public DeepslateTemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.DEEPSLATE_TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_stone_stairs")));
    }
}
```

`DeepslateTemporalStoneSlab.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneSlab extends SlabBlock {
    public DeepslateTemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_stone_slab")));
    }
}
```

`DeepslateTemporalStoneWall.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneWall extends WallBlock {
    public DeepslateTemporalStoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_stone_wall")));
    }
}
```

`DeepslateTemporalAmberOre.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalAmberOre extends DropExperienceBlock {
    public DeepslateTemporalAmberOre(BlockBehaviour.Properties properties) {
        super(UniformInt.of(2, 5), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_amber_ore")));
    }
}
```

`DeepslateTemporalGoldOre.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalGoldOre extends Block {
    public DeepslateTemporalGoldOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_gold_ore")));
    }
}
```

`DeepslateTemporalRedstoneOre.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalRedstoneOre extends RedStoneOreBlock {
    public DeepslateTemporalRedstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_REDSTONE_ORE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_redstone_ore")));
    }
}
```

`DeepslateClockstoneOre.java`:
```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateClockstoneOre extends Block {
    public DeepslateClockstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                .strength(4.5f, 3.0f)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_clockstone_ore")));
    }
}
```

- [ ] **Step 2: Copy to all Era C versions**

Copy all 8 files from `common/1.21.2/src/main/java/com/chronodawn/blocks/` to:
- `common/1.21.4/src/main/java/com/chronodawn/blocks/`
- `common/1.21.5/src/main/java/com/chronodawn/blocks/`
- `common/1.21.6/src/main/java/com/chronodawn/blocks/`
- `common/1.21.7/src/main/java/com/chronodawn/blocks/`
- `common/1.21.8/src/main/java/com/chronodawn/blocks/`
- `common/1.21.9/src/main/java/com/chronodawn/blocks/`
- `common/1.21.10/src/main/java/com/chronodawn/blocks/`

Files are byte-for-byte identical across all Era C versions.

- [ ] **Step 3: Verify compilation for Era C**

Run: `./gradlew :common-1.21.2:compileJava -Ptarget_mc_version=1.21.2`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/1.21.2/src/main/java/com/chronodawn/blocks/Deepslate*.java
git add common/1.21.4/src/main/java/com/chronodawn/blocks/Deepslate*.java
git add common/1.21.5/src/main/java/com/chronodawn/blocks/Deepslate*.java
git add common/1.21.6/src/main/java/com/chronodawn/blocks/Deepslate*.java
git add common/1.21.7/src/main/java/com/chronodawn/blocks/Deepslate*.java
git add common/1.21.8/src/main/java/com/chronodawn/blocks/Deepslate*.java
git add common/1.21.9/src/main/java/com/chronodawn/blocks/Deepslate*.java
git add common/1.21.10/src/main/java/com/chronodawn/blocks/Deepslate*.java
git commit -m "feat: add deepslate block classes for 1.21.2-1.21.10 (Era C)"
```

---

## Task 8: Create Block Classes — Era D (1.21.11)

**Files:** Same 8 block classes in `common/1.21.11/src/main/java/com/chronodawn/blocks/`

- [ ] **Step 1: Create block classes**

Copy from Era C (1.21.2), then replace `ResourceLocation` with `Identifier` in all files:
- Change import: `import net.minecraft.resources.ResourceLocation;` → `import net.minecraft.resources.Identifier;`
- Change usage: `ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`

All other code is identical to Era C.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/Deepslate*.java
git commit -m "feat: add deepslate block classes for 1.21.11 (Era D)"
```

---

## Task 9: Register Blocks in ModBlocks.java — All Versions

**Files:** `common/<version>/src/main/java/com/chronodawn/registry/ModBlocks.java` for all 11 versions

- [ ] **Step 1: Add registrations to all versions**

Add after the existing `TEMPORAL_STONE_WALL` registration in ModBlocks.java:

```java
// Deepslate Temporal Stone variants
public static final RegistrySupplier<Block> DEEPSLATE_TEMPORAL_STONE = BLOCKS.register(
    ModBlockId.DEEPSLATE_TEMPORAL_STONE.id(),
    () -> new DeepslateTemporalStoneBlock(DeepslateTemporalStoneBlock.createProperties())
);
public static final RegistrySupplier<Block> DEEPSLATE_TEMPORAL_STONE_STAIRS = BLOCKS.register(
    ModBlockId.DEEPSLATE_TEMPORAL_STONE_STAIRS.id(),
    () -> new DeepslateTemporalStoneStairs(DeepslateTemporalStoneStairs.createProperties())
);
public static final RegistrySupplier<Block> DEEPSLATE_TEMPORAL_STONE_SLAB = BLOCKS.register(
    ModBlockId.DEEPSLATE_TEMPORAL_STONE_SLAB.id(),
    () -> new DeepslateTemporalStoneSlab(DeepslateTemporalStoneSlab.createProperties())
);
public static final RegistrySupplier<Block> DEEPSLATE_TEMPORAL_STONE_WALL = BLOCKS.register(
    ModBlockId.DEEPSLATE_TEMPORAL_STONE_WALL.id(),
    () -> new DeepslateTemporalStoneWall(DeepslateTemporalStoneWall.createProperties())
);
```

Add after the existing `TEMPORAL_AMBER_ORE` registration:

```java
// Deepslate ores
public static final RegistrySupplier<Block> DEEPSLATE_TEMPORAL_AMBER_ORE = BLOCKS.register(
    ModBlockId.DEEPSLATE_TEMPORAL_AMBER_ORE.id(),
    () -> new DeepslateTemporalAmberOre(DeepslateTemporalAmberOre.createProperties())
);
public static final RegistrySupplier<Block> DEEPSLATE_TEMPORAL_GOLD_ORE = BLOCKS.register(
    ModBlockId.DEEPSLATE_TEMPORAL_GOLD_ORE.id(),
    () -> new DeepslateTemporalGoldOre(DeepslateTemporalGoldOre.createProperties())
);
public static final RegistrySupplier<Block> DEEPSLATE_TEMPORAL_REDSTONE_ORE = BLOCKS.register(
    ModBlockId.DEEPSLATE_TEMPORAL_REDSTONE_ORE.id(),
    () -> new DeepslateTemporalRedstoneOre(DeepslateTemporalRedstoneOre.createProperties())
);
public static final RegistrySupplier<Block> DEEPSLATE_CLOCKSTONE_ORE = BLOCKS.register(
    ModBlockId.DEEPSLATE_CLOCKSTONE_ORE.id(),
    () -> new DeepslateClockstoneOre(DeepslateClockstoneOre.createProperties())
);
```

Also add the necessary imports to each version's ModBlocks.java:
```java
import com.chronodawn.blocks.DeepslateTemporalStoneBlock;
import com.chronodawn.blocks.DeepslateTemporalStoneStairs;
import com.chronodawn.blocks.DeepslateTemporalStoneSlab;
import com.chronodawn.blocks.DeepslateTemporalStoneWall;
import com.chronodawn.blocks.DeepslateTemporalAmberOre;
import com.chronodawn.blocks.DeepslateTemporalGoldOre;
import com.chronodawn.blocks.DeepslateTemporalRedstoneOre;
import com.chronodawn.blocks.DeepslateClockstoneOre;
```

This registration code is identical across all 11 versions.

- [ ] **Step 2: Verify compilation for representative versions**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1 && ./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1 && ./gradlew :common-1.21.2:compileJava -Ptarget_mc_version=1.21.2 && ./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL for all

- [ ] **Step 3: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModBlocks.java
git commit -m "feat: register deepslate blocks in ModBlocks.java for all versions"
```

---

## Task 10: Create Loot Tables

**Files:**
- Create 8 loot tables in `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/`
- Create 8 loot tables in `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/`

Note: 1.20.1 uses `loot_tables/` (plural), 1.21.1+ uses `loot_table/` (singular).

- [ ] **Step 1: Create deepslate_temporal_stone loot table**

`loot_table/blocks/deepslate_temporal_stone.json` (drops self, no cobblestone variant):
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:deepslate_temporal_stone"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
```

- [ ] **Step 2: Create deepslate stone derivative loot tables**

`deepslate_temporal_stone_stairs.json`, `deepslate_temporal_stone_slab.json`, `deepslate_temporal_stone_wall.json` — same pattern as step 1 but with each block's own name.

For the slab, use the standard slab loot table with double-drop for `type=double`:
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:deepslate_temporal_stone_slab",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 2,
              "conditions": [
                {
                  "condition": "minecraft:block_state_property",
                  "block": "chronodawn:deepslate_temporal_stone_slab",
                  "properties": { "type": "double" }
                }
              ]
            },
            { "function": "minecraft:explosion_decay" }
          ]
        }
      ]
    }
  ]
}
```

Check: verify existing `temporal_stone_slab` loot table for the exact slab pattern used in this project, and follow it.

- [ ] **Step 3: Create deepslate ore loot tables**

Each deepslate ore should drop the same items as its non-deepslate variant, but silk touch drops the deepslate version.

`deepslate_temporal_amber_ore.json`:
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
              "name": "chronodawn:deepslate_temporal_amber_ore"
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
              "name": "chronodawn:raw_temporal_amber"
            }
          ]
        }
      ]
    }
  ]
}
```

`deepslate_temporal_gold_ore.json`: Same structure as `temporal_gold_ore.json` but silk touch drops `chronodawn:deepslate_temporal_gold_ore`, normal drops `minecraft:raw_gold`.

`deepslate_temporal_redstone_ore.json`: Same structure as `temporal_redstone_ore.json` but silk touch drops `chronodawn:deepslate_temporal_redstone_ore`, normal drops `minecraft:redstone` (4-5 with uniform_bonus_count fortune).

`deepslate_clockstone_ore.json`: Same structure as `clockstone_ore.json` but silk touch drops `chronodawn:deepslate_clockstone_ore`, normal drops `chronodawn:clockstone` (3-4 with ore_drops fortune).

- [ ] **Step 4: Copy to 1.20.1 (plural directory)**

Copy all 8 loot tables from `common/shared-1.21.1+/.../loot_table/blocks/` to `common/1.20.1/.../loot_tables/blocks/`. Contents are identical; only directory path differs.

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/deepslate_*
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/deepslate_*
git commit -m "feat: add loot tables for deepslate blocks"
```

---

## Task 11: Update Tags

**Files:**
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/needs_iron_tool.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/needs_iron_tool.json`

- [ ] **Step 1: Update pickaxe mineable tags**

Add all 8 new blocks to `mineable/pickaxe.json` in both locations:

```json
"chronodawn:deepslate_temporal_stone",
"chronodawn:deepslate_temporal_stone_stairs",
"chronodawn:deepslate_temporal_stone_slab",
"chronodawn:deepslate_temporal_stone_wall",
"chronodawn:deepslate_temporal_amber_ore",
"chronodawn:deepslate_temporal_gold_ore",
"chronodawn:deepslate_temporal_redstone_ore",
"chronodawn:deepslate_clockstone_ore"
```

- [ ] **Step 2: Update needs_iron_tool tags**

Add all 4 deepslate ores to `needs_iron_tool.json` in both locations:

```json
"chronodawn:deepslate_temporal_amber_ore",
"chronodawn:deepslate_temporal_gold_ore",
"chronodawn:deepslate_temporal_redstone_ore",
"chronodawn:deepslate_clockstone_ore"
```

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json
git add common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/needs_iron_tool.json
git add common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json
git add common/1.20.1/src/main/resources/data/minecraft/tags/blocks/needs_iron_tool.json
git commit -m "feat: add deepslate blocks to mineable and tool tier tags"
```

---

## Task 12: Update Translations

**Files:**
- Modify: `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- Modify: `common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/1.21.1/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- Modify: `common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/1.20.1/src/main/resources/assets/chronodawn/lang/ja_jp.json`

- [ ] **Step 1: Add English translations**

Add to all 3 `en_us.json` files (near the temporal stone entries):

```json
"block.chronodawn.deepslate_temporal_stone": "Deepslate Temporal Stone",
"block.chronodawn.deepslate_temporal_stone_stairs": "Deepslate Temporal Stone Stairs",
"block.chronodawn.deepslate_temporal_stone_slab": "Deepslate Temporal Stone Slab",
"block.chronodawn.deepslate_temporal_stone_wall": "Deepslate Temporal Stone Wall",
"block.chronodawn.deepslate_temporal_amber_ore": "Deepslate Temporal Amber Ore",
"block.chronodawn.deepslate_temporal_gold_ore": "Deepslate Temporal Gold Ore",
"block.chronodawn.deepslate_temporal_redstone_ore": "Deepslate Temporal Redstone Ore",
"block.chronodawn.deepslate_clockstone_ore": "Deepslate Clockstone Ore",
```

- [ ] **Step 2: Add Japanese translations**

Add to all 3 `ja_jp.json` files:

```json
"block.chronodawn.deepslate_temporal_stone": "深層テンポラルストーン",
"block.chronodawn.deepslate_temporal_stone_stairs": "深層テンポラルストーンの階段",
"block.chronodawn.deepslate_temporal_stone_slab": "深層テンポラルストーンのハーフブロック",
"block.chronodawn.deepslate_temporal_stone_wall": "深層テンポラルストーンの塀",
"block.chronodawn.deepslate_temporal_amber_ore": "深層テンポラルアンバー鉱石",
"block.chronodawn.deepslate_temporal_gold_ore": "深層テンポラル金鉱石",
"block.chronodawn.deepslate_temporal_redstone_ore": "深層テンポラルレッドストーン鉱石",
"block.chronodawn.deepslate_clockstone_ore": "深層クロックストーン鉱石",
```

- [ ] **Step 3: Run translation validation**

Run: `./gradlew validateTranslations`
Expected: No missing translation key errors

- [ ] **Step 4: Commit**

```bash
git add common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/*.json
git add common/1.21.1/src/main/resources/assets/chronodawn/lang/*.json
git add common/1.20.1/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: add translations for deepslate blocks (en/ja)"
```

---

## Task 13: Update Worldgen — Noise Settings (Surface Rules)

**Files:** `common/<version>/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json` for all 11 versions:
1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11

- [ ] **Step 1: Update deepslate surface rule in all noise_settings**

In each file, find the deepslate surface rule block:

```json
"result_state": {
  "Name": "minecraft:deepslate",
  "Properties": {
    "axis": "y"
  }
}
```

Replace with:

```json
"result_state": {
  "Name": "chronodawn:deepslate_temporal_stone"
}
```

Note: `deepslate_temporal_stone` has no `axis` property (it's a cube_all block, not a RotatedPillarBlock), so remove the `Properties` object.

- [ ] **Step 2: Commit**

```bash
git add common/*/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json
git commit -m "feat: replace vanilla deepslate with deepslate_temporal_stone in surface rules"
```

---

## Task 14: Update Worldgen — Ore Configured Features

**Files (all in `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/`):**
- Modify: `ore_temporal_amber.json`
- Modify: `ore_gold.json`
- Modify: `ore_redstone.json`
- Modify: `ore_clockstone.json`

- [ ] **Step 1: Update ore_temporal_amber.json**

Replace the `minecraft:deepslate` target with `chronodawn:deepslate_temporal_stone`, and change the ore state to the deepslate variant:

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 3,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:temporal_stone" },
        "state": { "Name": "chronodawn:temporal_amber_ore" }
      },
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:deepslate_temporal_stone" },
        "state": { "Name": "chronodawn:deepslate_temporal_amber_ore" }
      }
    ]
  }
}
```

- [ ] **Step 2: Update ore_gold.json**

Add deepslate target:

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
      },
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:deepslate_temporal_stone" },
        "state": { "Name": "chronodawn:deepslate_temporal_gold_ore" }
      }
    ]
  }
}
```

- [ ] **Step 3: Update ore_redstone.json**

Add deepslate target:

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
      },
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:deepslate_temporal_stone" },
        "state": { "Name": "chronodawn:deepslate_temporal_redstone_ore" }
      }
    ]
  }
}
```

- [ ] **Step 4: Update ore_clockstone.json**

Add deepslate target:

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
      },
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "chronodawn:deepslate_temporal_stone" },
        "state": { "Name": "chronodawn:deepslate_clockstone_ore" }
      }
    ]
  }
}
```

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_*.json
git commit -m "feat: add deepslate ore targets to configured features"
```

---

## Task 15: Build and Full Verification

- [ ] **Step 1: Run resource validation**

Run: `./gradlew validateResources`
Expected: No errors

- [ ] **Step 2: Run translation validation**

Run: `./gradlew validateTranslations`
Expected: No missing keys

- [ ] **Step 3: Build all versions**

Run: `./gradlew buildAll`
Expected: BUILD SUCCESSFUL for all versions

- [ ] **Step 4: Run unit tests**

Run: `./gradlew testAll`
Expected: All tests pass

- [ ] **Step 5: Run GameTests**

Run: `./gradlew gameTestAll`
Expected: All GameTests pass

- [ ] **Step 6: Report results to user**

Present build results and ask user if they want to proceed with further testing (e.g., running the client to verify in-game appearance).
