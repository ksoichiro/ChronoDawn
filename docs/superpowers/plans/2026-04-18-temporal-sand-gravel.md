# Temporal Sand & Gravel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 6 new blocks (`temporal_sand`, `temporal_gravel`, `temporal_sandstone` + stairs/slab/wall) that replace vanilla `sand`/`red_sand`/`gravel`/`sandstone`/`red_sandstone` in the Chrono Dawn dimension across all 11 supported Minecraft versions (1.20.1〜1.21.11).

**Architecture:** New ChronoDawn-namespaced blocks registered through Architectury DeferredRegister. Worldgen integration replaces vanilla block IDs in `noise_settings/chronodawn.json` surface_rule sections (24 occurrences per version) and `sand_disk.json`/`gravel_disk.json` configured_features. Vanilla compat tags (`#minecraft:sand`/`gravel`/`sandstone_blocks`) preserve TNT/concrete/glass-smelting recipes.

**Tech Stack:** Java 21, Architectury API (Fabric + NeoForge), Mojang mappings, Python (Pillow) for texture generation

**Spec:** `docs/superpowers/specs/2026-04-18-temporal-sand-gravel-design.md`

---

## Version Era Reference

Block class patterns differ across versions. Plan tasks reference these eras:

| Era | Versions | Properties helper | setId | ResourceLocation class |
|-----|----------|-------------------|-------|------------------------|
| A | 1.20.1 | `CompatBlockProperties.ofFullCopy(Blocks.X)` | No | N/A |
| B | 1.21.1 | `CompatBlockProperties.ofFullCopy(Blocks.X)` | No | N/A |
| C | 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10 | `BlockBehaviour.Properties.ofFullCopy(Blocks.X)` | Yes | `ResourceLocation` |
| D | 1.21.11 | `BlockBehaviour.Properties.ofFullCopy(Blocks.X)` | Yes | `Identifier` |

Recipe JSON formats also split by version (memory: "Recipe JSON format splits by version"):

| Era | dir | `key.X` | `result` |
|-----|-----|---------|----------|
| A (1.20.1) | `recipes/` | `{"item":"id"}` | `{"item":"id","count":N}` |
| B (1.21.1) | `recipe/` | `{"item":"id"}` | `{"id":"id","count":N}` |
| C (1.21.2+) | `recipe/` | `"id"` (string) | `{"id":"id","count":N}` |

---

## Task 1: Add 6 Block IDs to ModBlockId.java and ModItemId.java

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Add IDs to ModBlockId.java**

Add a new section after the `// Temporal Stone variants` block (after `TEMPORAL_STONE_PRESSURE_PLATE`):

```java
    // Temporal Sand / Gravel / Sandstone variants
    TEMPORAL_SAND(def("temporal_sand")),
    TEMPORAL_GRAVEL(def("temporal_gravel")),
    TEMPORAL_SANDSTONE(def("temporal_sandstone")),
    TEMPORAL_SANDSTONE_STAIRS(def("temporal_sandstone_stairs")),
    TEMPORAL_SANDSTONE_SLAB(def("temporal_sandstone_slab")),
    TEMPORAL_SANDSTONE_WALL(def("temporal_sandstone_wall")),
```

- [ ] **Step 2: Add matching IDs to ModItemId.java**

Locate the corresponding section (next to `TEMPORAL_STONE_*` entries) and add:

```java
    TEMPORAL_SAND("temporal_sand"),
    TEMPORAL_GRAVEL("temporal_gravel"),
    TEMPORAL_SANDSTONE("temporal_sandstone"),
    TEMPORAL_SANDSTONE_STAIRS("temporal_sandstone_stairs"),
    TEMPORAL_SANDSTONE_SLAB("temporal_sandstone_slab"),
    TEMPORAL_SANDSTONE_WALL("temporal_sandstone_wall"),
```

(Match the existing entry constructor signature; if `ModItemId` uses a different `def(...)` helper, copy the existing pattern instead of guessing.)

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java \
        common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat: add temporal sand/gravel/sandstone block and item IDs"
```

---

## Task 2: Stage Placeholder Textures (temporal_dirt copy)

**Decision:** Use the existing project-owned `temporal_dirt.png` as placeholder for all 5 new textures. Real pale-ice-blue textures generated from vanilla sources are out of scope for this task; the placeholder keeps the asset pipeline complete so blockstates/models validate. Replace later with the texture-generation script.

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sand.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_gravel.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone.png` (side)
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone_top.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone_bottom.png`

- [ ] **Step 1: Copy temporal_dirt.png to all 5 destination filenames**

```bash
cd common/shared/src/main/resources/assets/chronodawn/textures/block/
for name in temporal_sand temporal_gravel temporal_sandstone temporal_sandstone_top temporal_sandstone_bottom; do
  cp temporal_dirt.png "${name}.png"
done
```

- [ ] **Step 2: Verify file presence**

```bash
ls -1 common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sand.png \
       common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_gravel.png \
       common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone.png \
       common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone_top.png \
       common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone_bottom.png
```

Expected: all 5 paths print successfully.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sand.png \
        common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_gravel.png \
        common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone.png \
        common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone_top.png \
        common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_sandstone_bottom.png
git commit -m "feat: add placeholder textures (temporal_dirt copy) for temporal sand/gravel/sandstone"
```

---

## Task 3: Block classes for Era A (1.20.1)

**Files (all in `common/1.20.1/src/main/java/com/chronodawn/blocks/`):**
- Create: `TemporalSand.java`
- Create: `TemporalGravel.java`
- Create: `TemporalSandstone.java`
- Create: `TemporalSandstoneStairs.java`
- Create: `TemporalSandstoneSlab.java`
- Create: `TemporalSandstoneWall.java`

- [ ] **Step 1: TemporalSand.java**

```java
package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSand extends FallingBlock {
    public TemporalSand(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.SAND);
    }
}
```

- [ ] **Step 2: TemporalGravel.java**

1.20.1 has no public `GravelBlock`; extend `FallingBlock` directly. Flint drop is wired through the loot table only (Task 13).

```java
package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalGravel extends FallingBlock {
    public TemporalGravel(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.GRAVEL);
    }
}
```

