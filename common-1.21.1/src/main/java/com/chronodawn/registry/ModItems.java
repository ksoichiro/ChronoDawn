package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.items.ChronicleBookItem;
import com.chronodawn.items.DecorativeWaterBucketItem;
import com.chronodawn.items.PortalStabilizerItem;
import com.chronodawn.items.TimeHourglassItem;
import com.chronodawn.items.base.ClockstoneItem;
import com.chronodawn.items.base.EnhancedClockstoneItem;
import com.chronodawn.items.base.TimeCrystalItem;
import com.chronodawn.items.combat.TimeArrowItem;
import com.chronodawn.items.consumables.FruitOfTimeItem;
import com.chronodawn.items.consumables.TimeFruitPieItem;
import com.chronodawn.items.consumables.TimeJamItem;
import com.chronodawn.items.consumables.TimeWheatSeedsItem;
import com.chronodawn.items.consumables.TimeWheatItem;
import com.chronodawn.items.consumables.TimeBreadItem;
import com.chronodawn.items.consumables.TemporalRootItem;
import com.chronodawn.items.consumables.BakedTemporalRootItem;
import com.chronodawn.items.consumables.ChronoMelonSliceItem;
import com.chronodawn.items.consumables.ChronoMelonSeedsItem;
import com.chronodawn.items.consumables.TimelessMushroomItem;
import com.chronodawn.items.consumables.TemporalRootStewItem;
import com.chronodawn.items.consumables.GlisteningChronoMelonItem;
import com.chronodawn.items.consumables.ChronoMelonJuiceItem;
import com.chronodawn.items.consumables.TimelessMushroomSoupItem;
import com.chronodawn.items.consumables.EnhancedTimeBreadItem;
import com.chronodawn.items.consumables.TimeWheatCookieItem;
import com.chronodawn.items.consumables.ClockworkCookieItem;
import com.chronodawn.items.consumables.GoldenTimeWheatItem;
import com.chronodawn.items.equipment.ClockstoneArmorItem;
import com.chronodawn.items.equipment.ClockstoneAxeItem;
import com.chronodawn.items.equipment.ClockstoneHoeItem;
import com.chronodawn.items.equipment.ClockstonePickaxeItem;
import com.chronodawn.items.equipment.ClockstoneShovelItem;
import com.chronodawn.items.equipment.ClockstoneSwordItem;
import com.chronodawn.items.equipment.EnhancedClockstoneArmorItem;
import com.chronodawn.items.equipment.EnhancedClockstoneAxeItem;
import com.chronodawn.items.equipment.EnhancedClockstoneHoeItem;
import com.chronodawn.items.equipment.EnhancedClockstonePickaxeItem;
import com.chronodawn.items.equipment.EnhancedClockstoneShovelItem;
import com.chronodawn.items.equipment.EnhancedClockstoneSwordItem;
import com.chronodawn.items.artifacts.ChronobladeItem;
import com.chronodawn.items.artifacts.TimeTyrantMailItem;
import com.chronodawn.items.artifacts.EchoingTimeBootsItem;
import com.chronodawn.items.artifacts.UnstablePocketWatchItem;
import com.chronodawn.items.tools.TimeClockItem;
import net.minecraft.world.item.ArmorItem;
import com.chronodawn.items.KeyToMasterClockItem;
import com.chronodawn.items.UnstableHourglassItem;
import com.chronodawn.items.quest.AncientGearItem;
import com.chronodawn.items.tools.SpatiallyLinkedPickaxeItem;
import com.chronodawn.items.boats.ChronoDawnBoatItem;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.chronodawn.registry.ModFluids;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import com.chronodawn.registry.ModItemId;
import com.chronodawn.registry.ModBlockId;

