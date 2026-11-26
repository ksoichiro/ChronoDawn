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
package com.chronodawn.core.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Registry side of igniting a portal frame, shared by every version's {@code PortalIgnitionHandler}.
 *
 * <p>Igniting a frame that already has an entry reuses that entry instead of registering a second
 * one at the same position. Duplicates are harmful because the position index follows the last
 * registered entry, and after a reload the winner depends on the saved list order, so it can be
 * a stale DEACTIVATED entry. This mirrors the arrival-frame branch in {@code PortalTeleportHandler}.
 */
public final class PortalIgnitionRegistration {
    private PortalIgnitionRegistration() {
    }

    /**
     * Returns an ACTIVATED entry for the frame at {@code position}, reusing an existing one.
     *
     * <ul>
     *   <li>No entry: register a new one and activate it.
     *   <li>INACTIVE: activate it. DEACTIVATED: restore it to ACTIVATED.
     *   <li>ACTIVATED: keep it as is.
     *   <li>STABILIZED: register a new ACTIVATED entry that replaces the position index, as before.
     *       The old entry stays in the registry and saved data (see {@link PortalStateMachine}).
     * </ul>
     */
    public static PortalStateMachine registerOrReuse(
            PortalRegistry registry, ResourceKey<Level> dimension, BlockPos position) {
        PortalStateMachine existing = registry.getPortalAt(dimension, position);
        if (existing != null) {
            switch (existing.getCurrentState()) {
                case INACTIVE:
                    existing.activate();
                    return existing;
                case DEACTIVATED:
                    existing.setState(PortalState.ACTIVATED);
                    registry.markDirtyForPortal(existing.getPortalId());
                    return existing;
                case ACTIVATED:
                    return existing;
                default:
                    break;
            }
        }

        PortalStateMachine portal = new PortalStateMachine(UUID.randomUUID(), dimension, position);
        registry.registerPortal(portal);
        portal.activate();
        return portal;
    }
}
