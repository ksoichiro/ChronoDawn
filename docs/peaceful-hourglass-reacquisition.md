# Time Hourglass Reacquisition on Peaceful Difficulty

Investigation of whether a player can become unable to obtain a Time Hourglass
(`chronodawn:time_hourglass`) and therefore a Portal Stabilizer, when Hourglass
Golems cannot spawn on Peaceful. This is a static analysis of the data and
code. It was not confirmed in a running game.

## Conclusion

The suspected soft lock does not hold. Every ingredient is obtainable inside
Chrono Dawn, and the recipe unlock advancements do not require the blueprint.
No countermeasure was implemented.

## Findings

- `checkHourglassGolemSpawnRules` returns `false` on Peaceful in the 1.20.1,
  1.21.1 and 26.3 modules. The golem is a `Monster`, so it also cannot exist on
  Peaceful in vanilla terms. The golem is therefore not a source of hourglasses
  on Peaceful.
- The hourglass recipe needs glass, clockstone ingots and a `chronodawn:sand`
  tag item (vanilla sand or temporal sand). Clockstone ore is generated in
  Chrono Dawn biomes (`ore_clockstone` in the biome features), and sand can be
  smelted into glass.
- The recipe unlock advancements of both `time_hourglass` and
  `portal_stabilizer` are OR conditions (`has_item` or `has_the_recipe`) in the
  1.20.1, shared 1.21.1+ and 26.3 data. Holding glass unlocks the hourglass
  recipe, and holding a clockstone unlocks the stabilizer recipe. The blueprint
  items (Ancient Ruins chest, Forgotten Library chest) are not required, so
  `doLimitedCrafting` does not block the chain.
- The stabilizer ingredients are hourglasses, clockstone ingots, Fruit of Time
  and a clockstone block. Fruit of Time is placed by the Chrono Dawn biome
  features and also appears in several structure chests.

## Remaining risk

- A modpack that strips or overrides the mod advancements would break the
  unlock. This is already an accepted trade-off in
  `docs/archive/superpowers/2026-10-24-chronicle-acquisition-design.md`.
- An in-game check of the advancement unlock under `doLimitedCrafting` was not
  run. A scenario for it would settle this if the question comes up again.
- The emergency exit (dying and respawning in the Overworld) stays available.
