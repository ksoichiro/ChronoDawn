# Conventional (`c:`) Tag Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish conventional (`c:`) item and block tags for every Chrono Dawn material, guarded by a Gradle validation task that fails when a material is missing its tag.

**Architecture:** Pure data-pack work plus one project-local Gradle verification task. Tags for MC 1.21.1–1.21.11 live in a single shared resource root (`common/shared-1.21.1+`) because the conventional-tag names used here are identical across all ten versions; MC 1.20.1 gets a smaller, separate set following the older v1 convention. A new `validateConventionalTags` task reads a declarative rule file and fails the build when a material ID matching a rule is absent from its required tags.

**Tech Stack:** Groovy DSL Gradle scripts, `groovy.json.JsonSlurper`, JSON data-pack files. No Java code changes, no new dependencies.

**Spec:** `docs/superpowers/specs/2026-08-17-conventional-tags-design.md`

## Global Constraints

- Work happens in the worktree `.worktrees/conventional-tags` on branch `conventional-tags`. Run `pwd` before the first command to confirm.
- Namespace for all new tags is `c`. Never emit `forge:`.
- 1.21.1+ tag files: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/` and `.../block/` — **singular** directory names.
- 1.20.1 tag files: `common/1.20.1/src/main/resources/data/c/tags/items/` and `.../blocks/` — **plural** directory names.
- Do not create version-specific overrides inside the 1.21.x range. Every tag name in this plan is verified to exist unchanged in Fabric conventional tags v2 from 1.21.1 through 1.21.11.
- Food subtags use the **singular** form (`c:foods/bread`, `c:foods/soup`, `c:foods/raw_meat`, …). The plural forms are `@Deprecated` "typoed" aliases in Fabric's source and must never be emitted.
- Do not modify anything under `gradle/shared/` — it is a git submodule shared with other projects.
- Every JSON file ends with a trailing newline and uses 2-space indentation, matching existing tag files.
- Every tag file uses the shape `{"replace": false, "values": [...]}`.
- Commit messages: English, Conventional Commits.
- `./gradlew` may need to be run with the sandbox disabled if a JVM launch is blocked; a `dyld`/`libjli.dylib` error is the signal.

---

### Task 1: `validateConventionalTags` task + ore tags (1.21.1+)

Ships the validator together with the first tags it guards, so the task ends with a green build. The validator is written first and observed failing — that is this task's test cycle.

**Files:**
- Create: `scripts/conventional_tag_rules.json`
- Modify: `gradle/chronodawn-validation.gradle` (append a new task after `validateEraJsonFormat`, before the `walkLootConditions` helpers at line 408)
- Modify: `gradle.properties:65` (`checkall_extra_tasks`)
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/block/ores/{coal,iron,gold,redstone,clockstone,time_crystal,entropy_crystal,temporal_amber}.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/block/{ores.json,ores_in_ground/stone.json,ores_in_ground/deepslate.json,ore_bearing_ground/stone.json,ore_bearing_ground/deepslate.json}`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/ores/{coal,iron,gold,redstone,clockstone,time_crystal,entropy_crystal,temporal_amber}.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/ores.json`

**Interfaces:**
- Consumes: nothing.
- Produces: the Gradle task `validateConventionalTags`, and the rule-file schema that Tasks 2–5 extend. Rule objects have the shape:
  `{"name": String, "id_source": "block"|"item", "pattern": String (Java regex, full match) | "ids": [String], "require_any": [String glob], "require_all": [String]}`
  where tag names are written without the `c:` prefix and `require_any` globs use `*` to mean "any subtag under this path". The file also carries `"v2_allowed_tags"` and `"v1_allowed_tags"` (lists of exact tag names) and `"v1_root"` / `"v2_roots"`.

- [ ] **Step 1: Write the rule file with ore rules only**

Create `scripts/conventional_tag_rules.json`:

```json
{
  "v2_roots": [
    "common/shared-1.21.1+/src/main/resources/data/c/tags"
  ],
  "v1_root": "common/1.20.1/src/main/resources/data/c/tags",
  "v2_allowed_tags": [
    "block/ores",
    "block/ores/coal",
    "block/ores/iron",
    "block/ores/gold",
    "block/ores/redstone",
    "block/ores/clockstone",
    "block/ores/time_crystal",
    "block/ores/entropy_crystal",
    "block/ores/temporal_amber",
    "block/ores_in_ground/stone",
    "block/ores_in_ground/deepslate",
    "block/ore_bearing_ground/stone",
    "block/ore_bearing_ground/deepslate",
    "item/ores",
    "item/ores/coal",
    "item/ores/iron",
    "item/ores/gold",
    "item/ores/redstone",
    "item/ores/clockstone",
    "item/ores/time_crystal",
    "item/ores/entropy_crystal",
    "item/ores/temporal_amber"
  ],
  "v1_allowed_tags": [],
  "rules": [
    {
      "name": "ore blocks join an ores subtag",
      "id_source": "block",
      "pattern": ".*_ore",
      "require_any": ["block/ores/*"]
    },
    {
      "name": "ore items join an ores subtag",
      "id_source": "item",
      "pattern": ".*_ore",
      "require_any": ["item/ores/*"]
    },
    {
      "name": "stone-hosted ore blocks join ores_in_ground/stone",
      "id_source": "block",
      "pattern": "(?!deepslate_).*_ore",
      "require_all": ["block/ores_in_ground/stone"]
    },
    {
      "name": "deepslate ore blocks join ores_in_ground/deepslate",
      "id_source": "block",
      "pattern": "deepslate_.*_ore",
      "require_all": ["block/ores_in_ground/deepslate"]
    }
  ]
}
```

- [ ] **Step 2: Write the validator task**

Append to `gradle/chronodawn-validation.gradle`, immediately before the `// Helpers for loot_table walking.` comment:

