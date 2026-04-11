package com.chronodawn.items.equipment;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

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
 * - Mining Level: iron (INCORRECT_FOR_IRON_TOOL)
 * - Enchantability: 14
 *
 * 1.21.2+ version: ToolMaterial record constructor takes
 * (incorrectBlocksForDrops, uses, speed, attackDamageBonus, enchantmentValue, repairItems).
 * Repair tag mirrors ClockstoneTier's placeholder of IRON_TOOL_MATERIALS; a dedicated
 * chronodawn:entropy_crystal repair tag is deferred until the Clockstone placeholder is replaced.
 */
public final class EntropyCrystalTier {
    private static final TagKey<Item> REPAIR_TAG = ItemTags.IRON_TOOL_MATERIALS;

    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL,
        450,
        6.5f,
        2.5f,
        14,
        REPAIR_TAG
    );

    private EntropyCrystalTier() {
    }
}
