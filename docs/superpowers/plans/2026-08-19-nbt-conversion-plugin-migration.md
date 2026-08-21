# NBT Conversion Plugin Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the Python 3 + `nbtlib` build dependency by converting 1.20.1 structure files with the JVM implementation the shared Gradle plugin already ships.

**Architecture:** `gradle/shared` (a git submodule) is added to the root `settings.gradle` as an included plugin build. `common/1.20.1` applies `com.github.ksoichiro.mcmod` with only its `nbtConversion` feature enabled, replacing the `Exec`-based `convertNbtStructures` task with the plugin's `convertNbt`. A new `nbtStructures` lifecycle task, registered for every common module, gives the twenty-one loader build files one stable name to depend on so the ten dead task definitions can be deleted.

**Tech Stack:** Gradle 8.14 (Groovy DSL), composite build (`includeBuild`), the `com.github.ksoichiro.mcmod` plugin from `gradle/shared`, Querz NBT (inside the plugin).

**Spec:** `docs/superpowers/specs/2026-08-19-nbt-conversion-plugin-migration-design.md`

## Global Constraints

- Work in the worktree `.worktrees/nbt-plugin-migration` on branch `nbt-plugin-migration`. Run `pwd` before the first command.
- Do NOT modify anything under `gradle/shared/` — it is a git submodule shared with other projects. The plugin is consumed as-is.
- Build files use **Groovy** syntax (`maven { url '…' }`, not `url = "…"`).
- Running Gradle here needs `export JAVA_HOME="/Users/ksoichiro/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.10+7/Contents/Home"` (no system JDK on PATH) and the Bash tool's `dangerouslyDisableSandbox: true` (the JVM launch is otherwise blocked). If that JDK path is gone, list `/Users/ksoichiro/.gradle/jdks/` and use the JDK 21 there.
- The submodule must be initialised: `git -c protocol.file.allow=always submodule update --init`.
- `python3` with `nbtlib` is needed **only for Task 1** (capturing the reference output). Create a throwaway venv and put it first on `PATH`; never `pip3 install` into the system Python — Homebrew's Python is externally managed (PEP 668) and refuses.
- Byte-for-byte equality with the pre-migration output is the acceptance criterion. If any converted file differs, STOP and report which file and how it differs. Do not decide which implementation is "right" on your own.
- The plugin's task is named `convertNbt`; the cross-module lifecycle task is named `nbtStructures`. `convertNbtStructures` must not survive anywhere outside historical design documents.
- Commit messages: English, Conventional Commits. Do NOT run `git commit` — the controller commits.

---

### Task 1: Capture the reference output

Nothing can be verified after the migration without a known-good "before". This task produces it and changes no source file.

**Files:**
- No repository files are created or modified.

**Interfaces:**
- Consumes: nothing.
- Produces: a reference directory at `$TMPDIR/nbt-reference/` holding the `.nbt` files the current Python path generates, used as the acceptance oracle in Task 2.