```groovy
// ============================================================
// Task 5: validateConventionalTags
// ------------------------------------------------------------
// Verifies conventional (c:) tag coverage for materials. Three checks:
//
//   1. Coverage   — every registered ID matching a rule in
//                   scripts/conventional_tag_rules.json is present in the
//                   tags that rule requires. Rules are pattern-driven, so a
//                   newly added material inherits the requirement and a
//                   missing tag fails the build instead of silently making
//                   the material invisible to other mods.
//   2. Tag names  — every emitted tag name is on the allowlist for its
//                   convention era. Catches typos and the deprecated plural
//                   food/tool aliases, which no mod reads.
//   3. Consistency — when more than one 1.21.1+ resource root carries
//                   data/c/, their tag name sets must agree.
//
// Membership resolution follows '#c:' tag references transitively, so an
// ID counts as a member of c:ores when it sits in c:ores/coal and
// c:ores/coal is referenced from c:ores.
// ============================================================
tasks.register('validateConventionalTags') {
    group = 'verification'
    description = 'Verify conventional (c:) tag coverage, tag-name validity and cross-root consistency'

    doLast {
        def jsonSlurper = new JsonSlurper()
        def errors = []
        def rulesFile = file("${rootProject.projectDir}/scripts/conventional_tag_rules.json")
        if (!rulesFile.exists()) {
            throw new GradleException("validateConventionalTags: ${rulesFile} not found")
        }
        def config = jsonSlurper.parseText(rulesFile.text)

        def idsFrom = { String propName ->
            def f = file("${rootProject.projectDir}/${project.property(propName)}")
            def ids = [] as Set
            (f.text =~ /(\w+)\s*\(\s*"([^"]+)"\s*\)/).each { ids << it[2] }
            ids
        }
        def idSets = ['block': idsFrom('block_id_file'), 'item': idsFrom('item_id_file')]

        // Load every tag file under a c: tag root into 'type/path' -> raw values.
        def loadRoot = { String rootRel ->
            def tags = [:]
            def rootDir = file("${rootProject.projectDir}/${rootRel}")
            if (!rootDir.exists()) return tags
            fileTree(dir: rootDir, includes: ['**/*.json']).each { File f ->
                def name = rootDir.toPath().relativize(f.toPath()).toString()
                        .replace(File.separator, '/').replaceAll(/\.json$/, '')
                def json
                try {
                    json = jsonSlurper.parseText(f.text)
                } catch (Exception e) {
                    errors << "Conventional tags: ${name}.json is not valid JSON"
                    return
                }
                tags[name] = (json instanceof Map && json.values instanceof List) ? json.values : []
            }
            tags
        }

        // Resolve members of a tag, following '#c:' references transitively.
        def resolveMembers
        resolveMembers = { Map tags, String tagName, Set seen ->
            if (!tags.containsKey(tagName) || seen.contains(tagName)) return [] as Set
            seen << tagName
            def out = [] as Set
            def type = tagName.split('/')[0]
            tags[tagName].each { entry ->
                def raw = (entry instanceof Map) ? entry.id : entry
                if (!(raw instanceof String)) return
                if (raw.startsWith('#c:')) {
                    out.addAll(resolveMembers(tags, "${type}/${raw.substring(3)}".toString(), seen))
                } else {
                    out << raw
                }
            }
            out
        }

        def v2Roots = config.v2_roots as List
        def rootTags = [:]
        v2Roots.each { rootTags[it] = loadRoot(it as String) }

        // --- Check 1: coverage ---------------------------------------------
        int coverageChecked = 0
        v2Roots.each { String rootRel ->
            def tags = rootTags[rootRel]
            // Deliberately not skipping an empty root: "no tags yet" must fail
            // the coverage check, not pass it vacuously.
            config.rules.each { rule ->
                def candidates = rule.ids != null
                        ? (rule.ids as Set)
                        : idSets[rule.id_source as String].findAll { it ==~ (rule.pattern as String) }
                candidates.each { String id ->
                    def fqId = "chronodawn:${id}".toString()
                    coverageChecked++
                    (rule.require_all ?: []).each { String required ->
                        if (!resolveMembers(tags, required, [] as Set).contains(fqId)) {
                            errors << "${rootRel}: '${fqId}' missing from c:${required.split('/', 2)[1]} (rule: ${rule.name})"
                        }
                    }
                    def anyGlobs = rule.require_any ?: []
                    if (!anyGlobs.isEmpty()) {
                        def matched = anyGlobs.any { String glob ->
                            def re = glob.replace('*', '[^/]+')
                            tags.keySet().findAll { it ==~ re }.any { String t ->
                                resolveMembers(tags, t, [] as Set).contains(fqId)
                            }
                        }
                        if (!matched) {
                            errors << "${rootRel}: '${fqId}' is in none of ${anyGlobs.collect { 'c:' + it.split('/', 2)[1] }} (rule: ${rule.name})"
                        }
                    }
                }
            }
        }

        // --- Check 2: tag names on the era allowlist ------------------------
        def v2Allowed = (config.v2_allowed_tags ?: []) as Set
        v2Roots.each { String rootRel ->
            rootTags[rootRel].keySet().each { String name ->
                if (!v2Allowed.contains(name)) {
                    errors << "${rootRel}: tag '${name}' is not in v2_allowed_tags (typo, or a deprecated alias no mod reads?)"
                }
            }
        }
        def v1Root = config.v1_root as String
        def v1Tags = loadRoot(v1Root)
        def v1Allowed = (config.v1_allowed_tags ?: []) as Set
        v1Tags.keySet().each { String name ->
            if (!v1Allowed.contains(name)) {
                errors << "${v1Root}: tag '${name}' is not in v1_allowed_tags (conventional tags v1 has no such tag)"
            }
        }

        // --- Check 3: cross-root consistency among 1.21.1+ roots ------------
        def populated = v2Roots.findAll { !rootTags[it].isEmpty() }
        if (populated.size() > 1) {
            def reference = populated[0] as String
            def referenceNames = rootTags[reference].keySet() as Set
            populated.tail().each { String other ->
                def otherNames = rootTags[other].keySet() as Set
                (referenceNames - otherNames).each { errors << "${other}: missing tag '${it}' present in ${reference}" }
                (otherNames - referenceNames).each { errors << "${other}: extra tag '${it}' absent from ${reference}" }
            }
        }

        if (errors.isEmpty()) {
            logger.lifecycle("Conventional tag validation passed: ${coverageChecked} ID/rule checks across ${v2Roots.size()} 1.21.1+ root(s) and ${v1Tags.size()} v1 tag file(s).")
        } else {
            errors.each { logger.error("  ERROR: ${it}") }
            throw new GradleException("Conventional tag validation failed with ${errors.size()} error(s)")
        }
    }
}
```

