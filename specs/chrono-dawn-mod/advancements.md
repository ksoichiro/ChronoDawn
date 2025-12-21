# Chrono Dawn Mod - Advancement System Specification

**Created**: 2025-11-16
**Status**: Draft
**Purpose**: Define all advancements (achievements) for Chrono Dawn Mod to track player progression and provide clear goals

---

## Advancement Tree Structure

```
root (Chrono Dawn)
├── US1: Initial Exploration
│   ├── portal_creation (Time Hourglass crafted)
│   ├── dimension_entry (Enter Chrono Dawn) [depends: portal_creation]
│   ├── forgotten_library (Discover Forgotten Library) [depends: dimension_entry]
│   └── portal_stabilization (Stabilize Portal) [depends: forgotten_library]
├── US2: Mid-game Exploration
│   ├── desert_clock_tower (Discover Desert Clock Tower) [depends: dimension_entry]
│   ├── time_manipulation_tools (Craft time manipulation tool) [depends: desert_clock_tower]
│   └── time_guardian_defeat (Defeat Time Guardian) [depends: time_manipulation_tools]
├── US3: Endgame Content
│   ├── master_clock_access (Access Master Clock) [depends: time_guardian_defeat]
│   ├── time_tyrant_defeat (Defeat Time Tyrant) [depends: master_clock_access]
│   └── ultimate_artifacts (Craft Ultimate Artifact) [depends: time_tyrant_defeat]
├── Food Progression
│   ├── first_time_fruit (Eat Time Fruit) [depends: dimension_entry]
│   ├── time_fruit_pie (Eat Time Fruit Pie) [depends: first_time_fruit]
│   ├── time_jam (Eat Time Jam) [depends: first_time_fruit]
│   ├── time_bread (Eat Time Bread) [depends: dimension_entry]
│   └── all_foods_consumed (Eat all custom foods) [challenge] [depends: time_fruit_pie, time_jam, time_bread]
├── Equipment Progression
│   ├── tier1_full_set (Equip full Tier 1 armor) [depends: dimension_entry]
│   ├── tier2_full_set (Equip full Tier 2 armor) [depends: desert_clock_tower]
│   └── time_distortion_immunity (Obtain time distortion immunity) [depends: tier2_full_set]
├── Exploration
│   ├── first_time_crystal_ore (Mine Time Crystal Ore) [depends: dimension_entry]
│   └── all_biomes_explored (Explore all Chrono Dawn biomes) [challenge] [depends: dimension_entry]
└── Mob Encounters
    ├── temporal_wraith_defeated (Defeat Temporal Wraith) [depends: dimension_entry]
    ├── clockwork_sentinel_defeated (Defeat Clockwork Sentinel) [depends: dimension_entry]
    └── time_keeper_trade (Trade with Time Keeper) [depends: dimension_entry]
```

---

## Advancement Details

### Root Advancement

#### `root.json` - Chrono Dawn
- **ID**: `chronodawn:root`
- **English Title**: "Chrono Dawn"
- **Japanese Title**: "クロノドーン"
- **English Description**: "Welcome to the world of time manipulation"
- **Japanese Description**: "時間操作の世界へようこそ"
- **Trigger**: `inventory_changed` (obtain Clockstone)
- **Parent**: None (root advancement)
- **Frame**: `task`
- **Icon**: `chronodawn:clockstone`
- **Background**: `chronodawn:textures/block/chronodawn_stone.png`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

---

### US1: Initial Exploration Advancements

#### `portal_creation.json` - A Gateway Through Time
- **ID**: `chronodawn:portal_creation`
- **English Title**: "A Gateway Through Time"
- **Japanese Title**: "時を超える門"
- **English Description**: "Craft a Time Hourglass to create a portal to the Chrono Dawn"
- **Japanese Description**: "時の砂時計を作成し、クロノドーンへの扉を開く"
- **Trigger**: `inventory_changed` (obtain Time Hourglass)
- **Parent**: `chronodawn:root`
- **Frame**: `task`
- **Icon**: `chronodawn:time_hourglass`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `dimension_entry.json` - Entering the Frozen World
- **ID**: `chronodawn:dimension_entry`
- **English Title**: "Entering the Frozen World"
- **Japanese Title**: "凍結した世界へ"
- **English Description**: "Enter the Chrono Dawn dimension for the first time"
- **Japanese Description**: "初めてクロノドーン次元に突入する"
- **Trigger**: `changed_dimension` (to `chronodawn:chronodawn`)
- **Parent**: `chronodawn:portal_creation`
- **Frame**: `goal`
- **Icon**: `chronodawn:chronodawn_stone`
- **Show Toast**: `true`
- **Announce to Chat**: `true`
- **Hidden**: `false`

