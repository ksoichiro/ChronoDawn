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

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.WaysideSettings;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import com.chronodawn.core.wayside.SundialBlessing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The post of an Old Sundial. Right-clicking it grants a time-of-day blessing,
 * once per cooldown for each player.
 */
public class SundialGnomonBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
    // The gnomon stands on a center block, so the dial face is one block below its base.
    private static final double DIAL_FACE_OFFSET_Y = -0.9;

    public SundialGnomonBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SundialGnomonBlockEntity(pos, state);
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
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof SundialGnomonBlockEntity gnomon) {
            activate(serverLevel, pos, player, gnomon);
        }
        return InteractionResult.SUCCESS;
    }

    private static void activate(ServerLevel level, BlockPos pos, Player player, SundialGnomonBlockEntity gnomon) {
        WaysideSettings settings = ChronoDawnConfig.get().gameplay().wayside();
        long remaining = gnomon.tryActivate(player.getUUID(), level.getGameTime(), settings.sundialCooldownTicks());
        if (remaining > 0) {
            player.displayClientMessage(
                Component.translatable("message.chronodawn.sundial_gnomon.cooldown",
                    PlayerCooldownTracker.toMinutesRoundedUp(remaining)),
                true
            );
            return;
        }

        long dayTime = level.getDayTime();
        SundialBlessing blessing = SundialBlessing.forDayTime(dayTime);
        player.addEffect(new MobEffectInstance(
            blessing.effect() == SundialBlessing.Effect.SPEED ? MobEffects.MOVEMENT_SPEED : MobEffects.NIGHT_VISION,
            settings.sundialEffectDurationTicks(),
            blessing.amplifier()
        ));
        spawnParticles(level, pos, blessing, dayTime);
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, blessing.soundVolume(), blessing.soundPitch());
        player.displayClientMessage(Component.translatable(blessing.messageKey()), true);
    }

    private static void spawnParticles(ServerLevel level, BlockPos pos, SundialBlessing blessing, long dayTime) {
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        if (blessing == SundialBlessing.NIGHT) {
            SimpleParticleType moonlight = ParticleTypes.GLOW;
            level.sendParticles(moonlight, centerX, pos.getY() + 0.8, centerZ,
                blessing.particleCount(), 0.3, 0.4, 0.3, 0.01);
            return;
        }
        double faceY = pos.getY() + DIAL_FACE_OFFSET_Y;
        for (double[] offset : SundialBlessing.shadowParticleOffsets(dayTime, blessing.particleCount())) {
            level.sendParticles(ParticleTypes.END_ROD,
                centerX + offset[0], faceY, centerZ + offset[1],
                1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
