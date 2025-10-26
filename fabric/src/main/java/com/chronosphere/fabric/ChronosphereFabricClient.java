package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

/**
 * Fabric client-side initialization.
 *
 * This class handles client-only setup such as render layer registration.
 */
public class ChronosphereFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register render layers for blocks with transparency
        registerRenderLayers();

        Chronosphere.LOGGER.info("Chronosphere Fabric client initialized");
    }

    /**
     * Register render layers for blocks that require transparency or cutout rendering.
     */
    private void registerRenderLayers() {
        // Unstable Fungus uses cutout rendering for transparent parts (like vanilla mushrooms)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.UNSTABLE_FUNGUS.get(),
            RenderType.cutout()
        );
    }
}
