package com.chronosphere.entities.mobs;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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
 * Clockwork Sentinel (時計仕掛けの番兵) - Hostile Mob
 *
 * A mechanical guardian immune to time distortion effects. Moves at normal speed unlike other Chronosphere mobs.
 *
 * Attributes:
 * - Health: 30 (15 hearts)
 * - Attack Damage: 6 (3 hearts)
 * - Movement Speed: 0.3 (normal speed, NOT affected by Slowness IV)
 * - Armor: 5 (moderate protection)
 *
 * Special Mechanics:
 * - IMMUNE to time distortion effect (Slowness IV)
 * - Drops Ancient Gear (used for Master Clock progression)
 *
 * Spawn Conditions:
 * - Biomes: chronosphere_plains, chronosphere_forest, chronosphere_desert (all land biomes)
 * - Structures: Desert Clock Tower, Master Clock
 * - Light level: Any (spawns during night in day/night cycle)
 *
 * Drops:
 * - Ancient Gear (1-2, required for Master Clock access)
 * - Enhanced Clockstone (0-1, rare)
 *
 * Reference: spec.md (FR-026, FR-027)
 * Task: T202 [P] [US1] Create Clockwork Sentinel entity
 */
public class ClockworkSentinelEntity extends Monster {
    public ClockworkSentinelEntity(EntityType<? extends ClockworkSentinelEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10; // Higher XP reward due to immunity and difficulty
    }

    /**
     * Define AI goals for Clockwork Sentinel behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Melee attack (aggressive)
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));

        // Priority 2: Move towards target
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0f));

        // Priority 3: Random movement
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));

        // Priority 4: Look at player
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 5: Random look around
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Target selection: Aggressive towards players
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Create attribute supplier for Clockwork Sentinel
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0) // 15 hearts (tougher than Temporal Wraith)
            .add(Attributes.ATTACK_DAMAGE, 6.0) // 3 hearts (stronger attack)
            .add(Attributes.MOVEMENT_SPEED, 0.23) // Normal zombie speed (appears fast due to immunity to time distortion)
            .add(Attributes.ARMOR, 5.0) // Moderate armor protection
            .add(Attributes.FOLLOW_RANGE, 40.0); // Longer follow range (aggressive)
    }

    /**
     * Override canBeAffected to grant immunity to time distortion (Slowness IV)
     *
     * This makes Clockwork Sentinel a unique threat in Chronosphere, as it moves at normal speed
     * while other mobs are slowed down by the dimension's time distortion effect.
     */
    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        // Immune to Slowness effect (time distortion immunity)
        if (effect.getEffect() == net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) {
            return false;
        }

        return super.canBeAffected(effect);
    }

    /**
     * Override spawn rules to allow spawning in daylight
     * Chronosphere is always daytime, so we only check difficulty and spawn position validity
     */
    public static boolean checkClockworkSentinelSpawnRules(
        EntityType<ClockworkSentinelEntity> entityType,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        // Check difficulty (not Peaceful) and basic spawn position rules
        // Skip light level check entirely for Chronosphere
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        // Check if spawn position is valid (solid block below, etc.)
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }
}
