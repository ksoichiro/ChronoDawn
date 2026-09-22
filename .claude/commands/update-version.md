---
description: Update the Chrono Dawn version and finalize its release changelog
allowed-tools: Read, Edit, Bash(rg:*), Bash(git status:*), Bash(git tag:*), Bash(git log:*), Bash(git diff:*), Bash(git remote:*), Bash(./gradlew:*)
argument-hint: [version]
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
   truth.
2. Read `mod_version` from `gradle.properties` and report the current and target
   values.
3. Inspect `git status`, the latest actual `v*` release tag, the current
   `CHANGELOG.md` Unreleased section, and the previous-tag-to-`HEAD` log and
   diff. Preserve unrelated user changes.
4. Update `mod_version` to the requested value.
5. Finalize `CHANGELOG.md`:
   - keep a new empty `## [Unreleased]` at the top;
   - move the existing Unreleased content into
     `## [<version>] - YYYY-MM-DD`;
   - reconcile it against the actual diff without discarding accurate manually
     written entries;
   - record final user-visible outcomes, not intermediate commits;
   - update footer links using the actual previous tag and current `vX.Y.Z`
     tag format.
6. Update only documentation whose current-version examples or behavior changed.
   Search for the old version, but do not replace historical CHANGELOG entries,
   old release links, or intentionally version-specific examples.
7. Check Fabric, Forge, and NeoForge metadata. They normally use `${version}`.
   Update them only when dependency ranges or release behavior changed.
8. Run the verification required by `docs/release_process.md`, or report the
   exact commands not run and why. For pack-facing changes, include both Fabric
   and NeoForge 1.21.1 and any required upgrade test.
9. Review `git diff HEAD` and report changed files, commands run, skipped checks,
   remaining risks, and the expected tag name.

Do not commit, tag, push, publish, or contact modpack authors. Those actions
require separate explicit user instruction.
