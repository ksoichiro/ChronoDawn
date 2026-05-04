# Ancient Ruins Spawn Rate Tuning — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce Ancient Ruins generation density and re-theme the eligible
biomes so the structure matches the existing "relatively rare, deliberately
explored" Chronicle copy, while guaranteeing that no seed forces
multi-thousand-block searches.

**Architecture:** JSON-only change in `common/shared/`. Two worldgen JSONs
(structure_set + biome tag) drive the gameplay change; three documentation
files (Chronicle entry, spec.md SC-001, CHANGELOG) keep external descriptions
in sync. No Java code, no per-version overrides, no new resources.

**Tech Stack:** Minecraft data-pack JSON (`worldgen/structure_set`,
`tags/worldgen/biome`), Patchouli-style Chronicle JSON, Markdown.

**Reference:** `specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md`

**Per-commit gate:** Per the project's `CLAUDE.md` ("Require user instruction
before … git commit"), each commit step waits for explicit user approval
before running. Each task ends with a checkpoint where you present the diff
and ask the user to confirm the commit.

---

## File map

| File | Action | Purpose |
| --- | --- | --- |
| `specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md` | already created (commit it) | Design record for reviewers |
| `specs/chrono-dawn-mod/ancient-ruins-spawn-rate-plan.md` | already created (this file) | Implementation plan |
| `common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json` | modify | Loosen `spacing`/`separation` |
| `common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_ancient_ruins.json` | modify | Narrow biome tag with safety net |
| `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json` | modify | Re-theme page 4 location hint |
| `specs/chrono-dawn-mod/spec.md` | modify | Relax SC-001 from 10 min to 30 min |
| `CHANGELOG.md` | modify | Add Unreleased "Changed" entry |

---

## Task 1: Commit the design document and plan

**Files:**
- New: `specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md` (already on disk)
- New: `specs/chrono-dawn-mod/ancient-ruins-spawn-rate-plan.md` (already on disk)

- [ ] **Step 1: Verify both docs are on disk and unstaged**

Run:
```bash
git status --short specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md specs/chrono-dawn-mod/ancient-ruins-spawn-rate-plan.md
```

Expected output:
```
?? specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md
?? specs/chrono-dawn-mod/ancient-ruins-spawn-rate-plan.md
```

- [ ] **Step 2: Stage and ask user before commit**

```bash
git add specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md specs/chrono-dawn-mod/ancient-ruins-spawn-rate-plan.md
git diff --cached --stat
```

Then **stop and ask the user** for permission to commit. Suggested message:

```
docs(spec): add Ancient Ruins spawn rate tuning design + plan

Records the design rationale and implementation plan for reducing Ancient
Ruins generation density and narrowing the biome tag while preserving
guaranteed discovery on every seed.
```

- [ ] **Step 3: Run the commit (only after user approval)**

```bash
git commit -m "docs(spec): add Ancient Ruins spawn rate tuning design + plan" -m "Records the design rationale and implementation plan for reducing Ancient Ruins generation density and narrowing the biome tag while preserving guaranteed discovery on every seed."
```

---

## Task 2: Loosen `random_spread` placement and narrow the biome tag

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json`
- Modify: `common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_ancient_ruins.json`

These two files form one coherent gameplay change and ship in one commit.

- [ ] **Step 1: Update the structure_set**

Replace the entire contents of
`common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json`
with:

```json
{
  "structures": [
    {
      "structure": "chronodawn:ancient_ruins",
      "weight": 1
    }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "salt": 20005897,
    "spacing": 56,
    "separation": 20
  }
}
```

Keep the `salt` value unchanged so existing world seeds remain
deterministic for any chunks they have already generated.

- [ ] **Step 2: Update the biome tag**

Replace the entire contents of
`common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_ancient_ruins.json`
with:

```json
{
  "replace": false,
  "values": [
    "#minecraft:is_taiga",
    "minecraft:dark_forest"
  ]
}
```

`#minecraft:is_taiga` resolves to `taiga`, `snowy_taiga`,
`old_growth_pine_taiga`, and `old_growth_spruce_taiga`. Regular `taiga` is
the common-biome safety net that prevents 5000-block worst cases.

- [ ] **Step 3: Run resource validation**

```bash
./gradlew validateResources
```

Expected: `BUILD SUCCESSFUL`. The task validates JSON syntax and cross-references
(structure → structure_set, biome tag membership).

If validation fails, do NOT proceed — fix the JSON and re-run.

- [ ] **Step 4: Inspect the diff**

```bash
git diff -- common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_ancient_ruins.json
```

Expected: spacing 24→56, separation 8→20, biome tag entries swapped to
`is_taiga` + `dark_forest`. No other lines changed.

- [ ] **Step 5: Stage, ask user, then commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json common/shared/src/main/resources/data/chronodawn/tags/worldgen/biome/has_ancient_ruins.json
git diff --cached --stat
```

Pause for user approval. Suggested commit message:

```
tune(worldgen): reduce ancient_ruins spawn density and re-theme biomes

