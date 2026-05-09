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
 * Immutable record holding the parsed Chrono Dawn configuration.
 *
 * <p>Loaded once at mod startup by {@link ConfigLoader}. Consumers should
 * read values from {@link #INSTANCE} after {@code ConfigLoader.load(...)}
 * has been called from {@code ChronoDawn.init()}.
 *
 * <p>To extend this record with a new tunable, follow the recipe in the
 * design spec ({@code docs/superpowers/specs/2026-05-09-config-foundation-design.md},
 * "Extension pattern" section).
 */
public record ChronoDawnConfig(
    int schemaVersion,
    World world
) {
    /** Latest known schema version this build understands. */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    /**
     * Holds the active configuration once {@code ConfigLoader.load(...)} has
     * been called. Defaults to {@link ConfigDefaults#defaults()} so consumers
     * never see {@code null} even if the loader was skipped (e.g. in tests).
     */
    private static volatile ChronoDawnConfig INSTANCE = ConfigDefaults.defaults();

    public static ChronoDawnConfig get() {
        return INSTANCE;
    }

    /** Replace the active configuration. Called by {@link ConfigLoader}. */
    public static void set(ChronoDawnConfig config) {
        INSTANCE = config;
    }

    public record World(Structures structures) {}

    public record Structures(AncientRuins ancientRuins) {}

    public record AncientRuins(boolean enabled, int spacing, int separation, int salt) {}
}
