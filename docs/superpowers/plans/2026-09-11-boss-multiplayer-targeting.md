# Multiplayer Boss Targeting and Aggro Distribution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Chrono Dawn's six bosses respond to a hit from any player immediately, and — for bosses whose kit allows it — spread damage across a multiplayer group instead of fighting like a single-target encounter.

**Architecture:** A new Minecraft-API-free helper (`BossMultiplayer`) centralizes the "2+ participants" check. Each boss entity gets a `setLastHurtByMob` override (always active) plus, where applicable, a multiplayer-only shorter cooldown on its existing area attack or (Time Tyrant only) a phase-transition mob summon reusing an existing weak entity. All boss entity edits are duplicated across the thirteen version-specific `common/<version>` modules because the boss classes themselves are not shared code — this mirrors how the 2026-07-25 boss-multiplier work duplicated `createAttributes()` wiring across the same modules.

**Tech Stack:** Java 21/25, NeoForge + Fabric via Architectury, JUnit 5, Gradle multi-version build (`-Ptarget_mc_version=<version>`).

**Spec:** [docs/superpowers/specs/2026-09-11-boss-multiplayer-targeting-design.md](../specs/2026-09-11-boss-multiplayer-targeting-design.md)

## Global Constraints

- Twelve version modules besides `1.21.3` (which shares `1.21.2`'s common module) must all receive every applicable edit: `1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2` — thirteen `common/<version>` modules in total.
- No new Minecraft entity types. Time Tyrant's summon reuses the existing `ChronalLeechEntity`.
- No new config surface (`chronodawn.toml` is untouched by this plan). All new constants are internal balance values.
- Every multiplayer-only behavior change must be a strict no-op in singleplayer (1 participant). Verify this by inspection at each task, not just at the end.
- `BossMultiplayer.isMultiplayerEncounter(int)` must not reference any `net.minecraft.*` type — it is pure `common/shared` logic, compiled into every version module without a `compat/` layer.
- `./gradlew checkAll` must pass before this work is considered done.

---

## Task 1: `BossMultiplayer` shared helper

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/entities/bosses/BossMultiplayer.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/BossMultiplayerTest.java`

**Interfaces:**
- Produces: `BossMultiplayer.isMultiplayerEncounter(int participantCount)` → `boolean`, `true` iff `participantCount >= 2`. Every later task calls this exactly as `BossMultiplayer.isMultiplayerEncounter(this.bossEvent.getPlayers().size())`.

- [ ] **Step 1: Write the failing test**

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
package com.chronodawn.unit;

import com.chronodawn.entities.bosses.BossMultiplayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossMultiplayerTest {

    @Test
    void zeroOrOneParticipant_isNotMultiplayer() {
        assertFalse(BossMultiplayer.isMultiplayerEncounter(0));
        assertFalse(BossMultiplayer.isMultiplayerEncounter(1));
    }

    @Test
    void twoOrMoreParticipants_isMultiplayer() {
        assertTrue(BossMultiplayer.isMultiplayerEncounter(2));
        assertTrue(BossMultiplayer.isMultiplayerEncounter(3));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :common-1.21.5:test -Ptarget_mc_version=1.21.5 --tests "com.chronodawn.unit.BossMultiplayerTest"`
Expected: FAIL — compile error, `BossMultiplayer` does not exist.

- [ ] **Step 3: Write minimal implementation**

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
package com.chronodawn.entities.bosses;

/**
 * Whether a boss fight currently has enough participants to warrant the
 * multiplayer-only aggro-distribution behavior (shorter AoE cooldowns,
 * Time Tyrant's reinforcement summon). Takes a plain participant count
 * rather than {@code ServerBossEvent} so this class stays free of any
 * Minecraft type, matching {@link BossScaling}.
 */
public final class BossMultiplayer {

    private BossMultiplayer() {
    }

    public static boolean isMultiplayerEncounter(int participantCount) {
        return participantCount >= 2;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :common-1.21.5:test -Ptarget_mc_version=1.21.5 --tests "com.chronodawn.unit.BossMultiplayerTest"`
Expected: PASS (2 tests)

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/entities/bosses/BossMultiplayer.java common/shared/src/test/java/com/chronodawn/unit/BossMultiplayerTest.java
git commit -m "feat(bosses): add BossMultiplayer participant-count helper"
```

---

## Task 2: Immediate target switch — Time Guardian

**Files (identical edit in all thirteen):**
- Modify: `common/1.20.1/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.2/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.4/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.5/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.6/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.7/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.8/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.9/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/26.1.2/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
- Modify: `common/26.2/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`

**Interfaces:**
- Consumes: nothing from Task 1 (this override does not call `BossMultiplayer`).
- Produces: `TimeGuardianEntity` switches target to whichever player last hit it.

- [ ] **Step 1: Add the override to the 1.21.5 copy**

In `common/1.21.5/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`, find the end of `registerGoals()`:

```java
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
```

Insert immediately after that closing brace:

```java

    /**
     * Switch target to whoever just hit us, even if we already had a
     * different target. Vanilla's HurtByTargetGoal only takes effect while
     * the boss has no target, so without this override the boss keeps
     * chasing the first player it saw while everyone else hits it for free.
     */
    @Override
    public void setLastHurtByMob(LivingEntity entity) {
        super.setLastHurtByMob(entity);
        if (entity instanceof Player) {
            this.setTarget(entity);
        }
    }
```

`LivingEntity` and `Player` are already imported in this file (used by `NearestAttackableTargetGoal<>(this, Player.class, true)` and `HurtByTargetGoal`), so no new imports are needed.

- [ ] **Step 2: Compile-check the 1.21.5 copy**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Apply the identical insertion to the remaining twelve files**

Apply the exact same block from Step 1 (same anchor text: end of `registerGoals()`) to each of the other twelve `TimeGuardianEntity.java` files listed above.

- [ ] **Step 4: Compile-check the oldest and newest modules**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Expected: BUILD SUCCESSFUL

Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java
git commit -m "feat(bosses): Time Guardian switches target immediately when hit by a player"
```

---

## Task 3: Immediate target switch — Chronos Warden

**Files (identical edit in all thirteen):** same thirteen `common/<version>/.../ChronosWardenEntity.java` paths as Task 2, substituting `ChronosWardenEntity` for `TimeGuardianEntity`.

**Interfaces:**
- Consumes: nothing.
- Produces: `ChronosWardenEntity` switches target to whoever last hit it.

- [ ] **Step 1: Add the override to the 1.21.5 copy**

Same anchor and insertion as Task 2 Step 1 (registerGoals() ends with the identical `NearestAttackableTargetGoal` line in every boss class):

```java
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Switch target to whoever just hit us, even if we already had a
     * different target. Vanilla's HurtByTargetGoal only takes effect while
     * the boss has no target, so without this override the boss keeps
     * chasing the first player it saw while everyone else hits it for free.
     */
    @Override
    public void setLastHurtByMob(LivingEntity entity) {
        super.setLastHurtByMob(entity);
        if (entity instanceof Player) {
            this.setTarget(entity);
        }
    }
```

- [ ] **Step 2: Compile-check the 1.21.5 copy**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Apply the identical insertion to the remaining twelve `ChronosWardenEntity.java` files**

- [ ] **Step 4: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/ChronosWardenEntity.java
git commit -m "feat(bosses): Chronos Warden switches target immediately when hit by a player"
```

---

## Task 4: Immediate target switch — Clockwork Colossus

**Files:** same thirteen paths, `ClockworkColossusEntity.java`.

- [ ] **Step 1: Add the override to the 1.21.5 copy** (identical block to Task 2 Step 1, inserted at the same `registerGoals()` anchor)

- [ ] **Step 2: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Apply the identical insertion to the remaining twelve `ClockworkColossusEntity.java` files**

- [ ] **Step 4: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/ClockworkColossusEntity.java
git commit -m "feat(bosses): Clockwork Colossus switches target immediately when hit by a player"
```

---

## Task 5: Immediate target switch — Entropy Keeper

**Files:** same thirteen paths, `EntropyKeeperEntity.java`.

- [ ] **Step 1: Add the override to the 1.21.5 copy** (identical block to Task 2 Step 1)

- [ ] **Step 2: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Apply the identical insertion to the remaining twelve `EntropyKeeperEntity.java` files**

- [ ] **Step 4: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/EntropyKeeperEntity.java
git commit -m "feat(bosses): Entropy Keeper switches target immediately when hit by a player"
```

---

## Task 6: Immediate target switch — Temporal Phantom

**Files:** same thirteen paths, `TemporalPhantomEntity.java`.

- [ ] **Step 1: Add the override to the 1.21.5 copy** (identical block to Task 2 Step 1)

- [ ] **Step 2: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Apply the identical insertion to the remaining twelve `TemporalPhantomEntity.java` files**

- [ ] **Step 4: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/TemporalPhantomEntity.java
git commit -m "feat(bosses): Temporal Phantom switches target immediately when hit by a player"
```

---

## Task 7: Immediate target switch — Time Tyrant

**Files:** same thirteen paths, `TimeTyrantEntity.java`.

- [ ] **Step 1: Add the override to the 1.21.5 copy** (identical block to Task 2 Step 1)

- [ ] **Step 2: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Apply the identical insertion to the remaining twelve `TimeTyrantEntity.java` files**

- [ ] **Step 4: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/TimeTyrantEntity.java
git commit -m "feat(bosses): Time Tyrant switches target immediately when hit by a player"
```

---

## Task 8: Multiplayer AoE cooldown — Time Guardian

**Files:** same thirteen `TimeGuardianEntity.java` paths as Task 2.

**Interfaces:**
- Consumes: `BossMultiplayer.isMultiplayerEncounter(int)` from Task 1.
- Produces: Time Guardian's Phase 2 AoE fires on a 56-tick cooldown instead of 80 when 2+ players participate; unchanged in singleplayer.

- [ ] **Step 1: Edit the 1.21.5 copy — add the multiplayer constant**

Find:

```java
    // AoE timing (Phase 2) - public for testing
    public static final int AOE_COOLDOWN_TICKS = 80; // 4 seconds
    public static final double AOE_RANGE = 4.0; // Reduced from 5.0 for balance
```

Replace with:

```java
    // AoE timing (Phase 2) - public for testing
    public static final int AOE_COOLDOWN_TICKS = 80; // 4 seconds
    public static final int AOE_COOLDOWN_TICKS_MULTIPLAYER = 56; // 2.8 seconds, 2+ players
    public static final double AOE_RANGE = 4.0; // Reduced from 5.0 for balance
```

- [ ] **Step 2: Edit the 1.21.5 copy — use the multiplayer cooldown when arming**

Find:

```java
        // AoE ability (only when not in post-teleport delay)
        if (aoeCooldown <= 0 && this.getTarget() != null && postTeleportDelay <= 0) {
            performAoEAttack();
            aoeCooldown = AOE_COOLDOWN_TICKS;
        }
```

Replace with:

```java
        // AoE ability (only when not in post-teleport delay)
        if (aoeCooldown <= 0 && this.getTarget() != null && postTeleportDelay <= 0) {
            performAoEAttack();
            aoeCooldown = BossMultiplayer.isMultiplayerEncounter(this.bossEvent.getPlayers().size())
                ? AOE_COOLDOWN_TICKS_MULTIPLAYER
                : AOE_COOLDOWN_TICKS;
        }
```

`BossMultiplayer` is in the same package (`com.chronodawn.entities.bosses`) as `TimeGuardianEntity`, so no import is needed. `bossEvent` is already a private field on this class.

- [ ] **Step 3: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Apply the identical two edits to the remaining twelve `TimeGuardianEntity.java` files**

- [ ] **Step 5: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 6: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java
git commit -m "feat(bosses): Time Guardian AoE cooldown shortens with 2+ players"
```

---

## Task 9: Multiplayer Ground Slam cooldown — Chronos Warden

**Files:** same thirteen `ChronosWardenEntity.java` paths as Task 3.

**Interfaces:**
- Consumes: `BossMultiplayer.isMultiplayerEncounter(int)` from Task 1.
- Produces: Ground Slam re-arms on 140/100 ticks (Phase 1/2) instead of 200/140 when 2+ players participate; unchanged in singleplayer.

- [ ] **Step 1: Edit the 1.21.5 copy — add the multiplayer constants**

Find:

```java
    // Ground Slam timing
    private static final int GROUND_SLAM_COOLDOWN_PHASE1 = 200; // 10 seconds
    private static final int GROUND_SLAM_COOLDOWN_PHASE2 = 140; // 7 seconds
    private static final double GROUND_SLAM_RANGE = 4.0;
```

Replace with:

```java
    // Ground Slam timing
    private static final int GROUND_SLAM_COOLDOWN_PHASE1 = 200; // 10 seconds
    private static final int GROUND_SLAM_COOLDOWN_PHASE2 = 140; // 7 seconds
    private static final int GROUND_SLAM_COOLDOWN_PHASE1_MULTIPLAYER = 140; // 7 seconds, 2+ players
    private static final int GROUND_SLAM_COOLDOWN_PHASE2_MULTIPLAYER = 100; // 5 seconds, 2+ players
    private static final double GROUND_SLAM_RANGE = 4.0;
```

- [ ] **Step 2: Edit the 1.21.5 copy — use the multiplayer cooldown when arming**

Find:

```java
        // Set cooldown
        int cooldown = getPhase() == PHASE_1 ? GROUND_SLAM_COOLDOWN_PHASE1 : GROUND_SLAM_COOLDOWN_PHASE2;
        this.groundSlamCooldown = cooldown;
```

Replace with:

```java
        // Set cooldown
        boolean multiplayer = BossMultiplayer.isMultiplayerEncounter(this.bossEvent.getPlayers().size());
        int cooldown;
        if (getPhase() == PHASE_1) {
            cooldown = multiplayer ? GROUND_SLAM_COOLDOWN_PHASE1_MULTIPLAYER : GROUND_SLAM_COOLDOWN_PHASE1;
        } else {
            cooldown = multiplayer ? GROUND_SLAM_COOLDOWN_PHASE2_MULTIPLAYER : GROUND_SLAM_COOLDOWN_PHASE2;
        }
        this.groundSlamCooldown = cooldown;
```

- [ ] **Step 3: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Apply the identical two edits to the remaining twelve `ChronosWardenEntity.java` files**

- [ ] **Step 5: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 6: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/ChronosWardenEntity.java
git commit -m "feat(bosses): Chronos Warden Ground Slam cooldown shortens with 2+ players"
```

---

## Task 10: Multiplayer AoE cooldown — Time Tyrant

**Files:** same thirteen `TimeTyrantEntity.java` paths as Task 7.

**Interfaces:**
- Consumes: `BossMultiplayer.isMultiplayerEncounter(int)` from Task 1.
- Produces: Time Tyrant's Phase 3 AoE fires on an 84-tick cooldown instead of 120 when 2+ players participate; unchanged in singleplayer.

- [ ] **Step 1: Edit the 1.21.5 copy — add the multiplayer constant**

Find:

```java
    public static final int AOE_COOLDOWN_TICKS = 120; // 6 seconds
```

Replace with:

```java
    public static final int AOE_COOLDOWN_TICKS = 120; // 6 seconds
    public static final int AOE_COOLDOWN_TICKS_MULTIPLAYER = 84; // 4.2 seconds, 2+ players
```

- [ ] **Step 2: Edit the 1.21.5 copy — use the multiplayer cooldown when arming**

Find (inside `handleAoEAbility()`):

```java
            aoeCooldown = AOE_COOLDOWN_TICKS;
        }
    }
```

Replace with:

```java
            aoeCooldown = BossMultiplayer.isMultiplayerEncounter(this.bossEvent.getPlayers().size())
                ? AOE_COOLDOWN_TICKS_MULTIPLAYER
                : AOE_COOLDOWN_TICKS;
        }
    }
