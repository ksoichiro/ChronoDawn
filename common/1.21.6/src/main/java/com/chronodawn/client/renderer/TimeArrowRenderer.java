package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.projectiles.TimeArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Custom renderer for Time Arrow entity.
 *
 * Uses a custom texture to distinguish Time Arrows from regular arrows
 * when they are flying through the air.
 *
 * Texture location: assets/chronodawn/textures/entity/projectiles/time_arrow.png
 *
 * Reference: T171g - Boss Battle Balance Enhancement
 */
public class TimeArrowRenderer extends ArrowRenderer<TimeArrowEntity, TimeArrowRenderState> {
    /**
     * Texture location for Time Arrow entity.
     */
    private static final ResourceLocation TEXTURE_LOCATION =
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "textures/entity/projectiles/time_arrow.png");

    public TimeArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TimeArrowRenderState createRenderState() {
        return new TimeArrowRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(TimeArrowRenderState state) {
        return TEXTURE_LOCATION;
    }
}
