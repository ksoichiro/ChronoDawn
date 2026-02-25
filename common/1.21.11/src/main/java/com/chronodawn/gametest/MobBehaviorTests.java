package com.chronodawn.gametest;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Stub class for 1.21.5 gametest compatibility.
 *
 * In 1.21.5, the GameTest system was completely overhauled to be registry-based.
 * This stub provides the record types and generator methods needed by RegistryDrivenTestData.
 *
 * TODO: Implement full test generation for 1.21.5 registry-based gametest system.
 */
public final class MobBehaviorTests {

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
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateSpawnEggUsageTests(List<SpawnEggSpec> specs, TestFactory<T> factory) {
        return new ArrayList<>();
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateMobCategoryTests(List<MobCategorySpec> specs, TestFactory<T> factory) {
        return new ArrayList<>();
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateMobPersistenceTests(List<RegistrySupplier<EntityType<?>>> entities, TestFactory<T> factory) {
        return new ArrayList<>();
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateFlyingMobTests(List<RegistrySupplier<EntityType<?>>> entities, TestFactory<T> factory) {
        return new ArrayList<>();
    }
}
