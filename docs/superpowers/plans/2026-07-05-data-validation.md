# validateData Data-Pack Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add build-time validation that catches ChronoDawn's silent data-pack failure classes: unregistered IDs in tags and recipes, missing 1.21.4+ client item JSONs, wrong-era recipe ingredients — and re-arm the currently no-op `validateTranslations`.

**Architecture:** A new property-driven Gradle script `data-validation.gradle` in the `gradle/shared` submodule registers a `validateData` task with three checks (tags→IDs, recipe references→IDs, client-items coverage). A fourth check (ingredient object↔string era rule) extends the existing project-local `validateEraJsonFormat` in `gradle/chronodawn-validation.gradle`. Configuration lives in ChronoDawn's `gradle.properties`.

**Tech Stack:** Gradle Groovy DSL scripts (`apply from` style), `groovy.json.JsonSlurper`, regex extraction of IDs from Java enum files. No new dependencies.

**Design spec:** `docs/superpowers/specs/2026-07-05-data-validation-design.md`

## Global Constraints

- Two repos are involved: `gradle/shared/` is a **git submodule** (`minecraft-mod-gradle-scripts`). Files under `gradle/shared/` are committed **inside** that directory (`cd gradle/shared` first); everything else is committed in the ChronoDawn root. Never run `git -C` (blocked by a hook) — `cd` in a separate Bash call, then run git.
- ChronoDawn commits that depend on submodule changes must also stage the submodule pointer: `git add gradle/shared`.
- Do NOT push either repo. Pushing is done manually by the maintainer.
- Commit messages: English, Conventional Commits, ending with `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.
- Note for executors: `git commit` may fail with a GPG keyring permission error under the sandbox; if so, report back instead of retrying with `--no-gpg-sign` (never disable signing).
- Shell hook blocks compound commands and pipes; run commands one per Bash call, or append ` #allow-compound` when a pipe is unavoidable.
- Groovy DSL style: follow `gradle/shared/resource-validation.gradle` conventions (script-level `def` helper methods, `logger.lifecycle` progress lines, collect-all-errors-then-`GradleException`).
- All code comments and docs in English.
- There is no unit-test harness for `apply from` scripts. The test cycle for each check is: **baseline run (must pass on the real tree after triage) → seeded-error run (must fail) → revert seed → pass**.
- Run all Gradle commands from the ChronoDawn root: `/Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn`. Verify with `pwd` before the first command of each task.

---

### Task 1: Re-arm validateTranslations

The submodule's `validateTranslations` reads `entity_id_file` / `item_id_file` project properties. Neither is set anywhere in ChronoDawn, so the task currently validates **zero** entities/items.

**Files:**
- Modify: `gradle.properties` (ChronoDawn root)
- Possibly modify (baseline triage): lang files under `common/*/src/main/resources/assets/chronodawn/lang/`, or add `excluded_entities` to `gradle.properties`

**Interfaces:**
- Produces: `entity_id_file`, `item_id_file` properties — Task 2's script reads the same properties for ID extraction.

- [ ] **Step 1: Confirm the no-op baseline**

Run: `./gradlew validateTranslations`
Expected output contains: `Found 0 entities and 0 items to validate` — confirming the no-op.

- [ ] **Step 2: Add the ID file properties**

In `gradle.properties`, directly above the existing `checkall_extra_tasks=` line, add:

```properties
# ID enum files consumed by validateTranslations (submodule) and
# validateData (submodule): single source of truth for registered IDs.
entity_id_file=common/shared/src/main/java/com/chronodawn/registry/ModEntityId.java
item_id_file=common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
```

- [ ] **Step 3: Run and triage**

Run: `./gradlew validateTranslations`
Expected: `Found N entities and M items to validate` with N > 0 and M > 0. The run may now FAIL with missing-translation errors. Triage each error:

- Technical, never-player-visible entities (projectiles, marker entities): add the entity ID to a new `excluded_entities=` property in `gradle.properties` (comma-separated), with a `#` comment line above naming why.
- Player-visible entities/spawn eggs: add the missing `entity.chronodawn.<id>` / `item.chronodawn.<id>_spawn_egg` keys to `en_us.json` AND `ja_jp.json` in every lang root that has the sibling keys (`common/shared-1.21.2+`, `common/1.21.1`, `common/1.20.1`). Use the Edit tool with anchor lines — do not rewrite lang files with a JSON serializer (blank-line section grouping must survive).

Re-run until: `Cross-version translation validation passed.`

- [ ] **Step 4: Verify lang parity still passes**

Run: `./gradlew validateLangParity`
Expected: PASS (exit 0). If it fails, the ja_jp additions from Step 3 are incomplete — fix and re-run.

- [ ] **Step 5: Commit (ChronoDawn root)**

```bash
git add gradle.properties
# plus any lang files touched in Step 3
git commit -m "fix(build): arm validateTranslations with ID enum file properties

entity_id_file/item_id_file were never set, so validateTranslations has
been validating zero entities and items since it was introduced.

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: data-validation.gradle skeleton + Check A (tag entries → registered IDs)

**Files:**
- Create: `gradle/shared/data-validation.gradle` (submodule)
- Modify: `build.gradle` (ChronoDawn root, near line 131 where `resource-validation.gradle` is applied)
- Modify: `gradle.properties` (add `block_id_file`)

**Interfaces:**
- Consumes: `entity_id_file`, `item_id_file` properties from Task 1.
- Produces: task `validateData`; script-level helpers `extractDataEnumValues(File)` and `walkDataStrings(Object, String, Closure)` (walkDataStrings added in Task 3); properties `block_id_file`, `data_validation_excluded_ids`. Tasks 3 and 4 append checks inside the same `doLast` block.

- [ ] **Step 1: Create the script with plumbing + Check A**

Create `gradle/shared/data-validation.gradle`:

```groovy
// Data-pack cross-reference validation task.
//
// Validates that data JSONs reference registered IDs, and that 1.21.4+
// client item definitions (assets/<ns>/items/<id>.json) cover every
// item-form ID. Complements resource-validation.gradle, which covers
// assets (blockstates/models/textures); this script covers data/ and the
// 1.21.4+ client items directory.
//
// Required project properties (gradle.properties):
//   archives_name  - The mod's archive name (namespace in data paths)
//
// Optional project properties (a check is skipped unless its properties
// are set, so consumers opt in per check):
//   item_id_file   - Item ID enum file (relative to project root)
//   block_id_file  - Block ID enum file
//   entity_id_file - Entity ID enum file
//   data_validation_excluded_ids - Comma-separated IDs exempt from the
//                        tag and recipe reference checks
//   client_items_roots - Comma-separated resource roots (relative to
//                        project root) that must each contain
//                        assets/<ns>/items/<id>.json for every item-form
//                        ID (1.21.4+ client item format)
//   client_items_excluded_ids - Comma-separated basenames exempt from
//                        the client items check (e.g. variant-only item
//                        models, version-gated items)

import groovy.json.JsonSlurper

