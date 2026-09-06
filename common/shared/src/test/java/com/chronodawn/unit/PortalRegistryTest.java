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

import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link PortalRegistry} position lookups.
 *
 * Chrono Dawn maps Overworld and Chrono Dawn coordinates 1:1, so a portal frame in one
 * dimension can land on the exact same {@link BlockPos} as a portal already registered in
 * another. getPortalAt must scope its lookup by dimension, or the wrong dimension's portal
 * is returned and the caller never registers the intended one.
 */
class PortalRegistryTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clearRegistry() {
        PortalRegistry.getInstance().clear();
    }

    @Test
    void getPortalAt_returnsNull_whenPositionUnregistered() {
        BlockPos pos = new BlockPos(10, 80, 10);

        assertNull(PortalRegistry.getInstance().getPortalAt(Level.OVERWORLD, pos));
    }

    @Test
    void getPortalAt_scopesLookupByDimension_evenAtSamePosition() {
        BlockPos collidingPos = new BlockPos(100, 80, 100);
        PortalRegistry registry = PortalRegistry.getInstance();

        PortalStateMachine overworldPortal = new PortalStateMachine(UUID.randomUUID(), Level.OVERWORLD, collidingPos);
        registry.registerPortal(overworldPortal);

        // No portal has been registered in Level.END at this position yet.
        assertNull(registry.getPortalAt(Level.END, collidingPos));
        assertSame(overworldPortal, registry.getPortalAt(Level.OVERWORLD, collidingPos));

        PortalStateMachine endPortal = new PortalStateMachine(UUID.randomUUID(), Level.END, collidingPos);
        registry.registerPortal(endPortal);

        assertNotEquals(overworldPortal.getPortalId(), endPortal.getPortalId());
        assertSame(overworldPortal, registry.getPortalAt(Level.OVERWORLD, collidingPos));
        assertSame(endPortal, registry.getPortalAt(Level.END, collidingPos));
    }

    @Test
    void unregisterPortal_onlyClearsItsOwnDimension() {
        BlockPos collidingPos = new BlockPos(5, 70, 5);
        PortalRegistry registry = PortalRegistry.getInstance();

        PortalStateMachine overworldPortal = new PortalStateMachine(UUID.randomUUID(), Level.OVERWORLD, collidingPos);
        PortalStateMachine endPortal = new PortalStateMachine(UUID.randomUUID(), Level.END, collidingPos);
        registry.registerPortal(overworldPortal);
        registry.registerPortal(endPortal);

        registry.unregisterPortal(overworldPortal.getPortalId());

        assertNull(registry.getPortalAt(Level.OVERWORLD, collidingPos));
        assertSame(endPortal, registry.getPortalAt(Level.END, collidingPos));
    }

    @Test
    void getPortalAt_returnsNull_forUnknownDimension() {
        ResourceKey<Level> unusedDimension = Level.NETHER;

        assertNull(PortalRegistry.getInstance().getPortalAt(unusedDimension, new BlockPos(0, 0, 0)));
    }
}
