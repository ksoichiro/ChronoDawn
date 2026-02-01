package com.chronodawn.client.renderer;

import net.minecraft.client.renderer.entity.state.ArrowRenderState;

/**
 * Render state for Time Arrow entity.
 *
 * 1.21.2: Must extend ArrowRenderState instead of EntityRenderState
 * to satisfy ArrowRenderer's type bounds.
 */
public class TimeArrowRenderState extends ArrowRenderState {
}
