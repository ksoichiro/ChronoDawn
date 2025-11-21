package com.chronosphere.worldgen.biomes;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/**
 * TerraBlender Region for adding Strange Forest biome to Overworld.
 *
 * Strange Forest is a rare biome that signals the presence of Ancient Ruins underneath.
 * The biome represents Temporal Seal weakening, causing Chronosphere influence to leak into Overworld.
 *
 * Design Notes:
 * - Weight: 2 (rare but discoverable)
 * - Climate: Moderate temperature (0.0-0.5), moderate humidity (0.3-0.7)
 * - Biome: Blue-purple tinted forest indicating time distortion
 *
 * Task: T115n [US2] Implement Strange Forest biome for Ancient Ruins discoverability
 */
public class ChronosphereOverworldRegion extends Region {
    public static final ResourceKey<Biome> STRANGE_FOREST = ResourceKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath("chronosphere", "strange_forest")
    );

    public ChronosphereOverworldRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(net.minecraft.core.Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Add Strange Forest with moderate temperature and humidity
        // Temperature: 0.0-0.5 (cool to moderate, similar to regular forest)
        // Humidity: 0.3-0.7 (moderate, allows trees to grow)
        // Continentalness: -0.1 to 0.3 (inland areas, not too far from coast)
        // Erosion: -0.3 to 0.3 (varied terrain)
        // Depth: 0.0 to 0.3 (surface level, not underground)
        // Weirdness: 0.1 to 0.5 (slightly unusual, fits the "strange" theme)

        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(0.0F, 0.5F),  // Temperature: Cool to moderate
                Climate.Parameter.span(0.3F, 0.7F),  // Humidity: Moderate
                Climate.Parameter.span(-0.1F, 0.3F), // Continentalness: Inland
                Climate.Parameter.span(-0.3F, 0.3F), // Erosion: Varied
                Climate.Parameter.point(0.0F),       // Depth: Surface
                Climate.Parameter.span(0.1F, 0.5F),  // Weirdness: Slightly unusual
                0.0F                                 // Offset
            ),
            STRANGE_FOREST
        ));
    }
}
