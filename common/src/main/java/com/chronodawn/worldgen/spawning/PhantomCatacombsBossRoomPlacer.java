package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Phantom Catacombs Boss Room Placer
 *
 * Manages placement of boss_room in Phantom Catacombs structure.
 *
 * Placement Strategy:
 * - Location: Phantom Catacombs Room 7 (any dimension where structure generates)
 * - Trigger: Structure generation complete
 * - Max per structure: 1 (even if multiple room_7 instances exist)
 *
 * Implementation:
 * - Searches for Amethyst Block marker in room_7
 * - Places boss_room NBT structure at the first marker found
 * - Marker block is replaced by the boss_room structure
 *
 * NBT Setup Required (room_7):
 * - Place 1 Amethyst Block (minecraft:amethyst_block) at boss_room entrance position
 * - The boss_room will be placed starting from this position
 * - Orientation: The marker indicates where the boss_room entrance should connect
 *
 * Reference: T238 - Phantom Catacombs boss room placement
 */
public class PhantomCatacombsBossRoomPlacer {
    private static final ResourceLocation PHANTOM_CATACOMBS_ID = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "phantom_catacombs"
    );

    private static final ResourceLocation ROOM_7_TEMPLATE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "phantom_catacombs_room_7"
    );

    private static final ResourceLocation BOSS_ROOM_TEMPLATE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "phantom_catacombs_boss_room"
    );

    // Track structure positions where we've already placed boss_room (per dimension)
    // Use structure's origin position (BlockPos) instead of chunk position for accurate tracking
    // ConcurrentHashMap for thread-safe access across multiple dimension ticks
    private static final Map<ResourceLocation, Set<BlockPos>> processedStructures = new java.util.concurrent.ConcurrentHashMap<>();

    // Track structures currently being processed (multi-tick state machine)
    // ConcurrentHashMap for thread-safe access across multiple dimension ticks
    private static final Map<BlockPos, StructureProcessingState> processingStates = new java.util.concurrent.ConcurrentHashMap<>();

    // Check interval (in ticks) - check every 30 seconds to allow structure to fully generate
    private static final int CHECK_INTERVAL = 600;
    // ConcurrentHashMap for thread-safe access across multiple dimension ticks
    private static final Map<ResourceLocation, Integer> tickCounters = new java.util.concurrent.ConcurrentHashMap<>();

    // Store the rotation of last placed room_7 (for boss_room orientation)
    private static Rotation lastRoom7Rotation = Rotation.NONE;

    /**
     * Processing state enum for multi-tick state machine.
     * Each state represents a phase of the boss room placement process.
     */
    private enum ProcessingPhase {
        SEARCHING_MARKERS,     // Searching for Crying Obsidian markers (chunk by chunk)
        EVALUATING_CANDIDATES, // Evaluating placement candidates (batch by batch)
        PLACING_ROOMS,         // Placing room_7 and boss_room
        COMPLETED              // Processing completed
    }

    /**
     * State object for tracking multi-tick processing of a structure.
     * This allows processing to be spread across multiple ticks to prevent freezing.
     */
    private static class StructureProcessingState {
        final BlockPos structureOrigin;
        final ChunkPos initialChunkPos;
        final ResourceLocation dimensionId;
        final BoundingBox boundingBox;
        ProcessingPhase phase;

        // Marker search state
        List<ChunkPos> structureChunks;
        int currentChunkIndex;
        List<BlockPos> foundMarkers;
        int retryCount;

        // Evaluation state
        List<PlacementCandidate> candidates;
        int currentCandidateIndex;

        // Templates
        StructureTemplate room7Template;
        StructureTemplate bossRoomTemplate;

        // Selected placement
        PlacementCandidate selectedCandidate;

        StructureProcessingState(BlockPos origin, ChunkPos chunkPos, ResourceLocation dimId, BoundingBox bbox) {
            this.structureOrigin = origin;
            this.initialChunkPos = chunkPos;
            this.dimensionId = dimId;
            this.boundingBox = bbox;
            this.phase = ProcessingPhase.SEARCHING_MARKERS;
            this.structureChunks = new ArrayList<>();
            this.currentChunkIndex = 0;
            this.foundMarkers = new ArrayList<>();
            this.retryCount = 0;
            this.candidates = null;
            this.currentCandidateIndex = 0;
        }
    }

    /**
     * Check if a chunk contains a Phantom Catacombs structure.
     */
    private static boolean hasPhantomCatacombs(ServerLevel level, ChunkPos chunkPos) {
        var structureManager = level.structureManager();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(PHANTOM_CATACOMBS_ID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Find suitable locations for room_7 placement.
     *
     * Strategy: Find dead-end rooms (marked with Crying Obsidian) in the maze.
     * These are ideal for conversion to room_7 + boss_room.
     *
     * Performance: Now runs asynchronously (T309), so we can use broader search range
     * without freezing the main thread. Uses chunk-based searching for efficiency.
     *
     * NOTE: This method is called from async thread - only read world state, don't modify!
     *
     * @param level           The ServerLevel
     * @param chunkPos        The chunk position containing the structure
     * @param boundingBox     The structure's bounding box (null to use default range)
     * @return List of potential room_7 placement positions
     */
    private static List<BlockPos> findDeadEndMarkers(ServerLevel level, ChunkPos chunkPos, net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox) {
        List<BlockPos> markers = new ArrayList<>();

        // Y range: use structure's actual bounding box with some padding
        // This allows the search to work regardless of terrain height (plains, mountains, etc.)
        // Fallback range covers all possible build heights (-64 to 320 in 1.18+)
        int minY = boundingBox != null ? boundingBox.minY() - 10 : -64;
        int maxY = boundingBox != null ? boundingBox.maxY() + 10 : 320;

        // Get all chunks where this structure exists
        var structureManager = level.structureManager();
        var structureChunks = new HashSet<ChunkPos>();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(PHANTOM_CATACOMBS_ID)) {
                it.unimi.dsi.fastutil.longs.LongSet chunks = entry.getValue();
                for (long chunkLong : chunks) {
                    structureChunks.add(new ChunkPos(chunkLong));
                }
            }
        }

        if (structureChunks.isEmpty()) {
            ChronoDawn.LOGGER.warn("[Async] No structure chunks found for Phantom Catacombs at {}", chunkPos);
            return markers;
        }

        ChronoDawn.LOGGER.info(
            "[Async] Searching for Crying Obsidian markers in {} structure chunks (Y: {} to {}){}",
            structureChunks.size(),
            minY,
            maxY,
            boundingBox != null ? " [based on structure bounding box: " + boundingBox + "]" : " [using default range]"
        );

        // Search for Crying Obsidian markers in structure chunks only
        // With async processing, we can scan all chunks without freezing (T309: async fix)
        int chunksScanned = 0;
        for (ChunkPos structureChunk : structureChunks) {
            chunksScanned++;

            // Scan this chunk for Crying Obsidian markers
            for (BlockPos pos : BlockPos.betweenClosed(
                structureChunk.getMinBlockX(), minY, structureChunk.getMinBlockZ(),
                structureChunk.getMaxBlockX(), maxY, structureChunk.getMaxBlockZ()
            )) {
                BlockState blockState = level.getBlockState(pos);

                // Check if this is a Crying Obsidian (dead-end room marker)
                if (blockState.is(Blocks.CRYING_OBSIDIAN)) {
                    markers.add(pos.immutable());
                }
            }
        }

        if (!markers.isEmpty()) {
            ChronoDawn.LOGGER.info("[Async] Found {} Crying Obsidian markers after scanning {} chunks", markers.size(), chunksScanned);
            for (int i = 0; i < Math.min(markers.size(), 5); i++) {
                BlockPos marker = markers.get(i);
                ChronoDawn.LOGGER.info("  Marker {}: {}",
                    i + 1,
                    marker
                );
            }
            if (markers.size() > 5) {
                ChronoDawn.LOGGER.info("  ... and {} more", markers.size() - 5);
            }
        } else {
            ChronoDawn.LOGGER.warn(
                "[Async] No Crying Obsidian markers found in {} structure chunks. Structure may not be fully loaded yet.",
                structureChunks.size()
            );
        }

        return markers;
    }

    /**
     * Find the Crying Obsidian marker position within room_7 template.
     *
     * @param template The room_7 template
     * @return Relative position of Crying Obsidian within template, or null if not found
     */
    private static BlockPos findRoom7Marker(StructureTemplate template) {
        // Search for Crying Obsidian in the template
        for (StructureTemplate.StructureBlockInfo blockInfo : template.filterBlocks(
            BlockPos.ZERO,
            new StructurePlaceSettings(),
            Blocks.CRYING_OBSIDIAN
        )) {
            // Return the first Crying Obsidian found (replacement marker)
            return blockInfo.pos();
        }
        return null;
    }

    /**
     * Find the Amethyst Block connector position within room_7 template.
     *
     * @param template The room_7 template
     * @return Relative position of Amethyst Block within template, or null if not found
     */
    private static BlockPos findRoom7Connector(StructureTemplate template) {
        // Search for Amethyst Block in the template
        for (StructureTemplate.StructureBlockInfo blockInfo : template.filterBlocks(
            BlockPos.ZERO,
            new StructurePlaceSettings(),
            Blocks.AMETHYST_BLOCK
        )) {
            // Return the first Amethyst Block found (connection point to boss_room)
            return blockInfo.pos();
        }
        return null;
    }

    /**
     * Find the Amethyst Block connector position within boss_room template.
     *
     * @param template The boss_room template
     * @return Relative position of Amethyst Block within template, or null if not found
     */
    private static BlockPos findBossRoomConnector(StructureTemplate template) {
        // Search for Amethyst Block in the template
        for (StructureTemplate.StructureBlockInfo blockInfo : template.filterBlocks(
            BlockPos.ZERO,
            new StructurePlaceSettings(),
            Blocks.AMETHYST_BLOCK
        )) {
            // Return the first Amethyst Block found (connection point)
            return blockInfo.pos();
        }
        return null;
    }

    /**
     * Detect the entrance direction of a dead_end room by checking adjacent blocks.
     *
     * Strategy: Check multiple blocks in each direction and count air blocks.
     * The direction with the most air blocks is the entrance (corridor).
     */
    private static Direction detectEntranceDirection(ServerLevel level, BlockPos markerPos) {
        int maxAirBlocks = 0;
        Direction entranceDir = Direction.NORTH;

        // Check each horizontal direction
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            int airCount = 0;

            // Check 5 blocks in this direction
            for (int dist = 1; dist <= 5; dist++) {
                BlockPos checkPos = markerPos.relative(dir, dist);
                BlockState state = level.getBlockState(checkPos);

                // Count air and non-solid blocks (corridors/rooms)
                if (state.isAir() || !state.isSolidRender(level, checkPos)) {
                    airCount++;
                }
            }

            // The direction with most air is the entrance
            if (airCount > maxAirBlocks) {
                maxAirBlocks = airCount;
                entranceDir = dir;
            }
        }

        ChronoDawn.LOGGER.debug(
            "Detected entrance direction: {} (air blocks: {})",
            entranceDir,
            maxAirBlocks
        );

        return entranceDir;
    }

    /**
     * Convert entrance direction to rotation.
     * Assumes room NBT is saved with entrance facing NORTH.
     */
    private static Rotation getRotationFromDirection(Direction entranceDir) {
        return switch (entranceDir) {
            case NORTH -> Rotation.NONE;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.CLOCKWISE_90;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    /**
     * Calculate exit direction from room_7's rotation.
     * room_7 NBT has Amethyst Block at X=6 (east side).
     * After rotation, the exit direction changes.
     */
    private static Direction getExitDirectionFromRotation(Rotation rotation) {
        return switch (rotation) {
            case NONE -> Direction.EAST;                 // Amethyst stays at east
            case CLOCKWISE_90 -> Direction.SOUTH;        // East rotates to south
            case CLOCKWISE_180 -> Direction.WEST;        // East rotates to west
            case COUNTERCLOCKWISE_90 -> Direction.NORTH; // East rotates to north
        };
    }

    /**
     * Convert exit direction to rotation for boss_room.
     * boss_room NBT has entrance at X=0 (west side), room extends east.
     *
     * exitDir indicates which direction room_7 exits (where boss_room should be).
     * boss_room entrance should face room_7 (opposite of exit direction).
     *
     * Rotation mapping (Y-axis, view from above):
     * - NONE: entrance stays WEST
     * - CLOCKWISE_90: entrance WEST → SOUTH
     * - CLOCKWISE_180: entrance WEST → EAST
     * - COUNTERCLOCKWISE_90: entrance WEST → NORTH
     *
     * Required entrance direction based on exit:
     * - exit EAST → entrance must face WEST → NONE
     * - exit WEST → entrance must face EAST → CLOCKWISE_180
     * - exit NORTH → entrance must face SOUTH → CLOCKWISE_90
     * - exit SOUTH → entrance must face NORTH → COUNTERCLOCKWISE_90
     */
    private static Rotation getBossRoomRotationFromExitDirection(Direction exitDir) {
        return switch (exitDir) {
            case NORTH -> Rotation.COUNTERCLOCKWISE_90; // entrance west->north (to face south back to room_7) - FIXED
            case SOUTH -> Rotation.CLOCKWISE_90;        // entrance west->south (to face north back to room_7) - FIXED
            case EAST -> Rotation.NONE;                 // entrance stays west (to face west back to room_7)
            case WEST -> Rotation.CLOCKWISE_180;        // entrance west->east (to face east back to room_7)
            default -> Rotation.NONE;
        };
    }

    /**
     * Place room_7 structure at the specified dead-end marker position.
     *
     * This replaces a dead-end room with room_7:
     * - Dead-end room has a Crying Obsidian marker
     * - room_7.nbt has a Crying Obsidian at the same relative position
     * - Both markers are aligned to replace the dead-end room
     * - Rotation is detected from the dead-end's entrance direction
     *
     * @param level        The ServerLevel
     * @param deadEndPos   The position of the Crying Obsidian marker in dead-end room
     * @return The position of room_7's Amethyst Block connector (for boss_room placement), or null if failed
     */
    private static BlockPos placeRoom7(ServerLevel level, BlockPos deadEndPos) {
        var structureTemplateManager = level.getStructureManager();

        // Load room_7 template
        var templateOptional = structureTemplateManager.get(ROOM_7_TEMPLATE);
        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load room_7 template: {}", ROOM_7_TEMPLATE);
            return null;
        }

        StructureTemplate template = templateOptional.get();

        // Find the Crying Obsidian marker in room_7 template (replacement marker)
        BlockPos room7Marker = findRoom7Marker(template);
        if (room7Marker == null) {
            ChronoDawn.LOGGER.error(
                "Room_7 template {} does not contain a Crying Obsidian marker",
                ROOM_7_TEMPLATE
            );
            return null;
        }

        // Find the Amethyst Block connector in room_7 template (for boss_room connection)
        BlockPos room7Connector = findRoom7Connector(template);
        if (room7Connector == null) {
            ChronoDawn.LOGGER.error(
                "Room_7 template {} does not contain an Amethyst Block connector",
                ROOM_7_TEMPLATE
            );
            return null;
        }

        // Detect entrance direction to match dead_end orientation
        Direction entranceDir = detectEntranceDirection(level, deadEndPos);
        Rotation rotation = getRotationFromDirection(entranceDir);

        ChronoDawn.LOGGER.debug(
            "Room_7 marker before rotation: {}, connector before rotation: {}",
            room7Marker,
            room7Connector
        );

        // Calculate placement offset with rotation
        StructurePlaceSettings tempSettings = new StructurePlaceSettings().setRotation(rotation);
        BlockPos rotatedMarker = StructureTemplate.calculateRelativePosition(tempSettings, room7Marker);
        BlockPos rotatedConnectorRelative = StructureTemplate.calculateRelativePosition(tempSettings, room7Connector);

        ChronoDawn.LOGGER.debug(
            "Room_7 after rotation {}: marker {} → {}, connector {} → {}",
            rotation,
            room7Marker,
            rotatedMarker,
            room7Connector,
            rotatedConnectorRelative
        );

        BlockPos placementPos = deadEndPos.subtract(rotatedMarker);

        ChronoDawn.LOGGER.debug(
            "Room_7 placement: deadEndPos {} - rotatedMarker {} = placementPos {}",
            deadEndPos,
            rotatedMarker,
            placementPos
        );

        // Get template size to calculate bounding box
        net.minecraft.core.Vec3i templateSize = template.getSize();
        int sizeX = templateSize.getX();
        int sizeY = templateSize.getY();
        int sizeZ = templateSize.getZ();

        ChronoDawn.LOGGER.debug(
            "Room_7 template size: {}x{}x{} (rotation: {})",
            sizeX, sizeY, sizeZ, rotation
        );

        // Remove water in the room_7 area before placement
        int waterRemoved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            // Remove water blocks
            if (state.getFluidState().isSource() && state.is(Blocks.WATER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                waterRemoved++;
            }
        }

        if (waterRemoved > 0) {
            ChronoDawn.LOGGER.debug("Removed {} water blocks before room_7 placement", waterRemoved);
        }

        // Create placement settings without rotation
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Remove the Crying Obsidian marker before placing room_7
        level.setBlock(deadEndPos, Blocks.AIR.defaultBlockState(), 3);
        ChronoDawn.LOGGER.debug("Removed Crying Obsidian marker at {}", deadEndPos);

        // Place room_7 structure
        template.placeInWorld(level, placementPos, placementPos, settings, level.random, 2);

        // Remove waterlogging after placement
        int waterloggedRemoved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            // Remove waterlogged state
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                boolean waterlogged = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
                if (waterlogged) {
                    level.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, false), 2);
                    waterloggedRemoved++;
                }
            }
        }

        if (waterloggedRemoved > 0) {
            ChronoDawn.LOGGER.debug("Removed {} waterlogged blocks after room_7 placement", waterloggedRemoved);
        }

        // Calculate world position of room_7's Amethyst Block connector (with rotation)
        BlockPos rotatedConnector = StructureTemplate.calculateRelativePosition(tempSettings, room7Connector);
        BlockPos connectorWorldPos = placementPos.offset(rotatedConnector);

        ChronoDawn.LOGGER.info(
            "Placed room_7 at {} (replacing dead-end at {}), connector at {}, rotation: {}",
            placementPos,
            deadEndPos,
            connectorWorldPos,
            rotation
        );

        // Store rotation for boss_room placement
        lastRoom7Rotation = rotation;

        return connectorWorldPos;
    }

    /**
     * Place room_7 structure with specified rotation (for collision detection workflow).
     *
     * @param level        ServerLevel
     * @param deadEndPos   Dead-end marker position
     * @param rotation     Rotation to apply
     * @param template     room_7 template (already loaded)
     * @return World position of room_7's Amethyst Block connector
     */
    private static BlockPos placeRoom7WithRotation(
        ServerLevel level,
        BlockPos deadEndPos,
        Rotation rotation,
        StructureTemplate template
    ) {
        // Find the Crying Obsidian marker in room_7 template (replacement marker)
        BlockPos room7Marker = findRoom7Marker(template);
        if (room7Marker == null) {
            ChronoDawn.LOGGER.error(
                "Room_7 template {} does not contain a Crying Obsidian marker",
                ROOM_7_TEMPLATE
            );
            return null;
        }

        // Find the Amethyst Block connector in room_7 template (for boss_room connection)
        BlockPos room7Connector = findRoom7Connector(template);
        if (room7Connector == null) {
            ChronoDawn.LOGGER.error(
                "Room_7 template {} does not contain an Amethyst Block connector",
                ROOM_7_TEMPLATE
            );
            return null;
        }

        ChronoDawn.LOGGER.debug(
            "Placing room_7 with specified rotation {}: marker before rotation: {}, connector before rotation: {}",
            rotation,
            room7Marker,
            room7Connector
        );

        // Calculate placement offset with rotation
        StructurePlaceSettings tempSettings = new StructurePlaceSettings().setRotation(rotation);
        BlockPos rotatedMarker = StructureTemplate.calculateRelativePosition(tempSettings, room7Marker);
        BlockPos rotatedConnectorRelative = StructureTemplate.calculateRelativePosition(tempSettings, room7Connector);

        ChronoDawn.LOGGER.debug(
            "Room_7 after rotation {}: marker {} → {}, connector {} → {}",
            rotation,
            room7Marker,
            rotatedMarker,
            room7Connector,
            rotatedConnectorRelative
        );

        BlockPos placementPos = deadEndPos.subtract(rotatedMarker);

        ChronoDawn.LOGGER.debug(
            "Room_7 placement: deadEndPos {} - rotatedMarker {} = placementPos {}",
            deadEndPos,
            rotatedMarker,
            placementPos
        );

        // Get template size to calculate bounding box
        net.minecraft.core.Vec3i templateSize = template.getSize();
        int sizeX = templateSize.getX();
        int sizeY = templateSize.getY();
        int sizeZ = templateSize.getZ();

        ChronoDawn.LOGGER.debug(
            "Room_7 template size: {}x{}x{} (rotation: {})",
            sizeX, sizeY, sizeZ, rotation
        );

        // Remove water in the room_7 area before placement
        int waterRemoved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            // Remove water blocks
            if (state.getFluidState().isSource() && state.is(Blocks.WATER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                waterRemoved++;
            }
        }

        if (waterRemoved > 0) {
            ChronoDawn.LOGGER.debug("Removed {} water blocks before room_7 placement", waterRemoved);
        }

        // Create placement settings
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Remove the Crying Obsidian marker before placing room_7
        level.setBlock(deadEndPos, Blocks.AIR.defaultBlockState(), 3);
        ChronoDawn.LOGGER.debug("Removed Crying Obsidian marker at {}", deadEndPos);

        // Place room_7 structure
        template.placeInWorld(level, placementPos, placementPos, settings, level.random, 2);

        // Remove waterlogging after placement
        int waterloggedRemoved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            // Remove waterlogged state
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                boolean waterlogged = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
                if (waterlogged) {
                    level.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, false), 2);
                    waterloggedRemoved++;
                }
            }
        }

        if (waterloggedRemoved > 0) {
            ChronoDawn.LOGGER.debug("Removed {} waterlogged blocks after room_7 placement", waterloggedRemoved);
        }

        // Calculate world position of room_7's Amethyst Block connector (with rotation)
        BlockPos rotatedConnector = StructureTemplate.calculateRelativePosition(tempSettings, room7Connector);
        BlockPos connectorWorldPos = placementPos.offset(rotatedConnector);

        ChronoDawn.LOGGER.info(
            "Placed room_7 at {} (replacing dead-end at {}), connector at {}, rotation: {}",
            placementPos,
            deadEndPos,
            connectorWorldPos,
            rotation
        );

        return connectorWorldPos;
    }

    /**
     * Detect the exit direction of room_7 from the Amethyst Block connector.
     * The boss_room should extend in the opposite direction of the entrance.
     */
    private static Direction detectExitDirection(ServerLevel level, BlockPos connectorPos) {
        // Check which direction has more air/open space (that's where boss_room should go)
        int maxAirBlocks = 0;
        Direction bestDir = Direction.NORTH;

        ChronoDawn.LOGGER.debug("Detecting exit direction from connector at {}", connectorPos);

        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            int airBlocks = 0;
            // Check 10 blocks in this direction
            for (int i = 1; i <= 10; i++) {
                BlockPos checkPos = connectorPos.relative(dir, i);
                if (level.getBlockState(checkPos).isAir() ||
                    !level.getBlockState(checkPos).isSolidRender(level, checkPos)) {
                    airBlocks++;
                }
            }

            ChronoDawn.LOGGER.debug("  Direction {}: {} air/non-solid blocks", dir, airBlocks);

            if (airBlocks > maxAirBlocks) {
                maxAirBlocks = airBlocks;
                bestDir = dir;
            }
        }

        ChronoDawn.LOGGER.debug(
            "Detected exit direction: {} (air blocks: {})",
            bestDir,
            maxAirBlocks
        );

        return bestDir;
    }

    /**
     * Place boss_room structure at the specified connector position.
     *
     * This uses Jigsaw-like connection logic:
     * - room_7 has an Amethyst Block at the connection point (exit to boss_room)
     * - boss_room.nbt has an Amethyst Block at the connection point (entrance from room_7)
     * - Both Amethyst Blocks are aligned to the same world position
     * - Rotation is detected to avoid overwriting existing structures
     *
     * @param level        The ServerLevel
     * @param connectorPos The position of the Amethyst Block connector in room_7
     * @return true if placement was successful
     */
    private static boolean placeBossRoom(ServerLevel level, BlockPos connectorPos) {
        var structureTemplateManager = level.getStructureManager();

        // Load boss_room template
        var templateOptional = structureTemplateManager.get(BOSS_ROOM_TEMPLATE);
        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load boss_room template: {}", BOSS_ROOM_TEMPLATE);
            return false;
        }

        StructureTemplate template = templateOptional.get();

        // Find the Amethyst Block connector in boss_room template
        BlockPos bossRoomConnector = findBossRoomConnector(template);
        if (bossRoomConnector == null) {
            ChronoDawn.LOGGER.error(
                "Boss_room template {} does not contain an Amethyst Block connector",
                BOSS_ROOM_TEMPLATE
            );
            return false;
        }

        // Calculate exit direction from room_7's rotation (more reliable than detecting air blocks)
        Direction exitDir = getExitDirectionFromRotation(lastRoom7Rotation);
        Rotation rotation = getBossRoomRotationFromExitDirection(exitDir);

        ChronoDawn.LOGGER.debug(
            "Boss_room connector before rotation: {}, room_7 rotation: {}, exit direction: {}, boss_room rotation: {}",
            bossRoomConnector,
            lastRoom7Rotation,
            exitDir,
            rotation
        );

        // Calculate placement offset with rotation
        StructurePlaceSettings tempSettings = new StructurePlaceSettings().setRotation(rotation);
        BlockPos rotatedConnector = StructureTemplate.calculateRelativePosition(tempSettings, bossRoomConnector);

        ChronoDawn.LOGGER.debug(
            "Boss_room connector after rotation {}: {} → {}",
            rotation,
            bossRoomConnector,
            rotatedConnector
        );

        // Align Amethyst Blocks with 1-block offset to prevent overlap
        // Since both Amethyst Blocks are on the inner edge of the wall,
        // we need to offset boss_room by 1 block away from room_7
        BlockPos basePlacementPos = connectorPos.subtract(rotatedConnector);

        // Offset by 1 block in exit direction to prevent 1-block overlap
        BlockPos exitOffset = switch (exitDir) {
            case EAST -> new BlockPos(1, 0, 0);
            case WEST -> new BlockPos(-1, 0, 0);
            case NORTH -> new BlockPos(0, 0, -1);
            case SOUTH -> new BlockPos(0, 0, 1);
            default -> BlockPos.ZERO;
        };

        BlockPos placementPos = basePlacementPos.offset(exitOffset);
        BlockPos bossRoomConnectorWorldPos = placementPos.offset(rotatedConnector);

        ChronoDawn.LOGGER.debug(
            "Boss_room placement calculation: connectorPos {} - rotatedConnector {} = basePlacementPos {} + exitOffset {} = placementPos {}",
            connectorPos,
            rotatedConnector,
            basePlacementPos,
            exitOffset,
            placementPos
        );

        ChronoDawn.LOGGER.debug(
            "Verification: boss_room Amethyst Block will be at {} (should match room_7 connector at {})",
            bossRoomConnectorWorldPos,
            connectorPos
        );

        // Get template size to calculate bounding box
        net.minecraft.core.Vec3i templateSize = template.getSize();
        int sizeX = templateSize.getX();
        int sizeY = templateSize.getY();
        int sizeZ = templateSize.getZ();

        ChronoDawn.LOGGER.debug(
            "Boss_room template size: {}x{}x{} (rotation: {})",
            sizeX, sizeY, sizeZ, rotation
        );

        // Remove water in the boss_room area before placement
        int waterRemoved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            // Remove water blocks
            if (state.getFluidState().isSource() && state.is(Blocks.WATER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                waterRemoved++;
            }
        }

        if (waterRemoved > 0) {
            ChronoDawn.LOGGER.debug("Removed {} water blocks before boss_room placement", waterRemoved);
        }

        // Load processor list for waterlogging prevention
        ResourceLocation processorListId = ResourceLocation.fromNamespaceAndPath(
            ChronoDawn.MOD_ID,
            "convert_decorative_water"
        );

        // Create placement settings with processors
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Add processors from registry
        try {
            var processorListRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.PROCESSOR_LIST);
            var processorList = processorListRegistry.get(processorListId);

            if (processorList != null) {
                ChronoDawn.LOGGER.debug("Found processor list {}: {} processors", processorListId, processorList.list().size());
                for (var processor : processorList.list()) {
                    settings.addProcessor(processor);
                    ChronoDawn.LOGGER.debug("  Added processor: {}", processor.getClass().getSimpleName());
                }
                ChronoDawn.LOGGER.debug("Successfully applied {} structure processors to boss_room placement", processorList.list().size());
            } else {
                ChronoDawn.LOGGER.error("Processor list {} not found in registry! Boss_room WILL have waterlogging issues", processorListId);
                ChronoDawn.LOGGER.error("Available processor lists: {}", processorListRegistry.keySet());
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to load processor list {}: {}", processorListId, e.getMessage(), e);
        }

        // Place structure
        template.placeInWorld(level, placementPos, placementPos, settings, level.random, 2);

        // Remove water blocks after placement (in case water flowed in from surroundings)
        int waterRemovedAfter = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos.offset(-1, -1, -1),  // Expand by 1 block in all directions
            placementPos.offset(sizeX + 1, sizeY + 1, sizeZ + 1)
        )) {
            BlockState state = level.getBlockState(pos);
            // Remove water blocks that may have flowed in
            if (state.getFluidState().isSource() && state.is(Blocks.WATER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                waterRemovedAfter++;
            }
        }

        if (waterRemovedAfter > 0) {
            ChronoDawn.LOGGER.debug("Removed {} water blocks after boss_room placement (flowed in from surroundings)", waterRemovedAfter);
        }

        // Remove waterlogging after placement
        int waterloggedRemoved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            // Remove waterlogged state
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                boolean waterlogged = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
                if (waterlogged) {
                    level.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, false), 2);
                    waterloggedRemoved++;
                }
            }
        }

        if (waterloggedRemoved > 0) {
            ChronoDawn.LOGGER.debug("Removed {} waterlogged blocks after boss_room placement", waterloggedRemoved);
        }

        ChronoDawn.LOGGER.info(
            "Placed boss_room at {} (connector aligned to {}) in dimension {}",
            placementPos,
            connectorPos,
            level.dimension().location()
        );

        // Register boss_room for Temporal Phantom spawning
        // Calculate boss_room center from connector position (entrance)
        // boss_room is 21x21x9, center is ~10 blocks from entrance in exit direction
        // exitDir indicates where boss_room extends from connector
        BlockPos centerOffset = switch (exitDir) {
            case EAST -> new BlockPos(10, 4, 0);   // Room extends east
            case WEST -> new BlockPos(-10, 4, 0);  // Room extends west
            case NORTH -> new BlockPos(0, 4, -10); // Room extends north
            case SOUTH -> new BlockPos(0, 4, 10);  // Room extends south
            default -> new BlockPos(0, 4, 0);
        };
        BlockPos bossRoomCenter = connectorPos.offset(centerOffset);

        ChronoDawn.LOGGER.debug(
            "Registering boss_room for Temporal Phantom spawning: connector at {}, exit direction {}, center at {}",
            connectorPos,
            exitDir,
            bossRoomCenter
        );

        TemporalPhantomSpawner.registerBossRoom(level, bossRoomCenter);

        return true;
    }

    /**
     * Get the structure's origin position for tracking.
     * Returns the minimum corner of the structure's bounding box.
     */
    private static BlockPos getStructureOrigin(ServerLevel level, ChunkPos chunkPos) {
        var structureManager = level.structureManager();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(PHANTOM_CATACOMBS_ID)) {
                var structureStart = entry.getValue();
                // Get first chunk in the structure as origin
                if (!structureStart.isEmpty()) {
                    long firstChunk = structureStart.iterator().nextLong();
                    return new ChunkPos(firstChunk).getWorldPosition();
                }
            }
        }
        return null;
    }

    /**
     * Get the structure's bounding box by scanning for structure blocks.
     * As a fallback, scans the actual world for structure blocks to determine bounds.
     */
    private static net.minecraft.world.level.levelgen.structure.BoundingBox getStructureBoundingBox(ServerLevel level, ChunkPos chunkPos) {
        var structureManager = level.structureManager();
        int structureCount = 0;

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            structureCount++;

            if (structureLocation != null && structureLocation.equals(PHANTOM_CATACOMBS_ID)) {
                it.unimi.dsi.fastutil.longs.LongSet chunks = entry.getValue();
                ChronoDawn.LOGGER.debug("Found Phantom Catacombs structure with {} chunks", chunks.size());

                // Try to get bounding box from StructureStart
                int validStarts = 0;
                for (long chunkLong : chunks) {
                    ChunkPos structureChunk = new ChunkPos(chunkLong);
                    var startForStructure = structureManager.getStructureAt(structureChunk.getWorldPosition(), structure);
                    if (startForStructure != null && startForStructure.isValid()) {
                        validStarts++;
                        net.minecraft.world.level.levelgen.structure.BoundingBox box = startForStructure.getBoundingBox();
                        if (box != null) {
                            ChronoDawn.LOGGER.info("Successfully retrieved bounding box from StructureStart: {}", box);
                            return box;
                        } else {
                            ChronoDawn.LOGGER.debug("StructureStart is valid but bounding box is null for chunk {}", structureChunk);
                        }
                    }
                }
                ChronoDawn.LOGGER.debug("Checked {} structure chunks, found {} valid StructureStarts, but no bounding box", chunks.size(), validStarts);

                // Fallback: scan chunks for actual structure blocks to find Y bounds
                ChronoDawn.LOGGER.warn("Could not get bounding box from StructureStart, scanning chunks for structure blocks...");
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;
                int minX = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int minZ = Integer.MAX_VALUE;
                int maxZ = Integer.MIN_VALUE;
                boolean foundAny = false;
                int chunksScanned = 0;
                int chunksSkipped = 0;

                for (long chunkLong : chunks) {
                    ChunkPos structureChunk = new ChunkPos(chunkLong);

                    // Check if chunk is loaded before accessing it
                    if (!level.hasChunk(structureChunk.x, structureChunk.z)) {
                        ChronoDawn.LOGGER.debug("Chunk {} not loaded, skipping bounding box scan", structureChunk);
                        chunksSkipped++;
                        continue;
                    }

                    chunksScanned++;
                    var chunk = level.getChunk(structureChunk.x, structureChunk.z);

                    // Scan this chunk for structure blocks (e.g., deepslate, which is common in Phantom Catacombs)
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                                BlockPos pos = new BlockPos(structureChunk.getMinBlockX() + x, y, structureChunk.getMinBlockZ() + z);
                                BlockState state = chunk.getBlockState(pos);

                                // Look for structure-specific blocks (deepslate bricks, etc.)
                                if (state.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICKS) ||
                                    state.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICK_STAIRS) ||
                                    state.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICK_SLAB) ||
                                    state.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICK_WALL) ||
                                    state.is(net.minecraft.world.level.block.Blocks.CRACKED_DEEPSLATE_BRICKS) ||
                                    state.is(net.minecraft.world.level.block.Blocks.CRYING_OBSIDIAN)) {

                                    foundAny = true;
                                    minX = Math.min(minX, pos.getX());
                                    maxX = Math.max(maxX, pos.getX());
                                    minY = Math.min(minY, pos.getY());
                                    maxY = Math.max(maxY, pos.getY());
                                    minZ = Math.min(minZ, pos.getZ());
                                    maxZ = Math.max(maxZ, pos.getZ());
                                }
                            }
                        }
                    }
                }

                ChronoDawn.LOGGER.debug("Chunk scan results: {} scanned, {} skipped (not loaded)", chunksScanned, chunksSkipped);

                if (foundAny) {
                    var scannedBox = new net.minecraft.world.level.levelgen.structure.BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                    ChronoDawn.LOGGER.info("Created bounding box from chunk scan: {}", scannedBox);
                    return scannedBox;
                } else {
                    ChronoDawn.LOGGER.warn("No structure blocks found in chunk scan (scanned: {}, skipped: {})", chunksScanned, chunksSkipped);
                }
            }
        }

        if (structureCount == 0) {
            ChronoDawn.LOGGER.warn("No structures found at chunk position {}", chunkPos);
        }

        return null;
    }

    /**
     * Process a Phantom Catacombs structure and place room_7 + boss_room if not already placed.
     *
     * Strategy (Multi-tick state machine - T309):
     * 1. Find dead-end rooms (marked with Crying Obsidian) [SEARCHING_MARKERS phase]
     * 2. Evaluate placement candidates [EVALUATING_CANDIDATES phase]
     * 3. Replace the first dead-end room with room_7 [PLACING_ROOMS phase]
     * 4. Connect boss_room to room_7's exit [PLACING_ROOMS phase]
     *
     * This method initializes processing state and returns immediately.
     * Processing continues over multiple ticks via progressProcessing().
     *
     * @param level     The ServerLevel
     * @param chunkPos  The chunk position containing the structure
     */
    public static void processStructure(ServerLevel level, ChunkPos chunkPos) {
        ResourceLocation dimensionId = level.dimension().location();

        // Initialize tracking for this dimension if needed
        // Use ConcurrentHashMap.newKeySet() for thread-safe Set
        processedStructures.putIfAbsent(dimensionId, java.util.concurrent.ConcurrentHashMap.newKeySet());

        Set<BlockPos> dimensionProcessed = processedStructures.get(dimensionId);

        // Check if this chunk contains a Phantom Catacombs structure
        if (!hasPhantomCatacombs(level, chunkPos)) {
            return;
        }

        // Get structure origin for accurate tracking
        BlockPos structureOrigin = getStructureOrigin(level, chunkPos);
        if (structureOrigin == null) {
            return;
        }

        // Skip if we've already processed or are currently processing this structure
        if (dimensionProcessed.contains(structureOrigin) || processingStates.containsKey(structureOrigin)) {
            return;
        }

        // Get structure bounding box for accurate Y range search
        net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox = getStructureBoundingBox(level, chunkPos);

        if (boundingBox == null) {
            ChronoDawn.LOGGER.warn(
                "Could not determine bounding box for Phantom Catacombs at chunk {} (origin: {}). Will use default Y range (-64 to 320) for marker search.",
                chunkPos,
                structureOrigin
            );
            // Continue processing with null boundingBox - marker search will use default Y range
        }

        ChronoDawn.LOGGER.debug(
            "Found Phantom Catacombs at chunk {} (origin: {}) in dimension {}, initializing multi-tick processing",
            chunkPos,
            structureOrigin,
            dimensionId
        );

        // Initialize processing state for multi-tick state machine
        StructureProcessingState state = new StructureProcessingState(structureOrigin, chunkPos, dimensionId, boundingBox);

        ChronoDawn.LOGGER.debug(
            "Created processing state for {} (phase: {})",
            structureOrigin,
            state.phase
        );

        // Get structure chunks for marker search
        // Use StructureStart pieces to get accurate chunk coverage
        var structureManager = level.structureManager();

        // Get StructureStart from the initial chunk
        net.minecraft.world.level.levelgen.structure.StructureStart structureStart = null;

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(PHANTOM_CATACOMBS_ID)) {
                structureStart = structureManager.getStructureAt(chunkPos.getWorldPosition(), structure);
                break;
            }
        }

        if (structureStart == null || !structureStart.isValid()) {
            ChronoDawn.LOGGER.warn("Could not get valid StructureStart for Phantom Catacombs at {}", structureOrigin);
            // Fallback: use a large search area based on structure origin
            ChunkPos originChunkPos = new ChunkPos(structureOrigin);
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    state.structureChunks.add(new ChunkPos(originChunkPos.x + dx, originChunkPos.z + dz));
                }
            }
            ChronoDawn.LOGGER.debug(
                "Using fallback chunk search: {} chunks",
                state.structureChunks.size()
            );
        } else {
            // Get all pieces and calculate chunks from their bounding boxes
            var pieces = structureStart.getPieces();
            ChronoDawn.LOGGER.debug(
                "StructureStart has {} pieces",
                pieces.size()
            );

            for (var piece : pieces) {
                BoundingBox pieceBB = piece.getBoundingBox();
                if (pieceBB != null) {
                    // Calculate all chunks that this piece overlaps
                    int minChunkX = pieceBB.minX() >> 4; // Divide by 16
                    int maxChunkX = pieceBB.maxX() >> 4;
                    int minChunkZ = pieceBB.minZ() >> 4;
                    int maxChunkZ = pieceBB.maxZ() >> 4;

                    for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                        for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                            ChunkPos pieceChunk = new ChunkPos(cx, cz);
                            if (!state.structureChunks.contains(pieceChunk)) {
                                state.structureChunks.add(pieceChunk);
                            }
                        }
                    }
                }
            }

            ChronoDawn.LOGGER.debug(
                "Collected {} chunks from {} pieces",
                state.structureChunks.size(),
                pieces.size()
            );
        }

        if (state.structureChunks.isEmpty()) {
            ChronoDawn.LOGGER.warn("No structure chunks found for Phantom Catacombs at {}, cannot process", structureOrigin);
            return;
        }

        // Load templates
        var structureTemplateManager = level.getServer().getStructureManager();
        var room7TemplateOpt = structureTemplateManager.get(ROOM_7_TEMPLATE);
        var bossRoomTemplateOpt = structureTemplateManager.get(BOSS_ROOM_TEMPLATE);

        if (room7TemplateOpt.isEmpty() || bossRoomTemplateOpt.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load templates for boss_room placement");
            dimensionProcessed.add(structureOrigin);
            return;
        }

        state.room7Template = room7TemplateOpt.get();
        state.bossRoomTemplate = bossRoomTemplateOpt.get();

        // Register state for multi-tick processing
        processingStates.put(structureOrigin, state);

        ChronoDawn.LOGGER.debug(
            "Registered processing state for {} with {} chunks",
            structureOrigin,
            state.structureChunks.size()
        );
    }

    /**
     * Progress the multi-tick processing for all active structures.
     * This should be called every server tick.
     *
     * @param level The ServerLevel
     */
    public static void progressAllProcessing(ServerLevel level) {
        ResourceLocation currentDimension = level.dimension().location();

        // Filter structures that belong to the current dimension
        List<BlockPos> activeStructures = new ArrayList<>();
        for (Map.Entry<BlockPos, StructureProcessingState> entry : processingStates.entrySet()) {
            StructureProcessingState state = entry.getValue();
            if (state.dimensionId.equals(currentDimension)) {
                activeStructures.add(entry.getKey());
            }
        }

        for (BlockPos structureOrigin : activeStructures) {
            StructureProcessingState state = processingStates.get(structureOrigin);
            if (state != null) {
                progressProcessing(level, state);
            }
        }
    }

    /**
     * Progress one tick of processing for a structure.
     * Each call processes a small chunk of work to avoid freezing.
     *
     * @param level The ServerLevel
     * @param state The processing state
     */
    private static void progressProcessing(ServerLevel level, StructureProcessingState state) {
        try {
            switch (state.phase) {
                case SEARCHING_MARKERS -> progressMarkerSearch(level, state);
                case EVALUATING_CANDIDATES -> progressCandidateEvaluation(level, state);
                case PLACING_ROOMS -> progressRoomPlacement(level, state);
                case COMPLETED -> {
                    // Remove from processing states
                    processingStates.remove(state.structureOrigin);
                    // Add to processed structures
                    // Use ConcurrentHashMap.newKeySet() for thread-safe Set
                    processedStructures.computeIfAbsent(state.dimensionId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                        .add(state.structureOrigin);
                }
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("[PhaseExecution] ERROR processing structure {} in phase {}: {}",
                state.structureOrigin, state.phase, e.getMessage(), e);
            // Clean up on error
            processingStates.remove(state.structureOrigin);
            // Use ConcurrentHashMap.newKeySet() for thread-safe Set
            processedStructures.computeIfAbsent(state.dimensionId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(state.structureOrigin);
        }
    }

    /**
     * Progress marker search phase - scans one chunk per tick.
     */
    private static void progressMarkerSearch(ServerLevel level, StructureProcessingState state) {

        // Process one chunk per tick
        if (state.currentChunkIndex >= state.structureChunks.size()) {
            // All chunks scanned
            if (state.foundMarkers.isEmpty()) {
                // No markers found
                state.retryCount++;
                if (state.retryCount >= 5) {
                    ChronoDawn.LOGGER.warn(
                        "No Crying Obsidian markers found after {} retries for structure {}. Using fallback placement.",
                        state.retryCount,
                        state.structureOrigin
                    );
                    // Move to placement with null selected (triggers fallback)
                    state.phase = ProcessingPhase.PLACING_ROOMS;
                } else {
                    // Reset and retry
                    state.currentChunkIndex = 0;
                    state.foundMarkers.clear();
                }
            } else if (state.foundMarkers.size() < 1) {
                // Too few markers
                state.retryCount++;
                if (state.retryCount >= 5) {
                    ChronoDawn.LOGGER.warn(
                        "Only {} markers found after {} retries for structure {}. Using fallback placement.",
                        state.foundMarkers.size(),
                        state.retryCount,
                        state.structureOrigin
                    );
                    state.phase = ProcessingPhase.PLACING_ROOMS;
                } else {
                    state.currentChunkIndex = 0;
                    state.foundMarkers.clear();
                }
            } else {
                // Success - move to evaluation phase
                ChronoDawn.LOGGER.info(
                    "Found {} Crying Obsidian markers for Phantom Catacombs at {}",
                    state.foundMarkers.size(),
                    state.structureOrigin
                );
                state.phase = ProcessingPhase.EVALUATING_CANDIDATES;
            }
            return;
        }

        // Scan one chunk
        ChunkPos currentChunk = state.structureChunks.get(state.currentChunkIndex);
        int minY = state.boundingBox != null ? state.boundingBox.minY() - 10 : -64;
        int maxY = state.boundingBox != null ? state.boundingBox.maxY() + 10 : 320;

        for (BlockPos pos : BlockPos.betweenClosed(
            currentChunk.getMinBlockX(), minY, currentChunk.getMinBlockZ(),
            currentChunk.getMaxBlockX(), maxY, currentChunk.getMaxBlockZ()
        )) {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.is(Blocks.CRYING_OBSIDIAN)) {
                state.foundMarkers.add(pos.immutable());
            }
        }

        state.currentChunkIndex++;
    }

    /**
     * Progress candidate evaluation phase - evaluates a batch of candidates per tick.
     */
    private static void progressCandidateEvaluation(ServerLevel level, StructureProcessingState state) {
        // Generate all candidates on first evaluation tick
        if (state.candidates == null) {
            state.candidates = evaluateAllPlacements(
                level,
                state.foundMarkers,
                state.room7Template,
                state.bossRoomTemplate
            );

            if (state.candidates.isEmpty()) {
                ChronoDawn.LOGGER.error("No placement candidates generated for structure {}", state.structureOrigin);
                state.phase = ProcessingPhase.PLACING_ROOMS; // Fallback
                return;
            }

            ChronoDawn.LOGGER.debug(
                "Generated {} placement candidates for structure {}. Starting evaluation...",
                state.candidates.size(),
                state.structureOrigin
            );
        }

        // All candidates evaluated - select best one
        if (state.currentCandidateIndex >= state.candidates.size()) {
            // Select best candidate
            List<PlacementCandidate> collisionFree = state.candidates.stream()
                .filter(c -> c.collidingRoomCount == 0)
                .toList();

            if (!collisionFree.isEmpty()) {
                state.selectedCandidate = collisionFree.get(level.random.nextInt(collisionFree.size()));
                ChronoDawn.LOGGER.info(
                    "Phase 1: Found {} collision-free placements for {}. Selected dead_end at {}, rotation {}",
                    collisionFree.size(),
                    state.structureOrigin,
                    state.selectedCandidate.deadEndPos,
                    state.selectedCandidate.room7Rotation
                );
            } else {
                List<PlacementCandidate> minimalCollision = state.candidates.stream()
                    .filter(c -> c.collidingRoomCount == 1)
                    .toList();

                if (!minimalCollision.isEmpty()) {
                    state.selectedCandidate = minimalCollision.get(level.random.nextInt(minimalCollision.size()));
                    ChronoDawn.LOGGER.warn(
                        "Phase 2: No collision-free placement found for {}. Using minimal collision placement (1 room). Selected dead_end at {}, rotation {}",
                        state.structureOrigin,
                        state.selectedCandidate.deadEndPos,
                        state.selectedCandidate.room7Rotation
                    );
                } else {
                    ChronoDawn.LOGGER.warn(
                        "Phase 3: No safe maze connection possible for {} (all candidates have 2+ room collisions). Will place boss_room as hidden chamber.",
                        state.structureOrigin
                    );
                    // selectedCandidate remains null - triggers fallback
                }
            }

            // Move to placement phase
            state.phase = ProcessingPhase.PLACING_ROOMS;
            return;
        }

        // Note: We evaluate all candidates immediately for now
        // Could be split further if needed, but evaluation is relatively fast
        state.currentCandidateIndex = state.candidates.size();
    }

    /**
     * Progress room placement phase - places rooms and cleans up.
     */
    private static void progressRoomPlacement(ServerLevel level, StructureProcessingState state) {
        if (state.selectedCandidate == null) {
            // Fallback: independent placement
            ChronoDawn.LOGGER.error(
                "No safe maze connection possible for {}. Placing boss_room as independent hidden chamber.",
                state.structureOrigin
            );

            boolean success = placeBossRoomIndependently(level, state.structureOrigin, state.boundingBox);

            if (success) {
                ChronoDawn.LOGGER.info("Successfully placed boss_room as hidden chamber (fallback) for {}", state.structureOrigin);
            } else {
                ChronoDawn.LOGGER.error("Failed to place boss_room even as hidden chamber for {}", state.structureOrigin);
            }

            // Clean up markers
            int markersRemoved = cleanupCryingObsidianMarkers(level, state.structureOrigin, state.boundingBox, state.initialChunkPos);
            if (markersRemoved > 0) {
                ChronoDawn.LOGGER.info("Removed {} Crying Obsidian markers from structure {} (fallback)", markersRemoved, state.structureOrigin);
            }

            state.phase = ProcessingPhase.COMPLETED;
            return;
        }

        // Normal placement
        BlockPos room7ConnectorPos = placeRoom7WithRotation(
            level,
            state.selectedCandidate.deadEndPos,
            state.selectedCandidate.room7Rotation,
            state.room7Template
        );

        if (room7ConnectorPos == null) {
            ChronoDawn.LOGGER.error(
                "Failed to place room_7 at {} with rotation {} for structure {}",
                state.selectedCandidate.deadEndPos,
                state.selectedCandidate.room7Rotation,
                state.structureOrigin
            );

            // Clean up markers
            for (BlockPos markerPos : state.foundMarkers) {
                BlockState blockState = level.getBlockState(markerPos);
                if (blockState.is(Blocks.CRYING_OBSIDIAN)) {
                    level.setBlock(markerPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }

            state.phase = ProcessingPhase.COMPLETED;
            return;
        }

        // Store rotation
        lastRoom7Rotation = state.selectedCandidate.room7Rotation;

        // Place boss room
        if (placeBossRoom(level, room7ConnectorPos)) {
            ChronoDawn.LOGGER.info(
                "Successfully placed room_7 + boss_room for structure {} (dead_end at {}, room_7 connector at {}, collision count: {})",
                state.structureOrigin,
                state.selectedCandidate.deadEndPos,
                room7ConnectorPos,
                state.selectedCandidate.collidingRoomCount
            );
        } else {
            ChronoDawn.LOGGER.error(
                "Failed to place boss_room (room_7 connector at {}) for structure {}",
                room7ConnectorPos,
                state.structureOrigin
            );
        }

        // Clean up markers
        int markersRemoved = 0;
        for (BlockPos markerPos : state.foundMarkers) {
            BlockState blockState = level.getBlockState(markerPos);
            if (blockState.is(Blocks.CRYING_OBSIDIAN)) {
                level.setBlock(markerPos, Blocks.AIR.defaultBlockState(), 3);
                markersRemoved++;
            }
        }

        if (markersRemoved > 0) {
            ChronoDawn.LOGGER.info("Removed {} Crying Obsidian markers from structure {}", markersRemoved, state.structureOrigin);
        }

        state.phase = ProcessingPhase.COMPLETED;
    }

    /**
     * Find suitable locations for room_7 placement (DEPRECATED - now done incrementally).
     *
     * This method is kept for reference but no longer used in multi-tick processing.
     */
    @Deprecated
    private static List<BlockPos> findDeadEndMarkers_OLD(ServerLevel level, ChunkPos chunkPos, net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox) {
        // This method is replaced by progressMarkerSearch()
        return new ArrayList<>();
    }


    /**
     * Clean up all Crying Obsidian markers in the structure area.
     * This is used during fallback placement to remove all markers.
     *
     * @param level           ServerLevel
     * @param structureOrigin Structure origin position
     * @param boundingBox     Structure bounding box
     * @param chunkPos        Chunk position containing the structure
     * @return Number of markers removed
     */
    private static int cleanupCryingObsidianMarkers(ServerLevel level, BlockPos structureOrigin, BoundingBox boundingBox, ChunkPos chunkPos) {
        int markersRemoved = 0;

        // Get all chunks where this structure exists
        var structureManager = level.structureManager();
        var structureChunks = new HashSet<ChunkPos>();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(PHANTOM_CATACOMBS_ID)) {
                it.unimi.dsi.fastutil.longs.LongSet chunks = entry.getValue();
                for (long chunkLong : chunks) {
                    structureChunks.add(new ChunkPos(chunkLong));
                }
            }
        }

        // Y range from bounding box
        int minY = boundingBox != null ? boundingBox.minY() - 10 : -64;
        int maxY = boundingBox != null ? boundingBox.maxY() + 10 : 320;

        // Scan all structure chunks for Crying Obsidian
        for (ChunkPos structureChunk : structureChunks) {
            for (BlockPos pos : BlockPos.betweenClosed(
                structureChunk.getMinBlockX(), minY, structureChunk.getMinBlockZ(),
                structureChunk.getMaxBlockX(), maxY, structureChunk.getMaxBlockZ()
            )) {
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.CRYING_OBSIDIAN)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    markersRemoved++;
                }
            }
        }

        return markersRemoved;
    }

    /**
     * Clear tracking data for a dimension (useful for dimension unload).
     */
    public static void clearDimension(ResourceLocation dimensionId) {
        processedStructures.remove(dimensionId);
        // Clear processing states for this dimension
        processingStates.entrySet().removeIf(entry -> entry.getValue().dimensionId.equals(dimensionId));
    }

    /**
     * Check for Phantom Catacombs structures and place room_7 + boss_room if needed.
     * This is called every server tick.
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndPlaceRooms(ServerLevel level) {
        ResourceLocation dimensionId = level.dimension().location();

        ChronoDawn.LOGGER.debug("[DimensionCheck] checkAndPlaceRooms called for dimension: {}", dimensionId);

        // Initialize tracking for this dimension if needed
        // Use ConcurrentHashMap.newKeySet() for thread-safe Set
        processedStructures.putIfAbsent(dimensionId, java.util.concurrent.ConcurrentHashMap.newKeySet());
        tickCounters.putIfAbsent(dimensionId, 0);

        // Increment tick counter for this dimension (thread-safe)
        int currentTick = tickCounters.compute(dimensionId, (k, v) -> (v == null ? 0 : v) + 1);

        // Only check every CHECK_INTERVAL ticks for new structures
        if (currentTick < CHECK_INTERVAL) {
            // But always progress existing processing states
            progressAllProcessing(level);
            return;
        }
        tickCounters.put(dimensionId, 0);

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            // Still progress existing processing states
            progressAllProcessing(level);
            return;
        }

        // Check chunks around each player to initialize new processing
        for (var player : level.players()) {
            ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

            // Check chunks in an 8-chunk radius around player
            // With multi-tick processing, this doesn't cause freezing (T309: state machine fix)
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                    // Initialize processing for this chunk (will skip if already processed/processing)
                    processStructure(level, chunkPos);
                }
            }
        }

        // Progress all active processing states
        progressAllProcessing(level);
    }

    /**

    /**
     * Placement candidate data class.
     * Stores evaluation results for a potential boss_room placement.
     */
    private static class PlacementCandidate {
        final BlockPos deadEndPos;
        final Rotation room7Rotation;
        final BlockPos room7PlacementPos;
        final BlockPos connectorWorldPos;
        final BlockPos bossRoomPlacementPos;
        final Rotation bossRoomRotation;
        final int collidingRoomCount;
        final Set<BlockPos> collidingRoomOrigins;

        PlacementCandidate(
            BlockPos deadEndPos,
            Rotation room7Rotation,
            BlockPos room7PlacementPos,
            BlockPos connectorWorldPos,
            BlockPos bossRoomPlacementPos,
            Rotation bossRoomRotation,
            int collidingRoomCount,
            Set<BlockPos> collidingRoomOrigins
        ) {
            this.deadEndPos = deadEndPos;
            this.room7Rotation = room7Rotation;
            this.room7PlacementPos = room7PlacementPos;
            this.connectorWorldPos = connectorWorldPos;
            this.bossRoomPlacementPos = bossRoomPlacementPos;
            this.bossRoomRotation = bossRoomRotation;
            this.collidingRoomCount = collidingRoomCount;
            this.collidingRoomOrigins = collidingRoomOrigins;
        }
    }

    /**
     * Evaluate all possible placements for boss_room.
     * Tests all dead_end markers with all 4 rotations.
     *
     * @param level           ServerLevel
     * @param deadEndMarkers  List of dead_end marker positions
     * @param room7Template   room_7 structure template
     * @param bossRoomTemplate boss_room structure template
     * @return List of placement candidates with collision info
     */
    private static List<PlacementCandidate> evaluateAllPlacements(
        ServerLevel level,
        List<BlockPos> deadEndMarkers,
        StructureTemplate room7Template,
        StructureTemplate bossRoomTemplate
    ) {
        List<PlacementCandidate> candidates = new ArrayList<>();

        // Get relative positions from templates (only once)
        BlockPos room7Marker = findRoom7Marker(room7Template);
        BlockPos room7Connector = findRoom7Connector(room7Template);
        BlockPos bossRoomConnector = findBossRoomConnector(bossRoomTemplate);

        if (room7Marker == null || room7Connector == null || bossRoomConnector == null) {
            ChronoDawn.LOGGER.error("Cannot evaluate placements: missing markers in templates");
            return candidates;
        }

        ChronoDawn.LOGGER.debug(
            "Evaluating placement candidates for {} dead_ends (each with 3 boss_room directions)",
            deadEndMarkers.size()
        );

        // Evaluate all dead_ends with 3 boss_room directions (excluding maze entrance direction)
        for (BlockPos deadEndPos : deadEndMarkers) {
            // Detect actual entrance direction of this dead_end (for maze connection)
            Direction entranceDir = detectEntranceDirection(level, deadEndPos);

            ChronoDawn.LOGGER.debug(
                "Dead_end at {}: detected maze entrance direction {} (will exclude from boss_room placement)",
                deadEndPos,
                entranceDir
            );

            // Try all 4 rotations, but skip the one where boss_room would block maze entrance
            for (Rotation rotation : Rotation.values()) {
                // Calculate boss_room direction for this rotation
                Direction bossRoomDir = getExitDirectionFromRotation(rotation);

                // Skip if boss_room would be placed in maze entrance direction
                if (bossRoomDir == entranceDir) {
                    ChronoDawn.LOGGER.debug(
                        "  Rotation {}: Skipping (boss_room direction {} conflicts with maze entrance {})",
                        rotation,
                        bossRoomDir,
                        entranceDir
                    );
                    continue;
                }

                ChronoDawn.LOGGER.debug(
                    "  Rotation {}: Evaluating (boss_room direction {} is safe)",
                    rotation,
                    bossRoomDir
                );

                // 1. Calculate room_7 placement position (without placing blocks)
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
                BlockPos rotatedMarker = StructureTemplate.calculateRelativePosition(settings, room7Marker);
                BlockPos rotatedConnector = StructureTemplate.calculateRelativePosition(settings, room7Connector);
                BlockPos room7PlacementPos = deadEndPos.subtract(rotatedMarker);
                BlockPos connectorWorldPos = room7PlacementPos.offset(rotatedConnector);

                // 2. Calculate boss_room placement position
                Direction exitDir = getExitDirectionFromRotation(rotation);
                Rotation bossRoomRotation = getBossRoomRotationFromExitDirection(exitDir);
                StructurePlaceSettings bossSettings = new StructurePlaceSettings().setRotation(bossRoomRotation);
                BlockPos rotatedBossConnector = StructureTemplate.calculateRelativePosition(bossSettings, bossRoomConnector);

                BlockPos exitOffset = switch (exitDir) {
                    case EAST -> new BlockPos(1, 0, 0);
                    case WEST -> new BlockPos(-1, 0, 0);
                    case NORTH -> new BlockPos(0, 0, -1);
                    case SOUTH -> new BlockPos(0, 0, 1);
                    default -> BlockPos.ZERO;
                };

                BlockPos bossRoomPlacementPos = connectorWorldPos
                    .subtract(rotatedBossConnector)
                    .offset(exitOffset);

                // 3. Calculate boss_room bounding box
                BoundingBox bossRoomBox = calculateBossRoomBoundingBox(
                    bossRoomPlacementPos,
                    bossRoomTemplate.getSize(),
                    bossRoomRotation
                );

                // 4. Count colliding rooms
                Set<BlockPos> collidingRooms = new HashSet<>();
                int collidingCount = countCollidingRooms(level, bossRoomBox, collidingRooms);

                // 5. Create candidate
                candidates.add(new PlacementCandidate(
                    deadEndPos,
                    rotation,
                    room7PlacementPos,
                    connectorWorldPos,
                    bossRoomPlacementPos,
                    bossRoomRotation,
                    collidingCount,
                    collidingRooms
                ));
            }
        }

        ChronoDawn.LOGGER.debug(
            "Evaluation complete: {} candidates generated",
            candidates.size()
        );

        return candidates;
    }

    /**
     * Count maze rooms that collide with boss_room placement area.
     *
     * Performance: Now runs asynchronously (T309), so we can scan every block
     * for accurate collision detection without freezing the main thread.
     *
     * NOTE: This method is called from async thread - only read world state, don't modify!
     *
     * @param level           ServerLevel
     * @param bossRoomBox     Boss_room bounding box
     * @param outCollidingRooms Output set to store colliding room origins
     * @return Number of colliding rooms
     */
    private static int countCollidingRooms(ServerLevel level, BoundingBox bossRoomBox, Set<BlockPos> outCollidingRooms) {
        Set<BlockPos> collidingRooms = new HashSet<>();

        // Scan boss_room area for maze structure blocks (T309: now async, so full scan is OK)
        for (BlockPos pos : BlockPos.betweenClosed(
            bossRoomBox.minX(), bossRoomBox.minY(), bossRoomBox.minZ(),
            bossRoomBox.maxX(), bossRoomBox.maxY(), bossRoomBox.maxZ()
        )) {
            BlockState state = level.getBlockState(pos);

            // Check if this is a maze structure block
            if (state.is(Blocks.DEEPSLATE_BRICKS) ||
                state.is(Blocks.DEEPSLATE_BRICK_STAIRS) ||
                state.is(Blocks.DEEPSLATE_BRICK_SLAB) ||
                state.is(Blocks.DEEPSLATE_BRICK_WALL) ||
                state.is(Blocks.CRYING_OBSIDIAN) ||
                state.is(Blocks.CHEST)) {

                // Estimate room origin (rooms are 7x7, align to grid)
                BlockPos roomOrigin = new BlockPos(
                    Math.floorDiv(pos.getX(), 7) * 7,
                    pos.getY(),
                    Math.floorDiv(pos.getZ(), 7) * 7
                );
                collidingRooms.add(roomOrigin);
            }
        }

        if (outCollidingRooms != null) {
            outCollidingRooms.addAll(collidingRooms);
        }

        return collidingRooms.size();
    }

    /**
     * Calculate boss_room bounding box considering rotation.
     *
     * @param placementPos  Boss_room placement position
     * @param templateSize  Template size (X, Y, Z)
     * @param rotation      Boss_room rotation
     * @return Bounding box
     */
    private static BoundingBox calculateBossRoomBoundingBox(
        BlockPos placementPos,
        net.minecraft.core.Vec3i templateSize,
        Rotation rotation
    ) {
        int sizeX = templateSize.getX();
        int sizeY = templateSize.getY();
        int sizeZ = templateSize.getZ();

        // Calculate rotated size
        int rotatedSizeX, rotatedSizeZ;
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            rotatedSizeX = sizeZ;
            rotatedSizeZ = sizeX;
        } else {
            rotatedSizeX = sizeX;
            rotatedSizeZ = sizeZ;
        }

        return BoundingBox.fromCorners(
            placementPos,
            placementPos.offset(rotatedSizeX - 1, sizeY - 1, rotatedSizeZ - 1)
        );
    }

    /**
     * Place boss_room independently (hidden chamber fallback).
     * Used when no safe connection to maze is possible.
     *
     * @param level            ServerLevel
     * @param structureOrigin  Structure origin position
     * @param boundingBox      Structure bounding box
     * @return true if placement succeeded
     */
    private static boolean placeBossRoomIndependently(
        ServerLevel level,
        BlockPos structureOrigin,
        BoundingBox boundingBox
    ) {
        // Calculate hidden chamber position (offset from structure center)
        BlockPos hiddenPos = calculateHiddenChamberPosition(structureOrigin, boundingBox);

        ChronoDawn.LOGGER.warn(
            "Placing boss_room as independent hidden chamber at {} (no safe maze connection possible)",
            hiddenPos
        );

        // Load boss_room template
        var structureManager = level.getStructureManager();
        var templateOptional = structureManager.get(BOSS_ROOM_TEMPLATE);

        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Boss_room template {} not found", BOSS_ROOM_TEMPLATE);
            return false;
        }

        StructureTemplate template = templateOptional.get();

        // Load processor list for waterlogging prevention
        ResourceLocation processorListId = ResourceLocation.fromNamespaceAndPath(
            ChronoDawn.MOD_ID,
            "convert_decorative_water"
        );

        // Place boss_room without rotation (default orientation)
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(Rotation.NONE)
            .setIgnoreEntities(false);

        // Add processors from registry
        try {
            var processorListRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.PROCESSOR_LIST);
            var processorList = processorListRegistry.get(processorListId);

            if (processorList != null) {
                ChronoDawn.LOGGER.debug("Found processor list {}: {} processors (hidden chamber)", processorListId, processorList.list().size());
                for (var processor : processorList.list()) {
                    settings.addProcessor(processor);
                }
                ChronoDawn.LOGGER.debug("Successfully applied {} structure processors to hidden chamber placement", processorList.list().size());
            } else {
                ChronoDawn.LOGGER.error("Processor list {} not found for hidden chamber", processorListId);
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to load processor list for hidden chamber: {}", e.getMessage(), e);
        }

        template.placeInWorld(level, hiddenPos, hiddenPos, settings, level.random, 2);

        // Remove water blocks after placement (in case water flowed in from surroundings)
        net.minecraft.core.Vec3i templateSize = template.getSize();
        int sizeX = templateSize.getX();
        int sizeY = templateSize.getY();
        int sizeZ = templateSize.getZ();

        int waterRemovedAfter = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            hiddenPos.offset(-1, -1, -1),
            hiddenPos.offset(sizeX + 1, sizeY + 1, sizeZ + 1)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.getFluidState().isSource() && state.is(Blocks.WATER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                waterRemovedAfter++;
            }
        }

        if (waterRemovedAfter > 0) {
            ChronoDawn.LOGGER.debug("Removed {} water blocks after hidden chamber placement", waterRemovedAfter);
        }

        // Remove waterlogging after placement
        int waterloggedRemoved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            hiddenPos,
            hiddenPos.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                boolean waterlogged = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
                if (waterlogged) {
                    level.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, false), 2);
                    waterloggedRemoved++;
                }
            }
        }

        if (waterloggedRemoved > 0) {
            ChronoDawn.LOGGER.debug("Removed {} waterlogged blocks after hidden chamber placement", waterloggedRemoved);
        }

        ChronoDawn.LOGGER.info(
            "Successfully placed boss_room as hidden chamber at {}",
            hiddenPos
        );

        // Register boss_room for Temporal Phantom spawning
        // Hidden chamber uses ROTATION.NONE, so center is straightforward
        // boss_room is 21x21x9, center is at (10, 4, 10) relative to placement position
        BlockPos bossRoomCenter = hiddenPos.offset(10, 4, 10);

        ChronoDawn.LOGGER.debug(
            "Registering hidden chamber for Temporal Phantom spawning: placement at {}, center at {}",
            hiddenPos,
            bossRoomCenter
        );

        TemporalPhantomSpawner.registerBossRoom(level, bossRoomCenter);

        return true;
    }

    /**
     * Calculate position for hidden chamber placement.
     * Places boss_room offset from structure center in a diagonal direction.
     *
     * @param structureOrigin Structure origin
     * @param boundingBox     Structure bounding box (can be null)
     * @return Hidden chamber position
     */
    private static BlockPos calculateHiddenChamberPosition(BlockPos structureOrigin, BoundingBox boundingBox) {
        if (boundingBox == null) {
            // Fallback: use structure origin with default offset
            ChronoDawn.LOGGER.warn("BoundingBox is null in calculateHiddenChamberPosition, using structure origin as fallback");
            return structureOrigin.offset(60, -10, 60);
        }

        // Calculate structure center
        int centerX = (boundingBox.minX() + boundingBox.maxX()) / 2;
        int centerZ = (boundingBox.minZ() + boundingBox.maxZ()) / 2;
        int baseY = boundingBox.minY();

        // Offset diagonally (northeast) from center
        // Distance: 60 blocks (far enough to avoid maze overlap)
        return new BlockPos(centerX + 60, baseY - 10, centerZ + 60);
    }

    /**
     * Register event handlers for boss_room placement.
     */
    public static void register() {
        TickEvent.SERVER_POST.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                checkAndPlaceRooms(level);
            }
        });

        ChronoDawn.LOGGER.info("Registered PhantomCatacombsBossRoomPlacer");
    }
}
