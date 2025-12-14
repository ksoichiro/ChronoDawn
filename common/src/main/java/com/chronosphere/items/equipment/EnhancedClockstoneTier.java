package com.chronosphere.items.equipment;

import com.chronosphere.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Enhanced Clockstone Tier - Material tier for advanced Chronosphere equipment.
 *
 * Tier 2 equipment tier, comparable to diamond but with special time-manipulation abilities.
 * Crafted from Enhanced Clockstone + Time Crystal for superior performance.
 *
 * Stats:
 * - Durability: 1200 uses (iron: 250, diamond: 1561, clockstone: 450)
 * - Mining Speed: 7.5f (iron: 6.0f, diamond: 8.0f, clockstone: 6.5f)
 * - Attack Damage Bonus: 3.0f (iron: 2.0f, diamond: 3.0f, clockstone: 2.5f)
 * - Mining Level: 3 (can mine diamond-level blocks, same as diamond tier)
 * - Enchantability: 16 (iron: 14, diamond: 10, clockstone: 14)
 *
 * Special Ability (Sword):
 * - 25% chance to freeze enemy on hit for 2 seconds (Slowness X effect)
 *
 * Repair Material: Enhanced Clockstone
 *
 * Reference: T250-251 - Create Enhanced Clockstone equipment (Tier 2)
 */
public class EnhancedClockstoneTier implements Tier {
    public static final EnhancedClockstoneTier INSTANCE = new EnhancedClockstoneTier();

    private EnhancedClockstoneTier() {
    }

    @Override
    public int getUses() {
        return 1200; // Close to diamond (1561), much better than Tier 1 (450)
    }

    @Override
    public float getSpeed() {
        return 7.5f; // Between clockstone (6.5f) and diamond (8.0f)
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.0f; // Same as diamond
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; // Diamond mining level
    }

    @Override
    public int getEnchantmentValue() {
        return 16; // Better than iron/clockstone (14) and diamond (10)
    }

    @Override
    public Ingredient getRepairIngredient() {
        // Can be repaired with Time Crystal (universal repair material)
        return Ingredient.of(ModItems.TIME_CRYSTAL.get());
    }
}
