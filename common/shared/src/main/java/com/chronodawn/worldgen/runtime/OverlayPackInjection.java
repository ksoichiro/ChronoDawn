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

import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;

/**
 * Pure decision helpers shared by the Fabric Mixin
 * ({@code PackRepositoryOverlayMixin}) — extracted so the predicate is
 * unit-testable from {@code common/shared/src/test/} without standing up a
 * Mixin transformer or loading a Minecraft client.
 *
 * <p>Vanilla constructs the server data {@code PackRepository} with a
 * {@link ServerPacksSource} as one of its sources; the resource (client)
 * repository never does. That instance-of test is the most fragile bit of
 * loader-side wiring, since a future Mojang refactor could route the same
 * sources to both repos and silently re-enable overlay injection on the
 * client side — hence the dedicated regression coverage.
 */
public final class OverlayPackInjection {
    private OverlayPackInjection() {}

    public static boolean isServerDataRepository(RepositorySource[] sources) {
        for (RepositorySource s : sources) {
            if (s instanceof ServerPacksSource) {
                return true;
            }
        }
        return false;
    }
}
