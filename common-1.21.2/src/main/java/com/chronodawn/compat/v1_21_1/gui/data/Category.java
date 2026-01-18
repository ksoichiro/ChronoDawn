package com.chronodawn.compat.v1_21_1.gui.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a category in the Chronicle guidebook.
 * Categories contain a list of entries and are displayed in the left sidebar.
 */
public class Category {
    private final String id;
    private final LocalizedText title;
    private final LocalizedText description;
    private final ResourceLocation icon;
    private final int sortnum;
    private final List<Entry> entries;

    public Category(String id, JsonObject json) {
        this.id = id;
        this.title = new LocalizedText(json.getAsJsonObject("title"));
        this.description = json.has("description") ?
            new LocalizedText(json.getAsJsonObject("description")) :
            new LocalizedText("");
        this.icon = ResourceLocation.parse(json.get("icon").getAsString());
        this.sortnum = json.has("sortnum") ? json.get("sortnum").getAsInt() : 0;
        this.entries = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public LocalizedText getTitle() {
        return title;
    }

    public LocalizedText getDescription() {
        return description;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public int getSortnum() {
        return sortnum;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
        // Sort entries by sortnum
        entries.sort((a, b) -> Integer.compare(a.getSortnum(), b.getSortnum()));
    }
}
