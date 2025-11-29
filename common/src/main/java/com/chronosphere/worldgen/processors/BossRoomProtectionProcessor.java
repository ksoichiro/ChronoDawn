package com.chronosphere.worldgen.processors;

import com.chronosphere.Chronosphere;
import com.chronosphere.blocks.BossRoomBoundaryMarkerBlockEntity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Boss Room Protection Processor
 *
 * Detects Boss Room Boundary Marker blocks during structure generation and:
 * 1. Collects min/max marker positions
 * 2. Calculates BoundingBox from marker positions
 * 3. Registers protection with BlockProtectionHandler
 * 4. Replaces markers with specified blocks
 *
 * Processing happens in two phases:
 * - Phase 1 (processBlock): Collect marker positions and replace blocks
 * - Phase 2 (finalizeProcessing): Register protection after all blocks processed
 *
 * Usage:
 * - Place Boss Room Boundary Marker blocks in structure NBT at boss room corners
 * - Add this processor to structure's processor_list
 * - Markers will be replaced with specified blocks during generation
 * - Boss room will be protected until boss is defeated
 *
 * Implementation: T224 - Boss room protection with marker blocks
 */
public class BossRoomProtectionProcessor extends StructureProcessor {
    public static final MapCodec<BossRoomProtectionProcessor> CODEC =
        MapCodec.unit(BossRoomProtectionProcessor::new);

    // Collected marker positions during processing
    private BlockPos minMarkerPos = null;
    private BlockPos maxMarkerPos = null;
    private boolean protectionRegistered = false;

    @Override
    protected StructureProcessorType<?> getType() {
        return com.chronosphere.registry.ModStructureProcessorTypes.BOSS_ROOM_PROTECTION.get();
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader level,
        BlockPos jigsawPos,
        BlockPos relativePos,
        StructureTemplate.StructureBlockInfo originalBlockInfo,
        StructureTemplate.StructureBlockInfo currentBlockInfo,
        StructurePlaceSettings settings
    ) {
        // Check if this is a Boss Room Boundary Marker block
        if (!currentBlockInfo.state().is(ModBlocks.BOSS_ROOM_BOUNDARY_MARKER.get())) {
            return currentBlockInfo;
        }

        // Get the BlockEntity to read marker type and replacement block
        BlockPos worldPos = currentBlockInfo.pos();
        var blockEntity = level.getBlockEntity(worldPos);

        if (!(blockEntity instanceof BossRoomBoundaryMarkerBlockEntity markerEntity)) {
            Chronosphere.LOGGER.warn("Boss Room Boundary Marker at {} has no BlockEntity", worldPos);
            return currentBlockInfo;
        }

        // Collect marker positions
        if (markerEntity.isMinMarker()) {
            minMarkerPos = worldPos.immutable();
            Chronosphere.LOGGER.info("Found boss room min marker at {}", minMarkerPos);
        } else if (markerEntity.isMaxMarker()) {
            maxMarkerPos = worldPos.immutable();
            Chronosphere.LOGGER.info("Found boss room max marker at {}", maxMarkerPos);
        }

        // Register protection if we have both markers (only once per structure)
        if (minMarkerPos != null && maxMarkerPos != null && !protectionRegistered && level instanceof ServerLevel serverLevel) {
            registerProtection(serverLevel);
        }

        // Replace marker with specified block
        var replacementState = markerEntity.getReplacementState();
        Chronosphere.LOGGER.info("Replacing boss room marker at {} with {}", worldPos, replacementState);

        return new StructureTemplate.StructureBlockInfo(
            worldPos,
            replacementState,
            null // No block entity for replacement block
        );
    }

    /**
     * Register boss room protection with BlockProtectionHandler.
     */
    private void registerProtection(ServerLevel level) {
        if (minMarkerPos == null || maxMarkerPos == null) {
            Chronosphere.LOGGER.error("Cannot register boss room protection: missing marker positions");
            return;
        }

        // Create BoundingBox from marker positions
        BoundingBox bossRoomArea = new BoundingBox(
            Math.min(minMarkerPos.getX(), maxMarkerPos.getX()),
            Math.min(minMarkerPos.getY(), maxMarkerPos.getY()),
            Math.min(minMarkerPos.getZ(), maxMarkerPos.getZ()),
            Math.max(minMarkerPos.getX(), maxMarkerPos.getX()),
            Math.max(minMarkerPos.getY(), maxMarkerPos.getY()),
            Math.max(minMarkerPos.getZ(), maxMarkerPos.getZ())
        );

        // Use the min marker position as the unique ID for this boss room
        BlockProtectionHandler.registerProtectedArea(level, bossRoomArea, minMarkerPos);

        Chronosphere.LOGGER.info(
            "Registered boss room protection: min={}, max={}, bounds={}",
            minMarkerPos, maxMarkerPos, bossRoomArea
        );

        protectionRegistered = true;
    }
}
