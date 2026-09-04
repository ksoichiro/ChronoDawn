# Portal Opened Event API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a loader-neutral `PortalOpenedEvents` Java API, mirroring the
existing `BossDefeatedEvents` API, that fires once whenever a Chrono Dawn
portal transitions into the `ACTIVATED` state.

**Architecture:** Three public types plus an enum live in
`common/shared/src/main/java/com/chronodawn/api/event/`
(`PortalOpenedEvents`, `PortalOpenedContext`, `PortalOpenedListener`,
`PortalOpenCause`), copying the registration/dispatch/exception-isolation
shape of `BossDefeatedEvents` exactly. Two call sites per Minecraft version
module dispatch the event: `TimeHourglassItem.useOn()` (cause `IGNITION`)
and `PortalTeleportHandler.generatePortalStructure()` (cause `REIGNITION`,
fired only when that call actually transitions a portal to `ACTIVATED`).

**Tech Stack:** Java 21, JUnit 5, Architectury multi-loader common modules
(11 Minecraft version directories under `common/`).

**Spec:** `docs/superpowers/specs/2026-09-04-portal-opened-event-design.md`

## Global Constraints

- Public package: `com.chronodawn.api.event`. Type and method names must
  match this plan exactly — other modpack-author-readiness docs will link to
  them.
- The event fires on the logical server only, synchronously, after portal
  blocks are placed and the state machine has actually transitioned to
  `ACTIVATED`. It never fires for a `STABILIZED` regeneration or any other
  no-op call.
- Listener exceptions are caught and logged per listener; they must never
  interrupt portal activation or later listeners.
- `PortalOpenedEvents` must not expose Architectury's `Event` type or the
  internal `PortalStateMachine` / `PortalRegistry` classes.
- Every one of the 11 supported common modules
  (`1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9,
  1.21.10, 1.21.11`) must dispatch exactly once from `TimeHourglassItem` and
  exactly once from `PortalTeleportHandler.generatePortalStructure()`.
- License header on new files under `common/shared/src/main/java` copies the
  existing LGPL-3.0 header verbatim (see `BossDefeatedEvents.java`), with
  `Copyright (C) 2025 ksoichiro`, matching the project-wide convention of
  keeping the original copyright year even for files added later.
- `./gradlew checkAll` must pass before this plan is done.

---

