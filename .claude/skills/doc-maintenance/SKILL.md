---
name: doc-maintenance
description: Update Chrono Dawn docs for versions, content, dependencies, and releases.
---

# Documentation maintenance

Use this skill for user-visible features, supported versions, dependencies,
configuration, integration behavior, or release preparation. Match the current
document structure and search by content rather than relying on stored line
numbers.

For version and release work, read `docs/release_process.md` first. It is the
source of truth for release boundaries and verification.

## Sources of truth

- Supported versions and hotfix mappings: `gradle.properties`
- Per-version Java, loader, and dependency versions:
  `props/<minecraft-version>.properties`
- Mod version: `mod_version` in `gradle.properties`
- User-visible release history: `CHANGELOG.md`
- Project license: LGPL-3.0, as declared by `LICENSE`

Do not copy a current dependency/version table into this skill. Read the
properties files so the checklist does not become stale.

## Documentation map

- `README.md`: overview, requirements, support matrix, build examples
- `docs/player_guide.md`: gameplay behavior and player-facing configuration
- `docs/developer_guide.md`: architecture, toolchains, commands, and module
  layout
- `docs/configuration.md`: configuration keys, defaults, and effects
- `docs/modpack-integration.md`: tags, scripting, events, stable IDs, and pack
  examples
- `docs/curseforge_description.md`: CurseForge listing
- `docs/modrinth_description.md`: Modrinth listing
- `CHANGELOG.md`: released and pending user-visible changes
- `THIRD_PARTY_LICENSES.md`: dependency licenses and versions
- `AGENTS.md` and `CLAUDE.md`: durable agent instructions, supported targets,
  toolchains, and workflows

## Version or dependency changes

1. Read the actual supported version list and relevant
   `props/<version>.properties` files.
2. Update only documentation that states the affected version, dependency,
   loader, Java requirement, command, or artifact name.
3. Inspect versioned metadata under:
   - `fabric/<version>/src/main/resources/fabric.mod.json`
   - `forge/<version>/src/main/resources/META-INF/mods.toml`
   - `neoforge/<version>/src/main/resources/META-INF/neoforge.mods.toml`
4. Update `THIRD_PARTY_LICENSES.md` when a dependency or bundled component
   changes. Verify the license from an authoritative source. Do not infer that
   an optional dependency is license-compatible merely because it is popular.
5. Update `AGENTS.md` and `CLAUDE.md` when supported targets, the default target,
   Java requirements, module layout, or commands change.
6. Do not replace historical changelog entries or old release links while
   updating current examples.

Optional integrations should remain optional in metadata and code unless the
user explicitly chooses a required dependency. Document the no-dependency
fallback behavior.

## Content or gameplay changes

Check every place that describes the affected behavior. Common cases include:

- Bosses: stats, abilities, drops, multiplayer credit, scaling, and structure
  location
- Structures: generation, locator behavior, loot, and progression role
- Artifacts: stats, activation rules, accessory slots, recipes, and fallback
  inventory behavior
- Worldgen: biome/structure/ore counts, defaults, existing-world behavior, and
  disable controls
- Portals and progression: construction, stabilization, directionality,
  advancements, and pack hooks
- Configuration: exact key, default, valid range, reload/restart requirement,
  and migration behavior

Search for numeric summaries and words such as `all`, `both`, and `each` after
adding or removing countable content. Derive counts from implementation instead
of copying an older document.

## Modpack integration changes

Update `docs/modpack-integration.md` and the relevant configuration/player docs
when changing:

- conventional or mod-owned tags
- registry, advancement, boss, structure, or event IDs
- KubeJS, CraftTweaker, FTB Quests, Jade, JEI/EMI, accessory, Lootr, map, or
  locator behavior
- automatic guide grants or pack-overlap controls
- upgrade compatibility from a version used by a known pack

Keep time-sensitive pack names, download counts, and roadmap analysis in
`.claude/tasks.local.md` or a dated research document. Put only durable behavior
and supported integration contracts in public documentation.

## Pre-release check

- Confirm the target version section exists in `CHANGELOG.md` and its content
  matches the final diff.
- Confirm README, player, developer, configuration, integration, CurseForge,
  and Modrinth docs describe the same shipped behavior.
- Confirm versioned loader metadata matches the dependency properties.
- Run the verification in `docs/release_process.md`. The normal project-wide
  commands are `./gradlew checkAll` and `./gradlew collectJars`, not single-root
  `clean build` or `test` commands.
- For pack-facing work, verify Fabric and NeoForge 1.21.1 and perform any
  required upgrade test.
- Report skipped verification and its risk.

Do not commit, tag, push, publish, or contact external maintainers without
explicit user instruction.
