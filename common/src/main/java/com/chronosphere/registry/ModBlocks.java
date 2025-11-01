package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.blocks.ClockstoneBlock;
import com.chronosphere.blocks.ClockstoneOre;
import com.chronosphere.blocks.FruitOfTimeBlock;
import com.chronosphere.blocks.ReversingTimeSandstone;
import com.chronosphere.blocks.UnstableFungus;
import com.chronosphere.blocks.TimeWoodLog;
import com.chronosphere.blocks.TimeWoodLeaves;
import com.chronosphere.blocks.TimeWoodPlanks;
import com.chronosphere.blocks.TimeWoodSapling;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

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
     * Clockstone Block - Portal frame building material.
     * Crafted from 9x Clockstone items, used to construct portal frames.
     */
    public static final RegistrySupplier<Block> CLOCKSTONE_BLOCK = BLOCKS.register(
        "clockstone_block",
        () -> new ClockstoneBlock(ClockstoneBlock.createProperties())
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
     * Fruit of Time Block - Growing fruit attached to Time Wood Logs.
     * Has 3 growth stages (0-2) and drops Fruit of Time items when mature.
     * Similar to Cocoa blocks in vanilla Minecraft.
     */
    public static final RegistrySupplier<Block> FRUIT_OF_TIME_BLOCK = BLOCKS.register(
        "fruit_of_time",
        () -> new FruitOfTimeBlock(FruitOfTimeBlock.createProperties())
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
