package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.ChronalLeechModel;
import com.chronodawn.entities.mobs.ChronalLeechEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Chronal Leech entity.
 * Uses a custom silverfish-like model with segmented body and wings.
 *
 * Texture location: assets/chronodawn/textures/entity/mobs/chronal_leech.png
 * Model: Custom model created with Blockbench
 */
public class ChronalLeechRenderer extends MobRenderer<ChronalLeechEntity, ChronalLeechModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/chronal_leech.png"
    );

    public ChronalLeechRenderer(EntityRendererProvider.Context context) {
        super(context, new ChronalLeechModel(context.bakeLayer(ChronalLeechModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(ChronalLeechEntity entity) {
        return TEXTURE;
    }
}
