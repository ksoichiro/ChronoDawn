package com.chronodawn.items.tools;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

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
 *
 * Note: In 1.21.5, PickaxeItem has been removed. Items now use data components
 * and Item.Properties helper methods instead of inheritance.
 */
public class SpatiallyLinkedPickaxeItem extends Item {
    // Attack damage: 5.0 total (1.0 base + 4.0 bonus)
    // Diamond pickaxe: 5.0
    private static final float ATTACK_DAMAGE_BONUS = 4.0f;

    // Attack speed: 1.2 (base 4.0 - 2.8 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.8f;

    public SpatiallyLinkedPickaxeItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Spatially Linked Pickaxe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "spatially_linked_pickaxe");

        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Configure as pickaxe using ToolMaterial: material, attackDamage, attackSpeed
                .pickaxe(SpatiallyLinkedPickaxeTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }

    /**
     * Check if an item is a Spatially Linked Pickaxe.
     * Used by BlockEventHandler to identify when to apply drop doubling.
     *
     * @param item Item to check
     * @return true if item is Spatially Linked Pickaxe
     */
    public static boolean isSpatiallyLinkedPickaxe(Item item) {
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
