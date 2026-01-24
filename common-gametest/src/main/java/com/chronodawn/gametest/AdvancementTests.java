package com.chronodawn.gametest;

import com.chronodawn.compat.CompatAdvancementHelper;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
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
 * - Advancement parent (verifies parent advancement references are valid)
 * - Advancement isolation (verifies granting child does not grant parent)
 */
public final class AdvancementTests {

    private AdvancementTests() {
        // Utility class
    }

    /**
     * Advancement specification: advancement ID + expected parent ID.
     */
    public record AdvancementSpec(
        String id,
        String parentId
    ) {
        public AdvancementSpec(String id) {
            this(id, null);
        }
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
                    ResourceLocation advId = CompatResourceLocation.create("chronodawn", spec.id());
                    if (CompatAdvancementHelper.advancementExists(server, advId)) {
                        helper.succeed();
                    } else {
                        helper.fail("Advancement not loaded: chronodawn:" + spec.id());
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
                        // MockPlayer is not a ServerPlayer; skip test
                        helper.succeed();
                        return;
                    }
                    ResourceLocation advId = CompatResourceLocation.create("chronodawn", spec.id());

                    // Verify advancement exists first
                    if (!CompatAdvancementHelper.advancementExists(server, advId)) {
                        helper.fail("Advancement not found: chronodawn:" + spec.id());
                        return;
                    }

                    // Verify player doesn't have it yet
                    if (CompatAdvancementHelper.hasAdvancement(server, player, advId)) {
                        helper.fail("Player already has advancement before grant: " + spec.id());
                        return;
                    }

                    // Grant the advancement
                    boolean granted = CompatAdvancementHelper.grantAdvancement(server, player, advId);
                    if (!granted) {
                        helper.fail("Failed to grant advancement: " + spec.id());
                        return;
                    }

                    // Verify player now has it
                    if (CompatAdvancementHelper.hasAdvancement(server, player, advId)) {
                        helper.succeed();
                    } else {
                        helper.fail("Advancement was granted but hasAdvancement returned false: " + spec.id());
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying advancement parent references are valid.
     * Only tests advancements that have a non-null parent.
     */
    public static <T> List<T> generateAdvancementParentTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var spec : specs) {
            if (spec.parentId() == null) {
                continue;
            }
            String testName = "advancement_parent_" + spec.id().replace('/', '_');
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    MinecraftServer server = helper.getLevel().getServer();
                    ResourceLocation advId = CompatResourceLocation.create("chronodawn", spec.id());
                    ResourceLocation parentAdvId = CompatResourceLocation.create("chronodawn", spec.parentId());

                    // Verify the advancement itself exists
                    if (!CompatAdvancementHelper.advancementExists(server, advId)) {
                        helper.fail("Advancement not found: chronodawn:" + spec.id());
                        return;
                    }

                    // Verify the parent advancement exists
                    if (CompatAdvancementHelper.advancementExists(server, parentAdvId)) {
                        helper.succeed();
                    } else {
                        helper.fail("Parent advancement not found: chronodawn:" + spec.parentId() +
                            " (referenced by: chronodawn:" + spec.id() + ")");
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying granting a child does not grant its parent.
     */
    public static <T> List<T> generateAdvancementIsolationTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var spec : specs) {
            if (spec.parentId() == null) {
                continue;
            }
            String testName = "advancement_isolation_" + spec.id().replace('/', '_');
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    MinecraftServer server = helper.getLevel().getServer();
                    ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
                    if (player == null) {
                        // MockPlayer is not a ServerPlayer; skip test
                        helper.succeed();
                        return;
                    }
                    ResourceLocation advId = CompatResourceLocation.create("chronodawn", spec.id());
                    ResourceLocation parentAdvId = CompatResourceLocation.create("chronodawn", spec.parentId());

                    // Verify both advancements exist
                    if (!CompatAdvancementHelper.advancementExists(server, advId)) {
                        helper.fail("Advancement not found: chronodawn:" + spec.id());
                        return;
                    }
                    if (!CompatAdvancementHelper.advancementExists(server, parentAdvId)) {
                        helper.fail("Parent advancement not found: chronodawn:" + spec.parentId());
                        return;
                    }

                    // Grant the child advancement
                    CompatAdvancementHelper.grantAdvancement(server, player, advId);

                    // Verify parent is NOT automatically granted
                    if (CompatAdvancementHelper.hasAdvancement(server, player, parentAdvId)) {
                        helper.fail("Granting child advancement " + spec.id() +
                            " should not grant parent " + spec.parentId());
                    } else {
                        helper.succeed();
                    }
                });
            }));
        }
        return tests;
    }
}
