# Flagship-Parity Roadmap: Approach Outlines

**Created**: 2026-07-05
**Status**: Analysis — rough directions only; no detailed designs, no implementation
**Parent**: [2026-07-05-flagship-parity-roadmap.md](./2026-07-05-flagship-parity-roadmap.md)

---

This document records a **rough approach** for every work item in the
flagship-parity roadmap: the chosen direction, the first PR-sized slice, and
the main risks/prerequisites. It deliberately stops short of detailed design —
each M+ item still gets its own design spec + plan (per the existing
`docs/superpowers/` convention) before implementation.

## Cross-cutting rules (apply to every item)

- **Soft dependencies only.** Integrations use `compileOnly` deps, runtime
  `isModLoaded` guards, and classes isolated in `com.chronodawn.compat.<mod>`
  packages so missing mods never trigger classloading. The base JAR gains no
  new hard dependencies.
- **Config-gated behavior.** Anything that changes existing gameplay ships
  behind a `chronodawn.toml` key using the shipped config infrastructure.
- **One PR per slice**, `checkAll` green before merge, English docs.
- **Version-era awareness.** Every item that touches resources or client APIs
  must state which shared-resource directory / version modules it lands in;
  the 12-version matrix is the primary cost multiplier, so slices should start
  on a small version set and widen only after the shape is proven.

---

## P1. Ecosystem integrations

### P1-1. `c:` common tags

**Direction.** Fabric and NeoForge converged on the `c:` namespace in 1.21+,
and Fabric already used `c:` conventions on 1.20.1 (where we ship Fabric
only) — so a single `data/c/tags/...` tree in the shared resource directories
covers all targets, with per-era path differences (`tags/items` vs
`tags/item`) handled by the existing `shared-1.21.x+` directory stacking.
Two tag families:

1. Material tags others consume: `c:ores/...`, `c:ingots/...`, `c:gems/...`,
   `c:foods/...`, wood-set tags (`c:planks`, `c:stripped_logs`, …) for all
   three wood families.
2. Mod-owned tags others target: `chronodawn:biomes/...`,
   `chronodawn:structures/...`, plus a dimension tag, so compat mods and
   datapacks can address "anything in Chrono Dawn" without hardcoding IDs.

**First slice.** Material tags for the four ores + their drops and the food
items; extend `validateResources` to verify tag entries resolve to registered
IDs so tag drift is caught at build time.

**Risks.** Tag path renames across eras (plural→singular) — same class of
pitfall as the loot-table directory rename already documented; the validation
extension is the mitigation.

### P1-2. Recipe-viewer integration (JEI / EMI / REI)

**Direction.** Vanilla-type recipes already render automatically; the gap is
*mechanics* — portal construction, Portal Stabilizer usage, boss-drop sources,
artifact effects. Ship **information pages** (JEI "info recipes" / EMI info
pages), with all text in lang files and page composition in a small
data-driven registry, so per-viewer plugin code is a thin adapter. Implement
JEI + EMI natively (JEI covers both loaders, EMI dominant in newer Fabric
packs); REI is served via its JEI-compat layer — no native REI plugin unless
demand appears.

**First slice.** JEI info pages for portal construction + Portal Stabilizer on
two versions only (1.21.1 as the modpack baseline, and latest), guarded and
isolated per the cross-cutting rules. Widen versions and add EMI after the
content registry shape survives contact.

**Risks.** JEI/EMI API churn across 12 versions — mitigated by keeping
adapters minimal and content data-driven; accept that only a version subset is
covered initially and say so in README.

### P1-3. Jade / WTHIT tooltip support

**Direction.** Jade first (it runs on both loaders); WTHIT deferred unless
requested. One plugin class registering component providers for: crop growth
stages (Time Wheat now, crops from P5-1 later), stalactite/coral/torch variant
naming, and portal block state (stabilized or not). No entity providers
initially — bosses already have boss bars.

**First slice.** Jade plugin with the crop + portal providers on latest
version; backfill versions opportunistically.

