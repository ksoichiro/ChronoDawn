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
package com.chronodawn.worldgen.runtime;

import com.chronodawn.config.ConfigDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OverlayPackBootstrap}, the on-disk materialiser of the
 * runtime overlay datapack. Loader-side pack registration depends on
 * {@code overlayPath} being non-null only after a successful write, so the
 * three regressions guarded here are:
 *
 * <ol>
 *   <li>A successful run writes {@code pack.mcmeta} plus every byte payload
 *       returned by {@link RuntimeStructureOverlay#generate} and
 *       {@link RuntimePlacedFeatureOverlay#generate} to the expected paths.</li>
 *   <li>Calling {@code writeOverlay} twice with the same args is idempotent
 *       — same file set, same bytes, same {@code overlayPath} — so a server
 *       restart never observes a partial or drifted overlay.</li>
 *   <li>If {@code writeOverlay} fails (here: target dir collision with a
 *       regular file), {@code overlayPath} remains {@code null} so the
 *       Fabric Mixin / NeoForge {@code OverlayPackFinder} skip registration
 *       instead of pointing at a half-written directory.</li>
 * </ol>
 */
class OverlayPackBootstrapTest {

    /**
     * {@link OverlayPackBootstrap#overlayPath} is a static field that survives
     * across tests; reset to {@code null} before each test so failure-path
     * assertions are not contaminated by a previous successful run.
     */
    @BeforeEach
    void resetOverlayPath() throws ReflectiveOperationException {
        Field f = OverlayPackBootstrap.class.getDeclaredField("overlayPath");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    void writeOverlay_writesPackMcmetaAndAllGeneratedJsons(@TempDir Path configDir) throws IOException {
        OverlayPackBootstrap.writeOverlay(configDir, ConfigDefaults.defaults());

        Path overlayRoot = configDir.resolve(OverlayPackBootstrap.OVERLAY_DIR_NAME);
        assertEquals(overlayRoot, OverlayPackBootstrap.getOverlayPath(),
            "overlayPath must be published on success");
        assertTrue(Files.isRegularFile(overlayRoot.resolve("pack.mcmeta")),
            "pack.mcmeta must be written at the overlay root");

        // The overlay generators are the source of truth for which files must exist.
        // Re-generating here and comparing bytes guards against the bootstrap dropping
        // entries or writing them to the wrong relative path.
        Map<String, byte[]> expected = new LinkedHashMap<>();
        expected.putAll(RuntimeStructureOverlay.generate(ConfigDefaults.defaults()));
        expected.putAll(RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults()));
        for (Map.Entry<String, byte[]> entry : expected.entrySet()) {
            Path file = overlayRoot.resolve(entry.getKey());
            assertTrue(Files.isRegularFile(file),
                "Expected overlay file not written: " + entry.getKey());
            assertArrayEquals(entry.getValue(), Files.readAllBytes(file),
                "Bytes on disk diverge from generator output for: " + entry.getKey());
        }
    }

    @Test
    void writeOverlay_isIdempotentAcrossRepeatedCalls(@TempDir Path configDir) throws IOException {
        OverlayPackBootstrap.writeOverlay(configDir, ConfigDefaults.defaults());
        Map<Path, byte[]> first = snapshotRegularFiles(configDir);
        Path firstPath = OverlayPackBootstrap.getOverlayPath();

        OverlayPackBootstrap.writeOverlay(configDir, ConfigDefaults.defaults());
        Map<Path, byte[]> second = snapshotRegularFiles(configDir);

        assertEquals(first.keySet(), second.keySet(),
            "File set must be identical across writeOverlay invocations");
        for (Map.Entry<Path, byte[]> e : first.entrySet()) {
            assertArrayEquals(e.getValue(), second.get(e.getKey()),
                "Content drift across writeOverlay invocations at: " + e.getKey());
        }
        assertEquals(firstPath, OverlayPackBootstrap.getOverlayPath(),
            "Published overlayPath must be stable across repeated successful writes");
    }

    @Test
    void writeOverlay_ioFailure_leavesOverlayPathNull(@TempDir Path configDir) throws IOException {
        // Pre-occupy the overlay directory slot with a regular file so
        // Files.createDirectories(overlayRoot) throws FileAlreadyExistsException
        // — a deterministic, cross-platform way to drive writeOverlay's catch block.
        Path overlayRoot = configDir.resolve(OverlayPackBootstrap.OVERLAY_DIR_NAME);
        Files.writeString(overlayRoot, "regular-file-blocker");

        OverlayPackBootstrap.writeOverlay(configDir, ConfigDefaults.defaults());

        assertNull(OverlayPackBootstrap.getOverlayPath(),
            "overlayPath must remain null when writeOverlay fails — otherwise "
            + "loader-side registration would point at a half-written directory");
    }

    private static Map<Path, byte[]> snapshotRegularFiles(Path root) throws IOException {
        Map<Path, byte[]> out = new LinkedHashMap<>();
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path p : (Iterable<Path>) stream.filter(Files::isRegularFile)::iterator) {
                out.put(root.relativize(p), Files.readAllBytes(p));
            }
        }
        return out;
    }
}
