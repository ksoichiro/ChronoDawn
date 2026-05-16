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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads {@code chronodawn.toml} from the loader-provided config directory.
 *
 * <p>If the file is missing, the bundled commented default is materialised
 * to disk on first run. Invalid values fall back to defaults per-field
 * (the rest of the config still loads). Unknown keys are logged but
 * otherwise ignored.
 */
public final class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String CONFIG_FILE_NAME = "chronodawn.toml";
    private static final String BUNDLED_DEFAULT_RESOURCE = "/chronodawn-default-config.toml";

    // Vanilla limits for RandomSpreadStructurePlacement
    private static final int MIN_SPACING = 1;
    private static final int MAX_SPACING = 4096;
    private static final int MIN_SEPARATION = 0;

    // Top-level / nested keys recognised by this build.
    // Used to distinguish "ignored unknown key" from "missing optional key".
    private static final String K_SCHEMA_VERSION = "schema_version";
    private static final String K_WORLD = "world";
    private static final String K_STRUCTURES = "structures";
    private static final String K_ANCIENT_RUINS = "ancient_ruins";
    private static final String K_AR_ENABLED = "enabled";
    private static final String K_AR_SPACING = "spacing";
    private static final String K_AR_SEPARATION = "separation";
    private static final String K_AR_SALT = "salt";

    private ConfigLoader() {}

    /**
     * Read {@code <configDir>/chronodawn.toml}, creating it from the bundled
     * default if missing. Returns the parsed (and validated) config.
     *
     * <p>The result is also stored as {@link ChronoDawnConfig#get()}.
     */
    public static ChronoDawnConfig load(Path configDir) {
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);
        try {
            ensureFileExists(configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to materialise default config at {}; using built-in defaults", configFile, e);
            ChronoDawnConfig defaults = ConfigDefaults.defaults();
            ChronoDawnConfig.set(defaults);
            return defaults;
        }

        ChronoDawnConfig config = parseOrDefaults(configFile);
        ChronoDawnConfig.set(config);
        return config;
    }

    private static void ensureFileExists(Path configFile) throws IOException {
        if (Files.exists(configFile)) return;
        Files.createDirectories(configFile.getParent());
        try (InputStream in = ConfigLoader.class.getResourceAsStream(BUNDLED_DEFAULT_RESOURCE)) {
            if (in == null) {
                throw new IOException("Bundled default config resource not found: " + BUNDLED_DEFAULT_RESOURCE);
            }
            Files.copy(in, configFile, StandardCopyOption.REPLACE_EXISTING);
        }
        LOGGER.info("Wrote default config to {}", configFile);
    }

    private static ChronoDawnConfig parseOrDefaults(Path configFile) {
        CommentedConfig parsed;
        try (InputStream in = Files.newInputStream(configFile)) {
            parsed = new TomlParser().parse(in);
        } catch (IOException e) {
            LOGGER.error("Failed to read {}; using built-in defaults", configFile, e);
            return ConfigDefaults.defaults();
        } catch (RuntimeException e) {
            // night-config throws ParsingException (RuntimeException) on malformed TOML
            LOGGER.error("Failed to parse {} (is the TOML well-formed?); using built-in defaults", configFile, e);
            return ConfigDefaults.defaults();
        }

        int schemaVersion = parsed.<Number>getOptional(K_SCHEMA_VERSION)
            .map(Number::intValue)
            .orElse(ChronoDawnConfig.CURRENT_SCHEMA_VERSION);
        if (schemaVersion > ChronoDawnConfig.CURRENT_SCHEMA_VERSION) {
            LOGGER.warn(
                "{} declares schema_version={} but this build only knows up to {}; reading what we can",
                CONFIG_FILE_NAME, schemaVersion, ChronoDawnConfig.CURRENT_SCHEMA_VERSION
            );
        }

        ChronoDawnConfig.AncientRuins ancientRuins = parseAncientRuins(parsed);

        // Surface unknown top-level keys at WARN. Nested-table walking would be nice but
        // would balloon this method; the most common mistake is misspelling at top level.
        for (CommentedConfig.Entry entry : parsed.entrySet()) {
            String key = entry.getKey();
            if (!key.equals(K_SCHEMA_VERSION) && !key.equals(K_WORLD)) {
                LOGGER.warn("Unknown top-level key in {}: {}", CONFIG_FILE_NAME, key);
            }
        }

        return new ChronoDawnConfig(
            schemaVersion,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(ancientRuins),
                ConfigDefaults.defaults().world().ores()
            )
        );
    }

    private static ChronoDawnConfig.AncientRuins parseAncientRuins(CommentedConfig parsed) {
        String path = K_WORLD + "." + K_STRUCTURES + "." + K_ANCIENT_RUINS;

        boolean enabled = parsed.<Boolean>getOptional(path + "." + K_AR_ENABLED)
            .orElse(ConfigDefaults.ANCIENT_RUINS_ENABLED);

        int spacing = parsed.<Number>getOptional(path + "." + K_AR_SPACING)
            .map(Number::intValue)
            .orElse(ConfigDefaults.ANCIENT_RUINS_SPACING);

        int separation = parsed.<Number>getOptional(path + "." + K_AR_SEPARATION)
            .map(Number::intValue)
            .orElse(ConfigDefaults.ANCIENT_RUINS_SEPARATION);

        int salt = parsed.<Number>getOptional(path + "." + K_AR_SALT)
            .map(Number::intValue)
            .orElse(ConfigDefaults.ANCIENT_RUINS_SALT);

        // Validation: spacing must be in vanilla range, separation must be in [0, spacing).
        // Each field is validated independently so one bad value doesn't reset the others.
        if (spacing < MIN_SPACING || spacing > MAX_SPACING) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_AR_SPACING, spacing, MIN_SPACING, MAX_SPACING, ConfigDefaults.ANCIENT_RUINS_SPACING
            );
            spacing = ConfigDefaults.ANCIENT_RUINS_SPACING;
        }
        if (separation < MIN_SEPARATION || separation >= spacing) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, spacing={})); using default {}",
                path, K_AR_SEPARATION, separation, MIN_SEPARATION, spacing, ConfigDefaults.ANCIENT_RUINS_SEPARATION
            );
            separation = ConfigDefaults.ANCIENT_RUINS_SEPARATION;
            // If even the default exceeds the (now-validated) spacing, fall back to the safer half-spacing rule.
            if (separation >= spacing) {
                separation = Math.max(0, spacing - 1);
            }
        }

        return new ChronoDawnConfig.AncientRuins(enabled, spacing, separation, salt);
    }
}
