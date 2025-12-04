package com.chronosphere.gametest;

import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.function.Consumer;

/**
 * Common GameTest logic shared between Fabric and NeoForge implementations.
 *
 * This class provides the test logic as Consumer<GameTestHelper> functions
 * that can be used by both platform-specific GameTest implementations.
 *
 * Reference: T172f/T173d - Common GameTest Logic
 */
public final class ChronosphereGameTestLogic {

    private ChronosphereGameTestLogic() {
        // Utility class
    }

    // Standard test position
    public static final BlockPos TEST_POS = new BlockPos(1, 2, 1);

    // Expected health values
    public static final float TIME_GUARDIAN_HEALTH = 200.0f;
    public static final float TIME_TYRANT_HEALTH = 500.0f;

    // ============== Entity Spawn Tests ==============

    /**
     * Test that Time Guardian entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_GUARDIAN_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TIME_GUARDIAN.get(), TEST_POS);
    };

    /**
     * Test that Time Tyrant entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TIME_TYRANT.get(), TEST_POS);
    };

    /**
     * Test that Temporal Wraith entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_WRAITH_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TEMPORAL_WRAITH.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TEMPORAL_WRAITH.get(), TEST_POS);
    };

    /**
     * Test that Clockwork Sentinel entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_SENTINEL_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.CLOCKWORK_SENTINEL.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.CLOCKWORK_SENTINEL.get(), TEST_POS);
    };

    // ============== Block Placement Tests ==============

    /**
     * Test that Time Wood Log block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_LOG_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_LOG.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_LOG.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Planks block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_PLANKS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_PLANKS.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_PLANKS.get(), TEST_POS);
    };

    /**
     * Test that Clockstone Block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_BLOCK_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CLOCKSTONE_BLOCK.get());
        helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_BLOCK.get(), TEST_POS);
    };

    // ============== Entity Health Tests ==============

    /**
     * Test that Time Guardian has correct initial health (200 HP).
     */
    public static final Consumer<GameTestHelper> TEST_TIME_GUARDIAN_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - TIME_GUARDIAN_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Time Guardian health was " + actualHealth + ", expected " + TIME_GUARDIAN_HEALTH);
            }
        });
    };

    /**
     * Test that Time Tyrant has correct initial health (500 HP).
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - TIME_TYRANT_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Time Tyrant health was " + actualHealth + ", expected " + TIME_TYRANT_HEALTH);
            }
        });
    };
}
