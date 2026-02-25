package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.ClockworkColossusModel;
import com.chronodawn.entities.bosses.ClockworkColossusEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Clockwork Colossus Renderer
 *
 * Uses ClockworkColossusModel (HumanoidModel-based) with custom texture.
 *
 * Reference: research.md (Additional Bosses - Clockwork Colossus)
 * Task: T235h [Phase 1] Create renderer for Clockwork Colossus
 */
public class ClockworkColossusRenderer extends MobRenderer<ClockworkColossusEntity, ClockworkColossusRenderState, ClockworkColossusModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/clockwork_colossus.png"
    );

    // Custom model layer location for Clockwork Colossus
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "clockwork_colossus"),
        "main"
    );

    public ClockworkColossusRenderer(EntityRendererProvider.Context context) {
        super(context, new ClockworkColossusModel(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ClockworkColossusRenderState createRenderState() {
        return new ClockworkColossusRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(ClockworkColossusRenderState state) {
        return TEXTURE;
    }
}
