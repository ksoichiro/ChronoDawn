package com.chronodawn.client;

/**
 * Manages portal fade-in effect when teleporting between dimensions.
 *
 * This handler tracks a fade-in timer that starts when a dimension change is detected.
 * The fade alpha value decreases from 1.0 to 0.0 over the course of FADE_DURATION ticks,
 * creating a smooth black fade-in effect.
 */
public class PortalFadeHandler {
    private static int fadeInTimer = 0;
    private static final int FADE_DURATION = 20; // 1 second (20 ticks)

    /**
     * Decrements the fade timer each tick.
     * Should be called every client tick.
     */
    public static void tick() {
        if (fadeInTimer > 0) {
            fadeInTimer--;
        }
    }

    /**
     * Starts the fade-in effect.
     * Should be called when a dimension change is detected.
     */
    public static void triggerFadeIn() {
        fadeInTimer = FADE_DURATION;
    }

    /**
     * Gets the current fade alpha value.
     *
     * @return Alpha value from 1.0 (fully black) to 0.0 (fully transparent)
     */
    public static float getFadeAlpha() {
        if (fadeInTimer == 0) return 0.0f;
        return (float) fadeInTimer / FADE_DURATION; // 1.0 -> 0.0
    }
}
