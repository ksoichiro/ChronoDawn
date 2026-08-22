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
package com.chronodawn.config;

/** Settings controlling Chrono Dawn portal progression and re-ignition. */
public record PortalSettings(
    boolean oneWayUntilStabilized,
    boolean allowReignitionBeforeStabilization
) {

    /**
     * Whether the pre-stabilization portal gate is currently in force.
     *
     * <p>Two behaviors hang off this: the per-tick sweep that removes portal blocks in
     * Chrono Dawn, and the Time Hourglass refusing to ignite a portal there. They must
     * agree — permitting re-ignition while the sweep still runs would remove a
     * deliberately lit portal on the tick after its blocks are placed, making
     * {@link #allowReignitionBeforeStabilization()} a no-op.
     *
     * <p>Arrival-side portal destruction is deliberately not routed through this
     * predicate: under one-way entry the arrival portal is always destroyed. Where
     * re-ignition is permitted it is the recovery path from that destruction, not a
     * way to avoid it.
     *
     * @param portalsUnstable whether the dimension is still awaiting the Portal Stabilizer
     * @return {@code true} when portal blocks must be swept and re-ignition refused
     */
    public boolean enforcesInstabilityGate(boolean portalsUnstable) {
        return portalsUnstable && oneWayUntilStabilized && !allowReignitionBeforeStabilization;
    }
}
