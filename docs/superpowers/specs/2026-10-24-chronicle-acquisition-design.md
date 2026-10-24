# Design: Chronicle Acquisition Without Repeated Grants

**Created**: 2026-10-24
**Status**: Draft

---

## Why

The Chronicle of Chrono Dawn (`chronodawn:chronicle_book`) is handed out by
`PlayerEventHandler.giveChronicleBook` every time a player enters the Chrono
Dawn dimension without a copy in their own inventory. Only the player
inventory is checked, so a player who stores the book in a chest or Ender
Chest receives another copy on every entry. The book either occupies a slot
permanently or piles up in storage. 0.14.0 made it stack to 64 to soften
this, but the repeated grant is still a nuisance.

Granting it only once would fix the nuisance but make a lost book
unrecoverable, which is not acceptable. Blue Skies solves a similar problem
by letting the player learn a book, but that needs custom player state,
networking and key bindings across all 14 versions. This design avoids that
and stays with vanilla mechanisms.

## Goals

- A player receives the Chronicle once, on first entry to Chrono Dawn, and
  never again automatically.
- A player who loses the Chronicle can always craft a new one from materials
  available inside Chrono Dawn, even before the Portal Stabilizer makes the
  portal two-way.
- The Chronicle gets its own item texture.

## Non-goals

- Opening the Chronicle without the item (key binding, command, learned
  state). This can be added later on top of this design if carrying the
  book is still a complaint.
- Lectern support.

## Anti-goals

- New custom systems (player data attachments, sync packets, new items or
  blocks) when a vanilla mechanism does the job.
- A paper source so abundant inside Chrono Dawn that books become much
  cheaper than in the Overworld. This ruled out Temporal Kelp and wood.

## Decided

### First grant via advancement reward

- Add a `rewards.loot` entry to the existing `story/us1/dimension_entry`
  advancement (trigger `minecraft:changed_dimension` to
  `chronodawn:chronodawn`). Vanilla grants advancement rewards once per
  player, and drops the item at the player's feet if the inventory is full.
- The reward points at a new loot table that yields one
  `chronodawn:chronicle_book`.
- Remove `giveChronicleBook` and `hasChronicleBook` from
  `PlayerEventHandler` along with the call in the dimension-entry handler.
  The rest of that handler (Time Keeper Village placement, global state,
  portal deactivation) is unchanged. The handler exists in
  `common/shared` and as per-version copies (1.21.6 to 1.21.11, 26.1.2,
  26.2, 26.3). Apply the removal to every copy.

Modpacks that strip or override mod advancements lose the automatic first
grant. This is accepted: crafting works without the recipe being unlocked
(unless `doLimitedCrafting` is on), and pack authors commonly hand out
guidebooks through quest rewards. Mention the change in the CHANGELOG so
pack authors can react.

Files by era:

| Data | 1.20.1 | 1.21.1 to 26.3 |
|------|--------|----------------|
| Advancement | `common/1.20.1/.../data/chronodawn/advancements/story/us1/dimension_entry.json` | `common/shared-1.21.1+/.../data/chronodawn/advancement/story/us1/dimension_entry.json` |
| Loot table | `common/1.20.1/.../data/chronodawn/loot_tables/...` | `common/shared-1.21.1+/.../data/chronodawn/loot_table/...` |

26.3 overrides some advancements under `common/26.3/`, but not
`dimension_entry`, so the shared file applies. Confirm the 26.3 loot table
schema accepts the shared file (see the 26.3 data override notes).

Existing worlds: a player who already earned `dimension_entry` before the
update gets no reward. They already received at least one book under the old
behavior, and the recipe below covers a lost one.

### Paper from Time Wheat

- New shaped recipe: three `chronodawn:time_wheat` in a **vertical** column
  gives three `minecraft:paper`, mirroring vanilla sugar cane's 1:1 ratio.
