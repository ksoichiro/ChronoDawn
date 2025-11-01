package com.chronosphere.unit;

import com.chronosphere.ChronosphereTestBase;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.registry.ModItems;
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

    /**
     * Test that Time Wood blocks are properly registered.
     */
    @Test
    public void testTimeWoodBlocksRegistered() {
        logTest("Testing Time Wood blocks registration");

        assertNotNull(ModBlocks.TIME_WOOD_LOG, "Time Wood Log should be registered");
        assertNotNull(ModBlocks.TIME_WOOD_LEAVES, "Time Wood Leaves should be registered");
        assertNotNull(ModBlocks.TIME_WOOD_PLANKS, "Time Wood Planks should be registered");
        assertNotNull(ModBlocks.TIME_WOOD_SAPLING, "Time Wood Sapling should be registered");
        assertNotNull(ModBlocks.FRUIT_OF_TIME_BLOCK, "Fruit of Time Block should be registered");
    }

    /**
     * Test that Fruit of Time item is properly registered.
     */
    @Test
    public void testFruitOfTimeItemRegistered() {
        logTest("Testing Fruit of Time item registration");

        assertNotNull(ModItems.FRUIT_OF_TIME, "Fruit of Time item should be registered");
    }

    // TODO: Add tests in future phases:
    // - testEntitiesRegistered() - verify all custom entities are registered
}
