package com.chronodawn.client;

import com.chronodawn.blocks.TemporalGrassBlock;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Computes the per-block tint for {@link TemporalGrassBlock}, blending the
 * biome grass color toward a pale "edge" color when the block is near a
 * Temporal Sand / Temporal Gravel disk (or vanilla sand/gravel placed by a
 * player).
 *
 * Within 2 blocks (Chebyshev, same Y) of an edge trigger, the grass tint
 * lerps toward {@link #EDGE_TINT} with weight {@code (3 - d) / 3}, so the
 * gradient is strongest at distance 1 and fades out by distance 3.
 *
 * Pure function — no caching, recomputed on every chunk mesh bake. Cost is
 * comparable to vanilla {@link BiomeColors#getAverageGrassColor}.
 */
public final class TemporalGrassEdgeTint {

    /** Returned for inventory / fallback contexts where world+pos cannot be sampled. */
    public static final int DEFAULT_FALLBACK = 0x5B8AC4;

    /**
     * Color the tint shifts toward at the edge of a sand/gravel disk.
     *
     * Computed so that at distance 1 (with {@link #RADIUS} = 3, weight 0.75)
     * the lerped grass color exactly matches the sand average 0x90BBE7:
     * {@code EDGE = (4 × sand − grass) / 3} per channel, which gives
     * {@code 0xA2CBF3}. d=2 and d=3 then provide a smooth fade back to the
     * biome grass color.
     *
     * Earlier values were progressively closer to but never reached sand:
     *   - 0x9CB6CC (placeholder midpoint)
     *   - 0x80ACDC (sand_avg biased 30% toward grass — too conservative)
     *   - 0x90BBE7 (raw sand_avg — d=1 lands at ~0x83AFDE, ~13 RGB units short)
     * Visual testing showed each step still left a perceptible step at d=1.
     * The overshoot here closes the gap completely.
     */
    public static final int EDGE_TINT = 0xA2CBF3;

    /**
     * Chebyshev radius scanned for edge triggers. Wider radius = wider, smoother
     * transition ring. With RADIUS=3 the weights at d=1 / d=2 / d=3 are
     * 0.75 / 0.50 / 0.25, vs the original RADIUS=2's 0.667 / 0.333 — visual
     * testing showed RADIUS=2 left sand/gravel circles feeling disjoint even
     * with EDGE_TINT pulled fully to the sand average.
     */
    private static final int RADIUS = 3;

    private TemporalGrassEdgeTint() {}

    /**
     * Block-color provider entry point. Hand this as a method reference to
     * {@code ColorProviderRegistry.BLOCK.register} (Fabric) or
     * {@code RegisterColorHandlersEvent.Block#register} (NeoForge).
     */
    public static int provide(BlockState state, @Nullable BlockAndTintGetter world,
                              @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        if (world == null || pos == null
                || !(world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock)) {
            return DEFAULT_FALLBACK;
        }
        int base = BiomeColors.getAverageGrassColor(world, pos);
        return blend(world, pos, base);
    }

    /**
     * Pure blend: scan the {@code RADIUS}-block Chebyshev neighborhood at the
     * same Y as {@code pos} and lerp {@code baseTint} toward {@link #EDGE_TINT}
     * by the closest edge-trigger distance.
     */
    static int blend(BlockGetter world, BlockPos pos, int baseTint) {
        int minDist = RADIUS + 1;
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        outer:
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                int d = Math.max(Math.abs(dx), Math.abs(dz));
                if (d >= minDist) continue;
                cur.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                if (isEdgeTrigger(world.getBlockState(cur))) {
                    minDist = d;
                    if (d == 1) break outer;
                }
            }
        }
        if (minDist > RADIUS) return baseTint;
        float t = (RADIUS + 1 - minDist) / (float) (RADIUS + 1);
        return lerpRgb(baseTint, EDGE_TINT, t);
    }

    static boolean isEdgeTrigger(BlockState state) {
        return state.is(ModBlocks.TEMPORAL_SAND.get())
            || state.is(ModBlocks.TEMPORAL_GRAVEL.get())
            || state.is(Blocks.SAND)
            || state.is(Blocks.GRAVEL);
    }

    /** Per-channel linear interpolation. {@code t} is clamped by callers. */
    public static int lerpRgb(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
