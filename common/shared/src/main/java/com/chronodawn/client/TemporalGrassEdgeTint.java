package com.chronodawn.client;

import com.chronodawn.blocks.TemporalGrassBlock;
import com.chronodawn.core.dimension.ChronoDawnBiomeProvider;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Computes the per-block tint for {@link TemporalGrassBlock}, blending the
 * biome grass color toward a pale "edge" color when the block is near a
 * Temporal Sand / Temporal Gravel disk (or vanilla sand/gravel placed by a
 * player), and toward a dark teal "wet" color when near water.
 *
 * Edge blend: within {@link #RADIUS} blocks (Chebyshev, same Y) of an edge
 * trigger, the grass tint lerps toward {@link #EDGE_TINT} — or
 * {@link #EDGE_TINT_FADED} if the grass block is inside the Faded Plains
 * biome.
 *
 * Water blend: within {@link #WATER_RADIUS} blocks (Chebyshev, same Y and
 * one Y below) of water, the grass tint lerps toward {@link #WET_TINT}.
 *
 * Both effects stack — a block near both sand and water receives both blends.
 *
 * Sand/gravel side: outside Faded Plains, applies a subtle pull
 * ({@link #SAND_NEIGHBOR_TINT}) toward grass at d=1; inside Faded Plains,
 * applies {@link #BIOME_TINT_FADED} unconditionally and layers
 * {@link #SAND_NEIGHBOR_TINT_FADED} at d=1.
 *
 * Pure function — no caching, recomputed on every chunk mesh bake. Cost is
 * comparable to vanilla {@link BiomeColors#getAverageGrassColor}.
 */
public final class TemporalGrassEdgeTint {

    /** Returned for inventory / fallback contexts where world+pos cannot be sampled. */
    public static final int DEFAULT_FALLBACK = 0x5B8AC4;

    /**
     * Color the tint shifts toward at the edge of a sand/gravel disk.
     * Set to the average pixel color of {@code temporal_sand.png}; paired
     * with {@link #provideForSandGravel} so both sides of the boundary blend
     * symmetrically (grass-side fades toward sand, sand-side at d=1 fades
     * toward grass).
     *
     * Tuning history:
     *   - 0x9CB6CC: placeholder midpoint
     *   - 0x80ACDC: sand_avg biased 30% toward grass — too conservative
     *   - 0xA2CBF3: overshoot to make d=1 grass = sand exactly — felt like the
     *     circle had widened by 1 block; sand interior still looked floating
     *   - 0x90BBE7 (current): raw sand_avg, complemented by sand-side tint
     */
    public static final int EDGE_TINT = 0x90BBE7;

    /**
     * Multiplicative tint applied to Temporal Sand / Temporal Gravel blocks at
     * Chebyshev distance 1 from a Temporal Grass Block (same Y). Pulls the
     * baked sand/gravel texture color ~25% toward the grass tint so the d=1
     * sand row visually meets the grass-side gradient halfway.
     *
     * <p>Derivation: target pixel = lerp(SAND_AVG=0x90BBE7, GRASS=0x5B8AC4,
     * 0.25) = 0x83AFDE — same color the grass-side reaches at d=1 with
     * RADIUS=3. Multiplicative tint = target / SAND_AVG per channel ≈
     * (0xE8, 0xEF, 0xF5).
     */
    public static final int SAND_NEIGHBOR_TINT = 0xE8EFF5;

    /**
     * Multiplicative tint applied to Temporal Sand / Temporal Gravel anywhere inside
     * Faded Plains (chronodawn:chronodawn_faded_plains). Pulls the blue-leaning
     * texture (~0x90BBE7) toward a warm faded color. Effective rendered color
     * approx 0x907438 (yellow-brown).
     *
     * Multiplicative tint cannot raise channel values, so a "true" sandstone
     * yellow is unreachable without a texture rewrite — see design spec §7.
     *
     * Tuning history:
     *   - 0xFFE06A: initial estimate; in-game looked yellow-green (G > R), too "rich"
     *     for the withered theme. G=164 > R=144 made it olive rather than tan.
     *   - 0xFFA040 (current): pulls G below R for a yellow-brown look matching the
     *     "枯れた" (withered) feel. Effective avg shifts to (144, 116, 56).
     */
    public static final int BIOME_TINT_FADED = 0xFFA040;

    /**
     * Color the grass-side blend lerps toward when scanning detects an adjacent
     * sand/gravel block in Faded Plains. Mirrors prairies, where {@link #EDGE_TINT}
     * is the raw sand average — here it is the effective tinted-sand color (the
     * result of applying {@link #BIOME_TINT_FADED} to the sand texture average).
     *
     * Tuning history:
     *   - 0x90A460: matched BIOME_TINT_FADED 0xFFE06A — replaced when BIOME_TINT_FADED
     *     was retuned to 0xFFA040.
     *   - 0x907438 (current): matches BIOME_TINT_FADED 0xFFA040's effective output
     */
    public static final int EDGE_TINT_FADED = 0x907438;

    /**
     * Multiplicative tint applied to Temporal Sand / Temporal Gravel at Chebyshev
     * distance 1 from a Temporal Grass Block, when in Faded Plains. Initial value
     * 0xFFFFFF (no further pull) on the assumption that warm sand and warm grass
     * are already close enough at the boundary. Tune to a small darken
     * (e.g., 0xF8F8F0) only if visual testing reveals a step at d=1.
     *
     * Tuning history:
     *   - 0xFFFFFF (current): initial estimate, no additional darkening
     */
    public static final int SAND_NEIGHBOR_TINT_FADED = 0xFFFFFF;

    /**
     * Chebyshev radius scanned for edge triggers. Wider radius = wider, smoother
     * transition ring. With RADIUS=3 the weights at d=1 / d=2 / d=3 are
     * 0.75 / 0.50 / 0.25, vs the original RADIUS=2's 0.667 / 0.333 — visual
     * testing showed RADIUS=2 left sand/gravel circles feeling disjoint even
     * with EDGE_TINT pulled fully to the sand average.
     */
    private static final int RADIUS = 3;

    /**
     * Color the tint shifts toward when the grass block is near water.
     * Dark blue-green for a wet, saturated look.
     *
     * Tuning history:
     *   - 0x3A6B5D: dark teal — too green
     *   - 0x3A597F (current): base grass blue (0x5B8AC4) darkened ~35%
     */
    public static final int WET_TINT = 0x3A597F;

    /**
     * Chebyshev radius for water proximity detection. Controls how far
     * from water the wet tint extends. Also checks one Y below the grass
     * block to catch water at shore edges.
     */
    private static final int WATER_RADIUS = 2;

    private TemporalGrassEdgeTint() {}

    /**
     * Returns true if the biome at {@code pos} is {@code chronodawn:chronodawn_faded_plains}.
     *
     * <p>Uses {@link Minecraft#getInstance()} {@code .level} for biome lookup rather than
     * the {@code BlockAndTintGetter world} parameter, because color providers receive a
     * {@code RenderChunkRegion} during chunk mesh bake — that wrapper does not implement
     * {@link net.minecraft.world.level.LevelReader}, so {@code instanceof LevelReader}
     * silently returns false there. The client level is the authoritative biome source on
     * the render thread.
     *
     * <p>Uses {@code Holder.is(ResourceKey)} via the per-version
     * {@link ChronoDawnBiomeProvider#CHRONO_DAWN_FADED_PLAINS} key, which delegates the
     * 1.21.11 {@code ResourceLocation} → {@code Identifier} rename to the existing
     * {@code CompatResourceLocation} layer.
     */
    static boolean isInFadedPlains(BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        return level.getBiome(pos).is(ChronoDawnBiomeProvider.CHRONO_DAWN_FADED_PLAINS);
    }

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
        int edgeTint = isInFadedPlains(pos) ? EDGE_TINT_FADED : EDGE_TINT;
        return blend(world, pos, base, edgeTint);
    }

    /**
     * Returns true if any block at Chebyshev distance 1 (same Y) from {@code pos}
     * is a Temporal Grass Block.
     */
    static boolean scanForGrassNeighbor(BlockAndTintGetter world, BlockPos pos) {
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                cur.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                if (world.getBlockState(cur).getBlock() instanceof TemporalGrassBlock) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Block-color provider entry point for Temporal Sand / Temporal Gravel.
     * Returns {@link #SAND_NEIGHBOR_TINT} when the block has a Temporal Grass
     * Block as a Chebyshev-distance-1 neighbor at the same Y; otherwise
     * {@code 0xFFFFFF} (no tint, preserving the original baked texture color).
     *
     * <p>Mirrors the grass-side gradient: the d=1 ring of sand/gravel pulls
     * toward grass while the d=1 ring of grass pulls toward sand. They meet
     * at approximately the same visual color so the boundary has no step.
     *
     * <p>Item rendering (null world/pos) returns {@code 0xFFFFFF}, leaving
     * inventory icons untinted.
     */
    public static int provideForSandGravel(BlockState state, @Nullable BlockAndTintGetter world,
                                           @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        if (world == null || pos == null) return 0xFFFFFF;
        boolean nearGrass = scanForGrassNeighbor(world, pos);
        if (isInFadedPlains(pos)) {
            int result = BIOME_TINT_FADED;
            // No-op while SAND_NEIGHBOR_TINT_FADED == 0xFFFFFF; tuning hook — see constant Javadoc.
            if (nearGrass) result = multiplyRgb(result, SAND_NEIGHBOR_TINT_FADED);
            return result;
        }
        return nearGrass ? SAND_NEIGHBOR_TINT : 0xFFFFFF;
    }

    /**
     * Pure blend: scan the {@code RADIUS}-block Chebyshev neighborhood and
     * apply up to two tint layers — one for sand/gravel edge proximity
     * (toward {@code edgeTint}) and one for water proximity
     * (toward {@link #WET_TINT}). Both scans share the same loop.
     */
    static int blend(BlockGetter world, BlockPos pos, int baseTint, int edgeTint) {
        int minDistEdge = RADIUS + 1;
        int minDistWater = WATER_RADIUS + 1;
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        outer:
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                int d = Math.max(Math.abs(dx), Math.abs(dz));
                boolean canImproveEdge = d < minDistEdge;
                boolean canImproveWater = d <= WATER_RADIUS && d < minDistWater;
                if (!canImproveEdge && !canImproveWater) continue;
                cur.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                BlockState neighbor = world.getBlockState(cur);
                if (canImproveEdge && isEdgeTrigger(neighbor)) {
                    minDistEdge = d;
                }
                if (canImproveWater) {
                    if (isWater(neighbor)) {
                        minDistWater = d;
                    } else {
                        cur.setY(pos.getY() - 1);
                        if (isWater(world.getBlockState(cur))) {
                            minDistWater = d;
                        }
                        cur.setY(pos.getY());
                    }
                }
                if (minDistEdge == 1 && minDistWater == 1) break outer;
            }
        }
        int result = baseTint;
        if (minDistEdge <= RADIUS) {
            float t = (RADIUS + 1 - minDistEdge) / (float) (RADIUS + 1);
            result = lerpRgb(result, edgeTint, t);
        }
        if (minDistWater <= WATER_RADIUS) {
            float t = (WATER_RADIUS + 1 - minDistWater) / (float) (WATER_RADIUS + 1);
            result = lerpRgb(result, WET_TINT, t);
        }
        return result;
    }

    static boolean isEdgeTrigger(BlockState state) {
        return state.is(ModBlocks.TEMPORAL_SAND.get())
            || state.is(ModBlocks.TEMPORAL_GRAVEL.get())
            || state.is(Blocks.SAND)
            || state.is(Blocks.GRAVEL);
    }

    static boolean isWater(BlockState state) {
        return state.getFluidState().is(FluidTags.WATER);
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

    /** Per-channel multiplicative blend: out = a * b / 255 per channel.
     *  Uses integer truncation; rounding error ≤1 per channel vs. {@link #lerpRgb}. */
    public static int multiplyRgb(int a, int b) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (ar * br) / 0xFF;
        int g = (ag * bg) / 0xFF;
        int bl = (ab * bb) / 0xFF;
        return (r << 16) | (g << 8) | bl;
    }
}
