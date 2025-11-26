package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.worldgen.decorators.FruitDecorator;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Architectury Registry wrapper for custom tree decorator types.
 *
 * Tree decorators are used to add extra blocks to trees during world generation,
 * such as fruits, vines, beehives, etc.
 *
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 *
 * Reference: T080q [US1] Implement FruitDecorator class
 */
public class ModTreeDecoratorTypes {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES =
        DeferredRegister.create(Chronosphere.MOD_ID, Registries.TREE_DECORATOR_TYPE);

    /**
     * Fruit Decorator Type - Places Fruit of Time blocks on Time Wood Logs.
     */
    public static final RegistrySupplier<TreeDecoratorType<FruitDecorator>> FRUIT_DECORATOR =
        TREE_DECORATOR_TYPES.register("fruit_decorator", () -> {
            try {
                // Use reflection to create TreeDecoratorType since constructor is private
                java.lang.reflect.Constructor<TreeDecoratorType> constructor =
                    TreeDecoratorType.class.getDeclaredConstructor(com.mojang.serialization.MapCodec.class);
                constructor.setAccessible(true);

                return (TreeDecoratorType<FruitDecorator>) constructor.newInstance(FruitDecorator.CODEC);
            } catch (Exception e) {
                Chronosphere.LOGGER.error("Failed to create FruitDecorator TreeDecoratorType", e);
                throw new RuntimeException("Failed to create FruitDecorator TreeDecoratorType", e);
            }
        });

    /**
     * Initialize tree decorator type registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        TREE_DECORATOR_TYPES.register();
        Chronosphere.LOGGER.info("Registered ModTreeDecoratorTypes");
    }
}
