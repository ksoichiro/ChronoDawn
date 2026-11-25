package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import com.chronodawn.api.event.PortalOpenCause;
import com.chronodawn.api.event.PortalOpenedEvents;
import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalIgnitionRegistration;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Shared portal ignition logic, triggered by right-clicking a Clockstone portal
 * frame while holding any item in the {@code chronodawn:portal_igniters} tag
 * (see {@link com.chronodawn.events.BlockEventHandler}), so modpacks can add
 * their own ignition items without touching this mod's code.
 */
public final class PortalIgnitionHandler {
    private PortalIgnitionHandler() {
    }

    /**
     * Try to ignite a portal at the clicked position using the given item stack.
     * Assumes the caller has already confirmed the clicked block is Clockstone.
     * An existing entry at the frame position is reused (see {@link PortalIgnitionRegistration}),
     * so igniting again does not register a duplicate. Only a STABILIZED entry is replaced by a new
     * ACTIVATED one, and the old entry then remains in the registry and saved data.
     *
     * @param level The level
     * @param clickedPos Position that was clicked
     * @param player The interacting player (may be null)
     * @param stack The item stack used to ignite (consumed by 1 on success, unless creative)
     * @return The interaction result
     */
    public static InteractionResult tryIgnite(Level level, BlockPos clickedPos, Player player, ItemStack stack) {
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
            if (com.chronodawn.config.ChronoDawnConfig.get().gameplay().portals()
                    .enforcesInstabilityGate(globalState.arePortalsUnstable())) {
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
        PortalFrameValidator.PortalFrameData frameData = PortalFrameValidator.findFrameContaining(level, clickedPos);

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

        // Reuse the existing entry at this frame instead of registering a duplicate
        PortalStateMachine portal = PortalIgnitionRegistration.registerOrReuse(
            PortalRegistry.getInstance(), level.dimension(), frameData.getBottomLeft());
        PortalOpenedEvents.fire(portal.getPortalId(), level, frameData.getBottomLeft(), PortalOpenCause.IGNITION, player);

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
            stack.shrink(1);
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
     * Fill portal interior with portal blocks.
     *
     * @param level The level
     * @param frameData Portal frame data
     */
    private static void fillPortalBlocks(Level level, PortalFrameValidator.PortalFrameData frameData) {
        Set<BlockPos> interiorPositions = frameData.getInteriorPositions();
        BlockState portalState = ModBlocks.CHRONO_DAWN_PORTAL.get()
            .defaultBlockState()
            .setValue(
                com.chronodawn.blocks.ChronoDawnPortalBlock.AXIS,
                frameData.getAxis()
            );

        // Flag 18 (UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE): placing blocks one by one otherwise
        // triggers Level#setBlock's neighbor shape-update cascade (independent of
        // UPDATE_NEIGHBORS) before every interior block is placed, so ChronoDawnPortalBlock's
        // updateShape sees not-yet-placed neighbors as missing frame and destroys the portal
        // mid-construction. UPDATE_KNOWN_SHAPE skips that cascade, matching how vanilla places
        // multi-block structures in bulk.
        for (BlockPos pos : interiorPositions) {
            level.setBlock(pos, portalState, 18);
        }

        ChronoDawn.LOGGER.debug("Filled {} portal blocks in frame at {}",
            interiorPositions.size(), frameData.getBottomLeft());
    }
}
