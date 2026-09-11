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
package com.chronodawn.unit;

import com.chronodawn.entities.bosses.BossMultiplayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossMultiplayerTest {

    @Test
    void zeroOrOneParticipant_isNotMultiplayer() {
        assertFalse(BossMultiplayer.isMultiplayerEncounter(0));
        assertFalse(BossMultiplayer.isMultiplayerEncounter(1));
    }

    @Test
    void twoOrMoreParticipants_isMultiplayer() {
        assertTrue(BossMultiplayer.isMultiplayerEncounter(2));
        assertTrue(BossMultiplayer.isMultiplayerEncounter(3));
    }
}
