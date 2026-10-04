package com.chronodawn.items.artifacts;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Rolling position/health history used by Time Tyrant's Mail (Temporal Rollback).
 *
 * <p>Kept free of Minecraft types so the window selection can be unit tested. The
 * dimension is a type parameter; the handler stores the level's dimension key.
 *
 * @param <D> dimension identifier type
 */
public final class TemporalRollbackHistory<D> {

    /**
     * A recorded state of the wearer.
     */
    public record Snapshot<D>(long gameTime, D dimension, double x, double y, double z, float health) {}

    private final long windowTicks;
    private final Deque<Snapshot<D>> snapshots = new ArrayDeque<>();

    /**
     * @param windowTicks how far back the rewind may reach
     */
    public TemporalRollbackHistory(long windowTicks) {
        this.windowTicks = windowTicks;
    }

    public void record(Snapshot<D> snapshot) {
        snapshots.addLast(snapshot);
        prune(snapshot.gameTime());
    }

    /**
     * Returns the oldest snapshot inside the window that was taken in the given
     * dimension, which is the closest one to "windowTicks ago".
     */
    public Optional<Snapshot<D>> rewindTarget(long now, D dimension) {
        prune(now);
        for (Snapshot<D> snapshot : snapshots) {
            if (snapshot.dimension().equals(dimension)) {
                return Optional.of(snapshot);
            }
        }
        return Optional.empty();
    }

    public void clear() {
        snapshots.clear();
    }

    private void prune(long now) {
        while (!snapshots.isEmpty() && snapshots.peekFirst().gameTime() < now - windowTicks) {
            snapshots.removeFirst();
        }
    }
}
