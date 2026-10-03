---
name: update-version
description: Update the Chrono Dawn version and finalize its release changelog
disable-model-invocation: true
argument-hint: [version]
allowed-tools: Read, Edit, Bash(rg:*), Bash(git status:*), Bash(git tag:*), Bash(git log:*), Bash(git diff:*), Bash(git show:*), Bash(git remote:*), Bash(./gradlew:*)
---

## User input

```text
$ARGUMENTS
```

The argument must be a Semantic Versioning value without `v` or Minecraft/
loader build metadata, for example `0.10.0`, `0.10.1`, or `0.10.0-rc.1`.

Reject missing components, uppercase prerelease identifiers, underscores, and
input such as `v0.10.0` or `0.10.0+1.21.1`.

## Procedure

1. Read `docs/release_process.md` completely and follow it as the source of
   truth. The `versioning` skill covers how to choose the increment.
2. Read `mod_version` from `gradle.properties` and report the current and target
   values.
3. Inspect `git status`, the latest actual `v*` release tag, the current
   `CHANGELOG.md` Unreleased section, and the previous-tag-to-`HEAD` log and
   diff. Preserve unrelated user changes.
4. Update `mod_version` to the requested value.
5. Audit the commit history for CHANGELOG omissions (see below) before
   finalizing. Do this before moving the Unreleased content so that missing
   entries are added to the draft first.
6. Finalize `CHANGELOG.md`:
   - keep a new empty `## [Unreleased]` at the top;
   - move the existing Unreleased content into
     `## [<version>] - YYYY-MM-DD`;
   - reconcile it against the actual diff without discarding accurate manually
     written entries;
   - record final user-visible outcomes, not intermediate commits;
   - update footer links using the actual previous tag and current `vX.Y.Z`
     tag format.
7. Update only documentation whose current-version examples or behavior changed.
   Search for the old version, but do not replace historical CHANGELOG entries,
   old release links, or intentionally version-specific examples.
8. Check Fabric, Forge, and NeoForge metadata. They normally use `${version}`.
   Update them only when dependency ranges or release behavior changed.
9. Run the verification required by `docs/release_process.md`, or report the
   exact commands not run and why. For pack-facing changes, include both Fabric
   and NeoForge 1.21.1 and any required upgrade test.
10. Review `git diff HEAD` and report changed files, commands run, skipped
    checks, remaining risks, the commits found missing from the CHANGELOG, and
    the expected tag name.

## CHANGELOG omission audit

Goal: no user-visible change since the previous release tag is missing from the
CHANGELOG.

1. List every commit with `git log --no-merges --format='%h %s' <prev-tag>..HEAD`.
   Do not rely on the subject alone for `fix` and `feat` commits. Use
   `git show --stat <hash>` to see what was actually changed.
2. Classify each commit:
   - user-visible: `feat`, `fix`, balance or worldgen changes, new or changed
     config, translations that users see, dependency range changes, and
     pack-facing IDs such as tags, advancements, registries, or events;
   - not user-visible: pure `docs`, `test`, `chore`, `refactor`, `ci`, and
     reverts that cancel out within the same cycle.
3. For every user-visible commit, find the matching CHANGELOG entry in the
   Unreleased section. One entry may cover several commits, and a fix to a
   feature added in the same cycle is folded into that feature's entry instead
   of getting its own line.
4. Report a table of the commits with no matching entry, with hash, subject,
   and a proposed entry. Add them to the Unreleased draft in the existing
   style and section (Added, Changed, Fixed). Ask the user when a commit's
   visibility is unclear instead of guessing.
5. Check the other direction too. Flag CHANGELOG entries that no commit in the
   range explains, since they may describe reverted or already released work.
6. A change that touches only some Minecraft versions or loaders must say which
   ones in its entry. Check the diff paths (`common/<version>/`,
   `fabric/<version>/`, `neoforge/<version>/`) before writing the scope.

Do not commit, tag, push, publish, or contact modpack authors. Those actions
require separate explicit user instruction.