- [ ] **Step 3: TemporalSandstone.java**

```java
package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstone extends Block {
    public TemporalSandstone(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.SANDSTONE);
    }
}
```

- [ ] **Step 4: TemporalSandstoneStairs.java**

```java
package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneStairs extends StairBlock {
    public TemporalSandstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_SANDSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties();
    }
}
```

- [ ] **Step 5: TemporalSandstoneSlab.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneSlab extends SlabBlock {
    public TemporalSandstoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties();
    }
}
```

- [ ] **Step 6: TemporalSandstoneWall.java**

```java
package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneWall extends WallBlock {
    public TemporalSandstoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties();
    }
}
```

- [ ] **Step 7: Verify compilation**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Expected: BUILD SUCCESSFUL (will fail referencing `ModBlocks.TEMPORAL_SANDSTONE` until Task 7; defer commit until then)

---

## Task 4: Block classes for Era B (1.21.1)

**Files (all in `common/1.21.1/src/main/java/com/chronodawn/blocks/`):**
Same 6 filenames as Task 3.

- [ ] **Step 1: Copy each Era A class to its 1.21.1 location verbatim**

Era B (1.21.1) uses identical signatures to Era A: same `CompatBlockProperties.ofFullCopy(...)` helper, no `setId()`. The 6 files in Task 3 can be reused as-is. Do NOT use `BlockBehaviour.Properties.ofFullCopy` directly here.

```bash
for f in TemporalSand.java TemporalGravel.java TemporalSandstone.java \
         TemporalSandstoneStairs.java TemporalSandstoneSlab.java TemporalSandstoneWall.java; do
  cp "common/1.20.1/src/main/java/com/chronodawn/blocks/$f" \
     "common/1.21.1/src/main/java/com/chronodawn/blocks/$f"
done
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: BUILD SUCCESSFUL once Task 8 lands; commit deferred.

---

## Task 5: Block classes for Era C (1.21.2 〜 1.21.10)

**Files (all under `common/<ver>/src/main/java/com/chronodawn/blocks/`, ver ∈ {1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10}):**
Same 6 filenames as Task 3 × 8 versions = 48 files.

- [ ] **Step 1: TemporalSand.java template**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSand extends FallingBlock {
    public TemporalSand(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_sand")));
    }
}
```

- [ ] **Step 2: TemporalGravel.java template**

1.21.1+ exposes `GravelBlock` (which provides flint behaviour); use it.

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GravelBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalGravel extends GravelBlock {
    public TemporalGravel(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_gravel")));
    }
}
```

- [ ] **Step 3: TemporalSandstone.java template**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstone extends Block {
    public TemporalSandstone(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_sandstone")));
    }
}
```

- [ ] **Step 4: TemporalSandstoneStairs.java template**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneStairs extends StairBlock {
    public TemporalSandstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_SANDSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_sandstone_stairs")));
    }
}
```

- [ ] **Step 5: TemporalSandstoneSlab.java template**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneSlab extends SlabBlock {
    public TemporalSandstoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_sandstone_slab")));
    }
}
```

- [ ] **Step 6: TemporalSandstoneWall.java template**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneWall extends WallBlock {
    public TemporalSandstoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_sandstone_wall")));
    }
}
```

- [ ] **Step 7: Replicate the 6 templates into all 8 Era C versions**

For each version V in {1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10}, copy the 6 files from a finished version to `common/V/src/main/java/com/chronodawn/blocks/`. The templates above are bit-identical across these 8 versions.

- [ ] **Step 8: Sanity check — no unintended file changes**

Per memory "Subagent-Driven Development: Watch for Unintended Edits":

```bash
git diff --name-only -- '*.java' | grep -Ev 'TemporalSand|TemporalGravel|TemporalSandstone|ModBlock|ModItem'
```

Expected: empty output (only the new Temporal* files and registry edits show up).

- [ ] **Step 9: Verify compilation**

Run: `./gradlew :common-1.21.10:compileJava -Ptarget_mc_version=1.21.10`
Expected: BUILD SUCCESSFUL after Task 9 lands; commit deferred until then.

---

## Task 6: Block classes for Era D (1.21.11)

**Files (all in `common/1.21.11/src/main/java/com/chronodawn/blocks/`):**
Same 6 filenames as Task 3.

- [ ] **Step 1: Copy Era C versions and rewrite imports**

Start from any Era C copy (e.g., `common/1.21.10/.../TemporalSand.java`). For each of the 6 files:

- Replace `import net.minecraft.resources.ResourceLocation;` with `import net.minecraft.resources.Identifier;`
- Replace `ResourceLocation.fromNamespaceAndPath(` with `Identifier.fromNamespaceAndPath(`

(memory: "Util package moved in 1.21.11" — same pattern as the existing `TemporalDirtBlock.java` in 1.21.11.)

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL after Task 10 lands; commit deferred.

---

## Task 7: ModBlocks/ModItems registration for Era A (1.20.1)

**Files:**
- Modify: `common/1.20.1/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.20.1/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Add imports + registrations to ModBlocks.java**

Add imports near the existing `TemporalStone*` imports:

```java
import com.chronodawn.blocks.TemporalSand;
import com.chronodawn.blocks.TemporalGravel;
import com.chronodawn.blocks.TemporalSandstone;
import com.chronodawn.blocks.TemporalSandstoneStairs;
import com.chronodawn.blocks.TemporalSandstoneSlab;
import com.chronodawn.blocks.TemporalSandstoneWall;
```

Add registration entries near the existing `TEMPORAL_STONE_WALL` entry:

```java
    public static final RegistrySupplier<Block> TEMPORAL_SAND = BLOCKS.register(
        ModBlockId.TEMPORAL_SAND.id(),
        () -> new TemporalSand(TemporalSand.createProperties())
    );

    public static final RegistrySupplier<Block> TEMPORAL_GRAVEL = BLOCKS.register(
        ModBlockId.TEMPORAL_GRAVEL.id(),
        () -> new TemporalGravel(TemporalGravel.createProperties())
    );

    public static final RegistrySupplier<Block> TEMPORAL_SANDSTONE = BLOCKS.register(
        ModBlockId.TEMPORAL_SANDSTONE.id(),
        () -> new TemporalSandstone(TemporalSandstone.createProperties())
    );

    public static final RegistrySupplier<Block> TEMPORAL_SANDSTONE_STAIRS = BLOCKS.register(
        ModBlockId.TEMPORAL_SANDSTONE_STAIRS.id(),
        () -> new TemporalSandstoneStairs(TemporalSandstoneStairs.createProperties())
    );

    public static final RegistrySupplier<Block> TEMPORAL_SANDSTONE_SLAB = BLOCKS.register(
        ModBlockId.TEMPORAL_SANDSTONE_SLAB.id(),
        () -> new TemporalSandstoneSlab(TemporalSandstoneSlab.createProperties())
    );

    public static final RegistrySupplier<Block> TEMPORAL_SANDSTONE_WALL = BLOCKS.register(
        ModBlockId.TEMPORAL_SANDSTONE_WALL.id(),
        () -> new TemporalSandstoneWall(TemporalSandstoneWall.createProperties())
    );
