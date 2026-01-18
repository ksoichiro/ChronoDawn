package com.chronodawn.compat.v1_20_1.items.equipment;

import com.chronodawn.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Clockstone Tier - Material tier for basic ChronoDawn equipment.
 *
 * Tier 1 equipment tier, slightly better than iron but below diamond.
 * Crafted from Clockstone + Time Crystal for enhanced durability and performance.
 *
 * Stats:
 * - Durability: 450 uses (iron: 250, diamond: 1561)
 * - Mining Speed: 6.5f (iron: 6.0f, diamond: 8.0f)
 * - Attack Damage Bonus: 2.5f (iron: 2.0f, diamond: 3.0f)
 * - Mining Level: 2 (can mine iron-level blocks, same as iron tier)
 * - Enchantability: 14 (iron: 14, diamond: 10)
 *
 * Repair Material: Time Crystal
 *
 * Reference: tasks.md (T213-215)
 */
public class ClockstoneTier implements Tier {
    public static final ClockstoneTier INSTANCE = new ClockstoneTier();

    private ClockstoneTier() {
    }

    @Override
    public int getUses() {
        return 450; // Between iron (250) and diamond (1561)
    }

    @Override
    public float getSpeed() {
        return 6.5f; // Between iron (6.0f) and diamond (8.0f)
    }

    @Override
    public float getAttackDamageBonus() {
        return 2.5f; // Between iron (2.0f) and diamond (3.0f)
    }

    @Override
    public int getLevel() {
        return 2; // Iron mining level
    }

    // 1.20.1: getIncorrectBlocksForDrops() method does not exist in Tier interface

    @Override
    public int getEnchantmentValue() {
        return 14; // Same as iron
    }

    @Override
    public Ingredient getRepairIngredient() {
        // Can be repaired with Time Crystal
        return Ingredient.of(ModItems.TIME_CRYSTAL.get());
    }
}
