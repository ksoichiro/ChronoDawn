# Boss HP / Damage Multipliers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let modpack authors scale each Chrono Dawn boss's health and damage from `config/chronodawn.toml`.

**Architecture:** Base statistics move out of the eleven duplicated version modules into a single shared enum (`BossKind` / `BossAbility`). A shared, Minecraft-free helper (`BossScaling`) multiplies those base values by the configured multiplier at the point of use — attribute registration for health and melee damage, ability invocation for area attacks, and projectile impact for the two projectiles.

**Tech Stack:** Java 21, Architectury multi-loader (Fabric + NeoForge), night-config TOML parser, JUnit 5, Minecraft GameTest.

## Global Constraints

- Supported Minecraft versions: 1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11. There is no `common/1.21.3` — 1.21.3 reuses the 1.21.2 modules.
- Mojang mappings. Use official names (`net.minecraft.world.level.Level`), never Yarn.
- All committed files (docs, comments, CHANGELOG) are written in **English**.
- Commit messages: English, Conventional Commits.
- Every file ends with a newline.
- `schema_version` stays `1`. Do not bump it — this change is purely additive and every default reproduces current behavior.
- Defaults must be a no-op: a user who never edits `chronodawn.toml` must observe byte-identical gameplay.
- Shared code in `common/shared` compiles into **all eleven** version modules, so it must not use any API that differs across them. `Attributes.MAX_HEALTH` is `Attribute` on 1.20.1 and `Holder<Attribute>` from 1.21.1 — never pass attribute objects through shared code.
- Do not "normalise" pre-existing per-version differences you encounter. Specifically: `ChronosWardenEntity` has `MOVEMENT_SPEED` `0.15` on 1.20.1 and `0.20` on 1.21.1+. Leave both exactly as they are; this plan only touches `MAX_HEALTH` and `ATTACK_DAMAGE`.
- If executing via subagents: the controller performs all `git commit` calls. Subagents cannot GPG-sign (the sandbox blocks `~/.gnupg`).
- `buildAll` / `gameTestAll` wrappers occasionally report a spurious `FAILED`. Per-version standalone runs are authoritative; re-run the single version before believing a wrapper failure.

**Design spec:** `docs/superpowers/specs/2026-07-25-boss-stat-multipliers-design.md`

---

## File Structure

**New (all in `common/shared`, compiled into every version module):**

| File | Responsibility |
| --- | --- |
| `common/shared/src/main/java/com/chronodawn/config/BossSettings.java` | The two multipliers for one boss |
| `common/shared/src/main/java/com/chronodawn/config/BossesConfig.java` | The six `BossSettings` |
| `common/shared/src/main/java/com/chronodawn/entities/bosses/BossKind.java` | Boss identity + base `MAX_HEALTH` / `ATTACK_DAMAGE`; resolves its own `BossSettings` |
| `common/shared/src/main/java/com/chronodawn/entities/bosses/BossAbility.java` | Base damage of each non-attribute damage source, tied to its owning `BossKind` |
| `common/shared/src/main/java/com/chronodawn/entities/bosses/BossScaling.java` | Pure arithmetic: base × multiplier. No Minecraft imports — this is what makes it unit-testable |
| `common/shared/src/main/java/com/chronodawn/entities/bosses/BossProjectileDamage.java` | Resolves a projectile's owner to a `BossAbility`. Kept separate from `BossScaling` so the arithmetic stays Minecraft-free |
| `common/shared/src/test/java/com/chronodawn/unit/BossScalingTest.java` | Unit tests for the arithmetic and the no-op contract |

`BossKind`, `BossAbility`, `BossScaling` and `BossProjectileDamage` live in package `com.chronodawn.entities.bosses` — the same package as the boss entity classes. That is deliberate: the 66 call sites in the version modules then need **no new import**.

**Modified:**

| File | Change |
| --- | --- |
| `common/shared/.../config/ChronoDawnConfig.java` | Add `Gameplay gameplay` component and the `Gameplay` record |
| `common/shared/.../config/ConfigDefaults.java` | Add `BOSS_DEFAULTS` and wire `Gameplay` into `defaults()` |
| `common/shared/.../config/ConfigLoader.java` | Parse and validate `[gameplay.bosses.*]`; teach the unknown-key warning about `gameplay` |
| `common/shared/src/main/resources/chronodawn-default-config.toml` | Six commented boss tables |
| `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java` | Parsing / validation tests |
| `common/<v>/.../entities/bosses/{TimeGuardian,ChronosWarden,ClockworkColossus,EntropyKeeper,TemporalPhantom,TimeTyrant}Entity.java` | ×11 versions: route `MAX_HEALTH` / `ATTACK_DAMAGE` and ability damage through `BossScaling` |
| `common/<v>/.../entities/projectiles/{TimeBlast,GearProjectile}Entity.java` | ×11 versions: owner-resolved damage |
| `common/<v>/.../gametest/boss/BossFightTestLogic.java` | ×11 versions: max-health assertion |
| `docs/configuration.md`, `docs/modpack-integration.md`, `CHANGELOG.md`, `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md` | Documentation |

**The eleven version directories** (referred to below as `<v>`):
`1.20.1  1.21.1  1.21.2  1.21.4  1.21.5  1.21.6  1.21.7  1.21.8  1.21.9  1.21.10  1.21.11`

---

## Task 1: Config schema for boss multipliers

