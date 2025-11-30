package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.ClockworkColossusModel;
import com.chronosphere.entities.bosses.ClockworkColossusEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Clockwork Colossus Renderer
 *
 * Uses ClockworkColossusModel (HumanoidModel-based) with custom texture.
 *
 * Reference: research.md (Additional Bosses - Clockwork Colossus)
 * Task: T235h [Phase 1] Create renderer for Clockwork Colossus
 */
public class ClockworkColossusRenderer extends MobRenderer<ClockworkColossusEntity, ClockworkColossusModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "textures/entity/clockwork_colossus.png"
    );

    // Custom model layer location for Clockwork Colossus
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "clockwork_colossus"),
        "main"
    );

    public ClockworkColossusRenderer(EntityRendererProvider.Context context) {
        super(context, new ClockworkColossusModel(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ClockworkColossusEntity entity) {
        return TEXTURE;
    }
}
