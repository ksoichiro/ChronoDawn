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

import java.util.Set;

/**
 * Mixin to prevent waterlogging in underground structure generation.
 *
 * This addresses the Minecraft 1.18+ Aquifer system issue where water exists
 * in the generation area and causes waterloggable blocks to become waterlogged.
 *
 * Strategy:
 * 1. Before structure placement (@At("HEAD")):
 *    - Process each chunk separately (within chunkBox only)
 *    - Remove ONLY minecraft:water (Aquifer water)
 *    - Preserve chronosphere:decorative_water (custom fluid)
 *    - Decorative water is converted to minecraft:water by processor after placement
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
 *
 * Important: Uses individual StructurePiece bounding boxes, not the entire structure
 * bounding box. This prevents affecting water in empty space between Jigsaw pieces.
 *
 * Applied to structures: master_clock, guardian_vault, clockwork_depths, phantom_tower, entropy_crypt
 *
 * Task: T239 [US3] Guardian Vault structure generation
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
    // Structures that require waterlogging prevention
    private static final Set<String> WATERLOGGING_PREVENTION_STRUCTURES = Set.of(
        "master_clock",
        "guardian_vault",
        "clockwork_depths",
        "phantom_tower",
        "entropy_crypt"
    );

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
     * Processes each chunk separately, removing only Aquifer water (minecraft:water).
     * Decorative water (chronosphere:decorative_water) is preserved.
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
        // Get structure registry key to check if waterlogging prevention is needed
        net.minecraft.resources.ResourceLocation structureLocation =
            level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

        // Only apply to structures that need waterlogging prevention
        if (structureLocation == null ||
            !structureLocation.getNamespace().equals(Chronosphere.MOD_ID) ||
            !WATERLOGGING_PREVENTION_STRUCTURES.contains(structureLocation.getPath())) {
            return;
        }

        waterPositionsBeforePlacement.clear();

        java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> pieces = this.getPieces();

        int totalWaterRemoved = 0;

        // Iterate through each individual structure piece (Jigsaw parts)
        // Process only the intersection between piece and current chunk
        // This ensures we only process loaded chunks
        for (net.minecraft.world.level.levelgen.structure.StructurePiece piece : pieces) {
            BoundingBox pieceBox = piece.getBoundingBox();

            // Only process the intersection between piece and current chunk
            if (!pieceBox.intersects(chunkBox)) {
                continue;
            }

            // Calculate intersection area
            int minX = Math.max(pieceBox.minX(), chunkBox.minX());
            int minY = Math.max(pieceBox.minY(), chunkBox.minY());
            int minZ = Math.max(pieceBox.minZ(), chunkBox.minZ());
            int maxX = Math.min(pieceBox.maxX(), chunkBox.maxX());
            int maxY = Math.min(pieceBox.maxY(), chunkBox.maxY());
            int maxZ = Math.min(pieceBox.maxZ(), chunkBox.maxZ());

            // Record and remove ONLY vanilla water (Aquifer water) within current chunk
            // Preserve chronosphere:decorative_water (will be converted to water by processor)
            for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
                BlockState state = level.getBlockState(pos);
                // Only remove minecraft:water (Aquifer water), not chronosphere:decorative_water
                if (state.getFluidState().isSource() &&
                    state.getBlock() == Blocks.WATER &&
                    !state.is(com.chronosphere.registry.ModBlocks.DECORATIVE_WATER.get())) {
                    waterPositionsBeforePlacement.add(pos.immutable());
                    // Remove water temporarily to prevent waterlogging
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    totalWaterRemoved++;
                }
            }
        }

        if (totalWaterRemoved > 0) {
            Chronosphere.LOGGER.info(
                "Removed {} Aquifer water blocks in chunk {} for {}",
                totalWaterRemoved,
                chunkPos,
                structureLocation.getPath()
            );
        }
    }

    /**
     * Inject after structure placement to finalize waterlogging and convert decorative water.
     *
     * This runs after blocks in the current chunk have been placed and processed.
     *
     * IMPORTANT: For multi-chunk structures, this method is called separately for each chunk.
     * To prevent waterlogging from later chunks affecting earlier chunks, we process blocks
     * in the current chunk PLUS a 1-block border into adjacent chunks.
     *
     * At this point:
     * 1. CopyFluidLevelProcessor has recorded positions with intentional waterlogging
     * 2. CopyFluidLevelProcessor has removed all waterlogged states to prevent water spread
     * 3. Some blocks may have been re-waterlogged by water placement in this or adjacent chunks
     *
     * This method:
     * 1. Restores intentional waterlogging (from NBT)
     * 2. Removes unintentional waterlogging (from Aquifer water or water spread)
     * 3. Converts any remaining decorative water to vanilla water
     * 4. Re-processes adjacent chunk borders to fix waterlogging from this chunk's water
     */
    @Inject(
        method = "placeInChunk",
        at = @At("RETURN")
    )
    private void convertDecorativeWaterAfterPlacement(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator chunkGenerator,
        RandomSource random,
        BoundingBox chunkBox,
        ChunkPos chunkPos,
        CallbackInfo ci
    ) {
        // Get structure registry key to check if waterlogging prevention is needed
        net.minecraft.resources.ResourceLocation structureLocation =
            level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

        // Only apply to structures that need waterlogging prevention
        if (structureLocation == null ||
            !structureLocation.getNamespace().equals(Chronosphere.MOD_ID) ||
            !WATERLOGGING_PREVENTION_STRUCTURES.contains(structureLocation.getPath())) {
            waterPositionsBeforePlacement.clear();
            return;
        }

        java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> pieces = this.getPieces();

        int totalDecorativeWaterConverted = 0;
        int totalWaterloggedRestored = 0;
        int totalWaterloggedRemoved = 0;

        // Process each structure piece within current chunk
        for (net.minecraft.world.level.levelgen.structure.StructurePiece piece : pieces) {
            BoundingBox pieceBox = piece.getBoundingBox();

            // Only process the intersection between piece and current chunk
            if (!pieceBox.intersects(chunkBox)) {
                continue;
            }

            // Calculate intersection area
            // Expand by 1 block in X/Z directions to catch blocks in adjacent chunks
            // that may have been waterlogged by this chunk's water placement
            // This fixes the issue where earlier chunks are waterlogged by later chunks
            int minX = Math.max(pieceBox.minX(), chunkBox.minX() - 1);
            int minY = Math.max(pieceBox.minY(), chunkBox.minY());
            int minZ = Math.max(pieceBox.minZ(), chunkBox.minZ() - 1);
            int maxX = Math.min(pieceBox.maxX(), chunkBox.maxX() + 1);
            int maxY = Math.min(pieceBox.maxY(), chunkBox.maxY());
            int maxZ = Math.min(pieceBox.maxZ(), chunkBox.maxZ() + 1);

            // Finalize waterlogging and convert decorative water within current chunk
            // (and 1-block border into adjacent chunks)
            for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
                BlockState state = level.getBlockState(pos);

                // 1. Convert decorative water to vanilla water (preserving flow state)
                if (state.is(com.chronosphere.registry.ModBlocks.DECORATIVE_WATER.get())) {
                    BlockState newState = Blocks.WATER.defaultBlockState();

                    // Preserve fluid level if present
                    if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL)) {
                        int fluidLevel = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL);
                        newState = newState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL, fluidLevel);
                    }

                    level.setBlock(pos, newState, 2);
                    totalDecorativeWaterConverted++;
                }
                // 2. Finalize waterlogging state
                else if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                    boolean currentWaterlogged = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
                    // Use immutable() to match the Set (which stores immutable positions)
                    BlockPos immutablePos = pos.immutable();
                    boolean shouldBeWaterlogged = com.chronosphere.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.contains(immutablePos);

                    if (currentWaterlogged != shouldBeWaterlogged) {
                        BlockState newState = state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, shouldBeWaterlogged);
                        level.setBlock(pos, newState, 2);

                        if (shouldBeWaterlogged) {
                            totalWaterloggedRestored++;
                            // Remove from set after restoring (cleanup)
                            com.chronosphere.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.remove(immutablePos);
                        } else {
                            totalWaterloggedRemoved++;
                        }
                    } else if (shouldBeWaterlogged) {
                        // Already correctly waterlogged, remove from set (cleanup)
                        com.chronosphere.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.remove(immutablePos);
                    }
                }
            }
        }

        // Note: We don't clear INTENTIONAL_WATERLOGGING here because:
        // 1. Multi-chunk structures need to share the set across chunks
        // 2. Used positions are removed after restoration (above)
        // 3. Unused positions will be cleaned up by the next structure

        // Log set size for debugging (should be small or zero after processing)
        int remainingPositions = com.chronosphere.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.size();
        if (remainingPositions > 0) {
            Chronosphere.LOGGER.debug(
                "INTENTIONAL_WATERLOGGING set still contains {} positions after chunk {} for {}",
                remainingPositions,
                chunkPos,
                structureLocation.getPath()
            );
        }

        // Safety cleanup: if set is too large, clear it to prevent memory leak
        if (remainingPositions > 10000) {
            Chronosphere.LOGGER.warn(
                "INTENTIONAL_WATERLOGGING set is too large ({}), clearing to prevent memory leak",
                remainingPositions
            );
            com.chronosphere.worldgen.processors.CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING.clear();
        }

        if (totalDecorativeWaterConverted > 0) {
            Chronosphere.LOGGER.info(
                "Converted {} decorative water blocks in chunk {} for {}",
                totalDecorativeWaterConverted,
                chunkPos,
                structureLocation.getPath()
            );
        }

        if (totalWaterloggedRestored > 0) {
            Chronosphere.LOGGER.info(
                "Restored {} intentional waterlogged blocks in chunk {} for {}",
                totalWaterloggedRestored,
                chunkPos,
                structureLocation.getPath()
            );
        }

        if (totalWaterloggedRemoved > 0) {
            Chronosphere.LOGGER.info(
                "Removed {} unintentional waterlogged blocks in chunk {} for {}",
                totalWaterloggedRemoved,
                chunkPos,
                structureLocation.getPath()
            );
        }

        // Clear the set to free memory
        waterPositionsBeforePlacement.clear();
    }
}