- [ ] **Step 3: Run the validator and verify it fails**

```bash
./gradlew validateConventionalTags
```

Expected: FAIL. The `data/c/` directory does not exist yet, so every ore ID reports `is in none of [c:ores/*]`. There should be 12 ore blocks × rules plus 12 ore items reported.

- [ ] **Step 4: Create the ore block subtag files**

`common/shared-1.21.1+/src/main/resources/data/c/tags/block/ores/coal.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_coal_ore"
  ]
}
```

`.../block/ores/iron.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_iron_ore"
  ]
}
```

`.../block/ores/gold.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_gold_ore",
    "chronodawn:deepslate_temporal_gold_ore"
  ]
}
```

`.../block/ores/redstone.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_redstone_ore",
    "chronodawn:deepslate_temporal_redstone_ore"
  ]
}
```

`.../block/ores/clockstone.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_ore",
    "chronodawn:deepslate_clockstone_ore"
  ]
}
```

`.../block/ores/time_crystal.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_crystal_ore"
  ]
}
```

`.../block/ores/entropy_crystal.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:entropy_crystal_ore"
  ]
}
```

`.../block/ores/temporal_amber.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_amber_ore",
    "chronodawn:deepslate_temporal_amber_ore"
  ]
}
```

- [ ] **Step 5: Create the ore umbrella and ground tag files**

Only the mod's own subtags are added to `c:ores`; `c:ores/coal`, `/iron`, `/gold` and `/redstone` are already referenced from `c:ores` by the loader's own data pack.

`.../block/ores.json`:

```json
{
  "replace": false,
  "values": [
    "#c:ores/clockstone",
    "#c:ores/time_crystal",
    "#c:ores/entropy_crystal",
    "#c:ores/temporal_amber"
  ]
}
```

`.../block/ores_in_ground/stone.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_coal_ore",
    "chronodawn:temporal_iron_ore",
    "chronodawn:temporal_gold_ore",
    "chronodawn:temporal_redstone_ore",
    "chronodawn:clockstone_ore",
    "chronodawn:time_crystal_ore",
    "chronodawn:entropy_crystal_ore",
    "chronodawn:temporal_amber_ore"
  ]
}
```

`.../block/ores_in_ground/deepslate.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:deepslate_clockstone_ore",
    "chronodawn:deepslate_temporal_gold_ore",
    "chronodawn:deepslate_temporal_redstone_ore",
    "chronodawn:deepslate_temporal_amber_ore"
  ]
}
```

`.../block/ore_bearing_ground/stone.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_stone"
  ]
}
```

`.../block/ore_bearing_ground/deepslate.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:deepslate_temporal_stone"
  ]
}
```

- [ ] **Step 6: Create the ore item tag files**

Create `.../item/ores/coal.json`, `iron.json`, `gold.json`, `redstone.json`, `clockstone.json`, `time_crystal.json`, `entropy_crystal.json`, `temporal_amber.json` with **byte-identical content to their `block/ores/` counterparts in Step 4** (the item form of an ore block shares its ID), and `.../item/ores.json` identical to `block/ores.json` from Step 5.

