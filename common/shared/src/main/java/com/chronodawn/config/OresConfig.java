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
package com.chronodawn.config;

/**
 * Container for the ChronoDawn-specific ores currently exposed via
 * {@code config/chronodawn.toml}: Time Crystal, Entropy Crystal,
 * Temporal Amber, and Clockstone. The vanilla-overlay ores
 * (iron/gold/coal/redstone) are deliberately not exposed here — see
 * the design spec for the rationale.
 */
public record OresConfig(
    OreSettings timeCrystal,
    OreSettings entropyCrystal,
    OreSettings temporalAmber,
    OreSettings clockstone
) {}
