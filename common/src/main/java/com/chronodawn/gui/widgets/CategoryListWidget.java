package com.chronodawn.gui.widgets;

import com.chronodawn.gui.ChronicleScreen;
import com.chronodawn.gui.data.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Widget for displaying the category list in the Chronicle guidebook.
 * Displays categories in a scrollable list on the left sidebar.
 */
public class CategoryListWidget extends AbstractWidget {
    private final List<Category> categories;
    private Category selectedCategory;
    private final CategorySelectionCallback selectionCallback;

    private static final int CATEGORY_HEIGHT = 20;
    private static final int ICON_SIZE = 16;
    private static final int PADDING = 2;

    public CategoryListWidget(int x, int y, int width, int height,
                               List<Category> categories,
                               CategorySelectionCallback selectionCallback) {
        super(x, y, width, height, Component.empty());
        this.categories = categories;
        this.selectionCallback = selectionCallback;

        // Select first category by default
        if (!categories.isEmpty()) {
            this.selectedCategory = categories.get(0);
            if (selectionCallback != null) {
                selectionCallback.onCategorySelected(selectedCategory);
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int currentY = getY();

        for (Category category : categories) {
            boolean isHovered = mouseX >= getX() && mouseX < getX() + width &&
                              mouseY >= currentY && mouseY < currentY + CATEGORY_HEIGHT;
            boolean isSelected = category == selectedCategory;

            // Render background (subtle, book-like)
            int backgroundColor = isSelected ? 0x40D0C0A0 : (isHovered ? 0x20D0C0A0 : 0x00000000);
            if (backgroundColor != 0) {
                graphics.fill(getX(), currentY, getX() + width, currentY + CATEGORY_HEIGHT, backgroundColor);
            }

            // Render icon
            ItemStack icon = getIconStack(category);
            graphics.renderItem(icon, getX() + PADDING, currentY + PADDING);

            // Render text
            String languageCode = ChronicleScreen.getLanguageCode();
            String title = category.getTitle().get(languageCode);
            int textX = getX() + ICON_SIZE + PADDING * 2;
            int textY = currentY + (CATEGORY_HEIGHT - 8) / 2;
            int textColor = isSelected ? 0x3F3F3F : 0x5F5F5F;
            graphics.drawString(Minecraft.getInstance().font, title, textX, textY, textColor, false);

            currentY += CATEGORY_HEIGHT;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false; // Only handle left clicks
        }

        int currentY = getY();
        for (Category category : categories) {
            if (mouseX >= getX() && mouseX < getX() + width &&
                mouseY >= currentY && mouseY < currentY + CATEGORY_HEIGHT) {
                // Category clicked
                selectedCategory = category;
                if (selectionCallback != null) {
                    selectionCallback.onCategorySelected(category);
                }
                return true;
            }
            currentY += CATEGORY_HEIGHT;
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (selectedCategory != null) {
            String languageCode = ChronicleScreen.getLanguageCode();
            output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                Component.literal(selectedCategory.getTitle().get(languageCode)));
        }
    }

    /**
     * Callback interface for category selection events.
     */
    public interface CategorySelectionCallback {
        void onCategorySelected(Category category);
    }
}
