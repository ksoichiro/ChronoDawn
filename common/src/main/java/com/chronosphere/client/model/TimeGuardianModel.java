package com.chronosphere.client.model;

import com.chronosphere.entities.bosses.TimeGuardianEntity;
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

/**
 * Time Guardian Model
 *
 * Created with Blockbench 5.0.3
 * Converted from Yarn mappings to Mojang mappings for Architectury
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
public class TimeGuardianModel extends EntityModel<TimeGuardianEntity> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public TimeGuardianModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = this.root.getChild("left_leg");
        this.rightLeg = this.root.getChild("right_leg");
    }

    /**
     * Create the model layer definition.
     * This defines the geometry and texture mapping of the Time Guardian model.
     *
     * @return LayerDefinition with mesh and texture size
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Root part (pivot at Y=24, represents ground level)
        PartDefinition root = partdefinition.addOrReplaceChild("root",
            CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        // Body (main torso)
        PartDefinition body = root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -32.0F, -4.0F, 12.0F, 16.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(42, 43).addBox(-4.0F, -16.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Head (child of body) - pivot at neck (top of body)
        PartDefinition head = body.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(0, 23).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -32.0F, 0.0F));

        // Left arm (child of body) - pivot at left shoulder
        PartDefinition leftArm = body.addOrReplaceChild("left_arm",
            CubeListBuilder.create()
                .texOffs(0, 38).addBox(0.0F, -2.0F, -3.0F, 5.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(6.0F, -29.0F, 0.0F));

        // Right arm (child of body) - pivot at right shoulder
        PartDefinition rightArm = body.addOrReplaceChild("right_arm",
            CubeListBuilder.create()
                .texOffs(30, 23).addBox(-5.0F, -2.0F, -3.0F, 5.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-6.0F, -29.0F, 0.0F));

        // Left leg (child of root) - pivot at left hip
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
            CubeListBuilder.create()
                .texOffs(22, 43).addBox(1.0F, 3.0F, -3.0F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -16.0F, 0.0F));

        // Right leg (child of root) - pivot at right hip
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
            CubeListBuilder.create()
                .texOffs(38, 0).addBox(-6.0F, 3.0F, -3.0F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -16.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(TimeGuardianEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset all rotations to default
        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.leftArm.xRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.xRot = 0.0F;
        this.rightArm.zRot = 0.0F;
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
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
