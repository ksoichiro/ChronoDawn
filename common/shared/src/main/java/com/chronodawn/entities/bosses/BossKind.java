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

import com.chronodawn.config.BossSettings;
import com.chronodawn.config.BossesConfig;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.registry.ModEntityId;

import java.util.function.Function;

/**
 * The six boss entities and their shipped base statistics.
 *
 * <p>This enum is the single definition site for boss {@code MAX_HEALTH} and
 * {@code ATTACK_DAMAGE}. The values used to be duplicated in every version
 * module's {@code createAttributes()}; keeping them here means a balance
 * change is a one-line edit instead of eleven.
 *
 * <p>Deliberately free of Minecraft imports so it compiles unchanged across
 * 1.20.1 through 1.21.11.
 */
public enum BossKind {
    TIME_GUARDIAN(ModEntityId.TIME_GUARDIAN, 200.0, 10.0, b -> b.timeGuardian()),
    CHRONOS_WARDEN(ModEntityId.CHRONOS_WARDEN, 180.0, 9.0, b -> b.chronosWarden()),
    CLOCKWORK_COLOSSUS(ModEntityId.CLOCKWORK_COLOSSUS, 200.0, 12.0, b -> b.clockworkColossus()),
    ENTROPY_KEEPER(ModEntityId.ENTROPY_KEEPER, 160.0, 10.0, b -> b.entropyKeeper()),
    TEMPORAL_PHANTOM(ModEntityId.TEMPORAL_PHANTOM, 150.0, 8.0, b -> b.temporalPhantom()),
    TIME_TYRANT(ModEntityId.TIME_TYRANT, 500.0, 18.0, b -> b.timeTyrant());

    private final ModEntityId entityId;
    private final double baseMaxHealth;
    private final double baseAttackDamage;
    private final Function<BossesConfig, BossSettings> selector;

    BossKind(
        ModEntityId entityId,
        double baseMaxHealth,
        double baseAttackDamage,
        Function<BossesConfig, BossSettings> selector
    ) {
        this.entityId = entityId;
        this.baseMaxHealth = baseMaxHealth;
        this.baseAttackDamage = baseAttackDamage;
        this.selector = selector;
    }

    /** The {@code [gameplay.bosses.<key>]} table name, equal to the entity ID. */
    public String configKey() {
        return entityId.id();
    }

    public double baseMaxHealth() {
        return baseMaxHealth;
    }

    public double baseAttackDamage() {
        return baseAttackDamage;
    }

    /** The multipliers currently configured for this boss. */
    public BossSettings settings() {
        return selector.apply(ChronoDawnConfig.get().gameplay().bosses());
    }
}
