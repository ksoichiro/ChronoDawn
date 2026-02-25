package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.GlideFishModel;
import com.chronodawn.entities.mobs.GlideFishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for GlideFish entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/glide_fish.png
 */
public class GlideFishRenderer extends MobRenderer<GlideFishEntity, GlideFishModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/glide_fish.png"
    );

    public GlideFishRenderer(EntityRendererProvider.Context context) {
        super(context, new GlideFishModel(context.bakeLayer(GlideFishModel.LAYER_LOCATION)), 0.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(GlideFishEntity entity) {
        return TEXTURE;
    }
}
