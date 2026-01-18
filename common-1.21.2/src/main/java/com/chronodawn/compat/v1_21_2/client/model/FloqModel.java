package com.chronodawn.compat.v1_21_2.client.model;

import com.chronodawn.entities.mobs.FloqEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Floq Model
 * Made with Blockbench 5.0.4
 * Exported for Minecraft version 1.17 or later with Mojang mappings
 */
public class FloqModel extends EntityModel<FloqEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "floq"),
        "main"
    );

    private final ModelPart body;
    private final ModelPart right_eye;
    private final ModelPart left_eye;
    private final ModelPart mouth;

    public FloqModel(ModelPart root) {
        this.body = root.getChild("body");
        this.right_eye = root.getChild("right_eye");
        this.left_eye = root.getChild("left_eye");
        this.mouth = root.getChild("mouth");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition right_eye = partdefinition.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(32, 4).addBox(1.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition left_eye = partdefinition.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(32, 0).addBox(-3.3F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition mouth = partdefinition.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(FloqEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Animation is handled by FloqRenderer.scale() method
        // No per-frame setup needed here
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        // Scale transformation is applied in FloqRenderer.scale() method
        // Just render the parts directly here
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        right_eye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        left_eye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
