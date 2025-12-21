# Master Clock Structure Design

**Created**: 2025-11-09
**Purpose**: Define Master Clock structure specifications for US3 implementation

## Overview

Master Clockは、クロノドーンの最終ダンジョンとなる地下宮殿型構造物です。
ワールドスポーン近くに1つだけ生成され、Time Tyrant（ラスボス）との戦闘の舞台となります。

## Design Decisions

### 1. 構造タイプ: 地下宮殿型（案2採用）

**Rationale**:
- 地上部分を小さくし、発見難易度を高める
- Time Compassなどのガイダンスアイテムの重要性を強調
- 地下に広大な迷宮を配置し、探索要素を充実

### 2. 生成位置: ワールドスポーン基準（concentric_rings）

**Technical Approach**:
```json
{
  "placement": {
    "type": "minecraft:concentric_rings",
    "distance": 80,
    "spread": 20,
    "count": 1,
    "preferred_biomes": "#chronodawn:has_master_clock"
  }
}
```

**Characteristics**:
- ワールドスポーン(0, Y, 0)から80-100チャンク範囲内
- ディメンション全体で1つのみ生成
- プレイヤーの初回スポーン位置に近い

### 3. ランダム性: Jigsawシステム

**Approach**: Template Poolによる部屋のランダム配置
- 入口: 固定NBT
- 中層: 複数の部屋NBTからランダム選択・配置
- ボス部屋: 固定NBT

## Structure Specifications

### 地上部分（Entrance）

**Size**: 15x10x15（幅x高さx奥行き）
**Material**: Mossy Stone Bricks, Cracked Stone Bricks
**Theme**: 古代の小規模神殿風

**Features**:
- 中央に鍵穴付きの扉（Key to Master Clock使用）
- 扉の後ろに地下への階段
- 周囲に苔むした石レンガの壁
- 目立たないが、近づけば認識可能

**NBT File**: `master_clock_entrance.nbt`

### 地下部分（Main Dungeon）

**Size**: 41x50x41（幅x深さx奥行き）
**Material**: Stone Bricks, Deepslate, Polished Blackstone
**Theme**: 時間の中枢・機械仕掛けの迷宮

**Structure**:
```
Y層構造:
Y+10 to Y+0:   入口階層（階段、小部屋）
Y+0 to Y-15:   中層迷宮（Jigsaw部屋群）
Y-15 to Y-25:  移行層（Ancient Gears収集エリア）
Y-25 to Y-50:  ボス部屋（広大なホール）
```

### Jigsaw部屋システム

**部屋バリアント**（5-10種類のNBTファイル）:

1. **room_trap_arrows.nbt** (15x8x15)
   - 床の圧力板トラップ
   - 壁のディスペンサー（矢発射）
   - Reversing Time Sandstoneの床（踏むと復元）

2. **room_spawner.nbt** (17x8x17)
   - モブスポーナー（Skeleton/Zombie）
   - Time Distortion効果でモブもSlow
   - 複数の出入口

3. **room_maze.nbt** (21x8x21)
   - 迷路構造
   - 行き止まりにチェスト
   - Reversing Time Sandstoneの壁（破壊しても復元）

4. **room_puzzle_redstone.nbt** (15x10x15)
   - レッドストーンパズル
   - 正しい順序でレバー操作
   - 扉開放・アイテム獲得

5. **room_lava.nbt** (17x12x17)
   - 溶岩トラップ
   - パルクール要素
   - Reversing Time Sandstoneの足場（タイミング重視）

6. **room_time_puzzle.nbt** (15x8x15)
   - 時間逆行パズル
   - レバー操作→扉開く→数秒後閉じる
   - 急いで通過する必要

7. **room_guardian_arena.nbt** (19x10x19)
   - エリート敵（強化Skeleton x3）
   - 狭い戦闘エリア
   - クリア報酬チェスト

8. **room_rest.nbt** (13x8x13)
   - 休憩部屋
   - ベッド配置可能
   - チェスト（食料・ポーション）

