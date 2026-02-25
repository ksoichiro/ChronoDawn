package com.chronodawn.client.model;

import com.chronodawn.client.renderer.mobs.ClockworkSentinelRenderState;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Custom model for Clockwork Sentinel.
 * Made with Blockbench 5.0.3
 * Exported for Minecraft version 1.17 or later with Mojang mappings
 */
public class ClockworkSentinelModel extends EntityModel<ClockworkSentinelRenderState> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "clockwork_sentinel"),
        "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public ClockworkSentinelModel(ModelPart root) {
        super(root);
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.headwear = this.head.getChild("headwear");
        this.left_arm = this.body.getChild("left_arm");
        this.right_arm = this.body.getChild("right_arm");
        this.left_leg = this.root.getChild("left_leg");
        this.right_leg = this.root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, -15.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headwear = head.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 0).addBox(0.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 2.0F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(ClockworkSentinelRenderState state) {
        super.setupAnim(state);

        // Head rotation
        this.head.yRot = state.yRot * ((float)Math.PI / 180F);
        this.head.xRot = state.xRot * ((float)Math.PI / 180F);

        // Leg animation (walking)
        this.right_leg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.left_leg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float)Math.PI) * 1.4F * state.walkAnimationSpeed;

        // Arm animation (walking)
        this.right_arm.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float)Math.PI) * 2.0F * state.walkAnimationSpeed * 0.5F;
        this.left_arm.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 2.0F * state.walkAnimationSpeed * 0.5F;
    }
}
