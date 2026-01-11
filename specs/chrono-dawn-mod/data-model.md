# Data Model: Chrono Dawn Mod

**Feature**: Chrono Dawn Mod - 時間操作をテーマにしたMinecraft Mod
**Architecture**: Architectury Multi-Loader (NeoForge + Fabric)
**Date**: 2025-10-19 (Updated for Architectury)

## Overview

このドキュメントは、Chrono Dawn Modのデータモデルを定義します。Minecraft Modの特性上、エンティティはブロック、アイテム、Mob、ディメンション、構造物など多岐にわたります。

**Architectury Multi-Loader対応**:
- このModはArchitecturyフレームワークを使用し、NeoForgeとFabricの両方に対応
- 共通ロジックは `common/` モジュールで実装（80%）
- ローダー固有実装は `fabric/` および `neoforge/` モジュールで実装（20%）
- データモデルは両ローダーで互換性を維持

## Core Systems

### Dimension: Chrono Dawn

**Type**: Custom Dimension
**Key**: `chronodawn:chronodawn_dimension`

**Properties**:
- `dimension_type`: カスタムディメンションタイプ（時間の歪みを表現する環境設定）
- `biomes`: カスタムバイオームのリスト（砂漠、図書館周辺、マスタークロック周辺など）
- `world_generation`: カスタムワールド生成ロジック
  - 構造物: 忘れられた図書館、砂漠の時計塔、マスタークロック
  - 鉱石: クロックストーン、強化クロックストーン
  - 植生: 時間の果実の木
  - 特殊ブロック: 逆流の砂岩、不安定な菌糸

**State Transitions**:
- 初期状態: 時間が停止（Mobが極端に遅い）
- クロノスの瞳獲得後: 安定化（Mobの速度低下がさらに強化）

### Portal System

**Type**: Custom Portal
**Implementation**:
- **共通ロジック**: `common/` モジュールでポータル状態管理、検証ロジックを実装
- **NeoForge**: custom portal system Reforged を使用（`neoforge/compat/CustomPortalNeoForge.java`）
- **Fabric**: custom portal system (Fabric版) を使用（`fabric/compat/CustomPortalFabric.java`）

**Portal Frame**:
- `frame_block`: クロックストーンブロック（最小サイズ: 4x5、最大サイズ: 23x23）
- `ignition_item`: 時の砂時計
- `portal_tint`: 時間の歪みを表現する紫/青のグラデーション

**Portal States**:
1. **未起動**: フレームのみ存在
2. **起動済み**: 時の砂時計で起動、クロノドーンへの転送可能
3. **機能停止**: プレイヤーがクロノドーンに突入した直後、自動的に機能停止
4. **安定化**: ポータル安定化装置を使用後、双方向転送が可能

**Validation Rules**:
- ポータルフレームは完全な矩形でなければならない
- 各ポータルは一意のポータルIDを持つ（マルチポータル対応）
- 両ローダー間で状態管理を統一（共通のポータルレジストリ）

## Blocks

### Clockstone Ore (クロックストーン鉱石)

**Type**: Custom Ore Block
**Location**: オーバーワールド（古代遺跡）、クロノドーン（地下）

**Properties**:
- `hardness`: 3.0
- `blast_resistance`: 3.0
- `drop`: クロックストーン（アイテム）
- `fortune_multiplier`: 幸運エンチャントで増加可能

### Reversing Time Sandstone (逆流の砂岩)

**Type**: Special Block
**Location**: クロノドーン

**Properties**:
- `restoration_time`: 3秒（破壊後、元の状態に戻るまでの時間）
- `drop_on_break`: なし（修復されるまでドロップを抑制）
- `can_be_moved_by_piston`: false

**State Transitions**:
1. **通常状態**: 設置されている
2. **破壊中**: プレイヤーが破壊を試みる
3. **修復待機**: 破壊完了後、3秒間カウントダウン
4. **修復完了**: 元の状態に戻る

**Edge Cases**:
- 修復前に他のブロックが配置された場合: 配置されたブロックを破壊して元に戻す

### Unstable Fungus (不安定な菌糸)

