# Custom Dirt Block Set Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add temporal_dirt, temporal_grass_block, and coarse_temporal_dirt as custom terrain blocks for the ChronoDawn dimension, replacing vanilla dirt/grass in surface rules.

**Architecture:** Three new blocks extending vanilla equivalents. TemporalGrassBlock extends SpreadingSnowyDirtBlock with custom spread logic targeting temporal_dirt. Biome tinting via BlockColors registration. Surface rules updated in all 11 version-specific overworld.json files.

**Tech Stack:** Java 21, Architectury API, Fabric/NeoForge, Mojang mappings

---

## File Structure

### Shared (common/shared/)
- `src/main/java/com/chronodawn/registry/ModBlockId.java` — add 3 enum entries
- `src/main/java/com/chronodawn/registry/ModItemId.java` — add 3 enum entries
- `src/main/resources/assets/chronodawn/blockstates/temporal_dirt.json` — new
- `src/main/resources/assets/chronodawn/blockstates/temporal_grass_block.json` — new
- `src/main/resources/assets/chronodawn/blockstates/coarse_temporal_dirt.json` — new
- `src/main/resources/assets/chronodawn/models/block/temporal_dirt.json` — new
- `src/main/resources/assets/chronodawn/models/block/temporal_grass_block.json` — new
- `src/main/resources/assets/chronodawn/models/block/temporal_grass_block_snow.json` — new
- `src/main/resources/assets/chronodawn/models/block/coarse_temporal_dirt.json` — new
- `src/main/resources/assets/chronodawn/models/item/temporal_dirt.json` — new
- `src/main/resources/assets/chronodawn/models/item/temporal_grass_block.json` — new
- `src/main/resources/assets/chronodawn/models/item/coarse_temporal_dirt.json` — new
- `src/main/resources/data/chronodawn/worldgen/configured_feature/patch_coarse_dirt.json` — modify
- `src/main/resources/data/chronodawn/worldgen/placed_feature/grass_block.json` — modify
- `src/main/resources/data/chronodawn/worldgen/placed_feature/tall_grass_block.json` — modify

### Shared (common/shared-1.21.2+/)
- `src/main/resources/assets/chronodawn/lang/en_us.json` — add 3 translation keys

### Shared (common/shared-1.21.5+/)
- `src/main/resources/assets/chronodawn/items/temporal_dirt.json` — new
- `src/main/resources/assets/chronodawn/items/temporal_grass_block.json` — new (with grass tint)
- `src/main/resources/assets/chronodawn/items/coarse_temporal_dirt.json` — new

### Version-specific lang
- `common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json` — add 3 keys
- `common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json` — add 3 keys

### Version-specific items (1.21.4 only, since 1.21.5+ uses shared)
- `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_dirt.json` — new
- `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_grass_block.json` — new
- `common/1.21.4/src/main/resources/assets/chronodawn/items/coarse_temporal_dirt.json` — new

### Per-version Java (11 versions: 1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11)
- `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalDirtBlock.java` — new
- `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalGrassBlock.java` — new
- `common/<ver>/src/main/java/com/chronodawn/blocks/CoarseTemporalDirtBlock.java` — new
- `common/<ver>/src/main/java/com/chronodawn/registry/ModBlocks.java` — add 3 registrations
- `common/<ver>/src/main/java/com/chronodawn/registry/ModItems.java` — add 3 registrations + creative tab

### Per-version surface rules (11 versions)
- `common/<ver>/src/main/resources/data/minecraft/worldgen/noise_settings/overworld.json` — modify

### Per-version loot tables
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_dirt.json` — new
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_grass_block.json` — new
- `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/coarse_temporal_dirt.json` — new
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_dirt.json` — new (plural dir)
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_grass_block.json` — new
- `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/coarse_temporal_dirt.json` — new

