package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.MomentCreeperModel;
import com.chronodawn.compat.CompatResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders a white flash over Moment Creeper as it nears explosion.
 * Replaces the vanilla MobRenderer#getWhiteOverlayProgress hook removed in 1.21.2.
 */
public class MomentCreeperWhiteFlashLayer extends RenderLayer<MomentCreeperRenderState, MomentCreeperModel> {
    private static final ResourceLocation WHITE_TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/moment_creeper_white.png"
    );
    private static final RenderType WHITE_FLASH = RenderType.entityTranslucent(WHITE_TEXTURE);

    public MomentCreeperWhiteFlashLayer(RenderLayerParent<MomentCreeperRenderState, MomentCreeperModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       MomentCreeperRenderState state, float yRot, float xRot) {
        float intensity = flashIntensity(state.swelling);
        if (intensity <= 0.0F) {
            return;
        }

        int alpha = (int)(intensity * 255.0F);
        int color = (alpha << 24) | 0x00FFFFFF;

        collector.submitModel(this.getParentModel(), state, poseStack, WHITE_FLASH,
            15728880, OverlayTexture.NO_OVERLAY, color, null);
    }

    /**
     * Fades in smoothly from 0.5-0.8 swelling, then blinks rapidly from 0.8 onward,
     * mirroring vanilla Creeper's strobing white overlay just before it explodes.
     */
    static float flashIntensity(float swelling) {
        if (swelling <= 0.5F) {
            return 0.0F;
        }
        if (swelling < 0.8F) {
            return (swelling - 0.5F) / 0.3F;
        }
        return (int)((swelling - 0.8F) * 20.0F) % 2 == 0 ? 1.0F : 0.0F;
    }
}
