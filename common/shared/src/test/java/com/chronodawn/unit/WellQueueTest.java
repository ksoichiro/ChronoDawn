package com.chronodawn.unit;

import com.chronodawn.core.wayside.TimeWellOutcome;
import com.chronodawn.core.wayside.WellQueue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WellQueueTest {

    private static final UUID ITEM_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID ITEM_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void notDueBeforeDelay() {
        WellQueue queue = new WellQueue();
        queue.offer(ITEM_A, ALICE, 100);
        assertTrue(queue.due(100 + WellQueue.ANSWER_DELAY_TICKS - 1).isEmpty());
        List<WellQueue.Entry> due = queue.due(100 + WellQueue.ANSWER_DELAY_TICKS);
        assertEquals(1, due.size());
        assertEquals(ALICE, due.get(0).thrower());
    }

    @Test
    void dueEntriesComeOutInFirstSeenOrder() {
        WellQueue queue = new WellQueue();
        queue.offer(ITEM_A, ALICE, 100);
        queue.offer(ITEM_B, BOB, 101);
        List<WellQueue.Entry> due = queue.due(200);
        assertEquals(2, due.size());
        assertEquals(ITEM_A, due.get(0).item());
        assertEquals(ITEM_B, due.get(1).item());
    }

    @Test
    void reofferKeepsFirstSeen() {
        WellQueue queue = new WellQueue();
        queue.offer(ITEM_A, ALICE, 100);
        queue.offer(ITEM_A, BOB, 130);
        List<WellQueue.Entry> due = queue.due(140);
        assertEquals(1, due.size());
        assertEquals(ITEM_A, due.get(0).item());
        assertEquals(ALICE, due.get(0).thrower());
        assertEquals(100, due.get(0).firstSeen());
    }

    @Test
    void dueRemovesEntries() {
        WellQueue queue = new WellQueue();
        queue.offer(ITEM_A, ALICE, 100);
        assertEquals(1, queue.due(140).size());
        assertTrue(queue.due(500).isEmpty());
        assertFalse(queue.contains(ITEM_A));
    }

    @Test
    void forgetDropsEntry() {
        WellQueue queue = new WellQueue();
        queue.offer(ITEM_A, ALICE, 100);
        queue.forget(ITEM_A);
        assertEquals(0, queue.size());
    }

    @Test
    void exchangeMessageWinsOverCooldown() {
        assertEquals(TimeWellOutcome.EXCHANGED,
                TimeWellOutcome.mostImportant(TimeWellOutcome.COOLDOWN, TimeWellOutcome.EXCHANGED));
        assertEquals(TimeWellOutcome.COOLDOWN,
                TimeWellOutcome.mostImportant(TimeWellOutcome.NOT_ACCEPTED, TimeWellOutcome.COOLDOWN));
        assertEquals(TimeWellOutcome.NOT_ACCEPTED,
                TimeWellOutcome.mostImportant(null, TimeWellOutcome.NOT_ACCEPTED));
    }

    @Test
    void throwerGoneHasNoMessage() {
        assertNull(TimeWellOutcome.THROWER_GONE.messageKey());
        assertEquals("message.chronodawn.time_well.answered", TimeWellOutcome.EXCHANGED.messageKey());
    }
}
