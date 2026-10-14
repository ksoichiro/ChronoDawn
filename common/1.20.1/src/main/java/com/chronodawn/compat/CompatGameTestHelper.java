package com.chronodawn.compat;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

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

    /**
     * Fail the running test with a plain message.
     * Before 1.21.5, GameTestHelper.fail() takes the String directly.
     */
    public static void fail(GameTestHelper helper, String message) {
        helper.fail(message);
    }

    /**
     * Save one block entity and load the result into another, as a chunk save and reload would.
     * In 1.20.1, block entity NBT needs no registry access.
     */
    public static void copyBlockEntityData(GameTestHelper helper, BlockEntity from, BlockEntity to) {
        to.load(from.saveWithoutMetadata());
    }

    private CompatGameTestHelper() {
        // Utility class
    }
}
