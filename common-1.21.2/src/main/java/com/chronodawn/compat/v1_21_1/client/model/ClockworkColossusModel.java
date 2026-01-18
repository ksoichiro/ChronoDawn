package com.chronodawn.compat.v1_21_1.client.model;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.ClockworkColossusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;

/**
 * Clockwork Colossus Model
 *
 * Custom model created with Blockbench.
 * Features a massive mechanical guardian with gears and clockwork details.
 *
 * Reference: research.md (Additional Bosses - Clockwork Colossus)
 * Task: T235h [Phase 1] Create model for Clockwork Colossus
 */
public class ClockworkColossusModel extends EntityModel<ClockworkColossusEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "clockwork_colossus"),
        "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart head;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public ClockworkColossusModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.left_arm = this.body.getChild("left_arm");
        this.right_arm = this.body.getChild("right_arm");
        this.head = this.body.getChild("head");
        this.left_leg = this.root.getChild("left_leg");
        this.right_leg = this.root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body4_r1 = body.addOrReplaceChild("body4_r1", CubeListBuilder.create().texOffs(48, 8).addBox(-6.0F, -44.0F, 9.0F, 12.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition body3_r1 = body.addOrReplaceChild("body3_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-6.0F, -16.0F, 1.0F, 12.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition body2_r1 = body.addOrReplaceChild("body2_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-4.0F, -21.0F, 3.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -26.0F, -1.0F, 14.0F, 9.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(8.0F, -29.0F, 0.0F));

        PartDefinition arm_left4_r1 = left_arm.addOrReplaceChild("arm_left4_r1", CubeListBuilder.create().texOffs(70, 67).addBox(1.0F, -2.0F, -1.0F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 20.0F, -1.0F, -0.48F, 0.0F, 0.0F));

        PartDefinition arm_left3_r1 = left_arm.addOrReplaceChild("arm_left3_r1", CubeListBuilder.create().texOffs(36, 68).addBox(2.0F, -2.0F, 0.0F, 3.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition arm_left2_r1 = left_arm.addOrReplaceChild("arm_left2_r1", CubeListBuilder.create().texOffs(58, 30).addBox(7.0F, -19.0F, 4.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(10, 68).addBox(8.0F, -39.0F, 7.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(0, 47).addBox(7.0F, -36.0F, 3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 29.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition arm_left_r1 = left_arm.addOrReplaceChild("arm_left_r1", CubeListBuilder.create().texOffs(0, 66).addBox(8.0F, -31.0F, 4.0F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 29.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-8.0F, -29.0F, 0.0F));

        PartDefinition arm_right4_r1 = right_arm.addOrReplaceChild("arm_right4_r1", CubeListBuilder.create().texOffs(70, 59).addBox(-6.0F, -2.0F, -1.0F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 20.0F, -1.0F, -0.48F, 0.0F, 0.0F));

        PartDefinition arm_right3_r1 = right_arm.addOrReplaceChild("arm_right3_r1", CubeListBuilder.create().texOffs(26, 68).addBox(-5.0F, -2.0F, 0.0F, 3.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition arm_right2_r1 = right_arm.addOrReplaceChild("arm_right2_r1", CubeListBuilder.create().texOffs(0, 59).addBox(-12.0F, -19.0F, 4.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(47, 72).addBox(-16.0F, -39.0F, 7.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(30, 43).addBox(-12.0F, -36.0F, 3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 29.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition arm_right_r1 = right_arm.addOrReplaceChild("arm_right_r1", CubeListBuilder.create().texOffs(64, 16).addBox(-11.0F, -31.0F, 4.0F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 29.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));

        PartDefinition head_horn_left_bottom_r1 = head.addOrReplaceChild("head_horn_left_bottom_r1", CubeListBuilder.create().texOffs(22, 47).addBox(1.0F, -15.0F, -7.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, -0.0436F, 0.1309F));

        PartDefinition head_horn_right_r1 = head.addOrReplaceChild("head_horn_right_r1", CubeListBuilder.create().texOffs(22, 47).addBox(-2.0F, -15.0F, -7.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0436F, -0.1309F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -19.0F, -10.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 51).addBox(1.0F, 3.0F, -4.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(36, 19).addBox(1.0F, 14.0F, -5.0F, 5.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition leg_left3_r1 = left_leg.addOrReplaceChild("leg_left3_r1", CubeListBuilder.create().texOffs(34, 55).addBox(3.0F, -4.0F, -2.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition leg_left_r1 = left_leg.addOrReplaceChild("leg_left_r1", CubeListBuilder.create().texOffs(58, 59).addBox(3.0F, -13.0F, -4.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 43).addBox(-6.0F, 3.0F, -4.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(30, 32).addBox(-6.0F, 14.0F, -5.0F, 5.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition leg_right3_r1 = right_leg.addOrReplaceChild("leg_right3_r1", CubeListBuilder.create().texOffs(22, 55).addBox(-4.0F, -4.0F, -2.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition leg_right_r1 = right_leg.addOrReplaceChild("leg_right_r1", CubeListBuilder.create().texOffs(46, 59).addBox(-5.0F, -13.0F, -4.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(ClockworkColossusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Head rotation
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Leg animation - walking
        this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.left_leg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

        // Arm animation - opposite to legs for natural walking motion
        this.right_arm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.left_arm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // Add slight arm swing for idle animation
        this.right_arm.zRot = 0.0F;
        this.left_arm.zRot = 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
