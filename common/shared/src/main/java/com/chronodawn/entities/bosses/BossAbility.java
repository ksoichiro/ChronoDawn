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
 * Every boss damage source that is not the {@code ATTACK_DAMAGE} attribute.
 *
 * <p>Each constant names its owning boss so {@code damage_multiplier} resolves
 * correctly even when two bosses share a projectile type: Time Guardian and
 * Temporal Phantom both fire Time Blast, and each scales by its own config.
 */
public enum BossAbility {
    TIME_GUARDIAN_AOE(BossKind.TIME_GUARDIAN, 6.0f),
    TIME_GUARDIAN_TIME_BLAST(BossKind.TIME_GUARDIAN, 4.0f),
    CHRONOS_WARDEN_GROUND_SLAM(BossKind.CHRONOS_WARDEN, 2.0f),
    CLOCKWORK_COLOSSUS_GROUND_SLAM(BossKind.CLOCKWORK_COLOSSUS, 6.0f),
    CLOCKWORK_COLOSSUS_GEAR_PROJECTILE(BossKind.CLOCKWORK_COLOSSUS, 8.0f),
    ENTROPY_KEEPER_SLAM(BossKind.ENTROPY_KEEPER, 10.0f),
    TEMPORAL_PHANTOM_TIME_BLAST(BossKind.TEMPORAL_PHANTOM, 4.0f),
    TIME_TYRANT_AOE(BossKind.TIME_TYRANT, 12.0f);

    private final BossKind owner;
    private final float baseDamage;

    BossAbility(BossKind owner, float baseDamage) {
        this.owner = owner;
        this.baseDamage = baseDamage;
    }

    public BossKind owner() {
        return owner;
    }

    public float baseDamage() {
        return baseDamage;
    }
}
