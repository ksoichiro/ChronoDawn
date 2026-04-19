package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.world.item.ShieldItem;

public class EnhancedClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public EnhancedClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T2.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T2;
    }
}
