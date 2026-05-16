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
 * Tunable settings for a single ore's placed-feature generation.
 *
 * <p>{@code count} is the number of placement attempts per chunk (the
 * leading {@code minecraft:count} step). {@code yMin} and {@code yMax} are
 * absolute Y bounds for the {@code minecraft:height_range} modifier. When
 * {@code enabled} is {@code false}, the runtime overlay emits the same
 * shape with {@code count = 0} so the resource stays registered but
 * generates nothing.
 */
public record OreSettings(boolean enabled, int count, int yMin, int yMax) {}
