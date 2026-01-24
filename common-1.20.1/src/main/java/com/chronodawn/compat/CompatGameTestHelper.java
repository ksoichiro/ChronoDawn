package com.chronodawn.compat;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibility helper for GameTestHelper API differences (Minecraft 1.20.1).
 */
public class CompatGameTestHelper {
    /**
     * Create a mock ServerPlayer in the test environment.
     * In 1.20.1, makeMockPlayer() takes no arguments.
     *
     * @return ServerPlayer or null if mock player cannot be cast to ServerPlayer
     */
    public static ServerPlayer makeMockServerPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        return null;
    }

    private CompatGameTestHelper() {
        // Utility class
    }
}
