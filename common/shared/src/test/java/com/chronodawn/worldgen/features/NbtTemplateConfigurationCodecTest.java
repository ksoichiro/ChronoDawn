package com.chronodawn.worldgen.features;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NbtTemplateConfigurationCodecTest {

    @Test
    void roundTripsAllFields() {
        NbtTemplateConfiguration original = new NbtTemplateConfiguration(
                ResourceLocation.parse("chronodawn:time_cairn"),
                false,
                -1
        );

        JsonElement encoded = NbtTemplateConfiguration.CODEC.encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        NbtTemplateConfiguration decoded = NbtTemplateConfiguration.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original.template(), decoded.template());
        assertEquals(original.randomRotate(), decoded.randomRotate());
        assertEquals(original.yOffset(), decoded.yOffset());
    }

    @Test
    void defaultsRandomRotateAndYOffsetWhenAbsent() {
        String json = "{\"template\":\"chronodawn:time_well\"}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        NbtTemplateConfiguration decoded = NbtTemplateConfiguration.CODEC.parse(JsonOps.INSTANCE, obj)
                .getOrThrow();

        assertEquals(ResourceLocation.parse("chronodawn:time_well"), decoded.template());
        assertTrue(decoded.randomRotate());
        assertEquals(0, decoded.yOffset());
    }
}
