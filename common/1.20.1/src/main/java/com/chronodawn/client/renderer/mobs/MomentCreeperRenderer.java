package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.MomentCreeperModel;
import com.chronodawn.entities.mobs.MomentCreeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Moment Creeper entity.
 * Uses a custom creeper-like model with glass-like body containing frozen explosion effect.
 *
 * Texture location: assets/chronodawn/textures/entity/mobs/moment_creeper.png
 * Model: Custom model created with Blockbench
 */
public class MomentCreeperRenderer extends MobRenderer<MomentCreeperEntity, MomentCreeperModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/moment_creeper.png"
    );

    public MomentCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new MomentCreeperModel(context.bakeLayer(MomentCreeperModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(MomentCreeperEntity entity) {
        return TEXTURE;
    }

    /**
     * Flash white when close to exploding
     * Returns a value between 0.0 and 1.0 for the white overlay intensity
     */
    @Override
    protected float getWhiteOverlayProgress(MomentCreeperEntity entity, float partialTick) {
        float swelling = entity.getSwelling(partialTick);
        // Continuous white flash that increases with swelling (only when swelling > 0.5)
        if (swelling > 0.5f) {
            // Map 0.5-1.0 to 0.0-1.0 for overlay intensity
            return (swelling - 0.5f) * 2.0f;
        }
        return 0.0f;
    }
}