- The horizontal row of three is already Time Bread and 3x3 is the Time Wheat
  Bale. Vanilla also distinguishes recipes by orientation (two planks
  vertical are sticks, horizontal is a pressure plate), so this is a
  familiar pattern.
- Time Wheat was chosen over Temporal Kelp because it is a farmed crop with
  a pace closer to sugar cane. A water bucket recipe was rejected because it
  reads as soaking bread.
- This also makes vanilla books, bookshelves, enchanting tables and the Lost
  Adventurer Journal craftable without leaving Chrono Dawn. Leather already
  drops from Chrono Bovine.

### Chronicle recipe

- New shapeless recipe: `minecraft:book` + `chronodawn:time_crystal` gives
  one `chronodawn:chronicle_book`.
- Time Crystal ore is in Chrono Dawn at Y 0 to 48 with an iron pickaxe, and
  Timeline Striders and Forgotten Minutes drop it, so it is reachable early.

### Recipe unlock advancements

Both recipes get recipe-book unlock advancements, following the existing
`advancement/recipes/misc/lost_adventurer_journal.json` pattern
(`has_the_recipe` or `inventory_changed`), so the recipes show up in the
recipe book:

- Paper: unlocked by obtaining Time Wheat.
- Chronicle: unlocked by obtaining Time Crystal.

Recipe files by era: `common/1.20.1/.../recipes/`,
`common/1.21.1/.../recipe/`, `common/shared-1.21.2+/.../recipe/` (the same
split as `lost_adventurer_journal`). Advancement files: `common/1.20.1`,
`common/shared-1.21.1+`, and `common/26.3` if 26.3 needs its own copy as it
does for `lost_adventurer_journal`.

### Texture

- Point `common/shared/.../models/item/chronicle_book.json` at
  `chronodawn:item/chronicle_book` instead of `minecraft:item/book`. The
  1.21.4+ `items/chronicle_book.json` files only reference the model and need
  no change.
- Add `assets/chronodawn/textures/item/chronicle_book.png` as a
  **placeholder copied from the vanilla `item/book` texture**. The user edits
  it into an original texture. A raw vanilla copy must not ship in a
  release. The placeholder (copied from the 26.3 client jar) is already in
  the working tree of this branch, and the user edits it while the rest is
  implemented. The branch is not merged to main until the user has replaced
  it.

### Documentation

- `docs/player_guide.md`: the FAQ entry that says the book is received
  "automatically when entering", plus the recipes for paper and the
  Chronicle.
- `CHANGELOG.md` Unreleased.
- In-game Chronicle entries that describe how the book is obtained, if any.

## Left to the builder

- Loot table and recipe IDs and paths (e.g. `paper_from_time_wheat`).
- Whether the paper recipe uses a recipe `group`.
- Keeping the Chronicle stack size at 64. My call: keep it, since existing
  saves may already hold stacks. Update the stale `stacksTo` comment ("since
  a copy is granted on each entry without one") in every
  `ChronicleBookItem` copy.
- The `ModItems` Javadoc for `CHRONICLE_BOOK` says it is distributed via a
  `grant_chronicle_book` advancement, which no longer exists. Update it. The
  matching `advancements.chronodawn.grant_chronicle_book.*` lang keys are
  unused leftovers. My call: leave them alone in this change.
- Automated test coverage, for example a resource test that checks the
  advancement reward and recipes exist in every era.

## Verification

1. `./gradlew validateResources validateData` pass.
2. New world on 1.21.1 NeoForge and Fabric, and 26.3: entering Chrono Dawn
   gives exactly one Chronicle. Put it in a chest, leave and re-enter (or
   die and respawn into the dimension): no new copy.
3. Crafting chain inside Chrono Dawn: Time Wheat (vertical) → paper, paper +
   leather → book, book + Time Crystal → Chronicle. Recipe-book unlock
   toasts appear on picking up Time Wheat and Time Crystal.
4. 1.20.1 Forge: same as 2 and 3.
5. Chronicle shows the new texture on all versions.
