package com.chronodawn.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Compatibility helper for GameTestHelper API differences (Minecraft 1.21.2).
 */
public class CompatGameTestHelper {
    /**
     * Create a real ServerPlayer that has joined the test server.
     * makeMockPlayer() returns a plain Player, so code that needs a ServerPlayer (advancements,
     * teleports) must use this. Call {@link #removeMockServerPlayer} when the test is done,
     * otherwise the player stays logged in for every later test.
     */
    // Vanilla marks makeMockServerPlayerInLevel for removal but still ships it, and it is the
    // only GameTestHelper method that returns a player who has joined the server.
    @SuppressWarnings("removal")
    public static ServerPlayer makeMockServerPlayer(GameTestHelper helper) {
        return helper.makeMockServerPlayerInLevel();
    }

    /**
     * Log out a player created by {@link #makeMockServerPlayer}.
     */
    public static void removeMockServerPlayer(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /**
     * Create a lightweight mock player that never joins the server. Use it when a plain Player
     * is enough, and {@link #makeMockServerPlayer} when the code needs a ServerPlayer.
     */
    public static net.minecraft.world.entity.player.Player makeMockPlayer(GameTestHelper helper) {
        return helper.makeMockPlayer(GameType.SURVIVAL);
    }

    /**
     * Set who threw an item, as a player's drop or toss would.
     * From 1.21.1, ItemEntity#setThrower takes the entity itself.
     */
    public static void setThrower(net.minecraft.world.entity.item.ItemEntity item,
                                  net.minecraft.world.entity.Entity thrower) {
        item.setThrower(thrower);
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
     * From 1.20.5, block entity NBT is read and written with registry access.
     */
    public static void copyBlockEntityData(GameTestHelper helper, BlockEntity from, BlockEntity to) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        to.loadWithComponents(from.saveWithoutMetadata(registries), registries);
    }

    /**
     * Load one of the mod's structure templates, as worldgen does.
     * From 26.3, ServerLevel#getStructureManager is named getStructureTemplateManager.
     */
    public static java.util.Optional<net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate>
            loadModStructureTemplate(GameTestHelper helper, String path) {
        return helper.getLevel().getStructureManager().get(
            CompatResourceLocation.create(com.chronodawn.ChronoDawn.MOD_ID, path));
    }

    /**
     * Roll one of the mod's loot tables with chest parameters and no player. Rolling directly keeps
     * each result as its own stack, whereas filling a container splits and shuffles the stacks.
     * From 1.21, loot tables load from the server's reloadable registries.
     *
     * @param path the table path under data/chronodawn/loot_table(s), such as "chests/watchmaker_camp"
     */
    public static java.util.List<net.minecraft.world.item.ItemStack> rollModLootTable(
            net.minecraft.server.level.ServerLevel level, String path) {
        net.minecraft.world.level.storage.loot.LootTable table = level.getServer().reloadableRegistries().getLootTable(
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                CompatResourceLocation.create(com.chronodawn.ChronoDawn.MOD_ID, path)));
        net.minecraft.world.level.storage.loot.LootParams params =
            new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                    net.minecraft.world.phys.Vec3.ZERO)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        java.util.List<net.minecraft.world.item.ItemStack> result = new java.util.ArrayList<>();
        for (net.minecraft.world.item.ItemStack stack : table.getRandomItems(params)) {
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }

    private CompatGameTestHelper() {
        // Utility class
    }
}
