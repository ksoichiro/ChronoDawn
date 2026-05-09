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

import com.chronodawn.config.ChronoDawnConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Materialises the runtime overlay datapack on disk, under a directory rooted
 * at the loader-provided config dir. Loader-specific code then registers a
 * {@code PathPackResources} pointing at this directory.
 *
 * <p>Called once at mod startup after {@link com.chronodawn.config.ConfigLoader}
 * has loaded the config. Files are overwritten on every run, so config changes
 * take effect on next server start.
 */
public final class OverlayPackBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(OverlayPackBootstrap.class);

    public static final String OVERLAY_DIR_NAME = "chronodawn-runtime-overlay";

    /** Pack metadata content written at the root of the overlay directory. */
    private static final byte[] PACK_MCMETA_BYTES = (
        "{\n" +
        "  \"pack\": {\n" +
        "    \"description\": \"ChronoDawn runtime config overlay\",\n" +
        "    \"pack_format\": 41\n" +
        "  }\n" +
        "}\n"
    ).getBytes(java.nio.charset.StandardCharsets.UTF_8);

    private OverlayPackBootstrap() {}

    /**
     * Write the overlay datapack to disk and return its root path.
     *
     * @param configDir loader-provided config directory
     * @param config    parsed config
     * @return root path of the overlay datapack (suitable for {@code PathPackResources})
     */
    public static Path writeOverlay(Path configDir, ChronoDawnConfig config) {
        Path overlayRoot = configDir.resolve(OVERLAY_DIR_NAME);
        try {
            Files.createDirectories(overlayRoot);
            // pack.mcmeta at the overlay root
            Files.write(overlayRoot.resolve("pack.mcmeta"), PACK_MCMETA_BYTES);
            // Generated content
            Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(config);
            for (Map.Entry<String, byte[]> entry : overlay.entrySet()) {
                Path target = overlayRoot.resolve(entry.getKey());
                Files.createDirectories(target.getParent());
                Files.write(target, entry.getValue());
            }
            LOGGER.info("Wrote runtime config overlay datapack to {}", overlayRoot);
        } catch (IOException e) {
            LOGGER.error("Failed to write runtime config overlay to {}; config-driven worldgen will be inactive", overlayRoot, e);
        }
        return overlayRoot;
    }
}
