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
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.core.wayside.CampChestOutcome;
import com.chronodawn.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

/**
 * The chest of a Watchmaker's Camp. Each player opens their own 27 slots, which are filled from
 * the camp table on their first open and again once they are empty and the cooldown has passed.
 * Players can take items out but never put any in.
 *
 * <p>An {@link AbstractChestBlock} with {@link #FACING}, so the vanilla chest renderer draws it
 * as a plain single chest.
 */
public class WatchmakerChestBlock extends AbstractChestBlock<WatchmakerChestBlockEntity> {
    public static final MapCodec<WatchmakerChestBlock> CODEC = simpleCodec(WatchmakerChestBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final String LOOT_TABLE = "chests/watchmaker_camp";
    private static final String CONTAINER_TITLE = "block.chronodawn.watchmaker_chest";
    // The vanilla single chest shape.
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    private static final int FILL_PARTICLES = 12;
    private static final double FILL_PARTICLE_HEIGHT = 1.0;
    private static final double FILL_PARTICLE_SPREAD = 0.3;
    private static final double FILL_PARTICLE_SPEED = 0.05;
    private static final float FILL_SOUND_VOLUME = 1.0f;
    private static final float FILL_SOUND_PITCH = 0.8f;

    public WatchmakerChestBlock(Properties properties) {
        super(properties, () -> ModBlockEntities.WATCHMAKER_CHEST.get());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * @return the vanilla chest's properties
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.5f)
            .sound(SoundType.WOOD)
            .ignitedByLava()
            .setId(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "watchmaker_chest")));
    }

    @Override
    protected MapCodec<? extends AbstractChestBlock<WatchmakerChestBlockEntity>> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WatchmakerChestBlockEntity(pos, state);
    }

    /**
     * A single chest never combines with a neighbour, so the renderer draws it on its own.
     */
    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(
            BlockState state, Level level, BlockPos pos, boolean ignoreBlocked) {
        return DoubleBlockCombiner.Combiner::acceptNone;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // As the vanilla chest: drawn by the block entity renderer, the model gives particles only.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Only the lid moves, and only on the client.
        return level.isClientSide
            ? createTickerHelper(type, ModBlockEntities.WATCHMAKER_CHEST.get(), WatchmakerChestBlockEntity::lidAnimateTick)
            : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Unlike the vanilla chest a block above does not stop it opening: a blocked camp chest
        // would only be a lost camp.
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            use(serverLevel, pos, serverPlayer, level.getGameTime(),
                ChronoDawnConfig.get().gameplay().wayside().watchmakerCampCooldownTicks());
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Opens the chest for a player. Slots that still hold items open unchanged. Empty slots are
     * filled and opened when the cooldown allows, otherwise the player is told to wait. A
     * spectator, who only gets here through a direct call, sees their slots unchanged.
     */
    public static CampChestOutcome use(ServerLevel level, BlockPos pos, ServerPlayer player, long gameTime,
                                       long cooldownTicks) {
        if (!(level.getBlockEntity(pos) instanceof WatchmakerChestBlockEntity chest)) {
            return CampChestOutcome.WAITING;
        }
        UUID id = player.getUUID();
        // Vanilla never routes a spectator's click here: it asks getMenuProvider, which this block
        // does not provide, so spectators cannot open the chest. This guard defends direct callers,
        // so a spectator never triggers a fill or uses the cooldown. ServerPlayer#isSpectator reads
        // this same game mode. It is read from the game mode manager because the GameTest mock
        // player overrides isSpectator or gameMode() with a fixed value.
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || !chest.isEmptyFor(id)) {
            openMenu(player, chest);
            return CampChestOutcome.OPENED;
        }
        // Only an empty chest asks the cooldown, so a leftover item never uses it up.
        if (chest.tryActivate(id, gameTime, cooldownTicks) > 0) {
            player.displayClientMessage(Component.translatable(CampChestOutcome.WAITING.messageKey()), true);
            return CampChestOutcome.WAITING;
        }
        fill(level, pos, player, chest.slotsFor(id));
        playFillEffects(level, pos, player);
        openMenu(player, chest);
        return CampChestOutcome.FILLED;
    }

    private static void fill(ServerLevel level, BlockPos pos, ServerPlayer player, Container slots) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(
            ResourceKey.create(Registries.LOOT_TABLE, CompatResourceLocation.create(ChronoDawn.MOD_ID, LOOT_TABLE)));
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.THIS_ENTITY, player)
            .withLuck(player.getLuck())
            .create(LootContextParamSets.CHEST);
        table.fill(slots, params, level.getRandom().nextLong());
    }

    /**
     * Plays the sound of the camp's time turning back, to the opening player only, so nearby
     * players do not hear a fill they did not get.
     */
    private static void playFillEffects(ServerLevel level, BlockPos pos, ServerPlayer player) {
        player.playNotifySound(SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.BLOCKS,
            FILL_SOUND_VOLUME, FILL_SOUND_PITCH);
        level.sendParticles(player, ParticleTypes.REVERSE_PORTAL, false,
            pos.getX() + 0.5, pos.getY() + FILL_PARTICLE_HEIGHT, pos.getZ() + 0.5, FILL_PARTICLES,
            FILL_PARTICLE_SPREAD, FILL_PARTICLE_SPREAD, FILL_PARTICLE_SPREAD, FILL_PARTICLE_SPEED);
    }

    private static void openMenu(ServerPlayer player, WatchmakerChestBlockEntity chest) {
        UUID id = player.getUUID();
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, menuPlayer) ->
                new WatchmakerChestMenu(containerId, inventory, chest.slotsFor(id), chest),
            Component.translatable(CONTAINER_TITLE)
        )).ifPresent(containerId -> chest.startOpen(player));
    }

    /**
     * Drops the breaking player's own slots, in any game mode. The plain chest comes from the
     * loot table, and other players' slots are discarded: they only ever hold rolls nobody took.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof WatchmakerChestBlockEntity chest
                && !chest.isEmptyFor(player.getUUID())) {
            Container slots = chest.slotsFor(player.getUUID());
            Containers.dropContents(level, pos, slots);
            // Cleared so the items cannot drop twice if the block survives the break after all.
            slots.clearContent();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
