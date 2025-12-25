package com.chronodawn.gui.widgets;

import com.chronodawn.gui.ChronicleScreen;
import com.chronodawn.gui.data.Category;
import com.chronodawn.gui.data.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Widget for displaying the category list in the Chronicle guidebook.
 * Displays categories in an expandable list on the left sidebar.
 * When a category is clicked, it expands to show its entries.
 */
public class CategoryListWidget extends AbstractWidget {
    private final List<Category> categories;
    private Category selectedCategory;
    private Entry selectedEntry;
    private final EntrySelectionCallback selectionCallback;

    // Track which categories are expanded
    private final Map<Category, Boolean> expandedCategories = new HashMap<>();

    private static final int CATEGORY_HEIGHT = 20;
    private static final int ENTRY_HEIGHT = 18;
    private static final int ICON_SIZE = 16;
    private static final int PADDING = 2;
    private static final int ENTRY_INDENT = 10;

    public CategoryListWidget(int x, int y, int width, int height,
                               List<Category> categories,
                               EntrySelectionCallback selectionCallback) {
        super(x, y, width, height, Component.empty());
        this.categories = categories;
        this.selectionCallback = selectionCallback;

        // Expand and select first category and entry by default
        if (!categories.isEmpty()) {
            this.selectedCategory = categories.get(0);
            this.expandedCategories.put(selectedCategory, true);

            if (!selectedCategory.getEntries().isEmpty()) {
                this.selectedEntry = selectedCategory.getEntries().get(0);
                if (selectionCallback != null) {
                    selectionCallback.onEntrySelected(selectedEntry);
                }
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int currentY = getY();
        String languageCode = ChronicleScreen.getLanguageCode();

        for (Category category : categories) {
            boolean isExpanded = expandedCategories.getOrDefault(category, false);

            // Render category
            boolean isCategoryHovered = mouseX >= getX() && mouseX < getX() + width &&
                                       mouseY >= currentY && mouseY < currentY + CATEGORY_HEIGHT;
            boolean isCategorySelected = category == selectedCategory;

            // Render category background
            int backgroundColor = isCategorySelected ? 0x40D0C0A0 : (isCategoryHovered ? 0x20D0C0A0 : 0x00000000);
            if (backgroundColor != 0) {
                graphics.fill(getX(), currentY, getX() + width, currentY + CATEGORY_HEIGHT, backgroundColor);
            }

            // Render expand/collapse indicator
            String expandIndicator = isExpanded ? "▼" : "▶";
            graphics.drawString(Minecraft.getInstance().font, expandIndicator,
                getX() + 2, currentY + (CATEGORY_HEIGHT - 8) / 2, 0x5F5F5F, false);

            // Render category icon
            ItemStack icon = getIconStack(category);
            graphics.renderItem(icon, getX() + PADDING + 10, currentY + PADDING);

            // Render category text
            String title = category.getTitle().get(languageCode);
            int textX = getX() + ICON_SIZE + PADDING * 2 + 10;
            int textY = currentY + (CATEGORY_HEIGHT - 8) / 2;
            int textColor = isCategorySelected ? 0x3F3F3F : 0x5F5F5F;
            graphics.drawString(Minecraft.getInstance().font, title, textX, textY, textColor, false);

            currentY += CATEGORY_HEIGHT;

            // Render entries if expanded
            if (isExpanded) {
                for (Entry entry : category.getEntries()) {
                    boolean isEntryHovered = mouseX >= getX() && mouseX < getX() + width &&
                                            mouseY >= currentY && mouseY < currentY + ENTRY_HEIGHT;
                    boolean isEntrySelected = entry == selectedEntry;

                    // Render entry background
                    int entryBgColor = isEntrySelected ? 0x30A0907F : (isEntryHovered ? 0x15A0907F : 0x00000000);
                    if (entryBgColor != 0) {
                        graphics.fill(getX(), currentY, getX() + width, currentY + ENTRY_HEIGHT, entryBgColor);
                    }

                    // Render entry icon (smaller)
                    ItemStack entryIcon = getIconStack(entry);
                    graphics.renderItem(entryIcon, getX() + ENTRY_INDENT + PADDING, currentY + 1);

                    // Render entry text
                    String entryTitle = entry.getTitle().get(languageCode);
                    int entryTextX = getX() + ENTRY_INDENT + ICON_SIZE + PADDING;
                    int entryTextY = currentY + (ENTRY_HEIGHT - 8) / 2;
                    int entryTextColor = isEntrySelected ? 0x2F2F2F : 0x6F6F6F;

                    // Truncate text if too long
                    int maxWidth = width - ENTRY_INDENT - ICON_SIZE - PADDING * 2;
                    String displayText = Minecraft.getInstance().font.plainSubstrByWidth(entryTitle, maxWidth);
                    graphics.drawString(Minecraft.getInstance().font, displayText, entryTextX, entryTextY, entryTextColor, false);

                    currentY += ENTRY_HEIGHT;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false; // Only handle left clicks
        }

        int currentY = getY();
        for (Category category : categories) {
            boolean isExpanded = expandedCategories.getOrDefault(category, false);

            // Check if category was clicked
            if (mouseX >= getX() && mouseX < getX() + width &&
                mouseY >= currentY && mouseY < currentY + CATEGORY_HEIGHT) {
                // Toggle expansion
                expandedCategories.put(category, !isExpanded);
                selectedCategory = category;
                return true;
            }
            currentY += CATEGORY_HEIGHT;

            // Check if any entry was clicked (if category is expanded)
            if (isExpanded) {
                for (Entry entry : category.getEntries()) {
                    if (mouseX >= getX() && mouseX < getX() + width &&
                        mouseY >= currentY && mouseY < currentY + ENTRY_HEIGHT) {
                        // Entry clicked
                        selectedCategory = category;
                        selectedEntry = entry;
                        if (selectionCallback != null) {
                            selectionCallback.onEntrySelected(entry);
                        }
                        return true;
                    }
                    currentY += ENTRY_HEIGHT;
                }
            }
        }

        return false;
    }

    /**
     * Get the ItemStack for rendering a category icon.
     *
     * @param category Category
     * @return ItemStack for icon
     */
    private ItemStack getIconStack(Category category) {
        ResourceLocation iconId = category.getIcon();
        var item = BuiltInRegistries.ITEM.get(iconId);
        if (item == null || item == Items.AIR) {
            return new ItemStack(Items.BOOK); // Fallback to book
        }
        return new ItemStack(item);
    }

    /**
     * Get the ItemStack for rendering an entry icon.
     *
     * @param entry Entry
     * @return ItemStack for icon
     */
    private ItemStack getIconStack(Entry entry) {
        ResourceLocation iconId = entry.getIcon();
        var item = BuiltInRegistries.ITEM.get(iconId);
        if (item == null || item == Items.AIR) {
            return new ItemStack(Items.PAPER); // Fallback to paper
        }
        return new ItemStack(item);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (selectedEntry != null) {
            String languageCode = ChronicleScreen.getLanguageCode();
            output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                Component.literal(selectedEntry.getTitle().get(languageCode)));
        } else if (selectedCategory != null) {
            String languageCode = ChronicleScreen.getLanguageCode();
            output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                Component.literal(selectedCategory.getTitle().get(languageCode)));
        }
    }

    /**
     * Callback interface for entry selection events.
     */
    public interface EntrySelectionCallback {
        void onEntrySelected(Entry entry);
    }
}
