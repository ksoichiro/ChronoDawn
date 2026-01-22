package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.chronodawn.entities.boats.ChronoDawnChestBoat;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.Map;

/**
 * Custom chest boat renderer for ChronoDawn mod boats.
 * Uses vanilla chest boat model with custom textures for each Time Wood variant.
 *
 * Task: T268-T270 [US1] Create Time Wood Chest Boat renderers
 */
public class ChronoDawnChestBoatRenderer extends EntityRenderer<ChronoDawnChestBoat, ChronoDawnChestBoatRenderState> {

    private final Map<ChronoDawnBoatType, Pair<ResourceLocation, BoatModel>> boatResources;
    private final Model waterPatchModel;

    public ChronoDawnChestBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.boatResources = createBoatResources(context);
        // Create water patch model from vanilla layer (masks water inside boat)
        this.waterPatchModel = new Model.Simple(
            context.bakeLayer(ModelLayers.BOAT_WATER_PATCH),
            resourceLocation -> RenderType.waterMask()
        );
    }

    @Override
    public ChronoDawnChestBoatRenderState createRenderState() {
        return new ChronoDawnChestBoatRenderState();
    }

    @Override
    public void extractRenderState(ChronoDawnChestBoat entity, ChronoDawnChestBoatRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Set custom boat type
        state.boatType = entity.getChronoDawnBoatType();

        // Set BoatRenderState specific fields (not set by EntityRenderer.extractRenderState)
        state.yRot = entity.getYRot(partialTick);
        state.hurtTime = (float) entity.getHurtTime() - partialTick;
        state.hurtDir = entity.getHurtDir();
        state.damageTime = Math.max(0.0F, entity.getDamage() - partialTick);
        state.bubbleAngle = entity.getBubbleAngle(partialTick);
        state.isUnderWater = entity.isUnderWater();
        state.rowingTimeLeft = entity.getRowingTime(0, partialTick);
        state.rowingTimeRight = entity.getRowingTime(1, partialTick);
    }

    private static Map<ChronoDawnBoatType, Pair<ResourceLocation, BoatModel>> createBoatResources(
            EntityRendererProvider.Context context) {

        ImmutableMap.Builder<ChronoDawnBoatType, Pair<ResourceLocation, BoatModel>> builder = ImmutableMap.builder();

        for (ChronoDawnBoatType type : ChronoDawnBoatType.values()) {
            String texturePath = "textures/entity/chest_boat/" + type.getName() + ".png";
            ResourceLocation texture = CompatResourceLocation.create(ChronoDawn.MOD_ID, texturePath);

            // Use vanilla chest boat model layer (1.21.2: Boat.Type removed, using direct ModelLayerLocation)
            // Create custom ModelLayerLocation for our chest boat
            ModelLayerLocation modelLayer = new ModelLayerLocation(
                CompatResourceLocation.create(ChronoDawn.MOD_ID, "chest_boat/" + type.getName()),
                "main"
            );
            ModelPart modelPart = context.bakeLayer(modelLayer);
            BoatModel model = new BoatModel(modelPart);

            builder.put(type, Pair.of(texture, model));
        }

        return builder.build();
    }

    @Override
    public void render(ChronoDawnChestBoatRenderState state, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));

        Pair<ResourceLocation, BoatModel> pair = getModelWithLocation(state);
        ResourceLocation texture = pair.getFirst();
        BoatModel model = pair.getSecond();

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        model.setupAnim(state);

        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(texture));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        // Render water patch to mask water inside boat (only when not fully submerged)
        if (!state.isUnderWater) {
            this.waterPatchModel.renderToBuffer(
                poseStack,
                buffer.getBuffer(RenderType.waterMask()),
                packedLight,
                OverlayTexture.NO_OVERLAY
            );
        }

        poseStack.popPose();
        super.render(state, poseStack, buffer, packedLight);
    }

    private Pair<ResourceLocation, BoatModel> getModelWithLocation(ChronoDawnChestBoatRenderState state) {
        return boatResources.get(state.boatType);
    }

    // Note: getTextureLocation() removed in 1.21.2
    // Texture is now obtained directly in render() method via getModelWithLocation()
}
