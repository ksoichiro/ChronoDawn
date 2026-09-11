# Design: Multiplayer Boss Targeting and Aggro Distribution

**Created**: 2026-09-11
**Status**: Draft
**Related**: [Boss HP / Damage Multipliers](./2026-07-25-boss-stat-multipliers-design.md), [flagship-parity roadmap](./2026-07-05-flagship-parity-roadmap.md) P3 (player-count HP/damage scaling — explicitly out of scope here, tracked separately)

---

## Why

In multiplayer, all six bosses use the vanilla `NearestAttackableTargetGoal` /
`HurtByTargetGoal` pair for `targetSelector`. Once a target is set, it does not
change when a *different* player hits the boss — `HurtByTargetGoal` only takes
effect while the boss has no target, not as an interrupt. The practical result:
a boss locks onto whichever player it saw first and keeps attacking that player
even while everyone else hits it for free. This makes fights feel
single-target and reduces effective difficulty far below what the boss's
stats suggest, defeating the purpose of a "boss" encounter in a group.

This design closes that gap without touching boss stats (HP/damage), which
are already covered by the multiplier system. It is behavioral: who the boss
attacks, and — for the bosses whose kit allows it — how it spreads damage
across a group.

## Scope

Six boss entities under `com.chronodawn.entities.bosses`, all twelve version
modules (1.20.1, 1.21.1 through 1.21.11, 26.1.2, 26.2 — see "Version
coverage" below).

## Components

### 1. Immediate target switch (all six bosses, always active)

Each boss entity overrides `Mob.setLastHurtByMob(LivingEntity)`:

```java
@Override
public void setLastHurtByMob(LivingEntity entity) {
    super.setLastHurtByMob(entity);
    if (entity instanceof Player) {
        this.setTarget(entity);
    }
}
```

This is called by vanilla damage handling whenever a `LivingEntity` damages
the boss, before AI goals tick — so the switch takes effect the same tick the
hit lands, regardless of what `targetSelector` currently has locked in.
`HurtByTargetGoal` remains in place for non-player attackers (nothing to
change there); this override only concerns player-initiated switches.

**Why not centralize this in a shared base class**: none of the six boss
classes currently share a common abstract superclass — they each extend
`Monster` (or implement `RangedAttackMob`) directly, and introducing a shared
base now would be a much larger refactor across 6 classes × 12 version
modules for a 4-line method. The duplication is accepted, matching how
`createAttributes()` was duplicated before `BossKind` centralized the
*numbers* (not the code) in the 2026-07-25 multiplier design. `Mob` and
`LivingEntity` are stable types across the whole version matrix, so there is
no version-specific branching inside the override itself.

**Singleplayer impact**: none. The attacker is already the target in a
one-player fight, so the override is a no-op in practice.

### 2. Multiplayer detection helper

New shared class `common/shared/.../entities/bosses/BossMultiplayer`:

```java
public final class BossMultiplayer {
    private BossMultiplayer() {}

    public static boolean isMultiplayerEncounter(ServerBossEvent bossEvent) {
        return bossEvent.getPlayers().size() >= 2;
    }
}
```

`ServerBossEvent.getPlayers()` already tracks every player currently viewing
the boss bar (i.e., participating in the fight) and is stable across the
supported version range — it needs no version-specific handling, so unlike
`BossScaling` this helper is usable as-is without a `compat/` layer. Each
boss's `serverAiStep()` (or equivalent tick method) calls this once per check
rather than caching a value at spawn, so a player joining or leaving mid-fight
takes effect immediately.

### 3. AoE cooldown reduction (multiplayer only, existing AoE abilities only)

No new AoE abilities are added. Each boss that already has a cooldown-gated
area attack gets a second, shorter cooldown constant used only when
`BossMultiplayer.isMultiplayerEncounter()` is true at the moment the ability
goes back on cooldown:

| Boss | Ability | Current cooldown | Multiplayer cooldown |
| --- | --- | --- | --- |
| Time Guardian | Phase 2 AoE | 80 ticks (4s) | 56 ticks (2.8s) |
| Chronos Warden | Ground Slam | 200/140 ticks (Phase 1/2) | 140/100 ticks |
| Time Tyrant | Phase 3 AoE | 120 ticks (6s) | 84 ticks (4.2s) |

Entropy Keeper's Entropy Burst is currently one-time (fires once at 30% HP,
guarded by `entropyBurstTriggered`). In a multiplayer encounter, it becomes
repeatable: after the first trigger, re-arm it on a 1200-tick (60s) timer
instead of leaving `entropyBurstTriggered` permanently `true`. In singleplayer
it stays exactly as it is today (fires once).

