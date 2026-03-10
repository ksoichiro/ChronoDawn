package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates worldgen JSON resources for internal consistency.
 * Checks that biome spawners reference valid entities, configured features
 * reference valid blocks, and placed features reference valid configured features.
 */
public class WorldgenValidationTest {

    @TestFactory
    Collection<DynamicTest> biomeSpawnerEntityTests() {
        List<String> biomeFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/biome/");
        if (biomeFiles.isEmpty()) {
            return List.of(DynamicTest.dynamicTest("biome_files_found", () -> {
                // No biome files found for this version - skip
            }));
        }

        Set<String> entityIds = TestUtils.getEntityIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String biomePath : biomeFiles) {
            String biomeName = biomePath.substring(biomePath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> biome = TestUtils.loadJsonResource(biomePath);
            if (biome == null) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> spawners = (Map<String, Object>) biome.get("spawners");
            if (spawners == null) continue;

            for (Map.Entry<String, Object> entry : spawners.entrySet()) {
                String category = entry.getKey();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> entries = (List<Map<String, Object>>) entry.getValue();
                if (entries == null) continue;

                for (Map<String, Object> spawner : entries) {
                    String type = (String) spawner.get("type");
                    if (type != null && type.startsWith("chronodawn:")) {
                        tests.add(DynamicTest.dynamicTest(
                            "biome_spawner_" + biomeName + "_" + category + "_" + type.replace("chronodawn:", ""),
                            () -> assertTrue(
                                entityIds.contains(type),
                                "Biome '" + biomeName + "' spawner references unknown entity: " + type
                            )
                        ));
                    }
                }
            }
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> biomeFeatureReferenceTests() {
        List<String> biomeFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/biome/");
        if (biomeFiles.isEmpty()) return List.of();

        List<String> placedFeatureFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/placed_feature/");
        Set<String> placedFeatureIds = new java.util.HashSet<>();
        for (String pf : placedFeatureFiles) {
            String id = "chronodawn:" + pf.substring(pf.lastIndexOf('/') + 1).replace(".json", "");
            placedFeatureIds.add(id);
        }

        Collection<DynamicTest> tests = new ArrayList<>();

        for (String biomePath : biomeFiles) {
            String biomeName = biomePath.substring(biomePath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> biome = TestUtils.loadJsonResource(biomePath);
            if (biome == null) continue;

            @SuppressWarnings("unchecked")
            List<Object> features = (List<Object>) biome.get("features");
            if (features == null) continue;

            for (int step = 0; step < features.size(); step++) {
                Object stepFeatures = features.get(step);
                if (!(stepFeatures instanceof List)) continue;

                @SuppressWarnings("unchecked")
                List<String> featureList = (List<String>) stepFeatures;
                for (String feature : featureList) {
                    if (feature.startsWith("chronodawn:")) {
                        int finalStep = step;
                        tests.add(DynamicTest.dynamicTest(
                            "biome_feature_" + biomeName + "_step" + finalStep + "_" + feature.replace("chronodawn:", ""),
                            () -> assertTrue(
                                placedFeatureIds.contains(feature),
                                "Biome '" + biomeName + "' step " + finalStep +
                                " references unknown placed feature: " + feature
                            )
                        ));
                    }
                }
            }
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> placedFeatureConfiguredFeatureTests() {
        List<String> placedFeatureFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/placed_feature/");
        if (placedFeatureFiles.isEmpty()) return List.of();

        List<String> configuredFeatureFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/configured_feature/");
        Set<String> configuredFeatureIds = new java.util.HashSet<>();
        for (String cf : configuredFeatureFiles) {
            String id = "chronodawn:" + cf.substring(cf.lastIndexOf('/') + 1).replace(".json", "");
            configuredFeatureIds.add(id);
        }

        Collection<DynamicTest> tests = new ArrayList<>();

        for (String pfPath : placedFeatureFiles) {
            String pfName = pfPath.substring(pfPath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> pf = TestUtils.loadJsonResource(pfPath);
            if (pf == null) continue;

            String feature = (String) pf.get("feature");
            if (feature != null && feature.startsWith("chronodawn:")) {
                tests.add(DynamicTest.dynamicTest(
                    "placed_feature_ref_" + pfName,
                    () -> assertTrue(
                        configuredFeatureIds.contains(feature),
                        "Placed feature '" + pfName + "' references unknown configured feature: " + feature
                    )
                ));
            }
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> configuredFeatureBlockReferenceTests() {
        List<String> cfFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/configured_feature/");
        if (cfFiles.isEmpty()) return List.of();

        Set<String> blockIds = TestUtils.getBlockIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String cfPath : cfFiles) {
            String cfName = cfPath.substring(cfPath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> cf = TestUtils.loadJsonResource(cfPath);
            if (cf == null) continue;

            Set<String> blockRefs = extractBlockReferences(cf);
            for (String blockRef : blockRefs) {
                if (blockRef.startsWith("chronodawn:")) {
                    tests.add(DynamicTest.dynamicTest(
                        "configured_feature_block_" + cfName + "_" + blockRef.replace("chronodawn:", ""),
                        () -> assertTrue(
                            blockIds.contains(blockRef),
                            "Configured feature '" + cfName + "' references unknown block: " + blockRef
                        )
                    ));
                }
            }
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> structureStartPoolTests() {
        List<String> structureFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/structure/");
        if (structureFiles.isEmpty()) return List.of();

        List<String> templatePoolFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/template_pool/");
        Set<String> poolIds = new java.util.HashSet<>();
        for (String tp : templatePoolFiles) {
            // template_pool/ancient_ruins/start_pool.json -> chronodawn:ancient_ruins/start_pool
            String relative = tp.replace("data/chronodawn/worldgen/template_pool/", "").replace(".json", "");
            poolIds.add("chronodawn:" + relative);
        }

        Collection<DynamicTest> tests = new ArrayList<>();

        for (String structurePath : structureFiles) {
            String structureName = structurePath.substring(structurePath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> structure = TestUtils.loadJsonResource(structurePath);
            if (structure == null) continue;

            String startPool = (String) structure.get("start_pool");
            if (startPool != null && startPool.startsWith("chronodawn:")) {
                tests.add(DynamicTest.dynamicTest(
                    "structure_start_pool_" + structureName,
                    () -> assertTrue(
                        poolIds.contains(startPool),
                        "Structure '" + structureName + "' references unknown template pool: " + startPool
                    )
                ));
            }
        }

        return tests;
    }

    /**
     * Extract block references from worldgen JSON (Name fields in state providers).
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractBlockReferences(Object obj) {
        Set<String> refs = new java.util.HashSet<>();
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            // Block state: {"Name": "chronodawn:...", "Properties": {...}}
            Object name = map.get("Name");
            if (name instanceof String && ((String) name).startsWith("chronodawn:")) {
                refs.add((String) name);
            }
            // matching_blocks: {"blocks": ["chronodawn:...", ...]}
            Object blocks = map.get("blocks");
            if (blocks instanceof List) {
                for (Object b : (List<Object>) blocks) {
                    if (b instanceof String && ((String) b).startsWith("chronodawn:")) {
                        refs.add((String) b);
                    }
                }
            }
            for (Object value : map.values()) {
                refs.addAll(extractBlockReferences(value));
            }
        } else if (obj instanceof List) {
            for (Object item : (List<Object>) obj) {
                refs.addAll(extractBlockReferences(item));
            }
        }
        return refs;
    }
}
