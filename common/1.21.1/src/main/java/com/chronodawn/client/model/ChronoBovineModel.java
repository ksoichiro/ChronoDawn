/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.client.model;

import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.entities.mobs.ChronoBovineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Chrono Bovine Model - Based on Blockbench export with cow-style animations.
 * Adapted for 1.21.1.
 */
public class ChronoBovineModel extends EntityModel<ChronoBovineEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "chrono_bovine"),
        "main"
    );

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public ChronoBovineModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 28).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 16).addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 4.0F, -8.0F));

        PartDefinition rightHorn = head.addOrReplaceChild("mirrored", CubeListBuilder.create()
            .texOffs(44, 35).addBox(6.0F, -25.0F, -13.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F))
            .texOffs(40, 53).addBox(7.7F, -25.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offset(0.0F, 20.0F, 8.0F));

        rightHorn.addOrReplaceChild("mirrored_r1", CubeListBuilder.create()
            .texOffs(40, 49).addBox(1.3F, -2.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
            .texOffs(40, 44).addBox(0.0F, -2.0F, -13.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.3F)),
            PartPose.offsetAndRotation(5.0F, -26.0F, 0.0F, 0.0F, 0.0F, -0.829F));

        rightHorn.addOrReplaceChild("mirrored_r2", CubeListBuilder.create()
            .texOffs(52, 35).addBox(1.7F, -0.5F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.3F))
            .texOffs(32, 44).addBox(0.25F, -0.5F, -13.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offsetAndRotation(5.0F, -26.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

        rightHorn.addOrReplaceChild("mirrored_r3", CubeListBuilder.create()
            .texOffs(44, 27).addBox(-2.0F, 3.0F, -13.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(5.0F, -26.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

        PartDefinition leftHorn = head.addOrReplaceChild("mirrored2", CubeListBuilder.create()
            .texOffs(48, 40).addBox(-8.0F, -25.0F, -13.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F))
            .texOffs(54, 20).addBox(-9.7F, -25.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offset(0.0F, 20.0F, 8.0F));

        leftHorn.addOrReplaceChild("mirrored_r4", CubeListBuilder.create()
            .texOffs(48, 50).addBox(-3.3F, -2.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
            .texOffs(32, 49).addBox(-2.0F, -2.0F, -13.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.3F)),
            PartPose.offsetAndRotation(-5.0F, -26.0F, 0.0F, 0.0F, 0.0F, 0.829F));

        leftHorn.addOrReplaceChild("mirrored_r5", CubeListBuilder.create()
            .texOffs(32, 54).addBox(-3.7F, -0.5F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.3F))
            .texOffs(48, 45).addBox(-2.25F, -0.5F, -13.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offsetAndRotation(-5.0F, -26.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

        leftHorn.addOrReplaceChild("mirrored_r6", CubeListBuilder.create()
            .texOffs(44, 31).addBox(-2.0F, 3.0F, -13.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-5.0F, -26.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(44, 20).addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
            .texOffs(28, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
            .texOffs(0, 42).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
            .texOffs(44, 0).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 12.0F, -6.0F));

        partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
            .texOffs(16, 44).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 12.0F, -6.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ChronoBovineEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);

        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
