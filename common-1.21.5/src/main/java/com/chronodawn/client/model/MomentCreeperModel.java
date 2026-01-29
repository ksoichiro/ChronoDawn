package com.chronodawn.client.model;

import com.chronodawn.client.renderer.mobs.MomentCreeperRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;

/**
 * Custom model for Moment Creeper.
 * Made with Blockbench 5.0.7
 * Exported for Minecraft version 1.17 or later with Mojang mappings
 */
public class MomentCreeperModel extends EntityModel<MomentCreeperRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "moment_creeper"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public MomentCreeperModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition leg1 = partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 18.0F, 4.0F));

        PartDefinition leg2 = partdefinition.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 18.0F, 4.0F));

        PartDefinition leg3 = partdefinition.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 18.0F, -4.0F));

        PartDefinition leg4 = partdefinition.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 18.0F, -4.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(MomentCreeperRenderState state) {
        super.setupAnim(state);

        // Head rotation
        this.head.yRot = state.yRot * ((float)Math.PI / 180F);
        this.head.xRot = state.xRot * ((float)Math.PI / 180F);

        // Leg animation (creeper walking animation)
        this.leg1.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.leg2.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float)Math.PI) * 1.4F * state.walkAnimationSpeed;
        this.leg3.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float)Math.PI) * 1.4F * state.walkAnimationSpeed;
        this.leg4.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;

        // Swelling animation
        float swelling = state.swelling;
        if (swelling > 0.0F) {
            // Scale all parts when swelling (1.0 + swelling * 0.05 at max = 1.05x, subtle 5% increase)
            float scale = 1.0F + swelling * 0.05F;
            this.head.xScale = scale;
            this.head.yScale = scale;
            this.head.zScale = scale;
            this.body.xScale = scale;
            this.body.yScale = scale;
            this.body.zScale = scale;
            this.leg1.xScale = scale;
            this.leg1.yScale = scale;
            this.leg1.zScale = scale;
            this.leg2.xScale = scale;
            this.leg2.yScale = scale;
            this.leg2.zScale = scale;
            this.leg3.xScale = scale;
            this.leg3.yScale = scale;
            this.leg3.zScale = scale;
            this.leg4.xScale = scale;
            this.leg4.yScale = scale;
            this.leg4.zScale = scale;
        } else {
            // Reset to normal scale
            this.head.xScale = 1.0F;
            this.head.yScale = 1.0F;
            this.head.zScale = 1.0F;
            this.body.xScale = 1.0F;
            this.body.yScale = 1.0F;
            this.body.zScale = 1.0F;
            this.leg1.xScale = 1.0F;
            this.leg1.yScale = 1.0F;
            this.leg1.zScale = 1.0F;
            this.leg2.xScale = 1.0F;
            this.leg2.yScale = 1.0F;
            this.leg2.zScale = 1.0F;
            this.leg3.xScale = 1.0F;
            this.leg3.yScale = 1.0F;
            this.leg3.zScale = 1.0F;
            this.leg4.xScale = 1.0F;
            this.leg4.yScale = 1.0F;
            this.leg4.zScale = 1.0F;
        }
    }

    // Note: renderToBuffer() is now final in 1.21.2 and automatically renders all child parts.
}
