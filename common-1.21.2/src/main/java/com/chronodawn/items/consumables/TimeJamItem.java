package com.chronodawn.items.consumables;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Time Jam - Crafted food item from Time Fruits and sugar.
 *
 * Provides:
 * - Nutrition: Restores 4 hunger points (2 drumsticks)
 * - Speed I effect: Movement speed boost for 60 seconds
 *
 * Properties:
 * - Nutrition: 4 hunger points (2 drumsticks)
 * - Saturation: 0.5 (total 2.0)
 * - Effect: Speed I for 60 seconds
 * - Eating Speed: Normal
 *
 * Crafting:
 * - Recipe: 4x Fruit of Time + 1x Sugar
 *
 * Note:
 * - This is a mobility-focused food item
 * - Provides moderate nutrition with long-lasting Speed effect
 * - Useful for exploration and travel in the ChronoDawn dimension
 *
 * Reference: spec.md (User Story 1 Enhancement, FR-034)
 * Task: T221 [US1] Create Time Jam item
 */
public class TimeJamItem extends Item {
    public TimeJamItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Jam.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(4)        // 4 hunger points (2 drumsticks)
                .saturationModifier(0.5f)  // Saturation modifier (total: 4 * 0.5 = 2.0)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(
                                                MobEffects.MOVEMENT_SPEED,  // Speed I
                                                60 * 20,                    // 60 seconds (20 ticks/second)
                                                0                           // Amplifier 0 = Speed I
                                        ),
                                        1.0f  // 100% chance to apply effect
                                ))
                                .build());
    }
}