```

This is the only `aoeCooldown = AOE_COOLDOWN_TICKS;` assignment followed by two closing braces in the file — `handleAoEAbility()` is the sole writer of `aoeCooldown`.

- [ ] **Step 3: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Apply the identical two edits to the remaining twelve `TimeTyrantEntity.java` files**

- [ ] **Step 5: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 6: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/TimeTyrantEntity.java
git commit -m "feat(bosses): Time Tyrant AoE cooldown shortens with 2+ players"
```

---

## Task 11: Repeatable Entropy Burst — Entropy Keeper

**Files:** same thirteen `EntropyKeeperEntity.java` paths as Task 5.

**Interfaces:**
- Consumes: `BossMultiplayer.isMultiplayerEncounter(int)` from Task 1.
- Produces: with 2+ players, Entropy Burst re-fires every 1200 ticks (60s) after its first trigger at 30% HP instead of firing once; unchanged (fires once) in singleplayer.

- [ ] **Step 1: Edit the 1.21.5 copy — add the cooldown field and constant**

Find:

```java
    private final ServerBossEvent bossEvent;
    private int decayAuraTicks = 0;
    private int temporalRotCooldown = 0;
    private int degradationTimer = 0;
    private boolean entropyBurstTriggered = false;
```

Replace with:

```java
    private final ServerBossEvent bossEvent;
    private int decayAuraTicks = 0;
    private int temporalRotCooldown = 0;
    private int degradationTimer = 0;
    private boolean entropyBurstTriggered = false;
    private int entropyBurstCooldown = 0;
```

