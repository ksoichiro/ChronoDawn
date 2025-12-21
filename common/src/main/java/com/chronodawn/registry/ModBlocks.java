package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.blocks.BossRoomBoundaryMarkerBlock;
import com.chronodawn.blocks.BossRoomDoorBlock;
import com.chronodawn.blocks.EntropyCryptTrapdoorBlock;
import com.chronodawn.blocks.ClockstoneBlock;
import com.chronodawn.blocks.ClockstoneOre;
import com.chronodawn.blocks.ClockstoneSlab;
import com.chronodawn.blocks.ClockstoneStairs;
import com.chronodawn.blocks.ClockstoneWall;
import com.chronodawn.blocks.ClockTowerTeleporterBlock;
import com.chronodawn.blocks.ClockworkBlock;
import com.chronodawn.blocks.DecorativeWaterBlock;
import com.chronodawn.blocks.FruitOfTimeBlock;
import com.chronodawn.blocks.FrozenTimeIceBlock;
import com.chronodawn.blocks.ReversingTimeSandstone;
import com.chronodawn.blocks.TemporalBricksBlock;
import com.chronodawn.blocks.TemporalBricksSlab;
import com.chronodawn.blocks.TemporalBricksStairs;
import com.chronodawn.blocks.TemporalBricksWall;
import com.chronodawn.blocks.TemporalMossBlock;
import com.chronodawn.blocks.TemporalParticleEmitterBlock;
import com.chronodawn.blocks.TimeCrystalBlock;
import com.chronodawn.blocks.TimeCrystalOre;
import com.chronodawn.blocks.TimeWoodButton;
import com.chronodawn.blocks.TimeWoodDoor;
import com.chronodawn.blocks.TimeWoodFence;
import com.chronodawn.blocks.TimeWoodFenceGate;
import com.chronodawn.blocks.TimeWoodLeaves;
import com.chronodawn.blocks.TimeWoodLog;
import com.chronodawn.blocks.TimeWoodPlanks;
import com.chronodawn.blocks.TimeWoodPressurePlate;
import com.chronodawn.blocks.TimeWoodSapling;
import com.chronodawn.blocks.TimeWoodSlab;
import com.chronodawn.blocks.TimeWoodStairs;
import com.chronodawn.blocks.TimeWoodTrapdoor;
import com.chronodawn.blocks.UnstableFungus;
import com.chronodawn.blocks.DarkTimeWoodLog;
import com.chronodawn.blocks.DarkTimeWoodLeaves;
import com.chronodawn.blocks.DarkTimeWoodPlanks;
import com.chronodawn.blocks.DarkTimeWoodStairs;
import com.chronodawn.blocks.DarkTimeWoodSlab;
import com.chronodawn.blocks.DarkTimeWoodFence;
import com.chronodawn.blocks.DarkTimeWoodDoor;
import com.chronodawn.blocks.DarkTimeWoodTrapdoor;
import com.chronodawn.blocks.DarkTimeWoodFenceGate;
import com.chronodawn.blocks.DarkTimeWoodButton;
import com.chronodawn.blocks.DarkTimeWoodPressurePlate;
import com.chronodawn.blocks.DarkTimeWoodSapling;
import com.chronodawn.blocks.AncientTimeWoodLog;
import com.chronodawn.blocks.AncientTimeWoodLeaves;
import com.chronodawn.blocks.AncientTimeWoodPlanks;
import com.chronodawn.blocks.AncientTimeWoodStairs;
import com.chronodawn.blocks.AncientTimeWoodSlab;
import com.chronodawn.blocks.AncientTimeWoodFence;
import com.chronodawn.blocks.AncientTimeWoodDoor;
import com.chronodawn.blocks.AncientTimeWoodTrapdoor;
import com.chronodawn.blocks.AncientTimeWoodFenceGate;
import com.chronodawn.blocks.AncientTimeWoodButton;
import com.chronodawn.blocks.AncientTimeWoodPressurePlate;
import com.chronodawn.blocks.AncientTimeWoodSapling;
import com.chronodawn.blocks.TimeWheatBlock;
import com.chronodawn.blocks.TimeWheatBaleBlock;
import com.chronodawn.blocks.TemporalRootBlock;
import com.chronodawn.blocks.ChronoMelonStemBlock;
import com.chronodawn.blocks.AttachedChronoMelonStemBlock;
import com.chronodawn.blocks.ChronoMelonBlock;
import com.chronodawn.blocks.TimelessMushroomBlock;
import com.chronodawn.registry.ModFluids;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;

