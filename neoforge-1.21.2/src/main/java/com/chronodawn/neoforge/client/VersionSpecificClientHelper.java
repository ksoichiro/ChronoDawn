package com.chronodawn.neoforge.client;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Version-specific client helper for NeoForge 1.21.2.
 * Contains client-side code that differs between Minecraft versions.
 */
public class VersionSpecificClientHelper {

    /**
     * Register boat and chest boat model layers.
     * In 1.21.2, custom boats require explicit model layer registration using
     * BoatModel::createBoatModel and BoatModel::createChestBoatModel.
     *
     * @param event The layer definitions registration event
     */
    public static void registerBoatModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (ChronoDawnBoatType type : ChronoDawnBoatType.values()) {
            // Register boat model layer
            ModelLayerLocation boatLayer = new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "boat/" + type.getName()),
                "main"
            );
            event.registerLayerDefinition(boatLayer, BoatModel::createBoatModel);

            // Register chest boat model layer
            ModelLayerLocation chestBoatLayer = new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chest_boat/" + type.getName()),
                "main"
            );
            event.registerLayerDefinition(chestBoatLayer, BoatModel::createChestBoatModel);
        }

        ChronoDawn.LOGGER.info("Registered {} boat and {} chest boat model layers for 1.21.2",
            ChronoDawnBoatType.values().length, ChronoDawnBoatType.values().length);
    }

    /**
     * Handle client tick for portal effects.
     */
    public static void onClientTick() {
        com.chronodawn.client.PortalEffectHandler.onClientTick();
    }
}
