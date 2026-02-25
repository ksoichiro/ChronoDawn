# Minecraft 1.21.9 API 変更調査結果

## 概要

1.21.8 → 1.21.9 での破壊的 API 変更一覧。全47エラー、8カテゴリ。

---

## A. `noCollission()` → `noCollision()` (スペル修正)

**影響**: 15ファイル (全てブロック関連)
**難易度**: 低 (一括置換)

`BlockBehaviour.Properties.noCollission()` が `noCollision()` にリネーム（typo修正）。

**修正**: `common/1.21.9/` 内で `.noCollission()` → `.noCollision()` を一括置換

**対象ファイル**:
- `blocks/DarkTimeWoodButton.java:43`
- `blocks/TimeWoodPressurePlate.java:44`
- `blocks/TimeWoodSapling.java:56`
- `blocks/TemporalParticleEmitterBlock.java:61`
- `blocks/TimeBlossomBlock.java:62`
- `blocks/DawnBellBlock.java:40`
- `blocks/ChronoDawnPortalBlock.java:159`
- `blocks/UnstableFungus.java:64`
- `blocks/DuskBellBlock.java:40`
- `blocks/AncientTimeWoodSapling.java:56`
- `blocks/AncientTimeWoodPressurePlate.java:42`
- `blocks/DarkTimeWoodSapling.java:56`
- `blocks/DarkTimeWoodPressurePlate.java:43`
- `blocks/TimeWoodButton.java:45`
- `blocks/AncientTimeWoodButton.java:42`
- `registry/ModBlocks.java:459`

---

## B. `isClientSide` フィールド → `isClientSide()` メソッド

**影響**: 21ファイル
**難易度**: 低 (一括置換)
**状態**: ✅ 修正済み

`Level.isClientSide` フィールドが private になり `Level.isClientSide()` メソッドに変更。

---

## C. `TextureSheetParticle` → `SingleQuadParticle` + Particle API 全面変更

**影響**: 1ファイル (`client/particle/ChronoDawnPortalParticle.java`)
**難易度**: 大

### 変更点

1. **クラス名**: `TextureSheetParticle` → `SingleQuadParticle`（済み、ただし追加変更必要）

2. **コンストラクタ**: `TextureAtlasSprite` が必須引数に追加
   ```java
   // Before
   super(level, x, y, z, velocityX, velocityY, velocityZ);
   // After
   super(level, x, y, z, velocityX, velocityY, velocityZ, sprite);
   ```

3. **`getRenderType()` → `getGroup()` + `getLayer()`**:
   ```java
   // Before
   @Override
   public ParticleRenderType getRenderType() {
       return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }
   // After
   @Override
   public ParticleRenderType getGroup() {
       return ParticleRenderType.SINGLE_QUADS;
   }
   @Override
   protected SingleQuadParticle.Layer getLayer() {
       return SingleQuadParticle.Layer.TRANSLUCENT;
   }
   ```

4. **`ParticleProvider.createParticle`** に `RandomSource` 引数追加:
   ```java
   // Before
   Particle createParticle(T type, ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz);
   // After
   Particle createParticle(T type, ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz, RandomSource random);
   ```

5. **`pickSprite`** → コンストラクタで sprite を渡す方式に変更:
   ```java
   TextureAtlasSprite sprite = this.sprites.get(random);
   new ChronoDawnPortalParticle(level, x, y, z, vx, vy, vz, sprite);
   ```

---

## D. レンダラー: `render()` → `submit()` (描画パイプライン変更)

**影響**: 4ファイル
**難易度**: 大

### 変更点

メソッドシグネチャ:
```java
// Before (1.21.8)
void render(S state, PoseStack poseStack, MultiBufferSource buffer, int packedLight)

// After (1.21.9)
void submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState)
```

直接的な `VertexConsumer` / `renderToBuffer` 呼び出しの代わりに `submitModel()` / `submitCustomGeometry()` を使用。

### 対象ファイルと修正内容

