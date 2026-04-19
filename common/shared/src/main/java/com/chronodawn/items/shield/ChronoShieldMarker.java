package com.chronodawn.items.shield;

/**
 * Marker interface implemented by all ChronoDawn custom shield items.
 * Mixins use `instanceof ChronoShieldMarker` to detect our shields regardless of
 * which ShieldItem subclass is used per MC version.
 */
public interface ChronoShieldMarker {
    ChronoShieldTier getChronoShieldTier();
}
