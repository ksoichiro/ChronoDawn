# Temporal Stone Block Set Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `temporal_stone` and `temporal_cobblestone` blocks with stairs/slab/wall variants as Chrono Dawn dimension terrain base blocks.

**Architecture:** Follow the existing clockstone block pattern exactly — per-block Java classes in each version directory, shared resources in `common/shared/`, version-split loot tables (1.20.1 `loot_tables/` vs 1.21.1+ `loot_table/`). The `temporal_stone` loot table uses silk-touch alternatives (like `temporal_grass_block`).

**Tech Stack:** Java 21, Architectury API, Minecraft modding (Block/StairBlock/SlabBlock/WallBlock)

---

## File Map

### Shared (common/shared/)
- **Create:** `src/main/java/com/chronodawn/registry/ModBlockId.java` — add 8 entries
- **Create:** `src/main/java/com/chronodawn/registry/ModItemId.java` — add 8 entries
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_stone.json`
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_cobblestone.json`
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_stone_stairs.json`
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_stone_slab.json`
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_stone_wall.json`
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_cobblestone_stairs.json`
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_cobblestone_slab.json`
- **Create:** `src/main/resources/assets/chronodawn/blockstates/temporal_cobblestone_wall.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_stairs.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_stairs_inner.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_stairs_outer.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_slab.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_slab_top.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_wall_post.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_wall_side.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_stone_wall_side_tall.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_stairs.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_stairs_inner.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_stairs_outer.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_slab.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_slab_top.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_wall_post.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_wall_side.json`
- **Create:** `src/main/resources/assets/chronodawn/models/block/temporal_cobblestone_wall_side_tall.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_stone.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_cobblestone.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_stone_stairs.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_stone_slab.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_stone_wall.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_cobblestone_stairs.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_cobblestone_slab.json`
- **Create:** `src/main/resources/assets/chronodawn/models/item/temporal_cobblestone_wall.json`
- **Create:** `src/main/resources/assets/chronodawn/textures/block/temporal_stone.png`
- **Create:** `src/main/resources/assets/chronodawn/textures/block/temporal_cobblestone.png`

### Version-specific block classes (per MC version)
For each version (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11):
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneBlock.java`
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneBlock.java`
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneStairs.java`
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneSlab.java`
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneWall.java`
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneStairs.java`
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneSlab.java`
- **Create:** `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneWall.java`
- **Modify:** `common/<ver>/src/main/java/com/chronodawn/registry/ModBlocks.java` — add 8 registrations
- **Modify:** `common/<ver>/src/main/java/com/chronodawn/registry/ModItems.java` — add 8 registrations + creative tab entries

### Loot tables
- **Create (1.20.1):** `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_stone.json` (+ 7 more)
- **Create (1.21.1+):** `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_stone.json` (+ 7 more)

### Tags
- **Modify (1.20.1):** `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- **Modify (1.21.1+):** `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`

### Translations
- **Modify (1.20.1):** `common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json`
- **Modify (1.20.1):** `common/1.20.1/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- **Modify (1.21.2+):** `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json`
- **Modify (1.21.2+):** `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/ja_jp.json`

### Client Items JSON (1.21.4+)
- **Create (1.21.4):** `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_stone.json` (+ 7 more)
- **Create (1.21.5+):** `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_stone.json` (+ 7 more)

---

## Version Groups

Block classes have 4 distinct patterns based on API differences:

| Group | Versions | Key Differences |
|---|---|---|
| A | 1.20.1, 1.21.1 | `BlockBehaviour.Properties.of()` with explicit props, no `setId()` |
| B | 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10 | `BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)` + `.setId(ResourceKey...)` with `ResourceLocation` |
| C | 1.21.11 | Same as B but `Identifier` instead of `ResourceLocation` |

ModItems.java has matching version splits:
- 1.20.1: `new Item.Properties()` only
- 1.21.1: `new Item.Properties()` only (same as 1.20.1)
- 1.21.2–1.21.10: `new Item.Properties().useBlockDescriptionPrefix().setId(ResourceKey...)` with `ResourceLocation`
- 1.21.11: Same but `Identifier` instead of `ResourceLocation`

---

### Task 1: Register Block and Item IDs

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Add block IDs to ModBlockId.java**

Add the 8 new block IDs after the existing `TEMPORAL_BRICKS_WALL` entry (or in a logical grouping near other terrain blocks). All use default SOLID render layer:

