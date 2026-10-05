# Time Distortion Rebalance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Chrono Dawn ambient Slowness weaker by default, vary it with the dimension's time-flow speed (including a rare Speed I danger band with a bell cue), and add per-mob resistance tiers through entity type tags.

**Architecture:** All band, tier, and level rules live in a new pure-Java class `TimeDistortionPolicy` in `common/shared`, unit tested without Minecraft bootstrap. The Minecraft-facing `TimeDistortionEffect` (one shared copy plus two groups of version copies) asks the policy what to apply and replaces only its own ambient effect instances. The time-flow handler (shared plus three 26.x copies) asks the policy whether a speed change enters the Very fast band and plays a bell.

**Tech Stack:** Java 17/21/25, Architectury multi-version Gradle build, NightConfig TOML (`CommentedConfig`), JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-06-time-distortion-rebalance-design.md`

## Global Constraints

- New defaults: `normal_slowness_level = 3`, `enhanced_slowness_level = 4`, `link_to_time_flow = true`, `slow_below = 1.0`, `fast_above = 3.0`, `very_fast_above = 4.5`, `very_fast_speed_level = 1`.
- Ranges: slowness levels `1..=5`. Bounds `0.05..=20.0` and strictly increasing. `very_fast_speed_level` `0..=2`.
- A value equal to a bound belongs to the higher band.
- Slowness is clamped to at most V. A computed level below I means no Slowness.
- Resistant mobs take Slowness at level - 2 and Speed unchanged. Immune mobs take neither.
- Existing config files are never rewritten or migrated.
- Ambient instances: duration 100 ticks, `ambient = true`. Slowness `visible = false`, Speed `visible = true`.
- Cue: `SoundEvents.BELL_RESONATE`, `SoundSource.AMBIENT`, volume `0.6f`, pitch `0.8f`, only on entering Very fast.
- Docs, code, comments, and commit messages are English. Prose in docs and Chronicle text has no em dashes and no semicolons.
- Use Mojang names. Pre-1.21.5 effect constants are `MOVEMENT_SLOWDOWN` / `MOVEMENT_SPEED`. 1.21.5+ are `SLOWNESS` / `SPEED`. 1.21.11+ uses `identifier()` instead of `location()` on `ResourceKey`.
- Commits: GPG signing fails inside the sandbox. The controller (not a subagent) makes commits, outside the sandbox. Do not commit without the user's go-ahead for this plan's execution.
- Run Gradle for one version per invocation. Do not run two Gradle builds in the same checkout at the same time.

## Review Focus

- Existing install whose file has only `normal_slowness_level = 4` and `enhanced_slowness_level = 5` must keep 4 and 5 and get default band settings (Task 1 test `timeDistortion_legacyFile_keepsLevelsAndGetsBandDefaults`).
- `[gameplay.time_flow] enabled = false` with a custom `slow_below = 1.5` must still give the Normal band, not Slow (Task 2 test `band_timeFlowDisabled_isNormalEvenBelowSlowBound`).
- Non-ambient effects such as Resonance Reversal's Speed II (900 ticks, not ambient) must never be removed by band switching (Task 2 tests `replaceable_nonAmbient_isKept` and `replaceable_longAmbient_isKept`).
- `very_fast_speed_level = 0` must disable both Speed and the bell, so high speeds fall back to the Fast band (Task 2 tests `band_veryFastDisabled_fallsBackToFast` and `entersVeryFast_disabledLevel_neverTriggers`).
- A Resistant mob in the Very fast band must still get Speed I (Task 2 test `outcome_resistantVeryFast_getsUnchangedSpeed`).

---

## File Structure

| File | Responsibility |
|---|---|
| `common/shared/src/main/java/com/chronodawn/config/TimeDistortionSettings.java` | settings record, gains 5 band fields, loses `slownessAmplifier` in Task 4 |
| `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java` | new defaults |
| `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java` | parse and validate new keys |
| `common/shared/src/main/resources/chronodawn-default-config.toml` | bundled default file |
| `common/shared/src/main/java/com/chronodawn/core/time/TimeDistortionPolicy.java` | **new**, pure rules |
| `common/shared/src/test/java/com/chronodawn/unit/TimeDistortionPolicyTest.java` | **new**, policy tests |
| `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java` | config tests |
| `common/{1.20.1,1.21.5,1.21.11,26.1.2,26.2,26.3}/src/test/java/com/chronodawn/unit/TimeDistortionEffectTest.java` | constructor update only |
| `common/shared/src/main/java/com/chronodawn/tags/ModEntityTypeTags.java` | new tag key |
| `common/1.20.1/src/main/resources/data/chronodawn/tags/entity_types/time_distortion_{immune,resistant}.json` | 1.20.1 tags |
| `common/shared/src/main/resources/data/chronodawn/tags/entity_type/time_distortion_{immune,resistant}.json` | 1.21.1+ tags |
| `common/*/src/main/java/com/chronodawn/entities/mobs/ClockworkSentinelEntity.java` (14) | tag-aware override |
| `common/shared/.../core/time/TimeDistortionEffect.java` and 10 version copies | apply policy |
| `common/shared/.../events/TimeDistortionEventHandler.java` and 26.1.2, 26.2, 26.3 copies | bell cue |
| `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/progression/time_distortion.json` | Chronicle text |
| `docs/configuration.md`, `docs/player_guide.md`, `docs/curseforge_description.md`, `docs/modrinth_description.md`, `CHANGELOG.md` | docs |

Test command pattern (shared tests compile into every version module through `srcDir '../shared/src/test/java'`):

```bash
./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.<TestClass>'
```

---

### Task 1: Configuration keys and defaults

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/TimeDistortionSettings.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java:52-58`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java:95-99,120-121,414-456`
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml:287-297`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java:504-546`
- Modify: the six `TimeDistortionEffectTest.java` copies (constructor call only)

**Interfaces:**
- Produces: `TimeDistortionSettings(boolean enabled, int normalSlownessLevel, int enhancedSlownessLevel, Scope scope, boolean linkToTimeFlow, float slowBelow, float fastAbove, float veryFastAbove, int veryFastSpeedLevel)`. `slownessAmplifier(boolean)` stays in this task (version copies still call it) and is removed in Task 4.

- [ ] **Step 1: Update and add failing tests in `ConfigLoaderTest`**

Replace `timeDistortion_missingSection_fallsBackToDefaults` and `timeDistortion_validCustomValues_areReturnedVerbatim`, and add three tests after `timeDistortion_invalidFields_fallBackIndependently`. The expected default levels change from 4/5 to 3/4 because the spec changes the defaults on purpose, not to make a test pass.

```java
    @Test
    void timeDistortion_missingSection_fallsBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"), "schema_version = 1\n");

        TimeDistortionSettings settings = ConfigLoader.load(tmp).gameplay().timeDistortion();

        assertEquals(ConfigDefaults.TIME_DISTORTION_DEFAULTS, settings);
        assertEquals(new TimeDistortionSettings(
            true, 3, 4, TimeDistortionSettings.Scope.HOSTILE_MOBS,
            true, 1.0f, 3.0f, 4.5f, 1
        ), settings);
    }

    @Test
    void timeDistortion_validCustomValues_areReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.time_distortion]\n" +
            "enabled = false\n" +
            "normal_slowness_level = 2\n" +
            "enhanced_slowness_level = 3\n" +
            "scope = \"all_mobs\"\n" +
            "link_to_time_flow = false\n" +
            "slow_below = 0.8\n" +
            "fast_above = 2.0\n" +
            "very_fast_above = 6.0\n" +
            "very_fast_speed_level = 2\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(new TimeDistortionSettings(
            false, 2, 3, TimeDistortionSettings.Scope.ALL_MOBS,
            false, 0.8f, 2.0f, 6.0f, 2
        ), config.gameplay().timeDistortion());
    }

    @Test
    void timeDistortion_legacyFile_keepsLevelsAndGetsBandDefaults(@TempDir Path tmp) throws IOException {
        // A file written by an older release has the old explicit levels and no band keys.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.time_distortion]\n" +
            "enabled = true\n" +
            "normal_slowness_level = 4\n" +
            "enhanced_slowness_level = 5\n" +
            "scope = \"hostile_mobs\"\n");

        TimeDistortionSettings settings = ConfigLoader.load(tmp).gameplay().timeDistortion();

        assertEquals(new TimeDistortionSettings(
            true, 4, 5, TimeDistortionSettings.Scope.HOSTILE_MOBS,
            true, 1.0f, 3.0f, 4.5f, 1
        ), settings);
    }

    @Test
    void timeDistortion_nonIncreasingBounds_fallBackTogether(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.time_distortion]\n" +
            "normal_slowness_level = 2\n" +
            "slow_below = 2.0\n" +
            "fast_above = 2.0\n" +
            "very_fast_above = 5.0\n");

        TimeDistortionSettings settings = ConfigLoader.load(tmp).gameplay().timeDistortion();
        TimeDistortionSettings defaults = ConfigDefaults.TIME_DISTORTION_DEFAULTS;

        assertEquals(defaults.slowBelow(), settings.slowBelow());
        assertEquals(defaults.fastAbove(), settings.fastAbove());
        assertEquals(defaults.veryFastAbove(), settings.veryFastAbove());
        assertEquals(2, settings.normalSlownessLevel());
    }

    @Test
    void timeDistortion_outOfRangeBandValues_fallBack(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.time_distortion]\n" +
            "very_fast_above = 25.0\n" +
            "very_fast_speed_level = 3\n");

        TimeDistortionSettings settings = ConfigLoader.load(tmp).gameplay().timeDistortion();
        TimeDistortionSettings defaults = ConfigDefaults.TIME_DISTORTION_DEFAULTS;

        assertEquals(defaults.veryFastAbove(), settings.veryFastAbove());
        assertEquals(defaults.veryFastSpeedLevel(), settings.veryFastSpeedLevel());
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.ConfigLoaderTest'`
Expected: compilation FAIL (`TimeDistortionSettings` has no 9-argument constructor, no `slowBelow()`).

- [ ] **Step 3: Extend the record**

In `TimeDistortionSettings.java`, replace the record header and keep the `Scope` enum and `slownessAmplifier` as they are:

```java
/** Settings for the Chrono Dawn dimension's ambient Time Distortion effect. */
public record TimeDistortionSettings(
    boolean enabled,
    int normalSlownessLevel,
    int enhancedSlownessLevel,
    Scope scope,
    boolean linkToTimeFlow,
    float slowBelow,
    float fastAbove,
    float veryFastAbove,
    int veryFastSpeedLevel
) {
```

- [ ] **Step 4: Update defaults**

In `ConfigDefaults.java`, replace the `TIME_DISTORTION_DEFAULTS` block:

```java
    // Normal band Slowness III (IV after the Eye of Chronos). Existing config files keep
    // their written levels because the loader never rewrites them.
    public static final TimeDistortionSettings TIME_DISTORTION_DEFAULTS = new TimeDistortionSettings(
        true,
        3,
        4,
        TimeDistortionSettings.Scope.HOSTILE_MOBS,
        true,
        1.0f,
        3.0f,
        4.5f,
        1
    );
```

- [ ] **Step 5: Parse and validate the new keys**

In `ConfigLoader.java`, add after `K_TD_SCOPE`:

```java
    private static final String K_TD_LINK_TO_TIME_FLOW = "link_to_time_flow";
    private static final String K_TD_SLOW_BELOW = "slow_below";
    private static final String K_TD_FAST_ABOVE = "fast_above";
    private static final String K_TD_VERY_FAST_ABOVE = "very_fast_above";
    private static final String K_TD_VERY_FAST_SPEED_LEVEL = "very_fast_speed_level";
```

Add after `MAX_SLOWNESS_LEVEL`:

```java
    // 0 disables the Very fast band. Speed III and above makes melee mobs unavoidable.
    private static final int MIN_VERY_FAST_SPEED_LEVEL = 0;
    private static final int MAX_VERY_FAST_SPEED_LEVEL = 2;
```

In `parseTimeDistortion`, after the `scope` lookup, add:

```java
        boolean linkToTimeFlow = parsed.<Boolean>getOptional(path + "." + K_TD_LINK_TO_TIME_FLOW)
            .orElse(defaults.linkToTimeFlow());
        float slowBelow = parsed.<Number>getOptional(path + "." + K_TD_SLOW_BELOW)
            .map(Number::floatValue)
            .orElse(defaults.slowBelow());
        float fastAbove = parsed.<Number>getOptional(path + "." + K_TD_FAST_ABOVE)
            .map(Number::floatValue)
            .orElse(defaults.fastAbove());
        float veryFastAbove = parsed.<Number>getOptional(path + "." + K_TD_VERY_FAST_ABOVE)
            .map(Number::floatValue)
            .orElse(defaults.veryFastAbove());
        int veryFastSpeedLevel = parsed.<Number>getOptional(path + "." + K_TD_VERY_FAST_SPEED_LEVEL)
            .map(Number::intValue)
            .orElse(defaults.veryFastSpeedLevel());
```

After the existing scope validation, add:

```java
        // The three bounds only make sense together, so any violation resets all of them.
        if (slowBelow < MIN_TIME_SPEED || veryFastAbove > MAX_TIME_SPEED
            || slowBelow >= fastAbove || fastAbove >= veryFastAbove) {
            LOGGER.error(
                "Invalid {}.{{slow_below,fast_above,very_fast_above}} = ({}, {}, {}) "
                    + "(must satisfy {} <= slow_below < fast_above < very_fast_above <= {}); "
                    + "using defaults ({}, {}, {})",
                path, slowBelow, fastAbove, veryFastAbove, MIN_TIME_SPEED, MAX_TIME_SPEED,
                defaults.slowBelow(), defaults.fastAbove(), defaults.veryFastAbove()
            );
            slowBelow = defaults.slowBelow();
            fastAbove = defaults.fastAbove();
            veryFastAbove = defaults.veryFastAbove();
        }
        if (veryFastSpeedLevel < MIN_VERY_FAST_SPEED_LEVEL || veryFastSpeedLevel > MAX_VERY_FAST_SPEED_LEVEL) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_TD_VERY_FAST_SPEED_LEVEL, veryFastSpeedLevel,
                MIN_VERY_FAST_SPEED_LEVEL, MAX_VERY_FAST_SPEED_LEVEL, defaults.veryFastSpeedLevel()
            );
            veryFastSpeedLevel = defaults.veryFastSpeedLevel();
        }
