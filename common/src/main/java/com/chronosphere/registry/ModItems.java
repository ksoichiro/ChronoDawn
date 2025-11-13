package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.items.PortalStabilizerItem;
import com.chronosphere.items.TimeHourglassItem;
import com.chronosphere.items.base.ClockstoneItem;
import com.chronosphere.items.base.EnhancedClockstoneItem;
import com.chronosphere.items.base.TimeCrystalItem;
import com.chronosphere.items.consumables.FruitOfTimeItem;
import com.chronosphere.items.equipment.ClockstoneArmorItem;
import com.chronosphere.items.equipment.ClockstoneAxeItem;
import com.chronosphere.items.equipment.ClockstoneHoeItem;
import com.chronosphere.items.equipment.ClockstonePickaxeItem;
import com.chronosphere.items.equipment.ClockstoneShovelItem;
import com.chronosphere.items.equipment.ClockstoneSwordItem;
import com.chronosphere.items.tools.TimeClockItem;
import net.minecraft.world.item.ArmorItem;
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
     * Time Crystal Ore - BlockItem for placing Time Crystal Ore block.
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL_ORE = ITEMS.register(
        "time_crystal_ore",
        () -> new BlockItem(ModBlocks.TIME_CRYSTAL_ORE.get(), new Item.Properties())
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
     * Clock Tower Teleporter - BlockItem for Desert Clock Tower teleporter.
     * Requires 3 seconds of charging (holding right-click) before teleporting.
     * UP direction: 4th floor → 5th floor (boss room).
     * DOWN direction: 5th floor → 4th floor (appears after defeating Time Guardian).
     */
    public static final RegistrySupplier<Item> CLOCK_TOWER_TELEPORTER = ITEMS.register(
        "clock_tower_teleporter",
        () -> new BlockItem(ModBlocks.CLOCK_TOWER_TELEPORTER.get(), new Item.Properties())
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
     * Time Crystal - Advanced material obtained from Time Crystal Ore.
     * Used for crafting Clockstone equipment (swords, tools, armor).
     * Rarer than Clockstone (spawns at Y: 0-48, vein size 3-5).
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL = ITEMS.register(
        "time_crystal",
        () -> new TimeCrystalItem(TimeCrystalItem.createProperties())
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

    // === Equipment - Weapons ===

    /**
     * Clockstone Sword - Tier 1 time-themed weapon.
     * Basic tier weapon, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_SWORD = ITEMS.register(
        "clockstone_sword",
        () -> new ClockstoneSwordItem(ClockstoneSwordItem.createProperties())
    );

    // === Equipment - Tools ===

    /**
     * Clockstone Pickaxe - Tier 1 time-themed mining tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_PICKAXE = ITEMS.register(
        "clockstone_pickaxe",
        () -> new ClockstonePickaxeItem(ClockstonePickaxeItem.createProperties())
    );

    /**
     * Clockstone Axe - Tier 1 time-themed woodcutting tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_AXE = ITEMS.register(
        "clockstone_axe",
        () -> new ClockstoneAxeItem(ClockstoneAxeItem.createProperties())
    );

    /**
     * Clockstone Shovel - Tier 1 time-themed digging tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_SHOVEL = ITEMS.register(
        "clockstone_shovel",
        () -> new ClockstoneShovelItem(ClockstoneShovelItem.createProperties())
    );

    /**
     * Clockstone Hoe - Tier 1 time-themed farming tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_HOE = ITEMS.register(
        "clockstone_hoe",
        () -> new ClockstoneHoeItem(ClockstoneHoeItem.createProperties())
    );

    // === Equipment - Armor ===

    /**
     * Clockstone Helmet - Tier 1 time-themed helmet.
     * Defense: 2, Durability: 165
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_HELMET = ITEMS.register(
        "clockstone_helmet",
        () -> new ClockstoneArmorItem(ArmorItem.Type.HELMET, ClockstoneArmorItem.createProperties(ArmorItem.Type.HELMET))
    );

    /**
     * Clockstone Chestplate - Tier 1 time-themed chestplate.
     * Defense: 6, Durability: 240
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_CHESTPLATE = ITEMS.register(
        "clockstone_chestplate",
        () -> new ClockstoneArmorItem(ArmorItem.Type.CHESTPLATE, ClockstoneArmorItem.createProperties(ArmorItem.Type.CHESTPLATE))
    );

    /**
     * Clockstone Leggings - Tier 1 time-themed leggings.
     * Defense: 5, Durability: 225
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_LEGGINGS = ITEMS.register(
        "clockstone_leggings",
        () -> new ClockstoneArmorItem(ArmorItem.Type.LEGGINGS, ClockstoneArmorItem.createProperties(ArmorItem.Type.LEGGINGS))
    );

    /**
     * Clockstone Boots - Tier 1 time-themed boots.
     * Defense: 2, Durability: 195
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_BOOTS = ITEMS.register(
        "clockstone_boots",
        () -> new ClockstoneArmorItem(ArmorItem.Type.BOOTS, ClockstoneArmorItem.createProperties(ArmorItem.Type.BOOTS))
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
        output.accept(TIME_CRYSTAL_ORE.get());
        output.accept(CLOCKSTONE_BLOCK.get());
        output.accept(REVERSING_TIME_SANDSTONE.get());
        output.accept(UNSTABLE_FUNGUS.get());
        output.accept(TIME_WOOD_LOG.get());
        output.accept(TIME_WOOD_LEAVES.get());
        output.accept(TIME_WOOD_PLANKS.get());
        output.accept(TIME_WOOD_SAPLING.get());
        output.accept(CLOCK_TOWER_TELEPORTER.get());

        // === Base Materials ===
        output.accept(CLOCKSTONE.get());
        output.accept(TIME_CRYSTAL.get());
        output.accept(ENHANCED_CLOCKSTONE.get());
        output.accept(UNSTABLE_HOURGLASS.get());

        // === Portal Items ===
        output.accept(TIME_HOURGLASS.get());
        output.accept(PORTAL_STABILIZER.get());

        // === Consumables ===
        output.accept(FRUIT_OF_TIME.get());

        // === Equipment - Weapons ===
        output.accept(CLOCKSTONE_SWORD.get());

        // === Equipment - Tools ===
        output.accept(CLOCKSTONE_PICKAXE.get());
        output.accept(CLOCKSTONE_AXE.get());
        output.accept(CLOCKSTONE_SHOVEL.get());
        output.accept(CLOCKSTONE_HOE.get());

        // === Equipment - Armor ===
        output.accept(CLOCKSTONE_HELMET.get());
        output.accept(CLOCKSTONE_CHESTPLATE.get());
        output.accept(CLOCKSTONE_LEGGINGS.get());
        output.accept(CLOCKSTONE_BOOTS.get());

        // === Tools ===
        output.accept(TIME_CLOCK.get());
        output.accept(SPATIALLY_LINKED_PICKAXE.get());

        // === Key Items ===
        output.accept(KEY_TO_MASTER_CLOCK.get());
    }
}
