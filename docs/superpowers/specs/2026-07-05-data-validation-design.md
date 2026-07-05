# Design: validateData — Data-Pack Cross-Reference Validation

**Created**: 2026-07-05
**Status**: Approved design — first slice of the flagship-parity P3-1 workstream
**Parent**: [2026-07-05-flagship-parity-approach-outlines.md](./2026-07-05-flagship-parity-approach-outlines.md) (P3-1)

---

## Problem

ChronoDawn's dominant defect class is **silent cross-version breakage** in data
resources: recipes that stop resolving because an ID was typo'd or a format era
was wrong, tags that reference removed IDs, items whose 1.21.4+ client
definition (`items/<id>.json`) was forgotten in one of the two required roots
(purple-black icon). None of these fail the build; most only surface in manual
play.

The existing validation landscape (discovered during design):

| Task | Home | Covers |
| --- | --- | --- |
| `validateResources` | submodule `resource-validation.gradle` | JSON syntax, blockstate→model, model→texture |
| `validateTranslations` | submodule `resource-validation.gradle` | entity/spawn-egg lang keys — **currently a no-op** because `entity_id_file` / `item_id_file` are not set anywhere in the project |
| `validateBlockTagMembership` | project `gradle/chronodawn-validation.gradle` | Java `.replaceable()` ↔ tag presence |
| `validateRecipeUnlockAdvancements` | project | cooking recipe ↔ unlock advancement |
| `validateLangParity` | project | full lang-key parity (wraps `scripts/check_lang_parity.py`) |
| `validateEraJsonFormat` | project | recipe `result.item↔id`, advancement `icon.item↔id`, loot-table silk-touch predicate + `table_bonus` key, plural/singular directories |

What is genuinely missing, and in scope for this slice:

1. **Tag entry → registered ID** validation (also a prerequisite of the P1-1
   `c:` tag work).
2. **Recipe reference → registered ID** validation.
3. **Client Items JSON coverage** for 1.21.4+.
4. **Ingredient era rule** — `validateEraJsonFormat` treats all 1.21.x as one
   "modern" era, missing the object↔string ingredient split between 1.21.1 and
   1.21.2+.
5. **Fix the `validateTranslations` no-op** by setting the missing properties.

Out of scope (follow-up PRs): loot-table / advancement reference validation,
`sounds.json` ↔ `.ogg` parity, subtitle-key coverage, worldgen tag validation.

## Decisions (settled with maintainer)

- **Home for new generic checks**: the submodule **script variant**
  (`gradle/shared/` = `minecraft-mod-gradle-scripts`), as a **new file**
  `data-validation.gradle` registering a new task **`validateData`**.
  `validateResources` stays untouched. Rationale: minimal change to the shared
  task, clean assets-vs-data separation, reusable by the user's other mods.
- **Home for the ingredient era rule**: the existing project-local
  `validateEraJsonFormat` (era knowledge already lives there).
- **Wiring**: `checkAll` already supports `checkall_extra_tasks`
  (gradle.properties); append `validateData` to the existing list. No submodule
  `multi-version-tasks.gradle` change needed.
- **Property-driven and skip-by-default**: every check in `validateData`
  activates only when its properties are set, so other submodule consumers are
  unaffected until they opt in.

## Design

### Component 1: `gradle/shared/data-validation.gradle` (submodule, new)

Registers `validateData` (group `verification`). Follows the existing
`resource-validation.gradle` style: `JsonSlurper`, collect all errors, log
per-check counts via `logger.lifecycle`, fail at the end with a
`GradleException` carrying the error count.

**Shared plumbing** (within this script):

- *Resource roots*: all `common/shared*/src/main/resources` (auto-detected,
  same as `validateResources`) **plus** all version-specific
  `common/<ver>/src/main/resources` — data checks must see per-version roots,
  unlike the asset checks.
- *Registered-ID sets*: reuse the `extractEnumValues` regex approach
  (duplicated locally in this script; scripts applied via `apply from` do not
  share helpers). Sources configured via properties:
  - `item_id_file` → item IDs (already a documented property; newly set for
    ChronoDawn)
  - `block_id_file` → block IDs (**new property**)
  - `entity_id_file` → entity IDs (already documented; newly set)
  The regex `(\w+)\s*\(\s*"([^"]+)"\s*\)` matches both plain `NAME("id")` and
  ChronoDawn's `NAME(def("id"))` pattern (verified against `ModBlockId.java`).
- *Exclusions*: `data_validation_excluded_ids` (comma-separated) for IDs that
  are referenced in data but intentionally absent from the enum files.

**Check A — tag entries → registered IDs.**
Scan `data/*/tags/**/*.json` in every root (all namespaces — vanilla tag
files also carry `chronodawn:` entries). For each entry (bare string or
`{id: ...}` object):

- `chronodawn:<path>` → must exist in the ID set selected by the tag
  subdirectory: `item`/`items` → items ∪ blocks (BlockItems), `block`/`blocks`
  → blocks, `entity_type`/`entity_types` → entities. Other tag types
  (worldgen, …) are skipped and counted.
- `#chronodawn:<path>` → the referenced tag file must exist under
  `data/chronodawn/tags/<type>/<path>.json` in at least one root.
- Other namespaces (`minecraft:`, `c:`, …) → skipped (no static registry
  knowledge).

**Check B — recipe references → registered IDs.**
Scan `data/*/recipes/**/*.json` and `data/*/recipe/**/*.json` in every root.
Deep-walk the JSON tree; for every string value (excluding the `type` key):