- [ ] **Step 1: Initialise the submodule**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.worktrees/nbt-plugin-migration
git -c protocol.file.allow=always submodule update --init
```

- [ ] **Step 2: Create a throwaway venv with nbtlib**

```bash
python3 -m venv .nbtvenv
.nbtvenv/bin/pip install nbtlib
.nbtvenv/bin/python3 -c "import nbtlib; print('nbtlib', nbtlib.__version__)"
```

Expected: prints `nbtlib 2.x`. `.nbtvenv/` is git-ignored scratch; it must not be committed.

- [ ] **Step 3: Generate the structures from a clean state**

```bash
export JAVA_HOME="/Users/ksoichiro/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.10+7/Contents/Home"
export PATH="$PWD/.nbtvenv/bin:$PATH"
./gradlew clean1_20_1 --console=plain
./gradlew :common-1.20.1:convertNbtStructures -Ptarget_mc_version=1.20.1 --console=plain
```

Expected: `BUILD SUCCESSFUL`, and the log contains `Converting NBT structures from 1.21.1 to 1.20.1 format...`. If it says `UP-TO-DATE`, the clean did not take effect — delete `common/1.20.1/build/generated/` by hand and re-run, because a cached result is not a fresh conversion.

- [ ] **Step 4: Copy the output outside the repository**

```bash
rm -rf "$TMPDIR/nbt-reference"
mkdir -p "$TMPDIR/nbt-reference"
cp -R common/1.20.1/build/generated/nbt-structures/. "$TMPDIR/nbt-reference/"
find "$TMPDIR/nbt-reference" -name '*.nbt' | wc -l
```

Expected: a non-zero count. Record that number in your report — Task 2 compares against it. A count of zero means the conversion produced nothing and the reference is worthless; stop and report.

- [ ] **Step 5: Record the checksums**

```bash
cd "$TMPDIR/nbt-reference"
find . -name '*.nbt' -exec shasum -a 256 {} \; | sort > "$TMPDIR/nbt-reference.sha256"
wc -l "$TMPDIR/nbt-reference.sha256"
```

Expected: the same count as Step 4. Report the file path and the count; there is nothing to commit for this task.

---

### Task 2: Convert with the plugin instead of Python

**Files:**
- Modify: `settings.gradle` (the `pluginManagement` block at the top of the file)
- Modify: `common/1.20.1/build.gradle` (the `convertNbtStructures` task at line 91, and the `processResources.dependsOn` at line 151)

**Interfaces:**
- Consumes: the reference output from Task 1 (`$TMPDIR/nbt-reference/`, `$TMPDIR/nbt-reference.sha256`).
- Produces: the task `convertNbt` on project `:common-1.20.1`, configured to read `build/generated/nbt-block-replaced/data/chronodawn/structure` and write `build/generated/nbt-structures/data/chronodawn/structures`. Task 3 makes `nbtStructures` depend on it.

- [ ] **Step 1: Add the included build to `settings.gradle`**

Replace the existing `pluginManagement { … }` block (the first ten lines of the file) with:

```groovy
// gradle/shared is a git submodule. Fail early and clearly when it is missing,
// because includeBuild is evaluated before build.gradle's own check can run.
if (!file('gradle/shared/build.gradle').exists()) {
    throw new GradleException(
        "gradle/shared submodule is not initialized. Run: git submodule update --init")
}

pluginManagement {
    includeBuild 'gradle/shared'
    repositories {
        maven { url 'https://maven.fabricmc.net/' }
        maven { url 'https://maven.architectury.dev/' }
        maven { url 'https://maven.neoforged.net/releases/' }
        maven { url 'https://maven.minecraftforge.net/' }
        maven { url 'https://maven.parchmentmc.org/' }
        gradlePluginPortal()
    }
}
```

- [ ] **Step 2: Verify Gradle still configures**

```bash
export JAVA_HOME="/Users/ksoichiro/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.10+7/Contents/Home"
./gradlew tasks --console=plain -Ptarget_mc_version=1.20.1 > /dev/null
```

Expected: `BUILD SUCCESSFUL`. A failure here means `includeBuild` clashed with the existing plugin resolution — report the error rather than working around it.

- [ ] **Step 3: Apply the plugin in `common/1.20.1/build.gradle`**

At the top of the file, immediately after the existing
`apply from: rootProject.file('gradle/nbt-block-replacement.gradle')` line, add:

```groovy
apply plugin: 'com.github.ksoichiro.mcmod'
```

- [ ] **Step 4: Replace the Exec task with plugin configuration**

Delete the whole `tasks.register('convertNbtStructures', Exec) { … }` block (starts at line 91 with the comment `// NBT Structure Multi-Version Conversion`, ends at the closing brace before `// Run NBT conversion before processing resources`). Put this in its place:

