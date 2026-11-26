---
name: verification-commands
description: Use when building, cleaning, running, testing, validating or releasing Chrono Dawn, or before a PR. Gradle command patterns per Minecraft version and loader, unit tests, GameTest, prodSmokeTest, upgradeTest, scenarioCheck, validation tasks, TLA+ model checking (verifyFormalModels), checkAll, parallel verification limits and release tasks.
---

# Build, test and verification commands

Moved out of `CLAUDE.md` so that the always-loaded instructions stay short.
Supported versions, hotfix mappings and the default target are defined in
`gradle.properties` (`supported_mc_versions`, `hotfix_mc_versions`). Read them
there instead of keeping a list here. `<version>` below is the dotted version
(`1.21.1`) in `-Ptarget_mc_version=` and the underscored form (`1_21_1`) in task
names.

## Command patterns

- Clean: `./gradlew clean<version>` (for example `clean1_21_1`, `clean26_3`), or `./gradlew cleanAll`. `cleanAll` excludes 1.21.3.
- Build: `./gradlew build<version>`, or `./gradlew buildAll` for release (excludes 1.21.3). `build26_3` is the default target. `build1_21_3` uses the 1.21.2 modules and is for testing only.
- Run client: `./gradlew runClientFabric<version>` and `./gradlew runClientNeoForge<version>` (NeoForge exists for 1.21.x and 26.x only; 1.20.1 uses Fabric and Forge).
- Unit test (JUnit only, not GameTest): `./gradlew :common-<version>:test -Ptarget_mc_version=<version>`. Unit tests for all versions run inside `checkAll`'s `buildAll` step (`./gradlew buildAll -Pbuildall_run_tests=true`). Plain `buildAll` and `build<version>` skip them. There is no `testAll` task.
- GameTest: `./gradlew :fabric:runGameTest -Ptarget_mc_version=<version>` and `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=<version>`. `./gradlew gameTestAll` runs all versions including 1.21.3 runtime verification (1.21.2 and 1.21.3 run sequentially).
- GameTest servers may omit custom dimensions even when normal game servers load them. A dimension-dependent test that skips because its dimension is absent is not runtime verification. Use a normal client or production server for the final cross-dimension behavior check.
- **Parallel GameTest**: `gameTestAll` groups configurations by Minecraft version. 1.20.1 and 1.21.1 run in parallel, while 1.21.2 and 1.21.3 run sequentially in the same thread (they share modules). Within each version, fabric and neoforge run in a single Gradle process to avoid common module build conflicts. Each version uses a separate directory (fabric/1.20.1, fabric/1.21.1, etc.) with its own `.gradle/architectury/` path.

## Production smoke test

Headless, no display required. Recommended after Mixin config changes or when adding a new Minecraft version.

- `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=<version>` boots a real Fabric dedicated server with the built mod and confirms a fresh world is created without crashing.
- `./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=<version>` does the same for NeoForge.
- Not supported for Forge (1.20.1). `runProd`'s underlying installer path is unsupported for Forge < 50, see `gradle/shared/prod-run.gradle`.
- Unlike `runGameTest`/`runGameTestServer` (dev/mapped Loom environment), this launches an installer-provisioned production server, so it also catches Mixin remapping and injection issues that only surface outside the dev environment.
- Uses `build/prod-smoke/<loader>-<version>/`, separate from the manually-verified `run-prod/` used by `runProd`. Cleaned up by `clean<version>`/`cleanAll`.
- Also fails on server log WARN/ERROR/FATAL lines not matched by `prod-smoke-log-ignore.txt` (`prod_smoke_log_check=true`). Fix the cause rather than adding an ignore entry. Entries there are known issues pending a fix.

## Upgrade test

Runs on 1.21.1 and 26.3 (`upgrade_test_mc_versions`). Opens a game window and is not part of `checkAll`. Run it before a release that touches registries, worldgen, configs, portals, advancements, equipment, or progression, see `docs/release_process.md`.

