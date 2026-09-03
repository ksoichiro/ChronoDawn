# c:tools/*, c:armors/*, and Shield Tag Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish Fabric/NeoForge conventional (`c:`) tags for every Chrono
Dawn tool, weapon, armor, and shield item, so pack recipes and cross-mod
systems can select Chrono Dawn gear by functional category instead of exact
item ID.

**Architecture:** Add static JSON tag files under each version era's `data/c/tags/`
root (same mechanism the existing material tags already use), extend the
existing `scripts/conventional_tag_rules.json` rule set that
`validateConventionalTags` consumes so missing coverage fails the build, and
document the new tags in `docs/modpack-integration.md`.

**Tech Stack:** Static Minecraft data-pack JSON tag files; Groovy-based Gradle
validation task (`gradle/chronodawn-validation.gradle`); no Java code changes.

**Spec:** [docs/superpowers/specs/2026-09-03-tools-armors-shield-tags-design.md](../specs/2026-09-03-tools-armors-shield-tags-design.md)

## Global Constraints

- Every v2 tag uses the **singular** canonical form (`tools/melee_weapon`,
  not `tools/melee_weapons`) — the plural forms are deprecated Fabric
  aliases.
- No `c:armors/humanoid` or other 1.21.11-only armor subtags in this plan
  (deferred per spec §5). Only bare `c:armors`.
- No new tag category is invented outside what Fabric API / NeoForge
  actually ship (no `c:tools/mining_tools` custom umbrella for
  shovels/hoes — they simply get no v2 tag).
- Every new v2 tag file uses `"replace": false` and a flat `values` array,
  matching the existing `c:` tag files' shape exactly.
- No loader-specific files: NeoForge reads the same `c:` namespace as
  Fabric, same as every existing `c:` tag in this repo.

---

## File Structure

New files:

- `common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/melee_weapon.json`
- `common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/mining_tool.json`
- `common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/shield.json`
- `common/shared-1.21.1+/src/main/resources/data/c/tags/item/armors.json`
- `common/1.20.1/src/main/resources/data/c/tags/items/swords.json`
- `common/1.20.1/src/main/resources/data/c/tags/items/pickaxes.json`
- `common/1.20.1/src/main/resources/data/c/tags/items/axes.json`
- `common/1.20.1/src/main/resources/data/c/tags/items/shovels.json`
- `common/1.20.1/src/main/resources/data/c/tags/items/hoes.json`
- `common/1.20.1/src/main/resources/data/c/tags/items/shields.json`

Modified files:

- `scripts/conventional_tag_rules.json` — new `v2_allowed_tags` / `v1_allowed_tags`
  entries, new coverage `rules`, new `translation_exempt_tags` entries.
- `docs/modpack-integration.md` — new tag documentation section.
- `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md` —
  mark this slice shipped.
- `CHANGELOG.md` — one `[Unreleased]` → `Added` entry.

---

## Task 1: 1.20.1 (v1) tool and shield tags

**Files:**
- Create: `common/1.20.1/src/main/resources/data/c/tags/items/swords.json`
- Create: `common/1.20.1/src/main/resources/data/c/tags/items/pickaxes.json`
- Create: `common/1.20.1/src/main/resources/data/c/tags/items/axes.json`
- Create: `common/1.20.1/src/main/resources/data/c/tags/items/shovels.json`
- Create: `common/1.20.1/src/main/resources/data/c/tags/items/hoes.json`
- Create: `common/1.20.1/src/main/resources/data/c/tags/items/shields.json`
- Modify: `scripts/conventional_tag_rules.json`

**Interfaces:**
- Consumes: nothing from other tasks.
- Produces: nothing later tasks depend on directly — this task is
  independently verifiable via `validateConventionalTags`.

- [ ] **Step 1: Add the v1 coverage rules (failing state)**

Open `scripts/conventional_tag_rules.json`. Find the two existing `era: "v1"`
rules (`"ore blocks join blocks/ores (v1)"` and `"ore items join items/ores (v1)"`,
near the end of the `rules` array). Immediately after the second one
(`"ore items join items/ores (v1)"`), insert:

