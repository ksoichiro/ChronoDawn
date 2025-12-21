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
    private static final Map<ResourceLocation, Set<BlockPos>> processedStructures = new HashMap<>();

    // Check interval (in ticks) - check every 30 seconds to allow structure to fully generate
    private static final int CHECK_INTERVAL = 600;
    private static final Map<ResourceLocation, Integer> tickCounters = new HashMap<>();

    // Store the rotation of last placed room_7 (for boss_room orientation)
    private static Rotation lastRoom7Rotation = Rotation.NONE;

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
     * @param level           The ServerLevel
     * @param structureOrigin The structure's origin position
     * @param boundingBox     The structure's bounding box (null to use default range)
     * @return List of potential room_7 placement positions
     */
    private static List<BlockPos> findDeadEndMarkers(ServerLevel level, BlockPos structureOrigin, net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox) {
        List<BlockPos> markers = new ArrayList<>();

        // Search in a large area around the structure origin
        // Phantom Catacombs with size=12 can span very large areas
        int searchRadius = 150; // Search within 150 blocks from structure origin

        // Y range: use structure's actual bounding box with some padding
        // This allows the search to work regardless of terrain height (plains, mountains, etc.)
        // Fallback range covers all possible build heights (-64 to 320 in 1.18+)
        int minY = boundingBox != null ? boundingBox.minY() - 10 : -64;
        int maxY = boundingBox != null ? boundingBox.maxY() + 10 : 320;

        ChronoDawn.LOGGER.info(
            "Searching for Crying Obsidian markers around structure origin {} (X/Z radius: {}, Y: {} to {}){}",
            structureOrigin,
            searchRadius,
            minY,
            maxY,
            boundingBox != null ? " [based on structure bounding box: " + boundingBox + "]" : " [using default range]"
        );

        // Search for Crying Obsidian markers (dead-end room markers)
        for (BlockPos pos : BlockPos.betweenClosed(
            structureOrigin.getX() - searchRadius, minY, structureOrigin.getZ() - searchRadius,
            structureOrigin.getX() + searchRadius, maxY, structureOrigin.getZ() + searchRadius
        )) {
            BlockState blockState = level.getBlockState(pos);

            // Check if this is a Crying Obsidian (dead-end room marker)
            if (blockState.is(Blocks.CRYING_OBSIDIAN)) {
                markers.add(pos.immutable());
            }
        }

        if (!markers.isEmpty()) {
            ChronoDawn.LOGGER.info("Found {} Crying Obsidian markers at positions:", markers.size());
            for (int i = 0; i < Math.min(markers.size(), 5); i++) {
                BlockPos marker = markers.get(i);
                ChronoDawn.LOGGER.info("  Marker {}: {} (distance from origin: {})",
                    i + 1,
                    marker,
                    Math.sqrt(marker.distSqr(structureOrigin))
                );
            }
            if (markers.size() > 5) {
                ChronoDawn.LOGGER.info("  ... and {} more", markers.size() - 5);
            }
        } else {
            ChronoDawn.LOGGER.warn(
                "No Crying Obsidian markers found around origin {} (radius: {}). Structure may not be fully loaded yet.",
                structureOrigin,
                searchRadius
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

        ChronoDawn.LOGGER.info(
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

        ChronoDawn.LOGGER.info(
            "Room_7 marker before rotation: {}, connector before rotation: {}",
            room7Marker,
            room7Connector
        );

        // Calculate placement offset with rotation
        StructurePlaceSettings tempSettings = new StructurePlaceSettings().setRotation(rotation);
        BlockPos rotatedMarker = StructureTemplate.calculateRelativePosition(tempSettings, room7Marker);
        BlockPos rotatedConnectorRelative = StructureTemplate.calculateRelativePosition(tempSettings, room7Connector);

        ChronoDawn.LOGGER.info(
            "Room_7 after rotation {}: marker {} → {}, connector {} → {}",
            rotation,
            room7Marker,
            rotatedMarker,
            room7Connector,
            rotatedConnectorRelative
        );

        BlockPos placementPos = deadEndPos.subtract(rotatedMarker);

        ChronoDawn.LOGGER.info(
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

        ChronoDawn.LOGGER.info(
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
            ChronoDawn.LOGGER.info("Removed {} water blocks before room_7 placement", waterRemoved);
        }

        // Create placement settings without rotation
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Remove the Crying Obsidian marker before placing room_7
        level.setBlock(deadEndPos, Blocks.AIR.defaultBlockState(), 3);
        ChronoDawn.LOGGER.info("Removed Crying Obsidian marker at {}", deadEndPos);

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
            ChronoDawn.LOGGER.info("Removed {} waterlogged blocks after room_7 placement", waterloggedRemoved);
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

        ChronoDawn.LOGGER.info(
            "Placing room_7 with specified rotation {}: marker before rotation: {}, connector before rotation: {}",
            rotation,
            room7Marker,
            room7Connector
        );

        // Calculate placement offset with rotation
        StructurePlaceSettings tempSettings = new StructurePlaceSettings().setRotation(rotation);
        BlockPos rotatedMarker = StructureTemplate.calculateRelativePosition(tempSettings, room7Marker);
        BlockPos rotatedConnectorRelative = StructureTemplate.calculateRelativePosition(tempSettings, room7Connector);

        ChronoDawn.LOGGER.info(
            "Room_7 after rotation {}: marker {} → {}, connector {} → {}",
            rotation,
            room7Marker,
            rotatedMarker,
            room7Connector,
            rotatedConnectorRelative
        );

        BlockPos placementPos = deadEndPos.subtract(rotatedMarker);

        ChronoDawn.LOGGER.info(
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

        ChronoDawn.LOGGER.info(
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
            ChronoDawn.LOGGER.info("Removed {} water blocks before room_7 placement", waterRemoved);
        }

        // Create placement settings
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Remove the Crying Obsidian marker before placing room_7
        level.setBlock(deadEndPos, Blocks.AIR.defaultBlockState(), 3);
        ChronoDawn.LOGGER.info("Removed Crying Obsidian marker at {}", deadEndPos);

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
            ChronoDawn.LOGGER.info("Removed {} waterlogged blocks after room_7 placement", waterloggedRemoved);
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

        ChronoDawn.LOGGER.info("Detecting exit direction from connector at {}", connectorPos);

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

            ChronoDawn.LOGGER.info("  Direction {}: {} air/non-solid blocks", dir, airBlocks);

            if (airBlocks > maxAirBlocks) {
                maxAirBlocks = airBlocks;
                bestDir = dir;
            }
        }

        ChronoDawn.LOGGER.info(
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

        ChronoDawn.LOGGER.info(
            "Boss_room connector before rotation: {}, room_7 rotation: {}, exit direction: {}, boss_room rotation: {}",
            bossRoomConnector,
            lastRoom7Rotation,
            exitDir,
            rotation
        );

        // Calculate placement offset with rotation
        StructurePlaceSettings tempSettings = new StructurePlaceSettings().setRotation(rotation);
        BlockPos rotatedConnector = StructureTemplate.calculateRelativePosition(tempSettings, bossRoomConnector);

        ChronoDawn.LOGGER.info(
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

        ChronoDawn.LOGGER.info(
            "Boss_room placement calculation: connectorPos {} - rotatedConnector {} = basePlacementPos {} + exitOffset {} = placementPos {}",
            connectorPos,
            rotatedConnector,
            basePlacementPos,
            exitOffset,
            placementPos
        );

        ChronoDawn.LOGGER.info(
            "Verification: boss_room Amethyst Block will be at {} (should match room_7 connector at {})",
            bossRoomConnectorWorldPos,
            connectorPos
        );

        // Get template size to calculate bounding box
        net.minecraft.core.Vec3i templateSize = template.getSize();
        int sizeX = templateSize.getX();
        int sizeY = templateSize.getY();
        int sizeZ = templateSize.getZ();

        ChronoDawn.LOGGER.info(
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
            ChronoDawn.LOGGER.info("Removed {} water blocks before boss_room placement", waterRemoved);
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
                ChronoDawn.LOGGER.info("Found processor list {}: {} processors", processorListId, processorList.list().size());
                for (var processor : processorList.list()) {
                    settings.addProcessor(processor);
                    ChronoDawn.LOGGER.info("  Added processor: {}", processor.getClass().getSimpleName());
                }
                ChronoDawn.LOGGER.info("Successfully applied {} structure processors to boss_room placement", processorList.list().size());
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
            ChronoDawn.LOGGER.info("Removed {} water blocks after boss_room placement (flowed in from surroundings)", waterRemovedAfter);
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
            ChronoDawn.LOGGER.info("Removed {} waterlogged blocks after boss_room placement", waterloggedRemoved);
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

        ChronoDawn.LOGGER.info(
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

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(PHANTOM_CATACOMBS_ID)) {
                it.unimi.dsi.fastutil.longs.LongSet chunks = entry.getValue();

                // Try to get bounding box from StructureStart
                for (long chunkLong : chunks) {
                    ChunkPos structureChunk = new ChunkPos(chunkLong);
                    var startForStructure = structureManager.getStructureAt(structureChunk.getWorldPosition(), structure);
                    if (startForStructure != null && startForStructure.isValid()) {
                        net.minecraft.world.level.levelgen.structure.BoundingBox box = startForStructure.getBoundingBox();
                        if (box != null) {
                            ChronoDawn.LOGGER.info("Successfully retrieved bounding box from StructureStart: {}", box);
                            return box;
                        }
                    }
                }

                // Fallback: scan chunks for actual structure blocks to find Y bounds
                ChronoDawn.LOGGER.warn("Could not get bounding box from StructureStart, scanning chunks for structure blocks...");
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;
                int minX = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int minZ = Integer.MAX_VALUE;
                int maxZ = Integer.MIN_VALUE;
                boolean foundAny = false;

                for (long chunkLong : chunks) {
                    ChunkPos structureChunk = new ChunkPos(chunkLong);
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

                if (foundAny) {
                    var scannedBox = new net.minecraft.world.level.levelgen.structure.BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                    ChronoDawn.LOGGER.info("Created bounding box from chunk scan: {}", scannedBox);
                    return scannedBox;
                }
            }
        }
        return null;
    }

    /**
     * Process a Phantom Catacombs structure and place room_7 + boss_room if not already placed.
     *
     * Strategy:
     * 1. Find dead-end rooms (marked with Crying Obsidian)
     * 2. Replace the first dead-end room with room_7
     * 3. Connect boss_room to room_7's exit
     *
     * This should be called after structure generation is complete.
     * Uses structure origin for tracking to ensure processing only once per structure.
     *
     * @param level     The ServerLevel
     * @param chunkPos  The chunk position containing the structure
     */
    public static void processStructure(ServerLevel level, ChunkPos chunkPos) {
        ResourceLocation dimensionId = level.dimension().location();

        // Initialize tracking for this dimension if needed
        processedStructures.putIfAbsent(dimensionId, new HashSet<>());
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

        // Skip if we've already processed this structure
        if (dimensionProcessed.contains(structureOrigin)) {
            return;
        }

        // Get structure bounding box for accurate Y range search
        net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox = getStructureBoundingBox(level, chunkPos);

        ChronoDawn.LOGGER.info(
            "Found Phantom Catacombs structure at chunk {} (origin: {}) in dimension {}",
            chunkPos,
            structureOrigin,
            dimensionId
        );

        // Find all dead-end markers in the structure (search from structure origin with bounding box)
        List<BlockPos> deadEndPositions = findDeadEndMarkers(level, structureOrigin, boundingBox);

        if (deadEndPositions.isEmpty()) {
            // No dead-end rooms found yet - DON'T mark as processed
            // This allows retry on next check (structure might still be generating/loading)
            ChronoDawn.LOGGER.info(
                "No dead-end rooms found yet in Phantom Catacombs at chunk {} (dimension: {}). Will retry later.",
                chunkPos,
                dimensionId
            );
            return;
        }

        // Require at least 3 dead-end rooms before processing
        // This ensures the maze has generated sufficiently
        if (deadEndPositions.size() < 3) {
            ChronoDawn.LOGGER.info(
                "Only {} dead-end rooms found in Phantom Catacombs (minimum: 3). Will retry later.",
                deadEndPositions.size()
            );
            return;
        }

        ChronoDawn.LOGGER.info(
            "Found {} dead-end rooms in Phantom Catacombs at chunk {}",
            deadEndPositions.size(),
            chunkPos
        );

        // Load templates for evaluation
        var structureManager = level.getStructureManager();
        var room7TemplateOpt = structureManager.get(ROOM_7_TEMPLATE);
        var bossRoomTemplateOpt = structureManager.get(BOSS_ROOM_TEMPLATE);

        if (room7TemplateOpt.isEmpty() || bossRoomTemplateOpt.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load templates for boss_room placement");
            dimensionProcessed.add(structureOrigin);
            return;
        }

        StructureTemplate room7Template = room7TemplateOpt.get();
        StructureTemplate bossRoomTemplate = bossRoomTemplateOpt.get();

        // Evaluate all possible placements (all dead_ends × 4 rotations)
        List<PlacementCandidate> candidates = evaluateAllPlacements(
            level,
            deadEndPositions,
            room7Template,
            bossRoomTemplate
        );

        if (candidates.isEmpty()) {
            ChronoDawn.LOGGER.error("No placement candidates generated (template error?)");
            dimensionProcessed.add(structureOrigin);
            return;
        }

        // Phase 1: Find collision-free candidates
        List<PlacementCandidate> collisionFree = candidates.stream()
            .filter(c -> c.collidingRoomCount == 0)
            .toList();

        PlacementCandidate selected = null;

        if (!collisionFree.isEmpty()) {
            // ✅ Ideal: collision-free placement
            selected = collisionFree.get(level.random.nextInt(collisionFree.size()));
            ChronoDawn.LOGGER.info(
                "Phase 1: Found {} collision-free placements. Selected dead_end at {}, rotation {}",
                collisionFree.size(),
                selected.deadEndPos,
                selected.room7Rotation
            );
        } else {
            // Phase 2: Find minimal collision candidates (1 room only)
            List<PlacementCandidate> minimalCollision = candidates.stream()
                .filter(c -> c.collidingRoomCount == 1)
                .toList();

            if (!minimalCollision.isEmpty()) {
                // ⚠️ Acceptable: 1 room collision
                selected = minimalCollision.get(level.random.nextInt(minimalCollision.size()));
                ChronoDawn.LOGGER.warn(
                    "Phase 2: No collision-free placement found. Using minimal collision placement (1 room). Selected dead_end at {}, rotation {}",
                    selected.deadEndPos,
                    selected.room7Rotation
                );
                ChronoDawn.LOGGER.warn("Colliding room origins: {}", selected.collidingRoomOrigins);
            } else {
                // Phase 3: Fallback - independent placement
                ChronoDawn.LOGGER.error(
                    "Phase 3: No safe maze connection possible (all candidates have 2+ room collisions). Placing boss_room as independent hidden chamber."
                );

                boolean success = placeBossRoomIndependently(level, structureOrigin, boundingBox);

                if (success) {
                    ChronoDawn.LOGGER.info(
                        "Successfully placed boss_room as hidden chamber (fallback)"
                    );
                } else {
                    ChronoDawn.LOGGER.error("Failed to place boss_room even as hidden chamber");
                }

                // Clean up: Remove all Crying Obsidian markers from dead_ends
                int markersRemoved = 0;
                for (BlockPos markerPos : deadEndPositions) {
                    BlockState state = level.getBlockState(markerPos);
                    if (state.is(Blocks.CRYING_OBSIDIAN)) {
                        level.setBlock(markerPos, Blocks.AIR.defaultBlockState(), 3);
                        markersRemoved++;
                    }
                }

                if (markersRemoved > 0) {
                    ChronoDawn.LOGGER.info(
                        "Removed {} Crying Obsidian markers from dead_ends (hidden chamber fallback)",
                        markersRemoved
                    );
                }

                dimensionProcessed.add(structureOrigin);
                return;
            }
        }

        // Place room_7 at selected location with selected rotation
        BlockPos room7ConnectorPos = placeRoom7WithRotation(
            level,
            selected.deadEndPos,
            selected.room7Rotation,
            room7Template
        );

        if (room7ConnectorPos == null) {
            ChronoDawn.LOGGER.error(
                "Failed to place room_7 at {} with rotation {}",
                selected.deadEndPos,
                selected.room7Rotation
            );

            // Clean up: Remove all Crying Obsidian markers from dead_ends
            int markersRemoved = 0;
            for (BlockPos markerPos : deadEndPositions) {
                BlockState state = level.getBlockState(markerPos);
                if (state.is(Blocks.CRYING_OBSIDIAN)) {
                    level.setBlock(markerPos, Blocks.AIR.defaultBlockState(), 3);
                    markersRemoved++;
                }
            }

            if (markersRemoved > 0) {
                ChronoDawn.LOGGER.info(
                    "Removed {} Crying Obsidian markers from dead_ends (room_7 placement failed)",
                    markersRemoved
                );
            }

            dimensionProcessed.add(structureOrigin);
            return;
        }

        // Store rotation for boss_room placement
        lastRoom7Rotation = selected.room7Rotation;

        // Place boss_room connected to room_7
        if (placeBossRoom(level, room7ConnectorPos)) {
            ChronoDawn.LOGGER.info(
                "Successfully placed room_7 + boss_room in Phantom Catacombs (dead_end at {}, room_7 connector at {}, collision count: {})",
                selected.deadEndPos,
                room7ConnectorPos,
                selected.collidingRoomCount
            );
        } else {
            ChronoDawn.LOGGER.error(
                "Failed to place boss_room (room_7 connector at {})",
                room7ConnectorPos
            );
        }

        // Clean up: Remove all remaining Crying Obsidian markers from dead_ends
        int markersRemoved = 0;
        for (BlockPos markerPos : deadEndPositions) {
            BlockState state = level.getBlockState(markerPos);
            if (state.is(Blocks.CRYING_OBSIDIAN)) {
                level.setBlock(markerPos, Blocks.AIR.defaultBlockState(), 3);
                markersRemoved++;
            }
        }

        if (markersRemoved > 0) {
            ChronoDawn.LOGGER.info(
                "Removed {} Crying Obsidian markers from remaining dead_ends",
                markersRemoved
            );
        }

        // Mark this structure as processed
        dimensionProcessed.add(structureOrigin);
    }

    /**
     * Clear tracking data for a dimension (useful for dimension unload).
     */
    public static void clearDimension(ResourceLocation dimensionId) {
        processedStructures.remove(dimensionId);
    }

    /**
     * Check for Phantom Catacombs structures and place room_7 + boss_room if needed.
     * This is called every server tick.
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndPlaceRooms(ServerLevel level) {
        ResourceLocation dimensionId = level.dimension().location();

        // Initialize tracking for this dimension if needed
        processedStructures.putIfAbsent(dimensionId, new HashSet<>());
        tickCounters.putIfAbsent(dimensionId, 0);

        // Increment tick counter for this dimension
        int currentTick = tickCounters.get(dimensionId);
        currentTick++;

        // Only check every CHECK_INTERVAL ticks
        if (currentTick < CHECK_INTERVAL) {
            tickCounters.put(dimensionId, currentTick);
            return;
        }
        tickCounters.put(dimensionId, 0);

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Check chunks around each player
        for (var player : level.players()) {
            ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

            // Check chunks in a 8-chunk radius around player
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                    // Process this chunk (will skip if already processed)
                    processStructure(level, chunkPos);
                }
            }
        }
    }

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

        ChronoDawn.LOGGER.info(
            "Evaluating placement candidates for {} dead_ends (each with 3 boss_room directions)",
            deadEndMarkers.size()
        );

        // Evaluate all dead_ends with 3 boss_room directions (excluding maze entrance direction)
        for (BlockPos deadEndPos : deadEndMarkers) {
            // Detect actual entrance direction of this dead_end (for maze connection)
            Direction entranceDir = detectEntranceDirection(level, deadEndPos);

            ChronoDawn.LOGGER.info(
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
                    ChronoDawn.LOGGER.info(
                        "  Rotation {}: Skipping (boss_room direction {} conflicts with maze entrance {})",
                        rotation,
                        bossRoomDir,
                        entranceDir
                    );
                    continue;
                }

                ChronoDawn.LOGGER.info(
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

        ChronoDawn.LOGGER.info(
            "Evaluation complete: {} candidates generated",
            candidates.size()
        );

        return candidates;
    }

    /**
     * Count maze rooms that collide with boss_room placement area.
     *
     * @param level           ServerLevel
     * @param bossRoomBox     Boss_room bounding box
     * @param outCollidingRooms Output set to store colliding room origins
     * @return Number of colliding rooms
     */
    private static int countCollidingRooms(ServerLevel level, BoundingBox bossRoomBox, Set<BlockPos> outCollidingRooms) {
        Set<BlockPos> collidingRooms = new HashSet<>();

        // Scan boss_room area for maze structure blocks
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
                ChronoDawn.LOGGER.info("Found processor list {}: {} processors (hidden chamber)", processorListId, processorList.list().size());
                for (var processor : processorList.list()) {
                    settings.addProcessor(processor);
                }
                ChronoDawn.LOGGER.info("Successfully applied {} structure processors to hidden chamber placement", processorList.list().size());
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
            ChronoDawn.LOGGER.info("Removed {} water blocks after hidden chamber placement", waterRemovedAfter);
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
            ChronoDawn.LOGGER.info("Removed {} waterlogged blocks after hidden chamber placement", waterloggedRemoved);
        }

        ChronoDawn.LOGGER.info(
            "Successfully placed boss_room as hidden chamber at {}",
            hiddenPos
        );

        // Register boss_room for Temporal Phantom spawning
        // Hidden chamber uses ROTATION.NONE, so center is straightforward
        // boss_room is 21x21x9, center is at (10, 4, 10) relative to placement position
        BlockPos bossRoomCenter = hiddenPos.offset(10, 4, 10);

        ChronoDawn.LOGGER.info(
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
     * @param boundingBox     Structure bounding box
     * @return Hidden chamber position
     */
    private static BlockPos calculateHiddenChamberPosition(BlockPos structureOrigin, BoundingBox boundingBox) {
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
