package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.TimelineStriderModel;
import com.chronodawn.entities.mobs.TimelineStriderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Timeline Strider entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/timeline_strider.png
 */
public class TimelineStriderRenderer extends MobRenderer<TimelineStriderEntity, TimelineStriderRenderState, TimelineStriderModel> {
    private static final Identifier TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/timeline_strider.png"
    );

    public TimelineStriderRenderer(EntityRendererProvider.Context context) {
        super(context, new TimelineStriderModel(context.bakeLayer(TimelineStriderModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new TimelineStriderEmissiveLayer(this));
    }

    @Override
    public TimelineStriderRenderState createRenderState() {
        return new TimelineStriderRenderState();
    }

    @Override
    public Identifier getTextureLocation(TimelineStriderRenderState state) {
        return TEXTURE;
    }
}
