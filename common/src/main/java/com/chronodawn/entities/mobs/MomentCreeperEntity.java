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
 * Moment Creeper (モーメント・クリーパー) - Hostile Mob
 *
 * A creeper variant with glass-like body containing a frozen explosion effect.
 * Briefly "completely stops" before exploding. Explosion has less terrain damage
 * but stronger time debuff.
 *
 * Attributes:
 * - Health: 22 (11 hearts)
 * - Attack Damage: N/A (explosion-based)
 * - Movement Speed: 0.25 (standard creeper speed)
 *
 * Special Mechanics:
 * - Briefly freezes before exploding (future implementation)
 * - Explosion has less terrain damage, stronger time debuff (future implementation)
 *
 * Drops:
 * - Compressed Moment (future implementation)
 */
public class MomentCreeperEntity extends Monster {
    public MomentCreeperEntity(EntityType<? extends MomentCreeperEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5; // Standard XP reward
    }

    /**
     * Define AI goals for Moment Creeper behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Avoid players when powered (future: add SwellGoal for explosion)
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.0, 1.2));

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
     * Create attribute supplier for Moment Creeper
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 22.0) // 11 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.25) // Standard creeper speed
            .add(Attributes.FOLLOW_RANGE, 35.0); // Standard follow range
    }

    /**
     * Override spawn rules to allow spawning in daylight
     */
    public static boolean checkMomentCreeperSpawnRules(
        EntityType<MomentCreeperEntity> entityType,
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
