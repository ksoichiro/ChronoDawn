package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.TimeGuardianModel;
import com.chronosphere.entities.bosses.ClockworkColossusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Clockwork Colossus Renderer
 *
 * Uses TimeGuardianModel (same as Time Guardian) with custom texture.
 *
 * Reference: research.md (Additional Bosses - Clockwork Colossus)
 * Task: T235h [Phase 1] Create renderer for Clockwork Colossus
 */
public class ClockworkColossusRenderer extends MobRenderer<ClockworkColossusEntity, TimeGuardianModel<ClockworkColossusEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "textures/entity/clockwork_colossus.png"
    );

    public ClockworkColossusRenderer(EntityRendererProvider.Context context) {
        // Use TimeGuardianModel (same model as Time Guardian)
        super(context, new TimeGuardianModel(context.bakeLayer(TimeGuardianRenderer.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ClockworkColossusEntity entity) {
        return TEXTURE;
    }
}
