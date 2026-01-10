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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
     * Portal timer threshold (ticks) - same as Nether Portal.
     * 80 ticks = 4 seconds
     */
    private static final int PORTAL_TIME_THRESHOLD = 80;

    /**
     * Tracks how long entities have been inside portals.
     * Map: Entity UUID -> Portal time (ticks)
     */
    private static final Map<UUID, Integer> PORTAL_TIMERS = new HashMap<>();

    /**
     * Tracks the last portal position each entity teleported from.
     * Used to prevent re-teleporting from the same portal until entity exits.
     * Map: Entity UUID -> Last portal block position
     */
    private static final Map<UUID, BlockPos> LAST_TELEPORT_PORTAL = new HashMap<>();

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
                .randomTicks()       // Validate portal frame integrity
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

        // Periodic cleanup of stale portal timer entries (every ~10 seconds)
        if (random.nextInt(200) == 0) {
            cleanupStalePortalTimers(level);
        }
    }

    /**
     * Clean up portal timer entries for entities that no longer exist or are far from portals.
     *
     * @param level Server level
     */
    private static void cleanupStalePortalTimers(ServerLevel level) {
        PORTAL_TIMERS.entrySet().removeIf(entry -> {
            UUID entityId = entry.getKey();
            Entity entity = findEntityInAllDimensions(level.getServer(), entityId);
            // Remove if entity doesn't exist anymore
            return entity == null;
        });

        // Clean up LAST_TELEPORT_PORTAL for entities no longer in portals
        LAST_TELEPORT_PORTAL.entrySet().removeIf(entry -> {
            UUID entityId = entry.getKey();
            BlockPos lastPortalPos = entry.getValue();
            Entity entity = findEntityInAllDimensions(level.getServer(), entityId);

            if (entity == null) {
                // Entity doesn't exist anymore
                return true;
            }

            // Check if entity is still inside a portal block
            BlockPos entityPos = entity.blockPosition();
            BlockState stateAtEntity = entity.level().getBlockState(entityPos);

            // If entity is no longer in a portal, or is in a different portal, clear the record
            if (!stateAtEntity.is(ModBlocks.CHRONO_DAWN_PORTAL.get()) ||
                entityPos.distSqr(lastPortalPos) > 25) {
                return true;
            }

            return false;
        });
    }

    /**
     * Find an entity across all dimensions.
     *
     * @param server Minecraft server
     * @param entityId Entity UUID
     * @return Entity if found, null otherwise
     */
    private static Entity findEntityInAllDimensions(net.minecraft.server.MinecraftServer server, UUID entityId) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Check if this portal block has a valid frame.
     *
     * A portal block is valid if it has at least one frame block or portal block
     * in any of the 4 cardinal directions (up, down, left, right relative to portal plane).
     *
     * @param level The level
     * @param pos Portal block position
     * @param axis Portal axis
     * @return true if portal frame is valid
     */
    private boolean isValidPortalPosition(Level level, BlockPos pos, Direction.Axis axis) {
        // Check horizontal adjacent blocks (perpendicular to portal axis)
        Direction.Axis otherAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        Direction horizontalNeg = Direction.get(Direction.AxisDirection.NEGATIVE, otherAxis);
        Direction horizontalPos = Direction.get(Direction.AxisDirection.POSITIVE, otherAxis);

        // Check vertical adjacent blocks
        Direction up = Direction.UP;
        Direction down = Direction.DOWN;

        // Check all 4 cardinal directions around the portal block
        BlockState stateHorizontalNeg = level.getBlockState(pos.relative(horizontalNeg));
        BlockState stateHorizontalPos = level.getBlockState(pos.relative(horizontalPos));
        BlockState stateUp = level.getBlockState(pos.relative(up));
        BlockState stateDown = level.getBlockState(pos.relative(down));

        // Valid if any adjacent block is a frame block or portal block
        boolean validHorizontalNeg = isFrameBlock(stateHorizontalNeg) ||
                                     (stateHorizontalNeg.is(this) && stateHorizontalNeg.getValue(AXIS) == axis);
        boolean validHorizontalPos = isFrameBlock(stateHorizontalPos) ||
                                     (stateHorizontalPos.is(this) && stateHorizontalPos.getValue(AXIS) == axis);
        boolean validUp = isFrameBlock(stateUp) ||
                         (stateUp.is(this) && stateUp.getValue(AXIS) == axis);
        boolean validDown = isFrameBlock(stateDown) ||
                           (stateDown.is(this) && stateDown.getValue(AXIS) == axis);

        // Portal is valid if at least one adjacent block is a frame or portal block
        return validHorizontalNeg || validHorizontalPos || validUp || validDown;
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
        // Only process on server side
        if (level.isClientSide()) {
            return;
        }

        // Check if entity can use portals
        if (entity.isPassenger() || entity.isVehicle()) {
            return;
        }

        UUID entityId = entity.getUUID();

        // Check if entity recently teleported from this same portal
        // Prevent re-teleport until entity exits the portal
        BlockPos lastPortalPos = LAST_TELEPORT_PORTAL.get(entityId);
        if (lastPortalPos != null && isSamePortal(lastPortalPos, pos, level)) {
            // Entity is still in the same portal they teleported from
            // Reset timer to prevent re-teleport
            PORTAL_TIMERS.remove(entityId);
            return;
        }

        // Check portal cooldown (prevents instant re-teleport)
        if (entity.getPortalCooldown() > 0) {
            // Reset portal timer if on cooldown
            PORTAL_TIMERS.remove(entityId);
            return;
        }

        // Increment portal timer for this entity
        int portalTime = PORTAL_TIMERS.getOrDefault(entityId, 0);
        portalTime++;
        PORTAL_TIMERS.put(entityId, portalTime);

        // Only teleport after entity has been inside portal for threshold time
        if (portalTime >= PORTAL_TIME_THRESHOLD) {
            // Attempt teleportation
            boolean success = com.chronodawn.core.portal.PortalTeleportHandler.teleportThroughPortal(entity, pos);

            if (success) {
                // Reset portal timer
                PORTAL_TIMERS.remove(entityId);

                // Record this portal to prevent immediate re-entry
                LAST_TELEPORT_PORTAL.put(entityId, pos.immutable());

                // Set portal cooldown (300 ticks = 15 seconds)
                entity.setPortalCooldown(300);

                ChronoDawn.LOGGER.info("Entity {} teleported through portal at {} after {} ticks",
                    entity.getName().getString(), pos, portalTime);
            } else {
                // Teleportation failed, reset timer
                PORTAL_TIMERS.remove(entityId);
            }
        }
    }

    /**
     * Check if two portal block positions belong to the same portal.
     * Positions are considered part of the same portal if they're within 5 blocks of each other.
     *
     * @param pos1 First portal block position
     * @param pos2 Second portal block position
     * @param level Level to check in
     * @return true if positions are part of the same portal
     */
    private boolean isSamePortal(BlockPos pos1, BlockPos pos2, Level level) {
        // Check if positions are close enough to be the same portal (within 5 blocks)
        return pos1.distSqr(pos2) <= 25; // 5^2 = 25
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
        // Gentle, floating particles that slowly drift away from portal surface
        Direction.Axis axis = state.getValue(AXIS);

        // Reduce particle spawn frequency - only spawn 1 particle per tick
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();

        // Very gentle particle speed - subtle floating motion
        double speedX = (random.nextDouble() - 0.5) * 0.1;
        double speedY = (random.nextDouble() - 0.5) * 0.1;
        double speedZ = (random.nextDouble() - 0.5) * 0.1;

        int j = random.nextInt(2) * 2 - 1;

        // Adjust particle spawn position based on portal axis
        // Spawn particles slightly away from portal surface
        if (axis == Direction.Axis.X) {
            // X-axis portal (particles drift gently along Z)
            z = pos.getZ() + 0.5 + 0.25 * j;
            speedZ = random.nextFloat() * 0.3F * j; // Reduced from 2.0F to 0.3F
        } else {
            // Z-axis portal (particles drift gently along X)
            x = pos.getX() + 0.5 + 0.25 * j;
            speedX = random.nextFloat() * 0.3F * j; // Reduced from 2.0F to 0.3F
        }

        // Use lava particles for better orange effect
        // LAVA particles are orange/red and don't rise like FLAME
        level.addParticle(ParticleTypes.LAVA, x, y, z, speedX, speedY, speedZ);

        // Occasionally add flame particles for glow effect (less frequent)
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.FLAME, x, y, z, speedX * 0.5, speedY * 0.5, speedZ * 0.5);
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

        // Check if the changed neighbor is relevant (perpendicular to portal axis or vertical)
        boolean isRelevantDirection = directionAxis != axis;

        // If relevant neighbor changed and is neither a frame block nor a portal block,
        // destroy this portal block immediately
        if (isRelevantDirection && !isFrameBlock(neighborState) && !neighborState.is(this)) {
            // Frame is broken, destroy portal block
            ChronoDawn.LOGGER.debug("Destroyed portal block at {} due to broken frame (neighbor at {} changed to {})",
                currentPos, neighborPos, neighborState.getBlock().getName().getString());
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}
