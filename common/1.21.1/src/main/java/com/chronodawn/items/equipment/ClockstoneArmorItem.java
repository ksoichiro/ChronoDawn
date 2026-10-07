package com.chronodawn.items.equipment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Clockstone Armor Item - Tier 1 time-themed armor pieces.
 *
 * Basic tier armor crafted from Clockstone.
 * Provides better protection than iron equipment but below diamond tier.
 *
 * Armor Set:
 * - Helmet: Defense 2, Durability 220
 * - Chestplate: Defense 6, Durability 320
 * - Leggings: Defense 5, Durability 300
 * - Boots: Defense 2, Durability 260
 *
 * Total Set Defense: 15 (same as iron)
 * Toughness: 1.0f (better than iron's 0.0f)
 * Enchantability: 14 (same as iron)
 *
 * Full Set Bonus: shortens Slowness by 25% (stacks with ChronoDawn shields),
 * see TimeDebuffResistance.
 *
 * Crafting Recipes:
 * - Helmet: Clockstone x5
 * - Chestplate: Clockstone x8
 * - Leggings: Clockstone x7
 * - Boots: Clockstone x4
 *
 * Reference: tasks.md (T215)
 */
public class ClockstoneArmorItem extends ArmorItem {
    public ClockstoneArmorItem(Type type, Properties properties) {
        super(ClockstoneArmorMaterial.CLOCKSTONE, type, properties);
    }

    /**
     * Create default properties for Clockstone Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(Type type) {
        return new Properties()
                .stacksTo(1)
                .durability(type.getDurability(20)); // Durability multiplier: 20
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.clockstone_armor.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