**Template Pool Structure**:
```json
{
  "name": "chronodawn:master_clock/room_pool",
  "elements": [
    {"element": "chronodawn:master_clock/room_trap_arrows", "weight": 1},
    {"element": "chronodawn:master_clock/room_spawner", "weight": 1},
    {"element": "chronodawn:master_clock/room_maze", "weight": 1},
    {"element": "chronodawn:master_clock/room_puzzle_redstone", "weight": 1},
    {"element": "chronodawn:master_clock/room_lava", "weight": 1},
    {"element": "chronodawn:master_clock/room_time_puzzle", "weight": 1},
    {"element": "chronodawn:master_clock/room_guardian_arena", "weight": 1},
    {"element": "chronodawn:master_clock/room_rest", "weight": 1}
  ]
}
```

**部屋配置**:
- 中層に5-7部屋をJigsawでランダム配置
- 各部屋は通路で接続
- 毎回異なるレイアウトで生成

### Ancient Gears（段階的解放アイテム）

**Purpose**: ボス部屋への扉を開く鍵アイテム

**Item Specification**:
- **Name**: Ancient Gear（古代の歯車）
- **Type**: Quest Item
- **Max Stack Size**: 3
- **Required Count**: 3個

**配置**:
- 中層の部屋にランダムで3つ配置
- チェスト内、またはアイテムフレーム
- 各プレイヤーは探索しながら収集

**Usage**:
- 3つ集めると、ボス部屋前の扉が開く
- BlockEventHandlerで検出・扉開放処理

### ボス部屋

**Size**: 35x20x35（幅x高さx奥行き）
**Material**: Polished Blackstone, Gilded Blackstone, Crying Obsidian
**Theme**: 時間の暴君の玉座・静止のコア

**Features**:
- 中央に巨大な時計仕掛け（装飾）
- Time Tyrantのスポーン位置（中央）
- 柱の配置（戦術的カバー）
- 壁際に観客席風の段差
- 入口扉（Ancient Gears 3つで開放）
- 報酬チェスト（撃破後出現）

**NBT File**: `master_clock_boss_room.nbt`

## Gameplay Flow

### 1. 発見フェーズ

```
Player探索
  ↓
Time Compass使用（推奨）
  ↓
地上神殿発見（小さく目立たない）
  ↓
Key to Master Clock使用
  ↓
入口扉開放
```

### 2. 探索フェーズ

```
地下迷宮侵入
  ↓
Jigsaw部屋をランダム探索
  ├─ トラップ回避
  ├─ モブ撃破
  ├─ パズル解決
  └─ Ancient Gears収集（3つ）
  ↓
ボス部屋前到達
```

### 3. ボス戦フェーズ

```
Ancient Gears 3つ使用
  ↓
ボス部屋扉開放
  ↓
Time Tyrant戦闘
  ↓
撃破
  ↓
報酬獲得（Eye of Chronos, Fragment of Stasis Core）
```

## Reversing Time Sandstone活用

### 使用箇所

1. **room_trap_arrows**: 床のトラップ復元
2. **room_maze**: 壁が復元され、破壊による近道が不可
3. **room_lava**: 足場ブロックが復元され、タイミングが重要
4. **通路**: 一部の壁をReversing Time Sandstoneにし、破壊しても復元

### メカニクス

- プレイヤーが破壊
- 5秒後に自動復元
- 正規ルート以外の近道を防ぐ
- パズル要素（タイミング）

## Technical Implementation

### Required Files

#### NBT Structures
```
common/src/main/resources/data/chronodawn/structure/
  ├─ master_clock_entrance.nbt          (15x10x15)
  ├─ master_clock_room_trap_arrows.nbt  (15x8x15)
  ├─ master_clock_room_spawner.nbt      (17x8x17)
  ├─ master_clock_room_maze.nbt         (21x8x21)
  ├─ master_clock_room_puzzle_redstone.nbt (15x10x15)
  ├─ master_clock_room_lava.nbt         (17x12x17)
  ├─ master_clock_room_time_puzzle.nbt  (15x8x15)
  ├─ master_clock_room_guardian_arena.nbt (19x10x19)
  ├─ master_clock_room_rest.nbt         (13x8x13)
  └─ master_clock_boss_room.nbt         (35x20x35)
```

