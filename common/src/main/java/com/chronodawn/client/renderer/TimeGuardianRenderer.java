package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.TimeGuardianModel;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Time Guardian entity.
 *
 * Uses custom TimeGuardianModel created with Blockbench.
 *
 * Reference: T110-T115 [US2] Time Guardian implementation
 * Reference: T115a [US2] Custom model and texture
 */
public class TimeGuardianRenderer extends MobRenderer<TimeGuardianEntity, TimeGuardianModel<TimeGuardianEntity>> {
    // Custom texture for Time Guardian
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/time_guardian.png"
    );

    // Model layer location for Time Guardian
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "time_guardian"),
        "main"
    );

    public TimeGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new TimeGuardianModel(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TimeGuardianEntity entity) {
        return TEXTURE;
    }
}