**Type**: Special Block
**Location**: クロノドーン

**Properties**:
- `on_entity_collision`: エンティティが衝突時にランダム速度効果を付与
- `effect_duration`: 0.5秒
- `possible_effects`: [Speed I, Slowness I]
- `effect_chance`: 100%（衝突時必ず発動）

## Items

### Base Materials

#### Clockstone (クロックストーン)

**Type**: Material Item
**Max Stack Size**: 64

**Crafting Uses**:
- 時の砂時計の素材
- ポータルフレームブロック
- 基本的なアイテムの素材

#### Enhanced Clockstone (強化クロックストーン)

**Type**: Material Item
**Max Stack Size**: 64
**Obtained From**: 砂漠の時計塔

**Crafting Uses**:
- タイムクロック
- 空間連結ツルハシ
- 不安定な砂時計

#### Fragment of Stasis Core (静止のコアの破片)

**Type**: Boss Material
**Max Stack Size**: 16
**Obtained From**: 時間の暴君（ラスボス）

**Crafting Uses**:
- クロノブレード
- 時の番人のメイル
- 時間の残響ブーツ
- 不安定な懐中時計（究極版）

### Tools & Utilities

#### Time Hourglass (時の砂時計)

**Type**: Portal Ignition Item
**Max Stack Size**: 1
**Durability**: 1回使用で消費

**Use**:
- ポータルフレームを右クリックでポータルを起動

#### Portal Stabilizer (ポータル安定化装置)

**Type**: Utility Item
**Max Stack Size**: 1
**Durability**: 1回使用で消費
**Obtained From**: 忘れられた図書館で設計図を発見後、クラフト

**Use**:
- 機能停止したポータルを右クリックで安定化

#### Time Clock (タイムクロック)

**Type**: Utility Item
**Max Stack Size**: 1
**Cooldown**: 10秒

**Use**:
- 使用時、周囲のMobの次の攻撃AIルーチンを強制的にキャンセル
- 効果範囲: 半径8ブロック

#### Unstable Hourglass (不安定な砂時計)

**Type**: Material Item (リスクあり)
**Max Stack Size**: 1

**Crafting Trigger**:
- クラフト時に反転共鳴を誘発（プレイヤーにSlowness IV、周囲のMobにSpeed II、30秒～1分間）

### Consumables

#### Fruit of Time (時間の果実)

**Type**: Food
**Max Stack Size**: 64

**Properties**:
- `hunger_restore`: 4
- `saturation_modifier`: 0.3
- `eating_time`: 32 ticks (1.6秒)
- `effect_on_eat`: Haste I、60秒間

### Weapons

#### Chronoblade (クロノブレード)

**Type**: Sword
**Attack Damage**: 9
**Attack Speed**: 1.6
**Durability**: 2031
**Max Stack Size**: 1

**Special Effect**:
- 攻撃命中時、25%の確率でMobの次の攻撃AIをスキップ
- クールダウン: なし（確率発動）

**Crafting**:
- 静止のコアの破片 x2
- 強化クロックストーン x1
- ダイヤモンドの剣 x1

### Armor

#### Mail of the Time Guardian (時の番人のメイル)

**Type**: Chestplate
**Armor**: 8
**Armor Toughness**: 3
**Durability**: 592
**Max Stack Size**: 1

**Special Effect**:
- 致命的なダメージ受けた際、20%の確率でダメージ前のHPと位置にロールバック
- クールダウン: 60秒（発動後）

**Crafting**:
- 静止のコアの破片 x3
- 強化クロックストーン x2
- ダイヤモンドのチェストプレート x1

#### Echoing Time Boots (時間の残響ブーツ)

**Type**: Boots
**Armor**: 3
**Armor Toughness**: 3
**Durability**: 481
**Max Stack Size**: 1

**Special Effect**:
- ダッシュ時、デコイエンティティ（残像）を召喚
- デコイ持続時間: 5秒
- デコイ性質: 敵のターゲットを引く、ダメージを受けない
- クールダウン: 15秒

