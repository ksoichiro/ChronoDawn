package com.chronodawn.compat.v1_21_2.items.consumables;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

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
                .fast()
                .build();

        return new Properties().food(foodProperties);
    }
}
