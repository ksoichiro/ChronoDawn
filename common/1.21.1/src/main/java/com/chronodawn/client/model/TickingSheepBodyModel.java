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
import com.chronodawn.entities.mobs.TickingSheepEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Ticking Sheep body model - matches vanilla sheep body proportions.
 * Uses vanilla sheep texture (textures/entity/sheep/sheep.png).
 */
public class TickingSheepBodyModel extends EntityModel<TickingSheepEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "ticking_sheep"),
        "main"
    );

    private float headXRot;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public TickingSheepBodyModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Head - vanilla sheep proportions
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 6.0F, -8.0F));

        // Body - rotated 90 degrees on X axis
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));

        // Legs - vanilla sheep proportions (4x12x4)
        partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.0F, 12.0F, -5.0F));

        partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.0F, 12.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void prepareMobModel(TickingSheepEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        this.head.y = 6.0F + entity.getHeadEatPositionScale(partialTick) * 9.0F;
        this.headXRot = entity.getHeadEatAngleScale(partialTick);
    }

    @Override
    public void setupAnim(TickingSheepEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = this.headXRot;
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
