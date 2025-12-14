package com.chronosphere.worldgen.processors;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import com.chronosphere.worldgen.protection.PermanentProtectionHandler;
import com.chronosphere.worldgen.spawning.ClockworkColossusSpawner;
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
 * Supported marker types:
 * - boss_room_min/max: Temporary protection (removed when boss defeated) → BlockProtectionHandler
 * - permanent_protection_min/max: Permanent protection (never removed) → PermanentProtectionHandler
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
 * - Place Boss Room Boundary Marker blocks in structure NBT at corners
 * - Add this processor to structure's processor_list
 * - Markers will be replaced with specified blocks during generation
 * - Protection will be registered on next server tick
 *
 * Implementation: T224 - Boss room protection with marker blocks
 *                T302 - Permanent Master Clock wall protection
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
        final boolean isPermanent; // true = permanent protection, false = boss room protection
        final long addedTime; // Track when marker was added (for cleanup)

        MarkerData(BlockPos pos, boolean isMin, boolean isPermanent) {
            this.pos = pos.immutable();
            this.isMin = isMin;
            this.isPermanent = isPermanent;
            this.addedTime = System.currentTimeMillis();
        }
    }

    // Pending markers to process during server tick
    // Key: marker world position (unique), Value: marker data
    // Note: All dimensions share same map, but registration filters by loaded chunks
    private static final Map<BlockPos, MarkerData> PENDING_MARKERS = new ConcurrentHashMap<>();

    // Track last cleanup time to prevent memory leak from orphaned markers
    private static long lastCleanupTime = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 60000; // Clean up every 60 seconds
    private static final long MARKER_EXPIRY_MS = 300000; // Expire markers after 5 minutes

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
        boolean isBossRoomMin = "boss_room_min".equals(markerType);
        boolean isBossRoomMax = "boss_room_max".equals(markerType);
        boolean isPermanentMin = "permanent_protection_min".equals(markerType);
        boolean isPermanentMax = "permanent_protection_max".equals(markerType);

        if (isBossRoomMin || isBossRoomMax) {
            // Boss room protection (temporary, removed when boss defeated)
            PENDING_MARKERS.put(worldPos.immutable(), new MarkerData(worldPos, isBossRoomMin, false));
            Chronosphere.LOGGER.debug("Stored boss room {} marker at {} for later registration",
                isBossRoomMin ? "min" : "max", worldPos);
        } else if (isPermanentMin || isPermanentMax) {
            // Permanent protection (never removed)
            PENDING_MARKERS.put(worldPos.immutable(), new MarkerData(worldPos, isPermanentMin, true));
            Chronosphere.LOGGER.debug("Stored permanent protection {} marker at {} for later registration",
                isPermanentMin ? "min" : "max", worldPos);
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

        // Cleanup expired markers periodically to prevent memory leak
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            cleanupExpiredMarkers(currentTime);
            lastCleanupTime = currentTime;
        }

        // Separate min and max markers by type (boss room vs permanent)
        // ONLY process markers in loaded chunks to avoid checking unloaded dimensions
        List<MarkerData> bossRoomMinMarkers = new ArrayList<>();
        List<MarkerData> bossRoomMaxMarkers = new ArrayList<>();
        List<MarkerData> permanentMinMarkers = new ArrayList<>();
        List<MarkerData> permanentMaxMarkers = new ArrayList<>();

        // Optimization: Batch process to limit work per tick
        int processedCount = 0;
        final int MAX_PROCESS_PER_CALL = 20; // Limit processing to prevent lag

        for (var entry : PENDING_MARKERS.entrySet()) {
            // Limit work per call to prevent lag spikes
            if (processedCount >= MAX_PROCESS_PER_CALL) {
                break;
            }

            MarkerData marker = entry.getValue();

            // Quick check: Is chunk loaded in this dimension?
            // This is faster than full hasChunkAt() as it checks the chunk map cache
            if (!level.hasChunk(marker.pos.getX() >> 4, marker.pos.getZ() >> 4)) {
                continue;
            }

            processedCount++;

            // Separate by type and min/max
            if (marker.isPermanent) {
                if (marker.isMin) {
                    permanentMinMarkers.add(marker);
                } else {
                    permanentMaxMarkers.add(marker);
                }
            } else {
                if (marker.isMin) {
                    bossRoomMinMarkers.add(marker);
                } else {
                    bossRoomMaxMarkers.add(marker);
                }
            }
        }

        // Pair min and max markers
        List<BlockPos> processedMarkers = new ArrayList<>();

        // Process boss room markers (temporary protection)
        for (MarkerData minMarker : bossRoomMinMarkers) {
            // Find closest max marker (should be in same structure)
            MarkerData closestMax = null;
            double closestDistance = Double.MAX_VALUE;

            for (MarkerData maxMarker : bossRoomMaxMarkers) {
                double distance = minMarker.pos.distSqr(maxMarker.pos);

                // Reasonable distance check: boss rooms are typically < 100 blocks across
                // Distance squared < 100^2 = 10000 (in any dimension)
                if (distance < 10000 && distance < closestDistance) {
                    closestMax = maxMarker;
                    closestDistance = distance;
                }
            }

            if (closestMax != null) {
                // Register boss room protection (temporary)
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

                // Also register with Clockwork Colossus spawner if this is Clockwork Depths
                // Check if the bounding box is within reasonable size for engine room (typically ~30x30x16)
                int width = bossRoomArea.maxX() - bossRoomArea.minX();
                int depth = bossRoomArea.maxZ() - bossRoomArea.minZ();
                int height = bossRoomArea.maxY() - bossRoomArea.minY();

                // Clockwork Depths engine room is roughly 30x30x16
                if (width >= 20 && width <= 40 && depth >= 20 && depth <= 40 && height >= 10 && height <= 25) {
                    ClockworkColossusSpawner.registerEngineRoom(level, bossRoomArea);
                }

                Chronosphere.LOGGER.info(
                    "Registered boss room protection in dimension {}: min={}, max={}, bounds={}",
                    level.dimension().location(), minPos, maxPos, bossRoomArea
                );

                // Mark for removal
                processedMarkers.add(minPos);
                processedMarkers.add(maxPos);

                // Remove max marker from list to prevent re-pairing
                bossRoomMaxMarkers.remove(closestMax);
            } else {
                Chronosphere.LOGGER.warn("Found boss room min marker at {} but no matching max marker within 100 blocks",
                    minMarker.pos);
            }
        }

        // Process permanent protection markers
        for (MarkerData minMarker : permanentMinMarkers) {
            // Find closest max marker (should be in same structure)
            MarkerData closestMax = null;
            double closestDistance = Double.MAX_VALUE;

            for (MarkerData maxMarker : permanentMaxMarkers) {
                double distance = minMarker.pos.distSqr(maxMarker.pos);

                // Reasonable distance check: structures are typically < 100 blocks across
                // Distance squared < 100^2 = 10000 (in any dimension)
                if (distance < 10000 && distance < closestDistance) {
                    closestMax = maxMarker;
                    closestDistance = distance;
                }
            }

            if (closestMax != null) {
                // Register permanent protection
                BlockPos minPos = minMarker.pos;
                BlockPos maxPos = closestMax.pos;

                BoundingBox protectedArea = new BoundingBox(
                    Math.min(minPos.getX(), maxPos.getX()),
                    Math.min(minPos.getY(), maxPos.getY()),
                    Math.min(minPos.getZ(), maxPos.getZ()),
                    Math.max(minPos.getX(), maxPos.getX()),
                    Math.max(minPos.getY(), maxPos.getY()),
                    Math.max(minPos.getZ(), maxPos.getZ())
                );

                PermanentProtectionHandler.registerProtectedArea(level, protectedArea, minPos);

                Chronosphere.LOGGER.info(
                    "Registered permanent protection in dimension {}: min={}, max={}, bounds={}",
                    level.dimension().location(), minPos, maxPos, protectedArea
                );

                // Mark for removal
                processedMarkers.add(minPos);
                processedMarkers.add(maxPos);

                // Remove max marker from list to prevent re-pairing
                permanentMaxMarkers.remove(closestMax);
            } else {
                Chronosphere.LOGGER.warn("Found permanent protection min marker at {} but no matching max marker within 100 blocks",
                    minMarker.pos);
            }
        }

        // Remove processed markers
        processedMarkers.forEach(PENDING_MARKERS::remove);

        // Log remaining unpaired markers (for debugging)
        if (!PENDING_MARKERS.isEmpty() && (!bossRoomMinMarkers.isEmpty() || !permanentMinMarkers.isEmpty())) {
            Chronosphere.LOGGER.warn("Still have {} unpaired markers after registration", PENDING_MARKERS.size());
        }
    }

    /**
     * Clean up expired markers to prevent memory leak.
     * Removes markers that have been pending for more than MARKER_EXPIRY_MS milliseconds.
     *
     * @param currentTime Current system time in milliseconds
     */
    private static void cleanupExpiredMarkers(long currentTime) {
        int removedCount = 0;
        Iterator<Map.Entry<BlockPos, MarkerData>> iterator = PENDING_MARKERS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, MarkerData> entry = iterator.next();
            MarkerData marker = entry.getValue();

            if (currentTime - marker.addedTime > MARKER_EXPIRY_MS) {
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            Chronosphere.LOGGER.info("Cleaned up {} expired boss room markers (older than {} seconds)",
                removedCount, MARKER_EXPIRY_MS / 1000);
        }
    }
}
