package com.chronodawn.client.model;

import com.chronodawn.client.renderer.mobs.GlideFishRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;

/**
 * GlideFish Model
 * Based on Blockbench export, adapted for 1.21.2 RenderState pattern.
 */
public class GlideFishModel extends EntityModel<GlideFishRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "glide_fish"),
        "main"
    );

    private final ModelPart body;
    private final ModelPart rightFin;
    private final ModelPart leftFin;
    private final ModelPart tail;

    public GlideFishModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.rightFin = this.body.getChild("right_fin");
        this.leftFin = this.body.getChild("left_fin");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 0.0F));

        body.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(22, 2).addBox(-4.0F, 0.0F, -1.0F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 1.0F, 0.0F));

        body.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(22, 5).addBox(0.0F, 0.0F, -1.0F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 1.0F, 0.0F));

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 0.0F));

        partdefinition.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 19).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, -3.0F));

        partdefinition.addOrReplaceChild("fin_left", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("fin_right", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("fin_back", CubeListBuilder.create().texOffs(20, -6).addBox(0.0F, -2.0F, 0.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, 4).addBox(0.0F, -2.0F, 0.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 7.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(GlideFishRenderState state) {
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;
        // Tail swish animation
        this.tail.yRot = -0.25F * Mth.sin(0.3F * ageInTicks);
        // Fin flap animation
        this.rightFin.zRot = -0.2F * Mth.sin(0.4F * ageInTicks);
        this.leftFin.zRot = 0.2F * Mth.sin(0.4F * ageInTicks);
    }
}
