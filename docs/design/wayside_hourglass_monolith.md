# Design: Wayside Features, Part 6 (Hourglass Monolith)

**Created**: 2026-11-15
**Status**: Draft (design only, no implementation yet)
**Related**: [Roadmap](../archive/superpowers/2026-10-14-wayside-features-roadmap.md),
[Part 5 design](../archive/superpowers/2026-10-27-wayside-time-cairn-design.md),
[Part 3 design](../archive/superpowers/2026-10-18-wayside-time-well-design.md) (stranded-item lessons),
[Endgame reward shortage](endgame_reward_shortage.md)

## Why

Parts 1 to 5 gave the sundial, memorial, well, camp and cairn a reason to
stop. The Hourglass Monolith is the last of the six small surface features
and the only event-type one: activating it starts a short wave fight, and
clearing it pays out. The roadmap decided this role. This document fills in
the open points: the activator, wave composition, the state machine and its
interruptions, multiplayer, cooldown, rewards and the link to the endgame
reward shortage.

## Decisions (agreed with the maintainer)

| Topic | Decision |
|---|---|
| Enemies | Existing Chrono Dawn mobs only. No new entity, model or renderer |
| Scaling | Fixed enemy counts, independent of player count |
| Starter leaves, dies or logs out | The fight does not consume the cooldown. The cooldown is consumed only when the fight is cleared |
| Player leaves and comes back | The fight resumes from the wave where it stopped |
| Other players nearby | Count as participants and receive the clear reward |
| Cooldown default | 3 Minecraft days (72000 ticks) |
| Endgame rewards | The reward table may include Fragment of Stasis Core |
| In-game explanation | A Chronicle page and an updated intro page, decided here, not left to the wiki |

Everything below marked "my call" is a proposal for the maintainer to tune,
as in earlier parts.

## Goals

- Activating the monolith starts a short, readable fight (3 waves) that a
  well-equipped mid-game player can clear alone and a small group clears
  comfortably.
- A fight is never lost to a player leaving, dying or logging out. It can be
  continued, and it never leaves enemies or items stranded in the world.
- Clearing pays a modest, configurable reward and starts the per-player
  cooldown.
- A rare Fragment of Stasis Core drop works as the "another fragment source"
  option from the endgame reward shortage document, without changing boss
  rules.
- Newly generated monoliths only. Already-generated ones are not converted
  (roadmap rule).

## Non-goals and anti-goals

- No new mob, boss, structure or placement change. Rarity stays 1 in 16
  chunks on `chronodawn:temporal_sand`.
- No scaling by player count, no loot that scales with the number of kills.
- No Time Tyrant revival, and no change to Master Clock protection. Those stay
  in the endgame reward shortage task.
- Anti-goal: a farmable fragment machine. The fragment chance is small, the
  cooldown is per player and per monolith, and both are configurable.
- Anti-goal: the arena destroying the monolith or the player's belongings.
  No exploding mobs (Moment Creeper is excluded), and no block changes other
  than the activator's own state.

## Current structure

`hourglass_monolith.nbt` is 3x5x3 and uses `random_rotate: true`,
`y_offset: 0` and `min_ground_contact_ratio: 1.0` (decoded layout, x/y/z):

| y | Content |
|---|---|
| 0 | `temporal_sandstone_wall` ring around a `sand` center (1,0,1) |
| 1 | full 3x3 `temporal_sandstone` |
| 2 | `chiseled_sandstone` at (1,2,1), air around it (the hourglass waist) |
| 3 | full 3x3 `temporal_sandstone` |
| 4 | `temporal_sandstone_wall` ring, air at the center |

The NBT sits at `common/shared-1.21.1+/.../structure/hourglass_monolith.nbt`.
Confirm where the 1.20.1 copy lives (it is not under shared-1.21.1+) and
whether 26.3 needs its own, as it already has its own feature JSON.

## Activator

- The `chiseled_sandstone` at (1,2,1) becomes `chronodawn:hourglass_monolith_core`
  (my call on the name). It reuses the chiseled sandstone model and texture,
  so it looks the same until effects play. It has no item and drops plain
  `chiseled_sandstone` when broken (roadmap activator rule). Breaking it
  mid-fight ends the fight in the "paused" state below, and the enemies are
  removed.
