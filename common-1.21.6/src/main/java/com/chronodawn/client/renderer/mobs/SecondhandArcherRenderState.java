package com.chronodawn.client.renderer.mobs;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;

/**
 * Render state for Secondhand Archer entity.
 * Extends ArmedEntityRenderState to support ItemInHandLayer in 1.21.4.
 */
public class SecondhandArcherRenderState extends ArmedEntityRenderState {
    public boolean isAggressive;
}
