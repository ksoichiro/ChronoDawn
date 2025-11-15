package com.chronosphere.items.artifacts;

import net.minecraft.world.item.Item;

/**
 * Eye of Chronos - Ultimate Artifact Item
 *
 * Drop Source:
 * - Time Tyrant (1 per kill, guaranteed when killed by player)
 *
 * Effect:
 * - Enhanced Time Distortion: When in inventory, all hostile mobs in Chronosphere receive
 *   Slowness V (instead of Slowness IV from default time distortion effect)
 * - This stacks with the base time distortion effect for extreme slow
 *
 * Properties:
 * - Stack Size: 1
 * - Rarity: Epic (light purple text)
 *
 * Lore:
 * The all-seeing eye of Chronos, god of time. Enhances temporal manipulation powers,
 * slowing all hostile creatures to a near standstill in the Chronosphere dimension.
 *
 * Implementation:
 * - Effect is passive - automatically applied when item is in player's inventory
 * - Handled by EntityEventHandler.onLivingTick() (checks for Eye of Chronos in inventory)
 *
 * Reference: T144-T147 - Create Eye of Chronos item with enhanced time distortion effect
 */
public class EyeOfChronosItem extends Item {
    public EyeOfChronosItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Eye of Chronos item.
     *
     * @return Item properties with epic rarity and max stack size 1
     */
    public static Properties createProperties() {
        return new Properties()
            .stacksTo(1)
            .rarity(net.minecraft.world.item.Rarity.EPIC);
    }
}
