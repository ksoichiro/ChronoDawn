# Endgame Reward Shortage

The Time Tyrant can be defeated only once per world, and it is the only source of Fragments of Stasis Core. This document records the problem, what has already been done, and the open options. It is **not** an implementation plan. A design spec under `docs/superpowers/specs/` should be written once an approach is chosen.

## Current Behavior

- `TimeTyrantSpawner` spawns at most one Time Tyrant per world (`MAX_TIME_TYRANTS_PER_WORLD = 1`, plus the `isTyrantDefeated` flag in `ChronoDawnGlobalState`).
- Master Clock structures keep generating after the kill, but none of them spawn the boss again.
- Each boss room is unprotected only when its own boss is defeated (`BlockProtectionHandler`). Master Clock walls are always protected (`PermanentProtectionHandler`) so players cannot bypass the Ancient Gear door requirement. As a result, the boss rooms of every other Master Clock stay unbreakable forever after the one kill.
- The Time Tyrant drops 4 to 6 Fragments of Stasis Core (3 to 5 before 0.13.0), plus 1 to 2 per Looting level. In multiplayer only the players who were there for the kill get any.
- The other five bosses spawn once per dungeon copy (`BossSpawnData`), and their dungeons repeat across the world, so they can be fought again.

## Fragment Math

The four ultimate artifacts need 15 fragments through their original recipes:

| Artifact | Fragments |
|---|---|
| Chronoblade | 4 (+ Eye of Chronos) |
| Time Tyrant Mail | 6 |
| Echoing Time Boots | 2 |
| Unstable Pocket Watch | 3 |

Before 0.13.0 a world could never craft all four this way.

## Already Done

0.13.0 added the Clockwork Mechanism (4 Ancient Gears, 1 Enhanced Clockstone, 4 Time Crystals) and an alternative recipe for each artifact that needs only 1 Fragment of Stasis Core. One kill without Looting now covers all four artifacts in singleplayer.

This does not help:

- Multiplayer servers, where players who missed the fight have no source of fragments.
- Players who want to fight the Time Tyrant again.
- Worlds where the fragments were already spent on the original recipes.

## Player Reports

- 2026-10-20, CurseForge comment (0.8.0, Minecraft 1.21.1, NeoForge): the player defeated the Time Tyrant, then found two more Master Clocks. The boss did not spawn in either, and blocks could not be broken. They read both as bugs. This was a singleplayer player asking to fight again, not a multiplayer fragment shortage. The reply explained the once-per-world rule, said a way to fight again is planned with no date, and pointed to the 0.13.0 alternative recipes.

## Open Options

- **Time Tyrant revival.** Some way to fight the boss again, for example per Master Clock copy like the other bosses, or a crafted or ritual summon. Needs a decision on whether repeat kills drop the same loot and whether the world-state effects of the first kill (portal stabilization, sky color) stay tied to the first kill only.
- **Unprotect the remaining Master Clock boss rooms after the kill.** Smaller change that removes the "can't break blocks" confusion in the report above. The walls must stay protected while a boss room can still spawn the Tyrant. If revival lands, this may become unnecessary or need to change.
- **Another fragment source.** For example a rare drop or trade, aimed at multiplayer players who missed the fight.
