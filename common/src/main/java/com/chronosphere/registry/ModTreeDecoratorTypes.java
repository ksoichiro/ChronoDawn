package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.worldgen.decorators.FruitDecorator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Registry for custom tree decorator types.
 *
 * Tree decorators are used to add extra blocks to trees during world generation,
 * such as fruits, vines, beehives, etc.
 *
 * Note: TreeDecoratorType constructor is private in Minecraft 1.21.1,
 * so we use reflection-based registration through BuiltInRegistries.
 *
 * Reference: T080q [US1] Implement FruitDecorator class
 */
public class ModTreeDecoratorTypes {
    /**
     * Fruit Decorator Type - Places Fruit of Time blocks on Time Wood Logs.
     */
    public static TreeDecoratorType<FruitDecorator> FRUIT_DECORATOR;

    /**
     * Initialize tree decorator type registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        try {
            // Use reflection to create TreeDecoratorType since constructor is private
            java.lang.reflect.Constructor<TreeDecoratorType> constructor =
                TreeDecoratorType.class.getDeclaredConstructor(com.mojang.serialization.MapCodec.class);
            constructor.setAccessible(true);

            TreeDecoratorType<FruitDecorator> decoratorType =
                (TreeDecoratorType<FruitDecorator>) constructor.newInstance(FruitDecorator.CODEC);

            FRUIT_DECORATOR = Registry.register(
                BuiltInRegistries.TREE_DECORATOR_TYPE,
                ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "fruit_decorator"),
                decoratorType
            );

            Chronosphere.LOGGER.info("Registered ModTreeDecoratorTypes");
        } catch (Exception e) {
            Chronosphere.LOGGER.error("Failed to register TreeDecoratorTypes", e);
            throw new RuntimeException("Failed to register TreeDecoratorTypes", e);
        }
    }
}
