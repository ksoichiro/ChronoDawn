package com.chronodawn.compat.v1_21_2.client.model;

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
 * Time Guardian Model
 *
 * Created with Blockbench 5.0.3
 * Converted from Yarn mappings to Mojang mappings for Architectury
 *
 * Generic model that can be used for multiple boss entities (Time Guardian, Chronos Warden, Clockwork Colossus, etc.)
 *
 * Model Structure:
 * - root
 *   - body (parent for upper body parts)
 *     - head
 *     - left_arm
 *     - right_arm
 *   - left_leg
 *   - right_leg
 */
public class TimeGuardianModel<T extends Mob> extends EntityModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;   // Forearm for elbow bending animation
    private final ModelPart rightForearm;  // Forearm for elbow bending animation
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public TimeGuardianModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");

        // Get forearm parts for independent animation (elbow bending)
        this.leftForearm = this.leftArm.getChild("arm_left2_r1");
        this.rightForearm = this.rightArm.getChild("arm_right2_r1");

        this.leftLeg = this.root.getChild("left_leg");
        this.rightLeg = this.root.getChild("right_leg");
    }

    /**
     * Create the model layer definition.
     * This defines the geometry and texture mapping of the Time Guardian model.
     *
     * Updated with Blockbench model - includes articulated limbs for better animation.
     *
     * @return LayerDefinition with mesh and texture size
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(72, 50).addBox(-4.0F, -18.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -24.0F, -5.0F, 16.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(30, 40).addBox(0.0F, -5.0F, -4.0F, 7.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(50, 54).addBox(0.0F, -4.0F, -3.0F, 5.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -29.0F, 0.0F));

        PartDefinition arm_left3_r1 = left_arm.addOrReplaceChild("arm_left3_r1", CubeListBuilder.create().texOffs(52, 0).addBox(2.0F, -4.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 20.0F, -1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition arm_left2_r1 = left_arm.addOrReplaceChild("arm_left2_r1", CubeListBuilder.create().texOffs(60, 14).addBox(2.0F, -3.0F, -3.0F, 5.0F, 12.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(30, 26).addBox(-7.0F, -5.0F, -4.0F, 7.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(28, 54).addBox(-5.0F, -4.0F, -3.0F, 5.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -29.0F, 0.0F));

        PartDefinition arm_right3_r1 = right_arm.addOrReplaceChild("arm_right3_r1", CubeListBuilder.create().texOffs(0, 41).addBox(-8.0F, -4.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 20.0F, -1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition arm_right2_r1 = right_arm.addOrReplaceChild("arm_right2_r1", CubeListBuilder.create().texOffs(0, 55).addBox(-7.0F, -3.0F, -3.0F, 5.0F, 12.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 26).addBox(-4.0F, -19.0F, -5.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 41).addBox(1.0F, 14.0F, -5.0F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition leg_left_r1 = left_leg.addOrReplaceChild("leg_left_r1", CubeListBuilder.create().texOffs(72, 72).addBox(2.0F, -1.0F, -4.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition leg_left_r2 = left_leg.addOrReplaceChild("leg_left_r2", CubeListBuilder.create().texOffs(20, 74).addBox(2.0F, -6.0F, -4.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(60, 32).addBox(-6.0F, 14.0F, -5.0F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition leg_right_r1 = right_leg.addOrReplaceChild("leg_right_r1", CubeListBuilder.create().texOffs(72, 60).addBox(-5.0F, -1.0F, -4.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition leg_right_r2 = right_leg.addOrReplaceChild("leg_right_r2", CubeListBuilder.create().texOffs(0, 73).addBox(-5.0F, -6.0F, -4.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset all rotations to default
        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.leftArm.xRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.xRot = 0.0F;
        this.rightArm.zRot = 0.0F;
        this.leftForearm.xRot = 0.0F;   // Reset forearm rotation
        this.rightForearm.xRot = 0.0F;  // Reset forearm rotation
        this.leftLeg.xRot = 0.0F;
        this.rightLeg.xRot = 0.0F;

        // Head rotation (look at target)
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Walking animation - arms swing
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.8F * limbSwingAmount;

        // Walking animation - legs swing (opposite to arms for natural gait)
        // Right arm forward = Left leg forward
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // Idle animation - slight arm sway (only when not walking and not attacking)
        if (limbSwingAmount < 0.01F && entity.attackAnim <= 0.0F) {
            float idleSwing = ageInTicks * 0.05F;
            this.rightArm.xRot += Mth.cos(idleSwing) * 0.05F;
            this.leftArm.xRot += Mth.cos(idleSwing + (float)Math.PI) * 0.05F;
        }

        // Attack animation - raise arms (like Iron Golem)
        if (entity.attackAnim > 0.0F) {
            // attackAnim goes from 1.0 (start of attack) to 0.0 (end of attack)
            float attackProgress = entity.attackAnim;

            // Both arms raise up during attack
            // Use sine wave for smooth up-down motion
            float armRaise = Mth.sin(attackProgress * (float)Math.PI);

            // Raise both arms forward and up
            this.rightArm.xRot = -2.0F * armRaise; // Negative = forward/up
            this.leftArm.xRot = -2.0F * armRaise;

            // Slightly spread arms outward during attack
            this.rightArm.zRot = 0.3F * armRaise;
            this.leftArm.zRot = -0.3F * armRaise;

            // Bend elbows during attack for more dynamic motion
            // Negative value bends forearm inward (toward body)
            this.leftForearm.xRot = -0.5F * armRaise;
            this.rightForearm.xRot = -0.5F * armRaise;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
