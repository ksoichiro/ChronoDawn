# Design: Wayside Features, Part 5 (Time Cairn)

**Created**: 2026-10-27
**Status**: Draft
**Related**: [Roadmap](./2026-10-14-wayside-features-roadmap.md),
[Part 2 design](./2026-10-14-wayside-memorial-design.md) (collection
pattern), [Part 3 design](./2026-10-18-wayside-time-well-design.md),
[Part 4 design](./2026-10-22-wayside-watchmaker-camp-design.md)

## Why

The walk between major Chrono Dawn structures is long and has little to
discover (see the roadmap). Parts 1 to 4 gave the sundial, the memorial, the
well and the camp a reason to stop. The Time Cairn is the most common of the
six features (rarity 1 in 16 chunks, placed in every land biome), so today a
player passes several on every walk and none of them does anything.

Part 5 turns the cairn into a collection that fills itself while the player
travels. Turning the dial on top of a cairn gives a stone engraved with the
current time of day. A long walk passes through dawn, noon, dusk and
midnight, so the four stones collect naturally, and a player who is missing
one has a reason to look for a cairn at that hour. Combining the four stones
pays off at a cairn. At midnight, turning a dial rarely wakes the echo of a
traveler who passed this way long ago. The echo walks off and leads to a
cache they buried.

The roadmap records the ideas that were rejected for this feature (pointing
to structures, matching the dial to a fixed hour, a timed challenge, a
teleport anchor) and the decided role. This document fills in the open
points: the stone items, how they combine, the reward, the echo, the cache,
the per-cairn limit, and how Chrono Dawn's variable day length interacts
with all of it.

## Story

Travelers once marked the roads of this land with cairns and set a clockwork
dial on each one. A dial that is turned remembers the hour and gives it back
as a stone. Nobody has turned them in a very long time. A traveler's echo
still returns to some of them at midnight, to check on something they left
behind.

Player-facing text follows this framing. The echo is "a traveler" of the
past, not the Lost Adventurer of part 2 and not a living character, and it
never speaks.

## Goals

- Turning a cairn dial gives a Cairn Stone that matches the current time of
  day, so the collection fills while traveling.
- One cairn gives at most one stone per player per cooldown, so waiting at
  a single cairn is far slower than walking (roadmap constraint).
- Collecting all four stones and combining them gives a reward that is worth
  the walk, and the collection is repeatable.
- At midnight, a turn rarely summons an echo that leads to a buried cache.
  The cache exists the moment the echo appears, so nothing is lost if the
  player loses sight of it.
- Everything is tunable by config or datapack, as in parts 2 to 4.

## Non-goals and anti-goals

- No teleportation, no direction finding, no change to how the player
  navigates (rejected in the roadmap).
- No conversion of cairns in already-generated chunks (roadmap rule).
- No change to cairn placement or rarity.
- No new rendered entity. The echo is particles driven by the cairn, so no
  model, renderer or spawn egg is added across the 14 version modules.
- Anti-goal: a cairn that hands out gated materials. Boss-only items and
  Clockwork Mechanisms stay out of every table.
- Anti-goal: a stone that is only worth an advancement. The maintainer found
  "collect for an advancement only" unsatisfying in part 2.
- Anti-goal: an echo that can strand a player's reward. The cache is placed
  before the echo starts walking.

## Current structure

`time_cairn.nbt` is 3x3x3, placed with `y_offset: 0` on a block from the
feature's ground list (grass, dirt, podzol, coarse dirt, snow, temporal
grass, temporal dirt, coarse temporal dirt, temporal sand). Decoded layout
(x, y, z):

| Position | Block |
|---|---|
| (1, 0, 1) | `time_crystal_ore` |
| (0, 0, 1), (1, 0, 0), (1, 0, 2), (2, 0, 1) | `mossy_temporal_cobblestone_stairs`, each facing the center |
| (1, 1, 1) | `temporal_cobblestone` |
| (1, 2, 1) | `clockwork_dial` |
| everything else | air |

