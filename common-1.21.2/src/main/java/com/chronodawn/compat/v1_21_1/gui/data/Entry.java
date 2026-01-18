package com.chronodawn.compat.v1_21_1.gui.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single entry in a Chronicle category.
 * An entry contains a title, icon, and one or more pages.
 */
public class Entry {
    private final String id;
    private final String category;
    private final LocalizedText title;
    private final ResourceLocation icon;
    private final List<Page> pages;
    private final int sortnum;

    public Entry(String id, JsonObject json) {
        this.id = id;
        this.category = json.get("category").getAsString();
        this.title = new LocalizedText(json.getAsJsonObject("title"));
        this.icon = ResourceLocation.parse(json.get("icon").getAsString());
        this.sortnum = json.has("sortnum") ? json.get("sortnum").getAsInt() : 0;

        // Parse pages
        this.pages = new ArrayList<>();
        if (json.has("pages")) {
            JsonArray pagesArray = json.getAsJsonArray("pages");
            for (JsonElement pageElement : pagesArray) {
                pages.add(new Page(pageElement.getAsJsonObject()));
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public LocalizedText getTitle() {
        return title;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public List<Page> getPages() {
        return pages;
    }

    public int getSortnum() {
        return sortnum;
    }
}
