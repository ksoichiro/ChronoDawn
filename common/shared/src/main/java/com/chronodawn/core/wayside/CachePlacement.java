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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Where a Time Cairn echo looks for a place to bury its cache.
 *
 * <p>Eight candidate offsets fan out around the cairn from a random start angle, each at its
 * own random distance, so a blocked direction (water, a cliff) falls through to the next one.
 * The caller tries them in order and uses the first that fits.
 */
public final class CachePlacement {
    public static final int MIN_DISTANCE = 16;
    public static final int MAX_DISTANCE = 24;
    public static final int DIRECTIONS = 8;

    private CachePlacement() {}

    /**
     * Returns {@link #DIRECTIONS} {dx, dz} offsets in search order, one per 45 degree step from a
     * start angle drawn from the seed, each at an integer distance in [minDistance, maxDistance].
     */
    public static List<int[]> candidateOffsets(long seed, int minDistance, int maxDistance) {
        Random random = new Random(seed);
        double startAngle = random.nextDouble() * 2 * Math.PI;
        List<int[]> offsets = new ArrayList<>(DIRECTIONS);
        for (int i = 0; i < DIRECTIONS; i++) {
            double angle = startAngle + i * (2 * Math.PI / DIRECTIONS);
            int distance = minDistance + random.nextInt(maxDistance - minDistance + 1);
            offsets.add(new int[] {
                (int) Math.round(Math.cos(angle) * distance),
                (int) Math.round(Math.sin(angle) * distance)
            });
        }
        return offsets;
    }
}
