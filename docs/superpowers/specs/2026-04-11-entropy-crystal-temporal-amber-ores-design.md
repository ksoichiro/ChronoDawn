# Entropy Crystal Ore & Temporal Amber Ore Design

## Overview

Enhanced Clockstone と横並びの Tier 2 分岐として、特化型の鉱石・装備を2系統追加する。プレイヤーは Tier 2 時点で「Enhanced Clockstone(バランス型+時間歪み免疫)」「Entropy Crystal Sword(武器特化・継続ダメージ)」「Temporal Amber Armor(防具特化・耐久自動修復)」の三択から選択・混装できる。

- **Entropy Crystal**: 時が物を削るエネルギー。剣のみ。命中時に DoT 状態異常を付与。
- **Temporal Amber**: 時が物を保つ樹脂。防具4ピースのみ。フルセット装備+専用粉消費で戦闘外自動修復。

既存の Clockstone 系 / Enhanced Clockstone 系と並列のルートで、前提条件なし。Q&A で合意した設計方針:

- Enhanced Clockstoneと並列(Clockstoneから直接分岐、前提条件なし)
- 2種構成(武器特化 + 防具特化)
- 純粋特化(Entropy = 剣のみ、Temporal Amber = 防具4ピースのみ)
- 侵食(Entropy) / 保存(Preservation)のテーマ対比

## Goals

- プレイヤーに「攻めに振るか、守りに振るか、中庸(Enhanced Clockstone)を選ぶか」という意味のある三択を提供する
- Enhanced Clockstone の存在意義(フル装備+時間歪み免疫)を損なわない
- 修繕エンチャントの存在意義を損なわない(自動修復は粉消費=XP投資の代わりに鉱石投資)
- 既存の ChronoDawn 命名・実装パターンと整合させる
- 全サポートバージョン(1.20.1 / 1.21.1 〜 1.21.11)で一貫した体験を提供

## Non-Goals

- Entropy 側の斧・ピッケル・防具の追加(純粋特化)
- Temporal Amber 側のツール・盾の追加(純粋特化)
- 専用バイオーム・専用構造物の追加
- Temporal Deepslate 系鉱石の追加(既存深層鉱石と同じく将来別タスク。Temporal Amber Ore は初期実装では `temporal_stone` 母岩)
- GameTest の追加(手動検証のみ)
- Entropy 剣と既存エンチャント(Sharpness 等)の特殊相互作用

## Naming

| 項目 | 侵食側 | 保存側 |
|---|---|---|
| 鉱石ブロック | `entropy_crystal_ore` | `temporal_amber_ore` |
| 原材料 | `entropy_crystal` | `raw_temporal_amber` |
| 装備 | `entropy_crystal_sword` | `temporal_amber_helmet/chestplate/leggings/boots` |
| 副産物(修復燃料) | — | `temporal_amber_dust` |

日本語翻訳:
- Entropy Crystal Ore → エントロピークリスタル鉱石
- Entropy Crystal → エントロピークリスタル
- Entropy Crystal Sword → エントロピークリスタルソード
- Temporal Amber Ore → 時の琥珀鉱石
- Raw Temporal Amber → 粗時琥珀
- Temporal Amber Helmet/Chestplate/Leggings/Boots → 時琥珀の兜/胸当て/レギンス/ブーツ
- Temporal Amber Dust → 時琥珀の粉

既存命名 `Time Crystal` / `Temporal Stone` / `Temporal Iron` 等と整合する。

## Ore Generation

### 配置場所

両鉱石とも ChronoDawn ディメンション内、母岩は `chronodawn:temporal_stone`(既存ChronoDawn鉱石と同じパターン)。

| | Entropy Crystal Ore | Temporal Amber Ore |
|---|---|---|
| Y帯(仮) | 40 〜 100(浅層) | −30 〜 20(深層) |
| 鉱脈サイズ(仮) | 3〜5 | 2〜4 |
| チャンクあたり出現数(仮) | 4回試行 | 4回試行 |
| 希少度 | 中〜低(Clockstoneの入手動機を損なわない) | 中〜低(同) |
| バイオーム差別化 | なし(全バイオーム均一) | なし |

Y帯・出現数は実装時の調整前提。

### テーマ的意味づけ

