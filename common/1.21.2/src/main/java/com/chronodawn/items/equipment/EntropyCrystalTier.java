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
 * NeoForge 1.21.2/1.21.3 crashes at startup ("Unbound tags in registry ...
 * c:gems/...", "c:dusts/...") when a custom-namespace TagKey is used as
 * repairItems here. No fixed NeoForge release exists for 1.21.2 (upstream fix
 * neoforged/NeoForge#1651 landed at 21.3.7-beta, MC 1.21.3 only); kept on this
 * vanilla tag for both loaders on 1.21.2 until that changes.
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
