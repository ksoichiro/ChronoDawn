package com.chronodawn.items.consumables;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumable;

/**
 * Golden Time Wheat (黄金の時の小麦)
 *
 * Time Wheat infused with gold, providing powerful effects.
 * Similar to Golden Apple but for wheat.
 *
 * Recipe: 1x Time Wheat + 8x Gold Ingots
 * Effects: Regeneration II (10s) + Absorption II (2min)
 */
public class GoldenTimeWheatItem extends Item {

    public GoldenTimeWheatItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(4)
                .saturationModifier(1.2f)
                .alwaysEdible()
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 1),
                                        1.0f))
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.ABSORPTION, 120 * 20, 1),
                                        1.0f))
                                .build());
    }
}