Find:

```java
    private static final float PHASE_2_THRESHOLD = 0.5f;
    private static final float ENTROPY_BURST_THRESHOLD = 0.3f;
```

Replace with:

```java
    private static final float PHASE_2_THRESHOLD = 0.5f;
    private static final float ENTROPY_BURST_THRESHOLD = 0.3f;
    private static final int ENTROPY_BURST_RETRIGGER_TICKS = 1200; // 60 seconds, 2+ players only
```

- [ ] **Step 2: Edit the 1.21.5 copy — make the burst repeatable in multiplayer**

Find (inside `tick()`, within the `if (getCurrentPhase() == PHASE_2)` block):

```java
                // Entropy Burst: ONE-TIME at 30% HP
                float healthRatio = this.getHealth() / this.getMaxHealth();
                if (!entropyBurstTriggered && healthRatio <= ENTROPY_BURST_THRESHOLD) {
                    performEntropyBurst();
                    entropyBurstTriggered = true;
                }
```

Replace with:

```java
                // Entropy Burst: at 30% HP. Singleplayer: fires once. Multiplayer
                // (2+ players): re-fires every 60s after the first trigger.
                float healthRatio = this.getHealth() / this.getMaxHealth();
                if (entropyBurstCooldown > 0) {
                    entropyBurstCooldown--;
                }
                boolean canRetrigger = entropyBurstTriggered
                    && entropyBurstCooldown <= 0
                    && BossMultiplayer.isMultiplayerEncounter(this.bossEvent.getPlayers().size());
                if ((!entropyBurstTriggered || canRetrigger) && healthRatio <= ENTROPY_BURST_THRESHOLD) {
                    performEntropyBurst();
                    entropyBurstTriggered = true;
                    entropyBurstCooldown = ENTROPY_BURST_RETRIGGER_TICKS;
                }
```

