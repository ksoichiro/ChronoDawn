package com.chronodawn.unit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards that every day-cycle timeline applied to the ChronoDawn dimension is
 * driven by the dimension's own clock.
 *
 * TimeDistortionEventHandler pauses and manually advances the ChronoDawn clock,
 * so a timeline bound to minecraft:overworld (e.g. via #minecraft:in_overworld)
 * makes the sky, monster burning, and sky light level follow the Overworld's time
 * instead, and mobs spawn or burn under a sky that looks like the opposite time.
 */
public class DimensionClockConsistencyTest {

    // World-age based (pillager patrol gating), not part of the day/night cycle
    private static final Set<String> OVERWORLD_CLOCK_ALLOWED = Set.of("minecraft:early_game");

    @Test
    void chronoDawnTimelinesUseChronoDawnClock() {
        JsonObject dimensionType = loadJson("data/chronodawn/dimension_type/chronodawn.json");
        String defaultClock = dimensionType.get("default_clock").getAsString();
        assertEquals("chronodawn:chronodawn", defaultClock);

        Set<String> timelines = new LinkedHashSet<>();
        resolveTimelines(dimensionType.get("timelines"), timelines);
        assertTrue(timelines.contains("chronodawn:day"), "Day timeline missing from " + timelines);

        for (String timeline : timelines) {
            if (OVERWORLD_CLOCK_ALLOWED.contains(timeline)) {
                continue;
            }
            JsonObject json = loadJson(resourcePath(timeline, "timeline"));
            assertEquals(defaultClock, json.get("clock").getAsString(),
                "Timeline " + timeline + " must use the ChronoDawn clock");
        }
    }

    private void resolveTimelines(JsonElement element, Set<String> out) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> resolveTimelines(e, out));
            return;
        }
        String id = element.getAsString();
        if (id.startsWith("#")) {
            JsonObject tag = loadJson(resourcePath(id.substring(1), "tags/timeline"));
            resolveTimelines(tag.get("values"), out);
        } else {
            out.add(id);
        }
    }

    private static String resourcePath(String id, String directory) {
        String[] parts = id.split(":", 2);
        return "data/" + parts[0] + "/" + directory + "/" + parts[1] + ".json";
    }

    private JsonObject loadJson(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Missing resource: " + path);
            return JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read " + path, e);
        }
    }
}
