# Design: Portal Opened Event API

**Created**: 2026-09-04
**Status**: Draft
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project C

---

## Why

Chrono Dawn portals are a second progression signal packs care about, alongside
boss defeats. Today a pack can only infer portal activity indirectly, by
watching for the portal block itself or for a player changing dimension,
neither of which distinguishes a fresh ignition from an unrelated dimension
change, or reports which physical portal changed state.

This is the second slice of the scripting-events sub-project, following the
[boss-defeated event](./2026-08-29-boss-defeated-event-design.md). It
publishes a loader-neutral Java event that fires whenever a Chrono Dawn portal
transitions into an active state, whether by a player igniting a new frame or
by the mod auto-building or reusing a return portal on arrival.

## Scope

The event covers every `PortalStateMachine` transition into `ACTIVATED`:

| Trigger | Where | Cause |
| --- | --- | --- |
| Player ignites a valid frame with a Time Hourglass | `TimeHourglassItem.useOn()` | `IGNITION` |
| A brand-new return portal is generated at a teleport destination | `PortalTeleportHandler.generatePortalStructure()` (new portal branch) | `REIGNITION` |
| An `INACTIVE` registered portal is reactivated during teleport | `PortalTeleportHandler.generatePortalStructure()` (`INACTIVE` branch) | `REIGNITION` |
| A `DEACTIVATED` portal frame is reused after an unstable arrival | `PortalTeleportHandler.generatePortalStructure()` (`DEACTIVATED` branch) | `REIGNITION` |

`STABILIZED` portals that regenerate their blocks without a state change (the
frame already fully active) do not fire the event. Only an actual transition
into `ACTIVATED` counts as "opened".

## Public contract

Three types live under `com.chronodawn.api.event` in `common/shared`, matching
the boss-defeated event's package and shape:

- `PortalOpenedListener` — `@FunctionalInterface` with
  `onPortalOpened(PortalOpenedContext context)`.
- `PortalOpenedContext` — read-only interface exposing the portal's stable ID,
  server level, frame position, activation cause, and the player credited with
  the activation when one is known.
- `PortalOpenedEvents` — owns `register(listener)` / `unregister(listener)`.
- `PortalOpenCause` — a two-value enum, `IGNITION` and `REIGNITION`, described
  in the Scope table above.

The context is an interface, not a record, for the same reason as
`BossDefeatedContext`: a future accessor can be added as a default method
without breaking compiled consumers.

### Context accessors

| Accessor | Contract |
| --- | --- |
| `portalId()` | The `PortalStateMachine`'s `UUID`, stable for that physical portal's lifetime in the registry. |
| `level()` | The `ServerLevel` containing the portal frame. |
| `position()` | The frame's bottom-left `BlockPos`, matching `PortalStateMachine.getPosition()`. |
| `cause()` | `PortalOpenCause.IGNITION` or `PortalOpenCause.REIGNITION`. |
| `igniter()` | The `ServerPlayer` credited with the activation, or `null` when none is known (e.g. an auto-generated return portal reached by an entity that is not a player, if that ever becomes possible). |

`igniter()` is populated whenever the triggering code path already has a
`ServerPlayer` in scope. For `IGNITION` this is always the Time Hourglass
user. For `REIGNITION` this is the teleporting `ServerPlayer` passed down from
`PortalTeleportHandler.teleportThroughPortal()`, since only players can
currently trigger dimension travel through a Chrono Dawn portal.

## Dispatch semantics

- The event fires on the logical server only.
- It fires synchronously on the thread performing the activation. For
  `TimeHourglassItem`, that is the deferred `server.execute()` tick that
  fills the portal blocks and registers the state machine, matching where
  `portal.activate()` is already called today.
- It fires once per `PortalStateMachine` transition into `ACTIVATED`, never
  for a call that leaves the state unchanged (`STABILIZED` regeneration, or
  any future no-op path).
- It fires after the transition and after the portal blocks are placed, so a
  listener observes the completed physical and logical state.
- Listeners run in registration order.
- A runtime exception from one listener is logged and does not stop later
  listeners or escape into the portal activation path.
