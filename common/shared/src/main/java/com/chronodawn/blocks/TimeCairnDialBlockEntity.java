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

import com.chronodawn.core.wayside.EchoWalk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Holds per-player cooldowns for one Time Cairn dial, plus the echo that is currently
 * walking away from it. The echo is never saved: a reload ends it.
 */
public class TimeCairnDialBlockEntity extends CooldownActivatorBlockEntity {
    @Nullable
    EchoWalk echo;
    @Nullable
    BlockPos cachePos;

    public TimeCairnDialBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.TIME_CAIRN_DIAL.get(), pos, state);
    }

    public void startEcho(EchoWalk walk, BlockPos cache) {
        this.echo = walk;
        this.cachePos = cache;
    }

    public boolean echoActive() {
        return echo != null;
    }

    public void clearEcho() {
        this.echo = null;
        this.cachePos = null;
    }
}
