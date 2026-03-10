package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates that all supported Minecraft versions register the same set of
 * blocks, items, and entities. Detects accidental omissions when adding
 * content to one version but forgetting others.
 */
public class CrossVersionConsistencyTest {

    private static final String[] REGISTRY_FILES = {
        "ModBlocks.java", "ModItems.java", "ModEntities.java"
    };

    // All version directories that contain registry source files
    private static final String[] VERSION_DIRS = {
        "1.20.1", "1.21.1", "1.21.2", "1.21.4", "1.21.5",
        "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11"
    };

    @TestFactory
    Collection<DynamicTest> crossVersionRegistryConsistencyTests() {
        String projectRoot = TestUtils.getProjectRoot();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String registryFile : REGISTRY_FILES) {
            String registryName = registryFile.replace(".java", "");

            // Collect field names for each version
            Map<String, Set<String>> versionFields = new LinkedHashMap<>();
            for (String version : VERSION_DIRS) {
                Path sourceDir = Paths.get(projectRoot, "common", version, "src", "main", "java");
                if (!Files.isDirectory(sourceDir)) continue;

                try {
                    List<String> fields = TestUtils.getFieldNamesFromDir(sourceDir.toString(), registryFile);
                    versionFields.put(version, new TreeSet<>(fields));
                } catch (RuntimeException e) {
                    // Skip versions that don't have this registry file
                }
            }

            if (versionFields.size() < 2) continue;

            // Use the latest version as the reference
            String referenceVersion = null;
            Set<String> referenceFields = null;
            for (Map.Entry<String, Set<String>> entry : versionFields.entrySet()) {
                referenceVersion = entry.getKey();
                referenceFields = entry.getValue();
            }

            // Compare each version against the reference
            for (Map.Entry<String, Set<String>> entry : versionFields.entrySet()) {
                String version = entry.getKey();
                if (version.equals(referenceVersion)) continue;
                Set<String> fields = entry.getValue();

                String finalRefVersion = referenceVersion;
                Set<String> finalRefFields = referenceFields;
                tests.add(DynamicTest.dynamicTest(
                    "cross_version_" + registryName + "_" + version + "_vs_" + finalRefVersion,
                    () -> {
                        Set<String> missingInVersion = new TreeSet<>(finalRefFields);
                        missingInVersion.removeAll(fields);

                        Set<String> extraInVersion = new TreeSet<>(fields);
                        extraInVersion.removeAll(finalRefFields);

                        StringBuilder message = new StringBuilder();
                        if (!missingInVersion.isEmpty()) {
                            message.append(registryName).append(" in ").append(version)
                                .append(" is missing fields present in ").append(finalRefVersion)
                                .append(": ").append(missingInVersion);
                        }
                        if (!extraInVersion.isEmpty()) {
                            if (message.length() > 0) message.append("\n");
                            message.append(registryName).append(" in ").append(version)
                                .append(" has extra fields not in ").append(finalRefVersion)
                                .append(": ").append(extraInVersion);
                        }

                        assertEquals(
                            finalRefFields, fields,
                            message.toString()
                        );
                    }
                ));
            }
        }

        return tests;
    }
}