### Task 1: Public event API types and registration/dispatch tests

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/api/event/PortalOpenCause.java`
- Create: `common/shared/src/main/java/com/chronodawn/api/event/PortalOpenedListener.java`
- Create: `common/shared/src/main/java/com/chronodawn/api/event/PortalOpenedContext.java`
- Create: `common/shared/src/main/java/com/chronodawn/api/event/PortalOpenedEvents.java`
- Test: `common/shared/src/test/java/com/chronodawn/api/event/PortalOpenedEventsTest.java`

**Interfaces:**
- Produces: `PortalOpenCause` enum with values `IGNITION`, `REIGNITION`.
  `PortalOpenedListener.onPortalOpened(PortalOpenedContext)`.
  `PortalOpenedContext.portalId(): UUID`, `.level(): ServerLevel`,
  `.position(): BlockPos`, `.cause(): PortalOpenCause`,
  `.igniter(): @Nullable ServerPlayer`.
  `PortalOpenedEvents.register(PortalOpenedListener)`,
  `.unregister(PortalOpenedListener)`, and the internal
  `@ApiStatus.Internal static void fire(UUID portalId, Level level,
  BlockPos position, PortalOpenCause cause, @Nullable Player igniter)`
  (accepts the base `Level`/`Player` types so version-specific call sites
  never need to cast; `fire` narrows to `ServerLevel`/`ServerPlayer`
  internally and silently no-ops on a client level).
- Consumes: nothing from earlier tasks (this is the foundation task).

- [ ] **Step 1: Write the failing test file**

```java
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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalOpenedEventsTest {

    private static final UUID PORTAL_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final PortalOpenedContext CONTEXT = new PortalOpenedContext() {
        @Override
        public UUID portalId() {
            return PORTAL_ID;
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
        public PortalOpenCause cause() {
            return PortalOpenCause.IGNITION;
        }

        @Override
        public ServerPlayer igniter() {
            return null;
        }
    };

    private final List<PortalOpenedListener> registrations = new ArrayList<>();

    @AfterEach
    void unregisterListeners() {
        for (PortalOpenedListener listener : registrations) {
            PortalOpenedEvents.unregister(listener);
        }
    }

    @Test
    void listenersRunInRegistrationOrder() {
        List<Integer> calls = new ArrayList<>();
        register(context -> calls.add(1));
        register(context -> calls.add(2));

        PortalOpenedEvents.dispatch(CONTEXT);

        assertEquals(List.of(1, 2), calls);
    }

    @Test
    void unregisterRemovesTheListener() {
        List<UUID> calls = new ArrayList<>();
        PortalOpenedListener listener = context -> calls.add(context.portalId());
        register(listener);

        PortalOpenedEvents.unregister(listener);
        registrations.remove(listener);
        PortalOpenedEvents.dispatch(CONTEXT);

        assertEquals(List.of(), calls);
    }

    @Test
    void duplicateRegistrationsProduceDuplicateCallbacks() {
        List<UUID> calls = new ArrayList<>();
        PortalOpenedListener listener = context -> calls.add(context.portalId());
        register(listener);
        register(listener);

        PortalOpenedEvents.dispatch(CONTEXT);

        assertEquals(List.of(PORTAL_ID, PORTAL_ID), calls);
    }

    @Test
    void failingListenerDoesNotSuppressLaterListeners() {
        List<UUID> calls = new ArrayList<>();
        register(context -> {
            throw new IllegalStateException("expected test failure");
        });
        register(context -> calls.add(context.portalId()));

        assertDoesNotThrow(() -> PortalOpenedEvents.dispatch(CONTEXT));
        assertEquals(List.of(PORTAL_ID), calls);
    }

    @Test
    void nullListenersAreRejected() {
        assertThrows(NullPointerException.class, () -> PortalOpenedEvents.register(null));
        assertThrows(NullPointerException.class, () -> PortalOpenedEvents.unregister(null));
    }

    @Test
    void portalOpenCauseHasExactlyTwoStableValues() {
        assertEquals(List.of(PortalOpenCause.IGNITION, PortalOpenCause.REIGNITION), List.of(PortalOpenCause.values()));
    }

    private void register(PortalOpenedListener listener) {
        PortalOpenedEvents.register(listener);
        registrations.add(listener);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails to compile**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.api.event.PortalOpenedEventsTest"`
Expected: FAIL — compilation error, `PortalOpenedEvents`/`PortalOpenedContext`/`PortalOpenedListener`/`PortalOpenCause` do not exist.

- [ ] **Step 3: Create `PortalOpenCause.java`**

```java
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

/** Why a {@link PortalOpenedContext} portal transitioned into the active state. */
public enum PortalOpenCause {

    /** A player ignited a valid frame with a Time Hourglass. */
    IGNITION,

    /**
     * The mod generated a new return portal, or reactivated an existing
     * frame, while teleporting an entity through an already-active portal.
     */
    REIGNITION
}
```

- [ ] **Step 4: Create `PortalOpenedListener.java`**

```java
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

/** Receives server-side Chrono Dawn portal activation notifications. */
@FunctionalInterface
public interface PortalOpenedListener {

    /** Called synchronously after the portal's blocks are placed and its state becomes {@code ACTIVATED}. */
    void onPortalOpened(PortalOpenedContext context);
}
```

- [ ] **Step 5: Create `PortalOpenedContext.java`**

```java
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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Read-only context supplied when a Chrono Dawn portal transitions into the
 * active state.
 *
 * <p>The callback is synchronous. Values should not be retained beyond the
 * callback; copy anything an integration needs later.
 */
public interface PortalOpenedContext {

    /** Stable identifier of the physical portal, from the internal registry. */
    UUID portalId();

    /** The server level containing the portal frame. */
    ServerLevel level();

    /** The frame's bottom-left block position. */
    BlockPos position();

    /** Why the portal transitioned into the active state. */
    PortalOpenCause cause();

    /**
     * Player credited with the activation, or {@code null} when none is
     * known for this cause.
     */
    @Nullable
    ServerPlayer igniter();
}
```

- [ ] **Step 6: Create `PortalOpenedEvents.java`**

```java
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

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/** Public registration point for Chrono Dawn portal activation notifications. */
public final class PortalOpenedEvents {

    private static final CopyOnWriteArrayList<PortalOpenedListener> LISTENERS = new CopyOnWriteArrayList<>();

    private PortalOpenedEvents() {}

    /**
     * Registers a listener. Listeners run in registration order.
     *
     * <p>Registering the same object more than once creates one callback per
     * registration.
     */
    public static void register(PortalOpenedListener listener) {
        LISTENERS.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Removes the first registration of the listener, if present. */
    public static void unregister(PortalOpenedListener listener) {
        LISTENERS.remove(Objects.requireNonNull(listener, "listener"));
    }

    /** Internal entry point used by the version-specific portal activation sites. */
    @ApiStatus.Internal
    public static void fire(UUID portalId, Level level, BlockPos position, PortalOpenCause cause, @Nullable Player igniter) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer igniterPlayer = igniter instanceof ServerPlayer player ? player : null;
        dispatch(new DefaultContext(portalId, serverLevel, position.immutable(), cause, igniterPlayer));
    }

    static void dispatch(PortalOpenedContext context) {
        for (PortalOpenedListener listener : LISTENERS) {
            try {
                listener.onPortalOpened(context);
            } catch (RuntimeException e) {
                ChronoDawn.LOGGER.error(
                    "Portal opened listener {} failed for portal {}",
                    listener.getClass().getName(),
                    context.portalId(),
                    e
                );
            }
        }
    }

    private record DefaultContext(
        UUID portalId,
        ServerLevel level,
        BlockPos position,
        PortalOpenCause cause,
        @Nullable ServerPlayer igniter
    ) implements PortalOpenedContext {}
}
```

- [ ] **Step 7: Run the test to verify it passes**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.api.event.PortalOpenedEventsTest"`
Expected: PASS, all 6 tests green.

- [ ] **Step 8: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/api/event/PortalOpenCause.java \
        common/shared/src/main/java/com/chronodawn/api/event/PortalOpenedListener.java \
        common/shared/src/main/java/com/chronodawn/api/event/PortalOpenedContext.java \
        common/shared/src/main/java/com/chronodawn/api/event/PortalOpenedEvents.java \
        common/shared/src/test/java/com/chronodawn/api/event/PortalOpenedEventsTest.java
git commit -m "feat(events): add portal-opened event API"
```

---

### Task 2: Wire `TimeHourglassItem` dispatch across all 11 versions

**Files:**
- Modify: `common/1.20.1/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.2/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.4/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.5/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.6/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.7/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.8/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.9/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/items/TimeHourglassItem.java`
- Modify: `common/shared/src/test/java/com/chronodawn/api/event/PortalOpenedEventsTest.java`

**Interfaces:**
- Consumes: `PortalOpenedEvents.fire(UUID, Level, BlockPos, PortalOpenCause, Player)` and
  `PortalOpenCause.IGNITION` from Task 1.
- Produces: nothing new; this task only adds dispatch call sites.

These 11 files fall into two identical groups. Group A
(`1.20.1`, `1.21.1`) ignites synchronously inside `useOn()`. Group B
(`1.21.2` through `1.21.11`) defers ignition into a `server.execute()`
lambda; the edit content is otherwise identical across all 9 Group B
files.

- [ ] **Step 1: Extend the test with a source guard for both groups (still failing)**

Add to `common/shared/src/test/java/com/chronodawn/api/event/PortalOpenedEventsTest.java`, inside the test class, alongside the existing imports:

```java
import com.chronodawn.unit.TestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
```

Add these two fields near the top of the class, next to `PORTAL_ID`:

```java
    private static final String[] VERSION_DIRS = {
        "1.20.1", "1.21.1", "1.21.2", "1.21.4", "1.21.5", "1.21.6",
        "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11"
    };

    private static final String IGNITION_DISPATCH = "PortalOpenedEvents.fire(portal.getPortalId(), level, ";
```

Add this test method:

```java
    @Test
    void everyVersionDispatchesIgnitionExactlyOnce() throws IOException {
        Path projectRoot = Path.of(TestUtils.getProjectRoot());

        for (String version : VERSION_DIRS) {
            Path file = projectRoot.resolve("common").resolve(version)
                .resolve("src/main/java/com/chronodawn/items/TimeHourglassItem.java");
            String source = Files.readString(file, StandardCharsets.UTF_8);

            assertEquals(1, countOccurrences(source, IGNITION_DISPATCH),
                version + " TimeHourglassItem.java must dispatch the ignition portal-opened event exactly once");
        }
    }
```

Add this helper method (private, at the bottom of the class near `register`):

```java
    private static int countOccurrences(String source, String target) {
        int count = 0;
        int offset = 0;
        while ((offset = source.indexOf(target, offset)) >= 0) {
            count++;
            offset += target.length();
        }
        return count;
    }
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.api.event.PortalOpenedEventsTest"`
Expected: FAIL on `everyVersionDispatchesIgnitionExactlyOnce` — `expected: <1> but was: <0>` for `1.20.1`.

- [ ] **Step 3: Edit `common/1.20.1/src/main/java/com/chronodawn/items/TimeHourglassItem.java` and `common/1.21.1/.../TimeHourglassItem.java` (Group A, identical edit for both files)**

Edit the import block. Replace:

```java
import com.chronodawn.ChronoDawn;
import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
```

with:

```java
import com.chronodawn.ChronoDawn;
import com.chronodawn.api.event.PortalOpenCause;
import com.chronodawn.api.event.PortalOpenedEvents;
import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
```

Edit the ignition block in `useOn()`. Replace:

```java
        // Register portal in registry
        UUID portalId = UUID.randomUUID();
        PortalStateMachine portal = new PortalStateMachine(
            portalId,
            level.dimension(),
            frameData.getBottomLeft()
        );
        portal.activate();
        PortalRegistry.getInstance().registerPortal(portal);
```

with:

```java
        // Register portal in registry
        UUID portalId = UUID.randomUUID();
        PortalStateMachine portal = new PortalStateMachine(
            portalId,
            level.dimension(),
            frameData.getBottomLeft()
        );
        portal.activate();
        PortalRegistry.getInstance().registerPortal(portal);
        PortalOpenedEvents.fire(portal.getPortalId(), level, frameData.getBottomLeft(), PortalOpenCause.IGNITION, player);
```

- [ ] **Step 4: Edit the 9 Group B files (`1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11`), identical edit for each**

Edit the import block. Replace:

```java
import com.chronodawn.ChronoDawn;
import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
```

with:

```java
import com.chronodawn.ChronoDawn;
import com.chronodawn.api.event.PortalOpenCause;
import com.chronodawn.api.event.PortalOpenedEvents;
import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
```

Edit the ignition block inside the `server.execute()` lambda in `useOn()`. Replace:

```java
            portal.activate();
            PortalRegistry.getInstance().registerPortal(portal);
        });
```

with:

```java
            portal.activate();
            PortalRegistry.getInstance().registerPortal(portal);
            PortalOpenedEvents.fire(portal.getPortalId(), level, finalFrameData.getBottomLeft(), PortalOpenCause.IGNITION, player);
        });
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.api.event.PortalOpenedEventsTest"`
Expected: PASS, all tests green including `everyVersionDispatchesIgnitionExactlyOnce`.

- [ ] **Step 6: Compile every version to catch per-version API drift**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.20.1:compileJava :common-1.21.1:compileJava :common-1.21.2:compileJava :common-1.21.4:compileJava :common-1.21.5:compileJava :common-1.21.6:compileJava :common-1.21.7:compileJava :common-1.21.8:compileJava :common-1.21.9:compileJava :common-1.21.10:compileJava :common-1.21.11:compileJava`
Expected: BUILD SUCCESSFUL for every module.

- [ ] **Step 7: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.1/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.2/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.4/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.5/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.6/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.7/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.8/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.9/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.10/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/1.21.11/src/main/java/com/chronodawn/items/TimeHourglassItem.java \
        common/shared/src/test/java/com/chronodawn/api/event/PortalOpenedEventsTest.java
git commit -m "feat(events): dispatch portal-opened IGNITION from Time Hourglass ignition"
```

---

### Task 3: Wire `PortalTeleportHandler` dispatch across all 11 versions

**Files:**
- Modify: `common/1.20.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.2/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.4/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.5/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.6/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.7/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.8/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.9/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`
- Modify: `common/shared/src/test/java/com/chronodawn/api/event/PortalOpenedEventsTest.java`

**Interfaces:**
- Consumes: `PortalOpenedEvents.fire(UUID, Level, BlockPos, PortalOpenCause, Player)` and
  `PortalOpenCause.REIGNITION` from Task 1.
- Produces: nothing new; this task only adds dispatch call sites.

The relevant methods (`teleportThroughPortal`, `findReusablePortalFrame`,
`generatePortal`, `generatePortalStructure`) are byte-for-byte identical
across all 11 version files in this region, so the same edit applies to
every one of them.

- [ ] **Step 1: Extend the test with a source guard for `PortalTeleportHandler` (still failing)**

Add this field to `PortalOpenedEventsTest`, next to `IGNITION_DISPATCH`:

```java
    private static final String REIGNITION_DISPATCH =
        "PortalOpenedEvents.fire(portal.getPortalId(), level, pos, PortalOpenCause.REIGNITION";
```

Add this test method, next to `everyVersionDispatchesIgnitionExactlyOnce`:

```java
    @Test
    void everyVersionDispatchesReignitionExactlyOnce() throws IOException {
        Path projectRoot = Path.of(TestUtils.getProjectRoot());

        for (String version : VERSION_DIRS) {
            Path file = projectRoot.resolve("common").resolve(version)
                .resolve("src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java");
            String source = Files.readString(file, StandardCharsets.UTF_8);

            assertEquals(1, countOccurrences(source, REIGNITION_DISPATCH),
                version + " PortalTeleportHandler.java must dispatch the reignition portal-opened event exactly once");
        }
    }
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.api.event.PortalOpenedEventsTest"`
Expected: FAIL on `everyVersionDispatchesReignitionExactlyOnce` — `expected: <1> but was: <0>` for `1.20.1`.

- [ ] **Step 3: Edit all 11 `PortalTeleportHandler.java` files, identical edit for each**

Edit the import block. Replace:

```java
import com.chronodawn.ChronoDawn;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.items.TimeHourglassItem;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
```

with:

```java
import com.chronodawn.ChronoDawn;
import com.chronodawn.api.event.PortalOpenCause;
import com.chronodawn.api.event.PortalOpenedEvents;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.items.TimeHourglassItem;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
```

Add the `@Nullable` import. Replace:

```java
import net.minecraft.world.phys.Vec3;

import java.util.*;
```

with:

```java
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
```

Thread the igniter through the call sites in `teleportThroughPortal()`. Replace:

```java
            Optional<BlockPos> reusableFrame = findReusablePortalFrame(destLevel, destCoords, sourceAxis);
            BlockPos framePos = reusableFrame.orElse(null);
            if (framePos == null) {
                framePos = generatePortal(destLevel, destCoords, sourceAxis);
            }
```

with:

```java
            Optional<BlockPos> reusableFrame = findReusablePortalFrame(destLevel, destCoords, sourceAxis, player);
            BlockPos framePos = reusableFrame.orElse(null);
            if (framePos == null) {
                framePos = generatePortal(destLevel, destCoords, sourceAxis, player);
            }
```

Update `findReusablePortalFrame`'s signature and its call into `generatePortalStructure`. Replace:

```java
    private static Optional<BlockPos> findReusablePortalFrame(ServerLevel level, BlockPos coords, Direction.Axis axis) {
```

with:

```java
    private static Optional<BlockPos> findReusablePortalFrame(
            ServerLevel level, BlockPos coords, Direction.Axis axis, @Nullable ServerPlayer igniter) {
```

Replace:

```java
        if (nearestFrame != null) {
            generatePortalStructure(level, nearestFrame, axis);
            return Optional.of(nearestFrame);
        }
```

with:

```java
        if (nearestFrame != null) {
            generatePortalStructure(level, nearestFrame, axis, igniter);
            return Optional.of(nearestFrame);
        }
```

Update `generatePortal`'s signature and its call into `generatePortalStructure`. Replace:

```java
    private static BlockPos generatePortal(ServerLevel level, BlockPos coords, Direction.Axis axis) {
        // Find ground level starting from coords.y
        BlockPos groundPos = findGroundLevel(level, coords);

        // Generate a 4x5 portal at ground level with specified axis
        generatePortalStructure(level, groundPos, axis);
        return groundPos;
    }
```

with:

```java
    private static BlockPos generatePortal(ServerLevel level, BlockPos coords, Direction.Axis axis, @Nullable ServerPlayer igniter) {
        // Find ground level starting from coords.y
        BlockPos groundPos = findGroundLevel(level, coords);

        // Generate a 4x5 portal at ground level with specified axis
        generatePortalStructure(level, groundPos, axis, igniter);
        return groundPos;
    }
```

Update `generatePortalStructure`'s signature. Replace:

```java
    private static void generatePortalStructure(ServerLevel level, BlockPos pos, Direction.Axis axis) {
```

with:

```java
    private static void generatePortalStructure(ServerLevel level, BlockPos pos, Direction.Axis axis, @Nullable ServerPlayer igniter) {
```

Fire the event only when this call actually transitions the portal. Replace:

```java
        // Register or reignite portal in registry
        PortalStateMachine portal = PortalRegistry.getInstance().getPortalAt(pos);
        if (portal == null) {
            UUID portalId = UUID.randomUUID();
            portal = new PortalStateMachine(
                portalId,
                level.dimension(),
                pos
            );
            PortalRegistry.getInstance().registerPortal(portal);
            portal.activate();
        } else if (portal.getCurrentState() == PortalState.INACTIVE) {
            portal.activate();
        } else if (portal.getCurrentState() == PortalState.DEACTIVATED) {
            // Reigniting the same physical frame after an unstable entry is intentional:
            // the first ChronoDawn arrival removes portal blocks but keeps the frame and registry entry.
            // Treat this as restoring that existing portal, not as a normal player-triggered state transition.
            portal.setState(PortalState.ACTIVATED);
            PortalRegistry.getInstance().markDirtyForPortal(portal.getPortalId());
        }
    }
```

with:

```java
        // Register or reignite portal in registry
        PortalStateMachine portal = PortalRegistry.getInstance().getPortalAt(pos);
        boolean justOpened = false;
        if (portal == null) {
            UUID portalId = UUID.randomUUID();
            portal = new PortalStateMachine(
                portalId,
                level.dimension(),
                pos
            );
            PortalRegistry.getInstance().registerPortal(portal);
            portal.activate();
            justOpened = true;
        } else if (portal.getCurrentState() == PortalState.INACTIVE) {
            portal.activate();
            justOpened = true;
        } else if (portal.getCurrentState() == PortalState.DEACTIVATED) {
            // Reigniting the same physical frame after an unstable entry is intentional:
            // the first ChronoDawn arrival removes portal blocks but keeps the frame and registry entry.
            // Treat this as restoring that existing portal, not as a normal player-triggered state transition.
            portal.setState(PortalState.ACTIVATED);
            PortalRegistry.getInstance().markDirtyForPortal(portal.getPortalId());
            justOpened = true;
        }

        if (justOpened) {
            PortalOpenedEvents.fire(portal.getPortalId(), level, pos, PortalOpenCause.REIGNITION, igniter);
        }
    }
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.api.event.PortalOpenedEventsTest"`
Expected: PASS, all tests green including `everyVersionDispatchesReignitionExactlyOnce`.

- [ ] **Step 5: Compile every version to catch per-version API drift**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.20.1:compileJava :common-1.21.1:compileJava :common-1.21.2:compileJava :common-1.21.4:compileJava :common-1.21.5:compileJava :common-1.21.6:compileJava :common-1.21.7:compileJava :common-1.21.8:compileJava :common-1.21.9:compileJava :common-1.21.10:compileJava :common-1.21.11:compileJava`
Expected: BUILD SUCCESSFUL for every module.

- [ ] **Step 6: Mutation-check the source guards**

Temporarily delete one `PortalOpenedEvents.fire(portal.getPortalId(), level, pos, PortalOpenCause.REIGNITION` line (e.g. in `common/1.21.11/.../PortalTeleportHandler.java`) and re-run the test from Step 4; confirm `everyVersionDispatchesReignitionExactlyOnce` fails for `1.21.11`. Restore the deleted line afterward and re-run to confirm green again. Repeat once for the `IGNITION_DISPATCH` guard from Task 2 by deleting its call in the same file.

- [ ] **Step 7: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.2/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.4/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.5/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.6/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.7/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.8/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.9/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.10/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/1.21.11/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java \
        common/shared/src/test/java/com/chronodawn/api/event/PortalOpenedEventsTest.java
git commit -m "feat(events): dispatch portal-opened REIGNITION from teleport-time portal generation"
```

---

### Task 4: Documentation — modpack integration guide, changelog, roadmap

**Files:**
- Modify: `docs/modpack-integration.md`
- Modify: `CHANGELOG.md`
- Modify: `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`

**Interfaces:**
- Consumes: the shipped `PortalOpenedEvents` API from Tasks 1–3 (accessor
  and type names must match exactly).
- Produces: nothing consumed by later tasks.

- [ ] **Step 1: Add the portal-opened event section to `docs/modpack-integration.md`**

Insert a new `## Portal opened event API` section immediately after the
existing `## Boss defeated event API` section and before `## Future
integrations *(not yet shipped)*`. Replace:

```markdown
The Java event is the foundation for scripting integrations. Direct KubeJS,
CraftTweaker and FTB Quests bindings are not shipped yet; a pack cannot register
this event from those scripting systems without an addon bridge.

---

## Future integrations *(not yet shipped)*

### Additional scripting events and bindings

Portal-opened and Chronicle-entry-unlocked events, plus direct KubeJS,
FTB Quests and CraftTweaker bindings, are planned as independent follow-up
slices built on the stability rules established by the boss event.
```

with:

```markdown
The Java event is the foundation for scripting integrations. Direct KubeJS,
CraftTweaker and FTB Quests bindings are not shipped yet; a pack cannot register
this event from those scripting systems without an addon bridge.

---

## Portal opened event API

Java addons can subscribe to a loader-neutral event that fires whenever a
Chrono Dawn portal transitions into the active state, whether a player
ignites a fresh frame with a Time Hourglass or the mod generates or reuses a
return portal while teleporting someone through an already-active one.

```java
import com.chronodawn.api.event.PortalOpenedEvents;
import com.chronodawn.api.event.PortalOpenedListener;

public final class MyChronoDawnIntegration {
    private static final PortalOpenedListener PORTAL_LISTENER = context -> {
        if (context.cause() == com.chronodawn.api.event.PortalOpenCause.IGNITION
                && context.igniter() != null) {
            // Advance your quest or pack progression for this player.
        }
    };

    public static void register() {
        PortalOpenedEvents.register(PORTAL_LISTENER);
    }

    public static void unregister() {
        PortalOpenedEvents.unregister(PORTAL_LISTENER);
    }
}
```

Keep the listener instance if your addon supports reload or shutdown; passing a
new lambda to `unregister` does not remove the original registration.

The `PortalOpenedContext` accessors are:

| Accessor | Value |
| --- | --- |
| `portalId()` | Stable `UUID` of the physical portal in the internal registry. |
| `level()` | Server level containing the portal frame. |
| `position()` | Frame's bottom-left block position. |
| `cause()` | `PortalOpenCause.IGNITION` (Time Hourglass) or `PortalOpenCause.REIGNITION` (teleport-time generation or reuse). |
| `igniter()` | Credited `ServerPlayer`, or `null` when none is known. |

Callbacks run synchronously on the logical server after the portal's blocks
are placed and its state has already transitioned to active; a listener
never observes a `STABILIZED` portal's routine block regeneration, only an
actual activation. Listeners run in registration order. If one listener
throws a runtime exception, Chrono Dawn logs it and continues with the
remaining listeners without interrupting portal activation.

The Java event is the foundation for scripting integrations. Direct KubeJS,
CraftTweaker and FTB Quests bindings are not shipped yet; a pack cannot register
this event from those scripting systems without an addon bridge.

---

## Future integrations *(not yet shipped)*

### Additional scripting events and bindings

The Chronicle-entry-unlocked event, plus direct KubeJS, FTB Quests and
CraftTweaker bindings for the boss-defeated and portal-opened events, are
planned as independent follow-up slices built on the stability rules
established by those two events.
```

- [ ] **Step 2: Add a `CHANGELOG.md` entry**

Insert a new bullet under `## [Unreleased]` / `### Added`, immediately after
the existing boss-defeated bullet. Replace:

```markdown
- Added a loader-neutral Java boss-defeated event API for all six Chrono Dawn
  bosses. Addons can register through `BossDefeatedEvents` and receive a stable
  boss ID plus the defeated entity, server level, position, damage source and
  attributed player after built-in defeat consequences complete. Listener
  failures are isolated so they cannot interrupt boss death or later listeners.
  See [docs/modpack-integration.md](docs/modpack-integration.md#boss-defeated-event-api).
```

with:

```markdown
- Added a loader-neutral Java boss-defeated event API for all six Chrono Dawn
  bosses. Addons can register through `BossDefeatedEvents` and receive a stable
  boss ID plus the defeated entity, server level, position, damage source and
  attributed player after built-in defeat consequences complete. Listener
  failures are isolated so they cannot interrupt boss death or later listeners.
  See [docs/modpack-integration.md](docs/modpack-integration.md#boss-defeated-event-api).
- Added a loader-neutral Java portal-opened event API. Addons can register
  through `PortalOpenedEvents` and receive the portal's stable ID, server
  level, frame position, activation cause (Time Hourglass ignition or
  teleport-time reignition) and credited player after the portal's blocks
  are placed. Listener failures are isolated so they cannot interrupt portal
  activation or later listeners.
  See [docs/modpack-integration.md](docs/modpack-integration.md#portal-opened-event-api).
```

- [ ] **Step 3: Update the modpack-author readiness roadmap**

Replace:

```markdown
### C. Scripting events

**Status**: 🚧 Direct scripting bindings and additional progression events
remain.

KubeJS / FTB Quests / CraftTweaker bridges, portal-opened events and
Chronicle-entry-unlocked events are independent follow-up slices.
```

with:

```markdown
### C. Scripting events

**Status**: 🚧 Direct scripting bindings and the Chronicle-entry-unlocked
event remain.

Boss-defeated and portal-opened events are shipped. KubeJS / FTB Quests /
CraftTweaker bridges and a Chronicle-entry-unlocked event are independent
follow-up slices.
```

Replace the status tracker row for sub-project C:

```markdown
| C. Scripting events | 🚧 Direct bindings and additional progression events remain | [2026-08-29-boss-defeated-event-design.md](./2026-08-29-boss-defeated-event-design.md) |
```

with:

```markdown
| C. Scripting events | 🚧 Direct bindings and the Chronicle-entry-unlocked event remain | [2026-08-29-boss-defeated-event-design.md](./2026-08-29-boss-defeated-event-design.md), [2026-09-04-portal-opened-event-design.md](./2026-09-04-portal-opened-event-design.md) |
```

- [ ] **Step 4: Commit**

```bash
git add docs/modpack-integration.md CHANGELOG.md \
        docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md
git commit -m "docs(events): document the portal-opened event API"
```

---

### Task 5: Full multi-version verification and manual test handoff

**Files:**
- Modify: `.claude/tasks.local.md`

**Interfaces:**
- Consumes: the complete shipped feature from Tasks 1–4.
- Produces: nothing; this is the final verification task.

- [ ] **Step 1: Run the full verification suite**

Run: `mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew checkAll`
Expected: BUILD SUCCESSFUL. This runs `cleanAll`, `validateResources`,
`validateTranslations`, `buildAll`, `testAll`, and `gameTestAll` across all
11 supported versions and both loaders. If `buildAll` or `gameTestAll`
report a spurious `FAILED` from the wrapper task itself (see
`feedback_buildall_gametestall_wrapper_unreliable` in project memory), rerun
that specific stage standalone (e.g. `./gradlew buildAll` on its own) before
concluding it is a real failure.

- [ ] **Step 2: Update `.claude/tasks.local.md`**

Mark the portal-opened event line done and record the design/plan doc
paths, mirroring how the tools/armor/shield slice was recorded. Replace:

```markdown
- [ ] Design and implement a portal-opened event.
```

with:

```markdown
- [x] Design and implement a portal-opened event. Shipped 2026-09-04. Fires
  once whenever a `PortalStateMachine` transitions into `ACTIVATED`, whether
  by Time Hourglass ignition or teleport-time reignition/reuse. See
  `docs/superpowers/specs/2026-09-04-portal-opened-event-design.md` and
  `docs/superpowers/plans/2026-09-04-portal-opened-event.md`.
```

Add a new entry under "## User manual verification remaining":

```markdown
- [ ] On Fabric 1.21.11, register a test `PortalOpenedListener`, ignite a
  fresh frame with a Time Hourglass, and confirm one `IGNITION` callback
  with the expected position and player.
- [ ] On the same world, travel through that portal so a return portal is
  generated or reactivated on the other side, and confirm one `REIGNITION`
  callback.
- [ ] Repeat both checks on NeoForge 1.21.11.
```

- [ ] **Step 3: Commit**

```bash
git add .claude/tasks.local.md
git commit -m "chore: record portal-opened event slice completion"
```

Note: `.claude/tasks.local.md` is gitignored (`*.local.md`), so this commit
will report nothing staged — skip it if `git status` confirms the file is
ignored, and instead leave the edit as local-only tracking, consistent with
its stated purpose as a local scratchpad.
