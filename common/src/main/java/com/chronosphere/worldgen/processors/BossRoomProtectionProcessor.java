package com.chronosphere.worldgen.processors;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Boss Room Protection Processor
 *
 * Detects Boss Room Boundary Marker blocks during structure generation and:
 * 1. Collects marker positions (min and max)
 * 2. Stores positions in pending map for later registration
 * 3. Replaces markers with specified blocks
 *
 * Protection registration is deferred to server tick because:
 * - Structure generation happens in background threads
 * - ServerLevel is not available during structure processing
 * - Need to wait until world is ready to register protection
 *
 * Thread-safety:
 * - Uses ConcurrentHashMap for thread-safe marker storage
 * - Each marker is stored independently with world position as key
 * - Pairing happens during registration phase (single-threaded server tick)
 *
 * Usage:
 * - Place Boss Room Boundary Marker blocks in structure NBT at boss room corners
 * - Add this processor to structure's processor_list
 * - Markers will be replaced with specified blocks during generation
 * - Protection will be registered on next server tick
 * - Boss room will be protected until boss is defeated
 *
 * Implementation: T224 - Boss room protection with marker blocks
 */
public class BossRoomProtectionProcessor extends StructureProcessor {
    public static final MapCodec<BossRoomProtectionProcessor> CODEC =
        MapCodec.unit(BossRoomProtectionProcessor::new);

    /**
     * Marker data stored during structure generation.
     */
    private static class MarkerData {
        final BlockPos pos;
        final boolean isMin;

        MarkerData(BlockPos pos, boolean isMin) {
            this.pos = pos.immutable();
            this.isMin = isMin;
        }
    }

    // Pending markers to process during server tick
    // Key: marker world position (unique), Value: marker data
    private static final Map<BlockPos, MarkerData> PENDING_MARKERS = new ConcurrentHashMap<>();

    @Override
    protected StructureProcessorType<?> getType() {
        return com.chronosphere.registry.ModStructureProcessorTypes.BOSS_ROOM_PROTECTION.get();
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader level,
        BlockPos structurePos,
        BlockPos piecePos,
        StructureTemplate.StructureBlockInfo originalBlockInfo,
        StructureTemplate.StructureBlockInfo currentBlockInfo,
        StructurePlaceSettings settings
    ) {
        // Check if this is a Boss Room Boundary Marker block
        if (!currentBlockInfo.state().is(ModBlocks.BOSS_ROOM_BOUNDARY_MARKER.get())) {
            return currentBlockInfo;
        }

        // Read marker type and replacement block from NBT
        BlockPos worldPos = currentBlockInfo.pos();
        var nbt = currentBlockInfo.nbt();

        if (nbt == null) {
            Chronosphere.LOGGER.warn("Boss Room Boundary Marker at {} has no NBT data", worldPos);
            return currentBlockInfo;
        }

        String markerType = nbt.getString("MarkerType");
        String replaceWith = nbt.getString("ReplaceWith");

        if (markerType.isEmpty() || replaceWith.isEmpty()) {
            Chronosphere.LOGGER.warn("Boss Room Boundary Marker at {} has incomplete NBT (MarkerType={}, ReplaceWith={})",
                worldPos, markerType, replaceWith);
            return currentBlockInfo;
        }

        // Store marker data for later pairing
        boolean isMin = "boss_room_min".equals(markerType);
        boolean isMax = "boss_room_max".equals(markerType);

        if (isMin || isMax) {
            PENDING_MARKERS.put(worldPos.immutable(), new MarkerData(worldPos, isMin));
            Chronosphere.LOGGER.debug("Stored {} marker at {} for later registration",
                isMin ? "min" : "max", worldPos);
        }

        // Replace marker with specified block
        var replacementBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
            net.minecraft.resources.ResourceLocation.parse(replaceWith)
        );
        var replacementState = replacementBlock.defaultBlockState();

        Chronosphere.LOGGER.debug("Replacing boss room marker at {} with {}", worldPos, replacementState);

        return new StructureTemplate.StructureBlockInfo(
            worldPos,
            replacementState,
            null // No block entity for replacement block
        );
    }

    /**
     * Register all pending boss room protections.
     * Should be called from server tick event (single-threaded, safe to do pairing).
     *
     * Algorithm:
     * 1. Find all min markers
     * 2. For each min marker, find closest max marker
     * 3. Register protection if pair found and distance is reasonable
     * 4. Remove processed markers
     */
    public static void registerPendingProtections(ServerLevel level) {
        if (PENDING_MARKERS.isEmpty()) {
            return;
        }

        // Separate min and max markers
        List<MarkerData> minMarkers = new ArrayList<>();
        List<MarkerData> maxMarkers = new ArrayList<>();

        for (MarkerData marker : PENDING_MARKERS.values()) {
            // Check if marker is in this dimension (chunk must be loaded)
            if (!level.hasChunkAt(marker.pos)) {
                continue;
            }

            if (marker.isMin) {
                minMarkers.add(marker);
            } else {
                maxMarkers.add(marker);
            }
        }

        // Pair min and max markers
        List<BlockPos> processedMarkers = new ArrayList<>();

        for (MarkerData minMarker : minMarkers) {
            // Find closest max marker (should be in same structure)
            MarkerData closestMax = null;
            double closestDistance = Double.MAX_VALUE;

            for (MarkerData maxMarker : maxMarkers) {
                double distance = minMarker.pos.distSqr(maxMarker.pos);

                // Reasonable distance check: boss rooms are typically < 100 blocks across
                // Distance squared < 100^2 = 10000 (in any dimension)
                if (distance < 10000 && distance < closestDistance) {
                    closestMax = maxMarker;
                    closestDistance = distance;
                }
            }

            if (closestMax != null) {
                // Register protection
                BlockPos minPos = minMarker.pos;
                BlockPos maxPos = closestMax.pos;

                BoundingBox bossRoomArea = new BoundingBox(
                    Math.min(minPos.getX(), maxPos.getX()),
                    Math.min(minPos.getY(), maxPos.getY()),
                    Math.min(minPos.getZ(), maxPos.getZ()),
                    Math.max(minPos.getX(), maxPos.getX()),
                    Math.max(minPos.getY(), maxPos.getY()),
                    Math.max(minPos.getZ(), maxPos.getZ())
                );

                BlockProtectionHandler.registerProtectedArea(level, bossRoomArea, minPos);

                Chronosphere.LOGGER.info(
                    "Registered boss room protection in dimension {}: min={}, max={}, bounds={}",
                    level.dimension().location(), minPos, maxPos, bossRoomArea
                );

                // Mark for removal
                processedMarkers.add(minPos);
                processedMarkers.add(maxPos);

                // Remove max marker from list to prevent re-pairing
                maxMarkers.remove(closestMax);
            } else {
                Chronosphere.LOGGER.warn("Found min marker at {} but no matching max marker within 100 blocks",
                    minMarker.pos);
            }
        }

        // Remove processed markers
        processedMarkers.forEach(PENDING_MARKERS::remove);

        // Log remaining unpaired markers (for debugging)
        if (!PENDING_MARKERS.isEmpty() && !minMarkers.isEmpty()) {
            Chronosphere.LOGGER.warn("Still have {} unpaired markers after registration", PENDING_MARKERS.size());
        }
    }
}
