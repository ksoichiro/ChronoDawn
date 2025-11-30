package com.chronosphere.client.model;

import com.chronosphere.entities.bosses.ClockworkColossusEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

/**
 * Clockwork Colossus Model
 *
 * Uses HumanoidModel for simplicity (same as Chronos Warden).
 * Uses vanilla player model layer definition.
 * Texture will differentiate the mechanical/gear appearance.
 *
 * Reference: research.md (Additional Bosses - Clockwork Colossus)
 * Task: T235h [Phase 1] Create model for Clockwork Colossus
 */
public class ClockworkColossusModel extends HumanoidModel<ClockworkColossusEntity> {
    public ClockworkColossusModel(ModelPart root) {
        super(root);
    }

    /**
     * Create body layer definition for Clockwork Colossus.
     * Uses vanilla humanoid model with no deformation.
     */
    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(
            HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F),
            64,
            64
        );
    }
}
