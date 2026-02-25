package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.SecondhandArcherEntity;
import com.chronodawn.compat.CompatResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Secondhand Archer Model - Skeleton-like humanoid with shoulder pads.
 * Based on Blockbench export (64x64 texture).
 */
public class SecondhandArcherModel extends EntityModel<SecondhandArcherEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "secondhand_archer"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public SecondhandArcherModel(ModelPart root) {
        this.head = root.getChild("head");
        this.headwear = root.getChild("headwear");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Head
        partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Headwear (hat layer)
        partdefinition.addOrReplaceChild("headwear",
            CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Body
        partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Left Arm with shoulder pad
        PartDefinition leftArm = partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offset(5.0F, 2.0F, 0.0F));

        // Left shoulder pad decoration
        leftArm.addOrReplaceChild("left_shoulder_pad",
            CubeListBuilder.create()
                .texOffs(6, 33).addBox(11.5F, -20.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 33).addBox(11.5F, -23.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 37).mirror().addBox(11.5F, -22.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(4, 42).mirror().addBox(11.5F, -22.0F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 42).mirror().addBox(11.5F, -22.0F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offsetAndRotation(-5.0F, 22.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        // Right Arm with shoulder pad
        PartDefinition rightArm = partdefinition.addOrReplaceChild("right_arm",
            CubeListBuilder.create().texOffs(40, 16)
                .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-5.0F, 2.0F, 0.0F));

        // Right shoulder pad decoration
        rightArm.addOrReplaceChild("right_shoulder_pad",
            CubeListBuilder.create()
                .texOffs(6, 33).addBox(-12.5F, -20.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 33).addBox(-12.5F, -23.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 37).mirror().addBox(-12.5F, -22.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(4, 42).mirror().addBox(-12.5F, -22.0F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 42).mirror().addBox(-12.5F, -22.0F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offsetAndRotation(5.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        // Left Leg
        partdefinition.addOrReplaceChild("left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-1.0F, 0.0F, -1.1F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offset(2.0F, 12.0F, 0.1F));

        // Right Leg
        partdefinition.addOrReplaceChild("right_leg",
            CubeListBuilder.create().texOffs(0, 16)
                .addBox(-1.0F, 0.0F, -1.1F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 12.0F, 0.1F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(SecondhandArcherEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Head look direction
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.headwear.yRot = this.head.yRot;
        this.headwear.xRot = this.head.xRot;

        // Arm swing (walking)
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;

        // Leg swing
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;

        // Bow holding pose when aggressive
        if (entity.isAggressive()) {
            this.rightArm.xRot = -((float) Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = -0.8F + this.head.xRot;
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.leftArm.yRot = 0.5F + this.head.yRot;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        ModelPart modelPart = arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        modelPart.translateAndRotate(poseStack);
    }
}
