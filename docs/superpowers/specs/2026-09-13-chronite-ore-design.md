# Design: Chronite Ore and the Overworld Path to Ancient Ruins

**Created**: 2026-09-13
**Status**: Draft
**Related**: [Structure toggles](./2026-08-31-biome-structure-tags-design.md)

---

## Why

Ancient Ruins is a hard gate on the entire mod, and nothing in the Overworld
points a player toward it.

Two facts establish the gate:

- `ore_clockstone` targets `chronodawn:temporal_stone` and
  `chronodawn:deepslate_temporal_stone`, and is referenced only from
  `chronodawn_*` biomes. The sole Overworld source of Clockstone is the walls
  and chests of Ancient Ruins.
- The Time Hourglass recipe is unlocked by a `minecraft:knowledge_book` that
  drops only from `chests/ancient_ruins.json`.

A player who does not find the ruins never touches the mod. This is not "a
long search"; it is a dead end.

Finding them is hard for a structural reason. `random_spread` picks one
candidate position per `spacing`x`spacing` cell and generates nothing in that
cell if the biome at that single position does not match. It does not retry.
So the biome restriction multiplies directly into density. Ancient Ruins sits
at `spacing` 56 / `separation` 20 restricted to `#minecraft:is_taiga` plus
`minecraft:dark_forest`, which puts it between a desert pyramid (32/24) and a
woodland mansion (80/20, dark forest only), much closer to the mansion.

Vanilla's answer for something in the mansion's rarity band is not higher
density. It is a locator, the cartographer's explorer map. Density governs how
often you stumble across a structure while playing; a locator governs how you
reach the *first* one. They are independent levers, and only the second one can
help a world whose surroundings are already explored, because structure
placement is deterministic per seed and already-generated chunks never
recalculate.

`TimeCompassItem` already implements the locator. It defines
`STRUCTURE_ANCIENT_RUINS`, branches on
`ManagedStructure.Dimension.OVERWORLD` to pick the search level, calls
`findNearestMapStructure` with a 100-chunk radius, and ships `en_us`/`ja_jp`
strings for the Ancient Ruins target. The only missing piece is a way to obtain
it before entering the dimension. Today it comes from Time Keeper trades, and
Time Keepers live in Chrono Dawn.

## Scope

- One new Overworld ore (blocks, item, worldgen, loot, tags, config) across all
  thirteen version modules: 1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6,
  1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2.
- A Time Compass fallback target, plus its crafting recipe.
- Four recipes and their unlock advancements.
- Documentation corrections.

Explicitly **not** in scope:

- Ancient Ruins `spacing`, `separation`, or biome conditions. Changing them
  cannot help already-explored worlds, and loosening them risks re-triggering
  the "too many structures" feedback that produced the current values.
- The existing `time_arrow`, `time_hourglass`, and Clockstone recipes.
- A new arrow entity (see "Rejected alternatives").

## Design

### 1. Chronite Ore

The narrative: the Chrono Dawn's temporal field bleeds into Overworld bedrock
and crystallises. This is why a compass built from it senses the place where
the two worlds touched, the Ancient Ruins. The recipe and the flavour agree,
so the locator needs no separate explanation.

**Blocks**: `chronite_ore`, `deepslate_chronite_ore`. New textures on stone and
deepslate backgrounds; the existing Clockstone ore art sits on the light-blue
`temporal_stone` background and cannot be reused.

**Drop**: `chronite_shard`.

The shard form is a balance decision, not just flavour. A shard or dust reads
at a glance as a low-tier reagent, the way redstone and glowstone dust do, so
no player expects armour or tools from it. Enforcing "this does not lower the
difficulty" through the shape of the item costs nothing to explain.

**No armour, tools, or weapons are craftable from Chronite.** This is why
neither Clockstone nor Temporal Amber could be reused for this purpose:
Clockstone is the portal material, and Temporal Amber already has a full armour
set under `recipe/temporal_amber_*.json`. Generating either in the Overworld
would let a player skip the dimension for gear.