**Risks.** Low — Jade's API is comparatively stable; this is the cheapest,
most visible integration and a good pattern-setter for the compat package
layout.

### P1-4. Accessory-slot support (Curios / Trinkets)

**Direction.** First refactor inward: extract an internal "artifact ability"
abstraction (equip/unequip/tick/proc) that the existing PLAYER_POST-dispatch
pattern already approximates, so artifact logic has a single seam. Then thin
adapters: Trinkets on Fabric, Curios on NeoForge, each mapping the abstraction
onto their slot APIs plus the JSON slot-assignment files. Inventory-based
behavior remains the fallback so the mod is unchanged without the accessory
mods. The Accessories mod (cross-loader) is noted but not targeted — its
Curios/Trinkets compat layers mean our two adapters cover it transitively.

**First slice.** Abstraction refactor + Trinkets adapter for Unstable Pocket
Watch and Chrono Aegis on Fabric latest; Curios adapter as the second PR.

**Risks.** Double-activation bugs (item active in slot *and* inventory) — the
abstraction must own "is equipped" resolution in one place. Behavior parity
across the two artifact items must be gametested.

### P1-5. Map-mod friendliness (Xaero's / JourneyMap)

**Direction.** QA-first, code-later. Run a manual matrix (Xaero's Minimap +
World Map, JourneyMap; Fabric + NeoForge on 1.21.1 and latest) checking
dimension registration, biome colors, cave/surface mode, and portal waypoint
behavior. Fix what's broken (most likely biome color fallbacks); document the
verified list. Structure-waypoint API integration is explicitly deferred.

**First slice.** The QA matrix + a `docs/` compatibility note + fixes if any.

**Risks.** None structural; time-boxed manual work.

### P1-6. Performance/shader mod compat audit (Sodium / Iris / Embeddium)

**Direction.** Manual visual smoke checklist against the known-risk render
surfaces: portal VFX, `RenderType.eyes()` emissive layers, tinted foliage and
the grass edge-gradient (`ColorProvider` / tintindex paths), cutout render
types on plants/stalactites, and the boss GUI overlays. Matrix: Sodium+Iris on
Fabric (1.21.1 + latest), Embeddium/Oculus equivalents on NeoForge 1.21.1.
Findings become individual fix PRs; the checklist becomes a repeatable
pre-release step and a "tested with" section in README.

**First slice.** Write the checklist doc + run it once on Fabric latest;
file findings as issues.

**Risks.** Fixes may require render-path changes on old versions where the
APIs differ (the 1.21.9+ `submit()` migration is a known example) — scope
fixes per-era rather than forcing one implementation.

---

## P2. Modpack-author readiness (existing initiative)

### P2-A. Config expansion (continuing)

**Direction.** Keep the shipped pattern (hand-edited TOML, per-key fallback,
restart-required) and extend in this order: **boss HP/damage multipliers**
(read at attribute-application time; also the base P3-5 needs), **Time
Distortion strength/scope** (gate the existing effect application), **portal
behavior** (one-way enforcement, re-ignition), **per-structure/per-biome
enable toggles** last — those interact with worldgen registration timing and
are the only genuinely hard slice.

**First slice.** Boss multipliers — highest pack-author demand, mechanically
simple, and P3-5 stacks on it.

### P2-C. Scripting events

**Direction.** Two layers, cheapest first:

1. **No-code consumers now:** expose boss defeat, portal transit, and
   Chronicle unlock as custom **advancement criteria** (+ stats where natural).
   FTB Quests and datapacks consume advancements directly — this covers the
   dominant questpack workflow with no API commitment.
2. **Code consumers later:** an internal event bus wrapping Architectury
   events, published under `com.chronodawn.api.event` when P7 firms up;
   KubeJS bindings as a separate optional module only after the internal bus
   has soaked.

**First slice.** The three advancement criteria + documentation of their IDs
in the modpack-integration guide.

**Risks.** Criteria names become a public contract the moment a questpack uses
them — name them deliberately and never rename (same policy as lang keys,
P6-2).

### P2-B. Tag/datapack externalization

