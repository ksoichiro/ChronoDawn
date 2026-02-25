package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.HourglassGolemModel;
import com.chronodawn.entities.mobs.HourglassGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Hourglass Golem entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/hourglass_golem.png
 */
public class HourglassGolemRenderer extends MobRenderer<HourglassGolemEntity, HourglassGolemRenderState, HourglassGolemModel> {
    private static final Identifier TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/hourglass_golem.png"
    );

    public HourglassGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new HourglassGolemModel(context.bakeLayer(HourglassGolemModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public HourglassGolemRenderState createRenderState() {
        return new HourglassGolemRenderState();
    }

    @Override
    public Identifier getTextureLocation(HourglassGolemRenderState state) {
        return TEXTURE;
    }
}
