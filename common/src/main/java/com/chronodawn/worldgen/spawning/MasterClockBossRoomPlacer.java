package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModDimensions;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Master Clock Boss Room Placer
 *
 * Manages placement of boss_room in Master Clock structure at fixed Y=-50.
 *
 * Placement Strategy:
 * - Location: Fixed Y=-50 (always underground regardless of terrain)
 * - Detection: Find Amethyst Block marker at corridor end
 * - Connection: Generate connecting passage from corridor to Y=-50
 * - Max per structure: 1
 *
 * This ensures boss_room is never exposed on surface, even on mountains.
 *
 * Implementation inspired by PhantomCatacombsBossRoomPlacer.
 */
public class MasterClockBossRoomPlacer {
    private static final ResourceLocation MASTER_CLOCK_ID = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "master_clock"
    );

    private static final ResourceLocation BOSS_ROOM_TEMPLATE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "master_clock_boss_room"
    );

    private static final ResourceLocation STAIRS_TEMPLATE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "master_clock_stairs"
    );

    private static final ResourceLocation STAIRS_BOTTOM_TEMPLATE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "master_clock_stairs_bottom"
    );

    private static final ResourceLocation CORRIDOR_TEMPLATE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "master_clock_corridor"
    );

    // Fixed Y coordinate for boss_room (always underground)
    private static final int BOSS_ROOM_Y = -50;

    // Target Y coordinate for corridor placement (above boss room)
    private static final int CORRIDOR_Y = -30;

    // Track structure positions where we've already placed boss_room (per dimension)
    // Thread-safe: ConcurrentHashMap prevents race conditions in multiplayer
    private static final Map<ResourceLocation, Set<BlockPos>> processedStructures = new ConcurrentHashMap<>();

    // Check interval (in ticks) - check every 30 seconds
    private static final int CHECK_INTERVAL = 600;
    // Thread-safe: ConcurrentHashMap prevents race conditions in multiplayer
    private static final Map<ResourceLocation, Integer> tickCounters = new ConcurrentHashMap<>();

    /**
     * Check if a chunk contains a Master Clock structure.
     */
    private static boolean hasMasterClock(ServerLevel level, ChunkPos chunkPos) {
        var structureManager = level.structureManager();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(MASTER_CLOCK_ID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the structure's origin position for tracking.
     */
    private static BlockPos getStructureOrigin(ServerLevel level, ChunkPos chunkPos) {
        var structureManager = level.structureManager();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            net.minecraft.world.level.levelgen.structure.Structure structure = entry.getKey();
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(MASTER_CLOCK_ID)) {
                var structureStart = entry.getValue();
                if (!structureStart.isEmpty()) {
                    long firstChunk = structureStart.iterator().nextLong();
                    return new ChunkPos(firstChunk).getWorldPosition();
                }
            }
        }
        return null;
    }

    /**
     * Find Surface end Dropper marker (for direction).
     */
    private static BlockPos findSurfaceDropperMarker(ServerLevel level, BlockPos structureOrigin) {
        int searchRadius = 50;
        int maxY = 150;
        int minY = -10;

        ChronoDawn.LOGGER.info(
            "Searching for Surface Dropper marker around {}",
            structureOrigin
        );

        // Search for Dropper (direction marker)
        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(
                        structureOrigin.getX() + x,
                        y,
                        structureOrigin.getZ() + z
                    );
                    BlockState state = level.getBlockState(checkPos);

                    // Check if this is a Dropper (direction marker)
                    if (state.is(Blocks.DROPPER)) {
                        ChronoDawn.LOGGER.info("Found Surface Dropper marker at {}", checkPos);
                        return checkPos;
                    }
                }
            }
        }

        ChronoDawn.LOGGER.warn("No Surface Dropper marker found near {}", structureOrigin);
        return null;
    }

    /**
     * Find Surface Jigsaw Block (connection point for stairs).
     */
    private static BlockPos findSurfaceJigsawBlock(ServerLevel level, BlockPos structureOrigin) {
        int searchRadius = 50;
        int maxY = 150;
        int minY = -10;

        ChronoDawn.LOGGER.info(
            "Searching for Surface Jigsaw Block around {}",
            structureOrigin
        );

        // Search for Jigsaw Block (connection point)
        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(
                        structureOrigin.getX() + x,
                        y,
                        structureOrigin.getZ() + z
                    );
                    BlockState state = level.getBlockState(checkPos);

                    // Check if this is a Jigsaw Block
                    if (state.is(Blocks.JIGSAW)) {
                        BlockEntity blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof JigsawBlockEntity) {
                            ChronoDawn.LOGGER.info("Found Surface Jigsaw Block at {}", checkPos);
                            return checkPos;
                        }
                    }
                }
            }
        }

        ChronoDawn.LOGGER.warn("No Surface Jigsaw Block found near {}", structureOrigin);
        return null;
    }

    /**
     * Find Jigsaw Block position within a structure template.
     * Places template temporarily at very high Y to search for Jigsaw Block.
     * Returns the block position offset within the template.
     */
    private static BlockPos findJigsawInTemplate(StructureTemplate template, ServerLevel level, Rotation rotation) {
        // Place template temporarily at high Y to search for Jigsaw Block
        BlockPos tempPos = new BlockPos(0, 300, 0);

        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(true);

        template.placeInWorld(level, tempPos, tempPos, settings, level.random, 2);

        // Search for Jigsaw Block within the placed template
        net.minecraft.core.Vec3i size = template.getSize();

        BlockPos jigsawOffset = null;
        int blocksChecked = 0;
        int jigsawBlocksFound = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
            tempPos,
            tempPos.offset(size)
        )) {
            BlockState state = level.getBlockState(pos);
            blocksChecked++;

            if (state.is(Blocks.JIGSAW)) {
                jigsawBlocksFound++;
                jigsawOffset = pos.subtract(tempPos);
                break;
            }
        }

        // Clean up temporary placement
        for (BlockPos pos : BlockPos.betweenClosed(
            tempPos,
            tempPos.offset(size)
        )) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }

        return jigsawOffset;
    }

    /**
     * Get rotation from Dropper facing direction (surface orientation).
     * Adjusted by +90° to match surface orientation for stairs.
     */
    private static Rotation getRotationFromDirection(Direction direction) {
        return switch (direction) {
            case SOUTH -> Rotation.CLOCKWISE_90;          // +90° from original
            case WEST -> Rotation.CLOCKWISE_180;          // +90° from original
            case NORTH -> Rotation.COUNTERCLOCKWISE_90;   // +90° from original
            case EAST -> Rotation.NONE;                   // +90° from original
            default -> Rotation.NONE;
        };
    }

    /**
     * Get rotation from Jigsaw Block facing direction.
     * Used for corridor placement to match Jigsaw connection.
     * Adjusted by +90° (clockwise) to align with stairs_bottom connection.
     */
    private static Rotation getRotationFromJigsawFacing(Direction facing) {
        return switch (facing) {
            case SOUTH -> Rotation.CLOCKWISE_90;          // +90° adjustment
            case WEST -> Rotation.CLOCKWISE_180;          // +90° adjustment
            case NORTH -> Rotation.COUNTERCLOCKWISE_90;   // +90° adjustment
            case EAST -> Rotation.NONE;                   // +90° adjustment
            default -> Rotation.NONE;
        };
    }

    /**
     * Place stairs dynamically from Surface Dropper position until target Y is reached.
     * Stairs descend vertically (DOWN only).
     * Aligns Stairs top Jigsaw Block with Dropper position.
     * Returns StairsPlacementResult containing stairs_bottom Jigsaw position and list of Jigsaw positions to remove.
     */
    private static StairsPlacementResult placeStairsDynamically(
        ServerLevel level,
        BlockPos dropperPos,
        Direction horizontalDirection,
        int targetY
    ) {
        ChronoDawn.LOGGER.info(
            "Placing stairs dynamically from Dropper at {} to Y={} (rotation for horizontal direction: {})",
            dropperPos,
            targetY,
            horizontalDirection
        );

        var structureTemplateManager = level.getStructureManager();
        var templateOptional = structureTemplateManager.get(STAIRS_TEMPLATE);

        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load stairs template: {}", STAIRS_TEMPLATE);
            return null;
        }

        StructureTemplate template = templateOptional.get();
        Rotation rotation = getRotationFromDirection(horizontalDirection);

        // Get stairs template dimensions
        net.minecraft.core.Vec3i templateSize = template.getSize();
        int stairsHeight = templateSize.getY();

        ChronoDawn.LOGGER.info("Stairs template size: {}", templateSize);

        // Get template size with rotation applied (for water removal bounding box)
        net.minecraft.core.Vec3i stairsTemplateSize = template.getSize(rotation);

        // Get Jigsaw Block offset for this rotation (measured values from Structure Block)
        BlockPos stairsJigsawOffset = switch (rotation) {
            case NONE -> new BlockPos(7, 7, 7);              // 0° (SOUTH)
            case CLOCKWISE_90 -> new BlockPos(-7, 7, 7);     // 90° (WEST)
            case CLOCKWISE_180 -> new BlockPos(-7, 7, -7);   // 180° (NORTH)
            case COUNTERCLOCKWISE_90 -> new BlockPos(7, 7, -7); // 270° (EAST)
        };

        ChronoDawn.LOGGER.info("Stairs Jigsaw offset for rotation {}: {}", rotation, stairsJigsawOffset);

        // Calculate correct placement position so Jigsaw aligns with Dropper
        // Adjust Y by -1 to connect properly without overlapping surface bottom layer
        BlockPos stairsPlacementPos = dropperPos.subtract(stairsJigsawOffset).below();
        ChronoDawn.LOGGER.info("Stairs placement position (for Jigsaw-Dropper alignment): {}", stairsPlacementPos);

        // Create placement settings
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Add processors
        ResourceLocation processorListId = ResourceLocation.fromNamespaceAndPath(
            ChronoDawn.MOD_ID,
            "convert_decorative_water"
        );

        try {
            var processorListRegistry = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.PROCESSOR_LIST);
            var processorList = processorListRegistry.get(processorListId);

            if (processorList != null) {
                ChronoDawn.LOGGER.info("Applying {} processors for stairs from {}",
                    processorList.list().size(), processorListId);
                for (var processor : processorList.list()) {
                    settings.addProcessor(processor);
                }
            } else {
                ChronoDawn.LOGGER.warn("Processor list {} not found for stairs", processorListId);
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to load processor list for stairs: {}", e.getMessage(), e);
        }

        BlockPos currentPos = stairsPlacementPos;
        int stairsPlaced = 0;

        ChronoDawn.LOGGER.info("Starting stairs placement at Y={}, target Y={}, stairs height={}",
            currentPos.getY(), targetY, stairsHeight);

        // Get DecorativeWater block for protection
        var decorativeWaterBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK
            .get(ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "decorative_water"));

        // List to store Jigsaw Block positions for later removal
        List<BlockPos> jigsawsToRemove = new ArrayList<>();

        // List to store stairs positions for Jigsaw removal tracking
        List<BlockPos> stairsPositions = new ArrayList<>();

        // List to store protected areas (already placed structures) to avoid removing their water
        List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas = new ArrayList<>();

        // Place stairs until we're close to target Y
        while (currentPos.getY() - stairsHeight > targetY) {
            ChronoDawn.LOGGER.info("Placing stairs #{} at {}", stairsPlaced + 1, currentPos);

            // STEP 1: Remove Aquifer water BEFORE placement (mimic StructureStartMixin @HEAD)
            int waterRemoved = removeAquiferWaterBeforePlacement(level, currentPos, stairsTemplateSize, decorativeWaterBlock, protectedAreas);
            if (waterRemoved > 0) {
                ChronoDawn.LOGGER.info("Removed {} Aquifer water blocks before placing stairs #{}", waterRemoved, stairsPlaced + 1);
            }

            // STEP 2: Place stairs template (processors will be applied)
            template.placeInWorld(level, currentPos, currentPos, settings, level.random, 3);

            // STEP 3: Finalize waterlogging AFTER placement (mimic StructureStartMixin @RETURN)
            ChronoDawn.LOGGER.info("IMMEDIATE finalize for stairs #{} at {}", stairsPlaced + 1, currentPos);
            finalizeWaterloggingAfterPlacement(level, currentPos, templateSize, rotation);

            // STEP 3.5: Schedule delayed finalize to catch late waterlogging from adjacent chunks
            // Store immutable copy for lambda capture
            final BlockPos finalCurrentPos = currentPos.immutable();
            final net.minecraft.core.Vec3i finalTemplateSize = templateSize;
            final Rotation finalRotation = rotation;
            final int finalStairsNumber = stairsPlaced + 1;
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 2, () -> {
                finalizeWaterloggingAfterPlacement(level, finalCurrentPos, finalTemplateSize, finalRotation);
                ChronoDawn.LOGGER.info("Delayed finalize for stairs #{} completed", finalStairsNumber);
            }));

            // STEP 4: Add this structure to protected areas to prevent later structures from removing its water
            net.minecraft.world.level.levelgen.structure.BoundingBox stairsBoundingBox =
                net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(
                    currentPos,
                    currentPos.offset(stairsTemplateSize.getX() - 1, stairsTemplateSize.getY() - 1, stairsTemplateSize.getZ() - 1)
                );
            protectedAreas.add(stairsBoundingBox);

            // Record stairs position for Jigsaw removal tracking
            stairsPositions.add(new BlockPos(currentPos.getX(), currentPos.getY(), currentPos.getZ()));

            // Record Jigsaw Block positions for later removal (top and bottom Jigsaw)
            // Top Jigsaw offset (already calculated)
            BlockPos topJigsawPos = currentPos.offset(stairsJigsawOffset);
            jigsawsToRemove.add(topJigsawPos);

            // Bottom Jigsaw offset (same X/Z, Y=0)
            BlockPos bottomJigsawOffset = switch (rotation) {
                case NONE -> new BlockPos(7, 0, 7);
                case CLOCKWISE_90 -> new BlockPos(-7, 0, 7);
                case CLOCKWISE_180 -> new BlockPos(-7, 0, -7);
                case COUNTERCLOCKWISE_90 -> new BlockPos(7, 0, -7);
            };
            BlockPos bottomJigsawPos = currentPos.offset(bottomJigsawOffset);
            jigsawsToRemove.add(bottomJigsawPos);

            stairsPlaced++;

            // Move to next position: descend by template height
            currentPos = currentPos.below(stairsHeight);

            // Safety check to prevent infinite loop
            if (stairsPlaced > 100) {
                ChronoDawn.LOGGER.error("Stairs placement limit reached (safety check)");
                break;
            }
        }

        ChronoDawn.LOGGER.info("Placed {} stairs segments", stairsPlaced);
        ChronoDawn.LOGGER.info("Final stairs position (for stairs_bottom placement): Y={}", currentPos.getY());

        // Calculate correct stairs_bottom placement position
        // Last stairs has Jigsaw at offset Y=7, stairs_bottom has top Jigsaw at offset Y=11
        // Need to align them: lastStairsY + 7 = stairs_bottomY + 11
        // stairs_bottomY = lastStairsY + 7 - 11 = lastStairsY - 4
        BlockPos lastStairsPos = currentPos.above(stairsHeight); // Last placed stairs position
        BlockPos stairsBottomPos = new BlockPos(
            currentPos.getX(),
            lastStairsPos.getY() - 4,  // Adjust by 4 blocks to align Jigsaw Blocks
            currentPos.getZ()
        );

        ChronoDawn.LOGGER.info("Last stairs at Y={}, stairs_bottom adjusted to Y={} (4 blocks higher for Jigsaw alignment)",
            lastStairsPos.getY(), stairsBottomPos.getY());

        // Place stairs_bottom at the end
        var stairsBottomOptional = structureTemplateManager.get(STAIRS_BOTTOM_TEMPLATE);
        if (stairsBottomOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load stairs_bottom template: {}", STAIRS_BOTTOM_TEMPLATE);
            return null;
        }

        StructureTemplate stairsBottomTemplate = stairsBottomOptional.get();
        net.minecraft.core.Vec3i stairsBottomTemplateSizeOriginal = stairsBottomTemplate.getSize();
        net.minecraft.core.Vec3i stairsBottomTemplateSize = stairsBottomTemplate.getSize(rotation);

        // stairs_bottom uses same rotation as stairs for correct connection
        StructurePlaceSettings stairsBottomSettings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Add processors for stairs_bottom
        ResourceLocation processorListId2 = ResourceLocation.fromNamespaceAndPath(
            ChronoDawn.MOD_ID,
            "convert_decorative_water"
        );

        try {
            var processorListRegistry = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.PROCESSOR_LIST);
            var processorList = processorListRegistry.get(processorListId2);

            if (processorList != null) {
                ChronoDawn.LOGGER.info("Applying {} processors for stairs_bottom from {}",
                    processorList.list().size(), processorListId2);
                for (var processor : processorList.list()) {
                    stairsBottomSettings.addProcessor(processor);
                }
            } else {
                ChronoDawn.LOGGER.warn("Processor list {} not found for stairs_bottom", processorListId2);
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to load processor list for stairs_bottom: {}", e.getMessage(), e);
        }

        // STEP 1: Remove Aquifer water BEFORE placement
        int stairsBottomWaterRemoved = removeAquiferWaterBeforePlacement(level, stairsBottomPos, stairsBottomTemplateSize, decorativeWaterBlock, protectedAreas);
        if (stairsBottomWaterRemoved > 0) {
            ChronoDawn.LOGGER.info("Removed {} Aquifer water blocks before placing stairs_bottom", stairsBottomWaterRemoved);
        }

        // STEP 2: Place stairs_bottom at adjusted position
        stairsBottomTemplate.placeInWorld(level, stairsBottomPos, stairsBottomPos, stairsBottomSettings, level.random, 3);
        ChronoDawn.LOGGER.info("Placed stairs_bottom at {}", stairsBottomPos);

        // STEP 3: Finalize waterlogging AFTER placement
        finalizeWaterloggingAfterPlacement(level, stairsBottomPos, stairsBottomTemplateSizeOriginal, rotation);

        // STEP 3.5: Schedule delayed finalize for stairs_bottom
        final BlockPos finalStairsBottomPos = stairsBottomPos.immutable();
        final net.minecraft.core.Vec3i finalStairsBottomTemplateSizeOriginal = stairsBottomTemplateSizeOriginal;
        final Rotation finalRotation = rotation;
        level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 2, () -> {
            finalizeWaterloggingAfterPlacement(level, finalStairsBottomPos, finalStairsBottomTemplateSizeOriginal, finalRotation);
            ChronoDawn.LOGGER.info("Delayed finalize for stairs_bottom completed");
        }));

        // STEP 4: Add this structure to protected areas
        net.minecraft.world.level.levelgen.structure.BoundingBox stairsBottomBoundingBox =
            net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(
                stairsBottomPos,
                stairsBottomPos.offset(stairsBottomTemplateSize.getX() - 1, stairsBottomTemplateSize.getY() - 1, stairsBottomTemplateSize.getZ() - 1)
            );
        protectedAreas.add(stairsBottomBoundingBox);

        // Record stairs_bottom top Jigsaw position for later removal (at Y=11)
        BlockPos stairsBottomTopJigsawOffset = switch (rotation) {
            case NONE -> new BlockPos(7, 11, 7);
            case CLOCKWISE_90 -> new BlockPos(-7, 11, 7);
            case CLOCKWISE_180 -> new BlockPos(-7, 11, -7);
            case COUNTERCLOCKWISE_90 -> new BlockPos(7, 11, -7);
        };
        BlockPos stairsBottomTopJigsawPos = stairsBottomPos.offset(stairsBottomTopJigsawOffset);
        jigsawsToRemove.add(stairsBottomTopJigsawPos);

        // Get stairs_bottom side Jigsaw Block offset for corridor connection (measured values)
        // This is the horizontal Jigsaw Block, not the top one
        BlockPos stairsBottomJigsawOffset = switch (rotation) {
            case NONE -> new BlockPos(14, 1, 7);              // 0° (SOUTH)
            case CLOCKWISE_90 -> new BlockPos(-7, 1, 14);     // 90° (WEST)
            case CLOCKWISE_180 -> new BlockPos(-14, 1, -7);   // 180° (NORTH)
            case COUNTERCLOCKWISE_90 -> new BlockPos(7, 1, -14); // 270° (EAST)
        };

        // Calculate Jigsaw Block absolute position
        BlockPos stairsBottomJigsawPos = stairsBottomPos.offset(stairsBottomJigsawOffset);
        ChronoDawn.LOGGER.info("Stairs_bottom side Jigsaw Block at {} (offset: {})",
            stairsBottomJigsawPos, stairsBottomJigsawOffset);

        // Verify Jigsaw Block was placed and get its facing direction
        BlockState jigsawState = level.getBlockState(stairsBottomJigsawPos);
        if (!jigsawState.is(Blocks.JIGSAW)) {
            ChronoDawn.LOGGER.error("Expected Jigsaw Block at {}, but found: {}",
                stairsBottomJigsawPos, jigsawState.getBlock());
        } else {
            // Get the actual orientation of the Jigsaw Block
            var jigsawOrientation = jigsawState.getValue(net.minecraft.world.level.block.JigsawBlock.ORIENTATION);
            Direction jigsawFacing = jigsawOrientation.front();
            ChronoDawn.LOGGER.info("Stairs_bottom side Jigsaw Block facing: {}", jigsawFacing);
        }

        // Return stairs placement result (including protected areas and rotation)
        return new StairsPlacementResult(stairsBottomJigsawPos, jigsawsToRemove, stairsPositions, templateSize, rotation, protectedAreas);
    }

    /**
     * Find Jigsaw Block within stairs_bottom structure.
     */
    private static BlockPos findStairsBottomJigsawBlock(ServerLevel level, BlockPos stairsBottomStart, net.minecraft.core.Vec3i size) {
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();

        for (BlockPos pos : BlockPos.betweenClosed(
            stairsBottomStart,
            stairsBottomStart.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.JIGSAW)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof JigsawBlockEntity) {
                    ChronoDawn.LOGGER.info("Found stairs_bottom Jigsaw Block at {}", pos);
                    return pos;
                }
            }
        }

        ChronoDawn.LOGGER.warn("No Jigsaw Block found in stairs_bottom");
        return null;
    }

    /**
     * Stairs placement result containing stairs_bottom Jigsaw position and list of Jigsaw positions to remove later.
     */
    private static class StairsPlacementResult {
        final BlockPos stairsBottomJigsawPos;
        final List<BlockPos> jigsawsToRemove;
        final List<BlockPos> stairsPositions;
        final net.minecraft.core.Vec3i stairsTemplateSize;
        final Rotation stairsRotation;
        final List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas;

        StairsPlacementResult(BlockPos stairsBottomJigsawPos, List<BlockPos> jigsawsToRemove,
                              List<BlockPos> stairsPositions, net.minecraft.core.Vec3i stairsTemplateSize,
                              Rotation stairsRotation,
                              List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas) {
            this.stairsBottomJigsawPos = stairsBottomJigsawPos;
            this.jigsawsToRemove = jigsawsToRemove;
            this.stairsPositions = stairsPositions;
            this.stairsTemplateSize = stairsTemplateSize;
            this.stairsRotation = stairsRotation;
            this.protectedAreas = protectedAreas;
        }
    }

    /**
     * Corridor placement result containing Jigsaw position and rotation.
     */
    private static class CorridorPlacementResult {
        final BlockPos jigsawPos;
        final Rotation rotation;
        final List<BlockPos> stairsPositions;
        final net.minecraft.core.Vec3i stairsTemplateSize;
        final Rotation stairsRotation;
        final List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas;

        CorridorPlacementResult(BlockPos jigsawPos, Rotation rotation,
                                List<BlockPos> stairsPositions, net.minecraft.core.Vec3i stairsTemplateSize,
                                Rotation stairsRotation,
                                List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas) {
            this.jigsawPos = jigsawPos;
            this.rotation = rotation;
            this.stairsPositions = stairsPositions;
            this.stairsTemplateSize = stairsTemplateSize;
            this.stairsRotation = stairsRotation;
            this.protectedAreas = protectedAreas;
        }
    }

    /**
     * Place corridor at specified position (from stairs_bottom Jigsaw Block).
     * Also removes all Jigsaw Blocks recorded from stairs placement.
     * Removes water from stairs after all structure placement is complete.
     * Returns the position and rotation of the corridor's end Jigsaw Block.
     */
    private static CorridorPlacementResult placeCorridor(
        ServerLevel level,
        BlockPos stairsBottomJigsawPos,
        Direction direction,
        List<BlockPos> jigsawsToRemove,
        List<BlockPos> stairsPositions,
        net.minecraft.core.Vec3i stairsTemplateSize,
        Rotation stairsRotation,
        List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas
    ) {
        ChronoDawn.LOGGER.info(
            "Placing corridor from stairs_bottom Jigsaw Block at {} (direction: {})",
            stairsBottomJigsawPos,
            direction
        );

        var structureTemplateManager = level.getStructureManager();
        var templateOptional = structureTemplateManager.get(CORRIDOR_TEMPLATE);

        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load corridor template: {}", CORRIDOR_TEMPLATE);
            return null;
        }

        StructureTemplate template = templateOptional.get();
        // Use Jigsaw facing directly without +90° adjustment
        Rotation rotation = getRotationFromJigsawFacing(direction);
        ChronoDawn.LOGGER.info("Corridor rotation from Jigsaw facing {}: {}", direction, rotation);

        // Get corridor stairs_bottom side Jigsaw Block offset (measured values)
        BlockPos corridorStairsBottomJigsawOffset = switch (rotation) {
            case NONE -> new BlockPos(0, 10, 7);              // 0° (SOUTH)
            case CLOCKWISE_90 -> new BlockPos(-7, 10, 0);     // 90° (WEST)
            case CLOCKWISE_180 -> new BlockPos(0, 10, -7);   // 180° (NORTH)
            case COUNTERCLOCKWISE_90 -> new BlockPos(7, 10, 0); // 270° (EAST)
        };

        // Calculate corridor placement position so its Jigsaw aligns with stairs_bottom Jigsaw
        BlockPos corridorPlacementPos = stairsBottomJigsawPos.subtract(corridorStairsBottomJigsawOffset);
        ChronoDawn.LOGGER.info("Corridor placement position: {} (to align Jigsaw at {} with stairs_bottom Jigsaw)",
            corridorPlacementPos, corridorStairsBottomJigsawOffset);

        // Create placement settings
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false);

        // Add processors
        ResourceLocation processorListId = ResourceLocation.fromNamespaceAndPath(
            ChronoDawn.MOD_ID,
            "convert_decorative_water"
        );

        try {
            var processorListRegistry = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.PROCESSOR_LIST);
            var processorList = processorListRegistry.get(processorListId);

            if (processorList != null) {
                for (var processor : processorList.list()) {
                    settings.addProcessor(processor);
                }
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to load processor list: {}", e.getMessage(), e);
        }

        // Place corridor template
        template.placeInWorld(level, corridorPlacementPos, corridorPlacementPos, settings, level.random, 3);

        ChronoDawn.LOGGER.info("Placed corridor at {}", corridorPlacementPos);

        // Remove all Jigsaw Blocks from corridor
        net.minecraft.core.Vec3i corridorSize = template.getSize();
        for (BlockPos pos : BlockPos.betweenClosed(
            corridorPlacementPos,
            corridorPlacementPos.offset(corridorSize.getX() - 1, corridorSize.getY() - 1, corridorSize.getZ() - 1)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.JIGSAW)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        // Also remove stairs_bottom side Jigsaw Block (now that corridor is connected)
        BlockState stairsBottomJigsawState = level.getBlockState(stairsBottomJigsawPos);
        if (stairsBottomJigsawState.is(Blocks.JIGSAW)) {
            level.setBlock(stairsBottomJigsawPos, Blocks.AIR.defaultBlockState(), 3);
            ChronoDawn.LOGGER.info("Removed stairs_bottom side Jigsaw Block");
        }

        // Remove all recorded Jigsaw Blocks from stairs and stairs_bottom
        int stairsJigsawsRemoved = 0;
        for (BlockPos jigsawPos : jigsawsToRemove) {
            BlockState state = level.getBlockState(jigsawPos);
            if (state.is(Blocks.JIGSAW)) {
                level.setBlock(jigsawPos, Blocks.AIR.defaultBlockState(), 3);
                stairsJigsawsRemoved++;
            }
        }
        if (stairsJigsawsRemoved > 0) {
            ChronoDawn.LOGGER.info("Removed {} Jigsaw Blocks from stairs and stairs_bottom", stairsJigsawsRemoved);
        }

        // Note: Water removal is now handled per-structure during placement (before/after each stairs/stairs_bottom)
        // No need for batch water removal here

        // Get corridor boss_room side Jigsaw Block offset (measured values)
        BlockPos corridorBossRoomJigsawOffset = switch (rotation) {
            case NONE -> new BlockPos(14, 1, 7);              // 0° (SOUTH)
            case CLOCKWISE_90 -> new BlockPos(-7, 1, 14);     // 90° (WEST)
            case CLOCKWISE_180 -> new BlockPos(-14, 1, -7);   // 180° (NORTH)
            case COUNTERCLOCKWISE_90 -> new BlockPos(7, 1, -14); // 270° (EAST)
        };

        // Calculate Jigsaw Block absolute position
        BlockPos corridorJigsawPos = corridorPlacementPos.offset(corridorBossRoomJigsawOffset);
        ChronoDawn.LOGGER.info("Corridor boss_room side Jigsaw Block at {} (offset: {})",
            corridorJigsawPos, corridorBossRoomJigsawOffset);

        // Verify Jigsaw Block (don't remove it yet - boss room needs it for alignment)
        BlockState jigsawState = level.getBlockState(corridorJigsawPos);
        if (!jigsawState.is(Blocks.JIGSAW)) {
            ChronoDawn.LOGGER.warn("Expected Jigsaw Block at {}, but found: {}",
                corridorJigsawPos, jigsawState.getBlock());
        }

        return new CorridorPlacementResult(corridorJigsawPos, rotation, stairsPositions, stairsTemplateSize, stairsRotation, protectedAreas);
    }

    /**
     * Find Jigsaw Block within corridor structure and remove it.
     */
    private static BlockPos findCorridorJigsawBlock(ServerLevel level, BlockPos corridorStart, net.minecraft.core.Vec3i size) {
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();

        for (BlockPos pos : BlockPos.betweenClosed(
            corridorStart,
            corridorStart.offset(sizeX, sizeY, sizeZ)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.JIGSAW)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof JigsawBlockEntity) {
                    ChronoDawn.LOGGER.info("Found corridor Jigsaw Block at {}", pos);

                    // Remove the Jigsaw Block
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    ChronoDawn.LOGGER.info("Removed corridor Jigsaw Block");

                    return pos;
                }
            }
        }

        ChronoDawn.LOGGER.warn("No Jigsaw Block found in corridor");
        return null;
    }

    /**
     * Remove Aquifer water before structure placement to prevent waterlogging.
     * This mimics StructureStartMixin's @HEAD injection logic.
     *
     * @param level ServerLevel
     * @param placementPos Structure placement position
     * @param templateSize Structure template size
     * @param decorativeWaterBlock DecorativeWater block to preserve
     * @param protectedAreas Already placed structure areas to skip (to preserve their water)
     * @return Number of water blocks removed
     */
    private static int removeAquiferWaterBeforePlacement(
        ServerLevel level,
        BlockPos placementPos,
        net.minecraft.core.Vec3i templateSize,
        net.minecraft.world.level.block.Block decorativeWaterBlock,
        List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas
    ) {
        int waterRemoved = 0;
        int decorativeWaterFound = 0;
        int protectedWaterSkipped = 0;

        // Remove ONLY minecraft:water (Aquifer water) within the structure bounds
        // Preserve chronodawn:decorative_water (will be converted to water by processor)
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(templateSize.getX() - 1, templateSize.getY() - 1, templateSize.getZ() - 1)
        )) {
            // Check if this position is in a protected area (already placed structure)
            boolean isProtected = false;
            for (net.minecraft.world.level.levelgen.structure.BoundingBox protectedArea : protectedAreas) {
                if (protectedArea.isInside(pos)) {
                    isProtected = true;
                    break;
                }
            }

            if (isProtected) {
                // Skip water in already placed structures to preserve decorative water
                BlockState state = level.getBlockState(pos);
                if (state.getFluidState().isSource()) {
                    protectedWaterSkipped++;
                }
                continue;
            }

            BlockState state = level.getBlockState(pos);

            // DEBUG: Count decorative_water blocks
            if (state.is(decorativeWaterBlock)) {
                decorativeWaterFound++;
                continue; // Skip decorative_water
            }

            // Only remove minecraft:water (Aquifer water), not chronodawn:decorative_water
            if (state.getFluidState().isSource() &&
                state.getBlock() == Blocks.WATER) {
                // Remove water temporarily to prevent waterlogging
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                waterRemoved++;
            }
        }


        return waterRemoved;
    }

    /**
     * Finalize waterlogging state after structure placement.
     * This mimics StructureStartMixin's @RETURN injection logic.
     *
     * @param level ServerLevel
     * @param placementPos Structure placement position
     * @param templateSize Structure template size (WITHOUT rotation - original size)
     * @param rotation Structure rotation
     */
    private static void finalizeWaterloggingAfterPlacement(
        ServerLevel level,
        BlockPos placementPos,
        net.minecraft.core.Vec3i templateSize,
        Rotation rotation
    ) {
        int totalWaterloggedRestored = 0;
        int totalWaterloggedRemoved = 0;
        int totalWaterloggedBeforeFinalize = 0;

        // Calculate correct bounding box based on rotation
        // Rotation affects which direction the structure extends from placementPos
        BlockPos minPos, maxPos;
        switch (rotation) {
            case NONE -> {
                minPos = placementPos;
                maxPos = placementPos.offset(templateSize.getX() - 1, templateSize.getY() - 1, templateSize.getZ() - 1);
            }
            case CLOCKWISE_90 -> {
                // X and Z swap, X goes negative
                minPos = placementPos.offset(-(templateSize.getZ() - 1), 0, 0);
                maxPos = placementPos.offset(0, templateSize.getY() - 1, templateSize.getX() - 1);
            }
            case CLOCKWISE_180 -> {
                // Both X and Z go negative
                minPos = placementPos.offset(-(templateSize.getX() - 1), 0, -(templateSize.getZ() - 1));
                maxPos = placementPos.offset(0, templateSize.getY() - 1, 0);
            }
            case COUNTERCLOCKWISE_90 -> {
                // X and Z swap, Z goes negative
                minPos = placementPos.offset(0, 0, -(templateSize.getX() - 1));
                maxPos = placementPos.offset(templateSize.getZ() - 1, templateSize.getY() - 1, 0);
            }
            default -> throw new IllegalStateException("Unexpected rotation: " + rotation);
        }

        ChronoDawn.LOGGER.info("Finalize bounding box for rotation {}: {} to {}", rotation, minPos, maxPos);

        // Finalize waterlogging and convert decorative water
        int totalBlocksChecked = 0;
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            totalBlocksChecked++;
            BlockState state = level.getBlockState(pos);
            BlockPos immutablePos = pos.immutable();

            // Finalize waterlogging state (decorative_water conversion deferred until all structures placed)
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                boolean currentWaterlogged = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
                if (currentWaterlogged) {
                    totalWaterloggedBeforeFinalize++;
                }
                boolean shouldBeWaterlogged = com.chronodawn.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.contains(immutablePos);

                if (currentWaterlogged != shouldBeWaterlogged) {
                    BlockState newState = state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, shouldBeWaterlogged);
                    level.setBlock(pos, newState, 2);

                    if (shouldBeWaterlogged) {
                        totalWaterloggedRestored++;
                        // Remove from set after restoring (cleanup)
                        com.chronodawn.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.remove(immutablePos);
                    } else {
                        totalWaterloggedRemoved++;
                    }
                } else if (shouldBeWaterlogged) {
                    // Already correctly waterlogged, remove from set (cleanup)
                    com.chronodawn.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.remove(immutablePos);
                }
            }
        }

        if (totalWaterloggedBeforeFinalize > 0) {
            ChronoDawn.LOGGER.info(
                "DEBUG: Found {} waterlogged blocks before finalize at {}",
                totalWaterloggedBeforeFinalize,
                placementPos
            );
        }

        if (totalWaterloggedRestored > 0) {
            ChronoDawn.LOGGER.info(
                "Restored {} intentional waterlogged blocks at {}",
                totalWaterloggedRestored,
                placementPos
            );
        }

        if (totalWaterloggedRemoved > 0) {
            ChronoDawn.LOGGER.info(
                "Removed {} unintentional waterlogged blocks at {}",
                totalWaterloggedRemoved,
                placementPos
            );
        }

        // DEBUG: Log summary
        ChronoDawn.LOGGER.info(
            "Finalize waterlogging summary at {}: checked {} blocks, {} waterlogged before, restored {}, removed {}",
            placementPos,
            totalBlocksChecked,
            totalWaterloggedBeforeFinalize,
            totalWaterloggedRestored,
            totalWaterloggedRemoved
        );
    }

    /**
     * Convert decorative_water to vanilla water after all structures are placed.
     * This should be called ONLY ONCE after all structure placement is complete.
     *
     * @param level ServerLevel
     * @param placementPos Structure placement position
     * @param templateSize Structure template size (WITHOUT rotation - original size)
     * @param rotation Structure rotation
     */
    private static void convertDecorativeWaterToVanilla(
        ServerLevel level,
        BlockPos placementPos,
        net.minecraft.core.Vec3i templateSize,
        Rotation rotation
    ) {
        int totalDecorativeWaterConverted = 0;

        // Calculate correct bounding box based on rotation
        BlockPos minPos, maxPos;
        switch (rotation) {
            case NONE -> {
                minPos = placementPos;
                maxPos = placementPos.offset(templateSize.getX() - 1, templateSize.getY() - 1, templateSize.getZ() - 1);
            }
            case CLOCKWISE_90 -> {
                minPos = placementPos.offset(-(templateSize.getZ() - 1), 0, 0);
                maxPos = placementPos.offset(0, templateSize.getY() - 1, templateSize.getX() - 1);
            }
            case CLOCKWISE_180 -> {
                minPos = placementPos.offset(-(templateSize.getX() - 1), 0, -(templateSize.getZ() - 1));
                maxPos = placementPos.offset(0, templateSize.getY() - 1, 0);
            }
            case COUNTERCLOCKWISE_90 -> {
                minPos = placementPos.offset(0, 0, -(templateSize.getX() - 1));
                maxPos = placementPos.offset(templateSize.getZ() - 1, templateSize.getY() - 1, 0);
            }
            default -> throw new IllegalStateException("Unexpected rotation: " + rotation);
        }

        ChronoDawn.LOGGER.info("Converting decorative_water for rotation {}: {} to {}", rotation, minPos, maxPos);

        // Convert decorative water to vanilla water
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = level.getBlockState(pos);

            if (state.is(com.chronodawn.registry.ModBlocks.DECORATIVE_WATER.get())) {
                BlockState newState = Blocks.WATER.defaultBlockState();

                // Preserve fluid level if present
                if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL)) {
                    int fluidLevel = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL);
                    newState = newState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL, fluidLevel);
                }

                level.setBlock(pos, newState, 2);
                totalDecorativeWaterConverted++;
            }
        }

        if (totalDecorativeWaterConverted > 0) {
            ChronoDawn.LOGGER.info(
                "Converted {} decorative_water blocks to vanilla water at {}",
                totalDecorativeWaterConverted,
                placementPos
            );
        }
    }

    /**
     * Find Corridor end marker (Amethyst Block).
     */
    private static BlockPos findCorridorEndMarker(ServerLevel level, BlockPos structureOrigin) {
        int searchRadius = 100;
        int maxY = 150;
        int minY = -60;

        ChronoDawn.LOGGER.info(
            "Searching for Corridor end marker (Amethyst Block) around {}",
            structureOrigin
        );

        // Search for Amethyst Block (corridor marker)
        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(
                        structureOrigin.getX() + x,
                        y,
                        structureOrigin.getZ() + z
                    );
                    BlockState state = level.getBlockState(checkPos);

                    // Check if this is an Amethyst Block (Master Clock corridor end marker)
                    if (state.is(Blocks.AMETHYST_BLOCK)) {
                        ChronoDawn.LOGGER.info("Found Corridor end marker at {}", checkPos);
                        return checkPos;
                    }
                }
            }
        }

        ChronoDawn.LOGGER.warn("No Corridor end marker found near {}", structureOrigin);
        return null;
    }

    /**
     * Place boss_room connected to corridor via Jigsaw Block.
     * Also performs final water removal from stairs after all structures are placed.
     */
    private static boolean placeBossRoom(ServerLevel level, BlockPos corridorJigsawPos, Rotation corridorRotation,
                                          List<BlockPos> stairsPositions, net.minecraft.core.Vec3i stairsTemplateSize,
                                          Rotation stairsRotation,
                                          List<net.minecraft.world.level.levelgen.structure.BoundingBox> protectedAreas) {
        ChronoDawn.LOGGER.info(
            "Placing boss_room connected to corridor Jigsaw at {} (corridor rotation: {})",
            corridorJigsawPos,
            corridorRotation
        );

        var structureTemplateManager = level.getStructureManager();

        // Load boss_room template
        var templateOptional = structureTemplateManager.get(BOSS_ROOM_TEMPLATE);
        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load boss_room template: {}", BOSS_ROOM_TEMPLATE);
            return false;
        }

        StructureTemplate template = templateOptional.get();

        // Boss room uses same rotation as corridor for Jigsaw connection
        Rotation bossRoomRotation = corridorRotation;

        // Get boss_room corridor side Jigsaw Block offset (measured values)
        BlockPos bossRoomJigsawOffset = switch (bossRoomRotation) {
            case NONE -> new BlockPos(0, 1, 17);              // 0° (SOUTH)
            case CLOCKWISE_90 -> new BlockPos(-17, 1, 0);     // 90° (WEST)
            case CLOCKWISE_180 -> new BlockPos(0, 1, -17);    // 180° (NORTH)
            case COUNTERCLOCKWISE_90 -> new BlockPos(17, 1, 0); // 270° (EAST)
        };

        // Calculate boss_room placement position so its Jigsaw aligns with corridor Jigsaw
        BlockPos placementPos = corridorJigsawPos.subtract(bossRoomJigsawOffset);

        ChronoDawn.LOGGER.info(
            "Boss_room placement position: {} (Jigsaw offset: {}, rotation: {})",
            placementPos,
            bossRoomJigsawOffset,
            bossRoomRotation
        );

        // Get DecorativeWater block for protection
        var decorativeWaterBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK
            .get(ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "decorative_water"));

        // Get template size for water removal
        net.minecraft.core.Vec3i templateSize = template.getSize();

        // STEP 1: Remove Aquifer water BEFORE placement (mimic StructureStartMixin @HEAD)
        int waterRemoved = removeAquiferWaterBeforePlacement(level, placementPos, templateSize, decorativeWaterBlock, protectedAreas);
        if (waterRemoved > 0) {
            ChronoDawn.LOGGER.info("Removed {} Aquifer water blocks before placing boss_room", waterRemoved);
        }

        // Load processor list for waterlogging prevention
        ResourceLocation processorListId = ResourceLocation.fromNamespaceAndPath(
            ChronoDawn.MOD_ID,
            "convert_decorative_water"
        );

        // Create placement settings
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(bossRoomRotation)
            .setIgnoreEntities(false);

        // Add processors
        try {
            var processorListRegistry = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.PROCESSOR_LIST);
            var processorList = processorListRegistry.get(processorListId);

            if (processorList != null) {
                for (var processor : processorList.list()) {
                    settings.addProcessor(processor);
                }
                ChronoDawn.LOGGER.info("Applied {} structure processors", processorList.list().size());
            } else {
                ChronoDawn.LOGGER.error("Processor list {} not found", processorListId);
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to load processor list: {}", e.getMessage(), e);
        }

        // STEP 2: Place structure (processors will be applied)
        template.placeInWorld(level, placementPos, placementPos, settings, level.random, 3);

        // STEP 3: Finalize waterlogging AFTER placement (mimic StructureStartMixin @RETURN)
        finalizeWaterloggingAfterPlacement(level, placementPos, templateSize, bossRoomRotation);

        // STEP 3.5: Schedule delayed finalize for boss_room
        final BlockPos finalPlacementPos = placementPos.immutable();
        final net.minecraft.core.Vec3i finalTemplateSize = templateSize;
        final Rotation finalBossRoomRotation = bossRoomRotation;
        level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 2, () -> {
            finalizeWaterloggingAfterPlacement(level, finalPlacementPos, finalTemplateSize, finalBossRoomRotation);
            ChronoDawn.LOGGER.info("Delayed finalize for boss_room completed");
        }));

        // Remove all Jigsaw Blocks from boss room (corridor connection is already established)
        for (BlockPos pos : BlockPos.betweenClosed(
            placementPos,
            placementPos.offset(templateSize.getX() - 1, templateSize.getY() - 1, templateSize.getZ() - 1)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.JIGSAW)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        // Remove the corridor Jigsaw Block now that boss room is placed
        level.setBlock(corridorJigsawPos, Blocks.AIR.defaultBlockState(), 3);
        ChronoDawn.LOGGER.info("Removed corridor Jigsaw Block at {}", corridorJigsawPos);

        // Convert decorative_water to vanilla water for ALL structures (stairs, boss_room)
        // This is done AFTER all structures are placed to prevent decorative_water from being removed
        ChronoDawn.LOGGER.info("Converting decorative_water to vanilla water for all structures");
        for (BlockPos stairsPos : stairsPositions) {
            convertDecorativeWaterToVanilla(level, stairsPos, stairsTemplateSize, stairsRotation);
        }
        convertDecorativeWaterToVanilla(level, placementPos, templateSize, bossRoomRotation);
        ChronoDawn.LOGGER.info("Decorative water conversion completed for all structures");

        // Note: Water removal is now handled per-structure during placement (before/after each stairs/stairs_bottom/boss_room)
        // However, schedule multiple delayed finalizes to catch late waterlogging from chunk loading
        final List<BlockPos> finalStairsPositions = new ArrayList<>(stairsPositions);
        final net.minecraft.core.Vec3i finalStairsTemplateSize = stairsTemplateSize;

        // Schedule multiple checks at increasing intervals to catch chunk load waterlogging
        int[] checkDelays = {10, 40, 100, 200}; // 0.5s, 2s, 5s, 10s
        final Rotation finalStairsRotation = stairsRotation;
        for (int checkDelay : checkDelays) {
            final int finalDelay = checkDelay;
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + checkDelay, () -> {
                ChronoDawn.LOGGER.info("Performing delayed finalize for all stairs ({} ticks after boss_room)", finalDelay);
                int totalRemovedInFinal = 0;
                for (BlockPos stairsPos : finalStairsPositions) {
                    // Use rotation-aware check (manual check first to log waterlogged blocks found)
                    BlockPos minPos, maxPos;
                    switch (finalStairsRotation) {
                        case NONE -> {
                            minPos = stairsPos;
                            maxPos = stairsPos.offset(finalStairsTemplateSize.getX() - 1, finalStairsTemplateSize.getY() - 1, finalStairsTemplateSize.getZ() - 1);
                        }
                        case CLOCKWISE_90 -> {
                            minPos = stairsPos.offset(-(finalStairsTemplateSize.getZ() - 1), 0, 0);
                            maxPos = stairsPos.offset(0, finalStairsTemplateSize.getY() - 1, finalStairsTemplateSize.getX() - 1);
                        }
                        case CLOCKWISE_180 -> {
                            minPos = stairsPos.offset(-(finalStairsTemplateSize.getX() - 1), 0, -(finalStairsTemplateSize.getZ() - 1));
                            maxPos = stairsPos.offset(0, finalStairsTemplateSize.getY() - 1, 0);
                        }
                        case COUNTERCLOCKWISE_90 -> {
                            minPos = stairsPos.offset(0, 0, -(finalStairsTemplateSize.getX() - 1));
                            maxPos = stairsPos.offset(finalStairsTemplateSize.getZ() - 1, finalStairsTemplateSize.getY() - 1, 0);
                        }
                        default -> throw new IllegalStateException("Unexpected rotation: " + finalStairsRotation);
                    }

                    int waterloggedBefore = 0;
                    for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
                        BlockState state = level.getBlockState(pos);
                        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                            if (state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                                waterloggedBefore++;
                            }
                        }
                    }

                    if (waterloggedBefore > 0) {
                        ChronoDawn.LOGGER.warn("Found {} late waterlogged blocks at {} before delayed finalize (delay={})", waterloggedBefore, stairsPos, finalDelay);
                        finalizeWaterloggingAfterPlacement(level, stairsPos, finalStairsTemplateSize, finalStairsRotation);
                        totalRemovedInFinal += waterloggedBefore;
                    }
                }
                if (totalRemovedInFinal > 0) {
                    ChronoDawn.LOGGER.info("Delayed finalize (delay={}) removed {} late waterlogged blocks from stairs", finalDelay, totalRemovedInFinal);
                } else {
                    ChronoDawn.LOGGER.info("Delayed finalize (delay={}): no late waterlogging detected", finalDelay);
                }
            }));
        }

        ChronoDawn.LOGGER.info("Successfully placed boss_room via Jigsaw connection");
        return true;
    }

    /**
     * Process a Master Clock structure and place boss_room if not already placed.
     */
    public static void processStructure(ServerLevel level, ChunkPos chunkPos) {
        ResourceLocation dimensionId = level.dimension().location();

        // Thread-safe: Use ConcurrentHashMap.newKeySet() for thread-safe Set
        processedStructures.putIfAbsent(dimensionId, ConcurrentHashMap.newKeySet());
        Set<BlockPos> dimensionProcessed = processedStructures.get(dimensionId);

        if (!hasMasterClock(level, chunkPos)) {
            return;
        }

        BlockPos structureOrigin = getStructureOrigin(level, chunkPos);
        if (structureOrigin == null) {
            return;
        }

        if (dimensionProcessed.contains(structureOrigin)) {
            return;
        }

        ChronoDawn.LOGGER.info(
            "Found Master Clock structure at chunk {} (origin: {})",
            chunkPos,
            structureOrigin
        );

        // Step 1: Find Dropper marker (connection point and direction)
        BlockPos surfaceDropperPos = findSurfaceDropperMarker(level, structureOrigin);
        if (surfaceDropperPos == null) {
            ChronoDawn.LOGGER.info("No Surface Dropper marker found yet, will retry later");
            return;
        }

        // Step 2: Get horizontal direction from Dropper facing
        BlockState dropperState = level.getBlockState(surfaceDropperPos);
        Direction horizontalDirection = dropperState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);

        // Verify it's a horizontal direction
        if (horizontalDirection == Direction.UP || horizontalDirection == Direction.DOWN) {
            ChronoDawn.LOGGER.error("Dropper must face horizontally, not {}", horizontalDirection);
            return;
        }

        ChronoDawn.LOGGER.info("Surface Dropper marker horizontal direction: {}", horizontalDirection);

        // Remove the Dropper marker
        level.setBlock(surfaceDropperPos, Blocks.AIR.defaultBlockState(), 3);

        // Step 3: Place stairs dynamically from Dropper position (connection point)
        StairsPlacementResult stairsResult = placeStairsDynamically(level, surfaceDropperPos, horizontalDirection, CORRIDOR_Y);
        if (stairsResult == null) {
            ChronoDawn.LOGGER.error("Failed to place stairs");
            return;
        }

        // Step 4: Get actual Jigsaw Block facing direction from stairs_bottom
        BlockState jigsawState = level.getBlockState(stairsResult.stairsBottomJigsawPos);
        if (!jigsawState.is(Blocks.JIGSAW)) {
            ChronoDawn.LOGGER.error("Expected Jigsaw Block at stairs end {}, but found: {}",
                stairsResult.stairsBottomJigsawPos, jigsawState.getBlock());
            return;
        }

        var jigsawOrientation = jigsawState.getValue(net.minecraft.world.level.block.JigsawBlock.ORIENTATION);
        Direction jigsawFacing = jigsawOrientation.front();
        ChronoDawn.LOGGER.info("Using Jigsaw Block facing {} for corridor placement", jigsawFacing);

        // Step 5: Place corridor at stairs end using actual Jigsaw facing
        CorridorPlacementResult corridorResult = placeCorridor(level, stairsResult.stairsBottomJigsawPos, jigsawFacing,
            stairsResult.jigsawsToRemove, stairsResult.stairsPositions, stairsResult.stairsTemplateSize,
            stairsResult.stairsRotation, stairsResult.protectedAreas);
        if (corridorResult == null) {
            ChronoDawn.LOGGER.error("Failed to place corridor");
            return;
        }

        // Step 6: Place boss_room connected to corridor via Jigsaw
        if (placeBossRoom(level, corridorResult.jigsawPos, corridorResult.rotation,
                corridorResult.stairsPositions, corridorResult.stairsTemplateSize, corridorResult.stairsRotation,
                corridorResult.protectedAreas)) {
            ChronoDawn.LOGGER.info(
                "Successfully placed boss_room for Master Clock at {}",
                structureOrigin
            );
        } else {
            ChronoDawn.LOGGER.error("Failed to place boss_room");
        }

        dimensionProcessed.add(structureOrigin);
    }

    /**
     * Check for Master Clock structures and place boss_room if needed.
     */
    public static void checkAndPlaceRooms(ServerLevel level) {
        // Only process Chrono Dawn dimension (Master Clock only spawns there)
        if (!level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        ResourceLocation dimensionId = level.dimension().location();

        // Thread-safe: Use ConcurrentHashMap.newKeySet() for thread-safe Set
        processedStructures.putIfAbsent(dimensionId, ConcurrentHashMap.newKeySet());
        tickCounters.putIfAbsent(dimensionId, 0);

        // Thread-safe: Use atomic compute operation for tick counter increment
        int currentTick = tickCounters.compute(dimensionId, (k, v) -> (v == null ? 0 : v) + 1);

        if (currentTick < CHECK_INTERVAL) {
            return;
        }
        tickCounters.put(dimensionId, 0);

        if (level.players().isEmpty()) {
            return;
        }

        for (var player : level.players()) {
            ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);
                    processStructure(level, chunkPos);
                }
            }
        }
    }

    /**
     * Register event handlers.
     */
    public static void register() {
        TickEvent.SERVER_POST.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                checkAndPlaceRooms(level);
            }
        });

        ChronoDawn.LOGGER.info("Registered MasterClockBossRoomPlacer");
    }
}
