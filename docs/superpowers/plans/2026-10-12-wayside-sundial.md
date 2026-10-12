# Wayside Sundial Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make newly generated Old Sundials interactive: right-clicking the gnomon grants a time-of-day dependent buff, with a per-player, per-sundial cooldown, built on a cooldown foundation that later wayside features reuse.

**Architecture:** Version-independent logic (`PlayerCooldownTracker`, `SundialBlessing`) lives in `common/shared` and is unit tested. A new `sundial_gnomon` block with a `SundialGnomonBlockEntity` stores cooldowns. The block class is per version (14 modules), the abstract `CooldownActivatorBlockEntity` is shared for 1.20.1 to 1.21.4 and per version for 1.21.5 and later. `old_sundial.nbt` swaps its `iron_bars` post for the new block.

**Tech Stack:** Java 17/21/25, Architectury, Fabric, Forge 1.20.1, NeoForge, JUnit 5, Minecraft GameTest, Python `nbtlib` for the NBT edit.

**Spec:** `docs/superpowers/specs/2026-10-12-wayside-sundial-design.md`

## Global Constraints

- Supported modules: 1.20.1, 1.21.1, 1.21.2 (also serves 1.21.3), 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3.
- Block ID `sundial_gnomon`, block entity ID `sundial_gnomon`. No `BlockItem` for it.
- Cooldown default 24000 ticks, effect duration default 3600 ticks, both measured with `Level#getGameTime` for cooldown.
- Phases of `dayTime % 24000`: NOON 5000..6999 (Speed II), DAY 0..4999 and 7000..12999 (Speed I), NIGHT 13000..23999 (Night Vision).
- Breaking the gnomon drops `minecraft:iron_bars`.
- Config section `[gameplay.wayside]`, keys `sundial_cooldown_ticks` (20..=1728000) and `sundial_effect_duration_ticks` (20..=72000). Out-of-range values fall back per key.
- Code comments and docs in English. No em dashes or semicolons in docs and commit messages.
- Do not commit without the user's go-ahead. Each "Commit" step means: stage the listed files and ask the controller/user before running `git commit`. Commit messages use Conventional Commits and end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Never edit lang JSON with `json.load`/`json.dump`. Insert lines textually so blank-line groupings survive.
- Write temporary files under `$TMPDIR`.
- Gradle tasks fork a JVM and need the sandbox disabled. Run one Gradle invocation at a time in this worktree.

## Review Focus

1. Lowering `sundial_cooldown_ticks` in config while players are already on cooldown: the new, shorter value must apply immediately. Test in Task 1 (`shorterCooldownAppliesToExistingEntries`).
2. A corrupted or hand-edited `PlayerCooldowns` string in a saved block entity: loading must skip bad entries and never throw. Test in Task 1 (`decodeSkipsMalformedEntries`).
3. `dayTime` beyond one day or negative (26.x clocks return total ticks, commands can set any value): phase and shadow must use `floorMod`. Test in Task 2 (`wrapsLargeAndNegativeDayTime`).
4. A cooldown shorter than one minute remaining: the message must say "about 1 min", never 0. Test in Task 1 (`minutesRoundUp`).
5. Two players using the same sundial: each is tracked independently, and one player's use never blocks another. Tests in Task 1 (`playersAreIndependent`) and Task 4 GameTest.

---

## Version eras used in this plan

| Era | Modules | Interaction method | `codec()` | Client check | Speed effect | Action bar | Time of day |
|---|---|---|---|---|---|---|---|
| A | 1.20.1 | `public InteractionResult use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)` | none | `level.isClientSide` | `MobEffects.MOVEMENT_SPEED` | `player.displayClientMessage(c, true)` | `level.getDayTime()` |
| B | 1.21.1, 1.21.2, 1.21.4 | `protected InteractionResult useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)` | yes | `level.isClientSide` | `MobEffects.MOVEMENT_SPEED` | `displayClientMessage(c, true)` | `level.getDayTime()` |
| C | 1.21.5, 1.21.6, 1.21.7, 1.21.8 | as B | yes | `level.isClientSide` | `MobEffects.SPEED` | `displayClientMessage(c, true)` | `level.getDayTime()` |
| D | 1.21.9, 1.21.10, 1.21.11 | as B | yes | `level.isClientSide()` | `MobEffects.SPEED` | `displayClientMessage(c, true)` | `level.getDayTime()` |
| E | 26.1.2, 26.2 | as B | yes | `level.isClientSide()` | `MobEffects.SPEED` | `player.sendOverlayMessage(c)` | world clock helper |
| F | 26.3 | as B | none | `level.isClientSide()` | `MobEffects.SPEED` | `player.sendOverlayMessage(c)` | world clock helper |

`getRenderShape` is `public` in era A and `protected` in B to F. Each module's existing `ClockTowerTeleporterBlock.java` is the reference for its era. When in doubt, match that file.

Registration differences in `ModBlocks`:
- 1.20.1, 1.21.1: no `.setId(...)`.
- 1.21.2 to 1.21.10: `.setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(...)))`.
- 1.21.11, 26.1.2, 26.2, 26.3: same with `net.minecraft.resources.Identifier`.

Registration differences in `ModBlockEntities`:
- 1.20.1, 1.21.1: `BlockEntityType.Builder.of(Factory::new, block).build(null)`.
- 1.21.2 and later: `new BlockEntityType<>(Factory::new, Set.<Block>of(block))`.

Block entity serialization:
- 1.20.1 to 1.21.4: shared `CompatBlockEntity`, `saveData(CompoundTag)` / `loadData(CompoundTag)`, `tag.getString(key)` returns `""` when absent.
- 1.21.5: per-version `CompatBlockEntity` with `CompoundTag`, use `tag.getStringOr(key, "")`.
- 1.21.6 to 26.3: per-version `CompatBlockEntity` with `saveData(ValueOutput)` / `loadData(ValueInput)`, use `input.getStringOr(key, "")`.

---

### Task 1: PlayerCooldownTracker

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/PlayerCooldownTracker.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/PlayerCooldownTrackerTest.java`

**Interfaces:**
- Produces:
  - `public final class PlayerCooldownTracker`
  - `public long tryUse(UUID player, long gameTime, long cooldownTicks)` returns `0` when the use was accepted and recorded, otherwise the remaining ticks (> 0). Prunes expired entries first.
  - `public long remainingTicks(UUID player, long gameTime, long cooldownTicks)`
  - `public int size()`
  - `public String encode()`
  - `public static PlayerCooldownTracker decode(String encoded)`
  - `public static long toMinutesRoundedUp(long ticks)`

- [ ] **Step 1: Write the failing test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.PlayerCooldownTrackerTest'`
Expected: compilation FAIL, `PlayerCooldownTracker` not found.

- [ ] **Step 3: Write the implementation**

Copy the LGPL license header from an existing file such as `common/shared/src/main/java/com/chronodawn/config/TimeFlowSettings.java` to the top of the new file, then:

```java
package com.chronodawn.core.wayside;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player cooldowns for one wayside activator (a sundial, a well, ...).
 *
 * <p>Kept free of Minecraft types so it can be unit tested without bootstrapping
 * registries. The block entity that owns it persists {@link #encode()} as a single
 * string because string NBT access reads and writes the same way on every
 * supported Minecraft version.
 */
public final class PlayerCooldownTracker {

    private static final char ENTRY_SEPARATOR = ',';
    private static final char VALUE_SEPARATOR = '=';
    private static final long TICKS_PER_MINUTE = 1200L;

    private final Map<UUID, Long> lastUseByPlayer = new HashMap<>();

    /**
     * Attempts a use at {@code gameTime}.
     *
     * @return 0 if the use was accepted and recorded, otherwise the ticks left
     */
    public long tryUse(UUID player, long gameTime, long cooldownTicks) {
        prune(gameTime, cooldownTicks);
        long remaining = remainingTicks(player, gameTime, cooldownTicks);
        if (remaining == 0) {
            lastUseByPlayer.put(player, gameTime);
        }
        return remaining;
    }

    public long remainingTicks(UUID player, long gameTime, long cooldownTicks) {
        Long lastUse = lastUseByPlayer.get(player);
        if (lastUse == null || gameTime < lastUse) {
            // gameTime only runs backwards when a world is restored or copied, so do not lock players out.
            return 0;
        }
        return Math.max(0, cooldownTicks - (gameTime - lastUse));
    }

    public int size() {
        return lastUseByPlayer.size();
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, Long> entry : lastUseByPlayer.entrySet()) {
            if (sb.length() > 0) {
                sb.append(ENTRY_SEPARATOR);
            }
            sb.append(entry.getKey()).append(VALUE_SEPARATOR).append(entry.getValue());
        }
        return sb.toString();
    }

    /** Decodes {@link #encode()} output. Malformed entries are skipped, never thrown. */
    public static PlayerCooldownTracker decode(String encoded) {
        PlayerCooldownTracker tracker = new PlayerCooldownTracker();
        if (encoded == null || encoded.isEmpty()) {
            return tracker;
        }
        for (String entry : encoded.split(String.valueOf(ENTRY_SEPARATOR))) {
            int separator = entry.indexOf(VALUE_SEPARATOR);
            if (separator <= 0 || separator == entry.length() - 1) {
                continue;
            }
            try {
                UUID player = UUID.fromString(entry.substring(0, separator));
                long lastUse = Long.parseLong(entry.substring(separator + 1));
                tracker.lastUseByPlayer.put(player, lastUse);
            } catch (IllegalArgumentException ignored) {
                // NumberFormatException is an IllegalArgumentException, so this covers both parts.
            }
        }
        return tracker;
    }

    /** Converts remaining ticks to whole minutes for display, never returning 0 for a positive value. */
    public static long toMinutesRoundedUp(long ticks) {
        return (ticks + TICKS_PER_MINUTE - 1) / TICKS_PER_MINUTE;
    }

    private void prune(long gameTime, long cooldownTicks) {
        lastUseByPlayer.values().removeIf(lastUse -> gameTime < lastUse || gameTime - lastUse >= cooldownTicks);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.PlayerCooldownTrackerTest'`
Expected: PASS, 12 tests.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/core/wayside/PlayerCooldownTracker.java \
        common/shared/src/test/java/com/chronodawn/unit/PlayerCooldownTrackerTest.java
git commit -m "feat: add per-player cooldown tracker for wayside features"
```

---

### Task 2: SundialBlessing

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/SundialBlessing.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/SundialBlessingTest.java`

**Interfaces:**
- Produces:
  - `public enum SundialBlessing { DAY, NOON, NIGHT }`
  - `public enum SundialBlessing.Effect { SPEED, NIGHT_VISION }`
  - `public Effect effect()`, `public int amplifier()`, `public int particleCount()`, `public float soundPitch()`, `public String messageKey()`
  - `public static SundialBlessing forDayTime(long dayTime)`
  - `public static double shadowOffsetX(long dayTime)` returns a value in [-1, 1], negative is west (-X), 0 at noon and at night.

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.unit;

