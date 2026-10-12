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

import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Function;

/**
 * Pure decision helpers shared by the Fabric Mixin
 * ({@code PackRepositoryOverlayMixin}) — extracted so the predicate is
 * unit-testable from {@code common/shared/src/test/} without standing up a
 * Mixin transformer or loading a Minecraft client.
 *
 * <p>The client resource repository never carries a world or server folder
 * source, so the same predicate also keeps the overlay out of it.
 */
public final class OverlayPackInjection {
    private OverlayPackInjection() {}

    /**
     * Mirrors the condition under which Fabric API adds mod data packs to a
     * {@code PackRepository}: a world or server folder source is present.
     *
     * <p>The overlay overrides the Chrono Dawn dimension and references the
     * mod's own noise settings, biomes, and dimension type, so injecting it
     * into a repository without mod data packs breaks registry loading. Lunar
     * Client builds such a repository at startup (vanilla packs plus its own,
     * no world folder), which crashed the game before the title screen.
     *
     * @param packSourceOf reads the private pack source of a folder source;
     *                     the Mixin passes an accessor, tests pass a stub
     */
    public static boolean hasWorldOrServerFolderSource(
        RepositorySource[] sources,
        Function<FolderRepositorySource, PackSource> packSourceOf
    ) {
        for (RepositorySource s : sources) {
            if (s instanceof FolderRepositorySource folder) {
                PackSource packSource = packSourceOf.apply(folder);
                if (packSource == PackSource.WORLD || packSource == PackSource.SERVER) {
                    return true;
                }
            }
        }
        return false;
    }
}
