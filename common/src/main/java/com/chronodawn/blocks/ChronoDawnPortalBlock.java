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
     * Portal timer threshold (ticks) for survival mode.
     * 120 ticks = 6 seconds (longer than Nether Portal's 4 seconds)
     * Creative mode teleports immediately (no waiting time).
     */
    private static final int PORTAL_TIME_THRESHOLD = 120;

    /**
     * Tracks entity portal states and teleportation progress.
     * Map: Entity UUID -> State value
     *
     * State values:
     * -1: Just teleported (transitions to 1 on next tick)
     * 0: Not in portal or re-entered after exit (allows teleportation)
     * 1: In arrival portal, preventing re-teleport (maintained while standing still)
     * 2-119: Counting up to teleportation threshold (survival mode)
     * 120+: Ready to teleport (survival mode)
     * Note: Creative mode players teleport immediately regardless of state value
     */
    private static final Map<UUID, Integer> ENTITY_PORTAL_STATES = new HashMap<>();

    /**
     * Tracks the last portal position each entity was in.
     * Used for detecting when entities exit portals for immediate cleanup.
     * Map: Entity UUID -> Last portal position
     */
    private static final Map<UUID, BlockPos> LAST_PORTAL_POSITION = new HashMap<>();

    /**
     * Tracks the last game tick when entityInside() was called for each entity.
     * Used to detect when entities exit and re-enter portals (tick gap > 20).
     * Map: Entity UUID -> Last tick time
     */
    private static final Map<UUID, Long> LAST_INSIDE_TICK = new HashMap<>();

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

        // Periodic cleanup of stale portal state entries (every ~10 seconds)
        if (random.nextInt(200) == 0) {
            cleanupStalePortalStates(level);
        }
    }

    /**
     * Clean up portal state entries for entities that no longer exist.
     * This is a fallback cleanup for edge cases (e.g., server restart, entity death).
     * Main cleanup happens in entityInside() via tick gap detection.
     *
     * @param level Server level
     */
    private static void cleanupStalePortalStates(ServerLevel level) {
        ENTITY_PORTAL_STATES.entrySet().removeIf(entry -> {
            UUID entityId = entry.getKey();
            Entity entity = findEntityInAllDimensions(level.getServer(), entityId);
            if (entity == null) {
                // Entity no longer exists, clear all records
                LAST_PORTAL_POSITION.remove(entityId);
                LAST_INSIDE_TICK.remove(entityId);
                com.chronodawn.core.portal.PortalTeleportHandler.clearArrivalDimension(entityId);
                return true;
            }
            return false;
        });

        // Also clean up LAST_PORTAL_POSITION and LAST_INSIDE_TICK for entities that no longer exist
        LAST_PORTAL_POSITION.entrySet().removeIf(entry -> {
            Entity entity = findEntityInAllDimensions(level.getServer(), entry.getKey());
            return entity == null;
        });

        LAST_INSIDE_TICK.entrySet().removeIf(entry -> {
            Entity entity = findEntityInAllDimensions(level.getServer(), entry.getKey());
            return entity == null;
        });

        // Clean up arrival dimension records in PortalTeleportHandler (for deleted entities)
        com.chronodawn.core.portal.PortalTeleportHandler.cleanupStaleArrivalRecords(level.getServer());
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

        // Get current game time for re-entry detection
        long currentTick = level.getGameTime();
        BlockPos lastPortalPos = LAST_PORTAL_POSITION.get(entityId);
        Long lastTick = LAST_INSIDE_TICK.get(entityId);

        // CRITICAL: Detect portal exit and re-entry
        boolean exitedAndReentered = false;

        // Case 1: Entity moved to a different portal (distance-based)
        // Use distance > 25 (5+ blocks) to distinguish between:
        // - Same portal movement (player moving within 2x3 portal interior)
        // - Different portal (player teleported or walked to another portal)
        if (lastPortalPos != null && lastPortalPos.distSqr(pos) > 25) {
            // Different portal - clear records and reset state
            com.chronodawn.core.portal.PortalTeleportHandler.clearArrivalDimension(entityId);
            ENTITY_PORTAL_STATES.remove(entityId);
            exitedAndReentered = true;
            ChronoDawn.LOGGER.debug("Entity {} moved to different portal (distance > 5 blocks), cleared arrival record",
                entity.getName().getString());
        }
        // Case 2: Same portal area, but tick gap > 20 (1 second)
        // This means player exited portal and re-entered
        else if (lastPortalPos != null && lastTick != null && currentTick - lastTick > 20) {
            // Exited and re-entered same portal - clear records and reset state
            com.chronodawn.core.portal.PortalTeleportHandler.clearArrivalDimension(entityId);
            ENTITY_PORTAL_STATES.remove(entityId);
            exitedAndReentered = true;
            ChronoDawn.LOGGER.debug("Entity {} exited and re-entered portal (tick gap: {}), cleared arrival record",
                entity.getName().getString(), currentTick - lastTick);
        }

        // Update tracking maps
        LAST_PORTAL_POSITION.put(entityId, pos.immutable());
        LAST_INSIDE_TICK.put(entityId, currentTick);

        // Check if entity can teleport from current dimension
        // This prevents re-teleporting while still in the dimension they just arrived in
        if (!com.chronodawn.core.portal.PortalTeleportHandler.canTeleportFromDimension(entityId, level.dimension())) {
            // Entity is still in the dimension they just arrived in
            int currentState = ENTITY_PORTAL_STATES.getOrDefault(entityId, 0);

            if (currentState == -1) {
                // Just teleported, still in arrival portal
                // Set state to 1 to prevent re-teleport while standing still
                // This transition (-1 → 1) happens on the first tick after teleportation
                ENTITY_PORTAL_STATES.put(entityId, 1);
                ChronoDawn.LOGGER.debug("Prevented re-teleport for {} - just teleported, still in arrival dimension",
                    entity.getName().getString());
                return;
            } else if (currentState == 0) {
                // CRITICAL: State is 0, meaning either:
                // (a) First time entering this portal, OR
                // (b) Exited and re-entered quickly (tick gap < 20, not detected by tick gap check above)
                //
                // This is a fallback for rapid re-entry (< 1 second)
                // Clear the arrival record now to allow teleportation
                com.chronodawn.core.portal.PortalTeleportHandler.clearArrivalDimension(entityId);
                ChronoDawn.LOGGER.debug("Cleared arrival dimension for {} - rapid re-entry",
                    entity.getName().getString());
                // Don't return - allow state to start counting and teleportation to proceed
            } else {
                // State >= 1 = still in arrival portal, prevent re-teleport
                // Keep state at 1 to maintain "still in portal" state
                ENTITY_PORTAL_STATES.put(entityId, 1);
                ChronoDawn.LOGGER.debug("Prevented re-teleport for {} - still in arrival dimension {}",
                    entity.getName().getString(), level.dimension().location());
                return;
            }
        }

        // Increment portal state counter for this entity
        int stateValue = ENTITY_PORTAL_STATES.getOrDefault(entityId, 0);
        stateValue++;
        ENTITY_PORTAL_STATES.put(entityId, stateValue);

        // Play portal trigger sound when countdown starts (state 0 → 1)
        // Same sound as Nether Portal
        if (stateValue == 1 && entity instanceof net.minecraft.server.level.ServerPlayer) {
            level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        // Check if entity should teleport
        // Creative mode: teleport immediately (like vanilla Nether Portal)
        // Survival mode: wait for threshold time
        boolean shouldTeleport = false;
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            if (player.getAbilities().invulnerable) {
                // Creative mode: teleport immediately
                shouldTeleport = true;
            } else {
                // Survival mode: wait for threshold
                shouldTeleport = stateValue >= PORTAL_TIME_THRESHOLD;
            }
        } else {
            // Non-player entities: use threshold
            shouldTeleport = stateValue >= PORTAL_TIME_THRESHOLD;
        }

        // Only teleport after entity has been inside portal for threshold time
        if (shouldTeleport) {
            // Attempt teleportation
            boolean success = com.chronodawn.core.portal.PortalTeleportHandler.teleportThroughPortal(entity, pos);

            if (success) {
                // Set state to -1 to indicate "just teleported"
                // This flag prevents immediate re-evaluation on the next tick
                // and allows the state machine to transition: -1 → 1 (arrival portal)
                ENTITY_PORTAL_STATES.put(entityId, -1);

                ChronoDawn.LOGGER.info("Entity {} teleported through portal at {} in dimension {} after {} ticks",
                    entity.getName().getString(), pos, level.dimension().location(), stateValue);
            } else {
                // Teleportation failed, reset state
                ENTITY_PORTAL_STATES.remove(entityId);
            }
        }
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

        // Use custom ChronoDawn Portal particles
        // Golden/orange particles that float gently like Nether Portal
        level.addParticle(
            com.chronodawn.registry.ModParticles.CHRONO_DAWN_PORTAL.get(),
            x, y, z,
            speedX, speedY, speedZ
        );
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
