package com.chronosphere.neoforge.client;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * NeoForge client-side initialization for Chronosphere mod.
 *
 * Handles client-only registrations such as:
 * - Block render layers (for transparent/cutout blocks)
 * - Block color providers (for tinted blocks like leaves)
 * - Entity renderers
 * - Particle effects
 *
 * This class is only loaded on the client side (Dist.CLIENT).
 */
@EventBusSubscriber(modid = Chronosphere.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChronosphereClientNeoForge {

    /**
     * Client setup event handler.
     * Called during FML client setup phase.
     *
     * @param event The client setup event
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerRenderLayers();
            registerBlockColors();
        });
    }

    /**
     * Register render layers for blocks that need special rendering.
     *
     * - Cutout: For blocks with transparent textures (saplings, leaves with transparency)
     * - Translucent: For blocks with semi-transparent textures
     */
    private static void registerRenderLayers() {
        // Register Time Wood Sapling to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        // Register Time Wood Leaves to use cutout_mipped rendering (for leaf transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TIME_WOOD_LEAVES.get(),
            RenderType.cutoutMipped()
        );

        // Register Unstable Fungus to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.UNSTABLE_FUNGUS.get(),
            RenderType.cutout()
        );

        // Register Potted Unstable Fungus to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.POTTED_UNSTABLE_FUNGUS.get(),
            RenderType.cutout()
        );

        // Register Potted Time Wood Sapling to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.POTTED_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );
    }

    /**
     * Register block color providers for blocks that need tinting.
     *
     * Time Wood Leaves use a fixed blue color (#78A6DA) regardless of biome.
     * The texture should be grayscale, and the blue color is applied uniformly.
     */
    private static void registerBlockColors() {
        // Note: Block color registration for NeoForge will be implemented here
        // if needed. For now, Time Wood Leaves use a fixed color texture.
        // If we need dynamic tinting, we'll register color providers here.
    }
}
