# Plan: Config Foundation + Ancient Ruins Tuning

**Date**: 2026-05-09
**Spec**: [2026-05-09-config-foundation-design.md](../specs/2026-05-09-config-foundation-design.md)
**Branch**: `config-foundation`

---

## Phasing

### Phase 1 — Implementation plan (this document)
Done after this file is committed.

### Phase 2 — Infrastructure (1.21.11 only, fast iteration)

Touch only 1.21.11 sources first. Once that builds and tests pass, port outward.

**Common (lives in `common/shared/...`)**:

- `com.chronodawn.config.ChronoDawnConfig` — top-level immutable record `(int schemaVersion, World world)` with nested `World(Structures structures)` → `Structures(AncientRuins ancientRuins)` → `AncientRuins(boolean enabled, int spacing, int separation, int salt)`.
- `com.chronodawn.config.ConfigDefaults` — single source of truth, returns the default record (matches current `ancient_ruins.json`: spacing=56, separation=20, salt=20005897, enabled=true).
- `com.chronodawn.config.ConfigLoader` — public static `load(Path configDir)` returns `ChronoDawnConfig`. Internally:
  - resolves `<configDir>/chronodawn.toml`
  - if missing → copy bundled default file from classpath (`/chronodawn-default-config.toml`) to disk; log info
  - parse via night-config (`com.electronwill.nightconfig.toml.TomlParser`)
  - merge over defaults; validate per spec rules; collect errors to log; return defaults for any field that fails validation
- `com.chronodawn.platform.PackProvider` — `@ExpectPlatform` static methods: `void registerOverlayPack(String namespace, String packId, java.util.Map<net.minecraft.resources.ResourceLocation, byte[]> dataPackContent)`. Loader-specific impls register a `RepositorySource` over an in-memory `PackResources` containing the byte map.
- `com.chronodawn.worldgen.runtime.RuntimeStructureOverlay` — given a `ChronoDawnConfig`, produces `Map<ResourceLocation, byte[]>` containing `data/chronodawn/worldgen/structure_set/ancient_ruins.json`. JSON content built as a string with `String.format` (no JSON library needed; values are primitives).
- Resource: `common/shared/src/main/resources/chronodawn-default-config.toml` — commented default file (full content from spec).

**Loader-specific (lives in `fabric/base` and `neoforge/base`, with API-level details in version-specific dirs only if needed)**:

- `com.chronodawn.fabric.platform.PackProviderImpl` — implements the `@ExpectPlatform` Fabric companion. For NeoForge see below; for Fabric the cleanest path differs by version, see "Pack registration approach" below.
- `com.chronodawn.neoforge.platform.PackProviderImpl` — uses `AddPackFindersEvent` (this requires hooking the mod event bus from `ChronoDawnNeoForge`).

**Pack registration approach**:

- **NeoForge**: `AddPackFindersEvent` on `PackType.SERVER_DATA`. Pack is added with `PackSelectionConfig(true, Pack.Position.TOP, true)` so it sits above the bundled mod resources but below user datapacks.
- **Fabric**: First attempt — use `net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack(...)` if it accepts a runtime-built `Pack`; if it requires JAR-internal paths only, fall back to a Mixin into `PackRepository.<init>` that adds our `RepositorySource` to the constructor args. Mixins are already heavily used in this codebase, so the fallback fits the style.

### Phase 3 — Wiring (1.21.11 only)

- `ChronoDawn.init()` calls `ChronoDawnConfig CONFIG = ConfigLoader.load(ChronoDawnPlatform.getConfigDirectory());` and stores it in a static field. This is the early point both loaders share.
- The pack content is computed once via `RuntimeStructureOverlay.generate(CONFIG)`.
- `PackProvider.registerOverlayPack(...)` is called from each loader's entry point at the appropriate moment:
  - Fabric: from `ChronoDawnFabric.onInitialize()` after `ChronoDawn.init()`.
  - NeoForge: from `ChronoDawnNeoForge` constructor (modEventBus listener for `AddPackFindersEvent`); the `ChronoDawn.init()` runs synchronously in the constructor before the listener fires.
