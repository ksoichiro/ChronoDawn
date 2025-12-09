package com.chronosphere.worldgen.processors;

import com.chronosphere.Chronosphere;
import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Master Clock Protection Processor
 *
 * Protects the entire Master Clock structure (not just boss room) to prevent bypassing
 * the Ancient Gears requirement by breaking walls.
 *
 * Detection Strategy:
 * - Detects characteristic Master Clock blocks (Chiseled Quartz Block, Polished Andesite)
 * - Registers large bounding box covering entire structure (±50 blocks XZ, Y=-70 to Y=10)
 * - Uses same protection system as boss rooms (unprotected when Time Tyrant defeated)
 *
 * Implementation: T302 - Prevent Master Clock wall bypass
 */
public class MasterClockProtectionProcessor extends StructureProcessor {
    public static final MapCodec<MasterClockProtectionProcessor> CODEC =
        MapCodec.unit(MasterClockProtectionProcessor::new);

    // Track processed structures to avoid duplicate registration
    // Key: structure center position (rounded to chunk), Value: timestamp
    private static final Map<BlockPos, Long> PROCESSED_STRUCTURES = new ConcurrentHashMap<>();
    private static final long STRUCTURE_TIMEOUT_MS = 10000; // 10 seconds

    // Pending registrations (structure center -> marker block position)
    private static final Map<BlockPos, BlockPos> PENDING_REGISTRATIONS = new ConcurrentHashMap<>();

    @Override
    protected StructureProcessorType<?> getType() {
        return com.chronosphere.registry.ModStructureProcessorTypes.MASTER_CLOCK_PROTECTION.get();
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
        // Detect characteristic Master Clock blocks
        // Master Clock uses distinctive blocks like Chiseled Quartz Block and Polished Andesite
        boolean isMasterClockBlock = currentBlockInfo.state().is(Blocks.CHISELED_QUARTZ_BLOCK) ||
                                     currentBlockInfo.state().is(Blocks.POLISHED_ANDESITE);

        if (isMasterClockBlock) {
            BlockPos worldPos = currentBlockInfo.pos();

            // Round to chunk coordinates to group nearby markers
            BlockPos chunkKey = new BlockPos(
                (worldPos.getX() >> 4) << 4,
                0,
                (worldPos.getZ() >> 4) << 4
            );

            // Check if we already processed this structure recently
            Long lastProcessed = PROCESSED_STRUCTURES.get(chunkKey);
            long currentTime = System.currentTimeMillis();

            if (lastProcessed == null || (currentTime - lastProcessed) > STRUCTURE_TIMEOUT_MS) {
                // Mark as processed
                PROCESSED_STRUCTURES.put(chunkKey, currentTime);

                // Store for registration during server tick
                PENDING_REGISTRATIONS.put(chunkKey, worldPos.immutable());

                Chronosphere.LOGGER.info("Detected Master Clock structure block at {}, scheduling protection registration", worldPos);
            }
        }

        return currentBlockInfo;
    }

    /**
     * Register pending Master Clock protections.
     * Should be called from server tick event.
     */
    public static void registerPendingProtections(ServerLevel level) {
        if (PENDING_REGISTRATIONS.isEmpty()) {
            return;
        }

        // Process up to 5 registrations per tick to avoid lag
        int processed = 0;
        final int MAX_PER_TICK = 5;

        var iterator = PENDING_REGISTRATIONS.entrySet().iterator();
        while (iterator.hasNext() && processed < MAX_PER_TICK) {
            var entry = iterator.next();
            BlockPos chunkKey = entry.getKey();
            BlockPos markerPos = entry.getValue();

            // Check if chunk is loaded
            if (!level.hasChunk(markerPos.getX() >> 4, markerPos.getZ() >> 4)) {
                continue;
            }

            // Register large bounding box covering entire structure
            // Master Clock structure dimensions (approximate):
            // - Surface: ~20x20 at Y=0
            // - Descends to boss room at Y=-60
            // Total coverage: ±50 blocks XZ, Y=-70 to Y=10 (to be safe)
            BoundingBox structureArea = new BoundingBox(
                markerPos.getX() - 50,
                -70,
                markerPos.getZ() - 50,
                markerPos.getX() + 50,
                10,
                markerPos.getZ() + 50
            );

            // Use chunk position as unique ID (same structure shares ID)
            BlockProtectionHandler.registerProtectedArea(level, structureArea, chunkKey);

            Chronosphere.LOGGER.info(
                "Registered Master Clock structure protection at {}: bounds={}",
                markerPos, structureArea
            );

            iterator.remove();
            processed++;
        }

        // Cleanup old processed structures periodically
        long currentTime = System.currentTimeMillis();
        PROCESSED_STRUCTURES.entrySet().removeIf(entry ->
            (currentTime - entry.getValue()) > STRUCTURE_TIMEOUT_MS * 3
        );
    }

    /**
     * Reset for testing.
     */
    public static void reset() {
        PENDING_REGISTRATIONS.clear();
        PROCESSED_STRUCTURES.clear();
    }
}
