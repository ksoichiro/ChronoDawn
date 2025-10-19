package com.chronosphere.unit;

import com.chronosphere.ChronosphereTestBase;
import com.chronosphere.registry.ModDimensions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for registry infrastructure.
 *
 * This test class validates that all registries are properly initialized and accessible.
 *
 * TODO: Add tests for specific blocks, items, and entities in future phases.
 */
public class RegistryTest extends ChronosphereTestBase {
    /**
     * Test that dimension keys are properly initialized.
     */
    @Test
    public void testDimensionKeysInitialized() {
        logTest("Testing dimension keys initialization");

        assertNotNull(ModDimensions.CHRONOSPHERE_DIMENSION, "Chronosphere dimension key should not be null");
        assertNotNull(ModDimensions.CHRONOSPHERE_DIMENSION_TYPE, "Chronosphere dimension type key should not be null");
    }

    // TODO: Add tests in future phases:
    // - testBlocksRegistered() - verify all custom blocks are registered
    // - testItemsRegistered() - verify all custom items are registered
    // - testEntitiesRegistered() - verify all custom entities are registered
}
