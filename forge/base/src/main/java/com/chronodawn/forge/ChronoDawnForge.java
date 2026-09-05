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
package com.chronodawn.forge;

import com.chronodawn.ChronoDawn;
import com.chronodawn.forge.registry.ModFluidTypes;
import com.chronodawn.forge.registry.ModLootModifiers;
import com.chronodawn.forge.registry.ModParticles;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge-specific mod entry point.
 *
 * This is a minimal stub: full event registration (entity attributes, spawn
 * placements, block/tick events) is added in a later task, mirroring
 * {@code ChronoDawnNeoForge}'s constructor.
 */
@Mod(ChronoDawn.MOD_ID)
public class ChronoDawnForge {
    public ChronoDawnForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register FluidTypes (Forge-specific, must be registered before ChronoDawn.init())
        ModFluidTypes.register(modEventBus);

        ChronoDawn.init();

        // Register particle types (Forge-specific)
        ModParticles.register(modEventBus);

        // Register loot modifiers (Forge-specific)
        ModLootModifiers.register(modEventBus);
    }
}
