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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

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
        "clockstone_ore",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_ORE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_ore"))))
    );

    /**
     * Time Crystal Ore - BlockItem for placing Time Crystal Ore block.
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL_ORE = ITEMS.register(
        "time_crystal_ore",
        () -> new BlockItem(ModBlocks.TIME_CRYSTAL_ORE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_crystal_ore"))))
    );

    /**
     * Clockstone Block - BlockItem for placing Clockstone Block (portal frame material).
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_BLOCK = ITEMS.register(
        "clockstone_block",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_BLOCK.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_block"))))
    );

    /**
     * Clockstone Stairs - BlockItem for decorative stair variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_STAIRS = ITEMS.register(
        "clockstone_stairs",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_STAIRS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_stairs"))))
    );

    /**
     * Clockstone Slab - BlockItem for decorative slab variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_SLAB = ITEMS.register(
        "clockstone_slab",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_SLAB.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_slab"))))
    );

    /**
     * Clockstone Wall - BlockItem for decorative wall variant.
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_WALL = ITEMS.register(
        "clockstone_wall",
        () -> new BlockItem(ModBlocks.CLOCKSTONE_WALL.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_wall"))))
    );

    /**
     * Reversing Time Sandstone - BlockItem for special block that auto-restores after 3 seconds.
     */
    public static final RegistrySupplier<Item> REVERSING_TIME_SANDSTONE = ITEMS.register(
        "reversing_time_sandstone",
        () -> new BlockItem(ModBlocks.REVERSING_TIME_SANDSTONE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "reversing_time_sandstone"))))
    );

    /**
     * Unstable Fungus - BlockItem for special block that applies random speed effects.
     */
    public static final RegistrySupplier<Item> UNSTABLE_FUNGUS = ITEMS.register(
        "unstable_fungus",
        () -> new BlockItem(ModBlocks.UNSTABLE_FUNGUS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "unstable_fungus"))))
    );

    /**
     * Time Wood Log - BlockItem for placing Time Wood Log block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_LOG = ITEMS.register(
        "time_wood_log",
        () -> new BlockItem(ModBlocks.TIME_WOOD_LOG.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_log"))))
    );

    /**
     * Stripped Time Wood Log - BlockItem for placing Stripped Time Wood Log block.
     */
    public static final RegistrySupplier<Item> STRIPPED_TIME_WOOD_LOG = ITEMS.register(
        "stripped_time_wood_log",
        () -> new BlockItem(ModBlocks.STRIPPED_TIME_WOOD_LOG.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "stripped_time_wood_log"))))
    );

    /**
     * Time Wood - BlockItem for placing Time Wood block (all-bark variant).
     */
    public static final RegistrySupplier<Item> TIME_WOOD = ITEMS.register(
        "time_wood",
        () -> new BlockItem(ModBlocks.TIME_WOOD.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood"))))
    );

    /**
     * Stripped Time Wood - BlockItem for placing Stripped Time Wood block.
     */
    public static final RegistrySupplier<Item> STRIPPED_TIME_WOOD = ITEMS.register(
        "stripped_time_wood",
        () -> new BlockItem(ModBlocks.STRIPPED_TIME_WOOD.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "stripped_time_wood"))))
    );

    /**
     * Time Wood Leaves - BlockItem for placing Time Wood Leaves block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_LEAVES = ITEMS.register(
        "time_wood_leaves",
        () -> new BlockItem(ModBlocks.TIME_WOOD_LEAVES.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_leaves"))))
    );

    /**
     * Time Wood Planks - BlockItem for placing Time Wood Planks block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_PLANKS = ITEMS.register(
        "time_wood_planks",
        () -> new BlockItem(ModBlocks.TIME_WOOD_PLANKS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_planks"))))
    );

    /**
     * Time Wood Sapling - BlockItem for placing Time Wood Sapling block.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_SAPLING = ITEMS.register(
        "time_wood_sapling",
        () -> new BlockItem(ModBlocks.TIME_WOOD_SAPLING.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_sapling"))))
    );

    // === Dark Time Wood Block Items ===

    /**
     * Dark Time Wood Log - BlockItem for placing Dark Time Wood Log block.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_LOG = ITEMS.register(
        "dark_time_wood_log",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_LOG.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_log"))))
    );

    /**
     * Stripped Dark Time Wood Log - BlockItem for placing Stripped Dark Time Wood Log block.
     */
    public static final RegistrySupplier<Item> STRIPPED_DARK_TIME_WOOD_LOG = ITEMS.register(
        "stripped_dark_time_wood_log",
        () -> new BlockItem(ModBlocks.STRIPPED_DARK_TIME_WOOD_LOG.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "stripped_dark_time_wood_log"))))
    );

    /**
     * Dark Time Wood - BlockItem for placing Dark Time Wood block (all-bark variant).
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD = ITEMS.register(
        "dark_time_wood",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood"))))
    );

    /**
     * Stripped Dark Time Wood - BlockItem for placing Stripped Dark Time Wood block.
     */
    public static final RegistrySupplier<Item> STRIPPED_DARK_TIME_WOOD = ITEMS.register(
        "stripped_dark_time_wood",
        () -> new BlockItem(ModBlocks.STRIPPED_DARK_TIME_WOOD.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "stripped_dark_time_wood"))))
    );

    /**
     * Dark Time Wood Leaves - BlockItem for placing Dark Time Wood Leaves block.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_LEAVES = ITEMS.register(
        "dark_time_wood_leaves",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_LEAVES.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_leaves"))))
    );

    /**
     * Dark Time Wood Planks - BlockItem for placing Dark Time Wood Planks block.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_PLANKS = ITEMS.register(
        "dark_time_wood_planks",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_PLANKS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_planks"))))
    );

    /**
     * Dark Time Wood Stairs - BlockItem for placing Dark Time Wood Stairs.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_STAIRS = ITEMS.register(
        "dark_time_wood_stairs",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_STAIRS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_stairs"))))
    );

    /**
     * Dark Time Wood Slab - BlockItem for placing Dark Time Wood Slab.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_SLAB = ITEMS.register(
        "dark_time_wood_slab",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_SLAB.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_slab"))))
    );

    /**
     * Dark Time Wood Fence - BlockItem for placing Dark Time Wood Fence.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_FENCE = ITEMS.register(
        "dark_time_wood_fence",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_FENCE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_fence"))))
    );

    /**
     * Dark Time Wood Door - BlockItem for placing Dark Time Wood Door.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_DOOR = ITEMS.register(
        "dark_time_wood_door",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_DOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_door"))))
    );

    /**
     * Dark Time Wood Trapdoor - BlockItem for placing Dark Time Wood Trapdoor.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_TRAPDOOR = ITEMS.register(
        "dark_time_wood_trapdoor",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_TRAPDOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_trapdoor"))))
    );

    /**
     * Dark Time Wood Fence Gate - BlockItem for placing Dark Time Wood Fence Gate.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_FENCE_GATE = ITEMS.register(
        "dark_time_wood_fence_gate",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_FENCE_GATE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_fence_gate"))))
    );

    /**
     * Dark Time Wood Button - BlockItem for placing Dark Time Wood Button.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_BUTTON = ITEMS.register(
        "dark_time_wood_button",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_BUTTON.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_button"))))
    );

    /**
     * Dark Time Wood Pressure Plate - BlockItem for placing Dark Time Wood Pressure Plate.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_PRESSURE_PLATE = ITEMS.register(
        "dark_time_wood_pressure_plate",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_PRESSURE_PLATE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_pressure_plate"))))
    );

    /**
     * Dark Time Wood Sapling - BlockItem for placing Dark Time Wood Sapling.
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_SAPLING = ITEMS.register(
        "dark_time_wood_sapling",
        () -> new BlockItem(ModBlocks.DARK_TIME_WOOD_SAPLING.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_sapling"))))
    );

    // === Ancient Time Wood Block Items ===

    /**
     * Ancient Time Wood Log - BlockItem for placing Ancient Time Wood Log block.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_LOG = ITEMS.register(
        "ancient_time_wood_log",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_LOG.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_log"))))
    );

    /**
     * Stripped Ancient Time Wood Log - BlockItem for placing Stripped Ancient Time Wood Log block.
     */
    public static final RegistrySupplier<Item> STRIPPED_ANCIENT_TIME_WOOD_LOG = ITEMS.register(
        "stripped_ancient_time_wood_log",
        () -> new BlockItem(ModBlocks.STRIPPED_ANCIENT_TIME_WOOD_LOG.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "stripped_ancient_time_wood_log"))))
    );

    /**
     * Ancient Time Wood - BlockItem for placing Ancient Time Wood block (all-bark variant).
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD = ITEMS.register(
        "ancient_time_wood",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood"))))
    );

    /**
     * Stripped Ancient Time Wood - BlockItem for placing Stripped Ancient Time Wood block.
     */
    public static final RegistrySupplier<Item> STRIPPED_ANCIENT_TIME_WOOD = ITEMS.register(
        "stripped_ancient_time_wood",
        () -> new BlockItem(ModBlocks.STRIPPED_ANCIENT_TIME_WOOD.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "stripped_ancient_time_wood"))))
    );

    /**
     * Ancient Time Wood Leaves - BlockItem for placing Ancient Time Wood Leaves block.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_LEAVES = ITEMS.register(
        "ancient_time_wood_leaves",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_LEAVES.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_leaves"))))
    );

    /**
     * Ancient Time Wood Planks - BlockItem for placing Ancient Time Wood Planks block.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_PLANKS = ITEMS.register(
        "ancient_time_wood_planks",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_PLANKS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_planks"))))
    );

    /**
     * Ancient Time Wood Stairs - BlockItem for placing Ancient Time Wood Stairs.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_STAIRS = ITEMS.register(
        "ancient_time_wood_stairs",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_STAIRS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_stairs"))))
    );

    /**
     * Ancient Time Wood Slab - BlockItem for placing Ancient Time Wood Slab.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_SLAB = ITEMS.register(
        "ancient_time_wood_slab",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_SLAB.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_slab"))))
    );

    /**
     * Ancient Time Wood Fence - BlockItem for placing Ancient Time Wood Fence.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_FENCE = ITEMS.register(
        "ancient_time_wood_fence",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_FENCE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_fence"))))
    );

    /**
     * Ancient Time Wood Door - BlockItem for placing Ancient Time Wood Door.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_DOOR = ITEMS.register(
        "ancient_time_wood_door",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_DOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_door"))))
    );

    /**
     * Ancient Time Wood Trapdoor - BlockItem for placing Ancient Time Wood Trapdoor.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_TRAPDOOR = ITEMS.register(
        "ancient_time_wood_trapdoor",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_TRAPDOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_trapdoor"))))
    );

    /**
     * Ancient Time Wood Fence Gate - BlockItem for placing Ancient Time Wood Fence Gate.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_FENCE_GATE = ITEMS.register(
        "ancient_time_wood_fence_gate",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_FENCE_GATE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_fence_gate"))))
    );

    /**
     * Ancient Time Wood Button - BlockItem for placing Ancient Time Wood Button.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_BUTTON = ITEMS.register(
        "ancient_time_wood_button",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_BUTTON.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_button"))))
    );

    /**
     * Ancient Time Wood Pressure Plate - BlockItem for placing Ancient Time Wood Pressure Plate.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_PRESSURE_PLATE = ITEMS.register(
        "ancient_time_wood_pressure_plate",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_PRESSURE_PLATE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_pressure_plate"))))
    );

    /**
     * Ancient Time Wood Sapling - BlockItem for placing Ancient Time Wood Sapling.
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_SAPLING = ITEMS.register(
        "ancient_time_wood_sapling",
        () -> new BlockItem(ModBlocks.ANCIENT_TIME_WOOD_SAPLING.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_sapling"))))
    );

    /**
     * Time Wheat Bale - BlockItem for placing Time Wheat Bale block.
     */
    public static final RegistrySupplier<Item> TIME_WHEAT_BALE = ITEMS.register(
        "time_wheat_bale",
        () -> new BlockItem(ModBlocks.TIME_WHEAT_BALE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wheat_bale"))))
    );

    /**
     * Clock Tower Teleporter - BlockItem for Desert Clock Tower teleporter.
     * Requires 3 seconds of charging (holding right-click) before teleporting.
     * UP direction: 4th floor → 5th floor (boss room).
     * DOWN direction: 5th floor → 4th floor (appears after defeating Time Guardian).
     */
    public static final RegistrySupplier<Item> CLOCK_TOWER_TELEPORTER = ITEMS.register(
        "clock_tower_teleporter",
        () -> new BlockItem(ModBlocks.CLOCK_TOWER_TELEPORTER.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clock_tower_teleporter"))))
    );

    /**
     * Clockwork Block - Decorative block with animated rotating gears theme.
     */
    public static final RegistrySupplier<Item> CLOCKWORK_BLOCK = ITEMS.register(
        "clockwork_block",
        () -> new BlockItem(ModBlocks.CLOCKWORK_BLOCK.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockwork_block"))))
    );

    /**
     * Time Crystal Block - Decorative block that emits light level 10.
     */
    public static final RegistrySupplier<Item> TIME_CRYSTAL_BLOCK = ITEMS.register(
        "time_crystal_block",
        () -> new BlockItem(ModBlocks.TIME_CRYSTAL_BLOCK.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_crystal_block"))))
    );

    /**
     * Temporal Bricks - Building block crafted from Clockstone.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS = ITEMS.register(
        "temporal_bricks",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_bricks"))))
    );

    /**
     * Temporal Bricks Stairs - Stair variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_STAIRS = ITEMS.register(
        "temporal_bricks_stairs",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_STAIRS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_bricks_stairs"))))
    );

    /**
     * Temporal Bricks Slab - Slab variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_SLAB = ITEMS.register(
        "temporal_bricks_slab",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_SLAB.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_bricks_slab"))))
    );

    /**
     * Temporal Bricks Wall - Wall variant of Temporal Bricks.
     */
    public static final RegistrySupplier<Item> TEMPORAL_BRICKS_WALL = ITEMS.register(
        "temporal_bricks_wall",
        () -> new BlockItem(ModBlocks.TEMPORAL_BRICKS_WALL.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_bricks_wall"))))
    );

    /**
     * Time Wood Stairs - Stair variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_STAIRS = ITEMS.register(
        "time_wood_stairs",
        () -> new BlockItem(ModBlocks.TIME_WOOD_STAIRS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_stairs"))))
    );

    /**
     * Time Wood Slab - Slab variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_SLAB = ITEMS.register(
        "time_wood_slab",
        () -> new BlockItem(ModBlocks.TIME_WOOD_SLAB.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_slab"))))
    );

    /**
     * Time Wood Fence - Fence variant of Time Wood Planks.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_FENCE = ITEMS.register(
        "time_wood_fence",
        () -> new BlockItem(ModBlocks.TIME_WOOD_FENCE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_fence"))))
    );

    /**
     * Time Wood Door - Wooden door that can be opened/closed.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_DOOR = ITEMS.register(
        "time_wood_door",
        () -> new BlockItem(ModBlocks.TIME_WOOD_DOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_door"))))
    );

    /**
     * Time Wood Trapdoor - Wooden trapdoor that can be opened/closed.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_TRAPDOOR = ITEMS.register(
        "time_wood_trapdoor",
        () -> new BlockItem(ModBlocks.TIME_WOOD_TRAPDOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_trapdoor"))))
    );

    /**
     * Time Wood Fence Gate - Fence gate that connects to fences.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_FENCE_GATE = ITEMS.register(
        "time_wood_fence_gate",
        () -> new BlockItem(ModBlocks.TIME_WOOD_FENCE_GATE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_fence_gate"))))
    );

    /**
     * Time Wood Button - Wooden button that emits redstone signal.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_BUTTON = ITEMS.register(
        "time_wood_button",
        () -> new BlockItem(ModBlocks.TIME_WOOD_BUTTON.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_button"))))
    );

    /**
     * Time Wood Pressure Plate - Wooden pressure plate that emits redstone signal.
     */
    public static final RegistrySupplier<Item> TIME_WOOD_PRESSURE_PLATE = ITEMS.register(
        "time_wood_pressure_plate",
        () -> new BlockItem(ModBlocks.TIME_WOOD_PRESSURE_PLATE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_pressure_plate"))))
    );

    /**
     * Temporal Moss - Decorative moss block exclusive to swamp biome.
     */
    public static final RegistrySupplier<Item> TEMPORAL_MOSS = ITEMS.register(
        "temporal_moss",
        () -> new BlockItem(ModBlocks.TEMPORAL_MOSS.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_moss"))))
    );

    /**
     * Frozen Time Ice - Special ice block exclusive to snowy biome.
     */
    public static final RegistrySupplier<Item> FROZEN_TIME_ICE = ITEMS.register(
        "frozen_time_ice",
        () -> new BlockItem(ModBlocks.FROZEN_TIME_ICE.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "frozen_time_ice"))))
    );

    /**
     * Boss Room Door - Custom iron door item with BlockEntity for NBT data storage.
     * Identical appearance to vanilla iron door but can differentiate between entrance and boss room doors.
     */
    public static final RegistrySupplier<Item> BOSS_ROOM_DOOR = ITEMS.register(
        "boss_room_door",
        () -> new BlockItem(ModBlocks.BOSS_ROOM_DOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "boss_room_door"))))
    );

    /**
     * Entropy Crypt Trapdoor - Triggers Entropy Keeper boss spawn when first opened.
     * Used in Entropy Crypt structure as the entrance to Vault (treasure room).
     * After boss spawns (ACTIVATED=true), functions as normal trapdoor.
     */
    public static final RegistrySupplier<Item> ENTROPY_CRYPT_TRAPDOOR = ITEMS.register(
        "entropy_crypt_trapdoor",
        () -> new BlockItem(ModBlocks.ENTROPY_CRYPT_TRAPDOOR.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_crypt_trapdoor"))))
    );

    /**
     * Temporal Particle Emitter - Invisible block that emits time distortion particles.
     * Structure-only block, not added to creative tab.
     * Can be placed with commands for testing: /give @s chronodawn:temporal_particle_emitter
     */
    public static final RegistrySupplier<Item> TEMPORAL_PARTICLE_EMITTER = ITEMS.register(
        "temporal_particle_emitter",
        () -> new BlockItem(ModBlocks.TEMPORAL_PARTICLE_EMITTER.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_particle_emitter"))))
    );

    /**
     * Boss Room Boundary Marker - Structure editing tool for defining boss room protection areas.
     * Not added to creative tab - used only in structure editing with Structure Block.
     */
    public static final RegistrySupplier<Item> BOSS_ROOM_BOUNDARY_MARKER = ITEMS.register(
        "boss_room_boundary_marker",
        () -> new BlockItem(ModBlocks.BOSS_ROOM_BOUNDARY_MARKER.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "boss_room_boundary_marker"))))
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
        "eye_of_chronos",
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
        "guardian_stone",
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
        "colossus_gear",
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
        "phantom_essence",
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
        "entropy_core",
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
        "chrono_aegis",
        () -> new com.chronodawn.items.ChronoAegisItem(
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chrono_aegis")))
        )
    );

    // === Portal Items ===

    /**
     * Time Hourglass - Portal ignition item.
     * Single-use item for activating ChronoDawn portals.
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

    /**
     * Time Compass - Structure locator compass.
     * Points to nearest key structure (Ancient Ruins, Desert Clock Tower, or Master Clock Tower).
     * Target structure stored in NBT. Obtained through Time Keeper trades.
     */
    public static final RegistrySupplier<Item> TIME_COMPASS = ITEMS.register(
        "time_compass",
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
        "decorative_water_bucket",
        () -> new DecorativeWaterBucketItem(ModFluids.DECORATIVE_WATER.get(), new Item.Properties().stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "decorative_water_bucket"))))
    );

    /**
     * Chronicle Book - Custom guidebook item.
     * Opens the Chronicle GUI when used, replacing Patchouli dependency.
     * Distributed via advancement grant_chronicle_book when entering ChronoDawn.
     */
    public static final RegistrySupplier<Item> CHRONICLE_BOOK = ITEMS.register(
        "chronicle_book",
        () -> new ChronicleBookItem(ChronicleBookItem.createProperties())
    );

    // === Consumables ===

    /**
     * Fruit of Time - Food item found in ChronoDawn dimension.
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

    /**
     * Temporal Root - Root vegetable that can be planted or eaten.
     * Restores 3 hunger points (raw).
     * Can be planted on farmland like carrots.
     */
    public static final RegistrySupplier<Item> TEMPORAL_ROOT = ITEMS.register(
        "temporal_root",
        () -> new TemporalRootItem(TemporalRootItem.createProperties())
    );

    /**
     * Baked Temporal Root - Cooked root vegetable with healing properties.
     * Restores 6 hunger points + Regeneration I for 5 seconds.
     * Crafted by smelting Temporal Root.
     */
    public static final RegistrySupplier<Item> BAKED_TEMPORAL_ROOT = ITEMS.register(
        "baked_temporal_root",
        () -> new BakedTemporalRootItem(BakedTemporalRootItem.createProperties())
    );

    /**
     * Chrono Melon Slice - Basic food item from Chrono Melon.
     * Restores 2 hunger points.
     * Fast eating speed (1.6 seconds).
     */
    public static final RegistrySupplier<Item> CHRONO_MELON_SLICE = ITEMS.register(
        "chrono_melon_slice",
        () -> new ChronoMelonSliceItem(ChronoMelonSliceItem.createProperties())
    );

    /**
     * Chrono Melon Seeds - Seeds for planting Chrono Melon Stems.
     * Can be planted on farmland to grow Chrono Melon.
     */
    public static final RegistrySupplier<Item> CHRONO_MELON_SEEDS = ITEMS.register(
        "chrono_melon_seeds",
        () -> new ChronoMelonSeedsItem(ChronoMelonSeedsItem.createProperties())
    );

    /**
     * Chrono Melon Block Item - Full melon block item.
     * Can be placed as decoration or broken for 9 slices.
     */
    public static final RegistrySupplier<Item> CHRONO_MELON = ITEMS.register(
        "chrono_melon",
        () -> new BlockItem(ModBlocks.CHRONO_MELON.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chrono_melon"))))
    );

    /**
     * Timeless Mushroom - Edible mushroom that can be planted or eaten.
     * Restores 2 hunger points.
     * Can be planted in dark areas (light level 12 or less).
     */
    public static final RegistrySupplier<Item> TIMELESS_MUSHROOM = ITEMS.register(
        "timeless_mushroom",
        () -> new TimelessMushroomItem(TimelessMushroomItem.createProperties())
    );

    /**
     * Purple Time Blossom - Decorative flower with purple time-themed appearance.
     * Can be placed as a block or in flower pots.
     */
    public static final RegistrySupplier<Item> PURPLE_TIME_BLOSSOM = ITEMS.register(
        "purple_time_blossom",
        () -> new BlockItem(ModBlocks.PURPLE_TIME_BLOSSOM.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "purple_time_blossom"))))
    );

    /**
     * Orange Time Blossom - Decorative flower with orange time-themed appearance.
     * Can be placed as a block or in flower pots.
     */
    public static final RegistrySupplier<Item> ORANGE_TIME_BLOSSOM = ITEMS.register(
        "orange_time_blossom",
        () -> new BlockItem(ModBlocks.ORANGE_TIME_BLOSSOM.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "orange_time_blossom"))))
    );

    /**
     * Pink Time Blossom - Decorative flower with pink time-themed appearance.
     * Can be placed as a block or in flower pots.
     */
    public static final RegistrySupplier<Item> PINK_TIME_BLOSSOM = ITEMS.register(
        "pink_time_blossom",
        () -> new BlockItem(ModBlocks.PINK_TIME_BLOSSOM.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "pink_time_blossom"))))
    );

    /**
     * Dawn Bell - Tall blue flower representing dawn/morning in the time cycle.
     * 2 blocks high, cannot be potted.
     */
    public static final RegistrySupplier<Item> DAWN_BELL = ITEMS.register(
        "dawn_bell",
        () -> new BlockItem(ModBlocks.DAWN_BELL.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dawn_bell"))))
    );

    /**
     * Dusk Bell - Tall red flower representing dusk/evening in the time cycle.
     * 2 blocks high, cannot be potted.
     */
    public static final RegistrySupplier<Item> DUSK_BELL = ITEMS.register(
        "dusk_bell",
        () -> new BlockItem(ModBlocks.DUSK_BELL.get(), new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dusk_bell"))))
    );

    /**
     * Temporal Root Stew - Hearty stew combining Baked Temporal Root and Timeless Mushroom.
     * Restores 8 hunger points with Regeneration II for 10 seconds.
     * Recipe: 1x Baked Temporal Root + 1x Timeless Mushroom + 1x Bowl
     */
    public static final RegistrySupplier<Item> TEMPORAL_ROOT_STEW = ITEMS.register(
        "temporal_root_stew",
        () -> new TemporalRootStewItem(TemporalRootStewItem.createProperties())
    );

    /**
     * Glistening Chrono Melon - Premium melon slice infused with gold.
     * Restores 2 hunger points with high saturation and Absorption I for 30 seconds.
     * Recipe: 1x Chrono Melon Slice + 8x Gold Nuggets
     */
    public static final RegistrySupplier<Item> GLISTENING_CHRONO_MELON = ITEMS.register(
        "glistening_chrono_melon",
        () -> new GlisteningChronoMelonItem(GlisteningChronoMelonItem.createProperties())
    );

    /**
     * Chrono Melon Juice - Drinkable beverage providing Speed effect.
     * Restores 4 hunger points with Speed I for 60 seconds.
     * Recipe: 4x Chrono Melon Slices + 1x Glass Bottle
     */
    public static final RegistrySupplier<Item> CHRONO_MELON_JUICE = ITEMS.register(
        "chrono_melon_juice",
        () -> new ChronoMelonJuiceItem(ChronoMelonJuiceItem.createProperties())
    );

    /**
     * Timeless Mushroom Soup - Simple mushroom soup with Night Vision effect.
     * Restores 6 hunger points with Night Vision for 60 seconds.
     * Recipe: 2x Timeless Mushrooms + 1x Bowl
     */
    public static final RegistrySupplier<Item> TIMELESS_MUSHROOM_SOUP = ITEMS.register(
        "timeless_mushroom_soup",
        () -> new TimelessMushroomSoupItem(TimelessMushroomSoupItem.createProperties())
    );

    /**
     * Enhanced Time Bread - Improved Time Bread fortified with Temporal Root.
     * Restores 7 hunger points with Saturation I for 5 seconds.
     * Recipe: 3x Time Wheat + 1x Temporal Root
     */
    public static final RegistrySupplier<Item> ENHANCED_TIME_BREAD = ITEMS.register(
        "enhanced_time_bread",
        () -> new EnhancedTimeBreadItem(EnhancedTimeBreadItem.createProperties())
    );

    /**
     * Time Wheat Cookie - Simple cookie made from Time Wheat and cocoa beans.
     * Restores 2 hunger points. Fast-eating snack.
     * Recipe: 2x Time Wheat + 1x Cocoa Beans
     */
    public static final RegistrySupplier<Item> TIME_WHEAT_COOKIE = ITEMS.register(
        "time_wheat_cookie",
        () -> new TimeWheatCookieItem(TimeWheatCookieItem.createProperties())
    );

    /**
     * Clockwork Cookie - Time-themed cookie with defensive effects.
     * Restores 2 hunger points with Resistance I (30s) and Fire Resistance (30s).
     * Recipe: 2x Time Wheat + 1x Time Jam + 2x Clockwork Block → 4x Clockwork Cookie
     */
    public static final RegistrySupplier<Item> CLOCKWORK_COOKIE = ITEMS.register(
        "clockwork_cookie",
        () -> new ClockworkCookieItem(ClockworkCookieItem.createProperties())
    );

    /**
     * Golden Time Wheat - Time Wheat infused with gold, providing powerful effects.
     * Restores 4 hunger points with Regeneration II (10s) and Absorption II (2min).
     * Recipe: 1x Time Wheat + 8x Gold Ingots
     */
    public static final RegistrySupplier<Item> GOLDEN_TIME_WHEAT = ITEMS.register(
        "golden_time_wheat",
        () -> new GoldenTimeWheatItem(GoldenTimeWheatItem.createProperties())
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
        () -> new ClockstoneArmorItem(ArmorType.HELMET, ClockstoneArmorItem.createProperties(ArmorType.HELMET)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_helmet"))))
    );

    /**
     * Clockstone Chestplate - Tier 1 time-themed chestplate.
     * Defense: 6, Durability: 240
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_CHESTPLATE = ITEMS.register(
        "clockstone_chestplate",
        () -> new ClockstoneArmorItem(ArmorType.CHESTPLATE, ClockstoneArmorItem.createProperties(ArmorType.CHESTPLATE)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_chestplate"))))
    );

    /**
     * Clockstone Leggings - Tier 1 time-themed leggings.
     * Defense: 5, Durability: 225
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_LEGGINGS = ITEMS.register(
        "clockstone_leggings",
        () -> new ClockstoneArmorItem(ArmorType.LEGGINGS, ClockstoneArmorItem.createProperties(ArmorType.LEGGINGS)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_leggings"))))
    );

    /**
     * Clockstone Boots - Tier 1 time-themed boots.
     * Defense: 2, Durability: 195
     */
    public static final RegistrySupplier<Item> CLOCKSTONE_BOOTS = ITEMS.register(
        "clockstone_boots",
        () -> new ClockstoneArmorItem(ArmorType.BOOTS, ClockstoneArmorItem.createProperties(ArmorType.BOOTS)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_boots"))))
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
        () -> new EnhancedClockstoneArmorItem(ArmorType.HELMET, EnhancedClockstoneArmorItem.createProperties(ArmorType.HELMET)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "enhanced_clockstone_helmet"))))
    );

    /**
     * Enhanced Clockstone Chestplate - Tier 2 time-themed chestplate.
     * Defense: 7, Durability: 448
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_CHESTPLATE = ITEMS.register(
        "enhanced_clockstone_chestplate",
        () -> new EnhancedClockstoneArmorItem(ArmorType.CHESTPLATE, EnhancedClockstoneArmorItem.createProperties(ArmorType.CHESTPLATE)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "enhanced_clockstone_chestplate"))))
    );

    /**
     * Enhanced Clockstone Leggings - Tier 2 time-themed leggings.
     * Defense: 6, Durability: 420
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_LEGGINGS = ITEMS.register(
        "enhanced_clockstone_leggings",
        () -> new EnhancedClockstoneArmorItem(ArmorType.LEGGINGS, EnhancedClockstoneArmorItem.createProperties(ArmorType.LEGGINGS)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "enhanced_clockstone_leggings"))))
    );

    /**
     * Enhanced Clockstone Boots - Tier 2 time-themed boots.
     * Defense: 3, Durability: 364
     * Full set grants immunity to time distortion effects.
     */
    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_BOOTS = ITEMS.register(
        "enhanced_clockstone_boots",
        () -> new EnhancedClockstoneArmorItem(ArmorType.BOOTS, EnhancedClockstoneArmorItem.createProperties(ArmorType.BOOTS)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "enhanced_clockstone_boots"))))
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

    // === Spawn Eggs ===

    /**
     * Temporal Wraith Spawn Egg - For creative mode and debugging.
     * Primary color: Dark purple (0x4B0082) - Indigo (background)
     * Secondary color: Light cyan (0xADD8E6) - Light blue (spots)
     */
    public static final RegistrySupplier<Item> TEMPORAL_WRAITH_SPAWN_EGG = ITEMS.register(
        "temporal_wraith_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TEMPORAL_WRAITH,
            0x4B0082, // Background: Dark purple (indigo)
            0xADD8E6, // Spots: Light cyan
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_wraith_spawn_egg")))
        )
    );

    /**
     * Clockwork Sentinel Spawn Egg - For creative mode and debugging.
     * Primary color: Royal blue (0x4169E1) - Background
     * Secondary color: Gold (0xFFD700) - Spots
     */
    public static final RegistrySupplier<Item> CLOCKWORK_SENTINEL_SPAWN_EGG = ITEMS.register(
        "clockwork_sentinel_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CLOCKWORK_SENTINEL,
            0x4169E1, // Background: Royal blue
            0xFFD700, // Spots: Gold
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockwork_sentinel_spawn_egg")))
        )
    );

    /**
     * Time Keeper Spawn Egg - For creative mode and debugging.
     * Primary color: Dark slate blue (0x483D8B) - Background
     * Secondary color: White (0xF5F5F5) - Spots
     */
    public static final RegistrySupplier<Item> TIME_KEEPER_SPAWN_EGG = ITEMS.register(
        "time_keeper_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIME_KEEPER,
            0x483D8B, // Background: Dark slate blue
            0xF5F5F5, // Spots: Off-white
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_keeper_spawn_egg")))
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
        "time_guardian_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIME_GUARDIAN,
            0xFFD700, // Background: Gold
            0x4169E1, // Spots: Royal blue
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_guardian_spawn_egg")))
        )
    );

    /**
     * Time Tyrant Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Dark red (0x8B0000) - Background
     * Secondary color: Gold (0xFFD700) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> TIME_TYRANT_SPAWN_EGG = ITEMS.register(
        "time_tyrant_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIME_TYRANT,
            0x8B0000, // Background: Dark red
            0xFFD700, // Spots: Gold
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_tyrant_spawn_egg")))
        )
    );

    /**
     * Chronos Warden Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Dark slate gray (0x2F4F4F) - Background
     * Secondary color: Dark turquoise (0x00CED1) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> CHRONOS_WARDEN_SPAWN_EGG = ITEMS.register(
        "chronos_warden_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CHRONOS_WARDEN,
            0x2F4F4F, // Background: Dark slate gray
            0x00CED1, // Spots: Dark turquoise
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronos_warden_spawn_egg")))
        )
    );

    /**
     * Clockwork Colossus Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Slate gray (0x708090) - Background
     * Secondary color: Dark orange (0xFF8C00) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> CLOCKWORK_COLOSSUS_SPAWN_EGG = ITEMS.register(
        "clockwork_colossus_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CLOCKWORK_COLOSSUS,
            0x708090, // Background: Slate gray
            0xFF8C00, // Spots: Dark orange
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockwork_colossus_spawn_egg")))
        )
    );

    /**
     * Entropy Keeper Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Midnight blue (0x191970) - Background
     * Secondary color: Medium purple (0x9370DB) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> ENTROPY_KEEPER_SPAWN_EGG = ITEMS.register(
        "entropy_keeper_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.ENTROPY_KEEPER,
            0x191970, // Background: Midnight blue
            0x9370DB, // Spots: Medium purple
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_keeper_spawn_egg")))
        )
    );

    /**
     * Temporal Phantom Spawn Egg - For debugging and testing only (command-only).
     * Primary color: Dark gray (0x2F2F2F) - Background
     * Secondary color: Light gray (0xE0E0E0) - Spots
     * NOT displayed in creative tab (following vanilla behavior for Ender Dragon and Wither)
     */
    public static final RegistrySupplier<Item> TEMPORAL_PHANTOM_SPAWN_EGG = ITEMS.register(
        "temporal_phantom_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TEMPORAL_PHANTOM,
            0x2F2F2F, // Background: Dark gray
            0xE0E0E0, // Spots: Light gray
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_phantom_spawn_egg")))
        )
    );

    /**
     * Floq Spawn Egg - For creative mode and debugging.
     * Primary color: Teal (0x008080) - Background
     * Secondary color: Light green (0x90EE90) - Spots
     */
    public static final RegistrySupplier<Item> FLOQ_SPAWN_EGG = ITEMS.register(
        "floq_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.FLOQ,
            0x008080, // Background: Teal
            0x90EE90, // Spots: Light green
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "floq_spawn_egg")))
        )
    );

    public static final RegistrySupplier<Item> EPOCH_HUSK_SPAWN_EGG = ITEMS.register(
        "epoch_husk_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.EPOCH_HUSK,
            0xC9B78A, // Background: Sandy tan
            0x8B7355, // Spots: Dark brown
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "epoch_husk_spawn_egg")))
        )
    );

    public static final RegistrySupplier<Item> FORGOTTEN_MINUTE_SPAWN_EGG = ITEMS.register(
        "forgotten_minute_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.FORGOTTEN_MINUTE,
            0xA0A0D0, // Background: Pale purple
            0x6060A0, // Spots: Dark purple
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "forgotten_minute_spawn_egg")))
        )
    );

    public static final RegistrySupplier<Item> CHRONAL_LEECH_SPAWN_EGG = ITEMS.register(
        "chronal_leech_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.CHRONAL_LEECH,
            0x4A3A3A, // Background: Dark gray-brown
            0x8B0000, // Spots: Dark red
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronal_leech_spawn_egg")))
        )
    );

    public static final RegistrySupplier<Item> MOMENT_CREEPER_SPAWN_EGG = ITEMS.register(
        "moment_creeper_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.MOMENT_CREEPER,
            0x0DA70B, // Background: Creeper green
            0x000000, // Spots: Black
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "moment_creeper_spawn_egg")))
        )
    );

    // === GlideFish Items ===

    /**
     * Glide Fish - Food item dropped by GlideFish.
     * Restores 2 hunger points (raw fish equivalent).
     */
    public static final RegistrySupplier<Item> GLIDE_FISH = ITEMS.register(
        "glide_fish",
        () -> new Item(new Item.Properties()
                .food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.1f)
                    .build())
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "glide_fish"))))
    );

    /**
     * Cooked Glide Fish - Cooked version of Glide Fish.
     * Nutrition: 5, Saturation: 0.6 (equivalent to cooked cod).
     */
    public static final RegistrySupplier<Item> COOKED_GLIDE_FISH = ITEMS.register(
        "cooked_glide_fish",
        () -> new Item(new Item.Properties()
                .food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(5)
                    .saturationModifier(0.6f)
                    .build())
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "cooked_glide_fish"))))
    );

    public static final RegistrySupplier<Item> GLIDE_FISH_SPAWN_EGG = ITEMS.register(
        "glide_fish_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.GLIDE_FISH,
            0x4682B4, // Background: Steel blue
            0x87CEEB, // Spots: Sky blue
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "glide_fish_spawn_egg")))
        )
    );

    public static final RegistrySupplier<Item> TIMELINE_STRIDER_SPAWN_EGG = ITEMS.register(
        "timeline_strider_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.TIMELINE_STRIDER,
            0x1B1B3A, // Background: Dark navy
            0xFFD700, // Spots: Golden yellow
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "timeline_strider_spawn_egg")))
        )
    );

    public static final RegistrySupplier<Item> SECONDHAND_ARCHER_SPAWN_EGG = ITEMS.register(
        "secondhand_archer_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.SECONDHAND_ARCHER,
            0x7A6B5A, // Background: Brownish-gray
            0xD4C4A8, // Spots: Light tan/bone
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "secondhand_archer_spawn_egg")))
        )
    );

    public static final RegistrySupplier<Item> PARADOX_CRAWLER_SPAWN_EGG = ITEMS.register(
        "paradox_crawler_spawn_egg",
        () -> new com.chronodawn.items.DeferredSpawnEggItem(
            ModEntities.PARADOX_CRAWLER,
            0x2B2B4A, // Background: Dark indigo
            0x4FC3F7, // Spots: Bright cyan
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "paradox_crawler_spawn_egg")))
        )
    );

    // === Boats ===

    /**
     * Time Wood Boat - Boat crafted from Time Wood Planks.
     * Task: T268 [US1] Create Time Wood Boat
     */
    public static final RegistrySupplier<Item> TIME_WOOD_BOAT = ITEMS.register(
        "time_wood_boat",
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.TIME_WOOD, false, ChronoDawnBoatItem.createProperties("time_wood_boat"))
    );

    /**
     * Time Wood Chest Boat - Chest boat crafted from Time Wood Boat + Chest.
     * Task: T268 [US1] Create Time Wood Chest Boat
     */
    public static final RegistrySupplier<Item> TIME_WOOD_CHEST_BOAT = ITEMS.register(
        "time_wood_chest_boat",
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.TIME_WOOD, true, ChronoDawnBoatItem.createProperties("time_wood_chest_boat"))
    );

    /**
     * Dark Time Wood Boat - Boat crafted from Dark Time Wood Planks.
     * Task: T269 [US1] Create Dark Time Wood Boat
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_BOAT = ITEMS.register(
        "dark_time_wood_boat",
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.DARK_TIME_WOOD, false, ChronoDawnBoatItem.createProperties("dark_time_wood_boat"))
    );

    /**
     * Dark Time Wood Chest Boat - Chest boat crafted from Dark Time Wood Boat + Chest.
     * Task: T269 [US1] Create Dark Time Wood Chest Boat
     */
    public static final RegistrySupplier<Item> DARK_TIME_WOOD_CHEST_BOAT = ITEMS.register(
        "dark_time_wood_chest_boat",
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.DARK_TIME_WOOD, true, ChronoDawnBoatItem.createProperties("dark_time_wood_chest_boat"))
    );

    /**
     * Ancient Time Wood Boat - Boat crafted from Ancient Time Wood Planks.
     * Task: T270 [US1] Create Ancient Time Wood Boat
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_BOAT = ITEMS.register(
        "ancient_time_wood_boat",
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.ANCIENT_TIME_WOOD, false, ChronoDawnBoatItem.createProperties("ancient_time_wood_boat"))
    );

    /**
     * Ancient Time Wood Chest Boat - Chest boat crafted from Ancient Time Wood Boat + Chest.
     * Task: T270 [US1] Create Ancient Time Wood Chest Boat
     */
    public static final RegistrySupplier<Item> ANCIENT_TIME_WOOD_CHEST_BOAT = ITEMS.register(
        "ancient_time_wood_chest_boat",
        () -> new ChronoDawnBoatItem(ChronoDawnBoatType.ANCIENT_TIME_WOOD, true, ChronoDawnBoatItem.createProperties("ancient_time_wood_chest_boat"))
    );

    /**
     * Initialize item registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        ITEMS.register();
        ChronoDawn.LOGGER.info("Registered ModItems");
    }

    /**
     * Initialize spawn eggs after all entities are registered.
     * This method must be called after entity registration is complete.
     */
    public static void initializeSpawnEggs() {
        ChronoDawn.LOGGER.info("Initializing spawn eggs...");

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

        ChronoDawn.LOGGER.info("Spawn eggs initialized");
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
        if (GLIDE_FISH.isPresent()) {
            output.accept(GLIDE_FISH.get());
        }
        if (COOKED_GLIDE_FISH.isPresent()) {
            output.accept(COOKED_GLIDE_FISH.get());
        }
    }
}