### Block tags
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/shovel.json` — new
- `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/hoe.json` — modify (add coarse_temporal_dirt)

### Fabric client (11 versions)
- `fabric/<ver>/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java` — add BlockColors

### NeoForge client
- `neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java` — add BlockColors + ItemColors
- Version-specific NeoForge client files if they override base

---

### Task 1: Register Block and Item IDs (shared enums)

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Add block IDs to ModBlockId enum**

Add after existing terrain-related entries (e.g., after `TEMPORAL_MOSS`):

```java
TEMPORAL_DIRT(def("temporal_dirt")),
TEMPORAL_GRASS_BLOCK(def("temporal_grass_block").cutoutMipped()),
COARSE_TEMPORAL_DIRT(def("coarse_temporal_dirt")),
```

`temporal_grass_block` needs `cutoutMipped()` because the side overlay texture uses alpha transparency.

- [ ] **Step 2: Add item IDs to ModItemId enum**

Add in the same relative position:

```java
TEMPORAL_DIRT("temporal_dirt"),
TEMPORAL_GRASS_BLOCK("temporal_grass_block"),
COARSE_TEMPORAL_DIRT("coarse_temporal_dirt"),
```

- [ ] **Step 3: Build to verify enum compilation**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat: add block and item IDs for temporal dirt block set"
```

---

### Task 2: Create block classes (1.21.11 first, then propagate)

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalDirtBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalGrassBlock.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/CoarseTemporalDirtBlock.java`

- [ ] **Step 1: Create TemporalDirtBlock**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TemporalDirtBlock extends Block {
    public static final MapCodec<TemporalDirtBlock> CODEC = simpleCodec(TemporalDirtBlock::new);

    public TemporalDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_dirt")));
    }
}
```

- [ ] **Step 2: Create TemporalGrassBlock**

This block extends `SpreadingSnowyDirtBlock` and overrides `randomTick` to spread onto `temporal_dirt` instead of vanilla `dirt`. It also handles hoe → farmland conversion.

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

public class TemporalGrassBlock extends SpreadingSnowyDirtBlock {
    public static final MapCodec<TemporalGrassBlock> CODEC = simpleCodec(TemporalGrassBlock::new);

    public TemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends TemporalGrassBlock> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_grass_block")));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState());
            return;
        }

        if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            BlockState grassState = this.defaultBlockState();
            for (int i = 0; i < 4; ++i) {
                BlockPos targetPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (level.getBlockState(targetPos).is(ModBlocks.TEMPORAL_DIRT.get()) && canPropagate(grassState, level, targetPos)) {
                    level.setBlockAndUpdate(targetPos, grassState.setValue(SNOWY,
                            level.getBlockState(targetPos.above()).is(Blocks.SNOW)));
                }
            }
        }
    }

    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        }
        int lightBlocked = LightEngine.getLightBlockInto(level, state, pos, aboveState, above,
                net.minecraft.core.Direction.UP, aboveState.getLightBlock());
        return lightBlocked < level.getMaxLightLevel();
    }

    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return canBeGrass(state, level, pos) && !level.getFluidState(above).isSource();
    }
}
```

- [ ] **Step 3: Create CoarseTemporalDirtBlock**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CoarseTemporalDirtBlock extends Block {
    public static final MapCodec<CoarseTemporalDirtBlock> CODEC = simpleCodec(CoarseTemporalDirtBlock::new);

    public CoarseTemporalDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "coarse_temporal_dirt")));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand,
                                          BlockHitResult hitResult) {
        if (stack.getItem() instanceof HoeItem) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
```

- [ ] **Step 4: Add hoe tillable behavior to TemporalDirtBlock**

Add `useItemOn` override to `TemporalDirtBlock` for hoe → farmland conversion:

```java
@Override
protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                      Player player, net.minecraft.world.InteractionHand hand,
                                      BlockHitResult hitResult) {
    if (stack.getItem() instanceof HoeItem) {
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        }
        return InteractionResult.SUCCESS;
    }
    return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
}
```

Add the necessary imports to TemporalDirtBlock:

```java
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
```

- [ ] **Step 5: Build to verify compilation**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalDirtBlock.java
git add common/1.21.11/src/main/java/com/chronodawn/blocks/TemporalGrassBlock.java
git add common/1.21.11/src/main/java/com/chronodawn/blocks/CoarseTemporalDirtBlock.java
git commit -m "feat: add block classes for temporal dirt set (1.21.11)"
```

---

### Task 3: Register blocks and items (1.21.11)

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Register blocks in ModBlocks.java**

Add after the `TEMPORAL_MOSS` registration:

```java
/**
 * Temporal Dirt - Custom dirt block for the ChronoDawn dimension.
 * Equivalent to vanilla dirt. Hoe converts to farmland.
 */
