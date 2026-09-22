---
name: versioning
description: Choose versions and prepare Chrono Dawn releases.
---

# Chrono Dawn versioning

Use this skill when choosing a mod version, changing `mod_version`, or preparing
a release. Read `docs/release_process.md` before making changes. It is the
project source of truth for changelog, documentation, verification, and release
boundaries.

## Version model

- `gradle.properties` stores only the Semantic Versioning value in
  `mod_version`, such as `0.10.0` or `0.10.0-rc.1`.
- Platform builds append `+<minecraft-version>-<loader>` automatically.
  Supported loaders are Fabric, NeoForge, and Forge on Minecraft 1.20.1.
- Current Git release tags use `vX.Y.Z`. Read actual tags and CHANGELOG links.
  Do not revive the legacy `vX.Y.Z+<minecraft-version>` scheme.

## Choosing an increment

- Patch for backward-compatible fixes or release corrections.
- Minor for backward-compatible gameplay, configuration, or integration
  features.
- Major for intentionally incompatible public API, save, registry, or
  progression changes after 1.0.
- Before 1.0, document incompatible changes prominently even when using a minor
  increment.

Do not choose a version on the user's behalf when the choice changes release
scope. A pack-adoption release containing new accessory, multiplayer, quest, or
configuration features is not automatically a patch merely because it targets
an existing Minecraft version.

## Required outcome

Update `mod_version`, promote the existing Unreleased changelog content, update
only affected documentation, and verify the result as described in
`docs/release_process.md`. Do not commit, tag, push, publish, or contact pack
authors without explicit user instruction.
