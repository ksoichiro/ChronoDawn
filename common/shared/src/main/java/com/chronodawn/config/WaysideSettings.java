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
 * Settings for interactive wayside features such as the Old Sundial, the Lost Adventurer Memorial, the Time Well, the Watchmaker's Camp and the Time Cairn.
 *
 * <p>Cooldowns are per player and per feature instance, measured in game ticks.
 *
 * @param timeCairnCooldownTicks ticks before the same player can turn the same Time Cairn dial again
 * @param timeCairnEchoChance chance, from 0.0 to 1.0, that a midnight dial turn summons the echo
 */
public record WaysideSettings(
    int sundialCooldownTicks,
    int sundialEffectDurationTicks,
    int memorialCooldownTicks,
    int timeWellCooldownTicks,
    int watchmakerCampCooldownTicks,
    int timeCairnCooldownTicks,
    float timeCairnEchoChance
) {}
