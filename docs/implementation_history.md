# Implementation History

このドキュメントは、Chrono Dawnモッドの実装過程で得られた技術的な知見や試行錯誤の記録を保管します。

---

## Ancient Ruins Structure Placement - 試行錯誤ログ

**実装日**: 2025年初期
**目的**: Ancient Ruinsの配置設定を最適化

### 要件
- 配置距離: 700-1500ブロック（エンド要塞相当、冒険の始まり用）
- 配置数: ワールドに1つのみ（特別感）
- 配置バイオーム: 森林バイオーム（樹木が豊富）
- Strange Leaves効果: 周辺の葉を青紫色に変換（視認性向上）
- /locateコマンド: 使用可能であることが望ましい
- ワールド再生成: 解決策として使用不可

### 試行1: concentric_rings（標準配置）

**設定:**
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 69,
  "spread": 25,
  "count": 128,
  "preferred_biomes": "#chronodawn:has_ancient_ruins"
}
```

**問題:**
- Ancient Ruinsが2500-3000ブロックと遠すぎる
- Strange Forestがあってもその中にAncient Ruinsがないケース
- Strange Forest頻度を上げると「本末転倒」（ヒントにならない）

**結論:** ❌ 却下 - 距離制御が不十分、バイオームとの連携が弱い

---

### 試行2: RangedSingleStructurePlacement（カスタム配置 - 単一候補）

**設定:**
- カスタムStructurePlacement実装
- ワールドシードから決定論的に1つの座標を生成
- min_distance=700, max_distance=1500

**問題1: max_distance_from_centerの制限**
```
Caused by: java.lang.IllegalStateException: Value 450 outside of range [1:128]
```
- Minecraftの制限: max_distance_from_center ≤ 128ブロック
- SEARCH_RADIUS調整: 400→120→80ブロック

**問題2: terrain_adaptationとの組み合わせ**
```
Caused by: java.lang.IllegalStateException: Structure size including terrain adaptation must not exceed 128
```
- 解決: terrain_adaptation = "none"

**問題3: バイオーム制約**
- 選ばれた座標が偶然「海」バイオームになるケース
- 構造物は生成されるが、周辺に葉がなくStrange Leaves効果が見えない

**結論:** △ 部分的に機能 - 配置されるが、バイオーム運に左右される

---

### 試行3: RangedSingleStructurePlacement（複数候補システム）

**設定:**
- 500個→2000個の候補座標を生成
- 順番にバイオームチェック、最初に適合する位置に配置
- バイオーム: 森林・タイガ・平原など

**問題:**
- `isPlacementChunk`はチャンク生成時にしか呼ばれない
- プレイヤーが探索しないと候補がチェックされない
- ログが大量に出るが、構造物配置は確認できず

**結論:** ❌ 却下 - チャンク生成タイミングの問題、実用性が低い

---

### 試行4: concentric_rings再検討（標準配置 - パラメータ調整）

**設定:**
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 44,
  "spread": 25,
  "count": 3,
  "preferred_biomes": "#chronodawn:has_ancient_ruins"
}
```

**問題（予想）:**
- `count: 3` = 3箇所に配置される（要件: 1箇所のみ）
- `preferred_biomes`は「優先」であり「必須」ではない
- 試行1と同じ問題が再発する可能性

**結論:** ⏸️ 保留 - 試行1の問題が解決されていない

---

### 試行5: concentric_rings + StrangeLeavesProcessor（最終実装候補）

