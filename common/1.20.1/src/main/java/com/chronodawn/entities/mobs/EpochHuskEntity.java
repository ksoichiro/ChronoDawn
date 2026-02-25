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
 * Epoch Husk (エポック・ハスク) - Hostile Mob
 *
 * A weathered humanoid creature with parts crumbling like an hourglass.
 * When attacked, it deploys a temporary "time slowdown aura" that slightly reduces player attack speed.
 *
 * Attributes:
 * - Health: 24 (12 hearts)
 * - Attack Damage: 5 (2.5 hearts)
 * - Movement Speed: 0.23 (normal zombie speed)
 *
 * Special Mechanics:
 * - When damaged, deploys time slowdown aura (future implementation)
 * - Slows player attack speed slightly (future implementation)
 *
 * Drops:
 * - Temporal Fragment
 * - Decayed Clock Part (decorative/crafting item) (future implementation)
 */
public class EpochHuskEntity extends Monster {
    public EpochHuskEntity(EntityType<? extends EpochHuskEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5; // Standard XP reward for basic hostile mob
    }

    /**
     * Define AI goals for Epoch Husk behavior
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

        // Target selection: Aggressive towards players
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Create attribute supplier for Epoch Husk
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 24.0) // 12 hearts
            .add(Attributes.ATTACK_DAMAGE, 5.0) // 2.5 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.23) // Normal zombie speed
            .add(Attributes.FOLLOW_RANGE, 35.0); // Standard follow range
    }

    /**
     * Override spawn rules to allow spawning in daylight
     */
    public static boolean checkEpochHuskSpawnRules(
        EntityType<EpochHuskEntity> entityType,
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