**Direction.** Merged with P1-1 execution: the tag matrix is designed once.
Recipe-ingredient tag swaps (hardcoded item → tag lookups) follow as small
mechanical PRs after the tag set exists.

---

## P3. Trust and delivery infrastructure

### P3-1. Local verification coverage (CI deferred)

**Decision (2026-07-05).** CI and contribution-oriented repo infrastructure
are deprioritized: this is a solo project with no external contribution flow
today, and operating CI has real cost. The prerequisite investment is
**broad-coverage tests that run locally** — once those exist, CI (if ever
revisited) merely orchestrates existing Gradle tasks. `.gitmodules` already
points at GitHub, so enabling Actions later stays cheap.

**Direction.** Convert the silent-failure classes this repo has repeatedly
hit into local build/test failures:

- `validateResources` extensions: tag entries → registered IDs (needed by
  P1-1 anyway), recipe results/ingredients → registered IDs, advancement
  icon/criteria references, loot-table item references, `sounds.json` ↔
  `.ogg` file parity, subtitle-key coverage per SoundEvent (feeds P4-5).
- GameTest additions asserting **runtime registration outcomes** per version:
  the recipe manager actually contains every shipped recipe (catches the
  format-era silent drops), advancements load, configured/placed features
  resolve — the bug class that today only surfaces in manual play.
- Gameplay GameTests for the flows that guard releases: portal round trip,
  boss progression flags, artifact procs, and (with P3-4) save migrations.

**First slice.** The `validateResources` extensions — immediately useful and
a dependency of the P1-1 tag work.

**Risks.** GameTest runtime cost across versions — keep runtime assertions in
the shared gametest sources so they multiply across versions for free.

### P3-2. Release automation

**Direction.** `workflow_dispatch`-triggered (manual button, not tag-push —
keeps the human in control, matching current working style): runs the existing
`release` pipeline, then `releaseModrinth` / `releaseCurseForge` with token
secrets, then creates a GitHub Release with the changelog section extracted
from `CHANGELOG.md`. The Gradle release tasks already exist; this is
orchestration only. Independent of the P3-1 CI deferral: it automates the
maintainer's own release toil, not a contribution flow — but it is still
optional infrastructure, so it waits until manual releases actually hurt
(e.g., when day-N MC-version updates become time-critical).

**First slice.** Workflow that builds + collects JARs and drafts the GitHub
Release; platform uploads added once the dry run is trusted.

**Risks.** Secrets handling; a failed half-published release — mitigate by
making platform-upload steps idempotent/skippable and running them last.

### P3-3. Bug-report intake (templates minimized)

**Direction.** One YAML issue form only — bug report (MC version, loader, mod
version, log, other-mods field) — because it serves **players reporting
problems**, not contributors, and structured reports cut triage cost even at
low volume. PR templates, feature-request/compat forms, and label taxonomy
are deferred until external contributions actually appear (same rationale as
the P3-1 CI deferral).

### P3-4. Save-compatibility policy + migration harness

**Direction.** Policy first (S): a `docs/` page mapping SemVer to save
guarantees — saves survive all minor/patch updates; breaking changes only at
major versions, with a written migration path; the v0.3.0 break is
grandfathered as the lesson. Harness second (M): add an explicit schema
version int to ChronoDawn's persistent SavedData (boss progress, portal
state), with an ordered chain of on-load migration steps and a GameTest per
migration. No DataFixerUpper — our data surface is small enough that a bespoke
versioned-migration chain is simpler and cross-loader.

**First slice.** The policy doc + version-stamping the existing SavedData
(a no-op migration 0→1 proves the chain).

### P3-5. Multiplayer boss scaling

**Direction.** Opt-in via config (`[bosses] scale_with_players = false`
default, to avoid changing existing servers silently). On boss engage,
snapshot the count of players within the arena radius and apply
HP/damage multipliers on top of the P2-A config multipliers. No mid-fight
rescaling in the first pass — join-after-engage players fight the snapshot.

**First slice.** One boss (Chronos Warden) as the pattern, then roll out.