`entropyBurstCooldown` is intentionally not persisted in `readAdditionalSaveData`/`addAdditionalSaveData` — worst case after a server restart mid-fight it resets to 0, letting the burst retrigger slightly earlier than a full 60s after reload. That is a harmless, YAGNI-justified gap, not a correctness bug.

- [ ] **Step 3: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Apply the identical edits to the remaining twelve `EntropyKeeperEntity.java` files**

- [ ] **Step 5: Compile-check 1.20.1 and 26.2**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL for both

- [ ] **Step 6: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/EntropyKeeperEntity.java
git commit -m "feat(bosses): Entropy Burst repeats every 60s with 2+ players"
```

---

## Task 12: Time Tyrant Chronal Leech reinforcements

**Files:** same thirteen `TimeTyrantEntity.java` paths as Task 7 and Task 10.

**Interfaces:**
- Consumes: `BossMultiplayer.isMultiplayerEncounter(int)` from Task 1; `ModEntities.CHRONAL_LEECH` (`RegistrySupplier<EntityType<ChronalLeechEntity>>`, already registered in `com.chronodawn.registry.ModEntities`); `ChronalLeechEntity` (`com.chronodawn.entities.mobs.ChronalLeechEntity`, existing class — no new entity type).
- Produces: 3 `ChronalLeechEntity` spawn near Time Tyrant on each phase transition (Phase 1→2, Phase 2→3) when 2+ players participate; none in singleplayer.

This task has three code variants because the entity-creation API changed twice across the version matrix (confirmed against each version's own `TimeTyrantSpawner.java`, which already spawns an entity with the same API per version):

| Versions | `create()` call | Positioning | `finalizeSpawn()` arity |
| --- | --- | --- | --- |
| `1.20.1` | `create(level)` | `moveTo(x, y, z, yRot, xRot)` | 5 args: `(level, diff, MobSpawnType.TRIGGERED, null, null)` |
| `1.21.1` | `create(level)` | `moveTo(x, y, z, yRot, xRot)` | 4 args: `(level, diff, MobSpawnType.TRIGGERED, null)` |
| `1.21.2`, `1.21.4` | `create(level, EntitySpawnReason.TRIGGERED)` | `moveTo(x, y, z, yRot, xRot)` | 4 args: `(level, diff, EntitySpawnReason.TRIGGERED, null)` |
| `1.21.5`–`1.21.11`, `26.1.2`, `26.2` | `create(level, EntitySpawnReason.TRIGGERED)` | `setPos(x, y, z)` + `setYRot(yRot)` + `setXRot(0.0f)` | 4 args: `(level, diff, EntitySpawnReason.TRIGGERED, null)` |

- [ ] **Step 1: Edit the 1.21.5 copy — imports**

Find:

```java
import com.chronodawn.core.time.MobAICanceller;
import com.chronodawn.entities.bosses.ExtendedMeleeAttackGoal;
```

Replace with:

```java
import com.chronodawn.core.time.MobAICanceller;
import com.chronodawn.entities.bosses.ExtendedMeleeAttackGoal;
import com.chronodawn.entities.mobs.ChronalLeechEntity;
import com.chronodawn.registry.ModEntities;
```

- [ ] **Step 2: Edit the 1.21.5 copy — add the summon count constant**

Find:

```java
    public static final int AOE_COOLDOWN_TICKS = 120; // 6 seconds
    public static final int AOE_COOLDOWN_TICKS_MULTIPLAYER = 84; // 4.2 seconds, 2+ players
