package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.EpochHuskModel;
import com.chronodawn.entities.mobs.EpochHuskEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Epoch Husk entity.
 * Uses a custom zombie-like model with a custom texture.
 *
 * Texture location: assets/chronodawn/textures/entity/mobs/epoch_husk.png
 * Model: Custom model created with Blockbench
 */
public class EpochHuskRenderer extends MobRenderer<EpochHuskEntity, EpochHuskModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/epoch_husk.png"
    );

    public EpochHuskRenderer(EntityRendererProvider.Context context) {
        super(context, new EpochHuskModel(context.bakeLayer(EpochHuskModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EpochHuskEntity entity) {
        return TEXTURE;
    }
}
