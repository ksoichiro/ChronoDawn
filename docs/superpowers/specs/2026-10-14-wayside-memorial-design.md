# Design: Wayside Features, Part 2 (Lost Adventurer Memorial)

**Created**: 2026-10-14
**Status**: Draft

---

## Why

The [roadmap](./2026-10-14-wayside-features-roadmap.md) explains the whole
effort: long walks between major structures have nothing to discover, so
each of the six small surface features gets a reason to stop. Part 1 shipped
the cooldown foundation and the Old Sundial.

Part 2 gives `lost_adventurer_memorial` its role. Standing at the memorial
answers a prayer: light pours down and leaves a page of the lost
adventurer's journal and a few supplies. The roadmap noted that "praying"
would otherwise look like right-click gives items, so the trigger and the
presentation are designed to feel like an answered prayer.

## Scope of part 2

In scope:

- The `memorial_plate` activator block and its block entity
- Step-on detection that triggers once per visit
- The light pillar, sound and reward drop
- Six `faded_journal_page_<n>` items and their lore text
- The reward loot table and the "all pages" advancement
- A config key for the memorial cooldown
- Replacing the pressure plate in both memorial NBTs
- The save/load GameTest for `CooldownActivatorBlockEntity`, carried over
  from part 1. It shipped as `5e70c1ec7` before this spec.

Out of scope: parts 3 to 6, conversion of existing worlds, any change to
structure placement or rarity.

## Current structure

Both `lost_adventurer_memorial.nbt` and `lost_adventurer_memorial_snowy.nbt`
are 3x3x3. Decoded layout (x, y, z):

| Position | Block |
|---|---|
| (1, 0, 2) | `temporal_stone_bricks` (headstone base) |
| (1, 1, 2) | `temporal_stone_bricks_wall` (headstone) |
| (1, 2, 2) | `temporal_stone_bricks_slab` (headstone cap) |
| (0..2, 0, 1) | mossy cobblestone slab, brick slab, mossy cobblestone slab |
| (1, 0, 0) | `temporal_stone_pressure_plate` |

The snowy variant adds one snow layer at the four corners of y = 0. The
pressure plate sits in front of the headstone where a visitor would kneel.

## Player-facing behavior

### Appearance

The pressure plate in front of the headstone becomes
`chronodawn:memorial_plate`. It looks the same as
`temporal_stone_pressure_plate`.

### Trigger

A player steps onto the plate. No click is needed. A player walking past
who steps on it discovers the feature by accident, which is intended.

Standing on the plate keeps it pressed but does not trigger again. A player
must step off for about 10 ticks and step back on before the plate tries
again. Mobs, items and other entities press the plate (redstone output is
unchanged) but never trigger a prayer.

### Answered prayer

When the player is not on cooldown for this plate:

- **Light pillar.** `END_ROD` particles fall from about 8 blocks above the
  plate in a narrow column, with downward velocity so they read as light
  pouring down. A ring of `GLOW` particles appears around the plate where
  the light lands.
- **Sound.** `BEACON_ACTIVATE` at the plate. It is clearly audible and
  differs from the Old Sundial (note block chime) and the Time Distortion
  bell. To be confirmed in play-testing.
- **Reward.** The reward loot table is rolled and the stacks spawn as item
  entities about 1.5 blocks above the plate at the praying player's x/z
  (not the plate center) with zero velocity, so they fall onto the player
  with the light and are picked up. Two players on the plate each catch
  their own offering. They do not spawn above the headstone because the plate cannot tell which way the
  structure was rotated.
- **Message.** An action-bar line such as "Your prayer is answered".

### Cooldown

- Per player, per plate: 24000 ticks by default, counted in game time like
  the sundial.
- Stepping on the plate during cooldown shows an action-bar message with
  the approximate remaining minutes, using
  `PlayerCooldownTracker.toMinutesRoundedUp` like the sundial. Nothing else
  happens.

### Rewards

Each prayer rolls `chronodawn:gameplay/lost_adventurer_memorial`:

| Pool | Rolls | Entries |
|---|---|---|
| Page | 1 | `faded_journal_page_1` to `_6`, equal weight |
| Supplies | 1 to 2 | bread 2 to 4, arrow 4 to 8, torch 4 to 8, equal weight |

Pages are chosen at random. Duplicates are allowed, so no per-player record
of owned pages is needed. Modpack authors can replace the table with a
datapack. The praying player is passed to the loot context (`this` entity
and luck), so datapacks can condition on them.

### Journal pages

