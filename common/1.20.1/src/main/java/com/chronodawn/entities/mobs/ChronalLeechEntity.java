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
 * Chronal Leech (クローナル・リーチ) - Hostile Mob
 *
 * A small creature similar to silverfish that extends player cooldowns when attacking.
 * Spawns in groups.
 *
 * Attributes:
 * - Health: 10 (5 hearts)
 * - Attack Damage: 2 (1 heart)
 * - Movement Speed: 0.25 (silverfish-like)
 *
 * Special Mechanics:
 * - Extends player attack cooldown when attacking (future implementation)
 * - Spawns in groups (future implementation)
 *
 * Drops:
 * - Leaked Time (future implementation)
 */
public class ChronalLeechEntity extends Monster {
    public ChronalLeechEntity(EntityType<? extends ChronalLeechEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 3; // Lower XP due to swarm nature
    }

    /**
     * Define AI goals for Chronal Leech behavior
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
     * Create attribute supplier for Chronal Leech
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 10.0) // 5 hearts
            .add(Attributes.ATTACK_DAMAGE, 2.0) // 1 heart
            .add(Attributes.MOVEMENT_SPEED, 0.25) // Silverfish-like speed
            .add(Attributes.FOLLOW_RANGE, 20.0); // Shorter follow range
    }

    /**
     * Override spawn rules to allow spawning in daylight
     */
    public static boolean checkChronalLeechSpawnRules(
        EntityType<ChronalLeechEntity> entityType,
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
