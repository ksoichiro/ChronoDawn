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
package com.chronodawn.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only context supplied when a Chrono Dawn boss is defeated.
 *
 * <p>The callback is synchronous. References to the boss and damage source
 * should not be retained beyond the callback; copy any values an integration
 * needs later.
 */
public interface BossDefeatedContext {

    /** Stable namespaced ID such as {@code chronodawn:time_guardian}. */
    String bossId();

    /** The defeated boss entity. */
    LivingEntity boss();

    /** The server level where the defeat occurred. */
    ServerLevel level();

    /** Immutable snapshot of the boss's block position at dispatch. */
    BlockPos position();

    /** The source passed to the boss's death method. */
    DamageSource damageSource();

    /**
     * Player attributed with the defeat by the damage source, or {@code null}
     * when Minecraft does not attribute it to a player.
     */
    @Nullable
    ServerPlayer defeatingPlayer();
}
