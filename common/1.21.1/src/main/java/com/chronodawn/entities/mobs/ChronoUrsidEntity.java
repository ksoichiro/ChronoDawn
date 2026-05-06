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
package com.chronodawn.entities.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Chrono Ursid - A polar-bear-equivalent mob.
 * Spawns in ChronoDawn dimension snowy biomes, replacing vanilla polar bears.
 * Inherits all polar bear behavior: standing-up animation, neutral aggression,
 * cub protection, attack damage of 6, max health of 30.
 */
public class ChronoUrsidEntity extends PolarBear {

    public ChronoUrsidEntity(EntityType<? extends ChronoUrsidEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PolarBear.createAttributes();
    }

    /**
     * Check if this entity can spawn at the given location.
     * Uses the standard animal spawn rules to allow spawning in ChronoDawn snowy biomes
     * regardless of surface block (vanilla polar bear requires snow blocks).
     */
    public static boolean checkChronoUrsidSpawnRules(
            EntityType<ChronoUrsidEntity> entityType,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return Animal.checkAnimalSpawnRules(entityType, level, spawnType, pos, random);
    }
}