Adds the `[gameplay.bosses.*]` section end-to-end: records, defaults, parsing, validation, the bundled commented default, and the reference documentation. No boss behavior changes yet — this task is complete when the config round-trips correctly.

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/config/BossSettings.java`
- Create: `common/shared/src/main/java/com/chronodawn/config/BossesConfig.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml`
- Modify: `docs/configuration.md`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `record BossSettings(double healthMultiplier, double damageMultiplier)`
  - `record BossesConfig(BossSettings timeGuardian, BossSettings chronosWarden, BossSettings clockworkColossus, BossSettings entropyKeeper, BossSettings temporalPhantom, BossSettings timeTyrant)`
  - `record ChronoDawnConfig.Gameplay(BossesConfig bosses)`
  - `ChronoDawnConfig.gameplay()` returns `Gameplay`
  - `ConfigDefaults.BOSS_DEFAULTS` — a `BossSettings` of `(1.0, 1.0)`

---

- [ ] **Step 1: Write the failing tests**

Append these tests to `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java`, inside the existing `ConfigLoaderTest` class. Add `import com.chronodawn.config.BossSettings;` to the existing import block.

```java
    @Test
    void bosses_missingSection_fallsBackToDefaults(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[world.structures.ancient_ruins]\n" +
            "spacing = 32\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().timeGuardian());
        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().timeTyrant());
    }

    @Test
    void bosses_allSixParse(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "health_multiplier = 2.0\n" +
            "damage_multiplier = 0.5\n" +
            "[gameplay.bosses.chronos_warden]\n" +
            "health_multiplier = 1.5\n" +
            "[gameplay.bosses.clockwork_colossus]\n" +
            "damage_multiplier = 3.0\n" +
            "[gameplay.bosses.entropy_keeper]\n" +
            "health_multiplier = 0.5\n" +
            "[gameplay.bosses.temporal_phantom]\n" +
            "damage_multiplier = 0.0\n" +
            "[gameplay.bosses.time_tyrant]\n" +
            "health_multiplier = 10.0\n" +
            "damage_multiplier = 10.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);
        BossesConfig bosses = config.gameplay().bosses();

        assertEquals(new BossSettings(2.0, 0.5), bosses.timeGuardian());
        // Unspecified field within a present table still falls back
        assertEquals(new BossSettings(1.5, 1.0), bosses.chronosWarden());
        assertEquals(new BossSettings(1.0, 3.0), bosses.clockworkColossus());
        assertEquals(new BossSettings(0.5, 1.0), bosses.entropyKeeper());
        assertEquals(new BossSettings(1.0, 0.0), bosses.temporalPhantom());
        assertEquals(new BossSettings(10.0, 10.0), bosses.timeTyrant());
    }

    @Test
    void bosses_healthMultiplierZero_revertsToDefault(@TempDir Path tmp) throws IOException {
        // Zero health would mean a max health of 0 — rejected. damage_multiplier
        // on the same table is valid and must survive independently.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "health_multiplier = 0.0\n" +
            "damage_multiplier = 2.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS.healthMultiplier(),
            config.gameplay().bosses().timeGuardian().healthMultiplier());
        assertEquals(2.0, config.gameplay().bosses().timeGuardian().damageMultiplier(),
            "A valid field must not be reset by an invalid sibling");
    }

    @Test
    void bosses_damageMultiplierZero_isAccepted(@TempDir Path tmp) throws IOException {
        // Zero damage is deliberately allowed: story-focused packs keep the
        // encounter as spectacle without lethality.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_tyrant]\n" +
            "damage_multiplier = 0.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(0.0, config.gameplay().bosses().timeTyrant().damageMultiplier());
    }

    @Test
    void bosses_multiplierOverMax_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_tyrant]\n" +
            "health_multiplier = 11.0\n" +
            "damage_multiplier = 50.0\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().timeTyrant());
    }

    @Test
    void bosses_multiplierNegative_revertsToDefault(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.entropy_keeper]\n" +
            "health_multiplier = -1.0\n" +
            "damage_multiplier = -0.5\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().entropyKeeper());
    }

    @Test
    void bosses_nonFiniteMultiplier_revertsToDefault(@TempDir Path tmp) throws IOException {
        // TOML spells these nan / inf; night-config parses them as Double.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.temporal_phantom]\n" +
            "health_multiplier = nan\n" +
            "damage_multiplier = inf\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(ConfigDefaults.BOSS_DEFAULTS, config.gameplay().bosses().temporalPhantom());
    }

    @Test
    void bosses_integerLiteralIsAccepted(@TempDir Path tmp) throws IOException {
        // TOML distinguishes 2 (integer) from 2.0 (float); the loader reads via
        // Number so both must work.
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.clockwork_colossus]\n" +
            "health_multiplier = 2\n");

        ChronoDawnConfig config = ConfigLoader.load(tmp);

        assertEquals(2.0, config.gameplay().bosses().clockworkColossus().healthMultiplier());
    }
```

Add the import for `BossesConfig` alongside the others:

```java
import com.chronodawn.config.BossesConfig;
import com.chronodawn.config.BossSettings;
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*'
```

Expected: compilation failure — `BossSettings`, `BossesConfig`, and `ChronoDawnConfig.gameplay()` do not exist yet.

- [ ] **Step 3: Create `BossSettings`**

Create `common/shared/src/main/java/com/chronodawn/config/BossSettings.java`. Copy the 17-line LGPL header verbatim from `common/shared/src/main/java/com/chronodawn/config/OreSettings.java` — every source file in this project carries it.

```java
package com.chronodawn.config;

/**
 * Multipliers applied to a single boss's statistics.
 *
 * <p>{@code healthMultiplier} scales the boss's {@code MAX_HEALTH} attribute.
 * {@code damageMultiplier} scales every damage source the boss deals: its
 * {@code ATTACK_DAMAGE} attribute, its area-of-effect and slam abilities, and
 * the projectiles it fires. Both default to {@code 1.0}, which reproduces the
 * hardcoded values exactly.
 */
public record BossSettings(double healthMultiplier, double damageMultiplier) {}
```

- [ ] **Step 4: Create `BossesConfig`**

Create `common/shared/src/main/java/com/chronodawn/config/BossesConfig.java` with the same LGPL header.

```java
package com.chronodawn.config;

/**
 * Container for the six boss entities exposed via
 * {@code config/chronodawn.toml}. Field order follows the progression order
 * the player meets them in, not alphabetical order.
 */
public record BossesConfig(
    BossSettings timeGuardian,
    BossSettings chronosWarden,
    BossSettings clockworkColossus,
    BossSettings entropyKeeper,
    BossSettings temporalPhantom,
    BossSettings timeTyrant
) {}
```

- [ ] **Step 5: Extend `ChronoDawnConfig`**

In `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java`, change the record header from:

```java
public record ChronoDawnConfig(
    int schemaVersion,
    World world
) {
```

to:

```java
public record ChronoDawnConfig(
    int schemaVersion,
    World world,
    Gameplay gameplay
) {
```

and add the nested record next to the existing `World` / `Structures` / `AncientRuins` declarations at the bottom of the class:

```java
    public record Gameplay(BossesConfig bosses) {}
```

Leave `CURRENT_SCHEMA_VERSION` at `1`.

- [ ] **Step 6: Extend `ConfigDefaults`**

In `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java`, add after the ore defaults:

```java
    // All six bosses default to unmodified statistics. A single shared constant
    // keeps "the default is a no-op" impossible to break for one boss only.
    public static final BossSettings BOSS_DEFAULTS = new BossSettings(1.0, 1.0);
```

and extend `defaults()` with the third component:

```java
    public static ChronoDawnConfig defaults() {
        return new ChronoDawnConfig(
            ChronoDawnConfig.CURRENT_SCHEMA_VERSION,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    new ChronoDawnConfig.AncientRuins(
                        ANCIENT_RUINS_ENABLED,
                        ANCIENT_RUINS_SPACING,
                        ANCIENT_RUINS_SEPARATION,
                        ANCIENT_RUINS_SALT
                    )
                ),
                new OresConfig(
                    TIME_CRYSTAL_DEFAULTS,
                    ENTROPY_CRYSTAL_DEFAULTS,
                    TEMPORAL_AMBER_DEFAULTS,
                    CLOCKSTONE_DEFAULTS
                )
            ),
            new ChronoDawnConfig.Gameplay(
                new BossesConfig(
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS
                )
            )
        );
    }
```

- [ ] **Step 7: Extend `ConfigLoader`**

In `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java`, add these key and bound constants next to the existing ore constants:

```java
    private static final String K_GAMEPLAY = "gameplay";
    private static final String K_BOSSES = "bosses";
    private static final String K_HEALTH_MULTIPLIER = "health_multiplier";
    private static final String K_DAMAGE_MULTIPLIER = "damage_multiplier";

    // Health may not be zeroed: a max health of 0 kills the entity on spawn.
    // Damage may be zeroed: a harmless boss is a legitimate pack choice.
    private static final double MIN_HEALTH_MULTIPLIER = 0.1;
    private static final double MIN_DAMAGE_MULTIPLIER = 0.0;
    private static final double MAX_MULTIPLIER = 10.0;
