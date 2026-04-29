# AGENTS.md

This file gives AI coding agents the project-specific context needed to work on
Chrono Dawn. Prefer this file for quick orientation, and use the linked project
docs for details.

## Project Overview

Chrono Dawn is a multi-loader Minecraft mod built with Architectury. It supports
Fabric and NeoForge from one codebase, with most gameplay logic in common
modules and loader-specific code kept small.

- Language: Java 21
- Build: Gradle Groovy DSL
- Mappings: Mojang mappings, not Yarn
- License: LGPL-3.0
- Current default target: Minecraft 1.21.11
- Supported versions: 1.20.1, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5,
  1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11

Reference docs:

- `README.md`: user-facing overview and build examples
- `docs/developer_guide.md`: architecture and development details
- `CONTRIBUTING.md`: coding standards and contribution process
- `CLAUDE.md`: older AI-agent guidance; keep it consistent when changing
  shared project instructions

## Repository Layout

```text
common/
  shared/             Shared source included by versioned modules
  shared-1.21.1+/     Shared resources for 1.21.1+
  shared-1.21.2+/     Shared resources for 1.21.2+
  shared-1.21.5+/     Shared resources for 1.21.5+
  gametest/           Shared GameTest sources
  1.20.1/             Version-specific common module
  1.21.x/             Version-specific common modules
fabric/
  base/               Shared Fabric code
  1.20.1/             Fabric legacy module
  1.21.x/             Version-specific Fabric modules
neoforge/
  base/               Shared NeoForge code
  1.21.x/             Version-specific NeoForge modules
gradle/shared/        Git submodule with shared Gradle scripts
props/                Version-specific dependency properties
scripts/              Project utility scripts
specs/                Design specs, plans, tasks, research notes
docs/                 Player/developer/release documentation
```

Minecraft 1.21.3 is a hotfix release that reuses the 1.21.2 modules; do not add
separate `common/1.21.3`, `fabric/1.21.3`, or `neoforge/1.21.3` modules unless
the build model changes.

## Working Rules

- Preserve existing user changes. Check `git status --short` before editing and
  avoid reverting unrelated files.
- Prefer existing patterns in nearby version modules before introducing new
  abstractions.
- Put common gameplay behavior in `common/` where possible. Use `fabric/` or
  `neoforge/` only for loader APIs, entry points, events, or platform bridges.
- Use Mojang class names such as `Level`, `BlockPos`, and `Registry`; do not use
  Yarn names.
- Build files are Groovy DSL. Use syntax like `maven { url 'https://...' }`.
- Keep documentation consistent when changing versions, dependencies, features,
  commands, metadata, or release behavior. Update related files such as
  `README.md`, `docs/player_guide.md`, `docs/developer_guide.md`,
  `docs/curseforge_description.md`, `docs/modrinth_description.md`,
  `gradle.properties`, and loader metadata as needed.
- Research that affects future implementation should be written to an
  appropriate file, usually under `specs/chrono-dawn-mod/` or `docs/`, and
  linked from follow-up tasks when applicable.

## Coding Standards

- Java indentation: 4 spaces.
- JSON indentation: 2 spaces.
- Line length: 120 characters soft limit.
- Encoding: UTF-8.
- Line endings: LF.
- Comments should be in English and explain non-obvious reasons, not obvious
  mechanics.
- Registry IDs and resource names should follow existing lowercase naming
  conventions.

## Common Commands

Setup after clone:

```bash
git submodule update --init
```

Build one Minecraft version:

```bash
./gradlew build1_21_11
./gradlew build1_21_10
./gradlew build1_21_9
./gradlew build1_21_8
./gradlew build1_21_7
./gradlew build1_21_6
./gradlew build1_21_5
./gradlew build1_21_4
./gradlew build1_21_3
./gradlew build1_21_2
./gradlew build1_21_1
./gradlew build1_20_1
```

Build release artifacts:

```bash
./gradlew buildAll
./gradlew collectJars
./gradlew release
```

Run unit tests:

```bash
./gradlew testAll
./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11
./gradlew :common-1.21.10:test -Ptarget_mc_version=1.21.10
./gradlew :common-1.21.9:test -Ptarget_mc_version=1.21.9
./gradlew :common-1.21.8:test -Ptarget_mc_version=1.21.8
```

Run resource validation:

```bash
./gradlew validateResources
./gradlew validateTranslations
```

Run clients:

```bash
./gradlew runClientFabric1_21_11
./gradlew runClientNeoForge1_21_11
./gradlew runClientFabric1_21_3
./gradlew runClientNeoForge1_21_3
```

Run GameTests:

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.11
./gradlew gameTestAll
```

Full verification before larger changes or release work:

```bash
./gradlew checkAll
```

`checkAll` is expensive. For small changes, run the narrowest build, test, or
validation command that covers the affected version and loader.

## Multi-Version Notes

- Supported versions and hotfix mappings are defined in `gradle.properties`.
- 1.20.1 is Fabric-only.
- Shared sources/resources are included via source sets; directories named
  `shared*` and `gametest` are not Gradle subprojects.
- When adding a feature, inspect the nearest existing implementation across
  versioned modules and copy only the version-specific differences that are
  actually required.
- If an API differs between Minecraft versions, prefer the existing compatibility
  layer or platform helper pattern over scattering conditionals.

## Mixin Configuration

Fabric and NeoForge need different Mixin configuration because of mapping and
loader differences.

- Fabric loader-specific mixin configs must include
  `"refmap": "common-common-refmap.json"`.
- NeoForge loader-specific mixin configs must not include a `refMap` property.
- `chronodawn.mixins.json` is a common/reference config and is excluded from
  builds.
- When adding or changing Mixins, update both loader-specific configs when the
  behavior applies to both loaders.

## License and Dependencies

- The project is LGPL-3.0. Contributions are expected to be compatible with that
  license.
- Before adding a dependency, verify license compatibility and update
  `THIRD_PARTY_LICENSES.md`.
- MIT, Apache-2.0, and BSD licenses are generally compatible. Avoid proprietary
  or non-commercial licenses such as CC-BY-NC.

## Verification Reporting

When finishing a task, report:

- What changed.
- Which commands were run.
- Any commands that were skipped and why.
- Any remaining risks or follow-up work.

For changes that are verifiable but not yet verified, provide the exact command
and expected success criteria.
