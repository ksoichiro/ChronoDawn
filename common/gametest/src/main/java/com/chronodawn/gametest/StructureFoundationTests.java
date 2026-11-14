package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.worldgen.StructureFoundationFiller;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public final class StructureFoundationTests {

    // The floor of a fake start piece floats over a hollow, as on the low side of a slope.
    private static final int GROUND_Y = 1;
    private static final int FLOOR_Y = 6;
    private static final int PIECE_TOP_Y = 8;
    private static final int MIN_X = 1;
    private static final int MAX_X = 5;
    private static final int MIN_Z = 1;
    private static final int MAX_Z = 3;
    // Columns by x: stone ground, grass ground under a flower, sand ground, a lower piece of the
    // same structure, and a hole in the floor.
    private static final int STONE_X = 1;
    private static final int FLOWER_X = 2;
    private static final int SAND_X = 3;
    private static final int LOWER_PIECE_X = 4;
    private static final int NO_FLOOR_X = 5;
    private static final int LOWER_PIECE_MIN_Y = 3;
    private static final int LOWER_PIECE_MAX_Y = 4;
    // The current chunk stops before the last row, which another chunk fills later.
    private static final int CHUNK_MAX_Z = 2;

    private StructureFoundationTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("structure_foundation_fills_hollow_under_floor",
            StructureFoundationTests::testFillsHollowUnderFloor));
        tests.add(factory.create("structure_foundation_applies_to_surface_structures_only",
            StructureFoundationTests::testAppliesToSurfaceStructuresOnly));
        return tests;
    }

    /**
     * Test: every column under the floor is filled down to the ground with dirt, or with sand over
     * sand, without entering a lower piece, a column with no floor, or another chunk.
     */
    private static void testFillsHollowUnderFloor(GameTestHelper helper) {
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                Block ground = x == SAND_X ? ModBlocks.TEMPORAL_SAND.get()
                    : x == FLOWER_X ? ModBlocks.TEMPORAL_GRASS_BLOCK.get()
                    : Blocks.STONE;
                // Support for the sand, which would fall into an empty block below.
                helper.setBlock(new BlockPos(x, GROUND_Y - 1, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, GROUND_Y, z), ground);
                for (int y = GROUND_Y + 1; y < FLOOR_Y; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
                helper.setBlock(new BlockPos(x, FLOOR_Y, z),
                    x == NO_FLOOR_X ? Blocks.AIR : ModBlocks.TEMPORAL_STONE_BRICKS.get());
            }
        }
        helper.setBlock(new BlockPos(FLOWER_X, GROUND_Y + 1, MIN_Z), Blocks.POPPY);

        BoundingBox startPiece = box(helper, MIN_X, FLOOR_Y, MIN_Z, MAX_X, PIECE_TOP_Y, MAX_Z);
        BoundingBox lowerPiece = box(helper,
            LOWER_PIECE_X, LOWER_PIECE_MIN_Y, MIN_Z, LOWER_PIECE_X, LOWER_PIECE_MAX_Y, MAX_Z);
        BoundingBox chunk = box(helper, 0, 0, 0, 9, 9, CHUNK_MAX_Z);
        StructureFoundationFiller.fill(helper.getLevel(), chunk, startPiece, List.of(startPiece, lowerPiece));

        Block dirt = ModBlocks.TEMPORAL_DIRT.get();
        for (int z = MIN_Z; z <= CHUNK_MAX_Z; z++) {
            for (int y = GROUND_Y + 1; y < FLOOR_Y; y++) {
                if (!expect(helper, STONE_X, y, z, dirt)
                        || !expect(helper, FLOWER_X, y, z, dirt)
                        || !expect(helper, SAND_X, y, z, ModBlocks.TEMPORAL_SAND.get())
                        || !expect(helper, NO_FLOOR_X, y, z, Blocks.AIR)) {
                    return;
                }
            }
            if (!expect(helper, LOWER_PIECE_X, LOWER_PIECE_MAX_Y + 1, z, dirt)
                    || !expect(helper, LOWER_PIECE_X, LOWER_PIECE_MAX_Y, z, Blocks.AIR)) {
                return;
            }
        }
        for (int x = MIN_X; x <= MAX_X; x++) {
            if (!expect(helper, x, FLOOR_Y - 1, MAX_Z, Blocks.AIR)) {
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Test: the structures that sit on the surface get a foundation, and Master Clock and other
     * mods' structures do not.
     */
    private static void testAppliesToSurfaceStructuresOnly(GameTestHelper helper) {
        for (String path : List.of("ancient_ruins", "clockwork_depths", "desert_clock_tower",
                "entropy_crypt", "forgotten_library", "guardian_vault")) {
            if (!StructureFoundationFiller.appliesTo("chronodawn", path)) {
                CompatGameTestHelper.fail(helper, path + " does not get a foundation");
                return;
            }
        }
        if (StructureFoundationFiller.appliesTo("chronodawn", "master_clock")) {
            CompatGameTestHelper.fail(helper, "master_clock gets a foundation");
            return;
        }
        if (StructureFoundationFiller.appliesTo("minecraft", "forgotten_library")) {
            CompatGameTestHelper.fail(helper, "A structure of another namespace gets a foundation");
            return;
        }
        helper.succeed();
    }

    private static BoundingBox box(GameTestHelper helper, int minX, int minY, int minZ,
                                   int maxX, int maxY, int maxZ) {
        BlockPos min = helper.absolutePos(new BlockPos(minX, minY, minZ));
        BlockPos max = helper.absolutePos(new BlockPos(maxX, maxY, maxZ));
        return BoundingBox.fromCorners(min, max);
    }

    private static boolean expect(GameTestHelper helper, int x, int y, int z, Block expected) {
        BlockPos pos = new BlockPos(x, y, z);
        Block actual = helper.getLevel().getBlockState(helper.absolutePos(pos)).getBlock();
        if (actual != expected) {
            CompatGameTestHelper.fail(helper, "Expected " + expected + " at " + pos + ", found " + actual);
            return false;
        }
        return true;
    }
}
