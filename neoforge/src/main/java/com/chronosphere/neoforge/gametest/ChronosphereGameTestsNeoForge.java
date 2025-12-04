package com.chronosphere.neoforge.gametest;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * GameTest tests for Chronosphere mod on NeoForge.
 *
 * These tests run in an actual Minecraft server environment,
 * allowing for testing of entity spawning, block interactions,
 * and other gameplay mechanics that require the full game runtime.
 *
 * Reference: T173 [P] Run all GameTests on NeoForge loader
 */
@EventBusSubscriber(modid = Chronosphere.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ChronosphereGameTestsNeoForge {

    private static final String BATCH = "chronosphere";
    private static final String TEMPLATE = "chronosphere:ancient_ruins";
    private static final int DEFAULT_TIMEOUT = 100;

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(ChronosphereGameTestsNeoForge.class);
        Chronosphere.LOGGER.info("Registered Chronosphere GameTests for NeoForge");
    }

    /**
     * Generate test functions dynamically to work around registration issues.
     */
    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();

        // Entity spawn tests
        tests.add(createTest("testTimeGuardianCanSpawn", helper -> {
            BlockPos spawnPos = new BlockPos(1, 2, 1);
            helper.spawn(ModEntities.TIME_GUARDIAN.get(), spawnPos);
            helper.succeedWhenEntityPresent(ModEntities.TIME_GUARDIAN.get(), spawnPos);
        }));

        tests.add(createTest("testTimeTyrantCanSpawn", helper -> {
            BlockPos spawnPos = new BlockPos(1, 2, 1);
            helper.spawn(ModEntities.TIME_TYRANT.get(), spawnPos);
            helper.succeedWhenEntityPresent(ModEntities.TIME_TYRANT.get(), spawnPos);
        }));

        tests.add(createTest("testTemporalWraithCanSpawn", helper -> {
            BlockPos spawnPos = new BlockPos(1, 2, 1);
            helper.spawn(ModEntities.TEMPORAL_WRAITH.get(), spawnPos);
            helper.succeedWhenEntityPresent(ModEntities.TEMPORAL_WRAITH.get(), spawnPos);
        }));

        tests.add(createTest("testClockworkSentinelCanSpawn", helper -> {
            BlockPos spawnPos = new BlockPos(1, 2, 1);
            helper.spawn(ModEntities.CLOCKWORK_SENTINEL.get(), spawnPos);
            helper.succeedWhenEntityPresent(ModEntities.CLOCKWORK_SENTINEL.get(), spawnPos);
        }));

        // Block placement tests
        tests.add(createTest("testTimeWoodLogCanBePlaced", helper -> {
            BlockPos pos = new BlockPos(1, 2, 1);
            helper.setBlock(pos, ModBlocks.TIME_WOOD_LOG.get());
            helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_LOG.get(), pos);
        }));

        tests.add(createTest("testTimeWoodPlanksCanBePlaced", helper -> {
            BlockPos pos = new BlockPos(1, 2, 1);
            helper.setBlock(pos, ModBlocks.TIME_WOOD_PLANKS.get());
            helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_PLANKS.get(), pos);
        }));

        tests.add(createTest("testClockstoneBlockCanBePlaced", helper -> {
            BlockPos pos = new BlockPos(1, 2, 1);
            helper.setBlock(pos, ModBlocks.CLOCKSTONE_BLOCK.get());
            helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_BLOCK.get(), pos);
        }));

        // Entity health tests
        tests.add(createTest("testTimeGuardianInitialHealth", helper -> {
            BlockPos spawnPos = new BlockPos(1, 2, 1);
            var entity = helper.spawn(ModEntities.TIME_GUARDIAN.get(), spawnPos);

            helper.runAfterDelay(1, () -> {
                float expectedHealth = 200.0f;
                float actualHealth = entity.getHealth();

                if (Math.abs(actualHealth - expectedHealth) < 0.1f) {
                    helper.succeed();
                } else {
                    helper.fail("Time Guardian health was " + actualHealth + ", expected " + expectedHealth);
                }
            });
        }));

        tests.add(createTest("testTimeTyrantInitialHealth", helper -> {
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
        }));

        Chronosphere.LOGGER.info("Generated {} GameTest functions for NeoForge", tests.size());
        return tests;
    }

    private static TestFunction createTest(String name, Consumer<GameTestHelper> testFunc) {
        return new TestFunction(
            BATCH,                      // batch name
            name,                       // test name
            TEMPLATE,                   // structure template
            Rotation.NONE,              // rotation
            DEFAULT_TIMEOUT,            // timeout in ticks
            0L,                         // setup ticks
            true,                       // required
            testFunc                    // test function
        );
    }
}
