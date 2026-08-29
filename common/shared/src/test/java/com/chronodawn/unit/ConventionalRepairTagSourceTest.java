package com.chronodawn.unit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the version-specific repair-material consumers against tag regressions. */
class ConventionalRepairTagSourceTest {
    private static final List<String> VERSIONS = List.of(
        "1.21.1", "1.21.2", "1.21.4", "1.21.5", "1.21.6", "1.21.7",
        "1.21.8", "1.21.9", "1.21.10", "1.21.11"
    );

    private static final List<RepairConsumer> CONSUMERS = List.of(
        new RepairConsumer("items/equipment/ClockstoneTier.java", "TIME_CRYSTAL"),
        new RepairConsumer("items/equipment/EntropyCrystalTier.java", "ENTROPY_CRYSTAL"),
        new RepairConsumer("items/equipment/EnhancedClockstoneTier.java", "TIME_CRYSTAL"),
        new RepairConsumer("items/equipment/ClockstoneArmorMaterial.java", "TIME_CRYSTAL"),
        new RepairConsumer("items/equipment/EnhancedClockstoneArmorMaterial.java", "TIME_CRYSTAL"),
        new RepairConsumer("items/equipment/TemporalAmberArmorMaterial.java", "TEMPORAL_AMBER_DUST"),
        new RepairConsumer("items/artifacts/TimeTyrantArmorMaterial.java", "TIME_CRYSTAL")
    );

    @Test
    void everyModernRepairConsumerUsesItsDedicatedConventionalTag() throws IOException {
        String projectRoot = TestUtils.getProjectRoot();

        for (String version : VERSIONS) {
            String chronobladePath = version.equals("1.21.1") || version.equals("1.21.2")
                || version.equals("1.21.4")
                ? "items/artifacts/ChronobladeItem.java"
                : "items/artifacts/ChronobladeTier.java";

            assertUsesTag(projectRoot, version, chronobladePath, "TIME_CRYSTAL");
            for (RepairConsumer consumer : CONSUMERS) {
                assertUsesTag(projectRoot, version, consumer.relativePath(), consumer.tagField());
            }
        }
    }

    private static void assertUsesTag(String projectRoot, String version, String relativePath, String tagField)
        throws IOException {
        Path file = Paths.get(projectRoot, "common", version, "src", "main", "java", "com", "chronodawn", relativePath);
        String source = Files.readString(file, StandardCharsets.UTF_8);
        String tagReference = "ConventionalItemTags." + tagField;

        assertEquals(1, count(source, tagReference), version + " " + relativePath);
        String withoutConventionalTagName = source.replace("ConventionalItemTags.", "");
        assertTrue(
            !withoutConventionalTagName.contains("ItemTags."),
            version + " still uses a vanilla item tag: " + relativePath
        );
        assertTrue(!source.contains("Ingredient.of(ModItems."), version + " still uses an exact repair ingredient: " + relativePath);
    }

    private static int count(String source, String fragment) {
        int count = 0;
        int offset = 0;
        while ((offset = source.indexOf(fragment, offset)) >= 0) {
            count++;
            offset += fragment.length();
        }
        return count;
    }

    private record RepairConsumer(String relativePath, String tagField) {
    }
}