import com.chronodawn.core.wayside.SundialBlessing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SundialBlessingTest {

    @Test
    void phaseBoundaries() {
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(0));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(4999));
        assertEquals(SundialBlessing.NOON, SundialBlessing.forDayTime(5000));
        assertEquals(SundialBlessing.NOON, SundialBlessing.forDayTime(6999));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(7000));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(12999));
        assertEquals(SundialBlessing.NIGHT, SundialBlessing.forDayTime(13000));
        assertEquals(SundialBlessing.NIGHT, SundialBlessing.forDayTime(23999));
    }

    @Test
    void wrapsLargeAndNegativeDayTime() {
        assertEquals(SundialBlessing.NOON, SundialBlessing.forDayTime(24000L * 1000 + 6000));
        assertEquals(SundialBlessing.NIGHT, SundialBlessing.forDayTime(-1));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(-24000));
        assertEquals(-1.0, SundialBlessing.shadowOffsetX(24000L * 5), 1e-9);
    }

    @Test
    void effectsPerPhase() {
        assertEquals(SundialBlessing.Effect.SPEED, SundialBlessing.DAY.effect());
        assertEquals(0, SundialBlessing.DAY.amplifier());
        assertEquals(SundialBlessing.Effect.SPEED, SundialBlessing.NOON.effect());
        assertEquals(1, SundialBlessing.NOON.amplifier());
        assertEquals(SundialBlessing.Effect.NIGHT_VISION, SundialBlessing.NIGHT.effect());
        assertEquals(0, SundialBlessing.NIGHT.amplifier());
    }

    @Test
    void noonHasTheMostParticles() {
        assertEquals(true, SundialBlessing.NOON.particleCount() > SundialBlessing.DAY.particleCount());
        assertEquals(true, SundialBlessing.DAY.particleCount() > SundialBlessing.NIGHT.particleCount());
    }

    @Test
    void shadowPointsAwayFromTheSun() {
        // The sun rises in the east (+X), so the morning shadow points west (-X).
        assertEquals(-1.0, SundialBlessing.shadowOffsetX(0), 1e-9);
        assertEquals(0.0, SundialBlessing.shadowOffsetX(6000), 1e-9);
        assertEquals(1.0, SundialBlessing.shadowOffsetX(12000), 1e-9);
        assertEquals(1.0, SundialBlessing.shadowOffsetX(12999), 1e-9);
        assertEquals(0.0, SundialBlessing.shadowOffsetX(18000), 1e-9);
    }

    @Test
    void messageKeysAreDistinct() {
        assertEquals("message.chronodawn.sundial_gnomon.day", SundialBlessing.DAY.messageKey());
        assertEquals("message.chronodawn.sundial_gnomon.noon", SundialBlessing.NOON.messageKey());
        assertEquals("message.chronodawn.sundial_gnomon.night", SundialBlessing.NIGHT.messageKey());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.SundialBlessingTest'`
Expected: compilation FAIL, `SundialBlessing` not found.

- [ ] **Step 3: Write the implementation**

License header as in Task 1, then:

```java
package com.chronodawn.core.wayside;

/**
 * What an Old Sundial grants, chosen from the dimension's time of day.
 *
 * <p>Version-independent so phase boundaries can be unit tested. Each version's
 * {@code SundialGnomonBlock} maps {@link Effect} to the matching vanilla effect.
 */
public enum SundialBlessing {
    DAY(Effect.SPEED, 0, 12, 1.0f, "message.chronodawn.sundial_gnomon.day"),
    NOON(Effect.SPEED, 1, 24, 1.2f, "message.chronodawn.sundial_gnomon.noon"),
    NIGHT(Effect.NIGHT_VISION, 0, 8, 0.6f, "message.chronodawn.sundial_gnomon.night");

    public enum Effect { SPEED, NIGHT_VISION }

    private static final long TICKS_PER_DAY = 24000L;
    private static final long NOON_START = 5000L;
    private static final long NOON_END = 7000L;
    private static final long NIGHT_START = 13000L;
    private static final long SUNSET = 12000L;

    private final Effect effect;
    private final int amplifier;
    private final int particleCount;
    private final float soundPitch;
    private final String messageKey;

    SundialBlessing(Effect effect, int amplifier, int particleCount, float soundPitch, String messageKey) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.particleCount = particleCount;
        this.soundPitch = soundPitch;
        this.messageKey = messageKey;
    }

    public Effect effect() { return effect; }
    public int amplifier() { return amplifier; }
    public int particleCount() { return particleCount; }
    public float soundPitch() { return soundPitch; }
    public String messageKey() { return messageKey; }

    public static SundialBlessing forDayTime(long dayTime) {
        long time = Math.floorMod(dayTime, TICKS_PER_DAY);
        if (time >= NIGHT_START) {
            return NIGHT;
        }
        if (time >= NOON_START && time < NOON_END) {
            return NOON;
        }
        return DAY;
    }

    /**
     * X offset of the gnomon's shadow, from -1 (west) to 1 (east).
     * The sun crosses from east to west between dayTime 0 and 12000, and the
     * shadow points away from it. Night has no shadow.
     */
    public static double shadowOffsetX(long dayTime) {
        long time = Math.floorMod(dayTime, TICKS_PER_DAY);
        if (time >= NIGHT_START) {
            return 0.0;
        }
        double sunProgress = Math.min(time, SUNSET) / (double) SUNSET;
        double offset = -Math.cos(Math.PI * sunProgress);
        // Avoid -0.0 and 6.1e-17 style noise at noon so callers can compare against 0.
        return Math.abs(offset) < 1e-9 ? 0.0 : offset;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.SundialBlessingTest'`
Expected: PASS, 6 tests.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/core/wayside/SundialBlessing.java \
        common/shared/src/test/java/com/chronodawn/unit/SundialBlessingTest.java
git commit -m "feat: choose the Old Sundial blessing from the time of day"
```

---

### Task 3: Wayside config section

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java` (record `Gameplay`, around lines 105-125)
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java` (defaults near `TIME_FLOW_DEFAULTS` line ~61, and `new ChronoDawnConfig.Gameplay(` near line ~109)
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java` (key constants near line ~100, `parseGameplay` near line ~398, new `parseWayside`)
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml` (after `[gameplay.time_flow]`)
- Modify: `docs/configuration.md` (after the `[gameplay.time_flow]` section)
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java`

**Interfaces:**
- Produces:
  - `public record WaysideSettings(int sundialCooldownTicks, int sundialEffectDurationTicks)`
  - `ConfigDefaults.WAYSIDE_DEFAULTS = new WaysideSettings(24000, 3600)`
  - `ChronoDawnConfig.get().gameplay().wayside()`

- [ ] **Step 1: Write the failing tests**

Add to `ConfigLoaderTest` (next to the `timeFlow_*` tests, around line 547), and add `import com.chronodawn.config.WaysideSettings;` with the other config imports:

```java
    @Test
    void wayside_missingSection_fallsBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(ConfigDefaults.WAYSIDE_DEFAULTS, config.gameplay().wayside());
    }

    @Test
    void wayside_validCustomValues_areReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.wayside]\n" +
            "sundial_cooldown_ticks = 6000\n" +
            "sundial_effect_duration_ticks = 1200\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(new WaysideSettings(6000, 1200), config.gameplay().wayside());
    }

    @Test
    void wayside_outOfRangeValues_fallBackPerKey(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.wayside]\n" +
            "sundial_cooldown_ticks = 0\n" +
            "sundial_effect_duration_ticks = 2400\n");
        WaysideSettings settings = ConfigLoader.load(tmp).gameplay().wayside();
        assertEquals(ConfigDefaults.WAYSIDE_DEFAULTS.sundialCooldownTicks(), settings.sundialCooldownTicks());
        assertEquals(2400, settings.sundialEffectDurationTicks());
    }

    @Test
    void wayside_tooLongEffect_fallsBack(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.wayside]\n" +
            "sundial_effect_duration_ticks = 72001\n");
        WaysideSettings settings = ConfigLoader.load(tmp).gameplay().wayside();
        assertEquals(ConfigDefaults.WAYSIDE_DEFAULTS.sundialEffectDurationTicks(),
            settings.sundialEffectDurationTicks());
    }
```

These follow the setup of the neighboring `timeFlow_*` tests (`chronodawn.toml` in a `@TempDir`, `schema_version = 1` for the missing-section case).

- [ ] **Step 2: Run tests to verify they fail**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.ConfigLoaderTest'`
Expected: compilation FAIL, `WaysideSettings` not found.

- [ ] **Step 3: Implement**

`WaysideSettings.java` (license header as in other config files):

```java
package com.chronodawn.config;

/**
 * Settings for interactive wayside features such as the Old Sundial.
 *
 * <p>Cooldowns are per player and per feature instance, measured in game ticks.
 */
public record WaysideSettings(
    int sundialCooldownTicks,
    int sundialEffectDurationTicks
) {}
```

`ConfigDefaults.java`, after `TIME_FLOW_DEFAULTS`:

```java
    // One Minecraft day of cooldown and a three-minute buff, from the wayside design spec.
    public static final WaysideSettings WAYSIDE_DEFAULTS = new WaysideSettings(24000, 3600);
```

and in the `new ChronoDawnConfig.Gameplay(` call, add `WAYSIDE_DEFAULTS,` after `PORTAL_DEFAULTS,`.

`ChronoDawnConfig.java`, record `Gameplay`: add `WaysideSettings wayside` after `PortalSettings portals`, and update both convenience constructors so existing callers keep compiling:

```java
    public record Gameplay(
        TimeDistortionSettings timeDistortion,
        TimeFlowSettings timeFlow,
        PortalSettings portals,
        WaysideSettings wayside,
        BossesConfig bosses
    ) {
        /** Preserve the existing construction pattern for callers that only configure bosses. */
        public Gameplay(BossesConfig bosses) {
            this(
                ConfigDefaults.TIME_DISTORTION_DEFAULTS,
                ConfigDefaults.TIME_FLOW_DEFAULTS,
                ConfigDefaults.PORTAL_DEFAULTS,
                ConfigDefaults.WAYSIDE_DEFAULTS,
                bosses
            );
        }

        /** Preserve the existing construction pattern for callers that configure Time Distortion. */
        public Gameplay(TimeDistortionSettings timeDistortion, BossesConfig bosses) {
            this(timeDistortion, ConfigDefaults.TIME_FLOW_DEFAULTS, ConfigDefaults.PORTAL_DEFAULTS,
                ConfigDefaults.WAYSIDE_DEFAULTS, bosses);
        }
    }
```

`ConfigLoader.java`: add constants after the `K_PORTAL_*` constants:

```java
    private static final String K_WAYSIDE = "wayside";
    private static final String K_WS_SUNDIAL_COOLDOWN_TICKS = "sundial_cooldown_ticks";
    private static final String K_WS_SUNDIAL_EFFECT_DURATION_TICKS = "sundial_effect_duration_ticks";
    private static final int MIN_WAYSIDE_TICKS = 20;
    private static final int MAX_SUNDIAL_COOLDOWN_TICKS = 1_728_000;
    private static final int MAX_SUNDIAL_EFFECT_DURATION_TICKS = 72_000;
```

In `parseGameplay`, pass `parseWayside(parsed),` after `parsePortals(parsed),`. Add after `parsePortals`:

```java
    private static WaysideSettings parseWayside(CommentedConfig parsed) {
        String path = K_GAMEPLAY + "." + K_WAYSIDE;
        WaysideSettings defaults = ConfigDefaults.WAYSIDE_DEFAULTS;

        int cooldown = parsed.<Number>getOptional(path + "." + K_WS_SUNDIAL_COOLDOWN_TICKS)
            .map(Number::intValue)
            .orElse(defaults.sundialCooldownTicks());
        int duration = parsed.<Number>getOptional(path + "." + K_WS_SUNDIAL_EFFECT_DURATION_TICKS)
            .map(Number::intValue)
            .orElse(defaults.sundialEffectDurationTicks());

        // Validation: each field reverts independently so one bad value doesn't reset the other.
        if (cooldown < MIN_WAYSIDE_TICKS || cooldown > MAX_SUNDIAL_COOLDOWN_TICKS) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_WS_SUNDIAL_COOLDOWN_TICKS, cooldown, MIN_WAYSIDE_TICKS, MAX_SUNDIAL_COOLDOWN_TICKS,
                defaults.sundialCooldownTicks()
            );
            cooldown = defaults.sundialCooldownTicks();
        }
        if (duration < MIN_WAYSIDE_TICKS || duration > MAX_SUNDIAL_EFFECT_DURATION_TICKS) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_WS_SUNDIAL_EFFECT_DURATION_TICKS, duration, MIN_WAYSIDE_TICKS,
                MAX_SUNDIAL_EFFECT_DURATION_TICKS, defaults.sundialEffectDurationTicks()
            );
            duration = defaults.sundialEffectDurationTicks();
        }

        return new WaysideSettings(cooldown, duration);
    }
```

`chronodawn-default-config.toml`, insert after the `[gameplay.time_flow]` block (before `[gameplay.portals]`):

```toml
[gameplay.wayside]
# Old Sundial: how long (in ticks, 20 ticks = 1 second) each player must wait
# before using the same sundial again. Counted in game time, so sleeping does
# not shorten it. Range: 20 to 1728000.
sundial_cooldown_ticks = 24000

# Old Sundial: how long the granted Speed or Night Vision lasts, in ticks.
# Range: 20 to 72000.
sundial_effect_duration_ticks = 3600

```

`docs/configuration.md`, insert after the `[gameplay.time_flow]` section (after its last example, before the next `###` heading):

````markdown
### `[gameplay.wayside]`

Controls interactive wayside features. Old Sundials generated after this
update grant Speed in daylight (Speed II around noon) and Night Vision at
night when a player right-clicks the gnomon. Each player has a separate
cooldown for each sundial.

```toml
[gameplay.wayside]
sundial_cooldown_ticks = 24000
sundial_effect_duration_ticks = 3600
```

| Field | Type | Default | Range | Notes |
| --- | --- | --- | --- | --- |
| `sundial_cooldown_ticks` | integer | `24000` | `20..=1728000` | Ticks (20 ticks = 1 second) before the same player can use the same sundial again. Counted in game time, so sleeping does not shorten it. |
| `sundial_effect_duration_ticks` | integer | `3600` | `20..=72000` | How long the granted effect lasts. |
````

If `docs/configuration.md` has a table of contents or a section list near the top, add `[gameplay.wayside]` there too.

- [ ] **Step 4: Run tests to verify they pass**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.ConfigLoaderTest'`
Expected: PASS, including the 4 new tests.

Also run: `grep -rn "new ChronoDawnConfig.Gameplay(" common --include='*.java' | grep -v /build/`
Expected: only `ConfigLoader.java`, `ConfigDefaults.java`, and the 2-argument calls in `TimeDistortionEffectTest.java` files, which still compile through the convenience constructor.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java \
        common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java \
        common/shared/src/main/resources/chronodawn-default-config.toml \
        common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java \
        docs/configuration.md
git commit -m "feat: add wayside config section for the Old Sundial"
```

---

### Task 4: Sundial gnomon block on 1.21.1 (end to end)

This task builds the full feature on the modpack target 1.21.1, with all shared resources. Other modules do not compile between this task and Task 5, because the shared block entity references `ModBlockEntities.SUNDIAL_GNOMON`. Build and test only 1.21.1 in this task.

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` (near `CLOCK_TOWER_TELEPORTER` line ~252)
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockEntityId.java` (enum list line ~32)
- Create: `common/shared/src/main/java/com/chronodawn/blocks/CooldownActivatorBlockEntity.java`
- Create: `common/shared/src/main/java/com/chronodawn/blocks/SundialGnomonBlockEntity.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/SundialGnomonBlock.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java` (after `CLOCK_TOWER_TELEPORTER`, line ~449)
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlockEntities.java` (after `CLOCK_TOWER_TELEPORTER`, line ~29)
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/sundial_gnomon.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/sundial_gnomon.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/sundial_gnomon.json`
- Create: `common/26.3/src/main/resources/data/chronodawn/loot_table/blocks/sundial_gnomon.json`
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- Modify: `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- Modify: `en_us.json` and `ja_jp.json` in `common/1.20.1/src/main/resources/assets/chronodawn/lang/`, `common/1.21.1/src/main/resources/assets/chronodawn/lang/`, `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/`
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/SundialGnomonTests.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` (line ~445)

**Interfaces:**
- Consumes: `PlayerCooldownTracker` (Task 1), `SundialBlessing` (Task 2), `ChronoDawnConfig.get().gameplay().wayside()` (Task 3)
- Produces:
  - `ModBlockId.SUNDIAL_GNOMON`, `ModBlockEntityId.SUNDIAL_GNOMON`
  - `ModBlocks.SUNDIAL_GNOMON` (`RegistrySupplier<Block>`), `ModBlockEntities.SUNDIAL_GNOMON` (`RegistrySupplier<BlockEntityType<SundialGnomonBlockEntity>>`)
  - `public abstract class CooldownActivatorBlockEntity extends CompatBlockEntity` with `public long tryActivate(UUID player, long gameTime, long cooldownTicks)`
  - `public class SundialGnomonBlockEntity extends CooldownActivatorBlockEntity` with ctor `(BlockPos, BlockState)`
  - `public class SundialGnomonBlock extends BaseEntityBlock`
  - `SundialGnomonTests.generateTests(MobBehaviorTests.TestFactory<T>)`

- [ ] **Step 1: Write the failing GameTest**

`common/gametest/src/main/java/com/chronodawn/gametest/SundialGnomonTests.java`:

```java
package com.chronodawn.gametest;

import com.chronodawn.blocks.SundialGnomonBlockEntity;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SundialGnomonTests {

    private static final BlockPos GNOMON_POS = new BlockPos(1, 2, 1);
    private static final long COOLDOWN = 24000;

    private SundialGnomonTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("sundial_gnomon_tracks_cooldown_per_player",
            SundialGnomonTests::testCooldownPerPlayer));
        return tests;
    }

    /**
     * Test: the placed gnomon carries a block entity that refuses a second use by
     * the same player within the cooldown and accepts another player.
     */
    private static void testCooldownPerPlayer(GameTestHelper helper) {
        helper.setBlock(GNOMON_POS, ModBlocks.SUNDIAL_GNOMON.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(GNOMON_POS)) instanceof SundialGnomonBlockEntity gnomon)) {
            CompatGameTestHelper.fail(helper, "sundial_gnomon has no SundialGnomonBlockEntity");
            return;
        }
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        long now = helper.getLevel().getGameTime();
        if (gnomon.tryActivate(first, now, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "First activation was refused");
            return;
        }
        if (gnomon.tryActivate(first, now + 1, COOLDOWN) <= 0) {
            CompatGameTestHelper.fail(helper, "Second activation by the same player was accepted within the cooldown");
            return;
        }
        if (gnomon.tryActivate(second, now + 1, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "Activation by a different player was refused");
            return;
        }
        helper.succeed();
    }
}
```

In `common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java`, add after the `TimelessMushroomTests` line (~445):

```java
        all.addAll(SundialGnomonTests.generateTests(NamedTest::new));