Exact tick values are implementation-time tuning, not a config surface — they
are internal balance constants, not part of `[gameplay.bosses.*]`. Numbers
above are the starting point; adjust during implementation/testing without
returning to this design.

**Scope of the change**: this only shortens the *cooldown*, not the ability
itself — range, damage, and trigger conditions (phase, distance to target)
are unchanged. The multiplier system already scales the damage; this design
only changes cadence.

**Bosses with no AoE ability today** (Clockwork Colossus, Temporal Phantom)
get no new mechanic. Both already have a ranged attack (`GearProjectileEntity`,
`TimeBlastEntity`) that follows whatever `getTarget()` currently is, so the
immediate target switch (Component 1) is what makes their existing kit track
a newly-aggroed player.

### 4. Time Tyrant: Chronal Leech reinforcements

On each phase transition (Phase 1→2 and Phase 2→3 — two occurrences per
fight, no repeating timer), if `BossMultiplayer.isMultiplayerEncounter()` is
true, Time Tyrant summons 3 `ChronalLeechEntity` at randomized positions near
itself. `ChronalLeechEntity` is an existing weak mob (10 HP, 2 damage, already
designed to spawn in groups) — no new entity type is introduced.

Summoned leeches are ordinary `Monster`s with no persistence override; they
follow standard despawn rules like any other Chrono Dawn hostile mob. No
explicit tracking or cleanup on boss death — this is a deliberate YAGNI call,
not an oversight: at most 6 leeches ever exist per fight (3 per transition ×
2 transitions), which is a bounded, small number, and they are killable like
any other mob if a fight ends early.

In singleplayer, this component never fires.

## Version coverage

All twelve boss-bearing modules (1.20.1, 1.21.1–1.21.11, 26.1.2, 26.2) get
all four components. `1.21.3` shares `1.21.2`'s common module, so no
separate edit is needed there.

## Testing

- **Unit tests** (`common/shared/src/test`): `BossMultiplayer.isMultiplayerEncounter()`
  boundary cases (0, 1, 2, 3+ players). This is the only component here that
  is pure logic with no Minecraft bootstrap dependency, so it is the only
  piece covered by an automated test with real assertions.
- **No GameTest coverage for target-switching or the summon trigger.** This
  project's GameTest suite (`common/gametest`) has no fake-player
  infrastructure — every existing GameTest exercises mobs, blocks, or
  structures, never simulated players initiating combat. Building that
  infrastructure to test a two-player aggro scenario is a separate,
  nontrivial piece of work and is not part of this slice. Correctness here
  rests on manual verification instead.
- **Manual verification** (required, not optional, given the lack of
  automated coverage): two-client multiplayer session against each boss,
  confirm attacking a boss that is chasing another player pulls aggro
  immediately, and that AoE/Ground Slam/Entropy Burst fire noticeably more
  often than in a solo fight; confirm Time Tyrant spawns 3 Chronal Leeches at
  each phase transition with 2+ players present and none with 1.
- **Full matrix**: `./gradlew checkAll` must pass — this touches all twelve
  boss-bearing version modules. This catches compile errors and the existing
  regression suite, but not the new behavior itself.

## Out of scope

- Player-count-based HP/damage scaling — tracked separately under the
  flagship-parity roadmap P3, builds on the existing multiplier config
  surface instead of this design's mechanisms.
- New AoE abilities for Clockwork Colossus or Temporal Phantom.
- New mob types for the Time Tyrant summon (reuses `ChronalLeechEntity`).
- Config toggles for any of the above — these are internal balance
  constants, not modpack-author-facing tunables, at least for this slice.
- Explicit lifecycle management (tracking/despawn-on-boss-death) for summoned
  Chronal Leeches.

## Done criteria

- All six bosses override `setLastHurtByMob` to switch target immediately
  when hit by a player, in every boss-bearing version module.
- `BossMultiplayer.isMultiplayerEncounter()` exists in `common/shared` and is
  used by every AoE-cooldown and summon check described above.
- Time Guardian, Chronos Warden, Time Tyrant AoE cooldowns and Entropy
  Keeper's Entropy Burst behave as specified when 2+ players participate, and
  are unchanged in singleplayer.
- Time Tyrant summons 3 Chronal Leeches on each of its two phase transitions
  when 2+ players participate, and summons none in singleplayer.
- `./gradlew checkAll` passes.
- CHANGELOG updated.
