# Entropy Crystal & Temporal Amber Ores Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhanced Clockstoneと並列のTier 2分岐として、Entropy Crystal(DoT剣特化)とTemporal Amber(粉消費自動修復フル防具)の2系統を全11サポートバージョンに追加する。

**Architecture:** 既存の `TemporalCoalOre` / `EnhancedClockstoneSwordItem` / `EnhancedClockstoneArmorItem` / `ChronoAegisEffect` パターンを踏襲。Entropy MobEffect は `TimeDistortionEffect` と同じ MobEffect 継承パターンで `applyEffectTick` で直接ダメージ。Temporal Amber 自動修復は `TemporalAmberArmorItem#inventoryTick` で発動条件を判定し、HEAD スロットでのみ粉消費+4部位耐久回復を実行して重複実行を避ける。

**Tech Stack:** Java 21, Architectury API, Minecraft modding (1.20.1 / 1.21.1〜1.21.11), Groovy Gradle DSL, Fabric Loom

**Related Spec:** `docs/superpowers/specs/2026-04-11-entropy-crystal-temporal-amber-ores-design.md`

---

## Version Groups

| Group | Versions | Key API Differences |
|---|---|---|
| A | 1.20.1 | `loot_tables/` (plural), `DropExperienceBlock(Properties, IntProvider)`, `ArmorItem` + `SwordItem` inheritance, `Properties.of()`, no `setId()`, `ResourceLocation` |
| B | 1.21.1 | `loot_table/` (singular), `DropExperienceBlock(IntProvider, Properties)`, `ArmorItem` + `SwordItem` inheritance, `Properties.of()`, `ResourceLocation` |
| C | 1.21.2, 1.21.4 | Similar to B + `Properties.ofFullCopy().setId()`, Client Items JSON required from 1.21.4 |
| D | 1.21.5〜1.21.10 | `ArmorItem`/`SwordItem` 廃止 → `Item.Properties#humanoidArmor()`/`sword()` 使用、nested Silk Touch predicate, `ResourceLocation` |
| E | 1.21.11 | D と同じ + `Identifier` (Mojang rename), biome override 専用ディレクトリ |

**重要な共通ルール**:
- `common/shared/src/main/java/...` の shared ディレクトリは ID enum 等の version-agnostic な情報のみ置く
- ブロック/アイテム/エフェクトの Java クラスは **各バージョン個別** に配置(APIシグネチャ差のため)
- リソース JSON/PNG は共有可能なものは `common/shared/src/main/resources/assets/chronodawn/...` と `common/shared/src/main/resources/data/chronodawn/...` に置く
- バージョン差がある JSON(loot_table、Client Items、1.21.11 biome override)は `common/shared-1.21.1+/`, `common/shared-1.21.2+/`, `common/shared-1.21.5+/`, `common/1.21.11/` 等のバージョン別 shared または individual ディレクトリに置く

---

## Phase 1: Foundation (IDs, Effect, Creative Tab Hooks)

### Task 1: Register Block/Item/Effect IDs in shared enums

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java`
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`

- [ ] **Step 1: Add block IDs to ModBlockId.java**

既存の鉱石 enum 近傍(例: TEMPORAL_REDSTONE_ORE の直後)に追加:

```java
    // Specialized Tier 2 ores
    ENTROPY_CRYSTAL_ORE(def("entropy_crystal_ore")),
    TEMPORAL_AMBER_ORE(def("temporal_amber_ore")),
```

- [ ] **Step 2: Add item IDs to ModItemId.java**

既存のアイテム enum に追加(鉱石ブロックアイテム + 原材料 + 武器防具 + 粉):

```java
    // Entropy Crystal
    ENTROPY_CRYSTAL_ORE("entropy_crystal_ore"),
    ENTROPY_CRYSTAL("entropy_crystal"),
    ENTROPY_CRYSTAL_SWORD("entropy_crystal_sword"),

    // Temporal Amber
    TEMPORAL_AMBER_ORE("temporal_amber_ore"),
    RAW_TEMPORAL_AMBER("raw_temporal_amber"),
    TEMPORAL_AMBER_DUST("temporal_amber_dust"),
    TEMPORAL_AMBER_HELMET("temporal_amber_helmet"),
    TEMPORAL_AMBER_CHESTPLATE("temporal_amber_chestplate"),
    TEMPORAL_AMBER_LEGGINGS("temporal_amber_leggings"),
    TEMPORAL_AMBER_BOOTS("temporal_amber_boots"),
```

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat: add IDs for entropy crystal and temporal amber"
```

---

### Task 2: Register Effect ID

**Files:**
- Check/Modify: `common/shared/src/main/java/com/chronodawn/registry/ModEffectId.java` (存在すれば) または直接 `ModEffects.java`

- [ ] **Step 1: Inspect existing effect ID registration**

`common/shared/src/main/java/com/chronodawn/registry/` 配下で `ModEffectId` または類似のクラスを探し、存在すれば enum に `ENTROPY("entropy")` を追加する。存在しない場合は文字列リテラル `"entropy"` を直接 `ModEffects.java` で使用する(Task 6 で対応)。

- [ ] **Step 2: Commit if changes made**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModEffectId.java
git commit -m "feat: add entropy effect id"
```

変更がない場合はスキップ。

---

## Phase 2: Entropy Crystal Ore Block

### Task 3: Create EntropyCrystalOre block class for Group A (1.20.1)

**Files:**
- Create: `common/1.20.1/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`

- [ ] **Step 1: Copy TemporalCoalOre pattern from 1.20.1, adapt for Entropy Crystal**

```java
package com.chronodawn.blocks;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Entropy Crystal Ore - shallow layer specialized ore for Tier 2 weapon branch.
 * Drops Entropy Crystal used to craft Entropy Crystal Sword (DoT on hit).
 *
 * Generation: ChronoDawn dimension, Y 40-100, temporal_stone host.
 * Tool: Iron pickaxe tier or above.
 */
public class EntropyCrystalOre extends DropExperienceBlock {
    public EntropyCrystalOre(BlockBehaviour.Properties properties) {
        // 1.20.1: (Properties, IntProvider) 引数順
        super(properties, UniformInt.of(2, 5));
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(net.minecraft.world.level.block.SoundType.STONE);
    }
}
```

- [ ] **Step 2: Verify 1.20.1 compile**

```bash
./gradlew :common-1.20.1:compileJava -Ptarget_mc_version=1.20.1
```

Expected: BUILD SUCCESSFUL(Java のみ、resources エラーはこの段階ではあり得る)

- [ ] **Step 3: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git commit -m "feat: add EntropyCrystalOre block class for 1.20.1"
```

---

### Task 4: Create EntropyCrystalOre block class for Group B (1.21.1)

**Files:**
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`

- [ ] **Step 1: Adapt for 1.21.1 (DropExperienceBlock arg order reversed)**

```java
package com.chronodawn.blocks;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class EntropyCrystalOre extends DropExperienceBlock {
    public EntropyCrystalOre(BlockBehaviour.Properties properties) {
        // 1.21.1+: (IntProvider, Properties) 引数順
        super(UniformInt.of(2, 5), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add common/1.21.1/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git commit -m "feat: add EntropyCrystalOre block class for 1.21.1"
```

---

### Task 5: Create EntropyCrystalOre block classes for Group C/D (1.21.2, 1.21.4〜1.21.10)

**Files:**
- Create: `common/1.21.2/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`
- Create: `common/1.21.4/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`
- Create: `common/1.21.5/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`
- Create: `common/1.21.6/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`
- Create: `common/1.21.7/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`
- Create: `common/1.21.8/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`
- Create: `common/1.21.9/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`
- Create: `common/1.21.10/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`

- [ ] **Step 1: Create file per version with ofFullCopy + setId pattern**

