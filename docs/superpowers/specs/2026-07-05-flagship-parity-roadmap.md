# Roadmap: Toward Flagship-Mod Parity

**Created**: 2026-07-05
**Status**: Active — analysis complete; first P3-1/P6-1 slices shipped 2026-07-05, other pillars not started
**Type**: Living document (umbrella strategy; sub-initiatives link out)

---

## 1. Purpose and scope

This document analyzes what Chrono Dawn needs — **in development scope only** — to
grow downloads and stand alongside flagship dimension/adventure mods
(Twilight Forest, The Aether, L_Ender's Cataclysm, Alex's Caves, The
Undergarden, Deeper and Darker). Promotion, social content, and platform-page
optimization are explicitly out of scope; this is about what the *artifact
itself* and its *engineering infrastructure* must become.

It is the umbrella above existing narrower initiatives:

- [Modpack-Author Readiness roadmap](./2026-05-09-modpack-author-readiness-roadmap.md) (active)
- `specs/chrono-dawn-mod/future_features.md` (crop system design, ready to implement)

A note on realism: flagship mods sit at 10M–100M+ downloads; Chrono Dawn was at
~52.8k as of 2026-05-07, driven mostly by a single modpack. Development work
sets the **ceiling** (whether the mod *can* be adopted at scale); distribution
dynamics (modpack inclusion, new-MC-version search spikes) set the **slope**.
The strategy below therefore prioritizes work that removes adoption blockers
for the multipliers — pack authors, server owners, translators, addon authors —
over work that only pleases existing players.

## 2. Where Chrono Dawn stands today

### Strengths (already at or above flagship level)

| Area | Evidence |
| --- | --- |
| Version × loader coverage | 12 MC versions (1.20.1–1.21.11) × Fabric + NeoForge from one codebase. Most flagship mods support 2–3 versions; this breadth is a genuine differentiator and drives version-filter discoverability. |
| Complete progression arc | Portal → 5 mid/mini bosses → Master Clock final dungeon → Time Tyrant → ultimate artifacts. Many mods this size never ship an ending. |
| In-game guidance | Custom Chronicle guidebook (auto-granted), advancement tree (story/exploration/equipment/food/mobs), recipe-unlock advancements. |
| QA harness | Unit tests + GameTest across versions, `validateResources`, `validateTranslations`, `checkAll` pipeline. |
| Modpack groundwork | `chronodawn.toml` config (Ancient Ruins + 4 ores), `docs/configuration.md`, `docs/modpack-integration.md`, pack-overrides support. |
| Release discipline | SemVer + Keep a Changelog, `release` Gradle pipeline, Modrinth/CurseForge upload tasks. |
| Translation groundwork | `CONTRIBUTING.md` translation guide, `docs/translation-glossary.md`, `scripts/check_lang_parity.py`. |
| Atmosphere | Custom BGM per biome, ambient structures, emissive mob layers, cave decoration. |

### Gaps (relative to flagship mods; all development-scope)

