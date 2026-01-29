package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;

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

    // Durability cost when hitting entities
    private static final int DURABILITY_COST_PER_HIT = 1;

    // Shield disable duration in seconds
    private static final float SHIELD_DISABLE_SECONDS = 5.0f;

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
                .durability(450)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Mark as sword-type item
                .sword()
                // Add weapon component for attack behavior
                .component(DataComponents.WEAPON, new Weapon(
                    DURABILITY_COST_PER_HIT,
                    SHIELD_DISABLE_SECONDS
                ))
                // Add attribute modifiers for attack damage and speed
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers(itemId));
    }

    /**
     * Create attribute modifiers for attack damage and speed.
     */
    private static ItemAttributeModifiers createAttributeModifiers(ResourceLocation itemId) {
        return ItemAttributeModifiers.builder()
                .add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                        itemId,
                        ATTACK_DAMAGE_BONUS,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                )
                .add(
                    Attributes.ATTACK_SPEED,
                    new AttributeModifier(
                        itemId,
                        ATTACK_SPEED_MODIFIER,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                )
                .build();
    }
}
