# Unit Test Coverage Baseline (Minecraft 1.21.1)

This is a measurement only. No threshold is set and nothing is wired into CI.
Use the numbers to choose where unit tests are worth adding.

## How to measure

```bash
./gradlew :common-1.21.1:test :common-1.21.1:jacocoTestReport -Ptarget_mc_version=1.21.1
```

Reports are written to `common/1.21.1/build/reports/jacoco/test/`
(`html/index.html`, `jacocoTestReport.xml`, `jacocoTestReport.csv`).

The JaCoCo agent is attached to the `test` task only when a requested task name
contains `jacoco` (`common/1.21.1/build.gradle`). Plain `test`, `validateAll`,
`checkAll` and CI runs stay uninstrumented, so their duration does not change.
Name the `jacoco` task on the command line: a task pulled in only through a
dependency does not enable the agent.

## Baseline (2026-11-26, 62 shared test files plus `ResourceValidationTest`)

Overall: 8.7% of lines (1,992 of 22,899) and 5.7% of branches. The `gametest`
package is compiled into the main source set but only runs under GameTest, so
excluding it gives about 9.6% of lines. Most of the covered code is `config`,
`data`, `core.wayside`, `core.portal` and `worldgen.runtime`.

Most existing tests read JSON or source files (Recipe, LootTable, Tag and
Worldgen validation). They protect data but add almost nothing to Java coverage.

| Package | Lines | Line % | Branch % |
|---|---:|---:|---:|
| `worldgen` (all subpackages) | 3,180 | 4.0 | 2.9 |
| `gametest` | 3,067 | 2.8 | 0.0 |
| `registry` | 2,947 | 20.1 | 0.0 |
| `entities` | 2,889 | 1.3 | 0.2 |
| `blocks` | 2,703 | 0.4 | 1.1 |
| `client` | 2,464 | 0.8 | 0.0 |
| `items` | 1,316 | 1.8 | 2.0 |
| `core` | 1,064 | 31.6 | 26.4 |
| `gui` | 692 | 0.0 | 0.0 |
| `mixin` | 601 | 0.0 | 0.0 |
| `events` | 557 | 0.9 | 2.5 |
| `data` | 549 | 38.3 | 52.5 |
| `config` | 504 | 96.0 | 85.4 |
| `compat` | 129 | 1.6 | 0.0 |
| `api` | 45 | 68.9 | 33.3 |
| `version` | 27 | 92.6 | 70.0 |

Smaller packages (`fluids`, `network`, `anvil`, `tags`, `effects`, `platform`,
`integration`) total under 150 lines and are at or near 0%.

Selected subpackages:

| Subpackage | Lines | Line % |
|---|---:|---:|
| `worldgen.spawning` | 2,461 | 0.0 |
| `client.model` | 1,825 | 0.0 |
| `entities.bosses` | 1,503 | 2.5 |
| `entities.mobs` | 1,074 | 0.0 |
| `core.portal` | 592 | 25.2 |
| `mixin` (root) | 535 | 0.0 |
| `gui.widgets` | 428 | 0.0 |
| `worldgen.processors` | 314 | 0.0 |
| `core.wayside` | 168 | 92.9 |
| `core.time` | 127 | 24.4 |
| `core.teleport` | 116 | 0.0 |
| `worldgen.runtime` | 102 | 90.2 |

## Room for unit tests, by package

"Pure logic" means code that runs without a `Level`, the registries or a
renderer. The split is a judgment from class names and the uncovered-class list,
not a full code review.

### Worth testing (pure logic or data)

| Package | Uncovered candidates | Notes |
|---|---|---|
| `data` (38%) | `PlayerProgressData`, `DimensionStateData`, `ChronoDawnGlobalState`, `TimeKeeperVillageData`, `ChronoDawnTimeData` (0%), `BossSpawnData` (113 lines missed) | Saved-data codecs and state transitions. Past persistence bugs came from here (`docs/bug_prevention.md`). Check whether the codecs need a bootstrapped registry first |
| `core.portal` (25%) | `PortalFrameValidator` (86 lines, 0%) | Frame shape checks. The other portal classes need a `Level` |
| `core.time` (24%) | `ReversedResonance`, `TimeDistortionEffect` | Time and band arithmetic may be separable from entity effects |
| `core.teleport`, `core.dimension` | `TeleporterChargingHandler`, `DimensionStabilizer` | Only if the charge and cooldown math can be pulled out |
| `worldgen.processors`, `worldgen.features` | `BossRoomProtectionProcessor` (271 lines) | Region and offset math. Block-state handling needs bootstrap |
| `compat` | `ItemDataHandler121`, `ItemDurabilityHandler` | Small, era-specific helpers |
| `items.artifacts` | `TimeTyrantArmorAbilityHandler`, `ChronobladeAISkipHandler` | Ability conditions and cooldowns, if they can be reached without a player |

### Poor fit for unit tests

| Package | Reason |
|---|---|
| `client`, `gui`, `client.model`, `client.renderer` | Rendering, model parts, widgets. Use scenario checks (`docs/scenario_check.md`) |
| `registry` | Registration calls. Covered indirectly by `prodSmokeTest` and GameTest |
| `mixin` | Needs the mixin environment. Use `prodSmokeTest` |
| `blocks`, `entities`, `entities.ai`, `items` (most) | Depend on `Level`, `Player` and registries. GameTest fits better |
| `worldgen.spawning` | Placers and spawners for boss rooms and villages (2,461 lines) work on a `Level`. The large uncovered classes, such as `MasterClockBossRoomPlacer` (845 lines) and `PhantomCatacombsBossRoomPlacer` (817 lines), are structure and GameTest territory |
| `events` | Event handlers over live game objects |
| `gametest` | Only runs under GameTest, so unit-test coverage is meaningless. Exclude it from any future figure |

## Constraints for adding tests

- `common/1.21.1` has JUnit 5 only. There is no Mockito, and tests that need
  registries would have to call `Bootstrap.bootStrap()`. Avoid that unless the
  logic cannot be separated from the registries.
- Test sources are shared from `common/shared/src/test`, so a new test must also
  compile for the other versions that include that directory.

## Suggested next step

Start with `data` and `core.portal` (`PortalFrameValidator`), then `core.time`.
These have the highest share of testable logic. Register that work as a separate
task once the owner decides whether a threshold is wanted at all.