各バージョンで以下のテンプレートをコピー。ResourceKey/ResourceLocation のimport pathは既存の `TemporalCoalOre` を同バージョンでコピーして合わせる:

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class EntropyCrystalOre extends DropExperienceBlock {
    public EntropyCrystalOre(BlockBehaviour.Properties properties) {
        super(UniformInt.of(2, 5), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .setId(ResourceKey.create(Registries.BLOCK,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_crystal_ore")));
    }
}
```

**注意**: 1.21.2/1.21.4 は `ResourceLocation.fromNamespaceAndPath`、1.21.5+ は同じだが他バージョンの import 順に合わせる。迷ったら同バージョンの `TemporalCoalOre.java` を参照してパターンを模倣。

- [ ] **Step 2: Build shared common module per version**

1.21.11 はスキップ(Task 6 で個別処理)。例えば:

```bash
./gradlew :common-1.21.5:compileJava -Ptarget_mc_version=1.21.5
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/1.21.2/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git add common/1.21.4/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git add common/1.21.5/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git add common/1.21.6/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git add common/1.21.7/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git add common/1.21.8/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git add common/1.21.9/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git add common/1.21.10/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git commit -m "feat: add EntropyCrystalOre block class for 1.21.2-1.21.10"
```

---

### Task 6: Create EntropyCrystalOre block class for 1.21.11 (Identifier)

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java`

- [ ] **Step 1: Use Identifier (Mojang rename in 1.21.11)**

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class EntropyCrystalOre extends DropExperienceBlock {
    public EntropyCrystalOre(BlockBehaviour.Properties properties) {
        super(UniformInt.of(2, 5), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .setId(ResourceKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_crystal_ore")));
    }
}
```

**import 検証**: 同バージョンの `TemporalCoalOre.java` で使われている Identifier の import パスを確認してから書く。

- [ ] **Step 2: Build 1.21.11**

```bash
./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/blocks/EntropyCrystalOre.java
git commit -m "feat: add EntropyCrystalOre block class for 1.21.11"
```

---

### Task 7: Register EntropyCrystalOre in ModBlocks (all versions)

**Files (per version):**
- Modify: `common/1.20.1/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.2/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.4/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.5/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.6/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.7/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.8/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.9/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/registry/ModBlocks.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java`

- [ ] **Step 1: Add EntropyCrystalOre registration near TEMPORAL_COAL_ORE in each version**

既存の TEMPORAL_COAL_ORE 登録の直後に追加(各バージョンで同じ形):

```java
    public static final RegistrySupplier<Block> ENTROPY_CRYSTAL_ORE = BLOCKS.register(
        ModBlockId.ENTROPY_CRYSTAL_ORE.id(),
        () -> new EntropyCrystalOre(EntropyCrystalOre.createProperties())
    );
```

import を追加: `import com.chronodawn.blocks.EntropyCrystalOre;`

- [ ] **Step 2: Compile all versions**

```bash
./gradlew compileJava
```

Expected: 全バージョンで BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/1.20.1/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.2/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.4/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.5/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.6/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.7/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.8/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.9/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.10/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModBlocks.java
git commit -m "feat: register EntropyCrystalOre in ModBlocks"
```

---

### Task 8: Register EntropyCrystalOre BlockItem in ModItems (all versions)

**Files (per version):**
- Modify: `common/<ver>/src/main/java/com/chronodawn/registry/ModItems.java` (全11バージョン)

- [ ] **Step 1: Add BlockItem registration**

既存の TEMPORAL_COAL_ORE BlockItem 登録の直後に追加(各バージョン):

```java
    public static final RegistrySupplier<Item> ENTROPY_CRYSTAL_ORE = ITEMS.register(
        ModItemId.ENTROPY_CRYSTAL_ORE.id(),
        () -> new BlockItem(ModBlocks.ENTROPY_CRYSTAL_ORE.get(), new Item.Properties()
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.ENTROPY_CRYSTAL_ORE.id()))))
    );
```

**バージョン差**: 1.20.1/1.21.1 は `.setId()` を使わず `new Item.Properties().arch$tab(...)` のみ。1.21.2+ は `setId()` 付き。1.21.11 は `Identifier` インポート。各バージョンで同バージョンの `TEMPORAL_COAL_ORE` BlockItem 登録箇所を参照してパターンを揃える。

- [ ] **Step 2: Add to creative tab**

`populateCreativeTab` または同等メソッド内で、`TEMPORAL_REDSTONE_ORE` の直後に追加:

```java
        output.accept(ENTROPY_CRYSTAL_ORE.get());
```

- [ ] **Step 3: Compile all versions**

```bash
./gradlew compileJava
```

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register EntropyCrystalOre block item"
```

---

### Task 9: Entropy Crystal Ore textures

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/entropy_crystal_ore.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/entropy_crystal.png`

- [ ] **Step 1: Generate block texture**

`texture-creation` skill を参照。ベース: `chronodawn:block/temporal_stone` の既存テクスチャ。オーバーレイ: 砂色〜白系の結晶斑(砂時計テーマ)。`scripts/` 配下の既存 ore rebase スクリプトがあれば利用可能。手作業の場合は GIMP/PIL 等でベーステクスチャ + 結晶ドット(色: `#e8d8a8` 薄い砂色ハイライト + `#f5eeda` 白ハイライト)。

- [ ] **Step 2: Generate item texture**

Entropy Crystal アイテムテクスチャ(16x16)。結晶の単体画像: 砂色〜白系のダイヤモンド型シルエット、中央にハイライト。既存の `time_crystal.png` を参考に色味を砂時計調に。

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/entropy_crystal_ore.png
git add common/shared/src/main/resources/assets/chronodawn/textures/item/entropy_crystal.png
git commit -m "feat: add entropy crystal textures"
```

---

### Task 10: Entropy Crystal Ore blockstate + models

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/entropy_crystal_ore.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/entropy_crystal_ore.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal_ore.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal.json`

- [ ] **Step 1: Blockstate JSON**

```json
{
  "variants": {
    "": { "model": "chronodawn:block/entropy_crystal_ore" }
  }
}
```

- [ ] **Step 2: Block model JSON**

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "chronodawn:block/entropy_crystal_ore"
  }
}
```

- [ ] **Step 3: Item model for block item**

```json
{
  "parent": "chronodawn:block/entropy_crystal_ore"
}
```

- [ ] **Step 4: Item model for entropy crystal item**

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:item/entropy_crystal"
  }
}
```

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/entropy_crystal_ore.json
git add common/shared/src/main/resources/assets/chronodawn/models/block/entropy_crystal_ore.json
git add common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal_ore.json
git add common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal.json
git commit -m "feat: add entropy crystal ore blockstate and models"
```

---

### Task 11: Client Items JSON for Entropy Crystal Ore (1.21.4+ only)

**Files:**
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/entropy_crystal_ore.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/entropy_crystal_ore.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/entropy_crystal.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/entropy_crystal.json`

- [ ] **Step 1: Find existing template**

既存の `common/1.21.4/.../assets/chronodawn/items/temporal_coal_ore.json` と `common/shared-1.21.5+/.../assets/chronodawn/items/temporal_coal_ore.json` を開き、フォーマットを確認。

- [ ] **Step 2: Create matching JSONs for entropy_crystal_ore and entropy_crystal**

典型形(temporal_coal_ore の構造に合わせる):

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "chronodawn:block/entropy_crystal_ore"
  }
}
```

アイテム版(entropy_crystal.json)は `minecraft:item/generated` 相当の参照。temporal_coal の item JSON を参照して同じ形式にする。

- [ ] **Step 3: Commit**

```bash
git add common/1.21.4/src/main/resources/assets/chronodawn/items/entropy_crystal_ore.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/entropy_crystal_ore.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/entropy_crystal.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/entropy_crystal.json
git commit -m "feat: add entropy crystal client items JSON"
```

---

### Task 12: Entropy Crystal Ore loot tables

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/loot_tables/blocks/entropy_crystal_ore.json` (1.20.1 用、**plural** directory)
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/entropy_crystal_ore.json` (1.21.1+ **singular**)

**注意**: パスに注意。1.20.1 は `loot_tables/`、1.21.1+ は `loot_table/`(単数形)。どちらのディレクトリが既存で使われているかは、既存の `temporal_coal_ore.json` の配置を探して確認。

- [ ] **Step 1: Find existing temporal_coal_ore loot table paths**

```bash
find common -name "temporal_coal_ore.json" -path "*/loot*"
```

見つかったパスをテンプレートとして使用。

- [ ] **Step 2: Create 1.20.1 loot table (old format)**

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "name": "chronodawn:entropy_crystal_ore",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "enchantments": [
                      { "enchantment": "minecraft:silk_touch", "levels": { "min": 1 } }
                    ]
                  }
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "chronodawn:entropy_crystal",
              "functions": [
                { "function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops" },
                { "function": "minecraft:explosion_decay" }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

- [ ] **Step 3: Create 1.21.1+ loot table (new nested predicates format)**

1.21.5+ で Silk Touch predicate が nested `predicates` → `minecraft:enchantments` 形式に変わる(MEMORY参照)。1.21.1+ の既存 `temporal_coal_ore.json` をコピーし、item name と drop item だけ置換する。

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "predicates": {
                      "minecraft:enchantments": [
                        { "enchantments": "minecraft:silk_touch", "levels": { "min": 1 } }
                      ]
                    }
                  }
                }
              ],
              "name": "chronodawn:entropy_crystal_ore"
            },
            {
              "type": "minecraft:item",
              "functions": [
                { "function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops" },
                { "function": "minecraft:explosion_decay" }
              ],
              "name": "chronodawn:entropy_crystal"
            }
          ]
        }
      ]
    }
  ]
}
```

**重要**: 既存の temporal_coal_ore.json と構文を完全に合わせる。微妙なキー名差異(`enchantment` vs `enchantments`, `predicate` vs `predicates`)は既存ファイルに従う。

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/loot_tables/blocks/entropy_crystal_ore.json
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/entropy_crystal_ore.json
git commit -m "feat: add entropy crystal ore loot tables"
```

---

### Task 13: Entropy Crystal Ore tags (mineable + tool tier)

**Files:**
- Modify: `common/shared/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- Modify: `common/shared/src/main/resources/data/minecraft/tags/blocks/needs_iron_tool.json`
- (1.21.1+ のパスは `tags/block/` 単数形の可能性あり — 既存の temporal_coal_ore のタグ JSON を探して確認)

- [ ] **Step 1: Find existing tag file paths**

```bash
find common -name "pickaxe.json" -path "*mineable*"
find common -name "needs_iron_tool.json"
```

- [ ] **Step 2: Add chronodawn:entropy_crystal_ore to both tags**

既存ファイルの `values` 配列末尾に追加:

```json
"chronodawn:entropy_crystal_ore"
```

**注意**: 複数ディレクトリ(shared, shared-1.21.1+, shared-1.21.2+, 1.21.11)に同じタグJSONが存在する可能性がある。既存の temporal_coal_ore のエントリが入っている全てのファイルを更新する。

- [ ] **Step 3: Commit**

```bash
git add common/**/tags/**/pickaxe.json common/**/tags/**/needs_iron_tool.json
git commit -m "feat: add entropy crystal ore to pickaxe and iron tool tags"
```

---

### Task 14: Entropy Crystal Ore worldgen (configured_feature + placed_feature)

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_entropy_crystal.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_entropy_crystal.json`

- [ ] **Step 1: Inspect existing configured_feature**

```bash
find common -name "*.json" -path "*worldgen/configured_feature*"
```

`clockstone_ore` や `temporal_iron_ore` 相当のファイルを参照。

- [ ] **Step 2: configured_feature JSON**

```json
{
  "type": "minecraft:ore",
  "config": {
    "discard_chance_on_air_exposure": 0.0,
    "size": 4,
    "targets": [
      {
        "target": {
          "predicate_type": "minecraft:block_match",
          "block": "chronodawn:temporal_stone"
        },
        "state": {
          "Name": "chronodawn:entropy_crystal_ore"
        }
      }
    ]
  }
}
```

`size`: 鉱脈サイズ(3〜5の中間値として 4)

- [ ] **Step 3: placed_feature JSON (Y 40-100, count 4)**

```json
{
  "feature": "chronodawn:ore_entropy_crystal",
  "placement": [
    { "type": "minecraft:count", "count": 4 },
    { "type": "minecraft:in_square" },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:uniform",
        "min_inclusive": { "absolute": 40 },
        "max_inclusive": { "absolute": 100 }
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_entropy_crystal.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_entropy_crystal.json
git commit -m "feat: add entropy crystal ore worldgen features"
```

---

### Task 15: Entropy Crystal Ore biome integration

**Files:**
- Modify: NeoForge biome_modifier JSON (パスは既存 temporal_iron_ore の追加箇所を確認)
- Modify: Fabric biome JSON または biome modifier(既存パターンに従う)
- Modify: 1.21.11 専用 biome override JSON(既存の temporal_iron_ore の 1.21.11 修正箇所を確認)

- [ ] **Step 1: Find biome integration files**

```bash
find common -name "*.json" | xargs grep -l "temporal_iron_ore\|ore_temporal_iron" 2>/dev/null
```

`worldgen/biome`, `biome_modifier`, `forge:biome_modifier` 等のディレクトリを確認。

- [ ] **Step 2: Add placed_feature reference to ChronoDawn biome**

既存の temporal ore features が追加されている箇所(通常 `features` 配列の `underground_ores` カテゴリ等)に、新エントリを追加:

```json
"chronodawn:ore_entropy_crystal"
```

- [ ] **Step 3: Update 1.21.11 dedicated biome override**

MEMORY: `1.21.11 biome overrides — 1.21.11 has its own biome JSON directory that must be updated independently from shared-1.21.2+`

`common/1.21.11/src/main/resources/data/chronodawn/worldgen/biome/` (または類似) の ChronoDawn biome JSON にも同じエントリを追加。

- [ ] **Step 4: Commit**

```bash
git add <modified biome JSON files>
git commit -m "feat: integrate entropy crystal ore into chronodawn biome"
```

---

### Task 16: Entropy Crystal Ore lang entries

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`

- [ ] **Step 1: Add Japanese entries**

```json
  "block.chronodawn.entropy_crystal_ore": "エントロピークリスタル鉱石",
  "item.chronodawn.entropy_crystal": "エントロピークリスタル",
```

- [ ] **Step 2: Add English entries**

```json
  "block.chronodawn.entropy_crystal_ore": "Entropy Crystal Ore",
  "item.chronodawn.entropy_crystal": "Entropy Crystal",
```

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat: add entropy crystal ore translations"
```

---

### Task 17: Verify Entropy Crystal Ore for 1.21.11

- [ ] **Step 1: Build for 1.21.11**

```bash
./gradlew clean1_21_11 build1_21_11
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run resource validation**

```bash
./gradlew validateResources
./gradlew validateTranslations
```

Expected: 両方とも PASS

- [ ] **Step 3: Run client for 1.21.11 (Fabric) — 手動確認**

```bash
./gradlew runClientFabric1_21_11
```

確認事項:
- Creative タブに Entropy Crystal Ore が表示される
- ChronoDawn ディメンションに入って Y=40〜100 で鉱石を発見
- Iron Pickaxe で破壊して Entropy Crystal がドロップ
- Silk Touch で鉱石ブロックが回収される
- 日本語テキストが正しく表示

プレイ確認が完了したら次のタスクへ。

---

## Phase 3: Entropy MobEffect + Entropy Crystal Sword

### Task 18: Create EntropyEffect class (all versions)

**Files:**
- Create: `common/1.20.1/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.2/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.4/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.5/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.6/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.7/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.8/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.9/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.10/src/main/java/com/chronodawn/effects/EntropyEffect.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/effects/EntropyEffect.java`

- [ ] **Step 1: Inspect TimeDistortionEffect or ChronoAegisEffect for the target version**

既存の `common/<ver>/src/main/java/com/chronodawn/effects/*.java` を参照して、MobEffect のクラス形状を確認。ダメージ系の MobEffect 実装は `applyEffectTick` で `entity.hurt(...)` を呼ぶ。

- [ ] **Step 2: Create EntropyEffect (1.21.11 version, example)**

```java
package com.chronodawn.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Entropy MobEffect - continuous damage over time applied by Entropy Crystal Sword.
 *
 * Ticks once per second and deals 1 damage using generic damage source.
 * Bypasses no armor; regular damage calculation applies.
 */
public class EntropyEffect extends MobEffect {
    public EntropyEffect() {
        super(MobEffectCategory.HARMFUL, 0xc7a97a); // tan/sand color
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Tick once per second (20 ticks)
        int interval = 20;
        return duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.isDeadOrDying()) {
            return false;
        }
        // Use a generic magic damage source so that armor provides some reduction
        DamageSource source = entity.damageSources().magic();
        entity.hurt(source, 1.0f);
        return true;
    }
}
```

**バージョン差**:
- 1.20.1: `applyEffectTick(LivingEntity entity, int amplifier)` — ServerLevelなし
- 1.21.1+: `applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier)` — ServerLevel追加
- 1.21.11: 同じだが import path が異なる可能性あり、既存の同バージョンエフェクトを参照

各バージョンで `DamageSource`, `damageSources()` の有無は 1.20.1 で異なる可能性 — 1.20.1 は `DamageSource.GENERIC` 直接使用、1.21.1+ は `entity.damageSources().generic()` 相当。各バージョンの既存エフェクトクラスを参照。

- [ ] **Step 3: Commit per version group**

```bash
git add common/*/src/main/java/com/chronodawn/effects/EntropyEffect.java
git commit -m "feat: add EntropyEffect mob effect for all versions"
```

---

### Task 19: Register EntropyEffect in ModEffects (all versions)

**Files:**
- Modify: `common/<ver>/src/main/java/com/chronodawn/registry/ModEffects.java` (全11バージョン)

- [ ] **Step 1: Add registration**

既存の `TIME_DISTORTION` または `CHRONO_AEGIS_BUFF` の直後に追加(各バージョン):

```java
    public static final RegistrySupplier<MobEffect> ENTROPY = EFFECTS.register(
        "entropy",
        EntropyEffect::new
    );
```

import 追加: `import com.chronodawn.effects.EntropyEffect;`

- [ ] **Step 2: Compile all**

```bash
./gradlew compileJava
```

- [ ] **Step 3: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/registry/ModEffects.java
git commit -m "feat: register EntropyEffect in ModEffects"
```

---

### Task 20: Add EntropyEffect lang entries

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`

- [ ] **Step 1: Add entries**

ja_jp.json:
```json
  "effect.chronodawn.entropy": "エントロピー",
```

en_us.json:
```json
  "effect.chronodawn.entropy": "Entropy",
```

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat: add entropy effect translations"
```

---

### Task 21: Create EntropyCrystalTier (all versions)

**Files:**
- Create: `common/1.20.1/src/main/java/com/chronodawn/items/equipment/EntropyCrystalTier.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/items/equipment/EntropyCrystalTier.java`
- ... (全11バージョン)

- [ ] **Step 1: Inspect existing ClockstoneTier per version**

1.20.1/1.21.1/1.21.2/1.21.4 は `Tier` インターフェースまたは `SimpleTier`、1.21.5+ は `ToolMaterial`(record)に変化している可能性あり。既存のClockstoneTierをバージョンごとに確認してコピー元とする。

- [ ] **Step 2: Create EntropyCrystalTier (Clockstone 相当、攻撃力も同等)**

**1.20.1/1.21.1 例 (Tier class)**:

```java
package com.chronodawn.items.equipment;

import com.chronodawn.registry.ModTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;

public class EntropyCrystalTier implements Tier {
    public static final EntropyCrystalTier INSTANCE = new EntropyCrystalTier();

    @Override public int getUses() { return 400; }
    @Override public float getSpeed() { return 6.5f; }
    @Override public float getAttackDamageBonus() { return 2.5f; }
    @Override public int getLevel() { return 2; } // Iron tier
    @Override public int getEnchantmentValue() { return 14; }
    @Override public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
        return net.minecraft.world.item.crafting.Ingredient.of(
            com.chronodawn.registry.ModItems.ENTROPY_CRYSTAL.get()
        );
    }
}
```

**1.21.5+ 例 (ToolMaterial record/class)**:

既存の1.21.5+ ClockstoneTier の形状を参照。`ToolMaterial INSTANCE = new ToolMaterial(...)` 形式の場合、以下のように定数化:

```java
package com.chronodawn.items.equipment;

import com.chronodawn.registry.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public final class EntropyCrystalTier {
    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            400,
            6.5f,
            2.5f,
            14,
            ModTags.Items.ENTROPY_CRYSTAL_REPAIR_MATERIAL  // or Ingredient equivalent
    );
    private EntropyCrystalTier() {}
}
```

**重要**: 実際のシグネチャは既存の ClockstoneTier を確認して合わせる。repair material は Ingredient か Tag。

- [ ] **Step 3: Compile per version group**

```bash
./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11
```

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/items/equipment/EntropyCrystalTier.java
git commit -m "feat: add EntropyCrystalTier for all versions"
```

---

### Task 22: Create EntropyCrystalSwordItem (all versions)

**Files:**
- Create: `common/<ver>/src/main/java/com/chronodawn/items/equipment/EntropyCrystalSwordItem.java` (全11バージョン)

- [ ] **Step 1: Inspect EnhancedClockstoneSwordItem for hurtEnemy pattern**

`common/<ver>/src/main/java/com/chronodawn/items/equipment/EnhancedClockstoneSwordItem.java` を参照。

- [ ] **Step 2: Create EntropyCrystalSwordItem (1.21.11 example)**

```java
package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Entropy Crystal Sword - Tier 2 specialized weapon.
 *
 * Stats comparable to Clockstone Sword but with DoT effect:
 * On hit, applies Entropy effect for 5 seconds (1 damage/sec).
 */
public class EntropyCrystalSwordItem extends Item {
    private static final float ATTACK_DAMAGE_BONUS = 3.0f; // Clockstone Sword equivalent
    private static final float ATTACK_SPEED_MODIFIER = -2.4f;
    private static final int ENTROPY_DURATION_TICKS = 100; // 5 seconds

    public EntropyCrystalSwordItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        Identifier itemId = Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_crystal_sword");
        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                .sword(EntropyCrystalTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(
            ModEffects.ENTROPY.get(),
            ENTROPY_DURATION_TICKS,
            0,          // amplifier I
            false,      // ambient
            true        // visible particles
        ));
        super.hurtEnemy(stack, target, attacker);
    }
}
```

**バージョン差**:
- 1.20.1/1.21.1/1.21.2/1.21.4: `extends SwordItem` を継承。`public EntropyCrystalSwordItem(Properties properties) { super(EntropyCrystalTier.INSTANCE, properties.attributes(SwordItem.createAttributes(EntropyCrystalTier.INSTANCE, 3, -2.4f))); }`
- 1.21.5+: `extends Item` + `Item.Properties#sword()` を使う
- 1.21.11: `Identifier` import

各バージョンで同バージョンの `ClockstoneSwordItem` or `EnhancedClockstoneSwordItem` のシグネチャに合わせる。

**注意: hurtEnemy シグネチャ**:
- 1.20.1/1.21.1: `public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)` — 返値 boolean
- 1.21.5+: `public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)` または `hurtEnemy` のままの場合あり — 既存クラスを確認

- [ ] **Step 3: Compile all**

```bash
./gradlew compileJava
```

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/items/equipment/EntropyCrystalSwordItem.java
git commit -m "feat: add EntropyCrystalSwordItem for all versions"
```

---

### Task 23: Register EntropyCrystalSwordItem in ModItems (all versions)

**Files:**
- Modify: `common/*/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Add registration near CLOCKSTONE_SWORD**

```java
    public static final RegistrySupplier<Item> ENTROPY_CRYSTAL_SWORD = ITEMS.register(
        ModItemId.ENTROPY_CRYSTAL_SWORD.id(),
        () -> new EntropyCrystalSwordItem(EntropyCrystalSwordItem.createProperties()
                .arch$tab(ModCreativeTabs.CHRONO_DAWN))
    );
```

1.20.1/1.21.1 は `new EntropyCrystalSwordItem(new Item.Properties().arch$tab(...))` (SwordItem継承のコンストラクタ引数)。

- [ ] **Step 2: Register Entropy Crystal item (raw material)**

```java
    public static final RegistrySupplier<Item> ENTROPY_CRYSTAL = ITEMS.register(
        ModItemId.ENTROPY_CRYSTAL.id(),
        () -> new Item(new Item.Properties()
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.ENTROPY_CRYSTAL.id()))))
    );
```

- [ ] **Step 3: Add to creative tab population**

```java
        output.accept(ENTROPY_CRYSTAL.get());
        output.accept(ENTROPY_CRYSTAL_SWORD.get());
```

- [ ] **Step 4: Compile + Commit**

```bash
./gradlew compileJava
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register entropy crystal item and sword"
```

---

### Task 24: Entropy Crystal Sword textures and model

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/entropy_crystal_sword.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal_sword.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/entropy_crystal_sword.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/entropy_crystal_sword.json`

- [ ] **Step 1: Create sword texture (16x16)**

既存の `clockstone_sword.png` を参考に、刃を砂色〜白のグラデーションに。柄は既存剣と統一。

- [ ] **Step 2: Create item model**

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "chronodawn:item/entropy_crystal_sword"
  }
}
```

- [ ] **Step 3: Create Client Items JSON (1.21.4, shared-1.21.5+)**

既存の `clockstone_sword.json` client items を参照してコピー&編集。

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/item/entropy_crystal_sword.png
git add common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal_sword.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/entropy_crystal_sword.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/entropy_crystal_sword.json
git commit -m "feat: add entropy crystal sword textures and model"
```

---

### Task 25: Entropy Crystal Sword recipe

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/recipe/entropy_crystal_sword.json` (1.21.1+)
- Create: `common/shared/src/main/resources/data/chronodawn/recipes/entropy_crystal_sword.json` (1.20.1、複数形の場合)

- [ ] **Step 1: Find existing recipe directory**

```bash
find common -name "clockstone_sword.json" -path "*recipe*"
```

1.20.1 は `recipes/`、1.21.1+ は `recipe/` の可能性。既存パターンに合わせる。

- [ ] **Step 2: Create shaped recipe**

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": [
    "X",
    "X",
    "I"
  ],
  "key": {
    "X": { "item": "chronodawn:entropy_crystal" },
    "I": { "item": "minecraft:stick" }
  },
  "result": {
    "id": "chronodawn:entropy_crystal_sword",
    "count": 1
  }
}
```

1.20.1 では `"result": { "item": "chronodawn:entropy_crystal_sword" }` 形式の可能性あり。既存 recipe に合わせる。

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/recipe*/entropy_crystal_sword.json
git commit -m "feat: add entropy crystal sword recipe"
```

---

### Task 26: Entropy Crystal Sword lang entries

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json`
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`

- [ ] **Step 1: Add entries**

ja_jp.json:
```json
  "item.chronodawn.entropy_crystal_sword": "エントロピークリスタルソード",
```

en_us.json:
```json
  "item.chronodawn.entropy_crystal_sword": "Entropy Crystal Sword",
```

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat: add entropy crystal sword translations"
```

---

### Task 27: Verify Entropy Crystal Sword end-to-end (1.21.11)

- [ ] **Step 1: Build 1.21.11**

```bash
./gradlew clean1_21_11 build1_21_11
```

- [ ] **Step 2: validateResources + validateTranslations**

```bash
./gradlew validateResources validateTranslations
```

- [ ] **Step 3: Manual client test**

```bash
./gradlew runClientFabric1_21_11
```

確認事項:
- Creative タブに Entropy Crystal Sword が表示
- クラフトテーブルで Entropy Crystal x3 + 棒 x1 → 剣1本
- モブを攻撃して Entropy エフェクトが付与される(HUD のエフェクトアイコン、砂色)
- 5秒間毎秒1ダメージが入る

---

## Phase 4: Temporal Amber Ore Block

Phase 2 と同じ構造を踏襲する。Phase 2 との差分のみを記述する。

### Task 28: Create TemporalAmberOre block classes (all versions, same structure as Task 3-6)

**Files**: `common/<ver>/src/main/java/com/chronodawn/blocks/TemporalAmberOre.java` (全11バージョン)

- [ ] **Step 1: Copy EntropyCrystalOre.java per version and rename**

変更点:
- クラス名: `EntropyCrystalOre` → `TemporalAmberOre`
- ID 文字列: `"entropy_crystal_ore"` → `"temporal_amber_ore"`
- XP drop: `UniformInt.of(2, 5)` はそのまま
- Properties: 同じ(STONE base)

- [ ] **Step 2: Compile all versions**

```bash
./gradlew compileJava
```

- [ ] **Step 3: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/blocks/TemporalAmberOre.java
git commit -m "feat: add TemporalAmberOre block class for all versions"
```

---

### Task 29: Register TemporalAmberOre in ModBlocks/ModItems (all versions)

Task 7, 8 と同じ手順。

- [ ] **Step 1: Add registration to ModBlocks.java**

```java
    public static final RegistrySupplier<Block> TEMPORAL_AMBER_ORE = BLOCKS.register(
        ModBlockId.TEMPORAL_AMBER_ORE.id(),
        () -> new TemporalAmberOre(TemporalAmberOre.createProperties())
    );
```

- [ ] **Step 2: Add BlockItem to ModItems.java**

```java
    public static final RegistrySupplier<Item> TEMPORAL_AMBER_ORE = ITEMS.register(
        ModItemId.TEMPORAL_AMBER_ORE.id(),
        () -> new BlockItem(ModBlocks.TEMPORAL_AMBER_ORE.get(), new Item.Properties()
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(...))
    );
```

- [ ] **Step 3: Add to creative tab**

```java
        output.accept(TEMPORAL_AMBER_ORE.get());
```

- [ ] **Step 4: Compile + commit**

```bash
./gradlew compileJava
git add common/*/src/main/java/com/chronodawn/registry/ModBlocks.java
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register TemporalAmberOre block and item"
```

---

### Task 30: Temporal Amber Ore textures

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_amber_ore.png`

- [ ] **Step 1: Generate texture**

ベース: `chronodawn:block/temporal_stone`。オーバーレイ: 琥珀色(`#c68320`〜`#f4a83a`)の樹脂斑。深層感を出すため背景をやや暗めに調整。

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_amber_ore.png
git commit -m "feat: add temporal amber ore texture"
```

---

### Task 31: Temporal Amber Ore blockstate + models + client items JSON

Task 10, 11 と同じ構造。

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_amber_ore.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/block/temporal_amber_ore.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_amber_ore.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_amber_ore.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_amber_ore.json`

内容は EntropyCrystalOre 版の `entropy_crystal_ore` を `temporal_amber_ore` に置換するだけ。

- [ ] **Step 1: Create all JSONs**
- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/blockstates/temporal_amber_ore.json
git add common/shared/src/main/resources/assets/chronodawn/models/block/temporal_amber_ore.json
git add common/shared/src/main/resources/assets/chronodawn/models/item/temporal_amber_ore.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_amber_ore.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_amber_ore.json
git commit -m "feat: add temporal amber ore blockstate/models/items"
```

---

### Task 32: Temporal Amber Ore loot tables

Task 12 と同じ構造。ドロップは `chronodawn:raw_temporal_amber`。

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_amber_ore.json` (1.20.1)
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_amber_ore.json` (1.21.1+)

- [ ] **Step 1: Copy entropy_crystal_ore loot table and replace**

`chronodawn:entropy_crystal_ore` → `chronodawn:temporal_amber_ore`
`chronodawn:entropy_crystal` → `chronodawn:raw_temporal_amber`

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/loot_tables/blocks/temporal_amber_ore.json
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/temporal_amber_ore.json
git commit -m "feat: add temporal amber ore loot tables"
```

---

### Task 33: Temporal Amber Ore tags

Task 13 と同じ。`chronodawn:temporal_amber_ore` を mineable/pickaxe と needs_iron_tool に追加。

- [ ] **Step 1: Update tag JSONs**
- [ ] **Step 2: Commit**

```bash
git add common/**/tags/**/pickaxe.json common/**/tags/**/needs_iron_tool.json
git commit -m "feat: add temporal amber ore to pickaxe and iron tool tags"
```

---

### Task 34: Temporal Amber Ore worldgen (Y -30 to 20)

Task 14 と同じ構造、Y帯が異なる。

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_temporal_amber.json`
- Create: `common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_temporal_amber.json`

- [ ] **Step 1: configured_feature (size 3)**

```json
{
  "type": "minecraft:ore",
  "config": {
    "discard_chance_on_air_exposure": 0.0,
    "size": 3,
    "targets": [
      {
        "target": {
          "predicate_type": "minecraft:block_match",
          "block": "chronodawn:temporal_stone"
        },
        "state": {
          "Name": "chronodawn:temporal_amber_ore"
        }
      }
    ]
  }
}
```

- [ ] **Step 2: placed_feature (Y -30 to 20, count 4)**

```json
{
  "feature": "chronodawn:ore_temporal_amber",
  "placement": [
    { "type": "minecraft:count", "count": 4 },
    { "type": "minecraft:in_square" },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:uniform",
        "min_inclusive": { "absolute": -30 },
        "max_inclusive": { "absolute": 20 }
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ore_temporal_amber.json
git add common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_temporal_amber.json
git commit -m "feat: add temporal amber ore worldgen features"
```

---

### Task 35: Temporal Amber Ore biome integration

Task 15 と同じ。1.21.11 専用 biome override も更新する。

- [ ] **Step 1: Add ore_temporal_amber reference to all relevant biome JSONs**
- [ ] **Step 2: Commit**

```bash
git add <modified biome JSON files>
git commit -m "feat: integrate temporal amber ore into chronodawn biome"
```

---

### Task 36: Temporal Amber Ore lang entries

- [ ] **Step 1: Add entries**

ja_jp.json:
```json
  "block.chronodawn.temporal_amber_ore": "時の琥珀鉱石",
```

en_us.json:
```json
  "block.chronodawn.temporal_amber_ore": "Temporal Amber Ore",
```

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat: add temporal amber ore translations"
```

---

### Task 37: Verify Temporal Amber Ore in 1.21.11

- [ ] **Step 1: Build and run client**

```bash
./gradlew clean1_21_11 build1_21_11 validateResources validateTranslations
./gradlew runClientFabric1_21_11
```

- [ ] **Step 2: Confirm**

- Creative タブに表示
- Y -30〜20 で生成される
- Iron Pickaxe でドロップ: Raw Temporal Amber

---

## Phase 5: Raw Temporal Amber Item + Temporal Amber Dust + Crafting

### Task 38: Register Raw Temporal Amber item (all versions)

**Files:**
- Modify: `common/*/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Add registration**

```java
    public static final RegistrySupplier<Item> RAW_TEMPORAL_AMBER = ITEMS.register(
        ModItemId.RAW_TEMPORAL_AMBER.id(),
        () -> new Item(new Item.Properties()
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.RAW_TEMPORAL_AMBER.id()))))
    );
```

- [ ] **Step 2: Add to creative tab output**

```java
        output.accept(RAW_TEMPORAL_AMBER.get());
```

- [ ] **Step 3: Compile + Commit**

```bash
./gradlew compileJava
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register raw temporal amber item"
```

---

### Task 39: Register Temporal Amber Dust item (all versions)

同じパターンで `TEMPORAL_AMBER_DUST` を追加。

- [ ] **Step 1: Add registration**

```java
    public static final RegistrySupplier<Item> TEMPORAL_AMBER_DUST = ITEMS.register(
        ModItemId.TEMPORAL_AMBER_DUST.id(),
        () -> new Item(new Item.Properties()
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(...))
    );
```

- [ ] **Step 2: Add to creative tab**

- [ ] **Step 3: Compile + Commit**

```bash
./gradlew compileJava
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register temporal amber dust item"
```

---

### Task 40: Textures and models for raw amber + dust

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/raw_temporal_amber.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_amber_dust.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/raw_temporal_amber.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_amber_dust.json`
- Create: `common/1.21.4/.../items/raw_temporal_amber.json`, `temporal_amber_dust.json`
- Create: `common/shared-1.21.5+/.../items/raw_temporal_amber.json`, `temporal_amber_dust.json`

- [ ] **Step 1: Create textures**

raw_temporal_amber: 琥珀色の樹脂塊(16x16)。不定形シルエット。
temporal_amber_dust: 金色系の粉末(16x16)。既存の `glowstone_dust` 風のパーティクル配置で色を琥珀に。

- [ ] **Step 2: Create item models**

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:item/raw_temporal_amber"
  }
}
```

dust 側も同形式。

- [ ] **Step 3: Create Client Items JSON (1.21.4, shared-1.21.5+)**

既存の temporal_iron 相当の raw_temporal_iron.json があれば参照してコピー。

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/item/raw_temporal_amber.png
git add common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_amber_dust.png
git add common/shared/src/main/resources/assets/chronodawn/models/item/raw_temporal_amber.json
git add common/shared/src/main/resources/assets/chronodawn/models/item/temporal_amber_dust.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/raw_temporal_amber.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_amber_dust.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/raw_temporal_amber.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_amber_dust.json
git commit -m "feat: add raw temporal amber and dust textures/models"
```

---

### Task 41: Recipe: Raw Temporal Amber → Temporal Amber Dust x2

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/recipe/temporal_amber_dust.json` (1.21.1+)
- Create: `common/shared/src/main/resources/data/chronodawn/recipes/temporal_amber_dust.json` (1.20.1 if separate)

- [ ] **Step 1: Create shapeless recipe (1:2 ratio)**

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    { "item": "chronodawn:raw_temporal_amber" }
  ],
  "result": {
    "id": "chronodawn:temporal_amber_dust",
    "count": 2
  }
}
```

1.20.1 の result フォーマットは `{"item": "...", "count": 2}` の可能性。既存 recipe を確認。

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/recipe*/temporal_amber_dust.json
git commit -m "feat: add temporal amber dust crafting recipe"
```

---

### Task 42: Lang entries for raw amber + dust

- [ ] **Step 1: Add**

ja_jp.json:
```json
  "item.chronodawn.raw_temporal_amber": "粗時琥珀",
  "item.chronodawn.temporal_amber_dust": "時琥珀の粉",
```

en_us.json:
```json
  "item.chronodawn.raw_temporal_amber": "Raw Temporal Amber",
  "item.chronodawn.temporal_amber_dust": "Temporal Amber Dust",
```

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json
git add common/shared/src/main/resources/assets/chronodawn/lang/en_us.json
git commit -m "feat: add raw temporal amber and dust translations"
```

---

### Task 43: Verify amber items + dust crafting (1.21.11)

- [ ] **Step 1: Build**

```bash
./gradlew clean1_21_11 build1_21_11 validateResources validateTranslations
./gradlew runClientFabric1_21_11
```

- [ ] **Step 2: Manual test**

- Temporal Amber Ore を採掘 → Raw Temporal Amber ドロップ
- クラフトテーブルで Raw Temporal Amber 1個 → Temporal Amber Dust 2個
- 各アイテムが正しい名前で表示

---

## Phase 6: Temporal Amber Armor Material + Items

### Task 44: Create TemporalAmberArmorMaterial (all versions)

**Files:**
- Create: `common/<ver>/src/main/java/com/chronodawn/items/equipment/TemporalAmberArmorMaterial.java` (全11バージョン)

- [ ] **Step 1: Inspect EnhancedClockstoneArmorMaterial for version**

1.20.1: 古い `ArmorMaterial` interface 実装
1.21.1/1.21.2/1.21.4: `Holder<ArmorMaterial>`
1.21.5+: 新しい record ベース ArmorMaterial + equipment asset key

- [ ] **Step 2: Create material (1.21.11 example)**

```java
package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModTags;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.EnumMap;

public final class TemporalAmberArmorMaterial {
    private static final int DURABILITY_MULTIPLIER = 35; // 28 * 1.25 ≒ 35

    public static final Holder<ArmorMaterial> TEMPORAL_AMBER = Holder.direct(
        new ArmorMaterial(
            DURABILITY_MULTIPLIER,
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 3);
                map.put(ArmorType.LEGGINGS, 6);
                map.put(ArmorType.CHESTPLATE, 7);
                map.put(ArmorType.HELMET, 3);
            }),
            10,  // enchantability (diamond tier)
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            1.5f,  // toughness
            0.0f,  // knockback resistance
            ModTags.Items.TEMPORAL_AMBER_REPAIR_MATERIAL,  // or Ingredient-based
            CompatResourceLocation.createEquipmentAssetKey(ChronoDawn.MOD_ID, "temporal_amber")
        )
    );

    private TemporalAmberArmorMaterial() {}
}
```

**注意**:
- `ModTags.Items.TEMPORAL_AMBER_REPAIR_MATERIAL` は ModTags に定義が必要になる可能性あり。既存の `TIME_CRYSTAL_TAG` と同じ形で追加。または Tag を使わず `Ingredient.of(ModItems.RAW_TEMPORAL_AMBER.get())` を直接使う形の方がバージョンによって簡単。
- 各バージョンで既存 `EnhancedClockstoneArmorMaterial` を参照して正確なシグネチャを確認

- [ ] **Step 3: Compile per version**

```bash
./gradlew compileJava
```

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/items/equipment/TemporalAmberArmorMaterial.java
git commit -m "feat: add TemporalAmberArmorMaterial for all versions"
```

---

### Task 45: Create TemporalAmberArmorItem class (all versions, no repair logic yet)

**Files:**
- Create: `common/<ver>/src/main/java/com/chronodawn/items/equipment/TemporalAmberArmorItem.java` (全11バージョン)

- [ ] **Step 1: Create basic class (repair logic added in Phase 7)**

1.21.11 例:

```java
package com.chronodawn.items.equipment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public class TemporalAmberArmorItem extends Item {
    public TemporalAmberArmorItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties(ArmorType type) {
        return new Properties()
                .stacksTo(1)
                .humanoidArmor(TemporalAmberArmorMaterial.TEMPORAL_AMBER.value(), type);
    }

    public static boolean isWearingFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof TemporalAmberArmorItem;
    }
}
```

**1.20.1/1.21.1 version** は `ArmorItem` 継承:

```java
public class TemporalAmberArmorItem extends ArmorItem {
    public TemporalAmberArmorItem(Type type, Properties properties) {
        super(TemporalAmberArmorMaterial.TEMPORAL_AMBER.value(), type, properties);
    }