```

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: compilation FAIL, `SundialGnomonBlockEntity` and `ModBlocks.SUNDIAL_GNOMON` not found.

- [ ] **Step 3: Add IDs**

`ModBlockId.java`, after `CLOCK_TOWER_TELEPORTER(def("clock_tower_teleporter")),`:

```java
    SUNDIAL_GNOMON(def("sundial_gnomon").cutout()),
```

`ModBlockEntityId.java`, after `BOSS_ROOM_BOUNDARY_MARKER("boss_room_boundary_marker"),`:

```java
    SUNDIAL_GNOMON("sundial_gnomon"),
```

Confirm the cutout render layer is applied from `ModBlockId` on all three loaders: `grep -n "renderLayer()" fabric/1.21.1/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java neoforge/base/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java forge/base/src/main/java/com/chronodawn/forge/client/ChronoDawnClientForge.java` must show a loop over `ModBlockId` in each. If any loader does not apply render layers from `ModBlockId`, stop and report it instead of guessing. The blockstate uses vanilla models, so a `render_type` in model JSON is not an option.

- [ ] **Step 4: Shared block entities**

`common/shared/src/main/java/com/chronodawn/blocks/CooldownActivatorBlockEntity.java` (license header first):

```java
package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockEntity;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Base block entity for wayside activators that every player may use once per cooldown.
 *
 * <p>1.21.5 and later have their own copies of this class for the changed NBT API.
 */