```

Add the two parse methods at the end of the class, before the closing brace:

```java
    private static ChronoDawnConfig.Gameplay parseGameplay(CommentedConfig parsed) {
        return new ChronoDawnConfig.Gameplay(
            new BossesConfig(
                parseBoss(parsed, "time_guardian"),
                parseBoss(parsed, "chronos_warden"),
                parseBoss(parsed, "clockwork_colossus"),
                parseBoss(parsed, "entropy_keeper"),
                parseBoss(parsed, "temporal_phantom"),
                parseBoss(parsed, "time_tyrant")
            )
        );
    }

    private static BossSettings parseBoss(CommentedConfig parsed, String bossKey) {
        String path = K_GAMEPLAY + "." + K_BOSSES + "." + bossKey;
        BossSettings defaults = ConfigDefaults.BOSS_DEFAULTS;

        double health = parsed.<Number>getOptional(path + "." + K_HEALTH_MULTIPLIER)
            .map(Number::doubleValue)
            .orElse(defaults.healthMultiplier());

        double damage = parsed.<Number>getOptional(path + "." + K_DAMAGE_MULTIPLIER)
            .map(Number::doubleValue)
            .orElse(defaults.damageMultiplier());

        // Validation: each field reverts independently so one bad value doesn't
        // reset the other. isFinite also rejects nan / inf.
        if (!Double.isFinite(health) || health < MIN_HEALTH_MULTIPLIER || health > MAX_MULTIPLIER) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_HEALTH_MULTIPLIER, health, MIN_HEALTH_MULTIPLIER, MAX_MULTIPLIER,
                defaults.healthMultiplier()
            );
            health = defaults.healthMultiplier();
        }
        if (!Double.isFinite(damage) || damage < MIN_DAMAGE_MULTIPLIER || damage > MAX_MULTIPLIER) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_DAMAGE_MULTIPLIER, damage, MIN_DAMAGE_MULTIPLIER, MAX_MULTIPLIER,
                defaults.damageMultiplier()
            );
            damage = defaults.damageMultiplier();
        }

        return new BossSettings(health, damage);
    }
```

In `parseOrDefaults`, add the call and extend the constructor. Replace:

```java
        ChronoDawnConfig.AncientRuins ancientRuins = parseAncientRuins(parsed);
        com.chronodawn.config.OresConfig ores = parseOres(parsed);
```

with:

```java
        ChronoDawnConfig.AncientRuins ancientRuins = parseAncientRuins(parsed);
        com.chronodawn.config.OresConfig ores = parseOres(parsed);
        ChronoDawnConfig.Gameplay gameplay = parseGameplay(parsed);
```

Replace the unknown-key loop condition:

```java
            if (!key.equals(K_SCHEMA_VERSION) && !key.equals(K_WORLD)) {
```

with:

```java
            if (!key.equals(K_SCHEMA_VERSION) && !key.equals(K_WORLD) && !key.equals(K_GAMEPLAY)) {
```

And replace the return statement:

```java
        return new ChronoDawnConfig(
            schemaVersion,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(ancientRuins),
                ores
            ),
            gameplay
        );
```

- [ ] **Step 8: Run the tests to verify they pass**

```bash
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*ConfigLoaderTest*'
```

Expected: PASS, including the pre-existing tests. If `bosses_nonFiniteMultiplier_revertsToDefault` fails at the *parse* stage (night-config rejecting `nan` / `inf` as malformed TOML rather than producing a `Double`), the whole file falls back to defaults and the assertion still holds — but the failure mode differs. If that happens, keep the test and add a comment recording the observed behavior; do not delete the `isFinite` guard, because a `Number` from another source could still be non-finite.

- [ ] **Step 9: Extend the bundled default config**

Append to `common/shared/src/main/resources/chronodawn-default-config.toml`:

```toml
[gameplay.bosses.time_guardian]
# Scales the boss's maximum health. 2.0 = double health. Range: 0.1 to 10.0.
health_multiplier = 1.0

# Scales every damage source this boss deals: melee, its area-of-effect
# attack, and the Time Blast projectile it fires. 0.0 makes the boss
# harmless. Range: 0.0 to 10.0.
damage_multiplier = 1.0

[gameplay.bosses.chronos_warden]
health_multiplier = 1.0
damage_multiplier = 1.0

[gameplay.bosses.clockwork_colossus]
health_multiplier = 1.0
damage_multiplier = 1.0

[gameplay.bosses.entropy_keeper]
health_multiplier = 1.0
damage_multiplier = 1.0

[gameplay.bosses.temporal_phantom]
health_multiplier = 1.0
damage_multiplier = 1.0

[gameplay.bosses.time_tyrant]
health_multiplier = 1.0
damage_multiplier = 1.0
```

- [ ] **Step 10: Document the schema**

In `docs/configuration.md`, insert a new section after the `### [world.ores.*]` section and before `## Adding more configuration`. Match the surrounding style (fenced TOML sample, then a field table).

````markdown
---

### `[gameplay.bosses.*]`

Per-boss health and damage scaling for the six Chrono Dawn bosses. Each
boss has its own table; missing sections fall back to `1.0`, which
reproduces the shipped balance exactly.

Table keys are the entity IDs: `time_guardian`, `chronos_warden`,
`clockwork_colossus`, `entropy_keeper`, `temporal_phantom`,
`time_tyrant`.

```toml
[gameplay.bosses.time_guardian]
health_multiplier = 1.0
damage_multiplier = 1.0
```

| Field | Type | Default | Range | Notes |
| --- | --- | --- | --- | --- |
| `health_multiplier` | float | `1.0` | `0.1..=10.0` | Scales the `MAX_HEALTH` attribute. Zero is rejected — a maximum health of 0 kills the entity on spawn. |
| `damage_multiplier` | float | `1.0` | `0.0..=10.0` | Scales **every** damage source the boss deals: the melee `ATTACK_DAMAGE` attribute, area-of-effect and ground-slam abilities, and the projectiles it fires. `0.0` is allowed and makes the boss harmless. |

Unscaled base statistics, for reference when picking a multiplier:

| Boss | Max health | Melee damage | Other damage |
| --- | --- | --- | --- |
| Time Guardian | 200 | 10 | AoE 6, Time Blast 4 |
| Chronos Warden | 180 | 9 | Ground slam 2 |
| Clockwork Colossus | 200 | 12 | Ground slam 6, Gear projectile 8 |
| Entropy Keeper | 160 | 10 | Slam 10 |
| Temporal Phantom | 150 | 8 | Time Blast 4 |
| Time Tyrant | 500 | 18 | AoE 12 |

Armor, knockback resistance and movement speed are not configurable.
Multipliers apply to bosses spawned after the change; see
[`modpack-integration.md`](modpack-integration.md) for the
existing-world caveats.
````

- [ ] **Step 11: Verify the full config test suite and build**

```bash
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11
```

Expected: PASS. Then confirm the oldest version still compiles, since `ChronoDawnConfig`'s constructor arity changed:

```bash
./gradlew build1_20_1
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 12: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ \
        common/shared/src/main/resources/chronodawn-default-config.toml \
        common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java \
        docs/configuration.md
git commit -m "feat(config): add per-boss health and damage multipliers"
```

---

## Task 2: Shared base-stat table and scaling helper

Introduces the single definition site for boss base statistics and the arithmetic that applies the configured multipliers. Still no behavior change — nothing calls these yet.

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/entities/bosses/BossKind.java`
- Create: `common/shared/src/main/java/com/chronodawn/entities/bosses/BossAbility.java`
- Create: `common/shared/src/main/java/com/chronodawn/entities/bosses/BossScaling.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/BossScalingTest.java`

**Interfaces:**
- Consumes: `BossSettings`, `BossesConfig`, `ChronoDawnConfig.gameplay()`, `ConfigDefaults.BOSS_DEFAULTS` (Task 1).
- Produces:
  - `enum BossKind` with constants `TIME_GUARDIAN, CHRONOS_WARDEN, CLOCKWORK_COLOSSUS, ENTROPY_KEEPER, TEMPORAL_PHANTOM, TIME_TYRANT`; methods `String configKey()`, `double baseMaxHealth()`, `double baseAttackDamage()`, `BossSettings settings()`
  - `enum BossAbility` with constants `TIME_GUARDIAN_AOE, TIME_GUARDIAN_TIME_BLAST, CHRONOS_WARDEN_GROUND_SLAM, CLOCKWORK_COLOSSUS_GROUND_SLAM, CLOCKWORK_COLOSSUS_GEAR_PROJECTILE, ENTROPY_KEEPER_SLAM, TEMPORAL_PHANTOM_TIME_BLAST, TIME_TYRANT_AOE`; methods `BossKind owner()`, `float baseDamage()`
  - `BossScaling.health(BossKind)` → `double`
  - `BossScaling.attackDamage(BossKind)` → `double`
  - `BossScaling.ability(BossAbility)` → `float`

---

- [ ] **Step 1: Write the failing test**

Create `common/shared/src/test/java/com/chronodawn/unit/BossScalingTest.java` with the LGPL header copied from `ConfigLoaderTest.java`.

```java
package com.chronodawn.unit;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.entities.bosses.BossAbility;
import com.chronodawn.entities.bosses.BossKind;
import com.chronodawn.entities.bosses.BossScaling;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link BossScaling}. The most important case is
 * {@code defaultConfig_isExactlyBaseValues}: it pins the contract that a user
 * who never edits the config observes byte-identical boss statistics.
 */
