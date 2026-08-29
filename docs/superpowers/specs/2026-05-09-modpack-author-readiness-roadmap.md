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

**Status**: 🚧 Conventional material tags and equipment repair consumers are
shipped. Broader tag consumption and additional tag families remain.

Remaining slices:

- Tools / weapons / armor / shield tags (`c:tools/*`, `c:armors/*`) — deferred; tier semantics need their own decisions.
- Mod-owned biome and structure tags.
- The consuming half: replace hardcoded `Block` / `Item` references with tag
  lookups where appropriate (recipe ingredients, biome structure lists, etc.)
  so pack creators can swap materials without source patches. Produce small,
  targeted PRs as each consumer is selected. The first recipe consumer slice
  (modern material inputs) is now shipped; biome and structure consumers remain.

### C. Scripting events

**Status**: 🚧 Direct scripting bindings and additional progression events
remain.

KubeJS / FTB Quests / CraftTweaker bridges, portal-opened events and
Chronicle-entry-unlocked events are independent follow-up slices.

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
| B. Datapack / tag externalization | 🚧 Tool / armor tags, biome / structure tags and broader tag-lookup work remain | [2026-08-17-conventional-tags-design.md](./2026-08-17-conventional-tags-design.md) |
| C. Scripting events | 🚧 Direct bindings and additional progression events remain | [2026-08-29-boss-defeated-event-design.md](./2026-08-29-boss-defeated-event-design.md) |
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
