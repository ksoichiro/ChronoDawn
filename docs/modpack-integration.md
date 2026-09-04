# Modpack Integration Guide

Chrono Dawn is designed to be a friendly modpack ingredient. This guide
collects the integration points modpack creators are most likely to need.

If you find a missing integration point, please open an issue at
[https://github.com/ksoichiro/ChronoDawn/issues](https://github.com/ksoichiro/ChronoDawn/issues).

---

## Configuration

The mod reads `config/chronodawn.toml` once at startup. The full schema is
documented in [`docs/configuration.md`](configuration.md).

### Bundling a configuration with your pack

Place a pre-edited `chronodawn.toml` in your pack's overrides directory:

```
my-pack/
  overrides/
    config/
      chronodawn.toml   # ← your customized values
    ...
```

Chrono Dawn will read this file on first launch instead of writing the
default. Players can still edit it, and unknown keys / invalid values fall
back individually so a future Chrono Dawn version with new fields will not
break your bundled file.

### Example: rebalance a single ore

```toml
# Make Entropy Crystal one-third as common; deeper-only.
[world.ores.entropy_crystal]
count = 1
y_min = 5
y_max = 50
```

### Example: lower Clockstone tier-1 abundance

```toml
# Halve Clockstone density. Tier-1 progression slows; pairs well with
# raising the bundled Time Crystal / Entropy Crystal counts elsewhere.
[world.ores.clockstone]
count = 4
```

Bundle this file via `overrides/config/chronodawn.toml` and the runtime
overlay applies it on first launch. The other ores stay at their
defaults. See [`docs/configuration.md`](configuration.md) for the full
schema.

### Example: retune a dungeon's density

```toml
# Make the Entropy Crypt denser for a shorter progression loop.
[world.structures.entropy_crypt]
spacing = 30
separation = 12
```

### Example: disable a structure

```toml
# Remove the Guardian Vault. Chronos Warden and the Guardian Stone must
# come from your pack instead.
[world.structures.guardian_vault]
enabled = false
```

Disabling any structure other than Ancient Ruins gates progression — Chrono
Dawn logs a startup warning naming what becomes unobtainable. See
[`configuration.md`](configuration.md) for the full per-structure table
and the progression-warning behavior.

### Example: soften ambient Time Distortion

```toml
[gameplay.time_distortion]
# Keep the dimension's identity, but reduce its ambient movement penalty.
normal_slowness_level = 2
enhanced_slowness_level = 3
```

Set `enabled = false` to remove only the dimension-wide effect. This does not
remove Slowness from individual mobs, boss abilities, items, or blocks. The
default `scope = "hostile_mobs"` matches the shipped behavior; use
`"all_mobs"` only when the pack should also slow passive and friendly
mobs. Players and Chrono Dawn bosses remain excluded in both scopes.

### Example: place the bosses later in your progression

```toml
# Chrono Dawn's bosses sit mid-game by default. If your pack gates the
# Chrono dimension behind endgame gear, scale them up.
[gameplay.bosses.time_guardian]
health_multiplier = 2.0
damage_multiplier = 1.5

[gameplay.bosses.time_tyrant]
health_multiplier = 2.5
damage_multiplier = 1.5
```

`damage_multiplier` scales the boss's direct damage — melee, area-of-effect
and ground-slam abilities, projectiles, and Entropy Keeper's degradation —
together, so the encounter stays internally consistent. It does **not**
scale status effects a boss applies (Entropy Keeper's Wither and Poison;
the Slowness / Mining Fatigue debuffs other bosses apply) — a
`MobEffect` amplifier is an integer and cannot express a fractional
multiplier. Setting it to `0.0` removes direct damage, but a boss that
applies status effects keeps applying them at full strength — it is not
fully harmless. See [`configuration.md`](configuration.md) for the full
base-stat table.

### Restart and existing-world caveats

| Setting type | Restart needed? | Affects what already exists in a save? |
| --- | --- | --- |
| `world.structures.*` | yes (server / world reload) | no — only new chunks |
| `world.ores.*` | yes (server / world reload) | no — only new chunks |
| `gameplay.time_distortion` | yes (server / world reload) | yes — applies to eligible mobs as they tick after restart |
| `gameplay.portals` | yes (server / world reload) | yes — controls the next portal use or portal transit after restart |
| `gameplay.bosses.*` | yes (server / world reload) | **yes** — bosses already spawned are rescaled on load |

The worldgen rows are vanilla Minecraft constraints; the mod cannot work
around them. Document them in your pack notes if you expect players to
retune values mid-game.

#### Retuning bosses mid-save

`gameplay.bosses.*` behaves differently from the worldgen options, and the
difference can surprise players: a boss already standing in a loaded save
picks up the new multiplier the next time the world loads. Its **current
health is preserved while its maximum changes**.

That matters because every boss phase is a health *ratio*, not an absolute
value — Time Guardian switches phase at 50%, Time Tyrant at 66% and 33%,
with its Time Reversal ability triggering at 20%. Doubling
`health_multiplier` on an existing boss halves its health ratio, so a boss
sitting at full health in the previous session can reload directly into a
later phase. Lowering the multiplier does the reverse, and Minecraft clamps
current health down if it now exceeds the new maximum.

If your pack ships a boss retune as a mid-season update, either apply it
before players reach the boss, or say so in your changelog — a player who
left a boss fight half-finished will come back to a different one.

### Pack precedence

The mod ships its config-driven worldgen as a *built-in* data pack named
`chronodawn-runtime-overlay`. It sits **above** the bundled mod resources but
**below** any data pack the player or pack adds via the resource pack screen
or `world/datapacks/`. So:

1. User-installed datapacks (highest priority) — your pack can drop a custom
   `data/chronodawn/worldgen/structure_set/ancient_ruins.json` here to
   completely override what the config produces. The mod will not fight you.
2. `chronodawn-runtime-overlay` — generated from `chronodawn.toml`.
3. Bundled mod resources (lowest priority) — defaults shipped inside the JAR.

Use this in your favor: ship the values most players want via
`chronodawn.toml`, and reserve datapack overrides for genuinely custom
content (different mob lists, custom variants, etc.).

---

## Datapack-level overrides

Beyond the configuration file, you can swap any of Chrono Dawn's worldgen,
loot, recipe, or tag JSONs by including a datapack in your pack. The mod's
namespace is `chronodawn`. The most commonly customized resources:

- **Structures and placements**: `data/chronodawn/worldgen/structure*/...`
- **Loot tables**: `data/chronodawn/loot_table/...`
  - For 1.20.1 the directory is `data/chronodawn/loot_tables/...` (vanilla
    rename).
- **Recipes**: `data/chronodawn/recipe/...`
- **Biomes**: `data/chronodawn/worldgen/biome/...`
- **Tags**: `data/chronodawn/tags/...`

The mod follows vanilla data pack conventions throughout, so no
mod-specific syntax is required.

### Chrono Dawn biome and structure tags

The following mod-owned worldgen tags are stable integration points for
datapacks. They select groups of Chrono Dawn registry entries without requiring
your pack to repeat individual IDs:

| Tag | Contents |
| --- | --- |
| `#chronodawn:chronodawn_biomes` | All eleven Chrono Dawn biomes. |
| `#chronodawn:chronodawn_structures` | All eight Chrono Dawn structures. |
| `#chronodawn:boss_structures` | Desert Clock Tower, Guardian Vault, Clockwork Depths, Phantom Catacombs, Entropy Crypt, and Master Clock. |

For example, a biome selector that accepts biome tags can use
`#chronodawn:chronodawn_biomes` to target the whole dimension. The existing
`#chronodawn:has_*` biome tags control individual structure placement and are
not part of this public grouping API.

---

## Conventional (`c:`) tags

Chrono Dawn's ores, materials, storage blocks, stone/sand/gravel/sandstone,
wood sets, foods, crops and seeds are exposed through the Fabric/NeoForge
conventional tag namespace (`c:`), so unification recipes, JEI/EMI
recipe-viewer plugins, and other mods that consume materials by tag pick up
Chrono Dawn's items and blocks automatically. No loader-specific files are
needed: NeoForge reads the same `c:` namespace as Fabric.

The convention itself changed shape between the Minecraft versions this mod
supports, so the published tag set differs by era.

### 1.21.1–1.21.11

This range uses conventional tags v2, which has per-material subtags under
each umbrella (`c:ores/coal`, `c:gems/amethyst`, and so on). Every tag below
is stable across all ten 1.21.x versions this mod supports.

| Members | Tags |
| --- | --- |
| `temporal_coal_ore` | `c:ores/coal`, `c:ores`¹, `c:ores_in_ground/stone` |
| `temporal_iron_ore` | `c:ores/iron`, `c:ores`¹, `c:ores_in_ground/stone` |
| `temporal_gold_ore`, `deepslate_temporal_gold_ore` | `c:ores/gold`, `c:ores`¹, `c:ores_in_ground/stone` or `/deepslate` |
| `temporal_redstone_ore`, `deepslate_temporal_redstone_ore` | `c:ores/redstone`, `c:ores`¹, `c:ores_in_ground/stone` or `/deepslate` |
| `clockstone_ore`, `deepslate_clockstone_ore` | `c:ores/clockstone`, `c:ores`, `c:ores_in_ground/stone` or `/deepslate` |
| `time_crystal_ore` | `c:ores/time_crystal`, `c:ores`, `c:ores_in_ground/stone` |
| `entropy_crystal_ore` | `c:ores/entropy_crystal`, `c:ores`, `c:ores_in_ground/stone` |
| `temporal_amber_ore`, `deepslate_temporal_amber_ore` | `c:ores/temporal_amber`, `c:ores`, `c:ores_in_ground/stone` or `/deepslate` |
| `temporal_stone` | `c:ore_bearing_ground/stone` |
| `deepslate_temporal_stone` | `c:ore_bearing_ground/deepslate` |
| `clockstone` | `c:ingots/clockstone`, `c:ingots` |
| `enhanced_clockstone` | `c:ingots/enhanced_clockstone`, `c:ingots` |
| `time_crystal` | `c:gems/time_crystal`, `c:gems` |
| `entropy_crystal` | `c:gems/entropy_crystal`, `c:gems` |
| `raw_temporal_amber` | `c:raw_materials/temporal_amber`, `c:raw_materials` |
| `temporal_amber_dust` | `c:dusts/temporal_amber`, `c:dusts` |
| `clockstone_block` | `c:storage_blocks/clockstone`, `c:storage_blocks` |
| `time_crystal_block` | `c:storage_blocks/time_crystal`, `c:storage_blocks` |
| `temporal_stone` | `c:stones` |
| `temporal_cobblestone` | `c:cobblestones/normal`, `c:cobblestones` |
| `mossy_temporal_cobblestone` | `c:cobblestones/mossy`, `c:cobblestones` |
| `cobbled_deepslate_temporal_stone` | `c:cobblestones/deepslate`, `c:cobblestones` |
| `temporal_sand` | `c:sands` |
| `temporal_gravel` | `c:gravels` |
| `temporal_sandstone` | `c:sandstone/blocks` |
| `temporal_sandstone_slab` | `c:sandstone/slabs` |
| `temporal_sandstone_stairs` | `c:sandstone/stairs` |
| `stripped_*_log` (time_wood, dark_time_wood, ancient_time_wood) | `c:stripped_logs` |
| `stripped_*_wood` (time_wood, dark_time_wood, ancient_time_wood) | `c:stripped_woods` |
| `*_fence` (time_wood, dark_time_wood, ancient_time_wood) | `c:fences/wooden`, `c:fences` |
| `*_fence_gate` (time_wood, dark_time_wood, ancient_time_wood) | `c:fence_gates/wooden`, `c:fence_gates` |
| `time_bread`, `enhanced_time_bread` | `c:foods/bread`, `c:foods` |
| `time_wheat_cookie`, `clockwork_cookie` | `c:foods/cookie`, `c:foods` |
| `time_fruit_pie` | `c:foods/pie`, `c:foods` |
| `fruit_of_time`, `chrono_melon_slice` | `c:foods/fruit`, `c:foods` |
| `temporal_root`, `baked_temporal_root` | `c:foods/vegetable`, `c:foods` |
| `temporal_root_stew`, `timeless_mushroom_soup` | `c:foods/soup`, `c:foods` |
| `chrono_bovine_meat` | `c:foods/raw_meat`, `c:foods` |
| `cooked_chrono_bovine_meat` | `c:foods/cooked_meat`, `c:foods` |
| `glide_fish` | `c:foods/raw_fish`, `c:foods` |
| `cooked_glide_fish` | `c:foods/cooked_fish`, `c:foods` |
| `golden_time_wheat`, `glistening_chrono_melon` | `c:foods/golden`, `c:foods` |
| `chrono_melon_juice` | `c:drinks/juice`, `c:drinks` |
| `timeless_mushroom` | `c:mushrooms` |
| `time_wheat` | `c:crops/time_wheat`, `c:crops` |
| `time_wheat_seeds` | `c:seeds/time_wheat`, `c:seeds` |
| `chrono_melon` | `c:crops/chrono_melon`, `c:crops` |
| `chrono_melon_seeds` | `c:seeds/chrono_melon`, `c:seeds` |

Ore tags apply to both the block and its item form. `temporal_sand`
deliberately joins only the `c:sands` umbrella — its tint matches neither
`c:sands/colorless` nor `c:sands/red`. `chrono_melon_juice` and
`timeless_mushroom` are not foods in the tag sense and join only the tags
listed for them.

¹ On 1.21.2, 1.21.3, 1.21.4 and 1.21.5 the loaders' own data does not
reference `#c:ores/coal` (or `/iron`, `/gold`, `/redstone`) from the `c:ores`
umbrella, so on those four versions Chrono Dawn's ores of vanilla materials
are reachable through their own subtag but not through `c:ores`. 1.21.1 and
1.21.6+ reference them, and the mod-coined subtags (`c:ores/clockstone` and
friends) are referenced from this mod's own `c:ores` file on every version.
Target the per-material subtag when you need a specific material — that is
the tag mods read anyway.

Every tag path this mod coined carries a display name (`tag.item.c.*`) in
`en_us` and `ja_jp`, so recipe viewers show a readable name rather than the
raw id. A build-time check keeps that true for tags added later.

### Equipment repair materials

On 1.21.1 and newer, Chrono Dawn equipment consumes the following per-material
tags for its repair ingredient. This lets a unification addon or pack datapack
add compatible items without replacing Chrono Dawn's Java classes:

| Equipment | Repair tag |
| --- | --- |
| Clockstone and Enhanced Clockstone equipment | `c:gems/time_crystal` |
| Entropy Crystal equipment | `c:gems/entropy_crystal` |
| Temporal Amber armor | `c:dusts/temporal_amber` |
| Chronoblade and Time Tyrant Mail | `c:gems/time_crystal` |

Minecraft 1.20.1 retains exact-item repair ingredients because its conventional
`c:gems` tag has no per-material subtags and includes both crystal types.

### Recipe material inputs

On 1.21.1 and newer, recipes that consume Chrono Dawn's material items use the
same conventional tags. A unification addon or datapack can therefore provide
compatible members without replacing the recipes:

| Recipe input | Tag |
| --- | --- |
| Clockstone | `c:ingots/clockstone` |
| Clockstone Block | `c:storage_blocks/clockstone` |
| Enhanced Clockstone | `c:ingots/enhanced_clockstone` |
| Time Crystal | `c:gems/time_crystal` |
| Time Crystal Block | `c:storage_blocks/time_crystal` |
| Entropy Crystal | `c:gems/entropy_crystal` |
| Raw Temporal Amber | `c:raw_materials/temporal_amber` |

This conversion covers material-bearing recipes only. Recipes whose inputs do
not have a clear conventional equivalent remain unchanged. Minecraft 1.20.1
keeps the original exact-item recipe inputs because its conventional tags do
not provide the required per-material distinction.

### 1.20.1

1.20.1 uses conventional tags v1, which has **no per-material subtags** —
only flat plural umbrella tags. This means 1.20.1 carries a reduced set
compared to 1.21.1+: roughly a third of the coverage, and materials cannot
be targeted individually by tag on this version, only by category.

| Tag | Members |
| --- | --- |
| `c:ores` | all eight ore families (blocks and items) |
| `c:ingots` | `clockstone`, `enhanced_clockstone` |
| `c:gems` | `time_crystal`, `entropy_crystal` |
| `c:raw_ores` | `raw_temporal_amber` |
| `c:dusts` | `temporal_amber_dust` |
| `c:foods` | every food item listed in the 1.21.1+ table above |
| `c:sandstone_blocks` / `c:sandstone_slabs` / `c:sandstone_stairs` | temporal sandstone set |

Categories that exist in v2 but not v1 — `stones`, `gravels`, `cobblestones`,
`storage_blocks`, `crops`, `seeds`, `mushrooms`, `fences/wooden`,
`stripped_logs`, `stripped_woods`, `drinks` — are not emitted on 1.20.1,
because inventing tag names outside the convention would produce files no
other mod reads.

### Tools, weapons, armor, and shields

Chrono Dawn's tool, weapon, armor, and shield items — including boss-drop
and artifact gear — join the conventional tag categories the Fabric/NeoForge
convention actually provides. Unlike the material tags above, this
convention has no per-material subtags for equipment: only functional
categories.

On 1.21.1+:

| Chrono Dawn items | Tag |
| --- | --- |
| Clockstone, Enhanced Clockstone, and Entropy Crystal swords; Chronoblade; Clockstone and Enhanced Clockstone axes | `c:tools/melee_weapon` |
| Clockstone, Enhanced Clockstone, and Spatially Linked pickaxes | `c:tools/mining_tool` |
| Clockstone, Enhanced Clockstone, and Entropy Crystal shields | `c:tools/shield` |
| Clockstone, Enhanced Clockstone, and Temporal Amber armor (all 4 pieces each); Time Tyrant's Mail and Echoing Time Boots | `c:armors` |

Clockstone and Enhanced Clockstone shovels and hoes join no `c:` tag — the
convention has no category for them on this range. `c:armors/humanoid` and
its sibling subtags (`horse`, `nautilus`, `wolf`) exist only from 1.21.11
and are not yet published; the bare `c:armors` umbrella already means
humanoid armor across the whole 1.21.1–1.21.11 range.

On 1.20.1, which uses conventional tags v1's flat per-tool-type tags and has
no armor tag in any form:

| Chrono Dawn items | Tag |
| --- | --- |
| Swords (as above) | `c:swords` |
| Pickaxes (as above) | `c:pickaxes` |
| Axes | `c:axes` |
| Shovels | `c:shovels` |
| Hoes | `c:hoes` |
| Shields | `c:shields` |

---

## Boss defeated event API

Java addons can subscribe to a loader-neutral event for all six Chrono Dawn
bosses. The same API is present in Fabric and NeoForge builds for every
supported Minecraft version.

```java
import com.chronodawn.api.event.BossDefeatedEvents;
import com.chronodawn.api.event.BossDefeatedListener;

public final class MyChronoDawnIntegration {
    private static final BossDefeatedListener BOSS_LISTENER = context -> {
        String bossId = context.bossId();
        if (context.defeatingPlayer() != null) {
            // Advance your quest or pack progression for this player.
        }
    };

    public static void register() {
        BossDefeatedEvents.register(BOSS_LISTENER);
    }

    public static void unregister() {
        BossDefeatedEvents.unregister(BOSS_LISTENER);
    }
}
```

Keep the listener instance if your addon supports reload or shutdown; passing a
new lambda to `unregister` does not remove the original registration.

The `BossDefeatedContext` accessors are:

| Accessor | Value |
| --- | --- |
| `bossId()` | Stable namespaced boss ID. |
| `boss()` | Defeated `LivingEntity`, valid during the synchronous callback. |
| `level()` | Server level where the boss was defeated. |
| `position()` | Immutable block-position snapshot at dispatch. |
| `damageSource()` | Minecraft damage source that caused the death. |
| `defeatingPlayer()` | Attributed `ServerPlayer`, or `null` for environmental or otherwise unattributed damage. |

Stable boss IDs:

| Boss | ID |
| --- | --- |
| Time Guardian | `chronodawn:time_guardian` |
| Chronos Warden | `chronodawn:chronos_warden` |
| Clockwork Colossus | `chronodawn:clockwork_colossus` |
| Entropy Keeper | `chronodawn:entropy_keeper` |
| Temporal Phantom | `chronodawn:temporal_phantom` |
| Time Tyrant | `chronodawn:time_tyrant` |

Callbacks run synchronously on the logical server after the boss's built-in
defeat consequences. For example, a Time Tyrant listener observes the dimension
as already stabilized. Listeners run in registration order. If one listener
throws a runtime exception, Chrono Dawn logs it and continues with the remaining
listeners without interrupting the boss death.

The Java event is the foundation for scripting integrations. Direct KubeJS,
CraftTweaker and FTB Quests bindings are not shipped yet; a pack cannot register
this event from those scripting systems without an addon bridge.

---

## Portal opened event API

Java addons can subscribe to a loader-neutral event that fires whenever a
Chrono Dawn portal transitions into the active state, whether a player
ignites a fresh frame with a Time Hourglass or the mod generates or reuses a
return portal while teleporting someone through an already-active one.

```java
import com.chronodawn.api.event.PortalOpenedEvents;
import com.chronodawn.api.event.PortalOpenedListener;

public final class MyChronoDawnIntegration {
    private static final PortalOpenedListener PORTAL_LISTENER = context -> {
        if (context.cause() == com.chronodawn.api.event.PortalOpenCause.IGNITION
                && context.igniter() != null) {
            // Advance your quest or pack progression for this player.
        }
    };

    public static void register() {
        PortalOpenedEvents.register(PORTAL_LISTENER);
    }

    public static void unregister() {
        PortalOpenedEvents.unregister(PORTAL_LISTENER);
    }
}
```

Keep the listener instance if your addon supports reload or shutdown; passing a
new lambda to `unregister` does not remove the original registration.

The `PortalOpenedContext` accessors are:

| Accessor | Value |
| --- | --- |
| `portalId()` | Stable `UUID` of the physical portal in the internal registry. |
| `level()` | Server level containing the portal frame. |
| `position()` | Frame's bottom-left block position. |
| `cause()` | `PortalOpenCause.IGNITION` (Time Hourglass) or `PortalOpenCause.REIGNITION` (teleport-time generation or reuse). |
| `igniter()` | Credited `ServerPlayer`, or `null` when none is known. |

Callbacks run synchronously on the logical server after the portal's blocks
are placed and its state has already transitioned to active; a listener
never observes a `STABILIZED` portal's routine block regeneration, only an
actual activation. Listeners run in registration order. If one listener
throws a runtime exception, Chrono Dawn logs it and continues with the
remaining listeners without interrupting portal activation.

The Java event is the foundation for scripting integrations. Direct KubeJS,
CraftTweaker and FTB Quests bindings are not shipped yet; a pack cannot register
this event from those scripting systems without an addon bridge.

---

## Future integrations *(not yet shipped)*

### Additional scripting events and bindings

The Chronicle-entry-unlocked event, plus direct KubeJS, FTB Quests and
CraftTweaker bindings for the boss-defeated and portal-opened events, are
planned as independent follow-up slices built on the stability rules
established by those two events.

### Cross-mod compatibility (planned)

Targeted integrations with other commonly-bundled mods (Curios / Trinkets,
Patchouli, Create / AE2, etc.) will be added as separate optional features.
Each integration ships as opt-in code paths gated on the other mod's
presence — no hard dependencies.

---

## Reporting integration issues

When opening an issue, please include:

- The Chrono Dawn version and the Minecraft / loader versions you are using.
- The relevant section of `chronodawn.toml` (if config-related).
- The pack's full mod list (or a link to the modpack page) is helpful but not
  required.

The maintainers prioritize integration issues, since modpack inclusion is the
primary distribution channel for the mod.
