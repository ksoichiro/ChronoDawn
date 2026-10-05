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
import com.chronodawn.config.TimeDistortionSettings;
import com.chronodawn.core.time.TimeDistortionPolicy;
import com.chronodawn.core.time.TimeDistortionPolicy.Band;
import com.chronodawn.core.time.TimeDistortionPolicy.Kind;
import com.chronodawn.core.time.TimeDistortionPolicy.Outcome;
import com.chronodawn.core.time.TimeDistortionPolicy.Tier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeDistortionPolicyTest {

    private static final TimeDistortionSettings DEFAULTS = ConfigDefaults.TIME_DISTORTION_DEFAULTS;

    private static TimeDistortionSettings with(
        int normal, int enhanced, boolean link, float slowBelow, float fastAbove, float veryFastAbove, int speedLevel
    ) {
        return new TimeDistortionSettings(
            true, normal, enhanced, TimeDistortionSettings.Scope.HOSTILE_MOBS,
            link, slowBelow, fastAbove, veryFastAbove, speedLevel
        );
    }

    private static Outcome slowness(int level) {
        return new Outcome(Kind.SLOWNESS, level);
    }

    private static Outcome speed(int level) {
        return new Outcome(Kind.SPEED, level);
    }

    @Test
    void band_defaultBounds_mapEachRange() {
        assertEquals(Band.SLOW, TimeDistortionPolicy.band(true, 0.67f, DEFAULTS));
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 2.0f, DEFAULTS));
        assertEquals(Band.FAST, TimeDistortionPolicy.band(true, 4.0f, DEFAULTS));
        assertEquals(Band.VERY_FAST, TimeDistortionPolicy.band(true, 5.0f, DEFAULTS));
    }

    @Test
    void band_boundaryValues_belongToHigherBand() {
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 1.0f, DEFAULTS));
        assertEquals(Band.FAST, TimeDistortionPolicy.band(true, 3.0f, DEFAULTS));
        assertEquals(Band.VERY_FAST, TimeDistortionPolicy.band(true, 4.5f, DEFAULTS));
    }

    @Test
    void band_linkDisabled_isAlwaysNormal() {
        TimeDistortionSettings unlinked = with(3, 4, false, 1.0f, 3.0f, 4.5f, 1);
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 0.5f, unlinked));
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 10.0f, unlinked));
    }

    @Test
    void band_timeFlowDisabled_isNormalEvenBelowSlowBound() {
        TimeDistortionSettings highSlowBound = with(3, 4, true, 1.5f, 3.0f, 4.5f, 1);
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(false, 1.0f, highSlowBound));
    }

    @Test
    void band_veryFastDisabled_fallsBackToFast() {
        TimeDistortionSettings noSpeed = with(3, 4, true, 1.0f, 3.0f, 4.5f, 0);
        assertEquals(Band.FAST, TimeDistortionPolicy.band(true, 5.0f, noSpeed));
    }

    @Test
    void outcome_defaultsBeforeEnhancement() {
        assertEquals(slowness(4), TimeDistortionPolicy.outcome(Band.SLOW, false, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(3), TimeDistortionPolicy.outcome(Band.NORMAL, false, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(2), TimeDistortionPolicy.outcome(Band.FAST, false, Tier.NORMAL, DEFAULTS));
        assertEquals(speed(1), TimeDistortionPolicy.outcome(Band.VERY_FAST, false, Tier.NORMAL, DEFAULTS));
    }

    @Test
    void outcome_defaultsAfterEnhancement() {
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.SLOW, true, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(4), TimeDistortionPolicy.outcome(Band.NORMAL, true, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(3), TimeDistortionPolicy.outcome(Band.FAST, true, Tier.NORMAL, DEFAULTS));
        assertEquals(speed(1), TimeDistortionPolicy.outcome(Band.VERY_FAST, true, Tier.NORMAL, DEFAULTS));
    }

    @Test
    void outcome_legacyLevels_clampAtFive() {
        TimeDistortionSettings legacy = with(4, 5, true, 1.0f, 3.0f, 4.5f, 1);
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.SLOW, false, Tier.NORMAL, legacy));
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.SLOW, true, Tier.NORMAL, legacy));
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.NORMAL, true, Tier.NORMAL, legacy));
    }

    @Test
    void outcome_levelBelowOne_isNone() {
        TimeDistortionSettings weakest = with(1, 1, true, 1.0f, 3.0f, 4.5f, 1);
        assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(Band.FAST, false, Tier.NORMAL, weakest));
    }

    @Test
    void outcome_resistant_isTwoBelowClampedNormalLevel() {
        TimeDistortionSettings legacy = with(4, 5, true, 1.0f, 3.0f, 4.5f, 1);
        assertEquals(slowness(3), TimeDistortionPolicy.outcome(Band.SLOW, true, Tier.RESISTANT, legacy));
    }

    @Test
    void outcome_resistant_dropsTwoSlownessLevels() {
        assertEquals(slowness(2), TimeDistortionPolicy.outcome(Band.SLOW, false, Tier.RESISTANT, DEFAULTS));
        assertEquals(slowness(1), TimeDistortionPolicy.outcome(Band.NORMAL, false, Tier.RESISTANT, DEFAULTS));
        assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(Band.FAST, false, Tier.RESISTANT, DEFAULTS));
        assertEquals(slowness(2), TimeDistortionPolicy.outcome(Band.NORMAL, true, Tier.RESISTANT, DEFAULTS));
    }

    @Test
    void outcome_resistantVeryFast_getsUnchangedSpeed() {
        assertEquals(speed(1), TimeDistortionPolicy.outcome(Band.VERY_FAST, false, Tier.RESISTANT, DEFAULTS));
    }

    @Test
    void outcome_immune_isAlwaysNone() {
        for (Band band : Band.values()) {
            assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(band, false, Tier.IMMUNE, DEFAULTS));
            assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(band, true, Tier.IMMUNE, DEFAULTS));
        }
    }

    @Test
    void outcome_amplifierIsLevelMinusOne() {
        assertEquals(2, slowness(3).amplifier());
        assertEquals(0, speed(1).amplifier());
    }

    @Test
    void entersVeryFast_onlyOnTransitionIntoBand() {
        assertTrue(TimeDistortionPolicy.entersVeryFast(2.0f, 4.8f, DEFAULTS));
        assertFalse(TimeDistortionPolicy.entersVeryFast(4.6f, 4.8f, DEFAULTS));
        assertFalse(TimeDistortionPolicy.entersVeryFast(4.8f, 2.0f, DEFAULTS));
        assertFalse(TimeDistortionPolicy.entersVeryFast(2.0f, 3.5f, DEFAULTS));
    }

    @Test
    void entersVeryFast_disabledLevel_neverTriggers() {
        TimeDistortionSettings noSpeed = with(3, 4, true, 1.0f, 3.0f, 4.5f, 0);
        assertFalse(TimeDistortionPolicy.entersVeryFast(2.0f, 5.0f, noSpeed));
    }

    @Test
    void entersVeryFast_linkDisabled_neverTriggers() {
        TimeDistortionSettings unlinked = with(3, 4, false, 1.0f, 3.0f, 4.5f, 1);
        assertFalse(TimeDistortionPolicy.entersVeryFast(2.0f, 5.0f, unlinked));
    }

    @Test
    void replaceable_ownStaleInstance_isRemoved() {
        assertTrue(TimeDistortionPolicy.isReplaceableAmbient(true, 100, 3, 2));
        assertTrue(TimeDistortionPolicy.isReplaceableAmbient(true, 40, 2, -1));
    }

    @Test
    void replaceable_sameAmplifier_isKept() {
        assertFalse(TimeDistortionPolicy.isReplaceableAmbient(true, 100, 2, 2));
    }

    @Test
    void replaceable_nonAmbient_isKept() {
        // Resonance Reversal's Speed II near its end: short remaining time but not ambient.
        assertFalse(TimeDistortionPolicy.isReplaceableAmbient(false, 50, 1, 0));
    }

    @Test
    void replaceable_longAmbient_isKept() {
        assertFalse(TimeDistortionPolicy.isReplaceableAmbient(true, 101, 1, 0));
    }
}