```

- [ ] **Step 2: Add BlockItem registrations to ModItems.java (Era A pattern)**

```java
    public static final RegistrySupplier<Item> TEMPORAL_SAND = ITEMS.register(
        ModItemId.TEMPORAL_SAND.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_SAND.get(), new Item.Properties())
    );

    public static final RegistrySupplier<Item> TEMPORAL_GRAVEL = ITEMS.register(
        ModItemId.TEMPORAL_GRAVEL.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_GRAVEL.get(), new Item.Properties())
    );

    public static final RegistrySupplier<Item> TEMPORAL_SANDSTONE = ITEMS.register(
        ModItemId.TEMPORAL_SANDSTONE.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_SANDSTONE.get(), new Item.Properties())
    );

    public static final RegistrySupplier<Item> TEMPORAL_SANDSTONE_STAIRS = ITEMS.register(
        ModItemId.TEMPORAL_SANDSTONE_STAIRS.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_SANDSTONE_STAIRS.get(), new Item.Properties())
    );

    public static final RegistrySupplier<Item> TEMPORAL_SANDSTONE_SLAB = ITEMS.register(
        ModItemId.TEMPORAL_SANDSTONE_SLAB.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_SANDSTONE_SLAB.get(), new Item.Properties())
    );

    public static final RegistrySupplier<Item> TEMPORAL_SANDSTONE_WALL = ITEMS.register(
        ModItemId.TEMPORAL_SANDSTONE_WALL.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_SANDSTONE_WALL.get(), new Item.Properties())
    );
```

- [ ] **Step 3: Add to creative tab**

Find the `populateCreativeTab(...)` method in `ModItems.java`. Near the existing temporal-stone group, append:

```java
        output.accept(TEMPORAL_SAND.get());
        output.accept(TEMPORAL_GRAVEL.get());
        output.accept(TEMPORAL_SANDSTONE.get());
        output.accept(TEMPORAL_SANDSTONE_STAIRS.get());
        output.accept(TEMPORAL_SANDSTONE_SLAB.get());
        output.accept(TEMPORAL_SANDSTONE_WALL.get());
```

- [ ] **Step 4: Build 1.20.1**

Run: `./gradlew build1_20_1`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.20.1/src/main/java/com/chronodawn/registry/ModBlocks.java \
        common/1.20.1/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(1.20.1): register temporal sand/gravel/sandstone blocks"
```

---

## Task 8: ModBlocks/ModItems registration for Era B (1.21.1)

**Files:** Same shape as Task 7 but under `common/1.21.1/`.

- [ ] **Step 1: Apply Task 7 Steps 1–3 to the 1.21.1 ModBlocks/ModItems**

Era B uses the same registration pattern as Era A (no `setId()` on item Properties, no `useBlockDescriptionPrefix()`). Copy the entries verbatim.

- [ ] **Step 2: Build 1.21.1**

Run: `./gradlew build1_21_1`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/1.21.1/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java \
        common/1.21.1/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(1.21.1): register temporal sand/gravel/sandstone blocks"
```

---

## Task 9: ModBlocks/ModItems registration for Era C (1.21.2 〜 1.21.10)

**Files:** Same shape as Task 7 × 8 versions under `common/1.21.{2,4,5,6,7,8,9,10}/`.

- [ ] **Step 1: ModBlocks.java additions (template)**

Imports + registrations are byte-identical to Task 7 Step 1. Same 6 entries.

- [ ] **Step 2: ModItems.java additions (Era C item Properties pattern)**

Copy the Task 7 Step 2 block but expand each `Item.Properties()` to:

```java
new Item.Properties()
        .useBlockDescriptionPrefix()
        .setId(ResourceKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_SAND.id())))
```

(memory: "Bare new Item requires setId" — Era C without `.setId()` NPEs at runtime.) Repeat for all 6 IDs, swapping `TEMPORAL_SAND` for each.

- [ ] **Step 3: Add to creative tab**

Same `output.accept(...)` 6 lines as Task 7 Step 3.

- [ ] **Step 4: Replicate across all 8 Era C versions**

Apply Steps 1–3 in each of `common/1.21.{2,4,5,6,7,8,9,10}/src/main/java/com/chronodawn/registry/Mod{Blocks,Items}.java`.

- [ ] **Step 5: Build 1.21.10 (latest Era C)**

Run: `./gradlew build1_21_10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add common/1.21.2/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.4/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.5/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.6/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.7/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.8/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.9/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.10/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.2/src/main/java/com/chronodawn/registry/Mod*.java \
        common/1.21.4/src/main/java/com/chronodawn/registry/Mod*.java \
        common/1.21.5/src/main/java/com/chronodawn/registry/Mod*.java \
        common/1.21.6/src/main/java/com/chronodawn/registry/Mod*.java \
        common/1.21.7/src/main/java/com/chronodawn/registry/Mod*.java \
        common/1.21.8/src/main/java/com/chronodawn/registry/Mod*.java \
        common/1.21.9/src/main/java/com/chronodawn/registry/Mod*.java \
        common/1.21.10/src/main/java/com/chronodawn/registry/Mod*.java
