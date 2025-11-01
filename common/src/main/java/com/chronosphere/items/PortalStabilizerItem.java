package com.chronosphere.items;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.portal.PortalRegistry;
import com.chronosphere.core.portal.PortalState;
import com.chronosphere.core.portal.PortalStateMachine;
import com.chronosphere.data.ChronosphereGlobalState;
import com.chronosphere.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portal Stabilizer - Portal utility item.
 *
 * Used to stabilize deactivated Chronosphere portals, enabling bidirectional travel.
 * Single-use item that is consumed upon successful portal stabilization.
 *
 * Properties:
 * - Max Stack Size: 1
 * - Durability: 1 (consumed after use)
 *
 * Crafting:
 * - Recipe blueprint found in Forgotten Library (Chronosphere dimension)
 * - Requires Clockstone and other materials (recipe defined in data/chronosphere/recipes/)
 *
 * Usage:
 * 1. Find blueprint in Forgotten Library
 * 2. Craft Portal Stabilizer
 * 3. Right-click deactivated portal with Portal Stabilizer
 * 4. Portal becomes stabilized, allowing bidirectional travel
 * 5. Portal Stabilizer is consumed
 *
 * Portal States:
 * - Activated: One-way travel to Chronosphere (created by Time Hourglass)
 * - Deactivated: Portal stops working after player entry
 * - Stabilized: Bidirectional travel enabled (created by Portal Stabilizer)
 *
 * Reference: data-model.md (Items → Tools & Utilities → Portal Stabilizer)
 * Task: T063 [US1] Create Portal Stabilizer item
 */
public class PortalStabilizerItem extends Item {
    public PortalStabilizerItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Portal Stabilizer.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1); // Single-use item
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        // Only process on server side
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Check if clicked block is Clockstone Block (portal frame)
        BlockState clickedBlock = level.getBlockState(clickedPos);
        if (!clickedBlock.is(ModBlocks.CLOCKSTONE_BLOCK.get())) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("item.chronosphere.portal_stabilizer.invalid_block"), true);
            }
            return InteractionResult.FAIL;
        }

        // Search for nearby portal in registry
        PortalStateMachine portal = findNearbyPortal(level, clickedPos);

        if (portal == null) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("item.chronosphere.portal_stabilizer.no_portal"), true);
            }
            return InteractionResult.FAIL;
        }

        // Check portal state
        if (portal.getCurrentState() == PortalState.STABILIZED) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("item.chronosphere.portal_stabilizer.already_stabilized"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!portal.getCurrentState().canBeStabilized()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("item.chronosphere.portal_stabilizer.cannot_stabilize"), true);
            }
            return InteractionResult.FAIL;
        }

        // Stabilize the portal
        if (portal.stabilize()) {
            // Mark global state: Portal has been stabilized
            if (level instanceof ServerLevel serverLevel) {
                ChronosphereGlobalState globalState = ChronosphereGlobalState.get(serverLevel.getServer());
                globalState.markPortalStabilized();
            }

            // Play stabilization effect (visual + audio feedback)
            playStabilizationEffect(level, portal.getPosition());

            // Broadcast message to all players in the server
            if (level instanceof ServerLevel serverLevel) {
                broadcastStabilizationMessage(serverLevel);
            }

            // Consume the item
            itemStack.shrink(1);

            Chronosphere.LOGGER.info("Portal {} stabilized by player {}", portal.getPortalId(), player != null ? player.getName().getString() : "Unknown");
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    /**
     * Find a portal near the clicked position.
     *
     * @param level Level
     * @param pos Clicked position
     * @return Portal state machine, or null if not found
     */
    private PortalStateMachine findNearbyPortal(Level level, BlockPos pos) {
        PortalRegistry registry = PortalRegistry.getInstance();

        // Search in a 10x10x10 area around the clicked position
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos searchPos = pos.offset(x, y, z);
                    PortalStateMachine portal = registry.getPortalAt(searchPos);
                    if (portal != null) {
                        return portal;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Play visual and audio effects for portal stabilization.
     *
     * Creates a dramatic effect when Portal Stabilizer is used:
     * - Particles emanate from portal frame blocks
     * - Sound effects play to indicate successful stabilization
     * - Gives player feedback that something important happened
     *
     * @param level Level
     * @param portalPos Portal position (bottom-left corner)
     */
    private void playStabilizationEffect(Level level, BlockPos portalPos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Find portal frame blocks (4x5 portal assumption)
        // Frame: outer rectangle of Clockstone blocks
        for (int x = 0; x <= 3; x++) {
            for (int y = 0; y <= 4; y++) {
                // Only process frame blocks (outer edge)
                if ((x == 0 || x == 3) || (y == 0 || y == 4)) {
                    BlockPos framePos = portalPos.offset(x, y, 0);

                    // Check if this is actually a Clockstone Block
                    if (!level.getBlockState(framePos).is(ModBlocks.CLOCKSTONE_BLOCK.get())) {
                        continue;
                    }

                    // Spawn particles at this frame block
                    // END_ROD: White particles rising upward (like clock hands)
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.END_ROD,
                        framePos.getX() + 0.5,
                        framePos.getY() + 0.5,
                        framePos.getZ() + 0.5,
                        10, // count
                        0.3, 0.3, 0.3, // spread (x, y, z)
                        0.05 // speed
                    );

                    // FLAME: Orange particles matching portal theme
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        framePos.getX() + 0.5,
                        framePos.getY() + 0.5,
                        framePos.getZ() + 0.5,
                        5, // count
                        0.2, 0.2, 0.2, // spread
                        0.02 // speed
                    );
                }
            }
        }

        // Play sound effect at portal center
        BlockPos centerPos = portalPos.offset(1, 2, 0);
        level.playSound(
            null, // null = all nearby players can hear
            centerPos,
            net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN, // Dramatic portal spawn sound
            net.minecraft.sounds.SoundSource.BLOCKS,
            1.0F, // volume
            1.0F  // pitch
        );

        // Additional sound for "time stabilization" feel
        level.playSound(
            null,
            centerPos,
            net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
            net.minecraft.sounds.SoundSource.BLOCKS,
            0.5F, // quieter than main sound
            1.2F  // slightly higher pitch
        );

        Chronosphere.LOGGER.info("Played stabilization effect at {}", portalPos);
    }

    /**
     * Broadcast stabilization message to all players in the server.
     *
     * @param serverLevel Server level
     */
    private void broadcastStabilizationMessage(ServerLevel serverLevel) {
        Component message = Component.translatable("item.chronosphere.portal_stabilizer.success_reignite_required");

        // Send message to all players
        for (net.minecraft.server.level.ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            player.displayClientMessage(message, true);
        }

        Chronosphere.LOGGER.info("Broadcasted stabilization message to all players");
    }
}