- Registering the same listener object twice produces two callbacks, matching
  `BossDefeatedEvents`.

Registration uses the same copy-on-write listener list as
`BossDefeatedEvents`, for the same snapshot-iteration reasoning.

## Internal connection

`PortalTeleportHandler.generatePortalStructure()` gains an `@Nullable
ServerPlayer igniter` parameter, threaded through from its two callers
(`generatePortal()` and the reusable-frame branch of
`findReusablePortalFrame()`), both of which are themselves called from
`teleportThroughPortal()` where the `ServerPlayer` is already in scope. A
single local flag inside `generatePortalStructure()` records whether one of
its three branches actually transitioned the portal to `ACTIVATED`; the
method fires the event once, after block generation, only when that flag is
set. This keeps the dispatch call sites at exactly two per version module
(`TimeHourglassItem.useOn()` and `generatePortalStructure()`), the same shape
as the boss-defeated event's one-call-per-boss-class pattern.

The public API does not expose Architectury's `Event` type or the internal
`PortalStateMachine` / `PortalRegistry` classes, for the same reason as the
boss-defeated event: Architectury and the internal registry stay
implementation details, not part of the semver API surface.

## Compatibility policy

The following are stable for the current major version of Chrono Dawn:

- public package and type names;
- listener and accessor method names;
- the two `PortalOpenCause` values and their meaning;
- server-only, synchronous, post-activation timing;
- the meaning of a `null` igniter;
- listener ordering and exception isolation.

Additive changes may introduce new event types, new cause values, overloads
or default context methods. Removing or renaming an existing type, method or
cause value; changing dispatch side or timing; or changing an accessor's
meaning requires a major version.

## Testing

Shared unit tests cover:

- registration order and unregister behavior;
- duplicate registration behavior;
- exception isolation;
- `PortalOpenCause` value stability;
- a source guard proving every version module contains exactly one dispatch
  call in `TimeHourglassItem` and exactly one in
  `PortalTeleportHandler.generatePortalStructure()`.

The source guard must be mutation-checked by temporarily removing one
dispatch call and confirming the test fails, matching the boss-defeated
event's guard.

Manual verification is deferred to the user at the end of the implementation:
on one Fabric and one NeoForge current-version client/server, register a
small test listener, then (1) ignite a fresh frame with a Time Hourglass and
confirm one `IGNITION` callback with the expected position and player, and
(2) travel through a portal so a return portal is generated or reactivated on
the other side, and confirm one `REIGNITION` callback. The exact checklist
belongs in `.claude/tasks.local.md`.

The full multi-version `checkAll` task must pass because the hook touches all
eleven common modules and both loaders.

## Documentation

- `docs/modpack-integration.md`: public API reference and Java addon example,
  added alongside the existing boss-defeated section.
- `CHANGELOG.md`: new modpack integration API under `[Unreleased]`.
- Modpack-author readiness roadmap: sub-project C's portal-opened slice
  marked complete and linked.

## Out of scope

- Direct KubeJS, CraftTweaker or FTB Quests adapters. This slice supplies the
  stable Java foundation those integrations consume, same as boss-defeated.
- Chronicle-entry-unlocked event. It follows as an independent slice.
- Cancellation or mutation. A notification cannot prevent activation or alter
  portal placement.
- A portal-closed or portal-stabilized event. Those are separate signals with
  their own dispatch sites and are out of scope for this slice.
- Crediting an igniter for entities other than players. Only `ServerPlayer`
  is supported today; nothing in the portal code currently allows non-player
  entities to trigger activation.

## Done criteria

- The public API compiles unchanged in all supported common modules.
- Every portal activation dispatches exactly once, only on an actual
  transition into `ACTIVATED`, after blocks are placed.
- Listener failures cannot break the portal activation path or suppress
  later listeners.
- Unit tests and the mutation-checked source guard pass.
- Modpack integration docs, roadmap and changelog describe the shipped
  contract.
- `./gradlew checkAll` passes.
- Manual Fabric and NeoForge checks are recorded for the user.
