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
public class GearProjectileRenderer extends EntityRenderer<GearProjectileEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "textures/entity/gear_projectile.png"
    );

    private final ItemRenderer itemRenderer;

    public GearProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(GearProjectileEntity entity, float entityYaw, float partialTick,
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale to make it visible
        poseStack.scale(0.5f, 0.5f, 0.5f);

        // Rotate based on entity's rotation (spinning effect)
        float rotation = entity.tickCount + partialTick;
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
            entity.level(),
            entity.getId()
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GearProjectileEntity entity) {
        return TEXTURE;
    }
}
