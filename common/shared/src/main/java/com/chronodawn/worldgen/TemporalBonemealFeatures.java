package com.chronodawn.worldgen;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Selects biome-appropriate Chrono Dawn ground cover for grass bonemeal. */
public final class TemporalBonemealFeatures {
    private static final int GRASS_WEIGHT = 6;
    private static final int FERN_WEIGHT = 2;
    private static final int ROOT_WEIGHT = 1;
    private static final int TALL_GRASS_WEIGHT = 1;

    private static final ResourceKey<PlacedFeature> TEMPORAL_GRASS = feature("temporal_grass_block");
    private static final ResourceKey<PlacedFeature> FERN = feature("fern_block");
    private static final ResourceKey<PlacedFeature> TEMPORAL_ROOT = feature("temporal_root_block");
    private static final ResourceKey<PlacedFeature> TALL_GRASS = feature("tall_grass_block");
    private static final ResourceKey<PlacedFeature> FADED_GRASS = feature("faded_grass_block");

    private static final TagKey<Biome> GROWS_FERNS = biomeTag("bonemeal_grows_ferns");
    private static final TagKey<Biome> GROWS_TEMPORAL_ROOTS = biomeTag("bonemeal_grows_temporal_roots");
    private static final TagKey<Biome> GROWS_TALL_GRASS = biomeTag("bonemeal_grows_tall_grass");
    private static final TagKey<Biome> GROWS_FADED_GRASS = biomeTag("bonemeal_grows_faded_grass");

    private TemporalBonemealFeatures() {}

    public static ResourceKey<PlacedFeature> select(Holder<Biome> biome, RandomSource random) {
        boolean growsFerns = biome.is(GROWS_FERNS);
        boolean growsRoots = biome.is(GROWS_TEMPORAL_ROOTS);
        boolean growsTallGrass = biome.is(GROWS_TALL_GRASS);
        int totalWeight = GRASS_WEIGHT
                + (growsFerns ? FERN_WEIGHT : 0)
                + (growsRoots ? ROOT_WEIGHT : 0)
                + (growsTallGrass ? TALL_GRASS_WEIGHT : 0);
        int selection = random.nextInt(totalWeight);

        if (selection < GRASS_WEIGHT) {
            return biome.is(GROWS_FADED_GRASS) ? FADED_GRASS : TEMPORAL_GRASS;
        }
        selection -= GRASS_WEIGHT;

        if (growsFerns) {
            if (selection < FERN_WEIGHT) {
                return FERN;
            }
            selection -= FERN_WEIGHT;
        }
        if (growsRoots) {
            if (selection < ROOT_WEIGHT) {
                return TEMPORAL_ROOT;
            }
            selection -= ROOT_WEIGHT;
        }
        return TALL_GRASS;
    }

    private static ResourceKey<PlacedFeature> feature(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                CompatResourceLocation.create(ChronoDawn.MOD_ID, path));
    }

    private static TagKey<Biome> biomeTag(String path) {
        return TagKey.create(Registries.BIOME, CompatResourceLocation.create(ChronoDawn.MOD_ID, path));
    }
}
