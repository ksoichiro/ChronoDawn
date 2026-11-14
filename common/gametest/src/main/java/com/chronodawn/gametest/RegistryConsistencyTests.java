package com.chronodawn.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.registry.ModBlockEntityId;
import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModEntityId;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItemId;
import com.chronodawn.registry.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Registry consistency tests shared by all Minecraft versions.
 *
 * Each kind of check is one test that scans every ID and lists all failing IDs in a
 * single failure message, instead of generating one test per ID.
 * The summary test uses the ModItemId/ModBlockId/ModEntityId/ModBlockEntityId enums as the
 * source of truth for registration.
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
     * Generates one test that runs every check supplied by {@code collector} and fails once,
     * listing every failure. Aggregating per-ID checks into one test keeps the GameTest count
     * small, since these checks need no world and only read registries after one tick.
     *
     * @param factory factory to create test instances
     * @param testName name of the single generated test
     * @param title heading of the failure message
     * @param collector returns one message per failed ID, or an empty list when all pass
     * @return list containing the single aggregated test
     */
    public static <T> List<T> generateAggregated(
            TestFactory<T> factory, String testName, String title, Supplier<List<String>> collector) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create(testName, helper -> {
            helper.runAfterDelay(1, () -> {
                List<String> failures = collector.get();
                if (failures.isEmpty()) {
                    helper.succeed();
                } else {
                    CompatGameTestHelper.fail(helper, title + " (" + failures.size() + "):\n"
                            + String.join("\n", failures));
                }
            });
        }));
        return tests;
    }

    /**
     * Generates one test verifying that every registry ID in {@code registryClass} matches
     * its field name (after applying {@code idOverrides}).
     */
    public static <T> List<T> generateRegistryIdTest(
            TestFactory<T> factory, String testName, Class<?> registryClass, Map<String, String> idOverrides) {
        return generateAggregated(factory, testName, "Registry ID does not match field name", () -> {
            List<String> failures = new ArrayList<>();
            for (Field field : registryClass.getDeclaredFields()) {
                if (!isRegistrySupplierField(field)) continue;
                String fieldName = field.getName();
                String expectedId = idOverrides.getOrDefault(fieldName, fieldName.toLowerCase());
                try {
                    RegistrySupplier<?> supplier = (RegistrySupplier<?>) field.get(null);
                    String actualId = supplier.getId().getPath();
                    if (!actualId.equals(expectedId)) {
                        failures.add(fieldName + " has ID \"" + actualId + "\", expected \"" + expectedId + "\"");
                    }
                } catch (Exception e) {
                    failures.add("Failed to check ID for " + fieldName + ": " + e.getMessage());
                }
            }
            return failures;
        });
    }

    /**
     * Generates one test verifying that every spawn egg item references a registered entity.
     *
     * Convention: field TEMPORAL_WRAITH_SPAWN_EGG expects entity "temporal_wraith".
     */
    public static <T> List<T> generateSpawnEggEntityTest(TestFactory<T> factory) {
        return generateAggregated(factory, "spawn_egg_entity_check", "Spawn eggs referencing unregistered entities", () -> {
            List<String> failures = new ArrayList<>();
            for (Field field : ModItems.class.getDeclaredFields()) {
                if (!isRegistrySupplierField(field)) continue;
                String fieldName = field.getName();
                if (!fieldName.endsWith("_SPAWN_EGG")) continue;
                String entityId = fieldName.substring(0, fieldName.length() - "_SPAWN_EGG".length()).toLowerCase();
                if (!BuiltInRegistries.ENTITY_TYPE.containsKey(
                        CompatResourceLocation.create(ChronoDawn.MOD_ID, entityId))) {
                    failures.add("Spawn egg " + fieldName + " expects entity \"" + entityId
                            + "\" but it is not registered");
                }
            }
            return failures;
        });
    }

    /**
     * Generates one test verifying that a block with a same-named ModItems field has its item registered.
     * Blocks without an item field (e.g. portal blocks) are allowed.
     */
    public static <T> List<T> generateBlockItemConsistencyTest(TestFactory<T> factory) {
        return generateAggregated(factory, "block_item_check", "Blocks missing their item", () -> {
            List<String> failures = new ArrayList<>();
            for (Field field : ModBlocks.class.getDeclaredFields()) {
                if (!isRegistrySupplierField(field)) continue;
                String fieldName = field.getName();
                try {
                    RegistrySupplier<?> supplier = (RegistrySupplier<?>) field.get(null);
                    var blockId = supplier.getId();
                    if (BuiltInRegistries.ITEM.containsKey(blockId)) continue;
                    for (Field itemField : ModItems.class.getDeclaredFields()) {
                        if (itemField.getName().equals(fieldName) && isRegistrySupplierField(itemField)) {
                            failures.add(fieldName + " has a ModItems field but no item registered with ID \""
                                    + blockId + "\"");
                            break;
                        }
                    }
                } catch (Exception e) {
                    failures.add("Failed to check block-item for " + fieldName + ": " + e.getMessage());
                }
            }
            return failures;
        });
    }

    private static boolean isRegistrySupplierField(Field field) {
        return Modifier.isPublic(field.getModifiers())
                && Modifier.isStatic(field.getModifiers())
                && Modifier.isFinal(field.getModifiers())
                && RegistrySupplier.class.isAssignableFrom(field.getType());
    }

    /**
     * Generates a summary test that checks all items, blocks, entities, and block entities at once.
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
                List<String> missingEntities = new ArrayList<>();
                List<String> missingBlockEntities = new ArrayList<>();

                // Check all items
                for (ModItemId itemId : ModItemId.availableForCurrent()) {
                    var rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, itemId.id());
                    if (!BuiltInRegistries.ITEM.containsKey(rl)) {
                        missingItems.add(itemId.id());
                    }
                }

                // Check all blocks
                for (ModBlockId blockId : ModBlockId.availableForCurrent()) {
                    var rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, blockId.id());
                    if (!BuiltInRegistries.BLOCK.containsKey(rl)) {
                        missingBlocks.add(blockId.id());
                    }
                }

                // Check all entities
                for (ModEntityId entityId : ModEntityId.availableForCurrent()) {
                    var rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, entityId.id());
                    if (!BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) {
                        missingEntities.add(entityId.id());
                    }
                }

                // Check all block entities
                for (ModBlockEntityId blockEntityId : ModBlockEntityId.availableForCurrent()) {
                    var rl = CompatResourceLocation.create(
                            ChronoDawn.MOD_ID, blockEntityId.id());
                    if (!BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(rl)) {
                        missingBlockEntities.add(blockEntityId.id());
                    }
                }

                if (missingItems.isEmpty() && missingBlocks.isEmpty() && missingEntities.isEmpty() && missingBlockEntities.isEmpty()) {
                    ChronoDawn.LOGGER.info("Registry consistency check passed: {} items, {} blocks, {} entities, {} block entities",
                            ModItemId.availableForCurrent().size(),
                            ModBlockId.availableForCurrent().size(),
                            ModEntityId.availableForCurrent().size(),
                            ModBlockEntityId.availableForCurrent().size());
                    helper.succeed();
                } else {
                    StringBuilder sb = new StringBuilder("Registry consistency check failed:\n");
                    if (!missingItems.isEmpty()) {
                        sb.append("Missing items (").append(missingItems.size()).append("): ")
                                .append(String.join(", ", missingItems)).append("\n");
                    }
                    if (!missingBlocks.isEmpty()) {
                        sb.append("Missing blocks (").append(missingBlocks.size()).append("): ")
                                .append(String.join(", ", missingBlocks)).append("\n");
                    }
                    if (!missingEntities.isEmpty()) {
                        sb.append("Missing entities (").append(missingEntities.size()).append("): ")
                                .append(String.join(", ", missingEntities)).append("\n");
                    }
                    if (!missingBlockEntities.isEmpty()) {
                        sb.append("Missing block entities (").append(missingBlockEntities.size()).append("): ")
                                .append(String.join(", ", missingBlockEntities));
                    }
                    CompatGameTestHelper.fail(helper, sb.toString());
                }
            });
        }));

        return tests;
    }
}
