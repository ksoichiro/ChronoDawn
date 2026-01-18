package com.chronodawn.items.consumables;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Time Bread - Basic crafted food item from Time Wheat.
 *
 * Provides:
 * - Nutrition: Restores 5 hunger points (2.5 drumsticks)
 * - Regeneration I effect: Health regeneration for 5 seconds
 *
 * Properties:
 * - Nutrition: 5 hunger points (2.5 drumsticks)
 * - Saturation: 0.6 (total 3.0)
 * - Effect: Regeneration I for 5 seconds
 * - Eating Speed: Normal
 *
 * Crafting:
 * - Recipe: 3x Time Wheat → 1x Time Bread
 *
 * Note:
 * - This is a basic food item (similar to vanilla bread)
 * - Provides moderate nutrition with time-themed regeneration effect
 * - Easy to craft for early-game sustenance and healing
 * - Enhanced Time Bread provides much longer Regeneration effect (10 seconds)
 *
 * Reference: spec.md (User Story 1 Enhancement, FR-035)
 * Task: T216 [US1] Add eating effect to Time Bread
 */
public class TimeBreadItem extends Item {
    public TimeBreadItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Bread.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(5)        // 5 hunger points (2.5 drumsticks)
                .saturationModifier(0.6f)  // Saturation modifier (total: 5 * 0.6 = 3.0)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(
                                                MobEffects.REGENERATION,  // Regeneration I
                                                5 * 20,                   // 5 seconds (20 ticks/second)
                                                0                         // Amplifier 0 = Regeneration I
                                        ),
                                        1.0f  // 100% chance to apply effect
                                ))
                                .build());
    }
}
