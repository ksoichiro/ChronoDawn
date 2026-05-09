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
import com.chronodawn.config.ConfigLoader;
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
        ChronoDawnConfig.AncientRuins ar = config.world().structures().ancientRuins();

        assertEquals(32, ar.spacing());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_ENABLED, ar.enabled());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_SEPARATION, ar.separation());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_SALT, ar.salt());
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
        assertEquals(ConfigDefaults.ANCIENT_RUINS_SPACING, config.world().structures().ancientRuins().spacing());
        assertEquals(5, config.world().structures().ancientRuins().separation());
    }

    @Test
    void invalidSpacing_negative_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = -10\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ANCIENT_RUINS_SPACING, config.world().structures().ancientRuins().spacing());
    }

    @Test
    void invalidSpacing_overMax_revertsToDefault(@TempDir Path tmp) throws IOException {
        // Vanilla RandomSpreadStructurePlacement caps at 4096
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 5000\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ANCIENT_RUINS_SPACING, config.world().structures().ancientRuins().spacing());
    }

    @Test
    void invalidSeparation_geSpacing_revertsToDefault(@TempDir Path tmp) throws IOException {
        // separation must be < spacing; here separation > spacing → revert separation only
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 30\n" +
            "separation = 40\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        ChronoDawnConfig.AncientRuins ar = config.world().structures().ancientRuins();
        assertEquals(30, ar.spacing(), "Valid spacing should be kept");
        assertEquals(ConfigDefaults.ANCIENT_RUINS_SEPARATION, ar.separation(), "Invalid separation should fall back");
    }

    @Test
    void invalidSeparation_negative_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "separation = -5\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.ANCIENT_RUINS_SEPARATION, config.world().structures().ancientRuins().separation());
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
        ChronoDawnConfig.AncientRuins ar = config.world().structures().ancientRuins();
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
}
