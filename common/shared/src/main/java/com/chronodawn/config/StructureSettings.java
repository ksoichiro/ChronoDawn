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
 * Placement settings for one structure set.
 *
 * <p>{@code salt} is a {@code long} because one shipped structure set
 * ({@code forgotten_library}) uses a value above {@link Integer#MAX_VALUE}.
 * Vanilla reads salt as an int and truncates; keeping the full value here lets
 * the runtime overlay reproduce the bundled JSON byte-for-byte.
 */
public record StructureSettings(boolean enabled, int spacing, int separation, long salt) {}
