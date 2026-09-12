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

/**
 * Guards the version-specific repair-material consumers against tag regressions.
 *
 * <p>1.21.2 is deliberately excluded from the conventional-tag requirement: using a
 * custom-namespace tag as a {@code ToolMaterial}/{@code ArmorMaterial} repair ingredient
 * crashes NeoForge 1.21.2 at startup with an unbound-tag error (upstream
 * neoforged/NeoForge#1651, fixed only from 21.3.7-beta onward, never backported to the
 * abandoned MC-1.21.2 NeoForge line). 1.21.2 stays on the vanilla {@code ItemTags} it used
 * before the conventional-tag feature landed; a dedicated assertion below guards that
 * exception so it can't silently drift either back to a conventional tag (reintroducing the
 * crash) or to some other unexpected tag.
 */
class ConventionalRepairTagSourceTest {
    private static final List<String> VERSIONS = List.of(
        "1.21.1", "1.21.4", "1.21.5", "1.21.6", "1.21.7",
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

    // 1.21.2's vanilla-tag fallback, keyed by the same relative paths as CONSUMERS plus
    // ChronobladeItem.java. These are the exact tags each file used before the
    // conventional-tag feature landed (commit 10d59ce3) and were reverted back to.
    private static final List<RepairConsumer> LEGACY_1_21_2_CONSUMERS = List.of(
        new RepairConsumer("items/artifacts/ChronobladeItem.java", "NETHERITE_TOOL_MATERIALS"),
        new RepairConsumer("items/equipment/ClockstoneTier.java", "IRON_TOOL_MATERIALS"),
        new RepairConsumer("items/equipment/EntropyCrystalTier.java", "IRON_TOOL_MATERIALS"),
        new RepairConsumer("items/equipment/EnhancedClockstoneTier.java", "DIAMOND_TOOL_MATERIALS"),
        new RepairConsumer("items/equipment/ClockstoneArmorMaterial.java", "REPAIRS_IRON_ARMOR"),
        new RepairConsumer("items/equipment/EnhancedClockstoneArmorMaterial.java", "REPAIRS_DIAMOND_ARMOR"),
        new RepairConsumer("items/equipment/TemporalAmberArmorMaterial.java", "REPAIRS_DIAMOND_ARMOR"),
        new RepairConsumer("items/artifacts/TimeTyrantArmorMaterial.java", "REPAIRS_NETHERITE_ARMOR")
    );

    @Test
    void everyModernRepairConsumerUsesItsDedicatedConventionalTag() throws IOException {
        String projectRoot = TestUtils.getProjectRoot();

        for (String version : VERSIONS) {
            String chronobladePath = version.equals("1.21.1") || version.equals("1.21.4")
                ? "items/artifacts/ChronobladeItem.java"
                : "items/artifacts/ChronobladeTier.java";

            assertUsesConventionalTag(projectRoot, version, chronobladePath, "TIME_CRYSTAL");
            for (RepairConsumer consumer : CONSUMERS) {
                assertUsesConventionalTag(projectRoot, version, consumer.relativePath(), consumer.tagField());
            }
        }
    }

    @Test
    void mc1212StaysOnItsVanillaRepairTagFallback() throws IOException {
        String projectRoot = TestUtils.getProjectRoot();

        for (RepairConsumer consumer : LEGACY_1_21_2_CONSUMERS) {
            assertUsesVanillaTag(projectRoot, "1.21.2", consumer.relativePath(), consumer.tagField());
        }
    }

    private static void assertUsesConventionalTag(
            String projectRoot, String version, String relativePath, String tagField) throws IOException {
        String source = readSource(projectRoot, version, relativePath);
        String tagReference = "ConventionalItemTags." + tagField;

        assertEquals(1, count(source, tagReference), version + " " + relativePath);
        String withoutConventionalTagName = source.replace("ConventionalItemTags.", "");
        assertTrue(
            !withoutConventionalTagName.contains("ItemTags."),
            version + " still uses a vanilla item tag: " + relativePath
        );
        assertTrue(!source.contains("Ingredient.of(ModItems."), version + " still uses an exact repair ingredient: " + relativePath);
    }

    private static void assertUsesVanillaTag(
            String projectRoot, String version, String relativePath, String tagField) throws IOException {
        String source = readSource(projectRoot, version, relativePath);
        String tagReference = "ItemTags." + tagField;

        assertEquals(1, count(source, tagReference), version + " " + relativePath);
        assertTrue(
            !source.contains("ConventionalItemTags."),
            version + " unexpectedly uses a conventional tag, which crashes NeoForge on this version: "
                + relativePath
        );
    }

    private static String readSource(String projectRoot, String version, String relativePath) throws IOException {
        Path file = Paths.get(projectRoot, "common", version, "src", "main", "java", "com", "chronodawn", relativePath);
        return Files.readString(file, StandardCharsets.UTF_8);
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
