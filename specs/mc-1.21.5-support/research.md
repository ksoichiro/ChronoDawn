# Minecraft 1.21.5 対応調査結果

## 調査日: 2026-01-29

## 1. 依存関係バージョン情報

### 確認済みバージョン

| 依存関係 | 1.21.5対応バージョン | ソース |
|---------|---------------------|--------|
| NeoForge | 21.5.96 (stable) | [Maven Repository](https://maven.neoforged.net/releases/net/neoforged/neoforge/) |
| Fabric API | 0.121.0+1.21.5 | [Modrinth](https://modrinth.com/mod/fabric-api/version/0.121.0+1.21.5) |
| Architectury API | 16.1.4 (Fabric), 16.0.3 (NeoForge) | [Modrinth](https://modrinth.com/mod/architectury-api/versions) |

## 2. 主要な破壊的変更 (1.21.4 → 1.21.5)

### 2.1 武器・ツール・防具システムの完全刷新 (重要度: **高**)

**影響範囲**:
- `common-1.21.4/src/main/java/com/chronodawn/items/equipment/` 配下の全ファイル
- `common-1.21.4/src/main/java/com/chronodawn/items/artifacts/` 配下の一部

**変更内容**:
- `SwordItem`, `DiggerItem`, `ArmorItem` 等のハードコードされた基底クラスが**完全削除**
- 全ての機能はデータコンポーネントで実現:
  - `WEAPON` コンポーネント: ダメージ処理
  - `TOOL` コンポーネント: 採掘特性
  - `ARMOR` コンポーネント: 防御力
  - `BLOCKS_ATTACKS` コンポーネント: シールド的なブロック機能
- アイテムは `Item$Properties` のヘルパーメソッドで設定: `.sword()`, `.pickaxe()`, `.humanoidArmor()` 等

**対応が必要なファイル**:
- `ClockstoneSwordItem.java` (extends SwordItem)
- `EnhancedClockstoneSwordItem.java` (extends SwordItem)
- `ChronobladeItem.java` (extends SwordItem)
- `ClockstoneArmorItem.java` (extends ArmorItem)
- `EnhancedClockstoneArmorItem.java` (extends ArmorItem)
- `TimeTyrantMailItem.java` (extends ArmorItem)
- `EchoingTimeBootsItem.java` (extends ArmorItem)

### 2.2 NBT システムの刷新 (重要度: **中〜高**)

**変更内容**:
- タグから値を取得するメソッドが `Optional` を返すように変更:
  ```java
  // 旧: int val = tag.getInt("key");
  // 新: Optional<Integer> val = tag.getInt("key");
  //     int valOrDefault = tag.getIntOr("key", 0);
  //     CompoundTag child = tag.getCompoundOrEmpty("child");
  ```
- 新しいCodec連携:
  ```java
  tag.store("key", CODEC, object);
  Optional<T> recovered = tag.read("key", CODEC);
  ```
- `CompoundTag` が **final** に
- `CollectionTag` が **sealed interface** に

**影響範囲**:
- `common-shared/src/main/java/com/chronodawn/data/` 配下の全 SavedData クラス
- 全てのエンティティの `addAdditionalSaveData()` / `readAdditionalSaveData()` メソッド
- `compat/BlockEntityDataHandler.java` 等

### 2.3 SavedData 型システムの変更 (重要度: **中**)

**変更内容**:
- `SavedData` の抽象化が `SavedDataType` に委譲:
  ```java
  public static final SavedDataType<MyData> TYPE = new SavedDataType<>(
      "modid_identifier",
      MyData::new,
      ctx -> codecFactory(ctx),
      DataFixTypes.LEVEL
  );
  MyData data = storage.computeIfAbsent(MyData.TYPE);
  ```

**影響範囲**:
- `compat/CompatSavedData.java`
- `data/ChronoDawnWorldData.java`
- 全ての SavedData サブクラス

### 2.4 BlockEntity 削除ロジックのリファクタリング (重要度: **低〜中**)

**変更内容**:
- 削除処理が2つのメソッドに分割:
  - `BlockEntity#preRemoveSideEffects`: 削除前のアイテムドロップ処理
  - `BlockBehaviour#affectNeighborsAfterRemoval`: 隣接ブロックの更新
- Container インスタンスでは `Containers#updateNeighboursAfterDestroy` を使用

**影響範囲**:
- `BossRoomDoorBlockEntity.java`
- `ClockTowerTeleporterBlockEntity.java`
- `BossRoomBoundaryMarkerBlockEntity.java`

### 2.5 装備スロットシステムの拡張 (重要度: **低**)

**変更内容**:
- 新しい `EquipmentSlot#SADDLE` 追加
- `EntityEquipment` がマップベースのジェネリックアプローチに変更
- `getArmorSlots`, `getHandSlots` などのエンティティ固有メソッドが削除

### 2.6 GameTest システムの完全刷新 (重要度: **高**)

**変更内容**:
- アノテーション駆動のシステムが**完全に置き換えられ**、レジストリベースに:
  - `TestEnvironmentDefinition` によるセットアップ/ティアダウン
  - データパックレジストリ `minecraft:test_environment`
  - ゲームルール、時間、天気の設定可能な環境

**影響範囲**:
- `common-gametest/` 配下の全テスト
- `RegistryDrivenTestGenerator.java`
- `ChronoDawnGameTestLogic.java`

### 2.7 レンダリングパイプラインの刷新 (重要度: **低**)

**変更内容**:
- シェーダーJSONが廃止され、コード内の `RenderPipeline` で定義
- テクスチャ管理が `GpuTexture` 経由に

**影響範囲**:
- カスタムレンダリングを行っている箇所（現在のコードでは限定的）

### 2.8 その他の変更

- **VoxelShape ヘルパー**: `Block#cube()`, `column()` 等が追加
- **WeightedList**: `SimpleWeightedRandomList` → `WeightedList` に変更
- **Tickets システム**: レジストリベースに変更

## 3. プロジェクト影響分析

### 3.1 影響を受けるコードの概要

| カテゴリ | ファイル数 | 影響度 |
|---------|----------|--------|
| 武器/防具アイテム | 7+ | 高 |
| SavedData 関連 | 10+ | 中〜高 |
| NBT 操作 | 97+ | 中 |
| GameTest | 30+ | 高 |
| BlockEntity | 3+ | 低〜中 |

### 3.2 既存の互換性レイヤー

プロジェクトには既に以下の互換性レイヤーが存在:
- `compat/CompatSavedData.java` - SavedData のバージョン差異を吸収
- `compat/SavedDataHandler.java` - インターフェース
- `compat/BlockEntityDataHandler.java` - BlockEntity データの互換性

これらを1.21.5用に拡張または新バージョンを作成する必要がある。

## 4. 推奨アプローチ

### Phase 1: ディレクトリ構造の準備
1. `props/1.21.5.properties` の作成
2. `common-1.21.5/` ディレクトリの作成（1.21.4をベースに）
3. `fabric-1.21.5/` ディレクトリの作成
4. `neoforge-1.21.5/` ディレクトリの作成

### Phase 2: 基盤コードの対応
1. `compat/` パッケージの1.21.5対応
2. NBT関連のAPI変更対応
3. SavedDataType への移行

### Phase 3: 装備アイテムシステムの刷新
1. データコンポーネントベースの武器/防具システム実装
2. 既存アイテムクラスの移行

### Phase 4: GameTest の対応
1. レジストリベースのテストシステムへの移行
2. TestEnvironmentDefinition の実装

### Phase 5: 検証
1. ビルド確認
2. GameTest 実行
3. クライアント動作確認

## 5. 参考リンク

- [NeoForge 1.21.5 Primer](https://github.com/neoforged/.github/blob/main/primers/1.21.5/index.md)
- [Architectury API Modrinth](https://modrinth.com/mod/architectury-api/versions)
- [Fabric API Modrinth](https://modrinth.com/mod/fabric-api/versions)
- [NeoForge Maven](https://maven.neoforged.net/releases/net/neoforged/neoforge/)