tasks.register('validateData') {
    group = 'verification'
    description = 'Validates data-pack cross-references (tag entries, recipe references, client items coverage)'

    doLast {
        def archivesName = project.property('archives_name').toString()
        def jsonSlurper = new JsonSlurper()
        def errors = []

        // --- shared plumbing ------------------------------------------------

        // All resource roots: common/shared*, common/<version>, common/gametest.
        def roots = []
        file("${rootProject.projectDir}/common").listFiles()?.findAll {
            it.isDirectory() && file("${it}/src/main/resources").exists()
        }?.sort()?.each {
            roots << file("${it}/src/main/resources")
        }

        def readIds = { String propName ->
            def ids = [] as Set
            if (project.hasProperty(propName)) {
                ids.addAll(extractDataEnumValues(file("${rootProject.projectDir}/${project.property(propName)}")))
            }
            ids
        }
        def itemIds = readIds('item_id_file')
        def blockIds = readIds('block_id_file')
        def entityIds = readIds('entity_id_file')
        def itemAndBlockIds = (itemIds + blockIds) as Set

        def excludedIds = [] as Set
        if (project.hasProperty('data_validation_excluded_ids')) {
            excludedIds.addAll(project.property('data_validation_excluded_ids').toString().split(',').collect { it.trim() })
        }

        def relPath = { File f -> rootProject.projectDir.toPath().relativize(f.toPath()).toString() }

        // A referenced tag file may live under the singular or plural era
        // spelling of its type directory (tags/item vs tags/items).
        def tagTypeVariants = { String type ->
            [type, type.endsWith('s') ? type[0..-2] : "${type}s".toString()] as Set
        }
        def tagFileExists = { String type, String path ->
            roots.any { r ->
                tagTypeVariants(type).any { t ->
                    file("${r}/data/${archivesName}/tags/${t}/${path}.json").exists()
                }
            }
        }

        // --- Check A: tag entries -> registered IDs -------------------------
        logger.lifecycle("Checking tag entries -> registered IDs...")
        def tagTypeSources = [
            'item': itemAndBlockIds, 'items': itemAndBlockIds,
            'block': blockIds, 'blocks': blockIds,
            'entity_type': entityIds, 'entity_types': entityIds,
        ]
        int tagEntriesChecked = 0
        int tagEntriesSkipped = 0
        if (itemAndBlockIds || entityIds) {
            roots.each { root ->
                def dataDir = file("${root}/data")
                if (!dataDir.exists()) return
                dataDir.eachDir { nsDir ->
                    def tagsDir = file("${nsDir}/tags")
                    if (!tagsDir.exists()) return
                    tagsDir.eachDir { typeDir ->
                        def source = tagTypeSources[typeDir.name]
                        fileTree(dir: typeDir, includes: ['**/*.json']).each { File tagFile ->
                            def json
                            try { json = jsonSlurper.parseText(tagFile.text) } catch (Exception e) { return }
                            if (!(json instanceof Map) || !(json.values instanceof List)) return
                            json.values.each { entry ->
                                def raw = (entry instanceof Map) ? entry.id : entry
                                if (!(raw instanceof String)) return
                                if (raw.startsWith("#${archivesName}:")) {
                                    tagEntriesChecked++
                                    def refPath = raw.substring("#${archivesName}:".length())
                                    if (!tagFileExists(typeDir.name, refPath)) {
                                        errors << "Tag ${relPath(tagFile)}: referenced tag '${raw}' has no tag file in any resource root"
                                    }
                                } else if (raw.startsWith("${archivesName}:")) {
                                    if (source == null) { tagEntriesSkipped++; return }
                                    def id = raw.substring("${archivesName}:".length())
                                    if (excludedIds.contains(id)) return
                                    tagEntriesChecked++
                                    if (!source.contains(id)) {
                                        errors << "Tag ${relPath(tagFile)}: entry '${raw}' is not a registered ${typeDir.name} ID"
                                    }
                                }
                                // Other namespaces (minecraft:, c:, ...) are
                                // skipped: no static registry knowledge.
                            }
                        }
                    }
                }
            }
        }
        logger.lifecycle("  Checked ${tagEntriesChecked} tag entries (${tagEntriesSkipped} skipped: unsupported tag types)")

        // --- report ---------------------------------------------------------
        if (errors.isEmpty()) {
            logger.lifecycle("Data validation passed.")
        } else {
            errors.each { logger.error("  ERROR: ${it}") }
            throw new GradleException("Data validation failed with ${errors.size()} error(s)")
        }
    }
}

// Extract enum values from a ModXxxId.java file.
// Matches both NAME("id") and NAME(def("id")) shapes.
def extractDataEnumValues(File file) {
    def ids = []
    if (!file.exists()) return ids
    def matcher = (file.text =~ /(\w+)\s*\(\s*"([^"]+)"\s*\)/)
    matcher.each { ids << it[2] }
    return ids
}
```

- [ ] **Step 2: Wire into ChronoDawn**

In `build.gradle`, directly below the line `apply from: 'gradle/shared/resource-validation.gradle'` (line 131), add:

```groovy
apply from: 'gradle/shared/data-validation.gradle'
```

In `gradle.properties`, directly below the `item_id_file=` line added in Task 1, add:

```properties
block_id_file=common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
```

- [ ] **Step 3: Baseline run and triage**

Run: `./gradlew validateData`
Expected output shape:

```
Checking tag entries -> registered IDs...
  Checked N tag entries (K skipped: unsupported tag types)
