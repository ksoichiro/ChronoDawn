package com.chronosphere.entities.bosses;

import com.chronosphere.core.time.MobAICanceller;
import com.chronosphere.entities.bosses.ExtendedMeleeAttackGoal;
import com.chronosphere.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;
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

    // Phase constants
    private static final int PHASE_1 = 1; // 100%-66% HP: Time Stop
    private static final int PHASE_2 = 2; // 66%-33% HP: Teleport + Time Acceleration
    private static final int PHASE_3 = 3; // 33%-0% HP: AoE + HP Recovery

    // Ability cooldowns (in ticks)
    private int timeStopCooldown = 0;
    private int teleportCooldown = 0;
    private int timeAccelerationCooldown = 0;
    private int aoeCooldown = 0;
    private int postTeleportDelay = 0;

    // One-time abilities
    private boolean hasUsedTimeReversal = false;

    // Time Clock weakening state (T171f - Boss Balance Enhancement)
    private int timeClockWeakeningTicks = 0; // Remaining ticks of weakening effect
    private int timeClockUsesInPhase1 = 0;   // Number of uses in Phase 1
    private int timeClockUsesInPhase2 = 0;   // Number of uses in Phase 2
    private int timeClockUsesInPhase3 = 0;   // Number of uses in Phase 3
    private int previousPhase = PHASE_1;      // Track phase changes

    // Time Clock weakening constants
    private static final int TIME_CLOCK_WEAKENING_DURATION = 200; // 10 seconds
    private static final double WEAKENING_ARMOR_REDUCTION = 10.0; // 15 → 5
    private static final double WEAKENING_SPEED_MULTIPLIER = 0.5; // 50% speed reduction

    // Ability timing constants (in ticks)
    private static final int TIME_STOP_COOLDOWN_TICKS = 100; // 5 seconds
    private static final int TELEPORT_COOLDOWN_TICKS = 100; // 5 seconds
    private static final int TIME_ACCELERATION_COOLDOWN_TICKS = 160; // 8 seconds
    private static final int AOE_COOLDOWN_TICKS = 120; // 6 seconds
    private static final int POST_TELEPORT_DELAY_TICKS = 15; // 0.75 seconds

    // Ability parameters
    private static final double AOE_RANGE = 5.0; // 5-block radius
    private static final float AOE_DAMAGE = 12.0f; // 6 hearts
    private static final float TIME_REVERSAL_HP_PERCENT = 0.1f; // 10% of max HP
    private static final float TIME_REVERSAL_TRIGGER_THRESHOLD = 0.2f; // Trigger at 20% HP

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
            SoundEvents.WITHER_SPAWN,
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
        target.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            60, // 3 seconds
            4   // Slowness V
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
            SoundEvents.WITHER_SPAWN,
            SoundSource.HOSTILE,
            1.0f,
            1.5f
        );

        timeStopCooldown = TIME_STOP_COOLDOWN_TICKS;
    }

    /**
     * Phase 2 Ability: Teleport
     * Teleports behind or near the target player.
     */
    private void handleTeleportAbility(LivingEntity target) {
        if (teleportCooldown > 0 || postTeleportDelay > 0) {
            return;
        }

        // Attempt teleport
        Vec3 targetPos = target.position();
        Vec3 teleportPos = findTeleportPosition(targetPos);

        if (teleportPos != null && teleportToPosition(teleportPos)) {
            // Successful teleport
            teleportCooldown = TELEPORT_COOLDOWN_TICKS;
            postTeleportDelay = POST_TELEPORT_DELAY_TICKS;
        }
    }

    /**
     * Find a safe teleport position near the target.
     */
    private Vec3 findTeleportPosition(Vec3 targetPos) {
        // Try to teleport behind the target (3-5 blocks away)
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 3.0 + this.random.nextDouble() * 2.0;

            double x = targetPos.x + Math.cos(angle) * distance;
            double z = targetPos.z + Math.sin(angle) * distance;
            BlockPos testPos = BlockPos.containing(x, targetPos.y, z);

            // Check if position is safe (solid ground, no obstructions)
            if (isSafeTeleportPosition(testPos)) {
                return new Vec3(x, targetPos.y, z);
            }
        }

        return null;
    }

    /**
     * Check if a position is safe for teleportation.
     */
    private boolean isSafeTeleportPosition(BlockPos pos) {
        // Check ground is solid
        if (!this.level().getBlockState(pos.below()).isSolid()) {
            return false;
        }

        // Check space is clear (2 blocks tall for entity)
        for (int i = 0; i < 3; i++) {
            if (!this.level().getBlockState(pos.above(i)).isAir()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Teleport to the specified position.
     */
    private boolean teleportToPosition(Vec3 pos) {
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
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.HOSTILE,
            1.0f,
            1.0f
        );

        return true;
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
                // Damage
                player.hurt(this.damageSources().mobAttack(this), AOE_DAMAGE);

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
                SoundEvents.EVOKER_CAST_SPELL,
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
     */
    private void handleTimeReversalAbility() {
        if (hasUsedTimeReversal) {
            return;
        }

        float healthPercent = this.getHealth() / this.getMaxHealth();
        if (healthPercent > TIME_REVERSAL_TRIGGER_THRESHOLD) {
            return;
        }

        // Heal
        float healAmount = this.getMaxHealth() * TIME_REVERSAL_HP_PERCENT;
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
            SoundEvents.PLAYER_LEVELUP,
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
            SoundEvents.BELL_BLOCK,
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

        // Save Time Clock weakening state (T171f)
        tag.putInt("TimeClockWeakeningTicks", this.timeClockWeakeningTicks);
        tag.putInt("TimeClockUsesInPhase1", this.timeClockUsesInPhase1);
        tag.putInt("TimeClockUsesInPhase2", this.timeClockUsesInPhase2);
        tag.putInt("TimeClockUsesInPhase3", this.timeClockUsesInPhase3);
        tag.putInt("PreviousPhase", this.previousPhase);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Boss never despawns
    }
}