Six items, `faded_journal_page_1` to `faded_journal_page_6`, named "Faded
Journal Page (1/6)" and so on. Each page's
text is shown as its tooltip in gray italics. Read in order they tell the
story of the adventurer buried at the memorials. Draft text (lines are the
tooltip lines):

**Page 1**

- en: "The portal went dark behind me." / "The ruin scholars wrote of a" /
  "library where the way home is kept." / "I will find it."
- ja: 「背後でポータルの光が消えた。」 / 「遺跡の学者たちは、帰り道を」 /
  「記した書庫があると書いていた。」 / 「必ず見つけてみせる。」

**Page 2**

- en: "The sun here keeps no honest hours." / "Some days crawl, some vanish." /
  "I count them by the old sundials" / "and their shadows instead."
- ja: 「ここの太陽は正直に時を刻まない。」 / 「這うように長い日も、消える日もある。」 /
  「だから古い日時計の影で」 / 「日を数えることにした。」

**Page 3**

- en: "A roadside well, amber at its bottom." / "Its stones are worn smooth by hands." /
  "Someone made wishes here" / "long before I came."
- ja: 「道ばたの井戸の底に琥珀があった。」 / 「縁の石は、人の手ですり減っていた。」 /
  「私より前に、誰かが」 / 「ここで願いをかけたのだ。」

**Page 4**

- en: "Gears turn beneath the earth" / "with no one left to wind them." /
  "I went down to look." / "I almost did not come back up."
- ja: 「地の底で、巻く者もいない歯車が」 / 「回り続けている。」 /
  「降りて確かめに行った。」 / 「危うく戻れなくなるところだった。」

**Page 5**

- en: "The library was real." / "I copied the stabilizer drawings," /
  "but my hands shake too much to build." / "The great clock calls to me instead."
- ja: 「書庫は本当にあった。」 / 「安定化装置の図面は写したが、」 /
  「手が震えて組み立てられない。」 / 「代わりに、大時計が私を呼んでいる。」

**Page 6**

- en: "If you are reading this," / "I did not come back." /
  "Do not stop for the ringing you hear." / "Walk on, traveler."
- ja: 「これを読んでいるなら、」 / 「私は戻らなかったのだろう。」 /
  「聞こえる鐘の音に足を止めるな。」 / 「歩き続けよ、旅人よ。」

Pages 2 and 3 point at the Old Sundial and the Time Well (part 3), and
pages 4 and 5 at Clockwork Depths, the Forgotten Library and the Master
Clock, so the journal also hints at what is out there. The final wording
can change during review without affecting the design.

### Collection and the journal

