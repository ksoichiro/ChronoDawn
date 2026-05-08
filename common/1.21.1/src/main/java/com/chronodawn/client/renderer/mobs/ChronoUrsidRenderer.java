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
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Chrono Ursid entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/chrono_ursid.png
 */
public class ChronoUrsidRenderer extends MobRenderer<ChronoUrsidEntity, ChronoUrsidModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/chrono_ursid.png"
    );

    public ChronoUrsidRenderer(EntityRendererProvider.Context context) {
        super(context, new ChronoUrsidModel(context.bakeLayer(ChronoUrsidModel.LAYER_LOCATION)), 0.9f);
    }

    @Override
    public ResourceLocation getTextureLocation(ChronoUrsidEntity entity) {
        return TEXTURE;
    }

    /**
     * Match vanilla polar bear's overall size and the half-scale baby in one step.
     *
     * Vanilla 1.20.1/1.21.1 PolarBearRenderer applies poseStack.scale(1.2F) here;
     * vanilla 1.21.11 bakes the same 1.2x at the model layer via MeshTransformer.
     * Our model is left at the bbmodel's native 1.0x, so we apply the 1.2x at
     * render time for consistent sizing across every supported version.
     *
     * Babies render at 0.6x (= 0.5 * 1.2), since AgeableListModel proportional
     * scaling isn't available to a plain EntityModel-derived model.
     */
    @Override
    protected void scale(ChronoUrsidEntity entity, PoseStack poseStack, float partialTick) {
        float scale = entity.isBaby() ? 0.6F : 1.2F;
        poseStack.scale(scale, scale, scale);
    }
}
