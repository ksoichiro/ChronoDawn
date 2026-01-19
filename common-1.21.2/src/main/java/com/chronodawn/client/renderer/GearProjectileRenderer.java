package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.projectiles.GearProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Renderer for Gear Projectile entity.
 *
 * Renders as a rotating iron ingot (placeholder for gear model).
 * In future, can be replaced with custom gear model.
 *
 * Task: T235b [P] GearProjectileEntity renderer
 */
public class GearProjectileRenderer extends EntityRenderer<GearProjectileEntity, GearProjectileRenderState> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/gear_projectile.png"
    );

    private final ItemRenderer itemRenderer;

    public GearProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public GearProjectileRenderState createRenderState() {
        return new GearProjectileRenderState();
    }

    @Override
    public void extractRenderState(GearProjectileEntity entity, GearProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.tickCount = entity.tickCount;
    }

    @Override
    public void render(GearProjectileRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale to make it visible
        poseStack.scale(0.5f, 0.5f, 0.5f);

        // Rotate based on entity's rotation (spinning effect)
        float rotation = state.tickCount + state.ageInTicks;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 20.0f)); // Spin around Y axis
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 15.0f)); // Wobble on Z axis

        // Render as iron ingot (placeholder for gear)
        ItemStack stack = new ItemStack(Items.IRON_INGOT);
        this.itemRenderer.renderStatic(
            stack,
            ItemDisplayContext.GROUND,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            null, // level is no longer available in render state
            0 // entity ID is no longer available
        );

        poseStack.popPose();

        super.render(state, poseStack, buffer, packedLight);
    }

    // Note: getTextureLocation() removed in 1.21.2
    // Texture is obtained directly in render() method
}
