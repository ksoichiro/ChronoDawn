package com.chronodawn.unit;

import com.chronodawn.core.wayside.PlayerCooldownTracker;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerCooldownTrackerTest {

    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final long COOLDOWN = 24000;

    @Test
    void firstUseIsAccepted() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        assertEquals(0, tracker.tryUse(ALICE, 1000, COOLDOWN));
    }

    @Test
    void secondUseWithinCooldownIsRefusedWithRemainingTicks() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 1000, COOLDOWN);
        assertEquals(COOLDOWN - 500, tracker.tryUse(ALICE, 1500, COOLDOWN));
    }

    @Test
    void refusedUseDoesNotRestartCooldown() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 1000, COOLDOWN);
        tracker.tryUse(ALICE, 1500, COOLDOWN);
        assertEquals(0, tracker.tryUse(ALICE, 1000 + COOLDOWN, COOLDOWN));
    }

    @Test
    void useIsAcceptedExactlyWhenCooldownEnds() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 1000, COOLDOWN);
        assertEquals(1, tracker.tryUse(ALICE, 1000 + COOLDOWN - 1, COOLDOWN));
        assertEquals(0, tracker.tryUse(ALICE, 1000 + COOLDOWN, COOLDOWN));
    }

    @Test
    void playersAreIndependent() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 1000, COOLDOWN);
        assertEquals(0, tracker.tryUse(BOB, 1001, COOLDOWN));
        assertTrue(tracker.tryUse(ALICE, 1002, COOLDOWN) > 0);
    }

    @Test
    void shorterCooldownAppliesToExistingEntries() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 1000, COOLDOWN);
        assertEquals(0, tracker.tryUse(ALICE, 1100, 100));
    }

    @Test
    void expiredEntriesArePrunedOnUse() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 1000, COOLDOWN);
        tracker.tryUse(BOB, 1000 + COOLDOWN, COOLDOWN);
        assertEquals(1, tracker.size());
    }

    @Test
    void gameTimeBeforeLastUseIsTreatedAsReady() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 50000, COOLDOWN);
        assertEquals(0, tracker.tryUse(ALICE, 10, COOLDOWN));
    }

    @Test
    void encodeDecodeRoundTrip() {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        tracker.tryUse(ALICE, 1000, COOLDOWN);
        tracker.tryUse(BOB, 2000, COOLDOWN);
        PlayerCooldownTracker copy = PlayerCooldownTracker.decode(tracker.encode());
        assertEquals(COOLDOWN - 2000, copy.remainingTicks(ALICE, 3000, COOLDOWN));
        assertEquals(COOLDOWN - 1000, copy.remainingTicks(BOB, 3000, COOLDOWN));
        assertEquals(2, copy.size());
    }

    @Test
    void decodeOfEmptyOrNullIsEmpty() {
        assertEquals(0, PlayerCooldownTracker.decode("").size());
        assertEquals(0, PlayerCooldownTracker.decode(null).size());
    }

    @Test
    void decodeSkipsMalformedEntries() {
        String encoded = "not-a-uuid=5,"
            + ALICE + "=abc,"
            + BOB + "=1234,"
            + "=,"
            + "garbage";
        PlayerCooldownTracker tracker = PlayerCooldownTracker.decode(encoded);
        assertEquals(1, tracker.size());
        assertEquals(COOLDOWN - 766, tracker.remainingTicks(BOB, 2000, COOLDOWN));
    }

    @Test
    void minutesRoundUp() {
        assertEquals(1, PlayerCooldownTracker.toMinutesRoundedUp(1));
        assertEquals(1, PlayerCooldownTracker.toMinutesRoundedUp(1200));
        assertEquals(2, PlayerCooldownTracker.toMinutesRoundedUp(1201));
        assertEquals(20, PlayerCooldownTracker.toMinutesRoundedUp(24000));
    }
}
