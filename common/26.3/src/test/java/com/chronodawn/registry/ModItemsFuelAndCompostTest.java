package com.chronodawn.registry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 26.3 removed the central composter/fuel registries, so each item now declares its own
 * {@code .compostable(...)} / {@code .cookingFuel(...)} in ModItems.java. Those calls are easy
 * to drop silently (nothing fails at load time), so this pins the expected tier per item by
 * parsing the source, the same way the other registry consistency tests do.
 */
class ModItemsFuelAndCompostTest {

    private static final List<String> WOODS = List.of("TIME_WOOD", "DARK_TIME_WOOD", "ANCIENT_TIME_WOOD");

    // Burn times match the tag-based values used on every other version.
    private static final Map<String, String> FUEL_TIERS = Map.ofEntries(
        Map.entry("LOG", "WOOD_BLOCKS"),
        Map.entry("PLANKS", "WOOD_BLOCKS"),
        Map.entry("STAIRS", "WOOD_BLOCKS"),
        Map.entry("FENCE", "WOOD_BLOCKS"),
        Map.entry("FENCE_GATE", "WOOD_BLOCKS"),
        Map.entry("PRESSURE_PLATE", "WOOD_BLOCKS"),
        Map.entry("TRAPDOOR", "WOOD_BLOCKS"),
        Map.entry("BUTTON", "WOOD_ITEMS_EXTRA_SMALL"),
        Map.entry("SAPLING", "WOOD_ITEMS_EXTRA_SMALL"),
        Map.entry("DOOR", "WOOD_ITEMS_LARGE"),
        Map.entry("SLAB", "WOOD_SLABS"),
        Map.entry("BOAT", "BOATS"),
        Map.entry("CHEST_BOAT", "BOATS")
    );

    // Chances match the values previously registered in ComposterBlock.COMPOSTABLES.
    private static final Map<String, String> COMPOST_TIERS = Map.ofEntries(
        // 0.3
        Map.entry("TIME_WOOD_SAPLING", "LOW"), Map.entry("DARK_TIME_WOOD_SAPLING", "LOW"),
        Map.entry("ANCIENT_TIME_WOOD_SAPLING", "LOW"), Map.entry("TIME_WOOD_LEAVES", "LOW"),
        Map.entry("DARK_TIME_WOOD_LEAVES", "LOW"), Map.entry("ANCIENT_TIME_WOOD_LEAVES", "LOW"),
        Map.entry("TIME_WHEAT_SEEDS", "LOW"), Map.entry("CHRONO_MELON_SEEDS", "LOW"),
        Map.entry("TEMPORAL_TALL_GRASS", "LOW"), Map.entry("TEMPORAL_FERN", "LOW"),
        Map.entry("TEMPORAL_GRASS", "LOW"), Map.entry("FADED_TEMPORAL_GRASS", "LOW"),
        Map.entry("TEMPORAL_KELP", "LOW"), Map.entry("DRIED_TEMPORAL_KELP", "LOW"),
        Map.entry("TEMPORAL_SEAGRASS", "LOW"), Map.entry("TALL_TEMPORAL_SEAGRASS", "LOW"),
        // 0.65
        Map.entry("LUMEN_POLYP", "MEDIUM"), Map.entry("PURPLE_TIME_BLOSSOM", "MEDIUM"),
        Map.entry("ORANGE_TIME_BLOSSOM", "MEDIUM"), Map.entry("PINK_TIME_BLOSSOM", "MEDIUM"),
        Map.entry("DAWN_BELL", "MEDIUM"), Map.entry("DUSK_BELL", "MEDIUM"),
        Map.entry("TIME_WHEAT", "MEDIUM"), Map.entry("TEMPORAL_ROOT", "MEDIUM"),
        Map.entry("CHRONO_MELON_SLICE", "MEDIUM"), Map.entry("TIMELESS_MUSHROOM", "MEDIUM"),
        Map.entry("UNSTABLE_FUNGUS", "MEDIUM"),
        // 0.85
        Map.entry("BAKED_TEMPORAL_ROOT", "MEDIUM_HIGH"), Map.entry("TIME_WHEAT_COOKIE", "MEDIUM_HIGH"),
        // 1.0
        Map.entry("TIME_WHEAT_BALE", "ALWAYS_ADD_ONE")
    );

    @Test
    void woodenItemsDeclareTheirBurnTime() throws IOException {
        String source = readModItems();
        for (String wood : WOODS) {
            for (Map.Entry<String, String> e : FUEL_TIERS.entrySet()) {
                String field = wood + "_" + e.getKey();
                assertTrue(registration(source, field)
                        .contains(".cookingFuel(ContextIntProviders.COOKING_TIME_" + e.getValue() + ")"),
                    field + " must burn with COOKING_TIME_" + e.getValue());
            }
        }
    }

    @Test
    void compostableItemsDeclareTheirTier() throws IOException {
        String source = readModItems();
        assertEquals(30, COMPOST_TIERS.size());
        for (Map.Entry<String, String> e : COMPOST_TIERS.entrySet()) {
            assertTrue(registration(source, e.getKey())
                    .contains(".compostable(ContextIntProviders.COMPOSTABLE_" + e.getValue() + ")"),
                e.getKey() + " must be COMPOSTABLE_" + e.getValue());
        }
    }

    private static String readModItems() throws IOException {
        Path path = Paths.get(System.getProperty("chronodawn.source.dir"), "com/chronodawn/registry/ModItems.java");
        return Files.readString(path);
    }

    private static String registration(String source, String field) {
        Matcher m = Pattern.compile(
            "public static final RegistrySupplier<Item> " + field + " = ITEMS\\.register\\((.*?)\\n    \\);",
            Pattern.DOTALL).matcher(source);
        assertTrue(m.find(), "No registration found for " + field);
        return m.group(1);
    }
}
