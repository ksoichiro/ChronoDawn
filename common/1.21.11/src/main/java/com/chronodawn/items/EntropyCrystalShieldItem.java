package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;

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
}
