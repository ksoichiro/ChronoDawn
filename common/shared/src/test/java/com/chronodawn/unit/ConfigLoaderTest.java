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

import com.chronodawn.config.BossesConfig;
import com.chronodawn.config.BossSettings;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ConfigLoader;
import com.chronodawn.config.ManagedStructure;
import com.chronodawn.config.PortalSettings;
import com.chronodawn.config.StructureSettings;
import com.chronodawn.config.TimeDistortionSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ConfigLoader} covering the per-spec validation rules:
 * missing file, partial file, invalid values, unknown keys, and schema_version
 * handling. Each invalid field falls back to its default independently of the
 * others; the rest of the config keeps loading.
 */
class ConfigLoaderTest {

    @Test
    void missingFile_writesBundledDefaultAndReturnsDefaults(@TempDir Path tmp) throws IOException {
        ChronoDawnConfig config = ConfigLoader.load(tmp);

        // Bundled default file was materialised on disk
        assertTrue(Files.exists(tmp.resolve("chronodawn.toml")),
            "Default config file should be written when missing");

        // Returned config matches built-in defaults
        assertEquals(ConfigDefaults.defaults(), config);
    }

    @Test
    void partialFile_unspecifiedFieldsFallBackToDefaults(@TempDir Path tmp) throws IOException {
        // Only override spacing; everything else (enabled, separation, salt,
        // schema_version) should fall back to defaults.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 32\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        StructureSettings ar = config.world().structures().ancientRuins();

        assertEquals(32, ar.spacing());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.enabled(), ar.enabled());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.separation(), ar.separation());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.salt(), ar.salt());
        assertEquals(ChronoDawnConfig.CURRENT_SCHEMA_VERSION, config.schemaVersion());
    }

    @Test
    void invalidSpacing_zero_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 0\n" +
            "separation = 5\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        // spacing=0 fails validation → default 56; separation=5 is still valid (< 56) → kept
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.spacing(), config.world().structures().ancientRuins().spacing());
        assertEquals(5, config.world().structures().ancientRuins().separation());
    }

    @Test
    void invalidSpacing_negative_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = -10\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.spacing(), config.world().structures().ancientRuins().spacing());
    }

    @Test
    void invalidSpacing_overMax_revertsToDefault(@TempDir Path tmp) throws IOException {
        // Vanilla RandomSpreadStructurePlacement caps at 4096
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 5000\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.spacing(), config.world().structures().ancientRuins().spacing());
    }

    @Test
    void invalidSeparation_geSpacing_revertsToDefault(@TempDir Path tmp) throws IOException {
        // separation must be < spacing; here separation > spacing → revert separation only
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 30\n" +
            "separation = 40\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        StructureSettings ar = config.world().structures().ancientRuins();
        assertEquals(30, ar.spacing(), "Valid spacing should be kept");
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.separation(), ar.separation(), "Invalid separation should fall back");
    }

    @Test
    void invalidSeparation_negative_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "separation = -5\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.separation(), config.world().structures().ancientRuins().separation());
    }

    @Test
    void unknownTopLevelKey_isIgnored(@TempDir Path tmp) throws IOException {
        // Unknown key should not break parsing; valid fields still load.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "schema_version = 1\n" +
            "unknown_key = \"foo\"\n" +
            "[world.structures.ancient_ruins]\n" +
            "spacing = 100\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(100, config.world().structures().ancientRuins().spacing());
    }

    @Test
    void schemaVersionNewer_partialParseReturned(@TempDir Path tmp) throws IOException {
        // Newer schema version → log warn, but read what we recognize
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "schema_version = 99\n" +
            "[world.structures.ancient_ruins]\n" +
            "spacing = 24\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(99, config.schemaVersion());
        assertEquals(24, config.world().structures().ancientRuins().spacing());
    }

    @Test
    void malformedToml_fallsBackToDefaults(@TempDir Path tmp) throws IOException {
        // Garbage TOML → ParsingException → fall back to all defaults
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "this is not [valid TOML\n" +
            "===\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.defaults(), config);
    }

    @Test
    void validCustomConfig_isReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "schema_version = 1\n" +
            "[world.structures.ancient_ruins]\n" +
            "enabled = false\n" +
            "spacing = 16\n" +
            "separation = 4\n" +
            "salt = 12345\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        StructureSettings ar = config.world().structures().ancientRuins();
        assertEquals(false, ar.enabled());
        assertEquals(16, ar.spacing());
        assertEquals(4, ar.separation());
        assertEquals(12345, ar.salt());
    }

    @Test
    void load_publishesActiveInstance(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 99\n");

        ChronoDawnConfig returned = ConfigLoader.load(tmp);
        assertEquals(returned, ChronoDawnConfig.get(),
            "ConfigLoader.load() must publish the result via ChronoDawnConfig.get()");
    }

    @Test
    void oresDefault_allFieldsMatchDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OresConfig ores = config.world().ores();
        assertEquals(ConfigDefaults.TIME_CRYSTAL_DEFAULTS, ores.timeCrystal());
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, ores.entropyCrystal());
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS, ores.temporalAmber());
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS, ores.clockstone());
    }

    @Test
    void validOreCustom_isReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.time_crystal]\n" +
            "enabled = false\n" +
            "count = 7\n" +
            "y_min = -10\n" +
            "y_max = 20\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings tc = config.world().ores().timeCrystal();
        assertEquals(false, tc.enabled());
        assertEquals(7, tc.count());
        assertEquals(-10, tc.yMin());
        assertEquals(20, tc.yMax());
        // Other ores are untouched
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, config.world().ores().entropyCrystal());
    }

    @Test
    void validClockstoneCustom_isReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.clockstone]\n" +
            "enabled = false\n" +
            "count = 12\n" +
            "y_min = -8\n" +
            "y_max = 64\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings clk = config.world().ores().clockstone();
        assertEquals(false, clk.enabled());
        assertEquals(12, clk.count());
        assertEquals(-8, clk.yMin());
        assertEquals(64, clk.yMax());
        // Other ores untouched
        assertEquals(ConfigDefaults.TIME_CRYSTAL_DEFAULTS, config.world().ores().timeCrystal());
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, config.world().ores().entropyCrystal());
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS, config.world().ores().temporalAmber());
    }

    @Test
    void invalidClockstoneCount_overMax_revertsCount(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.clockstone]\n" +
            "count = 999\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS.count(),
            config.world().ores().clockstone().count());
    }

    @Test
    void clockstoneInvertedYRange_revertsBothYFieldsOnly(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.clockstone]\n" +
            "count = 11\n" +
            "y_min = 100\n" +
            "y_max = 50\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings clk = config.world().ores().clockstone();
        assertEquals(11, clk.count(), "Valid count survives an inverted-Y revert");
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS.yMin(), clk.yMin());
        assertEquals(ConfigDefaults.CLOCKSTONE_DEFAULTS.yMax(), clk.yMax());
    }

    @Test
    void invalidOreCount_negative_revertsCountOnly(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.time_crystal]\n" +
            "count = -5\n" +
            "y_min = 5\n" +
            "y_max = 40\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings tc = config.world().ores().timeCrystal();
        assertEquals(ConfigDefaults.TIME_CRYSTAL_DEFAULTS.count(), tc.count(),
            "Invalid count reverts to default");
        assertEquals(5, tc.yMin(), "Valid yMin survives a sibling field's revert");
        assertEquals(40, tc.yMax(), "Valid yMax survives a sibling field's revert");
    }

    @Test
    void invalidOreCount_overMax_revertsCount(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.entropy_crystal]\n" +
            "count = 100\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS.count(),
            config.world().ores().entropyCrystal().count());
    }

    @Test
    void invertedYRange_revertsBothYFieldsOnly(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.entropy_crystal]\n" +
            "count = 6\n" +
            "y_min = 50\n" +
            "y_max = 40\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings ec = config.world().ores().entropyCrystal();
        assertEquals(6, ec.count(), "Valid count survives an inverted-Y revert");
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS.yMin(), ec.yMin());
        assertEquals(ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS.yMax(), ec.yMax());
    }

    @Test
    void yOutOfRange_belowMin_revertsBothY(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.temporal_amber]\n" +
            "y_min = -100\n" +
            "y_max = 10\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        com.chronodawn.config.OreSettings ta = config.world().ores().temporalAmber();
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS.yMin(), ta.yMin());
        assertEquals(ConfigDefaults.TEMPORAL_AMBER_DEFAULTS.yMax(), ta.yMax());
    }

    @Test
    void unknownOreSection_isIgnored(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.ores.nonexistent]\n" +
            "count = 5\n" +
            "[world.ores.time_crystal]\n" +
            "count = 9\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(9, config.world().ores().timeCrystal().count(),
            "Known ore section still loads despite an unknown sibling");
    }

    @Test
    void bosses_missingSection_fallsBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 32\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().timeGuardian());
        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().timeTyrant());
    }

    @Test
    void bosses_allSixParse(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "health_multiplier = 2.0\n" +
            "damage_multiplier = 0.5\n" +
            "[gameplay.bosses.chronos_warden]\n" +
            "health_multiplier = 1.5\n" +
            "[gameplay.bosses.clockwork_colossus]\n" +
            "damage_multiplier = 3.0\n" +
            "[gameplay.bosses.entropy_keeper]\n" +
            "health_multiplier = 0.5\n" +
            "[gameplay.bosses.temporal_phantom]\n" +
            "damage_multiplier = 0.0\n" +
            "[gameplay.bosses.time_tyrant]\n" +
            "health_multiplier = 10.0\n" +
            "damage_multiplier = 10.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        BossesConfig bosses = config.gameplay().bosses();

        assertEquals(new BossSettings(2.0, 0.5), bosses.timeGuardian());
        // Unspecified field within a present table still falls back
        assertEquals(new BossSettings(1.5, 1.0), bosses.chronosWarden());
        assertEquals(new BossSettings(1.0, 3.0), bosses.clockworkColossus());
        assertEquals(new BossSettings(0.5, 1.0), bosses.entropyKeeper());
        assertEquals(new BossSettings(1.0, 0.0), bosses.temporalPhantom());
        assertEquals(new BossSettings(10.0, 10.0), bosses.timeTyrant());
    }

    @Test
    void bosses_healthMultiplierZero_revertsToDefault(@TempDir Path tmp) throws IOException {
        // Zero health would mean a max health of 0 — rejected. damage_multiplier
        // on the same table is valid and must survive independently.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "health_multiplier = 0.0\n" +
            "damage_multiplier = 2.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS.healthMultiplier(),
            config.gameplay().bosses().timeGuardian().healthMultiplier());
        assertEquals(2.0, config.gameplay().bosses().timeGuardian().damageMultiplier(),
            "A valid field must not be reset by an invalid sibling");
    }

    @Test
    void bosses_damageMultiplierZero_isAccepted(@TempDir Path tmp) throws IOException {
        // Zero damage is deliberately allowed: story-focused packs keep the
        // encounter as spectacle without lethality.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_tyrant]\n" +
            "damage_multiplier = 0.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(0.0, config.gameplay().bosses().timeTyrant().damageMultiplier());
    }

    @Test
    void bosses_multiplierOverMax_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_tyrant]\n" +
            "health_multiplier = 11.0\n" +
            "damage_multiplier = 50.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().timeTyrant());
    }

    @Test
    void bosses_multiplierNegative_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.entropy_keeper]\n" +
            "health_multiplier = -1.0\n" +
            "damage_multiplier = -0.5\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().entropyKeeper());
    }

    @Test
    void bosses_nonFiniteMultiplier_revertsToDefault(@TempDir Path tmp) throws IOException {
        // TOML spells these nan / inf; night-config parses them as Double.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.temporal_phantom]\n" +
            "health_multiplier = nan\n" +
            "damage_multiplier = inf\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().temporalPhantom());
    }

    @Test
    void bosses_integerLiteralIsAccepted(@TempDir Path tmp) throws IOException {
        // TOML distinguishes 2 (integer) from 2.0 (float); the loader reads via
        // Number so both must work.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.clockwork_colossus]\n" +
            "health_multiplier = 2\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(2.0, config.gameplay().bosses().clockworkColossus().healthMultiplier());
    }

    @Test
    void timeDistortion_missingSection_fallsBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.TIME_DISTORTION_DEFAULTS, config.gameplay().timeDistortion());
        assertEquals(3, config.gameplay().timeDistortion().slownessAmplifier(false));
        assertEquals(4, config.gameplay().timeDistortion().slownessAmplifier(true));
    }

    @Test
    void timeDistortion_validCustomValues_areReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.time_distortion]\n" +
            "enabled = false\n" +
            "normal_slowness_level = 2\n" +
            "enhanced_slowness_level = 3\n" +
            "scope = \"all_mobs\"\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(new TimeDistortionSettings(
            false, 2, 3, TimeDistortionSettings.Scope.ALL_MOBS
        ), config.gameplay().timeDistortion());
    }

    @Test
    void timeDistortion_invalidFields_fallBackIndependently(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.time_distortion]\n" +
            "normal_slowness_level = 0\n" +
            "enhanced_slowness_level = 2\n" +
            "scope = \"unknown\"\n");

        TimeDistortionSettings settings = ConfigLoader.load(tmp).gameplay().timeDistortion();

        assertEquals(ConfigDefaults.TIME_DISTORTION_DEFAULTS.normalSlownessLevel(), settings.normalSlownessLevel());
        assertEquals(2, settings.enhancedSlownessLevel());
        assertEquals(ConfigDefaults.TIME_DISTORTION_DEFAULTS.scope(), settings.scope());
    }

    @Test
    void portals_missingSection_fallsBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");

        assertEquals(ConfigDefaults.PORTAL_DEFAULTS, ConfigLoader.load(tmp).gameplay().portals());
    }

    @Test
    void portals_validCustomValues_areReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.portals]\n" +
            "one_way_until_stabilized = false\n" +
            "allow_reignition_before_stabilization = true\n");

        assertEquals(new PortalSettings(false, true), ConfigLoader.load(tmp).gameplay().portals());
    }

    @Test
    void structures_defaults_carryEveryShippedStructure() {
        ChronoDawnConfig.Structures structures = ConfigDefaults.defaults().world().structures();

        assertEquals(new StructureSettings(true, 56, 20, 20005897L), structures.ancientRuins());
        assertEquals(new StructureSettings(true, 30, 15, 8735421890L), structures.forgottenLibrary());
        assertEquals(new StructureSettings(true, 30, 10, 1663542342L), structures.desertClockTower());
        assertEquals(new StructureSettings(true, 48, 24, 928374651L), structures.guardianVault());
        assertEquals(new StructureSettings(true, 56, 28, 837465129L), structures.clockworkDepths());
        assertEquals(new StructureSettings(true, 20, 8, 745182936L), structures.phantomCatacombs());
        assertEquals(new StructureSettings(true, 50, 25, 738291456L), structures.entropyCrypt());
        assertEquals(new StructureSettings(true, 60, 20, 1234567890L), structures.masterClock());
    }

    @Test
    void structures_missingSections_fallBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");

        ChronoDawnConfig.Structures structures = ConfigLoader.load(tmp).world().structures();
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertEquals(structure.defaults(), structure.settingsOf(structures), structure.name());
        }
    }

    @Test
    void structures_customValues_areReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.master_clock]\n" +
            "enabled = false\n" +
            "spacing = 120\n" +
            "separation = 40\n" +
            "salt = 42\n");

        ChronoDawnConfig.Structures structures = ConfigLoader.load(tmp).world().structures();

        assertEquals(new StructureSettings(false, 120, 40, 42L), structures.masterClock());
        assertEquals(ConfigDefaults.ENTROPY_CRYPT_DEFAULTS, structures.entropyCrypt(),
            "One structure's section must not disturb another's");
    }

    @Test
    void structures_invalidSpacing_revertsOnlyThatKey(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.guardian_vault]\n" +
            "spacing = 0\n" +
            "separation = 5\n");

        StructureSettings settings = ConfigLoader.load(tmp).world().structures().guardianVault();

        assertEquals(ConfigDefaults.GUARDIAN_VAULT_DEFAULTS.spacing(), settings.spacing());
        assertEquals(5, settings.separation(), "A valid separation must survive an invalid spacing");
    }

    @Test
    void structures_separationAtOrAboveSpacing_revertsSeparation(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.phantom_catacombs]\n" +
            "spacing = 10\n" +
            "separation = 10\n");

        StructureSettings settings = ConfigLoader.load(tmp).world().structures().phantomCatacombs();

        assertEquals(10, settings.spacing());
        assertEquals(ConfigDefaults.PHANTOM_CATACOMBS_DEFAULTS.separation(), settings.separation());
    }
}
