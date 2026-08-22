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
import java.util.Arrays;

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
    private static final String K_STRUCTURE_ENABLED = "enabled";
    private static final String K_STRUCTURE_SPACING = "spacing";
    private static final String K_STRUCTURE_SEPARATION = "separation";
    private static final String K_STRUCTURE_SALT = "salt";

    private static final String K_ORES = "ores";
    private static final String K_TIME_CRYSTAL = "time_crystal";
    private static final String K_ENTROPY_CRYSTAL = "entropy_crystal";
    private static final String K_TEMPORAL_AMBER = "temporal_amber";
    private static final String K_CLOCKSTONE = "clockstone";
    private static final String K_ORE_ENABLED = "enabled";
    private static final String K_ORE_COUNT = "count";
    private static final String K_ORE_Y_MIN = "y_min";
    private static final String K_ORE_Y_MAX = "y_max";

    private static final int MIN_ORE_COUNT = 0;
    private static final int MAX_ORE_COUNT = 64;
    private static final int MIN_ORE_Y = -64;
    private static final int MAX_ORE_Y = 320;

    private static final String K_GAMEPLAY = "gameplay";
    private static final String K_TIME_DISTORTION = "time_distortion";
    private static final String K_TD_ENABLED = "enabled";
    private static final String K_TD_NORMAL_SLOWNESS_LEVEL = "normal_slowness_level";
    private static final String K_TD_ENHANCED_SLOWNESS_LEVEL = "enhanced_slowness_level";
    private static final String K_TD_SCOPE = "scope";
    private static final String K_PORTALS = "portals";
    private static final String K_PORTAL_ONE_WAY_UNTIL_STABILIZED = "one_way_until_stabilized";
    private static final String K_PORTAL_ALLOW_REIGNITION_BEFORE_STABILIZATION =
        "allow_reignition_before_stabilization";
    private static final String K_BOSSES = "bosses";
    private static final String K_HEALTH_MULTIPLIER = "health_multiplier";
    private static final String K_DAMAGE_MULTIPLIER = "damage_multiplier";

    // Health may not be zeroed: a max health of 0 kills the entity on spawn.
    // Damage may be zeroed: a harmless boss is a legitimate pack choice.
    private static final double MIN_HEALTH_MULTIPLIER = 0.1;
    private static final double MIN_DAMAGE_MULTIPLIER = 0.0;
    private static final double MAX_MULTIPLIER = 10.0;

    private static final int MIN_SLOWNESS_LEVEL = 1;
    private static final int MAX_SLOWNESS_LEVEL = 5;

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

        ChronoDawnConfig.Structures structures = parseStructures(parsed);
        com.chronodawn.config.OresConfig ores = parseOres(parsed);
        ChronoDawnConfig.Gameplay gameplay = parseGameplay(parsed);

        // Surface unknown top-level keys at WARN. Nested-table walking would be nice but
        // would balloon this method; the most common mistake is misspelling at top level.
        for (CommentedConfig.Entry entry : parsed.entrySet()) {
            String key = entry.getKey();
            if (!key.equals(K_SCHEMA_VERSION) && !key.equals(K_WORLD) && !key.equals(K_GAMEPLAY)) {
                LOGGER.warn("Unknown top-level key in {}: {}", CONFIG_FILE_NAME, key);
            }
        }

        return new ChronoDawnConfig(
            schemaVersion,
            new ChronoDawnConfig.World(
                structures,
                ores
            ),
            gameplay
        );
    }

    private static ChronoDawnConfig.Structures parseStructures(CommentedConfig parsed) {
        return new ChronoDawnConfig.Structures(
            parseStructure(parsed, ManagedStructure.ANCIENT_RUINS),
            parseStructure(parsed, ManagedStructure.FORGOTTEN_LIBRARY),
            parseStructure(parsed, ManagedStructure.DESERT_CLOCK_TOWER),
            parseStructure(parsed, ManagedStructure.GUARDIAN_VAULT),
            parseStructure(parsed, ManagedStructure.CLOCKWORK_DEPTHS),
            parseStructure(parsed, ManagedStructure.PHANTOM_CATACOMBS),
            parseStructure(parsed, ManagedStructure.ENTROPY_CRYPT),
            parseStructure(parsed, ManagedStructure.MASTER_CLOCK)
        );
    }

    private static StructureSettings parseStructure(CommentedConfig parsed, ManagedStructure structure) {
        String path = K_WORLD + "." + K_STRUCTURES + "." + structure.configKey();
        StructureSettings defaults = structure.defaults();

        boolean enabled = parsed.<Boolean>getOptional(path + "." + K_STRUCTURE_ENABLED)
            .orElse(defaults.enabled());

        int spacing = parsed.<Number>getOptional(path + "." + K_STRUCTURE_SPACING)
            .map(Number::intValue)
            .orElse(defaults.spacing());

        int separation = parsed.<Number>getOptional(path + "." + K_STRUCTURE_SEPARATION)
            .map(Number::intValue)
            .orElse(defaults.separation());

        long salt = parsed.<Number>getOptional(path + "." + K_STRUCTURE_SALT)
            .map(Number::longValue)
            .orElse(defaults.salt());

        // Validation: spacing must be in vanilla range, separation must be in [0, spacing).
        // Each field is validated independently so one bad value doesn't reset the others.
        if (spacing < MIN_SPACING || spacing > MAX_SPACING) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_STRUCTURE_SPACING, spacing, MIN_SPACING, MAX_SPACING, defaults.spacing()
            );
            spacing = defaults.spacing();
        }
        if (separation < MIN_SEPARATION || separation >= spacing) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, spacing={})); using default {}",
                path, K_STRUCTURE_SEPARATION, separation, MIN_SEPARATION, spacing, defaults.separation()
            );
            separation = defaults.separation();
            // If even the default exceeds the (now-validated) spacing, fall back to the safer half-spacing rule.
            if (separation >= spacing) {
                separation = Math.max(0, spacing - 1);
            }
        }

        return new StructureSettings(enabled, spacing, separation, salt);
    }

    private static com.chronodawn.config.OresConfig parseOres(CommentedConfig parsed) {
        return new com.chronodawn.config.OresConfig(
            parseOre(parsed, K_TIME_CRYSTAL, ConfigDefaults.TIME_CRYSTAL_DEFAULTS),
            parseOre(parsed, K_ENTROPY_CRYSTAL, ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS),
            parseOre(parsed, K_TEMPORAL_AMBER, ConfigDefaults.TEMPORAL_AMBER_DEFAULTS),
            parseOre(parsed, K_CLOCKSTONE, ConfigDefaults.CLOCKSTONE_DEFAULTS)
        );
    }

    private static com.chronodawn.config.OreSettings parseOre(
        CommentedConfig parsed, String oreKey, com.chronodawn.config.OreSettings defaults
    ) {
        String path = K_WORLD + "." + K_ORES + "." + oreKey;

        boolean enabled = parsed.<Boolean>getOptional(path + "." + K_ORE_ENABLED)
            .orElse(defaults.enabled());

        int count = parsed.<Number>getOptional(path + "." + K_ORE_COUNT)
            .map(Number::intValue)
            .orElse(defaults.count());

        int yMin = parsed.<Number>getOptional(path + "." + K_ORE_Y_MIN)
            .map(Number::intValue)
            .orElse(defaults.yMin());

        int yMax = parsed.<Number>getOptional(path + "." + K_ORE_Y_MAX)
            .map(Number::intValue)
            .orElse(defaults.yMax());

        // Validation: each field reverts independently so one bad value doesn't reset the others.
        if (count < MIN_ORE_COUNT || count > MAX_ORE_COUNT) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_ORE_COUNT, count, MIN_ORE_COUNT, MAX_ORE_COUNT, defaults.count()
            );
            count = defaults.count();
        }
        if (yMin < MIN_ORE_Y || yMax > MAX_ORE_Y || yMin > yMax) {
            LOGGER.error(
                "Invalid {}.{{y_min,y_max}} = ({}, {}) (must satisfy {} <= y_min <= y_max <= {}); using defaults ({}, {})",
                path, yMin, yMax, MIN_ORE_Y, MAX_ORE_Y, defaults.yMin(), defaults.yMax()
            );
            yMin = defaults.yMin();
            yMax = defaults.yMax();
        }

        return new com.chronodawn.config.OreSettings(enabled, count, yMin, yMax);
    }

    private static ChronoDawnConfig.Gameplay parseGameplay(CommentedConfig parsed) {
        return new ChronoDawnConfig.Gameplay(
            parseTimeDistortion(parsed),
            parsePortals(parsed),
            new BossesConfig(
                parseBoss(parsed, "time_guardian"),
                parseBoss(parsed, "chronos_warden"),
                parseBoss(parsed, "clockwork_colossus"),
                parseBoss(parsed, "entropy_keeper"),
                parseBoss(parsed, "temporal_phantom"),
                parseBoss(parsed, "time_tyrant")
            )
        );
    }

    private static TimeDistortionSettings parseTimeDistortion(CommentedConfig parsed) {
        String path = K_GAMEPLAY + "." + K_TIME_DISTORTION;
        TimeDistortionSettings defaults = ConfigDefaults.TIME_DISTORTION_DEFAULTS;

        boolean enabled = parsed.<Boolean>getOptional(path + "." + K_TD_ENABLED)
            .orElse(defaults.enabled());
        int normalLevel = parsed.<Number>getOptional(path + "." + K_TD_NORMAL_SLOWNESS_LEVEL)
            .map(Number::intValue)
            .orElse(defaults.normalSlownessLevel());
        int enhancedLevel = parsed.<Number>getOptional(path + "." + K_TD_ENHANCED_SLOWNESS_LEVEL)
            .map(Number::intValue)
            .orElse(defaults.enhancedSlownessLevel());
        String scopeValue = parsed.<String>getOptional(path + "." + K_TD_SCOPE)
            .orElse(defaults.scope().configValue());
        TimeDistortionSettings.Scope scope = TimeDistortionSettings.Scope.fromConfigValue(scopeValue)
            .orElse(defaults.scope());

        if (normalLevel < MIN_SLOWNESS_LEVEL || normalLevel > MAX_SLOWNESS_LEVEL) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_TD_NORMAL_SLOWNESS_LEVEL, normalLevel, MIN_SLOWNESS_LEVEL, MAX_SLOWNESS_LEVEL,
                defaults.normalSlownessLevel()
            );
            normalLevel = defaults.normalSlownessLevel();
        }
        if (enhancedLevel < MIN_SLOWNESS_LEVEL || enhancedLevel > MAX_SLOWNESS_LEVEL) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_TD_ENHANCED_SLOWNESS_LEVEL, enhancedLevel, MIN_SLOWNESS_LEVEL, MAX_SLOWNESS_LEVEL,
                defaults.enhancedSlownessLevel()
            );
            enhancedLevel = defaults.enhancedSlownessLevel();
        }
        if (TimeDistortionSettings.Scope.fromConfigValue(scopeValue).isEmpty()) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be one of {}); using default {}",
                path, K_TD_SCOPE, scopeValue,
                Arrays.toString(TimeDistortionSettings.Scope.values()), defaults.scope().configValue()
            );
        }

        return new TimeDistortionSettings(enabled, normalLevel, enhancedLevel, scope);
    }

    private static PortalSettings parsePortals(CommentedConfig parsed) {
        String path = K_GAMEPLAY + "." + K_PORTALS;
        PortalSettings defaults = ConfigDefaults.PORTAL_DEFAULTS;

        boolean oneWayUntilStabilized = parsed.<Boolean>getOptional(path + "." + K_PORTAL_ONE_WAY_UNTIL_STABILIZED)
            .orElse(defaults.oneWayUntilStabilized());
        boolean allowReignitionBeforeStabilization = parsed
            .<Boolean>getOptional(path + "." + K_PORTAL_ALLOW_REIGNITION_BEFORE_STABILIZATION)
            .orElse(defaults.allowReignitionBeforeStabilization());

        return new PortalSettings(oneWayUntilStabilized, allowReignitionBeforeStabilization);
    }

    private static BossSettings parseBoss(CommentedConfig parsed, String bossKey) {
        String path = K_GAMEPLAY + "." + K_BOSSES + "." + bossKey;
        BossSettings defaults = ConfigDefaults.BOSS_DEFAULTS;

        double health = parsed.<Number>getOptional(path + "." + K_HEALTH_MULTIPLIER)
            .map(Number::doubleValue)
            .orElse(defaults.healthMultiplier());

        double damage = parsed.<Number>getOptional(path + "." + K_DAMAGE_MULTIPLIER)
            .map(Number::doubleValue)
            .orElse(defaults.damageMultiplier());

        // Validation: each field reverts independently so one bad value doesn't
        // reset the other. isFinite also rejects nan / inf.
        if (!Double.isFinite(health) || health < MIN_HEALTH_MULTIPLIER || health > MAX_MULTIPLIER) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_HEALTH_MULTIPLIER, health, MIN_HEALTH_MULTIPLIER, MAX_MULTIPLIER,
                defaults.healthMultiplier()
            );
            health = defaults.healthMultiplier();
        }
        if (!Double.isFinite(damage) || damage < MIN_DAMAGE_MULTIPLIER || damage > MAX_MULTIPLIER) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_DAMAGE_MULTIPLIER, damage, MIN_DAMAGE_MULTIPLIER, MAX_MULTIPLIER,
                defaults.damageMultiplier()
            );
            damage = defaults.damageMultiplier();
        }

        return new BossSettings(health, damage);
    }
}
