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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates that every boss entity overrides both despawn checks in every
 * supported version module. Mob despawns far-away monsters by default, so a
 * boss missing either override can vanish when players leave its room.
 * The overrides were once added to the 1.21.1 module only, leaving the other
 * thirteen versions with bosses that could despawn.
 */
public class BossDespawnPreventionConsistencyTest {

    private static final String[] VERSION_DIRS = {
        "1.20.1", "1.21.1", "1.21.2", "1.21.4", "1.21.5",
        "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
        "26.1.2", "26.2", "26.3"
    };

    private static final String[] BOSSES = {
        "TimeGuardianEntity.java",
        "ChronosWardenEntity.java",
        "ClockworkColossusEntity.java",
        "EntropyKeeperEntity.java",
        "TemporalPhantomEntity.java",
        "TimeTyrantEntity.java",
    };

    // Match the return value too, so reverting an override to the despawning
    // value fails the test instead of passing on the signature alone. Line
    // comments may sit between the opening brace and the return.
    private static final Pattern PERSISTENCE_OVERRIDE = Pattern.compile(
        "public boolean isPersistenceRequired\\(\\)\\s*\\{\\s*(?://[^\\n]*\\s*)*return true;");
    private static final Pattern FAR_AWAY_OVERRIDE = Pattern.compile(
        "public boolean removeWhenFarAway\\(double distanceToClosestPlayer\\)\\s*\\{\\s*(?://[^\\n]*\\s*)*return false;");

    @TestFactory
    Collection<DynamicTest> bossDespawnPreventionPresentInEveryVersion() {
        String projectRoot = TestUtils.getProjectRoot();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String fileName : BOSSES) {
            String bossName = fileName.replace(".java", "");

            for (String version : VERSION_DIRS) {
                Path filePath = Paths.get(
                    projectRoot, "common", version, "src", "main", "java",
                    "com", "chronodawn", "entities", "bosses", fileName
                );

                tests.add(DynamicTest.dynamicTest(
                    "boss_despawn_" + bossName + "_" + version,
                    () -> {
                        if (!Files.isRegularFile(filePath)) {
                            fail(fileName + " not found for version " + version + " at " + filePath);
                            return;
                        }
                        String content = Files.readString(filePath, StandardCharsets.UTF_8);

                        assertTrue(
                            PERSISTENCE_OVERRIDE.matcher(content).find(),
                            fileName + " (" + version + ") is missing an isPersistenceRequired() override "
                                + "that returns true"
                        );
                        assertTrue(
                            FAR_AWAY_OVERRIDE.matcher(content).find(),
                            fileName + " (" + version + ") is missing a removeWhenFarAway(...) override "
                                + "that returns false"
                        );
                    }
                ));
            }
        }

        return tests;
    }
}
