# Floating arrival portals on Minecraft 1.21.1

Investigation date: 2026-10-24. Source version: 0.14.0.

The reported symptom is an arrival portal floating in the Chrono Dawn dimension,
with inconsistent results even when using the same seed. This investigation
identifies failure paths confirmed on a real NeoForge development server. It
does not establish which path caused the original reported occurrence. On
2026-10-25, the user confirmed that the manual comparison reproduced the expected
pre-fix floating portal and post-fix ground placement. World A required Portal
Stabilizer use before ignition.

Reported reproduction context:

- Minecraft 1.21.1, NeoForge.
- Seed: `1991166212585861250`.
- Overworld coordinates: `(0, 79, 32)`.
- The occurrence was on 2026-10-24 using a build from `main`. The exact commit
  is unknown. World-switching history is not remembered.
- Portal axis and arrival coordinates remain unknown. Whether the Overworld
  coordinates identify the frame corner or the player's position is also
  unconfirmed.

The locally available `main` tip at investigation time is `f7100142a` dated
2026-10-24. Its 1.21.1 teleport handler, shared placement helper, and shared
persistence manager matched the worktree before the fix described below. The Y=120 fallback was
removed on 2026-09-25 in `4ac059a82`, so that older path is unlikely to explain
a build from today's main. This does not identify the exact installed build.

## Confirmed pre-fix path: registry state survives world changes

`PortalRegistry` is a static singleton. `PortalPersistenceManager.initialize()`
registers a level-load callback, but no server-stop cleanup. The callback obtains
`PortalRegistryData` and attaches it to the singleton without first clearing the
previous world's entries.

Loading an existing registry file calls `loadFromNBT()`, which clears the
singleton. Creating data for a world without a registry file instead calls
`PortalRegistryData::new`. Its constructor does not clear the singleton. This
allows entries from a previous integrated-server session to enter a new world
within the same Minecraft process.

The 1.21.1 `PortalTeleportHandler.findReusablePortalFrame()` searches those
entries within 128 horizontal blocks. `canRestorePortalFrame()` accepts air for
every required frame block and every interior block. It requires neither a
surviving Clockstone block nor terrain support. A stale entry above the new
world's terrain can therefore cause a portal to be generated in midair, bypassing
`PortalPlacementHelper` entirely. Restored frames also omit optional corners.

This path depends on the previously opened world, not just the current seed.
Registry UUID iteration order can also affect selection when horizontal
distances tie. If the contaminated registry is saved, restarting Minecraft does
not remove the entries already stored in that world.

Relevant sources:

- `common/shared/src/main/java/com/chronodawn/core/portal/PortalPersistenceManager.java`
- `common/shared/src/main/java/com/chronodawn/core/portal/PortalRegistry.java`
- `common/shared/src/main/java/com/chronodawn/data/PortalRegistryData.java`
- `common/1.21.1/src/main/java/com/chronodawn/compat/CompatSavedData.java`
- `common/1.21.1/src/main/java/com/chronodawn/core/portal/PortalTeleportHandler.java`

## Secondary candidate: vegetation is accepted as terrain

`PortalPlacementHelper.validateFramePosition()` reads `WORLD_SURFACE` for four
columns. It rejects air, but does not require solid supporting ground or exclude
leaves. A level canopy can satisfy the placement checks and place a portal above
the actual terrain. Leaf loss can later expose the gap.

Replaceable vegetation only lowers the frame by one block. For a two-block-tall
plant, replacing its upper block can leave an unsupported frame when the lower
block is removed. These checks also do not validate an adjacent exit floor.

This is a separate placement weakness. It does not by itself explain differing
results with identical world state, entry coordinates, and portal axis.

Before commit `4ac059a82`, the 1.21.1 handler also had an unconditional Y=120
fallback. That fallback is absent from the investigated source. The installed
mod version is needed to determine whether the older path is relevant.

## Pre-fix runtime results

NeoForge 21.1.209 with Architectury 13.0.8 and the investigated 0.14.0 source was
started in disposable dedicated-server worlds using the reported seed. These
were normal servers with the Chrono Dawn dimension loaded, not GameTests.

