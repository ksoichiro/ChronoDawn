package com.chronosphere.items.artifacts;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Skip Handler for Chronoblade
 *
 * Manages the AI skip effect applied by Chronoblade. When an entity is marked
 * for AI skip, their next attack action will be completely cancelled.
 *
 * Implementation:
 * - Stores marked entities in a concurrent map (thread-safe for server environments)
 * - AI skip is checked and consumed during entity tick
 * - Prevents entity from executing attack goals for one action cycle
 *
 * Reference: T152 - Implement AI skip on hit (25% chance)
 */
public class ChronobladeAISkipHandler {
    /**
     * Map of entities marked for AI skip.
     * Key: Entity UUID
     * Value: Tick count when AI skip was applied
     */
    private static final Map<UUID, Long> AI_SKIP_ENTITIES = new ConcurrentHashMap<>();

    /**
     * Duration in ticks for AI skip window (1 second = 20 ticks).
     * The entity's attack AI will be skipped once within this window.
     * Public for testing.
     */
    public static final int AI_SKIP_DURATION = 20;

    /**
     * Apply AI skip effect to an entity.
     * The entity's next attack action will be cancelled.
     *
     * @param entity The entity to apply AI skip to
     */
    public static void applyAISkip(LivingEntity entity) {
        if (!(entity instanceof Mob)) {
            return; // Only affects mobs
        }

        AI_SKIP_ENTITIES.put(entity.getUUID(), entity.level().getGameTime());
    }

    /**
     * Check if entity should have their AI skipped this tick.
     * Consumes the AI skip effect if active.
     *
     * @param entity The entity to check
     * @return true if AI should be skipped this tick
     */
    public static boolean shouldSkipAI(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return false;
        }

        UUID uuid = entity.getUUID();
        Long appliedTime = AI_SKIP_ENTITIES.get(uuid);

        if (appliedTime == null) {
            return false;
        }

        long currentTime = entity.level().getGameTime();
        long elapsed = currentTime - appliedTime;

        // Check if within AI skip window
        if (elapsed > AI_SKIP_DURATION) {
            // Window expired, remove from map
            AI_SKIP_ENTITIES.remove(uuid);
            return false;
        }

        // Check if mob is attempting to attack (has attack target and is close enough)
        if (mob.getTarget() != null && mob.distanceToSqr(mob.getTarget()) < 9.0) { // 3 block range
            // Consume AI skip effect (remove from map)
            AI_SKIP_ENTITIES.remove(uuid);

            // Cancel current attack goal
            mob.setTarget(null); // Clear target temporarily to cancel attack

            // Restore target after a brief delay (handled in next tick)
            // This creates the "skip" effect - mob misses one attack opportunity
            return true;
        }

        return false;
    }

    /**
     * Clean up expired AI skip entries.
     * Should be called periodically (e.g., every second) to prevent memory leaks.
     *
     * @param currentTime Current game time in ticks
     */
    public static void cleanup(long currentTime) {
        AI_SKIP_ENTITIES.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > AI_SKIP_DURATION
        );
    }
}
