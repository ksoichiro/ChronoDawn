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

/**
 * Applies the configured multipliers to boss base statistics.
 *
 * <p>Every method reads {@code ChronoDawnConfig} at call time rather than
 * caching. That matters for {@link #ability}: a {@code static final} field
 * initialised at class load could run before the config is read, silently
 * capturing the default multiplier.
 */
public final class BossScaling {

    private BossScaling() {}

    /** Maximum health for the boss's {@code MAX_HEALTH} attribute. */
    public static double health(BossKind kind) {
        return kind.baseMaxHealth() * kind.settings().healthMultiplier();
    }

    /** Melee damage for the boss's {@code ATTACK_DAMAGE} attribute. */
    public static double attackDamage(BossKind kind) {
        return kind.baseAttackDamage() * kind.settings().damageMultiplier();
    }

    /** Damage for a non-attribute ability, scaled by its owner's multiplier. */
    public static float ability(BossAbility ability) {
        return (float) (ability.baseDamage() * ability.owner().settings().damageMultiplier());
    }
}
