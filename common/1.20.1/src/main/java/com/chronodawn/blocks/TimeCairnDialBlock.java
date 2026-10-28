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
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatLevelTime;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.WaysideSettings;
import com.chronodawn.core.wayside.CachePlacement;
import com.chronodawn.core.wayside.CairnHour;
import com.chronodawn.core.wayside.EchoWalk;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import com.chronodawn.registry.ModBlockEntities;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * The dial of a Time Cairn. Right-clicking it gives the cairn stone of the current hour of
 * the day, once per cooldown for each player. Holding a Cairn Keystone sets it into the dial
 * instead, which trades it for a reward without touching the cooldown.
 */
public class TimeCairnDialBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);
    private static final String KEYSTONE_LOOT_TABLE = "gameplay/time_cairn_keystone";

    // Given items pop out above the dial, with a small random sideways push.
    private static final double REWARD_SPAWN_HEIGHT = 0.4;
    private static final double REWARD_UP_SPEED = 0.25;
    private static final double REWARD_SIDE_SPEED = 0.1;

    private static final double RING_RADIUS = 0.45;
    private static final double RING_HEIGHT = 0.2;
    private static final int TURN_SPARKS = 12;
    private static final int TURN_DUST = 8;
    private static final float TURN_SOUND_VOLUME = 0.8f;
    private static final int KEYSTONE_SPARKS = 24;
    private static final int KEYSTONE_DUST_PER_HOUR = 6;
    private static final double KEYSTONE_RISE = 1.5;
    private static final float KEYSTONE_SOUND_VOLUME = 0.7f;
    private static final float KEYSTONE_SOUND_PITCH = 1.0f;

    private static final TagKey<Block> CACHE_GROUND = TagKey.create(Registries.BLOCK,
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "time_cairn_cache_ground"));
    private static final String CACHE_LOOT_TABLE = "chests/time_cairn_echo_cache";

    private static final int ECHO_START_SOULS = 16;
    private static final int ECHO_START_SPARKS = 4;
    // The start column runs from 1.0 to 2.8 blocks above the dial block's base.
    private static final double ECHO_START_CENTER_HEIGHT = 1.9;
    private static final double ECHO_START_SPREAD_Y = 0.9;
    private static final double ECHO_START_SPREAD_XZ = 0.15;
    private static final float ECHO_START_SOUND_VOLUME = 1.0f;
    private static final float ECHO_START_SOUND_PITCH = 0.8f;
    private static final int ECHO_WALK_SOULS = 6;
    private static final int ECHO_WALK_SPARKS = 1;
    // The walker is drawn at about waist height over the ground.
    private static final double ECHO_WALK_HEIGHT = 0.9;
    private static final double ECHO_WALK_SPREAD_XZ = 0.25;
    private static final double ECHO_WALK_SPREAD_Y = 0.5;
    private static final int ECHO_ARRIVAL_SOULS = 20;
    private static final double ECHO_ARRIVAL_SINK_SPEED = -0.1;
    private static final double ECHO_ARRIVAL_SPREAD = 0.3;
    private static final int ECHO_LINGER_PERIOD = 40;
    private static final int ECHO_LINGER_SOULS = 2;
    // The marker block sits right over the chest, so this is the top surface of the cache.
    private static final int ECHO_CACHE_MARKER_HEIGHT = 2;

    // Test-only overrides, see forceEchoChance and forceEchoDistances.
    @Nullable
    private static Float forcedEchoChance;
    @Nullable
    private static int[] forcedEchoDistances;

    /** What a turn of the dial did. */
    public enum TurnResult {
        /** The stone was given and no echo rose. */
        GIVEN,
        /** The player is still on cooldown, so nothing was given. */
        COOLDOWN,
        /** The stone was given and an echo rose from the cairn. */
        GIVEN_WITH_ECHO
    }

    public TimeCairnDialBlock(Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5f, 5.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimeCairnDialBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // A plain item in hand still reaches this method, so the keystone is checked here.
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (holdsKeystone(player)) {
                setKeystone(serverLevel, pos, player);
            } else {
                turn(serverLevel, pos, player, CompatLevelTime.dayTime(serverLevel));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Test-only override of the echo chance, so a GameTest does not depend on the config or on
     * the random roll. Pass null to restore the configured chance, and always do so when the
     * test ends.
     */
    public static void forceEchoChance(@Nullable Float chance) {
        forcedEchoChance = chance;
    }

    /**
     * Test-only override of the distance range the turn's echo searches, so a GameTest can keep
     * the cache inside its template instead of on whatever lies 16 to 24 blocks away. Pass null
     * to restore the production range, and always do so when the test ends.
     */
    public static void forceEchoDistances(@Nullable int[] minAndMax) {
        forcedEchoDistances = minAndMax;
    }

    private static float echoChance(WaysideSettings settings) {
        Float forced = forcedEchoChance;
        return forced != null ? forced : settings.timeCairnEchoChance();
    }

    public static boolean holdsKeystone(Player player) {
        return player.getMainHandItem().is(ModItems.CAIRN_KEYSTONE.get())
            || player.getOffhandItem().is(ModItems.CAIRN_KEYSTONE.get());
    }

    /**
     * Turns the dial for a player: gives the stone of the hour read from the day time, unless
     * the player's cooldown at this dial has not run out.
     */
    public static TurnResult turn(ServerLevel level, BlockPos pos, Player player, long dayTime) {
        if (!(level.getBlockEntity(pos) instanceof TimeCairnDialBlockEntity dial)) {
            // Without its block entity the dial cannot track cooldowns, so it refuses.
            return TurnResult.COOLDOWN;
        }
        WaysideSettings settings = ChronoDawnConfig.get().gameplay().wayside();
        long remaining = dial.tryActivate(player.getUUID(), level.getGameTime(), settings.timeCairnCooldownTicks());
        if (remaining > 0) {
            player.displayClientMessage(
                Component.translatable("message.chronodawn.time_cairn.cooldown",
                    PlayerCooldownTracker.toMinutesRoundedUp(remaining)),
                true
            );
            return TurnResult.COOLDOWN;
        }

        CairnHour hour = CairnHour.forDayTime(dayTime);
        spawnReward(level, pos, new ItemStack(ModItems.cairnStone(hour)));
        spawnTurnParticles(level, pos, hour);
        level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS,
            TURN_SOUND_VOLUME, hour.soundPitch());
        player.displayClientMessage(Component.translatable(hour.messageKey()), true);

        // A second echo would replace the first player's walker mid-walk.
        if (hour == CairnHour.MIDNIGHT && !dial.echoActive()
                && level.random.nextFloat() < echoChance(settings)
                && summonEcho(level, pos, player, level.random).isPresent()) {
            return TurnResult.GIVEN_WITH_ECHO;
        }
        return TurnResult.GIVEN;
    }

    /**
     * Sets the keystone in either hand into the dial and spawns its reward. Never touches the
     * turn cooldown.
     *
     * @return false, with nothing done, when neither hand holds a keystone
     */
    public static boolean setKeystone(ServerLevel level, BlockPos pos, Player player) {
        ItemStack keystone = player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.CAIRN_KEYSTONE.get())
            ? player.getItemInHand(InteractionHand.MAIN_HAND)
            : player.getItemInHand(InteractionHand.OFF_HAND);
        if (!keystone.is(ModItems.CAIRN_KEYSTONE.get())) {
            return false;
        }
        keystone.shrink(1);
        // An emptied table still consumes the keystone: a datapack that empties it chose that.
        for (ItemStack stack : rollKeystoneReward(level, pos, player)) {
            if (!stack.isEmpty()) {
                spawnReward(level, pos, stack);
            }
        }
        spawnKeystoneParticles(level, pos);
        level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS,
            KEYSTONE_SOUND_VOLUME, KEYSTONE_SOUND_PITCH);
        player.displayClientMessage(Component.translatable("message.chronodawn.time_cairn.keystone"), true);
        return true;
    }

    // The player is optional so GameTests can roll without one. When present, datapacks can
    // condition on the player and luck applies, as with vanilla chest loot.
    public static List<ItemStack> rollKeystoneReward(ServerLevel level, BlockPos pos, @Nullable Player player) {
        LootTable table = level.getServer().getLootData().getLootTable(
            CompatResourceLocation.create(ChronoDawn.MOD_ID, KEYSTONE_LOOT_TABLE));
        LootParams.Builder builder = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
        if (player != null) {
            builder.withParameter(LootContextParams.THIS_ENTITY, player).withLuck(player.getLuck());
        }
        return table.getRandomItems(builder.create(LootContextParamSets.CHEST));
    }

    /**
     * Pops the stack out above the dial instead of into the player's inventory, so a full
     * inventory loses nothing.
     */
    public static void spawnReward(ServerLevel level, BlockPos pos, ItemStack stack) {
        RandomSource random = level.random;
        ItemEntity item = new ItemEntity(level,
            pos.getX() + 0.5, pos.getY() + REWARD_SPAWN_HEIGHT, pos.getZ() + 0.5, stack);
        item.setDeltaMovement(
            (random.nextDouble() * 2.0 - 1.0) * REWARD_SIDE_SPEED,
            REWARD_UP_SPEED,
            (random.nextDouble() * 2.0 - 1.0) * REWARD_SIDE_SPEED);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.TIME_CAIRN_DIAL.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (tickLevel instanceof ServerLevel serverLevel && blockEntity instanceof TimeCairnDialBlockEntity dial) {
                tickEcho(serverLevel, pos, dial, serverLevel.getGameTime());
            }
        };
    }

    /**
     * Raises the midnight echo at the production distance, unless a test forced another range. See the six-argument overload.
     */
    public static Optional<BlockPos> summonEcho(ServerLevel level, BlockPos dialPos, Player player, RandomSource random) {
        int[] forced = forcedEchoDistances;
        return forced != null
            ? summonEcho(level, dialPos, player, random, forced[0], forced[1])
            : summonEcho(level, dialPos, player, random, CachePlacement.MIN_DISTANCE, CachePlacement.MAX_DISTANCE);
    }

    /**
     * Buries a cache and starts the echo that walks to it. The cache is placed before the walk
     * starts, so it exists even if the server stops right after.
     *
     * @return the chest position when an echo rose, else empty because no ground fit
     */
    public static Optional<BlockPos> summonEcho(ServerLevel level, BlockPos dialPos, Player player, RandomSource random,
                                                int minDistance, int maxDistance) {
        if (!(level.getBlockEntity(dialPos) instanceof TimeCairnDialBlockEntity dial)) {
            return Optional.empty();
        }
        Optional<BlockPos> spot = findCacheSpot(level, dialPos, random, minDistance, maxDistance);
        if (spot.isEmpty()) {
            return Optional.empty();
        }
        BlockPos chestPos = spot.get().below();
        placeCache(level, spot.get());
        dial.startEcho(new EchoWalk(dialPos.getX() + 0.5, dialPos.getZ() + 0.5,
            chestPos.getX() + 0.5, chestPos.getZ() + 0.5, level.getGameTime()), chestPos);

        double x = dialPos.getX() + 0.5;
        double y = dialPos.getY() + ECHO_START_CENTER_HEIGHT;
        double z = dialPos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SOUL, x, y, z, ECHO_START_SOULS,
            ECHO_START_SPREAD_XZ, ECHO_START_SPREAD_Y, ECHO_START_SPREAD_XZ, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, ECHO_START_SPARKS,
            ECHO_START_SPREAD_XZ, ECHO_START_SPREAD_Y, ECHO_START_SPREAD_XZ, 0.0);
        level.playSound(null, dialPos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS,
            ECHO_START_SOUND_VOLUME, ECHO_START_SOUND_PITCH);
        player.displayClientMessage(Component.translatable("message.chronodawn.time_cairn.echo"), true);
        return Optional.of(chestPos);
    }

    /**
     * Looks for ground to bury the cache in, trying the candidate directions in order.
     *
     * @return the marker position, the ground block that stays visible over the chest, or empty
     *         when no candidate has loaded, solid ground
     */
    public static Optional<BlockPos> findCacheSpot(ServerLevel level, BlockPos dialPos, RandomSource random,
                                                   int minDistance, int maxDistance) {
        for (int[] offset : CachePlacement.candidateOffsets(random.nextLong(), minDistance, maxDistance)) {
            int x = dialPos.getX() + offset[0];
            int z = dialPos.getZ() + offset[1];
            if (!level.hasChunkAt(new BlockPos(x, dialPos.getY(), z))) {
                continue;
            }
            // The heightmap counts fluids, so over water this lands on a water block, which is
            // not cache ground.
            BlockPos marker = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            if (level.getBlockState(marker).is(Blocks.SNOW)) {
                marker = marker.below();
            }
            if (level.getBlockState(marker).is(CACHE_GROUND) && canHoldChest(level, marker.below())) {
                return Optional.of(marker);
            }
        }
        return Optional.empty();
    }

    private static boolean canHoldChest(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolidRender(level, pos) && !state.hasBlockEntity() && state.getFluidState().isEmpty();
    }

    /**
     * Buries the cache chest right under the marker position and covers it with the coarse
     * variant of the ground that was there.
     */
    public static void placeCache(ServerLevel level, BlockPos markerPos) {
        BlockPos above = markerPos.above();
        if (level.getBlockState(above).is(Blocks.SNOW)) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        Block marker = markerFor(level.getBlockState(markerPos));
        BlockPos chestPos = markerPos.below();
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState()
            .setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(level.random)), Block.UPDATE_ALL);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(CompatResourceLocation.create(ChronoDawn.MOD_ID, CACHE_LOOT_TABLE), level.random.nextLong());
        }
        level.setBlock(markerPos, marker.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** The disturbed-ground block that marks a cache dug into the given ground. */
    public static Block markerFor(BlockState ground) {
        if (ground.is(ModBlocks.TEMPORAL_SAND.get())) {
            return ModBlocks.TEMPORAL_GRAVEL.get();
        }
        if (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.PODZOL)
                || ground.is(Blocks.COARSE_DIRT)) {
            return Blocks.COARSE_DIRT;
        }
        return ModBlocks.COARSE_TEMPORAL_DIRT.get();
    }

    /**
     * Moves the echo along its walk and draws it. Does nothing when no echo is active. The game
     * time is passed in so a GameTest can step through the walk without waiting.
     */
    public static void tickEcho(ServerLevel level, BlockPos pos, TimeCairnDialBlockEntity dial, long gameTime) {
        EchoWalk walk = dial.echo;
        BlockPos cache = dial.cachePos;
        if (walk == null || cache == null) {
            return;
        }
        if (walk.finished(gameTime)) {
            dial.clearEcho();
            return;
        }
        if (!walk.arrived(gameTime)) {
            double[] at = walk.positionAt(gameTime);
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (int) Math.floor(at[0]), (int) Math.floor(at[1]));
            double y = surface + ECHO_WALK_HEIGHT;
            level.sendParticles(ParticleTypes.SOUL, at[0], y, at[1], ECHO_WALK_SOULS,
                ECHO_WALK_SPREAD_XZ, ECHO_WALK_SPREAD_Y, ECHO_WALK_SPREAD_XZ, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, at[0], y, at[1], ECHO_WALK_SPARKS,
                ECHO_WALK_SPREAD_XZ, ECHO_WALK_SPREAD_Y, ECHO_WALK_SPREAD_XZ, 0.0);
            return;
        }
        // The flag, not an exact tick match, so a skipped tick still sends the burst once.
        if (!dial.arrivalBurstSent) {
            dial.arrivalBurstSent = true;
            // One call per particle with a zero count: the offset then is a velocity, so the souls
            // sink into the marker instead of scattering.
            for (int i = 0; i < ECHO_ARRIVAL_SOULS; i++) {
                level.sendParticles(ParticleTypes.SOUL,
                    cache.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * ECHO_ARRIVAL_SPREAD,
                    cache.getY() + ECHO_CACHE_MARKER_HEIGHT,
                    cache.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * ECHO_ARRIVAL_SPREAD,
                    0, 0.0, 1.0, 0.0, ECHO_ARRIVAL_SINK_SPEED);
            }
        }
        if (gameTime % ECHO_LINGER_PERIOD == 0) {
            level.sendParticles(ParticleTypes.SOUL, cache.getX() + 0.5, cache.getY() + ECHO_CACHE_MARKER_HEIGHT, cache.getZ() + 0.5,
                ECHO_LINGER_SOULS, 0.1, 0.2, 0.1, 0.0);
        }
    }

    private static DustParticleOptions dust(CairnHour hour) {
        int argb = hour.dustColorArgb();
        return new DustParticleOptions(new Vector3f(
            ((argb >> 16) & 0xFF) / 255.0f,
            ((argb >> 8) & 0xFF) / 255.0f,
            (argb & 0xFF) / 255.0f), 1.0f);
    }

    private static void spawnTurnParticles(ServerLevel level, BlockPos pos, CairnHour hour) {
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        double y = pos.getY() + RING_HEIGHT;
        for (int i = 0; i < TURN_SPARKS; i++) {
            double angle = 2 * Math.PI * i / TURN_SPARKS;
            level.sendParticles(ParticleTypes.END_ROD,
                centerX + Math.cos(angle) * RING_RADIUS, y, centerZ + Math.sin(angle) * RING_RADIUS,
                1, 0.0, 0.0, 0.0, 0.0);
        }
        DustParticleOptions dust = dust(hour);
        for (int i = 0; i < TURN_DUST; i++) {
            double angle = 2 * Math.PI * i / TURN_DUST;
            level.sendParticles(dust,
                centerX + Math.cos(angle) * RING_RADIUS, y, centerZ + Math.sin(angle) * RING_RADIUS,
                1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * A whole day over the dial: sparks rise on one column, and the four hours' dust follows
     * in sequence, each color in its own band of the climb.
     */
    private static void spawnKeystoneParticles(ServerLevel level, BlockPos pos) {
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        double baseY = pos.getY() + RING_HEIGHT;
        for (int i = 0; i < KEYSTONE_SPARKS; i++) {
            level.sendParticles(ParticleTypes.END_ROD,
                centerX, baseY + KEYSTONE_RISE * i / KEYSTONE_SPARKS, centerZ,
                1, 0.1, 0.0, 0.1, 0.0);
        }
        CairnHour[] hours = CairnHour.values();
        int steps = hours.length * KEYSTONE_DUST_PER_HOUR;
        for (int h = 0; h < hours.length; h++) {
            DustParticleOptions dust = dust(hours[h]);
            for (int j = 0; j < KEYSTONE_DUST_PER_HOUR; j++) {
                int step = h * KEYSTONE_DUST_PER_HOUR + j;
                double angle = 2 * Math.PI * step / KEYSTONE_DUST_PER_HOUR;
                level.sendParticles(dust,
                    centerX + Math.cos(angle) * RING_RADIUS, baseY + KEYSTONE_RISE * step / steps,
                    centerZ + Math.sin(angle) * RING_RADIUS,
                    1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}
