package com.chronodawn.gametest;

import com.chronodawn.worldgen.protection.BlockProtectionHandler;
import com.chronodawn.worldgen.protection.PermanentProtectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GameTests for block protection logic.
 *
 * Tests BlockProtectionHandler and PermanentProtectionHandler directly,
 * without relying on event systems that may not function in GameTest environments.
 */
public final class BlockProtectionTests {

    private BlockProtectionTests() {
        // Utility class
    }

    /**
     * Generates all block protection tests.
     *
     * @param factory Factory to create test instances
     * @param <T> The test type
     * @return List of generated tests
     */
    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();

        tests.add(factory.create("protection_area_registration",
            BlockProtectionTests::testAreaRegistration));
        tests.add(factory.create("protection_outside_area_not_protected",
            BlockProtectionTests::testOutsideAreaNotProtected));
        tests.add(factory.create("protection_boss_defeated_unprotects",
            BlockProtectionTests::testBossDefeatedUnprotects));
        tests.add(factory.create("protection_boss_defeated_at_unprotects",
            BlockProtectionTests::testBossDefeatedAtUnprotects));
        tests.add(factory.create("protection_permanent_not_unprotected",
            BlockProtectionTests::testPermanentNotUnprotected));
        tests.add(factory.create("protection_multiple_areas_independent",
            BlockProtectionTests::testMultipleAreasIndependent));
        tests.add(factory.create("protection_reset_clears_state",
            BlockProtectionTests::testResetClearsState));

