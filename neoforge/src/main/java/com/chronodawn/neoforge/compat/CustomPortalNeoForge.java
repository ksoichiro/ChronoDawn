package com.chronodawn.neoforge.compat;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModItems;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Custom Portal API Reforged integration for NeoForge.
 *
 * This class integrates with the Custom Portal API Reforged library to create
 * the ChronoDawn portal functionality on NeoForge.
 *
 * Portal Configuration:
 * - Frame Block: Clockstone Block (crafted from 9 Clockstone items)
 * - Ignition Item: Time Hourglass (right-click frame to activate)
 * - Destination: ChronoDawn dimension (chronodawn:chronodawn)
 * - Portal Color: Orange (#db8813 / RGB 219, 136, 19) - represents time/temporal theme
 * - Size: 4x5 to 23x23 (validated by PortalFrameValidator)
 *
 * Portal Behavior:
 * - First use: One-way travel to ChronoDawn (portal deactivates after entry)
 * - After stabilization: Bidirectional travel between Overworld and ChronoDawn
 * - Stabilization: Use Portal Stabilizer item on deactivated portal
 *
 * Reference: specs/001-chronosphere-mod/spec.md (User Story 1 - Portal System)
 * Task: T048 [US1] Integrate Custom Portal API (NeoForge)
 */
public class CustomPortalNeoForge {
    /**
     * Portal tint color - Orange (represents time/temporal theme).
     *
     * Color: #db8813 / RGB(219, 136, 19)
     *
     * Design Decision: Orange/gold color theme provides clear visual differentiation
     * from other portal types (Nether: purple, End: green, Aether: blue), while
     * evoking imagery of clock hands, brass gears, and ancient timepieces.
     * The warm metallic tone suggests both aged mechanisms and temporal energy.
     *
     * This color is applied to:
     * - Portal block (the visible portal surface)
     * - Teleport overlay (screen effect during teleportation)
     * - Particles (effects around portal frame)
     *
     * Reference: specs/001-chronosphere-mod/spec.md (Design Decision 2)
     */
    private static final int PORTAL_COLOR_R = 219;
    private static final int PORTAL_COLOR_G = 136;
    private static final int PORTAL_COLOR_B = 19;

    /**
     * Initialize Custom Portal API Reforged integration.
     *
     * This method should be called during NeoForge mod initialization
     * to register the ChronoDawn portal with Custom Portal API Reforged.
     */
    public static void init() {
        ChronoDawn.LOGGER.info("Initializing Custom Portal API Reforged integration (NeoForge)");

        // Get the ChronoDawn dimension identifier
        ResourceLocation dimensionId = ModDimensions.CHRONOSPHERE_DIMENSION.location();

        // Register ChronoDawn portal using Custom Portal Builder
        CustomPortalBuilder.beginPortal()
            // Set frame block (Clockstone Block)
            .frameBlock(ModBlocks.CLOCKSTONE_BLOCK.get())
            // Set ignition item (Time Hourglass - right-click to activate)
            .lightWithItem(ModItems.TIME_HOURGLASS.get())
            // Set destination dimension (ChronoDawn)
            .destDimID(dimensionId)
            // Set portal tint color (orange #db8813 for time theme)
            .tintColor(PORTAL_COLOR_R, PORTAL_COLOR_G, PORTAL_COLOR_B)
            // Register pre-ignition event - prevent ignition of unstable portals in ChronoDawn
            .registerPreIgniteEvent((player, world, portalPos, framePos, ignitionSource) -> {
                // Check if in ChronoDawn dimension with unstable portals
                if (world instanceof ServerLevel serverLevel) {
                    if (serverLevel.dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
                        ChronoDawnGlobalState globalState = ChronoDawnGlobalState.get(serverLevel.getServer());
                        if (globalState.arePortalsUnstable()) {
                            // Portals are unstable - prevent ignition and show warning
                            if (player != null) {
                                player.displayClientMessage(
                                    Component.translatable("item.chronodawn.time_hourglass.portal_deactivated"),
                                    true
                                );
                                ChronoDawn.LOGGER.info("Player {} attempted to ignite portal with Time Hourglass while portals are unstable",
                                    player.getName().getString());
                            }
                            return false; // Prevent ignition
                        }
                    }
                }
                return true; // Allow ignition
            })
            // Register portal ignition event - consume Time Hourglass when portal is successfully ignited
            .registerIgniteEvent((player, world, portalPos, framePos, ignitionSource) -> {
                // Only consume item in survival/adventure mode
                if (player != null && !player.isCreative()) {
                    // Check both hands for Time Hourglass
                    for (InteractionHand hand : InteractionHand.values()) {
                        ItemStack stack = player.getItemInHand(hand);
                        if (stack.is(ModItems.TIME_HOURGLASS.get())) {
                            // Consume one Time Hourglass
                            stack.shrink(1);
                            ChronoDawn.LOGGER.debug("Consumed Time Hourglass for player {} after successful portal ignition (remaining: {})",
                                player.getName().getString(), stack.getCount());
                            break; // Only consume from one hand
                        }
                    }
                }
            })
            // Register the portal
            .registerPortal();

        ChronoDawn.LOGGER.info("ChronoDawn portal registered with Custom Portal API Reforged");
        ChronoDawn.LOGGER.info("  Frame Block: Clockstone Block");
        ChronoDawn.LOGGER.info("  Ignition Item: Time Hourglass");
        ChronoDawn.LOGGER.info("  Destination: {}", dimensionId);
        ChronoDawn.LOGGER.info("  Portal Color: RGB({}, {}, {})", PORTAL_COLOR_R, PORTAL_COLOR_G, PORTAL_COLOR_B);
    }
}
