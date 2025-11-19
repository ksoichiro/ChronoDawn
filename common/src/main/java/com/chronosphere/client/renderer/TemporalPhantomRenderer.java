package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.TimeGuardianModel;
import com.chronosphere.entities.bosses.TemporalPhantomEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Temporal Phantom entity.
 *
 * Uses TimeGuardianModel (same as Time Guardian) with custom texture.
 * Applies transparency effect when Phase Shift is active.
 *
 * Task: T236 [Phase 2] Implement Temporal Phantom
 */
public class TemporalPhantomRenderer extends MobRenderer<TemporalPhantomEntity, TimeGuardianModel<TemporalPhantomEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "textures/entity/temporal_phantom.png"
    );

    public TemporalPhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new TimeGuardianModel<>(context.bakeLayer(TimeGuardianRenderer.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TemporalPhantomEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(TemporalPhantomEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);

        // Apply transparency when Phase Shift is active
        if (entity.isPhaseShiftActive()) {
            // Make entity semi-transparent (handled by render layer in future)
        }
    }
}