```java
// Temporal Stone variants
TEMPORAL_STONE(def("temporal_stone")),
TEMPORAL_COBBLESTONE(def("temporal_cobblestone")),
TEMPORAL_STONE_STAIRS(def("temporal_stone_stairs")),
TEMPORAL_STONE_SLAB(def("temporal_stone_slab")),
TEMPORAL_STONE_WALL(def("temporal_stone_wall")),
TEMPORAL_COBBLESTONE_STAIRS(def("temporal_cobblestone_stairs")),
TEMPORAL_COBBLESTONE_SLAB(def("temporal_cobblestone_slab")),
TEMPORAL_COBBLESTONE_WALL(def("temporal_cobblestone_wall")),
```

- [ ] **Step 2: Add item IDs to ModItemId.java**

Add matching entries in the same order:

```java
// Temporal Stone variants
TEMPORAL_STONE("temporal_stone"),
TEMPORAL_COBBLESTONE("temporal_cobblestone"),
TEMPORAL_STONE_STAIRS("temporal_stone_stairs"),
TEMPORAL_STONE_SLAB("temporal_stone_slab"),
TEMPORAL_STONE_WALL("temporal_stone_wall"),
TEMPORAL_COBBLESTONE_STAIRS("temporal_cobblestone_stairs"),
TEMPORAL_COBBLESTONE_SLAB("temporal_cobblestone_slab"),
TEMPORAL_COBBLESTONE_WALL("temporal_cobblestone_wall"),
```

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat: add temporal stone block and item IDs"
```

---

### Task 2: Create Block Classes (Group A: 1.20.1, 1.21.1)

**Files (per version):**
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneBlock.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneBlock.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneStairs.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneSlab.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalStoneWall.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneStairs.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneSlab.java`
- Create: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalCobblestoneWall.java`

- [ ] **Step 1: Create TemporalStoneBlock.java for 1.20.1 and 1.21.1**

Both versions use identical code:

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

- [ ] **Step 2: Create TemporalCobblestoneBlock.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TemporalCobblestoneBlock extends Block {
    public TemporalCobblestoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
```

- [ ] **Step 3: Create TemporalStoneStairs.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneStairs extends StairBlock {
    public TemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBlock.createProperties();
    }
}
```

- [ ] **Step 4: Create TemporalStoneSlab.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneSlab extends SlabBlock {
    public TemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBlock.createProperties();
    }
}
```

- [ ] **Step 5: Create TemporalStoneWall.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneWall extends WallBlock {
    public TemporalStoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBlock.createProperties();
    }
}
```

- [ ] **Step 6: Create TemporalCobblestoneStairs.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneStairs extends StairBlock {
    public TemporalCobblestoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_COBBLESTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalCobblestoneBlock.createProperties();
    }
}
```

- [ ] **Step 7: Create TemporalCobblestoneSlab.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneSlab extends SlabBlock {
    public TemporalCobblestoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalCobblestoneBlock.createProperties();
    }
}
```

- [ ] **Step 8: Create TemporalCobblestoneWall.java for 1.20.1 and 1.21.1**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneWall extends WallBlock {
    public TemporalCobblestoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalCobblestoneBlock.createProperties();
    }
}
```

- [ ] **Step 9: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.1/src/main/java/com/chronodawn/blocks/Temporal*.java
git commit -m "feat: add temporal stone block classes for 1.20.1 and 1.21.1"
```

---

### Task 3: Create Block Classes (Group B: 1.21.2–1.21.10)

**Files:** Same 8 block classes per version for 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10.

These versions use `BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)` and `.setId(ResourceKey...)` with `ResourceLocation`.

- [ ] **Step 1: Create TemporalStoneBlock.java for Group B versions**

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

- [ ] **Step 2: Create TemporalCobblestoneBlock.java for Group B versions**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneBlock extends Block {
    public TemporalCobblestoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_cobblestone")));
    }
}
```

- [ ] **Step 3: Create TemporalStoneStairs.java for Group B versions**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneStairs extends StairBlock {
    public TemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stone_stairs")));
    }
}
```

- [ ] **Step 4: Create TemporalStoneSlab.java for Group B versions**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneSlab extends SlabBlock {
    public TemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stone_slab")));
    }
}
```

