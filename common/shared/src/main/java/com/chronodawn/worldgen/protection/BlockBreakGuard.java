package com.chronodawn.worldgen.protection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Decides whether a player may break a block, shared by the Fabric, NeoForge and Forge break event
 * handlers so the decision can be tested without a player whose isCreative() is real.
 * (GameTest's mock server player always reports creative, so the loader event path cannot
 * exercise the protected branches.)
 */
public final class BlockBreakGuard {
    public enum Verdict {
        ALLOWED,
        PERMANENTLY_PROTECTED,
        BOSS_ROOM_PROTECTED
    }

    private BlockBreakGuard() {
    }

    /**
     * @param creative whether the breaking player is in creative mode, which bypasses protection
     */
    public static Verdict check(Level level, BlockPos pos, boolean creative) {
        if (creative) {
            return Verdict.ALLOWED;
        }
        // Permanent protection wins so the player sees the message that cannot be lifted.
        if (PermanentProtectionHandler.isProtected(level, pos)) {
            return Verdict.PERMANENTLY_PROTECTED;
        }
        if (BlockProtectionHandler.isProtected(level, pos)) {
            return Verdict.BOSS_ROOM_PROTECTED;
        }
        return Verdict.ALLOWED;
    }
}
