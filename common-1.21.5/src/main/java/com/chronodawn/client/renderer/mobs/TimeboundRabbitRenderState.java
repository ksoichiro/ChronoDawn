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
package com.chronodawn.client.renderer.mobs;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state for Timebound Rabbit entity.
 */
public class TimeboundRabbitRenderState extends LivingEntityRenderState {
    /**
     * Jump animation completion (0.0 - 1.0).
     * Used for leg animation during hop.
     */
    public float jumpRotation;
}
