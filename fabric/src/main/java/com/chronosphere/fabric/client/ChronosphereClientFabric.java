package com.chronosphere.fabric.client;

import com.chronosphere.client.model.ClockworkSentinelModel;
import com.chronosphere.client.model.TemporalWraithModel;
import com.chronosphere.client.model.TimeGuardianModel;
import com.chronosphere.client.model.TimeKeeperModel;
import com.chronosphere.client.model.TimeTyrantModel;
import com.chronosphere.client.renderer.TimeArrowRenderer;
import com.chronosphere.client.renderer.TimeGuardianRenderer;
import com.chronosphere.client.renderer.TimeTyrantRenderer;
import com.chronosphere.client.renderer.mobs.ClockworkSentinelRenderer;
import com.chronosphere.client.renderer.mobs.TemporalWraithRenderer;
import com.chronosphere.client.renderer.mobs.TimeKeeperRenderer;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.FoliageColor;

/**
 * Fabric client-side initialization for Chronosphere mod.
 *
 * Handles client-only registrations such as:
 * - Block color providers (for tinted blocks like leaves)
 * - Block render layers (for transparent/cutout blocks)
 * - Entity renderers
 * - Particle effects
 *
 * This class is only loaded on the client side.
 */
public class ChronosphereClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerBlockColors();
        registerRenderLayers();
        registerEntityModelLayers();
        registerEntityRenderers();
    }

    /**
     * Register block color providers for blocks that need tinting.
     *
     * Time Wood Leaves use a fixed blue color (#78A6DA) regardless of biome.
     * The texture should be grayscale, and the blue color is applied uniformly.
     */
    private void registerBlockColors() {
        // Register Time Wood Leaves to use fixed blue color (not biome-dependent)
        // This ensures leaves remain blue even when placed in other biomes
        ColorProviderRegistry.BLOCK.register(
            (state, world, pos, tintIndex) -> 0x78A6DA,
            ModBlocks.TIME_WOOD_LEAVES.get()
        );

        // Register the item color as well (for inventory/hand rendering)
        // Use the same blue color (0x78A6DA)
        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> 0x78A6DA,
            ModBlocks.TIME_WOOD_LEAVES.get()
        );
    }

    /**
     * Register render layers for blocks that need special rendering.
     *
     * - Cutout: For blocks with transparent textures (saplings, leaves with transparency)
     * - Translucent: For blocks with semi-transparent textures
     */
    private void registerRenderLayers() {
        // Register Time Wood Sapling to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        // Register Time Wood Leaves to use cutout_mipped rendering (for leaf transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TIME_WOOD_LEAVES.get(),
            RenderType.cutoutMipped()
        );

        // Register Unstable Fungus to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.UNSTABLE_FUNGUS.get(),
            RenderType.cutout()
        );

        // Register Potted Unstable Fungus to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.POTTED_UNSTABLE_FUNGUS.get(),
            RenderType.cutout()
        );

        // Register Potted Time Wood Sapling to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.POTTED_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        // Register Time Crystal Block to use translucent rendering (for glass-like transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TIME_CRYSTAL_BLOCK.get(),
            RenderType.translucent()
        );

        // Register Frozen Time Ice to use translucent rendering (for ice-like transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.FROZEN_TIME_ICE.get(),
            RenderType.translucent()
        );

        // Register Time Wheat to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TIME_WHEAT.get(),
            RenderType.cutout()
        );

        // Register Time Wood Door to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TIME_WOOD_DOOR.get(),
            RenderType.cutout()
        );

        // Register Time Wood Trapdoor to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TIME_WOOD_TRAPDOOR.get(),
            RenderType.cutout()
        );

        // Register Boss Room Door to use cutout rendering (for window transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.BOSS_ROOM_DOOR.get(),
            RenderType.cutout()
        );
    }

    /**
     * Register entity model layers for custom entity models.
     */
    private void registerEntityModelLayers() {
        // Register Time Guardian model layer
        EntityModelLayerRegistry.registerModelLayer(
            TimeGuardianRenderer.LAYER_LOCATION,
            TimeGuardianModel::createBodyLayer
        );

        // Register Time Tyrant model layer
        EntityModelLayerRegistry.registerModelLayer(
            TimeTyrantRenderer.LAYER_LOCATION,
            TimeTyrantModel::createBodyLayer
        );

        // Register Clockwork Sentinel model layer
        EntityModelLayerRegistry.registerModelLayer(
            ClockworkSentinelModel.LAYER_LOCATION,
            ClockworkSentinelModel::createBodyLayer
        );

        // Register Time Keeper model layer
        EntityModelLayerRegistry.registerModelLayer(
            TimeKeeperModel.LAYER_LOCATION,
            TimeKeeperModel::createBodyLayer
        );

        // Register Temporal Wraith model layer
        EntityModelLayerRegistry.registerModelLayer(
            TemporalWraithModel.LAYER_LOCATION,
            TemporalWraithModel::createBodyLayer
        );
    }

    /**
     * Register entity renderers for custom entities.
     */
    private void registerEntityRenderers() {
        // Register Time Guardian with custom renderer
        EntityRendererRegistry.register(
            ModEntities.TIME_GUARDIAN.get(),
            TimeGuardianRenderer::new
        );

        // Register Time Tyrant with custom renderer
        EntityRendererRegistry.register(
            ModEntities.TIME_TYRANT.get(),
            TimeTyrantRenderer::new
        );

        // Register Time Arrow with custom renderer
        EntityRendererRegistry.register(
            ModEntities.TIME_ARROW.get(),
            TimeArrowRenderer::new
        );

        // Register custom mobs with custom renderers
        EntityRendererRegistry.register(
            ModEntities.TEMPORAL_WRAITH.get(),
            TemporalWraithRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            ClockworkSentinelRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.TIME_KEEPER.get(),
            TimeKeeperRenderer::new
        );
    }
}
