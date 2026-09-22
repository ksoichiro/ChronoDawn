# Release process

This document is the source of truth for changing the Chrono Dawn version,
finalizing `CHANGELOG.md`, verifying release artifacts, and preparing a
release. It does not authorize commits, tags, pushes, or publishing. Perform
those actions only when the user explicitly requests them.

## Release targets

- `target_mc_version=26.2` is the default development target.
- Minecraft 1.21.1 is the long-term modpack compatibility target on both
  Fabric and NeoForge.
- Supported versions, hotfix mappings, and per-version loader sets come from
  `gradle.properties` and `props/<version>.properties`. Do not duplicate those
  lists in release automation.
- Minecraft 1.20.1 supports Fabric and Forge. NeoForge starts at 1.21.1.
- Minecraft 1.20.1 requires Java 17, 1.21.x requires Java 21, and 26.x requires
  Java 25.

## Choose the mod version

`mod_version` contains only the Semantic Versioning portion. Platform JARs add
Minecraft version and loader as build metadata, for example
`chronodawn-0.10.0+1.21.1-neoforge.jar`.

Use the smallest version increment that describes the final user-visible
change:

- Patch: backward-compatible fixes, release corrections, or documentation-only
  release adjustments.
- Minor: backward-compatible gameplay features, configuration surfaces, public
  integration APIs, or substantial modpack integration.
- Major: intentionally incompatible public API, save, registry, or progression
  changes after 1.0. Before 1.0, document any incompatibility prominently even
  when using a minor increment.

Do not infer a target version when the user has not selected one and the choice
would change release scope.

## Finalize the changelog

1. Read the current `## [Unreleased]` section. It is the primary draft and may
   contain carefully written entries accumulated during development.
2. Find the latest actual release tag with `git tag --sort=-v:refname`. Current
   releases use `vX.Y.Z`; older tags containing Minecraft build metadata are
   historical and must not be copied into a new tag automatically.
3. Compare the previous tag with `HEAD`, using both `git log` and `git diff`.
   Add missing user-visible outcomes, remove intermediate states, and retain
   accurate manually written entries.
4. Move the completed Unreleased content under
   `## [X.Y.Z] - YYYY-MM-DD`, then leave a new empty `## [Unreleased]` at the
   top.
5. Update reference links to compare the actual previous tag with `vX.Y.Z`.
6. Confirm that the target version section exists before publishing. The
   Gradle publishing tasks extract release notes from that exact section and
   otherwise may publish an empty changelog.

CHANGELOG entries describe released software behavior. Do not include download
analysis, roadmap rationale, unreleased proposals, internal refactoring, or
pack-author outreach plans unless they change what users receive.

For a pack-facing release, state applicable details explicitly:

- Minecraft versions and loaders affected
- upgrade and save/config compatibility
- new optional dependencies or integrations
- configuration defaults and migration behavior
- stable advancement, registry, tag, or event IDs exposed to pack authors
- known incompatibilities or manual migration steps

## Update documentation

Update only files affected by the release. Search for the old version and
changed behavior, but do not blindly replace historical changelog entries,
release links, compatibility notes, or examples that intentionally describe an
older version.

Check as applicable:

- `README.md`
- `docs/player_guide.md`
- `docs/developer_guide.md`
- `docs/configuration.md`
- `docs/modpack-integration.md`
- `docs/curseforge_description.md`
- `docs/modrinth_description.md`
- `THIRD_PARTY_LICENSES.md`
- `AGENTS.md` and `CLAUDE.md` when supported versions, commands, toolchains, or
  durable development policy changed
- versioned Fabric, Forge, and NeoForge metadata when dependency ranges or
  loader requirements changed

Metadata normally obtains the mod version from `${version}` and does not need a
manual version edit. Verify rather than assume this for every active loader.

## Verification

For a normal release, run:

```bash
./gradlew checkAll
./gradlew collectJars
```

`checkAll` performs the project-wide validations, builds, unit tests, and
GameTests. `collectJars` gathers the verified release artifacts into
`build/release/` without publishing them.

Inspect `build/release/` and confirm that every intended Minecraft
version/loader pair is present, filenames contain the target mod version, and
no stale artifacts from another version remain.

For changes intended to improve modpack adoption, also verify Minecraft 1.21.1
on both Fabric and NeoForge. If the release changes registries, worldgen,
configs, portals, advancements, equipment, or progression, test an in-place
upgrade from the currently adopted pack version, especially 0.8.0, using both
a client world and a dedicated server where applicable. Record expected
worldgen differences separately from data loss or startup failures.

If full verification is skipped or fails, report the exact missing command,
failure, and release risk. Do not disable checks or publish around a failure.

## Release boundary

The following actions require explicit user instruction even after preparation
and verification succeed:

- creating a commit
- creating or pushing a tag
- pushing commits
- running `releaseCurseForge`, `releaseModrinth`, or `releaseAll`
- contacting modpack authors or posting release announcements
