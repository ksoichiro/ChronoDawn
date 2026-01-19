package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.boats.ChronoDawnBoat;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
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
 * Custom boat renderer for ChronoDawn mod boats.
 * Uses vanilla boat model with custom textures for each Time Wood variant.
 *
 * Task: T268-T270 [US1] Create Time Wood Boat renderers
 */
public class ChronoDawnBoatRenderer extends EntityRenderer<ChronoDawnBoat, ChronoDawnBoatRenderState> {

    private final Map<ChronoDawnBoatType, Pair<ResourceLocation, BoatModel>> boatResources;

    public ChronoDawnBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.boatResources = createBoatResources(context);
    }

    @Override
    public ChronoDawnBoatRenderState createRenderState() {
        return new ChronoDawnBoatRenderState();
    }

    @Override
    public void extractRenderState(ChronoDawnBoat entity, ChronoDawnBoatRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Note: hurtTime, hurtDir, bubbleAngle, isUnderWater, yRot are set by super.extractRenderState()
        // in 1.21.2, as they are part of BoatRenderState
        state.boatType = entity.getChronoDawnBoatType();
    }

    private static Map<ChronoDawnBoatType, Pair<ResourceLocation, BoatModel>> createBoatResources(
            EntityRendererProvider.Context context) {

        ImmutableMap.Builder<ChronoDawnBoatType, Pair<ResourceLocation, BoatModel>> builder = ImmutableMap.builder();

        for (ChronoDawnBoatType type : ChronoDawnBoatType.values()) {
            String texturePath = "textures/entity/boat/" + type.getName() + ".png";
            ResourceLocation texture = CompatResourceLocation.create(ChronoDawn.MOD_ID, texturePath);

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
    public void render(ChronoDawnBoatRenderState state, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));

        // TODO: Apply hurt/damage animation if BoatRenderState provides these fields
        // In 1.21.2, these fields may be handled differently or removed from RenderState

        float bubbleAngle = state.bubbleAngle;
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(state.bubbleAngle));
        }

        Pair<ResourceLocation, BoatModel> pair = getModelWithLocation(state);
        ResourceLocation texture = pair.getFirst();
        BoatModel model = pair.getSecond();

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        model.setupAnim(state);

        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(texture));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        // TODO: Render water patch inside boat
        // In 1.21.2, waterPatch() method may have been removed or changed
        // Need to investigate vanilla BoatRenderer implementation
        // if (!state.isUnderWater) {
        //     VertexConsumer waterVertexConsumer = buffer.getBuffer(RenderType.waterMask());
        //     model.waterPatch().render(poseStack, waterVertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        // }

        poseStack.popPose();
        super.render(state, poseStack, buffer, packedLight);
    }

    private Pair<ResourceLocation, BoatModel> getModelWithLocation(ChronoDawnBoatRenderState state) {
        return boatResources.get(state.boatType);
    }

    @Override
    public ResourceLocation getTextureLocation(ChronoDawnBoatRenderState state) {
        return getModelWithLocation(state).getFirst();
    }
}
