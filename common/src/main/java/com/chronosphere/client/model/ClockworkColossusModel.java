package com.chronosphere.client.model;

import com.chronosphere.entities.bosses.ClockworkColossusEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Clockwork Colossus Model
 *
 * Uses HumanoidModel for simplicity (same as Time Guardian/Chronos Warden).
 * Texture will differentiate the mechanical/gear appearance.
 *
 * Reference: research.md (Additional Bosses - Clockwork Colossus)
 * Task: T235h [Phase 1] Create model for Clockwork Colossus
 */
public class ClockworkColossusModel extends HumanoidModel<ClockworkColossusEntity> {
    public ClockworkColossusModel(ModelPart root) {
        super(root);
    }
}
