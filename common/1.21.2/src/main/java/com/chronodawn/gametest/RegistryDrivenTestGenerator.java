package com.chronodawn.gametest;

import com.chronodawn.gametest.boss.BossFightTestLogic;
import com.chronodawn.registry.ModBlockEntityId;
import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModEntityId;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Generates GameTest functions from registry-driven test data.
 *
 * This class reads test definitions from {@link RegistryDrivenTestData}
 * and produces named test functions that can be used by both Fabric and NeoForge.
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

    /**
     * Generates one test verifying tool durability values, listing every mismatch.
     */
    public static List<NamedTest> generateToolDurabilityTests() {
        return RegistryConsistencyTests.generateAggregated(NamedTest::new,
            "tool_durability_check", "Tool durability mismatches", () -> {
                List<String> failures = new ArrayList<>();
                for (var spec : RegistryDrivenTestData.getToolSpecs()) {
                    int actual = new ItemStack(spec.item().get()).getMaxDamage();
                    if (actual != spec.expectedDurability()) {
                        failures.add(spec.item().getId().getPath() + " durability was " + actual +
                            ", expected " + spec.expectedDurability());
                    }
                }
                return failures;
            });
    }

    /**
     * Generates one test verifying armor defense values, listing every mismatch.
     */
    public static List<NamedTest> generateArmorDefenseTests() {
        return RegistryConsistencyTests.generateAggregated(NamedTest::new,
            "armor_defense_check", "Armor defense mismatches", () -> {
                List<String> failures = new ArrayList<>();
                for (var spec : RegistryDrivenTestData.getArmorSpecs()) {
                    ItemStack stack = new ItemStack(spec.item().get());
                    int actual = getArmorDefenseFromItemStack(stack, spec.slot());
                    if (actual != spec.expectedDefense()) {
                        failures.add(spec.item().getId().getPath() + " defense was " + actual +
                            ", expected " + spec.expectedDefense());
                    }
                }
                return failures;
            });
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
     * Generates a test verifying server-side structure template loading.
     */
    public static List<NamedTest> generateStructureTests() {
        return StructureTests.generateStructureTests(NamedTest::new);
    }

    /**
     * Generates tests verifying portal frame construction, ignition, and dimension transition.
     */
    public static List<NamedTest> generatePortalTests() {
        return PortalTests.generateTests(NamedTest::new);
    }

    /**
     * Generates tests verifying Faded Plains block placement rules.
     */
    public static List<NamedTest> generateFadedPlainsTests() {
        return FadedPlainsTests.generateTests(NamedTest::new);
    }

    /**
     * Generates one test verifying that all item registry IDs match their field names.
     */
    public static List<NamedTest> generateItemIdTests() {
        return RegistryConsistencyTests.generateRegistryIdTest(NamedTest::new, "item_id_check", ModItems.class, ID_OVERRIDES);
    }

    /**
     * Generates one test verifying that all block registry IDs match their field names.
     */
    public static List<NamedTest> generateBlockIdTests() {
        return RegistryConsistencyTests.generateRegistryIdTest(NamedTest::new, "block_id_check", ModBlocks.class, ID_OVERRIDES);
    }

    /**
     * Generates one test verifying that all entity registry IDs match their field names.
     */
    public static List<NamedTest> generateEntityIdTests() {
        return RegistryConsistencyTests.generateRegistryIdTest(NamedTest::new, "entity_id_check", ModEntities.class, ID_OVERRIDES);
    }

    /**
     * Generates one test verifying spawn egg items reference registered entities.
     */
    public static List<NamedTest> generateSpawnEggEntityTests() {
        return RegistryConsistencyTests.generateSpawnEggEntityTest(NamedTest::new);
    }

    /**
     * Generates one test verifying food items have valid nutrition and saturation values.
     */
    public static List<NamedTest> generateFoodPropertyTests() {
        return RegistryConsistencyTests.generateAggregated(NamedTest::new,
            "food_check", "Food items with invalid properties", () -> {
                List<String> failures = new ArrayList<>();
                for (Field field : ModItems.class.getDeclaredFields()) {
                    if (!isRegistrySupplierField(field)) continue;
                    String fieldName = field.getName();
                    try {
                        @SuppressWarnings("unchecked")
                        RegistrySupplier<Item> supplier = (RegistrySupplier<Item>) field.get(null);
                        FoodProperties food = new ItemStack(supplier.get()).get(DataComponents.FOOD);
                        if (food == null) continue; // Not a food item
                        if (food.nutrition() <= 0) {
                            failures.add(fieldName + " is food but has nutrition=" + food.nutrition() + " (expected > 0)");
                        } else if (food.saturation() < 0) {
                            failures.add(fieldName + " is food but has saturation=" + food.saturation() + " (expected >= 0)");
                        }
                    } catch (Exception e) {
                        failures.add("Failed to check food for " + fieldName + ": " + e.getMessage());
                    }
                }
                return failures;
            });
    }

    /**
     * Generates one test verifying that blocks with items have matching registry IDs.
     */
    public static List<NamedTest> generateBlockItemConsistencyTests() {
        return RegistryConsistencyTests.generateBlockItemConsistencyTest(NamedTest::new);
    }

    /**
     * Generates one test verifying equipment items (tools/armor) have maxStackSize of 1.
     */
    public static List<NamedTest> generateEquipmentStackSizeTests() {
        return RegistryConsistencyTests.generateAggregated(NamedTest::new,
            "stack_size_check", "Equipment with maxStackSize != 1", () -> {
                List<String> failures = new ArrayList<>();
                List<RegistrySupplier<? extends Item>> equipment = new ArrayList<>();
                for (var spec : RegistryDrivenTestData.getToolSpecs()) equipment.add(spec.item());
                for (var spec : RegistryDrivenTestData.getArmorSpecs()) equipment.add(spec.item());
                for (var item : equipment) {
                    int maxStack = new ItemStack(item.get()).getMaxStackSize();
                    if (maxStack != 1) {
                        failures.add(item.getId().getPath() + " maxStackSize=" + maxStack + ", expected 1");
                    }
                }
                return failures;
            });
    }

    /**
     * Generates a summary test checking all registry consistency at once.
     */
    public static List<NamedTest> generateRegistryConsistencySummaryTest() {
        return RegistryConsistencyTests.generateSummaryTest(NamedTest::new);
    }

    /**
     * Generates tests verifying Chronite-related recipes are loaded by the server's RecipeManager.
     */
    public static List<NamedTest> generateChroniteRecipeLoadTests() {
        return RecipeLoadTests.generateChroniteRecipeLoadTests(NamedTest::new);
    }

    /**
     * Generates tests verifying Clockwork Mechanism recipes are loaded by the server's RecipeManager.
     */
    public static List<NamedTest> generateClockworkMechanismRecipeLoadTests() {
        return RecipeLoadTests.generateClockworkMechanismRecipeLoadTests(NamedTest::new);
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
        all.addAll(generateBossFightTests());
        all.addAll(MobBehaviorTests.generateSpawnEggUsageTests(RegistryDrivenTestData.getSpawnEggSpecs(), NamedTest::new));
        all.addAll(MobBehaviorTests.generateMobCategoryTests(RegistryDrivenTestData.getMobCategorySpecs(), NamedTest::new));
        all.addAll(MobBehaviorTests.generateMobPersistenceTests(RegistryDrivenTestData.getPersistentEntities(), NamedTest::new));
        all.addAll(MobBehaviorTests.generateFlyingMobTests(RegistryDrivenTestData.getFlyingEntities(), NamedTest::new));
        all.addAll(MobBehaviorTests.generateAquaticMobTests(RegistryDrivenTestData.getAquaticEntities(), NamedTest::new));
        all.addAll(generateAdvancementLoadedTests());
        all.addAll(generateAdvancementGrantTests());
        all.addAll(generateStructureTests());
        all.addAll(generatePortalTests());
        all.addAll(generateFadedPlainsTests());
        all.addAll(TimelessMushroomTests.generateTests(NamedTest::new));
        all.addAll(SundialGnomonTests.generateTests(NamedTest::new));
        all.addAll(MemorialPlateTests.generateTests(NamedTest::new));
        all.addAll(ChronoBloomTests.generateTests(NamedTest::new));
        all.addAll(LumenPolypTests.generateTests(NamedTest::new));
        all.addAll(TimeWellTests.generateTests(NamedTest::new));
        all.addAll(TimeCairnDialTests.generateTests(NamedTest::new));
        all.addAll(WatchmakerCampLootTests.generateTests(NamedTest::new));
        all.addAll(WatchmakerChestTests.generateTests(NamedTest::new));
        // Conventional (c:) tag membership verified at runtime, including entries
        // that reach an umbrella tag only through a platform-defined subtag.
        all.addAll(ConventionalTagTests.generate(ModItems.class, ModBlocks.class,
                ConventionalTagTests.v2ItemSpecs(), ConventionalTagTests.v2BlockSpecs(), NamedTest::new));
        all.addAll(generateChroniteRecipeLoadTests());
        all.addAll(generateClockworkMechanismRecipeLoadTests());
        all.addAll(generateRegistryConsistencySummaryTest());
        return all;
    }

    // --- Utility methods ---

    /**
     * Get armor defense value from ItemStack using DataComponents (1.21.2 API).
     */
    static int getArmorDefenseFromItemStack(ItemStack stack, EquipmentSlot slot) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return 0;
        }
        int[] defense = {0};
        modifiers.forEach(slot, (attribute, modifier) -> {
            if (attribute.is(Attributes.ARMOR)) {
                defense[0] = (int) modifier.amount();
            }
        });
        return defense[0];
    }

    private static boolean isRegistrySupplierField(Field field) {
        return Modifier.isPublic(field.getModifiers())
            && Modifier.isStatic(field.getModifiers())
            && Modifier.isFinal(field.getModifiers())
            && RegistrySupplier.class.isAssignableFrom(field.getType());
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
