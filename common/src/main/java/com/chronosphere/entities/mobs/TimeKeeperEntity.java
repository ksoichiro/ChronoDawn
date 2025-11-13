package com.chronosphere.entities.mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import com.chronosphere.registry.ModItems;
import java.util.Optional;

/**
 * Time Keeper (時間の管理者) - Neutral Mob with Trading
 *
 * A mysterious entity that maintains the flow of time in Chronosphere. Offers trades for time-related items.
 *
 * Attributes:
 * - Health: 20 (10 hearts)
 * - Movement Speed: 0.2 (slow and deliberate)
 *
 * Special Mechanics:
 * - Neutral mob (attacks only when provoked)
 * - Villager-like trading interface
 * - Offers time-related items in exchange for Clockstone, Enhanced Clockstone, etc.
 * - Affected by dimension-wide Slowness IV effect
 *
 * Spawn Conditions:
 * - Near Forgotten Library structure
 * - Rare spawn in chronosphere_plains, chronosphere_forest
 *
 * Trades (examples):
 * - 8 Clockstone → 1 Time Hourglass
 * - 16 Clockstone → 1 Portal Stabilizer material
 * - 4 Enhanced Clockstone → 1 Ancient Gear
 *
 * Reference: spec.md (FR-026, FR-029)
 * Task: T203 [P] [US1] Create Time Keeper entity
 */
public class TimeKeeperEntity extends AbstractVillager {
    // Entity data accessor for trade cooldown
    private static final EntityDataAccessor<Integer> TRADE_COOLDOWN =
        SynchedEntityData.defineId(TimeKeeperEntity.class, EntityDataSerializers.INT);

    public TimeKeeperEntity(EntityType<? extends TimeKeeperEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0; // No XP on death (neutral mob, killing discourages trading)
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TRADE_COOLDOWN, 0);
    }

    /**
     * Define AI goals for Time Keeper behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Look at trading player
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));

        // Priority 2: Avoid hostile mobs
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Mob.class, 8.0f, 0.6, 0.6,
            (entity) -> entity instanceof net.minecraft.world.entity.monster.Monster));

        // Priority 3: Random movement
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.4));

        // Priority 4: Look at player
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 5: Random look around
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Target selection: Retaliate when attacked (neutral behavior)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /**
     * Create attribute supplier for Time Keeper
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0) // 10 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.2) // Slow movement
            .add(Attributes.FOLLOW_RANGE, 16.0); // Moderate follow range
    }

    /**
     * Override mobInteract to open trading UI
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && this.isAlive() && !this.isTrading()) {
            // Refresh offers if needed
            if (this.getOffers().isEmpty()) {
                this.updateTrades();
            }

            // Open trading screen
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    /**
     * Update available trades
     */
    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();

        if (offers.isEmpty()) {
            // Trade 1: 16 Clockstone → 1 Glowstone Dust (Portal Stabilizer material)
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 16),
                new ItemStack(Items.GLOWSTONE_DUST, 4),
                5, // Max uses
                5, // XP reward for player
                0.05f // Price multiplier
            ));

            // Trade 2: 8 Clockstone → 1-3 Fruit of Time
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 8),
                new ItemStack(ModItems.FRUIT_OF_TIME.get(), 3),
                5, // Max uses
                3, // XP reward
                0.05f
            ));

            // Trade 3: 1 Enhanced Clockstone + 16 Clockstone → 1 Ender Pearl (Portal Stabilizer material)
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.ENHANCED_CLOCKSTONE.get(), 1),
                Optional.of(new ItemCost(ModItems.CLOCKSTONE.get(), 16)),
                new ItemStack(Items.ENDER_PEARL, 1),
                3, // Max uses (rare trade)
                10, // Higher XP reward
                0.05f
            ));

            // Trade 4: 2 Enhanced Clockstone → 4 Time Wood Sapling
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.ENHANCED_CLOCKSTONE.get(), 2),
                new ItemStack(com.chronosphere.registry.ModBlocks.TIME_WOOD_SAPLING.get().asItem(), 4),
                3, // Max uses
                5, // XP reward
                0.05f
            ));
        }
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        // No XP reward for Time Keeper
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TradeCooldown", this.entityData.get(TRADE_COOLDOWN));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(TRADE_COOLDOWN, compound.getInt("TradeCooldown"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    @Override
    public boolean showProgressBar() {
        return false; // Time Keeper doesn't show profession progress bar
    }

    @Nullable
    @Override
    public net.minecraft.world.entity.AgeableMob getBreedOffspring(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.AgeableMob mate) {
        return null; // Time Keeper cannot breed
    }

    /**
     * Spawn rules for Time Keeper
     * Spawns in bright areas (light level >= 9) like peaceful mobs
     */
    public static boolean checkTimeKeeperSpawnRules(
        EntityType<TimeKeeperEntity> entityType,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        // Check basic mob spawn rules and require bright area (like animals)
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random)
            && level.getRawBrightness(pos, 0) >= 9;
    }
}
