package com.chronodawn.gui.data;

import com.chronodawn.ChronoDawn;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Loader and cache for Chronicle guidebook data.
 * Loads category and entry data from JSON files in resource packs.
 */
public class ChronicleData {
    private static final Gson GSON = new Gson();
    private static final String CATEGORIES_PATH = "assets/chronodawn/chronicle/categories.json";
    private static final String ENTRIES_PATH_PREFIX = "assets/chronodawn/chronicle/entries/";

    private final Map<String, Category> categories = new LinkedHashMap<>();
    private boolean loaded = false;

    private static ChronicleData instance;

    private ChronicleData() {}

    public static ChronicleData getInstance() {
        if (instance == null) {
            instance = new ChronicleData();
        }
        return instance;
    }

    /**
     * Load all Chronicle data from the resource manager.
     * This should be called during resource reload.
     *
     * @param resourceManager Resource manager
     */
    public void load(ResourceManager resourceManager) {
        categories.clear();
        loaded = false;

        try {
            // Load categories
            loadCategories(resourceManager);

            // Load entries for each category
            loadEntries(resourceManager);

            loaded = true;

            // Count total entries
            int totalEntries = categories.values().stream()
                .mapToInt(category -> category.getEntries().size())
                .sum();

            ChronoDawn.LOGGER.info("Loaded {} Chronicle categories with {} total entries",
                categories.size(), totalEntries);
        } catch (IOException e) {
            ChronoDawn.LOGGER.error("Failed to load Chronicle data", e);
        }
    }

    private void loadCategories(ResourceManager resourceManager) throws IOException {
        ResourceLocation categoriesLocation = CompatResourceLocation.create("chronodawn", "chronicle/categories.json");

        Optional<Resource> resource = resourceManager.getResource(categoriesLocation);
        if (resource.isEmpty()) {
            ChronoDawn.LOGGER.warn("Categories file not found: {}", CATEGORIES_PATH);
            return;
        }

        try (BufferedReader reader = resource.get().openAsReader()) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                String categoryId = entry.getKey();
                JsonObject categoryJson = entry.getValue().getAsJsonObject();
                Category category = new Category(categoryId, categoryJson);
                categories.put(categoryId, category);
            }
        }
    }

    private void loadEntries(ResourceManager resourceManager) throws IOException {
        // For each category, search for entry JSON files
        for (String categoryId : categories.keySet()) {
            String entryPathPrefix = "chronicle/entries/" + categoryId + "/";

            // Search for all JSON files in the category directory
            // Note: ResourceManager doesn't have a directory listing API,
            // so we'll need to try loading specific entry files
            // For now, we'll implement a simple approach that works with known entries

            // Try to discover entries by looking at resource paths
            // This is a limitation of ResourceManager - in production, we'd use a registry approach
            loadEntriesForCategory(resourceManager, categoryId);
        }
    }

    private void loadEntriesForCategory(ResourceManager resourceManager, String categoryId) {
        // Define all entry names for each category
        // Note: ResourceManager doesn't provide directory listing, so we enumerate explicitly
        Map<String, String[]> categoryEntries = new HashMap<>();

        // Getting Started category
        categoryEntries.put("basics", new String[]{
            "welcome", "ancient_ruins", "time_hourglass",
            "portal_building", "portal_activation", "escaping"
        });

        // Progression category
        categoryEntries.put("progression", new String[]{
            "time_distortion", "biomes", "wood_types",
            "forgotten_library", "portal_stabilizer", "progression_path"
        });

        // Structures category
        categoryEntries.put("structures", new String[]{
            "desert_clock_tower", "guardian_vault", "clockwork_depths",
            "phantom_catacombs", "entropy_crypt", "master_clock"
        });

        // Boss Battles category
        categoryEntries.put("bosses", new String[]{
            "time_guardian", "chronos_warden", "clockwork_colossus",
            "temporal_phantom", "entropy_keeper", "time_tyrant"
        });

        // Items & Equipment category
        categoryEntries.put("items", new String[]{
            "clockstone", "time_crystals", "chrono_aegis", "ultimate_artifacts"
        });

        String[] entryNames = categoryEntries.getOrDefault(categoryId, new String[0]);

        for (String entryName : entryNames) {
            ResourceLocation entryLocation = CompatResourceLocation.create(
                "chronodawn",
                "chronicle/entries/" + categoryId + "/" + entryName + ".json"
            );

            try {
                Optional<Resource> resource = resourceManager.getResource(entryLocation);
                if (resource.isPresent()) {
                    try (BufferedReader reader = resource.get().openAsReader()) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        Entry entry = new Entry(entryName, json);

                        // Add entry to its category
                        Category category = categories.get(categoryId);
                        if (category != null) {
                            category.addEntry(entry);
                        }
                    }
                }
            } catch (IOException e) {
                ChronoDawn.LOGGER.warn("Failed to load entry: {}/{}", categoryId, entryName);
            }
        }
    }

    /**
     * Get all categories in sorted order.
     *
     * @return List of categories
     */
    public List<Category> getCategories() {
        List<Category> categoryList = new ArrayList<>(categories.values());
        categoryList.sort((a, b) -> Integer.compare(a.getSortnum(), b.getSortnum()));
        return categoryList;
    }

    /**
     * Get a specific category by ID.
     *
     * @param id Category ID
     * @return Category, or null if not found
     */
    public Category getCategory(String id) {
        return categories.get(id);
    }

    /**
     * Check if data has been loaded successfully.
     *
     * @return true if loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Reload all Chronicle data.
     * This is called when resources are reloaded (F3+T).
     */
    public void reload(ResourceManager resourceManager) {
        load(resourceManager);
    }
}
