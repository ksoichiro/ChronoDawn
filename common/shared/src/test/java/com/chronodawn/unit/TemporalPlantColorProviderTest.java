package com.chronodawn.unit;

import com.chronodawn.client.TemporalGrassEdgeTint;
import com.chronodawn.client.TemporalPlantColorProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link TemporalPlantColorProvider}.
 * Provider entry points depend on a Minecraft client level, so these tests cover
 * the pure tint constants and math that keep the Faded Plains palette withered.
 */
class TemporalPlantColorProviderTest {

    @Test
    void fadedPlainsTint_shiftsTealBaselineTowardMutedDryBrown() {
        int effective = TemporalGrassEdgeTint.multiplyRgb(
            TemporalPlantColorProvider.BASELINE,
            TemporalPlantColorProvider.FADED_PLAINS_TINT
        );

        assertEquals(0x5B5A3D, effective);
    }
}
