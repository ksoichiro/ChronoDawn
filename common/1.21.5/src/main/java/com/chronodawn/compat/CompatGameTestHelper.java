package com.chronodawn.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Compatibility helper for GameTestHelper API differences (Minecraft 1.21.2).
 */
public class CompatGameTestHelper {
    /**
     * Create a mock ServerPlayer in the test environment.
     * In 1.21.2, makeMockPlayer() requires a GameType argument.
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

    /**
     * Fail the running test with a plain message.
     * From 1.21.5, GameTestHelper.fail() takes a Component instead of a String.
     */
    public static void fail(GameTestHelper helper, String message) {
        helper.fail(Component.literal(message));
    }

    /**
     * Save one block entity and load the result into another, as a chunk save and reload would.
     * From 1.20.5, block entity NBT is read and written with registry access.
     */
    public static void copyBlockEntityData(GameTestHelper helper, BlockEntity from, BlockEntity to) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        to.loadWithComponents(from.saveWithoutMetadata(registries), registries);
    }

    private CompatGameTestHelper() {
        // Utility class
    }
}
