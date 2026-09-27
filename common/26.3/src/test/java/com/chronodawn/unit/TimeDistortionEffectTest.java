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
import com.chronodawn.config.TimeDistortionSettings;
import com.chronodawn.core.time.TimeDistortionEffect;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/** Tests for the runtime configuration gate in {@link TimeDistortionEffect}. */
class TimeDistortionEffectTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void restoreDefaults() {
        ChronoDawnConfig.set(ConfigDefaults.defaults());
    }

    @Test
    void disabledSetting_doesNotInspectOrModifyEntities() {
        TimeDistortionSettings disabled = new TimeDistortionSettings(
            false, 4, 5, TimeDistortionSettings.Scope.HOSTILE_MOBS
        );
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        ChronoDawnConfig.set(new ChronoDawnConfig(
            defaults.schemaVersion(),
            defaults.world(),
            new ChronoDawnConfig.Gameplay(disabled, defaults.gameplay().bosses())
        ));
        LivingEntity entity = mock(LivingEntity.class);

        TimeDistortionEffect.applyTimeDistortion(entity);

        verifyNoInteractions(entity);
    }
}
