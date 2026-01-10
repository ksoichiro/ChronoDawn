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
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * ChronoDawn Portal Block - Custom portal block for dimension travel.
 *
 * This block forms the interior of ChronoDawn portals and handles teleportation
 * when entities collide with it.
 *
 * Portal Properties:
 * - Color: Orange (#db8813 / RGB 219, 136, 19) - represents time/temporal theme
 * - Light Level: 11 (similar to Nether Portal)
 * - Collision: None (entities pass through)
 * - Axis: X or Z (determines portal orientation)
 *
 * Portal Behavior:
 * - Created by Time Hourglass on valid Clockstone Block frame
 * - Teleports entities on collision (handled by PortalTeleportHandler in Phase 3)
 * - Destroyed if frame is broken (randomTick validation)
 * - State managed by PortalRegistry and PortalStateMachine
 *
 * Implementation:
 * - Phase 1: Block creation, rendering, particles (Current)
 * - Phase 2: Portal ignition (Time Hourglass)
 * - Phase 3: Teleportation logic (entityInside implementation)
 *
 * Reference: docs/portal_implementation_plan.md
 * Based on: vanilla NetherPortalBlock structure
 */
public class ChronoDawnPortalBlock extends Block {
    /**
     * Portal axis property (X or Z orientation).
     */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    /**
     * Portal block shape (full block, but no collision).
     */
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    /**
     * Portal color - Orange (#db8813 / RGB 219, 136, 19).
     * Represents time/temporal energy (clock hands, brass gears).
     */
    private static final int PORTAL_COLOR_R = 219;
    private static final int PORTAL_COLOR_G = 136;
    private static final int PORTAL_COLOR_B = 19;

    public ChronoDawnPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    /**
     * Create default properties for ChronoDawn Portal Block.
     *
     * @return Block properties with portal-specific settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .noCollission()      // Entities pass through
                // .randomTicks()       // TODO Phase 2: Enable after portal ignition implementation
                .strength(-1.0F)     // Unbreakable by normal means
                .lightLevel((state) -> 11)  // Orange glow (similar to Nether Portal)
                .noLootTable();      // Don't drop items when destroyed
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Return thin shape based on axis orientation
        return switch (state.getValue(AXIS)) {
            case Z -> Z_AXIS_AABB;
            default -> X_AXIS_AABB;
        };
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Validate portal frame integrity
        // If frame is broken, destroy portal blocks
        if (!isValidPortalPosition(level, pos, state.getValue(AXIS))) {
            level.removeBlock(pos, false);
            ChronoDawn.LOGGER.debug("Destroyed portal block at {} due to broken frame", pos);
        }
    }

    /**
     * Check if this portal block has a valid frame.
     *
     * @param level The level
     * @param pos Portal block position
     * @param axis Portal axis
     * @return true if portal frame is valid
     */
    private boolean isValidPortalPosition(Level level, BlockPos pos, Direction.Axis axis) {
        // Check if adjacent blocks are Clockstone Block or other portal blocks
        Direction.Axis otherAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        Direction direction1 = Direction.get(Direction.AxisDirection.NEGATIVE, otherAxis);
        Direction direction2 = Direction.get(Direction.AxisDirection.POSITIVE, otherAxis);

        BlockPos adjacentPos1 = pos.relative(direction1);
        BlockPos adjacentPos2 = pos.relative(direction2);

        BlockState state1 = level.getBlockState(adjacentPos1);
        BlockState state2 = level.getBlockState(adjacentPos2);

        // Valid if adjacent blocks are frame blocks or portal blocks
        boolean valid1 = isFrameBlock(state1) || (state1.is(this) && state1.getValue(AXIS) == axis);
        boolean valid2 = isFrameBlock(state2) || (state2.is(this) && state2.getValue(AXIS) == axis);

        return valid1 || valid2;
    }

    /**
     * Check if a block is a valid portal frame block.
     *
     * @param state Block state to check
     * @return true if block is Clockstone Block
     */
    private boolean isFrameBlock(BlockState state) {
        return state.is(ModBlocks.CLOCKSTONE_BLOCK.get());
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Phase 3: Implement teleportation logic
        // TODO: Handle entity collision and trigger teleportation
        // - Check teleport cooldown
        // - Look up portal in PortalRegistry
        // - Call PortalTeleportHandler
        // - Update portal state (ACTIVATED → DEACTIVATED)

        ChronoDawn.LOGGER.debug("Entity {} entered portal at {}", entity.getName().getString(), pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Play ambient sound more frequently (every ~2 seconds instead of ~5 seconds)
        if (random.nextInt(40) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5F,
                    random.nextFloat() * 0.4F + 0.8F,
                    false
            );
        }

        // Spawn orange/golden particles (Nether Portal style)
        // Based on vanilla NetherPortalBlock particle logic
        Direction.Axis axis = state.getValue(AXIS);

        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double speedX = (random.nextDouble() - 0.5) * 0.5;
            double speedY = (random.nextDouble() - 0.5) * 0.5;
            double speedZ = (random.nextDouble() - 0.5) * 0.5;

            int j = random.nextInt(2) * 2 - 1;

            // Adjust particle spawn position based on portal axis
            if (axis == Direction.Axis.X) {
                // X-axis portal (particles flow along Z)
                z = pos.getZ() + 0.5 + 0.25 * j;
                speedZ = random.nextFloat() * 2.0F * j;
            } else {
                // Z-axis portal (particles flow along X)
                x = pos.getX() + 0.5 + 0.25 * j;
                speedX = random.nextFloat() * 2.0F * j;
            }

            // Use lava particles for better orange effect
            // LAVA particles are orange/red and don't rise like FLAME
            level.addParticle(ParticleTypes.LAVA, x, y, z, speedX, speedY, speedZ);

            // Add some flame particles for glow effect
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.FLAME, x, y, z, speedX * 0.5, speedY * 0.5, speedZ * 0.5);
            }
        }
    }

    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        // Don't allow picking up portal blocks
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        // Rotate portal axis when structure rotates
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                    LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        // Validate frame when neighbor changes
        Direction.Axis axis = state.getValue(AXIS);
        Direction.Axis directionAxis = direction.getAxis();
        Direction.Axis otherAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        boolean isRelevantDirection = directionAxis != axis && directionAxis != Direction.Axis.Y;

        // If relevant neighbor changed and frame is invalid, destroy portal block
        // Note: Cannot fully validate frame in updateShape as we only have LevelAccessor
        // Full validation is done in randomTick() with ServerLevel
        if (isRelevantDirection && !isFrameBlock(neighborState) && !neighborState.is(this)) {
            // If neighbor is not a frame block or portal block, this portal block may be invalid
            // Will be validated and removed in randomTick if frame is actually broken
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}
