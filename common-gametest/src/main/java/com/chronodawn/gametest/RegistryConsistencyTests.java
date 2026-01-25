package com.chronodawn.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModItemId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tests to verify that all defined item/block IDs in ModItemId and ModBlockId enums
 * are properly registered in the game registries.
 *
 * These tests use the enum definitions as the source of truth.
 * If an ID is defined in the enum but not registered, the test will fail.
 */
public final class RegistryConsistencyTests {

    private RegistryConsistencyTests() {
        // Utility class
    }

    @FunctionalInterface
    public interface TestFactory<T> {
        T create(String name, Consumer<GameTestHelper> test);
    }

    /**
     * Generates tests for all item IDs defined in ModItemId enum.
     * Each test verifies that the item is properly registered.
     *
     * @param factory factory to create test instances
     * @return list of generated tests
     */
    public static <T> List<T> generateItemTests(TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();

        for (ModItemId itemId : ModItemId.availableForCurrent()) {
            String testName = "registry_item_" + itemId.id();
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    ResourceLocation rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, itemId.id());

                    if (!BuiltInRegistries.ITEM.containsKey(rl)) {
                        helper.fail("Item not registered: " + itemId.id() +
                                " (enum: " + itemId.name() + ")");
                        return;
                    }

                    helper.succeed();
                });
            }));
        }

        return tests;
    }

    /**
     * Generates tests for all block IDs defined in ModBlockId enum.
     * Each test verifies that the block is properly registered.
     *
     * @param factory factory to create test instances
     * @return list of generated tests
     */
    public static <T> List<T> generateBlockTests(TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();

        for (ModBlockId blockId : ModBlockId.availableForCurrent()) {
            String testName = "registry_block_" + blockId.id();
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    ResourceLocation rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, blockId.id());

                    if (!BuiltInRegistries.BLOCK.containsKey(rl)) {
                        helper.fail("Block not registered: " + blockId.id() +
                                " (enum: " + blockId.name() + ")");
                        return;
                    }

                    helper.succeed();
                });
            }));
        }

        return tests;
    }

    /**
     * Generates a summary test that checks all items and blocks at once.
     * This provides a quick overview of registration status.
     *
     * @param factory factory to create test instances
     * @return list containing a single summary test
     */
    public static <T> List<T> generateSummaryTest(TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();

        tests.add(factory.create("registry_consistency_summary", helper -> {
            helper.runAfterDelay(1, () -> {
                List<String> missingItems = new ArrayList<>();
                List<String> missingBlocks = new ArrayList<>();

                // Check all items
                for (ModItemId itemId : ModItemId.availableForCurrent()) {
                    ResourceLocation rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, itemId.id());
                    if (!BuiltInRegistries.ITEM.containsKey(rl)) {
                        missingItems.add(itemId.id());
                    }
                }

                // Check all blocks
                for (ModBlockId blockId : ModBlockId.availableForCurrent()) {
                    ResourceLocation rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, blockId.id());
                    if (!BuiltInRegistries.BLOCK.containsKey(rl)) {
                        missingBlocks.add(blockId.id());
                    }
                }

                if (missingItems.isEmpty() && missingBlocks.isEmpty()) {
                    ChronoDawn.LOGGER.info("Registry consistency check passed: {} items, {} blocks",
                            ModItemId.availableForCurrent().size(),
                            ModBlockId.availableForCurrent().size());
                    helper.succeed();
                } else {
                    StringBuilder sb = new StringBuilder("Registry consistency check failed:\n");
                    if (!missingItems.isEmpty()) {
                        sb.append("Missing items (").append(missingItems.size()).append("): ")
                                .append(String.join(", ", missingItems)).append("\n");
                    }
                    if (!missingBlocks.isEmpty()) {
                        sb.append("Missing blocks (").append(missingBlocks.size()).append("): ")
                                .append(String.join(", ", missingBlocks));
                    }
                    helper.fail(sb.toString());
                }
            });
        }));

        return tests;
    }
}