```json
    {
      "name": "sword items join items/swords (v1)",
      "era": "v1",
      "id_source": "item",
      "ids": [
        "clockstone_sword",
        "enhanced_clockstone_sword",
        "entropy_crystal_sword",
        "chronoblade"
      ],
      "require_all": [
        "items/swords"
      ]
    },
    {
      "name": "pickaxe items join items/pickaxes (v1)",
      "era": "v1",
      "id_source": "item",
      "ids": [
        "clockstone_pickaxe",
        "enhanced_clockstone_pickaxe",
        "spatially_linked_pickaxe"
      ],
      "require_all": [
        "items/pickaxes"
      ]
    },
    {
      "name": "axe items join items/axes (v1)",
      "era": "v1",
      "id_source": "item",
      "ids": [
        "clockstone_axe",
        "enhanced_clockstone_axe"
      ],
      "require_all": [
        "items/axes"
      ]
    },
    {
      "name": "shovel items join items/shovels (v1)",
      "era": "v1",
      "id_source": "item",
      "ids": [
        "clockstone_shovel",
        "enhanced_clockstone_shovel"
      ],
      "require_all": [
        "items/shovels"
      ]
    },
    {
      "name": "hoe items join items/hoes (v1)",
      "era": "v1",
      "id_source": "item",
      "ids": [
        "clockstone_hoe",
        "enhanced_clockstone_hoe"
      ],
      "require_all": [
        "items/hoes"
      ]
    },
    {
      "name": "shield items join items/shields (v1)",
      "era": "v1",
      "id_source": "item",
      "ids": [
        "clockstone_shield",
        "enhanced_clockstone_shield",
        "entropy_crystal_shield"
      ],
      "require_all": [
        "items/shields"
      ]
    }
```

Also add the six new tag names to `v1_allowed_tags` (append after
`"items/sandstone_stairs"`):

```json
    "items/swords",
    "items/pickaxes",
    "items/axes",
    "items/shovels",
    "items/hoes",
    "items/shields"
```

Save the file. Verify it is still valid JSON:

```bash
python3 -c "import json; json.load(open('scripts/conventional_tag_rules.json'))"
```
Expected: no output (no exception).

- [ ] **Step 2: Run validateConventionalTags to see it fail**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateConventionalTags
```
Expected: **FAIL**, with errors like `common/1.20.1/.../data/c/tags:
'chronodawn:clockstone_sword' missing from c:swords (rule: sword items join
items/swords (v1))` — one per rule added in Step 1, since none of the tag
files exist yet.

- [ ] **Step 3: Create the six v1 tag files**

`common/1.20.1/src/main/resources/data/c/tags/items/swords.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_sword",
    "chronodawn:enhanced_clockstone_sword",
    "chronodawn:entropy_crystal_sword",
    "chronodawn:chronoblade"
  ]
}
```

`common/1.20.1/src/main/resources/data/c/tags/items/pickaxes.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_pickaxe",
    "chronodawn:enhanced_clockstone_pickaxe",
    "chronodawn:spatially_linked_pickaxe"
  ]
}
```

`common/1.20.1/src/main/resources/data/c/tags/items/axes.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_axe",
    "chronodawn:enhanced_clockstone_axe"
  ]
}
```

`common/1.20.1/src/main/resources/data/c/tags/items/shovels.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_shovel",
    "chronodawn:enhanced_clockstone_shovel"
  ]
}
```

`common/1.20.1/src/main/resources/data/c/tags/items/hoes.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_hoe",
    "chronodawn:enhanced_clockstone_hoe"
  ]
}
```

`common/1.20.1/src/main/resources/data/c/tags/items/shields.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_shield",
    "chronodawn:enhanced_clockstone_shield",
    "chronodawn:entropy_crystal_shield"
  ]
}
```

- [ ] **Step 4: Run validateConventionalTags to see it pass**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateConventionalTags
```
Expected: **PASS** (task succeeds, no errors printed).

- [ ] **Step 5: Commit**

```bash
git add common/1.20.1/src/main/resources/data/c/tags/items/swords.json \
        common/1.20.1/src/main/resources/data/c/tags/items/pickaxes.json \
        common/1.20.1/src/main/resources/data/c/tags/items/axes.json \
        common/1.20.1/src/main/resources/data/c/tags/items/shovels.json \
        common/1.20.1/src/main/resources/data/c/tags/items/hoes.json \
        common/1.20.1/src/main/resources/data/c/tags/items/shields.json \
        scripts/conventional_tag_rules.json
git commit -m "feat(tags): add c: tool and shield tags for 1.20.1"
```