public static final RegistrySupplier<Block> TEMPORAL_DIRT = BLOCKS.register(
    ModBlockId.TEMPORAL_DIRT.id(),
    () -> new TemporalDirtBlock(TemporalDirtBlock.createProperties())
);

/**
 * Temporal Grass Block - Custom grass block for the ChronoDawn dimension.
 * Spreads to adjacent temporal dirt, reverts when light blocked.
 * Biome-tinted top and side overlay.
 */
public static final RegistrySupplier<Block> TEMPORAL_GRASS_BLOCK = BLOCKS.register(
    ModBlockId.TEMPORAL_GRASS_BLOCK.id(),
    () -> new TemporalGrassBlock(TemporalGrassBlock.createProperties())
);

/**
 * Coarse Temporal Dirt - Coarse variant that grass cannot spread onto.
 * Hoe converts to temporal dirt.
 */
public static final RegistrySupplier<Block> COARSE_TEMPORAL_DIRT = BLOCKS.register(
    ModBlockId.COARSE_TEMPORAL_DIRT.id(),
    () -> new CoarseTemporalDirtBlock(CoarseTemporalDirtBlock.createProperties())
);
```

Add imports at the top:

```java
import com.chronodawn.blocks.TemporalDirtBlock;
import com.chronodawn.blocks.TemporalGrassBlock;
import com.chronodawn.blocks.CoarseTemporalDirtBlock;
```

- [ ] **Step 2: Register items in ModItems.java**

Add after the `TEMPORAL_MOSS` item registration:

```java
public static final RegistrySupplier<Item> TEMPORAL_DIRT = ITEMS.register(
    ModItemId.TEMPORAL_DIRT.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_DIRT.get(), new Item.Properties()
            .useBlockDescriptionPrefix()
            .setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_DIRT.id()))))
);

public static final RegistrySupplier<Item> TEMPORAL_GRASS_BLOCK = ITEMS.register(
    ModItemId.TEMPORAL_GRASS_BLOCK.id(),
    () -> new BlockItem(ModBlocks.TEMPORAL_GRASS_BLOCK.get(), new Item.Properties()
            .useBlockDescriptionPrefix()
            .setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_GRASS_BLOCK.id()))))
);

public static final RegistrySupplier<Item> COARSE_TEMPORAL_DIRT = ITEMS.register(
    ModItemId.COARSE_TEMPORAL_DIRT.id(),
    () -> new BlockItem(ModBlocks.COARSE_TEMPORAL_DIRT.get(), new Item.Properties()
            .useBlockDescriptionPrefix()
            .setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.COARSE_TEMPORAL_DIRT.id()))))
);
```

- [ ] **Step 3: Add to creative tab**

In the `populateCreativeTab` method of `ModItems.java`, add after `output.accept(TEMPORAL_MOSS.get());`:

```java
output.accept(TEMPORAL_DIRT.get());
output.accept(TEMPORAL_GRASS_BLOCK.get());
output.accept(COARSE_TEMPORAL_DIRT.get());
```

- [ ] **Step 4: Build to verify**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register temporal dirt blocks and items (1.21.11)"
```

---

### Task 4: Add blockstate, model, and item model JSON (shared assets)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_dirt.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_grass_block.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/coarse_temporal_dirt.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_dirt.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_grass_block.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_grass_block_snow.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/coarse_temporal_dirt.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_dirt.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_grass_block.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/coarse_temporal_dirt.json`

- [ ] **Step 1: Create blockstate JSONs**

`blockstates/temporal_dirt.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/temporal_dirt"
    }
  }
}
```

`blockstates/coarse_temporal_dirt.json`:
```json
{
  "variants": {
    "": {
      "model": "chronodawn:block/coarse_temporal_dirt"
    }
  }
}
```

`blockstates/temporal_grass_block.json`:
```json
{
  "variants": {
    "snowy=false": {
      "model": "chronodawn:block/temporal_grass_block"
    },
    "snowy=true": {
      "model": "chronodawn:block/temporal_grass_block_snow"
    }
  }
}
```

- [ ] **Step 2: Create block model JSONs**

`models/block/temporal_dirt.json`:
```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/temporal_dirt"
  }
}
```

`models/block/coarse_temporal_dirt.json`:
```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/coarse_temporal_dirt"
  }
}
```

