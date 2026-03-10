package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates template pool JSON resources for structure file reference consistency.
 * Checks that all chronodawn: location references in template pools point to
 * existing NBT structure files.
 */
public class TemplatePoolValidationTest {

    @TestFactory
    Collection<DynamicTest> templatePoolStructureExistenceTests() {
        List<String> poolFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/template_pool/");
        if (poolFiles.isEmpty()) return List.of();

        Collection<DynamicTest> tests = new ArrayList<>();

        for (String poolPath : poolFiles) {
            String poolName = poolPath.replace("data/chronodawn/worldgen/template_pool/", "")
                .replace(".json", "").replace("/", "_");
            Map<String, Object> pool = TestUtils.loadJsonResource(poolPath);
            if (pool == null) continue;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> elements = (List<Map<String, Object>>) pool.get("elements");
            if (elements == null) continue;

            for (Map<String, Object> element : elements) {
                @SuppressWarnings("unchecked")
                Map<String, Object> elem = (Map<String, Object>) element.get("element");
                if (elem == null) continue;

                String location = (String) elem.get("location");
                if (location == null || !location.startsWith("chronodawn:")) continue;

                String structureName = location.replace("chronodawn:", "");
                tests.add(DynamicTest.dynamicTest(
                    "template_pool_nbt_" + poolName + "_" + structureName,
                    () -> assertTrue(
                        structureFileExists(structureName),
                        "Template pool '" + poolName + "' references missing NBT structure: " +
                        location + " (expected " + structureName + ".nbt)"
                    )
                ));
            }
        }

        return tests;
    }

    /**
     * Check if a structure NBT file exists in any of the resource directories.
     * For 1.20.1, NBT files are converted from 1.21.1+ format at build time,
     * so we also check the source directory (shared-1.21.1+).
     */
    private boolean structureFileExists(String structureName) {
        String projectRoot = TestUtils.getProjectRoot();
        String mcVersion = System.getProperty("chronodawn.minecraft.version", "");

        // 1.20.1 uses "structures/" (plural), 1.21.1+ uses "structure/" (singular)
        String[] structureDirNames = {"structure", "structures"};
        List<String> resourceDirs = new ArrayList<>();
        resourceDirs.add(Paths.get(projectRoot, "common", mcVersion, "src", "main", "resources").toString());
        if (TestUtils.compareVersions(mcVersion, "1.21.5") >= 0) {
            resourceDirs.add(Paths.get(projectRoot, "common", "shared-1.21.5+", "src", "main", "resources").toString());
        }
        if (TestUtils.compareVersions(mcVersion, "1.21.2") >= 0) {
            resourceDirs.add(Paths.get(projectRoot, "common", "shared-1.21.2+", "src", "main", "resources").toString());
        }
        // Always check shared-1.21.1+ as source of truth for NBT files
        // (1.20.1 converts these at build time)
        resourceDirs.add(Paths.get(projectRoot, "common", "shared-1.21.1+", "src", "main", "resources").toString());
        resourceDirs.add(Paths.get(projectRoot, "common", "shared", "src", "main", "resources").toString());

        for (String dir : resourceDirs) {
            for (String structureDir : structureDirNames) {
                Path nbtPath = Paths.get(dir, "data", "chronodawn", structureDir, structureName + ".nbt");
                if (Files.exists(nbtPath)) return true;
            }
        }
        return false;
    }
}