- `chronodawn:<path>` → must be in items ∪ blocks.
- `#chronodawn:<path>` → tag file must exist (as in Check A).

Era-agnostic by construction — no format assumptions, so one logic covers all
three recipe eras including smithing and cooking types.

**Check C — client items JSON coverage (1.21.4+).**
Configured by `client_items_roots` (comma-separated resource roots; for
ChronoDawn: `common/1.21.4/src/main/resources` and
`common/shared-1.21.5+/src/main/resources`) and
`client_items_excluded_ids`.

- *ID source*: item IDs (from `item_id_file`) ∪ basenames of
  `assets/<ns>/models/item/*.json` across all resource roots. The model proxy pulls in
  BlockItems while naturally excluding item-less blocks (wall torches, crops,
  kelp plants have no item model). Variant-only models (e.g.
  `chrono_shield_blocking`) go in the exclusion list.
- *Forward check*: every source ID must have `assets/<ns>/items/<id>.json` in
  **each** configured root.
- *Reverse checks*: the `items/*.json` sets of the configured roots must match
  each other (parity), and every file must correspond to a source ID (orphan
  detection).

### Component 2: `validateEraJsonFormat` ingredient rule (project, extended)

Refine the recipe portion of the existing root→era mapping from two tiers to
three (directory naming and non-recipe checks keep the current two-tier
legacy/modern mapping):

| Era | Roots | Ingredient shape | Result key |
| --- | --- | --- | --- |
| legacy | `common/shared`, `common/1.20.1` | object (`{"item": ...}` / `{"tag": ...}`) | `item` (existing check) |
| mid | `common/1.21.1`, `common/shared-1.21.1+` | object | `id` (existing check) |
| modern | `common/shared-1.21.2+`, `common/shared-1.21.5+`, `common/1.21.2` … `common/1.21.11` | plain string (or array of strings) | `id` (existing check) |

> Errata (final review, 2026-07-05): the original table omitted
> `common/shared-1.21.5+` from the modern tier. That root serves 1.21.5+
> only, so string ingredients are unambiguously correct there; the
> implementation includes it.

New assertions on `crafting_shaped` `key.*` values, `crafting_shapeless`
`ingredients[]` entries, and cooking-type `ingredient` fields:

- modern root + object ingredient → error (silently dropped recipe class)
- legacy/mid root + string ingredient → error

Unknown recipe `type`s are skipped, as today.

### Component 3: configuration changes (ChronoDawn `gradle.properties`)

```properties
entity_id_file=common/shared/src/main/java/com/chronodawn/registry/ModEntityId.java
item_id_file=common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
block_id_file=common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
client_items_roots=common/1.21.4/src/main/resources,common/shared-1.21.5+/src/main/resources
checkall_extra_tasks=validateLangParity,validateBlockTagMembership,validateRecipeUnlockAdvancements,validateEraJsonFormat,validateData
```

plus one `apply from: 'gradle/shared/data-validation.gradle'` line in the root
`build.gradle`, and exclusion properties as baseline findings require.

Setting `entity_id_file` / `item_id_file` also **re-arms the existing
`validateTranslations`**, which has been validating zero entities/items. Its
first armed run may surface real missing keys; fixing those (or adding
`excluded_entities`) is part of this slice's baseline work.

### Error handling

Identical to existing tasks: accumulate all errors, print each with
`logger.error`, throw `GradleException` with the count. Each check logs how
many references/files it examined so a "passed" run is distinguishable from a
skipped one.

## Verification plan

1. **Baseline**: run `validateData` + extended `validateEraJsonFormat` +
   re-armed `validateTranslations` against the current tree. Triage every
   finding: real defect → fix in this PR; intentional → exclusion entry with a
   comment.
2. **Seeded errors**: for each check class, temporarily introduce one
   violation (bogus tag entry, typo'd recipe result, deleted `items/<id>.json`,
   object ingredient in a modern root) and confirm detection; revert.
3. **Wiring**: run `./gradlew checkAll` and confirm `validateData` executes in
   the extra-tasks slot.
4. **Two-repo flow**: commit the submodule change in
   `minecraft-mod-gradle-scripts` first (local sibling clone → push to GitHub),
   then bump the submodule pointer in ChronoDawn together with the project-side
   changes.

## Risks and limitations

- Regex-based ID extraction can over- or under-match if enum file styles
  change; the per-check "examined N" log line is the tripwire (a sudden drop
  to 0 means extraction broke).
- `minecraft:` / `c:` references remain unvalidated (no vanilla registry
  knowledge at build time). Accepted; runtime GameTest assertions are the
  planned complement (approach outlines P3-1, later slice).
- Version-gated content (IDs registered only on some versions via
  `minVersion()`) may need exclusions or smarter filtering; handle via
  baseline triage and note outcomes in the plan.

## Documentation updates (same PR)

- Approach outlines P3-1 / P6-1 and roadmap P3/P6 tables: record that
  `validateEraJsonFormat`, `validateBlockTagMembership`,
  `validateRecipeUnlockAdvancements`, and `validateLangParity` already exist;
  mark the P6-1 parity gate as shipped; scope P3-1's remaining work to what
  this design adds.
- Submodule `README.md`: add `data-validation.gradle` to the file table.
- ChronoDawn `CLAUDE.md` command list: add `validateData` alongside
  `validateResources`.
