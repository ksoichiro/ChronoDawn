package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.TimelineStriderEntity;
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
 * Timeline Strider Model - Enderman-like humanoid with a floating disc.
 * Based on Blockbench export (128x64 texture).
 */
public class TimelineStriderModel extends EntityModel<TimelineStriderEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "timeline_strider"),
        "main"
    );

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public TimelineStriderModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.headwear = root.getChild("headwear");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Body (torso)
        PartDefinition body = partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(32, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -15.0F, 0.0F));

        // Disc (child of body)
        body.addOrReplaceChild("disc",
            CubeListBuilder.create().texOffs(0, 32)
                .addBox(-15.0F, 0.0F, -15.0F, 30.0F, 1.0F, 30.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0436F, 0.0F, 0.0F));

        // Head
        partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -15.0F, 0.0F));

        // Headwear (hat layer)
        partdefinition.addOrReplaceChild("headwear",
            CubeListBuilder.create().texOffs(0, 16)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, -15.0F, 0.0F));

        // Right Arm
        partdefinition.addOrReplaceChild("right_arm",
            CubeListBuilder.create().texOffs(56, 0)
                .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(5.0F, -13.0F, 0.0F));

        // Left Arm (mirrored)
        partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(56, 0).mirror()
                .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-5.0F, -13.0F, 0.0F));

        // Right Leg
        partdefinition.addOrReplaceChild("right_leg",
            CubeListBuilder.create().texOffs(56, 0)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, -6.0F, 0.0F));

        // Left Leg (mirrored)
        partdefinition.addOrReplaceChild("left_leg",
            CubeListBuilder.create().texOffs(56, 0).mirror()
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, -6.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(TimelineStriderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Arm swing
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;

        // Leg swing
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // Head look direction
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.headwear.yRot = this.head.yRot;
        this.headwear.xRot = this.head.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
