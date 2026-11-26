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

import com.chronodawn.core.portal.PortalIgnitionRegistration;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalState;
import com.chronodawn.core.portal.PortalStateMachine;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests that igniting a frame reuses the entry at that position instead of registering a duplicate.
 */
class PortalIgnitionRegistrationTest {
    private static final BlockPos POS = new BlockPos(12, 70, -34);

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clearRegistry() {
        PortalRegistry.getInstance().clear();
    }

    private static PortalStateMachine registerWithState(PortalState state) {
        PortalStateMachine portal = new PortalStateMachine(UUID.randomUUID(), Level.OVERWORLD, POS);
        PortalRegistry.getInstance().registerPortal(portal);
        portal.setState(state);
        return portal;
    }

    @Test
    void noEntry_registersNewActivatedEntry() {
        PortalRegistry registry = PortalRegistry.getInstance();

        PortalStateMachine portal = PortalIgnitionRegistration.registerOrReuse(registry, Level.OVERWORLD, POS);

        assertEquals(PortalState.ACTIVATED, portal.getCurrentState());
        assertSame(portal, registry.getPortalAt(Level.OVERWORLD, POS));
        assertEquals(1, registry.getAllPortals().size());
    }

    @Test
    void igniteTwiceAfterDeactivation_reusesSameEntry() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalStateMachine first = PortalIgnitionRegistration.registerOrReuse(registry, Level.OVERWORLD, POS);
        first.deactivate();

        PortalStateMachine second = PortalIgnitionRegistration.registerOrReuse(registry, Level.OVERWORLD, POS);

        assertSame(first, second);
        assertEquals(PortalState.ACTIVATED, second.getCurrentState());
        assertEquals(1, registry.getAllPortals().size());
    }

    @Test
    void inactiveEntry_isActivatedInPlace() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalStateMachine existing = registerWithState(PortalState.INACTIVE);

        PortalStateMachine portal = PortalIgnitionRegistration.registerOrReuse(registry, Level.OVERWORLD, POS);

        assertSame(existing, portal);
        assertEquals(PortalState.ACTIVATED, portal.getCurrentState());
        assertEquals(1, registry.getAllPortals().size());
    }

    @Test
    void activatedEntry_isKept() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalStateMachine existing = registerWithState(PortalState.ACTIVATED);

        PortalStateMachine portal = PortalIgnitionRegistration.registerOrReuse(registry, Level.OVERWORLD, POS);

        assertSame(existing, portal);
        assertEquals(1, registry.getAllPortals().size());
    }

    @Test
    void stabilizedEntry_isReplacedByNewActivatedEntry() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalStateMachine existing = registerWithState(PortalState.STABILIZED);

        PortalStateMachine portal = PortalIgnitionRegistration.registerOrReuse(registry, Level.OVERWORLD, POS);

        assertNotSame(existing, portal);
        assertEquals(PortalState.ACTIVATED, portal.getCurrentState());
        assertEquals(PortalState.STABILIZED, existing.getCurrentState());
        assertSame(portal, registry.getPortalAt(Level.OVERWORLD, POS));
    }

    @Test
    void otherDimensionEntry_isNotReused() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalStateMachine endPortal = new PortalStateMachine(UUID.randomUUID(), Level.END, POS);
        registry.registerPortal(endPortal);

        PortalStateMachine portal = PortalIgnitionRegistration.registerOrReuse(registry, Level.OVERWORLD, POS);

        assertNotSame(endPortal, portal);
        assertSame(endPortal, registry.getPortalAt(Level.END, POS));
    }
}
