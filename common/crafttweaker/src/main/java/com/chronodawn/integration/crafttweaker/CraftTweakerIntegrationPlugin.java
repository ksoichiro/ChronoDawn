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

import com.blamejared.crafttweaker.api.plugin.CraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ICraftTweakerPlugin;
import com.chronodawn.api.event.BossDefeatedEvents;
import com.chronodawn.api.event.PortalOpenedEvents;

/** Bridges Chrono Dawn's public lifecycle events to CraftTweaker. */
@CraftTweakerPlugin("chronodawn:events")
public final class CraftTweakerIntegrationPlugin implements ICraftTweakerPlugin {

    @Override
    public void initialize() {
        BossDefeatedEvents.register(context -> CraftTweakerBossDefeatedEvent.BUS.post(
            new CraftTweakerBossDefeatedEvent(context)
        ));
        PortalOpenedEvents.register(context -> CraftTweakerPortalOpenedEvent.BUS.post(
            new CraftTweakerPortalOpenedEvent(context)
        ));
    }
}