**Risks.** "Engage" needs a consistent definition across six bosses with
different AI; keep it as "first sets a target / takes damage".

### P3-6. Performance budget

**Direction.** A repeatable manual protocol, not automation: a reference test
world + spark profiling checklist (dimension idle tick cost, per-boss fight
cost, portal-check overhead, worldgen chunk times), run before each minor
release and after any entity/worldgen PR. Budgets get set from the first
baseline run rather than invented up front. Findings feed normal fix PRs; the
existing performance/thread-safety audit skill drives deeper passes.

**First slice.** Protocol doc + first baseline numbers recorded in-repo.

---

## P4. Onboarding and first-session experience

### P4-1. Pre-dimension guidance

**Direction.** A "torn first page" Chronicle variant item granted on the first
of: picking up any ChronoDawn item, or entering an Ancient Ruins bounding box
(location-based advancement trigger → existing PlayerEventHandler grant path).
It teaches exactly one thing — how to build and light the portal — and
upgrades into the full Chronicle on dimension entry (existing behavior
unchanged).

**First slice.** The grant triggers + a single-page variant reusing the
existing Chronicle UI.

### P4-2. Ancient Ruins locator

**Direction.** A craftable consumable ("Temporal Sextant" or similar) using
`findNearestMapStructure`, eye-of-ender-style directional cue, recipe from
early overworld-obtainable mod materials. Config keys for enable + search
radius. This compensates the widened 56-chunk spacing without reverting it.

**Risks.** `findNearestMapStructure` is expensive — must run server-side with
a cooldown, and the radius cap is the safety valve.

### P4-3. Advancement breadcrumb audit

**Direction.** Pure JSON pass: map the current story chain, ensure an unbroken
"first mod item → craft Hourglass → find Ruins → enter dimension" sequence
with descriptions that teach the next step. One PR, both advancement eras
(1.20.1 format vs 1.21.1+ format, per the documented split).

### P4-4. Chronicle upgrades

**Direction.** Incremental UI work on the existing custom book, in value
order: (1) per-category progress indicators (n/m structures discovered — data
already exists in ChronicleData), (2) text search, (3) visual polish. Each is
its own small PR; no rewrite, the bespoke book is an identity asset.

### P4-5. Accessibility audit

**Direction.** 30 subtitle keys already exist — the work is coverage, not
creation: extend `validateTranslations` to require a `subtitles.*` key for
every registered SoundEvent, then fill gaps. Plus a manual pass on
colorblind-safety of boss/effect visual cues and tooltip clarity. Checklist
PR + fix PRs.

---

## P5. Content depth, identity, replayability

### P5-1. Crop system

**Direction.** Implement exactly per the completed design in
`specs/chrono-dawn-mod/future_features.md`, sliced as: (1) crop blocks + base
food items, (2) worldgen placement, (3) crafted foods. All the multi-version
pitfalls involved (food property renames, client-items JSON, recipe format
eras) are already documented in-repo — this is execution, not design.

### P5-2. Post–Time Tyrant endgame

**Direction.** Choose **boss-rematch altar** as the first endgame beat: a
craftable/found altar in the Master Clock that re-summons defeated bosses with
escalating modifiers and cosmetic-tier rewards. Cheapest option because it
reuses all six existing bosses and arenas; "time-rift invasion" events remain
the second beat if the altar lands well. Needs a short design doc (reward
economy, modifier set) before code — flagged as the one P5 item with real
design risk besides P5-5.

### P5-3. Rare encounters and secrets

**Direction.** Three cheap word-of-mouth vehicles: (1) 2–3 hidden rooms
retrofitted into existing large structures (NBT edits via the established
build-time mapping pipeline where possible), (2) one legendary vanity item
with a deliberately obscure acquisition path, (3) one low-frequency ambient
world event (e.g., a passing "time echo" apparition). Each independent, each
one PR.

### P5-4. Visual identity pass