---

## Task 2: 1.21.1+ tool and shield tags (`tools/melee_weapon`, `tools/mining_tool`, `tools/shield`)

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/melee_weapon.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/mining_tool.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/shield.json`
- Modify: `scripts/conventional_tag_rules.json`

**Interfaces:**
- Consumes: nothing from Task 1.
- Produces: nothing later tasks depend on directly.

- [ ] **Step 1: Add the v2 coverage rules (failing state)**

In `scripts/conventional_tag_rules.json`, find the last v2-era rule (the one
whose `name` ends `"...join items/seeds"` / the `chrono_melon_seeds` rule,
immediately before the two `era: "v1"` ore rules). Insert the following
three rules immediately after it (still before the `era: "v1"` rules):

```json
    {
      "name": "sword and axe items join c:tools/melee_weapon",
      "id_source": "item",
      "ids": [
        "clockstone_sword",
        "enhanced_clockstone_sword",
        "entropy_crystal_sword",
        "chronoblade",
        "clockstone_axe",
        "enhanced_clockstone_axe"
      ],
      "require_all": [
        "item/tools/melee_weapon"
      ]
    },
    {
      "name": "pickaxe items join c:tools/mining_tool",
      "id_source": "item",
      "ids": [
        "clockstone_pickaxe",
        "enhanced_clockstone_pickaxe",
        "spatially_linked_pickaxe"
      ],
      "require_all": [
        "item/tools/mining_tool"
      ]
    },
    {
      "name": "shield items join c:tools/shield",
      "id_source": "item",
      "ids": [
        "clockstone_shield",
        "enhanced_clockstone_shield",
        "entropy_crystal_shield"
      ],
      "require_all": [
        "item/tools/shield"
      ]
    }
```

Add the three new tag names to `v2_allowed_tags` (append after
`"item/seeds/chrono_melon"`, the last entry):

```json
    "item/tools/melee_weapon",
    "item/tools/mining_tool",
    "item/tools/shield"
```

Add the same three names to `translation_exempt_tags` (append after
`"item/stripped_woods"`, the last entry) — these are tags Fabric API itself
supplies translations for, the same reasoning already applied to
`item/ores`, `item/foods/bread`, etc.:

```json
    "item/tools/melee_weapon",
    "item/tools/mining_tool",
    "item/tools/shield"
```

Verify JSON validity:
```bash
python3 -c "import json; json.load(open('scripts/conventional_tag_rules.json'))"
```
Expected: no output.

- [ ] **Step 2: Run validateConventionalTags to see it fail**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateConventionalTags
```
Expected: **FAIL**, with errors like `common/shared-1.21.1+/.../data/c/tags:
'chronodawn:clockstone_sword' missing from c:tools/melee_weapon (rule: sword
and axe items join c:tools/melee_weapon)`.

- [ ] **Step 3: Create the three v2 tool/shield tag files**

`common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/melee_weapon.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_sword",
    "chronodawn:enhanced_clockstone_sword",
    "chronodawn:entropy_crystal_sword",
    "chronodawn:chronoblade",
    "chronodawn:clockstone_axe",
    "chronodawn:enhanced_clockstone_axe"
  ]
}
```

`common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/mining_tool.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_pickaxe",
    "chronodawn:enhanced_clockstone_pickaxe",
    "chronodawn:spatially_linked_pickaxe"
  ]
}
```

`common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/shield.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_shield",
    "chronodawn:enhanced_clockstone_shield",
    "chronodawn:entropy_crystal_shield"
  ]
}
```

- [ ] **Step 4: Run validateConventionalTags to see it pass**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateConventionalTags
```
Expected: **PASS**.

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/melee_weapon.json \
        common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/mining_tool.json \
        common/shared-1.21.1+/src/main/resources/data/c/tags/item/tools/shield.json \
        scripts/conventional_tag_rules.json
git commit -m "feat(tags): add c:tools/melee_weapon, mining_tool, shield for 1.21.1+"
```

---

## Task 3: 1.21.1+ armor tag (`c:armors`)

