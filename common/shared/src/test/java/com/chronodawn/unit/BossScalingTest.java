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
import com.chronodawn.entities.bosses.BossAbility;
import com.chronodawn.entities.bosses.BossKind;
import com.chronodawn.entities.bosses.BossScaling;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link BossScaling}. The most important case is
 * {@code defaultConfig_isExactlyBaseValues}: it pins the contract that a user
 * who never edits the config observes byte-identical boss statistics.
 */
class BossScalingTest {

    @AfterEach
    void restoreDefaults() {
        // BossScaling reads the process-wide config singleton; leaving a
        // modified config behind would leak into other test classes.
        ChronoDawnConfig.set(ConfigDefaults.defaults());
    }

    @Test
    void defaultConfig_isExactlyBaseValues() {
        ChronoDawnConfig.set(ConfigDefaults.defaults());

        for (BossKind kind : BossKind.values()) {
            assertEquals(kind.baseMaxHealth(), BossScaling.health(kind), 0.0,
                kind + " health must be unmodified at the default multiplier");
            assertEquals(kind.baseAttackDamage(), BossScaling.attackDamage(kind), 0.0,
                kind + " attack damage must be unmodified at the default multiplier");
        }
        for (BossAbility ability : BossAbility.values()) {
            assertEquals(ability.baseDamage(), BossScaling.ability(ability), 0.0f,
                ability + " must be unmodified at the default multiplier");
        }
    }

    @Test
    void baseValuesMatchShippedBalance() {
        // Guards against a typo while moving literals out of the version modules.
        assertEquals(200.0, BossKind.TIME_GUARDIAN.baseMaxHealth(), 0.0);
        assertEquals(10.0, BossKind.TIME_GUARDIAN.baseAttackDamage(), 0.0);
        assertEquals(180.0, BossKind.CHRONOS_WARDEN.baseMaxHealth(), 0.0);
        assertEquals(9.0, BossKind.CHRONOS_WARDEN.baseAttackDamage(), 0.0);
        assertEquals(200.0, BossKind.CLOCKWORK_COLOSSUS.baseMaxHealth(), 0.0);
        assertEquals(12.0, BossKind.CLOCKWORK_COLOSSUS.baseAttackDamage(), 0.0);
        assertEquals(160.0, BossKind.ENTROPY_KEEPER.baseMaxHealth(), 0.0);
        assertEquals(10.0, BossKind.ENTROPY_KEEPER.baseAttackDamage(), 0.0);
        assertEquals(150.0, BossKind.TEMPORAL_PHANTOM.baseMaxHealth(), 0.0);
        assertEquals(8.0, BossKind.TEMPORAL_PHANTOM.baseAttackDamage(), 0.0);
        assertEquals(500.0, BossKind.TIME_TYRANT.baseMaxHealth(), 0.0);
        assertEquals(18.0, BossKind.TIME_TYRANT.baseAttackDamage(), 0.0);

        assertEquals(6.0f, BossAbility.TIME_GUARDIAN_AOE.baseDamage(), 0.0f);
        assertEquals(4.0f, BossAbility.TIME_GUARDIAN_TIME_BLAST.baseDamage(), 0.0f);
        assertEquals(2.0f, BossAbility.CHRONOS_WARDEN_GROUND_SLAM.baseDamage(), 0.0f);
        assertEquals(6.0f, BossAbility.CLOCKWORK_COLOSSUS_GROUND_SLAM.baseDamage(), 0.0f);
        assertEquals(8.0f, BossAbility.CLOCKWORK_COLOSSUS_GEAR_PROJECTILE.baseDamage(), 0.0f);
        assertEquals(10.0f, BossAbility.ENTROPY_KEEPER_SLAM.baseDamage(), 0.0f);
        assertEquals(4.0f, BossAbility.TEMPORAL_PHANTOM_TIME_BLAST.baseDamage(), 0.0f);
        assertEquals(12.0f, BossAbility.TIME_TYRANT_AOE.baseDamage(), 0.0f);
    }

    @Test
    void healthMultiplier_scalesOnlyTheTargetBoss(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "health_multiplier = 2.0\n");
        com.chronodawn.config.ConfigLoader.load(tmp);

        assertEquals(400.0, BossScaling.health(BossKind.TIME_GUARDIAN), 0.0);
        assertEquals(500.0, BossScaling.health(BossKind.TIME_TYRANT), 0.0,
            "Other bosses must be untouched");
    }

    @Test
    void damageMultiplier_scalesMeleeAndAbilities(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "damage_multiplier = 0.5\n");
        com.chronodawn.config.ConfigLoader.load(tmp);

        assertEquals(5.0, BossScaling.attackDamage(BossKind.TIME_GUARDIAN), 0.0);
        assertEquals(3.0f, BossScaling.ability(BossAbility.TIME_GUARDIAN_AOE), 0.0f);
        assertEquals(2.0f, BossScaling.ability(BossAbility.TIME_GUARDIAN_TIME_BLAST), 0.0f);
        // Temporal Phantom fires the same projectile type but is configured
        // separately, so its Time Blast stays at the base value.
        assertEquals(4.0f, BossScaling.ability(BossAbility.TEMPORAL_PHANTOM_TIME_BLAST), 0.0f);
    }

    @Test
    void damageMultiplierZero_yieldsZeroDamage(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_tyrant]\n" +
            "damage_multiplier = 0.0\n");
        com.chronodawn.config.ConfigLoader.load(tmp);

        assertEquals(0.0, BossScaling.attackDamage(BossKind.TIME_TYRANT), 0.0);
        assertEquals(0.0f, BossScaling.ability(BossAbility.TIME_TYRANT_AOE), 0.0f);
    }

    @Test
    void configKeysMatchTomlTableNames() {
        assertEquals("time_guardian", BossKind.TIME_GUARDIAN.configKey());
        assertEquals("chronos_warden", BossKind.CHRONOS_WARDEN.configKey());
        assertEquals("clockwork_colossus", BossKind.CLOCKWORK_COLOSSUS.configKey());
        assertEquals("entropy_keeper", BossKind.ENTROPY_KEEPER.configKey());
        assertEquals("temporal_phantom", BossKind.TEMPORAL_PHANTOM.configKey());
        assertEquals("time_tyrant", BossKind.TIME_TYRANT.configKey());
    }
}