class BossScalingTest {

    @AfterEach
    void restoreDefaults() {
        // BossScaling reads the process-wide config singleton; leaving a
        // modified config behind would leak into other test classes.
        ChronoDawnConfig.set(ConfigDefaults.defaults());
    }

    @Test
    void defaultConfig_isExactlyBaseValues() {
        ChronoDawnConfig.set(ConfigDefaults.defaults());

        for (BossKind kind : BossKind.values()) {
            assertEquals(kind.baseMaxHealth(), BossScaling.health(kind), 0.0,
                kind + " health must be unmodified at the default multiplier");
            assertEquals(kind.baseAttackDamage(), BossScaling.attackDamage(kind), 0.0,
                kind + " attack damage must be unmodified at the default multiplier");
        }
        for (BossAbility ability : BossAbility.values()) {
            assertEquals(ability.baseDamage(), BossScaling.ability(ability), 0.0f,
                ability + " must be unmodified at the default multiplier");
        }
    }

    @Test
    void baseValuesMatchShippedBalance() {
        // Guards against a typo while moving literals out of the version modules.
        assertEquals(200.0, BossKind.TIME_GUARDIAN.baseMaxHealth(), 0.0);
        assertEquals(10.0, BossKind.TIME_GUARDIAN.baseAttackDamage(), 0.0);
        assertEquals(180.0, BossKind.CHRONOS_WARDEN.baseMaxHealth(), 0.0);
        assertEquals(9.0, BossKind.CHRONOS_WARDEN.baseAttackDamage(), 0.0);
        assertEquals(200.0, BossKind.CLOCKWORK_COLOSSUS.baseMaxHealth(), 0.0);
        assertEquals(12.0, BossKind.CLOCKWORK_COLOSSUS.baseAttackDamage(), 0.0);
        assertEquals(160.0, BossKind.ENTROPY_KEEPER.baseMaxHealth(), 0.0);
        assertEquals(10.0, BossKind.ENTROPY_KEEPER.baseAttackDamage(), 0.0);
        assertEquals(150.0, BossKind.TEMPORAL_PHANTOM.baseMaxHealth(), 0.0);
        assertEquals(8.0, BossKind.TEMPORAL_PHANTOM.baseAttackDamage(), 0.0);
        assertEquals(500.0, BossKind.TIME_TYRANT.baseMaxHealth(), 0.0);
        assertEquals(18.0, BossKind.TIME_TYRANT.baseAttackDamage(), 0.0);

        assertEquals(6.0f, BossAbility.TIME_GUARDIAN_AOE.baseDamage(), 0.0f);
        assertEquals(4.0f, BossAbility.TIME_GUARDIAN_TIME_BLAST.baseDamage(), 0.0f);
        assertEquals(2.0f, BossAbility.CHRONOS_WARDEN_GROUND_SLAM.baseDamage(), 0.0f);
        assertEquals(6.0f, BossAbility.CLOCKWORK_COLOSSUS_GROUND_SLAM.baseDamage(), 0.0f);
        assertEquals(8.0f, BossAbility.CLOCKWORK_COLOSSUS_GEAR_PROJECTILE.baseDamage(), 0.0f);
        assertEquals(10.0f, BossAbility.ENTROPY_KEEPER_SLAM.baseDamage(), 0.0f);
        assertEquals(4.0f, BossAbility.TEMPORAL_PHANTOM_TIME_BLAST.baseDamage(), 0.0f);
        assertEquals(12.0f, BossAbility.TIME_TYRANT_AOE.baseDamage(), 0.0f);
    }

    @Test
    void healthMultiplier_scalesOnlyTheTargetBoss(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "health_multiplier = 2.0\n");
        com.chronodawn.config.ConfigLoader.load(tmp);

