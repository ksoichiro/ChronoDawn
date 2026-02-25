package com.chronodawn.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * Spatially Linked Pickaxe Tier - Diamond-equivalent tier for the special pickaxe.
 *
 * Uses diamond-equivalent stats with spatial manipulation abilities.
 * The special drop-doubling effect is handled separately in BlockEventHandler.
 *
 * Stats:
 * - Durability: 1561 uses (same as diamond)
 * - Mining Speed: 8.0f (same as diamond)
 * - Attack Damage Bonus: 3.0f (standard pickaxe)
 * - Mining Level: 3 (diamond level)
 * - Enchantability: 10 (same as diamond)
 *
 * Repair Material: Diamond (using diamond tag)
 *
 * Reference: FR-014 (Spatially Linked Pickaxe drop doubling)
 */
public class SpatiallyLinkedPickaxeTier {
    private static final TagKey<Item> REPAIR_TAG = ItemTags.DIAMOND_TOOL_MATERIALS;

    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, // Diamond mining level
        1561, // Diamond durability
        8.0f, // Diamond mining speed
        3.0f, // Attack damage bonus
        10,   // Enchantability (diamond)
        REPAIR_TAG
    );
}