```

(This is the state after Task 10. If Task 10 has not run yet in your working copy, find the plain `AOE_COOLDOWN_TICKS = 120;` line instead and add the line below after it.)

Replace with:

```java
    public static final int AOE_COOLDOWN_TICKS = 120; // 6 seconds
    public static final int AOE_COOLDOWN_TICKS_MULTIPLAYER = 84; // 4.2 seconds, 2+ players
    private static final int CHRONAL_LEECH_SUMMON_COUNT = 3;
```

- [ ] **Step 3: Edit the 1.21.5 copy — call the summon from `onPhaseTransition`**

Find:

```java
    private void onPhaseTransition(int newPhase) {
        // Play sound effect
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.BOSS_POWER_UP.get(),
            SoundSource.HOSTILE,
            2.0f,
            1.0f
        );

        // Spawn particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                100,
                0.5,
                1.0,
                0.5,
                0.1
            );
        }
    }
```

Replace with:

```java
    private void onPhaseTransition(int newPhase) {
        // Play sound effect
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.BOSS_POWER_UP.get(),
            SoundSource.HOSTILE,
            2.0f,
            1.0f
        );

        // Spawn particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                100,
                0.5,
                1.0,
                0.5,
                0.1
            );

            // Reinforcements: only with 2+ players in the fight
            if (BossMultiplayer.isMultiplayerEncounter(this.bossEvent.getPlayers().size())) {
                summonChronalLeeches(serverLevel);
            }
        }
    }

    /**
     * Summon Chronal Leeches near this boss to split player attention.
     * Multiplayer-only; called from onPhaseTransition. Summoned leeches are
     * ordinary Monsters with no persistence override or explicit cleanup —
     * they follow standard despawn rules like any other Chrono Dawn mob.
     */
    private void summonChronalLeeches(ServerLevel serverLevel) {
        for (int i = 0; i < CHRONAL_LEECH_SUMMON_COUNT; i++) {
            ChronalLeechEntity leech = ModEntities.CHRONAL_LEECH.get().create(
                serverLevel,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED
            );
            if (leech == null) {
                continue;
            }

            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 2.0 + this.random.nextDouble() * 2.0;
            leech.setPos(
                this.getX() + Math.cos(angle) * distance,
                this.getY(),
                this.getZ() + Math.sin(angle) * distance
            );
            leech.setYRot(this.random.nextFloat() * 360.0f);
            leech.setXRot(0.0f);

            leech.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(leech.blockPosition()),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED,
                null
            );

            serverLevel.addFreshEntity(leech);
        }
    }