```

Replace the return statement:

```java
        return new TimeDistortionSettings(
            enabled, normalLevel, enhancedLevel, scope,
            linkToTimeFlow, slowBelow, fastAbove, veryFastAbove, veryFastSpeedLevel
        );
```

`MIN_TIME_SPEED` and `MAX_TIME_SPEED` are declared below `MAX_SLOWNESS_LEVEL` already. Static finals are fine to reference from the method regardless of order.

- [ ] **Step 6: Update the bundled default file**

Replace lines 287-297 of `chronodawn-default-config.toml` (the `[gameplay.time_distortion]` section up to, not including, `[gameplay.time_flow]`):

```toml
[gameplay.time_distortion]
# Whether the Chrono Dawn dimension applies ambient Slowness to mobs.
enabled = true

# Slowness level in the Normal band, before and after the Eye of Chronos
# enhancement. Range: 1 to 5. Other bands are relative to this level.
normal_slowness_level = 3
enhanced_slowness_level = 4

# "hostile_mobs" affects only hostile mobs. "all_mobs" also affects passive
# and friendly mobs. Players and Chrono Dawn bosses are never affected.
scope = "hostile_mobs"

# Whether the effect follows the day/night cycle speed from [gameplay.time_flow].
# When false, or when time flow is disabled, the Normal band always applies.
link_to_time_flow = true