- After calling the actual destination portal lookup to load surrounding chunks,
  terrain selection from `(0, 79, 32)` chose frame position `(-1, 80, 33)` for
  both X and Z axes. All four supporting blocks were Temporal Grass Blocks with
  Temporal Dirt underneath. This fresh-world control did not select midair.
- A registered deactivated frame at `(0, 220, 32)` survived construction of a fresh
  `PortalRegistryData`. Calling the actual frame-reuse method created its
  Clockstone frame at that height with air immediately below it.
- A registry entry was still present when `ServerStoppedEvent` was delivered.
- A synthetic flat oak-leaf canopy at Y=240 was accepted as support and selected
  a frame at Y=241. This confirms the separate vegetation weakness, not its
  involvement in the reported occurrence.

The stale entry was deliberately inserted to reproduce the failure path. The
test does not switch two integrated-server worlds or teleport a player. The
source inspection connects the fresh-data constructor and retained registry to
that world-switching scenario. The user's exact occurrence remains unconfirmed.

Calling the placement helper before loading destination chunks initially returned
`null`. That is not equivalent to the real transfer flow, which calls
`findNearbyPortal()` first. The corrected diagnostic follows that order.

The original output is preserved in
`scripts/diagnostics/portal-floating-1.21.1/result.txt`. The diagnostic now checks
the fixed lifecycle behavior, including real disk persistence. It deliberately
inserts stale entries before the actual startup event, checks their removal,
generates a supported portal, and checks cleanup after the final save.
Run it from the repository root:

```bash
portal_probe_dir=$(mktemp -d "$TMPDIR/chronodawn-portal-probe.XXXXXX")
./gradlew :neoforge:runServer -Ptarget_mc_version=1.21.1 \
  --init-script scripts/diagnostics/portal-floating-1.21.1/probe.gradle \
  -Pportal_probe_dir="$portal_probe_dir" --console=plain
./gradlew :neoforge:runServer -Ptarget_mc_version=1.21.1 \
  --init-script scripts/diagnostics/portal-floating-1.21.1/probe.gradle \
  -Pportal_probe_dir="$portal_probe_dir" -Pportal_probe_resume=true --console=plain
```

Success means both commands exit with status 0 and all `PORTAL_PROBE` results
are `PASS`. The second run must report that the saved portal UUID and position
were loaded from disk, with no extra stale entries. Post-fix results are stored
in `fixed-fresh-result.txt` and `fixed-reload-result.txt` beside the diagnostic.

The init script adds diagnostic sources only for that invocation. Its shutdown
handler waits for the server thread to finish, then exits the disposable JVM to
avoid development-launcher threads keeping the process alive. Normal builds do not
include the diagnostic. Recompile without the init script afterward to remove
the extra compiled class:

```bash
./gradlew :neoforge:compileJava -Ptarget_mc_version=1.21.1
```

## Manual integrated-client comparison

The user ran this comparison on 2026-10-25 and confirmed the expected behavior
on both builds, with one correction: world A required stabilization before
ignition. The procedure below includes that correction. The automated NeoForge
lifecycle and disk-persistence checks above also passed. Use Minecraft 1.21.1, NeoForge, and the same config and mod
set for both builds.

Start Minecraft fresh for each build. Create two new creative worlds, A and B,
with cheats enabled and seed `1991166212585861250`. Do not reuse B from the
pre-fix test for the post-fix test, since stale entries can already be saved.

In A, enable creative flight before teleporting. Run:

```mcfunction
/gamemode creative
/execute in chronodawn:chronodawn run tp @s 1.5 226 35.5
/fill 0 220 32 3 224 32 chronodawn:clockstone_block
/fill 1 221 32 2 223 32 minecraft:air
/tp @s 1.5 225 32.5
/give @s chronodawn:portal_stabilizer
/give @s chronodawn:time_hourglass
```

Looking down from above the frame, first use the Portal Stabilizer on Clockstone
to stabilize the portal, then ignite it using the Time Hourglass. The user needed
this stabilization step in the actual client comparison. Confirm the portal
interior lights up. Do not enter it. Save and quit to the title screen without
exiting Minecraft. A raw `setblock` of portal blocks would not register the
portal and cannot replace the manual item interactions.

