package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.projectiles.GearProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
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
 * Updated for 1.21.4 Client Items system.
 */
public class GearProjectileRenderer extends EntityRenderer<GearProjectileEntity, GearProjectileRenderState> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/gear_projectile.png"
    );

    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();

    public GearProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
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

        // Render as iron ingot (placeholder for gear) using 1.21.5 Client Items system
        // In 1.21.5, updateForTopItem signature changed to (state, stack, context, level, entity, seed)
        ItemStack stack = new ItemStack(Items.IRON_INGOT);
        this.itemModelResolver.updateForTopItem(
            itemRenderState,
            stack,
            ItemDisplayContext.GROUND,
            Minecraft.getInstance().level,  // Level from client
            null,  // No LivingEntity for projectile
            0      // Seed for randomization
        );
        itemRenderState.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        super.render(state, poseStack, buffer, packedLight);
    }

    // Note: getTextureLocation() removed in 1.21.2
    // Texture is obtained directly in render() method
}
