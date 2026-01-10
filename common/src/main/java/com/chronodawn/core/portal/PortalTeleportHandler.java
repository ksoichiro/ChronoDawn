package com.chronodawn.core.portal;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.items.TimeHourglassItem;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Portal Teleport Handler - Handles player teleportation through ChronoDawn portals.
 *
 * This class manages the teleportation process:
 * - Determines destination dimension (Overworld ↔ ChronoDawn)
 * - Calculates destination coordinates (1:1 mapping, no scaling)
 * - Searches for existing portals at destination
 * - Generates new portals if none exist (Y=70-100 range)
 * - Updates portal states after teleportation
 *
 * Teleportation Flow:
 * 1. Player enters portal (collision with portal block)
 * 2. Calculate destination dimension and coordinates
 * 3. Search for existing portal at destination
 * 4. If no portal found, generate new portal (Y=70-100)
 * 5. Teleport player to destination
 * 6. Update portal state (ACTIVATED → DEACTIVATED)
 * 7. Mark ChronoDawn entered (if entering ChronoDawn for first time)
 *
 * Reference: docs/portal_implementation_plan.md (Phase 3)
 */
public class PortalTeleportHandler {
    /**
     * Tracks the last dimension each entity arrived at after teleportation.
     * Used to prevent re-teleporting to the same dimension until entity changes dimensions.
     * Map: Entity UUID -> Last arrival dimension
     */
    private static final Map<UUID, ResourceKey<Level>> LAST_ARRIVAL_DIMENSION = new HashMap<>();

    /**
     * Portal search radius (blocks) around destination coordinates.
     */
    private static final int PORTAL_SEARCH_RADIUS = 16;

    /**
     * Minimum Y level for portal generation.
     */
    private static final int MIN_PORTAL_Y = 70;

    /**
     * Maximum Y level for portal generation.
     */
    private static final int MAX_PORTAL_Y = 100;

    /**
     * Check if entity can teleport from the current dimension.
     * Prevents re-teleportation while still in the dimension they just arrived in.
     *
     * @param entityId Entity UUID
     * @param currentDimension Current dimension the entity is in
     * @return true if teleportation is allowed
     */
    public static boolean canTeleportFromDimension(UUID entityId, ResourceKey<Level> currentDimension) {
        ResourceKey<Level> lastArrivalDim = LAST_ARRIVAL_DIMENSION.get(entityId);
        if (lastArrivalDim == null) {
            // No previous teleport record, allow teleportation
            return true;
        }

        // Allow teleportation only if current dimension is different from last arrival dimension
        // This prevents re-teleporting while still in the dimension you just arrived in
        return !lastArrivalDim.equals(currentDimension);
    }

    /**
     * Record the dimension the entity just arrived at.
     *
     * @param entityId Entity UUID
     * @param arrivalDimension Dimension the entity arrived at
     */
    private static void recordArrivalDimension(UUID entityId, ResourceKey<Level> arrivalDimension) {
        LAST_ARRIVAL_DIMENSION.put(entityId, arrivalDimension);
        ChronoDawn.LOGGER.debug("Recorded arrival dimension {} for entity {}",
            arrivalDimension.location(), entityId);
    }

    /**
     * Clear the arrival dimension record for an entity.
     * Called when entity exits a portal.
     *
     * @param entityId Entity UUID
     */
    public static void clearArrivalDimension(UUID entityId) {
        LAST_ARRIVAL_DIMENSION.remove(entityId);
    }

    /**
     * Clean up stale arrival dimension records.
     * Removes records for entities that no longer exist or are not in portals.
     *
     * @param server Minecraft server
     */
    public static void cleanupStaleArrivalRecords(net.minecraft.server.MinecraftServer server) {
        LAST_ARRIVAL_DIMENSION.entrySet().removeIf(entry -> {
            UUID entityId = entry.getKey();

            // Find entity across all dimensions
            Entity entity = null;
            for (ServerLevel level : server.getAllLevels()) {
                entity = level.getEntity(entityId);
                if (entity != null) break;
            }

            if (entity == null) {
                // Entity doesn't exist anymore
                ChronoDawn.LOGGER.debug("Cleared arrival dimension for non-existent entity {}", entityId);
                return true;
            }

            // Check if entity is still in a portal
            BlockPos entityPos = entity.blockPosition();
            BlockState stateAtEntity = entity.level().getBlockState(entityPos);

            if (!stateAtEntity.is(ModBlocks.CHRONO_DAWN_PORTAL.get())) {
                // Entity is no longer in a portal, clear the record
                ChronoDawn.LOGGER.debug("Cleared arrival dimension for entity {} - not in portal",
                    entity.getName().getString());
                return true;
            }

            return false;
        });
    }

