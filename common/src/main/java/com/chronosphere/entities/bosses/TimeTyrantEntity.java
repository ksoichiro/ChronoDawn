package com.chronosphere.entities.bosses;

import com.chronosphere.core.time.MobAICanceller;
import com.chronosphere.entities.bosses.ExtendedMeleeAttackGoal;
import com.chronosphere.registry.ModEffects;
import com.chronosphere.registry.ModItems;
import com.chronosphere.registry.ModSounds;
import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Time Tyrant (時間の暴君) - Final Boss Entity
 *
 * The ultimate boss of the Chronosphere dimension, guarding the Stasis Core
 * at the deepest level of the Master Clock dungeon.
 *
 * Attributes (from data-model.md):
 * - Health: 500 (250 hearts)
 * - Armor: 15
 * - Attack Damage: 18 (9 hearts)
 * - Attack Speed: 0.4秒間隔
 * - Movement Speed: 0.25
 * - Size: 1.5f x 3.0f
 *
 * AI Phases:
 * - Phase 1 (HP 100%-66%): Time Stop attacks
 * - Phase 2 (HP 66%-33%): Teleport + Time Acceleration
 * - Phase 3 (HP 33%-0%): Area of Effect + HP Recovery
 *
 * Special Mechanics:
 * - Time Stop: Inflicts Slowness V on player (3 seconds)
 * - Teleport: Teleports behind or near player
 * - Time Acceleration: Self-buff with Speed II
 * - AoE Attack: 5-block radius damage + Slowness III
 * - Time Reversal: Recovers 10% HP once per fight (at ~20% HP)
 * - Triggers dimension stabilization on defeat
 * - Triggers reversed resonance (60 seconds) on defeat
 *
 * Drops:
 * - Eye of Chronos (1)
 * - Fragment of Stasis Core (5-8)
 * - Enhanced Clockstone (8-12)
 *
 * Defense:
 * - Projectile Resistance: 70% damage reduction from arrows and projectiles
 *
 * Reference: data-model.md (Entities - Time Tyrant)
 * Tasks: T134-T136
 */
public class TimeTyrantEntity extends Monster {
    // Boss bar for tracking health
    private final ServerBossEvent bossEvent = new ServerBossEvent(
        this.getDisplayName(),
        BossEvent.BossBarColor.PURPLE,
        BossEvent.BossBarOverlay.PROGRESS
    );

    // Entity data accessor for phase tracking
    private static final EntityDataAccessor<Integer> PHASE =
        SynchedEntityData.defineId(TimeTyrantEntity.class, EntityDataSerializers.INT);

    // Phase constants (public for testing)
    public static final int PHASE_1 = 1; // 100%-66% HP: Time Stop
    public static final int PHASE_2 = 2; // 66%-33% HP: Teleport + Time Acceleration
    public static final int PHASE_3 = 3; // 33%-0% HP: AoE + HP Recovery

    // Ability cooldowns (in ticks)
    private int timeStopCooldown = 0;
    private int teleportCooldown = 0;
    private int timeAccelerationCooldown = 0;
    private int aoeCooldown = 0;
    private int postTeleportDelay = 0;

    // One-time abilities
    private boolean hasUsedTimeReversal = false;

    // Chrono Aegis debuff flags (T238 - Chrono Aegis Integration)
    private boolean chronoAegisAnchorActive = false;     // Prevents teleport for 3s after teleport
    private boolean chronoAegisDisruptionActive = false; // HP recovery reduction active
    private int chronoAegisTeleportBlockTicks = 0;       // Remaining ticks of teleport block

    // Time Clock weakening state (T171f - Boss Balance Enhancement)
    private int timeClockWeakeningTicks = 0; // Remaining ticks of weakening effect
    private int timeClockUsesInPhase1 = 0;   // Number of uses in Phase 1
    private int timeClockUsesInPhase2 = 0;   // Number of uses in Phase 2
    private int timeClockUsesInPhase3 = 0;   // Number of uses in Phase 3
    private int previousPhase = PHASE_1;      // Track phase changes

    // Time Clock weakening constants (public for testing)
    public static final int TIME_CLOCK_WEAKENING_DURATION = 200; // 10 seconds
    public static final double WEAKENING_ARMOR_REDUCTION = 10.0; // 15 → 5
    public static final double WEAKENING_SPEED_MULTIPLIER = 0.5; // 50% speed reduction

