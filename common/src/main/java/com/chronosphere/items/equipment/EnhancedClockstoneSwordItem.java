package com.chronosphere.items.equipment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/**
 * Enhanced Clockstone Sword - Tier 2 time-themed weapon with freeze effect.
 *
 * Advanced tier weapon crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent performance with special time-manipulation ability.
 *
 * Properties:
 * - Attack Damage: 7.0 (base 3.0 + tier bonus 3.0, same as diamond sword)
 * - Attack Speed: -2.4 (standard sword speed)
 * - Durability: 1200 uses (EnhancedClockstoneTier)
 * - Enchantability: 16
 *
 * Special Ability:
 * - 25% chance to freeze enemy on hit for 2 seconds (Slowness X effect)
 * - Freeze effect applied on successful attack hit
 *
 * Crafting Recipe:
 * - Enhanced Clockstone x2
 * - Time Crystal x1
 * - Stick x1
 *
 * Reference: T250, T254 - Create Enhanced Clockstone Sword with freeze effect
 */
public class EnhancedClockstoneSwordItem extends SwordItem {
    /**
     * Freeze effect chance (25% = 0.25).
     */
    private static final float FREEZE_CHANCE = 0.25f;

    /**
     * Freeze effect duration in ticks (2 seconds = 40 ticks).
     */
    private static final int FREEZE_DURATION = 40;

    /**
     * Freeze effect amplifier (Slowness X = level 9 in code).
     * Level 9 = 90% movement speed reduction (nearly frozen).
     */
    private static final int FREEZE_AMPLIFIER = 9; // Slowness X

    public EnhancedClockstoneSwordItem(Properties properties) {
        super(EnhancedClockstoneTier.INSTANCE, properties.attributes(SwordItem.createAttributes(EnhancedClockstoneTier.INSTANCE, 3, -2.4f)));
    }

    /**
     * Create default properties for Enhanced Clockstone Sword item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1200);
    }

    /**
     * Called when the player successfully attacks an entity with this sword.
     * Applies freeze effect (Slowness X) with 25% chance.
     *
     * @param stack The item stack
     * @param target The entity being attacked
     * @param attacker The entity attacking (player)
     * @return true to allow the attack
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply freeze effect with 25% chance
        if (attacker.level().getRandom().nextFloat() < FREEZE_CHANCE) {
            target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, // Slowness effect
                FREEZE_DURATION,              // Duration (2 seconds = 40 ticks)
                FREEZE_AMPLIFIER,             // Amplifier (Slowness X)
                false,                        // Ambient (particle visibility)
                true                          // Show particles
            ));
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}
