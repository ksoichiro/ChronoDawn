package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.ChronoTurtleModel;
import com.chronodawn.entities.mobs.ChronoTurtleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for ChronoTurtle entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/chrono_turtle.png
 */
public class ChronoTurtleRenderer extends MobRenderer<ChronoTurtleEntity, ChronoTurtleRenderState, ChronoTurtleModel> {
    private static final Identifier TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/chrono_turtle.png"
    );

    public ChronoTurtleRenderer(EntityRendererProvider.Context context) {
        super(context, new ChronoTurtleModel(context.bakeLayer(ChronoTurtleModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ChronoTurtleRenderState createRenderState() {
        return new ChronoTurtleRenderState();
    }

    @Override
    public Identifier getTextureLocation(ChronoTurtleRenderState state) {
        return TEXTURE;
    }
}
