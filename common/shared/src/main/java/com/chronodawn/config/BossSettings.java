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
 * Multipliers applied to a single boss's statistics.
 *
 * <p>{@code healthMultiplier} scales the boss's {@code MAX_HEALTH} attribute.
 * {@code damageMultiplier} scales the boss's direct damage: its
 * {@code ATTACK_DAMAGE} attribute, its area-of-effect and ground-slam
 * abilities, the projectiles it fires, and Entropy Keeper's degradation. It
 * does <b>not</b> scale status effects a boss applies (Wither, Poison,
 * Slowness, Mining Fatigue) — a {@code MobEffect} amplifier is an integer and
 * cannot express a fractional multiplier, so those continue at full strength
 * even at {@code 0.0}. Both fields default to {@code 1.0}, which reproduces
 * the hardcoded values exactly.
 */
public record BossSettings(double healthMultiplier, double damageMultiplier) {}
