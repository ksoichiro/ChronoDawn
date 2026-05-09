# Design: Config Foundation + Ancient Ruins Tuning

**Date**: 2026-05-09
**Status**: Approved scope, pending implementation
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project A, first PR
**Branch (planned)**: `config-foundation`

---

## Problem

Chrono Dawn has zero config infrastructure. Modpack creators cannot tune any value without source patches or world-specific datapacks, which is friction enough that the mod becomes a less attractive integration target.

A concrete user request triggered the work: Ancient Ruins were too dense in early versions. We have since reduced spawn rate (spacing 24 → 56, separation 8 → 20) but lack data on whether the new defaults are right for everyone — and conversely some users may now find the structure too rare. The right answer is to expose the values for each user / pack to choose, with current values as defaults.

## Goals

1. Establish a cross-loader configuration system that future tunables (boss HP, ore generation, time distortion strength, etc.) can register into without re-architecting.
2. Expose Ancient Ruins generation parameters (`enabled`, `spacing`, `separation`, `salt`) as the first concrete consumer.
3. Document the configuration for both end-users and modpack creators.

## Non-goals

- Configuring anything other than Ancient Ruins in this PR. Other tunables come in subsequent PRs of sub-project A.
- Hot-reload of worldgen values. Minecraft does not re-read `StructurePlacement` on `/reload`; this is a vanilla constraint, not a defect.
- Retroactive changes to already-generated chunks. Same vanilla constraint.
- Server-client synchronization. Ancient Ruins is server-side only; sync becomes a concern when later config slices touch client-perceptible values (e.g. Time Distortion strength).
- Auto-generated config UI. File editing is the expected workflow for the modpack-creator audience.
- A heavy config UI / form library (Cloth Config, etc.). The TOML file is hand-edited; this matches NeoForge convention and the modpack-creator audience.

## Architecture

### Configuration file

