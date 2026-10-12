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
 * Per-player cooldowns for one wayside activator (a sundial, a well, ...).
 *
 * <p>Kept free of Minecraft types so it can be unit tested without bootstrapping
 * registries. The block entity that owns it persists {@link #encode()} as a single
 * string because string NBT access reads and writes the same way on every
 * supported Minecraft version.
 */
public final class PlayerCooldownTracker {

    private static final char ENTRY_SEPARATOR = ',';
    private static final char VALUE_SEPARATOR = '=';
    private static final long TICKS_PER_MINUTE = 1200L;

    private final Map<UUID, Long> lastUseByPlayer = new HashMap<>();

    /**
     * Attempts a use at {@code gameTime}.
     *
     * @return 0 if the use was accepted and recorded, otherwise the ticks left
     */
    public long tryUse(UUID player, long gameTime, long cooldownTicks) {
        prune(gameTime, cooldownTicks);
        long remaining = remainingTicks(player, gameTime, cooldownTicks);
        if (remaining == 0) {
            lastUseByPlayer.put(player, gameTime);
        }
        return remaining;
    }

    public long remainingTicks(UUID player, long gameTime, long cooldownTicks) {
        Long lastUse = lastUseByPlayer.get(player);
        if (lastUse == null || gameTime < lastUse) {
            // gameTime only runs backwards when a world is restored or copied, so do not lock players out.
            return 0;
        }
        return Math.max(0, cooldownTicks - (gameTime - lastUse));
    }

    public int size() {
        return lastUseByPlayer.size();
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, Long> entry : lastUseByPlayer.entrySet()) {
            if (sb.length() > 0) {
                sb.append(ENTRY_SEPARATOR);
            }
            sb.append(entry.getKey()).append(VALUE_SEPARATOR).append(entry.getValue());
        }
        return sb.toString();
    }

    /** Decodes {@link #encode()} output. Malformed entries are skipped, never thrown. */
    public static PlayerCooldownTracker decode(String encoded) {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        if (encoded == null || encoded.isEmpty()) {
            return tracker;
        }
        for (String entry : encoded.split(String.valueOf(ENTRY_SEPARATOR))) {
            int separator = entry.indexOf(VALUE_SEPARATOR);
            if (separator <= 0 || separator == entry.length() - 1) {
                continue;
            }
            try {
                UUID player = UUID.fromString(entry.substring(0, separator));
                long lastUse = Long.parseLong(entry.substring(separator + 1));
                tracker.lastUseByPlayer.put(player, lastUse);
            } catch (IllegalArgumentException ignored) {
                // NumberFormatException is an IllegalArgumentException, so this covers both parts.
            }
        }
        return tracker;
    }

    /** Converts remaining ticks to whole minutes for display, never returning 0 for a positive value. */
    public static long toMinutesRoundedUp(long ticks) {
        return (ticks + TICKS_PER_MINUTE - 1) / TICKS_PER_MINUTE;
    }

    private void prune(long gameTime, long cooldownTicks) {
        lastUseByPlayer.values().removeIf(lastUse -> gameTime < lastUse || gameTime - lastUse >= cooldownTicks);
    }
}
