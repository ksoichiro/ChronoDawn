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

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.BossKind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Public registration point for Chrono Dawn boss defeat notifications. */
public final class BossDefeatedEvents {

    private static final CopyOnWriteArrayList<BossDefeatedListener> LISTENERS = new CopyOnWriteArrayList<>();

    private BossDefeatedEvents() {}

    /**
     * Registers a listener. Listeners run in registration order.
     *
     * <p>Registering the same object more than once creates one callback per
     * registration.
     */
    public static void register(BossDefeatedListener listener) {
        LISTENERS.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Removes the first registration of the listener, if present. */
    public static void unregister(BossDefeatedListener listener) {
        LISTENERS.remove(Objects.requireNonNull(listener, "listener"));
    }

    /** Internal entry point used by the version-specific boss entities. */
    @ApiStatus.Internal
    public static void fire(BossKind kind, LivingEntity boss, DamageSource damageSource) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }

        ServerPlayer defeatingPlayer = damageSource.getEntity() instanceof ServerPlayer player ? player : null;
        dispatch(new DefaultContext(
            kind.eventId(),
            boss,
            level,
            boss.blockPosition().immutable(),
            damageSource,
            defeatingPlayer
        ));
    }

    static void dispatch(BossDefeatedContext context) {
        for (BossDefeatedListener listener : LISTENERS) {
            try {
                listener.onBossDefeated(context);
            } catch (RuntimeException e) {
                ChronoDawn.LOGGER.error(
                    "Boss defeated listener {} failed for {}",
                    listener.getClass().getName(),
                    context.bossId(),
                    e
                );
            }
        }
    }

    private record DefaultContext(
        String bossId,
        LivingEntity boss,
        ServerLevel level,
        BlockPos position,
        DamageSource damageSource,
        @Nullable ServerPlayer defeatingPlayer
    ) implements BossDefeatedContext {}
}
