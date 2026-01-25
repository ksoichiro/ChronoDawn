package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.ChronoTurtleModel;
import com.chronodawn.entities.mobs.ChronoTurtleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for ChronoTurtle entity - 1.21.1 version.
 * Uses MobRenderer<Entity, Model> pattern (not RenderState pattern).
 * Texture location: assets/chronodawn/textures/entity/mobs/chrono_turtle.png
 */
public class ChronoTurtleRenderer extends MobRenderer<ChronoTurtleEntity, ChronoTurtleModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/chrono_turtle.png"
    );

    public ChronoTurtleRenderer(EntityRendererProvider.Context context) {
        super(context, new ChronoTurtleModel(context.bakeLayer(ChronoTurtleModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ChronoTurtleEntity entity) {
        return TEXTURE;
    }
}
