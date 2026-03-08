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

import com.chronodawn.client.model.TickingSheepBodyModel;
import com.chronodawn.client.model.TickingSheepWoolModel;
import com.chronodawn.compat.CompatResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render layer for Ticking Sheep wool.
 * Renders the custom wool model on top of the vanilla-like sheep body.
 */
public class TickingSheepWoolLayer extends RenderLayer<TickingSheepRenderState, TickingSheepBodyModel> {
    private static final ResourceLocation WOOL_TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/ticking_sheep.png"
    );

    private final TickingSheepWoolModel woolModel;

    public TickingSheepWoolLayer(RenderLayerParent<TickingSheepRenderState, TickingSheepBodyModel> parent, TickingSheepWoolModel woolModel) {
        super(parent);
        this.woolModel = woolModel;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       TickingSheepRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.sheared) {
            return;
        }

        this.woolModel.setupAnim(state);
        // Use coloredCutoutModelCopyLayerRender (same as vanilla SheepWoolLayer)
        // to ensure proper depth testing, lighting, and texture rendering
        coloredCutoutModelCopyLayerRender(this.woolModel, WOOL_TEXTURE,
            poseStack, collector, packedLight, state, 0xFFFFFFFF, 0);
    }
}
