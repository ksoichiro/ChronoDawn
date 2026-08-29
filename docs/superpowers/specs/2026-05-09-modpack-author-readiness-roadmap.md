# Roadmap: Modpack-Author Readiness

**Created**: 2026-05-09
**Status**: Active initiative — multi-PR
**Type**: Living document (updated as sub-projects ship)

---

## Why this initiative exists

Chrono Dawn's downloads (52.8k as of 2026-05-07, ~5 months since launch) are primarily driven by **modpack inclusion**: a single confirmed pack accounts for the bulk of the count. Every additional pack that includes the mod is a strong multiplier on download growth.

Modpack creators evaluate mods on criteria that Chrono Dawn currently does not fully address. Closing those gaps is the highest-leverage development direction available within this repository (separate from promotion / external work, which is excluded from scope).

This roadmap exists to:

- Make explicit that "modpack-author readiness" is **not a single project** but a long-running initiative composed of independent sub-projects.
- Record the sequencing rationale so context is not lost between PRs.
- Provide a status tracker that future sessions can pick up from cold.

## Sub-projects

### A. Config system

**Status**: 🚧 Seven PRs shipped: Ancient Ruins ([design](./2026-05-09-config-foundation-design.md), [plan](../plans/2026-05-09-config-foundation.md)), Ore generation tuning for Time Crystal / Entropy Crystal / Temporal Amber ([design](./2026-05-16-ore-generation-tuning-design.md), [plan](../plans/2026-05-16-ore-generation-tuning.md)), Clockstone tuning ([design](./2026-05-17-ore-clockstone-config-tuning-design.md), [plan](../plans/2026-05-17-ore-clockstone-config-tuning.md)), Boss multipliers ([design](./2026-07-25-boss-stat-multipliers-design.md)), Time Distortion tuning ([design](./2026-08-02-time-distortion-config-design.md)), portal behavior ([design](./2026-08-04-portal-behavior-config-design.md)), and per-structure generation toggles ([design](./2026-08-22-structure-toggles-design.md)). Continuing with follow-up tunables.

Cross-loader config infrastructure with per-feature toggles and numeric tuning. The first slice responds to a real user request (Ancient Ruins density), and the infrastructure built here is reusable by every later tunable.

Planned follow-up tunables (each is a separate PR):

- Boss HP / damage multipliers — *shipped: per-boss `health_multiplier` /
  `damage_multiplier` for all six bosses ([design](./2026-07-25-boss-stat-multipliers-design.md))*
- Ore generation rates and Y ranges — *partial: Time Crystal / Entropy Crystal / Temporal Amber / Clockstone shipped. Vanilla-overlay ores in the Chrono dimension (`iron` / `gold` / `coal` / `redstone`) remain deferred — will be reconsidered if a request surfaces.*
- Time Distortion (Slowness) strength and scope — *shipped: `enabled`, normal /
  enhanced Slowness levels, and `hostile_mobs` / `all_mobs` scope
  ([design](./2026-08-02-time-distortion-config-design.md))*
- Portal behavior (one-way enforcement, re-ignition rules) — *shipped:
  `one_way_until_stabilized` and `allow_reignition_before_stabilization`
  ([design](./2026-08-04-portal-behavior-config-design.md))*
- Dimension-level toggles (per-biome / per-structure enable flags) — *shipped:
  per-structure `enabled` / `spacing` / `separation` / `salt` shipped for all
  eight structures, with a startup warning naming what a disabled
  progression-gating structure removes
  ([design](./2026-08-22-structure-toggles-design.md)). Per-biome `enabled`
  flags shipped for all nine non-core Chrono dimension biomes, with a
  documented fallback biome absorbing a disabled biome's region of the biome
  distribution and a startup warning naming what becomes unobtainable
  ([design](./2026-08-23-biome-toggles-design.md)).*

### B. Datapack and tag externalization

**Status**: 🚧 Publishing half shipped: conventional (`c:`) tags for materials — ores, raw materials, ingots and gems, storage blocks, stone / sand / gravel / sandstone, wood sets, foods, crops and seeds — across all supported versions, with a coverage guard and en_us / ja_jp names ([design](./2026-08-17-conventional-tags-design.md), [plan](../plans/2026-08-17-conventional-tags.md)). Documented in [`docs/modpack-integration.md`](../../modpack-integration.md).

