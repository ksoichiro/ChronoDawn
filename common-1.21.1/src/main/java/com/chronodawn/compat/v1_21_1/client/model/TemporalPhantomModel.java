package com.chronodawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Temporal Phantom Model
 *
 * Created with Blockbench 5.0.4
 * Exported for Minecraft version 1.17 or later with Mojang mappings
 *
 * Custom model for Temporal Phantom boss entity.
 *
 * Task: T236s [US3] Create custom texture for Temporal Phantom
 */
public class TemporalPhantomModel<T extends Mob> extends EntityModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart head;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public TemporalPhantomModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.head = this.body.getChild("head");
        this.leftLeg = this.root.getChild("left_leg");
        this.rightLeg = this.root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(42, 62).addBox(-4.0F, -23.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -24.0F, -3.0F, 10.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(30, 19).addBox(-3.0F, -4.0F, -4.0F, 7.0F, 6.0F, 8.0F, new CubeDeformation(0.1F))
        .texOffs(48, 33).addBox(0.0F, -2.0F, -3.0F, 4.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -29.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition arm_left3_r1 = left_arm.addOrReplaceChild("arm_left3_r1", CubeListBuilder.create().texOffs(20, 52).addBox(2.0F, -4.0F, -4.0F, 4.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 20.0F, -2.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition arm_left2_r1 = left_arm.addOrReplaceChild("arm_left2_r1", CubeListBuilder.create().texOffs(36, 0).addBox(2.0F, -4.0F, -3.0F, 4.0F, 13.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0F, 12.0F, -1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, -4.0F, -4.0F, 7.0F, 6.0F, 8.0F, new CubeDeformation(0.1F))
        .texOffs(0, 47).addBox(-4.0F, -2.0F, -3.0F, 4.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -29.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

        PartDefinition arm_right3_r1 = right_arm.addOrReplaceChild("arm_right3_r1", CubeListBuilder.create().texOffs(48, 50).addBox(-6.0F, -4.0F, -4.0F, 4.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 20.0F, -2.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition arm_right2_r1 = right_arm.addOrReplaceChild("arm_right2_r1", CubeListBuilder.create().texOffs(28, 33).addBox(-6.0F, -4.0F, -3.0F, 4.0F, 13.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, 12.0F, -1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 33).addBox(-4.0F, -19.0F, -5.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.0F, 2.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition leg_left_r1 = left_leg.addOrReplaceChild("leg_left_r1", CubeListBuilder.create().texOffs(66, 62).addBox(2.0F, -6.0F, 1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

        PartDefinition leg_left_r2 = left_leg.addOrReplaceChild("leg_left_r2", CubeListBuilder.create().texOffs(16, 64).addBox(2.0F, -4.0F, -3.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition leg_left_r3 = left_leg.addOrReplaceChild("leg_left_r3", CubeListBuilder.create().texOffs(60, 14).addBox(2.0F, -12.0F, -4.0F, 4.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition leg_right_r1 = right_leg.addOrReplaceChild("leg_right_r1", CubeListBuilder.create().texOffs(32, 64).addBox(-4.0F, -6.0F, 1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

        PartDefinition leg_right_r2 = right_leg.addOrReplaceChild("leg_right_r2", CubeListBuilder.create().texOffs(0, 64).addBox(-4.0F, -4.0F, -3.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition leg_right_r3 = right_leg.addOrReplaceChild("leg_right_r3", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, -12.0F, -4.0F, 4.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset rotations
        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.leftArm.xRot = 0.0F;
        this.leftArm.zRot = -0.1309F; // Default pose
        this.rightArm.xRot = 0.0F;
        this.rightArm.zRot = 0.1309F; // Default pose
        this.leftLeg.xRot = 0.0F;
        this.rightLeg.xRot = 0.0F;

        // Head rotation (look at target)
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Walking animation - arms swing
        this.rightArm.xRot += Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.leftArm.xRot += Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.8F * limbSwingAmount;

        // Walking animation - legs swing
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // Idle animation - slight floating motion for ghostly effect
        if (limbSwingAmount < 0.01F && entity.attackAnim <= 0.0F) {
            float idleFloat = ageInTicks * 0.08F;
            this.root.y = 20.0F + Mth.sin(idleFloat) * 1.0F; // Floating up and down

            // Slight arm sway
            this.rightArm.xRot += Mth.cos(idleFloat) * 0.05F;
            this.leftArm.xRot += Mth.cos(idleFloat + (float)Math.PI) * 0.05F;
        }

        // Attack animation
        if (entity.attackAnim > 0.0F) {
            float attackProgress = entity.attackAnim;
            float armRaise = Mth.sin(attackProgress * (float)Math.PI);

            // Raise both arms for attack
            this.rightArm.xRot = -2.0F * armRaise;
            this.leftArm.xRot = -2.0F * armRaise;

            // Spread arms outward
            this.rightArm.zRot = 0.3F * armRaise + 0.1309F;
            this.leftArm.zRot = -0.3F * armRaise - 0.1309F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
