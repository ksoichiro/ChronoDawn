package com.chronodawn.client.renderer;

import com.chronodawn.entities.boats.ChronoDawnBoatType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;

/**
 * Render state for ChronoDawn Chest Boat entity.
 * Stores rendering-specific data extracted from ChronoDawnChestBoat.
 *
 * Extends BoatRenderState to inherit boat rendering properties,
 * and adds custom boatType field for ChronoDawn-specific chest boats.
 */
public class ChronoDawnChestBoatRenderState extends BoatRenderState {
    /**
     * Custom boat type for ChronoDawn chest boats.
     * Note: Other fields (yaw, damageWobbleSide, damageWobbleTicks, damageWobbleStrength,
     * bubbleWobble, submergedInWater, leftPaddleAngle, rightPaddleAngle)
     * are inherited from BoatRenderState.
     */
    public ChronoDawnBoatType boatType;
}