- structure_set: spacing 24→56, separation 8→20 (still random_spread)
- biome tag: replace #is_forest + #is_taiga with #is_taiga + dark_forest

Brings Ancient Ruins density into line with the Chronicle entry's "rare,
deliberate exploration" framing. Keeping #is_taiga (which contains common
taiga) prevents woodland-mansion-style 5000-block worst cases. See
specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md.
```

- [ ] **Step 6: Run the commit (only after user approval)**

```bash
git commit -m "tune(worldgen): reduce ancient_ruins spawn density and re-theme biomes" -m "- structure_set: spacing 24->56, separation 8->20 (still random_spread)
- biome tag: replace #is_forest + #is_taiga with #is_taiga + dark_forest

Brings Ancient Ruins density into line with the Chronicle entry's 'rare,
deliberate exploration' framing. Keeping #is_taiga (which contains common
taiga) prevents woodland-mansion-style 5000-block worst cases. See
specs/chrono-dawn-mod/ancient-ruins-spawn-rate-design.md."
```

---

## Task 3: Update the Chronicle entry to match the new biomes

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json`

The current entry's page 4 says ruins spawn in "plains, forests, deserts,
and more". That was already wrong before this change and is more wrong
after it. Rewrite the location hint to match the new biome list, in both
English and Japanese.

- [ ] **Step 1: Read the current entry**

```bash
git show HEAD:common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json
```

Confirm the current page 4 (`pages[3]`) starts with "How to Find Ancient
Ruins" and contains the "plains, forests, deserts, and more" line.

- [ ] **Step 2: Replace page 4 in the JSON**

In `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json`,
replace the contents of `pages[3].text.en_us` with:

```
How to Find Ancient Ruins:

1. Search old taigas and dark forests — the kind of woods where time itself seems to slow

2. Look for distinctive weathered stone structures rising from the ground

3. Ruins spawn on the surface, making them visible from a distance

4. Use /locate structure chronodawn:ancient_ruins if cheats are enabled

Recommendation: Bring a map and explore deliberately — ruins are rare, but a determined journey will always find one.
```

And replace `pages[3].text.ja_jp` with:

```
古代遺跡の見つけ方:

1. 古いタイガと闇の森を探索 — 時の流れがゆっくりに感じられるような森が狙い目

2. 地面から立ち上がる特徴的な風化した石造構造物を探す

3. 遺跡は地表に生成されるため、遠くからでも見える

4. チートが有効な場合は /locate structure chronodawn:ancient_ruins を使用

推奨: 地図を持って計画的に探索しましょう。遺跡は希少ですが、根気強い旅であれば必ず見つかります。
```

Leave pages 1, 2, and 3 untouched. Page 2 already says "ruins are
relatively rare, so exploration is key" and now matches reality.

- [ ] **Step 3: Run resource validation**

```bash
./gradlew validateResources
```

Expected: `BUILD SUCCESSFUL`. Confirms the Chronicle JSON is still
syntactically valid.

- [ ] **Step 4: Inspect the diff**

```bash
git diff -- common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json
```

Expected: only `pages[3]` (page 4) changed in both `en_us` and `ja_jp`.
No structural changes (no removed pages, no added pages, no key changes).

- [ ] **Step 5: Stage, ask user, then commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/chronicle/entries/basics/ancient_ruins.json
git diff --cached
```

Pause for user approval. Suggested commit message:

```
docs(chronicle): update Ancient Ruins entry to match new biome targeting

Page 4 of the in-game Chronicle entry now points players at old taigas
and dark forests instead of the previous "plains, forests, deserts, and
more" wording (which never matched the implementation). Both en_us and
ja_jp updated.
```

- [ ] **Step 6: Run the commit (only after user approval)**

```bash
git commit -m "docs(chronicle): update Ancient Ruins entry to match new biome targeting" -m "Page 4 of the in-game Chronicle entry now points players at old taigas and dark forests instead of the previous 'plains, forests, deserts, and more' wording (which never matched the implementation). Both en_us and ja_jp updated."
```

---

## Task 4: Relax SC-001 in the spec

**Files:**
- Modify: `specs/chrono-dawn-mod/spec.md`

- [ ] **Step 1: Locate SC-001**

```bash
grep -n "^- \*\*SC-001" specs/chrono-dawn-mod/spec.md
```

Expected: a single match for the SC-001 line.

- [ ] **Step 2: Edit SC-001**

In `specs/chrono-dawn-mod/spec.md`, replace the SC-001 bullet:

Old:
```
- **SC-001**: Players can discover Ancient Ruins in the Overworld and obtain materials and blueprints necessary for entering Chrono Dawn within 10 minutes
```

New:
```
- **SC-001**: Players can discover Ancient Ruins in the Overworld and obtain materials and blueprints necessary for entering Chrono Dawn within 30 minutes of deliberate exploration
```

Leave SC-002, SC-003, SC-004, etc. untouched.

- [ ] **Step 3: Inspect the diff**

```bash
git diff -- specs/chrono-dawn-mod/spec.md
```

Expected: a single line change on the SC-001 bullet. No other edits.

- [ ] **Step 4: Stage, ask user, then commit**

```bash
git add specs/chrono-dawn-mod/spec.md
git diff --cached
```

Pause for user approval. Suggested commit message:

```
docs(spec): relax SC-001 time-to-discover from 10 to 30 minutes

