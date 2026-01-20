package com.chronodawn.fabric.client;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.chronodawn.client.model.ChronosWardenModel;
import com.chronodawn.client.model.ClockworkColossusModel;
import com.chronodawn.client.model.ClockworkSentinelModel;
import com.chronodawn.client.model.EntropyKeeperModel;
import com.chronodawn.client.model.FloqModel;
import com.chronodawn.client.model.TemporalPhantomModel;
import com.chronodawn.client.model.TemporalWraithModel;
import com.chronodawn.client.model.TimeGuardianModel;
import com.chronodawn.client.model.TimeKeeperModel;
import com.chronodawn.client.model.TimeTyrantModel;
import com.chronodawn.client.particle.ChronoDawnPortalParticle;
import com.chronodawn.gui.ChronicleScreen;
import com.chronodawn.gui.data.ChronicleData;
import com.chronodawn.items.ChronicleBookItem;
import com.chronodawn.client.renderer.ChronosWardenRenderer;
import com.chronodawn.client.renderer.ChronoDawnBoatRenderer;
import com.chronodawn.client.renderer.ChronoDawnChestBoatRenderer;
import com.chronodawn.client.renderer.ClockworkColossusRenderer;
import com.chronodawn.client.renderer.EntropyKeeperRenderer;
import com.chronodawn.client.renderer.GearProjectileRenderer;
import com.chronodawn.client.renderer.TemporalPhantomRenderer;
import com.chronodawn.client.renderer.TimeArrowRenderer;
import com.chronodawn.client.renderer.TimeBlastRenderer;
import com.chronodawn.client.renderer.TimeGuardianRenderer;
import com.chronodawn.client.renderer.TimeTyrantRenderer;
import com.chronodawn.client.renderer.mobs.ClockworkSentinelRenderer;
import com.chronodawn.client.renderer.mobs.FloqRenderer;
import com.chronodawn.client.renderer.mobs.TemporalWraithRenderer;
import com.chronodawn.client.renderer.mobs.TimeKeeperRenderer;
import com.chronodawn.items.TimeCompassItem;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import com.chronodawn.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Fabric client-side initialization for ChronoDawn mod.
 *
 * Handles client-only registrations such as:
 * - Block color providers (for tinted blocks like leaves)
 * - Block render layers (for transparent/cutout blocks)
 * - Entity renderers
 * - Particle effects
 *
 * This class is only loaded on the client side.
 */
