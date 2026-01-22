package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Enhanced Clockstone Tier - Material tier for advanced ChronoDawn equipment.
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
 * Repair Material: Time Crystal
 *
 * Reference: T250-251 - Create Enhanced Clockstone equipment (Tier 2)
 */
public class EnhancedClockstoneTier {
    // Time Crystal repair tag
    // TEMPORARY: Using vanilla tag to test if tag mechanism works
    // TODO: Find proper solution for custom mod tags in NeoForge 1.21.2
    private static final TagKey<Item> TIME_CRYSTAL_TAG = ItemTags.DIAMOND_TOOL_MATERIALS;

    // In 1.21.2, ToolMaterial constructor signature:
    // (TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus,
    //  int enchantmentValue, TagKey<Item> repairItems)
    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, // Diamond mining level
        1200, // Close to diamond (1561), much better than Tier 1 (450)
        7.5f, // Between clockstone (6.5f) and diamond (8.0f)
        3.0f, // Same as diamond
        16, // Better than iron/clockstone (14) and diamond (10)
        TIME_CRYSTAL_TAG // Repair ingredient as TagKey
    );
}
