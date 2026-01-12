package com.chronodawn.unit;

import com.chronodawn.ChronoDawnTestBase;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModItems;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for registry infrastructure.
 *
 * This test class validates that all registries are properly initialized and accessible.
 *
 * TODO: Add tests for specific blocks, items, and entities in future phases.
 */
public class RegistryTest extends ChronoDawnTestBase {
    /**
     * Test that dimension keys are properly initialized.
     */
    @Test
    public void testDimensionKeysInitialized() {
        requireMinecraft121("ResourceKey initialization requires Minecraft 1.21.1+ registry system");
        logTest("Testing dimension keys initialization");

        assertNotNull(ModDimensions.CHRONO_DAWN_DIMENSION, "ChronoDawn dimension key should not be null");
        assertNotNull(ModDimensions.CHRONO_DAWN_DIMENSION_TYPE, "ChronoDawn dimension type key should not be null");
    }

    /**
     * Test that Time Wood blocks are properly registered.
     */
    @Test
    public void testTimeWoodBlocksRegistered() {
        requireMinecraft121("Registry initialization requires Minecraft 1.21.1+ registry system");
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
        requireMinecraft121("Registry initialization requires Minecraft 1.21.1+ registry system");
        logTest("Testing Fruit of Time item registration");

        assertNotNull(ModItems.FRUIT_OF_TIME, "Fruit of Time item should be registered");
    }

    // TODO: Add tests in future phases:
    // - testEntitiesRegistered() - verify all custom entities are registered
}
