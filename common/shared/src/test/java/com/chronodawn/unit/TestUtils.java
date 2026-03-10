package com.chronodawn.unit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared test utilities for resource validation tests.
 * Provides methods to parse registry source files and load JSON resources
 * without requiring Minecraft class loading.
 */
public final class TestUtils {

    // Pattern to match: public static final RegistrySupplier<...> FIELD_NAME = ...
    // Uses >+ to handle nested generics like RegistrySupplier<EntityType<SomeEntity>>
    private static final Pattern REGISTRY_FIELD_PATTERN = Pattern.compile(
        "public\\s+static\\s+final\\s+RegistrySupplier<[^>]+>+\\s+(\\w+)\\s*="
    );

    // Known cases where field name doesn't match registry ID
    private static final Map<String, String> ID_OVERRIDES = Map.of(
        "FRUIT_OF_TIME_BLOCK", "fruit_of_time",
        "CHRONO_DAWN_BOAT", "chronodawn_boat",
        "CHRONO_DAWN_CHEST_BOAT", "chronodawn_chest_boat"
    );

    private static final Gson GSON = new Gson();

    private TestUtils() {}

    /**
     * Get the source directory from system property.
     */
    public static String getSourceDir() {
        String sourceDir = System.getProperty("chronodawn.source.dir");
        if (sourceDir == null) {
            throw new IllegalStateException(
                "System property 'chronodawn.source.dir' not set. " +
                "Run tests via Gradle: ./gradlew :common-<version>:test"
            );
        }
        return sourceDir;
    }

    /**
     * Get the project root directory from system property.
     */
    public static String getProjectRoot() {
        String root = System.getProperty("chronodawn.project.root");
        if (root == null) {
            throw new IllegalStateException(
                "System property 'chronodawn.project.root' not set. " +
                "Run tests via Gradle: ./gradlew :common-<version>:test"
            );
        }
        return root;
    }

    /**
     * Parse a registry source file to extract RegistrySupplier field names.
     */
    public static List<String> getFieldNames(String fileName) {
        return getFieldNamesFromDir(getSourceDir(), fileName);
    }

    /**
     * Parse a registry source file from a specific directory.
     */
    public static List<String> getFieldNamesFromDir(String sourceDir, String fileName) {
        Path filePath = Paths.get(sourceDir, "com", "chronodawn", "registry", fileName);
        List<String> fieldNames = new ArrayList<>();
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            Matcher matcher = REGISTRY_FIELD_PATTERN.matcher(content);
            while (matcher.find()) {
                fieldNames.add(matcher.group(1));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read source file: " + filePath, e);
        }
        return fieldNames;
    }

    /**
     * Convert a field name to its registry ID.
     */
    public static String toRegistryId(String fieldName) {
        return ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
    }

    /**
     * Get all registered block IDs (with chronodawn: prefix).
     */
    public static Set<String> getBlockIds() {
        Set<String> ids = new HashSet<>();
        for (String field : getFieldNames("ModBlocks.java")) {
            ids.add("chronodawn:" + toRegistryId(field));
        }
        return ids;
    }

    /**
     * Get all registered item IDs (with chronodawn: prefix).
     */
    public static Set<String> getItemIds() {
        Set<String> ids = new HashSet<>();
        for (String field : getFieldNames("ModItems.java")) {
            ids.add("chronodawn:" + toRegistryId(field));
        }
        return ids;
    }

    /**
     * Get all registered entity IDs (with chronodawn: prefix).
     */
    public static Set<String> getEntityIds() {
        Set<String> ids = new HashSet<>();
        for (String field : getFieldNames("ModEntities.java")) {
            ids.add("chronodawn:" + toRegistryId(field));
        }
        return ids;
    }

    /**
     * Get the combined set of all item and block IDs.
     * Items include block items, so this covers all valid item references.
     */
    public static Set<String> getAllItemAndBlockIds() {
        Set<String> ids = getItemIds();
        ids.addAll(getBlockIds());
        return ids;
    }

    /**
     * Load and parse a JSON file from classpath.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadJsonResource(String resourcePath) {
        try (InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            return GSON.fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                new TypeToken<Map<String, Object>>() {}.getType()
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Load a lang file from classpath.
     */
    public static Map<String, String> loadLangFile() {
        try (InputStream is = TestUtils.class.getClassLoader()
                .getResourceAsStream("assets/chronodawn/lang/en_us.json")) {
            if (is == null) return Collections.emptyMap();
            return GSON.fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                new TypeToken<Map<String, String>>() {}.getType()
            );
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Find all JSON resource files matching a classpath prefix pattern.
     * Uses the file system to scan resource directories since classpath scanning
     * is not reliable for listing resources.
     *
     * @param resourcePrefix e.g. "data/chronodawn/recipe/"
     * @return list of resource paths relative to classpath root
     */
    public static List<String> findJsonResources(String resourcePrefix) {
        List<String> results = new ArrayList<>();
        String projectRoot = getProjectRoot();
        String mcVersion = System.getProperty("chronodawn.minecraft.version", "");

        // Determine which shared resource directories to scan based on version
        List<String> resourceDirs = new ArrayList<>();
        // Version-specific resources first
        resourceDirs.add(Paths.get(projectRoot, "common", mcVersion, "src", "main", "resources").toString());
        // Shared resources (from newest to oldest)
        if (compareVersions(mcVersion, "1.21.5") >= 0) {
            resourceDirs.add(Paths.get(projectRoot, "common", "shared-1.21.5+", "src", "main", "resources").toString());
        }
        if (compareVersions(mcVersion, "1.21.2") >= 0) {
            resourceDirs.add(Paths.get(projectRoot, "common", "shared-1.21.2+", "src", "main", "resources").toString());
        }
        if (compareVersions(mcVersion, "1.21.1") >= 0) {
            resourceDirs.add(Paths.get(projectRoot, "common", "shared-1.21.1+", "src", "main", "resources").toString());
        }
        resourceDirs.add(Paths.get(projectRoot, "common", "shared", "src", "main", "resources").toString());

        Set<String> seen = new HashSet<>();
        for (String dir : resourceDirs) {
            Path base = Paths.get(dir, resourcePrefix);
            if (!Files.isDirectory(base)) continue;
            try (var stream = Files.walk(base)) {
                stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        String relative = resourcePrefix + base.relativize(p).toString();
                        if (seen.add(relative)) {
                            results.add(relative);
                        }
                    });
            } catch (IOException e) {
                // Skip directories that can't be read
            }
        }
        return results;
    }

    /**
     * Compare two Minecraft version strings semantically.
     * Handles versions like "1.21.10" > "1.21.5" correctly
     * (unlike String.compareTo which would give "1.21.10" < "1.21.5").
     */
    public static int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int len = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < len; i++) {
            int n1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int n2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }

    /**
     * Extract all chronodawn: references from a JSON object recursively.
     */
    @SuppressWarnings("unchecked")
    public static Set<String> extractChronodawnReferences(Object obj) {
        Set<String> refs = new HashSet<>();
        if (obj instanceof String) {
            String s = (String) obj;
            if (s.startsWith("chronodawn:")) {
                refs.add(s);
            } else if (s.startsWith("#chronodawn:")) {
                // Tag reference, not a direct ID
            }
        } else if (obj instanceof Map) {
            for (Object value : ((Map<String, Object>) obj).values()) {
                refs.addAll(extractChronodawnReferences(value));
            }
        } else if (obj instanceof List) {
            for (Object item : (List<Object>) obj) {
                refs.addAll(extractChronodawnReferences(item));
            }
        }
        return refs;
    }
}
