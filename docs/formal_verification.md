# Formal Verification

Some features of Chrono Dawn have many states and combinations, and a unit test
covers only the cases someone thought of. For those features the project keeps a
small model that is independent of Minecraft and lets a model checker visit
every reachable state. This is a trial. It covers one feature now and grows only
where it pays off.

## Running

```bash
./gradlew verifyFormalModels -Pvalidation_only=true
```

- Each `specs/tla/<Name>.cfg` is checked against `specs/tla/<Name>.tla` by a
  `verifyTla<Name>` task, and `verifyFormalModels` runs them all.
- The task fails when TLC reports a parse error, a violated invariant or
  property, or a deadlock. The counterexample trace is printed in the Gradle
  output.
- CI runs it in the `validate` job after `validateAll`. `checkAll` depends on it.
- It is not part of `validateAll`, so the pre-commit hook does not run it.
- The first run downloads `tla2tools.jar` (2.2 MB) from the TLA+ GitHub release
  into the Gradle cache. Inside a network sandbox, allow `github.com` and
  `release-assets.githubusercontent.com` for that run.
- TLC runs on the JVM that runs Gradle (JDK 21 or 25 here). TLC itself needs
  Java 11 or newer, and the model was also checked on JDK 17.

## Tool Choice

| Tool | Pros | Cons |
| --- | --- | --- |
| TLA+ with TLC | Visits every reachable state, including interleavings. Checks invariants, step properties and liveness ("the player can always get back"). One MIT-licensed JAR, no install. Deterministic with one worker. | A separate language to learn and review. The model is written by hand and can drift from the Java code. Not on Maven Central. |
| Alloy 6 | One JAR. Good at structural constraints, such as relations between registries and tags. Has temporal operators since version 6. | Checks only up to a bound on the number of steps and objects. Liveness and fairness are less direct than in TLA+. Fewer examples for state machines. |
| jqwik (property-based testing) | Runs as JUnit 5 tests on real Java code, so it cannot drift from it. Familiar to Java developers. | Samples random inputs, so it is not exhaustive and a failure may show only on some runs. The portal code imports Minecraft types, so the logic would have to be extracted first. No liveness. EPL-2.0. |
| Hand-written state explorer in JUnit | No new dependency. | Every property and the search itself are custom code, which is what a model checker already provides. |

TLA+ with TLC was chosen. The first target is a state machine whose risky
properties span several steps and several config options, which exhaustive
search covers and random sampling does not. A full search also gives the same
result on every run, so a violation can fail CI without flakes. Drift from the
code is the main cost. Each model lists the classes it follows, and a change to
those classes should update the model in the same commit.

`tla2tools.jar` 1.7.4 (MIT) is pinned with a SHA-256 in
`gradle/formal-verification.gradle`. 1.7.4 is the latest release that is not
marked as a pre-release.

TLA+ Tools are published only as GitHub release assets, not to Maven Central.
Of the alternatives, Alloy (`org.alloytools:org.alloytools.alloy.dist`) and
jqwik are on Maven Central. This did not change the choice, for these reasons:

- Gradle resolves the release asset through an Ivy repository like any other
  dependency, caches it, and the task checks the pinned SHA-256. A replaced
  asset fails the build instead of running unknown code.
- The costs are about 15 lines of build script, a manual version bump (two
  values) because dependency update bots do not track the asset, and one more
  host for the first download. TLC releases are rare (1.7.4 in August 2024, the
  next one is still a pre-release), so the manual bump is cheap.
- If the asset ever disappears, the task fails at once, and the jar can be
  committed under `specs/tla/lib/` instead (MIT allows it, 2.2 MB).
- What decided the choice, exhaustive search with fairness and liveness and a
  deterministic result, does not depend on where the jar comes from.

## Models

### Portal progression (`specs/tla/PortalProgression.tla`)

Follows `PortalIgnitionHandler`, `PortalStabilizationHandler`,
`PortalTeleportHandler`, `PlayerEventHandler`, the portal sweep in
`BlockEventHandler`, `ChronoDawnGlobalState` and `PortalSettings`. The logic is
the same in every supported Minecraft version.