```groovy
// NBT Structure Multi-Version Conversion
// Converts 1.21.1 NBT structure files to 1.20.1 format. The conversion runs on
// the JVM inside the shared mcmod plugin; only nbtConversion is enabled here
// because multiVersion and resourceValidation already come from the script
// plugins applied in the root build, and both default to enabled.
mcmod {
    multiVersion {
        enabled = false
    }
    resourceValidation {
        enabled = false
    }
    nbtConversion {
        enabled = true
        sourceVersion = '1.21.1'
        targetVersion = '1.20.1'
        inputDir = "${buildDir}/generated/nbt-block-replaced/data/chronodawn/structure"
        outputDir = "${buildDir}/generated/nbt-structures/data/chronodawn/structures"
    }
}

tasks.named('convertNbt') {
    dependsOn 'replaceNbtBlocks'
}
```

- [ ] **Step 5: Update the `processResources` dependency in the same file**

Line 151 currently reads:

```groovy
processResources.dependsOn replaceNbtBlocks, convertNbtStructures
```

Change it to:

```groovy
processResources.dependsOn replaceNbtBlocks, convertNbt
```

Leave the `sourcesJar` dependency (around line 177) alone for now — Task 3 rewrites it along with every other module's.

- [ ] **Step 6: Run the conversion through the plugin, from clean, with no Python**

```bash
export JAVA_HOME="/Users/ksoichiro/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.10+7/Contents/Home"
./gradlew clean1_20_1 --console=plain
./gradlew :common-1.20.1:convertNbt -Ptarget_mc_version=1.20.1 --console=plain
```

Note the deliberately plain `PATH`: the venv from Task 1 must NOT be active. Expected: `BUILD SUCCESSFUL` and a log line like `NBT conversion: converted N file(s) from 1.21.1 to 1.20.1`, with N equal to the count recorded in Task 1.

- [ ] **Step 7: Compare byte for byte against the reference**

```bash
cd common/1.20.1/build/generated/nbt-structures
find . -name '*.nbt' -exec shasum -a 256 {} \; | sort > "$TMPDIR/nbt-plugin.sha256"
diff "$TMPDIR/nbt-reference.sha256" "$TMPDIR/nbt-plugin.sha256" && echo "IDENTICAL"
```

Expected: prints `IDENTICAL` with no diff output.

If `diff` reports differences: STOP. Do not adjust the converter, the mapping, or the reference. Report which files differ and, for one of them, the first differing offset:

```bash
cmp "$TMPDIR/nbt-reference/<relative/path.nbt>" "./<relative/path.nbt>"
```

The controller decides what happens next.

- [ ] **Step 8: Confirm the JAR holds exactly one copy of each structure**

The plugin adds `outputDir` to `sourceSets.main.resources` on its own, and this build file already declares the same `srcDir`. Verify the duplicate is harmless:

```bash
export JAVA_HOME="/Users/ksoichiro/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.10+7/Contents/Home"
./gradlew :common-1.20.1:jar -Ptarget_mc_version=1.20.1 --console=plain
unzip -l common/1.20.1/build/libs/*.jar | grep -c 'data/chronodawn/structures/.*\.nbt'
unzip -l common/1.20.1/build/libs/*.jar | grep 'data/chronodawn/structures/.*\.nbt' | awk '{print $4}' | sort | uniq -d
```

Expected: the count matches Task 1's file count, and the `uniq -d` line prints nothing (no duplicated entry names). If duplicates appear, remove the now-redundant `srcDir "${buildDir}/generated/nbt-structures"` from the `sourceSets` block (around line 71) and re-run this step.

- [ ] **Step 9: Report**

Report: the converted-file count, the `IDENTICAL` result, the JAR entry count, and whether the redundant `srcDir` had to be removed. The controller commits.

---

### Task 3: One stable task name for the loader modules

**Files:**
- Modify: `gradle/nbt-block-replacement.gradle` (add the `nbtStructures` lifecycle task)
- Modify: `common/1.20.1/build.gradle` (hook `convertNbt` into `nbtStructures`; update `sourcesJar`)
- Modify: `common/{1.21.1,1.21.2,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11}/build.gradle` (delete the dead `convertNbtStructures` task; update `processResources` and `sourcesJar`)
- Modify: `fabric/{1.20.1,1.21.1,1.21.2,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11}/build.gradle` (11 files)
- Modify: `neoforge/{1.21.1,1.21.2,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11}/build.gradle` (10 files)

