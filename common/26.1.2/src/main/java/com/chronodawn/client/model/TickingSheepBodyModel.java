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

import com.chronodawn.client.renderer.mobs.TickingSheepRenderState;
import com.chronodawn.compat.CompatResourceLocation;
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
public class TickingSheepBodyModel extends EntityModel<TickingSheepRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "ticking_sheep"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public TickingSheepBodyModel(ModelPart root) {
        super(root);
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
    public void setupAnim(TickingSheepRenderState state) {
        super.setupAnim(state);
        this.head.y = 6.0F + state.headEatPositionScale * 9.0F;
        this.head.xRot = state.headEatAngleScale;
        this.head.yRot = state.yRot * ((float) Math.PI / 180F);
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
        this.leftHindLeg.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.rightFrontLeg.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.leftFrontLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
    }
}
