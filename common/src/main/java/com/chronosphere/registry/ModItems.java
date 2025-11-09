package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.items.PortalStabilizerItem;
import com.chronosphere.items.TimeHourglassItem;
import com.chronosphere.items.base.ClockstoneItem;
import com.chronosphere.items.base.EnhancedClockstoneItem;
import com.chronosphere.items.consumables.FruitOfTimeItem;
import com.chronosphere.items.consumables.TimeFruitPieItem;
import com.chronosphere.items.consumables.TimeJamItem;
import com.chronosphere.items.consumables.TimeWheatSeedsItem;
import com.chronosphere.items.consumables.TimeWheatItem;
import com.chronosphere.items.consumables.TimeBreadItem;
import com.chronosphere.items.tools.TimeClockItem;
import com.chronosphere.items.KeyToMasterClockItem;
import com.chronosphere.items.UnstableHourglassItem;
import com.chronosphere.items.tools.SpatiallyLinkedPickaxeItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/**
 * Architectury Registry wrapper for custom items.
 *
 * This class provides a centralized registry for all custom items in the Chronosphere mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Chronosphere.MOD_ID, Registries.ITEM);

    // === Block Items ===

    /**
     * Clockstone Ore - BlockItem for placing Clockstone Ore block.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_ORE = ITEMS.register(
        "clockstone_ore",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_ORE.get(), new Item.Properties())
    );

    /**
     * Clockstone Block - BlockItem for placing Clockstone Block (portal frame material).
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_BLOCK = ITEMS.register(
        "clockstone_block",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_BLOCK.get(), new Item.Properties())
    );

    /**
     * Reversing Time Sandstone - BlockItem for special block that auto-restores after 3 seconds.
     */
    public static final RegistrySupplier<Item> REVERSING_TIME_SANDSTONE = ITEMS.register(
        "reversing_time_sandstone",
        () -> new BlockItem(ModBlocks.REVERSING_TIME_SANDSTONE.get(), new Item.Properties())
    );

    /**
     * Unstable Fungus - BlockItem for special block that applies random speed effects.
     */
    public static final RegistrySupplier<Item> UNSTABLE_FUNGUS = ITEMS.register(
        "unstable_fungus",
        () -> new BlockItem(ModBlocks.UNSTABLE_FUNGUS.get(), new Item.Properties())
    );

    /**
     * Time Wood Log - BlockItem for placing Time Wood Log block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_LOG = ITEMS.register(
        "time_wood_log",
        () -> new BlockItem(ModBlocks.TIME_WOOD_LOG.get(), new Item.Properties())
    );

    /**
     * Time Wood Leaves - BlockItem for placing Time Wood Leaves block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_LEAVES = ITEMS.register(
        "time_wood_leaves",
        () -> new BlockItem(ModBlocks.TIME_WOOD_LEAVES.get(), new Item.Properties())
    );

    /**
     * Time Wood Planks - BlockItem for placing Time Wood Planks block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_PLANKS = ITEMS.register(
        "time_wood_planks",
        () -> new BlockItem(ModBlocks.TIME_WOOD_PLANKS.get(), new Item.Properties())
    );

    /**
     * Time Wood Sapling - BlockItem for placing Time Wood Sapling block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_SAPLING = ITEMS.register(
        "time_wood_sapling",
        () -> new BlockItem(ModBlocks.TIME_WOOD_SAPLING.get(), new Item.Properties())
    );

    /**
     * Time Wheat Bale - BlockItem for placing Time Wheat Bale block.
     */
    public static final RegistrySupplier<Item> TIME_WHEAT_BALE = ITEMS.register(
        "time_wheat_bale",
        () -> new BlockItem(ModBlocks.TIME_WHEAT_BALE.get(), new Item.Properties())
    );

    /**
     * Clock Tower Teleporter - BlockItem for Desert Clock Tower teleporter.
     * Requires 3 seconds of charging (holding right-click) before teleporting.
     * UP direction: 4th floor → 5th floor (boss room).
     * DOWN direction: 5th floor → 4th floor (appears after defeating Time Guardian).
     */
    public static final RegistrySupplier<Item> CLOCK_TOWER_TELEPORTER = ITEMS.register(
        "clock_tower_teleporter",
        () -> new BlockItem(ModBlocks.CLOCK_TOWER_TELEPORTER.get(), new Item.Properties())
    );

    /**
     * Clockwork Block - Decorative block with animated rotating gears theme.
     */
    public static final RegistrySupplier<Item> CLOCKWORK_BLOCK = ITEMS.register(
        "clockwork_block",
        () -> new BlockItem(ModBlocks.CLOCKWORK_BLOCK.get(), new Item.Properties())
    );

    /**
     * Time Crystal Block - Decorative block that emits light level 10.
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL_BLOCK = ITEMS.register(
        "time_crystal_block",
        () -> new BlockItem(ModBlocks.TIME_CRYSTAL_BLOCK.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks - Building block crafted from Clockstone.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS = ITEMS.register(
        "temporal_bricks",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks Stairs - Stair variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_STAIRS = ITEMS.register(
        "temporal_bricks_stairs",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_STAIRS.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks Slab - Slab variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_SLAB = ITEMS.register(
        "temporal_bricks_slab",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_SLAB.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks Wall - Wall variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_WALL = ITEMS.register(
        "temporal_bricks_wall",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_WALL.get(), new Item.Properties())
    );

    /**
     * Time Wood Stairs - Stair variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_STAIRS = ITEMS.register(
        "time_wood_stairs",
        () -> new BlockItem(ModBlocks.TIME_WOOD_STAIRS.get(), new Item.Properties())
    );

    /**
     * Time Wood Slab - Slab variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_SLAB = ITEMS.register(
        "time_wood_slab",
        () -> new BlockItem(ModBlocks.TIME_WOOD_SLAB.get(), new Item.Properties())
    );

    /**
     * Time Wood Fence - Fence variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_FENCE = ITEMS.register(
        "time_wood_fence",
        () -> new BlockItem(ModBlocks.TIME_WOOD_FENCE.get(), new Item.Properties())
    );

    /**
     * Temporal Moss - Decorative moss block exclusive to swamp biome.
     */
    public static final RegistrySupplier<Item> TEMPORAL_MOSS = ITEMS.register(
        "temporal_moss",
        () -> new BlockItem(ModBlocks.TEMPORAL_MOSS.get(), new Item.Properties())
    );

    /**
     * Frozen Time Ice - Special ice block exclusive to snowy biome.
     */
    public static final RegistrySupplier<Item> FROZEN_TIME_ICE = ITEMS.register(
        "frozen_time_ice",
        () -> new BlockItem(ModBlocks.FROZEN_TIME_ICE.get(), new Item.Properties())
    );

    // === Material Items ===

    /**
     * Clockstone - Base material obtained from Clockstone Ore.
     * Used for crafting Time Hourglass and other time-related items.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE = ITEMS.register(
        "clockstone",
        () -> new ClockstoneItem(ClockstoneItem.createProperties())
    );

    /**
     * Enhanced Clockstone - Advanced material obtained from Desert Clock Tower.
     * Used for crafting time manipulation items (Time Clock, Spatially Linked Pickaxe, Unstable Hourglass).
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE = ITEMS.register(
        "enhanced_clockstone",
        () -> new EnhancedClockstoneItem(EnhancedClockstoneItem.createProperties())
    );

    /**
     * Unstable Hourglass - Material item with crafting risk.
     * WARNING: Crafting triggers Reversed Resonance (Slowness IV on player, Speed II on mobs).
     * Used as crafting material for ultimate artifacts.
     */
    public static final RegistrySupplier<Item> UNSTABLE_HOURGLASS = ITEMS.register(
        "unstable_hourglass",
        () -> new UnstableHourglassItem(UnstableHourglassItem.createProperties())
    );

    // === Portal Items ===

    /**
     * Time Hourglass - Portal ignition item.
     * Single-use item for activating Chronosphere portals.
     */
    public static final RegistrySupplier<Item> TIME_HOURGLASS = ITEMS.register(
        "time_hourglass",
        () -> new TimeHourglassItem(TimeHourglassItem.createProperties())
    );

    /**
     * Portal Stabilizer - Portal utility item.
     * Single-use item for stabilizing deactivated portals.
     */
    public static final RegistrySupplier<Item> PORTAL_STABILIZER = ITEMS.register(
        "portal_stabilizer",
        () -> new PortalStabilizerItem(PortalStabilizerItem.createProperties())
    );

    // === Consumables ===

    /**
     * Fruit of Time - Food item found in Chronosphere dimension.
     * Restores hunger and grants Haste I effect for 30 seconds.
     */
    public static final RegistrySupplier<Item> FRUIT_OF_TIME = ITEMS.register(
        "fruit_of_time",
        () -> new FruitOfTimeItem(FruitOfTimeItem.createProperties())
    );

    /**
     * Time Fruit Pie - Crafted food item from Time Fruits and wheat.
     * Restores 8 hunger and grants Haste II effect for 30 seconds.
     */
    public static final RegistrySupplier<Item> TIME_FRUIT_PIE = ITEMS.register(
        "time_fruit_pie",
        () -> new TimeFruitPieItem(TimeFruitPieItem.createProperties())
    );

    /**
     * Time Jam - Crafted food item from Time Fruits and sugar.
     * Restores 4 hunger and grants Speed I effect for 60 seconds.
     */
    public static final RegistrySupplier<Item> TIME_JAM = ITEMS.register(
        "time_jam",
        () -> new TimeJamItem(TimeJamItem.createProperties())
    );

    /**
     * Time Wheat Seeds - Seeds for planting Time Wheat crops.
     * Can be planted on farmland to grow Time Wheat.
     */
    public static final RegistrySupplier<Item> TIME_WHEAT_SEEDS = ITEMS.register(
        "time_wheat_seeds",
        () -> new TimeWheatSeedsItem(TimeWheatSeedsItem.createProperties())
    );

    /**
     * Time Wheat - Harvested crop item from mature Time Wheat.
     * Used for crafting Time Bread.
     */
    public static final RegistrySupplier<Item> TIME_WHEAT = ITEMS.register(
        "time_wheat",
        () -> new TimeWheatItem(TimeWheatItem.createProperties())
    );

    /**
     * Time Bread - Basic crafted food item from Time Wheat.
     * Restores 5 hunger points.
     */
    public static final RegistrySupplier<Item> TIME_BREAD = ITEMS.register(
        "time_bread",
        () -> new TimeBreadItem(TimeBreadItem.createProperties())
    );

    // === Tools ===

    /**
     * Time Clock - Utility item for cancelling mob attack AI.
     * When used, forcibly cancels the next attack AI routine of all mobs within 8 blocks.
     * Cooldown: 10 seconds.
     */
    public static final RegistrySupplier<Item> TIME_CLOCK = ITEMS.register(
        "time_clock",
        () -> new TimeClockItem(TimeClockItem.createProperties())
    );

    /**
     * Spatially Linked Pickaxe - Time manipulation mining tool.
     * Diamond-equivalent pickaxe with 33% chance to double drops on block break.
     */
    public static final RegistrySupplier<Item> SPATIALLY_LINKED_PICKAXE = ITEMS.register(
        "spatially_linked_pickaxe",
        () -> new SpatiallyLinkedPickaxeItem(SpatiallyLinkedPickaxeItem.createProperties())
    );

    // === Key Items ===

    /**
     * Key to Master Clock - Key item for accessing Master Clock depths.
     * Obtained from defeating Time Guardian (mini-boss).
     * Used to open the door at Master Clock entrance.
     */
    public static final RegistrySupplier<Item> KEY_TO_MASTER_CLOCK = ITEMS.register(
        "key_to_master_clock",
        () -> new KeyToMasterClockItem(KeyToMasterClockItem.createProperties())
    );

    /**
     * Initialize item registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        ITEMS.register();
        Chronosphere.LOGGER.info("Registered ModItems");
    }

    /**
     * Populate creative mode tab with all items.
     * Called by ModCreativeTabs to add items to the Chronosphere creative tab.
     */
    public static void populateCreativeTab(
            net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters params,
            net.minecraft.world.item.CreativeModeTab.Output output) {
        // === Blocks ===
        output.accept(CLOCKSTONE_ORE.get());
        output.accept(CLOCKSTONE_BLOCK.get());
        output.accept(REVERSING_TIME_SANDSTONE.get());
        output.accept(UNSTABLE_FUNGUS.get());
        output.accept(TIME_WOOD_LOG.get());
        output.accept(TIME_WOOD_LEAVES.get());
        output.accept(TIME_WOOD_PLANKS.get());
        output.accept(TIME_WOOD_STAIRS.get());
        output.accept(TIME_WOOD_SLAB.get());
        output.accept(TIME_WOOD_FENCE.get());
        output.accept(TIME_WOOD_SAPLING.get());
        output.accept(TIME_WHEAT_BALE.get());
        output.accept(CLOCK_TOWER_TELEPORTER.get());
        output.accept(CLOCKWORK_BLOCK.get());
        output.accept(TIME_CRYSTAL_BLOCK.get());
        output.accept(TEMPORAL_BRICKS.get());
        output.accept(TEMPORAL_BRICKS_STAIRS.get());
        output.accept(TEMPORAL_BRICKS_SLAB.get());
        output.accept(TEMPORAL_BRICKS_WALL.get());
        output.accept(TEMPORAL_MOSS.get());
        output.accept(FROZEN_TIME_ICE.get());

        // === Base Materials ===
        output.accept(CLOCKSTONE.get());
        output.accept(ENHANCED_CLOCKSTONE.get());
        output.accept(UNSTABLE_HOURGLASS.get());

        // === Portal Items ===
        output.accept(TIME_HOURGLASS.get());
        output.accept(PORTAL_STABILIZER.get());

        // === Consumables ===
        output.accept(FRUIT_OF_TIME.get());
        output.accept(TIME_FRUIT_PIE.get());
        output.accept(TIME_JAM.get());
        output.accept(TIME_WHEAT_SEEDS.get());
        output.accept(TIME_WHEAT.get());
        output.accept(TIME_BREAD.get());

        // === Tools ===
        output.accept(TIME_CLOCK.get());
        output.accept(SPATIALLY_LINKED_PICKAXE.get());

        // === Key Items ===
        output.accept(KEY_TO_MASTER_CLOCK.get());
    }
}