#### D1. `client/renderer/TimeBlastRenderer.java`
- `render` → `submit`
- `MultiBufferSource buffer, int packedLight` → `SubmitNodeCollector collector, CameraRenderState cameraState`
- `this.entityRenderDispatcher.cameraOrientation()` → `cameraState.orientation`
- VertexConsumer 直接描画 → `collector.submitCustomGeometry()` に変更
- `super.render(...)` → `super.submit(...)`

#### D2. `client/renderer/ChronoDawnBoatRenderer.java`
- `render` → `submit`
- `buffer.getBuffer(...)` + `model.renderToBuffer(...)` → `collector.submitModel(...)` に変更
- `super.render(...)` → `super.submit(...)`

#### D3. `client/renderer/ChronoDawnChestBoatRenderer.java`
- BoatRenderer と同じ変更

#### D4. `client/renderer/GearProjectileRenderer.java`
- `render` → `submit`
- `ItemStackRenderState.render(poseStack, buffer, packedLight, overlay)` → `ItemStackRenderState.submit(poseStack, collector, ...)` に変更
- `super.render(...)` → `super.submit(...)`

### 新しいインポート
- `net.minecraft.client.renderer.SubmitNodeCollector`
- `net.minecraft.client.renderer.state.CameraRenderState`

### 削除するインポート
- `net.minecraft.client.renderer.MultiBufferSource`

---

## E. `ArmedModel` ジェネリクス追加

**影響**: 1ファイル (`client/model/SecondhandArcherModel.java`)
**難易度**: 小

```java
// Before
implements ArmedModel {
    void translateToHand(HumanoidArm arm, PoseStack poseStack)

// After
implements ArmedModel<SecondhandArcherRenderState> {
    void translateToHand(SecondhandArcherRenderState state, HumanoidArm arm, PoseStack poseStack)
```

---

## F. GUI `mouseClicked` シグネチャ変更

**影響**: 2ファイル
**難易度**: 中

```java
// Before
boolean mouseClicked(double mouseX, double mouseY, int button)

// After
boolean mouseClicked(MouseButtonEvent event, boolean consumed)
```

`MouseButtonEvent` は `x()`, `y()`, `buttonInfo().button()` でアクセス。

### 対象ファイル
- `gui/widgets/CategoryListWidget.java:178`
- `gui/widgets/EntryPageWidget.java:566-576`

新しいインポート: `net.minecraft.client.input.MouseButtonEvent`

---

## G. `SpawnEggItem` コンストラクタ変更

**影響**: 1ファイル (`items/DeferredSpawnEggItem.java`)
**難易度**: 中

### 変更点

1. **コンストラクタ**: EntityType 引数が削除
   ```java
   // Before
   super(entityType, properties);
   // After
   super(properties);
   ```

2. **`getType` メソッド**: `HolderLookup.Provider` 引数が削除
   ```java
   // Before
   EntityType<?> getType(HolderLookup.Provider registries, ItemStack stack)
   // After
   EntityType<?> getType(ItemStack stack)
   ```

---

## H. `getSharedSpawnPos()` 削除

**影響**: 1ファイル (`common/shared` の `core/dimension/ChronoDawnDimension.java:56`)
**難易度**: 小（ただし common/shared なので除外リスト対応が必要）

```java
// Before
BlockPos worldSpawn = level.getSharedSpawnPos();

// After
BlockPos worldSpawn = level.getRespawnData().pos();
```

common/shared にあるので、1.21.9 の build.gradle の `excludedFiles` に追加し、1.21.9 固有のバージョンを作成する必要がある。

---

## 修正優先順位

| 順番 | カテゴリ | エラー数 | 難易度 | 方法 |
|------|---------|---------|--------|------|
| 1 | A. noCollission → noCollision | ~16 | 低 | 一括置換 |
| 2 | H. getSharedSpawnPos | 1 | 小 | 除外+版固有ファイル |
| 3 | E. ArmedModel ジェネリクス | 2 | 小 | 手動修正 |
| 4 | G. SpawnEggItem | 2 | 中 | 手動修正 |
| 5 | F. mouseClicked | 5 | 中 | 手動修正 |
| 6 | C. Particle API | 7 | 大 | 全面書き換え |
| 7 | D. render → submit | ~14 | 大 | 全面書き換え |

**合計**: 47エラー → 0 を目標
