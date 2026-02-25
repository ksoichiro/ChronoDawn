package com.chronodawn.client.model;

import com.chronodawn.client.renderer.mobs.HourglassGolemRenderState;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Hourglass Golem Model - Enderman-like humanoid with a floating disc.
 * Based on Blockbench export (128x128 texture).
 */
public class HourglassGolemModel extends EntityModel<HourglassGolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "hourglass_golem"),
        "main"
    );

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public HourglassGolemModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Body (torso) - pivot at neck level
        PartDefinition body = partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 28)
                .addBox(-8.0F, -2.0F, -6.0F, 16.0F, 27.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(54, 36).addBox(-8.0F, -2.0F, -6.0F, 16.0F, 27.0F, 11.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, -7.0F, 0.0F));

        // Head
        partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -11.0F, -3.5F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-4.0F, -11.0F, -3.5F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.3F))
                .texOffs(24, 0).addBox(-1.0F, -5.0F, -5.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -7.0F, -2.0F));

        // Right Arm
        partdefinition.addOrReplaceChild("right_arm",
            CubeListBuilder.create().texOffs(59, 0)
                .addBox(-12.5F, -1.5F, -3.0F, 4.0F, 29.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 81).mirror().addBox(-13.5F, -2.5F, -4.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 95).addBox(-12.5F, 17.5F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.5F))
                .texOffs(0, 102).addBox(-14.0F, 20.0F, -4.0F, 6.0F, 1.5F, 8.0F, new CubeDeformation(0.5F))
                .texOffs(26, 84).addBox(-15.0F, 20.0F, -5.0F, 7.5F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 111).mirror().addBox(-13.0F, 22.0F, -3.0F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)).mirror(false),
            PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, 0.0436F));

        // Left Arm (mirrored)
        partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(59, 0).mirror().addBox(8.5F, -1.5F, -3.0F, 4.0F, 29.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 81).addBox(8.5F, -2.5F, -4.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 95).addBox(8.5F, 17.5F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.5F))
                .texOffs(0, 102).addBox(8.0F, 20.0F, -4.0F, 6.0F, 1.5F, 8.0F, new CubeDeformation(0.5F))
                .texOffs(26, 84).addBox(7.5F, 20.0F, -5.0F, 7.5F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 111).addBox(9.0F, 22.0F, -3.0F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)),
            PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -0.0436F));

        // Right Leg
        partdefinition.addOrReplaceChild("right_leg",
            CubeListBuilder.create().texOffs(37, 0)
                .addBox(-3.5F, 3.0F, -3.0F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 11.0F, 0.0F));

        // Left Leg (mirrored)
        partdefinition.addOrReplaceChild("left_leg",
            CubeListBuilder.create().texOffs(37, 0)
                .addBox(-3.5F, 3.0F, -3.0F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(5.0F, 11.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(HourglassGolemRenderState state) {
        super.setupAnim(state);
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;

        // Arm swing
        this.rightArm.xRot = Mth.cos(walkPos * 0.6662F) * 2.0F * walkSpeed * 0.5F;
        this.leftArm.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 2.0F * walkSpeed * 0.5F;

        // Leg swing
        this.rightLeg.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.leftLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;

        // Head look direction
        this.head.yRot = state.yRot * ((float) Math.PI / 180F);
        this.head.xRot = state.xRot * ((float) Math.PI / 180F);
    }
}