    public static Properties createProperties(Type type) {
        int durability = switch (type) {
            case HELMET -> 385;
            case CHESTPLATE -> 560;
            case LEGGINGS -> 525;
            case BOOTS -> 455;
        };
        return new Properties().stacksTo(1).durability(durability);
    }
}
```

各バージョンで `EnhancedClockstoneArmorItem` の形状に合わせる。

- [ ] **Step 2: Compile all**

```bash
./gradlew compileJava
```

- [ ] **Step 3: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/items/equipment/TemporalAmberArmorItem.java
git commit -m "feat: add TemporalAmberArmorItem for all versions"
```

---

### Task 46: Register 4 armor pieces in ModItems (all versions)

**Files:**
- Modify: `common/*/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Add 4 registrations per version**

1.21.11 例:

```java
    public static final RegistrySupplier<Item> TEMPORAL_AMBER_HELMET = ITEMS.register(
        ModItemId.TEMPORAL_AMBER_HELMET.id(),
        () -> new TemporalAmberArmorItem(TemporalAmberArmorItem.createProperties(ArmorType.HELMET)
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.TEMPORAL_AMBER_HELMET.id()))))
    );

    public static final RegistrySupplier<Item> TEMPORAL_AMBER_CHESTPLATE = ITEMS.register(
        ModItemId.TEMPORAL_AMBER_CHESTPLATE.id(),
        () -> new TemporalAmberArmorItem(TemporalAmberArmorItem.createProperties(ArmorType.CHESTPLATE)
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(...))
    );

    public static final RegistrySupplier<Item> TEMPORAL_AMBER_LEGGINGS = ITEMS.register(
        ModItemId.TEMPORAL_AMBER_LEGGINGS.id(),
        () -> new TemporalAmberArmorItem(TemporalAmberArmorItem.createProperties(ArmorType.LEGGINGS)
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(...))
    );

    public static final RegistrySupplier<Item> TEMPORAL_AMBER_BOOTS = ITEMS.register(
        ModItemId.TEMPORAL_AMBER_BOOTS.id(),
        () -> new TemporalAmberArmorItem(TemporalAmberArmorItem.createProperties(ArmorType.BOOTS)
                .arch$tab(ModCreativeTabs.CHRONO_DAWN)
                .setId(...))
    );
