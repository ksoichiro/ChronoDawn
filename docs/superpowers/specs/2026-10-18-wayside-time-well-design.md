# Design: Wayside Features, Part 3 (Time Well)

**Created**: 2026-10-18
**Status**: Draft

---

## Why

The [roadmap](./2026-10-14-wayside-features-roadmap.md) explains the whole
effort: long walks between major structures have nothing to discover, so
each of the six small surface features gets a reason to stop. Parts 1 and 2
shipped the cooldown foundation, the Old Sundial and the Lost Adventurer
Memorial.

Part 3 turns `time_well` into a wishing well. A player throws in something
of the present and something of the past rises back out. The well rewards
players who carry the Chrono Dawn materials and food they already gather
while exploring, by upgrading one of them a little, with a small chance of
something better.

Journal page 3 of part 2 already tells of "a roadside well, amber at its
bottom" where "someone made wishes", so this part also pays off that hint.

## Goals

- Throwing a suitable item into the well turns one of it into a slightly
  better item.
- A small chance of a different, higher item makes each wish exciting.
- Nothing the player throws in is ever lost.
- Mass conversion is impossible: one item per wish, one wish per player per
  well per cooldown.

## Non-goals and anti-goals

- Not a general item converter. Only explored-for Chrono Dawn materials and
  lightly processed food are accepted (decided).
- It must not become a reliable source of gated materials. Enhanced
  Clockstone is normally obtained only from the Desert Clock Tower boss,
  its chests and Clockwork Sentinels, so the well gives it only at a low
  chance (decided).
- Existing wells in already-generated chunks are not converted (roadmap).
- No change to placement or rarity (rarity filter 1 in 32 chunks stays).

## Scope of part 3

In scope:

- The `time_well_stone` activator block and its block entity
- Detection of thrown items in the well water
- The exchange, the return of everything not exchanged, and the
  presentation (particles, sound, message)
- Exchange loot tables, one per accepted item
- A config key for the well cooldown
- Adding water and the activator to `time_well.nbt`

Out of scope: parts 4 to 6, conversion of existing worlds, any change to
structure placement or rarity.

## Current structure

`time_well.nbt` is 3x5x3, placed with `y_offset: -1`, so structure y = 0 is
the top ground block and y = 1 is the first block above ground. Decoded
layout (x, y, z):

| Position | Block |
|---|---|
| (1, 0, 1) | `temporal_amber_ore` (bottom of the shaft) |
| y = 1, ring around (1, 1, 1) | `temporal_cobblestone` at the corners, `mossy_temporal_cobblestone` at the four sides |
| (1, 1, 1) | air (shaft) |
| y = 2, ring around (1, 2, 1) | `mossy_temporal_cobblestone_wall` at the corners, `temporal_cobblestone` at the four sides |
| (1, 2, 1) | air (shaft) |
| (0, 3, 2), (2, 3, 2) | `time_wood_log` (posts) |
| (1, 4, 2) | `time_wood_slab` |
| everything else | air |

There is no water. The shaft is the two air blocks above the ore, enclosed
on all four sides at both levels.

## Player-facing behavior

### Appearance

- Water fills the lower shaft block (1, 1, 1). The upper shaft block
  (1, 2, 1) stays air, so the water surface sits one block below the rim
  like a real well. The amber ore stays at the bottom, visible through the
  water. (My call: one block of water instead of two, for the look and so
  thrown items float where the player cannot reach them by accident.)
- One of the four `mossy_temporal_cobblestone` blocks next to the water,
  (1, 1, 0), becomes `chronodawn:time_well_stone`. It looks the same as
  `mossy_temporal_cobblestone`. (My call.)

### Making a wish

A player drops (Q key or from the inventory screen) an item stack so that
it lands in the well water.

About 2 seconds (40 ticks) after the item enters the water, the well
answers:

- **Exchange.** If the item is accepted (see the table below) and the
  thrower is not on cooldown for this well, one item of the stack is
  consumed and the exchange loot table for that item is rolled. The result
  rises out of the well.
- **Return.** Everything not exchanged rises out of the well unchanged in
  the same moment: the rest of the stack, items the well does not accept,
  and anything thrown during cooldown (decided).