# Band bounds as day/night cycle speed multipliers. Must satisfy
# 0.05 <= slow_below < fast_above < very_fast_above <= 20.0.
#   below slow_below:                 Slowness, Normal level + 1
#   slow_below to fast_above:         Slowness, Normal level
#   fast_above to very_fast_above:    Slowness, Normal level - 1
#   very_fast_above and above:        Speed at very_fast_speed_level
slow_below = 1.0
fast_above = 3.0
very_fast_above = 4.5

# Speed level given to mobs while time races. Range: 0 to 2. 0 disables the
# Very fast band, so the Fast band applies instead and no bell sounds.
very_fast_speed_level = 1

```

- [ ] **Step 7: Update the six `TimeDistortionEffectTest` copies**

In each of `common/{1.20.1,1.21.5,1.21.11,26.1.2,26.2,26.3}/src/test/java/com/chronodawn/unit/TimeDistortionEffectTest.java`, replace:

```java
        TimeDistortionSettings disabled = new TimeDistortionSettings(
            false, 4, 5, TimeDistortionSettings.Scope.HOSTILE_MOBS
        );
```

with:

```java
        TimeDistortionSettings disabled = new TimeDistortionSettings(
            false, 3, 4, TimeDistortionSettings.Scope.HOSTILE_MOBS,
            true, 1.0f, 3.0f, 4.5f, 1
        );
```

Verify the six files stay identical: `find common -name TimeDistortionEffectTest.java -not -path '*/build/*' | xargs md5 -q | sort -u | wc -l` prints `1`.

- [ ] **Step 8: Run tests to verify they pass**

Run: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.ConfigLoaderTest'`
Expected: PASS. Also run `./gradlew :common-1.21.5:test -Ptarget_mc_version=1.21.5 --tests 'com.chronodawn.unit.TimeDistortionEffectTest'`. Expected: PASS.

- [ ] **Step 9: Commit (controller, with user go-ahead)**

```bash
git add common/shared/src/main/java/com/chronodawn/config common/shared/src/main/resources/chronodawn-default-config.toml common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java common/*/src/test/java/com/chronodawn/unit/TimeDistortionEffectTest.java
git commit -m "feat(config): add Time Distortion band settings and lower default Slowness"
```

---

