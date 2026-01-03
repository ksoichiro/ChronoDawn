package com.chronodawn.worldgen.processors;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.worldgen.protection.BlockProtectionHandler;
import com.chronodawn.worldgen.protection.PermanentProtectionHandler;
import com.chronodawn.worldgen.spawning.ClockworkColossusSpawner;
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
        final long placementId; // Group ID for pairing (same structure = same placementId)
        final BlockPos structureRelativePos; // Structure-relative position for deterministic sorting

        MarkerData(BlockPos pos, boolean isMin, boolean isPermanent, long placementId, BlockPos structureRelativePos) {
            this.pos = pos.immutable();
            this.isMin = isMin;
            this.isPermanent = isPermanent;
            this.addedTime = System.currentTimeMillis();
            this.placementId = placementId;
            this.structureRelativePos = structureRelativePos;
        }
    }

    // Pending markers to process during server tick
    // Key: marker world position (unique), Value: marker data
    // Note: All dimensions share same map, but registration filters by loaded chunks
    private static final Map<BlockPos, MarkerData> PENDING_MARKERS = new ConcurrentHashMap<>();

    // Track paired markers to detect orphaned markers when overwriting
    // Key: marker position, Value: paired marker position (bidirectional)
    // Example: minPos → maxPos and maxPos → minPos
    private static final Map<BlockPos, BlockPos> PAIRED_MARKERS = new ConcurrentHashMap<>();

    // Track last cleanup time to prevent memory leak from orphaned markers
    private static long lastCleanupTime = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 60000; // Clean up every 60 seconds
    private static final long MARKER_EXPIRY_MS = 300000; // Expire markers after 5 minutes

    // ThreadLocal storage for placementId to group markers from same structure placement
    // Reuses same ID if called within PLACEMENT_ID_REUSE_WINDOW_MS milliseconds
    private static final ThreadLocal<PlacementIdContext> PLACEMENT_ID_CONTEXT = ThreadLocal.withInitial(PlacementIdContext::new);
    private static final long PLACEMENT_ID_REUSE_WINDOW_MS = 1000; // Reuse ID within 1000ms window (1 second)

    private static class PlacementIdContext {
        long lastPlacementId;
        long lastUpdateTime;
        BlockPos lastMarkerPos; // Track last marker position to distinguish structures

        /**
         * Generate or reuse placementId for marker grouping.
         * Uses distance-based heuristic: markers < 100 blocks apart are from same structure.
         * This ensures min/max markers from same structure get same placementId.
         */
        long getOrCreatePlacementId(BlockPos currentMarkerPos) {
            long currentTime = System.currentTimeMillis();

            // Check if this marker is from same structure as previous marker
            boolean isSameStructure = false;
            if (lastMarkerPos != null && currentTime - lastUpdateTime <= PLACEMENT_ID_REUSE_WINDOW_MS) {
                // Same structure: min/max markers are close together (< 100 blocks)
                // Different structures: typically > 200 blocks apart
                // Using 100 blocks ensures min/max from same structure get same placementId
                double distance = Math.sqrt(lastMarkerPos.distSqr(currentMarkerPos));
                isSameStructure = distance < 100.0;
            }

            if (isSameStructure) {
                // Reuse same ID for markers from same structure
                return lastPlacementId;
            }

            // Create new unique ID for new structure
            // Combine timestamp, nanoTime, and position hash for uniqueness
            long positionHash = (long)currentMarkerPos.hashCode() & 0xFFFFFFFFL;
            lastPlacementId = (currentTime << 32) | (System.nanoTime() & 0xFFFF0000L) | (positionHash & 0xFFFFL);
            lastUpdateTime = currentTime;
            lastMarkerPos = currentMarkerPos.immutable();
            return lastPlacementId;
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return com.chronodawn.registry.ModStructureProcessorTypes.BOSS_ROOM_PROTECTION.get();
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
            ChronoDawn.LOGGER.warn("Boss Room Boundary Marker at {} has no NBT data", worldPos);
            return currentBlockInfo;
        }

        String markerType = nbt.getString("MarkerType");
        String replaceWith = nbt.getString("ReplaceWith");

        if (markerType.isEmpty() || replaceWith.isEmpty()) {
            ChronoDawn.LOGGER.warn("Boss Room Boundary Marker at {} has incomplete NBT (MarkerType={}, ReplaceWith={})",
                worldPos, markerType, replaceWith);
            return currentBlockInfo;
        }

        // Store marker data for later pairing
        boolean isBossRoomMin = "boss_room_min".equals(markerType);
        boolean isBossRoomMax = "boss_room_max".equals(markerType);
        boolean isPermanentMin = "permanent_protection_min".equals(markerType);
        boolean isPermanentMax = "permanent_protection_max".equals(markerType);

        // Get or create placementId for grouping markers from same structure placement
        // PlacementId includes marker position to distinguish different structures
        long placementId = PLACEMENT_ID_CONTEXT.get().getOrCreatePlacementId(worldPos);

        // Calculate structure-relative position for debugging
        BlockPos relativePos = worldPos.subtract(piecePos);

        if (isBossRoomMin || isBossRoomMax) {
            // Boss room protection (temporary, removed when boss defeated)
            BlockPos immutablePos = worldPos.immutable();
            MarkerData existing = PENDING_MARKERS.put(immutablePos, new MarkerData(worldPos, isBossRoomMin, false, placementId, relativePos));
            if (existing != null) {
                long ageMs = System.currentTimeMillis() - existing.addedTime;

                // Check if overwritten marker was already paired
                BlockPos pairedWith = PAIRED_MARKERS.get(immutablePos);
                if (pairedWith != null) {
                    // CRITICAL: Overwriting a paired marker orphans its pair
                    ChronoDawn.LOGGER.error("OVERWRITE PAIRED MARKER: Existing boss room {} marker at {} (old placementId={}, age={}ms) was ALREADY PAIRED with {} - This will ORPHAN the paired marker!",
                        existing.isMin ? "min" : "max", worldPos, existing.placementId, ageMs, pairedWith);
                    // Remove bidirectional pairing since one side is being overwritten
                    PAIRED_MARKERS.remove(immutablePos);
                    PAIRED_MARKERS.remove(pairedWith);
                }
            }
        } else if (isPermanentMin || isPermanentMax) {
            // Permanent protection (never removed)
            BlockPos immutablePos = worldPos.immutable();
            MarkerData existing = PENDING_MARKERS.put(immutablePos, new MarkerData(worldPos, isPermanentMin, true, placementId, relativePos));
            if (existing != null) {
                long ageMs = System.currentTimeMillis() - existing.addedTime;

                // Check if overwritten marker was already paired
                BlockPos pairedWith = PAIRED_MARKERS.get(immutablePos);
                if (pairedWith != null) {
                    // CRITICAL: Overwriting a paired marker orphans its pair
                    ChronoDawn.LOGGER.error("OVERWRITE PAIRED MARKER: Existing permanent protection {} marker at {} (old placementId={}, age={}ms) was ALREADY PAIRED with {} - This will ORPHAN the paired marker!",
                        existing.isMin ? "min" : "max", worldPos, existing.placementId, ageMs, pairedWith);
                    // Remove bidirectional pairing since one side is being overwritten
                    PAIRED_MARKERS.remove(immutablePos);
                    PAIRED_MARKERS.remove(pairedWith);
                }
            }
        }

        // Replace marker with specified block
        var replacementBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
            net.minecraft.resources.ResourceLocation.parse(replaceWith)
        );
        var replacementState = replacementBlock.defaultBlockState();

        ChronoDawn.LOGGER.debug("Replacing boss room marker at {} with {}", worldPos, replacementState);

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
        // Filter to only process Chrono Dawn dimension
        // Boss Room Boundary Markers only exist in Chrono Dawn dimension
        if (!level.dimension().equals(com.chronodawn.registry.ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

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

        // Group boss room markers by placementId for order-based pairing
        Map<Long, List<MarkerData>> bossRoomMinsByPlacement = new HashMap<>();
        Map<Long, List<MarkerData>> bossRoomMaxsByPlacement = new HashMap<>();

        for (MarkerData min : bossRoomMinMarkers) {
            bossRoomMinsByPlacement.computeIfAbsent(min.placementId, k -> new ArrayList<>()).add(min);
        }
        for (MarkerData max : bossRoomMaxMarkers) {
            bossRoomMaxsByPlacement.computeIfAbsent(max.placementId, k -> new ArrayList<>()).add(max);
        }

        // Process boss room markers (temporary protection) by placement group
        for (Long placementId : bossRoomMinsByPlacement.keySet()) {
            List<MarkerData> mins = bossRoomMinsByPlacement.get(placementId);
            List<MarkerData> maxs = bossRoomMaxsByPlacement.get(placementId);

            if (maxs == null) {
                ChronoDawn.LOGGER.warn("Found {} boss room min markers with placementId={} but no max markers", mins.size(), placementId);
                continue;
            }

            // Remove duplicate markers (same position) - keep the newer one
            mins = removeDuplicateMarkers(mins);
            maxs = removeDuplicateMarkers(maxs);

            // Sort both lists by structure-relative Y coordinate (descending)
            // This ensures correct pairing even when ConcurrentHashMap extraction order is random
            mins.sort(Comparator.comparingInt((MarkerData m) -> m.structureRelativePos.getY()).reversed());
            maxs.sort(Comparator.comparingInt((MarkerData m) -> m.structureRelativePos.getY()).reversed());

            // Pair markers in order
            int pairCount = Math.min(mins.size(), maxs.size());
            for (int i = 0; i < pairCount; i++) {
                MarkerData minMarker = mins.get(i);
                MarkerData maxMarker = maxs.get(i);

                // Register boss room protection (temporary)
                BlockPos minPos = minMarker.pos;
                BlockPos maxPos = maxMarker.pos;

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

                double distance = Math.sqrt(minMarker.pos.distSqr(maxMarker.pos));
                ChronoDawn.LOGGER.info(
                    "Registered boss room protection in dimension {}: min={} (placementId={}), max={} (placementId={}), distance={:.1f} blocks, bounds={}",
                    level.dimension().location(), minPos, minMarker.placementId, maxPos, maxMarker.placementId, distance, bossRoomArea
                );

                // Record pairing (bidirectional) to detect orphaned markers
                PAIRED_MARKERS.put(minPos, maxPos);
                PAIRED_MARKERS.put(maxPos, minPos);

                // Mark for removal
                processedMarkers.add(minPos);
                processedMarkers.add(maxPos);
            }

            // Warn about unpaired markers
            if (mins.size() > maxs.size()) {
                for (int i = pairCount; i < mins.size(); i++) {
                    ChronoDawn.LOGGER.warn("Found boss room min marker at {} (placementId={}) but no matching max marker",
                        mins.get(i).pos, placementId);
                }
            } else if (maxs.size() > mins.size()) {
                for (int i = pairCount; i < maxs.size(); i++) {
                    ChronoDawn.LOGGER.warn("Found boss room max marker at {} (placementId={}) but no matching min marker",
                        maxs.get(i).pos, placementId);
                }
            }
        }

        // Group markers by placementId for order-based pairing
        Map<Long, List<MarkerData>> minMarkersByPlacement = new HashMap<>();
        Map<Long, List<MarkerData>> maxMarkersByPlacement = new HashMap<>();

        for (MarkerData min : permanentMinMarkers) {
            minMarkersByPlacement.computeIfAbsent(min.placementId, k -> new ArrayList<>()).add(min);
        }
        for (MarkerData max : permanentMaxMarkers) {
            maxMarkersByPlacement.computeIfAbsent(max.placementId, k -> new ArrayList<>()).add(max);
        }

        // Process permanent protection markers by placement group
        for (Long placementId : minMarkersByPlacement.keySet()) {
            List<MarkerData> mins = minMarkersByPlacement.get(placementId);
            List<MarkerData> maxs = maxMarkersByPlacement.get(placementId);

            if (maxs == null) {
                ChronoDawn.LOGGER.warn("Found {} min markers with placementId={} but no max markers", mins.size(), placementId);
                continue;
            }

            // Remove duplicate markers (same position) - keep the newer one (higher addedTime)
            // This handles cases where stairs_bottom overwrites stairs max marker
            mins = removeDuplicateMarkers(mins);
            maxs = removeDuplicateMarkers(maxs);

            // Sort both lists by structure-relative Y coordinate (descending)
            // This ensures min[0]→max[0], min[1]→max[1] pairing based on NBT structure position
            // Works correctly even when ConcurrentHashMap extraction order is random
            mins.sort(Comparator.comparingInt((MarkerData m) -> m.structureRelativePos.getY()).reversed());
            maxs.sort(Comparator.comparingInt((MarkerData m) -> m.structureRelativePos.getY()).reversed());

            // Pair markers in order
            int pairCount = Math.min(mins.size(), maxs.size());
            for (int i = 0; i < pairCount; i++) {
                MarkerData minMarker = mins.get(i);
                MarkerData maxMarker = maxs.get(i);

                // Register permanent protection
                BlockPos minPos = minMarker.pos;
                BlockPos maxPos = maxMarker.pos;

                BoundingBox protectedArea = new BoundingBox(
                    Math.min(minPos.getX(), maxPos.getX()),
                    Math.min(minPos.getY(), maxPos.getY()),
                    Math.min(minPos.getZ(), maxPos.getZ()),
                    Math.max(minPos.getX(), maxPos.getX()),
                    Math.max(minPos.getY(), maxPos.getY()),
                    Math.max(minPos.getZ(), maxPos.getZ())
                );

                PermanentProtectionHandler.registerProtectedArea(level, protectedArea, minPos);

                double distance = Math.sqrt(minMarker.pos.distSqr(maxMarker.pos));
                ChronoDawn.LOGGER.info(
                    "Registered permanent protection in dimension {}: min={} (placementId={}), max={} (placementId={}), distance={:.1f} blocks, bounds={}",
                    level.dimension().location(), minPos, minMarker.placementId, maxPos, maxMarker.placementId, distance, protectedArea
                );

                // Record pairing (bidirectional) to detect orphaned markers
                PAIRED_MARKERS.put(minPos, maxPos);
                PAIRED_MARKERS.put(maxPos, minPos);

                // Mark for removal
                processedMarkers.add(minPos);
                processedMarkers.add(maxPos);
            }

            // Warn about unpaired markers
            if (mins.size() > maxs.size()) {
                for (int i = pairCount; i < mins.size(); i++) {
                    ChronoDawn.LOGGER.warn("Found permanent protection min marker at {} (placementId={}) but no matching max marker",
                        mins.get(i).pos, placementId);
                }
            } else if (maxs.size() > mins.size()) {
                for (int i = pairCount; i < maxs.size(); i++) {
                    ChronoDawn.LOGGER.warn("Found permanent protection max marker at {} (placementId={}) but no matching min marker",
                        maxs.get(i).pos, placementId);
                }
            }
        }

        // Remove processed markers
        processedMarkers.forEach(pos -> {
            PENDING_MARKERS.remove(pos);
            // Also remove from paired markers since they are successfully processed
            PAIRED_MARKERS.remove(pos);
        });

        // Log remaining unpaired markers (for debugging)
        if (!PENDING_MARKERS.isEmpty() && (!bossRoomMinMarkers.isEmpty() || !permanentMinMarkers.isEmpty())) {
            ChronoDawn.LOGGER.warn("Still have {} unpaired markers after registration", PENDING_MARKERS.size());
        }
    }

    /**
     * Remove duplicate markers at the same position.
     * Keeps the marker with the highest addedTime (most recent).
     * This handles cases where stairs_bottom overwrites stairs markers.
     */
    private static List<MarkerData> removeDuplicateMarkers(List<MarkerData> markers) {
        Map<BlockPos, MarkerData> uniqueMarkers = new HashMap<>();

        for (MarkerData marker : markers) {
            MarkerData existing = uniqueMarkers.get(marker.pos);
            if (existing == null || marker.addedTime > existing.addedTime) {
                // Keep the newer marker
                uniqueMarkers.put(marker.pos, marker);
                if (existing != null) {
                    ChronoDawn.LOGGER.info("Removed duplicate marker at {} (kept newer one, time diff={}ms)",
                        marker.pos, marker.addedTime - existing.addedTime);
                }
            }
        }

        return new ArrayList<>(uniqueMarkers.values());
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

            long age = currentTime - marker.addedTime;
            if (age > MARKER_EXPIRY_MS) {
                ChronoDawn.LOGGER.info("Removing expired marker at {} (placementId={}, age={}s, type={})",
                    marker.pos, marker.placementId, age / 1000,
                    marker.isPermanent ? "permanent" : "boss_room");
                iterator.remove();
                // Also remove from paired markers
                PAIRED_MARKERS.remove(marker.pos);
                removedCount++;
            }
        }

        if (removedCount > 0) {
            ChronoDawn.LOGGER.info("Cleaned up {} expired boss room markers (older than {} seconds)",
                removedCount, MARKER_EXPIRY_MS / 1000);
        }
    }

    /**
     * Process and clear ALL pending markers immediately after programmatic structure placement.
     *
     * This method is designed for programmatic structure placement (e.g., Master Clock),
     * where each structure is placed sequentially and we can process markers immediately
     * after each placement. This eliminates the need for complex placementId grouping,
     * distance checks, and sorting logic.
     *
     * Assumptions:
     * - All markers in PENDING_MARKERS are from the same structure placement
     * - Markers are already in correct pairing order (min[0]↔max[0], min[1]↔max[1], etc.)
     * - Called immediately after structure placement (chunk is guaranteed loaded)
     *
     * Usage:
     * 1. Place structure with template.placeInWorld()
     * 2. Finalize waterlogging (if needed)
     * 3. Call processPendingMarkersImmediate(level) to register protection
     * 4. Repeat for next structure
     *
     * @param level Server level where protection should be registered
     */
    public static void processPendingMarkersImmediate(ServerLevel level) {
        if (PENDING_MARKERS.isEmpty()) {
            return;
        }

        // Separate min and max markers by type (boss room vs permanent)
        List<MarkerData> bossRoomMinMarkers = new ArrayList<>();
        List<MarkerData> bossRoomMaxMarkers = new ArrayList<>();
        List<MarkerData> permanentMinMarkers = new ArrayList<>();
        List<MarkerData> permanentMaxMarkers = new ArrayList<>();

        for (MarkerData marker : PENDING_MARKERS.values()) {
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

        // Process boss room markers (simple order-based pairing)
        int bossRoomPairCount = Math.min(bossRoomMinMarkers.size(), bossRoomMaxMarkers.size());
        for (int i = 0; i < bossRoomPairCount; i++) {
            MarkerData minMarker = bossRoomMinMarkers.get(i);
            MarkerData maxMarker = bossRoomMaxMarkers.get(i);

            BlockPos minPos = minMarker.pos;
            BlockPos maxPos = maxMarker.pos;

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
            int width = bossRoomArea.maxX() - bossRoomArea.minX();
            int depth = bossRoomArea.maxZ() - bossRoomArea.minZ();
            int height = bossRoomArea.maxY() - bossRoomArea.minY();

            if (width >= 20 && width <= 40 && depth >= 20 && depth <= 40 && height >= 10 && height <= 25) {
                ClockworkColossusSpawner.registerEngineRoom(level, bossRoomArea);
            }

            // Record pairing
            PAIRED_MARKERS.put(minPos, maxPos);
            PAIRED_MARKERS.put(maxPos, minPos);
        }

        // Warn about unpaired boss room markers
        if (bossRoomMinMarkers.size() != bossRoomMaxMarkers.size()) {
            ChronoDawn.LOGGER.warn("[IMMEDIATE] Boss room marker count mismatch: {} min, {} max",
                bossRoomMinMarkers.size(), bossRoomMaxMarkers.size());
        }

        // Process permanent protection markers (simple order-based pairing)
        int permanentPairCount = Math.min(permanentMinMarkers.size(), permanentMaxMarkers.size());
        for (int i = 0; i < permanentPairCount; i++) {
            MarkerData minMarker = permanentMinMarkers.get(i);
            MarkerData maxMarker = permanentMaxMarkers.get(i);

            BlockPos minPos = minMarker.pos;
            BlockPos maxPos = maxMarker.pos;

            BoundingBox protectedArea = new BoundingBox(
                Math.min(minPos.getX(), maxPos.getX()),
                Math.min(minPos.getY(), maxPos.getY()),
                Math.min(minPos.getZ(), maxPos.getZ()),
                Math.max(minPos.getX(), maxPos.getX()),
                Math.max(minPos.getY(), maxPos.getY()),
                Math.max(minPos.getZ(), maxPos.getZ())
            );

            PermanentProtectionHandler.registerProtectedArea(level, protectedArea, minPos);

            // Record pairing
            PAIRED_MARKERS.put(minPos, maxPos);
            PAIRED_MARKERS.put(maxPos, minPos);
        }

        // Warn about unpaired permanent markers
        if (permanentMinMarkers.size() != permanentMaxMarkers.size()) {
            ChronoDawn.LOGGER.warn("[IMMEDIATE] Permanent marker count mismatch: {} min, {} max",
                permanentMinMarkers.size(), permanentMaxMarkers.size());
        }

        // CRITICAL: Clear ALL pending markers after processing
        PENDING_MARKERS.clear();

        ChronoDawn.LOGGER.info("[IMMEDIATE] Processed {} boss room pairs, {} permanent pairs, cleared all pending markers",
            bossRoomPairCount, permanentPairCount);
    }
}
