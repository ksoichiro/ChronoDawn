package com.chronodawn.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration for {@link NbtTemplateFeature}.
 *
 * @param template     The structure template to load (e.g. "chronodawn:time_cairn").
 * @param randomRotate If true, the template is placed with a random 90-degree rotation.
 * @param yOffset      Vertical offset applied after heightmap snapping. Negative values
 *                     bury the structure into terrain (useful for "settled" footprints).
 */
public record NbtTemplateConfiguration(
        ResourceLocation template,
        boolean randomRotate,
        int yOffset
) implements FeatureConfiguration {

    public static final Codec<NbtTemplateConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("template").forGetter(NbtTemplateConfiguration::template),
                    Codec.BOOL.optionalFieldOf("random_rotate", Boolean.TRUE).forGetter(NbtTemplateConfiguration::randomRotate),
                    Codec.INT.optionalFieldOf("y_offset", 0).forGetter(NbtTemplateConfiguration::yOffset)
            ).apply(instance, NbtTemplateConfiguration::new)
    );
}