Data validation passed.
```

with N > 100 (the tree has many tag entries). If N is 0, the root discovery or ID extraction broke — debug before proceeding. If it FAILS: triage each error — a genuinely wrong tag entry is fixed in the tag JSON; an intentionally unregistered ID goes into `data_validation_excluded_ids=` in `gradle.properties` with a `#` comment naming why.

- [ ] **Step 4: Seeded-error verification**

Add a bogus entry `"chronodawn:definitely_not_a_block"` to the `values` array of `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/replaceable.json`.
Run: `./gradlew validateData`
Expected: FAIL with `entry 'chronodawn:definitely_not_a_block' is not a registered block ID`.
Revert the seeded change (`git checkout -- 'common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/replaceable.json'`).
Run: `./gradlew validateData` again. Expected: PASS.

- [ ] **Step 5: Commit — submodule first, then ChronoDawn**

```bash
cd gradle/shared
git add data-validation.gradle
git commit -m "feat: add validateData task (tag entry -> registered ID check)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
cd ../..
git add build.gradle gradle.properties gradle/shared
# plus any tag fixes from Step 3
git commit -m "feat(build): wire validateData tag entry validation

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

(Each `cd` must be its own Bash call per the hook constraints.)

---

### Task 3: Check B (recipe references → registered IDs)

**Files:**
- Modify: `gradle/shared/data-validation.gradle` (submodule)
- Possibly modify (baseline triage): recipe JSONs, `gradle.properties` exclusions

**Interfaces:**
- Consumes: `itemAndBlockIds`, `excludedIds`, `relPath`, `tagFileExists` from Task 2's `doLast`; adds script-level `walkDataStrings`.
- Produces: recipe-reference validation inside `validateData`.

- [ ] **Step 1: Add the walker helper**

In `gradle/shared/data-validation.gradle`, directly above the `def extractDataEnumValues(File file)` method at the bottom, add:

```groovy
// Walk every string value in a JSON tree, reporting the nearest map key.
def walkDataStrings(Object node, String parentKey, Closure visitor) {
    if (node instanceof Map) {
        node.each { k, v -> walkDataStrings(v, k as String, visitor) }
    } else if (node instanceof List) {
        node.each { walkDataStrings(it, parentKey, visitor) }
    } else if (node instanceof String) {
        visitor.call(parentKey, node)
    }
}
```

- [ ] **Step 2: Add Check B inside doLast**

In the same file, between the `logger.lifecycle("  Checked ${tagEntriesChecked} ...")` line and the `// --- report ---` comment, insert:

```groovy
        // --- Check B: recipe references -> registered IDs --------------------
        logger.lifecycle("Checking recipe references -> registered IDs...")
        int recipeRefsChecked = 0
        if (itemAndBlockIds) {
            roots.each { root ->
                def dataDir = file("${root}/data")
                if (!dataDir.exists()) return
                dataDir.eachDir { nsDir ->
                    ['recipe', 'recipes'].each { sub ->
                        def recipeDir = file("${nsDir}/${sub}")
                        if (!recipeDir.exists()) return
                        fileTree(dir: recipeDir, includes: ['**/*.json']).each { File f ->
                            def json
                            try { json = jsonSlurper.parseText(f.text) } catch (Exception e) { return }
                            walkDataStrings(json, null) { String key, String value ->
                                if (key == 'type') return
                                if (value.startsWith("#${archivesName}:")) {
                                    recipeRefsChecked++
                                    def refPath = value.substring("#${archivesName}:".length())
                                    if (!tagFileExists('item', refPath)) {
                                        errors << "Recipe ${relPath(f)}: ingredient tag '${value}' has no tag file in any resource root"
                                    }
                                } else if (value.startsWith("${archivesName}:")) {
                                    def id = value.substring("${archivesName}:".length())
                                    if (excludedIds.contains(id)) return
                                    recipeRefsChecked++
                                    if (!itemAndBlockIds.contains(id)) {
                                        errors << "Recipe ${relPath(f)}: reference '${value}' (key '${key}') is not a registered item/block ID"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        logger.lifecycle("  Checked ${recipeRefsChecked} recipe references")
```