- **Presentation.** On an exchange, `GLOW` particles spread over the water
  surface and rise up the shaft with a few `END_ROD` sparks, and
  `ENCHANTMENT_TABLE_USE` plays at the well (decided in direction, the sound
  is my call and to be confirmed in play-testing; it avoids the sundial's
  note block chime, the memorial's beacon sound and the Time Distortion
  bell). A plain return only plays a small splash
  (`GENERIC_SPLASH`) with a few bubbles.
- **Rising items.** Items spawn just above the rim and are tossed toward
  the thrower, landing outside the well, so they do not fall back in
  (decided). If the thrower is not online or not within 16 blocks, they
  pop straight up and land beside the well. Before the toss the well
  checks the blocks the arc passes over the rim. If something with a
  collision shape is in the way, like the roof slab or the log posts, the
  items go to the clear side closest to the intended direction (the thrower, or the random direction when there is none) instead. The template
  is placed rotated, so this is a geometric check, not a fixed side.
- **Message.** An action-bar line to the thrower:
  - exchange: "The well answers your wish"
  - not accepted: "The well has no use for this"
  - cooldown: "The well is still. Try again in about N minutes", using
    `PlayerCooldownTracker.toMinutesRoundedUp` like the sundial.

Only the thrown stack's item type matters. Names, enchantments and other
components do not change acceptance, and the result is a fresh item.

### Cooldown

- Per player, per well: 24000 ticks (one Minecraft day) by default, counted
  in game time like the sundial (decided in the roadmap).
- Only an exchange consumes the cooldown. Returning an unaccepted item or
  returning during cooldown does not (my call).
- If one player throws several stacks at once, they are answered in the
  order they entered the water: the first accepted one is exchanged and
  everything else returns.

### Who counts as the thrower

The item entity's thrower UUID decides whose wish it is and whose cooldown
is used, but only a player counts. When the well first sees an item, it
resolves the thrower with `level.getPlayerByUUID`. Items whose thrower is
not a player in that level are ignored: the well neither exchanges nor
returns them. This covers dispensers, droppers, broken containers, mob
drops, and mobs that set themselves as thrower (allays delivering items,
piglin bartering and villager gifts go through `BehaviorUtils.throwItem`,
which calls `setThrower`). Otherwise a row of allays would automate the
well. (My call. A player can still fish an ignored item out by emptying the
well.)

Items the well itself raises have no thrower either, but they are not
ignored. Each one carries a saved entity tag. If such an item falls back
into the shaft anyway, for example because a player built over the rim on
every side, the well queues it again as returned to a thrower who is gone.
It is raised again after the usual delay, with no message and no
cooldown. The new stack gets a fresh tag. This repeats at most once every
40 ticks per item, so it cannot spin, and nothing a player threw is lost.
Items with neither this tag nor a player thrower stay ignored.

If the thrower was a player when the item entered but has left the level by
the time the well answers, the item is returned straight up without an
exchange and no cooldown is consumed.

### Exchange table

Each accepted item has its own loot table. The well accepts an item when a
table exists for it and is not empty. Probabilities are entry weights, so
modpack authors tune them or add new items with a datapack (my call; see
"Config" for why probabilities are not in the config file).

Initial values. "Common" and "rare" are the two weighted entries of one
pool rolled once. The normal results come from the table agreed in
brainstorming. Counts and weights are my call.

| Thrown | Common result (weight) | Rare result (weight) |
|---|---|---|
| `time_wheat` | `time_bread` x1 (9) | `golden_time_wheat` x1 (1) |
| `time_bread` | `enhanced_time_bread` x1 (9) | `time_fruit_pie` x1 (1) |
| `chrono_melon_slice` | `chrono_melon_juice` x1 (9) | `glistening_chrono_melon` x1 (1) |
| `raw_temporal_amber` | `temporal_amber_dust` x2 to 3 (9) | `time_crystal` x1 (1) |
| `clockstone` | `clockstone` x2 (9) | `time_crystal` x1 (1) |
| `time_crystal` | `time_crystal` x2 (19) | `enhanced_clockstone` x1 (1) |

Enhanced Clockstone appears only in the `time_crystal` row and at 1 in 20,
because Time Crystal is the rarer input and Enhanced Clockstone gates
progression (decided: low chance only; which row and the rate are my
call). With one wish per well per day and wells at 1 in 32 chunks, this
does not replace the Desert Clock Tower.

