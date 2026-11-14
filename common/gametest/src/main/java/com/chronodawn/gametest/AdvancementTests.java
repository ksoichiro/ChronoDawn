package com.chronodawn.gametest;

import com.chronodawn.compat.CompatAdvancementHelper;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared advancement test generator used across all Minecraft versions.
 *
 * Generates tests for:
 * - Advancement loaded (verifies advancement JSON is loaded by server)
 * - Advancement grant (verifies granting and checking advancement via CompatAdvancementHelper)
 *
 * Parent references are validated against the JSON files by AdvancementValidationTest (JUnit).
 * A "granting a child does not grant its parent" test is intentionally absent: it only exercises
 * vanilla's grant behavior and does not depend on any ChronoDawn data.
 */
public final class AdvancementTests {

    private AdvancementTests() {
        // Utility class
    }

    /**
     * Advancement specification: advancement ID (path under the chronodawn namespace).
     */
    public record AdvancementSpec(String id) {
    }

    /**
     * Generates tests verifying advancements are loaded by the server.
     */
    public static <T> List<T> generateAdvancementLoadedTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var spec : specs) {
            String testName = "advancement_loaded_" + spec.id().replace('/', '_');
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    MinecraftServer server = helper.getLevel().getServer();
                    var advId = CompatResourceLocation.create("chronodawn", spec.id());
                    if (CompatAdvancementHelper.advancementExists(server, advId)) {
                        helper.succeed();
                    } else {
                        CompatGameTestHelper.fail(helper, "Advancement not loaded: chronodawn:" + spec.id());
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying advancement grant and check via CompatAdvancementHelper.
     */
    public static <T> List<T> generateAdvancementGrantTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var spec : specs) {
            String testName = "advancement_grant_" + spec.id().replace('/', '_');
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    MinecraftServer server = helper.getLevel().getServer();
                    ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
                    if (player == null) {
                        CompatGameTestHelper.fail(helper, "Could not create a mock ServerPlayer");
                        return;
                    }
                    try {
                        verifyGrant(helper, server, player, spec);
                    } finally {
                        CompatGameTestHelper.removeMockServerPlayer(helper, player);
                    }
                });
            }));
        }
        return tests;
    }

    private static void verifyGrant(GameTestHelper helper, MinecraftServer server, ServerPlayer player,
            AdvancementSpec spec) {
        var advId = CompatResourceLocation.create("chronodawn", spec.id());

        // Verify advancement exists first
        if (!CompatAdvancementHelper.advancementExists(server, advId)) {
            CompatGameTestHelper.fail(helper, "Advancement not found: chronodawn:" + spec.id());
            return;
        }

        // Verify player doesn't have it yet
        if (CompatAdvancementHelper.hasAdvancement(server, player, advId)) {
            CompatGameTestHelper.fail(helper, "Player already has advancement before grant: " + spec.id());
            return;
        }

        // Grant the advancement
        boolean granted = CompatAdvancementHelper.grantAdvancement(server, player, advId);
        if (!granted) {
            CompatGameTestHelper.fail(helper, "Failed to grant advancement: " + spec.id());
            return;
        }

        // Verify player now has it
        if (CompatAdvancementHelper.hasAdvancement(server, player, advId)) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "Advancement was granted but hasAdvancement returned false: " + spec.id());
        }
    }
}