**Direction.** Cheapest-first ladder: (1) data-driven biome ambience —
ambient particles, fog/water tuning per biome (JSON only, no client code);
(2) portal VFX upgrade (particles + sound, existing registration paths);
(3) **custom sky** last — no `DimensionSpecialEffects`/sky renderer exists
today, so this is new per-loader client code with real per-era churn (the
1.21.9+ submit-API split applies). The sky is the highest-value single visual
but must be its own designed project.

### P5-5. Signature-mechanic deepening

**Direction.** Design-first, prototype-gated: a short concept doc exploring
"time as a player verb" (candidate: deployable temporal bubble that slows
projectiles/mobs inside; rejected-alternatives recorded), then a prototype
behind a default-off config flag on latest version only. No commitment to
ship until the prototype is fun — this is the only item where the roadmap
accepts throwaway work.

---

## P6. Localization scale-out

### P6-1. Parity gate in the local pipeline

**Direction.** Wire `scripts/check_lang_parity.py` into the local aggregate
verification (e.g., as part of `validateTranslations` / `checkAll`) so parity
failures surface in the normal pre-commit pipeline. CI wiring only if/when
CI exists (see P3-1 deferral).

### P6-2. Key-stability policy

**Direction.** Add to CONTRIBUTING: lang keys are never renamed within a major
version; renames are listed in CHANGELOG under a "Translation impact" heading.
Optionally later: a script diffing keys against the last release tag.

### P6-3. Seed locales

**Direction.** Machine-assisted first passes for zh_cn, de_de, pt_br, driven
by the existing glossary, explicitly marked as machine-assisted in
CONTRIBUTING and in per-locale tracking issues inviting native-speaker PRs.
Quality bar: correct item/UI strings; Chronicle long-form text may lag and
fall back to en_us (verify fallback renders acceptably in the custom UI —
that's the one technical check).

---

## P7. Public API and addon platform

**Direction.** Strictly sequenced after P2-C's internal event bus has soaked.
Scope the first API to what P2-C already proved people want: events
(boss defeat, portal transit, Chronicle unlock), read-only progression
queries, and an artifact-ability registration hook (from P1-4's abstraction).
Package as `com.chronodawn.api` with its own SemVer, honoring the strict
`@ExpectPlatform` package conventions documented in-repo. Hosting: decide at
design time between JitPack (zero infra, good enough for a first artifact) and
Modrinth's maven (serves the full mod JAR); GitHub Packages is ruled out
(consumer auth friction). Ship with a reference addon (one extra artifact
item) that doubles as the API's integration test and the addon-developer
guide's worked example.

**First slice (when reached).** API module + events + JitPack availability +
reference addon repo.

---

## P8. Version-coverage strategy

### P8-1. Support policy

**Direction.** A short `docs/` policy: **latest MC version** = full feature
target; **recent versions** (the 1.21.x line) = features where cheap, fixes
always; **1.20.1** = fixes only; drops announced one minor release ahead.
Target: new MC version supported within ~2 weeks of stable release. This
codifies current practice rather than changing it — the point is that pack
authors can plan against a written promise.

### P8-2. Bring-up playbook

**Direction.** Distill the accumulated per-version-bump knowledge (currently
spread across memory entries and skills: mapping renames, resource-format
splits, render API migrations) into `docs/version-bringup-playbook.md` — an
ordered checklist from "add props file" to "checkAll green". Written once,
updated each bump; directly shortens the P8-1 two-week target.

---

## Suggested first wave (ties back to roadmap Phase 1)

Smallest-risk, highest-visibility opening sequence, in order:

1. P3-1 `validateResources` extensions (S) — also unblocks the tag work
2. P1-1 material tags + validation (S/M)
3. P1-3 Jade plugin — sets the compat-package pattern (S)
4. P1-6 shader-compat checklist + first run (S, may spawn fix PRs)
5. P2-A boss multipliers (M)
6. P1-2 JEI info pages, 2 versions (M)
7. P3-3 bug-report issue form (hours; anytime, independent)

Each of items 1–6 still gets a brief design spec under `docs/superpowers/`
before implementation, per convention.
