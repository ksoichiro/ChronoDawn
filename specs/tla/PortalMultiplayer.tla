--------------------------- MODULE PortalMultiplayer ---------------------------
CONSTANTS Players, Arrival, NewFrame
Frames == {Arrival, NewFrame}
OW == "Overworld"
CD == "ChronoDawn"
Returned == "Returned"

\* One excursion per player. The arrival frame is shared and can be forgotten;
\* the new complete frame is far enough away to be unregistered. A later
\* arrival can destroy the shared arrival portal while another player is in CD.
\* Acquisition abstracts gathering, recipes and crafting as documented in
\* formal_verification.md. Each item is consumed by its corresponding action.
VARIABLES oneWay, allowReignition, entered, stabilized, location, knowsArrival,
          frameState, item
vars == <<oneWay, allowReignition, entered, stabilized, location, knowsArrival,
          frameState, item>>
Gate == entered /\ oneWay /\ ~allowReignition /\ ~stabilized
Accessible(p, f) == f = NewFrame \/ knowsArrival[p]
TypeOK ==
    /\ oneWay \in BOOLEAN /\ allowReignition \in BOOLEAN
    /\ entered \in BOOLEAN /\ stabilized \in BOOLEAN
    /\ location \in [Players -> {OW, CD, Returned}]
    /\ knowsArrival \in [Players -> BOOLEAN]
    /\ frameState \in [Frames -> {"None", "Inactive", "Active"}]
    /\ item \in [Players -> {"None", "Stabilizer", "Hourglass"}]
Init ==
    /\ oneWay \in BOOLEAN /\ allowReignition \in BOOLEAN
    /\ entered = FALSE /\ stabilized = FALSE
    /\ location = [p \in Players |-> OW]
    /\ knowsArrival = [p \in Players |-> TRUE]
    /\ frameState = [f \in Frames |-> "None"]
    /\ item \in [Players -> {"None", "Hourglass"}]

Enter(p) ==
    /\ location[p] = OW
    /\ location' = [location EXCEPT ![p] = CD] /\ entered' = TRUE
    /\ frameState' = [frameState EXCEPT ![Arrival] =
        IF oneWay /\ ~stabilized THEN "Inactive" ELSE "Active"]
    /\ UNCHANGED <<oneWay, allowReignition, stabilized, knowsArrival, item>>
Forget(p) ==
    /\ location[p] = CD /\ knowsArrival[p]
    /\ knowsArrival' = [knowsArrival EXCEPT ![p] = FALSE]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, location, frameState, item>>
Acquire(p) ==
    /\ location[p] = CD
    /\ LET needed == IF Gate THEN "Stabilizer" ELSE "Hourglass" IN
       /\ item[p] # needed
       /\ item' = [item EXCEPT ![p] = needed]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, location, knowsArrival, frameState>>
StabilizeRegistered(p) ==
    /\ location[p] = CD /\ knowsArrival[p] /\ item[p] = "Stabilizer"
    /\ frameState[Arrival] = "Inactive" /\ ~stabilized
    /\ stabilized' = TRUE /\ item' = [item EXCEPT ![p] = "None"]
    /\ UNCHANGED <<oneWay, allowReignition, entered, location, knowsArrival, frameState>>
StabilizeFromFrame(p) ==
    /\ location[p] = CD /\ item[p] = "Stabilizer"
    /\ frameState[NewFrame] = "None" /\ ~stabilized
    /\ stabilized' = TRUE /\ item' = [item EXCEPT ![p] = "None"]
    /\ UNCHANGED <<oneWay, allowReignition, entered, location, knowsArrival, frameState>>
Ignite(p, f) ==
    /\ location[p] = CD /\ Accessible(p, f) /\ ~Gate
    /\ frameState[f] # "Active" /\ item[p] = "Hourglass"
    /\ frameState' = [frameState EXCEPT ![f] = "Active"]
    /\ item' = [item EXCEPT ![p] = "None"]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, location, knowsArrival>>
Return(p, f) ==
    /\ location[p] = CD /\ Accessible(p, f) /\ frameState[f] = "Active"
    /\ location' = [location EXCEPT ![p] = Returned]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, knowsArrival, frameState, item>>
Emergency(p) ==
    /\ location[p] = CD
    /\ location' = [location EXCEPT ![p] = Returned]
    /\ UNCHANGED <<oneWay, allowReignition, entered, stabilized, knowsArrival, frameState, item>>
Next == \/ /\ \A p \in Players : location[p] = Returned
           /\ UNCHANGED vars
        \/ \E p \in Players :
            \/ Enter(p) \/ Forget(p) \/ Acquire(p) \/ StabilizeRegistered(p)
            \/ StabilizeFromFrame(p) \/ Emergency(p)
            \/ \E f \in Frames : Ignite(p, f) \/ Return(p, f)
Spec == Init /\ [][Next]_vars
FairSpec ==
    /\ Spec
    /\ \A p \in Players :
        /\ WF_vars(Acquire(p))
        /\ WF_vars(StabilizeRegistered(p) \/ StabilizeFromFrame(p))
        /\ WF_vars(\E f \in Frames : Ignite(p, f))
        /\ WF_vars(\E f \in Frames : Return(p, f))

StabilizedOnlyAfterEntry == stabilized => entered
NoPortalWhileGated == Gate => \A f \in Frames : frameState[f] # "Active"
ProgressIsPermanent == [][ /\ (entered => entered') /\ (stabilized => stabilized') ]_vars
CanAlwaysReturn == \A p \in Players : (location[p] = CD) ~> (location[p] = Returned)
=============================================================================
