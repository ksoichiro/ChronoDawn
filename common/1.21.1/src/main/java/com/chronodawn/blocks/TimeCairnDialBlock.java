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
import com.chronodawn.core.wayside.CairnHour;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import com.chronodawn.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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
    public static final MapCodec<TimeCairnDialBlock> CODEC = simpleCodec(TimeCairnDialBlock::new);
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

    // Test-only override of the echo chance, see forceEchoChance.
    @Nullable
    private static Float forcedEchoChance;

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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimeCairnDialBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
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

        if (hour == CairnHour.MIDNIGHT && level.random.nextFloat() < echoChance(settings)
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
        LootTable table = level.getServer().reloadableRegistries().getLootTable(
            ResourceKey.create(Registries.LOOT_TABLE, CompatResourceLocation.create(ChronoDawn.MOD_ID, KEYSTONE_LOOT_TABLE)));
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

    /**
     * Placeholder for the midnight echo, which is added with the echo itself.
     *
     * @return the cache position when an echo rose, else empty
     */
    private static Optional<BlockPos> summonEcho(ServerLevel level, BlockPos pos, Player player, RandomSource random) {
        return Optional.empty();
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