Reflects the deliberate-exploration discovery loop established by the
Ancient Ruins spawn rate tuning (see ancient-ruins-spawn-rate-design.md).
```

- [ ] **Step 5: Run the commit (only after user approval)**

```bash
git commit -m "docs(spec): relax SC-001 time-to-discover from 10 to 30 minutes" -m "Reflects the deliberate-exploration discovery loop established by the Ancient Ruins spawn rate tuning (see ancient-ruins-spawn-rate-design.md)."
```

---

## Task 5: Add the CHANGELOG entry

**Files:**
- Modify: `CHANGELOG.md`

The current `[Unreleased]` section has only an `### Added` block. Insert a
new `### Changed` block immediately before the `## [0.7.0] - 2026-04-22`
heading (line 32 in the current file).

- [ ] **Step 1: Confirm the insertion point**

```bash
grep -n "^## \[0.7.0\]\|^### Added\|^## \[Unreleased\]" CHANGELOG.md
```

Expected: line numbers for `## [Unreleased]`, `### Added` (under
Unreleased), and `## [0.7.0] - 2026-04-22`. Insertion point is
immediately before the `[0.7.0]` heading.

- [ ] **Step 2: Insert the Changed block**

In `CHANGELOG.md`, just before the line `## [0.7.0] - 2026-04-22`,
insert the following (preserve a blank line above and below):

```markdown
### Changed
- **Ancient Ruins spawn rate** reduced. Placement spacing increased from 24 to 56 chunks (separation 8 → 20), and the eligible biome tag narrowed from "all forests + all taigas" to taiga variants + dark forest. Existing worlds keep already-generated ruins; only newly explored chunks reflect the change.

```

The list bullet line is one logical paragraph; do not wrap it inside the
file (CHANGELOG style elsewhere uses long single-line bullets).

- [ ] **Step 3: Inspect the diff**

```bash
git diff -- CHANGELOG.md
```

Expected: a single 4-line addition (header, bullet, surrounding blank
lines) immediately before `## [0.7.0] - 2026-04-22`.

- [ ] **Step 4: Stage, ask user, then commit**

```bash
git add CHANGELOG.md
git diff --cached
```

Pause for user approval. Suggested commit message:

```
docs(changelog): record Ancient Ruins spawn rate tuning
```

- [ ] **Step 5: Run the commit (only after user approval)**

```bash
git commit -m "docs(changelog): record Ancient Ruins spawn rate tuning"
```

---

## Task 6: Final verification

**Files:** none modified.

- [ ] **Step 1: Re-run resource validation across the project**

```bash
./gradlew validateResources
```

Expected: `BUILD SUCCESSFUL`. Catches any drift introduced by the
combined changes.

- [ ] **Step 2: Confirm the commit history**

```bash
git log --oneline -n 6
```

Expected: 5 new commits on top of the previous tip:

1. `docs(changelog): record Ancient Ruins spawn rate tuning`
2. `docs(spec): relax SC-001 time-to-discover from 10 to 30 minutes`
3. `docs(chronicle): update Ancient Ruins entry to match new biome targeting`
4. `tune(worldgen): reduce ancient_ruins spawn density and re-theme biomes`
5. `docs(spec): add Ancient Ruins spawn rate tuning design + plan`

- [ ] **Step 3: Offer the user a smoke test**

The change is fully data-driven, so unit tests can't observe it. Offer
the user a manual smoke test on one Fabric and one NeoForge version of
their choice. Do not run a client unprompted.

Suggested phrasing to the user:

```
Verification is possible via a manual client smoke test:

1. Run ./gradlew runClientFabric1_21_11 (or any other version)
2. Create a fresh creative world, fly outward several thousand blocks
3. Confirm Ancient Ruins now appear only in taiga or dark forest
   biomes, and at noticeably wider intervals than before
4. Open the Chronicle to "Finding Ancient Ruins" and confirm page 4
   reads "Search old taigas and dark forests..."

Expected: the structure feels rare-but-findable, and biome targeting
matches the Chronicle hint.

Want me to run the client, or skip the smoke test for now?
```

Wait for user choice. Do not auto-run.

---

## Self-review notes

- **Spec coverage**: every change listed in
  `ancient-ruins-spawn-rate-design.md` "Files touched" maps to a task
  (Task 2 covers files 1–2, Task 3 covers file 3, Task 4 covers file 4,
  Task 5 covers file 5).
- **Type/path consistency**: every file path referenced in this plan was
  verified against the working tree before writing.
- **No version-specific overrides**: all five touched files live under
  `common/shared/` (or are non-resource docs). The
  `feedback_1_21_11_biome_overrides` memory note applies to biome
  *override* JSONs, not biome *tags* — biome tag JSONs only live in
  `common/shared/`. Confirmed by file map.
- **Existing-world impact** is documented in the design doc (only newly
  generated chunks reflect the change); the CHANGELOG entry repeats this
  caveat to set player expectations.
