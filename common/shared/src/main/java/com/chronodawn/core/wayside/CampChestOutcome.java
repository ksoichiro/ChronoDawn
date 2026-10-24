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
 * What happened when a player opened a Watchmaker's Chest.
 */
public enum CampChestOutcome {
    /** The player's slots were empty and the cooldown allowed a fill, so they were filled and opened. */
    FILLED(null),
    /** The player's slots still held items, so they were opened unchanged. */
    OPENED(null),
    /** The player's slots were empty but the cooldown refused a fill, so nothing opened. */
    WAITING("message.chronodawn.watchmaker_chest.waiting");

    private final String messageKey;

    CampChestOutcome(String messageKey) {
        this.messageKey = messageKey;
    }

    /** Translation key of the action-bar message to show the player, or null for none. */
    public String messageKey() {
        return messageKey;
    }
}
