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

import com.chronodawn.compat.CompatBlockEntity;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Base block entity for wayside activators that every player may use once per cooldown.
 *
 * <p>1.21.6+: block entity data is read and written through ValueInput/ValueOutput.
 */
public abstract class CooldownActivatorBlockEntity extends CompatBlockEntity {
    private static final String COOLDOWNS_KEY = "PlayerCooldowns";

    private PlayerCooldownTracker cooldowns = new PlayerCooldownTracker();

    protected CooldownActivatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @return 0 if the activation was accepted and recorded, otherwise the ticks left
     */
    public long tryActivate(UUID player, long gameTime, long cooldownTicks) {
        long remaining = cooldowns.tryUse(player, gameTime, cooldownTicks);
        // Pruning can change the data even when the use is refused.
        setChanged();
        return remaining;
    }

    @Override
    public void saveData(ValueOutput output) {
        output.putString(COOLDOWNS_KEY, cooldowns.encode());
    }

    @Override
    public void loadData(ValueInput input) {
        cooldowns = PlayerCooldownTracker.decode(input.getStringOr(COOLDOWNS_KEY, ""));
    }
}
