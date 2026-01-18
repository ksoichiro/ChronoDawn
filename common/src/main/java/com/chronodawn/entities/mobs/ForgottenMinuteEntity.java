package com.chronodawn.entities.mobs;

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
 * Forgotten Minute (フォーゴトン・ミニット) - Hostile Mob
 *
 * A semi-transparent creature that suddenly appears and disappears after a short time.
 * When attacked, it strengthens nearby enemies.
 *
 * Attributes:
 * - Health: 14 (7 hearts)
 * - Attack Damage: 4 (2 hearts)
 * - Movement Speed: 0.4 (fast, similar to Vex)
 *
 * Special Mechanics:
 * - Suddenly appears and disappears after a short time (future implementation)
 * - When attacked, strengthens nearby enemies (future implementation)
 *
 * Drops:
 * - Faded Time Dust (future implementation)
 */
public class ForgottenMinuteEntity extends Monster {
    public ForgottenMinuteEntity(EntityType<? extends ForgottenMinuteEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5; // Standard XP reward
    }

    /**
     * Define AI goals for Forgotten Minute behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Melee attack
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));

        // Priority 2: Random movement
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));

        // Priority 3: Look at player
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 4: Random look around
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Target selection: Aggressive towards players
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Create attribute supplier for Forgotten Minute
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 14.0) // 7 hearts
            .add(Attributes.ATTACK_DAMAGE, 4.0) // 2 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.4) // Fast (Vex-like)
            .add(Attributes.FOLLOW_RANGE, 30.0); // Standard follow range
    }

    /**
     * Override spawn rules to allow spawning in daylight
     */
    public static boolean checkForgottenMinuteSpawnRules(
        EntityType<ForgottenMinuteEntity> entityType,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }
}
