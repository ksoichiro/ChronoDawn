package com.chronodawn.client.renderer.mobs;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state for Moment Creeper entity.
 * Stores rendering-specific data extracted from MomentCreeperEntity.
 */
public class MomentCreeperRenderState extends LivingEntityRenderState {
    /** Pre-interpolated swelling value (0.0 to 1.0) for model animation */
    public float swelling;
}
