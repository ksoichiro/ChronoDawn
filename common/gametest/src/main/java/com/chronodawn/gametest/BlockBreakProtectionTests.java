package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.worldgen.protection.BlockBreakGuard;
import com.chronodawn.worldgen.protection.BlockBreakGuard.Verdict;
import com.chronodawn.worldgen.protection.BlockProtectionHandler;
import com.chronodawn.worldgen.protection.PermanentProtectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GameTests for the decision the loader break-event handlers make (Fabric
 * PlayerBlockBreakEvents.BEFORE, NeoForge BlockEvent.BreakEvent, Forge BlockEvent.BreakEvent).
 *
 * The handlers delegate to {@link BlockBreakGuard} because GameTest's mock server player always
 * reports isCreative(), so a real destroyBlock call can never reach the protected branches. These
 * tests run the guard against a live level with the real protection state. The state transitions
 * of the handlers themselves are covered by BlockProtectionDataTest.
 */
public final class BlockBreakProtectionTests {

    private static final BlockPos TARGET_POS = new BlockPos(1, 2, 1);

    private BlockBreakProtectionTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("protection_break_blocked_in_boss_room_until_defeated",
            BlockBreakProtectionTests::testBossRoomBreakBlockedUntilDefeated));
        tests.add(factory.create("protection_break_blocked_in_permanent_area",
            BlockBreakProtectionTests::testPermanentAreaBreakBlocked));
        tests.add(factory.create("protection_creative_player_bypasses_protection",
            BlockBreakProtectionTests::testCreativePlayerBypasses));
        return tests;
    }

    /**
     * The test world can outlive a run, and boss room protection is saved with it. A key built from
     * the position alone would inherit the "defeated" mark of the previous run, so add a random part.
     */
    private static String uniqueAreaId(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }

    private static BoundingBox areaAround(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(TARGET_POS);
        return new BoundingBox(
            center.getX() - 1, center.getY() - 1, center.getZ() - 1,
            center.getX() + 1, center.getY() + 1, center.getZ() + 1);
    }

    private static boolean expect(GameTestHelper helper, boolean creative, Verdict expected, String when) {
        Verdict actual = BlockBreakGuard.check(helper.getLevel(), helper.absolutePos(TARGET_POS), creative);
        if (actual != expected) {
            CompatGameTestHelper.fail(helper, when + ": expected " + expected + ", got " + actual);
            return false;
        }
        return true;
    }

    /**
     * Test: a survival player cannot break a block in a boss room, and can once the boss is defeated.
     */
    private static void testBossRoomBreakBlockedUntilDefeated(GameTestHelper helper) {
        // A per-test id keeps this independent of other tests, so no global reset is needed.
        String areaId = uniqueAreaId("break_test_boss_room");
        BlockProtectionHandler.registerProtectedArea(helper.getLevel(), areaAround(helper), areaId);
        try {
            if (!expect(helper, false, Verdict.BOSS_ROOM_PROTECTED, "before the boss is defeated")) {
                return;
            }
            BlockProtectionHandler.onBossDefeated(helper.getLevel(), areaId);
            if (!expect(helper, false, Verdict.ALLOWED, "after the boss is defeated")) {
                return;
            }
            helper.succeed();
        } finally {
            // Leave no active protection behind if the test failed before the boss was defeated.
            BlockProtectionHandler.onBossDefeated(helper.getLevel(), areaId);
        }
    }

    /**
     * Test: a permanently protected block stays unbreakable, including after a boss is defeated
     * at the same position.
     */
    private static void testPermanentAreaBreakBlocked(GameTestHelper helper) {
        // Permanent protection has no per-area removal and is not saved with the world. Do not call
        // PermanentProtectionHandler.reset(): it would also drop the real areas that structure
        // generation registered in this server.
        PermanentProtectionHandler.registerProtectedArea(
            helper.getLevel(), areaAround(helper), uniqueAreaId("break_test_permanent"));
        if (!expect(helper, false, Verdict.PERMANENTLY_PROTECTED, "with permanent protection")) {
            return;
        }
        // Boss defeat releases temporary protection only.
        BlockProtectionHandler.onBossDefeatedAt(helper.getLevel(), helper.absolutePos(TARGET_POS));
        if (!expect(helper, false, Verdict.PERMANENTLY_PROTECTED, "after a boss defeat at the position")) {
            return;
        }
        helper.succeed();
    }

    /**
     * Test: creative players bypass boss room protection. Permanent protection takes the same early
     * return, so it is not registered here.
     */
    private static void testCreativePlayerBypasses(GameTestHelper helper) {
        String areaId = uniqueAreaId("break_test_creative");
        BlockProtectionHandler.registerProtectedArea(helper.getLevel(), areaAround(helper), areaId);
        try {
            // Control: the area really is protected for a survival player.
            if (!expect(helper, false, Verdict.BOSS_ROOM_PROTECTED, "survival player in the area")) {
                return;
            }
            if (!expect(helper, true, Verdict.ALLOWED, "creative player in a protected area")) {
                return;
            }
            helper.succeed();
        } finally {
            BlockProtectionHandler.onBossDefeated(helper.getLevel(), areaId);
        }
    }
}
