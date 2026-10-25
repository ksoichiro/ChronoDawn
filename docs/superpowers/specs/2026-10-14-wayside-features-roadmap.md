# Roadmap: Wayside Features (Parts 1 to 6)

**Created**: 2026-10-14
**Status**: Parts 1 to 4 shipped to main (2026-10-14, 2026-10-17, 2026-10-21, 2026-10-25). Parts 5 and 6 not started.
**Related**: [Part 1 design](./2026-10-12-wayside-sundial-design.md),
[Part 1 plan](../plans/2026-10-12-wayside-sundial.md)

This document records everything decided so far for the whole effort, so a
new session can pick up any part without repeating the design discussion.
Each part still gets its own design spec, implementation plan and review
cycle. Decisions marked "decided" were agreed with the maintainer. Items
under "Open" still need a decision in that part's brainstorming.

---

## Problem

Major Chrono Dawn structures are far apart (`random_spread` spacing 48 to 60
chunks for Master Clock, Ancient Ruins, Clockwork Depths, Entropy Crypt and
Guardian Vault, 30 for Desert Clock Tower and Forgotten Library, and biome
restrictions skip candidate cells). The story is easy to follow and compasses
point the way, but the walk between structures is long and has nothing to
discover. Several biomes pass by, yet nothing invites a detour.

## Options considered

| Option | Idea | Outcome |
|---|---|---|
| A | Give the six existing small surface features a reason to stop | **Chosen first.** Reuses existing NBTs and placement, fits the "safe upgrade" priority for modpacks |
| B | Add one or two mid-size structures (spacing 12 to 20 chunks) between major ones | Deferred. Revisit after A, could also feed the endgame reward shortage |
| C | Make discovery itself the reward (Chronicle records, collections) | Deferred. Chronicle is a static guidebook (`ChronicleData` reads JSON only), so per-player discovery tracking would be a new system |
| D | Shorten distances (spacing) or add fast travel | Deferred. Alone it only shortens empty stretches. Spacing changes do not affect already-generated worlds |

## Decisions for all parts (decided)

- **Each feature has its own role.** Roles combine utility, materials, small
  events and lore across the six.
- **Reusable, never one-shot.** The maintainer explicitly wants to avoid
  "used once, spent forever" features. Everything works on a cooldown.
- **Cooldown is per location and per player.** Players on a shared server
  never compete for one feature. Cooldowns are counted in game time
  (`Level#getGameTime`), so sleeping and Chrono Dawn's variable day length
  do not shorten them.
- **State lives in a dedicated activator block** with a block entity. A
  look-alike block placed by a player does nothing. Activator blocks have no
  item and drop their plain look-alike when broken, to prevent farming.
- **Already-generated features are not converted.** Only newly generated
  chunks get interactive features.
- **Major structure placement is unchanged.**
- **Rewards stay modest.** Farming risk from repeatable use is controlled by
  cooldown length and reward size, both configurable.

## Status

| Part | Feature | Role | Status |
|---|---|---|---|
| 1 | Shared foundation + `old_sundial` | Travel buff by time of day | Shipped (main `db7246186`) |
| 2 | `lost_adventurer_memorial` | Lore fragment and supplies, light-pillar effect | Shipped (2026-10-17), with the Lost Adventurer's Journal |
| 3 | `time_well` | Wishing well, exchange a thrown item | Shipped (2026-10-21) |
| 4 | `watchmaker_camp` | Per-player chest that refills over time | Shipped (2026-10-25) |
| 5 | `time_cairn` | Collect time-of-day cairn stones, rare midnight echo | Not started |
| 6 | `hourglass_monolith` | Small wave fight with a reward | Not started |

Order rationale: simplest first to prove the foundation, then features that
need new UI, entities or event state machines.

## Foundation available after parts 1 to 4

- `com.chronodawn.core.wayside.PlayerCooldownTracker` (pure Java):
  `tryUse(UUID, gameTime, cooldownTicks)` returns 0 when accepted, prunes
  expired entries on every use, serializes to one string.
- `CooldownActivatorBlockEntity` (abstract) with
  `tryActivate(UUID, gameTime, cooldownTicks)`. Three era copies:
  `common/shared` (CompoundTag, 1.20.1 to 1.21.4), `common/1.21.5`
  (`getStringOr`), `common/1.21.6` to `common/26.3` (ValueInput/ValueOutput).
  The 1.21.5+ `build.gradle` files exclude the shared copy.