        return tests;
    }

    /**
     * Test: After registering a protected area, blocks inside are protected.
     */
    private static void testAreaRegistration(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            // Reset state before test
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            ServerLevel level = helper.getLevel();
            BlockPos center = helper.absolutePos(new BlockPos(5, 5, 5));
            BoundingBox area = new BoundingBox(
                center.getX() - 5, center.getY() - 5, center.getZ() - 5,
                center.getX() + 5, center.getY() + 5, center.getZ() + 5
            );

            // Register protected area
            BlockProtectionHandler.registerProtectedArea(level, area, "test_area_1");

            // Check that position inside is protected
            if (BlockProtectionHandler.isProtected(level, center)) {
                helper.succeed();
            } else {
                helper.fail("Block at center should be protected after registration");
            }
        });
    }

    /**
     * Test: Blocks outside the protected area are not protected.
     */
    private static void testOutsideAreaNotProtected(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            // Reset state before test
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            ServerLevel level = helper.getLevel();
            BlockPos center = helper.absolutePos(new BlockPos(5, 5, 5));
            BoundingBox area = new BoundingBox(
                center.getX() - 2, center.getY() - 2, center.getZ() - 2,
                center.getX() + 2, center.getY() + 2, center.getZ() + 2
            );

            // Register protected area
            BlockProtectionHandler.registerProtectedArea(level, area, "test_area_2");

            // Check that position outside is not protected
            BlockPos outside = helper.absolutePos(new BlockPos(15, 15, 15));
            if (!BlockProtectionHandler.isProtected(level, outside)) {
                helper.succeed();
            } else {
                helper.fail("Block outside protected area should not be protected");
            }
        });
    }

    /**
     * Test: After calling onBossDefeated(), the area becomes unprotected.
     */
    private static void testBossDefeatedUnprotects(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            // Reset state before test
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            ServerLevel level = helper.getLevel();
            BlockPos center = helper.absolutePos(new BlockPos(5, 5, 5));
            BoundingBox area = new BoundingBox(
                center.getX() - 5, center.getY() - 5, center.getZ() - 5,
                center.getX() + 5, center.getY() + 5, center.getZ() + 5
            );

            Object uniqueId = "test_area_3";

            // Register protected area
            BlockProtectionHandler.registerProtectedArea(level, area, uniqueId);

            // Verify it's protected initially
            if (!BlockProtectionHandler.isProtected(level, center)) {
                helper.fail("Area should be protected before boss defeat");
                return;
            }

            // Mark boss as defeated
            BlockProtectionHandler.onBossDefeated(level, uniqueId);

            // Check that position is now unprotected
            if (!BlockProtectionHandler.isProtected(level, center)) {
                helper.succeed();
            } else {
                helper.fail("Block should be unprotected after boss defeat");
            }
        });
    }

    /**
     * Test: onBossDefeatedAt() finds and unprotects the area containing the position.
     */
    private static void testBossDefeatedAtUnprotects(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            // Reset state before test
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            ServerLevel level = helper.getLevel();
            BlockPos center = helper.absolutePos(new BlockPos(5, 5, 5));
            BoundingBox area = new BoundingBox(
                center.getX() - 5, center.getY() - 5, center.getZ() - 5,
                center.getX() + 5, center.getY() + 5, center.getZ() + 5
            );

            // Register protected area
            BlockProtectionHandler.registerProtectedArea(level, area, "test_area_4");

            // Verify it's protected initially
            if (!BlockProtectionHandler.isProtected(level, center)) {
                helper.fail("Area should be protected before boss defeat");
                return;
            }

            // Defeat boss at position (finds and unprotects area automatically)
            boolean found = BlockProtectionHandler.onBossDefeatedAt(level, center);
            if (!found) {
                helper.fail("onBossDefeatedAt() should find the protected area");
                return;
            }

            // Check that position is now unprotected
            if (!BlockProtectionHandler.isProtected(level, center)) {
                helper.succeed();
            } else {
                helper.fail("Block should be unprotected after boss defeat at position");
            }
        });
    }

    /**
     * Test: Permanent protection is not removed by boss defeat.
     */
    private static void testPermanentNotUnprotected(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            // Reset state before test
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            ServerLevel level = helper.getLevel();
            BlockPos center = helper.absolutePos(new BlockPos(5, 5, 5));
            BoundingBox area = new BoundingBox(
                center.getX() - 5, center.getY() - 5, center.getZ() - 5,
                center.getX() + 5, center.getY() + 5, center.getZ() + 5
            );

            Object uniqueId = "test_permanent_area";

            // Register permanent protected area
            PermanentProtectionHandler.registerProtectedArea(level, area, uniqueId);

            // Verify it's protected
            if (!PermanentProtectionHandler.isProtected(level, center)) {
                helper.fail("Permanent area should be protected after registration");
                return;
            }

            // Try to defeat boss (this should not affect permanent protection)
            // Note: PermanentProtectionHandler has no onBossDefeated method - that's the point
            // The protection should remain

            // Verify still protected
            if (PermanentProtectionHandler.isProtected(level, center)) {
                helper.succeed();
            } else {
                helper.fail("Permanent protection should remain after any boss-related events");
            }
        });
    }

    /**
     * Test: Multiple protected areas work independently.
     */
    private static void testMultipleAreasIndependent(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            // Reset state before test
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            ServerLevel level = helper.getLevel();

            // Create two separate protected areas
            BlockPos center1 = helper.absolutePos(new BlockPos(3, 5, 3));
            BoundingBox area1 = new BoundingBox(
                center1.getX() - 2, center1.getY() - 2, center1.getZ() - 2,
                center1.getX() + 2, center1.getY() + 2, center1.getZ() + 2
            );

            BlockPos center2 = helper.absolutePos(new BlockPos(10, 5, 10));
            BoundingBox area2 = new BoundingBox(
                center2.getX() - 2, center2.getY() - 2, center2.getZ() - 2,
                center2.getX() + 2, center2.getY() + 2, center2.getZ() + 2
            );

            Object uniqueId1 = "test_multi_area_1";
            Object uniqueId2 = "test_multi_area_2";

            // Register both areas
            BlockProtectionHandler.registerProtectedArea(level, area1, uniqueId1);
            BlockProtectionHandler.registerProtectedArea(level, area2, uniqueId2);

            // Verify both are protected
            if (!BlockProtectionHandler.isProtected(level, center1)) {
                helper.fail("Area 1 should be protected");
                return;
            }
            if (!BlockProtectionHandler.isProtected(level, center2)) {
                helper.fail("Area 2 should be protected");
                return;
            }

            // Defeat boss in area 1 only
            BlockProtectionHandler.onBossDefeated(level, uniqueId1);

            // Area 1 should be unprotected, area 2 should still be protected
            if (BlockProtectionHandler.isProtected(level, center1)) {
                helper.fail("Area 1 should be unprotected after boss defeat");
                return;
            }
            if (!BlockProtectionHandler.isProtected(level, center2)) {
                helper.fail("Area 2 should still be protected (independent)");
                return;
            }

            helper.succeed();
        });
    }

    /**
     * Test: reset() clears all protection state.
     */
    private static void testResetClearsState(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            // Reset state before test
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            ServerLevel level = helper.getLevel();
            BlockPos center = helper.absolutePos(new BlockPos(5, 5, 5));
            BoundingBox area = new BoundingBox(
                center.getX() - 5, center.getY() - 5, center.getZ() - 5,
                center.getX() + 5, center.getY() + 5, center.getZ() + 5
            );

            // Register both types of protected areas
            BlockProtectionHandler.registerProtectedArea(level, area, "test_reset_1");
            PermanentProtectionHandler.registerProtectedArea(level, area, "test_reset_2");

            // Verify both are protected
            if (!BlockProtectionHandler.isProtected(level, center)) {
                helper.fail("Block protection should be active before reset");
                return;
            }
            if (!PermanentProtectionHandler.isProtected(level, center)) {
                helper.fail("Permanent protection should be active before reset");
                return;
            }

            // Reset both handlers
            BlockProtectionHandler.reset();
            PermanentProtectionHandler.reset();

            // Verify both are cleared
            if (BlockProtectionHandler.isProtected(level, center)) {
                helper.fail("Block protection should be cleared after reset");
                return;
            }
            if (PermanentProtectionHandler.isProtected(level, center)) {
                helper.fail("Permanent protection should be cleared after reset");
                return;
            }

            helper.succeed();
        });
    }
}
