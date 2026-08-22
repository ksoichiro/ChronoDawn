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
package com.chronodawn.unit;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ProgressionWarnings;
import com.chronodawn.config.StructureSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionWarningsTest {

    private static ChronoDawnConfig.Structures with(boolean ancientRuins, boolean masterClock, boolean entropyCrypt) {
        ChronoDawnConfig.Structures d = ConfigDefaults.defaults().world().structures();
        return new ChronoDawnConfig.Structures(
            disable(d.ancientRuins(), ancientRuins),
            d.forgottenLibrary(),
            d.desertClockTower(),
            d.guardianVault(),
            d.clockworkDepths(),
            d.phantomCatacombs(),
            disable(d.entropyCrypt(), entropyCrypt),
            disable(d.masterClock(), masterClock)
        );
    }

    private static StructureSettings disable(StructureSettings s, boolean enabled) {
        return new StructureSettings(enabled, s.spacing(), s.separation(), s.salt());
    }

    @Test
    void defaults_produceNoWarnings() {
        assertTrue(ProgressionWarnings.forDisabledStructures(
            ConfigDefaults.defaults().world().structures()).isEmpty());
    }

    @Test
    void disabledAncientRuins_producesNoWarning() {
        assertTrue(ProgressionWarnings.forDisabledStructures(with(false, true, true)).stream()
            .noneMatch(m -> m.contains("ancient_ruins")),
            "Ancient Ruins gates nothing, so disabling it must stay silent");
    }

    @Test
    void disabledProgressionStructures_produceOneMessageEach() {
        List<String> warnings = ProgressionWarnings.forDisabledStructures(with(true, false, false));

        assertEquals(2, warnings.size());
        assertTrue(warnings.get(0).contains("world.structures.entropy_crypt"), warnings.get(0));
        assertTrue(warnings.get(0).contains("Entropy Keeper and the Entropy Core"), warnings.get(0));
        assertTrue(warnings.get(1).contains("world.structures.master_clock"), warnings.get(1));
        assertTrue(warnings.get(1).contains("Time Tyrant, the final boss"), warnings.get(1));
    }
}
