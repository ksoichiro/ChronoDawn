package com.chronodawn.items.equipment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

/**
 * Enhanced Clockstone Armor Item - Tier 2 time-themed armor pieces.
 *
 * Advanced tier armor crafted from Enhanced Clockstone.
 * Provides diamond-comparable protection with superior enchantability.
 *
 * Armor Set:
 * - Helmet: Defense 3, Durability 308
 * - Chestplate: Defense 7, Durability 448
 * - Leggings: Defense 6, Durability 420
 * - Boots: Defense 3, Durability 364
 *
 * Total Set Defense: 19 (iron: 15, diamond: 20, clockstone: 15)
 * Toughness: 2.0f (same as diamond)
 * Enchantability: 16 (better than iron/clockstone/diamond)
 *
 * Full Set Bonus: shortens Slowness, Weakness, and Mining Fatigue by 50%
 * (stacks with ChronoDawn shields), see TimeDebuffResistance.
 *
 * Crafting Recipes:
 * - Helmet: Enhanced Clockstone x5
 * - Chestplate: Enhanced Clockstone x8
 * - Leggings: Enhanced Clockstone x7
 * - Boots: Enhanced Clockstone x4
 *
 * Reference: T252, T254 - Create Enhanced Clockstone Armor with a time debuff resistance set bonus
 *
 * Note: In 1.21.5, ArmorItem has been removed. Items now use data components
 * and Item.Properties#humanoidArmor() instead of inheritance.
 */
public class EnhancedClockstoneArmorItem extends Item {
    public EnhancedClockstoneArmorItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(ArmorType type) {
        return new Properties()
                .stacksTo(1)
                // Use humanoidArmor() to set up armor components
                .humanoidArmor(EnhancedClockstoneArmorMaterial.ENHANCED_CLOCKSTONE.value(), type);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                               Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        tooltipAdder.accept(Component.translatable("item.chronodawn.enhanced_clockstone_armor.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
