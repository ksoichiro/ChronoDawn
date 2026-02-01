package com.chronodawn.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Compatibility wrapper for ResourceLocation creation (Minecraft 1.21.4).
 *
 * This class provides a version-independent way to create ResourceLocation instances.
 * In 1.20.1, ResourceLocation uses a constructor, while 1.21.1+ uses a static factory method.
 *
 * Usage:
 * <pre>{@code
 * // Instead of:
 * ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("chronodawn", "item"); // 1.21.1+ only
 *
 * // Use:
 * ResourceLocation loc = CompatResourceLocation.create("chronodawn", "item"); // Works in both versions
 * }</pre>
 */
public class CompatResourceLocation {
    /**
     * Create a ResourceLocation from namespace and path (1.21.1+ version).
     *
     * @param namespace Mod ID or "minecraft"
     * @param path Resource path (e.g., "blocks/stone")
     * @return ResourceLocation instance
     */
    public static ResourceLocation create(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Parse a ResourceLocation from a string (1.21.1+ version).
     *
     * @param location Location string in "namespace:path" format
     * @return ResourceLocation instance
     */
    public static ResourceLocation parse(String location) {
        return ResourceLocation.parse(location);
    }

    /**
     * Create a ResourceKey for EquipmentAsset from namespace and path (1.21.4+).
     * Used for ArmorMaterial constructor which now requires ResourceKey<EquipmentAsset>.
     *
     * @param namespace Mod ID or "minecraft"
     * @param path Resource path (e.g., "clockstone")
     * @return ResourceKey<EquipmentAsset> instance
     */
    public static ResourceKey<EquipmentAsset> createEquipmentAssetKey(String namespace, String path) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, create(namespace, path));
    }

    private CompatResourceLocation() {
        // Utility class - prevent instantiation
    }
}