### Task 2: `TimeDistortionPolicy`

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/core/time/TimeDistortionPolicy.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/TimeDistortionPolicyTest.java`

**Interfaces:**
- Consumes: `TimeDistortionSettings` accessors from Task 1.
- Produces:
  - `enum TimeDistortionPolicy.Band { SLOW, NORMAL, FAST, VERY_FAST }`
  - `enum TimeDistortionPolicy.Tier { IMMUNE, RESISTANT, NORMAL }`
  - `enum TimeDistortionPolicy.Kind { NONE, SLOWNESS, SPEED }`
  - `record TimeDistortionPolicy.Outcome(Kind kind, int level)` with `static final Outcome NONE` and `int amplifier()` (= `level - 1`)
  - `static Band band(boolean timeFlowEnabled, float timeSpeed, TimeDistortionSettings settings)`
  - `static boolean entersVeryFast(float previousSpeed, float nextSpeed, TimeDistortionSettings settings)`
  - `static Outcome outcome(Band band, boolean enhanced, Tier tier, TimeDistortionSettings settings)`
  - `static final int AMBIENT_DURATION_TICKS = 100`
  - `static boolean isReplaceableAmbient(boolean ambient, int remainingTicks, int currentAmplifier, int desiredAmplifier)`, where `desiredAmplifier` is `-1` for "no effect"

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

import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.TimeDistortionSettings;
import com.chronodawn.core.time.TimeDistortionPolicy;
import com.chronodawn.core.time.TimeDistortionPolicy.Band;
import com.chronodawn.core.time.TimeDistortionPolicy.Kind;
import com.chronodawn.core.time.TimeDistortionPolicy.Outcome;
import com.chronodawn.core.time.TimeDistortionPolicy.Tier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeDistortionPolicyTest {

    private static final TimeDistortionSettings DEFAULTS = ConfigDefaults.TIME_DISTORTION_DEFAULTS;

    private static TimeDistortionSettings with(
        int normal, int enhanced, boolean link, float slowBelow, float fastAbove, float veryFastAbove, int speedLevel
    ) {
        return new TimeDistortionSettings(
            true, normal, enhanced, TimeDistortionSettings.Scope.HOSTILE_MOBS,
            link, slowBelow, fastAbove, veryFastAbove, speedLevel
        );
    }

    private static Outcome slowness(int level) {
        return new Outcome(Kind.SLOWNESS, level);
    }

    private static Outcome speed(int level) {
        return new Outcome(Kind.SPEED, level);
    }

    @Test
    void band_defaultBounds_mapEachRange() {
        assertEquals(Band.SLOW, TimeDistortionPolicy.band(true, 0.67f, DEFAULTS));
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 2.0f, DEFAULTS));
        assertEquals(Band.FAST, TimeDistortionPolicy.band(true, 4.0f, DEFAULTS));
        assertEquals(Band.VERY_FAST, TimeDistortionPolicy.band(true, 5.0f, DEFAULTS));
    }

    @Test
    void band_boundaryValues_belongToHigherBand() {
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 1.0f, DEFAULTS));
        assertEquals(Band.FAST, TimeDistortionPolicy.band(true, 3.0f, DEFAULTS));
        assertEquals(Band.VERY_FAST, TimeDistortionPolicy.band(true, 4.5f, DEFAULTS));
    }

    @Test
    void band_linkDisabled_isAlwaysNormal() {
        TimeDistortionSettings unlinked = with(3, 4, false, 1.0f, 3.0f, 4.5f, 1);
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 0.5f, unlinked));
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(true, 10.0f, unlinked));
    }

    @Test
    void band_timeFlowDisabled_isNormalEvenBelowSlowBound() {
        TimeDistortionSettings highSlowBound = with(3, 4, true, 1.5f, 3.0f, 4.5f, 1);
        assertEquals(Band.NORMAL, TimeDistortionPolicy.band(false, 1.0f, highSlowBound));
    }

    @Test
    void band_veryFastDisabled_fallsBackToFast() {
        TimeDistortionSettings noSpeed = with(3, 4, true, 1.0f, 3.0f, 4.5f, 0);
        assertEquals(Band.FAST, TimeDistortionPolicy.band(true, 5.0f, noSpeed));
    }

    @Test
    void outcome_defaultsBeforeEnhancement() {
        assertEquals(slowness(4), TimeDistortionPolicy.outcome(Band.SLOW, false, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(3), TimeDistortionPolicy.outcome(Band.NORMAL, false, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(2), TimeDistortionPolicy.outcome(Band.FAST, false, Tier.NORMAL, DEFAULTS));
        assertEquals(speed(1), TimeDistortionPolicy.outcome(Band.VERY_FAST, false, Tier.NORMAL, DEFAULTS));
    }

    @Test
    void outcome_defaultsAfterEnhancement() {
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.SLOW, true, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(4), TimeDistortionPolicy.outcome(Band.NORMAL, true, Tier.NORMAL, DEFAULTS));
        assertEquals(slowness(3), TimeDistortionPolicy.outcome(Band.FAST, true, Tier.NORMAL, DEFAULTS));
        assertEquals(speed(1), TimeDistortionPolicy.outcome(Band.VERY_FAST, true, Tier.NORMAL, DEFAULTS));
    }

    @Test
    void outcome_legacyLevels_clampAtFive() {
        TimeDistortionSettings legacy = with(4, 5, true, 1.0f, 3.0f, 4.5f, 1);
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.SLOW, false, Tier.NORMAL, legacy));
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.SLOW, true, Tier.NORMAL, legacy));
        assertEquals(slowness(5), TimeDistortionPolicy.outcome(Band.NORMAL, true, Tier.NORMAL, legacy));
    }

    @Test
    void outcome_levelBelowOne_isNone() {
        TimeDistortionSettings weakest = with(1, 1, true, 1.0f, 3.0f, 4.5f, 1);
        assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(Band.FAST, false, Tier.NORMAL, weakest));
    }

    @Test
    void outcome_resistant_dropsTwoSlownessLevels() {
        assertEquals(slowness(2), TimeDistortionPolicy.outcome(Band.SLOW, false, Tier.RESISTANT, DEFAULTS));
        assertEquals(slowness(1), TimeDistortionPolicy.outcome(Band.NORMAL, false, Tier.RESISTANT, DEFAULTS));
        assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(Band.FAST, false, Tier.RESISTANT, DEFAULTS));
        assertEquals(slowness(2), TimeDistortionPolicy.outcome(Band.NORMAL, true, Tier.RESISTANT, DEFAULTS));
    }

    @Test
    void outcome_resistantVeryFast_getsUnchangedSpeed() {
        assertEquals(speed(1), TimeDistortionPolicy.outcome(Band.VERY_FAST, false, Tier.RESISTANT, DEFAULTS));
    }

    @Test
    void outcome_immune_isAlwaysNone() {
        for (Band band : Band.values()) {
            assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(band, false, Tier.IMMUNE, DEFAULTS));
            assertEquals(Outcome.NONE, TimeDistortionPolicy.outcome(band, true, Tier.IMMUNE, DEFAULTS));
        }
    }

    @Test
    void outcome_amplifierIsLevelMinusOne() {
        assertEquals(2, slowness(3).amplifier());
        assertEquals(0, speed(1).amplifier());
    }

    @Test
    void entersVeryFast_onlyOnTransitionIntoBand() {
        assertTrue(TimeDistortionPolicy.entersVeryFast(2.0f, 4.8f, DEFAULTS));
        assertFalse(TimeDistortionPolicy.entersVeryFast(4.6f, 4.8f, DEFAULTS));
        assertFalse(TimeDistortionPolicy.entersVeryFast(4.8f, 2.0f, DEFAULTS));
        assertFalse(TimeDistortionPolicy.entersVeryFast(2.0f, 3.5f, DEFAULTS));
    }

    @Test
    void entersVeryFast_disabledLevel_neverTriggers() {
        TimeDistortionSettings noSpeed = with(3, 4, true, 1.0f, 3.0f, 4.5f, 0);
        assertFalse(TimeDistortionPolicy.entersVeryFast(2.0f, 5.0f, noSpeed));
    }

    @Test
    void entersVeryFast_linkDisabled_neverTriggers() {
        TimeDistortionSettings unlinked = with(3, 4, false, 1.0f, 3.0f, 4.5f, 1);
        assertFalse(TimeDistortionPolicy.entersVeryFast(2.0f, 5.0f, unlinked));
    }

    @Test
    void replaceable_ownStaleInstance_isRemoved() {
        assertTrue(TimeDistortionPolicy.isReplaceableAmbient(true, 100, 3, 2));
        assertTrue(TimeDistortionPolicy.isReplaceableAmbient(true, 40, 2, -1));
    }

    @Test
    void replaceable_sameAmplifier_isKept() {
        assertFalse(TimeDistortionPolicy.isReplaceableAmbient(true, 100, 2, 2));
    }

    @Test
    void replaceable_nonAmbient_isKept() {
        // Resonance Reversal's Speed II near its end: short remaining time but not ambient.
        assertFalse(TimeDistortionPolicy.isReplaceableAmbient(false, 50, 1, 0));
    }

    @Test
    void replaceable_longAmbient_isKept() {
        assertFalse(TimeDistortionPolicy.isReplaceableAmbient(true, 101, 1, 0));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.TimeDistortionPolicyTest'`
Expected: compilation FAIL, `TimeDistortionPolicy` not found.

- [ ] **Step 3: Implement the policy**

Create `common/shared/src/main/java/com/chronodawn/core/time/TimeDistortionPolicy.java` with the LGPL header (same as the test file) and:

```java
package com.chronodawn.core.time;

import com.chronodawn.config.TimeDistortionSettings;

/**
 * Rules for the ambient Time Distortion effect, free of Minecraft types so every
 * version module shares one implementation and it can be tested without bootstrap.
 */
public final class TimeDistortionPolicy {
    /** Ambient instances last this long and are reapplied every tick. */
    public static final int AMBIENT_DURATION_TICKS = 100;

    private static final int RESISTANT_LEVEL_REDUCTION = 2;
    private static final int MAX_SLOWNESS_LEVEL = 5;

    public enum Band { SLOW, NORMAL, FAST, VERY_FAST }

    public enum Tier { IMMUNE, RESISTANT, NORMAL }

    public enum Kind { NONE, SLOWNESS, SPEED }

    /** The effect to apply. {@code level} is the player-visible potion level. */
    public record Outcome(Kind kind, int level) {
        public static final Outcome NONE = new Outcome(Kind.NONE, 0);

        public int amplifier() {
            return level - 1;
        }
    }

    private TimeDistortionPolicy() {
    }

    public static Band band(boolean timeFlowEnabled, float timeSpeed, TimeDistortionSettings settings) {
        if (!timeFlowEnabled || !settings.linkToTimeFlow()) {
            return Band.NORMAL;
        }
        if (timeSpeed < settings.slowBelow()) {
            return Band.SLOW;
        }
        if (timeSpeed < settings.fastAbove()) {
            return Band.NORMAL;
        }
        // A disabled Very fast band falls back to Fast, which also keeps the bell silent.
        if (timeSpeed < settings.veryFastAbove() || settings.veryFastSpeedLevel() == 0) {
            return Band.FAST;
        }
        return Band.VERY_FAST;
    }

    /** Called only when time flow is running, so the flow is treated as enabled. */
    public static boolean entersVeryFast(float previousSpeed, float nextSpeed, TimeDistortionSettings settings) {
        return band(true, previousSpeed, settings) != Band.VERY_FAST
            && band(true, nextSpeed, settings) == Band.VERY_FAST;
    }

    public static Outcome outcome(Band band, boolean enhanced, Tier tier, TimeDistortionSettings settings) {
        if (tier == Tier.IMMUNE) {
            return Outcome.NONE;
        }
        if (band == Band.VERY_FAST) {
            return new Outcome(Kind.SPEED, settings.veryFastSpeedLevel());
        }
        int base = enhanced ? settings.enhancedSlownessLevel() : settings.normalSlownessLevel();
        int level = switch (band) {
            case SLOW -> base + 1;
            case FAST -> base - 1;
            default -> base;
        };
        if (tier == Tier.RESISTANT) {
            level -= RESISTANT_LEVEL_REDUCTION;
        }
        level = Math.min(level, MAX_SLOWNESS_LEVEL);
        return level < 1 ? Outcome.NONE : new Outcome(Kind.SLOWNESS, level);
    }

    /**
     * Whether an active instance is a stale one this system applied and must be removed
     * before applying {@code desiredAmplifier} ({@code -1} for none). Vanilla keeps a
     * stronger instance over a weaker one, so without removal a band change would lag by
     * up to {@link #AMBIENT_DURATION_TICKS}. Non-ambient or longer effects come from other
     * sources, such as Resonance Reversal or potions, and are left alone.
     */
    public static boolean isReplaceableAmbient(
        boolean ambient, int remainingTicks, int currentAmplifier, int desiredAmplifier
    ) {
        return ambient && remainingTicks <= AMBIENT_DURATION_TICKS && currentAmplifier != desiredAmplifier;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.TimeDistortionPolicyTest'`
