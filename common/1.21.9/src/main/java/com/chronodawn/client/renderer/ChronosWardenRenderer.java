package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.ChronosWardenModel;
import com.chronodawn.entities.bosses.ChronosWardenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Chronos Warden entity.
 *
 * Uses custom ChronosWardenModel created with Blockbench.
 *
 * Task: T234e [Phase 1] Create ChronosWardenRenderer
 */
public class ChronosWardenRenderer extends MobRenderer<ChronosWardenEntity, ChronosWardenRenderState, ChronosWardenModel> {
    // Custom texture for Chronos Warden
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/chronos_warden.png"
    );

    public ChronosWardenRenderer(EntityRendererProvider.Context context) {
        // Use ChronosWardenModel with custom model layer
        super(context, new ChronosWardenModel(context.bakeLayer(ChronosWardenModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ChronosWardenRenderState createRenderState() {
        return new ChronosWardenRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(ChronosWardenRenderState state) {
        return TEXTURE;
    }
}