**設定:**
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 44,
  "spread": 25,
  "count": 3,
  "preferred_biomes": "#chronodawn:has_ancient_ruins"
}
```

**バイオームタグ（has_ancient_ruins）:**
```json
{
  "values": [
    "#minecraft:is_forest",
    "#minecraft:is_taiga",
    "minecraft:plains",
    "minecraft:sunflower_plains"
  ]
}
```

**重要な気づき: Strange Forestバイオーム自体は不要**
- `StrangeLeavesProcessor`が構造物周辺80ブロックの葉を青紫色に変換
- どのバイオームに配置されても「Strange Forest効果」が生まれる
- バイオーム定義は配置場所（樹木豊富）の制約のみ

**利点:**
- ✓ /locate structure chronodawn:ancient_ruins が使用可能
- ✓ 森林・タイガ・平原に配置（樹木豊富）
- ✓ 周辺80ブロックが青紫色に変わる（視認性）
- ✓ 700-1500ブロック範囲（3リング: 700/1100/1500）
- ✓ エンド要塞と同じ仕組みで実装

**制約:**
- max_distance_from_center = 120ブロック
- SEARCH_RADIUS = 80ブロック（安全マージン）
- terrain_adaptation = "none"

**結論:** ✅ 採用候補

---

### 試行6: random_spread（最終実装）

**設定:**
```json
{
  "type": "minecraft:random_spread",
  "spacing": 48,
  "separation": 24,
  "salt": 20005897
}
```

**構造物定義:**
```json
{
  "terrain_adaptation": "beard_thin",
  "max_distance_from_center": 80,
  "biomes": "#chronodawn:has_ancient_ruins"
}
```

**バイオームタグ（has_ancient_ruins）:**
```json
{
  "values": [
    "#minecraft:is_forest",
    "#minecraft:is_taiga"
  ]
}
```

**利点:**
- ✓ 安定した距離: 最小384ブロック、平均500-1000ブロック
- ✓ /locate structure chronodawn:ancient_ruins が使用可能
- ✓ 森林・タイガのみに配置（樹木豊富）
- ✓ terrain_adaptation="beard_thin"で地形に滑らかに馴染む
- ✓ バイオーム制限があっても距離が安定

**制約:**
- spacing=48（768ブロック）のため、それなりに複数生成される可能性
- concentric_ringsのような確実な1個配置ではない

**結論:** ✅ 採用 - 距離の安定性とバイオーム制限の両立

---

### 現在の状態
- **実装**: random_spread（spacing=48, separation=24）
- **配置**: 森林・タイガのみ、最小384ブロック、平均500-1000ブロック
- **地形適応**: beard_thin（斜面でも滑らかに配置）
- **コマンド**: /locate structure chronodawn:ancient_ruins が機能
- **次のステップ**: 高い塔を追加して視認性向上

---

### 技術的制約（重要）
- **max_distance_from_center ≤ 128ブロック** - Minecraftのハードコードされた制限
- **terrain_adaptationは制約に含まれる** - 構造物サイズ + terrain_adaptationの合計が128以下
- **SEARCH_RADIUS（StrangeLeavesProcessor）≤ 80ブロックが安全** - 128制限を考慮
- **バイオームタグは実行時に評価される** - 事前計算は不可

---

## Additional Bosses (T234-T238) - 実装記録

**実装日**: 2025-11-21～2025-11-22
**ブランチ**: T234-238-additional-bosses
**目的**: Time Tyrant戦の準備として4体のミニボスとChrono Aegisシステムを実装

### ✅ 完了した実装

#### 1. Four Mini-Boss Entities

**Chronos Warden (T234a-i):**
- **HP**: 180 (90 hearts)
- **メカニクス**: Stone Stance (ダメージ軽減), Ground Slam (ノックバックAoE)
- **ドロップ**: Guardian Stone (1-2) + Enhanced Clockstone (2-4)
- **ファイル**: ChronosWardenEntity.java, GuardianStoneItem.java, ChronosWardenRenderer.java
- **ビジュアル**: TimeGuardianModelをカスタムテクスチャで使用

**Clockwork Colossus (T235a-l):**
- **HP**: 200 (100 hearts)
- **メカニクス**: Gear Shot (遠距離), Overcharge (攻撃力強化), Repair Protocol (HP回復), Ground Slam
- **ドロップ**: Colossus Gear (1-2) + Enhanced Clockstone (2-4)
- **ファイル**: ClockworkColossusEntity.java, GearProjectileEntity.java, ColossusGearItem.java
- **ビジュアル**: TimeGuardianModelをカスタムテクスチャで使用

**Temporal Phantom (T236a-l):**
- **HP**: 150 (75 hearts)
- **メカニクス**: Phase Shift (30% 回避), Warp Bolt (遠距離魔法), Phantom Clone (召喚), Blink Strike
- **ドロップ**: Phantom Essence (1-2) + Enhanced Clockstone (2-4)
- **ファイル**: TemporalPhantomEntity.java, PhantomEssenceItem.java, TemporalPhantomRenderer.java
- **ビジュアル**: TimeGuardianModelをカスタムテクスチャで使用

**Entropy Keeper (T237a-m):**
- **HP**: 160 (80 hearts)
- **メカニクス**: Decay Aura (Wither I), Corrosion Touch (耐久値ダメージ), Temporal Rot (腐敗パッチ), Degradation (60秒ごとに攻撃力+2), Entropy Burst (一度きりの爆発)
- **ドロップ**: Entropy Core (1-2) + Enhanced Clockstone (2-4)
- **ファイル**: EntropyKeeperEntity.java, EntropyCoreItem.java, EntropyKeeperRenderer.java
- **ビジュアル**: TimeGuardianModelをカスタムテクスチャで使用

---

#### 2. Chrono Aegis System (T238a-l)

**Chrono Aegis Item:**
- **タイプ**: 消費アイテム（使い切り）
- **クラフト**: 4つのボスドロップを使用したシェイプレスレシピ
  ```
  Guardian Stone + Phantom Essence + Colossus Gear + Entropy Core → Chrono Aegis (1)
  ```
- **効果**: 10分間のバフ（12000ティック）
- **ビジュアル**: エピックレアリティ + エンチャント光
- **ファイル**: ChronoAegisItem.java, chrono_aegis.json (レシピ)

**Chrono Aegis Buff Effect:**
- **レジストリ**: ModEffects.CHRONO_AEGIS_BUFF
- **色**: ロイヤルブルー (0x4169E1)
- **アイコン**: mob_effect/chrono_aegis_buff.png
- **ファイル**: ChronoAegisEffect.java, ModEffects.java

**Time Tyrant統合 (T238g-j):**

1. **Time Stop Resistance** - Time Stopデバフを軽減: Slowness V → Slowness II
2. **Dimensional Anchor** - テレポート後3秒間、次のテレポートを防止
3. **Temporal Shield** - AoEダメージを50%軽減（12 → 6ダメージ）
4. **Time Reversal Disruption** - HP回復を軽減: 10% → 5% of max HP
5. **Clarity Auto-Cleanse** - 2秒ごとにSlowness/Weakness/Mining Fatigueを除去

**マルチプレイヤー保護:**
- デバフフラグにより複数のChrono Aegisプレイヤーからのスタックを防止
- `hasNearbyChronoAegisPlayer()`で32ブロック範囲をチェック
- 状態はNBTに保存してセーブ/ロードに対応

**Clarity実装 (T240):**
- EntityEventHandlerでイベントベースシステムを使用
- `MobEffect.applyEffectTick()`ではなく`PLAYER_POST`ティックイベントを使用
- 40ティックごと（2秒）にクレンジング
- NBTセーブ中のConcurrentModificationExceptionを回避

---

### 🔧 技術的な決定事項

#### 1. モデル再利用戦略
**決定**: すべてのボスがTimeGuardianModelを異なるテクスチャで使用
**理由**: 迅速な開発、一貫したサイズ/ヒットボックス、後で置き換え可能
**将来**: 各ボスのカスタムBlockbenchモデルを作成可能（T242）

#### 2. Holder<MobEffect>変換
**問題**: Minecraft 1.21.1は`Holder<MobEffect>`をエフェクトチェックに要求
**解決**: `BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)`を使用
**適用箇所**: ChronoAegisItem.java, TimeTyrantEntity.java

#### 3. エフェクト適用の安全性
**問題**: `applyEffectTick()`中の`removeEffect()`がNBTセーブクラッシュを引き起こす
**解決**: Clarity機能を無効化、イベントベース除去を実装
**教訓**: applyEffectTick()内でイテレーション中にエフェクトリストを変更しない

#### 4. 命名の一貫性修正
**問題**: Temporal PhantomがレジストリIDに`_boss`接尾辞を持っていた
**修正**: `temporal_phantom_boss` → `temporal_phantom`にリネーム
**適用**: ModEntities.java, ルートテーブルファイル名, 翻訳

---

### 📊 実装統計

- **作成ファイル**: 30以上
- **変更ファイル**: 15以上
- **コード行数**: ~3000以上
- **コミット**: 6
- **ビルド状態**: ✅ 成功
- **テスト状態**: ⚠️ 手動テストのみ（自動テスト保留中）

---

### 🎮 ゲームバランス

#### ボス難易度比較
| ボス | HP | 攻撃力 | 防御力 | 速度 | 難易度 |
|------|-----|--------|-------|-------|------------|
| Chronos Warden | 180 | 9 | 12 | 0.15 | 中（防御型） |
| Clockwork Colossus | 200 | 12 | 8 | 0.18 | 中～高（バランス型） |
| Temporal Phantom | 150 | 8 | 5 | 0.25 | 中（回避型） |
| Entropy Keeper | 160 | 10 (最大16) | 6 | 0.20 | 高（DoT/バースト） |

#### Chrono Aegisの影響
- **なし**: Time Tyrant戦は非常に困難（Slowness V、頻繁なテレポート、高ダメージ）
- **あり**: 戦闘が管理可能に（Slowness II、制限されたテレポート、軽減ダメージ）
- **設計目標**: Chrono Aegisなしでも達成可能、ありで大幅に容易化

---

### 📝 既知の問題

1. **ボスの視覚的類似性**
   - 原因: すべてのボスがTimeGuardianModelを使用
   - 影響: 視覚的バリエーションが少ない
   - 修正: カスタムモデルを作成（将来の作業）

2. **ボススポーン構造未実装**
   - 原因: まだ実装されていない
   - 影響: ボスが自然にスポーンできない
   - 修正: T239で実装

---

### 🔗 関連ファイル

**Entity Classes:**
- `common/src/main/java/com/chronodawn/entities/bosses/ChronosWardenEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/ClockworkColossusEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/TemporalPhantomEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/EntropyKeeperEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/TimeTyrantEntity.java` (変更)

**Item Classes:**
- `common/src/main/java/com/chronodawn/items/GuardianStoneItem.java`
- `common/src/main/java/com/chronodawn/items/ColossusGearItem.java`
- `common/src/main/java/com/chronodawn/items/PhantomEssenceItem.java`
- `common/src/main/java/com/chronodawn/items/EntropyCoreItem.java`
- `common/src/main/java/com/chronodawn/items/ChronoAegisItem.java`

**Registry Classes:**
- `common/src/main/java/com/chronodawn/registry/ModEntities.java`
- `common/src/main/java/com/chronodawn/registry/ModItems.java`
- `common/src/main/java/com/chronodawn/registry/ModEffects.java` (新規)

**Effect Classes:**
- `common/src/main/java/com/chronodawn/effects/ChronoAegisEffect.java` (新規)

**Event Handlers:**
- `common/src/main/java/com/chronodawn/events/EntityEventHandler.java` (Clarity機能用に変更)

---

## Desert Clock Tower Implementation

**実装日**: 2025-11-02
**タスク**: T093-T095, T099
**目的**: Enhanced Clockstoneを入手できる構造物を追加

### 完了したタスク

**T093: Desert Clock Tower structure NBT and JSON configuration**
- テンプレートプールJSON作成: `/common/src/main/resources/data/chronodawn/worldgen/template_pool/desert_clock_tower/start_pool.json`
- プロセッサリストJSON作成: `/common/src/main/resources/data/chronodawn/worldgen/processor_list/desert_clock_tower_loot.json`
- プレースホルダーNBT作成: `/common/src/main/resources/data/chronodawn/structure/desert_clock_tower.nbt`
  - ⚠️ **重要**: 現在ancient_ruins.nbtをプレースホルダーとして使用
  - **TODO**: 実際の塔構造をゲーム内で構造ブロックを使用して作成

**T094: Desert Clock Tower structure feature**
- 構造物JSON作成: `/common/src/main/resources/data/chronodawn/worldgen/structure/desert_clock_tower.json`
- 設定:
  - Type: `minecraft:jigsaw`
  - Biome: `chronodawn:chronodawn_plains`
  - Terrain adaptation: `beard_thin`
  - Start height: `absolute: 0`

**T095: Desert Clock Tower structure set**
- 構造物セットJSON作成: `/common/src/main/resources/data/chronodawn/worldgen/structure_set/desert_clock_tower.json`
- 配置設定:
  - Type: `minecraft:random_spread`
  - Salt: `1663542342`
  - Spacing: `20` (ancient_ruinsの16よりレア)
  - Separation: `8`

**T099: Enhanced Clockstone loot configuration**
- ルートテーブル作成: `/common/src/main/resources/data/chronodawn/loot_table/chests/desert_clock_tower.json`
- ルート内容:
  - **Pool 1** (保証): Enhanced Clockstone x4-8
  - **Pool 2** (保証): Clockstone x8-16
  - **Pool 3** (2-4個ランダム):
    - Iron Ingot x2-6 (weight: 10)
    - Gold Ingot x2-5 (weight: 8)
    - Diamond x1-3 (weight: 5)
    - Fruit of Time x4-8 (weight: 12)
    - Torch x8-16 (weight: 10)

### 設計ノート

- **配置戦略**: ancient_ruins（spacing 16）よりレア（spacing 20）でEnhanced Clockstoneをより価値あるものに
- **ルートバランス**: 保証されたEnhanced Clockstoneドロップ（4-8）で時間操作アイテム用の十分な素材を提供
- **バイオーム制限**: 現在chronodawn_plainsのみ - 将来他のchronodawnバイオームに拡大可能
- **構造物適応**: `beard_thin`を使用して自然な地形ブレンディング

### 次のステップ（TODO）

**高優先度:**
1. 実際のNBT構造を作成（T095a）
   - Minecraftのゲーム内構造ブロックを使用
   - 推奨仕様:
     - サイズ: 15x30x15ブロック（高い塔デザイン）
     - 素材: 砂岩ベースのブロック（滑らかな砂岩、カット砂岩、彫刻砂岩）
     - 特徴:
       - 時計塔の美学（時計盤装飾付きの垂直塔）
       - 複数階/レベル
       - 戦略的位置にチェスト配置
       - 装飾要素（階段、スラブ、フェンスなどのディテール）
     - 砂漠テーマで「Desert Clock Tower」の名前に合致

**中優先度:**
2. ゲーム内で構造物生成をテスト
3. ビジュアル検証

**オプション改善:**
4. バリアントの追加を検討（将来）

---

*最終更新: 2025-12-15*
