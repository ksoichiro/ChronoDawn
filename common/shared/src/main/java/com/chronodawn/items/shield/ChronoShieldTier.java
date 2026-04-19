package com.chronodawn.items.shield;

public enum ChronoShieldTier {
    T1(400, 100, false, false),   // axe disable 5s=100ticks, no speed, no echo
    T2(600, 60, true,  false),    // axe disable 3s=60ticks, speed, no echo
    T3(800, 40, true,  true);     // axe disable 2s=40ticks, speed, echo

    public final int durability;
    public final int axeDisableTicks;
    public final boolean hasSpeedOnBlock;   // effect #7
    public final boolean hasTimeEcho;       // effect #12

    ChronoShieldTier(int durability, int axeDisableTicks, boolean hasSpeedOnBlock, boolean hasTimeEcho) {
        this.durability = durability;
        this.axeDisableTicks = axeDisableTicks;
        this.hasSpeedOnBlock = hasSpeedOnBlock;
        this.hasTimeEcho = hasTimeEcho;
    }
}