#### JSON Configurations
```
common/src/main/resources/data/chronodawn/worldgen/
  ├─ template_pool/master_clock/
  │   ├─ entrance_pool.json
  │   ├─ room_pool.json
  │   └─ boss_room_pool.json
  ├─ processor_list/
  │   └─ master_clock_loot.json
  ├─ structure/
  │   └─ master_clock.json
  └─ structure_set/
      └─ master_clock.json
```

#### Java Classes
```
common/src/main/java/com/chronodawn/
  ├─ items/quest/
  │   └─ AncientGearItem.java
  └─ events/
      └─ BlockEventHandler.java (Ancient Gears detection, door opening)
```

### Jigsaw Configuration

**Start Pool** (entrance_pool.json):
```json
{
  "elements": [
    {
      "element": {
        "location": "chronodawn:master_clock/entrance",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      },
      "weight": 1
    }
  ]
}
```

**Room Pool** (room_pool.json):
```json
{
  "elements": [
    {"element": "chronodawn:master_clock/room_trap_arrows", "weight": 1},
    {"element": "chronodawn:master_clock/room_spawner", "weight": 1},
    {"element": "chronodawn:master_clock/room_maze", "weight": 1},
    {"element": "chronodawn:master_clock/room_puzzle_redstone", "weight": 1},
    {"element": "chronodawn:master_clock/room_lava", "weight": 1},
    {"element": "chronodawn:master_clock/room_time_puzzle", "weight": 1},
    {"element": "chronodawn:master_clock/room_guardian_arena", "weight": 1},
    {"element": "chronodawn:master_clock/room_rest", "weight": 1}
  ]
}
```

## Testing Strategy

### Structure Generation Test
```java
@GameTest
public void testMasterClockGeneration() {
  // Verify Master Clock generates near world spawn
  // Check only 1 instance exists per dimension
}
```

### Jigsaw Room Test
```java
@GameTest
public void testMasterClockRoomVariety() {
  // Generate structure multiple times
  // Verify different room layouts
}
```

### Ancient Gears Test
```java
@GameTest
public void testAncientGearsCollection() {
  // Place 3 Ancient Gears in player inventory
  // Use at boss room door
  // Verify door opens
}
```

## Estimated Complexity

### High Complexity
- ⚠️ **Jigsaw System Setup**: Template poolの複雑な設定
- ⚠️ **NBT Creation**: 8-10個の部屋NBTを手作業で作成
- ⚠️ **Ancient Gears Logic**: アイテム検出・扉開放処理

### Medium Complexity
- **Reversing Time Sandstone Integration**: 既存ブロックの配置
- **Loot Processor**: チェスト報酬の設定

### Low Complexity
- **Structure Set Configuration**: concentric_ringsの設定
- **Biome Tag**: has_master_clock tagの作成

## Future Enhancements (Optional)

- [ ] Boss部屋の天井にストーリー描写（壁画）
- [ ] 各部屋に時間テーマの装飾（時計、歯車）
- [ ] カスタムBGM（ダンジョンテーマ曲）
- [ ] パーティクルエフェクト（時間の歪み）
- [ ] Ancient Gears収集時のサウンドエフェクト
- [ ] 部屋クリア時のチェックポイントシステム

## References

- **Minecraft Structures**: Stronghold (concentric_rings), Ancient City (Jigsaw)
- **Desert Clock Tower**: 既存実装（21x50x21, NBT+JSON）
- **Reversing Time Sandstone**: common/src/main/java/com/chronodawn/blocks/ReversingTimeSandstone.java
