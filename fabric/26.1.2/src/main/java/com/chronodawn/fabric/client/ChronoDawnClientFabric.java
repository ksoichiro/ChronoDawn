package com.chronodawn.fabric.client;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.CobwebColorProvider;
import com.chronodawn.client.LeafColorProvider;
import com.chronodawn.client.TemporalGrassEdgeTint;
import com.chronodawn.client.TemporalPlantColorProvider;
import com.chronodawn.client.model.*;
import com.chronodawn.client.particle.ChronoDawnPortalParticle;
import com.chronodawn.client.particle.ChronoShieldEchoParticle;
import com.chronodawn.client.renderer.*;
import com.chronodawn.client.renderer.mobs.*;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.chronodawn.gui.ChronicleScreen;
import com.chronodawn.gui.data.ChronicleData;
import com.chronodawn.items.ChronicleBookItem;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

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
        // 26.1.2: block render layer registration removed. Minecraft now automatically
        // derives the ChunkSectionLayer from the assigned sprite's properties, so the
        // BlockRenderLayerMap step from earlier versions is no longer needed.
        registerEntityModelLayers();
        registerEntityRenderers();
        registerParticles();
        // registerItemProperties(); // Removed in 1.21.4 - use Client Items model definitions
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
                public Identifier getFabricId() {
                    return Identifier.fromNamespaceAndPath("chronodawn", "chronicle_data");
                }

                @Override
                public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
                    ChronicleData.getInstance().load(resourceManager);
                    ChronoDawn.LOGGER.debug("Chronicle data loaded/reloaded");
                }
            });
    }

    /**
     * Adapts the shared single-tintIndex-0 block color functions (also used, unchanged,
     * by NeoForge's {@code RegisterColorHandlersEvent.Block}) to Fabric API 26.1's
     * IntList-based {@link BlockTintsFactory}. All of ChronoDawn's block color providers
     * only ever supply a single tint (index 0), so this always adds exactly one value.
     */
    @FunctionalInterface
    private interface LegacyBlockColor {
        int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex);
    }

    private static BlockTintsFactory adaptColor(LegacyBlockColor legacy) {
        return (state, world, pos, tints) -> tints.add(legacy.getColor(state, world, pos, 0));
    }

    /**
     * Register block color providers for blocks that need tinting.
     *
     * Time Wood, Dark Time Wood, and Ancient Time Wood leaves use a per-tree
     * palette (cell-hashed by position) supplied by {@link LeafColorProvider}.
     * Textures are grayscale; the tint is applied uniformly to each face.
     *
     * 26.1.2: ColorProviderRegistry was replaced by BlockColorRegistry, whose
     * BlockTintsFactory collects tints into an IntList instead of returning a single
     * int; {@link #adaptColor} bridges the shared (state, world, pos, tintIndex) -> int
     * functions to that new shape.
     */
    private void registerBlockColors() {
        // Per-tree color variation for all 3 Time Wood leaf kinds.
        // Item tints for inventory icons are defined in items/<id>.json (1.21.4+).
        BlockColorRegistry.register(
            adaptColor((state, world, pos, tintIndex) ->
                LeafColorProvider.colorAt(LeafColorProvider.LeafKind.TIME_WOOD, pos)),
            ModBlocks.TIME_WOOD_LEAVES.get()
        );
        BlockColorRegistry.register(
            adaptColor((state, world, pos, tintIndex) ->
                LeafColorProvider.colorAt(LeafColorProvider.LeafKind.DARK_TIME_WOOD, pos)),
            ModBlocks.DARK_TIME_WOOD_LEAVES.get()
        );
        BlockColorRegistry.register(
            adaptColor((state, world, pos, tintIndex) ->
                LeafColorProvider.colorAt(LeafColorProvider.LeafKind.ANCIENT_TIME_WOOD, pos)),
            ModBlocks.ANCIENT_TIME_WOOD_LEAVES.get()
        );

        // Register Chrono Melon Stem color (like vanilla melon/pumpkin stems)
        // Color transitions from green (young) to golden-amber (mature)
        BlockColorRegistry.register(
            adaptColor((state, world, pos, tintIndex) -> {
                int age = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7);
                // Calculate color based on age (0-7)
                // Young (0): Green (0x5DBF41)
                // Mature (7): Golden-Amber (0xD4AF37)
                int r = (int) (93 + (212 - 93) * age / 7.0);  // 93 -> 212
                int g = (int) (191 + (175 - 191) * age / 7.0); // 191 -> 175
                int b = (int) (65 + (55 - 65) * age / 7.0);   // 65 -> 55
                return (r << 16) | (g << 8) | b;
            }),
            ModBlocks.CHRONO_MELON_STEM.get()
        );

        // Item color registration removed in 1.21.4 - use item model definitions instead

        // Register Attached Chrono Melon Stem color (use mature color - golden-amber)
        BlockColorRegistry.register(
            adaptColor((state, world, pos, tintIndex) -> 0xD4AF37),
            ModBlocks.ATTACHED_CHRONO_MELON_STEM.get()
        );

        // Item color registration removed in 1.21.4 - use item model definitions instead

        // Register Temporal Grass Block color: biome grass tint, blended toward a pale
        // edge color near Temporal Sand / Gravel disks. Logic lives in the shared helper.
        BlockColorRegistry.register(
            adaptColor(TemporalGrassEdgeTint::provide),
            ModBlocks.TEMPORAL_GRASS_BLOCK.get()
        );

        // Register Temporal Tall Grass + Temporal Fern colors.
        // Plant textures are baked teal (Chrono Dawn plains grass_color);
        // tint is biased toward the local biome's grass color (biome-weighted; see TemporalPlantColorProvider).
        BlockColorRegistry.register(
            adaptColor((state, world, pos, tintIndex) ->
                TemporalPlantColorProvider.blockTint(world, pos, tintIndex)),
            ModBlocks.TEMPORAL_TALL_GRASS.get(),
            ModBlocks.TEMPORAL_FERN.get(),
            ModBlocks.TEMPORAL_GRASS.get()
        );

        BlockColorRegistry.register(
            adaptColor((state, world, pos, tintIndex) ->
                CobwebColorProvider.blockTint(world, pos, tintIndex)),
            ModBlocks.CHRONO_COBWEB.get()
        );

        // Register Temporal Sand / Gravel sand-side gradient: at d=1 from a Temporal
        // Grass Block, pulls the baked sand/gravel color partway toward grass so the
        // boundary blends symmetrically with the grass-side gradient.
        BlockColorRegistry.register(
            adaptColor(TemporalGrassEdgeTint::provideForSandGravel),
            ModBlocks.TEMPORAL_SAND.get(),
            ModBlocks.TEMPORAL_GRAVEL.get()
        );
    }

    /**
     * Register entity model layers for custom entity models.
     */
    private void registerEntityModelLayers() {
        // Register Boat and Chest Boat model layers for all ChronoDawn boat types
        registerBoatModelLayers();

        // Register Time Guardian model layer
        ModelLayerRegistry.registerModelLayer(
            TimeGuardianRenderer.LAYER_LOCATION,
            TimeGuardianModel::createBodyLayer
        );

        // Register Chronos Warden model layer
        ModelLayerRegistry.registerModelLayer(
            ChronosWardenModel.LAYER_LOCATION,
            ChronosWardenModel::createBodyLayer
        );

        // Register Time Tyrant model layer
        ModelLayerRegistry.registerModelLayer(
            TimeTyrantRenderer.LAYER_LOCATION,
            TimeTyrantModel::createBodyLayer
        );

        // Register Clockwork Sentinel model layer
        ModelLayerRegistry.registerModelLayer(
            ClockworkSentinelModel.LAYER_LOCATION,
            ClockworkSentinelModel::createBodyLayer
        );

        // Register Time Keeper model layer
        ModelLayerRegistry.registerModelLayer(
            TimeKeeperModel.LAYER_LOCATION,
            TimeKeeperModel::createBodyLayer
        );

        // Register Temporal Wraith model layer
        ModelLayerRegistry.registerModelLayer(
            TemporalWraithModel.LAYER_LOCATION,
            TemporalWraithModel::createBodyLayer
        );

        // Register Floq model layer
        ModelLayerRegistry.registerModelLayer(
            FloqModel.LAYER_LOCATION,
            FloqModel::createBodyLayer
        );

        // Register Clockwork Colossus model layer
        ModelLayerRegistry.registerModelLayer(
            ClockworkColossusRenderer.LAYER_LOCATION,
            ClockworkColossusModel::createBodyLayer
        );

        // Register Temporal Phantom model layer
        ModelLayerRegistry.registerModelLayer(
            TemporalPhantomRenderer.LAYER_LOCATION,
            TemporalPhantomModel::createBodyLayer
        );

        // Register Entropy Keeper model layer
        ModelLayerRegistry.registerModelLayer(
            EntropyKeeperRenderer.LAYER_LOCATION,
            EntropyKeeperModel::createBodyLayer
        );

        // Register Epoch Husk model layer
        ModelLayerRegistry.registerModelLayer(
            EpochHuskModel.LAYER_LOCATION,
            EpochHuskModel::createBodyLayer
        );

        // Register Forgotten Minute model layer
        ModelLayerRegistry.registerModelLayer(
            ForgottenMinuteModel.LAYER_LOCATION,
            ForgottenMinuteModel::createBodyLayer
        );

        // Register Chronal Leech model layer
        ModelLayerRegistry.registerModelLayer(
            ChronalLeechModel.LAYER_LOCATION,
            ChronalLeechModel::createBodyLayer
        );

        // Register Moment Creeper model layer
        ModelLayerRegistry.registerModelLayer(
            MomentCreeperModel.LAYER_LOCATION,
            MomentCreeperModel::createBodyLayer
        );

        // Register GlideFish model layer
        ModelLayerRegistry.registerModelLayer(
            GlideFishModel.LAYER_LOCATION,
            GlideFishModel::createBodyLayer
        );

        // Register Timeline Strider model layer
        ModelLayerRegistry.registerModelLayer(
            TimelineStriderModel.LAYER_LOCATION,
            TimelineStriderModel::createBodyLayer
        );

        // Register Hourglass Golem model layer
        ModelLayerRegistry.registerModelLayer(
            HourglassGolemModel.LAYER_LOCATION,
            HourglassGolemModel::createBodyLayer
        );

        // Register Secondhand Archer model layer
        ModelLayerRegistry.registerModelLayer(
            SecondhandArcherModel.LAYER_LOCATION,
            SecondhandArcherModel::createBodyLayer
        );

        // Register Paradox Crawler model layer
        ModelLayerRegistry.registerModelLayer(
            ParadoxCrawlerModel.LAYER_LOCATION,
            ParadoxCrawlerModel::createBodyLayer
        );

        // Register Chrono Turtle model layer
        ModelLayerRegistry.registerModelLayer(
            ChronoTurtleModel.LAYER_LOCATION,
            ChronoTurtleModel::createBodyLayer
        );

        // Register Timebound Rabbit model layer
        ModelLayerRegistry.registerModelLayer(
            TimeboundRabbitModel.LAYER_LOCATION,
            TimeboundRabbitModel::createBodyLayer
        );

        // Register Pulse Hog model layer
        ModelLayerRegistry.registerModelLayer(
            PulseHogModel.LAYER_LOCATION,
            PulseHogModel::createBodyLayer
        );

        // Register Secondwing Fowl model layer
        ModelLayerRegistry.registerModelLayer(
            SecondwingFowlModel.LAYER_LOCATION,
            SecondwingFowlModel::createBodyLayer
        );

        // Register Ticking Sheep model layers (body + wool)
        ModelLayerRegistry.registerModelLayer(
            TickingSheepBodyModel.LAYER_LOCATION,
            TickingSheepBodyModel::createBodyLayer
        );
        ModelLayerRegistry.registerModelLayer(
            TickingSheepWoolModel.LAYER_LOCATION,
            TickingSheepWoolModel::createBodyLayer
        );

        // Register Chrono Bovine model layer
        ModelLayerRegistry.registerModelLayer(
            ChronoBovineModel.LAYER_LOCATION,
            ChronoBovineModel::createBodyLayer
        );
        ModelLayerRegistry.registerModelLayer(
            TemporalCapridModel.LAYER_LOCATION,
            TemporalCapridModel::createBodyLayer
        );
        ModelLayerRegistry.registerModelLayer(
            ChronoUrsidModel.LAYER_LOCATION,
            ChronoUrsidModel::createBodyLayer
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
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "boat/" + type.getName()),
                "main"
            );
            ModelLayerRegistry.registerModelLayer(boatLayer, BoatModel::createBoatModel);

            // Register chest boat model layer
            ModelLayerLocation chestBoatLayer = new ModelLayerLocation(
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chest_boat/" + type.getName()),
                "main"
            );
            ModelLayerRegistry.registerModelLayer(chestBoatLayer, BoatModel::createChestBoatModel);
        }

        ChronoDawn.LOGGER.debug("Registered {} boat and {} chest boat model layers",
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

        // Register custom mobs with custom renderers
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

        EntityRendererRegistry.register(
            ModEntities.GLIDE_FISH.get(),
            GlideFishRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.TIMELINE_STRIDER.get(),
            TimelineStriderRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.HOURGLASS_GOLEM.get(),
            HourglassGolemRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.SECONDHAND_ARCHER.get(),
            SecondhandArcherRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.PARADOX_CRAWLER.get(),
            ParadoxCrawlerRenderer::new
        );

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

        // Register Ticking Sheep renderer
        EntityRendererRegistry.register(
            ModEntities.TICKING_SHEEP.get(),
            TickingSheepRenderer::new
        );

        // Register Chrono Bovine renderer
        EntityRendererRegistry.register(
            ModEntities.CHRONO_BOVINE.get(),
            ChronoBovineRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.TEMPORAL_CAPRID.get(),
            TemporalCapridRenderer::new
        );

        EntityRendererRegistry.register(
            ModEntities.CHRONO_URSID.get(),
            ChronoUrsidRenderer::new
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
        ParticleProviderRegistry.getInstance().register(
            ModParticles.CHRONO_DAWN_PORTAL.get(),
            ChronoDawnPortalParticle.Provider::new
        );

        // Register Chrono Shield Echo particle provider (Time Echo visualization)
        ParticleProviderRegistry.getInstance().register(
            ModParticles.CHRONO_SHIELD_ECHO.get(),
            ChronoShieldEchoParticle.Provider::new
        );

        ChronoDawn.LOGGER.debug("Registered particle providers for Fabric");
    }

    /**
     * Register item properties for dynamic item rendering.
     *
     * Time Compass uses the "angle" property to rotate the needle
     * based on the target structure's position.
     *
     * Note: ItemProperties was removed in 1.21.4. Time Compass angle
     * should be defined via Client Items model definition JSON files.
     * TODO: Implement Time Compass using 1.21.4 Client Items system.
     */
    private void registerItemProperties() {
        // ItemProperties API removed in 1.21.4
        // Time Compass functionality needs to be implemented using
        // the new Client Items model definition system with special model types
        ChronoDawn.LOGGER.info("Time Compass properties registration skipped - requires 1.21.4 Client Items implementation");
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