    /**
     * Teleport an entity through a ChronoDawn portal.
     *
     * @param entity Entity to teleport
     * @param sourcePortalPos Source portal position
     * @return true if teleportation succeeded
     */
    public static boolean teleportThroughPortal(Entity entity, BlockPos sourcePortalPos) {
        if (!(entity instanceof ServerPlayer player)) {
            // Only players can teleport for now
            return false;
        }

        ServerLevel sourceLevel = (ServerLevel) player.level();
        MinecraftServer server = sourceLevel.getServer();

        // Read source portal axis for consistent orientation
        BlockState sourcePortalState = sourceLevel.getBlockState(sourcePortalPos);
        Direction.Axis sourceAxis = Direction.Axis.X; // Default axis
        if (sourcePortalState.is(ModBlocks.CHRONO_DAWN_PORTAL.get())) {
            sourceAxis = sourcePortalState.getValue(com.chronodawn.blocks.ChronoDawnPortalBlock.AXIS);
        }

        // Find source portal frame position from PortalRegistry
        // This ensures we use consistent frame bottom-left coordinates for 1:1 mapping
        BlockPos sourceFramePos = findSourcePortalFrame(sourceLevel, sourcePortalPos);
        if (sourceFramePos == null) {
            ChronoDawn.LOGGER.warn("Could not find portal frame for portal block at {}, using block position as fallback",
                sourcePortalPos);
            sourceFramePos = sourcePortalPos;
        }

        // Determine destination dimension
        ResourceKey<Level> destDimensionKey = getDestinationDimension(sourceLevel.dimension());
        ServerLevel destLevel = server.getLevel(destDimensionKey);

        if (destLevel == null) {
            ChronoDawn.LOGGER.error("Destination dimension {} not found", destDimensionKey.location());
            return false;
        }

        // Calculate destination coordinates (1:1 mapping using frame position)
        BlockPos destCoords = calculateDestinationCoords(sourceFramePos);

        // Search for existing portal at destination
        Optional<BlockPos> existingPortal = findNearbyPortal(destLevel, destCoords);

        BlockPos destPortalPos;
        if (existingPortal.isPresent()) {
            // Use existing portal (this is a portal block position)
            destPortalPos = existingPortal.get();
            ChronoDawn.LOGGER.info("Found existing portal at {} in dimension {}",
                destPortalPos, destDimensionKey.location());
        } else {
            // Generate new portal with same axis as source portal
            BlockPos framePos = generatePortal(destLevel, destCoords, sourceAxis);
            if (framePos == null) {
                ChronoDawn.LOGGER.error("Failed to generate portal at {} in dimension {}",
                    destCoords, destDimensionKey.location());
                return false;
            }
            ChronoDawn.LOGGER.info("Generated new portal at {} in dimension {} with axis {}",
                framePos, destDimensionKey.location(), sourceAxis);

            // Calculate portal interior position from frame bottom-left
            // Offset depends on axis: X axis uses (1, 1, 0), Z axis uses (0, 1, 1)
            destPortalPos = sourceAxis == Direction.Axis.X
                ? framePos.offset(1, 1, 0)
                : framePos.offset(0, 1, 1);
        }

        // Play portal travel sound (same as Nether portal)
        sourceLevel.playSound(
            null,
            sourcePortalPos,
            SoundEvents.PORTAL_TRAVEL,
            SoundSource.BLOCKS,
            0.25F,
            sourceLevel.getRandom().nextFloat() * 0.4F + 0.8F
        );

        // Calculate safe teleport position
        // Portal interior is 2x3 (width 2, height 3), place player at center
        Vec3 destPosition = calculateSafeTeleportPosition(destPortalPos, sourceAxis);

        // Calculate player rotation (face away from portal)
        float destYRot = calculateTeleportRotation(sourceAxis);

        player.teleportTo(destLevel, destPosition.x, destPosition.y, destPosition.z, destYRot, player.getXRot());

        // Update portal state (ACTIVATED → DEACTIVATED)
        PortalStateMachine sourcePortal = PortalRegistry.getInstance().getPortalAt(sourcePortalPos);
        if (sourcePortal != null && sourcePortal.getCurrentState() == PortalState.ACTIVATED) {
            sourcePortal.deactivate();
            ChronoDawn.LOGGER.info("Deactivated source portal at {}", sourcePortalPos);
        }

        // Mark ChronoDawn entered (if entering ChronoDawn for first time)
        boolean portalWasDestroyed = false;
        if (destDimensionKey.equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            ChronoDawnGlobalState globalState = ChronoDawnGlobalState.get(server);
            globalState.markChronoDawnEntered();

            // Destroy unstable portal (if portals are unstable)
            if (globalState.arePortalsUnstable()) {
                destroyUnstablePortal(destLevel, destPortalPos);
                ChronoDawn.LOGGER.info("Destroyed unstable portal at {} in ChronoDawn dimension", destPortalPos);
                portalWasDestroyed = true;
            }
        }

        // Record arrival dimension to prevent immediate re-teleportation to same dimension
        // CRITICAL: Only record if portal was NOT destroyed
        // If portal is destroyed, player is no longer "in portal" so no need to prevent re-entry
        if (!portalWasDestroyed) {
            recordArrivalDimension(player.getUUID(), destDimensionKey);
        } else {
            ChronoDawn.LOGGER.info("Skipped recording arrival dimension for {} - portal was destroyed",
                player.getName().getString());
        }

        return true;
    }

