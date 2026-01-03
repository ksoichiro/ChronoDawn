package com.chronodawn.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Enhanced Time Bread (強化された時のパン)
 *
 * An improved version of Time Bread fortified with Temporal Root.
 * Provides better nutrition and Saturation effect.
 *
 * Recipe: 3x Time Wheat + 1x Temporal Root
 * Effect: Saturation I for 5 seconds
 */
public class EnhancedTimeBreadItem extends Item {

    public EnhancedTimeBreadItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(7)
                .saturation(0.8f)
                .effect(new MobEffectInstance(MobEffects.SATURATION, 5 * 20, 0), 1.0f)
                .build();

        return new Properties().food(foodProperties);
    }
}
