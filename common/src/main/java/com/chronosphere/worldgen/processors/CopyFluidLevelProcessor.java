package com.chronosphere.worldgen.processors;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModStructureProcessorTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Structure Processor that:
 * 1. Converts chronosphere:decorative_water to minecraft:water while preserving fluid level
 * 2. Records intentional waterlogging positions (from NBT)
 * 3. Removes unintentional waterlogging (added during placement)
 *
 * This allows decorative water features (like waterfalls) to maintain their flow state
 * after structure generation, while preventing unwanted waterlogging caused by Aquifer water.
 *
 * Flow states:
 * - level=0: Source block (still water)
 * - level=1-7: Flowing water (decreasing flow strength)
 * - level=8-15: Falling water (used for vertical flow)
 *
 * Waterlogging Prevention:
 * During structure placement, Minecraft automatically sets waterlogged=true on waterloggable
 * blocks (stairs, slabs, fences, etc.) if water exists in adjacent chunks. This processor
 * tracks which blocks were intentionally waterlogged (in NBT) and removes only the
 * unintentional waterlogging.
 *
 * Task: T239 [US3] Guardian Vault structure generation
 */
public class CopyFluidLevelProcessor extends StructureProcessor {
    /**
     * Thread-safe set to track blocks that have intentional waterlogging (from NBT).
     * These positions will be preserved by the Mixin cleanup.
     */
    public static final Set<BlockPos> INTENTIONAL_WATERLOGGING =
        Collections.newSetFromMap(new ConcurrentHashMap<>());
    /**
     * Codec for serialization/deserialization of this processor.
     */
    public static final MapCodec<CopyFluidLevelProcessor> CODEC = MapCodec.unit(CopyFluidLevelProcessor::new);

    /**
     * Default constructor for codec instantiation.
     */
    public CopyFluidLevelProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader level,
        BlockPos blockPos,
        BlockPos piecePos,
        StructureTemplate.StructureBlockInfo originalBlockInfo,
        StructureTemplate.StructureBlockInfo currentBlockInfo,
        StructurePlaceSettings settings
    ) {
        BlockState state = currentBlockInfo.state();

        // 1. Convert decorative water to vanilla water (preserving level)
        if (state.is(ModBlocks.DECORATIVE_WATER.get())) {
            // Get the fluid level from the decorative water state
            // If level property exists, copy it to the vanilla water
            if (state.hasProperty(BlockStateProperties.LEVEL)) {
                int fluidLevel = state.getValue(BlockStateProperties.LEVEL);

                // Create vanilla water with the same level
                BlockState newState = Blocks.WATER.defaultBlockState()
                    .setValue(BlockStateProperties.LEVEL, fluidLevel);

                return new StructureTemplate.StructureBlockInfo(
                    currentBlockInfo.pos(),
                    newState,
                    currentBlockInfo.nbt()
                );
            } else {
                // No level property, just convert to source water
                return new StructureTemplate.StructureBlockInfo(
                    currentBlockInfo.pos(),
                    Blocks.WATER.defaultBlockState(),
                    currentBlockInfo.nbt()
                );
            }
        }

        // 2. Track intentional waterlogging and remove unintentional waterlogging
        // Intentional waterlogging (from NBT) will be recorded and preserved by Mixin
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            boolean currentWaterlogged = state.getValue(BlockStateProperties.WATERLOGGED);

            // Check if the original NBT block was also waterlogged
            BlockState originalState = originalBlockInfo.state();

            // Get the actual world position from currentBlockInfo
            BlockPos worldPos = currentBlockInfo.pos();

            boolean originalWaterlogged = originalState.hasProperty(BlockStateProperties.WATERLOGGED)
                && originalState.getValue(BlockStateProperties.WATERLOGGED);

            if (originalWaterlogged) {
                // This block was intentionally waterlogged in the NBT
                // Record its position so Mixin can preserve it
                INTENTIONAL_WATERLOGGING.add(worldPos.immutable());

                Chronosphere.LOGGER.debug(
                    "Recorded intentional waterlogging at {}: {}",
                    worldPos,
                    state.getBlock()
                );
            }

            // Remove all waterlogging here (both intentional and unintentional)
            // The Mixin will restore intentional waterlogging after all blocks are placed
            if (currentWaterlogged) {
                BlockState newState = state.setValue(BlockStateProperties.WATERLOGGED, false);

                return new StructureTemplate.StructureBlockInfo(
                    currentBlockInfo.pos(),
                    newState,
                    currentBlockInfo.nbt()
                );
            }
        }

        // No change needed
        return currentBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.COPY_FLUID_LEVEL.get();
    }
}
