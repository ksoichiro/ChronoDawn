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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Chrono Ursid entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/chrono_ursid.png
 */
public class ChronoUrsidRenderer extends MobRenderer<ChronoUrsidEntity, ChronoUrsidRenderState, ChronoUrsidModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
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
    public ResourceLocation getTextureLocation(ChronoUrsidRenderState state) {
        return TEXTURE;
    }
}
