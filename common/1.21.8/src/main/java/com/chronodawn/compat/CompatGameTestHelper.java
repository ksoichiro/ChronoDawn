package com.chronodawn.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;

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
     * Create a mock player without requiring it to be a ServerPlayer. Vanilla's mock player is a
     * plain Player, so makeMockServerPlayer returns null for it.
     */
    public static net.minecraft.world.entity.player.Player makeMockPlayer(GameTestHelper helper) {
        return helper.makeMockPlayer(GameType.SURVIVAL);
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
     * From 1.21.6, block entities load from a ValueInput, so the saved tag is wrapped in one.
     */
    public static void copyBlockEntityData(GameTestHelper helper, BlockEntity from, BlockEntity to) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        to.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries,
            from.saveWithoutMetadata(registries)));
    }

    private CompatGameTestHelper() {
        // Utility class
    }
}
