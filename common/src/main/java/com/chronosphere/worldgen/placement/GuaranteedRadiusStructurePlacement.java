package com.chronosphere.worldgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

/**
 * Custom StructurePlacement that guarantees at least one structure within a specified radius.
 *
 * This placement type ensures:
 * 1. Structure appears within `radiusChunks` of world origin (0, 0)
 * 2. Structures are spaced at least `minDistance` chunks apart (like random_spread)
 *
 * Placement Algorithm:
 * - Divide world into grids of `minDistance` size
 * - Within each grid cell, deterministically select one chunk for potential placement
 * - Only chunks within `radiusChunks` from origin are considered
 *
 * Use Cases:
 * - Ensuring players find key structures early game
 * - Balancing exploration requirements with player progression
 *
 * JSON Usage:
 * ```json
 * {
 *   "type": "chronosphere:guaranteed_radius",
 *   "salt": 12345,
 *   "radius_chunks": 50,
 *   "min_distance": 15,
 *   "frequency": 1.0
 * }
 * ```
 *
 * Reference: research.md "Guaranteed Structure Placement Research"
 * Task: T278 [US3] Create GuaranteedRadiusStructurePlacement.java
 */
public class GuaranteedRadiusStructurePlacement extends StructurePlacement {
    /**
     * Codec for JSON serialization/deserialization.
     */
    public static final MapCodec<GuaranteedRadiusStructurePlacement> CODEC = RecordCodecBuilder.<GuaranteedRadiusStructurePlacement>mapCodec(
        instance -> instance.group(
            // Base class fields
            Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO)
                .forGetter(GuaranteedRadiusStructurePlacement::locateOffset),
            FrequencyReductionMethod.CODEC
                .optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT)
                .forGetter(GuaranteedRadiusStructurePlacement::frequencyReductionMethod),
            Codec.floatRange(0.0F, 1.0F)
                .optionalFieldOf("frequency", 1.0F)
                .forGetter(GuaranteedRadiusStructurePlacement::frequency),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt")
                .forGetter(GuaranteedRadiusStructurePlacement::salt),
            ExclusionZone.CODEC.optionalFieldOf("exclusion_zone")
                .forGetter(GuaranteedRadiusStructurePlacement::exclusionZone),
            // Custom fields
            Codec.intRange(1, 1000).fieldOf("radius_chunks")
                .forGetter(p -> p.radiusChunks),
            Codec.intRange(1, 256).fieldOf("min_distance")
                .forGetter(p -> p.minDistance)
        ).apply(instance, GuaranteedRadiusStructurePlacement::new)
    ).validate(GuaranteedRadiusStructurePlacement::validate);

    private final int radiusChunks;
    private final int minDistance;

    /**
     * Construct a new GuaranteedRadiusStructurePlacement.
     *
     * @param locateOffset       Offset for structure location (from base class)
     * @param frequencyReduction Frequency reduction method (from base class)
     * @param frequency          Spawn frequency 0.0-1.0 (from base class)
     * @param salt               Random salt for seeded generation (from base class)
     * @param exclusionZone      Optional exclusion zone (from base class)
     * @param radiusChunks       Guaranteed radius in chunks from world origin
     * @param minDistance        Minimum distance between structures in chunks
     */
    public GuaranteedRadiusStructurePlacement(
        Vec3i locateOffset,
        FrequencyReductionMethod frequencyReduction,
        float frequency,
        int salt,
        Optional<ExclusionZone> exclusionZone,
        int radiusChunks,
        int minDistance
    ) {
        super(locateOffset, frequencyReduction, frequency, salt, exclusionZone);
        this.radiusChunks = radiusChunks;
        this.minDistance = minDistance;
    }

    /**
     * Validate configuration constraints.
     */
    private static DataResult<GuaranteedRadiusStructurePlacement> validate(GuaranteedRadiusStructurePlacement placement) {
        if (placement.minDistance > placement.radiusChunks) {
            return DataResult.error(() -> "min_distance cannot be greater than radius_chunks");
        }
        return DataResult.success(placement);
    }

    /**
     * Determine if a structure should be placed in the specified chunk.
     *
     * Algorithm:
     * 1. Check if chunk is within guaranteed radius
     * 2. Calculate grid cell for this chunk
     * 3. Use seeded random to select one chunk in the grid cell
     * 4. Return true only if this chunk matches the selected chunk
     *
     * @param state  Chunk generator structure state
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     * @return true if structure should be placed in this chunk
     */
    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        // Calculate distance from origin (in chunks)
        int chunkDist = Math.max(Math.abs(chunkX), Math.abs(chunkZ));

        // Only place within guaranteed radius
        if (chunkDist > radiusChunks) {
            return false;
        }

        // Calculate grid cell coordinates
        int gridX = Math.floorDiv(chunkX, minDistance);
        int gridZ = Math.floorDiv(chunkZ, minDistance);

        // Use seeded random for deterministic placement
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(state.getLevelSeed(), gridX, gridZ, salt());

        // Select random chunk within grid cell
        int offsetX = random.nextInt(minDistance);
        int offsetZ = random.nextInt(minDistance);

        int selectedChunkX = gridX * minDistance + offsetX;
        int selectedChunkZ = gridZ * minDistance + offsetZ;

        // Check if this chunk is the selected one
        return selectedChunkX == chunkX && selectedChunkZ == chunkZ;
    }

    /**
     * Get the placement type for registry.
     */
    @Override
    public StructurePlacementType<?> type() {
        return ModStructurePlacementTypes.GUARANTEED_RADIUS.get();
    }

    /**
     * Get the guaranteed radius in chunks.
     */
    public int getRadiusChunks() {
        return radiusChunks;
    }

    /**
     * Get the minimum distance between structures in chunks.
     */
    public int getMinDistance() {
        return minDistance;
    }
}