- [ ] **Step 7: Run the validator and verify it passes**

```bash
./gradlew validateConventionalTags
```

Expected: PASS, with a line like `Conventional tag validation passed: 48 ID/rule checks across 1 1.21.1+ root(s) and 0 v1 tag file(s).`

- [ ] **Step 8: Probe the guard — confirm it actually fires**

A green run does not prove the new check works. Temporarily delete `chronodawn:clockstone_ore` from `.../block/ores/clockstone.json` and re-run:

```bash
./gradlew validateConventionalTags
```

Expected: FAIL with `'chronodawn:clockstone_ore' is in none of [c:ores/*]`. Then restore the line and re-run to confirm PASS again. Do not commit the deliberate violation.

- [ ] **Step 9: Wire into checkAll**

In `gradle.properties:65`, append `,validateConventionalTags` to `checkall_extra_tasks`, giving:

```properties
checkall_extra_tasks=validateLangParity,validateBlockTagMembership,validateRecipeUnlockAdvancements,validateEraJsonFormat,validateData,validateConventionalTags
```

- [ ] **Step 10: Verify validateData still passes over the new namespace**

`validateData` Check A walks every namespace under `data/`, including the new `c`. Run it to confirm the `#c:` references and `chronodawn:` entries resolve:

```bash
./gradlew validateData
```

Expected: PASS.

- [ ] **Step 11: Commit**

```bash
git add scripts/conventional_tag_rules.json gradle/chronodawn-validation.gradle gradle.properties \
        common/shared-1.21.1+/src/main/resources/data/c
git commit -m "feat(tags): publish conventional ore tags with a coverage guard"
```

---

### Task 2: Material items and storage blocks (1.21.1+)

**Files:**
- Modify: `scripts/conventional_tag_rules.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/ingots/{clockstone,enhanced_clockstone}.json`, `.../item/ingots.json`
- Create: `.../item/gems/{time_crystal,entropy_crystal}.json`, `.../item/gems.json`
- Create: `.../item/raw_materials/temporal_amber.json`, `.../item/raw_materials.json`
- Create: `.../item/dusts/temporal_amber.json`, `.../item/dusts.json`
- Create: `.../block/storage_blocks/{clockstone,time_crystal}.json`, `.../block/storage_blocks.json`
- Create: `.../item/storage_blocks/{clockstone,time_crystal}.json`, `.../item/storage_blocks.json`

**Interfaces:**
- Consumes: the `validateConventionalTags` task and rule-file schema from Task 1.
- Produces: no new interfaces; extends the same rule file.

- [ ] **Step 1: Add the rules and allowlist entries**

In `scripts/conventional_tag_rules.json`, add to `v2_allowed_tags`:

```json
"item/ingots",
"item/ingots/clockstone",
"item/ingots/enhanced_clockstone",
"item/gems",
"item/gems/time_crystal",
"item/gems/entropy_crystal",
"item/raw_materials",
"item/raw_materials/temporal_amber",
"item/dusts",
"item/dusts/temporal_amber",
"block/storage_blocks",
"block/storage_blocks/clockstone",
"block/storage_blocks/time_crystal",
"item/storage_blocks",
"item/storage_blocks/clockstone",
"item/storage_blocks/time_crystal"
```

and add to `rules`:

```json
{
  "name": "material items join their category subtag",
  "id_source": "item",
  "ids": ["clockstone", "enhanced_clockstone", "time_crystal", "entropy_crystal", "raw_temporal_amber", "temporal_amber_dust"],
  "require_any": ["item/ingots/*", "item/gems/*", "item/raw_materials/*", "item/dusts/*"]
},
{
  "name": "storage blocks join storage_blocks",
  "id_source": "block",
  "ids": ["clockstone_block", "time_crystal_block"],
  "require_all": ["block/storage_blocks"]
},
{
  "name": "storage block items join storage_blocks",
  "id_source": "item",
  "ids": ["clockstone_block", "time_crystal_block"],
  "require_all": ["item/storage_blocks"]
}
```

- [ ] **Step 2: Run the validator and verify it fails**

```bash
./gradlew validateConventionalTags
```

Expected: FAIL, reporting the six material items and both storage blocks as missing, plus nothing else.

- [ ] **Step 3: Create the material item tag files**

`.../item/ingots/clockstone.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone"
  ]
}
```

`.../item/ingots/enhanced_clockstone.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:enhanced_clockstone"
  ]
}
```

`.../item/ingots.json`:

```json
{
  "replace": false,
  "values": [
    "#c:ingots/clockstone",
    "#c:ingots/enhanced_clockstone"
  ]
}
```

`.../item/gems/time_crystal.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_crystal"
  ]
}
```

`.../item/gems/entropy_crystal.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:entropy_crystal"
  ]
}
```

`.../item/gems.json`:

```json
{
  "replace": false,
  "values": [
    "#c:gems/time_crystal",
    "#c:gems/entropy_crystal"
  ]
}
```

`.../item/raw_materials/temporal_amber.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:raw_temporal_amber"
  ]
}
```

`.../item/raw_materials.json`:

```json
{
  "replace": false,
  "values": [
    "#c:raw_materials/temporal_amber"
  ]
}
```

