package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.UUID;

/**
 * Time Hourglass - Consumable portal ignition item.
 *
 * Used to activate ChronoDawn portals by right-clicking on a valid portal frame.
 *
 * Properties:
 * - Max Stack Size: 64 (consumable item)
 * - Consumed on use: 1 item per portal ignition (except in creative mode)
 *
 * Usage:
 * 1. Build valid portal frame (4x5 to 23x23 rectangle of Clockstone blocks)
 * 2. Right-click frame with Time Hourglass
 * 3. Portal activates (INACTIVE → ACTIVATED)
 * 4. Time Hourglass is consumed (stack shrinks by 1)
 *
 * Portal State Logic:
 * - INACTIVE: Can be ignited with Time Hourglass → ACTIVATED (consumes item)
 * - DEACTIVATED (in ChronoDawn only): Cannot be ignited (does NOT consume item)
 * - STABILIZED: Already active, no action needed
 *
 * Reference: docs/portal_implementation_plan.md (Phase 2)
 */
public class TimeHourglassItem extends Item {
    public TimeHourglassItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Hourglass.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64); // Consumable item - can stack for convenience
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();

        // Only process on server side
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        MinecraftServer server = level.getServer();
        if (server == null) {
            return InteractionResult.FAIL;
        }

        // Check if portals are unstable in ChronoDawn dimension
        if (level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            ChronoDawnGlobalState globalState = ChronoDawnGlobalState.get(server);
            if (globalState.arePortalsUnstable()) {
                if (player != null) {
                    player.displayClientMessage(
                        Component.translatable("message.chronodawn.portal.unstable"),
                        true
                    );
                }
                return InteractionResult.FAIL;
            }
        }

        // Try to find a valid portal frame starting from clicked position
        PortalFrameValidator.PortalFrameData frameData = findPortalFrame(level, clickedPos);

        if (frameData == null) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("message.chronodawn.portal.invalid_frame"),
                    true
                );
            }
            return InteractionResult.FAIL;
        }

        // Fill portal interior with portal blocks
        fillPortalBlocks(level, frameData);

        // Register portal in registry
        UUID portalId = UUID.randomUUID();
        PortalStateMachine portal = new PortalStateMachine(
            portalId,
            level.dimension(),
            frameData.getBottomLeft()
        );
        portal.activate();
        PortalRegistry.getInstance().registerPortal(portal);

        // Play ignition sound (fire charge use - similar to flint and steel)
        level.playSound(
            null,
            clickedPos,
            SoundEvents.FIRECHARGE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        // Consume item (except in creative mode)
        if (player != null && !player.isCreative()) {
            context.getItemInHand().shrink(1);
        }

        if (player != null) {
            player.displayClientMessage(
                Component.translatable("message.chronodawn.portal.ignited"),
                true
            );
        }

        ChronoDawn.LOGGER.debug("Portal ignited at {} in dimension {} by player {}",
            frameData.getBottomLeft(), level.dimension().location(),
            player != null ? player.getName().getString() : "unknown");

        return InteractionResult.CONSUME;
    }

    /**
     * Try to find a valid portal frame starting from clicked position.
     *
     * @param level The level
     * @param clickedPos Position that was clicked
     * @return Portal frame data if valid, null otherwise
     */
    private PortalFrameValidator.PortalFrameData findPortalFrame(Level level, BlockPos clickedPos) {
        // Try to find portal frame by searching for bottom-left corner
        // Check if clicked block is part of a Clockstone frame
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(ModBlocks.CLOCKSTONE_BLOCK.get())) {
            return null;
        }

        // Try different starting positions around clicked block
        // Portal can be oriented along X or Z axis
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            // Try positions in a 23x23 area around clicked position
            for (int dx = -22; dx <= 22; dx++) {
                for (int dy = -22; dy <= 22; dy++) {
                    BlockPos testPos = clickedPos.offset(
                        axis == Direction.Axis.X ? dx : 0,
                        dy,
                        axis == Direction.Axis.Z ? dx : 0
                    );

                    PortalFrameValidator.PortalFrameData frameData =
                        PortalFrameValidator.validateFrame(level, testPos, axis);

                    if (frameData != null) {
                        return frameData;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Fill portal interior with portal blocks.
     *
     * @param level The level
     * @param frameData Portal frame data
     */
    private void fillPortalBlocks(Level level, PortalFrameValidator.PortalFrameData frameData) {
        Set<BlockPos> interiorPositions = frameData.getInteriorPositions();
        BlockState portalState = ModBlocks.CHRONO_DAWN_PORTAL.get()
            .defaultBlockState()
            .setValue(
                com.chronodawn.blocks.ChronoDawnPortalBlock.AXIS,
                frameData.getAxis()
            );

        for (BlockPos pos : interiorPositions) {
            level.setBlock(pos, portalState, 3);
        }

        ChronoDawn.LOGGER.debug("Filled {} portal blocks in frame at {}",
            interiorPositions.size(), frameData.getBottomLeft());
    }
}
