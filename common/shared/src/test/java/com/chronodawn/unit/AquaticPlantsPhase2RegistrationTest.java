package com.chronodawn.unit;

import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModItemId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Locks in the block and item IDs added for the Temporal Aquatic Plants
 * Phase 2 feature. Ensures none get accidentally renamed or removed across versions.
 *
 * Phase 2 currently covers Lumen Polyp only (Chrono Coral is deferred).
 */
public class AquaticPlantsPhase2RegistrationTest {

    @Test
    void lumenPolypBlockIdExists() {
        ModBlockId id = ModBlockId.valueOf("LUMEN_POLYP");
        assertNotNull(id);
        assertEquals("lumen_polyp", id.id());
    }

    @Test
    void lumenPolypItemIdExists() {
        ModItemId id = ModItemId.valueOf("LUMEN_POLYP");
        assertNotNull(id);
        assertEquals("lumen_polyp", id.id());
    }
}
