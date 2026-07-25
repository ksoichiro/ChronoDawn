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
package com.chronodawn.entities.bosses;

import net.minecraft.world.entity.Entity;

/**
 * Resolves a projectile's damage from the boss that fired it.
 *
 * <p>Time Blast is fired by both Time Guardian and Temporal Phantom, so the
 * projectile cannot know its multiplier at construction time — it has to ask
 * its owner at impact. Gear Projectile has a single owner today but uses the
 * same path so the two do not drift apart.
 *
 * <p>An unowned projectile (owner despawned, or spawned by a command) falls
 * back to the unscaled base damage.
 */
public final class BossProjectileDamage {

    private BossProjectileDamage() {}

    public static float timeBlast(Entity owner) {
        if (owner instanceof TimeGuardianEntity) {
            return BossScaling.ability(BossAbility.TIME_GUARDIAN_TIME_BLAST);
        }
        if (owner instanceof TemporalPhantomEntity) {
            return BossScaling.ability(BossAbility.TEMPORAL_PHANTOM_TIME_BLAST);
        }
        return BossAbility.TIME_GUARDIAN_TIME_BLAST.baseDamage();
    }

    public static float gearProjectile(Entity owner) {
        if (owner instanceof ClockworkColossusEntity) {
            return BossScaling.ability(BossAbility.CLOCKWORK_COLOSSUS_GEAR_PROJECTILE);
        }
        return BossAbility.CLOCKWORK_COLOSSUS_GEAR_PROJECTILE.baseDamage();
    }
}
