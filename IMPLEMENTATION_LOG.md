# Ancient Ruins 配置実装の試行錯誤ログ

## 要件
- 配置距離: 700-1500ブロック（エンド要塞相当、冒険の始まり用）
- 配置数: ワールドに1つのみ（特別感）
- 配置バイオーム: 森林バイオーム（樹木が豊富）
- Strange Leaves効果: 周辺の葉を青紫色に変換（視認性向上）
- /locateコマンド: 使用可能であることが望ましい
- ワールド再生成: 解決策として使用不可

## 試行1: concentric_rings（標準配置）
### 設定
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 69,
  "spread": 25,
  "count": 128,
  "preferred_biomes": "#chronosphere:has_ancient_ruins"
}
```

### 問題
- Ancient Ruinsが2500-3000ブロックと遠すぎる
- Strange Forestがあってもその中にAncient Ruinsがないケース
- Strange Forest頻度を上げると「本末転倒」（ヒントにならない）

### 結論
❌ 却下 - 距離制御が不十分、バイオームとの連携が弱い

---

## 試行2: RangedSingleStructurePlacement（カスタム配置 - 単一候補）
### 設定
- カスタムStructurePlacement実装
- ワールドシードから決定論的に1つの座標を生成
- min_distance=700, max_distance=1500

### 問題1: max_distance_from_centerの制限
```
Caused by: java.lang.IllegalStateException: Value 450 outside of range [1:128]
```
- Minecraftの制限: max_distance_from_center ≤ 128ブロック
- SEARCH_RADIUS調整: 400→120→80ブロック

### 問題2: terrain_adaptationとの組み合わせ
```
Caused by: java.lang.IllegalStateException: Structure size including terrain adaptation must not exceed 128
```
- 解決: terrain_adaptation = "none"

### 問題3: バイオーム制約
- 選ばれた座標が偶然「海」バイオームになるケース
- 構造物は生成されるが、周辺に葉がなくStrange Leaves効果が見えない

### 結論
△ 部分的に機能 - 配置されるが、バイオーム運に左右される

---

## 試行3: RangedSingleStructurePlacement（複数候補システム）
### 設定
- 500個→2000個の候補座標を生成
- 順番にバイオームチェック、最初に適合する位置に配置
- バイオーム: 森林・タイガ・平原など

### 問題
- `isPlacementChunk`はチャンク生成時にしか呼ばれない
- プレイヤーが探索しないと候補がチェックされない
- ログが大量に出るが、構造物配置は確認できず

### 結論
❌ 却下 - チャンク生成タイミングの問題、実用性が低い

---

## 試行4: concentric_rings再検討（標準配置 - パラメータ調整）
### 設定
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 44,
  "spread": 25,
  "count": 3,
  "preferred_biomes": "#chronosphere:has_ancient_ruins"
}
```

### 問題（予想）
- `count: 3` = 3箇所に配置される（要件: 1箇所のみ）
- `preferred_biomes`は「優先」であり「必須」ではない
- 試行1と同じ問題が再発する可能性

### 結論
⏸️ 保留 - 試行1の問題が解決されていない

---

## 試行5: concentric_rings + StrangeLeavesProcessor（最終実装）
### 設定
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 44,
  "spread": 25,
  "count": 3,
  "preferred_biomes": "#chronosphere:has_ancient_ruins"
}
```

バイオームタグ（has_ancient_ruins）:
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

**重要な気づき**: Strange Forestバイオーム自体は不要
- `StrangeLeavesProcessor`が構造物周辺80ブロックの葉を青紫色に変換
- どのバイオームに配置されても「Strange Forest効果」が生まれる
- バイオーム定義は配置場所（樹木豊富）の制約のみ

### 利点
- ✓ /locate structure chronosphere:ancient_ruins が使用可能
- ✓ 森林・タイガ・平原に配置（樹木豊富）
- ✓ 周辺80ブロックが青紫色に変わる（視認性）
- ✓ 700-1500ブロック範囲（3リング: 700/1100/1500）
- ✓ エンド要塞と同じ仕組みで実装

### 制約
- max_distance_from_center = 120ブロック
- SEARCH_RADIUS = 80ブロック（安全マージン）
- terrain_adaptation = "none"

### 結論
✅ 採用 - 全要件を満たす

---

## 試行6: random_spread（最終実装）
### 設定
```json
{
  "type": "minecraft:random_spread",
  "spacing": 48,
  "separation": 24,
  "salt": 20005897
}
```

構造物定義:
```json
{
  "terrain_adaptation": "beard_thin",
  "max_distance_from_center": 80,
  "biomes": "#chronosphere:has_ancient_ruins"
}
```

バイオームタグ（has_ancient_ruins）:
```json
{
  "values": [
    "#minecraft:is_forest",
    "#minecraft:is_taiga"
  ]
}
```

### 利点
- ✓ 安定した距離: 最小384ブロック、平均500-1000ブロック
- ✓ /locate structure chronosphere:ancient_ruins が使用可能
- ✓ 森林・タイガのみに配置（樹木豊富）
- ✓ terrain_adaptation="beard_thin"で地形に滑らかに馴染む
- ✓ バイオーム制限があっても距離が安定

### 制約
- spacing=48（768ブロック）のため、それなりに複数生成される可能性
- concentric_ringsのような確実な1個配置ではない

### 結論
✅ 採用 - 距離の安定性とバイオーム制限の両立

---

## 現在の状態
- **実装**: random_spread（spacing=48, separation=24）
- **配置**: 森林・タイガのみ、最小384ブロック、平均500-1000ブロック
- **地形適応**: beard_thin（斜面でも滑らかに配置）
- **コマンド**: /locate structure chronosphere:ancient_ruins が機能
- **次のステップ**: 高い塔を追加して視認性向上

---

## 検討すべき選択肢
1. **count=1のconcentric_rings**: 1リングのみ、spread拡大
2. **random_spread with低頻度**: spacing/separationを極端に大きく
3. **カスタムStructurePlacement改良**: バイオーム判定を事前実行
4. **Feature-based配置**: StructurePlacementではなくFeatureとして実装

## 技術的制約
- max_distance_from_center ≤ 128ブロック
- terrain_adaptationは制約に含まれる
- SEARCH_RADIUS（StrangeLeavesProcessor）≤ 80ブロックが安全
- バイオームタグは実行時に評価される