/**
 * Architectury Registry wrapper for custom blocks.
 *
 * This class provides a centralized registry for all custom blocks in the ChronoDawn mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ChronoDawn.MOD_ID, Registries.BLOCK);

    /**
     * Clockstone Ore - Found in Overworld (Ancient Ruins) and ChronoDawn dimension.
     * Drops Clockstone item when mined with appropriate tool.
     */
    public static final RegistrySupplier<Block> CLOCKSTONE_ORE = BLOCKS.register(
        "clockstone_ore",
        () -> new ClockstoneOre(ClockstoneOre.createProperties())
    );

    /**
     * Time Crystal Ore - Rare ore found in ChronoDawn dimension.
     * Drops Time Crystal item when mined with appropriate tool.
     * Spawns at Y: 0-48, vein size 3-5.
     */
    public static final RegistrySupplier<Block> TIME_CRYSTAL_ORE = BLOCKS.register(
        "time_crystal_ore",
        () -> new TimeCrystalOre(TimeCrystalOre.createProperties())
    );

    /**
     * Clockstone Block - Portal frame building material.
     * Crafted from 9x Clockstone items, used to construct portal frames.
     */
    public static final RegistrySupplier<Block> CLOCKSTONE_BLOCK = BLOCKS.register(
        "clockstone_block",
        () -> new ClockstoneBlock(ClockstoneBlock.createProperties())
    );

    /**
     * Clockstone Stairs - Decorative stair variant of Clockstone Block.
     * Crafted from 6x Clockstone Block in stair pattern.
     */
    public static final RegistrySupplier<Block> CLOCKSTONE_STAIRS = BLOCKS.register(
        "clockstone_stairs",
        () -> new ClockstoneStairs(ClockstoneStairs.createProperties())
    );

    /**
     * Clockstone Slab - Decorative slab variant of Clockstone Block.
     * Crafted from 3x Clockstone Block in horizontal row.
     */
    public static final RegistrySupplier<Block> CLOCKSTONE_SLAB = BLOCKS.register(
        "clockstone_slab",
        () -> new ClockstoneSlab(ClockstoneSlab.createProperties())
    );

    /**
     * Clockstone Wall - Decorative wall variant of Clockstone Block.
     * Crafted from 6x Clockstone Block in 2 rows of 3.
     */
    public static final RegistrySupplier<Block> CLOCKSTONE_WALL = BLOCKS.register(
        "clockstone_wall",
        () -> new ClockstoneWall(ClockstoneWall.createProperties())
    );

    /**
     * Reversing Time Sandstone - Special block found in ChronoDawn dimension.
     * Automatically restores itself 3 seconds after being destroyed.
     * Cannot be moved by pistons, does not drop items.
     */
    public static final RegistrySupplier<Block> REVERSING_TIME_SANDSTONE = BLOCKS.register(
        "reversing_time_sandstone",
        () -> new ReversingTimeSandstone(ReversingTimeSandstone.createProperties())
    );

    /**
     * Unstable Fungus - Special block found in ChronoDawn dimension.
     * Applies random speed effects (Speed I or Slowness I) to entities that collide with it.
     * Effect duration: 0.5 seconds.
     */
    public static final RegistrySupplier<Block> UNSTABLE_FUNGUS = BLOCKS.register(
        "unstable_fungus",
        () -> new UnstableFungus(UnstableFungus.createProperties())
    );

    /**
     * Potted Unstable Fungus - Decorative potted version of Unstable Fungus.
     * Can be created by using Unstable Fungus on a flower pot.
     */
    public static final RegistrySupplier<Block> POTTED_UNSTABLE_FUNGUS = BLOCKS.register(
        "potted_unstable_fungus",
        () -> new FlowerPotBlock(UNSTABLE_FUNGUS.get(), Block.Properties.ofFullCopy(Blocks.FLOWER_POT))
    );

    /**
     * Time Wood Log - Custom log block for Fruit of Time trees.
     * Forms the trunk of trees found in the ChronoDawn dimension.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_LOG = BLOCKS.register(
        "time_wood_log",
        () -> new TimeWoodLog(TimeWoodLog.createProperties())
    );

    /**
     * Time Wood Leaves - Custom leaves block for Fruit of Time trees.
     * Forms the canopy of trees found in the ChronoDawn dimension.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_LEAVES = BLOCKS.register(
        "time_wood_leaves",
        () -> new TimeWoodLeaves(TimeWoodLeaves.createProperties())
    );

    /**
     * Time Wood Planks - Crafted building material from Time Wood Logs.
     * Standard planks block with time-themed appearance.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_PLANKS = BLOCKS.register(
        "time_wood_planks",
        () -> new TimeWoodPlanks(TimeWoodPlanks.createProperties())
    );

    /**
     * Time Wood Sapling - Grows into Fruit of Time trees.
     * Obtained from Time Wood Leaves.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_SAPLING = BLOCKS.register(
        "time_wood_sapling",
        () -> new TimeWoodSapling(TimeWoodSapling.createProperties())
    );

    /**
     * Potted Time Wood Sapling - Decorative potted version of Time Wood Sapling.
     * Can be created by using Time Wood Sapling on a flower pot.
     */
    public static final RegistrySupplier<Block> POTTED_TIME_WOOD_SAPLING = BLOCKS.register(
        "potted_time_wood_sapling",
        () -> new FlowerPotBlock(TIME_WOOD_SAPLING.get(), Block.Properties.ofFullCopy(Blocks.FLOWER_POT))
    );

    /**
     * Fruit of Time Block - Growing fruit attached to Time Wood Logs.
     * Has 3 growth stages (0-2) and drops Fruit of Time items when mature.
     * Similar to Cocoa blocks in vanilla Minecraft.
     */
    public static final RegistrySupplier<Block> FRUIT_OF_TIME_BLOCK = BLOCKS.register(
        "fruit_of_time",
        () -> new FruitOfTimeBlock(FruitOfTimeBlock.createProperties())
    );

    /**
     * Clock Tower Teleporter - Time-themed teleporter for Desert Clock Tower.
     * Requires 3 seconds of charging (holding right-click) before teleporting.
     * UP direction: 4th floor → 5th floor (boss room).
     * DOWN direction: 5th floor → 4th floor (appears after defeating Time Guardian).
     */
    public static final RegistrySupplier<Block> CLOCK_TOWER_TELEPORTER = BLOCKS.register(
        "clock_tower_teleporter",
        () -> new ClockTowerTeleporterBlock(
            Block.Properties.ofFullCopy(Blocks.GLOWSTONE)
                .lightLevel(state -> 15)
                .noOcclusion()
        )
    );

    /**
     * Clockwork Block - Decorative block with animated rotating gears theme.
     * Crafted from Clockstone and iron, used for steampunk/mechanical builds.
     */
    public static final RegistrySupplier<Block> CLOCKWORK_BLOCK = BLOCKS.register(
        "clockwork_block",
        () -> new ClockworkBlock(ClockworkBlock.createProperties())
    );

    /**
     * Time Crystal Block - Decorative block that emits light.
     * Crafted from 9 Time Crystals, emits light level 10.
     */
    public static final RegistrySupplier<Block> TIME_CRYSTAL_BLOCK = BLOCKS.register(
        "time_crystal_block",
        () -> new TimeCrystalBlock(TimeCrystalBlock.createProperties())
    );

    /**
     * Temporal Bricks - Building block crafted from Clockstone.
     * Used for construction and has stairs/slabs/walls variants.
     */
    public static final RegistrySupplier<Block> TEMPORAL_BRICKS = BLOCKS.register(
        "temporal_bricks",
        () -> new TemporalBricksBlock(TemporalBricksBlock.createProperties())
    );

    /**
     * Temporal Bricks Stairs - Stair variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Block> TEMPORAL_BRICKS_STAIRS = BLOCKS.register(
        "temporal_bricks_stairs",
        () -> new TemporalBricksStairs(TemporalBricksStairs.createProperties())
    );

    /**
     * Temporal Bricks Slab - Slab variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Block> TEMPORAL_BRICKS_SLAB = BLOCKS.register(
        "temporal_bricks_slab",
        () -> new TemporalBricksSlab(TemporalBricksSlab.createProperties())
    );

    /**
     * Temporal Bricks Wall - Wall variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Block> TEMPORAL_BRICKS_WALL = BLOCKS.register(
        "temporal_bricks_wall",
        () -> new TemporalBricksWall(TemporalBricksWall.createProperties())
    );

    /**
     * Time Wood Stairs - Stair variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_STAIRS = BLOCKS.register(
        "time_wood_stairs",
        () -> new TimeWoodStairs(TimeWoodStairs.createProperties())
    );

    /**
     * Time Wood Slab - Slab variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_SLAB = BLOCKS.register(
        "time_wood_slab",
        () -> new TimeWoodSlab(TimeWoodSlab.createProperties())
    );

    /**
     * Time Wood Fence - Fence variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_FENCE = BLOCKS.register(
        "time_wood_fence",
        () -> new TimeWoodFence(TimeWoodFence.createProperties())
    );

    /**
     * Time Wood Door - Wooden door that can be opened/closed.
     * Can be opened manually or with redstone signal.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_DOOR = BLOCKS.register(
        "time_wood_door",
        () -> new TimeWoodDoor(TimeWoodDoor.createProperties())
    );

    /**
     * Time Wood Trapdoor - Wooden trapdoor that can be opened/closed.
     * Can be placed horizontally or vertically.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_TRAPDOOR = BLOCKS.register(
        "time_wood_trapdoor",
        () -> new TimeWoodTrapdoor(TimeWoodTrapdoor.createProperties())
    );

    /**
     * Time Wood Fence Gate - Fence gate that connects to fences.
     * Can be opened manually or with redstone signal.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_FENCE_GATE = BLOCKS.register(
        "time_wood_fence_gate",
        () -> new TimeWoodFenceGate(TimeWoodFenceGate.createProperties())
    );

    /**
     * Time Wood Button - Wooden button that emits redstone signal when pressed.
     * Stays active for 1.5 seconds (30 ticks).
     */
    public static final RegistrySupplier<Block> TIME_WOOD_BUTTON = BLOCKS.register(
        "time_wood_button",
        () -> new TimeWoodButton(TimeWoodButton.createProperties())
    );

    /**
     * Time Wood Pressure Plate - Wooden pressure plate that emits redstone signal.
     * Activated by players, mobs, items, and other entities.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_PRESSURE_PLATE = BLOCKS.register(
        "time_wood_pressure_plate",
        () -> new TimeWoodPressurePlate(TimeWoodPressurePlate.createProperties())
    );

    /**
     * Temporal Moss - Decorative moss block exclusive to swamp biome.
     * Spreads to adjacent blocks similar to vanilla moss.
     */
    public static final RegistrySupplier<Block> TEMPORAL_MOSS = BLOCKS.register(
        "temporal_moss",
        () -> new TemporalMossBlock(TemporalMossBlock.createProperties())
    );

    /**
     * Frozen Time Ice - Special ice block exclusive to snowy biome.
     * Does not melt when exposed to light sources, slippery like ice.
     */
    public static final RegistrySupplier<Block> FROZEN_TIME_ICE = BLOCKS.register(
        "frozen_time_ice",
        () -> new FrozenTimeIceBlock(FrozenTimeIceBlock.createProperties())
    );

    /**
     * Time Wheat - Custom crop that grows in the ChronoDawn dimension.
     * Has 8 growth stages (0-7) like vanilla wheat.
     * Drops Time Wheat Seeds and Time Wheat (when mature).
     */
    public static final RegistrySupplier<Block> TIME_WHEAT = BLOCKS.register(
        "time_wheat",
        () -> new TimeWheatBlock(
            Block.Properties.ofFullCopy(Blocks.WHEAT)
        )
    );

    /**
     * Time Wheat Bale - Decorative storage block for Time Wheat.
     * Crafted from 9 Time Wheat items.
     * Can be rotated in 3 axes (like logs).
     * Reduces fall damage by 80% (same as vanilla hay bale).
     */
    public static final RegistrySupplier<Block> TIME_WHEAT_BALE = BLOCKS.register(
        "time_wheat_bale",
        () -> new TimeWheatBaleBlock(
            Block.Properties.ofFullCopy(Blocks.HAY_BLOCK)
        )
    );

    /**
     * Temporal Root - Root vegetable crop that grows on farmland.
     * Has 8 growth stages (0-7) like vanilla carrots/potatoes.
     * Drops Temporal Root items (food + seed).
     */
    public static final RegistrySupplier<Block> TEMPORAL_ROOT = BLOCKS.register(
        "temporal_root",
        () -> new TemporalRootBlock(
            Block.Properties.ofFullCopy(Blocks.CARROTS)
        )
    );

    /**
     * Chrono Melon Stem - Stem block that grows Chrono Melons.
     * Has 8 growth stages (0-7) like vanilla melon/pumpkin stems.
     * Produces melons on adjacent blocks when mature.
     */
    public static final RegistrySupplier<Block> CHRONO_MELON_STEM = BLOCKS.register(
        "chrono_melon_stem",
        () -> new ChronoMelonStemBlock(
            Block.Properties.ofFullCopy(Blocks.MELON_STEM)
        )
    );

    /**
     * Attached Chrono Melon Stem - Stem attached to a grown Chrono Melon.
     * Appears when a mature stem successfully grows a melon on an adjacent block.
     * Points toward the melon it's attached to.
     */
    public static final RegistrySupplier<Block> ATTACHED_CHRONO_MELON_STEM = BLOCKS.register(
        "attached_chrono_melon_stem",
        () -> new AttachedChronoMelonStemBlock(
            Block.Properties.ofFullCopy(Blocks.ATTACHED_MELON_STEM)
        )
    );

    /**
     * Chrono Melon - Full melon block that drops slices.
     * Drops 3-7 Chrono Melon Slices when broken (without Silk Touch).
     * Fortune enchantment increases drop count.
     */
    public static final RegistrySupplier<Block> CHRONO_MELON = BLOCKS.register(
        "chrono_melon",
        () -> new ChronoMelonBlock(
            Block.Properties.ofFullCopy(Blocks.MELON)
        )
    );

    /**
     * Timeless Mushroom - Edible mushroom that grows in darkness.
     * Grows in low light (light level 12 or less).
     * Can spread to nearby blocks in darkness.
     * Distinct from Unstable Fungus (silver/white vs purple/blue).
     */
    public static final RegistrySupplier<Block> TIMELESS_MUSHROOM = BLOCKS.register(
        "timeless_mushroom",
        () -> new TimelessMushroomBlock(
            Block.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM)
        )
    );

    /**
     * Boss Room Door - Custom iron door with BlockEntity for NBT data storage.
     * Identical appearance to vanilla iron door but can differentiate between:
     * - Entrance door (requires Key to Master Clock)
     * - Boss room door (requires 3x Ancient Gears)
     * Door type is stored in BlockEntity NBT and set in structure files.
     */
    public static final RegistrySupplier<Block> BOSS_ROOM_DOOR = BLOCKS.register(
        "boss_room_door",
        () -> new BossRoomDoorBlock(
            Block.Properties.ofFullCopy(Blocks.IRON_DOOR)
                .noOcclusion()
        )
    );

    /**
     * Entropy Crypt Trapdoor - Trapdoor that leads to Vault in Entropy Crypt.
     *
     * When a player tries to open it, Entropy Keeper spawns.
     * After the boss is spawned (ACTIVATED=true), functions as normal trapdoor.
     *
     * Task: T237 - Entropy Keeper boss spawn system
     */
    public static final RegistrySupplier<Block> ENTROPY_CRYPT_TRAPDOOR = BLOCKS.register(
        "entropy_crypt_trapdoor",
        () -> new EntropyCryptTrapdoorBlock(
            Block.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR)
                .noOcclusion()
        )
    );

    /**
     * Temporal Particle Emitter - Invisible block that emits time distortion particles.
     *
     * Used in Ancient Ruins to visualize Temporal Seal failure.
     * Emits purple portal particles that float upward, indicating temporal energy leakage.
     * Invisible, indestructible, no collision - intended for structure placement only.
     *
     * Task: T115m [US2]
     * Reference: specs/001-chronosphere-mod/lore.md
     */
    public static final RegistrySupplier<Block> TEMPORAL_PARTICLE_EMITTER = BLOCKS.register(
        "temporal_particle_emitter",
        () -> new TemporalParticleEmitterBlock(TemporalParticleEmitterBlock.createProperties())
    );

    /**
     * Decorative Water - Water block that looks like vanilla water but has a different ID.
     *
     * Used in NBT structure files to distinguish decorative water from Aquifer water.
     * During structure generation:
     * - Aquifer water (minecraft:water) is removed to prevent waterlogging
     * - Decorative water (chronodawn:decorative_water) is preserved
     * - A processor converts decorative water to minecraft:water after placement
     *
     * Creative mode only - place with Decorative Water Bucket.
     *
     * Task: T239 [US3] Guardian Vault & Master Clock waterlogging prevention
     */
    public static final RegistrySupplier<Block> DECORATIVE_WATER = BLOCKS.register(
        "decorative_water",
        () -> new DecorativeWaterBlock(ModFluids.DECORATIVE_WATER.get(), Block.Properties.ofFullCopy(Blocks.WATER))
    );

    /**
     * Boss Room Boundary Marker - Special marker block for defining boss room protection areas.
     * Visible during structure editing (Jigsaw-like appearance).
     * Replaced with specified block during world generation.
     * Not included in creative tab - placed via Structure Block.
     */
    public static final RegistrySupplier<Block> BOSS_ROOM_BOUNDARY_MARKER = BLOCKS.register(
        "boss_room_boundary_marker",
        () -> new BossRoomBoundaryMarkerBlock(Block.Properties.of()
            .strength(-1.0F, 3600000.0F) // Unbreakable like bedrock
            .noLootTable()
            .noOcclusion())
    );

    // ===== Dark Time Wood Blocks =====

    /**
     * Dark Time Wood Log - Darker variant of Time Wood Log.
     * Found in tall trees in the ChronoDawn dimension.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_LOG = BLOCKS.register(
        "dark_time_wood_log",
        () -> new DarkTimeWoodLog(DarkTimeWoodLog.createProperties())
    );

    /**
     * Dark Time Wood Leaves - Darker variant of Time Wood Leaves.
     * Forms the canopy of tall dark trees.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_LEAVES = BLOCKS.register(
        "dark_time_wood_leaves",
        () -> new DarkTimeWoodLeaves(DarkTimeWoodLeaves.createProperties())
    );

    /**
     * Dark Time Wood Planks - Crafted from Dark Time Wood Logs.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_PLANKS = BLOCKS.register(
        "dark_time_wood_planks",
        () -> new DarkTimeWoodPlanks(DarkTimeWoodPlanks.createProperties())
    );

    /**
     * Dark Time Wood Stairs - Stair variant of Dark Time Wood Planks.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_STAIRS = BLOCKS.register(
        "dark_time_wood_stairs",
        () -> new DarkTimeWoodStairs(DarkTimeWoodStairs.createProperties())
    );

    /**
     * Dark Time Wood Slab - Slab variant of Dark Time Wood Planks.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_SLAB = BLOCKS.register(
        "dark_time_wood_slab",
        () -> new DarkTimeWoodSlab(DarkTimeWoodSlab.createProperties())
    );

    /**
     * Dark Time Wood Fence - Fence variant of Dark Time Wood Planks.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_FENCE = BLOCKS.register(
        "dark_time_wood_fence",
        () -> new DarkTimeWoodFence(DarkTimeWoodFence.createProperties())
    );

    /**
     * Dark Time Wood Door - Wooden door that can be opened/closed.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_DOOR = BLOCKS.register(
        "dark_time_wood_door",
        () -> new DarkTimeWoodDoor(DarkTimeWoodDoor.createProperties())
    );

    /**
     * Dark Time Wood Trapdoor - Wooden trapdoor that can be opened/closed.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_TRAPDOOR = BLOCKS.register(
        "dark_time_wood_trapdoor",
        () -> new DarkTimeWoodTrapdoor(DarkTimeWoodTrapdoor.createProperties())
    );

    /**
     * Dark Time Wood Fence Gate - Fence gate that connects to fences.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_FENCE_GATE = BLOCKS.register(
        "dark_time_wood_fence_gate",
        () -> new DarkTimeWoodFenceGate(DarkTimeWoodFenceGate.createProperties())
    );

    /**
     * Dark Time Wood Button - Wooden button that emits redstone signal.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_BUTTON = BLOCKS.register(
        "dark_time_wood_button",
        () -> new DarkTimeWoodButton(DarkTimeWoodButton.createProperties())
    );

    /**
     * Dark Time Wood Pressure Plate - Wooden pressure plate that emits redstone signal.
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_PRESSURE_PLATE = BLOCKS.register(
        "dark_time_wood_pressure_plate",
        () -> new DarkTimeWoodPressurePlate(DarkTimeWoodPressurePlate.createProperties())
    );

    /**
     * Dark Time Wood Sapling - Grows into Dark Time Wood trees (tall variant).
     */
    public static final RegistrySupplier<Block> DARK_TIME_WOOD_SAPLING = BLOCKS.register(
        "dark_time_wood_sapling",
        () -> new DarkTimeWoodSapling(DarkTimeWoodSapling.createProperties())
    );

    /**
     * Potted Dark Time Wood Sapling - Decorative potted version of Dark Time Wood Sapling.
     */
    public static final RegistrySupplier<Block> POTTED_DARK_TIME_WOOD_SAPLING = BLOCKS.register(
        "potted_dark_time_wood_sapling",
        () -> new FlowerPotBlock(DARK_TIME_WOOD_SAPLING.get(), Block.Properties.ofFullCopy(Blocks.FLOWER_POT))
    );

    // ===== Ancient Time Wood Blocks =====

    /**
     * Ancient Time Wood Log - Weathered variant of Time Wood Log.
     * Found in wide-canopy trees in the ChronoDawn dimension.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_LOG = BLOCKS.register(
        "ancient_time_wood_log",
        () -> new AncientTimeWoodLog(AncientTimeWoodLog.createProperties())
    );

    /**
     * Ancient Time Wood Leaves - Wider-spreading leaves for ancient trees.
     * Forms a broad canopy on ancient trees.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_LEAVES = BLOCKS.register(
        "ancient_time_wood_leaves",
        () -> new AncientTimeWoodLeaves(AncientTimeWoodLeaves.createProperties())
    );

    /**
     * Ancient Time Wood Planks - Crafted from Ancient Time Wood Logs.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_PLANKS = BLOCKS.register(
        "ancient_time_wood_planks",
        () -> new AncientTimeWoodPlanks(AncientTimeWoodPlanks.createProperties())
    );

    /**
     * Ancient Time Wood Stairs - Stair variant of Ancient Time Wood Planks.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_STAIRS = BLOCKS.register(
        "ancient_time_wood_stairs",
        () -> new AncientTimeWoodStairs(AncientTimeWoodStairs.createProperties())
    );

    /**
     * Ancient Time Wood Slab - Slab variant of Ancient Time Wood Planks.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_SLAB = BLOCKS.register(
        "ancient_time_wood_slab",
        () -> new AncientTimeWoodSlab(AncientTimeWoodSlab.createProperties())
    );

    /**
     * Ancient Time Wood Fence - Fence variant of Ancient Time Wood Planks.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_FENCE = BLOCKS.register(
        "ancient_time_wood_fence",
        () -> new AncientTimeWoodFence(AncientTimeWoodFence.createProperties())
    );

    /**
     * Ancient Time Wood Door - Wooden door that can be opened/closed.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_DOOR = BLOCKS.register(
        "ancient_time_wood_door",
        () -> new AncientTimeWoodDoor(AncientTimeWoodDoor.createProperties())
    );

    /**
     * Ancient Time Wood Trapdoor - Wooden trapdoor that can be opened/closed.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_TRAPDOOR = BLOCKS.register(
        "ancient_time_wood_trapdoor",
        () -> new AncientTimeWoodTrapdoor(AncientTimeWoodTrapdoor.createProperties())
    );

    /**
     * Ancient Time Wood Fence Gate - Fence gate that connects to fences.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_FENCE_GATE = BLOCKS.register(
        "ancient_time_wood_fence_gate",
        () -> new AncientTimeWoodFenceGate(AncientTimeWoodFenceGate.createProperties())
    );

    /**
     * Ancient Time Wood Button - Wooden button that emits redstone signal.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_BUTTON = BLOCKS.register(
        "ancient_time_wood_button",
        () -> new AncientTimeWoodButton(AncientTimeWoodButton.createProperties())
    );

    /**
     * Ancient Time Wood Pressure Plate - Wooden pressure plate that emits redstone signal.
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_PRESSURE_PLATE = BLOCKS.register(
        "ancient_time_wood_pressure_plate",
        () -> new AncientTimeWoodPressurePlate(AncientTimeWoodPressurePlate.createProperties())
    );

    /**
     * Ancient Time Wood Sapling - Grows into Ancient Time Wood trees (wide-canopy variant).
     */
    public static final RegistrySupplier<Block> ANCIENT_TIME_WOOD_SAPLING = BLOCKS.register(
        "ancient_time_wood_sapling",
        () -> new AncientTimeWoodSapling(AncientTimeWoodSapling.createProperties())
    );

    /**
     * Potted Ancient Time Wood Sapling - Decorative potted version of Ancient Time Wood Sapling.
     */
    public static final RegistrySupplier<Block> POTTED_ANCIENT_TIME_WOOD_SAPLING = BLOCKS.register(
        "potted_ancient_time_wood_sapling",
        () -> new FlowerPotBlock(ANCIENT_TIME_WOOD_SAPLING.get(), Block.Properties.ofFullCopy(Blocks.FLOWER_POT))
    );

    /**
     * Initialize block registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        BLOCKS.register();
        ChronoDawn.LOGGER.info("Registered ModBlocks");
    }
}