- [ ] **Step 3: Baseline run and triage**

Run: `./gradlew validateData`
Expected: a new line `Checked N recipe references` with N > 200 (three recipe roots), then `Data validation passed.` If N is 0, the recipe directory discovery broke — debug. On FAIL, triage as in Task 2 Step 3 (fix real typos; exclusions only with a comment).

- [ ] **Step 4: Seeded-error verification**

In `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/time_bread.json`, change `"chronodawn:time_wheat"` to `"chronodawn:time_wheatt"`.
Run: `./gradlew validateData`
Expected: FAIL with `reference 'chronodawn:time_wheatt' (key 'W') is not a registered item/block ID`.
Revert: `git checkout -- 'common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/time_bread.json'`
Run again. Expected: PASS.

- [ ] **Step 5: Commit (submodule, then ChronoDawn if triage touched project files)**

```bash
cd gradle/shared
git add data-validation.gradle
git commit -m "feat: add recipe reference check to validateData

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
cd ../..
git add gradle/shared
# plus any recipe/properties fixes from Step 3
git commit -m "feat(build): enable validateData recipe reference check

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 4: Check C (client items JSON coverage, 1.21.4+)

**Files:**
- Modify: `gradle/shared/data-validation.gradle` (submodule)
- Modify: `gradle.properties` (add `client_items_roots`, likely `client_items_excluded_ids`)
- Possibly create (baseline triage): missing `assets/chronodawn/items/<id>.json` files

**Interfaces:**
- Consumes: `itemIds`, `roots`, `archivesName`, `errors` from Task 2's `doLast`.
- Produces: client-items validation inside `validateData`, gated on `client_items_roots`.

- [ ] **Step 1: Add Check C inside doLast**

In `gradle/shared/data-validation.gradle`, between the `logger.lifecycle("  Checked ${recipeRefsChecked} ...")` line (Task 3) and the `// --- report ---` comment, insert:

```groovy
        // --- Check C: client items JSON coverage (1.21.4+) -------------------
        if (project.hasProperty('client_items_roots')) {
            logger.lifecycle("Checking client items JSON coverage...")
            def clientRoots = project.property('client_items_roots').toString().split(',').collect {
                file("${rootProject.projectDir}/${it.trim()}")
            }
            def clientExcluded = [] as Set
            if (project.hasProperty('client_items_excluded_ids')) {
                clientExcluded.addAll(project.property('client_items_excluded_ids').toString().split(',').collect { it.trim() })
            }
            // Item-form IDs: registered items plus every item model basename.
            // The model union pulls in BlockItems; item-less blocks (wall
            // torches, crops, plant stems) have no item model, so they do not
            // produce false positives.
            def sourceIds = new TreeSet(itemIds)
            roots.each { r ->
                def mdir = file("${r}/assets/${archivesName}/models/item")
                if (mdir.exists()) {
                    fileTree(dir: mdir, includes: ['*.json']).each { sourceIds << it.name.replace('.json', '') }
                }
            }
            sourceIds.removeAll(clientExcluded)
            int clientChecked = 0
            clientRoots.each { cr ->
                def itemsDir = file("${cr}/assets/${archivesName}/items")
                def crRel = rootProject.projectDir.toPath().relativize(cr.toPath()).toString()
                sourceIds.each { id ->
                    clientChecked++
                    if (!file("${itemsDir}/${id}.json").exists()) {
                        errors << "Client items: '${id}' has no ${crRel}/assets/${archivesName}/items/${id}.json"
                    }
                }
                if (itemsDir.exists()) {
                    fileTree(dir: itemsDir, includes: ['*.json']).each { File f ->
                        def base = f.name.replace('.json', '')
                        if (!sourceIds.contains(base) && !clientExcluded.contains(base)) {
                            errors << "Client items: orphan ${crRel}/assets/${archivesName}/items/${f.name} does not correspond to any known item ID"
                        }
                    }
                }
            }
            logger.lifecycle("  Checked ${clientChecked} client item definitions across ${clientRoots.size()} roots (${sourceIds.size()} item-form IDs)")
        }
```

