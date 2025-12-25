package com.chronodawn.gui.widgets;

import com.chronodawn.gui.ChronicleScreen;
import com.chronodawn.gui.data.Entry;
import com.chronodawn.gui.data.Page;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Widget for displaying entry pages in the Chronicle guidebook.
 * Handles text rendering, pagination, and recipe display.
 */
public class EntryPageWidget extends AbstractWidget {
    private Entry currentEntry;
    private int currentPageIndex = 0;

    private Button previousButton;
    private Button nextButton;
    private Component pageNumberText;

    private static final int TEXT_MARGIN = 5;
    private static final int LINE_HEIGHT = 10;

    public EntryPageWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    /**
     * Set the entry to display.
     *
     * @param entry Entry to display (null to clear)
     */
    public void setEntry(Entry entry) {
        this.currentEntry = entry;
        this.currentPageIndex = 0;
        updatePageButtons();
    }

    /**
     * Initialize page navigation buttons.
     * Should be called from parent screen's init() method.
     */
    public void initializeButtons(Button.OnPress previousAction, Button.OnPress nextAction) {
        int buttonY = getY() + height - 25;

        this.previousButton = Button.builder(
            Component.literal("<"),
            previousAction
        ).bounds(getX() + 5, buttonY, 20, 20).build();

        this.nextButton = Button.builder(
            Component.literal(">"),
            nextAction
        ).bounds(getX() + width - 25, buttonY, 20, 20).build();

        updatePageButtons();
    }

    /**
     * Update button visibility based on current page.
     */
    private void updatePageButtons() {
        if (previousButton != null && nextButton != null) {
            if (currentEntry == null || currentEntry.getPages().isEmpty()) {
                previousButton.visible = false;
                previousButton.active = false;
                nextButton.visible = false;
                nextButton.active = false;
                pageNumberText = Component.empty();
            } else {
                int totalPages = currentEntry.getPages().size();
                boolean hasPrevious = currentPageIndex > 0;
                boolean hasNext = currentPageIndex < totalPages - 1;

                previousButton.visible = hasPrevious;
                previousButton.active = hasPrevious;
                nextButton.visible = hasNext;
                nextButton.active = hasNext;
                pageNumberText = Component.translatable("gui.chronodawn.chronicle.page",
                    currentPageIndex + 1, totalPages);
            }
        }
    }

    /**
     * Go to previous page.
     */
    public void previousPage() {
        if (currentEntry != null && currentPageIndex > 0) {
            currentPageIndex--;
            updatePageButtons();
        }
    }

    /**
     * Go to next page.
     */
    public void nextPage() {
        if (currentEntry != null && currentPageIndex < currentEntry.getPages().size() - 1) {
            currentPageIndex++;
            updatePageButtons();
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (currentEntry == null || currentEntry.getPages().isEmpty()) {
            // No entry selected - show placeholder
            String text = "Select a category to begin";
            Font font = Minecraft.getInstance().font;
            int textX = getX() + (width - font.width(text)) / 2;
            int textY = getY() + height / 2;
            graphics.drawString(font, text, textX, textY, 0x808080, false);
            return;
        }

        Page page = currentEntry.getPages().get(currentPageIndex);

        if (page.getType() == Page.PageType.TEXT) {
            renderTextPage(graphics, page);
        } else if (page.getType() == Page.PageType.RECIPE) {
            renderRecipePage(graphics, page);
        }

        // Render page number
        if (pageNumberText != null) {
            Font font = Minecraft.getInstance().font;
            int textX = getX() + (width - font.width(pageNumberText)) / 2;
            int textY = getY() + height - 35;
            graphics.drawString(font, pageNumberText, textX, textY, 0x3F3F3F, false);
        }
    }

    /**
     * Render a text page with word wrap.
     */
    private void renderTextPage(GuiGraphics graphics, Page page) {
        String languageCode = ChronicleScreen.getLanguageCode();
        String text = page.getText().get(languageCode);

        Font font = Minecraft.getInstance().font;
        // Calculate max line width from widget width
        int maxLineWidth = width - (TEXT_MARGIN * 2);
        List<String> lines = wrapText(text, maxLineWidth, font);

        int textX = getX() + TEXT_MARGIN;
        int textY = getY() + TEXT_MARGIN;

        // Reserve space for page number at bottom (40 pixels)
        int maxTextHeight = height - 40;

        for (String line : lines) {
            // Check if we have space for this line
            if (textY + LINE_HEIGHT - getY() > maxTextHeight) {
                break; // Stop rendering if we've reached the bottom margin
            }

            graphics.drawString(font, line, textX, textY, 0x000000, false);
            textY += LINE_HEIGHT;
        }
    }

    /**
     * Render a recipe page showing the crafting recipe.
     */
    private void renderRecipePage(GuiGraphics graphics, Page page) {
        ResourceLocation recipeId = page.getRecipe();
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        Optional<RecipeHolder<?>> recipeHolder = recipeManager.byKey(recipeId);
        if (recipeHolder.isEmpty()) {
            // Recipe not found - render error message with word wrap
            Font font = Minecraft.getInstance().font;
            String errorText = "Recipe not found: " + recipeId;
            int maxLineWidth = width - (TEXT_MARGIN * 2);
            List<String> lines = wrapText(errorText, maxLineWidth, font);

            int textX = getX() + TEXT_MARGIN;
            int textY = getY() + TEXT_MARGIN;

            for (String line : lines) {
                graphics.drawString(font, line, textX, textY, 0xFF0000, false);
                textY += LINE_HEIGHT;
            }
            return;
        }

        // Simple recipe display (show output item)
        // TODO: In a full implementation, render the actual crafting grid
        var recipe = recipeHolder.get().value();
        ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());

        int iconX = getX() + width / 2 - 8;
        int iconY = getY() + 20;
        graphics.renderItem(result, iconX, iconY);

        // Render item name
        Font font = Minecraft.getInstance().font;
        String itemName = result.getHoverName().getString();
        int textX = getX() + (width - font.width(itemName)) / 2;
        int textY = iconY + 20;
        graphics.drawString(font, itemName, textX, textY, 0x000000, false);
    }

    /**
     * Wrap text to fit within a maximum width.
     * Supports both English (word-based wrapping) and Japanese (character-based wrapping).
     *
     * @param text Text to wrap
     * @param maxWidth Maximum line width
     * @param font Font for measuring text width
     * @return List of wrapped lines
     */
    private List<String> wrapText(String text, int maxWidth, Font font) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            // Character-by-character wrapping (works for both English and Japanese)
            StringBuilder currentLine = new StringBuilder();

            for (int i = 0; i < paragraph.length(); i++) {
                char c = paragraph.charAt(i);
                String testLine = currentLine.toString() + c;

                if (font.width(testLine) <= maxWidth) {
                    currentLine.append(c);
                } else {
                    // Current line is full, add it and start new line
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                        currentLine.append(c);
                    } else {
                        // Single character too wide (shouldn't happen)
                        lines.add(String.valueOf(c));
                    }
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    public Button getPreviousButton() {
        return previousButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if buttons are clicked first
        if (previousButton != null && previousButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (nextButton != null && nextButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        // Default behavior for the widget itself
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (currentEntry != null) {
            String languageCode = ChronicleScreen.getLanguageCode();
            output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                Component.literal(currentEntry.getTitle().get(languageCode)));
        }
    }
}
