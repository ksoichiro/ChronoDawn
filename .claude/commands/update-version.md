---
description: Update mod version across all project files
allowed-tools: Read, Edit, Bash(grep*)
argument-hint: [version]
---

## User Input

```text
$ARGUMENTS
```

**Expected**: New version string (e.g., `0.3.0-beta`, `1.0.0`, `0.2.1-rc.1`)

You **MUST** validate the user input before proceeding.

## Version Format Validation

Validate that the input follows Semantic Versioning 2.0.0:

**Valid formats**:
- `X.Y.Z` (e.g., `1.0.0`) - Stable release
- `X.Y.Z-prerelease` (e.g., `0.2.0-beta`, `1.0.0-rc.1`) - Pre-release

**Invalid formats**:
- Missing components (e.g., `1.0`, `beta`)
- Including build metadata (e.g., `0.2.0+1.21.1`) - This is auto-generated
- Invalid prerelease identifiers (e.g., `0.2.0-Beta`, `0.2.0_beta`)

If invalid, show error and explain correct format.

## Conventions

This document uses the following placeholders:

| Placeholder | Description | Example |
|---|---|---|
| `<version>` | Mod version (semver) | `0.4.0-beta` |
| `<old_version>` | Current mod version before update | `0.3.0-beta` |
| `<mc_version>` | Minecraft version | `1.21.2` |
| `<loader>` | Mod loader name | `fabric`, `neoforge` |
| `<tag_mc_version>` | MC version used in git tags (see CHANGELOG) | `1.21.1` |

## Outline

1. **Validate version format**:
   - Check format: `<major>.<minor>.<patch>` or `<major>.<minor>.<patch>-<prerelease>`
   - All components must be numeric (except prerelease identifier)
   - Prerelease identifier (if present): alphanumeric + dots (e.g., `beta`, `alpha`, `rc.1`)
   - If invalid, stop and show error with examples

2. **Read current version**:
   - Read `gradle.properties` and find `mod_version=` line
   - Extract current `mod_version` value as `<old_version>`
   - Show `<old_version>` → `<version>` for user confirmation

3. **Update gradle.properties**:
   - Replace `mod_version=<old_version>` with `mod_version=<version>`

4. **Update documentation files**:
   - Target files: `README.md`, `docs/player_guide.md`, `docs/developer_guide.md`
   - Search for `<old_version>` in each file and replace with `<version>`
   - This covers all occurrences including:
     - JAR file names: `chronodawn-<version>+<mc_version>-<loader>.jar`
     - Output paths: `<loader>-<mc_version>/build/libs/chronodawn-...`
     - Common JAR: `common-<mc_version>-<version>.jar`
     - gradle.properties examples: `mod_version=<version>`

5. **Update CHANGELOG.md**:
   - Read current CHANGELOG.md to understand structure
   - Update `[Unreleased]` section header with new version and date:
     - Change `## [Unreleased]` to `## [<version>] - YYYY-MM-DD`
     - Add new empty `## [Unreleased]` section at the top
   - Update comparison links at bottom:
     - Update `[Unreleased]` link to compare from new version tag to HEAD
     - Add new version link comparing from previous tag to new tag
   - Determine `<tag_mc_version>` by reading the existing comparison links in CHANGELOG.md
   - Example:
     ```markdown
     ## [Unreleased]

     ## [<version>] - YYYY-MM-DD
     ### Added
     - ...

     [Unreleased]: https://github.com/ksoichiro/ChronoDawn/compare/v<version>+<tag_mc_version>...HEAD
     [<version>]: https://github.com/ksoichiro/ChronoDawn/compare/v<old_version>+<tag_mc_version>...v<version>+<tag_mc_version>
     ```

6. **Verification**:
   - Search for `<old_version>` in all updated files
   - Confirm no occurrences remain (except in CHANGELOG.md history sections which retain old versions)
   - Show summary of changed files

7. **Report**:
    - Show diff summary for each file
    - Confirm version update complete
    - Remind user: `fabric.mod.json` and `neoforge.mods.toml` use `${version}` (auto-updated by build)
    - Remind user: CHANGELOG.md section content can be used for GitHub Release, CurseForge, and Modrinth
    - Suggest next steps:
      - Test build: `./gradlew cleanAll buildAll`
      - Verify JAR names match documentation
      - Copy CHANGELOG.md section for release notes

## Important Notes

- **gradle.properties**: Only contains the semver part (e.g., `0.4.0-beta`). Build metadata (`+<mc_version>`) is auto-appended by build scripts.
- **Documentation JAR names**: Include MC version and loader as build metadata (e.g., `chronodawn-<version>+<mc_version>-<loader>.jar`)
- **Output paths**: Version-specific directories (e.g., `<loader>-<mc_version>/build/libs/`)
- **Common JAR**: Format is `common-<mc_version>-<version>.jar`
- **CHANGELOG tags**: Use the MC version found in existing comparison links (`<tag_mc_version>`). Do not assume a specific version—read from the file.
- **Search-based replacement**: Always use grep/search to find `<old_version>` occurrences. Do not rely on line numbers.
- **CHANGELOG.md** follows [Keep a Changelog](https://keepachangelog.com/) format:
  - Categories: Added, Changed, Deprecated, Removed, Fixed, Security
  - Version sections: `## [<version>] - YYYY-MM-DD`
  - Content can be copied directly to GitHub Release, CurseForge, and Modrinth changelogs

## Error Handling

If version format is invalid, show:
```
Error: Invalid version format "<input>"

Valid formats:
  - Stable: X.Y.Z (e.g., 1.0.0)
  - Pre-release: X.Y.Z-<identifier> (e.g., 0.2.0-beta, 1.0.0-rc.1)

Examples:
  Valid: 0.3.0-beta, 1.0.0, 1.2.3-rc.1
  Invalid: 0.3.0+1.21.1 (build metadata not allowed)
  Invalid: 0.3.0-Beta (use lowercase)
  Invalid: v0.3.0 (no 'v' prefix)

See https://semver.org/ for details.
```

## Reference

- Semantic Versioning: https://semver.org/