public abstract class CooldownActivatorBlockEntity extends CompatBlockEntity {
    private static final String COOLDOWNS_KEY = "PlayerCooldowns";

    private PlayerCooldownTracker cooldowns = new PlayerCooldownTracker();

    protected CooldownActivatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @return 0 if the activation was accepted and recorded, otherwise the ticks left
     */
    public long tryActivate(UUID player, long gameTime, long cooldownTicks) {
        long remaining = cooldowns.tryUse(player, gameTime, cooldownTicks);
        // Pruning can change the data even when the use is refused.
        setChanged();
        return remaining;
    }

    @Override
    public void saveData(CompoundTag tag) {
        tag.putString(COOLDOWNS_KEY, cooldowns.encode());
    }

    @Override
    public void loadData(CompoundTag tag) {
        cooldowns = PlayerCooldownTracker.decode(tag.getString(COOLDOWNS_KEY));
    }
}
```

`common/shared/src/main/java/com/chronodawn/blocks/SundialGnomonBlockEntity.java` (license header first):

```java
package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds per-player cooldowns for one Old Sundial gnomon.
 */
public class SundialGnomonBlockEntity extends CooldownActivatorBlockEntity {
    public SundialGnomonBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.SUNDIAL_GNOMON.get(), pos, state);
    }
}
```

- [ ] **Step 5: 1.21.1 block (era B)**

`common/1.21.1/src/main/java/com/chronodawn/blocks/SundialGnomonBlock.java` (license header first):

```java
package com.chronodawn.blocks;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.WaysideSettings;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import com.chronodawn.core.wayside.SundialBlessing;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The post of an Old Sundial. Right-clicking it grants a time-of-day blessing,
 * once per cooldown for each player.
 */
public class SundialGnomonBlock extends BaseEntityBlock {
    public static final MapCodec<SundialGnomonBlock> CODEC = simpleCodec(SundialGnomonBlock::new);
    private static final VoxelShape SHAPE = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
    // The gnomon stands on a center block, so the dial face is one block below its base.
    private static final double DIAL_FACE_OFFSET_Y = -0.9;
    private static final double SHADOW_LENGTH = 1.5;

