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
import com.chronodawn.entities.mobs.TimeboundRabbitEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Timebound Rabbit Model - Based on Blockbench export.
 * Adapted for 1.21.1 with rabbit-style animations.
 */
public class TimeboundRabbitModel extends EntityModel<TimeboundRabbitEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "timebound_rabbit"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftThigh;
    private final ModelPart rightThigh;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;
    private final ModelPart tail;

    // Store jump completion for animation
    private float jumpRotation;

    public TimeboundRabbitModel(ModelPart root) {
        this.head = root.getChild("head");
        this.nose = root.getChild("nose");
        this.leftEar = root.getChild("left_ear");
        this.rightEar = root.getChild("right_ear");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftThigh = root.getChild("left_thigh");
        this.rightThigh = root.getChild("right_thigh");
        this.leftFoot = root.getChild("left_foot");
        this.rightFoot = root.getChild("right_foot");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(32, 0).addBox(-2.5F, -4.0F, -5.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.5F, -3.0F));

        partdefinition.addOrReplaceChild("nose", CubeListBuilder.create()
            .texOffs(32, 9).addBox(-0.5F, -2.5F, -5.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.5F, -3.0F));

        partdefinition.addOrReplaceChild("left_ear", CubeListBuilder.create()
            .texOffs(58, 0).addBox(0.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.5F, -3.0F));

        partdefinition.addOrReplaceChild("right_ear", CubeListBuilder.create()
            .texOffs(52, 0).addBox(-2.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.5F, -3.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.0F, -2.0F, -10.0F, 6.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(41, 11).addBox(-2.0F, -2.3F, -9.0F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.0F, 7.0F));

        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(8, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.0F, 17.0F, -3.0F));

        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.0F, 17.0F, -3.0F));

        partdefinition.addOrReplaceChild("left_thigh", CubeListBuilder.create()
            .texOffs(30, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.0F, 16.0F, 2.5F));

        partdefinition.addOrReplaceChild("right_thigh", CubeListBuilder.create()
            .texOffs(16, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.0F, 16.0F, 2.5F));

        partdefinition.addOrReplaceChild("left_foot", CubeListBuilder.create()
            .texOffs(26, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.0F, 14.5F, 4.2F));

        partdefinition.addOrReplaceChild("right_foot", CubeListBuilder.create()
            .texOffs(8, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.0F, 14.5F, 4.2F));

        partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(52, 6).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 17.25F, 6.5F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void prepareMobModel(TimeboundRabbitEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        this.jumpRotation = entity.getJumpCompletion(partialTick);
    }

    @Override
    public void setupAnim(TimeboundRabbitEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Head rotation
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.nose.xRot = this.head.xRot;
        this.nose.yRot = this.head.yRot;
        this.leftEar.xRot = this.head.xRot;
        this.leftEar.yRot = this.head.yRot;
        this.rightEar.xRot = this.head.xRot;
        this.rightEar.yRot = this.head.yRot;

        // Jump animation - modified leg positions during jump
        float jumpAmount = Mth.sin(this.jumpRotation * (float) Math.PI);

        // Front legs extend forward during jump
        this.leftArm.xRot = (jumpAmount * 50.0F - 21.0F) * ((float) Math.PI / 180F);
        this.rightArm.xRot = (jumpAmount * 50.0F - 21.0F) * ((float) Math.PI / 180F);

        // Back legs extend backward during jump
        this.leftThigh.xRot = jumpAmount * -40.0F * ((float) Math.PI / 180F);
        this.rightThigh.xRot = jumpAmount * -40.0F * ((float) Math.PI / 180F);
        this.leftFoot.xRot = jumpAmount * 35.0F * ((float) Math.PI / 180F);
        this.rightFoot.xRot = jumpAmount * 35.0F * ((float) Math.PI / 180F);

        // Ear wiggle animation
        this.leftEar.zRot = -0.0873F + Mth.sin(ageInTicks * 0.1F) * 0.05F;
        this.rightEar.zRot = 0.0873F - Mth.sin(ageInTicks * 0.1F) * 0.05F;

        // Tail wiggle
        this.tail.yRot = Mth.sin(ageInTicks * 0.2F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftEar.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightEar.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