- Config section `[gameplay.wayside]` in `WaysideSettings`: sundial keys,
  `memorial_cooldown_ticks` and `time_well_cooldown_ticks`. Later parts add
  their keys there unless the part's design decides otherwise.
- Pattern for an activator block: `SundialGnomonBlock` per version module
  (14 copies), block entity class shared in `common/shared`, registration in
  each `ModBlocks` / `ModBlockEntities`, `ModBlockId` / `ModBlockEntityId`
  entries, GameTest in `common/gametest` registered in every
  `RegistryDrivenTestGenerator`.
- `com.chronodawn.core.wayside.StepOnTracker` (pure Java, part 2): tells a
  player stepping onto a plate apart from one standing on it
  (`STEP_OFF_TICKS` = 10). Kept in memory only.
- Walk-on activator pattern (part 2): `MemorialPlateBlock` subclasses each
  version's `TemporalStonePressurePlate` and implements `EntityBlock`.
  `onPlayerInside(ServerLevel, BlockPos, Player)` is public static so
  GameTests can drive it without the per-era `entityInside` signature.
- Data-driven rewards (part 2): `rollOffering(level, pos, lootTablePath)`
  rolls a chronodawn loot table with the player as `this` entity and luck.
  Shared pools live in their own table and are referenced through a
  `minecraft:loot_table` entry.
- GameTest helpers: `CompatGameTestHelper.copyBlockEntityData` (save/load
  round trip), `makeMockPlayer` (plain `Player`, never joins),
  `makeMockServerPlayer` / `removeMockServerPlayer` (a real joined
  `ServerPlayer`, always removed in a `finally` block).
- Thrown-item activator pattern (part 3): `WellQueue` and `TimeWellOutcome`
  decide and queue wishes. The `TimeWellStoneBlock` ticker scans for items in
  the water and re-queues while the waiting tag `chronodawn.time_well_waiting`
  is set. Items are released when the stone or water is removed, through
  `onRemove` up to 1.21.4 and `BlockEntity#preRemoveSideEffects` from 1.21.5.
  Items the well raises carry `chronodawn.time_well_risen`, so one that falls
  back into the water is raised again. `raise()` checks the toss path with
  collision shapes (`isTossPathClear`, `closestClearSide`) because the
  template rotates.
- More GameTest helpers (part 3): `CompatGameTestHelper.setThrower` (the
  parameter is a UUID on 1.20.1 and an Entity later) and
  `loadModStructureTemplate` (26.3 renamed the structure manager accessor), so
  shared tests can throw items as a joined player and place real templates in
  each rotation.

- Post-placement swap (part 4): `WatchmakerCampChests` replaces the camp's
  vanilla loot chest with the `watchmaker_chest` block after the template is
  placed, unless Lootr is present (`Platform.isModLoaded("lootr")`, no Lootr
  classes). Rotation does not matter because the swap works on the placed
  block.
- Per-player slots (part 4): `WatchmakerChestBlockEntity` keeps 27 slots per
  player UUID plus the cooldown. It is not a `Container`, so hoppers cannot
  touch it. A fill happens only when the player's slots are empty and
  `tryActivate` accepts.
- Take-only menu (part 4): `WatchmakerChestMenu` is a vanilla `GENERIC_9x3`
  menu whose slots accept no item, so the client needs no custom screen. The
  block uses `ChestRenderer` through a registered block entity renderer.
- More GameTest helpers (part 4): `CompatGameTestHelper.rollModLootTable`
  (rolls a chronodawn loot table), `hasLootTable` and `clearLootTable` (for
  `RandomizableContainer` block entities), and `clickPickup` / `clickSwap`
  (drive menu clicks).

## Per-feature design notes

What each feature looks like today comes from its NBT in
`common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/`
(1.20.1 copies are generated by the `convertNbt` task). Placement is in
`worldgen/placed_feature/<name>.json` (rarity filters below).

### Part 2: `lost_adventurer_memorial` (shipped)

Shipped design: [part 2 design](./2026-10-14-wayside-memorial-design.md).
The notes below are the starting point it was designed from.


- Today: 3x3x3 memorial of temporal stone bricks with a
  `temporal_stone_pressure_plate`. A snowy variant
  (`lost_adventurer_memorial_snowy`) exists. Rarity 1 in 16 chunks.
- Decided: using it ("praying") yields a lore fragment and a small amount of
  supplies (food, arrows, torches). The maintainer noted that "praying" is
  hard to express and would just look like right-click gives items, so the
  use must be dressed with a **light pillar pouring down** (particles and
  sound) to make it feel like a prayer being answered.
