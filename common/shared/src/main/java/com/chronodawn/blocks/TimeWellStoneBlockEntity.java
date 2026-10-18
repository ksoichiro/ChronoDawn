/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.blocks;

import com.chronodawn.core.wayside.WellQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds per-player cooldowns for one Time Well, and the items waiting in its water.
 *
 * <p>The queue and the water position are not saved. After a reload the well sees the
 * waiting items afresh, so a wish in flight starts its answer delay again.
 */
public class TimeWellStoneBlockEntity extends CooldownActivatorBlockEntity {
    private final WellQueue queue = new WellQueue();
    private BlockPos cachedWater;

    public TimeWellStoneBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.TIME_WELL_STONE.get(), pos, state);
    }

    public WellQueue queue() {
        return queue;
    }

    /**
     * @return the water source last found beside the stone, or null when not looked up yet
     */
    public BlockPos cachedWater() {
        return cachedWater;
    }

    public void cacheWater(BlockPos water) {
        cachedWater = water;
    }

    /**
     * Releases the waiting items when the stone is removed. From 1.21.5 on this is the last
     * removal hook that still sees the block entity: the block's affectNeighborsAfterRemoval
     * runs after it is gone. Older versions have no such method, so this one is never called
     * there and the block's onRemove releases the items instead.
     */
    // No @Override, so this file compiles for versions before 1.21.5 too. No super call either,
    // as the vanilla default only drops the contents of a Container, which this is not.
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            TimeWellStoneBlock.releaseWaiting(serverLevel, this);
        }
    }
}
