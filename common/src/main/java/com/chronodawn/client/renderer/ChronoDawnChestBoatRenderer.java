package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.chronodawn.entities.boats.ChronoDawnChestBoat;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.ChestBoatModel;
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
 * Custom chest boat renderer for ChronoDawn mod boats.
 * Uses vanilla chest boat model with custom textures for each Time Wood variant.
 *
 * Task: T268-T270 [US1] Create Time Wood Chest Boat renderers
 */
public class ChronoDawnChestBoatRenderer extends EntityRenderer<ChronoDawnChestBoat> {

    private final Map<ChronoDawnBoatType, Pair<ResourceLocation, ChestBoatModel>> boatResources;

    public ChronoDawnChestBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.boatResources = createBoatResources(context);
    }

    private static Map<ChronoDawnBoatType, Pair<ResourceLocation, ChestBoatModel>> createBoatResources(
            EntityRendererProvider.Context context) {

        ImmutableMap.Builder<ChronoDawnBoatType, Pair<ResourceLocation, ChestBoatModel>> builder = ImmutableMap.builder();

        for (ChronoDawnBoatType type : ChronoDawnBoatType.values()) {
            String texturePath = "textures/entity/chest_boat/" + type.getName() + ".png";
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, texturePath);

            // Use vanilla chest boat model layer
            ModelLayerLocation modelLayer = net.minecraft.client.model.geom.ModelLayers.createChestBoatModelName(Boat.Type.OAK);
            ModelPart modelPart = context.bakeLayer(modelLayer);
            ChestBoatModel model = new ChestBoatModel(modelPart);

            builder.put(type, Pair.of(texture, model));
        }

        return builder.build();
    }

    @Override
    public void render(ChronoDawnChestBoat entity, float entityYaw, float partialTicks, PoseStack poseStack,
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

        Pair<ResourceLocation, ChestBoatModel> pair = getModelWithLocation(entity);
        ResourceLocation texture = pair.getFirst();
        ChestBoatModel model = pair.getSecond();

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

    private Pair<ResourceLocation, ChestBoatModel> getModelWithLocation(ChronoDawnChestBoat boat) {
        return boatResources.get(boat.getChronoDawnBoatType());
    }

    @Override
    public ResourceLocation getTextureLocation(ChronoDawnChestBoat entity) {
        return getModelWithLocation(entity).getFirst();
    }
}
