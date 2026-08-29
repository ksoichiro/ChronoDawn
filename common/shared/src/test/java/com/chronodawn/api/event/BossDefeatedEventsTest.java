/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.api.event;

import com.chronodawn.entities.bosses.BossKind;
import com.chronodawn.unit.TestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BossDefeatedEventsTest {

    private static final String[] VERSION_DIRS = {
        "1.20.1", "1.21.1", "1.21.2", "1.21.4", "1.21.5", "1.21.6",
        "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11"
    };

    private static final Map<BossKind, String> BOSS_CLASSES = Map.of(
        BossKind.TIME_GUARDIAN, "TimeGuardianEntity.java",
        BossKind.CHRONOS_WARDEN, "ChronosWardenEntity.java",
        BossKind.CLOCKWORK_COLOSSUS, "ClockworkColossusEntity.java",
        BossKind.ENTROPY_KEEPER, "EntropyKeeperEntity.java",
        BossKind.TEMPORAL_PHANTOM, "TemporalPhantomEntity.java",
        BossKind.TIME_TYRANT, "TimeTyrantEntity.java"
    );

    private static final BossDefeatedContext CONTEXT = new BossDefeatedContext() {
        @Override
        public String bossId() {
            return "chronodawn:time_guardian";
        }

        @Override
        public LivingEntity boss() {
            return null;
        }

        @Override
        public ServerLevel level() {
            return null;
        }

        @Override
        public BlockPos position() {
            return null;
        }

        @Override
        public DamageSource damageSource() {
            return null;
        }

        @Override
        public ServerPlayer defeatingPlayer() {
            return null;
        }
    };

    private final List<BossDefeatedListener> registrations = new ArrayList<>();

    @AfterEach
    void unregisterListeners() {
        for (BossDefeatedListener listener : registrations) {
            BossDefeatedEvents.unregister(listener);
        }
    }

    @Test
    void listenersRunInRegistrationOrder() {
        List<Integer> calls = new ArrayList<>();
        register(context -> calls.add(1));
        register(context -> calls.add(2));

        BossDefeatedEvents.dispatch(CONTEXT);

        assertEquals(List.of(1, 2), calls);
    }

    @Test
    void unregisterRemovesTheListener() {
        List<String> calls = new ArrayList<>();
        BossDefeatedListener listener = context -> calls.add(context.bossId());
        register(listener);

        BossDefeatedEvents.unregister(listener);
        registrations.remove(listener);
        BossDefeatedEvents.dispatch(CONTEXT);

        assertEquals(List.of(), calls);
    }

    @Test
    void duplicateRegistrationsProduceDuplicateCallbacks() {
        List<String> calls = new ArrayList<>();
        BossDefeatedListener listener = context -> calls.add(context.bossId());
        register(listener);
        register(listener);

        BossDefeatedEvents.dispatch(CONTEXT);

        assertEquals(List.of("chronodawn:time_guardian", "chronodawn:time_guardian"), calls);
    }

    @Test
    void failingListenerDoesNotSuppressLaterListeners() {
        List<String> calls = new ArrayList<>();
        register(context -> {
            throw new IllegalStateException("expected test failure");
        });
        register(context -> calls.add(context.bossId()));

        assertDoesNotThrow(() -> BossDefeatedEvents.dispatch(CONTEXT));
        assertEquals(List.of("chronodawn:time_guardian"), calls);
    }

    @Test
    void nullListenersAreRejected() {
        assertThrows(NullPointerException.class, () -> BossDefeatedEvents.register(null));
        assertThrows(NullPointerException.class, () -> BossDefeatedEvents.unregister(null));
    }

    @Test
    void bossKindsExposeStableNamespacedIds() {
        assertEquals("chronodawn:time_guardian", BossKind.TIME_GUARDIAN.eventId());
        assertEquals("chronodawn:chronos_warden", BossKind.CHRONOS_WARDEN.eventId());
        assertEquals("chronodawn:clockwork_colossus", BossKind.CLOCKWORK_COLOSSUS.eventId());
        assertEquals("chronodawn:entropy_keeper", BossKind.ENTROPY_KEEPER.eventId());
        assertEquals("chronodawn:temporal_phantom", BossKind.TEMPORAL_PHANTOM.eventId());
        assertEquals("chronodawn:time_tyrant", BossKind.TIME_TYRANT.eventId());
    }

    @Test
    void everyVersionDispatchesEveryBossExactlyOnce() throws IOException {
        Path projectRoot = Path.of(TestUtils.getProjectRoot());

        for (String version : VERSION_DIRS) {
            Path bossDir = projectRoot.resolve("common").resolve(version)
                .resolve("src/main/java/com/chronodawn/entities/bosses");
            for (Map.Entry<BossKind, String> entry : BOSS_CLASSES.entrySet()) {
                String source = Files.readString(bossDir.resolve(entry.getValue()), StandardCharsets.UTF_8);
                String dispatch = "BossDefeatedEvents.fire(BossKind." + entry.getKey().name() + ", this, ";

                assertEquals(1, countOccurrences(source, dispatch),
                    version + " " + entry.getValue() + " must dispatch its mapped boss event exactly once");
            }
        }
    }

    private void register(BossDefeatedListener listener) {
        BossDefeatedEvents.register(listener);
        registrations.add(listener);
    }

    private static int countOccurrences(String source, String target) {
        int count = 0;
        int offset = 0;
        while ((offset = source.indexOf(target, offset)) >= 0) {
            count++;
            offset += target.length();
        }
        return count;
    }
}