    /**
     * Calculate safe teleport position within portal.
     * Places player at horizontal center of portal interior, just above ground.
     *
     * @param portalBlockPos Portal block position (one of the interior blocks)
     * @param axis Portal axis
     * @return Safe teleport position
     */
    private static Vec3 calculateSafeTeleportPosition(BlockPos portalBlockPos, Direction.Axis axis) {
        // Portal interior is 2x3 (width 2, height 3)
        // Place player at horizontal center, just above ground (y + 0.1)

        if (axis == Direction.Axis.X) {
            // X-axis portal: portal extends along X axis
            // Center is at x+0.5, z+0.5 (middle of 2-wide area)
            return new Vec3(
                portalBlockPos.getX() + 0.5,
                portalBlockPos.getY() + 0.1,  // Just above ground
                portalBlockPos.getZ() + 0.5   // Center of portal width
            );
        } else {
            // Z-axis portal: portal extends along Z axis
            // Center is at x+0.5, z+0.5 (middle of 2-wide area)
            return new Vec3(
                portalBlockPos.getX() + 0.5,  // Center of portal width
                portalBlockPos.getY() + 0.1,  // Just above ground
                portalBlockPos.getZ() + 0.5
            );
        }
    }

    /**
     * Calculate player rotation when exiting portal.
     * Player should face away from portal plane.
     *
     * @param axis Portal axis
     * @return Y rotation (degrees)
     */
    private static float calculateTeleportRotation(Direction.Axis axis) {
        if (axis == Direction.Axis.X) {
            // X-axis portal: face south (positive Z direction)
            return 180.0F;
        } else {
            // Z-axis portal: face east (positive X direction)
            return 90.0F;
        }
    }

