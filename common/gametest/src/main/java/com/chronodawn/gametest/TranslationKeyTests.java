package com.chronodawn.gametest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Shared translation key test generator used across all Minecraft versions.
 *
 * Verifies that all items have valid translation keys at runtime by calling
 * getDescriptionId() on the actual item instance and checking the returned
 * key exists in the specified language file.
 */
public final class TranslationKeyTests {

    private TranslationKeyTests() {
        // Utility class
    }

    @FunctionalInterface
    public interface TestFactory<T> {
        T create(String name, Consumer<GameTestHelper> test);
    }

    private static final List<String> SUPPORTED_LANGUAGES = List.of("en_us", "ja_jp");

    /**
     * Generates tests verifying that all items in the given registry class
     * have valid translation keys defined in all supported language files (en_us.json, ja_jp.json).
     *
     * @param itemsClass the registry class containing RegistrySupplier fields
     * @param idOverrides field name to ID overrides for test naming
     * @param factory factory to create test instances
     * @return list of generated tests for all supported languages
     */
    public static <T> List<T> generate(
            Class<?> itemsClass,
            Map<String, String> idOverrides,
            TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (String langCode : SUPPORTED_LANGUAGES) {
            tests.addAll(generateForLanguage(itemsClass, idOverrides, factory, langCode));
        }
        return tests;
    }

    private static <T> List<T> generateForLanguage(
            Class<?> itemsClass,
            Map<String, String> idOverrides,
            TestFactory<T> factory,
            String langCode) {
        Map<String, String> langMap = loadLangFile(langCode);

        List<T> tests = new ArrayList<>();
        for (Field field : itemsClass.getDeclaredFields()) {
            if (!isRegistrySupplierField(field)) continue;
            String fieldName = field.getName();
            String expectedId = idOverrides.getOrDefault(fieldName, fieldName.toLowerCase());
            String testName = "translation_key_" + langCode + "_" + expectedId;
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    try {
                        @SuppressWarnings("unchecked")
                        RegistrySupplier<Item> supplier = (RegistrySupplier<Item>) field.get(null);
                        Item item = supplier.get();
                        String descriptionId = item.getDescriptionId();
                        if (langMap.containsKey(descriptionId)) {
                            helper.succeed();
                        } else {
                            helper.fail(fieldName + " has descriptionId \"" + descriptionId +
                                "\" which is not in " + langCode + ".json");
                        }
                    } catch (Exception e) {
                        helper.fail("Failed to check translation for " + fieldName +
                            ": " + e.getMessage());
                    }
                });
            }));
        }
        return tests;
    }

    private static Map<String, String> loadLangFile(String langCode) {
        try (InputStream is = TranslationKeyTests.class.getResourceAsStream(
                "/assets/chronodawn/lang/" + langCode + ".json")) {
            if (is == null) return Collections.emptyMap();
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            return gson.fromJson(content,
                new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static boolean isRegistrySupplierField(Field field) {
        return Modifier.isPublic(field.getModifiers())
            && Modifier.isStatic(field.getModifiers())
            && Modifier.isFinal(field.getModifiers())
            && RegistrySupplier.class.isAssignableFrom(field.getType());
    }
}