| Gap | Why it caps growth |
| --- | --- |
| **Zero ecosystem integrations** — no JEI/EMI/REI plugin, no Jade/WTHIT support, no Curios/Trinkets slots, no Patchouli bridge, no map-mod hooks | Players evaluate a mod inside a 200-mod pack. A mod that doesn't show up in the tools they already use feels foreign and gets voted out of packs. |
| **No `c:` common tags** — items/blocks not exposed under conventional tags | Cross-mod recipes can't consume Chrono Dawn materials; pack authors can't unify ores/ingots; other mods can't target Chrono Dawn biomes/structures via tags. |
| **No CI/CD** — `.github/` does not exist: no PR build/test, no automated release, no issue/PR templates | Release cadence and crash-fix turnaround are trust signals pack authors check. Manual releases across 22 JARs make hotfixes expensive, which slows the single most download-relevant event: day-N support of a new MC version. |
| **No public API / addon story** — no published maven artifact, no stable events | Flagship mods grow ecosystems (addons, compat mods, questpacks) that market the base mod for free. Nothing today lets a third party build on Chrono Dawn without reflection. |
| **2 locales** (en_us, ja_jp) | Flagships ship 15–40 locales via community PRs. The infra exists; the locale count doesn't yet. |
| **Save-compat policy is informal** — a README warning about v0.3.0; no migration harness | Server owners and pack authors read breaking-change history as risk. A written guarantee + migration capability converts that history from liability to trust. |
| **Performance-mod compat unverified** — Sodium/Iris/Embeddium behavior not covered by any in-repo test or doc | The majority of players run Sodium or Embeddium. Custom render layers (emissive eyes, portals, tinted foliage) are exactly the surfaces that break under them. |
| **Onboarding cliff before the dimension** — Chronicle is granted only *after* entering; Ancient Ruins spacing was widened to 56 chunks | The first 30 minutes decide reviews and retention. Right now the weakest guided segment is everything before the portal. |
| **No multiplayer boss scaling** | Packs are largely played on servers; single-player-tuned bosses read as either trivial or unfair in MP. |

## 3. What flagship mods share (benchmark framework)

Six traits recur across the mods Chrono Dawn aims to join. Scored honestly:

1. **Be where players are** (versions × loaders) — ✅ already flagship-grade.
2. **Be a modpack staple** (config depth, tags, scripting hooks, docs) — 🚧 in progress via the modpack-readiness initiative.
3. **Be a good citizen of the ecosystem** (recipe viewers, tooltip mods, accessory slots, map mods, performance mods) — ❌ largest visible gap.
4. **Have an unmistakable identity** (signature mechanic + screenshot-grade visuals + content density) — 🚧 time-manipulation theme is strong and coherent; density and visual spectacle still below Alex's Caves / Cataclysm tier.
5. **Be trusted** (save stability, fast fixes, server-friendliness, visible QA) — 🚧 internal QA is strong but externally invisible (no CI badge, no policy docs, no issue workflow).
6. **Turn users into contributors** (translations, addons, datapack extension points) — 🚧 translation infra exists; addon/API surface does not.

## 4. Pillars and workstreams

### P1. Ecosystem integrations (highest-leverage new work)

Goal: Chrono Dawn feels native inside any large modpack.

| Work item | Notes | Size |
| --- | --- | --- |
| `c:` common tags for all materials (ingots/gems/ores/foods/wood sets), plus mod-owned biome/structure tags | Prerequisite for everything else in this pillar; pure datapack work but needs a per-version tag matrix (tag conventions changed across 1.20/1.21) | M |
| JEI/EMI/REI **information pages** for non-recipe mechanics: portal construction, Portal Stabilizer, boss-drop sources, artifact effects | Vanilla recipe types already render automatically; the gap is the *mechanics* JEI can't infer. EMI + JEI cover both loaders; REI via its JEI-compat layer | M |
| Jade/WTHIT plugin: crop growth stages, stalactite/coral variants, boss names, portal state | Small, extremely visible in packs | S |
| Curios (NeoForge) / Trinkets (Fabric) / Accessories support for Unstable Pocket Watch, Chrono Aegis, and future artifacts | Optional-dependency pattern via Architectury `@ExpectPlatform`; keep vanilla-inventory behavior as fallback | M |
| Map-mod friendliness: verify Xaero's/JourneyMap render the dimension correctly; expose structure markers where APIs allow | Mostly QA + small hooks | S |
| Performance/shader compat audit: Sodium, Embeddium, Iris, Oculus against portals, emissive layers, tinted foliage, custom sky | QA matrix + fixes; document results in README ("tested with…") | M |

All integrations must be **soft dependencies** (compile-time only, runtime-optional) so the base JAR stays dependency-light.

### P2. Modpack-author readiness (existing initiative — continue)

