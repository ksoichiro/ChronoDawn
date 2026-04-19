package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * T1 ChronoDawn shield (Era C: MC 1.20.1 — the oldest supported version).
 *
 * <p>1.20.1 predates {@code BLOCKS_ATTACKS} (1.21.5+), so blocking falls back
 * to vanilla {@link ShieldItem} built-in behavior. It also predates
 * {@code Item.Properties.setId()} (1.21.2+), so the Properties chain is minimal.
 * The {@code appendHoverText} signature uses {@link Level} as its second argument
 * (the {@code TooltipContext} record was introduced in 1.21.2).</p>
 */
public class ClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public ClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T1.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T1;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.clockstone_shield.tooltip.1"));
        tooltipComponents.add(Component.translatable("item.chronodawn.clockstone_shield.tooltip.2"));
    }
}
