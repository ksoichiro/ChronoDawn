package com.chronodawn.fabric;

import com.chronodawn.ChronoDawn;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Fuel registry for Fabric 1.21.2+.
 * Registers all wooden items from ChronoDawn as furnace fuel using tags.
 *
 * Migration note: Fabric API 1.21.2 changed from FuelRegistry.INSTANCE.add()
 * to event-based FuelRegistryEvents.BUILD.register().
 */
public class ChronoDawnFuelRegistry {
    /**
     * Register all wooden items as fuel using vanilla tags.
     * This method should be called during mod initialization.
     */
    public static void register() {
        FuelRegistryEvents.BUILD.register((builder, context) -> {
            // Vanilla tag-based fuel registration
            // Logs: 300 ticks (15 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:logs")), 300);

            // Planks: 300 ticks (15 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:planks")), 300);

            // Wooden Stairs: 300 ticks (15 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wooden_stairs")), 300);

            // Wooden Slabs: 150 ticks (7.5 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wooden_slabs")), 150);

            // Wooden Fences: 300 ticks (15 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wooden_fences")), 300);

            // Fence Gates: 300 ticks (15 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:fence_gates")), 300);

            // Wooden Buttons: 100 ticks (5 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wooden_buttons")), 100);

            // Wooden Pressure Plates: 300 ticks (15 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wooden_pressure_plates")), 300);

            // Wooden Doors: 200 ticks (10 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wooden_doors")), 200);

            // Wooden Trapdoors: 300 ticks (15 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wooden_trapdoors")), 300);

            // Boats: 1200 ticks (60 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:boats")), 1200);

            // Chest Boats: 1200 ticks (60 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:chest_boats")), 1200);

            // Saplings: 100 ticks (5 seconds)
            builder.add(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:saplings")), 100);

            ChronoDawn.LOGGER.debug("Registered fuel items for Fabric 1.21.2+ using tags");
        });
    }
}