#### `forgotten_library.json` - Archives of Lost Time
- **ID**: `chronodawn:forgotten_library`
- **English Title**: "Archives of Lost Time"
- **Japanese Title**: "失われた時の記録庫"
- **English Description**: "Discover the Forgotten Library structure"
- **Japanese Description**: "忘れられた図書館を発見する"
- **Trigger**: `location` (structure: `chronodawn:forgotten_library`)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `minecraft:bookshelf`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `portal_stabilization.json` - Anchoring Time
- **ID**: `chronodawn:portal_stabilization`
- **English Title**: "Anchoring Time"
- **Japanese Title**: "時間の固定"
- **English Description**: "Use the Portal Stabilizer to enable two-way portal travel"
- **Japanese Description**: "ポータル安定化装置を使用し、双方向移動を可能にする"
- **Trigger**: Custom (portal stabilization event)
- **Parent**: `chronodawn:forgotten_library`
- **Frame**: `goal`
- **Icon**: `chronodawn:portal_stabilizer`
- **Show Toast**: `true`
- **Announce to Chat**: `true`
- **Hidden**: `false`

---

### US2: Mid-game Exploration Advancements

#### `desert_clock_tower.json` - The Clockwork Spire
- **ID**: `chronodawn:desert_clock_tower`
- **English Title**: "The Clockwork Spire"
- **Japanese Title**: "時計仕掛けの尖塔"
- **English Description**: "Discover the Desert Clock Tower structure"
- **Japanese Description**: "砂漠の時計塔を発見する"
- **Trigger**: `location` (structure: `chronodawn:desert_clock_tower`)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `chronodawn:enhanced_clockstone`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_manipulation_tools.json` - Tools of Temporal Control
- **ID**: `chronodawn:time_manipulation_tools`
- **English Title**: "Tools of Temporal Control"
- **Japanese Title**: "時間操作の道具"
- **English Description**: "Craft your first time manipulation tool (Time Clock or Spatially-Linked Pickaxe)"
- **Japanese Description**: "初めて時間操作ツール（タイムクロックまたは空間連結ツルハシ）を作成する"
- **Trigger**: `inventory_changed` (obtain Time Clock OR Spatially-Linked Pickaxe)
- **Parent**: `chronodawn:desert_clock_tower`
- **Frame**: `task`
- **Icon**: `chronodawn:time_clock`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_guardian_defeat.json` - Vanquisher of the Guardian
- **ID**: `chronodawn:time_guardian_defeat`
- **English Title**: "Vanquisher of the Guardian"
- **Japanese Title**: "番人の打倒"
- **English Description**: "Defeat the Time Guardian and claim the Master Clock Key"
- **Japanese Description**: "時の番人を倒し、マスタークロックの鍵を手に入れる"
- **Trigger**: `player_killed_entity` (entity: `chronodawn:time_guardian`)
- **Parent**: `chronodawn:time_manipulation_tools`
- **Frame**: `goal`
- **Icon**: `chronodawn:master_clock_key`
- **Show Toast**: `true`
- **Announce to Chat**: `true`
- **Hidden**: `false`

---

### US3: Endgame Content Advancements

