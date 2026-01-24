package com.chronodawn.gui.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents localized text data for Chronicle guidebook.
 * Supports multiple languages with fallback to English.
 */
public class LocalizedText {
    private final Map<String, String> translations;
    private final String defaultText;

    public LocalizedText(JsonObject json) {
        this.translations = new HashMap<>();

        // Parse all language keys
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String lang = entry.getKey();
            String text = entry.getValue().getAsString();
            translations.put(lang, text);
        }

        // Default to English, or first available translation
        this.defaultText = translations.getOrDefault("en_us",
            translations.values().isEmpty() ? "" : translations.values().iterator().next());
    }

    public LocalizedText(String text) {
        this.translations = new HashMap<>();
        this.translations.put("en_us", text);
        this.defaultText = text;
    }

    /**
     * Get text for the specified language code.
     * Falls back to English if the requested language is not available.
     *
     * @param languageCode Language code (e.g., "en_us", "ja_jp")
     * @return Localized text
     */
    public String get(String languageCode) {
        return translations.getOrDefault(languageCode, defaultText);
    }

    /**
     * Get default text (English or first available).
     *
     * @return Default text
     */
    public String getDefault() {
        return defaultText;
    }
}
