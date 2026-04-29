package com.chronodawn.unit;

import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModItemId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Locks in the four block IDs and four item IDs added for the Temporal Aquatic Plants
 * Phase 1 feature. Ensures none get accidentally renamed or removed across versions.
 */
public class AquaticPlantsRegistrationTest {

    @Test
    void temporalKelpBlockIdExists() {
        ModBlockId id = ModBlockId.valueOf("TEMPORAL_KELP");
        assertNotNull(id);
        assertEquals("temporal_kelp", id.id());
    }

    @Test
    void temporalKelpPlantBlockIdExists() {
        ModBlockId id = ModBlockId.valueOf("TEMPORAL_KELP_PLANT");
        assertNotNull(id);
        assertEquals("temporal_kelp_plant", id.id());
    }

    @Test
    void temporalSeagrassBlockIdExists() {
        ModBlockId id = ModBlockId.valueOf("TEMPORAL_SEAGRASS");
        assertNotNull(id);
        assertEquals("temporal_seagrass", id.id());
    }

    @Test
    void tallTemporalSeagrassBlockIdExists() {
        ModBlockId id = ModBlockId.valueOf("TALL_TEMPORAL_SEAGRASS");
        assertNotNull(id);
        assertEquals("tall_temporal_seagrass", id.id());
    }

    @Test
    void temporalKelpItemIdExists() {
        ModItemId id = ModItemId.valueOf("TEMPORAL_KELP");
        assertNotNull(id);
        assertEquals("temporal_kelp", id.id());
    }

    @Test
    void temporalSeagrassItemIdExists() {
        ModItemId id = ModItemId.valueOf("TEMPORAL_SEAGRASS");
        assertNotNull(id);
        assertEquals("temporal_seagrass", id.id());
    }

    @Test
    void tallTemporalSeagrassItemIdExists() {
        ModItemId id = ModItemId.valueOf("TALL_TEMPORAL_SEAGRASS");
        assertNotNull(id);
        assertEquals("tall_temporal_seagrass", id.id());
    }

    @Test
    void driedTemporalKelpItemIdExists() {
        ModItemId id = ModItemId.valueOf("DRIED_TEMPORAL_KELP");
        assertNotNull(id);
        assertEquals("dried_temporal_kelp", id.id());
    }
}
