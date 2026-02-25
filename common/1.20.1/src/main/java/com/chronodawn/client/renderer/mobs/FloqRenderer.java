package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.FloqModel;
import com.chronodawn.entities.mobs.FloqEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renderer for Floq entity.
 * Uses a custom slime-like model with a custom texture.
 *
 * Texture location: assets/chronodawn/textures/entity/mobs/floq.png
 * Model: Custom model created with Blockbench
 */
public class FloqRenderer extends MobRenderer<FloqEntity, FloqModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/floq.png"
    );

    public FloqRenderer(EntityRendererProvider.Context context) {
        super(context, new FloqModel(context.bakeLayer(FloqModel.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(FloqEntity entity) {
        return TEXTURE;
    }

    /**
     * Apply scale transformation for squish animation
     */
    @Override
    protected void scale(FloqEntity entity, PoseStack poseStack, float partialTick) {
        // Interpolate squish value for smooth animation between ticks
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish);

        // Apply squash and stretch
        // Positive squish = stretched (jumping): tall and thin
        // Negative squish = squashed (landing): short and wide
        float yScale = (squish * 0.5F + 1.0F);
        float xzScale = 1.0F / yScale; // Inverse to maintain volume

        // Apply scale first
        poseStack.scale(xzScale, yScale, xzScale);

        // Then translate AFTER scaling to keep bottom at ground level
        if (entity.onGround()) {
            // Updated model with PartPose.offset(0, 1, 0):
            // All parts are offset 1 block (16 pixels) up
            // Body in model space: y=17 to y=23 (height=6, center at y=20)
            // After 1-block offset: effective y=1 to y=7 in world space
            // Ground level is at y=0, so center is at y=4 in world space
            // In block units: 4/16 = 0.25 blocks from ground to center
            //
            // After scaling, the center is at 0.25 * yScale blocks above ground
            // We want it at 0.25 blocks, so we translate by: 0.25 * (1 - yScale)
            // BUT since we already scaled, this translate happens in scaled space
            // So we need: (0.25 * (1 - yScale)) / yScale = 0.25 * (1/yScale - 1)
            float modelCenterHeight = 0.25F; // 4/16 blocks from ground to center
            float yOffset = modelCenterHeight * (1.0F / yScale - 1.0F);
            poseStack.translate(0.0F, yOffset, 0.0F);
        }
    }
}
