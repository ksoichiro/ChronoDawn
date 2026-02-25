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
public class MomentCreeperRenderer extends MobRenderer<MomentCreeperEntity, MomentCreeperRenderState, MomentCreeperModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/moment_creeper.png"
    );

    public MomentCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new MomentCreeperModel(context.bakeLayer(MomentCreeperModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public MomentCreeperRenderState createRenderState() {
        return new MomentCreeperRenderState();
    }

    @Override
    public void extractRenderState(MomentCreeperEntity entity, MomentCreeperRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Extract swelling value for model animation (pre-interpolated)
        state.swelling = entity.getSwelling(partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(MomentCreeperRenderState state) {
        return TEXTURE;
    }

    // Note: getWhiteOverlayProgress was removed in 1.21.2
    // White overlay effect for explosion will need to be implemented via RenderLayer in the future
}
