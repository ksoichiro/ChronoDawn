package com.chronodawn.client;

import net.minecraft.core.BlockPos;

/**
 * Per-tree color variation for ChronoDawn leaf blocks.
 *
 * Hashes an 8-block horizontal cell so all leaves of one tree (typically
 * 5–7 blocks wide) usually share a color while neighboring trees differ.
 * Y is ignored so the entire canopy of a tree colors uniformly.
 *
 * Each leaf kind defines a small palette weighted toward its primary tone
 * with a single rare accent color (withered yellow on blue kinds, faded
 * blue on the withered kind) for "枯れ枝" / "若枝" variation.
 */
public final class LeafColorProvider {

    /** Cell size = 1 << 3 = 8 blocks (slightly larger than typical canopy). */
    private static final int CELL_SIZE_LOG2 = 3;

    public enum LeafKind {
        // Mid blue (current Time Wood color) with lighter / darker variants
        // and a rare withered-yellow accent. Texture is light gray so saturated
        // tints come through clearly.
        TIME_WOOD(new int[] {
            0x78A6DA, 0x78A6DA, 0x78A6DA, 0x78A6DA,
            0x9BC1E0, 0x9BC1E0, 0x9BC1E0,
            0x5687B5, 0x5687B5, 0x5687B5,
            0x6FB3D6, 0x6FB3D6,
            0xC4A85B,
        }),
        // Subtle hue shifts around an untinted dark-gray base. Tints stay near
        // white because the texture is already very dark (~#464646 avg) — strong
        // tints would crush the block to near-black. Final on-screen colors are
        // dark gray with cool / warm / mossy / rusted hints.
        DARK_TIME_WOOD(new int[] {
            0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,
            0xE0E8FF, 0xE0E8FF, 0xE0E8FF,
            0xFFE8D8, 0xFFE8D8, 0xFFE8D8,
            0xE8FFE8, 0xE8FFE8,
            0xE0B898,
        }),
        // Subtle warm / cool shifts around an untinted medium-gray base. Tints
        // stay near white because the texture is mid gray (~#7D7D7D avg);
        // saturated tints would turn the leaves muddy brown. Final colors are
        // weathered gray with sepia, blue-cool, or aged warm hints.
        ANCIENT_TIME_WOOD(new int[] {
            0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,
            0xFFE8C8, 0xFFE8C8, 0xFFE8C8,
            0xE8E8FF, 0xE8E8FF, 0xE8E8FF,
            0xFFD8B0, 0xFFD8B0,
            0xA8C8E0,
        });

        private final int[] palette;

        LeafKind(int[] palette) {
            this.palette = palette;
        }

        /** Canonical color for inventory icons / item rendering. */
        public int iconColor() {
            return palette[0];
        }
    }

    private LeafColorProvider() {}

    /**
     * Returns the per-tree tint color (RGB) for a leaf at {@code pos}.
     * When {@code pos} is null (item rendering context), returns the icon color.
     */
    public static int colorAt(LeafKind kind, BlockPos pos) {
        if (pos == null) {
            return kind.iconColor();
        }
        int cellX = pos.getX() >> CELL_SIZE_LOG2;
        int cellZ = pos.getZ() >> CELL_SIZE_LOG2;
        int idx = Math.floorMod(hash(cellX, cellZ, kind.ordinal()), kind.palette.length);
        return kind.palette[idx];
    }

    private static int hash(int x, int z, int kindSalt) {
        int h = x * 374761393 + z * 668265263 + kindSalt * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }
}
