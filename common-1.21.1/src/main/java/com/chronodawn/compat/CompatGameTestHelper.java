package com.chronodawn.compat;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

/**
 * Compatibility helper for GameTestHelper API differences (Minecraft 1.21.1).
 */
public class CompatGameTestHelper {
    /**
     * Create a mock ServerPlayer in the test environment.
     * In 1.21.1, makeMockPlayer() requires a GameType argument.
     *
     * @return ServerPlayer or null if mock player cannot be cast to ServerPlayer
     */
    public static ServerPlayer makeMockServerPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        return null;
    }

    private CompatGameTestHelper() {
        // Utility class
    }
}
