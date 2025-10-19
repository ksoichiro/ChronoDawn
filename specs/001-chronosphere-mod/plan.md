# Implementation Plan: Chronosphere Mod - 時間操作をテーマにしたMinecraft Modの開発

**Branch**: `001-chronosphere-mod` | **Date**: 2025-10-19 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-chronosphere-mod/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

時間操作をテーマにしたMinecraft Mod「クロノスフィアの残響」の実装。プレイヤーは時間が停止したディメンション「クロノスフィア」を探索し、時間操作アイテムを駆使してラスボス「時間の暴君」を撃破する。サーバー負荷を抑えつつ、既存のMinecraftシステム(ステータス効果、イベントフック)を活用して時間の歪みを表現する。

## Technical Context

**Language/Version**: Java 21 (Minecraft Java Edition 1.21.1)
**Architecture**: Architectury Multi-Loader Framework (common / fabric / neoforge)
**Primary Dependencies**:
- Architectury API 13.0.8
- NeoForge 21.1.x (neoforgeモジュール)
- Fabric Loader 0.17.2+ + Fabric API 0.115.6+ (fabricモジュール)
- Custom Portal API (Reforged版 / Fabric版)
- mcjunitlib (テスト用)
**Storage**: ワールドデータ(Minecraftの標準セーブシステム)、設定ファイル(JSON/TOML形式)
**Testing**: JUnit 5 + Minecraft GameTest Framework (ハイブリッドアプローチ: 自動テスト80% / 手動テスト20%)
**Target Platform**: Minecraft Java Edition 1.21.1 (NeoForge + Fabric、クライアント + サーバー環境)
**Project Type**: multi-module (Architectury: common/fabric/neoforge)
**Performance Goals**: サーバー負荷を既存のMinecraft環境と比較して10%以内の増加に抑える、60 FPS維持(クライアント側)、Architecturyオーバーヘッドは実質ゼロ
**Constraints**: 既存のMinecraftゲームシステムを最大限活用、マルチプレイヤー環境での動作保証、両ローダー間の互換性維持
**Scale/Scope**: 25機能要件、約30エンティティ/アイテム/ブロック、3フェーズのストーリー進行、4つの構造物生成、2体のボスMob、3モジュール構成

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Note**: プロジェクトの憲法(constitution.md)はテンプレート状態のため、このModプロジェクトに適用可能な設計原則を以下に定義します。

### Design Principles for Chronosphere Mod

1. **Vanilla-First Design**: 既存のMinecraftシステム(ステータス効果、イベントフック、既存のエンティティ挙動)を最大限活用し、独自の複雑な実装を避ける
   - ✅ **PASS**: FR-025で明示的に要求されている。Mob速度低下はステータス効果、ブロック修復はイベントフックで実装予定

2. **Performance Budget**: サーバー負荷を既存環境の+10%以内に抑える
   - ✅ **PASS**: SC-008で測定可能な成功基準として定義されている

3. **Modular Progression**: 各ストーリーフェーズ(P1/P2/P3)は独立してテスト可能でなければならない
   - ✅ **PASS**: 各User Storyに独立したテストシナリオが定義されている

4. **Multiplayer Safety**: マルチプレイヤー環境で個々のプレイヤー体験が独立していること
   - ✅ **PASS**: Assumptionsセクションで明示的に考慮されている

**Status**: すべての設計原則に準拠。Phase 0に進行可能。

### Post-Phase 1 Re-evaluation (Updated for Architectury)

**Date**: 2025-10-19 (Updated for Architectury multi-loader architecture)
**Status**: ✅ **PASS** - Phase 1設計（Architectury構成）も全ての設計原則に準拠

#### Re-evaluation Results:

1. **Vanilla-First Design**: ✅ **PASS**
   - data-model.mdでステータス効果（Slowness IV/V, Speed, Haste）を活用した実装を確認
   - イベントハンドラー（Architectury Event API経由）を使用した実装設計
   - カスタムロジックは最小限（ボスAI、ポータルロジックのみ）
   - **Architectury考慮**: 共通ロジック(common/)でバニラMinecraft APIを優先使用

2. **Performance Budget**: ✅ **PASS**
   - data-model.mdの"Performance Optimization"セクションで具体的な最適化戦略を定義
   - Tick処理最適化（5Tick毎のチェック）
   - ポータルレジストリのキャッシュ戦略
   - ボスAIの状態機械パターン採用
   - **Architectury考慮**: ランタイムオーバーヘッドは実質ゼロ（コードライブラリとして機能）

3. **Modular Progression**: ✅ **PASS**
   - quickstart.mdでPhase 1（P1）、Phase 2（P2）、Phase 3（P3）の独立した実装フェーズを確認
   - 各フェーズは独立したテストが可能（GameTestフレームワーク、各ローダーで実行）
   - **Architectury考慮**: common/モジュールで共通ロジックを80%実装し、モジュール性を強化

4. **Multiplayer Safety**: ✅ **PASS**
   - data-model.mdの"Multiplayer Considerations"セクションで明示的に設計
   - ポータル状態、ボス撃破状態のグローバル管理
   - プレイヤー固有効果（クロノスの瞳）の分離
   - **Architectury考慮**: 両ローダー間で挙動を統一（Architectury Event API、Registry API）

**Additional Principle - Cross-Loader Compatibility** (Architectury導入に伴う追加原則):