**Mining**: stone pickaxe. The whole point is that an early player can reliably
get it; requiring iron would defeat the purpose.

**Worldgen**: no biome restriction, injected into the Overworld biome tag.
Roughly `count` 6, Y -48 to 112, cluster size in line with the existing ore
features. Final numbers get tuned during implementation. Unlike a structure,
a moderately generous ore does not read as clutter.

Injection is per-loader because the repo has no Overworld biome injection yet.
Every ChronoDawn ore feature is written directly into a `chronodawn_*` biome
JSON.

- NeoForge: a `data/neoforge/biome_modifier/*.json`. No Java.
- Fabric: `BiomeModifications.addFeature(...)` in `fabric/base/`, which is
  shared across all versions, so one file rather than thirteen. If the API
  signature turns out to differ on 26.x, the guard goes in that one file.

**Config**: `[world.ores.chronite]` following the existing `[world.ores.*]`
convention documented in `docs/configuration.md:208`, with `enabled`, `count`,
`y_min`, `y_max`.

### 2. Uses of Chronite

Chronite must not become dead weight once the compass is built, so it needs a
sink that never saturates.

| Stage | Use | Demand |
| --- | --- | --- |
| Early | Time Compass | once |
| Early on | Vanilla clock, alternate recipe | a few |
| Mid to late | Time Arrow, alternate recipe | unbounded |
| Any | `chronite_block` for storage and decoration | elastic |

The unbounded sink is arrows. Arrows are lost when fired, so demand never ends.

**`recipe/time_arrow_from_chronite.json`** (shapeless): 1 Chronite Shard + 1
arrow gives 1 Time Arrow. The existing Clockstone recipe is untouched.

This does not kill the Clockstone route. Per arrow produced:

| Route | Special material per arrow | Requires |
| --- | --- | --- |
| Clockstone | 0.25 Clockstone + 0.25 Fruit of Time | two dimension resources |
| Chronite | 1 Chronite Shard | Overworld only |

The Clockstone route stays four times more efficient in special material, so
anyone with a Fruit of Time farm keeps using it. The Chronite route is always
available and always expensive, which is the coal-like property we want
from a sink.

It also relieves an existing tension: `docs/player_guide.md:1264` lists
Clockstone as non-renewable, yet the only Time Arrow recipe spends it on a
consumable. Players no longer have to choose between arrows and gear.

The Chronite recipe drops `fruit_of_time`, which means a player can craft Time
Arrows before entering the dimension. That is acceptable. `TimeArrowEntity`
applies Slowness II for 3 seconds to ordinary mobs (Slowness III, Weakness II
and Glowing to the Time Tyrant specifically), which is roughly a vanilla tipped
arrow of Slowness, already Overworld-craftable with a brewing stand. The
part that matters is anti-Tyrant, and the Tyrant is inside the dimension.

**Vanilla clock, alternate recipe**: Chronite in place of gold. A time mod
making clocks cheap is thematically natural, useful early, and cannot affect
difficulty. The recipe lives under the `chronodawn:` namespace, so it adds a
way to craft `minecraft:clock` without displacing the vanilla recipe.

### 3. Time Compass in the Overworld

**Fallback**: when a Time Compass has no target set and is used in the
Overworld, set the target to `STRUCTURE_ANCIENT_RUINS`. Dimension structures
stay trade-only, so Time Keeper trades are unaffected.

The fallback is what lets the crafting recipe stay plain. Baking a preset
target into a recipe result would mean writing NBT for 1.20.1 and components
for 1.21+; letting the item resolve its own default on first use avoids that
split entirely.

`TimeCompassItem` is duplicated per version, so this is a thirteen-module edit.
Use the established MD5-hash-and-grep verification for repeated identical
edits rather than reading each file linearly.

