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
import com.chronodawn.entities.mobs.ChronoUrsidEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Chrono Ursid Model - Based on Blockbench export with polar-bear-style animations.
 * Adapted for 1.21.1.
 */
public class ChronoUrsidModel extends EntityModel<ChronoUrsidEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "chrono_ursid"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private float standAnimationScale;

    public ChronoUrsidModel(ModelPart root) {
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

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(90, 45).addBox(-3.5F, -3.0F, -3.0F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(90, 75).addBox(-2.5F, 1.0F, -5.0F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(68, 15).addBox(2.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(68, 18).addBox(-4.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 10.0F, -16.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-5.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(0, 25).addBox(-4.0F, -25.0F, -7.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-2.0F, 9.0F, 12.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition back = body.addOrReplaceChild("back", CubeListBuilder.create(),
            PartPose.offset(2.0F, -7.0F, 4.5F));

        PartDefinition clock = back.addOrReplaceChild("clock", CubeListBuilder.create()
            .texOffs(48, 25).addBox(-1.0F, -6.5F, -6.5F, 2.0F, 13.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(0, 49).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offset(3.0F, 0.0F, 0.0F));

        clock.addOrReplaceChild("cube_r1", CubeListBuilder.create()
            .texOffs(50, 0).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        clock.addOrReplaceChild("cube_r2", CubeListBuilder.create()
            .texOffs(60, 51).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        clock.addOrReplaceChild("cube_r3", CubeListBuilder.create()
            .texOffs(30, 51).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

        clock.addOrReplaceChild("cube_r4", CubeListBuilder.create()
            .texOffs(0, 64).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.0944F, 0.0F, 0.0F));

        clock.addOrReplaceChild("cube_r5", CubeListBuilder.create()
            .texOffs(30, 66).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.618F, 0.0F, 0.0F));

        PartDefinition clockwork = back.addOrReplaceChild("clockwork", CubeListBuilder.create()
            .texOffs(60, 66).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F))
            .texOffs(22, 96).addBox(-1.0F, -4.0F, -3.5F, 2.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        clockwork.addOrReplaceChild("cube_r6", CubeListBuilder.create()
            .texOffs(30, 81).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        clockwork.addOrReplaceChild("cube_r7", CubeListBuilder.create()
            .texOffs(80, 0).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        clockwork.addOrReplaceChild("cube_r8", CubeListBuilder.create()
            .texOffs(0, 79).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

        clockwork.addOrReplaceChild("cube_r9", CubeListBuilder.create()
            .texOffs(78, 30).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.0944F, 0.0F, 0.0F));

        clockwork.addOrReplaceChild("cube_r10", CubeListBuilder.create()
            .texOffs(78, 15).addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 13.0F, new CubeDeformation(-0.1F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.618F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create()
            .texOffs(40, 96).addBox(-1.0F, 5.0F, -1.0F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(86, 81).addBox(-1.0F, -5.0F, -2.0F, 5.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.5F, 14.0F, 6.0F));

        partdefinition.addOrReplaceChild("leg2", CubeListBuilder.create()
            .texOffs(60, 81).addBox(-4.0F, -5.0F, -2.0F, 5.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(60, 99).addBox(-3.0F, 5.0F, -1.0F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.5F, 14.0F, 6.0F));

        partdefinition.addOrReplaceChild("leg3", CubeListBuilder.create()
            .texOffs(50, 15).addBox(-1.0F, 5.0F, -2.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(90, 59).addBox(-1.0F, -5.0F, -2.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.5F, 14.0F, -8.0F));

        partdefinition.addOrReplaceChild("leg4", CubeListBuilder.create()
            .texOffs(80, 99).addBox(-3.0F, 5.0F, -2.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(0, 94).addBox(-4.0F, -5.0F, -2.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.5F, 14.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void prepareMobModel(ChronoUrsidEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        this.standAnimationScale = entity.getStandingAnimationScale(partialTick);
    }

    @Override
    public void setupAnim(ChronoUrsidEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float stand = this.standAnimationScale;
        stand = stand * stand;
        float ground = 1.0F - stand;

        // Body rears up (xRot reduces from 90deg baseline) and lifts up (y goes up).
        this.body.xRot = 1.5708F - stand * ((float) Math.PI * 0.4F);
        this.body.y = 9.0F - stand * 4.0F;

        // Head, leg3, leg4 are siblings of body (not children), so they don't follow
        // the body rotation automatically. Translate them by enough that the
        // silhouette matches a reared-up bear (verified against the body-child
        // natural follow positions, then tuned for a clearly visible motion).

        // Head: target the natural body-child follow position (0, -17, 2) at stand=1.
        // Computed by inverse-rotating the original world (0, 10, -16) through body's
        // 90deg baseline into body-local (2, -28, -1), then forward-rotating by body's
        // standing xRot of 0.314 rad. Plus a backward tilt so the snout points forward.
        this.head.xRot = headPitch * ((float) Math.PI / 180F) + stand * (-(float) Math.PI / 4F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.y = 10.0F - stand * 27.0F;
        this.head.z = -16.0F + stand * 18.0F;

        // Front legs (leg3 = front-left, leg4 = front-right): target natural follow
        // (3.5, -8, 1) at stand=1, so paws hang at chest height. Same derivation as
        // head; rotate forward (-1.0 rad ~ 57deg) for a striking-pose paw raise.
        this.leg3.y = 14.0F - stand * 22.0F;
        this.leg4.y = 14.0F - stand * 22.0F;
        this.leg3.z = -8.0F + stand * 9.0F;
        this.leg4.z = -8.0F + stand * 9.0F;

        // Quadruped diagonal-pair walk: (leg1+leg4), (leg2+leg3). Walk fades when
        // standing; back legs settle slightly back, front paws raise forward.
        this.leg1.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * ground - stand * 0.3F;
        this.leg2.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * ground - stand * 0.3F;
        this.leg3.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * ground - stand * 1.0F;
        this.leg4.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * ground - stand * 1.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