git commit -m "feat(1.21.2-1.21.10): register temporal sand/gravel/sandstone blocks"
```

---

## Task 10: ModBlocks/ModItems registration for Era D (1.21.11)

**Files:** `common/1.21.11/src/main/java/com/chronodawn/registry/Mod{Blocks,Items}.java`.

- [ ] **Step 1: Apply Era C registrations with `Identifier` import swap**

Same as Task 9 Steps 1–3 but in the ModItems entries, replace:

- `import net.minecraft.resources.ResourceLocation;` → `import net.minecraft.resources.Identifier;`
- `ResourceLocation.fromNamespaceAndPath(` → `Identifier.fromNamespaceAndPath(`

(memory: "Util package moved in 1.21.11" — same pattern as existing `TEMPORAL_STONE_SLAB` entry.)

- [ ] **Step 2: Build 1.21.11**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/Temporal*.java \
        common/1.21.11/src/main/java/com/chronodawn/registry/Mod*.java
git commit -m "feat(1.21.11): register temporal sand/gravel/sandstone blocks"
```

---

## Task 11: Blockstates and block models (shared)

**Files (all in `common/shared/src/main/resources/assets/chronodawn/`):**
- Create: `blockstates/temporal_sand.json`
- Create: `blockstates/temporal_gravel.json`
- Create: `blockstates/temporal_sandstone.json`
- Create: `blockstates/temporal_sandstone_stairs.json`
- Create: `blockstates/temporal_sandstone_slab.json`
- Create: `blockstates/temporal_sandstone_wall.json`
- Create: `models/block/temporal_sand.json`
- Create: `models/block/temporal_gravel.json`
- Create: `models/block/temporal_sandstone.json`
- Create: `models/block/temporal_sandstone_stairs.json`
- Create: `models/block/temporal_sandstone_inner_stairs.json`
- Create: `models/block/temporal_sandstone_outer_stairs.json`
- Create: `models/block/temporal_sandstone_slab.json`
- Create: `models/block/temporal_sandstone_slab_top.json`
- Create: `models/block/temporal_sandstone_wall_post.json`
- Create: `models/block/temporal_sandstone_wall_side.json`
- Create: `models/block/temporal_sandstone_wall_side_tall.json`

- [ ] **Step 1: Simple cube blockstates (sand, gravel)**

`blockstates/temporal_sand.json`:

```json
{
  "variants": {
    "": { "model": "chronodawn:block/temporal_sand" }
  }
}
```

Same shape for `temporal_gravel.json` (swap the model path).

- [ ] **Step 2: Sandstone blockstate (cube_bottom_top)**

`blockstates/temporal_sandstone.json`:

```json
{
  "variants": {
    "": { "model": "chronodawn:block/temporal_sandstone" }
  }
}
```

- [ ] **Step 3: Sandstone stairs/slab/wall blockstates**

Copy the structure (and rename references) from existing `temporal_stone_stairs.json`, `temporal_stone_slab.json`, `temporal_stone_wall.json` in the same directory. Replace every `temporal_stone` → `temporal_sandstone`. The full multi-variant JSON is large — copying is the only safe approach (do not hand-write 200+ lines).

- [ ] **Step 4: Block model — temporal_sand**

`models/block/temporal_sand.json`:

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "chronodawn:block/temporal_sand" }
}
```

Same shape for `models/block/temporal_gravel.json`.

- [ ] **Step 5: Block model — temporal_sandstone (cube_bottom_top)**

`models/block/temporal_sandstone.json`:

```json
{
  "parent": "minecraft:block/cube_bottom_top",
  "textures": {
    "bottom": "chronodawn:block/temporal_sandstone_bottom",
    "top": "chronodawn:block/temporal_sandstone_top",
    "side": "chronodawn:block/temporal_sandstone"
  }
}
```

- [ ] **Step 6: Sandstone stairs models (3 files)**

`models/block/temporal_sandstone_stairs.json`:

```json
{
  "parent": "minecraft:block/stairs",
  "textures": {
    "bottom": "chronodawn:block/temporal_sandstone_bottom",
    "top": "chronodawn:block/temporal_sandstone_top",
    "side": "chronodawn:block/temporal_sandstone"
  }
}
```

`temporal_sandstone_inner_stairs.json` and `temporal_sandstone_outer_stairs.json` — same body, change `parent` to `minecraft:block/inner_stairs` and `minecraft:block/outer_stairs` respectively.

- [ ] **Step 7: Sandstone slab models (2 files)**

`models/block/temporal_sandstone_slab.json`:

```json
{
  "parent": "minecraft:block/slab",
  "textures": {
    "bottom": "chronodawn:block/temporal_sandstone_bottom",
    "top": "chronodawn:block/temporal_sandstone_top",
    "side": "chronodawn:block/temporal_sandstone"
  }
}
```

`temporal_sandstone_slab_top.json` — same body, change `parent` to `minecraft:block/slab_top`.

- [ ] **Step 8: Sandstone wall models (3 files)**

`models/block/temporal_sandstone_wall_post.json`:

```json
{
  "parent": "minecraft:block/template_wall_post",
  "textures": { "wall": "chronodawn:block/temporal_sandstone" }
}
```

`temporal_sandstone_wall_side.json` — same body, parent `minecraft:block/template_wall_side`.
`temporal_sandstone_wall_side_tall.json` — same body, parent `minecraft:block/template_wall_side_tall`.

- [ ] **Step 9: Validate JSON**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL with no JSON / cross-reference errors.

- [ ] **Step 10: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_*.json \
        common/shared/src/main/resources/assets/chronodawn/models/block/temporal_sand.json \
        common/shared/src/main/resources/assets/chronodawn/models/block/temporal_gravel.json \
        common/shared/src/main/resources/assets/chronodawn/models/block/temporal_sandstone*.json
git commit -m "feat: add blockstates and block models for temporal sand/gravel/sandstone"
```

---

