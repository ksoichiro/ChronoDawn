package com.chronodawn.items.tools;

import com.chronodawn.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

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
     * Custom tier for Spatially Linked Pickaxe.
     * Uses diamond-equivalent stats.
     */
    private static final Tier SPATIALLY_LINKED_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1561; // Diamond durability
        }

        @Override
        public float getSpeed() {
            return 8.0f; // Diamond mining speed
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0f; // Diamond attack damage bonus
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 10; // Diamond enchantability
        }

        @Override
        public Ingredient getRepairIngredient() {
            // Can be repaired with Time Crystal (universal repair material)
            return Ingredient.of(ModItems.TIME_CRYSTAL.get());
        }
    };

    public SpatiallyLinkedPickaxeItem(Properties properties) {
        super(SPATIALLY_LINKED_TIER, properties.attributes(PickaxeItem.createAttributes(SPATIALLY_LINKED_TIER, 1.0f, -2.8f)));
    }

    /**
     * Create default properties for Spatially Linked Pickaxe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1561);
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
