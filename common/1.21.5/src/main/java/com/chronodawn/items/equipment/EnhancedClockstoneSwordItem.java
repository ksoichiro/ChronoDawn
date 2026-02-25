package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Enhanced Clockstone Sword - Tier 2 time-themed weapon with freeze effect.
 *
 * Advanced tier weapon crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent performance with special time-manipulation ability.
 *
 * Properties:
 * - Attack Damage: 7.0 (base 1.0 + 6.0 bonus, same as diamond sword)
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
 *
 * Note: In 1.21.5, SwordItem has been removed. Items now use data components
 * and Item.Properties helper methods instead of inheritance.
 */
public class EnhancedClockstoneSwordItem extends Item {
    // Attack damage: 7.0 total (1.0 base + 6.0 bonus)
    // Diamond sword: 7.0
    private static final float ATTACK_DAMAGE_BONUS = 6.0f;

    // Attack speed: 1.6 (base 4.0 - 2.4 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.4f;

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
        super(properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Sword item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "enhanced_clockstone_sword");

        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Configure as sword using ToolMaterial: material, attackDamage, attackSpeed
                .sword(EnhancedClockstoneTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }

    /**
     * Called when the player successfully attacks an entity with this sword.
     * Applies freeze effect (Slowness X) with 25% chance.
     *
     * In 1.21.5, hurtEnemy returns void instead of boolean.
     *
     * @param stack The item stack
     * @param target The entity being attacked
     * @param attacker The entity attacking (player)
     */
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply freeze effect with 25% chance
        if (attacker.level().getRandom().nextFloat() < FREEZE_CHANCE) {
            target.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS, // Slowness effect
                FREEZE_DURATION,              // Duration (2 seconds = 40 ticks)
                FREEZE_AMPLIFIER,             // Amplifier (Slowness X)
                false,                        // Ambient (particle visibility)
                true                          // Show particles
            ));
        }

        super.hurtEnemy(stack, target, attacker);
    }
}