- **浅層 = Entropy**: 風化・露出したエリアで発見、地表近くに散在。「時によって削られて表に出てきた」イメージ。
- **深層 = Temporal Amber**: 地下深くに封じ込められている。「時が閉じ込められ、保存されている」イメージ。

プレイヤーは ChronoDawn 到達直後にまず Entropy を発見(武器ルート)、深く掘り進めて Temporal Amber を発見(防具ルート)という自然な動線をたどる。

### Deepslate 系追加時の扱い

将来 Temporal Deepslate(仮称)が導入された時は、既存ChronoDawn深層鉱石と同じ手順でリベース:
1. Temporal Amber Ore のテクスチャ背景を Temporal Deepslate の色味に合わせて再生成
2. worldgen の replace target に Temporal Deepslate を追加
3. ブロックID・ドロップ・レシピは変更不要

現時点の設計は将来の Deepslate 追加と競合しない。

## Resource Chain

### Entropy 側

```
Entropy Crystal Ore
  ↓ 採掘(pickaxe, Iron tier 以上)
Entropy Crystal(1個、Fortune対応、Silk Touch で鉱石ブロック回収)
  ↓ クラフト
Entropy Crystal Sword
```

精錬ステップなし(結晶なので)。1本の剣で完結するため、採掘後の余剰は少ない。

### Temporal Amber 側

```
Temporal Amber Ore
  ↓ 採掘(pickaxe, Iron tier 以上)
Raw Temporal Amber(1個、Fortune対応、Silk Touch で鉱石ブロック回収)
  ├─ クラフト(防具レシピ) → Temporal Amber Helmet/Chestplate/Leggings/Boots
  └─ クラフト(粉砕レシピ、1:2) → Temporal Amber Dust(修復燃料)
```

精錬ステップなし(樹脂なので)。4ピース装備完成後は Raw Temporal Amber を粉に変換し、修復燃料として運用する。1:2レートにより後半は粉としての価値が高い。

## Crafting Recipes

### Entropy Crystal Sword

既存の SwordItem レシピパターン。
```
  X
  X
  I
```
X = `entropy_crystal`, I = `minecraft:stick`

### Temporal Amber Armor

既存の防具レシピパターン。X = `raw_temporal_amber`。
- Helmet: 5個
- Chestplate: 8個
- Leggings: 7個
- Boots: 4個

(Enhanced Clockstone のように Time Crystal を追加要求することはしない。完全並列ルートとして単純化。)

### Temporal Amber Dust

作業台で 1→2 変換(shapeless、燃料不要):
```
[raw_temporal_amber] → [temporal_amber_dust] x2
```

## Combat & Performance

### Entropy Crystal Sword

| 項目 | 値 |
|---|---|
| 攻撃力 | 6〜7(Clockstone Sword 相当) |
| 攻撃速度 | 1.6 |
| 耐久 | 〜400(Iron と Enhanced Clockstone の中間) |
| エンチャント性 | 14(Iron 9 より高めで Clockstone と同等) |
| 特殊効果 | 命中時に Entropy MobEffect を対象に付与 |
| ツール層 | Iron 以上が破壊可能 |

### Entropy MobEffect(カスタム状態異常)

| 項目 | 値 |
|---|---|
| 効果 | 対象に毎秒1ダメージの継続ダメージ |
| 持続時間 | 5秒(再命中で持続時間リセット+延長) |
| レベル | I 固定(強化なし) |
| アイコン | 砂時計が崩れるピクセルアイコン |
| 防具による軽減 | あり(通常のダメージ計算を通す) |
| 重複 | なし(再命中時は持続時間の更新のみ) |
| ボスへの効果 | 通常モブと同じ扱い |

実装は既存 `TimeDistortionEffect` と同じパターン。`MobEffect` を継承したクラスを `ModMobEffects` に登録し、`EntropyCrystalSwordItem#hurtEnemy` で `LivingEntity#addEffect` を呼ぶ。Mixin 不要。

### Temporal Amber Armor

| 部位 | 防御 | 耐久(胸当て基準の比率) |
|---|---|---|
| Helmet | 3 | 〜385 |
| Chestplate | 7 | 〜560 |
| Leggings | 6 | 〜525 |
| Boots | 3 | 〜455 |
| **合計防御** | **19** | — |

