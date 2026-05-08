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

import com.chronodawn.client.model.ChronoUrsidModel;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.entities.mobs.ChronoUrsidEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Renderer for Chrono Ursid entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/chrono_ursid.png
 */
public class ChronoUrsidRenderer extends MobRenderer<ChronoUrsidEntity, ChronoUrsidRenderState, ChronoUrsidModel> {
    private static final Identifier TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/chrono_ursid.png"
    );

    public ChronoUrsidRenderer(EntityRendererProvider.Context context) {
        super(context, new ChronoUrsidModel(context.bakeLayer(ChronoUrsidModel.LAYER_LOCATION)), 0.9f);
    }

    @Override
    public ChronoUrsidRenderState createRenderState() {
        return new ChronoUrsidRenderState();
    }

    @Override
    public void extractRenderState(ChronoUrsidEntity entity, ChronoUrsidRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.standAnimationScale = entity.getStandingAnimationScale(partialTick);
    }

    @Override
    public Identifier getTextureLocation(ChronoUrsidRenderState state) {
        return TEXTURE;
    }

    /**
     * Match vanilla polar bear's overall size and the half-scale baby in one step.
     *
     * Vanilla 1.21.2+ PolarBearModel bakes a 1.2x mesh transform into its body
     * layer, so the rendered model ends up 1.2x its bbmodel-native size. Our
     * model is left at native 1.0x, so we apply the 1.2x here at render time.
     *
     * Babies render at 0.6x (= 0.5 * 1.2). Vanilla achieves the smaller cub via
     * AgeableMobRenderer swapping to a separately-baked baby layer; we use a
     * single model and scale uniformly in this method instead.
     */
    @Override
    protected void scale(ChronoUrsidRenderState state, PoseStack poseStack) {
        float scale = state.isBaby ? 0.6F : 1.2F;
        poseStack.scale(scale, scale, scale);
    }
}