The forward check plus the orphan check together also enforce parity between the two roots, so no separate parity pass is needed.

- [ ] **Step 2: Configure ChronoDawn**

In `gradle.properties`, below `block_id_file=`, add:

```properties
# Resource roots that must carry assets/chronodawn/items/<id>.json for every
# item-form ID (1.21.4+ client item definitions; missing file = purple-black icon).
client_items_roots=common/1.21.4/src/main/resources,common/shared-1.21.5+/src/main/resources
```

- [ ] **Step 3: Baseline run and triage**

Run: `./gradlew validateData`
Expected: `Checked N client item definitions across 2 roots (M item-form IDs)` with M > 100. Likely findings and their dispositions:

- Variant-only item models (e.g. `chrono_shield_blocking`): add to a new `client_items_excluded_ids=` property with a `#` comment.
- Version-gated items that legitimately don't exist in a root's version range: add to `client_items_excluded_ids` with a comment naming the gate.
- Anything else missing: a **real bug** — create the missing `items/<id>.json` by copying the pattern of a sibling item of the same kind (plain item vs block item), e.g. `common/1.21.4/src/main/resources/assets/chronodawn/items/time_bread.json`.

Re-run until PASS.

- [ ] **Step 4: Seeded-error verification**

Move `common/1.21.4/src/main/resources/assets/chronodawn/items/time_bread.json` aside: `mv <that path> $TMPDIR/`.
Run: `./gradlew validateData`
Expected: FAIL with `'time_bread' has no common/1.21.4/.../items/time_bread.json`.
Restore: `git checkout -- 'common/1.21.4/src/main/resources/assets/chronodawn/items/time_bread.json'`
Run again. Expected: PASS.

- [ ] **Step 5: Commit (submodule, then ChronoDawn)**

```bash
cd gradle/shared
git add data-validation.gradle
git commit -m "feat: add client items JSON coverage check to validateData

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
cd ../..
git add gradle.properties gradle/shared
# plus any items/*.json created in Step 3
git commit -m "feat(build): enable client items coverage validation

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 5: Ingredient era rule in validateEraJsonFormat

**Files:**
- Modify: `gradle/chronodawn-validation.gradle` (ChronoDawn project — NOT the submodule)

**Interfaces:**
- Consumes: existing `roots` map, recipe-scan loop, and `errors` list inside the `validateEraJsonFormat` task (lines ~227–288).
- Produces: three-tier ingredient shape validation (legacy/mid object vs modern string).

- [ ] **Step 1: Add the modern-ingredient root set**

In `gradle/chronodawn-validation.gradle`, inside the `validateEraJsonFormat` task's `doLast`, directly after the `def roots = [...]` map closes (after the line `'common/1.21.11': 'modern',` and its closing `]`), add:

```groovy
        // Recipe ingredient shape has three tiers, not two: 1.20.1 and 1.21.1
        // both use object ingredients ({"item": ...} / {"tag": ...}); 1.21.2+
        // uses plain strings ("ns:id" / "#ns:tag"). result.item vs result.id
        // already splits at 1.21.1 and stays as-is below.
        def modernIngredientRoots = [
            'common/shared-1.21.2+', 'common/1.21.2', 'common/1.21.4',
            'common/1.21.5', 'common/1.21.6', 'common/1.21.7', 'common/1.21.8',
            'common/1.21.9', 'common/1.21.10', 'common/1.21.11',
        ] as Set
