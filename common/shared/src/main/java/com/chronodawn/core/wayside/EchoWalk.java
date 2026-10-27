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

/**
 * The path of a Time Cairn echo: a walker that leaves the cairn at a constant speed, reaches
 * the buried cache, then lingers there before the echo fades.
 *
 * <p>Pure geometry over game time so it can be unit tested. The caller turns positions into
 * particles.
 */
public final class EchoWalk {
    /** Blocks walked per game tick. */
    public static final double SPEED = 0.08;
    /** Ticks the echo stays at the end of its walk before it is finished. */
    public static final int LINGER_TICKS = 1200;

    // Guards the distance comparison against floating point noise in (ticks * SPEED).
    private static final double EPSILON = 1e-9;

    private final double startX;
    private final double startZ;
    private final double directionX;
    private final double directionZ;
    private final double totalDistance;
    private final long startGameTime;

    public EchoWalk(double startX, double startZ, double endX, double endZ, long startGameTime) {
        this.startX = startX;
        this.startZ = startZ;
        this.startGameTime = startGameTime;
        double dx = endX - startX;
        double dz = endZ - startZ;
        this.totalDistance = Math.hypot(dx, dz);
        this.directionX = totalDistance == 0.0 ? 0.0 : dx / totalDistance;
        this.directionZ = totalDistance == 0.0 ? 0.0 : dz / totalDistance;
    }

    /** The walker's {x, z} at the given game time, clamped to the start and the end. */
    public double[] positionAt(long gameTime) {
        double traveled = Math.min(Math.max(gameTime - startGameTime, 0L) * SPEED, totalDistance);
        return new double[] {startX + directionX * traveled, startZ + directionZ * traveled};
    }

    public boolean arrived(long gameTime) {
        return (gameTime - startGameTime) * SPEED >= totalDistance - EPSILON;
    }

    /** True once the walker has arrived and {@link #LINGER_TICKS} more ticks have passed. */
    public boolean finished(long gameTime) {
        return gameTime >= arrivalGameTime() + LINGER_TICKS;
    }

    public long arrivalGameTime() {
        return startGameTime + (long) Math.ceil(totalDistance / SPEED - EPSILON);
    }
}