`clockwork_dial` is a 2-pixel-tall plate (16x2x16) with a clock-face
texture. It already reads as "a dial you could turn", and it sits on top of
the pile where a player can reach it from the ground. The ore at the base
is minable and stays as it is.

The template is placed with `random_rotate: true`. The dial is at the
center column, so rotation does not change its position.

## Player-facing behavior

### Appearance

The `clockwork_dial` at (1, 2, 1) becomes `chronodawn:time_cairn_dial`. It
reuses the `clockwork_dial` model and texture and looks the same. It has no
item and drops a plain `clockwork_dial` when broken (roadmap activator
rule).

### Turning the dial

Right-clicking the dial with an empty hand or any item that is not a Cairn
Keystone turns it. If the player is not on cooldown for this cairn:

- The dial decides the **time of day** from the dimension's day time and
  pops out one **Cairn Stone** of that hour as an item entity on top of the
  dial, with a small upward toss so it lands beside the cairn. The item
  carries no owner and anyone can pick it up. Items are spawned, never put
  into the inventory directly, because a full creative inventory would
  silently delete them.
- Particles play over the dial face and a short stone sound plays. (My call:
  `END_ROD` sparks in a ring over the dial for every hour, plus `DUST`
  particles tinted per hour where the version's API makes that cheap: warm
  orange for dawn, pale gold for noon, violet for dusk, deep blue for
  midnight. If the dust color API is awkward in a module, that module uses
  `END_ROD` only. The sound is a short stone-on-stone turn such as
  `BLOCK_STONE_BUTTON_CLICK_ON` or `BLOCK_GRINDSTONE_USE`, pitched per hour
  like the sundial. The maintainer compares candidates with `/playsound`,
  roadmap lesson, and no bell sounds.)
- An action-bar message names the stone: "The dial turns and gives up a
  Dawn Stone." and so on, through one lang key per hour.
- The cooldown starts for this player at this cairn.

If the player is on cooldown, nothing happens except an action-bar message
with the remaining minutes, using `PlayerCooldownTracker.toMinutesRoundedUp`
like the other parts: "The dial will not turn yet. Try again in about N
min."

### Hours of the day

The 24000-tick day is split into four equal quarters centered on sunrise,
noon, sunset and midnight (my call). The boundaries are in day-time ticks:

| Stone | Day time | What the player sees |
|---|---|---|
| Dawn Stone | 21000 to 2999 (wraps) | late night turning to morning |
| Noon Stone | 3000 to 8999 | full daylight |
| Dusk Stone | 9000 to 14999 | afternoon turning to night |
| Midnight Stone | 15000 to 20999 | night |

Every turn gives a stone, there are no gaps. Equal quarters are easy to
explain and to unit test. The windows do not match the sundial's three-way
split exactly (noon there is 5000 to 7000, night starts at 13000); that is
accepted because the two features answer different questions. The hour is
read through each module's `CompatLevelTime.dayTime(level)`, which already
hides the era split (`getDayTime` before 26.x, the world clock on 26.x). The
26.x `day` timeline keeps a 24000-tick period, so the quarter table holds
there too.

### Cooldown and the per-cairn limit

- Per player, per cairn, counted in game time (`Level#getGameTime`), like
  parts 1 to 4. Default 24000 ticks (one Minecraft day), config key
  `time_cairn_cooldown_ticks`.
- This is the roadmap's "limit stones per cairn per day". One cairn gives
  one stone per player per day, whatever the hour. Waiting at a single cairn
  is still possible but slow: each wait is 20 real minutes, the hour on the
  next turn is effectively random because the dimension's day runs at a
  changing speed, and a four-piece set needs about eight random draws on
  average. Walking past four cairns at four different hours is much faster,
  which is the intended way to play.
