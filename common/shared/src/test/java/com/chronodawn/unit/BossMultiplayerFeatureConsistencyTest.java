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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates that every boss entity's multiplayer-aggro behavior (immediate
 * target switch, and — where applicable — the {@code BossMultiplayer}
 * cooldown/summon gating) is present in every supported version module.
 * Guards against the failure mode this feature's own implementation risked:
 * six boss classes duplicated across thirteen version directories, with no
 * shared code to enforce that an edit to one module also lands in the rest.
 */
public class BossMultiplayerFeatureConsistencyTest {

    private static final String[] VERSION_DIRS = {
        "1.20.1", "1.21.1", "1.21.2", "1.21.4", "1.21.5",
        "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
        "26.1.2", "26.2"
    };

    // Boss entity file name -> whether it also uses BossMultiplayer.isMultiplayerEncounter
    private static final String[][] BOSSES = {
        {"TimeGuardianEntity.java", "true"},
        {"ChronosWardenEntity.java", "true"},
        {"ClockworkColossusEntity.java", "false"},
        {"EntropyKeeperEntity.java", "true"},
        {"TemporalPhantomEntity.java", "false"},
        {"TimeTyrantEntity.java", "true"},
    };

    private static final String OLD_ERA_ENTRY_POINT_SIGNATURE =
        "public boolean hurt(DamageSource source, float amount)";
    private static final String NEW_ERA_ENTRY_POINT_SIGNATURE =
        "public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount)";
    private static final java.util.Set<String> OLD_ERA_VERSIONS =
        java.util.Set.of("1.20.1", "1.21.1");
    private static final String TARGET_SWITCH_BODY =
        "this.setLastHurtByMob(player);";
    private static final String MULTIPLAYER_GATE_SIGNATURE =
        "BossMultiplayer.isMultiplayerEncounter(";

    @TestFactory
    Collection<DynamicTest> bossMultiplayerFeaturePresentInEveryVersion() {
        String projectRoot = TestUtils.getProjectRoot();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String[] boss : BOSSES) {
            String fileName = boss[0];
            boolean expectMultiplayerGate = Boolean.parseBoolean(boss[1]);
            String bossName = fileName.replace(".java", "");

            for (String version : VERSION_DIRS) {
                Path filePath = Paths.get(
                    projectRoot, "common", version, "src", "main", "java",
                    "com", "chronodawn", "entities", "bosses", fileName
                );

                tests.add(DynamicTest.dynamicTest(
                    "boss_multiplayer_" + bossName + "_" + version,
                    () -> {
                        if (!Files.isRegularFile(filePath)) {
                            fail(fileName + " not found for version " + version + " at " + filePath);
                            return;
                        }
                        String content = Files.readString(filePath, StandardCharsets.UTF_8);

                        boolean isOldEra = OLD_ERA_VERSIONS.contains(version);
                        String expectedEntryPoint = isOldEra
                            ? OLD_ERA_ENTRY_POINT_SIGNATURE
                            : NEW_ERA_ENTRY_POINT_SIGNATURE;

                        assertTrue(
                            content.contains(expectedEntryPoint),
                            fileName + " (" + version + ") is missing the "
                                + (isOldEra ? "hurt(...)" : "hurtServer(...)")
                                + " damage-entry-point override"
                        );
                        assertTrue(
                            content.contains(TARGET_SWITCH_BODY),
                            fileName + " (" + version + ") is missing the setLastHurtByMob(player) "
                                + "call inside its damage-entry-point override "
                                + "(immediate target-switch)"
                        );

                        if (expectMultiplayerGate) {
                            assertTrue(
                                content.contains(MULTIPLAYER_GATE_SIGNATURE),
                                fileName + " (" + version + ") is missing a "
                                    + "BossMultiplayer.isMultiplayerEncounter(...) call site"
                            );
                        }
                    }
                ));
            }
        }

        return tests;
    }
}
