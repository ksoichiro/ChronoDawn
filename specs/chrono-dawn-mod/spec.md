# Feature Specification: Chrono Dawn Mod - Time-Manipulation Themed Minecraft Mod Development

**Branch**: `main`
**Created**: 2025-10-18
**Status**: Draft
**Input**: User description: "Please develop a Minecraft mod. The base idea is described in @DRAFT.md"

## Game Design Philosophy *(critical design decisions)*

This section documents critical design decisions that define the core gameplay experience of Chrono Dawn Mod. These decisions fundamentally impact implementation and player experience quality.

### Design Decision 1: Respawn Mechanics - End Dimension Approach (2025-10-26)

**Background**:
Initial designs considered complex custom respawn logic where "Portal Stabilizer possession determines respawn location." However, implementation verification revealed this approach had issues with both game balance and implementation complexity.

**Decision**:
Chrono Dawn dimension respawn behavior **follows Minecraft's standard mechanics completely (same as End dimension)**.

**Core Principle - "End-like Balance"**:
```
Entry:    One-way portal (Time Hourglass required) → Creates tension
Return:   Portal Stabilizer makes it bidirectional → Player achievement
Death:    Normal Minecraft respawn (bed/anchor or world spawn) → Fair difficulty
Escape:   Break bed and die to return to Overworld → Always possible
```

**Rationale**:

1. **Balanced Difficulty**
   - ❌ Bad design: Player trapped in Chrono Dawn on death → Can be stuck without resources
   - ✅ Good design: Respawn at bed/world spawn on death → Always escapable

2. **Maintains Tension**
   - One-way portal itself creates sufficient tension
   - Players carefully prepare before entering
   - Even if death allows escape, Portal Stabilizer is still needed for convenient travel

3. **Player Agency**
   - Set bed in Chrono Dawn → Establish base and continue exploration
   - Break bed and die → Emergency escape mechanism
   - Craft Portal Stabilizer → Enable convenient round trips

4. **Implementation Simplicity**
   - No custom respawn logic required
   - Leverages existing Minecraft systems
   - Reduces risk of bugs and unexpected behaviors

**Comparison with End Dimension**:

| Feature | End | Chrono Dawn |
|---------|-----|--------------|
| Entry Portal | End Portal (one-way) | Time Hourglass Portal (one-way) |
| Return Mechanism | Defeat Ender Dragon → Return Portal | Use Portal Stabilizer → Bidirectional |
| Death Respawn | Bed/World Spawn (standard) | Bed/World Spawn (standard) |
| Escape Method | Break bed + die | Break bed + die |
| Tension Source | Portal is one-way ✅ | Portal is one-way ✅ |
| Difficulty Level | Moderate (can escape) | Moderate (can escape) |

**Portal Stabilizer Role** (Clarification):
- ✅ **Affects**: Portal travel (one-way → bidirectional)
- ❌ **Does NOT affect**: Respawn location (always follows Minecraft standard)

**Edge Case - "Getting Trapped"**:
- **Scenario**: Player enters Chrono Dawn, portal deactivates, no bed set
- **Old Design**: Trapped in Chrono Dawn unless Portal Stabilizer is crafted
- **New Design**: Can die and respawn in Overworld (bed/world spawn)
  - Maintains tension: Portal is still one-way, can't easily return to retrieve items
  - Prevents frustration: Always has an escape route
  - Preserves motivation: Must craft Portal Stabilizer for convenient travel

**Implementation Note**:
- No custom respawn handler (PlayerEventHandler remains placeholder)
- Chrono Dawn dimension_type.json has `"bed_works": true`
- Players respawn following vanilla Minecraft mechanics

**Design Philosophy Summary**:
> "Like the End, Chrono Dawn creates tension through one-way travel, not through punishing death. Players feel challenged but never trapped. The Portal Stabilizer is an achievement that enables convenience, not survival necessity."

---

### Design Decision 2: Portal Visual Design - Color Theme (2025-10-26)

**Background**:
Chrono Dawn Mod introduces a custom dimensional portal using the Custom Portal API. Visual differentiation from Minecraft's existing portal types (Nether's purple, End's green) and popular mod portals (Aether's blue) is critical for player recognition and mod identity.

