# Design: Migrate NBT Structure Conversion to the Shared Gradle Plugin

**Created**: 2026-08-19
**Status**: Approved — ready for implementation planning
**Type**: Build infrastructure

---

## 1. Problem

Building for Minecraft 1.20.1 requires Python 3 with the `nbtlib` package.
`common/1.20.1/build.gradle` registers an `Exec` task, `convertNbtStructures`,
that shells out to `gradle/shared/convert_nbt_1_21_to_1_20.py` to downconvert
the mod's `.nbt` structure files from 1.21 format to 1.20.1 format. Without
`nbtlib` the 1.20.1 build fails outright:

```
Execution failed for task ':common-1.20.1:convertNbtStructures'.
> Python 3 with nbtlib is required for NBT conversion. Install with: pip3 install nbtlib
```

This is easy to miss: the conversion output is cached under
`build/generated/nbt-structures/`, so the task reports `UP-TO-DATE` until
something wipes it. `checkAll` starts with `cleanAll`, so a full verification
run is exactly when the missing dependency surfaces.

The dependency is also avoidable. The shared submodule (`gradle/shared`,
minecraft-mod-gradle-scripts) already contains a JVM implementation of the same
conversion — `V1_21ToV1_20NbtConverter.groovy`, built on the Querz NBT library,
wired up by `NbtConversionTasks.groovy` and covered by Spock tests. The Groovy
implementation is the primary one: the entity `Item`/`Items` conversion fix
landed there on 2026-03-31 and was backported to the Python script on
2026-04-29.

ChronoDawn does not consume it, because it uses `gradle/shared` only through
script plugins (`apply from: '…/multi-version-tasks.gradle'` and friends),
never through the compiled `McmodPlugin`.

## 2. Goal

Building any supported Minecraft version requires no Python interpreter and no
Python packages. Structure conversion runs on the JVM, from the implementation
the shared submodule already maintains and tests.

## 3. Scope

In scope:

- Consume `gradle/shared`'s compiled plugin through an included build.
- Replace the `Exec`-based `convertNbtStructures` in `common/1.20.1` with the
  plugin's `convertNbt` task.
- Delete the copy-pasted `convertNbtStructures` definition from the other ten
  `common/<version>/build.gradle` files, where it is dead code (each is guarded
  by `onlyIf { minecraft_version == '1.20.1' }` and can never run).
- Give every common module one stable task name for "structures are ready", so
  the loader modules stop naming a task that exists in only one of them. See
  §5.1.

Out of scope:

- Deleting `convert_nbt_1_21_to_1_20.py` from the upstream shared repository.
  That is a change to a different repository and would force a submodule bump;
  the script becoming unused here does not require it to disappear there.
- Migrating the other script plugins (`multi-version-tasks`,
  `resource-validation`, `data-validation`, `release-*`) to the compiled
  plugin. Worth doing eventually, but each carries its own task-name collision
  work and its own full `checkAll` cycle.
- `replaceNbtBlocks`. It stays in `buildSrc`, because its block mapping
  (`scripts/nbt_block_mappings.json`) is ChronoDawn-specific.

## 4. Consuming the plugin

`settings.gradle` already has a `pluginManagement` block. Add the shared build
to it:

```groovy
pluginManagement {
    includeBuild 'gradle/shared'
    repositories {
        // unchanged
    }
}
```

`gradle/shared/build.gradle` applies `java-gradle-plugin` and declares the id
`com.github.ksoichiro.mcmod`, so no change is needed on the submodule side.
Gradle compiles the plugin as part of the build; it is small, so the cost is a
few seconds on a cold build and nothing on a warm one.

**Uninitialised submodule.** `build.gradle` currently checks for
`gradle/shared/multi-version-tasks.gradle` and fails with an instruction to run
`git submodule update --init`. `includeBuild` is evaluated earlier, during
settings evaluation, so an uninitialised submodule would now fail with a less
helpful message. Add the same existence check to `settings.gradle`, before the
`includeBuild` line, carrying the same instruction.

**Task-name collisions.** `McmodPlugin` registers each feature behind an
`enabled` flag. `multiVersion` and `resourceValidation` default to `true`, and
ChronoDawn already gets both from script plugins. The plugin is therefore
applied with only `nbtConversion` enabled:

```groovy
mcmod {
    multiVersion { enabled = false }
    resourceValidation { enabled = false }
    nbtConversion { /* see below */ }
}
```

`prodRun`, `releaseModrinth` and `releaseCurseForge` already default to
`false`, so they need no explicit line — but the two that default to `true` do.

The plugin is applied **only** to `common/1.20.1`. No other module converts
anything.

## 5. Replacing the pipeline

Current 1.20.1 pipeline:

```
replaceNbtBlocks            (buildSrc, Querz)
  → build/generated/nbt-block-replaced/data/chronodawn/structure/*.nbt
convertNbtStructures        (Exec → python3 + nbtlib)
  → build/generated/nbt-structures/data/chronodawn/structures/*.nbt
processResources            ← sourceSets.main.resources.srcDir('…/generated/nbt-structures')
```

After migration, `common/1.20.1/build.gradle` configures the plugin instead:

```groovy
mcmod {
    multiVersion { enabled = false }
    resourceValidation { enabled = false }
    nbtConversion {
        enabled = true
        sourceVersion = '1.21.1'
        targetVersion = '1.20.1'
        inputDir  = "${buildDir}/generated/nbt-block-replaced/data/chronodawn/structure"
        outputDir = "${buildDir}/generated/nbt-structures/data/chronodawn/structures"
    }
}

tasks.named('convertNbt') {
    dependsOn 'replaceNbtBlocks'
}
```