    // Ability timing constants (in ticks) - public for testing
    public static final int TIME_STOP_COOLDOWN_TICKS = 100; // 5 seconds
    public static final int TELEPORT_COOLDOWN_TICKS = 100; // 5 seconds
    public static final int TIME_ACCELERATION_COOLDOWN_TICKS = 160; // 8 seconds
    public static final int AOE_COOLDOWN_TICKS = 120; // 6 seconds
    public static final int POST_TELEPORT_DELAY_TICKS = 15; // 0.75 seconds

    // Ability parameters (public for testing)
    public static final double AOE_RANGE = 5.0; // 5-block radius
    public static final float AOE_DAMAGE = 12.0f; // 6 hearts
    public static final float TIME_REVERSAL_HP_PERCENT = 0.1f; // 10% of max HP
    public static final float TIME_REVERSAL_TRIGGER_THRESHOLD = 0.2f; // Trigger at 20% HP

    public TimeTyrantEntity(EntityType<? extends TimeTyrantEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 200; // Experience points on defeat (increased from Time Guardian)
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, PHASE_1);
    }

    /**
     * Create attribute supplier for Time Tyrant.
     * Sets base attributes like health, armor, attack damage, and movement speed.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 500.0) // 250 hearts
            .add(Attributes.ARMOR, 15.0)
            .add(Attributes.ATTACK_DAMAGE, 18.0) // 9 hearts
            .add(Attributes.ATTACK_KNOCKBACK, 1.5)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // Complete immunity
    }

    /**
     * Get the current phase of the boss battle.
     *
     * @return Current phase (1, 2, or 3)
     */
    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Melee attack with extended range (3.5 blocks)
        this.goalSelector.addGoal(1, new ExtendedMeleeAttackGoal(this, 1.0, false, 3.5));

        // Priority 2: Move towards target
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0f));

        // Priority 3: Wander around
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));

        // Priority 4: Look at players
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 5: Random look around
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Target selection
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Track phase changes to reset Time Clock usage counters
            int currentPhase = this.entityData.get(PHASE);
            if (currentPhase != previousPhase) {
                previousPhase = currentPhase;
                // Phase changed - usage counter for new phase already at 0
            }

            // Update phase based on health
            updatePhase();

            // Handle Time Clock weakening effect
            handleTimeClockWeakening();

            // Handle phase-specific abilities
            handlePhaseAbilities();

            // Decrement cooldowns
            decrementCooldowns();
        }
    }

    /**
     * Update the current phase based on health percentage.
     */
    private void updatePhase() {
        float healthPercent = this.getHealth() / this.getMaxHealth();
        int currentPhase = this.entityData.get(PHASE);

        int newPhase;
        if (healthPercent > 0.66f) {
            newPhase = PHASE_1;
        } else if (healthPercent > 0.33f) {
            newPhase = PHASE_2;
        } else {
            newPhase = PHASE_3;
        }

        if (newPhase != currentPhase) {
            this.entityData.set(PHASE, newPhase);
            onPhaseTransition(newPhase);
        }
    }

    /**
     * Called when the boss transitions to a new phase.
     */
    private void onPhaseTransition(int newPhase) {
        // Play sound effect
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.BOSS_POWER_UP.get(),
            SoundSource.HOSTILE,
            2.0f,
            1.0f
        );

        // Spawn particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                100,
                0.5,
                1.0,
                0.5,
                0.1
            );
        }
    }

    /**
     * Handle phase-specific abilities based on current phase.
     */
    private void handlePhaseAbilities() {
        int currentPhase = this.entityData.get(PHASE);
        LivingEntity target = this.getTarget();

        if (target == null) {
            return;
        }

        // Phase 1: Time Stop
        if (currentPhase >= PHASE_1) {
            handleTimeStopAbility(target);
        }

        // Phase 2: Teleport + Time Acceleration
        if (currentPhase >= PHASE_2) {
            handleTeleportAbility(target);
            handleTimeAccelerationAbility();
        }

        // Phase 3: AoE + HP Recovery
        if (currentPhase >= PHASE_3) {
            handleAoEAbility();
            handleTimeReversalAbility();
        }
    }

    /**
     * Phase 1 Ability: Time Stop
     * Inflicts Slowness V on the target player for 3 seconds.
     */
    private void handleTimeStopAbility(LivingEntity target) {
        if (timeStopCooldown > 0 || postTeleportDelay > 0) {
            return;
        }

        // Check if within melee range
        double distance = this.distanceToSqr(target);
        if (distance > 16.0) { // 4 blocks squared
            return;
        }

        // Apply Time Stop effect
        // Chrono Aegis: Time Stop Resistance (Slowness V → Slowness II)
        boolean hasChronoAegis = false;
        if (target instanceof Player player) {
            Holder<net.minecraft.world.effect.MobEffect> aegisEffect =
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.CHRONO_AEGIS_BUFF.get());
            hasChronoAegis = player.hasEffect(aegisEffect);
        }
        int slownessLevel = hasChronoAegis ? 1 : 4; // Slowness II vs V

        target.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            60, // 3 seconds
            slownessLevel
        ));

        // Visual and audio feedback
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                target.getX(),
                target.getY() + 1.0,
                target.getZ(),
                30,
                0.5,
                0.5,
                0.5,
                0.05
            );
        }

        this.level().playSound(
            null,
            target.blockPosition(),
            ModSounds.BOSS_POWER_UP.get(),
            SoundSource.HOSTILE,
            1.0f,
            1.5f
        );

        timeStopCooldown = TIME_STOP_COOLDOWN_TICKS;
    }

    /**
     * Phase 2 Ability: Teleport
     * Teleports behind or near the target player.
     * Chrono Aegis: Dimensional Anchor prevents teleport for 3s after last teleport
     */
    private void handleTeleportAbility(LivingEntity target) {
        if (teleportCooldown > 0 || postTeleportDelay > 0) {
            return;
        }

        // Chrono Aegis: Dimensional Anchor - block teleport if active
        if (chronoAegisTeleportBlockTicks > 0) {
            return;
        }

        // Attempt teleport
        Vec3 targetPos = target.position();
        Vec3 teleportPos = findTeleportPosition(targetPos);

        if (teleportPos != null && teleportToPosition(teleportPos)) {
            // Successful teleport
            teleportCooldown = TELEPORT_COOLDOWN_TICKS;
            postTeleportDelay = POST_TELEPORT_DELAY_TICKS;

            // Chrono Aegis: Dimensional Anchor - activate teleport block
            if (!chronoAegisAnchorActive && hasNearbyChronoAegisPlayer()) {
                chronoAegisAnchorActive = true;
                chronoAegisTeleportBlockTicks = 60; // 3 seconds
            }
        }
    }

    /**
     * Find a safe teleport position near the target.
     * Scans for valid ground position to prevent suffocation.
     * Ensures teleport stays within boss room by checking path to target.
     */
    private Vec3 findTeleportPosition(Vec3 targetPos) {
        // Try to teleport near the target (2-4 blocks away - reduced to stay in room)
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            // Reduced distance to prevent escaping boss room (was 3-5, now 2-4)
            double distance = 2.0 + this.random.nextDouble() * 2.0;

            double x = targetPos.x + Math.cos(angle) * distance;
            double z = targetPos.z + Math.sin(angle) * distance;

            BlockPos candidatePos = BlockPos.containing(x, targetPos.y, z);

            // Check if there's a clear path (no walls between current pos and candidate)
            if (hasWallBetween(this.blockPosition(), candidatePos)) {
                continue; // Skip positions that require going through walls
            }

            // Find safe ground position by scanning vertically
            BlockPos safePos = findSafeGroundPosition(candidatePos);
            if (safePos != null) {
                // Double-check the area is truly safe (3x3 horizontal check)
                if (isAreaSafe(safePos)) {
                    return new Vec3(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
                }
            }
        }

        return null;
    }

    /**
     * Check if there's a wall between two positions.
     * Uses simple line-of-sight check for solid blocks.
     * Time Tyrant height is 4.0 blocks, so check 5 blocks for safety margin.
     */
    private boolean hasWallBetween(BlockPos from, BlockPos to) {
        // Simple check: sample points along the path
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 1) return false;

        int steps = (int) Math.ceil(distance);
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            int x = (int) (from.getX() + dx * t);
            int z = (int) (from.getZ() + dz * t);

            // Check at multiple Y levels (entity height 4.0 + margin 1.0 = 5 blocks)
            for (int y = 0; y < 5; y++) {
                BlockPos checkPos = new BlockPos(x, from.getY() + y, z);
                if (this.level().getBlockState(checkPos).isSolid()) {
                    return true; // Wall found
                }
            }
        }

        return false;
    }

    /**
     * Check if a 3x3 area around the position is safe (no solid blocks).
     * Time Tyrant height is 4.0 blocks, so check 5 blocks for safety margin.
     */
    private boolean isAreaSafe(BlockPos centerPos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = centerPos.offset(dx, 0, dz);
                // Check 5 blocks vertically at each position (entity height 4.0 + margin 1.0)
                for (int dy = 0; dy < 5; dy++) {
                    BlockState state = this.level().getBlockState(checkPos.above(dy));
                    if (state.isSolid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Find a safe ground position by scanning vertically.
     * Searches up to 3 blocks up and down from the starting position.
     *
     * @param startPos The starting position to search from
     * @return A safe BlockPos, or null if none found
     */
    private BlockPos findSafeGroundPosition(BlockPos startPos) {
        // First try the exact position
        if (isSafeTeleportPosition(startPos)) {
            return startPos;
        }

        // Search upward (for when target is on lower ground) - reduced range
        for (int offset = 1; offset <= 3; offset++) {
            BlockPos testPos = startPos.above(offset);
            if (isSafeTeleportPosition(testPos)) {
                return testPos;
            }
        }

        // Search downward (for when target is on higher ground) - reduced range
        for (int offset = 1; offset <= 3; offset++) {
            BlockPos testPos = startPos.below(offset);
            if (isSafeTeleportPosition(testPos)) {
                return testPos;
            }
        }

        return null;
    }

    /**
     * Check if a position is safe for teleportation.
     * Validates solid ground below and 5 blocks of non-solid space above for entity height.
     * Time Tyrant height is 4.0 blocks, so check 5 blocks for safety margin.
     */
    private boolean isSafeTeleportPosition(BlockPos pos) {
        // Check ground is solid
        if (!this.level().getBlockState(pos.below()).isSolid()) {
            return false;
        }

        // Check space is clear (5 blocks tall - entity height 4.0 + margin 1.0)
        // Use !isSolid() instead of isAir() to allow water, grass, etc.
        for (int i = 0; i < 5; i++) {
            BlockState state = this.level().getBlockState(pos.above(i));
            if (state.isSolid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Teleport to the specified position.
     * Validates position after teleport and reverts if entity is stuck in blocks.
     */
    private boolean teleportToPosition(Vec3 pos) {
        // Store original position for potential revert
        Vec3 originalPos = this.position();

        // Spawn particles at departure
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                50,
                0.5,
                1.0,
                0.5,
                0.1
            );
        }

        // Teleport
        this.teleportTo(pos.x, pos.y, pos.z);

        // Validate position after teleport - check if entity is stuck in blocks
        if (isStuckInBlocks()) {
            // Revert to original position
            this.teleportTo(originalPos.x, originalPos.y, originalPos.z);
            return false;
        }

        // Spawn particles at arrival
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                50,
                0.5,
                1.0,
                0.5,
                0.1
            );
        }

        // Play sound
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.BOSS_TELEPORT.get(),
            SoundSource.HOSTILE,
            1.0f,
            1.0f
        );

        return true;
    }

    /**
     * Check if the entity is stuck inside solid blocks (would cause suffocation).
     * Checks a 3x3x5 area around the entity for solid blocks.
     * Time Tyrant height is 4.0 blocks, so check 5 blocks for safety margin.
     */
    private boolean isStuckInBlocks() {
        BlockPos feetPos = this.blockPosition();

        // Check 3x3 horizontal area, 5 blocks vertical (entity height 4.0 + margin 1.0)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy < 5; dy++) {
                    BlockPos checkPos = feetPos.offset(dx, dy, dz);
                    BlockState state = this.level().getBlockState(checkPos);
                    // Check both isSolid and isSuffocating for comprehensive coverage
                    if (state.isSolid() || state.isSuffocating(this.level(), checkPos)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Phase 2 Ability: Time Acceleration
     * Applies Speed II to self for 5 seconds.
     */
    private void handleTimeAccelerationAbility() {
        if (timeAccelerationCooldown > 0 || postTeleportDelay > 0) {
            return;
        }

        // Apply speed buff
        this.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SPEED,
            100, // 5 seconds
            1    // Speed II
        ));

        // Visual feedback
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                50,
                0.5,
                1.0,
                0.5,
                0.1
            );
        }

        timeAccelerationCooldown = TIME_ACCELERATION_COOLDOWN_TICKS;
    }

    /**
     * Phase 3 Ability: Area of Effect Attack
     * Damages all players in 5-block radius and applies Slowness III + Mining Fatigue II.
     */
    private void handleAoEAbility() {
        if (aoeCooldown > 0 || postTeleportDelay > 0) {
            return;
        }

        AABB aoeBox = new AABB(this.blockPosition()).inflate(AOE_RANGE);

        boolean hitAnyEntity = false;
        for (Player player : this.level().getEntitiesOfClass(Player.class, aoeBox)) {
            if (player.isAlive()) {
                // Damage (Chrono Aegis: Temporal Shield - 50% reduction)
                float damage = AOE_DAMAGE;
                Holder<net.minecraft.world.effect.MobEffect> aegisEffect =
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.CHRONO_AEGIS_BUFF.get());
                if (player.hasEffect(aegisEffect)) {
                    damage *= 0.5f; // 50% damage reduction
                }
                player.hurt(this.damageSources().mobAttack(this), damage);

                // Apply debuffs
                player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    100, // 5 seconds
                    2    // Slowness III
                ));
                player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    100, // 5 seconds
                    1    // Mining Fatigue II
                ));

                hitAnyEntity = true;
            }
        }

        if (hitAnyEntity) {
            // Visual feedback
            if (this.level() instanceof ServerLevel serverLevel) {
                // Ground circle particles
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = this.getX() + Math.cos(angle) * AOE_RANGE;
                    double z = this.getZ() + Math.sin(angle) * AOE_RANGE;
                    serverLevel.sendParticles(
                        ParticleTypes.CRIMSON_SPORE,
                        x,
                        this.getY() + 0.1,
                        z,
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0
                    );
                }
            }

            // Sound effect
            this.level().playSound(
                null,
                this.blockPosition(),
                ModSounds.BOSS_CAST_SPELL.get(),
                SoundSource.HOSTILE,
                2.0f,
                0.8f
            );

            aoeCooldown = AOE_COOLDOWN_TICKS;
        }
    }

    /**
     * Phase 3 Ability: Time Reversal (HP Recovery)
     * Recovers 10% of max HP once per fight when HP drops below 20%.
     * Chrono Aegis: Time Reversal Disruption reduces recovery to 5%
     */
    private void handleTimeReversalAbility() {
        if (hasUsedTimeReversal) {
            return;
        }

        float healthPercent = this.getHealth() / this.getMaxHealth();
        if (healthPercent > TIME_REVERSAL_TRIGGER_THRESHOLD) {
            return;
        }

        // Heal (Chrono Aegis: Time Reversal Disruption - 10% → 5%)
        float healPercent = TIME_REVERSAL_HP_PERCENT;
        if (!chronoAegisDisruptionActive && hasNearbyChronoAegisPlayer()) {
            chronoAegisDisruptionActive = true;
            healPercent *= 0.5f; // 10% → 5%
        }
        float healAmount = this.getMaxHealth() * healPercent;
        this.heal(healAmount);
        hasUsedTimeReversal = true;

        // Visual feedback
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                100,
                0.5,
                1.0,
                0.5,
                0.2
            );

            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                50,
                0.5,
                1.0,
                0.5,
                0.1
            );
        }

        // Sound effect
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.TYRANT_PHASE_CHANGE.get(),
            SoundSource.HOSTILE,
            2.0f,
            1.0f
        );

        // Broadcast message to all nearby players
        Component message = Component.literal("Time Tyrant reverses time!")
            .withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD);

        for (Player player : this.level().getEntitiesOfClass(Player.class, new AABB(this.blockPosition()).inflate(32.0))) {
            player.displayClientMessage(message, true);
        }
    }

    /**
     * Decrement all ability cooldowns each tick.
     */
    private void decrementCooldowns() {
        if (timeStopCooldown > 0) timeStopCooldown--;
        if (teleportCooldown > 0) teleportCooldown--;
        if (timeAccelerationCooldown > 0) timeAccelerationCooldown--;
        if (aoeCooldown > 0) aoeCooldown--;
        if (postTeleportDelay > 0) postTeleportDelay--;

        // Chrono Aegis: Dimensional Anchor timer
        if (chronoAegisTeleportBlockTicks > 0) {
            chronoAegisTeleportBlockTicks--;
            if (chronoAegisTeleportBlockTicks == 0) {
                chronoAegisAnchorActive = false;
            }
        }

        // Chrono Aegis: Reset disruption flag if no Chrono Aegis players nearby
        if (!hasNearbyChronoAegisPlayer()) {
            chronoAegisDisruptionActive = false;
        }
    }

    // Time Clock Weakening Mechanic (T171f)

    /**
     * Apply Time Clock weakening effect to the boss.
     *
     * This method is called by TimeClockItem when a player uses it while targeting this boss.
     * The effect can only be applied once per phase (3 times total per fight).
     *
     * Effect Duration: 10 seconds (200 ticks)
     * Effect: Armor 15→5, Speed reduced by 50%
     *
     * @return true if effect was applied, false if already used in current phase
     */
    public boolean applyTimeClockWeakening() {
        int currentPhase = this.entityData.get(PHASE);

        // Check if already used in current phase
        int usesInCurrentPhase = switch (currentPhase) {
            case PHASE_1 -> timeClockUsesInPhase1;
            case PHASE_2 -> timeClockUsesInPhase2;
            case PHASE_3 -> timeClockUsesInPhase3;
            default -> 1; // Shouldn't happen, but treat as "already used"
        };

        if (usesInCurrentPhase > 0) {
            return false; // Already used in this phase
        }

        // Increment usage counter for current phase
        switch (currentPhase) {
            case PHASE_1 -> timeClockUsesInPhase1++;
            case PHASE_2 -> timeClockUsesInPhase2++;
            case PHASE_3 -> timeClockUsesInPhase3++;
        }

        // Apply weakening effect
        timeClockWeakeningTicks = TIME_CLOCK_WEAKENING_DURATION;

        // Visual feedback: spawn particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                this.getX(),
                this.getY() + 1.5,
                this.getZ(),
                50,
                0.5,
                1.0,
                0.5,
                0.1
            );
        }

        // Audio feedback
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.TYRANT_BELL.get(),
            SoundSource.HOSTILE,
            1.0f,
            0.5f // Lower pitch for debuff effect
        );

        return true;
    }

    /**
     * Handle Time Clock weakening effect each tick.
     *
     * Applies attribute modifiers for armor and speed reduction while active.
     * Decrements the weakening timer.
     */
    private void handleTimeClockWeakening() {
        if (timeClockWeakeningTicks > 0) {
            // Apply weakening effects via attribute modifiers
            // Note: We use temporary modifiers that are reapplied each tick
            // This is simpler than managing UUIDs and ensures clean removal

            // Reduce armor (15 → 5, reduction of 10)
            var armorAttribute = this.getAttribute(Attributes.ARMOR);
            if (armorAttribute != null) {
                double baseArmor = 15.0;
                double targetArmor = 5.0;
                double currentArmor = armorAttribute.getBaseValue();

                // Temporarily override armor value
                if (currentArmor > targetArmor) {
                    armorAttribute.setBaseValue(targetArmor);
                }
            }

            // Reduce speed (50% reduction)
            var speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                double baseSpeed = 0.25;
                double targetSpeed = baseSpeed * WEAKENING_SPEED_MULTIPLIER;

                // Temporarily override speed value
                speedAttribute.setBaseValue(targetSpeed);
            }

            // Decrement timer
            timeClockWeakeningTicks--;

            // When effect ends, restore attributes
            if (timeClockWeakeningTicks == 0) {
                armorAttribute = this.getAttribute(Attributes.ARMOR);
                if (armorAttribute != null) {
                    armorAttribute.setBaseValue(15.0);
                }

                speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttribute != null) {
                    speedAttribute.setBaseValue(0.25);
                }

                // Visual feedback: effect ended
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        ParticleTypes.POOF,
                        this.getX(),
                        this.getY() + 1.5,
                        this.getZ(),
                        20,
                        0.5,
                        1.0,
                        0.5,
                        0.05
                    );
                }
            }
        } else {
            // No weakening active - ensure attributes are at base values
            var armorAttribute = this.getAttribute(Attributes.ARMOR);
            if (armorAttribute != null && armorAttribute.getBaseValue() < 15.0) {
                armorAttribute.setBaseValue(15.0);
            }

            var speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null && speedAttribute.getBaseValue() < 0.25) {
                speedAttribute.setBaseValue(0.25);
            }
        }
    }

    // Boss bar management

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    // NBT persistence

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        this.entityData.set(PHASE, tag.getInt("Phase"));
        this.hasUsedTimeReversal = tag.getBoolean("HasUsedTimeReversal");

        // Load Chrono Aegis state (T238)
        this.chronoAegisAnchorActive = tag.getBoolean("ChronoAegisAnchorActive");
        this.chronoAegisDisruptionActive = tag.getBoolean("ChronoAegisDisruptionActive");
        this.chronoAegisTeleportBlockTicks = tag.getInt("ChronoAegisTeleportBlockTicks");

        // Load Time Clock weakening state (T171f)
        this.timeClockWeakeningTicks = tag.getInt("TimeClockWeakeningTicks");
        this.timeClockUsesInPhase1 = tag.getInt("TimeClockUsesInPhase1");
        this.timeClockUsesInPhase2 = tag.getInt("TimeClockUsesInPhase2");
        this.timeClockUsesInPhase3 = tag.getInt("TimeClockUsesInPhase3");
        this.previousPhase = tag.getInt("PreviousPhase");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", this.entityData.get(PHASE));
        tag.putBoolean("HasUsedTimeReversal", this.hasUsedTimeReversal);

        // Save Chrono Aegis state (T238)
        tag.putBoolean("ChronoAegisAnchorActive", this.chronoAegisAnchorActive);
        tag.putBoolean("ChronoAegisDisruptionActive", this.chronoAegisDisruptionActive);
        tag.putInt("ChronoAegisTeleportBlockTicks", this.chronoAegisTeleportBlockTicks);

        // Save Time Clock weakening state (T171f)
        tag.putInt("TimeClockWeakeningTicks", this.timeClockWeakeningTicks);
        tag.putInt("TimeClockUsesInPhase1", this.timeClockUsesInPhase1);
        tag.putInt("TimeClockUsesInPhase2", this.timeClockUsesInPhase2);
        tag.putInt("TimeClockUsesInPhase3", this.timeClockUsesInPhase3);
        tag.putInt("PreviousPhase", this.previousPhase);
    }

    /**
     * Check if any nearby players have Chrono Aegis buff active.
     * Used to determine Time Tyrant debuff effects.
     *
     * Task: T238 [US3] Integrate Chrono Aegis effects into Time Tyrant
     */
    private boolean hasNearbyChronoAegisPlayer() {
        AABB searchBox = new AABB(this.blockPosition()).inflate(32.0);
        Holder<net.minecraft.world.effect.MobEffect> aegisEffect =
            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.CHRONO_AEGIS_BUFF.get());
        return this.level().getEntitiesOfClass(Player.class, searchBox)
            .stream()
            .anyMatch(player -> player.hasEffect(aegisEffect));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Boss never despawns
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);

        // Unprotect Master Clock boss room when defeated
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            BlockProtectionHandler.onBossDefeatedAt(serverLevel, this.blockPosition());
        }
    }

    /**
     * Projectile Resistance: Reduces damage from arrows and projectiles by 70%.
     *
     * This prevents players from easily defeating the boss by attacking with arrows
     * from a distance without engaging in close combat.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Check if damage is from a projectile
        if (source.getDirectEntity() instanceof Projectile) {
            // Apply 70% damage reduction (Projectile Resistance)
            amount *= 0.3f;
        }

        return super.hurt(source, amount);
    }
}
