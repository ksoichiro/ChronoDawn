package com.chronodawn.worldgen.features;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NbtTemplateConfigurationCodecTest {

    @Test
    void roundTripsAllFields() {
        String json = """
                {
                  "template": "chronodawn:time_cairn",
                  "random_rotate": false,
                  "y_offset": -1,
                  "min_ground_contact_ratio": 0.8,
                  "ground_contact_y_offset": 0,
                  "clear_replaceable_blocks": false
                }
                """;
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        NbtTemplateConfiguration original = getOrThrow(NbtTemplateConfiguration.CODEC.parse(JsonOps.INSTANCE, obj));
        JsonElement encoded = getOrThrow(NbtTemplateConfiguration.CODEC.encodeStart(JsonOps.INSTANCE, original));
        NbtTemplateConfiguration decoded = getOrThrow(NbtTemplateConfiguration.CODEC.parse(JsonOps.INSTANCE, encoded));

        assertEquals(original.template().toString(), decoded.template().toString());
        assertEquals(original.randomRotate(), decoded.randomRotate());
        assertEquals(original.yOffset(), decoded.yOffset());
        assertEquals(original.minGroundContactRatio(), decoded.minGroundContactRatio());
        assertEquals(original.groundContactYOffset(), decoded.groundContactYOffset());
        assertEquals(original.clearReplaceableBlocks(), decoded.clearReplaceableBlocks());
    }

    @Test
    void defaultsRandomRotateAndYOffsetWhenAbsent() {
        String json = "{\"template\":\"chronodawn:time_well\"}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        NbtTemplateConfiguration decoded = getOrThrow(NbtTemplateConfiguration.CODEC.parse(JsonOps.INSTANCE, obj));

        assertEquals("chronodawn:time_well", decoded.template().toString());
        assertTrue(decoded.randomRotate());
        assertEquals(0, decoded.yOffset());
        assertEquals(0.0, decoded.minGroundContactRatio());
        assertEquals(-1, decoded.groundContactYOffset());
        assertTrue(decoded.clearReplaceableBlocks());
    }

    private static <T> T getOrThrow(DataResult<T> result) {
        return result.result().orElseThrow(() -> new AssertionError(result.toString()));
    }
}
