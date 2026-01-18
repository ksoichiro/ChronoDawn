package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.TemporalPhantomModel;
import com.chronodawn.entities.bosses.TemporalPhantomEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Temporal Phantom entity.
 *
 * Uses custom TemporalPhantomModel with custom texture.
 * Applies transparency effect when Phase Shift is active.
 *
 * Task: T236 [Phase 2] Implement Temporal Phantom
 * Task: T236s [US3] Create custom texture for Temporal Phantom
 */
public class TemporalPhantomRenderer extends MobRenderer<TemporalPhantomEntity, TemporalPhantomRenderState, TemporalPhantomModel<TemporalPhantomRenderState>> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/temporal_phantom.png"
    );

    // Model layer location for Temporal Phantom
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "temporal_phantom"),
        "main"
    );

    public TemporalPhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new TemporalPhantomModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public TemporalPhantomRenderState createRenderState() {
        return new TemporalPhantomRenderState();
    }

    @Override
    public void extractRenderState(TemporalPhantomEntity entity, TemporalPhantomRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.phaseShiftActive = entity.isPhaseShiftActive();
    }

    @Override
    public ResourceLocation getTextureLocation(TemporalPhantomRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(TemporalPhantomRenderState state, PoseStack poseStack, float partialTick) {
        super.scale(state, poseStack, partialTick);

        // Apply transparency when Phase Shift is active
        if (state.phaseShiftActive) {
            // Make entity semi-transparent (handled by render layer in future)
        }
    }

    /**
     * Override to use translucent render type for transparency support.
     * This allows the texture's alpha channel to be properly rendered.
     */
    @Override
    public RenderType getRenderType(TemporalPhantomRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        ResourceLocation texture = this.getTextureLocation(state);
        return RenderType.entityTranslucent(texture);
    }
}
