package com.chronodawn.client.renderer.mobs;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state for Floq entity.
 * Stores rendering-specific data extracted from FloqEntity.
 */
public class FloqRenderState extends LivingEntityRenderState {
    public float squish;
    public float oSquish;
    public boolean onGround;
}
