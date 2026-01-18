package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.Ingredient;

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
     * Time Crystal repair tag for Chronoblade
     */
    private static final TagKey<Item> TIME_CRYSTAL_TAG = TagKey.create(
        Registries.ITEM,
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "repairs_chronoblade")
    );

    /**
     * Custom tier for Chronoblade (netherite+).
     * In 1.21.2, ToolMaterial constructor signature:
     * (TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus,
     *  int enchantmentValue, TagKey<Item> repairItems)
     */
    public static final ToolMaterial TIER = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // Netherite mining level
        2000, // More durable than netherite (2031)
        9.0f, // Faster than netherite (9.0f)
        3.5f, // Slightly stronger than netherite (4.0 base = 8.0 total damage)
        18, // Better than netherite (15)
        TIME_CRYSTAL_TAG // Can be repaired with Time Crystal
    );

    public ChronobladeItem(Properties properties) {
        super(TIER, 4, -2.4f, properties);
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
}