```

1.20.1/1.21.1 は `ArmorItem.Type.HELMET` 等を使う(`ArmorType` でなく)。

- [ ] **Step 2: Add to creative tab**

```java
        output.accept(TEMPORAL_AMBER_HELMET.get());
        output.accept(TEMPORAL_AMBER_CHESTPLATE.get());
        output.accept(TEMPORAL_AMBER_LEGGINGS.get());
        output.accept(TEMPORAL_AMBER_BOOTS.get());
        output.accept(TEMPORAL_AMBER_DUST.get());
```

- [ ] **Step 3: Compile + Commit**

```bash
./gradlew compileJava
git add common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: register temporal amber armor pieces"
```

---

### Task 47: Armor textures (item icons + layer textures)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_amber_helmet.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_amber_chestplate.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_amber_leggings.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_amber_boots.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/models/armor/temporal_amber_layer_1.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/models/armor/temporal_amber_layer_2.png`

- [ ] **Step 1: Create item icon textures (16x16)**

既存 `enhanced_clockstone_helmet/chestplate/leggings/boots.png` を参考に色を琥珀色に変更(GIMP/PIL での色相シフト)。

- [ ] **Step 2: Create armor layer textures**

既存 `enhanced_clockstone_layer_1.png` と `layer_2.png` を参考に琥珀色に変換。サイズ: layer_1 は 64x32、layer_2 は 64x32(vanilla layered armor format)。

