package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;

import java.util.List;

/**
 * Enhanced Clockstone Pickaxe - Tier 2 time-themed mining tool.
 *
 * Advanced tier tool crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent mining performance with faster mining speed.
 *
 * Properties:
 * - Mining Speed: 7.5f (EnhancedClockstoneTier)
 * - Attack Damage: 1.0 (base) + 3.0 (tier bonus) = 4.0
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
    // Mining speed (between iron 6.0f and diamond 8.0f, closer to diamond)
    private static final float MINING_SPEED = 7.5f;

    // Attack damage: 4.0 total (1.0 base + 3.0 bonus)
    private static final float ATTACK_DAMAGE_BONUS = 3.0f;

    // Attack speed: 1.2 (base 4.0 - 2.8 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.8f;

    // Durability cost when hitting entities
    private static final int DURABILITY_COST_PER_HIT = 2;

    // Shield disable duration in seconds
    private static final float SHIELD_DISABLE_SECONDS = 0.0f;

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
                .durability(1200)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Mark as pickaxe-type item
                .pickaxe()
                // Add tool component for mining behavior
                .component(DataComponents.TOOL, createToolComponent())
                // Add weapon component for attack behavior
                .component(DataComponents.WEAPON, new Weapon(
                    DURABILITY_COST_PER_HIT,
                    SHIELD_DISABLE_SECONDS
                ))
                // Add attribute modifiers for attack damage and speed
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers(itemId));
    }

    /**
     * Create tool component for mining behavior.
     */
    private static Tool createToolComponent() {
        return new Tool(
            List.of(
                // Diamond-level mining for pickaxe-mineable blocks
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, MINING_SPEED)
            ),
            1.0f, // Default mining speed for non-matching blocks
            1     // Damage per block mined
        );
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