```

- [ ] **Step 2: Add the ingredient shape check to the recipe loop**

In the same task, inside the recipe `fileTree(...).each { File f -> ... }` block, after the existing `result` era checks (after the closing brace of `if (result instanceof Map) { ... }`), add:

```groovy
                    // Ingredient shape: object (1.20.1 / 1.21.1) vs string (1.21.2+).
                    def stringIngredients = modernIngredientRoots.contains(rootRel)
                    def ingredientNodes = []
                    if (json.key instanceof Map) ingredientNodes.addAll(json.key.values())
                    ['ingredients', 'ingredient', 'base', 'addition', 'template'].each { k ->
                        if (json.containsKey(k)) ingredientNodes << json[k]
                    }
                    def flatIngredients = []
                    ingredientNodes.each { n ->
                        (n instanceof List) ? flatIngredients.addAll(n) : (flatIngredients << n)
                    }
                    flatIngredients.each { entry ->
                        def relIng = rootProject.projectDir.toPath().relativize(f.toPath()).toString()
                        if (stringIngredients) {
                            if (entry instanceof Map && (entry.containsKey('item') || entry.containsKey('tag'))) {
                                errors << "${relIng}: object ingredient in 1.21.2+ root (expected plain string): ${entry}"
                            }
                        } else {
                            if (entry instanceof String) {
                                errors << "${relIng}: string ingredient '${entry}' in pre-1.21.2 root (expected object with item/tag)"
                            }
                        }
                    }
```

- [ ] **Step 3: Baseline run**

Run: `./gradlew validateEraJsonFormat`
Expected: `Era JSON format validation passed: N JSON files ...` (N unchanged from before, > 500). On FAIL: each finding is a real wrong-era ingredient — fix the recipe JSON to the era shape shown in the table in the design spec (`docs/superpowers/specs/2026-07-05-data-validation-design.md` § Component 2).

- [ ] **Step 4: Seeded-error verification**

In `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/time_bread.json`, change `"W": "chronodawn:time_wheat"` to `"W": { "item": "chronodawn:time_wheat" }`.
Run: `./gradlew validateEraJsonFormat`
Expected: FAIL with `object ingredient in 1.21.2+ root`.
Revert: `git checkout -- 'common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/time_bread.json'`

Then seed the inverse: in `common/1.21.1/src/main/resources/data/chronodawn/recipe/time_bread.json`, change `"W": { "item": "chronodawn:time_wheat" }` to `"W": "chronodawn:time_wheat"`.
Run: `./gradlew validateEraJsonFormat`
Expected: FAIL with `string ingredient 'chronodawn:time_wheat' in pre-1.21.2 root`.
Revert: `git checkout -- 'common/1.21.1/src/main/resources/data/chronodawn/recipe/time_bread.json'`
Run again. Expected: PASS.

- [ ] **Step 5: Commit (ChronoDawn root)**

```bash
git add gradle/chronodawn-validation.gradle
git commit -m "feat(build): validate recipe ingredient shape per era

validateEraJsonFormat treated all 1.21.x roots as one era; ingredient
object-vs-string actually splits at 1.21.2.

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 6: checkAll wiring + documentation

**Files:**
- Modify: `gradle.properties` (append to `checkall_extra_tasks`)
- Modify: `gradle/shared/README.md` (submodule, file table)
- Modify: `CLAUDE.md` (Resource Validation command list)
- Modify: `docs/superpowers/specs/2026-07-05-flagship-parity-approach-outlines.md` (P3-1, P6-1)
- Modify: `docs/superpowers/specs/2026-07-05-flagship-parity-roadmap.md` (P3, P6 tables)

**Interfaces:**
- Consumes: `validateData` task from Tasks 2–4.
- Produces: `validateData` runs as part of `checkAll`; documentation reflects shipped state.

- [ ] **Step 1: Append to checkall_extra_tasks**

In `gradle.properties`, change:

```properties
checkall_extra_tasks=validateLangParity,validateBlockTagMembership,validateRecipeUnlockAdvancements,validateEraJsonFormat
```

to:

