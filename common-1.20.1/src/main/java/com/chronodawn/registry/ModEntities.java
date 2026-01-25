package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.ChronosWardenEntity;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import com.chronodawn.entities.bosses.TemporalPhantomEntity;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import com.chronodawn.entities.mobs.ClockworkSentinelEntity;
import com.chronodawn.entities.mobs.FloqEntity;
import com.chronodawn.entities.mobs.TemporalWraithEntity;
import com.chronodawn.entities.mobs.TimeKeeperEntity;
import com.chronodawn.entities.mobs.EpochHuskEntity;
import com.chronodawn.entities.mobs.ForgottenMinuteEntity;
import com.chronodawn.entities.mobs.ChronalLeechEntity;
import com.chronodawn.entities.mobs.MomentCreeperEntity;
import com.chronodawn.entities.mobs.ChronoTurtleEntity;
import com.chronodawn.entities.mobs.GlideFishEntity;
import com.chronodawn.entities.mobs.ParadoxCrawlerEntity;
import com.chronodawn.entities.mobs.SecondhandArcherEntity;
import com.chronodawn.entities.mobs.TimelineStriderEntity;
import com.chronodawn.entities.boats.ChronoDawnBoat;
import com.chronodawn.entities.boats.ChronoDawnChestBoat;
import com.chronodawn.entities.projectiles.TimeArrowEntity;
import com.chronodawn.entities.projectiles.TimeBlastEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
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
        "time_guardian",
        () -> EntityType.Builder.of(TimeGuardianEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 2.8f) // Width 1.0, Height 2.8 (taller than player for boss presence)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build("time_guardian")
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
        "chronos_warden",
        () -> EntityType.Builder.of(ChronosWardenEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 2.5f) // Width 1.0, Height 2.5 (stone guardian size)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build("chronos_warden")
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
        "clockwork_colossus",
        () -> EntityType.Builder.of(com.chronodawn.entities.bosses.ClockworkColossusEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 2.5f) // Width 1.0, Height 2.5 (mechanical colossus size)
            .clientTrackingRange(10) // Tracking range for clients
            .updateInterval(3) // Update interval in ticks
            .build("clockwork_colossus")
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
        "gear_projectile",
        () -> EntityType.Builder.<com.chronodawn.entities.projectiles.GearProjectileEntity>of(com.chronodawn.entities.projectiles.GearProjectileEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f) // Small projectile size
            .clientTrackingRange(4) // Short tracking range
            .updateInterval(10) // Update interval in ticks
            .build("gear_projectile")
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
     * Epoch Husk (エポック・ハスク) - Hostile Mob
     *
     * A weathered humanoid creature with time slowdown aura when damaged.
     */
    public static final RegistrySupplier<EntityType<EpochHuskEntity>> EPOCH_HUSK = ENTITIES.register(
        "epoch_husk",
        () -> EntityType.Builder.of(EpochHuskEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f) // Zombie-like size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("epoch_husk")
    );

    /**
     * Forgotten Minute (フォーゴトン・ミニット) - Hostile Mob
     *
     * A semi-transparent creature that appears and disappears, strengthens nearby enemies when attacked.
     */
    public static final RegistrySupplier<EntityType<ForgottenMinuteEntity>> FORGOTTEN_MINUTE = ENTITIES.register(
        "forgotten_minute",
        () -> EntityType.Builder.of(ForgottenMinuteEntity::new, MobCategory.MONSTER)
            .sized(0.4f, 0.8f) // Small Vex-like size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("forgotten_minute")
    );

    /**
     * Chronal Leech (クローナル・リーチ) - Hostile Mob
     *
     * A small creature that extends player attack cooldown, spawns in groups.
     */
    public static final RegistrySupplier<EntityType<ChronalLeechEntity>> CHRONAL_LEECH = ENTITIES.register(
        "chronal_leech",
        () -> EntityType.Builder.of(ChronalLeechEntity::new, MobCategory.MONSTER)
            .sized(0.4f, 0.3f) // Small silverfish-like size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("chronal_leech")
    );

    /**
     * Moment Creeper (モーメント・クリーパー) - Hostile Mob
     *
     * A creeper variant that freezes before exploding, with less terrain damage but strong time debuff.
     */
    public static final RegistrySupplier<EntityType<MomentCreeperEntity>> MOMENT_CREEPER = ENTITIES.register(
        "moment_creeper",
        () -> EntityType.Builder.of(MomentCreeperEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.7f) // Standard creeper size
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("moment_creeper")
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
     * Floq - Slime-like Hostile Mob
     *
     * A small cube-shaped creature that moves by jumping like vanilla slimes.
     * Hostile mob that attacks players.
     */
    public static final RegistrySupplier<EntityType<FloqEntity>> FLOQ = ENTITIES.register(
        "floq",
        () -> EntityType.Builder.of(FloqEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 0.6f) // Small cube
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("floq")
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
            .sized(1.5f, 4.0f) // Width 1.5, Height 4.0 (includes head/horns)
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
     * Time Blast - Time Guardian's ranged attack projectile
     *
     * A time-themed magical projectile that applies Slowness II and Mining Fatigue I.
     * Task: T210 [P] [US2] Add ranged attack capability to Time Guardian
     */
    public static final RegistrySupplier<EntityType<TimeBlastEntity>> TIME_BLAST = ENTITIES.register(
        "time_blast",
        () -> EntityType.Builder.<TimeBlastEntity>of(TimeBlastEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f) // Small projectile size
            .clientTrackingRange(4) // Standard projectile tracking range
            .updateInterval(10) // Update interval in ticks
            .build("time_blast")
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
        "temporal_phantom",
        () -> EntityType.Builder.of(TemporalPhantomEntity::new, MobCategory.MONSTER)
            .sized(0.8f, 2.0f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("temporal_phantom")
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
        "entropy_keeper",
        () -> EntityType.Builder.of(EntropyKeeperEntity::new, MobCategory.MONSTER)
            .sized(0.9f, 2.2f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("entropy_keeper")
    );

    /**
     * GlideFish - Water creature that spawns in ChronoDawn dimension waters.
     * Behaves like vanilla Cod/Salmon with smooth swimming movement.
     */
    public static final RegistrySupplier<EntityType<GlideFishEntity>> GLIDE_FISH = ENTITIES.register(
        "glide_fish",
        () -> EntityType.Builder.of(GlideFishEntity::new, MobCategory.WATER_CREATURE)
            .sized(0.5f, 0.3f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("glide_fish")
    );

    /**
     * ChronoTurtle - Friendly water creature that spawns in ChronoDawn dimension waters.
     *
     * Spawns in water biomes of ChronoDawn dimension.
     * Behaves similarly to vanilla Turtle with smooth swimming.
     * Drops Turtle Scute with a chance when killed.
     */
    public static final RegistrySupplier<EntityType<ChronoTurtleEntity>> CHRONO_TURTLE = ENTITIES.register(
        "chrono_turtle",
        () -> EntityType.Builder.of(ChronoTurtleEntity::new, MobCategory.WATER_CREATURE)
            .sized(1.2f, 0.4f)  // Similar to vanilla turtle
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("chrono_turtle")
    );

    /**
     * Paradox Crawler - Spider-like hostile mob that climbs walls.
     *
     * A temporal arachnid creature that crawls on surfaces.
     * Drops string and spider eyes.
     */
    public static final RegistrySupplier<EntityType<ParadoxCrawlerEntity>> PARADOX_CRAWLER = ENTITIES.register(
        "paradox_crawler",
        () -> EntityType.Builder.of(ParadoxCrawlerEntity::new, MobCategory.MONSTER)
            .sized(1.4f, 0.9f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("paradox_crawler")
    );

    /**
     * Secondhand Archer - Skeleton-like hostile mob with ranged bow attacks.
     *
     * Spawns with a bow and attacks players with arrows.
     * Replaces Skeleton spawns in the Chrono Dawn dimension.
     */
    public static final RegistrySupplier<EntityType<SecondhandArcherEntity>> SECONDHAND_ARCHER = ENTITIES.register(
        "secondhand_archer",
        () -> EntityType.Builder.of(SecondhandArcherEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.99f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("secondhand_archer")
    );

    /**
     * Timeline Strider - Enderman-like hostile mob with teleportation.
     *
     * Neutral until stared at, teleports when damaged.
     * Drops Time Crystal.
     */
    public static final RegistrySupplier<EntityType<TimelineStriderEntity>> TIMELINE_STRIDER = ENTITIES.register(
        "timeline_strider",
        () -> EntityType.Builder.of(TimelineStriderEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 2.9f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("timeline_strider")
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
        "chronodawn_boat",
        () -> EntityType.Builder.<ChronoDawnBoat>of(ChronoDawnBoat::new, MobCategory.MISC)
            .sized(1.375F, 0.5625F) // Standard boat size
            .clientTrackingRange(10)
            .build("chronodawn_boat")
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
        "chronodawn_chest_boat",
        () -> EntityType.Builder.<ChronoDawnChestBoat>of(ChronoDawnChestBoat::new, MobCategory.MISC)
            .sized(1.375F, 0.5625F) // Standard boat size
            .clientTrackingRange(10)
            .build("chronodawn_chest_boat")
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