- [ ] **Step 5: Create TemporalStoneWall.java for Group B versions**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneWall extends WallBlock {
    public TemporalStoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stone_wall")));
    }
}
```

- [ ] **Step 6: Create TemporalCobblestoneStairs.java for Group B versions**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneStairs extends StairBlock {
    public TemporalCobblestoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_COBBLESTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalCobblestoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_cobblestone_stairs")));
    }
}
```

- [ ] **Step 7: Create TemporalCobblestoneSlab.java for Group B versions**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneSlab extends SlabBlock {
    public TemporalCobblestoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalCobblestoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_cobblestone_slab")));
    }
}
```

- [ ] **Step 8: Create TemporalCobblestoneWall.java for Group B versions**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneWall extends WallBlock {
    public TemporalCobblestoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalCobblestoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_cobblestone_wall")));
    }
}
```

- [ ] **Step 9: Copy all 8 files to each Group B version directory**

Copy the files to: `common/1.21.2/`, `common/1.21.4/`, `common/1.21.5/`, `common/1.21.6/`, `common/1.21.7/`, `common/1.21.8/`, `common/1.21.9/`, `common/1.21.10/`

- [ ] **Step 10: Commit**

```bash
git add common/1.21.2/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.4/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.5/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.6/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.7/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.8/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.9/src/main/java/com/chronodawn/blocks/Temporal*.java
git add common/1.21.10/src/main/java/com/chronodawn/blocks/Temporal*.java
git commit -m "feat: add temporal stone block classes for 1.21.2-1.21.10"
```

---

### Task 4: Create Block Classes (Group C: 1.21.11)

**Files:** Same 8 block classes in `common/1.21.11/`

These are identical to Group B except `ResourceLocation` → `Identifier`.

- [ ] **Step 1: Create TemporalStoneBlock.java for 1.21.11**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stone")));
    }
}
```

- [ ] **Step 2: Create TemporalCobblestoneBlock.java for 1.21.11**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneBlock extends Block {
    public TemporalCobblestoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_cobblestone")));
    }
}
```

- [ ] **Step 3: Create remaining 6 variant classes for 1.21.11**

Same as Group B Steps 3–8, but replace:
- `import net.minecraft.resources.ResourceLocation;` → `import net.minecraft.resources.Identifier;`
- `ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`

Files: `TemporalStoneStairs.java`, `TemporalStoneSlab.java`, `TemporalStoneWall.java`, `TemporalCobblestoneStairs.java`, `TemporalCobblestoneSlab.java`, `TemporalCobblestoneWall.java`