Remaining slices:

- Tools / weapons / armor / shield tags (`c:tools/*`, `c:armors/*`) — deferred; tier semantics need their own decisions.
- Mod-owned biome and structure tags.
- The consuming half: replace hardcoded `Block` / `Item` references with tag lookups where appropriate (recipe ingredients, biome structure lists, etc.) so pack creators can swap materials without source patches. Mostly mechanical work now that the tag set exists; produces small targeted PRs that fit between larger sub-projects.

### C. Scripting events

**Status**: 🚧 Boss-defeated Java event shipped
([design](./2026-08-29-boss-defeated-event-design.md)); direct scripting
bindings and additional progression events remain.

The first semver-stable public contract exposes server-side defeat notifications
for all six bosses through a loader-neutral Java API. KubeJS / FTB Quests /
CraftTweaker bridges, portal-opened events and Chronicle-entry-unlocked events
remain independent follow-up slices.

### D. Cross-mod compatibility

**Status**: ⏸ Deferred.

Targeted integrations with flagship mods commonly bundled in packs: Curios / Trinkets (artifacts in accessory slots), Patchouli (alternative guidebook), Create / AE2 cross-recipes. One integration per PR; ordering driven by which mods the existing pack-base most often combines with Chrono Dawn.

## Sequencing rationale

**A first** because:

- Configuration is table stakes — pack creators routinely refuse mods that cannot be tuned.
- The runtime-overlay infrastructure built here generalises beyond Ancient Ruins.
- Each subsequent sub-project benefits from being able to gate behavior on config (B's tag swaps may be config-driven; C's events may need feature flags; D's compat layers may need toggles).

**C second** because it is the largest differentiator for quest-driven modpacks (the dominant pack format). Bosses already exist; exposing their defeat as a public event makes Chrono Dawn an obvious choice for questline backbones.

**B and D in parallel / opportunistic** — both are valuable but lower-leverage and can ship as small PRs interleaved with the larger sub-projects.

## Status tracker

| Sub-project | Status | First spec |
| --- | --- | --- |
| A. Config system | 🚧 Eight PRs shipped (Ancient Ruins, Ore tuning, Clockstone tuning, Boss multipliers, Time Distortion tuning, Portal behavior, Structure toggles, Biome toggles) | [2026-05-09-config-foundation-design.md](./2026-05-09-config-foundation-design.md), [2026-07-25-boss-stat-multipliers-design.md](./2026-07-25-boss-stat-multipliers-design.md), [2026-08-02-time-distortion-config-design.md](./2026-08-02-time-distortion-config-design.md), [2026-08-04-portal-behavior-config-design.md](./2026-08-04-portal-behavior-config-design.md), [2026-08-22-structure-toggles-design.md](./2026-08-22-structure-toggles-design.md), [2026-08-23-biome-toggles-design.md](./2026-08-23-biome-toggles-design.md) |
| B. Datapack / tag externalization | 🚧 Conventional (`c:`) material tags shipped; tool / armor tags, biome / structure tags and the tag-lookup half remain | [2026-08-17-conventional-tags-design.md](./2026-08-17-conventional-tags-design.md) |
| C. Scripting events | 🚧 Boss-defeated Java event shipped; direct bindings and more progression events remain | [2026-08-29-boss-defeated-event-design.md](./2026-08-29-boss-defeated-event-design.md) |
| D. Cross-mod compatibility | ⏸ Deferred | — |

Each sub-project will produce multiple PRs over time. As work completes, this table is updated to reflect which slices have shipped.

## Out of scope for this initiative

- Promotion, marketing, social content (separate concern, excluded from in-repo work).
- New gameplay content additions (separate development track).
- Performance optimization (handled per need, not as part of this initiative).
- Localization infrastructure (already shipped — see `CONTRIBUTING.md` "Translations" section).

## How to update this document

When a sub-project's first PR ships:

1. Flip its status emoji to ✅ (or 🚧 if expansion follow-ups are planned).
2. Add link(s) to the design spec(s) that shipped.
3. Note any scope or sequencing changes the work surfaced.
4. If a new sub-project is identified, add it to the table with rationale.
