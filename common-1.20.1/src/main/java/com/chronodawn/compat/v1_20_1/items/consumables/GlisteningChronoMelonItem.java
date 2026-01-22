package com.chronodawn.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Glistening Chrono Melon (きらめく時のメロン)
 *
 * A premium melon slice infused with gold, providing high saturation and absorption.
 *
 * Recipe: 1x Chrono Melon Slice + 8x Gold Nuggets
 * Effect: Absorption I for 30 seconds
 */
public class GlisteningChronoMelonItem extends Item {

    public GlisteningChronoMelonItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(2)
                .saturationMod(1.2f)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 30 * 20, 0), 1.0f)
                .build();

        return new Properties().food(foodProperties);
    }
}
