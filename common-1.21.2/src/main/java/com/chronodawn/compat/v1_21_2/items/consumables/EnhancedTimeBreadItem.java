package com.chronodawn.compat.v1_21_2.items.consumables;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumable;

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
                .saturationModifier(0.8f)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.SATURATION, 5 * 20, 0),
                                        1.0f))
                                .build());
    }
}
