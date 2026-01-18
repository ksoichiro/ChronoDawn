package com.chronodawn.worldgen.protection;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Block Protection Handler
 *
 * Manages protected areas (boss rooms) that cannot be broken until the boss is defeated.
 *
 * Features:
 * - Register protected areas with unique IDs
 * - Check if a block position is protected
 * - Unprotect areas when bosses are defeated
 * - Dimension-aware protection tracking
 *
 * Usage:
 * 1. Register protected area when boss room is discovered/entered
 * 2. Check protection in block break events
 * 3. Unprotect area when boss is defeated
 *
 * Thread Safety (T429):
 * - Uses ConcurrentHashMap for protected areas to prevent race conditions
 * - Uses ConcurrentHashMap.newKeySet() for defeated bosses tracking
 *
 * Implementation: T224 - Boss room protection system
 * Task: T429 [Thread Safety] Fix non-thread-safe collection usage
 */
public class BlockProtectionHandler {
    // Key: Dimension + UniqueID, Value: BoundingBox
    // T429: Use ConcurrentHashMap for thread-safe access in multiplayer
    private static final Map<String, BoundingBox> PROTECTED_AREAS = new ConcurrentHashMap<>();

    // Set of defeated boss room keys (dimension + uniqueId)
    // T429: Use ConcurrentHashMap.newKeySet() for thread-safe Set
    private static final Set<String> DEFEATED_BOSSES = ConcurrentHashMap.newKeySet();

    /**
     * Register a protected area (boss room).
     *
     * @param level The ServerLevel containing the protected area
     * @param area The BoundingBox defining the protected area
     * @param uniqueId A unique identifier for this protected area (e.g., door BlockPos)
     */
    public static void registerProtectedArea(ServerLevel level, BoundingBox area, Object uniqueId) {
        String key = makeKey(level, uniqueId);
        PROTECTED_AREAS.put(key, area);
        ChronoDawn.LOGGER.info("Registered protected area: {} with bounds {}", key, area);
    }

    /**
     * Mark a boss room as defeated (unprotect the area).
     *
     * @param level The ServerLevel containing the boss room
     * @param uniqueId The unique identifier used when registering the protected area
     */
    public static void onBossDefeated(ServerLevel level, Object uniqueId) {
        String key = makeKey(level, uniqueId);
        DEFEATED_BOSSES.add(key);
        ChronoDawn.LOGGER.info("Boss defeated, unprotected area: {}", key);
    }

    /**
     * Check if a block position is protected.
     *
     * @param level The level to check
     * @param pos The block position to check
     * @return true if the position is in a protected area (boss not defeated)
     */
    public static boolean isProtected(net.minecraft.world.level.Level level, BlockPos pos) {
        String dimensionKey = level.dimension().location().toString();

        for (Map.Entry<String, BoundingBox> entry : PROTECTED_AREAS.entrySet()) {
            String key = entry.getKey();
            BoundingBox area = entry.getValue();

            // Check if this area belongs to the current dimension
            if (key.startsWith(dimensionKey + ":")) {
                // Check if boss is still alive AND position is inside protected area
                if (!DEFEATED_BOSSES.contains(key) && area.isInside(pos)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Find and unprotect the boss room containing the given position.
     * Used when a boss is defeated - automatically finds and unprotects the boss room.
     *
     * @param level The ServerLevel containing the boss
     * @param bossPos The position where the boss died
     * @return true if a protected area was found and unprotected
     */
    public static boolean onBossDefeatedAt(ServerLevel level, BlockPos bossPos) {
        String dimensionKey = level.dimension().location().toString();

        for (Map.Entry<String, BoundingBox> entry : PROTECTED_AREAS.entrySet()) {
            String key = entry.getKey();
            BoundingBox area = entry.getValue();

            // Check if this area belongs to the current dimension
            if (key.startsWith(dimensionKey + ":")) {
                // Check if boss position is inside this protected area
                if (area.isInside(bossPos)) {
                    // Mark as defeated (unprotect)
                    DEFEATED_BOSSES.add(key);
                    ChronoDawn.LOGGER.info(
                        "Boss defeated at {}! Unprotected boss room: {}",
                        bossPos, key
                    );
                    return true;
                }
            }
        }

        ChronoDawn.LOGGER.warn(
            "Boss defeated at {} but no protected area found containing this position",
            bossPos
        );
        return false;
    }

    /**
     * Create a unique key for dimension + uniqueId.
     *
     * @param level The ServerLevel
     * @param uniqueId The unique identifier (typically BlockPos of door)
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
        DEFEATED_BOSSES.clear();
        ChronoDawn.LOGGER.info("Block Protection Handler reset");
    }
}
