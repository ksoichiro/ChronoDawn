package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.blocks.BossRoomDoorBlock;
import com.chronosphere.blocks.ClockstoneBlock;
import com.chronosphere.blocks.ClockstoneOre;
import com.chronosphere.blocks.ClockstoneSlab;
import com.chronosphere.blocks.ClockstoneStairs;
import com.chronosphere.blocks.ClockstoneWall;
import com.chronosphere.blocks.ClockTowerTeleporterBlock;
import com.chronosphere.blocks.ClockworkBlock;
import com.chronosphere.blocks.FruitOfTimeBlock;
import com.chronosphere.blocks.FrozenTimeIceBlock;
import com.chronosphere.blocks.ReversingTimeSandstone;
import com.chronosphere.blocks.TemporalBricksBlock;
import com.chronosphere.blocks.TemporalBricksSlab;
import com.chronosphere.blocks.TemporalBricksStairs;
import com.chronosphere.blocks.TemporalBricksWall;
import com.chronosphere.blocks.TemporalMossBlock;
import com.chronosphere.blocks.TemporalParticleEmitterBlock;
import com.chronosphere.blocks.TimeCrystalBlock;
import com.chronosphere.blocks.TimeCrystalOre;
import com.chronosphere.blocks.TimeWoodButton;
import com.chronosphere.blocks.TimeWoodDoor;
import com.chronosphere.blocks.TimeWoodFence;
import com.chronosphere.blocks.TimeWoodFenceGate;
import com.chronosphere.blocks.TimeWoodLeaves;
import com.chronosphere.blocks.TimeWoodLog;
import com.chronosphere.blocks.TimeWoodPlanks;
import com.chronosphere.blocks.TimeWoodPressurePlate;
import com.chronosphere.blocks.TimeWoodSapling;
import com.chronosphere.blocks.TimeWoodSlab;
import com.chronosphere.blocks.TimeWoodStairs;
import com.chronosphere.blocks.TimeWoodTrapdoor;
import com.chronosphere.blocks.UnstableFungus;
import com.chronosphere.blocks.TimeWheatBlock;
import com.chronosphere.blocks.TimeWheatBaleBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;

/**
 * Architectury Registry wrapper for custom blocks.
 *
 * This class provides a centralized registry for all custom blocks in the Chronosphere mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Chronosphere.MOD_ID, Registries.BLOCK);

    /**
     * Clockstone Ore - Found in Overworld (Ancient Ruins) and Chronosphere dimension.
     * Drops Clockstone item when mined with appropriate tool.
     */
    public static final RegistrySupplier<Block> CLOCKSTONE_ORE = BLOCKS.register(
        "clockstone_ore",
        () -> new ClockstoneOre(ClockstoneOre.createProperties())
    );

    /**
     * Time Crystal Ore - Rare ore found in Chronosphere dimension.
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
     * Reversing Time Sandstone - Special block found in Chronosphere dimension.
     * Automatically restores itself 3 seconds after being destroyed.
     * Cannot be moved by pistons, does not drop items.
     */
    public static final RegistrySupplier<Block> REVERSING_TIME_SANDSTONE = BLOCKS.register(
        "reversing_time_sandstone",
        () -> new ReversingTimeSandstone(ReversingTimeSandstone.createProperties())
    );

    /**
     * Unstable Fungus - Special block found in Chronosphere dimension.
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
     * Forms the trunk of trees found in the Chronosphere dimension.
     */
    public static final RegistrySupplier<Block> TIME_WOOD_LOG = BLOCKS.register(
        "time_wood_log",
        () -> new TimeWoodLog(TimeWoodLog.createProperties())
    );

    /**
     * Time Wood Leaves - Custom leaves block for Fruit of Time trees.
     * Forms the canopy of trees found in the Chronosphere dimension.
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
     * Time Wheat - Custom crop that grows in the Chronosphere dimension.
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
     * Initialize block registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        BLOCKS.register();
        Chronosphere.LOGGER.info("Registered ModBlocks");
    }
}
