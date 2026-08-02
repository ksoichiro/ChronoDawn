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

import java.util.Arrays;
import java.util.Optional;

/** Settings for the Chrono Dawn dimension's ambient Time Distortion effect. */
public record TimeDistortionSettings(
    boolean enabled,
    int normalSlownessLevel,
    int enhancedSlownessLevel,
    Scope scope
) {
    /** The supported entity scopes, using stable lowercase TOML values. */
    public enum Scope {
        HOSTILE_MOBS("hostile_mobs"),
        ALL_MOBS("all_mobs");

        private final String configValue;

        Scope(String configValue) {
            this.configValue = configValue;
        }

        public String configValue() {
            return configValue;
        }

        public static Optional<Scope> fromConfigValue(String value) {
            return Arrays.stream(values()).filter(scope -> scope.configValue.equals(value)).findFirst();
        }
    }

    /** Convert the player-visible potion level to Minecraft's zero-based amplifier. */
    public int slownessAmplifier(boolean enhanced) {
        return (enhanced ? enhancedSlownessLevel : normalSlownessLevel) - 1;
    }
}
