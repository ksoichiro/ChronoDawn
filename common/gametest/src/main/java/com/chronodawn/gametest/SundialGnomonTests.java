package com.chronodawn.gametest;

import com.chronodawn.blocks.SundialGnomonBlockEntity;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SundialGnomonTests {

    private static final BlockPos GNOMON_POS = new BlockPos(1, 2, 1);
    private static final BlockPos RELOADED_GNOMON_POS = new BlockPos(1, 2, 2);
    private static final long COOLDOWN = 24000;

    private SundialGnomonTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("sundial_gnomon_tracks_cooldown_per_player",
            SundialGnomonTests::testCooldownPerPlayer));
        tests.add(factory.create("sundial_gnomon_cooldowns_survive_save_and_load",
            SundialGnomonTests::testCooldownsSurviveSaveAndLoad));
        return tests;
    }

    /**
     * Test: the placed gnomon carries a block entity that refuses a second use by
     * the same player within the cooldown and accepts another player.
     */
    private static void testCooldownPerPlayer(GameTestHelper helper) {
        helper.setBlock(GNOMON_POS, ModBlocks.SUNDIAL_GNOMON.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(GNOMON_POS)) instanceof SundialGnomonBlockEntity gnomon)) {
            CompatGameTestHelper.fail(helper, "sundial_gnomon has no SundialGnomonBlockEntity");
            return;
        }
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        long now = helper.getLevel().getGameTime();
        if (gnomon.tryActivate(first, now, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "First activation was refused");
            return;
        }
        if (gnomon.tryActivate(first, now + 1, COOLDOWN) <= 0) {
            CompatGameTestHelper.fail(helper, "Second activation by the same player was accepted within the cooldown");
            return;
        }
        if (gnomon.tryActivate(second, now + 1, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "Activation by a different player was refused");
            return;
        }
        helper.succeed();
    }

    /**
     * Test: cooldowns written by one gnomon are restored when its saved data is
     * loaded into another, including the game time each player last used it.
     */
    private static void testCooldownsSurviveSaveAndLoad(GameTestHelper helper) {
        helper.setBlock(GNOMON_POS, ModBlocks.SUNDIAL_GNOMON.get());
        helper.setBlock(RELOADED_GNOMON_POS, ModBlocks.SUNDIAL_GNOMON.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(GNOMON_POS)) instanceof SundialGnomonBlockEntity saved)
                || !(helper.getLevel().getBlockEntity(helper.absolutePos(RELOADED_GNOMON_POS)) instanceof SundialGnomonBlockEntity reloaded)) {
            CompatGameTestHelper.fail(helper, "sundial_gnomon has no SundialGnomonBlockEntity");
            return;
        }
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        long now = helper.getLevel().getGameTime();
        if (saved.tryActivate(first, now, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "First activation was refused");
            return;
        }
        CompatGameTestHelper.copyBlockEntityData(helper, saved, reloaded);
        long remaining = reloaded.tryActivate(first, now + 1, COOLDOWN);
        if (remaining != COOLDOWN - 1) {
            CompatGameTestHelper.fail(helper, "Expected " + (COOLDOWN - 1)
                + " ticks left after reload, got " + remaining);
            return;
        }
        if (reloaded.tryActivate(second, now + 1, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "Activation by a different player was refused after reload");
            return;
        }
        helper.succeed();
    }
}