    /**
     * Find source portal frame position from PortalRegistry.
     * This ensures consistent coordinate mapping based on frame bottom-left position.
     *
     * @param level Source level
     * @param portalBlockPos Portal block position (where player collided)
     * @return Frame bottom-left position, or null if not found
     */
    private static BlockPos findSourcePortalFrame(ServerLevel level, BlockPos portalBlockPos) {
        // Search for portals in this dimension
        Set<UUID> portalsInDimension = PortalRegistry.getInstance().getPortalsInDimension(level.dimension());

        for (UUID portalId : portalsInDimension) {
            PortalStateMachine portal = PortalRegistry.getInstance().getPortal(portalId);
            if (portal == null) {
                continue;
            }

            BlockPos framePos = portal.getPosition();

            // Check if portal block is within this portal's area (5x5x5 from frame bottom-left)
            for (int dx = 0; dx <= 5; dx++) {
                for (int dy = 0; dy <= 5; dy++) {
                    for (int dz = 0; dz <= 5; dz++) {
                        BlockPos checkPos = framePos.offset(dx, dy, dz);
                        if (checkPos.equals(portalBlockPos)) {
                            ChronoDawn.LOGGER.info("Found source portal frame at {} for portal block at {}",
                                framePos, portalBlockPos);
                            return framePos;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get the destination dimension based on source dimension.
     *
     * @param sourceDimension Source dimension key
     * @return Destination dimension key
     */
    private static ResourceKey<Level> getDestinationDimension(ResourceKey<Level> sourceDimension) {
        if (sourceDimension.equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            // ChronoDawn → Overworld
            return Level.OVERWORLD;
        } else {
            // Overworld (or any other dimension) → ChronoDawn
            return ModDimensions.CHRONO_DAWN_DIMENSION;
        }
    }

    /**
     * Calculate destination coordinates (1:1 X/Z mapping, Y will be adjusted to ground level).
     *
     * @param sourceFramePos Source portal frame bottom-left position
     * @return Destination coordinates (X/Z only, Y will be determined by ground search)
     */
    private static BlockPos calculateDestinationCoords(BlockPos sourceFramePos) {
        // 1:1 coordinate mapping for X and Z (no scaling like Nether Portal)
        // Use frame bottom-left position to ensure consistent portal alignment
        // Y coordinate will be determined by findGroundLevel() when generating portal
        // Start search from high position (Y=150) to find surface, not underground caves
        return new BlockPos(sourceFramePos.getX(), 150, sourceFramePos.getZ());
    }

    /**
     * Find a nearby portal at destination coordinates.
     *
     * @param level Destination level
     * @param coords Destination coordinates (expected to be frame bottom-left position)
     * @return Portal block position if found, empty otherwise
     */
    private static Optional<BlockPos> findNearbyPortal(ServerLevel level, BlockPos coords) {
        // First, try to find existing portals using PortalRegistry
        // This is much more efficient than scanning blocks
        Set<UUID> portalsInDimension = PortalRegistry.getInstance().getPortalsInDimension(level.dimension());

        ChronoDawn.LOGGER.info("Searching for existing portal in dimension {} near {} (found {} portals in dimension)",
            level.dimension().location(), coords, portalsInDimension.size());

        BlockPos nearestPortal = null;
        double nearestDistance = Double.MAX_VALUE;

        for (UUID portalId : portalsInDimension) {
            PortalStateMachine portal = PortalRegistry.getInstance().getPortal(portalId);
            if (portal == null) {
                continue;
            }

            BlockPos portalPos = portal.getPosition();

            // Calculate distance (ignoring Y coordinate for more lenient matching)
            double distance = Math.sqrt(
                Math.pow(portalPos.getX() - coords.getX(), 2) +
                Math.pow(portalPos.getZ() - coords.getZ(), 2)
            );

            ChronoDawn.LOGGER.info("  Portal at {} - horizontal distance: {} blocks (search coords: {})",
                portalPos, String.format("%.1f", distance), coords);

            // Only consider portals within reasonable horizontal distance (128 blocks)
            if (distance < 128 && distance < nearestDistance) {
                nearestDistance = distance;
                nearestPortal = portalPos;
            }
        }

        if (nearestPortal != null) {
            // Find actual portal block position (registry stores frame bottom-left)
            // Search for portal blocks near the frame position (axis-independent)
            for (int dx = 0; dx <= 5; dx++) {
                for (int dy = 0; dy <= 5; dy++) {
                    for (int dz = 0; dz <= 5; dz++) {
                        BlockPos checkPos = nearestPortal.offset(dx, dy, dz);
                        if (level.getBlockState(checkPos).is(ModBlocks.CHRONO_DAWN_PORTAL.get())) {
                            ChronoDawn.LOGGER.info("Found existing portal at {} (frame at {}, distance: {} blocks from {})",
                                checkPos, nearestPortal, nearestDistance, coords);
                            return Optional.of(checkPos);
                        }
                    }
                }
            }

            // Portal frame exists in registry but portal blocks are missing
            ChronoDawn.LOGGER.warn("Portal frame at {} exists in registry but portal blocks not found", nearestPortal);
        }

        // Fallback: Search in a radius around destination coordinates (block-by-block search)
        for (int x = -PORTAL_SEARCH_RADIUS; x <= PORTAL_SEARCH_RADIUS; x++) {
            for (int y = -PORTAL_SEARCH_RADIUS; y <= PORTAL_SEARCH_RADIUS; y++) {
                for (int z = -PORTAL_SEARCH_RADIUS; z <= PORTAL_SEARCH_RADIUS; z++) {
                    BlockPos checkPos = coords.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);

                    if (state.is(ModBlocks.CHRONO_DAWN_PORTAL.get())) {
                        // Found portal block, return this position
                        return Optional.of(checkPos);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Generate a new portal at destination coordinates.
     *
     * Finds ground level and places portal on solid ground.
     *
     * @param level Destination level
     * @param coords Destination coordinates (expected X/Z for frame bottom-left, Y is initial search point)
     * @param axis Portal axis (X or Z) - should match source portal axis
     * @return Portal frame bottom-left position if generated, null if failed
     */
    private static BlockPos generatePortal(ServerLevel level, BlockPos coords, Direction.Axis axis) {
        // Find ground level starting from coords.y
        BlockPos groundPos = findGroundLevel(level, coords);

        // Generate a 4x5 portal at ground level with specified axis
        generatePortalStructure(level, groundPos, axis);
        return groundPos;
    }

    /**
     * Find ground level for portal placement using Heightmap.
     *
     * Uses WORLD_SURFACE heightmap to find the true surface level,
     * avoiding underground caves. Ensures there's enough air space above for the portal.
     *
     * @param level Level
     * @param start Starting search position (X/Z coordinates, Y is ignored)
     * @return Ground position suitable for portal placement (one block above solid ground)
     */
    private static BlockPos findGroundLevel(ServerLevel level, BlockPos start) {
        // Use Heightmap to find true surface level (avoids underground caves)
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, start.getX(), start.getZ());

        // Start checking from surface level
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(start.getX(), surfaceY, start.getZ());

        // Check if there's a solid block at surface level
        BlockState surfaceBlock = level.getBlockState(pos);

        // If surface is not solid (e.g., water, air), search downward for solid ground
        if (!surfaceBlock.isSolid() || surfaceBlock.isAir()) {
            while (pos.getY() > level.getMinBuildHeight()) {
                BlockState currentBlock = level.getBlockState(pos);
                if (currentBlock.isSolid() && !currentBlock.isAir()) {
                    // Found solid ground
                    break;
                }
                pos.move(Direction.DOWN);
            }
        }

        // Now pos is at solid ground level, check if there's enough air above
        BlockPos.MutableBlockPos airPos = pos.mutable().move(Direction.UP);

        // Check if there's enough air above (6 blocks: 1 spawn + 5 for portal)
        boolean hasEnoughAir = true;
        for (int i = 0; i < 6; i++) {
            BlockState airState = level.getBlockState(airPos);
            if (!airState.isAir()) {
                hasEnoughAir = false;
                break;
            }
            airPos.move(Direction.UP);
        }

        if (hasEnoughAir) {
            // Found suitable surface, return position one block above solid ground
            return pos.above().immutable();
        }

        // If not enough air at surface (e.g., dense forest), try a few blocks higher
        for (int offset = 1; offset <= 5; offset++) {
            BlockPos testPos = pos.above(offset);
            boolean canPlaceHere = true;

            // Check if this position and 5 blocks above are all air
            for (int i = 0; i < 6; i++) {
                if (!level.getBlockState(testPos.above(i)).isAir()) {
                    canPlaceHere = false;
                    break;
                }
            }

            if (canPlaceHere) {
                return testPos;
            }
        }

        // Fallback: return surface + 1 (may clip through trees, but at least on surface)
        return pos.above().immutable();
    }

    /**
     * Generate a portal structure at given position.
     *
     * Creates a 4x5 Clockstone frame with portal blocks in the interior.
     *
     * @param level Level
     * @param pos Bottom-left corner position
     * @param axis Portal axis
     */
    private static void generatePortalStructure(ServerLevel level, BlockPos pos, Direction.Axis axis) {
        int width = 4;
        int height = 5;

        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction vertical = Direction.UP;

        // Generate Clockstone frame
        BlockState clockstoneState = ModBlocks.CLOCKSTONE_BLOCK.get().defaultBlockState();

        // Bottom edge
        for (int x = 0; x < width; x++) {
            level.setBlock(pos.relative(horizontal, x), clockstoneState, 3);
        }

        // Top edge
        for (int x = 0; x < width; x++) {
            level.setBlock(pos.relative(horizontal, x).relative(vertical, height - 1), clockstoneState, 3);
        }

        // Left edge
        for (int y = 0; y < height; y++) {
            level.setBlock(pos.relative(vertical, y), clockstoneState, 3);
        }

        // Right edge
        for (int y = 0; y < height; y++) {
            level.setBlock(pos.relative(horizontal, width - 1).relative(vertical, y), clockstoneState, 3);
        }

        // Fill interior with portal blocks
        BlockState portalState = ModBlocks.CHRONO_DAWN_PORTAL.get()
            .defaultBlockState()
            .setValue(com.chronodawn.blocks.ChronoDawnPortalBlock.AXIS, axis);

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                level.setBlock(pos.relative(horizontal, x).relative(vertical, y), portalState, 3);
            }
        }

        // Register portal in registry
        UUID portalId = UUID.randomUUID();
        PortalStateMachine portal = new PortalStateMachine(
            portalId,
            level.dimension(),
            pos
        );
        portal.activate();
        PortalRegistry.getInstance().registerPortal(portal);

        ChronoDawn.LOGGER.info("Generated 4x5 portal structure at {} in dimension {}",
            pos, level.dimension().location());
    }

    /**
     * Destroy an unstable portal with glass break sound effect.
     *
     * When portals are unstable (player entered ChronoDawn but hasn't stabilized),
     * the portal blocks shatter like glass and disappear.
     *
     * @param level The level containing the portal
     * @param portalPos Position of a portal block in the unstable portal
     */
    private static void destroyUnstablePortal(ServerLevel level, BlockPos portalPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // Start BFS from the given portal position
        if (level.getBlockState(portalPos).is(ModBlocks.CHRONO_DAWN_PORTAL.get())) {
            queue.add(portalPos);
            visited.add(portalPos);
        }

        // BFS to find all connected portal blocks
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Remove this portal block
            level.removeBlock(current, false);

            // Check all 6 adjacent positions for more portal blocks
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = current.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacent);

                if (adjacentState.is(ModBlocks.CHRONO_DAWN_PORTAL.get()) && !visited.contains(adjacent)) {
                    queue.add(adjacent);
                    visited.add(adjacent);
                }
            }
        }

        // Play glass break sound at portal center
        if (!visited.isEmpty()) {
            level.playSound(
                null,
                portalPos,
                SoundEvents.GLASS_BREAK,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
            );
            ChronoDawn.LOGGER.info("Destroyed {} unstable portal blocks with glass break sound", visited.size());

            // Clear arrival dimension records for all players near the destroyed portal
            // This is critical - when portal is destroyed, players are no longer "in portal"
            // but the cleanup doesn't run immediately, so we clear it here
            double searchRadius = 10.0; // blocks
            for (Entity entity : level.getEntitiesOfClass(Entity.class,
                    new net.minecraft.world.phys.AABB(portalPos).inflate(searchRadius))) {
                if (entity instanceof net.minecraft.server.level.ServerPlayer) {
                    clearArrivalDimension(entity.getUUID());
                    ChronoDawn.LOGGER.info("Cleared arrival dimension for {} - portal destroyed",
                        entity.getName().getString());
                }
            }
        }
    }
}
