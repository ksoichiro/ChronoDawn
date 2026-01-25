package com.chronodawn.gametest;

import com.chronodawn.gametest.MobBehaviorTests;
import com.chronodawn.gametest.boss.BossFightTestLogic;
import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItemId;
import com.chronodawn.registry.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Generates GameTest functions from registry-driven test data.
 *
 * Version: 1.21.1 (uses ArmorItem.getDefense() API, item.getFoodProperties())
 */
public final class RegistryDrivenTestGenerator {

    private RegistryDrivenTestGenerator() {
        // Utility class
    }

    public static final BlockPos TEST_POS = new BlockPos(1, 2, 1);

    // Known cases where field name doesn't match registry ID (intentional naming differences)
    private static final Map<String, String> ID_OVERRIDES = Map.of(
        "FRUIT_OF_TIME_BLOCK", "fruit_of_time",
        "CHRONO_DAWN_BOAT", "chronodawn_boat",
        "CHRONO_DAWN_CHEST_BOAT", "chronodawn_chest_boat"
    );

    public record NamedTest(String name, Consumer<GameTestHelper> test, int timeoutTicks) {
        public NamedTest(String name, Consumer<GameTestHelper> test) {
            this(name, test, 100);
        }
    }

    public static List<NamedTest> generateBlockPlacementTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (var blockSupplier : RegistryDrivenTestData.getPlaceableBlocks()) {
            String registryName = blockSupplier.getId().getPath();
            String testName = "block_placement_" + registryName;
            tests.add(new NamedTest(testName, helper -> {
                helper.setBlock(TEST_POS, blockSupplier.get());
                helper.succeedWhenBlockPresent(blockSupplier.get(), TEST_POS);
            }));
        }
        return tests;
    }

    public static List<NamedTest> generateEntitySpawnTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (var entitySupplier : RegistryDrivenTestData.getSpawnableEntities()) {
            String registryName = entitySupplier.getId().getPath();
            String testName = "entity_spawn_" + registryName;
            tests.add(new NamedTest(testName, helper -> {
                helper.spawn(entitySupplier.get(), TEST_POS);
                helper.succeedWhenEntityPresent(entitySupplier.get(), TEST_POS);
            }));
        }
        return tests;
    }

    public static List<NamedTest> generateToolDurabilityTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (var spec : RegistryDrivenTestData.getToolSpecs()) {
            String registryName = spec.item().getId().getPath();
            String testName = "tool_durability_" + registryName;
            tests.add(new NamedTest(testName, helper -> {
                ItemStack stack = new ItemStack(spec.item().get());
                helper.runAfterDelay(1, () -> {
                    int actual = stack.getMaxDamage();
                    if (actual == spec.expectedDurability()) {
                        helper.succeed();
                    } else {
                        helper.fail(registryName + " durability was " + actual +
                            ", expected " + spec.expectedDurability());
                    }
                });
            }));
        }
        return tests;
    }

    public static List<NamedTest> generateArmorDefenseTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (var spec : RegistryDrivenTestData.getArmorSpecs()) {
            String registryName = spec.item().getId().getPath();
            String testName = "armor_defense_" + registryName;
            tests.add(new NamedTest(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    if (spec.item().get() instanceof ArmorItem armorItem) {
                        int actual = armorItem.getDefense();
                        if (actual == spec.expectedDefense()) {
                            helper.succeed();
                        } else {
                            helper.fail(registryName + " defense was " + actual +
                                ", expected " + spec.expectedDefense());
                        }
                    } else {
                        helper.fail(registryName + " is not an ArmorItem");
                    }
                });
            }));
        }
        return tests;
    }

    public static List<NamedTest> generateEntityAttributeTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (var spec : RegistryDrivenTestData.getEntityAttributeSpecs()) {
            String entityName = spec.entity().getId().getPath();
            String testName = "entity_attr_" + entityName + "_" + spec.attributeName();
            tests.add(new NamedTest(testName, helper -> {
                var entity = helper.spawn(spec.entity().get(), TEST_POS);
                helper.runAfterDelay(1, () -> {
                    if (!(entity instanceof LivingEntity living)) {
                        helper.fail(entityName + " is not a LivingEntity");
                        return;
                    }
                    double actual = getAttributeValue(living, spec.attributeName());
                    if (Math.abs(actual - spec.expectedValue()) < 0.1) {
                        helper.succeed();
                    } else {
                        helper.fail(entityName + " " + spec.attributeName() +
                            " was " + actual + ", expected " + spec.expectedValue());
                    }
                });
            }));
        }
        return tests;
    }

    public static List<NamedTest> generateBossFightTests() {
        return BossFightTestLogic.generateTests();
    }

    /**
     * Generates tests verifying advancements are loaded by the server.
     */
    public static List<NamedTest> generateAdvancementLoadedTests() {
        return AdvancementTests.generateAdvancementLoadedTests(
            RegistryDrivenTestData.getAdvancementSpecs(), NamedTest::new);
    }

    /**
     * Generates tests verifying advancement grant and check via CompatAdvancementHelper.
     */
    public static List<NamedTest> generateAdvancementGrantTests() {
        return AdvancementTests.generateAdvancementGrantTests(
            RegistryDrivenTestData.getAdvancementSpecs(), NamedTest::new);
    }

    /**
     * Generates tests verifying advancement parent references are valid.
     */
    public static List<NamedTest> generateAdvancementParentTests() {
        return AdvancementTests.generateAdvancementParentTests(
            RegistryDrivenTestData.getAdvancementSpecs(), NamedTest::new);
    }

    /**
     * Generates tests verifying granting a child advancement does not grant its parent.
     */
    public static List<NamedTest> generateAdvancementIsolationTests() {
        return AdvancementTests.generateAdvancementIsolationTests(
            RegistryDrivenTestData.getAdvancementSpecs(), NamedTest::new);
    }

    /**
     * Generates tests verifying structure template loading, minimum size, and required blocks.
     */
    public static List<NamedTest> generateStructureTests() {
        return StructureTests.generateStructureTests(
            StructureTests.getStructureSpecs(), NamedTest::new);
    }

    /**
     * Generates tests verifying that all item registry IDs match their field names.
     */
    public static List<NamedTest> generateItemIdTests() {
        return generateRegistryIdTests(ModItems.class, "item_id_");
    }

    /**
     * Generates tests verifying that all block registry IDs match their field names.
     */
    public static List<NamedTest> generateBlockIdTests() {
        return generateRegistryIdTests(ModBlocks.class, "block_id_");
    }

    /**
     * Generates tests verifying that all entity registry IDs match their field names.
     */
    public static List<NamedTest> generateEntityIdTests() {
        return generateRegistryIdTests(ModEntities.class, "entity_id_");
    }

    /**
     * Generates tests verifying spawn egg items reference the correct entity.
     */
    public static List<NamedTest> generateSpawnEggEntityTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (Field field : ModItems.class.getDeclaredFields()) {
            if (!isRegistrySupplierField(field)) continue;
            String fieldName = field.getName();
            if (!fieldName.endsWith("_SPAWN_EGG")) continue;

            String expectedEntityId = fieldName.substring(0, fieldName.length() - "_SPAWN_EGG".length()).toLowerCase();
            tests.add(new NamedTest("spawn_egg_entity_" + expectedEntityId, helper -> {
                helper.runAfterDelay(1, () -> {
                    ResourceLocation entityId = ResourceLocation.fromNamespaceAndPath("chronodawn", expectedEntityId);
                    if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                        helper.succeed();
                    } else {
                        helper.fail("Spawn egg " + fieldName + " expects entity \"" +
                            expectedEntityId + "\" but it is not registered");
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying food items have valid nutrition and saturation values.
     */
    public static List<NamedTest> generateFoodPropertyTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (Field field : ModItems.class.getDeclaredFields()) {
            if (!isRegistrySupplierField(field)) continue;
            String fieldName = field.getName();
            String expectedId = fieldName.toLowerCase();
            tests.add(new NamedTest("food_check_" + expectedId, helper -> {
                helper.runAfterDelay(1, () -> {
                    try {
                        @SuppressWarnings("unchecked")
                        RegistrySupplier<Item> supplier = (RegistrySupplier<Item>) field.get(null);
                        ItemStack stack = new ItemStack(supplier.get());
                        FoodProperties food = stack.get(DataComponents.FOOD);
                        if (food == null) {
                            helper.succeed(); // Not a food item
                            return;
                        }
                        if (food.nutrition() <= 0) {
                            helper.fail(fieldName + " is food but has nutrition=" + food.nutrition() + " (expected > 0)");
                        } else if (food.saturation() < 0) {
                            helper.fail(fieldName + " is food but has saturation=" + food.saturation() + " (expected >= 0)");
                        } else {
                            helper.succeed();
                        }
                    } catch (Exception e) {
                        helper.fail("Failed to check food for " + fieldName + ": " + e.getMessage());
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying that blocks with items have matching registry IDs.
     */
    public static List<NamedTest> generateBlockItemConsistencyTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (Field field : ModBlocks.class.getDeclaredFields()) {
            if (!isRegistrySupplierField(field)) continue;
            String fieldName = field.getName();
            String expectedId = fieldName.toLowerCase();
            tests.add(new NamedTest("block_item_" + expectedId, helper -> {
                helper.runAfterDelay(1, () -> {
                    try {
                        @SuppressWarnings("unchecked")
                        RegistrySupplier<?> supplier = (RegistrySupplier<?>) field.get(null);
                        ResourceLocation blockId = supplier.getId();
                        if (BuiltInRegistries.ITEM.containsKey(blockId)) {
                            helper.succeed();
                        } else {
                            boolean hasItemField = false;
                            for (Field itemField : ModItems.class.getDeclaredFields()) {
                                if (itemField.getName().equals(fieldName) && isRegistrySupplierField(itemField)) {
                                    hasItemField = true;
                                    break;
                                }
                            }
                            if (hasItemField) {
                                helper.fail(fieldName + " has a ModItems field but no item registered with ID \"" +
                                    blockId + "\"");
                            } else {
                                helper.succeed();
                            }
                        }
                    } catch (Exception e) {
                        helper.fail("Failed to check block-item for " + fieldName + ": " + e.getMessage());
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying equipment items have maxStackSize of 1.
     */
    public static List<NamedTest> generateEquipmentStackSizeTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (var spec : RegistryDrivenTestData.getToolSpecs()) {
            String name = spec.item().getId().getPath();
            tests.add(new NamedTest("stack_size_" + name, helper -> {
                helper.runAfterDelay(1, () -> {
                    ItemStack stack = new ItemStack(spec.item().get());
                    int maxStack = stack.getMaxStackSize();
                    if (maxStack == 1) {
                        helper.succeed();
                    } else {
                        helper.fail(name + " maxStackSize=" + maxStack + ", expected 1");
                    }
                });
            }));
        }
        for (var spec : RegistryDrivenTestData.getArmorSpecs()) {
            String name = spec.item().getId().getPath();
            tests.add(new NamedTest("stack_size_" + name, helper -> {
                helper.runAfterDelay(1, () -> {
                    ItemStack stack = new ItemStack(spec.item().get());
                    int maxStack = stack.getMaxStackSize();
                    if (maxStack == 1) {
                        helper.succeed();
                    } else {
                        helper.fail(name + " maxStackSize=" + maxStack + ", expected 1");
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying all items defined in ModItemId enum are registered.
     */
    public static List<NamedTest> generateItemRegistryConsistencyTests() {
        return RegistryConsistencyTests.generateItemTests(NamedTest::new);
    }

    /**
     * Generates tests verifying all blocks defined in ModBlockId enum are registered.
     */
    public static List<NamedTest> generateBlockRegistryConsistencyTests() {
        return RegistryConsistencyTests.generateBlockTests(NamedTest::new);
    }

    /**
     * Generates a summary test checking all registry consistency at once.
     */
    public static List<NamedTest> generateRegistryConsistencySummaryTest() {
        return RegistryConsistencyTests.generateSummaryTest(NamedTest::new);
    }

    /**
     * Generate all tests from all categories.
     */
    public static List<NamedTest> generateAllTests() {
        List<NamedTest> all = new ArrayList<>();
        all.addAll(generateBlockPlacementTests());
        all.addAll(generateEntitySpawnTests());
        all.addAll(generateToolDurabilityTests());
        all.addAll(generateArmorDefenseTests());
        all.addAll(generateEntityAttributeTests());
        all.addAll(generateItemIdTests());
        all.addAll(generateBlockIdTests());
        all.addAll(generateEntityIdTests());
        all.addAll(generateSpawnEggEntityTests());
        all.addAll(generateFoodPropertyTests());
        all.addAll(generateBlockItemConsistencyTests());
        all.addAll(generateEquipmentStackSizeTests());
        all.addAll(TranslationKeyTests.generate(ModItems.class, ID_OVERRIDES, NamedTest::new));
        all.addAll(generateBossFightTests());
        all.addAll(MobBehaviorTests.generateSpawnEggUsageTests(RegistryDrivenTestData.getSpawnEggSpecs(), NamedTest::new));
        all.addAll(MobBehaviorTests.generateMobCategoryTests(RegistryDrivenTestData.getMobCategorySpecs(), NamedTest::new));
        all.addAll(MobBehaviorTests.generateMobPersistenceTests(RegistryDrivenTestData.getPersistentEntities(), NamedTest::new));
        all.addAll(MobBehaviorTests.generateFlyingMobTests(RegistryDrivenTestData.getFlyingEntities(), NamedTest::new));
        all.addAll(generateAdvancementLoadedTests());
        all.addAll(generateAdvancementGrantTests());
        all.addAll(generateAdvancementParentTests());
        all.addAll(generateAdvancementIsolationTests());
        all.addAll(generateStructureTests());
        // Registry consistency tests (ModItemId/ModBlockId enum verification)
        all.addAll(generateItemRegistryConsistencyTests());
        all.addAll(generateBlockRegistryConsistencyTests());
        all.addAll(generateRegistryConsistencySummaryTest());
        return all;
    }

    // --- Utility methods ---

    private static boolean isRegistrySupplierField(Field field) {
        return Modifier.isPublic(field.getModifiers())
            && Modifier.isStatic(field.getModifiers())
            && Modifier.isFinal(field.getModifiers())
            && RegistrySupplier.class.isAssignableFrom(field.getType());
    }

    private static List<NamedTest> generateRegistryIdTests(Class<?> registryClass, String testPrefix) {
        List<NamedTest> tests = new ArrayList<>();
        for (Field field : registryClass.getDeclaredFields()) {
            if (!isRegistrySupplierField(field)) continue;
            String fieldName = field.getName();
            String expectedId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            tests.add(new NamedTest(testPrefix + expectedId, helper -> {
                helper.runAfterDelay(1, () -> {
                    try {
                        @SuppressWarnings("unchecked")
                        RegistrySupplier<?> supplier = (RegistrySupplier<?>) field.get(null);
                        String actualId = supplier.getId().getPath();
                        if (actualId.equals(expectedId)) {
                            helper.succeed();
                        } else {
                            helper.fail(fieldName + " has ID \"" + actualId +
                                "\", expected \"" + expectedId + "\"");
                        }
                    } catch (Exception e) {
                        helper.fail("Failed to check ID for " + fieldName + ": " + e.getMessage());
                    }
                });
            }));
        }
        return tests;
    }

    private static double getAttributeValue(LivingEntity entity, String attributeName) {
        return switch (attributeName) {
            case "max_health" -> entity.getAttributeValue(Attributes.MAX_HEALTH);
            case "armor" -> entity.getAttributeValue(Attributes.ARMOR);
            case "attack_damage" -> entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
            case "knockback_resistance" -> entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            default -> throw new IllegalArgumentException("Unknown attribute: " + attributeName);
        };
    }

}