        assertEquals(400.0, BossScaling.health(BossKind.TIME_GUARDIAN), 0.0);
        assertEquals(500.0, BossScaling.health(BossKind.TIME_TYRANT), 0.0,
            "Other bosses must be untouched");
    }

    @Test
    void damageMultiplier_scalesMeleeAndAbilities(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_guardian]\n" +
            "damage_multiplier = 0.5\n");
        com.chronodawn.config.ConfigLoader.load(tmp);

        assertEquals(5.0, BossScaling.attackDamage(BossKind.TIME_GUARDIAN), 0.0);
        assertEquals(3.0f, BossScaling.ability(BossAbility.TIME_GUARDIAN_AOE), 0.0f);
        assertEquals(2.0f, BossScaling.ability(BossAbility.TIME_GUARDIAN_TIME_BLAST), 0.0f);
        // Temporal Phantom fires the same projectile type but is configured
        // separately, so its Time Blast stays at the base value.
        assertEquals(4.0f, BossScaling.ability(BossAbility.TEMPORAL_PHANTOM_TIME_BLAST), 0.0f);
    }

    @Test
    void damageMultiplierZero_yieldsZeroDamage(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.bosses.time_tyrant]\n" +
            "damage_multiplier = 0.0\n");
        com.chronodawn.config.ConfigLoader.load(tmp);

        assertEquals(0.0, BossScaling.attackDamage(BossKind.TIME_TYRANT), 0.0);
        assertEquals(0.0f, BossScaling.ability(BossAbility.TIME_TYRANT_AOE), 0.0f);
    }

    @Test
    void configKeysMatchTomlTableNames() {
        assertEquals("time_guardian", BossKind.TIME_GUARDIAN.configKey());
        assertEquals("chronos_warden", BossKind.CHRONOS_WARDEN.configKey());
        assertEquals("clockwork_colossus", BossKind.CLOCKWORK_COLOSSUS.configKey());
        assertEquals("entropy_keeper", BossKind.ENTROPY_KEEPER.configKey());
        assertEquals("temporal_phantom", BossKind.TEMPORAL_PHANTOM.configKey());
        assertEquals("time_tyrant", BossKind.TIME_TYRANT.configKey());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*BossScalingTest*'
```

Expected: compilation failure — `BossKind`, `BossAbility` and `BossScaling` do not exist.

- [ ] **Step 3: Create `BossKind`**

Create `common/shared/src/main/java/com/chronodawn/entities/bosses/BossKind.java` with the LGPL header.

```java
package com.chronodawn.entities.bosses;

import com.chronodawn.config.BossSettings;
import com.chronodawn.config.BossesConfig;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.registry.ModEntityId;

import java.util.function.Function;

/**
 * The six boss entities and their shipped base statistics.
 *
 * <p>This enum is the single definition site for boss {@code MAX_HEALTH} and
 * {@code ATTACK_DAMAGE}. The values used to be duplicated in every version
 * module's {@code createAttributes()}; keeping them here means a balance
 * change is a one-line edit instead of eleven.
 *
 * <p>Deliberately free of Minecraft imports so it compiles unchanged across
 * 1.20.1 through 1.21.11.
 */
public enum BossKind {
    TIME_GUARDIAN(ModEntityId.TIME_GUARDIAN, 200.0, 10.0, b -> b.timeGuardian()),
    CHRONOS_WARDEN(ModEntityId.CHRONOS_WARDEN, 180.0, 9.0, b -> b.chronosWarden()),
    CLOCKWORK_COLOSSUS(ModEntityId.CLOCKWORK_COLOSSUS, 200.0, 12.0, b -> b.clockworkColossus()),
    ENTROPY_KEEPER(ModEntityId.ENTROPY_KEEPER, 160.0, 10.0, b -> b.entropyKeeper()),
    TEMPORAL_PHANTOM(ModEntityId.TEMPORAL_PHANTOM, 150.0, 8.0, b -> b.temporalPhantom()),
    TIME_TYRANT(ModEntityId.TIME_TYRANT, 500.0, 18.0, b -> b.timeTyrant());

    private final ModEntityId entityId;
    private final double baseMaxHealth;
    private final double baseAttackDamage;
    private final Function<BossesConfig, BossSettings> selector;

    BossKind(
        ModEntityId entityId,
        double baseMaxHealth,
        double baseAttackDamage,
        Function<BossesConfig, BossSettings> selector
    ) {
        this.entityId = entityId;
        this.baseMaxHealth = baseMaxHealth;
        this.baseAttackDamage = baseAttackDamage;
        this.selector = selector;
    }

    /** The {@code [gameplay.bosses.<key>]} table name, equal to the entity ID. */
    public String configKey() {
        return entityId.id();
    }

    public double baseMaxHealth() {
        return baseMaxHealth;
    }

    public double baseAttackDamage() {
        return baseAttackDamage;
    }

    /** The multipliers currently configured for this boss. */
    public BossSettings settings() {
        return selector.apply(ChronoDawnConfig.get().gameplay().bosses());
    }
}
```

- [ ] **Step 4: Create `BossAbility`**

Create `common/shared/src/main/java/com/chronodawn/entities/bosses/BossAbility.java` with the LGPL header.

```java
package com.chronodawn.entities.bosses;

/**
 * Every boss damage source that is not the {@code ATTACK_DAMAGE} attribute.
 *
 * <p>Each constant names its owning boss so {@code damage_multiplier} resolves
 * correctly even when two bosses share a projectile type: Time Guardian and
 * Temporal Phantom both fire Time Blast, and each scales by its own config.
 */
public enum BossAbility {
    TIME_GUARDIAN_AOE(BossKind.TIME_GUARDIAN, 6.0f),
    TIME_GUARDIAN_TIME_BLAST(BossKind.TIME_GUARDIAN, 4.0f),
    CHRONOS_WARDEN_GROUND_SLAM(BossKind.CHRONOS_WARDEN, 2.0f),
    CLOCKWORK_COLOSSUS_GROUND_SLAM(BossKind.CLOCKWORK_COLOSSUS, 6.0f),
    CLOCKWORK_COLOSSUS_GEAR_PROJECTILE(BossKind.CLOCKWORK_COLOSSUS, 8.0f),
    ENTROPY_KEEPER_SLAM(BossKind.ENTROPY_KEEPER, 10.0f),
    TEMPORAL_PHANTOM_TIME_BLAST(BossKind.TEMPORAL_PHANTOM, 4.0f),
    TIME_TYRANT_AOE(BossKind.TIME_TYRANT, 12.0f);

    private final BossKind owner;
    private final float baseDamage;

    BossAbility(BossKind owner, float baseDamage) {
        this.owner = owner;
        this.baseDamage = baseDamage;
    }

    public BossKind owner() {
        return owner;
    }

    public float baseDamage() {
        return baseDamage;
    }
}
```

- [ ] **Step 5: Create `BossScaling`**

Create `common/shared/src/main/java/com/chronodawn/entities/bosses/BossScaling.java` with the LGPL header.

```java
package com.chronodawn.entities.bosses;

/**
 * Applies the configured multipliers to boss base statistics.
 *
 * <p>Every method reads {@code ChronoDawnConfig} at call time rather than
 * caching. That matters for {@link #ability}: a {@code static final} field
 * initialised at class load could run before the config is read, silently
 * capturing the default multiplier.
 */
public final class BossScaling {

    private BossScaling() {}

    /** Maximum health for the boss's {@code MAX_HEALTH} attribute. */
    public static double health(BossKind kind) {
        return kind.baseMaxHealth() * kind.settings().healthMultiplier();
    }

    /** Melee damage for the boss's {@code ATTACK_DAMAGE} attribute. */
    public static double attackDamage(BossKind kind) {
        return kind.baseAttackDamage() * kind.settings().damageMultiplier();
    }

    /** Damage for a non-attribute ability, scaled by its owner's multiplier. */
    public static float ability(BossAbility ability) {
        return (float) (ability.baseDamage() * ability.owner().settings().damageMultiplier());
    }
}
```

- [ ] **Step 6: Run the test to verify it passes**

```bash
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests '*BossScalingTest*'
```

Expected: PASS.

- [ ] **Step 7: Verify the oldest version compiles**

`ModEntityId` is shared, but confirm the new enum compiles against the 1.20.1 toolchain:

```bash
./gradlew build1_20_1
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/entities/bosses/ \
        common/shared/src/test/java/com/chronodawn/unit/BossScalingTest.java
git commit -m "feat(bosses): add shared base-stat table and scaling helper"
```

---

## Task 3: Route boss attributes through `BossScaling`

Replaces the `MAX_HEALTH` and `ATTACK_DAMAGE` literals in all 66 `createAttributes()` methods. After this task, health and melee damage respond to the config.

**Files:**
- Modify, for each of the eleven `<v>` values: `common/<v>/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`, `ChronosWardenEntity.java`, `ClockworkColossusEntity.java`, `EntropyKeeperEntity.java`, `TemporalPhantomEntity.java`, `TimeTyrantEntity.java`

**Interfaces:**
- Consumes: `BossScaling.health(BossKind)`, `BossScaling.attackDamage(BossKind)` (Task 2).
- Produces: nothing new.

**No imports are needed.** `BossKind` and `BossScaling` are in `com.chronodawn.entities.bosses`, the same package as every file edited here.

---

- [ ] **Step 1: Record the pre-change baseline**

```bash
grep -rn "Attributes.MAX_HEALTH\|Attributes.ATTACK_DAMAGE" common/*/src/main/java/com/chronodawn/entities/bosses/ | wc -l
```

Expected: `132` (6 bosses × 11 versions × 2 attributes). Note the number; Step 4 checks it went to zero.

- [ ] **Step 2: Apply the edit to every version**

For each `<v>` in `1.20.1 1.21.1 1.21.2 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11`, make exactly these six substitutions. Only the two listed lines change in each file — every other attribute line, comment, and the surrounding method stay byte-identical.

`TimeGuardianEntity.java`:

```java
            .add(Attributes.MAX_HEALTH, BossScaling.health(BossKind.TIME_GUARDIAN))
```
```java
            .add(Attributes.ATTACK_DAMAGE, BossScaling.attackDamage(BossKind.TIME_GUARDIAN))
```

`ChronosWardenEntity.java`:

```java
            .add(Attributes.MAX_HEALTH, BossScaling.health(BossKind.CHRONOS_WARDEN))
```
```java
            .add(Attributes.ATTACK_DAMAGE, BossScaling.attackDamage(BossKind.CHRONOS_WARDEN))
```

> **Do not touch this file's `MOVEMENT_SPEED` line.** It is `0.15` on 1.20.1 and `0.20` on 1.21.1+. That divergence predates this work and must survive it.

`ClockworkColossusEntity.java`:

```java
            .add(Attributes.MAX_HEALTH, BossScaling.health(BossKind.CLOCKWORK_COLOSSUS))
```
```java
            .add(Attributes.ATTACK_DAMAGE, BossScaling.attackDamage(BossKind.CLOCKWORK_COLOSSUS))
```

`EntropyKeeperEntity.java`:

```java
            .add(Attributes.MAX_HEALTH, BossScaling.health(BossKind.ENTROPY_KEEPER))
```
```java
            .add(Attributes.ATTACK_DAMAGE, BossScaling.attackDamage(BossKind.ENTROPY_KEEPER))
```

`TemporalPhantomEntity.java`:

```java
            .add(Attributes.MAX_HEALTH, BossScaling.health(BossKind.TEMPORAL_PHANTOM))
```
```java
            .add(Attributes.ATTACK_DAMAGE, BossScaling.attackDamage(BossKind.TEMPORAL_PHANTOM))
```

`TimeTyrantEntity.java`:

```java
            .add(Attributes.MAX_HEALTH, BossScaling.health(BossKind.TIME_TYRANT))
```
```java
            .add(Attributes.ATTACK_DAMAGE, BossScaling.attackDamage(BossKind.TIME_TYRANT))
```

The trailing `// 100 hearts` style comments on the original lines describe the literal that is being removed. Delete those comments along with the literal — the base value now lives in `BossKind` and a stale heart count here would drift.

- [ ] **Step 3: Verify no version module was missed**

```bash
grep -rn "Attributes.MAX_HEALTH, [0-9]\|Attributes.ATTACK_DAMAGE, [0-9]" common/*/src/main/java/com/chronodawn/entities/bosses/
```

Expected: **no output**. Any line printed is a version module that still has a hardcoded literal.

- [ ] **Step 4: Verify every boss now routes through the helper**

```bash
grep -rc "BossScaling.health\|BossScaling.attackDamage" common/*/src/main/java/com/chronodawn/entities/bosses/*Entity.java | grep -v ":2$"
```

Expected: **no output** — every one of the 66 boss files contains exactly two `BossScaling` calls. Any file listed has too few or too many.

- [ ] **Step 5: Build the oldest and newest versions**

```bash
./gradlew build1_20_1
```
```bash
./gradlew build1_21_11
```

Expected: BUILD SUCCESSFUL for both.

- [ ] **Step 6: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/
git commit -m "feat(bosses): scale boss health and melee damage by config"
```

---

## Task 4: Scale ability damage

Routes the five non-projectile ability damage sources through `BossScaling`. Each is currently a `static final` constant or an inline literal.

**Files:**
- Modify, for each of the eleven `<v>` values:
  - `common/<v>/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java`
  - `common/<v>/src/main/java/com/chronodawn/entities/bosses/TimeTyrantEntity.java`
  - `common/<v>/src/main/java/com/chronodawn/entities/bosses/ChronosWardenEntity.java`
  - `common/<v>/src/main/java/com/chronodawn/entities/bosses/ClockworkColossusEntity.java`
  - `common/<v>/src/main/java/com/chronodawn/entities/bosses/EntropyKeeperEntity.java`

**Interfaces:**
- Consumes: `BossScaling.ability(BossAbility)` (Task 2).
- Produces: nothing new.

**Line numbers differ between versions** (by up to two lines). Locate each site by its code text, not by line number.

---

- [ ] **Step 1: Confirm the site inventory**

```bash
grep -rn "AOE_DAMAGE\|GROUND_SLAM_DAMAGE\|mobAttack(this), 10.0f" common/*/src/main/java/com/chronodawn/entities/bosses/
```

Expected: 4 lines per version for the named constants (declaration + use, in `TimeGuardianEntity`, `TimeTyrantEntity`, `ChronosWardenEntity`, `ClockworkColossusEntity`) plus 1 line for the `EntropyKeeperEntity` inline literal — 9 lines per version, 99 in total. If a version shows a different count, inspect it before editing.

- [ ] **Step 2: `TimeGuardianEntity` — remove the constant, scale the use**

Delete this declaration:

```java
    public static final float AOE_DAMAGE = 6.0f; // 3 hearts - balanced for iron armor gameplay
```

Replace the use:

```java
            player.hurt(this.damageSources().mobAttack(this), AOE_DAMAGE);
```

with:

```java
            player.hurt(this.damageSources().mobAttack(this),
                BossScaling.ability(BossAbility.TIME_GUARDIAN_AOE));
```

Then confirm nothing else in the file referenced the constant:

```bash
grep -rn "AOE_DAMAGE" common/*/src/main/java/com/chronodawn/entities/bosses/TimeGuardianEntity.java
```

Expected: no output.

- [ ] **Step 3: `TimeTyrantEntity` — remove the constant, scale the use**

Delete this declaration:

```java
    public static final float AOE_DAMAGE = 12.0f; // 6 hearts
```

Replace:

```java
                float damage = AOE_DAMAGE;
```

with:

```java
                float damage = BossScaling.ability(BossAbility.TIME_TYRANT_AOE);
```

The Chrono Aegis halving that follows (`damage *= 0.5f;`) stays exactly as it is — it applies on top of the configured multiplier, which is the correct order.

- [ ] **Step 4: `ChronosWardenEntity` — remove the constant, scale the use**

Delete:

```java
    private static final float GROUND_SLAM_DAMAGE = 2.0f; // 1 heart
```

Replace:

```java
                entity.hurt(this.damageSources().mobAttack(this), GROUND_SLAM_DAMAGE);
```

with:

```java
                entity.hurt(this.damageSources().mobAttack(this),
                    BossScaling.ability(BossAbility.CHRONOS_WARDEN_GROUND_SLAM));
```

- [ ] **Step 5: `ClockworkColossusEntity` — remove the constant, scale the use**

Delete:

```java
    private static final float GROUND_SLAM_DAMAGE = 6.0f;
```

Replace:

```java
                    player.hurt(this.damageSources().mobAttack(this), GROUND_SLAM_DAMAGE);
```

with:

```java
                    player.hurt(this.damageSources().mobAttack(this),
                        BossScaling.ability(BossAbility.CLOCKWORK_COLOSSUS_GROUND_SLAM));
```

- [ ] **Step 6: `EntropyKeeperEntity` — scale the inline literal**

Replace:

```java
            entity.hurt(this.damageSources().mobAttack(this), 10.0f);
```

with:

```java
            entity.hurt(this.damageSources().mobAttack(this),
                BossScaling.ability(BossAbility.ENTROPY_KEEPER_SLAM));
```

- [ ] **Step 7: Verify no ability literal survives**

```bash
grep -rn "AOE_DAMAGE\|GROUND_SLAM_DAMAGE\|mobAttack(this), 10.0f" common/*/src/main/java/com/chronodawn/entities/bosses/
```

Expected: **no output**.

```bash
grep -rc "BossScaling.ability" common/*/src/main/java/com/chronodawn/entities/bosses/*Entity.java | grep -v ":0$\|:1$"
```

Expected: **no output** — each of the five affected files has exactly one `BossScaling.ability` call, and `TemporalPhantomEntity` has none (its only extra damage source is the Time Blast projectile, handled in Task 5).

- [ ] **Step 8: Build both ends of the matrix**

```bash
./gradlew build1_20_1
```
```bash
./gradlew build1_21_11
```

Expected: BUILD SUCCESSFUL for both.

- [ ] **Step 9: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/entities/bosses/
git commit -m "feat(bosses): scale boss ability damage by config"
```

---

## Task 5: Scale projectile damage via owner lookup

`TimeBlastEntity` is fired by both Time Guardian and Temporal Phantom, so its damage cannot be fixed at construction. Both projectiles resolve their shooter at impact.

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/entities/bosses/BossProjectileDamage.java`
- Modify, for each of the eleven `<v>` values:
  - `common/<v>/src/main/java/com/chronodawn/entities/projectiles/TimeBlastEntity.java`
  - `common/<v>/src/main/java/com/chronodawn/entities/projectiles/GearProjectileEntity.java`

**Interfaces:**
- Consumes: `BossScaling.ability(BossAbility)`, `BossAbility` (Task 2).
- Produces:
  - `BossProjectileDamage.timeBlast(Entity owner)` → `float`
  - `BossProjectileDamage.gearProjectile(Entity owner)` → `float`

---

- [ ] **Step 1: Create `BossProjectileDamage`**

Create `common/shared/src/main/java/com/chronodawn/entities/bosses/BossProjectileDamage.java` with the LGPL header. This class is separate from `BossScaling` on purpose: it imports `net.minecraft.world.entity.Entity`, and keeping `BossScaling` free of Minecraft types is what lets `BossScalingTest` run as a plain JUnit test.

```java
package com.chronodawn.entities.bosses;

import net.minecraft.world.entity.Entity;

/**
 * Resolves a projectile's damage from the boss that fired it.
 *
 * <p>Time Blast is fired by both Time Guardian and Temporal Phantom, so the
 * projectile cannot know its multiplier at construction time — it has to ask
 * its owner at impact. Gear Projectile has a single owner today but uses the
 * same path so the two do not drift apart.
 *
 * <p>An unowned projectile (owner despawned, or spawned by a command) falls
 * back to the unscaled base damage.
 */
public final class BossProjectileDamage {

    private BossProjectileDamage() {}

    public static float timeBlast(Entity owner) {
        if (owner instanceof TimeGuardianEntity) {
            return BossScaling.ability(BossAbility.TIME_GUARDIAN_TIME_BLAST);
        }
        if (owner instanceof TemporalPhantomEntity) {
            return BossScaling.ability(BossAbility.TEMPORAL_PHANTOM_TIME_BLAST);
        }
        return BossAbility.TIME_GUARDIAN_TIME_BLAST.baseDamage();
    }

    public static float gearProjectile(Entity owner) {
        if (owner instanceof ClockworkColossusEntity) {
            return BossScaling.ability(BossAbility.CLOCKWORK_COLOSSUS_GEAR_PROJECTILE);
        }
        return BossAbility.CLOCKWORK_COLOSSUS_GEAR_PROJECTILE.baseDamage();
    }
}
```

- [ ] **Step 2: Scale `TimeBlastEntity` in every version**

For each `<v>`, in `common/<v>/src/main/java/com/chronodawn/entities/projectiles/TimeBlastEntity.java`, add the import next to the existing ones:

```java
import com.chronodawn.entities.bosses.BossProjectileDamage;
```

Replace:

```java
            // Damage the target (4.0 = 2 hearts)
            // In 1.21.2, hurt() returns void
            target.hurt(this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), 4.0f);
