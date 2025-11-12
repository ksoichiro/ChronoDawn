package com.chronosphere.client.renderer.mobs;

import com.chronosphere.client.model.ClockworkSentinelModel;
import com.chronosphere.entities.mobs.ClockworkSentinelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Clockwork Sentinel entity.
 * Uses a custom Enderman-like model with a custom texture.
 *
 * Texture location: assets/chronosphere/textures/entity/mobs/clockwork_sentinel.png
 * Model: Custom model created with Blockbench
 */
public class ClockworkSentinelRenderer extends MobRenderer<ClockworkSentinelEntity, ClockworkSentinelModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "chronosphere",
        "textures/entity/mobs/clockwork_sentinel.png"
    );

    public ClockworkSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new ClockworkSentinelModel(context.bakeLayer(ClockworkSentinelModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ClockworkSentinelEntity entity) {
        return TEXTURE;
    }
}