```

- [ ] **Step 4: Compile-check 1.21.5**

Run: `./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Apply the same three edits (imports, constant, phase-transition call + summon method) to `1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11`, `26.1.2`, `26.2`**

These eight versions use the same API shape as 1.21.5 (Step 1–3 content is identical).

- [ ] **Step 6: Compile-check one of them**

Run: `./gradlew :common-26.2:compileJava -Ptarget_mc_version=26.2`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Apply the edits to `1.21.2` and `1.21.4`, using `moveTo` instead of `setPos`/`setYRot`/`setXRot`**

Same imports (Step 1) and same constant (Step 2). For the phase-transition call, same as Step 3. For the summon method, use:

```java
    private void summonChronalLeeches(ServerLevel serverLevel) {
        for (int i = 0; i < CHRONAL_LEECH_SUMMON_COUNT; i++) {
            ChronalLeechEntity leech = ModEntities.CHRONAL_LEECH.get().create(
                serverLevel,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED
            );
            if (leech == null) {
                continue;
            }

            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 2.0 + this.random.nextDouble() * 2.0;
            leech.moveTo(
                this.getX() + Math.cos(angle) * distance,
                this.getY(),
                this.getZ() + Math.sin(angle) * distance,
                this.random.nextFloat() * 360.0f,
                0.0f
            );

            leech.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(leech.blockPosition()),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED,
                null
            );

            serverLevel.addFreshEntity(leech);
        }
    }
```

