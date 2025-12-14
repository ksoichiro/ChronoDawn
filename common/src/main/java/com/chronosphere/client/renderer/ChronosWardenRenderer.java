package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.ChronosWardenModel;
import com.chronosphere.entities.bosses.ChronosWardenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Chronos Warden entity.
 *
 * Uses custom ChronosWardenModel created with Blockbench.
 *
 * Task: T234e [Phase 1] Create ChronosWardenRenderer
 */
public class ChronosWardenRenderer extends MobRenderer<ChronosWardenEntity, ChronosWardenModel> {
    // Custom texture for Chronos Warden
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "textures/entity/chronos_warden.png"
    );

    public ChronosWardenRenderer(EntityRendererProvider.Context context) {
        // Use ChronosWardenModel with custom model layer
        super(context, new ChronosWardenModel(context.bakeLayer(ChronosWardenModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ChronosWardenEntity entity) {
        return TEXTURE;
    }
}
