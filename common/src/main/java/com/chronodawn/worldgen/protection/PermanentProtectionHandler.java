package com.chronodawn.worldgen.protection;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Permanent Protection Handler
 *
 * Manages permanently protected areas that cannot be broken regardless of boss defeat status.
 * This is used for Master Clock structure walls to prevent bypassing the Ancient Gears requirement.
 *
 * Features:
 * - Register permanently protected areas with unique IDs
 * - Check if a block position is permanently protected
 * - No unprotection mechanism (permanent protection)
 * - Dimension-aware protection tracking
 *
 * Usage:
 * 1. Register protected area during structure generation (MasterClockProtectionProcessor)
 * 2. Check protection in block break events
 * 3. Protection remains active indefinitely
 *
 * Implementation: T302 - Prevent Master Clock wall bypass
 */
public class PermanentProtectionHandler {
    // Key: Dimension + UniqueID, Value: BoundingBox
    private static final Map<String, BoundingBox> PROTECTED_AREAS = new HashMap<>();

    /**
     * Register a permanently protected area.
     *
     * @param level The ServerLevel containing the protected area
     * @param area The BoundingBox defining the protected area
     * @param uniqueId A unique identifier for this protected area (e.g., structure center BlockPos)
     */
    public static void registerProtectedArea(ServerLevel level, BoundingBox area, Object uniqueId) {
        String key = makeKey(level, uniqueId);
        PROTECTED_AREAS.put(key, area);
        ChronoDawn.LOGGER.info("Registered permanent protected area: {} with bounds {}", key, area);
    }

    /**
     * Check if a block position is permanently protected.
     *
     * @param level The level to check
     * @param pos The block position to check
     * @return true if the position is in a permanently protected area
     */
    public static boolean isProtected(Level level, BlockPos pos) {
        String dimensionKey = level.dimension().location().toString();

        for (Map.Entry<String, BoundingBox> entry : PROTECTED_AREAS.entrySet()) {
            String key = entry.getKey();
            BoundingBox area = entry.getValue();

            // Check if this area belongs to the current dimension
            if (key.startsWith(dimensionKey + ":")) {
                // Check if position is inside protected area
                if (area.isInside(pos)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Create a unique key for dimension + uniqueId.
     *
     * @param level The ServerLevel
     * @param uniqueId The unique identifier (typically BlockPos of structure center)
     * @return A string key in format "dimension:uniqueId"
     */
    private static String makeKey(ServerLevel level, Object uniqueId) {
        return level.dimension().location() + ":" + uniqueId.toString();
    }

    /**
     * Reset all protection tracking (for testing or world reset).
     */
    public static void reset() {
        PROTECTED_AREAS.clear();
        ChronoDawn.LOGGER.info("Permanent Protection Handler reset");
    }
}
