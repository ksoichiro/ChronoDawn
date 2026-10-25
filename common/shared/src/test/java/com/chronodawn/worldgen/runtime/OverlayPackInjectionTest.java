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
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OverlayPackInjection#hasWorldOrServerFolderSource}. The
 * Fabric Mixin relies on this predicate to inject the runtime overlay only
 * where Fabric API also adds mod data packs; injecting it anywhere else makes
 * registry loading fail on the overlay's references to the mod's worldgen data.
 *
 * <p>Source instances are built via {@code sun.misc.Unsafe#allocateInstance}
 * because (a) their constructor signatures change across Minecraft versions
 * (this test runs against every version sharing {@code common/shared/src/test})
 * and (b) the test only needs objects whose {@code instanceof} answer is
 * {@code true}; it never calls any method on them. The pack source of a
 * folder is supplied by a stub instead of the Mixin accessor. Unsafe is
 * looked up by name because javac reports any compile-time reference to it
 * as an internal proprietary API, and {@code @SuppressWarnings} cannot
 * silence that warning.
 */
class OverlayPackInjectionTest {

    private static Object unsafe;
    private static Method allocateInstance;

    @BeforeAll
    static void bootstrap() throws ReflectiveOperationException {
        // ServerPacksSource's <clinit> chain reads SharedConstants for the
        // version manifest; allocating an instance triggers that init even
        // though we never invoke any vanilla method on the object.
        SharedConstants.tryDetectVersion();
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field f = unsafeClass.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = f.get(null);
        allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
    }

    @Test
    void emptySources_hasNoWorldOrServerFolder() {
        assertFalse(OverlayPackInjection.hasWorldOrServerFolderSource(
            new RepositorySource[0], packSourceOf(PackSource.WORLD)));
    }

    @Test
    void sourcesWithoutFolderSource_hasNoWorldOrServerFolder() {
        RepositorySource other = consumer -> { /* no-op */ };
        assertFalse(OverlayPackInjection.hasWorldOrServerFolderSource(
            new RepositorySource[] { other }, packSourceOf(PackSource.WORLD)));
    }

    @Test
    void vanillaOnlySources_hasNoWorldOrServerFolder() throws ReflectiveOperationException {
        // Lunar Client loads registries from a repository like this at startup.
        // Fabric API adds no mod data packs to it, so the overlay must stay out.
        ServerPacksSource serverPacks = allocate(ServerPacksSource.class);
        assertFalse(OverlayPackInjection.hasWorldOrServerFolderSource(
            new RepositorySource[] { serverPacks }, packSourceOf(PackSource.WORLD)));
    }

    @Test
    void worldFolderSource_hasWorldOrServerFolder() throws ReflectiveOperationException {
        ServerPacksSource serverPacks = allocate(ServerPacksSource.class);
        FolderRepositorySource folder = allocate(FolderRepositorySource.class);
        assertTrue(OverlayPackInjection.hasWorldOrServerFolderSource(
            new RepositorySource[] { serverPacks, folder }, packSourceOf(PackSource.WORLD)));
    }

    @Test
    void serverFolderSource_hasWorldOrServerFolder() throws ReflectiveOperationException {
        FolderRepositorySource folder = allocate(FolderRepositorySource.class);
        assertTrue(OverlayPackInjection.hasWorldOrServerFolderSource(
            new RepositorySource[] { folder }, packSourceOf(PackSource.SERVER)));
    }

    @Test
    void otherFolderSource_hasNoWorldOrServerFolder() throws ReflectiveOperationException {
        FolderRepositorySource folder = allocate(FolderRepositorySource.class);
        assertFalse(OverlayPackInjection.hasWorldOrServerFolderSource(
            new RepositorySource[] { folder }, packSourceOf(PackSource.DEFAULT)));
    }

    private static <T> T allocate(Class<T> type) throws ReflectiveOperationException {
        return type.cast(allocateInstance.invoke(unsafe, type));
    }

    private static Function<FolderRepositorySource, PackSource> packSourceOf(PackSource packSource) {
        return folder -> packSource;
    }
}
