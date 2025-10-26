package com.chronosphere.fabric.client;

import com.chronosphere.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;

/**
 * Fabric client-side initialization for Chronosphere mod.
 *
 * Handles client-only registrations such as:
 * - Block color providers (for tinted blocks like leaves)
 * - Entity renderers
 * - Particle effects
 *
 * This class is only loaded on the client side.
 */
public class ChronosphereClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerBlockColors();
    }

    /**
     * Register block color providers for blocks that need tinting.
     *
     * Time Wood Leaves use biome-based foliage coloring, similar to vanilla oak leaves.
     * The texture should be grayscale, and the color is applied based on the biome.
     */
    private void registerBlockColors() {
        // Register Time Wood Leaves to use biome foliage color (like oak leaves)
        ColorProviderRegistry.BLOCK.register(
            (state, world, pos, tintIndex) -> {
                if (world != null && pos != null) {
                    return BiomeColors.getAverageFoliageColor(world, pos);
                }
                return FoliageColor.getDefaultColor();
            },
            ModBlocks.TIME_WOOD_LEAVES.get()
        );

        // Register the item color as well (for inventory/hand rendering)
        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> FoliageColor.getDefaultColor(),
            ModBlocks.TIME_WOOD_LEAVES.get()
        );
    }
}
