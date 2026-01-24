package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.EpochHuskEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Custom model for Epoch Husk.
 * Made with Blockbench 5.0.7
 * Exported for Minecraft version 1.17 or later with Mojang mappings
 */
public class EpochHuskModel extends EntityModel<EpochHuskEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "epoch_husk"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart body;
    private final ModelPart left_arm;
    private final ModelPart left_arm2;
    private final ModelPart right_arm2;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public EpochHuskModel(ModelPart root) {
        this.head = root.getChild("head");
        this.headwear = root.getChild("headwear");
        this.body = root.getChild("body");
        this.left_arm = root.getChild("left_arm");
        this.left_arm2 = root.getChild("left_arm2");
        this.right_arm2 = root.getChild("right_arm2");
        this.right_arm = root.getChild("right_arm");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headwear = partdefinition.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(0, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition left_arm_r1 = left_arm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(6.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, 22.0F, 0.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition left_arm2 = partdefinition.addOrReplaceChild("left_arm2", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition left_arm_r2 = left_arm2.addOrReplaceChild("left_arm_r2", CubeListBuilder.create().texOffs(40, 32).mirror().addBox(6.0F, -24.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-5.0F, 22.0F, 0.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition right_arm2 = partdefinition.addOrReplaceChild("right_arm2", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition right_arm2_r1 = right_arm2.addOrReplaceChild("right_arm2_r1", CubeListBuilder.create().texOffs(24, 32).mirror().addBox(-10.0F, -24.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-5.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(40, 16).addBox(-10.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(0, 48).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(16, 48).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(EpochHuskEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Head rotation
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        this.headwear.yRot = this.head.yRot;
        this.headwear.xRot = this.head.xRot;

        // Leg animation (walking)
        this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.left_leg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

        // Arm animation (walking) - all arm parts follow the same rotation
        float rightArmRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        float leftArmRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;

        this.right_arm.xRot = rightArmRot;
        this.right_arm2.xRot = rightArmRot;
        this.left_arm.xRot = leftArmRot;
        this.left_arm2.xRot = leftArmRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        left_arm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        right_arm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
