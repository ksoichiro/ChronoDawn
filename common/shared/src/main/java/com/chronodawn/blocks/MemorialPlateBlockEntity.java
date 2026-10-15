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

import com.chronodawn.core.wayside.StepOnTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Holds per-player cooldowns for one Lost Adventurer Memorial plate.
 */
public class MemorialPlateBlockEntity extends CooldownActivatorBlockEntity {
    private final StepOnTracker steps = new StepOnTracker();

    public MemorialPlateBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.MEMORIAL_PLATE.get(), pos, state);
    }

    /**
     * @return true when the player has just stepped onto the plate, false while they keep standing on it
     */
    public boolean markStep(UUID player, long gameTime) {
        return steps.step(player, gameTime);
    }
}
