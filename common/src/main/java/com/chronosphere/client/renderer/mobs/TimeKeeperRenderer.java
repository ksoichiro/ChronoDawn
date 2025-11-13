package com.chronosphere.client.renderer.mobs;

import com.chronosphere.client.model.TimeKeeperModel;
import com.chronosphere.entities.mobs.TimeKeeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Time Keeper entity.
 * Uses a custom villager-like model with a custom texture.
 *
 * Texture location: assets/chronosphere/textures/entity/mobs/time_keeper.png
 * Model: Custom model created with Blockbench
 */
public class TimeKeeperRenderer extends MobRenderer<TimeKeeperEntity, TimeKeeperModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "chronosphere",
        "textures/entity/mobs/time_keeper.png"
    );

    public TimeKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new TimeKeeperModel(context.bakeLayer(TimeKeeperModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TimeKeeperEntity entity) {
        return TEXTURE;
    }
}

