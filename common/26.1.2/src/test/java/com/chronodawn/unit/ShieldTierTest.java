package com.chronodawn.unit;

import com.chronodawn.items.shield.ChronoShieldTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShieldTierTest {

    @Test
    public void all_tiers_raise_in_3_ticks() {
        assertEquals(0.15F, ChronoShieldTier.T1.blockDelaySeconds, 1e-5);
        assertEquals(0.15F, ChronoShieldTier.T2.blockDelaySeconds, 1e-5);
        assertEquals(0.15F, ChronoShieldTier.T3.blockDelaySeconds, 1e-5);
    }

    @Test
    public void tier_durability_matches_spec() {
        assertEquals(400, ChronoShieldTier.T1.durability);
        assertEquals(600, ChronoShieldTier.T2.durability);
        assertEquals(800, ChronoShieldTier.T3.durability);
    }

    @Test
    public void tier_axe_disable_matches_spec() {
        assertEquals(100, ChronoShieldTier.T1.axeDisableTicks);
        assertEquals(60, ChronoShieldTier.T2.axeDisableTicks);
        assertEquals(40, ChronoShieldTier.T3.axeDisableTicks);
    }

    @Test
    public void only_t2_and_t3_have_speed_on_block() {
        assertEquals(false, ChronoShieldTier.T1.hasSpeedOnBlock);
        assertEquals(true, ChronoShieldTier.T2.hasSpeedOnBlock);
        assertEquals(true, ChronoShieldTier.T3.hasSpeedOnBlock);
    }

    @Test
    public void only_t3_has_time_echo() {
        assertEquals(false, ChronoShieldTier.T1.hasTimeEcho);
        assertEquals(false, ChronoShieldTier.T2.hasTimeEcho);
        assertEquals(true, ChronoShieldTier.T3.hasTimeEcho);
    }
}