- [ ] **Step 3: Equipment asset JSON (1.21.5+ for humanoidArmor)**

1.21.5+ は `equipment/chronodawn/temporal_amber.json` が必要。既存の `enhanced_clockstone.json` をコピーして texture reference を temporal_amber に変更。

```json
{
  "layers": {
    "humanoid": [
      { "texture": "chronodawn:temporal_amber" }
    ],
    "humanoid_leggings": [
      { "texture": "chronodawn:temporal_amber" }
    ]
  }
}
```

配置パス: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/equipment/temporal_amber.json` または `common/shared/src/main/resources/assets/chronodawn/models/equipment/temporal_amber.json` — 既存 enhanced_clockstone.json の配置を確認。

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/item/temporal_amber_*.png
git add common/shared/src/main/resources/assets/chronodawn/textures/models/armor/temporal_amber_layer_*.png
git add common/*/src/main/resources/assets/chronodawn/equipment/temporal_amber.json
git commit -m "feat: add temporal amber armor textures"
```

---

### Task 48: Armor item models + Client Items JSON

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/temporal_amber_helmet.json`
- Create: ... chestplate, leggings, boots
- Create: `common/1.21.4/.../items/temporal_amber_{helmet,chestplate,leggings,boots}.json`
- Create: `common/shared-1.21.5+/.../items/temporal_amber_{helmet,chestplate,leggings,boots}.json`

- [ ] **Step 1: Item models (generic generated)**

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:item/temporal_amber_helmet"
  }
}
```

