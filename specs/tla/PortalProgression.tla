--------------------------- MODULE PortalProgression ---------------------------
(***************************************************************************)
(* Model of the Chrono Dawn portal progression, checked with TLC.          *)
(*                                                                         *)
(* It describes what the code does, not what the Javadoc of                *)
(* PortalStateMachine says. The model has no Minecraft types. It follows   *)
(* these classes, which share the same logic in every Minecraft version:   *)
(*                                                                         *)
(*   PortalIgnitionHandler.tryIgnite         -> Ignite                     *)
(*   PortalStabilizationHandler              -> StabilizeRegistered,       *)
(*                                              StabilizeFromFrame         *)
(*   PortalTeleportHandler.teleportThroughPortal                           *)
(*   + PlayerEventHandler (dimension change) -> Travel                     *)
(*   ChronoDawnPortalBlock.randomTick        -> BreakPortal                *)
(*   BlockEventHandler server tick sweep     -> Sweep                      *)
(*   ChronoDawnGlobalState                   -> entered, stabilized        *)
(*   PortalSettings                          -> oneWay, allowReignition    *)
(*                                                                         *)
(* Abstractions:                                                           *)
(* - One player, one portal frame per dimension. Travel maps the frame in  *)
(*   one dimension to the frame in the other, as the 1:1 coordinate        *)
(*   mapping does.                                                         *)
(* - state[d] is the registry entry at that frame position. "None" means   *)
(*   no entry. blocks[d] is whether portal blocks fill the frame.          *)
(* - Entities other than players, the arrival re-teleport guard, the      *)
(*   Time Tyrant flag and the legacy (0.8.0) registry migration are left  *)
(*   out.                                                                  *)
(* - The two config options are chosen in Init and never change, so one    *)
(*   TLC run covers all four combinations.                                 *)
(*                                                                         *)
(* Run: ./gradlew verifyFormalModels (docs/formal_verification.md).        *)
(***************************************************************************)

OW == "Overworld"
CD == "ChronoDawn"
Dims == {OW, CD}
Other(d) == IF d = OW THEN CD ELSE OW

PortalStates == {"None", "Inactive", "Activated", "Deactivated", "Stabilized"}

VARIABLES
    oneWay,           \* portals.oneWayUntilStabilized
    allowReignition,  \* portals.allowReignitionBeforeStabilization
    entered,          \* ChronoDawnGlobalState.hasEnteredChronoDawn
    stabilized,       \* ChronoDawnGlobalState.isPortalStabilized
    state,            \* registry state of the frame in each dimension
    blocks,           \* portal blocks present in the frame of each dimension
    playerDim         \* dimension the player is in

vars == <<oneWay, allowReignition, entered, stabilized, state, blocks, playerDim>>

\* ChronoDawnGlobalState.arePortalsUnstable
Unstable == entered /\ ~stabilized

\* PortalSettings.enforcesInstabilityGate
GateEnforced == Unstable /\ oneWay /\ ~allowReignition

TypeOK ==
    /\ oneWay \in BOOLEAN
    /\ allowReignition \in BOOLEAN
    /\ entered \in BOOLEAN
    /\ stabilized \in BOOLEAN
    /\ state \in [Dims -> PortalStates]
    /\ blocks \in [Dims -> BOOLEAN]
    /\ playerDim \in Dims

Init ==
    /\ oneWay \in BOOLEAN
    /\ allowReignition \in BOOLEAN
    /\ entered = FALSE
    /\ stabilized = FALSE
    /\ state = [d \in Dims |-> "None"]
    /\ blocks = [d \in Dims |-> FALSE]
    /\ playerDim = OW

-----------------------------------------------------------------------------
(* Actions *)

\* Time Hourglass on an empty, complete frame. tryIgnite always creates a new
\* PortalStateMachine and registers it, which replaces the registry entry at
\* the same position whatever its state was.
Ignite ==
    LET d == playerDim IN
    /\ ~blocks[d]
    /\ ~(d = CD /\ GateEnforced)
    /\ state' = [state EXCEPT ![d] = "Activated"]
    /\ blocks' = [blocks EXCEPT ![d] = TRUE]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, playerDim>>

\* The player breaks the frame. randomTick then removes the portal blocks.
\* Nothing unregisters the portal, so its registry state stays.
BreakPortal ==
    /\ blocks[playerDim]
    /\ blocks' = [blocks EXCEPT ![playerDim] = FALSE]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, state, playerDim>>

\* While the gate is in force, a server tick removes portal blocks around
\* every portal registered in Chrono Dawn.
Sweep ==
    /\ GateEnforced
    /\ blocks[CD]
    /\ blocks' = [blocks EXCEPT ![CD] = FALSE]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, state, playerDim>>

\* Registry state after generatePortalStructure restores or creates the frame
\* at the destination: a new or inactive entry is activated, and a deactivated
\* one is set back to Activated with setState.
Restored(s) == IF s \in {"None", "Inactive", "Deactivated"} THEN "Activated" ELSE s

