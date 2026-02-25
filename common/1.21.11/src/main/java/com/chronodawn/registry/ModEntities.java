package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.entities.boats.ChronoDawnBoat;
import com.chronodawn.entities.boats.ChronoDawnChestBoat;
import com.chronodawn.entities.bosses.*;
import com.chronodawn.entities.mobs.*;
import com.chronodawn.entities.projectiles.TimeArrowEntity;
import com.chronodawn.entities.projectiles.TimeBlastEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Architectury Registry wrapper for custom entities.
 *
 * This class provides a centralized registry for all custom entities in the ChronoDawn mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ChronoDawn.MOD_ID, Registries.ENTITY_TYPE);

    /**
     * Time Guardian (時の番人) - Mini-Boss Entity
     *
     * Reference: data-model.md (Entities - Time Guardian)
     * Task: T111 [US2] Register Time Guardian in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<TimeGuardianEntity>> TIME_GUARDIAN = ENTITIES.register(
        ModEntityId.TIME_GUARDIAN.id(),
        () -> EntityType.Builder.of(TimeGuardianEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 2.8f) // Width 1.0, Height 2.8 (taller than player for boss presence)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TIME_GUARDIAN.id())))
    );

    /**
     * Chronos Warden (クロノスの監視者) - Additional Mini-Boss Entity (Phase 1)
     *
     * Ancient guardian statue that protects underground vaults.
     * Drops Guardian Stone for crafting Chrono Aegis.
     *
     * Reference: research.md (Additional Bosses Implementation Plan - Phase 1)
     * Task: T234g [Phase 1] Register Chronos Warden in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<ChronosWardenEntity>> CHRONOS_WARDEN = ENTITIES.register(
        ModEntityId.CHRONOS_WARDEN.id(),
        () -> EntityType.Builder.of(ChronosWardenEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 2.5f) // Width 1.0, Height 2.5 (stone guardian size)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.CHRONOS_WARDEN.id())))
    );

    /**
     * Clockwork Colossus (機械仕掛けの巨人) - Additional Mini-Boss Entity (Phase 1)
     *
     * Massive mechanical guardian built by ancient clockworkers.
     * Drops Colossus Gear for crafting Chrono Aegis.
     *
     * Reference: research.md (Additional Bosses Implementation Plan - Phase 1)
     * Task: T235f [Phase 1] Register Clockwork Colossus in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<com.chronodawn.entities.bosses.ClockworkColossusEntity>> CLOCKWORK_COLOSSUS = ENTITIES.register(
        ModEntityId.CLOCKWORK_COLOSSUS.id(),
        () -> EntityType.Builder.of(com.chronodawn.entities.bosses.ClockworkColossusEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 2.5f) // Width 1.0, Height 2.5 (mechanical colossus size)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.CLOCKWORK_COLOSSUS.id())))
    );

    /**
     * Gear Projectile - Spinning gear fired by Clockwork Colossus
     *
     * Mechanical spinning gear projectile that deals damage and knockback.
     *
     * Reference: research.md (Additional Bosses Implementation Plan - Phase 1)
     * Task: T235f [Phase 1] Register Gear Projectile in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<com.chronodawn.entities.projectiles.GearProjectileEntity>> GEAR_PROJECTILE = ENTITIES.register(
        ModEntityId.GEAR_PROJECTILE.id(),
        () -> EntityType.Builder.<com.chronodawn.entities.projectiles.GearProjectileEntity>of(com.chronodawn.entities.projectiles.GearProjectileEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f) // Small projectile size
            .clientTrackingRange(4) // Short tracking range
            .updateInterval(10) // Update interval in ticks
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.GEAR_PROJECTILE.id())))
    );

    /**
     * Temporal Wraith (時の亡霊) - Hostile Mob
     *
     * Reference: spec.md (FR-026, FR-028)
     * Task: T204 [P] [US1] Register custom mobs in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<TemporalWraithEntity>> TEMPORAL_WRAITH = ENTITIES.register(
        ModEntityId.TEMPORAL_WRAITH.id(),
        () -> EntityType.Builder.of(TemporalWraithEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.8f) // Smaller than player (spectral appearance)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TEMPORAL_WRAITH.id())))
    );

    /**
     * Clockwork Sentinel (時計仕掛けの番兵) - Hostile Mob
     *
     * Reference: spec.md (FR-026, FR-027)
     * Task: T204 [P] [US1] Register custom mobs in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<ClockworkSentinelEntity>> CLOCKWORK_SENTINEL = ENTITIES.register(
        ModEntityId.CLOCKWORK_SENTINEL.id(),
        () -> EntityType.Builder.of(ClockworkSentinelEntity::new, MobCategory.MONSTER)
            .sized(0.9f, 2.0f) // Slightly larger than player (mechanical guardian)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.CLOCKWORK_SENTINEL.id())))
    );

    /**
     * Epoch Husk (エポック・ハスク) - Hostile Mob
     *
     * A weathered humanoid creature with time slowdown aura when damaged.
     */
    public static final RegistrySupplier<EntityType<EpochHuskEntity>> EPOCH_HUSK = ENTITIES.register(
        ModEntityId.EPOCH_HUSK.id(),
        () -> EntityType.Builder.of(EpochHuskEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f) // Zombie-like size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.EPOCH_HUSK.id())))
    );

    /**
     * Forgotten Minute (フォーゴトン・ミニット) - Hostile Mob
     *
     * A semi-transparent creature that appears and disappears, strengthens nearby enemies when attacked.
     */
    public static final RegistrySupplier<EntityType<ForgottenMinuteEntity>> FORGOTTEN_MINUTE = ENTITIES.register(
        ModEntityId.FORGOTTEN_MINUTE.id(),
        () -> EntityType.Builder.of(ForgottenMinuteEntity::new, MobCategory.MONSTER)
            .sized(0.4f, 0.8f) // Small Vex-like size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.FORGOTTEN_MINUTE.id())))
    );

    /**
     * Chronal Leech (クローナル・リーチ) - Hostile Mob
     *
     * A small creature that extends player attack cooldown, spawns in groups.
     */
    public static final RegistrySupplier<EntityType<ChronalLeechEntity>> CHRONAL_LEECH = ENTITIES.register(
        ModEntityId.CHRONAL_LEECH.id(),
        () -> EntityType.Builder.of(ChronalLeechEntity::new, MobCategory.MONSTER)
            .sized(0.4f, 0.3f) // Small silverfish-like size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.CHRONAL_LEECH.id())))
    );

    /**
     * Moment Creeper (モーメント・クリーパー) - Hostile Mob
     *
     * A creeper variant that freezes before exploding, with less terrain damage but strong time debuff.
     */
    public static final RegistrySupplier<EntityType<MomentCreeperEntity>> MOMENT_CREEPER = ENTITIES.register(
        ModEntityId.MOMENT_CREEPER.id(),
        () -> EntityType.Builder.of(MomentCreeperEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.7f) // Standard creeper size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.MOMENT_CREEPER.id())))
    );

    /**
     * Time Keeper (時間の管理者) - Neutral Mob with Trading
     *
     * Reference: spec.md (FR-026, FR-029)
     * Task: T204 [P] [US1] Register custom mobs in ModEntities registry
     */
    public static final RegistrySupplier<EntityType<TimeKeeperEntity>> TIME_KEEPER = ENTITIES.register(
        ModEntityId.TIME_KEEPER.id(),
        () -> EntityType.Builder.of(TimeKeeperEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.95f) // Similar to villager
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TIME_KEEPER.id())))
    );

    /**
     * Floq - Slime-like Hostile Mob
     *
     * A small cube-shaped creature that moves by jumping like vanilla slimes.
     * Hostile mob that attacks players.
     */
    public static final RegistrySupplier<EntityType<FloqEntity>> FLOQ = ENTITIES.register(
        ModEntityId.FLOQ.id(),
        () -> EntityType.Builder.of(FloqEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 0.6f) // Small cube
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.FLOQ.id())))
    );

    /**
     * Time Tyrant (時間の暴君) - Final Boss Entity
     *
     * Reference: data-model.md (Entities - Time Tyrant)
     * Tasks: T134-T135 [US3] Create and register Time Tyrant entity
     */
    public static final RegistrySupplier<EntityType<TimeTyrantEntity>> TIME_TYRANT = ENTITIES.register(
        ModEntityId.TIME_TYRANT.id(),
        () -> EntityType.Builder.of(TimeTyrantEntity::new, MobCategory.MONSTER)
            .sized(1.5f, 4.0f) // Width 1.5, Height 4.0 (includes head/horns)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TIME_TYRANT.id())))
    );

    /**
     * Time Arrow - Special projectile for fighting Time Tyrant
     *
     * Applies Slowness III, Weakness II, and Glowing effects when hitting Time Tyrant.
     * Task: T171g [US3] Create Time Arrow item and entity
     */
    public static final RegistrySupplier<EntityType<TimeArrowEntity>> TIME_ARROW = ENTITIES.register(
        ModEntityId.TIME_ARROW.id(),
        () -> EntityType.Builder.<TimeArrowEntity>of(TimeArrowEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f) // Standard arrow size
            .clientTrackingRange(4) // Standard arrow tracking range
            .updateInterval(20) // Standard arrow update interval
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TIME_ARROW.id())))
    );

    /**
     * Time Blast - Time Guardian's ranged attack projectile
     *
     * A time-themed magical projectile that applies Slowness II and Mining Fatigue I.
     * Task: T210 [P] [US2] Add ranged attack capability to Time Guardian
     */
    public static final RegistrySupplier<EntityType<TimeBlastEntity>> TIME_BLAST = ENTITIES.register(
        ModEntityId.TIME_BLAST.id(),
        () -> EntityType.Builder.<TimeBlastEntity>of(TimeBlastEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f) // Small projectile size
            .clientTrackingRange(4) // Standard projectile tracking range
            .updateInterval(10) // Update interval in ticks
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TIME_BLAST.id())))
    );

    /**
     * Temporal Phantom - Phase 2 Mini-Boss
     *
     * A spectral mage trapped between time, uses teleportation and phantom clones.
     * HP: 150, Attack: 8, Armor: 5, Speed: 0.25 (fast)
     *
     * Phase 1: Phase Shift (30% dodge), Warp Bolt (ranged magic)
     * Phase 2: Phantom Clone summons, Blink Strike teleportation
     *
     * Reference: research.md (Boss 3: Temporal Phantom)
     * Task: T236 [Phase 2] Implement Temporal Phantom
     */
    public static final RegistrySupplier<EntityType<TemporalPhantomEntity>> TEMPORAL_PHANTOM = ENTITIES.register(
        ModEntityId.TEMPORAL_PHANTOM.id(),
        () -> EntityType.Builder.of(TemporalPhantomEntity::new, MobCategory.MONSTER)
            .sized(0.8f, 2.0f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TEMPORAL_PHANTOM.id())))
    );

    /**
     * Entropy Keeper - Phase 2 Mini-Boss
     *
     * Aberrant entity that governs temporal decay and corruption.
     * HP: 160, Attack: 10, Armor: 6, Speed: 0.20
     *
     * Phase 1: Decay Aura (Wither I), Corrosion Touch (durability damage), Temporal Rot (corruption patches)
     * Phase 2: Degradation (+2 attack/60s, max 3 stacks), Entropy Burst (one-time explosion at 30% HP)
     *
     * Reference: research.md (Boss 4: Entropy Keeper)
     * Task: T237 [Phase 2] Implement Entropy Keeper
     */
    public static final RegistrySupplier<EntityType<EntropyKeeperEntity>> ENTROPY_KEEPER = ENTITIES.register(
        ModEntityId.ENTROPY_KEEPER.id(),
        () -> EntityType.Builder.of(EntropyKeeperEntity::new, MobCategory.MONSTER)
            .sized(0.9f, 2.2f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.ENTROPY_KEEPER.id())))
    );

    /**
     * GlideFish - Water Creature
     *
     * A small fish that spawns in ChronoDawn dimension waters.
     * Behaves similarly to vanilla Cod/Salmon.
     */
    public static final RegistrySupplier<EntityType<GlideFishEntity>> GLIDE_FISH = ENTITIES.register(
        ModEntityId.GLIDE_FISH.id(),
        () -> EntityType.Builder.of(GlideFishEntity::new, MobCategory.WATER_CREATURE)
            .sized(0.5f, 0.3f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.GLIDE_FISH.id())))
    );

    /**
     * Timeline Strider - Enderman-like hostile mob with teleportation.
     *
     * Neutral until stared at, teleports when damaged.
     * Drops Time Crystal.
     */
    public static final RegistrySupplier<EntityType<TimelineStriderEntity>> TIMELINE_STRIDER = ENTITIES.register(
        ModEntityId.TIMELINE_STRIDER.id(),
        () -> EntityType.Builder.of(TimelineStriderEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 2.9f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TIMELINE_STRIDER.id())))
    );

    /**
     * Hourglass Golem - Iron Golem-like hostile mob.
     *
     * Drops Time Hourglass.
     */
    public static final RegistrySupplier<EntityType<HourglassGolemEntity>> HOURGLASS_GOLEM = ENTITIES.register(
        ModEntityId.HOURGLASS_GOLEM.id(),
        () -> EntityType.Builder.of(HourglassGolemEntity::new, MobCategory.MONSTER)
            .sized(1.4f, 2.7f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.HOURGLASS_GOLEM.id())))
    );

    /**
     * Paradox Crawler - Spider-like hostile mob with wall climbing.
     *
     * Has wall climbing ability and leap attacks.
     * Replaces Spider spawns in ChronoDawn dimension.
     */
    public static final RegistrySupplier<EntityType<ParadoxCrawlerEntity>> PARADOX_CRAWLER = ENTITIES.register(
        ModEntityId.PARADOX_CRAWLER.id(),
        () -> EntityType.Builder.of(ParadoxCrawlerEntity::new, MobCategory.MONSTER)
            .sized(1.4f, 0.9f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.PARADOX_CRAWLER.id())))
    );

    /**
     * Secondhand Archer - Skeleton-like hostile mob with bow attacks.
     *
     * Spawns with a bow and attacks with arrows.
     * Replaces Skeleton spawns in ChronoDawn dimension.
     * Drops bones, damaged bow, arrows, and rarely Ancient Gears.
     */
    public static final RegistrySupplier<EntityType<SecondhandArcherEntity>> SECONDHAND_ARCHER = ENTITIES.register(
        ModEntityId.SECONDHAND_ARCHER.id(),
        () -> EntityType.Builder.of(SecondhandArcherEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.99f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.SECONDHAND_ARCHER.id())))
    );

    /**
     * Chrono Turtle - Friendly water creature
     *
     * Spawns in water biomes of ChronoDawn dimension.
     * Behaves similarly to vanilla Turtle with smooth swimming.
     * Drops Turtle Scute with a chance when killed.
     */
    public static final RegistrySupplier<EntityType<com.chronodawn.entities.mobs.ChronoTurtleEntity>> CHRONO_TURTLE = ENTITIES.register(
        ModEntityId.CHRONO_TURTLE.id(),
        () -> EntityType.Builder.of(com.chronodawn.entities.mobs.ChronoTurtleEntity::new, MobCategory.WATER_CREATURE)
            .sized(1.2f, 0.4f)  // Similar to vanilla turtle
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.CHRONO_TURTLE.id())))
    );

    /**
     * Timebound Rabbit - Friendly creature similar to vanilla Rabbit
     *
     * Spawns in ChronoDawn dimension biomes, replacing vanilla rabbits.
     * Drops rabbit meat and rabbit hide like vanilla rabbits.
     */
    public static final RegistrySupplier<EntityType<TimeboundRabbitEntity>> TIMEBOUND_RABBIT = ENTITIES.register(
        ModEntityId.TIMEBOUND_RABBIT.id(),
        () -> EntityType.Builder.of(TimeboundRabbitEntity::new, MobCategory.CREATURE)
            .sized(0.4f, 0.5f)  // Similar to vanilla rabbit
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.TIMEBOUND_RABBIT.id())))
    );

    /**
     * Pulse Hog - Friendly creature similar to vanilla Pig
     *
     * Spawns in ChronoDawn dimension biomes, replacing vanilla pigs.
     * Drops porkchop like vanilla pigs.
     */
    public static final RegistrySupplier<EntityType<PulseHogEntity>> PULSE_HOG = ENTITIES.register(
        ModEntityId.PULSE_HOG.id(),
        () -> EntityType.Builder.of(PulseHogEntity::new, MobCategory.CREATURE)
            .sized(0.9f, 0.9f)  // Similar to vanilla pig
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.PULSE_HOG.id())))
    );

    /**
     * Secondwing Fowl - Friendly creature similar to vanilla Chicken
     *
     * Spawns in ChronoDawn dimension biomes, replacing vanilla chickens.
     * Drops chicken like vanilla chickens.
     */
    public static final RegistrySupplier<EntityType<SecondwingFowlEntity>> SECONDWING_FOWL = ENTITIES.register(
        ModEntityId.SECONDWING_FOWL.id(),
        () -> EntityType.Builder.of(SecondwingFowlEntity::new, MobCategory.CREATURE)
            .sized(0.4f, 0.7f)  // Same as vanilla chicken
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.SECONDWING_FOWL.id())))
    );

    // === Boat Entities ===

    /**
     * ChronoDawn Boat - Custom boat entity for Time Wood variants
     *
     * Supports Time Wood, Dark Time Wood, and Ancient Time Wood types.
     * Each type uses the same entity class with different stored type data.
     *
     * Reference: research.md (Time Wood Boats Implementation Plan)
     * Task: T268-T270 [US1] Create Time Wood Boat variants
     */
    public static final RegistrySupplier<EntityType<ChronoDawnBoat>> CHRONO_DAWN_BOAT = ENTITIES.register(
        ModEntityId.CHRONO_DAWN_BOAT.id(),
        () -> EntityType.Builder.<ChronoDawnBoat>of(
                (entityType, level) -> new ChronoDawnBoat(entityType, level, () -> net.minecraft.world.item.Items.OAK_BOAT),
                MobCategory.MISC
            )
            .sized(1.375F, 0.5625F) // Standard boat size
            .clientTrackingRange(10)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.CHRONO_DAWN_BOAT.id())))
    );

    /**
     * ChronoDawn Chest Boat - Custom chest boat entity for Time Wood variants
     *
     * Supports Time Wood, Dark Time Wood, and Ancient Time Wood types with storage.
     * Each type uses the same entity class with different stored type data.
     *
     * Reference: research.md (Time Wood Boats Implementation Plan)
     * Task: T268-T270 [US1] Create Time Wood Chest Boat variants
     */
    public static final RegistrySupplier<EntityType<ChronoDawnChestBoat>> CHRONO_DAWN_CHEST_BOAT = ENTITIES.register(
        ModEntityId.CHRONO_DAWN_CHEST_BOAT.id(),
        () -> EntityType.Builder.<ChronoDawnChestBoat>of(
                (entityType, level) -> new ChronoDawnChestBoat(entityType, level, () -> net.minecraft.world.item.Items.OAK_CHEST_BOAT),
                MobCategory.MISC
            )
            .sized(1.375F, 0.5625F) // Standard boat size
            .clientTrackingRange(10)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, ModEntityId.CHRONO_DAWN_CHEST_BOAT.id())))
    );

    /**
     * Initialize entity registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        ENTITIES.register();
        ChronoDawn.LOGGER.debug("Registered ModEntities");
    }
}

