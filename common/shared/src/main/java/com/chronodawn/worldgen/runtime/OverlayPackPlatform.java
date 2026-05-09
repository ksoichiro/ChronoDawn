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

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

/**
 * Platform lookup for the loader-provided config directory, used by
 * {@link com.chronodawn.config.ConfigLoader} and {@link OverlayPackBootstrap}.
 *
 * <p>Lives in the same package as the worldgen-runtime consumers (instead of
 * piggy-backing on {@code com.chronodawn.platform.ChronoDawnPlatform}) because
 * Architectury's default {@code @ExpectPlatform} resolution looks for
 * {@code <originalPackage>.<loaderName>.<OriginalClassImpl>} — i.e.
 * {@code com.chronodawn.worldgen.runtime.fabric.OverlayPackPlatformImpl} /
 * {@code com.chronodawn.worldgen.runtime.neoforge.OverlayPackPlatformImpl}.
 * Reusing {@code ChronoDawnPlatform} would have required moving its existing
 * impls into a different package, with a much larger blast radius.
 */
public final class OverlayPackPlatform {
    private OverlayPackPlatform() {}

    /**
     * @return the loader's config directory, e.g.
     *         {@code <minecraft instance>/config/} for single-player or
     *         {@code <server root>/config/} for a dedicated server.
     */
    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError("@ExpectPlatform method not replaced");
    }
}
