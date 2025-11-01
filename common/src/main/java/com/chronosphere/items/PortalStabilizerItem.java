package com.chronosphere.items;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.portal.PortalRegistry;
import com.chronosphere.core.portal.PortalState;
import com.chronosphere.core.portal.PortalStateMachine;
import com.chronosphere.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
            // Relight the portal
            relightPortal(level, portal.getPosition());

            if (player != null) {
                player.displayClientMessage(Component.translatable("item.chronosphere.portal_stabilizer.success"), true);
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
     * Relight the portal by placing portal blocks.
     *
     * @param level Level
     * @param portalPos Portal position (bottom-left corner)
     */
    private void relightPortal(Level level, BlockPos portalPos) {
        // Find the portal frame and fill with portal blocks
        // This is a simplified implementation - assumes 4x5 portal
        for (int x = 1; x <= 2; x++) {
            for (int y = 1; y <= 3; y++) {
                BlockPos portalBlockPos = portalPos.offset(x, y, 0);
                if (level.getBlockState(portalBlockPos).isAir()) {
                    level.setBlock(portalBlockPos, Blocks.NETHER_PORTAL.defaultBlockState(), 3);
                }
            }
        }

        Chronosphere.LOGGER.info("Relit portal at {}", portalPos);
    }
}