- Open: what the lore fragment is (written book, a new item, or pages of
  something), whether fragments form a collection, which block is the
  activator (pressure plate or a new block), cooldown length (initial idea
  one Minecraft day), both NBT variants must get the activator.

### Part 3: `time_well` (shipped)

Shipped design: [part 3 design](./2026-10-18-wayside-time-well-design.md).
The notes below are the starting point it was designed from.

- Today: dry 3x5x3 well, a two-block shaft with one `temporal_amber_ore` at
  the bottom (`y_offset: -1`). There is **no water** in the NBT. Rarity 1 in
  32 chunks.
- Rejected: "draw water to get amber dust", because the ore at the bottom
  already gives amber.
- Decided: a **wishing well**. Add water inside the well (NBT edit, keep the
  amber ore). A player throws an item into the well, after a moment the water
  glows and another item rises. The returned item depends on the "value tier"
  of the thrown item (common material gives a slightly better material or
  food, Clockstone gives Time Crystal, and so on), with a rare jackpot. Theme:
  offer something of the present, receive something of the past. Per-player
  cooldown, about one Minecraft day. **During cooldown the thrown item comes
  back unchanged, it is never lost.**
- Open: the exchange table and tiers, how thrown items are detected (item
  entity in the water column), how the activator block is represented.

### Part 4: `watchmaker_camp` (decided role)

- Today: 5x2x5 camp with a chest (loot table), lectern, campfire, crafting
  table and anvil. Rarity 1 in 48 chunks. The only feature that already had
  interaction before this effort.
- Decided: **Lootr-like per-player chest** whose contents refill per player
  over time (initial idea two to three Minecraft days).
- Open: needs its own container UI and per-player inventories. Must not
  conflict with Lootr when it is installed (Lootr already converts loot
  chests in structures, normally one fill per player, refill depends on its
  config). Decide whether to defer to Lootr when present.

### Part 5: `time_cairn` (decided role, after several rounds)

- Today: 3x3x3 cairn with `chronodawn:clockwork_dial` on top and a
  `time_crystal_ore` below. Rarity 1 in 16 chunks.
- Rejected:
  - Pointing to the nearest major structure: makes the game too easy, or is
    useless when the player cannot tell which structure it means. "Only
    showing a direction makes it just a cairn."
  - Matching the dial to a fixed time for a reward: weak as a reason to stop,
    and sleeping trivializes it.
  - A timed collection challenge: overlaps with the monolith's event slot.
  - A return point / teleport anchor: another teleport method affects balance
    too much.
- Decided: **"Cairn stones of time" plus a rare "echo of the past".** Turning
  the dial gives a stone engraved with the current time of day (dawn, noon,
  dusk, midnight). Collecting all four and combining them gives a reward.
  Long walks pass through different times of day, so the set fills naturally
  and motivates "I want to find a cairn at night". At midnight, using a cairn
  rarely summons the echo of a past adventurer, a ghost that walks off and
  leads to a cache it buried.
- Constraint already identified: one cairn must not give all four stones by
  waiting, so limit stones per cairn per day (or similar).
- Open: stone items and combining recipe, the reward, echo entity and its
  path, cache placement, Chrono Dawn's variable day length interplay.

### Part 6: `hourglass_monolith` (decided role)

- Today: 3x5x3 temporal sandstone monolith in the desert. Rarity 1 in 16
  chunks.
- Decided: activating it starts a **short wave fight** (two or three waves,
  like a small raid). Clearing it gives a reward (Clockstone, Time Crystal,
  rarely Chronite Shard). The maintainer agreed but expects tuning.
- Open: wave composition and scaling, state machine and interruption
  handling (player leaves, dies, logs out, chunk unloads), multiplayer
  participation, cooldown (initial idea two to three Minecraft days), link to
  the endgame reward shortage.

## Lessons from parts 1 to 4 (apply to every part)

- **Render layers.** `ModBlockId.cutout()` only applies on Fabric. NeoForge
  and Forge take the layer from `"render_type"` in the model JSON. Vanilla
  models (for example `iron_bars_post`) have none, so wrap them in own models
  with `"render_type": "minecraft:cutout"`.
- **26.3 has its own `StructureTests` copy.** Update it together with
  `common/gametest/.../StructureTests.java` when changing a structure NBT.
- **26.3 has its own loot table and advancement copies** in the 26.3 schema.
- **Time of day differs by era.** Before 26.x use `Level#getDayTime`. 26.1.2
  and 26.2 use `clockManager().getTotalTicks(clock)`, 26.3 uses
  `clockManager().getInstance(clock).totalTicks()`. Mirror each module's
  `TimeDistortionEventHandler`.
