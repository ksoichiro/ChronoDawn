package com.chronodawn.items.consumables;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Clockwork Cookie (歯車クッキー)
 *
 * A time-themed cookie found in the ChronoDawn dimension.
 * Provides defensive effects useful for exploration and combat.
 *
 * Recipe: 2x Time Wheat + 1x Time Jam + 2x Clockwork Block → 4x Clockwork Cookie
 * Effects: Resistance I (30s) + Fire Resistance (30s)
 *
 * Properties:
 * - Nutrition: 2 hunger points (1 drumstick)
 * - Saturation: 0.4 (total 0.8)
 * - Effect: Resistance I for 30 seconds (100% chance)
 * - Effect: Fire Resistance for 30 seconds (100% chance)
 * - Eating Speed: Fast (1.6 seconds)
 *
 * Acquisition:
 * - Crafted from ChronoDawn materials (Time Wheat, Time Jam, Clockwork Block)
 *
 * Usage:
 * - Quick defensive buff for exploration
 * - Stackable portable protection
 * - Useful before boss fights or dangerous areas
 *
 * Note:
 * - All materials are obtainable in ChronoDawn dimension
 * - Clockwork Block represents the "clockwork" theme of the cookie
 */
public class ClockworkCookieItem extends Item {

    public ClockworkCookieItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockwork Cookie.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(2)              // 2 hunger points (1 drumstick)
                .saturationModifier(0.4f)  // Saturation modifier (total: 2 * 0.4 = 0.8)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(
                                                MobEffects.DAMAGE_RESISTANCE,  // Resistance I
                                                30 * 20,                       // 30 seconds (20 ticks/second)
                                                0                              // Amplifier 0 = Resistance I
                                        ),
                                        1.0f))  // 100% chance to apply effect
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(
                                                MobEffects.FIRE_RESISTANCE,    // Fire Resistance
                                                30 * 20,                       // 30 seconds (20 ticks/second)
                                                0                              // Amplifier 0 = Fire Resistance I
                                        ),
                                        1.0f))  // 100% chance to apply effect
                                .consumeSeconds(1.6f) // Fast eating speed (1.6 seconds)
                                .build());
    }
}