```

with:

```java
            // In 1.21.2, hurt() returns void
            target.hurt(
                this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()),
                BossProjectileDamage.timeBlast(this.getOwner())
            );
```

The `(LivingEntity) this.getOwner()` cast in the damage-source argument is pre-existing. Leave it exactly as found — changing it is out of scope for this plan.

- [ ] **Step 3: Scale `GearProjectileEntity` in every version**

For each `<v>`, in `common/<v>/src/main/java/com/chronodawn/entities/projectiles/GearProjectileEntity.java`, add the import:

```java
import com.chronodawn.entities.bosses.BossProjectileDamage;
```

Delete the declaration:

```java
    private static final float DAMAGE = 8.0f;
```

Replace:

```java
        entity.hurt(damageSource, DAMAGE);
```

with:

```java
        entity.hurt(damageSource, BossProjectileDamage.gearProjectile(owner));
```

The local `Entity owner = this.getOwner();` is already declared a few lines above this call — reuse it rather than calling `getOwner()` again.

- [ ] **Step 4: Verify no projectile literal survives**

```bash
grep -rn "mobProjectile(this, (LivingEntity) this.getOwner()), 4.0f\|float DAMAGE = 8.0f\|hurt(damageSource, DAMAGE)" common/*/src/main/java/com/chronodawn/entities/projectiles/
```

Expected: **no output**.

```bash
grep -rc "BossProjectileDamage" common/*/src/main/java/com/chronodawn/entities/projectiles/TimeBlastEntity.java common/*/src/main/java/com/chronodawn/entities/projectiles/GearProjectileEntity.java | grep -v ":2$"
```

Expected: **no output** — every file has exactly two occurrences (the import and the call).

- [ ] **Step 5: Build both ends of the matrix**

```bash
./gradlew build1_20_1
```
```bash
./gradlew build1_21_11
```

Expected: BUILD SUCCESSFUL for both.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/entities/bosses/BossProjectileDamage.java \
        common/*/src/main/java/com/chronodawn/entities/projectiles/