`.../item/dusts/temporal_amber.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_amber_dust"
  ]
}
```

`.../item/dusts.json`:

```json
{
  "replace": false,
  "values": [
    "#c:dusts/temporal_amber"
  ]
}
```

- [ ] **Step 4: Create the storage block tag files**

`.../block/storage_blocks/clockstone.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_block"
  ]
}
```

`.../block/storage_blocks/time_crystal.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_crystal_block"
  ]
}
```

`.../block/storage_blocks.json`:

```json
{
  "replace": false,
  "values": [
    "#c:storage_blocks/clockstone",
    "#c:storage_blocks/time_crystal"
  ]
}
```

Create `.../item/storage_blocks/clockstone.json`, `.../item/storage_blocks/time_crystal.json` and `.../item/storage_blocks.json` with content identical to their `block/` counterparts above.

- [ ] **Step 5: Run the validator and verify it passes**

```bash
./gradlew validateConventionalTags
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add scripts/conventional_tag_rules.json common/shared-1.21.1+/src/main/resources/data/c
git commit -m "feat(tags): publish conventional material and storage block tags"
```

---

### Task 3: Stone, sand, gravel, sandstone and wood sets (1.21.1+)

**Files:**
- Modify: `scripts/conventional_tag_rules.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/block/{stones.json,gravels.json,sands.json,cobblestones.json}`
- Create: `.../block/cobblestones/{normal,mossy,deepslate}.json`
- Create: `.../block/sandstone/{blocks,slabs,stairs}.json`
- Create: `.../block/{stripped_logs.json,stripped_woods.json,fences.json,fence_gates.json}`
- Create: `.../block/fences/wooden.json`, `.../block/fence_gates/wooden.json`
- Create: the matching `item/` files for every block tag above

**Interfaces:**
- Consumes: `validateConventionalTags` and the rule file.
- Produces: nothing new.

- [ ] **Step 1: Add the rules and allowlist entries**

In `scripts/conventional_tag_rules.json`, add to `v2_allowed_tags`:

```json
"block/stones",
"block/sands",
"block/gravels",
"block/cobblestones",
"block/cobblestones/normal",
"block/cobblestones/mossy",
"block/cobblestones/deepslate",
"block/sandstone/blocks",
"block/sandstone/slabs",
"block/sandstone/stairs",
"block/stripped_logs",
"block/stripped_woods",
"block/fences",
"block/fences/wooden",
"block/fence_gates",
"block/fence_gates/wooden",
"item/stones",
"item/sands",
"item/gravels",
"item/cobblestones",
"item/cobblestones/normal",
"item/cobblestones/mossy",
"item/cobblestones/deepslate",
"item/sandstone/blocks",
"item/sandstone/slabs",
"item/sandstone/stairs",
"item/stripped_logs",
"item/stripped_woods",
"item/fences",
"item/fences/wooden",
"item/fence_gates",
"item/fence_gates/wooden"
```

then add to `rules`:

```json
{
  "name": "cobblestone variants join a cobblestones subtag",
  "id_source": "block",
  "ids": ["temporal_cobblestone", "mossy_temporal_cobblestone", "cobbled_deepslate_temporal_stone"],
  "require_any": ["block/cobblestones/*"]
},
{
  "name": "stone, sand and gravel join their umbrella",
  "id_source": "block",
  "ids": ["temporal_stone"],
  "require_all": ["block/stones"]
},
{
  "name": "temporal sand joins sands",
  "id_source": "block",
  "ids": ["temporal_sand"],
  "require_all": ["block/sands"]
},
{
  "name": "temporal gravel joins gravels",
  "id_source": "block",
  "ids": ["temporal_gravel"],
  "require_all": ["block/gravels"]
},
{
  "name": "stripped logs join stripped_logs",
  "id_source": "block",
  "pattern": "stripped_.*_log",
  "require_all": ["block/stripped_logs"]
},
{
  "name": "stripped woods join stripped_woods",
  "id_source": "block",
  "pattern": "stripped_(?!.*_log$).*_wood",
  "require_all": ["block/stripped_woods"]
},
{
  "name": "wooden fences join fences/wooden",
  "id_source": "block",
  "pattern": ".*_fence",
  "require_all": ["block/fences/wooden"]
},
{
  "name": "wooden fence gates join fence_gates/wooden",
  "id_source": "block",
  "pattern": ".*_fence_gate",
  "require_all": ["block/fence_gates/wooden"]
}
```

Note the `stripped_woods` pattern deliberately excludes IDs ending in `_log`, because `stripped_time_wood_log` and `stripped_time_wood` are distinct blocks and only the latter belongs in `c:stripped_woods`.

- [ ] **Step 2: Run the validator and verify it fails**

```bash
./gradlew validateConventionalTags
```

Expected: FAIL, listing the three cobblestone variants, `temporal_stone`, `temporal_sand`, `temporal_gravel`, three stripped logs, three stripped woods, three fences and three fence gates.

- [ ] **Step 3: Create the stone, sand and gravel tag files**

`.../block/stones.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_stone"
  ]
}
```

`.../block/sands.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sand"
  ]
}
```

`.../block/gravels.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_gravel"
  ]
}
```

`.../block/cobblestones/normal.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_cobblestone"
  ]
}
```

