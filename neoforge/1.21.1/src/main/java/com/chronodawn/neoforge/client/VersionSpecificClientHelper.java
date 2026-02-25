package com.chronodawn.neoforge.client;

import com.chronodawn.ChronoDawn;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Version-specific client helper for NeoForge 1.21.1.
 * Contains client-side code that differs between Minecraft versions.
 */
public class VersionSpecificClientHelper {

    /**
     * Register boat and chest boat model layers.
     * In 1.21.1, custom boats don't require explicit model layer registration
     * as the vanilla BoatModel handles this differently.
     *
     * @param event The layer definitions registration event
     */
    public static void registerBoatModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // In 1.21.1, boat models are handled differently and don't need explicit registration
        // The BoatModel::createBoatModel and createChestBoatModel methods don't exist in 1.21.1
        ChronoDawn.LOGGER.debug("Boat model layer registration skipped for 1.21.1");
    }

    /**
     * Handle client tick for portal effects.
     */
    public static void onClientTick() {
        com.chronodawn.client.PortalEffectHandler.onClientTick();
    }
}
