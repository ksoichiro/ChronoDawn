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
public class ClockstoneTier {
    // Time Crystal repair tag
    // NeoForge 1.21.2/1.21.3 crashes at startup ("Unbound tags in registry ...
    // c:gems/...", "c:dusts/...") when a custom-namespace TagKey is used as
    // repairItems here. No fixed NeoForge release exists for 1.21.2 (upstream fix
    // neoforged/NeoForge#1651 landed at 21.3.7-beta, MC 1.21.3 only); kept on this
    // vanilla tag for both loaders on 1.21.2 until that changes.
    private static final TagKey<Item> TIME_CRYSTAL_TAG = ItemTags.IRON_TOOL_MATERIALS;

    // In 1.21.2, ToolMaterial constructor signature:
    // (TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus,
    //  int enchantmentValue, TagKey<Item> repairItems)
    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL, // Iron mining level
        450, // Durability: Between iron (250) and diamond (1561)
        6.5f, // Mining speed: Between iron (6.0f) and diamond (8.0f)
        2.5f, // Attack damage bonus: Between iron (2.0f) and diamond (3.0f)
        14, // Enchantability: Same as iron
        TIME_CRYSTAL_TAG // Repair ingredient as TagKey
    );
}
