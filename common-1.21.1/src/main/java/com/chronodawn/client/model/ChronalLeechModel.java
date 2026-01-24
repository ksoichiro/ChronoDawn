package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.ChronalLeechEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Custom model for Chronal Leech.
 * Made with Blockbench 5.0.7
 * Exported for Minecraft version 1.17 or later with Mojang mappings
 */
public class ChronalLeechModel extends EntityModel<ChronalLeechEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "chronal_leech"),
        "main"
    );

    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart body3;
    private final ModelPart body4;
    private final ModelPart body5;
    private final ModelPart body6;
    private final ModelPart body7;
    private final ModelPart wing1;
    private final ModelPart wing4;
    private final ModelPart wing5;

    public ChronalLeechModel(ModelPart root) {
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.body3 = root.getChild("body3");
        this.body4 = root.getChild("body4");
        this.body5 = root.getChild("body5");
        this.body6 = root.getChild("body6");
        this.body7 = root.getChild("body7");
        this.wing1 = root.getChild("wing1");
        this.wing4 = root.getChild("wing4");
        this.wing5 = root.getChild("wing5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body1 = partdefinition.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, -7.0F));

        PartDefinition body2 = partdefinition.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 4).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, -5.0F));

        PartDefinition body2_r1 = body2.addOrReplaceChild("body2_r1", CubeListBuilder.create().texOffs(19, 2).addBox(-5.0F, -1.0F, -5.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 5.0F, 0.0F, -0.0873F, 0.1745F));

        PartDefinition body2_r2 = body2.addOrReplaceChild("body2_r2", CubeListBuilder.create().texOffs(19, 0).addBox(2.0F, -1.0F, -5.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 5.0F, 0.0F, 0.0873F, -0.1745F));

        PartDefinition body3 = partdefinition.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, 0.0F, -1.5F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, -2.5F));

        PartDefinition body5_r1 = body3.addOrReplaceChild("body5_r1", CubeListBuilder.create().texOffs(19, 2).addBox(-6.0F, -1.0F, -3.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 2.5F, 0.0F, 0.0F, 0.1745F));

        PartDefinition body4_r1 = body3.addOrReplaceChild("body4_r1", CubeListBuilder.create().texOffs(19, 0).addBox(3.0F, -1.0F, -3.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 2.5F, 0.0F, 0.0F, -0.1745F));

        PartDefinition body4 = partdefinition.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, 0.5F));

        PartDefinition body6_r1 = body4.addOrReplaceChild("body6_r1", CubeListBuilder.create().texOffs(19, 2).addBox(-4.5F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -0.5F, 0.0F, 0.0F, 0.1745F));

        PartDefinition body5_r2 = body4.addOrReplaceChild("body5_r2", CubeListBuilder.create().texOffs(19, 0).addBox(1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -0.5F, 0.0F, 0.0F, -0.1745F));

        PartDefinition body5 = partdefinition.addOrReplaceChild("body5", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 3.5F));

        PartDefinition body7_r1 = body5.addOrReplaceChild("body7_r1", CubeListBuilder.create().texOffs(19, 2).addBox(-4.0F, -1.0F, 3.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -3.5F, 0.0F, 0.1309F, 0.1745F));

        PartDefinition body6_r2 = body5.addOrReplaceChild("body6_r2", CubeListBuilder.create().texOffs(19, 0).addBox(1.0F, -1.0F, 3.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -3.5F, 0.0F, -0.1309F, -0.1745F));

        PartDefinition body6 = partdefinition.addOrReplaceChild("body6", CubeListBuilder.create().texOffs(11, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 6.0F));

        PartDefinition body7 = partdefinition.addOrReplaceChild("body7", CubeListBuilder.create().texOffs(13, 4).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 8.0F));

        PartDefinition wing1 = partdefinition.addOrReplaceChild("wing1", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, -2.5F));

        PartDefinition wing3_r1 = wing1.addOrReplaceChild("wing3_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-7.0F, -3.0F, -2.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.5F, 0.0F, 0.1309F, 0.5236F));

        PartDefinition wing2_r1 = wing1.addOrReplaceChild("wing2_r1", CubeListBuilder.create().texOffs(26, 0).addBox(3.0F, -3.0F, -3.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.5F, 0.0F, 0.1309F, -0.5236F));

        PartDefinition wing4 = partdefinition.addOrReplaceChild("wing4", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, -2.5F));

        PartDefinition wing4_r1 = wing4.addOrReplaceChild("wing4_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-6.0F, -2.0F, 1.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.5F, 0.0F, 0.1309F, 0.7418F));

        PartDefinition wing3_r2 = wing4.addOrReplaceChild("wing3_r2", CubeListBuilder.create().texOffs(26, 0).addBox(2.0F, -2.0F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.5F, 0.0F, -0.1309F, -0.6981F));

        PartDefinition wing5 = partdefinition.addOrReplaceChild("wing5", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, -2.5F));

        PartDefinition wing5_r1 = wing5.addOrReplaceChild("wing5_r1", CubeListBuilder.create().texOffs(27, 1).addBox(-4.0F, -2.0F, 4.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.5F, 0.0F, 0.0F, 0.7418F));

        PartDefinition wing4_r2 = wing5.addOrReplaceChild("wing4_r2", CubeListBuilder.create().texOffs(27, 0).addBox(1.0F, -2.0F, 3.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.5F, 0.0F, 0.0F, -0.7418F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(ChronalLeechEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Body segments wiggle animation
        float wiggle = Mth.cos(ageInTicks * 0.6F) * 0.1F;
        this.body2.yRot = wiggle;
        this.body3.yRot = wiggle * 1.2F;
        this.body4.yRot = wiggle * 1.4F;
        this.body5.yRot = wiggle * 1.6F;
        this.body6.yRot = wiggle * 1.8F;
        this.body7.yRot = wiggle * 2.0F;

        // Wings flapping
        this.wing1.zRot = Mth.cos(ageInTicks * 1.3F) * 0.3F;
        this.wing4.zRot = Mth.cos(ageInTicks * 1.3F + 1.0F) * 0.3F;
        this.wing5.zRot = Mth.cos(ageInTicks * 1.3F + 2.0F) * 0.3F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wing4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wing5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
