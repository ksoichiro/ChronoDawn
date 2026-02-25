/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.entities.mobs;

import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Secondwing Fowl - A friendly creature similar to vanilla Chicken.
 * Spawns in ChronoDawn dimension biomes, replacing vanilla chickens.
 * Drops chicken like vanilla chickens.
 */
public class SecondwingFowlEntity extends Animal {

    public SecondwingFowlEntity(EntityType<? extends SecondwingFowlEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, stack -> this.isFood(stack), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)  // Same as vanilla chicken
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.TEMPT_RANGE, 10.0);
    }

    /**
     * Check if this entity can spawn at the given location.
     */
    public static boolean checkSecondwingFowlSpawnRules(
            EntityType<SecondwingFowlEntity> entityType,
            LevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random) {
        return Animal.checkAnimalSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS) || stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS) || stack.is(Items.TORCHFLOWER_SEEDS) || stack.is(Items.PITCHER_POD) || stack.is(ModItems.TIME_WHEAT_SEEDS) || stack.is(ModItems.CHRONO_MELON_SEEDS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.SECONDWING_FOWL.get().create(level, EntitySpawnReason.BREEDING);
    }

    // === Sounds (same as vanilla Chicken) ===

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }
}
