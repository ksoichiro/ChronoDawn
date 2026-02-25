package com.chronodawn.gui;

import com.chronodawn.gui.data.ChronicleData;
import com.chronodawn.gui.data.Entry;
import com.chronodawn.gui.widgets.CategoryListWidget;
import com.chronodawn.gui.widgets.EntryPageWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

import java.util.List;

/**
 * Main GUI screen for the Chronicle guidebook.
 * Displays categories in the left sidebar and entry content on the right.
 */
public class ChronicleScreen extends Screen {
    // Use 12-part parchment-style book textures for maximum flexibility (Nine-patch-like structure)
    // Corners (4 parts - fixed size 16x16)
    private static final ResourceLocation CORNER_TOP_LEFT =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/corner_top_left.png");
    private static final ResourceLocation CORNER_TOP_RIGHT =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/corner_top_right.png");
    private static final ResourceLocation CORNER_BOTTOM_LEFT =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/corner_bottom_left.png");
    private static final ResourceLocation CORNER_BOTTOM_RIGHT =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/corner_bottom_right.png");
    // Edges (4 parts - tileable 16x16)
    private static final ResourceLocation EDGE_TOP =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/edge_top.png");
    private static final ResourceLocation EDGE_BOTTOM =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/edge_bottom.png");
    private static final ResourceLocation EDGE_LEFT =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/edge_left.png");
    private static final ResourceLocation EDGE_RIGHT =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/edge_right.png");
    // Page background (1 part - tileable 16x16)
    private static final ResourceLocation PAGE_BACKGROUND =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/page.png");
    // Binding shadow (3 parts - top/middle/bottom for seamless corners)
    private static final ResourceLocation BINDING_TOP =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/binding_top.png");
    private static final ResourceLocation BINDING_MIDDLE =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/binding_middle.png");
    private static final ResourceLocation BINDING_BOTTOM =
        ResourceLocation.fromNamespaceAndPath("chronodawn", "textures/gui/chronicle/binding_bottom.png");
    private static final int BOOK_WIDTH = 320;
    private static final int BOOK_HEIGHT = 240;

    private final ChronicleData data;
    private int leftPos;
    private int topPos;
    private int categoryListWidth;

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
        this.categoryListWidth = (BOOK_WIDTH - 30) / 2; // 30 = left margin (10) + center margin (20)
        int categoryListHeight = BOOK_HEIGHT - 20; // Match right page height
        this.categoryList = new CategoryListWidget(
            leftPos + 10,
            topPos + 10, // Match right page start position
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

        // Initialize entry page widget (right page can start higher since title is on left page)
        int entryPageX = leftPos + categoryListWidth + 20;
        int entryPageWidth = BOOK_WIDTH - categoryListWidth - 30;
        int entryPageHeight = BOOK_HEIGHT - 20; // Expanded height (was BOOK_HEIGHT - 40)
        this.entryPage = new EntryPageWidget(
            entryPageX,
            topPos + 10, // Start higher (was topPos + 30)
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

        // Layer 2: Render book background using 12-part parchment-style textures
        // This Nine-patch-like structure allows precise positioning and easy customization

        final int CORNER_SIZE = 16;
        final int EDGE_SIZE = 16;
        final int BINDING_WIDTH = 16;

        // Calculate binding position (between left and right pages)
        int bindingX = leftPos + categoryListWidth + 10; // Position after category list

        // Calculate dimensions
        int innerWidth = BOOK_WIDTH - (CORNER_SIZE * 2);
        int innerHeight = BOOK_HEIGHT - (CORNER_SIZE * 2);
        int leftPageWidth = bindingX - leftPos - CORNER_SIZE;
        int rightPageWidth = BOOK_WIDTH - (bindingX - leftPos) - BINDING_WIDTH - CORNER_SIZE;

        // === Render page backgrounds (behind everything, tiled) ===
        // Left page background (tiled 16x16)
        for (int y = 0; y < innerHeight; y += EDGE_SIZE) {
            for (int x = 0; x < leftPageWidth; x += EDGE_SIZE) {
                int width = Math.min(EDGE_SIZE, leftPageWidth - x);
                int height = Math.min(EDGE_SIZE, innerHeight - y);
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    PAGE_BACKGROUND,
                    leftPos + CORNER_SIZE + x, topPos + CORNER_SIZE + y,  // Position
                    0.0f, 0.0f,                                            // UV
                    width, height,                                         // Size
                    16, 16                                                 // Texture size
                );
            }
        }

        // Right page background (tiled 16x16)
        for (int y = 0; y < innerHeight; y += EDGE_SIZE) {
            for (int x = 0; x < rightPageWidth; x += EDGE_SIZE) {
                int width = Math.min(EDGE_SIZE, rightPageWidth - x);
                int height = Math.min(EDGE_SIZE, innerHeight - y);
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    PAGE_BACKGROUND,
                    bindingX + BINDING_WIDTH + x, topPos + CORNER_SIZE + y, // Position
                    0.0f, 0.0f,                                              // UV
                    width, height,                                           // Size
                    16, 16                                                   // Texture size
                );
            }
        }

