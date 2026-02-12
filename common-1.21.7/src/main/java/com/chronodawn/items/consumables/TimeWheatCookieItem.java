package com.chronodawn.items.consumables;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;

/**
 * Time Wheat Cookie (時の小麦クッキー)
 *
 * A simple cookie made from Time Wheat and cocoa beans.
 * Fast-eating snack food.
 *
 * Recipe: 2x Time Wheat + 1x Cocoa Beans
 * Effect: None (quick snack)
 */
public class TimeWheatCookieItem extends Item {

    public TimeWheatCookieItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.4f)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .consumeSeconds(1.6f) // Fast eating speed (1.6 seconds instead of 3.2)
                                .build())
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wheat_cookie")));
    }
}
