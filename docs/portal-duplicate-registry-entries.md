# Duplicate portal registry entries at one position

Investigation date: 2026-11-25. Source: `main` at `b0fd72fe0`.

A review of the portal state fix raised a question: when `PortalRegistry` holds
two entries for the same dimension and position, can the position index
(`portalsByPosition`) point at the old entry after a save and reload? This
document records the answer, the effect on gameplay, and the policy.

## Result

Yes. Duplicates are created by normal play, and which entry owns the position
after a reload depends on the order of the saved list, not on registration
order. The unit tests added to `PortalRegistryTest` reproduce both outcomes.

## How duplicates appear

- `PortalRegistry.registerPortal` adds the entry to `portals` and overwrites the
  position index with `put(position, portalId)`. It never removes the entry that
  previously owned the position, so the old UUID stays in `portals`.
- `PortalIgnitionHandler` registers a new UUID on every ignition without calling
  `getPortalAt` first. Igniting a frame whose earlier entry is DEACTIVATED
  (blocks removed after an unstable arrival, frame kept) leaves two entries at
  the frame's bottom-left position: the old DEACTIVATED one and a new ACTIVATED
  one. This code exists in every `common/<version>/` module.
- `PortalTeleportHandler` (arrival frame creation) and `PlayerEventHandler`
  (arrival without a registered entry) both check `getPortalAt` first, so they
  do not add duplicates by themselves.

## Why the index can flip after a reload

- `saveToNBT` iterates `portals.values()`, a `ConcurrentHashMap` keyed by random
  UUIDs. The saved order is hash order, so it is effectively random per entry
  pair.
- `loadFromNBT` calls `clear()` and then `registerPortal` in list order. The
  entry that comes last in the list wins the position index.
- In memory the newest entry owns the index. After a reload it is the newest
  entry in about half of the cases and the old one in the rest.

## Gameplay effect

`getPortalAt` feeds three state decisions:

- Arrival lookup in `PlayerEventHandler`: an ACTIVATED entry is deactivated and
  its blocks are removed, a DEACTIVATED one is skipped.
- `PortalArrivalHelper`: deactivates the arrival entry only when it is ACTIVATED.
- `PortalTeleportHandler` frame creation: INACTIVE and DEACTIVATED entries are
  reignited, ACTIVATED ones are left alone.

This section comes from reading the code and the unit tests. It was not
observed in a running game.

If the old DEACTIVATED entry wins after a reload, a freshly ignited frame is
treated as already deactivated. The arrival-side deactivation is skipped, so the
registry keeps a stale state for that frame, and reignition checks run against
the wrong entry. If the new entry wins, nothing looks wrong. The symptom would
be intermittent per world and per frame.

## Policy

- Do not touch existing data. Entries already saved are not merged or deleted on
  load. The old entry carries no timestamp, so no rule could pick the newer one
  reliably, and the entry that wins is harmless when it is the new one.
- Prevent new duplicates at the source. Every `PortalIgnitionHandler` should look
  up `getPortalAt` before registering, and reuse the existing entry the way
  `PortalTeleportHandler` does: register only when nothing is there, activate an
  INACTIVE or DEACTIVATED entry, and leave an ACTIVATED one alone.
- Do not change `registerPortal` or `loadFromNBT` ordering semantics. The tests
  pin the current behavior so that a later change shows up as an intentional
  edit.

The prevention change touches the ignition handler in every version module, so
it is tracked as a separate task.

## Tests

`common/shared/src/test/java/com/chronodawn/unit/PortalRegistryTest.java`:

- `duplicateEntriesAtSamePosition_indexFollowsSavedListOrderAfterReload`: one
  DEACTIVATED and one ACTIVATED entry at a position. Reloading with the new entry
  first yields the DEACTIVATED one, and with the old entry first yields the
  ACTIVATED one.
- `duplicateEntriesAtSamePosition_bothSurviveSaveAndLoad`: both entries are kept
  across save and load, so existing worlds keep their duplicates.

Run on 1.21.1: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*PortalRegistryTest'`.
