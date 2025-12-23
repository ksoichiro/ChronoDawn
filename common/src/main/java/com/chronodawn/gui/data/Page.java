package com.chronodawn.gui.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a single page in a Chronicle entry.
 * Pages can contain either text or a recipe reference.
 */
public class Page {
    private final PageType type;
    private final LocalizedText text;
    private final ResourceLocation recipe;

    public Page(JsonObject json) {
        // Determine page type
        if (json.has("text")) {
            this.type = PageType.TEXT;
            this.text = new LocalizedText(json.getAsJsonObject("text"));
            this.recipe = null;
        } else if (json.has("recipe")) {
            this.type = PageType.RECIPE;
            this.text = null;
            this.recipe = ResourceLocation.parse(json.get("recipe").getAsString());
        } else {
            // Fallback to empty text page
            this.type = PageType.TEXT;
            this.text = new LocalizedText("");
            this.recipe = null;
        }
    }

    public PageType getType() {
        return type;
    }

    public LocalizedText getText() {
        return text;
    }

    public ResourceLocation getRecipe() {
        return recipe;
    }

    public enum PageType {
        TEXT,
        RECIPE
    }
}