The model has one player and one portal frame per dimension. For each frame it
tracks the registry state (`None` when there is no registry entry) and whether
portal blocks fill it. Both config options, `oneWayUntilStabilized` and
`allowReignitionBeforeStabilization`, are chosen in the initial state, so one
run covers all four combinations. The full search visits 46 distinct states and
finishes in about a second.

Checked properties:

| Property | Meaning |
| --- | --- |
| `StabilizedOnlyAfterEntry` | The dimension cannot be stabilized before the player has entered it. |
| `NoChronoDawnPortalWhileGated` | While the pre-stabilization gate is in force, Chrono Dawn has no portal blocks, even without the tick sweep. The sweep is a safety net for blocks placed by other means. |
| `NoReturnBeforeStabilization` | With `oneWayUntilStabilized` on and `allowReignitionBeforeStabilization` off, the player cannot leave Chrono Dawn before it is stabilized, even by rebuilding or re-igniting the arrival frame. |
| `PortalStabilizedImpliesGlobal` | A stabilized portal always comes with world-wide stabilization. |
| `NeverInactive` | No code path leaves a registry entry `INACTIVE`. |
| `ProgressIsPermanent` | "Entered" and "stabilized" are never lost. |
| `NoForbiddenTransition` | No portal goes from `ACTIVATED` or `INACTIVE` to `STABILIZED`, and no registry entry disappears. |
| `CanAlwaysReturn` | A player in Chrono Dawn who keeps trying (strong fairness on ignition, travel and stabilization) always gets back to the Overworld, with every config combination. |
| Deadlock check (TLC default) | Some action is always possible. Breaking this means the player is stuck. |

To see that the properties can fail, these changes to the model were checked
once by hand (not in CI):

| Change | Result |
| --- | --- |
| The gate stays on after stabilization (`Unstable == entered`) | Deadlock: the player is stuck in Chrono Dawn. |
| Arrival no longer destroys the unstable portal | `NoChronoDawnPortalWhileGated` violated. |
| Ignition in Chrono Dawn ignores the gate | `NoChronoDawnPortalWhileGated` violated, and `NoReturnBeforeStabilization` too when checked alone. |
| The stabilizer also accepts an `ACTIVATED` portal | `StabilizedOnlyAfterEntry` violated. |
| `NoForbiddenTransition` also forbids `DEACTIVATED -> ACTIVATED`, or separately `STABILIZED ->` anything else (both listed under Findings) | Violated in each case, so the code really takes both. |
| Stabilization from an unregistered frame removed | Still passes. The registered arrival frame is enough in this model, which does not model losing that frame. |

### Findings

The model captures these behaviors found when comparing the implementation with
its earlier Javadoc:

- `DEACTIVATED -> ACTIVATED` occurs when travel restores a destination frame
  and sets the existing entry back to `ACTIVATED` with `setState`.
- Igniting an empty frame creates a new entry at that position, even if the old
  entry was stabilized. The position index initially points to the new `ACTIVATED` entry,
  while the old entry remains in the registry and saved data. The world-wide
  stabilization flag does not change.
- Travel leaves the source portal unchanged. A former source deactivation
  lookup used a portal block position instead of the frame's bottom-left
  registry key, so it never found the source entry and was removed. Under
  one-way entry, the unstable arrival portal in Chrono Dawn is deactivated and
  destroyed instead.

The model tracks the entry selected by the position index, not retained older
entries. `Travel` and `Ignite` represent the current behavior.

## Adding a Model

1. Pick behavior with many states or config combinations whose failure is
   expensive to find in game, such as progression gates or persistence.
2. Write `specs/tla/<Name>.tla` without Minecraft types. List the classes it
   follows in the header comment.
3. Write `specs/tla/<Name>.cfg` with `SPECIFICATION`, `INVARIANTS` and
   `PROPERTIES`. Keep the state space small enough that CI finishes in seconds.
4. Break the model on purpose once to check that each property can fail, and
   record the result in this document.
5. Run `./gradlew verifyFormalModels -Pvalidation_only=true` and add the model
   to the list above.
