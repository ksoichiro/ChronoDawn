# Minecraft Mod Specification: Echoes of the Chrono Dawn

## 1. Mod Overview

* Title: Echoes of the Chrono Dawn
* Theme: Escape from "Chrono Dawn," a world where time has stopped and become distorted, and gain control over the power of time.
* Objective: Explore the dimension, defeat the final boss "Time Tyrant," stabilize the world's time, and acquire the ultimate time manipulation artifact.
* Features: Unique gameplay that interferes with existing mob speed, block breaking, and item behavior. Expresses "time distortion" using tricks that minimize server load.

## 2. Storyline and Progression Goals

Players discover the existence of this world in ancient ruins in the Overworld, create a portal, and enter.

### Phase 1: Unexpected Confinement and Path to Escape

1. **"Discovery of Traces":** Discover **Clockstone** and **Time Hourglass Blueprint** in ancient ruins in the Overworld.
2. **"Unstable Gate":** Activate the portal using **Time Hourglass** and enter Chrono Dawn. Immediately after entry, the portal **stops functioning** due to time distortion.
3. **"Survival and Clues to Return":** Secure special food such as **Fruit of Time**, and discover the **"Portal Stabilizer Blueprint"** necessary for return in the **Forgotten Library**.
4. **"Portal Repair":** Craft the stabilizer and use it on the non-functioning portal to secure **free travel** to and from the Overworld.

### Phase 2: Time Mysteries and Traces of the Tyrant

1. **"Distortion Technology":** Explore the **Desert Clock Tower** and obtain **Enhanced Clockstone** necessary for time manipulation item recipes (**Time Clock**, **Spatially Linked Pickaxe**, etc.).
2. **"Guardian's Interference":** Defeat the mid-boss **"Time Guardian"** that appears to prevent player exploration, and obtain the **key** to the **Master Clock** where the final boss "Time Tyrant" resides.

### Phase 3: Final Battle and World Stabilization

1. **"Confrontation with the Tyrant":** Battle the final boss **"Time Tyrant"**, the source that eternally froze the dimension, in the deepest part of Master Clock.
2. **"Core Liberation":** Defeat the tyrant and destroy the **"Stasis Core"** that was stopping the flow of time.
3. **"Liberator of Chrono Dawn":** Acquire the **"Eye of Chronos"** as a reward, Chrono Dawn world stabilizes (special mobs and blocks become stably available), and free travel becomes possible.

## 3. Dimension: Chrono Dawn Characteristics and Behavior (Low Load Implementation)

To avoid server load, the main time characteristics that ensure dimension gameplay utilize existing Minecraft systems (status effects, event hooks).

| Characteristic Name | Implementation Mechanism | Gameplay Role |
| ------ | ------------ | -------------------- |
| Entity Delay | Apply extreme constant speed reduction (Slowness IV～V) to most custom mobs. Players act at normal speed. | Promotes strategic combat where attack timing of mobs can be predicted and observed. |
| Unstable Repair | Hook break events of specific blocks (Reversing Time Sandstone, etc.) and execute processing to swap back to original block after a few seconds. Prevents item drop emission until repair completion. | Functions as **"path obstacle"** where mining is impossible or difficult. Timed puzzle element. |
| Random Speed Floor | Hook entity collision events of Unstable Mycelium block. Applies random (Speed I or Slowness I) effect to colliding players for 0.5 seconds. | Creates unpredictability underfoot, locally increases difficulty of movement and evasion. |
| Reverse Resonance (Risk) | Limited trigger activation: Occurs only for short periods (30 seconds～1 minute) when player crafts Unstable Hourglass or immediately after defeating boss mob. Applies Slowness IV to player, Speed II to surrounding mobs, reversing the situation. | Risk-reward mechanism that clarifies difficulty adjustment gimmick dependent on player action. |

## 4. New Materials and Items

| Item Name | Type | Acquisition/Use | Unique Effect |
| ---- | ---- | ---- | ---- |
| Clockstone | Ore/Material | Portal creation, basic item material. | None (base material). |
| Stasis Core Fragment | Boss Material | "Time Tyrant" drop. Essential material for ultimate items. | None (most important material). |
| Fruit of Time | Food | Restores hunger. Temporarily increases player mining speed (Haste I) when eaten. | Only food source that increases mining efficiency. |
| Unstable Hourglass | Utility | Material for ultimate items. Involves risk of triggering Reverse Resonance when crafted. | Key material for advancing to next phase in exchange for risk. |
| Time Clock | Utility | Has cooldown after use. | When used, forcefully cancels next attack AI routine of surrounding mobs. |

## 5. Ultimate Artifacts (Using Stasis Core Fragment)

### 5.1. Weapons and Armor

| Item Name | Slot | Implementation Logic | Differentiation from Vanilla (Uniqueness) |
| ---- | ---- | ---- | ---- |
| Chronoblade | Sword | When attack hits, probabilistically **forcefully skips mob's next attack AI (cooldown reset)**. | Interferes with mob behavior itself, creating one-sided attack opportunities. |
| Time Guardian's Mail | Chestplate | When player receives fatal damage, probabilistically **rolls back to HP and position before damage (rewind)**. | Time manipulation defense that nullifies instant death events themselves rather than damage reduction. |
| Echoing Time Boots | Boots | When player runs, summons decoy (afterimage entity) that draws enemy targets for short time. | Specialized in tactical evasion/luring by diverting enemy targets. |

### 5.2. Tools and Utilities

| Item Name | Type | Implementation Logic | Differentiation from Vanilla (Uniqueness) |
| ---- | ---- | ---- | ---- |
| Spatially Linked Pickaxe | Pickaxe | When breaking blocks, probabilistically executes item drop generation processing twice. | Effects stack with vanilla Fortune enchantment, dramatically improving gathering efficiency. |
| Unstable Pocket Watch | Utility | When used, instantly swaps current speed status effects of surrounding mobs and players. | Strategic trump card that can temporarily exchange speed advantage by player's will. |
| Eye of Chronos | Ultimate Artifact | (Final reward) After acquisition, base delay effect of Chrono Dawn mobs is further enhanced (stabilized). | Permanently changes the environment of the entire dimension. |