## Internal structure

Same layout as parts 1 and 2. Code that touches renamed vanilla API lives
in each version module, everything else in `common/shared`.

### Pure logic (`common/shared`, no Minecraft dependency)

- **`WellQueue`** (name is the builder's choice) in
  `com.chronodawn.core.wayside`: remembers when each item entity UUID was
  first seen in the water, `offer(UUID, gameTime)` and `due(gameTime)`
  returning the entries older than `ANSWER_DELAY_TICKS` (40) in first-seen
  order, and `forget(UUID)`. Entries for entities no longer present are
  dropped. Not persisted: after a chunk reload an item still in the water
  is simply seen again and answered 40 ticks later, so nothing is lost.

### Block entity (`common/shared`)

- **`TimeWellStoneBlockEntity`** extends `CooldownActivatorBlockEntity` and
  owns a transient `WellQueue`. It adds no saved data, so it needs no era
  copies.
- Server-side ticker, every tick (one small AABB query per loaded well): find the water source among the
  block's four horizontal neighbours (the structure is randomly rotated, so
  the block cannot know the direction in advance; cache the result and
  recheck if that block is no longer water), list `ItemEntity`s whose
  bounding box intersects that water block, offer those with a thrower to
  the queue, and answer the due ones.
- While an item waits it is set to never be picked up
  (`setNeverPickUp`). Besides stopping anyone from taking it mid-wish, this
  stops it merging with another stack: `ItemEntity` only merges entities
  whose pickup delay is not infinite, and its merge check compares `target`
  but not the thrower, so two players' identical items could otherwise
  merge into one and lose one player's wish. The returned items get a
  normal pickup delay again.

### Block (all 14 version modules)

- **`TimeWellStoneBlock`** behaves like `mossy_temporal_cobblestone`
  (strength, sound, tool) and implements `EntityBlock` with the server
  ticker.
- `answer(ServerLevel, BlockPos, ItemEntity)` (public static, like
  `MemorialPlateBlock.onPlayerInside`) performs one answer so GameTests can
  drive it without waiting for the ticker. It returns what happened
  (exchanged, not accepted, cooldown, ignored) for assertions.
- `rollExchange(ServerLevel, BlockPos, Item)` returns the result stacks, or
  an empty list when the item has no table. The loot table lookup differs
  by era (`getLootData` on 1.20.1, `reloadableRegistries` later), following
  `MemorialPlateBlock.rollOffering`. A missing table is told apart from an
  empty roll by comparing with `LootTable.EMPTY`.
- Reading the thrower UUID from `ItemEntity` differs by era. Follow each
  version's API.

### Registration

- `ModBlockId.TIME_WELL_STONE` and `ModBlockEntityId.TIME_WELL_STONE`.
- Block and block entity in every version's `ModBlocks` and
  `ModBlockEntities`. No `BlockItem`.

### Resources

- Blockstate and model for `time_well_stone` reusing the
  `mossy_temporal_cobblestone` model.
- Block loot table dropping `mossy_temporal_cobblestone`, and the
  `minecraft:mineable/pickaxe` tag entry, matching the plain block.
- Exchange loot tables at
  `gameplay/time_well/<item path>.json` (for example
  `gameplay/time_well/clockstone.json`), in the three places loot tables
  live: `common/1.20.1` (`loot_tables`), `common/shared-1.21.1+`, and
  `common/26.3` in the 26.3 schema. Only `chronodawn` items are looked up
  under this path. Accepting items from other namespaces is left for
  later.
- Translation keys in `en_us` and `ja_jp` for the block name and the three
  action-bar messages, in all three lang sets (1.20.1, 1.21.1,
  `shared-1.21.2+`).
- `time_well.nbt` in `common/shared-1.21.1+`, edited directly with nbtlib:
  `minecraft:water` (level 0) at (1, 1, 1), `chronodawn:time_well_stone` at
  (1, 1, 0). The 1.20.1 copy is generated by `convertNbt`. `NbtTemplateFeature`
  places templates with plain `placeInWorld` and default settings (liquids
  kept, no processors), so a `minecraft:water` palette entry is placed as
  is. The waterlogging-prevention processors that made
  `chronodawn:decorative_water` necessary only run for jigsaw structures,
  and `decorative_water` itself relies on a processor, so it is not an
  option here. Confirm in a fresh world that the water is present and
  does not spread. If it turns out missing, the fallback is for the block
  entity to place the water source itself on its first tick, without
  touching `NbtTemplateFeature`, which all six wayside features share.

