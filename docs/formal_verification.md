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
once by hand (not in CI). Individual property runs select that property alone.
For the liveness mutation, deadlock checking is disabled in the temporary
mutant configuration so TLC reports the temporal violation. The normal
configuration retains deadlock checking:

| Change | Result |
| --- | --- |
| The gate stays on after stabilization (`Unstable == entered`) | Deadlock: the player is stuck in Chrono Dawn. |
| Invalid initial player dimension | `TypeOK` violated. |
| Initial state is stabilized before entry | `StabilizedOnlyAfterEntry` violated. |
| Registered stabilization does not set the global flag | `PortalStabilizedImpliesGlobal` violated. |
| An initial registry entry is `INACTIVE` | `NeverInactive` violated. |
| Entry toggles the entered flag | `ProgressIsPermanent` violated. |
| Registered stabilization accepts an `ACTIVATED` portal | `NoForbiddenTransition` violated. |
| Remove both stabilization paths and their fairness clauses | `CanAlwaysReturn` violated. |
| Arrival no longer destroys the unstable portal | `NoChronoDawnPortalWhileGated` violated. |
| Ignition in Chrono Dawn ignores the gate | `NoChronoDawnPortalWhileGated` violated, and `NoReturnBeforeStabilization` too when checked alone. |
| The stabilizer also accepts an `ACTIVATED` portal | `StabilizedOnlyAfterEntry` violated. |
| `NoForbiddenTransition` also forbids `DEACTIVATED -> ACTIVATED`, or separately `STABILIZED ->` anything else (both listed under Findings) | Violated in each case, so the code really takes both. |
| Stabilization from an unregistered frame removed | Still passes in this single-player model. The multiplayer model below covers losing the registered frame. |

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

### Multiplayer portal return (`specs/tla/PortalMultiplayer.tla`)

This complementary model keeps the original portal-state checks above and
explores two players sharing the global flags and the arrival portal. A second,
complete frame is far enough from the arrival frame to be unregistered. Either
player can lose the arrival frame, and a later arrival can destroy its portal
blocks while the other player is in Chrono Dawn. Players can carry a spare
hourglass on entry. Both settings are varied, and the check visits 1,616
reachable states in under a second of TLC time on the development machine.

Each player makes at most one trip. The Overworld portal and construction of
complete, suitably separated frames are assumed available. A frame remains
available after it is built, except for the arrival portal destroyed by another
entry. Deliberate repeated destruction, combat, terrain, inventories beyond the
next useful item, and resource depletion are outside this model. It is not a
proof that arbitrary players or modpacks can always obtain these items.

`Acquire` represents exploration, gathering and crafting as one action. A
player in Chrono Dawn obtains a stabilizer when the gate is enforced and an
hourglass otherwise. It may replace an item no longer needed after another
player stabilizes the world. Stabilizing or igniting consumes that item. The
following conditions are assumed, not proven by the model:

- Clockstone is available in Ancient Ruins in the Overworld and underground in
  Chrono Dawn. A Clockstone Block requires nine Clockstone. Fruit of Time and
  Temporal Sand are available in Chrono Dawn. Temporal Sand is in the vanilla
  sand tag and can be smelted into glass when a furnace and fuel are available.
- The Time Hourglass recipe needs four glass, two Clockstone and one Temporal
  Sand. Its blueprint is in Overworld Ancient Ruins. A player who did not
  unlock or bring it can instead obtain hourglasses from Hourglass Golems in
  Chrono Dawn, provided those mobs can spawn and be defeated.
- The Portal Stabilizer blueprint is in Forgotten Library chests in Chrono
  Dawn. Its recipe needs four Clockstone, one Clockstone Block, two Fruit of
  Time and two Time Hourglasses. A further hourglass is needed to ignite the
  frame after stabilization. The model assumes sufficient repeated collection.
- In Peaceful, Hourglass Golems cannot spawn. A player without the hourglass
  recipe or spare hourglasses may lack this supply when crafting undiscovered
  recipes is restricted. This case does not satisfy the acquisition assumption.
  Emergency respawn remains a separate escape route.

`CanAlwaysReturn` checks each player independently under weak fairness for
acquisition, stabilization, ignition and portal travel. The assumptions mean
that an action which stays enabled is eventually taken. Emergency respawn is
also a possible transition, representing removal of a respawn point and death,
though it is not required by the fairness assumptions. Therefore the property
also checks a behavior where the player never chooses emergency respawn. The
other player can enter or leave at any point, subject to the one-trip bound.
This includes the shared arrival portal being destroyed during the first
player's trip. It does not cover an adversary repeatedly breaking frames or
withholding resources.

The following mutations were checked individually with TLC. Each run selected
only the property in the result column, so another failure cannot mask it.
For the removed transition, its fairness clause was removed as well. Otherwise
fairness could exclude the very behavior the mutation is meant to test.

| Mutation | Result |
| --- | --- |
| Acquisition produces an invalid item value | `TypeOK` violated. |
| Initial state is stabilized before entry | `StabilizedOnlyAfterEntry` violated. |
| Ignition ignores the gate | `NoPortalWhileGated` violated with a carried hourglass. |
| Emergency respawn clears stabilization | `ProgressIsPermanent` violated. |
| Remove unregistered-frame stabilization and its fairness clause | `CanAlwaysReturn` violated after the arrival frame is forgotten. Emergency respawn remains possible but is not forced. |

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
