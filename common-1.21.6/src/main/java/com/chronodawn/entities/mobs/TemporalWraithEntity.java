package com.chronodawn.entities.mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Temporal Wraith (時の亡霊) - Hostile Mob
 *
 * A spectral entity that inflicts temporal slowness on attack.
 *
 * Attributes:
 * - Health: 20 (10 hearts)
 * - Attack Damage: 4 (2 hearts)
 * - Movement Speed: 0.25 (slightly faster than zombies)
 *
 * Special Mechanics:
 * - Inflicts Slowness II (10 seconds) on attack
 * - Affected by dimension-wide Slowness IV effect
 *
 * Spawn Conditions:
 * - Biomes: chronodawn_plains, chronodawn_forest
 * - Light level: Any (ChronoDawn is always daytime, so spawns regardless of light)
 *
 * Drops:
 * - Clockstone (0-2)
 * - Fruit of Time (0-1, rare)
 *
 * Note: Originally designed to phase through blocks when damaged, but this feature
 * requires complex implementation and has been deferred to future updates.
 *
 * Reference: spec.md (FR-026, FR-028)
 * Task: T201 [P] [US1] Create Temporal Wraith entity
 */
public class TemporalWraithEntity extends Monster {

    public TemporalWraithEntity(EntityType<? extends TemporalWraithEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5; // Experience points on defeat
    }

    /**
     * Define AI goals for Temporal Wraith behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Melee attack
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));

        // Priority 2: Random movement
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8));

        // Priority 3: Look at player
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 4: Random look around
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Target selection: Attack player on sight
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Create attribute supplier for Temporal Wraith
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0) // 10 hearts
            .add(Attributes.ATTACK_DAMAGE, 4.0) // 2 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.25) // Slightly faster than zombie (0.23)
            .add(Attributes.FOLLOW_RANGE, 35.0); // Follow range
    }

    /**
     * Override doHurtTarget to inflict Slowness II on attack
     */
    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(serverLevel, target);

        if (result && target instanceof LivingEntity livingEntity) {
            // Apply Slowness II for 10 seconds (200 ticks)
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1)); // Level 1 = Slowness II
        }

        return result;
    }

    /**
     * Override spawn rules to allow spawning in daylight
     * ChronoDawn is always daytime, so we only check difficulty and spawn position validity
     */
    public static boolean checkTemporalWraithSpawnRules(
        EntityType<TemporalWraithEntity> entityType,
        ServerLevelAccessor level,
        EntitySpawnReason spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        // Check difficulty (not Peaceful) and basic spawn position rules
        // Skip light level check entirely for ChronoDawn
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        // Check if spawn position is valid (solid block below, etc.)
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

}
