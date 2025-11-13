# Custom Mob Textures

このディレクトリには、カスタムモブのテクスチャファイルを配置します。

## 必要なテクスチャファイル

以下の3つのテクスチャファイルが必要です:

1. **temporal_wraith.png** - Temporal Wraith (時の亡霊)
2. **clockwork_sentinel.png** - Clockwork Sentinel (時計仕掛けの番兵)
3. **time_keeper.png** - Time Keeper (時間の管理者)

## プレースホルダーとしてバニラテクスチャをコピーする方法

### 方法1: Minecraft JARから直接コピー

1. Minecraftのインストールディレクトリを開く
   - **macOS**: `~/Library/Application Support/minecraft/versions/1.21.1/`
   - **Windows**: `%APPDATA%\.minecraft\versions\1.21.1\`
   - **Linux**: `~/.minecraft/versions/1.21.1/`

2. `1.21.1.jar`ファイルを解凍ツール（7-Zip、The Unarchiverなど）で開く

3. 以下のパスからテクスチャをコピー:
   - `assets/minecraft/textures/entity/zombie/zombie.png` → `temporal_wraith.png`
   - `assets/minecraft/textures/entity/skeleton/skeleton.png` → `clockwork_sentinel.png`
   - `assets/minecraft/textures/entity/wandering_trader.png` → `time_keeper.png`

4. このディレクトリ (`common/src/main/resources/assets/chronosphere/textures/entity/mobs/`) にコピー

### 方法2: 開発環境のGradleキャッシュからコピー

1. プロジェクトルートで以下を実行:
   ```bash
   ./gradlew :fabric:extractNatives
   ```

2. Gradleキャッシュディレクトリを開く:
   ```bash
   # macOS/Linux
   cd ~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1/

   # Windows
   cd %USERPROFILE%\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged\1.21.1\
   ```

3. `minecraft-merged-*.jar`を解凍して、方法1と同じパスからコピー

## テクスチャ編集後

テクスチャを編集したら、以下を実行してゲーム内で確認:

```bash
./gradlew :fabric:runClient
```

## テクスチャのサイズ

- **temporal_wraith.png**: 64x64 pixels (標準的なプレイヤー/モブテクスチャ)
- **clockwork_sentinel.png**: 64x32 pixels (スケルトンテクスチャ)
- **time_keeper.png**: 64x64 pixels (村人テクスチャ)

## 注意事項

- バニラのテクスチャをそのまま使用する場合は、Mojangの利用規約を確認してください
- 配布する場合は、必ず独自のテクスチャに差し替えてください
- これらのファイルは`.gitignore`に追加されており、Gitにはコミットされません
