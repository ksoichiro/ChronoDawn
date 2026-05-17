package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Enhanced Clockstone Pickaxe - Tier 2 time-themed mining tool.
 *
 * Advanced tier tool crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent mining performance with faster mining speed.
 *
 * Properties:
 * - Mining Speed: 7.5f (EnhancedClockstoneTier)
 * - Attack Damage: 5.0 (player base 1.0 + tier bonus 3.0 + pickaxe bonus 1.0)
 * - Attack Speed: -2.8
 * - Durability: 1200 uses (EnhancedClockstoneTier)
 * - Enchantability: 16
 *
 * Crafting Recipe:
 * - Enhanced Clockstone x3
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: T251 - Create Enhanced Clockstone tools (Tier 2)
 *
 * Note: In 1.21.5, PickaxeItem has been removed. Items now use data components
 * and Item.Properties helper methods instead of inheritance.
 */
public class EnhancedClockstonePickaxeItem extends Item {
    // Pickaxe damage param; total in-game: 1.0 (player) + 3.0 (tier) + 1.0 = 5.0
    private static final float ATTACK_DAMAGE_BONUS = 1.0f;

    // Attack speed: 1.2 (base 4.0 - 2.8 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.8f;

    public EnhancedClockstonePickaxeItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Pickaxe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "enhanced_clockstone_pickaxe");

        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Configure as pickaxe using ToolMaterial: material, attackDamage, attackSpeed
                .pickaxe(EnhancedClockstoneTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }
}
