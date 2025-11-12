package com.chronosphere.client.renderer.mobs;

import com.chronosphere.client.model.TemporalWraithModel;
import com.chronosphere.entities.mobs.TemporalWraithEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Temporal Wraith entity.
 * Uses a custom humanoid model with a custom texture.
 *
 * Texture location: assets/chronosphere/textures/entity/mobs/temporal_wraith.png
 * Model: Custom model created with Blockbench
 */
public class TemporalWraithRenderer extends MobRenderer<TemporalWraithEntity, TemporalWraithModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "chronosphere",
        "textures/entity/mobs/temporal_wraith.png"
    );

    public TemporalWraithRenderer(EntityRendererProvider.Context context) {
        super(context, new TemporalWraithModel(context.bakeLayer(TemporalWraithModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TemporalWraithEntity entity) {
        return TEXTURE;
    }
}

