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
package com.chronodawn.forge.event;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.runtime.OverlayPackBootstrap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;

import java.nio.file.Path;

/**
 * Forge-side registration of the on-disk runtime overlay datapack as a
 * built-in pack source for the server pack repository.
 *
 * <p>{@link com.chronodawn.config.ConfigLoader} writes
 * {@code config/chronodawn-runtime-overlay/} during {@code ChronoDawn.init()};
 * here we expose that directory as a high-priority data pack so the JSONs
 * inside override the bundled mod resources.
 *
 * <p>1.20.1 lacks {@code PackLocationInfo}, {@code PackSelectionConfig}, and
 * {@code PathPackResources.PathResourcesSupplier} (all added in 1.20.5), so
 * this uses the older {@code Pack.readMetaAndCreate(name, title, alwaysActive,
 * resources, type, position, source)} overload and constructs
 * {@link PathPackResources} directly with {@code (name, path, isBuiltIn)},
 * mirroring {@code PackRepositoryOverlayMixin} (the Fabric 1.20.1 equivalent).
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

        Pack.ResourcesSupplier resourcesSupplier = name -> new PathPackResources(
            OverlayPackBootstrap.PACK_ID,
            overlayPath,
            false
        );

        Pack pack = Pack.readMetaAndCreate(
            OverlayPackBootstrap.PACK_ID,
            Component.literal(PACK_DISPLAY_NAME),
            true,
            resourcesSupplier,
            PackType.SERVER_DATA,
            Pack.Position.TOP,
            PackSource.BUILT_IN
        );
        if (pack == null) {
            ChronoDawn.LOGGER.warn(
                "Runtime overlay pack at {} failed metadata validation; not registering",
                overlayPath
            );
            return;
        }
        event.addRepositorySource(consumer -> consumer.accept(pack));
        ChronoDawn.LOGGER.info("Registered runtime config overlay datapack with Forge");
    }
}
