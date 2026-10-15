# Design: Ancient Gear as a Fragment-Saving Material

**Created**: 2026-10-14
**Status**: Draft

---

## Why

Ancient Gears are easy to get. Clockwork Sentinels always drop one,
Secondhand Archers drop one 5% of the time, two vault chests contain them,
and they can be crafted. Their only use is the Master Clock boss room door,
which needs 3 in the inventory and does not consume them. Every gear after
the third is useless.

At the same time, the endgame is short on Fragments of Stasis Core. The four
ultimate artifacts need 15 in total (Chronoblade 4, Time Tyrant Mail 6,
Echoing Time Boots 2, Unstable Pocket Watch 3). The only source is the Time
Tyrant, which spawns once per world and drops 3 to 5, plus 1 to 2 per Looting
level. Without Looting a world can make at most one or two artifacts.

This design turns spare gears into a way to need fewer Fragments. Gears never
become Fragments. The Tyrant kill stays mandatory for every artifact.

## Goal

After one Tyrant kill without Looting, a player can craft all four artifacts
by paying the rest in gears and other repeatable materials.

## Scope

In scope:

- New item Clockwork Mechanism
- One alternative recipe per artifact that uses 1 Fragment plus Mechanisms
- Time Tyrant Fragment drop raised from 3-5 to 4-6

Out of scope, each a candidate for a later spec:

- Reviving or respawning the Time Tyrant under some condition. This is the
  long-term fix for servers and multiplayer, where one kill per world cannot
  cover several players. The alternative recipes here stay useful after that
  change, as a way to save Fragments.
- Repairing or upgrading artifacts with gears
- A config option for any of the above

## Design

### Clockwork Mechanism

A plain crafting material with no behavior.

```
CGC
GEG
CGC
```

- `G`: `chronodawn:ancient_gear`
- `E`: `#c:ingots/enhanced_clockstone`
- `C`: `#c:gems/time_crystal`

Yields 1. Recipe category `misc`.

### Alternative artifact recipes

Each alternative recipe keeps the grid shape of the existing recipe and
replaces every Fragment slot except one with a Mechanism (`M`). The existing
Fragment-only recipes stay unchanged. Mixed recipes (for example 2 Fragments
and 2 Mechanisms for the Chronoblade) are not provided.

| Artifact | Existing pattern | Alternative pattern | Fragments | Mechanisms |
|---|---|---|---|---|
| Chronoblade | `FCF / FEF / " U "` | `MCM / FEM / " U "` | 1 | 3 |
| Time Tyrant Mail | `F F / FEF / FUF` | `M M / MEM / FUM` | 1 | 5 |
| Echoing Time Boots | `F F / E E` | `F M / E E` | 1 | 1 |
| Unstable Pocket Watch | `" F " / FUF / " E "` | `" F " / MUM / " E "` | 1 | 2 |

Recipe IDs are `<artifact>_from_clockwork_mechanism`, with the same category
as the existing artifact recipe.

Cost of all four through the alternative recipes: 4 Fragments and 11
Mechanisms, which is 44 Ancient Gears, 11 Enhanced Clockstone and 44 Time
Crystals. The Tyrant itself drops 8 to 12 Enhanced Clockstone, so the
Clockstone part can start right after the kill.

### Time Tyrant drop

The Fragment pool changes from `uniform 3-5` to `uniform 4-6`. The Looting
bonus (`enchanted_count_increase`, 1 to 2 per level) stays as it is. With 4
Fragments guaranteed, one kill without Looting always covers all four
alternative recipes.

### Existing worlds

The drop change only affects Tyrants killed after the update. A world whose
Tyrant is already dead keeps whatever Fragments it got. With 3 Fragments it
can now make three of the four artifacts through the alternative recipes.
Full recovery for those worlds belongs to the Tyrant revival spec.

## Files per version

Follow where the existing artifact files live. The split below was checked
against `time_tyrant_mail`.

| Content | Locations |
|---|---|
| Item ID and registration | `ModItemId`, plus `ModItems` in every version-specific common module |
| Texture and item model | `common/shared/.../assets/chronodawn/` |
| Client items JSON | `common/1.21.4/` and `common/shared-1.21.5+/` |
| Lang | Every version's lang files. 1.20.1 and 1.21.1 lang are standalone |
| Recipes (Mechanism + 4 alternatives) | `common/1.20.1/.../recipes/`, `common/1.21.1/.../recipe/`, `common/shared-1.21.2+/.../recipe/` |
| Recipe unlock advancements | `common/1.20.1/.../advancements/recipes/`, `common/shared-1.21.1+/.../advancement/recipes/`, and the 26.3 override where the existing artifact has one |
| Time Tyrant loot table | `common/1.20.1/.../loot_tables/entities/`, `common/shared-1.21.1+/.../loot_table/entities/`, `common/26.3/.../loot_table/entities/` |

Recipe unlocks: the Mechanism recipe unlocks when the player picks up an
Ancient Gear. Each alternative artifact recipe unlocks when the player picks
up a Clockwork Mechanism or a Fragment of Stasis Core. Mirror the existing
artifact advancement files for shape.

## Verification

- `./gradlew validateResources validateData validateTranslations`
- GameTests, following the existing recipe and loot test patterns:
  - Each alternative recipe matches its grid and yields the right artifact
  - The Mechanism recipe yields a Clockwork Mechanism
  - The Time Tyrant loot table never yields fewer than 4 Fragments without
    Looting
- `./gradlew checkAll`
- Manual check on 1.21.1 NeoForge and 1.21.1 Fabric: the recipes appear in
  the recipe book after picking up the inputs, and crafting works in survival

## Documentation

- `docs/player_guide.md`: Ancient Gear use, new Clockwork Mechanism entry,
  alternative recipes under each artifact, Time Tyrant Fragment count
- `CHANGELOG.md` Unreleased section
- Chronicle (in-game guide) entries that mention Ancient Gear or artifact
  recipes, if any
- `docs/curseforge_description.md` and `docs/modrinth_description.md` only if
  they state the Fragment count or the gear's use