    public SundialGnomonBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SundialGnomonBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof SundialGnomonBlockEntity gnomon) {
            activate(serverLevel, pos, player, gnomon);
        }
        return InteractionResult.SUCCESS;
    }

    private static void activate(ServerLevel level, BlockPos pos, Player player, SundialGnomonBlockEntity gnomon) {
        WaysideSettings settings = ChronoDawnConfig.get().gameplay().wayside();
        long remaining = gnomon.tryActivate(player.getUUID(), level.getGameTime(), settings.sundialCooldownTicks());
        if (remaining > 0) {
            player.displayClientMessage(
                Component.translatable("message.chronodawn.sundial_gnomon.cooldown",
                    PlayerCooldownTracker.toMinutesRoundedUp(remaining)),
                true
            );
            return;
        }

        long dayTime = level.getDayTime();
        SundialBlessing blessing = SundialBlessing.forDayTime(dayTime);
        player.addEffect(new MobEffectInstance(
            blessing.effect() == SundialBlessing.Effect.SPEED ? MobEffects.MOVEMENT_SPEED : MobEffects.NIGHT_VISION,
            settings.sundialEffectDurationTicks(),
            blessing.amplifier()
        ));
        spawnParticles(level, pos, blessing, dayTime);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, blessing.soundPitch());
        player.displayClientMessage(Component.translatable(blessing.messageKey()), true);
    }

    private static void spawnParticles(ServerLevel level, BlockPos pos, SundialBlessing blessing, long dayTime) {
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        if (blessing == SundialBlessing.NIGHT) {
            SimpleParticleType moonlight = ParticleTypes.GLOW;
            level.sendParticles(moonlight, centerX, pos.getY() + 0.8, centerZ,
                blessing.particleCount(), 0.3, 0.4, 0.3, 0.01);
            return;
        }
        double shadowX = SundialBlessing.shadowOffsetX(dayTime);
        double faceY = pos.getY() + DIAL_FACE_OFFSET_Y;
        int count = blessing.particleCount();
        for (int i = 0; i < count; i++) {
            double t = (double) i / count;
            level.sendParticles(ParticleTypes.END_ROD,
                centerX + shadowX * SHADOW_LENGTH * t, faceY, centerZ,
                1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
```

- [ ] **Step 6: 1.21.1 registration**

`common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java`, after the `CLOCK_TOWER_TELEPORTER` registration (line ~449). Add `import com.chronodawn.blocks.SundialGnomonBlock;` if the file imports block classes individually rather than with a wildcard.

```java
    /**
     * Sundial Gnomon - interactive post of the Old Sundial wayside feature.
     * No item: it only appears through worldgen, and breaking it drops iron bars.
     */
    public static final RegistrySupplier<Block> SUNDIAL_GNOMON = BLOCKS.register(
        ModBlockId.SUNDIAL_GNOMON.id(),
        () -> new SundialGnomonBlock(
            CompatBlockProperties.ofFullCopy(Blocks.IRON_BARS)
                .strength(5.0f, 6.0f)
                .noOcclusion()
        )
    );
```

`common/1.21.1/src/main/java/com/chronodawn/registry/ModBlockEntities.java`, after `CLOCK_TOWER_TELEPORTER` (line ~29). Add `import com.chronodawn.blocks.SundialGnomonBlockEntity;` if needed.

```java
    /**
     * Sundial Gnomon BlockEntity - stores per-player cooldowns.
     */
    public static final RegistrySupplier<BlockEntityType<SundialGnomonBlockEntity>> SUNDIAL_GNOMON =
        BLOCK_ENTITIES.register(ModBlockEntityId.SUNDIAL_GNOMON.id(), () ->
            BlockEntityType.Builder.of(
                SundialGnomonBlockEntity::new,
                ModBlocks.SUNDIAL_GNOMON.get()
            ).build(null)
        );
```

Check that no `ModItems` registration loops over every `ModBlocks` entry to create block items: `grep -n "SUNDIAL\|BLOCKS.getRegistrar\|forEach" common/1.21.1/src/main/java/com/chronodawn/registry/ModItems.java`. Block items in this project are registered explicitly, so nothing should be needed.

- [ ] **Step 7: Shared resources**

`common/shared/src/main/resources/assets/chronodawn/blockstates/sundial_gnomon.json` (same parts vanilla iron bars uses when unconnected):

```json
{
  "multipart": [
    { "apply": { "model": "minecraft:block/iron_bars_post_ends" } },
    { "apply": { "model": "minecraft:block/iron_bars_post" } }
  ]
}
```

`common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/sundial_gnomon.json` and `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/sundial_gnomon.json` (identical):

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:iron_bars"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
```

`common/26.3/src/main/resources/data/chronodawn/loot_table/blocks/sundial_gnomon.json` (26.3 schema):

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:iron_bars"
        }
      ],
      "condition": {
        "type": "minecraft:survives_explosion"
      }
    }
  ]
}
```

In both `mineable/pickaxe.json` files, add `"chronodawn:sundial_gnomon",` on the line after `"chronodawn:clockwork_dial",`. Keep the JSON valid (watch the trailing comma if `clockwork_dial` is the last entry).

Lang, line-based insertion. In each of the six files, insert after the line containing `"block.chronodawn.clockwork_dial"` and after the line containing `"message.chronodawn.teleporter_unbreakable"`. Use a small Python script that reads lines and inserts text, not `json.load`.

en_us, after `block.chronodawn.clockwork_dial`:

```
  "block.chronodawn.sundial_gnomon": "Sundial Gnomon",
```

en_us, after `message.chronodawn.teleporter_unbreakable`:

```
  "message.chronodawn.sundial_gnomon.day": "The sundial's shadow quickens your steps.",
  "message.chronodawn.sundial_gnomon.noon": "At high noon, the sundial's light urges you onward.",
  "message.chronodawn.sundial_gnomon.night": "Moonlight gathers on the dial and clears your sight.",
  "message.chronodawn.sundial_gnomon.cooldown": "The shadow has not moved yet. Try again in about %s min.",
```

ja_jp, after `block.chronodawn.clockwork_dial`:

```
  "block.chronodawn.sundial_gnomon": "日時計の針",
```

ja_jp, after `message.chronodawn.teleporter_unbreakable`:

```
  "message.chronodawn.sundial_gnomon.day": "日時計の影が、足取りを軽くする。",
  "message.chronodawn.sundial_gnomon.noon": "正午の光が、先へと背中を押す。",
  "message.chronodawn.sundial_gnomon.night": "月明かりが文字盤に集まり、視界を澄ませる。",
  "message.chronodawn.sundial_gnomon.cooldown": "影はまだ動いていない。あと約%s分で再び使える。",
```

Validate each edited JSON file parses: `python3 -c "import json,sys; [json.load(open(f)) for f in sys.argv[1:]]" <files>`.

- [ ] **Step 8: Build and run tests for 1.21.1**

Run (sandbox disabled), one at a time:
1. `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1`
   Expected: PASS (all unit tests, including `ModIdEnumTest`, `CrossVersionConsistencyTest`, `LootTableValidationTest`).
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1`
   Expected: PASS, including `sundial_gnomon_tracks_cooldown_per_player` and the registry consistency tests for `sundial_gnomon`.
3. `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1`
   Expected: PASS, same tests.
4. `./gradlew validateResources validateTranslations`
   Expected: PASS. If `validateTranslations` reports missing keys for other versions, they are the lang files edited in Step 7, so recheck those insertions.

If a unit test such as `CrossVersionConsistencyTest` fails because other versions lack `SundialGnomonBlock`, note it and continue. Task 5 adds them, and the test must pass at the end of Task 5.

