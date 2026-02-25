package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Clockstone Pickaxe - Tier 1 time-themed mining tool.
 *
 * Basic tier tool crafted from Clockstone and Time Crystal.
 * Provides better mining performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Mining Speed: 6.5f (ClockstoneTier)
 * - Attack Damage: 1.0 (base) + 2.5 (tier bonus) = 3.5
 * - Attack Speed: -2.8 (standard pickaxe speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x3
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: tasks.md (T214)
 *
 * Note: In 1.21.5, PickaxeItem has been removed. Items now use data components
 * and Item.Properties helper methods instead of inheritance.
 */
public class ClockstonePickaxeItem extends Item {
    // Attack damage: 3.5 total (1.0 base + 2.5 bonus)
    private static final float ATTACK_DAMAGE_BONUS = 2.5f;

    // Attack speed: 1.2 (base 4.0 - 2.8 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.8f;

    public ClockstonePickaxeItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Pickaxe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_pickaxe");

        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Configure as pickaxe using ToolMaterial: material, attackDamage, attackSpeed
                .pickaxe(ClockstoneTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }
}
