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

import com.chronodawn.client.renderer.mobs.SecondwingFowlRenderState;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Secondwing Fowl Model - Based on Blockbench export.
 * Adapted for 1.21.2 RenderState pattern with chicken-style animations.
 */
public class SecondwingFowlModel extends EntityModel<SecondwingFowlRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            CompatResourceLocation.create("chronodawn", "secondwing_fowl"),
            "main"
    );

    private final ModelPart head;
    private final ModelPart bill;
    private final ModelPart chin;
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public SecondwingFowlModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.bill = root.getChild("bill");
        this.chin = root.getChild("chin");
        this.body = root.getChild("body");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(0, 23).addBox(-2.0F, -10.0F, -1.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 15.0F, -4.0F));

        partdefinition.addOrReplaceChild("bill", CubeListBuilder.create()
            .texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 15.0F, -4.0F));

        partdefinition.addOrReplaceChild("chin", CubeListBuilder.create()
            .texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 15.0F, -4.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create()
            .texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 13.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create()
            .texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 13.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(1.0F, 19.0F, 1.0F));

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 19.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(SecondwingFowlRenderState state) {
        super.setupAnim(state);

        // Head rotation
        this.head.xRot = state.xRot * ((float) Math.PI / 180F);
        this.head.yRot = state.yRot * ((float) Math.PI / 180F);

        // Walking animation - standard quadruped leg movement
        float walkSpeed = state.walkAnimationSpeed;
        float walkPos = state.walkAnimationPos;

        this.rightLeg.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.leftLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
    }
}
