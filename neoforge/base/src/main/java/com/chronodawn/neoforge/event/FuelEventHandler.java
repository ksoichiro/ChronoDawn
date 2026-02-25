package com.chronodawn.neoforge.event;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

/**
 * Fuel event handler for NeoForge.
 * Registers all wooden items from ChronoDawn as furnace fuel using tags.
 */
@EventBusSubscriber(modid = ChronoDawn.MOD_ID)
public class FuelEventHandler {
    /**
     * Handle furnace fuel burn time event to register tag-based fuel values.
     *
     * @param event The furnace fuel burn time event
     */
    @SubscribeEvent
    public static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack itemStack = event.getItemStack();

        // Check each tag and set burn time
        // Logs: 300 ticks (15 seconds)
        if (isInTag(itemStack, "minecraft:logs")) {
            event.setBurnTime(300);
            return;
        }

        // Planks: 300 ticks (15 seconds)
        if (isInTag(itemStack, "minecraft:planks")) {
            event.setBurnTime(300);
            return;
        }

        // Wooden Stairs: 300 ticks (15 seconds)
        if (isInTag(itemStack, "minecraft:wooden_stairs")) {
            event.setBurnTime(300);
            return;
        }

        // Wooden Slabs: 150 ticks (7.5 seconds)
        if (isInTag(itemStack, "minecraft:wooden_slabs")) {
            event.setBurnTime(150);
            return;
        }

        // Wooden Fences: 300 ticks (15 seconds)
        if (isInTag(itemStack, "minecraft:wooden_fences")) {
            event.setBurnTime(300);
            return;
        }

        // Fence Gates: 300 ticks (15 seconds)
        if (isInTag(itemStack, "minecraft:fence_gates")) {
            event.setBurnTime(300);
            return;
        }

        // Wooden Buttons: 100 ticks (5 seconds)
        if (isInTag(itemStack, "minecraft:wooden_buttons")) {
            event.setBurnTime(100);
            return;
        }

        // Wooden Pressure Plates: 300 ticks (15 seconds)
        if (isInTag(itemStack, "minecraft:wooden_pressure_plates")) {
            event.setBurnTime(300);
            return;
        }

        // Wooden Doors: 200 ticks (10 seconds)
        if (isInTag(itemStack, "minecraft:wooden_doors")) {
            event.setBurnTime(200);
            return;
        }

        // Wooden Trapdoors: 300 ticks (15 seconds)
        if (isInTag(itemStack, "minecraft:wooden_trapdoors")) {
            event.setBurnTime(300);
            return;
        }

        // Boats: 1200 ticks (60 seconds)
        if (isInTag(itemStack, "minecraft:boats")) {
            event.setBurnTime(1200);
            return;
        }

        // Chest Boats: 1200 ticks (60 seconds)
        if (isInTag(itemStack, "minecraft:chest_boats")) {
            event.setBurnTime(1200);
            return;
        }

        // Saplings: 100 ticks (5 seconds)
        if (isInTag(itemStack, "minecraft:saplings")) {
            event.setBurnTime(100);
        }
    }

    /**
     * Helper method to check if an item is in a specific tag.
     *
     * @param itemStack The item stack to check
     * @param tagId The tag ID (e.g., "minecraft:planks")
     * @return True if the item is in the tag, false otherwise
     */
    private static boolean isInTag(ItemStack itemStack, String tagId) {
        ResourceLocation tagLocation = ResourceLocation.parse(tagId);
        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagLocation);
        return itemStack.is(tag);
    }
}
