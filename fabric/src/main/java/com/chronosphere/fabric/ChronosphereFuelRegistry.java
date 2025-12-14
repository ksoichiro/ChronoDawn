package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Fuel registry for Fabric.
 * Registers all wooden items from Chronosphere as furnace fuel using tags.
 */
public class ChronosphereFuelRegistry {
    /**
     * Register all wooden items as fuel using vanilla tags.
     * This method should be called during mod initialization.
     */
    public static void register() {
        FuelRegistry registry = FuelRegistry.INSTANCE;

        // Vanilla tag-based fuel registration
        // Logs: 300 ticks (15 seconds)
        registerTagFuel(registry, "minecraft:logs", 300);

        // Planks: 300 ticks (15 seconds)
        registerTagFuel(registry, "minecraft:planks", 300);

        // Wooden Stairs: 300 ticks (15 seconds)
        registerTagFuel(registry, "minecraft:wooden_stairs", 300);

        // Wooden Slabs: 150 ticks (7.5 seconds)
        registerTagFuel(registry, "minecraft:wooden_slabs", 150);

        // Wooden Fences: 300 ticks (15 seconds)
        registerTagFuel(registry, "minecraft:wooden_fences", 300);

        // Fence Gates: 300 ticks (15 seconds)
        registerTagFuel(registry, "minecraft:fence_gates", 300);

        // Wooden Buttons: 100 ticks (5 seconds)
        registerTagFuel(registry, "minecraft:wooden_buttons", 100);

        // Wooden Pressure Plates: 300 ticks (15 seconds)
        registerTagFuel(registry, "minecraft:wooden_pressure_plates", 300);

        // Wooden Doors: 200 ticks (10 seconds)
        registerTagFuel(registry, "minecraft:wooden_doors", 200);

        // Wooden Trapdoors: 300 ticks (15 seconds)
        registerTagFuel(registry, "minecraft:wooden_trapdoors", 300);

        // Boats: 1200 ticks (60 seconds)
        registerTagFuel(registry, "minecraft:boats", 1200);

        // Chest Boats: 1200 ticks (60 seconds)
        registerTagFuel(registry, "minecraft:chest_boats", 1200);

        // Saplings: 100 ticks (5 seconds)
        registerTagFuel(registry, "minecraft:saplings", 100);

        Chronosphere.LOGGER.info("Registered fuel items for Fabric using tags");
    }

    /**
     * Helper method to register fuel for all items in a tag.
     *
     * @param registry The fuel registry instance
     * @param tagId The tag ID (e.g., "minecraft:planks")
     * @param burnTime The burn time in ticks (20 ticks = 1 second)
     */
    private static void registerTagFuel(FuelRegistry registry, String tagId, int burnTime) {
        ResourceLocation tagLocation = ResourceLocation.parse(tagId);
        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagLocation);
        registry.add(tag, burnTime);
    }
}
