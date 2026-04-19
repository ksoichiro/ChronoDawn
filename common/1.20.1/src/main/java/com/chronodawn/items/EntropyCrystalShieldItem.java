package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * T3 ChronoDawn shield (Era C: MC 1.20.1). Adds Time Echo auto-block via
 * ChronoShieldDamageMixin targeting Player.hurt at HEAD (no hurtServer on Era C).
 */
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.entropy_crystal_shield.tooltip.1"));
        tooltipComponents.add(Component.translatable("item.chronodawn.entropy_crystal_shield.tooltip.2"));
    }
}
