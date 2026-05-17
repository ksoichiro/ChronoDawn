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

import net.minecraft.SharedConstants;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OverlayPackInjection#isServerDataRepository}. The Fabric
 * Mixin relies on this predicate to skip injection into the client resource
 * repository; a future Mojang refactor that routes ServerPacksSource into
 * both repos (or removes it from the server one) would silently flip the
 * outcome, hence the explicit regression coverage.
 *
 * <p>Instances of {@link ServerPacksSource} are built via {@link Unsafe#allocateInstance}
 * because (a) its constructor signature changes across Minecraft versions
 * (this test runs against all 11 versions sharing {@code common/shared/src/test})
 * and (b) the test only needs an object whose {@code instanceof} answer is
 * {@code true}; it never calls any method on the instance.
 */
class OverlayPackInjectionTest {

    private static Unsafe unsafe;

    @BeforeAll
    static void bootstrap() throws ReflectiveOperationException {
        // ServerPacksSource's <clinit> chain reads SharedConstants for the
        // version manifest; allocating an instance triggers that init even
        // though we never invoke any vanilla method on the object.
        SharedConstants.tryDetectVersion();
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe) f.get(null);
    }

    @Test
    void emptySources_isNotServerData() {
        assertFalse(OverlayPackInjection.isServerDataRepository(new RepositorySource[0]));
    }

    @Test
    void sourcesWithoutServerPacksSource_isNotServerData() {
        RepositorySource other = consumer -> { /* no-op */ };
        assertFalse(OverlayPackInjection.isServerDataRepository(new RepositorySource[] { other }));
    }

    @Test
    void singleServerPacksSource_isServerData() throws InstantiationException {
        ServerPacksSource serverPacks = (ServerPacksSource) unsafe.allocateInstance(ServerPacksSource.class);
        assertTrue(OverlayPackInjection.isServerDataRepository(new RepositorySource[] { serverPacks }));
    }

    @Test
    void mixedSourcesContainingServerPacksSource_isServerData() throws InstantiationException {
        RepositorySource other = consumer -> {};
        ServerPacksSource serverPacks = (ServerPacksSource) unsafe.allocateInstance(ServerPacksSource.class);
        assertTrue(OverlayPackInjection.isServerDataRepository(
            new RepositorySource[] { other, serverPacks, other }));
    }
}
