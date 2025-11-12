package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.mobs.ClockworkSentinelEntity;
import com.chronosphere.entities.mobs.TemporalWraithEntity;
import com.chronosphere.entities.mobs.TimeKeeperEntity;
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
     * Temporal Wraith (時の亡霊) - Hostile Mob
     *
     * Reference: spec.md (FR-026, FR-028)
     * Task: T204 [P] [US1] Register custom mobs in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<TemporalWraithEntity>> TEMPORAL_WRAITH = ENTITIES.register(
        "temporal_wraith",
        () -> EntityType.Builder.of(TemporalWraithEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.8f) // Smaller than player (spectral appearance)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("temporal_wraith")
    );

    /**
     * Clockwork Sentinel (時計仕掛けの番兵) - Hostile Mob
     *
     * Reference: spec.md (FR-026, FR-027)
     * Task: T204 [P] [US1] Register custom mobs in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<ClockworkSentinelEntity>> CLOCKWORK_SENTINEL = ENTITIES.register(
        "clockwork_sentinel",
        () -> EntityType.Builder.of(ClockworkSentinelEntity::new, MobCategory.MONSTER)
            .sized(0.9f, 2.0f) // Slightly larger than player (mechanical guardian)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("clockwork_sentinel")
    );

    /**
     * Time Keeper (時間の管理者) - Neutral Mob with Trading
     *
     * Reference: spec.md (FR-026, FR-029)
     * Task: T204 [P] [US1] Register custom mobs in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<TimeKeeperEntity>> TIME_KEEPER = ENTITIES.register(
        "time_keeper",
        () -> EntityType.Builder.of(TimeKeeperEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.95f) // Similar to villager
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("time_keeper")
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