- The waist is one block tall, so the core is reached by right-clicking
  through the gap on any side. Verify reach and the interaction hitbox with a
  real client before the implementation plan is final. If it is awkward, move
  the core into the open ring of layer 4 instead of changing the shape.
- A new `HourglassMonolithBlockEntity` extends `CooldownActivatorBlockEntity`
  (era copies as for the other activators) and holds the fight state.
  Cooldown bookkeeping reuses `PlayerCooldownTracker`.

## Fight rules

### Starting

Right-clicking the core starts the fight if all of these hold:

- No fight is active at this monolith (one fight per monolith at a time).
- The difficulty is not Peaceful. Otherwise an action-bar message says the
  monolith does not answer in peaceful (monsters would not spawn).
- The player is not on cooldown for this monolith. Otherwise the usual
  "try again in about N min" message.

### Arena and participants

- The arena is a cylinder of radius 24 around the core (my call). Enemies
  spawn on the surface in a ring 8 to 14 blocks out, at positions that pass a
  simple solid-ground, two-block-air check, using spawn-egg style `finalizeSpawn`
  so the mobs behave as normal.
- Participants are the starter plus every player seen within radius 32 while
  the fight is active (my call, a little wider than the arena so edge
  fighting counts). The set is stored by UUID in the block entity.
- Players who are on cooldown for this monolith may stand and fight, but
  receive no reward and are told so at the end. The cooldown stays per player
  and per monolith, as in every other part.

### Waves (my call, all existing mobs)

Hourglass Golem (80 HP, 12 damage) is far stronger than every other
candidate, so it appears once, in the last wave.

| Wave | Enemies |
|---|---|
| 1 | 3 Epoch Husk, 2 Forgotten Minute |
| 2 | 2 Clockwork Sentinel, 2 Secondhand Archer, 2 Temporal Wraith |
| 3 | 1 Hourglass Golem, 2 Temporal Wraith |

Counts are fixed in code and do not scale. The next wave starts 3 seconds
after the last enemy of the wave dies, with an action-bar "Wave N of 3"
message, a particle ring over the core and a sound. Mob stats follow current
values, so re-check the table against the stat files when implementing.
The wave table is kept as one data class so the maintainer can tune it
without touching the state machine.

Every spawned mob is marked persistent (no natural despawn) and carries a
persisted tag naming this monolith (`chronodawn_monolith` plus the core's
packed position), so cleanup never depends on remembered UUIDs.

### State machine

States, stored in the block entity and saved to disk:

- `IDLE`: no fight. Cooldown only decides whether a start is allowed.
- `ACTIVE`: a wave is running. The block entity stores the wave number, the
  participant set and the count of enemies still alive.
- `PAUSED`: the fight stopped without being cleared. It stores the wave
  number reached. No enemies exist.

Transitions:

- `IDLE` to `ACTIVE`: a valid right-click, wave 1.
- `ACTIVE` to next wave or to `IDLE`: when every tagged enemy in the arena is
  dead. After wave 3 the fight is cleared, rewards are paid and the cooldown is
  recorded for each rewarded participant. State becomes `IDLE`.
- `ACTIVE` to `PAUSED`: no participant is within the arena radius for 30
  seconds (600 ticks, my call), the core is broken, or the chunk is loaded
  with the state still `ACTIVE`. On the way into `PAUSED`, every tagged enemy
  in the arena is discarded.
- `PAUSED` to `ACTIVE`: any player who is not on cooldown right-clicks the
  core. The fight restarts at the stored wave with a full count of that
  wave, not the leftover survivors. This is the "resume" rule: finished waves
  are kept, and the wave in progress restarts.

Interruption cases:

| Case | Result |
|---|---|
| Starter walks away | Other participants keep fighting. If nobody stays near, `PAUSED` after 30 s |
| Starter dies | The fight continues for anyone still there. The starter may come back and continue, no cooldown spent |
| Starter logs out | Same as walking away. Participants are stored by UUID, so a return after relogin works |
| All participants die | They respawn elsewhere, so 30 s later `PAUSED`, enemies discarded |
| Chunk unloads mid-fight | Tagged mobs are saved with the chunk. On load, an `ACTIVE` state is switched to `PAUSED` and tagged mobs in the arena are discarded, so nothing hangs around unattended |
| Server stops mid-fight | Same as chunk unload |
| Dimension is not Chrono Dawn | The monolith only exists in Chrono Dawn, so this is not handled |

The cooldown is recorded only in the clear transition. No cooldown is ever
spent on start, pause or abort, which is the maintainer's decision.

### Why wave progress is kept but the current wave restarts

Keeping exact survivor counts would need entity UUIDs to survive chunk
unloads, and a half-finished wave left alone in the desert is exactly the
kind of stranded state the part 3 review kept finding. Restarting the
current wave costs the player little and removes that whole class of bugs.

## Rewards

### Items

Each rewarded participant gets one roll of a loot table
(`chronodawn:wayside/hourglass_monolith`, my call on the name). Items are
spawned as item entities at the core, spread out and tossed slightly, never
put straight into inventories, because a full creative inventory silently
deletes them (part 3 and the creative-inventory lesson). They carry a pickup
owner for 60 seconds so a stranger does not take them, then become free.

Proposed contents per participant (my call, modest):

| Item | Amount | Chance |
|---|---|---|
| Clockstone | 2 to 4 | guaranteed |
| Time Crystal | 1 to 3 | guaranteed |
| Chronite Shard | 1 | 25% |
| Fragment of Stasis Core | 1 | 5%, configurable |

Gated materials (Ancient Gear, Clockwork Mechanism, boss-only items) stay out
of the table.

### Link to the endgame reward shortage

[Endgame reward shortage](endgame_reward_shortage.md) lists "another fragment
source" as an open option, aimed at players who missed the single Time Tyrant
kill, such as multiplayer players. The monolith's fragment chance fills that
slot partially:

- It is per participant, so everyone present in multiplayer rolls
  independently, which the Tyrant kill does not offer.
- At 5% per clear and one clear per player per monolith per 3 days, it adds a
  slow trickle, not a bypass. A single fragment covers one alternative recipe
  from 0.13.0.
- It does not touch Time Tyrant rules, so revival and Master Clock room
  unprotection remain separate decisions in that document.

After this feature ships, update the "Open Options" entry in that document to
say that one fragment source exists, and keep the remaining options open.

## Configuration

Added to `[gameplay.wayside]` in `WaysideSettings` and
`chronodawn-default-config.toml` (key names my call):

```toml
hourglass_monolith_cooldown_ticks = 72000
hourglass_monolith_fragment_chance = 0.05
```

| Field | Type | Default | Range | Notes |
| --- | --- | --- | --- | --- |
| `hourglass_monolith_cooldown_ticks` | integer | `72000` | `20..=1728000` | Ticks before the same player can be rewarded by the same monolith again. Counted in game time. |
| `hourglass_monolith_fragment_chance` | float | `0.05` | `0.0..=1.0` | Chance per participant per clear to receive a Fragment of Stasis Core. `0` disables it. |

The other reward amounts live in the loot table, so a datapack can change
them. The fragment chance is a config value, so it is applied in code as a
separate roll, not inside the table. Document both in `docs/configuration.md`
next to the other wayside keys.

## Feedback and effects

- Start: a low sand-colored particle ring around the core and a sound.
- Wave start and clear: `END_ROD` and dust particles over the core, with
  action-bar messages ("Wave 2 of 3", "The monolith falls silent.").
- Sound is chosen by comparing candidates with `/playsound` in a real client
  (roadmap lesson). Avoid bell sounds, which Time Distortion already uses.
- No new texture. If the core later gets a glowing variant, draw it as a
  placeholder and let the maintainer hand-paint it (roadmap lesson).

## In-game explanation (Chronicle)

Chronicle's "Wayside Features" entry
(`.../chronicle/entries/structures/wayside_features.json`) gets:

1. The intro page's list of features gains the monolith in en_us and ja_jp.
2. A new Hourglass Monolith page covering:
   - Where to find it: a tall sandstone hourglass in the desert.
   - How to start it: use the chiseled block at its waist.
   - What happens: three waves of Chrono Dawn creatures, the last with a
     Hourglass Golem, at and around the monolith.
   - Rewards and rules: everyone who fought nearby is rewarded, with a rare
     Fragment of Stasis Core. Each player can be rewarded again after about
     three days.
   - Leaving is safe: a fight that is left can be continued later, from the
     wave reached.

Exact en_us and ja_jp copy is written during implementation. Reuse the
tone of the existing five pages and grep each key across versions before
writing new text (lang wording drift lesson).

## Internal structure

Pure logic (`common/shared`, no Minecraft dependency, unit tested):

- `MonolithWaves`: the wave table and "next wave" decisions.
- `MonolithFightState`: the state machine and its transitions, including
  the 30-second absence timer and the pause and resume rules. Takes ticks and
  booleans, never world objects, so every row of the interruption table is a
  JUnit test.
- `MonolithRewards`: fragment roll and who is rewarded, given participants and
  cooldown results.

Block entity and block (all 14 version modules, with era copies where the NBT
API differs, like `CooldownActivatorBlockEntity`):

- `HourglassMonolithBlock` and `HourglassMonolithBlockEntity`, with a
  server-side ticker that runs only while `ACTIVE` or `PAUSED` is not idle.
  The ticker checks players, counts tagged enemies in the arena and drives
  the wave transitions.
- Registration in every module, one loot table per era format (1.20.1 uses
  `"name"` in loot table references, 1.21.1+ uses `"value"`, and 26.3 has its
  own copies of loot tables and advancements).

Resources:

- `hourglass_monolith.nbt` swap of the center block in each copy, plus 26.3's
  own `StructureTests` copy.
- Block model, blockstate and lang keys (en_us and ja_jp) for the core and the
  messages.
- Chronicle page and intro updates.

## Testing

- JUnit for `MonolithFightState`, `MonolithWaves` and `MonolithRewards`: every
  transition, the absence timer, pause then resume at the stored wave,
  cooldown recorded only on clear, non-participants and on-cooldown
  participants receiving nothing.
- GameTest: start the fight with `makeMockServerPlayer` (removed in a
  `finally`), confirm wave 1 spawns tagged mobs, kill them, confirm wave 2, and
  clear to confirm rewards and cooldown. Keep distances inside the test
  template. Another test breaks the core mid-fight and expects no tagged
  mobs and `PAUSED`. GameTest mock players are always creative, so any
  break-protection check goes through an extracted guard, not the player.
- Structure tests: the NBT test expects the core block in place of the
  chiseled sandstone. Update `StructureSpecs` and 26.3's own copy together.
- Real client check on 1.21.1 (both loaders), then the usual order:
  `validateResources validateTranslations validateData validateEraTagParity`,
  `buildAll`, then `gameTestAll`, run separately. Run `prodSmokeTest` on the
  new block registration.
- Manual checks to write out for the maintainer: leave mid-wave and come back,
  die and come back, relog, and break the core while a fight runs. State what
  a broken core looks like (plain chiseled sandstone, no more fight), so it is
  not mistaken for a working monolith.

## Known trade-offs

- Restarting the current wave on resume is slightly harsh after a long wave 3
  fight, in exchange for no stranded mobs. The maintainer can switch to
  persisting survivors later if play-testing finds it too punishing.
- Fixed enemy counts make the fight easy for large groups. This was chosen on
  purpose to keep the rule simple and the reward modest.
- Rewarding every participant means a group clears faster per player. Per-player
  cooldown and the small fragment chance are the brakes, both configurable.
- The 1.21.1 modpack audience (see Support Priority) can tune or disable the
  fragment chance, and the cooldown, through config.

## Documentation

When implemented: update `docs/configuration.md` (keys above), the player
guide's wayside section, the CurseForge and Modrinth descriptions if they list
wayside features, the roadmap status table, the "Open Options" entry in the
endgame reward shortage document, and `CHANGELOG.md` under Unreleased.
