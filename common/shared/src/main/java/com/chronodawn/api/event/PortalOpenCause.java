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

/** Why a {@link PortalOpenedContext} portal transitioned into the active state. */
public enum PortalOpenCause {

    /** A player ignited a valid frame with a Time Hourglass. */
    IGNITION,

    /**
     * The mod generated a new return portal, or reactivated an existing
     * frame, while teleporting an entity through an already-active portal.
     */
    REIGNITION
}
