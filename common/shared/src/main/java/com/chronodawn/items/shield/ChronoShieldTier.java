package com.chronodawn.items.shield;

public enum ChronoShieldTier {
    T1(400, 100, 0.15F, false, false),   // axe disable 5s=100ticks, no speed, no echo
    T2(600, 60, 0.15F, true,  false),    // axe disable 3s=60ticks, speed, no echo
    T3(800, 40, 0.15F, true,  true);     // axe disable 2s=40ticks, speed, echo

    public final int durability;
    public final int axeDisableTicks;
    public final float blockDelaySeconds;   // effect #1 (3 ticks = 0.15s vs vanilla 0.25s)
    public final boolean hasSpeedOnBlock;   // effect #7
    public final boolean hasTimeEcho;       // effect #12

    ChronoShieldTier(int durability, int axeDisableTicks, float blockDelaySeconds, boolean hasSpeedOnBlock, boolean hasTimeEcho) {
        this.durability = durability;
        this.axeDisableTicks = axeDisableTicks;
        this.blockDelaySeconds = blockDelaySeconds;
        this.hasSpeedOnBlock = hasSpeedOnBlock;
        this.hasTimeEcho = hasTimeEcho;
    }
}