Expected: PASS (20 tests). Then run the same on 1.20.1 (Java 17): `./gradlew :common-1.20.1:test -Ptarget_mc_version=1.20.1 --tests 'com.chronodawn.unit.TimeDistortionPolicyTest'`. Expected: PASS.

- [ ] **Step 5: Commit (controller, with user go-ahead)**

```bash
git add common/shared/src/main/java/com/chronodawn/core/time/TimeDistortionPolicy.java common/shared/src/test/java/com/chronodawn/unit/TimeDistortionPolicyTest.java
git commit -m "feat(time): add Time Distortion band and tier policy"
```

---

### Task 3: Resistance tags and Sentinel override

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/tags/ModEntityTypeTags.java`
- Modify: `common/1.20.1/src/main/resources/data/chronodawn/tags/entity_types/time_distortion_immune.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/tags/entity_types/time_distortion_resistant.json`
- Modify: `common/shared/src/main/resources/data/chronodawn/tags/entity_type/time_distortion_immune.json`
- Create: `common/shared/src/main/resources/data/chronodawn/tags/entity_type/time_distortion_resistant.json`
- Modify: `common/{1.20.1,1.21.1,1.21.2,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11,26.1.2,26.2,26.3}/src/main/java/com/chronodawn/entities/mobs/ClockworkSentinelEntity.java`

**Interfaces:**
- Produces: `ModEntityTypeTags.TIME_DISTORTION_RESISTANT` (`TagKey<EntityType<?>>`).

- [ ] **Step 1: Add the tag key**

In `ModEntityTypeTags.java`, replace the existing constant line with:

```java
    /**
     * Mobs that ignore ambient Time Distortion, both Slowness and Speed. For bosses this
     * covers only the ambient effect. Clockwork Sentinel also rejects Slowness from every
     * other source while it is in this tag (see {@code ClockworkSentinelEntity#canBeAffected}).
     */
    public static final TagKey<EntityType<?>> TIME_DISTORTION_IMMUNE = entityTypeTag("time_distortion_immune");
    /** Mobs that take ambient Slowness two levels weaker. Speed applies unchanged. */
    public static final TagKey<EntityType<?>> TIME_DISTORTION_RESISTANT = entityTypeTag("time_distortion_resistant");
```

- [ ] **Step 2: Update tag JSON (both directories, identical content)**

`time_distortion_immune.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_guardian",
    "chronodawn:time_tyrant",
    "chronodawn:chronos_warden",
    "chronodawn:clockwork_colossus",
    "chronodawn:entropy_keeper",
    "chronodawn:temporal_phantom",
    "chronodawn:clockwork_sentinel"
  ]
}
```

`time_distortion_resistant.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_wraith",
    "chronodawn:timeline_strider",
    "chronodawn:forgotten_minute"
  ]
}
```

Confirm the entity IDs exist: `grep -hoE '"(clockwork_sentinel|temporal_wraith|timeline_strider|forgotten_minute)"' common/shared/src/main/java/com/chronodawn/registry/ModEntityId.java | sort -u` must print all four.

- [ ] **Step 3: Make the Sentinel override tag-aware**

In each of the 14 `ClockworkSentinelEntity.java` files, replace the javadoc and method from `* Override canBeAffected to grant immunity` through the end of `canBeAffected` with the block below. Use `MOVEMENT_SLOWDOWN` in 1.20.1, 1.21.1, 1.21.2, and 1.21.4, and `SLOWNESS` in 1.21.5 and later (keep whichever constant the file already uses).

```java
     * Rejects Slowness from every source while this type is in
     * {@code chronodawn:time_distortion_immune}, so the Sentinel keeps full speed in a
     * slowed dimension. Removing it from the tag through a datapack restores normal
     * Slowness, ambient or otherwise.
     */
    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        if (effect.getEffect() == net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN
            && this.getType().is(com.chronodawn.tags.ModEntityTypeTags.TIME_DISTORTION_IMMUNE)) {
            return false;
        }

        return super.canBeAffected(effect);
    }
```

Keep the javadoc opening `/**` line that precedes the replaced text. After editing, check that the override bodies fall into exactly two groups:
`find common -name ClockworkSentinelEntity.java -not -path '*/build/*' | xargs -I{} sh -c "sed -n '/canBeAffected/,/^    }/p' {} | md5 -q" | sort | uniq -c` prints two lines with counts 4 and 10.

- [ ] **Step 4: Validate resources and compile both eras**

Run: `./gradlew validateResources`, then `./gradlew validateData`. Expected: PASS.
Run: `./gradlew build1_21_1`, then `./gradlew build1_21_5`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit (controller, with user go-ahead)**

```bash
git add common/shared/src/main/java/com/chronodawn/tags/ModEntityTypeTags.java common/1.20.1/src/main/resources/data/chronodawn/tags/entity_types common/shared/src/main/resources/data/chronodawn/tags/entity_type common/*/src/main/java/com/chronodawn/entities/mobs/ClockworkSentinelEntity.java
git commit -m "feat(entities): add Time Distortion resistance tiers through entity tags"
```

---

### Task 4: Apply the policy in `TimeDistortionEffect`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/core/time/TimeDistortionEffect.java` (used by 1.20.1, 1.21.1, 1.21.2, 1.21.4)
- Modify: `common/{1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10}/src/main/java/com/chronodawn/core/time/TimeDistortionEffect.java` (group B)
- Modify: `common/{1.21.11,26.1.2,26.2,26.3}/src/main/java/com/chronodawn/core/time/TimeDistortionEffect.java` (group C)
- Modify: `common/shared/src/main/java/com/chronodawn/config/TimeDistortionSettings.java` (remove `slownessAmplifier`)

