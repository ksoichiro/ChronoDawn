package com.chronodawn.fabric.client;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.*;
import com.chronodawn.client.particle.ChronoDawnPortalParticle;
import com.chronodawn.gui.ChronicleScreen;
import com.chronodawn.gui.data.ChronicleData;
import com.chronodawn.items.ChronicleBookItem;
import com.chronodawn.client.renderer.*;
import com.chronodawn.client.renderer.mobs.*;
import com.chronodawn.items.TimeCompassItem;
import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import com.chronodawn.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
                    ChronoDawn.LOGGER.debug("Chronicle data loaded/reloaded");
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
     * Fabric does not support the render_type field in block model JSON files,
     * so we must register render layers programmatically using BlockRenderLayerMap.
     *
     * Render layer information is defined in ModBlockId using a fluent builder pattern,
     * allowing centralized management of block properties.
     */
    private void registerRenderLayers() {
        for (ModBlockId blockId : ModBlockId.availableForCurrent()) {
            if (blockId.renderLayer() == ModBlockId.RenderLayer.SOLID) {
                continue; // SOLID blocks don't need registration
            }

            ResourceLocation blockLoc = ResourceLocation.fromNamespaceAndPath(
                ChronoDawn.MOD_ID, blockId.id());
            Block block = BuiltInRegistries.BLOCK.get(blockLoc);

            if (block == Blocks.AIR) {
                ChronoDawn.LOGGER.warn("Block not found in registry: {}", blockLoc);
                continue;
            }

            switch (blockId.renderLayer()) {
                case CUTOUT -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
                case CUTOUT_MIPPED -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutoutMipped());
                case TRANSLUCENT -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
                default -> {} // SOLID already handled above
            }
        }
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

        // Register Epoch Husk model layer
        EntityModelLayerRegistry.registerModelLayer(
            EpochHuskModel.LAYER_LOCATION,
            EpochHuskModel::createBodyLayer
        );

        // Register Forgotten Minute model layer
        EntityModelLayerRegistry.registerModelLayer(
            ForgottenMinuteModel.LAYER_LOCATION,
            ForgottenMinuteModel::createBodyLayer
        );

        // Register Chronal Leech model layer
        EntityModelLayerRegistry.registerModelLayer(
            ChronalLeechModel.LAYER_LOCATION,
            ChronalLeechModel::createBodyLayer
        );

        // Register Moment Creeper model layer
        EntityModelLayerRegistry.registerModelLayer(
            MomentCreeperModel.LAYER_LOCATION,
            MomentCreeperModel::createBodyLayer
        );

        // Register Glide Fish model layer
        EntityModelLayerRegistry.registerModelLayer(
            GlideFishModel.LAYER_LOCATION,
            GlideFishModel::createBodyLayer
        );

        // Register Timeline Strider model layer
        EntityModelLayerRegistry.registerModelLayer(
            TimelineStriderModel.LAYER_LOCATION,
            TimelineStriderModel::createBodyLayer
        );

        // Register Hourglass Golem model layer
        EntityModelLayerRegistry.registerModelLayer(
            HourglassGolemModel.LAYER_LOCATION,
            HourglassGolemModel::createBodyLayer
        );

        // Register Secondhand Archer model layer
        EntityModelLayerRegistry.registerModelLayer(
            SecondhandArcherModel.LAYER_LOCATION,
            SecondhandArcherModel::createBodyLayer
        );

        // Register Paradox Crawler model layer
        EntityModelLayerRegistry.registerModelLayer(
            ParadoxCrawlerModel.LAYER_LOCATION,
            ParadoxCrawlerModel::createBodyLayer
        );

        // Register Chrono Turtle model layer
        EntityModelLayerRegistry.registerModelLayer(
            ChronoTurtleModel.LAYER_LOCATION,
            ChronoTurtleModel::createBodyLayer
        );

        // Register Timebound Rabbit model layer
        EntityModelLayerRegistry.registerModelLayer(
            TimeboundRabbitModel.LAYER_LOCATION,
            TimeboundRabbitModel::createBodyLayer
        );

        // Register Pulse Hog model layer
        EntityModelLayerRegistry.registerModelLayer(
            PulseHogModel.LAYER_LOCATION,
            PulseHogModel::createBodyLayer
        );

        // Register Secondwing Fowl model layer
        EntityModelLayerRegistry.registerModelLayer(
            SecondwingFowlModel.LAYER_LOCATION,
            SecondwingFowlModel::createBodyLayer
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

        EntityRendererRegistry.register(
            ModEntities.EPOCH_HUSK.get(),
            EpochHuskRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.FORGOTTEN_MINUTE.get(),
            ForgottenMinuteRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.CHRONAL_LEECH.get(),
            ChronalLeechRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.MOMENT_CREEPER.get(),
            MomentCreeperRenderer::new
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

        // Register Glide Fish with custom renderer
        EntityRendererRegistry.register(
            ModEntities.GLIDE_FISH.get(),
            GlideFishRenderer::new
        );

        // Register Timeline Strider with custom renderer
        EntityRendererRegistry.register(
            ModEntities.TIMELINE_STRIDER.get(),
            TimelineStriderRenderer::new
        );

        // Register Hourglass Golem with custom renderer
        EntityRendererRegistry.register(
            ModEntities.HOURGLASS_GOLEM.get(),
            HourglassGolemRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.SECONDHAND_ARCHER.get(),
            SecondhandArcherRenderer::new
        );

        // Register Paradox Crawler with custom renderer
        EntityRendererRegistry.register(
            ModEntities.PARADOX_CRAWLER.get(),
            ParadoxCrawlerRenderer::new
        );

        // Register Chrono Turtle with custom renderer
        EntityRendererRegistry.register(
            ModEntities.CHRONO_TURTLE.get(),
            ChronoTurtleRenderer::new
        );

        // Register Timebound Rabbit renderer
        EntityRendererRegistry.register(
            ModEntities.TIMEBOUND_RABBIT.get(),
            TimeboundRabbitRenderer::new
        );

        // Register Pulse Hog renderer
        EntityRendererRegistry.register(
            ModEntities.PULSE_HOG.get(),
            PulseHogRenderer::new
        );

        // Register Secondwing Fowl renderer
        EntityRendererRegistry.register(
            ModEntities.SECONDWING_FOWL.get(),
            SecondwingFowlRenderer::new
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

        ChronoDawn.LOGGER.debug("Registered particle providers for Fabric");
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
                return net.minecraft.world.InteractionResultHolder.success(stack);
            }
            return net.minecraft.world.InteractionResultHolder.pass(stack);
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
