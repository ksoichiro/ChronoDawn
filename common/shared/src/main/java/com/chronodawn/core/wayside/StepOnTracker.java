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
package com.chronodawn.core.wayside;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tells a player stepping onto a plate apart from a player standing on it.
 *
 * <p>Plates report every tick while an entity is inside them, so a wayside plate
 * asks this tracker before acting. Kept in memory only: after a chunk reload a
 * player already on the plate counts as a new visit, and the cooldown keeps that
 * from paying out twice.
 */
public final class StepOnTracker {

    /** Ticks a player must be off the plate before stepping on again is a new visit. */
    public static final long STEP_OFF_TICKS = 10;

    private final Map<UUID, Long> lastSeenByPlayer = new HashMap<>();

    /**
     * Records that the player is on the plate at {@code gameTime}.
     *
     * @return true when this starts a new visit
     */
    public boolean step(UUID player, long gameTime) {
        // gameTime only runs backwards when a world is restored or copied, so drop those entries too.
        lastSeenByPlayer.values().removeIf(lastSeen -> gameTime - lastSeen > STEP_OFF_TICKS || gameTime < lastSeen);
        return lastSeenByPlayer.put(player, gameTime) == null;
    }

    public int size() {
        return lastSeenByPlayer.size();
    }
}
