package com.chronodawn.entities.mobs;

import com.chronodawn.items.TimeCompassItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import com.chronodawn.registry.ModItems;

import java.util.Optional;

/**
 * Time Keeper (時間の管理者) - Neutral Mob with Trading
 *
 * A mysterious entity that maintains the flow of time in ChronoDawn. Offers trades for time-related items.
 *
 * Attributes:
 * - Health: 20 (10 hearts)
 * - Movement Speed: 0.2 (slow and deliberate)
 *
 * Special Mechanics:
 * - Neutral mob (attacks only when provoked)
 * - Villager-like trading interface
 * - Offers time-related items in exchange for Clockstone, Enhanced Clockstone, etc.
 * - **NOT** affected by dimension-wide Slowness IV effect (excluded from time distortion)
 *
 * Spawn Conditions:
 * - Near Forgotten Library structure
 * - Rare spawn in chronodawn_plains, chronodawn_forest
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
        // Mark as invulnerable so hostile mobs ignore this entity
        this.setInvulnerable(true);
    }

    /**
     * Prevent despawning - Time Keepers are important NPCs that should persist.
     * Similar to vanilla Villagers.
     */
    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    /**
     * Never despawn regardless of distance from players.
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /**
     * Override hurtServer to allow player attacks despite being invulnerable.
     * The invulnerable flag makes hostile mobs ignore Time Keeper,
     * but we still want players to be able to attack if needed.
     */
    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        // Allow damage that bypasses invulnerability (/kill, void, etc.)
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(serverLevel, source, amount);
        }
        // Allow damage from players
        if (source.getEntity() instanceof Player) {
            // Temporarily remove invulnerable flag to process damage
            this.setInvulnerable(false);
            boolean result = super.hurtServer(serverLevel, source, amount);
            this.setInvulnerable(true);
            return result;
        }
        // Block all other damage
        return false;
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
            // Trade 1: 16 Clockstone → 4 Glowstone Dust (Portal Stabilizer material)
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 16),
                new ItemStack(Items.GLOWSTONE_DUST, 4),
                5, // Max uses
                5, // XP reward for player
                0.05f // Price multiplier
            ));

            // Trade 2: 8 Clockstone → 3 Fruit of Time
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 8),
                new ItemStack(ModItems.FRUIT_OF_TIME.get(), 3),
                5, // Max uses
                3, // XP reward
                0.05f
            ));

            // Trade 3: 4 Fruit of Time → 8 String
            // Early-game resource for bow crafting (cobwebs may be scarce)
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.FRUIT_OF_TIME.get(), 4),
                new ItemStack(Items.STRING, 8),
                8, // Max uses (renewable resource)
                2, // XP reward
                0.05f
            ));

            // Trade 4: 1 Enhanced Clockstone + 16 Clockstone → 1 Ender Pearl (Portal Stabilizer material)
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.ENHANCED_CLOCKSTONE.get(), 1),
                Optional.of(new ItemCost(ModItems.CLOCKSTONE.get(), 16)),
                new ItemStack(Items.ENDER_PEARL, 1),
                3, // Max uses (rare trade)
                10, // Higher XP reward
                0.05f
            ));

            // Trade 5: 2 Enhanced Clockstone → 4 Time Wood Sapling
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.ENHANCED_CLOCKSTONE.get(), 2),
                new ItemStack(com.chronodawn.registry.ModBlocks.TIME_WOOD_SAPLING.get().asItem(), 4),
                3, // Max uses
                5, // XP reward
                0.05f
            ));

            // Trade 6: 12 Clockstone + 4 Time Crystal → Time Compass (Desert Clock Tower)
            // Points to nearest Desert Clock Tower in ChronoDawn
            // Early-game guidance item (accessible right after entering ChronoDawn)
            // Recommended visit order: 1st
            ItemStack desertTowerCompass = createCompassWithPosition(TimeCompassItem.STRUCTURE_DESERT_CLOCK_TOWER);
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 12),
                Optional.of(new ItemCost(ModItems.TIME_CRYSTAL.get(), 4)),
                desertTowerCompass,
                3, // Max uses (can buy multiple for party members)
                10, // XP reward
                0.05f
            ));

            // Trade 7: 12 Clockstone + 6 Time Crystal → Time Compass (Phantom Catacombs)
            // Points to Phantom Catacombs mid-boss dungeon
            // Recommended visit order: 2nd (after Desert Clock Tower)
            ItemStack phantomCatacombsCompass = createCompassWithPosition(TimeCompassItem.STRUCTURE_PHANTOM_CATACOMBS);
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 12),
                Optional.of(new ItemCost(ModItems.TIME_CRYSTAL.get(), 6)),
                phantomCatacombsCompass,
                2, // Max uses (mid-boss dungeon compass)
                12, // XP reward
                0.05f
            ));

            // Trade 8: 12 Clockstone + 6 Time Crystal → Time Compass (Guardian Vault)
            // Points to Guardian Vault mid-boss dungeon
            // Recommended visit order: 3rd
            ItemStack guardianVaultCompass = createCompassWithPosition(TimeCompassItem.STRUCTURE_GUARDIAN_VAULT);
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 12),
                Optional.of(new ItemCost(ModItems.TIME_CRYSTAL.get(), 6)),
                guardianVaultCompass,
                2, // Max uses (mid-boss dungeon compass)
                12, // XP reward
                0.05f
            ));

            // Trade 9: 12 Clockstone + 6 Time Crystal → Time Compass (Clockwork Depths)
            // Points to Clockwork Depths mid-boss dungeon
            // Recommended visit order: 4th
            ItemStack clockworkDepthsCompass = createCompassWithPosition(TimeCompassItem.STRUCTURE_CLOCKWORK_DEPTHS);
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 12),
                Optional.of(new ItemCost(ModItems.TIME_CRYSTAL.get(), 6)),
                clockworkDepthsCompass,
                2, // Max uses (mid-boss dungeon compass)
                12, // XP reward
                0.05f
            ));

            // Trade 10: 12 Clockstone + 6 Time Crystal → Time Compass (Entropy Crypt)
            // Points to Entropy Crypt mid-boss dungeon
            // Recommended visit order: 5th
            ItemStack entropyCryptCompass = createCompassWithPosition(TimeCompassItem.STRUCTURE_ENTROPY_CRYPT);
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 12),
                Optional.of(new ItemCost(ModItems.TIME_CRYSTAL.get(), 6)),
                entropyCryptCompass,
                2, // Max uses (mid-boss dungeon compass)
                12, // XP reward
                0.05f
            ));

            // Trade 11: 16 Clockstone + 8 Time Crystal → Time Compass (Master Clock)
            // Points to nearest Master Clock Tower in ChronoDawn
            // End-game guidance item (after 4 mid-boss dungeons)
            // Recommended visit order: 6th (final destination)
            ItemStack masterClockCompass = createCompassWithPosition(TimeCompassItem.STRUCTURE_MASTER_CLOCK);
            offers.add(new MerchantOffer(
                new ItemCost(ModItems.CLOCKSTONE.get(), 16),
                Optional.of(new ItemCost(ModItems.TIME_CRYSTAL.get(), 8)),
                masterClockCompass,
                1, // Max uses (rare, powerful item)
                15, // Higher XP reward
                0.05f
            ));
        }
    }

    /**
     * Create a Time Compass without coordinates pre-set.
     * Player must right-click the compass to search for the structure.
     *
     * Design Decision: Structure search is NOT performed during trade creation
     * because it would block world loading and cause performance issues.
     * Instead, players manually trigger the search by right-clicking the compass.
     *
     * @param structureType Structure type to locate
     * @return Time Compass with target structure type set, but no coordinates yet
     */
    private ItemStack createCompassWithPosition(String structureType) {
        ItemStack compass = TimeCompassItem.createCompass(structureType);
        // Coordinates will be set when player right-clicks the compass (see TimeCompassItem.use())
        return compass;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        super.notifyTrade(offer);
        // No post-processing needed - compass coordinates are set when player uses the item
    }

    /**
     * Locate a structure and set its position in the Time Compass.
     *
     * @param serverLevel Server level to search in
     * @param compassStack Time Compass ItemStack
     * @param structureType Structure type to locate
     */
    private void locateAndSetStructurePosition(ServerLevel serverLevel, ItemStack compassStack, String structureType) {
        com.chronodawn.ChronoDawn.LOGGER.debug("Time Keeper: Locating structure for compass - type: {}", structureType);

        // Determine which dimension to search in
        ServerLevel searchLevel = serverLevel;
        ResourceLocation structureId;

        switch (structureType) {
            case TimeCompassItem.STRUCTURE_ANCIENT_RUINS:
                // Ancient Ruins are in the Overworld
                searchLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
                structureId = CompatResourceLocation.create("chronodawn", "ancient_ruins");
                break;
            case TimeCompassItem.STRUCTURE_DESERT_CLOCK_TOWER:
                // Desert Clock Tower is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "desert_clock_tower");
                break;
            case TimeCompassItem.STRUCTURE_MASTER_CLOCK:
                // Master Clock is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "master_clock");
                break;
            case TimeCompassItem.STRUCTURE_PHANTOM_CATACOMBS:
                // Phantom Catacombs is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "phantom_catacombs");
                break;
            case TimeCompassItem.STRUCTURE_GUARDIAN_VAULT:
                // Guardian Vault is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "guardian_vault");
                break;
            case TimeCompassItem.STRUCTURE_CLOCKWORK_DEPTHS:
                // Clockwork Depths is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "clockwork_depths");
                break;
            case TimeCompassItem.STRUCTURE_ENTROPY_CRYPT:
                // Entropy Crypt is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "entropy_crypt");
                break;
            default:
                com.chronodawn.ChronoDawn.LOGGER.warn("Time Keeper: Unknown structure type: {}", structureType);
                return;
        }

        if (searchLevel == null) {
            com.chronodawn.ChronoDawn.LOGGER.warn("Time Keeper: Search level is null for structure: {}", structureType);
            return;
        }

        // Get trading player's position as search origin
        Player tradingPlayer = this.getTradingPlayer();
        BlockPos searchOrigin = tradingPlayer != null ? tradingPlayer.blockPosition() : this.blockPosition();
        com.chronodawn.ChronoDawn.LOGGER.debug("Time Keeper: Searching from position: {}", searchOrigin);

        // Get structure registry
        // In 1.21.2, get() returns Optional<Holder.Reference<T>>
        var structureRegistry = searchLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var structureHolderOpt = structureRegistry.get(structureId);

        if (structureHolderOpt.isPresent()) {
            com.chronodawn.ChronoDawn.LOGGER.debug("Time Keeper: Structure found in registry: {}", structureId);

            // Create HolderSet for the single structure
            HolderSet<Structure> structureSet = HolderSet.direct(structureHolderOpt.get());

            // Locate nearest structure
            var structurePair = searchLevel.getChunkSource().getGenerator().findNearestMapStructure(
                searchLevel,
                structureSet,
                searchOrigin,
                100, // Search radius in chunks
                false // Skip known structures
            );

            if (structurePair != null) {
                BlockPos structurePos = structurePair.getFirst();
                GlobalPos globalPos = GlobalPos.of(searchLevel.dimension(), structurePos);
                TimeCompassItem.setTargetPosition(compassStack, globalPos);
                com.chronodawn.ChronoDawn.LOGGER.debug("Time Keeper: Successfully set compass target to: {}", structurePos);
            } else {
                com.chronodawn.ChronoDawn.LOGGER.warn("Time Keeper: No structure found within 100 chunks of {}", searchOrigin);
            }
        } else {
            com.chronodawn.ChronoDawn.LOGGER.warn("Time Keeper: Structure not found in registry: {}", structureId);
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
        EntitySpawnReason spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        // Check basic mob spawn rules and require bright area (like animals)
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random)
            && level.getRawBrightness(pos, 0) >= 9;
    }
}
