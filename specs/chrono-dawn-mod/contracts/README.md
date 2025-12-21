# Contracts: Chrono Dawn Mod

このディレクトリには、Minecraft Mod「Chrono Dawn」のデータコントラクトとビルド設定が含まれています。

**Architecture**: Architectury Multi-Loader (NeoForge + Fabric)

Minecraft Modのコントラクトは、伝統的なWeb APIとは異なり、以下の形式で定義されます:

## Architectury Build Configuration

### Gradle Configuration Files

Architecturyマルチローダー構成のためのGradle設定ファイル:

- **`gradle.properties`** - プロジェクト全体の設定（Minecraftバージョン、依存関係バージョン、Mod情報）
- **`settings.gradle`** - マルチモジュールプロジェクト設定（common, fabric, neoforge）
- **`build.gradle`** - ルートビルドスクリプト（全モジュール共通設定）
- **`build_common.gradle`** - commonモジュールのビルド設定
- **`build_fabric.gradle`** - fabricモジュールのビルド設定
- **`build_neoforge.gradle`** - neoforgeモジュールのビルド設定

### Mod Metadata Files

- **`fabric_mod.json`** - Fabric mod メタデータ（`fabric/src/main/resources/` に配置）
- **`neoforge_mods.toml`** - NeoForge mod メタデータ（`neoforge/src/main/resources/META-INF/` に配置）

## Data Contract Types

### 1. Recipes (レシピ)

アイテムのクラフトレシピを定義するJSON仕様。

例:
- `time_hourglass_recipe.json` - 時の砂時計のレシピ
- `chronoblade_recipe.json` - クロノブレードのレシピ

**Schema**: Minecraft Recipe JSON Schema (1.21.1)
**Location**: `data/chronodawn/recipes/`

### 2. Loot Tables (ルートテーブル)

ブロック破壊、Mob撃破時のドロップアイテムを定義するJSON仕様。

例:
- `time_guardian_loot.json` - 時の番人のドロップテーブル
- `time_tyrant_loot.json` - 時間の暴君のドロップテーブル

**Schema**: Minecraft Loot Table JSON Schema (1.21.1)
**Location**: `data/chronodawn/loot_tables/`

### 3. World Generation (ワールド生成)

構造物、バイオーム、配置ルールを定義するJSON仕様。

例:
- `ancient_ruins_structure.json` - 古代遺跡の構造物定義
- `chronodawn_biome.json` - クロノドーンのバイオーム定義

**Schema**: Minecraft World Generation JSON Schema (1.21.1)
**Location**: `data/chronodawn/worldgen/`

### 4. Dimension (ディメンション)

カスタムディメンションの設定を定義するJSON仕様。

例:
- `chronodawn_dimension.json` - クロノドーンディメンションの定義

**Schema**: Minecraft Dimension JSON Schema (1.21.1)
**Location**: `data/chronodawn/dimension/`

## Sample Contracts

このディレクトリには、主要なアイテムとエンティティのサンプルコントラクトが含まれています。

実装時には、これらのサンプルを参考に完全なデータパックを作成してください。
