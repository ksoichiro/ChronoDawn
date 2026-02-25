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

import com.chronodawn.client.model.SecondwingFowlModel;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.entities.mobs.SecondwingFowlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Secondwing Fowl entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/secondwing_fowl.png
 */
public class SecondwingFowlRenderer extends MobRenderer<SecondwingFowlEntity, SecondwingFowlModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/secondwing_fowl.png"
    );

    public SecondwingFowlRenderer(EntityRendererProvider.Context context) {
        super(context, new SecondwingFowlModel(context.bakeLayer(SecondwingFowlModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(SecondwingFowlEntity entity) {
        return TEXTURE;
    }
}
