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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/** Public registration point for Chrono Dawn portal activation notifications. */
public final class PortalOpenedEvents {

    private static final CopyOnWriteArrayList<PortalOpenedListener> LISTENERS = new CopyOnWriteArrayList<>();

    private PortalOpenedEvents() {}

    /**
     * Registers a listener. Listeners run in registration order.
     *
     * <p>Registering the same object more than once creates one callback per
     * registration.
     */
    public static void register(PortalOpenedListener listener) {
        LISTENERS.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Removes the first registration of the listener, if present. */
    public static void unregister(PortalOpenedListener listener) {
        LISTENERS.remove(Objects.requireNonNull(listener, "listener"));
    }

    /** Internal entry point used by the version-specific portal activation sites. */
    @ApiStatus.Internal
    public static void fire(UUID portalId, Level level, BlockPos position, PortalOpenCause cause, @Nullable Player igniter) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer igniterPlayer = igniter instanceof ServerPlayer player ? player : null;
        dispatch(new DefaultContext(portalId, serverLevel, position.immutable(), cause, igniterPlayer));
    }

    static void dispatch(PortalOpenedContext context) {
        for (PortalOpenedListener listener : LISTENERS) {
            try {
                listener.onPortalOpened(context);
            } catch (RuntimeException e) {
                ChronoDawn.LOGGER.error(
                    "Portal opened listener {} failed for portal {}",
                    listener.getClass().getName(),
                    context.portalId(),
                    e
                );
            }
        }
    }

    private record DefaultContext(
        UUID portalId,
        ServerLevel level,
        BlockPos position,
        PortalOpenCause cause,
        @Nullable ServerPlayer igniter
    ) implements PortalOpenedContext {}
}
