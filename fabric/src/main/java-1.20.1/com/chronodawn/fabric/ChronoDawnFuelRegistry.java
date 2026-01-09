package com.chronodawn.fabric;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModItems;
import net.fabricmc.fabric.api.registry.FuelRegistry;

/**
 * Fuel registry for Fabric (1.20.1 version).
 * Registers all wooden items from ChronoDawn as furnace fuel using individual item registration.
 *
 * Note: 1.20.1 uses individual item registration instead of TagKey-based registration
 * due to FabricTagKey API limitations in Fabric API 0.92.2+1.20.1.
 */
public class ChronoDawnFuelRegistry {
    /**
     * Register all wooden items as fuel using individual item registration.
     * This method should be called during mod initialization.
     */
    public static void register() {
        FuelRegistry registry = FuelRegistry.INSTANCE;

        // === Time Wood Items ===
        // Logs: 300 ticks (15 seconds)
        registry.add(ModItems.TIME_WOOD_LOG.get(), 300);

        // Planks: 300 ticks (15 seconds)
        registry.add(ModItems.TIME_WOOD_PLANKS.get(), 300);

        // Stairs: 300 ticks (15 seconds)
        registry.add(ModItems.TIME_WOOD_STAIRS.get(), 300);

        // Slabs: 150 ticks (7.5 seconds)
        registry.add(ModItems.TIME_WOOD_SLAB.get(), 150);

        // Fences: 300 ticks (15 seconds)
        registry.add(ModItems.TIME_WOOD_FENCE.get(), 300);

        // Doors: 200 ticks (10 seconds)
        registry.add(ModItems.TIME_WOOD_DOOR.get(), 200);

        // Trapdoors: 300 ticks (15 seconds)
        registry.add(ModItems.TIME_WOOD_TRAPDOOR.get(), 300);

        // Fence Gates: 300 ticks (15 seconds)
        registry.add(ModItems.TIME_WOOD_FENCE_GATE.get(), 300);

        // Buttons: 100 ticks (5 seconds)
        registry.add(ModItems.TIME_WOOD_BUTTON.get(), 100);

        // Pressure Plates: 300 ticks (15 seconds)
        registry.add(ModItems.TIME_WOOD_PRESSURE_PLATE.get(), 300);

        // Saplings: 100 ticks (5 seconds)
        registry.add(ModItems.TIME_WOOD_SAPLING.get(), 100);

        // Boats: 1200 ticks (60 seconds)
        registry.add(ModItems.TIME_WOOD_BOAT.get(), 1200);

        // Chest Boats: 1200 ticks (60 seconds)
        registry.add(ModItems.TIME_WOOD_CHEST_BOAT.get(), 1200);

        // === Dark Time Wood Items ===
        // Logs: 300 ticks (15 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_LOG.get(), 300);

        // Planks: 300 ticks (15 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_PLANKS.get(), 300);

        // Stairs: 300 ticks (15 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_STAIRS.get(), 300);

        // Slabs: 150 ticks (7.5 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_SLAB.get(), 150);

        // Fences: 300 ticks (15 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_FENCE.get(), 300);

        // Doors: 200 ticks (10 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_DOOR.get(), 200);

        // Trapdoors: 300 ticks (15 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_TRAPDOOR.get(), 300);

        // Fence Gates: 300 ticks (15 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_FENCE_GATE.get(), 300);

        // Buttons: 100 ticks (5 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_BUTTON.get(), 100);

        // Pressure Plates: 300 ticks (15 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_PRESSURE_PLATE.get(), 300);

        // Saplings: 100 ticks (5 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_SAPLING.get(), 100);

        // Boats: 1200 ticks (60 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_BOAT.get(), 1200);

        // Chest Boats: 1200 ticks (60 seconds)
        registry.add(ModItems.DARK_TIME_WOOD_CHEST_BOAT.get(), 1200);

        // === Ancient Time Wood Items ===
        // Logs: 300 ticks (15 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_LOG.get(), 300);

        // Planks: 300 ticks (15 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_PLANKS.get(), 300);

        // Stairs: 300 ticks (15 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_STAIRS.get(), 300);

        // Slabs: 150 ticks (7.5 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_SLAB.get(), 150);

        // Fences: 300 ticks (15 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_FENCE.get(), 300);

        // Doors: 200 ticks (10 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_DOOR.get(), 200);

        // Trapdoors: 300 ticks (15 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_TRAPDOOR.get(), 300);

        // Fence Gates: 300 ticks (15 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_FENCE_GATE.get(), 300);

        // Buttons: 100 ticks (5 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_BUTTON.get(), 100);

        // Pressure Plates: 300 ticks (15 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_PRESSURE_PLATE.get(), 300);

        // Saplings: 100 ticks (5 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_SAPLING.get(), 100);

        // Boats: 1200 ticks (60 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_BOAT.get(), 1200);

        // Chest Boats: 1200 ticks (60 seconds)
        registry.add(ModItems.ANCIENT_TIME_WOOD_CHEST_BOAT.get(), 1200);

        ChronoDawn.LOGGER.info("Registered fuel items for Fabric (1.20.1) using individual item registration");
    }
}