- [ ] **Step 4: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/Temporal*.java
git commit -m "feat: add temporal stone block classes for 1.21.11"
```

---

### Task 5: Register Blocks in ModBlocks.java (All Versions)

**Files:** Modify `common/<ver>/src/main/java/com/chronodawn/registry/ModBlocks.java` for all 11 versions.

- [ ] **Step 1: Add block registrations to all ModBlocks.java files**

Add after the existing temporal terrain block entries (near `TEMPORAL_DIRT`, `TEMPORAL_GRASS_BLOCK`, `COARSE_TEMPORAL_DIRT`). All versions use the same registration pattern:

```java
// Temporal Stone variants
public static final RegistrySupplier<Block> TEMPORAL_STONE = BLOCKS.register(
    ModBlockId.TEMPORAL_STONE.id(),
    () -> new TemporalStoneBlock(TemporalStoneBlock.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_COBBLESTONE = BLOCKS.register(
    ModBlockId.TEMPORAL_COBBLESTONE.id(),
    () -> new TemporalCobblestoneBlock(TemporalCobblestoneBlock.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_STONE_STAIRS = BLOCKS.register(
    ModBlockId.TEMPORAL_STONE_STAIRS.id(),
    () -> new TemporalStoneStairs(TemporalStoneStairs.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_STONE_SLAB = BLOCKS.register(
    ModBlockId.TEMPORAL_STONE_SLAB.id(),
    () -> new TemporalStoneSlab(TemporalStoneSlab.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_STONE_WALL = BLOCKS.register(
    ModBlockId.TEMPORAL_STONE_WALL.id(),
    () -> new TemporalStoneWall(TemporalStoneWall.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_COBBLESTONE_STAIRS = BLOCKS.register(
    ModBlockId.TEMPORAL_COBBLESTONE_STAIRS.id(),
    () -> new TemporalCobblestoneStairs(TemporalCobblestoneStairs.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_COBBLESTONE_SLAB = BLOCKS.register(
    ModBlockId.TEMPORAL_COBBLESTONE_SLAB.id(),
    () -> new TemporalCobblestoneSlab(TemporalCobblestoneSlab.createProperties())
);

public static final RegistrySupplier<Block> TEMPORAL_COBBLESTONE_WALL = BLOCKS.register(
    ModBlockId.TEMPORAL_COBBLESTONE_WALL.id(),
    () -> new TemporalCobblestoneWall(TemporalCobblestoneWall.createProperties())
);
```

Also add imports for the 8 new block classes in each file.

- [ ] **Step 2: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModBlocks.java
git commit -m "feat: register temporal stone blocks in ModBlocks for all versions"
```

---

### Task 6: Register Block Items in ModItems.java (All Versions)

**Files:** Modify `common/<ver>/src/main/java/com/chronodawn/registry/ModItems.java` for all 11 versions.

- [ ] **Step 1: Add block item registrations for 1.20.1 and 1.21.1**

```java
// Temporal Stone variants
public static final RegistrySupplier<Item> TEMPORAL_STONE = ITEMS.register(
    ModItemId.TEMPORAL_STONE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_STONE.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_COBBLESTONE = ITEMS.register(
    ModItemId.TEMPORAL_COBBLESTONE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_COBBLESTONE.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_STONE_STAIRS = ITEMS.register(
    ModItemId.TEMPORAL_STONE_STAIRS.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_STONE_STAIRS.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_STONE_SLAB = ITEMS.register(
    ModItemId.TEMPORAL_STONE_SLAB.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_STONE_SLAB.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_STONE_WALL = ITEMS.register(
    ModItemId.TEMPORAL_STONE_WALL.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_STONE_WALL.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_COBBLESTONE_STAIRS = ITEMS.register(
    ModItemId.TEMPORAL_COBBLESTONE_STAIRS.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_COBBLESTONE_STAIRS.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_COBBLESTONE_SLAB = ITEMS.register(
    ModItemId.TEMPORAL_COBBLESTONE_SLAB.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_COBBLESTONE_SLAB.get(), new Item.Properties())
);

public static final RegistrySupplier<Item> TEMPORAL_COBBLESTONE_WALL = ITEMS.register(
    ModItemId.TEMPORAL_COBBLESTONE_WALL.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_COBBLESTONE_WALL.get(), new Item.Properties())
);
```

- [ ] **Step 2: Add block item registrations for 1.21.2–1.21.10**

Same pattern but with `.useBlockDescriptionPrefix()` and `.setId(ResourceKey...)`:

```java
public static final RegistrySupplier<Item> TEMPORAL_STONE = ITEMS.register(
    ModItemId.TEMPORAL_STONE.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_STONE.get(), new Item.Properties()
            .useBlockDescriptionPrefix()
            .setId(ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_STONE.id()))))
);
// ... repeat for all 8 items with matching IDs
```

- [ ] **Step 3: Add block item registrations for 1.21.11**

Same as Step 2 but `Identifier.fromNamespaceAndPath(...)` instead of `ResourceLocation.fromNamespaceAndPath(...)`.

- [ ] **Step 4: Add creative tab entries in all versions**

In the `populateCreativeTab()` method, add after the existing terrain block entries (near `TEMPORAL_DIRT`, `COARSE_TEMPORAL_DIRT`):

```java
output.accept(TEMPORAL_STONE.get());
output.accept(TEMPORAL_STONE_STAIRS.get());
output.accept(TEMPORAL_STONE_SLAB.get());
output.accept(TEMPORAL_STONE_WALL.get());
output.accept(TEMPORAL_COBBLESTONE.get());
output.accept(TEMPORAL_COBBLESTONE_STAIRS.get());
output.accept(TEMPORAL_COBBLESTONE_SLAB.get());
output.accept(TEMPORAL_COBBLESTONE_WALL.get());
```

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register temporal stone block items for all versions"
```

---

### Task 7: Create Textures

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_stone.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_cobblestone.png`

- [ ] **Step 1: Create temporal_stone.png**

Use a Python script to color-transform vanilla `stone.png` to match the temporal theme (purple-blue tint). The script should:
1. Read the vanilla stone texture from the Minecraft JAR or use an extracted copy
2. Apply a hue shift toward purple/blue to match existing temporal blocks
3. Save as 16x16 PNG

If vanilla textures aren't directly available, create a placeholder 16x16 purple-tinted stone texture programmatically.

- [ ] **Step 2: Create temporal_cobblestone.png**

Same color transformation applied to vanilla `cobblestone.png`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_stone.png
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_cobblestone.png
git commit -m "feat: add temporal stone textures"
```

---

### Task 8: Create Blockstate JSONs

**Files:** All in `common/shared/src/main/resources/assets/chronodawn/blockstates/`

- [ ] **Step 1: Create temporal_stone.json and temporal_cobblestone.json blockstates**

`temporal_stone.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/temporal_stone"
    }
  }
}
```

`temporal_cobblestone.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/temporal_cobblestone"
    }
  }
}
```

- [ ] **Step 2: Create stairs blockstates (temporal_stone_stairs.json, temporal_cobblestone_stairs.json)**

Copy the exact structure from `clockstone_stairs.json`, replacing `chronodawn:block/clockstone_stairs` with `chronodawn:block/temporal_stone_stairs` (and `_inner`/`_outer` variants). Same for cobblestone variant.

The stairs blockstate has all `facing`/`half`/`shape` combinations with appropriate `x`/`y` rotations and `uvlock: true`. (Full 209-line file; copy from existing `clockstone_stairs.json` and search-replace the model name.)

- [ ] **Step 3: Create slab blockstates (temporal_stone_slab.json, temporal_cobblestone_slab.json)**

`temporal_stone_slab.json`:
```json
{
  "variants": {
    "type=bottom": {
      "model": "chronodawn:block/temporal_stone_slab"
    },
    "type=double": {
      "model": "chronodawn:block/temporal_stone"
    },
    "type=top": {
      "model": "chronodawn:block/temporal_stone_slab_top"
    }
  }
}
```

`temporal_cobblestone_slab.json`:
```json
{
  "variants": {
    "type=bottom": {
      "model": "chronodawn:block/temporal_cobblestone_slab"
    },
    "type=double": {
      "model": "chronodawn:block/temporal_cobblestone"
    },
    "type=top": {
      "model": "chronodawn:block/temporal_cobblestone_slab_top"
    }
  }
}
```

- [ ] **Step 4: Create wall blockstates (temporal_stone_wall.json, temporal_cobblestone_wall.json)**

Copy from `clockstone_wall.json`, replacing `chronodawn:block/clockstone_wall_post` → `chronodawn:block/temporal_stone_wall_post`, `clockstone_wall_side` → `temporal_stone_wall_side`, `clockstone_wall_side_tall` → `temporal_stone_wall_side_tall`. Same for cobblestone.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_stone*.json
git add common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_cobblestone*.json
git commit -m "feat: add temporal stone blockstate JSONs"
```

---

### Task 9: Create Block Model JSONs

**Files:** All in `common/shared/src/main/resources/assets/chronodawn/models/block/`

- [ ] **Step 1: Create base block models**

`temporal_stone.json`:
```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/temporal_stone"
  }
}
```

`temporal_cobblestone.json`:
```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/temporal_cobblestone"
  }
}
```

- [ ] **Step 2: Create stairs models (3 per block: normal, inner, outer)**

For temporal stone (repeat pattern for cobblestone, replacing texture):

`temporal_stone_stairs.json`:
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

`temporal_stone_stairs_inner.json`:
```json
{
  "parent": "minecraft:block/inner_stairs",
  "textures": {
    "bottom": "chronodawn:block/temporal_stone",
    "top": "chronodawn:block/temporal_stone",
    "side": "chronodawn:block/temporal_stone"
  }
}
```

`temporal_stone_stairs_outer.json`:
```json
{
  "parent": "minecraft:block/outer_stairs",
  "textures": {
    "bottom": "chronodawn:block/temporal_stone",
    "top": "chronodawn:block/temporal_stone",
    "side": "chronodawn:block/temporal_stone"
  }
}
```

Cobblestone: same 3 files with `temporal_cobblestone` texture.

- [ ] **Step 3: Create slab models (2 per block: bottom, top)**

`temporal_stone_slab.json`:
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

`temporal_stone_slab_top.json`:
```json
{
  "parent": "minecraft:block/slab_top",
  "textures": {
    "bottom": "chronodawn:block/temporal_stone",
    "top": "chronodawn:block/temporal_stone",
    "side": "chronodawn:block/temporal_stone"
  }
}
```

Cobblestone: same 2 files with `temporal_cobblestone` texture.

- [ ] **Step 4: Create wall models (3 per block: post, side, side_tall)**

`temporal_stone_wall_post.json`:
```json
{
  "parent": "minecraft:block/template_wall_post",
  "textures": {
    "wall": "chronodawn:block/temporal_stone"
  }
}
```

`temporal_stone_wall_side.json`:
```json
{
  "parent": "minecraft:block/template_wall_side",
  "textures": {
    "wall": "chronodawn:block/temporal_stone"
  }
}
```

`temporal_stone_wall_side_tall.json`:
```json
{
  "parent": "minecraft:block/template_wall_side_tall",
  "textures": {
    "wall": "chronodawn:block/temporal_stone"
  }
}
```

Cobblestone: same 3 files with `temporal_cobblestone` texture.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/block/temporal_stone*.json
git add common/shared/src/main/resources/assets/chronodawn/models/block/temporal_cobblestone*.json
git commit -m "feat: add temporal stone block model JSONs"
```

---

### Task 10: Create Item Model JSONs

**Files:**
- `common/shared/src/main/resources/assets/chronodawn/models/item/` — 8 files
- `common/1.21.4/src/main/resources/assets/chronodawn/items/` — 8 files
- `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/` — 8 files

- [ ] **Step 1: Create item models in common/shared/models/item/**

Standard block items reference parent block model:

`temporal_stone.json`:
```json
{
  "parent": "chronodawn:block/temporal_stone"
}
```

Same pattern for: `temporal_cobblestone.json`, `temporal_stone_stairs.json`, `temporal_stone_slab.json`, `temporal_cobblestone_stairs.json`, `temporal_cobblestone_slab.json`.

Wall items use `wall_inventory` parent:

`temporal_stone_wall.json`:
```json
{
  "parent": "minecraft:block/wall_inventory",
  "textures": {
    "wall": "chronodawn:block/temporal_stone"
  }
}
```

`temporal_cobblestone_wall.json`:
```json
{
  "parent": "minecraft:block/wall_inventory",
  "textures": {
    "wall": "chronodawn:block/temporal_cobblestone"
  }
}
```

- [ ] **Step 2: Create Client Items JSONs for 1.21.4 and shared-1.21.5+**

For each of the 8 blocks, create in both `common/1.21.4/src/main/resources/assets/chronodawn/items/` and `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/`:

`temporal_stone.json`:
```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/temporal_stone"}}
```

Repeat for all 8 block names: `temporal_stone`, `temporal_cobblestone`, `temporal_stone_stairs`, `temporal_stone_slab`, `temporal_stone_wall`, `temporal_cobblestone_stairs`, `temporal_cobblestone_slab`, `temporal_cobblestone_wall`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/item/temporal_stone*.json
git add common/shared/src/main/resources/assets/chronodawn/models/item/temporal_cobblestone*.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_stone*.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_cobblestone*.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_stone*.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_cobblestone*.json
git commit -m "feat: add temporal stone item model JSONs"
```

---

### Task 11: Create Loot Tables

**Files:**
- 1.20.1: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/` — 8 files
- 1.21.1+: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/` — 8 files

- [ ] **Step 1: Create temporal_stone loot table (silk touch → self, otherwise → cobblestone)**

Both 1.20.1 and 1.21.1+ versions have the same content (only directory path differs):

`temporal_stone.json`:
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
                    "enchantments": [
                      {
                        "enchantment": "minecraft:silk_touch",
                        "levels": {
                          "min": 1
                        }
                      }
                    ]
                  }
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "chronodawn:temporal_cobblestone"
            }
          ]
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

- [ ] **Step 2: Create simple drop loot tables for cobblestone, stairs, wall**

Standard drop-self pattern for: `temporal_cobblestone.json`, `temporal_stone_stairs.json`, `temporal_stone_wall.json`, `temporal_cobblestone_stairs.json`, `temporal_cobblestone_wall.json`:

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:<block_id>"
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

- [ ] **Step 3: Create slab loot tables (double slab → 2 drops)**

`temporal_stone_slab.json` and `temporal_cobblestone_slab.json`:

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
              "conditions": [
                {
                  "condition": "minecraft:block_state_property",
                  "block": "chronodawn:<slab_block_id>",
                  "properties": {
                    "type": "double"
                  }
                }
              ],
              "count": 2,
              "add": false
            },
            {
              "function": "minecraft:explosion_decay"
            }
          ],
          "name": "chronodawn:<slab_block_id>"
        }
      ]
    }
  ]
}
```

- [ ] **Step 4: Copy all 8 loot tables to both locations**

1.20.1: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/`
1.21.1+: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/`

- [ ] **Step 5: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_stone*.json
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_cobblestone*.json
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_stone*.json
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_cobblestone*.json
git commit -m "feat: add temporal stone loot tables"
```