Create fresh world B within that same Minecraft process, with the same seed.
Enable creative flight and run:

```mcfunction
/gamemode creative
/tp @s 1.5 85 35.5
/fill 0 79 32 3 83 32 chronodawn:clockstone_block
/fill 1 80 32 2 82 32 minecraft:air
/tp @s 1.5 84 32.5
/give @s chronodawn:time_hourglass
```

Right-click the frame using the Time Hourglass and confirm ignition. Then move
into its interior, or run:

```mcfunction
/tp @s 1.5 80 32.5
```

Wait for the portal to transfer the player. The Overworld frame has bottom-left
`(0, 79, 32)` and X axis in both builds. Expected comparison:

| Build | Arrival frame |
| --- | --- |
| Before fix | Restored at `(0, 220, 32)` above terrain from A's stale registry entry |
| After fix | Newly generated on terrain. The automated default-config run selected `(-1, 80, 33)` |

With default one-way progression, the arrival portal interior disappears after
entry. Inspect the remaining Clockstone frame and F3 coordinates. The player's
feet are approximately one block above the frame base, around Y=221 before the
fix and Y=81 after it in the default-config world.

Exit Minecraft completely before switching builds, and repeat with new A and B
worlds. If the pre-fix run does not float, record ignition success, both world
names, seed, frame coordinates, and the actual arrival frame coordinates. Do not
infer a fix from the post-fix control alone if the pre-fix trigger did not occur.

This checks the known registry-isolation defect. It does not establish that the
user's original occurrence used that exact trigger. Vegetation placement remains
a separate issue.

## Implemented fix and verification scope

`PortalPersistenceManager` now clears registry entries and detaches the old
SavedData reference on `SERVER_BEFORE_START` and `SERVER_STOPPED`. It does not
clear on `SERVER_STOPPING`, because the final save still serializes the registry.
The normal Overworld load callback still loads existing saved portals and
attaches the new world's SavedData.

`PortalPersistenceManagerTest` covers startup isolation, shutdown cleanup,
SavedData detachment, final-save preservation, and the subsequent saved-data
round trip. All three new tests failed before the implementation change and
passed afterward alongside the existing registry tests.

The NeoForge runtime probe passed for both a fresh world and a restart of that
world. It verified startup isolation, absence of the stale midair frame,
supported portal generation at `(-1, 80, 33)`, retention until final save,
shutdown cleanup, and restoration of the same UUID and coordinates from disk.

The fix prevents in-memory cross-world contamination. It does not delete stale
entries already persisted to a world's registry file, relocate existing floating
frames, or change intentionally recoverable damaged-frame behavior.

For terrain selection, validate durable support while preserving the documented
water-surface placement behavior. Changing heightmap type alone does not cover
restoration or tall vegetation.

The production change is limited to the shared persistence manager. Investigation used `rg`, `git log`, `git show`,
source inspection, and the isolated NeoForge `runServer` diagnostic. The first
init-script attempt failed because it also ran inside the included build. A
project-lookup guard fixed the diagnostic setup. Intermediate diagnostic servers
saved successfully but needed lingering JVMs terminated. Calling `System.exit`
on the server thread also blocked shutdown, so the final diagnostic waits for
that thread from a separate thread before exiting. The final saved diagnostic
completed all checks and `runServer` exited with status 0. The normal
`compileJava` command also succeeded afterward, and no diagnostic class remained
in the build output.

The final normal verification command is:

```bash
./gradlew :common-1.21.1:test :fabric:build :neoforge:build -Ptarget_mc_version=1.21.1
```

This command passed: 4,256 common tests had no failures, errors, or skips, and
both loader JARs built successfully. Fabric's build also ran 2,505 required
GameTests successfully. Its test world omitted the Chrono Dawn dimension, so
dimension-dependent tests that return early do not count as runtime portal
verification. Both production JARs were checked to exclude diagnostic classes.

Fabric runtime probes and production-JAR smoke tests were not run. No Mixin
changed. The user subsequently completed the NeoForge manual client comparison
above and confirmed the expected pre-fix and post-fix behavior. This confirms
the controlled registry-isolation case, while the original occurrence's history
remains unknown.