- `fabric/1.21.11/build.gradle`: add `implementation` and `include` for `com.electronwill.night-config:toml:3.8.3` (matching version that NeoForge already pulls in for 1.21.11).

### Phase 4 — Tests + build (1.21.11)

- Unit tests under `common/1.21.11/src/test/java`:
  - `ConfigLoaderTest`: tmp dir, missing file, partial file, invalid spacing (0 / negative / > 4096), unknown keys, schema version handling
  - `RuntimeStructureOverlayTest`: default config → byte content of generated JSON matches expected; custom config; `enabled=false` → empty `structures` array
- Run `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11` then `./gradlew build1_21_11` until clean.

### Phase 5 — Port to 1.21.1

- Same code in `common/shared` already shared. Need:
  - `fabric/1.21.1/build.gradle` — add night-config include
  - `fabric/1.21.1/src/main/java/.../platform/PackProviderImpl.java` (if API differs from 1.21.11)
  - `neoforge/1.21.1/src/main/java/.../platform/PackProviderImpl.java` (if API differs)
  - Wire calls from `ChronoDawnFabric` / `ChronoDawnNeoForge` 1.21.1 entry points
- Build + test: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1` then `./gradlew build1_21_1`.

### Phase 6 — Port to remaining versions

Versions: 1.20.1 (Fabric only), 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10.

Approach: dispatch a subagent per version, instructing it to:
1. `pwd` verify it is in `.worktrees/config-foundation/`
2. Mirror the 1.21.11 platform impl + 1.21.11 build.gradle changes into the target version
3. Run `./gradlew :common-X:test build_X` for that version
4. Report back

Per-version PackResources / Pack constructor signatures are the biggest variable; subagent verifies build, fixes API differences using existing 1.21.1 / 1.21.11 impls as references.

### Phase 7 — Documentation + CHANGELOG

- `docs/configuration.md` — file location, current schema (Ancient Ruins only), restart/existing-chunk admonitions, validation behavior
- `docs/modpack-integration.md` — bundling configs in packs, datapack precedence, future-section placeholders
- `README.md` Features section — one-line "Configurable" with link
- `CHANGELOG.md` `[Unreleased]` `### Added` — single-line entry per spec
- `docs/superpowers/specs/2026-05-09-modpack-author-readiness-roadmap.md` — flip A's status, link to this plan + the design

### Phase 8 — Verification

- `./gradlew checkAll` (clean / validateResources / validateTranslations / buildAll / testAll / gameTestAll)
- Resolve any per-version regressions
- Final `git status` clean, branch ready for merge

## Risks during implementation

- **Fabric pack-registration**: The biggest unknown. If `ResourceManagerHelper` proves insufficient, fall back to Mixin into `PackRepository.<init>`. Both are local to the loader-specific source set.
- **night-config API drift**: Keep the loader code minimal (TOML parse + map access), so library changes don't ripple. If a Fabric version pulls a different night-config version transitively (via Architectury or Fabric API), align the explicit `include` to that version.
- **Per-version Pack class signatures**: Mojang renamed/restructured Pack and PackLocationInfo across 1.20.1 / 1.21.x lines. Worst case adds a small per-version platform impl rather than a single base impl.
- **Subagent worktree pwd hazard**: Documented in `.claude/tasks.local.md`; reaffirm in each subagent prompt.

## What is NOT in this PR

(Per spec.) Other tunables, hot reload, retroactive chunk updates, server-client sync, auto-config UI, biome-list customization for Ancient Ruins, validation of disable behavior beyond the empty-structures-array check.

## Manual verification (left to user)

After my checkAll passes and the branch is ready to merge:

1. `./gradlew runClientFabric1_21_11`, generate a new world, locate Ancient Ruins (consider /locate structure chronodawn:ancient_ruins).
2. Quit, edit `config/chronodawn.toml` (set spacing=16), restart, generate a NEW world, observe density increase.
3. Quit, set `enabled=false`, restart, generate a NEW world, /locate should return "no nearby structure".
4. Repeat on `runClientNeoForge1_21_11`.

I can stage the project up to "build clean, unit tests pass, checkAll passes". The visual verification above is yours.
