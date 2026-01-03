package com.chronodawn.items.artifacts;

import com.chronodawn.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/**
 * Chronoblade - Ultimate Time-Manipulating Weapon
 *
 * The ultimate artifact crafted from the fragments of the defeated Time Tyrant.
 * Wields the power of time itself to disrupt enemy attack patterns.
 *
 * Properties:
 * - Attack Damage: 8.0 (base 4.0 + tier bonus 3.5, stronger than netherite)
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
 */
public class ChronobladeItem extends SwordItem {
    /**
     * AI skip chance (25% = 0.25). Public for testing.
     */
    public static final float AI_SKIP_CHANCE = 0.25f;

    /**
     * Custom tier for Chronoblade (netherite+).
     */
    public static final ChronobladeTier TIER = new ChronobladeTier();

    public ChronobladeItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, 4, -2.4f)));
    }

    /**
     * Create default properties for Chronoblade item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(2000)
                .rarity(net.minecraft.world.item.Rarity.EPIC);
    }

    /**
     * Called when the player successfully attacks an entity with this sword.
     * Applies AI skip effect with 25% chance.
     *
     * @param stack The item stack
     * @param target The entity being attacked
     * @param attacker The entity attacking (player)
     * @return true to allow the attack
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply AI skip effect with 25% chance
        if (attacker.level().getRandom().nextFloat() < AI_SKIP_CHANCE) {
            ChronobladeAISkipHandler.applyAISkip(target);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * Custom tier for Chronoblade.
     * Superior to netherite in all aspects.
     */
    private static class ChronobladeTier implements Tier {
        @Override
        public int getUses() {
            return 2000; // More durable than netherite (2031)
        }

        @Override
        public float getSpeed() {
            return 9.0f; // Faster than netherite (9.0f)
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.5f; // Slightly stronger than netherite (4.0 base = 8.0 total damage)
        }

        @Override
        public int getLevel() {
            return 4; // Netherite mining level
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 18; // Better than netherite (15)
        }

        @Override
        public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
            // Can be repaired with Time Crystal
            return net.minecraft.world.item.crafting.Ingredient.of(ModItems.TIME_CRYSTAL.get());
        }
    }
}