#### `master_clock_access.json` - The Heart of Time
- **ID**: `chronodawn:master_clock_access`
- **English Title**: "The Heart of Time"
- **Japanese Title**: "時間の心臓部"
- **English Description**: "Access the Master Clock using the Master Clock Key"
- **Japanese Description**: "マスタークロックの鍵を使用し、最深部に到達する"
- **Trigger**: `location` (structure: `chronodawn:master_clock`)
- **Parent**: `chronodawn:time_guardian_defeat`
- **Frame**: `task`
- **Icon**: `chronodawn:master_clock_key`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_tyrant_defeat.json` - Liberator of Chrono Dawn
- **ID**: `chronodawn:time_tyrant_defeat`
- **English Title**: "Liberator of Chrono Dawn"
- **Japanese Title**: "クロノドーンの解放者"
- **English Description**: "Defeat the Time Tyrant and shatter the Stasis Core"
- **Japanese Description**: "時間の暴君を倒し、静止のコアを破壊する"
- **Trigger**: `player_killed_entity` (entity: `chronodawn:time_tyrant`)
- **Parent**: `chronodawn:master_clock_access`
- **Frame**: `challenge`
- **Icon**: `chronodawn:chronos_eye`
- **Show Toast**: `true`
- **Announce to Chat**: `true`
- **Hidden**: `false`

#### `ultimate_artifacts.json` - Master of Time
- **ID**: `chronodawn:ultimate_artifacts`
- **English Title**: "Master of Time"
- **Japanese Title**: "時の支配者"
- **English Description**: "Craft an ultimate artifact from Stasis Core Shards"
- **Japanese Description**: "静止のコアの破片から究極のアーティファクトを作成する"
- **Trigger**: `inventory_changed` (obtain Chronoblade OR Time Keeper's Mail OR Temporal Echo Boots OR Unstable Pocket Watch)
- **Parent**: `chronodawn:time_tyrant_defeat`
- **Frame**: `challenge`
- **Icon**: `chronodawn:chronoblade`
- **Show Toast**: `true`
- **Announce to Chat**: `true`
- **Hidden**: `false`

---

### Food Progression Advancements

#### `first_time_fruit.json` - Taste of Time
- **ID**: `chronodawn:first_time_fruit`
- **English Title**: "Taste of Time"
- **Japanese Title**: "時の味"
- **English Description**: "Consume a Time Fruit for the first time"
- **Japanese Description**: "初めて時間の果実を食べる"
- **Trigger**: `consume_item` (item: `chronodawn:time_fruit`)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `chronodawn:time_fruit`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_fruit_pie.json` - Temporal Pastry Chef
- **ID**: `chronodawn:time_fruit_pie`
- **English Title**: "Temporal Pastry Chef"
- **Japanese Title**: "時間のパティシエ"
- **English Description**: "Eat a Time Fruit Pie"
- **Japanese Description**: "時の果実パイを食べる"
- **Trigger**: `consume_item` (item: `chronodawn:time_fruit_pie`)
- **Parent**: `chronodawn:first_time_fruit`
- **Frame**: `task`
- **Icon**: `chronodawn:time_fruit_pie`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_jam.json` - Sweet Temporal Spread
- **ID**: `chronodawn:time_jam`
- **English Title**: "Sweet Temporal Spread"
- **Japanese Title**: "甘美な時の広がり"
- **English Description**: "Eat Time Jam"
- **Japanese Description**: "時のジャムを食べる"
- **Trigger**: `consume_item` (item: `chronodawn:time_jam`)
- **Parent**: `chronodawn:first_time_fruit`
- **Frame**: `task`
- **Icon**: `chronodawn:time_jam`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_bread.json` - Bread from a Frozen Age
- **ID**: `chronodawn:time_bread`
- **English Title**: "Bread from a Frozen Age"
- **Japanese Title**: "凍結時代のパン"
- **English Description**: "Eat Time Bread"
- **Japanese Description**: "時のパンを食べる"
- **Trigger**: `consume_item` (item: `chronodawn:time_bread`)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `chronodawn:time_bread`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `all_foods_consumed.json` - Gourmet of Time
- **ID**: `chronodawn:all_foods_consumed`
- **English Title**: "Gourmet of Time"
- **Japanese Title**: "時のグルメ"
- **English Description**: "Eat all custom Chrono Dawn foods (Time Fruit, Time Fruit Pie, Time Jam, Time Bread)"
- **Japanese Description**: "クロノドーンの全てのカスタム食料を食べる（時間の果実、時の果実パイ、時のジャム、時のパン）"
- **Trigger**: Custom (composite trigger tracking all 4 foods)
- **Parent**: `chronodawn:time_fruit_pie`, `chronodawn:time_jam`, `chronodawn:time_bread`
- **Frame**: `challenge`
- **Icon**: `minecraft:golden_apple`
- **Show Toast**: `true`
- **Announce to Chat**: `true`
- **Hidden**: `false`

---

### Equipment Progression Advancements

