package com.chronodawn.gui.widgets;

import com.chronodawn.gui.ChronicleScreen;
import com.chronodawn.gui.data.Entry;
import com.chronodawn.gui.data.Page;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.mojang.blaze3d.systems.RenderSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Widget for displaying entry pages in the Chronicle guidebook.
 * Handles text rendering, pagination, and recipe display.
 *
 * Automatically splits long text into multiple pages based on available display area.
 */
public class EntryPageWidget extends AbstractWidget {
    // Vanilla book.png texture for page navigation buttons
    private static final ResourceLocation BOOK_TEXTURE =
        CompatResourceLocation.create("minecraft", "textures/gui/book.png");
    private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
        CompatResourceLocation.create("minecraft", "widget/page_forward"),
        CompatResourceLocation.create("minecraft", "widget/page_forward_highlighted")
    );
    private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(
        CompatResourceLocation.create("minecraft", "widget/page_backward"),
        CompatResourceLocation.create("minecraft", "widget/page_backward_highlighted")
    );

    private Entry currentEntry;
    private int currentPageIndex = 0;

    // Virtual pages created by splitting long text pages
    private List<VirtualPage> virtualPages = new ArrayList<>();

    private Button previousButton;
    private Button nextButton;
    private Component pageNumberText;

    private static final int TEXT_MARGIN = 5;
    private static final int LINE_HEIGHT = 10;

    /**
     * Represents a virtual page (portion of original page that fits on screen).
     */
    private static class VirtualPage {
        final Page.PageType type;
        final List<String> lines; // For text pages
        final ResourceLocation recipe; // For recipe pages
        final ResourceLocation image; // For image pages

        VirtualPage(List<String> lines) {
            this.type = Page.PageType.TEXT;
            this.lines = lines;
            this.recipe = null;
            this.image = null;
        }

        VirtualPage(ResourceLocation recipe) {
            this.type = Page.PageType.RECIPE;
            this.lines = null;
            this.recipe = recipe;
            this.image = null;
        }

        VirtualPage(Page.PageType type, ResourceLocation image) {
            this.type = type;
            this.lines = null;
            this.recipe = null;
            this.image = image;
        }
    }

    public EntryPageWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    /**
     * Set the entry to display.
     * Automatically splits long text pages into multiple virtual pages.
     *
     * @param entry Entry to display (null to clear)
     */
    public void setEntry(Entry entry) {
        this.currentEntry = entry;
        this.currentPageIndex = 0;
        this.virtualPages.clear();

        if (entry != null) {
            // Calculate how many lines can fit on one page
            int maxTextHeight = height - 40; // Reserve space for page number
            int maxLinesPerPage = maxTextHeight / LINE_HEIGHT;

            Font font = Minecraft.getInstance().font;
            int maxLineWidth = width - (TEXT_MARGIN * 2);

            // Process each page in the entry
            for (Page page : entry.getPages()) {
                if (page.getType() == Page.PageType.TEXT) {
                    // Get text and wrap it
                    String languageCode = ChronicleScreen.getLanguageCode();
                    String text = page.getText().get(languageCode);
                    List<String> allLines = wrapText(text, maxLineWidth, font);

                    // Split wrapped lines into virtual pages
                    for (int i = 0; i < allLines.size(); i += maxLinesPerPage) {
                        int endIndex = Math.min(i + maxLinesPerPage, allLines.size());
                        List<String> pageLines = allLines.subList(i, endIndex);
                        virtualPages.add(new VirtualPage(new ArrayList<>(pageLines)));
                    }
                } else if (page.getType() == Page.PageType.RECIPE) {
                    // Recipe pages are not split
                    virtualPages.add(new VirtualPage(page.getRecipe()));
                } else if (page.getType() == Page.PageType.IMAGE) {
                    // Image pages are not split
                    virtualPages.add(new VirtualPage(Page.PageType.IMAGE, page.getImage()));
                }
            }
        }

        updatePageButtons();
    }

    /**
     * Initialize page navigation buttons.
     * Should be called from parent screen's init() method.
     */
    public void initializeButtons(Button.OnPress previousAction, Button.OnPress nextAction) {
        int buttonY = getY() + height - 15; // Align with left page bottom

        // Use vanilla book page navigation button images (23x13 pixels)
        this.previousButton = new SilentImageButton(
            getX() + 5, buttonY, 23, 13,
            PAGE_BACKWARD_SPRITES,
            previousAction
        );

        this.nextButton = new SilentImageButton(
            getX() + width - 28, buttonY, 23, 13,
            PAGE_FORWARD_SPRITES,
            nextAction
        );

        updatePageButtons();
    }

    /**
     * Custom ImageButton that doesn't play click sound.
     * Used for page navigation buttons where page turn sound is played separately.
     */
    private static class SilentImageButton extends ImageButton {
        public SilentImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
            super(x, y, width, height, sprites, onPress);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            // Disable button click sound (page turn sound is played separately)
        }
    }

    /**
     * Update button visibility based on current virtual page.
     */
    private void updatePageButtons() {
        if (previousButton != null && nextButton != null) {
            if (virtualPages.isEmpty()) {
                previousButton.visible = false;
                previousButton.active = false;
                nextButton.visible = false;
                nextButton.active = false;
                pageNumberText = Component.empty();
            } else {
                int totalPages = virtualPages.size();
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
        if (!virtualPages.isEmpty() && currentPageIndex > 0) {
            currentPageIndex--;
            updatePageButtons();
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F)
            );
        }
    }

    /**
     * Go to next page.
     */
    public void nextPage() {
        if (!virtualPages.isEmpty() && currentPageIndex < virtualPages.size() - 1) {
            currentPageIndex++;
            updatePageButtons();
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F)
            );
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (virtualPages.isEmpty()) {
            // No entry selected - show placeholder
            Component placeholderText = Component.translatable("gui.chronodawn.chronicle.select_category");
            Font font = Minecraft.getInstance().font;
            int textX = getX() + (width - font.width(placeholderText)) / 2;
            int textY = getY() + height / 2;
            graphics.drawString(font, placeholderText, textX, textY, 0x808080, false);
            return;
        }

        VirtualPage virtualPage = virtualPages.get(currentPageIndex);

        if (virtualPage.type == Page.PageType.TEXT) {
            renderTextVirtualPage(graphics, virtualPage);
        } else if (virtualPage.type == Page.PageType.RECIPE) {
            renderRecipeVirtualPage(graphics, virtualPage);
        } else if (virtualPage.type == Page.PageType.IMAGE) {
            renderImageVirtualPage(graphics, virtualPage);
        }

        // Render page number (aligned with navigation buttons)
        if (pageNumberText != null) {
            Font font = Minecraft.getInstance().font;
            int textX = getX() + (width - font.width(pageNumberText)) / 2;
            // Button Y position: height - 15, button height: 13, font height: ~9
            // Center text vertically within button: (13 - 9) / 2 = 2
            int textY = getY() + height - 15 + 2;
            graphics.drawString(font, pageNumberText, textX, textY, 0x3F3F3F, false);
        }
    }

    /**
     * Render a text virtual page (already wrapped and split).
     */
    private void renderTextVirtualPage(GuiGraphics graphics, VirtualPage virtualPage) {
        Font font = Minecraft.getInstance().font;
        int textX = getX() + TEXT_MARGIN;
        int textY = getY() + TEXT_MARGIN;

        // Render pre-wrapped lines (already fit on this page)
        for (String line : virtualPage.lines) {
            graphics.drawString(font, line, textX, textY, 0x000000, false);
            textY += LINE_HEIGHT;
        }
    }

    /**
     * Render a recipe virtual page showing the crafting recipe.
     */
    private void renderRecipeVirtualPage(GuiGraphics graphics, VirtualPage virtualPage) {
        // TODO: 1.21.2 - Recipe API changed significantly
        // - Level.getRecipeManager() removed
        // - ClientPacketListener.getRecipeManager() removed
        // - RecipeManager.byKey() signature changed
        // - Recipe.getResultItem() signature changed or removed
        // Need to investigate proper way to access recipes in 1.21.2

        // Temporarily show placeholder message
        Font font = Minecraft.getInstance().font;
        String placeholder = "Recipe display (TODO: Fix 1.21.2 Recipe API)";
        int maxLineWidth = width - (TEXT_MARGIN * 2);
        List<String> lines = wrapText(placeholder, maxLineWidth, font);

        int textX = getX() + TEXT_MARGIN;
        int textY = getY() + TEXT_MARGIN;

        for (String line : lines) {
            graphics.drawString(font, line, textX, textY, 0x808080, false);
            textY += LINE_HEIGHT;
        }
    }

    /**
     * Render an image virtual page with automatic scaling to fit page dimensions.
     * Maintains aspect ratio and applies sepia/beige tone to match book background.
     */
    private void renderImageVirtualPage(GuiGraphics graphics, VirtualPage virtualPage) {
        ResourceLocation imageLocation = virtualPage.image;

        try {
            // Calculate available space (leave margin and space for page number)
            int availableWidth = width - (TEXT_MARGIN * 2);
            int availableHeight = height - 50; // Reserve space for page number

            // Get image dimensions
            int[] dimensions = getImageDimensions(imageLocation);
            if (dimensions == null) {
                throw new Exception("Could not load image dimensions");
            }

            int imageWidth = dimensions[0];
            int imageHeight = dimensions[1];

            // Calculate scale factor to fit within available space while maintaining aspect ratio
            float scaleX = (float) availableWidth / imageWidth;
            float scaleY = (float) availableHeight / imageHeight;
            float scale = Math.min(scaleX, scaleY); // Use smaller scale to fit both dimensions

            // Calculate final render dimensions
            int renderWidth = (int) (imageWidth * scale);
            int renderHeight = (int) (imageHeight * scale);

            // Center the image on the page
            int renderX = getX() + (width - renderWidth) / 2;
            int renderY = getY() + (height - 40 - renderHeight) / 2;

            // Apply sepia/beige tone to match book background (0xF0E0D0)
            // RGB values: R=0.94, G=0.88, B=0.82 (slight warm tone)
            // 1.21.2: GuiGraphics.setColor() removed, use RenderSystem.setShaderColor() instead
            RenderSystem.setShaderColor(0.94f, 0.88f, 0.82f, 1.0f);

            // Use PoseStack to scale the image
            var poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(renderX, renderY, 0);
            poseStack.scale(scale, scale, 1.0f);

            // Render image at original size (will be scaled by matrix)
            graphics.blit(imageLocation, 0, 0, 0, 0,
                         imageWidth, imageHeight,
                         imageWidth, imageHeight);

            poseStack.popPose();

            // Reset color to white (default)
            // 1.21.2: GuiGraphics.setColor() removed, use RenderSystem.setShaderColor() instead
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            // Apply vignette effect (fade edges to match book background)
            renderVignetteEffect(graphics, renderX, renderY, renderWidth, renderHeight);

        } catch (Exception e) {
            // If image fails to load, show error message
            Font font = Minecraft.getInstance().font;
            String errorText = "Failed to load image: " + imageLocation;
            int maxLineWidth = width - (TEXT_MARGIN * 2);
            List<String> lines = wrapText(errorText, maxLineWidth, font);

            int textX = getX() + TEXT_MARGIN;
            int textY = getY() + TEXT_MARGIN;

            for (String line : lines) {
                graphics.drawString(font, line, textX, textY, 0xFF0000, false);
                textY += LINE_HEIGHT;
            }
        }
    }

    /**
     * Render vignette effect (fade edges) around the image for a sketch-like appearance.
     *
     * @param graphics GuiGraphics for rendering
     * @param x Image X position
     * @param y Image Y position
     * @param width Image width
     * @param height Image height
     */
    private void renderVignetteEffect(GuiGraphics graphics, int x, int y, int width, int height) {
        // Book background color (0xF0E0D0 = RGB 240, 224, 208)
        int bookBgColor = 0xF0E0D0;
        int r = (bookBgColor >> 16) & 0xFF;
        int g = (bookBgColor >> 8) & 0xFF;
        int b = bookBgColor & 0xFF;

        // Vignette fade distance (how far the fade extends inward)
        int fadeDistance = 15;

        // Top edge gradient (opaque to transparent, top to bottom)
        for (int i = 0; i <= fadeDistance; i++) {
            int alpha = (int) (255 * (1.0f - (float) i / fadeDistance));
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(x, y + i, x + width, y + i + 1, color);
        }

        // Bottom edge gradient (transparent to opaque, bottom to top)
        for (int i = 0; i <= fadeDistance; i++) {
            int alpha = (int) (255 * (1.0f - (float) i / fadeDistance));
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(x, y + height - i, x + width, y + height - i + 1, color);
        }

        // Left edge gradient (opaque to transparent, left to right)
        for (int i = 0; i <= fadeDistance; i++) {
            int alpha = (int) (255 * (1.0f - (float) i / fadeDistance));
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(x + i, y, x + i + 1, y + height, color);
        }

        // Right edge gradient (opaque to transparent, right to left)
        for (int i = 0; i <= fadeDistance; i++) {
            int alpha = (int) (255 * (1.0f - (float) i / fadeDistance));
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(x + width - i, y, x + width - i + 1, y + height, color);
        }

        // Corner fade (diagonal gradients for smoother corners)
        // Top-left corner (distance from top-left corner)
        for (int i = 0; i <= fadeDistance; i++) {
            for (int j = 0; j <= fadeDistance; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (distance <= fadeDistance) {
                    int alpha = (int) (255 * (1.0f - distance / fadeDistance));
                    int color = (alpha << 24) | (r << 16) | (g << 8) | b;
                    graphics.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                }
            }
        }

        // Top-right corner (distance from top-right corner)
        for (int i = 0; i <= fadeDistance; i++) {
            for (int j = 0; j <= fadeDistance; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (distance <= fadeDistance) {
                    int alpha = (int) (255 * (1.0f - distance / fadeDistance));
                    int color = (alpha << 24) | (r << 16) | (g << 8) | b;
                    graphics.fill(x + width - 1 - i, y + j, x + width - i, y + j + 1, color);
                }
            }
        }

        // Bottom-left corner (distance from bottom-left corner)
        for (int i = 0; i <= fadeDistance; i++) {
            for (int j = 0; j <= fadeDistance; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (distance <= fadeDistance) {
                    int alpha = (int) (255 * (1.0f - distance / fadeDistance));
                    int color = (alpha << 24) | (r << 16) | (g << 8) | b;
                    graphics.fill(x + i, y + height - 1 - j, x + i + 1, y + height - j, color);
                }
            }
        }

        // Bottom-right corner (distance from bottom-right corner)
        for (int i = 0; i <= fadeDistance; i++) {
            for (int j = 0; j <= fadeDistance; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (distance <= fadeDistance) {
                    int alpha = (int) (255 * (1.0f - distance / fadeDistance));
                    int color = (alpha << 24) | (r << 16) | (g << 8) | b;
                    graphics.fill(x + width - 1 - i, y + height - 1 - j,
                                 x + width - i, y + height - j, color);
                }
            }
        }
    }

    /**
     * Get the dimensions of an image texture.
     *
     * @param imageLocation Resource location of the image
     * @return int array [width, height], or null if failed
     */
    private int[] getImageDimensions(ResourceLocation imageLocation) {
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

            // Convert texture location to resource location
            // textures/gui/chronicle/image.png -> assets/chronodawn/textures/gui/chronicle/image.png
            ResourceLocation resourcePath = CompatResourceLocation.create(
                imageLocation.getNamespace(),
                imageLocation.getPath()
            );

            // Open the image file
            try (InputStream stream = resourceManager.open(resourcePath)) {
                NativeImage image = NativeImage.read(stream);
                int width = image.getWidth();
                int height = image.getHeight();
                image.close();
                return new int[]{width, height};
            }
        } catch (IOException e) {
            return null;
        }
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
    public void playDownSound(SoundManager soundManager) {
        // Disable click sound for the entry page widget itself
        // (Navigation buttons have their own sound handling)
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
