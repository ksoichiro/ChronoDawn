package com.chronodawn.gametest;

import com.chronodawn.gametest.boss.BossFightTestLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Generates GameTest functions from registry-driven test data.
 *
 * Version: 1.20.1 (uses ArmorItem.getDefense() API)
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

    public static List<NamedTest> generateAllTests() {
        List<NamedTest> all = new ArrayList<>();
        all.addAll(generateBlockPlacementTests());
        all.addAll(generateEntitySpawnTests());
        all.addAll(generateToolDurabilityTests());
        all.addAll(generateArmorDefenseTests());
        all.addAll(generateEntityAttributeTests());
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