/**
 * Architectury Registry wrapper for custom items.
 *
 * This class provides a centralized registry for all custom items in the ChronoDawn mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ChronoDawn.MOD_ID, Registries.ITEM);

    // === Block Items ===

    /**
     * Clockstone Ore - BlockItem for placing Clockstone Ore block.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_ORE = ITEMS.register(
        ModItemId.CLOCKSTONE_ORE.id(),
        () -> new BlockItem(ModBlocks.CLOCKSTONE_ORE.get(), new Item.Properties())
    );

    /**
     * Time Crystal Ore - BlockItem for placing Time Crystal Ore block.
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL_ORE = ITEMS.register(
        ModItemId.TIME_CRYSTAL_ORE.id(),
        () -> new BlockItem(ModBlocks.TIME_CRYSTAL_ORE.get(), new Item.Properties())
    );

    /**
     * Clockstone Block - BlockItem for placing Clockstone Block (portal frame material).
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_BLOCK = ITEMS.register(
        ModItemId.CLOCKSTONE_BLOCK.id(),
        () -> new BlockItem(ModBlocks.CLOCKSTONE_BLOCK.get(), new Item.Properties())
    );

    /**
     * Clockstone Stairs - BlockItem for decorative stair variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_STAIRS = ITEMS.register(
        ModItemId.CLOCKSTONE_STAIRS.id(),
        () -> new BlockItem(ModBlocks.CLOCKSTONE_STAIRS.get(), new Item.Properties())
    );

    /**
     * Clockstone Slab - BlockItem for decorative slab variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_SLAB = ITEMS.register(
        ModItemId.CLOCKSTONE_SLAB.id(),
        () -> new BlockItem(ModBlocks.CLOCKSTONE_SLAB.get(), new Item.Properties())
    );

    /**
     * Clockstone Wall - BlockItem for decorative wall variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_WALL = ITEMS.register(
        ModItemId.CLOCKSTONE_WALL.id(),
        () -> new BlockItem(ModBlocks.CLOCKSTONE_WALL.get(), new Item.Properties())
    );

    /**
     * Reversing Time Sandstone - BlockItem for special block that auto-restores after 3 seconds.
     */
    public static final RegistrySupplier<Item> REVERSING_TIME_SANDSTONE = ITEMS.register(
        ModItemId.REVERSING_TIME_SANDSTONE.id(),
        () -> new BlockItem(ModBlocks.REVERSING_TIME_SANDSTONE.get(), new Item.Properties())
    );

    /**
     * Unstable Fungus - BlockItem for special block that applies random speed effects.
     */
    public static final RegistrySupplier<Item> UNSTABLE_FUNGUS = ITEMS.register(
        ModItemId.UNSTABLE_FUNGUS.id(),
        () -> new BlockItem(ModBlocks.UNSTABLE_FUNGUS.get(), new Item.Properties())
    );

    /**
     * Time Wood Log - BlockItem for placing Time Wood Log block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_LOG = ITEMS.register(
        ModItemId.TIME_WOOD_LOG.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_LOG.get(), new Item.Properties())
    );

    /**
     * Stripped Time Wood Log - BlockItem for placing Stripped Time Wood Log block.
     */
    public static final RegistrySupplier<Item> STRIPPED_TIME_WOOD_LOG = ITEMS.register(
        ModItemId.STRIPPED_TIME_WOOD_LOG.id(),
        () -> new BlockItem(ModBlocks.STRIPPED_TIME_WOOD_LOG.get(), new Item.Properties())
    );

    /**
     * Time Wood - BlockItem for placing Time Wood block (all-bark variant).
     */
    public static final RegistrySupplier<Item> TIME_WOOD = ITEMS.register(
        ModItemId.TIME_WOOD.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD.get(), new Item.Properties())
    );

    /**
     * Stripped Time Wood - BlockItem for placing Stripped Time Wood block.
     */
    public static final RegistrySupplier<Item> STRIPPED_TIME_WOOD = ITEMS.register(
        ModItemId.STRIPPED_TIME_WOOD.id(),
        () -> new BlockItem(ModBlocks.STRIPPED_TIME_WOOD.get(), new Item.Properties())
    );

    /**
     * Time Wood Leaves - BlockItem for placing Time Wood Leaves block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_LEAVES = ITEMS.register(
        ModItemId.TIME_WOOD_LEAVES.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_LEAVES.get(), new Item.Properties())
    );

    /**
     * Time Wood Planks - BlockItem for placing Time Wood Planks block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_PLANKS = ITEMS.register(
        ModItemId.TIME_WOOD_PLANKS.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_PLANKS.get(), new Item.Properties())
    );

    /**
     * Time Wood Sapling - BlockItem for placing Time Wood Sapling block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_SAPLING = ITEMS.register(
        ModItemId.TIME_WOOD_SAPLING.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_SAPLING.get(), new Item.Properties())
    );

    // === Dark Time Wood Block Items ===

    /**
     * Dark Time Wood Log - BlockItem for placing Dark Time Wood Log block.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_LOG = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_LOG.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_LOG.get(), new Item.Properties())
    );

    /**
     * Stripped Dark Time Wood Log - BlockItem for placing Stripped Dark Time Wood Log block.
     */
    public static final RegistrySupplier<Item> STRIPPED_DARK_TIME_WOOD_LOG = ITEMS.register(
        ModItemId.STRIPPED_DARK_TIME_WOOD_LOG.id(),
        () -> new BlockItem(ModBlocks.STRIPPED_DARK_TIME_WOOD_LOG.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood - BlockItem for placing Dark Time Wood block (all-bark variant).
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD = ITEMS.register(
        ModItemId.DARK_TIME_WOOD.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD.get(), new Item.Properties())
    );

    /**
     * Stripped Dark Time Wood - BlockItem for placing Stripped Dark Time Wood block.
     */
    public static final RegistrySupplier<Item> STRIPPED_DARK_TIME_WOOD = ITEMS.register(
        ModItemId.STRIPPED_DARK_TIME_WOOD.id(),
        () -> new BlockItem(ModBlocks.STRIPPED_DARK_TIME_WOOD.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Leaves - BlockItem for placing Dark Time Wood Leaves block.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_LEAVES = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_LEAVES.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_LEAVES.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Planks - BlockItem for placing Dark Time Wood Planks block.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_PLANKS = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_PLANKS.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_PLANKS.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Stairs - BlockItem for placing Dark Time Wood Stairs.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_STAIRS = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_STAIRS.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_STAIRS.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Slab - BlockItem for placing Dark Time Wood Slab.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_SLAB = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_SLAB.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_SLAB.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Fence - BlockItem for placing Dark Time Wood Fence.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_FENCE = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_FENCE.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_FENCE.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Door - BlockItem for placing Dark Time Wood Door.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_DOOR = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_DOOR.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_DOOR.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Trapdoor - BlockItem for placing Dark Time Wood Trapdoor.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_TRAPDOOR = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_TRAPDOOR.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_TRAPDOOR.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Fence Gate - BlockItem for placing Dark Time Wood Fence Gate.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_FENCE_GATE = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_FENCE_GATE.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_FENCE_GATE.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Button - BlockItem for placing Dark Time Wood Button.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_BUTTON = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_BUTTON.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_BUTTON.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Pressure Plate - BlockItem for placing Dark Time Wood Pressure Plate.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_PRESSURE_PLATE = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_PRESSURE_PLATE.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_PRESSURE_PLATE.get(), new Item.Properties())
    );

    /**
     * Dark Time Wood Sapling - BlockItem for placing Dark Time Wood Sapling.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_SAPLING = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_SAPLING.id(),
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_SAPLING.get(), new Item.Properties())
    );

    // === Ancient Time Wood Block Items ===

    /**
     * Ancient Time Wood Log - BlockItem for placing Ancient Time Wood Log block.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_LOG = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_LOG.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_LOG.get(), new Item.Properties())
    );

    /**
     * Stripped Ancient Time Wood Log - BlockItem for placing Stripped Ancient Time Wood Log block.
     */
    public static final RegistrySupplier<Item> STRIPPED_ANCIENT_TIME_WOOD_LOG = ITEMS.register(
        ModItemId.STRIPPED_ANCIENT_TIME_WOOD_LOG.id(),
        () -> new BlockItem(ModBlocks.STRIPPED_ANCIENT_TIME_WOOD_LOG.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood - BlockItem for placing Ancient Time Wood block (all-bark variant).
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD.get(), new Item.Properties())
    );

    /**
     * Stripped Ancient Time Wood - BlockItem for placing Stripped Ancient Time Wood block.
     */
    public static final RegistrySupplier<Item> STRIPPED_ANCIENT_TIME_WOOD = ITEMS.register(
        ModItemId.STRIPPED_ANCIENT_TIME_WOOD.id(),
        () -> new BlockItem(ModBlocks.STRIPPED_ANCIENT_TIME_WOOD.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Leaves - BlockItem for placing Ancient Time Wood Leaves block.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_LEAVES = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_LEAVES.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_LEAVES.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Planks - BlockItem for placing Ancient Time Wood Planks block.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_PLANKS = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_PLANKS.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_PLANKS.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Stairs - BlockItem for placing Ancient Time Wood Stairs.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_STAIRS = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_STAIRS.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_STAIRS.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Slab - BlockItem for placing Ancient Time Wood Slab.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_SLAB = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_SLAB.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_SLAB.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Fence - BlockItem for placing Ancient Time Wood Fence.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_FENCE = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_FENCE.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_FENCE.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Door - BlockItem for placing Ancient Time Wood Door.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_DOOR = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_DOOR.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_DOOR.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Trapdoor - BlockItem for placing Ancient Time Wood Trapdoor.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_TRAPDOOR = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_TRAPDOOR.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_TRAPDOOR.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Fence Gate - BlockItem for placing Ancient Time Wood Fence Gate.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_FENCE_GATE = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_FENCE_GATE.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_FENCE_GATE.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Button - BlockItem for placing Ancient Time Wood Button.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_BUTTON = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_BUTTON.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_BUTTON.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Pressure Plate - BlockItem for placing Ancient Time Wood Pressure Plate.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_PRESSURE_PLATE = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_PRESSURE_PLATE.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_PRESSURE_PLATE.get(), new Item.Properties())
    );

    /**
     * Ancient Time Wood Sapling - BlockItem for placing Ancient Time Wood Sapling.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_SAPLING = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_SAPLING.id(),
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_SAPLING.get(), new Item.Properties())
    );

    /**
     * Time Wheat Bale - BlockItem for placing Time Wheat Bale block.
     */
    public static final RegistrySupplier<Item> TIME_WHEAT_BALE = ITEMS.register(
        ModItemId.TIME_WHEAT_BALE.id(),
        () -> new BlockItem(ModBlocks.TIME_WHEAT_BALE.get(), new Item.Properties())
    );

    /**
     * Clock Tower Teleporter - BlockItem for Desert Clock Tower teleporter.
     * Requires 3 seconds of charging (holding right-click) before teleporting.
     * UP direction: 4th floor → 5th floor (boss room).
     * DOWN direction: 5th floor → 4th floor (appears after defeating Time Guardian).
     */
    public static final RegistrySupplier<Item> CLOCK_TOWER_TELEPORTER = ITEMS.register(
        ModItemId.CLOCK_TOWER_TELEPORTER.id(),
        () -> new BlockItem(ModBlocks.CLOCK_TOWER_TELEPORTER.get(), new Item.Properties())
    );

    /**
     * Clockwork Block - Decorative block with animated rotating gears theme.
     */
    public static final RegistrySupplier<Item> CLOCKWORK_BLOCK = ITEMS.register(
        ModItemId.CLOCKWORK_BLOCK.id(),
        () -> new BlockItem(ModBlocks.CLOCKWORK_BLOCK.get(), new Item.Properties())
    );

    /**
     * Time Crystal Block - Decorative block that emits light level 10.
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL_BLOCK = ITEMS.register(
        ModItemId.TIME_CRYSTAL_BLOCK.id(),
        () -> new BlockItem(ModBlocks.TIME_CRYSTAL_BLOCK.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks - Building block crafted from Clockstone.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS = ITEMS.register(
        ModItemId.TEMPORAL_BRICKS.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks Stairs - Stair variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_STAIRS = ITEMS.register(
        ModItemId.TEMPORAL_BRICKS_STAIRS.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_STAIRS.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks Slab - Slab variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_SLAB = ITEMS.register(
        ModItemId.TEMPORAL_BRICKS_SLAB.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_SLAB.get(), new Item.Properties())
    );

    /**
     * Temporal Bricks Wall - Wall variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_WALL = ITEMS.register(
        ModItemId.TEMPORAL_BRICKS_WALL.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_WALL.get(), new Item.Properties())
    );

    /**
     * Time Wood Stairs - Stair variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_STAIRS = ITEMS.register(
        ModItemId.TIME_WOOD_STAIRS.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_STAIRS.get(), new Item.Properties())
    );

    /**
     * Time Wood Slab - Slab variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_SLAB = ITEMS.register(
        ModItemId.TIME_WOOD_SLAB.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_SLAB.get(), new Item.Properties())
    );

    /**
     * Time Wood Fence - Fence variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_FENCE = ITEMS.register(
        ModItemId.TIME_WOOD_FENCE.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_FENCE.get(), new Item.Properties())
    );

    /**
     * Time Wood Door - Wooden door that can be opened/closed.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_DOOR = ITEMS.register(
        ModItemId.TIME_WOOD_DOOR.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_DOOR.get(), new Item.Properties())
    );

    /**
     * Time Wood Trapdoor - Wooden trapdoor that can be opened/closed.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_TRAPDOOR = ITEMS.register(
        ModItemId.TIME_WOOD_TRAPDOOR.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_TRAPDOOR.get(), new Item.Properties())
    );

    /**
     * Time Wood Fence Gate - Fence gate that connects to fences.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_FENCE_GATE = ITEMS.register(
        ModItemId.TIME_WOOD_FENCE_GATE.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_FENCE_GATE.get(), new Item.Properties())
    );

    /**
     * Time Wood Button - Wooden button that emits redstone signal.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_BUTTON = ITEMS.register(
        ModItemId.TIME_WOOD_BUTTON.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_BUTTON.get(), new Item.Properties())
    );

    /**
     * Time Wood Pressure Plate - Wooden pressure plate that emits redstone signal.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_PRESSURE_PLATE = ITEMS.register(
        ModItemId.TIME_WOOD_PRESSURE_PLATE.id(),
        () -> new BlockItem(ModBlocks.TIME_WOOD_PRESSURE_PLATE.get(), new Item.Properties())
    );

    /**
     * Temporal Moss - Decorative moss block exclusive to swamp biome.
     */
    public static final RegistrySupplier<Item> TEMPORAL_MOSS = ITEMS.register(
        ModItemId.TEMPORAL_MOSS.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_MOSS.get(), new Item.Properties())
    );

    /**
     * Frozen Time Ice - Special ice block exclusive to snowy biome.
     */
    public static final RegistrySupplier<Item> FROZEN_TIME_ICE = ITEMS.register(
        ModItemId.FROZEN_TIME_ICE.id(),
        () -> new BlockItem(ModBlocks.FROZEN_TIME_ICE.get(), new Item.Properties())
    );

    /**
     * Boss Room Door - Custom iron door item with BlockEntity for NBT data storage.
     * Identical appearance to vanilla iron door but can differentiate between entrance and boss room doors.
     */
    public static final RegistrySupplier<Item> BOSS_ROOM_DOOR = ITEMS.register(
        ModItemId.BOSS_ROOM_DOOR.id(),
        () -> new BlockItem(ModBlocks.BOSS_ROOM_DOOR.get(), new Item.Properties())
    );

    /**
     * Entropy Crypt Trapdoor - Triggers Entropy Keeper boss spawn when first opened.
     * Used in Entropy Crypt structure as the entrance to Vault (treasure room).
     * After boss spawns (ACTIVATED=true), functions as normal trapdoor.
     */
    public static final RegistrySupplier<Item> ENTROPY_CRYPT_TRAPDOOR = ITEMS.register(
        ModItemId.ENTROPY_CRYPT_TRAPDOOR.id(),
        () -> new BlockItem(ModBlocks.ENTROPY_CRYPT_TRAPDOOR.get(), new Item.Properties())
    );

    /**
     * Temporal Particle Emitter - Invisible block that emits time distortion particles.
     * Structure-only block, not added to creative tab.
     * Can be placed with commands for testing: /give @s chronodawn:temporal_particle_emitter
     */
    public static final RegistrySupplier<Item> TEMPORAL_PARTICLE_EMITTER = ITEMS.register(
        ModItemId.TEMPORAL_PARTICLE_EMITTER.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_PARTICLE_EMITTER.get(), new Item.Properties())
    );

    /**
     * Boss Room Boundary Marker - Structure editing tool for defining boss room protection areas.
     * Not added to creative tab - used only in structure editing with Structure Block.
     */
    public static final RegistrySupplier<Item> BOSS_ROOM_BOUNDARY_MARKER = ITEMS.register(
        ModItemId.BOSS_ROOM_BOUNDARY_MARKER.id(),
        () -> new BlockItem(ModBlocks.BOSS_ROOM_BOUNDARY_MARKER.get(), new Item.Properties())
    );

    // === Material Items ===

    /**
     * Clockstone - Base material obtained from Clockstone Ore.
     * Used for crafting Time Hourglass and other time-related items.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE = ITEMS.register(
        ModItemId.CLOCKSTONE.id(),
        () -> new ClockstoneItem(ClockstoneItem.createProperties())
    );

    /**
     * Time Crystal - Advanced material obtained from Time Crystal Ore.
     * Used for crafting Clockstone equipment (swords, tools, armor).
     * Rarer than Clockstone (spawns at Y: 0-48, vein size 3-5).
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL = ITEMS.register(
        ModItemId.TIME_CRYSTAL.id(),
        () -> new TimeCrystalItem(TimeCrystalItem.createProperties())
    );

    /**
     * Enhanced Clockstone - Advanced material obtained from Desert Clock Tower.
     * Used for crafting time manipulation items (Time Clock, Spatially Linked Pickaxe, Unstable Hourglass).
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE.id(),
        () -> new EnhancedClockstoneItem(EnhancedClockstoneItem.createProperties())
    );

    /**
     * Unstable Hourglass - Material item with crafting risk.
     * WARNING: Crafting triggers Reversed Resonance (Slowness IV on player, Speed II on mobs).
     * Used as crafting material for ultimate artifacts.
     */
    public static final RegistrySupplier<Item> UNSTABLE_HOURGLASS = ITEMS.register(
        ModItemId.UNSTABLE_HOURGLASS.id(),
        () -> new UnstableHourglassItem(UnstableHourglassItem.createProperties())
    );

    /**
     * Fragment of Stasis Core - Boss material item.
     * Dropped by Time Tyrant (3-5 per kill, affected by Looting).
     * Used for crafting ultimate artifacts (Chronoblade, Time Guardian's Mail, etc.).
     */
    public static final RegistrySupplier<Item> FRAGMENT_OF_STASIS_CORE = ITEMS.register(
        ModItemId.FRAGMENT_OF_STASIS_CORE.id(),
        () -> new com.chronodawn.items.base.FragmentOfStasisCoreItem(
            com.chronodawn.items.base.FragmentOfStasisCoreItem.createProperties()
        )
    );

    /**
     * Eye of Chronos - Ultimate artifact item.
     * Dropped by Time Tyrant (1 per kill, guaranteed).
     * Effect: Enhanced Time Distortion (Slowness V on hostile mobs in ChronoDawn when in inventory).
     */
    public static final RegistrySupplier<Item> EYE_OF_CHRONOS = ITEMS.register(
        ModItemId.EYE_OF_CHRONOS.id(),
        () -> new com.chronodawn.items.artifacts.EyeOfChronosItem(
            com.chronodawn.items.artifacts.EyeOfChronosItem.createProperties()
        )
    );

    /**
     * Guardian Stone - Additional Boss material item (T234-T238).
     * Dropped by Chronos Warden (1 per kill, guaranteed).
     * Used for crafting Chrono Aegis (Time Tyrant preparation item).
     */
    public static final RegistrySupplier<Item> GUARDIAN_STONE = ITEMS.register(
        ModItemId.GUARDIAN_STONE.id(),
        () -> new com.chronodawn.items.boss.GuardianStoneItem(
            com.chronodawn.items.boss.GuardianStoneItem.createProperties()
        )
    );

    /**
     * Colossus Gear - Additional Boss material item (T234-T238).
     * Dropped by Clockwork Colossus (1 per kill, guaranteed).
     * Used for crafting Chrono Aegis (Time Tyrant preparation item).
     */
    public static final RegistrySupplier<Item> COLOSSUS_GEAR = ITEMS.register(
        ModItemId.COLOSSUS_GEAR.id(),
        () -> new com.chronodawn.items.boss.ColossusGearItem(
            com.chronodawn.items.boss.ColossusGearItem.createProperties()
        )
    );

    /**
     * Phantom Essence - Boss drop from Temporal Phantom
     *
     * A spectral essence dropped by Temporal Phantom (Phase 2 mini-boss).
     * Dropped by Temporal Phantom (1-2 per kill).
     * Used for crafting Chrono Aegis (Time Tyrant preparation item).
     *
     * Reference: research.md (Boss 3: Temporal Phantom)
     * Task: T236 [Phase 2] Implement Temporal Phantom
     */
    public static final RegistrySupplier<Item> PHANTOM_ESSENCE = ITEMS.register(
        ModItemId.PHANTOM_ESSENCE.id(),
        () -> new com.chronodawn.items.PhantomEssenceItem(
            com.chronodawn.items.PhantomEssenceItem.createProperties()
        )
    );

    /**
     * Entropy Core - Boss drop from Entropy Keeper
     *
     * A corrupted core dropped by Entropy Keeper (Phase 2 mini-boss).
     * Dropped by Entropy Keeper (1-2 per kill).
     * Used for crafting Chrono Aegis (Time Tyrant preparation item).
     *
     * Reference: research.md (Boss 4: Entropy Keeper)
     * Task: T237 [Phase 2] Implement Entropy Keeper
     */
    public static final RegistrySupplier<Item> ENTROPY_CORE = ITEMS.register(
        ModItemId.ENTROPY_CORE.id(),
        () -> new com.chronodawn.items.EntropyCoreItem(
            com.chronodawn.items.EntropyCoreItem.createProperties()
        )
    );

    // === Ultimate Crafted Items ===

    /**
     * Chrono Aegis - Ultimate preparation item for Time Tyrant boss fight
     *
     * Crafted from 4 boss drop materials:
     * - Guardian Stone (Chronos Warden drop)
     * - Phantom Essence (Temporal Phantom drop)
     * - Colossus Gear (Clockwork Colossus drop)
     * - Entropy Core (Entropy Keeper drop)
     *
     * Provides 10-minute buff with Time Tyrant protection effects.
     *
     * Reference: research.md (Chrono Aegis System)
     * Task: T238 [US3] Implement Chrono Aegis system
     */
    public static final RegistrySupplier<Item> CHRONO_AEGIS = ITEMS.register(
        ModItemId.CHRONO_AEGIS.id(),
        () -> new com.chronodawn.items.ChronoAegisItem(
            new Item.Properties()
        )
    );

    // === Portal Items ===

    /**
     * Time Hourglass - Portal ignition item.
     * Single-use item for activating ChronoDawn portals.
     */
    public static final RegistrySupplier<Item> TIME_HOURGLASS = ITEMS.register(
        ModItemId.TIME_HOURGLASS.id(),
        () -> new TimeHourglassItem(TimeHourglassItem.createProperties())
    );

    /**
     * Portal Stabilizer - Portal utility item.
     * Single-use item for stabilizing deactivated portals.
     */
    public static final RegistrySupplier<Item> PORTAL_STABILIZER = ITEMS.register(
        ModItemId.PORTAL_STABILIZER.id(),
        () -> new PortalStabilizerItem(PortalStabilizerItem.createProperties())
    );

    /**
     * Time Compass - Structure locator compass.
     * Points to nearest key structure (Ancient Ruins, Desert Clock Tower, or Master Clock Tower).
     * Target structure stored in NBT. Obtained through Time Keeper trades.
     */
    public static final RegistrySupplier<Item> TIME_COMPASS = ITEMS.register(
        ModItemId.TIME_COMPASS.id(),
        () -> new com.chronodawn.items.TimeCompassItem(
            com.chronodawn.items.TimeCompassItem.createProperties()
        )
    );

    /**
     * Decorative Water Bucket - Structure building tool.
     * Places chronodawn:decorative_water instead of minecraft:water.
     * Used in NBT structure files to distinguish decorative water from Aquifer water.
     * Creative mode only - not obtainable in survival.
     */
    public static final RegistrySupplier<Item> DECORATIVE_WATER_BUCKET = ITEMS.register(
        ModItemId.DECORATIVE_WATER_BUCKET.id(),
        () -> new DecorativeWaterBucketItem(ModFluids.DECORATIVE_WATER.get(), new Item.Properties().stacksTo(1))
    );

    /**
     * Chronicle Book - Custom guidebook item.
     * Opens the Chronicle GUI when used, replacing Patchouli dependency.
     * Distributed via advancement grant_chronicle_book when entering ChronoDawn.
     */
    public static final RegistrySupplier<Item> CHRONICLE_BOOK = ITEMS.register(
        ModItemId.CHRONICLE_BOOK.id(),
        () -> new ChronicleBookItem(ChronicleBookItem.createProperties())
    );

    // === Consumables ===

    /**
     * Fruit of Time - Food item found in ChronoDawn dimension.
     * Restores hunger and grants Haste I effect for 30 seconds.
     */
    public static final RegistrySupplier<Item> FRUIT_OF_TIME = ITEMS.register(
        ModItemId.FRUIT_OF_TIME.id(),
        () -> new FruitOfTimeItem(FruitOfTimeItem.createProperties())
    );

    /**
     * Time Fruit Pie - Crafted food item from Time Fruits and wheat.
     * Restores 8 hunger and grants Haste II effect for 30 seconds.
     */
    public static final RegistrySupplier<Item> TIME_FRUIT_PIE = ITEMS.register(
        ModItemId.TIME_FRUIT_PIE.id(),
        () -> new TimeFruitPieItem(TimeFruitPieItem.createProperties())
    );

    /**
     * Time Jam - Crafted food item from Time Fruits and sugar.
     * Restores 4 hunger and grants Speed I effect for 60 seconds.
     */
    public static final RegistrySupplier<Item> TIME_JAM = ITEMS.register(
        ModItemId.TIME_JAM.id(),
        () -> new TimeJamItem(TimeJamItem.createProperties())
    );

    /**
     * Time Wheat Seeds - Seeds for planting Time Wheat crops.
     * Can be planted on farmland to grow Time Wheat.
     */
    public static final RegistrySupplier<Item> TIME_WHEAT_SEEDS = ITEMS.register(
        ModItemId.TIME_WHEAT_SEEDS.id(),
        () -> new TimeWheatSeedsItem(TimeWheatSeedsItem.createProperties())
    );

    /**
     * Time Wheat - Harvested crop item from mature Time Wheat.
     * Used for crafting Time Bread.
     */
    public static final RegistrySupplier<Item> TIME_WHEAT = ITEMS.register(
        ModItemId.TIME_WHEAT.id(),
        () -> new TimeWheatItem(TimeWheatItem.createProperties())
    );

    /**
     * Time Bread - Basic crafted food item from Time Wheat.
     * Restores 5 hunger points.
     */
    public static final RegistrySupplier<Item> TIME_BREAD = ITEMS.register(
        ModItemId.TIME_BREAD.id(),
        () -> new TimeBreadItem(TimeBreadItem.createProperties())
    );

    /**
     * Temporal Root - Root vegetable that can be planted or eaten.
     * Restores 3 hunger points (raw).
     * Can be planted on farmland like carrots.
     */
    public static final RegistrySupplier<Item> TEMPORAL_ROOT = ITEMS.register(
        ModItemId.TEMPORAL_ROOT.id(),
        () -> new TemporalRootItem(TemporalRootItem.createProperties())
    );

    /**
     * Baked Temporal Root - Cooked root vegetable with healing properties.
     * Restores 6 hunger points + Regeneration I for 5 seconds.
     * Crafted by smelting Temporal Root.
     */
    public static final RegistrySupplier<Item> BAKED_TEMPORAL_ROOT = ITEMS.register(
        ModItemId.BAKED_TEMPORAL_ROOT.id(),
        () -> new BakedTemporalRootItem(BakedTemporalRootItem.createProperties())
    );

    /**
     * Chrono Melon Slice - Basic food item from Chrono Melon.
     * Restores 2 hunger points.
     * Fast eating speed (1.6 seconds).
     */
    public static final RegistrySupplier<Item> CHRONO_MELON_SLICE = ITEMS.register(
        ModItemId.CHRONO_MELON_SLICE.id(),
        () -> new ChronoMelonSliceItem(ChronoMelonSliceItem.createProperties())
    );

    /**
     * Chrono Melon Seeds - Seeds for planting Chrono Melon Stems.
     * Can be planted on farmland to grow Chrono Melon.
     */
    public static final RegistrySupplier<Item> CHRONO_MELON_SEEDS = ITEMS.register(
        ModItemId.CHRONO_MELON_SEEDS.id(),
        () -> new ChronoMelonSeedsItem(ChronoMelonSeedsItem.createProperties())
    );

    /**
     * Chrono Melon Block Item - Full melon block item.
     * Can be placed as decoration or broken for 9 slices.
     */
    public static final RegistrySupplier<Item> CHRONO_MELON = ITEMS.register(
        ModItemId.CHRONO_MELON.id(),
        () -> new BlockItem(ModBlocks.CHRONO_MELON.get(), new Item.Properties())
    );

    /**
     * Timeless Mushroom - Edible mushroom that can be planted or eaten.
     * Restores 2 hunger points.
     * Can be planted in dark areas (light level 12 or less).
     */
    public static final RegistrySupplier<Item> TIMELESS_MUSHROOM = ITEMS.register(
        ModItemId.TIMELESS_MUSHROOM.id(),
        () -> new TimelessMushroomItem(TimelessMushroomItem.createProperties())
    );

    /**
     * Purple Time Blossom - Decorative flower with purple time-themed appearance.
     * Can be placed as a block or in flower pots.
     */
    public static final RegistrySupplier<Item> PURPLE_TIME_BLOSSOM = ITEMS.register(
        ModItemId.PURPLE_TIME_BLOSSOM.id(),
        () -> new BlockItem(ModBlocks.PURPLE_TIME_BLOSSOM.get(), new Item.Properties())
    );

    /**
     * Orange Time Blossom - Decorative flower with orange time-themed appearance.
     * Can be placed as a block or in flower pots.
     */
    public static final RegistrySupplier<Item> ORANGE_TIME_BLOSSOM = ITEMS.register(
        ModItemId.ORANGE_TIME_BLOSSOM.id(),
        () -> new BlockItem(ModBlocks.ORANGE_TIME_BLOSSOM.get(), new Item.Properties())
    );

    /**
     * Pink Time Blossom - Decorative flower with pink time-themed appearance.
     * Can be placed as a block or in flower pots.
     */
    public static final RegistrySupplier<Item> PINK_TIME_BLOSSOM = ITEMS.register(
        ModItemId.PINK_TIME_BLOSSOM.id(),
        () -> new BlockItem(ModBlocks.PINK_TIME_BLOSSOM.get(), new Item.Properties())
    );

    /**
     * Dawn Bell - Tall blue flower representing dawn/morning in the time cycle.
     * 2 blocks high, cannot be potted.
     */
    public static final RegistrySupplier<Item> DAWN_BELL = ITEMS.register(
        ModItemId.DAWN_BELL.id(),
        () -> new BlockItem(ModBlocks.DAWN_BELL.get(), new Item.Properties())
    );

    /**
     * Dusk Bell - Tall red flower representing dusk/evening in the time cycle.
     * 2 blocks high, cannot be potted.
     */
    public static final RegistrySupplier<Item> DUSK_BELL = ITEMS.register(
        ModItemId.DUSK_BELL.id(),
        () -> new BlockItem(ModBlocks.DUSK_BELL.get(), new Item.Properties())
    );

    /**
     * Temporal Root Stew - Hearty stew combining Baked Temporal Root and Timeless Mushroom.
     * Restores 8 hunger points with Regeneration II for 10 seconds.
     * Recipe: 1x Baked Temporal Root + 1x Timeless Mushroom + 1x Bowl
     */
    public static final RegistrySupplier<Item> TEMPORAL_ROOT_STEW = ITEMS.register(
        ModItemId.TEMPORAL_ROOT_STEW.id(),
        () -> new TemporalRootStewItem(TemporalRootStewItem.createProperties())
    );

    /**
     * Glistening Chrono Melon - Premium melon slice infused with gold.
     * Restores 2 hunger points with high saturation and Absorption I for 30 seconds.
     * Recipe: 1x Chrono Melon Slice + 8x Gold Nuggets
     */
    public static final RegistrySupplier<Item> GLISTENING_CHRONO_MELON = ITEMS.register(
        ModItemId.GLISTENING_CHRONO_MELON.id(),
        () -> new GlisteningChronoMelonItem(GlisteningChronoMelonItem.createProperties())
    );

    /**
     * Chrono Melon Juice - Drinkable beverage providing Speed effect.
     * Restores 4 hunger points with Speed I for 60 seconds.
     * Recipe: 4x Chrono Melon Slices + 1x Glass Bottle
     */
    public static final RegistrySupplier<Item> CHRONO_MELON_JUICE = ITEMS.register(
        ModItemId.CHRONO_MELON_JUICE.id(),
        () -> new ChronoMelonJuiceItem(ChronoMelonJuiceItem.createProperties())
    );

    /**
     * Timeless Mushroom Soup - Simple mushroom soup with Night Vision effect.
     * Restores 6 hunger points with Night Vision for 60 seconds.
     * Recipe: 2x Timeless Mushrooms + 1x Bowl
     */
    public static final RegistrySupplier<Item> TIMELESS_MUSHROOM_SOUP = ITEMS.register(
        ModItemId.TIMELESS_MUSHROOM_SOUP.id(),
        () -> new TimelessMushroomSoupItem(TimelessMushroomSoupItem.createProperties())
    );

    /**
     * Enhanced Time Bread - Improved Time Bread fortified with Temporal Root.
     * Restores 7 hunger points with Saturation I for 5 seconds.
     * Recipe: 3x Time Wheat + 1x Temporal Root
     */
    public static final RegistrySupplier<Item> ENHANCED_TIME_BREAD = ITEMS.register(
        ModItemId.ENHANCED_TIME_BREAD.id(),
        () -> new EnhancedTimeBreadItem(EnhancedTimeBreadItem.createProperties())
    );

    /**
     * Time Wheat Cookie - Simple cookie made from Time Wheat and cocoa beans.
     * Restores 2 hunger points. Fast-eating snack.
     * Recipe: 2x Time Wheat + 1x Cocoa Beans
     */
    public static final RegistrySupplier<Item> TIME_WHEAT_COOKIE = ITEMS.register(
        ModItemId.TIME_WHEAT_COOKIE.id(),
        () -> new TimeWheatCookieItem(TimeWheatCookieItem.createProperties())
    );

    /**
     * Clockwork Cookie - Time-themed cookie with defensive effects.
     * Restores 2 hunger points with Resistance I (30s) and Fire Resistance (30s).
     * Recipe: 2x Time Wheat + 1x Time Jam + 2x Clockwork Block → 4x Clockwork Cookie
     */
    public static final RegistrySupplier<Item> CLOCKWORK_COOKIE = ITEMS.register(
        ModItemId.CLOCKWORK_COOKIE.id(),
        () -> new ClockworkCookieItem(ClockworkCookieItem.createProperties())
    );

    /**
     * Golden Time Wheat - Time Wheat infused with gold, providing powerful effects.
     * Restores 4 hunger points with Regeneration II (10s) and Absorption II (2min).
     * Recipe: 1x Time Wheat + 8x Gold Ingots
     */
    public static final RegistrySupplier<Item> GOLDEN_TIME_WHEAT = ITEMS.register(
        ModItemId.GOLDEN_TIME_WHEAT.id(),
        () -> new GoldenTimeWheatItem(GoldenTimeWheatItem.createProperties())
    );

    // === Equipment - Weapons ===

    /**
     * Clockstone Sword - Tier 1 time-themed weapon.
     * Basic tier weapon, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_SWORD = ITEMS.register(
        ModItemId.CLOCKSTONE_SWORD.id(),
        () -> new ClockstoneSwordItem(ClockstoneSwordItem.createProperties())
    );

    // === Equipment - Tools ===

    /**
     * Clockstone Pickaxe - Tier 1 time-themed mining tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_PICKAXE = ITEMS.register(
        ModItemId.CLOCKSTONE_PICKAXE.id(),
        () -> new ClockstonePickaxeItem(ClockstonePickaxeItem.createProperties())
    );

    /**
     * Clockstone Axe - Tier 1 time-themed woodcutting tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_AXE = ITEMS.register(
        ModItemId.CLOCKSTONE_AXE.id(),
        () -> new ClockstoneAxeItem(ClockstoneAxeItem.createProperties())
    );

    /**
     * Clockstone Shovel - Tier 1 time-themed digging tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_SHOVEL = ITEMS.register(
        ModItemId.CLOCKSTONE_SHOVEL.id(),
        () -> new ClockstoneShovelItem(ClockstoneShovelItem.createProperties())
    );

    /**
     * Clockstone Hoe - Tier 1 time-themed farming tool.
     * Basic tier tool, slightly better than iron but below diamond.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_HOE = ITEMS.register(
        ModItemId.CLOCKSTONE_HOE.id(),
        () -> new ClockstoneHoeItem(ClockstoneHoeItem.createProperties())
    );

    // === Equipment - Armor ===

    /**
     * Clockstone Helmet - Tier 1 time-themed helmet.
     * Defense: 2, Durability: 165
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_HELMET = ITEMS.register(
        ModItemId.CLOCKSTONE_HELMET.id(),
        () -> new ClockstoneArmorItem(ArmorItem.Type.HELMET, ClockstoneArmorItem.createProperties(ArmorItem.Type.HELMET))
    );

    /**
     * Clockstone Chestplate - Tier 1 time-themed chestplate.
     * Defense: 6, Durability: 240
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_CHESTPLATE = ITEMS.register(
        ModItemId.CLOCKSTONE_CHESTPLATE.id(),
        () -> new ClockstoneArmorItem(ArmorItem.Type.CHESTPLATE, ClockstoneArmorItem.createProperties(ArmorItem.Type.CHESTPLATE))
    );

    /**
     * Clockstone Leggings - Tier 1 time-themed leggings.
     * Defense: 5, Durability: 225
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_LEGGINGS = ITEMS.register(
        ModItemId.CLOCKSTONE_LEGGINGS.id(),
        () -> new ClockstoneArmorItem(ArmorItem.Type.LEGGINGS, ClockstoneArmorItem.createProperties(ArmorItem.Type.LEGGINGS))
    );

    /**
     * Clockstone Boots - Tier 1 time-themed boots.
     * Defense: 2, Durability: 195
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_BOOTS = ITEMS.register(
        ModItemId.CLOCKSTONE_BOOTS.id(),
        () -> new ClockstoneArmorItem(ArmorItem.Type.BOOTS, ClockstoneArmorItem.createProperties(ArmorItem.Type.BOOTS))
    );

    // === Tier 2 Equipment - Weapons ===

    /**
     * Enhanced Clockstone Sword - Tier 2 time-themed weapon with freeze effect.
     * Advanced tier weapon comparable to diamond, with 25% chance to freeze enemy on hit.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_SWORD = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_SWORD.id(),
        () -> new EnhancedClockstoneSwordItem(EnhancedClockstoneSwordItem.createProperties())
    );

    // === Tier 2 Equipment - Tools ===

    /**
     * Enhanced Clockstone Pickaxe - Tier 2 time-themed mining tool.
     * Advanced tier tool comparable to diamond, with faster mining speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_PICKAXE = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_PICKAXE.id(),
        () -> new EnhancedClockstonePickaxeItem(EnhancedClockstonePickaxeItem.createProperties())
    );

    /**
     * Enhanced Clockstone Axe - Tier 2 time-themed woodcutting tool.
     * Advanced tier tool comparable to diamond, with faster mining speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_AXE = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_AXE.id(),
        () -> new EnhancedClockstoneAxeItem(EnhancedClockstoneAxeItem.createProperties())
    );

    /**
     * Enhanced Clockstone Shovel - Tier 2 time-themed digging tool.
     * Advanced tier tool comparable to diamond, with faster mining speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_SHOVEL = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_SHOVEL.id(),
        () -> new EnhancedClockstoneShovelItem(EnhancedClockstoneShovelItem.createProperties())
    );

    /**
     * Enhanced Clockstone Hoe - Tier 2 time-themed farming tool.
     * Advanced tier tool comparable to diamond, with faster tilling speed.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_HOE = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_HOE.id(),
        () -> new EnhancedClockstoneHoeItem(EnhancedClockstoneHoeItem.createProperties())
    );

    // === Ultimate Weapons ===

    /**
     * Chronoblade - Ultimate time-manipulating weapon.
     * Crafted from fragments of defeated Time Tyrant.
     * 25% chance to skip enemy's next attack AI on hit.
     */
    public static final RegistrySupplier<Item> CHRONOBLADE = ITEMS.register(
        ModItemId.CHRONOBLADE.id(),
        () -> new ChronobladeItem(ChronobladeItem.createProperties())
    );

    // === Ultimate Armor ===

    /**
     * Time Tyrant's Mail - Ultimate chestplate with rollback effect.
     * 20% chance to rollback to previous state when receiving lethal damage.
     */
    public static final RegistrySupplier<Item> TIME_TYRANT_MAIL = ITEMS.register(
        ModItemId.TIME_TYRANT_MAIL.id(),
        () -> new TimeTyrantMailItem(TimeTyrantMailItem.createProperties())
    );

    /**
     * Echoing Time Boots - Ultimate boots with decoy summoning.
     * Summons decoy entity when sprinting (15s cooldown).
     */
    public static final RegistrySupplier<Item> ECHOING_TIME_BOOTS = ITEMS.register(
        ModItemId.ECHOING_TIME_BOOTS.id(),
        () -> new EchoingTimeBootsItem(EchoingTimeBootsItem.createProperties())
    );

    // === Ultimate Utilities ===

    /**
     * Unstable Pocket Watch - Speed effect swapping utility.
     * Swaps speed effects between player and nearby mobs (30s cooldown).
     */
    public static final RegistrySupplier<Item> UNSTABLE_POCKET_WATCH = ITEMS.register(
        ModItemId.UNSTABLE_POCKET_WATCH.id(),
        () -> new UnstablePocketWatchItem(UnstablePocketWatchItem.createProperties())
    );

    // === Tier 2 Equipment - Armor ===

    /**
     * Enhanced Clockstone Helmet - Tier 2 time-themed helmet.
     * Defense: 3, Durability: 308
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_HELMET = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_HELMET.id(),
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.HELMET, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.HELMET))
    );

    /**
     * Enhanced Clockstone Chestplate - Tier 2 time-themed chestplate.
     * Defense: 7, Durability: 448
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_CHESTPLATE = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_CHESTPLATE.id(),
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.CHESTPLATE, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.CHESTPLATE))
    );

    /**
     * Enhanced Clockstone Leggings - Tier 2 time-themed leggings.
     * Defense: 6, Durability: 420
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_LEGGINGS = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_LEGGINGS.id(),
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.LEGGINGS, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.LEGGINGS))
    );

    /**
     * Enhanced Clockstone Boots - Tier 2 time-themed boots.
     * Defense: 3, Durability: 364
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_BOOTS = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_BOOTS.id(),
        () -> new EnhancedClockstoneArmorItem(ArmorItem.Type.BOOTS, EnhancedClockstoneArmorItem.createProperties(ArmorItem.Type.BOOTS))
    );

    // === Tools ===

    /**
     * Time Clock - Utility item for cancelling mob attack AI.
     * When used, forcibly cancels the next attack AI routine of all mobs within 8 blocks.
     * Cooldown: 10 seconds.
     */
    public static final RegistrySupplier<Item> TIME_CLOCK = ITEMS.register(
        ModItemId.TIME_CLOCK.id(),
        () -> new TimeClockItem(TimeClockItem.createProperties())
    );

    /**
     * Spatially Linked Pickaxe - Time manipulation mining tool.
     * Diamond-equivalent pickaxe with 33% chance to double drops on block break.
     */
    public static final RegistrySupplier<Item> SPATIALLY_LINKED_PICKAXE = ITEMS.register(
        ModItemId.SPATIALLY_LINKED_PICKAXE.id(),
        () -> new SpatiallyLinkedPickaxeItem(SpatiallyLinkedPickaxeItem.createProperties())
    );

    // === Combat Items ===

    /**
     * Time Arrow - Special arrow for fighting Time Tyrant.
     * When hitting Time Tyrant, applies Slowness III, Weakness II, and Glowing effects.
     * Crafted from: Clockstone (top) + Fruit of Time (center) + Arrow (bottom) → 4x Time Arrow
     */
    public static final RegistrySupplier<Item> TIME_ARROW = ITEMS.register(
        ModItemId.TIME_ARROW.id(),
        () -> new TimeArrowItem(TimeArrowItem.createProperties())
    );

    // === Key Items ===

    /**
     * Key to Master Clock - Key item for accessing Master Clock depths.
     * Obtained from defeating Time Guardian (mini-boss).
     * Used to open the door at Master Clock entrance.
     */
    public static final RegistrySupplier<Item> KEY_TO_MASTER_CLOCK = ITEMS.register(
        ModItemId.KEY_TO_MASTER_CLOCK.id(),
        () -> new KeyToMasterClockItem(KeyToMasterClockItem.createProperties())
    );

    /**
     * Ancient Gear - Quest item for progressive unlock in Master Clock dungeon.
     * Obtained from chests in Master Clock dungeon rooms.
     * Players must collect 3 Ancient Gears to unlock the boss room.
     */
    public static final RegistrySupplier<Item> ANCIENT_GEAR = ITEMS.register(
        ModItemId.ANCIENT_GEAR.id(),
        () -> new AncientGearItem(AncientGearItem.createProperties())
    );

    // === Mob Drops ===

    /**
     * Glide Fish - Food item dropped by Glide Fish.
     * Nutrition: 2, Saturation: 0.1
     */
    public static final RegistrySupplier<Item> GLIDE_FISH = ITEMS.register(
        ModItemId.GLIDE_FISH.id(),
        () -> new Item(new Item.Properties().food(
            new net.minecraft.world.food.FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.1f)
                .build()
        ))
    );

    /**
     * Cooked Glide Fish - Cooked version of Glide Fish.
     * Nutrition: 5, Saturation: 0.6 (equivalent to cooked cod).
     */
    public static final RegistrySupplier<Item> COOKED_GLIDE_FISH = ITEMS.register(
        ModItemId.COOKED_GLIDE_FISH.id(),
        () -> new Item(new Item.Properties().food(
            new net.minecraft.world.food.FoodProperties.Builder()
                .nutrition(5)
                .saturationModifier(0.6f)
                .build()
        ))
    );

    // === Spawn Eggs ===

    /**
     * Temporal Wraith Spawn Egg - For creative mode and debugging.
     * Primary color: Dark purple (0x4B0082) - Indigo (background)
     * Secondary color: Light cyan (0xADD8E6) - Light blue (spots)
     */
    public static final RegistrySupplier<Item> TEMPORAL_WRAITH_SPAWN_EGG = ITEMS.register(
        ModItemId.TEMPORAL_WRAITH_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TEMPORAL_WRAITH,
            0x4B0082, // Background: Dark purple (indigo)
            0xADD8E6, // Spots: Light cyan
            new Item.Properties()
        )
    );

    /**
     * Clockwork Sentinel Spawn Egg - For creative mode and debugging.
     * Primary color: Royal blue (0x4169E1) - Background
     * Secondary color: Gold (0xFFD700) - Spots
     */
    public static final RegistrySupplier<Item> CLOCKWORK_SENTINEL_SPAWN_EGG = ITEMS.register(
        ModItemId.CLOCKWORK_SENTINEL_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CLOCKWORK_SENTINEL,
            0x4169E1, // Background: Royal blue
            0xFFD700, // Spots: Gold
            new Item.Properties()
        )
    );

    /**
     * Time Keeper Spawn Egg - For creative mode and debugging.
     * Primary color: Dark slate blue (0x483D8B) - Background
     * Secondary color: White (0xF5F5F5) - Spots
     */
    public static final RegistrySupplier<Item> TIME_KEEPER_SPAWN_EGG = ITEMS.register(
        ModItemId.TIME_KEEPER_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIME_KEEPER,
            0x483D8B, // Background: Dark slate blue
            0xF5F5F5, // Spots: Off-white
            new Item.Properties()
        )
    );

    // === Boss Spawn Eggs (Command-only, not in creative tab) ===

    /**
     * Time Guardian Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Gold (0xFFD700) - Background
     * Secondary color: Royal blue (0x4169E1) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> TIME_GUARDIAN_SPAWN_EGG = ITEMS.register(
        ModItemId.TIME_GUARDIAN_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIME_GUARDIAN,
            0xFFD700, // Background: Gold
            0x4169E1, // Spots: Royal blue
            new Item.Properties()
        )
    );

    /**
     * Time Tyrant Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Dark red (0x8B0000) - Background
     * Secondary color: Gold (0xFFD700) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> TIME_TYRANT_SPAWN_EGG = ITEMS.register(
        ModItemId.TIME_TYRANT_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIME_TYRANT,
            0x8B0000, // Background: Dark red
            0xFFD700, // Spots: Gold
            new Item.Properties()
        )
    );

    /**
     * Chronos Warden Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Dark slate gray (0x2F4F4F) - Background
     * Secondary color: Dark turquoise (0x00CED1) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> CHRONOS_WARDEN_SPAWN_EGG = ITEMS.register(
        ModItemId.CHRONOS_WARDEN_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CHRONOS_WARDEN,
            0x2F4F4F, // Background: Dark slate gray
            0x00CED1, // Spots: Dark turquoise
            new Item.Properties()
        )
    );

    /**
     * Clockwork Colossus Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Slate gray (0x708090) - Background
     * Secondary color: Dark orange (0xFF8C00) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> CLOCKWORK_COLOSSUS_SPAWN_EGG = ITEMS.register(
        ModItemId.CLOCKWORK_COLOSSUS_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CLOCKWORK_COLOSSUS,
            0x708090, // Background: Slate gray
            0xFF8C00, // Spots: Dark orange
            new Item.Properties()
        )
    );

    /**
     * Entropy Keeper Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Midnight blue (0x191970) - Background
     * Secondary color: Medium purple (0x9370DB) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> ENTROPY_KEEPER_SPAWN_EGG = ITEMS.register(
        ModItemId.ENTROPY_KEEPER_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.ENTROPY_KEEPER,
            0x191970, // Background: Midnight blue
            0x9370DB, // Spots: Medium purple
            new Item.Properties()
        )
    );

    /**
     * Temporal Phantom Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Dark gray (0x2F2F2F) - Background
     * Secondary color: Light gray (0xE0E0E0) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> TEMPORAL_PHANTOM_SPAWN_EGG = ITEMS.register(
        ModItemId.TEMPORAL_PHANTOM_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TEMPORAL_PHANTOM,
            0x2F2F2F, // Background: Dark gray
            0xE0E0E0, // Spots: Light gray
            new Item.Properties()
        )
    );

    /**
     * Floq Spawn Egg - For creative mode and debugging.
     * Primary color: Teal (0x008080) - Background
     * Secondary color: Light green (0x90EE90) - Spots
     */
    public static final RegistrySupplier<Item> FLOQ_SPAWN_EGG = ITEMS.register(
        ModItemId.FLOQ_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.FLOQ,
            0x008080, // Background: Teal
            0x90EE90, // Spots: Light green
            new Item.Properties()
        )
    );

    public static final RegistrySupplier<Item> EPOCH_HUSK_SPAWN_EGG = ITEMS.register(
        ModItemId.EPOCH_HUSK_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.EPOCH_HUSK,
            0xC9B78A, // Background: Sandy tan
            0x8B7355, // Spots: Dark brown
            new Item.Properties()
        )
    );

    public static final RegistrySupplier<Item> FORGOTTEN_MINUTE_SPAWN_EGG = ITEMS.register(
        ModItemId.FORGOTTEN_MINUTE_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.FORGOTTEN_MINUTE,
            0xA0A0D0, // Background: Pale purple
            0x6060A0, // Spots: Dark purple
            new Item.Properties()
        )
    );

    public static final RegistrySupplier<Item> CHRONAL_LEECH_SPAWN_EGG = ITEMS.register(
        ModItemId.CHRONAL_LEECH_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CHRONAL_LEECH,
            0x4A3A3A, // Background: Dark gray-brown
            0x8B0000, // Spots: Dark red
            new Item.Properties()
        )
    );

    public static final RegistrySupplier<Item> MOMENT_CREEPER_SPAWN_EGG = ITEMS.register(
        ModItemId.MOMENT_CREEPER_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.MOMENT_CREEPER,
            0x0DA70B, // Background: Creeper green
            0x000000, // Spots: Black
            new Item.Properties()
        )
    );

    /**
     * Glide Fish Spawn Egg - For creative mode and debugging.
     * Primary color: Light blue (0x87CEEB) - Background
     * Secondary color: Silver (0xC0C0C0) - Spots
     */
    public static final RegistrySupplier<Item> GLIDE_FISH_SPAWN_EGG = ITEMS.register(
        ModItemId.GLIDE_FISH_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.GLIDE_FISH,
            0x87CEEB, // Background: Light blue
            0xC0C0C0, // Spots: Silver
            new Item.Properties()
        )
    );

    public static final RegistrySupplier<Item> TIMELINE_STRIDER_SPAWN_EGG = ITEMS.register(
        ModItemId.TIMELINE_STRIDER_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIMELINE_STRIDER,
            0x1B1B3A, // Background: Dark navy
            0xFFD700, // Spots: Golden yellow
            new Item.Properties()
        )
    );

    public static final RegistrySupplier<Item> SECONDHAND_ARCHER_SPAWN_EGG = ITEMS.register(
        ModItemId.SECONDHAND_ARCHER_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.SECONDHAND_ARCHER,
            0x7A6B5A, // Background: Brownish-gray
            0xD4C4A8, // Spots: Light tan/bone
            new Item.Properties()
        )
    );

    public static final RegistrySupplier<Item> PARADOX_CRAWLER_SPAWN_EGG = ITEMS.register(
        ModItemId.PARADOX_CRAWLER_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.PARADOX_CRAWLER,
            0x2B2B4A, // Background: Dark indigo
            0x4FC3F7, // Spots: Bright cyan
            new Item.Properties()
        )
    );

    /**
     * Chrono Turtle Spawn Egg - For creative mode and debugging.
     * Primary color: Deep teal-green (0x3D7A5A) - Background (shell color)
     * Secondary color: Light sea green (0x8BC4A4) - Spots (accent)
     */
    public static final RegistrySupplier<Item> CHRONO_TURTLE_SPAWN_EGG = ITEMS.register(
        ModItemId.CHRONO_TURTLE_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CHRONO_TURTLE,
            0x3D7A5A, // Background: Deep teal-green (shell color)
            0x8BC4A4, // Spots: Light sea green (accent)
            new Item.Properties()
        )
    );

    /**
     * Timebound Rabbit Spawn Egg
     *
     * Primary color: Sky blue (0x5BA3D8) - Background (main body)
     * Secondary color: Deep blue (0x2E6E9C) - Spots (accent)
     */
    public static final RegistrySupplier<Item> TIMEBOUND_RABBIT_SPAWN_EGG = ITEMS.register(
        ModItemId.TIMEBOUND_RABBIT_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIMEBOUND_RABBIT,
            0x5BA3D8, // Background: Sky blue
            0x2E6E9C, // Spots: Deep blue
            new Item.Properties()
        )
    );

    /**
     * Pulse Hog Spawn Egg
     *
     * Primary color: Medium purple (0x7B68EE) - Background (main body)
     * Secondary color: Dark slate blue (0x483D8B) - Spots (accent)
     */
    public static final RegistrySupplier<Item> PULSE_HOG_SPAWN_EGG = ITEMS.register(
        ModItemId.PULSE_HOG_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.PULSE_HOG,
            0x7B68EE, // Background: Medium purple
            0x483D8B, // Spots: Dark slate blue
            new Item.Properties()
        )
    );

    /**
     * Secondwing Fowl Spawn Egg
     *
     * Primary color: Medium purple (0x9370DB) - Background (body color)
     * Secondary color: Gold (0xFFD700) - Spots (beak and feet)
     */
    public static final RegistrySupplier<Item> SECONDWING_FOWL_SPAWN_EGG = ITEMS.register(
        ModItemId.SECONDWING_FOWL_SPAWN_EGG.id(),
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.SECONDWING_FOWL,
            0x9370DB, // Background: Medium purple (body color)
            0xFFD700, // Spots: Gold (beak and feet)
            new Item.Properties()
        )
    );

    // === Boats ===

    /**
     * Time Wood Boat - Boat crafted from Time Wood Planks.
     * Task: T268 [US1] Create Time Wood Boat
     */
    public static final RegistrySupplier<Item> TIME_WOOD_BOAT = ITEMS.register(
        ModItemId.TIME_WOOD_BOAT.id(),
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.TIME_WOOD, false, ChronoDawnBoatItem.createProperties())
    );

    /**
     * Time Wood Chest Boat - Chest boat crafted from Time Wood Boat + Chest.
     * Task: T268 [US1] Create Time Wood Chest Boat
     */
    public static final RegistrySupplier<Item> TIME_WOOD_CHEST_BOAT = ITEMS.register(
        ModItemId.TIME_WOOD_CHEST_BOAT.id(),
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.TIME_WOOD, true, ChronoDawnBoatItem.createProperties())
    );

    /**
     * Dark Time Wood Boat - Boat crafted from Dark Time Wood Planks.
     * Task: T269 [US1] Create Dark Time Wood Boat
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_BOAT = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_BOAT.id(),
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.DARK_TIME_WOOD, false, ChronoDawnBoatItem.createProperties())
    );

    /**
     * Dark Time Wood Chest Boat - Chest boat crafted from Dark Time Wood Boat + Chest.
     * Task: T269 [US1] Create Dark Time Wood Chest Boat
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_CHEST_BOAT = ITEMS.register(
        ModItemId.DARK_TIME_WOOD_CHEST_BOAT.id(),
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.DARK_TIME_WOOD, true, ChronoDawnBoatItem.createProperties())
    );

    /**
     * Ancient Time Wood Boat - Boat crafted from Ancient Time Wood Planks.
     * Task: T270 [US1] Create Ancient Time Wood Boat
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_BOAT = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_BOAT.id(),
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.ANCIENT_TIME_WOOD, false, ChronoDawnBoatItem.createProperties())
    );

    /**
     * Ancient Time Wood Chest Boat - Chest boat crafted from Ancient Time Wood Boat + Chest.
     * Task: T270 [US1] Create Ancient Time Wood Chest Boat
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_CHEST_BOAT = ITEMS.register(
        ModItemId.ANCIENT_TIME_WOOD_CHEST_BOAT.id(),
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.ANCIENT_TIME_WOOD, true, ChronoDawnBoatItem.createProperties())
    );

    /**
     * Initialize item registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        ITEMS.register();
        ChronoDawn.LOGGER.debug("Registered ModItems");
    }

    /**
     * Initialize spawn eggs after all entities are registered.
     * This method must be called after entity registration is complete.
     */
    public static void initializeSpawnEggs() {
        ChronoDawn.LOGGER.debug("Initializing spawn eggs...");

        if (TEMPORAL_WRAITH_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) TEMPORAL_WRAITH_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (CLOCKWORK_SENTINEL_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) CLOCKWORK_SENTINEL_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (TIME_KEEPER_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) TIME_KEEPER_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        // Boss spawn eggs (command-only, not in creative tab)
        if (TIME_GUARDIAN_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) TIME_GUARDIAN_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (TIME_TYRANT_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) TIME_TYRANT_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (CHRONOS_WARDEN_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) CHRONOS_WARDEN_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (CLOCKWORK_COLOSSUS_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) CLOCKWORK_COLOSSUS_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (ENTROPY_KEEPER_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) ENTROPY_KEEPER_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (TEMPORAL_PHANTOM_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) TEMPORAL_PHANTOM_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (FLOQ_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) FLOQ_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (EPOCH_HUSK_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) EPOCH_HUSK_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (FORGOTTEN_MINUTE_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) FORGOTTEN_MINUTE_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (CHRONAL_LEECH_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) CHRONAL_LEECH_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (MOMENT_CREEPER_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) MOMENT_CREEPER_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (GLIDE_FISH_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) GLIDE_FISH_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (TIMELINE_STRIDER_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) TIMELINE_STRIDER_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (SECONDHAND_ARCHER_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) SECONDHAND_ARCHER_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (PARADOX_CRAWLER_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) PARADOX_CRAWLER_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (CHRONO_TURTLE_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) CHRONO_TURTLE_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (TIMEBOUND_RABBIT_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) TIMEBOUND_RABBIT_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (PULSE_HOG_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) PULSE_HOG_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        if (SECONDWING_FOWL_SPAWN_EGG.get() instanceof com.chronodawn.items.DeferredSpawnEggItem) {
            ((com.chronodawn.items.DeferredSpawnEggItem) SECONDWING_FOWL_SPAWN_EGG.get()).initializeSpawnEgg();
        }

        ChronoDawn.LOGGER.debug("Spawn eggs initialized");
    }

    /**
     * Populate creative mode tab with all items.
     * Called by ModCreativeTabs to add items to the ChronoDawn creative tab.
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
        output.accept(STRIPPED_TIME_WOOD_LOG.get());
        output.accept(TIME_WOOD.get());
        output.accept(STRIPPED_TIME_WOOD.get());
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
        // Dark Time Wood
        output.accept(DARK_TIME_WOOD_LOG.get());
        output.accept(STRIPPED_DARK_TIME_WOOD_LOG.get());
        output.accept(DARK_TIME_WOOD.get());
        output.accept(STRIPPED_DARK_TIME_WOOD.get());
        output.accept(DARK_TIME_WOOD_LEAVES.get());
        output.accept(DARK_TIME_WOOD_PLANKS.get());
        output.accept(DARK_TIME_WOOD_STAIRS.get());
        output.accept(DARK_TIME_WOOD_SLAB.get());
        output.accept(DARK_TIME_WOOD_FENCE.get());
        output.accept(DARK_TIME_WOOD_DOOR.get());
        output.accept(DARK_TIME_WOOD_TRAPDOOR.get());
        output.accept(DARK_TIME_WOOD_FENCE_GATE.get());
        output.accept(DARK_TIME_WOOD_BUTTON.get());
        output.accept(DARK_TIME_WOOD_PRESSURE_PLATE.get());
        output.accept(DARK_TIME_WOOD_SAPLING.get());
        // Ancient Time Wood
        output.accept(ANCIENT_TIME_WOOD_LOG.get());
        output.accept(STRIPPED_ANCIENT_TIME_WOOD_LOG.get());
        output.accept(ANCIENT_TIME_WOOD.get());
        output.accept(STRIPPED_ANCIENT_TIME_WOOD.get());
        output.accept(ANCIENT_TIME_WOOD_LEAVES.get());
        output.accept(ANCIENT_TIME_WOOD_PLANKS.get());
        output.accept(ANCIENT_TIME_WOOD_STAIRS.get());
        output.accept(ANCIENT_TIME_WOOD_SLAB.get());
        output.accept(ANCIENT_TIME_WOOD_FENCE.get());
        output.accept(ANCIENT_TIME_WOOD_DOOR.get());
        output.accept(ANCIENT_TIME_WOOD_TRAPDOOR.get());
        output.accept(ANCIENT_TIME_WOOD_FENCE_GATE.get());
        output.accept(ANCIENT_TIME_WOOD_BUTTON.get());
        output.accept(ANCIENT_TIME_WOOD_PRESSURE_PLATE.get());
        output.accept(ANCIENT_TIME_WOOD_SAPLING.get());
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
        // Note: TEMPORAL_PARTICLE_EMITTER is not added to creative tab (structure-only block)

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
        // Decorative Water Bucket is intentionally excluded from creative tab (admin/command only)
        output.accept(CHRONICLE_BOOK.get());
        // Add example Time Compass (targets Desert Clock Tower by default)
        output.accept(com.chronodawn.items.TimeCompassItem.createCompass(
            com.chronodawn.items.TimeCompassItem.STRUCTURE_DESERT_CLOCK_TOWER));

        // === Consumables ===
        output.accept(FRUIT_OF_TIME.get());
        output.accept(TIME_FRUIT_PIE.get());
        output.accept(TIME_JAM.get());
        output.accept(TIME_WHEAT_SEEDS.get());
        output.accept(TIME_WHEAT.get());
        output.accept(TIME_BREAD.get());

        // === Crop System (T211-T215) ===
        output.accept(TEMPORAL_ROOT.get());
        output.accept(BAKED_TEMPORAL_ROOT.get());
        output.accept(CHRONO_MELON_SEEDS.get());
        output.accept(CHRONO_MELON_SLICE.get());
        output.accept(CHRONO_MELON.get());
        output.accept(TIMELESS_MUSHROOM.get());
        output.accept(PURPLE_TIME_BLOSSOM.get());
        output.accept(ORANGE_TIME_BLOSSOM.get());
        output.accept(PINK_TIME_BLOSSOM.get());
        output.accept(DAWN_BELL.get());
        output.accept(DUSK_BELL.get());

        // === Crafted Foods (T215) ===
        output.accept(TEMPORAL_ROOT_STEW.get());
        output.accept(TIMELESS_MUSHROOM_SOUP.get());
        output.accept(GLISTENING_CHRONO_MELON.get());
        output.accept(CHRONO_MELON_JUICE.get());
        output.accept(ENHANCED_TIME_BREAD.get());
        output.accept(TIME_WHEAT_COOKIE.get());
        output.accept(CLOCKWORK_COOKIE.get());
        output.accept(GOLDEN_TIME_WHEAT.get());

        // === Fish Foods ===
        output.accept(GLIDE_FISH.get());
        output.accept(COOKED_GLIDE_FISH.get());

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

        // === Boss Drops ===
        output.accept(GUARDIAN_STONE.get());
        output.accept(COLOSSUS_GEAR.get());
        output.accept(PHANTOM_ESSENCE.get());
        output.accept(ENTROPY_CORE.get());

        // === Ultimate Crafted Items ===
        output.accept(CHRONO_AEGIS.get());

        // === Boats ===
        output.accept(TIME_WOOD_BOAT.get());
        output.accept(TIME_WOOD_CHEST_BOAT.get());
        output.accept(DARK_TIME_WOOD_BOAT.get());
        output.accept(DARK_TIME_WOOD_CHEST_BOAT.get());
        output.accept(ANCIENT_TIME_WOOD_BOAT.get());
        output.accept(ANCIENT_TIME_WOOD_CHEST_BOAT.get());

        // === Spawn Eggs ===
        // Check if spawn eggs are registered before adding (timing issue with DeferredRegister)
        if (TEMPORAL_WRAITH_SPAWN_EGG.isPresent()) {
            output.accept(TEMPORAL_WRAITH_SPAWN_EGG.get());
        }
        if (CLOCKWORK_SENTINEL_SPAWN_EGG.isPresent()) {
            output.accept(CLOCKWORK_SENTINEL_SPAWN_EGG.get());
        }
        if (TIME_KEEPER_SPAWN_EGG.isPresent()) {
            output.accept(TIME_KEEPER_SPAWN_EGG.get());
        }
        if (FLOQ_SPAWN_EGG.isPresent()) {
            output.accept(FLOQ_SPAWN_EGG.get());
        }
        if (EPOCH_HUSK_SPAWN_EGG.isPresent()) {
            output.accept(EPOCH_HUSK_SPAWN_EGG.get());
        }
        if (FORGOTTEN_MINUTE_SPAWN_EGG.isPresent()) {
            output.accept(FORGOTTEN_MINUTE_SPAWN_EGG.get());
        }
        if (CHRONAL_LEECH_SPAWN_EGG.isPresent()) {
            output.accept(CHRONAL_LEECH_SPAWN_EGG.get());
        }
        if (MOMENT_CREEPER_SPAWN_EGG.isPresent()) {
            output.accept(MOMENT_CREEPER_SPAWN_EGG.get());
        }
        if (GLIDE_FISH_SPAWN_EGG.isPresent()) {
            output.accept(GLIDE_FISH_SPAWN_EGG.get());
        }
        if (TIMELINE_STRIDER_SPAWN_EGG.isPresent()) {
            output.accept(TIMELINE_STRIDER_SPAWN_EGG.get());
        }
        if (SECONDHAND_ARCHER_SPAWN_EGG.isPresent()) {
            output.accept(SECONDHAND_ARCHER_SPAWN_EGG.get());
        }
        if (PARADOX_CRAWLER_SPAWN_EGG.isPresent()) {
            output.accept(PARADOX_CRAWLER_SPAWN_EGG.get());
        }
        if (CHRONO_TURTLE_SPAWN_EGG.isPresent()) {
            output.accept(CHRONO_TURTLE_SPAWN_EGG.get());
        }
        if (TIMEBOUND_RABBIT_SPAWN_EGG.isPresent()) {
            output.accept(TIMEBOUND_RABBIT_SPAWN_EGG.get());
        }
        if (PULSE_HOG_SPAWN_EGG.isPresent()) {
            output.accept(PULSE_HOG_SPAWN_EGG.get());
        }
        if (SECONDWING_FOWL_SPAWN_EGG.isPresent()) {
            output.accept(SECONDWING_FOWL_SPAWN_EGG.get());
        }
    }
}
