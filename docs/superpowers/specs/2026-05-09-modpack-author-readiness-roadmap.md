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

**Status**: 🚧 In progress — first PR scoped to Ancient Ruins (`2026-05-09-config-foundation-design.md`).

Cross-loader config infrastructure with per-feature toggles and numeric tuning. The first slice responds to a real user request (Ancient Ruins density), and the infrastructure built here is reusable by every later tunable.

Planned follow-up tunables (each is a separate PR):

- Boss HP / damage multipliers
- Ore generation rates and Y ranges
- Time Distortion (Slowness) strength and scope
- Portal behavior (one-way enforcement, re-ignition rules)
- Dimension-level toggles (per-biome / per-structure enable flags)

### B. Datapack and tag externalization

**Status**: ⏸ Deferred.

Replace hardcoded `Block` / `Item` references with tag lookups where appropriate (recipe ingredients, biome structure lists, etc.) so pack creators can swap materials without source patches. Mostly mechanical work once the tag set is designed; produces small targeted PRs that fit between larger sub-projects.

### C. Scripting events

**Status**: ⏸ Deferred.

Public API exposing events that KubeJS / FTB Quests / CraftTweaker can subscribe to: boss defeated, portal opened, Chronicle entry unlocked, etc. Requires a semver-stable contract — once published, breaking it costs trust with pack creators. Designed only after sub-project A has shaped the configuration patterns these events may reference.

### D. Cross-mod compatibility

**Status**: ⏸ Deferred.

Targeted integrations with flagship mods commonly bundled in packs: Curios / Trinkets (artifacts in accessory slots), Patchouli (alternative guidebook), Create / AE2 cross-recipes. One integration per PR; ordering driven by which mods the existing pack-base most often combines with Chrono Dawn.

## Sequencing rationale

**A first** because:

- Configuration is table stakes — pack creators routinely refuse mods that cannot be tuned.
- The runtime-overlay infrastructure built here generalises beyond Ancient Ruins.
- Each subsequent sub-project benefits from being able to gate behavior on config (B's tag swaps may be config-driven; C's events may need feature flags; D's compat layers may need toggles).

**C second** because it is the largest differentiator for quest-driven modpacks (the dominant pack format). Bosses already exist; exposing their defeat as a pubic event makes Chrono Dawn an obvious choice for questline backbones.

**B and D in parallel / opportunistic** — both are valuable but lower-leverage and can ship as small PRs interleaved with the larger sub-projects.

## Status tracker

| Sub-project | Status | First spec |
| --- | --- | --- |
| A. Config system | 🚧 In progress | [2026-05-09-config-foundation-design.md](./2026-05-09-config-foundation-design.md) |
| B. Datapack / tag externalization | ⏸ Deferred | — |
| C. Scripting events | ⏸ Deferred | — |
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