**Crafting**:
- 静止のコアの破片 x2
- 強化クロックストーン x2
- ダイヤモンドのブーツ x1

### Ultimate Tools

#### Spatially Linked Pickaxe (空間連結ツルハシ)

**Type**: Pickaxe
**Mining Speed**: ダイヤモンドツルハシ相当
**Durability**: 1561
**Max Stack Size**: 1

**Special Effect**:
- ブロック破壊時、33%の確率でドロップアイテム生成処理を二重に実行
- 幸運エンチャントと効果が重複

**Crafting**:
- 静止のコアの破片 x2
- 強化クロックストーン x3
- ダイヤモンドのツルハシ x1

#### Unstable Pocket Watch (不安定な懐中時計)

**Type**: Utility Item
**Max Stack Size**: 1
**Cooldown**: 30秒

**Use**:
- 使用時、周囲のMobとプレイヤーの速度ステータス効果を瞬時に入れ替える
- 効果範囲: 半径10ブロック
- 効果持続: 10秒

**Crafting**:
- 静止のコアの破片 x3
- 不安定な砂時計 x1
- 強化クロックストーン x2

### Key Items

#### Eye of Chronos (クロノスの瞳)

**Type**: Quest Item / Ultimate Artifact
**Max Stack Size**: 1
**Obtained From**: 時間の暴君撃破

**Effect**:
- 所持しているだけで、クロノドーン内のMobの基本速度低下効果がさらに強化される（Slowness IV → Slowness V）
- クロノドーンの時間安定化の象徴

#### Key to Master Clock (マスタークロックへの鍵)

**Type**: Key Item
**Max Stack Size**: 1
**Obtained From**: 時の番人撃破

**Use**:
- マスタークロック入口の扉を開く

#### Ancient Gear (古代の歯車)

**Type**: Quest Item / Progressive Unlock Item
**Max Stack Size**: 3
**Obtained From**: マスタークロック中層の部屋（チェスト/アイテムフレーム）

**Use**:
- 3つ収集するとマスタークロックのボス部屋扉が開く
- 段階的解放システムの一部

**Mechanics**:
- プレイヤーのインベントリ内で3つ所持を検出
- BlockEventHandler.javaでAncient Gearsカウント
- 条件達成時、ボス部屋前の扉を開放

## Entities

### Custom Mobs

#### Time Guardian (時の番人)

**Type**: Mini-Boss Entity
**Health**: 200 (ハート100個)
**Armor**: 10
**Attack Damage**: 10 (5ハート - 通常攻撃)
**AoE Attack Damage**: 6 (3ハート - Phase 2範囲攻撃)
**Attack Speed**: 0.5秒間隔
**Movement Speed**: 0.2（Slowness IV適用前の基本速度）

**AI Patterns**:
- フェーズ1（HP 100%～50%）: 近接攻撃中心
- フェーズ2（HP 50%～0%）: テレポート攻撃 + 範囲攻撃

**Drops**:
- マスタークロックへの鍵（100%）
- 強化クロックストーン x3-5（100%）

**Special Mechanics**:
- Slowness IV効果を受ける（ディメンション全体の効果）
- 撃破後、反転共鳴を誘発（30秒間）

#### Time Tyrant (時間の暴君)

**Type**: Boss Entity
**Health**: 500 (ハート250個)
**Armor**: 15
**Attack Damage**: 18
**Attack Speed**: 0.4秒間隔
**Movement Speed**: 0.25（Slowness IV適用前の基本速度）

**AI Patterns**:
- フェーズ1（HP 100%～66%）: 近接攻撃 + 時間停止攻撃（プレイヤーにSlowness V、3秒間）
- フェーズ2（HP 66%～33%）: テレポート + 時間加速（自身にSpeed II、5秒間）
- フェーズ3（HP 33%～0%）: 範囲攻撃 + 時間逆行（自身のHPを10%回復、1回のみ）

**Drops**:
- 静止のコアの破片 x5-8（100%）
- クロノスの瞳 x1（100%）
- 強化クロックストーン x8-12（100%）

