package com.chronodawn.items.quest;

import net.minecraft.world.item.Item;

/**
 * Ancient Gear item - Quest item for progressive unlock in Master Clock dungeon.
 *
 * Obtained by looting chests in Master Clock dungeon rooms.
 * Used as a key for unlocking the boss room door:
 * - Players must collect 3 Ancient Gears to unlock the boss room
 * - Detection logic checks player inventory when approaching boss room door
 *
 * Properties:
 * - Max Stack Size: 64
 * - Required Count: 3 (to unlock boss room)
 *
 * Reference: spec.md (Master Clock structure - Ancient Gears progressive unlock)
 * Tasks: T131a [US3] Create Ancient Gear item
 */
public class AncientGearItem extends Item {
    /**
     * Number of Ancient Gears required to unlock the Master Clock boss room.
     */
    public static final int REQUIRED_COUNT = 3;

    public AncientGearItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Ancient Gear item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64);
    }
}
