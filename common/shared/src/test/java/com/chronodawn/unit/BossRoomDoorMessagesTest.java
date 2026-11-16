package com.chronodawn.unit;

import com.chronodawn.events.BossRoomDoorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BossRoomDoorMessagesTest {

    @Test
    void unlocksOnlyWithKeyAndGears() {
        assertEquals(BossRoomDoorMessages.UNLOCKED, BossRoomDoorMessages.messageKey(true, true));
    }

    @Test
    void namesTheMissingRequirement() {
        assertEquals(BossRoomDoorMessages.MISSING_GEARS, BossRoomDoorMessages.messageKey(true, false));
        assertEquals(BossRoomDoorMessages.MISSING_KEY, BossRoomDoorMessages.messageKey(false, true));
        assertEquals(BossRoomDoorMessages.MISSING_KEY_AND_GEARS, BossRoomDoorMessages.messageKey(false, false));
    }
}
