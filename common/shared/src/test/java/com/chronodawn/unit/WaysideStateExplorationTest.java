package com.chronodawn.unit;

import com.chronodawn.core.wayside.PlayerCooldownTracker;
import com.chronodawn.core.wayside.WellQueue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaysideStateExplorationTest {
    private static final UUID[] IDS = {new UUID(0, 1), new UUID(0, 2)};
    private static final int ACTIONS = 10;
    private static final int DEPTH = 5;

    @Test
    void queueOperationSequences() {
        explore(new int[DEPTH], 0, true);
    }

    @Test
    void cooldownOperationSequences() {
        explore(new int[DEPTH], 0, false);
    }

    private void explore(int[] actions, int index, boolean queue) {
        if (index == actions.length) {
            if (queue) checkQueue(actions);
            else checkCooldown(actions);
            return;
        }
        for (int action = 0; action < ACTIONS; action++) {
            actions[index] = action;
            explore(actions, index + 1, queue);
        }
    }

    private void checkQueue(int[] actions) {
        WellQueue queue = new WellQueue();
        List<WellQueue.Entry> expected = new ArrayList<>();
        long time = 0;
        for (int action : actions) {
            if (action < 4) {
                UUID item = IDS[action / 2];
                UUID thrower = IDS[action % 2];
                queue.offer(item, thrower, time);
                if (expected.stream().noneMatch(entry -> entry.item().equals(item))) {
                    expected.add(new WellQueue.Entry(item, thrower, time));
                }
            } else if (action < 6) {
                UUID item = IDS[action - 4];
                queue.forget(item);
                expected.removeIf(entry -> entry.item().equals(item));
            } else if (action < 8) {
                time += action == 6 ? 39 : 1;
            } else {
                List<WellQueue.Entry> due = new ArrayList<>();
                for (WellQueue.Entry entry : expected) {
                    if (time - entry.firstSeen() >= 40) due.add(entry);
                }
                assertEquals(due, queue.due(time));
                expected.removeAll(due);
            }
            assertEquals(expected.size(), queue.size());
            for (UUID item : IDS) {
                assertEquals(expected.stream().anyMatch(entry -> entry.item().equals(item)), queue.contains(item));
            }
        }
        assertEquals(expected, queue.due(time + 40));
        assertEquals(List.of(), queue.due(time + 40));
    }

    private void checkCooldown(int[] actions) {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        Long[] last = new Long[2];
        long time = 0;
        for (int action : actions) {
            if (action < 4) {
                int player = action / 2;
                long cooldown = action % 2 == 0 ? 2 : 3;
                for (int i = 0; i < last.length; i++) {
                    if (last[i] != null && (time < last[i] || time - last[i] >= cooldown)) last[i] = null;
                }
                long remaining = last[player] == null ? 0 : cooldown - (time - last[player]);
                assertEquals(remaining, tracker.tryUse(IDS[player], time, cooldown));
                if (remaining == 0) last[player] = time;
            } else if (action < 7) {
                time += action - 3;
            } else if (action == 7) {
                time = 0;
            } else if (action == 8) {
                tracker = PlayerCooldownTracker.decode(tracker.encode());
            } else {
                for (int i = 0; i < last.length; i++) {
                    long remaining = last[i] == null || time < last[i] ? 0 : Math.max(0, 3 - (time - last[i]));
                    assertEquals(remaining, tracker.remainingTicks(IDS[i], time, 3));
                }
            }
            int size = (last[0] == null ? 0 : 1) + (last[1] == null ? 0 : 1);
            assertEquals(size, tracker.size());
        }
    }
}
