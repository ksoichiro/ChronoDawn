# Upgrade Test Automation Design

Date: 2026-10-23

## Intent

Every release that touches registries, worldgen, configs, portals,
advancements, equipment, or progression needs an in-place upgrade check
(`docs/release_process.md`, "Verification"). Today that check is partly
manual and partly ad-hoc:

- Server-side checks were scripted by an agent under `build/upgrade-test/`,
  which `cleanAll` deletes, so the scripts are rewritten every release.
- The check list is decided anew in each session. For 0.14.0 the maintainer
  judged "boot + `locate`" insufficient and items were added mid-release.
- The maintainer opens a client to look at the upgraded world, which blocks
  the release on human time.

The goal is one command that, without human input, tells the maintainer
whether upgrading an existing 1.21.1 world to the release candidate crashes
or visibly breaks anything.

What the maintainer cares about, in their order:

1. **No crash** after the upgrade, on server or client.
2. **No obvious anomaly a player would notice**, for example items in an
   inventory vanishing or turning into something else.
3. **Advancements and any other data the mod persists** survive the upgrade.

## Goals

- A Gradle task, provisionally `upgradeTest`, that runs the full upgrade
  check for Minecraft 1.21.1 on both Fabric and NeoForge.
- Two upgrade sources per run:
  - **A pinned baseline version**, currently `0.8.0`, the version adopted by
    the largest modpack. It must be a single configuration value (for
    example in `gradle.properties`) so it can be moved when pack adoption
    changes.
  - **The previous release**, resolved automatically from git tags: the
    latest `vX.Y.Z` tag reachable from HEAD. If HEAD is exactly that tag
    (a re-run after release), use the tag before it. Release verification
    runs before the commit that bumps `mod_version`, so "the highest tag
    lower than `mod_version`" would skip the version players are on.
    (Builder's call, recorded here.)
- The old-version world is **generated automatically** by the old JAR on
  every run. A hand-made world would not work because "previous release"
  changes with every release.
- A **fixed regression test**: assertions do not follow each release's
  changes. When an intentional change breaks an assertion, the maintainer
  reviews the failure and updates the test.
- Assertions are about **preservation**: what existed before the upgrade
  still exists after it with the same value. Additions (new advancements,
  new items, new config keys, new worldgen in ungenerated chunks) must not
  fail the test.
- Client coverage: an automatically launched production client joins the
  upgraded world, so client-only crashes and missing model/texture errors
  are caught. A game window appearing on the maintainer's Mac for tens of
  seconds is acceptable.
- Failure output names the exact check, the expected value, and the actual
  value, so an agent can report it without re-running anything.

## Non-goals

- Other Minecraft versions or loaders. 1.21.1 is the LTS/modpack target.
  The design should not block adding versions later, but nothing beyond
  1.21.1 is built now.
- Inclusion in `checkAll` or any other frequently run path. This task is run
  by hand or by an agent before a release. A runtime of tens of minutes is
  acceptable.
- CI. This is a solo project with local verification only.
- Visual judgments such as terrain seams at the border between old and new
  chunks. These stay a short manual note in `docs/release_process.md`, needed
  only when worldgen changes.
- Screenshot comparison.
- Researching which version modpacks currently use. Updating the baseline
  value is a separate, occasional decision by the maintainer.

## Anti-goals

- A test that fails on every release because something was added. That
  would train the maintainer to ignore it.
- A test that silently passes when its setup did not happen (for example the
  boss never spawned, so "boss defeated" was never set). Every setup step
  must be asserted on the old version before the upgrade, so a broken setup
  fails loudly instead of comparing nothing with nothing.
- Fixed `sleep`s as the synchronization mechanism. The 0.13.0/0.14.0 driver
  used a FIFO plus sleeps, which is fragile.

## What must survive the upgrade

From the 1.21.1 code (`common/1.21.1`, `common/shared`). The builder should
re-scan for anything added since.

| Data | Where it lives |
|------|----------------|
| Global state: entered dimension, portal stabilized, Time Tyrant defeated | `chronodawn_global_state` SavedData |
| Independent dimension time | `chronodawn_time` SavedData |
| Boss room protection areas and their defeated flags | `chronodawn_block_protection` SavedData |
| Time Keeper village placement | `chronodawn_time_keeper_village` SavedData |
| Boss spawn data | `BossSpawnData` |
| Per-player progress: defeated bosses, Chronos Eye, stabilized portals, shield cooldowns | `PlayerProgressData` (`*_player_progress`) |
| Block entities: Time Well stone, boss room door, boundary marker, Clock Tower teleporter, cooldown activator | `CompatBlockEntity` subclasses |
| Custom entity save data: bosses, Moment Creeper, Ticking Sheep, Time Keeper, boats | `addAdditionalSaveData` overrides |
| Player inventory with mod items, including a damaged tool and stacked items | player data |
| Container contents with mod items | chests in the overworld and the Chrono Dawn dimension |
| Placed mod blocks, both dimensions | chunk data |
| Granted advancements | player advancement data |
| Existing config file | `config/chronodawn.toml`, expected byte-identical (existing config is never rewritten, see `docs/configuration.md`) |
| Already generated terrain | per-block-id counts of full-status chunks, as done for 0.14.0 |

Plus one forward check: forceload a biome-specific area that was never
generated and confirm new worldgen appears there.

### Setup is layered by the version that introduced the content

The old world is built by the old JAR, so it can only contain what that
version has. Setup steps and their assertions are therefore grouped by the
mod version that introduced the content (for example a base layer for
content present in 0.8.0, a layer for Chronite from 0.9.0, a layer for the
Time Well from 0.14.0). A run applies every layer whose version is less than
or equal to the upgrade source. The 0.8.0 path covers the base layer, and
the previous-release path also covers recent content, which is the most
likely to break. (Maintainer's decision.)

Layers are keyed by introducing version, not by baseline, so moving the
baseline stays a one-line config change. When new persisted data is added
in a future release, a layer for it is added to the test.

## How it works (decided)

- **Server runtime:** reuse the production server that `prodSmokeTest`
  provisions (`gradle/shared/prod-run.gradle`), not the dev Loom
  environment. This also catches remapping and Mixin problems.
- **Client runtime:** reuse the production client that `runProd` launches.
  The client joins the dedicated server (offline mode) rather than opening a
  singleplayer copy, so one world and one server log carry both checks.
- **Old JARs:** downloaded from Modrinth by project id `yEsoyNev`
  (`/v2/project/yEsoyNev/version?game_versions=["1.21.1"]`, filter by
  `version_number` and loader) and cached outside `build/` so `cleanAll`
  does not force a re-download. The slug endpoint returns 404.
- **Old-run dependencies:** `props/1.21.1.properties` at `v0.8.0` pins the
  same Fabric Loader, NeoForge, Architectury, and Fabric API versions as
  HEAD, so the old run reuses the current `prodMods`. A future baseline that
  needs different dependency versions would need this revisited.
- **Commands:** sent over RCON, with each step waiting for a confirmed result
  instead of sleeping.
- **Fixed seed and fixed coordinates**, so `locate` results and chunk
  comparisons are reproducible.
- **Flow per loader and per source version:**
  1. Start the server with the old JAR on a fresh world.
  2. Start the client and join, so a real player exists.
  3. Build the state in the table above, then assert every piece of it on
     the old version and record the values.
  4. Disconnect the client, save, stop the server, snapshot the world.
  5. Swap in the release candidate JAR from the current build.
  6. Start the server, join with the client again, and re-read every
     recorded value.
  7. Compare. Check server and client logs for crashes and WARN/ERROR lines.
- **Log checks** apply only to the boots after the upgrade (server and
  client). Warnings from an old release can never be fixed, so they do not
  belong in an ignore list. A failure on the old version is detected by a
  crash or by the pre-upgrade setup assertions instead. (Builder's call,
  recorded here.) The post-upgrade check follows the `prod_smoke_log_check`
  model: a project ignore list of regexes, where every entry is a known
  issue. Known benign lines
  from earlier runs: NeoForge's "version differences" warning and Fabric's
  `No data fixer registered for <entity>` errors, which appear in both old
  and new versions.
- **Port handling:** each server uses a non-default port. Running Fabric and
  NeoForge servers at the same time hit "FAILED TO BIND TO PORT" in 0.14.0,
  so loaders either run sequentially or get distinct ports.
- **Location:** the task and its scripts live in this repository, not in
  the `gradle/shared` submodule, because the checks are ChronoDawn-specific.
  Output goes under `build/` (a fresh run is fine), downloads go to a cache
  that survives `cleanAll`.
- **Documentation:** `docs/release_process.md` "Verification" points to the
  task and keeps only the residual manual visual check.

## Left to the builder

- Language for the driver and the comparison (Groovy inside Gradle, or a
  Python script called from Gradle as `validateSoundSubtitles` and the
  anvil-parser checks already do).
- How to create states that need real gameplay, such as "boss defeated" or
  "portal stabilized". Prefer spawning and killing the entity or triggering
  the real code path. If a state cannot be reached that way, editing the
  saved NBT before the upgrade is acceptable. If neither works, drop that
  item and list it in the task's output as not covered.
- How to read values back: RCON (`data get`, `advancement`), offline NBT
  parsing of the saved world, or both.
- How the client is told to join and when it is shut down. Quick-play
  arguments and killing the process after a fixed number of seconds in the
  world are both acceptable, as long as a client crash is reported as a
  failure.
- Exact task names and the layout of the assertion definitions.