- **Check geometry against the NBT.** Part 1's first particle code drew inside
  the solid pedestal block. Decode the NBT before placing effects.
- **Pick clearly audible sounds.** `AMETHYST_BLOCK_CHIME` was inaudible in
  play-testing. `NOTE_BLOCK_CHIME.value()` works. Avoid bell sounds, which the
  Time Distortion "distant bell" already uses.
- **Collections must pay off.** In part 2 the maintainer play-tested
  "collect all six pages for an advancement only" and found it unsatisfying.
  The pages now craft into a journal that changes later prayers. When a
  feature hands out a collectible, decide up front what completing the
  set does, and keep repeatable rewards to common ores and processed food.
- **No recolored vanilla textures.** Tinting a vanilla PNG is still a copy.
  Draw new textures or ask the maintainer, who hand-edits them.
- **Loot table references differ by era.** A `minecraft:loot_table` entry
  names its table with `"name"` on 1.20.1 and `"value"` from 1.21.1.
- **Recipe formats differ by era.** Object ingredients and result `"item"`
  on 1.20.1, object ingredients and result `"id"` on 1.21.1, string
  ingredients from 1.21.2 (`common/shared-1.21.2+`). 26.3 recipe-unlock
  advancements use `"recipes"` in `recipe_unlocked`.
- **Mock players.** Vanilla `makeMockPlayer` is a plain `Player`. Anything
  that needs a `ServerPlayer` (advancements, teleports) must use
  `makeMockServerPlayer` and remove the player afterwards.
- **Never lose a player's item.** In part 3 most review findings were ways
  an item could be stranded: a chunk reload, a removed water source or
  activator, an item falling back. Anything an activator holds or emits needs
  a persisted entity tag so it can be picked up again after reload. An
  in-memory queue alone is not enough.
- **Look-alike activators confuse manual checks.** After breaking the Time
  Well Stone, spilled water carried items out and the maintainer read it as
  the well still working. State in the check steps what a broken activator
  does, and how to tell the feature's effect from vanilla physics.
- **Vanilla sound events can hide variants.** `entity.generic.splash`
  randomly plays a file with a second splash two seconds in. Let the
  maintainer compare candidates with `/playsound` before changing a sound.
- **The 1.20.1 GameTest world is reused across runs.** Tests that search an
  area for items must ignore entities older than the test. `TimeWellTests`
  filters by spawn time.
- **Runtime checks that were accepted as sufficient:** 1.21.1 NeoForge and
  Fabric, 1.20.1 Forge, 26.3 NeoForge, 26.2 Fabric. Other versions differ only
  in renamed constants and are covered by compile and tests.
- **Verification commands:** `checkAll` exceeds the 10-minute foreground limit
  in agent sessions, so run `validateResources validateTranslations
  validateData validateEraTagParity`, `buildAll`, `testAll` and `gameTestAll`
  separately, then read `build/gametest-*.log`. `buildAll` needs several GB of
  free disk space and failed once with "No space left on device".
- **Check that a template's containers can open.** The original camp NBT
  put the wool roof directly on the chest, and a vanilla chest under an
  opaque block never opens. Nobody noticed until part 4 decoded the NBT.
- **Swapping a block entity block during worldgen needs an intermediate
  block.** `WorldGenRegion.setBlock` only queues a pending entry when the new
  state also has a block entity, so the old chest block entity survives.
  Setting `BARRIER` first removes it. `/place feature` and GameTests run on a
  live chunk and never take this path, so check natural generation by hand.
- **Lootr converts chests after the chunk is live,** not during worldgen. A
  feature that leaves a plain `minecraft:chest` with a loot table is enough
  for Lootr, and a custom block that is not a `Container` is never touched.
- **GameTest mock players always report "not a spectator".** Check
  `gameMode.getGameModeForPlayer()` when a test needs a spectator. Real
  spectators never reach a block's use method anyway, vanilla asks
  `getMenuProvider` instead.
- **Forge 1.20.1 has no GameTest task.** Run the 1.20.1 GameTests through
  `:fabric:runGameTest -Ptarget_mc_version=1.20.1`.
- **Show structure changes in play before polishing.** The raised camp roof
  passed every test but looked like floating wool. The maintainer's screenshot
  led to the canopy hooked onto the poles.

## Ideas parked for later

- Mid-size structures between major ones (option B), possibly as an endgame
  reward source.
- Per-player discovery records in Chronicle (option C).
- Spacing adjustments or fast travel (option D), only together with A or B.
