package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.ParadoxCrawlerModel;
import com.chronodawn.entities.mobs.ParadoxCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Paradox Crawler entity.
 */
public class ParadoxCrawlerRenderer extends MobRenderer<ParadoxCrawlerEntity, ParadoxCrawlerModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/paradox_crawler.png"
    );

    public ParadoxCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new ParadoxCrawlerModel(context.bakeLayer(ParadoxCrawlerModel.LAYER_LOCATION)), 0.8f);
    }

    @Override
    public ResourceLocation getTextureLocation(ParadoxCrawlerEntity entity) {
        return TEXTURE;
    }
}