`.../block/cobblestones/mossy.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:mossy_temporal_cobblestone"
  ]
}
```

`.../block/cobblestones/deepslate.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:cobbled_deepslate_temporal_stone"
  ]
}
```

`.../block/cobblestones.json`:

```json
{
  "replace": false,
  "values": [
    "#c:cobblestones/normal",
    "#c:cobblestones/mossy",
    "#c:cobblestones/deepslate"
  ]
}
```

- [ ] **Step 4: Create the sandstone tag files**

`.../block/sandstone/blocks.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sandstone"
  ]
}
```

`.../block/sandstone/slabs.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sandstone_slab"
  ]
}
```

`.../block/sandstone/stairs.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sandstone_stairs"
  ]
}
```

- [ ] **Step 5: Create the wood set tag files**

`.../block/stripped_logs.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:stripped_time_wood_log",
    "chronodawn:stripped_dark_time_wood_log",
    "chronodawn:stripped_ancient_time_wood_log"
  ]
}
```

`.../block/stripped_woods.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:stripped_time_wood",
    "chronodawn:stripped_dark_time_wood",
    "chronodawn:stripped_ancient_time_wood"
  ]
}
```

`.../block/fences/wooden.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_wood_fence",
    "chronodawn:dark_time_wood_fence",
    "chronodawn:ancient_time_wood_fence"
  ]
}
```

`.../block/fences.json`:

```json
{
  "replace": false,
  "values": [
    "#c:fences/wooden"
  ]
}
```

`.../block/fence_gates/wooden.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_wood_fence_gate",
    "chronodawn:dark_time_wood_fence_gate",
    "chronodawn:ancient_time_wood_fence_gate"
  ]
}
```

`.../block/fence_gates.json`:

```json
{
  "replace": false,
  "values": [
    "#c:fence_gates/wooden"
  ]
}
```

- [ ] **Step 6: Mirror every file above into `item/`**

Create the same tag paths under `.../data/c/tags/item/` with identical content. Every block listed has an item form with the same ID.

- [ ] **Step 7: Run the validator and verify it passes**

```bash
./gradlew validateConventionalTags
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add scripts/conventional_tag_rules.json common/shared-1.21.1+/src/main/resources/data/c
git commit -m "feat(tags): publish conventional stone, sand and wood set tags"
```

---

### Task 4: Foods, crops and seeds (1.21.1+)

**Files:**
- Modify: `scripts/conventional_tag_rules.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/c/tags/item/foods.json`
- Create: `.../item/foods/{bread,cookie,pie,fruit,vegetable,soup,raw_meat,cooked_meat,raw_fish,cooked_fish,golden}.json`
- Create: `.../item/{drinks.json,mushrooms.json,crops.json,seeds.json}`
- Create: `.../item/drinks/juice.json`, `.../item/crops/{time_wheat,chrono_melon}.json`, `.../item/seeds/{time_wheat,chrono_melon}.json`

**Interfaces:**
- Consumes: `validateConventionalTags` and the rule file.
- Produces: nothing new.

- [ ] **Step 1: Add the rules and allowlist entries**

In `scripts/conventional_tag_rules.json`, add to `v2_allowed_tags`:

```json
"item/foods",
"item/foods/bread",
"item/foods/cookie",
"item/foods/pie",
"item/foods/fruit",
"item/foods/vegetable",
"item/foods/soup",
"item/foods/raw_meat",
"item/foods/cooked_meat",
"item/foods/raw_fish",
"item/foods/cooked_fish",
"item/foods/golden",
"item/drinks",
"item/drinks/juice",
"item/mushrooms",
"item/crops",
"item/crops/time_wheat",
"item/crops/chrono_melon",
"item/seeds",
"item/seeds/time_wheat",
"item/seeds/chrono_melon"
```

then add to `rules`:

```json
{
  "name": "food items join a foods subtag",
  "id_source": "item",
  "ids": ["time_bread", "enhanced_time_bread", "time_wheat_cookie", "clockwork_cookie", "time_fruit_pie", "fruit_of_time", "chrono_melon_slice", "temporal_root", "baked_temporal_root", "temporal_root_stew", "timeless_mushroom_soup", "chrono_bovine_meat", "cooked_chrono_bovine_meat", "glide_fish", "cooked_glide_fish", "golden_time_wheat", "glistening_chrono_melon"],
  "require_any": ["item/foods/*"],
  "require_all": ["item/foods"]
},
{
  "name": "crops join a crops subtag",
  "id_source": "item",
  "ids": ["time_wheat", "chrono_melon"],
  "require_any": ["item/crops/*"]
},
{
  "name": "seeds join a seeds subtag",
  "id_source": "item",
  "ids": ["time_wheat_seeds", "chrono_melon_seeds"],
  "require_any": ["item/seeds/*"]
}
```

- [ ] **Step 2: Run the validator and verify it fails**

```bash
./gradlew validateConventionalTags
```

Expected: FAIL, listing the 17 food items (each twice — once for `require_any`, once for `require_all`), two crops and two seeds.

- [ ] **Step 3: Create the food subtag files**

`.../item/foods/bread.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_bread",
    "chronodawn:enhanced_time_bread"
  ]
}
```