- [ ] **Step 8: Compile-check 1.21.2**

Run: `./gradlew :common-1.21.2:compileJava -Ptarget_mc_version=1.21.2`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Apply the edits to `1.21.1`, using `MobSpawnType` and 4-arg `finalizeSpawn`**

Imports — find:

```java
import com.chronodawn.core.time.MobAICanceller;
import com.chronodawn.entities.bosses.ExtendedMeleeAttackGoal;
```

Replace with:

```java
import com.chronodawn.core.time.MobAICanceller;
import com.chronodawn.entities.bosses.ExtendedMeleeAttackGoal;
import com.chronodawn.entities.mobs.ChronalLeechEntity;
import com.chronodawn.registry.ModEntities;
import net.minecraft.world.entity.MobSpawnType;
```

Constant and phase-transition call: same as Step 2 and Step 3.

Summon method:

```java
    private void summonChronalLeeches(ServerLevel serverLevel) {
        for (int i = 0; i < CHRONAL_LEECH_SUMMON_COUNT; i++) {
            ChronalLeechEntity leech = ModEntities.CHRONAL_LEECH.get().create(serverLevel);
            if (leech == null) {
                continue;
            }

            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 2.0 + this.random.nextDouble() * 2.0;
            leech.moveTo(
                this.getX() + Math.cos(angle) * distance,
                this.getY(),
                this.getZ() + Math.sin(angle) * distance,
                this.random.nextFloat() * 360.0f,
                0.0f
            );

            leech.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(leech.blockPosition()),
                MobSpawnType.TRIGGERED,
                null
            );

            serverLevel.addFreshEntity(leech);
        }
    }
```