## Task 12: Item models + Client Items JSON (1.21.4+)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_sand.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_gravel.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_sandstone.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_sandstone_stairs.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_sandstone_slab.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_sandstone_wall.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_*.json` (6 files)
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_*.json` (6 files)

- [ ] **Step 1: Item models for sand/gravel/sandstone/stairs/slab**

Each is a one-line parent reference. Example `models/item/temporal_sand.json`:

```json
{ "parent": "chronodawn:block/temporal_sand" }
```

Same for `temporal_gravel`, `temporal_sandstone`, `temporal_sandstone_stairs`, `temporal_sandstone_slab` (5 files).

- [ ] **Step 2: Item model for sandstone wall (uses wall_inventory)**

`models/item/temporal_sandstone_wall.json`:

```json
{
  "parent": "minecraft:block/wall_inventory",
  "textures": { "wall": "chronodawn:block/temporal_sandstone" }
}
```

- [ ] **Step 3: 1.21.4 Client Items JSON (6 files)**

Each file in `common/1.21.4/src/main/resources/assets/chronodawn/items/<id>.json`:

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/<id>"}}
```

Replace `<id>` with each of: `temporal_sand`, `temporal_gravel`, `temporal_sandstone`, `temporal_sandstone_stairs`, `temporal_sandstone_slab`, `temporal_sandstone_wall`.

- [ ] **Step 4: shared-1.21.5+ Client Items JSON (6 files)**

Same content as Step 3, in `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/`.

- [ ] **Step 5: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/item/temporal_*.json \
        common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_*.json \
        common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_*.json
git commit -m "feat: add item models and client items JSON for temporal sand/gravel/sandstone"
```

---

## Task 13: Loot tables for 1.20.1 (plural `loot_tables/`)

**Files (all in `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/`):**
- Create: `temporal_sand.json`
- Create: `temporal_gravel.json`
- Create: `temporal_sandstone.json`
- Create: `temporal_sandstone_stairs.json`
- Create: `temporal_sandstone_slab.json`
- Create: `temporal_sandstone_wall.json`

- [ ] **Step 1: temporal_sand.json (drop-self)**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        { "type": "minecraft:item", "name": "chronodawn:temporal_sand" }
      ],
      "conditions": [{ "condition": "minecraft:survives_explosion" }]
    }
  ]
}
```

- [ ] **Step 2: temporal_sandstone / stairs / wall (drop-self)**

Same shape as Step 1, swap `name`. 3 files.

- [ ] **Step 3: temporal_sandstone_slab.json (double=2 drops)**

Copy `common/1.20.1/.../loot_tables/blocks/temporal_stone_slab.json` and rename every `temporal_stone_slab` → `temporal_sandstone_slab`.

- [ ] **Step 4: temporal_gravel.json (flint drop, 1.20.1 silk-touch format)**

memory: "Loot table silk touch predicate format (version-split)" — 1.20.1 uses the DIRECT `enchantments` form.

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
              "name": "chronodawn:temporal_gravel",
              "conditions": [
                { "condition": "minecraft:any_of",
                  "terms": [
                    {
                      "condition": "minecraft:match_tool",
                      "predicate": {
                        "enchantments": [
                          { "enchantment": "minecraft:silk_touch", "levels": { "min": 1 } }
                        ]
                      }
                    }
                  ]
                }
              ]
            },
            {
              "type": "minecraft:alternatives",
              "children": [
                {
                  "type": "minecraft:item",
                  "name": "minecraft:flint",
                  "conditions": [
                    {
                      "condition": "minecraft:table_bonus",
                      "enchantment": "minecraft:fortune",
                      "chances": [0.1, 0.14285715, 0.25, 1.0]
                    }
                  ]
                },
                {
                  "type": "minecraft:item",
                  "name": "chronodawn:temporal_gravel"
                }
              ]
            }
          ]
        }
      ],
      "conditions": [{ "condition": "minecraft:survives_explosion" }]
    }
  ]
}
```

- [ ] **Step 5: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_*.json
git commit -m "feat(1.20.1): add loot tables for temporal sand/gravel/sandstone"
```

---

## Task 14: Loot tables for 1.21.1+ (singular `loot_table/`)

**Files (all in `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/`):**
- Create: `temporal_sand.json`
- Create: `temporal_gravel.json`
- Create: `temporal_sandstone.json`
- Create: `temporal_sandstone_stairs.json`
- Create: `temporal_sandstone_slab.json`
- Create: `temporal_sandstone_wall.json`

- [ ] **Step 1: drop-self loot tables**

`temporal_sand.json`, `temporal_sandstone.json`, `temporal_sandstone_stairs.json`, `temporal_sandstone_wall.json` — same shape as Task 13 Step 1.

- [ ] **Step 2: temporal_sandstone_slab.json**

Copy `common/shared-1.21.1+/.../loot_table/blocks/temporal_stone_slab.json` and rename every `temporal_stone_slab` → `temporal_sandstone_slab`.

- [ ] **Step 3: temporal_gravel.json (flint drop, NEW nested predicate format)**

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
              "name": "chronodawn:temporal_gravel",
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
              ]
            },
            {
              "type": "minecraft:alternatives",
              "children": [
                {
                  "type": "minecraft:item",
                  "name": "minecraft:flint",
                  "conditions": [
                    {
                      "condition": "minecraft:table_bonus",
                      "enchantment": "minecraft:fortune",
                      "chances": [0.1, 0.14285715, 0.25, 1.0]
                    }
                  ]
                },
                {
                  "type": "minecraft:item",
                  "name": "chronodawn:temporal_gravel"
                }
              ]
            }
          ]
        }
      ],
      "conditions": [{ "condition": "minecraft:survives_explosion" }]
    }
  ]
}
```

- [ ] **Step 4: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_*.json
git commit -m "feat(1.21.1+): add loot tables for temporal sand/gravel/sandstone"
```

---

## Task 15: Mining tags (`mineable/shovel`, `mineable/pickaxe`)

