package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class EntropyCrystalShieldItem extends ShieldItem implements ChronoShieldMarker {
    public EntropyCrystalShieldItem(Properties properties) {
        super(properties
            .durability(ChronoShieldTier.T3.durability)
            .rarity(Rarity.RARE)
        );
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T3;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                               Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        tooltipAdder.accept(Component.translatable("item.chronodawn.entropy_crystal_shield.tooltip.1"));
        tooltipAdder.accept(Component.translatable("item.chronodawn.entropy_crystal_shield.tooltip.2"));
    }
}