同形式で chestplate/leggings/boots。

- [ ] **Step 2: Client Items JSON (copy enhanced_clockstone pattern)**

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/item/temporal_amber_*.json
git add common/1.21.4/src/main/resources/assets/chronodawn/items/temporal_amber_*.json
git add common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/temporal_amber_*.json
git commit -m "feat: add temporal amber armor item models and client items"
```

---

### Task 49: Armor recipes

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/recipe/temporal_amber_helmet.json`
- Create: ... chestplate, leggings, boots

- [ ] **Step 1: Shaped recipes**

helmet:
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": ["XXX", "X X", "   "],
  "key": { "X": { "item": "chronodawn:raw_temporal_amber" } },
  "result": { "id": "chronodawn:temporal_amber_helmet", "count": 1 }
}
```

chestplate:
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": ["X X", "XXX", "XXX"],
  "key": { "X": { "item": "chronodawn:raw_temporal_amber" } },
  "result": { "id": "chronodawn:temporal_amber_chestplate", "count": 1 }
}
```

leggings:
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": ["XXX", "X X", "X X"],
  "key": { "X": { "item": "chronodawn:raw_temporal_amber" } },
  "result": { "id": "chronodawn:temporal_amber_leggings", "count": 1 }
}
```

boots:
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": ["   ", "X X", "X X"],
  "key": { "X": { "item": "chronodawn:raw_temporal_amber" } },
  "result": { "id": "chronodawn:temporal_amber_boots", "count": 1 }
}
```

