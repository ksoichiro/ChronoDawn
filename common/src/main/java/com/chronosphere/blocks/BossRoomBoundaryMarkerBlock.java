package com.chronosphere.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Boss Room Boundary Marker Block
 *
 * A special marker block used in structure NBT files to define boss room boundaries.
 * Similar to Jigsaw blocks, this block is visible during structure editing but gets
 * replaced with a specified block during world generation.
 *
 * Features:
 * - Marks min/max corners of boss room bounding box
 * - Stores replacement block in NBT (defaults to air)
 * - Processed by BossRoomProtectionProcessor during structure generation
 * - Full block with collision (easy to place in Structure Block editor)
 *
 * Usage in structure files:
 * - Place at boss room's northwest floor corner: {MarkerType: "boss_room_min", ReplaceWith: "minecraft:stone_bricks"}
 * - Place at boss room's southeast ceiling corner: {MarkerType: "boss_room_max", ReplaceWith: "minecraft:air"}
 *
 * Processing flow:
 * 1. Structure generation starts
 * 2. BossRoomProtectionProcessor detects both markers
 * 3. Calculates BoundingBox from marker positions
 * 4. Registers protection with BlockProtectionHandler
 * 5. Replaces markers with specified blocks
 *
 * Implementation: T224 - Boss room protection with marker blocks
 */
public class BossRoomBoundaryMarkerBlock extends BaseEntityBlock {
    public static final MapCodec<BossRoomBoundaryMarkerBlock> CODEC =
        simpleCodec(BossRoomBoundaryMarkerBlock::new);

    public BossRoomBoundaryMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BossRoomBoundaryMarkerBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