`models/block/temporal_grass_block.json` — follows vanilla grass_block model structure:
```json
{
  "parent": "minecraft:block/grass_block",
  "textures": {
    "bottom": "chronodawn:block/temporal_dirt",
    "top": "chronodawn:block/temporal_grass_block_top",
    "side": "chronodawn:block/temporal_grass_block_side",
    "overlay": "chronodawn:block/temporal_grass_block_side_overlay",
    "particle": "chronodawn:block/temporal_dirt"
  }
}
```

`models/block/temporal_grass_block_snow.json`:
```json
{
  "parent": "minecraft:block/cube_bottom_top",
  "textures": {
    "bottom": "chronodawn:block/temporal_dirt",
    "top": "chronodawn:block/temporal_grass_block_top",
    "side": "chronodawn:block/temporal_grass_block_side_snow",
    "particle": "chronodawn:block/temporal_dirt"
  }
}
```

Note: `temporal_grass_block_side_snow` is a dedicated snowy side texture. If a separate snow-side texture is not desired, use `"side": "minecraft:block/grass_block_snow"` to reuse vanilla's snowy side texture. If the user wants to keep textures minimal, this can reference `temporal_grass_block_side` instead and rely on the snow layer on top for visual distinction.

- [ ] **Step 3: Create item model JSONs**

`models/item/temporal_dirt.json`:
```json
{
  "parent": "chronodawn:block/temporal_dirt"
}
```

`models/item/temporal_grass_block.json`:
```json
{
  "parent": "chronodawn:block/temporal_grass_block"
}
```

`models/item/coarse_temporal_dirt.json`:
```json
{
  "parent": "chronodawn:block/coarse_temporal_dirt"
}
```

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/
git add common/shared/src/main/resources/assets/chronodawn/models/
git commit -m "feat: add blockstate and model JSON for temporal dirt blocks"
```

---

### Task 5: Add placeholder textures

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_dirt.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_grass_block_top.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_grass_block_side.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_grass_block_side_overlay.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/coarse_temporal_dirt.png`

**User-provided textures required.** These 5 PNG files (16x16) must be created by the user:

| File | Color Mode | Description |
|---|---|---|
| `temporal_dirt.png` | Full color | Dirt texture for all faces |
| `temporal_grass_block_top.png` | Grayscale | Grass top face, tinted by biome |
| `temporal_grass_block_side.png` | Full color | Side face with dirt + grass stripe |
| `temporal_grass_block_side_overlay.png` | Grayscale | Side overlay, grass part only, tinted by biome |
| `coarse_temporal_dirt.png` | Full color | Coarse dirt texture for all faces |