| 項目 | 値 |
|---|---|
| 靭性(toughness) | 1.5 |
| ノックバック耐性 | 0 |
| エンチャント性 | 10(ダイヤ相当、Enhanced Clockstone の 16 より低い) |
| 胸当て耐久 | 〜560(Enhanced Clockstone 448 の約 +25%) |

Enhanced Clockstone との比較:
- 防御合計は同等(19)
- 靭性はやや低い(2.0 → 1.5)
- エンチャント性は明確に低い(16 → 10)
- 耐久は高い(約 +25%)
- フルセット効果が異なる(時間歪み免疫 vs 自動修復)

### Temporal Amber 自動修復(フルセット効果)

**発動条件**:
- プレイヤーが Temporal Amber 防具を4部位全装備中
- 最後に被ダメージを受けてから10秒以上経過
- インベントリに `temporal_amber_dust` が1個以上存在
- 少なくとも1部位が満耐久未満

**動作**:
- 3秒ごとに1チック発動
- 1チックで各装備の耐久を5回復(4ピース合計20耐久)
- 1チックにつき `temporal_amber_dust` 1個をインベントリから消費
- 全装備が満タンの時は発動せず、粉を消費しない

**修繕エンチャントとの関係**:
- Temporal Amber 防具に Mending(修繕)を付与可能
- Mending(XP経由)と自動修復(粉経由)は両立する異なる経路
- 戦闘中は Mending のみ有効、戦闘外は両方が並行動作

**バランス試算**:
- 胸当て 0→満タン(560耐久)に必要な粉: 560 / 5 = 112個
- フルセット 0→満タンに必要な粉: 約 95 個(各部位で分配修復)
- Raw Temporal Amber 約 48 個から生成可能(1:2レート)
- 鉱脈1つ(2〜4ブロック)で 1〜2 回分の大規模修復
- 数十鉱脈で遠征 1 回分の運用をカバー

**実装**:
- `TemporalAmberArmorItem#inventoryTick` でフルセット判定・戦闘外判定・粉消費・耐久回復を実装
- フルセット判定は既存 `EnhancedClockstoneArmorItem#isWearingFullSet` パターン踏襲
- 戦闘外判定: `Player#getLastHurtByMobTimestamp` または独自 CooldownTracker で「最後に被ダメージを受けた tick」を追跡
- サーバーサイドで耐久を変更(クライアントには自動同期)

## Implementation Scope

### ブロッククラス(全バージョン)

- `EntropyCrystalOre.java`(`DropExperienceBlock` 継承、既存 `TemporalCoalOre` パターン)
- `TemporalAmberOre.java`(同上)
- 各バージョンの `ModBlocks.java` に登録追加

### アイテムクラス(全バージョン)

- `EntropyCrystalSwordItem.java`(`SwordItem` 継承、`hurtEnemy` で Entropy MobEffect 付与)
- `EntropyCrystalTier.java`(`Tier` 実装)
- `TemporalAmberArmorItem.java`(`Item` 継承、`inventoryTick` で修復ロジック)
- `TemporalAmberArmorMaterial.java`(`ArmorMaterial` / `Holder<ArmorMaterial>` 定義)
- `TemporalAmberDustItem.java`(通常の `Item`)
- 各バージョンの `ModItems.java` に以下を登録:
  - BlockItem: entropy_crystal_ore, temporal_amber_ore
  - Item: entropy_crystal, raw_temporal_amber, temporal_amber_dust
  - Item: entropy_crystal_sword
  - Item: temporal_amber_helmet / chestplate / leggings / boots

### MobEffect(全バージョン)

- `EntropyEffect.java`(`MobEffect` 継承、`applyEffectTick` で `entity.hurt(...)` を呼ぶ)
- 各バージョンの `ModMobEffects.java` に登録
- 翻訳キー `effect.chronodawn.entropy`

### 修復ロジック実装詳細

`TemporalAmberArmorItem#inventoryTick` 内で以下を順に評価:

1. サーバーサイドかつ `entity` が `Player` であること
2. スタックのスロットが ARMOR スロット(HEAD/CHEST/LEGS/FEET)であること
3. `isWearingFullSet(player)` が true
4. 最後に被ダメージを受けてから 200 tick(10秒)以上経過
5. 自身の耐久が満タンではない
6. `Player#getInventory` に `temporal_amber_dust` が1個以上存在
7. `level.getGameTime() % 60 == 0`(3秒ごと、スロット間で重複しないよう HEAD スロットでのみ実行)
8. 条件が全て満たされたら:
   - `temporal_amber_dust` を1個消費
   - 4部位それぞれに `setDamageValue(currentDamage - 5)`(下限0)

スロット間の競合回避のため、実際の粉消費+4部位修復は `HEAD` 部位の inventoryTick でのみ実行する(既存 `EnhancedClockstoneArmorItem` の判定パターンを踏襲)。

### Worldgen

- `common/shared-*/src/main/resources/data/chronodawn/worldgen/configured_feature/entropy_crystal_ore.json`
- `common/shared-*/src/main/resources/data/chronodawn/worldgen/configured_feature/temporal_amber_ore.json`
- `common/shared-*/src/main/resources/data/chronodawn/worldgen/placed_feature/entropy_crystal_ore.json`(Y = 40〜100、count 4)
- `common/shared-*/src/main/resources/data/chronodawn/worldgen/placed_feature/temporal_amber_ore.json`(Y = -30〜20、count 4)
- biome modifier(NeoForge)/ biome override(Fabric)で ChronoDawn バイオームに紐付け
- **1.21.11 の biome override 別ディレクトリ対応**(記憶: `1.21.11` 専用の biome JSON を独立更新)

### リソースファイル

**Blockstate / Model JSON**:
- `blockstates/entropy_crystal_ore.json`, `temporal_amber_ore.json`
- `models/block/entropy_crystal_ore.json`, `temporal_amber_ore.json`
- `models/item/*.json`(鉱石BlockItem、剣、防具4ピース、粉、原材料、結晶)

**テクスチャ PNG**:
- `textures/block/entropy_crystal_ore.png`(temporal_stone 背景 + 砂色/白系の結晶斑)
- `textures/block/temporal_amber_ore.png`(temporal_stone 背景 + 琥珀色の樹脂斑)
- `textures/item/entropy_crystal.png`
- `textures/item/raw_temporal_amber.png`
- `textures/item/temporal_amber_dust.png`
- `textures/item/entropy_crystal_sword.png`
- `textures/item/temporal_amber_helmet.png` / `chestplate.png` / `leggings.png` / `boots.png`
- `textures/models/armor/temporal_amber_layer_1.png`
- `textures/models/armor/temporal_amber_layer_2.png`

`texture-creation` スキルに従い、既存 vanilla テクスチャの色変換・合成で生成。

**Client Items JSON(1.21.4+)**:
- 剣・防具4ピース・鉱石BlockItem にそれぞれ `items/*.json` を追加
- shared-1.21.5+ にも共通 JSON を配置(記憶: 1.21.4 と shared-1.21.5+ に両方必要)

**翻訳**:
- `lang/ja_jp.json`, `lang/en_us.json` にブロック・アイテム・MobEffect の翻訳キー追加
- `validateTranslations` タスクがバージョン間整合性を検証

### レシピ・ルートテーブル・タグ

**レシピ**(データパック JSON):
- `recipes/entropy_crystal_sword.json`(shaped)
- `recipes/temporal_amber_helmet/chestplate/leggings/boots.json`(shaped)
- `recipes/temporal_amber_dust.json`(shapeless, 1 raw_temporal_amber → 2 dust)

**ルートテーブル**:
- `loot_table/blocks/entropy_crystal_ore.json`(Fortune対応の entropy_crystal ドロップ、Silk Touch で自己ドロップ)
- `loot_table/blocks/temporal_amber_ore.json`(同様)
- **1.20.1 は `loot_tables`(複数形)ディレクトリ**、1.21.1+ は `loot_table`(単数形)
- Silk Touch 判定は 1.21.5+ の nested `predicates` → `minecraft:enchantments` 形式で書く

**タグ**:
- `minecraft:mineable/pickaxe`(両鉱石ブロック)
- `minecraft:needs_iron_tool`(両鉱石ブロック、カスタム鉱石の tier ゲーティング必須)
- `chronodawn:ores`(存在する場合)
- 既存の他タグ整合性は実装時に確認