**Recipe**: vanilla compass plus Chronite Shard plus clock, in a shaped pattern
distinctive enough that collision with another mod is not a practical concern.
Exact pattern settled during implementation.

Recipe collision deserves a note, because it is a different risk class from
overriding a vanilla loot table. A loot table override replaces a file at a
vanilla path and the loser disappears silently. A recipe lives at a
`chronodawn:` ID; the only failure mode is ambiguity when two shaped patterns
match exactly, which a distinctive pattern avoids.

### 4. Documentation

- `docs/player_guide.md` Step 1 (lines 115-120) says "various biomes" twice.
  That predates the taiga / dark forest restriction and actively misleads
  players into searching plains and deserts. Correct it, and document the
  Chronite to Time Compass path.
- `docs/configuration.md:70` describes Ancient Ruins as "Overworld flavour;
  gates nothing", and line 103 implies disabling it is safe. Both contradict
  the Clockstone and blueprint findings above. Correct them, and add
  `[world.ores.chronite]`.
- `docs/curseforge_description.md` and `docs/modrinth_description.md`: add a
  spoiler-gated block under Step 1 covering the biome hint, the Time Compass,
  and `/locate`. Verify that Modrinth renders `<details>`; if it does not, fall
  back to a clearly marked spoiler heading and a horizontal rule.
- Chronicle: add a Time Compass page to `basics/ancient_ruins.json` and a
  Chronite entry under `items/`. The Chronicle is still granted on first
  dimension entry, so these pages are for consistency, not for solving the
  gate. The gate is solved by the ore and the compass being Overworld-side.

## Rejected alternatives

**Loosening Ancient Ruins placement.** Cannot help an already-explored world,
since placement is deterministic per seed and generated chunks are never
recalculated. Changing `spacing` or `salt` also shifts the coordinate lattice,
leaving existing worlds inconsistent. And both the current biome restriction
and the current spacing exist because of "too many structures" feedback.

**An explorer map from vanilla chest loot.** Data-only and cheap, but
probabilistic: the player must first find a taiga village and then win the
roll. That relocates the chicken-and-egg problem rather than solving it, and it
requires overriding vanilla loot tables, which other mods silently clobber.

**A Time Compass recipe from vanilla materials only.** Cheapest of all and
collision risk is low in practice, but the mod's key item should be made from
the mod's own material, and a vanilla-only recipe gives no deterministic
mod-side source to anchor on.

**Reusing Temporal Amber or Clockstone as the Overworld ore.** Both have gear
recipes; generating either in the Overworld lowers the difficulty of the whole
game.

**A sundial block that yields material over time.** The strongest thematic fit
for a time mod and fully deterministic, but it needs block-entity tick logic in
thirteen modules, and "place it and wait" carries a real risk that players
never discover the mechanic. Explanation cost was the deciding factor against
it.

**A new arrow item for the Chronite recipe.** Would need an item, an entity, a
renderer (plus a separate render state on 1.21.5+ and 26.x), entity and item
registration, creative tab entries, and test data, all in thirteen modules, with
entity registration historically easy to miss in individual NeoForge modules.
That is larger than the ore itself, spent on a secondary concern, and the same
balance outcome is available from one extra recipe file.

**A Time Keeper trade that buys Chronite.** A fine additional sink and
thematically sound, but `TimeKeeperEntity` is duplicated per version. Deferred
until the arrow sink proves insufficient.

## Verification

- `./gradlew validateResources`, `validateData`, `validateTranslations` for the
  new blocks, item, tags and recipes.
- Unit tests: `CreativeTabCompletenessTest` and `ResourceValidationTest` need
  the new IDs.
- In-game, per loader on at least one version per API era: Chronite generates
  in Overworld stone and deepslate, drops shards with a stone pickaxe, silk
  touch yields the ore block, all four recipes work, and a freshly crafted Time
  Compass used in the Overworld points at Ancient Ruins.
- `./gradlew checkAll` before the branch is considered done. A single-version
  build is not sufficient.
