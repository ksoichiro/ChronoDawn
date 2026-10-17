package com.chronodawn.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The Lost Adventurer's journal, bound from all six pages. Its closing passage is the tooltip.
 * Praying while holding it is handled by MemorialPlateBlock.
 */
public class LostAdventurerJournalItem extends Item {
    public static final int LINE_COUNT = 4;

    public LostAdventurerJournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        for (int line = 1; line <= LINE_COUNT; line++) {
            tooltipComponents.add(Component.translatable("item.chronodawn.lost_adventurer_journal.line" + line)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