Holding all six pages grants the advancement
`chronodawn:exploration/lost_adventurer_journal` ("The Lost Adventurer's
Journal").

The six pages and a book craft (shapeless) into the item
`chronodawn:lost_adventurer_journal` ("Lost Adventurer's Journal"). The
recipe is unlocked when the player picks up any page. The journal's tooltip
holds the closing passage, which reveals what became of the adventurer:

- en: "I reached the great clock and heard it strike." / "Time folded, and I
  woke at the roadside," / "under a stone with my own name on it." / "Pray
  for the next traveler, not for me."
- ja: 「大時計にたどり着き、その鐘を聞いた。」 / 「時が折り重なり、気づけば道ばたにいた。」 /
  「自分の名が刻まれた石の下で。」 / 「私ではなく、次の旅人のために祈ってくれ。」

The passage explains why every memorial belongs to the same adventurer.

The journal is never consumed. A player holding it in the main hand or the
off hand when stepping onto the plate makes a blessed prayer: the page pool
is replaced by one roll from a modest offering pool, and the supplies pool
is unchanged. Without the journal in hand, the prayer is the normal one and
drops a page, so players still missing pages keep collecting, and a player
with the journal stops receiving duplicate pages simply by holding it.

Blessed offering pool (one roll, equal weight):

| Item | Count | Why |
|---|---|---|
| `clockstone` | 1 to 2 | Common Chrono Dawn ore, not rare |
| `raw_temporal_amber` | 1 to 2 | Same |
| `time_fruit_pie` | 1 | Food that needs crafting from several ingredients |
| `temporal_root_stew` | 1 | Same |
| `baked_temporal_root` | 2 to 3 | Food that needs a furnace |

`time_bread` is left out because it is crafted from wheat alone, which is
too close to food found everywhere on the surface. Amounts stay small
because a player who waits near a group of memorials can pray at each one
once per cooldown.

The blessed offering is a separate loot table,
`chronodawn:gameplay/lost_adventurer_memorial_blessed`, so modpack authors
can change it independently. The block chooses the table from the praying
player's hands. Loot tables cannot see the held item, so the choice is made
in code.

### Farming prevention

- Breaking `memorial_plate` drops `temporal_stone_pressure_plate`.
- `memorial_plate` has no item.
- A memorial built by a player from the plain pressure plate does nothing.

## Internal structure

Same layout as part 1. Code that touches renamed vanilla API lives in each
version module, everything else in `common/shared`.

### Pure logic (`common/shared`, no Minecraft dependency)

- **`StepOnTracker`** in `com.chronodawn.core.wayside`: remembers the last
  game time each player was seen on the plate. `step(UUID, gameTime)`
  returns true when the player was not seen within the previous
  `STEP_OFF_TICKS` (10) ticks, then records the time. Entries older than
  `STEP_OFF_TICKS` are pruned on every call. Not persisted. After a chunk
  reload a player already standing on the plate counts as a new visit, and
  the cooldown still stops a second reward.

### Block entity (`common/shared`)

- **`MemorialPlateBlockEntity`** extends `CooldownActivatorBlockEntity` and
  owns a transient `StepOnTracker`. It adds no saved data, so it needs no
  era copies.

### Block (all 14 version modules)

- **`MemorialPlateBlock`** extends that version's
  `TemporalStonePressurePlate` and implements `EntityBlock`.
  - `entityInside` calls `super` so the plate presses as usual, then on the
    server, for a `Player`, asks the block entity's `StepOnTracker`. On a
    new visit it calls `tryActivate` with the memorial cooldown.
  - On success it spawns the particles, plays the sound, drops the reward
    and sends the message. On refusal it sends the cooldown message.
  - `rollOffering(ServerLevel, BlockPos)` returns the reward stacks. It is
    public static so the GameTest can roll the table without a player. An
    overload takes the loot table path, so a GameTest can check that a
    missing table yields an empty list. The loot table lookup differs by era
    (`getLootData` on 1.20.1, `reloadableRegistries` later).
  - The `entityInside` signature and the plate's superclass constructor
    follow each version's existing `TemporalStonePressurePlate`.

### Items (all 14 version modules)

- **`JournalPageItem`** takes the page number and the tooltip line count.
  `appendHoverText` adds `item.chronodawn.faded_journal_page_<n>.line<k>`
  for each line, following the per-version tooltip signature used by
  `TimeCompassItem`.
- Registered six times in each `ModItems`, with `ModItemId` entries, added
  to the creative tab after the other exploration items.
- **`LostAdventurerJournalItem`** shows the four closing lines
  (`item.chronodawn.lost_adventurer_journal.line<k>`) the same way, stacks
  to 1, and is registered once in each `ModItems` after the pages.
- `MemorialPlateBlock` picks `gameplay/lost_adventurer_memorial_blessed`
  when either hand holds the journal, otherwise
  `gameplay/lost_adventurer_memorial`.

### Registration

- `ModBlockId.MEMORIAL_PLATE` and `ModBlockEntityId.MEMORIAL_PLATE`.
- Block and block entity in every version's `ModBlocks` and
  `ModBlockEntities`. No `BlockItem`.
- Same render layer as `temporal_stone_pressure_plate`.

### Resources

- Blockstate and models for `memorial_plate` that reuse the
  `temporal_stone_pressure_plate` models (up and down).
- Block loot table dropping `temporal_stone_pressure_plate`, and the
  `minecraft:mineable/pickaxe` tag entry, matching the plain plate.
- Reward loot tables `gameplay/lost_adventurer_memorial.json` (page pool)
  and `gameplay/lost_adventurer_memorial_blessed.json` (blessed offering
  pool). Both pull their supplies from
  `gameplay/lost_adventurer_memorial_supplies.json` through a
  `minecraft:loot_table` entry, so a datapack changes supplies in one place.
  The entry names the table with `"name"` on 1.20.1 and `"value"` from 1.21.1.
- Shapeless recipe `lost_adventurer_journal` (six pages and a `minecraft:book`)
  in every recipe root (`common/1.20.1/.../recipes`, `common/1.21.1/.../recipe`,
  `common/shared-1.21.2+/.../recipe`), each in that era's recipe format, plus
  a recipe-unlock advancement under `recipes/misc` triggered by picking up
  any page.
- Journal item model and texture (an aged copy of vanilla `writable_book`),
  and client item JSON for 1.21.4 and later.
- Advancement `exploration/lost_adventurer_journal.json` with six
  `inventory_changed` criteria, all required.
- Loot tables exist in three places: `common/1.20.1` (`loot_tables`),
  `common/shared-1.21.1+`, and `common/26.3` in the 26.3 schema. The
  advancement exists in `common/1.20.1` (`advancements`) and
  `common/shared-1.21.1+`. 26.3 reads the shared advancement.
- One item texture shared by all six pages, six item models pointing at
  it, and client item JSON for 1.21.4 and later.
- Translation keys in `en_us` and `ja_jp` for the six page names, their
  tooltip lines, the journal name and its four lines, the block name, the two action-bar messages and the
  advancement, in all three lang sets (1.20.1, 1.21.1, `shared-1.21.2+`).
- Both memorial NBTs in `common/shared-1.21.1+`: replace the
  `temporal_stone_pressure_plate` palette entry with
  `chronodawn:memorial_plate`, edited directly with nbtlib. The 1.20.1
  copies are generated by `convertNbt`.

### Config

One key in `[gameplay.wayside]`, following the sundial keys in
`WaysideSettings`, `ConfigDefaults` and `ConfigLoader`:

| Key | Default | Range | Meaning |
|---|---|---|---|
| `memorial_cooldown_ticks` | 24000 | 20 to 1728000 | Per-player cooldown for one memorial |

## Error handling

- Saved cooldowns fail safe the same way as part 1: unreadable data starts
  an empty tracker.
- If the reward loot table is missing or empty (for example removed by a
  datapack), the prayer still shows the light and sound, consumes the
  cooldown and drops nothing. No exception reaches the player.
- `entityInside` does nothing on the client.

## Testing

- **Unit tests** (`common/shared/src/test`), `StepOnTrackerTest`:
  - first step returns true
  - standing (calls every tick) returns false
  - stepping back on after more than `STEP_OFF_TICKS` returns true
  - players are independent
  - stale entries are pruned
- **GameTest** (`common/gametest`, registered in every
  `RegistryDrivenTestGenerator`):
  - placing `memorial_plate` gives a `MemorialPlateBlockEntity`
  - `rollOffering` yields exactly one journal page and one or two supply
    stacks
  - `memorial_plate` drops one `temporal_stone_pressure_plate`
    (`BlockState#getDrops`, as `FadedPlainsTests` does)
  - `StructureTests` in `common/gametest` and its 26.3 copy expect
    `memorial_plate` in both memorial NBTs. `BlockRequirement` only checks
    minimum counts, so the NBT edit script asserts that no plain pressure
    plate is left
  - a missing reward loot table yields an empty offering
  - stepping onto the plate drops one offering, and standing on it or
    stepping back on within the cooldown drops nothing more
  - the advancement loads, through an `AdvancementSpec` entry in each
    version's `RegistryDrivenTestData`
  - the blessed table yields no page, exactly one offering from its pool and
    one or two supply stacks
  - a player holding the journal in either hand rolls the blessed table, a
    player without it rolls the normal table
  - the journal recipe loads on every version (the crafted result is checked
    by hand)
- **Validation**: `validateResources validateTranslations validateData
  validateEraTagParity`, then `buildAll`, `testAll` and `gameTestAll` run
  separately.
- **Manual check** on 1.21.1 NeoForge and Fabric, 1.20.1 Forge, 26.3
  NeoForge and 26.2 Fabric, at a memorial in newly generated chunks: the
  pillar looks like falling light, the sound is clearly audible, items land
  on the player, the cooldown message appears, and standing still does not
  retrigger. GameTest servers may omit Chrono Dawn dimensions, so this uses
  a normal client.

## Known trade-offs

- The blessed offering repeats for every memorial a player reaches, so its
  pool holds only common ores and processed food. Rarer rewards are left to
  parts 3 to 6 and the endgame work.

- The plate has no collision box, like any pressure plate, so brushing its
  edge triggers it. Accepted.
- Duplicate pages are possible. Finding all six takes more prayers than a
  no-duplicate scheme would. Accepted to avoid a per-player record.
- A player riding a mount does not trigger the plate, because only the mount
  touches it. Praying means stepping down at the grave. Accepted.

## Documentation

- `docs/player_guide.md`: describe the memorial, the pages and the
  advancement. The guide, README and store pages call this structure
  "Petrified Adventurer", which does not match its NBT. Rename it to "Lost
  Adventurer Memorial" there.
- Roadmap status table: mark part 2 when it ships.
