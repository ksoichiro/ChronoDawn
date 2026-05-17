package com.chronodawn.items.artifacts;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Chronoblade - Ultimate Time-Manipulating Weapon
 *
 * The ultimate artifact crafted from the fragments of the defeated Time Tyrant.
 * Wields the power of time itself to disrupt enemy attack patterns.
 *
 * Properties:
 * - Attack Damage: 8.5 (player base 1.0 + tier bonus 4.0 + sword bonus 3.5, slightly above netherite)
 * - Attack Speed: -2.4 (standard sword speed)
 * - Durability: 2000 uses (superior to diamond/netherite)
 * - Enchantability: 18
 *
 * Special Ability - AI Skip:
 * - 25% chance to skip enemy's next attack AI on successful hit
 * - When triggered, enemy's next attack action is cancelled completely
 * - Provides a one-sided attack opportunity during the AI skip window
 * - Extremely powerful in combat, allowing players to chain attacks safely
 *
 * Crafting Recipe:
 * - Fragment of Stasis Core x3
 * - Eye of Chronos x1
 * - Enhanced Clockstone x2
 * - Unstable Hourglass x1
 *
 * Lore:
 * Forged from the shattered core of Time Tyrant, this blade cuts through not just
 * flesh and bone, but the very fabric of temporal causality. Those struck by it
 * find their next actions stolen from the timeline itself.
 *
 * Reference: T148-T152 - Create Chronoblade with AI skip ability
 *
 * Note: In 1.21.5, SwordItem has been removed. Items now use data components
 * and Item.Properties helper methods instead of inheritance.
 */
public class ChronobladeItem extends Item {
    /**
     * AI skip chance (25% = 0.25). Public for testing.
     */
    public static final float AI_SKIP_CHANCE = 0.25f;

    // Sword damage param; total in-game: 1.0 (player) + 4.0 (tier) + 3.5 = 8.5
    // Netherite sword: 8.0
    private static final float ATTACK_DAMAGE_BONUS = 3.5f;

    // Attack speed: 1.6 (base 4.0 - 2.4 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.4f;

    public ChronobladeItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Chronoblade item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        Identifier itemId = Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronoblade");

        return new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Configure as sword using ChronobladeTier: material, attackDamage, attackSpeed
                .sword(ChronobladeTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }

    /**
     * Called when the player successfully attacks an entity with this sword.
     * Applies AI skip effect with 25% chance.
     *
     * In 1.21.5, hurtEnemy returns void instead of boolean.
     *
     * @param stack The item stack
     * @param target The entity being attacked
     * @param attacker The entity attacking (player)
     */
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply AI skip effect with 25% chance
        if (attacker.level().getRandom().nextFloat() < AI_SKIP_CHANCE) {
            ChronobladeAISkipHandler.applyAISkip(target);
        }

        super.hurtEnemy(stack, target, attacker);
    }
}