**Decision**:
Chrono Dawn portal uses an **orange/gold color theme** with hex color **#db8813** (RGB: 219, 136, 19).

**Core Principle - "Visual Identity Through Color"**:
```
Portal Block:      Orange (#db8813) → Clear differentiation from all other portals
Teleport Overlay:  Orange (#db8813) → Consistent thematic experience
Particles:         Orange (#db8813) → Unified visual feedback
```

**Rationale**:

1. **Clear Differentiation**
   - ❌ Bad design: Purple portal → Confusion with Nether portals
   - ❌ Bad design: Blue portal → Confusion with Aether mod portals
   - ✅ Good design: Orange portal → Instantly recognizable as Chrono Dawn

2. **Thematic Consistency**
   - Orange/gold evokes imagery of clock hands and watch mechanisms
   - Warm metallic tone suggests brass gears and ancient timepieces
   - Represents both aged mechanical devices and temporal energy
   - Creates cohesive mod identity across visual elements

3. **Implementation Simplicity**
   - Single RGB value controls all portal visual elements (block, overlay, particles)
   - Custom Portal API's `.tintColor(r, g, b)` method provides straightforward implementation
   - No complex rendering logic or gradient systems required

**Color Specification**:

| Property | Value |
|----------|-------|
| Hex Color | #db8813 |
| RGB | 219, 136, 19 |
| Theme | Orange/Gold (Time/Clockwork) |

**Implementation**:
```java
// Constants in CustomPortalFabric.java and CustomPortalNeoForge.java
private static final int PORTAL_COLOR_R = 219;
private static final int PORTAL_COLOR_G = 136;
private static final int PORTAL_COLOR_B = 19;
```

**Affected Visual Elements**:
- Portal Block: The visible portal surface between frame blocks
- Teleport Overlay: Screen overlay effect during dimensional teleportation
- Particles: Particle effects around portal frame edges

**Comparison with Other Portals**:

| Portal Type | Color | Theme |
|-------------|-------|-------|
| Nether | Purple (RGB: ~138, 43, 226) | Fire/Hell |
| End | Green (particle-based) | Void/Ender |
| Aether (Mod) | Blue | Sky/Heaven |
| Chrono Dawn | Orange (#db8813) | Time/Clockwork |

**Alternative Colors Considered**:
- **Purple (Initial)**: Rejected due to confusion with Nether portals
- **Blue (#4e7bec)**: Rejected due to conflict with Aether mod portals (discovered during testing)
- **Green**: Rejected due to association with End portal
- **Red**: Rejected as it implies danger/nether themes
- **Multi-Color Gradients**: Rejected due to API limitations and implementation complexity

**Design Philosophy Summary**:
> "Chrono Dawn portal's orange/gold color theme creates instant visual recognition, evokes clockwork and temporal machinery, and ensures players never confuse it with vanilla or popular mod portals. Simplicity in implementation maintains performance while delivering clear differentiation."

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Initial Dimension Entry and Securing Return Path (Priority: P1)

The player discovers Clockstone and the Time Hourglass blueprint in Ancient Ruins in the Overworld, creates a portal, and enters Chrono Dawn. However, immediately after entry, the portal becomes inactive and the player is trapped. The player discovers the Portal Stabilizer blueprint in the Forgotten Library, crafts and uses it to secure free travel between the Overworld and Chrono Dawn.

**Why this priority**: This is the most critical phase that establishes the mod's fundamental game loop (dimension travel). Without this, players remain trapped in the dimension and the mod cannot function as intended.

**Independent Test**: Verify that players can discover Ancient Ruins in the Overworld, create a portal, and enter Chrono Dawn. Confirm that players can obtain food (Fruit of Time) necessary for survival in the dimension and discover the Portal Stabilizer blueprint in the Forgotten Library. Verify that players can craft the stabilizer, repair the portal, and travel freely between the Overworld and Chrono Dawn.

**Acceptance Scenarios**:

1. **Given** the player is in the Overworld, **When** exploring Ancient Ruins, **Then** Clockstone and Time Hourglass blueprint can be discovered
2. **Given** the player has crafted the Time Hourglass, **When** activating the portal, **Then** the player can enter Chrono Dawn
3. **Given** the player has entered Chrono Dawn, **When** entry is complete, **Then** the portal becomes inactive and return through normal means is impossible
4. **Given** the player is in Chrono Dawn, **When** exploring the Forgotten Library, **Then** the Portal Stabilizer blueprint can be discovered
5. **Given** the player has crafted the Portal Stabilizer, **When** using it on the portal frame, **Then** the Chrono Dawn dimension is stabilized with a spectacular effect (particles + sound) and message displayed to all players **And** the portal can be re-ignited with the Time Hourglass **And** free return to the Overworld through the portal becomes possible
6. **Given** the player is in Chrono Dawn, **When** obtaining and consuming Fruit of Time, **Then** hunger is restored and mining speed temporarily increases

---

### User Story 2 - Obtaining Time Manipulation Items and Boss Battle Preparation (Priority: P2)

The player explores the Desert Clock Tower and obtains Enhanced Clockstone. Using this, they craft time manipulation items (Time Clock, Spatial Connection Pickaxe, etc.) to improve combat and mining efficiency. Afterwards, they defeat the Time Guardian (mid-boss) to obtain the Master Clock Key.

**Why this priority**: Adds strategy and progression to the basic loop established in P1. Time manipulation items give players a tangible sense of "manipulating time" and strengthen the mod's uniqueness.

**Independent Test**: Verify that players can explore the Desert Clock Tower and obtain Enhanced Clockstone. Confirm that time manipulation items like Time Clock and Spatial Connection Pickaxe can be crafted. Verify that Time Clock can be used to cancel surrounding mob attacks. Confirm that the Spatial Connection Pickaxe increases drop rates when mining. Verify that the Time Guardian can be found, defeated in combat, and the Master Clock Key obtained.

**Acceptance Scenarios**:

1. **Given** the player is in Chrono Dawn, **When** exploring the Desert Clock Tower, **Then** Enhanced Clockstone can be obtained
2. **Given** the player has obtained Enhanced Clockstone, **When** crafting according to recipes, **Then** time manipulation items like Time Clock and Spatial Connection Pickaxe can be created
3. **Given** the player has Time Clock, **When** using it (with cooldown), **Then** surrounding mobs' next attack AI routines are forcibly cancelled
4. **Given** the player has Spatial Connection Pickaxe, **When** breaking blocks, **Then** drop items are doubled with a certain probability
5. **Given** the player has time manipulation items, **When** battling the Time Guardian, **Then** the Time Guardian can be defeated and the Master Clock Key obtained

---

### User Story 3 - Final Boss Defeat and Ultimate Rewards (Priority: P3)

The player uses the Master Clock Key to reach the deepest chamber and battles the final boss "Time Tyrant". Upon defeating the Tyrant, the Stasis Core is destroyed and Chrono Dawn's time is stabilized. As a reward, the player obtains the "Eye of Chronos" and can craft ultimate artifacts (Chronoblade, Time Guardian's Mail, etc.).

**Why this priority**: This is the mod's endgame content and the culmination of all story and exploration. Even without this, the mod can be enjoyed if P1 and P2 are complete, but this provides complete sense of accomplishment.

**Independent Test**: Verify that the Master Clock Key can be used to reach the deepest chamber of the Master Clock. Confirm that the Time Tyrant can be battled and defeated. Verify that upon defeat, the Stasis Core is destroyed and the Eye of Chronos is obtained. Confirm that ultimate artifacts (Chronoblade, Time Guardian's Mail, Echo Boots of Time, Spatial Connection Pickaxe, Unstable Pocket Watch) can be crafted using the Eye of Chronos and Fragments of Stasis Core.

**Acceptance Scenarios**:

1. **Given** the player has the Master Clock Key, **When** using it at the Master Clock entrance, **Then** the path to the deepest chamber opens
2. **Given** the player has reached the Master Clock's deepest chamber, **When** battling the Time Tyrant, **Then** the Time Tyrant can be defeated
3. **Given** the player has defeated the Time Tyrant, **When** the defeat is complete, **Then** the Stasis Core is destroyed and the Eye of Chronos and Fragments of Stasis Core can be obtained
4. **Given** the player has obtained the Eye of Chronos, **When** staying in Chrono Dawn, **Then** mob slowdown in the dimension is further enhanced and exploration is stabilized
5. **Given** the player has Fragments of Stasis Core, **When** crafting according to recipes, **Then** ultimate artifacts (Chronoblade, Time Guardian's Mail, Echo Boots of Time, etc.) can be created
6. **Given** the player has Chronoblade, **When** hitting mobs with attacks, **Then** there is a chance the mob's next attack AI is skipped, creating a one-sided attack opportunity
7. **Given** the player is equipped with Time Guardian's Mail, **When** receiving fatal damage, **Then** there is a chance to roll back to HP and position before damage, avoiding instant death
8. **Given** the player is equipped with Echo Boots of Time, **When** sprinting, **Then** a decoy (afterimage entity) that draws enemy targets is summoned briefly
9. **Given** the player has Unstable Pocket Watch, **When** using it, **Then** speed status effects of surrounding mobs and the player are instantly swapped

---

### Edge Cases

- If the player dies in Chrono Dawn without creating the Portal Stabilizer, how do they escape? (Respawn handling configuration)
- Portal stabilization applies to the entire dimension, so new portals created after stabilization can be immediately ignited with Time Hourglass (multiple portals can be freely created)
- After stabilization, when moving from Overworld to Chrono Dawn, the portal generated on the Chrono Dawn side is NOT automatically deactivated (remains active, allowing bidirectional travel)
- Portal Stabilizer does NOT have ignition functionality, only performs dimension stabilization. Ignition is done with Time Hourglass
- What happens if a player attempts to reach the Master Clock without defeating the Time Guardian? (Cannot progress due to key requirement)
- What is the frequency of probability-based activation for Chronoblade or Time Guardian's Mail? (10%, 25%, 50%, etc. - requires balance adjustment)
- When Reversal Resonance activates, what mitigation measures exist if the player cannot escape to a safe zone? (Effect duration adjustment, warning display, etc.)
- What is the behavior if other blocks are placed while Reversing Time Sandstone is restoring? (Destroy placed blocks and restore, or skip restoration)
- Does the mining speed boost effect of Fruit of Time stack with existing Haste enchantments? (Stacks, or set an upper limit)
- If the player uses Unstable Pocket Watch consecutively, do speed effects accumulate? (Effect overwrite, or cooldown setting)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system must generate Ancient Ruins in the Overworld, allowing players to discover Clockstone and Time Hourglass blueprints
- **FR-002**: Players must be able to create Time Hourglass, activate portals, and enter the Chrono Dawn dimension
- **FR-003**: Immediately after the player enters Chrono Dawn, the portal must deactivate, making it impossible for the player to return through normal means
- **FR-004**: The system must generate Forgotten Library in Chrono Dawn, allowing players to discover Portal Stabilizer blueprints
- **FR-005**: Players must be able to create and use Portal Stabilizer to stabilize the Chrono Dawn dimension, then re-ignite the portal with Time Hourglass to enable free travel between Overworld and Chrono Dawn
- **FR-006**: The majority of custom mobs in Chrono Dawn must have permanent extreme speed reduction (Slowness IV-V equivalent), while players must be able to move at normal speed
- **FR-007**: The system must generate Fruit of Time in Chrono Dawn, allowing players to collect and consume it to restore hunger and temporarily increase mining speed (Haste I equivalent)
- **FR-008**: The system must hook block destruction events for specific blocks (such as Reversing Time Sandstone) and execute restoration to the original block after a few seconds. Drop item emission must be suspended until restoration is complete
- **FR-009**: The system must hook entity collision events for Unstable Fungus blocks and apply random speed effects (Speed I or Slowness I) to colliding players for 0.5 seconds
- **FR-010**: The system must trigger Reversal Resonance for a short time (30 seconds to 1 minute) when the player creates Unstable Pocket Watch or immediately after defeating mid-bosses or final boss, applying Slowness IV to the player and Speed II to surrounding mobs
- **FR-011**: The system must generate Desert Clock Tower in Chrono Dawn, allowing players to obtain Enhanced Clockstone
- **FR-012**: Players must be able to use Enhanced Clockstone to create time manipulation items such as Time Clock and Spatial Link Pickaxe
- **FR-013**: Using Time Clock must forcibly cancel the next attack AI routine of surrounding mobs (with cooldown)
- **FR-014**: Breaking blocks with Spatial Link Pickaxe must probabilistically execute drop item generation processing twice
- **FR-015**: The system must spawn mid-boss "Time Guardian" in Chrono Dawn, allowing players to obtain Master Clock Key upon defeat
- **FR-016**: Using Master Clock Key must open the path to the deepest part of Master Clock
- **FR-017**: The system must spawn final boss "Time Tyrant" in the deepest part of Master Clock; upon player defeat, the Stasis Core is destroyed, and players must obtain Eye of Chronos and Fragments of Stasis Core
- **FR-018**: Upon obtaining Eye of Chronos, the basic speed reduction effect of mobs in Chrono Dawn must be further enhanced
- **FR-019**: Players must be able to use Fragments of Stasis Core to create ultimate artifacts (Chronoblade, Time Guardian's Mail, Echo Boots of Time, enhanced version of Spatial Link Pickaxe, Unstable Pocket Watch)
- **FR-020**: Landing attacks with Chronoblade must probabilistically skip the next attack AI of mobs
- **FR-021**: While equipped with Time Guardian's Mail, receiving fatal damage must probabilistically roll back to HP and position before damage
- **FR-022**: Dashing while equipped with Echo Boots of Time must summon a decoy (afterimage entity) that draws enemy targets for a short time
- **FR-023**: Using Unstable Pocket Watch must instantly swap current speed status effects between surrounding mobs and the player
- **FR-024**: Respawn processing when the player dies in Chrono Dawn must be set to either Overworld or a safe point within Chrono Dawn
- **FR-025**: To reduce server load, the system must implement time characteristics by maximally utilizing existing Minecraft systems (status effects, event hooks)

#### Additional Requirements (Gameplay Enhancement)

- **FR-026**: The system must spawn unique time-themed mobs (Temporal Wraith, Clockwork Sentinel, Time Keeper) in Chrono Dawn, providing players with diverse combat and trading experiences
- **FR-027**: Clockwork Sentinels must be immune to time distortion effects (Slowness IV) and move at normal speed
- **FR-028**: Temporal Wraiths must have the ability to phase through blocks when attacked to escape, and must apply Slowness II to players upon attack
- **FR-029**: Time Keepers must provide a trading interface as neutral mobs, offering items exchangeable for time-related items
- **FR-030**: The system must generate Time Crystal Ore in Chrono Dawn, allowing players to mine it at Y-coordinate 0-48
- **FR-031**: Players must be able to use Clockstone and Time Crystal to create basic equipment set (Tier 1: sword, axe, shovel, hoe, full armor)
- **FR-032**: Players must be able to use Enhanced Clockstone and Time Crystal to create advanced equipment set (Tier 2: Enhanced Clockstone equipment)
- **FR-033**: Tier 2 swords must probabilistically freeze enemies for 2 seconds on attack, and wearing full Tier 2 armor must grant complete immunity to time distortion effects
- **FR-034**: Players must be able to use Fruit of Time to create processed foods (Time Fruit Pie, Time Jam), each providing different buff effects
- **FR-035**: The system must naturally generate Time Wheat in Chrono Dawn, allowing players to harvest, cultivate, and create Time Bread
- **FR-036**: The system must generate additional biomes (Mountain, Swamp, Snowy, Cave) in Chrono Dawn, providing diversity in player exploration
- **FR-037**: The system must generate biome-specific blocks (Temporal Moss, Frozen Time Ice) in each biome to improve biome distinctiveness
- **FR-038**: Players must be able to create decorative blocks (Clockwork Block, Time Crystal Block, Temporal Bricks) and building variations (stairs, slabs, walls, fences)
- **FR-039**: The system must generate unique time-themed terrain features (Temporal Rift Canyon, Floating Clockwork Ruins, Time Crystal Caverns) after defeating Master Clock, providing endgame exploration elements

### Key Entities

- **Clockstone**: Basic material of Chrono Dawn. Can be discovered in Ancient Ruins and used as base material for portal creation and items.
- **Time Hourglass**: Special item created from Clockstone. Activates portals and enables entry into Chrono Dawn.
- **Portal Stabilizer**: Blueprints discovered in Forgotten Library, created through crafting. Repairs deactivated portals and enables free travel.
- **Fruit of Time**: Special food obtainable in Chrono Dawn. Restores hunger and provides temporary mining speed boost.
- **Enhanced Clockstone**: Advanced material obtainable in Desert Clock Tower. Required for time manipulation item recipes.
- **Time Clock**: Utility item created from Enhanced Clockstone. Cancels attack AI of surrounding mobs when used (with cooldown).
- **Spatial Link Pickaxe**: Tool created from Enhanced Clockstone. Probabilistically doubles drop items when breaking blocks.
- **Unstable Hourglass**: Material for ultimate items. Carries risk of triggering Reversal Resonance during crafting.
- **Master Clock Key**: Important item obtained by defeating Time Guardian. Opens the path to the deepest part of Master Clock.
- **Fragments of Stasis Core**: Most important material obtained by defeating Time Tyrant. Essential for creating ultimate artifacts.
- **Eye of Chronos**: Ultimate artifact obtained by defeating Time Tyrant. After acquisition, speed reduction of mobs in Chrono Dawn is further enhanced.
- **Chronoblade**: Ultimate sword created from Fragments of Stasis Core. Probabilistically skips next attack AI of mobs upon landing hits.
- **Time Guardian's Mail**: Ultimate chestplate created from Fragments of Stasis Core. Probabilistically rolls back to state before damage when receiving fatal damage.
- **Echo Boots of Time**: Ultimate boots created from Fragments of Stasis Core. Summons decoy that draws enemy targets when dashing.
- **Unstable Pocket Watch**: Ultimate utility item created from Fragments of Stasis Core. Swaps speed status effects of surrounding mobs and player when used.
- **Ancient Ruins**: Structure generated in Overworld. Clockstone and Time Hourglass blueprints can be discovered.
- **Forgotten Library**: Structure generated in Chrono Dawn. Portal Stabilizer blueprints can be discovered.
- **Desert Clock Tower**: Structure generated in Chrono Dawn. Enhanced Clockstone can be obtained.
- **Master Clock**: Structure in the deepest part of Chrono Dawn. Time Tyrant awaits.
- **Time Guardian**: Mid-boss appearing in Chrono Dawn. Drops Master Clock Key upon defeat.
- **Time Tyrant**: Final boss appearing in the deepest part of Master Clock. Root cause of eternally frozen dimension. Fragments of Stasis Core and Eye of Chronos can be obtained upon defeat.
- **Reversing Time Sandstone**: Special block generated in Chrono Dawn. Restores to original state a few seconds after being broken.
- **Unstable Fungus**: Special block generated in Chrono Dawn. Applies random speed effects to colliding players.

#### Additional Mobs (Gameplay Enhancement)

- **Temporal Wraith**: Hostile mob appearing in forest and plains biomes within Chrono Dawn. Has the ability to phase through blocks to escape when attacked, and applies Slowness II to players upon attack.
- **Clockwork Sentinel**: Hostile mob appearing in desert biomes and structures. Immune to time distortion effects (Slowness IV) and moves at normal speed as a threat. Drops Ancient Gear upon defeat.
- **Time Keeper**: Neutral mob appearing near Forgotten Library. Can trade like villagers, offering exchanges for time-related items (Time Hourglass, Portal Stabilizer materials, etc.).

#### Additional Equipment (Gameplay Enhancement)

- **Time Crystal Ore**: Rarer ore than Clockstone Ore. Generated at Y-coordinate 0-48 with vein size 3-5 blocks. Time Crystal can be mined.
- **Time Crystal**: Material mined from Time Crystal Ore. Used in equipment crafting to improve durability.
- **Clockstone Equipment Set (Tier 1)**: Basic equipment created from Clockstone and Time Crystal. Includes sword, axe, shovel, hoe, and full armor (helmet, chestplate, leggings, boots). Performance slightly better than iron equipment.
- **Enhanced Clockstone Equipment Set (Tier 2)**: Advanced equipment created from Enhanced Clockstone and Time Crystal. Swords probabilistically freeze enemies for 2 seconds on attack, and wearing full armor grants complete immunity to time distortion effects.

#### Additional Food (Gameplay Enhancement)

- **Time Fruit Pie**: Created from 3 Fruit of Time and wheat. Restores 8 hunger + 30 seconds Haste II effect.
- **Time Jam**: Created from 4 Fruit of Time and sugar. Restores 4 hunger + 60 seconds Speed I effect.
- **Time Wheat**: Crop naturally growing in plains and forest biomes within Chrono Dawn. Can be harvested after 8 growth stages. Can be cultivated like vanilla wheat.
- **Time Wheat Seeds**: Obtained by harvesting Time Wheat. Used for cultivation.
- **Time Bread**: Created from 3 Time Wheat. Restores 5 hunger.

#### Additional Blocks (Gameplay Enhancement)

- **Clockwork Block**: Decorative block. Features animated texture with rotating gears.
- **Time Crystal Block**: Decorative block. Emits light level 10, created from 9 Time Crystals.
- **Temporal Bricks**: Building block. Created from 4 Clockstone. Available in variations including stairs, slabs, walls, and fences.
- **Temporal Moss**: Decorative block exclusive to swamp biome. Has the ability to spread to surroundings like vanilla moss.
- **Frozen Time Ice**: Block exclusive to snowy biome. Unlike normal ice, does not melt and remains permanently slippery.

#### Additional Biomes (Gameplay Enhancement)

- **Chrono Dawn Mountain**: High-altitude stone terrain. Sparse vegetation with rocky landscape.
- **Chrono Dawn Swamp**: Terrain rich in water and clay. Temporal Moss grows naturally with unique vegetation.
- **Chrono Dawn Snowy**: Terrain covered in snow and ice. Frozen Time Ice is generated, themed around frozen time.
- **Chrono Dawn Cave**: Underground biome. Time Crystal Ore is exposed on walls.

#### Additional Terrain Features (US3 Enhancement)

- **Temporal Rift Canyon**: Special structure with distorted terrain and floating blocks. Time Crystal veins are exposed on walls.
- **Floating Clockwork Ruins**: Floating island structure with broken clockwork mechanisms. Contains loot chests.
- **Time Crystal Caverns**: Crystal structure generated underground. Time Crystals with luminous effects are formed.

## Assumptions and Dependencies

### Assumptions

- Players are assumed to be proficient in basic Minecraft operations (movement, mining, crafting, combat)
- Specific probability values for probability-based effects (Chronoblade, Time Guardian's Mail, etc.) will be determined through balance adjustments after implementation (recommended range: 10%-50%)
- Structures such as Ancient Ruins, Forgotten Library, Desert Clock Tower, and Master Clock are generated within discoverable range for players
- Time characteristics in Chrono Dawn (mob speed reduction, block restoration, etc.) are adjusted to not impair player exploration experience
- In server environments, multiple players may be in Chrono Dawn simultaneously, but individual player experiences are independent

### Dependencies

- Minecraft version: [To be determined at implementation - Recommended: Latest stable or widely-used version]
- Mod loader: [To be determined at implementation - Forge, Fabric, NeoForge, etc.]
- Design must consider operation in server environments
- Depends on existing Minecraft game systems (dimension generation, portal mechanics, entity behavior, status effects)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Players can discover Ancient Ruins in the Overworld and obtain materials and blueprints necessary for entering Chrono Dawn within 10 minutes
- **SC-002**: After entering Chrono Dawn, players can create Portal Stabilizer and secure return path to Overworld within 30 minutes
- **SC-003**: Mobs in Chrono Dawn show 50% or more speed reduction compared to normal mobs, allowing players to engage in strategic combat
- **SC-004**: Players can explore Desert Clock Tower and obtain Enhanced Clockstone and time manipulation items within 20 minutes
- **SC-005**: Players can defeat Time Guardian within 5 attempts and obtain Master Clock Key
- **SC-006**: Players can defeat Time Tyrant within 10 attempts and obtain Eye of Chronos and Fragments of Stasis Core
- **SC-007**: Players equipped with ultimate artifacts can perceive combat efficiency improvement of 30% or more compared to normal equipment
- **SC-008**: Server load increase is kept within 10% compared to existing Minecraft environment
- **SC-009**: 90% or more of players can understand Portal Stabilizer creation process and secure return path on first playthrough
- **SC-010**: 80% or more of players can perceive the effects of time manipulation items and appreciate the uniqueness of "manipulating time"
