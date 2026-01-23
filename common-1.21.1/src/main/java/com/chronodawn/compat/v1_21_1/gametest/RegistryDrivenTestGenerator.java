package com.chronodawn.gametest;

import com.chronodawn.gametest.boss.BossFightTestLogic;
import com.chronodawn.registry.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Generates GameTest functions from registry-driven test data.
 *
 * This class reads test definitions from {@link RegistryDrivenTestData}
 * and produces named test functions that can be used by both Fabric and NeoForge.
 *
 * Version: 1.21.1 (uses ArmorItem.getDefense() API)
 */
public final class RegistryDrivenTestGenerator {

    private RegistryDrivenTestGenerator() {
        // Utility class
    }

    public static final BlockPos TEST_POS = new BlockPos(1, 2, 1);

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
     * Generates tests verifying that all item registry IDs match their field names.
     *
     * Uses reflection to scan ModItems for all RegistrySupplier fields and checks
     * that each item's registry ID equals the field name in lowercase.
     * This catches accidental ID changes that would break saves, recipes, and loot tables.
     */
    public static List<NamedTest> generateItemIdTests() {
        List<NamedTest> tests = new ArrayList<>();
        for (Field field : ModItems.class.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())
                || !Modifier.isStatic(field.getModifiers())
                || !Modifier.isFinal(field.getModifiers())
                || !RegistrySupplier.class.isAssignableFrom(field.getType())) {
                continue;
            }
            String fieldName = field.getName();
            String expectedId = fieldName.toLowerCase();
            tests.add(new NamedTest("item_id_" + expectedId, helper -> {
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
        all.addAll(generateBossFightTests());
        return all;
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