git commit -m "feat(bosses): scale boss projectile damage by config"
```

---

## Task 6: GameTest coverage for the attribute path

Unit tests prove the arithmetic; this proves the value actually reaches the entity through each loader's attribute-registration path.

**Files:**
- Modify, for each of the eleven `<v>` values: `common/<v>/src/main/java/com/chronodawn/gametest/boss/BossFightTestLogic.java`

**Interfaces:**
- Consumes: `BossKind.baseMaxHealth()` (Task 2), `BossScaling` wiring (Task 3).
- Produces: GameTests named `boss_time_guardian_base_max_health` and `boss_time_tyrant_base_max_health`.

**Per-version API note:** `helper.fail(...)` takes a `Component` on newer versions and a `String` on older ones. Match whatever the surrounding tests in the same file already use — do not introduce a new call style.

---

- [ ] **Step 1: Add the test to `generateTimeGuardianTests()`**

In each version's `BossFightTestLogic.java`, add this test to the `generateTimeGuardianTests()` list, following the existing `tests.add(new NamedTest(...))` idiom. The version below uses the `Component.literal` form; on versions whose file passes a bare `String` to `fail`, drop `Component.literal(...)` and pass the string directly.

```java
        // Default config must leave the shipped balance untouched. This is the
        // runtime half of BossScalingTest: it proves the value survives the
        // loader's attribute-registration path, not just the arithmetic.
        tests.add(new NamedTest("boss_time_guardian_base_max_health", helper -> {
            var guardian = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

            helper.runAfterDelay(2, () -> {
                float expected = (float) BossKind.TIME_GUARDIAN.baseMaxHealth();
                if (guardian.getMaxHealth() == expected) {
                    helper.succeed();
                } else {
                    helper.fail(Component.literal("Time Guardian max health should be " +
                        expected + " under default config, was " + guardian.getMaxHealth()));
                }
            });
        }, BOSS_TIMEOUT));
