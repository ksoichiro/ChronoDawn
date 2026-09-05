package com.chronodawn.forge.client;

import com.chronodawn.ChronoDawn;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * Version-specific client helper for Forge 1.20.1.
 * Contains client-side code that differs between Minecraft versions.
 */
public class VersionSpecificClientHelper {

    /**
     * Register boat and chest boat model layers.
     * In 1.20.1, custom boats don't require explicit model layer registration
     * (mirrors both Fabric 1.20.1's ChronoDawnClientFabric, which never calls
     * EntityModelLayerRegistry.registerModelLayer for boats, and NeoForge
     * 1.21.1's equivalent no-op).
     *
     * @param event The layer definitions registration event
     */
    public static void registerBoatModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // In 1.20.1, boat models don't need explicit layer registration.
        ChronoDawn.LOGGER.debug("Boat model layer registration skipped for 1.20.1");
    }

    /**
     * Handle client tick for portal effects.
     */
    public static void onClientTick() {
        com.chronodawn.client.PortalEffectHandler.onClientTick();
    }
}
