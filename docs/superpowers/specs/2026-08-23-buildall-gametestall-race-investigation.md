# `buildAll` / `gameTestAll` race investigation

Written 2026-08-23. Investigation only — no fix landed. The fix, if one is
still needed, belongs in the `gradle/shared` submodule
([minecraft-mod-gradle-scripts](https://github.com/ksoichiro/minecraft-mod-gradle-scripts)),
on its own branch, since it benefits every consumer project.

## Symptom

`./gradlew checkAll` occasionally fails at `buildAll`. The per-version log
(`build/build-<version>.log`) shows a pattern like:

```
> Task :common-1.21.9:sourcesJar UP-TO-DATE
> Task :common-1.21.9:remapSourcesJar FAILED
  property 'input' specifies file '.../common/1.21.9/build/devlibs/common-1.21.9-0.8.0-sources.jar' which doesn't exist
```

`sourcesJar` is judged UP-TO-DATE while its declared output jar is absent, so
Loom's `remapSourcesJar` fails on a missing input. Every per-version build
passes when run standalone. Prior to this investigation it had reproduced
twice in one session via `./gradlew cleanAll && ./gradlew buildAll`, both
times failing on 1.21.9.

## What was tested and ruled out

All four hypotheses below were tested empirically (not just reasoned about)
and disproved. `common/1.21.9/build.gradle`'s `clean` task was temporarily
patched with a 15s `Thread.sleep` in `doFirst`, plus `doFirst` timestamp
logging on `compileJava`/`sourcesJar`/`remapSourcesJar`/`jar`, to directly
observe task execution ordering. The patch was reverted after each test
(no trace left in the working tree).

| # | Hypothesis | Test | Result |
|---|---|---|---|
| 1 | `clean`+`build` combined in one invocation races within a single process, even with `--no-parallel` (as `buildAll`'s per-version subprocess uses) | Ran `gradlew clean build -Ptarget_mc_version=1.21.9 -x test --project-cache-dir .gradle/build-1.21.9 --no-parallel` with the sleep probe | **Ruled out.** `compileJava` started 671ms *after* `clean`'s sleep completed — fully serialized. |
| 2 | Same race, but for the standalone `build<version>` task, which does **not** pass `--no-parallel` and so inherits `org.gradle.parallel=true` from `gradle.properties` | Ran `gradlew clean build -Ptarget_mc_version=1.21.9 -x test` (no `--no-parallel`, no isolated cache dir) with the same probe | **Ruled out.** `compileJava` started 348ms after `clean`'s sleep completed — still fully serialized. Gradle 8.14's mutation-tracking (the fix for [gradle/gradle#15163](https://github.com/gradle/gradle/issues/15163), closed at the 7.4 RC1 milestone) correctly prevents `clean` and same-project build tasks from overlapping, parallel or not. |
| 3 | The root project has its own `clean` task, and `buildAll`'s 3 concurrent subprocesses race deleting/writing the shared root `build/` directory | `./gradlew clean build -Ptarget_mc_version=1.21.9 -x test --dry-run` and inspected which `clean` tasks actually run | **Ruled out.** The root project has no plugin-provided `clean` task at all (no `base`/`java` plugin applied at root). Only `:common-<version>:clean`, `:fabric:clean`, `:neoforge:clean` run — no root-level clean. |
| 4 | Two versions built concurrently by `buildAll` share a physical output directory | Read `settings.gradle` | **Ruled out.** `project(":${commonProject}").projectDir = file("common/${moduleVersion}")` (same pattern for `fabric`/`neoforge`) — every version gets its own directory, no sharing. `--project-cache-dir` further isolates each subprocess's Gradle-internal execution history. |

## Reproduction attempts

Four full end-to-end attempts, all on the same machine (10 CPUs), none
reproduced the failure:

1. `common-1.21.9` alone, `clean` + `remapSourcesJar` only — success (baseline, not expected to race).
2. 3-version concurrent `clean`+`remapSourcesJar` (`1.21.7`/`1.21.8`/`1.21.9`, matching a `buildAll` batch) × 3 repeats — all succeeded.
3. Full `./gradlew cleanAll && ./gradlew buildAll` (all 11 versions, exact reported repro command) × 2 — both succeeded in ~3 minutes.
4. Same as #3 but with `./gradlew --stop` (kill all daemons) and `.gradle/build-*` cache dirs removed first, to approximate a colder cache — succeeded in 2m39s (no slower — global Loom/mapping caches under `GRADLE_USER_HOME` stayed warm regardless).

The original two failures happened right after a large code change (structure
toggles, Time Compass consolidation, ~1200 lines deleted) that likely forced
much longer full-recompile times than the warm-cache ~3 minute runs here,
widening whatever race window exists. That specific condition — long-running,
resource-heavy concurrent builds under real development-session system load —
was not reproduced here and would be expensive/disruptive to fake convincingly
(large synthetic source changes, deliberate system load).

## What's left unexplained

With all four code-level hypotheses disproved and direct reproduction
unsuccessful, the remaining plausible causes are outside what static analysis
or lightweight experiments in this repo can pin down:

- Contention in `GRADLE_USER_HOME`-shared caches that `--project-cache-dir`
  does **not** isolate (dependency/module cache, and particularly Loom's own
  Minecraft mapping/decompilation caches) under concurrent access from 3
  simultaneous daemons.
- OS-level resource contention (memory pressure, GC pauses) during genuinely
  long, heavy concurrent builds causing a daemon to stall long enough for its
  internal state (e.g. file-system-watching) to miss an update — a
  reliability issue rather than a true concurrency race in Gradle's own task
  graph.

Confirming either would require instrumenting Loom's internals or reproducing
under real heavy load, which is out of scope for a quick investigation.

## Recommendation

No code-level smoking gun was found in this codebase's `buildAll`/`checkAll`
wiring — the existing `--no-parallel` safeguard already prevents the intra-
process race class that seemed most likely going in. Given:

- The failure is empirically rare (didn't reproduce in 4/4 attempts here),
- Gradle's own official guidance is still to avoid combining `clean` with
  other tasks in one invocation regardless of whether a specific bug is
  currently triggering it, and
- Any fix belongs in `gradle/shared` (a separate repo/branch) since it isn't
  ChronoDawn-specific,

the lowest-risk path if this bites again is to split `buildAll`'s and the
standalone `build<version>` task's `clean build` into two sequential
`ProcessBuilder`/`runGradle` invocations there, rather than chasing the exact
mechanism further. That change was **not** made in this session — this
document only records the investigation.

## Reference

- [gradle/gradle#15163](https://github.com/gradle/gradle/issues/15163) — the
  general class of `clean`+`build`+`org.gradle.parallel` race this
  investigation started from; closed/fixed at the 7.4 RC1 milestone (this
  project uses Gradle 8.14, so it isn't directly responsible here, but its
  existence is why the intra-process hypotheses were tested first).
- `feedback_buildall_gametestall_wrapper_unreliable`,
  `feedback_concurrent_gradle_same_repo_collision`,
  `project_gradle_shared_local_submodule` — related memory entries from
  earlier sessions.