```

- [ ] **Step 2: Add the test to `generateTimeTyrantTests()`**

```java
        tests.add(new NamedTest("boss_time_tyrant_base_max_health", helper -> {
            var tyrant = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

            helper.runAfterDelay(2, () -> {
                float expected = (float) BossKind.TIME_TYRANT.baseMaxHealth();
                if (tyrant.getMaxHealth() == expected) {
                    helper.succeed();
                } else {
                    helper.fail(Component.literal("Time Tyrant max health should be " +
                        expected + " under default config, was " + tyrant.getMaxHealth()));
                }
            });
        }, BOSS_TIMEOUT));
```

- [ ] **Step 3: Add the import**

`BossFightTestLogic` is in `com.chronodawn.gametest.boss`, not the bosses package, so the import is required:

```java
import com.chronodawn.entities.bosses.BossKind;
```

- [ ] **Step 4: Verify all eleven versions were edited**

```bash
grep -rc "base_max_health" common/*/src/main/java/com/chronodawn/gametest/boss/BossFightTestLogic.java | grep -v ":2$"
```

Expected: **no output** — each file contains both new test names.

- [ ] **Step 5: Run the GameTests on one version per loader**

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11
```
```bash
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.11
```

Expected: both pass, including `boss_time_guardian_base_max_health` and `boss_time_tyrant_base_max_health`. Then confirm the oldest version, where the attribute API differs most:

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/gametest/boss/BossFightTestLogic.java
git commit -m "test(bosses): assert default boss max health at runtime"
```

---

## Task 7: Measure existing-world behavior and finish documentation

The design spec deliberately left one fact unmeasured: whether a boss already present in a saved world picks up a changed multiplier. This task measures it and writes down what was observed — an unverified row in the caveats table would be worse than no row.

**Files:**
- Modify: `docs/modpack-integration.md`
- Modify: `CHANGELOG.md`
- Modify: `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`

**Interfaces:**
- Consumes: everything from Tasks 1–6.
- Produces: nothing code-facing.

---

- [ ] **Step 1: Manual verification — new spawns**

```bash
./gradlew runClientFabric1_21_11
```

In the running client:
1. Quit to the title screen once so the config file is written, then edit `fabric/1.21.11/run/config/chronodawn.toml` and set, under `[gameplay.bosses.time_guardian]`, `health_multiplier = 2.0` and `damage_multiplier = 2.0`.
2. Restart the client.
3. Create a creative test world, `/summon chronodawn:time_guardian`.
4. Confirm the boss bar shows 400 health (200 × 2.0).
5. Switch to survival, let the Guardian's area attack hit you, and confirm the damage is roughly double the 6.0 base.

Expected: both scale.

Then repeat steps 1–4 (the boss-bar check is enough; the damage check does not need repeating) on the other loader and on the oldest 1.21 line, because the attribute-registration path differs per loader and the entity API differs most at 1.21.1:

```bash
./gradlew runClientNeoForge1_21_11
```
```bash
./gradlew runClientFabric1_21_1
```
```bash
./gradlew runClientNeoForge1_21_1
```

Each writes its config under the matching `<loader>/<version>/run/config/chronodawn.toml`. Expected: the boss bar shows 400 health in all four combinations.

- [ ] **Step 2: Manual verification — existing spawns**

1. With the boss from Step 1 still alive, save and quit.
2. Set `health_multiplier = 4.0` in the same config file.
3. Restart and reload the same world.
4. Record what the already-spawned boss's max health is: still 400, or now 800?

Write down the observed answer — it decides the wording in Step 3.

- [ ] **Step 3: Update the modpack integration guide**

In `docs/modpack-integration.md`, add a row to the "Restart and existing-world caveats" table. Use the phrasing that matches what Step 2 actually showed:

If the already-spawned boss kept its old health:

```markdown
| `gameplay.bosses.*` | yes (server / world reload) | no — only bosses spawned after the change |
```

If it picked up the new value:

```markdown
| `gameplay.bosses.*` | yes (server / world reload) | yes — already-spawned bosses are rescaled on load |
```

Then add a worked example to the same document, after the existing ore examples:

````markdown
### Example: place the bosses later in your progression

```toml
# Chrono Dawn's bosses sit mid-game by default. If your pack gates the
# Chrono dimension behind endgame gear, scale them up.
[gameplay.bosses.time_guardian]
health_multiplier = 2.0
damage_multiplier = 1.5

[gameplay.bosses.time_tyrant]
health_multiplier = 2.5
damage_multiplier = 1.5
```

`damage_multiplier` covers melee, area attacks and projectiles together,
so the encounter stays internally consistent. Setting it to `0.0` leaves
the boss present but harmless — useful for cinematic or story-driven
packs. See [`configuration.md`](configuration.md) for the full base-stat
table.
````

- [ ] **Step 4: Update the changelog**

Add to the unreleased section of `CHANGELOG.md`, matching the surrounding entry style:

```markdown
- Added per-boss `health_multiplier` and `damage_multiplier` config options
  under `[gameplay.bosses.*]` for all six bosses. `damage_multiplier` covers
  melee, ability and projectile damage. Defaults reproduce the existing
  balance exactly.
```

- [ ] **Step 5: Update the roadmap status tracker**

In `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`:

Under "Planned follow-up tunables", change the `Boss HP / damage multipliers` bullet to record that it shipped, with a link to this slice's design spec:

```markdown
- Boss HP / damage multipliers — *shipped: per-boss `health_multiplier` /
  `damage_multiplier` for all six bosses ([design](./2026-07-25-boss-stat-multipliers-design.md))*
```

Update the sub-project A status line and the status-tracker table row to say four PRs have shipped (Ancient Ruins, Ore generation tuning, Clockstone tuning, Boss multipliers), adding the same design-spec link.

- [ ] **Step 6: Mark the design spec as implemented**

In `docs/superpowers/specs/2026-07-25-boss-stat-multipliers-design.md`, change the status line to:

```markdown
**Status**: Implemented
```

- [ ] **Step 7: Run the full verification matrix**

```bash
./gradlew checkAll
```

Expected: BUILD SUCCESSFUL. If the wrapper reports a failure, re-run the specific version standalone before treating it as real — `buildAll` / `gameTestAll` have a known spurious-failure mode.

- [ ] **Step 8: Commit**

```bash
git add docs/ CHANGELOG.md
git commit -m "docs(config): document boss health and damage multipliers"
```

---

## Completion checklist

- [ ] `[gameplay.bosses.*]` parses, validates per-field, and defaults to a no-op
- [ ] No `MAX_HEALTH` / `ATTACK_DAMAGE` literal remains in any boss `createAttributes()`
- [ ] All eight `BossAbility` damage sources scale with `damage_multiplier`
- [ ] Existing-world behavior measured and written into the caveats table
- [ ] `./gradlew checkAll` passes
- [ ] `docs/configuration.md`, `docs/modpack-integration.md`, `CHANGELOG.md` and the roadmap updated
