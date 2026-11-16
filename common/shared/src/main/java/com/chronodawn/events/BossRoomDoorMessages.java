package com.chronodawn.events;

/**
 * Chooses the overlay message for the Master Clock boss room door.
 *
 * The door needs both the Key to Master Clock and 3 Ancient Gears. The message
 * names whichever requirement is missing, so a player holding enough gears but
 * no key is told about the key rather than about the gears.
 */
public final class BossRoomDoorMessages {
    public static final String UNLOCKED = "message.chronodawn.boss_room_unlocked";
    public static final String MISSING_GEARS = "message.chronodawn.boss_room_locked";
    public static final String MISSING_KEY = "message.chronodawn.master_clock_locked";
    public static final String MISSING_KEY_AND_GEARS = "message.chronodawn.boss_room_locked_key_and_gears";

    private BossRoomDoorMessages() {
    }

    public static String messageKey(boolean hasKey, boolean hasGears) {
        if (hasKey && hasGears) {
            return UNLOCKED;
        }
        if (hasKey) {
            return MISSING_GEARS;
        }
        return hasGears ? MISSING_KEY : MISSING_KEY_AND_GEARS;
    }
}
