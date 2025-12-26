package com.chronodawn.gui;

import com.chronodawn.gui.data.ChronicleData;
import com.chronodawn.gui.data.Entry;
import com.chronodawn.gui.widgets.CategoryListWidget;
import com.chronodawn.gui.widgets.EntryPageWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Main GUI screen for the Chronicle guidebook.
 * Displays categories in the left sidebar and entry content on the right.
 */
public class ChronicleScreen extends Screen {
    // Use vanilla book texture as placeholder (no custom texture needed)
    private static final ResourceLocation BOOK_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/book.png");
    private static final int BOOK_WIDTH = 320;
    private static final int BOOK_HEIGHT = 240;

    private final ChronicleData data;
    private int leftPos;
    private int topPos;

    private CategoryListWidget categoryList;
    private EntryPageWidget entryPage;
    private Entry selectedEntry;

    public ChronicleScreen() {
        super(Component.translatable("gui.chronodawn.chronicle.title"));
        this.data = ChronicleData.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        // Center the book on screen
        this.leftPos = (this.width - BOOK_WIDTH) / 2;
        this.topPos = (this.height - BOOK_HEIGHT) / 2;

        // Initialize category list widget with entry selection callback
        // Set category list to half of book width (minus margins)
        int categoryListWidth = (BOOK_WIDTH - 30) / 2; // 30 = left margin (10) + center margin (20)
        int categoryListHeight = BOOK_HEIGHT - 40;
        this.categoryList = new CategoryListWidget(
            leftPos + 10,
            topPos + 30,
            categoryListWidth,
            categoryListHeight,
            data.getCategories(),
            entry -> {
                // Entry selected - update the entry page
                selectedEntry = entry;
                if (entryPage != null) {
                    entryPage.setEntry(selectedEntry);
                }
            }
        );
        addRenderableWidget(categoryList);

        // Initialize entry page widget
        int entryPageX = leftPos + categoryListWidth + 20;
        int entryPageWidth = BOOK_WIDTH - categoryListWidth - 30;
        int entryPageHeight = BOOK_HEIGHT - 40;
        this.entryPage = new EntryPageWidget(
            entryPageX,
            topPos + 30,
            entryPageWidth,
            entryPageHeight
        );
        this.entryPage.initializeButtons(
            btn -> entryPage.previousPage(),
            btn -> entryPage.nextPage()
        );
        addRenderableWidget(entryPage);

        // Add page navigation buttons
        if (entryPage.getPreviousButton() != null) {
            addRenderableWidget(entryPage.getPreviousButton());
        }
        if (entryPage.getNextButton() != null) {
            addRenderableWidget(entryPage.getNextButton());
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Override to prevent default blur effect
        // Render semi-transparent dark background only OUTSIDE the book area
        // Color format: 0xAARRGGBB (AA=alpha, 80=50% opacity)

        // Top area
        graphics.fill(0, 0, this.width, topPos, 0x80101010);
        // Bottom area
        graphics.fill(0, topPos + BOOK_HEIGHT, this.width, this.height, 0x80101010);
        // Left area
        graphics.fill(0, topPos, leftPos, topPos + BOOK_HEIGHT, 0x80101010);
        // Right area
        graphics.fill(leftPos + BOOK_WIDTH, topPos, this.width, topPos + BOOK_HEIGHT, 0x80101010);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Layer 1: Render dark background (via our overridden renderBackground)
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // Layer 2: Render book background (sharp, no blur)
        graphics.fill(leftPos, topPos, leftPos + BOOK_WIDTH, topPos + BOOK_HEIGHT, 0xFFF0E0D0);

        // Render book border (darker brown)
        graphics.fill(leftPos, topPos, leftPos + BOOK_WIDTH, topPos + 2, 0xFF5C4033);
        graphics.fill(leftPos, topPos + BOOK_HEIGHT - 2, leftPos + BOOK_WIDTH, topPos + BOOK_HEIGHT, 0xFF5C4033);
        graphics.fill(leftPos, topPos, leftPos + 2, topPos + BOOK_HEIGHT, 0xFF5C4033);
        graphics.fill(leftPos + BOOK_WIDTH - 2, topPos, leftPos + BOOK_WIDTH, topPos + BOOK_HEIGHT, 0xFF5C4033);

        // Layer 3: Render title
        Component title = Component.translatable("gui.chronodawn.chronicle.title");
        int titleX = leftPos + (BOOK_WIDTH / 2) - (this.font.width(title) / 2);
        int titleY = topPos + 12;
        graphics.drawString(this.font, title, titleX, titleY, 0x3F3F3F, false);

        // Layer 4: Render widgets (category list, entry pages, buttons) on top
        super.render(graphics, mouseX, mouseY, partialTick);

        // Layer 5: Render tooltips (must be last to appear on top)
        if (categoryList != null) {
            String tooltip = categoryList.getHoveredTooltip();
            if (tooltip != null) {
                graphics.renderTooltip(this.font, Component.literal(tooltip), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        // Chronicle book doesn't pause the game
        return false;
    }

    /**
     * Get current client language code.
     *
     * @return Language code (e.g., "en_us", "ja_jp")
     */
    public static String getLanguageCode() {
        return Minecraft.getInstance().options.languageCode;
    }
}