- `./gradlew :fabric:upgradeTest :neoforge:upgradeTest -Ptarget_mc_version=1.21.1` (or `26.3`) builds a world with `upgrade_test_baseline_version` (0.8.0, or 0.13.0 on 26.3 via `props/26.3.properties`) and the latest release tag reachable from HEAD, upgrades it to the built JAR on production server+client runtimes, and fails on crashes or lost/changed data. Report: `<loader>/<version>/build/upgrade-test/report.txt`.
- Run without the agent sandbox (network, JVM forks, local ports). In Claude Code that means running outside the sandbox. In Codex, use a sandbox mode or approval that allows these. `-Pupgrade_test_sources=0.8.0,0.13.0`, `-Pupgrade_test_candidate_jar=<jar>` and `-Pupgrade_test_ports=<server>,<rcon>` override the defaults (`-Pscenario_check_ports` for scenarioCheck). Without them the driver reserves a free server/RCON port pair at run time (from 25700-25999, held with a lock file under `~/.cache/chronodawn/verify-locks`), so several checkouts can run the same task at once without `-P`.
- Driver and setup layers live in `scripts/upgrade_test/`. New persisted content needs a layer named after the version that introduces it. Log ignores go in `upgrade-test-log-ignore.txt` (known issues only).
- `./gradlew testUpgradeTestScripts` runs unit tests for the driver (they bind local ports, so also without the sandbox).

## Parallel verification

Several worktrees or agents on one machine. Details in `docs/developer_guide.md` "Running verification in parallel".

- Run at most 2 verifications at the same time (`checkAll`, `scenarioCheck`, `upgradeTest`). The `scenarioCheck` and `upgradeTest` drivers enforce it with lock files and wait for a free slot. Change the limit per machine with the environment variable `CHRONODAWN_MAX_PARALLEL_VERIFY`. Start `checkAll` only when fewer than the limit are running, because it is not slot-limited.
- `checkAll`, `gameTestAll`, `upgradeTest`, `scenarioCheck` and `prodSmokeTest` fail before they start when the checkout's volume has less than `min_free_disk_gb` (20 GB) free. Free space as the message says instead of passing `-Pmin_free_disk_gb=0`, unless the owner asks for it.
- Before a PR, classify the diff with `python3 scripts/merge_tier/classify.py`, run the `independent-verification` skill, and put its Verification section with a `Merge tier:` line in the PR body (`docs/merge_policy.md`). The `Merge tier` CI check fails when the declared tier is lower than the diff's.
- Before a PR, run `checkAll` for all versions. Limit it to the changed versions (`build<version>`, `:fabric:runGameTest -Ptarget_mc_version=<v>`) only when the impact is clearly confined to them, and say so in the PR body.

## Scenario check

1.21.1 and 26.3, tested on macOS. Opens a game window and is not part of `checkAll`. See `docs/scenario_check.md` and the `scenario-check` skill.

- `./gradlew :neoforge:scenarioCheck -Ptarget_mc_version=1.21.1 [-Pscenario_check_scenarios=a,b]` (or `26.3`) boots the built JAR on a fresh world with production server+client, runs `scripts/scenario_check/scenarios/*.json` over RCON, and takes in-game screenshots. Use it instead of asking the owner to check in game. Read `<loader>/<version>/build/scenario-check/summary.md`, look at each shot against its note, then attach `sheet.png` to the PR or task for the owner.
- Run without the agent sandbox. Screenshots come from the game's framebuffer through a Java agent, so they need no Screen Recording permission and work while the Mac is locked.
- `./gradlew testScenarioCheckScripts` runs unit tests for the driver.
- Before adding or editing a scenario, read the `scenario-check` skill. Wait on conditions (`settle`, `await`, `screen`, `input` with `until`) instead of fixed sleeps, and check the scenario's time in `summary.md`.
- When a change needs an in-game check, do not stop and wait for the owner. If RCON commands and screenshots can cover the check, add or extend a scenario, run it on both 1.21.1 loaders (and both 26.3 loaders when the change affects 26.3), fix what the shots show is wrong, commit the scenario with the change, attach `sheet.png` to the PR or task, and continue through the PR. The owner answers OK or NG per shot later.
- A scenario can also act as the player: one right-click or left-click on what the crosshair points at, opening a screen such as a trader, and checking the open screen's title (`input` and `screen` steps).
- List only what a scenario cannot cover (typing or clicking inside a GUI screen, held or repeated input such as mining in survival, Minecraft versions other than 1.21.1 and 26.3) as manual checks in the PR body or task, and still continue. A `server-log` FAIL from a WARN that an existing task already tracks does not block the change. Name that task in the report.

## Resource validation

