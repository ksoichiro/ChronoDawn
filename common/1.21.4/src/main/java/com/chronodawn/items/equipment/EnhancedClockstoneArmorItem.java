package com.chronodawn.items.equipment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.List;

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
 */
public class EnhancedClockstoneArmorItem extends ArmorItem {
    public EnhancedClockstoneArmorItem(ArmorType type, Properties properties) {
        // In 1.21.2, ArmorItem constructor expects ArmorMaterial, not Holder<ArmorMaterial>
        super(EnhancedClockstoneArmorMaterial.ENHANCED_CLOCKSTONE.value(), type, properties);
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
                .durability(type.getDurability(EnhancedClockstoneArmorMaterial.BASE_DURABILITY));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.enhanced_clockstone_armor.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
