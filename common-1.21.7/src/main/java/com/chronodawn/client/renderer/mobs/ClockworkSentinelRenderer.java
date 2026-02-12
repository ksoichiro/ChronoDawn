package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.ClockworkSentinelModel;
import com.chronodawn.entities.mobs.ClockworkSentinelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Clockwork Sentinel entity.
 * Uses a custom Enderman-like model with a custom texture.
 *
 * Texture location: assets/chronodawn/textures/entity/mobs/clockwork_sentinel.png
 * Model: Custom model created with Blockbench
 */
public class ClockworkSentinelRenderer extends MobRenderer<ClockworkSentinelEntity, ClockworkSentinelRenderState, ClockworkSentinelModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/clockwork_sentinel.png"
    );

    public ClockworkSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new ClockworkSentinelModel(context.bakeLayer(ClockworkSentinelModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ClockworkSentinelRenderState createRenderState() {
        return new ClockworkSentinelRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(ClockworkSentinelRenderState state) {
        return TEXTURE;
    }
}
