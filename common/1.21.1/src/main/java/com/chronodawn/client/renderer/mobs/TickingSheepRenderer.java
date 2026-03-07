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
import com.chronodawn.entities.mobs.TickingSheepEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Ticking Sheep entity.
 * Uses vanilla sheep texture for the body and custom ticking_sheep.png for the wool layer.
 */
public class TickingSheepRenderer extends MobRenderer<TickingSheepEntity, TickingSheepBodyModel> {
    private static final ResourceLocation BODY_TEXTURE = CompatResourceLocation.create(
        "minecraft",
        "textures/entity/sheep/sheep.png"
    );

    public TickingSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new TickingSheepBodyModel(context.bakeLayer(TickingSheepBodyModel.LAYER_LOCATION)), 0.7f);
        this.addLayer(new TickingSheepWoolLayer(this,
            new TickingSheepWoolModel(context.bakeLayer(TickingSheepWoolModel.LAYER_LOCATION))));
    }

    @Override
    public ResourceLocation getTextureLocation(TickingSheepEntity entity) {
        return BODY_TEXTURE;
    }
}