**Interfaces:**
- Consumes: `TimeDistortionPolicy` (Task 2), `ModEntityTypeTags.TIME_DISTORTION_RESISTANT` (Task 3), `TimeDistortionEventHandler.getCurrentSpeed(ServerLevel)` (existing, returns `1.0f` when unset), `ChronoDawnConfig.get().gameplay().timeFlow().enabled()`.
- Produces: unchanged public entry point `TimeDistortionEffect.applyTimeDistortion(LivingEntity)`.

- [ ] **Step 1: Rewrite the shared copy**

Replace the whole file body after the package line with (keep the existing imports block replaced by this one):

```java
package com.chronodawn.core.time;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.TimeDistortionSettings;
import com.chronodawn.core.time.TimeDistortionPolicy.Kind;
import com.chronodawn.core.time.TimeDistortionPolicy.Outcome;
import com.chronodawn.core.time.TimeDistortionPolicy.Tier;
import com.chronodawn.data.DimensionStateData;
import com.chronodawn.events.TimeDistortionEventHandler;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.tags.ModEntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/**
 * Applies the ambient Time Distortion effect to mobs in the Chrono Dawn dimension.
 *
 * <p>Which effect and level apply is decided by {@link TimeDistortionPolicy} from the
 * current day/night cycle speed, the Eye of Chronos enhancement, and the mob's tier
 * ({@code time_distortion_immune} / {@code time_distortion_resistant} tags). Instances
 * are ambient and short, and are reapplied every tick.
 */
public class TimeDistortionEffect {
    private static final int NO_EFFECT = -1;

    public static void applyTimeDistortion(LivingEntity entity) {
        TimeDistortionSettings settings = ChronoDawnConfig.get().gameplay().timeDistortion();
        if (!settings.enabled()) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!isInChronoDawnDimension(entity) || !isAffectedMob(entity, settings.scope())) {
            return;
        }
        Tier tier = tierOf(entity);
        if (tier == Tier.IMMUNE) {
            return;
        }

        boolean timeFlowEnabled = ChronoDawnConfig.get().gameplay().timeFlow().enabled();
        float timeSpeed = TimeDistortionEventHandler.getCurrentSpeed(serverLevel);
        Outcome outcome = TimeDistortionPolicy.outcome(
            TimeDistortionPolicy.band(timeFlowEnabled, timeSpeed, settings),
            isDimensionEnhanced(serverLevel),
            tier,
            settings
        );

        syncSlowness(entity, outcome.kind() == Kind.SLOWNESS ? outcome.amplifier() : NO_EFFECT);
        syncSpeed(entity, outcome.kind() == Kind.SPEED ? outcome.amplifier() : NO_EFFECT);
    }

    // Slowness and Speed need separate methods because the effect handle type differs
    // between Minecraft eras (MobEffect vs Holder<MobEffect>) and this file is shared.
    private static void syncSlowness(LivingEntity entity, int amplifier) {
        MobEffectInstance current = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (current != null && TimeDistortionPolicy.isReplaceableAmbient(
            current.isAmbient(), current.getDuration(), current.getAmplifier(), amplifier)) {
            entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        if (amplifier != NO_EFFECT) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, TimeDistortionPolicy.AMBIENT_DURATION_TICKS, amplifier, true, false
            ));
        }
    }

    private static void syncSpeed(LivingEntity entity, int amplifier) {
        MobEffectInstance current = entity.getEffect(MobEffects.MOVEMENT_SPEED);
        if (current != null && TimeDistortionPolicy.isReplaceableAmbient(
            current.isAmbient(), current.getDuration(), current.getAmplifier(), amplifier)) {
            entity.removeEffect(MobEffects.MOVEMENT_SPEED);
        }
        if (amplifier != NO_EFFECT) {
            // Visible particles are one of the cues that mobs have sped up.
            entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, TimeDistortionPolicy.AMBIENT_DURATION_TICKS, amplifier, true, true
            ));
        }
    }

    private static Tier tierOf(LivingEntity entity) {
        if (entity.getType().is(ModEntityTypeTags.TIME_DISTORTION_IMMUNE)) {
            return Tier.IMMUNE;
        }
        if (entity.getType().is(ModEntityTypeTags.TIME_DISTORTION_RESISTANT)) {
            return Tier.RESISTANT;
        }
        return Tier.NORMAL;
    }

    private static boolean isInChronoDawnDimension(LivingEntity entity) {
        return entity.level().dimension().location().equals(ModDimensions.CHRONO_DAWN_DIMENSION.location());
    }

    private static boolean isAffectedMob(LivingEntity entity, TimeDistortionSettings.Scope scope) {
        if (entity instanceof Player) {
            return false;
        }
        return scope == TimeDistortionSettings.Scope.HOSTILE_MOBS
            ? entity instanceof Monster
            : entity instanceof net.minecraft.world.entity.Mob;
    }

    private static boolean isDimensionEnhanced(ServerLevel level) {
        return DimensionStateData.get(level).getTimeDistortionLevel()
            == DimensionStateData.TimeDistortionLevel.SLOWNESS_V;
    }
}
```

Before replacing, check the current import list of the shared file. If it imports anything this version does not use (for example `ChronoDawn`, `FloqEntity`, `TimeKeeperEntity`, `EnhancedClockstoneArmorItem`, `ModItems`), drop them. If the file has a license header above `package`, keep it.

- [ ] **Step 2: Rewrite group B and group C copies**

Write the same file content to each group B and group C copy, then apply these exact substitutions:

| Token in shared copy | Group B (1.21.5 to 1.21.10) | Group C (1.21.11, 26.1.2, 26.2, 26.3) |
|---|---|---|
| `MobEffects.MOVEMENT_SLOWDOWN` | `MobEffects.SLOWNESS` | `MobEffects.SLOWNESS` |
| `MobEffects.MOVEMENT_SPEED` | `MobEffects.SPEED` | `MobEffects.SPEED` |
| `.dimension().location().equals(ModDimensions.CHRONO_DAWN_DIMENSION.location())` | unchanged | `.dimension().identifier().equals(ModDimensions.CHRONO_DAWN_DIMENSION.identifier())` |

Also check each group's current imports for a version-specific package (for example `Monster` moved packages in some versions). Keep whatever import path the existing copy uses for `Monster`, `Player`, `Mob`, and `DimensionStateData`. Then verify the groups:
`for f in $(find common -name TimeDistortionEffect.java -not -path '*/build/*'); do md5 -q $f; done | sort | uniq -c` prints three lines with counts 1, 6, 4.

- [ ] **Step 3: Remove `slownessAmplifier`**

Delete the `slownessAmplifier` method and its javadoc from `TimeDistortionSettings.java`. Confirm no callers remain: `grep -rn slownessAmplifier common --include='*.java'` (quote the glob) prints nothing outside `build/`.

- [ ] **Step 4: Compile and test all three groups**

Run each in turn (one Gradle invocation at a time):
- `./gradlew build1_21_1`
- `./gradlew build1_21_5`
- `./gradlew build26_3`
- `./gradlew :common-1.21.5:test -Ptarget_mc_version=1.21.5 --tests 'com.chronodawn.unit.TimeDistortionEffectTest'`

Expected: BUILD SUCCESSFUL and PASS. `TimeDistortionEffectTest` still passes because the disabled check returns before touching the entity.

- [ ] **Step 5: Commit (controller, with user go-ahead)**

```bash
git add common/shared/src/main/java/com/chronodawn/core/time/TimeDistortionEffect.java common/*/src/main/java/com/chronodawn/core/time/TimeDistortionEffect.java common/shared/src/main/java/com/chronodawn/config/TimeDistortionSettings.java
git commit -m "feat(time): vary Time Distortion with time flow speed and mob tier

Also make 1.21.5+ honor the time_distortion_immune tag instead of a
hardcoded boss list."
```