\* The player walks through the portal blocks of the frame they stand at.
\* 1. Destination: reuse its portal blocks, or restore the frame.
\* 2. Source: nothing changes. The code means to deactivate an Activated
\*    source under one-way entry, but it looks the source up by the portal
\*    block position, while the registry is keyed by the frame's bottom-left
\*    corner, so the lookup finds nothing (docs/formal_verification.md).
\* 3. Arrival in Chrono Dawn marks it entered. While portals are unstable
\*    under one-way entry, the arrival portal is deactivated and destroyed
\*    (PortalArrivalHelper, destroyUnstablePortal). PlayerEventHandler then
\*    finds it already deactivated and changes nothing.
Travel ==
    LET from == playerDim
        to == Other(from)
        destState == IF blocks[to] THEN state[to] ELSE Restored(state[to])
        enteredNext == entered \/ to = CD
        destroy == to = CD /\ oneWay /\ enteredNext /\ ~stabilized
    IN
    /\ blocks[from]
    /\ state' = [d \in Dims |->
                    IF d = from THEN state[from]
                    ELSE IF destroy /\ destState = "Activated" THEN "Deactivated"
                    ELSE destState]
    /\ blocks' = [d \in Dims |-> IF d = from THEN blocks[from] ELSE ~destroy]
    /\ entered' = enteredNext
    /\ playerDim' = to
    /\ UNCHANGED <<oneWay, allowReignition, stabilized>>

\* Portal Stabilizer on the frame of a registered, deactivated portal.
StabilizeRegistered ==
    LET d == playerDim IN
    /\ state[d] = "Deactivated"
    /\ state' = [state EXCEPT ![d] = "Stabilized"]
    /\ stabilized' = TRUE
    /\ UNCHANGED <<oneWay, allowReignition, entered, blocks, playerDim>>

\* Portal Stabilizer on a complete frame in Chrono Dawn that has no registry
\* entry nearby, such as a new frame the player built
\* (stabilizeFromUnregisteredFrame). No portal state changes.
StabilizeFromFrame ==
    /\ playerDim = CD
    /\ ~stabilized
    /\ stabilized' = TRUE
    /\ UNCHANGED <<oneWay, allowReignition, entered, state, blocks, playerDim>>

Next ==
    \/ Ignite
    \/ BreakPortal
    \/ Sweep
    \/ Travel
    \/ StabilizeRegistered
    \/ StabilizeFromFrame

Spec == Init /\ [][Next]_vars

\* A player who keeps trying to leave: each way out is taken if it is enabled
\* again and again. Breaking frames and the sweep get no fairness.
FairSpec ==
    /\ Spec
    /\ SF_vars(Ignite)
    /\ SF_vars(Travel)
    /\ SF_vars(StabilizeRegistered)
    /\ SF_vars(StabilizeFromFrame)

-----------------------------------------------------------------------------
(* Invariants *)

\* Stabilization needs a deactivated portal, and only entering Chrono Dawn
\* deactivates one, or the player is in Chrono Dawn already.
StabilizedOnlyAfterEntry == stabilized => entered

\* While the gate is in force, Chrono Dawn never has portal blocks, even
\* without the sweep. The arrival destroys them, and ignition there is
\* refused. The sweep is a safety net for blocks placed by other means.
NoChronoDawnPortalWhileGated == GateEnforced => ~blocks[CD]

\* A portal that the player has stabilized implies global stabilization.
PortalStabilizedImpliesGlobal ==
    \A d \in Dims : state[d] = "Stabilized" => stabilized

\* No code path leaves a portal Inactive: tryIgnite and restoration call
\* activate() right after creating the state machine.
NeverInactive == \A d \in Dims : state[d] # "Inactive"

-----------------------------------------------------------------------------
(* Properties over steps and behaviors *)

\* Global progress is never lost.
ProgressIsPermanent ==
    [][/\ entered => entered'
       /\ stabilized => stabilized']_vars

\* Transitions of the registry state that no step takes. The Javadoc of
\* PortalStateMachine lists more forbidden transitions, but restoration takes
\* Deactivated -> Activated, and re-igniting a broken stabilized frame takes
\* Stabilized -> Activated. docs/formal_verification.md lists both.
NoForbiddenTransition ==
    [][\A d \in Dims :
         /\ ~(state[d] = "Activated" /\ state'[d] = "Stabilized")
         /\ ~(state[d] = "Inactive" /\ state'[d] = "Stabilized")
         /\ ~(state[d] # "None" /\ state'[d] = "None")]_vars

\* A player in Chrono Dawn who keeps trying can always get back to the
\* Overworld, with every combination of the two config options.
CanAlwaysReturn == (playerDim = CD) ~> (playerDim = OW)

=============================================================================