**Special Mechanics**:
- Slowness IV効果を受ける（ディメンション全体の効果）
- 撃破後、静止のコアが破壊され、ディメンションが安定化
- 撃破後、反転共鳴を誘発（60秒間）

#### Decoy Entity (残像エンティティ)

**Type**: Temporary Entity
**Health**: 1 (即座に消滅する、ダメージは受けない）
**Lifespan**: 5秒
**AI**: 移動しない、プレイヤーの位置に召喚

**Purpose**:
- 時間の残響ブーツのデコイ効果
- 敵Mobのターゲットを引く

## Structures

### Ancient Ruins (古代遺跡)

**Type**: World Generation Structure
**Location**: オーバーワールド（地上）
**Generation Frequency**: 稀（チャンクあたり0.1%）

**Contents**:
- クロックストーン鉱石 x10-20
- 時の砂時計の設計図（Book）x1
- チェスト（ランダムルートテーブル）

### Forgotten Library (忘れられた図書館)

**Type**: World Generation Structure
**Location**: クロノドーン（地上）
**Generation Frequency**: 稀（チャンクあたり0.5%）

**Contents**:
- ポータル安定化装置の設計図（Book）x1
- チェスト（ランダムルートテーブル）
- 本棚（装飾）

### Desert Clock Tower (砂漠の時計塔)

**Type**: World Generation Structure
**Location**: クロノドーン（砂漠バイオーム）
**Generation Frequency**: 稀（チャンクあたり0.3%）

**Contents**:
- 強化クロックストーン x5-10
- チェスト（ランダムルートテーブル）

### Master Clock (マスタークロック)

**Type**: World Generation Structure (Underground Palace with Jigsaw System)
**Location**: クロノドーン（ワールドスポーン近く、concentric_rings placement）
**Generation Frequency**: 1つのみ（ディメンション全体）
**Design Document**: `specs/chrono-dawn-mod/master-clock-design.md`

**Placement Configuration**:
- `type`: `minecraft:concentric_rings`
- `distance`: 80 chunks（ワールドスポーンから80-100チャンク範囲）
- `spread`: 20 chunks
- `count`: 1
- `preferred_biomes`: `#chronodawn:has_master_clock`

**Structure Composition**:

1. **地上部分（Entrance）** - 15x10x15
   - 小規模な古代神殿風の入口
   - Key to Master Clock使用で扉開放
   - 地下への階段

2. **地下迷宮（Main Dungeon）** - 41x50x41
   - Jigsawシステムによるランダム部屋配置
   - 8種類の部屋バリアント（各15x8x15～21x8x21）:
     - room_trap_arrows: 矢トラップ + Reversing Time Sandstone
     - room_spawner: モブスポーナー（Skeleton/Zombie）
     - room_maze: 迷路 + Reversing Time Sandstone walls
     - room_puzzle_redstone: レッドストーンパズル
     - room_lava: 溶岩トラップ + パルクール
     - room_time_puzzle: 時間逆行パズル（扉が自動で閉じる）
     - room_guardian_arena: エリート敵戦闘
     - room_rest: 休憩部屋（ベッド配置可能）
   - 各部屋にAncient Gears配置（3つ収集でボス部屋開放）

3. **ボス部屋（Boss Room）** - 35x20x35
   - Time Tyrant spawn位置
   - 戦闘用の広いホール
   - 報酬チェスト（撃破後出現）

**Progressive Unlock System**:
- **Phase 1**: Key to Master Clock → 入口扉開放
- **Phase 2**: Ancient Gears x3収集 → ボス部屋扉開放
- **Phase 3**: Time Tyrant撃破 → 報酬獲得

**Contents**:
- Ancient Gears x3（中層の部屋に配置、チェスト/アイテムフレーム）
- 時間の暴君（ボス、ボス部屋中央）
- 静止のコア（破壊可能なブロック、撃破時に自動破壊）
- ランダムルートチェスト（各部屋）
- 報酬チェスト（Eye of Chronos, Fragment of Stasis Core）

**Special Features**:
- Reversing Time Sandstone使用（トラップ、迷路、パズル）
- Jigsawによる毎回異なるレイアウト
- 段階的解放（探索→収集→ボス戦）

## Game Mechanics

### Time Distortion Effects (時間の歪み効果)

**Type**: Dimension-wide Effect
**Implementation**: イベントハンドラー（EntityTickEvent）

**Effect Application**:
- クロノドーン内の全カスタムMobにSlowness IVを付与
- プレイヤーは影響を受けない
- クロノスの瞳獲得後、Slowness V に強化

### Reversed Resonance (反転共鳴)

**Type**: Triggered Event
**Triggers**:
1. 不安定な砂時計をクラフト
2. 時の番人を撃破
3. 時間の暴君を撃破

**Effect**:
- プレイヤーにSlowness IV（30秒～60秒間、トリガーにより異なる）
- 周囲のMob（半径20ブロック）にSpeed II（同じ期間）

**Warning**:
- 発動前に画面に警告メッセージを表示

### Respawn Logic (リスポーン処理)

**Type**: Minecraft Standard Behavior (no custom implementation)
**Implementation**: None (uses vanilla Minecraft respawn mechanics)

**Design Philosophy**: Similar to End dimension behavior
- Chrono Dawn respawn follows Minecraft's standard respawn mechanics
- Players respawn at their set bed/respawn anchor, or world spawn if none set
- Portal Stabilizer does NOT affect respawn location
- Portal Stabilizer only makes portal bidirectional (like End return portal)
- Players can escape Chrono Dawn by breaking bed and dying (same as End)

**Rules**:
1. ベッド/リスポーンアンカーを設置済み: その場所にリスポーン（通常のMinecraft動作）
2. ベッド/リスポーンアンカー未設置: オーバーワールドのワールドスポーン地点にリスポーン（通常のMinecraft動作）
3. ポータル安定化装置の有無: リスポーン位置には**影響しない**

**Escape Mechanic**:
- プレイヤーはクロノドーンでベッドを設置して破壊し、死亡することでオーバーワールドに脱出可能
- この仕様により、ポータルが一方通行でも過度に難しくならない
- エンドディメンションと同じバランス設計

## Data Persistence

### World Saved Data

**Portal Registry**:
- `portal_id`: ポータルのユニークID
- `dimension`: ポータルが存在するディメンション
- `position`: ポータルの座標
- `state`: ポータルの状態（未起動/起動済み/機能停止/安定化）
- `linked_portal_id`: リンク先のポータルID（双方向転送用）

**Player Progress**:
- `has_chronos_eye`: クロノスの瞳を獲得済みか
- `stabilized_portals`: 安定化済みのポータルIDリスト
- `defeated_bosses`: 撃破済みのボスリスト

**Dimension State**:
- `is_stabilized`: クロノドーンが安定化されているか（時間の暴君撃破後）
- `time_distortion_level`: 時間の歪みレベル（Slowness IVまたはV）

## Validation Rules

### Item Crafting

- すべてのレシピはJSONファイルで定義（`data/chronodawn/recipes/`）
- 不安定な砂時計のクラフトはカスタムイベントをトリガー

### Boss Spawning

- 時の番人: クロノドーン内でランダムスポーン（1ワールドにつき最大3体）
- 時間の暴君: マスタークロック最深部に1体のみ（撃破後リスポーンしない）

### Portal Validation

- ポータルフレームは完全な矩形（4x5以上、23x23以下）
- フレームの角は必ずクロックストーンブロック
- ポータル内部は空気ブロックのみ

## Edge Cases

### Multiplayer Considerations

- ポータル状態はグローバル（全プレイヤー共有）
- ボス撃破状態はグローバル（1度撃破されたら全プレイヤーに影響）
- クロノスの瞳の効果は所持者のみ（他プレイヤーには影響しない）
- 反転共鳴は発動したプレイヤーの周囲のみ

### Performance Optimization

- 時間の歪み効果はTick処理最適化（毎Tickではなく、5Tick毎にチェック）
- ポータルレジストリはキャッシュ（頻繁なディスクI/Oを避ける）
- ボスAIは状態機械パターンで実装（無駄な計算を削減）
