package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * T2 ChronoDawn shield (Era B: MC 1.21.4). Adds Speed-on-block via
 * ChronoShieldBlockingMixin which targets hurtCurrentlyUsedShield as a block-success
 * signal (BLOCKS_ATTACKS / applyItemBlocking don't exist pre-1.21.5).
 */
public class EnhancedClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public EnhancedClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T2.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T2;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.enhanced_clockstone_shield.tooltip.1"));
        tooltipComponents.add(Component.translatable("item.chronodawn.enhanced_clockstone_shield.tooltip.2"));
    }
}
