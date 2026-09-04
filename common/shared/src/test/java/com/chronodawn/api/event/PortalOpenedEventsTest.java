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
package com.chronodawn.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalOpenedEventsTest {

    private static final UUID PORTAL_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final PortalOpenedContext CONTEXT = new PortalOpenedContext() {
        @Override
        public UUID portalId() {
            return PORTAL_ID;
        }

        @Override
        public ServerLevel level() {
            return null;
        }

        @Override
        public BlockPos position() {
            return null;
        }

        @Override
        public PortalOpenCause cause() {
            return PortalOpenCause.IGNITION;
        }

        @Override
        public ServerPlayer igniter() {
            return null;
        }
    };

    private final List<PortalOpenedListener> registrations = new ArrayList<>();

    @AfterEach
    void unregisterListeners() {
        for (PortalOpenedListener listener : registrations) {
            PortalOpenedEvents.unregister(listener);
        }
    }

    @Test
    void listenersRunInRegistrationOrder() {
        List<Integer> calls = new ArrayList<>();
        register(context -> calls.add(1));
        register(context -> calls.add(2));

        PortalOpenedEvents.dispatch(CONTEXT);

        assertEquals(List.of(1, 2), calls);
    }

    @Test
    void unregisterRemovesTheListener() {
        List<UUID> calls = new ArrayList<>();
        PortalOpenedListener listener = context -> calls.add(context.portalId());
        register(listener);

        PortalOpenedEvents.unregister(listener);
        registrations.remove(listener);
        PortalOpenedEvents.dispatch(CONTEXT);

        assertEquals(List.of(), calls);
    }

    @Test
    void duplicateRegistrationsProduceDuplicateCallbacks() {
        List<UUID> calls = new ArrayList<>();
        PortalOpenedListener listener = context -> calls.add(context.portalId());
        register(listener);
        register(listener);

        PortalOpenedEvents.dispatch(CONTEXT);

        assertEquals(List.of(PORTAL_ID, PORTAL_ID), calls);
    }

    @Test
    void failingListenerDoesNotSuppressLaterListeners() {
        List<UUID> calls = new ArrayList<>();
        register(context -> {
            throw new IllegalStateException("expected test failure");
        });
        register(context -> calls.add(context.portalId()));

        assertDoesNotThrow(() -> PortalOpenedEvents.dispatch(CONTEXT));
        assertEquals(List.of(PORTAL_ID), calls);
    }

    @Test
    void nullListenersAreRejected() {
        assertThrows(NullPointerException.class, () -> PortalOpenedEvents.register(null));
        assertThrows(NullPointerException.class, () -> PortalOpenedEvents.unregister(null));
    }

    @Test
    void portalOpenCauseHasExactlyTwoStableValues() {
        assertEquals(List.of(PortalOpenCause.IGNITION, PortalOpenCause.REIGNITION), List.of(PortalOpenCause.values()));
    }

    private void register(PortalOpenedListener listener) {
        PortalOpenedEvents.register(listener);
        registrations.add(listener);
    }
}
