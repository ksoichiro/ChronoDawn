package com.chronodawn.gametest;

import com.chronodawn.items.DeferredSpawnEggItem;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Shared mob behavior test generator used across all Minecraft versions.
 *
 * Generates tests for:
 * - Spawn egg usage (verifies spawn eggs produce correct entity type)
 * - Mob category (verifies entities are in correct MobCategory)
 * - Mob persistence (verifies persistent entities don't despawn)
 * - Flying mob behavior (verifies flying mobs use FlyingMoveControl)
 */
public final class MobBehaviorTests {

    private static final BlockPos TEST_POS = new BlockPos(1, 2, 1);

    private MobBehaviorTests() {
        // Utility class
    }

    /**
     * Spawn egg specification: spawn egg item + expected entity type.
     */
    public record SpawnEggSpec(
        RegistrySupplier<Item> spawnEgg,
        RegistrySupplier<? extends EntityType<?>> expectedEntity
    ) {}

    /**
     * Mob category specification: entity type + expected MobCategory.
     */
    public record MobCategorySpec(
        RegistrySupplier<? extends EntityType<?>> entity,
        MobCategory expectedCategory
    ) {}

    @FunctionalInterface
    public interface TestFactory<T> {
        T create(String name, Consumer<GameTestHelper> test);
    }

    /**
     * Generates tests verifying spawn eggs can spawn the correct entity type.
     *
     * Uses DeferredSpawnEggItem.getType() to get the entity type from the spawn egg,
     * then spawns it and verifies the spawned entity matches the expected type.
     */
    public static <T> List<T> generateSpawnEggUsageTests(
            List<SpawnEggSpec> specs,
            TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var spec : specs) {
            String entityName = spec.expectedEntity().getId().getPath();
            String testName = "spawn_egg_usage_" + entityName;
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    Item eggItem = spec.spawnEgg().get();
                    if (!(eggItem instanceof DeferredSpawnEggItem spawnEgg)) {
                        helper.fail(entityName + " spawn egg is not a DeferredSpawnEggItem");
                        return;
                    }
                    ItemStack eggStack = new ItemStack(eggItem);
                    EntityType<?> entityType = spawnEgg.getType(eggStack);
                    if (entityType == null) {
                        helper.fail(entityName + " spawn egg returned null entity type");
                        return;
                    }
                    EntityType<?> expectedType = spec.expectedEntity().get();
                    if (entityType != expectedType) {
                        helper.fail(entityName + " spawn egg returned wrong entity type: " +
                            BuiltInRegistries.ENTITY_TYPE.getKey(entityType) +
                            ", expected: " + BuiltInRegistries.ENTITY_TYPE.getKey(expectedType));
                        return;
                    }
                    var entity = helper.spawn(entityType, TEST_POS);
                    if (entity != null) {
                        helper.succeed();
                    } else {
                        helper.fail(entityName + " could not be spawned from spawn egg entity type");
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying entities are registered with the correct MobCategory.
     */
    public static <T> List<T> generateMobCategoryTests(
            List<MobCategorySpec> specs,
            TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var spec : specs) {
            String entityName = spec.entity().getId().getPath();
            String testName = "mob_category_" + entityName;
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    EntityType<?> entityType = spec.entity().get();
                    var actualCategory = entityType.getCategory();
                    if (actualCategory == spec.expectedCategory()) {
                        helper.succeed();
                    } else {
                        helper.fail(entityName + " has category " + actualCategory.getName() +
                            ", expected " + spec.expectedCategory().getName());
                    }
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying persistent entities don't despawn.
     *
     * Checks that requiresCustomPersistence() returns true
     * and removeWhenFarAway() returns false.
     */
    public static <T> List<T> generateMobPersistenceTests(
            List<RegistrySupplier<EntityType<?>>> entities,
            TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var entitySupplier : entities) {
            String entityName = entitySupplier.getId().getPath();
            tests.add(factory.create("mob_persistence_" + entityName, helper -> {
                var entity = helper.spawn(entitySupplier.get(), TEST_POS);
                helper.runAfterDelay(1, () -> {
                    if (!(entity instanceof Mob mob)) {
                        helper.fail(entityName + " is not a Mob");
                        return;
                    }
                    if (!mob.requiresCustomPersistence()) {
                        helper.fail(entityName + " should return true for requiresCustomPersistence()");
                        return;
                    }
                    if (mob.removeWhenFarAway(100.0)) {
                        helper.fail(entityName + " should return false for removeWhenFarAway()");
                        return;
                    }
                    helper.succeed();
                });
            }));
        }
        return tests;
    }

    /**
     * Generates tests verifying flying mobs use FlyingMoveControl.
     */
    public static <T> List<T> generateFlyingMobTests(
            List<RegistrySupplier<EntityType<?>>> entities,
            TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var entitySupplier : entities) {
            String entityName = entitySupplier.getId().getPath();
            tests.add(factory.create("mob_flying_" + entityName, helper -> {
                var entity = helper.spawn(entitySupplier.get(), TEST_POS);
                helper.runAfterDelay(1, () -> {
                    if (!(entity instanceof Mob mob)) {
                        helper.fail(entityName + " is not a Mob");
                        return;
                    }
                    if (!(mob.getMoveControl() instanceof FlyingMoveControl)) {
                        helper.fail(entityName + " should use FlyingMoveControl but uses " +
                            mob.getMoveControl().getClass().getSimpleName());
                        return;
                    }
                    helper.succeed();
                });
            }));
        }
        return tests;
    }
}