---

### Task 5: Bell cue and Chronicle text

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/events/TimeDistortionEventHandler.java:150-158`
- Modify: `common/{26.1.2,26.2,26.3}/src/main/java/com/chronodawn/events/TimeDistortionEventHandler.java:175-183`
- Modify: `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/progression/time_distortion.json`

**Interfaces:**
- Consumes: `TimeDistortionPolicy.entersVeryFast(float, float, TimeDistortionSettings)` (Task 2).

- [ ] **Step 1: Add the cue to all four handler copies**

Add imports (alphabetical placement next to existing ones):

```java
import com.chronodawn.config.TimeDistortionSettings;
import com.chronodawn.core.time.TimeDistortionPolicy;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
```

Add constants after `TIME_SYNC_INTERVAL`:

```java
    // A distant bell is the only hint that mobs just sped up. Players who read the
    // Chronicle learn to take cover, others just hear ambience.
    private static final float VERY_FAST_CUE_VOLUME = 0.6f;
    private static final float VERY_FAST_CUE_PITCH = 0.8f;
```

Inside the `if (timeFlow.enabled() && timeUntilChange <= 0) {` block, insert right after the `int newDuration = ...` line:

```java
            TimeDistortionSettings distortion = ChronoDawnConfig.get().gameplay().timeDistortion();
            if (distortion.enabled() && TimeDistortionPolicy.entersVeryFast(currentSpeed, newSpeed, distortion)) {
                playVeryFastCue(level);
            }
```

Add the method after `syncTimeToClients`:

```java
    private static void playVeryFastCue(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            player.playNotifySound(
                SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, VERY_FAST_CUE_VOLUME, VERY_FAST_CUE_PITCH
            );
        }
    }
```

- [ ] **Step 2: Compile both eras**

Run: `./gradlew build1_20_1`, then `./gradlew build26_3`.
Expected: BUILD SUCCESSFUL. If either fails because `ServerPlayer.playNotifySound` or `SoundEvents.BELL_RESONATE` is missing or has a different signature in that version, replace the loop body in that copy only with the following and note the change in the commit message:

```java
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, VERY_FAST_CUE_VOLUME, VERY_FAST_CUE_PITCH);
```

- [ ] **Step 3: Rewrite the Chronicle entry**

Replace the `pages` array in `time_distortion.json` with:

```json
  "pages": [
    {
      "text": {
        "en_us": "Time in Chrono Dawn is distorted. Hostile mobs move slower than they should, while you keep your normal speed.\n\nThe strength follows the flow of time:\n• When time runs slowly, mobs are slowed the most\n• When time races, mobs are barely slowed\n• Some creatures resist the distortion, and a few ignore it\n\nObtaining the Eye of Chronos deepens the distortion.",
        "ja_jp": "クロノドーンでは時間が歪んでいます。敵対的なモブは本来より遅く動き、あなたは通常の速さで動けます。\n\n歪みの強さは時の流れに従います:\n• 時の流れが遅いとき、モブは最も遅くなる\n• 時の流れが速いとき、モブはあまり遅くならない\n• 歪みに抵抗する生物や、まったく影響を受けない生物もいる\n\nクロノスの瞳を入手すると、歪みはさらに深まります。"
      }
    },
    {
      "text": {
        "en_us": "When Time Races:\n\nSometimes time rushes so fast that the distortion flips, and mobs move faster than normal. Watch for the signs:\n• A distant bell tolls\n• The sun and moon race across the sky\n• Mobs trail swirling particles\n\nWhen you notice them, find shelter or fight with care until time slows down again.",
        "ja_jp": "時が駆けるとき:\n\n時の流れが速くなりすぎると歪みが反転し、モブが通常より素早く動くことがあります。兆候に注意しましょう:\n• 遠くで鐘が鳴る\n• 太陽と月が空を駆け抜ける\n• モブの周りに渦巻く粒子が見える\n\n兆候に気づいたら、時の流れが落ち着くまで身を隠すか、慎重に戦いましょう。"
      }
    }
  ]
