# GameTest Framework 構築計画

## 背景

### 現状
- テストは「GameTest」と命名されているが、実際はJUnit 5テスト
- Minecraftランタイム不要のテスト（クラス存在、定数値、ファイル存在）のみ実行可能
- Minecraftランタイム必要なテストは `@Disabled` でスキップ
- `runGameTest` タスクは存在しない

### 目標
- Minecraft GameTest Framework を正式に構築
- `@Disabled` テストを実際に実行可能にする
- Fabric/NeoForge 両プラットフォームでGameTest実行

## 技術調査結果

### Fabric GameTest
- **参照**: [Fabric Automated Testing Documentation](https://docs.fabricmc.net/develop/automatic-testing)
- Fabric Loom の `fabricApi.configureTests` ブロックで設定
- `@GameTest` アノテーションと `GameTestHelper` を使用
- クライアント/サーバー両方のGameTestをサポート

```gradle
fabricApi {
    configureTests {
        createSourceSet = true
        modId = "chronosphere-test"
        enableGameTests = true
        enableClientGameTests = true
        eula = true
    }
}
```

### NeoForge GameTest
- **参照**: [NeoForge Game Tests Documentation](https://docs.neoforged.net/docs/misc/gametest/)
- `gameTestServer` run configuration を追加
- `RegisterGameTestsEvent` でテスト登録
- システムプロパティ `neoforge.enabledGameTestNamespaces` で制御

```gradle
runs {
    gameTestServer {
        gameTestServer()
        systemProperty 'neoforge.enabledGameTestNamespaces', 'chronosphere'
    }
}
```

### Architectury Loom の考慮事項
- 現プロジェクトは Architectury Loom を使用（純粋なFabric Loom / NeoGradleではない）
- common モジュールのテストをプラットフォーム固有モジュールで実行する必要
- 追加の設定が必要な可能性あり

## 実装計画

### Phase 1: Fabric GameTest 構築

#### T172a: Fabric build.gradle に GameTest 設定追加
```gradle
fabricApi {
    configureTests {
        createSourceSet = true
        modId = "chronosphere-test"
        enableGameTests = true
        enableClientGameTests = false  // サーバーサイドのみで開始
        eula = true
    }
}
```

#### T172b: Fabric GameTest ソースセット作成
- `fabric/src/gametest/java/com/chronosphere/gametest/`
- `fabric/src/gametest/resources/`

#### T172c: サンプル GameTest 作成
```java
package com.chronosphere.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class ChronosphereGameTests {
    @GameTest(template = "chronosphere:empty_3x3")
    public void testTimeGuardianSpawn(GameTestHelper helper) {
        // Time Guardian エンティティのスポーンテスト
        helper.spawn(ModEntityTypes.TIME_GUARDIAN.get(), new BlockPos(1, 1, 1));
        helper.succeedWhenEntityPresent(ModEntityTypes.TIME_GUARDIAN.get(), new BlockPos(1, 1, 1));
    }
}
```

#### T172d: テスト用構造体テンプレート作成
- `fabric/src/gametest/resources/data/chronosphere/gametest/structures/`
- `empty_3x3.nbt` - 基本的な空の構造体
- `boss_arena.nbt` - ボス戦テスト用アリーナ

#### T172e: Fabric GameTest 実行確認
```bash
./gradlew :fabric:runGameTest
```

### Phase 2: NeoForge GameTest 構築

#### T173a: NeoForge build.gradle に GameTest run configuration 追加
```gradle
loom {
    runs {
        // 既存の client, server に追加
        gameTestServer {
            server()
            setConfigName("NeoForge GameTest Server")
            ideConfigGenerated(true)
            runDir("run")
            // GameTest 有効化
            property("neoforge.enabledGameTestNamespaces", "chronosphere")
        }
    }
}
```

#### T173b: NeoForge GameTest 登録クラス作成
```java
package com.chronosphere.neoforge.gametest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = "chronosphere", bus = EventBusSubscriber.Bus.MOD)
public class ChronosphereGameTestsNeoForge {
    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(ChronosphereGameTests.class);
    }
}
```

#### T173c: NeoForge GameTest 実行確認
```bash
./gradlew :neoforge:runGameTestServer
```

### Phase 3: 共通 GameTest ロジック

#### T172f/T173d: 共通テストロジックを common モジュールに配置
- `common/src/main/java/com/chronosphere/gametest/` (共有テストロジック)
- プラットフォーム固有の登録は各モジュールで実施

### Phase 4: 既存 @Disabled テストの移行

#### T172g/T173e: 優先度高のテストから移行
1. ボス戦テスト (Time Guardian, Time Tyrant)
2. 構造体生成テスト (Desert Clock Tower, Master Clock)
3. アイテム効果テスト (Chronoblade, Armor)
4. ポータルテスト

## タスク一覧

| ID | タスク | 依存 | 優先度 |
|----|--------|------|--------|
| T172a | Fabric build.gradle GameTest設定 | - | 高 |
| T172b | Fabric GameTest ソースセット作成 | T172a | 高 |
| T172c | Fabric サンプルGameTest作成 | T172b | 高 |
| T172d | テスト用構造体テンプレート作成 | T172b | 中 |
| T172e | Fabric GameTest実行確認 | T172c | 高 |
| T172f | 共通テストロジック整理 | T172e | 中 |
| T172g | 既存@Disabledテスト移行(Fabric) | T172f | 低 |
| T173a | NeoForge build.gradle GameTest設定 | - | 高 |
| T173b | NeoForge GameTest登録クラス作成 | T173a | 高 |
| T173c | NeoForge GameTest実行確認 | T173b | 高 |
| T173d | 共通テストロジック活用(NeoForge) | T173c, T172f | 中 |
| T173e | 既存@Disabledテスト移行(NeoForge) | T173d | 低 |

## リスク・課題

1. **Architectury Loom 互換性**: 純粋なFabric Loom/NeoGradleとは設定が異なる可能性
2. **テスト構造体**: NBT構造体の作成・管理が必要
3. **CI統合**: ヘッドレス環境でのGameTest実行にはXVFB等が必要（Linux）
4. **実行時間**: GameTestはMinecraft起動が必要なため、JUnitより大幅に遅い

## 完了ステータス

### Phase 1: Fabric GameTest ✅ 完了 (2025-12)
- **コミット**: 9cca9db
- **実行コマンド**: `./gradlew :fabric:runGameTest`
- **テスト数**: 9件全て合格
- **実装**:
  - `fabric/build.gradle`: `loom { runs { gameTest { ... } } }` 設定追加
  - `fabric/src/gametest/java/.../ChronosphereGameTestsFabric.java`: @GameTest使用
  - `FabricGameTest.EMPTY_STRUCTURE` テンプレート使用（Fabric標準）

### Phase 2: NeoForge GameTest ✅ 完了 (2025-12)
- **コミット**: 6790b80
- **実行コマンド**: `./gradlew :neoforge:runGameTestServer`
- **テスト数**: 9件全て合格
- **実装**:
  - `neoforge/build.gradle`: `gameTestServer` run configuration追加
  - `neoforge/src/main/java/.../ChronosphereGameTestsNeoForge.java`: @GameTestGenerator使用
  - `RegisterGameTestsEvent` でテストクラス登録
  - 既存の `chronosphere:ancient_ruins` 構造体テンプレート使用

### Phase 3: 共通GameTestロジック ✅ 完了 (2025-12)
- **コミット**: de34c79
- **実装**:
  - `common/src/main/java/com/chronosphere/gametest/ChronosphereGameTestLogic.java`: 共通テストロジック
  - Fabric/NeoForge両方が `Consumer<GameTestHelper>` で共通ロジックを呼び出し
- **結果**: コード重複削減（172行追加、177行削除）

### 学んだこと
- **Fabric**: Architectury Loomで `@GameTest` アノテーションが正常に動作
- **NeoForge**: Architectury Loomで `@GameTest` アノテーションが発見されない問題あり
  - 解決策: `@GameTestGenerator` を使用して `TestFunction` を明示的に生成
- **テンプレート**: Fabricは SNBT 形式 (`fabric-gametest-api-v1:empty`) をサポート、NeoForgeはNBT形式のみ

## 参照

- [Fabric Automated Testing](https://docs.fabricmc.net/develop/automatic-testing)
- [NeoForge Game Tests](https://docs.neoforged.net/docs/misc/gametest/)
- [Minecraft Wiki - GameTest](https://minecraft.wiki/w/GameTest)