Optional (only if custom snowy side is desired):
| `temporal_grass_block_side_snow.png` | Full color | Side face when snowy (or reuse vanilla's) |

- [ ] **Step 1: Place user-provided textures into the textures directory**

Copy the 5 PNG files to: `common/shared/src/main/resources/assets/chronodawn/textures/block/`

- [ ] **Step 2: Build to verify texture references resolve**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_dirt.png
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_grass_block_top.png
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_grass_block_side.png
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_grass_block_side_overlay.png
git add common/shared/src/main/resources/assets/chronodawn/textures/block/coarse_temporal_dirt.png
git commit -m "feat: add textures for temporal dirt blocks"
```

---

### Task 6: Add translations

**Files:**
- Modify: `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json`

- [ ] **Step 1: Add English translations**

Add these entries to all 3 lang files:

```json
"block.chronodawn.temporal_dirt": "Temporal Dirt",
"block.chronodawn.temporal_grass_block": "Temporal Grass Block",
"block.chronodawn.coarse_temporal_dirt": "Coarse Temporal Dirt",
```

- [ ] **Step 2: Add Japanese translations**

Check if Japanese lang files exist and add:

```json
"block.chronodawn.temporal_dirt": "時の土",
"block.chronodawn.temporal_grass_block": "時の草ブロック",
"block.chronodawn.coarse_temporal_dirt": "粗い時の土",
```

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/
git add common/1.21.1/src/main/resources/assets/chronodawn/lang/
git add common/1.20.1/src/main/resources/assets/chronodawn/lang/
git commit -m "feat: add translations for temporal dirt blocks"
```

---

### Task 7: Add loot tables

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_dirt.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_grass_block.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/coarse_temporal_dirt.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_dirt.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_grass_block.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/coarse_temporal_dirt.json`

- [ ] **Step 1: Create loot tables for 1.21.1+**

`temporal_dirt.json` (drops itself):
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_dirt"
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

`coarse_temporal_dirt.json` (drops itself):
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:coarse_temporal_dirt"
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

`temporal_grass_block.json` (drops temporal_dirt, silk touch drops itself):
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
              "name": "chronodawn:temporal_grass_block",
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
              "name": "chronodawn:temporal_dirt"
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

- [ ] **Step 2: Create loot tables for 1.20.1**

Same content as above, but placed in `loot_tables` (plural) directory.

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/
git commit -m "feat: add loot tables for temporal dirt blocks"
```

---

### Task 8: Add block tags

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/shovel.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/hoe.json`

- [ ] **Step 1: Create shovel mineable tag**

`shovel.json`:
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

- [ ] **Step 2: Update hoe mineable tag**

Add `coarse_temporal_dirt` to existing `hoe.json` values (coarse dirt is hoe-tillable):

Current values: `["chronodawn:time_wood_leaves", "chronodawn:temporal_moss"]`

Updated values:
```json
{
  "replace": false,
  "values": [
    "chronodawn:time_wood_leaves",
    "chronodawn:temporal_moss",
    "chronodawn:coarse_temporal_dirt"
  ]
}
```

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/
git commit -m "feat: add block tags for temporal dirt blocks"
```

---

### Task 9: Register block colors (client-side tinting)

**Files:**
- Modify: All 11 `fabric/<ver>/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modify: `neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`
- Modify: Version-specific NeoForge client files as needed

- [ ] **Step 1: Add block color to Fabric client (1.21.11)**

In `fabric/1.21.11/.../ChronoDawnClientFabric.java`, add after the existing `TIME_WOOD_LEAVES` block color registration:

```java
ColorProviderRegistry.BLOCK.register(
    (state, world, pos, tintIndex) -> {
        if (world != null && pos != null) {
            return BiomeColors.getAverageGrassColor(world, pos);
        }
        return GrassColor.get(0.5, 1.0);
    },
    ModBlocks.TEMPORAL_GRASS_BLOCK.get()
);
```

Add imports:
```java
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
```

- [ ] **Step 2: Add item color for Fabric 1.20.1 and 1.21.1**

For Fabric versions < 1.21.4, item tinting is done in code:

```java
ColorProviderRegistry.ITEM.register(
    (stack, tintIndex) -> GrassColor.get(0.5, 1.0),
    ModBlocks.TEMPORAL_GRASS_BLOCK.get()
);
```

- [ ] **Step 3: Add block + item color to NeoForge**

In `neoforge/base/.../ChronoDawnClientNeoForge.java`, in `onRegisterBlockColors`:

```java
event.register(
    (state, world, pos, tintIndex) -> {
        if (world != null && pos != null) {
            return BiomeColors.getAverageGrassColor(world, pos);
        }
        return GrassColor.get(0.5, 1.0);
    },
    ModBlocks.TEMPORAL_GRASS_BLOCK.get()
);
```

In `onRegisterItemColors`:

```java
event.register(
    (stack, tintIndex) -> GrassColor.get(0.5, 1.0),
    ModBlocks.TEMPORAL_GRASS_BLOCK.get()
);
```

Add imports:
```java
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
```

- [ ] **Step 4: Propagate to all Fabric version files**

Repeat Step 1 for all 11 Fabric versions. For versions 1.20.1 and 1.21.1, also add item color (Step 2).

- [ ] **Step 5: Build to verify**

Run: `./gradlew build1_21_11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add fabric/
git add neoforge/
git commit -m "feat: register biome-tinted block colors for temporal grass block"
```

---

### Task 10: Add item model definitions (1.21.4+)

**Files:**
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_dirt.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_grass_block.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/coarse_temporal_dirt.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_dirt.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_grass_block.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/coarse_temporal_dirt.json`

- [ ] **Step 1: Create item definitions for 1.21.5+ (shared)**

`items/temporal_dirt.json`:
```json
{
  "model": {
    "type": "minecraft:model",
    "model": "chronodawn:item/temporal_dirt"
  }
}
```

`items/coarse_temporal_dirt.json`:
```json
{
  "model": {
    "type": "minecraft:model",
    "model": "chronodawn:item/coarse_temporal_dirt"
  }
}
```

`items/temporal_grass_block.json` (with grass tint for inventory display):
```json
{
  "model": {
    "type": "minecraft:model",
    "model": "chronodawn:item/temporal_grass_block",
    "tints": [
      {
        "type": "minecraft:grass",
        "downfall": 1.0,
        "temperature": 0.5
      }
    ]
  }
}
```

- [ ] **Step 2: Create identical item definitions for 1.21.4**

Copy the same 3 files to `common/1.21.4/src/main/resources/assets/chronodawn/items/`.

- [ ] **Step 3: Commit**

```bash
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/
git add common/1.21.4/src/main/resources/assets/chronodawn/items/
git commit -m "feat: add item model definitions for temporal dirt blocks (1.21.4+)"
```

---

### Task 11: Update surface rules

**Files:**
- Modify: All 11 `common/<ver>/src/main/resources/data/minecraft/worldgen/noise_settings/overworld.json`

- [ ] **Step 1: Understand the replacement strategy**

In each `overworld.json`, find all `result_state` entries that reference:
- `"Name": "minecraft:grass_block"` → replace with `"Name": "chronodawn:temporal_grass_block"`
- `"Name": "minecraft:dirt"` → replace with `"Name": "chronodawn:temporal_dirt"`
- `"Name": "minecraft:coarse_dirt"` → replace with `"Name": "chronodawn:coarse_temporal_dirt"`

**Important:** These are dimension-specific noise settings. Only the ChronoDawn dimension uses this file. The replacements affect ALL biomes except desert (which uses `reversing_time_sandstone`). Since the entire overworld.json is overridden by the mod for the dimension, all references should be replaced.

- [ ] **Step 2: Update 1.21.11 overworld.json**

Use search-and-replace on all three block references in the file.

- [ ] **Step 3: Propagate to all 10 remaining versions**

Each version has its own overworld.json. Apply the same replacements.

- [ ] **Step 4: Build all versions to verify**

Run: `./gradlew buildAll`
Expected: BUILD SUCCESSFUL for all versions

- [ ] **Step 5: Commit**

```bash
git add common/1.20.1/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.1/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.2/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.4/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.5/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.6/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.7/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.8/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.9/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.10/src/main/resources/data/minecraft/worldgen/noise_settings/
git add common/1.21.11/src/main/resources/data/minecraft/worldgen/noise_settings/
git commit -m "feat: update surface rules to use temporal dirt blocks"
```

---

### Task 12: Update worldgen features

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/patch_coarse_dirt.json`
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/grass_block.json`
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/tall_grass_block.json`

- [ ] **Step 1: Update patch_coarse_dirt configured feature**

Change the target and state to use custom blocks:

```json
{
  "type": "minecraft:disk",
  "config": {
    "half_height": 0,
    "radius": {
      "type": "minecraft:uniform",
      "min_inclusive": 1,
      "max_inclusive": 3
    },
    "state_provider": {
      "fallback": {
        "type": "minecraft:simple_state_provider",
        "state": {
          "Name": "chronodawn:coarse_temporal_dirt"
        }
      },
      "rules": []
    },
    "target": {
      "type": "minecraft:matching_blocks",
      "blocks": [
        "chronodawn:temporal_grass_block"
      ]
    }
  }
}
```

- [ ] **Step 2: Update grass_block placed feature**

Add custom blocks to the placement predicate:

```json
{
  "feature": "chronodawn:grass_block",
  "placement": [
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:all_of",
        "predicates": [
          {
            "type": "minecraft:matching_blocks",
            "offset": [0, 0, 0],
            "blocks": ["minecraft:air"]
          },
          {
            "type": "minecraft:matching_blocks",
            "offset": [0, -1, 0],
            "blocks": [
              "minecraft:grass_block",
              "minecraft:dirt",
              "minecraft:podzol",
              "minecraft:coarse_dirt",
              "chronodawn:temporal_grass_block",
              "chronodawn:temporal_dirt",
              "chronodawn:coarse_temporal_dirt"
            ]
          }
        ]
      }
    }
  ]
}
```

- [ ] **Step 3: Update tall_grass_block placed feature**

Same change — add custom blocks to the matching_blocks predicate:

```json
{
  "feature": "chronodawn:tall_grass_block",
  "placement": [
    {
      "type": "minecraft:block_predicate_filter",
      "predicate": {
        "type": "minecraft:all_of",
        "predicates": [
          {
            "type": "minecraft:matching_blocks",
            "offset": [0, 0, 0],
            "blocks": ["minecraft:air"]
          },
          {
            "type": "minecraft:matching_blocks",
            "offset": [0, -1, 0],
            "blocks": [
              "minecraft:grass_block",
              "minecraft:dirt",
              "minecraft:podzol",
              "minecraft:coarse_dirt",
              "chronodawn:temporal_grass_block",
              "chronodawn:temporal_dirt",
              "chronodawn:coarse_temporal_dirt"
            ]
          }
        ]
      }
    }
  ]
}
```

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/
git commit -m "feat: update worldgen features to use temporal dirt blocks"
```