- **Location**: `config/chronodawn.toml` (single file, all features inside, one config per server / single-player instance — Minecraft's standard convention)
- **Format**: TOML, hand-editable, supports inline comments
- **Parser**: night-config (`com.electronwill.night-config:toml`) — already bundled by NeoForge for its own `ModConfigSpec`; declared explicitly and JAR-in-JAR'd for Fabric via Loom's `include` configuration
- **Schema versioning**: top-level `schema_version` integer (starts at `1`)
- **Lifecycle**: read once at mod common-init; not re-read on `/reload`

Example (defaults reflected exactly; note inline comments are part of the shipped default):

```toml
# Chrono Dawn configuration.
# Changes require a server (or single-player world) restart to take effect.
# Already-generated chunks are not retroactively updated.

schema_version = 1

[world.structures.ancient_ruins]
# Whether Ancient Ruins generate at all in the Overworld.
enabled = true

# Average distance (in chunks) between Ancient Ruins placement attempts.
# Lower = denser, higher = rarer. Must be > separation. Vanilla limit: 4096.
spacing = 56

# Minimum guaranteed distance (in chunks) between two Ancient Ruins.
# Must be < spacing.
separation = 20

# Random seed offset for placement; usually no reason to change this.
salt = 20005897
```

**Behavior**:

- File missing → write the commented default file (above) to disk on first run, log info-level message.
- Partial file → merge with defaults silently for omitted keys; user comments preserved (night-config supports preserving comments on rewrite, which we do not currently use, but this leaves the option open).
- Unknown keys → log warning at WARN level, ignore.
- Invalid values → log ERROR, fall back to default for that field; the rest of the config still loads.
- Schema version newer than known → log warning, attempt to read what we recognize.
- Schema version older → migrate up via field-rename map (currently empty, future-proofing).

### Loading flow

1. On common mod init, `ConfigLoader.load()` reads `config/chronodawn.toml` via night-config (or writes the commented default file if absent).
2. Parsed result becomes an immutable `ChronoDawnConfig` record passed to consumers.
3. `RuntimeStructureOverlay` generates JSON content for `data/chronodawn/worldgen/structure_set/ancient_ruins.json` from the config.
4. `PackProvider` (loader-specific) registers a virtual datapack named `chronodawn_config_overlay` containing the generated file(s).
5. Pack priority: above the bundled mod-jar resources, below user-installed datapacks. Default-enabled, force-enabled (cannot be disabled by the user from the in-game pack selector — disabling it would only re-expose the bundled defaults, which is identical behavior to setting all config keys to defaults).

### Disabling a structure

`world.structures.ancient_ruins.enabled = false` produces a structure_set JSON with an empty `structures` array:

```json
{
  "structures": [],
  "placement": {
    "type": "minecraft:random_spread",
    "salt": 20005897,
    "spacing": 56,
    "separation": 20
  }
}
```

This keeps the placement registered (so other systems referencing the ID still find it) but no structure variant gets selected, so nothing generates. Cleaner than omitting the JSON, which would risk re-loading the bundled default.

(Note: even with TOML as the user-facing config format, the *generated* overlay is still vanilla-format JSON because that is what Minecraft's data-pack loader expects. TOML only governs the input file; the on-disk overlay we register is JSON.)

### Cross-loader pack registration

A `com.chronodawn.platform.PackProvider` interface is implemented per loader:

```java
public interface PackProvider {
    void registerOverlayPack(String packId, PackResources pack);
}
```

- **Fabric implementation**: registers a `RepositorySource` via `ResourceManagerHelper` (or equivalent depending on MC version) that yields the pack on each query.
- **NeoForge implementation**: subscribes to `AddPackFindersEvent` (server type) and adds a `RepositorySource` yielding the pack.

The actual `PackResources` implementation is shared (in `common/shared/...`) and wraps the in-memory generated JSON.

### Validation rules (Ancient Ruins)

- `spacing` must be > 0 and ≤ 4096 (vanilla `RandomSpreadStructurePlacement` limit)
- `separation` must be ≥ 0 and < `spacing`
- `salt` must fit in a signed 32-bit int (no other constraint)
- `enabled` must be boolean

Any value failing validation logs an error and reverts to default for that field only.

### Cross-version coverage

- **Primary implementation targets**: 1.21.1 + 1.21.11 (oldest commonly used + latest)
- After validating these two, port to remaining versions (1.20.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10) one at a time
- API differences in `Pack` constructor / `PackResources` interface / `RepositorySource` registration are absorbed in the existing `compat/` package (matches established pattern)
- 1.20.1 is Fabric-only; no NeoForge-side work needed for that version
- Risk acknowledged: each new MC version added in future will need verification of the pack-registration code path (added to roadmap document as ongoing maintenance)

## Files

### New

- `common/shared/src/main/java/com/chronodawn/config/ChronoDawnConfig.java` — immutable record + nested types
- `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java` — read / write / validate via night-config
- `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java` — single source of truth for default values
- `common/shared/src/main/resources/chronodawn-default-config.toml` — bundled commented default (used to materialise the file on first run; lives in resources so the comment text stays under version control)
- `common/shared/src/main/java/com/chronodawn/worldgen/runtime/RuntimeStructureOverlay.java` — generates structure_set JSON from config
- `common/shared/src/main/java/com/chronodawn/platform/PackProvider.java` — interface
- `common/shared/src/main/java/com/chronodawn/platform/InMemoryPackResources.java` — shared pack-resources impl wrapping a `Map<ResourceLocation, byte[]>`
- `fabric/base/src/main/java/com/chronodawn/fabric/platform/FabricPackProvider.java`
- `neoforge/base/src/main/java/com/chronodawn/neoforge/platform/NeoForgePackProvider.java`
- Per-version `compat/` shims as needed (only added when API differs from the primary 1.21.1 / 1.21.11 implementations)
- Unit tests under `common/gametest` or each version's `test` source set:
  - `ConfigLoaderTest`: missing / partial / invalid / unknown-key / schema-version cases
  - `RuntimeStructureOverlayTest`: known config → expected JSON
- `docs/configuration.md` — user-facing config reference
- `docs/modpack-integration.md` — modpack creator guide (general; extends as later sub-projects ship)

### Modified

- Mod entry points (Fabric `ChronoDawnFabric.java` family, NeoForge `ChronoDawnNeoForge.java` family) — wire config loading and pack registration during init
- Fabric per-version `build.gradle` files — declare and `include` (JAR-in-JAR) the night-config TOML dependency. NeoForge already provides it transitively, so no NeoForge-side dependency change is needed.
- `CHANGELOG.md` — record under `[Unreleased]` `### Added` (single line, no new sub-group; sub-grouping can be revisited later if multiple config-related entries accumulate)
- `README.md` (Features section) — one-line mention that the mod is configurable, link to `docs/configuration.md`

### Untouched (deliberate)

- `common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json` — the bundled JSON keeps current default values. The overlay only takes effect when the user customizes config or chooses identical-but-explicit values (a no-op overlay does no harm).

## Documentation deliverables

### `docs/configuration.md`

- File location and JSON format
- Schema reference (currently: only Ancient Ruins; this section grows PR-by-PR)
- Two clearly-marked admonitions:
  - "**Changes require server (or single-player world) restart.** `/reload` does not affect world generation."
  - "**Existing chunks are not retroactively updated.** New values apply only to chunks generated after the change."
- Behavior on invalid / missing values

### `docs/modpack-integration.md`

- How to bundle a `chronodawn.json` in a modpack distribution
- Conflict resolution: user datapacks override the mod's overlay (intentional)
- "Future sections" placeholders for scripting events (sub-project C) and cross-mod compat (D), so this doc is the canonical entry point for pack creators going forward

## Verification plan

### Unit tests (in `common-1.21.x:test`)

- `ConfigLoader`:
  - Missing file → defaults written, returned config matches defaults
  - File with only `$schema_version` → defaults filled in
  - Invalid `spacing` (= 0, negative, > 4096) → reverted to default, error logged
  - Unknown key (e.g. `world.foo`) → kept in input ignored, warning logged
  - Schema version unknown (e.g. 99) → warning, partial parse
- `RuntimeStructureOverlay`:
  - Default config → identical JSON to the bundled `ancient_ruins.json`
  - Custom spacing → JSON reflects custom value
  - `enabled: false` → JSON has empty `structures` array

### Manual integration

1. Build for **1.21.1** (Fabric + NeoForge). Run client.
2. Confirm `config/chronodawn.json` is written on first run.
3. Stop, edit `spacing` to `16`, restart, generate a new world. Confirm visibly higher density of Ancient Ruins.
4. Stop, set `enabled: false`, restart, generate a new world. Confirm no Ancient Ruins appear.
5. Repeat steps 1–4 on **1.21.11** (Fabric + NeoForge).

### Full verification

After per-version manual checks pass for 1.21.1 and 1.21.11, run `./gradlew checkAll` to validate all 11 versions × 2 loaders. Any version-specific compat shim issue surfaces here.

## Risks (recap from prior discussion)

| Risk | Mitigation |
| --- | --- |
| Existing worlds do not reflect new config | Documented in `docs/configuration.md` |
| `/reload` does not apply worldgen config | Documented in `docs/configuration.md` |
| User datapack overrides our overlay | Intentional; documented in `docs/modpack-integration.md` |
| Per-MC-version pack-registration API churn | Absorb in `compat/`; verify primary 1.21.1 / 1.21.11 first; record as ongoing maintenance in roadmap |
| Generated JSON is malformed | Unit-tested; integration tested in dev client |

## Open questions

All resolved at design time:

- ✅ Free-adjustment vs preset choices → free (user confirmed)
- ✅ Include ON/OFF toggle in addition to numeric tuning → yes
- ✅ Default values → match current bundled JSON exactly (spacing=56, separation=20, salt=20005897)
- ✅ Config file format → TOML via night-config (NeoForge convention; comments allowed in default file)
- ✅ CHANGELOG grouping → flat entry under `### Added`, no new sub-group for now

## Extension pattern (for future tunables)

The infrastructure built in this PR is designed so subsequent tunables (boss HP, ore generation, time distortion strength, etc.) can be added without re-architecting. The recipe for adding a new tunable is:

1. **Add a nested record** to `ChronoDawnConfig`. Example: a new `BossesConfig` record under `gameplay.bosses.<entity_id>`.
2. **Add defaults** for every field to `ConfigDefaults`. Defaults must match the current hardcoded behavior so adding a config option is a no-op for users who never touch the file.
3. **Extend the bundled `chronodawn-default-config.toml`** so the commented default file ships an explanation of the new option. This is the contract: every user-facing field must have an inline TOML comment.
4. **Wire the consumer**:
   - For worldgen JSONs: add an overlay generator (similar to `RuntimeStructureOverlay`) that emits the relevant `data/.../*.json` from the config value.
   - For runtime values (e.g. boss attribute multipliers): inject the config record into the consumer (entity attribute setup, item handler, etc.).
5. **Update `docs/configuration.md`** with the new section. The reference file is the canonical source of truth for what is configurable.
6. **Add unit tests** mirroring the patterns in `ConfigLoaderTest` / `RuntimeStructureOverlayTest`.

The extension does **not** require:

- Changes to `PackProvider` (loader-specific) unless a new resource type needs registration.
- Changes to `InMemoryPackResources` unless the resource layout changes.
- Changes to the schema-version handling unless a breaking rename is introduced (in which case `ConfigLoader` gets a migration entry).

This separation of "infrastructure (this PR)" from "tunables (subsequent PRs)" is the main reason sub-project A is being scoped to Ancient Ruins first: it forces the infrastructure to be designed for extension.

## Out of scope for this PR (explicit)

- Other tunables (boss HP, ore rates, time distortion, etc.) — follow-up PRs in sub-project A
- Per-biome control of where Ancient Ruins generate — would need a `biome_tag` field; defer until a user requests it
- Hot reload of worldgen — vanilla limitation
- Auto-config-UI — out of audience scope

## Done criteria

- All files in the "Files / New + Modified" section exist or are updated.
- Unit tests pass on 1.21.1 and 1.21.11.
- Manual integration verification (steps 1–4) passes on both versions.
- `./gradlew checkAll` succeeds.
- `docs/configuration.md` and `docs/modpack-integration.md` reviewed for clarity.
- CHANGELOG entry written.
- Roadmap document's status tracker updated to reflect that the first PR of sub-project A has shipped.
