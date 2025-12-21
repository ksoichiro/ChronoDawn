package com.chronodawn.core.dimension;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * Custom biome provider for the ChronoDawn dimension.
 *
 * Manages biome generation and registration for the ChronoDawn dimension.
 * Biomes are defined via JSON datapacks in resources/data/chronodawn/worldgen/biome/.
 *
 * Reference: data-model.md (Dimension: ChronoDawn → biomes)
 * Task: T043 [US1] Implement custom biome provider
 */
public class ChronoDawnBiomeProvider {

    /**
     * Resource key for the ChronoDawn Plains biome.
     * This is the default biome for the ChronoDawn dimension.
     */
    public static final ResourceKey<Biome> CHRONOSPHERE_PLAINS = ResourceKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronodawn_plains")
    );

    /**
     * Resource key for the ChronoDawn Desert biome.
     * This biome will contain the Desert Clock Tower structure (User Story 2).
     */
    public static final ResourceKey<Biome> CHRONOSPHERE_DESERT = ResourceKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronodawn_desert")
    );

    /**
     * Resource key for the ChronoDawn Dark Forest biome.
     * This biome features dense fog and reduced visibility.
     */
    public static final ResourceKey<Biome> CHRONOSPHERE_DARK_FOREST = ResourceKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronodawn_dark_forest")
    );

    /**
     * Initialize biome provider.
     * Called during mod initialization.
     *
     * Note: Biomes are registered via JSON datapacks, not programmatically.
     * This method primarily exists for logging and future extension points.
     */
    public static void init() {
        ChronoDawn.LOGGER.info("ChronoDawn biome provider initialized (biomes defined via JSON datapacks)");
    }

    /**
     * Get the default biome for the ChronoDawn dimension.
     *
     * @return ResourceKey for the default biome (ChronoDawn Plains)
     */
    public static ResourceKey<Biome> getDefaultBiome() {
        return CHRONOSPHERE_PLAINS;
    }

    /**
     * Check if a biome is a ChronoDawn biome.
     *
     * @param biomeKey ResourceKey of the biome to check
     * @return true if the biome belongs to the ChronoDawn dimension
     */
    public static boolean isChronoDawnBiome(ResourceKey<Biome> biomeKey) {
        String namespace = biomeKey.location().getNamespace();
        return namespace.equals(ChronoDawn.MOD_ID);
    }
}
