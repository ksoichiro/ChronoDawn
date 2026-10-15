package com.chronodawn.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A page from the Lost Adventurer's journal. Its text is the tooltip.
 */
public class JournalPageItem extends Item {
    public static final int LINE_COUNT = 4;

    private final int page;

    public JournalPageItem(Properties properties, int page) {
        super(properties);
        this.page = page;
    }

    public int page() {
        return page;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        for (int line = 1; line <= LINE_COUNT; line++) {
            tooltipComponents.add(Component.translatable("item.chronodawn.faded_journal_page_" + page + ".line" + line)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