public class ChronoDawnClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerBlockColors();
        registerRenderLayers();
        registerEntityModelLayers();
        registerEntityRenderers();
        registerParticles();
        registerItemProperties();
        registerChronicleDataLoader();
        registerChronicleBookHandler();
        registerPortalEffects();
    }

    /**
     * Register Chronicle Data resource reload listener.
     * Loads guidebook data from JSON files when resources are loaded/reloaded.
     */
    private void registerChronicleDataLoader() {
        ResourceManagerHelper.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return ResourceLocation.fromNamespaceAndPath("chronodawn", "chronicle_data");
                }

                @Override
                public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
                    ChronicleData.getInstance().load(resourceManager);
                    ChronoDawn.LOGGER.info("Chronicle data loaded/reloaded");
                }
            });
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

        // Register Chrono Melon Stem color (like vanilla melon/pumpkin stems)
        // Color transitions from green (young) to golden-amber (mature)
        ColorProviderRegistry.BLOCK.register(
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

        // Register item color for Chrono Melon Stem (use mature color)
        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> 0xD4AF37, // Golden-amber
            ModBlocks.CHRONO_MELON_STEM.get()
        );

        // Register Attached Chrono Melon Stem color (use mature color - golden-amber)
        ColorProviderRegistry.BLOCK.register(
            (state, world, pos, tintIndex) -> 0xD4AF37,
            ModBlocks.ATTACHED_CHRONO_MELON_STEM.get()
        );

        // Register item color for Attached Chrono Melon Stem
        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> 0xD4AF37,
            ModBlocks.ATTACHED_CHRONO_MELON_STEM.get()
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

        // Register Temporal Root to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TEMPORAL_ROOT.get(),
            RenderType.cutout()
        );

        // Register Chrono Melon Stem to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.CHRONO_MELON_STEM.get(),
            RenderType.cutout()
        );

        // Register Attached Chrono Melon Stem to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ATTACHED_CHRONO_MELON_STEM.get(),
            RenderType.cutout()
        );

        // Register Timeless Mushroom to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.TIMELESS_MUSHROOM.get(),
            RenderType.cutout()
        );

        // Register Purple Time Blossom to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.PURPLE_TIME_BLOSSOM.get(),
            RenderType.cutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.POTTED_PURPLE_TIME_BLOSSOM.get(),
            RenderType.cutout()
        );

        // Register Orange Time Blossom to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ORANGE_TIME_BLOSSOM.get(),
            RenderType.cutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.POTTED_ORANGE_TIME_BLOSSOM.get(),
            RenderType.cutout()
        );

        // Register Pink Time Blossom to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.PINK_TIME_BLOSSOM.get(),
            RenderType.cutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.POTTED_PINK_TIME_BLOSSOM.get(),
            RenderType.cutout()
        );

        // Register Dawn Bell to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.DAWN_BELL.get(),
            RenderType.cutout()
        );

        // Register Dusk Bell to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.DUSK_BELL.get(),
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

        // Register Entropy Crypt Trapdoor to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ENTROPY_CRYPT_TRAPDOOR.get(),
            RenderType.cutout()
        );

        // Register Dark Time Wood blocks to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.DARK_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.POTTED_DARK_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.DARK_TIME_WOOD_DOOR.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.DARK_TIME_WOOD_TRAPDOOR.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.DARK_TIME_WOOD_LEAVES.get(),
            RenderType.cutoutMipped()
        );

        // Register Ancient Time Wood blocks to use cutout rendering (for transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ANCIENT_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.POTTED_ANCIENT_TIME_WOOD_SAPLING.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ANCIENT_TIME_WOOD_DOOR.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ANCIENT_TIME_WOOD_TRAPDOOR.get(),
            RenderType.cutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ANCIENT_TIME_WOOD_LEAVES.get(),
            RenderType.cutoutMipped()
        );

        // Register ChronoDawn Portal to use translucent rendering (for portal transparency)
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.CHRONO_DAWN_PORTAL.get(),
            RenderType.translucent()
        );
    }

    /**
     * Register entity model layers for custom entity models.
     */
    private void registerEntityModelLayers() {
        // Register Boat and Chest Boat model layers for all ChronoDawn boat types
        registerBoatModelLayers();

        // Register Time Guardian model layer
        EntityModelLayerRegistry.registerModelLayer(
            TimeGuardianRenderer.LAYER_LOCATION,
            TimeGuardianModel::createBodyLayer
        );

        // Register Chronos Warden model layer
        EntityModelLayerRegistry.registerModelLayer(
            ChronosWardenModel.LAYER_LOCATION,
            ChronosWardenModel::createBodyLayer
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

        // Register Floq model layer
        EntityModelLayerRegistry.registerModelLayer(
            FloqModel.LAYER_LOCATION,
            FloqModel::createBodyLayer
        );

        // Register Clockwork Colossus model layer
        EntityModelLayerRegistry.registerModelLayer(
            ClockworkColossusRenderer.LAYER_LOCATION,
            ClockworkColossusModel::createBodyLayer
        );

        // Register Temporal Phantom model layer
        EntityModelLayerRegistry.registerModelLayer(
            TemporalPhantomRenderer.LAYER_LOCATION,
            TemporalPhantomModel::createBodyLayer
        );

        // Register Entropy Keeper model layer
        EntityModelLayerRegistry.registerModelLayer(
            EntropyKeeperRenderer.LAYER_LOCATION,
            EntropyKeeperModel::createBodyLayer
        );
    }

    /**
     * Register boat and chest boat model layers for all ChronoDawn boat types.
     * In Minecraft 1.21.2, custom boats require explicit model layer registration.
     */
    private void registerBoatModelLayers() {
        for (ChronoDawnBoatType type : ChronoDawnBoatType.values()) {
            // Register boat model layer
            ModelLayerLocation boatLayer = new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "boat/" + type.getName()),
                "main"
            );
            EntityModelLayerRegistry.registerModelLayer(boatLayer, BoatModel::createBoatModel);

            // Register chest boat model layer
            ModelLayerLocation chestBoatLayer = new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chest_boat/" + type.getName()),
                "main"
            );
            EntityModelLayerRegistry.registerModelLayer(chestBoatLayer, BoatModel::createChestBoatModel);
        }

        ChronoDawn.LOGGER.info("Registered {} boat and {} chest boat model layers",
            ChronoDawnBoatType.values().length, ChronoDawnBoatType.values().length);
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

        // Register Chronos Warden with custom renderer
        EntityRendererRegistry.register(
            ModEntities.CHRONOS_WARDEN.get(),
            ChronosWardenRenderer::new
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

        // Register Time Blast with custom renderer
        EntityRendererRegistry.register(
            ModEntities.TIME_BLAST.get(),
            TimeBlastRenderer::new
        );

        // Register Gear Projectile with custom renderer
        EntityRendererRegistry.register(
            ModEntities.GEAR_PROJECTILE.get(),
            GearProjectileRenderer::new
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

        EntityRendererRegistry.register(
            ModEntities.FLOQ.get(),
            FloqRenderer::new
        );

        // Register Clockwork Colossus with custom renderer
        EntityRendererRegistry.register(
            ModEntities.CLOCKWORK_COLOSSUS.get(),
            ClockworkColossusRenderer::new
        );

        // Register Temporal Phantom with custom renderer
        EntityRendererRegistry.register(
            ModEntities.TEMPORAL_PHANTOM.get(),
            TemporalPhantomRenderer::new
        );

        // Register Entropy Keeper with custom renderer
        EntityRendererRegistry.register(
            ModEntities.ENTROPY_KEEPER.get(),
            EntropyKeeperRenderer::new
        );

        // Register ChronoDawn Boat with custom renderer
        EntityRendererRegistry.register(
            ModEntities.CHRONO_DAWN_BOAT.get(),
            ChronoDawnBoatRenderer::new
        );

        // Register ChronoDawn Chest Boat with custom renderer
        EntityRendererRegistry.register(
            ModEntities.CHRONO_DAWN_CHEST_BOAT.get(),
            ChronoDawnChestBoatRenderer::new
        );
    }

    /**
     * Register particle providers for custom particles.
     */
    private void registerParticles() {
        // Register ChronoDawn portal particle provider
        ParticleFactoryRegistry.getInstance().register(
            ModParticles.CHRONO_DAWN_PORTAL.get(),
            ChronoDawnPortalParticle.Provider::new
        );

        ChronoDawn.LOGGER.info("Registered particle providers for Fabric");
    }

    /**
     * Register item properties for dynamic item rendering.
     *
     * Time Compass uses the "angle" property to rotate the needle
     * based on the target structure's position.
     */
    private void registerItemProperties() {
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

    /**
     * Register Chronicle Book item use handler.
     * Opens the Chronicle GUI when the Chronicle Book is used.
     */
    private void registerChronicleBookHandler() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            var stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ChronicleBookItem) {
                // Only open GUI on client side to avoid server thread errors
                if (world.isClientSide()) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new ChronicleScreen());
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
    }

    /**
     * Register portal effects handler for client tick events.
     * Manages portal-related visual effects (nausea, fade, overlay).
     */
    private void registerPortalEffects() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            com.chronodawn.client.PortalEffectHandler.onClientTick();
        });
    }
}
