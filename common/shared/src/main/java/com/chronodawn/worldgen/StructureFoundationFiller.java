package com.chronodawn.worldgen;

import com.chronodawn.registry.ModBlocks;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntBinaryOperator;

/**
 * Fills the hollow under the floor of a surface structure down to the natural ground, like the
 * cobblestone foundation of a vanilla woodland mansion.
 *
 * <p>Terrain adaptation alone cannot do this. A structure is placed at the terrain height at its
 * centre, so on uneven ground part of the floor sits well above the ground. The beard that
 * {@code beard_thin} adds fades out within about 4 to 8 blocks below the floor. Where the ground
 * is deeper, the beard leaves a slab of terrain under the floor with a hollow between the slab and
 * the original ground, or no terrain at all at the low edge of a slope.
 */
public final class StructureFoundationFiller {

    /**
     * Structures of this mod whose start piece sits on the surface. Master Clock is left out: it
     * uses no terrain adaptation and its surface piece is placed on purpose.
     */
    private static final Set<String> FOUNDATION_STRUCTURES = Set.of(
        "ancient_ruins",
        "clockwork_depths",
        "desert_clock_tower",
        "entropy_crypt",
        "forgotten_library",
        "guardian_vault",
        "sentinel_outpost"
    );

    /**
     * The deepest hollow under a floor that is still filled. A deeper one rejects the location.
     * The beard of {@code beard_thin} already fills about this much as sloped terrain, so the fill
     * stays a short wall at most.
     */
    public static final int MAX_FOUNDATION_DEPTH = 8;

    // Every 8 blocks plus the far edge: 6 x 6 samples for the 35 x 35 Forgotten Library.
    private static final int SAMPLE_STEP = 8;

    private StructureFoundationFiller() {
        // Utility class
    }

    /**
     * Returns whether the structure with the given registry name gets a foundation.
     */
    public static boolean appliesTo(String namespace, String path) {
        return com.chronodawn.ChronoDawn.MOD_ID.equals(namespace) && FOUNDATION_STRUCTURES.contains(path);
    }

    /**
     * Chunk offsets tried, nearest first, when the chosen chunk is too steep: the 8 chunks around
     * it, then the 16 chunks one ring further out.
     */
    private static final int[][] RETRY_OFFSETS = retryOffsets();

    // Set while the retries run, so that a retry checks its own point but does not retry again.
    private static final ThreadLocal<Boolean> RETRYING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * Generates a structure at another chunk offset, as if that chunk had been chosen.
     */
    @FunctionalInterface
    public interface RetryAt {
        Optional<Structure.GenerationStub> at(int chunkOffsetX, int chunkOffsetZ);
    }

    /**
     * Keeps a structure off steep ground without making it rarer.
     *
     * <p>If the chosen point is too steep (see {@link #rejectSteepGround}), the chunks near it are
     * tried in turn and the first point that passes is used. The structure is still recorded in
     * the chunk its placement chose, so {@code /locate} and the compasses point at most two chunks
     * away from it. Only when no nearby point passes is the structure dropped.
     */
    public static Optional<Structure.GenerationStub> placeOnGround(Structure.GenerationContext context,
                                                                   Structure.GenerationStub stub,
                                                                   RetryAt retryAt) {
        Optional<Structure.GenerationStub> grounded = rejectSteepGround(context, stub);
        if (grounded.isPresent() || RETRYING.get()) {
            return grounded;
        }
        RETRYING.set(Boolean.TRUE);
        try {
            for (int[] offset : RETRY_OFFSETS) {
                Optional<Structure.GenerationStub> retried = retryAt.at(offset[0], offset[1]);
                if (retried.isPresent()) {
                    return retried;
                }
            }
        } finally {
            RETRYING.set(Boolean.FALSE);
        }
        return Optional.empty();
    }

