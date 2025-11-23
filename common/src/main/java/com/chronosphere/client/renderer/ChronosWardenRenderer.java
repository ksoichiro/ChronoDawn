package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.TimeGuardianModel;
import com.chronosphere.entities.bosses.ChronosWardenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Chronos Warden entity.
 *
 * Uses TimeGuardianModel (same as Time Guardian) with custom texture.
 *
 * Task: T234e [Phase 1] Create ChronosWardenRenderer
 */
public class ChronosWardenRenderer extends MobRenderer<ChronosWardenEntity, TimeGuardianModel<ChronosWardenEntity>> {
    // Custom texture for Chronos Warden (stone guardian appearance)
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "textures/entity/chronos_warden.png"
    );

    public ChronosWardenRenderer(EntityRendererProvider.Context context) {
        // Use TimeGuardianModel (same model as Time Guardian)
        super(context, new TimeGuardianModel(context.bakeLayer(TimeGuardianRenderer.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ChronosWardenEntity entity) {
        return TEXTURE;
    }
}
