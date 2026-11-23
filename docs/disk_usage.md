# Disk Usage with Parallel Work

Running two or three checkouts at once (the main checkout plus worktrees)
repeatedly filled the disk and stopped builds and tests. This document records
where the space goes, how it grows with the number of checkouts, and the
policy chosen to keep it under control. It continues the `.gradle/` work in
[Developer Guide](developer_guide.md#gametestall-architecture) and does not repeat it.

Measured on 2026-11-23 on a 460 GB APFS volume with about 15 GB free.

## Where the space goes

### Shared by every checkout (does not grow with parallel work)

| Location | Size | Notes |
|----------|------|-------|
| `~/.gradle/caches/fabric-loom` | 21 GB | `minecraftMaven` 7.9 GB, `assets` 2.1 GB, one directory per Minecraft version |
| `~/.gradle/caches/build-cache-1` | 21 GB | 2475 entries, none above 100 MB. About half (9.9 GB) was not touched in the last 2 days |
| `~/.gradle/caches/neoformruntime` | 12 GB | `intermediate_results` 8.1 GB (1101 entries) |
| `~/.gradle/caches/minecraftforge` | 11 GB | ForgeGradle, only needed for 1.20.1 |
| `~/.gradle/caches/forge_gradle` | 10 GB | `mcp_repo` 4.3 GB, minecraft_user_repo 2.2 GB |
| other | about 3 GB | `modules-2`, wrapper caches |

Total: about 76 GB. This is the floor. It does not depend on how many
checkouts run.

### Per checkout (grows linearly with parallel work)

| Location | Size | Notes |
|----------|------|-------|
| `.gradle/` | 0.3 GB fresh, about 2.9 GB after `checkAll` | Per-version `loom-cache/minecraftMaven` (access widener makes the jars project specific). Cannot be shared, see the Developer Guide |
| `run-prod/cache/` | 1.9 to 2.8 GB | Downloaded server, installer, library and asset files. Identical across checkouts |
| `run-prod/instances/` | 0.3 GB | Per-checkout mods and worlds |
| `<loader>/<version>/build/` | 0.45 to 0.6 GB each for 1.21.1 and 26.3 | See below |
| `common/<version>/build/` | 0.05 to 0.08 GB | |

`<loader>/<version>/build/` is mostly test output, not compile output. For
`fabric/26.3/build` (465 MB): `scenario-check/` 311 MB (a 210 MB server copy),
`prod-smoke/` 63 MB, `libs/` 63 MB, `resources/` 25 MB.

A checkout that ran `checkAll`, a scenario check and a prod smoke test costs
about 5 GB. One that only edited files and built a single version costs well
under 1 GB.

### Transient

`upgradeTest` copies the server with `cp -cR` on macOS (APFS clone, so it
shares blocks until changed). The world before the upgrade is a real copy
(`shutil.copytree`). The output directory
`<loader>/<version>/build/upgrade-test/<source>` is rebuilt on the next run but
never removed after a run.

## Growth with parallel work

```
used = shared floor (about 76 GB)
     + (number of checkouts) x (3 to 5 GB)
     + transient copies (1 to 3 GB while a heavy test runs)
```

With the main checkout plus two worktrees this is about 15 GB, plus the
transient part and headroom. A volume with 15 GB free is therefore already at
the limit, which matches the observed 8 GB low point.

Leftover worktree directories are the other source. Removing a worktree
without deleting its directory, or a failed cleanup, left about 5 GB and 13
empty directories.

## Decisions

The design target is 2 concurrent tasks. 3 is allowed only when at least 25 GB
is free. An external disk for `~/.gradle` is not available, so every change
below has to fit on the internal disk.

### Operating rules (adopted)

1. **Remove the worktree when the task ends.** After the branch is merged or
   dropped, run `git worktree remove <path>` and confirm the directory is
   gone. `git worktree list` and `ls ~/.fuda/worktrees/<project>` must agree.
   Delete empty directories that `git worktree list` does not show. This
   releases `.gradle/`, `build/` and `run-prod/` together, which is the main
   fix for build-up.
2. **Run at most one heavy task at a time across all checkouts.** Heavy means
   `checkAll`, `gameTestAll`, `upgradeTest`, `scenarioCheck` and
   `prodSmokeTest` on several versions. They also use fixed ports, so they
   collide anyway. Light work (edit, `validateAll`, one-version build, unit
   test) can run in parallel.
3. **Check free space before starting a heavy task.** Stop and clean up when
   less than 20 GB is free (25 GB for the third concurrent checkout).
   The heavy tasks check 20 GB themselves (build change 4). The 25 GB for a
   third checkout stays a manual check with `df -h /`.
4. **Clean up after heavy tasks.** Run `./gradlew clean<version>` or
   `cleanAll` once the result is recorded. Delete
   `<loader>/<version>/build/upgrade-test` and `build/scenario-check` server
   copies when the report is saved.
5. **Trim the shared Gradle caches by hand once a month**, or whenever free
   space falls below the threshold: delete `~/.gradle/caches/fabric-loom/<v>`
   for versions that are no longer supported and the `.broken-*` directories
   under `run-prod/cache/`. Gradle recreates anything it still needs.

### Build changes (adopted, implemented in separate tasks)

1. **Shorten the Gradle build cache retention to 2 days.** Gradle keeps
   entries for 7 days by default. `buildAll` and `gameTestAll` only need the
   cache for the next phase of the same run, so 2 days keeps that benefit.
   Expected saving: about 10 GB. The setting lives in the user's
   `~/.gradle/init.d`, so the repository documents it and cannot enforce it.
2. **Share `run-prod/cache/` across checkouts.** It holds downloads only. The
   instances (`run-prod/instances/`) stay per checkout. Expected saving: 2 to
   3 GB per extra checkout. `prod_base_dir` moves cache and instances together,
   so a separate cache location needs a change in `gradle/shared/prod-run.gradle`
   (a submodule), or a symlink as a stop gap. First-time downloads from two
   checkouts at once can race, so warm the cache from one checkout first.
3. **Delete server copies and worlds after a successful scenario check,
   upgrade test and prod smoke test**, and keep the reports and screenshots.
   Expected saving: about 0.3 GB per loader and version.
4. **Add a free-space guard** to the heavy tasks. It fails fast below the
   threshold with a message that names the cleanup commands. Implemented as
   `min_free_disk_gb` (default 20 GB, see the Developer Guide section "Free
   disk space check"). The guard does not count the running checkouts, so
   the 25 GB for a third checkout stays a written rule for now.

### Rejected

- **Moving `~/.gradle` to an external disk.** None is available.
- **Sharing one `--project-cache-dir` between checkouts.** Gradle task
  history and file hashes belong to one build (see the Developer Guide).
- **Dropping `org.gradle.caching`.** It only costs the build cache size, which
  the 2 day retention already reduces, and it saves a recompile between
  `buildAll` and `gameTestAll`.
- **Shrinking `build/`.** It is test output, covered by build change 3, not
  compile output.

## Follow-up tasks

Each build change above is tracked as its own task. The operating rules live
in the Developer Guide section "Disk space with parallel work".