---

### Task 12: Update Mining Tags and Translations

**Files:**
- Tags: `common/1.20.1/.../tags/blocks/mineable/pickaxe.json`, `common/shared-1.21.1+/.../tags/block/mineable/pickaxe.json`
- Lang: `common/1.20.1/.../lang/en_us.json`, `common/1.20.1/.../lang/ja_jp.json`, `common/shared-1.21.2+/.../lang/en_us.json`, `common/shared-1.21.2+/.../lang/ja_jp.json`

- [ ] **Step 1: Add blocks to pickaxe mineable tags**

Add to both tag files' `values` array:

```json
"chronodawn:temporal_stone",
"chronodawn:temporal_cobblestone",
"chronodawn:temporal_stone_stairs",
"chronodawn:temporal_stone_slab",
"chronodawn:temporal_stone_wall",
"chronodawn:temporal_cobblestone_stairs",
"chronodawn:temporal_cobblestone_slab",
"chronodawn:temporal_cobblestone_wall"
```

- [ ] **Step 2: Add English translations**

Add to both `en_us.json` files (1.20.1 and shared-1.21.2+):

```json
"block.chronodawn.temporal_stone": "Temporal Stone",
"block.chronodawn.temporal_cobblestone": "Temporal Cobblestone",
"block.chronodawn.temporal_stone_stairs": "Temporal Stone Stairs",
"block.chronodawn.temporal_stone_slab": "Temporal Stone Slab",
"block.chronodawn.temporal_stone_wall": "Temporal Stone Wall",
"block.chronodawn.temporal_cobblestone_stairs": "Temporal Cobblestone Stairs",
"block.chronodawn.temporal_cobblestone_slab": "Temporal Cobblestone Slab",
"block.chronodawn.temporal_cobblestone_wall": "Temporal Cobblestone Wall"
```