- Only an accepted turn consumes the cooldown. Setting a keystone (below)
  does not touch the cooldown, and a turn during cooldown does not reset it.
- Sleeping advances day time but not game time, so sleeping does not shorten
  the cooldown (roadmap decision). Sleeping can bring the dawn hour closer
  for the next cairn, and only the dawn hour. Accepted.

### Variable day length

Chrono Dawn's day runs at 0.67x to 5x of game time (`TimeFlowSettings`
defaults), and the speed changes every 1200 to 6000 game ticks. For the
cairn this means:

- An hour quarter lasts between 1 and 7.5 real minutes, 5 minutes at 1x.
  The player sees the sky change, and the message names the stone, so a
  fast day is never confusing.
- The set fills faster than in the Overworld, which is the point. Nothing
  in the design depends on a quarter lasting a fixed real time.
- The cooldown is in game time and does not vary with the day speed.
- With time flow disabled in the config, the dimension runs a normal day
  and all of the above still holds.

### Cairn Stones

Four items: `cairn_stone_dawn`, `cairn_stone_noon`, `cairn_stone_dusk`,
`cairn_stone_midnight`, named "Dawn Stone", "Noon Stone", "Dusk Stone",
"Midnight Stone". They stack normally. Each has one tooltip line of flavor
(my call, final wording is the builder's):

- Dawn: "A cairn stone engraved with the first light."
- Noon: "A cairn stone engraved with the sun at its height."
- Dusk: "A cairn stone engraved with the last light."
- Midnight: "A cairn stone engraved with a moonless hour."

Textures are new pixel art, not tinted vanilla textures (roadmap lesson):
one small rounded stone, each with a different engraving (a half sun on the
horizon, a full sun, a half sun with rays below, a crescent moon). The
maintainer hand-edits textures, so the builder drafts them and flags them
for review.

### Combining the four stones

A shapeless recipe of one stone of each hour gives one **Cairn Keystone**
(`cairn_keystone`), "Cairn Keystone". Four items fit the 2x2 grid in the
inventory screen, so the player can craft it at the cairn where the fourth
stone appeared. The recipe is unlocked when the player picks up any Cairn
Stone, following the journal recipe in part 2, in every recipe root
(`common/1.20.1`, `common/1.21.1`, `common/shared-1.21.2+`) with a
recipe-unlock advancement in every era including the 26.3 schema.

The keystone's tooltip tells the player what to do with it (my call):
"The four hours of a day, bound in one stone." and "Set it into a Time
Cairn."

### Setting a keystone

Right-clicking a cairn dial while holding a Cairn Keystone in the main hand
or the off hand (my call, matching the memorial's either-hand rule) sets
the keystone instead of turning the dial:

- One keystone is consumed.
- The loot table `chronodawn:gameplay/time_cairn_keystone` is rolled with
  the player as `this` entity and luck, like the memorial's offering, and
  the result pops out on top of the dial the same way a stone does.
- A longer particle effect plays, suggesting a full day passing over the
  dial (my call: the four hour colors in sequence rising from the dial, then
  `END_ROD` sparks), with a sound that is clearly different from the turn.
  Candidates for the maintainer to compare: `BLOCK_ENCHANTMENT_TABLE_USE`
  is taken by the well, `ITEM_LODESTONE_COMPASS_LOCK` by the camp, so try
  `BLOCK_BEACON_POWER_SELECT`, `BLOCK_AMETHYST_CLUSTER_BREAK` or
  `BLOCK_RESPAWN_ANCHOR_CHARGE`. No bell sounds.
- Action-bar message: "The cairn remembers a whole day."
- Setting a keystone has no cooldown and does not consume the turn
  cooldown. The stones themselves are rate limited, so the keystone needs no
  limit of its own.
- A keystone used on cooldown for turning still works. A keystone used on
  a player-placed `clockwork_dial` does nothing, because only the activator
  block has this behavior.

Why a keystone and not a reward straight from the recipe: a recipe result
is fixed, while a loot table gives variety and lets modpack authors tune or
replace the reward with a datapack, consistent with parts 2 to 4. Why set it
at a cairn rather than use it anywhere: the player who crafts the fourth
stone is standing at a cairn, so the reward happens where the collection
finished, and the keystone pulls players back to cairns later, which is the
whole purpose of the wayside effort. The maintainer favored collections that
feed back into their own feature (part 2's journal), so this follows that.

### Keystone reward table

`chronodawn:gameplay/time_cairn_keystone`, one pool, exactly one roll, plus a
small supplies pool. The cairn is a pile of stones, so it gives stones (my
call on the theme and the ordering, weights and counts are the builder's
tuning):

| Item | Count | Weight | Why |
|---|---|---|---|
| `time_crystal` | 2 to 3 | high | The ore in the cairn's base, always welcome |
| `clockstone` | 3 to 5 | high | Common Chrono Dawn ore, fits "stones" |
| `entropy_crystal` | 1 | medium | Rare non-boss material the maintainer likes as a wayside reward |
| `enhanced_clockstone` | 1 | low | Also drops from bosses and the Clockwork Sentinel, not a shortcut |
| `ancient_gear` | 1 | low | Same reasoning as the camp table |

Supplies pool (one roll): `clockwork_cookie` 1 to 2, `baked_temporal_root`
2 to 3, `torch` 4 to 8, equal weight.

A player who sets about four keystones should usually have seen one Entropy
Crystal. Excluded on purpose: saplings (the camp's job), Chronite (Overworld
ore), every boss-only material, Clockwork Mechanism. The 1.20.1 table
(`loot_tables/`, `"name"` references) and the 26.3 schema copy carry the
same content.

### The midnight echo

When an accepted turn gives a **Midnight Stone**, the cairn rolls the echo
chance (config `time_cairn_echo_chance`, default 0.2, 0 disables the echo).
The chance is rolled only on accepted midnight turns, never during cooldown
and never for the other three hours. With four cairns a day a player meets
an echo about once a week of play. That is "rare" in the roadmap's sense,
and the default is a tuning starting point.

When the roll succeeds:

1. **The cache is placed first.** The cairn picks a spot 16 to 24 blocks
   away (see "Cache placement"). If no valid spot exists, there is no echo
   this time, the stone is still given, and nothing else changes. The
   chance is not saved up.
2. **The echo appears.** A loose column of particles about the height of a
   player rises next to the cairn (my call: `SOUL` particles for the body
   with a few `END_ROD` sparks, chosen because `REVERSE_PORTAL` already
   means the Temporal Echo Decoy). A soft sound plays once at the cairn.
   Candidates for the maintainer to compare with `/playsound`:
   `PARTICLE_SOUL_ESCAPE`, `BLOCK_RESPAWN_ANCHOR_AMBIENT`,
   `ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM`, `BLOCK_SCULK_CATALYST_BLOOM`. No
   bell sounds, nothing that reads as a hostile mob.
3. **The echo walks.** The particle column moves in a straight line from
   the cairn to the cache at a slow walking pace (my call: 0.08 blocks per
   tick, about 1.6 blocks per second, so the walk takes 10 to 15 seconds).
   Its height follows the surface under it, so it glides over bushes and
   water. It passes through anything in the way, it is a ghost.
4. **The echo kneels and fades.** At the cache the column sinks into the
   ground over about a second and ends with a small burst. For the next
   minute a few `SOUL` particles drift up from the cache spot now and then,
   so a player who looked away still finds it.
5. **Message** to the player who turned the dial, when the echo appears:
   "A pale figure rises from the cairn and walks away." Nothing else is
   written. The echo never speaks.

Everyone nearby sees the particles. The cache is a normal chest, so whoever
digs it up gets it. On a shared server another player can take it; the
event is rare and this is accepted.

The echo is presentation only. Its state (start time, start and end
position) is kept in the cairn's block entity in memory and is not saved.
If the chunk unloads or the server stops mid-walk, the echo simply does not
resume. The cache is already in the world, so the player loses only the
show.

### The buried cache

The cache is a vanilla `minecraft:chest` one block below the surface with a
marker block on top of it, so the player digs one block and finds the
chest. The marker is a natural block that stands out from the ground around
it (my call): `coarse_temporal_dirt` on temporal grass or dirt,
`minecraft:coarse_dirt` on vanilla grass or dirt, `temporal_gravel` on
temporal sand, and `coarse_temporal_dirt` under snow (the snow layer is
removed so the marker shows). The builder may simplify this to one or two
marker blocks if the biome match is awkward; the rule to keep is "visible
as freshly dug, and natural".

The chest gets the loot table `chronodawn:chests/time_cairn_echo_cache` and
a random seed. It is a plain chest with a loot table, so Lootr converts it
into a per-player chest when Lootr is installed (part 4 lesson), and Chrono
Dawn adds no rules on top. The chest faces a random horizontal direction.

Cache contents, one pool with exactly one roll and one supplies pool (my
call on the ordering, tuning is the builder's). The echo is the rare event,
so the cache is a little better than the keystone:

| Item | Count | Weight | Why |
|---|---|---|---|
| `entropy_crystal` | 1 to 2 | high | The traveler's find |
| `enhanced_clockstone` | 1 | medium | Not a shortcut, see above |
| `ancient_gear` | 1 | medium | Theme fit |
| `dark_time_wood_sapling` | 1 | low | No other source besides the camp |
| `ancient_time_wood_sapling` | 1 | low | Same |
| `time_crystal` | 2 to 4 | medium | Fallback that is still welcome |

Supplies pool (1 to 2 rolls): `minecraft:clock`, `minecraft:compass`,
`dawn_lantern` or `dusk_lantern` 1, `clockwork_cookie` 1 to 2, equal
weight. A traveler's things.

Excluded: boss-only materials, Clockwork Mechanism, Chronite, Cairn Stones
(the cache must not shortcut the collection).

### Cache placement

The cairn tries up to eight evenly spaced directions starting from a random
angle, at a random distance of 16 to 24 blocks, and accepts the first spot
that passes all of these:

- The chunk is loaded (the player is at the cairn, so this normally holds
  for every spot within 24 blocks).
- The surface column has a top solid block T whose block is in the cairn
  feature's ground list (grass, dirt, podzol, coarse dirt, snow block,
  temporal grass, temporal dirt, coarse temporal dirt, temporal sand). A
  snow layer (`minecraft:snow`) on top of T is allowed and is removed; the
  layer itself is never T. The list is kept as a block tag
  `chronodawn:time_cairn_cache_ground` with the same entries as the placed
  feature's list minus the snow layer, so a datapack can extend it (my
  call).
- The block below T is solid, has no block entity and is not a fluid.
- Neither T nor the block below is part of another cairn's three blocks
  (so the cache never overwrites a dial) and the spot is at least 4 blocks
  from the cairn's own base.

The chest replaces the block below T, the marker replaces T. No other block
is touched. If all eight directions fail, there is no echo.

The surface height comes from the `MOTION_BLOCKING_NO_LEAVES` heightmap, so
a spot under a tree canopy is fine and a spot on a leaf block is not
chosen, because leaves are not in the ground list.

### Breaking the dial

Breaking `time_cairn_dial` drops one `clockwork_dial`. Cooldowns for that
cairn are gone with the block entity, which is the same as the other parts.
A running echo stops, its cache stays.

### Cairn Stones in the Time Well (optional, data only)

The Time Well accepts any `chronodawn` item that has a table under
`gameplay/time_well/`. Adding four tables lets a player throw a duplicate
stone into a well: common result `clockstone` x1, rare result one of the
other three stones at equal weight. This is a sink for duplicates and a
small cross-feature touch, and it needs no code. One wish per well per day
keeps it from shortcutting the collection. (My call, marked optional. The
builder includes it unless the maintainer removes it.)

## Internal structure

Same layout as parts 1 to 4. Code that touches renamed vanilla API lives in
each version module, everything else in `common/shared`.

### Pure logic (`common/shared`, no Minecraft dependency)

- **`CairnHour`** enum in `com.chronodawn.core.wayside`: `DAWN`, `NOON`,
  `DUSK`, `MIDNIGHT` with `forDayTime(long)` implementing the quarter table,
  plus per-hour message key, sound pitch and particle color. Unit tested at
  every boundary.
- **`EchoWalk`** (name is the builder's choice): given start, end, start
  game time and speed, returns the horizontal position at a game time and
  whether the walk is finished, so the block entity's ticker is a thin
  wrapper. Unit tested.
- **`CachePlacement`** helper for the eight-direction search order and the
  distance range, kept pure so the search order is testable. The world
  checks stay in the block entity.

### Block entity (`common/shared`, era copies where needed)

- **`TimeCairnDialBlockEntity`** extends `CooldownActivatorBlockEntity`. It
  adds no saved data, so the three era copies of the parent are enough and
  this class needs none. The echo state is transient fields.
- Server ticker, only while an echo or its lingering particles are active.
  A cairn with nothing going on does not tick (the block returns a ticker
  only when needed, or the ticker returns immediately). Most cairns in the
  world are idle, so this matters.

### Block (all 14 version modules)

- **`TimeCairnDialBlock`** copies `clockwork_dial`'s shape, strength, sound
  and tool, and implements `EntityBlock`.
- Use handling: a keystone in either hand goes to `setKeystone`, anything
  else goes to `turn`. The use-method signatures differ by era, so each
  module follows its own `SundialGnomonBlock` and `WatchmakerChestBlock`.
- `turn(ServerLevel, BlockPos, Player, long dayTime)` is public static and
  takes the day time as a parameter, so GameTests can ask for each hour
  without changing the level's day time (tests share one level). It returns
  what happened (hour given, cooldown, echo started) for assertions.
- `setKeystone(ServerLevel, BlockPos, Player)` likewise public static,
  returns whether a keystone was consumed.
- `summonEcho(ServerLevel, BlockPos, Player, RandomSource)` public static,
  places the cache and starts the echo, returns the cache position or empty.
  `turn` calls it after the chance roll, tests call it directly.
- Loot table rolling follows `MemorialPlateBlock.rollOffering` and the
  era-split lookup (`getLootData` on 1.20.1, `reloadableRegistries` later).
  Setting the chest's loot table uses `RandomizableContainer.setLootTable`,
  which takes a `ResourceLocation` on 1.20.1 and a `ResourceKey<LootTable>`
  from 1.21.1.
- Reading the day time goes through the module's `CompatLevelTime.dayTime`
  (all 14 modules have it).

### Items

- Four `CairnStoneItem` instances (or plain `Item` with a tooltip helper,
  builder's choice) and one `CairnKeystoneItem`, registered in every
  version's `ModItems`, added to the creative tab next to the journal items,
  with `ModItemId` entries.
- Mirror the Faded Journal Page's file footprint exactly. `grep -rl
  faded_journal_page_1` lists every place a plain item needs: item model in
  `common/shared`, client item JSON in `common/1.21.4` and
  `common/shared-1.21.5+`, lang in the three lang sets, textures in
  `common/shared`.

### Registration

- `ModBlockId.TIME_CAIRN_DIAL`, `ModBlockEntityId.TIME_CAIRN_DIAL`.
- Block and block entity in every version's `ModBlocks` and
  `ModBlockEntities`. No `BlockItem`.
- No entity registration.

### Resources

- Blockstate and model for `time_cairn_dial` reusing the `clockwork_dial`
  model. If the dial model lacks a `render_type`, it is a full solid-looking
  plate and needs none; check before copying the cutout rule from part 1.
- Block loot table dropping `clockwork_dial`, and the same mineable tag as
  `clockwork_dial`.
- Loot tables `gameplay/time_cairn_keystone` and
  `chests/time_cairn_echo_cache` in the three loot table roots
  (`common/1.20.1` `loot_tables/`, `common/shared-1.21.1+`, `common/26.3`
  schema). Optional `gameplay/time_well/cairn_stone_<hour>.json` x4 in the
  same three roots.
- Recipe `cairn_keystone` in the three recipe roots, each in its era's
  format (object ingredients and result `"item"` on 1.20.1, result `"id"` on
  1.21.1, string ingredients from 1.21.2), plus recipe-unlock advancements
  under `recipes/misc` in every era, with the 26.3 `"recipes"` key.
- Advancement `exploration/time_cairn_stones` ("A Full Day", goal frame,
  parent `story/us1/dimension_entry`), granted when the player has held all
  four stones, following `exploration/lost_adventurer_journal`. Advancement
  copies where the era needs them (26.3).
- Block tag `chronodawn:time_cairn_cache_ground`.
- Translation keys in `en_us` and `ja_jp`, in all three lang sets: block
  name, five item names, five tooltip lines (plus the keystone's second
  line), four "gives up a stone" messages, the cooldown message, the
  keystone message, the echo message, and the advancement title and
  description. Run `validateTranslations` and the sound-subtitle check if a
  custom sound event is added (none is planned).
- `time_cairn.nbt` in `common/shared-1.21.1+`, edited directly with nbtlib:
  `chronodawn:time_cairn_dial` at (1, 2, 1). The 1.20.1 copy is generated by
  `convertNbt`. `StructureTests` in `common/gametest` and its 26.3 copy
  expect `time_cairn_dial` in the template.

### Config

Two keys in `[gameplay.wayside]`, following the camp key in
`WaysideSettings`, `ConfigDefaults`, `ConfigLoader`,
`chronodawn-default-config.toml`, `ConfigLoaderTest` and
`docs/configuration.md`:

| Key | Default | Range | Meaning |
|---|---|---|---|
| `time_cairn_cooldown_ticks` | 24000 | 20 to 1728000 | Per-player cooldown for one cairn |
| `time_cairn_echo_chance` | 0.2 | 0.0 to 1.0 | Chance that an accepted midnight turn summons the echo. 0 disables it |

Rewards, cache contents and the cache ground list stay in data (loot tables
and a tag) so there is one place to tune them and datapacks can change
them, as in parts 2 to 4.

## Error handling

- Saved cooldowns fail safe like parts 1 to 4: unreadable data starts an
  empty tracker.
- A reward table that exists but rolls nothing still consumes the keystone
  and shows the message, because a datapack that empties it chose that.
  (My call; the alternative, refusing the keystone, would make an emptied
  table look like a bug to the player.)
- A missing or empty cache table leaves an empty chest. Accepted, that is
  the datapack author's choice.
- No valid cache spot means no echo and no error. The stone is still given.
- The ticker does nothing on the client and does nothing when no echo is
  active.
- If the day-time accessor fails in some future era, the block falls back
  to `DAWN` and logs once. (My call.)

## Testing

- **Unit tests** (`common/shared/src/test`):
  - `CairnHour.forDayTime` at every boundary (20999 and 21000, 2999 and
    3000, 8999 and 9000, 14999 and 15000) and with negative and very large
    day times.
  - `EchoWalk` position at start, midway and after the end, and the
    finished flag.
  - `CachePlacement` yields eight distinct directions and distances inside
    16 to 24.
  - `ConfigLoaderTest` for the two new keys, defaults and clamping.
- **GameTest** (`common/gametest`, registered in every
  `RegistryDrivenTestGenerator`). Players are `makeMockServerPlayer`,
  removed in a `finally` block.
  - placing `time_cairn_dial` gives a `TimeCairnDialBlockEntity`
  - `turn` at day times 0, 6000, 12000 and 18000 spawns exactly one item of
    the matching stone near the dial; the 1.20.1 world is reused across
    runs, so item searches ignore entities older than the test
  - a second `turn` by the same player within the cooldown spawns nothing
    and reports cooldown; a different player can still turn
  - `setKeystone` with a keystone in the main hand consumes one and spawns
    at least one item from the keystone table; in the off hand likewise;
    without a keystone it reports no keystone
  - `setKeystone` works while the player is on turn cooldown, and does not
    change the remaining cooldown
  - `summonEcho` on a flat grass platform places a chest with the cache loot
    table 16 to 24 blocks away with the expected marker on top; on a
    platform of water or air it returns empty and places nothing
  - a Midnight `turn` with the echo chance forced to 1 reports an echo and a
    cache exists; with the chance 0 it reports none
  - `time_cairn_dial` drops one `clockwork_dial`
  - the four stones craft into one keystone (follow how
    `MemorialPlateTests` checks the journal recipe)
  - `StructureTests` and its 26.3 copy expect `time_cairn_dial` in
    `time_cairn.nbt`
  - optional: each Cairn Stone thrown into a Time Well yields one stack
    from its table
- **Validation**: `validateResources validateTranslations validateData
  validateEraTagParity`, then `buildAll`, `testAll` and `gameTestAll` run
  separately (roadmap lesson), reading `build/gametest-*.log`.
- **Production smoke test** on one Fabric and one NeoForge version, because
  a new block entity and block are added.
- **Manual check** on 1.21.1 NeoForge and Fabric, 1.20.1 Forge, 26.3
  NeoForge and 26.2 Fabric, at a cairn in newly generated chunks: the dial
  looks unchanged, turning it pops a stone that matches the sky with
  visible particles and an audible sound, the cooldown message appears on
  the second try, four stones craft in the 2x2 grid, setting the keystone
  gives the reward with a clearly different effect, and a midnight turn
  with `time_cairn_echo_chance = 1.0` shows an echo walking to a spot where
  a marker block sits on a chest with loot. Breaking the dial drops a
  Clockwork Dial and a player-placed Clockwork Dial does nothing on
  right-click. GameTest servers may omit the Chrono Dawn dimension, so this
  uses a normal client, and the maintainer takes a screenshot of the echo
  before particle polish (roadmap lesson: show it in play first).

## Known trade-offs

- A player can camp one cairn and collect a set in a few hours of waiting.
  Walking is far faster, so this is accepted rather than adding per-hour
  memory to each cairn.
- Duplicates accumulate. The optional Time Well tables give them a use; if
  the maintainer drops that, duplicates are just extra stones.
- The echo is particles, not a figure. It will not look like a person up
  close. Chosen to avoid an entity with a model and renderer in 14 modules;
  a rendered echo can replace it later without changing the cache or the
  stones.
- The echo path ignores obstacles, so it can pass through a tree trunk or a
  cliff face. Accepted for a ghost.
- Another player can dig up the cache first. Rare; Lootr makes it per
  player when installed.
- A cairn in a cave or on a one-block ledge may never find a valid cache
  spot. The stone is still given, so nothing is lost.

## Documentation

- `docs/player_guide.md`: a Time Cairn paragraph next to the other wayside
  features: turning the dial, the four stones and their hours, the keystone,
  the midnight echo and the cache, the cooldown, what breaking the dial
  drops, and the config keys.
- `docs/configuration.md`: the two keys.
- `CHANGELOG.md` Unreleased: add the feature.
- Roadmap: mark part 5 shipped and add its lessons.
