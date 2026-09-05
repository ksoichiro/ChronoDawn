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

import com.chronodawn.client.renderer.mobs.TemporalCapridRenderState;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Temporal Caprid model adapted from the Blockbench export with goat-style walking.
 */
public class TemporalCapridModel extends EntityModel<TemporalCapridRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "temporal_caprid"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart leftHorn;
    private final ModelPart rightHorn;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public TemporalCapridModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.nose = this.head.getChild("nose");
        this.leftHorn = this.head.getChild("left_horn");
        this.rightHorn = this.head.getChild("right_horn");
        this.body = root.getChild("body");
        this.rightHindLeg = root.getChild("leg1");
        this.leftHindLeg = root.getChild("leg2");
        this.rightFrontLeg = root.getChild("leg3");
        this.leftFrontLeg = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(50, 13).addBox(-0.5F, -3.0F, -14.0F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(60, 17).addBox(-6.0F, -11.0F, -10.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.5F, 14.0F, 0.0F));

        head.addOrReplaceChild("mirror", CubeListBuilder.create()
            .texOffs(60, 20).addBox(2.5F, -21.0F, -10.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-0.5F, 10.0F, 0.0F));

        head.addOrReplaceChild("nose", CubeListBuilder.create()
            .texOffs(44, 27).addBox(-3.0F, -4.0F, -8.0F, 5.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -8.0F, -8.0F, 0.9599F, 0.0F, 0.0F));

        head.addOrReplaceChild("left_horn", CubeListBuilder.create()
            .texOffs(24, 52).addBox(-0.01F, -15.0F, -10.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(56, 44).addBox(-0.01F, -20.7F, -10.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.3F))
            .texOffs(40, 57).addBox(1.49F, -15.0F, -10.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_horn", CubeListBuilder.create()
            .texOffs(32, 52).addBox(-2.99F, -15.0F, -10.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(56, 52).addBox(-2.99F, -20.7F, -10.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.3F))
            .texOffs(60, 13).addBox(-5.49F, -15.0F, -10.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 27).addBox(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-0.5F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create()
            .texOffs(0, 52).addBox(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.5F, 14.0F, 4.0F));

        partdefinition.addOrReplaceChild("leg2", CubeListBuilder.create()
            .texOffs(12, 52).addBox(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.5F, 14.0F, 4.0F));

        partdefinition.addOrReplaceChild("leg3", CubeListBuilder.create()
            .texOffs(44, 44).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.5F, 14.0F, -6.0F));

        partdefinition.addOrReplaceChild("leg4", CubeListBuilder.create()
            .texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.5F, 14.0F, -6.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(TemporalCapridRenderState state) {
        super.setupAnim(state);

        float headXRot = state.xRot * ((float) Math.PI / 180F);
        float headYRot = state.yRot * ((float) Math.PI / 180F);
        this.head.xRot = headXRot;
        this.head.yRot = headYRot;
        
        float walkSpeed = state.walkAnimationSpeed;
        float walkPos = state.walkAnimationPos;
        this.rightHindLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
        this.leftHindLeg.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.rightFrontLeg.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.leftFrontLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
    }
}
