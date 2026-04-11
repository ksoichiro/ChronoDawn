package com.chronodawn.items.equipment;

import com.chronodawn.registry.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Entropy Crystal Tier - Material tier for the Entropy Crystal Sword.
 *
 * Stats parity with Clockstone Tier (Tier 2 equipment, between iron and diamond).
 * The specialization lives on the sword itself (Entropy DoT on hit), not the tier stats.
 *
 * Stats:
 * - Durability: 450 uses
 * - Mining Speed: 6.5f
 * - Attack Damage Bonus: 2.5f
 * - Mining Level: 2 (iron)
 * - Enchantability: 14
 *
 * Repair Material: Entropy Crystal
 *
 * 1.20.1 version: Tier interface without getIncorrectBlocksForDrops.
 */
public class EntropyCrystalTier implements Tier {
    public static final EntropyCrystalTier INSTANCE = new EntropyCrystalTier();

    private EntropyCrystalTier() {
    }

    @Override
    public int getUses() {
        return 450;
    }

    @Override
    public float getSpeed() {
        return 6.5f;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2.5f;
    }

    @Override
    public int getLevel() {
        return 2;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.ENTROPY_CRYSTAL.get());
    }
}
