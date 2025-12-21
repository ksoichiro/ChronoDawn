# User Story 3 Worktree Analysis

**Created**: 2025-11-02
**Purpose**: US2完了後のUS3並列実装準備

## タスク概要

**Total Tasks**: 49タスク (T123-T171e)

### カテゴリ別タスク数

| カテゴリ | タスク範囲 | 数 | 並列可能 |
|---------|-----------|---|---------|
| Tests | T123-T127 | 5 | ✅ 全て[P] |
| Master Clock構造物 | T128-T133 | 6 | ⚠️ T133のみ依存 |
| Time Tyrant (Boss) | T134-T140 | 7 | ❌ 連続実行 |
| Boss Materials | T141-T147 | 7 | ⚠️ T147のみ依存 |
| Chronoblade (Weapon) | T148-T152 | 5 | ⚠️ T152のみ依存 |
| Time Guardian's Mail | T153-T157 | 5 | ⚠️ T157のみ依存 |
| Echoing Time Boots | T158-T164 | 7 | ⚠️ T162-164依存 |
| Ultimate Pickaxe | T165-T166 | 2 | ⚠️ T166依存 |
| Unstable Pocket Watch | T167-T171 | 5 | ⚠️ T171のみ依存 |
| Localization & Tab | T171a-c | 3 | ❌ 統合後一括 |
| Recipe Review | T171d-e | 2 | ✅ 並列可能 |

## 依存関係マップ

### Time Tyrant Boss（ブロッキング）
```
T134-T140: Time Tyrantエンティティ
  ↓ (loot table依存)
T141-T147: Boss Materials (Fragment, Eye of Chronos)
  ↓ (crafting材料依存)
T148-T171: 全Artifact Items
```

**結論**: Boss Materials (T141-T147) はTime Tyrant (T134-T140) 完了まで開始不可

### 構造物（独立）
```
T128-T133: Master Clock構造物
  - T128-T132 [P]: 並列実行可能
  - T133: BlockEventHandler変更（鍵ドア）
```

### Artifact Items（相互独立）
```
T148-T152: Chronoblade
T153-T157: Time Guardian's Mail
T158-T164: Echoing Time Boots (+ Decoy Entity)
T165-T166: Ultimate Pickaxe
T167-T171: Unstable Pocket Watch
```

**結論**: 各Artifactは相互に独立（ModItems.java以外競合なし）

## 共通ファイル競合

### 高頻度変更ファイル

**ModItems.java** (7箇所):
- T142: FRAGMENT_OF_STASIS_CORE
- T145: EYE_OF_CHRONOS
- T149: CHRONOBLADE
- T154: TIME_GUARDIAN_MAIL
- T159: ECHOING_TIME_BOOTS
- T168: UNSTABLE_POCKET_WATCH

**ModEntities.java** (2箇所):
- T135: TIME_TYRANT
- T163: DECOY_ENTITY

**EntityEventHandler.java** (3箇所):
- T138: Stasis Core destruction
- T139: Reversed resonance (60s)
- T147: Eye of Chronos effect (Slowness V)

**BlockEventHandler.java** (1箇所):
- T133: Key-based door opening

### 最終統合ファイル

- lang/en_us.json, lang/ja_jp.json (T171a-b)
- ModCreativeTabs.java (T171c)

## Worktree分割案（3つのアプローチ）

### オプション1: 最小競合アプローチ（4 worktrees）

```
worktree-1 (us3-structures): Master Clock構造物
  T128-T133 (6タスク)

worktree-2 (us3-boss): Time Tyrant + Boss Materials
  T134-T147 (14タスク)
  ⚠️ ブロッキング: 他worktreeはT141-T147完了後に開始

worktree-3 (us3-weapons): 武器系 Artifacts
  T148-T152: Chronoblade (5タスク)
  T165-T166: Ultimate Pickaxe (2タスク)

worktree-4 (us3-armor-utility): 防具・ユーティリティ系
  T153-T157: Time Guardian's Mail (5タスク)
  T158-T164: Echoing Time Boots (7タスク)
  T167-T171: Unstable Pocket Watch (5タスク)
```

**利点**: 機能別にクリアに分割、ModItems.java競合が分散
**欠点**: worktree-2完了まで他が開始できない

---

### オプション2: 完全並列アプローチ（6 worktrees）

```
worktree-1 (us3-structures): Master Clock
  T128-T133 (6タスク)

worktree-2 (us3-boss): Time Tyrant ONLY
  T134-T140 (7タスク)

worktree-3 (us3-boss-materials): Boss Materials
  T141-T147 (7タスク)
  🔒 依存: worktree-2完了後に開始

worktree-4 (us3-chronoblade): Chronoblade
  T148-T152 (5タスク)
  🔒 依存: worktree-3完了後に開始

worktree-5 (us3-armor): Time Guardian's Mail + Echoing Time Boots
  T153-T164 (12タスク)
  🔒 依存: worktree-3完了後に開始

worktree-6 (us3-utility): Ultimate Pickaxe + Unstable Pocket Watch
  T165-T171 (7タスク)
  🔒 依存: worktree-3完了後に開始
```