Owned by the [living roadmap](./2026-05-09-modpack-author-readiness-roadmap.md).
Sequencing there stands: config expansion (boss multipliers, Time Distortion,
portal behavior, per-structure toggles) → scripting events (boss defeated,
portal opened, Chronicle entry unlocked — the FTB Quests/KubeJS hook surface)
→ tag/datapack externalization. This umbrella document adds one input: the
`c:` tag work in P1 overlaps sub-project B and should be executed once, under
whichever initiative moves first.

### P3. Trust and delivery infrastructure

Goal: the engineering quality that already exists becomes mechanically enforced — locally first.

> **Decision (2026-07-05)**: contribution-oriented infrastructure (CI, PR
> templates) is deprioritized — solo project, no external contribution flow,
> and standing infrastructure has operating cost. The prerequisite is broad
> **local** test coverage; CI, if ever revisited, then only orchestrates
> existing Gradle tasks. See the
> [approach outlines](./2026-07-05-flagship-parity-approach-outlines.md).

| Work item | Notes | Size |
| --- | --- | --- |
| **Local verification coverage expansion**: extend `validateResources` (tag/recipe/loot/advancement/sound cross-references, subtitle coverage) and the GameTest suite (runtime recipe/advancement/feature registration assertions, portal/boss/migration flows) | The repo's history shows silent cross-version breakage is the dominant defect class; local coverage protects the 22-JAR matrix with no standing infrastructure. First slice shipped 2026-07-05 (validateData + ingredient era rule; see the data-validation design spec). | M |
| GitHub Actions CI (**deferred**) — revisit when external contributions or release cadence demand it | By then it only runs existing tasks; `.gitmodules` already points at GitHub so enabling later is cheap | M |
| Automated release workflow (**deferred**): manual-trigger `release` pipeline → Modrinth + CurseForge publish + GitHub Release with changelog extraction | Automates the maintainer's own toil, not contribution flow — adopt when manual releases across 22 JARs actually hurt (e.g., day-N MC-version updates) | M |
| Bug-report issue template (player crash intake); other templates deferred until external contributions exist | Serves players, not contributors; structured reports cut triage cost even at low volume | S |
| **Save-compatibility policy** document + migration harness (versioned save-data with explicit upgrade paths; never repeat the v0.3.0 hard break) | Policy doc is S; a minimal migration framework for boss-progress/portal data is M | S+M |
| Multiplayer boss scaling (HP/damage per nearby player count, config-gated) | Rides on P2's config infrastructure | M |
| Performance budget: tick-cost profiling of entity handlers/portals under spark; document and fix hotspots | An `audit-performance-thread-safety` skill already exists in-repo to drive this | M |
| **Fix the Fabric 1.21.5+ GameTest blind spot** (found 2026-07-25): `fabric/<v>/src/gametest/java/com/chronodawn/gametest/ChronoDawnGameTests.java` for 1.21.5–1.21.11 overrides `fabric/base`'s same-named class and drops its `RegistryDrivenTestGenerator.generateAllTests()` call (`fabric/base/src/gametest/…/ChronoDawnGameTests.java:29`). Every registry-driven GameTest — boss fights, registry consistency, translations, advancements, structures, portals, mob behavior — silently does not run on Fabric for 7 of 11 versions. NeoForge is unaffected: it wires `generateAllTests()` in every version. The gap is stark in one `gameTestAll` run on 1.21.11: NeoForge reports "All 1090 required tests passed", Fabric reports "All 10 required tests passed". | Expect a wave of genuine failures the first time the suppressed tests actually run — budget for triage, not just the wiring change. This is the same silent-coverage-loss class the row above exists to prevent. | M |
| ~~**Fix `structure_contains_9_smooth_stone_old_sundial`**~~ — **resolved 2026-07-27.** Root cause was a stale test expectation, not an era-specific defect: `587b2424` added `"minecraft:smooth_stone": "chronodawn:smooth_temporal_stone"` to `scripts/nbt_block_mappings.json`, which the `replaceNbtBlocks` build task applies to `old_sundial.nbt` (deliberate — see the Smooth Temporal Stone design spec), but the GameTest spec kept requiring the pre-replacement vanilla block. All five affected versions failed for this one reason; 1.20.1 and 1.21.1 were re-run directly and both reported the identical `contains 0 smooth_stone, expected at least 9`. 1.20.1 was never a separate cause, despite having no `.nbt` files of its own in source: they are generated from the same shared source via `replaceNbtBlocks` → `convertNbtStructures`. Fixed by expecting `smooth_temporal_stone` and adding a `structure_no_unreplaced_vanilla_blocks` guard so a future mapping addition fails loudly instead of silently invalidating a spec. | The drift slipped through because that design spec's Verification section listed `validateResources` / `buildAll` / `testAll` but not `gameTestAll`. Any change to `nbt_block_mappings.json` must run `gameTestAll`. | S |