`.../item/foods/cookie.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_wheat_cookie",
    "chronodawn:clockwork_cookie"
  ]
}
```

`.../item/foods/pie.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_fruit_pie"
  ]
}
```

`.../item/foods/fruit.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:fruit_of_time",
    "chronodawn:chrono_melon_slice"
  ]
}
```

`.../item/foods/vegetable.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_root",
    "chronodawn:baked_temporal_root"
  ]
}
```

`.../item/foods/soup.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_root_stew",
    "chronodawn:timeless_mushroom_soup"
  ]
}
```

`.../item/foods/raw_meat.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:chrono_bovine_meat"
  ]
}
```

`.../item/foods/cooked_meat.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:cooked_chrono_bovine_meat"
  ]
}
```

`.../item/foods/raw_fish.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:glide_fish"
  ]
}
```

`.../item/foods/cooked_fish.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:cooked_glide_fish"
  ]
}
```

`.../item/foods/golden.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:golden_time_wheat",
    "chronodawn:glistening_chrono_melon"
  ]
}
```

`.../item/foods.json`:

```json
{
  "replace": false,
  "values": [
    "#c:foods/bread",
    "#c:foods/cookie",
    "#c:foods/pie",
    "#c:foods/fruit",
    "#c:foods/vegetable",
    "#c:foods/soup",
    "#c:foods/raw_meat",
    "#c:foods/cooked_meat",
    "#c:foods/raw_fish",
    "#c:foods/cooked_fish",
    "#c:foods/golden"
  ]
}
```

- [ ] **Step 4: Create the drink, mushroom, crop and seed tag files**

`.../item/drinks/juice.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:chrono_melon_juice"
  ]
}
```

`.../item/drinks.json`:

```json
{
  "replace": false,
  "values": [
    "#c:drinks/juice"
  ]
}
```

`.../item/mushrooms.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:timeless_mushroom"
  ]
}
```

`.../item/crops/time_wheat.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_wheat"
  ]
}
```

`.../item/crops/chrono_melon.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:chrono_melon"
  ]
}
```

`.../item/crops.json`:

```json
{
  "replace": false,
  "values": [
    "#c:crops/time_wheat",
    "#c:crops/chrono_melon"
  ]
}
```

`.../item/seeds/time_wheat.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_wheat_seeds"
  ]
}
```

`.../item/seeds/chrono_melon.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:chrono_melon_seeds"
  ]
}
```

`.../item/seeds.json`:

```json
{
  "replace": false,
  "values": [
    "#c:seeds/time_wheat",
    "#c:seeds/chrono_melon"
  ]
}
```

- [ ] **Step 5: Run the validator and verify it passes**

```bash
./gradlew validateConventionalTags
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add scripts/conventional_tag_rules.json common/shared-1.21.1+/src/main/resources/data/c
git commit -m "feat(tags): publish conventional food, crop and seed tags"
```

---

### Task 5: 1.20.1 conventional tags v1 set

Conventional tags v1 has no per-material subtags, so 1.20.1 gets flat umbrella tags only, in **plural** directories.

**Files:**
- Modify: `scripts/conventional_tag_rules.json` (`v1_allowed_tags`)
- Create: `common/1.20.1/src/main/resources/data/c/tags/blocks/ores.json`
- Create: `common/1.20.1/src/main/resources/data/c/tags/blocks/{sandstone_blocks,sandstone_slabs,sandstone_stairs}.json`
- Create: `common/1.20.1/src/main/resources/data/c/tags/items/{ores,ingots,gems,raw_ores,dusts,foods,sandstone_blocks,sandstone_slabs,sandstone_stairs}.json`

**Interfaces:**
- Consumes: the `v1_root` / `v1_allowed_tags` handling already implemented in Task 1's validator.
- Produces: nothing new.

- [ ] **Step 1: Populate `v1_allowed_tags`**

In `scripts/conventional_tag_rules.json`, replace `"v1_allowed_tags": []` with:

```json
"v1_allowed_tags": [
  "blocks/ores",
  "blocks/sandstone_blocks",
  "blocks/sandstone_slabs",
  "blocks/sandstone_stairs",
  "items/ores",
  "items/ingots",
  "items/gems",
  "items/raw_ores",
  "items/dusts",
  "items/foods",
  "items/sandstone_blocks",
  "items/sandstone_slabs",
  "items/sandstone_stairs"
]
```

- [ ] **Step 2: Verify the allowlist check rejects an out-of-convention name**

Create a throwaway file `common/1.20.1/src/main/resources/data/c/tags/items/storage_blocks.json`:

```json
{
  "replace": false,
  "values": []
}
```

Run:

```bash
./gradlew validateConventionalTags
```

Expected: FAIL with `tag 'items/storage_blocks' is not in v1_allowed_tags (conventional tags v1 has no such tag)`. Delete the throwaway file afterwards — `c:storage_blocks` genuinely does not exist in v1 and must not be shipped on 1.20.1.

- [ ] **Step 3: Create the 1.20.1 block tag files**