**Interfaces:**
- Consumes: the `convertNbt` task configured in Task 2.
- Produces: a task `nbtStructures` on every common module, meaning "structure `.nbt` files are generated and ready to consume". On 1.20.1 it depends on `replaceNbtBlocks` and `convertNbt`; elsewhere on `replaceNbtBlocks` alone.

- [ ] **Step 1: Register the lifecycle task**

Append to `gradle/nbt-block-replacement.gradle`:

```groovy
// Stable name for "structure .nbt files are generated and ready to consume".
// Loader modules depend on this across the project boundary, so it must exist
// in every common module; only 1.20.1 adds a conversion step to it.
tasks.register('nbtStructures') {
    group = 'chrono dawn build'
    description = 'Structure .nbt files are generated and ready to be consumed'
    dependsOn 'replaceNbtBlocks'
}
```

- [ ] **Step 2: Hook the 1.20.1 conversion into it**

In `common/1.20.1/build.gradle`, directly below the `tasks.named('convertNbt') { … }` block added in Task 2, add:

```groovy
tasks.named('nbtStructures') {
    dependsOn 'convertNbt'
}
```

- [ ] **Step 3: Delete the ten dead task definitions**

In each of `common/1.21.1`, `common/1.21.2`, `common/1.21.4`, `common/1.21.5`, `common/1.21.6`, `common/1.21.7`, `common/1.21.8`, `common/1.21.9`, `common/1.21.10`, `common/1.21.11` `/build.gradle`, delete the entire `tasks.register('convertNbtStructures', Exec) { … }` block. It begins with the comment `// NBT Structure Multi-Version Conversion` and ends at the closing brace before the `// Run NBT conversion before processing resources` comment. The block is guarded by `onlyIf { project.ext.minecraft_version == '1.20.1' }` and can never run in these modules.

- [ ] **Step 4: Update `processResources` in all eleven common modules**

In every `common/<version>/build.gradle`, the line

```groovy
processResources.dependsOn replaceNbtBlocks, convertNbtStructures
```

becomes

```groovy
processResources.dependsOn nbtStructures
```

In `common/1.20.1/build.gradle` this replaces the line Task 2 left as
`processResources.dependsOn replaceNbtBlocks, convertNbt`.

- [ ] **Step 5: Update `sourcesJar` in all eleven common modules**

In every `common/<version>/build.gradle`, inside `tasks.named('sourcesJar')`, the line

```groovy
    dependsOn replaceNbtBlocks, convertNbtStructures
```

becomes

```groovy
    dependsOn nbtStructures
```

Leave the neighbouring `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` untouched.

- [ ] **Step 6: Update the twenty-one loader modules**

In each of the eleven `fabric/<version>/build.gradle` and ten `neoforge/<version>/build.gradle` files, the line

```groovy
    dependsOn project(commonModule).tasks.named('replaceNbtBlocks'), project(commonModule).tasks.named('convertNbtStructures')
```

becomes

```groovy
    dependsOn project(commonModule).tasks.named('nbtStructures')
```

Keep the surrounding block exactly as it is — the Fabric files wrap it in `processResources { … }`, the NeoForge files in `tasks.named("processResources", ProcessResources).configure { … }`, and the following `from(...)` / `duplicatesStrategy` lines stay.

- [ ] **Step 7: Confirm the old name is gone from build logic**

```bash
grep -rln 'convertNbtStructures' --exclude-dir=build --exclude-dir=.git --exclude-dir=.gradle .
```

