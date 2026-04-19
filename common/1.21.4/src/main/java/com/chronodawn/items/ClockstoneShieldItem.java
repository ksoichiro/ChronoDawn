package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * T1 ChronoDawn shield (Era B: MC 1.21.4).
 *
 * <p>MC 1.21.4 predates the {@code BLOCKS_ATTACKS} DataComponent (1.21.5+), so shield
 * blocking relies on the vanilla {@link ShieldItem} built-in behavior. The 3-tick raise
 * (Effect #1) is not achievable here without a separate Mixin on the use-duration
 * constant; that is deliberately scoped out of the Era B port per the shield design
 * doc. See commit footer for full limitations list.</p>
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.clockstone_shield.tooltip.1"));
        tooltipComponents.add(Component.translatable("item.chronodawn.clockstone_shield.tooltip.2"));
    }
}
