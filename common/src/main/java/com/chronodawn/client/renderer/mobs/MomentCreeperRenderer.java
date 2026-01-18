package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.MomentCreeperModel;
import com.chronodawn.entities.mobs.MomentCreeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Moment Creeper entity.
 * Uses a custom creeper-like model with glass-like body containing frozen explosion effect.
 *
 * Texture location: assets/chronodawn/textures/entity/mobs/moment_creeper.png
 * Model: Custom model created with Blockbench
 */
public class MomentCreeperRenderer extends MobRenderer<MomentCreeperEntity, MomentCreeperModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/moment_creeper.png"
    );

    public MomentCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new MomentCreeperModel(context.bakeLayer(MomentCreeperModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(MomentCreeperEntity entity) {
        return TEXTURE;
    }
}