Expected: only files under `docs/superpowers/` (historical design documents, including this migration's own spec) and `common/1.20.1/src/main/resources/data/chronodawn/structures/README.md`. Update that README if it instructs a reader to run the task by name; leave the design documents alone.

- [ ] **Step 8: Build the two ends of the version range**

```bash
export JAVA_HOME="/Users/ksoichiro/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.10+7/Contents/Home"
./gradlew clean build -Ptarget_mc_version=1.20.1 -x test --console=plain
./gradlew clean build -Ptarget_mc_version=1.21.11 -x test --console=plain
```

Expected: both `BUILD SUCCESSFUL`. 1.20.1 exercises the conversion path; 1.21.11 exercises a module whose task definition was deleted. A missing-task error names the file that still references the old task.

- [ ] **Step 9: Re-verify byte equality after the rewiring**

```bash
cd common/1.20.1/build/generated/nbt-structures
find . -name '*.nbt' -exec shasum -a 256 {} \; | sort > "$TMPDIR/nbt-after-task3.sha256"
diff "$TMPDIR/nbt-reference.sha256" "$TMPDIR/nbt-after-task3.sha256" && echo "IDENTICAL"
```

Expected: `IDENTICAL`. Report the result. The controller commits.

---

### Task 4: Full verification without Python

**Files:**
- Modify: `common/1.20.1/src/main/resources/data/chronodawn/structures/README.md` — only if Task 3 Step 7 found it names the old task.
- Modify: `docs/developer_guide.md` — only if it turns out to document a Python prerequisite.

**Interfaces:**
- Consumes: the finished migration from Tasks 2 and 3.
- Produces: nothing consumed by later tasks.

- [ ] **Step 1: Remove the venv so Python cannot be used by accident**

```bash
cd /Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.worktrees/nbt-plugin-migration
rm -rf .nbtvenv
python3 -c "import nbtlib" ; echo "exit=$?"
```

Expected: a `ModuleNotFoundError` and a non-zero exit. That is the point of this task: the pipeline must now pass in an environment where the old path could not have worked.

- [ ] **Step 2: Check for a stale Python prerequisite in the docs**

```bash
grep -rn 'nbtlib\|pip3 install' docs/ README.md CLAUDE.md 2>/dev/null
```

If a hit describes a build prerequisite, delete that sentence. If the only hits are historical design documents under `docs/superpowers/`, change nothing. Report which case applied.

- [ ] **Step 3: Run the full verification pipeline**

```bash
export JAVA_HOME="/Users/ksoichiro/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.10+7/Contents/Home"
./gradlew checkAll --console=plain
```

This is long — it cleans, validates, builds eleven versions, runs unit tests and GameTests across twenty-three loader/version configurations. Give the Bash tool its maximum timeout, and if it still times out, re-run in the background and poll the log rather than abandoning it. Do not substitute a shorter task.

Expected: `Total: 12 passed, 0 failed, 0 skipped`. Because Step 1 removed `nbtlib`, a green run proves the Python dependency is gone.

- [ ] **Step 4: Interpret any failure before reporting it**

If `gameTestAll` reports a failure, check whether the log for that version says `All NNNN required tests passed` before a non-zero exit — that shutdown-time flake has been observed on this repo, and the authoritative check is a standalone re-run:

```bash
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=<version> --console=plain
./gradlew :fabric:runGameTest -Ptarget_mc_version=<version> --console=plain
```

If `buildAll` fails, re-run that one version standalone (`./gradlew clean build -Ptarget_mc_version=<version> -x test`) to get the real compiler output — the wrapper hides it.

Report exactly what you observed either way. Do not paper over a real failure.

- [ ] **Step 5: Report**

Report the `checkAll` summary line, whether any docs changed, and confirm `.nbtvenv` is gone and `git status` is clean apart from your intended edits. The controller commits.

---

## Notes for the executor

- The plugin lives in a git submodule that must not be edited. If the plugin looks like it needs a change, stop and report — that is a decision about another repository.
- `convertNbt` has no `onlyIf` guard because it exists only on `common/1.20.1`, which is only configured when building 1.20.1. Do not add one.
- The reference checksums from Task 1 are the acceptance oracle for the whole plan. Keep them until Task 4 finishes; if `$TMPDIR` is cleared, re-run Task 1 rather than trusting a fresh output as its own reference.