5. **Cross-Loader Compatibility**: ✅ **PASS**
   - 共通コード(common/)で80%の機能を実装
   - ローダー固有実装(fabric/, neoforge/)は最小限(20%)
   - @ExpectPlatformパターンで環境依存コードを明示的に分離
   - 既知の問題（エンティティレンダリング）への対処方針を確立

**Conclusion**: Phase 1の設計（Architectury構成）は全ての憲法原則に準拠し、さらにクロスローダー互換性も確保。実装フェーズ（Phase 2: tasks.md生成）に進行可能。

## Project Structure

### Documentation (this feature)

```
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root) - Architectury Multi-Loader Structure

```
Chronosphere/
├── common/                        # ローダー非依存の共通コード
│   ├── src/main/java/com/chronosphere/
│   │   ├── Chronosphere.java      # 共通エントリーポイント
│   │   ├── core/                  # Core systems
│   │   │   ├── dimension/         # Dimension logic (loader-agnostic)
│   │   │   ├── portal/            # Portal mechanics & stabilization
│   │   │   └── time/              # Time manipulation systems
│   │   ├── blocks/                # Custom blocks
│   │   │   ├── ClockstoneOre.java
│   │   │   ├── ReversingTimeSandstone.java
│   │   │   └── UnstableFungus.java
│   │   ├── items/                 # Custom items & tools
│   │   │   ├── base/              # Base materials
│   │   │   ├── tools/             # Tools (pickaxes, etc)
│   │   │   ├── artifacts/         # Ultimate artifacts
│   │   │   └── consumables/       # Food & consumables
│   │   ├── entities/              # Custom mobs & entities
│   │   │   ├── bosses/            # Time Guardian, Time Tyrant
│   │   │   └── DecoyEntity.java  # Echo Boots decoy
│   │   ├── worldgen/              # World generation
│   │   │   ├── structures/        # Ancient Ruins, Library, Clock Tower
│   │   │   └── features/          # Ore placement, tree features
│   │   ├── events/                # Event handlers (Architectury Events)
│   │   │   ├── PlayerEvents.java
│   │   │   ├── EntityEvents.java
│   │   │   └── BlockEvents.java
│   │   ├── registry/              # Architectury Registry wrappers
│   │   │   ├── ModBlocks.java
│   │   │   ├── ModItems.java
│   │   │   └── ModEntities.java
│   │   ├── platform/              # @ExpectPlatform abstractions
│   │   │   └── ChronospherePlatform.java
│   │   └── util/                  # Utility classes
│   └── src/main/resources/
│       ├── data/chronosphere/     # Data packs (recipes, loot tables, etc)
│       │   ├── recipes/
│       │   ├── loot_tables/
│       │   └── worldgen/
│       └── assets/chronosphere/   # Assets (textures, models, sounds)
│           ├── textures/
│           ├── models/
│           └── sounds/
│
├── fabric/                        # Fabric固有実装
│   ├── src/main/java/com/chronosphere/fabric/
│   │   ├── ChronosphereFabric.java           # Fabric entry point
│   │   ├── client/
│   │   │   └── ChronosphereClientFabric.java # Client initialization
│   │   ├── platform/              # @ExpectPlatform implementations
│   │   │   └── ChronospherePlatformImpl.java
│   │   └── compat/                # Fabric API integrations
│   │       └── CustomPortalFabric.java
│   └── src/main/resources/
│       ├── fabric.mod.json        # Fabric mod metadata
│       └── chronosphere.mixins.json
│
├── neoforge/                      # NeoForge固有実装
│   ├── src/main/java/com/chronosphere/neoforge/
│   │   ├── ChronosphereNeoForge.java         # NeoForge entry point
│   │   ├── client/
│   │   │   └── ChronosphereClientNeoForge.java # Client initialization
│   │   ├── platform/              # @ExpectPlatform implementations
│   │   │   └── ChronospherePlatformImpl.java
│   │   ├── event/                 # NeoForge event handlers
│   │   │   └── EntityRendererHandler.java # Manual registration
│   │   └── compat/                # NeoForge API integrations
│   │       └── CustomPortalNeoForge.java
│   └── src/main/resources/
│       ├── META-INF/
│       │   ├── neoforge.mods.toml # NeoForge mod metadata
│       │   └── mods.toml
│       └── pack.mcmeta
│
├── common/src/test/java/com/chronosphere/  # 共通テスト
│   ├── integration/               # Integration tests (run per loader)
│   │   ├── DimensionTest.java
│   │   ├── PortalTest.java
│   │   └── ProgressionTest.java
│   └── unit/                      # Unit tests
│       ├── TimeEffectTest.java
│       └── ItemEffectTest.java
│
├── build.gradle                   # Root build configuration
├── settings.gradle                # Multi-module settings
├── gradle.properties              # Gradle properties
└── architectury.properties        # Architectury configuration
```

**Structure Decision**: Architecturyマルチローダー構造を採用。共通ロジック(common/)で80%を実装し、ローダー固有実装(fabric/, neoforge/)は最小限(20%)に抑制。@ExpectPlatformパターンで環境依存コードを明示的に分離し、両ローダー間の互換性を維持。

## Complexity Tracking

*Fill ONLY if Constitution Check has violations that must be justified*

**Status**: Constitution Checkに違反なし。複雑性の正当化は不要。