---

### Task 13: Propagate block classes to all versions

**Files:**
- Create: Block classes in `common/<ver>/src/main/java/com/chronodawn/blocks/` for all 10 remaining versions
- Modify: `ModBlocks.java` and `ModItems.java` in all 10 remaining versions

- [ ] **Step 1: Adapt block classes for 1.21.1**

For 1.21.1 (and 1.20.1), the key differences from 1.21.11:
- No `.setId()` call in `createProperties()`
- No `MapCodec` / `codec()` override needed (added in 1.21.5)

`TemporalDirtBlock.java` for 1.21.1:
```java
package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
// ... same useItemOn imports ...

public class TemporalDirtBlock extends Block {
    public TemporalDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.DIRT);
    }

    // useItemOn for hoe → farmland (same logic)
}
```

Similar adaptations for `TemporalGrassBlock` and `CoarseTemporalDirtBlock`.

- [ ] **Step 2: Adapt for 1.21.5+ versions (need codec)**

Versions 1.21.5 through 1.21.11 need `MapCodec` and `codec()`. Versions 1.21.5+ also need `.setId()` in properties.

- [ ] **Step 3: Adapt for 1.21.2 and 1.21.4**

These versions need `.setId()` but may differ in whether codec is required. Check existing block classes for these versions to confirm.

