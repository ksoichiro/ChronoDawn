package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Architectury Registry wrapper for custom entities.
 *
 * This class provides a centralized registry for all custom entities in the Chronosphere mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Chronosphere.MOD_ID, Registries.ENTITY_TYPE);

    /**
     * Time Guardian (時の番人) - Mini-Boss Entity
     *
     * Reference: data-model.md (Entities - Time Guardian)
     * Task: T111 [US2] Register Time Guardian in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<TimeGuardianEntity>> TIME_GUARDIAN = ENTITIES.register(
        "time_guardian",
        () -> EntityType.Builder.of(TimeGuardianEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 2.8f) // Width 1.0, Height 2.8 (taller than player for boss presence)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build("time_guardian")
    );

    /**
     * Initialize entity registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        ENTITIES.register();
        Chronosphere.LOGGER.info("Registered ModEntities");
    }
}

