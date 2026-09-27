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
 * Ticking Sheep wool layer model - exported from Blockbench.
 * Renders on top of the body model with explosive-looking wool and fuse protrusions.
 * Uses ticking_sheep.png texture.
 */
public class TickingSheepWoolModel extends EntityModel<TickingSheepRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "ticking_sheep_wool"),
        "main"
    );

    private static final float BODY_R1_INIT_Z_ROT = -0.1833F;
    private static final float BODY_R2_INIT_Z_ROT = -0.1745F;
    private static final float BODY_R3_INIT_Z_ROT = 0.1833F;
    private static final float BODY_R4_INIT_Z_ROT = 0.1745F;

    private final ModelPart body;
    private final ModelPart body2;
    private final ModelPart body3;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart bodyR1;
    private final ModelPart bodyR2;
    private final ModelPart bodyR3;
    private final ModelPart bodyR4;

    public TickingSheepWoolModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.body2 = root.getChild("body2");
        this.body3 = root.getChild("body3");
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.bodyR1 = this.body2.getChild("body_r1");
        this.bodyR2 = this.body2.getChild("body_r2");
        this.bodyR3 = this.body3.getChild("body_r3");
        this.bodyR4 = this.body3.getChild("body_r4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Main wool body
        PartDefinition body = partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create(),
            PartPose.offset(0.0F, 5.0F, 2.0F));

        body.addOrReplaceChild("rotation",
            CubeListBuilder.create()
                .texOffs(28, 8).addBox(-4.0F, -10.5F, -7.0F, 8.0F, 16.0F, 6.0F, new CubeDeformation(1.75F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        // Fuse protrusions - right side
        PartDefinition body2 = partdefinition.addOrReplaceChild("body2",
            CubeListBuilder.create(),
            PartPose.offset(0.0F, 6.0F, -8.0F));

        body2.addOrReplaceChild("body_r1",
            CubeListBuilder.create()
                .texOffs(25, -2).addBox(8.75F, -17.0F, 6.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(8.75F, -17.0F, 0.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(8.75F, -17.0F, -6.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 18.0F, 8.0F, 0.0F, 0.0F, BODY_R1_INIT_Z_ROT));

        body2.addOrReplaceChild("body_r2",
            CubeListBuilder.create()
                .texOffs(25, -2).addBox(8.75F, -17.0F, 3.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(8.75F, -17.0F, -3.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(8.75F, -17.0F, -9.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 18.0F, 8.0F, 0.0F, 0.0F, BODY_R2_INIT_Z_ROT));

        // Fuse protrusions - left side
        PartDefinition body3 = partdefinition.addOrReplaceChild("body3",
            CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        body3.addOrReplaceChild("body_r3",
            CubeListBuilder.create()
                .texOffs(25, -2).addBox(-8.75F, -17.0F, 0.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(-8.75F, -17.0F, -6.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(-8.75F, -17.0F, 6.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, BODY_R3_INIT_Z_ROT));

        body3.addOrReplaceChild("body_r4",
            CubeListBuilder.create()
                .texOffs(25, -2).addBox(-8.75F, -17.0F, 3.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(-8.75F, -17.0F, -3.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, -2).addBox(-8.75F, -17.0F, -9.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, BODY_R4_INIT_Z_ROT));

        // Head wool
        partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.6F)),
            PartPose.offset(0.0F, 6.0F, -8.0F));

        // Leg wool
        partdefinition.addOrReplaceChild("leg1",
            CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)),
            PartPose.offset(-3.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("leg2",
            CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)),
            PartPose.offset(3.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("leg3",
            CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)),
            PartPose.offset(-3.0F, 12.0F, -5.0F));

        partdefinition.addOrReplaceChild("leg4",
            CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)),
            PartPose.offset(3.0F, 12.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(TickingSheepRenderState state) {
        super.setupAnim(state);
        // Sync animation with body model
        this.head.y = 6.0F + state.headEatPositionScale * 9.0F;
        this.head.xRot = state.headEatAngleScale;
        this.head.yRot = state.yRot * ((float) Math.PI / 180F);
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        this.leg1.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
        this.leg2.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.leg3.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed;
        this.leg4.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;

        // Fur protrusion sway: X rotation only (forward-backward rocking)
        // Z rotation is left at initial values to avoid top clipping into body
        float ageInTicks = state.ageInTicks;
        float idleRock = Mth.sin(ageInTicks * 0.09F) * 0.04F;
        float walkRock = Mth.sin(walkPos * 0.5F) * 0.12F * walkSpeed;

        this.bodyR1.xRot = idleRock + walkRock;
        this.bodyR2.xRot = Mth.sin(ageInTicks * 0.09F + 0.8F) * 0.04F + Mth.sin(walkPos * 0.5F + 1.0F) * 0.12F * walkSpeed;
        this.bodyR3.xRot = Mth.sin(ageInTicks * 0.09F + 1.6F) * 0.04F + Mth.sin(walkPos * 0.5F + 2.0F) * 0.12F * walkSpeed;
        this.bodyR4.xRot = Mth.sin(ageInTicks * 0.09F + 2.4F) * 0.04F + Mth.sin(walkPos * 0.5F + 3.0F) * 0.12F * walkSpeed;
    }

    // Note: renderToBuffer() is now final in 1.21.2 and automatically renders all child parts.
}
