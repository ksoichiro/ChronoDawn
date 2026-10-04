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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the version-specific portal implementations against reverting to
 * player-only travel or reintroducing the player's wait time for mobs.
 */
public class PortalEntityTravelConsistencyTest {

    private static final String[] VERSION_DIRS = {
        "1.20.1", "1.21.1", "1.21.2", "1.21.4", "1.21.5",
        "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
        "26.1.2", "26.2", "26.3"
    };

    private static final Set<String> LEGACY_TELEPORT_SIGNATURES = Set.of("1.20.1", "1.21.1");

    @TestFactory
    Collection<DynamicTest> everyVersionSupportsNonPlayerPortalTravel() {
        Path projectRoot = Path.of(TestUtils.getProjectRoot());
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String version : VERSION_DIRS) {
            tests.add(DynamicTest.dynamicTest("portal_entity_travel_" + version, () -> {
                String handler = read(projectRoot, version, "core/portal/PortalTeleportHandler.java");
                String block = read(projectRoot, version, "blocks/ChronoDawnPortalBlock.java");

                assertFalse(handler.contains("Only players can teleport"),
                    version + " must not reject every non-player entity");
                assertTrue(handler.contains("boolean teleported = entity.teleportTo("),
                    version + " must use Entity.teleportTo for cross-dimension data transfer");
                assertTrue(handler.contains("if (player == null"),
                    version + " must protect the one-way progression portal from non-player entities");
                assertTrue(block.contains("private static final int NON_PLAYER_PORTAL_TIME_THRESHOLD = 1;"),
                    version + " must use Nether-like entry timing for non-player entities");

                String expectedTail = LEGACY_TELEPORT_SIGNATURES.contains(version)
                    ? "Set.of(), destYRot, entity.getXRot());"
                    : "Set.of(), destYRot, entity.getXRot(), false);";
                assertTrue(handler.contains(expectedTail),
                    version + " must use the correct Entity.teleportTo signature");
            }));
        }

        return tests;
    }

    private static String read(Path projectRoot, String version, String relativePath) throws IOException {
        Path file = projectRoot.resolve("common").resolve(version)
            .resolve("src/main/java/com/chronodawn").resolve(relativePath);
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
