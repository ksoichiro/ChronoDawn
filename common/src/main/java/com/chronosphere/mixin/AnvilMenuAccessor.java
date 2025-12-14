package com.chronosphere.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor Mixin for AnvilMenu to access private fields.
 *
 * Reference: integrate-time-crystal-anvil-repair task
 */
@Mixin(AnvilMenu.class)
public interface AnvilMenuAccessor {
    @Accessor("cost")
    DataSlot getCost();

    @Accessor("repairItemCountCost")
    void setRepairItemCountCost(int count);
}