#### `tier1_full_set.json` - Clockwork Warrior
- **ID**: `chronodawn:tier1_full_set`
- **English Title**: "Clockwork Warrior"
- **Japanese Title**: "時計仕掛けの戦士"
- **English Description**: "Equip a full set of Tier 1 Clockstone armor"
- **Japanese Description**: "Tier 1クロックストーン装備一式を装着する"
- **Trigger**: Custom (wearing Clockstone Helmet, Chestplate, Leggings, Boots)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `chronodawn:clockstone_chestplate`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `tier2_full_set.json` - Enhanced Temporal Armor
- **ID**: `chronodawn:tier2_full_set`
- **English Title**: "Enhanced Temporal Armor"
- **Japanese Title**: "強化された時間装甲"
- **English Description**: "Equip a full set of Tier 2 Enhanced Clockstone armor"
- **Japanese Description**: "Tier 2強化クロックストーン装備一式を装着する"
- **Trigger**: Custom (wearing Enhanced Clockstone Helmet, Chestplate, Leggings, Boots)
- **Parent**: `chronodawn:desert_clock_tower`
- **Frame**: `task`
- **Icon**: `chronodawn:enhanced_clockstone_chestplate`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_distortion_immunity.json` - Beyond Time's Grasp
- **ID**: `chronodawn:time_distortion_immunity`
- **English Title**: "Beyond Time's Grasp"
- **Japanese Title**: "時の束縛を超えて"
- **English Description**: "Obtain time distortion immunity by wearing full Tier 2 armor"
- **Japanese Description**: "Tier 2フルアーマー装備により時間歪曲免疫を得る"
- **Trigger**: Custom (wearing full Tier 2 armor + time distortion immunity effect active)
- **Parent**: `chronodawn:tier2_full_set`
- **Frame**: `goal`
- **Icon**: `minecraft:potion` (with custom NBT for effect icon)
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

---

### Exploration Advancements

#### `first_time_crystal_ore.json` - Crystallized Time
- **ID**: `chronodawn:first_time_crystal_ore`
- **English Title**: "Crystallized Time"
- **Japanese Title**: "結晶化した時間"
- **English Description**: "Mine your first Time Crystal Ore"
- **Japanese Description**: "初めて時のクリスタル鉱石を採掘する"
- **Trigger**: `item_used_on_block` (item: pickaxe, block: `chronodawn:time_crystal_ore`)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `chronodawn:time_crystal`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `all_biomes_explored.json` - Temporal Cartographer
- **ID**: `chronodawn:all_biomes_explored`
- **English Title**: "Temporal Cartographer"
- **Japanese Title**: "時間の地図製作者"
- **English Description**: "Explore all Chrono Dawn biomes (Plains, Desert, Forest, Mountain, Swamp, Snowy, Cave)"
- **Japanese Description**: "クロノドーンの全バイオームを探索する（平原、砂漠、森林、山岳、沼地、雪原、洞窟）"
- **Trigger**: Custom (composite trigger tracking all 7 biomes)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `challenge`
- **Icon**: `minecraft:map`
- **Show Toast**: `true`
- **Announce to Chat**: `true`
- **Hidden**: `false`

---

### Mob Encounters Advancements

#### `temporal_wraith_defeated.json` - Wraith Hunter
- **ID**: `chronodawn:temporal_wraith_defeated`
- **English Title**: "Wraith Hunter"
- **Japanese Title**: "亡霊狩り"
- **English Description**: "Defeat your first Temporal Wraith"
- **Japanese Description**: "初めて時の亡霊を倒す"
- **Trigger**: `player_killed_entity` (entity: `chronodawn:temporal_wraith`)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `minecraft:phantom_membrane`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `clockwork_sentinel_defeated.json` - Mechanism Breaker
- **ID**: `chronodawn:clockwork_sentinel_defeated`
- **English Title**: "Mechanism Breaker"
- **Japanese Title**: "機構破壊者"
- **English Description**: "Defeat your first Clockwork Sentinel"
- **Japanese Description**: "初めて時計仕掛けの番兵を倒す"
- **Trigger**: `player_killed_entity` (entity: `chronodawn:clockwork_sentinel`)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `chronodawn:ancient_gear`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

#### `time_keeper_trade.json` - Temporal Merchant
- **ID**: `chronodawn:time_keeper_trade`
- **English Title**: "Temporal Merchant"
- **Japanese Title**: "時の商人"
- **English Description**: "Trade with a Time Keeper for the first time"
- **Japanese Description**: "初めて時間の管理者と取引する"
- **Trigger**: Custom (villager trade with Time Keeper entity)
- **Parent**: `chronodawn:dimension_entry`
- **Frame**: `task`
- **Icon**: `minecraft:emerald`
- **Show Toast**: `true`
- **Announce to Chat**: `false`
- **Hidden**: `false`

---

## Implementation Notes

### Trigger Types

Chrono Dawn advancements use the following trigger types:

1. **Standard Minecraft Triggers**:
   - `inventory_changed` - Obtaining items
   - `consume_item` - Eating/drinking items
   - `changed_dimension` - Entering dimensions
   - `location` - Entering structures/biomes
   - `player_killed_entity` - Defeating mobs/bosses
   - `item_used_on_block` - Mining ores

2. **Custom Triggers** (require implementation):
   - Portal stabilization event
   - Composite food consumption tracking (all foods)
   - Armor set detection (full Tier 1/Tier 2)
   - Time distortion immunity effect detection
   - Composite biome exploration tracking (all biomes)
   - Villager trade with Time Keeper

### Advancement Frames

- **task** (default) - Bronze square frame, standard advancement
- **goal** - Rounded frame, major milestone
- **challenge** - Fancy frame, optional difficult content

### Experience Rewards

Advancements do NOT grant experience points by default in Minecraft 1.21.1. Experience is granted only through gameplay (mining, killing mobs, etc.).

However, we can add experience rewards to specific advancements if desired:

- **goal** advancements: 50-100 XP
- **challenge** advancements: 100-500 XP

### Localization Requirements

All advancement titles and descriptions must be translated in:
- `en_us.json` (English)
- `ja_jp.json` (Japanese)

Format:
```json
{
  "advancements.chronodawn.root.title": "Chrono Dawn",
  "advancements.chronodawn.root.description": "Welcome to the world of time manipulation"
}
```

### File Locations

All advancement JSON files are stored in:
```
common/src/main/resources/data/chronodawn/advancements/
```

Subdirectory structure (optional, for organization):
```
advancements/
├── root.json
├── story/
│   ├── us1/
│   ├── us2/
│   └── us3/
├── food/
├── equipment/
├── exploration/
└── mobs/
```

---

## Total Advancement Count

- **Root**: 1
- **US1**: 4 (portal_creation, dimension_entry, forgotten_library, portal_stabilization)
- **US2**: 3 (desert_clock_tower, time_manipulation_tools, time_guardian_defeat)
- **US3**: 3 (master_clock_access, time_tyrant_defeat, ultimate_artifacts)
- **Food**: 5 (first_time_fruit, time_fruit_pie, time_jam, time_bread, all_foods_consumed)
- **Equipment**: 3 (tier1_full_set, tier2_full_set, time_distortion_immunity)
- **Exploration**: 2 (first_time_crystal_ore, all_biomes_explored)
- **Mobs**: 3 (temporal_wraith_defeated, clockwork_sentinel_defeated, time_keeper_trade)

**Total**: 24 advancements

---

## Testing Checklist

When implementing advancements, verify:

- [ ] All advancement JSON files are valid and load without errors
- [ ] Advancement tree displays correctly in-game (press `L` key)
- [ ] All triggers fire correctly when conditions are met
- [ ] Toast notifications appear for relevant advancements
- [ ] Chat announcements appear for `goal` and `challenge` advancements
- [ ] Localization strings display correctly in both English and Japanese
- [ ] Advancement dependencies work correctly (parent advancements unlock children)
- [ ] Custom triggers (portal stabilization, armor sets, etc.) function properly
- [ ] All advancement icons display correctly

---

## Future Expansion Ideas

Potential additional advancements for future updates:

- **Speed Runner**: Complete all US1-US3 advancements in under 2 hours
- **No Time to Waste**: Defeat Time Tyrant without dying in Chrono Dawn
- **Temporal Archaeologist**: Discover all structures (Ancient Ruins, Forgotten Library, Desert Clock Tower, Master Clock)
- **Master Craftsman**: Craft all Chrono Dawn items at least once
- **Mob Encyclopedia**: Defeat at least 1 of each Chrono Dawn mob type
- **Temporal Farmer**: Harvest 1000 Time Wheat
- **Crystal Collector**: Mine 100 Time Crystal Ore blocks