### Config

One key in `[gameplay.wayside]`, following the memorial key in
`WaysideSettings`, `ConfigDefaults` and `ConfigLoader`:

| Key | Default | Range | Meaning |
|---|---|---|---|
| `time_well_cooldown_ticks` | 24000 | 20 to 1728000 | Per-player cooldown for one well |

Probabilities and results stay in the loot tables, as the memorial's do, so
there is one place to tune them and datapacks can change them. (In
brainstorming I said the rates would be configurable. I chose datapack
tuning over config keys to follow part 2. The maintainer may ask for
config keys instead.)

## Error handling

- Saved cooldowns fail safe the same way as parts 1 and 2: unreadable data
  starts an empty tracker.
- A table that exists but rolls nothing (emptied by a datapack) counts as
  not accepted: the item returns and no cooldown is consumed.
- If the water block is gone (a player broke the well), the ticker does
  nothing.
- The ticker does nothing on the client.

## Testing

- **Unit tests** (`common/shared/src/test`), for the queue:
  - an item is not due before 40 ticks and is due after
  - due entries come out in first-seen order
  - offering the same UUID again keeps the first-seen time
  - forgotten and vanished entries are dropped
- **GameTest** (`common/gametest`, registered in every
  `RegistryDrivenTestGenerator`). Throwers are real joined players from
  `makeMockServerPlayer`, removed in a `finally` block, because the well
  resolves throwers with `getPlayerByUUID`. Two mock players cover the
  "different thrower" case.
  - placing `time_well_stone` gives a `TimeWellStoneBlockEntity`
  - each of the six exchange tables yields exactly one stack from its pool
  - an item without a table, for example `minecraft:dirt`, is returned
    unchanged and the cooldown is not consumed
  - a stack of 5 `clockstone` yields one exchange and 4 returned `clockstone`
  - a second wish by the same thrower within the cooldown returns the item
    unchanged; a different thrower can still wish
  - an item with no thrower, or with a thrower UUID that is not a player in
    the level, is ignored and left in place
  - a waiting item cannot merge with another stack
  - `time_well_stone` drops one `mossy_temporal_cobblestone`
  - `StructureTests` in `common/gametest` and its 26.3 copy expect
    `time_well_stone` and `water` in `time_well.nbt`
- **Validation**: `validateResources validateTranslations validateData
  validateEraTagParity`, then `buildAll`, `testAll` and `gameTestAll` run
  separately.
- **Manual check** on 1.21.1 NeoForge and Fabric, 1.20.1 Forge, 26.3
  NeoForge and 26.2 Fabric, at a well in newly generated chunks: the water
  stays put, a thrown Clockstone is answered after about 2 seconds with
  visible glow and an audible sound, results land outside the well near the
  player, a stack returns its rest, dirt comes back, the cooldown message
  appears. GameTest servers may omit Chrono Dawn dimensions, so this uses a
  normal client.

## Known trade-offs

- A player who wants a result again must find another well or wait a day.
  Intended.
- Items without a thrower stay in the water and can despawn there after
  five minutes. Only non-player sources produce them. Accepted. Items the
  well raised are the exception: they carry a tag, so one that falls back
  is raised again. If every side of the rim is built over, it keeps rising
  and falling back about every three seconds until a player takes it.
- Chunk unload during the 2-second wait delays the answer until the chunk
  is loaded again. Nothing is lost.
- Only `chronodawn` items can be added by datapack under the lookup path.
- Two identical stacks thrown in the same tick can still merge before the
  well first sees them (one tick). Accepted as negligible.
- A player's stack can merge into an ownerless item already lying in the
  water before the well first sees it. That wish is then ignored. Emptying
  the shaft recovers the items. Accepted as rare.

## Documentation

- `docs/player_guide.md`: describe the Time Well, what it accepts and that
  nothing is lost.
- `CHANGELOG.md` Unreleased: add the feature.
- Roadmap status table and part 3 notes: mark part 3 when it ships.
