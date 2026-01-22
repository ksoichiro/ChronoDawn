package com.chronodawn.items.tools;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Spatially Linked Pickaxe - Time manipulation mining tool.
 *
 * Enhanced pickaxe crafted from Enhanced Clockstone that manipulates spatial connections
 * to duplicate block drops. When mining blocks, has a 33% chance to double the drops
 * through spatial duplication.
 *
 * Properties:
 * - Mining Speed: Diamond pickaxe equivalent (8.0f)
 * - Durability: 1561 uses
 * - Mining Level: 3 (can mine diamond-level blocks)
 * - Special Effect: 33% chance to double drops on block break
 * - Compatible with Fortune enchantment (effects stack)
 *
 * Crafting Recipe:
 * - Enhanced Clockstone x3
 * - Diamond Pickaxe x1 (or sticks, depending on recipe design)
 *
 * Drop Doubling Mechanic:
 * - Implemented in BlockEventHandler.java
 * - Checks if player is holding this pickaxe
 * - 33% random chance to duplicate loot table drops
 * - Does NOT affect experience orb drops
 *
 * Reference: data-model.md (Items → Ultimate Tools → Spatially Linked Pickaxe)
 * Related: FR-014 (Spatially Linked Pickaxe drop doubling)
 */
public class SpatiallyLinkedPickaxeItem extends PickaxeItem {
    /**
     * Time Crystal repair tag for Spatially Linked Pickaxe
     * TEMPORARY: Using vanilla tag to test if tag mechanism works
     * TODO: Find proper solution for custom mod tags in NeoForge 1.21.2
     */
    private static final TagKey<Item> TIME_CRYSTAL_TAG = ItemTags.DIAMOND_TOOL_MATERIALS;

    /**
     * Custom tier for Spatially Linked Pickaxe.
     * Uses diamond-equivalent stats.
     * In 1.21.2, ToolMaterial constructor signature:
     * (TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus,
     *  int enchantmentValue, TagKey<Item> repairItems)
     */
    private static final ToolMaterial SPATIALLY_LINKED_TIER = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, // Diamond mining level
        1561, // Diamond durability
        8.0f, // Diamond mining speed
        3.0f, // Diamond attack damage bonus
        10, // Diamond enchantability
        TIME_CRYSTAL_TAG // Can be repaired with Time Crystal
    );

    public SpatiallyLinkedPickaxeItem(Properties properties) {
        // 1.21.2: PickaxeItem constructor takes (ToolMaterial, float attackDamage, float attackSpeed, Properties)
        super(SPATIALLY_LINKED_TIER, 1.0f, -2.8f, properties);
    }

    /**
     * Create default properties for Spatially Linked Pickaxe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1561)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "spatially_linked_pickaxe")));
    }

    /**
     * Check if an item is a Spatially Linked Pickaxe.
     * Used by BlockEventHandler to identify when to apply drop doubling.
     *
     * @param item Item to check
     * @return true if item is Spatially Linked Pickaxe
     */
    public static boolean isSpatiallyLinkedPickaxe(net.minecraft.world.item.Item item) {
        return item instanceof SpatiallyLinkedPickaxeItem;
    }

    /**
     * Get the drop doubling probability.
     *
     * @return Drop doubling chance (0.33 = 33%)
     */
    public static double getDropDoublingChance() {
        return 0.33;
    }
}
