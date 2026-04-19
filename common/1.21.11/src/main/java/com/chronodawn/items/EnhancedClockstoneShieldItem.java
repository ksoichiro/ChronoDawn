package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class EnhancedClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public EnhancedClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T2.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T2;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                               Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        tooltipAdder.accept(Component.translatable("item.chronodawn.enhanced_clockstone_shield.tooltip.1"));
        tooltipAdder.accept(Component.translatable("item.chronodawn.enhanced_clockstone_shield.tooltip.2"));
    }
}
