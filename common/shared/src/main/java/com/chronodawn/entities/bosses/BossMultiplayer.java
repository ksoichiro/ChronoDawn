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
package com.chronodawn.entities.bosses;

/**
 * Whether a boss fight currently has enough participants to warrant the
 * multiplayer-only aggro-distribution behavior (shorter AoE cooldowns,
 * Time Tyrant's reinforcement summon). Takes a plain participant count
 * rather than {@code ServerBossEvent} so this class stays free of any
 * Minecraft type, matching {@link BossScaling}.
 */
public final class BossMultiplayer {

    private BossMultiplayer() {
    }

    public static boolean isMultiplayerEncounter(int participantCount) {
        return participantCount >= 2;
    }
}
