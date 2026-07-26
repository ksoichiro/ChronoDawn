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

`damage_multiplier` covers melee, area attacks and projectiles together,
so the encounter stays internally consistent. Setting it to `0.0` leaves
the boss present but harmless — useful for cinematic or story-driven
packs. See [`configuration.md`](configuration.md) for the full base-stat
table.

### Restart and existing-world caveats

| Setting type | Restart needed? | Affects what already exists in a save? |
| --- | --- | --- |
| `world.structures.*` | yes (server / world reload) | no — only new chunks |
| `world.ores.*` | yes (server / world reload) | no — only new chunks |
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

---

## Future integrations *(not yet shipped)*

The following sections will land in subsequent releases. They are listed
here so this document stays the canonical entry point for pack creators.

### Scripting events (planned)

A public Java API and KubeJS / FTB-Quests / CraftTweaker bindings will let
pack creators react to in-game events such as boss defeats, portal openings,
and Chronicle entry unlocks. These will respect a semver-stable contract and
will be designed to integrate with the configuration system documented above.

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
