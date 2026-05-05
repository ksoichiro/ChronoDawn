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

import com.chronodawn.client.model.ChronoBovineModel;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.entities.mobs.ChronoBovineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Chrono Bovine entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/chrono_bovine.png
 */
public class ChronoBovineRenderer extends MobRenderer<ChronoBovineEntity, ChronoBovineModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/chrono_bovine.png"
    );

    public ChronoBovineRenderer(EntityRendererProvider.Context context) {
        super(context, new ChronoBovineModel(context.bakeLayer(ChronoBovineModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(ChronoBovineEntity entity) {
        return TEXTURE;
    }
}