- [ ] **Step 4: Copy and adapt ModBlocks.java and ModItems.java registrations**

For each of the 10 remaining versions, add the same 3 block registrations and 3 item registrations as in Task 3, adjusting for version-specific API differences.

- [ ] **Step 5: Build all versions**

Run: `./gradlew buildAll`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add common/1.20.1/ common/1.21.1/ common/1.21.2/ common/1.21.4/ common/1.21.5/
git add common/1.21.6/ common/1.21.7/ common/1.21.8/ common/1.21.9/ common/1.21.10/
git commit -m "feat: propagate temporal dirt block classes to all versions"
```

---

### Task 14: Full verification

- [ ] **Step 1: Run validateResources**

Run: `./gradlew validateResources`
Expected: No errors (verifies blockstate→model→texture cross-references)

- [ ] **Step 2: Run validateTranslations**

Run: `./gradlew validateTranslations`
Expected: No missing translation keys

- [ ] **Step 3: Run buildAll**

Run: `./gradlew buildAll`
Expected: BUILD SUCCESSFUL for all versions

- [ ] **Step 4: Run testAll**

Run: `./gradlew testAll`
Expected: All tests pass

- [ ] **Step 5: Run gameTestAll**

Run: `./gradlew gameTestAll`
Expected: All game tests pass

- [ ] **Step 6: Visual verification in-game**

Run: `./gradlew runClientFabric1_21_11`

Verify:
1. Blocks appear in creative tab
2. temporal_dirt texture displays correctly
3. temporal_grass_block top is tinted by biome color
4. temporal_grass_block side overlay is tinted
5. Hoe on temporal_dirt → farmland
6. Hoe on coarse_temporal_dirt → temporal_dirt
7. Grass spreads from temporal_grass_block to adjacent temporal_dirt
8. Grass reverts to temporal_dirt when light blocked
9. Silk touch on temporal_grass_block drops itself
10. Normal break on temporal_grass_block drops temporal_dirt
11. Enter ChronoDawn dimension — surface uses temporal blocks
