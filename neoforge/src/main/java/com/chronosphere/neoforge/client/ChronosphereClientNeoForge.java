package com.chronosphere.neoforge.client;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.ChronosWardenModel;
import com.chronosphere.client.model.ClockworkColossusModel;
import com.chronosphere.client.model.ClockworkSentinelModel;
import com.chronosphere.client.model.EntropyKeeperModel;
import com.chronosphere.client.model.FloqModel;
import com.chronosphere.client.model.TemporalPhantomModel;
import com.chronosphere.client.model.TemporalWraithModel;
import com.chronosphere.client.model.TimeGuardianModel;
import com.chronosphere.client.model.TimeKeeperModel;
import com.chronosphere.client.model.TimeTyrantModel;
import com.chronosphere.client.renderer.ChronosWardenRenderer;
import com.chronosphere.client.renderer.ChronosphereBoatRenderer;
import com.chronosphere.client.renderer.ChronosphereChestBoatRenderer;
import com.chronosphere.client.renderer.ClockworkColossusRenderer;
import com.chronosphere.client.renderer.EntropyKeeperRenderer;
import com.chronosphere.client.renderer.GearProjectileRenderer;
import com.chronosphere.client.renderer.TemporalPhantomRenderer;
import com.chronosphere.client.renderer.TimeArrowRenderer;
import com.chronosphere.client.renderer.TimeBlastRenderer;
import com.chronosphere.client.renderer.TimeGuardianRenderer;
import com.chronosphere.client.renderer.TimeTyrantRenderer;
import com.chronosphere.client.renderer.mobs.ClockworkSentinelRenderer;
import com.chronosphere.client.renderer.mobs.FloqRenderer;
import com.chronosphere.client.renderer.mobs.TemporalWraithRenderer;
import com.chronosphere.client.renderer.mobs.TimeKeeperRenderer;
import com.chronosphere.items.TimeCompassItem;
import com.chronosphere.neoforge.client.particle.ChronospherePortalParticle;
import com.chronosphere.neoforge.registry.ModParticles;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModEntities;
import com.chronosphere.registry.ModItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.Optional;

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
@EventBusSubscriber(modid = Chronosphere.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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
            registerItemProperties();
        });
    }

    /**
     * Register entity model layers for custom entity models.
     * Called during entity model layer registration phase.
     *
     * @param event The layer definitions registration event
     */
    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register Time Guardian model layer
        event.registerLayerDefinition(
            TimeGuardianRenderer.LAYER_LOCATION,
            TimeGuardianModel::createBodyLayer
        );

        // Register Chronos Warden model layer
        event.registerLayerDefinition(
            ChronosWardenModel.LAYER_LOCATION,
            ChronosWardenModel::createBodyLayer
        );

        // Register Time Tyrant model layer
        event.registerLayerDefinition(
            TimeTyrantRenderer.LAYER_LOCATION,
            TimeTyrantModel::createBodyLayer
        );

        // Register custom mob model layers
        event.registerLayerDefinition(
            TemporalWraithModel.LAYER_LOCATION,
            TemporalWraithModel::createBodyLayer
        );

        event.registerLayerDefinition(
            ClockworkSentinelModel.LAYER_LOCATION,
            ClockworkSentinelModel::createBodyLayer
        );

        event.registerLayerDefinition(
            TimeKeeperModel.LAYER_LOCATION,
            TimeKeeperModel::createBodyLayer
        );

        event.registerLayerDefinition(
            FloqModel.LAYER_LOCATION,
            FloqModel::createBodyLayer
        );

        event.registerLayerDefinition(
            ClockworkColossusRenderer.LAYER_LOCATION,
            ClockworkColossusModel::createBodyLayer
        );

        event.registerLayerDefinition(
            TemporalPhantomRenderer.LAYER_LOCATION,
            TemporalPhantomModel::createBodyLayer
        );

        event.registerLayerDefinition(
            EntropyKeeperRenderer.LAYER_LOCATION,
            EntropyKeeperModel::createBodyLayer
        );

        Chronosphere.LOGGER.info("Registered entity model layers for NeoForge");
    }

    /**
     * Register entity renderers for custom entities.
     * Called during entity renderer registration phase.
     *
     * @param event The entity renderers registration event
     */
    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register Time Guardian with custom renderer
        event.registerEntityRenderer(
            ModEntities.TIME_GUARDIAN.get(),
            TimeGuardianRenderer::new
        );

        // Register Chronos Warden with custom renderer
        event.registerEntityRenderer(
            ModEntities.CHRONOS_WARDEN.get(),
            ChronosWardenRenderer::new
        );

        // Register Time Tyrant with custom renderer
        event.registerEntityRenderer(
            ModEntities.TIME_TYRANT.get(),
            TimeTyrantRenderer::new
        );

        // Register Time Arrow with custom renderer
        event.registerEntityRenderer(
            ModEntities.TIME_ARROW.get(),
            TimeArrowRenderer::new
        );

        // Register Time Blast with custom renderer
        event.registerEntityRenderer(
            ModEntities.TIME_BLAST.get(),
            TimeBlastRenderer::new
        );

        // Register Gear Projectile with custom renderer
        event.registerEntityRenderer(
            ModEntities.GEAR_PROJECTILE.get(),
            GearProjectileRenderer::new
        );

        // Register Clockwork Colossus with custom renderer
        event.registerEntityRenderer(
            ModEntities.CLOCKWORK_COLOSSUS.get(),
            ClockworkColossusRenderer::new
        );

        // Register Temporal Phantom with custom renderer
        event.registerEntityRenderer(
            ModEntities.TEMPORAL_PHANTOM.get(),
            TemporalPhantomRenderer::new
        );

        // Register Entropy Keeper with custom renderer
        event.registerEntityRenderer(
            ModEntities.ENTROPY_KEEPER.get(),
            EntropyKeeperRenderer::new
        );

        // Register custom mobs with renderers
        event.registerEntityRenderer(
            ModEntities.TEMPORAL_WRAITH.get(),
            TemporalWraithRenderer::new
        );

        event.registerEntityRenderer(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            ClockworkSentinelRenderer::new
        );

        event.registerEntityRenderer(
            ModEntities.TIME_KEEPER.get(),
            TimeKeeperRenderer::new
        );

        event.registerEntityRenderer(
            ModEntities.FLOQ.get(),
            FloqRenderer::new
        );

        // Register Chronosphere Boat with custom renderer
        event.registerEntityRenderer(
            ModEntities.CHRONOSPHERE_BOAT.get(),
            ChronosphereBoatRenderer::new
        );

        // Register Chronosphere Chest Boat with custom renderer
        event.registerEntityRenderer(
            ModEntities.CHRONOSPHERE_CHEST_BOAT.get(),
            ChronosphereChestBoatRenderer::new
        );

        Chronosphere.LOGGER.info("Registered entity renderers for NeoForge");
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

        // Register Time Crystal Block to use translucent rendering (for glass-like transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TIME_CRYSTAL_BLOCK.get(),
            RenderType.translucent()
        );

        // Register Frozen Time Ice to use translucent rendering (for ice-like transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.FROZEN_TIME_ICE.get(),
            RenderType.translucent()
        );

        // Register Time Wood Door to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TIME_WOOD_DOOR.get(),
            RenderType.cutout()
        );

        // Register Time Wood Trapdoor to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TIME_WOOD_TRAPDOOR.get(),
            RenderType.cutout()
        );

        // Register Boss Room Door to use cutout rendering (for window transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.BOSS_ROOM_DOOR.get(),
            RenderType.cutout()
        );

        // Register Entropy Crypt Trapdoor to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.ENTROPY_CRYPT_TRAPDOOR.get(),
            RenderType.cutout()
        );

        // Register Time Wheat to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TIME_WHEAT.get(),
            RenderType.cutout()
        );

        // Register Temporal Root to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TEMPORAL_ROOT.get(),
            RenderType.cutout()
        );

        // Register Chrono Melon Stem to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.CHRONO_MELON_STEM.get(),
            RenderType.cutout()
        );

        // Register Attached Chrono Melon Stem to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.ATTACHED_CHRONO_MELON_STEM.get(),
            RenderType.cutout()
        );

        // Register Timeless Mushroom to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.TIMELESS_MUSHROOM.get(),
            RenderType.cutout()
        );

        // Register Dark Time Wood blocks to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.DARK_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.POTTED_DARK_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.DARK_TIME_WOOD_DOOR.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.DARK_TIME_WOOD_TRAPDOOR.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.DARK_TIME_WOOD_LEAVES.get(),
            RenderType.cutoutMipped()
        );

        // Register Ancient Time Wood blocks to use cutout rendering (for transparency)
        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.ANCIENT_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.POTTED_ANCIENT_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.ANCIENT_TIME_WOOD_DOOR.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.ANCIENT_TIME_WOOD_TRAPDOOR.get(),
            RenderType.cutout()
        );

        ItemBlockRenderTypes.setRenderLayer(
            ModBlocks.ANCIENT_TIME_WOOD_LEAVES.get(),
            RenderType.cutoutMipped()
        );
    }

    /**
     * Register block color providers for blocks that need tinting.
     *
     * Time Wood Leaves use a fixed blue color (#78A6DA) regardless of biome.
     * The texture should be grayscale, and the blue color is applied uniformly.
     */
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        // Register Time Wood Leaves to use fixed blue color (not biome-dependent)
        // This ensures leaves remain blue even when placed in other biomes
        // Color: #78A6DA (light blue)
        event.register(
            (state, world, pos, tintIndex) -> 0x78A6DA,
            ModBlocks.TIME_WOOD_LEAVES.get()
        );

        // Register Chrono Melon Stem color (like vanilla melon/pumpkin stems)
        // Color transitions from green (young) to golden-amber (mature)
        event.register(
            (state, world, pos, tintIndex) -> {
                int age = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7);
                // Calculate color based on age (0-7)
                // Young (0): Green (0x5DBF41)
                // Mature (7): Golden-Amber (0xD4AF37)
                int r = (int) (93 + (212 - 93) * age / 7.0);  // 93 -> 212
                int g = (int) (191 + (175 - 191) * age / 7.0); // 191 -> 175
                int b = (int) (65 + (55 - 65) * age / 7.0);   // 65 -> 55
                return (r << 16) | (g << 8) | b;
            },
            ModBlocks.CHRONO_MELON_STEM.get()
        );

        // Register Attached Chrono Melon Stem color (use mature color - golden-amber)
        event.register(
            (state, world, pos, tintIndex) -> 0xD4AF37,
            ModBlocks.ATTACHED_CHRONO_MELON_STEM.get()
        );

        Chronosphere.LOGGER.info("Registered block color handlers for NeoForge");
    }

    /**
     * Register item color providers for items that need tinting.
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // Register the item color as well (for inventory/hand rendering)
        // Use the same blue color (0x78A6DA)
        event.register(
            (stack, tintIndex) -> 0x78A6DA,
            ModBlocks.TIME_WOOD_LEAVES.get()
        );

        // Register item color for Chrono Melon Stem (use mature color)
        event.register(
            (stack, tintIndex) -> 0xD4AF37, // Golden-amber
            ModBlocks.CHRONO_MELON_STEM.get()
        );

        // Register item color for Attached Chrono Melon Stem (use mature color)
        event.register(
            (stack, tintIndex) -> 0xD4AF37, // Golden-amber
            ModBlocks.ATTACHED_CHRONO_MELON_STEM.get()
        );

        Chronosphere.LOGGER.info("Registered item color handlers for NeoForge");
    }

    /**
     * Register particle providers for custom particles.
     *
     * @param event The particle providers registration event
     */
    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        // Register Chronosphere portal particle provider
        event.registerSpriteSet(
            ModParticles.CHRONOSPHERE_PORTAL.get(),
            ChronospherePortalParticle.Provider::new
        );

        Chronosphere.LOGGER.info("Registered particle providers for NeoForge");
    }

    /**
     * Legacy method - now replaced by event-based registration.
     * Kept for compatibility with existing code structure.
     */
    private static void registerBlockColors() {
        // Note: Block color registration is now handled by RegisterColorHandlersEvent
    }

    /**
     * Register item properties for dynamic item rendering.
     *
     * Time Compass uses the "angle" property to rotate the needle
     * based on the target structure's position.
     */
    private static void registerItemProperties() {
        // Register Time Compass angle property
        // This makes the compass needle point towards the target structure
        net.minecraft.client.renderer.item.ItemProperties.register(
            ModItems.TIME_COMPASS.get(),
            ResourceLocation.fromNamespaceAndPath("minecraft", "angle"),
            (stack, level, entity, seed) -> {
                // Get target position from compass NBT
                Optional<GlobalPos> targetPos = TimeCompassItem.getTargetPosition(stack);
                if (targetPos.isEmpty() || level == null) {
                    // No target or no level - return random angle
                    return (float) Math.random();
                }

                GlobalPos target = targetPos.get();

                // Check if we're in the correct dimension
                if (!level.dimension().equals(target.dimension())) {
                    // Wrong dimension - spin randomly
                    return (float) Math.random();
                }

                // Calculate angle to target
                Entity holder = entity != null ? entity : null;
                if (holder == null) {
                    return 0.0f;
                }

                return calculateCompassAngle(holder, target.pos());
            }
        );
    }

    /**
     * Calculate the compass needle angle towards a target position.
     * Returns a value between 0.0 and 1.0, where 0.0 is north.
     *
     * @param entity Entity holding the compass
     * @param targetPos Target block position
     * @return Compass angle (0.0 to 1.0)
     */
    private static float calculateCompassAngle(Entity entity, BlockPos targetPos) {
        // Get entity position
        Vec3 entityPos = entity.position();

        // Calculate direction vector to target
        double dx = targetPos.getX() + 0.5 - entityPos.x;
        double dz = targetPos.getZ() + 0.5 - entityPos.z;

        // Calculate angle in radians
        double angleRadians = Math.atan2(dz, dx);

        // Get entity's yaw (body rotation)
        float yaw = entity.getYRot();

        // Convert to compass angle (0.0 = north, clockwise)
        // atan2 returns angle from east axis, so we need to adjust
        double compassAngle = (angleRadians - Math.toRadians(yaw) + Math.PI) / (Math.PI * 2.0);

        // Normalize to 0.0-1.0 range
        return (float) Mth.positiveModulo(compassAngle, 1.0);
    }
}