Keeping `inputDir` and `outputDir` at their current values is what makes this a
drop-in replacement. The plugin preserves each file's path relative to
`inputDir`, so the `structure` (singular) → `structures` (plural) rename that
1.20.1 needs continues to be expressed by the directory names themselves. The
target `DataVersion` comes from the plugin's own version table, keyed by
`targetVersion`.

Removed by this change: the `onlyIf` version guard (unnecessary — the plugin is
applied only to the 1.20.1 module), the Python/`nbtlib` availability probe, and
the `Exec` invocation.

Two behavioural differences to watch:

1. **Double resource registration.** `NbtConversionTasks` wires
   `processResources.dependsOn('convertNbt')` itself and adds `outputDir` to
   `sourceSets.main.resources`. `common/1.20.1/build.gradle:71` already declares
   that same `srcDir`. The duplicate is expected to be harmless — Gradle
   deduplicates source directories, and `processResources` already sets
   `duplicatesStrategy = EXCLUDE` — but the implementation must confirm the JAR
   contains exactly one copy of each structure file rather than assume it.
2. **Task rename.** `convertNbtStructures` disappears; the plugin's task is
   `convertNbt`, and cross-module consumers use `nbtStructures` (§5.1). Grep for
   `convertNbtStructures` across the repository before finishing — the only
   remaining hits should be historical design documents, which are left alone.

### 5.1 The loader modules depend on this task by name

Deleting the dead definitions is not free. Every loader module names the task
across the project boundary:

```groovy
// fabric/<version>/build.gradle, neoforge/<version>/build.gradle
dependsOn project(commonModule).tasks.named('replaceNbtBlocks'),
          project(commonModule).tasks.named('convertNbtStructures')
```

Eleven Fabric and ten NeoForge build files do this, and `tasks.named` fails
eagerly when the task is absent. Removing the definition from ten common
modules would break every loader build except 1.20.1's. The loaders genuinely
need the dependency — they copy from the common module's *source* directories,
not from its `processResources` output, so the converted files must already
exist on disk.

Rather than make twenty-one files version-aware, introduce one lifecycle task
that exists everywhere. `gradle/nbt-block-replacement.gradle` is applied by all
eleven common modules, so it is the natural home:

```groovy
// gradle/nbt-block-replacement.gradle
tasks.register('nbtStructures') {
    group = 'chrono dawn build'
    description = 'Structure .nbt files are generated and ready to be consumed'
    dependsOn 'replaceNbtBlocks'
}
```

`common/1.20.1/build.gradle` adds the version-specific step to it:

```groovy
tasks.named('nbtStructures') { dependsOn 'convertNbt' }
```

Every loader module then depends on the single stable name:

```groovy
dependsOn project(commonModule).tasks.named('nbtStructures')
```

This removes the current oddity where all twenty-one loader files depend on a
task that is a no-op on ten of the eleven versions, and it keeps the
version-specific knowledge in the one module that has it.

The same substitution applies to the `sourcesJar` dependency inside each common
module (`common/<version>/build.gradle`, `tasks.named('sourcesJar')`), which
today lists `replaceNbtBlocks, convertNbtStructures`.

The other ten modules lose the task definition entirely, and their
`processResources.dependsOn` lines shrink to `nbtStructures`.

## 6. Verification

A broken conversion does not fail the build. It ships a 1.20.1 JAR whose
structures are missing their item frames and container contents. The
verification is built around that.

**Byte-equality against the current output — the acceptance test.** Before
touching anything, run the existing Python path from a clean state and copy
`common/1.20.1/build/generated/nbt-structures/` somewhere outside the repository
(`$TMPDIR`), so `cleanAll` cannot destroy the reference. After the migration, regenerate through the
plugin and compare every `.nbt` byte for byte. Identical output proves
equivalence. If any file differs, stop and report which one and how — deciding
which implementation is correct is not a call to make silently.

**GameTest.** `gameTestAll` must pass on 1.20.1, including `StructureTests` and
the `structure_no_unreplaced_vanilla_blocks` guard, which together cover block
replacement and the survival of block-attached entities.

**`checkAll` without Python.** Run the full pipeline with no `nbtlib` reachable
on `PATH`. `checkAll` begins with `cleanAll`, so the conversion genuinely
re-runs. This is the change's whole purpose; a run that leaves `nbtlib`
installed proves nothing.

**Dead-definition removal.** `buildAll` must pass for all eleven versions after
the ten copies are deleted.

## 7. Documentation

`docs/developer_guide.md`, `README.md` and `CLAUDE.md` were checked and contain
no Python or `nbtlib` prerequisite to remove. If the implementation finds one
elsewhere, it goes in the same commit.

No CHANGELOG entry: this is an internal build change with no player-facing or
pack-facing effect.

## 8. Risks

| Risk | Handling |
| --- | --- |
| Plugin output differs from the Python output in some edge case | The byte-equality check is the gate; a difference blocks the migration until explained |
| `includeBuild` interacts badly with the Loom / Architectury plugin resolution already in `pluginManagement` | Detected immediately — every Gradle invocation would fail at settings evaluation |
| Uninitialised submodule produces a confusing failure | Existence check added to `settings.gradle` |
| Duplicate resource registration puts structure files in the JAR twice | Verified by inspecting the built 1.20.1 JAR, not assumed |
| A loader module still names a task that no longer exists | `buildAll` covers all eleven versions on both loaders and fails eagerly on a missing task name |
