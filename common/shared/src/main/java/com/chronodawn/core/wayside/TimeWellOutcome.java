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
 * What happened to a thrown item in the Time Well.
 *
 * <p>Declared from most to least important, so when one throw covers several
 * items the earliest constant is the one the player is told about.
 */
public enum TimeWellOutcome {
    EXCHANGED("message.chronodawn.time_well.answered"),
    COOLDOWN("message.chronodawn.time_well.cooldown"),
    NOT_ACCEPTED("message.chronodawn.time_well.not_accepted"),
    /** The thrower is offline or gone, so there is nobody to tell. */
    THROWER_GONE(null);

    private final String messageKey;

    TimeWellOutcome(String messageKey) {
        this.messageKey = messageKey;
    }

    /** Translation key of the message to show the thrower, or null for none. */
    public String messageKey() {
        return messageKey;
    }

    /** Returns the more important of two outcomes. A null outcome loses to anything. */
    public static TimeWellOutcome mostImportant(TimeWellOutcome a, TimeWellOutcome b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.compareTo(b) <= 0 ? a : b;
    }
}
