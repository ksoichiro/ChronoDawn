package com.chronosphere.fabric.compat;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.registry.ModItems;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom Portal API integration for Fabric.
 *
 * This class integrates with the Custom Portal API library to create
 * the Chronosphere portal functionality on Fabric.
 *
 * Portal Configuration:
 * - Frame Block: Clockstone Block (crafted from 9 Clockstone items)
 * - Ignition Item: Time Hourglass (right-click frame to activate)
 * - Destination: Chronosphere dimension (chronosphere:chronosphere)
 * - Portal Color: Blue-violet (RGB 138, 43, 226) - represents time/temporal theme
 * - Size: 4x5 to 23x23 (validated by PortalFrameValidator)
 *
 * Portal Behavior:
 * - First use: One-way travel to Chronosphere (portal deactivates after entry)
 * - After stabilization: Bidirectional travel between Overworld and Chronosphere
 * - Stabilization: Use Portal Stabilizer item on deactivated portal
 *
 * Reference: specs/001-chronosphere-mod/spec.md (User Story 1 - Portal System)
 * Task: T048 [US1] Integrate Custom Portal API (Fabric)
 */
public class CustomPortalFabric {
    /**
     * Portal tint color - Blue-violet (represents time/temporal theme).
     * RGB(138, 43, 226) - A mystical purple-blue color.
     */
    private static final int PORTAL_COLOR_R = 138;
    private static final int PORTAL_COLOR_G = 43;
    private static final int PORTAL_COLOR_B = 226;

    /**
     * Initialize Custom Portal API integration.
     *
     * This method should be called during Fabric mod initialization
     * to register the Chronosphere portal with Custom Portal API.
     */
    public static void init() {
        Chronosphere.LOGGER.info("Initializing Custom Portal API integration (Fabric)");

        // Get the Chronosphere dimension identifier
        ResourceLocation dimensionId = ModDimensions.CHRONOSPHERE_DIMENSION.location();

        // Register Chronosphere portal using Custom Portal Builder
        CustomPortalBuilder.beginPortal()
            // Set frame block (Clockstone Block)
            .frameBlock(ModBlocks.CLOCKSTONE_BLOCK.get())
            // Set ignition item (Time Hourglass - right-click to activate)
            .lightWithItem(ModItems.TIME_HOURGLASS.get())
            // Set destination dimension (Chronosphere)
            .destDimID(dimensionId)
            // Set portal tint color (blue-violet for time theme)
            .tintColor(PORTAL_COLOR_R, PORTAL_COLOR_G, PORTAL_COLOR_B)
            // Register the portal
            .registerPortal();

        Chronosphere.LOGGER.info("Chronosphere portal registered with Custom Portal API");
        Chronosphere.LOGGER.info("  Frame Block: Clockstone Block");
        Chronosphere.LOGGER.info("  Ignition Item: Time Hourglass");
        Chronosphere.LOGGER.info("  Destination: {}", dimensionId);
        Chronosphere.LOGGER.info("  Portal Color: RGB({}, {}, {})", PORTAL_COLOR_R, PORTAL_COLOR_G, PORTAL_COLOR_B);
    }
}
