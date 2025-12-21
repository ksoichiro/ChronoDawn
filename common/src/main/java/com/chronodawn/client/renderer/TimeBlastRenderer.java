package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.projectiles.TimeBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Custom renderer for Time Blast entity.
 *
 * Renders Time Blast as a small glowing sphere (magical projectile).
 * The visual effect is enhanced by particles emitted from TimeBlastEntity.tick().
 *
 * Reference: T210 - Add ranged attack capability to Time Guardian
 */
public class TimeBlastRenderer extends EntityRenderer<TimeBlastEntity> {
    /**
     * Texture location for Time Blast entity (uses a simple glowing texture).
     */
    private static final ResourceLocation TEXTURE_LOCATION =
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "textures/entity/projectiles/time_blast.png");

    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

    public TimeBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TimeBlastEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale the projectile to be small (0.3 blocks)
        poseStack.scale(0.3f, 0.3f, 0.3f);

        // Rotate to face the camera (billboard effect)
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);

        // Render a quad (billboard) with the texture
        // Vertex order: bottom-left, top-left, top-right, bottom-right
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, -0.5f, -0.5f, 0, 0, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, -0.5f, 0.5f, 0, 0, 0);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.5f, 0.5f, 0, 1, 0);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.5f, -0.5f, 0, 1, 1);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Helper method to add a vertex to the buffer.
     */
    private void vertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f, int packedLight,
                        float x, float y, float z, float u, float v) {
        consumer.addVertex(matrix4f, x, y, z)
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(TimeBlastEntity entity) {
        return TEXTURE_LOCATION;
    }
}