**Files:**
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/armors.json`
- Modify: `scripts/conventional_tag_rules.json`

**Interfaces:**
- Consumes: nothing from Tasks 1–2.
- Produces: nothing later tasks depend on directly.

- [ ] **Step 1: Add the armor coverage rule (failing state)**

In `scripts/conventional_tag_rules.json`, insert this rule immediately after
the three rules added in Task 2 (still before the `era: "v1"` rules):

```json
    {
      "name": "armor items join c:armors",
      "id_source": "item",
      "ids": [
        "clockstone_helmet",
        "clockstone_chestplate",
        "clockstone_leggings",
        "clockstone_boots",
        "enhanced_clockstone_helmet",
        "enhanced_clockstone_chestplate",
        "enhanced_clockstone_leggings",
        "enhanced_clockstone_boots",
        "temporal_amber_helmet",
        "temporal_amber_chestplate",
        "temporal_amber_leggings",
        "temporal_amber_boots",
        "time_tyrant_mail"
      ],
      "require_all": [
        "item/armors"
      ]
    }
```

Add `"item/armors"` to `v2_allowed_tags` (append after the three tool tags
added in Task 2).

Add `"item/armors"` to `translation_exempt_tags` (append after the three
tool tags added in Task 2) — Fabric API's own bare `armors` tag already
carries an upstream translation.

Verify JSON validity:
```bash
python3 -c "import json; json.load(open('scripts/conventional_tag_rules.json'))"
```
Expected: no output.

- [ ] **Step 2: Run validateConventionalTags to see it fail**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateConventionalTags
```
Expected: **FAIL**, with errors like `common/shared-1.21.1+/.../data/c/tags:
'chronodawn:clockstone_helmet' missing from c:armors (rule: armor items join
c:armors)` — one per armor item ID.

- [ ] **Step 3: Create the armor tag file**

`common/shared-1.21.1+/src/main/resources/data/c/tags/item/armors.json`:
```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_helmet",
    "chronodawn:clockstone_chestplate",
    "chronodawn:clockstone_leggings",
    "chronodawn:clockstone_boots",
    "chronodawn:enhanced_clockstone_helmet",
    "chronodawn:enhanced_clockstone_chestplate",
    "chronodawn:enhanced_clockstone_leggings",
    "chronodawn:enhanced_clockstone_boots",
    "chronodawn:temporal_amber_helmet",
    "chronodawn:temporal_amber_chestplate",
    "chronodawn:temporal_amber_leggings",
    "chronodawn:temporal_amber_boots",
    "chronodawn:time_tyrant_mail"
  ]
}
```

- [ ] **Step 4: Run validateConventionalTags to see it pass**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateConventionalTags
```
Expected: **PASS**.

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/c/tags/item/armors.json \
        scripts/conventional_tag_rules.json
git commit -m "feat(tags): add c:armors for Chrono Dawn equipment on 1.21.1+"
```

---

## Task 4: Documentation

