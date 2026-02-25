package com.chronodawn.entities.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import org.jetbrains.annotations.Nullable;

/**
 * Secondhand Archer - Skeleton-like hostile mob with ranged bow attacks.
 *
 * Spawns with a bow and attacks players with arrows.
 * Replaces Skeleton spawns in the Chrono Dawn dimension.
 */
public class SecondhandArcherEntity extends Monster implements RangedAttackMob {

    private final RangedBowAttackGoal<SecondhandArcherEntity> bowGoal = new RangedBowAttackGoal<>(this, 1.0, 20, 15.0F);
    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2, false);

    public SecondhandArcherEntity(EntityType<? extends SecondhandArcherEntity> entityType, Level level) {
        super(entityType, level);
        this.reassessWeaponGoal();
    }

    @Override
    public void aiStep() {
        boolean isSunBurn = this.isSunBurnTick();
        if (isSunBurn) {
            ItemStack headItem = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!headItem.isEmpty()) {
                if (headItem.isDamageableItem()) {
                    headItem.setDamageValue(headItem.getDamageValue() + this.random.nextInt(2));
                    if (headItem.getDamageValue() >= headItem.getMaxDamage()) {
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
                isSunBurn = false;
            }
            if (isSunBurn) {
                this.igniteForSeconds(8);
            }
        }
        super.aiStep();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RestrictSunGoal(this));
        this.goalSelector.addGoal(2, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    /**
     * Spawn rules: allows spawning in ChronoDawn daylight.
     */
    public static boolean checkSecondhandArcherSpawnRules(
        EntityType<SecondhandArcherEntity> entityType,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        BlockPos pos,
        RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor level,
        DifficultyInstance difficulty,
        MobSpawnType spawnReason,
        @Nullable SpawnGroupData spawnGroupData
    ) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.reassessWeaponGoal();
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    /**
     * Switch between bow and melee goals based on held item.
     */
    public void reassessWeaponGoal() {
        if (this.level() != null && !this.level().isClientSide) {
            this.goalSelector.removeGoal(this.meleeGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            ItemStack itemStack = this.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemStack.is(Items.BOW)) {
                this.goalSelector.addGoal(4, this.bowGoal);
            } else {
                this.goalSelector.addGoal(4, this.meleeGoal);
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        Arrow arrow = new Arrow(this.level(), this, new ItemStack(Items.ARROW), null);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + dist * 0.2, dz, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!this.level().isClientSide) {
            this.reassessWeaponGoal();
        }
    }
}