- [ ] **Step 9: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java \
        common/shared/src/main/java/com/chronodawn/registry/ModBlockEntityId.java \
        common/shared/src/main/java/com/chronodawn/blocks/CooldownActivatorBlockEntity.java \
        common/shared/src/main/java/com/chronodawn/blocks/SundialGnomonBlockEntity.java \
        common/1.21.1/src/main/java/com/chronodawn/blocks/SundialGnomonBlock.java \
        common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java \
        common/1.21.1/src/main/java/com/chronodawn/registry/ModBlockEntities.java \
        common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java \
        common/gametest/src/main/java/com/chronodawn/gametest/SundialGnomonTests.java \
        common/shared/src/main/resources/assets/chronodawn/blockstates/sundial_gnomon.json \
        common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/sundial_gnomon.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/sundial_gnomon.json \
        common/26.3/src/main/resources/data/chronodawn/loot_table/blocks/sundial_gnomon.json \
        common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json \
        common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json \
        common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json \
        common/1.20.1/src/main/resources/assets/chronodawn/lang/ja_jp.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/ja_jp.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/ja_jp.json
git commit -m "feat: add the interactive Sundial Gnomon block on 1.21.1"
```

---

### Task 5: Port the gnomon to the other 13 modules

**Files:**
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/SundialGnomonBlock.java` for v in 1.20.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/CooldownActivatorBlockEntity.java` for v in 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3
- Modify: `common/<v>/build.gradle` for the same 10 modules (add `'CooldownActivatorBlockEntity.java'` to `excludedFiles`)
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java` and `ModBlockEntities.java` for all 13
- Modify: `common/<v>/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` for all 13 (add `all.addAll(SundialGnomonTests.generateTests(NamedTest::new));` after the `TimelessMushroomTests` line)

**Interfaces:**
- Consumes: everything Task 4 produces, with identical names and signatures.
- Produces: the same `SundialGnomonBlock`, `ModBlocks.SUNDIAL_GNOMON`, `ModBlockEntities.SUNDIAL_GNOMON` in every module.

- [ ] **Step 1: Block entity base for 1.21.5**

`common/1.21.5/src/main/java/com/chronodawn/blocks/CooldownActivatorBlockEntity.java`: the Task 4 shared file with one change, plus the class Javadoc noting the version:

```java
    @Override
    public void loadData(CompoundTag tag) {
        // In 1.21.5, getString() returns Optional, so use getStringOr()
        cooldowns = PlayerCooldownTracker.decode(tag.getStringOr(COOLDOWNS_KEY, ""));
    }
```

- [ ] **Step 2: Block entity base for 1.21.6 to 26.3**

For 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3, create this file (license header first):

```java
package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockEntity;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Base block entity for wayside activators that every player may use once per cooldown.
 *
 * <p>1.21.6+: block entity data is read and written through ValueInput/ValueOutput.
 */
public abstract class CooldownActivatorBlockEntity extends CompatBlockEntity {
    private static final String COOLDOWNS_KEY = "PlayerCooldowns";

    private PlayerCooldownTracker cooldowns = new PlayerCooldownTracker();

    protected CooldownActivatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @return 0 if the activation was accepted and recorded, otherwise the ticks left
     */
    public long tryActivate(UUID player, long gameTime, long cooldownTicks) {
        long remaining = cooldowns.tryUse(player, gameTime, cooldownTicks);
        // Pruning can change the data even when the use is refused.
        setChanged();
        return remaining;
    }

    @Override
    public void saveData(ValueOutput output) {
        output.putString(COOLDOWNS_KEY, cooldowns.encode());
    }

    @Override
    public void loadData(ValueInput input) {
        cooldowns = PlayerCooldownTracker.decode(input.getStringOr(COOLDOWNS_KEY, ""));
    }
}
```

- [ ] **Step 3: Exclude the shared base in 1.21.5 to 26.3**

In each of the 10 `common/<v>/build.gradle` files that list `'ClockTowerTeleporterBlockEntity.java',` in `excludedFiles`, add on the next line:

```groovy
                'CooldownActivatorBlockEntity.java',
```

Verify: `grep -c "CooldownActivatorBlockEntity.java" common/*/build.gradle` shows 1 for exactly those 10 files.

- [ ] **Step 4: Block classes per era**

Start from the Task 4 file `common/1.21.1/.../SundialGnomonBlock.java` and apply the era table at the top of this plan. Every module gets a full copy. The differences are listed exactly:

Era A, 1.20.1:
- Remove the `MapCodec` import, the `CODEC` field and the `codec()` override.
- `getRenderShape` is `public RenderShape getRenderShape(BlockState state)`.
- Replace the `useWithoutItem` override with:

```java
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof SundialGnomonBlockEntity gnomon) {
            activate(serverLevel, pos, player, gnomon);
        }
        return InteractionResult.SUCCESS;
    }
```
  and add `import net.minecraft.world.InteractionHand;`.

Era B, 1.21.2 and 1.21.4: identical to the 1.21.1 file.

Era C, 1.21.5 to 1.21.8: 1.21.1 file with `MobEffects.MOVEMENT_SPEED` replaced by `MobEffects.SPEED`.

Era D, 1.21.9 to 1.21.11: era C plus `level.isClientSide` replaced by `level.isClientSide()`.

Era E, 26.1.2 and 26.2: era D plus:
- Both `player.displayClientMessage(X, true)` calls become `player.sendOverlayMessage(X)`.
- `long dayTime = level.getDayTime();` becomes `long dayTime = getDayTime(level);` with this helper and imports `net.minecraft.core.Holder`, `net.minecraft.core.registries.Registries`, `net.minecraft.world.clock.WorldClock`, `net.minecraft.world.clock.WorldClocks`:

```java
    // 26.1.2: Level#getDayTime() was removed in favor of the world clock system.
    // Same lookup as TimeDistortionEventHandler: the dimension's default clock, else the overworld clock.
    private static long getDayTime(ServerLevel level) {
        Holder<WorldClock> clock = level.dimensionType().defaultClock().orElseGet(() ->
            level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD));
        return level.clockManager().getInstance(clock).totalTicks();
    }
```

Era F, 26.3: era E minus the `MapCodec` import, `CODEC` field and `codec()` override (match `common/26.3/.../ClockTowerTeleporterBlock.java`, which has none).

After writing each file, diff it against that module's `ClockTowerTeleporterBlock.java` signatures (`grep -n "useWithoutItem\|public InteractionResult use\|codec()\|getRenderShape\|isClientSide" <file>`) to confirm the same era choices.

- [ ] **Step 5: Registration per module**

Copy the Task 4 `SUNDIAL_GNOMON` entries into each module's `ModBlocks.java` and `ModBlockEntities.java`, directly after `CLOCK_TOWER_TELEPORTER`, applying the registration rules at the top of this plan:

- `ModBlocks`, 1.20.1: as 1.21.1.
- `ModBlocks`, 1.21.2 to 1.21.10: add before the closing parenthesis of the properties chain:
  ```java
                .setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "sundial_gnomon")))
  ```
- `ModBlocks`, 1.21.11 to 26.3: same with `net.minecraft.resources.Identifier.fromNamespaceAndPath`.
- `ModBlockEntities`, 1.20.1: as 1.21.1 (`Builder.of(...).build(null)`).
- `ModBlockEntities`, 1.21.2 and later:
  ```java
    public static final RegistrySupplier<BlockEntityType<SundialGnomonBlockEntity>> SUNDIAL_GNOMON =
        BLOCK_ENTITIES.register(ModBlockEntityId.SUNDIAL_GNOMON.id(), () ->
            new BlockEntityType<>(
                SundialGnomonBlockEntity::new,
                Set.<Block>of(ModBlocks.SUNDIAL_GNOMON.get())
            )
        );
  ```

