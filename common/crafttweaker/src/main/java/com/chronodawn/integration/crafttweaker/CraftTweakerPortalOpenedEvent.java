/*
 * Copyright (C) 2026 ksoichiro
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
package com.chronodawn.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.event.ZenEvent;
import com.blamejared.crafttweaker.api.event.bus.CommonWirelessEventBusWire;
import com.blamejared.crafttweaker.api.event.bus.IEventBus;
import com.chronodawn.api.event.PortalOpenedContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType;

/** CraftTweaker event fired after Chrono Dawn activates a portal. */
@ZenRegister
@ZenEvent
@ZenCodeType.Name("mods.chronodawn.event.PortalOpenedEvent")
public final class CraftTweakerPortalOpenedEvent {

    @ZenEvent.Bus
    public static final IEventBus<CraftTweakerPortalOpenedEvent> BUS = IEventBus.direct(
        CraftTweakerPortalOpenedEvent.class,
        CommonWirelessEventBusWire.of()
    );

    private final String portalId;
    private final ServerLevel level;
    private final BlockPos position;
    private final String cause;
    @Nullable
    private final ServerPlayer igniter;

    CraftTweakerPortalOpenedEvent(PortalOpenedContext context) {
        this.portalId = context.portalId().toString();
        this.level = context.level();
        this.position = context.position();
        this.cause = context.cause().name();
        this.igniter = context.igniter();
    }

    @ZenCodeType.Getter("portalId")
    public String portalId() {
        return portalId;
    }

    @ZenCodeType.Getter("level")
    public ServerLevel level() {
        return level;
    }

    @ZenCodeType.Getter("position")
    public BlockPos position() {
        return position;
    }

    @ZenCodeType.Getter("cause")
    public String cause() {
        return cause;
    }

    @Nullable
    @ZenCodeType.Getter("igniter")
    public ServerPlayer igniter() {
        return igniter;
    }
}
