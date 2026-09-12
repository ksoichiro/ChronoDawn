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
import com.chronodawn.api.event.BossDefeatedContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType;

/** CraftTweaker event fired after Chrono Dawn applies a boss's defeat effects. */
@ZenRegister
@ZenEvent
@ZenCodeType.Name("mods.chronodawn.event.BossDefeatedEvent")
public final class CraftTweakerBossDefeatedEvent {

    @ZenEvent.Bus
    public static final IEventBus<CraftTweakerBossDefeatedEvent> BUS = IEventBus.direct(
        CraftTweakerBossDefeatedEvent.class,
        CommonWirelessEventBusWire.of()
    );

    private final String bossId;
    private final LivingEntity boss;
    private final ServerLevel level;
    private final BlockPos position;
    private final DamageSource damageSource;
    @Nullable
    private final ServerPlayer defeatingPlayer;

    CraftTweakerBossDefeatedEvent(BossDefeatedContext context) {
        this.bossId = context.bossId();
        this.boss = context.boss();
        this.level = context.level();
        this.position = context.position();
        this.damageSource = context.damageSource();
        this.defeatingPlayer = context.defeatingPlayer();
    }

    @ZenCodeType.Getter("bossId")
    public String bossId() {
        return bossId;
    }

    @ZenCodeType.Getter("boss")
    public LivingEntity boss() {
        return boss;
    }

    @ZenCodeType.Getter("level")
    public ServerLevel level() {
        return level;
    }

    @ZenCodeType.Getter("position")
    public BlockPos position() {
        return position;
    }

    @ZenCodeType.Getter("damageSource")
    public DamageSource damageSource() {
        return damageSource;
    }

    @Nullable
    @ZenCodeType.Getter("defeatingPlayer")
    public ServerPlayer defeatingPlayer() {
        return defeatingPlayer;
    }
}
