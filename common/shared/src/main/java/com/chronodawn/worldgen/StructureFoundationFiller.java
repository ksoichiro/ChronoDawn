package com.chronodawn.worldgen;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.Set;

/**
 * Fills the air under the floor of a surface structure down to the ground, like the cobblestone
 * foundation of a vanilla woodland mansion.
 *
 * <p>Terrain adaptation alone cannot do this. The beard that {@code beard_thin} adds below a
 * structure fades out within about 4 to 8 blocks of the floor, and its kernel stops at 12 blocks.
 * A structure is placed at the terrain height at its centre, so on a slope the floor at the low
 * edge can sit much higher than the ground and leave a hollow beneath it.
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
     * Fills the columns under the start piece's floor within {@code chunkBox}.
     *
     * <p>A column is filled only where the bottom layer of the start piece holds a solid block.
     * The fill goes down through air, fluids and plants, and stops at the first block with a
     * collision shape, at another piece of the same structure, or at the bottom of
     * {@code chunkBox}. Stopping at another piece keeps a shaft that leads to a lower piece open.
     *
     * @param level the level being generated
     * @param chunkBox the part of the world the current chunk may write to
     * @param startPieceBox the bounding box of the structure's start piece
     * @param pieceBoxes the bounding boxes of every piece of the structure
     * @return the number of blocks placed
     */
    public static int fill(WorldGenLevel level, BoundingBox chunkBox, BoundingBox startPieceBox,
                           List<BoundingBox> pieceBoxes) {
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
                placed += fillColumn(level, chunkBox, pos, x, floorY - 1, z, pieceBoxes);
            }
        }
        return placed;
    }

    private static int fillColumn(WorldGenLevel level, BoundingBox chunkBox, BlockPos.MutableBlockPos pos,
                                  int x, int topY, int z,
                                  List<BoundingBox> pieceBoxes) {
        // Find the bottom of the hollow first, so the fill block can follow the ground under it.
        int y = topY;
        BlockState ground = null;
        while (true) {
            pos.set(x, y, z);
            if (y < chunkBox.minY() || level.isOutsideBuildHeight(pos) || isInsideAnyPiece(pos, pieceBoxes)) {
                break;
            }
            if (!isPassable(level, pos)) {
                ground = level.getBlockState(pos);
                break;
            }
            y--;
        }

        int bottomY = y + 1;
        if (bottomY > topY) {
            return 0;
        }
        BlockState fill = fillFor(ground);
        for (int fillY = topY; fillY >= bottomY; fillY--) {
            pos.set(x, fillY, z);
            level.setBlock(pos, fill, 2);
        }
        return topY - bottomY + 1;
    }

    /**
     * Sand under the hollow is continued with the same sand, so desert foundations match their
     * surroundings. Anything else gets dirt: grass would show its green sides on every block, and
     * copying stone could copy an exposed ore. Sand with a block entity, such as suspicious sand,
     * is not copied either.
     */
    private static BlockState fillFor(BlockState ground) {
        if (ground != null && ground.is(BlockTags.SAND) && !ground.hasBlockEntity()) {
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
