package com.chronodawn.items.artifacts;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * Chronoblade Tier - Ultimate artifact tier for the Chronoblade.
 *
 * The pinnacle of time-manipulation technology, surpassing even netherite.
 * Crafted from fragments of the defeated Time Tyrant.
 *
 * Stats:
 * - Durability: 2000 uses (superior to netherite's 2031)
 * - Mining Speed: 9.0f (if used as a tool)
 * - Attack Damage Bonus: 4.0f (superior to netherite's 4.0f)
 * - Mining Level: 4 (netherite level)
 * - Enchantability: 18 (better than netherite's 15)
 *
 * Special Ability:
 * - 25% chance to skip enemy's next attack AI on successful hit
 *
 * Repair Material: Enhanced Time Crystal (using netherite tag temporarily)
 *
 * Reference: T148-T152 - Create Chronoblade with AI skip ability
 */
public class ChronobladeTier {
    // Repair ingredient tag (temporary: using netherite until custom tag is implemented)
    private static final TagKey<Item> REPAIR_TAG = ItemTags.NETHERITE_TOOL_MATERIALS;

    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // Netherite mining level
        2000, // Superior durability
        9.0f, // High mining speed
        4.0f, // Attack damage bonus (netherite level)
        18,   // High enchantability
        REPAIR_TAG
    );
}
