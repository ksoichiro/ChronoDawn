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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Holds items that landed in the Time Well until it is time to answer them.
 *
 * <p>Kept in memory only: items still queued after a chunk reload are simply
 * offered again and start waiting from scratch.
 */
public final class WellQueue {

    /** Ticks an item waits in the well before it is answered. */
    public static final long ANSWER_DELAY_TICKS = 40;

    /** An item waiting in the well, with the player who threw it. */
    public record Entry(UUID item, UUID thrower, long firstSeen) {
    }

    // LinkedHashMap keeps first-seen order, which is the order entries are answered in.
    private final Map<UUID, Entry> entries = new LinkedHashMap<>();

    /** Queues the item, keeping the original time and thrower if it is already queued. */
    public void offer(UUID item, UUID thrower, long gameTime) {
        entries.putIfAbsent(item, new Entry(item, thrower, gameTime));
    }

    public boolean contains(UUID item) {
        return entries.containsKey(item);
    }

    /** Removes and returns the entries that have waited long enough, in first-seen order. */
    public List<Entry> due(long gameTime) {
        List<Entry> result = new ArrayList<>();
        Iterator<Entry> it = entries.values().iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            if (gameTime - entry.firstSeen() >= ANSWER_DELAY_TICKS) {
                result.add(entry);
                it.remove();
            }
        }
        return result;
    }

    public void forget(UUID item) {
        entries.remove(item);
    }

    public int size() {
        return entries.size();
    }
}
