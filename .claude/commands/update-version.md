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

If invalid, show error and explain correct format using `.claude/skills/versioning.md` examples.

## Outline

1. **Read versioning guide**: Load `.claude/skills/versioning.md` to understand version format and update procedure

2. **Validate version format**:
   - Check format: `<major>.<minor>.<patch>` or `<major>.<minor>.<patch>-<prerelease>`
   - All components must be numeric (except prerelease identifier)
   - Prerelease identifier (if present): alphanumeric + dots (e.g., `beta`, `alpha`, `rc.1`)
   - If invalid, stop and show error with examples

3. **Extract version components**:
   - Parse major, minor, patch
   - Parse prerelease identifier (if present)
   - Store for use in file updates

4. **Read current version**:
   - Read `gradle.properties` line 6
   - Extract current `mod_version` value
   - Show current → new version for user confirmation

5. **Update gradle.properties**:
   - Update line 6: `mod_version=<new-version>`
   - Example: `mod_version=0.3.0-beta`

6. **Update README.md**:
   - Line 81-83: Build output file names
     - `chronodawn-<version>+1.21.1-fabric.jar`
     - `chronodawn-<version>+1.21.1-neoforge.jar`
     - `common-<version>.jar`
   - Line 209: Fabric installation JAR name
     - `chronodawn-<version>+1.21.1-fabric.jar`
   - Line 220: NeoForge installation JAR name
     - `chronodawn-<version>+1.21.1-neoforge.jar`

7. **Update docs/player_guide.md**:
   - Line 78-79: Download file names
     - `chronodawn-<version>+1.21.1-fabric.jar`
     - `chronodawn-<version>+1.21.1-neoforge.jar`

8. **Update docs/developer_guide.md**:
   - Line 235: gradle.properties example
     - `mod_version=<version>`
   - Line 274-276: Build output file names
     - `chronodawn-<version>+1.21.1-fabric.jar`
     - `chronodawn-<version>+1.21.1-neoforge.jar`
     - `common-<version>.jar`

9. **Update CHANGELOG.md**:
   - Read current CHANGELOG.md to understand structure
   - Update `[Unreleased]` section header with new version and date:
     - Change `## [Unreleased]` to `## [<version>] - YYYY-MM-DD`
     - Add new empty `## [Unreleased]` section at the top
   - Update comparison links at bottom:
     - Update `[Unreleased]` link to compare from new version tag to HEAD
     - Add new version link: `[<version>]: https://github.com/ksoichiro/ChronoDawn/compare/v<previous>+1.21.1...v<new>+1.21.1`
   - Example:
     ```markdown
     ## [Unreleased]

     ## [0.3.0-beta] - 2026-01-15
     ### Added
     - New feature description

     [Unreleased]: https://github.com/ksoichiro/ChronoDawn/compare/v0.3.0-beta+1.21.1...HEAD
     [0.3.0-beta]: https://github.com/ksoichiro/ChronoDawn/compare/v0.2.0-beta+1.21.1...v0.3.0-beta+1.21.1
     ```

10. **Verification**:
   - Search for old version string in all updated files
   - Confirm no occurrences remain
   - Show summary of changed files

11. **Report**:
    - Show diff summary for each file
    - Confirm version update complete
    - Remind user: fabric.mod.json and neoforge.mods.toml use `${version}` (auto-updated)
    - Remind user: CHANGELOG.md section content can be used for GitHub Release, CurseForge, and Modrinth
    - Suggest next steps:
      - Test build: `./gradlew clean build`
      - Verify JAR names match documentation
      - Copy CHANGELOG.md section for release notes when creating GitHub Release/CurseForge/Modrinth upload

## Important Notes

- **Do NOT** include `+1.21.1` in gradle.properties - this is auto-appended by build scripts
- **Do include** `+1.21.1-fabric` and `+1.21.1-neoforge` in documentation JAR names
- **Common JAR** does NOT include Minecraft version: `common-<version>.jar`
- **Prerelease identifiers** use hyphen: `-beta`, `-alpha`, `-rc.1`
- **Build metadata** uses plus: `+1.21.1` (auto-generated, not in gradle.properties)
- **CHANGELOG.md** follows [Keep a Changelog](https://keepachangelog.com/) format:
  - Categories: Added, Changed, Deprecated, Removed, Fixed, Security
  - Version sections use format: `## [<version>] - YYYY-MM-DD`
  - Comparison links at bottom use full version with build metadata: `v<version>+1.21.1`
  - Content can be copied directly to GitHub Release, CurseForge, and Modrinth changelogs

## Version Update Examples

### Example 1: Beta Version
```
Input: 0.3.0-beta
gradle.properties: mod_version=0.3.0-beta
README.md: chronodawn-0.3.0-beta+1.21.1-fabric.jar
```

### Example 2: Stable Release
```
Input: 1.0.0
gradle.properties: mod_version=1.0.0
README.md: chronodawn-1.0.0+1.21.1-fabric.jar
```

### Example 3: Release Candidate
```
Input: 1.0.0-rc.1
gradle.properties: mod_version=1.0.0-rc.1
README.md: chronodawn-1.0.0-rc.1+1.21.1-fabric.jar
```

## Error Handling

If version format is invalid, show:
```
Error: Invalid version format "<input>"

Valid formats:
  - Stable: X.Y.Z (e.g., 1.0.0)
  - Pre-release: X.Y.Z-<identifier> (e.g., 0.2.0-beta, 1.0.0-rc.1)

Examples:
  ✓ 0.3.0-beta
  ✓ 1.0.0
  ✓ 1.2.3-rc.1
  ✗ 0.3.0+1.21.1 (build metadata not allowed)
  ✗ 0.3.0-Beta (use lowercase)
  ✗ v0.3.0 (no 'v' prefix)

See .claude/skills/versioning.md for details.
```

## Success Message

After successful update:
```
✓ Version updated: <old> → <new>

Updated files:
  - gradle.properties
  - README.md (3 locations)
  - docs/player_guide.md (1 location)
  - docs/developer_guide.md (2 locations)
  - CHANGELOG.md (version header, comparison links)

Next steps:
  1. Test build: ./gradlew clean build
  2. Verify JAR names:
     - fabric/build/libs/chronodawn-<version>+1.21.1-fabric.jar
     - neoforge/build/libs/chronodawn-<version>+1.21.1-neoforge.jar
  3. Commit changes (if ready)

For release:
  - Copy CHANGELOG.md [<version>] section for GitHub Release notes
  - Use same content for CurseForge and Modrinth changelog fields
```

## Reference

- Versioning guide: `.claude/skills/versioning.md`
- Semantic Versioning: https://semver.org/