**Files:**
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/shovel.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/shovel.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`

- [ ] **Step 1: Append to shovel tags**

Add to the `values` array in both shovel.json files:

```json
"chronodawn:temporal_sand",
"chronodawn:temporal_gravel"
```

- [ ] **Step 2: Append to pickaxe tags**

Add to the `values` array in both pickaxe.json files:

```json
"chronodawn:temporal_sandstone",
"chronodawn:temporal_sandstone_stairs",
"chronodawn:temporal_sandstone_slab",
"chronodawn:temporal_sandstone_wall"
```

- [ ] **Step 3: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/*.json \
        common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/*.json
git commit -m "feat: tag temporal sand/gravel/sandstone for proper tools"
```

---

## Task 16: Vanilla compat tags (`#minecraft:sand`, `#minecraft:gravel`, `#minecraft:sandstone_blocks`, `#minecraft:walls`)

**Files (4 new tag files for 1.20.1, 4 for shared-1.21.1+):**
- Create: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/sand.json`
- Create: `common/1.20.1/src/main/resources/data/minecraft/tags/items/sand.json`
- Create: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/gravel.json`
- Create: `common/1.20.1/src/main/resources/data/minecraft/tags/items/gravel.json`
- Create: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/sandstone_blocks.json`
- Create: `common/1.20.1/src/main/resources/data/minecraft/tags/items/sandstone_blocks.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/walls.json` (append)
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/items/walls.json` (append)
- Mirror all of the above under `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/{block,item}/`

- [ ] **Step 1: Sand tag**

`tags/{blocks|block}/sand.json` (and matching items/item):

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sand"
  ]
}
```

- [ ] **Step 2: Gravel tag**

Same shape as Step 1, value `chronodawn:temporal_gravel`.

- [ ] **Step 3: Sandstone_blocks tag**

Same shape, value `chronodawn:temporal_sandstone`.

- [ ] **Step 4: Walls — append to existing tag**

Edit existing `walls.json` files in both directories. Add `"chronodawn:temporal_sandstone_wall"` to the `values` array (preserving the existing `chronodawn:*_wall` entries already there).

- [ ] **Step 5: Validate**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add common/1.20.1/src/main/resources/data/minecraft/tags/blocks/{sand,gravel,sandstone_blocks,walls}.json \
        common/1.20.1/src/main/resources/data/minecraft/tags/items/{sand,gravel,sandstone_blocks,walls}.json \
        common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/{sand,gravel,sandstone_blocks,walls}.json \
        common/shared-1.21.1+/src/main/resources/data/minecraft/tags/item/{sand,gravel,sandstone_blocks,walls}.json
git commit -m "feat: register temporal sand/gravel/sandstone in vanilla compat tags"
```

---

## Task 17: Recipes for 1.20.1 (Era A format)

**Files (all in `common/1.20.1/src/main/resources/data/chronodawn/recipes/`):**
- Create: `temporal_sandstone.json`
- Create: `temporal_sandstone_stairs.json`
- Create: `temporal_sandstone_slab.json`
- Create: `temporal_sandstone_wall.json`
- Create: `temporal_sandstone_stairs_stonecutting.json`
- Create: `temporal_sandstone_slab_stonecutting.json`
- Create: `temporal_sandstone_wall_stonecutting.json`

- [ ] **Step 1: temporal_sandstone (4 sand → 1 sandstone)**

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building_blocks",
  "pattern": [
    "SS",
    "SS"
  ],
  "key": {
    "S": { "item": "chronodawn:temporal_sand" }
  },
  "result": {
    "item": "chronodawn:temporal_sandstone",
    "count": 1
  }
}
```

- [ ] **Step 2: temporal_sandstone_stairs (6 sandstone → 4 stairs)**

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building_blocks",
  "pattern": [
    "B  ",
    "BB ",
    "BBB"
  ],
  "key": {
    "B": { "item": "chronodawn:temporal_sandstone" }
  },
  "result": {
    "item": "chronodawn:temporal_sandstone_stairs",
    "count": 4
  }
}
```

- [ ] **Step 3: temporal_sandstone_slab (3 sandstone → 6 slab)**

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building_blocks",
  "pattern": [
    "BBB"
  ],
  "key": {
    "B": { "item": "chronodawn:temporal_sandstone" }
  },
  "result": {
    "item": "chronodawn:temporal_sandstone_slab",
    "count": 6
  }
}
```

- [ ] **Step 4: temporal_sandstone_wall (6 sandstone → 6 wall)**

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building_blocks",
  "pattern": [
    "BBB",
    "BBB"
  ],
  "key": {
    "B": { "item": "chronodawn:temporal_sandstone" }
  },
  "result": {
    "item": "chronodawn:temporal_sandstone_wall",
    "count": 6
  }
}
```

- [ ] **Step 5: Stonecutting variants (3 files)**

`temporal_sandstone_stairs_stonecutting.json`:

```json
{
  "type": "minecraft:stonecutting",
  "ingredient": { "item": "chronodawn:temporal_sandstone" },
  "result": "chronodawn:temporal_sandstone_stairs",
  "count": 1
}
```

`temporal_sandstone_slab_stonecutting.json` — same body, change `result` to slab and `count` to 2.
`temporal_sandstone_wall_stonecutting.json` — same body, change `result` to wall and `count` to 1.

- [ ] **Step 6: Validate and build**

```
./gradlew validateResources
./gradlew build1_20_1
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/recipes/temporal_sandstone*.json
git commit -m "feat(1.20.1): add temporal sandstone crafting and stonecutting recipes"
```

---

## Task 18: Recipes for 1.21.1 (Era B format)

**Files:** Same 7 filenames as Task 17 in `common/1.21.1/src/main/resources/data/chronodawn/recipe/` (singular).

- [ ] **Step 1: Crafting recipes**

Identical body to Task 17 Steps 1–4, but change `"result": { "item": ..., "count": ... }` to `"result": { "id": ..., "count": ... }` (Era B switched the result key from `item` to `id` while keeping `key.X` as an object).

- [ ] **Step 2: Stonecutting recipes**

Identical body to Task 17 Step 5. Stonecutting kept the same shape across Eras A and B.

- [ ] **Step 3: Validate and build**

```
./gradlew validateResources
./gradlew build1_21_1
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/1.21.1/src/main/resources/data/chronodawn/recipe/temporal_sandstone*.json
git commit -m "feat(1.21.1): add temporal sandstone crafting and stonecutting recipes"
```