`common/1.20.1/src/main/resources/data/c/tags/blocks/ores.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_coal_ore",
    "chronodawn:temporal_iron_ore",
    "chronodawn:temporal_gold_ore",
    "chronodawn:deepslate_temporal_gold_ore",
    "chronodawn:temporal_redstone_ore",
    "chronodawn:deepslate_temporal_redstone_ore",
    "chronodawn:clockstone_ore",
    "chronodawn:deepslate_clockstone_ore",
    "chronodawn:time_crystal_ore",
    "chronodawn:entropy_crystal_ore",
    "chronodawn:temporal_amber_ore",
    "chronodawn:deepslate_temporal_amber_ore"
  ]
}
```

`.../blocks/sandstone_blocks.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sandstone"
  ]
}
```

`.../blocks/sandstone_slabs.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sandstone_slab"
  ]
}
```

`.../blocks/sandstone_stairs.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_sandstone_stairs"
  ]
}
```

- [ ] **Step 4: Create the 1.20.1 item tag files**

`.../items/ores.json`, `.../items/sandstone_blocks.json`, `.../items/sandstone_slabs.json` and `.../items/sandstone_stairs.json` get content identical to their `blocks/` counterparts from Step 3.

`.../items/ingots.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone",
    "chronodawn:enhanced_clockstone"
  ]
}
```

`.../items/gems.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_crystal",
    "chronodawn:entropy_crystal"
  ]
}
```

`.../items/raw_ores.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:raw_temporal_amber"
  ]
}
```

`.../items/dusts.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:temporal_amber_dust"
  ]
}
```

`.../items/foods.json`:

```json
{
  "replace": false,
  "values": [
    "chronodawn:time_bread",
    "chronodawn:enhanced_time_bread",
    "chronodawn:time_wheat_cookie",
    "chronodawn:clockwork_cookie",
    "chronodawn:time_fruit_pie",
    "chronodawn:fruit_of_time",
    "chronodawn:chrono_melon_slice",
    "chronodawn:temporal_root",
    "chronodawn:baked_temporal_root",
    "chronodawn:temporal_root_stew",
    "chronodawn:timeless_mushroom_soup",
    "chronodawn:chrono_bovine_meat",
    "chronodawn:cooked_chrono_bovine_meat",
    "chronodawn:glide_fish",
    "chronodawn:cooked_glide_fish",
    "chronodawn:golden_time_wheat",
    "chronodawn:glistening_chrono_melon"
  ]
}
```

- [ ] **Step 5: Run the validator and verify it passes**

```bash
./gradlew validateConventionalTags
```

Expected: PASS, with the summary line now reporting 13 v1 tag files.

- [ ] **Step 6: Commit**

```bash
git add scripts/conventional_tag_rules.json common/1.20.1/src/main/resources/data/c
git commit -m "feat(tags): publish conventional tags v1 set for 1.20.1"
```

---

### Task 6: Documentation and full verification

**Files:**
- Modify: `docs/modpack-integration.md`
- Modify: `docs/developer_guide.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes: the finished tag set from Tasks 1–5.
- Produces: nothing consumed by later tasks.

- [ ] **Step 1: Document the published tags**

Add a section `## Conventional (`c:`) tags` to `docs/modpack-integration.md` listing, in two tables (one for 1.21.1+, one for 1.20.1), every tag published and its members. Source the content from the spec's §4 and §5 tables rather than re-deriving it. State explicitly that 1.20.1 carries a reduced set because conventional tags v1 has no per-material subtags.

- [ ] **Step 2: List the new validation task**

In `docs/developer_guide.md`, add `validateConventionalTags` to the validation-task list alongside `validateData` / `validateEraJsonFormat`, described as "Verify conventional (`c:`) tag coverage, tag-name validity and cross-root consistency". Also add it to the `checkAll` sequence description if that document enumerates the steps.

- [ ] **Step 3: Add a changelog entry**

Under the Unreleased section of `CHANGELOG.md`, add under `### Added`:

```markdown
- Conventional (`c:`) tags for ores, materials, storage blocks, stone/sand/gravel/sandstone, wood sets, foods, crops and seeds, so other mods and modpack recipes can consume Chrono Dawn materials by tag
```

- [ ] **Step 4: Run the full verification pipeline**

```bash
./gradlew checkAll
```

Expected: PASS for every step, including the new `validateConventionalTags`. This is the authoritative check that the new `data/c/` namespace does not break resource validation, builds, unit tests or GameTests on any of the 12 versions. Budget time — this is the long run.

If the wrapper reports a spurious failure in `buildAll` or `gameTestAll`, re-run the specific version standalone (`./gradlew clean build -Ptarget_mc_version=<ver> -x test`) to get the real compiler output before concluding anything.

- [ ] **Step 5: Commit**

```bash
git add docs/modpack-integration.md docs/developer_guide.md CHANGELOG.md
git commit -m "docs(tags): document published conventional tags"
```

---

## Notes for the executor

- The validator resolves `#c:` references transitively. That is why an ore listed only in `c:ores/clockstone` still satisfies a rule requiring membership in `c:ores` — provided `c:ores` references `#c:ores/clockstone`.
- Item and block tag files for the same material are usually byte-identical, because a block and its item share an ID. They still must both exist: recipes match item tags, worldgen and mining logic match block tags.
- Never add a tag name that is not in the corresponding allowlist. If a genuinely new conventional tag is needed, verify it exists in the Fabric API source for the target version first, then add it to the allowlist in the same commit.
