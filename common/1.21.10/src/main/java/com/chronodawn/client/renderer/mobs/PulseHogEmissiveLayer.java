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

import com.chronodawn.client.model.PulseHogModel;
import com.chronodawn.compat.CompatResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Emissive render layer for Pulse Hog.
 * Renders glowing eyes and side patterns using RenderType.eyes().
 */
public class PulseHogEmissiveLayer extends RenderLayer<PulseHogRenderState, PulseHogModel> {
    private static final ResourceLocation EMISSIVE_TEXTURE = CompatResourceLocation.create(
        "chronodawn", "textures/entity/mobs/pulse_hog_emissive.png"
    );
    private static final RenderType EMISSIVE = RenderType.eyes(EMISSIVE_TEXTURE);

    public PulseHogEmissiveLayer(RenderLayerParent<PulseHogRenderState, PulseHogModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       PulseHogRenderState state, float yRot, float xRot) {
        collector.submitModel(this.getParentModel(), state, poseStack, EMISSIVE,
            15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);
    }
}