        // === Render corners (fixed size) ===
        // Top-left corner
        graphics.blit(RenderPipelines.GUI_TEXTURED, CORNER_TOP_LEFT, leftPos, topPos, 0.0f, 0.0f, CORNER_SIZE, CORNER_SIZE, 16, 16);
        // Top-right corner
        graphics.blit(RenderPipelines.GUI_TEXTURED, CORNER_TOP_RIGHT, leftPos + BOOK_WIDTH - CORNER_SIZE, topPos, 0.0f, 0.0f, CORNER_SIZE, CORNER_SIZE, 16, 16);
        // Bottom-left corner
        graphics.blit(RenderPipelines.GUI_TEXTURED, CORNER_BOTTOM_LEFT, leftPos, topPos + BOOK_HEIGHT - CORNER_SIZE, 0.0f, 0.0f, CORNER_SIZE, CORNER_SIZE, 16, 16);
        // Bottom-right corner
        graphics.blit(RenderPipelines.GUI_TEXTURED, CORNER_BOTTOM_RIGHT, leftPos + BOOK_WIDTH - CORNER_SIZE, topPos + BOOK_HEIGHT - CORNER_SIZE, 0.0f, 0.0f, CORNER_SIZE, CORNER_SIZE, 16, 16);

        // === Render binding shadow (in the center, 3 parts spanning full height) ===
        // Binding top (same height as top edge)
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            BINDING_TOP,
            bindingX, topPos,                   // Position (same height as top corners)
            0.0f, 0.0f,                          // UV
            BINDING_WIDTH, CORNER_SIZE,          // Size (16x16)
            16, 16                               // Texture size
        );

        // Binding middle (tiled vertically between top and bottom)
        int bindingMiddleHeight = BOOK_HEIGHT - (CORNER_SIZE * 2);
        for (int y = 0; y < bindingMiddleHeight; y += EDGE_SIZE) {
            int height = Math.min(EDGE_SIZE, bindingMiddleHeight - y);
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BINDING_MIDDLE,
                bindingX, topPos + CORNER_SIZE + y,  // Position
                0.0f, 0.0f,                           // UV
                BINDING_WIDTH, height,                // Size
                16, 16                                // Texture size
            );
        }

        // Binding bottom (same height as bottom edge)
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            BINDING_BOTTOM,
            bindingX, topPos + BOOK_HEIGHT - CORNER_SIZE, // Position (same height as bottom corners)
            0.0f, 0.0f,                                    // UV
            BINDING_WIDTH, CORNER_SIZE,                    // Size (16x16)
            16, 16                                         // Texture size
        );

        // === Render edges (tiled, split by binding) ===
        // Top edge - left side (from left corner to binding)
        for (int x = CORNER_SIZE; x < bindingX - leftPos; x += EDGE_SIZE) {
            int width = Math.min(EDGE_SIZE, bindingX - leftPos - x);
            graphics.blit(RenderPipelines.GUI_TEXTURED, EDGE_TOP, leftPos + x, topPos, 0.0f, 0.0f, width, EDGE_SIZE, 16, 16);
        }
        // Top edge - right side (from binding to right corner)
        for (int x = bindingX - leftPos + BINDING_WIDTH; x < BOOK_WIDTH - CORNER_SIZE; x += EDGE_SIZE) {
            int width = Math.min(EDGE_SIZE, BOOK_WIDTH - CORNER_SIZE - x);
            graphics.blit(RenderPipelines.GUI_TEXTURED, EDGE_TOP, leftPos + x, topPos, 0.0f, 0.0f, width, EDGE_SIZE, 16, 16);
        }

        // Bottom edge - left side (from left corner to binding)
        for (int x = CORNER_SIZE; x < bindingX - leftPos; x += EDGE_SIZE) {
            int width = Math.min(EDGE_SIZE, bindingX - leftPos - x);
            graphics.blit(RenderPipelines.GUI_TEXTURED, EDGE_BOTTOM, leftPos + x, topPos + BOOK_HEIGHT - EDGE_SIZE, 0.0f, 0.0f, width, EDGE_SIZE, 16, 16);
        }
        // Bottom edge - right side (from binding to right corner)
        for (int x = bindingX - leftPos + BINDING_WIDTH; x < BOOK_WIDTH - CORNER_SIZE; x += EDGE_SIZE) {
            int width = Math.min(EDGE_SIZE, BOOK_WIDTH - CORNER_SIZE - x);
            graphics.blit(RenderPipelines.GUI_TEXTURED, EDGE_BOTTOM, leftPos + x, topPos + BOOK_HEIGHT - EDGE_SIZE, 0.0f, 0.0f, width, EDGE_SIZE, 16, 16);
        }

        // Left edge (tiled vertically, full height between corners)
        for (int y = CORNER_SIZE; y < BOOK_HEIGHT - CORNER_SIZE; y += EDGE_SIZE) {
            int height = Math.min(EDGE_SIZE, BOOK_HEIGHT - CORNER_SIZE - y);
            graphics.blit(RenderPipelines.GUI_TEXTURED, EDGE_LEFT, leftPos, topPos + y, 0.0f, 0.0f, EDGE_SIZE, height, 16, 16);
        }
        // Right edge (tiled vertically, full height between corners)
        for (int y = CORNER_SIZE; y < BOOK_HEIGHT - CORNER_SIZE; y += EDGE_SIZE) {
            int height = Math.min(EDGE_SIZE, BOOK_HEIGHT - CORNER_SIZE - y);
            graphics.blit(RenderPipelines.GUI_TEXTURED, EDGE_RIGHT, leftPos + BOOK_WIDTH - EDGE_SIZE, topPos + y, 0.0f, 0.0f, EDGE_SIZE, height, 16, 16);
        }

        // Layer 3: Render widgets (category list, entry pages, buttons) on top
        super.render(graphics, mouseX, mouseY, partialTick);

        // Layer 4: Render tooltips (must be last to appear on top)
        // TODO: 1.21.6 - renderTooltip signature changed, needs investigation
        // Old: graphics.renderTooltip(Font, List<Component>, int, int)
        // New: graphics.renderTooltip(Font, List<ClientTooltipComponent>, int, int, ClientTooltipPositioner, ResourceLocation)
        if (categoryList != null) {
            String tooltip = categoryList.getHoveredTooltip();
            if (tooltip != null) {
                graphics.renderTooltip(
                    this.font,
                    List.of(ClientTooltipComponent.create(Component.literal(tooltip).getVisualOrderText())),
                    mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
                );
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
