# Progression toggle investigation

This investigation compares the configuration warnings with the shipped 1.21.1
resources and code. It is preliminary: it is not yet an exhaustive reachability
model, and findings must be checked against every supported version before they
are used as cross-version guarantees.

| Source | Observed progression relationship | Evidence |
| --- | --- | --- |
| Ancient Ruins | The Time Hourglass recipe also unlocks from glass. | `common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes/misc/time_hourglass.json` |
| Forgotten Library | Its chest contains a Portal Stabilizer recipe book, but the recipe advancement also unlocks from Clockstone. | `common/1.21.1/src/main/resources/data/chronodawn/loot_table/chests/forgotten_library.json`, `common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes/misc/portal_stabilizer.json` |
| Desert Clock Tower | Time Guardian drops the Master Clock key and Enhanced Clockstone. | `common/1.21.1/src/main/java/com/chronodawn/worldgen/spawning/TimeGuardianSpawner.java`, `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/entities/time_guardian.json` |
| Master Clock | Opening the boss room door spawns Time Tyrant. The door needs the key and three Ancient Gears. | `common/1.21.1/src/main/java/com/chronodawn/events/BlockEventHandler.java`, `docs/player_guide.md` |
| Four mid-boss structures | The four boss drops craft Chrono Aegis, which is recommended for Time Tyrant but is not listed as a door requirement. | `common/1.21.1/src/main/resources/data/chronodawn/recipe/chrono_aegis.json`, `docs/player_guide.md` |
| Core plains biome | It contains Clockstone ore, Time Crystal ore and Fruit of Time trees. It cannot be disabled. | `common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/chronodawn_plains.json`, `common/shared/src/main/java/com/chronodawn/config/ManagedBiome.java` |

The Portal Stabilizer recipe uses Clockstone, a Clockstone block, Fruit of Time
and Time Hourglasses. Ancient Gears use Enhanced Clockstone, Time Crystals and
iron. Enhanced Clockstone has sources besides Time Guardian, including other
boss loot and structure chests. These alternative sources matter when checking
warnings and must not be replaced with an assumed exclusive dependency.

The existing biome test enumerates all biome toggles to check structure biome
availability. A separate progression model must also guard its assumptions
against recipe, loot, advancement, spawning and biome data changes. In
particular, a model that treats the library as the only way to unlock the
Stabilizer recipe would disagree with the advancement above.
