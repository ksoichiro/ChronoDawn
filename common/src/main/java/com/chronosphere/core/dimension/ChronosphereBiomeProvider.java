package com.chronosphere.core.dimension;

import com.chronosphere.Chronosphere;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * Custom biome provider for the Chronosphere dimension.
 *
 * Manages biome generation and registration for the Chronosphere dimension.
 * Biomes are defined via JSON datapacks in resources/data/chronosphere/worldgen/biome/.
 *
 * Reference: data-model.md (Dimension: Chronosphere → biomes)
 * Task: T043 [US1] Implement custom biome provider
 */
public class ChronosphereBiomeProvider {

    /**
     * Resource key for the Chronosphere Plains biome.
     * This is the default biome for the Chronosphere dimension.
     */
    public static final ResourceKey<Biome> CHRONOSPHERE_PLAINS = ResourceKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "chronosphere_plains")
    );

    /**
     * Resource key for the Chronosphere Desert biome.
     * This biome will contain the Desert Clock Tower structure (User Story 2).
     */
    public static final ResourceKey<Biome> CHRONOSPHERE_DESERT = ResourceKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "chronosphere_desert")
    );

    /**
     * Initialize biome provider.
     * Called during mod initialization.
     *
     * Note: Biomes are registered via JSON datapacks, not programmatically.
     * This method primarily exists for logging and future extension points.
     */
    public static void init() {
        Chronosphere.LOGGER.info("Chronosphere biome provider initialized (biomes defined via JSON datapacks)");
    }

    /**
     * Get the default biome for the Chronosphere dimension.
     *
     * @return ResourceKey for the default biome (Chronosphere Plains)
     */
    public static ResourceKey<Biome> getDefaultBiome() {
        return CHRONOSPHERE_PLAINS;
    }

    /**
     * Check if a biome is a Chronosphere biome.
     *
     * @param biomeKey ResourceKey of the biome to check
     * @return true if the biome belongs to the Chronosphere dimension
     */
    public static boolean isChronosphereBiome(ResourceKey<Biome> biomeKey) {
        String namespace = biomeKey.location().getNamespace();
        return namespace.equals(Chronosphere.MOD_ID);
    }
}
