package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Clockstone Sword - Tier 1 time-themed weapon.
 *
 * Basic tier weapon crafted from Clockstone and Time Crystal.
 * Provides better performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Attack Damage: 6.0 (base 3.0 + tier bonus 2.5, same as iron sword damage)
 * - Attack Speed: -2.4 (standard sword speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x2
 * - Time Crystal x1
 * - Stick x1
 *
 * Reference: tasks.md (T213)
 *
 * Note: In 1.21.5, SwordItem has been removed. Items now use data components
 * and Item.Properties helper methods instead of inheritance.
 */
public class ClockstoneSwordItem extends Item {
    // Attack damage: 6.0 total (1.0 base + 5.0 bonus)
    // Iron sword: 6.0, Diamond sword: 7.0
    private static final float ATTACK_DAMAGE_BONUS = 5.0f;

    // Attack speed: 1.6 (base 4.0 - 2.4 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.4f;

    public ClockstoneSwordItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Sword item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_sword");

        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Configure as sword using ToolMaterial: material, attackDamage, attackSpeed
                // Attack damage bonus: 5.0f (total 6.0, same as iron sword)
                // Attack speed modifier: -2.4f (standard sword speed)
                .sword(ClockstoneTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }
}
