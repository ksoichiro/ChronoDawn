package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.ParadoxCrawlerEntity;
import com.chronodawn.compat.CompatResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Paradox Crawler Model - Spider-like creature with 8 legs.
 * Based on Blockbench export (64x32 texture).
 */
public class ParadoxCrawlerModel extends EntityModel<ParadoxCrawlerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "paradox_crawler"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart body;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;

    public ParadoxCrawlerModel(ModelPart root) {
        this.head = root.getChild("head");
        this.neck = root.getChild("neck");
        this.body = root.getChild("body");
        this.rightFrontLeg = root.getChild("leg1");
        this.leftFrontLeg = root.getChild("leg2");
        this.rightMiddleFrontLeg = root.getChild("leg3");
        this.leftMiddleFrontLeg = root.getChild("leg4");
        this.rightMiddleHindLeg = root.getChild("leg5");
        this.leftMiddleHindLeg = root.getChild("leg6");
        this.rightHindLeg = root.getChild("leg7");
        this.leftHindLeg = root.getChild("leg8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(32, 4)
                .addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 15.0F, -3.0F));

        partdefinition.addOrReplaceChild("neck",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 12)
                .addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 15.0F, 9.0F));

        partdefinition.addOrReplaceChild("leg1",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 15.0F, 4.0F));

        partdefinition.addOrReplaceChild("leg2",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 15.0F, 4.0F));

        partdefinition.addOrReplaceChild("leg3",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 15.0F, 1.0F));

        partdefinition.addOrReplaceChild("leg4",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 15.0F, 1.0F));

        partdefinition.addOrReplaceChild("leg5",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 15.0F, -2.0F));

        partdefinition.addOrReplaceChild("leg6",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 15.0F, -2.0F));

        partdefinition.addOrReplaceChild("leg7",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 15.0F, -5.0F));

        partdefinition.addOrReplaceChild("leg8",
            CubeListBuilder.create().texOffs(18, 0)
                .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 15.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(ParadoxCrawlerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSpread = 0.7853982F;

        this.rightFrontLeg.zRot = -legSpread;
        this.leftFrontLeg.zRot = legSpread;
        this.rightMiddleFrontLeg.zRot = -legSpread * 0.74F;
        this.leftMiddleFrontLeg.zRot = legSpread * 0.74F;
        this.rightMiddleHindLeg.zRot = -legSpread * 0.74F;
        this.leftMiddleHindLeg.zRot = legSpread * 0.74F;
        this.rightHindLeg.zRot = -legSpread;
        this.leftHindLeg.zRot = legSpread;

        float legSwing1 = -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount;
        float legSwing2 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        float legSwing3 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float legSwing4 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;

        this.rightFrontLeg.yRot = legSwing1;
        this.leftFrontLeg.yRot = -legSwing1;
        this.rightMiddleFrontLeg.yRot = legSwing2;
        this.leftMiddleFrontLeg.yRot = -legSwing2;
        this.rightMiddleHindLeg.yRot = legSwing3;
        this.leftMiddleHindLeg.yRot = -legSwing3;
        this.rightHindLeg.yRot = legSwing4;
        this.leftHindLeg.yRot = -legSwing4;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightMiddleFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftMiddleFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightMiddleHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftMiddleHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
