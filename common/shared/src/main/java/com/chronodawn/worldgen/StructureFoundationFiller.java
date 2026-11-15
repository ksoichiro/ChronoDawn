package com.chronodawn.worldgen;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
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
        "guardian_vault"
    );

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
