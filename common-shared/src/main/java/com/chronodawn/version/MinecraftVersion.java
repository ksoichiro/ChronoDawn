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
package com.chronodawn.version;

/**
 * Enum representing supported Minecraft versions.
 * Used for version-specific feature availability checks.
 */
public enum MinecraftVersion {
    MC_1_20_1("1.20.1", 1, 20, 1),
    MC_1_21_1("1.21.1", 1, 21, 1),
    MC_1_21_2("1.21.2", 1, 21, 2);

    private final String displayName;
    private final int major;
    private final int minor;
    private final int patch;

    MinecraftVersion(String displayName, int major, int minor, int patch) {
        this.displayName = displayName;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Returns the display name of this version (e.g., "1.21.2").
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the major version number.
     */
    public int major() {
        return major;
    }

    /**
     * Returns the minor version number.
     */
    public int minor() {
        return minor;
    }

    /**
     * Returns the patch version number.
     */
    public int patch() {
        return patch;
    }

    /**
     * Checks if this version is at least the specified version.
     *
     * @param other the version to compare against
     * @return true if this version is greater than or equal to other
     */
    public boolean isAtLeast(MinecraftVersion other) {
        if (this.major != other.major) {
            return this.major > other.major;
        }
        if (this.minor != other.minor) {
            return this.minor > other.minor;
        }
        return this.patch >= other.patch;
    }

    /**
     * Returns the oldest supported Minecraft version.
     */
    public static MinecraftVersion oldest() {
        return MC_1_20_1;
    }

    /**
     * Returns the newest supported Minecraft version.
     */
    public static MinecraftVersion newest() {
        return MC_1_21_2;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
