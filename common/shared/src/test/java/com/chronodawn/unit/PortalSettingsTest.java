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

import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.PortalSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PortalSettings} gate logic.
 *
 * <p>The gate drives both the unstable-portal block sweep and the Time Hourglass
 * re-ignition check, so these cases pin the interaction between the two settings.
 */
class PortalSettingsTest {

    @Test
    void oneWayWithoutReignition_enforcesTheGateWhileUnstable() {
        assertTrue(new PortalSettings(true, false).enforcesInstabilityGate(true));
    }

    @Test
    void defaults_enforceTheGateWhileUnstable() {
        assertTrue(ConfigDefaults.PORTAL_DEFAULTS.enforcesInstabilityGate(true));
    }

    @Test
    void stabilizedDimension_neverEnforcesTheGate() {
        for (boolean oneWay : new boolean[] {false, true}) {
            for (boolean allowReignition : new boolean[] {false, true}) {
                assertFalse(new PortalSettings(oneWay, allowReignition).enforcesInstabilityGate(false),
                    "one_way=" + oneWay + ", allow_reignition=" + allowReignition);
            }
        }
    }

    @Test
    void oneWayDisabled_neverEnforcesTheGate() {
        assertFalse(new PortalSettings(false, false).enforcesInstabilityGate(true));
        assertFalse(new PortalSettings(false, true).enforcesInstabilityGate(true));
    }

    /**
     * Regression: the sweep used to key off one_way_until_stabilized alone, so a portal
     * lit under allow_reignition_before_stabilization was removed on the next tick.
     */
    @Test
    void reignitionAllowed_liftsTheGateWhileStillUnstable() {
        assertFalse(new PortalSettings(true, true).enforcesInstabilityGate(true));
    }
}