---

## Task 19: Recipes for 1.21.2+ (Era C format, shared)

**Files:** Same 7 filenames in `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/`.

- [ ] **Step 1: Crafting recipes (Era C: ingredient is a string)**

Example `temporal_sandstone.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building_blocks",
  "pattern": [
    "SS",
    "SS"
  ],
  "key": {
    "S": "chronodawn:temporal_sand"
  },
  "result": {
    "id": "chronodawn:temporal_sandstone",
    "count": 1
  }
}
```

Apply the same change (`"key": { "X": "<id>" }` instead of `"key": { "X": { "item": "<id>" } }`) to the other 3 crafting recipes.

- [ ] **Step 2: Stonecutting recipes (Era C: ingredient is a string)**

```json
{
  "type": "minecraft:stonecutting",
  "ingredient": "chronodawn:temporal_sandstone",
  "result": {
    "id": "chronodawn:temporal_sandstone_stairs",
    "count": 1
  }
}
```

(Note: Era C also moved stonecutting `result` into an object with `id`/`count` keys instead of the bare-string + `count` form. Verify against an existing `common/shared-1.21.2+/.../recipe/temporal_*` stonecutting JSON before writing.)

- [ ] **Step 3: Validate and build (latest version)**

```
./gradlew validateResources
./gradlew build1_21_11
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/temporal_sandstone*.json
git commit -m "feat(1.21.2+): add temporal sandstone crafting and stonecutting recipes"
```

---

## Task 20: Translations (en_us / ja_jp × 3 lang locations)

**Files (6 files modified):**
- Modify: `common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/1.20.1/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- Modify: `common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/1.21.1/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- Modify: `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/ja_jp.json`

- [ ] **Step 1: Add 6 entries to en_us.json files (line-based Edit, NOT json.dumps)**

memory: "Lang JSON line-based edit" — use the Edit tool with anchors. Insert near the existing `block.chronodawn.temporal_stone*` block, preserving blank-line groupings:

```json
  "block.chronodawn.temporal_sand": "Temporal Sand",
  "block.chronodawn.temporal_gravel": "Temporal Gravel",
  "block.chronodawn.temporal_sandstone": "Temporal Sandstone",
  "block.chronodawn.temporal_sandstone_stairs": "Temporal Sandstone Stairs",
  "block.chronodawn.temporal_sandstone_slab": "Temporal Sandstone Slab",
  "block.chronodawn.temporal_sandstone_wall": "Temporal Sandstone Wall",
```

- [ ] **Step 2: Add 6 entries to ja_jp.json files**

```json
  "block.chronodawn.temporal_sand": "テンポラルサンド",
  "block.chronodawn.temporal_gravel": "テンポラルグラベル",
  "block.chronodawn.temporal_sandstone": "テンポラル砂岩",
  "block.chronodawn.temporal_sandstone_stairs": "テンポラル砂岩の階段",
  "block.chronodawn.temporal_sandstone_slab": "テンポラル砂岩のハーフブロック",
  "block.chronodawn.temporal_sandstone_wall": "テンポラル砂岩の塀",
```

- [ ] **Step 3: Validate**

```
./gradlew validateTranslations
```

Expected: BUILD SUCCESSFUL — all 6 keys present in every (version × locale) pair.

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: add en_us/ja_jp translations for temporal sand/gravel/sandstone"
```

---

## Task 21: Update disk_features (sand_disk, gravel_disk)

**Files:**
- Modify: `common/1.20.1/src/main/resources/data/chronodawn/worldgen/configured_feature/sand_disk.json`
- Modify: `common/1.20.1/src/main/resources/data/chronodawn/worldgen/configured_feature/gravel_disk.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/sand_disk.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/gravel_disk.json`

- [ ] **Step 1: Update sand_disk.json (both versions)**

In each file, change `state_provider.fallback.state.Name` from `minecraft:sand` to `chronodawn:temporal_sand`. In the `target.blocks` array, change `minecraft:gravel` → `chronodawn:temporal_gravel`. Leave `temporal_grass_block` and `temporal_dirt` untouched.

- [ ] **Step 2: Update gravel_disk.json (both versions)**

Change `state_provider.fallback.state.Name` from `minecraft:gravel` to `chronodawn:temporal_gravel`. The `target.blocks` array has no `minecraft:` references — leave as-is.

- [ ] **Step 3: Validate**

```
./gradlew validateResources
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/worldgen/configured_feature/{sand_disk,gravel_disk}.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/worldgen/configured_feature/{sand_disk,gravel_disk}.json
git commit -m "feat: redirect chronodawn sand/gravel disk features to temporal blocks"
```

---

## Task 22: Replace surface_rule block IDs in noise_settings (× 11)

**Files:**
- Modify: `common/<ver>/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json` for each ver in {1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}

- [ ] **Step 1: Confirm baseline occurrence count**

```
./gradlew validateResources
```

Then for each version, count occurrences (should be 24 in every version):

```bash
grep -c '"Name": "minecraft:\(sand\|red_sand\|gravel\|sandstone\|red_sandstone\)"' \
   common/1.21.11/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json
