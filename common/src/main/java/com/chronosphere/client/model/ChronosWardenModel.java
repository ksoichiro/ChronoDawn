package com.chronosphere.client.model;

import com.chronosphere.entities.bosses.ChronosWardenEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Chronos Warden Model
 *
 * Uses Minecraft's built-in HumanoidModel for simplicity.
 * This provides a humanoid shape similar to Iron Golem or Zombie.
 *
 * Task: T234e [Phase 1] Create ChronosWardenModel
 */
public class ChronosWardenModel extends HumanoidModel<ChronosWardenEntity> {
    public ChronosWardenModel(ModelPart root) {
        super(root);
    }
}
