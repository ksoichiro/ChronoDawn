package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.TimelineStriderModel;
import com.chronodawn.entities.mobs.TimelineStriderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Timeline Strider entity.
 */
public class TimelineStriderRenderer extends MobRenderer<TimelineStriderEntity, TimelineStriderModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn", "textures/entity/mobs/timeline_strider.png"
    );

    public TimelineStriderRenderer(EntityRendererProvider.Context context) {
        super(context, new TimelineStriderModel(context.bakeLayer(TimelineStriderModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TimelineStriderEntity entity) {
        return TEXTURE;
    }
}
