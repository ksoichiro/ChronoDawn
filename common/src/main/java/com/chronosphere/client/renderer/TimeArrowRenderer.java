package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.projectiles.TimeArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom renderer for Time Arrow entity.
 *
 * Uses a custom texture to distinguish Time Arrows from regular arrows
 * when they are flying through the air.
 *
 * Texture location: assets/chronosphere/textures/entity/projectiles/time_arrow.png
 *
 * Reference: T171g - Boss Battle Balance Enhancement
 */
public class TimeArrowRenderer extends ArrowRenderer<TimeArrowEntity> {
    /**
     * Texture location for Time Arrow entity.
     */
    private static final ResourceLocation TEXTURE_LOCATION =
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "textures/entity/projectiles/time_arrow.png");

    public TimeArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TimeArrowEntity entity) {
        return TEXTURE_LOCATION;
    }
}