For each module, compare against how `CLOCK_TOWER_TELEPORTER` is written in the same file and match it, including `setId` placement and whether `strength` is set before or after `ofFullCopy`.

Add the `SundialGnomonTests` line to each module's `RegistryDrivenTestGenerator.java`.

- [ ] **Step 6: Build all modules**

Run (sandbox disabled): `./gradlew buildAll`
Expected: BUILD SUCCESSFUL for all 14 modules. On a compile error, fix the offending module using its `ClockTowerTeleporterBlock.java` as the reference. If `SoundEvents.AMETHYST_BLOCK_CHIME` turns out to be a `Holder` in some module, append `.value()` there only and add a one-line comment naming the version.

- [ ] **Step 7: Unit tests and GameTests**

Run (sandbox disabled), one at a time:
1. `./gradlew testAll`
   Expected: PASS for all modules.
2. `./gradlew gameTestAll`
   Expected: PASS. `sundial_gnomon_tracks_cooldown_per_player` and the `sundial_gnomon` registry consistency tests run on every Fabric and NeoForge configuration (Forge 1.20.1 has no GameTest integration).

- [ ] **Step 8: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/blocks/SundialGnomonBlock.java \
        common/*/src/main/java/com/chronodawn/blocks/CooldownActivatorBlockEntity.java \
        common/*/build.gradle \
        common/*/src/main/java/com/chronodawn/registry/ModBlocks.java \
        common/*/src/main/java/com/chronodawn/registry/ModBlockEntities.java \
        common/*/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java
git commit -m "feat: port the Sundial Gnomon to every supported Minecraft version"
```

Before committing, `git status --short` must show only files from Task 5.

---

### Task 6: Put the gnomon into the Old Sundial structure

**Files:**
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/old_sundial.nbt`
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java` (the `old_sundial` spec, line ~114)

**Interfaces:**
- Consumes: `ModBlockId.SUNDIAL_GNOMON`, `ModBlocks.SUNDIAL_GNOMON` (Task 4).

- [ ] **Step 1: Update the structure test first**

In `StructureTests.java`, change the `old_sundial` spec from expecting `iron_bars` to expecting the gnomon:

```java
            new StructureSpec("old_sundial", 3, 3, 3, List.of(
                new BlockRequirement(ModBlockId.SMOOTH_TEMPORAL_STONE.id(), ModBlocks.SMOOTH_TEMPORAL_STONE, 9),
                new BlockRequirement(ModBlockId.SUNDIAL_GNOMON.id(), ModBlocks.SUNDIAL_GNOMON, 1)
            )),
```

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1`
Expected: FAIL on the `old_sundial` structure test, expecting 1 `sundial_gnomon` and finding 0.

- [ ] **Step 3: Edit the NBT palette**

Confirm the editor is available: `python3 -c "import nbtlib"`. If it fails, use the project's local venv as described in memory `feedback_nbtlib_missing_venv_workaround`.

```bash
python3 - <<'EOF'
import nbtlib
path = 'common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/old_sundial.nbt'
f = nbtlib.load(path)
hits = [p for p in f['palette'] if p['Name'] == 'minecraft:iron_bars']
assert len(hits) == 1, f'expected one iron_bars palette entry, found {len(hits)}'
entry = hits[0]
entry['Name'] = nbtlib.String('chronodawn:sundial_gnomon')
if 'Properties' in entry:
    del entry['Properties']
f.save()
g = nbtlib.load(path)
print([str(p['Name']) for p in g['palette']])
EOF
```

Expected output lists `chronodawn:sundial_gnomon` and no `minecraft:iron_bars`. The file stays gzip-compressed (nbtlib keeps the original compression on `save()`). Verify with `python3 -c "import gzip;gzip.open('common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/old_sundial.nbt').read(10)"`.

- [ ] **Step 4: Run to verify it passes**

Run (sandbox disabled), one at a time:
1. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1`
   Expected: PASS, including the `old_sundial` structure test.
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1`
   Expected: PASS. The 1.20.1 copy comes from the `convertNbt` task, so this proves the conversion handles the new block. Forge 1.20.1 has no GameTest integration in this project.

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/old_sundial.nbt \
        common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java
git commit -m "feat: generate Old Sundials with an interactive gnomon"
```

---

### Task 7: Docs, changelog and full verification

**Files:**
- Modify: `CHANGELOG.md` (`## [Unreleased]` / `### Added`)
- Modify: `docs/player_guide.md` (the section that lists small surface features, or the Chrono Dawn dimension exploration section)

- [ ] **Step 1: Changelog**

Add under `## [Unreleased]` → `### Added`, keeping the bold-lead style of neighboring entries:

```markdown
- **Old Sundial blessings**: right-click the post of an Old Sundial to gain
  Speed in daylight, Speed II around noon, or Night Vision at night, for
  three minutes. Each player can use each sundial once per Minecraft day
  (about 20 minutes, counted in game time). Breaking the post drops Iron
  Bars. The cooldown and effect length are configurable under
  `[gameplay.wayside]`. Only Old Sundials in newly generated chunks have the
  interactive post.
```

- [ ] **Step 2: Player guide**

Find where Old Sundial or the small surface features are described: `grep -n -i "sundial\|monolith\|cairn" docs/player_guide.md`. If a description exists, extend it with the same facts as the changelog entry in the guide's tone. If none exists, add a short "Old Sundial" paragraph to the section about exploring the Chrono Dawn dimension. Use the `doc-maintenance` skill's checklist for any other file it says must stay consistent (for example the CurseForge and Modrinth descriptions only if they list structures).

- [ ] **Step 3: Full verification**

Run (sandbox disabled): `./gradlew checkAll`
Expected: every stage passes (cleanAll, validateResources, validateTranslations, buildAll, testAll, gameTestAll). Also run `./gradlew validateData` since `checkAll` does not include it.

No Mixin changed, so `prodSmokeTest` is not required.

- [ ] **Step 4: Manual check (user)**

Present to the user:
1. `./gradlew runClientNeoForge1_21_1` and `./gradlew runClientFabric1_21_1`
2. Create a new world, enter Chrono Dawn, and find a newly generated Old Sundial in plains, faded plains or prairies (`/locate` does not cover features, so explore or use `/place feature chronodawn:old_sundial`).
3. Right-click the post in daylight: Speed I for 3 minutes, particles along the shadow, chime, action-bar message.
4. `/time set 6000` and use a second sundial (or wait out the cooldown): Speed II.
5. `/time set 18000` and use another: Night Vision with glow particles.
6. Right-click the same sundial again: cooldown message with remaining minutes, no new effect.
7. Break the post in survival: Iron Bars drop.
Expected: all of the above on both loaders.

- [ ] **Step 5: Commit**

```bash
git add CHANGELOG.md docs/player_guide.md
git commit -m "docs: describe Old Sundial blessings"
```
