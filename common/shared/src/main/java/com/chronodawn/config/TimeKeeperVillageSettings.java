package com.chronodawn.config;

/**
 * Configures the Time Keeper Village placed near the first Chrono Dawn entry.
 *
 * <p>These values intentionally describe runtime placement rather than a vanilla
 * structure set, so they remain valid if the placement implementation changes.
 */
public record TimeKeeperVillageSettings(
    boolean enabled,
    int preferredMinDistance,
    int preferredMaxDistance,
    int maxDistance,
    int timeKeeperCount,
    String templateId,
    String lootTableId
) {}
