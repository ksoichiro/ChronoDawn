package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.ForgottenMinuteModel;
import com.chronodawn.entities.mobs.ForgottenMinuteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Forgotten Minute entity.
 * Uses a custom flying creature model with wings and a semi-transparent appearance.
 *
 * Texture location: assets/chronodawn/textures/entity/mobs/forgotten_minute.png
 * Model: Custom model created with Blockbench
 */
public class ForgottenMinuteRenderer extends MobRenderer<ForgottenMinuteEntity, ForgottenMinuteModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/forgotten_minute.png"
    );

    public ForgottenMinuteRenderer(EntityRendererProvider.Context context) {
        super(context, new ForgottenMinuteModel(context.bakeLayer(ForgottenMinuteModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(ForgottenMinuteEntity entity) {
        return TEXTURE;
    }
}