```properties
checkall_extra_tasks=validateLangParity,validateBlockTagMembership,validateRecipeUnlockAdvancements,validateEraJsonFormat,validateData
```

Verify the task resolves: run `./gradlew validateData` — expected PASS. (A full `checkAll` run takes hours and is left to the maintainer's normal pre-release routine; the extra-tasks mechanism is already proven by the four existing entries.)

- [ ] **Step 2: Submodule README**

In `gradle/shared/README.md`, in the Files table, after the `resource-validation.gradle` row, add:

```markdown
| `data-validation.gradle` | Data-pack cross-reference checks (tag entries, recipe references, 1.21.4+ client items coverage) |
```

- [ ] **Step 3: CLAUDE.md command list**

In ChronoDawn `CLAUDE.md`, in the **Resource Validation** section, after the `validateResources` line, add:

```markdown
- `./gradlew validateData` - Check data-pack cross-references (tag entries → registered IDs, recipe references, 1.21.4+ client items coverage)
```

- [ ] **Step 4: Roadmap document corrections**

In `docs/superpowers/specs/2026-07-05-flagship-parity-approach-outlines.md`:

- In the P3-1 section, after the "**Direction.**" paragraph's bullet list, add a status note:

```markdown
**Status (2026-07-05).** Discovery: `gradle/chronodawn-validation.gradle`
already provided `validateEraJsonFormat`, `validateBlockTagMembership`,
`validateRecipeUnlockAdvancements`, and `validateLangParity`. The first
slice shipped as the submodule `validateData` task (tag entries → IDs,
recipe references → IDs, client items coverage) plus an ingredient era
rule in `validateEraJsonFormat`, and re-armed `validateTranslations`
(its ID-file properties had never been set). Remaining from the
Direction list: loot-table/advancement references, sounds/subtitle
parity, GameTest runtime assertions.
```

- In the P6-1 section, replace the whole **Direction** paragraph with:

```markdown
**Status (2026-07-05).** Already shipped before this roadmap was written:
`validateLangParity` (in `gradle/chronodawn-validation.gradle`) wraps
`scripts/check_lang_parity.py` and runs in `checkAll` via
`checkall_extra_tasks`. No further work needed.
```

In `docs/superpowers/specs/2026-07-05-flagship-parity-roadmap.md`:

- In the P3 table, in the "Local verification coverage expansion" row's Notes cell, append: `First slice shipped 2026-07-05 (validateData + ingredient era rule; see the data-validation design spec).`
- In the P6 table, in the parity-gate row's Notes cell, replace with: `Already shipped: validateLangParity task runs in checkAll (discovered 2026-07-05).`

- [ ] **Step 5: Commit (submodule, then ChronoDawn)**

```bash
cd gradle/shared
git add README.md
git commit -m "docs: add data-validation.gradle to file table

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
cd ../..
git add gradle.properties CLAUDE.md docs/superpowers/specs/2026-07-05-flagship-parity-approach-outlines.md docs/superpowers/specs/2026-07-05-flagship-parity-roadmap.md gradle/shared
git commit -m "feat(build): run validateData in checkAll; update docs

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 7: Final verification sweep

**Files:** none (verification only)

- [ ] **Step 1: Run every validation task**

Run each, expecting PASS from all:

```
./gradlew validateResources
./gradlew validateTranslations
./gradlew validateLangParity
./gradlew validateBlockTagMembership
./gradlew validateRecipeUnlockAdvancements
./gradlew validateEraJsonFormat
./gradlew validateData
```

- [ ] **Step 2: Confirm repo states**

Run `git status` in the ChronoDawn root — expected: clean tree.
Run `git status` in `gradle/shared` — expected: clean tree, 2–4 commits ahead of origin.
Run `git log --oneline -8` in both and report the commit list to the maintainer.

- [ ] **Step 3: Report**

Report to the maintainer: baseline findings fixed (list them), exclusions added (list with reasons), and a reminder that the submodule commits need a manual push to GitHub (and the local sibling clone) before the ChronoDawn pointer commit is pushed.
