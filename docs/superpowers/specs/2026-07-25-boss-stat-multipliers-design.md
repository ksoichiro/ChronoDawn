# Design: Boss HP / Damage Multipliers

**Created**: 2026-07-25
**Status**: Implemented
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A (config system)
**Builds on**: [Config foundation](./2026-05-09-config-foundation-design.md)

---

## Why

Sub-project A's roadmap lists "Boss HP / damage multipliers" as the next tunable
after the shipped ore-generation slices. Modpack authors place Chrono Dawn's
bosses at different points in their own progression curve: a pack that gates the
Chrono dimension behind endgame gear needs tougher bosses, and an
exploration-focused pack needs softer ones. Without a multiplier the only
recourse is a source patch, which pack authors will not do.

This slice adds per-boss `health_multiplier` and `damage_multiplier` to
`chronodawn.toml`.

## Scope

Six boss entities, all under `com.chronodawn.entities.bosses`:

| Boss | Config key | MAX_HEALTH | ATTACK_DAMAGE |
| --- | --- | --- | --- |
| Time Guardian | `time_guardian` | 200.0 | 10.0 |
| Chronos Warden | `chronos_warden` | 180.0 | 9.0 |
| Clockwork Colossus | `clockwork_colossus` | 200.0 | 12.0 |
| Entropy Keeper | `entropy_keeper` | 160.0 | 10.0 |
| Temporal Phantom | `temporal_phantom` | 150.0 | 8.0 |
| Time Tyrant | `time_tyrant` | 500.0 | 18.0 |

`damage_multiplier` covers **every** damage source a boss deals, not only the
melee attribute. Partial coverage would be actively misleading: halving a boss's
damage while its area-of-effect attack still one-shots the player is worse than
no option at all. The non-attribute damage sources are:

| Source | Base | Owner |
| --- | --- | --- |
| `TimeGuardianEntity.AOE_DAMAGE` | 6.0 | Time Guardian |
| `TimeTyrantEntity.AOE_DAMAGE` | 12.0 | Time Tyrant |
| `ClockworkColossusEntity.GROUND_SLAM_DAMAGE` | 6.0 | Clockwork Colossus |
| `ChronosWardenEntity.GROUND_SLAM_DAMAGE` | 2.0 | Chronos Warden |
| `EntropyKeeperEntity` inline slam damage | 10.0 | Entropy Keeper |
| `GearProjectileEntity.DAMAGE` | 8.0 | Clockwork Colossus |
| `TimeBlastEntity` impact damage | 4.0 | Time Guardian **and** Temporal Phantom |
| `EntropyKeeperEntity.applyDegradation()` runtime `ATTACK_DAMAGE` write | 10.0 baseline + 2.0 per stack | Entropy Keeper |

The table above was read from the 1.21.11 module. The implementation plan must
confirm the same set exists — with the same values — in every version module
before assuming the edit is uniform.

The last row was found during implementation, not during design: Entropy
Keeper's Phase 2 degradation mechanic overwrites the `ATTACK_DAMAGE` base value
at runtime with a hardcoded formula. Any damage source that *writes* an
attribute rather than reading one has to be scaled too, or the multiplier is
silently discarded the moment the mechanic fires. Both the baseline and the
per-stack step scale, so the degradation curve keeps its shape.

## Config schema

A new top-level `[gameplay]` section, matching the shape the config-foundation
spec anticipated (`gameplay.bosses.<entity_id>`):

```toml
[gameplay.bosses.time_guardian]
health_multiplier = 1.0
damage_multiplier = 1.0

# ... same two keys for chronos_warden, clockwork_colossus,
#     entropy_keeper, temporal_phantom, time_tyrant
```

Config keys equal the entity registration IDs in `ModEntityId`, so a pack author
who knows the entity ID can guess the config key.

`schema_version` stays **1**. The addition is purely additive: every key is
optional and its default reproduces current behavior exactly, so an existing
`chronodawn.toml` keeps working untouched.

### Java records

Mirrors the existing `OresConfig` / `OreSettings` pair so the two tunables read
the same way:

- `ChronoDawnConfig` gains a `Gameplay gameplay` component
- `Gameplay` holds `BossesConfig bosses`
- `BossesConfig` holds six `BossSettings`
- `BossSettings(double healthMultiplier, double damageMultiplier)`

`ConfigDefaults` gains six entries, all `1.0` / `1.0`.

### Validation

Follows the established per-field rule: an out-of-range value logs at ERROR and
reverts **that field only** to its default, so one typo does not reset the rest
of the file.

| Field | Valid range | Rationale |
| --- | --- | --- |
| `health_multiplier` | `[0.1, 10.0]` | Zero would give a max health of 0 — instant death or an unstable entity. |
| `damage_multiplier` | `[0.0, 10.0]` | Zero is safe and meaningful: story- and exploration-focused packs can keep the boss encounter as spectacle without lethality. |

`NaN` and the infinities are rejected by the same range check and revert to the
default.

`ConfigLoader`'s unknown-top-level-key warning must learn about `gameplay`, or
it will warn about a key it just parsed.

## Components

Three new classes in `common/shared`, therefore compiled into every version
module:

- **`BossKind`** — enum of the six bosses; carries the config key and the base
  `MAX_HEALTH` / `ATTACK_DAMAGE`, and resolves its own `BossSettings`.
- **`BossAbility`** — enum of every damage source that is not the
  `ATTACK_DAMAGE` attribute, each tied to its owning `BossKind`. Time Blast
  appears twice because two bosses fire it and each scales independently.
- **`BossScaling`** — `health(kind)`, `attackDamage(kind)`, and
  `ability(ability)`; each multiplies the base value by the configured
  multiplier read from `ChronoDawnConfig.get()`.

Together `BossKind` and `BossAbility` are the single definition site for every
boss base number.

None of these touch a Minecraft API — they are `double` arithmetic over an enum.
That is what makes them shareable: `Attributes.MAX_HEALTH` is an `Attribute` on
1.20.1 and a `Holder<Attribute>` from 1.21.1 onward, so any shared helper that
handled attribute objects directly would not compile across the matrix. Keeping
the shared layer numeric side-steps the split entirely.

### Removing existing duplication

`createAttributes()` is currently byte-identical across all eleven version
modules for every boss — the same literals repeated 66 times. Routing them
through `BossKind` collapses that to one definition site. This is not
incidental cleanup; it is the reason the change is safe to make at this scale,
because a version module that was missed retains a numeric literal and is
therefore greppable.

## Data flow

```
chronodawn.toml
      ↓  ConfigLoader.load()   (called from ChronoDawn.init())
ChronoDawnConfig.get()
      ↓
  BossScaling
      ├── createAttributes()      — once at startup
      ├── ability damage          — at ability use
      └── projectile damage       — at impact, via owner lookup
```

**Attributes.** Each version's `createAttributes()` becomes
`.add(Attributes.MAX_HEALTH, BossScaling.health(BossKind.TIME_GUARDIAN))` and
similar. Ordering is safe on both loaders and was verified against the sources:

- NeoForge — `ChronoDawn.init()` runs in the mod constructor;
  `registerEntityAttributes` is a listener registered afterwards on the mod event
  bus and fires later.
- Fabric — `onInitialize()` calls `ChronoDawn.init()` before
  `registerEntityAttributes()`.

**Ability damage.** The `static final` ability constants must **not** simply be
multiplied in place. A `static final` initialiser runs at class load, which may
precede config load (entity-type registration can trigger class initialisation),
so a baked-in constant risks capturing the default multiplier. The constants
therefore stay as base values and the multiplier is applied at each use site via
`BossScaling.ability(...)`.

**Projectiles.** `TimeBlastEntity` is fired by both Time Guardian and Temporal
Phantom, so its damage cannot be scaled by a single boss's multiplier at
construction time. Instead the projectile resolves its shooter through
`getOwner()` at impact and applies that boss's multiplier.
`GearProjectileEntity` has only one owner today (Clockwork Colossus) but uses
the same path, so the two projectiles do not diverge.

## Behavior boundaries

**Existing worlds.** Measured on 1.21.11 Fabric, 2026-07-26 — and the
measurement contradicted this spec's original expectation, which was that
already-spawned bosses would keep their old statistics because attribute base
values persist in entity NBT.

