package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.bosses.TimeTyrantEntity;
import com.chronosphere.entities.projectiles.TimeArrowEntity;
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
     * Time Tyrant (時間の暴君) - Final Boss Entity
     *
     * Reference: data-model.md (Entities - Time Tyrant)
     * Tasks: T134-T135 [US3] Create and register Time Tyrant entity
     */
    public static final RegistrySupplier<EntityType<TimeTyrantEntity>> TIME_TYRANT = ENTITIES.register(
        "time_tyrant",
        () -> EntityType.Builder.of(TimeTyrantEntity::new, MobCategory.MONSTER)
            .sized(1.5f, 3.0f) // Width 1.5, Height 3.0 (larger than Time Guardian)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build("time_tyrant")
    );

    /**
     * Time Arrow - Special projectile for fighting Time Tyrant
     *
     * Applies Slowness III, Weakness II, and Glowing effects when hitting Time Tyrant.
     * Task: T171g [US3] Create Time Arrow item and entity
     */
    public static final RegistrySupplier<EntityType<TimeArrowEntity>> TIME_ARROW = ENTITIES.register(
        "time_arrow",
        () -> EntityType.Builder.<TimeArrowEntity>of(TimeArrowEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f) // Standard arrow size
            .clientTrackingRange(4) // Standard arrow tracking range
            .updateInterval(20) // Standard arrow update interval
            .build("time_arrow")
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

