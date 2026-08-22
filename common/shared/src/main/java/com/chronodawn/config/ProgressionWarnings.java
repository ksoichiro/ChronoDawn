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

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the startup warnings for structures a pack has disabled.
 *
 * <p>Disabling a structure is allowed — pack authors routinely substitute their own
 * sources — so the mod states the consequence rather than refusing the setting.
 * Message building is separated from logging so it can be tested directly.
 */
public final class ProgressionWarnings {
    private ProgressionWarnings() {}

    /**
     * @param structures the parsed structure settings
     * @return one message per disabled structure that gates progression, in enum order
     */
    public static List<String> forDisabledStructures(ChronoDawnConfig.Structures structures) {
        List<String> warnings = new ArrayList<>();
        for (ManagedStructure structure : ManagedStructure.values()) {
            if (structure.progressionNote().isEmpty()) {
                continue;
            }
            if (structure.settingsOf(structures).enabled()) {
                continue;
            }
            warnings.add(
                "world.structures." + structure.configKey() + " is disabled: "
                    + structure.progressionNote()
                    + " becomes unobtainable unless your pack provides another source."
            );
        }
        return warnings;
    }
}
