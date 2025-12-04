package com.chronosphere.gametest;

import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

/**
 * GameTest tests for Chronosphere mod.
 *
 * These tests run in an actual Minecraft server environment,
 * allowing for testing of entity spawning, block interactions,
 * and other gameplay mechanics that require the full game runtime.
 *
 * Reference: T172 [P] Run all GameTests on Fabric loader
 */
public class ChronosphereGameTests implements FabricGameTest {

    /**
     * Test that Time Guardian entity can be spawned.
     * Uses empty structure template (floor only).
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeGuardianCanSpawn(GameTestHelper helper) {
        // Spawn Time Guardian at center of test area
        BlockPos spawnPos = new BlockPos(1, 2, 1);

        helper.spawn(ModEntities.TIME_GUARDIAN.get(), spawnPos);

        // Succeed when entity is present
        helper.succeedWhenEntityPresent(ModEntities.TIME_GUARDIAN.get(), spawnPos);
    }

    /**
     * Test that Time Tyrant entity can be spawned.
     * Uses empty structure template (floor only).
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeTyrantCanSpawn(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 2, 1);

        helper.spawn(ModEntities.TIME_TYRANT.get(), spawnPos);

        helper.succeedWhenEntityPresent(ModEntities.TIME_TYRANT.get(), spawnPos);
    }

    /**
     * Test that Temporal Wraith entity can be spawned.
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void testTemporalWraithCanSpawn(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 2, 1);

        helper.spawn(ModEntities.TEMPORAL_WRAITH.get(), spawnPos);

        helper.succeedWhenEntityPresent(ModEntities.TEMPORAL_WRAITH.get(), spawnPos);
    }

    /**
     * Test that Clockwork Sentinel entity can be spawned.
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void testClockworkSentinelCanSpawn(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 2, 1);

        helper.spawn(ModEntities.CLOCKWORK_SENTINEL.get(), spawnPos);

        helper.succeedWhenEntityPresent(ModEntities.CLOCKWORK_SENTINEL.get(), spawnPos);
    }

    /**
     * Test that Time Wood Log block can be placed.
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeWoodLogCanBePlaced(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);

        helper.setBlock(pos, ModBlocks.TIME_WOOD_LOG.get());

        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_LOG.get(), pos);
    }

    /**
     * Test that Time Wood Planks block can be placed.
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeWoodPlanksCanBePlaced(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);

        helper.setBlock(pos, ModBlocks.TIME_WOOD_PLANKS.get());

        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_PLANKS.get(), pos);
    }

    /**
     * Test that Clockstone Block can be placed.
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void testClockstoneBlockCanBePlaced(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);

        helper.setBlock(pos, ModBlocks.CLOCKSTONE_BLOCK.get());

        helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_BLOCK.get(), pos);
    }

    /**
     * Test that Time Guardian has correct initial health.
     * Time Guardian should have 200 HP (100 hearts).
     */
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testTimeGuardianInitialHealth(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 2, 1);

        var entity = helper.spawn(ModEntities.TIME_GUARDIAN.get(), spawnPos);

        helper.runAfterDelay(1, () -> {
            float expectedHealth = 200.0f;  // 100 hearts = 200 HP
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - expectedHealth) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Time Guardian health was " + actualHealth + ", expected " + expectedHealth);
            }
        });
    }

    /**
     * Test that Time Tyrant has correct initial health.
     * Time Tyrant should have 500 HP.
     */
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testTimeTyrantInitialHealth(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 2, 1);

        var entity = helper.spawn(ModEntities.TIME_TYRANT.get(), spawnPos);

        helper.runAfterDelay(1, () -> {
            float expectedHealth = 500.0f;
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - expectedHealth) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Time Tyrant health was " + actualHealth + ", expected " + expectedHealth);
            }
        });
    }
}