1.20.1 は `"result": {"item": "...", "count": 1}` の可能性あり。既存 recipe で確認。

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/recipe/temporal_amber_*.json
git commit -m "feat: add temporal amber armor recipes"
```

---

### Task 50: Armor lang entries

- [ ] **Step 1: Add**

ja_jp.json:
```json
  "item.chronodawn.temporal_amber_helmet": "時琥珀の兜",
  "item.chronodawn.temporal_amber_chestplate": "時琥珀の胸当て",
  "item.chronodawn.temporal_amber_leggings": "時琥珀のレギンス",
  "item.chronodawn.temporal_amber_boots": "時琥珀のブーツ",
```

en_us.json:
```json
  "item.chronodawn.temporal_amber_helmet": "Temporal Amber Helmet",
  "item.chronodawn.temporal_amber_chestplate": "Temporal Amber Chestplate",
  "item.chronodawn.temporal_amber_leggings": "Temporal Amber Leggings",
  "item.chronodawn.temporal_amber_boots": "Temporal Amber Boots",
```

- [ ] **Step 2: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: add temporal amber armor translations"
```

---

### Task 51: Verify Temporal Amber armor (without repair) in 1.21.11

- [ ] **Step 1: Build + run**

```bash
./gradlew clean1_21_11 build1_21_11 validateResources validateTranslations
./gradlew runClientFabric1_21_11
```

- [ ] **Step 2: Confirm**

- 4部位クラフト可能
- 装備時に琥珀色のレイヤーが表示される
- 防御値・耐久が想定通り
- まだ自動修復なし(Phase 7 で追加)

---

## Phase 7: Temporal Amber Auto-repair Logic

### Task 52: Add inventoryTick override to TemporalAmberArmorItem (all versions)

**Files:**
- Modify: `common/*/src/main/java/com/chronodawn/items/equipment/TemporalAmberArmorItem.java`

- [ ] **Step 1: Add repair constants and inventoryTick (1.21.11 example)**

既存 class に追加:

```java
    private static final int REPAIR_INTERVAL_TICKS = 60;        // 3 seconds
    private static final int COMBAT_COOLDOWN_TICKS = 200;       // 10 seconds
    private static final int REPAIR_AMOUNT_PER_PIECE = 5;
    private static final int FULL_SET_PIECES = 4;

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;

        // HEAD スロットでのみ実行して重複回避
        if (player.getItemBySlot(EquipmentSlot.HEAD) != stack) return;

        // フルセット判定
        if (!isWearingFullSet(player)) return;

        // 発動タイミング(3秒ごと)
        if (level.getGameTime() % REPAIR_INTERVAL_TICKS != 0) return;

        // 戦闘外判定(最後の被ダメージから10秒経過)
        int lastHurtTick = player.getLastHurtByMobTimestamp();
        if (level.getGameTime() - lastHurtTick < COMBAT_COOLDOWN_TICKS) return;

        // 全装備が満タンか確認
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        boolean anyDamaged = head.getDamageValue() > 0 || chest.getDamageValue() > 0
                || legs.getDamageValue() > 0 || feet.getDamageValue() > 0;
        if (!anyDamaged) return;

        // Dust 検索
        int dustSlot = findDustSlot(player);
        if (dustSlot == -1) return;

        // Dust 1個消費
        player.getInventory().getItem(dustSlot).shrink(1);

        // 4部位を5耐久ずつ回復
        repairPiece(head);
        repairPiece(chest);
        repairPiece(legs);
        repairPiece(feet);
    }

    private static int findDustSlot(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item.is(ModItems.TEMPORAL_AMBER_DUST.get())) {
                return i;
            }
        }
        return -1;
    }

    private static void repairPiece(ItemStack stack) {
        if (!(stack.getItem() instanceof TemporalAmberArmorItem)) return;
        int currentDamage = stack.getDamageValue();
        if (currentDamage > 0) {
            stack.setDamageValue(Math.max(0, currentDamage - REPAIR_AMOUNT_PER_PIECE));
        }
    }
```

必要 import:
```java
import com.chronodawn.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
```

**バージョン差**:
- 1.20.1: `inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected)` — 同じシグネチャ
- 1.21.5+: シグネチャが変わっている可能性あり — 既存 `EnhancedClockstoneArmorItem` または vanilla `ElytraItem#inventoryTick` 相当の override を同バージョンで確認
- `getLastHurtByMobTimestamp` は Player#getLastHurtByMobTimestamp() が存在するか確認。ない場合は `player.getLastHurtMobTimestamp()` や独自フィールドを使う
- `player.getInventory().getContainerSize()` は 1.21.5+ で API 変更あり

各バージョンで既存の `EnhancedClockstoneArmorItem` の override メソッドを参照し、そのシグネチャに合わせる。

- [ ] **Step 2: Compile all versions**

```bash
./gradlew compileJava
```

- [ ] **Step 3: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/items/equipment/TemporalAmberArmorItem.java
git commit -m "feat: add temporal amber armor auto-repair logic"
```

---

### Task 53: Verify auto-repair (1.21.11)

- [ ] **Step 1: Build + run client**

```bash
./gradlew clean1_21_11 build1_21_11
./gradlew runClientFabric1_21_11
```

- [ ] **Step 2: Manual verification**

1. Creative で Temporal Amber 防具4部位と Temporal Amber Dust をインベントリに入れる
2. 防具を装備
3. 防具の耐久を消費(落下ダメージや叩いて消耗)
4. サバイバルモードに切り替え、モブから離れ、10秒待機
5. 3秒ごとに耐久が5ずつ回復し、粉が1個ずつ減ることを確認
6. モブに殴られた直後は発動しないことを確認(コンバットクールダウン)
7. 粉を全て消費した後、修復が停止することを確認
8. 3部位装備+1部位別ブランドに変える → 修復が発動しないことを確認

---

## Phase 8: Advancements

### Task 54: Create 4 advancement JSONs

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/advancement/chronodawn/entropy_crystal_obtained.json` (または `advancements/` 複数形、既存パスに合わせる)
- Create: `.../temporal_amber_obtained.json`
- Create: `.../temporal_amber_dust_obtained.json`
- Create: `.../temporal_amber_full_set.json`
- 1.20.1 は `advancements/chronodawn/*.json` 複数形の可能性

- [ ] **Step 1: Find existing advancement path**