- `./gradlew validateResources` checks JSON syntax and cross-references (blockstate→model, model→texture).
- `./gradlew validateData` checks data-pack cross-references (tag entries → registered IDs, recipe references, 1.21.4+ client items coverage).
- `./gradlew validateTranslations` does cross-version translation key validation (entities, spawn eggs).
- `./gradlew validateAll` runs every file-only validation that `checkAll` runs (`checkall_extra_tasks`). CI (`.github/workflows/ci.yml`) runs it with `-Plang_required_locales=en_us`, which turns missing non-English keys into warnings. The default in `gradle.properties` keeps `ja_jp` required for `checkAll` and releases. CI also passes `-Pvalidation_only=true`, which makes `settings.gradle` skip the version subprojects so Loom does not set up Minecraft. Validation tasks must therefore stay root-project, file-only tasks.
- `validateVersionSync` fails when per-version copies that `gradle/version-sync.lock` records as identical start to differ. Apply the change to every listed version. Only when a version must differ for an era-specific reason, run `./gradlew updateVersionSyncLock` and commit the lock. In Claude Code, the PostToolUse hook in `.claude/settings.json` prints the stale copies right after an edit. Codex has no such hook, so run `./gradlew validateVersionSync` after editing a per-version copy.
- CI (`.github/workflows/ci.yml`) runs `validate` (`validateAll` and `verifyFormalModels`), `build` (build and unit tests), `prod-smoke` (`:fabric:prodSmokeTest`, `:neoforge:prodSmokeTest`) and `gametest` (`:fabric:runGameTest`, `:neoforge:runGameTestServer`) on 1.21.1 and 26.3. Fabric and NeoForge run in separate Gradle invocations: in one invocation both loaders transform the shared common module concurrently, and the NeoForge jar intermittently resolved `@ExpectPlatform` calls to the Fabric implementation (`NoClassDefFoundError ...runtime.fabric.*Impl`). Never save Minecraft JARs, mappings or world data as CI artifacts. A failing `prod-smoke` job prints the server log tail in its last step.
- GitHub Actions: pin every `uses:` to a full commit SHA with the version as a trailing comment, and pin `runs-on` to a fixed image (`ubuntu-24.04`), never `@vN` or `ubuntu-latest`. Update the actions with `GITHUB_TOKEN=$(gh auth token) mise exec pinact@5.0.1 -- pinact run -u` (run without the sandbox), then check that CI passes.
- When a Minecraft rename breaks a version that CI does not build, add a rule to `scripts/era_symbol_rules.json` (`validateEraSymbols`) in the same change.
- `docs/bug_prevention.md` maps past bug classes to these checks and lists rejected checks. A new bug class goes there first. The pre-commit hook (`scripts/hooks/install.sh`, Git 2.54+) runs `validateAll`.

## Formal verification (`verifyFormalModels`)

- `./gradlew verifyFormalModels -Pvalidation_only=true` model-checks every `specs/tla/<Name>.tla` with TLC through a `verifyTla<Name>` task. It fails on a parse error, a violated invariant or property, or a deadlock, and prints the counterexample trace. Models, checked properties and how to add one are in `docs/formal_verification.md`.
- CI runs it in the `validate` job after `validateAll`, and `checkAll` depends on it. It is not in `validateAll` or `checkall_extra_tasks`, so the pre-commit hook does not run it.
- The first run downloads `tla2tools.jar` from the TLA+ GitHub release into the Gradle cache. Run Gradle without the sandbox as usual. A sandboxed `curl` or `java` run against the release needs `github.com` and `release-assets.githubusercontent.com` allowed.
- When you change a class that a model lists in its header (the portal handlers for `PortalProgression.tla`), update the model in the same commit and run the task.

## Full verification (`checkAll`)

Recommended before commits and PRs. `./gradlew checkAll` first runs `verifyFormalModels` as a task dependency, then runs, in sequence:

1. `cleanAll`
2. `validateResources`
3. `validateTranslations`
4. `buildAll` (release build for every version; also runs unit tests in the same parallel per-version builds)
5. `gameTestAll` (GameTests including the 1.21.3 runtime verification)

When capturing `checkAll` (or any long Gradle run) output, redirect it to `<checkout>/build/checkall.log` using the absolute path of the current checkout (the worktree root when working in a worktree). Do not use a fixed filename under `$TMPDIR` or `/private/tmp/claude-501/`. Every session shares that directory, so concurrent runs overwrite each other's logs. Sandboxed and unsandboxed commands also resolve `$TMPDIR` to different directories, so a log written by one may not be visible to the other. The root `build/` is gitignored and survives `cleanAll`.

## Release

- `./gradlew collectJars` collects release JARs from all versions into `build/release/`.
- `./gradlew release` is the full pipeline: cleanAll → buildAll → collectJars.
- `./gradlew releaseModrinth` (requires `MODRINTH_TOKEN`), `./gradlew releaseCurseForge` (requires `CURSEFORGE_TOKEN`) and `./gradlew releaseAll` upload the JARs in `build/release/`.
- See `docs/release_process.md` and the `versioning` skill.
