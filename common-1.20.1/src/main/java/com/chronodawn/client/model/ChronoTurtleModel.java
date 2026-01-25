package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.ChronoTurtleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.util.Mth;

/**
 * ChronoTurtle Model - 1.20.1 version
 * Uses EntityModel<Entity> pattern.
 * Based on Blockbench export.
 * Texture size: 128x64
 */
public class ChronoTurtleModel extends EntityModel<ChronoTurtleEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create("chronodawn", "chrono_turtle"),
        "main"
    );

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart body2;  // Shell belly
    private final ModelPart leg1;   // Back left
    private final ModelPart leg2;   // Back right
    private final ModelPart leg3;   // Front left
    private final ModelPart leg4;   // Front right

    public ChronoTurtleModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.body2 = root.getChild("body2");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Head
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(3, 0).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 19.0F, -10.0F));

        // Body (shell)
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(),
            PartPose.offset(0.0F, 11.0F, -10.0F));

        body.addOrReplaceChild("body_rotation", CubeListBuilder.create()
            .texOffs(7, 37).addBox(-9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(31, 1).addBox(-5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        // Body2 (shell belly)
        PartDefinition body2 = partdefinition.addOrReplaceChild("body2", CubeListBuilder.create(),
            PartPose.offset(0.0F, 11.0F, -10.0F));

        body2.addOrReplaceChild("body2_rotation", CubeListBuilder.create()
            .texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        // Back legs (flippers)
        partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create()
            .texOffs(1, 23).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.5F, 22.0F, 11.0F));

        partdefinition.addOrReplaceChild("leg2", CubeListBuilder.create()
            .texOffs(1, 12).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.5F, 22.0F, 11.0F));

        // Front legs (flippers)
        partdefinition.addOrReplaceChild("leg3", CubeListBuilder.create()
            .texOffs(27, 30).addBox(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-5.0F, 21.0F, -4.0F));

        partdefinition.addOrReplaceChild("leg4", CubeListBuilder.create()
            .texOffs(27, 24).addBox(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(5.0F, 21.0F, -4.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(ChronoTurtleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Swimming animation for flippers
        // Front flippers move like paddling
        float frontFlipperAngle = Mth.cos(ageInTicks * 0.3F) * 0.4F;
        this.leg3.zRot = frontFlipperAngle;
        this.leg4.zRot = -frontFlipperAngle;

        // Back flippers move slightly
        float backFlipperAngle = Mth.cos(ageInTicks * 0.25F) * 0.2F;
        this.leg1.yRot = backFlipperAngle;
        this.leg2.yRot = -backFlipperAngle;

        // Head bobs slightly
        this.head.xRot = Mth.sin(ageInTicks * 0.1F) * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