They do not. Raising `health_multiplier` from `2.0` to `4.0` and reloading the
same save took an existing Time Guardian's max health from 400 to 800. **Current
health is preserved; only the maximum changes.** Damage scaling was confirmed at
the same time: max health and area-of-effect damage both doubled at `2.0`.

That combination has a consequence worth stating plainly, because it is not
obvious from the option's name: **every boss phase is a health *ratio***
(`getHealth() / getMaxHealth()` — Time Guardian flips at 50%, Time Tyrant at
66% and 33%, with Time Reversal at 20%). Doubling the maximum while preserving
current health halves the ratio, so an existing boss can jump straight into a
later phase the moment the save reloads. Lowering the multiplier does the
reverse, and Minecraft clamps current health down if it now exceeds the new
maximum.

This is why the spec required the measurement instead of accepting the
expectation: the guessed answer would have produced a caveats row that was not
merely incomplete but backwards.

**Restart required.** The config is read once at startup, as with every existing
option.

## Testing

**Unit tests** (`common/shared/src/test`, extending the `ConfigLoaderTest`
patterns):

- Parsing all six bosses; defaults when the `[gameplay]` section is absent
  entirely; defaults when a single boss table is absent
- Out-of-range values reverting per-field; `NaN` and infinities rejected
- `BossScaling` arithmetic at the boundaries (`0.1`, `10.0`, and `0.0` for
  damage)
- **Identity test**: with all multipliers at their `1.0` default,
  `BossScaling`'s output equals the `BossKind` / `BossAbility` base values
  exactly. This pins the contract that users who never touch the config see no
  change whatsoever.

**Literal-removal check.** Because the change deletes literals rather than
adding them, a grep over `entities/bosses/` for a numeric literal following
`Attributes.MAX_HEALTH,` finds any version module that was missed. The
implementation plan carries this as an explicit step.

**GameTest.** Summon each boss under default config and assert `getMaxHealth()`
matches `BossKind` — runtime proof that the attribute-registration path is
intact on both loaders. Non-default multipliers are not GameTested: the config
file is not readily swappable inside the GameTest harness, and the unit tests
already cover the arithmetic.

**Manual verification.** On 1.21.1 and 1.21.11, Fabric and NeoForge: set
`health_multiplier = 2.0` for Time Guardian, confirm the boss bar reflects
double health, and confirm AOE damage taken doubles at
`damage_multiplier = 2.0`.

**Full matrix.** `./gradlew checkAll` must pass. Verifying a single version is
not sufficient for a change that touches all eleven modules.

## Out of scope

- Multipliers for `ARMOR`, `KNOCKBACK_RESISTANCE`, `MOVEMENT_SPEED` — the
  roadmap entry is HP and damage; add on request
- A per-boss disable toggle — bosses gate progression (portals, advancements),
  so disabling one breaks the pack rather than tuning it
- Multipliers for non-boss mobs — separate scope
- Multiplayer player-count scaling — tracked separately in the
  [flagship-parity roadmap](./2026-07-05-flagship-parity-roadmap.md) P3; it will
  build on the config surface this slice establishes
- Hot reload — the config is startup-read by design

## Documentation

- `docs/configuration.md`: new `### [gameplay.bosses.*]` section (the canonical
  reference for what is configurable)
- `common/shared/src/main/resources/chronodawn-default-config.toml`: commented
  defaults — every user-facing field carries an inline comment, per the config
  foundation contract
- `docs/modpack-integration.md`: a rebalancing example, plus the existing-world
  caveat row
- `CHANGELOG.md`
- `2026-05-09-modpack-author-readiness-roadmap.md`: status tracker updated with
  this slice

## Done criteria

- Config parses, validates, and defaults correctly; unit tests pass
- All eleven version modules route boss statistics through `BossKind`, with
  no numeric literals left in `createAttributes()` for the six bosses
- All eight non-attribute damage sources scale with `damage_multiplier`
- Existing-world behavior measured and documented
- `./gradlew checkAll` passes
- Documentation and CHANGELOG updated