### 実績(Advancement)

新規 advancement を追加(全てバニラトリガーで検出可能なものに絞る):

- `chronodawn:entropy_crystal_obtained` — Entropy Crystal を初めて入手(`inventory_changed` トリガー)
- `chronodawn:temporal_amber_obtained` — Raw Temporal Amber を初めて入手(`inventory_changed`)
- `chronodawn:temporal_amber_dust_obtained` — Temporal Amber Dust を初めて入手(`inventory_changed`)
- `chronodawn:temporal_amber_full_set` — Temporal Amber 防具4部位全装備(`thrown_item_picked_up_by_entity` は不適切なため、`inventory_changed` の全4部位条件指定で検出)

advancement JSON の配置は既存 `chronodawn` 実績ディレクトリパターンに従う。アイコン・親子関係は既存実績ツリー構造に合わせる。カスタムトリガーが必要な挙動(自動修復の発動)は advancement 対象外とし、スコープ簡略化のため省略する。

### 対応バージョン

- 1.20.1
- 1.21.1
- 1.21.2(1.21.3 と共有)
- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11

### バージョン差分の主な注意点

- **1.20.1**: `loot_tables`(複数形)ディレクトリ、`DropExperienceBlock` コンストラクタ引数順(Properties, IntProvider)
- **1.21.1〜1.21.3**: loot_table 構造が 1.21.1+ 形式。`DropExperienceBlock` 引数順が (IntProvider, Properties) に反転
- **1.21.4+**: Client Items JSON 必須
- **1.21.5+**: `ArmorItem` 廃止 → `Item.Properties#humanoidArmor()`。既存 `EnhancedClockstoneArmorItem` パターンを踏襲。loot table の Silk Touch 判定形式変更
- **1.21.11**: biome override 別ディレクトリ(`shared-1.21.2+` とは別に `1.21.11` 専用の biome JSON を更新)

### スコープ外(再掲)

- Entropy 側の斧・ピッケル・防具
- Temporal Amber 側のツール・盾
- 専用バイオーム、専用構造物
- Temporal Deepslate 追加(将来別タスク)
- GameTest(手動検証のみ)
- Entropy 剣と既存エンチャントの特殊相互作用

## Verification

### ビルド・検証

- `./gradlew checkAll`(全バージョンクリーン + validateResources + validateTranslations + buildAll + testAll + gameTestAll)
- `./gradlew validateResources` で JSON 構文・blockstate→model→texture のクロスリファレンスを検証
- `./gradlew validateTranslations` でバージョン間翻訳キー整合を検証

### 手動検証(各バージョン代表: 1.21.1, 1.21.5, 1.21.11)

1. `./gradlew runClientFabric{version}` または `runClientNeoForge{version}` で起動
2. クリエイティブで ChronoDawn ディメンションに入り、両鉱石ブロックが CreativeTab に表示されることを確認
3. サバイバルで ChronoDawn に入域し、Y帯を変えて両鉱石が想定層に生成されることを確認
4. Entropy Crystal Ore を採掘 → Entropy Crystal がドロップ → 剣をクラフト
5. 剣でモブを攻撃 → Entropy エフェクトが付与され、5秒間 DoT が発動することを確認
6. Temporal Amber Ore を採掘 → Raw Temporal Amber がドロップ → 防具4ピースと粉をクラフト
7. 防具フルセット装備 → 防具の耐久を消費(落下ダメージ等)→ インベントリに粉を入れて10秒待機 → 修復が発動し粉が消費されることを確認
8. 戦闘中(モブから被ダメージ)に修復が止まることを確認
9. 粉がない時に修復が発動しないことを確認
10. 各 advancement のトリガーが正常に発火することを確認

## Open Questions

実装時に解決:

- Entropy Crystal の実際の攻撃力・耐久値(Clockstone Sword の正確な値を既存コードから参照)
- Temporal Amber 防具の各部位耐久値(Enhanced Clockstone × 1.25 の具体値)
- ore 出現数の最終値(プレイテストで調整)
- advancement の親子関係とアイコン(既存ツリー構造との整合)
- Y帯の最終値(実プレイでの発見難度に応じて微調整)
