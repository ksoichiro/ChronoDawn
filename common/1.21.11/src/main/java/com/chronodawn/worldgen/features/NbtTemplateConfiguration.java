package com.chronodawn.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration for {@link NbtTemplateFeature}.
 *
 * <p>1.21.11 override: uses {@code Identifier} (renamed from {@code ResourceLocation}
 * in Minecraft 1.21.11).</p>
 *
 * @param template     The structure template to load (e.g. "chronodawn:time_cairn").
 * @param randomRotate If true, the template is placed with a random 90-degree rotation.
 * @param yOffset      Vertical offset applied after heightmap snapping. Negative values
 *                     bury the structure into terrain (useful for "settled" footprints).
 * @param minGroundContactRatio Minimum fraction of the rotated template footprint that
 *                              must have sturdy terrain directly below it.
 * @param groundContactYOffset Template-relative Y offset used for ground contact checks.
 * @param clearReplaceableBlocks If true, replaceable blocks such as grass and snow layers
 *                               are cleared from the footprint before placement.
 */
public record NbtTemplateConfiguration(
        Identifier template,
        boolean randomRotate,
        int yOffset,
        double minGroundContactRatio,
        int groundContactYOffset,
        boolean clearReplaceableBlocks
) implements FeatureConfiguration {

    public static final Codec<NbtTemplateConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("template").forGetter(NbtTemplateConfiguration::template),
                    Codec.BOOL.optionalFieldOf("random_rotate", Boolean.TRUE).forGetter(NbtTemplateConfiguration::randomRotate),
                    Codec.INT.optionalFieldOf("y_offset", 0).forGetter(NbtTemplateConfiguration::yOffset),
                    Codec.doubleRange(0.0, 1.0).optionalFieldOf("min_ground_contact_ratio", 0.0)
                            .forGetter(NbtTemplateConfiguration::minGroundContactRatio),
                    Codec.INT.optionalFieldOf("ground_contact_y_offset", -1)
                            .forGetter(NbtTemplateConfiguration::groundContactYOffset),
                    Codec.BOOL.optionalFieldOf("clear_replaceable_blocks", Boolean.TRUE)
                            .forGetter(NbtTemplateConfiguration::clearReplaceableBlocks)
            ).apply(instance, NbtTemplateConfiguration::new)
    );
}