**利点**: 最大の並列性（worktree-3完了後）
**欠点**: worktree管理が複雑、順次実行部分が多い

---

### オプション3: 2段階アプローチ（推奨）

#### 第1段階（即座開始可能）
```
worktree-1 (us3-phase1-structures): Master Clock
  T128-T133 (6タスク)

worktree-2 (us3-phase1-boss): Time Tyrant + Boss Materials
  T134-T147 (14タスク)
```

#### 第2段階（worktree-2完了後）
```
worktree-3 (us3-phase2-weapons): Chronoblade + Ultimate Pickaxe
  T148-T152, T165-T166 (7タスク)

worktree-4 (us3-phase2-armor): Time Guardian's Mail + Echoing Time Boots
  T153-T164 (12タスク)

worktree-5 (us3-phase2-utility): Unstable Pocket Watch
  T167-T171 (5タスク)
```

**利点**: クリティカルパス（Boss）を優先、その後最大並列化
**欠点**: 2段階の調整が必要

---

## 推奨実装戦略

### ステップ1: 第1段階（US2完了直後）
1. worktree-1 (structures): 即座開始
2. worktree-2 (boss): 即座開始（優先）

### ステップ2: Boss完了確認
- worktree-2が**T141-T147完了**を確認

### ステップ3: 第2段階（最大並列化）
3. worktree-3 (weapons): 並列開始
4. worktree-4 (armor): 並列開始
5. worktree-5 (utility): 並列開始

### ステップ4: 統合
- 全worktreeマージ
- T171a-c: Localization & Creative Tab一括更新
- T171d-e: Recipe Material Review

---

## タスク実装順序（worktree内）

### worktree-2 (boss) の優先順序
```
優先度1: T134-T137 (Time Tyrant本体 + loot table)
優先度2: T138-T140 (特殊効果)
優先度3: T141-T146 (Boss Materials)
優先度4: T147 (Eye of Chronos effect)
```

**理由**: T141-T147完了が第2段階のブロッカー

---

## 共通ファイル統合戦略

### 各worktreeでの方針
- ModItems.java: **変更しない**（コメントのみ追加可）
- ModEntities.java: **変更しない**（コメントのみ追加可）
- EntityEventHandler.java: **変更しない**
- BlockEventHandler.java: **変更しない**
- lang: **変更しない**
- ModCreativeTabs: **変更しない**

### 統合後の一括更新
1. 全worktreeマージ完了
2. ModItems.javaに全アイテム一括登録
3. ModEntities.javaに全エンティティ一括登録
4. EventHandlerに全ロジック一括追加
5. Localization一括追加
6. Creative Tab一括更新

---

## US2との違い

| 観点 | US2 | US3 |
|-----|-----|-----|
| タスク数 | 34 | 49 |
| worktree数 | 3 | 5 (2段階) |
| ブロッキング依存 | なし | Boss Materials |
| 並列機会 | 3 worktree同時 | 1段階目2個→2段階目3個 |
| 統合複雑度 | 中 | 高 |

**US3の特徴**: Boss Materials完了まで大半のArtifactが開始不可

---

## 次のアクション

### US2完了時
1. オプション3（2段階アプローチ）を採用
2. 第1段階worktree作成:
   ```bash
   git worktree add -b us3-phase1-structures ../ChronoDawn-us3-structures
   git worktree add -b us3-phase1-boss ../ChronoDawn-us3-boss
   ```

### Boss Materials完了時
3. 第2段階worktree作成:
   ```bash
   git worktree add -b us3-phase2-weapons ../ChronoDawn-us3-weapons
   git worktree add -b us3-phase2-armor ../ChronoDawn-us3-armor
   git worktree add -b us3-phase2-utility ../ChronoDawn-us3-utility
   ```

---

## 見積もり

### 各worktreeの作業量
- worktree-1 (structures): 6タスク → 1-2日
- worktree-2 (boss): 14タスク → 3-4日（AIロジック含む）
- worktree-3 (weapons): 7タスク → 1-2日
- worktree-4 (armor): 12タスク → 2-3日（Decoy Entity含む）
- worktree-5 (utility): 5タスク → 1日

**合計**: 8-12日（並列実行で5-7日に短縮可能）

---

## リスク

### 高リスク
- **Boss AI実装** (T136): 3フェーズAI、最も複雑
- **Decoy Entity** (T162-T164): 新エンティティタイプ
- **Rollback Logic** (T157): 状態保存・復元ロジック

### 中リスク
- **Dimension Stabilizer** (T140): 新システム
- **Eye of Chronos effect** (T147): 既存システム変更

### 低リスク
- 構造物、テクスチャ、レシピ系タスク
