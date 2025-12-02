package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.boats.ChronosphereBoat;
import com.chronosphere.entities.boats.ChronosphereBoatType;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.Map;

/**
 * Custom boat renderer for Chronosphere mod boats.
 * Uses vanilla boat model with custom textures for each Time Wood variant.
 *
 * Task: T268-T270 [US1] Create Time Wood Boat renderers
 */
public class ChronosphereBoatRenderer extends EntityRenderer<ChronosphereBoat> {

    private final Map<ChronosphereBoatType, Pair<ResourceLocation, BoatModel>> boatResources;

    public ChronosphereBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.boatResources = createBoatResources(context);
    }

    private static Map<ChronosphereBoatType, Pair<ResourceLocation, BoatModel>> createBoatResources(
            EntityRendererProvider.Context context) {

        ImmutableMap.Builder<ChronosphereBoatType, Pair<ResourceLocation, BoatModel>> builder = ImmutableMap.builder();

        for (ChronosphereBoatType type : ChronosphereBoatType.values()) {
            String texturePath = "textures/entity/boat/" + type.getName() + ".png";
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, texturePath);

            // Use vanilla boat model layer
            ModelLayerLocation modelLayer = net.minecraft.client.model.geom.ModelLayers.createBoatModelName(Boat.Type.OAK);
            ModelPart modelPart = context.bakeLayer(modelLayer);
            BoatModel model = new BoatModel(modelPart);

            builder.put(type, Pair.of(texture, model));
        }

        return builder.build();
    }

    @Override
    public void render(ChronosphereBoat entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        float hurtTime = (float) entity.getHurtTime() - partialTicks;
        float damage = entity.getDamage() - partialTicks;

        if (damage < 0.0F) {
            damage = 0.0F;
        }

        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) entity.getHurtDir()));
        }

        float bubbleAngle = entity.getBubbleAngle(partialTicks);
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(entity.getBubbleAngle(partialTicks)));
        }

        Pair<ResourceLocation, BoatModel> pair = getModelWithLocation(entity);
        ResourceLocation texture = pair.getFirst();
        BoatModel model = pair.getSecond();

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        model.setupAnim(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(texture));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        // Render water patch inside boat
        if (!entity.isUnderWater()) {
            VertexConsumer waterVertexConsumer = buffer.getBuffer(RenderType.waterMask());
            model.waterPatch().render(poseStack, waterVertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private Pair<ResourceLocation, BoatModel> getModelWithLocation(ChronosphereBoat boat) {
        return boatResources.get(boat.getChronosphereBoatType());
    }

    @Override
    public ResourceLocation getTextureLocation(ChronosphereBoat entity) {
        return getModelWithLocation(entity).getFirst();
    }
}
