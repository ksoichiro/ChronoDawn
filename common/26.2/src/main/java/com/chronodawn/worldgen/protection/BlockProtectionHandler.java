package com.chronodawn.worldgen.protection;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.BlockProtectionData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Manages temporary boss-room protection persisted in the dimension's SavedData. */
public final class BlockProtectionHandler {
    private BlockProtectionHandler() {
    }

    public static void registerProtectedArea(ServerLevel level, BoundingBox area, Object uniqueId) {
        String key = makeKey(level, uniqueId);
        BlockProtectionData.get(level).register(key, area);
        ChronoDawn.LOGGER.debug("Registered protected area: {} with bounds {}", key, area);
    }

    public static void onBossDefeated(ServerLevel level, Object uniqueId) {
        String key = makeKey(level, uniqueId);
        BlockProtectionData.get(level).markDefeated(key);
        ChronoDawn.LOGGER.debug("Boss defeated, unprotected area: {}", key);
    }

    public static boolean isProtected(Level level, BlockPos pos) {
        return level instanceof ServerLevel serverLevel && BlockProtectionData.get(serverLevel).isProtected(pos);
    }

    public static boolean onBossDefeatedAt(ServerLevel level, BlockPos bossPos) {
        BlockProtectionData data = BlockProtectionData.get(level);
        String key = data.findAreaAt(bossPos);
        if (key == null) {
            ChronoDawn.LOGGER.warn("Boss defeated at {} but no protected area found containing this position", bossPos);
            return false;
        }
        data.markDefeated(key);
        ChronoDawn.LOGGER.debug("Boss defeated at {}! Unprotected boss room: {}", bossPos, key);
        return true;
    }

    private static String makeKey(ServerLevel level, Object uniqueId) {
        return level.dimension().identifier() + ":" + uniqueId;
    }

    /** Retained for GameTests and explicit world-reset tooling. */
    public static void reset() {
        BlockProtectionData.resetLoadedData();
        ChronoDawn.LOGGER.debug("Block Protection Handler reset");
    }
}
