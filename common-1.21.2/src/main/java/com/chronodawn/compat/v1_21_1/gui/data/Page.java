package com.chronodawn.compat.v1_21_1.gui.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a single page in a Chronicle entry.
 * Pages can contain text, a recipe reference, or an image.
 */
public class Page {
    private final PageType type;
    private final LocalizedText text;
    private final ResourceLocation recipe;
    private final ResourceLocation image;

    public Page(JsonObject json) {
        // Determine page type
        if (json.has("text")) {
            this.type = PageType.TEXT;
            this.text = new LocalizedText(json.getAsJsonObject("text"));
            this.recipe = null;
            this.image = null;
        } else if (json.has("recipe")) {
            this.type = PageType.RECIPE;
            this.text = null;
            this.recipe = ResourceLocation.parse(json.get("recipe").getAsString());
            this.image = null;
        } else if (json.has("image")) {
            this.type = PageType.IMAGE;
            this.text = null;
            this.recipe = null;
            this.image = ResourceLocation.parse(json.get("image").getAsString());
        } else {
            // Fallback to empty text page
            this.type = PageType.TEXT;
            this.text = new LocalizedText("");
            this.recipe = null;
            this.image = null;
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

    public ResourceLocation getImage() {
        return image;
    }

    public enum PageType {
        TEXT,
        RECIPE,
        IMAGE
    }
}
