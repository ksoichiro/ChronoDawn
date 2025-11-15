package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.items.PortalStabilizerItem;
import com.chronosphere.items.TimeHourglassItem;
import com.chronosphere.items.base.ClockstoneItem;
import com.chronosphere.items.base.EnhancedClockstoneItem;
import com.chronosphere.items.base.TimeCrystalItem;
import com.chronosphere.items.combat.TimeArrowItem;
import com.chronosphere.items.consumables.FruitOfTimeItem;
import com.chronosphere.items.consumables.TimeFruitPieItem;
import com.chronosphere.items.consumables.TimeJamItem;
import com.chronosphere.items.consumables.TimeWheatSeedsItem;
import com.chronosphere.items.consumables.TimeWheatItem;
import com.chronosphere.items.consumables.TimeBreadItem;
import com.chronosphere.items.equipment.ClockstoneArmorItem;
import com.chronosphere.items.equipment.ClockstoneAxeItem;
import com.chronosphere.items.equipment.ClockstoneHoeItem;
import com.chronosphere.items.equipment.ClockstonePickaxeItem;
import com.chronosphere.items.equipment.ClockstoneShovelItem;
import com.chronosphere.items.equipment.ClockstoneSwordItem;
import com.chronosphere.items.equipment.EnhancedClockstoneArmorItem;
import com.chronosphere.items.equipment.EnhancedClockstoneAxeItem;
import com.chronosphere.items.equipment.EnhancedClockstoneHoeItem;
import com.chronosphere.items.equipment.EnhancedClockstonePickaxeItem;
import com.chronosphere.items.equipment.EnhancedClockstoneShovelItem;
import com.chronosphere.items.equipment.EnhancedClockstoneSwordItem;
import com.chronosphere.items.artifacts.ChronobladeItem;
import com.chronosphere.items.artifacts.TimeTyrantMailItem;
import com.chronosphere.items.artifacts.EchoingTimeBootsItem;
import com.chronosphere.items.artifacts.UnstablePocketWatchItem;
import com.chronosphere.items.tools.TimeClockItem;
import net.minecraft.world.item.ArmorItem;
import com.chronosphere.items.KeyToMasterClockItem;
import com.chronosphere.items.UnstableHourglassItem;
import com.chronosphere.items.quest.AncientGearItem;
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
     * Clockstone Stairs - BlockItem for decorative stair variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_STAIRS = ITEMS.register(
        "clockstone_stairs",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_STAIRS.get(), new Item.Properties())
    );

    /**
     * Clockstone Slab - BlockItem for decorative slab variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_SLAB = ITEMS.register(
        "clockstone_slab",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_SLAB.get(), new Item.Properties())
    );

    /**
     * Clockstone Wall - BlockItem for decorative wall variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_WALL = ITEMS.register(
        "clockstone_wall",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_WALL.get(), new Item.Properties())
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
     * Time Wood Door - Wooden door that can be opened/closed.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_DOOR = ITEMS.register(
        "time_wood_door",
        () -> new BlockItem(ModBlocks.TIME_WOOD_DOOR.get(), new Item.Properties())
    );

    /**
     * Time Wood Trapdoor - Wooden trapdoor that can be opened/closed.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_TRAPDOOR = ITEMS.register(
        "time_wood_trapdoor",
        () -> new BlockItem(ModBlocks.TIME_WOOD_TRAPDOOR.get(), new Item.Properties())
    );

    /**
     * Time Wood Fence Gate - Fence gate that connects to fences.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_FENCE_GATE = ITEMS.register(
        "time_wood_fence_gate",
        () -> new BlockItem(ModBlocks.TIME_WOOD_FENCE_GATE.get(), new Item.Properties())
    );

    /**
     * Time Wood Button - Wooden button that emits redstone signal.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_BUTTON = ITEMS.register(
        "time_wood_button",
        () -> new BlockItem(ModBlocks.TIME_WOOD_BUTTON.get(), new Item.Properties())
    );

    /**
     * Time Wood Pressure Plate - Wooden pressure plate that emits redstone signal.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_PRESSURE_PLATE = ITEMS.register(
        "time_wood_pressure_plate",
        () -> new BlockItem(ModBlocks.TIME_WOOD_PRESSURE_PLATE.get(), new Item.Properties())
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

    /**
     * Boss Room Door - Custom iron door item with BlockEntity for NBT data storage.
     * Identical appearance to vanilla iron door but can differentiate between entrance and boss room doors.
     */
    public static final RegistrySupplier<Item> BOSS_ROOM_DOOR = ITEMS.register(
        "boss_room_door",
        () -> new BlockItem(ModBlocks.BOSS_ROOM_DOOR.get(), new Item.Properties())
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

    /**
     * Fragment of Stasis Core - Boss material item.
     * Dropped by Time Tyrant (3-5 per kill, affected by Looting).
     * Used for crafting ultimate artifacts (Chronoblade, Time Guardian's Mail, etc.).
     */
    public static final RegistrySupplier<Item> FRAGMENT_OF_STASIS_CORE = ITEMS.register(
        "fragment_of_stasis_core",
        () -> new com.chronosphere.items.base.FragmentOfStasisCoreItem(
            com.chronosphere.items.base.FragmentOfStasisCoreItem.createProperties()
        )
    );

    /**
     * Eye of Chronos - Ultimate artifact item.
     * Dropped by Time Tyrant (1 per kill, guaranteed).
     * Effect: Enhanced Time Distortion (Slowness V on hostile mobs in Chronosphere when in inventory).
     */
    public static final RegistrySupplier<Item> EYE_OF_CHRONOS = ITEMS.register(
        "eye_of_chronos",
        () -> new com.chronosphere.items.artifacts.EyeOfChronosItem(
            com.chronosphere.items.artifacts.EyeOfChronosItem.createProperties()
        )
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

    // === Tier 2 Equipment - Weapons ===

    /**
     * Enhanced Clockstone Sword - Tier 2 time-themed weapon with freeze effect.
     * Advanced tier weapon comparable to diamond, with 25% chance to freeze enemy on hit.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_SWORD = ITEMS.register(
        "enhanced_clockstone_sword",
        () -> new EnhancedClockstoneSwordItem(EnhancedClockstoneSwordItem.createProperties())
    );

    // === Tier 2 Equipment - Tools ===

    /**
     * Enhanced Clockstone Pickaxe - Tier 2 time-themed mining tool.
     * Advanced tier tool comparable to diamond, with faster mining speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_PICKAXE = ITEMS.register(
        "enhanced_clockstone_pickaxe",
        () -> new EnhancedClockstonePickaxeItem(EnhancedClockstonePickaxeItem.createProperties())
    );

    /**
     * Enhanced Clockstone Axe - Tier 2 time-themed woodcutting tool.
     * Advanced tier tool comparable to diamond, with faster mining speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_AXE = ITEMS.register(
        "enhanced_clockstone_axe",
        () -> new EnhancedClockstoneAxeItem(EnhancedClockstoneAxeItem.createProperties())
    );

    /**
     * Enhanced Clockstone Shovel - Tier 2 time-themed digging tool.
     * Advanced tier tool comparable to diamond, with faster mining speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_SHOVEL = ITEMS.register(
        "enhanced_clockstone_shovel",
        () -> new EnhancedClockstoneShovelItem(EnhancedClockstoneShovelItem.createProperties())
    );

    /**
     * Enhanced Clockstone Hoe - Tier 2 time-themed farming tool.
     * Advanced tier tool comparable to diamond, with faster tilling speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_HOE = ITEMS.register(
        "enhanced_clockstone_hoe",
        () -> new EnhancedClockstoneHoeItem(EnhancedClockstoneHoeItem.createProperties())
    );

    // === Ultimate Weapons ===

    /**
     * Chronoblade - Ultimate time-manipulating weapon.
     * Crafted from fragments of defeated Time Tyrant.
     * 25% chance to skip enemy's next attack AI on hit.
     */
    public static final RegistrySupplier<Item> CHRONOBLADE = ITEMS.register(
        "chronoblade",
        () -> new ChronobladeItem(ChronobladeItem.createProperties())
    );

    // === Ultimate Armor ===

    /**
     * Time Tyrant's Mail - Ultimate chestplate with rollback effect.
     * 20% chance to rollback to previous state when receiving lethal damage.
     */
    public static final RegistrySupplier<Item> TIME_TYRANT_MAIL = ITEMS.register(
        "time_tyrant_mail",
        () -> new TimeTyrantMailItem(TimeTyrantMailItem.createProperties())
    );

    /**
     * Echoing Time Boots - Ultimate boots with decoy summoning.
     * Summons decoy entity when sprinting (15s cooldown).
     */
    public static final RegistrySupplier<Item> ECHOING_TIME_BOOTS = ITEMS.register(
        "echoing_time_boots",
        () -> new EchoingTimeBootsItem(EchoingTimeBootsItem.createProperties())
    );

    // === Ultimate Utilities ===

    /**
     * Unstable Pocket Watch - Speed effect swapping utility.
     * Swaps speed effects between player and nearby mobs (30s cooldown).
     */
    public static final RegistrySupplier<Item> UNSTABLE_POCKET_WATCH = ITEMS.register(
        "unstable_pocket_watch",
        () -> new UnstablePocketWatchItem(UnstablePocketWatchItem.createProperties())
    );

    // === Tier 2 Equipment - Armor ===

    /**
     * Enhanced Clockstone Helmet - Tier 2 time-themed helmet.
     * Defense: 3, Durability: 308
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_HELMET = ITEMS.register(
        "enhanced_clockstone_helmet",
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.HELMET, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.HELMET))
    );

    /**
     * Enhanced Clockstone Chestplate - Tier 2 time-themed chestplate.
     * Defense: 7, Durability: 448
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_CHESTPLATE = ITEMS.register(
        "enhanced_clockstone_chestplate",
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.CHESTPLATE, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.CHESTPLATE))
    );

    /**
     * Enhanced Clockstone Leggings - Tier 2 time-themed leggings.
     * Defense: 6, Durability: 420
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_LEGGINGS = ITEMS.register(
        "enhanced_clockstone_leggings",
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.LEGGINGS, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.LEGGINGS))
    );

    /**
     * Enhanced Clockstone Boots - Tier 2 time-themed boots.
     * Defense: 3, Durability: 364
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_BOOTS = ITEMS.register(
        "enhanced_clockstone_boots",
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.BOOTS, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.BOOTS))
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

    // === Combat Items ===

    /**
     * Time Arrow - Special arrow for fighting Time Tyrant.
     * When hitting Time Tyrant, applies Slowness III, Weakness II, and Glowing effects.
     * Crafted from: Clockstone (top) + Fruit of Time (center) + Arrow (bottom) → 4x Time Arrow
     */
    public static final RegistrySupplier<Item> TIME_ARROW = ITEMS.register(
        "time_arrow",
        () -> new TimeArrowItem(TimeArrowItem.createProperties())
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
     * Ancient Gear - Quest item for progressive unlock in Master Clock dungeon.
     * Obtained from chests in Master Clock dungeon rooms.
     * Players must collect 3 Ancient Gears to unlock the boss room.
     */
    public static final RegistrySupplier<Item> ANCIENT_GEAR = ITEMS.register(
        "ancient_gear",
        () -> new AncientGearItem(AncientGearItem.createProperties())
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
        output.accept(CLOCKSTONE_STAIRS.get());
        output.accept(CLOCKSTONE_SLAB.get());
        output.accept(CLOCKSTONE_WALL.get());
        output.accept(REVERSING_TIME_SANDSTONE.get());
        output.accept(UNSTABLE_FUNGUS.get());
        output.accept(TIME_WOOD_LOG.get());
        output.accept(TIME_WOOD_LEAVES.get());
        output.accept(TIME_WOOD_PLANKS.get());
        output.accept(TIME_WOOD_STAIRS.get());
        output.accept(TIME_WOOD_SLAB.get());
        output.accept(TIME_WOOD_FENCE.get());
        output.accept(TIME_WOOD_DOOR.get());
        output.accept(TIME_WOOD_TRAPDOOR.get());
        output.accept(TIME_WOOD_FENCE_GATE.get());
        output.accept(TIME_WOOD_BUTTON.get());
        output.accept(TIME_WOOD_PRESSURE_PLATE.get());
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
        output.accept(TIME_CRYSTAL.get());
        output.accept(ENHANCED_CLOCKSTONE.get());
        output.accept(UNSTABLE_HOURGLASS.get());
        output.accept(FRAGMENT_OF_STASIS_CORE.get());
        output.accept(EYE_OF_CHRONOS.get());

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

        // === Tier 2 Equipment - Weapons ===
        output.accept(ENHANCED_CLOCKSTONE_SWORD.get());

        // === Tier 2 Equipment - Tools ===
        output.accept(ENHANCED_CLOCKSTONE_PICKAXE.get());
        output.accept(ENHANCED_CLOCKSTONE_AXE.get());
        output.accept(ENHANCED_CLOCKSTONE_SHOVEL.get());
        output.accept(ENHANCED_CLOCKSTONE_HOE.get());

        // === Tier 2 Equipment - Armor ===
        output.accept(ENHANCED_CLOCKSTONE_HELMET.get());
        output.accept(ENHANCED_CLOCKSTONE_CHESTPLATE.get());
        output.accept(ENHANCED_CLOCKSTONE_LEGGINGS.get());
        output.accept(ENHANCED_CLOCKSTONE_BOOTS.get());

        // === Ultimate Weapons ===
        output.accept(CHRONOBLADE.get());

        // === Ultimate Armor ===
        output.accept(TIME_TYRANT_MAIL.get());
        output.accept(ECHOING_TIME_BOOTS.get());

        // === Ultimate Utilities ===
        output.accept(UNSTABLE_POCKET_WATCH.get());

        // === Tools ===
        output.accept(TIME_CLOCK.get());
        output.accept(SPATIALLY_LINKED_PICKAXE.get());

        // === Combat Items ===
        output.accept(TIME_ARROW.get());

        // === Key Items ===
        output.accept(KEY_TO_MASTER_CLOCK.get());
        output.accept(ANCIENT_GEAR.get());
    }
}
