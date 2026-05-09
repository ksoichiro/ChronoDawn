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
package com.chronodawn.neoforge.event;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.runtime.OverlayPackBootstrap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.Optional;

/**
 * NeoForge-side registration of the on-disk runtime overlay datapack as a
 * built-in pack source for the server pack repository.
 *
 * <p>{@link com.chronodawn.config.ConfigLoader} writes
 * {@code config/chronodawn-runtime-overlay/} during {@code ChronoDawn.init()};
 * here we expose that directory as a high-priority data pack so the JSONs
 * inside override the bundled mod resources.
 */
public final class OverlayPackFinder {
    public static final String PACK_DISPLAY_NAME = "ChronoDawn config overlay";

    private OverlayPackFinder() {}

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        Path overlayPath = OverlayPackBootstrap.getOverlayPath();
        if (overlayPath == null) {
            // Bootstrap failed to materialise the overlay (e.g. IO error). Skip
            // rather than register an empty pack source.
            return;
        }

        PackLocationInfo info = new PackLocationInfo(
            OverlayPackBootstrap.PACK_ID,
            Component.literal(PACK_DISPLAY_NAME),
            PackSource.BUILT_IN,
            Optional.empty()
        );
        // alwaysActive=true + Position.TOP: sits above bundled mod resources but
        // below user datapacks (vanilla applies user packs at TOP after built-ins).
        PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, false);

        Pack pack = Pack.readMetaAndCreate(
            info,
            new PathPackResources.PathResourcesSupplier(overlayPath),
            PackType.SERVER_DATA,
            selection
        );
        if (pack == null) {
            ChronoDawn.LOGGER.warn(
                "Runtime overlay pack at {} failed metadata validation; not registering",
                overlayPath
            );
            return;
        }
        event.addRepositorySource(consumer -> consumer.accept(pack));
        ChronoDawn.LOGGER.info("Registered runtime config overlay datapack with NeoForge");
    }
}