```

Edit the file line-based (replace only the `pages` array text). Do not round-trip it through a JSON serializer.

- [ ] **Step 4: Validate**

Run: `./gradlew validateResources`. Expected: PASS. Check the JSON parses: `python3 -m json.tool common/shared/src/main/resources/assets/chronodawn/chronicle/entries/progression/time_distortion.json > /dev/null`.

- [ ] **Step 5: Commit (controller, with user go-ahead)**

```bash
git add common/shared/src/main/java/com/chronodawn/events/TimeDistortionEventHandler.java common/*/src/main/java/com/chronodawn/events/TimeDistortionEventHandler.java common/shared/src/main/resources/assets/chronodawn/chronicle/entries/progression/time_distortion.json
git commit -m "feat(time): ring a distant bell when time races and mobs speed up"
```

---

### Task 6: Documentation

**Files:**
- Modify: `docs/configuration.md:392-430`
- Modify: `docs/player_guide.md:39,194,1089,1225`
- Modify: `docs/curseforge_description.md:153`, `docs/modrinth_description.md:152`
- Modify: `CHANGELOG.md` (Unreleased)

Read the `human-tone` skill before writing. No em dashes, no semicolons.

- [ ] **Step 1: `docs/configuration.md`**

Replace the section from `### \`[gameplay.time_distortion]\`` up to (not including) the `---` before `### \`[gameplay.time_flow]\`` with:

````markdown
### `[gameplay.time_distortion]`

Controls the ambient effect on mobs in the Chrono Dawn dimension. The effect
is applied to eligible mobs every tick. Setting `enabled = false` stops new
applications. Players and Chrono Dawn bosses are never affected.

The effect follows the day/night cycle speed from `[gameplay.time_flow]`. The
current speed falls into one of four bands:

| Band | Speed multiplier | Effect |
| --- | --- | --- |
| Slow | below `slow_below` | Slowness, Normal level + 1 (at most V) |
| Normal | `slow_below` to `fast_above` | Slowness at the Normal level |
| Fast | `fast_above` to `very_fast_above` | Slowness, Normal level - 1 (none below I) |
| Very fast | `very_fast_above` and above | Speed at `very_fast_speed_level` |

The Normal level is `normal_slowness_level`, or `enhanced_slowness_level`
after the Eye of Chronos enhancement. A speed equal to a bound belongs to the
higher band. A distant bell sounds for players in the dimension when the speed
enters the Very fast band.

Mobs in the `chronodawn:time_distortion_immune` entity type tag are not
affected. Mobs in `chronodawn:time_distortion_resistant` take Slowness two
levels weaker and Speed unchanged. Clockwork Sentinel also rejects Slowness
from any other source while it is in the immune tag.

```toml
[gameplay.time_distortion]
enabled = true
normal_slowness_level = 3
enhanced_slowness_level = 4
scope = "hostile_mobs"
link_to_time_flow = true
slow_below = 1.0
fast_above = 3.0
very_fast_above = 4.5
very_fast_speed_level = 1
```

| Field | Type | Default | Range | Notes |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | `true` | — | Turns the ambient effect on or off. |
| `normal_slowness_level` | integer | `3` | `1..=5` | Normal band Slowness level before the Eye of Chronos enhancement. |
| `enhanced_slowness_level` | integer | `4` | `1..=5` | Normal band Slowness level after the Eye of Chronos enhancement. |
| `scope` | string | `"hostile_mobs"` | `"hostile_mobs"`, `"all_mobs"` | `all_mobs` also affects passive and friendly mobs, including Speed in the Very fast band. Players and Chrono Dawn bosses remain excluded. |
| `link_to_time_flow` | boolean | `true` | — | When `false`, or when `[gameplay.time_flow]` is disabled, the Normal band always applies and no bell sounds. |
| `slow_below` | float | `1.0` | `0.05..=20.0` | Upper bound of the Slow band. |
| `fast_above` | float | `3.0` | `0.05..=20.0` | Lower bound of the Fast band. |
| `very_fast_above` | float | `4.5` | `0.05..=20.0` | Lower bound of the Very fast band. The three bounds must be strictly increasing, otherwise all three revert to their defaults. |
| `very_fast_speed_level` | integer | `1` | `0..=2` | Speed level in the Very fast band. `0` disables the band, so the Fast band applies instead and no bell sounds. |

#### Existing installs

Chrono Dawn writes `chronodawn.toml` once and never rewrites it. A file
created before this change keeps `normal_slowness_level = 4` and
`enhanced_slowness_level = 5`, so mobs stay one level slower than the new
defaults in every Slowness band. To adopt the new balance, set them to `3`
and `4`.

#### Example: remove ambient Slowness

```toml
[gameplay.time_distortion]
enabled = false
```

This does not affect Slowness applied by individual attacks, items, blocks, or
boss abilities. Those mechanics retain their own balance.

#### Example: a constant effect without time-flow bands

```toml
[gameplay.time_distortion]
link_to_time_flow = false
```

#### Example: make the Chrono dimension less restrictive

```toml
[gameplay.time_distortion]
normal_slowness_level = 2
enhanced_slowness_level = 3
```
````

- [ ] **Step 2: `docs/player_guide.md`**

Line 39, replace the bullet with:
`- **Time Distortion Effect**: Hostile mobs are slowed while you move at normal speed. The effect follows the flow of time, and when time races, mobs briefly become faster instead`

Line 194, replace the paragraph with:
`Hostile mobs in Chrono Dawn are slowed by distorted time, while you move at normal speed. With default settings they usually have **Slowness III**. When time flows slowly they are slowed more, and when it speeds up they are slowed less. When time races, the distortion flips and mobs get **Speed I**. A distant bell, a racing sky, and swirling particles around mobs are the signs. Some creatures resist the distortion, and Clockwork Sentinel ignores it. After the Eye of Chronos enhancement, every Slowness band becomes one level stronger.`

Line 1089, replace `   - Hostile mobs have Slowness IV` with `   - Hostile mobs are usually slowed (Slowness III by default)`.

Line 1225, replace `   - Mobs already have Slowness IV in Chrono Dawn` with `   - Mobs are already slowed in Chrono Dawn`. Then read the next line (`   - Makes combat trivially easy!`) and replace it with `   - Makes combat much easier while time is not racing`.

Read about 10 lines around each location first and adjust only the lines named here.

- [ ] **Step 3: Store descriptions**

In `README.md:365`, replace `   - [ ] Verify hostile mobs have Slowness IV effect` with `   - [ ] Verify hostile mobs have Slowness III effect (default config, Normal band)`. Add `README.md` to this task's commit.

In `docs/curseforge_description.md:153` and `docs/modrinth_description.md:152`, replace `- **Time Distortion**: Hostile mobs in Chrono Dawn move extremely slowly` with `- **Time Distortion**: Hostile mobs in Chrono Dawn are slowed, more or less depending on the flow of time, and briefly speed up when time races`.

- [ ] **Step 4: `CHANGELOG.md`**

Under `## [Unreleased]`, add to `### Changed` (create the heading after `### Added` if absent):

```markdown
- **Time Distortion rebalance**: the ambient effect in Chrono Dawn now follows
  the day/night cycle speed. Mobs are slowed more when time runs slowly and
  less when it speeds up. When time races, mobs get Speed I instead, and a
  distant bell warns players in the dimension. The default Normal level is
  Slowness III (IV after the Eye of Chronos), down from IV (V). New config
  keys: `link_to_time_flow`, `slow_below`, `fast_above`, `very_fast_above`,
  and `very_fast_speed_level`.
  Existing `chronodawn.toml` files keep their written levels (4 and 5). Set
  `normal_slowness_level = 3` and `enhanced_slowness_level = 4` to adopt the
  new balance.
- **Time Distortion resistance**: Temporal Wraith, Timeline Strider, and
  Forgotten Minute now resist the distortion (two Slowness levels weaker),
  listed in the new `chronodawn:time_distortion_resistant` entity type tag.
  Clockwork Sentinel is now in `chronodawn:time_distortion_immune`, and
  removing it from that tag makes it affected by Slowness again.
```

Under `### Fixed` (create after `### Changed` if absent):

```markdown
- **Time Distortion immune tag on Minecraft 1.21.5 and later**: datapack
  changes to `chronodawn:time_distortion_immune` were ignored on these
  versions because the boss list was hardcoded. The tag is now honored on
  every version.
```

- [ ] **Step 5: Commit (controller, with user go-ahead)**

```bash
git add README.md docs/configuration.md docs/player_guide.md docs/curseforge_description.md docs/modrinth_description.md CHANGELOG.md
git commit -m "docs: describe the Time Distortion rebalance"
```

---

### Task 7: Full verification

- [ ] **Step 1: Static checks**

Run, one at a time: `./gradlew validateResources`, `./gradlew validateData`, `./gradlew validateTranslations`. Expected: all PASS.

- [ ] **Step 2: Build and unit test every version**

Run: `./gradlew buildAll`, then `./gradlew testAll`. Expected: BUILD SUCCESSFUL. If a wrapper reports success, confirm with the per-version log lines rather than the exit code alone.

- [ ] **Step 3: Production smoke test on the priority targets**

Run: `./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=1.21.1`, `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=1.21.1`, `./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=26.3`. Expected: PASS with no new WARN/ERROR lines.

- [ ] **Step 4: Hand the runtime checklist to the user**

The behavior needs a real client. Give the user these steps for 1.21.1 (NeoForge and Fabric) and 26.3:

1. Start with a new instance so the new `chronodawn.toml` is written. Confirm `normal_slowness_level = 3` in it.
2. In Chrono Dawn, check a Normal-band mob with `/effect` UI or `/data get entity @e[type=chronodawn:epoch_husk,limit=1,sort=nearest] active_effects`. Expected: Slowness amplifier 2, ambient true.
3. Check Temporal Wraith: Slowness amplifier 0. Clockwork Sentinel: no Slowness.
4. Set `[gameplay.time_flow] min_speed = 4.8`, `max_speed = 5.0`, `min_duration_ticks = 200`, `max_duration_ticks = 400`, restart, and wait for the first speed change. Expected: one distant bell, Speed amplifier 0 with particles on mobs, and no leftover Slowness.
5. Set `min_speed = 0.5`, `max_speed = 0.9`. Expected: the Speed effect disappears within a tick of the change and Slowness amplifier 3 appears.
6. Replace the config file with one that has only the old keys (`normal_slowness_level = 4`, `enhanced_slowness_level = 5`). Expected: Normal band Slowness amplifier 3.
7. On 26.3, add a datapack that overrides `data/chronodawn/tags/entity_type/time_distortion_immune.json` with `"replace": true` and only the six bosses. Expected: Clockwork Sentinel gets Slowness.
