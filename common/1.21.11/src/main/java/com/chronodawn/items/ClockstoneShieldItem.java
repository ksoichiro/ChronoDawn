package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.world.item.ShieldItem;

public class ClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public ClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T1.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T1;
    }
}
