# Running scenarioCheck and upgradeTest on a Linux CI runner

Result of a trial on 2026-11-27: both tasks run on a GitHub-hosted
`ubuntu-24.04` runner with Xvfb and Mesa llvmpipe software rendering. No
driver or agent change was needed. This page records what was run, what was
measured, and a proposal for putting it into CI. Nothing here is wired into
`ci.yml`.

## How it was run

`.github/workflows/linux-client-trial.yml` is a `workflow_dispatch` workflow
that is not a required check. It installs `xvfb libgl1 libglx-mesa0
libgl1-mesa-dri libegl1 mesa-vulkan-drivers libxrandr2 libxcursor1 libxi6
libxxf86vm1 libxrender1 libxtst6 libopenal1`, sets `LIBGL_ALWAYS_SOFTWARE=1`
and `GALLIUM_DRIVER=llvmpipe`, and runs the Gradle task under
`xvfb-run -a -s "-screen 0 1280x720x24"`. `upgradeTest` needs
`fetch-depth: 0` because the driver picks the previous release from the `v*`
tags. The runner reported `llvmpipe (LLVM 20.1.2)`, OpenGL 4.5 core, Mesa 25.2.

The trial uploads nothing. It prints text only (report, log tails, timings).

## Results

| Task | Loader / version | Result | Wall time of the job |
|------|------------------|--------|----------------------|
| `scenarioCheck` (`polished-stones`) | Fabric 1.21.1, NeoForge 1.21.1, Fabric 26.3, NeoForge 26.3 | PASS, 3 shots each, no image check flagged | 3.5 to 5 min |
| `scenarioCheck` (all 18 scenarios) | Fabric 1.21.1 | PASS, 126 automatic image checks all pass | 10 min |
| `scenarioCheck` (all 18 scenarios) | NeoForge 26.3 | 4 failures, see below | 11 min |
| `upgradeTest` | Fabric 1.21.1, NeoForge 1.21.1, Fabric 26.3, NeoForge 26.3 | PASS | 5 to 7 min |

What works on Linux: compiling `ScenarioShotAgent.java` and loading it with
`-javaagent`, framebuffer screenshots (1280x720), `use`/`attack` input, `screen`
title reads, and the 1.21.1 in-screen clicks and slot clicks (the
`time-keeper-*`, `chronicle-wayside-features` and `daylight-detector-craft`
scenarios pass on Fabric 1.21.1).

The NeoForge 26.3 failures are not Linux problems:

- `chronicle-wayside-features`, `time-keeper-chronite-trade` and
  `daylight-detector-craft` fail on a screen click or slot click. That is the
  existing limit in this document: those inputs are 1.21.1 only.
- `wolf-spawn-forest` failed (`wolf-in-forest` found no wolf in the forest
  biome after 95 s, 24 s on Fabric 1.21.1). It failed the same way on macOS
  with NeoForge 26.3 and Fabric 26.3, so it is not a platform problem. The
  force-loaded square around the forest nearest to 0 0 was only about 12%
  forest, and with the fixed seed the 26.3 chunks there generate no wolf at
  all (1.21.1 got one). The scenario now locates a larger forest from
  -1120 1184 and passes on both loaders of 1.21.1 and 26.3.

### Image checks under software rendering

`images.py` did not flag anything. Mean brightness over all shots was 57 to
188 (floor 15), missing-texture magenta was 0.0% except two shots at 0.2%
(limit 0.5%), and no frame was single-color. No threshold or `expect` change
is needed.

### Resources (peak over the run, one job)

| Item | Measured |
|------|----------|
| Runner | 4 vCPU, 16 GB RAM, 145 GB disk |
| Peak used memory | 4.8 to 6.6 GB (server plus client JVM) |
| Disk used by the run | about 1 to 2 GB (`df` before and after) on top of the 58 to 64 GB the image starts with (the runtime cache under `~/.cache/chronodawn` is 0.7 to 0.9 GB, Gradle home 1.2 to 1.8 GB) |
| Free disk | 82 to 87 GB left, so `min_free_disk_gb: 0` (already set in `ci.yml`) is not needed for these tasks, but it is harmless |

## Proposal for CI (to be decided by the owner)

- Do not add it to `ci.yml` or to the required checks. The hosted runner is
  stable enough, but a game client adds flake risk (for example the wolf
  spawn) that would block merges.
- Run it as a separate workflow. Two options:
  1. Nightly on `main`, only on days with new commits (as proposed for the
     night CI), for `scenarioCheck` (all scenarios, Fabric 1.21.1 and
     NeoForge 1.21.1, plus one 26.3 loader) and `upgradeTest` for all four
     combinations. Cost: about 10 to 11 min for scenarios and 5 to 7 min per
     `upgradeTest` combination.
  2. On demand through `workflow_dispatch` with the scenario list and the
     loader as inputs, so a PR author can run a scenario without a Mac.
- Parallelism: each combination is its own job on its own runner, so the limit
  of 2 runs per Mac (`CHRONODAWN_MAX_PARALLEL_VERIFY`) and the 20 GB disk guard
  do not apply.
- Artifacts: do not upload screenshots, JARs, mappings or worlds, as in
  `prod-smoke`. The job log carries `report.txt`, the per-shot automatic
  checks and the log tails. A person who needs to see the images still uses a
  Mac. If screenshots from CI are wanted later, the options are: do not store
  them (current), or store them in a short-retention private location. The
  owner decides, because the images show Minecraft assets.
- Open item before relying on it: decide whether the 26.3 screen-click
  support (an existing limit) should be added so that the full scenario set
  can run on 26.3.
