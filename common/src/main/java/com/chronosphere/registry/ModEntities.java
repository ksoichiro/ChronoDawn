package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;

/**
 * Architectury Registry wrapper for custom entities.
 *
 * This class provides a centralized registry for all custom entities in the Chronosphere mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Chronosphere.MOD_ID, Registries.ENTITY_TYPE);

    // TODO: Register custom entities here in future phases
    // Example:
    // public static final RegistrySupplier<EntityType<TimeGuardianEntity>> TIME_GUARDIAN = ENTITIES.register(
    //     "time_guardian",
    //     () -> EntityType.Builder.of(TimeGuardianEntity::new, MobCategory.MONSTER)
    //         .sized(1.0f, 2.0f)
    //         .build("time_guardian")
    // );

    /**
     * Initialize entity registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        ENTITIES.register();
        Chronosphere.LOGGER.info("Registered ModEntities");
    }
}