- [ ] **Step 3: Add Japanese translations**

Add to both `ja_jp.json` files (1.20.1 and shared-1.21.2+):

```json
"block.chronodawn.temporal_stone": "テンポラルストーン",
"block.chronodawn.temporal_cobblestone": "テンポラル丸石",
"block.chronodawn.temporal_stone_stairs": "テンポラルストーンの階段",
"block.chronodawn.temporal_stone_slab": "テンポラルストーンのハーフブロック",
"block.chronodawn.temporal_stone_wall": "テンポラルストーンの塀",
"block.chronodawn.temporal_cobblestone_stairs": "テンポラル丸石の階段",
"block.chronodawn.temporal_cobblestone_slab": "テンポラル丸石のハーフブロック",
"block.chronodawn.temporal_cobblestone_wall": "テンポラル丸石の塀"
```

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json
git add common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json
git add common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json
git add common/1.20.1/src/main/resources/assets/chronodawn/lang/ja_jp.json
git add common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json
git add common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/ja_jp.json
git commit -m "feat: add temporal stone mining tags and translations"
```

---

### Task 13: Build Verification

- [ ] **Step 1: Run resource validation**

```bash
./gradlew validateResources
```

Expected: PASS — all blockstate→model and model→texture references resolve.

- [ ] **Step 2: Run translation validation**

```bash
./gradlew validateTranslations
```

Expected: PASS — all block translation keys present.

- [ ] **Step 3: Build for 1.21.2 (representative version)**

```bash
./gradlew clean1_21_2 build1_21_2
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Run unit tests**

```bash
./gradlew :common-1.21.2:test -Ptarget_mc_version=1.21.2
```

Expected: All tests pass (including CreativeTabCompletenessTest if it checks for new blocks).

- [ ] **Step 5: Build all versions**

```bash
./gradlew buildAll
```

Expected: BUILD SUCCESSFUL for all versions.

- [ ] **Step 6: Run full test suite**

```bash
./gradlew testAll
```

Expected: All tests pass.
