package com.chronosphere.blocks;

import com.chronosphere.core.teleport.TeleporterChargingHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Clock Tower Teleporter Block
 *
 * A time-themed teleporter that requires 3 seconds of charging (holding right-click)
 * before teleporting the player between floors of the Desert Clock Tower.
 *
 * Features:
 * - Long-press charging (3 seconds)
 * - Progressive particle and sound effects
 * - UP direction: 4th floor → 5th floor (boss room)
 * - DOWN direction: 5th floor → 4th floor (spawns after defeating Time Guardian)
 * - BlockEntity stores target position for safe teleportation
 */
public class ClockTowerTeleporterBlock extends BaseEntityBlock {
    public static final MapCodec<ClockTowerTeleporterBlock> CODEC = simpleCodec(ClockTowerTeleporterBlock::new);
    public static final EnumProperty<TeleportDirection> DIRECTION = EnumProperty.create("direction", TeleportDirection.class);

    // Floor height in blocks (adjust based on actual structure)
    private static final int FLOOR_HEIGHT = 8;

    public ClockTowerTeleporterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DIRECTION, TeleportDirection.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClockTowerTeleporterBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Server-side: Start charging the teleporter
            // This will be handled by TeleporterChargingHandler.tick()
            // Initial sound and particles are played by TeleporterChargingHandler.startCharging()
            TeleporterChargingHandler.startCharging(
                serverPlayer,
                pos,
                state.getValue(DIRECTION).name(),
                level.getGameTime()
            );
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Teleport direction enum
     */
    public enum TeleportDirection implements net.minecraft.util.StringRepresentable {
        UP("up"),
        DOWN("down");

        private final String name;

        TeleportDirection(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
