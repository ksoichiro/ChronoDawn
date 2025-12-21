package com.chronodawn.core.dimension;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Core dimension logic for the ChronoDawn dimension.
 *
 * Handles:
 * - Dimension access and registration
 * - Spawn location calculation
 * - Dimension transitions
 * - Time distortion effects
 *
 * Reference: data-model.md (Dimension: ChronoDawn)
 * Task: T042 [US1] Implement dimension registry logic
 */
public class ChronoDawnDimension {

    /**
     * Get the ChronoDawn dimension key.
     *
     * @return ResourceKey for the ChronoDawn dimension
     */
    public static ResourceKey<Level> getDimensionKey() {
        return ModDimensions.CHRONOSPHERE_DIMENSION;
    }

    /**
     * Check if a given level is the ChronoDawn dimension.
     *
     * @param level Level to check
     * @return true if the level is ChronoDawn, false otherwise
     */
    public static boolean isChronoDawnDimension(Level level) {
        return level.dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION);
    }

    /**
     * Get spawn location in ChronoDawn dimension.
     *
     * Players entering ChronoDawn for the first time should spawn near the Forgotten Library.
     * For now, returns a default spawn location (can be enhanced with structure finding later).
     *
     * @param level ServerLevel of the ChronoDawn dimension
     * @return BlockPos for spawn location
     */
    public static BlockPos getSpawnLocation(ServerLevel level) {
        // Default spawn location (Y=64, near world spawn)
        // TODO: Implement structure-based spawn near Forgotten Library (T070-T072)
        BlockPos worldSpawn = level.getSharedSpawnPos();
        return new BlockPos(worldSpawn.getX(), 64, worldSpawn.getZ());
    }

    /**
     * Handle player entry to ChronoDawn dimension.
     *
     * Called when a player first enters the ChronoDawn dimension.
     * This is the entry point for dimension-specific initialization.
     *
     * @param player ServerPlayer entering the dimension
     */
    public static void onPlayerEnterDimension(ServerPlayer player) {
        ChronoDawn.LOGGER.info("Player {} entered ChronoDawn dimension", player.getName().getString());

        // Future enhancement: Track player entry for quest progression (T028)
        // Future enhancement: Trigger reversed resonance if applicable (T073-T074)
    }

    /**
     * Handle player exit from ChronoDawn dimension.
     *
     * @param player ServerPlayer leaving the dimension
     */
    public static void onPlayerLeaveDimension(ServerPlayer player) {
        ChronoDawn.LOGGER.info("Player {} left ChronoDawn dimension", player.getName().getString());

        // Future enhancement: Clear dimension-specific effects
    }

    /**
     * Initialize dimension systems.
     * Called during mod initialization.
     */
    public static void init() {
        ChronoDawn.LOGGER.info("ChronoDawn dimension logic initialized");
        // Dimension registration is handled via JSON datapacks
        // This method exists for future extension points
    }
}
