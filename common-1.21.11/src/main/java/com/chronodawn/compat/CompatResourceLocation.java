package com.chronodawn.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Compatibility wrapper for Identifier creation (Minecraft 1.21.4).
 *
 * This class provides a version-independent way to create Identifier instances.
 * In 1.20.1, Identifier uses a constructor, while 1.21.1+ uses a static factory method.
 *
 * Usage:
 * <pre>{@code
 * // Instead of:
 * Identifier loc = Identifier.fromNamespaceAndPath("chronodawn", "item"); // 1.21.1+ only
 *
 * // Use:
 * Identifier loc = CompatResourceLocation.create("chronodawn", "item"); // Works in both versions
 * }</pre>
 */
public class CompatResourceLocation {
    /**
     * Create a Identifier from namespace and path (1.21.1+ version).
     *
     * @param namespace Mod ID or "minecraft"
     * @param path Resource path (e.g., "blocks/stone")
     * @return Identifier instance
     */
    public static Identifier create(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Parse a Identifier from a string (1.21.1+ version).
     *
     * @param location Location string in "namespace:path" format
     * @return Identifier instance
     */
    public static Identifier parse(String location) {
        return Identifier.parse(location);
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
