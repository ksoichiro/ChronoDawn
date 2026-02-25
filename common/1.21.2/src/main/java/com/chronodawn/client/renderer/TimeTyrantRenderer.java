package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.TimeTyrantModel;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Time Tyrant entity.
 *
 * Uses custom TimeTyrantModel (placeholder - to be replaced with Blockbench model).
 *
 * Reference: T134-T135 [US3] Time Tyrant entity creation and registration
 */
public class TimeTyrantRenderer extends MobRenderer<TimeTyrantEntity, TimeTyrantRenderState, TimeTyrantModel> {
    // Custom texture for Time Tyrant
    // TODO: Create custom texture for Time Tyrant in textures/entity/time_tyrant.png
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/time_tyrant.png"
    );

    // Model layer location for Time Tyrant
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "time_tyrant"),
        "main"
    );

    public TimeTyrantRenderer(EntityRendererProvider.Context context) {
        super(context, new TimeTyrantModel(context.bakeLayer(LAYER_LOCATION)), 0.75f); // Larger shadow than Time Guardian
    }

    @Override
    public TimeTyrantRenderState createRenderState() {
        return new TimeTyrantRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(TimeTyrantRenderState state) {
        return TEXTURE;
    }
}
