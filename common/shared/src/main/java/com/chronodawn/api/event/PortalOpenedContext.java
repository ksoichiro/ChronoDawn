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
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Read-only context supplied when a Chrono Dawn portal transitions into the
 * active state.
 *
 * <p>The callback is synchronous. Values should not be retained beyond the
 * callback; copy anything an integration needs later.
 */
public interface PortalOpenedContext {

    /** Stable identifier of the physical portal, from the internal registry. */
    UUID portalId();

    /** The server level containing the portal frame. */
    ServerLevel level();

    /** The frame's bottom-left block position. */
    BlockPos position();

    /** Why the portal transitioned into the active state. */
    PortalOpenCause cause();

    /**
     * Player credited with the activation, or {@code null} when none is
     * known for this cause.
     */
    @Nullable
    ServerPlayer igniter();
}
