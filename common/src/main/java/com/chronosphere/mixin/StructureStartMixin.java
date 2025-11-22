package com.chronosphere.mixin;

import com.chronosphere.Chronosphere;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to prevent waterlogging in Guardian Vault structure generation.
 *
 * This addresses the Minecraft 1.18+ Aquifer system issue where water exists
 * in the generation area and causes waterloggable blocks to become waterlogged.
 *
 * Strategy:
 * 1. Before structure placement (@At("HEAD")):
 *    - Record all water positions in structure piece bounding boxes
 *    - Temporarily remove all water (prevents waterlogging during placement)
 *    - Exclude top 5 blocks to preserve surface water
 *
 * 2. After structure placement (@At("RETURN")):
 *    - Log completion (no water restoration)
 *    - Positions after placement:
 *      a) Air/cave_air → Water successfully prevented waterlogging or NBT placed air
 *      b) Solid block → Structure placed block (waterlogging prevented!) or structure_void preserved terrain
 *      c) Water → Decorative water from NBT (already placed)
 *
 * This approach ensures:
 * - ✅ Waterloggable blocks are NOT waterlogged (water removed before placement)
 * - ✅ Decorative water from NBT is preserved (placed after water removal)
 * - ✅ Structure_void areas with solid blocks are preserved (stone, dirt, etc. remain)
 * - ⚠️ Structure_void areas with water become air (unavoidable limitation)
 * - ✅ Surface water is preserved (top 5 blocks excluded from processing)
 *
 * Important: Uses individual StructurePiece bounding boxes, not the entire structure
 * bounding box. This prevents affecting water in empty space between Jigsaw pieces.
 *
 * Task: T239 [US3] Guardian Vault structure generation
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
    @Shadow
    public abstract BoundingBox getBoundingBox();

    @Shadow
    private Structure structure;

    @Shadow
    public abstract java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> getPieces();

    // Store water positions before structure placement
    private final java.util.Set<BlockPos> waterPositionsBeforePlacement = new java.util.HashSet<>();

    /**
     * Inject before structure placement to record and remove existing water.
     *
     * This prevents waterloggable blocks from becoming waterlogged during placement.
     * We'll restore water in structure_void areas after placement.
     */
    @Inject(
        method = "placeInChunk",
        at = @At("HEAD")
    )
    private void removeWaterBeforePlacement(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator chunkGenerator,
        RandomSource random,
        BoundingBox chunkBox,
        ChunkPos chunkPos,
        CallbackInfo ci
    ) {
        // Get structure registry key to check if this is Guardian Vault
        net.minecraft.resources.ResourceLocation structureLocation =
            level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

        // Only apply to Guardian Vault structures
        if (structureLocation == null ||
            !structureLocation.getNamespace().equals(Chronosphere.MOD_ID) ||
            !structureLocation.getPath().equals("guardian_vault")) {
            return;
        }

        waterPositionsBeforePlacement.clear();

        java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> pieces = this.getPieces();

        // Iterate through each individual structure piece (Jigsaw parts)
        // This ensures we only process actual structure areas, not empty space
        // between disconnected pieces (e.g., between entrance and underground hall)
        for (net.minecraft.world.level.levelgen.structure.StructurePiece piece : pieces) {
            BoundingBox pieceBox = piece.getBoundingBox();

            // Record and remove water source blocks before structure placement
            // Exclude top 5 blocks of each piece to preserve surface water (lakes, oceans)
            int maxYToRecord = pieceBox.maxY() - 5;

            for (BlockPos pos : BlockPos.betweenClosed(
                pieceBox.minX(), pieceBox.minY(), pieceBox.minZ(),
                pieceBox.maxX(), maxYToRecord, pieceBox.maxZ()
            )) {
                BlockState state = level.getBlockState(pos);
                if (state.getFluidState().isSource() && state.getBlock() == Blocks.WATER) {
                    waterPositionsBeforePlacement.add(pos.immutable());
                    // Remove water temporarily to prevent waterlogging
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        if (!waterPositionsBeforePlacement.isEmpty()) {
            Chronosphere.LOGGER.info(
                "Temporarily removed {} water blocks before Guardian Vault placement (across {} pieces)",
                waterPositionsBeforePlacement.size(),
                pieces.size()
            );
        }
    }

    /**
     * Inject after structure placement to log completion.
     *
     * Strategy:
     * - Do NOT restore water in any positions
     * - Water was removed before placement to prevent waterlogging
     * - Decorative water from NBT is already placed by the structure
     * - structure_void areas with solid blocks (stone, dirt, etc.) are preserved
     * - structure_void areas with water will become air (unavoidable limitation)
     */
    @Inject(
        method = "placeInChunk",
        at = @At("RETURN")
    )
    private void logWaterloggingPrevention(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator chunkGenerator,
        RandomSource random,
        BoundingBox chunkBox,
        ChunkPos chunkPos,
        CallbackInfo ci
    ) {
        // Skip if we didn't record any water positions (not a Guardian Vault)
        if (waterPositionsBeforePlacement.isEmpty()) {
            return;
        }

        int totalWaterRemoved = waterPositionsBeforePlacement.size();
        Chronosphere.LOGGER.info(
            "Prevented waterlogging for {} positions in Guardian Vault (water removed before placement)",
            totalWaterRemoved
        );

        // Clear the set to free memory
        waterPositionsBeforePlacement.clear();
    }
}