    private static int[][] retryOffsets() {
        List<int[]> offsets = new java.util.ArrayList<>();
        for (int ring = 1; ring <= 2; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                        offsets.add(new int[] {dx, dz});
                    }
                }
            }
        }
        return offsets.toArray(new int[0][]);
    }

    /**
     * Drops a generation point whose floor would hang more than {@link #MAX_FOUNDATION_DEPTH}
     * blocks over the natural ground anywhere under the start piece. On such steep ground the
     * foundation would show as a tall straight wall.
     *
     * <p>This runs where {@code /locate} and the compasses check a candidate too, so they never
     * point at a structure that is not generated. The pieces are assembled once here, and the
     * returned stub carries them so that generation does not assemble them again.
     */
    public static Optional<Structure.GenerationStub> rejectSteepGround(Structure.GenerationContext context,
                                                                       Structure.GenerationStub stub) {
        StructurePiecesBuilder builder = stub.getPiecesBuilder();
        List<StructurePiece> pieces = builder.build().pieces();
        Structure.GenerationStub built = new Structure.GenerationStub(stub.position(), Either.right(builder));
        if (pieces.isEmpty()) {
            return Optional.of(built);
        }
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        IntBinaryOperator groundTop = (x, z) -> chunkGenerator.getBaseHeight(
            x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        // Stop at the first sample past the limit: a rejected point goes on to up to 24 retries.
        if (maxFoundationDepth(pieces.get(0).getBoundingBox(), groundTop, MAX_FOUNDATION_DEPTH)
                > MAX_FOUNDATION_DEPTH) {
            return Optional.empty();
        }
        return Optional.of(built);
    }

    /**
     * Returns the largest number of blocks between the floor of {@code startPieceBox} and the
     * natural ground, sampled on a grid over the floor that includes its edges.
     */
    public static int maxFoundationDepth(BoundingBox startPieceBox, IntBinaryOperator groundTop) {
        return maxFoundationDepth(startPieceBox, groundTop, Integer.MAX_VALUE);
    }

    /**
     * Like {@link #maxFoundationDepth(BoundingBox, IntBinaryOperator)}, but returns as soon as a
     * sample is deeper than {@code limit}.
     */
    public static int maxFoundationDepth(BoundingBox startPieceBox, IntBinaryOperator groundTop, int limit) {
        int floorY = startPieceBox.minY();
        int deepest = 0;
        for (int x : samples(startPieceBox.minX(), startPieceBox.maxX())) {
            for (int z : samples(startPieceBox.minZ(), startPieceBox.maxZ())) {
                deepest = Math.max(deepest, floorY - groundTop.applyAsInt(x, z));
                if (deepest > limit) {
                    return deepest;
                }
            }
        }
        return deepest;
    }

    private static int[] samples(int min, int max) {
        int count = Math.max(1, (max - min + SAMPLE_STEP - 1) / SAMPLE_STEP) + 1;
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = i == count - 1 ? max : min + i * SAMPLE_STEP;
        }
        return result;
    }

    /**
     * Fills the hollow under the start piece's floor within {@code chunkBox}, down to the height
     * the terrain would have without any structure.
     *
     * @return the number of blocks placed
     */
    public static int fill(WorldGenLevel level, ChunkGenerator chunkGenerator, BoundingBox chunkBox,
                           BoundingBox startPieceBox, List<BoundingBox> pieceBoxes) {
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        // The base height is computed from the noise alone, without the beard of any structure,
        // so it is the original ground even under a slab that the beard raised.
        IntBinaryOperator groundTop = (x, z) -> chunkGenerator.getBaseHeight(
            x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, randomState);
        return fill(level, chunkBox, startPieceBox, pieceBoxes, groundTop);
    }

    /**
     * Fills the columns under the start piece's floor within {@code chunkBox}.
     *
     * <p>A column is filled only where the bottom layer of the start piece holds a solid block.
     * Every block without a collision shape (air, fluids, plants) from just below the floor down to
     * {@code groundTop} is replaced. Solid blocks in between, such as a slab raised by the beard,
     * are kept, and natural caves below {@code groundTop} are left alone. The fill stops at another
     * piece of the same structure, so a shaft that leads to a lower piece is not blocked, and at
     * the bottom of {@code chunkBox}.
     *
     * @param level the level being generated
     * @param chunkBox the part of the world the current chunk may write to
     * @param startPieceBox the bounding box of the structure's start piece
     * @param pieceBoxes the bounding boxes of every piece of the structure
     * @param groundTop the lowest y to fill in a column, which is the first y above the natural
     *     ground
     * @return the number of blocks placed
     */
    public static int fill(WorldGenLevel level, BoundingBox chunkBox, BoundingBox startPieceBox,
                           List<BoundingBox> pieceBoxes, IntBinaryOperator groundTop) {
        if (!startPieceBox.intersects(chunkBox)) {
            return 0;
        }
        int floorY = startPieceBox.minY();
        int minX = Math.max(startPieceBox.minX(), chunkBox.minX());
        int maxX = Math.min(startPieceBox.maxX(), chunkBox.maxX());
        int minZ = Math.max(startPieceBox.minZ(), chunkBox.minZ());
        int maxZ = Math.min(startPieceBox.maxZ(), chunkBox.maxZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int placed = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                pos.set(x, floorY, z);
                if (isPassable(level, pos)) {
                    continue;
                }
                int bottomY = Math.max(groundTop.applyAsInt(x, z), chunkBox.minY());
                if (bottomY >= floorY) {
                    continue;
                }
                placed += fillColumn(level, pos, x, floorY - 1, bottomY, z, pieceBoxes);
            }
        }
        return placed;
    }

    private static int fillColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int topY, int bottomY,
                                  int z, List<BoundingBox> pieceBoxes) {
        pos.set(x, bottomY - 1, z);
        BlockState fill = fillFor(level.getBlockState(pos));
        int placed = 0;
        for (int y = topY; y >= bottomY; y--) {
            pos.set(x, y, z);
            if (isInsideAnyPiece(pos, pieceBoxes)) {
                break;
            }
            if (isPassable(level, pos)) {
                level.setBlock(pos, fill, 2);
                placed++;
            }
        }
        return placed;
    }

    /**
     * Sand ground is continued with the same sand, so desert foundations match their surroundings.
     * Anything else gets dirt: grass would show its green sides on every block, and copying stone
     * could copy an exposed ore. Sand with a block entity, such as suspicious sand, is not copied
     * either.
     */
    private static BlockState fillFor(BlockState ground) {
        if (ground.is(BlockTags.SAND) && !ground.hasBlockEntity()) {
            return ground.getBlock().defaultBlockState();
        }
        return ModBlocks.TEMPORAL_DIRT.get().defaultBlockState();
    }

    private static boolean isPassable(WorldGenLevel level, BlockPos pos) {
        // Air, fluids, grass and flowers have no collision shape.
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isInsideAnyPiece(BlockPos pos, List<BoundingBox> pieceBoxes) {
        for (BoundingBox box : pieceBoxes) {
            if (box.isInside(pos)) {
                return true;
            }
        }
        return false;
    }
}
