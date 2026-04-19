package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public ClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T1.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                               Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        tooltipAdder.accept(Component.translatable("item.chronodawn.clockstone_shield.tooltip.1"));
        tooltipAdder.accept(Component.translatable("item.chronodawn.clockstone_shield.tooltip.2"));
    }
}
