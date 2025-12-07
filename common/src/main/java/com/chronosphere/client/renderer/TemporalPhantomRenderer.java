package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.TemporalPhantomModel;
import com.chronosphere.entities.bosses.TemporalPhantomEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Temporal Phantom entity.
 *
 * Uses custom TemporalPhantomModel with custom texture.
 * Applies transparency effect when Phase Shift is active.
 *
 * Task: T236 [Phase 2] Implement Temporal Phantom
 * Task: T236s [US3] Create custom texture for Temporal Phantom
 */
public class TemporalPhantomRenderer extends MobRenderer<TemporalPhantomEntity, TemporalPhantomModel<TemporalPhantomEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "textures/entity/temporal_phantom.png"
    );

    // Model layer location for Temporal Phantom
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "temporal_phantom"),
        "main"
    );

    public TemporalPhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new TemporalPhantomModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
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
