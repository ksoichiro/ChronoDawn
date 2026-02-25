package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.ForgottenMinuteEntity;
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
 * Custom model for Forgotten Minute.
 * Made with Blockbench 5.0.7
 * Exported for Minecraft version 1.17 or later with Mojang mappings
 */
public class ForgottenMinuteModel extends EntityModel<ForgottenMinuteEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "forgotten_minute"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart left_wing;
    private final ModelPart right_wing;

    public ForgottenMinuteModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.right_arm = root.getChild("right_arm");
        this.left_arm = root.getChild("left_arm");
        this.left_wing = root.getChild("left_wing");
        this.right_wing = root.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(10, 10).addBox(1.5F, -7.0F, -0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(10, 15).addBox(-5.5F, -7.0F, -0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(0, 16).addBox(-1.5F, 1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-2.0F, 18.5F, 0.0F));

        PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(23, 0).addBox(-4.5F, -6.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 5.5F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(2.0F, 18.5F, 0.0F));

        PartDefinition left_arm_r1 = left_arm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(23, 7).addBox(2.5F, -6.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 5.5F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition left_wing = partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(-1.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 19.0F, 1.0F));

        PartDefinition right_wing = partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(1.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 19.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(ForgottenMinuteEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Head rotation
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Arm animation (subtle movement)
        this.right_arm.zRot = Mth.cos(ageInTicks * 0.1F) * 0.15F;
        this.left_arm.zRot = -Mth.cos(ageInTicks * 0.1F) * 0.15F;

        // Wing animation (flapping)
        this.left_wing.yRot = Mth.cos(ageInTicks * 0.5F) * 0.4F;
        this.right_wing.yRot = -Mth.cos(ageInTicks * 0.5F) * 0.4F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_wing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_wing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
