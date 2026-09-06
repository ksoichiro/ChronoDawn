# Roadmap: Modpack-Author Readiness

**Created**: 2026-05-09
**Status**: Active initiative — remaining work tracked below
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

**Status**: 🚧 Core configuration work is shipped; only deferred or newly
requested tunables remain.

Cross-loader config infrastructure with per-feature toggles and numeric tuning
is available for the existing configurable features. The deferred vanilla-
overlay ore tuning (`iron`, `gold`, `coal`, and `redstone` in the Chrono
dimension) should only be scheduled if a concrete need surfaces.

### B. Datapack and tag externalization

**Status**: 🚧 Conventional material tags, mod-owned biome / structure tags,
and equipment repair consumers are shipped. Broader tag consumption and
additional tag families remain.

Remaining slices:

- Tools / weapons / armor / shield tags (`c:tools/*`, `c:armors/*`) —
  shipped 2026-09-03. The convention needed no tier decision: it offers
  only functional categories, with no shovel/hoe category and no
  per-material armor subtags below 1.21.11. See
  [2026-09-03-tools-armors-shield-tags-design.md](./2026-09-03-tools-armors-shield-tags-design.md).
- The consuming half: replace hardcoded `Block` / `Item` references with tag
  lookups where appropriate (recipe ingredients, biome structure lists, etc.)
  so pack creators can swap materials without source patches. Produce small,
  targeted PRs as each consumer is selected. The first recipe consumer slice
  (modern material inputs) is now shipped.

**Biome / structure material references — reviewed, no conversion
warranted (2026-08-31).** Every hardcoded `Block`/`Item` reference in
worldgen Java is either a structural marker block intrinsic to a placement
algorithm (`Blocks.JIGSAW`, `AMETHYST_BLOCK`, `CRYING_OBSIDIAN`, `DROPPER` in
`MasterClockBossRoomPlacer`, `PhantomCatacombsBossRoomPlacer`,
`EntropyKeeperSpawner` — changing these breaks placement logic, not a
material swap) or already covered by the existing build-time
`scripts/nbt_block_mappings.json` NBT block-swap mechanism for actual
terrain materials. Biome definitions have no Java-level block hardcodes;
they are fully data-driven via `ManagedBiome`/`RuntimeBiomeOverlay` JSON. No
further action is planned for this slice unless a concrete modpack-author
need surfaces.

### C. Scripting events

**Status**: 🚧 CraftTweaker / FTB Quests bindings remain. The
Chronicle-entry-unlocked event is deferred indefinitely (see below).

Boss-defeated and portal-opened events are shipped. The KubeJS scripting
bridge shipped 2026-09-06: a bundled `kubejs.classfilter.txt` lets scripts
call the existing Java events directly via `Java.loadClass`, so no addon
mod or new build dependency was needed. See
[docs/modpack-integration.md](../modpack-integration.md#kubejs-scripting-bridge).
CraftTweaker and FTB Quests bridges remain as independent follow-up slices
(neither offers the same unfiltered-Java-access shortcut as KubeJS, so each
needs its own feasibility check before scoping).

**Chronicle-entry-unlocked event — deferred (2026-09-06).** Investigated
while scoping this slice: the Chronicle guidebook has no "unlocked" concept
today. Every entry loaded from `categories.json` / per-category JSON is
always visible to every player; there is no per-player unlock-state
tracking and no existing trigger that marks an entry unlocked. Firing an
event on entry-unlock therefore requires first deciding whether Chronicle
entries should progressively unlock at all, which unlock criteria would
apply per entry, and how per-player state would persist — a gameplay/UX
design decision for the Chronicle feature itself, not an event-API
integration task. Revisit only after that gameplay design is decided
separately; do not resurrect this as a scripting-events slice until then.

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
| A. Config system | 🚧 Core configuration shipped; deferred vanilla-overlay ore tuning remains optional | [2026-05-09-config-foundation-design.md](./2026-05-09-config-foundation-design.md) |
| B. Datapack / tag externalization | 🚧 Tool/armor/shield tags shipped; remaining work is opportunistic consumer conversion (recipes or Java code still hardcoding a tool/armor `Item` reference) | [2026-08-17-conventional-tags-design.md](./2026-08-17-conventional-tags-design.md), [2026-08-29-tag-consumers-design.md](./2026-08-29-tag-consumers-design.md), [2026-08-31-biome-structure-tags-design.md](./2026-08-31-biome-structure-tags-design.md), [2026-09-03-tools-armors-shield-tags-design.md](./2026-09-03-tools-armors-shield-tags-design.md) |
| C. Scripting events | 🚧 KubeJS bridge shipped; CraftTweaker/FTB Quests bindings remain; Chronicle-entry-unlocked event deferred indefinitely (no unlock concept exists to hook) | [2026-08-29-boss-defeated-event-design.md](./2026-08-29-boss-defeated-event-design.md), [2026-09-04-portal-opened-event-design.md](./2026-09-04-portal-opened-event-design.md) |
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
