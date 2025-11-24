package com.chronosphere.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chronicle of Chronosphere - A guide book for players entering the Chronosphere dimension.
 *
 * @deprecated Replaced by Patchouli guide book system (chronosphere:chronicle).
 * This class is kept for fallback purposes only in case Patchouli is not available.
 *
 * This book contains essential information about:
 * - Dimension mechanics and time distortion effects
 * - Structure locations and their purposes
 * - Item crafting progression
 * - Boss strategies
 *
 * Players receive this book automatically when entering Chronosphere for the first time.
 *
 * Note: This is a placeholder item. The actual book given to players is a vanilla written_book
 * with custom content. This item appears in creative menu but should not be used directly.
 */
@Deprecated
public class ChronicleOfChronosphereItem extends Item {

    public ChronicleOfChronosphereItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Item.Properties()
            .stacksTo(1);
    }

    /**
     * Create a new Chronicle of Chronosphere book with pre-written content (English version).
     * Uses vanilla written_book item to ensure it can be read properly.
     * @return ItemStack with book content
     */
    public static ItemStack createBook() {
        // Use vanilla written_book item instead of custom item
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        // Create book content with pages
        List<Component> pageComponents = Arrays.asList(
            Component.literal("=== Chronicle of Chronosphere ===\n\nWelcome, traveler.\n\nYou have entered a dimension where time itself has frozen. This chronicle will guide you through your journey."),
            Component.literal("=== Time Distortion ===\n\nIn this realm, hostile creatures move at a fraction of their normal speed.\n\nYou, however, maintain your temporal flow. Use this advantage wisely."),
            Component.literal("=== Your First Goal ===\n\nSeek the Forgotten Library, a structure of knowledge lost to time.\n\nWithin its walls, you will find the recipe for the Portal Stabilizer - your key to returning home."),
            Component.literal("=== Forgotten Library ===\n\nThis ancient library contains:\n- Portal Stabilizer recipe\n- Time manipulation knowledge\n- Hidden treasures\n\nSearch thoroughly."),
            Component.literal("=== Portal Stabilizer ===\n\nOnce crafted, use it on your portal's Clockstone frame.\n\nThe portal will become bidirectional, allowing free travel between worlds.\n\nUse Time Hourglass to reignite the portal."),
            Component.literal("=== Desert Clock Tower ===\n\nIn the desert biome stands a tower of gears and clockwork.\n\nInside, you'll find Enhanced Clockstone - a material for advanced tools and weapons."),
            Component.literal("=== Time Guardian ===\n\nThis mini-boss guards the path to the Master Clock.\n\nDefeat it to obtain the Key to Master Clock.\n\nStrategy: Use Time Clock to cancel its attacks."),
            Component.literal("=== Master Clock Tower ===\n\nThe heart of this frozen world.\n\n3 Ancient Gears are required to open the boss chamber.\n\nDefeating Clockwork Sentinels may yield these gears."),
            Component.literal("=== Time Tyrant ===\n\nThe final guardian of the Stasis Core.\n\nStrategies:\n- Time Arrows inflict severe debuffs\n- Destroy Temporal Anchors to weaken abilities\n- Bring backup supplies"),
            Component.literal("=== After Victory ===\n\nDefeating the Time Tyrant grants:\n- Eye of Chronos (restores time flow permanently)\n- Stasis Core Shards (ultimate artifacts)\n\nCraft Chronoblade, Time Tyrant's Mail, and more.")
        );

        // Convert Component list to Filterable list (required in Minecraft 1.21.1)
        List<Filterable<Component>> pages = pageComponents.stream()
            .map(Filterable::passThrough)
            .collect(Collectors.toList());

        WrittenBookContent content = new WrittenBookContent(
            Filterable.passThrough("Chronicle of Chronosphere"),
            "Unknown Author",
            0,
            pages,
            false
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);

        // Disable enchantment glint effect
        book.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);

        return book;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.chronosphere.chronicle_of_chronosphere.tooltip")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