**Files:**
- Modify: `docs/modpack-integration.md`
- Modify: `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes: the tag lists established in Tasks 1–3 (exact tag names and
  member IDs).
- Produces: nothing — this is the final task.

- [ ] **Step 1: Add a new tools/armor/shield section to modpack-integration.md**

In `docs/modpack-integration.md`, find the `### 1.20.1` subsection under
`## Conventional (\`c:\`) tags` (the one documenting the v1 material tag
table, ending with the paragraph "...would produce files no other mod
reads."). Insert a new subsection immediately after it, before the `---`
that precedes `## Boss defeated event API`:

```markdown

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
| Clockstone, Enhanced Clockstone, and Temporal Amber armor (all 4 pieces each); Time Tyrant's Mail | `c:armors` |

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
```

- [ ] **Step 2: Mark the roadmap slice shipped**

In `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md`,
replace:

```markdown
- Tools / weapons / armor / shield tags (`c:tools/*`, `c:armors/*`) — deferred; tier semantics need their own decisions.
```

with:

```markdown
- Tools / weapons / armor / shield tags (`c:tools/*`, `c:armors/*`) —
  shipped 2026-09-03. The convention needed no tier decision: it offers
  only functional categories, with no shovel/hoe category and no
  per-material armor subtags below 1.21.11. See
  [2026-09-03-tools-armors-shield-tags-design.md](./2026-09-03-tools-armors-shield-tags-design.md).
```

Then update the status-tracker table row for `B. Datapack / tag externalization`:
replace

```markdown
| B. Datapack / tag externalization | 🚧 Tool / armor tag semantics remain undecided; modern material recipe inputs and biome / structure tags shipped; biome / structure Java material references reviewed with no conversion warranted | [2026-08-17-conventional-tags-design.md](./2026-08-17-conventional-tags-design.md), [2026-08-29-tag-consumers-design.md](./2026-08-29-tag-consumers-design.md), [2026-08-31-biome-structure-tags-design.md](./2026-08-31-biome-structure-tags-design.md) |
```

with

```markdown
| B. Datapack / tag externalization | 🚧 Tool/armor/shield tags shipped; remaining work is opportunistic consumer conversion (recipes or Java code still hardcoding a tool/armor `Item` reference) | [2026-08-17-conventional-tags-design.md](./2026-08-17-conventional-tags-design.md), [2026-08-29-tag-consumers-design.md](./2026-08-29-tag-consumers-design.md), [2026-08-31-biome-structure-tags-design.md](./2026-08-31-biome-structure-tags-design.md), [2026-09-03-tools-armors-shield-tags-design.md](./2026-09-03-tools-armors-shield-tags-design.md) |
```

- [ ] **Step 3: Add a CHANGELOG entry**

In `CHANGELOG.md`, under `## [Unreleased]` → `### Added`, insert as the
first bullet:

```markdown
- Chrono Dawn tool, weapon, armor, and shield items — including boss-drop
  and artifact gear — now join Fabric/NeoForge conventional (`c:`) tags by
  function: `c:tools/melee_weapon`, `c:tools/mining_tool`, `c:tools/shield`,
  and `c:armors` on Minecraft 1.21.1+, and the matching flat `c:swords` /
  `c:pickaxes` / `c:axes` / `c:shovels` / `c:hoes` / `c:shields` tags on
  1.20.1. See
  [docs/modpack-integration.md](docs/modpack-integration.md#tools-weapons-armor-and-shields).
```

- [ ] **Step 4: Verify the docs render sensibly**

```bash
grep -n "Tools, weapons, armor, and shields" docs/modpack-integration.md
grep -n "tools-armors-shield-tags-design" docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md
grep -n "c:tools/melee_weapon" CHANGELOG.md
```
Expected: each command prints one matching line.

- [ ] **Step 5: Commit**

```bash
git add docs/modpack-integration.md \
        docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md \
        CHANGELOG.md
git commit -m "docs(tags): document c:tools/*, c:armors/*, and shield tag coverage"
```

---

## Task 5: Full verification

**Files:** none (verification only).

**Interfaces:**
- Consumes: the complete state from Tasks 1–4.
- Produces: nothing — this is the final gate before the slice is considered done.

- [ ] **Step 1: Run validateResources**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateResources
```
Expected: **PASS** (this exercises the standard resource cross-reference
checks, including tag-member existence, across every version).

- [ ] **Step 2: Run validateConventionalTags standalone one more time**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateConventionalTags
```
Expected: **PASS**.

- [ ] **Step 3: Run the 1.21.11 unit test suite**

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11
```
Expected: **PASS** (this slice adds no Java code, so this run is a
regression check, not new coverage).

- [ ] **Step 4: Report results to the user**

Summarize the three verification commands' outcomes. If all three passed,
the slice is complete; do not run `checkAll` unless the user asks — it
rebuilds and gametests every supported version and is expensive.

---

## Self-Review Notes

- **Spec coverage:** §3 (scope) → Tasks 1–3 tag every item listed. §4.1/§4.2
  (tag assignments) → Tasks 1–3 mirror the exact tables. §5 (deferred
  `armors/humanoid`) → explicitly excluded, called out in Task 4's doc
  section. §6 (file layout) → File Structure section matches exactly. §7
  (verification) → Tasks 1–3's Step 2/4 plus Task 5. §8 (documentation) →
  Task 4.
- **Placeholder scan:** no TBD/TODO; every JSON snippet and doc edit is
  complete text ready to paste.
- **Type consistency:** tag path strings (`tools/melee_weapon`,
  `tools/mining_tool`, `tools/shield`, `armors`) are identical across the
  rules file, the tag files, and the documentation in every task.