```bash
find common -name "*.json" -path "*advance*" | head -20
```

- [ ] **Step 2: entropy_crystal_obtained.json**

```json
{
  "parent": "minecraft:story/mine_diamond",
  "display": {
    "icon": { "id": "chronodawn:entropy_crystal" },
    "title": { "translate": "advancements.chronodawn.entropy_crystal_obtained.title" },
    "description": { "translate": "advancements.chronodawn.entropy_crystal_obtained.description" },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": true,
    "hidden": false
  },
  "criteria": {
    "has_entropy_crystal": {
      "trigger": "minecraft:inventory_changed",
      "conditions": {
        "items": [
          { "items": "chronodawn:entropy_crystal" }
        ]
      }
    }
  },
  "requirements": [["has_entropy_crystal"]]
}
```

- [ ] **Step 3: temporal_amber_obtained.json** (同形式、item を raw_temporal_amber に)

- [ ] **Step 4: temporal_amber_dust_obtained.json** (同形式、item を temporal_amber_dust に、parent を temporal_amber_obtained に)

- [ ] **Step 5: temporal_amber_full_set.json** (4部位全て装備判定)

```json
{
  "parent": "chronodawn:chronodawn/temporal_amber_dust_obtained",
  "display": {
    "icon": { "id": "chronodawn:temporal_amber_chestplate" },
    "title": { "translate": "advancements.chronodawn.temporal_amber_full_set.title" },
    "description": { "translate": "advancements.chronodawn.temporal_amber_full_set.description" },
    "frame": "challenge",
    "show_toast": true,
    "announce_to_chat": true,
    "hidden": false
  },
  "criteria": {
    "wearing_full_set": {
      "trigger": "minecraft:inventory_changed",
      "conditions": {
        "items": [
          { "items": "chronodawn:temporal_amber_helmet" },
          { "items": "chronodawn:temporal_amber_chestplate" },
          { "items": "chronodawn:temporal_amber_leggings" },
          { "items": "chronodawn:temporal_amber_boots" }
        ]
      }
    }
  },
  "requirements": [["wearing_full_set"]]
}
```

**注意**: `inventory_changed` は装備スロットではなくインベントリを見る。実際の装備は判定しないが、「4部位すべて所持」で十分な簡略化。将来必要になれば `equipped` トリガーに拡張可能。

- [ ] **Step 6: Advancement lang entries**

ja_jp.json:
```json
  "advancements.chronodawn.entropy_crystal_obtained.title": "時を削る者",
  "advancements.chronodawn.entropy_crystal_obtained.description": "エントロピークリスタルを入手する",
  "advancements.chronodawn.temporal_amber_obtained.title": "時を封じし者",
  "advancements.chronodawn.temporal_amber_obtained.description": "粗時琥珀を入手する",
  "advancements.chronodawn.temporal_amber_dust_obtained.title": "砕かれた時",
  "advancements.chronodawn.temporal_amber_dust_obtained.description": "時琥珀の粉を入手する",
  "advancements.chronodawn.temporal_amber_full_set.title": "琥珀に守られ",
  "advancements.chronodawn.temporal_amber_full_set.description": "時琥珀の防具をすべて入手する",
```

en_us.json:
```json
  "advancements.chronodawn.entropy_crystal_obtained.title": "Eroder of Time",
  "advancements.chronodawn.entropy_crystal_obtained.description": "Obtain an Entropy Crystal",
  "advancements.chronodawn.temporal_amber_obtained.title": "Sealed in Time",
  "advancements.chronodawn.temporal_amber_obtained.description": "Obtain Raw Temporal Amber",
  "advancements.chronodawn.temporal_amber_dust_obtained.title": "Fractured Time",
  "advancements.chronodawn.temporal_amber_dust_obtained.description": "Obtain Temporal Amber Dust",
  "advancements.chronodawn.temporal_amber_full_set.title": "Preserved in Amber",
  "advancements.chronodawn.temporal_amber_full_set.description": "Obtain a full set of Temporal Amber armor",
```

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/advancement*/chronodawn/*.json
git add common/shared/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: add advancements for entropy crystal and temporal amber"
```

---

## Phase 9: Full Verification

### Task 55: checkAll (全バージョンビルド+検証)

- [ ] **Step 1: Run checkAll**

```bash
./gradlew checkAll
```

Expected: 以下全てが PASS:
- cleanAll
- validateResources
- validateTranslations
- buildAll (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11)
- testAll
- gameTestAll

- [ ] **Step 2: Fix any failures**

失敗した場合はバージョン別に調査。典型的なエラー:
- `loot_tables/` vs `loot_table/` ディレクトリ不整合 → Task 12, 32 を確認
- 1.21.11 biome override 未更新 → Task 15, 35 を確認
- `ArmorItem` / `SwordItem` 非継承 vs 継承の混在 → Task 22, 45 を確認
- Translation key missing → Task 16, 20, 26, 36, 42, 50, 54 を確認

---

### Task 56: Manual verification in 3 representative versions

- [ ] **Step 1: 1.21.11 (latest, E group)**

```bash
./gradlew runClientFabric1_21_11
```

確認:
- ChronoDawn ディメンション到達
- 浅層で Entropy Crystal Ore 発見、採掘、剣クラフト、Entropy DoT 発動
- 深層で Temporal Amber Ore 発見、採掘、防具クラフト、粉クラフト
- フルセット装備 → 耐久消費 → 10秒放置 → 自動修復発動
- 4つの advancement が発火
- 日本語・英語テキストが正常

- [ ] **Step 2: 1.21.5 (middle, D group)**

```bash
./gradlew runClientFabric1_21_5
```

同じシナリオを実行(Client Items JSON の動作確認)。

- [ ] **Step 3: 1.21.1 (older, B group)**

```bash
./gradlew runClientFabric1_21_1
```

同じシナリオ + ArmorItem/SwordItem 継承パターンの動作確認。

- [ ] **Step 4: (Optional) 1.20.1 確認**

```bash
./gradlew runClientFabric1_20_1
```

古い loot_tables 複数形ディレクトリの動作確認。

---

### Task 57: Final commit & cleanup

- [ ] **Step 1: git log review**

```bash
git log --oneline | head -60
```

各 phase のコミットが揃っているか確認。

- [ ] **Step 2: Any forgotten files?**

```bash
git status
```

untracked ファイルがあれば整理。

- [ ] **Step 3: (Optional) squash related commits if desired**

大量のコミットを整理したい場合は interactive rebase を user 指示で実施。このプランでは自動 squash は行わない。

---

## Open Implementation Notes

実装時に以下を既存コードで確認すること:

1. **`ModCreativeTabs.CHRONO_DAWN` の正確なアクセス方法**: 既存 ModItems.java の `.arch$tab(...)` 引数を参照
2. **1.21.5+ の `Item.Properties#sword()` および `#humanoidArmor()` メソッド呼び出し**: 既存 `ClockstoneSwordItem.createProperties()` と `EnhancedClockstoneArmorItem.createProperties()` を参照
3. **`hurtEnemy` vs `postHurtEnemy` メソッド名**: 1.21.5+ でリネームの可能性。各バージョンの `EnhancedClockstoneSwordItem` を参照
4. **`Player#getLastHurtByMobTimestamp()` の存在**: 存在しないバージョンでは独自の `lastHurtTick` を WeakHashMap<UUID, Long> で管理するか、`Player#hurtTime` を使う
5. **`inventoryTick` メソッドシグネチャ**: バージョン間で `Entity` → `LivingEntity` などの変更があれば合わせる
6. **loot table の Silk Touch predicate フォーマット**: 1.21.5+ は nested `predicates` → `minecraft:enchantments` 形式。MEMORY の `feedback_loot_table_silk_touch_format.md` を参照
7. **1.21.11 の Identifier import path**: `net.minecraft.resources.Identifier` が正しいか、既存の 1.21.11 ファイルで確認
8. **biome override / biome modifier の正確なパス**: NeoForge と Fabric で場所が異なる可能性。既存の temporal_coal_ore の統合場所を参照
9. **1.21.11 専用の biome override JSON 位置**: MEMORY の `feedback_1_21_11_biome_overrides.md` 参照
10. **equipment asset JSON の配置**: 1.21.5+ で `assets/chronodawn/equipment/*.json` が必要な場合、既存 `enhanced_clockstone.json` の配置を基準に

## Self-Review Notes

このプランは spec の以下セクションを実装する:
- ✅ Naming (Task 1, 16, 20, 26, 36, 42, 50)
- ✅ Ore Generation (Task 14, 15, 34, 35)
- ✅ Resource Chain (Task 12, 32, 38, 41)
- ✅ Crafting Recipes (Task 25, 41, 49)
- ✅ Combat & Performance - Entropy Sword (Task 21-27)
- ✅ Combat & Performance - Entropy Effect (Task 18-20)
- ✅ Combat & Performance - Temporal Amber Armor (Task 44-51)
- ✅ Combat & Performance - Auto-repair (Task 52-53)
- ✅ Advancements (Task 54)
- ✅ Verification (Task 17, 27, 37, 43, 51, 53, 55-57)
- ✅ All 11 version support (throughout)