- [ ] **Step 10: Compile-check 1.21.1**

Run: `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: Apply the edits to `1.20.1`, using `MobSpawnType` and 5-arg `finalizeSpawn`**

Imports: same as Step 9. Constant and phase-transition call: same as Step 2 and Step 3.

Summon method:

```java
    private void summonChronalLeeches(ServerLevel serverLevel) {
        for (int i = 0; i < CHRONAL_LEECH_SUMMON_COUNT; i++) {
            ChronalLeechEntity leech = ModEntities.CHRONAL_LEECH.get().create(serverLevel);
            if (leech == null) {
                continue;
            }

            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 2.0 + this.random.nextDouble() * 2.0;
            leech.moveTo(
                this.getX() + Math.cos(angle) * distance,
                this.getY(),
                this.getZ() + Math.sin(angle) * distance,
                this.random.nextFloat() * 360.0f,
                0.0f
            );

            leech.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(leech.blockPosition()),
                MobSpawnType.TRIGGERED,
                null,
                null
            );

            serverLevel.addFreshEntity(leech);
        }
    }
```

- [ ] **Step 12: Compile-check 1.20.1**

Run: `./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1`
Expected: BUILD SUCCESSFUL

- [ ] **Step 13: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/TimeTyrantEntity.java
git commit -m "feat(bosses): Time Tyrant summons Chronal Leeches at phase transitions with 2+ players"
```

---

## Task 13: CHANGELOG and full matrix verification

**Files:**
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes: all of Tasks 1–12.
- Produces: nothing further downstream — this is the final task.

- [ ] **Step 1: Add a CHANGELOG entry**

In `CHANGELOG.md`, under `## [Unreleased]` → `### Added`, add a new subsection (matching the existing `#### Minecraft Version Support` / `#### Configuration` style) after the existing subsections:

```markdown
#### Boss Multiplayer Behavior

- **Immediate target switching** — all six bosses now switch their attack target to whichever player just hit them, even mid-fight. Previously a boss kept chasing the first player it targeted while everyone else in the group hit it for free.
- **Multiplayer-only aggro distribution** — with 2 or more players in the fight, Time Guardian's and Time Tyrant's AoE attacks and Chronos Warden's Ground Slam recharge faster, Entropy Keeper's Entropy Burst repeats instead of firing once, and Time Tyrant summons 3 Chronal Leeches at each phase transition. Singleplayer fights are unaffected.
```

- [ ] **Step 2: Run the full verification matrix**

Run: `./gradlew checkAll`
Expected: BUILD SUCCESSFUL — this runs `cleanAll`, `validateResources`, `validateTranslations`, `buildAll`, `testAll`, and `gameTestAll` across all thirteen boss-bearing modules plus `1.20.1`.

If any step fails, fix the failing module's `TimeGuardianEntity.java` / `ChronosWardenEntity.java` / `ClockworkColossusEntity.java` / `EntropyKeeperEntity.java` / `TemporalPhantomEntity.java` / `TimeTyrantEntity.java` (whichever the failure names) to match the version-appropriate pattern from Tasks 2–12, then re-run.

- [ ] **Step 3: Manual multiplayer verification**

Per the design doc's Testing section — this is required, not optional, since no automated test exercises the actual multiplayer behavior:

1. Start a two-client session (e.g. `runClientFabric1_21_5` twice, or one client + one dedicated server) against each of the six bosses.
2. Confirm that a boss chasing player A switches to player B the instant B lands a hit.
3. Confirm Time Guardian's/Time Tyrant's AoE and Chronos Warden's Ground Slam fire noticeably more often than in a solo fight.
4. Confirm Entropy Keeper's Entropy Burst fires more than once in a two-player fight that lasts past its first trigger.
5. Confirm Time Tyrant spawns exactly 3 Chronal Leeches at each of its two phase transitions with 2 players present.
6. Repeat steps 2–5 solo and confirm no behavior changed from before this plan (no early AoE, no retriggered burst, no leeches).

- [ ] **Step 4: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs(changelog): record multiplayer boss targeting and aggro distribution"
```