### P4. Onboarding and first-session experience

Goal: a player who installs the mod alone (no pack, no wiki) reaches the dimension within one session and understands what to do next.

| Work item | Notes | Size |
| --- | --- | --- |
| Pre-dimension guidance: grant the Chronicle (or a "torn first page" variant) when the player first sees an Ancient Ruin or picks up the first mod item — not only after entering the dimension | Closes the weakest guided segment | S |
| Ancient Ruins discoverability aid: a craftable/lootable locator (eye-of-ender-style) or a vanilla-map-integrated hint, balancing the widened 56-chunk spacing | Config-gated | M |
| Advancement polish: ensure a continuous breadcrumb chain from "obtain first Time material" to "enter the dimension", with descriptions that teach | Audit + JSON work | S |
| Chronicle upgrades: search, progress indicators (n/m structures found), visual polish | The custom UI is an asset — flagships mostly rely on Patchouli; a polished bespoke book is identity | M |
| Subtitle/accessibility audit: subtitle keys for all custom sounds, tooltip clarity, colorblind-safe boss/effect cues | Also feeds translation completeness | S |

### P5. Content depth, identity, and replayability

Goal: enough density that exploration keeps paying off, and visuals distinctive enough that screenshots are self-explanatory. (Content is the slowest download lever per hour invested, but it defines the ceiling of word-of-mouth.)