```

Expected: `24`. Repeat (or skip) for spot-check on 1.20.1, 1.21.1, 1.21.5.

- [ ] **Step 2: Apply 5 replacements per file**

For each of the 11 files, perform these exact string replacements (use Edit's `replace_all`):

| Find | Replace |
|---|---|
| `"Name": "minecraft:sand"` | `"Name": "chronodawn:temporal_sand"` |
| `"Name": "minecraft:red_sand"` | `"Name": "chronodawn:temporal_sand"` |
| `"Name": "minecraft:gravel"` | `"Name": "chronodawn:temporal_gravel"` |
| `"Name": "minecraft:sandstone"` | `"Name": "chronodawn:temporal_sandstone"` |
| `"Name": "minecraft:red_sandstone"` | `"Name": "chronodawn:temporal_sandstone"` |

**Critical:** Match `"Name": "minecraft:..."` exactly. The string `"noise": "minecraft:gravel"` (noise provider name, NOT a block ID) must NOT be replaced. Use the `"Name":` prefix to scope the replacement.

- [ ] **Step 3: Verify no `minecraft:sand|red_sand|gravel|sandstone|red_sandstone` remains as a `"Name"` value**

```bash
grep '"Name": "minecraft:\(sand\|red_sand\|gravel\|sandstone\|red_sandstone\)"' \
   common/*/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json
```

Expected: no matches.

Also verify `"noise": "minecraft:gravel"` was preserved:

```bash
grep -c '"noise": "minecraft:gravel"' \
   common/1.21.11/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json
```

Expected: `2` (matches baseline).

- [ ] **Step 4: Validate JSON and build the latest version**

```
./gradlew validateResources
./gradlew build1_21_11
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/resources/data/chronodawn/worldgen/noise_settings/chronodawn.json
git commit -m "feat: replace vanilla sand/gravel/sandstone with temporal blocks in chronodawn surface rules"
```

---

## Task 23: Full multi-version build + tests

- [ ] **Step 1: cleanAll**

```
./gradlew cleanAll
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: validateResources + validateTranslations**

```
./gradlew validateResources
./gradlew validateTranslations
```

Expected: both BUILD SUCCESSFUL

- [ ] **Step 3: buildAll**

```
./gradlew buildAll
```

Expected: BUILD SUCCESSFUL across all 11 versions. If a version fails, fix the source error there (do not mass-revert).

- [ ] **Step 4: testAll (includes CreativeTabCompletenessTest)**

```
./gradlew testAll
```

Expected: BUILD SUCCESSFUL. If `CreativeTabCompletenessTest` fails, recheck Task 7/8/9/10 Step 3 entries.

- [ ] **Step 5: gameTestAll**

```
./gradlew gameTestAll
```

Expected: BUILD SUCCESSFUL across all version-pair groups.

- [ ] **Step 6: If anything fails, fix and re-run, then commit any fixups**

memory: "Run checkAll before marking multi-version phases complete" — do not declare done until checkAll passes.

```
./gradlew checkAll
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit any fixups**

If Steps 1–6 produced fixups, commit them with `fix:` prefix and a focused message.

---

## Task 24: Manual client verification (1.21.11 Fabric)

- [ ] **Step 1: Launch client**

```
./gradlew runClientFabric1_21_11
```

- [ ] **Step 2: Creative-tab visual check**

Open the ChronoDawn creative tab. Verify the 6 new blocks (`temporal_sand`, `temporal_gravel`, `temporal_sandstone`, `temporal_sandstone_stairs`, `temporal_sandstone_slab`, `temporal_sandstone_wall`) appear next to the temporal stone group with correct pale-blue textures and English names.

- [ ] **Step 3: Worldgen visual check**

Use a portal to enter the Chrono Dawn dimension on a freshly generated chunk (not a previously loaded one). Verify:
- Beach areas / dimension surface show pale-blue temporal sand instead of vanilla yellow sand
- Sand/gravel disk features show temporal blocks
- Underground sandstone (where surface rules expose it) shows temporal sandstone

- [ ] **Step 4: Crafting check**

Place a crafting table. Verify Temporal Sand 4個 (2x2) → 1 Temporal Sandstone. Verify Temporal Sandstone → stairs/slab/wall recipes work.

- [ ] **Step 5: Tag compat check**

Place TNT and try crafting it with `chronodawn:temporal_sand` (4 sand + 5 gunpowder pattern). Confirm the recipe resolves (this verifies the `#minecraft:sand` tag entry).

- [ ] **Step 6: Flint drop check**

Mine `chronodawn:temporal_gravel` repeatedly with a vanilla shovel. Confirm flint drops occur at roughly the same rate as vanilla gravel (~10% base, higher with Fortune).

- [ ] **Step 7: Falling block check**

Place `chronodawn:temporal_sand` mid-air; confirm it falls. Same for `temporal_gravel`.

- [ ] **Step 8: Switch loader and version**

Repeat selected checks (creative tab + crafting + worldgen) on:
```
./gradlew runClientNeoForge1_21_11
./gradlew runClientFabric1_20_1
```

Expected: identical behavior across loaders and across the version extremes.

- [ ] **Step 9: Note any defects**

If any defect is found, file a fix task and continue debugging. Do NOT skip — incomplete client verification is a Phase 6 silent-break risk (see memory: "Run checkAll before marking multi-version phases complete").

---

## Task 25: Documentation refresh (optional unless externally observable)

Per CLAUDE.md "Check if README/docs need updating after implementation. Only update if external behavior or usage changes."

- [ ] **Step 1: Decide whether docs need updating**

Player-visible change summary:
- New blocks visible in ChronoDawn dimension
- 6 new entries in creative tab
- New crafting recipes (Temporal Sand → Temporal Sandstone, etc.)

Update if user expectations / playthrough docs reference vanilla sand presence in the dimension (`docs/player_guide.md`, possibly `docs/curseforge_description.md` / `modrinth_description.md`).

- [ ] **Step 2: If updating, edit the affected sections only**

Make minimal targeted edits — do not restructure docs.

- [ ] **Step 3: Commit (if applicable)**

```bash
git commit -m "docs: note temporal sand/gravel/sandstone in player guide"
```

If no docs need updating, skip the commit.

---

## Self-Review Notes

This plan was reviewed against the spec on 2026-04-18:

- **Spec coverage:** All 5 spec sections (Blocks, Properties, Textures, Worldgen, Tags, Recipes, Loot, Translations, Verification) have at least one corresponding task.
- **Placeholder scan:** No TBD/TODO; every code block contains complete, ready-to-paste content.
- **Type consistency:** All 6 IDs spelled identically across tasks (`temporal_sand`, `temporal_gravel`, `temporal_sandstone`, `temporal_sandstone_stairs`, `temporal_sandstone_slab`, `temporal_sandstone_wall`). Method name `createProperties()` consistent. `BlockBehaviour.Properties` used uniformly.
- **Era split:** A/B/C/D matches the existing `TemporalDirtBlock`/`TemporalStoneStairs` precedent in the repo.
- **Memory citations:** 7 memory entries cited in-context where relevant.
