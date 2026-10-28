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
package com.chronodawn.items;

import com.chronodawn.core.wayside.CairnHour;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A stone engraved with one hour of the day, given by a Time Cairn dial. Its text is the tooltip, and the last
 * line is a hint that four different stones make a Cairn Keystone.
 */
public class CairnStoneItem extends Item {
    private static final int LINE_COUNT = 2;

    private final CairnHour hour;

    public CairnStoneItem(Properties properties, CairnHour hour) {
        super(properties);
        this.hour = hour;
    }

    public CairnHour hour() {
        return hour;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        for (int line = 1; line <= LINE_COUNT; line++) {
            tooltipComponents.add(Component.translatable("item.chronodawn." + hour.stonePath() + ".line" + line)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