| Work item | Notes | Size |
| --- | --- | --- |
| Crop system (Temporal Root, Chrono Melon, Timeless Mushroom + 11 food items) | Design already complete in `future_features.md`; cheapest content win available | M |
| Post–Time Tyrant endgame: repeatable challenge (boss rematch altar, time-rift invasions, or roguelite micro-dungeon) | Flagship mods retain players after "the end"; retained players seed packs and reviews | L |
| Rare encounters & secrets: low-frequency world events, hidden rooms in existing structures, one legendary vanity item | High word-of-mouth density per dev-hour | M |
| Visual identity pass: custom sky/celestial bodies for perpetual twilight, ambient particles, portal VFX | The dimension's look *is* the marketing asset; pure dev work | M–L |
| Signature-mechanic deepening: make time manipulation something the *player does* (localized time-stop zones, rewind mechanics beyond the Mail's proc) — not only something that happens to mobs | This is what "famous for X" means; design-first, prototype behind a config flag | L |

### P6. Localization scale-out

Goal: locale count stops being a visible gap.

| Work item | Notes | Size |
| --- | --- | --- |
| Parity gate in the local pipeline: wire `check_lang_parity.py` into `validateTranslations` / `checkAll` | Already shipped: validateLangParity task runs in checkAll (discovered 2026-07-05). | S |
| Key-stability policy: never rename lang keys within a major version (translators' work survives updates); document in CONTRIBUTING | Policy only | S |
| Seed 2–3 high-population locales (zh_cn, de_de, pt_br) at "good enough" quality to signal that PRs improving them are welcome | Machine-assisted first pass is acceptable as scaffolding if clearly marked | M |

### P7. Public API and addon platform (long horizon)

Goal: third parties can build on Chrono Dawn without reflection, making the mod a platform.

| Work item | Notes | Size |
| --- | --- | --- |
| Define `com.chronodawn.api` surface: events (boss defeat, portal transit, Chronicle unlock), registries lookup, artifact hooks | Must follow the strict `@ExpectPlatform` package conventions already learned in this codebase | M |
| Publish API artifact to a public maven (maven-publish plugin is already applied; needs repository + versioning policy) | SemVer on the API independent of mod version | S |
| Addon developer guide + a tiny reference addon (e.g., one extra artifact) proving the surface works | The reference addon doubles as an integration test | M |

Sequence strictly **after** P2's scripting events stabilize — a published API is
a promise, and breaking it costs the trust this roadmap is trying to build.

### P8. Version-coverage strategy (formalize what already works)

| Work item | Notes | Size |
| --- | --- | --- |
| Written support policy: which MC versions get new features vs. fixes-only vs. dropped; target lag for new snapshots/releases | Today's breadth is excellent but implicit; a policy makes it a promise pack authors can plan around | S |
| New-version bring-up playbook: distill the accumulated per-version pitfalls (already captured as memory/skills) into a repo doc so bring-up is a checklist, not archaeology | Directly shortens day-N support, the biggest organic spike | S |

## 5. Sequencing

**Phase 1 — "Verified locally + pack-native" (next ~2–3 months)**
P3 local verification coverage first (the `validateResources` extensions are
a dependency of the tag work), then P1 `c:` tags → Jade → JEI/EMI info pages
→ Curios/Trinkets, interleaved with P2 config expansion already in flight.
Include the P1 Sodium/Iris audit early — if something is broken there, it is
silently costing installs today. The bug-report issue form ships whenever
convenient; CI and other repo templates stay deferred per the P3 decision.

Rationale: these are the items a pack author checks in a 15-minute evaluation.
They are also mostly S/M-sized with no design risk.

**Phase 2 — "Hooks + onboarding" (following ~3 months)**
P2 scripting events (the questpack enabler), P4 onboarding chain, P3 boss
scaling + save-compat policy, P6 localization gates. Ship the crop system (P5)
here as the content beat accompanying an infrastructure-heavy stretch — release
notes that contain only plumbing don't travel.

**Phase 3 — "Platform + spectacle" (longer horizon)**
P7 API + reference addon, P5 endgame/replayability and the signature-mechanic
deepening, P8 policies formalized. By this point the events surface from
Phase 2 has had real questpack usage, which is the input a stable API needs.

Content (P5) is a **parallel track** throughout — roughly one content beat per
release so every changelog has a player-facing headline, with the large design
efforts (endgame, signature mechanic) landing in Phase 3.

## 6. Signals to watch (development-observable)

- Number of public modpacks including Chrono Dawn (Modrinth/CurseForge pack search) — the primary multiplier this roadmap targets.
- Lag (days) between a new MC release and a Chrono Dawn build supporting it.
- Crash/issue reports per release, and median time-to-fix.
- Locale count and parity-check pass rate.
- Third-party artifacts referencing the API (post-Phase 3).
- Download mix shift: single-pack-dominated → multi-pack + direct installs.

## 7. Relationship to existing documents

- **This document** — umbrella strategy and prioritization across all pillars.
- **[Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md)** — owns P2 execution detail; continues unchanged.
- **`specs/chrono-dawn-mod/future_features.md`** — owns crop-system design (first P5 beat).
- Per-work-item designs should follow the existing pattern: one design spec + plan under `docs/superpowers/` per PR-sized slice.

## 8. Out of scope

- Marketing, promotion, social content, platform-page/SEO work.
- Paid services (Crowdin/Weblate hosting decisions are community-ops, not code; the repo-side enablers are in P6).
- Telemetry of any kind — deliberately excluded; player trust outweighs the data.
- Backporting below 1.20.1.

## How to update this document

1. When a pillar's first slice ships, add a status note under that pillar with links to its design/plan docs.
2. When priorities shift (e.g., a pack author requests something specific), record the trigger here — sequencing changes should leave a paper trail.
3. Revisit the phase boundaries quarterly; this document describes intent, not commitment.
