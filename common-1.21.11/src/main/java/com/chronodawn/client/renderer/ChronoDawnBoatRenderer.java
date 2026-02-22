package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.boats.ChronoDawnBoat;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import com.chronodawn.compat.CompatResourceLocation;

import java.util.Map;

/**
 * Custom boat renderer for ChronoDawn mod boats.
 * Uses vanilla boat model with custom textures for each Time Wood variant.
 *
 * 1.21.9: render() replaced with submit(), model rendering uses submitModel().
 *
 * Task: T268-T270 [US1] Create Time Wood Boat renderers
 */
public class ChronoDawnBoatRenderer extends EntityRenderer<ChronoDawnBoat, ChronoDawnBoatRenderState> {

    private final Map<ChronoDawnBoatType, Pair<Identifier, BoatModel>> boatResources;
    private final Model waterPatchModel;

    public ChronoDawnBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.boatResources = createBoatResources(context);
        // Create water patch model from vanilla layer (masks water inside boat)
        this.waterPatchModel = new Model.Simple(
            context.bakeLayer(ModelLayers.BOAT_WATER_PATCH),
            resourceLocation -> RenderTypes.waterMask()
        );
    }

    @Override
    public ChronoDawnBoatRenderState createRenderState() {
        return new ChronoDawnBoatRenderState();
    }

    @Override
    public void extractRenderState(ChronoDawnBoat entity, ChronoDawnBoatRenderState state, float partialTick) {
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

    private static Map<ChronoDawnBoatType, Pair<Identifier, BoatModel>> createBoatResources(
            EntityRendererProvider.Context context) {

        ImmutableMap.Builder<ChronoDawnBoatType, Pair<Identifier, BoatModel>> builder = ImmutableMap.builder();

        for (ChronoDawnBoatType type : ChronoDawnBoatType.values()) {
            String texturePath = "textures/entity/boat/" + type.getName() + ".png";
            Identifier texture = CompatResourceLocation.create(ChronoDawn.MOD_ID, texturePath);

            // Use vanilla boat model layer (1.21.2: Boat.Type removed, using direct ModelLayerLocation)
            // Create custom ModelLayerLocation for our boat
            ModelLayerLocation modelLayer = new ModelLayerLocation(
                CompatResourceLocation.create(ChronoDawn.MOD_ID, "boat/" + type.getName()),
                "main"
            );
            ModelPart modelPart = context.bakeLayer(modelLayer);
            BoatModel model = new BoatModel(modelPart);

            builder.put(type, Pair.of(texture, model));
        }

        return builder.build();
    }

    @Override
    public void submit(ChronoDawnBoatRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));

        Pair<Identifier, BoatModel> pair = getModelWithLocation(state);
        Identifier texture = pair.getFirst();
        BoatModel model = pair.getSecond();

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        model.setupAnim(state);

        // 1.21.9: renderToBuffer replaced with submitModel(Model, S state, PoseStack, RenderType, int light, int overlay, int color, CrumblingOverlay)
        collector.submitModel(model, state, poseStack, model.renderType(texture), 0xF000F0, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);

        // Render water patch to mask water inside boat (only when not fully submerged)
        if (!state.isUnderWater) {
            collector.submitModel(this.waterPatchModel, state, poseStack, RenderTypes.waterMask(), 0xF000F0, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    private Pair<Identifier, BoatModel> getModelWithLocation(ChronoDawnBoatRenderState state) {
        return boatResources.get(state.boatType);
    }

    // Note: getTextureLocation() removed in 1.21.2
    // Texture is now obtained directly in submit() method via getModelWithLocation()
}
