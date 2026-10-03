package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates template pool JSON resources for structure file reference consistency.
 * Checks that all chronodawn: location references in template pools point to
 * existing NBT structure files.
 */
public class TemplatePoolValidationTest {

    /**
     * Template pool directories of structures listed in StructureStartMixin's
     * WATERLOGGING_PREVENTION_STRUCTURES. Keep in sync with that set.
     */
    private static final Set<String> WATERLOGGING_PREVENTION_POOL_DIRS = Set.of(
        "master_clock",
        "guardian_vault",
        "clockwork_depths",
        "phantom_catacombs",
        "phantom_tower",
        "entropy_crypt"
    );

    /**
     * NBT encoding of a "waterlogged": "true" block state property
     * (TAG_String id, name length, name, value length, value).
     */
    private static final byte[] WATERLOGGED_TRUE_NBT = concat(
        new byte[] {0x08, 0x00, 0x0B},
        "waterlogged".getBytes(StandardCharsets.UTF_8),
        new byte[] {0x00, 0x04},
        "true".getBytes(StandardCharsets.UTF_8)
    );

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
     * StructureStartMixin clears waterlogging on every block it does not find in
     * CopyFluidLevelProcessor.INTENTIONAL_WATERLOGGING. Templates with intentionally
     * waterlogged blocks in those structures therefore lose their water unless the
     * pool element runs chronodawn:copy_fluid_level to record the positions.
     */
    @TestFactory
    Collection<DynamicTest> waterloggedTemplatesUseCopyFluidLevelTests() {
        String poolPrefix = "data/chronodawn/worldgen/template_pool/";
        List<String> poolFiles = TestUtils.findJsonResources(poolPrefix);
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String poolPath : poolFiles) {
            String relative = poolPath.substring(poolPrefix.length());
            int slash = relative.indexOf('/');
            if (slash < 0 || !WATERLOGGING_PREVENTION_POOL_DIRS.contains(relative.substring(0, slash))) continue;

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
                Path nbtPath = findStructureFile(structureName);
                if (nbtPath == null || !containsWaterloggedBlock(nbtPath)) continue;

                Object processors = elem.get("processors");
                tests.add(DynamicTest.dynamicTest(
                    "template_pool_copy_fluid_level_" + structureName,
                    () -> assertTrue(
                        usesCopyFluidLevel(processors),
                        "Template '" + structureName + "' has waterlogged blocks but its pool element "
                            + "processors " + processors + " do not include chronodawn:copy_fluid_level, "
                            + "so StructureStartMixin removes the water"
                    )
                ));
            }
        }

        return tests;
    }

    private static boolean usesCopyFluidLevel(Object processors) {
        if (!(processors instanceof String id) || !id.startsWith("chronodawn:")) return false;
        Map<String, Object> list = TestUtils.loadJsonResource(
            "data/chronodawn/worldgen/processor_list/" + id.substring("chronodawn:".length()) + ".json");
        if (list == null) return false;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) list.get("processors");
        if (entries == null) return false;
        return entries.stream().anyMatch(p -> "chronodawn:copy_fluid_level".equals(p.get("processor_type")));
    }

    private static boolean containsWaterloggedBlock(Path nbtPath) {
        byte[] data;
        try (InputStream in = new GZIPInputStream(Files.newInputStream(nbtPath))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            in.transferTo(out);
            data = out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + nbtPath, e);
        }

        outer:
        for (int i = 0; i <= data.length - WATERLOGGED_TRUE_NBT.length; i++) {
            for (int j = 0; j < WATERLOGGED_TRUE_NBT.length; j++) {
                if (data[i + j] != WATERLOGGED_TRUE_NBT[j]) continue outer;
            }
            return true;
        }
        return false;
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }

    /**
     * Check if a structure NBT file exists in any of the resource directories.
     * For 1.20.1, NBT files are converted from 1.21.1+ format at build time,
     * so we also check the source directory (shared-1.21.1+).
     */
    private boolean structureFileExists(String structureName) {
        return findStructureFile(structureName) != null;
    }

    private static Path findStructureFile(String structureName) {
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
                if (Files.exists(nbtPath)) return nbtPath;
            }
        }
        return null;
    }
}
