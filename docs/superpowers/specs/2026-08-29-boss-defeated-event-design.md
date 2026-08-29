# Design: Boss Defeated Event API

**Created**: 2026-08-29
**Status**: Implemented
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project C

---

## Why

Quest-driven modpacks need a stable way to react when Chrono Dawn progression
changes. Today a pack can observe item drops or advancements, but neither is a
reliable boss-defeat signal: loot can be changed by a datapack, advancements can
be granted by commands, and both lose the actual defeated boss and damage
context.

This is the first slice of the scripting-events sub-project. It publishes a
loader-neutral Java event for all six Chrono Dawn bosses. KubeJS,
CraftTweaker, FTB Quests bridges and other addons can build on that contract
without reflecting into entity implementation classes.

## Scope

The event covers the six registered boss entity types:

| Boss | Stable event ID |
| --- | --- |
| Time Guardian | `chronodawn:time_guardian` |
| Chronos Warden | `chronodawn:chronos_warden` |
| Clockwork Colossus | `chronodawn:clockwork_colossus` |
| Entropy Keeper | `chronodawn:entropy_keeper` |
| Temporal Phantom | `chronodawn:temporal_phantom` |
| Time Tyrant | `chronodawn:time_tyrant` |

The ID is a string rather than an enum in the public contract. Consumers can
persist and compare it without loading a registry object, and future bosses can
be added without forcing an exhaustive enum switch to change.

## Public contract

Three types live under `com.chronodawn.api.event` in `common/shared`, so every
supported Minecraft version and loader exposes the same source-level API:

- `BossDefeatedListener` is a functional interface with
  `onBossDefeated(BossDefeatedContext context)`.
- `BossDefeatedContext` is a read-only interface exposing the stable boss ID,
  defeated entity, server level, death position, damage source and the
  defeating player when the damage source attributes the defeat to one.
- `BossDefeatedEvents` owns `register(listener)` and `unregister(listener)`.

The context is an interface rather than a Java record. Adding a record component
would replace the canonical constructor and break compiled consumers. A future
optional accessor can instead be added as a default interface method without
breaking existing implementations.

Minecraft objects are deliberately exposed alongside the stable string ID.
Java addons usually need the level or player immediately, and hiding them behind
Chrono Dawn wrappers would create a second API that must duplicate Minecraft.
Binary compatibility across different Minecraft versions is not promised —
addons already compile against a specific Minecraft version — but method names,
meaning and boss IDs are stable across Chrono Dawn builds for those versions.

### Context accessors

| Accessor | Contract |
| --- | --- |
| `bossId()` | Stable namespaced ID from the table above. |
| `boss()` | The defeated `LivingEntity`; valid during the synchronous callback. |
| `level()` | The server level where the defeat occurred. |
| `position()` | Immutable block-position snapshot taken at dispatch. |
| `damageSource()` | The source passed to the boss's `die` method. |
| `defeatingPlayer()` | The `ServerPlayer` attributed by `DamageSource.getEntity()`, or `null`. |

`defeatingPlayer()` does not guess ownership beyond Minecraft's damage-source
attribution. It handles direct player attacks and player-owned projectiles, but
may be `null` for environmental damage, commands or indirect mechanisms. The
raw damage source remains available to integrations that need different credit
rules.

## Dispatch semantics

- The event fires on the logical server only.
- It fires synchronously on the thread running the boss death.
- It fires once for each boss death lifecycle, following vanilla's `die`
  lifecycle rather than creating separate persistent bookkeeping.
- It fires after that boss's existing defeat consequences, such as boss-room
  unprotection and Time Tyrant dimension stabilization. A listener therefore
  observes the completed Chrono Dawn state transition.
- Listeners run in registration order.
- A runtime exception from one listener is logged and does not stop later
  listeners or escape into the boss death path.
- Registering the same listener object twice produces two callbacks. Each
  `unregister` call removes one registration, matching ordinary listener-list
  semantics.

Registration uses a copy-on-write listener list. Dispatch occurs rarely, while
script or addon registration may be replaced during reload. Snapshot iteration
lets a listener register or unregister during a callback without corrupting the
current dispatch.

## Internal connection

`BossKind` remains the internal single source of truth for the six boss keys and
gains a namespaced `eventId()`. Each version-specific boss `die` method calls an
internal dispatch entry point exactly once, after its existing server-side
consequences.

The public API does not expose Architectury's `Event` type. Doing so would make
Architectury listener behavior — including exception propagation and API
changes — part of Chrono Dawn's semver contract. Architectury remains an
implementation dependency of the mod, not a dependency of this event surface.

## Compatibility policy

The following are stable for the current major version of Chrono Dawn:

- public package and type names;
- listener and accessor method names;
- the six boss ID strings;
- server-only, synchronous, post-consequence timing;
- the meaning of a `null` defeating player;
- listener ordering and exception isolation.

Additive changes may introduce new event types, new boss IDs, overloads or
default context methods. Removing or renaming an existing type, method or boss
ID; changing dispatch side or timing; or changing an accessor's meaning requires
a major version.

## Testing

Shared unit tests cover:

- registration order and unregister behavior;
- duplicate registration behavior;
- exception isolation;
- all six `BossKind` values mapping to their stable event IDs;
- a source guard proving every version module contains exactly one correctly
  mapped dispatch call in each of the six boss classes.

The source guard must be mutation-checked by temporarily removing one dispatch
call and confirming the test fails. It protects the 66 version-specific hook
sites from drifting when a Minecraft version is added or copied.

Manual verification is deferred to the user at the end of the implementation:
on one Fabric and one NeoForge current-version client/server, register a small
test listener, defeat a boss with a player and confirm one callback with the
expected ID, player, level and post-defeat state. The exact checklist belongs in
`.claude/task.local.md`.

The full multi-version `checkAll` task must pass because the hook touches all
eleven common modules and both loaders.

## Documentation

- `docs/modpack-integration.md`: public API reference and Java addon example.
- `CHANGELOG.md`: new modpack integration API under `[Unreleased]`.
- Modpack-author readiness roadmap: sub-project C becomes in progress with the
  boss-defeated slice linked.

## Out of scope

- Direct KubeJS, CraftTweaker or FTB Quests adapters. This slice supplies the
  stable Java foundation those integrations consume.
- Portal-opened and Chronicle-entry-unlocked events. They follow as independent
  slices after this contract proves usable.
- Cancellation or mutation. A defeat notification cannot prevent death, alter
  loot or rewrite Chrono Dawn progression state.
- Persisting or replaying events for listeners registered after the defeat.
- Defining party or nearby-player credit rules; integrations can derive those
  from the server level and death position.

## Done criteria

- The public API compiles unchanged in all supported common modules.
- Every boss dispatches once after its existing server-side defeat consequences.
- Listener failures cannot break the death path or suppress later listeners.
- Unit tests and the mutation-checked 66-site source guard pass.
- Modpack integration docs, roadmap and changelog describe the shipped contract.
- `./gradlew checkAll` passes.
- Manual Fabric and NeoForge checks are recorded for the user.
